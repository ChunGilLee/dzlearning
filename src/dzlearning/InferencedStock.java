package dzlearning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import dolziplib.matrix.Matrix;
import dzlearning.StockInfo;

public class InferencedStock {
	StockInfo stockInfo;
	double trainingCorrectRate;
	String targetStartDay; //alwasys aligned to sunday of the week. yyyy-MM-dd
	String targetEndDay; //always aligned to saturday of the week.
	double realDungrakRate = Double.NaN;
	
	public Matrix output = null;
	
	public static final int RESULT_UNKNOWN=0;
	public static final int RESULT_UNDER3PERCENT=1;
	public static final int RESULT_OVER3PERCENT=2;
	public int inferenceResult = RESULT_UNKNOWN;
	public double inferenceResultRate = Double.NaN; //valid only if Worker.rightAnswerType==RIGHT_ANSWER_TYPE_RATE
	public int realResult = RESULT_UNKNOWN;
	
	public String getStringCode(int resultId)
	{
		if(resultId==RESULT_UNDER3PERCENT)return "UNDR3";
		else if(resultId==RESULT_OVER3PERCENT)return "OVER3";
		return "UNKN";
	}
	
	public String toString()
	{
		String ret =String.format("%s(%d,code:%s) %s-%s trainingCR:%f realRate:%f inf:%s real:%s %s", stockInfo.name,stockInfo.idStockInfo,
				stockInfo.code,targetStartDay,targetEndDay,trainingCorrectRate,this.realDungrakRate,getStringCode(this.inferenceResult),
				getStringCode(this.realResult), this.isCorrenct() ? "CORRECT":"WRONG");
		return ret;
	}
	
	public InferencedStock(StockInfo stockInfo)
	{
		this.stockInfo = stockInfo;
	}
	
	public boolean isCorrenct()
	{
		if(this.realResult==RESULT_UNKNOWN)return false;
		if(this.realResult==this.inferenceResult)return true;
		return false;
	}
	
	public void getRealResult() throws Exception
	{
		Calendar endDay = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		endDay.setTime(sdf.parse(targetEndDay));
	
		Calendar now = Calendar.getInstance();
		if(now.compareTo(endDay)<0)
		{
			realResult = RESULT_UNKNOWN;
			return;
		}
		
		Connection conn = DB.getConn();
		
		PreparedStatement ps = conn.prepareStatement("select * from DailyStock where id_stock_info=? and date between ? and ? order by date");
		ps.setInt(1, this.stockInfo.idStockInfo);
		ps.setString(2, targetStartDay);
		ps.setString(3, this.targetEndDay);
		ResultSet rs = ps.executeQuery();
		double out = Double.NaN;
		while(rs.next())
		{
			double dungrakRate = rs.getDouble("dungrak_rate");
			if(Double.isNaN(out))out = dungrakRate;
			else
			{
				out = dungrakRate*out/100.0 + dungrakRate + out;
			}
		}
		ps.close();
		rs.close();
		
		if(Double.isNaN(out))realResult = RESULT_UNKNOWN;
		realDungrakRate = out;
		
		conn.close();
	}
	
}
