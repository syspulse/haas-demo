# Datalake

## S3 Taxonomy 

```
/data
  /{env}
    /{network}
      /token
        /uniswap
          info.csv
          /holders
              /{YYYY}/{mm}/{DD}
                 holders.csv
          /transactions

      /circulation
        /uniswap
          /{YYYY}/{mm}/{DD}/
            uniswap.csv      
      /raw
        csv/
          /blocks
            /{YYYY}/{mm}/{DD}/
              blocks.json
          /transactions
          /tokens
          /logs
        parq/
          /blocks
            /YYYY/mm/DD/
              blocks.parq
          /transactions           
         

```

1. __parq__ - Parquet format (Spark transformed)


## Spark (EMR)

Connect to EMR Master:

```
ssh -i ../../keys/haas-key hadoop@emr.hacken.cloud
```


### Scala 

Simple test script

`spark-shell`:


Simple quick version
```
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

val input = "s3://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/10/13/token-23_53_11.csv"
val df1 = spark.read.csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number","ts")
val df = df1.withColumn("value",col("value").cast(DecimalType(38, 0)))
df.printSchema
df.show(10)
```

Correct version for Schema and partitioned folders (tokens)
```
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

val input = "s3://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/*/*/"
val df1 = spark.read.format("com.databricks.spark.csv").schema("token_address string, from_address string, to_address string, value decimal, transaction_hash string, log_index int, block_number int, ts long").load(input)
val df = df1
df.printSchema
df.show(10)
```

### PySpark

```
from pyspark.sql import SparkSession
spark = SparkSession.builder.getOrCreate()
schema = "token_address string, from_address string, to_address string, value decimal, transaction_hash string, log_index int, block_number int, ts long"
# df = spark.read.schema(schema).csv('s3a://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/10/13/token-23_53_11.csv',inferSchema=True)
df = spark.read.schema(schema).csv('s3a://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/10/*/*',inferSchema=True)
df.show(10)
```

