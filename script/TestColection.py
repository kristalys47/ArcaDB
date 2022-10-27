import os
import json
import time

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

# for n in range(12):
    # time.sleep(60*5)
getlogs()
with open("logs.log", "w") as logs:
    for n in os.listdir():
        if(n.find("-json.log")>0):
            print(n)
            f_object = open(n, "r")
            lines = f_object.readlines()
            for line in lines:
                log = json.loads(line)
                logs.write(log["time"] + " " + log["log"])
