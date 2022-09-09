// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

import $file.Tokens,Tokens.Tokens
import $file.ERC20,ERC20.ERC20

case class Holder(addr:String,value:Double)

case class Circulation(holders:List[Holder])

object Holder{ implicit val rw: RW[Holder] = macroRW }
//import Holder._
object Circulation{ implicit val rw: RW[Circulation] = macroRW }
//import Circulation._

@main
def main(input:String="./UNI-1000.csv", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  val ss = SparkSession.builder().appName("circ-holders").config("spark.master", "local").config("default.parallelism",1).config("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain").getOrCreate()
  import ss.implicits._

  val ts0 = Instant.now

  val df = ss.read.option("header", "false").format("com.databricks.spark.csv").option("inferSchema", "true").csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number")
  df.printSchema()

  val receivers = df.select("to_address")
  val holdersAll = df.dropDuplicates("to_address").sort(col("value").desc).select("to_address","value")

  val holdersBalance = holdersAll.collect.map( r => (r.get(0).toString,ERC20.balanceOf(Tokens.UNI,r.get(0).toString)))
  
  // need to resort because balance is different now
  val holders = holdersBalance.map( h => Holder(h._1,h._2.doubleValue / decimals)).sortBy(- _.value).take(25).toList

  holders.foreach{ println _ }
  
  //df.write.option("maxRecordsPerFile", batch).option("compression", "gzip").mode("overwrite").format(codec).save(output);
  
  val ts1 = Instant.now
  val elapsed = Duration.between(ts0, ts1)
  println(s"Elapsed time: ${elapsed.toMinutes} min (${elapsed.toSeconds} sec)")

  // read from JDBC
  val jdbcDF = ss.read.format("jdbc").option("url", "jdbc:mysql://localhost:3306/haas_db").option("user", "haas_user").option("password", "haas_pass").option("dbtable", "haas_db.HOLDERS").load()
  
  // overwrite results (append == INSERT)
  val hDF = ss.createDataFrame(holders)
  hDF.write.format("jdbc").option("url", "jdbc:mysql://localhost:3306/haas_db").option("user", "haas_user").option("password", "haas_pass").option("dbtable", "haas_db.HOLDERS").mode("overwrite").save()

  // the same
  val dbProp = new java.util.Properties
  Map("driver" -> "com.mysql.jdbc.Driver","user" -> "haas_user","password" -> "haas_pass").foreach{ case(k,v) => dbProp.setProperty(k,v)}
  val dbUrl = "jdbc:mysql://localhost:3306/haas_db"
  val dbTable = "HOLDERS"
  hDF.write.mode("overwrite").jdbc(dbUrl, dbTable, dbProp)

  val circ = Circulation(holders)
  val circJson = write(circ)
  println(circJson)
}