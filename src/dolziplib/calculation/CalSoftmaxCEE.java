package dolziplib.calculation;

import dolziplib.DDouble;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalSoftmaxCEE implements Cal{

	public Matrix input0;
	public Matrix outputSoftmax;
	Matrix rightAnswer;
	DDouble output;
	Matrix backOutput = null;
	
	
	MatrixOperator op;
	
	String name;
	
	long duration = 0;
	
	public double getOutputValue()
	{
		return output.value;
	}
	
	public CalSoftmaxCEE(Matrix i0, Matrix out0, Matrix rightAnswer, DDouble output, MatrixOperator op,String name)
	{
		this.input0 = i0;
		this.outputSoftmax = out0;
		this.op = op;
		this.name = name;
		this.output = output;
		this.rightAnswer = rightAnswer;		
	}
	public void setBackPropagation(Matrix output)
	{
		this.backOutput = output;
	}
	
	@Override
	public long doCal(boolean measureDuration)  throws Exception{
		// TODO Auto-generated method stub
		
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		op.getSoftMax(input0, outputSoftmax);
		op.crossEntropyError(outputSoftmax, rightAnswer, output);
		if(measureDuration)
		{
			long endTime = System.nanoTime();
			this.duration = endTime - startTime;
			return endTime - startTime;
		}
		else return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public long getDuration() {
		// TODO Auto-generated method stub
		return this.duration;
	}

	@Override
	public long doBack(boolean measureDuration) throws Exception {
		// TODO Auto-generated method stub
		if(backOutput!=null)
		{
			
			//아래 back propagation은 answer data가 one-hot-encoding일 경우에만 성립한다.
			//one-hot-encoding이 아닐 경우에는, 각각의 row에서 해당 answer data의 row의 합산값으로 나눠줘야 한다(검증하지 않았음.).
			long startT = 0;
			if(measureDuration)startT = System.nanoTime();
			
			boolean fastBack = true;
			if(fastBack)
			{
				Matrix tmp = rightAnswer.copy();
				MatrixOperator op = tmp.getMatrixOperator();
				op.mult(tmp, -1.0, tmp);
				op.add(tmp, outputSoftmax, tmp);
				double h = 1.0/(double)rightAnswer.getHeight();
				op.mult(tmp, h, backOutput);
				tmp.release();
			}
			else
			{	
				
				for(int i=0;i<backOutput.getHeight();i++)
				{
					for(int j=0;j<backOutput.getWidth();j++)
					{
						double v = (outputSoftmax.getData(i, j) - rightAnswer.getData(i, j))/(double)rightAnswer.getHeight();
						
						
						backOutput.setData(v, i, j);
					}
				}
				
			}
			
			if(measureDuration) {
				long endT = System.nanoTime();
				System.out.printf(" cee back:%d\n",(endT-startT)/1000);
			}
		}
		return 0;
	}
	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
}
