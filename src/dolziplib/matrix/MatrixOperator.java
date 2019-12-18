package dolziplib.matrix;

import dolziplib.DDouble;
import dolziplib.DZHelper;
import dolziplib.DZMath;

public abstract class MatrixOperator {
	
	protected void checkReleased(Matrix a,String name)
	{
		if(a.isReleased())throw new RuntimeException(name+" was released.");
	}
	
	public void dot(Matrix a, Matrix b, Matrix out) throws Exception {
		
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(out, "out");
		
		// TODO Auto-generated method stub
		if(a.getWidth() != b.getHeight())
		{
			String msg = String.format("a.W != b.H a(%d,%d) b(%d,%d) c(%d,%d)", 
					a.getHeight(), a.getWidth(),
					b.getHeight(), b.getWidth(),
					out.getHeight(), out.getWidth());
			throw new Exception(msg);
		}
		if(a.getHeight() != out.getHeight())
		{
			String msg = String.format("a.H != out.H a(%d,%d) b(%d,%d) c(%d,%d)", 
					a.getHeight(), a.getWidth(),
					b.getHeight(), b.getWidth(),
					out.getHeight(), out.getWidth());
			throw new Exception(msg);
		}
		if(b.getWidth() != out.getWidth())
		{
			String msg = String.format("b.W != out.W a(%d,%d) b(%d,%d) c(%d,%d)", 
					a.getHeight(), a.getWidth(),
					b.getHeight(), b.getWidth(),
					out.getHeight(), out.getWidth());
			throw new Exception(msg);
		}
				
		for(int i=0;i<a.getHeight();i++)
		{
			for(int k=0;k<b.getWidth();k++)
			{
				double sum = 0;
				for(int j=0;j<a.getWidth();j++)
				{
					sum += a.getData(i, j) * b.getData(j, k);
				}
				out.setData(sum, i, k);
			}
		}
	}
	
