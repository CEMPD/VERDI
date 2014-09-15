package anl.verdi.data;

/**
 * Interface for classes that handle reading data from
 * datasets.  This data could be originally from a file
 * or from a model or other source.
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 * @see Dataset, DataLoader
 */
public abstract class AbstractDataReader<T extends Dataset> implements DataReader<T> {

	protected T set;

	/**
	 * Constructs a data reader to load in data or transfer data from another source.
	 *
	 * @param set
	 */
	public AbstractDataReader(T set) {
		this.set = set;
	}
}
