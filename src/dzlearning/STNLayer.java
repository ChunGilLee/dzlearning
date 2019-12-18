package dzlearning;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.HeteroLayer;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.Learner;
import dolziplib.Net;
import dolziplib.ScoreBoard;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;
import dolziplib.mnist.Mnist;
import dolziplib.mnist.MnistImg;
import dolziplib.param.ParameterSpace;

public class STNLayer implements Runnable{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	Matrix inputFullData = null;
	Matrix rightAnswerFullData = null;
	public Matrix rightAnswerFullDataWithRate = null;
	public Net net;
	Matrix input = null;
	Matrix rightAnswer = null;
	public Matrix rightAnswerWithRate = null;
	//Random r = null;
	int layerMatrixWidth[] = null;
	double last10CorrectRate[] = new double[10];
	int last10CorrectRateIndex=0;
	int numOfInput=0;
	boolean inferenceMode = false;
	
	
	private static Random rand = null;
	private static Object randKey = new Object();
	private static Random getRandom()
	{
		synchronized (randKey) {
			if(rand==null)rand = new Random(System.currentTimeMillis());
			return rand;
		}
	}
	
	private void addCorrectRate(double rate)
	{
		last10CorrectRate[last10CorrectRateIndex] = rate;
		last10CorrectRateIndex++;
		if(last10CorrectRateIndex>=last10CorrectRate.length)last10CorrectRateIndex=0;
	}
	
	ScoreBoard scoreboard = null;
	String name = null;
	String saveFolder = null;
	
	public static STNLayer createForTraining(Matrix inputFullData, Matrix rightAnswerFullData, DZConfig config, 
			ScoreBoard scoreboard,String name,String saveFolder, int numOfInput)
	{
		return createForTraining(inputFullData, rightAnswerFullData, config, scoreboard, name, saveFolder, numOfInput,  null);
	}
	
	public static STNLayer createForTraining(Matrix inputFullData, Matrix rightAnswerFullData, DZConfig param, 
			ScoreBoard scoreboard,String name,String saveFolder, int numOfInput,  Matrix rightAnswerFullWithRate)
	{
		STNLayer layers = new STNLayer();
		layers.matrixType = inputFullData.getMatrixType();
		layers.inputFullData = inputFullData;
		layers.rightAnswerFullData = rightAnswerFullData;
		layers.config = param;
		layers.scoreboard = scoreboard;
		layers.name = name;
		layers.saveFolder = saveFolder;
		layers.rightAnswerFullDataWithRate = rightAnswerFullWithRate;
		layers.setParameters(numOfInput);
		
		return layers;
	}



	public static STNLayer createForInference(Matrix inputFullData, Matrix rightAnswerFullData, DZConfig param, 
			String name,String saveFolder)
	{
		return createForInference(inputFullData, rightAnswerFullData, param, name, saveFolder, null);
	}
	
	public static STNLayer createForInference(Matrix inputFullData, Matrix rightAnswerFullData, DZConfig param, 
			String name,String saveFolder, Matrix rightAnswerFullWithRate)
	{
		STNLayer layers = new STNLayer();
		layers.matrixType = inputFullData.getMatrixType();
		layers.inferenceMode = true;
		layers.inputFullData = inputFullData;
		layers.rightAnswerFullData = rightAnswerFullData;
		layers.config = param;
		layers.scoreboard = null;
		layers.name = name;
		layers.saveFolder = saveFolder;
		layers.rightAnswerFullDataWithRate = rightAnswerFullWithRate;
		layers.setParameters(inputFullData.getHeight());
		
		return layers;
	}
	
	private void init(int numOfInput, int matrixType, int layerMatrixWidth[])
	{
		init(numOfInput, matrixType, layerMatrixWidth, LayerType.LAST_LAYER_TYPE_CEE);
	}
	private void init(int numOfInput, int matrixType, int layerMatrixWidth[], int lastLayerType)
	{
		if(layerMatrixWidth==null)layerMatrixWidth = new int[] {50,100}; //default
		this.layerMatrixWidth = layerMatrixWidth;
		
		this.matrixType = matrixType;
		
		input = Matrix.create(numOfInput, this.inputFullData.getWidth(), matrixType);
		input.setName("DataInput Matrix");
		rightAnswer = Matrix.create(numOfInput, this.rightAnswerFullData.getWidth(), matrixType);
		rightAnswer.setName("CorrectAnswer Matrix");
		if(this.rightAnswerFullDataWithRate!=null)this.rightAnswerWithRate = 
				Matrix.create(numOfInput, this.rightAnswerFullDataWithRate.getWidth(), matrixType);

		nextRandomInput();
		
		net = new Net(this.config);
		net.setRandom(STNLayer.getRandom());
		if(this.config.getHeleroLayerConfig()!=null)
		{
			LayerType firstLayerType = new LayerType();
			firstLayerType.layerType = LayerType.LAYER_TYPE_HETERO;
			net.addFirstNeuron(input, layerMatrixWidth[0],firstLayerType);
		}
		else
		{
			net.addFirstNeuron(input, layerMatrixWidth[0],new LayerType());
		}
		for(int i=1;i<layerMatrixWidth.length;i++)net.addMidNeuron(layerMatrixWidth[i],new LayerType());
		LayerType lt = new LayerType();
		lt.lastLayerType = lastLayerType;
		net.addLastNeuron(rightAnswer,lt);
	}
	
