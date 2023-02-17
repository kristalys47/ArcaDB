import redis
import json
r = redis.Redis(host='136.145.77.83', port=6379)

def imgClas(directory, property, boolean):
    print(100)

def start():
    task = r.blpop("python", 0)
    # type, file_location, function_name, property, boolean
    print(task)
    json_dic = json.load(task)

    print(json_dic)

    if json_dic["plan"]:
        array = json_dic["plan"]
        for n in array:
            print(n)

start()