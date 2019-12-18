.section	.text

.macro avx_lack_function instruction
	vmovaps %ymm12,%ymm13
	\instruction %xmm11,%xmm13
	vperm2f128 $0x81,%ymm11,%ymm11,%ymm11
	vperm2f128 $0x81,%ymm12,%ymm12,%ymm12
	\instruction %xmm11,%xmm12
	vperm2f128 $0x20,%ymm12,%ymm13,%ymm13
.endm

.macro lack_func instruction,input0_reg,input1_reg,output0_reg
	vmovaps \input0_reg,%ymm11
	vmovaps \input1_reg,%ymm12
	avx_lack_function \instruction
	vmovaps %ymm13,\output0_reg
.endm

// ymm11->input0, ymm12->input1, ymm13->output
add_dword_avx_actual:
	avx_lack_function paddd
	ret
sub_dword_avx_actual:
	avx_lack_function psubd
	ret

.macro lack_func_shift_right num input_reg output_reg
	vmovaps \input_reg,%ymm11
	psrad \num,%xmm11
	vmovaps %ymm11,%ymm12
	vmovaps \input_reg,%ymm11
	vperm2f128 $0x01,%ymm11,%ymm11,%ymm11
	psrad \num,%xmm11
	vperm2f128 $0x20,%ymm11,%ymm12,\output_reg
.endm

.globl v_dot_v_avx_burst
.type v_dot_v_avx_burst @function
v_dot_v_avx_burst:

push %rbp

//rdi : float *v0
//rsi : float *v1
//rdx : int count, number of burst (56 floats, 224 bytes)


        vxorps %ymm14,%ymm14,%ymm14
main_loop_burst:
        cmp $0,%rdx
        jle add_ymm3_burst
        vmovaps (%rdi),%ymm0
        vmovaps (%rsi),%ymm1
        vmovaps 32(%rdi),%ymm2
        vmovaps 32(%rsi),%ymm3
        vmovaps 64(%rdi),%ymm4
        vmovaps 64(%rsi),%ymm5
        vmovaps 96(%rdi),%ymm6
        vmovaps 96(%rsi),%ymm7
        vmovaps 128(%rdi),%ymm8
        vmovaps 128(%rsi),%ymm9
        vmovaps 160(%rdi),%ymm10
        vmovaps 160(%rsi),%ymm11
        vmovaps 192(%rdi),%ymm12
        vmovaps 192(%rsi),%ymm13
        vdpps $0xf1, %ymm0, %ymm1, %ymm0
        vdpps $0xf1, %ymm2, %ymm3, %ymm2
        vdpps $0xf1, %ymm4, %ymm5, %ymm4
        vdpps $0xf1, %ymm6, %ymm7, %ymm6
        vdpps $0xf1, %ymm8, %ymm9, %ymm8
        vdpps $0xf1, %ymm10, %ymm11, %ymm10
        vdpps $0xf1, %ymm12, %ymm13, %ymm12
        vaddps %ymm0, %ymm2, %ymm0
        vaddps %ymm4, %ymm6, %ymm4
        vaddps %ymm8, %ymm10, %ymm8
        vaddps %ymm0, %ymm4, %ymm0
        vaddps %ymm8, %ymm12, %ymm8
        vaddps %ymm0, %ymm8, %ymm0
        vaddps %ymm0, %ymm14, %ymm14
        sub $1,%rdx
        add $224,%rdi
        add $224,%rsi
        jmp main_loop_burst
add_ymm3_burst:

        //vmovups %ymm3,%ymm0

        #if 1
        vextractf128 $1,%ymm14,%xmm1
        movaps %xmm14, %xmm0
        addps %xmm1, %xmm0
        #endif

        #if 0
        //sub $32,%rsp
        //vmovups %ymm3,(%rsp)
        //movups (%rsp),%xmm0
        //addss 16(%rsp),%xmm0
        //add $32,%rsp
        #endif

exit_v_dot_v_avx_burst:
        pop %rbp
        ret



.globl v_dot_v_avx
.type v_dot_v_avx @function
v_dot_v_avx:

