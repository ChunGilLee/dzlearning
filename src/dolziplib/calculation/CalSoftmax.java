package dolziplib.calculation;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalSoftmax implements Cal{

	Matrix input0;
	Matrix output;
	
	MatrixOperator op;
	
	String name;
	
	long duration = 0;
	
	
	public CalSoftmax(Matrix i0, Matrix out0, MatrixOperator op,String name)
	{
		this.input0 = i0;
		this.output = out0;
		this.op = op;
		this.name = name;
	}
	
	@Override
	public long doCal(boolean measureDuration)  throws Exception{
		// TODO Auto-generated method stub
		
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		op.getSoftMax(input0, output);
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
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
}
