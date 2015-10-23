/*
 * Created by JFormDesigner on Tue Jun 19 12:44:35 EDT 2007
 */

package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anl.verdi.data.Variable;
import anl.verdi.plot.gui.ObsAnnotation.Symbol;
import anl.verdi.util.ObsTimeChecker;
import anl.verdi.util.Tools;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * @author User #2
 */
public class ObsOverlayDialog extends JDialog {
	private static final long serialVersionUID = 8908119418905452946L;
	protected boolean canceled = true;
	protected int strokeSize, shapeSize;
	protected Variable selectedVar;
	protected ObsTimeChecker checker;
	protected ObsAnnotation.Symbol symbol;

	public ObsOverlayDialog(Frame owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	public ObsOverlayDialog(Dialog owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	protected void addListeners() {
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    canceled = false;
			  shapeSize = ((Integer)shapeSpn.getValue()).intValue();
			  strokeSize = ((Integer)strokeSpn.getValue()).intValue();
			  symbol = getSelectedSymbol();
			  dispose();
		  }
		});

		cancelButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    dispose();
		  }
		});
		
		varList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (varList.getSelectedIndex() != -1) {
						if (checker.isCompatible((Variable)varList.getSelectedValue())) {
							okButton.setEnabled(true);
							selectedVar = (Variable)varList.getSelectedValue();
							statusLbl.setText("");
						} else {
							statusLbl.setText("Observation variable must be compatible with tile plot");
						}
				} else {
					okButton.setEnabled(false);
				}

				}
			}
		});
	}

	protected Symbol getSelectedSymbol() {
		return (Symbol)symList.getSelectedItem();
	}
	
	public Symbol getSymbol() {
		return this.symbol;
	}

	public int getShapeSize() {
		return shapeSize;
	}

	public int getStrokeSize() {
		return strokeSize;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public Variable getSelectedVar() {
		return selectedVar;
	}
	
	public boolean showLegend() {
		return false;
	}

	public void init(java.util.List<Variable> vars, ObsTimeChecker checker) {
		this.checker = checker;
		Collections.sort(vars, new Comparator<Variable>() {
			public int compare(Variable o1, Variable o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		DefaultListModel model = new DefaultListModel();
		for (Variable var : vars) {
			model.addElement(var);
		}
		varList.setModel(model);
	}

	protected void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		statusLbl = new JLabel();
		separator1 = compFactory.createSeparator("Observation Details");
		scrollPane1 = new JScrollPane();
		varList = new JList();
		label1 = new JLabel();
		strokeSpn = new JSpinner();
		label2 = new JLabel();
		shapeSpn = new JSpinner();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		String verdiHome = Tools.getVerdiHome();		// 2014 new method for reading in an image file
		String separator = "/";		// use forward slash only for constructor ImageIcon(String filename);
		String pathName = verdiHome + separator + "plugins" + separator + "core" + separator + "icons"
				 + separator;
		String fileCircle = new String(pathName + "circle.png");
		String fileDiamond = new String(pathName + "diamond.png");
		String fileSquare = new String(pathName + "square.png");
		String fileStar = new String(pathName + "star.png");
		String fileSun = new String(pathName + "sun.png");
		String fileTriangle = new String(pathName + "triangle.png");
		
		names = new Symbol[] {Symbol.CIRCLE, Symbol.DIAMOND, Symbol.SQUARE, Symbol.STAR, Symbol.SUN, Symbol.TRIANGLE};
		symbols = new Icon[] {
				new ImageIcon(fileCircle),
				new ImageIcon(fileDiamond),
				new ImageIcon(fileSquare),
				new ImageIcon(fileStar),
				new ImageIcon(fileSun),
				new ImageIcon(fileTriangle)
//				new ImageIcon(getClass().getResource("/circle.png")),
//				new ImageIcon(getClass().getResource("/diamond.png")),
//				new ImageIcon(getClass().getResource("/square.png")),
//				new ImageIcon(getClass().getResource("/star.png")),
//				new ImageIcon(getClass().getResource("/sun.png")),
//				new ImageIcon(getClass().getResource("/triangle.png"))
		};
		symList = new JComboBox(names);
		
		label3 = new JLabel();
		symPanel = new JPanel(new BorderLayout());
		selectedImg = new JLabel();
		selectedImg.setIcon(symbols[0]);
		
		symPanel.add(selectedImg, BorderLayout.LINE_START);
		symPanel.add(symList, BorderLayout.LINE_END);
		
		symList.addActionListener(new AbstractAction(){
			private static final long serialVersionUID = -5121239000837587642L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = ((JComboBox)e.getSource()).getSelectedIndex();
				selectedImg.setIcon(symbols[index]);
			}
			
		});
		
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle("Observation Dialog");
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				// 2014
				ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.6);
				ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, 0.5);
				contentPanel.setLayout(new FormLayout(
					new ColumnSpec[] {
						aColumnSpec,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						bColumnSpec
					},
					new RowSpec[] {
						FormFactory.PREF_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC
					}));
//				new ColumnSpec[] {
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.6),
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//						FormFactory.RELATED_GAP_COLSPEC,
//						new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, 0.5)
//					},
//					new RowSpec[] {
//						FormFactory.PREF_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC
//					}));

				//---- statusLbl ----
				statusLbl.setForeground(Color.red);
				contentPanel.add(statusLbl, cc.xywh(1, 1, 4, 1));
				contentPanel.add(separator1, cc.xywh(1, 3, 4, 1));

				//======== scrollPane1 ========
				{

					//---- varList ----
					varList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane1.setViewportView(varList);
				}
				contentPanel.add(scrollPane1, cc.xywh(1, 5, 1, 11));

				//---- label1 ----
				label1.setText("Stroke Size:");
				contentPanel.add(label1, cc.xywh(3, 5, 2, 1));

				//---- strokeSpn ----
				strokeSpn.setModel(new SpinnerNumberModel(1, 1, null, 1));
				contentPanel.add(strokeSpn, cc.xy(4, 7));

				//---- label2 ----
				label2.setText("Shape Size:");
				contentPanel.add(label2, cc.xywh(3, 9, 2, 1));
				
				//---- shapeSpn ----
				shapeSpn.setModel(new SpinnerNumberModel(8, 1, null, 1));
				contentPanel.add(shapeSpn, cc.xy(4, 11));
				
				//---- label3 ----
				label3.setText("Symbol:");
				contentPanel.add(label3, cc.xywh(3, 13, 2, 1));

				//---- symPanel ----
				contentPanel.add(symPanel, cc.xy(4, 15));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				RowSpec[] aRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					aRowSpec));
//				buttonBar.setLayout(new FormLayout(
//						new ColumnSpec[] {
//							FormFactory.GLUE_COLSPEC,
//							FormFactory.BUTTON_COLSPEC,
//							FormFactory.RELATED_GAP_COLSPEC,
//							FormFactory.BUTTON_COLSPEC
//						},
//						RowSpec.decodeSpecs("pref")));

				//---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, cc.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonBar.add(cancelButton, cc.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(400, 260);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JLabel statusLbl;
	protected JComponent separator1;
	protected JScrollPane scrollPane1;
	protected JList varList;
	protected JLabel label1;
	protected JSpinner strokeSpn;
	protected JLabel label2;
	protected JSpinner shapeSpn;
	protected JLabel label3;
	protected Symbol[] names;
	protected Icon[] symbols;
	protected JComboBox symList;
	protected JLabel selectedImg;
	protected JPanel symPanel;
	protected JPanel buttonBar;
	protected JButton okButton;
	protected JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
