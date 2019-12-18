	.text


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

