import os
import json
import time

def clear_images():
    array =  [94, 79, 99, 78, 106, 119, 118, 124, 120, 101, 80, 86, 88]
    for i in array:
        os.system("ssh root@136.145.77.{} 'docker image prune -f -a'".format(i))


def clear_alluxios();
    os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /join"')
    os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio clearCache"')
    os.system('ssh root@136.145.77.83 "./alluxio-2.8.1/bin/alluxio fs rm -r /results"')
