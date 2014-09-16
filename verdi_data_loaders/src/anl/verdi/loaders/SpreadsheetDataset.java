/**
 * SpreadsheetDataset.java - Result from reading ASCII spreadsheet data files.
 * @author Todd Plessel
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.data.Variable;

public class SpreadsheetDataset implements Dataset {

	private String alias = NULL_ALIAS;
	private final String fileName;
	private List<Variable> variables;
	private final Axes<CoordAxis> coordAxes;
	private final int[] points;
	private final double[][] data;
	private int conv = -1;

    /**
     * Construct a SpreadsheetDataset with the given attributes.
     */
	public SpreadsheetDataset( final String fileName,
								final Axes<CoordAxis> coordAxes,
								final int[] points,
								final double[][] data ) {
		this.fileName = fileName;
		this.coordAxes = coordAxes;
		this.points = points;
		this.data = data;
	}

    /**
     * Sets the variables for this Dataset.
     *
     * @param variables variables for this Dataset.
     */
    public void setVariables( final List<Variable> variables ) {
		this.variables = variables;
    }

    /**
     * Sets the alias for this Dataset.
     *
     * @param alias the alias for this Dataset.
     */
    public void setAlias( String alias ) {
    	this.alias = alias;
    }

    /**
     * Gets the alias of this Dataset. If no alias has been assigned the alias
     * will be {@link #NULL_ALIAS NULL_ALIAS}.
     *
     * @return the alias of this Dataset.
     */
    public String getAlias() {
    	final String result = alias;
    	return result;
    }

    /**
     * Gets the list of variable names in this Dataset.
     *
     * @return the list of variable names in this Dataset.
     */
    public List<String> getVariableNames() {
    	final List<String> result = new ArrayList<String>( variables.size() );

    	for ( final Variable variable : variables ) {
    		result.add( variable.getName() );
    	}

    	return result;
    }

    /**
     * Gets the list of variables in this Dataset.
     *
     * @return the list of variables in this Dataset.
     */
    public List<Variable> getVariables() {
    	final List<Variable> result = variables;
    	return result;
    }

    /**
     * Gets the named variable.
     *
     * @param name the name of the variable to get
     * @return the named variable
     */
    public Variable getVariable( final String name ) {
    	final int count = variables.size();
    	Variable result = null;

    	for ( int index = 0; result == null && index < count; ++index ) {
    		final Variable variable = variables.get( index );

    		if ( variable.getName().equals( name ) ) {
    			result = variable;
    		}
    	}

    	return result;
    }

    /**
     * Gets the index of the named variable.
     *
     * @param name the name of the variable to get
     * @return the index of the named variable or -1 if not found.
     */
    public int getVariableIndex( final String name ) {
    	final int count = variables.size();
    	int result = -1;

    	for ( int index = 0; result == -1 && index < count; ++index ) {
    		final Variable variable = variables.get( index );

    		if ( variable.getName().equals( name ) ) {
    			result = index;
    		}
    	}

    	return result;
    }

    /**
     * Gets the coordindate Axes for this Dataset.
     *
     * @return the coordindate Axes for this Dataset.
     */
    public Axes<CoordAxis> getCoordAxes() {
    	final Axes<CoordAxis> result = coordAxes;
    	return result;
    }

    /**
     *
     * @return true if there is a time axis, otherwise false
     */
    public boolean hasTimeAxis() {
    	final boolean result = true;
    	return result;
    }

    /**
     * See if there is a z (e.g. layer) axis
     *
     * @return true if there is a Z axis, otherwise false
     */
    public boolean hasZAxis() {
    	final boolean result = coordAxes.getZAxis() != null;
    	return result;
    }

    /**

     * @return true if there is a X axis, otherwise false
     */
    public boolean hasXAxis() {
    	final boolean result = true;
    	return result;
    }

    /**
     * @return true if there is a Y axis, otherwise false
     */
    public boolean hasYAxis() {
    	final boolean result = true;
    	return result;
    }

    /**
     * Gets the url of this Dataset.
     *
     * @return the url of this Dataset.
     */
    public URL getURL() {
    	URL result = null;

    	try {
    		result = new URL( "file://" + fileName );
    	} catch ( Exception unused ) {
    	}
 
    	return result;
    }

    /**
     * Gets the index of this Dataset inside its URL, or
     * Dataset.SINGLE_DATASET if it is the only dataset
     * inside the URL.
     *
     * @return the index of this Dataset inside its URL
     */
    public int getIndexInURL() {
    	final int result = SINGLE_DATASET;
    	return result;
    }


    /**
     * Gets the name of this Dataset.
     *
     * @return the name of this Dataset.
     */
    public String getName() {
    	final int slash = fileName.lastIndexOf( '/' );
    	final String result =
    		slash >= 0 && fileName.length() > 1 ?
    				fileName.substring( slash + 1 )
    		: fileName;
    	return result;
    }

    /**
     * Gets the fully-pathed file name of this Dataset.
     *
     * @return the fully-pathed file name of this Dataset.
     */
    public String getFileName() {
    	final String result = fileName;
    	return result;
    }

    /**
     * Closes this dataset. (No-op.)
     */
    public void close() {
    	// No-op.
    }

    /**
     * Whether or not this dataset represents an observational dataset
     *
     * @return true if this contains obs data, otherwise false.
     */
    public boolean isObs() {
    	final boolean result = true;
    	return result;
    }

    /**
     * Return a dataset meta data including projection information
     *
     * @return a dataset meta data including projection information.
     */
    public DatasetMetadata getMetadata() {
    	final DatasetMetadata result = null; // UNIMPLEMENTED.
    	return result;
    }

   /**
    * @return int total number of hours of data.
    */  
	public int getHours() {
		final int result = points.length;
		return result;
	}

	   /**
	* @return int total number of data points for the given hour.
	*/  
	public int getPointsInHour( final int hour ) {
		final int result = points[ hour ];
		return result;
	}

	   /**
	* @return int first 0-based index of data for given hour.
	*/  
	public int getHourOffset( final int hour ) {
		int result = 0;

		for ( int h = 0; h < hour; ++h ) {
			result += points[ h ];
		}

		return result;
	}

   /**
	* @return double data value for variable and index.
	*/  
	public double getValueForVariable( final int variable, int index ) {
		final double result = data[ variable ][ index ];
		return result;
	}
	
	@Override
	public int getNetcdfCovn() {
		return conv ;
	}


	@Override
	public void setNetcdfConv(int conv) {
		this.conv = conv;
	}

}




