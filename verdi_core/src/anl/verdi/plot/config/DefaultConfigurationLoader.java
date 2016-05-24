package anl.verdi.plot.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DefaultConfigurationLoader extends DefaultHandler2 {

	private static final String PLOT_CONFIG_EL = "PlotConfig";
	private static final String TITLE = "title";
	private static final String SUBTITLE_1 = "subtitle1";
	private static final String SUBTITLE_2 = "subtitle2";
	private static final String LEGEND_UNITS = "legendUnits";

	/*
	PlotConfig title="$title" subtitle1="$subtitle1" subtitle2="$subtitle2"
	legendUnits="$legendUnits">
	 */

	private String subtitle1, subtitle2, legendUnits, title;


	public String getSubtitle1() {
		return subtitle1;
	}

	public String getSubtitle2() {
		return subtitle2;
	}

	public String getLegendUnits() {
		return legendUnits;
	}


	public String getTitle() {
		return title;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals(PLOT_CONFIG_EL)) {
			subtitle1 = attributes.getValue(SUBTITLE_1);
			subtitle2 = attributes.getValue(SUBTITLE_2);
			legendUnits = attributes.getValue(LEGEND_UNITS);
			title = attributes.getValue(TITLE);
		}
	}
}
