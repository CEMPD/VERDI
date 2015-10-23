package anl.verdi.plot.color;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ColorMapper {
	static final Logger Logger = LogManager.getLogger(ColorMapper.class.getName());

	public static void main(String[] args) throws Exception {
		Logger.debug("in anl.verdi.plot.color.ColorMapper.main, do not expect to see this message");
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
		template = template + "/ColorMap.vt";
		/*
		<ColorMap intervalType="$intervalType" paletteType="$paletteType" min="$min" max="$max">
#foreach($color in $colors)
<Color>$color.getRGB()</Color>
		 */
		context.put("map", map);

		Writer writer = new FileWriter(new File("./map.config"));
		Velocity.mergeTemplate(template, "UTF-8", context, writer);
		writer.close();

		ColorMapLoader loader = new ColorMapLoader();
		loader.load(new File("./map.config"));
		

		/*
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace(); 
		}
		PaletteSelectionPanel panel = new PaletteSelectionPanel();

//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMapFrame frame = new JMapFrame();
		frame.setDefaultCloseOperation(JMapFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		*/
	}
}
