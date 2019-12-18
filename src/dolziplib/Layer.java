package dolziplib;

import java.util.LinkedList;
import java.util.Map;
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
import mymltest.DZ2Layer;

public interface Layer {
	
	public static boolean checkSpendTime = false;
	
	public static class LayerType
	{
		public static final int LAYER_LOCATION_FIRST=1;
		public static final int LAYER_LOCATION_MID=2;
		public static final int LAYER_LOCATION_LAST=3;
		public int layerLocation=LAYER_LOCATION_MID;
		
		/*
		public static final int LAYER_ACTIVATION_NORMAL=1;
		public static final int LAYER_ACTIVATION_CUSTOM=2;
		public static final int LAYER_ACTIVATION_RELU=3;
		public int layerActivation=LAYER_ACTIVATION_NORMAL;
		*/
		
		public static final int LAYER_TYPE_HOMO=1;
		public static final int LAYER_TYPE_HETERO=2;
		public int layerType = LAYER_TYPE_HOMO;
		
		public static final int LAST_LAYER_TYPE_CEE=0;
		public static final int LAST_LAYER_TYPE_MSE=1;
		public int lastLayerType = LAST_LAYER_TYPE_CEE;
	}
	
		
	public void setUpdator(ParamUpdator pu);
	public void setInferenceMode();	
	
	public Matrix getOutputMatrix();
	public Matrix getBackPropagationOutputMatrix();
	public Matrix getInputMatrix();


