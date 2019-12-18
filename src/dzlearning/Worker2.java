package dzlearning;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.Layer;
import dolziplib.ScoreBoard;
import dolziplib.matrix.Matrix;
import dolziplib.param.ParameterSpace;
import dzlearning.StockInfo;
//import sun.awt.image.ImageWatched.Link;
import dzlearning.STData.Answer;

public class Worker2 extends Thread{

	int matrixType = Matrix.MATRIX_TYPE_AVX;
	//WorkerInferenceResult workerInferenceResult = null;
	boolean batchNorm = true;
	
	public static class WorkerResult
	{
		Exception exceptionCode = null;
		int idStockInfo;
		LinkedList<InferencedStock> inferencedStocks = new LinkedList<InferencedStock>();	
		public double getAvgConnectRate()
		{
			double sum=0.0;
			int count=0;
			for(InferencedStock is:inferencedStocks)
			{
				if(is.isCorrenct())sum+=1.0;
				count++;
			}
			return sum/(double)count;
		}
		public void printResult()
		{
			for(InferencedStock is:inferencedStocks)
			{
				System.out.println(is);
			}
		}
	}
	
	public static class ThreadParam
	{
		int threadId;
		int idForTraining;
		int idForInference;
		int paramIndex;
	}
	
	Data dataTraining = null;
	Data dataInference = null;
	CountDownLatch latch = null;
	String workingFolder = null;
	LinkedList<WorkerResult> results = new LinkedList<>();
	boolean doTraining = true;
	//LinkedList<Integer> idStockInfo = null;
	LinkedList<ThreadParam> threadparams = null;
	
	public Worker2(Data trainingData, Data dataInference,
			CountDownLatch latch,String workingFolder,
			boolean doTraining, LinkedList<ThreadParam> threadparams) throws ParseException, SQLException
	{

		this.latch = latch;
		
		this.dataTraining = trainingData;
		this.workingFolder = workingFolder;
		this.doTraining = doTraining;
		this.dataInference = dataInference;
		//this.workerInferenceResult = workerInferenceResult;
		this.threadparams = threadparams;
		
		/*
		for(int i=0;i<dataInference.getIds().length;i++)
		{
			WorkerResult result = new WorkerResult();
			result.idStockInfo = dataInference.getIds()[i];
			this.results.add(result);
			
		}
		

		Calendar inferenceStartDate = Calendar.getInstance();
		Calendar inferenceEndDate = Calendar.getInstance();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		inferenceStartDate.setTime(sdf.parse(dataInference.rightStartDate));
		inferenceEndDate.setTime(sdf.parse(dataInference.rightEndDate));
		
		if(dataInference.inputStartDate.compareTo(dataInference.inputEndDate)>0)
		{
			throw new RuntimeException("inputStartDate("+dataInference.inputStartDate+
					") after inputEndDate("+dataInference.inputEndDate+")");
		}
		if(dataInference.rightStartDate.compareTo(dataInference.rightEndDate)>0)
		{
			throw new RuntimeException("rightStartDate("+dataInference.rightStartDate+
					") after rightEndDate("+dataInference.rightEndDate+")");
		}
		*/

		/*
		for(WorkerResult result:results)
		{
			StockInfo stockInfo = DataGenerator.getStockInfoById(result.idStockInfo);
			LinkedList<Answer> answers =  dataInference.getRightAnswer(result.idStockInfo);
			for(Answer answer:answers)
			{
				InferencedStock is = new InferencedStock(stockInfo);
				is.targetStartDay = answer.date;
				cal.setTime(sdf.parse(is.targetStartDay));
				cal.add(Calendar.DAY_OF_YEAR, 6);
				is.targetEndDay = sdf.format(cal.getTime());
				result.inferencedStocks.add(is);
			}
		}
		*/			

		
	}
	
	private String savedTrainingParamName = null;
	private int runningCounter=0;
	
