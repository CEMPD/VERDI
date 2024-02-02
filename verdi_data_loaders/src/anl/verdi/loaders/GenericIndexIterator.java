package anl.verdi.loaders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import anl.verdi.data.AxisRange;
import ucar.ma2.IndexIterator;

public class GenericIndexIterator implements IndexIterator {
	
	double[][] list = null;
	
	int index = -1;
	
	int dataPosition;
	
	long dataStart;
	
	long dataEnd;
	
	List<GenericFieldFilter> rangeFilters = new ArrayList<GenericFieldFilter>();
	
	private class GenericFieldFilter {
		
		int	fieldPosition;
		double min;
		double max;
		GenericFieldFilter(int fieldPosition, double min, double max) {
			this.fieldPosition = fieldPosition;
			this.min = min;
			this.max = max;
		}
		
		boolean accept(double[] row) {
			return fieldPosition < row.length && row[fieldPosition] >= min && row[fieldPosition] <= max;
		}
	}
	
	public GenericIndexIterator(double[][] list, int position, long dataStart, long dataEnd) {
		this.list = list;
		this.dataPosition = position;
		this.dataStart = dataStart;
		this.dataEnd = dataEnd;
		if (dataStart > 0) {
			//Field position map is hardcoded in ICTDataset String[] fields
			GenericFieldFilter filter = new GenericFieldFilter(0, dataStart, dataEnd -1);
			rangeFilters.add(filter);
		}
	}
	
	private boolean rowInRange(double[] row) {
		for (int i = 0; i < rangeFilters.size(); ++i) {
			if (!rangeFilters.get(i).accept(row)) {
				return false;
			}
		}
		return true;
	}

	public boolean getBooleanCurrent() {
		return false;
	}

	public boolean getBooleanNext() {
		return false;
	}

	public byte getByteCurrent() {
		return 0;
	}

	public byte getByteNext() {
		return 0;
	}

	public char getCharCurrent() {
		return 0;
	}

	public char getCharNext() {
		return 0;
	}

	public int[] getCurrentCounter() {
		int[] ret = new int[] { index };
		return ret;
	}
	
	private void rangeCheck() {
		while (index > -1 && index < list.length && !rowInRange(list[index])) {
			++index;
		}
			
	}

	public double getDoubleCurrent() {
		rangeCheck();
		if (index > -1 && index < list.length) {
			return list[index][dataPosition];
		}
		return 0;
	}

	public double getDoubleNext() {
		++index;
		return getDoubleCurrent();
	}

	public float getFloatCurrent() {
		return 0;
	}

	public float getFloatNext() {
		return 0;
	}

	public int getIntCurrent() {
		Object obj = null;
		rangeCheck();
		if (index > -1 && index < list.length)
			return (int)list[index][dataPosition];			
		return 0;
	}

	public int getIntNext() {
		++index;
		return getIntCurrent();
	}

	public long getLongCurrent() {
		return 0;
	}

	public long getLongNext() {
		return 0;
	}

	public Object getObjectCurrent() {
		rangeCheck();
		if (index > -1 && index < list.length) {
			return list[index][dataPosition];
		}		
		return null;
	}

	public Object getObjectNext() {
		++index;
		return getObjectCurrent();
	}

	public short getShortCurrent() {
		return 0;
	}

	public short getShortNext() {
		return 0;
	}

	public boolean hasNext() {
		int currentIndex = index;
		rangeCheck();
		int newIndex = index;
		index = currentIndex;
		return newIndex < list.length && list.length > 0;
	}

	public Object next() {
		return getObjectNext();
	}

	@Override
	public void setBooleanCurrent(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBooleanNext(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setByteCurrent(byte arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setByteNext(byte arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharCurrent(char arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharNext(char arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDoubleCurrent(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDoubleNext(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFloatCurrent(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFloatNext(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntCurrent(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntNext(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLongCurrent(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLongNext(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setObjectCurrent(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setObjectNext(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShortCurrent(short arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShortNext(short arg0) {
		// TODO Auto-generated method stub

	}

}
