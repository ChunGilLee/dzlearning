package AddData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dzlearning.SimpleData;

public class KospiAdditionalData extends AdditionalData
{
	
	public KospiAdditionalData(int numOfDataInput, Connection conn) throws SQLException
	{
		this.name = "KOSPI";
		this.numOfDataInput = numOfDataInput;
		PreparedStatement ps = conn.prepareStatement("select date,kospi, kospi_dungrak from DailyTotalStock order by date desc");
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String date = rs.getString("date");
			double kospiV = rs.getDouble("kospi");
			double dungrak = rs.getDouble("kospi_dungrak");
			double kospiPrevious = kospiV - dungrak;
			double rate = dungrak/kospiPrevious * 100.0;
			SimpleData sd = new SimpleData();
			sd.date = date;
			sd.rate =  rate;
			this.data.add(sd);
		}
	}
}