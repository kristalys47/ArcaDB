
import redis

r = redis.Redis(host='136.145.77.83', port=6379)

r.flushall()

tables = ["orders", "supplier", "region", "partsupp", "part", "nation", "customer", "lineitem"]
numbers = [229, 1, 1, 63, 34, 1, 24, 1201]
r.set("joinTupleLength", 1000000)
r.set("partition_size", "20000")

for n in range(1201):
    for index in range(len(tables)):
        if(n < numbers[index]):
            r.rpush(tables[index], "/db100GB/" + tables[index] + "/" + tables[index] + str(n) + ".orc")
