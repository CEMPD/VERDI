package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages


//import simphony.util.messages.MessageCenter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.conv.M3IOConvention;
import ucar.nc2.dataset.conv.MPASConvention;
import ucar.nc2.dataset.conv.WRFConvention;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.core.VerdiConstants;
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

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a netcdf file conforming to the MPAS convention.
	 *
	 * @param url a url that points to a netcdf file conforming to the
	 *            Models-3 convention.
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createMPASDatasets(URL url) {
		Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, url = " + url);
		NetcdfDataset netcdfDataset = null;
		List<Dataset> setList = new ArrayList<Dataset>();
		try {
			Logger.debug("ready to call openNetcdfGridDataset");
			String urlString = url.toExternalForm();
			netcdfDataset = NetcdfDataset.openDataset(urlString);
			Logger.debug("in NetcdfDatasetFactory.createModels3Datasets, back from openNetcdfGridDataset");
			if (!MPASConvention.isMine(netcdfDataset)) {
				Logger.debug("MPASConvention.isMine == false");
				throw new IOException("Loading non-mpas file into MPASDataset");
			}
			// if here then ok.
			Logger.debug("isMine == true, returning createDatasets for url = " + url);
			setList.add(new MPASDataset(url, netcdfDataset));
		} catch (Exception io) {
			io.printStackTrace();
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (netcdfDataset != null)
					netcdfDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
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
			Logger.error("Error reading netcdf file " + e.getMessage());
		} catch (IOException e) {
			Logger.error("Error reading netcdf file " + e.getMessage());
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

	// TODO: need to deal with extended GridDatasets that have DIMENSIONS besides X,Y,Z,Time, e.g. crop types, etc
	private List<Dataset> createDatasets(GridDataset gridDataset, URL url, int netcdfConv) {
		List<Dataset> sets = new ArrayList<Dataset>();
		// we need to maintain the order in which these are
		// created so we need the linked hashmap.
		Map<GridCoordSystem, List<GridDatatype>> map = new LinkedHashMap<GridCoordSystem, List<GridDatatype>>();
		for (GridDatatype grid : (List<GridDatatype>) gridDataset.getGrids()) {
			GridCoordSystem system = grid.getCoordinateSystem();
			
			List<GridDatatype> list = map.get(system);
			if (list == null) {
				list = new ArrayList<GridDatatype>();
				map.put(system, list);
			}
			list.add(grid);
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
		if (map.values().size() == 1) {
			sets.add(new GridNetcdfDataset(url, map.values().iterator().next(), gridDataset));
		} else {
			int index = 1;
			for (List<GridDatatype> grids : map.values()) {
				GridNetcdfDataset dataset = new GridNetcdfDataset(url, grids, gridDataset, index++);
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
