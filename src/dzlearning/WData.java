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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.matrix.Matrix;

public class WData implements Data {

	String startDate;  //추측하고자 하는 날짜 기준.
	String endDate;
	LinkedList<Stock> stocks = new LinkedList<>();
	int ids[] = null;
	int lastLayerType = LayerType.LAST_LAYER_TYPE_MSE;
	
	public JsonObject getJson()
	{
		JsonArray stocks = new JsonArray();
		for(Stock stock:this.stocks)
		{
			stocks.add(stock.getJson());
		}
		JsonObject json = new JsonObject();
		json.add("stocks", stocks);
		json.addProperty("startDate", this.startDate);
		json.addProperty("endDate", this.endDate);
		json.addProperty("lastLayerType", this.lastLayerType);
		return json;
	}
	public void getFromJson(JsonObject json) throws Exception
	{
		JsonArray stocks = json.get("stocks").getAsJsonArray();
		for(JsonElement e:stocks)
		{
			JsonObject stock = e.getAsJsonObject();
			this.stocks.add(Stock.getFromJson(stock));
		}
		this.startDate = json.get("startDate").getAsString();
		this.endDate = json.get("endDate").getAsString();	
		this.lastLayerType = json.get("lastLayerType").getAsInt();
	}
			
	public static WData getFromDB(String startDay, String endDay, int ids[]) throws Exception
	{
		WData wdata =new WData();
		
		wdata.startDate = WData.getSundayOfDay(startDay);
		wdata.endDate = WData.getSaturdayOfDay(endDay);
		//wdata.startDate = "2007-01-01";
		//wdata.endDate = "2018-05-31";
		
		Connection conn = DB.getConn();
		System.out.print("loading ids...");
		if(ids==null)ids = wdata.getInputDataTargetCompany();
		wdata.ids = ids;
		String idsS = DZHelper.arrayToCommaSeperatedString(ids);
		System.out.println("OK");
		System.out.print("loading data from DB...");
		PreparedStatement ps = conn.prepareStatement("select * from DailyStock where id_stock_info in ("+idsS+") and date between ? and ? order by id_stock_info,date");
		
		Calendar tmp = WData.getCalendarFromString(wdata.startDate);
		tmp.add(Calendar.DAY_OF_YEAR, -7);
		ps.setString(1, WData.getDateString(tmp));
		ps.setString(2, wdata.endDate);
		ResultSet rs = ps.executeQuery();
		Stock currentStock = null;
		Week currentWeek = null;
		while(rs.next())
		{
			int idStockInfo = rs.getInt("id_stock_info");
			if(currentStock == null || currentStock.idStockInfo!=idStockInfo)
			{
				if(currentStock!=null)
				{
					if(currentWeek!=null)
					{
						currentStock.weeks.add(currentWeek);
						currentWeek = null;
					}
					wdata.stocks.add(currentStock);
				}
				currentStock = new Stock(idStockInfo);
				System.out.println("current stock id:"+currentStock.idStockInfo);
			}
			
			String date = rs.getString("date");
			if(currentWeek == null || currentWeek.isDateInThisWeek(date)==false)
			{
				if(currentWeek!=null)
				{
					currentStock.weeks.add(currentWeek);
				}
				currentWeek = new Week(idStockInfo, date);
			}
			
			double dungrakRate = rs.getDouble("dungrak_rate");
			currentWeek.putRate(dungrakRate);
		}
		
		if(currentStock!=null)
		{
			if(currentWeek!=null)currentStock.weeks.add(currentWeek);
			wdata.stocks.add(currentStock);
		}
		
		conn.close();
		System.out.println("OK");
		
		System.out.print("check validation...");
		wdata.checkNumOfValidData();
		System.out.println("OK");
		
		System.out.print("clearing dummy...");
		wdata.setDummyToZero();
		System.out.println("OK");
		
		System.out.println("Num of stocks:"+wdata.stocks.size());
		if(wdata.stocks.size()>0)System.out.println("Num of weeks:"+wdata.stocks.get(0).weeks.size());
		
		/*
		wdata.saveToFile("wdata.json");
		WData wdata2 = WData.loadFromFile("wdata.json");
		if(wdata.isEqualTo(wdata2))
		{
			System.out.println("save ok");
		}
		else
		{
			System.out.println("save failed");
		}
		*/
		
		return wdata;
	}
	
	public boolean isEqualTo(WData wdata)
	{
		if(wdata.startDate.equals(this.startDate)==false)return false;
		if(wdata.endDate.equals(this.endDate)==false)return false;
		Iterator<Stock> compareTo = wdata.stocks.iterator();
		for(Stock stock:stocks)
		{
			Stock comparing = compareTo.next();
			if(stock.isEqualTo(comparing)==false)return false;
		}
		return true;
	}
	
	public void setDummyToZero()
	{
		for(Stock stock:this.stocks)
		{
			stock.setDummyToZero();
		}
	}
	