	@Override
	public void run()
	{
		
		WorkerInferenceResult trainingResult = null;
		WorkerInferenceResult inferenceResult = null;
		for(ThreadParam id:this.threadparams)
		{
			savedTrainingParamName = "training_"+id.idForTraining+"_"+id.threadId+"_"+runningCounter;
			try {
					if(this.doTraining)
					{
						trainingResult = this.dataTraining.getResultHolder();
						training(id);
						trainingResult.setTrainingResult(id.idForTraining, this.traniningCorrectRate , null, this.trainingConfig,savedTrainingParamName);
					}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(trainingResult!=null)trainingResult.setTrainingResult(id.idForTraining,  this.traniningCorrectRate, e, this.trainingConfig,savedTrainingParamName);
				break;
			}
			
			
			
			try {
				if(this.dataInference!=null)
				{
					inferenceResult = this.dataInference.getResultHolder();
					Matrix result = inference(id);
					inferenceResult.setInferenceResult(id.idForInference, result.getAllData(),this.traniningCorrectRate , null,this.inferenceConfig,savedTrainingParamName);
					result.release();
				}
				else
				{
					System.out.println("there is no inference data for id:"+id.idForInference);
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(inferenceResult!=null)inferenceResult.setInferenceResult(id.idForInference, null, this.traniningCorrectRate, e,this.inferenceConfig,savedTrainingParamName);
				break;
			}
			runningCounter++;
			
			
		}
		this.latch.countDown();

	}
	
	private double traniningCorrectRate = Double.NaN;
	private DZConfig trainingConfig = null;
	private void training(ThreadParam tp) throws Exception
	{
		StockInfo sinfo = DataGenerator.getStockInfoById(tp.idForTraining);
		System.out.println("stock:"+sinfo.name+"("+sinfo.code+") id:"+tp.idForTraining+" paramIndex:"+tp.paramIndex);
		
		Matrix input = this.dataTraining.getInputData(tp.idForTraining, this.matrixType);
		Matrix right = this.dataTraining.getAnswerData(tp.idForTraining, this.matrixType);
		
		//check data
		DZHelper.checkData(input);
		DZHelper.checkSize(input);
		DZHelper.checkData(right);
		DZHelper.checkSize(right);
		
		trainingConfig = this.dataTraining.getParameter(tp.idForTraining, tp.paramIndex);
		STNLayer layer = null;
		layer = STNLayer.createForTraining(input, right, trainingConfig, null, savedTrainingParamName, workingFolder, 
				trainingConfig.getInt("training_sample", 200));
		long startT = System.nanoTime();
		layer.runEBPLoop(trainingConfig.getInt("training_iteration" , 1000) , false, false);
		//layer.runEBPLoop(10, false, true);
		long endT = System.nanoTime();
		traniningCorrectRate = layer.getAvgCorrectRate();
		layer.saveToFile();		
		Layer l3 = layer.net.getLayer(2);
		//System.out.println("mu:"+l3.batchNormalization.mu);
		//System.out.println("var:"+l3.batchNormalization.var);
		System.out.println("end of tranining, "+(endT-startT)/1000000+" ms");
		layer.release();
		input.release();
		right.release();
		
	}
	private DZConfig inferenceConfig = null;
	private Matrix inference(ThreadParam tp) throws Exception
	{
		
		Matrix input = this.dataInference.getInputData(tp.idForInference, this.matrixType);
		Matrix right = this.dataInference.getAnswerData(tp.idForInference, this.matrixType);
		
		//check data
		DZHelper.checkData(input);
		DZHelper.checkSize(input);
		DZHelper.checkData(right);
		DZHelper.checkSize(right);
		
		inferenceConfig = this.dataInference.getParameter(tp.idForInference, tp.paramIndex);
		
		STNLayer layer = null;
		// tp.idForTraining *NOT* tp.idForInference. because we should load training matrix saved in tp.idForTraining.
		layer = STNLayer.createForInference(input, right, inferenceConfig, savedTrainingParamName, workingFolder);
		layer.inference();	
		//Layer l3 = layer.net.getLayer(2);
		//System.out.println("mu:"+l3.batchNormalization.mu);
		//System.out.println("var:"+l3.batchNormalization.var);
		Matrix inferenced = layer.net.getInferencedResult();
		Matrix newOne = inferenced.copy(true);
				
		layer.release();
		input.release();
				
		return newOne;
	}
	
}
