package anl.verdi.plot.color;

import static anl.verdi.plot.color.ColorMap.IntervalType.AUTOMATIC;
import static anl.verdi.plot.color.ColorMap.IntervalType.CUSTOM;
import static anl.verdi.plot.color.ColorMap.ScaleType.LINEAR;
import static anl.verdi.plot.color.ColorMap.ScaleType.LOGARITHM;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.table.AbstractTableModel;

import anl.verdi.plot.color.ColorMap.PaletteType;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ColorMapTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2171805813435147840L;
	private ColorMap map;
	private final static Class<?>[] CLASSES = {Color.class, Double.class};
	private final static String[] NAMES = {"Color", "Interval Start"};

	private boolean edit = false;
	private ColorMap.ScaleType sType = ColorMap.ScaleType.LINEAR;

	public ColorMapTableModel() {
		map = new ColorMap();
	}

	public void resetColorMap(ColorMap map) {
		this.map = map;
		fireTableDataChanged();
	}
	
	public void resetPaletteType(PaletteType paletteType) {
		map.setPaletteType(paletteType);
	}
	
	public void resetPalette(Palette palette) {
		map.setPalette(palette);
		fireTableDataChanged();
	}

	public ColorMap getColorMap() {
		return map;
	}


	/**
	 * Returns a default name for the column using spreadsheet conventions:
	 * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
	 * returns an empty string.
	 *
	 * @param column the column being queried
	 * @return a string containing the default name of <code>column</code>
	 */
	@Override
	public String getColumnName(int column) {
		return NAMES[column];
	}

	public int getColumnCount() {
		return CLASSES.length;
	}

	public int getRowCount() {
		return map.getColorCount();
	}


	/**
	 * Returns false.  This is the default implementation for all cells.
	 *
	 * @param rowIndex    the row being queried
	 * @param columnIndex the column being queried
	 * @return false
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 || edit;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) return map.getColor(rowIndex);
		else {
			double interval = 0;
			try {
				interval = map.getIntervalStart(rowIndex);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				return String.format("%" + map.getFormatString(), interval);
			} catch (Exception e) {
				//throw e;
			}
			return null;
		}
	}


	/**
	 * This empty implementation is provided so users don't have to implement
	 * this method if their data model is not editable.
	 *
	 * @param aValue      value to assign to cell
	 * @param rowIndex    row of cell
	 * @param columnIndex column of cell
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue != null) {
			if (columnIndex == 0) map.setColor(rowIndex, (Color) aValue);
			else if (columnIndex == 1) {
				try {
					double val = Double.parseDouble(aValue.toString());
					map.setIntervalStart(rowIndex, val);
				} 
				catch (NumberFormatException ex) {}
				catch (Exception ex) {}
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

//	public void setIntervalEditEnabled(boolean val) {
//		edit = val;
//		map.setIntervalType(val ? CUSTOM : AUTOMATIC);
//		fireTableDataChanged();
//	}
	
	public void setIntervalEditEnabled(int val) {
		edit = val == 1;
		if ( val == 0) {
			map.setIntervalType(AUTOMATIC);
		} else if ( val == 1) {
			map.setIntervalType(CUSTOM);
		} 
		fireTableDataChanged();
	}
	
	public void setScaleType(int val) {
		if ( val == 0) {
			map.setScaleType(LINEAR);
			this.sType = LINEAR;
		} else if ( val == 1) {
			map.setScaleType(LOGARITHM);
			this.sType = LOGARITHM;
		} 
		fireTableDataChanged();
	}	
	
	public ColorMap.ScaleType getScaleType() {
		return this.sType;
	}
	
	public void setLogBase( String base){
		try {
			double baseValue = Math.E;
			if ( !base.trim().equalsIgnoreCase("E")) {
				baseValue = Double.parseDouble( base);
			}			
			map.setLogBase(baseValue);
		} catch (NumberFormatException ex) {
			
		}
	}
	
	public String getLogBase( ){
		String base = "E";
		double baseValue = map.getLogBase();
		if ( baseValue != Math.E) {
			Double objBase = baseValue;
			base = objBase.toString();
		}
		return base;
	}
}
