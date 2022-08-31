import os
import sys
import logging
from botocore.exceptions import ClientError
import pandas as pd
import boto3
import math

# os.system('cat /home/kristalys/git/Container-DBMS/redissetupAlreadyUp.sh')"
stream = os.popen('cat /home/kristalys/git/Container-DBMS/redissetupAlreadyUp.sh')
n = stream.read()
PAGE_SIZE = 256*1024*1024
AWS_S3_ACCESS_KEY = 'AKIA6E4TYZ3JLKC2LPFR'
AWS_S3_SECRET_KEY = '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'
S3_BUCKET = "testingjoin"
session = boto3.Session(
    aws_access_key_id=AWS_S3_ACCESS_KEY,
    aws_secret_access_key=AWS_S3_SECRET_KEY,
)
s3 = session.resource('s3')

def upload_file(file_name, bucket, object_name=None):
    # If S3 object_name was not specified, use file_name\n",
    if object_name is None:
        object_name = os.path.basename(file_name)
        try:
            response = s3.upload_file(file_name, S3_BUCKET, object_name)
        except ClientError as e:
            logging.error(e)
            return False
    return True


def csv():
    csv_file = {"dbgen/orders.tbl", "dbgen/supplier.tbl", "dbgen/region.tbl",
                "dbgen/partsupp.tbl", "dbgen/part.tbl", "dbgen/orders.tbl",
                "dbgen/nation.tbl", "dbgen/lineitem.tbl", "dbgen/customer.tbl"}
    for n in csv_file:
        data = pd.read_csv(n, delimiter='|', header=None)
        data = data.iloc[:, :-1]

        schema = data.tail(1)
        schema.to_json("test.json", orient="records")  # output file
        single_row_size = math.ceil((data.tail(10).memory_usage(index=False, deep=True).sum())/10)
        print(single_row_size)

        schema = os.popen('java -jar orc-tools-1.7.5-uber.jar json-schema test.json | grep -o struct.[^\>]*\>').read()
        print(schema)
        os.system('rm test.json')

        low = 0  # Initial Lower Limit
        offset = PAGE_SIZE//single_row_size
        high = offset  # Initial Higher Limit
        index = 0
        while high < len(data):
            df_new = data[low:high]  # subsetting DataFrame based on index
            low = high  # changing lower limit
            high = high + offset  # givig uper limit with increment of 1000
            filename = n.split(".")[0] + str(index) + ".csv"
            filenameorc = n.split(".")[0] + str(index) + ".orc"
            df_new.to_csv(filename, header=None, index=None)  # output file
            index = index + 1
            os.system("java -jar orc-tools-1.7.5-uber.jar convert {} -o {} --schema \'{}>\'".format(filename,
                                                                                                   filenameorc,
                                                                                               schema.split(">")[0]))
            os.system('rm {}'.format(filename))


csv()
