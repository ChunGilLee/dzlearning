/*
 * main.c
 *
 *  Created on: Jun 17, 2018
 *      Author: highvolt
 */

#include <stdio.h>
#include <time.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_blas.h>
#include <gsl/gsl_cblas.h>

#include "avx.h"
#include <x86intrin.h>

#include <math.h>
#include <float.h>

void printMat(gsl_matrix *m)
{
	int i=0;
	int j=0;
	for(int i=0;i<m->size1;i++)
	{
		for(int j=0;j<m->size2;j++)
		{
			printf("%f ",gsl_matrix_get(m,i,j));
		}
		printf("\n");
	}
}

void fillData(gsl_matrix *m)
{
	int i=0;
	int j=0;
	for(int i=0;i<m->size1;i++)
	{
		for(int j=0;j<m->size2;j++)
		{
			gsl_matrix_set(m,i,j,(double)i + (double)j);
		}
	}
}
void fillZero(gsl_matrix *m)
{
	int i=0;
	int j=0;
	for(int i=0;i<m->size1;i++)
	{
		for(int j=0;j<m->size2;j++)
		{
			gsl_matrix_set(m,i,j,0.0);
		}
	}
}

#define TEST_HEIGHT 100
#define TEST_WIDTH 768
#define TEST_OUT_WIDTH 50

double blas_test()
{
	gsl_matrix * aaa;
	gsl_matrix * bbb;
	gsl_matrix * ccc;
	aaa = gsl_matrix_alloc(TEST_HEIGHT,TEST_WIDTH);
	bbb = gsl_matrix_alloc(TEST_WIDTH,TEST_OUT_WIDTH);
	ccc = gsl_matrix_alloc(TEST_HEIGHT,TEST_OUT_WIDTH);

	fillData(aaa);
	fillData(bbb);
	fillZero(ccc);

	//printMat(aaa);
	//printMat(bbb);

	clock_t start = clock();
	gsl_blas_dgemm(CblasNoTrans,CblasNoTrans,1.0,aaa,bbb,1.0,ccc);
	clock_t end = clock();

	//printf("blass time duration : %f seconds \n",(end-start)/(double)CLOCKS_PER_SEC);

	//printMat(ccc);


	gsl_matrix_free(aaa);
	gsl_matrix_free(bbb);
	gsl_matrix_free(ccc);

	return (end-start)/(double)CLOCKS_PER_SEC;
}

double blas_test_add()
{
	gsl_matrix * aaa;
	gsl_matrix * bbb;
	gsl_matrix * ccc;
	aaa = gsl_matrix_alloc(TEST_HEIGHT,TEST_WIDTH);
	bbb = gsl_matrix_alloc(TEST_HEIGHT,TEST_WIDTH);
	ccc = gsl_matrix_alloc(TEST_HEIGHT,TEST_WIDTH);

	fillData(aaa);
	fillData(bbb);
	fillZero(ccc);

	//printMat(aaa);
	//printMat(bbb);

	clock_t start = clock();
	gsl_matrix_add(aaa,bbb);
	clock_t end = clock();

	//printf("blass time duration : %f seconds \n",(end-start)/(double)CLOCKS_PER_SEC);

	//printMat(ccc);


	gsl_matrix_free(aaa);
	gsl_matrix_free(bbb);
	gsl_matrix_free(ccc);

	return (end-start)/(double)CLOCKS_PER_SEC;
}

double blas_test_scale()
{
	gsl_matrix * aaa;
	aaa = gsl_matrix_alloc(TEST_HEIGHT,TEST_WIDTH);
	double bbb = 2.0;

	fillData(aaa);

	//printMat(aaa);
	//printMat(bbb);

	clock_t start = clock();
	gsl_matrix_scale(aaa,bbb);
	clock_t end = clock();

	//printf("blass time duration : %f seconds \n",(end-start)/(double)CLOCKS_PER_SEC);

	//printMat(ccc);

	gsl_matrix_free(aaa);

	return (end-start)/(double)CLOCKS_PER_SEC;
}

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

double normal_test()
{
	double aaa[TEST_HEIGHT][TEST_WIDTH];
	double bbb[TEST_WIDTH][TEST_OUT_WIDTH];
	double ccc[TEST_HEIGHT][TEST_OUT_WIDTH];

	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,aaa);
	fillDataNormal(TEST_WIDTH,TEST_OUT_WIDTH,bbb);
	fillZeroNormal(TEST_HEIGHT,TEST_OUT_WIDTH,ccc);

	clock_t start = clock();
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
	clock_t end = clock();
	return (end-start)/(double)CLOCKS_PER_SEC;
}

