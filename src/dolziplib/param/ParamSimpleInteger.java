package dolziplib.param;

import java.util.LinkedList;
import java.util.Map;

public abstract class ParamSimpleInteger extends ParamDefault{
	
	int min;
	int max;
	int step;

	public Cond getCondition(int v)
	{
		for(Cond cond:this.conditions)
		{
			Integer i = (Integer)cond.value;
			if(i.intValue()==v)return cond;
		}
		return null;
	}
	
	/**
	 * step must be not zero even if min == max.
	 * 
	 * @param min
	 * @param max
	 * @param step
	 */
	public ParamSimpleInteger(int min, int max, int step)
	{
		this.min = min;
		this.max = max;
		this.step = step;
		
		if(this.min>this.max)
		{
			throw new RuntimeException("min > max");
		}
		if(this.min<=0)
		{
			throw new RuntimeException("min <= 0");			
		}
		if(this.step==0)
		{
			throw new RuntimeException("step == 0");						
		}
		
		for(int i=this.min;i<=this.max;i=i+this.step)
		{
			Cond cond = new Cond();
			cond.name = this.getName();
			cond.value = new Integer(i);
			this.conditions.add(cond);
		}
		
		
		this.reset();
	}
	
	public abstract String getName(); 
}
