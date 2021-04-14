package anl.verdi.loaders;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ucar.unidata.geoloc.projection.Mercator;

/**
 * Creates a wkt for latitude-longitude based on
 * netcdf produced parameters.
 * 
 * Adapted from LambertWKTCreator
 *
 * @author Alexis Zubrow & Qun He
 * @version $Revision$ $Date$
 */
public class MercatorWKTCreator {
	static final Logger Logger = LogManager.getLogger(MercatorWKTCreator.class.getName());

	public String createWKT(Mercator proj) throws IOException {
		Logger.debug("in MercatorWKTCreator");
		VelocityContext context = new VelocityContext();
		String template = getClass().getPackage().getName();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		template = template.replace('.', '/');
		template = template + "/mercator_wkt.vt";

		Writer writer = new StringWriter();
		try {
			Velocity.mergeTemplate(template, "UTF-8", context, writer);
		} catch (Exception ex) {
			throw new IOException("Error merging template", ex);
		} finally {
			if (writer != null) writer.close();
			Thread.currentThread().setContextClassLoader(loader);
		}
		String wkt = writer.toString();
		wkt = wkt.replace("e_radius", Double.toString(proj.getEarthRadius() * 1000));
		wkt = wkt.replace("f_easting", Double.toString(proj.getFalseEasting()));
		wkt = wkt.replace("f_northing", Double.toString(proj.getFalseNorthing()));
		wkt = wkt.replace("c_meridian", Double.toString(proj.getOriginLon()));
		Logger.debug("in MercatorWKTcreate: proj.EarthRadius returns " + proj.getEarthRadius()*1000);
		Logger.debug("in MercatorWKTcreate: proj.getFalseEasting returns " + proj.getFalseEasting());
		Logger.debug("in MercatorWKTcreate: proj.getFalseNorthing returns " + proj.getFalseNorthing());
		//Logger.debug("in MercatorWKTcreate: central_meridian proj.getOriginLon returns " + proj.getOriginLon());

		
		return wkt;
	}
}
