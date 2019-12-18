package dzlearning;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dzlearning.DData.FourWeek;
import dzlearning.DData.Stock;


public class DDataResult implements WorkerInferenceResult{
	DData parent = null;
	
	int investedCount=0;
	double profitSum=0.0;
	
	private class TrainingCorrectRate
	{
		double rate;
		int idStockInfo;
		StockInfo stockInfo;
	}
	private class Invest
	{
		int inferencedIndex;
		String date;
		double realRate;
	}
	private class InferenceCorrectRate
	{
		double correctRate;
		int idStockInfo;
		StockInfo stockInfo;
		LinkedList<Invest> investments = new LinkedList<>();
	}
	
	LinkedList<TrainingCorrectRate> trainCorrectRateStore = new LinkedList<>();
	LinkedList<InferenceCorrectRate> inferenceCorrectRateStore = new LinkedList<>();
	
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
			for(FourWeek fw:stock.fourWeeks)
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
				
				if(indexOfInferenced>=4)
				{
					profitSum += fw.answerData;
					investedCount++;
					//System.out.println("average profit:"+profitSum/(double)investedCount);
				}
				
				if(indexOfInferenced>3)
				{
					Invest invest = new Invest();
					invest.date = fw.targetDay;
					invest.realRate = fw.answerData;
					invest.inferencedIndex = indexOfInferenced;
					icr.investments.add(invest);
				}
				
				count++;
			}
			double correctRate = (double)correctCount/(double)count;
			icr.idStockInfo = idStockInfo;
			icr.correctRate = correctRate;
			//icr.realRate = fw.
			icr.stockInfo = stockInfo;
			this.inferenceCorrectRateStore.add(icr);
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
		else System.out.printf("Average Profit : %f\n",avgProfit/(double)investCount);
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
			this.trainCorrectRateStore.add(tcr);
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
