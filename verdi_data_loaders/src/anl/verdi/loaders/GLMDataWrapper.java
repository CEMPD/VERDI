package anl.verdi.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import anl.verdi.data.Dataset;
import anl.verdi.plot.data.TextDataset;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayShort;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.conv.WRFConvention;

public class GLMDataWrapper extends GenericDataWrapper implements TextDataset {
	
	static final Logger Logger = LogManager.getLogger(GLMDataWrapper.class.getName());
	
	String timeVarName = null;
	GregorianCalendar startTimeCal;
	
	private NetcdfFile dataFile = null;
	Map<String, Array> dataArrays = new HashMap<String, Array>();
	
	List<String> varNames = new ArrayList<String>();
	
	Map<String, String> columnNameMap = null;

	
	public GenericDataWrapper openInstance(URL url) {
		GLMDataWrapper wrapper = new GLMDataWrapper();
		wrapper.open(url);
		return wrapper;
	}
	
	public boolean canHandle(URL url) {
		//  try to open up the file
		NetcdfFile file = null;
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			file = NetcdfFile.open(urlString);
			
		    Attribute att = file.findGlobalAttribute("Conventions");
		    if (att == null || !att.getStringValue().equals("CF-1.7"))
		    	return false;
		    return true;
			
		} catch (IOException io) {
			// just warn here because it be correct that
			// this is not a netcdf file
			//Logger.error("Error reading data file", io);
		} catch (URISyntaxException e) {
			//Logger.error("Error reading data file ", e);
		} catch (Throwable t) {
			//t.printStackTrace();
		}
		finally {
			try {
				if (file != null) 
					file.close();
			} catch (IOException e) {}
		}

