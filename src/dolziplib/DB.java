package dolziplib;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
	
	private static String objectForSync = "";
	private static String url = null;
	
	private static String password = "";
	
	private static void init() throws ClassNotFoundException
	{
		if(url != null)return;
		
		synchronized (objectForSync) {
			if(url != null)return;
			
	        Class.forName("com.mysql.jdbc.Driver");
	        url = "jdbc:mysql://localhost:3306/stock?user=stock"+
	                        "&password="+password+"&noAccessToProcedureBodies=true&useSSL=false"+
	                        "&useUnicode=yes&characterEncoding=UTF-8";
		}
	}
	
	public static Connection getConn()
	{
		try {
			init();
			return DriverManager.getConnection(url);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}
