package mymltest;

import com.sun.management.GarbageCollectorMXBean;

import dolziplib.matrix.Matrix;

public class TestCaseGrad implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	int testInputNum = 3;
	
	public TestCaseGrad(int matrixType)
	{
		this.matrixType = matrixType;
	}
	
	private static boolean checkMatrix(Matrix a, Matrix b)
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
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		
		try {
			DZ2Layer dz2layer = new DZ2Layer(Matrix.MATRIX_TYPE_JAVA);
			//dz2layer.setInputImageAll(false);
			dz2layer.setInputImage(testInputNum, true);
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
			boolean failed=false;
			if(checkMatrix(w1Grad,dz2layer.w1Grad)==false)failed=true;
			if(checkMatrix(w2Grad,dz2layer.w2Grad)==false)failed=true;
			if(checkMatrix(w3Grad,dz2layer.w3Grad)==false)failed=true;
			if(checkMatrix(b1Grad,dz2layer.b1Grad)==false)failed=true;
			if(checkMatrix(b2Grad,dz2layer.b2Grad)==false)failed=true;
			if(checkMatrix(b3Grad,dz2layer.b3Grad)==false)failed=true;
			
			return failed;
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Gradiant Backpropagation Test 1 input";
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
