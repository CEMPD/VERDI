/**
 * MPASCellAxis - Interface used to represent virtual x, y, and z axes defined by an array of unstructured cells
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

public interface MPASCellAxis {
	
	public CoordAxis getXAxis();
	
	public CoordAxis getYAxis();
	
	public CoordAxis getZAxis(String variable);


}