push %rbp

//rdi : float *v0
//rsi : float *v1
//rdx : int width, number of floats
//rcx : int count, number of width


	vxorps %ymm3,%ymm3,%ymm3
main_loop:
	cmp $0,%rcx
	jle add_ymm3
	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1
	vdpps $0xf1, %ymm0, %ymm1, %ymm2
	vaddps %ymm2, %ymm3, %ymm3
	sub $1,%rcx
	add $32,%rdi
	add $32,%rsi
	jmp main_loop
add_ymm3:
	
	//vmovups %ymm3,%ymm0

	#if 1
	vextractf128 $1,%ymm3,%xmm1
	movaps %xmm3, %xmm0
	addps %xmm1, %xmm0
	#endif
	
	#if 0
	//sub $32,%rsp
	//vmovups %ymm3,(%rsp)
	//movups (%rsp),%xmm0
	//addss 16(%rsp),%xmm0
	//add $32,%rsp
	#endif

exit_v_dot_v_avx:
	pop %rbp
	ret


.globl avx_test
.type avx_test @function
avx_test:

push %rbp

//rdi : float *v0
//rsi : float *v1
//rdx : float *v2(output)
//rcx : int count, number of width


	vxorps %ymm3,%ymm3,%ymm3
avx_test_loop:
	cmp $0,%rcx
	jle avx_test_end
	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1

	sub $1,%rcx
	add $32,%rdi
	add $32,%rsi
	jmp avx_test_loop

avx_test_end:
	pop %rbp
	ret


.globl v_add_v_avx
.type v_add_v_avx @function
v_add_v_avx:

push %rbp

//rdi : float *v0 input
//rsi : float *v1 input
//rdx : float *v2 output
//rcx : int count, number of width

v_add_v_avx_loop:
	cmp $0,%rcx
	jle v_add_v_avx_end
	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1
	vaddps %ymm0, %ymm1, %ymm2
	vmovaps %ymm2,(%rdx)
	sub $1,%rcx
	add $32,%rdi
	add $32,%rsi
	add $32,%rdx
	jmp v_add_v_avx_loop
v_add_v_avx_end:

	pop %rbp
	ret

.globl v_mul_s_avx
.type v_mul_s_avx @function
v_mul_s_avx:

push %rbp

//rdi : float *v0 input
//rsi : float *v1 output
//rdx : int count, number of width
//rcx : address of scale data.
	//call print_reg
	vbroadcastss (%rcx),%ymm1
v_mul_s_avx_loop:
	cmp $0,%rdx
	jle v_mul_s_avx_end
	vmovaps (%rdi),%ymm0
	vmulps %ymm0, %ymm1, %ymm2
	vmovaps %ymm2, (%rsi)
	sub $1,%rdx
	add $32,%rdi
	add $32,%rsi
	jmp v_mul_s_avx_loop
v_mul_s_avx_end:
	pop %rbp
	ret

