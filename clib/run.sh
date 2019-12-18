#/bin/bash

GSL_DIR=/home/highvolt/gsl-2.5/bin

rm -f main
gcc -o main -I${GSL_DIR}/include -I. main.c avx.c vector_dot_vector.S ${GSL_DIR}/lib/libgsl.a ${GSL_DIR}/lib/libgslcblas.a -lm -Ofast -mavx -march=native

./main
