#!/bin/sh

redis-cli -p 6380 flushall
redis-cli flushall
redis-cli rpush "product" "/product/product.orc"
redis-cli rpush "product" "/product/product.orc"
redis-cli rpush "product" "/product/product.orc"
#redis-cli rpush "customer" "/customer/customer.orc"
#redis-cli rpush "customer" "/customer/customer.orc"
redis-cli rpush "customer" "/customer/customer.orc"


#redis-cli sadd node 172.20.59.90
#redis-cli set 172.20.59.90 available