package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import org.jfree.util.Log;

import com.google.common.collect.ImmutableList;

//import simphony.util.messages.MessageCenter;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dataset.conv.COARDSConvention;
import ucar.nc2.dataset.conv.M3IOConvention;
import ucar.nc2.dataset.conv.MPASConvention;
import ucar.nc2.dataset.conv.WRFConvention;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;

/**
 * Creates Dataset-s from netcdf files.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetcdfDatasetFactory {

	static final Logger Logger = LogManager.getLogger(NetcdfDatasetFactory.class.getName());
//	protected static final MessageCenter msgCenter = MessageCenter.getMessageCenter(NetcdfDatasetFactory.class);

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the CF convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            CF convention.
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createCFDatasets(URL url) {
		Logger.debug("in NetcdfDatasetFactory.createCFDatasets, url = " + url);
		GridDataset gridDataset = null;
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			gridDataset = openNetcdfGridDataset(url);
			Logger.debug("in NetcdfDatasetFactory.createCFDatasets, back from openNetcdfGridDataset");
			final NetcdfFile file = gridDataset.getNetcdfFile();
			Logger.debug("in NetcdfDatasetFactory.createCFDatasets, now have file = " + file);
			final Attribute attribute = file.findGlobalAttribute("Conventions");
			Logger.debug("in NetcdfDatasetFactory.createCFDatasets, now have attribute = " + attribute);
			final boolean isMine = attribute != null && attribute.getStringValue().startsWith( "CF-" );
			Logger.debug("isMine = " + isMine);

			if ( ! isMine ) {
				throw new IOException("Loading non-CF file into CFDataset");
			}

			final List<Dataset> result = createDatasets(gridDataset, url, -1);
			return result;
		} catch (Exception io) {
			Logger.error("Error reading netcdf file", io);
			try {
				if (gridDataset != null)
					gridDataset.close();
	
			} catch (IOException e) {
				Logger.error("Error closing netcdf file", e);
			}
		}
		return new ArrayList<Dataset>();
	}

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the MPAS convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            Models-3 convention.
	 * @return a list of Datasets from the specified URL
	 * @throws IOException 
	 */
	public List<Dataset> createMPASDatasets(URL url) throws IOException {
		Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, url = " + url);
		NetcdfDataset netcdfDataset = null;
		List<Dataset> setList = new ArrayList<Dataset>();
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			String urlString = url.toExternalForm();
			netcdfDataset = NetcdfDataset.openDataset(urlString);
			Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, back from openNetcdfGridDataset");
			//Already check in MPASLoader
			/*if (!MPASConvention.isMine(netcdfDataset)) {
				Logger.debug("MPASConvention.isMine == false");
				throw new IOException("Loading non-mpas file into MPASDataset");
			}*/
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			setList.add(new MPASDataset(url, netcdfDataset));
		} catch (IOException ex) {
			try {
				if (netcdfDataset != null)
					netcdfDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw ex;
		}
		return setList;
	}

	public List<Dataset> createBCONDatasets(URL url) throws IOException {
		Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, url = " + url);
		NetcdfDataset netcdfDataset = null;
		List<Dataset> setList = new ArrayList<Dataset>();
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			String urlString = url.toExternalForm();
			netcdfDataset = NetcdfDataset.openDataset(urlString);
			Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, back from openNetcdfGridDataset");
			//Already check in MPASLoader
			/*if (!MPASConvention.isMine(netcdfDataset)) {
				Logger.debug("MPASConvention.isMine == false");
				throw new IOException("Loading non-mpas file into MPASDataset");
			}*/
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			//setList.add(new BCONDataset(url, netcdfDataset));
		} catch (IOException ex) {
			try {
				if (netcdfDataset != null)
					netcdfDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw ex;
		}
		return setList;
	}

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the Models-3 convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            Models-3 convention.
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createModels3Datasets(URL url) {
		Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, url = " + url);
		GridDataset gridDataset = null;
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			gridDataset = openNetcdfGridDataset(url);
			Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, back from openNetcdfGridDataset");
			if (!M3IOConvention.isMine(gridDataset.getNetcdfDataset())) {
				Logger.debug("M3IOConvention.isMine == false");
				throw new IOException("Loading non-models3 file into Models3Dataset");
			}
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			return createDatasets(gridDataset, url, -1);
		} catch (Exception io) {
			io.printStackTrace();
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (gridDataset != null)
					gridDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<Dataset>();
	}

	public Dataset createObsDataset(URL url) {
		NetcdfDataset dataset = null;
		try {
			String urlString = url.toExternalForm();
			Logger.debug("External url string " + urlString + " path " + new URI(urlString).getPath());
			//if (url.getProtocol().equals("file")) {  DEBUG
				//urlString = new URI(urlString).getPath();
			//}
//			dataset = NetcdfDataset.openDataset(urlString, false, new CancelTask() {
				// 2014 require implementation of interface functions isCancel() and setProgress()
				// isCancel() previously implemented; no helpful information could be founc for setProgress()
				// documentation gives option of "null" for the CancelTask argument - see below
//				public boolean isCancel() {
//					return false;
//				}
//				public void setError(String msg) {}
//				@Override
//				public void setProgress(String arg0, int arg1) {
//					
//				}
//			});
			dataset = NetcdfDataset.openDataset(urlString, false, null);
			return new Models3ObsDataset(url, dataset);
		} catch (URISyntaxException e) {
			Logger.error("Error reading netcdf file", e);
		} catch (IOException e) {
			Logger.error("Error reading netcdf file", e);
		}

		return null;
	}

	public Dataset createWRFObsDataset(URL url) {
		NetcdfDataset dataset = null;
		try {
			String urlString = url.toExternalForm();
			Logger.debug("External url string " + urlString + " path " + new URI(urlString).getPath());
			//if (url.getProtocol().equals("file")) {  DEBUG
				//urlString = new URI(urlString).getPath();
			//}
//			dataset = NetcdfDataset.openDataset(urlString, false, new CancelTask() {
				// 2014 require implementation of interface functions isCancel() and setProgress()
				// isCancel() previously implemented; no helpful information could be founc for setProgress()
				// documentation gives option of "null" for the CancelTask argument - see below
//				public boolean isCancel() {
//					return false;
//				}
//				public void setError(String msg) {}
//				@Override
//				public void setProgress(String arg0, int arg1) {
//					
//				}
//			});
			dataset = NetcdfDataset.openDataset(urlString, false, null);
			WRFConvention conv = new WRFConvention();
			conv.augmentDataset(dataset, null);
			
			//GridDataset gridDataset = GridDataset.open(urlString);
			//gridDataset.getGlobalAttributes();
			
			
			/*List<Dataset> sets = new ArrayList<Dataset>();
			// we need to maintain the order in which these are
			// created so we need the linked hashmap.
			Set<String> gridVars = new HashSet<String>();
			GridCoordSystem system = null;
			Map<GridCoordSystem, List<GridDatatype>> map = new LinkedHashMap<GridCoordSystem, List<GridDatatype>>();
			for (GridDatatype grid : (List<GridDatatype>) gridDataset.getGrids()) {
				system = grid.getCoordinateSystem();
				
				List<GridDatatype> list = map.get(system);
				if (list == null) {
					list = new ArrayList<GridDatatype>();
					map.put(system, list);
				}
				list.add(grid);
				gridVars.add(grid.getVariable().getFullName());
				//System.out.println("Got grid variable " + grid.getVariable().getFullName());
			}*/

			return new WRFObsDataset(url, dataset, conv.getProjection());
		} catch (URISyntaxException e) {
			Logger.error("Error reading netcdf file", e);
		} catch (IOException e) {
			Logger.error("Error reading netcdf file", e);
		}

		return null;
	}

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the Models-3 convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            Models-3 convention.
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createWRFDatasets(URL url) {
		Logger.debug("in NetcdfDatasetFactory.createWRFDatasets for url = " + url);
		GridDataset gridDataset = null;
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			gridDataset = openNetcdfGridDataset(url); // JIZHEN-SHIFT
			Logger.debug("in NetcdfDatasetFactory.craeteWRFDatasets, back from openNetcdfGridDataset");
			if (!WRFConvention.isMine(gridDataset.getNetcdfDataset())) {
				Logger.debug("isMine == false");
				throw new IOException("Loading non-models3 file into Models3Dataset");
			}
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			return createDatasets(gridDataset, url, VerdiConstants.NETCDF_CONV_ARW_WRF);
		} catch (Exception io) {
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (gridDataset != null)
					gridDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<Dataset>();
	}

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the Models-3 convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            COARDS convention.
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createCOARDSDatasets(URL url) {
		Logger.debug("in NetcdfDatasetFactory.createCOARDSDatasets for url = " + url);
		GridDataset gridDataset = null;
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			gridDataset = openNetcdfGridDataset(url); // JIZHEN-SHIFT
			Logger.debug("in NetcdfDatasetFactory.createCOARDSDatasets, back from openNetcdfGridDataset");
			/*if (!COARDSConvention.isMine(gridDataset.getNetcdfDataset())) {
				Logger.debug("isMine == false");
				throw new IOException("Loading non-coards file into COARDSDataset");
			}*/
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			return createDatasets(gridDataset, url, -1);
		} catch (Exception io) {
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (gridDataset != null)
					gridDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<Dataset>();
	}

	// TODO: need to deal with extended GridDatasets that have DIMENSIONS besides X,Y,Z,Time, e.g. crop types, etc
	private List<Dataset> createDatasets(GridDataset gridDataset, URL url, int netcdfConv) {
		List<Dataset> sets = new ArrayList<Dataset>();
		// we need to maintain the order in which these are
		// created so we need the linked hashmap.
		Set<String> gridVars = new HashSet<String>();
		GridCoordSystem system = null;
		Map<GridCoordSystem, List<GridDatatype>> map = new LinkedHashMap<GridCoordSystem, List<GridDatatype>>();
		Map<Integer, GridCoordSystem> dimMap = new HashMap<Integer, GridCoordSystem>();
		for (GridDatatype grid : (List<GridDatatype>) gridDataset.getGrids()) {
			system = grid.getCoordinateSystem();
			
			List<GridDatatype> list = map.get(system);
			if (list == null) {
				list = new ArrayList<GridDatatype>();
				map.put(system, list);
			}
			list.add(grid);
			gridVars.add(grid.getVariable().getFullName());
			dimMap.put(grid.getVariable().getDimensions().size(), system);
			//System.out.println("Got grid variable " + grid.getVariable().getFullName());
		}
		NetcdfDataset ncd = gridDataset.getNetcdfDataset();
		List<Variable> vars = ncd.getVariables();
		Map<String, List<VariableDS>> ncdVarMap = new LinkedHashMap<String, List<VariableDS>>();
		for (Variable rawVar : vars) {
			if (!(rawVar instanceof VariableDS))
				continue;
			VariableDS var = (VariableDS)rawVar;
			//System.out.println("Got ncd var " + var.getFullName() + ", issGrid: " + gridVars.contains(var.getFullName()) + " enhanced: " + (var instanceof VariableDS));			
			if (!gridVars.contains(var.getFullName())) {
				String dimString = rawVar.getDimensionsString();
				List<VariableDS> list = ncdVarMap.get(dimString);
				if (list == null) {
					list = new ArrayList<VariableDS>();
					ncdVarMap.put(dimString,  list);
				}
				list.add(var);
			}
		}
//		if ( 1==2 ) {
//			for ( GridCoordSystem system : map.keySet()) {
//				Logger.debug("Coord system: " + system + ": ");
//				List<GridDatatype> grids = map.get(system);
//				for ( GridDatatype grid : grids ) {
//					Logger.debug("\t"+grid);
//				}
//			}
//			
//		}
		int index = 0;
		if (map.values().size() == 1 && ncdVarMap.size() == 0) {
			sets.add(new GridNetcdfDataset(url, map.values().iterator().next(), gridDataset));
		} else if (map.values().size() == 0 && ncdVarMap.size() == 1) {
			if (system != null && ncdVarMap.get(0).get(0).getDimensions().toString().indexOf("x") > -1) {
				sets.add(new GridNetcdfDataset(url, gridDataset, ncdVarMap.values().iterator().next(), system));
			}
		} else {
			index = 1;
			Set<String> xDims = new HashSet<String>();
			Set<String> yDims = new HashSet<String>();
			for (List<GridDatatype> grids : map.values()) {
				GridNetcdfDataset dataset = new GridNetcdfDataset(url, grids, gridDataset, index++);
				dataset.setNetcdfConv(netcdfConv);
				anl.verdi.data.CoordAxis x = dataset.getCoordAxes().getXAxis();
				anl.verdi.data.CoordAxis y = dataset.getCoordAxes().getYAxis();
				if (x instanceof NetCdfCoordAxis) {
					xDims.add(((NetCdfCoordAxis)x).getDatasetDimension());
				}
				if (y instanceof NetCdfCoordAxis) {
					yDims.add(((NetCdfCoordAxis)y).getDatasetDimension());
				}
				sets.add(dataset);
			}
			Map<String, List<VariableDS>> rawDisplayableVars = new HashMap<String, List<VariableDS>>();
			for (List<VariableDS> varList : ncdVarMap.values()) {
				//System.out.println("Dim " + varList.get(0).getDimensions());
				boolean containsX = false;
				boolean containsY = false;
				for (String dim : xDims) {
					if (varList.get(0).getDimensions().toString().indexOf(dim) != -1 )
						containsX = true;
				}
				for (String dim : yDims) {
					if (varList.get(0).getDimensions().toString().indexOf(dim) != -1 )
						containsY = true;
				}
				if (system != null && ((containsX && containsY) || varList.get(0).getDimensions().toString().indexOf("x") > -1)) {
					String varDimensions = varList.get(0).getDimensions().toString();
					List<VariableDS> dimVars = rawDisplayableVars.get(varDimensions);
					if (dimVars == null) {
						dimVars = new ArrayList<VariableDS>();
						rawDisplayableVars.put(varDimensions,  dimVars);
					}
					dimVars.add(varList.get(0));
				}			
			}
			for (List<VariableDS> varsByDim : rawDisplayableVars.values()) {
				//system = dimMap.get(varsByDim.get(0).getDimensions().size());
				String parentDimString = dimMap.get(varsByDim.get(0).getDimensions().size()).getName();

				ImmutableList<Dimension> dims = varsByDim.get(0).getDimensions();

				GridNetcdfDataset dataset = new GridNetcdfDataset(url, gridDataset, varsByDim, index++, system);
				Axes<CoordAxis> axes = dataset.getCoordAxes();
				axes.getAxes().get(1).getClass();
				Set<String> knownDimensions = new HashSet<String>();
				for (int i = 0; i < dims.size(); ++i) {
					Dimension dim = dims.get(i);
					String dimName = dim.getName();
					boolean dimFound = false;
					List<CoordAxis> parentAxis = axes.getAxes(); 
					for (CoordAxis axis : parentAxis) {
						if (axis.getName().equals(dim.getName()) ||
								(axis instanceof NetCdfCoordAxis && ((NetCdfCoordAxis)axis).getDatasetDimension().equals(dim.getName()))) {
							dimFound = true;
							knownDimensions.add(axis.getName());
						}
					}
					//if (parentDimString.indexOf(dimName) == -1) {
					if (!dimFound) {
						CoordAxis newAxis = new DimensionCoordAxis(dim, AxisType.LAYER);
						axes.addAxis(newAxis);
						knownDimensions.add(newAxis.getName());
					}						
				}
				List<CoordAxis> parentAxes =axes.getAxes();
				for (int i = parentAxes.size() - 1; i >= 0; --i) {
					if (!knownDimensions.contains(parentAxes.get(i).getName()))
						parentAxes.remove(i);
				}
				dataset.setNetcdfConv(netcdfConv);
				sets.add(dataset);
			}
		}
		
		return sets;
	}

	private GridDataset openNetcdfGridDataset(URL url) throws URISyntaxException, IOException {
		Logger.debug("in NetcdfDatasetFactory.openNetcdfGridDataset, url = " + url);
		String urlString = url.toExternalForm();
		Logger.debug("when converted .toExternalForm(), urlString = " + urlString);

//	2015 next block commented out because removes leading "file:" in URL & causing problems in
//	NetCDF Java library v 4.5
//	commented out per e-mail from support-netcdf-java@unidata.ucar.edu, 3/10/2015
//		if (url.getProtocol().equals("file")) {
//			Logger.debug("url.getProtocol().equals('file') so ready to get updated urlString");
//			urlString = new URI(urlString).getPath();
//			Logger.debug("updated urlString = " + urlString);
//		}
		
		validNetcdfFile( urlString);
		Logger.debug("back from validNetcdfFile");

		// return GridDataset.open(urlString); // NetCDF ENHANCE
		Logger.debug("ready to call GridDataset.open for urlString = " + urlString);
		GridDataset gridDataset = GridDataset.open(urlString);
		
//		if ( 1 == 2 )
//			printGridDatasetInfo( gridDataset);

		return gridDataset;
	}   
	
