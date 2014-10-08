package anl.verdi.loaders;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ucar.unidata.geoloc.projection.Stereographic;

/**
 * Creates a wkt for utm projection based on
 * netcdf produced parameters.
 * 
 * Adapted from LambertWKTCreator
 *
 * @author Alexis Zubrow & Qun He
 * @version $Revision$ $Date$
 */
public class PolarStereographicWKTCreator {
	static final Logger Logger = LogManager.getLogger(PolarStereographicWKTCreator.class.getName());

	public String createWKT(Stereographic proj) throws IOException {
		Logger.debug("in PolarStereographicWKTCreator.createWKT");
		VelocityContext context = new VelocityContext();
		String template = getClass().getPackage().getName();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		template = template.replace('.', '/');
		template = template + "/polar_stereographic_wkt.vt";
		double projNatOriginLat = proj.getNaturalOriginLat();	// 2014 trying to see what happens when not set to 0.0 in test dataset
		Logger.debug("in createWKT: proj.getNaturalOriginLat returns " + projNatOriginLat);
		if (projNatOriginLat < 70.0)
			projNatOriginLat = 90.0;
		context.put("lat_origin", projNatOriginLat);	// 2014 NOTE: usually 90.0 but some datasets use 70.0
		context.put("central_meridian", 0.0);	//proj.getCentralMeridian());	// NOTE: function not in NetCDF-Java v4.3.20

		// 2014 NetCDF-Java v4.3.20 returns -98.0 for scale
		// per documentation scale is normally 1.0 but could be less
		// added following logic to trap value returned for scale and if negative set to 1.0
		double projScale = proj.getScale();
		if(projScale < 0.0)
			projScale = 1.0;
		context.put("scale", projScale);		// 2014 had been proj.getScale()
		Logger.debug("in createWKT: proj.getScale returns " + proj.getScale());
		Writer writer = new StringWriter();

		try {
			Velocity.mergeTemplate(template, "UTF-8", context, writer);
		} catch (Exception ex) {
			throw new IOException("Error merging template", ex);
		} finally {
			if (writer != null) writer.close();
			Thread.currentThread().setContextClassLoader(loader);
		}
		return writer.toString();
	}
}
