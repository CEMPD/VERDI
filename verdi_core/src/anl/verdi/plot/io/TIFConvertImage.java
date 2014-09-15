/* ****************************************************************************
 * Author..:Pedro J Rivera
 * ****************************************************************************
 * 
 * Purpose.:Utility class to convert images to TIF images
 *          
 * ****************************************************************************
 */

package anl.verdi.plot.io;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Utility class to convert images to TIF images
 * 
 * @author Pedro J Rivera
 * 
 */
public class TIFConvertImage extends TIFConvert {

	/**
	 * Convert an image to a TIF and save as file
	 * @param image
	 * @param tif
	 * @return
	 * @throws IOException 
	 */
	public static void convert(String image, String tif) throws IOException {
		convert(image, tif, DEFAULT_DPI, DEFAULT_COLOR, DEFAULT_COMPRESSION, DEFAULT_COMPRESSION);
	}
	
	/**
	 * Convert an image to a TIF and save as file
	 * @param image
	 * @param tif
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException 
	 */
	public static void convert(String image, String tif, int dpi, int color, int compression, float quality) throws IOException {
		convert(getImageFromImage(image, dpi, color, compression), tif, dpi, compression, quality);
	}

	/**
	 * Convert an image to a TIF and return as byte array
	 * @param image
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException 
	 */
	public static byte[] convert(String image, int dpi, int color, int compression, float quality) throws IOException {
		return convert(getImageFromImage(image, dpi, color, compression), dpi, compression, quality);
	}
	
	/**
	 * Convert a byte array image type to a TIF and save to file
	 * @param image
	 * @param tif
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException 
	 */
	public static void convert(byte[] image, String tif, int dpi, int color, int compression, float quality) throws IOException {
		convert(getImageFromImage(image, dpi, color, compression), tif, dpi, compression, quality);
	}
	
	/**
	 * Convert a byte array image type to a TIF and return as byte array
	 * @param image
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException 
	 */
	public static byte[] convert(byte[] image, int dpi, int color, int compression, float quality) throws IOException {
		return convert(getImageFromImage(image, dpi, color, compression), dpi, compression, quality);		
	}

	/**
	 * Convert a buffered image type to a TIF and return as byte array
	 * @param image
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException
	 */
	public static byte[] convert(BufferedImage image, int dpi, int color, int compression, float quality) throws IOException {
		return convert(getImageFromImage(image, dpi, color, compression), dpi, compression, quality);		
	}
	
	/**
	 * Convert a buffered image type to a TIF and save to file
	 * @param image
	 * @param tif
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @throws IOException
	 */
	public static void convert(BufferedImage image, String tif, int dpi, int color, int compression, float quality) throws IOException {
		convert(getImageFromImage(image, dpi, color, compression), tif, dpi, compression, quality);
	}
	
	/**
	 * Convert a buffered image type to a TIF and save to file
	 * @param image
	 * @param tif
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @throws IOException
	 */
	public static void convert(BufferedImage image, String tif) throws IOException {
		convert(getImageFromImage(image, TIFConvert.DEFAULT_DPI, TIFConvert.DEFAULT_COLOR, TIFConvert.DEFAULT_COMPRESSION), tif, TIFConvert.DEFAULT_DPI, TIFConvert.DEFAULT_COMPRESSION, TIFConvert.DEFAULT_COMPRESSION_QUALITY);
	}
	
	/**
	 * Convert a byte array image into a TIF buffered image
	 * @param img
	 * @param dpi
	 * @param color
	 * @param compression
	 * @param quality
	 * @return
	 * @throws IOException 
	 */
	private static BufferedImage[] getImageFromImage(byte[] img, int dpi, int color, int compression) throws IOException {
		return getImageFromImage(ImageIO.read(new ByteArrayInputStream(img)), dpi, color, compression);
	}
	
	/**
	 * Convert an image into a TIF buffered image
	 * @param img
	 * @param dpi
	 * @param color
	 * @param compression
	 * @return
	 * @throws IOException 
	 */
	private static BufferedImage[] getImageFromImage(String img, int dpi, int color, int compression) throws IOException {
		return getImageFromImage(ImageIO.read(new File(img)), dpi, color, compression);
	}

	/**
	 * Convert a buffered image into a TIF buffered image
	 * @param img
	 * @param dpi
	 * @param color
	 * @param compression
	 * @return
	 */
	private static BufferedImage[] getImageFromImage(BufferedImage img, int dpi, int color, int compression) {
		BufferedImage image[] = new BufferedImage[1];		
		image[0] = getBufferedImage(color, compression, img.getWidth(), img.getHeight());			
		Graphics2D g = (Graphics2D)image[0].getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return image;
	}

}