//	private NetcdfDataset openNetcdfDataset(URL url) throws URISyntaxException, IOException {
//		String urlString = url.toExternalForm();
//		
//		if (url.getProtocol().equals("file")) {
//			urlString = new URI(urlString).getPath();
//		}
//		
//		validNetcdfFile( urlString);
//
//		// return GridDataset.open(urlString); // NetCDF ENHANCE
//		
//		NetcdfDataset netcdfDataset = NetcdfDataset.openDataset(urlString);
//		
////		if ( 1 == 2 )
////			printNetcdfDatasetInfo( netcdfDataset);
//
//		return netcdfDataset;
//	} 
	
	private void validNetcdfFile(String urlString) throws IOException {
		if ( urlString == null) {
			throw new IOException("Invalid netcdf file: url is null.");
		}
		
		NetcdfFile file = null;
		try {
			file = NetcdfFile.open( urlString );
		} catch (IOException e) {
			throw new IOException("Invalid netcdf file: " + e.getMessage());
		} finally {
			if ( file != null)
				file.close();
		}
	}
	
//	private void printNetcdfDatasetInfo( NetcdfDataset netcdfDataset) {
//		
//		Logger.debug("Detailed information: " + netcdfDataset.getDetailInfo());
//		Logger.debug("File type description: " + netcdfDataset.getFileTypeDescription());
//		Logger.debug("Coordinate Systems: " + netcdfDataset.getCoordinateSystems().toString());
//		List<Variable> variables = netcdfDataset.getVariables();
//
//		List<Dimension> dims = netcdfDataset.getDimensions();
//		Logger.debug("# of dimensions: " + dims.size());
//		Iterator<Dimension> dimIt = dims.iterator();
//		while( dimIt.hasNext()) {
//			Dimension dim = dimIt.next();	
//			Logger.debug("Dim: " + dim);				
//		}
//		dimIt = null;
//		
//		Logger.debug("# of vars: " + variables.size());
//		List<Variable> netcdfVars = netcdfDataset.getVariables();
//		Iterator<Variable> varIt = netcdfVars.iterator();
//		while( varIt.hasNext()) {
//			Variable var = varIt.next();	
//			Logger.debug("Netcdf Var: " + var);
//		}
//		varIt = null;
//	}
	
