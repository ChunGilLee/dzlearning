package dzlearning;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.ScoreBoard;
import dolziplib.ScoreBoard.Score;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;
import dolziplib.mnist.Mnist;
import dolziplib.mnist.MnistNLayer;
import dolziplib.param.ParameterSpace;
import dzlearning.StockInfo;
import dzlearning.STData.Answer;
import dzlearning.STData.AnswerData;
import dzlearning.Worker.WorkerResult;
import dzlearning.Worker2.ThreadParam;

public class Main {
	
	static ExecutorService exeService = null;
	
	private static void mnistParameterSearch()
	{
		Mnist mnist = Mnist.getFromBinFile("mnist_train.bin");
		int numOfProcess = Runtime.getRuntime().availableProcessors();
		numOfProcess--; //한개는 내가 사용하는 용도
		exeService = Executors.newFixedThreadPool(numOfProcess);
		ParameterSpace paramSpace = ParameterSpace.createParamSpace();
		ScoreBoard scoreboard  = new ScoreBoard();
		int totalCond = paramSpace.getTotalParamSpace();
		System.out.println("number of paramspace :"+totalCond);
		try {
			
			Map<String,Object> param = paramSpace.getNextParamTable();
			Calendar cal  =Calendar.getInstance();
			
			//estimate time
			MnistNLayer m0 = new MnistNLayer(mnist,param,null,"testing","test");
			long startT = System.nanoTime();
			m0.run();
			long endT = System.nanoTime();
			m0.release();
			
			long dur = (endT - startT)/1000;
			dur = (dur * totalCond)/1000000;
			System.out.println("Single Core time:"+dur+"s Multi Core time:"+dur/numOfProcess+"s");
			
			int index=0;
			while(param!=null)
			{
				cal  =Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String date = sdf.format(cal.getTime());
				date+="_"+index+"_"+totalCond;
				
				MnistNLayer m = new MnistNLayer(mnist,param,scoreboard,date,"test");
				exeService.execute(m);
				param = paramSpace.getNextParamTable();
				index++;
			}
			
			exeService.shutdown();
			exeService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			
			System.out.println("Main thread end!!!!");
			
			Score scores[] = scoreboard.getScores();
			//sort it
			boolean needLoop = true;
			while(needLoop)
			{
				needLoop = false;
				for(int i=0;i<scores.length-1;i++)
				{
					if(scores[i].point>scores[i+1].point)
					{
						Score tt = scores[i+1];
						scores[i+1] = scores[i];
						scores[i] = tt;
						needLoop = true;
					}
				}
			}
			for(int i=0;i<scores.length;i++)
			{
				System.out.println(scores[i].name+":"+scores[i].point);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void changeRateDistribution(STData data)
	{
		int plusCount=0;
		double plus = 0.0;
		int minusCount=0;
		double minus = 0.0;
		int pluscounts[] = new int[10];
		int minuscounts[] = new int[10];
		for(int i=0;i<pluscounts.length;i++)pluscounts[i] = 0;
		for(int i=0;i<minuscounts.length;i++)minuscounts[i] = 0;
		
		for(AnswerData ad : data.rightAnswer)
		{
			for(Answer v:ad.rightAnswer)
			{
				if(v.rate>=0)
				{
					plus+=v.rate;
					plusCount++;
					int vl = (int)(v.rate*100.0);
					if(vl>9)vl=9;
					pluscounts[vl]++;
				}
				else if(v.rate<0)
				{
					minus +=v.rate;
					minusCount++;
					int vl = (int)(v.rate*(-100.0));
					if(vl>9)vl=9;
					minuscounts[vl]++;
				}
				//System.out.printf("v:%f\n",v);
			}
		}
		
		for(int i=0;i<pluscounts.length;i++)
		{
			System.out.printf("%d~%d.9 = %d\n",i,i,pluscounts[i]);
		}
		for(int i=0;i<minuscounts.length;i++)
		{
			System.out.printf("-%d~-%d.9 = %d\n",i,i,minuscounts[i]);
		}
		
		System.out.printf("plus counter:%d avg %f\n",plusCount,plus/(double)plusCount);
		System.out.printf("minus counter:%d avg %f\n",minusCount,minus/(double)minusCount);
	}
	
	public static void getInputParameters() throws Exception
	{
		DataGenerator dg = new DataGenerator();
		STData data = dg.loadInputDataFromDB();
		
		Matrix input = Matrix.create(data.getHeightOfInput(), data.getWidthOfInput(), Matrix.MATRIX_TYPE_JAVA);
		input.setData(data.getInputData());
		Matrix right = Matrix.create(data.getHeightOfAnswer(3),2 , Matrix.MATRIX_TYPE_JAVA);
		right.setData(data.getRightAnswerData(3,3.0));		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Calendar cal = Calendar.getInstance();
		String date = sdf.format(cal.getTime());
		
		System.out.println("input h:"+input.getHeight()+" w:"+input.getWidth());
		System.out.println("right h:"+right.getHeight()+" w:"+right.getWidth());
		
		input.saveToBinFile("input_h"+input.getHeight()+"_w"+input.getWidth()+"_"+date+"_matrix.bin");
		right.saveToBinFile("right_h"+right.getHeight()+"_w"+right.getWidth()+"_"+date+"_matrix.bin");
	}
	
	public static void generateInputParameters() throws Exception
	{
		DataGenerator dg = new DataGenerator();
		//STData data = dg.loadInputDataFromDB("2007-01-01","2007-01-14");
		STData data = dg.loadInputDataFromDB();
		data.saveToFile("stdata.txt");
		
		
		Matrix input = Matrix.create(data.getHeightOfInput(), data.getWidthOfInput(), Matrix.MATRIX_TYPE_JAVA);
		input.setData(data.getInputData());
		Matrix right = Matrix.create(data.getHeightOfAnswer(3),2 , Matrix.MATRIX_TYPE_JAVA);
		right.setData(data.getRightAnswerData(3,3.0));	
		
		STData data2 = STData.loadFromFile("stdata.txt");
		
		Matrix input2 = Matrix.create(data2.getHeightOfInput(), data2.getWidthOfInput(), Matrix.MATRIX_TYPE_JAVA);
		input2.setData(data2.getInputData());
		Matrix right2 = Matrix.create(data2.getHeightOfAnswer(3),2 , Matrix.MATRIX_TYPE_JAVA);
		right2.setData(data2.getRightAnswerData(3,3.0));	
		Matrix right2Rate = Matrix.create(data2.getHeightOfAnswer(3),3 , Matrix.MATRIX_TYPE_JAVA);
		right2Rate.setData(data2.getRightAnswerWithRate(3,3.0, true));	
		
		DZHelper.checkMatrix("input", input, input2);
		DZHelper.checkMatrix("right", right, right2);
		
		//System.out.println(right2Rate);
		//Matrix mg = DZHelper.mergeToRight(a, b)
				
	}
	public static void generateInferenceData() throws Exception
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		cal.add(Calendar.DAY_OF_YEAR, -14);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = sdf.format(cal.getTime());
		cal.add(Calendar.DAY_OF_YEAR, 7);
		String endDate = sdf.format(cal.getTime());
		
		DataGenerator dg = new DataGenerator();
		STData data = dg.loadInputDataFromDB(startDate,endDate);
		data.saveToFile("stdata_inference.txt");
	}
	
	public static void test2() throws Exception
	{
		Matrix input0 = Matrix.loadFromBinFile("test_normal_input0.bin", Matrix.MATRIX_TYPE_AVX);
		Matrix input1 = Matrix.loadFromBinFile("test_normal_input1.bin", Matrix.MATRIX_TYPE_AVX);
		MatrixOperator op = input0.getMatrixOperator();
		input0 = DZHelper.getSlice(input0, 0, 50,0,50);
		input1 = DZHelper.getSlice(input1, 0, 50,0,50);
		
		System.out.println("input0:"+input0);
		System.out.println("input1:"+input1);
		
		DZHelper.checkData(input0);
		DZHelper.checkData(input1);

		Matrix output = Matrix.create(input0.getHeight(), input1.getWidth(), Matrix.MATRIX_TYPE_AVX);
		System.out.println("output:"+output);
		
		DZHelper.checkData(output);

		
		long startT = System.nanoTime();
		op.dot(input0, input1, output);
		long endT = System.nanoTime();
		System.out.printf("end of tranining, %d us A(%d,%d) dot B(%d,%d) \n",(endT-startT)/1000, 
				input0.getHeight(), input0.getWidth(), input1.getHeight(), input1.getWidth(),
				output.getHeight(), output.getWidth());
		
		input0.release();
		input1.release();
		output.release();

		input0 = Matrix.loadFromBinFile("test_batch_input0.bin", Matrix.MATRIX_TYPE_AVX);
		input1 = Matrix.loadFromBinFile("test_batch_input1.bin", Matrix.MATRIX_TYPE_AVX);
		op = input0.getMatrixOperator();
		input0 = DZHelper.getSlice(input0, 0, 50,0,50);
		input1 = DZHelper.getSlice(input1, 0, 50,0,50);

		System.out.println("input0:"+input0);
		System.out.println("input1:"+input1);
		DZHelper.checkData(input0);
		DZHelper.checkData(input1);
		output = Matrix.create(input0.getHeight(), input1.getWidth(), Matrix.MATRIX_TYPE_AVX);		
		System.out.println("output:"+output);

		DZHelper.checkData(output);

		
		startT = System.nanoTime();
		op.dot(input0, input1, output);
		endT = System.nanoTime();
		System.out.printf("end of tranining, %d us A(%d,%d) dot B(%d,%d) \n",(endT-startT)/1000, 
				input0.getHeight(), input0.getWidth(), input1.getHeight(), input1.getWidth(),
				output.getHeight(), output.getWidth());

	}
		
	public static void test1() throws Exception
	{
		Matrix input = Matrix.create(594, 3190, Matrix.MATRIX_TYPE_AVX);
		Matrix right = Matrix.create(594, 2, Matrix.MATRIX_TYPE_AVX);
		input.loadFromBinFile("input_h594_w3190_20180723_093912_matrix.bin");
		right.loadFromBinFile("right_h:594_w:2_20180723_093912_matrix.bin");
		
		//check data
		DZHelper.checkData(input);
		DZHelper.checkData(right);
		
		
		STNLayer layer = null;
		DZConfig param = DZConfig.getStaticParameter();
		ScoreBoard sb = new ScoreBoard();
		layer = STNLayer.createForTraining(input, right, param, sb, "sttest", "sttest", 200);
		long startT = System.nanoTime();
		layer.runEBPLoop(1, true, true);
		long endT = System.nanoTime();
		
		System.out.println("end of tranining, "+(endT-startT)/1000000+" ms");
		
		System.out.println("num of layers:"+layer.net.getNumOfLayers());
		
		layer.release();		
		input.release();
		right.release();
	}
	
	public static void doSingleParam(int rightAnswerType) throws Exception
	{
		STData data = STData.loadFromFile("stdata.txt");
		inferenceSingle(data, 1, "test",rightAnswerType);
	}
	
	//0: one hot encoding
	//1: rate
	public static final int RIGHT_ANSWER_TYPE_ONE_HOT=0;
	public static final int RIGHT_ANSWER_TYPE_RATE=1;
	public static double inferenceSingle(STData dataTraining,int idStockInfo,String folder, int rightAnswerType) throws Exception
	{
		
		StockInfo sinfo = DataGenerator.getStockInfoById(idStockInfo);
		System.out.println("stock:"+sinfo.name+"("+sinfo.code+") id:"+idStockInfo);
		
		Matrix input = Matrix.create(dataTraining.getHeightOfInput(), dataTraining.getWidthOfInput(), Matrix.MATRIX_TYPE_AVX);
		input.setData(dataTraining.getInputData());
		Matrix right=null;
		if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
		{
			right = Matrix.create(dataTraining.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
			right.setData(dataTraining.getRightAnswerData(idStockInfo,3.0));	
		}
		else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
		{
			right = Matrix.create(dataTraining.getHeightOfAnswer(idStockInfo),1 , Matrix.MATRIX_TYPE_AVX);
			right.setData(dataTraining.getRightAnswerMappedRate(idStockInfo) );				
		}
		else
		{
			throw new RuntimeException("invalid right answer type");
		}
			
		//check data
		DZHelper.checkData(input);
		DZHelper.checkData(right);
		
		STNLayer layer = null;
		DZConfig param = DZConfig.getStaticParameter();
		if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)param = DZConfig.getStaticParameter();
		else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)param = DZConfig.getStaticParameter(LayerType.LAST_LAYER_TYPE_MSE);
		ScoreBoard sb = new ScoreBoard();
		
		
		layer = STNLayer.createForTraining(input, right, param, sb, "training_"+idStockInfo, folder, 200);
		//layer = STNLayer.createForTraining(input, right, param, sb, "training_"+idStockInfo, folder, 3,true);
		long startT = System.nanoTime();
		layer.runEBPLoop(500, false, true);
		long endT = System.nanoTime();
		layer.saveToFile();
		System.out.println("end of tranining, "+(endT-startT)/1000000+" ms");
		layer.release();
		input.release();
		right.release();
		
	
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		cal.setTime(sdf.parse("2018-06-11"));
		
		for(int i=0;i<5;i++)
		{
			String startD = sdf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 6);
			String endD = sdf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			
			DataGenerator dg = new DataGenerator();
			STData dataInference = dg.loadInputDataFromDB(startD,endD);
			//STData dataInference = dg.loadInputDataFromDB("2018-01-01","2018-04-30");
	
			System.out.println("startD:"+startD+" endD:"+endD);
			
			
			Matrix inputInf = Matrix.create(dataInference.getHeightOfInput(), dataInference.getWidthOfInput(), Matrix.MATRIX_TYPE_AVX);
			inputInf.setData(dataInference.getInputData());
			Matrix rightInf = null;
			Matrix rightInfRate = null;
			if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
			{
				rightInf = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
				rightInf.setData(dataInference.getRightAnswerData(idStockInfo,3.0));	
				rightInfRate = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),3 , Matrix.MATRIX_TYPE_AVX);
				rightInfRate.setData(dataInference.getRightAnswerWithRate(idStockInfo,3.0,true));	
			}
			else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
			{
				rightInf = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),1 , Matrix.MATRIX_TYPE_AVX);
				rightInf.setData(dataInference.getRightAnswerMappedRate(idStockInfo));	
				rightInfRate = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
				rightInfRate.setData(dataInference.getRightAnswerMappedWithRealRate(idStockInfo,true));	
			}
			
			inputInf.saveToBinFile("input_inf.bin");
			rightInf.saveToBinFile("right_inf.bin");
			
			layer = STNLayer.createForInference(inputInf, rightInf, param, "training_"+idStockInfo, folder,rightInfRate);
			layer.inference();		
			
			if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
			{
				Matrix tmp = DZHelper.mergeToRight(layer.net.getLasyLayer().getOutputMatrix(), layer.rightAnswerWithRate);
				System.out.println("output:"+tmp);
				tmp.release();
			}
			else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
			{
				Matrix tmp = DZHelper.mergeToRight(layer.net.getLasyLayer().getOutputMatrix(), layer.rightAnswerWithRate);
				for(int j=0;j<tmp.getHeight();j++)
				{
					double v = tmp.getData(j, 0);
					v = STData.rateDeMapped(v);
					tmp.setData(v,j,1);
				}
				System.out.println("output:"+tmp);
				tmp.release();
			}
			
			double correctRate = layer.getCorrectRate();
			//System.out.println(layer.net.getLasyLayer().output);
			
			layer.release();
			inputInf.release();
			rightInf.release();
			if(rightInfRate!=null)rightInfRate.release();
		}

		return 0.0;
	}
	
	
	//startDate 과 endDate는 inference date기준
	public static void onlyInferenceSingle(STData dataTranining, int idStockInfo, String folder, String prefix,
			String startDate,String endDate) throws Exception
	{
		DataGenerator dg = new DataGenerator();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(startDate));
		cal.add(Calendar.DAY_OF_YEAR, -7);
		String startInputDate = sdf.format(cal.getTime());
		cal.setTime(sdf.parse(endDate));
		cal.add(Calendar.DAY_OF_YEAR, -7);
		String endInputDate = sdf.format(cal.getTime());
		
		System.out.println("startInputDate:"+startInputDate+" endInputDate:"+endInputDate);
		STData dataInference = dg.loadInputDataFromDB(startInputDate,endInputDate);
		//STData dataInference = dg.loadInputDataFromDB("2018-01-01","2018-04-30");

		Matrix inputInf = Matrix.create(dataInference.getHeightOfInput(), dataInference.getWidthOfInput(), Matrix.MATRIX_TYPE_AVX);
		inputInf.setData(dataInference.getInputData());
		Matrix rightInf = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
		rightInf.setData(dataInference.getRightAnswerData(idStockInfo,3.0));
		Matrix rightInfWithRate = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),3 , Matrix.MATRIX_TYPE_AVX);
		rightInfWithRate.setData(dataInference.getRightAnswerWithRate(idStockInfo,3.0,true));
		
		DZConfig param = DZConfig.getStaticParameter();
		
		STNLayer layer = STNLayer.createForInference(inputInf, rightInf, param, prefix, folder,rightInfWithRate);
		layer.inference();		
		
		Matrix withRight = DZHelper.mergeToRight(layer.net.getLasyLayer().getOutputMatrix(), layer.rightAnswerWithRate);
		System.out.println("output:"+withRight);
		//System.out.println("w:"+layer.net.getLayer(0).w);
		withRight.release();
		
		rightInfWithRate.release();
		layer.release();
		inputInf.release();
		rightInf.release();
	}
	

	public static void inferenceAllStock() throws Exception
	{
		int[] ids = DataGenerator.getTargetCompany();
		STData data = STData.loadFromFile("stdata.txt");
		double correctRateSum = 0;
		int count=0;
		for(int i=0;i<100;i++)
		{
			correctRateSum += inferenceSingle(data, ids[i], "test11" , RIGHT_ANSWER_TYPE_ONE_HOT);
			count++;
		}
		System.out.println("total correct rate:"+correctRateSum/(double)count);
	}
	
	private static String getDateAWeekAgo(String date) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(date));
		cal.add(Calendar.DAY_OF_YEAR, -7);
		return sdf.format(cal.getTime());
	}
	
	private static STData dataInference = null;
	private static synchronized STData getDataInference(String startDate,String endDate) throws Exception
	{
		if(dataInference!=null)return dataInference;
		DataGenerator dg = new DataGenerator();
		STData dataInference = dg.loadInputDataFromDB(getDateAWeekAgo(startDate),getDateAWeekAgo(endDate));
		dataInference.saveToFile("data_inference.txt");
		return dataInference;
	}
	
	public static void inferenceAllStockInThread(boolean doTraining)  throws Exception
	{
		int[] ids = DataGenerator.getTargetCompany();
		STData data = STData.loadFromFile("stdata.txt");
		
		STData dataInference = getDataInference("2018-07-16","2018-07-16");
		
		System.out.printf("%s %s %s %s\n",dataInference.inputStartDate,dataInference.inputEndDate,
				dataInference.rightStartDate,dataInference.rightEndDate);
		
		int offset=0;
		int numOfIdsForTest=ids.length;
		int numOfThread=7;
		
		LinkedList<LinkedList<Integer>> forThreads = new LinkedList<LinkedList<Integer>>();
		for(int i=0;i<numOfThread;i++)
		{
			LinkedList<Integer> idsForThread = new LinkedList<Integer>();
			forThreads.add(idsForThread);
		}
		
		for(int i=0;i<numOfIdsForTest;i++)
		{
			LinkedList<Integer> idsForThread = forThreads.get(i%(forThreads.size()));
			idsForThread.add(new Integer(ids[i+offset]));
		}
		
		CountDownLatch latch = new CountDownLatch(forThreads.size());

		LinkedList<Worker> workers = new LinkedList<>();
		for(int i=0;i<forThreads.size();i++)
		{
			Worker worker = new Worker(data, dataInference, forThreads.get(i), latch, "thread_test",doTraining, Worker.RIGHT_ANSWER_TYPE_RATE); 
			workers.add(worker);
			worker.start();
		}
		latch.await();
		
		LinkedList<InferencedStock> invested = new LinkedList<>();
		int total=0;
		int correct=0;
		for(Worker worker:workers)
		{
			for(WorkerResult res:worker.results)
			{
				res.printResult();
				for(InferencedStock is:res.inferencedStocks)
				{
					total++;
					if(is.isCorrenct())correct++;
					if(is.inferenceResult == InferencedStock.RESULT_UNKNOWN)
					{
						System.out.println("RESULT_UNKNOWN output matrix:"+is.output);
					}
					
					if(is.inferenceResult == InferencedStock.RESULT_OVER3PERCENT)
					{
						invested.add(is);
					}
				}
			}
		}
		System.out.printf("correct rate: %f\n",(double)correct/(double)total);
		
		double sumRateOfInvest=0.0;
		for(InferencedStock is:invested)
		{
			System.out.printf("invest:%s %f\n",is.stockInfo.name,is.realDungrakRate);
			sumRateOfInvest += is.realDungrakRate;
		}
		System.out.printf("total investment rate:%f, numofinvest:%d\n", sumRateOfInvest/(double)invested.size(),invested.size());
	}
	
	public static void testaaa()
	{
		double data[] = new double[] {6.364,-1.709,2.609,6.78,4.762};
		double out = 0.0;
		for(int i=0;i<data.length;i++)
		{
			if(i==0)out = data[i];
			else
			{
				out = data[i]*out/100.0 + data[i] + out;
			}
		}
		System.out.println(out);
	}
	
	public static void inferenceAllStockInThread2(boolean doTraining)  throws Exception
	{
		//inferenceSelectedStockInThread2(doTraining, null);
		inferenceSelectedStockInThread2(doTraining, getTestCompanies(),1);
	}
	public static void inferenceSingleStockInThread2(boolean doTraining, int idStockInfo , int numOfLoop)  throws Exception
	{
		int ids[] = new int[1];
		ids[0] = idStockInfo;
		inferenceSelectedStockInThread2(doTraining, ids,numOfLoop);
	}
	
	public static void inferenceSelectedStockInThread2(boolean doTraining, int inputIds[], int numOfLoop)  throws Exception
	{
		
		Data data = Data.getFromDB(Data.DATA_TYPE_HETERODATA, "2008-01-01", "2018-03-31", inputIds);
		
		//int[] idsForInferenceTT = new int[1];
		//idsForInferenceTT[0] = 525;
		int[] idsForInferenceTT = inputIds;
		
		//Data inferenceData = Data.getFromDB(Data.DATA_TYPE_HETERODATA, "2018-07-02", "2018-07-20", idsForInferenceTT);
		Data inferenceData = Data.getFromDB(Data.DATA_TYPE_HETERODATA, "2017-04-01", "2017-04-30", idsForInferenceTT);
			
			int[] idsForTraining = data.getIds();
			int[] idsForInference = inferenceData.getIds();
			int offset=0;
			int numOfIdsForTest=idsForTraining.length;
			int numOfThread=7;
			
			if(numOfLoop>1)
			{
				int[] newIds = new int[idsForTraining.length*numOfLoop];
				int[] newIds2 = new int[idsForTraining.length*numOfLoop];
				for(int i=0;i<idsForTraining.length;i++)
				{
					for(int j=0;j<numOfLoop;j++)
					{
						newIds[i*numOfLoop+j]=idsForTraining[i];
						newIds2[i*numOfLoop+j]=idsForInference[i];
					}
				}
				idsForTraining = newIds;
				idsForInference = newIds2;
				offset = offset * numOfLoop;
				numOfIdsForTest = numOfIdsForTest * numOfLoop;
			}
			
			LinkedList<LinkedList<ThreadParam>> forThreads = new LinkedList<LinkedList<ThreadParam>>();
			for(int i=0;i<numOfThread;i++)
			{
				LinkedList<ThreadParam> idsForThread = new LinkedList<ThreadParam>();
				forThreads.add(idsForThread);
			}
			
			for(int i=0;i<numOfIdsForTest;i++)
			{
				LinkedList<ThreadParam> idsForThread = forThreads.get(i%(forThreads.size()));
				ThreadParam tp = new ThreadParam();
				tp.idForTraining = idsForTraining[i+offset];
				tp.idForInference = idsForInference[i+offset];
				tp.paramIndex = 0;
				idsForThread.add(tp);
			}
			
			CountDownLatch latch = new CountDownLatch(forThreads.size());
	
			LinkedList<Worker2> workers = new LinkedList<>();
			for(int i=0;i<forThreads.size();i++)
			{
				LinkedList<ThreadParam> threadParam = forThreads.get(i);
				for(ThreadParam tp:threadParam)
				{
					tp.threadId = i;
				}
				Worker2 worker = new Worker2(data, inferenceData,latch, "ddata_test2", true,threadParam);
				workers.add(worker);
				worker.start();
			}
			latch.await();
			
		data.getResultHolder().printResult();
		inferenceData.getResultHolder().printResult();

	}
	
	public static int[] getTestCompanies()
	{
		int ids[] = {
				113, //한양증권
				712, //동양고속
				38, //흥국화재
				329, //대구백화점
				577, //동서
				297, //넥센
				588, //신도리코
				525, //SK텔레콤
		};
		return ids;
	}
	
	public static void generate2GenInputParameters() throws Exception
	{
		//wdata.startDate = "2007-01-01";
		//wdata.endDate = "2018-05-31";
		//WData wdata = WData.getFromDB("2007-01-01","2018-05-31");
		
		int ids[] = new int[2];
		ids[0] = 1;
		ids[1] = 3;
		//Data data = Data.getFromDB(Data.DATA_TYPE_DAYDATA, "2007-02-01", "2018-05-31");
		//Data data = Data.getFromDB(Data.DATA_TYPE_DAYDATA, "2007-02-01", "2007-02-05", ids);
		Data data = Data.getFromDB(Data.DATA_TYPE_HETERODATA, "2007-02-01", "2007-02-05", ids);
		data.saveToFile("ddata.json");
		System.out.println("generating completed");
		
		
		Data data2 = Data.loadFromFile("ddata.json");
		Matrix mm = data2.getInputData(1, Matrix.MATRIX_TYPE_JAVA);
		System.out.println(mm);
		Matrix mm2 = data2.getAnswerData(1, Matrix.MATRIX_TYPE_JAVA);
		System.out.println(mm2);
		mm.release();
		mm2.release();
		
		mm = data2.getInputData(3, Matrix.MATRIX_TYPE_JAVA);
		System.out.println(mm);
		mm2 = data2.getAnswerData(3, Matrix.MATRIX_TYPE_JAVA);
		System.out.println(mm2);
		DZHelper.printArray(data2.getIds());

		mm.release();
		mm2.release();
		
		DZHelper.printArray(data2.getIds());
		data2.printDebug(1);
		data2.printDebug(3);
		
		
	}
	
	public static void main(String args[]) throws Exception
	{
		//System.out.println(Math.round(-1.6));
		//test2();
		//doSingleParam(RIGHT_ANSWER_TYPE_RATE);
		//getInputParameters();
		//DataValidation.dataValidationDailyStock();
		//generateInputParameters();
		//generateInferenceData();
		//inferenceAllStock();
		//inferenceAllStockInThread(true);
		
		//STData data = STData.loadFromFile("stdata.txt");
		//onlyInferenceSingle(data, 1, "thread_test", "training_1", "2017-06-11", "2017-07-28");
		
		//generate2GenInputParameters();
		//inferenceAllStockInThread2(true);
		inferenceSingleStockInThread2(true, 525,10);
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@completed@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
	}
}
