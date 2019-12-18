package dolziplib;

import java.util.Map;

public interface Learner extends Runnable {

	public void setParameters(Map<String,Object> param);
	public void release();
	public void saveToFile(String folder, String prefix) throws Exception;
	public double getPoint();
}
