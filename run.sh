#!/bin/bash

test=$1

for N in 1 10 100 1000 10000 100000 1000000 10000000
do
    java -XX:-TieredCompilation -XX:MaxInlineLevel=11 -jar -Dbenchmark.n=$N target/microbenchmarks.jar -f 2 -rf csv -rff $test.$N.txt ".*$test.*"
done