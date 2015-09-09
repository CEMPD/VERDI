package anl.verdi.gis;

import java.awt.BorderLayout;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.map.FeatureLayer;
import org.geotools.styling.Style;
// NOTE: where FeatureLayer is now used in this code, MapLayer had been used
									// GeoTools 13 - SNAPSHOT User's Guide says that MapLayer is now a shallow wrapper around FeatureLayer, etc. 
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

//import repast.gis.styleEditor.StyleEditorPanel;		// 2014 Repast Simphony changed the package name
import repast.simphony.gis.styleEditor.StyleEditorPanel;
import anl.verdi.plot.gui.VerdiBoundaries;
//import gov.epa.emvl.MapLines;						// 2015 replaced with VerdiBoundaries and VerdiStyle
// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer

/**
 * @author Nick Collier
 * @version $Revision: 1.5 $ $Date: 2007/05/16 22:20:57 $
 */
public class FastTileEditorStep extends PanelWizardStep {

	private static final long serialVersionUID = -3910162428960110620L;
	static final Logger Logger = LogManager.getLogger(FastTileEditorStep.class.getName());

	private FastTileAddLayerWizardModel model;

	private StyleEditorPanel panel;
	
	private FeatureLayer controlLayer;
//	private MapLayer aMapLayer;		// 2014 see above w.r.t. deprecated

	public FastTileEditorStep() {
		this(null);
		Logger.debug("default constructor for FastTileEditorStep");
	}
	
	public FastTileEditorStep(FeatureLayer control) {
		super("Edit Style", "Please edit this layer's style");
		Logger.debug("in constructor for FastTileEditorStep, control = " + control);
		controlLayer = control;
//		aMapLayer = control.;
		setLayout(new BorderLayout());
		setComplete(true);
	}

	@Override
	public void init(WizardModel wizardModel) {
		model = (FastTileAddLayerWizardModel) wizardModel;
	}

//	@Override
	public void prepare() {
		// NOTE: 2014 appears that MapLayer, FeatureLayer, etc. cannot be assigned to StyleEditorPanel at this time
		// printing an error message if hit this function
		
//		System.err.println("in anl.verdi.gis.FastTileEditorStep.prepare(); Unable to assign controlLayer this version of libraries.");
		if (controlLayer == null)
			controlLayer = model.getControlLayer();
		if(controlLayer == null)
			panel = new StyleEditorPanel();
		else
//			panel = new StyleEditorPanel(controlLayer);		// 2014 does not work; only constructor is default constructor
			panel = new StyleEditorPanel();	// need to give it a FeatureLayer
		add(panel, BorderLayout.CENTER);
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
		Logger.debug("in FastTileEditorStep.applyState, ready for model");
		FastTileAddLayerWizardModel aModel = model;
		Logger.debug("just got model = " + model);
		Logger.debug("set aModel = model = " + aModel);		// aModel is OK here
		Logger.debug("got aModel, now ready to get VerdiBoundaries");
		VerdiBoundaries aVerdiBoundaries = aModel.getLayer();	// HERE returns null for layer
		Logger.debug("got VerdiBoundaries, now ready to get the Style");	// OK - get this message
		Logger.debug("ready to getStyle for panel: " + panel.getName());	// panel.getName() == null, so bombs rest
		Style aStyle = panel.getStyle();		// 2014 throws NullPointerException
		Logger.debug("get the style = " + aStyle + ", now ready to set the Style");	// do NOT get this message
//		aMapLines.setStyle(aStyle);		// 2014 throws NullPointerException
		aVerdiBoundaries.getVerdiStyle().setStyle(aStyle);
//		model.getLayer().setStyle(panel.getStyle());
		Logger.debug("done in FastTileEditorStep.applyState & returning");
	}
}
