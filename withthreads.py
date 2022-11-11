import sys
import os
import threading

def runcomputers(n):
    # os.system("ssh root@136.145.77.%d apt install maven -y"%n)
    os.system("ssh root@136.145.77.%d rm -r worker"%(n))
    os.system("ssh root@136.145.77.%d mkdir /root/worker"%(n))
    # # # os.system("scp runmvn0.sh root@136.145.77.%d:/root/worker/."%(n))
    # # # os.system("ssh root@136.145.77.%d \"cd worker ; bash runmvn0.sh\""%(n))
    os.system("scp -r WorkerThreads root@136.145.77.%d:/root/worker"%(n))
    os.system("ssh root@136.145.77.%d \"cd worker/WorkerThreads ; mvn clean install\""%(n))
    os.system("ssh root@136.145.77.%d \"cd worker/WorkerThreads ; bash runmvn.sh\""%(n))


for n in [94, 79, 99, 80, 78, 106, 119, 118, 124, 120, 101]:
     x = threading.Thread(target=runcomputers, args=(n,))
     x.start()


# ssh -l root@136.145.77.%d su echo "org.slf4j.simpleLogger.showDateTime=true" >> /etc/maven/logging/simplelogger.properties