	//check valid rate number in every stocks
	public void checkNumOfValidData()
	{
		int numOfWeek=-1;
		for(Stock stock:this.stocks)
		{
			if(numOfWeek<0)numOfWeek = stock.weeks.size();
			else
			{
				if(numOfWeek != stock.weeks.size())
				{
					throw new RuntimeException("idStockInfo:"+stock.idStockInfo+" not matched number of week("+numOfWeek+")");
				}
			}
		}
		
		if(stocks.size()>0)
		{
			Stock reference = stocks.get(0);
			for(Week week:reference.weeks)
			{
				//System.out.println("reference week:"+week.startD+" num:"+week.getNumOfValidRate());
				for(Stock stock:this.stocks)
				{
					Week test = stock.getWeek(week.startD);
					if(week.getNumOfValidRate()!=test.getNumOfValidRate())
					{
						throw new RuntimeException(
								String.format("Number of valid rate at not matched date:%s id:%d num:%d <=> id:%d num:%d\n",
										week.startD,reference.idStockInfo,week.getNumOfValidRate(),
										stock.idStockInfo,test.getNumOfValidRate()));
					}
					if(week.startD.equals(test.startD)==false || week.endD.equals(test.endD)==false)
					{
						throw new RuntimeException(
								String.format("StartDate or endDate at matched id:%d S:%s E:%s <=> id:%d S:%s E:%s\n",
										reference.idStockInfo,week.startD,week.endD,
										stock.idStockInfo,test.startD,test.endD));
					}
				}
			}
		}
		
	}
	
	private static class Stock
	{
		int idStockInfo;
		LinkedList<Week> weeks = new LinkedList<Week>();
		
		public boolean isEqualTo(Stock stock)
		{
			if(idStockInfo!=stock.idStockInfo)return false;
			Iterator<Week> compareTo = stock.weeks.iterator();
			for(Week week:weeks)
			{
				Week compareToWeek = compareTo.next();
				if(week.isEqualTo(compareToWeek)==false)return false;
			}
			return true;
		}
		
		public JsonObject getJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("idStockInfo", idStockInfo);
			JsonArray weeksJson = new JsonArray();
			for(Week week:weeks)
			{
				weeksJson.add(week.getJson());
			}
			json.add("weeks", weeksJson);
			return json;
		}
		public static Stock getFromJson(JsonObject json) throws ParseException
		{
			Stock stock = new Stock(json.get("idStockInfo").getAsInt());
			JsonArray weeksJson = json.get("weeks").getAsJsonArray();
			for(int i=0;i<weeksJson.size();i++)
			{
				stock.weeks.add(Week.getFromJson(weeksJson.get(i).getAsJsonObject()));
			}
			return stock;
		}
		
		public Stock(int idStockInfo)
		{
			this.idStockInfo = idStockInfo;
		}
		
		public Week getWeek(String date)
		{
			for(Week week:weeks)
			{
				if(week.isDateInThisWeek(date))return week;
			}
			return null;
		}
		