/* 오차가 너무 큰 버그가 있어서 사용할수 없음
.globl exp_avx
.type exp_avx @function
exp_avx:

push %rbp

//rdi : float *v0 : input
//rsi : float *v1 : output
//rdx : int count, number of line

	lea exp_avx_data(%rip),%rax
	vbroadcastss (%rax),%ymm0  // ymm0 : a
	vbroadcastss 4(%rax),%ymm1 // ymm1 : m
	vbroadcastss 8(%rax),%ymm2  // ymm2 : ttm32
	vbroadcastss 12(%rax),%ymm3  // ymm3 : c0
	vbroadcastss 16(%rax),%ymm4  // ymm4 : c1
	vbroadcastss 20(%rax),%ymm5  // ymm5 : c2
	//ymm10 for accmulator

exp_avx_loop:
	cmp $0,%rdx
	jle exp_avx_end

	vmovaps (%rdi),%ymm6 // ymm6 : x
	add $32,%rdi

	vmulps %ymm0,%ymm6,%ymm10               //_mm_mul_ps (a, x)
	vcvtps2dq %ymm10,%ymm7 // ymm7 : t      //t = _mm_cvtps_epi32 (_mm_mul_ps (a, x));
	vandps %ymm7,%ymm1,%ymm8 // ymm8 : j     //j = _mm_and_si128 (t, m);
	vmovaps %ymm7, %ymm11
	vmovaps %ymm8, %ymm12
	call sub_dword_avx_actual
	vmovaps %ymm13, %ymm7                     //t = _mm_sub_epi32 (t, j);
	//VPSUBD %ymm7,%ymm8,%ymm7                //t = _mm_sub_epi32 (t, j); <--- 찾아라

	vcvtdq2ps %ymm7,%ymm10                  //_mm_cvtepi32_ps (t)

	vmulps %ymm2,%ymm10, %ymm9 //ymm9: f    //mm_mul_ps (ttm23, _mm_cvtepi32_ps (t));
	vmovaps %ymm3,%ymm10   // ymm10 : p     //p = c0;
	vmulps %ymm10,%ymm9, %ymm10              //p = _mm_mul_ps (p, f);
	vaddps %ymm10,%ymm4, %ymm10             //p = _mm_add_ps (p, c1);
	vmulps %ymm10,%ymm9, %ymm10             //p = _mm_mul_ps (p, f);
	vaddps %ymm10,%ymm5, %ymm10             //p = _mm_add_ps (p, c2);
	//nothing do to                         //_mm_castps_si128 (p)

	vmovaps %ymm8, %ymm11
	vmovaps %ymm10, %ymm12
	call add_dword_avx_actual
	vmovaps %ymm13, %ymm10                     //_mm_add_epi32 (j, _mm_castps_si128 (p))

//	vpaddd %ymm8, %ymm10, %ymm10                   //_mm_add_epi32 (j, _mm_castps_si128 (p)) <--- 찾아라
	//nothing do to                          //r = _mm_castsi128_ps (_mm_add_epi32 (j, _mm_castps_si128 (p)));
	vmovaps %ymm10,(%rsi)
	add $32,%rsi
	dec %rdx
	jmp exp_avx_loop

exp_avx_end:
	pop %rbp
	ret

// access byte offset.
exp_avx_data:
	.float 12102203.0
	.long 0xff800000
	.float 1.1920929e-7
	.float 0.3371894346
	.float 0.657636276
	.float 1.00172476
*/

.globl sigmoid_avx
.type sigmoid_avx @function
sigmoid_avx:

	push %rbp

//rdi : float *v0 : input
//rsi : float *v1 : output
//rdx : int count, number of line

	lea exp_avx_data2(%rip),%rax
	vbroadcastss (%rax),%ymm0  // ymm0 : 12102203.0
	vbroadcastss 4(%rax),%ymm1 // ymm1 : 127*(1 << 23)
	vbroadcastss 8(%rax),%ymm2  // ymm2 : 3537
	vbroadcastss 12(%rax),%ymm3  // ymm3 : 13668
	vbroadcastss 16(%rax),%ymm4  // ymm4 : 15817
	vbroadcastss 20(%rax),%ymm5  // ymm5 : -80470
	vbroadcastss 24(%rax),%ymm6  // ymm6 : 0xffff
	//ymm10 for accmulator
	vbroadcastss 28(%rax),%ymm15  // ymm15 : 1.0
	vbroadcastss 32(%rax),%ymm14  // ymm15 : -1.0

sigmoid_avx_loop:
	cmp $0,%rdx
	jle sigmoid_avx_end

	vmovaps (%rdi),%ymm7 // ymm7 : x
	add $32,%rdi

	vmulps %ymm14,%ymm7,%ymm7
	call exp_avx2_internal
	vaddps %ymm15,%ymm10,%ymm10
	vdivps %ymm10,%ymm15,%ymm10

	vmovaps %ymm10,(%rsi)
	add $32,%rsi
	dec %rdx
	jmp sigmoid_avx_loop

sigmoid_avx_end:
	pop %rbp
	ret

.globl exp_avx
.type exp_avx @function
exp_avx:

push %rbp

