/*
 * avx.h
 *
 *  Created on: Jun 17, 2018
 *      Author: highvolt
 */

#ifndef AVX_H_
#define AVX_H_

#include <x86intrin.h>

int is_avx_available();
int is_avx2_available();

typedef struct
{
	int flipped; // flipping for avx matrix operation. Second operand must be flipped matrix.
	int width; //same to original width in flipped.
	int width_internal; //number of columns in row in data footprint.
	int height; //same to original height in flipped.
	float *data; //width must be aligned to 32 bytes. Only data footprint is different from not-flipped matrix.

}AVX_MATRIX_F;

AVX_MATRIX_F *create_avx_matrix_f(int height, int width);
void release_avx_matrix_f(AVX_MATRIX_F *matrix);
AVX_MATRIX_F *flip_f(AVX_MATRIX_F *matrix);
AVX_MATRIX_F *unflip_f(AVX_MATRIX_F *matrix);

void fill_zero_f(AVX_MATRIX_F *matrix);

float get_data_f(AVX_MATRIX_F *matrix,int row,int column);
void set_data_f(AVX_MATRIX_F *matrix,int row,int column,float d);
void print_matrix_f(AVX_MATRIX_F *matrix);


int matrix_dot_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_add_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_sub_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_scale_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b, float s);
int matrix_sigmoid_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b);
int matrix_exp_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b);
int matrix_CEE_f(AVX_MATRIX_F *answer,AVX_MATRIX_F *right_answer,float *ret);
int matrix_softmax_f(AVX_MATRIX_F *input,AVX_MATRIX_F *output);
int matrix_add_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b, float s);
int matrix_mmult_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_mdiv_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_transpose_f(AVX_MATRIX_F *input,AVX_MATRIX_F *output);
int matrix_sum_f(AVX_MATRIX_F *a,AVX_MATRIX_F *b);
int matrix_sum_all_f(AVX_MATRIX_F *a, float *output);
int matrix_pow_matrix_f(AVX_MATRIX_F *a, AVX_MATRIX_F *b, AVX_MATRIX_F *c);
int matrix_pow_f(AVX_MATRIX_F *a, float b, AVX_MATRIX_F *c);

float vector_dot_vector(float *data0, float *data1, int len);

float fastExp4(register float x);

/////////////////////////////////////////////////////////////////
// implemented by assembly
/////////////////////////////////////////////////////////////////
//width is 8. (avx float size in ymm?)
// count is number of lines.
// v0, v1 must be aligned 32 bytes
extern float v_dot_v_avx(float *v0,float *v1, int width, int count);

#define AVX_BURST_LEN_F 56
//count : number burst. one burst is 56 floats.
extern float v_dot_v_avx_burst(float *v0,float *v1, int count);

//count : number of iteraion( one iteration : 8 floats)
// v2 = v0 + v1
extern void v_add_v_avx(float *v0, float *v1, float *v2, int count);

// 원소끼리 꼽하기
extern void v_mmult_v_avx(float *v0, float *v1, float *v2, int count);

// 원소끼리 나누기
extern void v_mdiv_v_avx(float *v0, float *v1, float *v2, int count);

//count : number of iteraion( one iteration : 8 floats)
// v1 = v0 * s
// 's' is address of single float data
extern void v_mul_s_avx(float *v0, float *v1, int count, float *s);
//extern void v_mul_s_avx(float s);
extern void v_add_s_avx(float *v0, float *v1, int count, float *s);

//extern void exp_avx(float *input, float *ouput, int count);
extern void exp_avx(float *input, float *ouput, int count);
extern void sigmoid_avx(float *input, float *ouput, int count);
extern void add_dword_avx(int32_t *input0, int32_t *input1, int32_t *output);
extern void sub_dword_avx(int32_t *input0, int32_t *input1, int32_t *output);
extern void log_avx(float *input, float *output, int count);
extern float cross_entropy_error(float *answer, float *rightAnswer, int count);
extern float get_max_avx(float *input, int count, float *buf);
extern void sub_avx(float *a,float *b, float *out, int count);
extern float sum_avx(float *a, int count);
extern void sub_single_avx(float *vector, float *scalar, float *vector_output, int count);
extern void div_single_avx(float *vector, float *scalar, float *vector_output, int count);
extern float avx_test(float *a, float *b, float *c, int count);

////////////////////////////////////////////////////////////////

#endif /* AVX_H_ */
