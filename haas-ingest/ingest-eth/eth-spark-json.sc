// run with ammonite prefed with spark:
// amm-spark3

import org.apache.spark.sql.functions.desc
import spark.implicits._

val df = spark.read.format("json").option("inferSchema", "true").load("tx-1734.log")

//val df = spark.read.format("csv").option("inferSchema", "false").load("output/2022/09/20/")

// sort by transfer value
df.select("value").orderBy(desc("value")).map(r=>(r.getDecimal(0))).map(v => v.doubleValue).limit(20).collect

// DF -> DF
df.select("value").map(r=>(r.getDecimal(0))).map(v => v.doubleValue / 1e18).orderBy(desc("value")).limit(10).toDF.show