//rdi : float *v0 : input
//rsi : float *v1 : output
//rdx : int count, number of line

	lea exp_avx_data2(%rip),%rax
	vbroadcastss (%rax),%ymm0  // ymm0 : 12102203.0
	vbroadcastss 4(%rax),%ymm1 // ymm1 : 127*(1 << 23)
	vbroadcastss 8(%rax),%ymm2  // ymm2 : 3537
	vbroadcastss 12(%rax),%ymm3  // ymm3 : 13668
	vbroadcastss 16(%rax),%ymm4  // ymm4 : 15817
	vbroadcastss 20(%rax),%ymm5  // ymm5 : -80470
	vbroadcastss 24(%rax),%ymm6  // ymm6 : 0xffff
	//ymm10 for accmulator

exp_avx_loop2:
	cmp $0,%rdx
	jle exp_avx_end2

	vmovaps (%rdi),%ymm7 // ymm7 : x
	add $32,%rdi

	call exp_avx2_internal

	vmovaps %ymm10,(%rsi)
	add $32,%rsi
	dec %rdx
	jmp exp_avx_loop2

exp_avx_end2:
	pop %rbp
	ret

// input : ymm7, output ymm10
exp_avx2_internal:
	vmulps %ymm0,%ymm7,%ymm10               //(12102203.0f*x)
	vcvtps2dq %ymm10,%ymm10              //(int32_t)(12102203.0f*x)

	lack_func paddd,%ymm1,%ymm10,%ymm10  //(int32_t)(12102203.0f*x) - 127*(1 << 23);
	vmovaps %ymm10,%ymm9       //ymm9 : reinterpreter.i
	lack_func_shift_right $7,%ymm10,%ymm10  //reinterpreter.i >> 7

	lack_func pand,%ymm6,%ymm10,%ymm10 //(reinterpreter.i >> 7) & 0xFFFF

	vmovaps %ymm10,%ymm8			//ymm8 : m
	lack_func pmulld,%ymm2,%ymm10,%ymm10 //3537 * m
	lack_func_shift_right $16,%ymm10,%ymm10  // >> 16
	lack_func paddd,%ymm3,%ymm10,%ymm10 // - 13668

	lack_func pmulld,%ymm8,%ymm10,%ymm10 // * m
	lack_func_shift_right $18,%ymm10,%ymm10  // >> 18
	lack_func paddd,%ymm4,%ymm10,%ymm10 // - 15817

	lack_func pmulld,%ymm8,%ymm10,%ymm10 // * m

	lack_func_shift_right $14,%ymm10,%ymm10  // >> 14
	lack_func paddd,%ymm5,%ymm10,%ymm10 // - 80470
	lack_func pmulld,%ymm8,%ymm10,%ymm10 // * m
	lack_func_shift_right $11,%ymm10,%ymm10  // >> 11

	lack_func paddd,%ymm9,%ymm10,%ymm10 // reinterpreter.i -accumlator

	// input value가 88.72283보다 크거나 -87.33654 보다 작으면 올바르게 작동하지 않는다. 해당 케이스에서는 별도로 직접 데이터를 기입한다.
	// ymm0~4 까지를 임시로 사용한다.

	sub $(32*6),%rsp
	vmovups %ymm0,(%rsp)
	vmovups %ymm1,32(%rsp)
	vmovups %ymm2,64(%rsp)
	vmovups %ymm3,96(%rsp)
	vmovups %ymm4,128(%rsp)
	push %rax

	lea exp_avx_data2_1(%rip),%rax

	// check upper value
	vbroadcastss (%rax),%ymm0 // ymm0 :88.72283
	vbroadcastss 4(%rax),%ymm1 // ymm1 :0x7f800000
	vcmpps $0x02,%ymm0,%ymm7,%ymm2 // VCMPLEPS
	vandps %ymm10,%ymm2,%ymm3
	vcmpps $0x0e,%ymm0,%ymm7,%ymm2 // VCMPGTPS
	vandps %ymm1,%ymm2,%ymm4
	vaddps %ymm3,%ymm4,%ymm10

	// check lower value
	vbroadcastss 8(%rax),%ymm0 // ymm0 :-87.33654
	vcmpps $0x0d,%ymm0,%ymm7,%ymm2 // VCMPGEPS
	vandps %ymm10,%ymm2,%ymm10

	pop %rax
	vmovups (%rsp),%ymm0
	vmovups 32(%rsp),%ymm1
	vmovups 64(%rsp),%ymm2
	vmovups 96(%rsp),%ymm3
	vmovups 128(%rsp),%ymm4
	add $(32*6),%rsp

	ret

