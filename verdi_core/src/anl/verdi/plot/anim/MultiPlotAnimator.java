package anl.verdi.plot.anim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.AnimationListener;
import anl.verdi.plot.util.WriteAnimatedGif;

/**
 * Animates plots over a series of time ranges.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class MultiPlotAnimator {

	private int index = 1;

	private Timer timer = new Timer(500, null);
	private List<AnimationListener> listeners = new ArrayList<AnimationListener>();

	/**
	 * Adds the specified plot to animate. The animation will run
	 * from the starting timestep to the ending timestep.
	 *
	 * @param plot  the plot to animate
	 * @param start the starting timestep
	 * @param end   the ending timestep
	 */
	public void addPlotToAnimate(TimeAnimatablePlot plot, int start, int end, File baseGifFile) {
		File f = baseGifFile;
		if (baseGifFile != null) {
			String pathname = baseGifFile.getAbsolutePath();
			int index = pathname.lastIndexOf(".");
			pathname = pathname.substring(0, index) + "-" + this.index++ + pathname.substring(index, pathname.length());
			f = new File(pathname);
		}
		timer.addActionListener(new UpdatePlotAction(plot, start, end, f));
	}

	/**
	 * Adds an AnimationListener to this MultiPlotAnimator to listen
	 * for animation events.
	 *
	 * @param listener the listener to add
	 */
	public void addAnimationListener(AnimationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Starts the animation.
	 */
	public void start() {
		timer.start();
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
		if (timer.isRunning()) {
			timer.stop();
			for (ActionListener listener : timer.getActionListeners()) {
				if (listener instanceof UpdatePlotAction) {
					((UpdatePlotAction)listener).stopGif();
				}
			}
		}
		fireStopped();
	}

	// ActionListener that the timer fires to
	// perform the actual animation
	private class UpdatePlotAction implements ActionListener {

		private int end;
		private int current;
		private TimeAnimatablePlot plot;
//		private AnimatedGifEncoder gifEncoder;
		private WriteAnimatedGif writeAnimatedGif;

		public UpdatePlotAction(TimeAnimatablePlot plot, int start, int end, File gifFile) {
			this.end = end;
			this.current = start;
			this.plot = plot;
			if (gifFile != null) {
				writeAnimatedGif = new WriteAnimatedGif();
				try {
					writeAnimatedGif.start(gifFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				gifEncoder = new AnimatedGifEncoder();
//				gifEncoder.start(gifFile.getAbsolutePath());
//				gifEncoder.setDelay(500);
			}
		}

		public void stopGif() {
			if (writeAnimatedGif != null)
				try {
					writeAnimatedGif.finish();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		public void actionPerformed(ActionEvent e) {
			if (current > end) {
				stop();
			} else {
				plot.updateTimeStep(current++);
				BufferedImage bufferedImage = plot.getBufferedImage();
				if (writeAnimatedGif != null)
					try {
						writeAnimatedGif.addFrame(bufferedImage, 50);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		}
	}
}
