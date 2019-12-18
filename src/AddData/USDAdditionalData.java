package AddData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dzlearning.SimpleData;

public class USDAdditionalData extends AdditionalData
{		
	public USDAdditionalData(int numOfDataInput, Connection conn) throws SQLException
	{
		this.name = "USD";
		this.numOfDataInput = numOfDataInput;
		PreparedStatement ps = conn.prepareStatement("select id_currency,date,usd_rate from Currency order by date desc");
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String date = rs.getString("date");
			double rate = rs.getDouble("usd_rate");
			Object r = rs.getDouble("usd_rate");
			int id = rs.getInt("id_currency");
			if(r==null || Double.isNaN(rate))
			{
				throw new RuntimeException("Currency data is not valid at "+id);
			}
			SimpleData sd = new SimpleData();
			sd.date = date;
			sd.rate =  rate;
			this.data.add(sd);
		}
	}
}