// access byte offset.
exp_avx_data2:
	.float 12102203.0
	.long 127*(1 << 23)
	.long 3537
	.long 13668
	.long 15817
	.long -80470
	.long 0xffff
	.float 1.0
	.float -1.0
exp_avx_data2_1:
	.float 88.72283
	.long 0x7f800000
	.float -87.33654

.globl add_dword_avx
.type add_dword_avx @function
add_dword_avx:
	push %rbp

	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1
	vmovaps %ymm0,%ymm11
	vmovaps %ymm1,%ymm12
	call add_dword_avx_actual
	/*
	vmovaps %ymm1,%ymm2
	paddd %xmm0,%xmm2
	vperm2f128 $0x81,%ymm0,%ymm0,%ymm0
	vperm2f128 $0x81,%ymm1,%ymm1,%ymm1
	paddd %xmm0,%xmm1
	vperm2f128 $0x20,%ymm1,%ymm2,%ymm2
	*/
	vmovaps %ymm13,(%rdx)

	pop %rbp
ret

.globl sub_dword_avx
.type sub_dword_avx @function
sub_dword_avx:
	push %rbp

	vmovaps (%rdi),%ymm11
	vmovaps (%rsi),%ymm12

	call sub_dword_avx_actual
	/*
	vmovaps %ymm1,%ymm2
	psubd %xmm0,%xmm2
	vperm2f128 $0x81,%ymm0,%ymm0,%ymm0
	vperm2f128 $0x81,%ymm1,%ymm1,%ymm1
	psubd %xmm0,%xmm1
	vperm2f128 $0x20,%ymm1,%ymm2,%ymm2
	*/
	vmovaps %ymm13,(%rdx)

	pop %rbp
ret

.globl log_avx
.type log_avx @function
log_avx:

	push %rbp

	lea log_avx_const(%rip),%rax
	vbroadcastss (%rax),%ymm0 //-0x3f2aaaab
	vbroadcastss 4(%rax),%ymm1 //0xff800000
	vbroadcastss 8(%rax),%ymm2 //1.19209290e-7
	vbroadcastss 12(%rax),%ymm3 //-1.0
	vbroadcastss 16(%rax),%ymm4 //0.230836749
	vbroadcastss 20(%rax),%ymm5 //-0.279208571
	vbroadcastss 24(%rax),%ymm6 //0.331826031
	vbroadcastss 28(%rax),%ymm7 //-0.498910338
	vbroadcastss 32(%rax),%ymm8 //0.693147182

//rdi : float *v0 : data
//rsi : float *v1 : output
//rdx : int count, number of line

log_avx_loop:
	cmp $0,%rdx
	jle log_avx_end

	vmovaps (%rdi),%ymm9 // ymm9 : a

	lack_func paddd,%ymm9,%ymm0,%ymm10
	lack_func pand,%ymm10,%ymm1,%ymm14 // ymm14 : e
	lack_func psubd,%ymm14,%ymm9,%ymm15 //ymm15: m
	VCVTDQ2PS %ymm14,%ymm14
	vmulps %ymm14,%ymm2,%ymm0 //ymm0 : i
	vaddps %ymm15,%ymm3,%ymm9 //ymm9 : f
	vmulps %ymm9,%ymm9,%ymm14 //ymm14 : s

	vmulps %ymm4,%ymm9,%ymm10
	vaddps %ymm10,%ymm5,%ymm15 //ymm15 : r
	vmulps %ymm6,%ymm9,%ymm10
	vaddps %ymm10,%ymm7,%ymm1 //ymm1 : t
	vmulps %ymm15,%ymm14,%ymm10
	vaddps %ymm10,%ymm1,%ymm15 //ymm15 : r
	vmulps %ymm15,%ymm14,%ymm10
	vaddps %ymm10,%ymm9,%ymm15 //ymm15 : r
	vmulps %ymm0,%ymm8,%ymm10
	vaddps %ymm10,%ymm15,%ymm15 //ymm15 : r

	vmovaps %ymm15,(%rsi)
	add $32,%rdi
	add $32,%rsi
	vbroadcastss (%rax),%ymm0 //-0x3f2aaaab
	vbroadcastss 4(%rax),%ymm1 //0xff800000

	dec %rdx
	jmp log_avx_loop

