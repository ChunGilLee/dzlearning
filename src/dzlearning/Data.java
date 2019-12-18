package dzlearning;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dolziplib.DZConfig;
import dolziplib.matrix.Matrix;

public interface Data {

	public static final int DATA_TYPE_WEEKDATA = 0;
	public static final int DATA_TYPE_DAYDATA = 1;
	public static final int DATA_TYPE_HETERODATA = 2;

	//startDay, endDay, 추측하고자 하는 날짜 기준.
	public static Data getFromDB(int dataType,String startDay, String endDay, int ids[]) throws Exception
	{
		if(dataType==DATA_TYPE_WEEKDATA)
		{
			return WData.getFromDB(startDay,endDay, ids);
		}
		else if(dataType == DATA_TYPE_DAYDATA)
		{
			return DData.getFromDB(startDay,endDay, ids);
		}
		else if(dataType == DATA_TYPE_HETERODATA)
		{
			return HData.getFromDB(startDay,endDay, ids);
		}
		throw new RuntimeException("Invalid data type");
	}
	
	//startDay, endDay, 추측하고자 하는 날짜 기준.
	public static Data getFromDB(int dataType,String startDay, String endDay) throws Exception
	{
		if(dataType==DATA_TYPE_WEEKDATA)
		{
			return WData.getFromDB(startDay,endDay, null);
		}
		else if(dataType == DATA_TYPE_DAYDATA)
		{
			return DData.getFromDB(startDay,endDay, null);
		}
		else if(dataType == DATA_TYPE_HETERODATA)
		{
			return HData.getFromDB(startDay,endDay, null);
		}
		throw new RuntimeException("Invalid data type");		
	}
	
	public int[] getIds();
	
	public DZConfig getParameter(int idStockInfo, int index);
	public int getNumOfParameter();
	
	public Matrix getInputData(int idStockInfo,int matrixType);
	public Matrix getAnswerData(int idStockInfo,int matrixType);
	
	public StockInfo getStockInfoInAnswerData(int idStockInfo,int index);
	public int getLastLayerType();
	
	public static StockInfo getStockInfoById(int idStockInfo) throws SQLException
	{
		Connection conn = DB.getConn();
		StockInfo sinfo  = null;
		try {
			PreparedStatement ps = conn.prepareStatement("select * from StockInfo where id_stock_info=?");
			ps.setInt(1, idStockInfo);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				sinfo = StockInfo.parse(rs);
			}
			rs.close();
			ps.close();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		conn.close();
		return sinfo;
	}
	
	//우선 2017년 1월 2일에 존재하고 2018년 6월 28에 존재하는 회사만을 기준으로 함.
	public default int[] getInputDataTargetCompany() throws SQLException
	{
		Connection conn = DB.getConn();
		LinkedList<Integer> id2007 = new LinkedList<>();
		PreparedStatement ps = conn.prepareStatement("select id_stock_info from DailyStock where date='2007-01-02' order by id_stock_info");
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			id2007.add(new Integer(rs.getInt("id_stock_info")));
		}
		rs.close();
		ps.close();
		
		LinkedList<Integer> id2018 = new LinkedList<>();
		ps = conn.prepareStatement("select id_stock_info from DailyStock where date='2018-06-28' order by id_stock_info");
		rs = ps.executeQuery();
		while(rs.next())
		{
			id2018.add(new Integer(rs.getInt("id_stock_info")));
		}
		rs.close();
		ps.close();
		conn.close();
		
		LinkedList<Integer> ret = new LinkedList<>();
		for(Integer i0:id2007)
		{
			for(Integer i1:id2018)
			{
				if(i0.intValue() == i1.intValue())
				{
					ret.add(i0);
					break;
				}
			}
		}
		
		int[] r = new int[ret.size()];
		int count=0;
		for(Integer i:ret)
		{
			r[count] = i.intValue();
			count++;
		}
		return r;
	}
	
	public JsonObject getJson();
	public void getFromJson(JsonObject json) throws Exception;
	public int getDataType();
	
	public default void saveToFile(String path) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write(this.getDataType());
		/*
		Gson gson = new Gson();
		JsonObject json = this.getJson();
		json.addProperty("data_type_of_data_interface", this.getDataType());
		String jsonString = gson.toJson(json);
		*/
		this.saveToFile(bw);
		bw.flush();
		bw.close();
	}
	
	public default void printDebug(int idStockInfo){}
	
	public void saveToFile(Writer writer) throws IOException;
	public void loadFromFile(Reader reader) throws IOException;
	
	public static Data loadFromFile(String path) throws Exception
	{
		FileReader fr = new FileReader(path);
		/*
		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse(fr);
		JsonObject json = e.getAsJsonObject();
		*/
		Data data = null;
		int dataType = fr.read();
		if(dataType==DATA_TYPE_WEEKDATA)
		{
			data = new WData();
			data.loadFromFile(fr);
			//data.getFromJson(json);
		}
		else if(dataType==DATA_TYPE_DAYDATA)
		{
			data =new DData();
			data.loadFromFile(fr);
			//data.getFromJson(json);
		}
		else if(dataType==DATA_TYPE_HETERODATA)
		{
			data= new HData();
			data.loadFromFile(fr);
		}
		fr.close();
		return data;
	}
	
	public WorkerInferenceResult getResultHolder();
	
}
