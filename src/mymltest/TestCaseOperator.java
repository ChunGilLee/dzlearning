package mymltest;

import java.util.Calendar;

import dolziplib.DZMath;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class TestCaseOperator implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	
	public TestCaseOperator(int matrixType)
	{
		this.matrixType = matrixType;
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
	
	private boolean sumCheck(Matrix a)
	{
		Matrix b = Matrix.create(a.getHeight(), 1, a.getMatrixType());
		MatrixOperator op = a.getMatrixOperator();
		try {
			op.sum(a, b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
		for(int i=0;i<a.getHeight();i++)
		{
			double aV = DZMath.getSum(a.getRow(i));
			double bV = b.getData(i, 0);
			double delta = aV - bV;
			if(delta<0)delta *= -1;
			double compare = 0.0001;
			if(aV>100)compare = 0.001;
			if(aV>10000)compare = 0.01;
			if(delta>compare)
			{
				System.out.printf("sum check failed %f <=> %f\n",aV,bV);
				return true;
			}
		}
		return false;
	}
	
	private boolean testMatrix(int h, int w) throws Exception
	{
		boolean ret = false;
		
		Matrix input1Tmp = Matrix.create(1, w,  this.matrixType); //for testing 1-height input matrix
		
		System.out.println(" test h:"+h+" w:"+w);
		// add matrix test
		Matrix input0 = Matrix.create(h, w, this.matrixType);
		MatrixOperator op = input0.getMatrixOperator();
		System.out.println("Test Matrix:"+input0.getMatrixTypeName());
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input0.setData(i+j, i, j);
		Matrix input1 = Matrix.create(h, w, this.matrixType);
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input1.setData(i+j+1, i, j);
		Matrix output = Matrix.create(h, w, this.matrixType);
		op.add(input0, input1, output);
		double [][] right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = i+j+i+j+1;
		ret |= checkMatrix("m + m same size",output,right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		
		//add matrix width 1 height input
		input1Tmp.setStandardRandomValue(1234);
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = input0.getData(i, j)+input1Tmp.getData(0, j);
		op.add(input0, input1Tmp, output);
		ret |= checkMatrix("m + m(1) size",output,right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		op.add(input1Tmp, input0, output);
		ret |= checkMatrix("m(1) + m size",output,right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();

		
		// add scalar test
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input0.setData(i+j/10, i, j);
		op.add(input0, 1.234, input0);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = i+j/10 + 1.234;
		ret |= checkMatrix("m + s",input0,right);
		//ret |= sumCheck(input0); //check something dirty in dummy 
		input0.durtyCheck();

		// mmult test
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input0.setData((i+j)*1.23, i, j);
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input1.setData((i+j)*3.21, i, j);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = ((i+j)*1.23) * ((i+j)*3.21);
		op.mult(input0, input1, output);
		ret |= checkMatrix("m mmult m", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		input1Tmp.setStandardRandomValue(8811);
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = input0.getData(i, j) * input1Tmp.getData(0, j);
		//System.out.println("input0:"+input0);
		//System.out.println("input1Tmp:"+input1Tmp);
		op.mult(input0, input1Tmp, output);
		//System.out.println("output:"+output);
		ret |= checkMatrix("m mmult m(1)", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		output.setData(0.0);
		op.mult(input1Tmp, input0, output);
		ret |= checkMatrix("m(1) mmult m", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		
		
		// add scalar + sum test
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)input0.setData((i+j)*1.23-10.0, i, j);
		op.add(input0, 3.32, input0);
		Matrix output2 = Matrix.create(h, 1, input0.getMatrixType());
		op.sum(input0, output2);
		right = new double[h][1];
		for(int i=0;i<h;i++)right[i][0] = DZMath.getSum(input0.getRow(i));
		ret |= checkMatrix("m + s and sum", output2, right);
		//ret |= sumCheck(output2); //check something dirty in dummy 
		output2.durtyCheck();
		output2.release();
		
		//pow
		input0.setStandardRandomValue(0);
		input1.setStandardRandomValue(1);
		input0.setData(2.1, 0, 0);
		input1.setData(5.5, 0, 0);
		input0.setData(2.1, 0, 1);
		input1.setData(-5.5, 0, 1);
		input0.setData(2.1, 0, 2);
		op.pow(input0, input1, output);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = Math.pow(input0.getData(i, j), input1.getData(i, j));
		ret |= checkMatrix("pow", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 	
		output.durtyCheck();
		
		//pow scalar
		input0.setStandardRandomValue(2);
		op.pow(input0, 0.5 ,output);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = Math.sqrt(input0.getData(i, j));
		ret |= checkMatrix("pow(scalr)", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 	
		output.durtyCheck();

		//div
		input0.setStandardRandomValue(3);
		input1.setStandardRandomValue(4);
		input0.setData(0, 0, 0);
		input1.setData(1e-4, 0, 0);
		op.div(input0, input1, output);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = input0.getData(i, j) / input1.getData(i, j);
		ret |= checkMatrix("div", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 		
		output.durtyCheck();
		
		//sub
		input0.setStandardRandomValue(5);
		input1.setGuassianRandomValue(6);
		op.sub(input0, input1, output);
		right = new double[h][w];
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = input0.getData(i, j) - input1.getData(i, j);
		ret |= checkMatrix("m - m same size", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 
		output.durtyCheck();
		input1Tmp.setStandardRandomValue(4321);
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = input0.getData(i, j) - input1Tmp.getData(0, j);
		op.sub(input0, input1Tmp, output);
		ret |= checkMatrix("m - m(1) ", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy 	
		output.durtyCheck();
		for(int i=0;i<h;i++)for(int j=0;j<w;j++)right[i][j] = -input0.getData(i, j) + input1Tmp.getData(0, j);
		op.sub(input1Tmp, input0, output);
		ret |= checkMatrix("m(1) - m ", output, right);
		//ret |= sumCheck(output); //check something dirty in dummy
		output.durtyCheck();
		
		
		//dot
		Matrix input2Tmp = Matrix.create(input0.getWidth(), 5, input0.getMatrixType());
		Matrix input3Tmp = Matrix.create(input0.getHeight(), input2Tmp.getWidth(), input0.getMatrixType());
		input0.setStandardRandomValue(7);
		input2Tmp.setStandardRandomValue(8);
		op.dot(input0, input2Tmp, input3Tmp);
		right = new double[input3Tmp.getHeight()][input3Tmp.getWidth()];
		for(int i=0;i<input0.getHeight();i++)
		{
			for(int k=0;k<input2Tmp.getWidth();k++)
			{
				double sum = 0;
				for(int j=0;j<input0.getWidth();j++)
				{
					sum += input0.getData(i, j) * input2Tmp.getData(j, k);
				}
				right[i][k] = sum;
			}
		}
		//System.out.println("a:"+input0);
		//System.out.println("b:"+input2Tmp);
		//System.out.println("c:"+input3Tmp);
		ret |= checkMatrix("dot ", input3Tmp, right);
		//ret |= sumCheck(input3Tmp); //check something dirty in dummy
		input3Tmp.durtyCheck();
		
		
		
		input3Tmp.release();
		input2Tmp.release();
		input1Tmp.release();
		input0.release();
		input1.release();
		output.release();
		
		return ret;
	}
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		try {
			boolean ret = false;
			ret |= testMatrix(2,3);
			ret |= testMatrix(8,8);
			ret |= testMatrix(3,21);
			
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
		return "Operator Test)";
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
