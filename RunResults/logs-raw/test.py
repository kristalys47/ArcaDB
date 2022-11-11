import requests
import sys
import json
import os
import time
import threading

def getlogs():
    os.system("scp root@136.145.77.94:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.101:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.120:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.124:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.118:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.119:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.106:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.78:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.99:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.80:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.79:/var/lib/docker/containers/*/*-json.log .")

def processlogs():
    with open("logs.log", "w") as logs:
        for n in os.listdir():
            if(n.find("-json.log")>0):
                print(n)
                f_object = open(n, "r")
                lines = f_object.readlines()
                for line in lines:
                    log = json.loads(line)
                    logs.write(log["time"] + " " + log["log"])

def test():
    URL = "http://136.145.77.80:7271/database/query"
    # sending get request and saving the response as response object
    for n in [60]:
        json = {
            "mode": 3,
            "buckets": n,
            "query": "select * from part, lineitem where lineitem.\"01\" = part.\"00\"",
            "query1": "select * from lineitem, orders where lineitem.\"00\" = orders.\"00\""
            }
        print(n)
        for n in range(3):
            os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /join"')
            os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio clearCache"')
            os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /results"')
            r = requests.get(url = URL, json = json)
            print(r.elapsed)
            sys.stdout.flush()

def getlogsfromVms(n):
    os.system("scp root@136.145.77.%d:/root/worker/WorkerThreads/logs* log%dt.log "%(n, n))

def getlogsfromVmslist():
    array =  [94, 79, 99, 80, 78, 106, 119, 118, 124, 120, 101]
    dic = []
    for n in array:
         x = threading.Thread(target=getlogsfromVms, args=(n,))
         x.start()
         dic.append(x)
    for n in dic:
        n.join()
def addvmlogstolog():
    with open("logs.log", "w") as logs:
        for n in os.listdir():
            if(n.find("t.log")>0):
                print(n)
                f_object = open(n, "r")
                lines = f_object.readlines()
                for line in lines:
                    logs.write(line)


# os.system("scp root@136.145.77.79:/root/worker/WorkerThreads/logs* loggert.log ")
test()
# getlogsfromVmslist
# os.system("scp root@136.145.77.80:/var/lib/docker/containers/*/*-json.log .")
# addvmlogstolog()
# processlogs()
# getlogsandprocessnossh()
