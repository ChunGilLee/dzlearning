package mymltest;

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import dolziplib.Batchwork;
import dolziplib.DDouble;
import dolziplib.DZMath;
import dolziplib.DZPython;
import dolziplib.HomoLayer;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.calculation.CalSoftmaxCEE;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixAVX;
import dolziplib.matrix.MatrixColt;
import dolziplib.matrix.MatrixOperator;
import dolziplib.mnist.Mnist;
import dolziplib.mnist.Mnist3Layer;
import dolziplib.mnist.MnistImg;
import dolziplib.mnist.MnistNLayer;
import dolziplib.param.ParameterSpace;
//import javafx.scene.transform.MatrixType;

public class Main {

	
	static void printDoubleArray(double[] input)
	{
		for(int i=0;i<input.length;i++)
		{
			System.out.printf("%f ", input[i]);
		}
		System.out.println();
	}
	
	/*
	static void test0()
	{
		DoubleMatrix2D m2d = DoubleFactory2D.dense.make(new double[][] {{1.0,2.0},{3.0,4.0}});
		System.out.println(m2d);
		
	}
	
	static void test1()
	{
		double[][] Ad = {{1,2},{3,4}};
		double[][] Bd = {{5,6},{7,8}};
		
		DoubleMatrix2D A = DoubleFactory2D.dense.make(Ad);
		DoubleMatrix2D B = DoubleFactory2D.dense.make(Bd);
		
		Algebra algebra = new Algebra();
		DoubleMatrix2D C = algebra.mult(A, B); // vector dot
		System.out.println(C);
		
	}
	
	static void test2()
	{
		DoubleMatrix2D A = DoubleFactory2D.dense.make(new double[] {0.3,0.7,1.1},1);
		
		System.out.println(A);
		DoubleMatrix2D Z = DZMath.sigmoid(A);
		
		System.out.println(Z);
	}
	
	static DoubleMatrix2D test3Network(double x0, double x1)
	{
		DoubleMatrix2D W1 = DoubleFactory2D.dense.make(new double[][] {{0.1,0.3,0.5},{0.2,0.4,0.6}});
		DoubleMatrix2D B1 = DoubleFactory2D.dense.make(new double[] {0.1,0.2,0.3},1);
		DoubleMatrix2D W2 = DoubleFactory2D.dense.make(new double[][] {{0.1,0.4},{0.2,0.5},{0.3,0.6}});
		DoubleMatrix2D B2 = DoubleFactory2D.dense.make(new double[] {0.1,0.2},1);
		DoubleMatrix2D W3 = DoubleFactory2D.dense.make(new double[][] {{0.1,0.3},{0.2,0.4}});
		DoubleMatrix2D B3 = DoubleFactory2D.dense.make(new double[] {0.1,0.2},1);
		
		DoubleMatrix2D x = DoubleFactory2D.dense.make(new double[] {x0,x1},1);
		
		Algebra algebra = new Algebra();
		DoubleMatrix2D a1 = DZMath.add(algebra.mult(x, W1),B1);
		DoubleMatrix2D z1 = DZMath.sigmoid(a1);
		DoubleMatrix2D a2 = DZMath.add(algebra.mult(z1, W2),B2);
		DoubleMatrix2D z2 = DZMath.sigmoid(a2);
		DoubleMatrix2D a3 = DZMath.add(algebra.mult(z2, W3),B3);

		return a3;
	}
	static void test3()
	{
		DoubleMatrix2D ret = test3Network(1.0, 0.5);
		System.out.println(ret);
	}
	

	
	static void test5()
	{
		System.out.println("loading train data...");
		Mnist mnistTrain = Mnist.getFromFile("mnist_train.csv");
		System.out.println("completed imgs:"+mnistTrain.imgs.size());
		System.out.println("loading train data...");
		Mnist mnistTest = Mnist.getFromFile("mnist_test.csv");
		System.out.println("completed imgs:"+mnistTest.imgs.size());
		
		//System.out.println("total train imgs:"+mnistTrain.imgs.size());
		System.out.println(mnistTest.imgs.get(0));
	}
	*/
	
