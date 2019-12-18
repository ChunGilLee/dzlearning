#/bin/bash

rm -f main_normal
gcc -o main_normal main_normal.c -Ofast -mavx -march=native
./main_normal
