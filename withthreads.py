import sys
import os
import threading

def runcomputers(n):
    # os.system("ssh root@136.145.77.%d apt install maven -y"%n)
    os.system("ssh root@136.145.77.%d rm -r /db100GB"%(n))
    # os.system("ssh root@136.145.77.%d rm -r /db50GB"%(n))
    # os.system("ssh root@136.145.77.%d rm -r /db10GB"%(n))

    os.system("ssh root@136.145.77.%d \"pkill -f '/usr/bin/java*' ; pkill -u root 'bash*' \""%(n))
    os.system("ssh root@136.145.77.%d rm -r worker"%(n))
    # os.system("ssh root@136.145.77.%d mkdir /root/worker"%(n))
    os.system("ssh root@136.145.77.%d mkdir -p /root/worker/WorkerThreads"%(n))
    # os.system("scp runmvn0.sh root@136.145.77.%d:/root/worker/."%(n))
    # os.system("ssh root@136.145.77.%d \"cd worker ; bash runmvn0.sh\""%(n))

    # os.system("scp -r WorkerThreads root@136.145.77.%d:/root/worker"%(n))

    os.system("scp -r WorkerThreads/src root@136.145.77.%d:/root/worker/WorkerThreads"%(n))
    os.system("scp WorkerThreads/.env root@136.145.77.%d:/root/worker/WorkerThreads"%(n))
    os.system("scp WorkerThreads/pom.xml root@136.145.77.%d:/root/worker/WorkerThreads"%(n))
    os.system("scp WorkerThreads/runmvn.sh root@136.145.77.%d:/root/worker/WorkerThreads"%(n))

    os.system("ssh root@136.145.77.%d \"cd worker/WorkerThreads ; mvn clean install ; bash runmvn.sh\""%(n))
    # os.system("ssh root@136.145.77.%d \"cd worker/WorkerThreads ; export $(xargs <.env) ; echo $MODE\""%(n))
    # os.system("ssh root@136.145.77.%d \"cd worker/WorkerThreads ; export $(grep -v '^#' .env | xargs) ; mvn exec:java -Dexec.mainClass=\"orc.main\" -Dexec.args=\"${S3_BUCKET} ${AWS_S3_ACCESS_KEY} ${AWS_S3_SECRET_KEY} ${REDIS_HOST} ${REDIS_PORT} ${REDIS_HOST_TIMES} ${REDIS_PORT_TIMES} ${WORKER_APP_PORT} ${COORDINATOR_APP_PORT} ${POSTGRES_PASSWORD} ${POSTGRES_USERNAME} ${POSTGRES_HOST} ${POSTGRES_PORT} ${POSTGRES_DB_NAME} ${MODE}\" -Dexec.cleanupDaemonThreads=false -Dsbt.classloader.close=false --log-file logs.log\""%(n))

array =  [94, 79, 99, 80, 78, 106, 119, 118, 124, 120, 101]
# array =  [94, 79]
for n in array:
     x = threading.Thread(target=runcomputers, args=(n,))
     x.start()


# ssh -l root@136.145.77.%d su echo "org.slf4j.simpleLogger.showDateTime=true" >> /etc/maven/logging/simplelogger.properties
