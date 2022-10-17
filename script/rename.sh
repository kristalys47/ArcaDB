#!/bin/bash
a=1
for i in *.csv;
do
  new=$(printf "lineitem%03d.csv" "$a") #04 pad to length of 4
  mv -i -- "$i" "$new"
  let a=a+1
done
