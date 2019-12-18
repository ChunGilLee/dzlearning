package dolziplib.paramupdator;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class ParamUpdatorAdaGrad implements ParamUpdator{

	public double learningRate = 0.1;
	
	Matrix hMatrix = null;
	Matrix tmp = null;
	
	public ParamUpdatorAdaGrad(double learningRate)
	{
		this.learningRate = learningRate;
	}
	
	
	@Override
	public void applyGrad(Matrix a, Matrix b) {
		// TODO Auto-generated method stub
		
		MatrixOperator op = a.getMatrixOperator();
		if(hMatrix==null)
		{
			hMatrix = b.copy(false);
			hMatrix.setData(0.0);
		}
		if(tmp==null)
		{
			tmp = b.copy(false);
		}
		
		{
			/*
			int w = hMatrix.getWidth();
			int h = hMatrix.getHeight();
			try {
				for(int i=0;i<h;i++)
				{
					for(int j=0;j<w;j++)
					{
						double v = b.getData(i, j);
						
						double v1 = hMatrix.getData(i, j);
						hMatrix.setData(v1 + v*v, i, j);
					}
				}
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
			*/
			try {
				op.mult(b, b, tmp);
				op.add(hMatrix,tmp,hMatrix);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		
		/*
		
		int w = a.getWidth();
		int h = a.getHeight();
		try {
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<w;j++)
				{
					double v = a.getData(i, j);
					double v1 = v - learningRate * b.getData(i, j) / (Math.sqrt(hMatrix.getData(i, j)) + 1e-7);
					a.setData(v1, i, j);
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		*/
		op.pow(hMatrix,0.5, tmp);
		op.add(tmp, 1e-4 , tmp);
		op.div(b, tmp, tmp);
		op.mult(tmp, learningRate*(-1.0), tmp);
		op.add(a, tmp, a);
		
	}


	@Override
	public ParamUpdator copy() {
		// TODO Auto-generated method stub
		return new ParamUpdatorAdaGrad(learningRate);
	}


	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(hMatrix!=null)
		{
			hMatrix.release();
			hMatrix = null;
		}
		if(tmp!=null)
		{
			tmp.release();
			tmp = null;
		}
	}

}
