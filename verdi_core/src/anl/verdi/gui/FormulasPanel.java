/*
 * Created by JFormDesigner on Sun Mar 11 21:12:03 CDT 2007
 */

package anl.verdi.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author User #1
 */
public class FormulasPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4063989712599725937L;
	static final Logger Logger = LogManager.getLogger(FormulasPanel.class.getName());

	public FormulasPanel() {
		this(new FormulaListModel());
	}

	public FormulasPanel(FormulaListModel model) {	
		Logger.debug("in formulasPanel constructor FormulaListModel");
		initComponents();
		addFormulaPanel.setFormulaListModel(model);
		addFormulaSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					JList list = (JList) e.getSource();
					int index = list.getSelectedIndex();
					if (index >= 0) {
						FormulaListElement element = (FormulaListElement) list.getModel().getElementAt(index);
						setTimeValues(element);
						setLayerValues(element);
						setDomainValues(element);
					} else {
						setTimeValues(null);
						setLayerValues(null);
						setDomainValues(null);
					}
				}
			}
		});
		
		addPanelListeners();
	}

	private void addPanelListeners() {
		Logger.debug("in FormulasPanel addPanelListener");
		timePanel1.addListeners(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = addFormulaPanel.getList().getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((FormulaListElement) obj).setTimeMin(val);
				}
			}
		},
						new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								Object obj = addFormulaPanel.getList().getSelectedValue();
								if (obj != null) {
									JSpinner spinner = (JSpinner) e.getSource();
									int val = ((Integer) spinner.getValue()).intValue() - 1;
									((FormulaListElement) obj).setTimeMax(val);
								}
							}
						},

						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object obj = addFormulaPanel.getList().getSelectedValue();
								if (obj != null) {
									JCheckBox box = (JCheckBox) e.getSource();
									((FormulaListElement) obj).setTimeUsed(box.isSelected());
								}
							}
						});

		layerPanel1.addListeners(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = addFormulaPanel.getList().getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((FormulaListElement) obj).setLayerMin(val);
				}
			}
		},
						new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								Object obj = addFormulaPanel.getList().getSelectedValue();
								if (obj != null) {
									JSpinner spinner = (JSpinner) e.getSource();
									int val = ((Integer) spinner.getValue()).intValue() - 1;
									((FormulaListElement) obj).setLayerMax(val);
								}
							}
						},

						new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								Object obj = addFormulaPanel.getList().getSelectedValue();
								if (obj != null) {
									JCheckBox box = (JCheckBox) evt.getSource();
									((FormulaListElement) obj).setLayerUsed(box.isSelected());
								}
							}
						});
	}

	private void setTimeValues(FormulaListElement element) {
		Logger.debug("in FormulasPanel setTimeValues");
		if (element != null && element.getTimeMin() != FormulaListElement.NO_TIME_VALUE) {
			timePanel1.setEnabled(true);
			timePanel1.reset(element.getAxes(), element.getTimeMin(), element.getTimeMax(), element.isTimeUsed());
		} else {
			timePanel1.setEnabled(false);
		}
	}

	public void setLayerValues(FormulaListElement element) {
		Logger.debug("in FormulasPanel setLayerValue");
		if (element != null && element.getLayerMin() != FormulaListElement.NO_LAYER_VALUE) {
			layerPanel1.setEnabled(true);
			layerPanel1.reset(element.getDefaultZAxis(),
							element.getLayerMin(), element.getLayerMax(), element.isLayerUsed());
		} else {
			layerPanel1.setEnabled(false);
		}
	}

	public FormulaEditor getFormulaEditor() {
		Logger.debug("in FormulasPanel getFormulaEditor");
		return addFormulaPanel;
	}

	public void addFormulaSelectionListener(ListSelectionListener listener) {
		Logger.debug("in FormulasPanel addFormulaSelectionListener");
		addFormulaPanel.addFormulaSelectionListener(listener);
	}
	
	public void setFormulaCreator(FormulaElementCreator validator) {
		Logger.debug("in FormulasPanel setFormulaCreator");
		addFormulaPanel.setCreator(validator);
	}

	public void setDomainValues(AbstractListElement element) {
		Logger.debug("in FormulasPanel setDomainValues");
		if (element == null) domainPanel1.setDomainValues(null);
		else if (element.getAxes().getXAxis() != null && element.getAxes().getYAxis() != null)
			domainPanel1.setDomainValues(element);
		else domainPanel1.setDomainValues(null);
	}

	private void initComponents() {
		Logger.debug("in FormulasPanel initComponents");
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		addFormulaPanel = new AddFormulaPanel();
		timePanel1 = new TimePanel();
		layerPanel1 = new LayerPanel();
		domainPanel1 = new DomainPanel();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setPreferredSize(new Dimension(274, 700));
		setMinimumSize(new Dimension(146, 600));
		// 2014
		RowSpec[] aRowSpec = RowSpec.decodeSpecs("fill:max(pref;125dlu):grow");
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		setLayout(new FormLayout(
						aColumnSpec,
						new RowSpec[]{
										FormFactory.PREF_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.PREF_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.PREF_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										aRowSpec[0]
						}));
//		setLayout(new FormLayout(
//				ColumnSpec.decodeSpecs("default:grow"),
//				new RowSpec[]{
//								FormFactory.PREF_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.PREF_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.PREF_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								new RowSpec("fill:max(pref;125dlu):grow")
//				}));
		add(addFormulaPanel, cc.xy(1, 1));
		add(timePanel1, cc.xy(1, 3));
		add(layerPanel1, cc.xy(1, 5));
		add(domainPanel1, cc.xy(1, 7));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private AddFormulaPanel addFormulaPanel;
	private TimePanel timePanel1;
	private LayerPanel layerPanel1;
	private DomainPanel domainPanel1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
