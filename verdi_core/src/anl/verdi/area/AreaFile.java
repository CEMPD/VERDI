package anl.verdi.area;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Encapsulates a set of data about an area file. 
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public interface AreaFile {

	static final String NULL_ALIAS = "anl.verdi.data.DataSet.NULL_ALIAS";
	static final int SINGLE_DATASET = -1;

	/**
	 * Sets the alias for this area file.
	 *
	 * @param alias the alias for this area file.
	 */
	void setAlias(String alias);

	/**
	 * Gets the alias of this AreaFile. If no alias has been assigned the alias
	 * will be {@link #NULL_ALIAS NULL_ALIAS}.
	 *
	 * @return the alias of this AreaFile.
	 */
	String getAlias();

	/**
	 * Gets the list of area names in this AreaFile.
	 *
	 * @return the list of area names in this area file.
	 */
	List<String> getAreaNames();

	/**
	 * Gets the list of areas in this Area File.
	 *
	 * @return the list of variables in this area file.
	 */
	List<Area> getAreas();


	/**
	 * Gets the url of this AreaFile.
	 *
	 * @return the url of this AreaFile.
	 */
	URL getURL();

	/**
	 * Gets the index of this AreaFile inside its URL, or
	 * Dataset.SINGLE_DATASET if it is the only dataset
	 * inside the URL.
	 *
	 * @return the index of this Dataset inside its URL
	 */
	int getIndexInURL();


	/**
	 * Gets the name of this AreaFile.
	 *
	 * @return the name of this AreaFile.
	 */
	String getName();

	/**
	 * Closes this AreaFile. It will have to be recreated to be used again.
	 *
	 * @throws IOException if an error occurs while closing the AreaFile
	 */
	public void close() throws IOException;





}
