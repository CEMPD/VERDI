/* ****************************************************************************
 * Author..:Pedro J Rivera
 * ****************************************************************************
 * 
 * Purpose.:Abstract class for converting buffered images to TIF images
 *          with proper TIF tags set.
 *          
 * ****************************************************************************
 */
package anl.verdi.plot.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.stream.ImageOutputStream;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
//import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
//import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.media.imageio.stream.FileChannelImageOutputStream;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageMetadata;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;

/**
 * Abstract Class TIFConvert
 * 
 * @author Pedro J Rivera
 * 
 */
public abstract class TIFConvert {
	public static final int CLR_RGB = BufferedImage.TYPE_INT_RGB;
	public static final int CLR_GRAY_SCALE = BufferedImage.TYPE_BYTE_GRAY;
	public static final int CLR_BLACK_WHITE = BufferedImage.TYPE_BYTE_BINARY;

	public static final int COMPRESSION_NONE = BaselineTIFFTagSet.COMPRESSION_NONE;
	public static final int COMPRESSION_CCITT_RLE = BaselineTIFFTagSet.COMPRESSION_CCITT_RLE;
	public static final int COMPRESSION_CCITT_T_4 = BaselineTIFFTagSet.COMPRESSION_CCITT_T_4;
	public static final int COMPRESSION_CCITT_T_6 = BaselineTIFFTagSet.COMPRESSION_CCITT_T_6;
	public static final int COMPRESSION_LZW = BaselineTIFFTagSet.COMPRESSION_LZW;
	public static final int COMPRESSION_JPEG = BaselineTIFFTagSet.COMPRESSION_JPEG;
	//public static final int COMPRESSION_ZLIB = BaselineTIFFTagSet.COMPRESSION_ZLIB;
	public static final int COMPRESSION_PACKBITS = BaselineTIFFTagSet.COMPRESSION_PACKBITS;
	public static final int COMPRESSION_DEFLATE = BaselineTIFFTagSet.COMPRESSION_DEFLATE;

	protected static final int DEFAULT_DPI = 300;
	protected static final int DEFAULT_COLOR = CLR_RGB;
	protected static final int DEFAULT_COMPRESSION = COMPRESSION_LZW;
	protected static final float DEFAULT_COMPRESSION_QUALITY = 0.25f;
	
	/**
	 * Convert buffered image to a TIF and save to file
	 * @param image
	 * @param tif
	 * @param dpi
	 * @param compression
	 * @param quality
	 * @throws IOException
	 */
	protected static void convert(BufferedImage[] image, String tif, int dpi, int compression, float quality) throws IOException {

		File file = new File(tif);

		if (file.exists()) {
			file.delete();
		}

		RandomAccessFile raf = new RandomAccessFile(file, "rw");		
		FileChannelImageOutputStream fios = new FileChannelImageOutputStream(raf.getChannel());

		convert(image, fios, dpi, compression, quality);
		
		fios.flush();
		fios.close();
		raf.close();

	}

	/**
	 * Convert buffered image and return TIF as a byte array
	 * @param image
	 * @param dpi
	 * @param compression
	 * @param quality
	 * @return 
	 * @throws IOException
	 */
	protected static byte[] convert(BufferedImage[] image, int dpi, int compression, float quality) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

		convert(image, ios, dpi, compression, quality);
		
		ios.flush();
		ios.close();