double normal_test_add()
{
	double aaa[TEST_HEIGHT][TEST_WIDTH];
	double bbb[TEST_HEIGHT][TEST_WIDTH];
	double ccc[TEST_HEIGHT][TEST_WIDTH];

	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,aaa);
	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,bbb);
	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,ccc);

	clock_t start = clock();
	for(int i=0;i<TEST_HEIGHT;i++)
	{
		for(int j=0;j<TEST_WIDTH;j++)
		{
			ccc[i][j] = aaa[i][j] + bbb[i][j];
		}
	}
	clock_t end = clock();
	return (end-start)/(double)CLOCKS_PER_SEC;
}
double normal_test_scale()
{
	double aaa[TEST_HEIGHT][TEST_WIDTH];
	double ccc[TEST_HEIGHT][TEST_WIDTH];
	double bbb = 2.0;

	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,aaa);
	fillDataNormal(TEST_HEIGHT,TEST_WIDTH,ccc);

	clock_t start = clock();
	for(int i=0;i<TEST_HEIGHT;i++)
	{
		for(int j=0;j<TEST_WIDTH;j++)
		{
			ccc[i][j] = aaa[i][j] * bbb;
		}
	}
	clock_t end = clock();
	return (end-start)/(double)CLOCKS_PER_SEC;
}



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

void fillDataNormalf(int height,int width,float m[height][width])
{
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			m[i][j] = (float)i + (float)j;
		}
	}
}
void fillZeroNormalf(int height,int width,float m[height][width])
{
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			m[i][j] = 0.0;
		}
	}
}

void fillData_avx(AVX_MATRIX_F *m)
{
	int i=0;
	int j=0;
	for(int i=0;i<m->height;i++)
	{
		for(int j=0;j<m->width;j++)
		{
			set_data_f(m, i, j, (float)i+(float)j);
		}
	}
}

double avx()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	AVX_MATRIX_F *b = create_avx_matrix_f(TEST_WIDTH, TEST_OUT_WIDTH);
	AVX_MATRIX_F *c = create_avx_matrix_f(TEST_HEIGHT, TEST_OUT_WIDTH);
	fillData_avx(a);
	fillData_avx(b);
	AVX_MATRIX_F *b_flip = flip_f(b);
	clock_t start = clock();
	int ret = matrix_dot_matrix_f(a, b_flip, c);
	clock_t end = clock();
	if(ret!=0)printf("ret : %d\n",ret);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(b_flip);
	release_avx_matrix_f(c);

	return (end-start)/(double)CLOCKS_PER_SEC;
}

double avx_add()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	AVX_MATRIX_F *b = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	AVX_MATRIX_F *c = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	fillData_avx(a);
	fillData_avx(b);
	clock_t start = clock();
	int ret = matrix_add_matrix_f(a, b, c);
	clock_t end = clock();
	if(ret!=0)printf("ret : %d\n",ret);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);

	return (end-start)/(double)CLOCKS_PER_SEC;
}
double avx_scale()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	AVX_MATRIX_F *c = create_avx_matrix_f(TEST_HEIGHT, TEST_WIDTH);
	fillData_avx(a);
	clock_t start = clock();
	int ret = matrix_scale_f(a, c, 2.0);
	clock_t end = clock();
	if(ret!=0)printf("ret : %d\n",ret);

	release_avx_matrix_f(a);
	release_avx_matrix_f(c);

	return (end-start)/(double)CLOCKS_PER_SEC;
}

void avx_test0()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(2, 2);
	set_data_f(a, 0, 0, 1);
	set_data_f(a, 0, 1, 2);
	set_data_f(a, 1, 0, 3);
	set_data_f(a, 1, 1, 4);

	print_matrix_f(a);

	AVX_MATRIX_F *b = create_avx_matrix_f(2, 2);
	set_data_f(b, 0, 0, 4);
	set_data_f(b, 0, 1, 3);
	set_data_f(b, 1, 0, 2);
	set_data_f(b, 1, 1, 1);

	print_matrix_f(b);

	AVX_MATRIX_F *b_flipped = flip_f(b);
	print_matrix_f(b_flipped);


	AVX_MATRIX_F *c = create_avx_matrix_f(2, 2);

	int ret = matrix_dot_matrix_f(a, b_flipped, c);
	printf("ret : %d\n",ret);

	print_matrix_f(c);

	/*
	printf("wi:%d h:%d\n",a->width_internal,a->height);
	for(int i=0;i<a->width_internal*a->height;i++)
	{
		printf("0x%x %d %f\n",&(a->data[i]),i,a->data[i]);
	}
	*/

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);

}

