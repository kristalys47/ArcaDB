import os
import sys
import logging
from botocore.exceptions import ClientError
import pandas as pd
import boto3
import math

def csvShort():
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

        df_new = data[0:100]
        filename = n.split(".")[0] + ".csv"
        df_new.to_csv(filename, index=None)


csvShort()
