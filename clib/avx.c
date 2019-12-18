/*
 * avx.c
 *
 *  Created on: Jun 17, 2018
 *      Author: highvolt
 */

#include <stdio.h>
#include <stdlib.h>
#include <float.h>
#include <string.h>
#include <math.h>
#include <x86intrin.h>
#include "avx.h"


#define FLOAT_ALIGN_SIZE 8

/*
#define OSXSAVEFlag (1UL<<27)
#define AVXFlag     ((1UL<<28)|OSXSAVEFlag)
#define VAESFlag    ((1UL<<25)|AVXFlag|OSXSAVEFlag)
#define FMAFlag     ((1UL<<12)|AVXFlag|OSXSAVEFlag)
#define CLMULFlag   ((1UL<< 1)|AVXFlag|OSXSAVEFlag)
int is_avx_available()
{
    int CPUInfo[4], InfoType=1, ECX = 1;
    __cpuidex(CPUInfo, 1, 1);       // read the desired CPUID format
    unsigned int ECX = CPUInfo[2];  // the output of CPUID in the ECX register.
    if ((ECX & feature) != feature) // Missing feature
        return false;
    __int64 val = _xgetbv(0);       // read XFEATURE_ENABLED_MASK register
    if ((val&6) != 6)               // check OS has enabled both XMM and YMM support.
        return 0;
    return 1;
}
*/

/*
typedef struct
{
	int flipped=0;
	int width;
	int width_internal;
	int height;
	float *data; //width must be aligned to 32 bytes
}AVX_MATRIX_F;
 */

int is_avx_available()
{
	__builtin_cpu_init();
	if (__builtin_cpu_supports ("avx"))
	{
		return 1;
	}
	else return 0;
}
int is_avx2_available()
{
	__builtin_cpu_init();
	if (__builtin_cpu_supports ("avx2"))
	{
		return 1;
	}
	else return 0;
}


AVX_MATRIX_F *create_avx_matrix_f(int height, int width)
{
	AVX_MATRIX_F *avx = (AVX_MATRIX_F *)malloc(sizeof(AVX_MATRIX_F));

	//width must be aligned to 8 floats(32 bytes).
	avx->width = width;
	avx->height = height;
	avx->width_internal = (width/FLOAT_ALIGN_SIZE) * FLOAT_ALIGN_SIZE;
	avx->flipped = 0;
	if(width%FLOAT_ALIGN_SIZE > 0)avx->width_internal += FLOAT_ALIGN_SIZE;
	//avx->data = malloc(sizeof(float) * avx->height * avx->width_internal);
	avx->data = (float *)aligned_alloc(32,sizeof(float) * avx->height * avx->width_internal);

	//printf("create avx %p %p\n",(void *)avx,(void *)avx->data);

	fill_zero_f(avx); //clear해줘야 한다. dummy로 남아 있는 부분에 값이 있는 경우, vector 연산시에 필요 없는 element가 합산 될수 있다.

	/*
	for(int i=0;i<avx->height * avx->width_internal;i++)
	{
		printf("[%d] %p %f\n",i,&(avx->data[i]),avx->data[i]);
	}
*/
	return avx;
}

AVX_MATRIX_F *flip_f(AVX_MATRIX_F *matrix)
{
	AVX_MATRIX_F *newone = create_avx_matrix_f(matrix->width,matrix->height);
	newone->width = matrix->width;
	newone->height = matrix->height;
	newone->flipped = 1;
	for(int i=0;i<matrix->height;i++)
	{
		for(int j=0;j<matrix->width;j++)
		{
			float d = get_data_f(matrix,i,j);
			set_data_f(newone,i,j,d);
		}
	}
	return newone;
}
AVX_MATRIX_F *unflip_f(AVX_MATRIX_F *matrix)
{
	AVX_MATRIX_F *newone = create_avx_matrix_f(matrix->height,matrix->width);
	newone->width = matrix->width;
	newone->height = matrix->height;
	newone->flipped = 0;
	for(int i=0;i<matrix->height;i++)
	{
		for(int j=0;j<matrix->width;j++)
		{
			float d = get_data_f(matrix,i,j);
			set_data_f(newone,i,j,d);
		}
	}
	return newone;
}

