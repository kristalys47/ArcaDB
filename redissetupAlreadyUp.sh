#!/bin/sh

redis-cli -h 136.145.77.83 -p 6380 flushall
redis-cli -h 136.145.77.83 -p 6379 flushall
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product0.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product2.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product1.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product3.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product4.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product5.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "product" "/product/product6.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer0.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer1.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer2.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer3.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer4.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer6.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer5.orc"
redis-cli -h 136.145.77.83 -p 6379 rpush "customer" "/customer/customer7.orc"


#redis-cli sadd node 172.20.59.90
#redis-cli set 172.20.59.90 available
