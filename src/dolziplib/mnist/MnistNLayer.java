package dolziplib.mnist;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.Learner;
import dolziplib.Net;
import dolziplib.ScoreBoard;
import dolziplib.matrix.Matrix;
import dolziplib.param.ParameterSpace;

public class MnistNLayer implements Runnable{

	Mnist choosen = null;
	int matrixType = Matrix.MATRIX_TYPE_AVX;
	Mnist mnist = null;
	public Net net;
	Matrix input = null;
	Matrix rightAnswer = null;
	Random r = null;
	int layerMatrixWidth[] = null;
	double last10CorrectRate[] = new double[10];
	int last10CorrectRateIndex=0;
	
	private void addCorrectRate(double rate)
	{
		last10CorrectRate[last10CorrectRateIndex] = rate;
		last10CorrectRateIndex++;
		if(last10CorrectRateIndex>=last10CorrectRate.length)last10CorrectRateIndex=0;
	}
	
	ScoreBoard scoreboard = null;
	String name = null;
	String saveFolder = null;
	
	public MnistNLayer(Mnist set, Map<String,Object> param, ScoreBoard scoreboard,String name,String saveFolder)
	{
		mnist = set;
		this.param = param;
		this.scoreboard = scoreboard;
		this.name = name;
		this.saveFolder = saveFolder;
	}
	
	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
	{
		mnist = set;
		this.init(numOfInput, matrixType, layerMatrixWidth, false, LayerType.LAST_LAYER_TYPE_CEE);
	}
	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[], boolean batchNorm)
	{
		mnist = set;
		this.init(numOfInput, matrixType, layerMatrixWidth, batchNorm, LayerType.LAST_LAYER_TYPE_CEE);
	}
	public MnistNLayer(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[], boolean batchNorm, int lastLayerType)
	{
		mnist = set;
		this.init(numOfInput, matrixType, layerMatrixWidth, batchNorm, lastLayerType);
	}
	
	private void init(int numOfInput, int matrixType, int layerMatrixWidth[], boolean batchNorm ,int layerLayerType)
	{
		if(layerMatrixWidth==null)layerMatrixWidth = new int[] {50,100}; //default
		this.layerMatrixWidth = layerMatrixWidth;
		
		r = new Random(Calendar.getInstance().getTimeInMillis());
		this.matrixType = matrixType;
		Mnist choosen = new Mnist();

		
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
		
		DZConfig config = new DZConfig();
		config.putBoolean("batch_norm", batchNorm);
		
		net = new Net(config);
		net.addFirstNeuron(input, layerMatrixWidth[0],new LayerType());
		for(int i=1;i<layerMatrixWidth.length;i++)net.addMidNeuron(layerMatrixWidth[i],new LayerType());
		LayerType lt = new LayerType();
		lt.lastLayerType = layerLayerType;
		net.addLastNeuron(rightAnswer,lt);
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
	
	public void forwoard() throws Exception
	{
		net.forward();
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
	
	private int iter=0;
	private boolean performanceCheck=false;
	public void runEBPLoop(int iter, boolean performanceCheck) throws Exception
	{	
		runEBPLoop(iter, performanceCheck, false);
	}
	
	public void runEBPLoop(int iter, boolean performanceCheck,boolean printCEE) throws Exception
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
			if(printCEE)System.out.printf("[%d/%d]",i,iter);
			if(performanceCheck)startT = System.nanoTime();
			if(printCEE)this.evaluation();
			this.addCorrectRate(net.correctRate);
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("eval %d us\n",(endT-startT)/1000);
			if(performanceCheck)startT = System.nanoTime();
			this.nextRandomInput();
			if(performanceCheck)endT = System.nanoTime();
			if(performanceCheck)System.out.printf("netxRandom %d us\n",(endT-startT)/1000);
			this.forwoard();
		}
	}
	
	public double getAvgCorrectRate()
	{
		double sum = 0.0;
		for(int i=0;i<this.last10CorrectRate.length;i++)
		{
			sum += this.last10CorrectRate[i];
		}
		return sum/this.last10CorrectRate.length;
	}
	
	CountDownLatch latch = null;
	public void runEBPLoop(int iter, boolean performanceCheck, CountDownLatch latch) throws Exception
	{
		if(latch!=null)
		{
			this.iter = iter;
			this.performanceCheck = performanceCheck;
			this.latch = latch;
			Thread t = new Thread(this);
			t.start();
		}
		else
		{
			this.runEBPLoop(iter, performanceCheck);
		}
	}
	
	@Override
	public void run()
	{
		try {
			this.setParameters();
			this.runEBPLoop(this.iter, this.performanceCheck);
			if(this.latch!=null)this.latch.countDown();
			if(this.scoreboard!=null)this.scoreboard.mark(this.name, this.getAvgCorrectRate());
			System.out.println(this.name+":"+this.getAvgCorrectRate());
			try {
				this.saveToFile(this.saveFolder, this.name);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.release();
	}
	
	
	
	public void storeParamToFile(String prefix)
	{
		net.storeParamToFile(prefix, false, false);
	}
	
	public void loadParamFile(String prefix)
	{
		net.loadParamFromFile(prefix, false, false);		
	}
	
	public void release()
	{
		net.release();
	}

	Map<String, Object> param = null;
	
	private void setParameters() {
		// TODO Auto-generated method stub
		/*
		 * 	private void init(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
	{
		if(layerMatrixWidth==null)layerMatrixWidth = new int[] {50,100}; //default
		 */
		
		Integer d = (Integer) param.get("num_of_layer");
		int [] width = new int[d.intValue()];
		for(int i=0;i<width.length;i++)
		{
			Integer tmp = (Integer)param.get("W_width_of_"+i+"layer");
			width[i] = tmp.intValue();
		}
		
		this.init(100, Matrix.MATRIX_TYPE_AVX, width,false, LayerType.LAST_LAYER_TYPE_CEE);
		
		this.iter = 1000;
		this.performanceCheck = false;
		
		Double learningRate = (Double) param.get("learning_rate");
		this.net.setNormalUpdator(learningRate.doubleValue());
		

	}

	private void saveToFile(String folder, String prefix) throws Exception {
		// TODO Auto-generated method stub
		
		File f = new File(folder);
		if(f.exists())
		{
			if(f.isDirectory()==false)throw new Exception(folder+" is not a folder");
		}
		else
		{
			if(f.mkdir()==false)throw new Exception("cannot create folder:"+folder);
		}
		
		this.param.put("correct_rate", new Double(this.getAvgCorrectRate()));
		DZHelper.saveFileFromMap(this.param, folder+"/"+prefix+"_summary");
		this.storeParamToFile(folder+"/"+prefix);
		
	}
}