void avx_test1()
{
	int t_h = TEST_HEIGHT;
	int t_w = TEST_WIDTH;
	int t_o_w = TEST_OUT_WIDTH;
	//int t_h = 1;
	//int t_w = 57;
	//int t_o_w = 1;

	gsl_matrix * aaa;
	gsl_matrix * bbb;
	gsl_matrix * ccc;
	aaa = gsl_matrix_alloc(t_h,t_w);
	bbb = gsl_matrix_alloc(t_w,t_o_w);
	ccc = gsl_matrix_alloc(t_h,t_o_w);

	fillData(aaa);
	fillData(bbb);
	fillZero(ccc);

	gsl_blas_dgemm(CblasNoTrans,CblasNoTrans,1.0,aaa,bbb,1.0,ccc);

	AVX_MATRIX_F *a = create_avx_matrix_f(t_h, t_w);
	AVX_MATRIX_F *b = create_avx_matrix_f(t_w, t_o_w);
	AVX_MATRIX_F *c = create_avx_matrix_f(t_h, t_o_w);
	fillData_avx(a);
	fillData_avx(b);
	AVX_MATRIX_F *b_flip = flip_f(b);
	int ret = matrix_dot_matrix_f(a, b_flip, c);
	printf("ret : %d\n",ret);

	int breakit=0;
	for(int i=0;i<ccc->size1;i++)
	{
		for(int j=0;j<ccc->size2;j++)
		{
			float d0 = (float)gsl_matrix_get(ccc,i,j);
			float d1 = get_data_f(c, i, j);
			float delta = d0-d1;
			if(delta<0)delta = (-1)*delta;
			if(delta> d0*0.01)
			{
				printf("[%d,%d] %f != %f, delta=%f\n",i,j,d0,d1,delta/d0);
				breakit=1;
				break;
			}
		}
		if(breakit==1)break;
	}

	//print_matrix_f(a);
	//print_matrix_f(b);
	//print_matrix_f(c);

	gsl_matrix_free(aaa);
	gsl_matrix_free(bbb);
	gsl_matrix_free(ccc);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(b_flip);
	release_avx_matrix_f(c);

}

void avx_test1_add()
{
	int t_h = TEST_HEIGHT;
	int t_w = TEST_WIDTH;
	//int t_h = 1;
	//int t_w = 57;
	//int t_o_w = 1;

	gsl_matrix * aaa;
	gsl_matrix * bbb;
	gsl_matrix * ccc;
	aaa = gsl_matrix_alloc(t_h,t_w);
	bbb = gsl_matrix_alloc(t_h,t_w);
	ccc = aaa;

	fillData(aaa);
	fillData(bbb);

	gsl_matrix_add(aaa,bbb);

	AVX_MATRIX_F *a = create_avx_matrix_f(t_h, t_w);
	AVX_MATRIX_F *b = create_avx_matrix_f(t_h, t_w);
	AVX_MATRIX_F *c = create_avx_matrix_f(t_h, t_w);
	fillData_avx(a);
	fillData_avx(b);
	int ret = matrix_add_matrix_f(a, b, c);
	printf("ret : %d\n",ret);

	int breakit=0;
	for(int i=0;i<ccc->size1;i++)
	{
		for(int j=0;j<ccc->size2;j++)
		{
			float d0 = (float)gsl_matrix_get(ccc,i,j);
			float d1 = get_data_f(c, i, j);
			float delta = d0-d1;
			if(delta<0)delta = (-1)*delta;
			if(delta> d0*0.01)
			{
				printf("[%d,%d] %f != %f, delta=%f\n",i,j,d0,d1,delta/d0);
				breakit=1;
				break;
			}
		}
		if(breakit==1)break;
	}

	//print_matrix_f(a);
	//print_matrix_f(b);
	//print_matrix_f(c);

	gsl_matrix_free(aaa);
	gsl_matrix_free(bbb);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);

}

