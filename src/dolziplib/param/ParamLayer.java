package dolziplib.param;

public class ParamLayer extends ParamSimpleInteger{
	
	
	public ParamLayer(int min, int max) {
		super(min, max, 1);
		// TODO Auto-generated constructor stub
		
	}
	
	public String getName()
	{
		return "num_of_layer";
	}
}
