package dzlearning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import dolziplib.DZHelper;
import dolziplib.matrix.Matrix;


public class DataGenerator {

	public int getIdStockInfoByName(String stockName) throws Exception
	{
		Connection conn = DB.getConn();
		
		try {
			PreparedStatement ps = conn.prepareStatement("select id_stock_info from StockInfo where name=?");
			ps.setString(1, stockName);
			ResultSet rs = ps.executeQuery();
			int idStockInfo = 0;
			if(rs.next())
			{
				idStockInfo = rs.getInt("id_stock_info");
			}
			rs.close();
			ps.close();
			return idStockInfo;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		conn.close();
		return 0;
	}
	
	
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
	
	public static int[] getTargetCompany() throws SQLException
	{
		Connection conn = DB.getConn();
		int ids[] = getInputDataTargetCompany(conn);
		conn.close();
		return ids;
	}
	
	//우선 2017년 1월 2일에 존재하고 2018년 6월 28에 존재하는 회사만을 기준으로 함.
	public static int[] getInputDataTargetCompany(Connection conn) throws SQLException
	{
		LinkedList<Integer> id2007 = new LinkedList<>();
		PreparedStatement ps = conn.prepareStatement("select id_stock_info from DailyStock where date='2007-01-02'");
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			id2007.add(new Integer(rs.getInt("id_stock_info")));
		}
		rs.close();
		ps.close();
		
		LinkedList<Integer> id2018 = new LinkedList<>();
		ps = conn.prepareStatement("select id_stock_info from DailyStock where date='2018-06-28'");
		rs = ps.executeQuery();
		while(rs.next())
		{
			id2018.add(new Integer(rs.getInt("id_stock_info")));
		}
		rs.close();
		ps.close();
		
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
	
	private static String getInPhaseFromArray(int ids[])
	{
		if(ids.length==0)return "()";
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(int i=0;i<ids.length;i++)
		{
			if(i!=0)sb.append(',');
			sb.append(ids[i]);
		}
		sb.append(')');
		return sb.toString();
	}
	
	private static void checkData(double [] data)
	{
		for(int j=0;j<data.length;j++)
		{
			double v = data[j];
			if(Double.isNaN(v)) throw new RuntimeException("data["+j+"] is NaN");
			if(Double.isFinite(v)==false) throw new RuntimeException("data["+j+"] is not finite value");
		}
	}
	private static void checkData(double data)
	{
		if(Double.isNaN(data)) throw new RuntimeException("data is NaN");
		if(Double.isFinite(data)==false) throw new RuntimeException("data is not finite value");
	}
	public STData loadInputDataFromDB() throws Exception
	{
		//2007년 1월 1일이 월요일
		return loadInputDataFromDB("2007-01-01","2018-05-31");
	}
	
	//입력 date는 input date기준임. 2007-01-01, 2007-01-07 인 경우, 입력데이터는 2007-01-01~2007-01-07이며, 정답데이터는 2007-01-07~2007-01-14임.
	public STData loadInputDataFromDB(String startDate,String endDate) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Connection conn = DB.getConn();
		
		int r[] = getInputDataTargetCompany(conn);
		String ids = getInPhaseFromArray(r);
		//PreparedStatement ps
		CompanyStockInfo companyStockInfo[] = new CompanyStockInfo[r.length];
		for(int i=0;i<companyStockInfo.length;i++)
		{
			companyStockInfo[i] = new CompanyStockInfo();
			companyStockInfo[i].idStockInfo = r[i];
		}
		
		//날짜를 해당 주의 일요일/토요일로 맞춤.
		Calendar startDay = Calendar.getInstance();
		startDay.setTime(sdf.parse(startDate));
		startDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		Calendar endDay = Calendar.getInstance();
		endDay.setTime(sdf.parse(endDate));
		endDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		
		STData stdata = new STData();
		stdata.inputStartDate = sdf.format(startDay.getTime());
		stdata.inputEndDate = sdf.format(endDay.getTime());
		startDay.add(Calendar.DAY_OF_YEAR, 7);
		endDay.add(Calendar.DAY_OF_YEAR, 7);
		stdata.rightStartDate = sdf.format(startDay.getTime());
		stdata.rightEndDate = sdf.format(endDay.getTime());
		startDay.add(Calendar.DAY_OF_YEAR, -7);
		endDay.add(Calendar.DAY_OF_YEAR, -7);
		
		
		boolean inputEnabled=true;
		while(true)
		{
			
			//2017년 9월30일 부터 10월9일까지 어떤 일이지 모르겠지만 장이 열리지 않았음.(아니면 내가 데이터를 못 모았을수도 있음)
			//건너뛰는 코드 필요.
			
			boolean skipAddingData = false;
			System.out.println("loading input data("+sdf.format(startDay.getTime())+")...");
			if(inputEnabled)skipAddingData |= loadInputdataFromDBAWeek(conn,startDay,companyStockInfo,ids);
			
			startDay.add(Calendar.DAY_OF_YEAR, 7);
			if(skipAddingData)System.out.println("skipped by nothing of input data.");
			skipAddingData |= getRightAnswerFromDBAWeek(conn, startDay, companyStockInfo,ids);
	
			if(skipAddingData==false)
			{
				for(int i=0;i<companyStockInfo.length;i++)
				{
					//System.out.printf("%d has %d,(%s),RA:%f\n",companyStockInfo[i].idStockInfo,
					//		companyStockInfo[i].info.size(),companyStockInfo[i].printInputDataArray(),companyStockInfo[i].rightAnswer);
					
					checkData(companyStockInfo[i].rightAnswer);
					stdata.addRightAnswer(companyStockInfo[i].idStockInfo, companyStockInfo[i].rightAnswer, sdf.format(startDay.getTime()));
					
				}
				
				if(inputEnabled)
				{
					double[] inputArray = getInputArray(companyStockInfo, 5);
					//DZHelper.printArray(inputArray);
					checkData(inputArray);
					stdata.inputData.add(inputArray);
				}
			}
			else System.out.println("skipped by nothing of right answer.");
			if(startDay.after(endDay))break;
			
			for(int i=0;i<companyStockInfo.length;i++)
			{
				companyStockInfo[i].clear();
			}
		}
		
		conn.close();
		
		return stdata;
	}
	
