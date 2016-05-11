package anl.verdi.plot.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import anl.verdi.plot.gui.FastTilePlotPanel;

/**
 * Creates an animated GIF from GIF frames. A thin wrapper to code written by
 * other people, as documented on the thread on the Sun forums 'Create animated
 * GIF using imageio' http://forums.sun.com/thread.jspa?threadID=5395006 See the
 * printUsage() method for details on parameters required.
 * 
 * @author Andrew Thompson
 */
public class WriteAnimatedGif {
	
	static final Logger Logger = LogManager.getLogger(WriteAnimatedGif.class.getName());
	private ImageWriter iw;
	private ImageOutputStream ios;
	private int frameCount = 0;

	public WriteAnimatedGif() {
		//
	}

	/**
	 * See http://forums.sun.com/thread.jspa?messageID=10755673#10755673
	 * 
	 * @author Maxideon
	 * @param delayTime
	 *            String Frame delay for this frame.
	 */
	private void configure(IIOMetadata meta, String delayTime) {

		String metaFormat = meta.getNativeMetadataFormatName();

		if (!"javax_imageio_gif_image_1.0".equals(metaFormat)) {
			throw new IllegalArgumentException(
					"Unfamiliar gif metadata format: " + metaFormat);
		}

		Node root = meta.getAsTree(metaFormat);

		// find the GraphicControlExtension node
		Node child = root.getFirstChild();
		while (child != null) {
			if ("GraphicControlExtension".equals(child.getNodeName())) {
				break;
			}
			child = child.getNextSibling();
		}

		IIOMetadataNode gce = (IIOMetadataNode) child;
		gce.setAttribute("userDelay", "FALSE");
		gce.setAttribute("delayTime", delayTime);

		// only the first node needs the ApplicationExtensions node
		if (frameCount == 1) {
			IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
			IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
			ae.setAttribute("applicationID", "NETSCAPE");
			ae.setAttribute("authenticationCode", "2.0");
			byte[] uo = new byte[] {
			// last two bytes is an unsigned short (little endian) that
					// indicates the the number of times to loop.
					// 0 means loop forever.
					0x1, 0x0, 0x0 };
			ae.setUserObject(uo);
			aes.appendChild(ae);
			root.appendChild(aes);
		}

		try {
			meta.setFromTree(metaFormat, root);
		} catch (IIOInvalidTreeException e) {
			// shouldn't happen
			throw new Error(e);
		}
	}

	/**
	 * See http://forums.sun.com/thread.jspa?messageID=9988198
	 * 
	 * @author GeoffTitmus
	 * @param file
	 *            File A File in which to store the animation.
	 * @param frames
	 *            BufferedImage[] Array of BufferedImages, the frames of the
	 *            animation.
	 * @param delayTime
	 *            String String, representing the frame delay time (in centiseconds)
	 */
	public void saveAnimate(File file, BufferedImage[] frames,
			int delayTime) throws Exception {

		String[] delayTimes = new String[frames.length];
 		for (int i = 0; i < delayTimes.length; i++) delayTimes[i] = delayTime + "";
		ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();

		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		iw.setOutput(ios);
		iw.prepareWriteSequence(null);
		frameCount = 0;
		for (int i = 0; i < frames.length; i++) 
		{			
			frameCount++;
			BufferedImage src = frames[i];
			ImageWriteParam iwp = iw.getDefaultWriteParam();
			IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(src), iwp);
			configure(metadata, delayTimes[i]);
			IIOImage ii = new IIOImage(src, null, metadata);
			iw.writeToSequence(ii, null);
		}
		iw.endWriteSequence();
		ios.close();
	}

	public void start(File file) throws Exception {
		this.frameCount = 0;
		iw = ImageIO.getImageWritersByFormatName("gif").next();

		ios = ImageIO.createImageOutputStream(file);
		iw.setOutput(ios);
		iw.prepareWriteSequence(null);
	}

	public void finish() throws Exception {
		iw.endWriteSequence();
		ios.close();
	}
	
	/**
	 * See http://forums.sun.com/thread.jspa?messageID=9988198
	 * 
	 * @author Darin Del Vecchio
	 * @param index
	 *            int, Index of frame being added to GIF (Zero Based)
	 * @param frame
	 *            BufferedImage, the frame of the
	 *            animation.
	 * @param delayTime
	 *            String, representing the frame delay time (in centiseconds)
	 */
	public void addFrame(BufferedImage frame,
			int delayTime) throws Exception 
		{
			frameCount++;
			ImageWriteParam iwp = iw.getDefaultWriteParam();
			IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frame), iwp);
			configure(metadata, delayTime + "");
			IIOImage ii = new IIOImage(frame, null, metadata);
			iw.writeToSequence(ii, null);
	}

	/** Dump the usage to the System.err stream. */
