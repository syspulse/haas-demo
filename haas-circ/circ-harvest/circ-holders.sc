// ATTENTION: the sequence of deps loading is important !

// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

// ATTENTION: Versions are VERY important !
import $ivy.`org.apache.spark::spark-core:3.2.0` 
import $ivy.`org.apache.spark::spark-sql:3.2.0` 
import $ivy.`org.apache.hadoop:hadoop-aws:3.2.2`

import org.apache.spark.sql.{SparkSession,Dataset}
import org.apache.spark.sql.functions._

import java.time._
import java.math.BigInteger

val region = Option(System.getenv("AWS_REGION")).getOrElse("eu-west-1")
val geth = Option(System.getenv("ETH_RPC")).getOrElse("http://192.168.1.13:8545")

// UNI Contract: 0x1f9840a85d5af5bf1d1762f925bdaddc4201f984
// exported data: ./eth-export.sh 10861674 10866675 |grep 0x1f9840a85d5af5bf1d1762f925bdaddc4201f984 >UNI-5000.csv

def hex(s:String) = new BigInteger(s.drop(2),16)

// curl -X POST --data '{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984", "data":"0x70a082310000000000000000000000000b88516a6d22bf8e0d3657effbd41577c5fd4cb7"}, "latest"],"id":67}' -H "Content-Type: application/json" http://127.0.0.1:8545/
// balanceOf("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984","0x41653c7d61609d856f29355e404f310ec4142cfb")
def balanceOf(token:String,addr:String) = {
  val data = "0x70a08231000000000000000000000000" + addr.drop(2)
  val id = 3
  val jsonData = s"""{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "${token}", "data":"${data}"}, "latest"],"id":${id}}"""
  val res = requests.post(geth,data = jsonData, headers=Map("Content-Type"->"application/json"))
  val value = hex(ujson.read(res.text).obj("result").str)
  value
}

case class Holder(addr:String,value:Double)

@main
def main(input:String="./UNI-1000.csv", output:String = "./UNI-1000-Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  val ss = SparkSession.builder().appName("circ-holders").config("spark.master", "local").config("default.parallelism",1).config("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain").getOrCreate()
  import ss.implicits._

  val ts0 = Instant.now

  val df = ss.read.option("header", "false").format("com.databricks.spark.csv").option("inferSchema", "true").csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number")
  df.printSchema()

  val receivers = df.select("to_address")
  val holdersAll = df.dropDuplicates("to_address").sort(col("value").desc).select("to_address","value").collect

  val holdersBalance = holdersAll.map( r => (r.get(0).toString,balanceOf("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984",r.get(0).toString)))
  
  // need to resort because balance is different now
  val holders = holdersBalance.map( h => Holder(h._1,h._2.doubleValue / decimals)).sortBy(- _.value).take(25)

  holders.foreach{ println _ }
  
  //df.write.option("maxRecordsPerFile", batch).option("compression", "gzip").mode("overwrite").format(codec).save(output);
  
  val ts1 = Instant.now
  val elapsed = Duration.between(ts0, ts1)
  println(s"Elapsed time: ${elapsed.toMinutes} min (${elapsed.toSeconds} sec)")
}