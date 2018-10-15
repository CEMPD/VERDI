package anl.verdi.plot.types;

import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;

/**
 * Offsets the axis labels by 1, so that the origin appears to be 1,1.
 * @author Nick Collier
 */

public class AxisNumberFormatter extends NumberFormat {

	private DecimalFormat format;

	public AxisNumberFormatter(DecimalFormat format) {
		this.format = format;
	}


	public void applyLocalizedPattern(String pattern) {
		format.applyLocalizedPattern(pattern);
	}

	public void applyPattern(String pattern) {
		format.applyPattern(pattern);
	}

	public Object clone() {
		return format.clone();
	}

	public boolean equals(Object obj) {
		return obj instanceof AxisNumberFormatter && format.equals(obj);
	}

	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
		return format.format(number + 1, result, fieldPosition);
	}

	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
		return format.format(number + 1, result, fieldPosition);
	}

	public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
		double val = ((Number) number).doubleValue() + 1;
		return format.format(val, toAppendTo, pos);
	}

	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		return format.formatToCharacterIterator(obj);
	}

	public Currency getCurrency() {
		return format.getCurrency();
	}

	public DecimalFormatSymbols getDecimalFormatSymbols() {
		return format.getDecimalFormatSymbols();
	}

	public int getGroupingSize() {
		return format.getGroupingSize();
	}

	public int getMaximumFractionDigits() {
		return format.getMaximumFractionDigits();
	}

	public int getMaximumIntegerDigits() {
		return format.getMaximumIntegerDigits();
	}

	public int getMinimumFractionDigits() {
		return format.getMinimumFractionDigits();
	}

	public int getMinimumIntegerDigits() {
		return format.getMinimumIntegerDigits();
	}

	public int getMultiplier() {
		return format.getMultiplier();
	}

	public String getNegativePrefix() {
		return format.getNegativePrefix();
	}

	public String getNegativeSuffix() {
		return format.getNegativeSuffix();
	}

	public String getPositivePrefix() {
		return format.getPositivePrefix();
	}

	public String getPositiveSuffix() {
		return format.getPositiveSuffix();
	}

	public RoundingMode getRoundingMode() {
		return format.getRoundingMode();
	}

	public int hashCode() {
		return format.hashCode();
	}

	public boolean isDecimalSeparatorAlwaysShown() {
		return format.isDecimalSeparatorAlwaysShown();
	}

	public boolean isParseBigDecimal() {
		return format.isParseBigDecimal();
	}

	public Number parse(String text, ParsePosition pos) {
		return format.parse(text, pos);
	}

	public void setCurrency(Currency currency) {
		format.setCurrency(currency);
	}

	public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
		format.setDecimalFormatSymbols(newSymbols);
	}

	public void setDecimalSeparatorAlwaysShown(boolean newValue) {
		format.setDecimalSeparatorAlwaysShown(newValue);
	}

	public void setGroupingSize(int newValue) {
		format.setGroupingSize(newValue);
	}

	public void setMaximumFractionDigits(int newValue) {
		format.setMaximumFractionDigits(newValue);
	}

	public void setMaximumIntegerDigits(int newValue) {
		format.setMaximumIntegerDigits(newValue);
	}

	public void setMinimumFractionDigits(int newValue) {
		format.setMinimumFractionDigits(newValue);
	}

	public void setMinimumIntegerDigits(int newValue) {
		format.setMinimumIntegerDigits(newValue);
	}

	public void setMultiplier(int newValue) {
		format.setMultiplier(newValue);
	}

	public void setNegativePrefix(String newValue) {
		format.setNegativePrefix(newValue);
	}

	public void setNegativeSuffix(String newValue) {
		format.setNegativeSuffix(newValue);
	}

	public void setParseBigDecimal(boolean newValue) {
		format.setParseBigDecimal(newValue);
	}

	public void setPositivePrefix(String newValue) {
		format.setPositivePrefix(newValue);
	}

	public void setPositiveSuffix(String newValue) {
		format.setPositiveSuffix(newValue);
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		format.setRoundingMode(roundingMode);
	}

	public String toLocalizedPattern() {
		return format.toLocalizedPattern();
	}

	public String toPattern() {
		return format.toPattern();
	}
}