void fill_zero_f(AVX_MATRIX_F *matrix)
{
	if(matrix->flipped == 0)
	{
		int size_of_data = matrix->height * matrix->width_internal * sizeof(float);
		memset(matrix->data,0,size_of_data);

		/*
		unsigned int *t = (unsigned int *)matrix->data;
		for(int i=0;i<matrix->height * matrix->width_internal;i++)
		{
			printf("[%d] %p %f 0x%x\n",i,&(matrix->data[i]),matrix->data[i],t[i]);
		}
		*/

	}
	else
	{
		int size_of_data = matrix->width * matrix->width_internal * sizeof(float);
		memset(matrix->data,0,size_of_data);
	}
}

float get_data_f(AVX_MATRIX_F *matrix,int row,int column)
{
	//if(row >= matrix->height || column >= matrix->width)return FLT_MIN;

	if(matrix->flipped==0)
	{
		//printf("%p %f\n",&( matrix->data[(row * matrix->width_internal + column)]),matrix->data[(row * matrix->width_internal + column)]);
		return matrix->data[(row * matrix->width_internal + column)];
	}
	else
	{
		return matrix->data[(column * matrix->width_internal + row)];
	}
}
void set_data_f(AVX_MATRIX_F *matrix,int row,int column,float d)
{
	if(row >= matrix->height || column >= matrix->width)return;
	if(matrix->flipped==0)
	{
		matrix->data[(row * matrix->width_internal + column)] = d;
	}
	else
	{
		matrix->data[(column * matrix->width_internal + row)] = d;
	}

}

void release_avx_matrix_f(AVX_MATRIX_F *matrix)
{
	//printf("release avx %p %p\n",(void *)matrix,(void *)matrix->data);
	free(matrix->data);
	free(matrix);
}

void print_matrix_f(AVX_MATRIX_F *matrix)
{
	printf("row:%d column:%d\n",matrix->height,matrix->width);
	for(int i=0;i<matrix->height;i++)
	{
		for(int j=0;j<matrix->width;j++)
		{
			printf("%f ",get_data_f(matrix, i, j));
		}
		printf("\n");
	}
}

float hsum_ps_sse3(__m128 v) {
    __m128 shuf = _mm_movehdup_ps(v);        // broadcast elements 3,1 to 2,0
    __m128 sums = _mm_add_ps(v, shuf);
    shuf        = _mm_movehl_ps(shuf, sums); // high half -> low half
    sums        = _mm_add_ss(sums, shuf);
    return        _mm_cvtss_f32(sums);
}
float hsum256_ps_avx(__m256 v) {
    __m128 vlow  = _mm256_castps256_ps128(v);
    __m128 vhigh = _mm256_extractf128_ps(v, 1); // high 128
           vlow  = _mm_add_ps(vlow, vhigh);     // add the low 128
    return hsum_ps_sse3(vlow);         // and inline the sse3 version, which is optimal for AVX
    // (no wasted instructions, and all of them are the 4B minimum)
}

#define MAX_YMM 16

#if 0
float get_sum_of_registers(__m256 ymm[MAX_YMM],int register_counter)
{
	for(int i=2;i<register_counter;i++)
	{
		if(i==2)continue;
		else ymm[2] = __builtin_ia32_addps256(ymm[2], ymm[i]);
	}
	return hsum256_ps_avx(ymm[2]);
}

