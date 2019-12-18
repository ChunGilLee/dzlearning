package AddData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dzlearning.SimpleData;

public class DailyDataAdditionalData extends AdditionalData
{		
	public DailyDataAdditionalData(int numOfDataInput, Connection conn, String name, boolean isDouble) throws SQLException
	{
		this.name = name;
		this.numOfDataInput = numOfDataInput;
		PreparedStatement ps = conn.prepareStatement("select id_daily_data,date,double_data,long_data from manualdb.DailyData where name=? order by date desc limit ?");
		ps.setString(1, name);
		ps.setInt(2, numOfDataInput);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String date = rs.getString("date");
			double rate = rs.getDouble("double_data");
			long rateL = rs.getLong("long_data");
			Object r = null;
			if(isDouble)r = rs.getDouble("double_data");
			else r = rs.getLong("long_data");
			int id = rs.getInt("id_daily_data");
			if(isDouble && (r==null || Double.isNaN(rate)))
			{
				throw new RuntimeException("Daily data is not valid at "+id);
			}
			else if(isDouble==false && r==null)
			{
				throw new RuntimeException("Daily data is not valid at "+id);				
			}
			SimpleData sd = new SimpleData();
			sd.date = date;
			sd.rate =  rate;
			if(isDouble==false)sd.rateL = rateL;
			this.data.add(sd);
		}
	}
}