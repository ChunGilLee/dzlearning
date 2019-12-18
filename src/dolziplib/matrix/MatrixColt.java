package dolziplib.matrix;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;


public class MatrixColt implements Matrix{
		
	int row;
	int columns;
	DoubleMatrix2D coltMatrix = null;
	String name = "no_name";

	
	public MatrixColt(int row, int columns)
	{		
		this.setSize(row, columns);
	}
	
	public MatrixColt(int row, int columns, DoubleMatrix2D coltMatrix)
	{
		this.row = row;
		this.columns = columns;
		this.coltMatrix = coltMatrix;
	}
	@Override
	public double[] getRow(int index) {
		// TODO Auto-generated method stub
		double[][] data = this.coltMatrix.toArray();
		return data[index];
	}
	@Override
	public double getData(int row, int column) {
		// TODO Auto-generated method stub
		return coltMatrix.get(row, column);
	}
	
	@Override
	public double[][] getAllData() {
		// TODO Auto-generated method stub
		double[][] data = this.coltMatrix.toArray();
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
	public void setData(Matrix matrix) {
		// TODO Auto-generated method stub
		this.row = matrix.getHeight();
		this.columns = matrix.getWidth();
		this.coltMatrix =DoubleFactory2D.dense.make(matrix.getAllData());
	}

	@Override
	public void setData(double[] data, int row)
	{
		// TODO Auto-generated method stub
		if(this.columns != data.length)
		{
			throw new RuntimeException("Width is not matched. column:"+this.columns+",length:"+data.length);
		}
		
		for(int i=0;i<data.length;i++)
		{
			this.coltMatrix.set(row, i, data[i]);
		}
		
	}

	@Override
	public void setData(double data, int row, int column)
	{
		// TODO Auto-generated method stub
		
		if(this.row <= row || this.columns <=column)
		{
			throw new RuntimeException("range over");			
		}
		this.coltMatrix.set(row, column, data);
	}
	
	
	@Override
	public void setData(double[][] data)
	{
		// TODO Auto-generated method stub
		
		if(this.getHeight() != data.length)
		{
			throw new RuntimeException("height not matched");			
		}
		
		for(int i=0;i<data.length;i++)
		{
			if(this.getWidth() != data[i].length)
			{
				throw new RuntimeException("width not matched");							
			}
		}
		
		this.coltMatrix.assign(data);
	}

	@Override
	public Matrix copy(boolean withContents) {
		// TODO Auto-generated method stub
		
		if(released)throw new RuntimeException("current matrix was released.");
		
		MatrixColt newOne = new MatrixColt(this.row, this.columns);
		if(withContents)newOne.coltMatrix = this.coltMatrix.copy();
		return newOne;
	}
	
	public String toString()
	{
		return Matrix.printMatrix(this);
	}
	



	@Override
	public double getSum() {
		// TODO Auto-generated method stub
		return this.coltMatrix.zSum();
	}

	@Override
	public MatrixOperator getMatrixOperator() {
		// TODO Auto-generated method stub
		MatrixOperatorColt op = new MatrixOperatorColt();
		return op;
	}

	@Override
	public void setSize(int row, int column) {
		// TODO Auto-generated method stub
		this.row = row;
		this.columns = column;
		coltMatrix = DoubleFactory2D.dense.make(row, column);
	}

	@Override
	public String getMatrixTypeName() {
		// TODO Auto-generated method stub
		return "Colt";
	}

	@Override
	public int getMatrixType() {
		// TODO Auto-generated method stub
		return Matrix.MATRIX_TYPE_COLT;
	}

	@Override
	public void setData(double data) {
		// TODO Auto-generated method stub
		this.coltMatrix.assign(data);
	}

	boolean released = false;
	@Override
	public void release() {
		// TODO Auto-generated method stub
		this.released=true;
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
