package mymltest;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import dolziplib.Batchwork;
import dolziplib.DDouble;
import dolziplib.calculation.Cal;
import dolziplib.calculation.CalAdd;
import dolziplib.calculation.CalCrossEntoryErroy;
import dolziplib.calculation.CalDot;
import dolziplib.calculation.CalSigmoid;
import dolziplib.calculation.CalSoftmax;
import dolziplib.calculation.CalSoftmaxCEE;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixColt;
import dolziplib.matrix.MatrixOperator;
import dolziplib.mnist.Mnist;
import dolziplib.mnist.MnistImg;

public class DZ2Layer extends Batchwork {

	boolean singleImage = false;
	boolean inputAssigned=false;
	boolean trainSet = false;
	
	public Mnist mnistTrain;
	public Mnist mnistTest;
	public Mnist choosen;
	
	public Matrix input;
	public Matrix correctAnswer;
	
	public Matrix w1;
	public Matrix w2;
	public Matrix w3;
	public Matrix b1;
	public Matrix b2;
	public Matrix b3;
	
	public Matrix w1Grad;
	public Matrix w2Grad;
	public Matrix w3Grad;
	public Matrix b1Grad;
	public Matrix b2Grad;
	public Matrix b3Grad;

	public Matrix A3Grad;
	public Matrix dot3Grad;
	public Matrix Z2Grad;
	public Matrix A2Grad;
	public Matrix dot2Grad;
	public Matrix Z1Grad;
	public Matrix A1Grad;
	public Matrix dot1Grad;
	public Matrix inputGrad;
	

	public Matrix dot1;
	public Matrix A1;
	public Matrix Z1;
	public Matrix dot2;
	public Matrix A2;
	public Matrix Z2;
	public Matrix dot3;
	public Matrix A3;
	
	public Matrix ret;
	int size=0;
	
	private Random r = null;
	public DDouble crossErrorEntropy;
	
	int matrixType;
	
