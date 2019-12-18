package dolziplib.calculation;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalSigmoid implements Cal{

	Matrix input0;
	Matrix output;
	Matrix inputBack = null;
	Matrix outputBack = null;
	Matrix tmp = null;
	
	MatrixOperator op;
	
	String name;
	long duration = 0;
	
	double avg = Double.NaN;
	double variance = Double.NaN;
	double gamma =1.0;
	double beta = 0.0;
	
	public CalSigmoid(Matrix i0, Matrix out0, MatrixOperator op,String name)
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
		
			op.getSigmoid(input0, output);
		
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

		if(tmp==null)tmp = this.output.copy(false);
		MatrixOperator op = tmp.getMatrixOperator();
		op.add(this.output, 0, tmp);
		op.mult(tmp, -1.0, tmp);
		op.add(tmp, 1.0, tmp);
		op.mult(tmp, output, tmp);
		op.mult(tmp, inputBack, outputBack);
		

		/*
		boolean fastBack = true;
		if(fastBack)
		{
		
			MatrixOperator op = tmp.getMatrixOperator();
			op.add(this.output, tmp, 0);
			op.multToA(tmp, -1.0);
			op.addToA(tmp, 1.0);
			op.mmult(tmp, output, tmp);
			op.mmult(tmp, inputBack, outputBack);
			

		}
		else
		{
			
			for(int i=0;i<this.inputBack.getHeight();i++)
			{
				for(int j=0;j<this.inputBack.getWidth();j++)
				{
					double v = inputBack.getData(i, j);
					double o = output.getData(i, j);
					double v2 = v * (1.0 - o) * o;
					outputBack.setData(v2, i, j);
				}
			}
			
		}
		*/
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" sigmoid back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(this.tmp!=null)
		{
			this.tmp.release();
			this.tmp = null;
		}
	}
}
