package anl.verdi.util;
// 2014 changed from old version where JScience was used to the new UOMO library

//import static javax.measure.units.NonSI.CUBIC_INCH;	// JScience changed its hierarchy
//import static javax.measure.units.NonSI.FOOT;
//import static javax.measure.units.NonSI.HECTARE;
//import static javax.measure.units.NonSI.INCH;
//import static javax.measure.units.NonSI.MILE;
//import static javax.measure.units.NonSI.OUNCE;
//import static javax.measure.units.NonSI.POUND;
//import static javax.measure.units.NonSI.YARD;
//import static javax.measure.units.SI.CENTI;
//import static javax.measure.units.SI.CUBIC_METER;		// JScience change its spelling
//import static javax.measure.units.SI.GRAM;
//import static javax.measure.units.SI.KELVIN;
//import static javax.measure.units.SI.KILO;
//import static javax.measure.units.SI.KILOGRAM;
//import static javax.measure.units.SI.METER;
//import static javax.measure.units.SI.MICRO;
//import static javax.measure.units.SI.MILLI;
//import static javax.measure.units.SI.NANO;
//import static javax.measure.units.SI.SQUARE_METER;	// JScience changed its spelling

//import static javax.measure.unit.NonSI.CUBIC_INCH;
//import static javax.measure.unit.NonSI.FOOT;
//import static javax.measure.unit.NonSI.HECTARE;
//import static javax.measure.unit.NonSI.INCH;
//import static javax.measure.unit.NonSI.MILE;
//import static javax.measure.unit.NonSI.OUNCE;
//import static javax.measure.unit.NonSI.POUND;
//import static javax.measure.unit.NonSI.YARD;
//import static javax.measure.unit.SI.CENTI;
//import static javax.measure.unit.SI.CUBIC_METRE;
//import static javax.measure.unit.SI.GRAM;
//import static javax.measure.unit.SI.KELVIN;
//import static javax.measure.unit.SI.KILO;
//import static javax.measure.unit.SI.KILOGRAM;
//import static javax.measure.unit.SI.METER;
//import static javax.measure.unit.SI.MICRO;
//import static javax.measure.unit.SI.MILLI;
//import static javax.measure.unit.SI.NANO;
//import static javax.measure.unit.SI.SQUARE_METRE;

