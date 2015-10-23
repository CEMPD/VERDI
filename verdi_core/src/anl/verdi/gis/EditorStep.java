package anl.verdi.gis;

import java.awt.BorderLayout;

import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import repast.simphony.gis.styleEditor.StyleEditorPanel;

//import repast.gis.styleEditor.StyleEditorPanel;		// libraries changed in repast simphony

/**
 * @author Nick Collier
 * @version $Revision: 1.5 $ $Date: 2007/05/16 22:20:57 $
 */
public class EditorStep extends PanelWizardStep {

	private static final long serialVersionUID = -3910162428960110620L;

	private AddLayerWizardModel model;

	private StyleEditorPanel panel;

	public EditorStep() {
		super("Edit Style", "Please edit this layers style");
		setLayout(new BorderLayout());
		setComplete(true);
	}

	@Override
	public void init(WizardModel wizardModel) {
		model = (AddLayerWizardModel) wizardModel;

	}

	@Override
	public void prepare() {
		panel = new StyleEditorPanel();		// model.getLayer() 2014
		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void applyState() throws InvalidStateException {
		model.getLayer().setStyle(panel.getStyle());
	}
}
