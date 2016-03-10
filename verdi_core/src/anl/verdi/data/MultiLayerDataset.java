package anl.verdi.data;

public interface MultiLayerDataset {
	
	public CoordAxis getZAxis(String variable);
	
	public CoordAxis getDefaultZAxis();

}
