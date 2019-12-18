package dolziplib.matrix;

import java.util.LinkedList;

public class MatrixAVX implements Matrix{
	
	public static LinkedList<Long> avxMatrixResource = new LinkedList<Long>();
	
	String name = "no_name";
	
	long avxAllocAddress=0;
	int width=0;
	int height=0;
	boolean flipped = false;
	static
	{
		System.load(System.getProperty("user.dir")+"/avx_clib.so");
	}
	
	public boolean isFlipped()
	{
		return this.flipped;
	}
	
	//do not call directly init. use initAvxMatrix
	private native long init(int height, int width);
	//do not call directly deinit. use deinitAvxMatrix
	private native void deinit(long addr);
	
	private native long flip(long addr);
	private native long unflip(long addr);
	private native float getdata(long addr, int row, int column);
	private native float setdata(long addr, int row, int column, float d);
	
	private long initAvxMatrix(int height, int width)
	{
		long addr = init(height,width);
		if(addr!=0)
		{
			Long l = new Long(addr);
			synchronized (avxMatrixResource) {
				//System.out.println("alloc res:"+l.longValue());
				avxMatrixResource.add(l);
			}
		}
		return addr;
	}
	private void deinitAvxMatrix(long addr)
	{
		LinkedList<Long> shouldRelease = new LinkedList<>();
		//System.out.print("before avxrn:"+avxMatrixResource.size());
		synchronized (avxMatrixResource) {
			for(Long l:avxMatrixResource)
			{
				if(l.longValue() == addr)
				{
					shouldRelease.add(l);
				}
			}
			for(Long l:shouldRelease)
			{
				avxMatrixResource.remove(l);
			}
		}
		//System.out.println("after avxrn:"+avxMatrixResource.size());
		for(Long l:shouldRelease)
		{
			//System.out.printf("dealloc res 0x%x\n",l.longValue());
			//System.out.flush();
			deinit(l.longValue());
		}
		
	}
	public static void releaseAllAvxMatrixResource()
	{
		MatrixAVX m = new MatrixAVX();
		Long l[] = new Long[0];
		synchronized (avxMatrixResource) {
			l = avxMatrixResource.toArray(new Long[0]);
			avxMatrixResource.clear();
		}
		for(int i=0;i<l.length;i++)
		{
			m.deinit(l[i].longValue());
		}
	}
	
	public MatrixAVX()
	{
		
	}
	
	public MatrixAVX(int r, int c)
	{
		this.setSize(r, c);
	}
	
	boolean released=false;
	
    @Override
    protected void finalize() throws Throwable{
    	super.finalize();
    	//System.out.println("Matrix AVX finalize");
    	this.release();
    }
	
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return this.width;
	}
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return this.height;
	}
	@Override
	public void setSize(int row, int column) {
		// TODO Auto-generated method stub
		
		//System.out.println("AVX matrix creation : "+row+",column:"+column);
		
		if(this.avxAllocAddress!=0)
		{
			this.deinitAvxMatrix(this.avxAllocAddress);
			this.avxAllocAddress = 0;
		}
		
		this.width = column;
		this.height = row;
		
		this.avxAllocAddress = this.initAvxMatrix(row, column);
		if(this.avxAllocAddress==0)
		{
			System.out.println("fail to create avx matrix!!!!");
		}
		this.durtyCheck();
	}

	@Override
	public double getData(int row, int column) {
		// TODO Auto-generated method stub
		
		if(released)throw new RuntimeException("current matrix was released.");
		if(this.height <= row || this.width <=column)
		{
			throw new RuntimeException("range over ("+this.height+","+this.width+") <= "+row+","+column);			
		}
		
		return this.getdata(this.avxAllocAddress, row, column);
	}
	
	@Override
	public void setData(double data, int row, int column){
		// TODO Auto-generated method stub
		
		if(released)throw new RuntimeException("current matrix was released.");
		if(this.height <= row || this.width <=column)
		{
			throw new RuntimeException("range over ("+this.height+","+this.width+") <= "+row+","+column);			
		}
		
		this.setdata(this.avxAllocAddress, row, column, (float)data);
	}

	@Override
	public Matrix copy(boolean withContents) {
		// TODO Auto-generated method stub
		
		if(released)throw new RuntimeException("current matrix was released.");
		
		MatrixAVX m = new MatrixAVX(this.height, this.width);
		try {
			if(withContents)
			{
				if(this.flipped)
				{
					m.setData(this.getAllData());					
				}
				else
				{
					MatrixOperator op = m.getMatrixOperator();
					op.add(this, 0.0 , m);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return m;
	}

	
	@Override
	public MatrixOperator getMatrixOperator() {
		// TODO Auto-generated method stub
		return new MatrixOperatorAVX();
	}
	@Override
	public String getMatrixTypeName() {
		// TODO Auto-generated method stub
		return "AVX";
	}
	@Override
	public int getMatrixType() {
		// TODO Auto-generated method stub
		return Matrix.MATRIX_TYPE_AVX;
	}
	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(avxAllocAddress!=0)this.deinitAvxMatrix(this.avxAllocAddress);
		this.avxAllocAddress = 0;
		this.released = true;
	}
	
	public String toString()
	{
		return Matrix.printMatrix(this);
	}
	
	public void flip()
	{
		if(this.flipped)return;
		long newone = flip(this.avxAllocAddress);
		Long no = new Long(newone);
		synchronized (avxMatrixResource) {
			avxMatrixResource.add(no);
		}
		this.deinitAvxMatrix(this.avxAllocAddress);
		this.avxAllocAddress = newone;
		this.flipped = true;
	}
	public void unflip()
	{
		if(this.flipped==false)return;
		long newone = unflip(this.avxAllocAddress);
		Long no = new Long(newone);
		synchronized (avxMatrixResource) {
			avxMatrixResource.add(no);
		}
		this.deinitAvxMatrix(this.avxAllocAddress);
		this.avxAllocAddress = newone;
		this.flipped = false;
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

	@Override
	public void durtyCheck()
	{
		if(flipped)
		{
			int widthDummySize = 8 -  this.getHeight()%8;
			for(int i=0;i<this.getWidth();i++)
			{
				for(int j=this.getHeight();j<(this.getHeight()+widthDummySize);j++)
				{
					double v = this.getdata(this.avxAllocAddress, i, j);
					if(v!=0)
					{
						throw new RuntimeException("durty data at "+i+","+j+" v:"+v);
					}
				}
			}
		}
		else
		{
			int widthDummySize = 8 -  this.getWidth()%8;
			if(widthDummySize==8)
			{
				//there is no dummy.
				return;
			}
			for(int i=0;i<this.getHeight();i++)
			{
				for(int j=this.getWidth();j<(this.getWidth()+widthDummySize);j++)
				{
					double v = this.getdata(this.avxAllocAddress, i, j);
					if(v!=0)
					{
						//System.out.println("durty data at "+i+","+j+" v:"+v);
						throw new RuntimeException("durty data at "+i+","+j+" v:"+v+" width:"+this.getWidth()+",widthDummy:"+widthDummySize);
					}
				}
			}
		}
	}
}
