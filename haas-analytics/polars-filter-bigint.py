import polars as pl
df = pl.scan_parquet("ethereum__transactions__10861674_to_10861674.parquet")
df.select(pl.col("value").cast(pl.Float32)).filter(pl.col("value") >= 4.0e19).collect()

# Filter with BigInt
df.select(pl.col("value"),pl.col("value").apply(lambda v: int(v) >= 40000000000000000000).alias("res")).filter(pl.col("res") == True).collect()
