package dolziplib.param;

import java.util.Map;

public interface Param {
	
	public String getName();
	public boolean isNextExists();
	public void reset();
	public int numOfCondition();
	public boolean moveNextCondition();
	public void putCorrentCondition(Map<String,Object> map);
	public Cond getCurrentCondition();
}
