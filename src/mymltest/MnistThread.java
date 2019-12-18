package mymltest;

import java.util.Random;

import dolziplib.matrix.Matrix;
import dolziplib.mnist.Mnist;
import dolziplib.mnist.MnistNLayer;

public class MnistThread extends Thread{

	public int[] layerInfo = null;
	public double learningRate = 0.0;
	public MnistNLayer nlayer = null;
	public Random random = null;
	
	public MnistThread(Mnist mnist,Random random)
	{
		this.random = random;
	}

	public void setup()
	{
		this.setupLayerInfo();
	//	nlayer = new MnistNLayer(set, 100, Matrix.MATRIX_TYPE_AVX, layerMatrixWidth)
	}
	
	private void setupLayerInfo()
	{
		
	}
}
