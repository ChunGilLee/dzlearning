package dolziplib.matrix;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Mult;
import dolziplib.DZMath;

public class MatrixOperatorColt extends MatrixOperator{

	

	@Override
	public void dot(Matrix aT, Matrix bT, Matrix cT) throws Exception {
		// TODO Auto-generated method stub
		
		if(aT.getWidth() != bT.getHeight())
		{
			throw new Exception("first matrix width is not matched to second height. first width:"+aT.getWidth()+", second height:"+bT.getHeight());
		}
		if(aT.getHeight() != cT.getHeight())
		{
			throw new Exception("first matrix height is not matched to third height. first height:"+aT.getHeight()+", second height:"+cT.getHeight());			
		}
		
		MatrixColt a = (MatrixColt)aT;
		MatrixColt b = (MatrixColt)bT;
		
		Algebra al = new Algebra();
		DoubleMatrix2D ret =  al.mult(a.coltMatrix, b.coltMatrix);
		MatrixColt newOne = new MatrixColt(a.row, b.columns,ret);
		newOne.release();
		
		MatrixColt c = (MatrixColt)cT;
		c.setData(newOne.getAllData());
		
		return;
		
	}


	@Override
	protected void add(Matrix aT, Matrix bT, Matrix cT, final boolean sub)
	{
		MatrixColt a = null;
		MatrixColt b = null;
		if(aT.getHeight() != bT.getHeight() && aT.getHeight() == 1 && bT.getHeight() != 1)
		{
			a = (MatrixColt)bT.copy();
			a.setData(0.0);
			double[] firstRow = aT.getRow(0);
			for(int i=0;i<a.getHeight();i++)
			{
				a.setData(firstRow, i);
			}
			b = (MatrixColt)bT;
		}
		else if(aT.getHeight() != bT.getHeight() && aT.getHeight() != 1 && bT.getHeight() == 1)
		{
			b = (MatrixColt)aT.copy();
			b.setData(0.0);
			double[] firstRow = bT.getRow(0);
			for(int i=0;i<b.getHeight();i++)
			{
				b.setData(firstRow, i);		
			}
			a = (MatrixColt)aT;
		}
		else
		{
			a = (MatrixColt)aT;
			b = (MatrixColt)bT;
		}

		if(a.getHeight() != b.getHeight() || a.getWidth() != b.getWidth())
		{
			throw new RuntimeException("first matrix height,width("+a.getHeight()+","+a.getWidth()+
					") is not matched to second height,width("+b.getHeight()+","+b.getWidth()+")");
		}

		
		MatrixColt ret = (MatrixColt)cT;
		ret.setData(a.getAllData());
		
		
		//System.out.printf("a h:%d w:%d b h:%d w:%d c h:%d w:%d\n",aT.getHeight(),aT.getWidth(),b.getHeight(),b.getWidth(),
		//		ret.getHeight(),ret.getWidth());
		
		DoubleDoubleFunction addFunction = new DoubleDoubleFunction() {
			
			@Override
			public double apply(double arg0, double arg1) {
				// TODO Auto-generated method stub
				if(sub) return arg0-arg1;
				else return arg0+arg1;
			}
		};
		
		ret.coltMatrix.assign(b.coltMatrix,addFunction);
		
	}


	public double getMax(MatrixColt x)
	{
		DoubleDoubleFunction ddf = new DoubleDoubleFunction() {
						
			@Override
			public double apply(double arg0, double arg1) {
				// TODO Auto-generated method stub
				if(arg0>arg1)return arg0;
				else return arg1;
			}
		};
		
		DoubleFunction df = new DoubleFunction() {
			
			@Override
			public double apply(double arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}
		};
		
		return x.coltMatrix.aggregate(ddf, df);
	}


	private static double sigmoid(double x)
	{
		return 1.0/(1+Math.exp(-x));
	}

	@Override
	public Matrix get1DMatrix(Matrix a) {
		// TODO Auto-generated method stub
		try {
			MatrixColt newOne = new MatrixColt(1, a.getHeight()*a.getWidth());
			double data[][] = a.getAllData();
			int index=0;
			for(int i=0;i<a.getHeight();i++)
			{
				for(int j=0;j<a.getWidth();j++)
				{
					newOne.setData(data[i][j], 0, index);
					index++;
				}
			}
			return newOne;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

}
