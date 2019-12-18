package dolziplib.mnist;

import java.util.Calendar;
import java.util.Random;

import dolziplib.DZConfig;
import dolziplib.Layer.LayerType;
import dolziplib.Net;
import dolziplib.matrix.Matrix;

public class Mnist3Layer {

	Mnist choosen = null;
	int matrixType = Matrix.MATRIX_TYPE_AVX;
	Mnist mnist = null;
	public Net net;
	Matrix input = null;
	Matrix rightAnswer = null;
	Random r = null;
	
	public Mnist3Layer(boolean trainSet, int numOfInput, int matrixType)
	{
		r = new Random(Calendar.getInstance().getTimeInMillis());
		this.matrixType = matrixType;
		Mnist choosen = new Mnist();
		if(trainSet)
		{
			mnist = Mnist.getFromBinFile("mnist_train.bin");
		}
		else
		{
			mnist = Mnist.getFromBinFile("mnist_test.bin");
		}
		int count=0;
		for(MnistImg img:mnist.imgs)
		{
			if(count>=numOfInput)break;
			choosen.imgs.add(img);
			count++;
		}
		
		input = Matrix.create(choosen.imgs.size(), choosen.getWidth(), matrixType);
		input.setName("DataInput Matrix");
		rightAnswer = Matrix.create(choosen.imgs.size(), choosen.getAnswerWidth(), matrixType);
		rightAnswer.setName("CorrectAnswer Matrix");
		
		try {
			
			choosen.getInputMatrix(numOfInput, input);
			choosen.getCorrectAnswerMatrix(numOfInput, rightAnswer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		net = new Net(new DZConfig());
		net.addFirstNeuron(input, 50, new LayerType());
		net.addMidNeuron(100, new LayerType());
		net.addLastNeuron(rightAnswer, new LayerType());
	}
	
	
	
	public void nextRandomInput()
	{
		Mnist choosen = new Mnist();
		int mnistSize = this.mnist.imgs.size();
		MnistImg[] imgs = this.mnist.imgs.toArray(new MnistImg[0]);
		int index=0;
		for(int i=0;i<input.getHeight();i++)
		{
			index = r.nextInt(mnistSize);
			choosen.imgs.add(imgs[index]);
		}
				
		try {
			
			choosen.getInputMatrix(input.getHeight(), input);
			choosen.getCorrectAnswerMatrix(rightAnswer.getHeight(), rightAnswer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setParamDefault()
	{
		net.loadParamFromFile();
	}
	public void setParamInit()
	{
		net.setInitParam();
	}
	
	public void forwoard()
	{
		try {
			net.forward();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void backpropagation()
	{
		try {
			net.backpropagation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void evaluation()
	{
		try {
			net.evaluation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double getCorrectRate()
	{
		return this.net.correctRate;
	}
	public double getCEE()
	{
		return this.net.getCEE();
	}
	
	public void getGradBySGD()
	{
		this.net.getGradBySGD();
	}
	
	public void applyGrad()
	{
		this.net.applyGrad();
	}
	
	public void runEBPLoop(int iter, boolean performanceCheck)
	{
		this.setParamInit();
		this.forwoard();
		long startT=0;
		long endT=0;
		for(int i=0;i<iter;i++)
		{
			if(performanceCheck)startT = System.nanoTime();
			this.backpropagation();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("backpro %d us\n",(endT-startT)/1000);
			if(performanceCheck)startT = System.nanoTime();
			this.applyGrad();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("applyGrad %d us\n",(endT-startT)/1000);
			if(performanceCheck)startT = System.nanoTime();
			this.forwoard();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("forward %d us\n",(endT-startT)/1000);
			//System.out.printf("[%d/%d]",i,iter);
			startT = System.nanoTime();
			//this.evaluation();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("eval %d us\n",(endT-startT)/1000);
			if(performanceCheck)startT = System.nanoTime();
			this.nextRandomInput();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("netxRandom %d us\n",(endT-startT)/1000);
			this.forwoard();
		}
	}
	
	public void storeParamToFile(String prefix)
	{
		net.storeParamToFile(prefix, false, false);
	}
	
	public void loadParamFile(String prefix)
	{
		net.loadParamFromFile(prefix, false, false);		
	}
}


