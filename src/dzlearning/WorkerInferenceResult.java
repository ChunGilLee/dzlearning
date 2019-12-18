package dzlearning;

import dolziplib.DZConfig;

public interface WorkerInferenceResult {

	public void setInferenceResult(int idStockInfo,double[][] result,double trainingCorrectRate, Exception error, DZConfig config, String paramPrefix);
	public void setTrainingResult(int idStockInfo,double trainingCorrectRate, Exception error, DZConfig config, String paramPrefix);
	public void printResult();
	public double getTrainingCorrectRate();
	public double getInferenceCorrectRate();
}
