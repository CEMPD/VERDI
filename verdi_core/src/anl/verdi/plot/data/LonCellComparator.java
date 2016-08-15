package anl.verdi.plot.data;

import java.util.Comparator;

import anl.verdi.data.MeshCellInfo;

public class LonCellComparator implements Comparator {

	private static LonCellComparator instance = new LonCellComparator();
	
	public static LonCellComparator getInstance() {
		return instance;
	}

	public int compare(Object o1, Object o2) {
		double v1 = 0;
		double v2 = 0;
		if (o1 instanceof Double)
			v1 = (Double)o1;
		else
			v1 = ((MeshCellInfo)o1).getLon(((MeshCellInfo)o1).getMinXPosition());
		if (o2 instanceof Double)
			v2 = (Double)o2;
		else
			v2 = ((MeshCellInfo)o2).getLon(((MeshCellInfo)o2).getMinXPosition());
		if (v1 == v2)
			return 0;
		if (v1 < v2)
			return -1;
		return 1;
	}
	
}