log_avx_end:

	pop %rbp
	ret

log_avx_const:
	.long -0x3f2aaaab
	.long 0xff800000
	.float 1.19209290e-7
	.float -1.0
	.float 0.230836749
	.float -0.279208571
	.float 0.331826031
	.float -0.498910338
	.float 0.693147182
	.float 0.00000001


// it uses %ymm11,%ymm12, %ymm13
.macro horizontal_adding_all input,output
	vmovaps \input,%ymm11
	vxorps %ymm12,%ymm12,%ymm12
	vhaddps %ymm12,%ymm11,%ymm11
	vhaddps %ymm12,%ymm11,%ymm11
	vperm2f128 $0x81,%ymm11,%ymm11,%ymm12
	vperm2f128 $0x80,%ymm11,%ymm11,%ymm11
	vaddps %ymm11,%ymm12,\output
.endm

/* ------------------------------------------------------------------------------------*/
.globl cross_entropy_error
.type cross_entropy_error @function
cross_entropy_error:

	push %rbp

	lea cross_entropy_error_const(%rip),%rax
	vbroadcastss (%rax),%ymm0 //-0x3f2aaaab
	vbroadcastss 4(%rax),%ymm1 //0xff800000
	vbroadcastss 8(%rax),%ymm2 //1.19209290e-7
	vbroadcastss 12(%rax),%ymm3 //-1.0
	vbroadcastss 16(%rax),%ymm4 //0.230836749
	vbroadcastss 20(%rax),%ymm5 //-0.279208571
	vbroadcastss 24(%rax),%ymm6 //0.331826031
	vbroadcastss 28(%rax),%ymm7 //-0.498910338

//rdi : float *v0 : answer
//rsi : float *v1 : rightanswer
//rdx : int count, number of line
//ymm0 : return of resul

	vxorps %ymm8,%ymm8,%ymm8 // ymm8 : sum of result.
cross_entropy_error_loop:
	cmp $0,%rdx
	jle cross_entropy_error_end

	vbroadcastss 36(%rax),%ymm10 //0.00000001
	vmovaps (%rdi),%ymm9 // ymm9 : a
	vaddps %ymm9,%ymm10,%ymm9

// below for log(x), x:ymm9
	//restore for constan
	vbroadcastss (%rax),%ymm0 //-0x3f2aaaab
	vbroadcastss 4(%rax),%ymm1 //0xff800000

	lack_func paddd,%ymm9,%ymm0,%ymm10
	lack_func pand,%ymm10,%ymm1,%ymm14 // ymm14 : e
	lack_func psubd,%ymm14,%ymm9,%ymm15 //ymm15: m
	VCVTDQ2PS %ymm14,%ymm14
	vmulps %ymm14,%ymm2,%ymm0 //ymm0 : i
	vaddps %ymm15,%ymm3,%ymm9 //ymm9 : f
	vmulps %ymm9,%ymm9,%ymm14 //ymm14 : s

	vmulps %ymm4,%ymm9,%ymm10
	vaddps %ymm10,%ymm5,%ymm15 //ymm15 : r
	vmulps %ymm6,%ymm9,%ymm10
	vaddps %ymm10,%ymm7,%ymm1 //ymm1 : t
	vmulps %ymm15,%ymm14,%ymm10
	vaddps %ymm10,%ymm1,%ymm15 //ymm15 : r
	vmulps %ymm15,%ymm14,%ymm10
	vaddps %ymm10,%ymm9,%ymm15 //ymm15 : r
	vbroadcastss 32(%rax),%ymm10 //0.693147182
	vmulps %ymm0,%ymm10,%ymm10
	vaddps %ymm10,%ymm15,%ymm15 //ymm15 : r
	//ymm15 has log(x)

	vmovaps (%rsi),%ymm14 // ymm14 : right answer
	vmulps %ymm15,%ymm14,%ymm10
	horizontal_adding_all %ymm10, %ymm10
	vaddps %ymm10,%ymm8,%ymm8

	add $32,%rdi
	add $32,%rsi
	dec %rdx
	jmp cross_entropy_error_loop