	public static Layer makeFirst(Matrix input, int nextWidth, String name, DZConfig config, LayerType layerType)
	{		
		if(layerType.layerType==LayerType.LAYER_TYPE_HOMO)
		{
			HomoLayer layer = new HomoLayer();
			layer.bachNorm = config.getBoolean("batch_norm", false);
			String activation = config.getString("activation", "");
			layer.input = input;
			layer.w = Matrix.create(input.getWidth(), nextWidth, input.getMatrixType());
			layer.b = Matrix.create(1, nextWidth, input.getMatrixType());
			layer.output = Matrix.create(input.getHeight(), nextWidth, input.getMatrixType());
			layer.layerType = layerType;
			layer.layerType.layerLocation = LayerType.LAYER_LOCATION_FIRST;
			layer.dotResult = Matrix.create(input.getHeight(), layer.w.getWidth(), input.getMatrixType());
			MatrixOperator op = input.getMatrixOperator();
			layer.dot = new CalDot(layer.input, layer.w, layer.dotResult, op, name+"_dot");
			
			Matrix sigmoidInput = null;
			if(layer.bachNorm)
			{
				layer.batchNormResult = Matrix.create(input.getHeight(), layer.w.getWidth(), input.getMatrixType());
				layer.batchNormalization = new CalBatchNormalization(layer.dotResult, layer.batchNormResult, op, name+"_batchNorm", config);
				sigmoidInput = layer.batchNormResult;
			}
			else
			{
				layer.addResult = Matrix.create(input.getHeight(), layer.w.getWidth(), input.getMatrixType());
				layer.add = new CalAdd(layer.dotResult, layer.b, layer.addResult, op, name+"_add");
				sigmoidInput = layer.addResult;
			}
			
			if(activation.equals("relu"))
			{
				layer.relu = new CalReLU(sigmoidInput, layer.output,  op, name+"_relu");			
			}
			else
			{
				layer.sigmod = new CalSigmoid(sigmoidInput, layer.output,  op, name+"_sigmoid");
			}
			
			layer.calculations.add(layer.dot);
			if(layer.bachNorm)layer.calculations.add(layer.batchNormalization);
			else layer.calculations.add(layer.add);
			
			if(activation.equals("relu"))layer.calculations.add(layer.relu);
			else layer.calculations.add(layer.sigmod);
			
			//back propagation
			//layer.inputGrad = layer.input.copy(false); We don't need gradient for input matrix.
			layer.wGrad = layer.w.copy(false);
			layer.bGrad = layer.b.copy(false);
			layer.addGradTmp = layer.dotResult.copy(false);
			if(layer.bachNorm)layer.sigmoidGradTmp = layer.batchNormResult.copy(false);
			else layer.sigmoidGradTmp = layer.addResult.copy(false);
			layer.dot.setBackPropagation(null, layer.wGrad, layer.addGradTmp);
			if(layer.bachNorm)layer.batchNormalization.setBackPropagation(layer.addGradTmp, layer.sigmoidGradTmp);
			else layer.add.setBackPropagation(layer.addGradTmp, layer.bGrad, layer.sigmoidGradTmp);
			
			if(activation.equals("relu"))layer.relu.setBackPropagation(layer.sigmoidGradTmp, null);
			else layer.sigmod.setBackPropagation(layer.sigmoidGradTmp, null);
			
			layer.w.setName(name+"_w");
			layer.b.setName(name+"_b");
			layer.output.setName(name+"_output");
			layer.input.setName(name+"_intput");
			layer.wGrad.setName(name+"_wGrad");
			layer.bGrad.setName(name+"_bGrad");
			layer.addGradTmp.setName(name+"_addGradTmp");
			if(activation.equals("relu"))layer.sigmoidGradTmp.setName(name+"_ReLUGradTmp");
			else layer.sigmoidGradTmp.setName(name+"_sigmoidGradTmp");
			
			return layer;
		}
		else if(layerType.layerType==LayerType.LAYER_TYPE_HETERO)
		{
			
			final HeteroLayer layer = new HeteroLayer(config.getHeleroLayerConfig());
			layer.input = input;
			layer.output = Matrix.create(input.getHeight(), nextWidth, input.getMatrixType());
			layer.layerType = layerType;
			layer.layerType.layerLocation = LayerType.LAYER_LOCATION_FIRST;

			layer.output.setName(name+"_output");
			layer.input.setName(name+"_intput");
			
			layer.createInnerLayerStructure();
			return layer;
		}
		return null;
	}

	
	public static Layer makeAndConnectMid(Layer front, int nextWidth,  String name, DZConfig config, LayerType layerType)
	{
		
		if(layerType.layerType==LayerType.LAYER_TYPE_HETERO)
		{
			throw new RuntimeException("HETERO layer at mid is not implemented. Back propagation for input in HETERO is not implemented.");
		}
		
		HomoLayer layer = new HomoLayer();
		layer.bachNorm = config.getBoolean("batch_norm", false);;
		String activation = config.getString("activation", "");
		layer.input = front.getOutputMatrix();
		layer.w = Matrix.create(layer.input.getWidth(), nextWidth, layer.input.getMatrixType());
		layer.b = Matrix.create(1, nextWidth, layer.input.getMatrixType());
		layer.output = Matrix.create(layer.input.getHeight(), nextWidth, layer.input.getMatrixType());
		layer.layerType = layerType;
		layer.layerType.layerLocation = LayerType.LAYER_LOCATION_MID;
		
		layer.dotResult = Matrix.create(layer.input.getHeight(), layer.w.getWidth(), layer.input.getMatrixType());
		MatrixOperator op = layer.input.getMatrixOperator();
		layer.dot = new CalDot(layer.input, layer.w, layer.dotResult, op, name+"_dot");
		
		Matrix sigmoidInput = null;
		if(layer.bachNorm)
		{
			layer.batchNormResult = Matrix.create(layer.input.getHeight(), layer.w.getWidth(), layer.input.getMatrixType());
			layer.batchNormalization = new CalBatchNormalization(layer.dotResult, layer.batchNormResult, op, name+"_batchNorm",config);
			sigmoidInput = layer.batchNormResult;
		}
		else
		{
			layer.addResult = Matrix.create(layer.input.getHeight(), layer.w.getWidth(), layer.input.getMatrixType());
			layer.add = new CalAdd(layer.dotResult, layer.b,layer.addResult , op, name+"_add");
			sigmoidInput = layer.addResult;
		}
		
		if(activation.equals("relu"))
		{
			layer.relu = new CalReLU(sigmoidInput, layer.output,  op, name+"_relu");			
		}
		else
		{
			layer.sigmod = new CalSigmoid(sigmoidInput, layer.output,  op, name+"_sigmoid");
		}
		
		layer.calculations.add(layer.dot);
		if(layer.bachNorm)layer.calculations.add(layer.batchNormalization);
		else layer.calculations.add(layer.add);
		
		if(activation.equals("relu"))layer.calculations.add(layer.relu);
		else layer.calculations.add(layer.sigmod);

		front.setNext(layer);
		layer.front = front;
		
		//back propagation
		layer.inputGrad = layer.input.copy(false);
		layer.wGrad = layer.w.copy(false);
		layer.bGrad = layer.b.copy(false);
		layer.addGradTmp = layer.dotResult.copy(false);
		if(layer.bachNorm)layer.sigmoidGradTmp = layer.batchNormResult.copy(false);
		else layer.sigmoidGradTmp = layer.addResult.copy(false);
		layer.dot.setBackPropagation(layer.inputGrad, layer.wGrad, layer.addGradTmp);
		
		if(layer.bachNorm)layer.batchNormalization.setBackPropagation(layer.addGradTmp, layer.sigmoidGradTmp);
		else layer.add.setBackPropagation(layer.addGradTmp, layer.bGrad, layer.sigmoidGradTmp);

		if(activation.equals("relu"))layer.relu.setBackPropagation(layer.sigmoidGradTmp, null);
		else layer.sigmod.setBackPropagation(layer.sigmoidGradTmp, null);
		front.setInputForBackPropagation(layer.getBackPropagationOutputMatrix());
		
		layer.w.setName(name+"_w");
		layer.b.setName(name+"_b");
		layer.output.setName(name+"_output");
		layer.wGrad.setName(name+"_wGrad");
		layer.bGrad.setName(name+"_bGrad");
		layer.addGradTmp.setName(name+"_addGradTmp");
		if(activation.equals("relu"))layer.sigmoidGradTmp.setName(name+"_ReLUGradTmp");
		else layer.sigmoidGradTmp.setName(name+"_sigmoidGradTmp");
		
		return layer;	
	}
	


