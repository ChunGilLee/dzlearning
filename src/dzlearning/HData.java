package dzlearning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.DoubleBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import AddData.AdditionalData;
import AddData.KospiAdditionalData;
import AddData.USDAdditionalData;
import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.DZMath;
import dolziplib.HeteroLayerConfig;
import dolziplib.Layer;
import dolziplib.HeteroLayerConfig.HeteroLayerConfigElement;
import dolziplib.Layer.LayerType;
import dolziplib.matrix.Matrix;

/**
 * @author highvolt
 * input : 단일 stock에 대해서 inference하고자 하는 대상의 가장 마지막 28일(4주)의 등략율과 다른 속성의 데이터를 HeteroLayer로 input data 사용
 * right answer : 해당일의 등략율
 */
public class HData implements Data {

	//saveFile에서 저장이 안되는것들. 추후 구현 필요
	private static final int NumOfDailyStockForEachStock=80;
	
	private static final int NumOfKospiData=20; // set to zero for disable.
	private static final int NumOfCurrencyUSDData=20; // set to zero for disable.
	private static final int NumOfEconomicGrowth=1; // for months. 0 or 1
	private static final int NumOfCallRate=20; //콜금리, set to zero for disable.
	private static final boolean includeSeasonality = true;
	
	private static final boolean bypassMainStockData = true;
	
	private static final int NumOfDayForAnswer = 20;
	
	int numOfAnswerD = 3;
	
	int lastLayerType = LayerType.LAST_LAYER_TYPE_CEE;
	String startDate; //inference 하고자 하는 날짜 기준.
	String endDate;   //inference 하고자 하는 날짜 기준.
	LinkedList<Stock> stocks = new LinkedList<>();
	public int ids[] = null;
	
	@Override
	public void printDebug(int idStockInfo)
	{
		Stock stock = this.getStock(idStockInfo);
		
		for(FourWeek fw:stock.fourWeeks)
		{
			System.out.printf("id:%d inputDataStartDay:%s inputDataEndDay:%s targetDay:%s\n",
					stock.idStockInfo,
					fw.startDay,fw.endDay,fw.targetDay);
		}
	}
	
	public JsonObject getJson()
	{
		JsonArray stocks = new JsonArray();
		int numOfStock = this.stocks.size();
		int count =0;
		for(Stock stock:this.stocks)
		{
			System.out.printf("[%d/%d] json loading %d\n",count,numOfStock,stock.idStockInfo);
			stocks.add(stock.getJson());
			count++;
		}
		JsonObject json = new JsonObject();
		json.add("stocks", stocks);
		json.addProperty("startDate", this.startDate);
		json.addProperty("endDate", this.endDate);
		json.addProperty("lastLayerType", this.lastLayerType);
		JsonArray ids = new JsonArray();
		for(int i=0;i<this.ids.length;i++)
		{
			ids.add(this.ids[i]);
		}
		json.add("ids", ids);
		
		return json;
	}
	public void getFromJson(JsonObject json) throws ParseException
	{
		JsonArray stocks = json.get("stocks").getAsJsonArray();
		for(JsonElement e:stocks)
		{
			this.stocks.add(Stock.getFromJson(e.getAsJsonObject(),this));
		}
		
		this.startDate = json.get("startDate").getAsString();
		this.endDate = json.get("endDate").getAsString();
		this.lastLayerType = json.get("lastLayerType").getAsInt();
		JsonArray ids = json.get("ids").getAsJsonArray();
		this.ids = new int[ids.size()];
		int count=0;
		for(JsonElement e:ids)
		{
			this.ids[count] = e.getAsInt();
			count++;
		}
		
		return;
	}
	
		
	public static HData getFromDB(String startDay, String endDay, int ids[]) throws Exception
	{
		HData wdata =new HData();
		
		wdata.startDate = startDay;
		wdata.endDate = endDay;
		
		if(ids==null)ids = wdata.getInputDataTargetCompany();
		wdata.ids = ids;
		
		Connection conn = DB.getConn();
		try {
			for(int i=0;i<wdata.ids.length;i++)
			{
				System.out.printf("[%d/%d] loading %d\n",i,wdata.ids.length,wdata.ids[i]);
				Stock stock = Stock.getFromDB(conn, wdata.ids[i], wdata.startDate, wdata.endDate, wdata);
				if(stock!=null)wdata.stocks.add(stock);
			}
		}catch(Exception ex)
		{
			conn.close();
			throw ex;
		}
		conn.close();
		
		return wdata;
	}
	

