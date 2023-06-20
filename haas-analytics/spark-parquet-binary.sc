// spark-3.2.0/bin/spark-shell
// 

val df = spark.read.parquet("/mnt/share/data/paradigm/transfers/00000000/*.parquet")
val hexToBigInt = udf((bytes: Array[Byte]) => BigInt(bytes))
val hex = udf((bytes: Array[Byte]) => "0x"+bytes.map("%02x".format(_)).mkString)
val df2 = df.withColumn("value",hexToBigInt(col("value"))).withColumn("from",hex(col("from_address"))).withColumn("to",hex(col("to_address")))
df2.filter("value < 31337").select("from","to","value").show(10)
