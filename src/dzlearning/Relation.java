package dzlearning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Relation {

	private static HashMap<String, Object> CodeAndId = null;
	
	private synchronized static HashMap<String, Object> getCodeAndId() throws SQLException
	{
		if(CodeAndId!=null)return CodeAndId;
		Connection conn = DB.getConn();
		PreparedStatement ps = conn.prepareStatement("select code,name,id_stock_info from StockInfo");
		ResultSet rs = ps.executeQuery();
		CodeAndId = new HashMap<>();
		while(rs.next())
		{
			CodeAndId.put(rs.getString("code")+"_id", new Integer(rs.getInt("id_stock_info")));
			CodeAndId.put(rs.getString("code")+"_name", rs.getString("name"));
		}
		rs.close();
		ps.close();
		return CodeAndId;
	}
	private int getIdFromCode(String code) throws SQLException
	{
		HashMap<String, Object> map = getCodeAndId();
		Integer i = (Integer)map.get(code+"_id");
		return i.intValue();
	}
	
	public int[] getRelationStock(int idStockInfo) throws Exception
	{
		
		if(idStockInfo==688)
		{ //농심홀딩스
			int[] ret = new int[4];
			ret[0] = getIdFromCode("004990"); //롯데지주
			ret[1] = getIdFromCode("097950"); //CJ제일제당
			ret[2] = getIdFromCode("271560"); //오리온
			ret[3] = getIdFromCode("007310"); //오뚜기
			return ret;
		}
		
		return new int[0];
	}
}
