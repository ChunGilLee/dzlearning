package dzlearning;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.Layer.LayerType;
import dzlearning.HData.FourWeek;
import dzlearning.HData.Stock;


public class HDataResult implements WorkerInferenceResult{
	HData parent = null;
	
	int investedCount=0;
	double profitSum=0.0;
	
	private class TrainingCorrectRate
	{
		double rate;
		int idStockInfo;
		StockInfo stockInfo;
		DZConfig config;
		String paramPrefix;
	}
	private class Invest
	{
		int idStockInfo;
		int inferencedIndex;
		String date;
		double inferencedRate;
		double realRate;
		int investCount=0;
		
		public Invest copy()
		{
			Invest newOne = new Invest();
			newOne.idStockInfo = this.idStockInfo;
			newOne.inferencedIndex = this.inferencedIndex;
			newOne.date = this.date;
			newOne.inferencedRate = this.inferencedRate;
			newOne.realRate = this.realRate;
			newOne.investCount = this.investCount;
			return newOne;
		}
		public String toString()
		{
			return String.format("Invest [%d] %s RealRate:%f investCount:%d", 
					this.idStockInfo,this.date,this.realRate,this.investCount);
		}
	}
	private class InferenceCorrectRate
	{
		double correctRate;
		int idStockInfo;
		DZConfig config;
		StockInfo stockInfo;
		String paramPrefix;
		LinkedList<Invest> investments = new LinkedList<>();
	}
	
	LinkedList<TrainingCorrectRate> trainCorrectRateStore = new LinkedList<>();
	LinkedList<InferenceCorrectRate> inferenceCorrectRateStore = new LinkedList<>();
	LinkedList<Invest> globalInvest = new LinkedList<>();
	
	private Invest getGlobalInvest(Invest invest)
	{
		for(Invest global:globalInvest)
		{
			if(global.idStockInfo==invest.idStockInfo && global.date.equals(invest.date))return global;
		}
		Invest investNew = invest.copy();
		this.globalInvest.add(investNew);
		return investNew;
	}
	private void sortGlobalInvest()
	{
		Collections.sort(this.globalInvest,new Comparator<Invest>() {
			@Override
			public int compare(Invest o1, Invest o2) {
				// TODO Auto-generated method stub
				if(o2.investCount>o1.investCount)return 1;
				else if(o2.investCount<o1.investCount)return -1;
				return 0;
			}
		});
	}
	
