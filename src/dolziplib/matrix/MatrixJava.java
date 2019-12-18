package dolziplib.matrix;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;


public class MatrixJava implements Matrix{
		
	int row;
	int columns;
	
	double[][] data;
	String name = "no_name";

	
	public MatrixJava(int row, int columns)
	{		
		this.setSize(row, columns);
	}
	

	@Override
	public double[] getRow(int index) {
		// TODO Auto-generated method stub
		return data[index];
	}
	@Override
	public double getData(int row, int column) {
		// TODO Auto-generated method stub
		return data[row][column];
	}
	
	@Override
	public double[][] getAllData() {
		// TODO Auto-generated method stub
		return data;
	}

	
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return columns;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return row;
	}
	

	@Override
	public void setData(double data, int row, int column){
		// TODO Auto-generated method stub
		
		if(this.row <= row || this.columns <=column)
		{
			throw new RuntimeException("range over ("+this.row+","+this.columns+") <= "+row+","+column);			
		}
		this.data[row][column] = data;
	}
	

	@Override
	public Matrix copy(boolean withContents) {
		
		if(released)throw new RuntimeException("current matrix was released.");
		
		// TODO Auto-generated method stub
		MatrixJava newOne = new MatrixJava(this.row, this.columns);
		try {
			if(withContents)newOne.setData(this.data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return newOne;
	}
	
	public String toString()
	{
		return Matrix.printMatrix(this);
	}
	

	@Override
	public MatrixOperator getMatrixOperator() {
		// TODO Auto-generated method stub
		MatrixOperatorJava op = new MatrixOperatorJava();
		return op;
	}

	@Override
	public void setSize(int row, int column) {
		// TODO Auto-generated method stub
		this.row = row;
		this.columns = column;
		try {
		this.data = new double[row][column];
		}catch(java.lang.OutOfMemoryError ex)
		{
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			
			System.out.println("free memory:"+freeMemory/(1024*1024));
			System.out.println("allocated memory:"+allocatedMemory/(1024*1024));
			System.out.println("max memory:"+maxMemory/(1024*1024));
			System.out.println("total memory:"+(freeMemory + (maxMemory - allocatedMemory)/(1024*1024)));
				
			ex.printStackTrace();
		}
	}

	@Override
	public String getMatrixTypeName() {
		// TODO Auto-generated method stub
		return "JAVA";
	}

	@Override
	public int getMatrixType() {
		// TODO Auto-generated method stub
		return Matrix.MATRIX_TYPE_JAVA;
	}

	boolean released = false;
	@Override
	public void release() {
		// TODO Auto-generated method stub
		this.released = true;
	}


	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public boolean isReleased() {
		// TODO Auto-generated method stub
		return released;
	}


}
