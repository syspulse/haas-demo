// ATTENTION: the sequence of deps loading is important !

// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

// ATTENTION: Versions are VERY important !
import $ivy.`org.apache.spark::spark-core:3.2.0` 
import $ivy.`org.apache.spark::spark-sql:3.2.0` 
import $ivy.`org.apache.hadoop:hadoop-aws:3.2.2`

import org.apache.spark.sql.{SparkSession,Dataset}
import org.apache.spark.sql.functions._

import $ivy.`mysql:mysql-connector-java:8.0.22`

import upickle.default._
import upickle.default.{ReadWriter => RW, macroRW}

import java.time._
import java.math.BigInteger

val region = Option(System.getenv("AWS_REGION")).getOrElse("eu-west-1")
val geth = Option(System.getenv("ETH_RPC")).getOrElse("http://192.168.1.13:8545")

def toHex(s:String) = new BigInteger(s.drop(2),16)

