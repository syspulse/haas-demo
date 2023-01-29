from pyspark.sql import SparkSession
from pyspark.sql import functions as F

spark = SparkSession.builder.getOrCreate()

df = spark.createDataFrame([('row_p_a', 'row_p_b'), ('row_q_a', 'row_q_b'), ('row_r_a', 'row_r_b')],['col_a','col_b'])

print('Rows: {}'.format(df.count()))

df = df.filter(F.col('col_a').contains('p'))
df.show()
