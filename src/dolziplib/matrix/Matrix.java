package dolziplib.matrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;


public interface Matrix {
	public int getWidth();
	public int getHeight();
	
	public void setSize(int row, int column);
	public void setName(String name);
	public String getName();
	
	public double getData(int row, int column);
	public default double[] getRow(int index)
	{
		double d[] = new double[this.getWidth()];
		for(int i=0;i<this.getWidth();i++)d[i] = this.getData(index, i);
		return d;
	}
	public default double[][] getAllData()
	{
		double d[][] = new double[this.getHeight()][this.getWidth()];
		for(int i=0;i<this.getHeight();i++)
		{
			for(int j=0;j<this.getWidth();j++)d[i][j] = this.getData(i, j);
		}
		return d;
	}
	
	public default void setStandardRandomValue(long seed)
	{
		try {
			Random r = new Random(seed);
			for(int i=0;i<this.getHeight();i++)
			{
				for(int j=0;j<this.getWidth();j++)this.setData(r.nextDouble(), i, j);
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	
	}
	public default void setGuassianRandomValue(long seed)
	{
		try {
			Random r = new Random(seed);
			for(int i=0;i<this.getHeight();i++)
			{
				for(int j=0;j<this.getWidth();j++)this.setData(r.nextGaussian(), i, j);
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	
	}
	
	public default void setData(double[][] data) throws Exception {
		// TODO Auto-generated method stub
		
		if(this.getHeight() != data.length)
		{
			throw new Exception("height is not matched "+this.getHeight()+" <=> "+data.length);
		}
		
		int w = this.getWidth();
		for(int i=0;i<data.length;i++)
		{
			if(w != data[i].length)
			{
				throw new Exception("All width is not matched");
			}
		}
		
		for(int i=0;i<this.getHeight();i++)
		{
			for(int j=0;j<this.getWidth();j++)
			{
				this.setData(data[i][j], i, j);
			}
		}	
	}
	
	
	public void setData(double data, int row, int column);
	public default void setData(Matrix matrix) {
		// TODO Auto-generated method stub
		this.setSize(matrix.getHeight(), matrix.getWidth());
		for(int i=0;i<this.getHeight();i++)
		{
			for(int j=0;j<this.getWidth();j++)
			{
				try {
					this.setData(matrix.getData(i, j),i,j);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	
	
	public default void setData(double[] data, int row)
	{
		// TODO Auto-generated method stub
		if(this.getWidth() != data.length)
		{
			throw new RuntimeException("Width is not matched. column:"+this.getWidth()+",length:"+data.length);
		}
		
		for(int i=0;i<data.length;i++)
		{
			this.setData(data[i], row, i);
		}
		
	}
	
	public default void setData(double data) {
		// TODO Auto-generated method stub
		for(int i=0;i<this.getHeight();i++)
		{
			for(int j=0;j<this.getWidth();j++)
			{
				try {
					this.setData(data,i, j);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public default Matrix copy()
	{
		return copy(true);
	}
	public Matrix copy(boolean withContents);
	
	
	public default double getSum() {
		// TODO Auto-generated method stub
		
		double sum = 0;
		for(int i=0;i<this.getHeight();i++)
		{
			for(int j=0;j<this.getWidth();j++)
			{
				sum += this.getData(i, j);
			}
		}	
		return sum;
	}
	
	public MatrixOperator getMatrixOperator();
	
	public String getMatrixTypeName();
	public int getMatrixType();
	
	public void release(); //internal allocated resource must be released in this method.
	public boolean isReleased();
		
	public default void saveToBinFile(String fileName) throws Exception
	{
		int w = this.getWidth();
		int h = this.getHeight();
		byte[] bBuffer = new byte[w*h*Double.BYTES + 2*Integer.BYTES]; //2 for width and height.
		ByteBuffer bb = ByteBuffer.wrap(bBuffer);
		
		bb.putInt(w);
		bb.putInt(h);
		double[][] data = this.getAllData();
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				bb.putDouble(data[i][j]);
			}
		}
		
		FileOutputStream fos = new FileOutputStream(fileName);
		
		fos.write(bBuffer);
		fos.close();
	}
	public default void loadFromBinFile(String fileName) throws Exception
	{
		File f = new File(fileName);
		byte[] buffer = new byte[(int)f.length()];
		FileInputStream fis = new FileInputStream(f);
		fis.read(buffer);
		fis.close();
		
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		int w = bb.getInt();
		int h = bb.getInt();
		
		double[][] data = new double[h][w];
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				data[i][j] = bb.getDouble();
			}
		}
		this.setData(data);
	}
	public static Matrix loadFromBinFile(String fileName, int matrixType) throws Exception
	{
		File f = new File(fileName);
		byte[] buffer = new byte[(int)f.length()];
		FileInputStream fis = new FileInputStream(f);
		fis.read(buffer);
		fis.close();
		
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		int w = bb.getInt();
		int h = bb.getInt();
		
		double[][] data = new double[h][w];
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				data[i][j] = bb.getDouble();
			}
		}
		Matrix matrix = Matrix.create(h, w, matrixType);
		matrix.setData(data);
		return matrix;
	}
	
	public static final int MATRIX_TYPE_COLT = 1;
	public static final int MATRIX_TYPE_JAVA = 2;
	public static final int MATRIX_TYPE_AVX = 3;
	
	public static Matrix create(int row,int column, int type)
	{
		if(type==MATRIX_TYPE_COLT)
		{
			MatrixColt colt = new MatrixColt(row, column);
			return colt;
		}
		else if(type==MATRIX_TYPE_JAVA)
		{
			MatrixJava java = new MatrixJava(row, column);
			return java;
		}
		else if(type==MATRIX_TYPE_AVX)
		{
			MatrixAVX java = new MatrixAVX(row, column);
			return java;
		}
		return null;
	}
	
	public default int getMaxIndex(int row)
	{
		double[] rowD = this.getRow(row);
		double max = Double.NaN;
		int index = -1;
		for(int i=0;i<rowD.length;i++)
		{
			if(i==0)
			{
				max = rowD[i];
				index = 0;
			}
			else
			{
				if(max<rowD[i])
				{
					max=rowD[i];
					index = i;
				}
			}
		}
		return index;
		
	}
	
	public static String printMatrix(Matrix m)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(""+m.getHeight()+" x "+m.getWidth()+" matrix\n");
		for(int i=0;i<m.getHeight();i++)
		{
			for(int j=0;j<m.getWidth();j++)
			{
				if(j>30)
				{
					sb.append("...");
					break;
				}
				sb.append(String.format("%.8f",m.getData(i,j)));
				sb.append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public default String getSizeString()
	{
		return this.getName()+" "+this.getHeight()+" x "+this.getWidth();
	}
	
	//MatrixAVX를 위해서 만들어졌음.
	public default void durtyCheck()
	{
		
	}
}

