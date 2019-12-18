/*
 * batchwork.c
 *
 *  Created on: Jun 20, 2018
 *      Author: highvolt
 */


#include <stdio.h>
#include "batchwork.h"

int do_jobs(int num_of_job, JOB jobs[num_of_job])
{
	int ret = 0;
	for(int i=0;i<num_of_job;i++)
	{
		if(jobs[i].type_of_job == TYPE_OF_JOB_DOT)
		{
			AVX_MATRIX_F *a = (AVX_MATRIX_F *)jobs[i].operand0;
			AVX_MATRIX_F *b = (AVX_MATRIX_F *)jobs[i].operand1;
			AVX_MATRIX_F *c = (AVX_MATRIX_F *)jobs[i].operand2;

			ret = matrix_dot_matrix_f(a, b, c);
			if(ret!=0)return ret;
		}
		else if(jobs[i].type_of_job == TYPE_OF_JOB_ADD)
		{
			AVX_MATRIX_F *a = (AVX_MATRIX_F *)jobs[i].operand0;
			AVX_MATRIX_F *b = (AVX_MATRIX_F *)jobs[i].operand1;
			AVX_MATRIX_F *c = (AVX_MATRIX_F *)jobs[i].operand2;

			ret = matrix_add_matrix_f(a, b, c);
			if(ret!=0)return ret;
		}
		else if(jobs[i].type_of_job == TYPE_OF_JOB_SCALE)
		{
			AVX_MATRIX_F *a = (AVX_MATRIX_F *)jobs[i].operand0;
			AVX_MATRIX_F *b = (AVX_MATRIX_F *)jobs[i].operand1;
			ret = matrix_scale_f(a, b, *(jobs[i].operand_f0));
			if(ret!=0)return ret;
		}
	}
	return ret;
}
