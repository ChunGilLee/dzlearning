package dolziplib.matrix;

import dolziplib.DDouble;

public class MatrixOperatorAVX extends MatrixOperator{

	static
	{
		System.load(System.getProperty("user.dir")+"/avx_clib.so");
	}
	
	private native int dot(long addrA, long addrB, long addrC);
	private native int add(long addrA, long addrB, long addrC);
	private native int sub(long addrA, long addrB, long addrC);
	private native int scale(long addrA, long addrB, float s);
	private native int addscalar(long addrA, long addrB, float s);
	private native int sigmoid(long addrA, long addrB);
	private native int CEE(ReturnParam retParam,long addrA, long addrB);
	private native int softmax(long addrA, long addrB);
	private native int mmult(long addrA, long addrB, long addrC); //원소끼리 꼽하기
	private native int transpose(long addrA, long addrB);
	private native int sum(long addrA, long addrB); // [n x m] input -> [n x 1] output
	private native int pow(long addrA, long addrB, long addrC);
	private native int mdiv(long addrA, long addrB, long addrC); //원소끼리 나누기
	private native int mpow(long addrA, long addrB, float s);
	private native int allsum(ReturnParam retParam,long addrA);
	
	@Override
	public void dot(Matrix aT, Matrix bT, Matrix cT) throws Exception {
		
		checkReleased(aT, "a");
		checkReleased(bT, "b");
		checkReleased(cT, "out");
		
		
		// TODO Auto-generated method stub
		if(aT.getWidth() != bT.getHeight())
		{
			throw new Exception("first matrix width is not matched to second height. first width:"+aT.getWidth()+", second height:"+bT.getHeight());
		}
		if(aT.getHeight() != cT.getHeight())
		{
			throw new Exception("first matrix height is not matched to third height. first height:"+aT.getHeight()+", second height:"+cT.getHeight());			
		}
		MatrixAVX a = (MatrixAVX)aT;
		MatrixAVX b = (MatrixAVX)bT;
		MatrixAVX c = (MatrixAVX)cT;
		
		b.flip();
		a.unflip();
		c.unflip();
		//System.out.println("ccc00:"+c);
		
		int ret = dot(a.avxAllocAddress,b.avxAllocAddress,c.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("avx error exception code:"+ret);						
		}
		
		//System.out.println("aaa:"+a);
		//System.out.println("bbb:"+b);
		//System.out.println("ccc11:"+c);

	}
	
	@Override
	protected void add(Matrix a, Matrix b, Matrix c, final boolean sub)
	{
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(c, "out");
		
		if(a.getWidth() != b.getWidth())
		{
			throw new RuntimeException("first matrix width("+a.getWidth()+
					") is not matched to second width("+b.getWidth()+")");				
		}
		
		MatrixAVX cT = (MatrixAVX)c;
		MatrixAVX aT = (MatrixAVX)a;
		MatrixAVX bT = (MatrixAVX)b;
		
		aT.unflip();
		bT.unflip();
		cT.unflip();
		
		if(sub)
		{
			int ret = sub(aT.avxAllocAddress,bT.avxAllocAddress,cT.avxAllocAddress);
			if(ret!=0)
			{
				throw new RuntimeException("avx sub exception code:"+ret);
			}			
		}
		else
		{
			int ret = add(aT.avxAllocAddress,bT.avxAllocAddress,cT.avxAllocAddress);
			if(ret!=0)
			{
				throw new RuntimeException("avx add exception code:"+ret);
			}
		}
	}	
	
	
	
