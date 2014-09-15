package anl.verdi.plot.color;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ColorMapTest extends TestCase {

	public void testIO() throws Exception {
		Palette pal = Palette.getDefaultPalette();
		ColorMap map = new ColorMap(pal, 0, 1);

		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);

		VelocityContext context = new VelocityContext();
		String template = ColorMap.class.getPackage().getName();
		template = template.replace('.', '/');
		// template = template + "/ColorMap.vt";
		template = "../../../../../src/anl/verdi/plot/config/PlotConfig.vt";
		context.put("map", map);

		Writer writer = new FileWriter(new File("./map.config"));
		Velocity.mergeTemplate(template, "UTF-8", context, writer);
		writer.close();

		ColorMapLoader loader = new ColorMapLoader();
		ColorMap map2 = loader.load(new File("./map.config"));

		assertEquals(map.getMax(), map2.getMax());
		assertEquals(map.getMin(), map2.getMin());
		assertEquals(map.getPalette().getDescription(), map2.getPalette().getDescription());
		assertEquals(map.getColorCount(), map2.getColorCount());
		assertEquals(map.getIntervalType(), map2.getIntervalType());
		assertEquals(map.getPaletteType(), map2.getPaletteType());

		for (int i = 0; i < map.getColorCount(); i++) {
			assertEquals(map.getColor(i), map2.getColor(i));
			assertEquals(map.getIntervalStart(i), map2.getIntervalStart(i));
		}

	}
}
