import os
import sys
import logging
import pandas as pd
import math
import alluxio
from alluxio import option

PAGE_SIZE = 256*1024*1024


def csv():
    csv_file = []
    for num in range(1, 302):
        csv_file.append("lineitem{:03d}.csv".format(num))

    print('first')
    data = pd.read_csv("splited/lineitem301.csv", delimiter='|', header=None)
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
    index = 0

    for n in csv_file:
        print(n)
        data = pd.read_csv("splited/" + n, delimiter='|', header=None)
        data = data.iloc[:, :-1]

        dict = []
        for k in range(len(data.axes[1])):
            if k<10:
                dict.append("0" + str(k))
            else:
                dict.append(str(k))
        data.columns = dict


        low = 0  # Initial Lower Limit
        offset = PAGE_SIZE//single_row_size
        high = offset  # Initial Higher Limit

        first = True
        while high < len(data) or first:
            first = False
            df_new = data[low:high]  # subsetting DataFrame based on index
            low = high  # changing lower limit
            high = high + offset  # givig uper limit with increment of 1000
            filename = n.split(".")[0] + str(index) + ".csv"
            filenameorc = "lineitem" + str(index) + ".orc"
            df_new.to_csv(filename, header=None, index=None)  # output file
            index = index + 1

            os.system("java -jar orc-tools-1.7.5-uber.jar convert {} -o {} --schema \'{}>\'".format(filename,
                                                                                                   "/root/db/lineitem/"+filenameorc,
                                                                                               schema.split(">")[0]))
            os.system('rm {}'.format(filename))

            # upload_file(filenameorc, "/"+n.split(".")[0]+"/"+filenameorc)


csv()
# upload_file("part0.orc", "/part")
