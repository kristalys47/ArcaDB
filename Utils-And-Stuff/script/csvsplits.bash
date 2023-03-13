#!/bin/bash

rm x*
filename="${1:-PagSeguro.csv}"
lines=${2:-1000}

split -l $lines $filename
[ ! -f "$filename" ] && { echo "$0 - Arquivo $filename Não encontrado"; exit 1; }
for i in x*;do
    mv "$i" "$i.csv";
done
