package anl.verdi.gui;

/* ******************************************************************
 * 
 * 	COPYRIGHT NOTIFICATION
 * 
 * ***************************************************************** 
 * 
 * COPYRIGHT 2004 UNIVERSITY OF CHICAGO
 * 
 * THIS SOFTWARE DISCLOSES MATERIAL PROTECTABLE UNDER COPYRIGHT LAWS 
 * OF THE UNITED STATES AND FURTHER DISSEMINATION IS PROHIBITED WITHOUT PRIOR WRITTEN CONSENT OF ARGONNE NATIONAL LABORATORY'S PATENT COUNSEL.
 * 
 * *****************************************************************
 * 
 * ARGONNE NATIONAL LABORATORY (ANL), WITH FACILITIES IN THE STATES OF ILLINOIS AND IDAHO, IS OWNED BY THE UNITED STATES GOVERNMENT, AND OPERATED BY THE UNIVERSITY OF CHICAGO UNDER PROVISION OF A CONTRACT WITH THE DEPARTMENT OF ENERGY.
 * 
 * ******************************************************************
 * 
 * 	GOVERNMENT LICENSE AND DISCLAIMER
 * 
 * *****************************************************************
 * NOTICE: The Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after December 2003 the Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.   NEITHER THE UNITED STATES GOVERNMENT NOR ANY AGENCY THEREOF, NOR THE UNIVERSITY OF CHICAGO, NOR ANY OF THEIR EMPLOYEES, MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY INFORMATION, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 *   
 * ******************************************************************
 * LICENSING INQUIRIES MAY BE DIRECTED TO THE OFFICE OF TECHNOLOGY TRANSFER AT ARGONNE NATIONAL LABORATORY.
 * ******************************************************************
 */

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class ExtendedOptionPane {
  /** Type used for <code>showConfirmDialog</code>. */
  public static final int         DEFAULT_OPTION = ExtendedOptionPane.DEFAULT_OPTION;
  /** Type used for <code>showConfirmDialog</code>. */
  public static final int         YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
  /** Type used for <code>showConfirmDialog</code>. */
  public static final int         YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
  /** Type used for <code>showConfirmDialog</code>. */
  public static final int         OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;

  //
  // Return values.
  //
  /** Return value from class method if YES is chosen. */
  public static final int         YES_OPTION = JOptionPane.YES_OPTION;
  /** Return value from class method if NO is chosen. */
  public static final int         NO_OPTION = JOptionPane.NO_OPTION;
  /** Return value from class method if CANCEL is chosen. */
  public static final int         CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
  /** Return value form class method if OK is chosen. */
  public static final int         OK_OPTION = JOptionPane.OK_OPTION;
  /** Return value from class method if user closes window without selecting
   * anything, more than likely this should be treated as either a
   * <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>. */
  public static final int         CLOSED_OPTION = JOptionPane.CLOSED_OPTION;

  //
  // Message types. Used by the UI to determine what icon to display,
  // and possibly what behavior to give based on the type.
  //
  /** Used for error messages. */
  public static final int  ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
  /** Used for information messages. */
  public static final int  INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
  /** Used for warning messages. */
  public static final int  WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
  /** Used for questions. */
  public static final int  QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
  /** No icon is used. */
  public static final int   PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

  /** Bound property name for <code>icon</code>. */
  public static final String      ICON_PROPERTY = JOptionPane.ICON_PROPERTY;
  /** Bound property name for <code>message</code>. */
  public static final String      MESSAGE_PROPERTY = JOptionPane.MESSAGE_PROPERTY;
  /** Bound property name for <code>value</code>. */
  public static final String      VALUE_PROPERTY = JOptionPane.VALUE_PROPERTY;
  /** Bound property name for <code>option</code>. */
  public static final String      OPTIONS_PROPERTY = JOptionPane.OPTIONS_PROPERTY;
  /** Bound property name for <code>initialValue</code>. */
  public static final String      INITIAL_VALUE_PROPERTY = JOptionPane.INITIAL_VALUE_PROPERTY;
  /** Bound property name for <code>type</code>. */
  public static final String      MESSAGE_TYPE_PROPERTY = JOptionPane.MESSAGE_TYPE_PROPERTY;
  /** Bound property name for <code>optionType</code>. */
  public static final String      OPTION_TYPE_PROPERTY = JOptionPane.OPTION_TYPE_PROPERTY;
  /** Bound property name for <code>selectionValues</code>. */
  public static final String      SELECTION_VALUES_PROPERTY = JOptionPane.SELECTION_VALUES_PROPERTY;
  /** Bound property name for <code>initialSelectionValue</code>. */
  public static final String      INITIAL_SELECTION_VALUE_PROPERTY = JOptionPane.INITIAL_SELECTION_VALUE_PROPERTY;
  /** Bound property name for <code>inputValue</code>. */
  public static final String      INPUT_VALUE_PROPERTY = JOptionPane.INPUT_VALUE_PROPERTY;
  /** Bound property name for <code>wantsInput</code>. */
  public static final String      WANTS_INPUT_PROPERTY = JOptionPane.WANTS_INPUT_PROPERTY;
  /**
   * Shows a question-message dialog requesting input from the user. The 
   * dialog uses the default frame, which usually means it is centered on 
   * the screen. 
   *
   * @param message the <code>Object</code> to display
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static String showInputDialog(Object message)
      throws HeadlessException {
      return showInputDialog(null, message);
  }
  public static void setWindowAlwaysOnTop(Component component, boolean state){
    // get the property
    String string=System.getProperty("WINDOW_LAYERS");
    if(string!=null && string.equals("true")){
    if(component!=null&&component instanceof Window){
      ((Window)component).setAlwaysOnTop(state);
    }
    }
  }
  /**
   * Shows a question-message dialog requesting input from the user, with
   * the input value initialized to <code>initialSelectionValue</code>. The 
   * dialog uses the default frame, which usually means it is centered on 
   * the screen. 
   *
   * @param message the <code>Object</code> to display
   * @param initialSelectionValue the value used to initialize the input
   *                 field
   * @since 1.4
   */
  public static String showInputDialog(Object message, Object initialSelectionValue) {
      return showInputDialog(null, message, initialSelectionValue);
  }

  /**
   * Shows a question-message dialog requesting input from the user
   * parented to <code>parentComponent</code>.
   * The dialog is displayed on top of the <code>Component</code>'s
   * frame, and is usually positioned below the <code>Component</code>. 
   *
   * @param parentComponent  the parent <code>Component</code> for the
   *    dialog
   * @param message  the <code>Object</code> to display
   * @exception HeadlessException if
   *    <code>GraphicsEnvironment.isHeadless</code> returns
   *    <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static String showInputDialog(Component parentComponent,
      Object message) throws HeadlessException {
    String retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal= JOptionPane.showInputDialog(parentComponent, message, getString(
          "OptionPane.inputDialogTitle", parentComponent), ExtendedOptionPane.QUESTION_MESSAGE);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }
  static String getString(Object key, Component c) { 
    Locale l = (c == null) ? Locale.getDefault() : c.getLocale();
    return UIManager.getString(key, l);
}
  /**
   * Shows a question-message dialog requesting input from the user and
   * parented to <code>parentComponent</code>. The input value will be
   * initialized to <code>initialSelectionValue</code>.
   * The dialog is displayed on top of the <code>Component</code>'s
   * frame, and is usually positioned below the <code>Component</code>.  
   *
   * @param parentComponent  the parent <code>Component</code> for the
   *    dialog
   * @param message the <code>Object</code> to display
   * @param initialSelectionValue the value used to initialize the input
   *                 field
   * @since 1.4
   */
  public static String showInputDialog(Component parentComponent, Object message, 
         Object initialSelectionValue) {
    String retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal= (String)JOptionPane.showInputDialog(parentComponent, message,
                    getString("OptionPane.inputDialogTitle",
                    parentComponent), ExtendedOptionPane.QUESTION_MESSAGE, null, null,
                    initialSelectionValue);
      }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Shows a dialog requesting input from the user parented to
   * <code>parentComponent</code> with the dialog having the title
   * <code>title</code> and message type <code>messageType</code>.
   *
   * @param parentComponent  the parent <code>Component</code> for the
   *      dialog
   * @param message  the <code>Object</code> to display
   * @param title    the <code>String</code> to display in the dialog
   *      title bar
   * @param messageType the type of message that is to be displayed:
   *                  <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>,
   *      <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static String showInputDialog(Component parentComponent,
      Object message, String title, int messageType)
      throws HeadlessException {
    String retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=(String)JOptionPane.showInputDialog(parentComponent, message, title,
                                     messageType, null, null, null);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Prompts the user for input in a blocking dialog where the
   * initial selection, possible selections, and all other options can
   * be specified. The user will able to choose from
   * <code>selectionValues</code>, where <code>null</code> implies the
   * user can input
   * whatever they wish, usually by means of a <code>JTextField</code>. 
   * <code>initialSelectionValue</code> is the initial value to prompt
   * the user with. It is up to the UI to decide how best to represent
   * the <code>selectionValues</code>, but usually a
   * <code>JComboBox</code>, <code>JList</code>, or
   * <code>JTextField</code> will be used.
   *
   * @param parentComponent  the parent <code>Component</code> for the
   *      dialog
   * @param message  the <code>Object</code> to display
   * @param title    the <code>String</code> to display in the
   *      dialog title bar
   * @param messageType the type of message to be displayed:
   *                  <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>,
   *      <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @param icon     the <code>Icon</code> image to display
   * @param selectionValues an array of <code>Object</code>s that
   *      gives the possible selections
   * @param initialSelectionValue the value used to initialize the input
   *                 field
   * @return user's input, or <code>null</code> meaning the user
   *      canceled the input
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static Object showInputDialog(Component parentComponent,
      Object message, String title, int messageType, Icon icon,
      Object[] selectionValues, Object initialSelectionValue)
      throws HeadlessException {
    Object retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showInputDialog(parentComponent,message,title,messageType,icon,selectionValues,initialSelectionValue);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Brings up an information-message dialog titled "Message".
   *
   * @param parentComponent determines the <code>Frame</code> in
   *    which the dialog is displayed; if <code>null</code>,
   *    or if the <code>parentComponent</code> has no
   *    <code>Frame</code>, a default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static void showMessageDialog(Component parentComponent,
      Object message) throws HeadlessException {
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      showMessageDialog(parentComponent, message, getString(
                  "OptionPane.messageDialogTitle", parentComponent),
                  ExtendedOptionPane.INFORMATION_MESSAGE);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
  }

  /**
   * Brings up a dialog that displays a message using a default
   * icon determined by the <code>messageType</code> parameter.
   *
   * @param parentComponent determines the <code>Frame</code>
   *    in which the dialog is displayed; if <code>null</code>,
   *    or if the <code>parentComponent</code> has no
   *    <code>Frame</code>, a default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param messageType the type of message to be displayed:
   *                  <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>,
   *      <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static void showMessageDialog(Component parentComponent,
      Object message, String title, int messageType)
      throws HeadlessException {
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      JOptionPane.showMessageDialog(parentComponent, message, title, messageType, null);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
  }

  /**
   * Brings up a dialog displaying a message, specifying all parameters.
   *
   * @param parentComponent determines the <code>Frame</code> in which the
   *      dialog is displayed; if <code>null</code>,
   *      or if the <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *                  default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param messageType the type of message to be displayed:
   *                  <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>,
   *      <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @param icon      an icon to display in the dialog that helps the user
   *                  identify the kind of message that is being displayed
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static void showMessageDialog(Component parentComponent,
      Object message, String title, int messageType, Icon icon)
      throws HeadlessException {
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      JOptionPane.showOptionDialog(parentComponent, message, title, ExtendedOptionPane.DEFAULT_OPTION, 
                       messageType, icon, null, null);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
  }

  /**
   * Brings up a dialog with the options <i>Yes</i>,
   * <i>No</i> and <i>Cancel</i>; with the
   * title, <b>Select an Option</b>.
   *
   * @param parentComponent determines the <code>Frame</code> in which the
   *      dialog is displayed; if <code>null</code>,
   *      or if the <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *                  default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @return an integer indicating the option selected by the user
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showConfirmDialog(Component parentComponent,
      Object message) throws HeadlessException {
    int retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showConfirmDialog(parentComponent, message,
                               UIManager.getString("OptionPane.titleText"),
                               ExtendedOptionPane.YES_NO_CANCEL_OPTION);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Brings up a dialog where the number of choices is determined
   * by the <code>optionType</code> parameter.
   * 
   * @param parentComponent determines the <code>Frame</code> in which the
   *      dialog is displayed; if <code>null</code>,
   *      or if the <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *                  default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param optionType an int designating the options available on the dialog:
   *                  <code>YES_NO_OPTION</code>, or
   *      <code>YES_NO_CANCEL_OPTION</code>
   * @return an int indicating the option selected by the user
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showConfirmDialog(Component parentComponent,
      Object message, String title, int optionType)
      throws HeadlessException {
    int retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showConfirmDialog(parentComponent, message, title, optionType,
                               ExtendedOptionPane.QUESTION_MESSAGE);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Brings up a dialog where the number of choices is determined
   * by the <code>optionType</code> parameter, where the
   * <code>messageType</code>
   * parameter determines the icon to display.
   * The <code>messageType</code> parameter is primarily used to supply
   * a default icon from the Look and Feel.
   *
   * @param parentComponent determines the <code>Frame</code> in
   *      which the dialog is displayed; if <code>null</code>,
   *      or if the <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *                  default <code>Frame</code> is used.
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param optionType an integer designating the options available
   *      on the dialog: <code>YES_NO_OPTION</code>,
   *      or <code>YES_NO_CANCEL_OPTION</code>
   * @param messageType an integer designating the kind of message this is; 
   *                  primarily used to determine the icon from the pluggable
   *                  Look and Feel: <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>, 
   *                  <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @return an integer indicating the option selected by the user
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showConfirmDialog(Component parentComponent,
      Object message, String title, int optionType, int messageType)
      throws HeadlessException {
    int retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showConfirmDialog(parentComponent, message, title, optionType,
                              messageType, null);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }

  /**
   * Brings up a dialog with a specified icon, where the number of 
   * choices is determined by the <code>optionType</code> parameter.
   * The <code>messageType</code> parameter is primarily used to supply
   * a default icon from the look and feel.
   *
   * @param parentComponent determines the <code>Frame</code> in which the
   *      dialog is displayed; if <code>null</code>,
   *      or if the <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *      default <code>Frame</code> is used
   * @param message   the Object to display
   * @param title     the title string for the dialog
   * @param optionType an int designating the options available on the dialog:
   *                  <code>YES_NO_OPTION</code>,
   *      or <code>YES_NO_CANCEL_OPTION</code>
   * @param messageType an int designating the kind of message this is, 
   *                  primarily used to determine the icon from the pluggable
   *                  Look and Feel: <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>, 
   *                  <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @param icon      the icon to display in the dialog
   * @return an int indicating the option selected by the user
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showConfirmDialog(Component parentComponent,
      Object message, String title, int optionType,
      int messageType, Icon icon) throws HeadlessException {
    int retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showOptionDialog(parentComponent, message, title, optionType,
                              messageType, icon, null, null);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }
  /**
   * Brings up a dialog with a specified icon, where the initial
   * choice is determined by the <code>initialValue</code> parameter and
   * the number of choices is determined by the <code>optionType</code> 
   * parameter.
   * <p>
   * If <code>optionType</code> is <code>YES_NO_OPTION</code>,
   * or <code>YES_NO_CANCEL_OPTION</code>
   * and the <code>options</code> parameter is <code>null</code>,
   * then the options are
   * supplied by the look and feel. 
   * <p>
   * The <code>messageType</code> parameter is primarily used to supply
   * a default icon from the look and feel.
   *
   * @param parentComponent determines the <code>Frame</code>
   *      in which the dialog is displayed;  if 
   *                  <code>null</code>, or if the
   *      <code>parentComponent</code> has no
   *      <code>Frame</code>, a 
   *                  default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param optionType an integer designating the options available on the
   *      dialog: <code>YES_NO_OPTION</code>,
   *      or <code>YES_NO_CANCEL_OPTION</code>
   * @param messageType an integer designating the kind of message this is, 
   *                  primarily used to determine the icon from the
   *      pluggable Look and Feel: <code>ERROR_MESSAGE</code>,
   *      <code>INFORMATION_MESSAGE</code>, 
   *                  <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *      or <code>PLAIN_MESSAGE</code>
   * @param icon      the icon to display in the dialog
   * @param options   an array of objects indicating the possible choices
   *                  the user can make; if the objects are components, they
   *                  are rendered properly; non-<code>String</code>
   *      objects are
   *                  rendered using their <code>toString</code> methods;
   *                  if this parameter is <code>null</code>,
   *      the options are determined by the Look and Feel
   * @param initialValue the object that represents the default selection
   *                  for the dialog; only meaningful if <code>options</code>
   *      is used; can be <code>null</code>
   * @return an integer indicating the option chosen by the user, 
   *            or <code>CLOSED_OPTION</code> if the user closed
   *                  the dialog
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   <code>true</code>
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showOptionDialog(Component parentComponent,
      Object message, String title, int optionType, int messageType,
      Icon icon, Object[] options, Object initialValue)
      throws HeadlessException {
    int retVal;
    if(parentComponent==null || !(parentComponent instanceof Window))parentComponent=ExtendedDialog.getDefaultParent();
    try{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,true);
      retVal=JOptionPane.showOptionDialog(parentComponent,message,title,optionType,messageType,icon,options,initialValue);
    }
    finally{
      ExtendedOptionPane.setWindowAlwaysOnTop(parentComponent,false);
    }
    return retVal;
  }
}