	private void setRandom(Matrix m)
	{
		try {
			for(int i=0;i<m.getHeight();i++)
			{
				for(int j=0;j<m.getWidth();j++)
				{
					m.setData(r.nextGaussian(), i, j);
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void initMatrixValues()
	{
		initMatrixValues(false);
	}
	public void initMatrixValues(boolean fromErrorBinary)
	{
		if(fromErrorBinary)
		{
			/*
					dz2layer.input.saveToBinFile("error_input.bin");
					dz2layer.correctAnswer.saveToBinFile("error_correct_answer.bin");
					dz2layer.w1.saveToBinFile("error_w1.bin");
					dz2layer.w2.saveToBinFile("error_w2.bin");
					dz2layer.w3.saveToBinFile("error_w3.bin");
					dz2layer.b1.saveToBinFile("error_b1.bin");
					dz2layer.b2.saveToBinFile("error_b2.bin");
					dz2layer.b3.saveToBinFile("error_b3.bin");
			 */
			try {
				input.loadFromBinFile("error_input.bin");
				correctAnswer.loadFromBinFile("error_correct_answer.bin");
				w1.loadFromBinFile("error_w1.bin");
				w2.loadFromBinFile("error_w2.bin");
				w3.loadFromBinFile("error_w3.bin");
				b1.loadFromBinFile("error_b1.bin");
				b2.loadFromBinFile("error_b2.bin");
				b3.loadFromBinFile("error_b3.bin");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			
			setRandom(w1);
			setRandom(w2);
			setRandom(w3);
			
			//setRandom(b1);
			//setRandom(b2);
			//setRandom(b3);
			
			b1.setData(0);
			b2.setData(0);
			b3.setData(0);
			
			/*
			w1.setData(0.5);
			w2.setData(0.5);
			w3.setData(0.5);
			
			b1.setData(0.5);
			b2.setData(0.5);
			b3.setData(0.5);
			*/
		}
		/*

		*/
		
		w1Grad.setData(0.0);
		w2Grad.setData(0.0);
		w3Grad.setData(0.0);
		
		b1Grad.setData(0.0);
		b2Grad.setData(0.0);
		b3Grad.setData(0.0);
	}
	
	public void clearGrad()
	{
		w1Grad.setData(0.0);
		w2Grad.setData(0.0);
		w3Grad.setData(0.0);
		
		b1Grad.setData(0.0);
		b2Grad.setData(0.0);
		b3Grad.setData(0.0);
		
		A3Grad.setData(0.0);
		dot3Grad.setData(0.0);
		Z2Grad.setData(0.0);
		A2Grad.setData(0.0);
		dot2Grad.setData(0.0);
		Z1Grad.setData(0.0);
		A1Grad.setData(0.0);
		dot1Grad.setData(0.0);
		inputGrad.setData(0.0);
		b3Grad.setData(0.0);
		b3Grad.setData(0.0);
		b3Grad.setData(0.0);
		
	}
	
	public static double h = 1e-3;
	private void getGrad(Matrix input,Matrix output)
	{
		
		//System.out.println("input name:"+input.getName());
		//System.out.println("grad h:"+input.getHeight()+" w:"+input.getWidth());
		//int total = input.getHeight() * input.getWidth();
		try {
			for(int i=0;i<input.getHeight();i++)
			{
				for(int j=0;j<input.getWidth();j++)
				{
					double tmp = input.getData(i, j);
					input.setData(tmp+DZ2Layer.h, i, j);
					this.doBatch(false);
					double fxh1 = this.crossErrorEntropy.value;
					input.setData(tmp-h, i, j);
					this.doBatch(false);
					double fxh2 = this.crossErrorEntropy.value;
					double v = (fxh1-fxh2)/(2*h);
					output.setData(v, i, j);
					input.setData(tmp,i,j);
					
					/*
					if(input.getName().equals("b3"))
					{
						System.out.printf("fxh1:%f fxh2:%f 2*h:%f ret:%f\n",fxh1,fxh2,2*h,v);
					}
					*/
					
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void getAllGrad()
	{
		this.getGrad(w1, w1Grad);
		this.getGrad(w2, w2Grad);
		this.getGrad(w3, w3Grad);
		this.getGrad(b1, b1Grad);
		this.getGrad(b2, b2Grad);
		this.getGrad(b3, b3Grad);
	}
	
	private void applyGrad(Matrix a, Matrix b, double learningRate)  throws Exception 
	{
		int w = a.getWidth();
		int h = a.getHeight();
		try {
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<w;j++)
				{
					double v = a.getData(i, j);
					double v1 = v - learningRate * b.getData(i, j);
					a.setData(v1, i, j);
					
					if(Double.isNaN(v) || Double.isNaN(v1))
					{
						throw new Exception("apply Grid NaN M:"+a.getName()+" ["+i+","+j+"]");
					}
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void applyGrad(double learningRate)  throws Exception 
	{
		this.applyGrad(w1, w1Grad, learningRate);
		this.applyGrad(w2, w2Grad, learningRate);
		this.applyGrad(w3, w3Grad, learningRate);
		this.applyGrad(b1, b1Grad, learningRate);
		this.applyGrad(b2, b2Grad, learningRate);
		this.applyGrad(b3, b3Grad, learningRate);
	}
	
	private boolean checkNaN(Matrix m)
	{
		for(int i=0;i<m.getHeight();i++)
		{
			for(int j=0;j<m.getWidth();j++)
			{
				if(Double.isNaN(m.getData(i, j)))
				{
					System.out.println("Matrix "+m.getName()+" has NaN.");
					return true;
				}
			}
		}
		return false;
	}
	public boolean checkNaN()
	{
		if(checkNaN(w1Grad))return true;
		if(checkNaN(w2Grad))return true;
		if(checkNaN(w3Grad))return true;
		if(checkNaN(b1Grad))return true;
		if(checkNaN(b2Grad))return true;
		if(checkNaN(b3Grad))return true;
	
		return false;
	}
	
	public int getTotalChangElementSize()
	{
		int ret = w1.getHeight() * w1.getWidth();
		ret += w2.getWidth() * w2.getHeight();
		ret += w3.getHeight() * w3.getWidth();
		ret += b1.getHeight() * b1.getWidth();
		ret += b2.getHeight() * b2.getWidth();
		ret += b3.getHeight() * b3.getWidth();
		return ret;
	}
	
	public DZ2Layer getFullTest()
	{
		DZ2Layer test = new DZ2Layer();
		test.matrixType = this.matrixType;
		test.r = new Random(Calendar.getInstance().getTimeInMillis());
		test.mnistTest = this.mnistTest;
		test.mnistTrain = this.mnistTrain;
		try {
			test.w1 = this.w1.copy();
			test.w2 = this.w2.copy();
			test.w3 = this.w3.copy();
			test.b1 = this.b1.copy();
			test.b2 = this.b2.copy();
			test.b3 = this.b3.copy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test.setInputImageAll(false);
		return test;
		
	}
	private DZ2Layer()
	{
	}
	
	public DZ2Layer(int matrixType)
	{
		this.matrixType = matrixType;
		r = new Random(Calendar.getInstance().getTimeInMillis());
		System.out.println("loading mnist...");
		
		mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
		mnistTest = Mnist.getFromBinFile("mnist_test.bin");
		
		System.out.println("loading weight...");

		try {
			w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType);
			w2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType);
			w3 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType);
			b1 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType);
			b2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType);
			b3 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setInputSingleImage(int index,boolean trainSet)
	{
		this.trainSet = trainSet;
		MnistImg img = null;
		if(trainSet)
		{
			img = this.mnistTrain.imgs.get(index);
			choosen = this.mnistTrain;
		}
		else
		{
			img = this.mnistTest.imgs.get(index);
			choosen = this.mnistTest;
		}
		if(this.input!=null)this.input.release();
		input = img.getInputMatrix(this.matrixType);
		
		if(this.correctAnswer!=null)this.correctAnswer.release();
		correctAnswer = img.getCorrectAnswerMatrix(matrixType);
		
		setupInternalMatrix();
		setupCalculation();
		
		singleImage = true;
		inputAssigned = true;
	}
	public void setInputImageAll(boolean trainSet)
	{
		this.trainSet = trainSet;
		if(this.input!=null)this.input.release();
		if(this.correctAnswer!=null)this.correctAnswer.release();
		if(trainSet)
		{
			input = mnistTrain.getInputMatrix(mnistTrain.imgs.size(), matrixType);
			correctAnswer = mnistTrain.getCorrectAnswerMatrix(mnistTrain.imgs.size(), matrixType);
			this.choosen = mnistTrain;
		}
		else
		{
			input = mnistTest.getInputMatrix(mnistTest.imgs.size(), matrixType);
			correctAnswer = mnistTest.getCorrectAnswerMatrix(mnistTest.imgs.size(), matrixType);			
			this.choosen = mnistTest;
		}
		
		setupInternalMatrix();
		setupCalculation();
		
		singleImage = false;
		inputAssigned = true;
	}
	public void setInputImage(int size,boolean trainSet)
	{
		this.trainSet = trainSet;
		if(this.input!=null)this.input.release();
		if(this.correctAnswer!=null)this.correctAnswer.release();
		if(trainSet)
		{
			input = mnistTrain.getInputMatrix(size, matrixType);
			correctAnswer = mnistTrain.getCorrectAnswerMatrix(size, matrixType);
			choosen = mnistTrain;
		}
		else
		{
			input = mnistTest.getInputMatrix(size, matrixType);
			correctAnswer = mnistTest.getCorrectAnswerMatrix(size, matrixType);			
			choosen = mnistTest;
		}
		
		setupInternalMatrix();
		setupCalculation();
		
		singleImage = false;
		inputAssigned = true;
	}
	
	public void setRandomInputImage(int size,boolean trainSet)
	{
		this.size = size;
		this.trainSet = trainSet;
		if(trainSet)
		{
			choosen = new Mnist();
			for(int i=0;i<size;i++)
			{
				int next = r.nextInt(mnistTrain.imgs.size());
				choosen.imgs.add(mnistTrain.imgs.get(next));
				choosen.originalIndex.add(new Integer(next));
			}
		}
		else
		{
			choosen = new Mnist();
			for(int i=0;i<size;i++)
			{
				int next = r.nextInt(mnistTest.imgs.size());
				choosen.imgs.add(mnistTest.imgs.get(next));
				choosen.originalIndex.add(new Integer(next));
			}
		}
		
		if(this.input!=null)
		{
			try {
				choosen.getInputMatrix(size, input);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else input = choosen.getInputMatrix(size, matrixType);
		if(this.correctAnswer!=null)
		{
			try {
				choosen.getCorrectAnswerMatrix(size, correctAnswer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else correctAnswer = choosen.getCorrectAnswerMatrix(size, matrixType);
		
		if(inputAssigned==false)
		{
			setupInternalMatrix();
			setupCalculation();
			
			singleImage = false;
			inputAssigned = true;
		}
	}
	public void setNextRandomImage()
	{
		if(trainSet)
		{
			choosen = new Mnist();
			for(int i=0;i<size;i++)
			{
				int next = r.nextInt(mnistTrain.imgs.size());
				choosen.imgs.add(mnistTrain.imgs.get(next));
				choosen.originalIndex.add(new Integer(next));
			}
		}
		else
		{
			choosen = new Mnist();
			for(int i=0;i<size;i++)
			{
				int next = r.nextInt(mnistTest.imgs.size());
				choosen.imgs.add(mnistTest.imgs.get(next));
				choosen.originalIndex.add(new Integer(next));
			}
		}
		
		if(this.input!=null)
		{
			try {
				choosen.getInputMatrix(size, input);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(this.correctAnswer!=null)
		{
			try {
				choosen.getCorrectAnswerMatrix(size, correctAnswer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void setupInternalMatrix()
	{
		w1.setName("w1");
		w2.setName("w2");
		w3.setName("w3");

		b1.setName("b1");
		b2.setName("b2");
		b3.setName("b3");

		w1Grad = w1.copy(false);
		w1Grad.setName("w1Grad");
		w2Grad = w2.copy(false);
		w2Grad.setName("w2Grad");
		w3Grad = w3.copy(false);
		w3Grad.setName("w3Grad");
		b1Grad = b1.copy(false);
		b1Grad.setName("b1Grad");
		b2Grad = b2.copy(false);
		b2Grad.setName("b2Grad");
		b3Grad = b3.copy(false);
		b3Grad.setName("b3Grad");
		
		dot1 = Matrix.create(input.getHeight(), w1.getWidth(), this.matrixType);
		dot1.setName("dot1");
		A1 = dot1.copy(false);
		A1.setName("A1");
		Z1 = A1.copy(false);
		Z1.setName("Z1");

		dot2 = Matrix.create(Z1.getHeight(), w2.getWidth(), this.matrixType);
		dot2.setName("dot2");
		A2 = dot2.copy(false);
		A2.setName("A2");
		Z2 = A2.copy(false);
		Z2.setName("Z2");

		dot3 = Matrix.create(Z2.getHeight(), w3.getWidth(), this.matrixType);
		dot3.setName("dot3");
		A3 = dot3.copy(false);
		A3.setName("A3");

		ret = A3.copy(false);
		ret.setName("ret");
		
		crossErrorEntropy = new DDouble();
		crossErrorEntropy.value = 0.0;

		/*
		Matrix dot1 = op.dot(input, W1);
		Matrix A1 = op.add(dot1,B1);
		Matrix Z1 = op.getSigmoid(A1);
		Matrix dot2 = op.dot(Z1, W2);
		Matrix A2 = op.add(dot2,B2);
		Matrix Z2 = op.getSigmoid(A2);
		Matrix A3 = op.add(op.dot(Z2, W3),B3);
		Matrix ret = op.getSoftMax(A3);
		*/
	}
	
	private void setupCalculation()
	{
		this.clearCal();
		
		MatrixOperator op = this.input.getMatrixOperator();

		CalDot cal0 = new CalDot(input, w1, dot1,op ,"dot1");
		this.inputGrad = input.copy(false);
		this.dot1Grad = this.dot1.copy(false);
		cal0.setBackPropagation(this.inputGrad, this.w1Grad, this.dot1Grad);
		this.addCal(cal0);
		
		CalAdd cal1 = new CalAdd(dot1, b1, A1, op,"add1");
		this.A1Grad = A1.copy(false);
		cal1.setBackPropagation(this.dot1Grad, this.b1Grad, this.A1Grad);
		this.addCal(cal1);
		
		CalSigmoid cal2 = new CalSigmoid(A1, Z1,  op ,"sigmoid1");
		this.Z1Grad = Z1.copy(false);
		cal2.setBackPropagation(this.A1Grad, this.Z1Grad);
		this.addCal(cal2);
		
		CalDot cal3 = new CalDot(Z1, w2, dot2,op ,"dot2");
		this.dot2Grad = this.dot2.copy(false);
		cal3.setBackPropagation(this.Z1Grad, this.w2Grad, this.dot2Grad);
		this.addCal(cal3);

		CalAdd cal4 = new CalAdd(dot2, b2, A2, op,"add2");
		this.A2Grad = A2.copy(false);
		cal4.setBackPropagation(this.dot2Grad, this.b2Grad, this.A2Grad);
		this.addCal(cal4);

		CalSigmoid cal5 = new CalSigmoid(A2, Z2,   op ,"sigmoid2");
		this.Z2Grad = Z2.copy(false);
		cal5.setBackPropagation(this.A2Grad, this.Z2Grad);
		this.addCal(cal5);

		CalDot cal6 = new CalDot(Z2, w3, dot3,op ,"dot3");
		this.dot3Grad = this.dot3.copy(false);
		cal6.setBackPropagation(this.Z2Grad, this.w3Grad, this.dot3Grad);
		this.addCal(cal6);

		CalAdd cal7 = new CalAdd(dot3, b3, A3, op,"add3");
		this.A3Grad = A3.copy(false);
		cal7.setBackPropagation(this.dot3Grad, this.b3Grad, this.A3Grad);
		this.addCal(cal7);
		
		CalSoftmaxCEE cal8 = new CalSoftmaxCEE(A3, ret,this.correctAnswer, crossErrorEntropy, op, "softmaxCEE");
		cal8.setBackPropagation(this.A3Grad);
		this.addCal(cal8);
	}
	
	static boolean matrixCheck(String name,Matrix A, Matrix B)
	{
		if(A.getHeight()!=B.getHeight())
		{
			System.out.println(name+" width is not matched");
			return false;
		}
		if(A.getWidth()!=B.getWidth())
		{
			System.out.println(name+" width is not matched");
			return false;
		}
		System.out.printf(name+" checking matrix %d x %d ...\n",A.getHeight(),A.getWidth());
		for(int i=0;i<A.getHeight();i++)
		{
			for(int j=0;j<A.getWidth();j++)
			{
				float a = (float)A.getData(i, j);
				float b = (float)B.getData(i, j);
				float delta = a-b;
				if(delta<0)delta *= -1;
				if(delta/a > 0.01)
				{
					System.out.printf("[%d,%d] not matched %f <==> %f\n",i,j,a,b);
					return false;
				}
			}
		}
		System.out.println("ok matched");
		
		return true;
	}
	
	private static int maxPossibleNum(double data[])
	{
		int index=-1;
		double max=-1.0;
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
	
	public double getCorrectRate()
	{
		int totalTestingCount = ret.getHeight();
		int correct = 0;
		for(int i=0;i<totalTestingCount;i++)
		{
			double[] outputArray = ret.getRow(i);
			double[] correctArray = this.correctAnswer.getRow(i);
			int maxNum = maxPossibleNum(outputArray);
			int rightNum = maxPossibleNum(correctArray);
			if(maxNum == rightNum)
			{
				correct++;
				//System.out.println("testing label:"+rightNum+" correct.");
			}
			else
			{
				//System.out.println("testing label:"+rightNum);

			}
		}
		return (double)correct/(double)totalTestingCount;
	}
	
	public void evaluation()
	{
		System.out.println("evaluation...");
		int totalTestingCount = ret.getHeight();
		int correct = 0;
		System.out.print("choosed image:");
		if(totalTestingCount>100)System.out.print("more than 100");
		for(int i=0;i<totalTestingCount;i++)
		{
			if(totalTestingCount<=100)System.out.print(""+choosen.originalIndex.get(i).intValue()+" ");
			
			double[] outputArray = ret.getRow(i);
			MnistImg img = choosen.imgs.get(i);
			
			if(maxPossibleNum(outputArray) == img.label)correct++;
			else 
			{
			//	System.out.println("error index :"+i+" label:"+img.label+" wrong answer:"+maxPossibleNum(outputArray));
			}
		}
		System.out.println();
		
		for(Cal cal:this.calculations)
		{
			System.out.println(cal.getName()+" duration:"+cal.getDuration()/1000.0+" us");
		}
		
		System.out.println("input matrix type:"+input.getMatrixTypeName()+" h:"+input.getHeight()+" w:"+input.getWidth());
		System.out.println("output matrix type:"+ret.getMatrixTypeName()+" h:"+ret.getHeight()+" w:"+ret.getWidth());
		System.out.printf("total duration = %f us\n",this.totalDuration/1000.0);
		try {
			System.out.println("total:"+totalTestingCount+" correct:"+correct +" cross_entropy_error:"+this.crossErrorEntropy.value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
