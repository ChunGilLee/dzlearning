package dolziplib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


import dolziplib.matrix.Matrix;

public class DZHelper {

	public static double getAverage(double[] data)
	{
		double sum = 0.0;
		for(int i=0;i<data.length;i++)
		{
			sum += data[i];
		}
		return sum/(double)data.length;
	}
	
	public static boolean checkMatrix(String name, Matrix a, Matrix b)
	{
		System.out.print("checking "+name+" ...");
		for(int i=0;i<a.getHeight();i++)
		{
			for(int j=0;j<a.getWidth();j++)
			{
				double delta = a.getData(i, j) - b.getData(i, j);
				
				if(delta<0)delta *= -1;
				if(delta>0.01)
				{
					System.out.printf("[%d,%d] %f <==> %f | %f\n",i,j,a.getData(i, j),b.getData(i, j),delta);
					return true;
				}
			}
		}
		System.out.println("OK");
		return false;
	}
	
	public static void saveFileFromMap(Map<String,Object> map,String path) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(path);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (Map.Entry<String,Object> entry : map.entrySet()) {
			  String key = entry.getKey();
			  Object value = entry.getValue();
			  
			  if(value instanceof String)
			  {
				  bw.write(key+"[{String}]="+value);
				  bw.newLine();
				  
			  }
			  else if(value instanceof Integer)
			  {
				  bw.write(key+"[{Integer}]="+value);
				  bw.newLine();				  
			  }
			  else if(value instanceof Double)
			  {
				  bw.write(key+"[{Double}]="+value);
				  bw.newLine();				  				  
			  }
			  else if(value instanceof Boolean)
			  {
				  Boolean b = (Boolean)value;
				  if(b.booleanValue())bw.write(key+"[{Boolean}]=true");
				  else bw.write(key+"[{Boolean}]=false");
				  bw.newLine();				  				  				  
			  }
			  else
			  {
				  throw new RuntimeException("invalid object type :"+value);
			  }
			  
		}
		bw.flush();
		bw.close();
	}
	
	public static Map<String,Object> loadFileFromMap(String path) throws Exception
	{
		
		HashMap<String,Object> ret = new HashMap<String,Object>();
		
		BufferedReader in = new BufferedReader(new FileReader(path));
		String line = in.readLine();
		while(line!=null)
		{
			
			String sp[] = line.split("=");
						
			if(sp.length==2)
			{
				String name = sp[0].trim();
				String value = sp[1].trim();
				
				if(name.contains("[{String}]"))
				{
					name = name.replace("[{String}]", "");
					ret.put(name, value);
				}
				else if(name.contains("[{Integer}]"))
				{
					name = name.replace("[{Integer}]", "");
					Integer v = Integer.parseInt(value);
					ret.put(name, v);
				}
				else if(name.contains("[{Double}]"))
				{
					name = name.replace("[{Double}]", "");					
					Double v = Double.parseDouble(value);
					ret.put(name,v);
				}
				else if(name.contains("[{Boolean}]"))
				{
					name = name.replace("[{Boolean}]", "");	
					if(name.equals("true"))ret.put(name,new Boolean(true));
					else if(name.equals("false"))ret.put(name,new Boolean(false));
					else
					{
						in.close();
						throw new RuntimeException("Invalid parameter name :"+name);
					}
				}
				else
				{
					in.close();
					throw new RuntimeException("Invalid parameter name :"+name);
				}
			}
			
			line = in.readLine();
		}
		in.close();
		
		return ret;
		

	}
	
	public static void checkData(Matrix m)
	{
		for(int i=0;i<m.getHeight();i++)
		{
			for(int j=0;j<m.getWidth();j++)
			{
				double v = m.getData(i, j);
				if(Double.isNaN(v)) throw new RuntimeException("data["+i+"]["+j+"] is NaN");
				if(Double.isFinite(v)==false) throw new RuntimeException("data["+i+"]["+j+"] is not finite value");
			}
		}
	}
	
	public static void checkSize(Matrix m)
	{
		if(m.getHeight()==0 || m.getWidth()==0) throw new RuntimeException("data is zero size");
	}
	
	//startRowIndex including, endRowIndex excluding
	public static Matrix getSlice(Matrix m, int startRowIndex, int endRowIndex)
	{
		Matrix newOne = Matrix.create(endRowIndex-startRowIndex, m.getWidth(), m.getMatrixType());
		for(int i=startRowIndex,j=0;i<endRowIndex;i++,j++)
		{
			double[] row = m.getRow(i);
			newOne.setData(row, j);
		}
		return newOne;
	}
	public static Matrix getSlice(Matrix m, int startRowIndex, int endRowIndex, int startColumn, int endColumn)
	{
		Matrix newOne = Matrix.create(endRowIndex-startRowIndex, endColumn - startColumn, m.getMatrixType());
		for(int i=startRowIndex,j=0;i<endRowIndex;i++,j++)
		{
			for(int k=startColumn,w=0;k<endColumn;k++,w++)
			{
				double v = m.getData(i, k);
				newOne.setData(v,j,w);
			}
		}
		return newOne;
	}
	
	public static void printArray(int[] input)
	{
		for(int i=0;i<input.length;i++)
		{
			System.out.printf("%d ",input[i]);
		}
		System.out.println("");
	}	
	public static void printArray(double[] input)
	{
		for(int i=0;i<input.length;i++)
		{
			System.out.printf("%f ",input[i]);
		}
		System.out.println("");
	}
	public static void printArray(double[][] input)
	{
		for(int i=0;i<input.length;i++)
		{
			for(int j=0;j<input[i].length;j++)
			{
				System.out.printf("%f ",input[i][j]);
			}
			System.out.println("");
		}
		System.out.println("");
	}
	public static void printArray(String prefix,double[] input)
	{
		System.out.print(prefix);
		for(int i=0;i<input.length;i++)
		{
			System.out.printf("%f ",input[i]);
		}
		System.out.println("");
	}
	public static void printArray(String prefix,double[][] input)
	{
		System.out.print(prefix +" "+input.length+"x"+input[0].length+" array\n");
		for(int i=0;i<input.length;i++)
		{
			for(int j=0;j<input[i].length;j++)
			{
				System.out.printf("%f ",input[i][j]);
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	public static String getConfigFromDB(String name)
	{
		String value = null;
		try {
			Connection conn = DB.getConn();
			PreparedStatement ps = conn.prepareStatement("use stlearn");
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("select value from config where name=?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				value = rs.getString("value");
			}
			rs.close();
			ps.close();
			
			conn.close();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return value;
	}
	
	public static Matrix mergeToRight(Matrix a, Matrix b)
	{
		if(a.getHeight()!=b.getHeight())
		{
			throw new RuntimeException("a.height != b.height");
		}
		Matrix c = Matrix.create(a.getHeight(), a.getWidth()+b.getWidth(), a.getMatrixType());
		for(int i=0;i<a.getHeight();i++)
		{
			int index=0;
			for(int j=0;j<a.getWidth();j++,index++)
			{
				c.setData(a.getData(i, j),i,index);
			}
			for(int j=0;j<b.getWidth();j++,index++)
			{
				c.setData(b.getData(i, j),i,index);
			}
		}
		return c;
	}
	
	public static void updateConfigInDB(String name,String value)
	{
		try {
			Connection conn = DB.getConn();
			PreparedStatement ps = conn.prepareStatement("use stlearn");
			ps.execute();
			ps.close();

			ps = conn.prepareStatement("lock tables config write");
			
			
			ps = conn.prepareStatement("select id_config from config where name=?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			int idConfig = 0;
			if(rs.next())
			{
				idConfig = rs.getInt("id_config");
			}
			rs.close();
			ps.close();
			
			if(idConfig!=0)
			{
				ps = conn.prepareStatement("update config set value=?,updated=now()");
				ps.setString(1, value);
				ps.execute();
				ps.close();
			}
			else
			{
				ps = conn.prepareStatement("insert into config set name=?,value=?,updated=now()");
				ps.setString(1, name);
				ps.setString(2, value);
				ps.execute();
				ps.close();
			}
			
			ps = conn.prepareStatement("unlock tables");
			ps.execute();
			ps.close();
			
			
			conn.close();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static String arrayToCommaSeperatedString(int ids[])
	{
		if(ids.length==0)return "";
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<ids.length;i++)
		{
			if(i!=0)sb.append(',');
			sb.append(ids[i]);
		}
		return sb.toString();
	}
	public static int getIndexOfOneHotEncoding(double data[])
	{
		for(int i=0;i<data.length;i++)if(data[i]>0.5)return i;
		return -1;
	}
	public static int getIndexOfMax(double data[])
	{
		double max=Double.NaN;
		int index=-1;
		for(int i=0;i<data.length;i++)
		{
			if(Double.isNaN(max))
			{
				max = data[i];
				index = i;
			}
			else
			{
				if(data[i]>max)
				{
					max = data[i];
					index=i;
				}
			}
		}
		return index;
	}
}