// measure: Length (L)
//import javax.measure.unit.BaseUnit;
//import javax.measure.unit.Unit;
import static org.eclipse.uomo.units.AbstractUnit.ONE;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.eclipse.uomo.units.AbstractUnit;
import org.eclipse.uomo.units.SI;
import org.eclipse.uomo.units.SymbolMap;
import org.eclipse.uomo.units.impl.BaseUnit;
import org.eclipse.uomo.units.impl.TransformedUnit;
import org.eclipse.uomo.units.impl.format.LocalUnitFormatImpl;
import org.eclipse.uomo.units.impl.system.Imperial;
import org.eclipse.uomo.units.impl.system.USCustomary;
//import org.unitsofmeasurement.*;
//import org.unitsofmeasurement.quantity.*;
//import org.unitsofmeasurement.service.*;
import org.unitsofmeasurement.unit.Unit;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VUnits {
	static final Logger Logger = LogManager.getLogger(VUnits.class.getName());

	private static VUnits instance = new VUnits();

	private Map<String, Unit> unitMap = new HashMap<String, Unit>();
//	private Map<String,AreaAmount> areaMap = new HashMap<String,AreaAmount>();
	public static final Unit MISSING_UNIT = new BaseUnit("_MISSING_UNIT_");
//	public static final Unit MISSING_UNIT = new BaseUnit("all", "_MISSING_UNIT_");

	private VUnits() {
//		Logger.debug("in private constructor for VUnits");
		// areas
		unitMap.put("m2", SI.SQUARE_METRE);
		unitMap.put("km2", (SI.Prefix.KILO(SI.METRE).multiply(SI.Prefix.KILO(SI.METRE))));	//SI.METRE).multiply(1000).multiply(SI.METRE).multiply(1000));
		unitMap.put("ha", Imperial.ACRE.multiply(2.4711));
		unitMap.put("hectare", Imperial.ACRE.multiply(2.4711));
		unitMap.put("acre", Imperial.ACRE);					//	 HECTARE.times(.40469));
		unitMap.put("sq mi", USCustomary.MILE.multiply(USCustomary.MILE));

		// mass
		unitMap.put("kg", SI.KILOGRAM);
		unitMap.put("g", SI.GRAM);
		unitMap.put("lb", USCustomary.POUND);
		unitMap.put("lbs", USCustomary.POUND);
		unitMap.put("oz", USCustomary.OUNCE);
		unitMap.put("mg", (SI.Prefix.MILLI(SI.GRAM)));		// MILLI:	.divide(1000)
		unitMap.put("\u00b5g", (SI.Prefix.MICRO(SI.GRAM)));	// MICRO:	.divide(1e6)
		unitMap.put("ug", (SI.Prefix.MICRO(SI.GRAM)));		// MICRO	.divide(1e6)
		unitMap.put("ng", (SI.Prefix.NANO(SI.GRAM)));		// NANO:	.divide(1e9)

		// others
		unitMap.put("deg K", SI.KELVIN);
		unitMap.put("deg k", SI.KELVIN);
		unitMap.put("K", SI.KELVIN);
		unitMap.put("k", SI.KELVIN);
		unitMap.put("m3", SI.CUBIC_METRE);
		unitMap.put("in3", USCustomary.CUBIC_INCH);

		SymbolMap symbols = LocalUnitFormatImpl.getInstance().getSymbolMap();
		Unit unit = null;
		// parts per million
		unit = ONE.divide(1000000);
		symbols.label(unit,  "PPM");
		unitMap.put("PPM", unit);
		unit = ONE.divide(1000000);
		symbols.label(unit, "ppm");
		unitMap.put("ppm", unit);
		unitMap.put("ppmV", unit);
		unitMap.put("ppmv", unit);
		
		// parts per billion
		unit = ONE.divide(1000000000);
		symbols.label(unit, "PPB");
		unitMap.put("PPB", unit);
		unit = ONE.divide(1000000000);
		symbols.label(unit, "ppb");
		unitMap.put("ppb", unit);
		unit = ONE.divide(1000000000);
		symbols.label(unit, "ppbV");
		unitMap.put("ppbV", unit);
		unitMap.put("ppbv", unit);
		
		// length
		unitMap.put("cm", (SI.Prefix.CENTI(SI.METRE)));		// CENTI:	.divide(100)
		unitMap.put("m", SI.METRE);
		unitMap.put("in",USCustomary.INCH);
		unitMap.put("ft", USCustomary.FOOT);
		unitMap.put("yd", USCustomary.YARD);
		
	}

	/**
	 * Parses the unit string into a Unit object. A unit unique to the string will be returned.
	 *
	 * @param unitString the string to parse
	 * @return the created Unit.
	 */
	public static Unit createUnit(String unitString) {
		return instance.make1Unit(unitString);
	}

	/**
	 * Parses the unit string into a Unit object. When possible a
	 * known unit (e.g. kg, kg / m2, etc.) will be returned
	 * otherwise a unit unique to the string will be returned.
	 *
	 * @param unitString the string to parse
	 * @return the created Unit.
	 */
	public static Unit createConvUnit(String unitString) {
		return instance.makeUnit(unitString);
	}

	public static String getFormattedName(Unit unit) {
		return instance.getName(unit);
	}

	private Unit makeUnit(String unitString) {
		if (unitString == null || unitString.trim().length()==0) {
			unitString = "none";
		}
		unitString = unitString.trim();
		// convert superscripts
		unitString=unitString.replace('\u00b2','2');
		unitString=unitString.replace('\u00b3','3');
//		Logger.debug("in VUnits.makeUnit, unitString = " + unitString);		
		if (unitString.contains("/")) {
			return getDividedUnit(unitString);
		}

		return getUnit(unitString);
	}

	private Unit make1Unit(String unitString) {
		if (unitString == null || unitString.trim().length()==0) {
			unitString = "none";
		}
		unitString = unitString.trim();
		// convert superscripts
		unitString=unitString.replace('\u00b2','2');
		unitString=unitString.replace('\u00b3','3');
//		Logger.debug("in VUnits.make1Unit, unitString = " + unitString);		
		return getUnit(unitString);
	}

	private Unit getDividedUnit(String unitString) {
//		Logger.debug("in VUnits.getDividedUnit for unitString = " + unitString);
		StringTokenizer tok = new StringTokenizer(unitString, "/");
		if (tok.countTokens() != 2) {
//			Logger.debug("in VUnits.getDividedUnit, count of tokens = " + tok.countTokens() + " returning BaseUnit = " + new BaseUnit(unitString));
			return new BaseUnit(unitString);
		}
		String strRhs = tok.nextToken().trim();
		String strLhs = tok.nextToken().trim();
		if (!(unitMap.containsKey(strRhs) && unitMap.containsKey(strLhs))) {
//			Logger.debug("in VUnits.getDividedUnit, unitString = " + new BaseUnit(unitString));
			return new BaseUnit(unitString);
		}
		Unit rhs = getUnit(strRhs);
		Unit lhs = getUnit(strLhs);
		try {
//			Logger.debug("in VUnits.getDividedUnit, returning rhs.divide = " + rhs.divide(lhs));
			return rhs.divide(lhs);
		} catch (Exception ex) {
//			Logger.debug("in VUnits.getDividedUnit, returning BaseUnit(unitString) = " + new BaseUnit(unitString));
			return new BaseUnit(unitString);
		}
	}

	private Unit getUnit(String unitString) {
		Unit unit = unitMap.get(unitString);
//		Logger.debug("in VUnits.getUnit, Unit = " + unit);
		if (unit == null) {
//			Logger.debug("unit is null, going into try block to get a unit");
			try {
				// see if we can make an SI type unit out of it
			// 2014  trying to figure out what the valueOf function does
			// in javax.measure.unit.Unit.valueOf(java.lang.CharSequence csq) documentation says:
			// Returns a unit instance that is defined from the specified character sequence using the standard unit format.
			// parameter: csq - the character sequence to parse
			// returns UnitFormat.getStandardInstance().parse(csq, new ParsePosition(0))
			// exception matches; thrown if the specified character sequence cannot be correctly parsed (e.g., symbol unknown)
				unit = AbstractUnit.valueOf(unitString);	// changed Unit.valueof(  to AbstractUnit.valueOf(
//				Logger.debug("in VUnits.getUnit, have an AbstractUnit = " + unit);
			} catch (IllegalArgumentException ex) {
				unit = new BaseUnit(unitString);
//				Logger.debug("in VUnits.getUnit, caught a problem so assigning unit = " + unit);
			}
		}
//		Logger.debug("returning from VUnits.getUnit unit = " + unit);
		return unit;
	}

	private String getName(Unit unit) {
		if (unit.equals(unitMap.get("PPM"))) return "PPM";
		else if (unit.equals(unitMap.get("PPB"))) return "PPB";
		else return unit.toString();
	}

	private UnsupportedUnitException getException(String unitString) {
//		Logger.debug("in UnsupportedUnitException for unitString = " + unitString);
		return new UnsupportedUnitException("Unsupported unit format: '" + unitString + "'");
	}
	
}
