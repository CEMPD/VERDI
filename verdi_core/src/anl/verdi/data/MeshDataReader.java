package anl.verdi.data;

public class MeshDataReader {
	
	ArrayReader variable;
	DataFrame frame;
	MPASDataFrameIndex index;
	int timestep;
	int layer;
	
	public MeshDataReader(ArrayReader renderVariable, DataFrame dataFrame, MPASDataFrameIndex dataIndex, int dataStep, int dataLayer) {
		variable = renderVariable;
		frame = dataFrame;
		index = dataIndex;
		timestep = dataStep;
		layer = dataLayer;
	}
	
	public ArrayReader getArrayReader() {
		return variable;
	}
	
	public DataFrame getDataFrame() {
		return frame;
	}
	
	public MPASDataFrameIndex getDataIndex() {
		return index;
	}
	
	public int getTimestep() {
		return timestep;
	}

	public int getLayer() {
		return layer;
	}
	
	public void setTimestep(int step) {
		timestep = step;
	}
	
	public void setLayer(int num) {
		layer = num;
	}
}
