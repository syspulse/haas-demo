// run with ammonite prefed with spark:
// amm-spark3

import org.apache.spark.sql.functions.desc
import spark.implicits._

// read all files !
val df = spark.read.format("csv").option("header", "false").option("inferSchema", "true").load("output/*/*/*").toDF("block_timestamp","transaction_index","hash","block_number","from_address","to_address","gas","gas_price","input","value")

// sort by transfer value
df.select("value").orderBy(desc("value")).map(r=>(r.getDecimal(0))).map(v => v.doubleValue).limit(20).collect

// DF -> DF
df.select("value").map(r=>(r.getDecimal(0))).map(v => v.doubleValue / 1e18).orderBy(desc("value")).limit(10).toDF.show