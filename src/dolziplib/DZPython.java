package dolziplib;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import dolziplib.matrix.Matrix;

public class DZPython {
	
	private static LinkedList<Double> getColumnData(String d)
	{
		LinkedList<Double> ret = new LinkedList<Double>();
		String sp[] = d.split(" ");
		for(int i=0;i<sp.length;i++)
		{
			String v = sp[i].trim();
			if(v.length()==0)continue;
			Double dv = Double.parseDouble(v);
			ret.add(dv);
		}
		return ret;
	}
	
	private static class NextRowTextResult
	{
		String rowString;
		int lastWordIndex;
	}
	private static NextRowTextResult getNextRowText(String data,int fromIndex)
	{
		int start = data.indexOf('[', fromIndex);
		int end = data.indexOf(']', fromIndex);
		
		if(start < 0 || end < 0)return null;
		
		NextRowTextResult ret = new NextRowTextResult();
		ret.rowString = data.substring(start,end);
		ret.lastWordIndex = end;
		return ret;
	}
	
	public static Matrix getFromPythonPrintFile(String file, int matrixType)
	{
		try {
			
			String text = new String(Files.readAllBytes(Paths.get(file)));
			text = text.replaceAll("(\r\n|\n|\r)", "");
			
			//처음 대갈호와 마지막대갈호 삭제
			text = text.replace("[[", "[");
			text = text.replace("]]", "]");
			
			//System.out.println("original:"+text);
		
			int width=0;
			LinkedList<LinkedList<Double>> rows = new LinkedList<LinkedList<Double>>();
			int fromIndex=0;
			while(true)
			{
				NextRowTextResult dRes = getNextRowText(text, fromIndex);
				if(dRes==null)break;
				//System.out.println(""+dRes.lastWordIndex+":"+dRes.rowString);
				fromIndex = dRes.lastWordIndex+1;
				
				dRes.rowString = dRes.rowString.replace('[', ' ');
				dRes.rowString = dRes.rowString.replace(']', ' ');
				dRes.rowString = dRes.rowString.trim();
				LinkedList<Double> v = getColumnData(dRes.rowString);
				if(width==0)
				{
					width = v.size();
				}
				else
				{
					if(width != v.size())
					{
						throw new Exception("matrix width not match. prediction:"+width+",current:"+v.size()); 
					}
				}
				rows.add(v);
			}
			
			if(rows.size()==0)return null;
			Matrix mat = Matrix.create(rows.size(), width, matrixType);
			int columnCount=0;
			int rowCount=0;
			for(LinkedList<Double> row:rows)
			{
				columnCount=0;
				for(Double dv:row)
				{
					//mat.set
					mat.setData(dv.doubleValue(), rowCount, columnCount);
					columnCount++;
				}
				rowCount++;
			}
			return mat;
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
}
