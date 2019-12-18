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
import dolziplib.Layer.LayerType;
import dolziplib.ScoreBoard;
import dolziplib.matrix.Matrix;
import dolziplib.param.ParameterSpace;
import dzlearning.StockInfo;
import dzlearning.STData.Answer;

public class Worker extends Thread{

	public static final int RIGHT_ANSWER_TYPE_ONE_HOT=0;
	public static final int RIGHT_ANSWER_TYPE_RATE=1;
	int rightAnswerType;
	int matrixType = Matrix.MATRIX_TYPE_AVX;
	
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
	
	STData dataTraining = null;
	STData dataInference = null;
	CountDownLatch latch = null;
	String workingFolder = null;
	LinkedList<WorkerResult> results = new LinkedList<>();
	boolean doTraining = true;
	
	
	//targetDay : It will inference a week which includes the targetDay. yyyy-MM-dd
	// Automatically it finds Sunday of the week and Saturday of the week.
	public Worker(STData trainingData, STData dataInference,LinkedList<Integer> idStockInfo, CountDownLatch latch,String workingFolder,
			boolean doTraining, int rightAnswerType) throws ParseException, SQLException
	{

		this.rightAnswerType = rightAnswerType;
		this.latch = latch;

		if(idStockInfo.size()==0)return;
		
		this.dataTraining = trainingData;
		this.workingFolder = workingFolder;
		this.doTraining = doTraining;
		this.dataInference = dataInference;
		
		for(Integer id:idStockInfo)
		{
			WorkerResult result = new WorkerResult();
			result.idStockInfo = id.intValue();
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


		/*
		while(true)
		{
			
			if(inferenceStartDate.after(inferenceEndDate))break;
			String startDate = sdf.format(inferenceStartDate.getTime());
			inferenceStartDate.add(Calendar.DAY_OF_YEAR, 6);
			String endDate = sdf.format(inferenceStartDate.getTime());
			inferenceStartDate.add(Calendar.DAY_OF_YEAR, 1);
			
			
			for(WorkerResult result:results)
			{
				StockInfo stockInfo = DataGenerator.getStockInfoById(result.idStockInfo);
				InferencedStock is = new InferencedStock(stockInfo);
				LinkedList<Answer> answers =  dataInference.getRightAnswer(result.idStockInfo);
				
				is.targetStartDay = startDate;
				is.targetEndDay = endDate;
				result.inferencedStocks.add(is);
			}			
		}
		*/
		
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

		
	}
	
	@Override
	public void run()
	{
		
		for(WorkerResult result:results)
		{
			try {
					Matrix inferenced = inferenceSingle(result.idStockInfo);
					if(inferenced.getHeight() != result.inferencedStocks.size())
					{
						throw new Exception("inferencedHeight("+inferenced.getHeight()+") != inferencedStocks.size("+result.inferencedStocks.size()+")");
					}
					for(int i=0;i<inferenced.getHeight();i++)
					{
						InferencedStock is = result.inferencedStocks.get(i);
						is.getRealResult();
						
						if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
						{
							if(inferenced.getData(i, 0) > inferenced.getData(i, 1))is.inferenceResult = InferencedStock.RESULT_UNDER3PERCENT;
							else if(inferenced.getData(i, 0) <= inferenced.getData(i, 1)) is.inferenceResult = InferencedStock.RESULT_OVER3PERCENT; 
							else is.inferenceResult = InferencedStock.RESULT_UNKNOWN; //for NaN
						}
						else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
						{
							double v = STData.rateDeMapped(inferenced.getData(i, 0));
							if(v>=3.0)is.inferenceResult = InferencedStock.RESULT_OVER3PERCENT;
							else if(v<3.0)is.inferenceResult = InferencedStock.RESULT_UNDER3PERCENT;
							else is.inferenceResult = InferencedStock.RESULT_UNKNOWN; //for NaN
							is.inferenceResultRate = v;
						}
						
						if(is.realDungrakRate<3.0)is.realResult = InferencedStock.RESULT_UNDER3PERCENT;
						else if(is.realDungrakRate>=3.0)is.realResult = InferencedStock.RESULT_OVER3PERCENT;
						else is.realDungrakRate = InferencedStock.RESULT_UNKNOWN; //for NaN
						
						is.trainingCorrectRate = traniningCorrectRate;
						is.output = inferenced;
						
					}
					//inferenced.release();
					
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result.exceptionCode = e;
			}
		}
		this.latch.countDown();
	}
	
	private double traniningCorrectRate = Double.NaN;
	private Matrix inferenceSingle(int idStockInfo) throws Exception
	{
		
		StockInfo sinfo = DataGenerator.getStockInfoById(idStockInfo);
		System.out.println("stock:"+sinfo.name+"("+sinfo.code+") id:"+idStockInfo);
		
		Matrix input = Matrix.create(dataTraining.getHeightOfInput(), dataTraining.getWidthOfInput(), this.matrixType);
		input.setData(dataTraining.getInputData());
		Matrix right = null;
		if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
		{
			right = Matrix.create(dataTraining.getHeightOfAnswer(idStockInfo),2 , this.matrixType);
			right.setData(dataTraining.getRightAnswerData(idStockInfo,3.0));	
			//Matrix rightWithRate = Matrix.create(dataTraining.getHeightOfAnswer(idStockInfo),3 , Matrix.MATRIX_TYPE_AVX);
			//rightWithRate.setData(dataTraining.getRightAnswerWithRate(idStockInfo,3.0));
		}
		else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
		{
			right = Matrix.create(dataTraining.getHeightOfAnswer(idStockInfo),1 ,this.matrixType);
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
		DZConfig param = null;
		if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)param = DZConfig.getStaticParameter();
		else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)param = DZConfig.getStaticParameter(LayerType.LAST_LAYER_TYPE_MSE);
		ScoreBoard sb = new ScoreBoard();
		
		if(doTraining)
		{
			
			layer = STNLayer.createForTraining(input, right, param, sb, "training_"+idStockInfo, workingFolder, 200);
			long startT = System.nanoTime();
			layer.runEBPLoop(500, false, true);
			//layer.runEBPLoop(10, false, true);
			long endT = System.nanoTime();
			traniningCorrectRate = layer.getAvgCorrectRate();
			layer.saveToFile();
			System.out.println("end of tranining, "+(endT-startT)/1000000+" ms");
			layer.release();
			input.release();
			right.release();
		}


		Matrix inputInf = Matrix.create(dataInference.getHeightOfInput(), dataInference.getWidthOfInput(), Matrix.MATRIX_TYPE_AVX);
		inputInf.setData(dataInference.getInputData());
		Matrix rightInf = null;
		Matrix rightInfWithRate = null;
		if(rightAnswerType == RIGHT_ANSWER_TYPE_ONE_HOT)
		{
			rightInf = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
			rightInf.setData(dataInference.getRightAnswerData(idStockInfo,3.0));	
			rightInfWithRate = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),3 , Matrix.MATRIX_TYPE_AVX);
			rightInfWithRate.setData(dataInference.getRightAnswerWithRate(idStockInfo,3.0,true));
		}
		else if(rightAnswerType == RIGHT_ANSWER_TYPE_RATE)
		{
			rightInf = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),1 , Matrix.MATRIX_TYPE_AVX);
			rightInf.setData(dataInference.getRightAnswerMappedRate(idStockInfo));	
			rightInfWithRate = Matrix.create(dataInference.getHeightOfAnswer(idStockInfo),2 , Matrix.MATRIX_TYPE_AVX);
			rightInfWithRate.setData(dataInference.getRightAnswerMappedWithRealRate(idStockInfo,true));	
		}
		
		layer = STNLayer.createForInference(inputInf, rightInf, param, "training_"+idStockInfo, workingFolder, rightInfWithRate);
		layer.inference();	
		
		//Matrix tmp = DZHelper.mergeToRight(layer.net.getLasyLayer().output, layer.rightAnswerWithRate);
		//System.out.println("aaa:"+tmp);
		//tmp.release();
		
		Matrix inferenced = layer.net.getInferencedResult();
		Matrix newOne = inferenced.copy(true);
				
		layer.release();
		inputInf.release();
		rightInf.release();
		rightInfWithRate.release();
		

		return newOne;
	}
}