	private ArrayList<Integer> used = new ArrayList<Integer>();
	public void nextRandomInput()
	{
		
		
		int count=0;
		for(int i=0;i<inputFullData.getHeight();i++)
		{
			
			if(used.size()==0)
			{
				used = new ArrayList<Integer>(inputFullData.getHeight());
				for(int j=0;j<this.inputFullData.getHeight();j++)
				{
					used.add(new Integer(j));
				}
			}
			
			if(count>=this.input.getHeight())break;
			int index = getRandom().nextInt(this.used.size());
			int row = used.get(index).intValue();
			this.used.remove(index);
			double [] d = this.inputFullData.getRow(row);
			double [] a = this.rightAnswerFullData.getRow(row);
			input.setData(d, i);
			this.rightAnswer.setData(a, i);
			if(this.rightAnswerFullDataWithRate!=null)
			{
				double [] r = this.rightAnswerFullDataWithRate.getRow(row);
				this.rightAnswerWithRate.setData(r,i);
			}
			count++;
		}
		
	}
	public void setFullInput()
	{
		MatrixOperator op = inputFullData.getMatrixOperator();
		op.add(inputFullData, 0, input);
		op.add(this.rightAnswerFullData, 0, rightAnswer);
		if(this.rightAnswerFullDataWithRate!=null)
			op.add(this.rightAnswerFullDataWithRate, 0, this.rightAnswerWithRate);
		
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
		this.backpropagation(false);
		
	}
	public void backpropagation(boolean evaluation)
	{
		try {
			net.backpropagation(evaluation);
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
	
	public void inference()
	{	
		this.setParamInit();
		this.loadParamFile(this.name, this.saveFolder);
		this.setFullInput();
		this.net.setInferenceMode();
		this.forwoard();
		this.evaluation();
	}
	
	
	private int iter=0;
	private boolean performanceCheck=false;
	public void runEBPLoop(int iter, boolean performanceCheck)
	{	
		runEBPLoop(iter, performanceCheck, false);
	}
	
	public void runEBPLoop(int iter, boolean performanceCheck,boolean printCEE)
	{	
		this.setParamInit();
		this.forwoard();
		
		//HeteroLayer layer = (HeteroLayer)this.net.getLayer(0);
		//Matrix Startfilters = layer.getFilter(2).copy(true);
		
		long startT=0;
		long endT=0;
		for(int i=0;i<iter;i++)
		{			
			if(performanceCheck)startT = System.nanoTime();
			this.backpropagation(performanceCheck);
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
			
			//Matrix tmp = DZHelper.mergeToRight(this.net.getLasyLayer().addResult, this.rightAnswer);
			//System.out.println("tmp:"+tmp);
			//System.out.println("output:"+this.net.getLasyLayer().output);
			//tmp.release();
			
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
		//Matrix Endfilters = layer.getFilter(2);
		//System.out.println("start filter:"+Startfilters);
		//System.out.println("end filter:"+Endfilters);
		
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
	public void runEBPLoop(int iter, boolean performanceCheck, CountDownLatch latch)
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
		//this.setParameters();
		this.runEBPLoop(this.iter, this.performanceCheck, true);
		if(this.scoreboard!=null)this.scoreboard.mark(this.name, this.getAvgCorrectRate());
		System.out.println(this.name+":"+this.getAvgCorrectRate());
		try {
			this.saveToFile(this.saveFolder, this.name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(this.latch!=null)this.latch.countDown();
		this.release();
	}
	
	
	public void storeParamToFile(String prefix)
	{
		net.storeParamToFile(prefix, false, false);
	}
	
	public void loadParamFile(String prefix,String folder)
	{
		net.loadParamFromFile(folder+"/"+prefix, false, false);		
	}
	
	public void loadParamFile(String prefix)
	{
		net.loadParamFromFile(prefix, false, false);		
	}
	
	public void release()
	{
		net.release();
		
		if(this.inputFullData!=null)this.inputFullData.release();
		if(this.rightAnswerFullData!=null)this.rightAnswerFullData.release();
		if(this.rightAnswerFullDataWithRate!=null)this.rightAnswerFullDataWithRate.release();
		if(this.rightAnswer!=null)this.rightAnswer.release();
		if(this.rightAnswerWithRate!=null)this.rightAnswerWithRate.release();
	}

	DZConfig config = null;
	
	private void setParameters(int numOfInput) {
		// TODO Auto-generated method stub
		/*
		 * 	private void init(Mnist set, int numOfInput, int matrixType, int layerMatrixWidth[])
	{
		if(layerMatrixWidth==null)layerMatrixWidth = new int[] {50,100}; //default
		 */
		
		int [] width = new int[config.getInt("num_of_layer", 0)];
		for(int i=0;i<width.length;i++)
		{
			width[i] = config.getInt("W_width_of_"+i+"layer", 0);
		}
		
		
		int d = config.getInt("last_layer_type", -1);
		if(d<0)this.init(numOfInput, this.matrixType, width);
		else this.init(numOfInput, this.matrixType, width, d);
		
		//this.iter = 1000;
		this.performanceCheck = false;
		
		String updator = config.getString("updator", null);
		if(updator.equals("normal"))
		{
			this.net.setNormalUpdator(config.getDouble("learning_rate",0.01));
		}
		else if(updator.equals("adam"))
		{
			this.net.setAdamUpdator(
					config.getDouble("learning_rate",0.01),
					config.getDouble("learning_rate_beta1",0.09),
					config.getDouble("learning_rate_beta2",0.0999));
		}
		else
		{
			throw new RuntimeException("invalid updator");
		}

	}

	public void saveToFile() throws Exception
	{
		this.saveToFile(this.saveFolder, this.name);
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
		
		this.config.put("correct_rate", new Double(this.getAvgCorrectRate()));
		DZHelper.saveFileFromMap(this.config, folder+"/"+prefix+"_summary");
		this.storeParamToFile(folder+"/"+prefix);
		
	}
}


