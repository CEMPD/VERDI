package anl.verdi.plot.data;

public class MinMaxInfo {

	double max = Double.MAX_VALUE * -1;
	double min = Double.MAX_VALUE;
	//double sum = 0;
	//double average = 0;
	int count = 0;
	int totalCount = 0;
	
	public MinMaxInfo(int totalCount) {
		this.totalCount = totalCount;
	}
		
	/*public synchronized void incrementSum(double amount) {
		sum += amount;
	}*/
	
	public synchronized void incrementCount(int amount) {
		count += amount;
		//average = sum / count;
	}
	
	public void visitValue(double value) {
		if (value < min)
			min = value;
		if (value > max)
			max = value;
		//incrementSum(value);
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
	
	/*public double getSum() {
		return sum;
	}*/
	
	public double getCompletion() {
		return count / (double)totalCount * 100;
	}
	
}
