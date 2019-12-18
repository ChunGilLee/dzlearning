/*
 * java_interface.c
 *
 *  Created on: Jun 20, 2018
 *      Author: highvolt
 */

#include "jni.h"
#include <stdio.h>
#include "java_interface.h"
#include "avx.h"

JNIEXPORT void JNICALL Java_dolziplib_Batchwork_init(JNIEnv *env, jobject thisObj)
{
	printf("init batchwork\n");
}
JNIEXPORT void JNICALL Java_dolziplib_Batchwork_deinit(JNIEnv *env, jobject thisObj)
{
	printf("deinit batchwork\n");
}

JNIEXPORT jlong JNICALL Java_dolziplib_matrix_MatrixAVX_init(JNIEnv *env, jobject thisObj,jint height,jint width)
{
	AVX_MATRIX_F *m = create_avx_matrix_f(height, width);
	//printf("reserved m: 0x%llx\n",(long long unsigned int)m);

	if(m!=0)
	{
		if(m->data==0)
		{
			free(m);
			return 0;
		}
	}

	return (jlong)m;
}
JNIEXPORT jlong JNICALL Java_dolziplib_matrix_MatrixAVX_flip(JNIEnv *env, jobject thisObj,jlong addr)
{
	AVX_MATRIX_F *m = (AVX_MATRIX_F *)addr;
	AVX_MATRIX_F *ret = flip_f(m);
	return (jlong)ret;
}
JNIEXPORT jlong JNICALL Java_dolziplib_matrix_MatrixAVX_unflip(JNIEnv *env, jobject thisObj,jlong addr)
{
	AVX_MATRIX_F *m = (AVX_MATRIX_F *)addr;
	AVX_MATRIX_F *ret = unflip_f(m);
	return (jlong)ret;
}
JNIEXPORT void JNICALL Java_dolziplib_matrix_MatrixAVX_deinit(JNIEnv *env, jobject thisObj,jlong addr)
{
	AVX_MATRIX_F *m = (AVX_MATRIX_F *)addr;
	//printf("release m: 0x%llx\n",(long long unsigned int)m);
	release_avx_matrix_f(m);
}

JNIEXPORT jfloat JNICALL Java_dolziplib_matrix_MatrixAVX_getdata(JNIEnv *env, jobject thisObj,jlong addr, jint row,jint column)
{
	AVX_MATRIX_F *m = (AVX_MATRIX_F *)addr;
	float d = get_data_f(m, row, column);
	return d;
}
JNIEXPORT void JNICALL Java_dolziplib_matrix_MatrixAVX_setdata(JNIEnv *env, jobject thisObj,jlong addr, jint row,jint column, jfloat d)
{
	AVX_MATRIX_F *m = (AVX_MATRIX_F *)addr;
	set_data_f(m, row, column, d);
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_dot(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_dot_matrix_f(a, b, c);
	return ret;
}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_add(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_add_matrix_f(a, b, c);
	return ret;

}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sub(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_sub_matrix_f(a, b, c);
	return ret;

}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_scale(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jfloat s)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_scale_f(a, b, s);
	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sigmoid(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_sigmoid_f(a, b);
	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_CEE(JNIEnv *env, jobject thisObj, jobject retObj,
		jlong matrixA, jlong matrixB)
{
	jclass return_class = (*env)->GetObjectClass(env, retObj);

	// get integer from retObj.getAge()
	//jmethodID midGetAge = (*env)->GetMethodID(env, returnClass, "getAge", "()I");
	//int age =  (*env)->CallIntMethod(env, retObj, midGetAge);
	//printf("age:%d\n",age);

	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	float ret_value = 0.0;
	int ret = matrix_CEE_f(a, b, &ret_value);
	if(ret!=0)return ret;

	jmethodID set_float = (*env)->GetMethodID(env, return_class, "setFloat", "(F)V");
	(*env)->CallIntMethod(env, retObj, set_float, ret_value);

	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_softmax(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_softmax_f(a, b);
	return ret;
}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_addscalar(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, float s)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_add_f(a, b, s);
	return ret;
}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mmult(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_mmult_matrix_f(a, b, c);
	return ret;

}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mdiv(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_mdiv_matrix_f(a, b, c);
	return ret;

}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_transpose(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_transpose_f(a, b);
	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sum(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_sum_f(a, b);
	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_pow(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;
	AVX_MATRIX_F *c = (AVX_MATRIX_F *)matrixC;

	int ret = matrix_pow_matrix_f(a, b, c);
	return ret;

}
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mpow(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jfloat s)
{
	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	AVX_MATRIX_F *b = (AVX_MATRIX_F *)matrixB;

	int ret = matrix_pow_f(a, s, b);
	return ret;
}

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_allsum(JNIEnv *env, jobject thisObj, jobject retObj,
		jlong matrixA)
{
	jclass return_class = (*env)->GetObjectClass(env, retObj);

	AVX_MATRIX_F *a = (AVX_MATRIX_F *)matrixA;
	float ret_value = 0.0;
	int ret = matrix_sum_all_f(a, &ret_value);
	if(ret!=0)return ret;

	jmethodID set_float = (*env)->GetMethodID(env, return_class, "setFloat", "(F)V");
	(*env)->CallIntMethod(env, retObj, set_float, ret_value);

	return ret;
}