//	private void printGridDatasetInfo( GridDataset gridDataset) {
//		
//		Logger.debug("Desc: " + gridDataset.getDescription());
//		Logger.debug("Info: " + gridDataset.getDetailInfo());
//		Logger.debug("Feature type: " + gridDataset.getFeatureType().toString());
//		
//		NetcdfDataset netcdfDataset = gridDataset.getNetcdfDataset();
//		List<Dimension> dims = netcdfDataset.getDimensions();
//		Logger.debug("# of dimensions: " + dims.size());
//		Iterator<Dimension> dimIt = dims.iterator();
//		while( dimIt.hasNext()) {
//			Dimension dim = dimIt.next();	
//			Logger.debug("Dim: " + dim);
//		}
//		dimIt = null;
//		
//		List<VariableSimpleIF> variables = gridDataset.getDataVariables();
//		Logger.debug("Vars got from GridDataset: ");
//		Logger.debug("# of vars: " + variables.size());
//		Iterator<VariableSimpleIF> it = variables.iterator();
//		while ( it.hasNext()) {
//			VariableSimpleIF sVar = it.next();	
//			Logger.debug("Var: " + sVar);
//		}
//		it = null;
//
//		Logger.debug("Vars got from NetcdfDataset: ");
//		List<Variable> netcdfVars = netcdfDataset.getVariables();
//		Iterator<Variable> varIt = netcdfVars.iterator();
//		while( varIt.hasNext()) {
//			Variable var = varIt.next();	
//			Logger.debug("Netcdf Var: " + var);
//		}
//		varIt = null;
//	}
}
