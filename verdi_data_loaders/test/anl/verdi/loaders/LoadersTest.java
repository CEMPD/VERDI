package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import simphony.util.messages.MessageCenter;
import ucar.ma2.Array;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;
import anl.verdi.data.Variable;
import anl.verdi.util.VUnits;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LoadersTest extends TestCase {

	// DRY_N tstep = 0, layer = 0, x = 105 - 110 (incl.), y = 10 - 12 (incl.)
	// from ./test/anl/pave2/loaders/agg.cctmJ3fx.b312.nh3c1.dep_wa.annual.2001base
	// DRY_N(0:0:1, 0:0:1, 10:12:1, 105:110:1)
	private static double[][][][] DRY_N =

					{
									{
													{
																	{0.5457404, 0.55001736, 0.55527115, 0.5610287, 0.5648446, 0.56660295},
																	{0.59396875, 0.5977756, 0.60294914, 0.60869455, 0.61238605, 0.61389303},
																	{0.6443029, 0.64779294, 0.65138006, 0.65725166, 0.66230166, 0.6646886}
													}
									}
					};


	private static final MessageCenter msgCenter = MessageCenter
					.getMessageCenter(LoadersTest.class);

	static {
		org.apache.log4j.BasicConfigurator.configure();
	}

	private Object[] createModels3SetReader() throws Exception {
		File f = new File("./test/anl/verdi/loaders/agg.cctmJ3fx.b312.nh3c1.dep_wa.annual.2001base");
		// sample file with multiple layers and times (on Sun under /home/widing)
		//File f = new File("M:/PAVE/sampleFiles/pave_example_data/Ozone.ncf");
		URL url = f.toURI().toURL();
		DataLoader loader = new Models3Loader();
		assertTrue(loader.canHandle(url));
		Object[] obj = new Object[2];
		obj[0] = loader.createDatasets(url).get(0);
		obj[1] = loader.createReader((Dataset) obj[0]);
		return obj;
	}

	public void testGridDataset() throws IOException, URISyntaxException {
		File f = new File("C:\\Documents and Settings\\Nick Collier\\My Documents\\src\\pave2\\alignment_test\\fourcell.ncf");
		String urlString = f.toURI().toURL().toExternalForm();
		GridDataset dataset = GridDataset.open(new URI(urlString).getPath());
		GridDatatype grid = (GridDatatype) dataset.getGrids().get(0);
		GridCoordSystem gcs = grid.getCoordinateSystem();
		CoordinateAxis1D xaxis = (CoordinateAxis1D) gcs.getXHorizAxis();
		CoordinateAxis1D yaxis = (CoordinateAxis1D) gcs.getYHorizAxis();
		// coordVal is the sw corner, so coordEdge + 1 should be the center
		// of that cell.
		double xVal = xaxis.getCoordValue(1);
		double yVal = yaxis.getCoordValue(1);

		Projection proj = grid.getCoordinateSystem().getProjection();
		LatLonPointImpl latLon = new LatLonPointImpl();
		proj.projToLatLon(new ProjectionPointImpl(xVal, yVal), latLon);
		Point2D point = new Point2D.Double(latLon.getLongitude(), latLon.getLatitude());
		System.out.println("point = " + point);
	}

	public void testObsLoader() throws Exception {
		File f = new File("C:\\Documents and Settings\\Nick Collier\\My Documents\\VERDI data\\AIRNOW_M8hrO3_20070609.ncf");
		// sample file with multiple layers and times (on Sun under /home/widing)
		//File f = new File("M:/PAVE/sampleFiles/pave_example_data/Ozone.ncf");
		URL url = f.toURI().toURL();
		DataLoader loader = new Models3ObsLoader();
		assertTrue(loader.canHandle(url));
		Dataset dataset = loader.createDatasets(url).get(0);
		assertTrue(dataset.hasTimeAxis());

		DataReader reader = loader.createReader(dataset);
		Variable var = dataset.getVariables().get(0);

		List<AxisRange> range = new ArrayList<AxisRange>();
		range.add(new AxisRange(dataset.getCoordAxes().getTimeAxis(), 0, 1));
		range.add(new AxisRange(dataset.getCoordAxes().getZAxis(), 0, 1));

		DataFrame frame = reader.getValues(dataset, range, var);
		assertEquals(1166, frame.getSize());
	}

	public void testModels3LoaderFull() throws Exception {
		Object[] objs = createModels3SetReader();
		Dataset dataset = (Dataset) objs[0];
		DataReader reader = (DataReader) objs[1];

		List<String> varNames = dataset.getVariableNames();

		if (varNames != null) {
			for (String var : varNames) msgCenter.trace("variable " + var);
		}
		List<Variable> vars = dataset.getVariables();
		assertNotNull(vars);
		assertTrue(dataset.hasTimeAxis());
		assertTrue(dataset.hasZAxis());
		assertTrue(dataset.hasXAxis());
		assertTrue(dataset.hasYAxis());

		Unit unit = VUnits.createUnit("kg/ha");
		for (Variable var : vars) {
			if (var.getName().equals("RT")) {
				assertEquals(VUnits.createUnit("RT"), var.getUnit());
			} else {
				assertEquals(unit, var.getUnit());
			}
		}

		Variable var = vars.get(0);
		// get some sample data to test it
		// make the ranges
		List<AxisRange> ranges = new ArrayList<AxisRange>();
		Axes<CoordAxis> coords = dataset.getCoordAxes();
		for (CoordAxis coord : coords.getAxes()) {
			ranges.add(new AxisRange(coord));
		}

		DataFrame result = reader.getValues(dataset, ranges, var);
		Array array = result.getArray();
		assertNotNull(array);
		// check the size
		assertEquals(4, array.getRank());
		int[] shape = array.getShape();
		// 1 in time step
		assertEquals(1, shape[0]);
		// 1 in layer
		assertEquals(1, shape[1]);
		// row, col
		assertEquals(112, shape[2]);
		assertEquals(148, shape[3]);
	}

	public void testModels3Range() throws Exception {
		Object[] objs = createModels3SetReader();
		Dataset dataset = (Dataset) objs[0];
		DataReader reader = (DataReader) objs[1];
		Variable var = null;
		for (Variable variable : dataset.getVariables()) {
			if (variable.getName().equals("DRY_N")) var = variable;
		}
		assertNotNull(var);
		List<AxisRange> ranges = new ArrayList<AxisRange>();
		Axes<CoordAxis> axes = dataset.getCoordAxes();
		// x
		ranges.add(new AxisRange(axes.getXAxis(), 105, 6));
		// y
		ranges.add(new AxisRange(axes.getYAxis(), 10, 3));
		// time
		ranges.add(new AxisRange(axes.getTimeAxis(), 0, 1));
		// layer
		ranges.add(new AxisRange(axes.getZAxis(), 0, 1));

		DataFrame result = reader.getValues(dataset, ranges, var);
		Array array = result.getArray();
		assertNotNull(array);
		assertEquals(4, array.getRank());
		int[] shape = array.getShape();

		// 1 in time step
		DataFrameAxis timeAxis = result.getAxes().getTimeAxis();
		int timeIndex = timeAxis.getArrayIndex();
		assertEquals(1, shape[timeIndex]);
		assertEquals(0, timeAxis.getOrigin());
		assertEquals(0, timeAxis.getRange().getOrigin());
		assertEquals(1, timeAxis.getExtent());
		assertEquals(1, timeAxis.getRange().getExtent());
		assertEquals(AxisType.TIME, timeAxis.getAxisType());
		// 1 in layer

		DataFrameAxis layerAxis = result.getAxes().getZAxis();
		int layerIndex = layerAxis.getArrayIndex();
		assertEquals(1, shape[layerIndex]);
		assertEquals(0, layerAxis.getOrigin());
		assertEquals(0, layerAxis.getRange().getOrigin());
		assertEquals(1, layerAxis.getExtent());
		assertEquals(1, layerAxis.getRange().getExtent());
		assertEquals(AxisType.LAYER, layerAxis.getAxisType());

		// 3 rows
		DataFrameAxis yAxis = result.getAxes().getYAxis();
		int yIndex = yAxis.getArrayIndex();
		assertEquals(3, shape[yIndex]);
		assertEquals(10, yAxis.getOrigin());
		assertEquals(10, yAxis.getRange().getOrigin());
		assertEquals(3, yAxis.getExtent());
		assertEquals(3, yAxis.getRange().getExtent());
		assertEquals(AxisType.Y_AXIS, yAxis.getAxisType());
		// 6 columns
		DataFrameAxis xAxis = result.getAxes().getXAxis();
		int xIndex = xAxis.getArrayIndex();
		assertEquals(6, shape[xIndex]);
		assertEquals(105, xAxis.getOrigin());
		assertEquals(105, xAxis.getRange().getOrigin());
		assertEquals(6, xAxis.getExtent());
		assertEquals(6, xAxis.getRange().getExtent());
		assertEquals(AxisType.X_AXIS, xAxis.getAxisType());

		assertEquals(18, array.getSize());
		DataFrameIndex index = result.getIndex();
		for (int t = 0; t < shape[timeIndex]; t++) {
			for (int l = 0; l < shape[layerIndex]; l++) {
				for (int row = 0; row < shape[yIndex]; row++) {
					for (int col = 0; col < shape[xIndex]; col++) {
						index.set(t, l, col, row);
						assertEquals(DRY_N[t][l][row][col], result.getDouble(index), .000001);
					}
				}
			}
		}
	}

	public void testWRFLoader() throws Exception {
		File f = new File("E:\\VERDI data\\wrfout_nudge.nc");
		URL url = f.toURI().toURL();
		WRFLoader loader = new WRFLoader();
		assertTrue(loader.canHandle(url));

		List<Dataset> datasets = loader.createDatasets(url);
		assertEquals(8, datasets.size());
		List<Integer> varSizes = new ArrayList<Integer>();
		varSizes.add(3);
		varSizes.add(3);
		varSizes.add(58);
		varSizes.add(3);
		varSizes.add(1);
		varSizes.add(7);
		varSizes.add(1);
		varSizes.add(3);

		for (Dataset dataset : datasets) {
			int size = dataset.getVariables().size();
			assertTrue(varSizes.remove(new Integer(size)));

			assertNotNull(dataset.getCoordAxes().getDate(0));
		}
		assertTrue(varSizes.isEmpty());


	}
}
