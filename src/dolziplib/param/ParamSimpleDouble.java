package dolziplib.param;

import java.util.LinkedList;
import java.util.Map;

public abstract class ParamSimpleDouble extends ParamDefault{
	
	double min;
	double max;
	double step;
	static final int STEP_TYPE_ADD = 1;
	static final int STEP_TYPE_MUL = 2;
	int stepType = STEP_TYPE_ADD;
	
	
	/**
	 * step must be not zero even if min == max.
	 * 
	 * @param min
	 * @param max
	 * @param step
	 */
	public ParamSimpleDouble(double min, double max, double step)
	{
		init(min,max,step,STEP_TYPE_ADD);
	}
	
	/**
	 * step must be not zero even if min == max.
	 * 
	 * @param min
	 * @param max
	 * @param step
	 */
	public ParamSimpleDouble(double min, double max, double step, int stepType)
	{
		init(min,max,step,stepType);
	}
	
	private void init(double min, double max, double step, int stepType)
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
		
		for(double i=this.min;i<=this.max;)
		{
			Cond cond = new Cond();
			cond.name = this.getName();
			cond.value = new Double(i);
			this.conditions.add(cond);
			
			if(stepType == STEP_TYPE_ADD)i=i+step;
			else if(stepType == STEP_TYPE_MUL)i=i*step;
		}
		
		
		
		this.reset();
	}
	
	public abstract String getName();
	
}
