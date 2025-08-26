package anl.verdi.plot.gui;

public class TileDownloader implements Runnable {
	
	  double[][] targetDomain;
	  int targetWidth;
	  int targetHeight;
	  AbstractPlotPanel imageListener;
	  
	  public TileDownloader(double[][] domain, int width, int height, AbstractPlotPanel listener) {
		  targetDomain = domain;
		  targetWidth = width;
		  targetHeight = height;
		  imageListener = listener;
	  }
	  
	  @Override
	  public void run() {
		String imagePath;
		try {
			//System.err.println("TileDownloader retrieving image");
			imagePath = VerdiTileUtil.getInstance().retrieveImage(targetDomain, targetWidth, targetHeight);
			//System.err.println("Got image " + imagePath);
			imageListener.setBackgroundImage(imagePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }

}
