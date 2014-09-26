package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.util.VUnits;

/**
 * class that stores metadata on a variable in a dataset
 * 
 * @see DefaultVariable
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 * 
 */
public class DefaultVariable implements Variable<CoordAxis> {
  
  protected Unit unit;
  protected String name, fullName;
  protected String description;
  protected Dataset dataset;

  public DefaultVariable(String name, String description, Unit unit, Dataset dataset) {
    this.name=name;
    this.description=description;
	  this.unit = unit;
System.out.println("in DefaultVariable constructor, Unit = " + this.unit);
	  this.dataset = dataset;
	  fullName = new StringBuffer(name).append(" (").append(VUnits.getFormattedName(unit)).
					  append(")").toString();
System.out.println("in DefaultVariable constructor, fullName = " + fullName);
  }

	/**
	 * Gets the dataset in which this variable appears. This
	 * may be null if the Variable is a composite composed of
	 * multiple datasets.
	 *
	 * @return the dataset in which this variable appears.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
 * Get the description of a variable
 * @return the description 
 */
  public String getDescription() {
    return description;
  }

/**
 * Get the name of the variable
 * @return the name 
 */
  public String getName() {
    return name;
  }

	/**
	 *
	 * @return the variables name plus its unit represention
	 */
  public String toString(){
    return fullName + ((dataset != null) ? dataset.getAlias() : "");
  }

	/**
	 * Gets this Variable's standard of measurement.
	 *
	 * @return this Variable's standard of measurement.
	 */
	public Unit getUnit() {
System.out.println("in DefaultVariable, returning unit = " + unit);
		return unit;
	}
}
