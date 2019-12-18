package dolziplib.calculation;


import dolziplib.DDouble;
import dolziplib.DZHelper;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalMeanSquaredError implements Cal{

	Matrix sample;
	Matrix rightAnswer;
	Matrix backOutput;
	public Matrix outputMeanSquare = null;
	public Matrix output;
	DDouble outputV;
	
	MatrixOperator op;
	
	String name;
	long duration= 0;
	
	public CalMeanSquaredError(Matrix sample, Matrix output, Matrix rightAnswer, DDouble outputV,MatrixOperator op,String name)
	{
		this.sample = sample;
		this.rightAnswer = rightAnswer;
		this.outputV = outputV;
		this.op = op;
		this.name = name;
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
		if(outputMeanSquare==null)outputMeanSquare = output.copy(false);
		
		op.meanSquared(sample, rightAnswer, outputMeanSquare);
		outputV.value = op.sum(outputMeanSquare)/(2.0*outputMeanSquare.getHeight());
		op.add(sample, 0, output); //CalMeanSquared에서는 input값이 바로 예측한 값이 된다.
		
		//Matrix tmp = DZHelper.mergeToRight(sample, rightAnswer);
		//Matrix tmp2 = DZHelper.mergeToRight(tmp, outputMeanSquare);
		//System.out.println("mse "+output.value+" "+tmp2);
		//tmp2.release();
		//tmp.release();
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
				this.backOutput.setData((y-t)/this.backOutput.getHeight(),i,j);
			}
		}
		
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" mse back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(outputMeanSquare!=null)outputMeanSquare.release();
	}
}
