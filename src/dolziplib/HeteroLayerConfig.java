package dolziplib;

import java.util.LinkedList;

import com.google.gson.JsonObject;

import dolziplib.matrix.Matrix;

public class HeteroLayerConfig {
	public class HeteroLayerConfigElement
	{
		public int startIndex;       //including
		public int endIndex;		 //including
		public boolean baypass = false;
		public int numOfFilters;
		private int outputWidth; // == num of filters
		public int getOutputWidth()
		{
			if(baypass)return endIndex-startIndex+1;
			else return numOfFilters;
		}
		
		public JsonObject getJson()
		{
			JsonObject jobj = new JsonObject();
			jobj.addProperty("startIndex", this.startIndex);
			jobj.addProperty("endIndex", this.endIndex);
			jobj.addProperty("bypass", this.baypass);
			jobj.addProperty("numOfFilters", this.numOfFilters);
			jobj.addProperty("outputWidth", this.outputWidth);
			return jobj;
		}
		public void setJson(JsonObject jobj)
		{
			this.startIndex =  jobj.get("startIndex").getAsInt();
			this.endIndex =  jobj.get("endIndex").getAsInt();
			this.baypass =  jobj.get("bypass").getAsBoolean();
			this.numOfFilters =  jobj.get("numOfFilters").getAsInt();
			this.outputWidth =  jobj.get("outputWidth").getAsInt();
		}
	}
	
	public LinkedList<HeteroLayerConfigElement> configElements = new LinkedList<>();
	
	public void checkOverlap(int inputTotalWidth, int outputTotalWidth)
	{
		boolean[] map = new boolean[inputTotalWidth];
		for(int i=0;i<map.length;i++)map[i]=false;
		
		int outputMapIndex=0;
		int configElementCount=0;
		
		for(HeteroLayerConfigElement e:configElements)
		{
			if(e.startIndex > e.endIndex)throw new RuntimeException(
					String.format("[%d] startindex(%d) over endIndex(%d)",configElementCount,e.startIndex,e.endIndex));
			if(e.startIndex >= inputTotalWidth || e.endIndex >=inputTotalWidth)throw new RuntimeException(
					String.format("[%d] index(%d,%d) over totalWidth(%d)",configElementCount, e.startIndex,e.endIndex, outputTotalWidth));
			for(int i=e.startIndex;i<=e.endIndex;i++)
			{
				if(map[i])throw new RuntimeException(String.format("[%d] overlapped(%d,%d,%d)",configElementCount, e.startIndex,e.endIndex,i));
				map[i]=true;
			}
			
			
			if(outputMapIndex>=outputTotalWidth)throw new RuntimeException(String.format("[%d] outputWidth exceeds %d,%d",configElementCount, outputMapIndex,outputTotalWidth));
			outputMapIndex+=e.getOutputWidth();
			
			configElementCount++;
		}
		
		for(int i=0;i<map.length;i++)
		{
			if(map[i]==false)throw new RuntimeException(String.format("not packed at %d",i));
		}
		if(outputMapIndex!=outputTotalWidth)throw new RuntimeException(String.format("outputWidth shortage %d,%d",outputMapIndex,outputTotalWidth));
	}
	public int getTotalOutputWidth()
	{
		int w=0;
		for(HeteroLayerConfigElement e:configElements)
		{
			w += e.getOutputWidth();
		}
		return w;
	}
	
}
