package anl.verdi.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;
/**
 * Exports the data from a JTable into a csv file.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TableExporter {

	private JTable table;
	private String title, rangeAxisName;
	private boolean exportHeader=false;
	public TableExporter(JTable table, String title, String rangeAxisName) {
		this.table = table;
		this.title = title;
		this.rangeAxisName = rangeAxisName;
	}

	/**
	 * Run the exporter. This will display a file
	 * chooser and save the table data in the
	 * selected file.
	 *
	 * @throws java.io.IOException if there is an error while creating
	 *                             the image or saving the plot.
	 */
	public void run() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {

			public String getDescription() {
				return "Text Files (*.txt, *.csv)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				String ext = findExtension(f);
				return ext != null && (ext.toLowerCase().equals("txt") ||
								ext.toLowerCase().equals("csv"));
			}
		});

		int res = chooser.showSaveDialog(table);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			save(file);
		}
	}

	private void save(File file) throws IOException {
		String ext = findExtension(file);
		if (ext == null) {
			file = new File(file.getAbsolutePath() + ".csv");
		}
		PrintWriter writer = new PrintWriter(file);
		TableModel model = table.getModel();
		writer.print("\"");
		writer.print(title);
		writer.print("\"");
		writer.println();
		int colCount = model.getColumnCount();
		int rowCount = model.getRowCount();
		
		// export the header if desired
		if(isExportHeader()){
			// get the headers from the columns
			for(int col = 0; col < colCount; col++){
				Object headerVal=table.getTableHeader().getColumnModel().getColumn(col).getHeaderValue();
				if (col > 0) {
					writer.print(",");
				}
				writer.print(headerVal);
			}
			writer.print("\n");
		}
		
		// do the header row
		for (int col = 0; col < colCount; col++) {
			Object val = model.getValueAt(0, col);
			if (col == 0 && val.toString().length() == 0) {
				val = rangeAxisName;
			} else if (col > 0) {
				writer.print(",");
			}
			writer.print(val);
		}

		for (int row = 1; row < rowCount; row++) {
			writer.println();
			for (int col = 0; col < colCount; col++) {
				if (col > 0) writer.print(",");
				Object val = model.getValueAt(row, col);
				writer.print(val);
			}
		}
		writer.flush();
		writer.close();
		if (writer.checkError()) throw new IOException("Error while writing probed data to file");
	}

	private String findExtension(File f) {
//		String ext = null;
//		String s = f.getName();
//		int i = s.lastIndexOf('.');
//
//		if (i > 0 && i < s.length() - 1) {
//			ext = s.substring(i + 1).toLowerCase();
//		}
		String fileName = f.toString();
		String ext = new String(FilenameUtils.getExtension(fileName)).toLowerCase();
		return ext;
	}

	public boolean isExportHeader() {
		return exportHeader;
	}

	public void setExportHeader(boolean exportHeader) {
		this.exportHeader = exportHeader;
	}

	
}
