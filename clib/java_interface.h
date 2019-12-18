/*
 * java_interface.h
 *
 *  Created on: Jun 20, 2018
 *      Author: highvolt
 */

#ifndef JAVA_INTERFACE_H_
#define JAVA_INTERFACE_H_

#include "jni.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_dolziplib_Batchwork_init(JNIEnv *env, jobject thisObj);
JNIEXPORT void JNICALL Java_dolziplib_Batchwork_deinit(JNIEnv *env, jobject thisObj);

JNIEXPORT jlong JNICALL Java_dolziplib_matrix_MatrixAVX_init(JNIEnv *env, jobject thisObj,jint height,jint width);
JNIEXPORT void JNICALL Java_dolziplib_matrix_MatrixAVX_deinit(JNIEnv *env, jobject thisObj,jlong addr);
JNIEXPORT jlong JNICALL Java_dolziplib_matrix_MatrixAVX_flip(JNIEnv *env, jobject thisObj,jlong addr);

JNIEXPORT jfloat JNICALL Java_dolziplib_matrix_MatrixAVX_getdata(JNIEnv *env, jobject thisObj,jlong addr, jint row,jint column);
JNIEXPORT void JNICALL Java_dolziplib_matrix_MatrixAVX_setdata(JNIEnv *env, jobject thisObj,jlong addr, jint row,jint column, jfloat d);

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_dot(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_add(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_scale(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, float s);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sigmoid(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB);

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_CEE(JNIEnv *env, jobject thisObj, jobject retObj,
		jlong matrixA, jlong matrixB);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_softmax(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_addscalar(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, float s);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mmult(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mdiv(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_transpose(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB);

JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sum(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_pow(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_mpow(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jfloat s);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_sub(JNIEnv *env, jobject thisObj,
		jlong matrixA, jlong matrixB, jlong matrixC);
JNIEXPORT jint JNICALL Java_dolziplib_matrix_MatrixOperatorAVX_allsum(JNIEnv *env, jobject thisObj, jobject retObj,
		jlong matrixA);

#ifdef __cplusplus
}
#endif

#endif /* JAVA_INTERFACE_H_ */
