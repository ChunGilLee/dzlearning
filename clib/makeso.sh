#!/bin/bash

rm -f avx_clib.so
#gcc -fPIC -I. -I/usr/local/java/include -I/usr/local/java/include/linux java_interface.c  test.S -Ofast -mavx -march=native -shared
gcc -fPIC -I. -I/usr/local/java/include -I/usr/local/java/include/linux java_interface.c  vector_dot_vector.S avx.c -Ofast -mavx -march=native -shared -o avx_clib.so -lm

execstack -c avx_clib.so

