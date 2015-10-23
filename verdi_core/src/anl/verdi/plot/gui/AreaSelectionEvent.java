package anl.verdi.plot.gui;

import java.awt.Rectangle;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class AreaSelectionEvent {

	private Rectangle area;
	private boolean isFinished;
	private String areaString = "";

	public AreaSelectionEvent(Rectangle area, boolean finished) {
		this.area = area;
		isFinished = finished;
		StringBuilder builder = new StringBuilder("(");
		builder.append(area.x);
		builder.append(", ");
		builder.append(area.y);
		builder.append(")");
		if (area.width > 0 || area.height > 0) {
			builder.append(" - (");
			builder.append(area.x + area.width);
			builder.append(", ");
			builder.append(area.y - area.height);
			builder.append(")");
		}
		areaString = builder.toString();
	}

	public AreaSelectionEvent(Rectangle area, String areaString, boolean finished) {
		this.area = area;
		isFinished = finished;
		this.areaString = areaString;
	}

	public Rectangle getArea() {
		return area;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public String areaToString() {
		return areaString;
	}
}
