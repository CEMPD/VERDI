package saf.core.ui.msg;

import simphony.util.messages.MessageEvent;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.apache.log4j.Level;
/*
 * Created by JFormDesigner on Mon Dec 04 15:11:05 EST 2006
 */



/**
 * @author User #2
 */
public class MessagePanel extends JPanel {

	private MessageTableModel model;

	public MessagePanel() {
		this(new MessageTableModel());
	}

	public MessagePanel(MessageTableModel aModel) {
		initComponents();
		splitPane1.setDividerLocation(.75);
		errorTxt.setEditable(false);
		this.model = aModel;
		errorTable.setModel(model);
		errorTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = errorTable.rowAtPoint(e.getPoint());
					if (row != -1) errorTxt.setText(model.getFullMessage(row));
				}
			}
		});
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		splitPane1 = new JSplitPane();
		scrollPane2 = new JScrollPane();
		errorTable = new JTable();
		scrollPane1 = new JScrollPane();
		errorTxt = new JTextArea();
		errorTxt.setRows(30);

		//======== this ========
		setLayout(new BorderLayout());

		//======== splitPane1 ========
		{
			splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane1.setResizeWeight(0.75);
			
			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(errorTable);
			}
			splitPane1.setTopComponent(scrollPane2);
			
			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(errorTxt);
			}
			splitPane1.setBottomComponent(scrollPane1);
		}
		add(splitPane1, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JSplitPane splitPane1;
	private JScrollPane scrollPane2;
	private JTable errorTable;
	private JScrollPane scrollPane1;
	private JTextArea errorTxt;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public static void main(String[] args) {
		MessageTableModel model = new MessageTableModel();
		model.addMessageEvent(new MessageEvent(new Object(), Level.WARN, "failed to do blah blah"));
		Throwable exception = new RuntimeException();
		exception = exception.fillInStackTrace();
		model.addMessageEvent(new MessageEvent(new Object(), Level.ERROR, exception));
		MessagePanel panel = new MessagePanel(model);
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
