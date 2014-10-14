package anl.verdi.area;

// 2014 class appears to not be used
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Implementation of a ShapeFile
// *
// * @author Mary Ann Bitz
// * @version $Revision$ $Date$
// */
//public class ShapeFile implements AreaFile {
//
//	protected String alias = NULL_ALIAS;
//	protected URL url;
//
//
//	/**
//	 * Creates a Shape File.
//	 *
//	 * @param url the url of the area file
//	 */
//	protected ShapeFile(URL url) {
//		this.url = url;
//	}
//
//	/**
//	 * Gets the url of this area file.
//	 *
//	 * @return the url of this area file.
//	 */
//	public URL getURL() {
//		return url;
//	}
//
//	/**
//	 * Gets the alias of this area file. If no alias has been assigned the alias will be
//	 * {@link #NULL_ALIAS NULL_ALIAS}.
//	 *
//	 * @return the alias of this area file.
//	 */
//	public String getAlias() {
//		return alias;
//	}
//
//	/**
//	 * Sets the alias for this area file.
//	 *
//	 * @param alias the alias for this area file.
//	 */
//	public void setAlias(String alias) {
//		this.alias = alias;
//	}
//
//	public List<Area> getAreas() {
//		return new ArrayList<Area>();
//	}
//
//	/**
//	 * Gets the name of this Shape File.  2014: returns "" only, not name of a shapefile
//	 *
//	 * @return "" (should be the name of this ShapeFile)
//	 */
//	public String getName() {
//		return "";
//	}
//
//	/**
//	 * Closes this area file. It will have to be
//	 * recreated to be used again.
//	 */
//	public void close() throws IOException {}
//
//	/**
//	 * Gets the index in this URL.  2014: returns -1 only, does not look for the value to return
//	 *
//	 * @return -1 (constant)
//	 */
//	@Override
//	public int getIndexInURL() {
//		// TODO Auto-generated method stub
//		return -1;
//	}
//
//	@Override
//	public List<String> getAreaNames() {
//		List<Area> myAreas = getAreas();
//		List<String> areaNames = new ArrayList<String>();
//		for(int i=0; i<myAreas.size(); i++)
//		{
//			areaNames.set(i, myAreas.get(i).getName());
//		}
//		return areaNames;
//	}
//
//
//
//
//}
