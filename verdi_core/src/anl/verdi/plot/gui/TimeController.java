package anl.verdi.plot.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anl.gui.panel.layout.SpringUtilities;
import anl.verdi.plot.types.AbstractTilePlot;
import anl.verdi.plot.types.TilePlot;
/**
 * Class for controlling time inputs to animate plots
 * @author Mark Altaweel
 *
 */
public class TimeController extends JPanel implements ChangeListener, ActionListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractTilePlot plot;
	private SpinnerNumberModel begModel;
	private SpinnerNumberModel endModel;
	private int begValue=0;
	private int enValue=0;
	private int layer;
	private int time;
	private Timer t;

	/**
	 * Default constructor
	 *
	 */
	public TimeController() {
        super.setLayout(new SpringLayout());
		addSpinerDetails();
        createAndShowGUI();
    }
	
	/**
	 * Constructor that takes a specific tile plot object
	 * @param plot a tile plot object
	 */
	public TimeController(AbstractTilePlot plot) {
        super.setLayout(new SpringLayout());
		addSpinerDetails();
        createAndShowGUI();
        this.plot=plot;
    }
	
	public void setPlot(AbstractTilePlot plot) {
		this.plot=plot;
	}
    
    /**
     * Method to setup details of spinner
     *
     */
    public void addSpinerDetails() {
    	String[] labels = {"Beginning Time:", "End Time:"};
        int numPairs = labels.length;
      
        JFormattedTextField ftf = null;
      
        begModel = new SpinnerNumberModel(0, //initial value
                0, //min
                1000,
                0);                //
        JSpinner spinner = addLabeledSpinner(this,
                                             labels[0],
                                             begModel);
        
        //Tweak the spinner's formatted text field.
        ftf = getTextField(spinner);
        if (ftf != null ) {
            ftf.setColumns(4); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }


        //Add second label-spinner pair.
      
        endModel = new SpinnerNumberModel(0, //initial value
                                       0, //min
                                       1000,
                                       0);                //step
        
        spinner = addLabeledSpinner(this, labels[1], endModel);
       
        //Make the number be formatted without a thousands separator.
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));

        
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                                        numPairs, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        6, 10);       //xPad, yPad
        
        begModel.addChangeListener(this);
        endModel.addChangeListener(this);
    }

    /**
     * Return the formatted text field used by the editor, or
     * null if the editor doesn't descend from JSpinner.DefaultEditor.
     */
    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    
    /**
     * Method to add spinner model
     * @param c the container swing object
     * @param label the label of the spinner to add
     * @param model the spinner model to add
     * @return a  JSpinner object with added spinner
     */
    static protected JSpinner addLabeledSpinner(Container c,String label,SpinnerModel model) {
        JLabel l = new JLabel(label);
        c.add(l);

        JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);

        return spinner;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private  void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Animated Plot Controller");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        this.setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Respond to state change with updating the time step layer
     */
	public void stateChanged(ChangeEvent e) {
		SpinnerModel model = (SpinnerModel)e.getSource();
		int value=((Number)model.getNextValue()).intValue();
		if(model == begModel) {
			begValue=value;
		}
		else {
			enValue=value;
		}

		if(model == endModel) {
			layer = plot.getLayer();

			t = new javax.swing.Timer(1000, this); 
				 
			t.start();
			time=begValue;
		}
	}

	/**
	 * Method to do the updating
	 * @param t is the t time
	 * @param layer is the layer to update
	 */
	private void updatePlot(int t, int layer) {
		 ((TilePlot)plot).updateTimeStepLayer(t,layer);
	}
	 
	/**
	 * Action to update the time of the plot
	 */
	 public void actionPerformed(ActionEvent e) {
		if(time<=enValue) {
			updatePlot(time,layer);
		}
		else {
			t.stop();
		}
		time++;
     }
	    
}