	public static class FourWeek
	{
		String startDay = null;
		String endDay = null;
		String targetDay = null;
		int idStockInfo;
		//public static int DATA_LENGTH=NumOfDailyStockForEachStock + NumOfKospiData;
		//double[] data = new double[DATA_LENGTH];
		double[] data = null;
		double answerData = Double.NaN;
		static String today = null;
		
		public JsonObject getJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("startDay", this.startDay);
			json.addProperty("endDay", this.endDay);
			json.addProperty("targetDay", this.targetDay);
			json.addProperty("idStockInfo", this.idStockInfo);
			json.addProperty("answerData", this.answerData);
			JsonArray data = new JsonArray();
			for(int i=0;i<this.data.length;i++)
			{
				data.add(this.data[i]);
			}
			json.add("data", data);
			return json;
		}
		public static FourWeek getFromJson(JsonObject json)
		{
			FourWeek fw = new FourWeek();
			fw.startDay = json.get("startDay").getAsString();
			fw.endDay = json.get("endDay").getAsString();
			fw.targetDay = json.get("targetDay").getAsString();
			fw.idStockInfo = json.get("idStockInfo").getAsInt();
			fw.answerData = json.get("answerData").getAsDouble();
			JsonArray data = json.get("data").getAsJsonArray();
			if(data.size()!=fw.data.length)
			{
				throw new RuntimeException("data size not matched "+data.size()+" <=> "+fw.data.length+" id:"+fw.idStockInfo);
			}
			int count=0;
			for(JsonElement e:data)
			{
				fw.data[count]=e.getAsDouble();
				count++;
			}
			return fw;
		}
		
		private synchronized static String getToday()
		{
			if(today!=null)return today;
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			today = sdf.format(cal.getTime());
			return today;
		}
		
