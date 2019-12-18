package dolziplib.mnist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import dolziplib.matrix.Matrix;

public class Mnist {

	public LinkedList<MnistImg> imgs = new LinkedList<>();
	public LinkedList<Integer> originalIndex = new LinkedList<Integer>();
	
	public int getWidth()
	{
		return imgs.get(0).getMatrixWidth();
	}
	public int getAnswerWidth()
	{
		return imgs.get(0).getAnswerWidth();
	}
	
	public void getInputMatrix(int size,Matrix input) throws Exception
	{
		LinkedList<MnistImg> target = imgs;
		if(size<imgs.size())
		{
			target = new LinkedList<>();
			int c=0;
			for(MnistImg img:imgs)
			{
				target.add(img);
				c++;
				if(c==size)break;
			}
		}
		
		int index=0;
		for(MnistImg img:target)
		{
			input.setData(img.getDataNormalized1D(), index);
			index++;
		}
	}
	
	public Matrix getInputMatrix(int size,int matrixType)
	{
		try {
			int sizeR = size;
			if(sizeR>imgs.size())sizeR = imgs.size();
			
			Matrix input = Matrix.create(sizeR, MnistImg.WIDTH*MnistImg.HEIGHT, matrixType);
			
			getInputMatrix(size,input);
			
			return input;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	public Matrix getCorrectAnswerMatrix(int size, int matrixType)
	{
		try {
			int sizeR = size;
			if(sizeR>imgs.size())sizeR = imgs.size();
			
			Matrix correct = Matrix.create(sizeR, 10, matrixType);
			getCorrectAnswerMatrix(size,correct);

			return correct;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	public void getCorrectAnswerMatrix(int size, Matrix input) throws Exception
	{

			LinkedList<MnistImg> target = imgs;
			if(size<imgs.size())
			{
				target = new LinkedList<>();
				int c=0;
				for(MnistImg img:imgs)
				{
					target.add(img);
					c++;
					if(c==size)break;
				}
			}
			int index=0;
			for(MnistImg img:target)
			{
				//Matrix imgCorrect = img.getCorrectAnswerMatrix(matrixType);
				//correct.setData(imgCorrect.getRow(0), index);
				double[] imgCorrent = img.getCorrectAnswerMatrixByrArray();
				input.setData(imgCorrent,index);
				index++;
			}

	}
	
	private static MnistImg getImg(String data) throws Exception
	{
		MnistImg img = new MnistImg();
		String[] sp = data.split(",");
		img.label = Integer.parseInt(sp[0]);
		
		int row=0;
		int column=0;
		for(int i=1;i<sp.length;i++)
		{
			img.data[row][column] = Integer.parseInt(sp[i]);
			column++;
			if(column>=MnistImg.WIDTH)
			{
				row++;
				column=0;
			}
		}
		img.generateNormalized();
		return img;
	}
	
	public static Mnist getFromFile(String fileName)
	{
		try {
			
			Mnist mnist = new Mnist();
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			
			String line;
			int i=0;
			while((line = br.readLine()) != null)
			{
				MnistImg img = getImg(line);
				mnist.imgs.add(img);
				mnist.originalIndex.add(new Integer(i));
				i++;
			}
			
			br.close();
			return mnist;
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public void saveToBinFile(String fileName)
	{
		try {
			
			FileOutputStream fos = new FileOutputStream(fileName);
			
			ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
			bb.putInt(this.imgs.size());
			fos.write(bb.array());
			for(MnistImg img : imgs)
			{
				fos.write(img.getBinary().array());
			}
			fos.close();
			
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static Mnist getFromBinFile(String fileName)
	{
		try {
			
			File f = new File(fileName);
			byte[] buffer = new byte[(int)f.length()];
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			
			FileInputStream fis = new FileInputStream(fileName);
			fis.read(buffer);
			fis.close();
			
			Mnist mnist = new Mnist();
			
			int num = bb.getInt();
			for(int i=0;i<num;i++)
			{
				MnistImg img = MnistImg.loadFromBinary(bb);
				mnist.imgs.add(img);
				mnist.originalIndex.add(new Integer(i));
			}
			return mnist;
			
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
}
