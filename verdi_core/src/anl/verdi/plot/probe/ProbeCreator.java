package anl.verdi.plot.probe;

import javax.swing.table.TableModel;

/**
 * Interface for classes that will create probes.s
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface ProbeCreator {

	/**
	 * Creates the table model that contains the probed data.
	 *
	 * @return the created TableModel.
	 */
	TableModel createTableModel();

	/**
	 * Gets the name of the probe.
	 *
	 * @return the name of the probe.
	 */
	String getName();

	/**
	 * Gets the name of the range axis.
	 *
	 * @return the name of the range axis.
	 */
	String getRangeAxisName();
}
