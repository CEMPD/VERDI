package anl.verdi.data;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of a Dataset
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractDataset implements Dataset {

	protected String alias = NULL_ALIAS;
	protected URL url;


	/**
	 * Creates an AbstractDataset.
	 *
	 * @param url the url of the dataset
	 */
	protected AbstractDataset(URL url) {
		this.url = url;
	}

	/**
	 * Gets the url of this Dataset.
	 *
	 * @return the url of this Dataset.
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Gets the alias of this Dataset. If no alias has been assigned the alias will be
	 * {@link #NULL_ALIAS NULL_ALIAS}.
	 *
	 * @return the alias of this Dataset.
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias for this Dataset.
	 *
	 * @param alias the alias for this Dataset.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public List<String> getVariableNames() {
		return new ArrayList<String>();
	}

	/**
	 * Gets the named variable. NOTE: Always returns NULL.
	 *
	 * @param name the name of the variable to get
	 * @return the named variable
	 */
	public Variable getVariable(String name) {
		return null;  //todo implement method
	}

	public List<Variable> getVariables() {
		return new ArrayList<Variable>();
	}

	/**
	 * Gets the coordindate Axes for this Dataset.
	 *
	 * @return the coordindate Axes for this Dataset.
	 */
	public Axes<CoordAxis> getCoordAxes() {
		return new Axes<CoordAxis>();
	}

	/**
	 * @return true if there is a time axis, otherwise false
	 */
	public boolean hasTimeAxis() {
		return getCoordAxes().getTimeAxis() != null;
	}

	/**
	 * @return true if there is a X axis, otherwise false
	 */
	public boolean hasXAxis() {
		return getCoordAxes().getXAxis() != null;
	}

	/**
	 * @return true if there is a Y axis, otherwise false
	 */
	public boolean hasYAxis() {
		return getCoordAxes().getYAxis() != null;
	}

	/**
	 * See if there is a z (e.g. layer) axis
	 *
	 * @return true if there is a Z axis, otherwise false
	 */
	public boolean hasZAxis() {
		return getCoordAxes().getZAxis() != null;
	}


	/**
	 * Gets the name of this Dataset. NOTE: Always returns an empty String.
	 *
	 * @return the name of this Dataset;
	 */
	public String getName() {
		return "";
	}

	/**
	 * Closes this dataset. It will have to be
	 * recreated to be used again.
	 */
	public void close() throws IOException {}


	/**
	 * Whether or not this dataset represents an observational dataset. NOTE: Always returns FALSE.
	 *
	 * @return true if this contains obs data, otherwise false.
	 */
	public boolean isObs() {
		return false;
	}
	
	/**
	 * Returns the value used to indicate a missing reading
	 */
	
	public Double getMissingDataMarker(Variable variable) {
		return null;
	}
	
}