//	public static void printUsage() {
//		StringBuffer sb = new StringBuffer();
//		String eol = System.getProperty("line.separator");
//		sb.append("Usage: 2 forms each using 3 arguments");
//		sb.append(eol);
//		sb.append("1) output (animated GIF) file name");
//		sb.append(eol);
//		sb.append("2) input files (animation frames), separated by ','");
//		sb.append(eol);
//		sb
//				.append("3) single frame rate, or comma separared list of frame rates");
//		sb.append(eol);
//		sb
//				.append("java WriteAnimatedGif animate.gif frm1.gif,frm2.gif,..,frmN.gif 100");
//		sb.append(eol);
//		sb
//				.append("java WriteAnimatedGif animate.gif frm1.gif,frm2.gif,..,frmN.gif 100,40,..,N");
//		sb.append(eol);
//		sb
//				.append("The 2nd form must have exactly as many integers as there are frames.");
//		sb.append(eol);
//		sb
//				.append("Frame rates are specified in increments of 1/100th second, NOT milliseconds.");
//		sb.append(eol);
//
//		System.err.print(sb);
//	}

	/**
	 * Checks that a String intended as a delayTime is an integer>0. If not,
	 * dumps a warning message and the usage, then exits. If successful, returns
	 * the String unaltered.
	 */
	public static String checkDelay(String delay) {
		try {
			int val = Integer.parseInt(delay);
			if (val < 1) {
				Logger.error("Animation frame delay '" + val + "' is < 1!");
				System.exit(1);
			}
		} catch (NumberFormatException nfe) {
			Logger.error("Could not parse '" + delay + "' as an integer.");
			System.exit(1);
		}
		return delay;
	}

	/**
	 * Parse the arguments and if successful, attempt to write the animated GIF.
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
//			printUsage();
			System.exit(1);
		}

		// deal with the output file name
		File f = new File(args[0]);

		// deal with the input file names
		String[] names = args[1].split(",");
		if (names.length < 2) {
			System.err.println("An animation requires 2 or more frames!");
//			printUsage();
			System.exit(1);
		}
		BufferedImage[] frames = new BufferedImage[names.length];
		for (int ii = 0; ii < names.length; ii++) {
			frames[ii] = ImageIO.read(new File(names[ii]));
		}

		// deal with the frame rates
		String[] delays = args[2].split(",");
		// note: length of names, not delays
		String[] delayTimes = new String[names.length];
		if (delays.length != names.length) {
			System.err.println(delays.length + " delays specified for "
					+ names.length + " frames!");
//			printUsage();
			System.exit(1);
		} else if (delays.length == 1) {
			for (int ii = 0; ii < delayTimes.length; ii++) {
				// fill all values with the single delayTime
				delayTimes[ii] = checkDelay(delays[0]);
			}
		} else {
			for (int ii = 0; ii < delayTimes.length; ii++) {
				delayTimes[ii] = checkDelay(delays[ii]);
			}
		}

		// save an animated GIF
//		saveAnimate(f, frames, delayTimes);
	}
}