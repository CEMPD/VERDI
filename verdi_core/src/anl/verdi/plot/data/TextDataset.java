package anl.verdi.plot.data;

import java.util.Map;

public interface TextDataset {

	void setCanceledGuiInput(boolean b);

	void setColumnNameMap(Map<String, String> columnNameMap);
	
	boolean hasColumn(String name);

}
