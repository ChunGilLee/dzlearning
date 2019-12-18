package mymltest;

import dolziplib.matrix.Matrix;
import dolziplib.mnist.Mnist3Layer;

public class TestCaseNetAllInput implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	
	public TestCaseNetAllInput(int matrixType)
	{
		this.matrixType = matrixType;
	}
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		
		Mnist3Layer m3 = new Mnist3Layer(false, 10000, this.matrixType);
		m3.setParamDefault();
		m3.forwoard();
		m3.evaluation();
		
		if(m3.getCorrectRate() != 0.9352)
		{
			System.out.println("Error : Correct Rate, Expected:0.9352 Result:"+m3.getCorrectRate());
			return true;
		}
		if(m3.getCEE() < 0.2245 || m3.getCEE() > 0.2346)
		{
			System.out.println("Error : CEE, Expected:0.2245(Approxi.) Result:"+m3.getCEE());
			return true;
		}
		
		
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Net All Input(10000) Mnist Test";
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
