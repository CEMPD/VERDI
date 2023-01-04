package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import anl.verdi.data.Dataset;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.conv.M3IOConvention;

public class BCONLoader extends Models3Loader {
	
	public boolean canHandle(URL url) throws Exception {
		//  try to open up the file
		NetcdfFile file = null;
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			file = NetcdfFile.open(urlString);
			return M3IOConvention.isMine(file) && hasPerim(file);

		} catch (IOException io) {
			//io.printStackTrace();
			// just warn here because it be correct that
			// this is not a netcdf file
			Logger.warn("Error reading netcdf file " + io.getMessage());
			throw io;
		} catch (URISyntaxException e) {
			//e.printStackTrace();
			Logger.warn("Error reading netcdf file " + e.getMessage());
			throw e;
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			try {
				if (file != null) file.close();
			} catch (IOException e) {}
		}
		return false;
	}
	
	public List<Dataset> createDatasets(URL url) throws IOException {
		NetcdfDatasetFactory factory = new NetcdfDatasetFactory();
		return factory.createBCONDatasets(url);
	}

	private boolean hasPerim(NetcdfFile file) {
		List<Dimension> dims = file.getDimensions();
		boolean hasPerim = false;
		for (Dimension dim : dims) {
//			if (dim.getName().equals("ROW")) hasRow = true;			// getName() is deprecated & no replacement suggested
//			else if (dim.getName().equals("COL")) hasCol = true;	// Based on code examples selected getShortName()
			if (dim.getShortName().equals("PERIM")) hasPerim = true;
		}

		return hasPerim;
	}
}
