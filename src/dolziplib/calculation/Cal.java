package dolziplib.calculation;

public interface Cal {

	long doCal(boolean measureDuration) throws Exception;
	long doBack(boolean measureDuration) throws Exception;
	long getDuration();
	void release();
	
	String getName();
}
