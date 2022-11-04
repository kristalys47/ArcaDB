from pyspark.sql import SparkSession

spark = SparkSession.builder.master("spark://136.145.77.83:7077").getOrCreate()
df2 = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/part/part2.orc")
# sc = spark.sparkContext
#
# df = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/nation/")
