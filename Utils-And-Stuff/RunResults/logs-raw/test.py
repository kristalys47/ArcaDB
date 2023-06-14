import requests
import sys
import json
import os
import time
import threading

def cleanImages():
    os.system("ssh root@136.145.77.94  docker image prune -f")
    os.system("ssh root@136.145.77.101 docker image prune -f")
    os.system("ssh root@136.145.77.120 docker image prune -f")
    os.system("ssh root@136.145.77.124 docker image prune -f")
    os.system("ssh root@136.145.77.118 docker image prune -f")
    os.system("ssh root@136.145.77.119 docker image prune -f")
    os.system("ssh root@136.145.77.106 docker image prune -f")
    os.system("ssh root@136.145.77.78  docker image prune -f")
    os.system("ssh root@136.145.77.99  docker image prune -f")
    os.system("ssh root@136.145.77.80  docker image prune -f")
    os.system("ssh root@136.145.77.79  docker image prune -f")
    os.system("ssh root@136.145.77.86  docker image prune -f")
    os.system("ssh root@136.145.77.88  docker image prune -f")


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
    os.system("scp root@136.145.77.86:/var/lib/docker/containers/*/*-json.log .")
    os.system("scp root@136.145.77.88:/var/lib/docker/containers/*/*-json.log .")

def getContainerLogs(n):
    os.system("scp root@136.145.77.{}:/var/lib/docker/containers/*/*-json.log .".format(n))

def getContainersLogs():
    dic = []
    array =  [94, 79, 99, 78, 106, 119, 118, 124, 120, 101, 80, 86, 88]
    # array =  [80, 86, 88]

    for n in array:
         x = threading.Thread(target=getContainerLogs, args=(n,))
         x.start()
         dic.append(x)
    for n in dic:
        n.join()

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
            "query234": "select a.eyeglasses, a.bangs from image as a",
            "query321": "select * from image as a where a.eyeglasses=1 and a.bangs=1",
            "query11": "select a.eyeglasses, a.id from customer as b inner join image as a on (a.id=b.\"00\") where a.eyeglasses=1 and b.\"03\">20",
            "query250": "select * from part, lineitem where lineitem.\"01\" = part.\"00\"",
            "query1": "select * from lineitem, orders where lineitem.\"00\" = orders.\"00\"",
            "query2": "select a.id, a.molecular_formula, a.canonical_smiles, a.isomeric_smiles, a.molecular_weight from pubchem as a where a.molecular_weight=0",
            "query3445": "select a.id, a.molecular_formula, a.canonical_smiles, a.isomeric_smiles, a.molecular_weight from pubchem as a where a.molecular_weight>437.9",
            "query": "select a.id, a.molecular_formula, a.canonical_smiles, a.isomeric_smiles, a.molecular_weight from pubchem as a where a.molecular_weight>437.9 and a.exact_mass>200"

            }
        print(n)
        for n in range(3):
            # os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /join"')
            # os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio clearCache"')
            # os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /results"')
            os.system('ssh root@136.145.77.80 "python3 /root/load-redis-100GB.py"')
            r = requests.get(url = URL, json = json)
            print(r.elapsed)
            sys.stdout.flush()

def getlogsfromVms(n):
    os.system("scp root@136.145.77.%d:/root/worker/WorkerThreads/logs* log%dtt.log "%(n, n))

def getlogsfromVmslist():
    array =  [94, 79, 99, 78, 106, 119, 118, 124, 120, 101]
    # array =  [94, 79]
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


# os.system("scp root@136.145.77.124:/root/worker/WorkerThreads/logs* loggert.log ")
# getlogsfromVmslist()
test()
# cleanImages()

# getlogsfromVmslist
# os.system("scp root@136.145.77.80:/var/lib/docker/containers/*/*-json.log .")
# addvmlogstolog()
# processlogs()
# getlogsandprocessnossh()
# time.sleep(60*30)
# getContainersLogs()