	private static double[] getInputArray(CompanyStockInfo companyStockInfo[], int widthForEachCompany)
	{
		double[] ret = new double[companyStockInfo.length*widthForEachCompany];
		int index=0;
		for(int i=0;i<companyStockInfo.length;i++)
		{
			if(companyStockInfo[i].info.size()>widthForEachCompany)throw new RuntimeException("info size > widthForEachCompany");
			int padding = widthForEachCompany - companyStockInfo[i].info.size();
			int k=0;
			for(k=0;k<padding;k++)ret[index+k] = 0.0;
			for(DailyInfo dailyInfo:companyStockInfo[i].info)
			{
				ret[index+k] = dailyInfo.getInputData();
				k++;
			}
			index+=widthForEachCompany;
		}
		return ret;
	}
	
	
	private static class CompanyStockInfo
	{
		int idStockInfo=0;
		LinkedList<DailyInfo> info = new LinkedList<>();
		LinkedList<DailyInfo> forRightAnswer = new LinkedList<>();
		double rightAnswer = Double.NaN;
		public CompanyStockInfo() {
			// TODO Auto-generated constructor stub
		}
		public void add(DailyInfo i)
		{
			if(this.idStockInfo==i.idStockInfo)this.info.add(i);
		}
		public String printInputDataArray()
		{
			String ret ="";
			for(DailyInfo i:info)
			{
				if(i!=info.getFirst())ret+=",";
				ret+=i.getInputData();
			}
			return ret;
		}
		public void clear()
		{
			this.info.clear();
			this.rightAnswer = Double.NaN;
			this.forRightAnswer.clear();
		}
	}
	
