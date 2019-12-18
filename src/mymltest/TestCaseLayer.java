package mymltest;

import java.util.Random;

import com.sun.management.GarbageCollectorMXBean;

import dolziplib.DZConfig;
import dolziplib.DZHelper;
import dolziplib.GenericCallback;
import dolziplib.HeteroLayer;
import dolziplib.HeteroLayerConfig;
import dolziplib.HeteroLayerConfig.HeteroLayerConfigElement;
import dolziplib.Layer;
import dolziplib.Layer.LayerType;
import dolziplib.matrix.Matrix;
import dolziplib.matrix.MatrixOperator;

public class TestCaseLayer implements TestCase{

	int matrixType = Matrix.MATRIX_TYPE_JAVA;
	int testInputNum = 3;
	
	public TestCaseLayer(int matrixType)
	{
		this.matrixType = matrixType;
	}
	
	private static boolean checkMatrix(Matrix a, Matrix b)
	{
		System.out.print("checking "+a.getName()+","+b.getName()+" ...");
		for(int i=0;i<a.getHeight();i++)
		{
			for(int j=0;j<a.getWidth();j++)
			{
				double delta = a.getData(i, j) - b.getData(i, j);
				//delta = delta/a.getData(i, j);
				if(delta<0)delta *= -1;
				if(delta>0.01)
				{
					System.out.printf("[%d,%d] %f <==> %f | %f\n",i,j,a.getData(i, j),b.getData(i, j),delta);
					return false;
				}
			}
		}
		System.out.println("OK");
		return true;
	}
	
	private boolean testHeteroLayer() throws Exception
	{
		HeteroLayerConfig config = new HeteroLayerConfig();
		HeteroLayerConfigElement e =  config.new HeteroLayerConfigElement();
		e.startIndex=0;
		e.endIndex=3;
		e.baypass=true;
		config.configElements.add(e);
		e =  config.new HeteroLayerConfigElement();
		e.startIndex=4;
		e.endIndex=7;
		e.numOfFilters=2;
		config.configElements.add(e);
		e =  config.new HeteroLayerConfigElement();
		e.startIndex=8;
		e.endIndex=9;
		e.numOfFilters=2;
		config.configElements.add(e);
		
		HeteroLayer layer = new HeteroLayer(config);
		Matrix input = Matrix.create(1, 10, this.matrixType);
		Matrix output = Matrix.create(1, config.getTotalOutputWidth(), this.matrixType);
		
		layer.input = input;
		layer.output = output;
		
		layer.createInnerLayerStructure();
		layer.setInitParam(new Random(0));
		
		input.setGuassianRandomValue(123);
		
		layer.doForward();
		System.out.println("input:"+input);
		
		Matrix compare = output.copy(false);
		Matrix filter = layer.getFilter(1);
		Matrix tmp = Matrix.create(1, 2, this.matrixType);
		Matrix sub = DZHelper.getSlice(input, 0, input.getHeight(), 4, 8);
		MatrixOperator op = input.getMatrixOperator();
		op.dot(sub, filter, tmp);
		compare.setData(tmp.getData(0, 0),0,4);
		compare.setData(tmp.getData(0, 1),0,5);
		sub.release();
		filter = layer.getFilter(2);
		sub = DZHelper.getSlice(input, 0, input.getHeight(), 8, 10);
		op.dot(sub, filter, tmp);
		compare.setData(tmp.getData(0, 0),0,6);
		compare.setData(tmp.getData(0, 1),0,7);
		sub.release();
		
		compare.setData(input.getData(0, 0),0,0);
		compare.setData(input.getData(0, 1),0,1);
		compare.setData(input.getData(0, 2),0,2);
		compare.setData(input.getData(0, 3),0,3);

		System.out.println("output:"+output);
		System.out.println("compare:"+compare);

		boolean ret = false;
		if(checkMatrix(output, compare)==false)
		{
			System.out.println("fail in forward test for HeteroLayer");
			ret = true;
		}
		
		tmp.release();
		input.release();
		output.release();
		layer.release();
		
		return ret;
	}
	
	
	
