// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8
import java.util.UUID

//import $file.Tokens,Tokens.Tokens
//import $file.ERC20,ERC20.ERC20

import $ivy.`io.syspulse::circ-core:0.0.1`
import $ivy.`io.syspulse::circ-harvest:0.0.1`

import io.syspulse.haas.circ._
import io.syspulse.haas.circ.Holder
import io.syspulse.haas.circ.Circulation

implicit val rw1: RW[Holder] = macroRW
implicit val rw2: RW[Circulation] = macroRW

@main
def main(input:String="./UNI-1000.csv", token:String = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, token=${token}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  val ss = SparkSession.builder().appName("circ-holders").config("spark.master", "local").config("default.parallelism",1).config("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain").getOrCreate()
  import ss.implicits._

  val ts0 = Instant.now

  val df = ss.read.option("header", "false").format("com.databricks.spark.csv").option("inferSchema", "true").csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number")
  df.printSchema()

  val accountBalanceDelta = df
    .where(s"token_address == '${token}'")
    .select("from_address","to_address","value","block_number").sort("block_number")
    .flatMap(r => Seq((r.getInt(3),r.getString(0),BigInt(r.getDecimal(2).negate.toBigInteger)),(r.getInt(3),r.getString(1),BigInt(r.getDecimal(2).toBigInteger))))

  val accountBalanceDeltaCollected = accountBalanceDelta.collect
  val holders = Supply.holders(accountBalanceDeltaCollected,10)
  
  // val receivers = df.select("to_address")
  // val holdersAll = df.dropDuplicates("to_address").sort(col("value").desc).select("to_address","value")

  //val holdersBalance = holdersAll.collect.map( r => (r.get(0).toString,ERC20.balanceOf(Tokens.UNI,r.get(0).toString)))
  // need to resort because balance is different now
  //val holders = holdersBalance.map( h => Holder(h._1,h._2.doubleValue / decimals)).sortBy(- _.value).take(25).toList

  holders.foreach{ println _ }
  
  //df.write.option("maxRecordsPerFile", batch).option("compression", "gzip").mode("overwrite").format(codec).save(output);
  
  val ts1 = Instant.now
  val elapsed = Duration.between(ts0, ts1)
  println(s"Elapsed time: ${elapsed.toMinutes} min (${elapsed.toSeconds} sec)")

  val circ = Circulation(id=UUID.randomUUID(), tid = "UNI", holders = holders)
  println(circ)
  println(write(circ))
}