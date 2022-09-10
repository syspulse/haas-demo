//
// This scrpit must be run with spark-shell or Almond Spark Shell ! (does not work from plain Ammonite)

// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

import $ivy.`io.syspulse::circ-core:0.0.1`
import $ivy.`io.syspulse::circ-harvest:0.0.1`

import io.syspulse.haas.circ._
import io.syspulse.haas.circ.Holder
import io.syspulse.haas.circ.Circulation

implicit val rw1: RW[Holder] = macroRW
implicit val rw2: RW[Circulation] = macroRW

import $file.Tokens,Tokens.Tokens
import $file.ERC20,ERC20.ERC20


@main
def main(input:String="./UNI-1000.csv", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  val ss = SparkSession.builder().appName("circ-total").config("spark.master", "local").config("default.parallelism",1).config("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain").getOrCreate()
  import ss.implicits._

  val ts0 = Instant.now

  val df = ss.read.option("header", "false").format("com.databricks.spark.csv").option("inferSchema", "true").csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number")
  df.printSchema()

  // sorted by Block Number !
  val accountBalanceDelta = df
    .select("from_address","to_address","value","block_number")
    .sort("block_number")
    .flatMap(r => Seq((r.getInt(3),r.getString(0),BigInt(r.getDecimal(2).negate.toBigInteger)),(r.getInt(3),r.getString(1),BigInt(r.getDecimal(2).toBigInteger))))
  
  val accountBalanceDeltaCollected = accountBalanceDelta.collect
  
  val supply = Supply.history(accountBalanceDeltaCollected)

  supply.foreach{ println _ }
  
  //df.write.option("maxRecordsPerFile", batch).option("compression", "gzip").mode("overwrite").format(codec).save(output);
  
  val ts1 = Instant.now
  val elapsed = Duration.between(ts0, ts1)
  println(s"Elapsed time: ${elapsed.toMinutes} min (${elapsed.toSeconds} sec)")

  // val circ = Circulation(holders)
  // val circJson = write(circ)
  // println(circJson)
}