	public void getSigmoid(Matrix input,Matrix output) {
		
		checkReleased(input, "input");
		checkReleased(output, "output");
		
		// TODO Auto-generated method stub
		if(input.getWidth() != output.getWidth() || input.getHeight() != output.getHeight())
		{
			System.out.println("sigmoid input and output w:h not matched.");
			for(int i=0;i<output.getHeight();i++)
			{
				for(int j=0;j<output.getWidth();j++)
				{
					try {
						output.setData(0.0, i, j);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return;
			}
		}
		
		MatrixAVX i0 = (MatrixAVX)input;
		MatrixAVX ret0 = (MatrixAVX)output;
		i0.unflip();
		ret0.unflip();
		
		sigmoid(i0.avxAllocAddress, ret0.avxAllocAddress);
		
	}
	
	@Override
	public void pow(Matrix a, Matrix b, Matrix out)
	{
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(out, "out");
		
		MatrixAVX at = (MatrixAVX)a;
		MatrixAVX bt = (MatrixAVX)b;
		MatrixAVX ct = (MatrixAVX)out;
		
		at.unflip();
		bt.unflip();
		ct.unflip();
		
		int ret = pow(at.avxAllocAddress,bt.avxAllocAddress,ct.avxAllocAddress);
		if(ret!=0) throw new RuntimeException("axv pow return:"+ret);
	}
	
	
	public static class ReturnParam
	{
		float retFloat = 0.0f;
		/* for test
		public int getAge() {
	        return 123;
	    }
	    */
		
		public void setFloat(float a)
		{
			retFloat = a;
		}
	}
	
	@Override
	public void crossEntropyError(Matrix answers, Matrix rightAnswers, DDouble output) throws Exception
	{
		
		checkReleased(answers, "answers");
		checkReleased(rightAnswers, "rightAnswers");
		
		ReturnParam param = new ReturnParam();
		MatrixAVX a = (MatrixAVX)answers;
		MatrixAVX b = (MatrixAVX)rightAnswers;
				
		int ret = this.CEE(param, a.avxAllocAddress,b.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("CEE error code:"+ret);
		}
		
		output.value = param.retFloat;
	}
	
	@Override
	public void meanSquared(Matrix answers, Matrix rightAnswers, Matrix output) throws Exception
	{
		checkReleased(answers, "answers");
		checkReleased(rightAnswers, "rightAnswers");
		
		if(answers.getHeight()!=rightAnswers.getHeight() || answers.getHeight()!=output.getHeight())
		{
			throw new RuntimeException(String.format("height not matched answers:%d rightAnswers:%d output:%d",
					answers.getHeight(), rightAnswers.getHeight(), output.getHeight()));
		}
		if(answers.getWidth()!=rightAnswers.getWidth() || answers.getWidth()!=output.getWidth())
		{
			throw new RuntimeException(String.format("width not matched answers:%d rightAnswers:%d output:%d",
					answers.getWidth(), rightAnswers.getWidth(), output.getWidth()));
		}
		
		this.sub(answers, rightAnswers, output);
		this.mult(output, output, output);
	}
	
	
	@Override
	public void getSoftMax(Matrix a,Matrix b) throws Exception{
		// TODO Auto-generated method stub
		
		checkReleased(a, "input");
		checkReleased(b, "output");
		
		MatrixAVX input = (MatrixAVX)a;
		MatrixAVX output = (MatrixAVX)b;
		
		int ret = this.softmax(input.avxAllocAddress, output.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("softmax error code:"+ret);
		}
	}
	
	@Override
	public void mult(Matrix a, double b, Matrix out)
	{
		
		checkReleased(a, "a");
		checkReleased(out, "output");
		
		MatrixAVX aT = (MatrixAVX)a;
		MatrixAVX outT = (MatrixAVX)out;
		
		aT.unflip();
		outT.unflip();
		
		int retCode = scale(aT.avxAllocAddress, outT.avxAllocAddress, (float)b);
		if(retCode!=0)
		{
			System.out.println("AVX scale return code:"+retCode);
		}
	}
	

	@Override
	public void mult(Matrix a, Matrix b, Matrix c) throws Exception
	{
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(c, "out");
		
		
		MatrixAVX cT = (MatrixAVX)c;
		MatrixAVX aT = (MatrixAVX)a;
		MatrixAVX bT = (MatrixAVX)b;
		
		aT.unflip();
		bT.unflip();
		cT.unflip();
		
		int ret = mmult(aT.avxAllocAddress,bT.avxAllocAddress,cT.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("avx add exception code:"+ret);
		}

	}
	
	@Override
	public void transpose(Matrix aT, Matrix outT) throws Exception
	{
		
		checkReleased(aT, "a");
		checkReleased(outT, "out");
		
		if(aT.getHeight() != outT.getWidth())throw new Exception("input height("+aT.getHeight()+
				") != output width("+outT.getWidth()+")");
		if(aT.getWidth() != outT.getHeight())throw new Exception("input width("+aT.getWidth()+
				") != output height("+outT.getHeight()+")");
		
		MatrixAVX a = (MatrixAVX)aT;
		MatrixAVX b = (MatrixAVX)outT;
		
		int ret = transpose(a.avxAllocAddress, b.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("avx transpose exception code:"+ret);
		}
	}
	
	@Override
	public void sum(Matrix input, Matrix output)  throws Exception
	{
		checkReleased(input, "a");
		checkReleased(output, "out");
		
		MatrixAVX a = (MatrixAVX)input;
		MatrixAVX b = (MatrixAVX)output;
		int ret = sum(a.avxAllocAddress,b.avxAllocAddress);
		if(ret!=0)
		{
			throw new Exception("avx sum exception code:"+ret);
		}
	}
	
	@Override
	public double sum(Matrix input)
	{
		checkReleased(input, "input");
		MatrixAVX a = (MatrixAVX)input;
		ReturnParam param = new ReturnParam();
		int ret = allsum(param,a.avxAllocAddress);
		if(ret!=0)
		{
			throw new RuntimeException("avx all sum exception code:"+ret);
		}
		return param.retFloat;
	}
	
	@Override
	public void div(Matrix input0, Matrix input1, Matrix output)
	{
		checkReleased(input0, "a");
		checkReleased(input1, "b");
		checkReleased(output, "out");
		
		MatrixAVX a = (MatrixAVX)input0;
		MatrixAVX b = (MatrixAVX)input1;
		MatrixAVX c = (MatrixAVX)output;
		
		int ret = mdiv(a.avxAllocAddress,b.avxAllocAddress,c.avxAllocAddress);
		if(ret!=0) new RuntimeException("avx div exception code:"+ret);
	}
	
	/*
	 현재 a 값에 음수가 있으면, 해당 값이 infinite가 되는 문제가 있다(복소수가 되므로..).
	 그런데, v값이 정수 일때도(복소수가 아닌데도), 음수가 있으면 infinite되는 문제가 있어, 우선, pow는 java코드를 사용하고, 추후 수정한다.
	@Override
	public void pow(Matrix a, double v, Matrix out)
	{
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		MatrixAVX aT = (MatrixAVX)a;
		MatrixAVX bT = (MatrixAVX)out;
		
		int ret = mpow(aT.avxAllocAddress, bT.avxAllocAddress, (float)v);
		if(ret!=0) new RuntimeException("avx pow(scalar) exception code:"+ret);
	}
	*/
}
