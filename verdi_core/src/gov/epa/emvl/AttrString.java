// 2014 Appears to never be used. Trying to delete it.
//package gov.epa.emvl;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.font.FontRenderContext;
//import java.awt.font.TextLayout;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.font.TextAttribute;
//import java.text.AttributedCharacterIterator;
//import java.text.AttributedString;
// 
//import javax.swing.JFrame;
//import javax.swing.JPanel;
// 
//public class AttrString{
//  String s = "attributed string to test superscript and subscript.";
//  public AttrString(){
//    JFrame jf = new JFrame("AttributedString");
//    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    jf.add(new MyJPanel(s));
//    jf.setSize(300, 300);
//    jf.setVisible(true);
//  }
//  public static void main(String[] args){
//    new AttrString();
//  }
//  class MyJPanel extends JPanel{
//    AttributedString as = null;
//    AttributedCharacterIterator aci = null;
//    public MyJPanel(String s){
//      this.setPreferredSize(new Dimension(512, 256));
//      as = new AttributedString(s);
// 
//      //Font font = new Font(Font.SANS_SERIF, Font.PLAIN, null);
//      //font = font.deriveFont(32);
// 
//      as.addAttribute(TextAttribute.SIZE, (float)36);
//      as.addAttribute(TextAttribute.FAMILY, Font.MONOSPACED);
//      as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 5, 10);
//      as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 15, 20);
//      as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, 22, 25);
//      aci = as.getIterator();
//    }
//    public void paintComponent(Graphics g){
//      Graphics2D g2d = (Graphics2D) g;
//      FontRenderContext frc = g2d.getFontRenderContext();
//      TextLayout t = new TextLayout(aci, frc);
//      g2d.setColor(Color.RED);
//      t.draw(g2d, (float)10, (float)100);
//    }
//  }
//}
//
//
