package AddData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import dzlearning.SimpleData;

public abstract class AdditionalData {

	protected LinkedList<SimpleData> data = new LinkedList<SimpleData>();
	protected int numOfDataInput=0;
	protected String name=null;
	
	public LinkedList<SimpleData> getData()
	{
		return data;
	}
	
	public int getNumOfDataInput()
	{
		return this.numOfDataInput;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	
	private static HashMap<String, Object> singleton = new HashMap<>();
	public static AdditionalData getData(int data, int numOfDataInput, Connection conn) throws SQLException
	{
		synchronized (singleton) {
			Object o = singleton.get(""+data);
			if(o!=null)return (AdditionalData)o;			
			
			AdditionalData ad = getDataInternal(data, numOfDataInput, conn);
			singleton.put(""+data, ad);
			return ad;
		}
	}
	
	
	public static final int KOSPI=1;
	public static final int CURRENCY_USD=2;
	public static final int ECONOMIC_GROWTH=3;
	public static final int CALL_RATE=4;
	private static AdditionalData getDataInternal(int data, int numOfDataInput, Connection conn) throws SQLException
	{
		if(data==KOSPI)
		{
			return new KospiAdditionalData(numOfDataInput, conn);
		}
		else if(data==CURRENCY_USD)
		{
			return new USDAdditionalData(numOfDataInput, conn);
		}
		else if(data==ECONOMIC_GROWTH)
		{
			return new MonthlyDataAdditionalData(numOfDataInput, conn, "economic_growth", true);
		}
		else if(data==CALL_RATE)
		{
			return new DailyDataAdditionalData(numOfDataInput, conn, "call_rate", true);
		}
		
		return null;
	}
}
