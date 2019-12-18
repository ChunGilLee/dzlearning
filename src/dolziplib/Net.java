package dolziplib;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import dolziplib.Layer.LayerType;
import dolziplib.matrix.Matrix;
import dolziplib.paramupdator.ParamUpdator;
import dolziplib.paramupdator.ParamUpdatorAdaGrad;
import dolziplib.paramupdator.ParamUpdatorAdam;
import dolziplib.paramupdator.ParamUpdatorMomentum;
import dolziplib.paramupdator.ParamUpdatorNormal;

public class Net {

	LinkedList<Layer> layers = new LinkedList<>();
	Matrix rightAnswer = null;
	Random r = new Random(Calendar.getInstance().getTimeInMillis());
	public static double h = 1e-3;
	
	Layer firstLayer = null;
	Layer lastLayer = null;
	
	public double correctRate = 0.0;
	
	boolean inferenceMode = false;
	
	DZConfig config = null;
	

	public Net(DZConfig config)
	{
		this.config = config;
	}
	
	public void setRandom(Random r)
	{
		this.r = r;
	}
	
	public void setInferenceMode()
	{
		this.inferenceMode = true;
		for(Layer n:layers)
		{
			n.setInferenceMode();
		}
	}
	
	public Matrix getInferencedResult()
	{
		return this.getLasyLayer().getOutputMatrix();
	}
	
	private ParamUpdator updator = new ParamUpdatorNormal(0.1);
	
	public void setNormalUpdator(double learningRate)
	{
		updator = new ParamUpdatorNormal(learningRate);
		for(Layer n:layers)
		{
			n.setUpdator(this.updator);
		}
	}
	public void setMomentumUpdator(double learningRate,double momentum)
	{
		updator = new ParamUpdatorMomentum(learningRate, momentum);
		for(Layer n:layers)
		{
			n.setUpdator(this.updator);
		}
	}
	public void setAdaGradUpdator(double learningRate)
	{
		updator = new ParamUpdatorAdaGrad(learningRate);
		for(Layer n:layers)
		{
			n.setUpdator(this.updator);
		}
	}
	public void setAdamUpdator(double learningRate, double beta1, double beta2)
	{
		updator = new ParamUpdatorAdam(learningRate,beta1,beta2);
		for(Layer n:layers)
		{
			n.setUpdator(this.updator);
		}
	}
	
