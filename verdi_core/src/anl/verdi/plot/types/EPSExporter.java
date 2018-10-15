package anl.verdi.plot.types;

public interface EPSExporter {
	
	public void exportEPSImage(String filename);
	
	public void exportEPSImage(String filename, int width, int height);

}
