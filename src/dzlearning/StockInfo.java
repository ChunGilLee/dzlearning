package dzlearning;

import java.sql.ResultSet;
import java.sql.SQLException;


public class StockInfo
{
	public int idStockInfo;
	public String name;
	public String code;
	
	public static StockInfo parse(ResultSet rs) throws SQLException
	{
		StockInfo sinfo = new StockInfo();
		sinfo.idStockInfo = rs.getInt("id_stock_info");
		sinfo.code = rs.getString("code");
		sinfo.name = rs.getString("name");
		return sinfo;
	}
}