	private void calCorectRate()
	{
		if(this.lastLayer==null)throw new RuntimeException("No last layer");
		Layer n = this.lastLayer;
		if(n.getLayerType().lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			int rightCount = 0;
			Matrix output = n.getOutputMatrix();
			for(int i=0;i<output.getHeight();i++)
			{
				if(this.rightAnswer.getMaxIndex(i) == output.getMaxIndex(i))rightCount++;			
			}
			
			correctRate = (double)rightCount/(double)output.getHeight();
		}
		else if(n.getLayerType().lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
		{
			double v = this.getCEE();
			v = v*2;
			v = Math.sqrt(v);
			correctRate = v;
		}
		else
		{
			throw new RuntimeException("invalid last layer type");
		}
	}
	
	
	public void addFirstNeuron(Matrix input, int nextWidth,LayerType layerType)
	{
		if(this.firstLayer!=null)
		{
			throw new RuntimeException("First Layer Already added");
		}
		
		layers.clear();
		layerType.layerLocation = LayerType.LAYER_LOCATION_FIRST;
		
		Layer n = Layer.makeFirst(input, nextWidth,  "0Layer", this.config, layerType);
		n.setUpdator(this.updator);
		layers.add(n);
		this.firstLayer = n;
	}

	public void addMidNeuron(int nextWidth,LayerType layerType)
	{
		Layer lastOne = layers.getLast();
		layerType.layerLocation = LayerType.LAYER_LOCATION_MID;
		Layer n = Layer.makeAndConnectMid(lastOne, nextWidth,  ""+layers.size()+"Layer", this.config, layerType);
		n.setUpdator(this.updator);
		layers.add(n);
	}

	public void addLastNeuron(Matrix rightAnswer,LayerType layerType)
	{
		if(this.lastLayer!=null)
		{
			throw new RuntimeException("Last Layer Already added");
		}
		layerType.layerLocation = LayerType.LAYER_LOCATION_LAST;
		
		this.rightAnswer = rightAnswer;
		Layer lastOne = layers.getLast();
		Layer n = Layer.makeAndConnectLast(lastOne, rightAnswer, ""+layers.size()+"Layer", layerType);
		n.setUpdator(this.updator);
		layers.add(n);
		this.lastLayer = n;
	}
	
	public int getNumOfLayers()
	{
		return layers.size();
	}
	
	public int getLastLayerIndex()
	{
		return (layers.size() - 1);
	}
	public Layer getLasyLayer()
	{
		return this.layers.get(this.getLastLayerIndex());
	}
	
	public Matrix getW(int layer)
	{
		Layer n = this.layers.get(layer);
		if(n instanceof HomoLayer)
		{
			HomoLayer hl = (HomoLayer)n;
			return hl.w;
		}
		throw new RuntimeException("This is not homo layer");
	}
	
	public Matrix getB(int layer)
	{
		Layer n = this.layers.get(layer);
		if(n instanceof HomoLayer)
		{
			HomoLayer hl = (HomoLayer)n;
			return hl.b;
		}
		throw new RuntimeException("This is not homo layer");
	}
	
	public Matrix getLastResult()
	{
		Layer n = this.layers.getLast();
		return n.getOutputMatrix();
	}
	
	public double getCEE()
	{
		Layer n = this.layers.getLast();
		DDouble d = n.getErrorValue();
		//System.out.println("last layer type:"+d.lastLayerType);
		return d.value;
	}
	
	public void forward() throws Exception
	{
		
		if(this.firstLayer==null)throw new Exception("No first layer");
		if(this.lastLayer==null)throw new Exception("No last layer");
		
		for(Layer n:this.layers)
		{
			DZHelper.checkData(n.getInputMatrix());
			n.doForward();
			DZHelper.checkData(n.getOutputMatrix());
		}
		calCorectRate();
	}
	
	public void getGradBySGD()
	{
		final Layer last = this.layers.getLast();
		int layer=0;
		for(Layer n:this.layers)
		{
			System.out.println("Getting "+layer+" layer ...");
			n.getGradBySGD(new GenericCallback() {
				
				@Override
				public Object doGenericCallback(Object o) {
					// TODO Auto-generated method stub
					try {
						Net.this.forward();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			}, last.getErrorValue());
			layer++;
		}		
		System.out.println("completed");
	}
	
	public void evaluation() throws Exception
	{
		System.out.println("CEE:"+this.getCEE()+" CorrectRate:"+this.correctRate);
	}
	
	public void loadParamFromFile()
	{
		this.loadParamFromFile("right",false,false);
	}
	public void loadParamFromFile(String prefix,boolean includeInput, boolean includeOutput)
	{
		try {
			
			int layer=0;
			for(Layer n:this.layers)
			{
				n.loadParamFromFile(prefix+"_"+layer, includeInput, includeOutput);
				layer++;
			}
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void storeParamToFile()
	{
		storeParamToFile("testing",false,false);
	}
	public void storeParamToFile(String prefix,boolean includeInput, boolean includeOutput)
	{
		try {
			
			int layer=0;
			for(Layer n:this.layers)
			{
				n.storeParamToFile(prefix+"_"+layer, includeInput, includeOutput);
				layer++;
			}
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setInitParam()
	{
		for(Layer n:this.layers)
		{
			n.setInitParam(this.r);
		}
	}
	
	public Layer getLayer(int layer)
	{
		return this.layers.get(layer);
	}
	
	public void backpropagation() throws Exception
	{
		this.backpropagation(false);
	}

	public void backpropagation(boolean evaluation) throws Exception
	{
		Layer n[] = this.layers.toArray(new Layer[0]);
		for(int i=(n.length-1);i>=0;i--)
		{
			long startT=0;
			if(evaluation)
			{
				startT = System.nanoTime();
			}
			n[i].backpropagation(evaluation);
			if(evaluation)
			{
				long endT = System.nanoTime();
				System.out.println("back propa ["+i+"] "+(endT-startT)/1000000+" ms");
			}
		}
	}
	
	public void applyGrad()
	{
		for(Layer n:this.layers)
		{
			n.applyGrad();
			if(n instanceof HomoLayer)
			{
				HomoLayer hl = (HomoLayer)n;
				DZHelper.checkData(hl.w);				
			}
		}
	}
	
	public void release()
	{		
		for(Layer n:this.layers)
		{
			n.release();
		}
	}
	
}
