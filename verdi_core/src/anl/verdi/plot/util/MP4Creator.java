package anl.verdi.plot.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

public class MP4Creator {
	
	static final Logger Logger = LogManager.getLogger(MP4Creator.class);
	
	SeekableByteChannel out = null;
    AWTSequenceEncoder encoder = null;
    boolean encoding = true;

	
	public void init(String outputPath) {
		try {
			out = NIOUtils.writableFileChannel(outputPath);
			encoder = new AWTSequenceEncoder(out, Rational.R(1, 1));
			encoding = true;
		} catch (Exception e) {
			Logger.error("Error initializing MP4Creator",  e);
		}
	}
	
	public void addImage(BufferedImage image) {
		synchronized(this) {
			if (!encoding)
				return;
			try {
				if (image.getWidth() %2 == 1 || image.getHeight() % 2 == 1) {
					int height = image.getHeight();
					int width = image.getWidth();
					if (height % 2 == 1)
						++height;
					if (width % 2 == 1)
						++width;
					BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			        Graphics2D g = result.createGraphics();
			        g.drawImage(image, 0, 0, null);
			        g.dispose();
			        image = result;		        
				}
				encoder.encodeImage(image);
			} catch (IOException e) {
				Logger.error("Error adding image", e);
			}
		}
	}
	
	public void finish() {
		synchronized (this) {
	        try {
	        	encoding = false;
				encoder.finish();
			} catch (IOException e) {
				Logger.error("Error writing MP4Creator data",  e);
			}
		}
        NIOUtils.closeQuietly(out);




	}

}
