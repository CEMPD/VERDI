package anl.verdi.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerdiFileNameFilter implements FilenameFilter {
	
	private Pattern pattern;

	public VerdiFileNameFilter(String pattern) {
		String reg = pattern.replaceAll("\\*", ".*");
		this.pattern = Pattern.compile(reg);
	}

	public boolean accept(File dir, String name) {
		if (dir == null || !dir.exists() || !dir.isDirectory() || new File(dir, name).isDirectory())
			return false;
		
		Matcher m = pattern.matcher(name);

		return m.matches();
	}
}
