package dolziplib.paramupdator;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class ParamUpdatorNormal implements ParamUpdator{

	public double learningRate = 0;
	
	public ParamUpdatorNormal(double learningRate)
	{
		this.learningRate = learningRate;
	}
	
	@Override
	public void applyGrad(Matrix a, Matrix b) {
		// TODO Auto-generated method stub
		
		//System.out.println("update lr:"+this.learningRate);
		
		Matrix tmp = b.copy(true);
		MatrixOperator op = tmp.getMatrixOperator();
		op.mult(tmp, -learningRate,tmp);
		try {
			op.add(a, tmp, a);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tmp.release();
		
		
		/*
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
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		*/
	}

	@Override
	public ParamUpdator copy() {
		// TODO Auto-generated method stub
		return new ParamUpdatorNormal(this.learningRate);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

}
