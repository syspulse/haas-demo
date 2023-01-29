from pyspark.sql import SparkSession

spark = SparkSession.builder.getOrCreate()
schema = "token_address string, from_address string, to_address string, value decimal, transaction_hash string, log_index int, block_number int, ts long"

df = spark.read.schema(schema).csv('s3a://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/10/13/token-23_53_11.csv',inferSchema=True)

# df = spark.read.schema(schema).csv('s3a://haas-data-dev/data/dev/ethereum/raw/csv/tokens/2022/*/*/*',inferSchema=True)

df.show(10)
df.count()
df.where("from_address == '0x0000000000000000000000000000000000000000'").select("to_address","value").show(10,100)