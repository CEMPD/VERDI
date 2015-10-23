package anl.verdi.area.target;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.gui.panel.layout.SpringUtilities;

/**
 * 
 * File Name:ProjectionPanel.java
 * Description:
 * A panel that lets the user pick projection information
 * 
 * @version March 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class ProjectionPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7978023355251034569L;
	HashMap classMap;
	ProjectionInfo projectionPanel;
	JLabel projLabel;
	public JComboBox projBox;
	// possible types of projections
	String[] projStrings = {"Equal Lat/Lon","Polar Sterographic Projection",
				"Lambert Conformal Conic Projection","Mercator Projection","Transverse Mercator Projection"};
				
	/**
	 * Constructs the panel
	 *
	 */
	public ProjectionPanel() {
		super();
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),"Projection Information"));
	}
	/**
	 * Whether the projection has been changed
	 * @return if it has been modified
	 */
	public boolean isModified(){
		return projectionPanel.isMod();
	}
	
	/**
	 * Initial setup of the panel
	 *
	 */
	public void setup(){
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		

		projLabel = new JLabel("Projection:");
		projBox = new JComboBox(projStrings);
		projBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				String projSys = (String)cb.getSelectedItem();
				setProjectionInfo(projSys);
			}
		});
		add(projLabel);
		add(projBox);
		//SpringUtilities.makeCompactGrid(panel,1,2,10,10,5,5);
		layout.putConstraint(SpringLayout.WEST,projLabel,5,SpringLayout.WEST,this);
		layout.putConstraint(SpringLayout.NORTH,projLabel,15,SpringLayout.NORTH,this);
		layout.putConstraint(SpringLayout.WEST,projBox,5,SpringLayout.EAST,projLabel);
		layout.putConstraint(SpringLayout.NORTH,projBox,15,SpringLayout.NORTH,this);
		
		classMap=new HashMap();
		classMap.put("Equal Lat/Lon",ProjectionInfo.XYProjection);
		classMap.put("Polar Sterographic Projection",ProjectionInfo.PSProjection);
		classMap.put("Lambert Conformal Conic Projection",ProjectionInfo.LambertCCProjection);
		classMap.put("Mercator Projection",ProjectionInfo.MercatorProjection);
		classMap.put("Transverse Mercator Projection",ProjectionInfo.TransverseMercatorProjection);
		
		projectionPanel=new ProjectionInfo();
		projectionPanel.setAllowAutoCM(false);
		projectionPanel.setAllowAutoZone(false);
		projectionPanel.setStatusTextField(new JTextField());

		setProjectionInfo("Equal Lat/Lon");
		add(projectionPanel);

		layout.putConstraint(SpringLayout.WEST,projectionPanel,5,SpringLayout.WEST,this);
		layout.putConstraint(SpringLayout.NORTH,projectionPanel,10,SpringLayout.SOUTH,projBox);

		// set the size explicitly
		SpringUtilities.setSize(layout,this,300,300,350,250,250,300);
		
	}
	/** 
	 * Get the string representing a given projection
	 * @return the string corresponding to the projection
	 */
	public CoordinateReferenceSystem getProjection(){
		if(projectionPanel.update()){
			CoordinateReferenceSystem proj=projectionPanel.getProjection();
			return proj;
		}
		return null;
	}
	/** 
	 * Get the error string in the projection panel 
	 * @return
	 */
	public String getErrorString(){
		return projectionPanel.getStatusTextField().getText();
	}
//	/**
//	 * Get the projection object from the panel
//	 * @return the projection entered by the user
//	 */
//	public GeoProjection getProjection(){
//	//	if(projectionPanel.update())return projectionPanel.getAdaptor().getProjection(projectionPanel);
//		return null;
//	}
	
	/**
	 * Update the projection panel based on the selected coordinate system
	 * @param coordString the coordinate system they picked
	 */
	public void setProjectionInfo(String coordString){
		int projectionClass=((Number)classMap.get(coordString)).intValue();
		projectionPanel.setProjectionClass(projectionClass);
		
		
		// hide or show the panel of other parameters
		if(coordString.equals("Equal Lat/Lon")){
			projectionPanel.setVisible(false);
		}
		else projectionPanel.setVisible(true);
		
		// redraw the panels to look nice
		projectionPanel.initialize(projectionClass);
		//projectionPanel.update(null);
		projectionPanel.validate(projectionClass);
		projectionPanel.invalidate();
		projectionPanel.validate();
		projectionPanel.repaint();
		
		invalidate();
		validate();
		repaint();
		
		JComponent parent = (JComponent)getParent();
		if(parent!=null){
			parent.invalidate();
			parent.validate();
			parent.repaint();
		}
	}
}
