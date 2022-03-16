package anl.verdi.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import anl.verdi.util.Tools;
import anl.verdi.util.VersionInfo;

public class SplashPanel extends JPanel {

  /**
	 * 
	 */
	private static final long serialVersionUID = -2885347834825102311L;

	Timer timer;

  public SplashPanel() {
    setOpaque(true);
  }

  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	String pathName = Tools.getIconsDir() + "splash.jpg";
	File imageInputFile = new File(pathName);
	try {
		BufferedImage aBufferedImage = ImageIO.read(imageInputFile);
		g2d.drawImage(aBufferedImage, null, 0, 0);		
		g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
		g2d.setColor(new Color(Integer.parseInt("d52c16", 16)));
		g2d.drawChars(VersionInfo.getVersion().toCharArray(), 0,  VersionInfo.getVersion().length(), 310, 75);
	} catch (IOException e) {
		System.err.println("Failure reading " + pathName);
		e.printStackTrace();
		System.exit(ERROR);
	}
//    Image image = new ImageIcon(getClass().getResource("icons/splash.jpg")).getImage();	// older method this & next lines
	//   g2d.drawImage(image, 0, 0, this);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
  }

/** Take an Image associated with a file, and wait until it is
*  done loading. Just a simple application of MediaTracker.
*  If you are loading multiple images, don't use this
*  consecutive times; instead use the version that takes
*  an array of images.
*/

  public static boolean waitForImage(Image image, Component c) {
	  MediaTracker tracker = new MediaTracker(c);
	  tracker.addImage(image, 0);

	  try {
		  tracker.waitForAll();
	  } catch(InterruptedException ie) {}
	  
	  return(!tracker.isErrorAny());
  }

  public void start() {
    ActionListener runner = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        repaint();
      }
    };

    timer = new Timer(250, runner);
    timer.start();
  }

  public void stop() {
    if (timer != null) timer.stop();
  }
}
