package dolziplib.param;

import java.util.LinkedList;
import java.util.Map;

public abstract class ParamDefault implements Param{
	
	LinkedList<Cond> conditions = new LinkedList<>();
	int currentConditionIndex=0;
	
	public Cond getCurrentCondition()
	{
		return conditions.get(currentConditionIndex);
	}
	
	
	public boolean moveNextCondition()
	{
		Cond current = conditions.get(currentConditionIndex);
		
		boolean moved=false;
		LinkedList<Param> reversed = ParameterSpace.reverseList(current.params);
		LinkedList<Param> shouldBeReset = new LinkedList<>();
		for(Param param:reversed)
		{
			if(param.moveNextCondition())
			{
				moved=true;
				for(Param p2:shouldBeReset)
				{
					p2.reset();
				}
				break;
			}
			else
			{
				shouldBeReset.add(param);
			}
		}
		if(moved)return true;
				
		currentConditionIndex++;
		
		if(currentConditionIndex>=this.conditions.size())
		{
			currentConditionIndex = this.conditions.size()-1;
			return false;
		}
		return true;		
	}
	
	@Override
	public int numOfCondition() {
		// TODO Auto-generated method stub
		int sum=0;
		
		for(Cond cond:this.conditions)
		{
			int sum0 = 1;
			if(cond.params.size()>0)
			{
				for(Param param:cond.params)
				{
					sum0 *= param.numOfCondition();
				}
				sum = sum + sum0;
			}
			else sum++;
		}
		
		return sum;
	}

	@Override
	public void putCorrentCondition(Map<String,Object> map)
	{
		Cond current = conditions.get(currentConditionIndex);
		map.put(this.getName(), current.value);
		for(Param param:current.params)
		{
			param.putCorrentCondition(map);
		}
	}
	
	
	@Override
	public boolean isNextExists() {
		// TODO Auto-generated method stub
		
		Cond current = conditions.get(currentConditionIndex);
		
		for(Param p:current.params)
		{
			if(p.isNextExists())return true;
		}
		
		if(currentConditionIndex>=this.conditions.size())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		this.currentConditionIndex = 0;
		
		for(Cond cond:this.conditions)
		{
			for(Param param:cond.params)
			{
				param.reset();
			}
		}
	}
	
}