	@Override
	public synchronized void setInferenceResult(int idStockInfo, double[][] result, double trainingCorrectRate,Exception error, DZConfig config, String paramPrefix) {
		// TODO Auto-generated method stub
		if(error!=null)
		{
			System.out.println("Inference Error !! "+idStockInfo+","+error.getMessage());
		}
		else
		{
			StockInfo stockInfo= null;
			try {
				stockInfo = Data.getStockInfoById(idStockInfo);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Stock stock = parent.getStock(idStockInfo);
			
			if(stock.fourWeeks.size()!=result.length)throw new RuntimeException("Inferenced result height not matched to input height.");
			int count=0;
			int correctCount=0;
			InferenceCorrectRate icr = new InferenceCorrectRate();
			icr.idStockInfo = idStockInfo;
			icr.paramPrefix = paramPrefix;
			double correctRate = 0;
			for(FourWeek fw:stock.fourWeeks)
			{
				if(parent.lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
				{
					int indexOfInferenced = DZHelper.getIndexOfMax(result[count]);
					int indexOfAnswer = DZHelper.getIndexOfOneHotEncoding(parent.ceeAnswer(null, fw.answerData));
					DZHelper.arrayToCommaSeperatedString(parent.ids);
					System.out.printf("%s(%d) %s InferencedIndex:%d realIndex:%d(%f) %s\n",
							stockInfo.name,stockInfo.idStockInfo,fw.targetDay,indexOfInferenced,indexOfAnswer,fw.answerData,
							indexOfInferenced==indexOfAnswer? "OK":"FAILED");
					//DZHelper.printArray("inferened:", result[count]);
					//DZHelper.printArray("real:"+fw.answerData+"%, ",ceeAnswer(null, fw.answerData));
					
					if(indexOfInferenced==indexOfAnswer)correctCount++;
					
					
					if(((parent.numOfAnswerD%2)!=0 && indexOfInferenced>parent.numOfAnswerD/2) ||
							((parent.numOfAnswerD%2)==0 && indexOfInferenced>=parent.numOfAnswerD/2))
					{
						profitSum += fw.answerData;
						investedCount++;
	
						Invest invest = new Invest();
						invest.idStockInfo = icr.idStockInfo;
						invest.date = fw.targetDay;
						invest.realRate = fw.answerData;
						invest.inferencedIndex = indexOfInferenced;
						icr.investments.add(invest);
						
						Invest global = this.getGlobalInvest(invest);
						global.investCount++;
					}
					correctRate = (double)correctCount/(double)(count+1);
					//System.out.printf("correctCount %d, count %d correctRate:%f $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n",correctCount, count, correctRate);
				}
				else if(parent.lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
				{
					if(result[count].length!=1)throw new RuntimeException("invalid result width");
					double inferencedRate = result[count][0];
					double diffRate = fw.answerData - inferencedRate;
					if(diffRate<0)diffRate=diffRate*(-1);
					
					System.out.printf("%s(%d) %s InferencedRate:%f realRate:%f diff:%f %s\n",
							stockInfo.name,stockInfo.idStockInfo,fw.targetDay,inferencedRate,fw.answerData,
							diffRate,inferencedRate>=1.0 ? "Invest":"" );
					
					if(inferencedRate>=1.0)
					{
						profitSum += fw.answerData;
						investedCount++;
	
						Invest invest = new Invest();
						invest.date = fw.targetDay;
						invest.realRate = fw.answerData;
						invest.inferencedRate = inferencedRate;
						icr.investments.add(invest);						
					}
					
					correctRate = diffRate/(double)(count+1);
				}
				else
				{
					throw new RuntimeException("invalid last layer type");
				}
				
				count++;
			}

			icr.correctRate = correctRate;
			//icr.realRate = fw.
			icr.stockInfo = stockInfo;
			icr.config = config;
			this.inferenceCorrectRateStore.add(icr);
			System.out.printf("set Inference result %s(%d), correct rate:%f configIndex:%d\n",
					stockInfo.name,stockInfo.idStockInfo,icr.correctRate,icr.config.configIndex);

			//System.out.println("total:"+count+" correct:"+correctCount+" rate:"+correctRate);
		}
	}

	private void printTrainingResult()
	{
		if(trainCorrectRateStore.size()==0)
		{
			System.out.println("There is no training result");
			return;
		}
		System.out.println("Num of training result:"+this.trainCorrectRateStore.size());
		
		Collections.sort(this.trainCorrectRateStore,new Comparator<TrainingCorrectRate>() {
			@Override
			public int compare(TrainingCorrectRate o1, TrainingCorrectRate o2) {
				// TODO Auto-generated method stub
				if(o2.rate>o1.rate)return 1;
				else if(o2.rate<o1.rate)return -1;
				return 0;
			}
		});
		
		double sum = 0.0;
		for(TrainingCorrectRate tcr:this.trainCorrectRateStore)
		{
			System.out.printf("training CorrectRate %s(%d):%f ,config:%d, param:%s\n",tcr.stockInfo.name,tcr.idStockInfo,tcr.rate,tcr.config.configIndex,tcr.paramPrefix);
			sum += tcr.rate;
		}
		if(this.trainCorrectRateStore.size()!=0)
		{
			double avg = sum/this.trainCorrectRateStore.size();
			double var = 0.0;
			for(TrainingCorrectRate tcr:this.trainCorrectRateStore)
			{
				var += Math.sqrt((tcr.rate - avg)*(tcr.rate - avg));
			}
			var = var/this.trainCorrectRateStore.size();
			System.out.printf("Average training CorrectRate : %f, var:%f\n",avg,var);
		}
	}
	
	private void printInferenceResult()
	{
		if(inferenceCorrectRateStore.size()==0)
		{
			System.out.println("There is no inference result");
			return;
		}
		System.out.println("Num of inference result:"+this.inferenceCorrectRateStore.size());
		
		
		Collections.sort(this.inferenceCorrectRateStore,new Comparator<InferenceCorrectRate>() {
			@Override
			public int compare(InferenceCorrectRate o1, InferenceCorrectRate o2) {
				// TODO Auto-generated method stub
				if(o2.correctRate>o1.correctRate)return 1;
				else if(o2.correctRate<o1.correctRate)return -1;
				return 0;
			}
		});
		
		
		double sum = 0.0;
		for(InferenceCorrectRate tcr:this.inferenceCorrectRateStore)
		{
			System.out.printf("inference CorrectRate %s(%d) : %f config:%d, param:%s\n",tcr.stockInfo.name,tcr.idStockInfo,tcr.correctRate,tcr.config.configIndex,tcr.paramPrefix);
			sum += tcr.correctRate;
		}
		if(this.inferenceCorrectRateStore.size()!=0)
		{
			double avg = sum/this.inferenceCorrectRateStore.size();
			double var = 0.0;
			for(InferenceCorrectRate tcr:this.inferenceCorrectRateStore)
			{
				var += Math.sqrt((tcr.correctRate - avg)*(tcr.correctRate - avg));
			}
			var = var/this.inferenceCorrectRateStore.size();
			System.out.printf("Average inference CorrectRate : %f, var:%f\n",avg,var);
		}
		
		double avgProfit = 0.0;
		int investCount=0;
		for(InferenceCorrectRate tcr:this.inferenceCorrectRateStore)
		{
			for(Invest invest:tcr.investments)
			{
				avgProfit += invest.realRate;
				investCount++;
				//System.out.printf("invest : %s(%d) %s %d %f\n",tcr.stockInfo.name,tcr.idStockInfo,
				//		invest.date,invest.inferencedIndex,invest.realRate);
			}
		}
		if(investCount==0)System.out.println("Average Profit : There is no investment.");
		else 
		{
			this.sortGlobalInvest();
			for(Invest iv:this.globalInvest)
			{
				System.out.println(iv);
			}
			System.out.printf("Average Profit : %f(%d)\n",avgProfit/(double)investCount,investCount);
		}
	}
	
	@Override
	public void printResult() {
		// TODO Auto-generated method stub
		
		printTrainingResult();
		printInferenceResult();
	}

	@Override
	public synchronized void setTrainingResult(int idStockInfo, double trainingCorrectRate, Exception error, DZConfig config, String paramPrefix) {
		// TODO Auto-generated method stub
		
		//System.out.println("training result insertion!!!!!!!"+this);
		
		if(error!=null)
		{
			System.out.println("Training Error !! "+idStockInfo+","+error.getMessage());
		}
		else
		{	
			StockInfo stockInfo= null;
			try {
				stockInfo = Data.getStockInfoById(idStockInfo);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TrainingCorrectRate tcr = new TrainingCorrectRate();
			tcr.rate = trainingCorrectRate;
			tcr.idStockInfo = idStockInfo;
			tcr.stockInfo = stockInfo;
			tcr.config = config;
			tcr.paramPrefix = paramPrefix;
			this.trainCorrectRateStore.add(tcr);
			System.out.printf("set Tranining result %s(%d), correct rate:%f configIndex:%d\n",
					stockInfo.name,stockInfo.idStockInfo,tcr.rate,config.configIndex);
		}
	}
	
	@Override
	public double getTrainingCorrectRate() {
		// TODO Auto-generated method stub
		
		double sum = 0.0;
		for(TrainingCorrectRate tcr:this.trainCorrectRateStore)
		{
			System.out.printf("training CorrectRate %s(%d) : %f\n",tcr.stockInfo.name,tcr.idStockInfo,tcr.rate);
			sum += tcr.rate;
		}
		if(this.trainCorrectRateStore.size()!=0)
		{
			double avg = sum/this.trainCorrectRateStore.size();
			double var = 0.0;
			for(TrainingCorrectRate tcr:this.trainCorrectRateStore)
			{
				var += Math.sqrt((tcr.rate - avg)*(tcr.rate - avg));
			}
			var = var/this.trainCorrectRateStore.size();
			return avg;
		}
		else return 0.0;
	}

	@Override
	public double getInferenceCorrectRate() {
		// TODO Auto-generated method stub
		
		double sum = 0.0;
		for(InferenceCorrectRate tcr:this.inferenceCorrectRateStore)
		{
			System.out.printf("inference CorrectRate %s(%d) : %f\n",tcr.stockInfo.name,tcr.idStockInfo,tcr.correctRate);
			sum += tcr.correctRate;
		}
		
		if(this.inferenceCorrectRateStore.size()!=0)
		{
			double avg = sum/this.inferenceCorrectRateStore.size();
			double var = 0.0;
			for(InferenceCorrectRate tcr:this.inferenceCorrectRateStore)
			{
				var += Math.sqrt((tcr.correctRate - avg)*(tcr.correctRate - avg));
			}
			var = var/this.inferenceCorrectRateStore.size();
			return avg;
		}
		else return 0.0;
	}
}
