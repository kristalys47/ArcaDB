import os
import sys
import logging
import pandas as pd
import math
import alluxio
from alluxio import option


# os.system('cat /home/kristalys/git/Container-DBMS/redissetupAlreadyUp.sh')"
stream = os.popen('cat /home/kristalys/git/Container-DBMS/redissetupAlreadyUp.sh')
n = stream.read()
PAGE_SIZE = 256*1024*1024



def upload_file(file_name, object_name):
    print(file_name)
    print(object_name)
    client = alluxio.Client('136.145.77.107', 39999)
    opt = option.CreateFile(recursive=True)
    # # f = client.create_file(object_name +'/'+ file_name, opt)
    # # with open(file_name) as this_file:
    # #     f.write(this_file)
    # # info("done")
    with client.open("/db100GB"+object_name +'/'+ file_name, 'w', opt) as alluxio_file:
        with open(file_name, 'rb') as local_file:
            alluxio_file.write(local_file)
            alluxio_file.close()
def csv2():
    # csv_file = {"orders.tbl", "supplier.tbl", "region.tbl",
    #                 "partsupp.tbl", "part.tbl", "nation.tbl",
    #                 "customer.tbl", "lineitem.tbl"}
    # csv_file = {"orders.tbl", "part.tbl", "customer.tbl", "lineitem.tbl"}
    # csv_file = {"supplier.tbl", "region.tbl", "partsupp.tbl", "nation.tbl"}

    for n in csv_file:
        print(n)
        data = pd.read_csv("dbgen/" + n, delimiter='|', header=None)
        data = data.iloc[:, :-1]

        dict = []
        for k in range(len(data.axes[1])):
            if k<10:
                dict.append("0" + str(k))
            else:
                dict.append(str(k))
        data.columns = dict


        schema = data.tail(1)
        schema.to_json("test.json", orient="records")  # output file
        single_row_size = math.ceil((data.tail(10).memory_usage(index=False, deep=True).sum())/10)
        print(single_row_size)
        print("Schema---")

        schema = os.popen('java -jar orc-tools-1.7.5-uber.jar json-schema test.json | grep -o struct.[^\>]*\>').read()
        print(schema)
        print("---")


        low = 0  # Initial Lower Limit
        offset = PAGE_SIZE//single_row_size
        high = offset  # Initial Higher Limit
        index = 0
        first = True
        while high < len(data) or first:
            first = False
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
            # upload_file(filenameorc, "/"+n.split(".")[0])
            # upload_file(filenameorc, "/"+n.split(".")[0]+"/"+filenameorc)

def csv():
    csv_file = {"orders.tbl", "supplier.tbl", "region.tbl",
                    "partsupp.tbl", "part.tbl", "nation.tbl",
                    "customer.tbl", "lineitem.tbl"}

    for n in csv_file:
        print(n)
        data = pd.read_csv("dbgen/" + n, delimiter='|', header=None)
        data = data.iloc[:, :-1]

        dict = []
        for k in range(len(data.axes[1])):
            if k<10:
                dict.append("0" + str(k))
            else:
                dict.append(str(k))
        data.columns = dict


        schema = data.tail(1)
        schema.to_json("test.json", orient="records")  # output file
        single_row_size = math.ceil((data.tail(10).memory_usage(index=False, deep=True).sum())/10)
        print(single_row_size)
        print("Schema---")

        schema = os.popen('java -jar orc-tools-1.7.5-uber.jar json-schema test.json | grep -o struct.[^\>]*\>').read()
        print(schema)
        print("---")


        low = 0  # Initial Lower Limit
        offset = PAGE_SIZE//single_row_size
        high = offset  # Initial Higher Limit
        index = 0
        first = True
        while high < len(data) or first:
            first = False
            df_new = data[low:high]  # subsetting DataFrame based on index
            low = high  # changing lower limit
            high = high + offset  # givig uper limit with increment of 1000
            filename = n.split(".")[0] + str(index) + ".csv"
            filenameorc = n.split(".")[0] + str(index) + ".orc"
            df_new.to_csv(filename, header=None, index=None)  # output file
            index = index + 1

            os.system("java -jar orc-tools-1.7.5-uber.jar convert {} -o {} --schema \'{}>\'".format(filename,
                                                                                                   "/root/db/"+n.split(".")[0]+"/"+filenameorc,
                                                                                               schema.split(">")[0]))
            os.system('rm {}'.format(filename))

            # upload_file(filenameorc, "/"+n.split(".")[0]+"/"+filenameorc)


csv()
# upload_file("part0.orc", "/part")