		public void setDummyToZero()
		{
			for(Week week:weeks)
			{
				week.setDummyToZero();
			}	
		}
	}
	
	private static class Week
	{
		public static final int rateArraySize=5;
		public String startD;
		public String endD;
		public double rate[] = new double[rateArraySize];
		public int idStockInfo=0;
		public int numOfValid=-1; //it will be set when clearing dummy
		
		public boolean isEqualTo(Week week)
		{
			if(startD.equals(week.startD)==false)return false;
			if(endD.equals(week.endD)==false)return false;
			if(idStockInfo!=week.idStockInfo)return false;
			if(numOfValid!=week.numOfValid)return false;
			for(int i=0;i<rate.length;i++)
			{
				if(rate[i]!=week.rate[i])return false;
			}
			return true;
		}
		
		public JsonObject getJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("startD", this.startD);
			json.addProperty("endD", this.endD);
			JsonArray rates = new JsonArray();
			for(int i=0;i<rate.length;i++)
			{
				rates.add(rate[i]);
			}
			json.add("rate", rates);
			json.addProperty("idStockInfo", this.idStockInfo);
			json.addProperty("numOfValid", numOfValid);
			return json;
		}
		public static Week getFromJson(JsonObject json) throws ParseException
		{
			Week week = new Week();
			week.idStockInfo = json.get("idStockInfo").getAsInt();
			week.startD = json.get("startD").getAsString();
			week.endD = json.get("endD").getAsString();
			JsonArray rates = json.get("rate").getAsJsonArray();
			for(int i=0;i<rates.size();i++)
			{
				week.rate[i] = rates.get(i).getAsDouble();
			}
			week.numOfValid = json.get("numOfValid").getAsInt();
			return week;
		}
		
		public Week()
		{}
		
		public Week(int idStockInfo, String date) throws ParseException
		{
			this.idStockInfo = idStockInfo;
			this.setDate(date);
			for(int i=0;i<rate.length;i++)
			{
				rate[i] = Double.NaN;
			}
		}
		
		public boolean isDateInThisWeek(String date)
		{
			if(date.compareTo(this.startD)>=0 && date.compareTo(this.endD)<=0)return true;
			return false;
		}
		
		private void setDate(String dateString) throws ParseException
		{
			this.startD = WData.getSundayOfDay(dateString);
			this.endD = WData.getSaturdayOfDay(dateString);
		}
		
		public void putRate(double v)
		{
			for(int i=0;i<rate.length;i++)
			{
				if(Double.isNaN(rate[i]))
				{
					rate[i] = v;
					break;
				}
			}
		}
		
		public int getNumOfValidRate()
		{
			if(numOfValid>=0)return numOfValid;
			int count=0;
			for(int i=0;i<rate.length;i++)
			{
				if(Double.isNaN(rate[i])==false)count++;
			}
			return count;
		}
		
		public void setDummyToZero()
		{
			int count=0;
			for(int i=0;i<rate.length;i++)
			{
				if(Double.isNaN(rate[i]))
				{
					rate[i] = 0;
				}
				else count++;
			}			
			this.numOfValid = count;
		}
		
		public double getRealRate()
		{
			double out = 0.0;
			for(int i=0;i<rate.length;i++)
			{
				if(i==0)out = rate[i];
				else
				{
					out = rate[i]*out/100.0 + rate[i] + out;
				}
			}
			return out;
		}
		public double getMappedRate()
		{
			return rateMapped(this.getRealRate());
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
	}
	
	public static String getDateString(Calendar cal)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
		
	}
	public static Calendar getCalendarFromString(String date) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(date));
		return cal;
	}
	public static String getSundayOfDay(String date) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = getCalendarFromString(date);
		cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
		return sdf.format(cal.getTime());
	}
	public static String getSaturdayOfDay(String date) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = getCalendarFromString(date);
		cal.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
		return sdf.format(cal.getTime());
	}
	
	@Override
	public Matrix getInputData(int matrixType, int idStockInfo) {
		// TODO Auto-generated method stub
		
		/*
		 * 구현 필요.
		 * 
		Stock referenceStock = this.stocks.get(0);
		Calendar startD = WData.getCalendarFromString(this.startDate);
		Calendar endD = WData.getCalendarFromString(this.endDate);
		endD.add(Calendar.DAY_OF_YEAR, -(7*4));
		LinkedList<Week> sameWeek = new LinkedList<>();
		LinkedList<double[]> data = new LinkedList<>();
		while(true)
		{
			sameWeek.clear();
			for(Stock stock:stocks)
			{
				Week week = stock.getWeek(WData.getDateString(startD));
				sameWeek.add(week);
			}
			
			double[] eachData = new double[sameWeek.size() * Week.rateArraySize];
			int index=0;
			for(Week week:sameWeek)
			{
				for(int i=0;i<Week.rateArraySize;i++)
				{
					eachData[index+i] = week.ra
				}
				index += Week.rateArraySize;
			}
			startD.add(Calendar.DAY_OF_YEAR, 7);
			if(startD.after(endD))break;
		}
		DoubleBuffer doubleBuffer = DoubleBuffer.
		Matrix input = Matrix.create(referenceStock., column, matrixType);
		*/
		return null;
	}
	@Override
	public Matrix getAnswerData(int matrixType, int idStockInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public StockInfo getStockInfoInAnswerData(int index, int idStockInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DZConfig getParameter(int idStockInfo,int index) {
		// TODO Auto-generated method stub

		DZConfig params = new DZConfig();
		params.put("num_of_layer", new Integer(2));
		params.put("updator", "adam"); //normal or adam
		
		if(lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			params.put("learning_rate", new Double(0.001));
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
		params.put("W_width_of_0layer", new Integer(500));
		params.put("W_width_of_1layer", new Integer(100));
		params.put("W_width_of_2layer", new Integer(50));
		
		params.put("last_layer_type", new Integer(lastLayerType));
		
		return params;
	}
	@Override
	public int getNumOfParameter()
	{
		return 1;
	}
	
	
	@Override
	public int getDataType() {
		// TODO Auto-generated method stub
		return Data.DATA_TYPE_WEEKDATA;
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
	
	private class InferenceResult implements WorkerInferenceResult
	{

		@Override
		public void setInferenceResult(int idStockInfo, double[][] result, double trainingCorrectRate, Exception error, DZConfig config, String paramPrefix) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void printResult() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTrainingResult(int idStockInfo, double trainingCorrectRate, Exception error, DZConfig config, String paramPrefix) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getTrainingCorrectRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getInferenceCorrectRate() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}

	@Override
	public WorkerInferenceResult getResultHolder() {
		// TODO Auto-generated method stub
		return new InferenceResult();
	}
	@Override
	public void saveToFile(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void loadFromFile(Reader reader) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