float vector_dot_vector(float *data0, float *data1, int len)
{
	__m256 ymm[MAX_YMM];
	float ret = 0;

	int current_register_index=2;

	for(int i=0;i<len/FLOAT_ALIGN_SIZE;i++)
	{
		ymm[0] = __builtin_ia32_loadups256(&(data0[FLOAT_ALIGN_SIZE*i]));
		ymm[1] = __builtin_ia32_loadups256(&(data1[FLOAT_ALIGN_SIZE*i]));
		ymm[current_register_index] = __builtin_ia32_mulps256(ymm[0], ymm[1]);
		current_register_index++;
		if(current_register_index==MAX_YMM)
		{
			ret += get_sum_of_registers(ymm,current_register_index);
			current_register_index=2;
		}
	}

	ret += get_sum_of_registers(ymm,current_register_index);

	return ret;
}
#endif

#if 1

//extern float v_dot_v_avx_burst(float *v0,float *v1, int count);

float vector_dot_vector(float *data0, float *data1, int len)
{
	float sum = 0.0;
	int burst_count = len/AVX_BURST_LEN_F;

	sum = v_dot_v_avx_burst(data0,data1,burst_count);

	int count = (len % AVX_BURST_LEN_F)/FLOAT_ALIGN_SIZE;
	int width = FLOAT_ALIGN_SIZE;

	sum += v_dot_v_avx(&(data0[burst_count * AVX_BURST_LEN_F]),&(data1[burst_count * AVX_BURST_LEN_F]),width,count);
	return sum;
}
#endif

#if 0
float vector_dot_vector(float *data0, float *data1, int len)
{
	__m256 ymm[MAX_YMM];
	float ret = 0;
	float buffer[FLOAT_ALIGN_SIZE];

	float zero[FLOAT_ALIGN_SIZE] = {0};
	ymm[3] = __builtin_ia32_loadups256(zero);

	for(int i=0;i<len/FLOAT_ALIGN_SIZE;i++)
	{
		ymm[0] = __builtin_ia32_loadups256(&(data0[FLOAT_ALIGN_SIZE*i]));
		ymm[1] = __builtin_ia32_loadups256(&(data1[FLOAT_ALIGN_SIZE*i]));
		ymm[2] = __builtin_ia32_dpps256(ymm[0],ymm[1],0xf1);
		ymm[3] = __builtin_ia32_addps256(ymm[3],ymm[2]);

		//__builtin_ia32_storeups256(buffer, ymm[2]);
		//ret += buffer[0];
		//ret += buffer[4];

	}
	ret = hsum256_ps_avx(ymm[3]);
	return ret;
}
#endif

/*
return code
0 : ok
1 : A matrix must be not-flipped.
2 : B matrix must be flipped.
3 : Width of A is not matched to height of B.
4 : Height of A is not matched to height of C.
5 : Width of B is not matched to width of C.
 */
int matrix_dot_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->flipped == 1)return 1;
	if(b->flipped == 0)return 2;

	if(a->width != b->height)return 3;

	if(a->height != c->height)return 4;
	if(b->width != c->width)return 5;

	/*
	printf("v0:0x%x -- v0:0x%x, v1:0x%x -- v1:0x%x\n",a->data,
			&(a->data[a->height * a->width_internal]),
			b->data,
			&(b->data[b->width * b->width_internal]));
	*/

	float sum = 0;
	int counter =0;
	for(int i=0;i<a->height;i++)
	{
		for(int j=0;j<b->width;j++)
		{
			sum = vector_dot_vector(&(a->data[a->width_internal*i]),&(b->data[b->width_internal*j]),a->width_internal);
			set_data_f(c, i, j, sum);
			counter++;
		}
	}


	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
5 : c is flipped
*/
int matrix_add_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->width != b->width)return 1;
	if(b->width != c->width)return 1;

	if(a->flipped)return 3;
	if(b->flipped)return 4;
	if(c->flipped)return 5;

	if(a->height==1)
	{
		if(b->height != c->height)return 21;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<b->height;i++)
			v_add_v_avx(a->data,&(b->data[b->width_internal*i]), &(c->data[c->width_internal*i]), num);
		return 0;
	}
	else if(b->height==1)
	{
		if(a->height != c->height)return 22;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<a->height;i++)
			v_add_v_avx(&(a->data[a->width_internal*i]), b->data, &(c->data[c->width_internal*i]), num);
		return 0;
	}
	else
	{
		if(a->height != b->height)return 23;
		if(b->height != c->height)return 24;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<a->height;i++)
			v_add_v_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), &(c->data[c->width_internal*i]), num);

	}

	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
