package dolziplib.paramupdator;

import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class ParamUpdatorAdam implements ParamUpdator{

	public double learningRate = 0.1;
	public double beta1 = 0.9;
	public double beta2 = 0.999;
	int iter=0;
	
	Matrix m = null;
	Matrix v = null;
	Matrix tmp0 = null;
	
	public ParamUpdatorAdam(double learningRate,double beta1, double beta2)
	{
		this.learningRate = learningRate;
		this.beta1 = beta1;
		this.beta2 = beta2;
	}
	
	
	@Override
	public void applyGrad(Matrix a, Matrix b) {
		// TODO Auto-generated method stub
		
		MatrixOperator op = a.getMatrixOperator();
		if(m==null)
		{
			m = b.copy(false);
			m.setData(0.0);
		}
		if(v==null)
		{
			v = b.copy(false);
			v.setData(0.0);
		}
		if(tmp0==null)
		{
			tmp0 = b.copy(false);
		}

		
		this.iter++;
		double lrT = this.learningRate * Math.sqrt(1.0 - Math.pow(this.beta2,this.iter))/(1.0 - Math.pow(this.beta1, this.iter));
		
		
		{
			op.sub(b, m, tmp0);
			op.mult(tmp0, (1.0-beta1),tmp0);
			op.add(m, tmp0, m);
			
			op.pow(b, 2.0 , tmp0);
			op.sub(tmp0, v, tmp0);
			op.mult(tmp0, (1.0-beta2), tmp0 );
			op.add(v, tmp0,v);
			
			op.pow(v,0.5,tmp0);
			op.add(tmp0, 1e-4, tmp0);
			op.div(m, tmp0, tmp0);
			op.mult(tmp0, lrT, tmp0);
			op.sub(a,tmp0,a);
		}
	}


	@Override
	public ParamUpdator copy() {
		// TODO Auto-generated method stub
		return new ParamUpdatorAdam(learningRate,this.beta1,this.beta2);
	}


	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(m!=null)
		{
			m.release();
			m = null;
		}
		if(v!=null)
		{
			v.release();
			v = null;
		}
		if(tmp0!=null)
		{
			tmp0.release();
			tmp0 = null;
		}
	}

}
