package anl.verdi.area.target;

public class TargetAreaInfo {
	
	double area = 0;
	int[] rowIndex = null;
	int[] colIndex = null;
	float[] overlapArea = null;
	
	public TargetAreaInfo(double area, int[] rowIndex, int[] colIndex, float[] overlapArea) {
		this.area = area;
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.overlapArea = overlapArea;
	}
	
	public int[] getRowIndex() {
		return rowIndex;
	}
	
	public int[] getColIndex() {
		return colIndex;
	}
	
	public float[] getOverlapArea() {
		return overlapArea;
	}

	public double getArea() {
		return area;
	}
}
