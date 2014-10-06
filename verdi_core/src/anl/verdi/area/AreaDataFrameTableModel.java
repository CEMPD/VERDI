package anl.verdi.area;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.area.target.GridInfo;
import anl.verdi.area.target.Target;
import anl.verdi.area.target.TargetCalculator;
import anl.verdi.data.AbstractDataFrameTableModel;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.Variable;

/**
 * DataFrameTableModel for data where each x,y cell value
 * contains the data to show in the table.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class AreaDataFrameTableModel extends AbstractDataFrameTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6345384193482940803L;
	static final Logger Logger = LogManager.getLogger(AreaDataFrameTableModel.class.getName());
	ArrayList areas;
	ArrayList[] values,valuesAve;
	Variable[] variables;
	public AreaDataFrameTableModel(DataFrame[] frames,ArrayList areas,Variable[] vars) {
		super(frames[0]);
		// copy the values
		values=new ArrayList[vars.length];
		valuesAve=new ArrayList[vars.length];
		colCount =2+vars.length*2;
		rowCount = areas.size();
		Target.setCurrentTilePlot(null);
		for(int i=0;i<vars.length;i++){
			Axes<DataFrameAxis> axes = frames[i].getAxes();

			colNameOffset = frames[i].getAxes().getXAxis().getOrigin() + 1;
			rowNameOffset = frames[i].getAxes().getYAxis().getOrigin() + 1;
			// copy the array of targets
			this.areas=new ArrayList(areas.size());
			this.areas.addAll(areas);
			this.variables=vars;

			final DataFrameAxis colAxis = axes.getXAxis();
			int cols;
			if (colAxis == null) {
				cols = 1;
			} else {
				cols = colAxis.getExtent();
			}

			final DataFrameAxis rowAxis = axes.getYAxis();
			int rows;
			if (rowAxis == null) {
				rows = 1;
			} else {
				rows = rowAxis.getExtent();
			}

			ArrayList<Float>v=new ArrayList(areas.size());
			ArrayList<Float>vAve=new ArrayList(areas.size());
			values[i]=v;
			valuesAve[i]=vAve;

			//(areas.size());
			// get the data for the frame

			GridInfo gridInfo = GridInfo.formGridInfo(frames[i],0,cols-1,0,rows-1);
			Target.setCurrentGridInfo(gridInfo);
			TargetCalculator calc = new TargetCalculator();
			calc.calculateIntersections(Target.getTargets(),frames[i],null);

			float[][] data= getAllLayerData(frames[i],0,0,rows,cols);

			for(Target target:(ArrayList<Target>)areas){
				// calculate the deposition and draw filled
				// get the data for this variable

				if(target.containsDeposition()){
					v.add(target.calculateTotalDeposition(data));
					vAve.add(target.calculateAverageDeposition(data));
				}else{
					v.add(new Float(0.0));
					vAve.add(new Float(0.0));
				}
			}
		}
	}


	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 *
	 * @return the value Object at the specified cell
	 * @param	rowIndex	the row whose value is to be queried
	 * @param	columnIndex the column whose value is to be queried
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Target target = (Target)areas.get(rowIndex);

		switch(columnIndex){
		case 0:return target.toString();
		case 1:return target.getArea();
		}
		int variableIndex=(columnIndex-2)/2;
		if(values[variableIndex].isEmpty()){
			Logger.error("error in export table");
		}
		if(columnIndex%2==0)return values[variableIndex].get(rowIndex);
		else return valuesAve[variableIndex].get(rowIndex);
	}

	public Variable[] getVariables() {
		return variables;
	}

	public float[][] getAllLayerData(DataFrame dataFrame,int timestep, int layer,int rows,int columns) {

		// Reallocate the subsetLayerData[][] only if needed:

		float[][] subsetLayerData;

		subsetLayerData = new float[rows][columns];


		// Copy from dataFrame into subsetLayerData[ rows ][ columns ]:

		final DataFrameIndex dataFrameIndex = dataFrame.getIndex();

		for (int row = 0; row < rows; ++row) {

			for (int column = 0; column < columns; ++column) {
				dataFrameIndex.set(timestep, layer, column, row);
				final float value = dataFrame.getFloat(dataFrameIndex);
				subsetLayerData[row][column] = value;
			}
		}
		return subsetLayerData;
	}


	public ArrayList getAreas() {
		return areas;
	}
}
