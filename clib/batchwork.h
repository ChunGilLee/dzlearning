/*
 * batchwork.h
 *
 *  Created on: Jun 20, 2018
 *      Author: highvolt
 */

#ifndef BATCHWORK_H_
#define BATCHWORK_H_

#include "avx.h"

#define TYPE_OF_JOB_DOT 1
#define TYPE_OF_JOB_ADD 2
#define TYPE_OF_JOB_SCALE 3
typedef struct
{
	int type_of_job;
	void *operand0;
	void *operand1;
	void *operand2;
	void *operand3;

	float operand_f0;

}JOB;

#endif /* BATCHWORK_H_ */
