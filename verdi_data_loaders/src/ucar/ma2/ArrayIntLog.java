/**
 * ArrayIntLog - Subclass of ArrayInt used to return log of each value
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/


package ucar.ma2;

public class ArrayIntLog extends ArrayInt {
	
	double logBase;

	public ArrayIntLog(ArrayInt source, double base) {
		super(source.getIndex(), false, (int[])source.getStorage());
		setLogBase(base);
		
	}
	
	public void setLogBase(double base) {
		logBase = Math.log(base);
	}
	
	public float getFloat(Index index) {
		return (float)(Math.log(super.getFloat(index)) / logBase);
	}	
	public float getFloat(int index) {
		return (float)(Math.log(super.getFloat(index)) / logBase);
	}
	public double getDouble(Index index) {
		return (double)(Math.log(super.getDouble(index)) / logBase);
	}	
	public double getDouble(int index) {
		return (double)(Math.log(super.getDouble(index)) / logBase);
	}
	public int getInt(Index index) {
		return (int)Math.round(Math.log(super.getInt(index)) / logBase);
	}	
	public int getInt(int index) {
		return (int)Math.round(Math.log(super.getInt(index)) / logBase);
	}
	public long getLong(Index index) {
		return Math.round(Math.log(super.getInt(index)) / logBase);
	}	
	public long getLong(int index) {
		return Math.round(Math.log(super.getInt(index)) / logBase);
	}
	
	
}