void avx_test1_scale()
{
	int t_h = TEST_HEIGHT;
	int t_w = TEST_WIDTH;
	//int t_h = 1;
	//int t_w = 57;
	//int t_o_w = 1;

	gsl_matrix * aaa;
	gsl_matrix * bbb;
	gsl_matrix * ccc;
	aaa = gsl_matrix_alloc(t_h,t_w);
	bbb = gsl_matrix_alloc(t_h,t_w);
	ccc = aaa;

	fillData(aaa);
	fillData(bbb);

	gsl_matrix_scale(aaa,2.0);

	AVX_MATRIX_F *a = create_avx_matrix_f(t_h, t_w);
	AVX_MATRIX_F *b = create_avx_matrix_f(t_h, t_w);
	AVX_MATRIX_F *c = create_avx_matrix_f(t_h, t_w);
	fillData_avx(a);
	fillData_avx(b);
	int ret = matrix_scale_f(a, c, 2.0);
	printf("ret : %d\n",ret);

	int breakit=0;
	for(int i=0;i<ccc->size1;i++)
	{
		for(int j=0;j<ccc->size2;j++)
		{
			float d0 = (float)gsl_matrix_get(ccc,i,j);
			float d1 = get_data_f(c, i, j);
			float delta = d0-d1;
			if(delta<0)delta = (-1)*delta;
			if(delta> d0*0.01)
			{
				printf("[%d,%d] %f != %f, delta=%f\n",i,j,d0,d1,delta/d0);
				breakit=1;
				break;
			}
		}
		if(breakit==1)break;
	}

	//print_matrix_f(a);
	//print_matrix_f(b);
	//print_matrix_f(c);

	gsl_matrix_free(aaa);
	gsl_matrix_free(bbb);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);

}

void avx_test2()
{
	float a[8] = {1,1,1,1,2,2,2,2};
	float b[8] = {1,1,1,1,2,2,2,2};
	float c[8] = {0,0,0,0,0,0,0,0};

	__m256 ymm[3];
	ymm[0] = __builtin_ia32_loadups256(a);
	ymm[1] = __builtin_ia32_loadups256(b);

	ymm[2] = __builtin_ia32_dpps256(ymm[0],ymm[1],0xf1);

	__builtin_ia32_storeups256(c, ymm[2]);
	for(int i=0;i<8;i++)
	{
		printf("%f ",c[i]);
	}
	printf("\n");

}
void avx_add_test()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(2, 2);
	AVX_MATRIX_F *b = create_avx_matrix_f(2, 2);
	AVX_MATRIX_F *c = create_avx_matrix_f(2, 2);

	set_data_f(a, 0, 0, 1);
	set_data_f(a, 0, 1, 2);
	set_data_f(a, 1, 0, 3);
	set_data_f(a, 1, 1, 4);

	set_data_f(b, 0, 0, 1);
	set_data_f(b, 0, 1, 2);
	set_data_f(b, 1, 0, 3);
	set_data_f(b, 1, 1, 4);

	AVX_MATRIX_F *d = flip_f(b);
	AVX_MATRIX_F *e = unflip_f(d);

	int ret = matrix_add_matrix_f(a, e, c);
	printf("ret:%d\n",ret);

	print_matrix_f(a);
	print_matrix_f(b);
	print_matrix_f(c);


	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);
	release_avx_matrix_f(d);
	release_avx_matrix_f(e);


}

void avx_sigmoid_test()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(100, 200);
	AVX_MATRIX_F *b = create_avx_matrix_f(100, 200);

	for(int i=0;i<100;i++)
	{
		for(int j=0;j<200;j++)
		{
			set_data_f(a, i, j, 1);
		}
	}

	int ret = matrix_sigmoid_f(a, b);
	printf("ret:%d\n",ret);

	int breakit=0;
	for(int i=0;i<a->height;i++)
	{
		for(int j=0;j<a->width;j++)
		{
			float d0 = 1.0/(1.0+exp(-get_data_f(a, i, j)));
			float d1 = get_data_f(b, i, j);
			float delta = d0-d1;
			if(delta<0)delta = (-1)*delta;
			if(delta> d0*0.01)
			{
				printf("[%d,%d] %f(right) != %f(sample), delta=%f\n",i,j,d0,d1,delta/d0);
				breakit=1;
				break;
			}
		}
		if(breakit==1)break;
	}

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
}

void avx_exp_test()
{
	AVX_MATRIX_F *a = create_avx_matrix_f(100, 200);
	AVX_MATRIX_F *b = create_avx_matrix_f(100, 200);

	for(int i=0;i<100;i++)
	{
		for(int j=0;j<200;j++)
		{
			//set_data_f(a, i, j, i*j/10000);
			set_data_f(a, i, j, 1);
		}
	}

	int ret = matrix_exp_f(a, b);
	printf("ret:%d\n",ret);

	int breakit=0;
	for(int i=0;i<a->height;i++)
	{
		for(int j=0;j<a->width;j++)
		{
			float d0 = exp(get_data_f(a, i, j));
			float d1 = get_data_f(b, i, j);
			float delta = d0-d1;
			if(delta<0)delta = (-1)*delta;
			if(delta> d0*0.01)
			{
				printf("[%d,%d] %f(right) != %f(sample), delta=%f\n",i,j,d0,d1,delta/d0);
				breakit=1;
				break;
			}
		}
		if(breakit==1)break;
	}

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
}

