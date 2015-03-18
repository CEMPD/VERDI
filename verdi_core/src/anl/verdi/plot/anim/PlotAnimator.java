package anl.verdi.plot.anim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.protocol.FileTypeDescriptor;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import simphony.util.messages.MessageCenter;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.AnimationListener;
import anl.verdi.plot.util.MovieMaker;
import anl.verdi.plot.util.VideoMaker;
import anl.verdi.plot.util.WriteAnimatedGif;

/**
 * Animates a plot over a series of time ranges.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotAnimator {
	static final Logger Logger = LogManager.getLogger(PlotAnimator.class.getName());

	private TimeAnimatablePlot plot;
	private MovieMaker maker;
	private VideoMaker videoMaker;
//	private AnimatedGifEncoder gifEncoder;
	private WriteAnimatedGif writeAnimatedGif;
	private Timer timer;
	private List<AnimationListener> listeners = new ArrayList<AnimationListener>();
	private UpdatePlotAction action;
	private int width, height;
	

	/**
	 * Creates a PlotAnimtor to animate the specified plot.
	 * This assumes the plot is currently displayed and the
	 * image size will be that of the displayed plot.
	 *
	 * @param plot the plot to animate
	 */
	public PlotAnimator(TimeAnimatablePlot plot) {
		this.plot = plot;
	}

	/**
	 * Creates a PlotAnimtor to animate the specified plot. The image
	 * size is determined by the width and height parameters. The plot
	 * need not be displayed.
	 *
	 * @param plot   the plot to animate
	 * @param width  the width of the animated image
	 * @param height the height of the animated image
	 */
	public PlotAnimator(TimeAnimatablePlot plot, int width, int height) {
		this.plot = plot;
		this.width = width;
		this.height = height;
	}

	/**
	 * Adds an AnimationListener to this PlotAnimator to listen
	 * for animation events.
	 *
	 * @param listener the listener to add
	 */
	public void addAnimationListener(AnimationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Starts the animation beginning at the start timestep
	 * and ending at the end timestep with the specified delay
	 * between frames.
	 *
	 * @param start the starting timestep
	 * @param end   the ending timestep
	 * @param delay delay between frames in milliseconds
	 */
	public void start(int start, int end, int delay) {
		action = new UpdatePlotAction(start, end);
		timer = new Timer(0, action);
		timer.setDelay(delay);
		timer.start();
	}

	/**
	 * Starts the animation beginning at the start timestep
	 * and ending at the end timestep. A movie or animated gif
	 * of the animated plot will be saved into the specified
	 * files, if those files are not null.
	 *
	 * @param start           the starting timestep
	 * @param end             the ending timestep
	 * @param movieFile       if not null, a quicktime movie will be made
	 *                        and saved to this file
	 * @param animatedGifFile if not null, an anmiated gif will be made
	 *                        and saved to this file
	 */
	public void start(int start, int end, File movieFile, File animatedGifFile, File aviFile) {
		start(start, end, 1, movieFile, animatedGifFile, aviFile);
	}
	
	public void start(int start, int end, int delay, File movieFile, File animatedGifFile, File aviFile) {
		if (movieFile != null) maker = new MovieMaker(1, movieFile, FileTypeDescriptor.QUICKTIME);
		
		if (animatedGifFile != null) {
//			gifEncoder = new AnimatedGifEncoder();
//			gifEncoder.start(animatedGifFile.getAbsolutePath());
//			gifEncoder.setDelay(500);
			writeAnimatedGif = new WriteAnimatedGif();
			try {
				writeAnimatedGif.start(animatedGifFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (aviFile != null) {
			videoMaker = new VideoMaker(1, aviFile, FileTypeDescriptor.MSVIDEO);
		}
		
		start(start, end, delay);
	}

	private void fireStopped() {
		for (AnimationListener listener : listeners) {
			listener.animationStopped();
		}
	}

	/**
	 * Stops the animation.
	 */
	public void stop() {
		if (action != null) action.stop();
		maker = null;
//		for (BufferedImage bi : bufferedImages) bi = null;
		writeAnimatedGif = null;
//		gifEncoder = null;
		videoMaker = null;
	}

	// ActionListener that the timer fires to
	// perform the actual animation
	private class UpdatePlotAction implements ActionListener {

		private int end;
		private int current;

		public UpdatePlotAction(int start, int end) {
			this.end = end;
			this.current = start;
		}

		public void stop() {
			timer.stop();
			if (maker != null) maker.cleanUp();
			if (writeAnimatedGif != null) {
//			if (gifEncoder != null) {
//				gifEncoder.finish();
				//
				try {
					writeAnimatedGif.finish();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (videoMaker != null) videoMaker.cleanUp();
			fireStopped();
		}

		public void actionPerformed(ActionEvent e) {
			if (current > end) {
				stop();
			} else {
				plot.updateTimeStep(current++);
				try {
					if (maker != null || writeAnimatedGif/*gifEncoder*/ != null || videoMaker != null) {
						BufferedImage bufferedImage = null;
						if (width == 0) bufferedImage = plot.getBufferedImage();
						else bufferedImage = plot.getBufferedImage(width, height);
						if (maker != null) maker.addImageAsFrame(bufferedImage);
						if (writeAnimatedGif/*gifEncoder*/ != null) {
//							bufferedImages[current - 1] = bufferedImage;
							try {
								writeAnimatedGif.addFrame(bufferedImage, 50);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						if (videoMaker != null) videoMaker.addImageAsFrame(bufferedImage);
					}
				} catch (IOException ex) {
					maker = null;
					videoMaker = null;
					Logger.error("Error while making movie " + ex.getMessage());
				}
			}
		}
	}
}
