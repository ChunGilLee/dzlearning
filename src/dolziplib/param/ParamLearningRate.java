package dolziplib.param;

public class ParamLearningRate extends ParamSimpleDouble{
	
	public ParamLearningRate(double min, double max, double step) {
		super(min, max, step);
		// TODO Auto-generated constructor stub
	}
	public ParamLearningRate(double min, double max, double step, int stepType) {
		super(min, max, step, stepType);
		// TODO Auto-generated constructor stub
	}

	public String getName()
	{
		return "learning_rate";
	}

}
