package dolziplib;

import java.util.LinkedList;
import java.util.Random;

import dolziplib.HeteroLayerConfig.HeteroLayerConfigElement;
import dolziplib.calculation.Cal;
import dolziplib.calculation.CalAdd;
import dolziplib.calculation.CalBatchNormalization;
import dolziplib.calculation.CalDot;
import dolziplib.calculation.CalMeanSquaredError;
import dolziplib.calculation.CalSigmoid;
import dolziplib.calculation.CalSoftmaxCEE;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;
import dolziplib.paramupdator.ParamUpdator;

public class HeteroLayer implements Layer {

	public static boolean checkSpendTime = false;
	public LayerType layerType = null;
	public Matrix output;
	public Matrix input;
	public Matrix bias = null;
	public Matrix biasGrad = null;
	public Matrix backOutput;
	public Matrix backInput;
	
	public ParamUpdator biasUpdator = null;
		
	Layer next = null;
	Layer front = null;
	
	LinkedList<SubLayer> sublayers = new LinkedList<SubLayer>();
		
	
	private class SubLayer
	{
		HeteroLayerConfigElement configElement;
		int outputIndex;
		Matrix entireInput;
		Matrix entireOutput;
		Matrix entireBackInput;
		Matrix input;
		Matrix backInput;
		Matrix output = null;
		Matrix filter = null;
		Matrix filterGrad = null;
		CalDot calDot;
		ParamUpdator updator = null;

		public void applyGrad()
		{
			if(this.configElement.baypass==false)
			{
				this.updator.applyGrad(this.filter, this.filterGrad);
				//System.out.println("apply filter!!!!!!!!!!!!!!!!!!!:"+this.filter);
			}
		}
		
		public void setUpdate(ParamUpdator updator)
		{
			this.updator = updator.copy();
		}
		
		public SubLayer(HeteroLayerConfigElement configElement, Matrix entireInput, Matrix entireOutput, int outputIndex)
		{
			this.configElement = configElement;
			this.entireInput=entireInput;
			this.entireOutput = entireOutput;
			this.outputIndex = outputIndex;

			
			if(this.configElement.baypass==false)
			{
				this.input = Matrix.create(entireInput.getHeight(), configElement.endIndex-configElement.startIndex+1, entireInput.getMatrixType());
				filter = Matrix.create(input.getWidth(), configElement.numOfFilters, input.getMatrixType());
				filterGrad = filter.copy(false);
				output = Matrix.create(entireInput.getHeight(), filter.getWidth(), input.getMatrixType());			
				
				this.calDot = new CalDot(input, filter, output, input.getMatrixOperator(), "filter_dot");				
			}
		}
		
		public void setBackPropagation(Matrix entireBackInput)
		{
			this.entireBackInput = entireBackInput;
			if(this.configElement.baypass==false)
			{
				this.backInput = Matrix.create(entireBackInput.getHeight(), configElement.getOutputWidth(), this.entireBackInput.getMatrixType());
				this.calDot.setBackPropagation(null, filterGrad, backInput);
			}
		}
		
		public void release()
		{
			if(this.filter!=null)this.filter.release();
			if(this.output!=null)this.output.release();
			if(this.calDot!=null)this.calDot.release();
			if(this.filterGrad!=null)this.filterGrad.release();
			this.entireOutput.release();
			if(this.backInput!=null)this.backInput.release();
			if(this.updator!=null)this.updator.release();
		}
		public void doForward() throws Exception
		{
			if(this.configElement.baypass)
			{
				for(int i=0;i<this.entireInput.getHeight();i++)
				{
					for(int j=this.configElement.startIndex,k=this.outputIndex;j<=this.configElement.endIndex;j++,k++)
					{
						double v = this.entireInput.getData(i, j);
						//System.out.printf("[%d,%d] => [%d,%d] v:%f\n",i,j,i,k,v);
						this.entireOutput.setData(v,i,k);
					}
				}
			}
			else
			{
				for(int i=0;i<this.entireInput.getHeight();i++)
				{
					for(int j=0,k=this.configElement.startIndex;j<this.input.getWidth();j++,k++)
					{
						double v = this.entireInput.getData(i, k);
						this.input.setData(v,i,j);
					}
				}
				
				this.calDot.doCal(false);
				
				for(int i=0;i<this.entireInput.getHeight();i++)
				{
					for(int j=0,k=this.outputIndex;j<this.output.getWidth();j++,k++)
					{
						double v = this.output.getData(i, j);
						//System.out.printf("[%d,%d] ==> [%d,%d] v:%f\n",i,0,i,k,v);
						this.entireOutput.setData(v,i,k);
					}
				}
			}
		}
		
