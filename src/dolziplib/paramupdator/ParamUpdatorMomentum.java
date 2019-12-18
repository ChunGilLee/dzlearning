package dolziplib.paramupdator;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class ParamUpdatorMomentum implements ParamUpdator{

	public double learningRate = 0.1;
	public double momentum = 0.9;
	
	Matrix vel = null;
	
	public ParamUpdatorMomentum(double learningRate, double momentum)
	{
		this.learningRate = learningRate;
		this.momentum = momentum;
	}
	
	
	@Override
	public void applyGrad(Matrix a, Matrix b) {
		// TODO Auto-generated method stub
		
		MatrixOperator op = a.getMatrixOperator();
		if(vel==null)
		{
			vel = b.copy(true);
			op.mult(vel, -learningRate,vel);
		}
		else
		{
			int w = vel.getWidth();
			int h = vel.getHeight();
			try {
				for(int i=0;i<h;i++)
				{
					for(int j=0;j<w;j++)
					{
						double v = vel.getData(i, j);
						double v1 = (momentum * v) - (learningRate * b.getData(i, j));
						vel.setData(v1, i, j);
					}
				}
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		int w = a.getWidth();
		int h = a.getHeight();
		try {
			for(int i=0;i<h;i++)
			{
				for(int j=0;j<w;j++)
				{
					double v = a.getData(i, j);
					double v1 = v + vel.getData(i, j);
					a.setData(v1, i, j);
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	@Override
	public ParamUpdator copy() {
		// TODO Auto-generated method stub
		return new ParamUpdatorMomentum(learningRate,momentum);
	}


	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(vel!=null)
		{
			vel.release();
			vel = null;
		}
	}

}