		//targetDay : inference 하고자 하는 날짜. 즉 answer data가 있는 날짜
		public static FourWeek getFromArray(SimpleData sd[], int idStockInfo, String targetDay, LinkedList<AdditionalData> additionalData) throws Exception
		{
			FourWeek fourWeek = new FourWeek();
			fourWeek.idStockInfo = idStockInfo;
			fourWeek.targetDay = targetDay;
			
			int dataLen = NumOfDailyStockForEachStock;
			if(includeSeasonality)dataLen += 12;
			for(AdditionalData ad:additionalData)
			{
				dataLen += ad.getNumOfDataInput();
			}			
			fourWeek.data = new double[dataLen];
						
			//for the stock.
			for(int i=0;i<sd.length;i++)
			{
				if(sd[i].date.equals(targetDay))
				{
					if(NumOfDayForAnswer<=1)
					{
						fourWeek.answerData = sd[i].rate;
					}
					else
					{
						double stackedRate=100.0;
						for(int j=0;j<NumOfDayForAnswer;j++)
						{
							stackedRate = stackedRate + stackedRate * sd[i-j].rate * 0.01;
						}
						fourWeek.answerData = stackedRate - 100.0;
					}
				}
				if(sd[i].date.compareTo(targetDay)<0)
				{
					fourWeek.endDay = sd[i].date;
					for(int j=0;j<NumOfDailyStockForEachStock;j++)
					{
						fourWeek.data[j] =  sd[i+j].rate;
						fourWeek.startDay = sd[i+j].date;
					}
					break;
				}
			}
			
			if(includeSeasonality)
			{
				//extract month
				String monthS = targetDay.substring(5, 7);
				int month = Integer.parseInt(monthS);
				month--;
				for(int i=0;i<12;i++)
				{
					if(month==i)fourWeek.data[NumOfDailyStockForEachStock+i] = 1.0;
					else fourWeek.data[NumOfDailyStockForEachStock+i] = 0.0;
				}
			}
			
			int currentDataIndex=NumOfDailyStockForEachStock;
			if(includeSeasonality)currentDataIndex+=12;
			
			
			for(AdditionalData ad:additionalData)
			{
				if(ad.getNumOfDataInput()==0)continue;
				LinkedList<SimpleData> data = ad.getData();
				int savedCount=0;
				for(SimpleData adsd:data)
				{
					if(adsd.date.compareTo(targetDay)<0)
					{
						fourWeek.data[currentDataIndex] = adsd.rate;
						currentDataIndex++;
						savedCount++;
						if(savedCount==ad.getNumOfDataInput())break;
					}
				}
				
				if(savedCount!=ad.getNumOfDataInput())
				{
					throw new RuntimeException("Not enough additional data, "+ad.getName()+" requirement:"+ad.getNumOfDataInput()+" acturally:"+savedCount);
				}
			}
			
			
			
			if(Double.isNaN(fourWeek.answerData))
			{
				if(targetDay.compareTo(FourWeek.getToday())>=0)
				{
					// answerData가 NaN이여도 상관없음. 오늘 이후 날짜는 원래 데이터가 없으니깐.
				}
				else
				{
					//targetData가 없는 경우임. 즉, 주식이 개장하지 않은 날로 보아야 함.
					return null;
				}
			}
			
			return fourWeek;
		}
		
