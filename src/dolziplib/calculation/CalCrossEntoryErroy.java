package dolziplib.calculation;


import dolziplib.DDouble;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalCrossEntoryErroy implements Cal{

	Matrix sample;
	Matrix rightAnswer;
	Matrix backOutput;
	DDouble output;
	
	MatrixOperator op;
	
	String name;
	long duration= 0;
	
	public CalCrossEntoryErroy(Matrix sample, Matrix rightAnswer, DDouble output,MatrixOperator op,String name)
	{
		this.sample = sample;
		this.rightAnswer = rightAnswer;
		this.output = output;
		this.op = op;
		this.name = name;
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
		op.crossEntropyError(sample, rightAnswer, output);
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
		
		long startT = 0;
		if(measureDuration)startT = System.nanoTime();
		
		for(int i=0;i<this.backOutput.getHeight();i++)
		{
			for(int j=0;j<this.backOutput.getWidth();j++)
			{
				double y = this.sample.getData(i, j);
				double t = this.rightAnswer.getData(i, j);
				this.backOutput.setData(-t/y/this.backOutput.getHeight(),i,j);
			}
		}
		
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" cee back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
}
