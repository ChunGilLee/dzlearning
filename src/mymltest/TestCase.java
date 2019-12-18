package mymltest;

import dolziplib.matrix.Matrix;

public interface TestCase {

	public boolean doTest();
	public String getName();
	public void release();
}