#define TEST_NUM 100
void performance()
{
	int i=0;
	double sum=0.0;

	printf("matrix dot matrix performance test");

	for(i=0;i<TEST_NUM;i++)
	{
		sum += normal_test();
	}
	printf("normal time duration : %f seconds \n",sum/(double)TEST_NUM);

	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += blas_test();
	}
	printf("blas time duration : %f seconds \n",sum/(double)TEST_NUM);

	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += avx();
	}
	printf("avx time duration : %f seconds \n",sum/(double)TEST_NUM);
}

void performance_add()
{
	int i=0;
	double sum=0.0;

	printf("matrix add matrix performance test");

	for(i=0;i<TEST_NUM;i++)
	{
		sum += normal_test_add();
	}
	printf("normal time duration : %f seconds \n",sum/(double)TEST_NUM);

	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += blas_test_add();
	}
	printf("blas time duration : %f seconds \n",sum/(double)TEST_NUM);


	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += avx_add();
	}
	printf("avx time duration : %f seconds \n",sum/(double)TEST_NUM);

}

void performance_scale()
{
	int i=0;
	double sum=0.0;

	printf("matrix scale performance test");

	for(i=0;i<TEST_NUM;i++)
	{
		sum += normal_test_scale();
	}
	printf("normal time duration : %f seconds \n",sum/(double)TEST_NUM);

	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += blas_test_scale();
	}
	printf("blas time duration : %f seconds \n",sum/(double)TEST_NUM);

	sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += avx_scale();
	}
	printf("avx time duration : %f seconds \n",sum/(double)TEST_NUM);
}

struct abc {
	union
	{
		float d[100*200];
		int32_t i[100*200];
	};
};

double avx_perf0()
{

	//float data0[768] __attribute__ ((aligned (32)));
	//float data1[768] __attribute__ ((aligned (32)));

	static struct abc data0 __attribute__ ((aligned (32)));;
	static struct abc data1 __attribute__ ((aligned (32)));;

	long addr0 = (long)(&(data0.d[0]));
	long addr1 = (long)(&(data1.d[0]));

	if(addr0%32 !=0 )printf("addr0 is not aligned\n");
	if(addr1%32 !=0 )printf("addr1 is not aligned\n");


	clock_t start = clock();
	for(int i=0;i<5000;i++)
	{
		vector_dot_vector(data0.d, data1.d, 768);
	}
	clock_t end = clock();
	//printf("duration:%f seconds\n",((end-start)/(double)CLOCKS_PER_SEC));;
	return (end-start)/(double)CLOCKS_PER_SEC;
}

void avx_perf1()
{
	int i;
	double sum = 0.0;
	for(i=0;i<TEST_NUM;i++)
	{
		sum += avx_perf0();
	}
	printf("avx time duration : %f seconds \n",sum/(double)TEST_NUM);
}


void avx_test3()
{
	static struct abc data0 __attribute__ ((aligned (32)));;
	static struct abc data1 __attribute__ ((aligned (32)));;
	int width=8;
	int count=2;

	data0.d[0] = 0;
	data0.d[1] = 1;
	data0.d[2] = 2;
	data0.d[3] = 3;
	data0.d[4] = 4;
	data0.d[5] = 5;
	data0.d[6] = 6;
	data0.d[7] = 7;

	data1.d[0] = 0;
	data1.d[1] = 1;
	data1.d[2] = 2;
	data1.d[3] = 3;
	data1.d[4] = 4;
	data1.d[5] = 5;
	data1.d[6] = 6;
	data1.d[7] = 7;

	data0.d[8] = 8;
	data0.d[9] = 9;
	data0.d[10] = 10;
	data0.d[11] = 11;
	data0.d[12] = 12;
	data0.d[13] = 13;
	data0.d[14] = 14;
	data0.d[15] = 15;

	data1.d[8] = 8;
	data1.d[9] = 9;
	data1.d[10] = 10;
	data1.d[11] = 11;
	data1.d[12] = 12;
	data1.d[13] = 13;
	data1.d[14] = 14;
	data1.d[15] = 15;

	//printf("v0:0x%lx\n",v0);
	//printf("v1:0x%lx\n",v1);

	printf("ret:%f\n",v_dot_v_avx(data0.d,data1.d,width,count));
}
void avx_test4()
{
	static struct abc data0 __attribute__ ((aligned (32)));;
	static struct abc data1 __attribute__ ((aligned (32)));;

	for(int i=0;i<16;i++)
	{
		data0.d[i] = i;
	}
	float scale = 2.0;
	v_mul_s_avx(data0.d,data1.d,2,&scale);
	//v_mul_s_avx(2.0);
	for(int i=0;i<16;i++)
	{
		printf("%f\n",data1.d[i]);
	}
}


