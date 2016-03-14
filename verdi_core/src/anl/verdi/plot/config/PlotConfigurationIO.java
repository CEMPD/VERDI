package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.ColorMapLoader;

/**
 * Interface for classes that represent a plot configuration
 * and can save and load the configuration data.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotConfigurationIO extends DefaultHandler2 {

	private Map<String, StringConvertor> convertors = new HashMap<String, StringConvertor>();
	private PlotConfiguration config;
	private DelegatingHandler handler;

	private static class StringConv implements StringConvertor<String> {
		public String convertFromString(String str) {
			return str;
		}

		public String convertToString(Object obj) {
			return obj.toString();
		}
	}

	private static class ColorConvertor implements StringConvertor<Color> {

		// assumes String is the string rep of the
		// int returned by Color.getRBG
		public Color convertFromString(String str) {
			return new Color(Integer.parseInt(str));
		}

		public String convertToString(Object obj) {
			Color color = (Color) obj;
			return String.valueOf(color.getRGB());
		}
	}

	private static class FontConvertor implements StringConvertor<Font> {

		// uses Font.decode
		public Font convertFromString(String str) {
			return Font.decode(str);
		}

		// String in the form expected by Font.decode
		public String convertToString(Object obj) {
			Font font = (Font) obj;
			String strStyle;
			if (font.isBold()) {
				strStyle = font.isItalic() ? "bolditalic" : "bold";
			} else {
				strStyle = font.isItalic() ? "italic" : "plain";
			}

			return font.getName() + "-" + strStyle + "-" + font.getSize();
		}
	}

	private static class BooleanConvertor implements StringConvertor<Boolean> {

		public Boolean convertFromString(String str) {
			return Boolean.valueOf(str);
		}

		public String convertToString(Object obj) {
			return obj.toString();
		}
	}
	
	private static class IntegerConvertor implements StringConvertor<Integer> {

		public Integer convertFromString(String str) {
			return Integer.valueOf(str);
		}

		public String convertToString(Object obj) {
			return obj.toString();
		}
	}

	private class DelegatingHandler extends DefaultHandler2 {
		DefaultHandler2 curHandler;
		ColorMapLoader cMapLoader = new ColorMapLoader();

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			curHandler.characters(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals(ColorMapLoader.COLOR_MAP_EL)) {
				curHandler = cMapLoader;
			}
			curHandler.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			curHandler.endElement(uri, localName, qName);
			if (qName.equals(ColorMapLoader.COLOR_MAP_EL)) curHandler = PlotConfigurationIO.this;
		}
	}

	public PlotConfigurationIO() {
		addConvertor(Font.class.getName(), new FontConvertor());
		addConvertor(String.class.getName(), new StringConv());
		addConvertor(Color.class.getName(), new ColorConvertor());
		addConvertor(Boolean.class.getName(), new BooleanConvertor());
		addConvertor(Integer.class.getName(), new IntegerConvertor());
		handler = new DelegatingHandler();
		handler.curHandler = this;
	}


	/**
	 * Adds a converter to convert some class to and from a String representation.
	 *
	 * @param className the class this converter operates on
	 * @param convertor the converter
	 */
	public void addConvertor(String className, StringConvertor convertor) {
		convertors.put(className, convertor);
	}

	private java.util.List<Property> processMap(Map<String, Object> map) {
		java.util.List<Property> list = new ArrayList<Property>();
		for (Map.Entry entry : map.entrySet()) {
			Object value = entry.getValue();
			Class valClass = value.getClass();
			StringConvertor conv = convertors.get(value.getClass().getName());
			if (conv != null) value = conv.convertToString(value);
			Property prop = new Property((String) entry.getKey(), valClass, value);
			list.add(prop);
		}

		return list;
	}

	/**
	 * Saves a plot's configuration data to the specified file.
	 *
	 * @param file the file to save the configuration to
	 * @param map  the configuration data to save
	 * @throws IOException if an error occurs during saving.
	 */
	void saveConfiguration(File file, Map<String, Object> map) throws IOException {
		java.util.List<Property> props = processMap(map);
		VelocityContext context = new VelocityContext();	// 2014 keep
		// 2014 problems finding the PlotConfig.vt file within core.java
		String template = getClass().getPackage().getName();	// 2014 from LambertWKTCreator.java
		ClassLoader loader = Thread.currentThread().getContextClassLoader();	// 2014 from LambertWKTCreator.java
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());	// 2014 from LambertWKTCreator.java
		template = template.replace('.', '/');	// 2014 keep 
		template = template + '/' + "PlotConfig.vt";	// 2014 keep
		ColorMap colorMap = (ColorMap) map.get(TilePlotConfiguration.COLOR_MAP);
		if (colorMap != null) context.put("map", colorMap);
		context.put("properties", props);
		Writer writer = new FileWriter(file);	// 2014 NOTE: LambertWKTCreator uses a StringWriter() instead
		try {
			Velocity.mergeTemplate(template, "UTF-8", context, writer);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IOException("Error merging template", ex);
		} finally {
			if (writer != null) writer.close();
			Thread.currentThread().setContextClassLoader(loader); // 2014 from LambertWKTCreator.java
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//<property type="java.awt.Color" key="anl.verdi.plot.config.PlotConfiguration.title_color" value="-16777216" />
		if (qName.equals("property")) {
			String type = attributes.getValue("type");
			String key = attributes.getValue("key");
			String value = attributes.getValue("value");
			StringConvertor conv = convertors.get(type);
			Object val = value;
			if (conv != null) val = conv.convertFromString(value);
			config.putObject(key, val);
		}
	}

	/**
	 * Loads the configuration data from the specified file into
	 * a PlotConfiguration and returns that configuration.
	 *
	 * @param file contains the configuration data
	 * @return the loaded PlotConfiguration
	 * @throws IOException if an error occurs during loading.
	 */
	public PlotConfiguration loadConfiguration(File file) throws IOException {
		config = new PlotConfiguration();
		config.setPreviousFolder(file.getParentFile());
		
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream inputStream = file.toURI().toURL().openStream();
			parser.parse(inputStream, handler);
			inputStream.close();

			if (handler.cMapLoader.getColorMap() != null) {
				config.putObject(TilePlotConfiguration.COLOR_MAP, handler.cMapLoader.getColorMap());
			}

		} catch (ParserConfigurationException e) {
			throw new IOException("Error while loading configuration", e);
		} catch (SAXException e) {
			throw new IOException("Error while loading configuration", e);
		}
		
		// set defaults for new parameters that might not be in existing config files
		if (config.getObject(PlotConfiguration.LEGEND_SHOW) == null) {
			config.putObject(PlotConfiguration.LEGEND_SHOW, true);
		}

		return config;
	}
}
