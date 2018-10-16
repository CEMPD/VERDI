// XDRConverter.java - not part of any package

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.feature.AttributeType;
import org.opengis.feature.*;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.simple.SimpleFeature;
//import org.geotools.feature.AttributeTypeFactory;
import org.opengis.feature.simple.SimpleFeatureType;
//import org.geotools.feature.FeatureType;
//import org.geotools.feature.FeatureTypeFactory;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.FeatureType;
//import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class XDRConverter {

	public void run(String file) throws IOException {
//		DataInputStream in = new DataInputStream(new FileInputStream(file + ".bin"));	// java.io.DataInputStream.readLine()
					// deprecated because does not properly convert bytes to characters. Replace with BufferedReader
//		BufferedReader in = new BufferedReader(new FileReader(file + ".bin"));
//		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file + ".bin")));
		Scanner in = new Scanner(new FileInputStream(file = ".bin"));
		try {
//			in.readLine();		// change readLine() to 
//			in.readLine();
//			String dims = in.readLine();
			in.nextLine();
			in.nextLine();
			String dims = in.nextLine();
			int pCount = Integer.parseInt(dims.substring(0, dims.indexOf(' ')).trim());
			int vCount = Integer.parseInt(dims.substring(dims.indexOf(' ') + 1, dims.length()).trim());
//			in.readLine();
			in.nextLine();

			int counts[] = new int[pCount];
			int offsets[] = new int[pCount];

			//byte[] vals = new byte[pCount * 4];
			//in.read(vals);
			//ByteBuffer buf = ByteBuffer.wrap(vals);


			for (int i = 0; i < pCount; i++) {
//				counts[i] = in.readInt();
				counts[i] = in.nextInt();
			}

			for (int i = 0; i < pCount; i++) {
//				offsets[i] = in.readInt();
				offsets[i] = in.nextInt();
			}

			float[][] verts = new float[vCount][2];
			for (int i = 0; i < vCount; i++) {
//				verts[i] = new float[]{in.readFloat(), in.readFloat()};
				verts[i] = new float[]{in.nextFloat(), in.nextFloat()};
			}

			List<LineString> lines = new ArrayList<LineString>();
			GeometryFactory factory = new GeometryFactory();
			for (int i = 0; i < pCount; i++) {
				int numVerts = counts[i];
				Coordinate[] coords = new Coordinate[numVerts];
				int offset = offsets[i];
				for (int j = offset; j < offset + numVerts; j++) {
					float lon = verts[j][0];
					float lat  = verts[j][1];
					coords[j - offset] = new Coordinate(lon, lat);
				}

				lines.add(factory.createLineString(coords));
			}

//			writeShapefile(file, lines);	// 2014 disable export shapefile VERDI 1.5.0
		} catch (IllegalAttributeException e) {
			e.printStackTrace();
		} catch (SchemaException e) {
			e.printStackTrace(); 
		} finally {
			in.close();
		}
	}

	// 2014 disable export shapefile VERDI 1.5.0
//	private void writeShapefile(String file, List<LineString> lines) throws SchemaException, IllegalAttributeException, IOException 
//	{
////		AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom", LineString.class);	// USED
////		FeatureType ft = FeatureTypeFactory.newFeatureType(new AttributeType[] {geom}, "border");	// USED "border"
////		ShapefileDataStore datastore = new ShapefileDataStore(new File(file + ".shp").toURI().toURL());	// USED
//		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//		builder.setName("border");
//		builder.setNamespaceURI(new File(file + ".shp").toURI());
//		builder.add("the_geom", LineString.class);
//		
//		SimpleFeatureType FLAG = builder.buildFeatureType();
//		SimpleFeature flag1 = SimpleFeatureBuilder.build(FLAG, new Object[]{geom}, "flag.1");
//		datastore.createSchema(ft);
//		FeatureWriter writer = datastore.getFeatureWriter("border",
//						((FeatureStore)datastore.getFeatureSource("border")).getTransaction());
//		for (LineString line : lines) {
//			writer.next().setAttribute("the_geom", line);
//		}
//
//		writer.write();
//		writer.close();
//	}

	public static void main(String[] args) throws IOException {
		XDRConverter converter = new XDRConverter();
		String[] files = {"./map_data/map_na", "./map_data/map_counties", "./map_data/Department_of_State_Valid_QGIS"};
		for (String file : files) {
			converter.run(file);
		}
	}
}
