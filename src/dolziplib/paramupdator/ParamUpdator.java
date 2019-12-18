package dolziplib.paramupdator;

import dolziplib.matrix.Matrix;

public interface ParamUpdator {

	public void applyGrad(Matrix a,Matrix b);
	public ParamUpdator copy();
	public void release();
}
