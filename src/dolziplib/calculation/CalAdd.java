package dolziplib.calculation;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalAdd implements Cal{

	Matrix input0;
	Matrix input1;
	Matrix output;
	Matrix backInput = null;
	Matrix backOutput0 = null;
	Matrix backOutput1 = null;
	
	
	MatrixOperator op;
	
	String name;
	long duration=0;
	
	public CalAdd(Matrix i0,Matrix i1, Matrix out0, MatrixOperator op, String name)
	{
		this.input0 = i0;
		this.input1 = i1;
		this.output = out0;
		this.op = op;
		this.name = name;		
	}
	
	public void setBackPropagation(Matrix output0,Matrix output1,Matrix input)
	{
		this.backOutput0 = output0;
		this.backOutput1 = output1;
		this.backInput = input;
	}
		
	@Override
	public long doCal(boolean measureDuration) throws Exception{
		// TODO Auto-generated method stub
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		op.add(input0, input1, output);
		if(measureDuration)
		{
			long endTime = System.nanoTime();
			this.duration = endTime-startTime;
			return endTime - startTime;
		}
		else return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public long getDuration() {
		// TODO Auto-generated method stub
		return this.duration;
	}

	private void doBackSameHeight(Matrix backInput, Matrix backOutput) throws Exception
	{
		boolean fastBack = true;
		if(fastBack)
		{
			MatrixOperator op = backInput.getMatrixOperator();
			op.mult(backInput, 1.0, backOutput);
		}
		else
		{
			for(int i=0;i<backInput.getHeight();i++)
			{
				for(int j=0;j<backInput.getWidth();j++)
				{
					backOutput.setData(this.backInput.getData(i,j), i, j);				
				}
			}
		}		
	}
	private void doBackSameDiff(Matrix backInput, Matrix backOutput) throws Exception
	{
		double[] tmp = new double[backInput.getWidth()];
		for(int i=0;i<tmp.length;i++)tmp[i]=0.0;
		for(int i=0;i<backInput.getHeight();i++)
		{
			for(int j=0;j<backInput.getWidth();j++)
			{
				tmp[j] += this.backInput.getData(i,j);
			}
		}		
		for(int j=0;j<backOutput.getWidth();j++)
		{
			backOutput.setData(tmp, 0);
		}

	}
	
	
	@Override
	public long doBack(boolean measureDuration) throws Exception {
		// TODO Auto-generated method stub
				
		if(this.backInput == null || this.backOutput0 == null || this.backOutput1 == null)return 0;
			
		long startT = 0;
		if(measureDuration)startT = System.nanoTime();

		
		if(backOutput0.getHeight()==this.backInput.getHeight())doBackSameHeight(this.backInput,this.backOutput0);
		else doBackSameDiff(this.backInput,this.backOutput0);
		if(backOutput1.getHeight()==this.backInput.getHeight())doBackSameHeight(this.backInput,this.backOutput1);
		else doBackSameDiff(this.backInput,this.backOutput1);
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" add back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
}