		return baos.toByteArray();
	}
	
	/**
	 * Convert buffered image to TIF format
	 * @param image
	 * @param os
	 * @param dpi
	 * @param compression
	 * @param quality
	 * @throws IOException
	 */
	protected static void convert(BufferedImage[] image, Object os, int dpi, int compression, float quality) throws IOException {

		TIFFImageWriter writer = new TIFFImageWriter(new TIFFImageWriterSpi());
		TIFFImageWriteParam iwp = new TIFFImageWriteParam(writer.getLocale());

		writer.setOutput(os);
		writer.prepareWriteSequence(null);

		iwp.setCompressionMode(TIFFImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionType(getCompression(compression));			

		if (!isCompressionLossless(compression)) {
			if (quality < 0.0F || quality > 1.0F)
				quality = DEFAULT_COMPRESSION_QUALITY;
			iwp.setCompressionQuality(quality);
		}

		for (int i = 0; i < image.length; i++) {
			ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(image[i]);
			TIFFImageMetadata imageMetadata = (TIFFImageMetadata)writer.getDefaultImageMetadata(imageType, iwp);
			imageMetadata = createImageMetadata(imageMetadata, image[i].getHeight(), image[i].getWidth(), dpi, compression, image[i].getType());
			writer.writeToSequence(new IIOImage(image[i], null, imageMetadata), iwp);
		}
		
		writer.endWriteSequence();
		writer.dispose();				
	}
	
	/**
	 * Get an appropriate buffered image
	 * @param color
	 * @param compression
	 * @param width
	 * @param height
	 * @return
	 */
	protected static BufferedImage getBufferedImage(int color, int compression, int width, int height) {
		BufferedImage image;
		if (compression == COMPRESSION_CCITT_RLE ||
			compression == COMPRESSION_CCITT_T_4 ||
			compression == COMPRESSION_CCITT_T_6 ||
			color == CLR_BLACK_WHITE ) {
	        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		} else if (color == CLR_GRAY_SCALE) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}	
		return image;
	}
	
	/**
	 * Get compression string
	 * @param compression
	 * @return
	 */
	protected static String getCompression(int compression) {
		String c = null;
		for (int i = 0; i < TIFFImageWriter.TIFFCompressionTypes.length; i++) {
			if (compression == TIFFImageWriter.compressionNumbers[i]) {
				c = TIFFImageWriter.TIFFCompressionTypes[i];
			}
		}
		return c;
	}

	/**
	 * Get loss less flag
	 * @param compression
	 * @return
	 */
	protected static boolean isCompressionLossless(int compression) {
		boolean lossless = true;
		for (int i = 0; i < TIFFImageWriter.TIFFCompressionTypes.length; i++) {
			if (compression == TIFFImageWriter.compressionNumbers[i]) {
				lossless = TIFFImageWriter.isCompressionLossless[i];
			}
		}		
		return lossless;
	}
	
	/**
	 * Return the image meta data for the new TIF image
	 * @param imageMetadata
	 * @return
	 * @throws IIOInvalidTreeException
	 * 
	 * Based on TIFF V6.0 specifications.</br></br>
	 * @see <a href="http://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf">TIFF 6.0 Specification</a>
	 */
	protected static TIFFImageMetadata createImageMetadata(TIFFImageMetadata imageMetadata, int height, int width, int dpi, int compression, int type) throws IIOInvalidTreeException {
		
		/*
		 * Fields are arrays
		 * Each TIFF field has an associated Count. This means that all fields are actually
		 * one-dimensional arrays, even though most fields contain only a single value. 
		 */
		// 2014 following vars are not used
//		char[] cImageWidth = new char[] {(char)width};
//		char[] cImageLength = new char[] {(char)height};
//		char[] cResolutionUnit = new char[] {BaselineTIFFTagSet.RESOLUTION_UNIT_INCH};
//		long[][] cDpiResolutionX = new long[][] {{(long)dpi, (long)1}}; //, {(long)0, (long)0}};
//		long[][] cDpiResolutionY = new long[][] {{(long)dpi, (long)1}}; //, {(long)0, (long)0}};
//		char[] cRowsPerStrip = new char[] {(char)(height)};
//		char[] cStripOffsets = new char[] {(char)1};
//		char[] cStripByteCounts = new char[] {(char)1};
		
		/*
		 *  Get the IFD (Image File Directory) which is the root of all the tags
		 *  for this image. From here we can get all the tags in the image.
		 */
		
		/* !!!
		 * WIN64
		 * Generated a error code on purpose
		 * if the platform is not win64, please uncomment the following code 
		 * 
		 **/
		
		
//		TIFFDirectory ifd = imageMetadata.getRootIFD();		
//		
//		/*
//		 *  Create the necessary TIFF tags that we want to add to the image meta data
//		 */
//		BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
//
//		/*
//		 * Rows and Columns 
//		 */
//		TIFFTag tagImageLength = base.getTag(BaselineTIFFTagSet.TAG_IMAGE_LENGTH);
//		TIFFTag tagImageWidth = base.getTag(BaselineTIFFTagSet.TAG_IMAGE_WIDTH);
//		TIFFField fieldImageLength = new TIFFField(tagImageLength, TIFFTag.TIFF_SHORT, 1, cImageLength);
//		TIFFField fieldImageWidth = new TIFFField(tagImageWidth, TIFFTag.TIFF_SHORT, 1, cImageWidth);
//		ifd.addTIFFField(fieldImageLength);
//		ifd.addTIFFField(fieldImageWidth);
//
//		/*
//		 *  Physical Dimensions
//		 */
//		TIFFTag tagResUnit = base.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT);
//		TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
//		TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);
//		TIFFField fieldResUnit = new TIFFField(tagResUnit, TIFFTag.TIFF_SHORT, 1, cResolutionUnit);
//		TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1, cDpiResolutionX);
//		TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1, cDpiResolutionY);
//		ifd.addTIFFField(fieldResUnit);
//		ifd.addTIFFField(fieldXRes);
//		ifd.addTIFFField(fieldYRes);
//		
//		/*
//		 * Location of the Data
//		 */
//		TIFFTag tagRowsPerStrip = base.getTag(BaselineTIFFTagSet.TAG_ROWS_PER_STRIP);
//		TIFFTag tagStripOffSets = base.getTag(BaselineTIFFTagSet.TAG_STRIP_OFFSETS);
//		TIFFTag tagStripByteCounts = base.getTag(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS);
//		TIFFField fieldRowsPerStrip = new TIFFField(tagRowsPerStrip, TIFFTag.TIFF_SHORT, 1, cRowsPerStrip);
//		TIFFField fieldStripOffsets = new TIFFField(tagStripOffSets, TIFFTag.TIFF_SHORT, 1, cStripOffsets);
//		TIFFField fieldStripByteCounts = new TIFFField(tagStripByteCounts, TIFFTag.TIFF_SHORT, 1, cStripByteCounts);
//		ifd.addTIFFField(fieldRowsPerStrip);
//		ifd.addTIFFField(fieldStripOffsets);
//		ifd.addTIFFField(fieldStripByteCounts);
		
		/*
		 * END of WIN64
		 */
		
		return imageMetadata;

	}

}
