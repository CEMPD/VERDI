package anl.verdi.plot.types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import anl.verdi.plot.color.ColorMapLoader;
import anl.verdi.plot.config.DefaultConfigurationLoader;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.TilePlotConfiguration;

/**
 * Configurator for AbstractTilePlots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TilePlotConfigurationIO extends DefaultHandler2  {

	private AbstractTilePlot plot;
	private ColorMapLoader cMapLoader = new ColorMapLoader();
	private DefaultConfigurationLoader defaultHandler = new DefaultConfigurationLoader();
	private DefaultHandler2 curHandler = defaultHandler;

	public TilePlotConfigurationIO(AbstractTilePlot plot) {
		this.plot = plot;
	}

	/**
	 * Loads the configuration data from the specified file a
	 * Plot most likely set in the constructor.
	 *
	 * @param file contains the configuration data
	 */
	public void loadConfiguration(File file) throws IOException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream inputStream = file.toURI().toURL().openStream();
			parser.parse(inputStream, this);
			inputStream.close();

			TilePlotConfiguration config = new TilePlotConfiguration();
			config.setColorMap(cMapLoader.getColorMap());
			config.setTitle(defaultHandler.getTitle());
			config.setSubtitle1(defaultHandler.getSubtitle1());
			config.setSubtitle2(defaultHandler.getSubtitle2());
			config.setUnits(defaultHandler.getLegendUnits());
			plot.configure(config);

		} catch (ParserConfigurationException e) {
			throw new IOException("Error while loading configuration", e);
		} catch (SAXException e) {
			throw new IOException("Error while loading configuration", e);
		}
	}

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
	}

	/**
	 * Saves a plot's configuration data to the specified file.
	 *
	 * @param file the file to save the configuration to
	 */
	public void saveConfiguration(File file) throws IOException {
		VelocityContext context = new VelocityContext();
		String template = PlotConfigurationIO.class.getPackage().getName();
		template = template.replace('.', '/');
		template = template + "/TilePlotConfig.vt";

		JFreeChart chart = plot.chart;
		context.put("title", chart.getTitle().getText());
		TextTitle title = (TextTitle) chart.getSubtitle(plot.subTitle1Index);
		context.put("subtitle1", title.getText());
		title = (TextTitle) chart.getSubtitle(plot.subTitle2Index);
		context.put("subtitle2", title.getText());
		context.put("legendUnits", plot.units);
		context.put("map", plot.getColorMap());

		Writer writer = new FileWriter(file);
		try {
			Velocity.mergeTemplate(template, "UTF-8", context, writer);
		} catch (Exception ex) {
			throw new IOException("Error merging template", ex);
		} finally {
			if (writer != null) writer.close();
		}
	}
}
