#!/bin/bash

EXPORT_DIR=./output

#build native library
rm -f avx_clib.so
cd clib
./makeso.sh
cp avx_clib.so ../
cd ..

#build java code
find -name "*.java" > sources.txt
javac -d ${EXPORT_DIR} @sources.txt -cp "${PWD}/lib/colt.jar:${PWD}/lib/concurrent.jar:${PWD}/lib/gson-2.8.5.jar"
cp -f avx_clib.so mnist_train.bin ${EXPORT_DIR}