5 : c is flipped
*/
int matrix_sub_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->width != b->width)return 1;
	if(b->width != c->width)return 1;

	if(a->flipped)return 3;
	if(b->flipped)return 4;
	if(c->flipped)return 5;

	if(a->height==1)
	{
		if(b->height != c->height)return 21;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<b->height;i++)
			sub_avx(&(b->data[b->width_internal*i]),a->data, &(c->data[c->width_internal*i]), num);
		return 0;
	}
	else if(b->height==1)
	{
		if(a->height != c->height)return 22;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<a->height;i++)
			sub_avx(b->data,&(a->data[a->width_internal*i]),  &(c->data[c->width_internal*i]), num);
		return 0;
	}
	else
	{
		if(a->height != b->height)return 23;
		if(b->height != c->height)return 24;

		int num = a->width_internal/FLOAT_ALIGN_SIZE;
		for(int i=0;i<a->height;i++)
			sub_avx(&(b->data[b->width_internal*i]),&(a->data[a->width_internal*i]),  &(c->data[c->width_internal*i]), num);

	}

	return 0;
}


/*
 * 원소끼리 꼽하기
return code
0 : ok
1 : all width of matrix are not matched.
2 : height of matrix are not matched.
3 : a is flipped
4 : b is flipped
5 : c is flipped
*/
int matrix_mmult_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->width != b->width)return 1;
	if(b->width != c->width)return 1;

	int h = a->height;
	if(h==1 && b->height!=1)h = b->height;
	if(h!=c->height)return 2;

	if(a->flipped)return 3;
	if(b->flipped)return 4;
	if(c->flipped)return 5;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	for(int i=0,ah=0,bh=0;i<h;i++)
	{
		v_mmult_v_avx(&(a->data[a->width_internal*ah]), &(b->data[b->width_internal*bh]), &(c->data[c->width_internal*i]), num);
		if(a->height!=1)ah++;
		if(b->height!=1)bh++;
	}

	return 0;
}

/*
 * 원소끼리 나누기
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
5 : c is flipped
*/
int matrix_mdiv_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->width != b->width)return 1;
	if(b->width != c->width)return 1;
	if(a->height != b->height)return 2;
	if(b->height != c->height)return 2;

	if(a->flipped)return 3;
	if(b->flipped)return 4;
	if(c->flipped)return 5;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	int rest = c->width_internal - c->width;

	for(int i=0;i<a->height;i++)
	{
		v_mdiv_v_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), &(c->data[c->width_internal*i]), num);

		//printf("rest:%d offset:%d %d %d\n",rest,c->width_internal*i + num*FLOAT_ALIGN_SIZE - rest,c->width_internal,c->width);
		//clear dummy. NaN in dummy when diving by zero. dummy of b is filled with zero. so output dummy will be NaN.
		int offset = c->width_internal*i + num*FLOAT_ALIGN_SIZE - rest;
		for(int j=0;j<rest;j++)
		{
			c->data[offset + j] = 0;
		}
	}

	return 0;
}

