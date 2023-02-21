import redis
import json
import alluxio
from alluxio import option
r = redis.Redis(host='136.145.77.83', port=6379)
client = alluxio.Client('136.145.77.107', 39999)

def imgClas(directory, property, boolean):
    print(100)

def start():
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    # type, file_location, function_name, property, boolean
    print(task)
    json_dic = json.loads(task)

    print(json_dic)

    if json_dic["plan"]:
        array = json_dic["plan"]
        for n in array:
            print(n)

    list = client.ls("/image")

    for n in list:
        print(n)

start()