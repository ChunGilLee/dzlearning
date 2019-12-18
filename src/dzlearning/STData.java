package dzlearning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import dolziplib.DZHelper;

public class STData
{
	public String inputStartDate;
	public String inputEndDate;
	public String rightStartDate;
	public String rightEndDate;
	
	public class Answer
	{
		String date;
		double rate;
	}
	
	public LinkedList<Answer> getRightAnswer(int idStockInfo)
	{
		for(AnswerData ad:rightAnswer)
		{
			if(ad.idStockInfo==idStockInfo)return ad.rightAnswer;
		}
		return null;
	}
	
	public class AnswerData
	{
		int idStockInfo;
		LinkedList<Answer> rightAnswer = new LinkedList<>();		
		
		private String toStringData()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(this.idStockInfo);
			for(Answer v:rightAnswer)
			{
				sb.append(',');
				sb.append(v.date);
				sb.append("_");
				sb.append(v.rate);
			}
			return sb.toString();
		}
		private void fromStringData(String data)
		{
			String sp[] = data.split(",");
			for(int i=0;i<sp.length;i++)
			{
				if(i==0)
				{
					this.idStockInfo = Integer.parseInt(sp[i]);
					continue;
				}
				String s2[] = sp[i].split("_");
				Answer answer = new Answer();
				answer.date = s2[0];
				answer.rate = Double.parseDouble(s2[1]);
				rightAnswer.add(answer);
			}
		}
	}
	
	LinkedList<double[]> inputData = new LinkedList<>();
	public LinkedList<AnswerData> rightAnswer = new LinkedList<>();
	
	private void putToMap(Map<String,Object> map)
	{
		int index=0;
		
		for(double[] data:inputData)
		{
			StringBuilder sb = null;
			for(int i=0;i<data.length;i++)
			{
				if(sb==null)
				{
					sb= new StringBuilder();
					sb.append(data[i]);					
				}
				else 
				{
					sb.append(',');
					sb.append(data[i]);
				}
			}
			map.put("input_data_"+index, sb.toString());
			index++;
		}
		map.put("input_start_date", this.inputStartDate);
		map.put("input_end_date", this.inputEndDate);
		map.put("right_start_date", this.rightStartDate);
		map.put("right_end_date", this.rightEndDate);
		
	}
	private void getFromMap(Map<String,Object> map)
	{
		int index=0;
		String inputDataS = (String)map.get("input_data_"+index);
		while(inputDataS!=null)
		{
			String sp[] = inputDataS.split(",");
			double[] v = new double[sp.length];
			//System.out.println("number of v:"+v.length);
			for(int i=0;i<v.length;i++)
			{
				v[i] = Double.parseDouble(sp[i]);
			}
			this.inputData.add(v);
			index++;
			inputDataS = (String)map.get("input_data_"+index);
		}
		this.inputStartDate = (String)map.get("input_start_date");
		this.inputEndDate = (String)map.get("input_end_date");
		this.rightStartDate = (String)map.get("right_start_date");
		this.rightEndDate = (String)map.get("right_end_date");
	}
	
	public void saveToFile(String path) throws Exception
	{
		HashMap<String, Object> map = new HashMap<>();
		int index=0;
		for(AnswerData adata:this.rightAnswer)
		{
			String stringData = adata.toStringData();
			map.put("answer_data_"+index, stringData);
			index++;
		}
		this.putToMap(map);
		DZHelper.saveFileFromMap(map, path);
	}
	public static STData loadFromFile(String path) throws Exception
	{
		STData stdata = new STData();
		stdata.rightAnswer.clear();
		stdata.inputData.clear();
		Map<String, Object> map = DZHelper.loadFileFromMap(path);
		int index=0;
		while(true)
		{
			String adata = (String)map.get("answer_data_"+index);
			if(adata==null)break;
			AnswerData answerData = stdata.new AnswerData();
			answerData.fromStringData(adata);
			stdata.rightAnswer.add(answerData);
			index++;
		}
		stdata.getFromMap(map);
		return stdata;
	}
	
	public int getWidthOfInput()
	{
		double[] first = inputData.getFirst();
		return first.length;
	}
	public int getHeightOfInput()
	{
		return inputData.size();
	}
	public int getHeightOfAnswer(int idStockInfo)
	{
		for(AnswerData ad : rightAnswer)
		{
			if(ad.idStockInfo==idStockInfo)return ad.rightAnswer.size();
		}
		return 0;
	}
	
	public void addRightAnswer(int idStockInfo, double v, String date)
	{
		if(Double.isNaN(v))
		{
			throw new RuntimeException("right answer is NaN"); 
		}
		boolean exist=false;
		for(AnswerData ad:rightAnswer)
		{
			if(ad.idStockInfo==idStockInfo)
			{
				Answer a = new Answer();
				a.date = date;
				a.rate = v;
				ad.rightAnswer.add(a);
				exist =true;
			}
		}
		if(exist==false)
		{
			AnswerData ad = new AnswerData();
			ad.idStockInfo = idStockInfo;
			Answer a = new Answer();
			a.date = date;
			a.rate = v;
			ad.rightAnswer.add(a);
			rightAnswer.add(ad);
		}
	}
	
	public double[][] getInputData()
	{
		double[][] ret = new double[inputData.size()][];
		int index=0;
		int width=0;
		for(double[] in:inputData)
		{
			if(width==0)width = in.length;
			if(width!=in.length)throw new RuntimeException("width of input data is not equal.");
			ret[index] = in;
			index++;
		}
		return ret;
	}
	

	public double[][] getRightAnswerData(int idStockInfo,double baseRate)
	{
		return getRightAnswerWithRate(idStockInfo, baseRate, false);
	}
	public double[][] getRightAnswerWithRate(int idStockInfo, double baseRate, boolean withRate)
	{
		int index=0;
		for(AnswerData ad:rightAnswer)
		{
			if(ad.idStockInfo == idStockInfo)
			{
				if(inputData.size()!=ad.rightAnswer.size())throw new RuntimeException("height of input data is not equal to right answer height.");
				double[][] ret = null;
				if(withRate)ret = new double[inputData.size()][3];
				else ret = new double[inputData.size()][2];
				for(Answer d:ad.rightAnswer)
				{
					//System.out.println("aaaabbbb:"+d.doubleValue()+" baseRate:"+baseRate);
					
					if(d.rate>=baseRate)
					{
						ret[index][0] = 0.0;
						ret[index][1] = 1.0;
					}
					else
					{
						ret[index][0] = 1.0;
						ret[index][1] = 0.0;
					}
					if(withRate)ret[index][2] = d.rate;
					index++;
				}
				return ret;
			}
		}
		return null;
	}
	
	private static double rateMapped(double v)
	{
		//-35.0 ~ +35.0 ===> 0.0 ~ 10.0
		if(v<-35.0)v=(-35.0);
		else if(v>35.0)v=35.0;
		v = (v+35.0)/7.0;
		return v;
	}
	public static double rateDeMapped(double v)
	{
		v = v * 7.0;
		v = v-35.0;
		return v;
	}
	
	public double[][] getRightAnswerMappedRate(int idStockInfo)
	{
		return getRightAnswerMappedWithRealRate(idStockInfo,false);
	}
	public double[][] getRightAnswerMappedWithRealRate(int idStockInfo, boolean withRate)
	{
		int index=0;
		for(AnswerData ad:rightAnswer)
		{
			if(ad.idStockInfo == idStockInfo)
			{
				if(inputData.size()!=ad.rightAnswer.size())throw new RuntimeException("height of input data is not equal to right answer height.");
				double[][] ret = null;
				int w = 1;
				if(withRate)w=2;
				ret = new double[inputData.size()][w];
				for(Answer d:ad.rightAnswer)
				{
					ret[index][0] = rateMapped(d.rate);
					if(withRate)ret[index][1] = d.rate;
					index++;
				}
				return ret;
			}
		}
		return null;
	}
}