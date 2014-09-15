package anl.verdi.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saf.core.ui.util.FileChooserUtilities;
import anl.verdi.commandline.BatchScriptHandler;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.core.VerdiGUI;
import anl.verdi.util.Tools;

public class ScriptPanel extends JPanel {
	private static final long serialVersionUID = -7689933862528746587L;

	private File curFile = null;

	private JTextArea text;
	
	private JButton runButton;

	private VerdiGUI vGui;

	private String name;
	
	private StringBuffer sb;

	private boolean textChanged = false;

	private VerdiApplication vApp;

	public ScriptPanel(File script) {
		super(new BorderLayout());
		this.curFile = script;
		this.name = "Script Editor";
		this.sb = new StringBuffer(getFileContent(curFile));
		add(createScrollTextPane(), BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
	}
	
	private JComponent createScrollTextPane() {
		text = new JTextArea();
		text.setText(sb.toString());
		text.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				name += "*";
				textChanged = true;
				runButton.setEnabled(isBatchScript(text.getText()));
				runButton.revalidate();
			}
		});
		JScrollPane scroll = new JScrollPane(text);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		return scroll;
	}

	private String getFileContent(File file) {
		StringBuilder sb = new StringBuilder();

		if (file == null || !file.exists()) {
			return sb.toString();
		}

		BufferedReader reader = null;
		String line = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null)
				sb.append(line + Tools.LINE_SEPARATOR);
		} catch (IOException e) {
			sb.append("Error reading file!" + Tools.LINE_SEPARATOR);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					sb.append("Error closing file. " + e.getMessage()
							+ Tools.LINE_SEPARATOR);
				}
		}

		return sb.toString();
	}

	private JPanel createButtonsPanel() {
		JPanel container = new JPanel();

		runButton = new JButton(" Run ");
		runButton.addActionListener(runScript());
		runButton.setEnabled(isBatchScript(text.getText()));
		container.add(runButton);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(saveFileAction());
		container.add(saveButton);

		JButton saveAsButton = new JButton("Save As...");
		saveAsButton.setMargin(new Insets(2, 4, 2, 4));
		saveAsButton.addActionListener(saveAsAction());
		container.add(saveAsButton);

		return container;
	}

	private Action saveFileAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = -1074191446634681215L;

			public void actionPerformed(ActionEvent e) {
				save(curFile);
			}
		};
	}

	private Action saveAsAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = -1074191446634681215L;

			public void actionPerformed(ActionEvent e) {
				File file = FileChooserUtilities.getSaveFile(curFile);

				if (file != null) {
					curFile = file;
					save(file);
				}
			}
		};
	}

	private void save(File file) {
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(file);
			writer.write(text.getText());
			textChanged = false;

			name = file.getName();
			ScriptPanel.this.revalidate();
			
			vApp.setCurrentScriptFile(file);
			vGui.setStatusTwoText("File saved to " + file.getAbsolutePath() + ".");
		} catch (Exception exc) {
			if (vGui != null)
				vGui.showMessage("Save Batch Script", "Error saving file: "
						+ exc.getMessage() + ".");
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private Action runScript() {
		return new AbstractAction() {
			private static final long serialVersionUID = -1074191446634681215L;

			public void actionPerformed(ActionEvent e) {
				if (!projectCleared())
					return;
				
				List<List<String>> cleanText = clearComments(text.getText());
				List<String> global = cleanText.get(0);
				List<String> tasks = cleanText.get(1);
				
				String selected = text.getSelectedText();
				
				if (selected != null) tasks = clearComments(selected).get(1);
				
				boolean startsWithTask = tasks.get(0).toUpperCase().equals(VerdiConstants.TASK);
				boolean endsWithTask = tasks.get(tasks.size()-1).toUpperCase().equals(VerdiConstants.END_TASK);
				
				if (!startsWithTask || !endsWithTask)
					vGui.showMessage("Run Batch Script", "Selected text doesn't contruct a valid script.");
				
				global.addAll(tasks);
				processScript(global.toArray(new String[0]));
			}
		};
	}
	
	private List<List<String>> clearComments(String selected) {
		List<String> global = new ArrayList<String>();
		List<String> tasks = new ArrayList<String>();
		
		selected = selected.replaceAll(Tools.LINE_SEPARATOR, VerdiConstants.SEPARATOR);
		selected = selected.replaceAll("\r", VerdiConstants.SEPARATOR);
		selected = selected.replaceAll("\n", VerdiConstants.SEPARATOR);
		String[] lines = selected.split(VerdiConstants.SEPARATOR);
		
		boolean flagGlobalStart = false;
		boolean flagTaskStart = false;
		
		for (String line : lines) {
			line = (line != null) ? line.trim() : line;
			
			if (isComment(line))
				continue;
			
			if (line.toUpperCase().startsWith(VerdiConstants.TASK))
				flagTaskStart = true;
			
			if (line.toUpperCase().startsWith(VerdiConstants.END_TASK)) {
				flagTaskStart = false;
				tasks.add(line);
			}
			
			if (line.toUpperCase().startsWith(VerdiConstants.GLOBAL))
				flagGlobalStart = true;
			
			if (line.toUpperCase().startsWith(VerdiConstants.END_GLOBAL)) {
				flagGlobalStart = false;
				global.add(line);
			}
			
			if (flagGlobalStart)
				flagTaskStart = false;
			
			if (flagTaskStart)
				flagGlobalStart = false;
			
			if (flagGlobalStart)
				global.add(line);
			
			if (flagTaskStart)
				tasks.add(line);
		}
		
		List<List<String>> cleanText = new ArrayList<List<String>>();
		cleanText.add(global);
		cleanText.add(tasks);
		
		return cleanText;
	}
	
	private boolean isComment(String line) {
		return line == null || line.isEmpty() || line.startsWith("#") || line.startsWith("*");
	}

	private void processScript(String[] args) {
		BatchScriptHandler bHandler = new BatchScriptHandler(args, vApp, false);
		
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			bHandler.run();

			if (vGui != null)
				vGui.showMessage("Run Batch Script",
						"Finished running batch script file: "
								+ curFile.getAbsolutePath());
		} catch (Throwable exc) {
			exc.printStackTrace();
			if (vGui != null)
				vGui.showMessage("Batch Script Error",
								exc == null || exc.getMessage() == null ? "Error running batch script file: "
								+ curFile.getAbsolutePath() + "." : exc.getMessage());
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private boolean projectCleared() {
		boolean cleared = true;
		
		if (vApp.getProject().getDatasets().getSize() > 0) {
			vGui.showMessage("Close Datasets", "Please close all datasets before running any batch script.");
			cleared = false;
		}
		
		return cleared;
	}

	public String getName() {
		return name;
	}

	public void observe(VerdiApplication vApp) {
		this.vApp = vApp;
		this.vGui = vApp.getGui();
	}

	public boolean hasChanges() {
		return textChanged;
	}

	public void saveChanges() {
		save(curFile);
	}
	
	private boolean isBatchScript(String text) {
		if (text == null || text.trim().isEmpty())
			return false;
		
		text = text.toLowerCase();
		
		return text.contains("<task>") && text.contains("</task>");
	}
}
