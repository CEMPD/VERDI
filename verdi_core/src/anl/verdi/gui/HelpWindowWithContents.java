package anl.verdi.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import anl.gui.window.dialog.OKWindow;

public class HelpWindowWithContents extends OKWindow {
  /**
	 * 
	 */
	private static final long serialVersionUID = 7290813555804895372L;
JEditorPane editorPaneLeft,editorPaneRight;
  public HelpWindowWithContents (Dialog dialog,String title, String doc,String doc2){
    super(dialog,title,false);
    inits(doc,doc2);
  }
  public HelpWindowWithContents(Frame frame, String title, String doc,String doc2) {
    //super(fr.getFrame(),title,state);
    super(frame, title, false);
    inits(doc,doc2);
  }
  public void inits(String doc,String doc2){
    Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
    
    JSplitPane splitPanel = new JSplitPane();
    splitPanel.setBorder(new javax.swing.border.EtchedBorder());

    editorPaneLeft = new JEditorPane();
    editorPaneLeft.setEditable(false);
    editorPaneLeft.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          
          if (e.getDescription().startsWith("#")) {
            // move to the link
            editorPaneRight.scrollToReference(e.getDescription().substring(1));
            
          }
        }
      }
    });
    //JPanel leftPanel = new JPanel(new BorderLayout());
    //leftPanel.add(editorPaneLeft,BorderLayout.CENTER);
    
    editorPaneRight = new JEditorPane();
    editorPaneRight.setEditable(false);
    editorPaneRight.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          JEditorPane src = (JEditorPane)e.getSource();
          if (e.getDescription().startsWith("#")) {
            // move to the link
            src.scrollToReference(e.getDescription().substring(1));
            
          }
        }
      }
    });
    //JPanel rightPanel = new JPanel(new BorderLayout());
    //rightPanel.add(editorPaneRight,BorderLayout.CENTER);
    
    JScrollPane editorScrollPaneLeft = new JScrollPane(editorPaneLeft);
    editorScrollPaneLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    editorScrollPaneLeft.setPreferredSize(new Dimension(200, 300));
    editorScrollPaneLeft.setMinimumSize(new Dimension(200, 300));
    
    JScrollPane editorScrollPaneRight = new JScrollPane(editorPaneRight);
    editorScrollPaneRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    editorScrollPaneRight.setPreferredSize(new Dimension(500, 300));
    editorScrollPaneRight.setMinimumSize(new Dimension(300, 100));
    
//    setDoc(editorPaneLeft,doc);
    setDoc(editorPaneRight,doc2);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    WindowAdapter catchclose = new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        dispose();
      }
      // remove this if fixed in JDK 1.3.1
      public void windowActivated(WindowEvent ev) {
        ev.getWindow().repaint();
      }
    };
    this.addWindowListener(catchclose);
//    splitPanel.setLeftComponent(editorScrollPaneLeft);
//    splitPanel.setRightComponent(editorScrollPaneRight);
    
    
//    getContentPane().add("Center", splitPanel);
    getContentPane().add("Center", editorScrollPaneRight);
    pack();
    setLocation(100, 100);
    setSize(dim.width-200, dim.height-200);
    validate();
    
 //   splitPanel.setDividerLocation(0.5);
    setVisible(true);
  }
  void setDoc(JEditorPane pane,String doc) {
    if (doc != null) {
      try {
        File file = new File(doc);
        URL url = file.toURI().toURL();
        pane.setPage(url);
      } catch (IOException e) {
	      e.printStackTrace();
        System.err.println("Attempted to read a bad file: " + doc);
      }
    } else {
      System.err.println("Couldn't find file: " + doc);
    }
  }
  public boolean doAction() {
    return true;
  }
  public void dispose() {
    window = null;
    super.dispose();
  }
  static HelpWindowWithContents window;
  public static void showContents(Window frame, String title, String doc, String doc2) {
    if (window == null) {
      if(frame instanceof Dialog)window = new HelpWindowWithContents((Dialog)frame, title, doc,doc2);
      else window=new HelpWindowWithContents((Frame)frame, title, doc,doc2);
    } else {
      window.setTitle(title);
      window.setDoc(window.editorPaneLeft,doc);
      window.setDoc(window.editorPaneRight,doc2);
    }
  }

}
