package dzlearning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DailyInfo
{
	long idDailyStock;
	int idStockInfo;
	long endPrice;
	long dungrak;
	double dungrakRate;
	String date;
	
	public static DailyInfo getFromResultSet(ResultSet rs) throws SQLException
	{
		DailyInfo dailyInfo = new DailyInfo();
		dailyInfo.idDailyStock = rs.getLong("id_daily_stock");
		dailyInfo.idStockInfo = rs.getInt("id_stock_info");
		dailyInfo.endPrice = rs.getLong("end_price");
		dailyInfo.dungrak = rs.getLong("dungrak");
		dailyInfo.date = rs.getString("date");
		dailyInfo.dungrakRate = rs.getDouble("dungrak_rate");
		return dailyInfo;
	}
	public double getInputData()
	{

		if(dungrakRate>35.0 || dungrakRate<-35.0)
		{
			if((this.idStockInfo==651 && this.date.equals("2009-04-20")) || //재상장
					(this.idStockInfo==547 && this.date.equals("2009-08-05")) || //일진디스플, 유상증자
					(this.idStockInfo==291 && this.date.equals("2007-06-27")) //신한, 유상 소각
					)
			{
				//재상장으로 인한 변경으로 보임. 문제 없어보임.
			}
			else
			{
				throw new RuntimeException("rate is wrong id_stock_info:"+this.idStockInfo+", date:"+this.date+" r:"+dungrakRate);				
			}
		}
		
		if(dungrak!=0 && dungrakRate==0)
		{
			throw new RuntimeException("dungrak rate=0 but dungrak="+this.dungrak+" id_stock_info:"+this.idStockInfo+ ", date:"+this.date);
		}
		
		//System.out.printf("endprice %f dungrak %f rate %f\n",(double)(endPrice),(double)dungrak,rate);
		return dungrakRate;
	}
}