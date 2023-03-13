
import redis
import json as js

tables = ["orders", "supplier", "region", "partsupp", "part", "nation", "customer", "lineitem"]
numbers = [229, 1, 1, 63, 34, 1, 24, 1201]
# r.set("joinTupleLength", 1000000)
# for table in tables:
#     r.set(table+"_partition_size", "20000")

for n in range(len(numbers)):
    json = {}
    json_array = []
    json["type"] = "structured"
    json["image"] = tables[n]
    for i in range(numbers[n]):
        json_array.append(tables[n] + str(i) + ".orc")
    json["files"] = json_array
    with open(tables[n]+"/metadata.json", "w") as f:
        f.write(js.dumps(json))