void avx_test5()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc output __attribute__ ((aligned (32)));
	static struct abc output3 __attribute__ ((aligned (32)));

	for(int i=0;i<(100*200);i++)input.d[i] = 1.0;

	input.d[0] = 88.72283;
	input.d[1] = -87.33654;
	input.d[2] = -88.72284;
	input.d[3] = 100;

	exp_avx(input.d,output3.d,25);

	//__m128 input00 = _mm_load_ps(input.d);
	//__m128 output00 = fast_exp_sse(input00);

	float max = exp(-90);
	printf("max :%f 0x%x\n",max,*((unsigned int *)(&max)));


	for(int i=0;i<8;i++)
	{
		float f = exp(input.d[i]);
		printf("%f %f %f %f | 0x%x, %u\n",f,fastExp4(input.d[i]),
				output.d[i],output3.d[i],output3.i[i],output3.i[i]);
	}
}
void avx_test6()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc output __attribute__ ((aligned (32)));

	for(int i=0;i<8*32;i++)
	{
		input.d[i] = i/60.0;
		output.d[i] = 0;
	}

	exp_avx(input.d,output.d,32);

	for(int i=0;i<8*32;i++)
	{
		float delta = output.d[i] - exp(input.d[i]);
		if(delta<0)delta=delta*(-1.0);
		if((delta/exp(input.d[i]))>0.1)
		{
			printf("[%d] %f<==>%f(%f), 0x%x, %u, %f\n",i,output.d[i],exp(input.d[i]),input.d[i],output.i[i],output.i[i],delta);
			break;
		}
	}
}

void avx_test7()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc output __attribute__ ((aligned (32)));
	static struct abc output3 __attribute__ ((aligned (32)));

	input.d[0] = 6.76485157f;
	input.d[1] = 100.0;
	input.d[2] = 87;
	input.d[3] = 80;

	input.d[4] = -100.0;
	input.d[5] = -87;
	input.d[6] = -80;
	input.d[7] = 1;

	sigmoid_avx(input.d,output3.d,1);

	for(int i=0;i<8;i++)
	{
		printf("%f %f %f %f | 0x%x, %u\n",1.0/(1.0+exp(-input.d[i])),1.0/(1.0+fastExp4(input.d[i])),
				output.d[i],output3.d[i],output3.i[i],output3.i[i]);
	}
}

void avx_test8()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc output __attribute__ ((aligned (32)));

	for(int i=0;i<8*32;i++)
	{
		input.d[i] = i/60.0;
		output.d[i] = 0;
	}

	sigmoid_avx(input.d,output.d,32);

	for(int i=0;i<8*32;i++)
	{
		float rightV = 1.0/(1.0+exp(-input.d[i]));
		float delta = output.d[i] - rightV;
		if(delta/rightV<0)delta=delta*(-1.0);
		if((delta/rightV)>0.1)
		{
			printf("[%d] %f<==>%f(%f), 0x%x, %u, %f\n",i,output.d[i],rightV,input.d[i],output.i[i],output.i[i],delta);
			break;
		}
	}
}

int __float_as_int(float a)
{
	int *ret = (int *)(&a);
	return (*ret);
}
float __int_as_float(int a)
{
	float *ret = (float *)(&a);
	return (*ret);
}

/* natural log on [0x1.f7a5ecp-127, 0x1.fffffep127]. Maximum relative error 9.4529e-5 */
float my_faster_logf (float a)
{
    float m, r, s, t, i, f;
    int32_t e;

    e = (__float_as_int (a) - 0x3f2aaaab) & 0xff800000;
    m = __int_as_float (__float_as_int (a) - e);
    i = (float)e * 1.19209290e-7f; // 0x1.0p-23
    /* m in [2/3, 4/3] */
    f = m - 1.0f;
    s = f * f;
    /* Compute log1p(f) for f in [-1/3, 1/3] */
    r = fmaf (0.230836749f, f, -0.279208571f); // 0x1.d8c0f0p-3, -0x1.1de8dap-2
    t = fmaf (0.331826031f, f, -0.498910338f); // 0x1.53ca34p-2, -0x1.fee25ap-2
    r = fmaf (r, s, t);
    r = fmaf (r, s, f);
    r = fmaf (i, 0.693147182f, r); // 0x1.62e430p-1 // log(2)
    return r;
}