cross_entropy_error_end:
	vmulps %ymm8,%ymm3,%ymm0
	pop %rbp
	ret

cross_entropy_error_const:
	.long -0x3f2aaaab
	.long 0xff800000
	.float 1.19209290e-7
	.float -1.0
	.float 0.230836749
	.float -0.279208571
	.float 0.331826031
	.float -0.498910338
	.float 0.693147182
	.float 0.00000001
/* ------------------------------------------------------------------------------------*/


/* ------------------------------------------------------------------------------------*/
.globl get_max_avx
.type get_max_avx @function
get_max_avx:

	push %rbp

//rdi : float *v0 : answer
//rsi : int count, number of line
//ymm0 : return of resul

	vmovaps (%rdi),%ymm8 // ymm8 : sum of result.
get_max_avx_loop:
	cmp $0,%rsi
	jle get_max_avx_loop_end

	vmovaps (%rdi),%ymm9
	vmaxps %ymm9,%ymm8,%ymm8

	add $32,%rdi
	dec %rsi
	jmp get_max_avx_loop

get_max_avx_loop_end:

	vperm2f128 $0x81,%ymm8,%ymm8,%ymm9
	vperm2f128 $0x80,%ymm8,%ymm8,%ymm8
	vmaxps %ymm9,%ymm8,%ymm8
	vpermilps $0,%ymm8,%ymm0
	vpermilps $1,%ymm8,%ymm1
	vpermilps $2,%ymm8,%ymm2
	vpermilps $3,%ymm8,%ymm3
	vmaxps %ymm0,%ymm1,%ymm0
	vmaxps %ymm0,%ymm2,%ymm0
	vmaxps %ymm0,%ymm3,%ymm0
	vxorps %ymm1,%ymm1,%ymm1
	vblendps $1,%ymm0,%ymm1,%ymm0

	pop %rbp
	ret

/* ------------------------------------------------------------------------------------*/



/* ------------------------------------------------------------------------------------*/
.globl sub_avx
.type sub_avx @function
sub_avx:

	push %rbp

//rdi : float *v0 : input A
//rsi : float *v1 : input B
//rdx : float *v2 : output C = A - B
//rcx : count

sub_avx_loop:
	cmp $0,%rcx
	jle sub_avx_loop_end

	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1

	vsubps %ymm0,%ymm1,%ymm2

	vmovaps %ymm2,(%rdx)

	add $32,%rdi
	add $32,%rsi
	add $32,%rdx
	dec %rcx
	jmp sub_avx_loop

sub_avx_loop_end:

	pop %rbp
	ret

/* ------------------------------------------------------------------------------------*/

/* ------------------------------------------------------------------------------------*/
.globl sub_single_avx
.type sub_single_avx @function
sub_single_avx:

	push %rbp

//rdi : float *v0 : input A
//rsi : float *v1 : scale B !!!
//rdx : float *v2 : output C = A - B
//rcx : count

	vbroadcastss (%rsi),%ymm1

sub_single_avx_loop:
	cmp $0,%rcx
	jle sub_single_avx_loop_end

	vmovaps (%rdi),%ymm0

	vsubps %ymm1,%ymm0,%ymm2

	vmovaps %ymm2,(%rdx)

	add $32,%rdi
	add $32,%rdx
	dec %rcx
	jmp sub_single_avx_loop

sub_single_avx_loop_end:

	pop %rbp
	ret

