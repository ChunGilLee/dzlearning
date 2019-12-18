package dolziplib.calculation;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalReLU implements Cal{

	boolean leakyReLu = true;
	
	Matrix input0;
	Matrix output;
	Matrix inputBack = null;
	Matrix outputBack = null;
	
	MatrixOperator op;
	
	String name;
	long duration = 0;
	
	double avg = Double.NaN;
	double variance = Double.NaN;
	double gamma =1.0;
	double beta = 0.0;
	
	public CalReLU(Matrix i0, Matrix out0, MatrixOperator op,String name)
	{
		this.input0 = i0;
		this.output = out0;
		this.op = op;
		this.name =  name;
	}
	
	public void setBackPropagation(Matrix output,Matrix input)
	{
		this.outputBack = output;
		this.inputBack = input;
	}
	//특정 케이스에서, 특히 Neuron을 만들어 연결할때, setBackPropagation을 설정을 할때 output, input 메트릭스가 모두
	//준비 되지 않을 수 있다. 이때는 우선 위 함수로 ouput만 설정해 놓고, 추후에 input이 만들어 질때 아래의 함수로 input을 설정한다.
	public void setBackPropagation(Matrix input)
	{
		this.inputBack = input;
	}
	
	
	@Override
	public long doCal(boolean measureDuration)  throws Exception{
		// TODO Auto-generated method stub
		
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		
		for(int i=0;i<this.input0.getHeight();i++)
		{
			for(int j=0;j<this.input0.getWidth();j++)
			{
				double v = this.input0.getData(i, j);
				if(v>=0)this.output.setData(v, i, j);
				else 
				{
					if(leakyReLu)
					{
						this.output.setData(v*0.01, i, j);
					}
					else this.output.setData(0.0,i,j);
				}
			}
		}
		
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
		
		if(inputBack ==null || outputBack == null) return 0;
		
		long startT = 0;
		if(measureDuration)startT = System.nanoTime();

		for(int i=0;i<this.inputBack.getHeight();i++)
		{
			for(int j=0;j<this.inputBack.getWidth();j++)
			{
				double input = this.input0.getData(i, j);
				double v = this.inputBack.getData(i, j);
				if(input>=0)this.outputBack.setData(v, i, j);
				else 
				{
					if(leakyReLu)
					{
						this.outputBack.setData(0.01*v,i,j);
					}
					else this.outputBack.setData(0.0,i,j);
				}
			}
		}
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" sigmoid back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}
}