/*
 * c = a^b
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
5 : c is flipped
*/
int matrix_pow_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c)
{
	if(a->width != b->width)return 1;
	if(b->width != c->width)return 1;
	if(a->height != b->height)return 2;
	if(b->height != c->height)return 2;

	if(a->flipped)return 3;
	if(b->flipped)return 4;
	if(c->flipped)return 5;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	int num_for_clearing = a->width_internal - a->width;
	for(int i=0;i<a->height;i++)
	{
		log_avx(&(a->data[a->width_internal*i]), &(c->data[c->width_internal*i]), num);
		v_mmult_v_avx(&(c->data[c->width_internal*i]), &(b->data[b->width_internal*i]), &(c->data[c->width_internal*i]), num);
		exp_avx(&(c->data[c->width_internal*i]), &(c->data[c->width_internal*i]), num);

		if(num_for_clearing>0)
		{
			int offset = c->width_internal*(i+1) - num_for_clearing;
			for(int j=0;j<num_for_clearing;j++)c->data[offset+j] = 0.0f;
		}
	}
	return 0;
}

/*
 * c = a^b (b:scalar)
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : c is flipped
*/
int matrix_pow_f(AVX_MATRIX_F *a, float b, AVX_MATRIX_F *c)
{
	if(a->width != c->width)return 1;
	if(a->height != c->height)return 2;

	if(a->flipped)return 3;
	if(c->flipped)return 4;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	int num_for_clearing = a->width_internal - a->width;

	int need_release_a=0;
	// a 내에 minus값이 있고, b가 정수 일때, 잘 계산이 되지 않고 있다.
	//우선 b가 2의 배수이면 모든 수가 양수가 될테니.. 이 부분만이라도 먼저 해결 한다.
	{
		float f = floor(b + 1e-7);
		float diff = b - f;
		if(diff<1e-7)
		{
			int integer = (int)(b+0.1);
			if(integer % 2 == 0)
			{
				AVX_MATRIX_F * new_a = create_avx_matrix_f(a->height, a->width);
				int ret = matrix_mmult_matrix_f(a, a, new_a);
				need_release_a = 1;
				if(ret!=0)
				{
					release_avx_matrix_f(new_a);
					return ret;
				}
				a = new_a;
				b=b/2.0;
			}
		}
	}

	for(int i=0;i<a->height;i++)
	{
		log_avx(&(a->data[a->width_internal*i]), &(c->data[c->width_internal*i]), num);
		v_mul_s_avx(&(c->data[c->width_internal*i]),&(c->data[c->width_internal*i]),num,&b);
		exp_avx(&(c->data[c->width_internal*i]), &(c->data[c->width_internal*i]), num);

		if(num_for_clearing>0)
		{
			int offset = c->width_internal*(i+1) - num_for_clearing;
			for(int j=0;j<num_for_clearing;j++)c->data[offset+j] = 0.0f;
		}
	}

	if(need_release_a)release_avx_matrix_f(a);

	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_scale_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b, float s)
{

	if(a->width != b->width)return 1;
	if(a->height != b->height)return 2;
	if(a->flipped)return 3;
	if(b->flipped)return 4;


	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	for(int i=0;i<a->height;i++)
		v_mul_s_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), num, &s);
	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_add_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b, float s)
{

	if(a->width != b->width)return 1;
	if(a->height != b->height)return 2;
	if(a->flipped)return 3;
	if(b->flipped)return 4;

	// dummy 영역의 값이 s 만큼 더해져서 0이 아닌 값이 될수 있으므로, dummy영역을 제외한 float array에만 값을 기입하도록 함.
	// dummy영역에 값이 있으면 sigmoid값계산, 합계계산,dot연산등이 모두 틀려진다.
	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	int num_for_single_op = FLOAT_ALIGN_SIZE - (a->width_internal - a->width);
	//printf("ffff %d %d %d\n",a->width_internal,a->width,num_for_single_op);
	if(num_for_single_op>0)num--;

	for(int i=0;i<a->height;i++)
	{
		v_add_s_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), num, &s);
		float *lastA = &(a->data[a->width_internal*i + FLOAT_ALIGN_SIZE*num]);
		float *lastB = &(b->data[b->width_internal*i + FLOAT_ALIGN_SIZE*num]);

		for(int j=0;j<num_for_single_op;j++)
		{
			lastB[j] = lastA[j] + s;
		}
	}
	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_exp_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b)
{
	if(a->width != b->width)return 1;
	if(a->height != b->height)return 2;
	if(a->flipped)return 3;
	if(b->flipped)return 4;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	for(int i=0;i<a->height;i++)
	{
		exp_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), num);
	}
	return 0;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_sigmoid_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b)
{
	if(a->width != b->width)return 1;
	if(a->height != b->height)return 2;
	if(a->flipped)return 3;
	if(b->flipped)return 4;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	for(int i=0;i<a->height;i++)
	{
		//float *ad = &(a->data[a->width_internal*i]);
		//printf("ad:%f, %d\n",ad[0],num);
		sigmoid_avx(&(a->data[a->width_internal*i]), &(b->data[b->width_internal*i]), num);
	}
	return 0;
}

