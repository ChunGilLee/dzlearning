package dolziplib;

import java.util.HashMap;
import java.util.Map;

import dolziplib.Layer.LayerType;

public class DZConfig extends HashMap<String,Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -323950507695591766L;

	public int configIndex=0;
	
	public boolean getBoolean(String key,boolean defaultValue)
	{
		if(this.containsKey(key))
		{
			Boolean t = (Boolean)this.get(key);
			return t.booleanValue();
		}
		return defaultValue;
	}
	public void putBoolean(String key,boolean value)
	{
		this.put(key, new Boolean(value));
	}
	
	public String getString(String key, String defaultValue)
	{
		if(this.containsKey(key))
		{
			String t = (String)this.get(key);
			return t;
		}
		return defaultValue;
	}
	public void putString(String key, String value)
	{
		this.put(key, value);
	}
	public int getInt(String key, int defaultValue)
	{
		if(this.containsKey(key))
		{
			Integer t = (Integer)this.get(key);
			return t.intValue();
		}
		return defaultValue;
	}
	public void putInt(String key, int value)
	{
		this.put(key, new Integer(value));
	}
	
	public double getDouble(String key, double defaultValue)
	{
		if(this.containsKey(key))
		{
			Double t = (Double)this.get(key);
			return t.doubleValue();
		}
		return defaultValue;
	}
	public void putDouble(String key, double value)
	{
		this.put(key, new Double(value));
	}
	
	HeteroLayerConfig hConfig = null;
	public void setHeteroLayerConfig(HeteroLayerConfig hConfig)
	{
		this.hConfig = hConfig;
	}
	public HeteroLayerConfig getHeleroLayerConfig()
	{
		return hConfig;
	}
	
	
	public static DZConfig getStaticParameter()
	{
		return getStaticParameter(LayerType.LAST_LAYER_TYPE_CEE);
	}

	public static DZConfig getStaticParameter(int lastLayerType)
	{
		DZConfig params = new DZConfig();
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
}
