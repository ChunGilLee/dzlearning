package mymltest;

import com.sun.management.GarbageCollectorMXBean;

import dolziplib.matrix.Matrix;

public class TestCaseAllInput implements TestCase{

	public int matrixType = Matrix.MATRIX_TYPE_JAVA;
	
	public TestCaseAllInput(int matrixType)
	{
		this.matrixType = matrixType;
	}
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		
		DZ2Layer dz2layer = new DZ2Layer(matrixType);
		dz2layer.setInputImageAll(false);
		dz2layer.doBatch(true);
		dz2layer.evaluation();
		
		if(dz2layer.getCorrectRate() != 0.9352)
		{
			System.out.println("Error : Correct Rate, Expected:0.9352 Result:"+dz2layer.getCorrectRate());
			return true;
		}
		if(dz2layer.crossErrorEntropy.value < 0.2245 || dz2layer.crossErrorEntropy.value > 0.2346)
		{
			System.out.println("Error : CEE, Expected:0.2245(Approxi.) Result:"+dz2layer.crossErrorEntropy.value);
			return true;
		}
		
		
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "All Input(10000) Mnist Test";
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