/*
return code
0 : ok
1 : Width of matrix are not matched to Height of output.
2 : Height of matrix are not matched to Width of output.
*/
int matrix_transpose_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b)
{
	if(a->width != b->height)return 1;
	if(a->height != b->width)return 2;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	for(int i=0;i<a->height;i++)
	{
		for(int j=0;j<a->width;j++)
		{
			set_data_f(b, j, i, get_data_f(a, i,j));
		}
	}
	return 0;
}

/*
return code
0 : ok
1 : Height of matrix are not matched to height of output.
*/
int matrix_sum_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b)
{
	if(a->height != b->height)return 1;

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	float sum = 0.0;
	for(int i=0;i<a->height;i++)
	{
		sum = sum_avx(&(a->data[a->width_internal*i]), num);
		set_data_f(b, i, 0, sum);
	}
	return 0;
}

int matrix_sum_all_f(AVX_MATRIX_F *a, float *output)
{

	int num = a->width_internal/FLOAT_ALIGN_SIZE;
	(*output)=0.0;
	for(int i=0;i<a->height;i++)
	{
		(*output) += sum_avx(&(a->data[a->width_internal*i]), num);
	}
	return 0;
}

float CEE_single(float *answer, float *right_answer, int num)
{
	float ret = 0.0;
	if(num==0)return ret;
	for(int i=0;i<num;i++)
	{
		float ff = right_answer[i] * log(answer[i] + 0.00000001);
		ret += ff;
	}
	ret *= -1.0;
	return ret;
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_CEE_f(AVX_MATRIX_F *answer,AVX_MATRIX_F *right_answer,float *ret)
{
	if(answer->width != right_answer->width)return 1;
	if(answer->height != right_answer->height)return 2;
	if(answer->flipped)return 3;
	if(right_answer->flipped)return 4;

	//last vector may have dummies. we should exclude the dummies.
	int num_busrt = answer->width/FLOAT_ALIGN_SIZE;
	int num_single  = answer->width%FLOAT_ALIGN_SIZE;
	float sum = 0.0;

	//printf("num of burst:%d single:%d\n",num_busrt,num_single);
	for(int i=0;i<answer->height;i++)
	{
		sum += cross_entropy_error(&(answer->data[answer->width_internal*i]) , &(right_answer->data[answer->width_internal*i]), num_busrt);
		sum += CEE_single(&(answer->data[answer->width_internal*i + num_busrt*FLOAT_ALIGN_SIZE]),
				&(right_answer->data[answer->width_internal*i + num_busrt*FLOAT_ALIGN_SIZE]),num_single);
	}
	(*ret) = sum/((float)(answer->height));
	return 0;
}

float get_max(float *input,  int num, float init_value)
{
	float ret = init_value;

	for(int i=0;i<num;i++)
	{
		if(ret<input[i])ret = input[i];
	}
	return ret;
}
void get_soft_max(float *input, float *output, int num, float max)
{
	float tmp;
	float sum = 0.0;
	for(int i=0;i<num;i++)
	{
		tmp = input[i] - max;
		output[i] = exp(tmp);
		//printf("input: [%d] %f %f %f\n",i,input[i],max,output[i]);
		sum += output[i];
	}
	//printf("sum: %f\n",sum);
	for(int i=0;i<num;i++)
	{
		output[i] = output[i]/sum;
	}
}

/*
return code
0 : ok
1 : all width of matrix are not matched.
2 : all height of matrix are not matched.
3 : a is flipped
4 : b is flipped
*/
int matrix_softmax_f(AVX_MATRIX_F *input,AVX_MATRIX_F *output)
{
	if(input->width != output->width)return 1;
	if(input->height != output->height)return 2;
	if(input->flipped)return 3;
	if(output->flipped)return 4;

	//last vector may have dummies. we should exclude the dummies.
	int num_busrt = input->width/FLOAT_ALIGN_SIZE;
	int num_single  = input->width%FLOAT_ALIGN_SIZE;

	float max = 0.0;
	float sum = 0.0;
	//printf("num of burst:%d single:%d\n",num_busrt,num_single);
	float *burst_input_addr;
	float *burst_output_addr;
	float *single_input_addr;
	float *single_output_addr;
	for(int i=0;i<input->height;i++)
	{
		burst_input_addr = &(input->data[input->width_internal*i]);
		burst_output_addr = &(output->data[output->width_internal*i]);
		single_input_addr = &(input->data[input->width_internal*i + num_busrt*FLOAT_ALIGN_SIZE]);
		single_output_addr = &(output->data[output->width_internal*i + num_busrt*FLOAT_ALIGN_SIZE]);

		max = get_max_avx(burst_input_addr, num_busrt, 0);
		max = get_max(single_input_addr,num_single,max);

		sub_single_avx(burst_input_addr, &max, burst_output_addr, num_busrt);
		exp_avx(burst_output_addr, burst_output_addr, num_busrt);
		sum = sum_avx(burst_output_addr, num_busrt);

		// rest data handle
		for(int j=0;j<num_single;j++)
		{
			single_output_addr[j] = single_input_addr[j] - max;
			single_output_addr[j] = exp(single_output_addr[j]);
			sum+=single_output_addr[j];
		}

		for(int j=0;j<input->width;j++)
		{
			burst_output_addr[j] = burst_output_addr[j]/sum;
		}

	}
	return 0;
}


struct tmp_123 {
	union
	{
		float d[30];
		int32_t i[30];
	};
};

void print_m128i(__m128i v)
{
	//v = _mm_set1_epi32 (0xff800000);

	printf("0x%x ",_mm_extract_epi32(v,0));
	printf("0x%x ",_mm_extract_epi32(v,1));
	printf("0x%x ",_mm_extract_epi32(v,2));
	printf("0x%x ",_mm_extract_epi32(v,3));

	printf("\n");
}
void print_m128(__m128 v)
{
	static struct tmp_123 input __attribute__ ((aligned (32)));
	_mm_store_ps(input.d,v);

	printf("%f(0x%x) ",input.d[0],input.i[0]);
	printf("%f(0x%x) ",input.d[1],input.i[1]);
	printf("%f(0x%x) ",input.d[2],input.i[2]);
	printf("%f(0x%x) ",input.d[3],input.i[3]);

	printf("\n");
}

// 추후 레퍼런스용으로 다음 코드는 있음. 실제 사용하지 않음.
float fastExp4(register float x)  // quartic spline approximation
{
    union { float f; int32_t i; } reinterpreter;

    reinterpreter.i = (int32_t)(12102203.0f*x) + 127*(1 << 23);

    int32_t m = (reinterpreter.i >> 7) & 0xFFFF;  // copy mantissa
    // empirical values for small maximum relative error (1.21e-5):
    int32_t k0 = (3537*m) >> 16;
    int32_t k1 = (k0 + 13668)*m;
    int32_t k2 = k1 >> 18;
    int32_t k3 = ((k2 + 15817)*m);
    int32_t k4 = (( k3 >> 14) - 80470);
    int32_t k5 = k4*m >> 11;
    reinterpreter.i += k5;
    return reinterpreter.f;
}