	public static Layer makeAndConnectLast(Layer front, Matrix rightAnswer, String name, LayerType layerType)
	{
		
		if(layerType.layerType==LayerType.LAYER_TYPE_HETERO)
		{
			throw new RuntimeException("HETERO layer at last is not implemented. Back propagation for input in HETERO is not implemented.");
		}
		
		int nextWidth = rightAnswer.getWidth();
		HomoLayer layer = new HomoLayer();
		layer.rightAnswer = rightAnswer;
		layer.input = front.getOutputMatrix();
		layer.w = Matrix.create(layer.input.getWidth(), nextWidth, layer.input.getMatrixType());
		layer.b = Matrix.create(1, nextWidth, layer.input.getMatrixType());
		layer.output = Matrix.create(layer.input.getHeight(), nextWidth, layer.input.getMatrixType());
		layer.layerType = layerType;
		layer.layerType.layerLocation = LayerType.LAYER_LOCATION_LAST;
		
		layer.dotResult = Matrix.create(layer.input.getHeight(), layer.w.getWidth(), layer.input.getMatrixType());
		MatrixOperator op = layer.input.getMatrixOperator();
		layer.dot = new CalDot(layer.input, layer.w, layer.dotResult, op, name+"_dot");
		layer.addResult = Matrix.create(layer.input.getHeight(), layer.w.getWidth(), layer.input.getMatrixType());
		layer.add = new CalAdd(layer.dotResult, layer.b, layer.addResult, op, name+"_add");
		layer.lastOut = new DDouble();
		layer.lastOut.lastLayerType = layerType.lastLayerType;
		
		Cal lastOutputCal = null;
		if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
		{
			layer.cee = new CalSoftmaxCEE(layer.addResult, layer.output, rightAnswer, layer.lastOut, op, name+"_CEE");
			lastOutputCal = layer.cee;
		}
		else if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
		{
			layer.mse = new CalMeanSquaredError(layer.addResult, layer.output, rightAnswer, layer.lastOut, op, name+"_MSE");
			lastOutputCal = layer.mse;
		}
		else throw new RuntimeException("invalid last layer type:"+layerType.lastLayerType);
		
		layer.calculations.add(layer.dot);
		layer.calculations.add(layer.add);
		layer.calculations.add(lastOutputCal);
		
		front.setNext(layer);
		layer.front = front;
		
		//back propagation
		layer.inputGrad = layer.input.copy(false);
		layer.wGrad = layer.w.copy(false);
		layer.bGrad = layer.b.copy(false);
		layer.addGradTmp = layer.dotResult.copy(false);
		layer.ceeGradTmp = layer.addResult.copy(false);
		layer.dot.setBackPropagation(layer.inputGrad, layer.wGrad, layer.addGradTmp);
		layer.add.setBackPropagation(layer.addGradTmp, layer.bGrad, layer.ceeGradTmp);
		if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
			layer.cee.setBackPropagation(layer.ceeGradTmp);
		else if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
			layer.mse.setBackPropagation(layer.ceeGradTmp);
		front.setInputForBackPropagation(layer.getBackPropagationOutputMatrix());

		layer.w.setName(name+"_w");
		layer.b.setName(name+"_b");
		layer.output.setName(name+"_output");
		layer.wGrad.setName(name+"_wGrad");
		layer.bGrad.setName(name+"_bGrad");
		layer.addGradTmp.setName(name+"_addGradTmp");
		if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_CEE)
			layer.ceeGradTmp.setName(name+"_ceeGradTmp");
		else if(layerType.lastLayerType==LayerType.LAST_LAYER_TYPE_MSE)
			layer.ceeGradTmp.setName(name+"_mseGradTmp");
		
		return layer;	
	}
	

	public void doForward() throws Exception;
	public default void backpropagation() throws Exception
	{
		this.backpropagation(false);
	}
	public void backpropagation(boolean evaluation) throws Exception;
	
	public void loadParamFromFile(String prefix,boolean includeInput, boolean includeOutput);
	public void storeParamToFile(String prefix,boolean includeInput, boolean includeOutput);
	public void getGradBySGD(GenericCallback forwarding, DDouble cee);
	
	public DDouble getErrorValue();
	
	public void setInitParam(Random r);
	public void applyGrad();
	public void release();
	public void setNext(Layer layer);
	public void setInputForBackPropagation(Matrix m);
	public LayerType getLayerType();
}
