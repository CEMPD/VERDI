package saf.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Simple SAX parsers that reads jpf format plugin files for attributes.
 * 
 * @author Nick Collier
 *         Date: Dec 8, 2008 9:19:26 AM
 */
public class PluginReader extends DefaultHandler2 {

  private static final String ATTRIBUTE_TAG = "attribute";
  private static final String PLUGIN_TAG = "plugin";

  private static final String ID_ATTRIBUTE = "id";
  private static final String VALUE_ATTRIBUTE = "value";

  private URL url;
  List<PluginAttributes> attributes = new ArrayList<PluginAttributes>();
  private PluginAttributes current = null;


  /**
   * Creates a PluginReader to read the specified URL.
   *
   * @param url the url to the file to read
   */
  public PluginReader(URL url) {
    this.url = url;
  }

  /**
   * Parse the plugin file and return a PluginAttributes containing the parsed data.
   *
   *
   * @return PluginAttributes containing the parsed data
   *
   * @throws ParserConfigurationException if there a parser cannot be configured
   * @throws SAXException if there is an error during parsing
   * @throws IOException if there is an error during parsing
   */
  public PluginAttributes parse() throws ParserConfigurationException, SAXException, IOException {
    attributes.clear();
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    InputStream inputStream = url.openStream();
    parser.parse(inputStream, this);
    inputStream.close();

    return current;
  }

  @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals(ATTRIBUTE_TAG)) {
      current.addAttribute(attributes.getValue(ID_ATTRIBUTE),  attributes.getValue(VALUE_ATTRIBUTE));
    } else if (qName.equals(PLUGIN_TAG)) {
      current = new PluginAttributes(attributes.getValue(ID_ATTRIBUTE));
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    //System.out.println("publicId = " + publicId);
    return new InputSource(new StringReader(""));
  }

  @Override
  public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
    //System.out.println("publicId = " + publicId);
    return new InputSource(new StringReader(""));
  }
}
