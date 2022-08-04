#!/bin/sh

redis-cli rpush "product" "/product/product.orc"
redis-cli rpush "customer" "/customer/customer.orc"

