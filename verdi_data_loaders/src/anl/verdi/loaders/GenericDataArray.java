package anl.verdi.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.IndexIterator;

public class GenericDataArray extends ucar.ma2.ArrayDouble.D4 {
	
	Map<Integer, Object>[] rankMaps = new Map[4];
	
	List<Integer> time = new ArrayList<Integer>();
	List<Integer> x = new ArrayList<Integer>();
	List<Integer> y = new ArrayList<Integer>();
	List<Integer> layer = new ArrayList<Integer>();
	List<Double> value = new ArrayList<Double>();


	double rawData[][] = null;
	
	public GenericDataArray() {
		super(0, 0, 0, 0);
		
		for (int i = 0; i < 4; ++i)
			rankMaps[i] = new HashMap<Integer, Object>();
	}


	public double get(int time, int x, int y, int layer) {
		
		Object ret = null;
		
		Map<Integer, Object> childMap = getChildMap(0, time, false);
		
		if (childMap != null)
			childMap = getChildMap(1, x, false);
		
		if (childMap != null)
			childMap = getChildMap(2, y, false);
		
		if (childMap != null)
			ret = childMap.get(layer);
		
		if (ret == null) {
			throw new RuntimeException("Illegal index - t: " + time + " x: " + x + " y: " + y + " layer: " + layer);
		}
		
		return (Double)ret;
	}
	

	public void set(int time, int x, int y, int layer, double value) {
				
		Map<Integer, Object> childMap = getChildMap(0, time, true);
		
		childMap = getChildMap(1, x, true);
		childMap = getChildMap(1, y, true);
		
		childMap.put(layer,  value);
		
		this.time.add(time);
		this.x.add(x);
		this.y.add(y);
		this.layer.add(layer);
		this.value.add(value);
	}
	
	public static final int POS_TIME = 0;
	public static final int POS_X = 1;
	public static final int POS_Y = 2;
	public static final int POS_LAYER = 3;
	public static final int POS_VALUE = 4;
	
	
	public void addRow(double time, double x, double y, double layer, double value) {
		double[] row = new double[] { time, x, y, layer, value};
	}
	
	private Map<Integer, Object> getChildMap(int rank, int index, boolean create) {
		Map<Integer, Object> childMap = (Map<Integer, Object>)rankMaps[rank].get(index);
		
		if (childMap == null && create) {
			childMap = new HashMap<Integer, Object>();
			rankMaps[rank].put(index,  childMap);
		}
		
		return childMap;
	}
	
	public IndexIterator getIndexIterator() {
		return super.getIndexIterator();
	}


	public void setRawData(double[][] data) {
		rawData = data;
		
	}
	
	

}
