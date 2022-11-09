from pyspark.sql import SparkSession

spark = SparkSession.builder.master("spark://136.145.77.83:7077").getOrCreate()
df2 = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/part/part2.orc")
# sc = spark.sparkContext
#
# df = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/nation/")


f = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/lineitem/")
g = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/part/")

gdf = g.toDF("id", "01", "02", "03", "04", "05", "06", "07", "08")
fdf = f.toDF("00", "pid", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15")
f.createOrReplaceTempView("lineitem")
g.createOrReplaceTempView("part")




f = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/lineitem/")
g = spark.read.orc("alluxio://136.145.77.107:19998/db10GB/part/")

gdf = g.toDF("id", "01", "02", "03", "04", "05", "06", "07", "08")
fdf = f.toDF("00", "pid", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15")
fdf.createOrReplaceTempView("lineitem")
gdf.createOrReplaceTempView("part")
