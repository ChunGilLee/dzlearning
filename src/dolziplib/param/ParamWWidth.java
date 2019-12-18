package dolziplib.param;

public class ParamWWidth extends ParamSimpleInteger{
	
	int layerIndex = -1;
	
	public ParamWWidth(int min, int max, int step, int layerIndex) {
		super(min, max, step);
		// TODO Auto-generated constructor stub
		
		if(layerIndex<0)
		{
			throw new RuntimeException("layerIndex < 0");
		}
		
		this.layerIndex = layerIndex;
	}

	public int getLayerIndex()
	{
		return layerIndex;
	}
	
	public String getName()
	{
		return "W_width_of_"+this.layerIndex+"layer";
	}
}