	static void test0()
	{
		int matrixType = Matrix.MATRIX_TYPE_COLT;
		try {
			Matrix a = Matrix.create(4, 2, matrixType);
			a.setData(new double[][] {{1,2},{3,4},{5,6},{7,8}});
			Matrix b = Matrix.create(1, 2, matrixType);
			b.setData(new double[][] {{1,2}});
			MatrixOperator op = a.getMatrixOperator();
			Matrix c = a.copy(false);
			op.add(a, b, c);
			System.out.println(a);
			System.out.println(b);
			System.out.println(c);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test1()
	{
		int matrixType = Matrix.MATRIX_TYPE_AVX;
		try {
			Matrix a = Matrix.create(2, 3, matrixType);
			a.setData(new double[][] {{1,2,3},{4,5,6}});
			Matrix b = Matrix.create(3, 1, matrixType);
			b.setData(new double[][] {{1},{2},{3}});
			MatrixOperator op = a.getMatrixOperator();
			Matrix c = Matrix.create(2, 1, a.getMatrixType());
			op.dot(a, b, c);
			System.out.println(c);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static void test4()
	{
		double[][] input = new double[][] {{0.3,2.9,4.0} , {1010,1000,990}};
		
		Matrix matrix = Matrix.create(2, 3, Matrix.MATRIX_TYPE_COLT);
		try {
			matrix.setData(input);
			MatrixOperator op = matrix.getMatrixOperator();
			Matrix soft = matrix.copy(false);
			op.getSoftMax(matrix, soft);
			System.out.println(soft);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static long showTimeMS(String name,long previousTick)
	{
		long current = System.nanoTime();
		System.out.printf("%s : %f us\n",name,(current-previousTick)/1000.0);
		current = System.nanoTime();
		return current;
	}
	
	private static Matrix predict(Matrix W1,Matrix W2,Matrix W3,
			Matrix B1,Matrix B2, Matrix B3, Matrix input)
	{
		/*
		boolean showPerformance = true;
		MatrixOperator op = W1.getMatrixOperator();
		try {
			long current = System.nanoTime();
			long start = current;
			Matrix dot1 = op.dot(input, W1);
			if(showPerformance)current = showTimeMS("dot1", current);
			Matrix A1 = op.add(dot1,B1);
			if(showPerformance)current = showTimeMS("add1", current);
			Matrix Z1 = op.getSigmoid(A1);
			if(showPerformance)current = showTimeMS("sigmoid1", current);
			Matrix dot2 = op.dot(Z1, W2);
			if(showPerformance)current = showTimeMS("dot2", current);
			Matrix A2 = op.add(dot2,B2);
			if(showPerformance)current = showTimeMS("add2", current);
			Matrix Z2 = op.getSigmoid(A2);
			if(showPerformance)current = showTimeMS("sigmoid2", current);
			Matrix dot3 = op.dot(Z2, W3);
			if(showPerformance)current = showTimeMS("dot3", current);
			Matrix A3 = op.add(dot3,B3);
			if(showPerformance)current = showTimeMS("add3", current);

			Matrix ret = op.getSoftMax(A3);
			if(showPerformance)current = showTimeMS("softmax", current);
			

		return ret;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	*/
		return null;
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
	
	static void test6() //single image test
	{
		int matrixType = Matrix.MATRIX_TYPE_COLT;
		
		try {
			System.out.println("loading mnist...");
			//Mnist mnistTrain = Mnist.getFromFile("mnist_train.csv");
			//Mnist mnistTest = Mnist.getFromFile("mnist_test.csv");
			
			//mnistTrain.saveToBinFile("mnist_train.bin");
			//mnistTest.saveToBinFile("mnist_test.bin");
			
			Mnist mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
			Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
			
			System.out.println("loading weight...");
	
			Matrix w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType);
			System.out.println("w1 matrix w:"+w1.getWidth()+" h:"+w1.getHeight());
			Matrix w2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType);
			System.out.println("w2 matrix w:"+w2.getWidth()+" h:"+w2.getHeight());
			Matrix w3 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType);
			System.out.println("w3 matrix w:"+w3.getWidth()+" h:"+w3.getHeight());

			Matrix b1 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType);
			System.out.println("b1 matrix w:"+b1.getWidth()+" h:"+b1.getHeight());
			Matrix b2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType);
			System.out.println("b2 matrix w:"+b2.getWidth()+" h:"+b2.getHeight());
			Matrix b3 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType);
			System.out.println("b3 matrix w:"+b3.getWidth()+" h:"+b3.getHeight());

			/*		
			try {
				w1.saveToBinFile("W1_matrix.bin");
				w2.saveToBinFile("W2_matrix.bin");
				w3.saveToBinFile("W3_matrix.bin");
				b1.saveToBinFile("B1_matrix.bin");
				b2.saveToBinFile("B2_matrix.bin");
				b3.saveToBinFile("B3_matrix.bin");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			int testingIndex=211;
			MnistImg img = mnistTest.imgs.get(testingIndex);
			Matrix input = img.getMatrix(matrixType);
			MatrixOperator op = input.getMatrixOperator();
			input = (MatrixColt)op.get1DMatrix(input);
			//DoubleMatrix2D input = DoubleFactory2D.dense.make(DZMath.from2DTo1D(mnistTest.imgs.get(testingIndex).dataNormalized),1);
			System.out.println("target image:"+mnistTest.imgs.get(testingIndex));
			System.out.println("predict...");
			Matrix output = predict(w1, w2, w3, b1, b2, b3, input);
			System.out.println("the number : "+maxPossibleNum(output.getRow(0)));
			System.out.println("output:"+output);
			System.out.println("correct:"+img.getCorrectAnswerMatrix(matrixType));
			System.out.println("CEE:"+op.crossEntropyError(output, img.getCorrectAnswerMatrix(matrixType)));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static boolean matrixCheck(String name,Matrix A, Matrix B)
	{
		if(A.getHeight()!=B.getHeight())
		{
			System.out.println(name+" height is not matched A height:"+A.getHeight()+",B height:"+B.getHeight());
			return false;
		}
		if(A.getWidth()!=B.getWidth())
		{
			System.out.println(name+" width is not matched A width:"+A.getWidth()+",B width:"+B.getWidth());
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
	
	static void test6_1() //single image test sigmoid
	{
		int matrixType1 = Matrix.MATRIX_TYPE_COLT;
		int matrixType2 = Matrix.MATRIX_TYPE_AVX;
		
		MnistImg img = null;
		
		try {
			System.out.println("loading mnist...");
			//Mnist mnistTrain = Mnist.getFromFile("mnist_train.csv");
			//Mnist mnistTest = Mnist.getFromFile("mnist_test.csv");
			
			//mnistTrain.saveToBinFile("mnist_train.bin");
			//mnistTest.saveToBinFile("mnist_test.bin");
					
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_COLT);
			dz2layer.setInputSingleImage(1, false);
			dz2layer.doBatch(false);
			
			System.out.println("loading weight...");
	
			Matrix w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType1);
			Matrix w2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType1);
			Matrix w3 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType1);

			Matrix b1 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType1);
			Matrix b2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType1);
			Matrix b3 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType1);

			Matrix w1_2 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType2);
			Matrix w2_2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType2);
			Matrix w3_2 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType2);

			Matrix b1_2 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType2);
			Matrix b2_2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType2);
			Matrix b3_2 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType2);

			/*		
			try {
				w1.saveToBinFile("W1_matrix.bin");
				w2.saveToBinFile("W2_matrix.bin");
				w3.saveToBinFile("W3_matrix.bin");
				b1.saveToBinFile("B1_matrix.bin");
				b2.saveToBinFile("B2_matrix.bin");
				b3.saveToBinFile("B3_matrix.bin");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			int testingIndex=1;
			img = dz2layer.mnistTest.imgs.get(testingIndex);
			Matrix input1 = img.getMatrix(matrixType1);
			Matrix input2 = img.getMatrix(matrixType2);
			MatrixOperator op1 = input1.getMatrixOperator();
			MatrixOperator op2 = input2.getMatrixOperator();
			input1 = op1.get1DMatrix(input1);
			input2 = op2.get1DMatrix(input2);
			//DoubleMatrix2D input = DoubleFactory2D.dense.make(DZMath.from2DTo1D(mnistTest.imgs.get(testingIndex).dataNormalized),1);
			//System.out.println("target image:"+mnistTest.imgs.get(testingIndex));
			System.out.println("predict...");
			Matrix output1 = null;
			Matrix output2 = null;
						
			try {
				
				/*
				Matrix dot1_2 = op2.dot(input2, w1_2);
				Matrix A1_2 = op2.add(dot1_2,b1_2);
				Matrix Z1_2 = op2.getSigmoid(A1_2);
				Matrix dot2_2 = op2.dot(Z1_2, w2_2);
				Matrix A2_2 = op2.add(dot2_2,b2_2);
				Matrix Z2_2 = op2.getSigmoid(A2_2);
				Matrix tmp_2 = op2.dot(Z2_2, w3_2);				
				Matrix A3_2 = op2.add(tmp_2,b3_2);
				Matrix ret_2 = op2.getSoftMax(A3_2);
				output2 = ret_2;
				
				Matrix dot1 = op1.dot(input1, w1);
				Matrix A1 = op1.add(dot1,b1);
				Matrix Z1 = op1.getSigmoid(A1);
				Matrix dot2 = op1.dot(Z1, w2);
				Matrix A2 = op1.add(dot2,b2);
				Matrix Z2 = op1.getSigmoid(A2);
				Matrix dot3 = op1.dot(Z2, w3);				
				Matrix A3 = op1.add(dot3,b3);
				Matrix ret = op1.getSoftMax(A3);
				output1 = ret;
				//printDoubleArray(output1.getRow(0));
			

				boolean chekcfailed = false;
				if(chekcfailed==false)matrixCheck("input",dz2layer.input,input1);
				if(chekcfailed==false)matrixCheck("dot1",dz2layer.dot1,dot1);
				if(chekcfailed==false)matrixCheck("A1",dz2layer.A1,A1);
				if(chekcfailed==false)matrixCheck("Z1",dz2layer.Z1,Z1);
				if(chekcfailed==false)matrixCheck("w2",dz2layer.w2,w2);
				if(chekcfailed==false)matrixCheck("dot2",dz2layer.dot2,dot2);
				if(chekcfailed==false)matrixCheck("A2",dz2layer.A2,A2);
				if(chekcfailed==false)matrixCheck("Z2",dz2layer.Z2,Z2);
				if(chekcfailed==false)matrixCheck("dot3",dz2layer.dot3,dot3);
				if(chekcfailed==false)matrixCheck("A3",dz2layer.A3,A3);
				if(chekcfailed==false)matrixCheck("ret",dz2layer.ret,ret);
				*/
				
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
			
			if(output1 !=null)System.out.println("the number(0) : "+maxPossibleNum(output1.getRow(0)));
			if(output2 !=null)System.out.println("the number(1) : "+maxPossibleNum(output2.getRow(0)));
			//System.out.println("output:"+output);
			Matrix correct = img.getCorrectAnswerMatrix(matrixType1);
			System.out.println("correct:"+correct);
			//System.out.println("CEE:"+op1.crossEntropyError(output1, img.getCorrectAnswerMatrix(matrixType1)));
			
			System.out.println(" cross entroy error(0):"+DZMath.crossEntropyError(output1.getRow(0), correct.getRow(0)));
			System.out.println(" cross entroy error(1):"+DZMath.crossEntropyError(output2.getRow(0), correct.getRow(0)));
			System.out.println(" cross entroy error(2):"+DZMath.crossEntropyError(dz2layer.ret.getRow(0), correct.getRow(0)));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	static void test6_2()
	{
		DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
		dz2layer.setInputSingleImage(1, false);
		dz2layer.doBatch(false);
		int testingIndex=1;
		MnistImg img = dz2layer.mnistTest.imgs.get(testingIndex);
		Matrix correct = img.getCorrectAnswerMatrix(Matrix.MATRIX_TYPE_AVX);
		System.out.println("the number(3) : "+maxPossibleNum(dz2layer.ret.getRow(0)));
		try {
			System.out.println(" cross entroy error(3):"+DZMath.crossEntropyError(dz2layer.ret.getRow(0), correct.getRow(0)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test7() //all image test
	{
		int matrixType = Matrix.MATRIX_TYPE_COLT;
		
		try {
			System.out.println("loading mnist...");
			Mnist mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
			Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
			
			System.out.println("loading weight...");
	
			Matrix w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType);
			System.out.println("w1 matrix w:"+w1.getWidth()+" h:"+w1.getHeight());
			Matrix w2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType);
			System.out.println("w2 matrix w:"+w2.getWidth()+" h:"+w2.getHeight());
			Matrix w3 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType);
			System.out.println("w3 matrix w:"+w3.getWidth()+" h:"+w3.getHeight());
	
			Matrix b1 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType);
			System.out.println("b1 matrix w:"+b1.getWidth()+" h:"+b1.getHeight());
			Matrix b2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType);
			System.out.println("b2 matrix w:"+b2.getWidth()+" h:"+b2.getHeight());
			Matrix b3 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType);
			System.out.println("b3 matrix w:"+b3.getWidth()+" h:"+b3.getHeight());
	
					
			System.out.println("predict...");
			int totalTestingCount = 0;
			int correct = 0;
			for(MnistImg img:mnistTest.imgs)
			{
				//System.out.println("totalCounting:"+totalTestingCount);
				
				Matrix input = img.getMatrix(matrixType);
				MatrixOperator op = input.getMatrixOperator();
				input = (Matrix)op.get1DMatrix(input);
				
				Matrix output = predict(w1, w2, w3, b1, b2, b3, input);
				
				double[] outputArray = output.getRow(0);
				if(maxPossibleNum(outputArray) == img.label)correct++;
				else 
				{
					System.out.println("error index :"+totalTestingCount+" label:"+img.label+" wrong answer:"+maxPossibleNum(outputArray));
				}
				/*
				else if(totalTestingCount>10)
				{
					System.out.println("target image:"+img);
					System.out.println("prediction:"+maxPossibleNum(outputArray));
					for(int i=0;i<outputArray.length;i++)
					{
						System.out.printf("%d:%f ",i,outputArray[i]);
					}
					System.out.println();
					//break;
					
				}
				*/
				totalTestingCount++;
			}
			System.out.println("total:"+totalTestingCount+" correct:"+correct);
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	
	static void test7_1() //all image test in one time.
	{
		int matrixType = Matrix.MATRIX_TYPE_AVX;
		
		try {
			System.out.println("loading mnist...");
			Mnist mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
			Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
			
			System.out.println("loading weight...");
	
			Matrix w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType);
			System.out.println("w1 matrix w:"+w1.getWidth()+" h:"+w1.getHeight());
			Matrix w2 = Matrix.loadFromBinFile("W2_matrix.bin", matrixType);
			System.out.println("w2 matrix w:"+w2.getWidth()+" h:"+w2.getHeight());
			Matrix w3 = Matrix.loadFromBinFile("W3_matrix.bin", matrixType);
			System.out.println("w3 matrix w:"+w3.getWidth()+" h:"+w3.getHeight());
	
			Matrix b1 = Matrix.loadFromBinFile("B1_matrix.bin", matrixType);
			System.out.println("b1 matrix w:"+b1.getWidth()+" h:"+b1.getHeight());
			Matrix b2 = Matrix.loadFromBinFile("B2_matrix.bin", matrixType);
			System.out.println("b2 matrix w:"+b2.getWidth()+" h:"+b2.getHeight());
			Matrix b3 = Matrix.loadFromBinFile("B3_matrix.bin", matrixType);
			System.out.println("b3 matrix w:"+b3.getWidth()+" h:"+b3.getHeight());
	
					
			System.out.println("predict...");
			
			Matrix correctAnswer = mnistTest.getCorrectAnswerMatrix(mnistTest.imgs.size(), matrixType);
			Matrix input = mnistTest.getInputMatrix(mnistTest.imgs.size(), matrixType);
			MatrixOperator op = input.getMatrixOperator();
			long start = System.nanoTime();
			Matrix output = predict(w1, w2, w3, b1, b2, b3, input);
			long ceestart = System.nanoTime();
			double CEE = op.crossEntropyError(output, correctAnswer);
			long end = System.nanoTime();
			System.out.println("cee:"+(end-ceestart)/1000.0);
			
			System.out.println("evaluation...");
			int totalTestingCount = output.getHeight();
			int correct = 0;
			for(int i=0;i<totalTestingCount;i++)
			{
				double[] outputArray = output.getRow(i);
				MnistImg img = mnistTest.imgs.get(i);
				if(maxPossibleNum(outputArray) == img.label)correct++;
				else 
				{
				//	System.out.println("error index :"+i+" label:"+img.label+" wrong answer:"+maxPossibleNum(outputArray));
				}
			}
			System.out.println("input matrix type:"+input.getMatrixTypeName()+" h:"+input.getHeight()+" w:"+input.getWidth());
			System.out.println("output matrix type:"+output.getMatrixTypeName()+" h:"+output.getHeight()+" w:"+output.getWidth());
			System.out.printf("predict time: %f us\n",(end-start)/1000.0);
			System.out.println("total:"+totalTestingCount+" correct:"+correct +" cross_entropy_error:"+CEE);
			
			//System.out.println("211th data----");
			//printDoubleArray(output.getRow(211));
			
			/*
			int totalTestingCount = 0;
			int correct = 0;
			for(MnistImg img:mnistTest.imgs)
			{
				System.out.println("totalCounting:"+totalTestingCount);
				
				Matrix input = img.getMatrix(matrixType);
				MatrixOperator op = input.getMatrixOperator();
				input = (MatrixColt)op.get1DMatrix(input);
				
				Matrix output = predict(w1, w2, w3, b1, b2, b3, input);
				
				double[] outputArray = output.getRow(0);
				if(maxPossibleNum(outputArray) == img.label)correct++;

				totalTestingCount++;
			}
			
			System.out.println("total:"+totalTestingCount+" correct:"+correct);
			*/
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static void test7_2()
	{
		DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
		dz2layer.setInputImageAll(false);
		//dz2layer.setInputImage(1, true);
		dz2layer.doBatch(true);
		dz2layer.evaluation();
		
		System.out.println("w1 "+dz2layer.w1.getHeight()+" x " + dz2layer.w1.getWidth());
		System.out.println("w2 "+dz2layer.w2.getHeight()+" x " + dz2layer.w2.getWidth());
		System.out.println("w3 "+dz2layer.w3.getHeight()+" x " + dz2layer.w3.getWidth());
		System.out.println("b1 "+dz2layer.b1.getHeight()+" x " + dz2layer.b1.getWidth());
		System.out.println("b2 "+dz2layer.b2.getHeight()+" x " + dz2layer.b2.getWidth());
		System.out.println("b3 "+dz2layer.b3.getHeight()+" x " + dz2layer.b3.getWidth());
		
	}
	
	public static void test8()
	{
		double ans[] = {0.1,0.05,0.6,0.0,0.05,0.1,0.0,0.1,0.0,0.0};
		double right[] = {0,0,1,0,0,0,0,0,0,0};
		
		double error=0;
		try {
			error = DZMath.meanSquaredError(ans, right);
			System.out.printf("mean squared error : %f\n",error);
			error = DZMath.crossEntropyError(ans, right);
			System.out.printf("cross entropy error : %f\n",error);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test9()
	{
		int matrixType = Matrix.MATRIX_TYPE_AVX;
		int sampling = 100;
		
		try {
			System.out.println("loading mnist...");
			Mnist mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
			Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
			
			Matrix input = mnistTrain.getInputMatrix(sampling, matrixType);
			Matrix correctAnswer = mnistTrain.getCorrectAnswerMatrix(sampling, matrixType);
			
			TwoLayerNet layer2 = new TwoLayerNet(input,correctAnswer, 50, 0.01, matrixType);
			layer2.doCurrentParam();
			/*
			System.out.println("cee :"+layer2.CEE+", accuracy:"+layer2.accuracy);
			long startT = Calendar.getInstance().getTimeInMillis();
			layer2.moveNext();
			long end  = Calendar.getInstance().getTimeInMillis();
			System.out.println("duration:"+(end-startT)+"ms");
			layer2.doCurrentParam();
			System.out.println("cee :"+layer2.CEE+", accuracy:"+layer2.accuracy);
			*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test10()
	{
		try {
		Matrix t = Matrix.create(2, 10, Matrix.MATRIX_TYPE_COLT);
		double[][] tD = new double[][] {
				{0,0,1,0,0,0,0,0,0,0}
				,{0,0,1,0,0,0,0,0,0,0}
		};
		t.setData(tD);
		Matrix y = Matrix.create(2, 10, Matrix.MATRIX_TYPE_COLT);
		double[][] yD = new double[][] {{
				0.1,
				0.05,
				0.6,
				0.0,
				0.05,
				0.1,
				0.0,
				0.1,
				0.0,0.0}
		,{0.1,0.05,0.1,0.0,0.05,0.1,0.0,0.6,0.0,0.0}
		};
		System.out.println("d:"+yD.length);
		y.setData(yD);
		
		MatrixOperator op = t.getMatrixOperator();
		System.out.println("CEE:"+op.crossEntropyError(y, t));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static long dotProductTest(int matrixType)
	{
		try {
			Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");		
			Matrix w1 = Matrix.loadFromBinFile("W1_matrix.bin", matrixType);
			Matrix input = mnistTest.getInputMatrix(100, matrixType);
			
			//System.out.println("w1 ma:"+w1.getMatrixTypeName());
			//System.out.println("input ma:"+input.getMatrixTypeName());
			
			MatrixOperator op = w1.getMatrixOperator();
			long start = System.nanoTime();
			Matrix a1 = Matrix.create(input.getHeight(), w1.getWidth(), input.getMatrixType());
			long end = System.nanoTime();
			//System.out.println("time:"+(end-start)+" ns");
			return (end-start);
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return 0;
	}
	public static void test11()
	{
		long sum = 0;
		for(int i=0;i<100;i++)
		{
			sum += dotProductTest(Matrix.MATRIX_TYPE_JAVA);
			
		}
		System.out.println("java time:"+(sum/100)+" ns");
		
		sum = 0;
		for(int i=0;i<100;i++)
		{
			sum += dotProductTest(Matrix.MATRIX_TYPE_COLT);
			
		}
		System.out.println("colt time:"+(sum/100)+" ns");
		
		sum = 0;
		for(int i=0;i<100;i++)
		{
			sum += dotProductTest(Matrix.MATRIX_TYPE_AVX);
			
		}
		System.out.println("avx time:"+(sum/100)+" ns");
	}
	

	public static void test12()
	{
		//Batchwork bw = new Batchwork();
		//MatrixAVX m = new MatrixAVX();
		
		/*
		String property = System.getProperty("java.library.path");
		StringTokenizer parser = new StringTokenizer(property, ";");
		while (parser.hasMoreTokens()) {
		    System.out.println(parser.nextToken());
		    }
		    */
	}
	
	public static void test13()
	{
		double answer[] =      {0.1, 0.2, 0.1, 0.3 ,0.1, 0.2, 0.1, 0.2, 0.3};
		double answerRight[] = {0.0, 0.0, 0.0, 0.1 ,0.0, 0.0, 0.0, 0.0, 0.5};
		
		try {
			Matrix answerM = null;
			Matrix answerRightM  =null;
			MatrixOperator op = null;
			DDouble d = null;
			int size = answer.length; 
			int type = Matrix.MATRIX_TYPE_JAVA;
			
			
			answerM = Matrix.create(1, size, type);
			answerM.setData(answer, 0);		
			answerRightM = Matrix.create(1, size, type);
			answerRightM.setData(answerRight, 0);
			op = answerM.getMatrixOperator();
			d = new DDouble();
			op.crossEntropyError(answerM, answerRightM, d);
			System.out.println(answerM.getMatrixTypeName()+"CEE:"+d.value);
			
			type = Matrix.MATRIX_TYPE_AVX;
			answerM = Matrix.create(1, size, type);
			answerM.setData(answer, 0);		
			answerRightM = Matrix.create(1, size, type);
			answerRightM.setData(answerRight, 0);
			op = answerM.getMatrixOperator();
			d = new DDouble();
			op.crossEntropyError(answerM, answerRightM, d);
			System.out.println(answerM.getMatrixTypeName()+"CEE:"+d.value);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test14()
	{
		double answer[] =      {0.1, 0.2, 0.1, 0.3 ,0.1, 0.2, 0.1, 0.2, 0.3};
		
		try {
			Matrix answerM = null;
			MatrixOperator op = null;
			DDouble d = null;
			int size = answer.length; 
			int type = Matrix.MATRIX_TYPE_JAVA;
			
			
			answerM = Matrix.create(1, size, type);
			answerM.setData(answer, 0);		
			op = answerM.getMatrixOperator();
			Matrix ret = answerM.copy(false);
			op.getSoftMax(answerM,ret);
			System.out.println(ret);
			
			type = Matrix.MATRIX_TYPE_AVX;
			answerM = Matrix.create(1, size, type);
			answerM.setData(answer, 0);		
			op = answerM.getMatrixOperator();
			op.getSoftMax(answerM,ret);
			System.out.println(ret);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test20()
	{
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setRandomInputImage(50, true);
			//dz2layer.setInputSingleImage(1, true);
			//dz2layer.setInputImage(1, true);
			System.out.println(" total number of elements:"+dz2layer.getTotalChangElementSize());
			dz2layer.initMatrixValues();
			dz2layer.doBatch(false);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value);
			long gradTime=0;
			int iteration = 1000;
			for(int i=0;i<iteration;i++)
			{
				
				long st = System.nanoTime();
				dz2layer.getAllGrad();
				long et = System.nanoTime();
				long durationInSec = (et-st)/1000000000;
				System.out.printf("[%d/%d] duration : %d sec. rest time %d sec(%f min)\n"
						,i,iteration,durationInSec,durationInSec*(iteration-i),
						(double)(durationInSec*(iteration-i)) / 60.0);
				
				if(dz2layer.checkNaN())
				{
					dz2layer.input.saveToBinFile("error_input.bin");
					dz2layer.correctAnswer.saveToBinFile("error_correct_answer.bin");
					dz2layer.w1.saveToBinFile("error_w1.bin");
					dz2layer.w2.saveToBinFile("error_w2.bin");
					dz2layer.w3.saveToBinFile("error_w3.bin");
					dz2layer.b1.saveToBinFile("error_b1.bin");
					dz2layer.b2.saveToBinFile("error_b2.bin");
					dz2layer.b3.saveToBinFile("error_b3.bin");
					break;
				}
				dz2layer.applyGrad(0.01);
				dz2layer.doBatch(false);
				System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
				if(Double.isNaN(dz2layer.crossErrorEntropy.value))
				{
					dz2layer.input.saveToBinFile("error_input.bin");
					dz2layer.correctAnswer.saveToBinFile("error_correct_answer.bin");
					dz2layer.w1.saveToBinFile("error_w1.bin");
					dz2layer.w2.saveToBinFile("error_w2.bin");
					dz2layer.w3.saveToBinFile("error_w3.bin");
					dz2layer.b1.saveToBinFile("error_b1.bin");
					dz2layer.b2.saveToBinFile("error_b2.bin");
					dz2layer.b3.saveToBinFile("error_b3.bin");
					break;
				}
				dz2layer.setNextRandomImage();
			}
		
			//DZ2Layer fullTest = dz2layer.getFullTest();
			//fullTest.doBatch(false);
			//fullTest.evaluation();
			
			dz2layer.w1.saveToBinFile("result_w1.bin");
			dz2layer.w2.saveToBinFile("result_w2.bin");
			dz2layer.w3.saveToBinFile("result_w3.bin");
			dz2layer.b1.saveToBinFile("result_b1.bin");
			dz2layer.b2.saveToBinFile("result_b2.bin");
			dz2layer.b3.saveToBinFile("result_b3.bin");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void checkNaN(Matrix m)
	{
		System.out.print("checking matrix "+m.getName()+" ...");
		for(int i=0;i<m.getHeight();i++)
		{
			for(int j=0;j<m.getWidth();j++)
			{
				if(Double.isNaN(m.getData(i, j)))
				{
					System.out.printf("[%d,%d] is NaN\n",i,j);
					return;
				}
			}
		}
		System.out.println("OK");
	}
	
	static void test21()
	{
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			//dz2layer.setRandomInputImage(10, true);
			dz2layer.setInputImage(10, true);
			dz2layer.initMatrixValues(true);
			dz2layer.doBatch(false);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
			
			/*
			checkNaN(dz2layer.input);
			checkNaN(dz2layer.w1);
			checkNaN(dz2layer.dot1);
			checkNaN(dz2layer.b1);
			checkNaN(dz2layer.A1);
			checkNaN(dz2layer.Z1);
			checkNaN(dz2layer.w2);
			checkNaN(dz2layer.dot2);
			checkNaN(dz2layer.b2);
			checkNaN(dz2layer.A2);
			checkNaN(dz2layer.Z2);
			checkNaN(dz2layer.w3);
			checkNaN(dz2layer.dot3);
			checkNaN(dz2layer.b3);
			checkNaN(dz2layer.A3);
			checkNaN(dz2layer.ret);		
			*/
			
			System.out.println("A1 5,14 => "+dz2layer.A1.getData(5, 14));
			System.out.println("Z1 5,14 => "+dz2layer.Z1.getData(5, 14));
			
			//dz2layer.getAllGrad();
			
			if(dz2layer.checkNaN())
			{
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static double getGrad(Matrix input,Matrix output, int row, int column)
	{
		double grad0 = Double.NaN;
		MatrixOperator op = input.getMatrixOperator();
		double v = input.getData(row, column);
		try {
			input.setData(v+0.0001,row,column);
			op.getSigmoid(input,output);
			op.getSigmoid(output,output);
			double t0 = output.getData(row, column);
			input.setData(v-0.0001,row,column);
			op.getSigmoid(input,output);
			op.getSigmoid(output,output);
			double t1 = output.getData(row, column);
			grad0 = (t0-t1)/0.0002;
			input.setData(v,row,column);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return grad0;
	}
	static void test22()
	{
		Matrix input = Matrix.create(1, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix output = Matrix.create(1, 3, Matrix.MATRIX_TYPE_JAVA);
		
		try {
			MatrixOperator op = input.getMatrixOperator();
			input.setData(-0.4, 0, 0);
			input.setData(0.01, 0, 1);
			input.setData(0.8, 0, 2);
			System.out.println("input :"+input);
			op.getSigmoid(input,output);
			System.out.println("output :"+output);
			op.getSigmoid(output,output);			
			System.out.println("output :"+output);

			System.out.println("grad0:"+getGrad(input,output,0,0));
			
			op.getSigmoid(input,output);
			double bbb = ((1-0.401312)*0.401312) * ((1-0.599003)*0.599003);
			System.out.println("aaa:"+bbb);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void test23()
	{
		Matrix input = Matrix.create(1, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix output = Matrix.create(1, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix right = Matrix.create(1, 3, Matrix.MATRIX_TYPE_JAVA);
		
		try {
			MatrixOperator op = input.getMatrixOperator();
			input.setData(0.1, 0, 0);
			input.setData(0.3, 0, 1);
			input.setData(0.2, 0, 2);
			right.setData(0.0, 0, 0);
			right.setData(1.0, 0, 1);
			right.setData(0.0, 0, 2);
			
			System.out.println("input :"+input);
			op.getSoftMax(input, output);
			double cee = op.crossEntropyError(output, right);
			System.out.println("CEE:"+cee+" output :"+output);
			
			int row = 0;
			int column =1;
			double v = input.getData(row, column);
			try {
				input.setData(v+0.0001,row,column);
				op.getSoftMax(input, output);
				double t0 = op.crossEntropyError(output, right);
				input.setData(v-0.0001,row,column);
				op.getSoftMax(input, output);
				double t1 = op.crossEntropyError(output, right);
				double grad0 = (t0-t1)/0.0002;
				input.setData(v,row,column);
				System.out.println("grad:"+grad0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			double grad1 = (output.getData(row, column)-right.getData(row, column)) / 10.0;
			System.out.println("grad1:"+grad1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test24()
	{
		Matrix input = Matrix.create(2, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix output0 = Matrix.create(2, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix output = Matrix.create(2, 3, Matrix.MATRIX_TYPE_JAVA);
		Matrix right = Matrix.create(2, 3, Matrix.MATRIX_TYPE_JAVA);
		
		try {
			MatrixOperator op = input.getMatrixOperator();
			input.setData(0.1, 0, 0);
			input.setData(0.3, 0, 1);
			input.setData(0.2, 0, 2);
			input.setData(0.5, 1, 0);
			input.setData(0.1, 1, 1);
			input.setData(0.3, 1, 2);
			right.setData(0.0, 0, 0);
			right.setData(1.0, 0, 1);
			right.setData(0.0, 0, 2);
			right.setData(1.0, 1, 0);
			right.setData(0.0, 1, 1);
			right.setData(0.0, 1, 2);
			
			System.out.println("input :"+input);
			op.getSigmoid(input, output0);
			op.getSoftMax(output0, output);
			double cee = op.crossEntropyError(output, right);
			System.out.println("CEE:"+cee+" output :"+output);
			
			int row = 0;
			int column =1;
			double v = input.getData(row, column);
			try {
				input.setData(v+0.0001,row,column);
				op.getSigmoid(input, output0);
				op.getSoftMax(output0, output);
				double t0 = op.crossEntropyError(output, right);
				input.setData(v-0.0001,row,column);
				op.getSigmoid(input, output0);
				op.getSoftMax(output0, output);
				double t1 = op.crossEntropyError(output, right);
				double grad0 = (t0-t1)/0.0002;
				input.setData(v,row,column);
				System.out.println("grad:"+grad0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			double grad1 = (output.getData(row, column)-right.getData(row, column))/input.getHeight();
			grad1 = grad1*((1-output0.getData(row, column))*output0.getData(row, column));
			System.out.println("grad1:"+grad1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static boolean checkMatrix(Matrix a, Matrix b)
	{
		System.out.print("checking "+a.getName()+","+b.getName()+" ...");
		for(int i=0;i<a.getHeight();i++)
		{
			for(int j=0;j<a.getWidth();j++)
			{
				double delta = a.getData(i, j) - b.getData(i, j);
				//delta = delta/a.getData(i, j);
				if(delta<0)delta *= -1;
				if(delta>0.01)
				{
					System.out.printf("[%d,%d] %f <==> %f | %f\n",i,j,a.getData(i, j),b.getData(i, j),delta);
					return false;
				}
			}
		}
		System.out.println("OK");
		return true;
	}
	
	static void test25()
	{
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			//dz2layer.setInputImageAll(false);
			dz2layer.setInputImage(10, true);
			//dz2layer.initMatrixValues(false);
			dz2layer.doBatch(false);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
			dz2layer.getAllGrad();
			Matrix w1Grad = dz2layer.w1Grad.copy(true);
			Matrix w2Grad = dz2layer.w2Grad.copy(true);
			Matrix w3Grad = dz2layer.w3Grad.copy(true);
			Matrix b1Grad = dz2layer.b1Grad.copy(true);
			Matrix b2Grad = dz2layer.b2Grad.copy(true);
			Matrix b3Grad = dz2layer.b3Grad.copy(true);
			dz2layer.clearGrad();
			dz2layer.doBack();
			checkMatrix(w1Grad,dz2layer.w1Grad);
			checkMatrix(w2Grad,dz2layer.w2Grad);
			checkMatrix(w3Grad,dz2layer.w3Grad);
			checkMatrix(b1Grad,dz2layer.b1Grad);
			checkMatrix(b2Grad,dz2layer.b2Grad);
			checkMatrix(b3Grad,dz2layer.b3Grad);
			
			printDoubleArray(w1Grad.getRow(0));
			//printDoubleArray(dz2layer.w2Grad.getRow(0));
					
			//System.out.println(dz2layer.A3Grad);
			//System.out.println(b3Grad);
			//System.out.println(dz2layer.b3Grad);
			//System.out.println(dz2layer.b3Grad);
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static void test26()
	{
		TestCaseManager.doFullTest(Matrix.MATRIX_TYPE_AVX);
	}
	
	static void showMemoryStatus()
	{
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		System.out.println("free memory:"+freeMemory/(1024*1024));
		System.out.println("allocated memory:"+allocatedMemory/(1024*1024));
		System.out.println("max memory:"+maxMemory/(1024*1024));
		System.out.println("total memory:"+((freeMemory + (maxMemory - allocatedMemory))/(1024*1024)));
	}
	
	static void backpropagationFullTest()
	{
		Matrix w1 = null;
		Matrix w2 = null;
		Matrix w3 = null;
		Matrix b1 = null;
		Matrix b2 = null;
		Matrix b3 = null;
		
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setRandomInputImage(1000, true);
			//System.out.println("aaa:"+dz2layer.w1.getData(0, 0));
			dz2layer.initMatrixValues(false);
			dz2layer.doBatch(false);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
			int iteration = 1000;
			for(int i=0;i<iteration;i++)
			{
				dz2layer.doBack();
				dz2layer.applyGrad(0.1);
				dz2layer.doBatch(false);
				System.out.println("["+i+"/"+iteration+"] "+
						"w1(0,0)="+dz2layer.w1.getData(0, 0)+"("+(-0.0074)+") "+
						"CEE :"+
						dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());				
				dz2layer.setNextRandomImage();
				dz2layer.doBatch(false);
			}
			
			w1 = dz2layer.w1.copy();
			w2 = dz2layer.w2.copy();
			w3 = dz2layer.w3.copy();
			b1 = dz2layer.b1.copy();
			b2 = dz2layer.b2.copy();
			b3 = dz2layer.b3.copy();
			
			w1.saveToBinFile("best_w1.bin");
			w2.saveToBinFile("best_w2.bin");
			w3.saveToBinFile("best_w3.bin");
			
			b1.saveToBinFile("best_b1.bin");
			b2.saveToBinFile("best_b2.bin");
			b3.saveToBinFile("best_b3.bin");
			
			dz2layer = null;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.gc();
		
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setInputImageAll(false);
			dz2layer.w1.setData(w1.getAllData());
			dz2layer.w2.setData(w2.getAllData());
			dz2layer.w3.setData(w3.getAllData());
			dz2layer.b1.setData(b1.getAllData());
			dz2layer.b2.setData(b2.getAllData());
			dz2layer.b3.setData(b3.getAllData());
			dz2layer.doBatch(false);
			dz2layer.evaluation();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static void test28()
	{
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setInputImage(10000, true);
			//System.out.println("aaa:"+dz2layer.w1.getData(0, 0));
			dz2layer.initMatrixValues(false);
			dz2layer.doBatch(false);
			
			
			Matrix dot1 = dz2layer.dot1.copy(true);
			Matrix A1 = dz2layer.A1.copy(true);
			Matrix Z1 = dz2layer.Z1.copy(true);
			Matrix Z2 = dz2layer.Z2.copy(true);
			Matrix A3 = dz2layer.A3.copy(true);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
			
			//dz2layer.doBack();
			//dz2layer.getAllGrad();
			double v= dz2layer.w1.getData(200, 0);
			dz2layer.w1.setData(v + 0.1,200,0);
			dz2layer.doBatch(false);
			System.out.println("CEE :"+dz2layer.crossErrorEntropy.value+" CorrectRate:"+dz2layer.getCorrectRate());
			
			checkMatrix(dot1, dz2layer.dot1);
			checkMatrix(A1, dz2layer.A1);
			checkMatrix(Z1, dz2layer.Z1);

			System.out.printf("old A1[2,0]<=>%f Z1[2,0]=%f, new  A1[2,0]<=>%f Z1[2,0]=%f\n",
					A1.getData(2, 0), Z1.getData(2, 0), dz2layer.A1.getData(2, 0),dz2layer.Z1.getData(2, 0));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void test29()
	{
		Mnist mnistTrain = Mnist.getFromBinFile("mnist_train.bin");
		int count=0;
		for(MnistImg img:mnistTrain.imgs)
		{
			double data[] = img.getDataNormalized1D();
			if(data[0]!=0)
			{
				System.out.println("["+count+"] is not zero:"+data[0]);
			}
			count++;
		}
	}
	
	public static void test30()
	{
		
		try {
			Mnist3Layer m3 = new Mnist3Layer(true, 10, Matrix.MATRIX_TYPE_AVX);
			m3.setParamDefault();
			m3.getGradBySGD();

			HomoLayer hl = (HomoLayer)m3.net.getLayer(0);
			Matrix checkIt = hl.wGrad;
			Matrix reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W1_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B1_matrix.bin");
			checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(1);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W2_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B2_matrix.bin");
			checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(2);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W3_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B3_matrix.bin");
			checkMatrix(checkIt, reference);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setInputImage(10, true);
			dz2layer.getAllGrad();
			//dz2layer.doBatch(false);
			//dz2layer.evaluation();
			
			dz2layer.w1Grad.saveToBinFile("mnist_100_grad_W1_matrix.bin");
			dz2layer.w2Grad.saveToBinFile("mnist_100_grad_W2_matrix.bin");
			dz2layer.w3Grad.saveToBinFile("mnist_100_grad_W3_matrix.bin");

			dz2layer.b1Grad.saveToBinFile("mnist_100_grad_B1_matrix.bin");
			dz2layer.b2Grad.saveToBinFile("mnist_100_grad_B2_matrix.bin");
			dz2layer.b3Grad.saveToBinFile("mnist_100_grad_B3_matrix.bin");

			
			System.out.println(dz2layer.b3Grad.getSizeString());
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	static void test31()
	{
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_AVX);
			dz2layer.setInputImage(10, false);
			//System.out.println("aaa:"+dz2layer.w1.getData(0, 0));
			dz2layer.initMatrixValues(false);
			dz2layer.doBatch(false);
			
			
			System.out.println(dz2layer.input);
			System.out.println(dz2layer.correctAnswer);
			System.out.println(dz2layer.ret);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void test32()
	{
		
		try {
			Mnist3Layer m3 = new Mnist3Layer(true, 100, Matrix.MATRIX_TYPE_AVX);
			m3.setParamDefault();
			m3.forwoard();
			m3.backpropagation();
			
			
			HomoLayer hl = (HomoLayer)m3.net.getLayer(0);
			Matrix checkIt = hl.wGrad;
			Matrix reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W1_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B1_matrix.bin");
			checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(1);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W2_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B2_matrix.bin");
			checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(2);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W3_matrix.bin");
			checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B3_matrix.bin");
			checkMatrix(checkIt, reference);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void NETbackpropagationFullTest()
	{
		
		try {
			Mnist3Layer m3 = new Mnist3Layer(true, 1000, Matrix.MATRIX_TYPE_AVX);
			m3.runEBPLoop(1000,false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test33()
	{
		try {
			Matrix m = Matrix.create(2, 2, Matrix.MATRIX_TYPE_AVX);
			m.setData(new double[][] {{0, 1},{2, 3}});
			Matrix m2 = Matrix.create(2, 2, Matrix.MATRIX_TYPE_AVX);
			m2.setData(new double[][] {{2, 2},{3, 3}});

			MatrixOperator op = m.getMatrixOperator();
			op.mult(m, -2, m);
			System.out.println(m);
			
			op.add(m, m2, m);
			System.out.println(m);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test34()
	{
		
		try {
			Mnist3Layer m3 = new Mnist3Layer(true, 100, Matrix.MATRIX_TYPE_AVX);
			//m3.net.setMomentumUpdator(0.01,0.9);
			//m3.net.setAdaGradUpdator(0.01);
			m3.net.setNormalUpdator(0.01);
			long startT = System.nanoTime();
			m3.runEBPLoop(10000,false);
			long endT = System.nanoTime();
			System.out.printf("time;%d us\n",(endT-startT)/1000);
			//m3.storeParamToFile("normalupdator_100x3000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test35()
	{
		try {
			Mnist3Layer m3 = new Mnist3Layer(false, 10000, Matrix.MATRIX_TYPE_AVX);
			m3.loadParamFile("normalupdator_100x3000");
			m3.forwoard();
			m3.evaluation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void test36()
	{
		
		try {
			Mnist3Layer m3 = new Mnist3Layer(true, 1000, Matrix.MATRIX_TYPE_AVX);
			//m3.net.setMomentumUpdator(0.01,0.9);
			//m3.net.setAdaGradUpdator(0.01);
			m3.net.setNormalUpdator(0.01);
			long startT = System.nanoTime();
			m3.runEBPLoop(10000,false);
			long endT = System.nanoTime();
			System.out.printf("time;%d us\n",(endT-startT)/1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Matrix getTransposed(Matrix m)
	{
		try {
			Matrix ret = Matrix.create(m.getWidth(), m.getHeight(), m.getMatrixType());
			for(int i=0;i<ret.getHeight();i++)
			{
				for(int j=0;j<ret.getWidth();j++)
				{
					ret.setData(m.getData(j, i), i, j);
				}
			}
			return ret;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
		
	}
	
	public static void test37()
	{
		Matrix m = Matrix.create(1000, 2000, Matrix.MATRIX_TYPE_AVX);
		m.setStandardRandomValue(0);
		
		long startT = System.nanoTime();
		Matrix mTrans = getTransposed(m);
		long endT = System.nanoTime();
		System.out.printf(" dot back:%d\n",(endT-startT)/1000);
		
		Matrix mTrans1 = Matrix.create(m.getWidth(), m.getHeight(), m.getMatrixType());
		MatrixOperator op = mTrans1.getMatrixOperator();
		try {
			startT = System.nanoTime();
			op.transpose(m, mTrans1);
			endT = System.nanoTime();
			System.out.printf(" dot back:%d\n",(endT-startT)/1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		checkMatrix(mTrans, mTrans1);
		mTrans.release();
		mTrans1.release();

		startT = System.nanoTime();
		mTrans = getTransposed(m);
		endT = System.nanoTime();
		System.out.printf(" dot back:%d\n",(endT-startT)/1000);
		
		mTrans1 = Matrix.create(m.getWidth(), m.getHeight(), m.getMatrixType());
		op = mTrans1.getMatrixOperator();
		try {
			startT = System.nanoTime();
			op.transpose(m, mTrans1);
			endT = System.nanoTime();
			System.out.printf(" dot back:%d\n",(endT-startT)/1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		checkMatrix(mTrans, mTrans1);
		mTrans.release();
		mTrans1.release();

		
		m.release();

	}
	static void test38()
	{
		Mnist mnist = Mnist.getFromBinFile("mnist_train.bin");
		try {
			CountDownLatch latch = new CountDownLatch(2);
			//int layer[] = new int[] { 100,70, 50 };
			//int layer[] = null;
			int layer0[] = new int[] { 100,200, 70, 50 };
			MnistNLayer m0 = new MnistNLayer(mnist, 100, Matrix.MATRIX_TYPE_AVX,layer0);
			int layer1[] = new int[] { 100, 70, 50 };
			MnistNLayer m1 = new MnistNLayer(mnist, 100, Matrix.MATRIX_TYPE_AVX,layer1);
			//m3.net.setMomentumUpdator(0.01,0.9);
			//m3.net.setAdaGradUpdator(0.01);
			m0.net.setNormalUpdator(0.01);
			m1.net.setNormalUpdator(0.01);
			long startT = System.nanoTime();
			
			m0.runEBPLoop(100,false,latch);
			m1.runEBPLoop(100,false,latch);
			latch.await();
			long endT = System.nanoTime();
			System.out.printf("time:%d s\n",(endT-startT)/1000000000);
			System.out.printf("m0 CorrectRate:%f\n",m0.getAvgCorrectRate());
			System.out.printf("m1 CorrectRate:%f\n",m1.getAvgCorrectRate());
			
			m0.release();
			m1.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			CountDownLatch latch = new CountDownLatch(2);
			//int layer[] = new int[] { 100,70, 50 };
			//int layer[] = null;
			int layer0[] = new int[] { 100,200, 70, 50 };
			MnistNLayer m0 = new MnistNLayer(mnist, 100, Matrix.MATRIX_TYPE_AVX,layer0);
			int layer1[] = new int[] { 100, 70, 50 };
			MnistNLayer m1 = new MnistNLayer(mnist, 100, Matrix.MATRIX_TYPE_AVX,layer1);
			//m3.net.setMomentumUpdator(0.01,0.9);
			//m3.net.setAdaGradUpdator(0.01);
			m0.net.setNormalUpdator(0.01);
			m1.net.setNormalUpdator(0.01);
			long startT = System.nanoTime();
			
			m0.runEBPLoop(100,false,latch);
			m1.runEBPLoop(100,false,latch);
			latch.await();
			long endT = System.nanoTime();
			System.out.printf("time:%d s\n",(endT-startT)/1000000000);
			System.out.printf("m0 CorrectRate:%f\n",m0.getAvgCorrectRate());
			System.out.printf("m1 CorrectRate:%f\n",m1.getAvgCorrectRate());
			
			m0.release();
			m1.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test39()
	{
		try {
			Matrix m0 = Matrix.create(1000, 1000, Matrix.MATRIX_TYPE_AVX);
			MatrixAVX m1 = (MatrixAVX)Matrix.create(1000, 1000, Matrix.MATRIX_TYPE_AVX);
			Matrix m2 = Matrix.create(1000, 1000, Matrix.MATRIX_TYPE_AVX);
			
			m0.setStandardRandomValue(0);
			m1.setStandardRandomValue(1);
			
			MatrixOperator op = m0.getMatrixOperator();
			//m1.flip();
			
			long startT = System.nanoTime();
				//op.getSigmoid(m0, m2);
				op.dot(m0, m1, m2);
			long endT = System.nanoTime();
			System.out.printf("time:%d us\n",(endT-startT)/1000);
			
			m0.release();
			m1.release();
			m2.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test40()
	{
		ParameterSpace ps = ParameterSpace.createParamSpace();
		System.out.println(ps);
		//ps.printAllConditions();
	}
	
	//normal full test
	static void test41()
	{
		Mnist mnist = Mnist.getFromBinFile("mnist_train.bin");
		Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
		//Matrix
		try {
			//	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
			/*		this.init(100, Matrix.MATRIX_TYPE_AVX, width);
		
		this.iter = 1000;
		this.performanceCheck = false;
		
		Double learningRate = (Double) param.get("learning_rate");
		this.net.setNormalUpdator(learningRate.doubleValue());
		*/
			MnistNLayer m0 = new MnistNLayer(mnist, 100,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200});
			//m0.net.setNormalUpdator(0.1);
			//m0.net.setAdaGradUpdator(0.1);
			m0.net.setAdamUpdator(0.001, 0.9, 0.999);
			long startT = System.nanoTime();
			m0.runEBPLoop(10000, false,true);
			long endT = System.nanoTime();
			System.out.printf("time:%d s\n",(endT-startT)/1000000000);
			System.out.println("CorrectRate:"+m0.getAvgCorrectRate());			
			m0.storeParamToFile("test");
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			//	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
			/*		this.init(100, Matrix.MATRIX_TYPE_AVX, width);
		
		this.iter = 1000;
		this.performanceCheck = false;
		
		Double learningRate = (Double) param.get("learning_rate");
		this.net.setNormalUpdator(learningRate.doubleValue());
		*/
			MnistNLayer m0 = new MnistNLayer(mnistTest, 10000,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200});
			//m0.net.setNormalUpdator(0.1);
			m0.net.setAdaGradUpdator(0.1);
			m0.loadParamFile("test");
			Layer n = m0.net.getLayer(0);
			m0.forwoard();
			m0.evaluation();
			//System.out.println("CorrectRate:"+m0.getAvgCorrectRate());
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// with batch normalization
	static void test44()
	{
		Mnist mnist = Mnist.getFromBinFile("mnist_train.bin");
		Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
		//Matrix
		try {
			//	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
			/*		this.init(100, Matrix.MATRIX_TYPE_AVX, width);
		
		this.iter = 1000;
		this.performanceCheck = false;
		
		Double learningRate = (Double) param.get("learning_rate");
		this.net.setNormalUpdator(learningRate.doubleValue());
		*/
			MnistNLayer m0 = new MnistNLayer(mnist, 100,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200},true);
			//m0.net.setNormalUpdator(0.1);
			//m0.net.setAdaGradUpdator(0.1);
			m0.net.setAdamUpdator(0.001, 0.9, 0.999);
			long startT = System.nanoTime();
			//m0.runEBPLoop(1000, false,true);
			m0.runEBPLoop(1000, false,true);
			long endT = System.nanoTime();
			System.out.printf("time:%d s\n",(endT-startT)/1000000000);
			System.out.println("CorrectRate:"+m0.getAvgCorrectRate());			
			m0.storeParamToFile("test");
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			//	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
			/*		this.init(100, Matrix.MATRIX_TYPE_AVX, width);
		
		this.iter = 1000;
		this.performanceCheck = false;
		
		Double learningRate = (Double) param.get("learning_rate");
		this.net.setNormalUpdator(learningRate.doubleValue());
		*/
			MnistNLayer m0 = new MnistNLayer(mnistTest, 10000,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200},true);
			//m0.net.setNormalUpdator(0.1);
			m0.net.setAdaGradUpdator(0.1);
			m0.loadParamFile("test");
			m0.net.setInferenceMode();
			Layer n = m0.net.getLayer(0);
			m0.forwoard();
			m0.evaluation();
			//System.out.println("CorrectRate:"+m0.getAvgCorrectRate());
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// with batch normalization and MSE
	static void test45()
	{
		Mnist mnist = Mnist.getFromBinFile("mnist_train.bin");
		Mnist mnistTest = Mnist.getFromBinFile("mnist_test.bin");
		//Matrix
		try {
			MnistNLayer m0 = new MnistNLayer(mnist, 100,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200},true, LayerType.LAST_LAYER_TYPE_MSE);
			m0.net.setAdamUpdator(0.001, 0.9, 0.999);
			long startT = System.nanoTime();
			m0.runEBPLoop(1000, false,true);
			long endT = System.nanoTime();
			System.out.printf("time:%d s\n",(endT-startT)/1000000000);
			System.out.println("CorrectRate:"+m0.getAvgCorrectRate());			
			m0.storeParamToFile("test");
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			MnistNLayer m0 = new MnistNLayer(mnistTest, 10000,Matrix.MATRIX_TYPE_AVX, new int[] {150,200,200},true, LayerType.LAST_LAYER_TYPE_MSE);
			//m0.net.setNormalUpdator(0.1);
			m0.net.setAdaGradUpdator(0.1);
			m0.loadParamFile("test");
			m0.net.setInferenceMode();
			Layer n = m0.net.getLayer(0);
			m0.forwoard();
			m0.evaluation();
			
			m0.release();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void test42()
	{
		try {
			
			System.out.printf("aaa = %f\n",Math.exp(10));
			int h=2;
			int w=2;
			
			int matrixType = Matrix.MATRIX_TYPE_JAVA;
			Matrix i0 = Matrix.create(h, w, matrixType);
			i0.setData(new double[][] {{0.2,0.4},{0.5,0.1}});
			Matrix out0 = Matrix.create(h, w, matrixType);
			DDouble output = new DDouble();
			Matrix rightAnswer =  Matrix.create(h, w, matrixType);
			rightAnswer.setData(new double[][] {{0.0,1.0},{0.0,1.0}});
			CalSoftmaxCEE cal = new CalSoftmaxCEE(i0, out0, rightAnswer, output, i0.getMatrixOperator(), "test");
			
			Matrix backoutput = Matrix.create(h, w, matrixType);
			cal.setBackPropagation(backoutput);
			
			cal.doCal(true);
			System.out.println("ddoute :"+output.value);
			System.out.println("output :"+out0);

		
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	static void test43()
	{
		double[][] input0d = new double[][] { 
		{1.66107035, -0.47644565, 0.34451401, -0.81808048, -1.51276934} ,
		{-0.37442514, -1.40983093, -1.44646335, -1.12402618, 0.24039200} ,
		{-0.27398980, 0.88848662, 1.31969225, 0.66990471, -0.01347201} ,
		{-1.01265538, 0.99779028, -0.21774316, 1.27220201, 1.28584921},
		};

		double[][] input1d = new double[][] {
		{0.42479390, 0.61225915, 0.41319147, -0.71618503}, 
		{0.14318737, 0.11443800, 0.94854975, 0.18371667} ,
		{0.52171218, 0.31821257, 0.62643296, -0.18565236} ,
		{1.33182299, -1.39467990, -0.10795847, 1.65809202} ,
		{-0.46015459, 0.87836581, -0.78612369, 0.33252603}
		};
		
		int mt = Matrix.MATRIX_TYPE_AVX;
		Matrix i0 = Matrix.create(4, 5, mt);
		Matrix i1 = Matrix.create(5, 4, mt);
		Matrix o0 = Matrix.create(4, 4, mt);
		
		MatrixOperator op = i0.getMatrixOperator();
		try {
			i0.setData(input0d);
			i1.setData(input1d);
			op.dot(i0, i1, o0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		System.out.println(o0);
		i0.release();
		i1.release();
		o0.release();
	}
	
	public static void main(String args[])
	{
		//test0();
		//System.out.println("aaa");
		//test11();
		//test7_1();
		//test9();
		
		//test6_1();
	
		//test7_2();
		//test13();
		
		//test14();
		
		//test20();
		
		//backpropagationFullTest();
		//test25();
		
		//TestCaseManager.doFullTest(Matrix.MATRIX_TYPE_AVX);
		TestCaseManager.doMy1(Matrix.MATRIX_TYPE_JAVA);
		
		//test33();
		//NETbackpropagationFullTest();
		//test38();
		
		//test39();
		//test42();
		//test44();
		//test45();
		
		MatrixAVX.releaseAllAvxMatrixResource();
	}
}
