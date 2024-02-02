package anl.verdi.plot.color;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import anl.verdi.plot.color.ColorMap.ScaleType;

/**
 * Loads a ColorMap from an appropriate xml file.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ColorMapLoader extends DefaultHandler2 {

	public static final String COLOR_MAP_EL = "ColorMap";
	private static final String PALETTE_EL = "Palette";
	private static final String COLOR_EL = "Color";
	private static final String STEP_EL = "Step";
	private static final String FORMAT_EL = "Format";
	private static final String DESCRIPTION = "description";
	private static final String REVERSE_COLORS = "reverseColors";
	private static final String INTERVAL_TYPE = "intervalType";
	private static final String SCALE_TYPE = "scaleType";
	//private static final String STAT_TYPE = "statType";
	private static final String LOG_BASE = "logBase";
	private static final String PALETTE_TYPE = "paletteType";
	private static final String MIN = "min";
	private static final String MAX = "max";
	private static final String FORMAT_TYPE = "type";
	private static final String FORMAT_PATTERN = "pattern";

	private ColorMap map;
	private String intervalType, scaleType, paletteType, logBase;
	private double min, max;
	private String formatString;
	private List<Color> colors = new ArrayList<Color>();
	private List<Double> intervals = new ArrayList<Double>();
	private List<Double> logIntervals = new ArrayList<Double>();
	private boolean colorOn = false;
	private boolean scaleOn = false;
	private String description;
	private boolean reverseColors = false;

	public ColorMap load(File file) throws IOException, ParserConfigurationException, SAXException {
		return load(file.toURI().toURL());
	}

	public ColorMap load(URL url) throws IOException, SAXException, ParserConfigurationException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		InputStream inputStream = url.openStream();
		parser.parse(inputStream, this);
		inputStream.close();
		return map;
	}

	public ColorMap getColorMap() {
		return map;
	}

	/**
	 * Receive notification of the start of an element.
	 * <p/>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the start of
	 * each element (such as allocating a new tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri        The Namespace URI, or the empty string if the
	 *                   element has no Namespace URI or if Namespace
	 *                   processing is not being performed.
	 * @param localName  The local name (without prefix), or the
	 *                   empty string if Namespace processing is not being
	 *                   performed.
	 * @param qName      The qualified name (with prefix), or the
	 *                   empty string if qualified names are not available.
	 * @param attributes The attributes attached to the element.  If
	 *                   there are no attributes, it shall be an empty
	 *                   Attributes object.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#startElement
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals(COLOR_MAP_EL)) {
			intervalType = attributes.getValue(INTERVAL_TYPE);
			scaleType = attributes.getValue(SCALE_TYPE);
			//statType = attributes.getValue(STAT_TYPE);
			logBase = attributes.getValue(LOG_BASE);
			paletteType = attributes.getValue(PALETTE_TYPE);
			try {
				min = Double.parseDouble(attributes.getValue(MIN));
				max = Double.parseDouble(attributes.getValue(MAX));
			} catch (NumberFormatException ex) {
				throw new SAXException("Invalid numeric value", ex);
			}
		} else if (qName.equals(PALETTE_EL)) {
			description = attributes.getValue(DESCRIPTION);
			String reverseColorsStr = attributes.getValue(REVERSE_COLORS);
			if (Boolean.parseBoolean(reverseColorsStr)) 
				reverseColors = true;
			else
				reverseColors = false;
		} else if (qName.equals(COLOR_EL)) {
			colorOn = true;
		} else if (qName.equals(STEP_EL)) {
			scaleOn = true;
		} else if (qName.equals(FORMAT_EL)) {
			formatString = attributes.getValue(FORMAT_PATTERN);
		}
	}


	/**
	 * Receive notification of the end of an element.
	 * <p/>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the end of
	 * each element (such as finalising a tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri       The Namespace URI, or the empty string if the
	 *                  element has no Namespace URI or if Namespace
	 *                  processing is not being performed.
	 * @param localName The local name (without prefix), or the
	 *                  empty string if Namespace processing is not being
	 *                  performed.
	 * @param qName     The qualified name (with prefix), or the
	 *                  empty string if qualified names are not available.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#endElement
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals(COLOR_EL)) colorOn = false;
		if (qName.equals(STEP_EL)) scaleOn = false;
		if (qName.equals(COLOR_MAP_EL)) {
			Palette pal = new Palette(colors.toArray(new Color[colors.size()]), description, reverseColors);
			ScaleType scaleTypeObj = ColorMap.getScaleType(scaleType);
			map = new ColorMap(pal, intervals, logIntervals, scaleTypeObj);
			map.setPaletteType(ColorMap.getPaletteType(paletteType));
			map.setIntervalType(ColorMap.getIntervalType(intervalType));
			map.setScaleType(scaleTypeObj);
			map.setMinMax(min, max, true);
			
			try{
				if (ColorMap.getScaleType(scaleType) == ColorMap.ScaleType.LOGARITHM) {
					double baseValue = Math.E;
					if ( !logBase.trim().equalsIgnoreCase("E")) {
						baseValue = Double.parseDouble( logBase);
					}
					map.setLogBase(baseValue);
				}
			} catch (NumberFormatException ex){
				
			}
			
			try {
				map.setFormatString(formatString);
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}
	}

	/**
	 * Receive notification of character data inside an element.
	 * <p/>
	 * <p>By default, do nothing.  Application writers may override this
	 * method to take specific actions for each chunk of character data
	 * (such as adding the data to a node or buffer, or printing it to
	 * a file).</p>
	 *
	 * @param ch     The characters.
	 * @param start  The start position in the character array.
	 * @param length The number of characters to use from the
	 *               character array.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#characters
	 */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (colorOn) {
			String num = new String(ch, start, length).trim();
			try {
				Color c = null;
				if (num.startsWith("#"))
					c = Color.decode(num);
				else
					c = new Color(Integer.parseInt(num));
				colors.add(c);
			} catch (NumberFormatException ex) {
				throw new SAXException("Invalid color value", ex);
			}
		}
		
		if (scaleOn) {
			String num = new String(ch, start, length).trim();
			try {
				Double val = new Double(num);
				if (ColorMap.getScaleType(scaleType) == ColorMap.ScaleType.LOGARITHM) 
					logIntervals.add(val);
				else
					intervals.add(val);
			} catch (NumberFormatException ex) {
				throw new SAXException("Invalid scale value", ex);
			}
		}
	}
}
