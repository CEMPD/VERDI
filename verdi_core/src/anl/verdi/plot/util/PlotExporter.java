package anl.verdi.plot.util;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import anl.verdi.plot.gui.FastTilePlot;
import anl.verdi.plot.gui.MeshPlot;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.io.TIFConvertImage;
import anl.verdi.plot.types.EPSExporter;
import anl.verdi.util.Utilities;
/**
 * Saves snapshots of plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotExporter {

	public final static String JPEG = "jpeg";
	public final static String JPG = "jpg";
	public final static String TIFF = "tiff";
	public final static String TIF = "tif";
	public final static String PNG = "png";
	public final static String BMP = "bmp";
	public final static String EPS = "eps";
	public final static String SHP = "shp";
	public final static String ASC = "asc";

	private Plot plot;
	private String currentExt = "png";
	private static File previousFolder;

	private class ImageFileFilter extends FileFilter {

		private String description;
		private Set<String> imageExt = new HashSet<String>();
		private String ext;

		public ImageFileFilter(String description, String... exts) {
			this.description = description;
			ext = exts[0];
			for (String str : exts) {
				imageExt.add(str);
			}
		}

		public String getDescription() {
			return description;
		}

		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			String ext = findExtension(f);
			return ext != null && imageExt.contains(ext);
		}

		public String getExtension() {
			return ext;
		}
	}

	public PlotExporter(Plot plot) {
		this.plot = plot;
	}

	/**
	 * Run the exporter. This will show a file chooser with
	 * the current export formats as a file filters and
	 * save a snapshot of the plot to those files.
	 *
	 * @throws IOException if there is an error while creating
	 * the image or saving the plot.
	 */
	public void run() throws IOException {
		JFileChooser chooser = (previousFolder != null ? new JFileChooser(previousFolder) : new JFileChooser());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(new ImageFileFilter("BMP Image (*.bmp)", BMP));
		chooser.addChoosableFileFilter(new ImageFileFilter("JPEG Image (*.jpg, *.jpeg)", JPG, JPEG));
		chooser.addChoosableFileFilter(new ImageFileFilter("TIFF Image (*.tif, *.tiff)", TIF, TIFF));
		chooser.addChoosableFileFilter(new ImageFileFilter("EPS Image (*.eps)", EPS));
		final FileFilter pngFileFilter = new ImageFileFilter("PNG Image (*.png)", PNG);
		chooser.addChoosableFileFilter(pngFileFilter);
		chooser.addChoosableFileFilter(new ImageFileFilter("Shapefile (*.shp, *.shx, *.dbf)", SHP));
		chooser.addChoosableFileFilter(new ImageFileFilter("ASCII Grid (*.asc)", ASC));
		chooser.setFileFilter(pngFileFilter);

		chooser.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
					currentExt = ((ImageFileFilter)evt.getNewValue()).getExtension();
				}
			}
		});

		int res = chooser.showSaveDialog(plot.getPanel());
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			save(file);
			previousFolder = file.getParentFile();
		}
		currentExt = "png";
	}

	/**
	 * Saves an image of the plot in the specified format to the specified file.
	 *
	 * @param format the format of the image file. One of PlotExporter.JPG, PlotExporter.TIF,
	 * PlotExporter.PNG, or PlotExporter.BMP
	 *
	 * @param file the file to save the image to
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @throws IOException if there is an error while saving the image
	 */
	public void save(String format, File file, int width, int height) throws IOException {
		BufferedImage image = plot.getBufferedImage(width, height);
		ImageIO.write(image, format, file);
	}
	
	private void save(File file) throws IOException {
		String ext = findExtension(file);

		if (ext == null || !ext.equals(currentExt)) {
			file = new File(file.getAbsolutePath() + "." + currentExt);
		}

		if( plot instanceof MeshPlot && 
				(currentExt.equals(SHP) || 
						currentExt.equals(EPS) ||
						currentExt.equals( ASC) ) ){
			String filename = file.getAbsolutePath();
			int extPos = filename.indexOf("." + currentExt);

			if (extPos > 0)
				filename = filename.substring(0, extPos);
			if (currentExt.equalsIgnoreCase(SHP))
				((MeshPlot)plot).exportShapeFile(filename);
			else if (currentExt.equalsIgnoreCase(EPS))
				((MeshPlot)plot).exportEPSImage(filename);
			else if (currentExt.equalsIgnoreCase(ASC))
				((MeshPlot)plot).exportASCIIGrid(filename);
		}
		
		if ( plot instanceof FastTilePlot &&
				( currentExt.equalsIgnoreCase(EPS) ||
				  currentExt.equals( SHP )  ||  
				  currentExt.equals( ASC ) ) ) {
			String filename = file.getAbsolutePath();
			int extPos = filename.indexOf("." + currentExt);

			if (extPos > 0)
				filename = filename.substring(0, extPos);

			if ( currentExt.equalsIgnoreCase(EPS) ) {
				((FastTilePlot)plot).exportEPSImage(filename);
			} else if ( currentExt.equals( SHP ) ) {
				((FastTilePlot)plot).exportShapefile(filename);
			} else {
				((FastTilePlot)plot).exportASCIIGrid(filename);				
			}

			return;
		}
		
		if ( plot instanceof EPSExporter &&
				 currentExt.equalsIgnoreCase(EPS)) {
			String filename = file.getAbsolutePath();
			int extPos = filename.indexOf("." + currentExt);

			if (extPos > 0)
				filename = filename.substring(0, extPos);

			if ( currentExt.equalsIgnoreCase(EPS) ) {
				((EPSExporter)plot).exportEPSImage(filename);
			}
			return;
		}

		
		if (currentExt.equalsIgnoreCase(TIFF) || currentExt.equalsIgnoreCase(TIF)) {
			if ( !Utilities.is64bitWindows()) {
				BufferedImage image = plot.getBufferedImage();
				TIFConvertImage.convert(image, file.getAbsolutePath());
				return;
			}
		}
		

		BufferedImage image = plot.getBufferedImage();
		ImageIO.write(image, currentExt, file);
	}

	private String findExtension(File f) {
//		String ext = null;
//		String s = f.getName();
//		int i = s.lastIndexOf('.');
//
//		if (i > 0 && i < s.length() - 1) {
//			ext = s.substring(i + 1).toLowerCase();
//		}
		String fileName = f.toString();
		String ext = new String(FilenameUtils.getExtension(fileName)).toLowerCase(); // 2015  NullPointerException thrown when first going to specify file name for exporting graphics
		return ext;
	}
	
	/**This function only to be used from command line scripts*/
	public void saveFromCommandLine(File file)
	{
		String ext = findExtension(file);
		
		if(	 ext.equalsIgnoreCase(JPEG) ||
			  ext.equalsIgnoreCase(JPG) ||
			  ext.equalsIgnoreCase(TIFF) ||
			  ext.equalsIgnoreCase(TIF) ||
			  ext.equalsIgnoreCase(PNG) ||
			  ext.equalsIgnoreCase(BMP) ||
			  ext.equalsIgnoreCase(EPS) ||
			  ext.equalsIgnoreCase(SHP) ||
			  ext.equalsIgnoreCase(ASC) )
		{
			currentExt = ext;
		}
		
		try{
			save(file);
		}catch(IOException e){}
		
		currentExt = "png";
	}
}
