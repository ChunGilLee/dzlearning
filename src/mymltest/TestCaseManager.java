package mymltest;

import java.util.LinkedList;

public class TestCaseManager {
	static LinkedList<TestCase> cases = new LinkedList<>();
	
	private static void buildFullTestCase(int matrixType)
	{
		cases.clear();
		cases.add(new TestCaseOperator(matrixType));
		cases.add(new TestCaseAllInput(matrixType));
		cases.add(new TestCaseGrad(matrixType));
		cases.add(new TestCaseNetAllInput(matrixType));
		cases.add(new TestCaseNetSGD(matrixType));
		cases.add(new TestCaseNetEBP(matrixType));
		cases.add(new TestCaseBackpropagation(matrixType));
		cases.add(new TestCaseLayer(matrixType));
		
	}
	
	public static void doFullTest(int matrixType)
	{
		buildFullTestCase(matrixType);
		doTest();
	}

	public static void doMy1(int matrixType)
	{
		cases.clear();
		cases.add(new TestCaseBackpropagation(matrixType));
		doTest();
	}
	
	public static void doTest()
	{
		boolean pass = true;
		for(TestCase tc:cases)
		{
			System.out.println("TC:"+tc.getName()+"++++++++++++++++++++");
			boolean ret = tc.doTest();
			tc.release();
			if(ret)
			{
				System.out.println("TC:"+tc.getName()+" Failed ---------------");				
				pass = false;
				break;
			}
			System.out.println("TC:"+tc.getName()+" OK -----------------");				
		}
		if(pass==false)System.out.println("TestCase Result: Failed");
		else System.out.println("TestCase Result: OK");
		
	}
	
}
