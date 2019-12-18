package mymltest;

import java.util.Calendar;
import java.util.Random;

import dolziplib.DDouble;
import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.DZMath;
import dolziplib.calculation.Cal;
import dolziplib.calculation.CalAdd;
import dolziplib.calculation.CalBatchNormalization;
import dolziplib.calculation.CalCrossEntoryErroy;
import dolziplib.calculation.CalDot;
import dolziplib.calculation.CalMeanSquaredError;
import dolziplib.calculation.CalReLU;
import dolziplib.calculation.CalSigmoid;
import dolziplib.calculation.CalSoftmaxCEE;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class TestCaseBackpropagation implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	
	public TestCaseBackpropagation(int matrixType)
	{
		this.matrixType = matrixType;
	}
	
	private static boolean checkZeroMatrix(Matrix a)
	{
		for(int i=0;i<a.getHeight();i++)
		{
			for(int j=0;j<a.getWidth();j++)
			{
				if(a.getData(i, j)>0.0001 || a.getData(i, j)<-0.0001)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private static boolean checkMatrix(String name, Matrix a, double right[][])
	{
		System.out.print("checking "+name+" ...");
		for(int i=0;i<a.getHeight();i++)
		{
			for(int j=0;j<a.getWidth();j++)
			{
				double delta = a.getData(i, j) - right[i][j];
				
				if(delta<0)delta *= -1;
				if(delta>0.01)
				{
					System.out.printf("[%d,%d] %f <==> %f | %f\n",i,j,a.getData(i, j),right[i][j],delta);
					return true;
				}
			}
		}
		System.out.println("OK");
		return false;
	}
	private static boolean checkValue(String name, double a, double v)
	{
		double delta = a - v; 
		System.out.print("checking "+name+" ...");
		if(delta<0)delta *= -1;
		if(delta>0.01)
		{
			System.out.printf("%f <==> %f | %f\n",a,v,delta);
			return true;
		}
		System.out.println("OK");
		return false;
	}
	
	//dot연산은 그 자체만으로는 backpropagation의 연산 test를 할수 없다.
	//그 이유는 dot의 backpropagation의 연산의 dL/dX 또는 dL/dY의 기반이며, 이때의  L값은 스칼라값이기 때문이다.
	//즉, 출력단이 matrix이면 grad를 알수 없기 때문에, 반드시 최종 스칼라로 만드는 SoftmaxCEE단이 있어야 한다.
	private boolean dotTest(int h, int w) throws Exception
	{
		boolean ret = false;
		
		
		Matrix input0 = Matrix.create(h, w, this.matrixType);
		Matrix input1 = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);

		Matrix backOuput0 = Matrix.create(h, w, this.matrixType);
		Matrix backOuput1 = Matrix.create(h, w, this.matrixType);
		Matrix backInput = Matrix.create(h, w, this.matrixType);

		MatrixOperator op = input0.getMatrixOperator();

		Matrix ceeInput = output;
		Matrix ceeOutput = ceeInput.copy(false);
		Matrix ceeBackOutput = backInput.copy(false);
		Matrix rightAnswer = ceeOutput.copy(false);
		Random random = new Random(555);
		 //SoftmaxCEE는 one-hot-encoding의 answerdata여야 올바른 backpropagation이 된다.
		for(int i=0;i<rightAnswer.getHeight();i++)
		{
			double rightA[] = new double[rightAnswer.getWidth()];
			for(int j=0;j<rightA.length;j++)rightA[j]=0;
			rightA[random.nextInt(rightA.length)] = 1.0;
			rightAnswer.setData(rightA, i);
		}
		DDouble ceeOutputV = new DDouble();
		
		CalDot dot = new CalDot(input0, input1, output, op, "");
		dot.setBackPropagation(backOuput0, backOuput1, backInput);
		CalSoftmaxCEE cee = new CalSoftmaxCEE(ceeInput, ceeOutput, rightAnswer, ceeOutputV, op, "");
		cee.setBackPropagation(backInput);
		
		input0.setStandardRandomValue(111);
		input1.setGuassianRandomValue(222);
		
		dot.doCal(false);
		cee.doCal(false);
		
		double dx=0.0001;
		
		double[][] sdg = new double[h][w];
		
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				double o = input1.getData(i, j);
				input1.setData(o+dx, i, j);
				dot.doCal(false);
				cee.doCal(false);
				double v1 = ceeOutputV.value;
				//System.out.println("v1:"+v1+ " ceeinput0:"+output.getData(r, c));
				
				input1.setData(o-dx, i, j);
				dot.doCal(false);
				cee.doCal(false);
				double v2 = ceeOutputV.value;
				//System.out.println("v2:"+v2+ " ceeinput1:"+output.getData(r, c));
				
				//System.out.println("dy/dx = "+(v1-v2)/(2.0*dx));
				input1.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		dot.doCal(false);
		cee.doCal(false);
		//System.out.println("cee:"+ceeOutputV.value);
		cee.doBack(false);
		dot.doBack(false);
		//double grad = backOuput1.getData(r, c);
		//System.out.println("grad:"+grad);
		
		if(checkZeroMatrix(backOuput1))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("dot product backpropagation",backOuput1,sdg);
		
		rightAnswer.release();
		ceeInput.release();
		ceeOutput.release();
		ceeBackOutput.release();
		input0.release();
		input1.release();
		output.release();
		
		return ret;
	}
	

	private boolean sigmoidTest(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);

		Matrix backOuput = Matrix.create(h, w, this.matrixType);
		Matrix backInput = Matrix.create(h, w, this.matrixType);

		MatrixOperator op = input.getMatrixOperator();
		CalSigmoid cal = new CalSigmoid(input, output,  op, "");
		cal.setBackPropagation(backOuput,backInput);
		
		input.setStandardRandomValue(111);
				
		double dx=0.0001;
		
		double[][] sdg = new double[h][w];
		
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
		
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				cal.doCal(false);
				double v1 = output.getData(i, j);
				
				input.setData(o-dx, i, j);
				cal.doCal(false);
				double v2 = output.getData(i, j);
				
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		backInput.setData(1.0);
		cal.doCal(false);
		cal.doBack(false);
		
		
		if(checkZeroMatrix(backOuput))
		{
			System.out.println("matrix is zero");
			ret=true;
		}
		else ret = checkMatrix("Sigmoid backpropagation", backOuput, sdg);
		
		
		backOuput.release();
		input.release();
		output.release();
		
		return ret;
	}
	
	private boolean ReLUTest(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);

		Matrix backOuput = Matrix.create(h, w, this.matrixType);
		Matrix backInput = Matrix.create(h, w, this.matrixType);

		MatrixOperator op = input.getMatrixOperator();
		CalReLU cal = new CalReLU(input, output,  op, "");
		cal.setBackPropagation(backOuput,backInput);
		
		input.setStandardRandomValue(111);
		op.mult(input, -1.0, input);
				
		double dx=0.0001;
		
		double[][] sdg = new double[h][w];
		
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
		
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				cal.doCal(false);
				double v1 = output.getData(i, j);
				
				input.setData(o-dx, i, j);
				cal.doCal(false);
				double v2 = output.getData(i, j);
				
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		backInput.setData(1.0);
		cal.doCal(false);
		cal.doBack(false);
		
		//System.out.println("input:"+input);
		//System.out.println("backOuput:"+backOuput);
		//DZHelper.printArray("sdg:", sdg);
		
		if(checkZeroMatrix(backOuput))
		{
			System.out.println("matrix is zero");
			ret=true;
		}
		else ret = checkMatrix("ReLU backpropagation", backOuput, sdg);
		
		
		backOuput.release();
		input.release();
		output.release();
		
		return ret;
	}
	
	//본 함수의 결과 치는 eps가 0.000001 일때 유효하다. CalBatchNormalization.eps 값을 변경하면 결과치도 조금 변경되어, fail이 날수 있다.
	private boolean batchNormalizationSingleTest() throws Exception
	{
		int h=4;
		int w=5;
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);
		Matrix backOuput = Matrix.create(h, w, this.matrixType);
		Matrix backInput = Matrix.create(h, w, this.matrixType);
		
		DZConfig config = new DZConfig();
		
		MatrixOperator op = input.getMatrixOperator();
		CalBatchNormalization cal = new CalBatchNormalization(input, output, op, "",config);
		cal.setBackPropagation(backOuput,backInput);
		
		//single backpropagation test
		input.setData(new double[][] {
			{0.1,0.3,0.7,0.8,0.4}, 
			{0.8,0.7,0.2,1.0,0.3},
			{0.4,0.4,0.7,0.5,0.1},
			{0.5,0.5,0.1,0.1,0.3}
		});
		cal.doCal(false);
		//System.out.println("output:"+output);
		backInput.setData(new double[][] {
				{10,11,12,13,14},
				{21,22,23,24,25},
		        {34,32,47,32,76},
		        {25,42,43,21,32} 
		}
		);
		cal.doBack(false);
		double[][] singleGrad = {
				{-30.96005696, -83.83842117, -59.99375114, -26.090719,    35.71846404},
						 {-25.03949505, -61.23473172, -37.47392555,   8.26950118, -58.92525675},
						 { 48.71956673,  45.20220208,  66.23917669,  27.05228444,  17.8983485 },
						 {  7.27998528,  99.87095082,  31.2285,      -9.23106662,   5.30844421}

		};
		System.out.println("back output:"+backOuput);
		
		boolean ret = false;
		ret |= checkMatrix("batch normalization back propagation(single) ",backOuput, singleGrad );
		return ret;
	}
	
	private boolean batchNormalizationTest(int h, int w) throws Exception
	{
		int lastCEEDimenstion = 4;
		boolean ret = false;
		DDouble ceeOutputV = new DDouble();
		
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);
		Matrix backOuput = Matrix.create(h, w, this.matrixType);
		Matrix backInput = Matrix.create(h, w, this.matrixType);
		
		Matrix dotInput1 = Matrix.create(w, lastCEEDimenstion, this.matrixType);
		Matrix dotOutput = Matrix.create(h, lastCEEDimenstion, this.matrixType);
		Matrix dotBackOutput1 = dotInput1.copy(false);
		
		Matrix ceeOutput = dotOutput.copy(false);
		Matrix ceeBackOutput = dotOutput.copy(false);
		Matrix rightAnswer = dotOutput.copy(false);
		
		MatrixOperator op = input.getMatrixOperator();
		CalBatchNormalization cal = new CalBatchNormalization(input, output, op, "", new DZConfig());
		cal.setBackPropagation(backOuput,backInput);
		
		CalDot dot = new CalDot(output, dotInput1, dotOutput, op, "");
		dot.setBackPropagation(backInput, dotBackOutput1, ceeBackOutput);
		

		CalSoftmaxCEE cee = new CalSoftmaxCEE(dotOutput, ceeOutput, rightAnswer, ceeOutputV, op, "");
		cee.setBackPropagation(ceeBackOutput);
		
		
		
		Random random = new Random(555);
		 //SoftmaxCEE는 one-hot-encoding의 answerdata여야 올바른 backpropagation이 된다.
		for(int i=0;i<rightAnswer.getHeight();i++)
		{
			double rightA[] = new double[rightAnswer.getWidth()];
			for(int j=0;j<rightA.length;j++)rightA[j]=0;
			rightA[random.nextInt(rightA.length)] = 1.0;
			rightAnswer.setData(rightA, i);
		}

		
		dotInput1.setGuassianRandomValue(321);
		input.setStandardRandomValue(111);
				
		cal.doCal(false);
		dot.doCal(false);
		cee.doCal(false);
		//System.out.println("forward0:"+output);
		//System.out.println("forward1 :"+dotOutput);
		//System.out.println("forward2 :"+ceeOutput);
		
		//int a=3;
		//if(a==3)return true;
		
		double dx=0.0001;
		
		// beta grad testing
		double[][] betaGrad = new double[1][cal.betaGrad.getWidth()];
		for(int i=0;i<cal.beta.getWidth();i++)
		{
			double o = cal.beta.getData(0, i);
			cal.beta.setData(o+dx,0,i);
			cal.doCal(false);
			dot.doCal(false);
			cee.doCal(false);
			double v1 = ceeOutputV.value;
			cal.beta.setData(o-dx,0,i);
			cal.doCal(false);
			dot.doCal(false);
			cee.doCal(false);
			double v2 = ceeOutputV.value;
			double grad0 = (v1-v2)/(2.0*dx);
			betaGrad[0][i] = grad0;
			cal.beta.setData(o,0,i);
		}
		cal.doCal(false);
		dot.doCal(false);
		cee.doCal(false);

		cee.doBack(false);
		dot.doBack(false);
		cal.doBack(false);
		//DZHelper.printArray("grad normal:",betaGrad);
		System.out.println("beta grad back:"+cal.betaGrad);

		if(checkZeroMatrix(cal.betaGrad))
		{
			System.out.println("beta grad matrix is zero");
			ret = true;
		}
		else ret |= checkMatrix("beta value ",cal.betaGrad, betaGrad );
		
			
		
		// gamma grad testing
		double[][] gammaGrad = new double[1][cal.gammaGrad.getWidth()];
		for(int i=0;i<cal.gamma.getWidth();i++)
		{
			double o = cal.gamma.getData(0, i);
			cal.gamma.setData(o+dx,0,i);
			cal.doCal(false);
			dot.doCal(false);
			cee.doCal(false);
			double v1 = ceeOutputV.value;
			cal.gamma.setData(o-dx,0,i);
			cal.doCal(false);
			dot.doCal(false);
			cee.doCal(false);
			double v2 = ceeOutputV.value;
			double grad0 = (v1-v2)/(2.0*dx);
			gammaGrad[0][i] = grad0;
			cal.gamma.setData(o,0,i);
		}
		cal.doCal(false);
		dot.doCal(false);
		cee.doCal(false);
		cee.doBack(false);
		dot.doBack(false);
		cal.doBack(false);
		//DZHelper.printArray("grad normal:",gammaGrad);
		System.out.println("gamma grad back:"+cal.gammaGrad);
		
		if(checkZeroMatrix(cal.gammaGrad))
		{
			System.out.println("gamma grad matrix is zero");
			ret = true;
		}
		else ret |= checkMatrix("gamma value ",cal.gammaGrad, gammaGrad );
		
		double[][] sdg = new double[h][w];
		
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
		
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				cal.doCal(false);
				dot.doCal(false);
				cee.doCal(false);
				double v1 = ceeOutputV.value;
				
				input.setData(o-dx, i, j);
				cal.doCal(false);
				dot.doCal(false);
				cee.doCal(false);
				double v2 = ceeOutputV.value;
				
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}

		
		cal.doCal(false);
		dot.doCal(false);
		cee.doCal(false);
		cee.doBack(false);
		dot.doBack(false);
		cal.doBack(false);
		
		//DZHelper.printArray(sdg);
		System.out.println("backOuput:"+backOuput);
	
		ret |= checkMatrix("Batch Normalization backpropagation", backOuput, sdg);
		

		dotInput1.release();
		dotOutput.release();
		dotBackOutput1.release();
		rightAnswer.release();
		ceeOutput.release();
		ceeBackOutput.release();
		backOuput.release();
		input.release();
		output.release();
		
		return ret;
	}
	
	private boolean softmaxCEETest(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);

		Matrix backOuput = Matrix.create(h, w, this.matrixType);

		MatrixOperator op = input.getMatrixOperator();

		Matrix rightAnswer = output.copy(false);
		DDouble ceeOutputV = new DDouble();
		
		CalSoftmaxCEE cee = new CalSoftmaxCEE(input, output, rightAnswer, ceeOutputV, op, "");
		cee.setBackPropagation(backOuput);
		
		input.setStandardRandomValue(111);
		Random random = new Random(555);
		for(int i=0;i<rightAnswer.getHeight();i++) //SoftmaxCEE는 one-hot-encoding의 answerdata여야 올바른 backpropagation이 된다.
		{
			double rightA[] = new double[rightAnswer.getWidth()];
			for(int j=0;j<rightA.length;j++)rightA[j]=0;
			rightA[random.nextInt(rightA.length)] = 1.0;
			rightAnswer.setData(rightA, i);
		}
				
		double dx=0.0001;
		
		double[][] sdg = new double[h][w];
		
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
		
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				cee.doCal(false);
				double v1 = ceeOutputV.value;
				
				input.setData(o-dx, i, j);
				cee.doCal(false);
				double v2 = ceeOutputV.value;
				
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		cee.doCal(false);
		cee.doBack(false);
		
		if(checkZeroMatrix(backOuput))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("Softmax and CEE backpropagation", backOuput, sdg);
		
		
		backOuput.release();
		rightAnswer.release();
		input.release();
		output.release();
		
		return ret;
	}
	
	
	private boolean softmaxCEETest() throws Exception
	{
		boolean ret = false;
		int h=4;
		int w=4;
		
		Matrix input = Matrix.create(h, w, this.matrixType);
		Matrix output = Matrix.create(h, w, this.matrixType);

		Matrix backOuput = Matrix.create(h, w, this.matrixType);

		MatrixOperator op = input.getMatrixOperator();

		Matrix rightAnswer = output.copy(false);
		DDouble ceeOutputV = new DDouble();
		
		CalSoftmaxCEE cee = new CalSoftmaxCEE(input, output, rightAnswer, ceeOutputV, op, "");
		cee.setBackPropagation(backOuput);
		
		input.setStandardRandomValue(111);
		Random random = new Random(888);
		for(int i=0;i<rightAnswer.getHeight();i++) //SoftmaxCEE는 one-hot-encoding의 answerdata여야 올바른 backpropagation이 된다.
		{
			double rightA[] = new double[rightAnswer.getWidth()];
			for(int j=0;j<rightA.length;j++)rightA[j]=0;
			rightA[random.nextInt(rightA.length)] = 1.0;
			rightAnswer.setData(rightA, i);
		}
		
		double dx=0.0001;
		
		double[][] sdg = new double[h][w];
		
			for(int j=0;j<w;j++)
			{
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				int i=0;
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				double o1 = input.getData(1, j);
				input.setData(o1+dx, 1, j);
				cee.doCal(false);
				double v1 = ceeOutputV.value;
				
				System.out.println("["+i+","+j+"] cee:"+v1);
				System.out.println("["+i+","+j+"] input"+input);
				System.out.println("["+i+","+j+"] output"+output);
				System.out.println("-----------------------------------------------------------");
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				
				input.setData(o-dx, i, j);
				input.setData(o1-dx, 1, j);
				cee.doCal(false);
				double v2 = ceeOutputV.value;

				System.out.println("["+i+","+j+"] cee:"+v2);
				System.out.println("["+i+","+j+"] input"+input);
				System.out.println("["+i+","+j+"] output"+output);
				
				input.setData(o, i, j);
				input.setData(o1, 1, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
				System.out.println("["+i+","+j+"] grad:"+sdg[i][j]);
				System.out.println("=============================================================");
			}
		
		
		DZHelper.printArray(sdg);
		
		cee.doCal(false);
		cee.doBack(false);
		
		if(checkZeroMatrix(backOuput))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("Softmax and CEE backpropagation", backOuput, sdg);
		
		
		backOuput.release();
		rightAnswer.release();
		input.release();
		output.release();
		
		return ret;
	}
	
	private boolean dotTestMSE(int h, int w) throws Exception
	{
		boolean ret = false;
		
		
		Matrix input0 = Matrix.create(h, w, this.matrixType);
		Matrix input1 = Matrix.create(w, 1, this.matrixType);
		Matrix output = Matrix.create(h, 1, this.matrixType);
		Matrix outputMSE = Matrix.create(h, 1, this.matrixType);
		

		Matrix backOuput0 = Matrix.create(h, w, this.matrixType);
		Matrix backOuput1 = Matrix.create(w, 1, this.matrixType);
		Matrix backInput = Matrix.create(h, 1, this.matrixType);

		MatrixOperator op = input0.getMatrixOperator();

		Matrix rightAnswer = output.copy(false);
		
		input0.setStandardRandomValue(111);
		input1.setStandardRandomValue(222);
		rightAnswer.setStandardRandomValue(333);
		
		DDouble ceeOutputV = new DDouble();
		
		CalDot dot = new CalDot(input0, input1, output, op, "");
		dot.setBackPropagation(backOuput0, backOuput1, backInput);
		CalMeanSquaredError mse = new CalMeanSquaredError(output, outputMSE, rightAnswer, ceeOutputV, op, "");
		mse.setBackPropagation(backInput);
		
		input0.setStandardRandomValue(111);
		input1.setGuassianRandomValue(222);
		
		dot.doCal(false);
		mse.doCal(false);
		
		System.out.println("output:"+output);
		System.out.println("output1:"+ceeOutputV.value);
		
		double dx=0.0001;
		
		double[][] sdg = new double[input1.getHeight()][1];
		
		for(int i=0;i<input1.getHeight();i++)
		{
			for(int j=0;j<1;j++)
			{
				double o = input1.getData(i, j);
				input1.setData(o+dx, i, j);
				dot.doCal(false);
				mse.doCal(false);
				double v1 = ceeOutputV.value;
				//System.out.println("v1:"+v1+ " ceeinput0:"+output.getData(0, 0));
				
				input1.setData(o-dx, i, j);
				dot.doCal(false);
				mse.doCal(false);
				double v2 = ceeOutputV.value;
				//System.out.println("v2:"+v2+ " ceeinput1:"+output.getData(0, 0));
				
				//System.out.println("dy/dx = "+(v1-v2)/(2.0*dx));
				input1.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		DZHelper.printArray("sdg", sdg);
		
		dot.doCal(false);
		mse.doCal(false);
		//System.out.println("cee:"+ceeOutputV.value);
		mse.doBack(false);
		dot.doBack(false);
		//double grad = backOuput1.getData(r, c);
		//System.out.println("grad:"+grad);
		System.out.println("backoutput:"+backOuput1);
		
		if(checkZeroMatrix(backOuput1))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("dot product with single output backpropagation",backOuput1,sdg);
		
		outputMSE.release();
		rightAnswer.release();
		input0.release();
		input1.release();
		output.release();
		
		return ret;
	}
	
	private boolean CEE(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input = Matrix.create(h,w, this.matrixType);
		Matrix right = input.copy(false);
		
		Matrix backOutput = input.copy(false);
		MatrixOperator op = input.getMatrixOperator();
		
		input.setStandardRandomValue(123); //CEE에서는 input값이 반드시 0~1사이여야 한다. 즉, 가우시안분포를 사용할수 없다.
		right.setStandardRandomValue(321);
		
		DDouble ceeOutput = new DDouble();
		
		CalCrossEntoryErroy cee = new CalCrossEntoryErroy(input, right, ceeOutput, op, "");
		cee.setBackPropagation(backOutput);
		
		double dx=0.0001;
		double[][] sdg = new double[h][w];
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				cee.doCal(false);
				double v1 = ceeOutput.value;
				//System.out.println("v1:"+v1+ " ceeinput0:"+output.getData(0, 0));
				
				input.setData(o-dx, i, j);
				cee.doCal(false);
				double v2 = ceeOutput.value;
				//System.out.println("v2:"+v2+ " ceeinput1:"+output.getData(0, 0));
				
				//System.out.println("dy/dx = "+(v1-v2)/(2.0*dx));
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		//System.out.println("input:"+input);
		//System.out.println("right:"+right);
		//DZHelper.printArray("sdg:", sdg);
		cee.doBack(false);
		
		System.out.println("backoutput:"+backOutput);
		
		if(checkZeroMatrix(backOutput))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("CEE backpropagation",backOutput,sdg);
		
		input.release();
		backOutput.release();
		
		return ret;
	}
	
	private boolean MSE(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input = Matrix.create(h,w, this.matrixType);
		Matrix right = input.copy(false);
		Matrix outputMSE = input.copy(false);
		
		Matrix backOutput = input.copy(false);
		MatrixOperator op = input.getMatrixOperator();
		
		input.setStandardRandomValue(123);
		right.setStandardRandomValue(321);
		
		DDouble ceeOutput = new DDouble();
		
		CalMeanSquaredError mse = new CalMeanSquaredError(input, outputMSE, right, ceeOutput, op, "");
		mse.setBackPropagation(backOutput);
		
		double dx=0.0001;
		double[][] sdg = new double[h][w];
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				double o = input.getData(i, j);
				input.setData(o+dx, i, j);
				mse.doCal(false);
				double v1 = ceeOutput.value;
				//System.out.println("v1:"+v1+ " ceeinput0:"+output.getData(0, 0));
				
				input.setData(o-dx, i, j);
				mse.doCal(false);
				double v2 = ceeOutput.value;
				//System.out.println("v2:"+v2+ " ceeinput1:"+output.getData(0, 0));
				
				//System.out.println("dy/dx = "+(v1-v2)/(2.0*dx));
				input.setData(o, i, j);
				sdg[i][j] = (v1-v2)/(2.0*dx);
			}
		}
		
		//System.out.println("input:"+input);
		//System.out.println("right:"+right);
		System.out.println("output:"+outputMSE);
		DZHelper.printArray("sdg:", sdg);
		mse.doBack(false);
		
		System.out.println("backoutput:"+backOutput);
		
		if(checkZeroMatrix(backOutput))
		{
			System.out.println("matrix is zero");
			ret = true;
		}
		else ret = checkMatrix("MSE backpropagation",backOutput,sdg);
		
		outputMSE.release();
		input.release();
		backOutput.release();
		
		return ret;
	}
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		boolean ret = false;
		try {

			//테스트시에 되도록이면 Dimension이 2로 하는것은 좋지 않다.
			// 그 이유는 2인 경우, 평균에서 두개의 결과값의 거리가 똑같아서 변화의 여지가 상쇄에서 grad값이 0으로 나타날수 있다. 
			// 되도록이면 2개로 하지 말자.
			//ret |= sigmoidTest(2,2);
			//ret |= softmaxCEETest(2,2);
			//ret |= dotTest(2,2);
			//ret |= batchNormalizationSingleTest();
			//ret |= batchNormalizationTest(4, 5);
			//ret |= softmaxCEETest();
			
			//ret |= CEE(5,2);
			//ret |= MSE(2,2);
			//ret |= dotTestMSE(5,7);
			
			ret |= ReLUTest(2,2);
			
			
			return ret;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return true;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Back Propagation Test";
	}
	/*

	
	if(dz2layer)
	
	return false;
	*/

	@Override
	public void release() {
		// TODO Auto-generated method stub
		System.gc();
	}
}