		return false;
	}
	
	public void open(URL url) {
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			dataFile = NetcdfFile.open(urlString);
			
			List<Variable> vars = dataFile.getVariables();
			for (Variable var : vars)
				varNames.add(var.getName());
			
			new CSVDialog(this, varNames, GenericDataset.fields);


		} catch (Throwable t) {
			Logger.error("Error reading netcdf file", t);
		}

	}
		
	public String[] getTimeVars() {
		return new String[] { columnNameMap.get(GenericDataset.FIELD_TIME_AXIS) };
	}
	
	public String[] getLonVars() {
		return new String[] { columnNameMap.get(GenericDataset.FIELD_X_AXIS) };
	}
	
	public String[] getLatVars() {
		return new String[] { columnNameMap.get(GenericDataset.FIELD_Y_AXIS) };
	}
	
	public String[] getReservedVars() {
		return new String[] { "UTC", "Start_UTC", "Stop_UTC", "Mid_UTC", "Longitude", "Latitude"};
	}
	
	public String[] getVarList() {
		return new String[] { columnNameMap.get(GenericDataset.FIELD_VALUE_AXIS) };
	}
	
	public String[] getColumnList() {
		Set<String> fieldSet = new HashSet<String>();
		fieldSet.add(getVarList()[0]);
		fieldSet.add(getLatVars()[0]);
		fieldSet.add(getLonVars()[0]);
		fieldSet.add(getTimeVars()[0]);
		return fieldSet.toArray(new String[0]);
		
	}
	
	static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public GregorianCalendar getStartDate() {
		if (startTimeCal != null) {
			return startTimeCal;
		}
		timeVarName = getTimeVars()[0];
		for(Variable var : dataFile.getVariables()) {
			if (var.getName().equals(timeVarName)) {
				String dateStr = var.getUnitsString();
			    try {
			    	dateStr = dateStr.substring(dateStr.indexOf("since") + 6);
					Date date = DATE_PARSER.parse(dateStr);
					startTimeCal = (GregorianCalendar) GregorianCalendar.getInstance();
					startTimeCal.setTime(date);
					return startTimeCal;
				} catch (ParseException e) {
					e.printStackTrace();
				}
			    return null;

			}
		}
		return null;

	}
	
	public int getNumRows() {
		Array data = getDataArray(getVarList()[0]);
		if (data == null)
			return 0;
		return (int)data.getSize();
	}
	
	public List<Double> getValues(String variable) {
		return getValues(variable, 0, -1);
	}
	
	public List<Double> getValues(String variable, int base, int maxResults) {
		Array data = getDataArray(variable);
		if (data == null) {
			variable = variable + "_energy";
			data = getDataArray(variable);
		}
		List<Double> ret = new ArrayList<Double>();
		if (data != null) {
			if (maxResults < 0)
				maxResults = (int)data.getSize();
			if (data instanceof ArrayDouble.D1) {
				for (int i = base; i < maxResults; ++i ) {
					Double value = ((ArrayDouble.D1)data).get(i);
					ret.add(applyMetadata(variable, value));
				}
			} else if (data instanceof ArrayShort.D1) {
				for (int i = base; i < maxResults; ++i ) {
					Short value = ((ArrayShort.D1)data).get(i);
					ret.add(applyMetadata(variable, value));
				}
			}
			else if (data instanceof ArrayFloat.D1) {
				for (int i = base; i < maxResults; ++i ) {
					Double value = new Double(((ArrayFloat.D1)data).get(i));
					ret.add(applyMetadata(variable, value));
				}
			}
		}
		return ret; 
	}
	
	public Double applyMetadata(String variable, Double value) {
		Map<String, String> md = VAR_METADATA.get(variable);
		if (md == null)
			return value;
		String mod = md.get(MD_SCALE_FACTOR);
		if (mod != null) {
			double sf = Double.parseDouble(mod);
			value = value.doubleValue() * sf;
		}
		mod = md.get(MD_ADD_OFFSET);
		if (mod != null) {
			double offset = Double.parseDouble(mod);
			value = value.doubleValue() + offset;
		}
		mod = md.get(MD_TIME_BASE);
		if (mod != null) {
			double base = Double.parseDouble(mod);
			value = value.doubleValue() + base;
		}
		return value;
	}
	
	public Double applyMetadata(String variable, Short value) {
		Map<String, String> md = VAR_METADATA.get(variable);
		if (md == null)
			return new Double(value);
		String mod = md.get(MD_UNSIGNED);
		if ("true".equalsIgnoreCase(mod))
			return applyMetadata(variable, new Double(Short.toUnsignedLong(value)));
		return new Double(value);
	}
	
	public Double getValue(String variable, int i) {
		Array data = getDataArray(variable);
		if (data == null) {
			variable = variable + "_energy";
			data = getDataArray(variable);
		}
		if (data != null) {
			if (data instanceof ArrayDouble.D1) {
				Double value = ((ArrayDouble.D1)data).get(i);
				return (applyMetadata(variable, value));
			} else if (data instanceof ArrayShort.D1) {
				Short value = ((ArrayShort.D1)data).get(i);
				return applyMetadata(variable, value);
			}
			else if (data instanceof ArrayFloat.D1) {
				Double value = new Double(((ArrayFloat.D1)data).get(i));
				return applyMetadata(variable, value);
			}
		}
		return null;
	}
	
	private static final String MD_UNSIGNED = "_Unsigned";
	private static final String MD_SCALE_FACTOR = "scale_factor";
	private static final String MD_ADD_OFFSET = "add_offset";
	private static final String MD_UNITS = "units";
	private static final String MD_TIME_BASE = "time_base";
	
	private static Set<String> SUPPORTED_METADATA = new HashSet<String>();
	
	static {
		SUPPORTED_METADATA.add(MD_UNSIGNED);
		SUPPORTED_METADATA.add(MD_SCALE_FACTOR);
		SUPPORTED_METADATA.add(MD_ADD_OFFSET);
		SUPPORTED_METADATA.add(MD_UNITS);
	}
	
	Map<String, Map<String, String>> VAR_METADATA = new HashMap<String, Map<String, String>>();
	
	
	private Array getDataArray(String variable) {
		Array array = dataArrays.get(variable);
		if (array == null) {
			Variable var = dataFile.findVariable(variable);
			if (var == null)
				return null;
			Map<String, String> md = VAR_METADATA.get(variable);
			if (md == null) {
				md = new HashMap<String, String>();
				VAR_METADATA.put(variable,  md);
			}
			List<Attribute> attrs = var.getAttributes();
			for (Attribute attr : attrs) {
				if (SUPPORTED_METADATA.contains(attr.getName())) {
					if (attr.getName().equals(MD_UNITS)) {
						String unitStr = var.getUnitsString();
						if (unitStr != null && unitStr.indexOf("since") > -1) {
						    try {
						    	unitStr = unitStr.substring(unitStr.indexOf("since") + 6);
								Date date = DATE_PARSER.parse(unitStr);
								date.getTime();
								md.put(MD_TIME_BASE, Long.toString(date.getTime()));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}

					} else
						md.put(attr.getName(), attr.getStringValue());
				}

			}
			try {
				array = var.read();
				dataArrays.put(variable,  array);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return (Array)array;
	}
	
	public List<Dataset> createDatasets(URL url) {
		ArrayList<Dataset> sets = new ArrayList<Dataset>();
		GLMDataWrapper wrapper = new GLMDataWrapper();
		
		wrapper.dataFile = dataFile;
		wrapper.columnNameMap = columnNameMap;
		wrapper.getStartDate();
		sets.add(new GenericDataset(url, wrapper));
		/*wrapper = new GLMDataWrapper();
		wrapper.dataFile = dataFile;
		wrapper.eventName = "group";
		sets.add(new GenericDataset(url, wrapper));
		wrapper = new GLMDataWrapper();
		wrapper.dataFile = dataFile;
		wrapper.eventName = "event";
		sets.add(new GenericDataset(url, wrapper));*/
		return sets;
	}
	
	public boolean isObs() {
		return true;
	}

	@Override
	public void setCanceledGuiInput(boolean b) {
		return;
		
	}

	@Override
	public void setColumnNameMap(Map<String, String> columnNameMap) {
		this.columnNameMap = columnNameMap;
		
	}

	@Override
	public boolean hasColumn(String name) {
		for (String var : varNames) {
			if (var.equals(name))
				return true;
		}
		return false;
	}


}
