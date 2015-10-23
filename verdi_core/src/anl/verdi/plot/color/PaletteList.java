package anl.verdi.plot.color;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PaletteList extends JList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4705230382068881524L;

	public PaletteList() {
		super(new DefaultListModel());
		this.setCellRenderer(new CellRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setVisibleRowCount(-1);
	}

	public void setData(List<Palette> palettes) {
		DefaultListModel model = (DefaultListModel) getModel();
		model.clear();
		for (Palette palette : palettes) {
			model.addElement(palette);
		}
	}

	//Subclass JList to workaround bug 4832765, which can cause the
	//scroll pane to not let the user easily scroll up to the beginning
	//of the list.  An alternative would be to set the unitIncrement
	//of the JScrollBar to a fixed value. You wouldn't get the nice
	//aligned scrolling, but it should work.
	public int getScrollableUnitIncrement(Rectangle visibleRect,
	                                      int orientation,
	                                      int direction) {
		int row;
		if (orientation == SwingConstants.VERTICAL &&
						direction < 0 && (row = getFirstVisibleIndex()) != -1) {
			Rectangle r = getCellBounds(row, row);
			if ((r.y == visibleRect.y) && (row != 0)) {
				Point loc = r.getLocation();
				loc.y--;
				int prevIndex = locationToIndex(loc);
				Rectangle prevR = getCellBounds(prevIndex, prevIndex);

				if (prevR == null || prevR.y >= r.y) {
					return 0;
				}
				return prevR.height;
			}
		}
		return super.getScrollableUnitIncrement(
						visibleRect, orientation, direction);
	}

	private class CellRenderer extends DefaultListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1313302808589213821L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
		                                              boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Palette palette = (Palette) value;
			label.setText(palette.getDescription());
			label.setIcon(new PaletteIcon(palette, PaletteIcon.Orientation.HORIZONTAL));
			label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			return label;
		}
	}
}
