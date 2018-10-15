/**
 * MultiAxisDataset - Interface to represent a dataset with multiple axes of a common type
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

public interface MultiAxisDataset {
	
	public CoordAxis getTimeAxis(String variable);
	
	public CoordAxis getDefaultTimeAxis();
	
	public CoordAxis getZAxis(String variable);
	
	public CoordAxis getDefaultZAxis();

}
