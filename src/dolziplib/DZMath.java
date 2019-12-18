package dolziplib;

import cern.jet.math.Mult;

public class DZMath {

	public static double meanSquaredError(double[] answers, double[] rightAnswers) throws Exception
	{
		if(answers.length != rightAnswers.length)
		{
			throw new Exception("A length of asnwers is not matched to rightAnswers' length");
		}
		
		double diffSum = 0;
		for(int i=0;i<answers.length;i++)
		{
			double diff = rightAnswers[i] - answers[i];
			diffSum += diff * diff;
		}
		return diffSum/2;
	}
	
	public static double crossEntropyError(double[] answers, double[] rightAnswers) throws Exception
	{
		if(answers.length != rightAnswers.length)
		{
			throw new Exception("A length of asnwers is not matched to rightAnswers' length");
		}
		double diffSum = 0;
		for(int i=0;i<answers.length;i++)
		{
			double diff = rightAnswers[i] * Math.log(answers[i] + 0.00000001);
			diffSum += diff;
		}
		return -diffSum;	
	}
		
	private static double getMax(double[] input)
	{
		double max=-Double.MAX_VALUE;
		for(int i=0;i<input.length;i++)
		{
			if(max<input[i])max = input[i];
		}
		return max;
	}
	public static double[] getSoftMax(double[] input)
	{
		double[] ret = new double[input.length];
		for(int i=0;i<ret.length;i++)ret[i] = input[i];
		double max = getMax(input);
		double sum = 0;
		for(int i=0;i<ret.length;i++)
		{
			ret[i] -= max;
			ret[i] = Math.exp(ret[i]);
			//System.out.printf(" %f %f\n",max,ret[i]);
			sum += ret[i];
		}
		//DZHelper.printArray("softmax _________aaa",ret);
		//System.out.println("sum :"+sum);
		for(int i=0;i<ret.length;i++)
		{
			ret[i] = ret[i]/sum;
		}
		//DZHelper.printArray("softmax _________bbb("+sum+")",ret);
		return ret;
	}

	public static double sigmoid(double x)
	{
		return 1.0/(1+Math.exp(-x));
	}
	public static double getSum(double[] input)
	{
		double sum = 0.0;
		for(int i=0;i<input.length;i++)
		{
			sum += input[i];
		}
		return sum;
	}
}
