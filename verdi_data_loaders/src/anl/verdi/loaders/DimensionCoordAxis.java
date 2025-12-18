package anl.verdi.loaders;

import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;
import ucar.nc2.Dimension;

public class DimensionCoordAxis implements CoordAxis {
	
	Dimension dim;
	AxisType type;
	Range range;
	
	public DimensionCoordAxis(Dimension dim, AxisType type) {
		this.dim = dim;
		this.range = new Range(0, dim.getLength());
		this.type = type;

	}

	@Override
	public String getName() {
		return dim.getShortName();
	}

	@Override
	public String getDescription() {
		return dim.getName();
	}

	@Override
	public Range getRange() {
		return range;
	}

	@Override
	public AxisType getAxisType() {
		return type;
	}

	@Override
	public double getValue(int index) {
		return index + range.getOrigin();
	}

	@Override
	public Unit getUnits() {
		return VUnits.MISSING_UNIT;
	}

	@Override
	public boolean isCompatible(CoordAxis axis) {
		if (axis == this) return true;
		long extent = axis.getRange().getExtent();
		if (axis.getAxisType().equals(this.getAxisType()) &&
						extent == this.getRange().getExtent()) {
			for (int i = 0; i < extent; i++) {
				if (getValue(i) != axis.getValue(i)) return false;
			}
			return true;
		}
		return false;
	}

}
