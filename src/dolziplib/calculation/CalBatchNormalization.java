package dolziplib.calculation;

import java.util.HashMap;
import java.util.Map;

import dolziplib.DDouble;
import dolziplib.DZHelper;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class CalBatchNormalization implements Cal{

	Matrix input0;
	Matrix output;
	Matrix inputBack = null;
	Matrix outputBack = null;
	Matrix xhat = null;
	Matrix xmu = null;
	public Matrix var = null;
	Matrix sqrtvar = null;
	Matrix ivar = null;
	Matrix muSum = null;
	Matrix varSum = null;
	public Matrix mu = null;
	int numOfCount=0;
	boolean inferenceMode = false;
	
	MatrixOperator op;
	
	String name;
	long duration = 0;
	
	double avg = Double.NaN;
	
	public Matrix gamma = null;
	public Matrix beta = null;
	public Matrix gammaGrad = null;
	public Matrix betaGrad = null;
	
	private static double eps = 0.000001;
	
	private boolean useLastMuAndVar=true; // uses last mu and var for inference instead of average mu and var.
	
	public void setInferenceMode()
	{
		this.inferenceMode = true;
	}
	
	public void saveData(String prefix) throws Exception
	{
		if(muSum!=null)muSum.saveToBinFile(prefix+"_BN_mu_sum_matrix.bin");
		if(varSum!=null)varSum.saveToBinFile(prefix+"_BN_var_sum_matrix.bin");
		if(gamma!=null)gamma.saveToBinFile(prefix+"_BN_gamma_matrix.bin");
		if(beta!=null)beta.saveToBinFile(prefix+"_BN_beta_matrix.bin");
		
		HashMap<String, Object> config = new HashMap<>();
		config.put("num_of_count",new Integer(this.numOfCount));
		DZHelper.saveFileFromMap(config, prefix+"_BN_config.txt");
	}
	public void loadData(String prefix) throws Exception
	{
		System.out.println("loading BN prefix:"+prefix);
		
		if(muSum==null)muSum=Matrix.loadFromBinFile(prefix+"_BN_mu_sum_matrix.bin", input0.getMatrixType());
		else muSum.loadFromBinFile(prefix+"_BN_mu_sum_matrix.bin");
		if(varSum==null)varSum=Matrix.loadFromBinFile(prefix+"_BN_var_sum_matrix.bin", input0.getMatrixType());
		else varSum.loadFromBinFile(prefix+"_BN_var_sum_matrix.bin");
		if(gamma==null)gamma=Matrix.loadFromBinFile(prefix+"_BN_gamma_matrix.bin", input0.getMatrixType());
		else gamma.loadFromBinFile(prefix+"_BN_gamma_matrix.bin");
		if(beta==null)beta=Matrix.loadFromBinFile(prefix+"_BN_beta_matrix.bin", input0.getMatrixType());
		else beta.loadFromBinFile(prefix+"_BN_beta_matrix.bin");
		
		Map<String, Object> config = DZHelper.loadFileFromMap(prefix+"_BN_config.txt");
		Integer v = (Integer)config.get("num_of_count");
		this.numOfCount = v.intValue();
		
		//System.out.println("num of count:"+this.numOfCount);
	}
	
	public CalBatchNormalization(Matrix i0, Matrix out0, MatrixOperator op,String name, Map<String, Object> config)
	{
		this.input0 = i0;
		this.output = out0;
		this.op = op;
		this.name =  name;
		
		gamma = Matrix.create(1, i0.getWidth(), i0.getMatrixType());
		beta = gamma.copy(false);
		
		gamma.setData(1.0);
		beta.setData(0.0);
		
		if(config!=null && config.containsKey("use_last_mu_and_var"))
		{
			Boolean use_last_mu_and_var = (Boolean)config.get("use_last_mu_and_var");
			this.useLastMuAndVar = use_last_mu_and_var.booleanValue();
		}
	}
	
	
	public void setBackPropagation(Matrix output,Matrix input)
	{
		this.outputBack = output;
		this.inputBack = input;
		this.gammaGrad = Matrix.create(1, inputBack.getWidth(), inputBack.getMatrixType());
		this.betaGrad = Matrix.create(1, inputBack.getWidth(), inputBack.getMatrixType());
	}
	
	boolean debugMsg = false;
	
	@Override
	public long doCal(boolean measureDuration)  throws Exception{
		// TODO Auto-generated method stub
				
		long startTime = 0;
		if(measureDuration)
		{
			startTime = System.nanoTime();
		}
		
		if(debugMsg)System.out.println("input0:"+input0); 

		int limitForSaved=100; //입력데이터의 갯수가 limitforSaved보다 적으면, 평균과 분산을 구할 수가 없다(특히 한두개라고 보면). 
		//그래서 특정갯수 이하일때는 training 시점에 사용했던 평균과 분산의 평균값을 사용하기로 한다.
		//하지만 특정 갯수 이상일때는 현재 입력되는 데이터의 평균과 분산을 사용하는게 정확도가 더 높기 때문에 이렇게 사용한다.(논문에 없는 내용)
		
		//step1
		if(mu==null)mu = Matrix.create(1, output.getWidth(), output.getMatrixType());
		if(this.inferenceMode && input0.getHeight()<limitForSaved)
		{
			if(useLastMuAndVar)op.add(muSum, 0, mu);
			else op.mult(muSum, 1.0/(double)this.numOfCount, mu);
		}
		else
		{
			mu.setData(0.0);
			for(int i=0;i<mu.getWidth();i++)
			{
				double sum = 0.0;
				for(int j=0;j<input0.getHeight();j++)
				{
					sum += input0.getData(j, i);
				}
				mu.setData(sum, 0,i);
			}
			op.mult(mu, 1.0/(double)input0.getHeight(),mu);
			
			//save mu for inference
			numOfCount++;
			if(muSum==null)muSum = mu.copy(true);
			else 
			{
				if(useLastMuAndVar)op.add(mu, 0, muSum);
				else op.add(muSum,mu , muSum);
			}
			//op.mult(muSum, 1.0/(double)numOfCount, muSum);
		}

	
		//System.out.println(name+" mu[0,0] :" +mu.getData(0, 0)+" muA[0,0] :"+muSum.getData(0, 0)+" count:"+numOfCount);
		
		//step2
		if(debugMsg)System.out.println("mu:"+mu);
		//if(this.name.equals("2Layer_batchNorm"))System.out.println("mu:"+mu);
		if(xmu==null)xmu = output.copy(false);
		op.sub(input0, mu, xmu);
		if(debugMsg)System.out.println("xmu:"+xmu);
		
		//step3
		Matrix sq = xmu.copy(false);
		op.pow(xmu, 2, sq);
		if(debugMsg)System.out.println("sq:"+sq);
		
		//step4
		if(var==null)var = mu.copy(false);
		if(this.inferenceMode && input0.getHeight()<limitForSaved)
		{
			if(useLastMuAndVar)op.add(varSum, 0, var);
			else op.mult(varSum, 1.0/(double)this.numOfCount, var);
		}
		else {
			for(int i=0;i<var.getWidth();i++)
			{
				double sum = 0.0;
				for(int j=0;j<sq.getHeight();j++)
				{
					sum += sq.getData(j, i);
				}
				var.setData(sum, 0,i);
			}
			op.mult(var, 1.0/(double)input0.getHeight(), var);
			
			//save var for inference
			if(varSum==null)varSum = var.copy(true);
			else {
				if(useLastMuAndVar)op.add(var,0 , varSum);
				else op.add(varSum,var , varSum);
			}
			//op.mult(varSum, 1.0/(double)numOfCount, varSum);
		}

		
		//step5
		if(debugMsg)System.out.println("var:"+var);
		//if(this.name.equals("2Layer_batchNorm"))System.out.println("var:"+var);
		if(sqrtvar==null)sqrtvar = var.copy(false);
		op.add(var,eps, sqrtvar);
		op.pow(sqrtvar, 0.5, sqrtvar);
		if(debugMsg)System.out.println("sqrtvar:"+sqrtvar);
		
		//step6
		if(ivar==null)ivar = sqrtvar.copy(false);
		ivar.durtyCheck();
		ivar.setData(1.0);
		sqrtvar.durtyCheck();
		
		op.div(ivar, sqrtvar, ivar);
		ivar.durtyCheck();
		if(debugMsg)System.out.println("ivar:"+ivar);
		
		//step7
		if(xhat==null)xhat = xmu.copy(false);
		op.mult(xmu, ivar, xhat);   //height not matched
		if(debugMsg)System.out.println("xhat:"+xhat);
		//System.out.println("xhat:"+xhat); //xhat값이 너무 1(또는 -1)에 가까운게 현재 문제임.!!!

		//step8
		op.mult(this.gamma,xhat,output);
		if(debugMsg)System.out.println("gamma:"+gamma);
		if(debugMsg)System.out.println("output:"+output);

		//step9
		op.add(output,this.beta,output);

		
		//System.out.println(name+" mu[0,0] :" +mu.getData(0, 0)+" muA[0,0] :"+muSum.getData(0, 0)+" count:"+numOfCount);
		//System.out.println(name+" gamma[0,0] :" +gamma.getData(0, 0));
		//System.out.println(name+" beta[0,0] :" +beta.getData(0, 0));

		sq.release();
		

		
		if(measureDuration)
		{
			long endTime = System.nanoTime();
			this.duration = endTime - startTime;
			return endTime - startTime;
		}
		else return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public long getDuration() {
		// TODO Auto-generated method stub
		return this.duration;
	}

	@Override
	public long doBack(boolean measureDuration) throws Exception {
		// TODO Auto-generated method stub
		
		if(inputBack ==null || outputBack == null) return 0;
		
		long startT = 0;
		if(measureDuration)startT = System.nanoTime();
		
		MatrixOperator op= inputBack.getMatrixOperator();
		
		//step 9
		this.betaGrad.setData(0.0);
		for(int i=0;i<inputBack.getHeight();i++)
		{
			for(int j=0;j<inputBack.getWidth();j++)
			{
				this.betaGrad.setData(this.betaGrad.getData(0, j)+inputBack.getData(i, j), 0,j);
			}
		}

		
		//step 8
		this.gammaGrad.setData(0.0);
		for(int i=0;i<inputBack.getHeight();i++)
		{
			for(int j=0;j<inputBack.getWidth();j++)
			{
				double v = this.gammaGrad.getData(0, j);
				v += inputBack.getData(i, j) * xhat.getData(i, j);
				this.gammaGrad.setData(v, 0, j);
			}
		}
		Matrix dxhat = this.inputBack.copy(true);  //dxhat h = out.h
		op.mult(inputBack,gamma, dxhat);
		if(debugMsg)System.out.println("dxhat:"+dxhat);
		
		//step 7		
		Matrix divar = Matrix.create(1, inputBack.getWidth(), inputBack.getMatrixType()); //divar h = 1
		divar.setData(0.0);
		for(int i=0;i<dxhat.getHeight();i++)
		{
			for(int j=0;j<dxhat.getWidth();j++)
			{
				double v = divar.getData(0, j);
				double dd = dxhat.getData(i, j) * xmu.getData(i, j);;
				v += dd;
				//System.out.printf("[%d,%d] %f = %f * %f, v:%f\n",i,j,dd,dxhat.getData(i, j),xmu.getData(i, j),v);
				divar.setData(v, 0, j);
			}
		}
		if(debugMsg)System.out.println("divar"+divar);
		Matrix dxmul = dxhat.copy(false);
		if(debugMsg)System.out.println("dxhat"+dxhat);
		if(debugMsg)System.out.println("ivar"+ivar);
		op.mult(dxhat, ivar, dxmul);
		dxhat.release();

		//step 6
		Matrix dsqrtvar = divar.copy(false);
		Matrix tmp111 = dsqrtvar.copy(false);
		tmp111.setData(-1.0);
		op.pow(sqrtvar, 2, dsqrtvar);
		op.div(tmp111, dsqrtvar, dsqrtvar);
		op.mult(dsqrtvar, divar, dsqrtvar);
		divar.release();
		
		//step 5
		Matrix dvar = dsqrtvar.copy(false);  //dvar h = 1
		tmp111.setData(0.5);
		Matrix tmp222 = var.copy(true);
		op.add(tmp222, eps, tmp222);
		op.pow(tmp222, 0.5, tmp222);
		op.div(tmp111, tmp222, tmp111);
		op.mult(tmp111, dsqrtvar, dvar);
		if(debugMsg)System.out.println("dvar:"+dvar);
		dsqrtvar.release();
		tmp111.release();
		tmp222.release();
		
		//step 4
		Matrix dsq = outputBack.copy(false);  //dsq h = out.h
		dsq.setData(1.0);
		op.mult(dsq, 1.0/(double)outputBack.getHeight(), dsq);
		op.mult(dsq, dvar, dsq);
		if(debugMsg)System.out.println("dsq:"+dsq);
		dvar.release();
		
		//step3
		Matrix dxmu2 = outputBack.copy(false);
		op.mult(dsq, xmu, dsq);
		op.mult(dsq, 2.0, dxmu2);//dxmu2 h = out.h
		if(debugMsg)System.out.println("dxmu2:"+dxmu2);
		dsq.release();
		
		//step2
		Matrix dx1 = dxmu2.copy(false);	
		op.add(dxmul, dxmu2, dx1); 
		Matrix dmu = Matrix.create(1, dxmul.getWidth(), dxmul.getMatrixType());
		for(int i=0;i<dxmul.getHeight();i++)
		{
			for(int j=0;j<dxmul.getWidth();j++)
			{
				double v = dmu.getData(0, j);
				v += dxmul.getData(i, j) + dxmu2.getData(i, j);
				dmu.setData(v, 0, j);
			}
		}
		op.mult(dmu, -1.0, dmu);  
		if(debugMsg)System.out.println("dmu:"+dmu);
		  
		
		//step1
		Matrix dx2 = dxmul;
		dx2.setData(1.0);
		op.mult(dx2, dmu, dx2);
		op.mult(dx2,1.0/(double)outputBack.getHeight(),dx2);
		dmu.release();

		//step0
		op.add(dx1, dx2, outputBack);
		if(debugMsg)System.out.println("dx1:"+dx1);
		if(debugMsg)System.out.println("dx2:"+dx2);

		dxmul.release();
		dxmu2.release();
		dx1.release();
		dx2.release();
		
		if(measureDuration) {
			long endT = System.nanoTime();
			System.out.printf(" normalization back:%d\n",(endT-startT)/1000);
		}
		
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
		if(this.xhat!=null)
		{
			this.xhat.release();
			this.xhat = null;
		}
		if(this.gamma!=null)
		{
			this.gamma.release();
			this.gamma = null;
		}
		if(this.beta!=null)
		{
			this.beta.release();
			this.beta = null;
		}
		if(this.gammaGrad!=null)
		{
			this.gammaGrad.release();
			this.gammaGrad = null;
		}
		if(this.betaGrad!=null)
		{
			this.betaGrad.release();
			this.betaGrad = null;
		}
		
		if(muSum!=null)
		{
			muSum.release();
			muSum = null;
		}
		
		if(varSum!=null)
		{
			varSum.release();
			varSum = null;
		}
		if(mu!=null)
		{
			mu.release();
			mu = null;
		}
	}
}
