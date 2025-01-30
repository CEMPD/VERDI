package anl.verdi.plot.gui;

import java.io.File;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;

public class GridFinderTest {
	
	public void openGrid() {
		File vFile = new File("/home/tahoward/share/verdi/terrain.jpg");
		System.out.println("Finding format");
		AbstractGridFormat format = GridFormatFinder.findFormat( vFile );
		System.out.println("Getting reader");
		GridCoverage2DReader imageReader = format.getReader( vFile );
		System.out.println("Displaying reader");
		System.out.println(imageReader);

	}

	public static void main(String[] args) {
		new GridFinderTest().openGrid();

	}

}