/* ------------------------------------------------------------------------------------*/

/* ------------------------------------------------------------------------------------*/
.globl sum_avx
.type sum_avx @function
sum_avx:

	push %rbp

//rdi : float *v0 : input A
//rsi : count
// xmm0 : return
	vxorps %ymm1,%ymm1,%ymm1
sum_avx_loop:
	cmp $0,%rsi
	jle sum_avx_loop_end

	vmovaps (%rdi),%ymm0
	vaddps %ymm0,%ymm1,%ymm1

	add $32,%rdi
	dec %rsi
	jmp sum_avx_loop

sum_avx_loop_end:
	horizontal_adding_all %ymm1,%ymm0

	pop %rbp
	ret

/* ------------------------------------------------------------------------------------*/

//아래 div_single_avx는 테스트 하지 않았음. 버그가 있을 확률이 높음.
/* ------------------------------------------------------------------------------------*/
.globl div_single_avx
.type div_single_avx @function
div_single_avx:

	push %rbp

//rdi : float *v0 : input A
//rsi : float *v1 : scale B !!!
//rdx : float *v2 : output C = A / B
//rcx : count

	vbroadcastss (%rsi),%ymm1

div_single_avx_loop:
	cmp $0,%rcx
	jle div_single_avx_loop_end

	vmovaps (%rdi),%ymm0

	vdivps %ymm1,%ymm0,%ymm2

	vmovaps %ymm2,(%rdx)

	add $32,%rdi
	add $32,%rdx
	dec %rcx
	jmp div_single_avx_loop

div_single_avx_loop_end:

	pop %rbp
	ret

/* ------------------------------------------------------------------------------------*/

.globl v_add_s_avx
.type v_add_s_avx @function
v_add_s_avx:

push %rbp

//rdi : float *v0 input
//rsi : float *v1 output
//rdx : int count, number of width
//rcx : address of scale data.
	//call print_reg
	vbroadcastss (%rcx),%ymm1
v_add_s_avx_loop:
	cmp $0,%rdx
	jle v_add_s_avx_end
	vmovaps (%rdi),%ymm0
	vaddps %ymm0, %ymm1, %ymm2
	vmovaps %ymm2, (%rsi)
	sub $1,%rdx
	add $32,%rdi
	add $32,%rsi
	jmp v_add_s_avx_loop
v_add_s_avx_end:
	pop %rbp
	ret
/* ------------------------------------------------------------------------------------*/
//원소끼리 곱하기
.globl v_mmult_v_avx
.type v_mmult_v_avx @function
v_mmult_v_avx:

push %rbp

//rdi : float *v0 input
//rsi : float *v1 input
//rdx : float *v2 output
//rcx : int count, number of width

v_mmult_v_avx_loop:
	cmp $0,%rcx
	jle v_mmult_v_avx_end
	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1
	vmulps %ymm0, %ymm1, %ymm2
	vmovaps %ymm2,(%rdx)
	sub $1,%rcx
	add $32,%rdi
	add $32,%rsi
	add $32,%rdx
	jmp v_mmult_v_avx_loop
v_mmult_v_avx_end:

	pop %rbp
	ret
/* ------------------------------------------------------------------------------------*/
/* ------------------------------------------------------------------------------------*/
//원소끼리 나누기
.globl v_mdiv_v_avx
.type v_mdiv_v_avx @function
v_mdiv_v_avx:

push %rbp

//rdi : float *v0 input
//rsi : float *v1 input
//rdx : float *v2 output
//rcx : int count, number of width

v_mdiv_v_avx_loop:
	cmp $0,%rcx
	jle v_mdiv_v_avx_end
	vmovaps (%rdi),%ymm0
	vmovaps (%rsi),%ymm1
	vdivps %ymm1, %ymm0, %ymm2
	vmovaps %ymm2,(%rdx)
	sub $1,%rcx
	add $32,%rdi
	add $32,%rsi
	add $32,%rdx
	jmp v_mdiv_v_avx_loop
v_mdiv_v_avx_end:

	pop %rbp
	ret
/* ------------------------------------------------------------------------------------*/
