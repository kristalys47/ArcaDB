
import redis

r = redis.Redis(host='136.145.77.83', port=6379)
rresults = redis.Redis(host='136.145.77.83', port=6380)

r.flushall()
rresults.flushall()

tables = ["orders", "supplier", "region", "partsupp", "part", "nation", "customer", "lineitem"]
numbers = [22, 1, 1, 6, 3, 1, 2, 132]

for table in tables:
    r.set(table+"_partition_size", "20000")

for n in range(132):
    for index in range(len(tables)):
        if(n < numbers[index]):
            r.rpush(tables[index], "/" + tables[index] + "/" + tables[index] + str(n) + ".orc")