void avx_test9()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc output1 __attribute__ ((aligned (32)));
	static struct abc output2 __attribute__ ((aligned (32)));
	static struct abc output3 __attribute__ ((aligned (32)));

	int test_len=1000; //1 -> 8 floats

	for(int i=0;i<8*test_len;i++)
	{
		input.d[i] = i/60.0+0.000001;
	}

	clock_t start;
	clock_t end;

	start = clock();
	log_avx(input.d,output1.d,test_len);
	end = clock();
	double log_avx_time = (end-start)/(double)CLOCKS_PER_SEC;


	start = clock();
	for(int i=0;i<8*test_len;i++)
	{
		output3.d[i] = log(input.d[i]);
	}
	end = clock();
	double clib_time = (end-start)/(double)CLOCKS_PER_SEC;

	start = clock();
	for(int i=0;i<8*test_len;i++)
	{
		output2.d[i] = my_faster_logf(input.d[i]);
	}
	end = clock();
	double faster_log_f_time = (end-start)/(double)CLOCKS_PER_SEC;

	for(int i=0;i<8*test_len;i++)
	{
		float delta = output1.d[i] - output3.d[i];
		float per = delta/output3.d[i];
		if(per<0)per=per*(-1.0);
		if(per>0.01)
		{
			printf("[%d] i:%f o1:%f o2:%f o3:%f || 0x%x, %u, %f\n",
					i,input.d[i],
					output1.d[i],output2.d[i],output3.d[i]
					,output1.i[i],output1.i[i],delta);
			break;
		}
	}

	int i=143;
	printf("[%d] i:%f o1:%f o2:%f o3:%f || 0x%x, %u, %f\n",
			i,input.d[i],
			output1.d[i],output2.d[i],output3.d[i]
			,output1.i[i],output1.i[i],0.0);


	printf("avx time:%f, faster log time:%f clib time:%f\n",log_avx_time,faster_log_f_time,clib_time);
}

void avx_test10()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc rightInput __attribute__ ((aligned (32)));
	//static struct abc buf __attribute__ ((aligned (32)));

	int test_len=1; //1 -> 8 floats

	//double answer[] =      {0.1, 0.2, 0.1, 0.3 ,0.1, 0.2, 0.1, 0.2};
	//double answerRight[] = {0.0, 0.0, 0.0, 0.1 ,0.0, 0.0, 0.0, 0.0};
	/*
	for(int i=0;i<8*test_len;i++)
	{
		input.d[i] = i/60.0+0.000001;
		rightInput.d[i] = i/25.0;
	}
	*/
	input.d[0] = 0.1; rightInput.d[0] = 0.0;
	input.d[1] = 0.2; rightInput.d[1] = 0.0;
	input.d[2] = 0.1; rightInput.d[2] = 0.0;
	input.d[3] = 0.3; rightInput.d[3] = 0.1;
	input.d[4] = 0.1; rightInput.d[4] = 0.0;
	input.d[5] = 0.2; rightInput.d[5] = 0.0;
	input.d[6] = 0.1; rightInput.d[6] = 0.0;
	input.d[7] = 0.2; rightInput.d[7] = 0.0;


	clock_t start;
	clock_t end;

	start = clock();
	float output0 = cross_entropy_error(input.d,rightInput.d,test_len);
	end = clock();
	double log_avx_time = (end-start)/(double)CLOCKS_PER_SEC;


	start = clock();
	float output1=0.0;
	for(int i=0;i<8*test_len;i++)
	{
		float ff = rightInput.d[i] * log(input.d[i] + 0.00000001);
		output1 += ff;
	}
	output1 *= -1.0;
	end = clock();
	double clib_time = (end-start)/(double)CLOCKS_PER_SEC;

	start = clock();
	float output2=0.0;
	for(int i=0;i<8*test_len;i++)
	{
		float ff = rightInput.d[i] * my_faster_logf(input.d[i] + 0.00000001);
		output2 += ff;
	}
	output2 *= -1.0;
	end = clock();
	double faster_log_f_time = (end-start)/(double)CLOCKS_PER_SEC;

	printf(" result avx:%f Clib:%f fasterlog:%f\n",output0,output1,output2);
	printf("avx time:%f, faster log time:%f clib time:%f\n",log_avx_time,faster_log_f_time,clib_time);

	/*
	for(int i=0;i<8;i++)
	{
	printf("[%d] i:%f o1:%f o2:%f || 0x%x, %u, %f\n",
			i,input.d[i],
			buf.d[i],log(input.d[i] + 0.00000001)
			,buf.i[i],buf.i[i],0.0);
	}
	*/
}