	public void pow(Matrix a, Matrix b, Matrix out)
	{
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(out, "out");
		
		try {
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					out.setData(Math.pow(a.getData(i, j), b.getData(i, j)),i,j);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void pow(Matrix a, double b,  Matrix out)
	{
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		try {
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					out.setData(Math.pow(a.getData(i, j), b),i,j);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(Matrix a, double b ,Matrix out)
	{
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		try {
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					out.setData(a.getData(i, j) + b, i, j);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getSoftMax(Matrix a,Matrix out) throws Exception{
		// TODO Auto-generated method stub
		
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		Matrix ret = out;
		
		int h = a.getHeight();
		for(int i=0;i<h;i++)
		{
			double[] row = a.getRow(i);
			double[] v = DZMath.getSoftMax(row);
			ret.setData(v, i);
		}
	}
	
	public void getSigmoid(Matrix a,Matrix out) {
		// TODO Auto-generated method stub
		
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		try {
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					out.setData(DZMath.sigmoid(a.getData(i, j)), i, j);
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}		
	}
	
	public Matrix get1DMatrix(Matrix a) {
		// TODO Auto-generated method stub
		
		checkReleased(a, "a");
		
		try {
			Matrix newOne = Matrix.create(1, a.getHeight()*a.getWidth(), a.getMatrixType()); 
			double data[][] = a.getAllData();
			int index=0;
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					newOne.setData(data[i][j], 0, index);
					index++;
				}
			}
			return newOne;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public void mult(Matrix a, double b , Matrix out)
	{
		
		checkReleased(a, "a");
		checkReleased(out, "out");

		try {
			
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)out.setData(b*a.getData(i, j), i, j);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//원소끼리 꼽하기
	public void mult(Matrix a, Matrix b, Matrix out)  throws Exception
	{
		
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(out, "out");

		try {
			
			int h = a.getHeight();
			if(h==1 && b.getHeight()!=1) h = b.getHeight();
			boolean aHeightFixed = false;
			if(a.getHeight()==1)aHeightFixed=true;
			boolean bHeightFixed = false;
			if(b.getHeight()==1)bHeightFixed=true;
						
			int ai=0;
			int bi=0;
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					out.setData(a.getData(ai, j)*b.getData(bi, j), i, j);
				}
				if(aHeightFixed==false)ai++;
				if(bHeightFixed==false)bi++;
			}
		}catch (Exception e) {
			// TODO: handle exception
			
			System.out.printf("a(%d,%d) b(%d,%d) c(%d,%d)\n",a.getHeight(),a.getWidth(),b.getHeight(),b.getWidth(),out.getHeight(),out.getWidth());
			
			e.printStackTrace();
		}
	}
	
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
		
		for(int i=0;i<answers.getHeight();i++)
		{
			for(int j=0;j<answers.getWidth();j++)
			{
				double a = answers.getData(i, j);
				double r = rightAnswers.getData(i, j);
				output.setData((a-r)*(a-r),i,j);
			}
		}
	}
	
	public void crossEntropyError(Matrix answers, Matrix rightAnswers, DDouble output) throws Exception
	{
		checkReleased(answers, "answers");
		checkReleased(rightAnswers, "rightAnswers");
		
		MatrixOperator op = answers.getMatrixOperator();
		//System.out.println("aaaaaaaaaaaaaaaa:"+answers);
		Matrix ans1D = op.get1DMatrix(answers);
		//System.out.println("bbbbbbbbbbbbbbbb:"+ans1D);
		op = rightAnswers.getMatrixOperator();
		Matrix right1D = op.get1DMatrix(rightAnswers);
		
		int height = answers.getHeight();	
		double[] ans = ans1D.getRow(0);
		double[] right = right1D.getRow(0);
		//DZHelper.printArray("ans :",ans);
		//DZHelper.printArray("right :",right);
		double sum = DZMath.crossEntropyError(ans, right);
		output.value = sum/(double)height;
	}
	
	public double crossEntropyError(Matrix answers, Matrix rightAnswers) throws Exception
	{
		DDouble output = new DDouble();
		crossEntropyError(answers,rightAnswers,output);
		return output.value;
	}
	
	public void transpose(Matrix a, Matrix out) throws Exception
	{
		checkReleased(a, "a");
		checkReleased(out, "out");
		
		if(a.getHeight() != out.getWidth())throw new Exception("input height("+a.getHeight()+
				") != output width("+out.getWidth()+")");
		if(a.getWidth() != out.getHeight())throw new Exception("input width("+a.getWidth()+
				") != output height("+out.getHeight()+")");
		
		for(int i=0;i<out.getHeight();i++)
		{
			for(int j=0;j<out.getWidth();j++)
			{
				out.setData(a.getData(j, i), i, j);
			}
		}
	}
	
	public void sub(Matrix a,Matrix b, Matrix out)
	{
		this.add(a, b, out, true);
	}
	
	public void add(Matrix a, Matrix b, Matrix out)
	{
		this.add(a, b, out, false);
	}
	
	protected void add(Matrix aT, Matrix bT, Matrix cT, final boolean sub)
	{
		checkReleased(aT, "a");
		checkReleased(bT, "b");
		checkReleased(cT, "out");
		
		
		int h = aT.getHeight();
		if(h==1 && bT.getHeight()>1)h = bT.getHeight();
		boolean aFixed = (aT.getHeight()==1);
		boolean bFixed = (bT.getHeight()==1);
		
		if(h!=cT.getHeight())
		{
			throw new RuntimeException(String.format("height not match (%d,%d) + (%d,%d) = (%d,%d)",
					aT.getHeight(),aT.getWidth(),bT.getHeight(),bT.getWidth(),cT.getHeight(),cT.getWidth()));
		}
		
		if(aT.getWidth() != bT.getWidth() || bT.getWidth() != cT.getWidth())
		{
			throw new RuntimeException(String.format("width not match (%d,%d) + (%d,%d) = (%d,%d)",
					aT.getHeight(),aT.getWidth(),bT.getHeight(),bT.getWidth(),cT.getHeight(),cT.getWidth()));
		}
				
		if(sub)
		{
			int ai=0;
			int bi=0;
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<cT.getWidth();j++)
				{
					cT.setData(aT.getData(ai, j)-bT.getData(bi, j), i, j);
				}
				if(aFixed==false)ai++;
				if(bFixed==false)bi++;
			}
		}
		else
		{
			int ai=0;
			int bi=0;
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<cT.getWidth();j++)
				{
					cT.setData(aT.getData(ai, j)+bT.getData(bi, j), i, j);
				}
				if(aFixed==false)ai++;
				if(bFixed==false)bi++;
			}
		}
	}
	
	public void sum(Matrix input, Matrix output)  throws Exception
	{
		
		checkReleased(input, "input");
		checkReleased(output, "output");
		
		double sum = 0.0;
		for(int i=0;i<input.getHeight();i++)
		{
			sum = 0.0;
			for(int j=0;j<input.getWidth();j++)
			{
				sum += input.getData(i, j);
			}
			output.setData(sum, i, 0);
		}
	}
	
	public double sum(Matrix input)
	{
		checkReleased(input, "input");
		double sum = 0.0;
		for(int i=0;i<input.getHeight();i++)
		{
			for(int j=0;j<input.getWidth();j++)
			{
				sum += input.getData(i, j);
			}
		}
		return sum;
	}
	
	/** output = a / b
	 * @param a
	 * @param b
	 * @param output
	 */
	public void div(Matrix a, Matrix b, Matrix output)
	{
		
		checkReleased(a, "a");
		checkReleased(b, "b");
		checkReleased(output, "out");
		
		int h = a.getHeight();
		if(h==1 && b.getHeight()>1)h = b.getHeight();
		boolean aFixed = (a.getHeight()==1);
		boolean bFixed = (b.getHeight()==1);
		
		try {
			int aH = 0;
			int bH = 0;
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					output.setData(a.getData(aH, j)/b.getData(bH, j),i,j);
				}
				if(aFixed==false)aH++;
				if(bFixed==false)bH++;
			}
		}catch(Exception ex)
		{
			System.out.printf("input0 (%d,%d), input1 (%d,%d), output(%d,%d)\n",
					a.getHeight(),a.getWidth(),b.getHeight(),b.getWidth(),
					output.getHeight(), output.getWidth());
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
	}
}
