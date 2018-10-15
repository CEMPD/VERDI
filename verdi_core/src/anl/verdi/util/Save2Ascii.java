package anl.verdi.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.probe.ProbeCreator;
import anl.verdi.plot.probe.ProbeCreatorFactory;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.types.VerticalCrossSectionPlot;

/**
 * Saves a plots data to a comma delimited ascii file.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Save2Ascii {

	private ProbeCreator creator;

	public Save2Ascii(Plot plot) {
		DataFrame frame = plot.getData().get(0);
		Slice slice = createSlice(plot);
		ProbeEvent evt = new ProbeEvent(plot, frame, slice, plot.getType());
		if (plot.getType() == Formula.Type.VERTICAL_CROSS_SECTION) {
			evt.setXConstant(Boolean.valueOf(((VerticalCrossSectionPlot)plot).getCrossSectionType() ==
							VerticalCrossSectionPlot.CrossSectionType.X));
		}
		creator = ProbeCreatorFactory.createProbeCreator(evt);
	}

	private Slice createSlice(Plot plot) {
		Axes<DataFrameAxis> axes = plot.getData().get(0).getAxes();
		Formula.Type type = plot.getType();
		Slice slice = new Slice();
		if (type == Formula.Type.TILE) {
			slice.setTimeRange(axes.getTimeAxis().getOrigin(), 1);
			if (axes.getZAxis() != null) slice.setLayerRange(axes.getZAxis().getOrigin(), 1);
			slice.setXRange(axes.getXAxis().getOrigin(), axes.getXAxis().getExtent());
			slice.setYRange(axes.getYAxis().getOrigin(), axes.getYAxis().getExtent());
		} else if (type == Formula.Type.TIME_SERIES_LINE || type == Formula.Type.TIME_SERIES_BAR) {
			slice.setTimeRange(axes.getTimeAxis().getOrigin(), axes.getTimeAxis().getExtent());
			if (axes.getZAxis() != null) slice.setLayerRange(axes.getZAxis().getOrigin(), 1);
		} else if (type == Formula.Type.VERTICAL_CROSS_SECTION) {
			slice.setTimeRange(axes.getTimeAxis().getOrigin(), 1);
			slice.setLayerRange(axes.getZAxis().getOrigin(), axes.getZAxis().getExtent());
			VerticalCrossSectionPlot vcPlot = (VerticalCrossSectionPlot) plot;
			if (vcPlot.getCrossSectionType() == VerticalCrossSectionPlot.CrossSectionType.X) {
				slice.setXRange(vcPlot.getConstant(), 1);
				slice.setYRange(axes.getYAxis().getOrigin(), axes.getYAxis().getExtent());
			} else {
				slice.setYRange(vcPlot.getConstant(), 1);
				slice.setXRange(axes.getXAxis().getOrigin(), axes.getXAxis().getExtent());
			}
		}

		return slice;
	}

	public void save(File file) throws IOException {
		String ext = findExtension(file);
		if (ext == null) {
			file = new File(file.getAbsolutePath() + ".csv");
		}
		PrintWriter writer = new PrintWriter(file);
		TableModel model = creator.createTableModel();
		writer.print("\"");
		writer.print(creator.getName());
		writer.print("\"");
		writer.println();
		int colCount = model.getColumnCount();
		int rowCount = model.getRowCount();
		// do the header row
		for (int col = 0; col < colCount; col++) {
			Object val = model.getValueAt(0, col);
			if (col == 0 && val.toString().length() == 0) {
				val = creator.getRangeAxisName();
			} else if (col > 0) {
				writer.print(",");
			}
			writer.print(val);
		}

		for (int row = 1; row < rowCount; row++) {
			writer.println();
			for (int col = 0; col < colCount; col++) {
				if (col > 0) writer.print(",");
				Object val = model.getValueAt(row, col);
				writer.print(val);
			}
		}
		writer.flush();
		writer.close();
		if (writer.checkError()) throw new IOException("Error while writing probed data to file");
	}

	private String findExtension(File f) {
//		String ext = null;
//		String s = f.getName();
//		int i = s.lastIndexOf('.');
//
//		if (i > 0 && i < s.length() - 1) {
//			ext = s.substring(i + 1).toLowerCase();
//		}
		String fileName = f.toString();
		String ext = new String(FilenameUtils.getExtension(fileName)).toLowerCase();
		
		return ext;
	}
}