void avx_test11()
{
	static struct abc input __attribute__ ((aligned (32)));
	static struct abc buf __attribute__ ((aligned (32)));

	int test_len=1000; //1 -> 8 floats

	for(int i=0;i<8*test_len;i++)
	{
		input.d[i] = i/60.0;
	}

	/*
	input.d[0] = 0.1;
	input.d[1] = 0.2;
	input.d[2] = 0.3;
	input.d[3] = 0.4;
	input.d[4] = 1.5;
	input.d[5] = 0.6;
	input.d[6] = 0.7;
	input.d[7] = 0.8;
	*/

	clock_t start;
	clock_t end;

	start = clock();
	float output0 = get_max_avx(input.d,test_len,buf.d);
	end = clock();
	double log_avx_time = (end-start)/(double)CLOCKS_PER_SEC;


	start = clock();
	float output1=0.0;
	for(int i=0;i<8*test_len;i++)
	{
		if(i==0)output1 = input.d[i];
		else
		{
			if(output1<input.d[i])output1 = input.d[i];
		}
	}
	end = clock();
	double clib_time = (end-start)/(double)CLOCKS_PER_SEC;


	printf(" result avx:%f Clib:%f\n",output0,output1);
	printf("avx time:%f, clib time:%f\n",log_avx_time,clib_time);

	/*
	for(int i=0;i<8;i++)
	{
	printf("[%d] i:%f o1:%f o2:%f || 0x%x, %u, %f\n",
			i,input.d[i],
			buf.d[i],log(input.d[i] + 0.00000001)
			,buf.i[i],buf.i[i],0.0);
	}
	*/
}

void avx_test12()
{

	AVX_MATRIX_F * a = create_avx_matrix_f(1000, 1000);
	AVX_MATRIX_F * b1 = create_avx_matrix_f(1000, 1000);
	AVX_MATRIX_F * c = create_avx_matrix_f(1000, 1000);
	AVX_MATRIX_F * b = flip_f(b1);
	clock_t start = clock();

	avx_test(a->data,b->data,0,(a->width_internal/32)*1000*1000);

	/*
	float sum = 0.0;
	int a_index = 0;
	int count = a->width_internal/32;
	int b_index = 0;
	for(int i=0;i<a->height;i++)
	{
		a_index += a->width_internal;
		float * a_point = &(a->data[a_index]);
		b_index = 0;
		for(int j=0;j<b->width;j++)
		{
			b_index += b->width_internal;
			sum = avx_test(a_point,&(b->data[b_index]),0,count);
			//set_data_f(c, i, j, sum);
			//counter++;

			//sum = vector_dot_vector(&(a->data[a->width_internal*i]),&(b->data[b->width_internal*j]),a->width_internal);
			//set_data_f(c, i, j, sum);
		}
	}
	*/

	clock_t end = clock();
	double log_avx_time = (end-start)/(double)CLOCKS_PER_SEC;
	printf("time : %f s\n",log_avx_time);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(b1);
	release_avx_matrix_f(c);

}

void avx_test13()
{

	AVX_MATRIX_F * a = create_avx_matrix_f(1000, 1000);
	AVX_MATRIX_F * b = create_avx_matrix_f(1000, 1000);
	AVX_MATRIX_F * c = create_avx_matrix_f(1000, 1000);

	a->data[0] = 2;
	a->data[1] = -2;

	b->data[0] = 2;
	b->data[1] = 0.7;

	clock_t start = clock();
	log_avx(a->data,c->data, 1);
	v_mmult_v_avx(c->data, b->data, c->data, 1);
	exp_avx(c->data,c->data,1);

	clock_t end = clock();
	double log_avx_time = (end-start)/(double)CLOCKS_PER_SEC;
	printf("time : %f s\n",log_avx_time);

	printf("%f %f\n",c->data[0],c->data[1]);

	release_avx_matrix_f(a);
	release_avx_matrix_f(b);
	release_avx_matrix_f(c);

}


int main()
{
	printf("AVX2 support : %d\n",is_avx2_available());
	printf("AVX support : %d\n",is_avx_available());

	//normal_test0();
	//avx_test0();
	//avx_test1();
	//avx_test2();
	//performance();
	//avx_perf1();

	//avx_test3();

	//performance_add();
	//avx_test1_add();
	//avx_test1_scale();
	//performance_scale();



	//avx_test5();
	//avx_test8();
	//avx_test7();
	//avx_add_test();
	//avx_sigmoid_test();
	//avx_exp_test();

	//avx_test10();
	//avx_test11();
	avx_test13();
}
