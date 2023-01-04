package anl.verdi.loaders;

import java.util.List;

import ucar.ma2.IndexIterator;

public class ICTIndexIterator implements IndexIterator {
	
	double[][] list = null;
	
	int index = -1;
	
	int dataPosition;
	
	public ICTIndexIterator(double[][] list, int position) {
		this.list = list;
		this.dataPosition = position;
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

	public double getDoubleCurrent() {
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
		return index < list.length && list.length > 0;
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