		public void doBack() throws Exception
		{
			if(this.configElement.baypass)
			{
				/*
				for(int i=0;i<this.entireInput.getHeight();i++)
				{
					for(int j=this.configElement.startIndex,k=this.outputIndex;j<=this.configElement.endIndex;j++,k++)
					{
						double v = this.entireInput.getData(i, j);
						//System.out.printf("[%d,%d] => [%d,%d] v:%f\n",i,j,i,k,v);
						this.entireOutput.setData(v,i,k);
					}
				}
				*/
			}
			else
			{
				for(int i=0;i<this.backInput.getHeight();i++)
				{
					for(int j=0,k=this.outputIndex;j<this.configElement.getOutputWidth();j++,k++)
					{
						double v = this.entireBackInput.getData(i, k);
						this.backInput.setData(v,i,j);
					}
				}
				
				calDot.doBack(false);
				
			}
		}

		public void setInitParam(Random r)
		{
			if(filter!=null)
			{
				double standardDeviation = 1.0;
				double t = (double)(this.input.getWidth());
				standardDeviation = 1/Math.sqrt(t);
				
				for(int i=0;i<this.filter.getHeight();i++)
				{
					for(int j=0;j<this.filter.getWidth();j++)
					{
						this.filter.setData(r.nextGaussian()*standardDeviation, i, j);
					}
				}
			}
		}
	}
	
	public HeteroLayerConfig hConfig = null;
	
	public HeteroLayer(HeteroLayerConfig hConfig)
	{
		this.hConfig = hConfig;
	}
	public void createInnerLayerStructure()
	{
	
		this.hConfig.checkOverlap(this.input.getWidth(),this.output.getWidth());
		
		int outputIndex=0;
		for(HeteroLayerConfigElement e:this.hConfig.configElements)
		{
			SubLayer sub = new SubLayer(e , this.input, this.output, outputIndex);
			this.sublayers.add(sub);
			outputIndex+=e.getOutputWidth();
		}
		this.bias = output.copy(false);
		this.biasGrad = this.bias.copy(false);
		
	}
	
	/** Each column is each filter.
	 * @param index
	 * @return
	 */
	public Matrix getFilter(int index)
	{
		
		if(this.sublayers.get(index).configElement.baypass)
		{
			//throw new RuntimeException("A layer for the index is bypass-sublayer. That has no filters");
			return null;
		}
		return this.sublayers.get(index).filter;
	}
	public Matrix getFilterGrad(int index)
	{
		if(this.sublayers.get(index).configElement.baypass)
		{
			//throw new RuntimeException("A layer for the index is bypass-sublayer. That has no filters");
			return null;
		}
		return this.sublayers.get(index).filterGrad;
	}
	public HeteroLayerConfig getHeteroLayerConfig()
	{
		return this.hConfig;
	}
	
	public void setUpdator(ParamUpdator pu)
	{
		this.biasUpdator = pu.copy();
		for(SubLayer sub:this.sublayers)
		{
			sub.setUpdate(pu);
		}
	}
	
	boolean inferenceMode = false;
	
	public void setInferenceMode()
	{
		this.inferenceMode = true;
	}
	

	public void doForward() throws Exception
	{
		for(SubLayer sub:this.sublayers)
		{
			sub.doForward();
		}
		MatrixOperator op = this.output.getMatrixOperator();
		op.add(this.output, this.bias, this.output);
	}
	public void backpropagation() throws Exception
	{
		this.backpropagation(false);
	}
	public void backpropagation(boolean evaluation) throws Exception
	{
		
		MatrixOperator op = this.backInput.getMatrixOperator();
		op.add(this.backInput, 0.0, this.biasGrad);
		for(SubLayer sub:this.sublayers)
		{
			sub.doBack();
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
		return ret;
	}
	
	public void loadParamFromFile(String prefix,boolean includeInput, boolean includeOutput)
	{


	}
	public void storeParamToFile(String prefix,boolean includeInput, boolean includeOutput)
	{
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
		for(SubLayer sublayer:this.sublayers)
		{
			if(sublayer.configElement.baypass==false)
			{
				this.getGradBySGD(forwarding, cee, sublayer.filter, sublayer.filterGrad);
			}
		}
		this.getGradBySGD(forwarding, cee, this.bias, this.biasGrad);
	}
	
	
	public void setInitParam(Random r)
	{
		for(SubLayer sub:this.sublayers)
		{
			sub.setInitParam(r);
		}
		bias.setData(0.0);
	}
	
	public void applyGrad()
	{
		this.biasUpdator.applyGrad(this.bias, this.biasGrad);
		for(SubLayer sublayer:this.sublayers)
		{
			sublayer.applyGrad();
		}
	}
	
	public void release()
	{		
		for(SubLayer sub:this.sublayers)
		{
			sub.release();
		}
		this.biasUpdator.release();
		this.bias.release();
		this.biasGrad.release();
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
		this.backInput = m;
		for(SubLayer sub:this.sublayers)
		{
			sub.setBackPropagation(this.backInput);
		}
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
		throw new RuntimeException("This is not last layer :"+this.layerType.layerLocation);
		
	}
	@Override
	public Matrix getBackPropagationOutputMatrix() {
		// TODO Auto-generated method stub
		return this.backOutput;
	}


}
