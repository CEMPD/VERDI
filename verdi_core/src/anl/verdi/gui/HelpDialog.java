package anl.verdi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXHeader;

import anl.verdi.util.VersionInfo;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;



/**
 * @author User #2
 */
public class HelpDialog extends JDialog {
	private static final long serialVersionUID = -8261331914096627049L;
	
	public HelpDialog(Frame owner) {
		super(owner);
		try {
			initComponents();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xHeader1.setDescription("Version: " + VersionInfo.getVersion() + " " + VersionInfo.getDate());
	}

	public HelpDialog(Dialog owner) {
		super(owner);
		try {
			initComponents();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xHeader1.setDescription("Version: " + VersionInfo.getVersion() + " " + VersionInfo.getDate());
	}

	private void okButtonActionPerformed(ActionEvent e) {
		this.dispose();
	}

	private void initComponents() throws URISyntaxException {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		xHeader1 = new JXHeader();
		buttonBar = new JPanel();
		okButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle("Help VERDI");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default:grow",
					"default"));

				//======== scrollPane1 ========
				{

					//---- xHeader1 ----
//					 HTML.Tag anchor = HTML.Tag.A;
					 
//					JLabel label = new JLabel("<html>Visual Environment For Rich Data Interpretation (VERDI)<br/><br/><a href=\"http://www.verdi-tool.org/verdiUserManual.htm\">HTML Version</a><br/><br/><a href=\"http://www.cmascenter.org/help/model_docs/verdi/1.4/VerdiUserManual.pdf\">PDF Version</a></html>");

//					javax.swing.text.html.HTMLEditorKit
//					JPanel editorPanePanel = new JPanel();
//					JEditorPane editorPane = new JEditorPane();
//					HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//					htmlEditorKit.
//					editorPane.setEditorKit() 
//					editorPane.setEditable(false);
//					java.net.URL helpURL = TextSamplerDemo.class.getResource(
//					                                "TextSamplerDemoHelp.html");
//			        editorPane.setText("<html>Visual Environment For Rich Data Interpretation (VERDI)<br/><br/><a href=\"http://www.verdi-tool.org/verdiUserManual.htm\">HTML Version</a><br/><br/><a href=\"http://www.cmascenter.org/help/model_docs/verdi/1.4/VerdiUserManual.pdf\">PDF Version</a></html>");

					//Put the editor pane in a scroll pane.
//					JScrollPane editorScrollPane = new JScrollPane(editorPane);
//					editorScrollPane.setVerticalScrollBarPolicy(
//					                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//					editorScrollPane.setPreferredSize(new Dimension(250, 145));
//					editorScrollPane.setMinimumSize(new Dimension(10, 10));					
//					editorPanePanel.add(editorScrollPane);
					
				    final URI uri = new URI("https://www.cmascenter.org/verdi/documentation/1.5/VerdiUserManual1.5.htm");	// 2014 VERDI 1.5 documentation
				    // old URI was: "http://www.verdi-tool.org/verdiUserManual_URI_uri.htm"
				    final URI uri2 = new URI("http://www.cmascenter.org/verdi/documentation/1.5/VerdiUserManual1.5.pdf");	// 2014 VERDI 1.5 documentation
				    // old URI was: "http://www.cmascenter.org/help/model_docs/verdi/1.4/VerdiUserManual_URI_uri2.pdf"
				    class OpenUrlAction implements ActionListener {
				      @Override public void actionPerformed(ActionEvent e) {
				        open(uri);
				      }
				    }
				    class OpenUrlAction2 implements ActionListener {
					      @Override public void actionPerformed(ActionEvent e) {
					        open(uri2);
					      }
					    }
				    JButton htmlLinkButton = new JButton();
				    htmlLinkButton.setText("<HTML><FONT color=\"#000099\"><U>HTML Version</U></FONT></HTML>");
				    htmlLinkButton.setHorizontalAlignment(SwingConstants.LEFT);
				    htmlLinkButton.setBorderPainted(false);
				    htmlLinkButton.setOpaque(false);
				    htmlLinkButton.setBackground(Color.WHITE);
				    htmlLinkButton.setToolTipText(uri.toString());
				    htmlLinkButton.addActionListener(new OpenUrlAction());
					
					
				    JButton pdfLinkButton = new JButton();
				    pdfLinkButton.setText("<HTML><FONT color=\"#000099\"><U>PDF Version</U></FONT></HTML>");
				    pdfLinkButton.setHorizontalAlignment(SwingConstants.LEFT);
				    pdfLinkButton.setBorderPainted(false);
				    pdfLinkButton.setOpaque(false);
				    pdfLinkButton.setBackground(Color.WHITE);
				    pdfLinkButton.setToolTipText(uri.toString());
				    pdfLinkButton.addActionListener(new OpenUrlAction2());
					
					
					JPanel panel = new JPanel(new BorderLayout());
					panel.add(htmlLinkButton, BorderLayout.NORTH);
					panel.add(pdfLinkButton, BorderLayout.SOUTH);
//					xHeader1.setTitle("<html>Visual Environment For Rich Data Interpretation (VERDI)<br/><br/><a href=\"http:// xHeader1 setTitle www.verdi-tool.org/verdiUserManual.htm\">HTML Version</a><br/><br/><a href=\"http://XHeader1 setTitle www.cmascenter.org/help/model_docs/verdi/1.4/VerdiUserManual.pdf\">PDF Version</a></html>"); // 2014 does not appear to be used
//					xHeader1.setDescription("Version 1.0 06062007\n");
					scrollPane1.setViewportView(panel);
				}
				contentPanel.add(scrollPane1, cc.xy(1, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				// 2014
				RowSpec[] aRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					aRowSpec));
//				buttonBar.setLayout(new FormLayout(
//						new ColumnSpec[] {
//							FormFactory.GLUE_COLSPEC,
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
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JXHeader xHeader1;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables




//	public static void main(String[] args) throws URISyntaxException {
//	    final URI uri = new URI("http://java.sun.com");
//	    class OpenUrlAction implements ActionListener {
//	      @Override public void actionPerformed(ActionEvent e) {
//	        open(uri);
//	      }
//	    }
//	    JFrame frame = new JFrame("Links");
//	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	    frame.setSize(100, 400);
//	    Container container = frame.getContentPane();
//	    container.setLayout(new GridBagLayout());
//	    JButton button = new JButton();
//	    button.setText("<HTML>Click the <FONT color=\"#000099\"><U>link</U></FONT>"
//	        + " to go to the Java website.</HTML>");
//	    button.setHorizontalAlignment(SwingConstants.LEFT);
//	    button.setBorderPainted(false);
//	    button.setOpaque(false);
//	    button.setBackground(Color.WHITE);
//	    button.setToolTipText(uri.toString());
//	    button.addActionListener(new OpenUrlAction());
//	    container.add(button);
//	    frame.setVisible(true);
//	  }

	  private static void open(URI uri) {
	    if (Desktop.isDesktopSupported()) {
	      try {
	        Desktop.getDesktop().browse(uri);
	      } catch (IOException e) { /* TODO: error handling */ }
	    } else { /* TODO: error handling */ }
	  }
}
