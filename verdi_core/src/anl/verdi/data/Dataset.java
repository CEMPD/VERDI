package anl.verdi.data;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Encapsulates a set of data. Each Dataset can have an alias to help with
 * identifying its variables from those of other datasets.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */

// TODO: the dataset only support Time, Z besides X, Y, need to extend it to real n-D datasets

public interface Dataset {

	static final String NULL_ALIAS = "anl.verdi.data.DataSet.NULL_ALIAS";
	static final int SINGLE_DATASET = -1;

	/**
	 * Sets the alias for this Dataset.
	 *
	 * @param alias the alias for this Dataset.
	 */
	void setAlias(String alias);

	/**
	 * Gets the alias of this Dataset. If no alias has been assigned the alias
	 * will be {@link #NULL_ALIAS NULL_ALIAS}.
	 *
	 * @return the alias of this Dataset.
	 */
	String getAlias();

	/**
	 * Gets the list of variable names in this Dataset.
	 *
	 * @return the list of variable names in this Dataset.
	 */
	List<String> getVariableNames();

	/**
	 * Gets the list of variables in this Dataset.
	 *
	 * @return the list of variables in this Dataset.
	 */
	List<Variable> getVariables();

	/**
	 * Gets the named variable.
	 *
	 * @param name the name of the variable to get
	 * @return the named variable
	 */
	Variable getVariable(String name);

	/**
	 * Gets the coordindate Axes for this Dataset.
	 *
	 * @return the coordindate Axes for this Dataset.
	 */
	Axes<CoordAxis> getCoordAxes();

	/**
	 *
	 * @return true if there is a time axis, otherwise false
	 */
	boolean hasTimeAxis();

	/**
	 * See if there is a z (e.g. layer) axis
	 *
	 * @return true if there is a Z axis, otherwise false
	 */
	boolean hasZAxis();

	/**

	 * @return true if there is a X axis, otherwise false
	 */
	boolean hasXAxis();

	/**
	 * @return true if there is a Y axis, otherwise false
	 */
	boolean hasYAxis();

	/**
	 * Gets the url of this Dataset.
	 *
	 * @return the url of this Dataset.
	 */
	URL getURL();

	/**
	 * Gets the index of this Dataset inside its URL, or
	 * Dataset.SINGLE_DATASET if it is the only dataset
	 * inside the URL.
	 *
	 * @return the index of this Dataset inside its URL
	 */
	int getIndexInURL();


	/**
	 * Gets the name of this Dataset.
	 *
	 * @return the name of this Dataset.
	 */
	String getName();

	/**
	 * Closes this dataset. It will have to be recreated to be used again.
	 *
	 * @throws IOException if an error occurs while closing the dataset
	 */
	public void close() throws IOException;

	/**
	 * Whether or not this dataset represents an observational dataset
	 *
	 * @return true if this contains obs data, otherwise false.
	 */
	public boolean isObs();


	/**
	 * Return a dataset meta data including projection information
	 *
	 * @return a dataset meta data including projection information.
	 */
	public DatasetMetadata getMetadata();
	
	public int getNetcdfCovn();
	
	public void setNetcdfConv(int conv);

}
