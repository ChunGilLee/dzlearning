package dolziplib.calculation;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixAVX;
import dolziplib.matrix.MatrixOperator;

public class CalDot implements Cal{

	public Matrix input0;
	public Matrix input1;
	public Matrix output;
	Matrix backInput;
	Matrix backOutput0;
	Matrix backOutput1;
	
	Matrix input1Trans = null;
	Matrix input0Trans = null;
	
	MatrixOperator op;
	String name;
	
	long duration = 0;
	
	public CalDot(Matrix i0,Matrix i1, Matrix out0, MatrixOperator op,String name)
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
	public long doCal(boolean measureDuration)  throws Exception{
		// TODO Auto-generated method stub
		
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		op.dot(input0, input1, output);
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
		
		//dot product의 back propagation은 출력층의 최종단이 scalar일 경우에만
		//아래와 같이 전치행렬dot반대편입력matrix로 된다.
		//특히 가장 마지막 출력단이 Softmax CEE일때만 올바른 결과를 보여준다.
		//마지막 출력단 까지 가는 여정 중에 지금까지 구현되지 않은 새로운 layer가 만들어져서 추가되면
		//본 dot 연산의 back propagation도 다시 검증을 해야 됨을 유의하자.
				
		long startT = 0;
		if(measureDuration)startT = System.nanoTime();

		if(this.backInput == null)return 0;
		MatrixOperator op = this.backInput.getMatrixOperator();
		//이미 만들어 놓은 matrix를 사용하면, matrix 생성시 zero clear time을 없앨수 있다.
		if(input0Trans == null && this.backOutput1 != null)
		{ //input0Trans 는 backoutput1을 위해 사용되고, input1Trans는 backoutpu0를 위해 사용된다.
			input0Trans = Matrix.create(this.input0.getWidth(), this.input0.getHeight(), this.input0.getMatrixType());
		}
		if(input1Trans == null && this.backOutput0 != null)
		{
			input1Trans = Matrix.create(this.input1.getWidth(), this.input1.getHeight(), this.input1.getMatrixType());
		}
		
		if(this.backOutput0 != null)op.transpose(this.input1, input1Trans);
		if(this.backOutput1 != null)op.transpose(this.input0, input0Trans);
		if(measureDuration)
		{
			long endT = System.nanoTime();
			System.out.println(this.getName()+" transpose time:"+(endT-startT)/1000 +" us");
		}

		if(this.backOutput0 != null)
		{
			if(measureDuration && backInput instanceof MatrixAVX)
			{
				MatrixAVX backInputT = (MatrixAVX)this.backInput;
				MatrixAVX input1TransT = (MatrixAVX)this.input1Trans;
				MatrixAVX backOutput0T = (MatrixAVX)this.backOutput0;
				
				System.out.printf("%s first %b(%d,%d) %b(%d,%d) %b(%d,%d) \n",this.getName(),
						backInputT.isFlipped(),backInputT.getHeight(),backInputT.getWidth(),
						input1TransT.isFlipped(),input1TransT.getHeight(),input1TransT.getWidth(),
						backOutput0T.isFlipped(),backOutput0T.getHeight(),backOutput0T.getWidth());

			}
			op.dot(this.backInput, input1Trans, this.backOutput0);
		}
		if(this.backOutput1 != null)
		{
			
			//System.out.println("backInput:"+this.backInput);
			//System.out.println("backOuput1:"+this.backOutput1);
			//System.out.println("input0Trans"+this.input0Trans);
			
			if(measureDuration && backInput instanceof MatrixAVX)
			{
				MatrixAVX backInputT = (MatrixAVX)this.backInput;
				MatrixAVX input0TransT = (MatrixAVX)this.input0Trans;
				MatrixAVX backOutput1T = (MatrixAVX)this.backOutput1;
				
				//input0Trans.saveToBinFile("test_normal_input0.bin");
				//backInput.saveToBinFile("test_normal_input1.bin");
				
				
				System.out.printf("%s second %b(%d,%d) %b(%d,%d) %b(%d,%d) \n",this.getName(),
						backInputT.isFlipped(),backInputT.getHeight(),backInputT.getWidth(),
						input0TransT.isFlipped(),input0TransT.getHeight(),input0TransT.getWidth(),
						backOutput1T.isFlipped(),backOutput1T.getHeight(),backOutput1T.getWidth());

			}
			op.dot(input0Trans, this.backInput, this.backOutput1);
		}
				
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf("%s dot back:%d us, input(%d,%d), output0(%d,%d), output1(%d,%d)\n",
					this.getName(),(endT-startT)/1000,
					backInput.getHeight(),backInput.getWidth(),
					backOutput0==null? 0:backOutput0.getHeight(),backOutput0==null? 0:backOutput0.getWidth(),
					backOutput1==null? 0:backOutput1.getHeight(),backOutput1==null? 0:backOutput1.getWidth()
					);
		}
		
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(input1Trans!=null)input1Trans.release();
		if(input0Trans!=null)input0Trans.release();
	}
}
