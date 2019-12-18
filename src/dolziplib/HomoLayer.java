package dolziplib;

import java.util.LinkedList;
import java.util.Random;

import dolziplib.calculation.Cal;
import dolziplib.calculation.CalAdd;
import dolziplib.calculation.CalBatchNormalization;
import dolziplib.calculation.CalDot;
import dolziplib.calculation.CalMeanSquaredError;
import dolziplib.calculation.CalReLU;
import dolziplib.calculation.CalSigmoid;
import dolziplib.calculation.CalSoftmaxCEE;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;
import dolziplib.paramupdator.ParamUpdator;

public class HomoLayer implements Layer {
	public static boolean checkSpendTime = false;
	
	public LayerType layerType = null;
	
	public Matrix output;
	public Matrix input;
	public Matrix w;
	public Matrix b = null;
	public Matrix rightAnswer = null;
	DDouble lastOut;

	
	Matrix inputGrad = null;
	public Matrix wGrad = null;
	public Matrix bGrad = null;
	Matrix addGradTmp = null;
	Matrix sigmoidGradTmp = null;
	Matrix ceeGradTmp = null;
	
	public Matrix dotResult = null;
	public Matrix addResult = null;
	public Matrix batchNormResult = null;
	
	CalDot dot = null;
	CalAdd add = null;
	CalSigmoid sigmod = null;
	CalReLU relu = null;
	CalSoftmaxCEE cee = null;
	public CalBatchNormalization batchNormalization = null;
	CalMeanSquaredError mse = null;
	
	Layer next = null;
	Layer front = null;
	
	LinkedList<Cal> calculations = new LinkedList<Cal>();
	
	public ParamUpdator wUpdator = null;
	public ParamUpdator bUpdator = null;
			
	public boolean bachNorm = false;
	
	
	public void setUpdator(ParamUpdator pu)
	{
		wUpdator = pu.copy();
		bUpdator = pu.copy();
	}
	
	boolean inferenceMode = false;
	
	public void setInferenceMode()
	{
		this.inferenceMode = true;
		if(this.batchNormalization!=null)this.batchNormalization.setInferenceMode();
	}
	

	public void doForward() throws Exception
	{
		for(Cal cal:this.calculations)
		{
			cal.doCal(false);
		}
	}
	public void backpropagation() throws Exception
	{
		this.backpropagation(false);
	}
	public void backpropagation(boolean evaluation) throws Exception
	{
		Cal cal[] = this.calculations.toArray(new Cal[0]);
		for(int i=(cal.length-1);i>=0;i--)
		{
			cal[i].doBack(evaluation);
		}
	}
	
