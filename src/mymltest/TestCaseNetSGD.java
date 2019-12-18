package mymltest;

import dolziplib.HomoLayer;
import dolziplib.matrix.Matrix;
import dolziplib.mnist.Mnist3Layer;

public class TestCaseNetSGD implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	
	public TestCaseNetSGD(int matrixType)
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
					return true;
				}
			}
		}
		System.out.println("OK");
		return false;
	}
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		try {
			boolean ret = false;
			Mnist3Layer m3 = new Mnist3Layer(true, 100, matrixType);
			m3.setParamDefault();
			m3.getGradBySGD();
	
			HomoLayer hl = (HomoLayer)m3.net.getLayer(0);
			Matrix checkIt = hl.wGrad;
			Matrix reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W1_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B1_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(1);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W2_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B2_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
			hl = (HomoLayer)m3.net.getLayer(2);
			checkIt = hl.wGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_W3_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
			checkIt = hl.bGrad;
			reference = checkIt.copy(false);
			reference.loadFromBinFile("mnist_100_grad_B3_matrix.bin");
			ret |= checkMatrix(checkIt, reference);
			
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
		return "Net SGD Test(100)";
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
