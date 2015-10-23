package anl.verdi.loaders;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ucar.unidata.geoloc.projection.LambertConformal;

/**
 * Creates a WKT for lambert conformal projection based on
 * netcdf produced parameters.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LambertWKTCreator {
	static final Logger Logger = LogManager.getLogger(LambertWKTCreator.class.getName());

	public String createWKT(LambertConformal proj) throws IOException {
		Logger.debug("in LambertWKTCreator");
		VelocityContext context = new VelocityContext();
		String template = getClass().getPackage().getName();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		template = template.replace('.', '/');
		template = template + '/' + "lambert_wkt.vt";
		/*
		 $central_meridian],
  PARAMETER["latitude_of_origin", $latitude_of_origin],
  PARAMETER["standard_parallel_1", $standard_parallel_1],
  PARAMETER["false_easting", 0.0],
  PARAMETER["false_northing", 0.0],
  PARAMETER["standard_parallel_2", $standard_parallel_2],
  */
		context.put("central_meridian", proj.getOriginLon());
		context.put("latitude_of_origin", proj.getOriginLat());
		context.put("standard_parallel_1", proj.getParallelOne());
		context.put("standard_parallel_2", proj.getParallelTwo());

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