		//targetDay : inference 하고자 하는 날짜. 즉 answer data가 있는 날짜
		/* AdditionalData 가 구현되지 않음.
		public static FourWeek getFromDB(Connection conn, int idStockInfo, String targetDay) throws Exception
		{
			FourWeek fourWeek = new FourWeek();
			fourWeek.idStockInfo = idStockInfo;
			fourWeek.targetDay = targetDay;
			int count=0;

			//System.out.println("fourweek target day:"+targetDay);
			
				PreparedStatement ps = conn.prepareStatement("select * from DailyStock where id_stock_info=? and date<? order by date desc limit "+FourWeek.DATA_LENGTH);
				ps.setInt(1, idStockInfo);
				ps.setString(2, targetDay);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
				{
					String date = rs.getString("date");
					if(fourWeek.startDay==null)fourWeek.startDay = date;
					fourWeek.endDay = date;
					fourWeek.data[count] = rs.getDouble("dungrak_rate");
					count++;
				}
				rs.close();
				ps.close();
				
				ps = conn.prepareStatement("select * from DailyStock where id_stock_info=? and date=?");
				ps.setInt(1, idStockInfo);
				ps.setString(2, targetDay);
				rs = ps.executeQuery();
				if(rs.next())
				{
					fourWeek.answerData = rs.getDouble("dungrak_rate");
				}
				rs.close();
				ps.close();
				
				if(count!=fourWeek.data.length)
				{
					String msg = String.format("data length is not matched to num of data in DB %d<=>%d. id:%d, date:%s",
							fourWeek.data.length,count,idStockInfo,targetDay);
					throw new Exception(msg);
				}
				
				if(Double.isNaN(fourWeek.answerData))
				{
					if(targetDay.compareTo(FourWeek.getToday())>=0)
					{
						// answerData가 NaN이여도 상관없음. 오늘 이후 날짜는 원래 데이터가 없으니깐.
					}
					else
					{
						//targetData가 없는 경우임. 즉, 주식이 개장하지 않은 날로 보아야 함.
						return null;
					}
				}

			return fourWeek;
		}
		*/
	}
	
	
	private LinkedList<AdditionalData> additionalData = new LinkedList<>();
	private LinkedList<AdditionalData> getAdditionalData(Connection conn) throws SQLException
	{
		synchronized (additionalData) {
			if(additionalData.size()>0)return additionalData;
			if(NumOfKospiData>0)additionalData.add(AdditionalData.getData(AdditionalData.KOSPI, NumOfKospiData, conn));
			if(NumOfCurrencyUSDData>0)additionalData.add(AdditionalData.getData(AdditionalData.CURRENCY_USD, NumOfCurrencyUSDData, conn));
			if(NumOfEconomicGrowth>0)additionalData.add(AdditionalData.getData(AdditionalData.ECONOMIC_GROWTH, NumOfEconomicGrowth, conn));
			if(NumOfCallRate >0)additionalData.add(AdditionalData.getData(AdditionalData.ECONOMIC_GROWTH, NumOfEconomicGrowth, conn));
			return additionalData;
		}
	}
	
	public static class Stock
	{
		int idStockInfo;
		LinkedList<FourWeek> fourWeeks = new LinkedList<>();
		HData parent = null;
			
		private JsonObject getJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("idStockInfo", this.idStockInfo);
			JsonArray fourWeeks = new JsonArray();
			for(FourWeek fw:this.fourWeeks)
			{
				fourWeeks.add(fw.getJson());
			}
			json.add("fourWeeks", fourWeeks);
			return json;
		}
		public static Stock getFromJson(JsonObject json, HData parent)
		{
			Stock stock = new Stock();
			stock.parent = parent;
			stock.idStockInfo = json.get("idStockInfo").getAsInt();
			JsonArray fourWeeks = json.get("fourWeeks").getAsJsonArray();
			for(JsonElement e:fourWeeks)
			{
				stock.fourWeeks.add(FourWeek.getFromJson(e.getAsJsonObject()));
			}
			return stock;
		}
		

		
		public static Stock getFromDB(Connection conn, int idStockInfo, String targetStartDay, String targetEndDay, HData parent) throws Exception
		{
			Stock stock = new Stock();
			stock.parent = parent;
			stock.idStockInfo = idStockInfo;
			Calendar startDay = WData.getCalendarFromString(targetStartDay);
			Calendar endDay = WData.getCalendarFromString(targetEndDay);
			
			LinkedList<SimpleData> sdList = new LinkedList<>();
			PreparedStatement ps = conn.prepareStatement("select date,dungrak_rate from DailyStock where id_stock_info=? order by date desc");
			ps.setInt(1, idStockInfo);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				SimpleData sd = new SimpleData();
				sd.date = rs.getString("date");
				sd.rate = rs.getDouble("dungrak_rate");
				sdList.add(sd);
			}
			rs.close();
			ps.close();
			
			SimpleData sdArray[] = sdList.toArray(new SimpleData[0]);
			sdList.clear();
			sdList = null;
			
			LinkedList<AdditionalData> additionalData = parent.getAdditionalData(conn);
			
			while(startDay.after(endDay)==false)
			{
				//FourWeek fw = FourWeek.getFromDB(conn, idStockInfo, WData.getDateString(startDay));
				FourWeek fw = FourWeek.getFromArray(sdArray, idStockInfo, WData.getDateString(startDay),additionalData);
				if(fw!=null)stock.fourWeeks.add(fw);
				
				startDay.add(Calendar.DAY_OF_YEAR, 1);
			}
			
			if(stock.fourWeeks.size()==0)
			{
				throw new Exception("there is not input data. id:"+idStockInfo+" startDay:"+targetEndDay+", endDay:"+targetEndDay);
			}
			
			return stock;
		}
		
	}
	
	
	public double[] ceeAnswer(double[] ret, double answerData)
	{
		int rightIndex = -1;
		
		double scailingFactor = 1.0;
		if(NumOfDayForAnswer>1)scailingFactor = 1.0 + ((double)NumOfDayForAnswer)/20.0;
		
		if(ret==null)ret = new double[this.numOfAnswerD];	
		for(int i=0;i<ret.length;i++)ret[i] = Double.NaN;
		
		if(this.numOfAnswerD==2)
		{
			if(answerData>=1.0*scailingFactor)rightIndex=1;
			else rightIndex=0;
		}
		else if(this.numOfAnswerD==3)
		{
			if(answerData<-1.0*scailingFactor)rightIndex=0;
			else if(answerData>=-1.0*scailingFactor && answerData<=1.0*scailingFactor)rightIndex=1;
			else rightIndex=2;
		}
		else if(this.numOfAnswerD==7)
		{
			if(answerData>=3.0*scailingFactor)rightIndex=6;
			else if(answerData<3.0*scailingFactor && answerData>=2.0*scailingFactor)rightIndex=5;
			else if(answerData<2.0*scailingFactor && answerData>=1.0*scailingFactor)rightIndex=4;
			else if(answerData<1.0*scailingFactor && answerData>=0.0*scailingFactor)rightIndex=3;
			else if(answerData<0.0*scailingFactor && answerData>=-1.0*scailingFactor)rightIndex=2;
			else if(answerData<-1.0*scailingFactor && answerData>=-2.0*scailingFactor)rightIndex=1;
			else if(answerData<-2.0*scailingFactor)rightIndex=0;
		}
		else if(this.numOfAnswerD==14)
		{
			if(answerData>=3.0*scailingFactor)rightIndex=13;
			else if(answerData<3.0*scailingFactor && answerData>=2.5*scailingFactor)rightIndex=12;
			else if(answerData<2.5*scailingFactor && answerData>=2.0*scailingFactor)rightIndex=11;
			else if(answerData<2.0*scailingFactor && answerData>=1.5*scailingFactor)rightIndex=10;
			else if(answerData<1.5*scailingFactor && answerData>=1.0*scailingFactor)rightIndex=9;
			else if(answerData<1.0*scailingFactor && answerData>=0.5*scailingFactor)rightIndex=8;
			else if(answerData<0.5*scailingFactor && answerData>=0.0*scailingFactor)rightIndex=7;
			else if(answerData<0.0*scailingFactor && answerData>=-0.5*scailingFactor)rightIndex=6;
			else if(answerData<-0.5*scailingFactor && answerData>=-1.0*scailingFactor)rightIndex=5;
			else if(answerData<-1.0*scailingFactor && answerData>=-1.5*scailingFactor)rightIndex=4;
			else if(answerData<-1.5*scailingFactor && answerData>=-2.0*scailingFactor)rightIndex=3;
			else if(answerData<-2.0*scailingFactor && answerData>=-2.5*scailingFactor)rightIndex=2;
			else if(answerData<-2.5*scailingFactor && answerData>=-3.0*scailingFactor)rightIndex=1;
			else if(answerData<-3.0*scailingFactor)rightIndex=0;
		}
		else
		{
			throw new RuntimeException("invalid num of answers :"+this.numOfAnswerD);
		}
		for(int i=0;i<ret.length;i++)
		{
			if(i==rightIndex)ret[i]=1.0;
			else ret[i]=0.0;
		}
		
		for(int i=0;i<ret.length;i++)
		{
			if(Double.isNaN(ret[i]))throw new RuntimeException("invalid data in answers :"+i);
		}
		return ret;
	}
	
	
	@Override
	public Matrix getAnswerData(int idStockInfo,int matrixType) {
		// TODO Auto-generated method stub
		
		Stock target = null;
		for(Stock s:this.stocks)
		{
			if(s.idStockInfo==idStockInfo)target=s;
		}
		if(target==null)return null;

		Matrix m = null;
		if(this.lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			int sizeOfRight=numOfAnswerD;
			m = Matrix.create(target.fourWeeks.size(), sizeOfRight, matrixType);
			double[] right = new double[sizeOfRight];
			int count=0;
			for(FourWeek fw:target.fourWeeks)
			{
				ceeAnswer(right, fw.answerData);
				m.setData(right,count);
				count++;
			}
		}
		else if(this.lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
		{
			m = Matrix.create(target.fourWeeks.size(), 1, matrixType);
			double[] right = new double[1];
			int count=0;
			for(FourWeek fw:target.fourWeeks)
			{
				right[0] = fw.answerData;				
				m.setData(right,count);
				count++;
			}
		}
		
		return m;
	}
	@Override
	public StockInfo getStockInfoInAnswerData(int idStockInfo,int index) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Matrix getInputData(int idStockInfo, int matrixType) {
		// TODO Auto-generated method stub
		
		Stock target = null;
		for(Stock s:this.stocks)
		{
			if(s.idStockInfo==idStockInfo)target=s;
		}
		if(target==null)return null;
		FourWeek fwTmp = target.fourWeeks.get(0);
		int dataLen = fwTmp.data.length;
		Matrix m = Matrix.create(target.fourWeeks.size(), dataLen, matrixType);
		int count=0;
		for(FourWeek fw:target.fourWeeks)
		{
			m.setData(fw.data, count);
			count++;
		}
		return m;
	}

	
	
	
	private DZConfig getDefaultParameter() throws SQLException
	{
		DZConfig params = new DZConfig();
		params.put("num_of_layer", new Integer(3));
		params.put("updator", "adam"); //normal or adam
		params.putBoolean("use_last_mu_and_var", true);
		params.putBoolean("batch_norm", true);
		
		params.putInt("training_iteration", 2000);
		params.putInt("training_sample", 100);

		params.put("W_width_of_0layer", new Integer(500));
		params.put("W_width_of_1layer", new Integer(100));
		params.put("W_width_of_2layer", new Integer(20));
		
		params.putString("activation", "relu");
		
		HeteroLayerConfig config = new HeteroLayerConfig();
		HeteroLayerConfigElement e =  config.new HeteroLayerConfigElement();
		e.startIndex=0;
		e.endIndex=NumOfDailyStockForEachStock-1;     //including
		if(bypassMainStockData)e.baypass=true;
		else
		{
			e.baypass = false;
			e.numOfFilters = 14;
		}
		config.configElements.add(e);
		int endIndex = e.endIndex;
		
		//for Seasonality
		if(includeSeasonality)
		{
			e = config.new HeteroLayerConfigElement();
			e.startIndex=endIndex+1;
			e.endIndex=e.startIndex+12 -1;
			e.numOfFilters=8;
			config.configElements.add(e);
			endIndex = e.endIndex;		
		}
		
		LinkedList<AdditionalData> adData = this.getAdditionalData(null);
		for(AdditionalData ad:adData)
		{
			e =  config.new HeteroLayerConfigElement();
			e.startIndex=endIndex+1;
			e.endIndex=e.startIndex+ad.getNumOfDataInput()-1;
			e.numOfFilters=8;
			if(ad.getName().equals("economic_growth"))
			{
				e.baypass=true;
				e.numOfFilters=0;
			}
			config.configElements.add(e);
			endIndex = e.endIndex;			
		}
		params.setHeteroLayerConfig(config);
		params.put("W_width_of_0layer", new Integer(config.getTotalOutputWidth()));
		//params.put("W_width_of_0layer", new Integer(300));

		if(lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			params.put("learning_rate", new Double(0.01));
			params.put("learning_rate_beta1", new Double(0.9)); //for adam
			params.put("learning_rate_beta2", new Double(0.999)); //for adam
		}
		else if(lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
		{
			params.put("learning_rate", new Double(0.01));
			params.put("learning_rate_beta1", new Double(0.9)); //for adam
			params.put("learning_rate_beta2", new Double(0.999)); //for adam
		}
		else
		{
			throw new RuntimeException("invalid last layer type");
		}
		
		params.put("last_layer_type", new Integer(lastLayerType));
		
		return params;
	}
	
	@Override
	public DZConfig getParameter(int idStockInfo,int index) {
		// TODO Auto-generated method stub
		
		DZConfig defaultConfig = null;
		try {
			defaultConfig = getDefaultParameter();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if(idStockInfo==113) //한양증권
		{
			defaultConfig.putInt("training_iteration", 2000);
			defaultConfig.putInt("training_sample", 100);
		}
		else if(idStockInfo==283 || idStockInfo==240 || idStockInfo==525) //현대자동차(283), 농심(240) //SK텔레콤(525), 
		{
			//defaultConfig.putInt("training_iteration", 500);
			defaultConfig.putInt("training_iteration", 3000);
			defaultConfig.putInt("training_sample", 50);	
			
			defaultConfig.put("num_of_layer", new Integer(2));
			
			defaultConfig.put("W_width_of_1layer", new Integer(100));
			defaultConfig.put("W_width_of_2layer", new Integer(30));
			defaultConfig.put("W_width_of_3layer", new Integer(20));
			defaultConfig.put("W_width_of_4layer", new Integer(20));
		}
		
		defaultConfig.configIndex = index;
		return defaultConfig;

	}
	
	@Override
	public int getNumOfParameter()
	{
		return 1;
	}

	@Override
	public int getDataType() {
		// TODO Auto-generated method stub
		return Data.DATA_TYPE_HETERODATA;
	}
	@Override
	public int[] getIds() {
		// TODO Auto-generated method stub
		return this.ids;
	}
	@Override
	public int getLastLayerType() {
		// TODO Auto-generated method stub
		return this.lastLayerType;
	}
	
	public Stock getStock(int id)
	{
		for(Stock s:this.stocks)
		{
			if(s.idStockInfo==id)return s;
		}
		return null;
	}
	
	
	HDataResult res = new HDataResult();
	@Override
	public WorkerInferenceResult getResultHolder() {
		// TODO Auto-generated method stub
		res.parent = this;
		return res;
	}
	
	private static char delimiter = '^'; 
	
	@Override
	public void saveToFile(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
		JsonObject json = new JsonObject();
		json.addProperty("startDate", this.startDate);
		json.addProperty("endDate", this.endDate);
		json.addProperty("lastLayerType", this.lastLayerType);
		JsonArray ids = new JsonArray();
		for(int i=0;i<this.ids.length;i++)
		{
			ids.add(this.ids[i]);
		}
		json.add("ids", ids);
		
		Gson gson = new Gson();
		String s = gson.toJson(json);
		writer.write(s);
		writer.write(delimiter);
		writer.flush();
		
		int numOfStock = this.stocks.size();
		int count =0;
		for(Stock stock:this.stocks)
		{
			System.out.printf("[%d/%d] json loading %d\n",count,numOfStock,stock.idStockInfo);

			writer.write(gson.toJson(stock.getJson()));
			writer.write(delimiter);			
			writer.flush();
			count++;
		}
	}
	
	private String getNextString(Reader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		while(true)
		{
			int r = reader.read();
			char c = (char) r;
			if(c==delimiter)
			{
				return sb.toString();
			}
			if(r<0)
			{
				//System.out.println("end of file");
				break;
			}
			sb.append(c);
		}
		return null;
	}
	
	@Override
	public void loadFromFile(Reader reader) throws IOException {
		// TODO Auto-generated method stub
		//reader.
	//	reader.
		
		String s = getNextString(reader);
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(s).getAsJsonObject();
		
		this.startDate = json.get("startDate").getAsString();
		this.endDate = json.get("endDate").getAsString();
		this.lastLayerType = json.get("lastLayerType").getAsInt();
		JsonArray ids = json.get("ids").getAsJsonArray();
		this.ids = new int[ids.size()];
		int count=0;
		for(JsonElement e:ids)
		{
			this.ids[count] = e.getAsInt();
			count++;
		}
		
		count=0;
		while(true)
		{
			System.out.println("loading stock from file "+count);
			s = getNextString(reader);
			if(s==null)break;
			json = parser.parse(s).getAsJsonObject();
			this.stocks.add(Stock.getFromJson(json,this));
			count++;
		}
	}
}
