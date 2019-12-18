package dolziplib.param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import dolziplib.Layer;
import dolziplib.Layer.LayerType;

public class ParameterSpace {
	
	LinkedList<Param> params = new LinkedList<>();
	boolean fisrt = true;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		int totalCond = 0;
		for(Param param:params)
		{
			sb.append(param.getName());
			sb.append(':');
			sb.append(param.numOfCondition());
			sb.append('\n');
			if(totalCond==0)totalCond = param.numOfCondition();
			else totalCond = totalCond * param.numOfCondition();
		}
		sb.append("Total condition:"+totalCond);
		return sb.toString();
	}
	
	public int getTotalParamSpace()
	{
		int totalCond = 0;
		for(Param param:params)
		{
			if(totalCond==0)totalCond = param.numOfCondition();
			else totalCond = totalCond * param.numOfCondition();
		}
		return totalCond;
	}
	
	/**
	 * It causes reset of all parameters and its children.
	 */
	public void printAllConditions()
	{
		this.reset();
		
		Map<String,Object> ret = this.getNextParamTable();
		int count=0;
		while(ret!=null)
		{
			System.out.printf("[%d]----------\n",count);
			for (Map.Entry<String,Object> entry : ret.entrySet()) {
			  String key = entry.getKey();
			  Object value = entry.getValue();
			  
			  System.out.println(key+":"+value);
			}
			ret = this.getNextParamTable();
			count++;
		}

		this.reset();
	}
	
	public static ParameterSpace createParamSpace()
	{
		LinkedList<Param> params = new LinkedList<>();
		
		int minLayer=3;
		int maxLayer=5;
		int WWidthMin = 50;
		int WWidthMax = 200;
		int WWidthStep = 50;
		
		ParamLayer layerParam = new ParamLayer(minLayer, maxLayer);
		params.add(layerParam);
		
		for(int i=minLayer;i<=maxLayer;i++)
		{
			Cond cond = layerParam.getCondition(i);
			for(int j=0;j<i;j++)
			{
				ParamWWidth width = new ParamWWidth(WWidthMin, WWidthMax, WWidthStep, j);
				cond.params.add(width);
			}
		}
		
		params.add(new ParamLearningRate(0.001, 0.1, 10.0, ParamLearningRate.STEP_TYPE_MUL));
		
		ParameterSpace ps = new ParameterSpace();
		ps.params = params;
		return ps;
	}
	
	public static Map<String,Object> getStaticParameter()
	{
		return getStaticParameter(LayerType.LAST_LAYER_TYPE_CEE);
	}

	public static Map<String,Object> getStaticParameter(int lastLayerType)
	{
		HashMap<String, Object> params = new HashMap<>();
		params.put("num_of_layer", new Integer(3));
		params.put("updator", "adam"); //normal or adam
		
		params.put("use_last_mu_and_var", new Boolean(true));
		
		if(lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			params.put("learning_rate", new Double(0.001));
			params.put("learning_rate_beta1", new Double(0.9)); //for adam
			params.put("learning_rate_beta2", new Double(0.999)); //for adam
		}
		else if(lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
		{
			params.put("learning_rate", new Double(0.01));
			params.put("learning_rate_beta1", new Double(0.9)); //for adam
			params.put("learning_rate_beta2", new Double(0.999)); //for adam
		}
		else
		{
			throw new RuntimeException("invalid last layer type");
		}
		params.put("W_width_of_0layer", new Integer(50));
		params.put("W_width_of_1layer", new Integer(30));
		params.put("W_width_of_2layer", new Integer(20));
		
		params.put("last_layer_type", new Integer(lastLayerType));
		
		return params;
	}
	
	public static ParameterSpace createParamSpaceForST()
	{
		LinkedList<Param> params = new LinkedList<>();
		
		int minLayer=3;
		int maxLayer=3;
		int WWidthMin = 1000;
		int WWidthMax = 1000;
		int WWidthStep = 50;
		
		ParamLayer layerParam = new ParamLayer(minLayer, maxLayer);
		params.add(layerParam);
		
		for(int i=minLayer;i<=maxLayer;i++)
		{
			Cond cond = layerParam.getCondition(i);
			for(int j=0;j<i;j++)
			{
				ParamWWidth width = new ParamWWidth(WWidthMin, WWidthMax, WWidthStep, j);
				cond.params.add(width);
			}
		}
		
		params.add(new ParamLearningRate(0.001, 0.1, 10.0, ParamLearningRate.STEP_TYPE_MUL));
		
		ParameterSpace ps = new ParameterSpace();
		ps.params = params;
		return ps;
	}
	
	public static LinkedList<Param> reverseList(LinkedList<Param> in)
	{
		LinkedList<Param> ret = new LinkedList<Param>();
		for(Param p:in)
		{
			ret.push(p);
		}
		return ret;
	}
		
	//bug가 있음.
	public synchronized boolean isNextAvailable()
	{
		if(this.fisrt==false)return true;
		else
		{
			for(Param param:this.params)
			{
				if(param.isNextExists())return true;
			}
		}
		return false;
	}
	
	public synchronized Map<String,Object> getNextParamTable()
	{
		if(this.fisrt==false)
		{
			boolean moved=false;
			LinkedList<Param> reversed = reverseList(this.params);
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
			if(moved==false)return null;
		}
		
		HashMap<String,Object> ret = new HashMap<String,Object>();
		for(Param param:params)
		{
			param.putCorrentCondition(ret);
		}
		this.fisrt = false;
		
		return ret;
	}
	
	public void reset()
	{
		for(Param param:params)
		{
			param.reset();
		}
		this.fisrt = true;
	}
}