	private boolean checkNaN(Matrix m)
	{
		for(int i=0;i<m.getHeight();i++)
		{
			for(int j=0;j<m.getWidth();j++)
			{
				if(Double.isNaN(m.getData(i, j)))
				{
					System.out.printf("Matrix "+m.getName()+" has NaN at (%d,%d)\n",i,j);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkNaN()
	{
		boolean ret = false;
		if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_FIRST)
		{
			ret |= checkNaN(this.w);
			ret |= checkNaN(this.b);
			ret |= checkNaN(this.output);
			ret |= checkNaN(this.input);
			ret |= checkNaN(this.wGrad);
			ret |= checkNaN(this.bGrad);
			ret |= checkNaN(this.addGradTmp);
			ret |= checkNaN(this.sigmoidGradTmp);
			
		}
		else if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_MID)
		{
			ret |= checkNaN(this.w);
			ret |= checkNaN(this.b);
			ret |= checkNaN(this.output);
			ret |= checkNaN(this.input);
			ret |= checkNaN(this.wGrad);
			ret |= checkNaN(this.bGrad);
			ret |= checkNaN(this.addGradTmp);
			ret |= checkNaN(this.sigmoidGradTmp);
			
		}
		else if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_LAST)
		{
			ret |= checkNaN(this.w);
			ret |= checkNaN(this.b);
			ret |= checkNaN(this.output);
			ret |= checkNaN(this.input);
			ret |= checkNaN(this.wGrad);
			ret |= checkNaN(this.bGrad);
			ret |= checkNaN(this.addGradTmp);
			ret |= checkNaN(this.ceeGradTmp);
			ret |= Double.isNaN(this.lastOut.value);
		}
		
		return ret;
	}
	
	public void loadParamFromFile(String prefix,boolean includeInput, boolean includeOutput)
	{
		try {
			
			this.w.loadFromBinFile(prefix+"_w_matrix.bin");
			this.b.loadFromBinFile(prefix+"_b_matrix.bin");
			if(includeInput)this.input.loadFromBinFile(prefix+"_input_matrix.bin");
			if(includeOutput)this.input.loadFromBinFile(prefix+"_output_matrix.bin");
			
			if(this.batchNormalization!=null)this.batchNormalization.loadData(prefix);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			
			if(includeInput)this.input.loadFromBinFile(prefix+"_input_matrix.bin");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			
			if(includeInput)this.output.loadFromBinFile(prefix+"_output_matrix.bin");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	public void storeParamToFile(String prefix,boolean includeInput, boolean includeOutput)
	{
		try {
			this.w.saveToBinFile(prefix+"_w_matrix.bin");
			this.b.saveToBinFile(prefix+"_b_matrix.bin");
			if(includeInput)this.input.saveToBinFile(prefix+"_input_matrix.bin");
			if(includeOutput)this.input.saveToBinFile(prefix+"_output_matrix.bin");
			
			if(this.batchNormalization!=null)this.batchNormalization.saveData(prefix);
			
			//if(this.bachNorm)
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void getGradBySGD(GenericCallback forwarding, DDouble cee, Matrix param,Matrix grad)
	{
		//System.out.println("aaa:"+param.getName());
		try {
			for(int i=0;i<param.getHeight();i++)
			{
				for(int j=0;j<param.getWidth();j++)
				{
					double v = param.getData(i, j);
					param.setData(v+Net.h, i, j);
					forwarding.doGenericCallback(null);
					double ret0 = cee.value;
					param.setData(v-Net.h, i, j);
					forwarding.doGenericCallback(null);					
					double ret1 = cee.value;
					double ret = (ret0-ret1)/(2.0*Net.h);
					grad.setData(ret, i, j);
					param.setData(v, i, j);
					
					/*
					if(param.getName().equals("2Layer_b"))
					{
						System.out.printf("fxh1:%f fxh2:%f 2*h:%f ret:%f\n",ret0,ret1,2*h,ret);
					}
					*/
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void getGradBySGD(GenericCallback forwarding, DDouble cee)
	{
		getGradBySGD(forwarding, cee, w, wGrad);
		getGradBySGD(forwarding, cee, b, bGrad);
	}
	
	
	public void setInitParam(Random r)
	{
		try {
			
			double standardDeviation = 1.0;
			double t = (double)(this.input.getWidth());
			standardDeviation = 1/Math.sqrt(t);
			
			for(int i=0;i<this.w.getHeight();i++)
			{
				for(int j=0;j<this.w.getWidth();j++)
				{
					this.w.setData(r.nextGaussian()*standardDeviation, i, j);
				}
			}
			b.setData(0.0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void applyGrad()
	{
		this.wUpdator.applyGrad(w, wGrad);
		this.bUpdator.applyGrad(b, bGrad);
	}
	
	public void release()
	{		
		if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_FIRST)
		{
			input.release();
			w.release();
			output.release();
			wGrad.release();
			bGrad.release();
			addGradTmp.release();
			sigmoidGradTmp.release();
			dotResult.release();
		}
		else if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_MID)
		{
			w.release();
			output.release();
			inputGrad.release();
			wGrad.release();
			bGrad.release();
			addGradTmp.release();
			dotResult.release();
		}
		else if(this.layerType.layerLocation == LayerType.LAYER_LOCATION_LAST)
		{
			w.release();
			output.release();
			inputGrad.release();
			wGrad.release();
			bGrad.release();
			addGradTmp.release();
			ceeGradTmp.release();
			dotResult.release();
		}
		
		if(this.batchNormResult!=null) this.batchNormResult.release();
		if(this.batchNormalization !=null)this.batchNormalization.release();
		if(this.b !=null) this.b.release();
		if(this.addResult !=null) this.addResult.release();
		
		if(this.wUpdator!=null)this.wUpdator.release();
		if(this.bUpdator!=null)this.bUpdator.release();
	}


	@Override
	public Matrix getOutputMatrix() {
		// TODO Auto-generated method stub
		return this.output;
	}


	@Override
	public void setNext(Layer layer) {
		this.next = layer;
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setInputForBackPropagation(Matrix m) {
		// TODO Auto-generated method stub
		if(this.sigmod!=null)this.sigmod.setBackPropagation(m);
		else this.relu.setBackPropagation(m);
	}


	@Override
	public LayerType getLayerType() {
		// TODO Auto-generated method stub
		return this.layerType;
	}


	@Override
	public Matrix getInputMatrix() {
		// TODO Auto-generated method stub
		return this.input;
	}


	@Override
	public DDouble getErrorValue() {
		// TODO Auto-generated method stub
		if(this.layerType.layerLocation==LayerType.LAYER_LOCATION_LAST)
		{
			return this.lastOut;
		}
		throw new RuntimeException("This is not last layer :"+this.layerType.layerLocation);
		
	}


	@Override
	public Matrix getBackPropagationOutputMatrix() {
		// TODO Auto-generated method stub
		return inputGrad;
	}


}
