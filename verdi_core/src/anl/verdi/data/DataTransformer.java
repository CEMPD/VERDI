package anl.verdi.data;

/**
 * Transform the data contained by a DataFrame. For example, averaging all the
 * values together for each time step.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface DataTransformer {

	/**
	 * Transforms the data contained by the specified DataFrame.
	 *
	 * @param frame the data to transform
	 * @return the transformed data
	 */
	DataFrame transform(DataFrame frame);
}
