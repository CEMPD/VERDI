package anl.verdi.area;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Vector;

import org.eclipse.uomo.units.SI;
import org.eclipse.uomo.units.impl.ProductUnit;
//import javax.measure.unit.ProductUnit;
//import javax.measure.unit.SI;
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.units.ProductUnit;
//import javax.measure.units.SI;
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.util.VUnits;


/**
 * 
 * File Name:Units.java
 * Description:
 * Converts between units.
 * 
 * @version April 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class Units {
	/** show units as mass/area */
	public final static int MASS_AREA = 0;
	/** show units as mass */
	public final static int MASS = 1;
	/** show units as percentages */
	public final static int PERCENT = 2;
	/** current type of the unit, one of MASS_AREA, MASS, or PERCENT */
	static int currentType = MASS_AREA;

	// types of mass units
	static String[] massChoices = { "kg", "g", "lbs" ,"ug"};

	// types of area units
	static String[] areaChoices = { "acre", "ha", "m2", "km2" };

	// values to multiply to convert mass units to kg units  
	static double kgConverts[] = { 1.0, 0.001, 0.45359237,0.000000001 };

	// values to multiply to convert area units to hectare units 
	static double haConverts[] = { 0.4047, 1.0, 0.0001, 100.0 };

	// current values set for each type
	static int currentMass=2;
	static int currentArea=0;

	static String[] lengthChoices = { "m", "km" };
	/** convert a length to km */
	static public double convertLength(String inUnit, double val) {
		if (inUnit.equals("km"))
			return val;
		return val / 1000.0;
	}
	static public String getAreaFromLength(String length) {
		return length + "2";
	}

	// conversion routines
	static public String getMassFromMassAreaString(String inUnit) {
		int index = inUnit.indexOf('/');
		if(index<0)return null;
		return (inUnit.substring(0, index)).trim();
	}
	static public int getMassFromMassArea(String inUnit) {
		return getMassIndex(getMassFromMassAreaString(inUnit));
	}
	static public String getAreaFromMassAreaString(String inUnit) {
		int index = inUnit.indexOf('/');
		return (inUnit.substring(index + 1, inUnit.length())).trim();
	}
	static public int getAreaFromMassArea(String inUnit) {
		return getAreaIndex(getAreaFromMassAreaString(inUnit));
	}
	static public double conversionMass(String inUnit) {
		int inIndex = getMassIndex(inUnit);
		return conversionMass(inIndex);
	}
	static public double conversionMass(int inIndex) {
		return kgConverts[inIndex] / kgConverts[currentMass];
	}
	static public double convertMass(String inUnit, String outUnit, double val) {
		int inIndex = getMassIndex(inUnit);
		int outIndex = getMassIndex(outUnit);
		return convertMass(inIndex, outIndex, val);
	}
	static public double convertArea(String inUnit, String outUnit, double val) {
		int inIndex = getAreaIndex(inUnit);
		int outIndex = getAreaIndex(outUnit);
		return convertArea(inIndex, outIndex, val);
	}
	static public double convertMass(String inUnit, double val) {
		int inIndex = getMassIndex(inUnit);
		return convertMass(inIndex, currentMass, val);
	}
	static public double convertMass(int inIndex, double val) {
		return convertMass(inIndex, currentMass, val);
	}
	static public double convertMass(int inIndex, int outIndex, double val) {
		if (inIndex == outIndex)
			return val;
		val = val * kgConverts[inIndex] / kgConverts[outIndex];
		return val;
	}

	static public double convertArea(int inIndex, int outIndex, double val) {
		if (inIndex == outIndex)
			return val;
		val = val * haConverts[inIndex] / haConverts[outIndex];
		return val;
	}
	static public double convertArea(String inUnit, double val) {
		int inIndex = getAreaIndex(inUnit);
		return convertArea(inIndex, currentArea, val);
	}
	static public double convertArea(int inIndex, double val) {
		return convertArea(inIndex, currentArea, val);
	}
	static public double conversionArea(String inUnit) {
		int inIndex = getAreaIndex(inUnit);
		return conversionArea(inIndex);
	}
	static public double conversionArea(int inIndex) {
		return haConverts[inIndex] / haConverts[currentArea];
	}
	static public double conversionMassArea(String inUnit) {
		int massIndex = getMassFromMassArea(inUnit);
		int areaIndex = getAreaFromMassArea(inUnit);
		return conversionMass(massIndex) / conversionArea(areaIndex);
	}
	static public double convertMassArea(String inUnit, double val) {
		return val * conversionMassArea(inUnit);
	}

	/**
	 * Get the index of the mass
	 * @param unit input value
	 * @return
	 */
	static public int getMassIndex(String unit) {
		for (int i = 0; i < massChoices.length; i++) {
			if (massChoices[i].equals(unit))
				return i;
		}
		return -1;
	}
	/**
	 * Get the index of the area 
	 * @param unit input value
	 * @return
	 */
	static public int getAreaIndex(String unit) {
		if (unit.equals("hectare"))
			unit = "ha";
		for (int i = 0; i < areaChoices.length; i++) {
			if (areaChoices[i].equals(unit))
				return i;
		}
		return -1;
	}
	/**
	 * Set the currently used mass unit
	 * @param val the type of the unit
	 */
	static public void setCurrentMass(String val) {
		int index = getMassIndex(val);
		if (index >= 0)
			currentMass = index;
	}
	/**
	 * Set the currently used mass unit
	 * @param val the type of the unit
	 */
	static public void setCurrentMass(int val) {
		currentMass = val;
	}
	/**
	 * Set the currently used area unit
	 * @param val the type of the unit
	 */
	static public void setCurrentArea(String val) {
		int index = getAreaIndex(val);
		if (index >= 0)
			currentArea = index;
	}
	/**
	 * Set the currently used area unit
	 * @param val the type of the unit
	 */
	static public void setCurrentArea(int val) {
		currentArea = val;
	}

	/**
	 * Get the currently used area as a string
	 * @return the value
	 */
	public static String getCurrentAreaString() {
		return areaChoices[currentArea];
	}

	/**
	 * Get the currently used mass as a string
	 * @return the value
	 */
	public static String getCurrentMassString() {
		return massChoices[currentMass];
	}
	/**
	 * Get the current used values for mass/area as a string
	 * @return the value
	 */
	public static String getCurrentMassAreaString() {
		return getCurrentMassString() + "/" + getCurrentAreaString();
	}
	/**
	 * Get the current type of unit being shown in the range
	 * @return
	 */
	public static String getCurrentTypeString() {
		switch (currentType) {
		case MASS :
			return getCurrentMassString();
		case MASS_AREA :
			return getCurrentMassAreaString();
		case PERCENT :
			return "%";
		}
		return null;
	}
	/**
	 * Get the current type of unit being shown in the range
	 * @return
	 */
	public static String getCurrentTypeString(int type) {
		switch (type) {
		case MASS :
			return getCurrentMassString();
		case MASS_AREA :
			return getCurrentMassAreaString();
		case PERCENT :
			return "%";
		}
		return null;
	}
	/** 
	 * Get the value to convert the current type
	 * @param unitString the input type
	 * @return
	 */
	public static double getConversion(String unitString) {
		if (unitString.equals("%"))
			return 1.0;
		if (unitString.indexOf('/') > 0) {
			return conversionMassArea(unitString);
		}
		return conversionMass(unitString);
	}
	/**
	 * Get the current area index
	 * @return the index
	 */
	public static int getCurrentArea() {
		return currentArea;
	}

	/**
	 * Get the current mass index
	 * @return the index
	 */
	public static int getCurrentMass() {
		return currentMass;
	}

	/**
	 * Get the current type being displayed
	 * @return the type index
	 */
	public static int getCurrentType() {
		return currentType;
	}

	/**
	 * Set the current type to this one
	 * @param i the new type
	 */
	public static void setCurrentType(int i) {
		currentType = i;
	}
	/**
	 * Converts the number to one of a pretty format
	 * @param num the original number
	 * @return the prettied up one
	 */
	public static Number prettyNum(float num) {
		DecimalFormat decimalFormat = new DecimalFormat("0.0000E0");
		String val1 = decimalFormat.format(num);
		Number num1;
		try {
			num1 = decimalFormat.parse(val1);

		} catch (ParseException ex) {
			return new Float(num);
		}
		return num1;
	}
