package anl.verdi.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Utilities {

	private static NumberFormat nFormat = DecimalFormat.getInstance();
	private static NumberFormat eFormat = new DecimalFormat("0.###E0");

	static {
		nFormat.setMaximumFractionDigits(3);
	}

	public static String formatDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(date);
	}
	public static String formatDate(GregorianCalendar date) {	// 2014 added to handle GregorianCalendar class
		SimpleDateFormat formatSD = new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss z");
		formatSD.setCalendar(date);
		formatSD.setTimeZone(TimeZone.getTimeZone("UTC"));
		String aString = formatSD.format(date.getTime());
		return aString;
	}

	public static String formatShortDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(date);
	}
	
	public static String formatShortDate(GregorianCalendar date)
	{	// 2014 add support for GregorianCalendar because many Date-related functions are deprecated
		// NOTE: Make the following function calls in this order
		SimpleDateFormat formatSD = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		formatSD.setCalendar(date);
		formatSD.setTimeZone(TimeZone.getTimeZone("UTC"));
		String aString = formatSD.format(date.getTime());
		return aString;
	}

	public static String formatNumber(double val) {
		if (val < .001 && val != 0) return eFormat.format(val);
		return nFormat.format(val);
	}

	public static String formatLat(double latitude, int fractions) {
		nFormat.setMaximumFractionDigits(fractions);
		String val = formatNumber(Math.abs(latitude));
		nFormat.setMaximumFractionDigits(3);
		if (latitude < 0) return val + "S";
		else return val + "N";
	}

	public static String formatLon(double longitude, int fractions) {
		nFormat.setMaximumFractionDigits(fractions);
		String val = formatNumber(Math.abs(longitude));
		nFormat.setMaximumFractionDigits(3);
		if (longitude < 0) return val + "W";
		else return val + "E";
	}
	
	public static boolean is64bitWindows() {
		boolean is64bitWin = false;
		if (System.getProperty("os.name").contains("Windows")) {
		    is64bitWin = (System.getProperty("os.arch").indexOf("64") != -1);
		} 
		return is64bitWin;
	}
}
