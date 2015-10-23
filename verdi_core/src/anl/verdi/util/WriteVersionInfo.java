package anl.verdi.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

public class WriteVersionInfo {
	static final Logger Logger = LogManager.getLogger(WriteVersionInfo.class.getName());
	private static String timestamp = new Date(new java.util.Date().getTime()).toString(); //in the yyyy-mm-dd format
	private static String ls = System.getProperty("line.separator");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String version = args[0];
		String versionInfoClass = "package anl.verdi.util;" + ls + ls
			+ "/**" + ls 
			+ " * NOTE: Auto-generated file. Please don't try to change." + ls
			+ " * @version $Revision$ $Date$" + ls
			+ " */" + ls
			+ "public class VersionInfo {" + ls
			+ "	public static final String version = \"" + version + "\";" + ls
			+ "	public static final String date = \"" + timestamp + "\";" + ls + ls
			+ "	public static String getVersion() {" + ls
			+ "		return version;" + ls
			+ "	}" + ls + ls
			+ "	public static String getDate() {" + ls
			+ "		return date;" + ls 
			+ "	}" + ls + ls
			+ "}";
		
		File file = new File(System.getProperty("user.dir") + "/../verdi_core/src/anl/verdi/util/VersionInfo.java");

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(versionInfoClass);
			writer.close();
		} catch (IOException e) {
			Logger.error("Error writting VersionInfo.java file. " + e.getMessage());
		}
	}

}
