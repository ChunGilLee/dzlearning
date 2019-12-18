package mymltest;

import java.util.Calendar;
import java.util.Random;

import dolziplib.DZMath;
import dolziplib.ForwardFunction;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class TwoLayerNet{

	int matrixType = Matrix.MATRIX_TYPE_COLT;
	int inputSize;
	int hiddenSize;
	int outputSize;
	double weightInitStd;
	
	Matrix w1;
	Matrix w2;
	Matrix b1;
	Matrix b2;
	
	Random r = new Random(0);
	
	Matrix input;
	Matrix correct;
		
	private void fillRandomValue(Matrix w)
	{
		for(int i=0;i<w.getHeight();i++)
		{
			for(int j=0;j<w.getWidth();j++)
			{
				try {
					w.setData(r.nextDouble(), i, j);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public TwoLayerNet(Matrix input, Matrix correct,  int hiddenSize, double weightInitStd, int matrixType)
	{
		this.matrixType = matrixType;
		this.input = input;
		this.correct = correct;
		this.inputSize = input.getWidth();
		this.hiddenSize = hiddenSize;
		this.outputSize = correct.getWidth();
		this.weightInitStd = weightInitStd;
		
		w1 = Matrix.create(inputSize, hiddenSize, matrixType);
		w1.setData(0.5);
		//fillRandomValue(w1);
		//w1.getMatrixOperator().multToA(w1, weightInitStd);
		w2 = Matrix.create(hiddenSize, outputSize, matrixType);
		w2.setData(0.5);
		//fillRandomValue(w2);
		//w2.getMatrixOperator().multToA(w2, weightInitStd);
		
		b1 = Matrix.create(1, hiddenSize, matrixType);
		b1.setData(0.0);
		b2 = Matrix.create(1, outputSize, matrixType);
		b2.setData(0.0);	
	}
	
	private long showTimeMS(String name,long previousTick)
	{
		long current = System.nanoTime();
		System.out.printf("%s : %f us\n",name,(current-previousTick)/1000.0);
		current = System.nanoTime();
		return current;
	}
	public Matrix predict()
	{
		boolean showPerformance = true;
		try {
			//System.out.println("predict++");
			long current = System.nanoTime();
			long start = current;
			MatrixOperator op = input.getMatrixOperator();
			Matrix t1 = Matrix.create(input.getHeight(), w1.getWidth(), input.getMatrixType());
			op.dot(input, w1, t1);
			if(showPerformance)current = showTimeMS("dot w1", current);
			Matrix a1 = t1.copy(false);
			op.add(t1,b1,a1);
			if(showPerformance)current = showTimeMS("add b1", current);
			Matrix z1 = a1.copy(false);
			op.getSigmoid(a1,z1);
			if(showPerformance)current = showTimeMS("sigmoid z1", current);
			Matrix t2 = Matrix.create(z1.getHeight(), w2.getWidth(), z1.getMatrixType());
			op.dot(z1, w2, t2);
			if(showPerformance)current = showTimeMS("dot w2", current);
			Matrix a2 = t2.copy(false);
			op.add(t2,b2,a2);
			if(showPerformance)current = showTimeMS("add b2", current);
			Matrix result = a2.copy(false);
			op.getSoftMax(a2,result);
			if(showPerformance)current = showTimeMS("softmax a2", current);
			long end=current = System.nanoTime();;
			System.out.printf(" total time : %f us\n",(end-start)/1000.0);
			//System.out.println("predict--");
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public double getLoss() throws Exception
	{
		Matrix answer = this.predict();
		return this.getLoss(answer);
	}
	private double getLoss(Matrix answer) throws Exception
	{
		MatrixOperator op = answer.getMatrixOperator();
		return op.crossEntropyError(answer, correct);		
	}
	
	private int getMaxIndex(double[] data)
	{
		int index=-1;
		double max=-1;
		for(int i=0;i<data.length;i++)
		{
			if(index<0 || max<data[i])
			{
				index=i;
				max = data[i];
			}
		}
		return index;
	}
	public double accuracy()  throws Exception
	{
		Matrix answer = this.predict();
		return accuracy(answer);
	}
	private double accuracy(Matrix answer)  throws Exception
	{
		int height = answer.getHeight();
		int rightAnswerCount = 0;
		for(int i=0;i<height;i++)
		{
			double[] dataAnswer = answer.getRow(i);
			double[] dataCorrent = correct.getRow(i);
			int indexAnswer = getMaxIndex(dataAnswer);
			int indexCorrent = getMaxIndex(dataCorrent);
			if(indexAnswer<0 || indexCorrent<0)
			{
				throw new Exception("there is no answer");
			}
			if(indexAnswer == indexCorrent)rightAnswerCount++;
		}
		return ((double)rightAnswerCount)/((double)height);		
	}

	public double CEE = 0.0;
	public double accuracy = 0.0;
	public void doCurrentParam()
	{
		try {
			Matrix answer = this.predict();
			this.CEE = this.getLoss(answer);
			this.accuracy = this.accuracy(answer);
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static double learningRate = 0.1;
	
	public void moveNext()
	{
		try {
			Matrix w1Delta = this.getNumericalGrandient(this.w1);
			MatrixOperator op = w1Delta.getMatrixOperator();
			op.mult(w1Delta, learningRate,w1Delta);
			op.sub(w1, w1Delta, w1);
			
			/*
			Matrix w2Delta = this.getNumericalGrandient(this.w2);
			op = w2Delta.getMatrixOperator();
			op.multToA(w2Delta, learningRate);
			this.w2 = op.sub(w2, w1Delta);

			Matrix b1Delta = this.getNumericalGrandient(this.b1);
			op = b1Delta.getMatrixOperator();
			op.multToA(b1Delta, learningRate);
			this.b1 = op.sub(b1, w1Delta);

			Matrix b2Delta = this.getNumericalGrandient(this.b2);
			op = b2Delta.getMatrixOperator();
			op.multToA(b2Delta, learningRate);
			this.b2 = op.sub(b2, w1Delta);
			*/
		}catch(Exception ex)
		{
			
		}
	}
	
	public Matrix getNumericalGrandient(Matrix x)
	{
		try {
			double h = 0.0001;
			
			Matrix output = x.copy();
			
			int row = x.getHeight();
			int column = x.getWidth();
			System.out.println("move... total count:"+(row*column));
			int count=0;
			long startT = Calendar.getInstance().getTimeInMillis();
			for(int i=0;i<row;i++)
			{
				for(int j=0;j<column;j++)
				{
					double tmpVal = x.getData(i, j);
					x.setData(tmpVal + h, i, j);
					double res0 = this.getLoss();
					x.setData(tmpVal - h, i, j);
					double res1 = this.getLoss();
					output.setData((res0-res1)/(2.0*h), i, j);
					x.setData(tmpVal, i,j);
					count++;
										
				}
				double rate = (double)count/(double)(row*column);
				long current = Calendar.getInstance().getTimeInMillis();
				System.out.printf("%f percent, current count:%d, duration:%d ms\n", rate*100,count,(current - startT));
				startT = current;
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	

	
}
