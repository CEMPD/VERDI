package anl.verdi.data;

public interface MultiAxisDataset {
	
	public CoordAxis getTimeAxis(String variable);
	
	public CoordAxis getZAxis(String variable);
	
	public CoordAxis getDefaultZAxis();

}
