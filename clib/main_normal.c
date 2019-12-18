/*
 * main.c
 *
 *  Created on: Jun 17, 2018
 *      Author: highvolt
 */

#include <stdio.h>
#include <time.h>
#include <x86intrin.h>

#define TEST_HEIGHT 100
#define TEST_WIDTH 768
#define TEST_OUT_WIDTH 50

void printMatNormal(int height, int width,double m[height][width])
{
	int i=0;
	int j=0;
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			printf("%f ",m[i][j]);
		}
		printf("\n");
	}
}


void fillDataNormal(int height,int width,double m[height][width])
{
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			m[i][j] = (double)i + (double)j;
		}
	}
}
void fillZeroNormal(int height,int width,double m[height][width])
{
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			m[i][j] = 0.0;
		}
	}
}


double aaa[TEST_HEIGHT][TEST_WIDTH];
double bbb[TEST_WIDTH][TEST_OUT_WIDTH];
double ccc[TEST_HEIGHT][TEST_OUT_WIDTH];
void copyit()
{
	for(int i=0;i<TEST_HEIGHT;i++)
	{
		for(int k=0;k<TEST_OUT_WIDTH;k++)
		{
			double sum = 0;
			for(int j=0;j<TEST_WIDTH;j++)
			{
				sum += aaa[i][j] * bbb[j][k];
			}
			ccc[i][k] = sum;
		}
	}
}

double normal_test()
{

	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,aaa);
	fillDataNormal(TEST_WIDTH,TEST_OUT_WIDTH,bbb);
	fillZeroNormal(TEST_HEIGHT,TEST_OUT_WIDTH,ccc);

	clock_t start = clock();
	copyit();
	clock_t end = clock();
	return (end-start)/(double)CLOCKS_PER_SEC;
}
/*
double normal_test0()
{
	double aaa[2][2];
	double bbb[2][2];
	double ccc[2][2];

	aaa[0][0] = 1.0;
	aaa[0][1] = 2.0;
	aaa[1][0] = 3.0;
	aaa[1][1] = 4.0;

	bbb[0][0] = 4.0;
	bbb[0][1] = 3.0;
	bbb[1][0] = 2.0;
	bbb[1][1] = 1.0;

	for(int i=0;i<2;i++)
	{
		for(int k=0;k<2;k++)
		{
			double sum = 0;
			for(int j=0;j<2;j++)
			{
				sum += aaa[i][j] * bbb[j][k];
			}
			ccc[i][k] = sum;
		}
	}

	printMatNormal(2,2,ccc);
}
*/

#define TEST_NUM 100

void simd_test0()
{
	__m256 ymm0, ymm1;			//define the registers used
	float a[8]={1,2,3,4,5,6,7,8};
	float b[8]={2,3,4,5,6,7,8,9};
	float c[8];
	ymm0 = __builtin_ia32_loadups256(a);	//load the 8 floats in a into ymm0
	ymm1 = __builtin_ia32_loadups256(b);	//load the 8 floats in b into ymm1

	ymm0 = __builtin_ia32_mulps256(ymm0, ymm1);
	__builtin_ia32_storeups256(c, ymm0);	//copy the 8 floats in ymm0 to c

	for(int i=0;i<8;i++)
	{
		printf("%f ",c[i]);
	}
	printf("\n");
}


int main()
{

	/*
	int i=0;
	double sum=0.0;

	for(i=0;i<TEST_NUM;i++)
	{
		sum += normal_test();
	}
	printf("normal time duration : %f seconds \n",sum/(double)TEST_NUM);
*/


}
