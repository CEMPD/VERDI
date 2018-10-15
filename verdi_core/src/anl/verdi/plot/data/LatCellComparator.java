package anl.verdi.plot.data;

import java.util.Comparator;

import anl.verdi.data.MeshCellInfo;

public class LatCellComparator implements Comparator {

	private static LatCellComparator instance = new LatCellComparator();
	
	public static LatCellComparator getInstance() {
		return instance;
	}

	public int compare(Object o1, Object o2) {
		double v1 = 0;
		double v2 = 0;
		if (o1 instanceof Double)
			v1 = (Double)o1;
		else if (o1 instanceof Integer)
			v1 = ((Integer)o1).intValue();
		else
			v1 = ((MeshCellInfo)o1).getLat(((MeshCellInfo)o1).getMinYPosition());
		if (o2 instanceof Double)
			v2 = (Double)o2;
		else if (o2 instanceof Integer)
			v2 = ((Integer)o2).intValue();
		else
			v2 = ((MeshCellInfo)o2).getLat(((MeshCellInfo)o2).getMinYPosition());
		if (v1 == v2)
			return 0;
		if (v1 < v2)
			return -1;
		return 1;
	}
	
}
