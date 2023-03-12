#!/usr/bin/env bash

./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/lineitem/ /db100GB/lineitem
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/partsupp/ /db100GB/partsupp
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/nation/ /db100GB/nation
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/part/ /db100GB/part
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/supplier/ /db100GB/supplier
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/orders/ /db100GB/orders
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/region/ /db100GB/region
./alluxio-2.8.1/bin/alluxio fs copyFromLocal database/customer/ /db100GB/customer


./alluxio-2.8.1/bin/alluxio fs /db100GB/lineitem
./alluxio-2.8.1/bin/alluxio fs /db100GB/partsupp
./alluxio-2.8.1/bin/alluxio fs /db100GB/nation
./alluxio-2.8.1/bin/alluxio fs /db100GB/part
./alluxio-2.8.1/bin/alluxio fs /db100GB/supplier
./alluxio-2.8.1/bin/alluxio fs /db100GB/lineitem
./alluxio-2.8.1/bin/alluxio fs /db100GB/lineitem
./alluxio-2.8.1/bin/alluxio fs /db100GB/lineitem
./alluxio-2.8.1/bin/alluxio fs /db100GB/lineitem


./alluxio-2.8.1/bin/alluxio fs copyFromLocal tables.json  /
