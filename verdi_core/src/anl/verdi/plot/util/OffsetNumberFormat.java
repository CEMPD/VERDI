package anl.verdi.plot.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class OffsetNumberFormat extends NumberFormat {

	private NumberFormat formatter;
	private int offset;

	public OffsetNumberFormat(NumberFormat formatter, int offset) {
		if (formatter == null) {
			this.formatter = DecimalFormat.getInstance();	
		} else {
			this.formatter = formatter;
		}
		this.offset = offset;
	}


	public Object clone() {
		return formatter.clone();
	}

	public boolean equals(Object obj) {
		return formatter.equals(obj);
	}

	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return formatter.format(number + offset, toAppendTo, pos);
	}

	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return formatter.format(number + offset, toAppendTo, pos);
	}

	public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
		return formatter.format(number, toAppendTo, pos);
	}

	public Currency getCurrency() {
		return formatter.getCurrency();
	}

	public int getMaximumFractionDigits() {
		return formatter.getMaximumFractionDigits();
	}

	public int getMaximumIntegerDigits() {
		return formatter.getMaximumIntegerDigits();
	}

	public int getMinimumFractionDigits() {
		return formatter.getMinimumFractionDigits();
	}

	public int getMinimumIntegerDigits() {
		return formatter.getMinimumIntegerDigits();
	}

	public RoundingMode getRoundingMode() {
		return formatter.getRoundingMode();
	}

	public int hashCode() {
		return formatter.hashCode();
	}

	public boolean isGroupingUsed() {
		return formatter.isGroupingUsed();
	}

	public boolean isParseIntegerOnly() {
		return formatter.isParseIntegerOnly();
	}

	public Number parse(String source) throws ParseException {
		return formatter.parse(source);
	}

	public Number parse(String source, ParsePosition parsePosition) {
		return formatter.parse(source, parsePosition);
	}

	public void setCurrency(Currency currency) {
		formatter.setCurrency(currency);
	}

	public void setGroupingUsed(boolean newValue) {
		formatter.setGroupingUsed(newValue);
	}

	public void setMaximumFractionDigits(int newValue) {
		formatter.setMaximumFractionDigits(newValue);
	}

	public void setMaximumIntegerDigits(int newValue) {
		formatter.setMaximumIntegerDigits(newValue);
	}

	public void setMinimumFractionDigits(int newValue) {
		formatter.setMinimumFractionDigits(newValue);
	}

	public void setMinimumIntegerDigits(int newValue) {
		formatter.setMinimumIntegerDigits(newValue);
	}

	public void setParseIntegerOnly(boolean value) {
		formatter.setParseIntegerOnly(value);
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		formatter.setRoundingMode(roundingMode);
	}
}
