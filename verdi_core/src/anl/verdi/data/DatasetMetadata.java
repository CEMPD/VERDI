package anl.verdi.data;

import java.util.List;

/**
 * Enacapsulates a set of meta data of any dataset.
 * @author Qun He
 * @version $Revision$ $Date$
 */
public interface DatasetMetadata {

	/**
	 * Gets the list of projection parameters in this Dataset if it is a Grid type dataset.
	 *
	 * @return the list of projection parameters in this Dataset.
	 */
	List<String> getProjectionParameters(String projectionName);

	/**
	 * String representation of the projection parameters.
	 * 
	 * @return String representation of the projection parameters.
	 */
	String paramsToString(String projectionName);

	/**
	 * Gets the names of projections this Dataset supports.
	 *
	 * @return the names of projections this Dataset supports.
	 */
	List<String> getProjectionNames();

	/**
	 * Gets the class names of projections this Dataset supports.
	 *
	 * @return the class names of projections this Dataset supports.
	 */
	List<String> getProjectionClassNames();

	/**
	 * Gets the global attributes from this Dataset.
	 *
	 * @return the global attributes from this Dataset.
	 */
	List<String> getGlobalAttributes(String projectionName);

	/**
	 * Gets dimension information from the Dataset (e.g. ncols, nvars, tsteps)
	 *
	 * @return the dimensions as a series of strings (CDL format for NetCDF)
	 */
	List<String> getDimensionInfo();

}