	private boolean testHeteroLayerBackPropagation() throws Exception
	{
		boolean ret = false;
		HeteroLayerConfig config = new HeteroLayerConfig();
		HeteroLayerConfigElement e =  config.new HeteroLayerConfigElement();
		e.startIndex=0;
		e.endIndex=3;
		e.baypass=true;
		config.configElements.add(e);
		e =  config.new HeteroLayerConfigElement();
		e.startIndex=4;
		e.endIndex=7;
		e.numOfFilters=2;
		config.configElements.add(e);
		e =  config.new HeteroLayerConfigElement();
		e.startIndex=8;
		e.endIndex=9;
		e.numOfFilters=2;
		config.configElements.add(e);
		
		DZConfig dzConfig = DZConfig.getStaticParameter();
		dzConfig.setHeteroLayerConfig(config);
		
		Matrix input = Matrix.create(1, 10, this.matrixType);
		LayerType lt = new LayerType();
		lt.layerType = LayerType.LAYER_TYPE_HETERO;
		final HeteroLayer layer = (HeteroLayer)Layer.makeFirst(input, config.getTotalOutputWidth(), "hetero_l_first", dzConfig, lt);
				
		layer.setInitParam(new Random(0));
		input.setGuassianRandomValue(123);
				
		Matrix rightAnswer = Matrix.create(input.getHeight(), 5, this.matrixType);
		for(int i=0;i<rightAnswer.getHeight();i++)rightAnswer.setData(1.0,i,i);
		LayerType layerType = new LayerType();
		layerType.layerType = LayerType.LAYER_TYPE_HOMO;
		layerType.layerLocation = LayerType.LAYER_LOCATION_LAST;
		final Layer lastLayer = Layer.makeAndConnectLast(layer, rightAnswer, "layerLayer", layerType);
		lastLayer.setInitParam(new Random(0));
				
		layer.doForward();
		lastLayer.doForward();
		
		System.out.println("CEE:"+lastLayer.getErrorValue().value);
		
		layer.getGradBySGD(new GenericCallback() {
			
			@Override
			public Object doGenericCallback(Object o) {
				// TODO Auto-generated method stub
				try {
					layer.doForward();
					lastLayer.doForward();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}, lastLayer.getErrorValue());


		System.out.println("bais grad:"+layer.biasGrad);

		for(int i=0;i<layer.getHeteroLayerConfig().configElements.size();i++)
		{
			Matrix filterGrad = layer.getFilterGrad(i);
			if(filterGrad!=null)
			{
				System.out.println("filterGrad at "+i+" "+filterGrad);
				filterGrad.setData(0.0);
			}
		}
		
		layer.biasGrad.setData(0.0);
		
		//clear for SGD
		layer.doForward();
		lastLayer.doForward();
		
		lastLayer.backpropagation();
		layer.backpropagation();

		System.out.println("bais grad:"+layer.biasGrad);
		
		for(int i=0;i<layer.getHeteroLayerConfig().configElements.size();i++)
		{
			Matrix filterGrad = layer.getFilterGrad(i);
			if(filterGrad!=null)
			{
				System.out.println("filterGrad at "+i+" "+filterGrad);
			}
		}
		
		
		input.release();
		layer.release();
		lastLayer.release();
		
		return ret;
	}
	
	
	@Override
	public boolean doTest() {
		// TODO Auto-generated method stub
		
		try {
			boolean failed=false;
			//failed |= this.testHeteroLayer();
			failed |= testHeteroLayerBackPropagation();
			return failed;
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Layer Test";
	}
	/*

	
	if(dz2layer)
	
	return false;
	*/

	@Override
	public void release() {
		// TODO Auto-generated method stub
		System.gc();
	}
}
