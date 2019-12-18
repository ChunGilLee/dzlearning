package dolziplib.mnist;

import java.nio.ByteBuffer;

import dolziplib.matrix.Matrix;

public class MnistImg {

	public static final int WIDTH=28;
	public static final int HEIGHT=28;
	
	public int label;
	public int[][] data = new int[HEIGHT][WIDTH];
	public double[][] dataNormalized = new double[HEIGHT][WIDTH];
	
	public double[] getDataNormalized1D()
	{
		double[] ret = new double[WIDTH*HEIGHT];
		int index=0;
		for(int i=0;i<HEIGHT;i++)
		{
			for(int j=0;j<WIDTH;j++)
			{
				ret[index] = this.dataNormalized[i][j];
				index++;
			}
		}
		return ret;
	}
	
	public int getMatrixWidth()
	{
		return WIDTH*HEIGHT;
	}
	
	public Matrix getInputMatrix(int matrixType)
	{
		Matrix input = Matrix.create(1, MnistImg.WIDTH*MnistImg.HEIGHT, matrixType);
		try {
			getInputMatrix(matrixType,input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return input;
	}
	
	public void getInputMatrix(int matrixType,Matrix input) throws Exception
	{
		input.setData(this.getDataNormalized1D(), 0);
	}
	
	public void generateNormalized()
	{
		for(int i=0;i<HEIGHT;i++)
		{
			for(int j=0;j<WIDTH;j++)
			{
				double d = (double)this.data[i][j];
				this.dataNormalized[i][j] = d/255.0;
				if(this.dataNormalized[i][j]>1.0)this.dataNormalized[i][j] = 1.0;
			}
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		sb.append(""+WIDTH+" x "+HEIGHT+", label:"+label+"\n");
		
		for(int i=0;i<HEIGHT;i++)
		{
			for(int j=0;j<WIDTH;j++)
			{
				if(data[i][j]>0)
				{
					sb.append('X');
				}
				else sb.append('O');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public ByteBuffer getBinary()
	{
		ByteBuffer bb = ByteBuffer.allocate(HEIGHT*WIDTH*Integer.BYTES + Integer.BYTES);
		bb.putInt(this.label);
		for(int i=0;i<HEIGHT;i++)
		{
			for(int j=0;j<WIDTH;j++)
			{
				bb.putInt(data[i][j]);
			}
		}
		return bb;
	}
	public static MnistImg loadFromBinary(ByteBuffer bb)
	{
		MnistImg img = new MnistImg();
		img.label = bb.getInt();
		for(int i=0;i<HEIGHT;i++)
		{
			for(int j=0;j<WIDTH;j++)
			{
				img.data[i][j] = bb.getInt();
			}
		}
		img.generateNormalized();
		return img;
	}
	
	
	public Matrix getMatrix(int matrixType)
	{
		Matrix input = Matrix.create(HEIGHT, WIDTH, matrixType);
		try {
			input.setData(this.dataNormalized);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return input;
	}
	
	public Matrix getCorrectAnswerMatrix(int matrixType)
	{
		try {
			Matrix input = Matrix.create(1, 10, matrixType);
			for(int i=0;i<10;i++)input.setData(0.0, 0, i);
			input.setData(1.0, 0, this.label);
			return input;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void getCorrectAnswerMatrix(int matrixType, Matrix input)
	{
		try {
			for(int i=0;i<10;i++)input.setData(0.0, 0, i);
			input.setData(1.0, 0, this.label);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getAnswerWidth()
	{
		return 10;
	}
	
	public double[] getCorrectAnswerMatrixByrArray()
	{
		try {
			double[] ret = new double[10];
			for(int i=0;i<10;i++)ret[i] = 0.0;
			ret[this.label] = 1.0;
			return ret;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
