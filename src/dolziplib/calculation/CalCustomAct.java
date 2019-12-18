package dolziplib.calculation;

import dolziplib.DDouble;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalCustomAct implements Cal{

	Matrix input0;
	Matrix output0;
	
	MatrixOperator op;
	
	String name;
	
	long duration = 0;
	Matrix backOutput = null;
	Matrix rightAnswer;
	DDouble output;
	
	
	public CalCustomAct(Matrix i0, Matrix out0, Matrix rightAnswer, DDouble output,MatrixOperator op,String name)
	{
		this.input0 = i0;
		this.output0 = out0;
		this.op = op;
		this.name = name;
		this.rightAnswer = rightAnswer;
		this.output = output;
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
		op.add(input0, 0.0, output0);
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
		backOutput.setData(1.0);
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
}
