/*
 * Created by JFormDesigner on Wed Apr 25 09:21:06 CDT 2007
 */

package anl.verdi.gis;

import gov.epa.emvl.Mapper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
//NOTE: where FeatureLayer is now used in this code, MapLayer had been used
import org.geotools.map.FeatureLayer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

// From FastTileLayerEditor
/**
 * @author User #2
 */
public class GTTileLayerEditor extends JDialog {
	private static final long serialVersionUID = 3280560059468223635L;
	static final Logger Logger = LogManager.getLogger(GTTileLayerEditor.class.getName());
	private Mapper mapper;
	private boolean canceled = true;

	public GTTileLayerEditor(Frame owner) {	// 2015 This is the constructor being used
		super(owner);
		initComponents();
		Logger.debug("in GTTileLayerEditor constructor #1");
	}

	public GTTileLayerEditor(Dialog owner) {
		super(owner);
		initComponents();
		Logger.debug("in GTTileLayerEditor constructor #2");
	}

	public void init(Mapper mapper) {
		this.mapper = mapper;
		layerEditorPanel1.setContext(mapper.getLayers());
	}

	private void okButtonActionPerformed(ActionEvent e) {
		layerEditorPanel1.commit();
		canceled = false;
		setVisible(false);
		dispose();
	}

	public boolean wasCanceled() {
		return canceled;
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
	}
	
	public void setLayerControl(FeatureLayer layer) {
		if (layerEditorPanel1 != null)
			layerEditorPanel1.setControlLayer(layer);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator1 = compFactory.createSeparator("Manage Layers");
		layerEditorPanel1 = new GTTileLayerPanel();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
		setTitle("Manage Layers");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				// 2014
				ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
				RowSpec aRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				contentPanel.setLayout(new FormLayout(
					aColumnSpec,
					new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						aRowSpec
					}));
//				contentPanel.setLayout(new FormLayout(
//						ColumnSpec.decodeSpecs("default:grow"),
//						new RowSpec[] {
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
//						}));
				contentPanel.add(separator1, cc.xy(1, 1));
				contentPanel.add(layerEditorPanel1, cc.xy(1, 3));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				// 2014
				RowSpec[] bRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					bRowSpec));
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
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, cc.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, cc.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JComponent separator1;
	private GTTileLayerPanel layerEditorPanel1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
