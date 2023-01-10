package anl.verdi.area;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import anl.verdi.area.target.Target;

public class RangeLevelFilter implements PropertyIsEqualTo {
	
	float cutoff;
	boolean unselectedOnly;
	boolean debug = false;
	
	/* Geotools has a bug where filters continue being applied after the first match.
	 * initialFilter and matchFound allows a list of filters to know and stop processing
	 * after the initial match.
	 */
	boolean initialFilter = false;
	static ThreadLocal<Boolean> matchFound = new ThreadLocal<Boolean>();
	
	public RangeLevelFilter(double cutoff, boolean unselectedOnly) {
		this.cutoff = (float)cutoff;
		this.unselectedOnly = unselectedOnly;
	}
	
	public void setInitialFilter(boolean initial) {
		initialFilter = initial;
	}
	
	public void setDebug(boolean d) {
		debug = d;
	}
	
	@Override
	public boolean evaluate(Object object) {
		if (initialFilter)
			matchFound.set(Boolean.FALSE);
		else if (Boolean.TRUE.equals(matchFound.get()))
				return false;				
		
		if (debug) {
			debug = true;
		}
		boolean ret = true;
		if (unselectedOnly)
			ret = matchUnSelected(object);
		if (ret)
			ret = matchRange(object);
		if (ret)
			matchFound.set(Boolean.TRUE);
		return ret;
	}
	
	public boolean matchUnSelected(Object object) {
		if (object instanceof SimpleFeature) {
			Object attr = ((SimpleFeature)object).getAttribute("the_geom");
			if (attr instanceof Geometry) {
				Target tgt = Target.getTarget((Geometry)attr);
				if (tgt == null  || !tgt.depositionCalculated() || !tgt.containsDeposition())
					return false;
				return !tgt.isSelectedPolygon();
			}
		}
		return false;
	}

	public boolean matchRange(Object object) {
		if (object instanceof SimpleFeature) {
			Object attr = ((SimpleFeature)object).getAttribute("the_geom");
			if (attr instanceof Point) {
				Double obsValue = (Double)((Point)attr).getUserData();
				if (obsValue != null) {
					return obsValue >= cutoff;
				}
			}
			if (attr instanceof Geometry) {
				//TODO - need tgt to have correct data and type of map
				Target tgt = Target.getTarget((Geometry)attr);
				if (tgt == null || !tgt.depositionCalculated()) {
					//System.out.println("Could not resolve target " + attr);
					//TODO - figure out where this came from.  There are a ton of points.
					return false;
				}
				boolean ret = tgt != null && tgt.getCurrentDeposition() >= cutoff;
				//if (ret)
					//System.out.println("Found a match at " + tgt.getCurrentDeposition() +", cutoff " + cutoff + ", color " + color);
				return ret;
			}
		}
		return false;
	}

	@Override
	public Object accept(FilterVisitor visitor, Object extraData) {
		return this;
	}

	@Override
	public Expression getExpression1() {
		return null;
	}

	@Override
	public Expression getExpression2() {
		return null;
	}

	@Override
	public boolean isMatchingCase() {
		return false;
	}

	@Override
	public MatchAction getMatchAction() {
		return MatchAction.ANY;
	}

}
