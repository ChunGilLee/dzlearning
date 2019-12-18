package dolziplib;

import java.util.LinkedList;

import dolziplib.calculation.Cal;

public class Batchwork {
	
	public LinkedList<Cal> calculations = new LinkedList<Cal>();
	public long totalDuration=0;
	
	public boolean doBatch(boolean measureEachDuration)
	{
		try {
			long start = System.nanoTime();
			for(Cal cal:calculations)
			{
				cal.doCal(measureEachDuration);
			}
			long end = System.nanoTime();
			totalDuration = end-start;
			
			return false;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return true;
	}
	
	public boolean doBack()
	{
		try {
			long start = System.nanoTime();
			Cal[] cal = calculations.toArray(new Cal[0]);
			for(int i=(cal.length-1);i>=0;i--)
			{
				cal[i].doBack(false);
			}
			long end = System.nanoTime();
			totalDuration = end-start;
			
			return false;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return true;
	}
	
	public void addCal(Cal cal)
	{
		this.calculations.add(cal);
	}
	public void clearCal()
	{
		this.calculations.clear();
	}
	
}
