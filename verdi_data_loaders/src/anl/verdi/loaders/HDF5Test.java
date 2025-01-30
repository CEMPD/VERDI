package anl.verdi.loaders;

import java.nio.file.Paths;
import java.util.Map;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;

public class HDF5Test {

	public void run() {
		String path = "/home/tahoward/allstate/3B-MO.MS.MRG.3IMERG.20160101-S000000-E235959.01.V06B.HDF5";
		HdfFile hdfFile = new HdfFile(Paths.get(path));
		Map<String, Attribute> attrs = hdfFile.getAttributes();
		Map<String, Node> children = hdfFile.getChildren();
		for (Attribute attr : attrs.values()) {
			System.out.println("Attr: " + attr);
		}
		for (Node node : children.values()) {
			System.out.println("Node: " + node);
		}
		Dataset dataset = hdfFile.getDatasetByPath("/path/to/dataset");
		
		
	}
	
	public static void main(String[] args) {
		new HDF5Test().run();
	}
}
