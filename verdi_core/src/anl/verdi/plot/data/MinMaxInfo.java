/**
 * MinMaxInfo - Used to calculate min/max values across levels and timesteps
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/


package anl.verdi.plot.data;

public class MinMaxInfo {

	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	int minIndex = -1;
	int maxIndex = -1;
	int count = 0;
	int totalCount = 0;
	
	public MinMaxInfo(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public synchronized void incrementCount(int amount) {
		count += amount;
	}
	
	public void visitValue(double value, int index) {
		if (value <= min) {
			min = value;
			minIndex = index;
		}
		if (value >= max) {
			max = value;
			maxIndex = index;
		}
	}
	
	public int getMinIndex() {
		return minIndex;
	}
	
	public int getMaxIndex() {
		return maxIndex;
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public void calcDone() {
	}
	
	public int getCount() {
		return count;
	}
	
	public double getCompletion() {
		return count / (double)totalCount * 100;
	}
	
}