// does not appear to be used 2014
//	/**
//	 * Load the data from the vector of options
//	 * @param data the vector containing the file data
//	 * @param offset the current offset
//	 * @return if it was successful
//	 * @throws IOException
//	 */
//	public static int load(Vector data, int offset) throws IOException {
//		try {
//			String massString = ((String) ((Vector)data.get(offset)).get(0)).trim();
//			String areaString = ((String) ((Vector)data.get(offset)).get(1)).trim();
//			offset = offset + 1;
//
//			setCurrentMass(massString);
//			setCurrentArea(areaString);
//
//		} catch (ClassCastException ex) {
//			throw (new IOException("Load Error: Incorrect format reading unit types"));
//		}
//		return offset;
//	}
	/**
	 * Save the data to the options file
	 * @param data the vector to save it to
	 */
	public static void save(Vector data) {
		// types of the units
		data.add(getCurrentMassString() + "," + getCurrentAreaString());
	}
	/*
	 * Write out the units being used in this dataset
	 */
	public static void export(String fileName){
		Vector data = new Vector();
		data.add("Mass Units: "+getCurrentMassString());
		data.add("Area Units: "+getCurrentAreaString());
		FileHelper.writeDataLines(fileName, data);
	}
	public static boolean isUnitPerArea(String gridUnit){
		Unit gridTopUnit=null;
		Unit gridAreaUnit=null;
		Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
		// see if it is a mass/area value
		if(gridOriginalUnit instanceof ProductUnit){
			// break up the product unit
			Unit topUnit=null;
			Unit bottomUnit=null;
			if(((ProductUnit)gridOriginalUnit).getUnitPow(0)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}else{
				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}
			if(((ProductUnit)gridOriginalUnit).getUnitPow(1)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}else{
				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}
			if(topUnit!=null){
				gridTopUnit = topUnit;
			}

//			if(bottomUnit!=null&&bottomUnit.getSystemUnit().equals(SI.SQUARE_METRE))	
			// JScience spelling change, function name change but comment says getStandardUnit returns 
			// "the system unit this unit is derived from"
			// org.unitsofmeasurement uses getSystemUnit
			if(bottomUnit!=null&&bottomUnit.getSystemUnit().equals(SI.SQUARE_METRE))	// JScience spelling change
			{
				gridAreaUnit=bottomUnit;
			}
			if(gridTopUnit!=null&&gridAreaUnit!=null)return true;	  
		}
		return false;
	}
	public static boolean isConcentration(String gridUnit){
//		Unit gridMassUnit=null;		// not used
//		Unit gridVolumeUnit=null;	// not used
		Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
		// see if it is a ppm value
		if(gridUnit.contains("1/1000000"))return true;
		if(gridUnit.toLowerCase().contains("ppm"))return true;
		// see if it is a mass/volume value
		if(gridOriginalUnit instanceof ProductUnit){
			// break up the product unit
			Unit topUnit=null;
			Unit bottomUnit=null;
			if(((ProductUnit)gridOriginalUnit).getUnitPow(0)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}else{
				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}
			if(((ProductUnit)gridOriginalUnit).getUnitPow(1)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}else{
				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}
			if(topUnit==null||bottomUnit==null)return false;
//			Unit top = topUnit.getSystemUnit(); // JScience changed "getSystemUnit" to "getStandardUnit"
//			Unit bottom = bottomUnit.getSystemUnit();
//			if((topUnit.getSystemUnit().equals(SI.KILOGRAM))&&(bottomUnit.getSystemUnit().equals(SI.CUBIC_METER)||
//					(bottomUnit.getSystemUnit().equals(SI.METER)&&((ProductUnit)gridOriginalUnit).getUnitPow(1)==-3))){
//		Unit top = topUnit.getStandardUnit();
//			Unit bottom = bottomUnit.getStandardUnit();
			// org.unitsofmeasurement uses getSystemUnit
			if((topUnit.getSystemUnit().equals(SI.KILOGRAM))&&(bottomUnit.getSystemUnit().equals(SI.CUBIC_METRE)||
					(bottomUnit.getSystemUnit().equals(SI.METRE)&&((ProductUnit)gridOriginalUnit).getUnitPow(1)==-3))){
				return true;
			} 
		}
		return false;
	}
	public static String getTotalVariable(String gridUnit){
		if(isLength(gridUnit)){
			Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
			// convert to the standard unit
			//Unit standardOne = gridOriginalUnit.getSystemUnit();
			// raise to the third power
			Unit finalUnit = gridOriginalUnit.pow(3);
			return finalUnit.toString();
		}else{
			return getMassFromMassAreaString(gridUnit);
		}
	}
	public static boolean isLength(String gridUnit){

		Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
//		if(gridOriginalUnit.getSystemUnit().equals(SI.METER))return true;	// JScience changed getSystemUnit to getStandardUnit
		if(gridOriginalUnit.getSystemUnit().equals(SI.METRE))		// org.unitsofmeasurement uses getSystemUnit
			return true;

		return false;
	}
	public static Unit getTopUnit(String gridUnit){

		Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
		// see if it is a mass/area value
		if(gridOriginalUnit instanceof ProductUnit){
			// break up the product unit
			Unit topUnit=null;
			Unit bottomUnit=null;
			if(((ProductUnit)gridOriginalUnit).getUnitPow(0)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}
			if(((ProductUnit)gridOriginalUnit).getUnitPow(1)>=1){
				topUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}
			if(topUnit!=null){
				return topUnit;
			}

		}
		return null;
	}
	public static Unit getAreaUnit(String gridUnit){

		Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
		// see if it is a mass/area value
		if(gridOriginalUnit instanceof ProductUnit){
			// break up the product unit
			Unit topUnit=null;
			Unit bottomUnit=null;
			if(((ProductUnit)gridOriginalUnit).getUnitPow(0)<1){

				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(0);
			}
			if(((ProductUnit)gridOriginalUnit).getUnitPow(1)<1){

				bottomUnit=((ProductUnit)gridOriginalUnit).getUnit(1);
			}

			if(bottomUnit!=null&&bottomUnit.getSystemUnit().equals(SI.SQUARE_METRE)) // JScience changed spelling, getSystemUnit to getStandardUnit
			{
				return bottomUnit;
			}

		}

		return null;
	}
}
