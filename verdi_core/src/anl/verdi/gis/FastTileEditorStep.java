package anl.verdi.gis;

import java.awt.BorderLayout;

import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
									// NOTE: where FeatureLayer is now used in this code, MapLayer had been used
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

//import repast.gis.styleEditor.StyleEditorPanel;
import repast.simphony.gis.styleEditor.StyleEditorPanel;

/**
 * @author Nick Collier
 * @version $Revision: 1.5 $ $Date: 2007/05/16 22:20:57 $
 */
public class FastTileEditorStep extends PanelWizardStep {

	private static final long serialVersionUID = -3910162428960110620L;

	private FastTileAddLayerWizardModel model;

	private StyleEditorPanel panel;
	
	private FeatureLayer controlLayer;

	public FastTileEditorStep() {
		this(null);
	}
	
	public FastTileEditorStep(FeatureLayer control) {
		super("Edit Style", "Please edit this layer's style");
		controlLayer = control;
		setLayout(new BorderLayout());
		setComplete(true);
	}

	@Override
	public void init(WizardModel wizardModel) {
		model = (FastTileAddLayerWizardModel) wizardModel;
	}

//	@Override
	public void JEBprepare() {
		// NOTE: 2014 appears that MapLayer, FeatureLayer, etc. cannot be assigned to StyleEditorPanel at this time
		// printing an error message if hit this function
		panel = new StyleEditorPanel();
		System.err.println("in anl.verdi.gis.FastTileEditorStep.prepare(); Unable to assign controlLayer this version of libraries.");
//		if (controlLayer == null)
//			controlLayer = model.getControlLayer();
//		
////		panel = (controlLayer != null) ? new StyleEditorPanel(controlLayer) : new StyleEditorPanel();
//		if(controlLayer != null)
//		{
//			panel = new StyleEditorPanel();
//			panel.setdata(controlLayer.g)
//		}
////			panel = new StyleEditorPanel();	// v2.1 has default constructor only
//////		if(controlLayer != null) panel.set? new StyleEditorPanel(controlLayer) : 
//////		panel.set???	// 2014: How to assign the controlLayer to the StyleEditorPanel?
//////		model.getControlLayer().
////		panel.setData(controlLayer.A,controlLayer.getStyle(),controlLayer.getFeatureSource());
//		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void applyState() throws InvalidStateException {
		model.getLayer().setStyle(panel.getStyle());
	}
}
