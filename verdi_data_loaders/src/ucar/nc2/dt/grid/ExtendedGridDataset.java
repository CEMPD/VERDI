/**
 * 
 */
package ucar.nc2.dt.grid;

import java.io.IOException;
import java.util.Formatter;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author jizhen
 *
 */
public class ExtendedGridDataset extends GridDataset {

	/**
	 * @param ds
	 * @throws IOException
	 */
	public ExtendedGridDataset(NetcdfDataset ds) throws IOException {
		super(ds);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ds
	 * @param parseInfo
	 * @throws IOException
	 */
	public ExtendedGridDataset(NetcdfDataset ds, Formatter parseInfo)
			throws IOException {
		super(ds, parseInfo);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