	/**
	 * @param conn
	 * @param startDayOfWeek
	 * @param companyStockInfo
	 * @param ids
	 * @return true: There is nothing to right answer. false: At least, one of right answer exists.
	 * @throws SQLException
	 */
	private boolean getRightAnswerFromDBAWeek(Connection conn, Calendar startDayOfWeek,CompanyStockInfo companyStockInfo[],String ids) throws SQLException
	{
		Calendar endDayOfWeek = Calendar.getInstance();
		endDayOfWeek.setTime(startDayOfWeek.getTime());
		endDayOfWeek.add(Calendar.DAY_OF_YEAR, 6);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String startD = sdf.format(startDayOfWeek.getTime());
		String endD = sdf.format(endDayOfWeek.getTime());

		PreparedStatement ps = conn.prepareStatement("select * from DailyStock where date between ? and ? and id_stock_info in "+ids+" order by date asc");
		ps.setString(1, startD);
		ps.setString(2, endD);

		//System.out.println("ps:"+ps);
		ResultSet rs = ps.executeQuery();
		int count= 0;
		while(rs.next())
		{
			DailyInfo di = DailyInfo.getFromResultSet(rs);
			for(int i=0;i<companyStockInfo.length;i++)
			{
				if(companyStockInfo[i].idStockInfo == di.idStockInfo)
				{
					companyStockInfo[i].forRightAnswer.add(di);
					break;
				}
			}
			count ++;
		}
		rs.close();
		ps.close();
		
		if(count==0)return true;
		
		for(int i=0;i<companyStockInfo.length;i++)
		{		
			try {

				double out = 0.0;
				int numOfRA=0;
				for(DailyInfo di:companyStockInfo[i].forRightAnswer)
				{
					if(numOfRA==0)out = di.dungrakRate;
					else
					{
						//if(di.idStockInfo==1)System.out.println(String.format("%f %f",out,di.dungrakRate));
						out = di.dungrakRate*out/100.0 + di.dungrakRate + out;
					}
					numOfRA++;
				}
				companyStockInfo[i].rightAnswer = out;
				
				//15% rising x 5 times = about 102%. I set 110 for 102%
				if(companyStockInfo[i].forRightAnswer.get(0).date.compareTo("2015-01-01") < 0 &&
						companyStockInfo[i].rightAnswer > 110 || companyStockInfo[i].rightAnswer < -110)
				{
					throw new RuntimeException(String.format("rightAnswer is wrong:%f idStockInfo:%d startdate:%s",
							companyStockInfo[i].rightAnswer,companyStockInfo[i].idStockInfo,
							companyStockInfo[i].forRightAnswer.get(0).date));
				}
				
				//30% rising x 5 times = about 271%. I set 275 for 271%
				if(companyStockInfo[i].forRightAnswer.get(0).date.compareTo("2015-01-01") >= 0 &&
						companyStockInfo[i].rightAnswer > 275 || companyStockInfo[i].rightAnswer < -275)
				{
					throw new RuntimeException(String.format("rightAnswer is wrong:%f idStockInfo:%d startdate:%s",
							companyStockInfo[i].rightAnswer,companyStockInfo[i].idStockInfo,
							companyStockInfo[i].forRightAnswer.get(0).date));
				}
				
			}catch(Exception ex)
			{
				System.out.println("getRightAnswerFromDBAWeek id_stock_info:"+companyStockInfo[i].idStockInfo);
				throw ex;
			}
		}
		return false;
	}

	/**
	 * @param conn
	 * @param startDayOfWeek
	 * @param companyStockInfo
	 * @param ids
	 * @return true: There is nothing to right answer. false: At least, one of right answer exists.
	 * @throws SQLException
	 */
	private boolean loadInputdataFromDBAWeek(Connection conn, Calendar startDayOfWeek,CompanyStockInfo companyStockInfo[], String ids) throws SQLException
	{
		Calendar endDayOfWeek = Calendar.getInstance();
		endDayOfWeek.setTime(startDayOfWeek.getTime());
		endDayOfWeek.add(Calendar.DAY_OF_YEAR, 6);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String startD = sdf.format(startDayOfWeek.getTime());
		String endD = sdf.format(endDayOfWeek.getTime());
		PreparedStatement ps = conn.prepareStatement("select * from DailyStock where date between ? and ? and id_stock_info in "+ids+" order by date");
		ps.setString(1, startD);
		ps.setString(2, endD);
		ResultSet rs = ps.executeQuery();
		int count=0;
		while(rs.next())
		{
			DailyInfo di = DailyInfo.getFromResultSet(rs);
			for(int i=0;i<companyStockInfo.length;i++)
			{
				companyStockInfo[i].add(di);
			}
			count++;
		}
		rs.close();
		ps.close();
		if(count==0)return true;
		return false;
	}
	
	
}
