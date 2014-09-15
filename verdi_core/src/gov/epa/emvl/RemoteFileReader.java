
/******************************************************************************
PURPOSE: RemoteFileReader.java - Display and navigate the file directory on
         another computer, choose a NetCDF file, list its real variables,
         choose a variable, copy the file subset with the selected variable
         to the local computer.

NOTES:   Uses opensource jaramiko.jar (http://www.lag.net/jaramiko/) and
         requires programs RemoteFileUtility and ncvariable on the remote
         computer.

         javac -g -Xlint RemoteFileReader.java ; java RemoteFileReader

HISTORY: 2010-03-26 plessel.todd@epa.gov, Created.
STATUS: Unreviewed, tested.
******************************************************************************/

package gov.epa.emvl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.lag.jaramiko.Channel;
import net.lag.jaramiko.ClientTransport;
import anl.verdi.util.Tools;

public class RemoteFileReader extends JDialog
  implements ActionListener, MouseListener, ListSelectionListener {
	
  public static List<String> TEMP_REMOTE_FILE_LIST = new ArrayList<String>();

  // Attributes:

  private static final long serialVersionUID = 7610862979074837762L;//serialver
  private static final int SSH_PORT = 22;
  private static final int CONNECTION_TIMEOUT_SECONDS = 15;
  private static final String remoteFileUtil = System.getProperty(Tools.REMOTE_UTIL_PATH);
  private static final String remoteSSHPath = System.getProperty(Tools.SSH_PATH);
  private static final String remoteFileUtilityPath = (remoteFileUtil == null || remoteFileUtil.trim().isEmpty()) ?
		  "/usr/local/bin/RemoteFileUtility" : remoteFileUtil.trim();
  private static final String remoteSSHCmd = (remoteSSHPath == null || remoteSSHPath.trim().isEmpty()) ?
		  "/usr/bin/ssh" : remoteSSHPath.trim();
  private static String[] remoteHostNames = null;
  private static String[] remoteHostDomains = null;
  private static int localCopyFileVersion = 0; // Counter ensures uniqueness.
  private static final String localUserHome = System.getProperty( "user.home" );
  private static final boolean IS_WINDOWS = File.separatorChar == '\\';
  private static final String authorizedKeysFile =
    localUserHome + "/.ssh/authorized_keys";
  private static final boolean AUTHORIZED_KEYS_FILE_EXISTS =
    ( ! IS_WINDOWS && new File( authorizedKeysFile ).exists() );
  private final JTextField remoteUserName = new JTextField( 8 );
  private final JComboBox remoteHostName;
  private final JPasswordField remotePassword = new JPasswordField( 8 );
  private final JButton connectButton = new JButton( "Connect" );
  private final JTextField remoteDirectoryField = new JTextField( 80 );
  private final JList remoteDirectoryListing = new JList();
  private final JList variableListing = new JList();
  private final JButton readButton = new JButton( "Read" );
  private final JButton cancelButton = new JButton( "Cancel" );
  private Process remoteFileUtilityProcess = null;
  private Channel remoteFileUtilityChannel = null;
  private String remoteDirectory = null;
  private String remoteFileName = null;
  private String previousRemoteUserName = "";
  private int previousRemoteHostIndex = -1;
  private final byte[] readBuffer = new byte[ 16384 ];
  private final StringBuffer stringBuffer = new StringBuffer( 1024 );

  // Read property file to initialize remoteHostNames and RemoteHostDomains:

  public static void readRemoteHostNames( final String remoteHosts ) {

    if ( remoteHosts != null && remoteHosts.length() > 0 ) {
      final String[] hosts = remoteHosts.split( "," );
      final int count = hosts.length;

      if ( count > 0 ) {
        remoteHostNames = new String [ count ];
        remoteHostDomains = new String [ count ];

        for ( int index = 0; index < count; ++index ) {
          final String host = hosts[ index ];
          final int dotIndex = host.indexOf( '.' );

          if ( dotIndex > 0 ) {
            final String hostName = host.substring( 0, dotIndex ).trim();
            final String domainName = host.substring( dotIndex ).trim();
            remoteHostNames[ index ] = hostName;
            remoteHostDomains[ index ] = domainName;
          }
        }
      }
    } else {
      remoteHostNames = new String [ 1 ];
      remoteHostDomains = new String [ 1 ];
      remoteHostNames[ 0 ] = "localhost";
      remoteHostDomains[ 0 ] = "";
    }
  }

  // Destructor:

  protected void close() {

    if ( remoteFileUtilityChannel != null ) {
      remoteFileUtilityChannel.close();
      remoteFileUtilityChannel = null;
    }

    if ( remoteFileUtilityProcess != null ) {
      try { remoteFileUtilityProcess.destroy(); } catch (Exception unused_1) {}
      remoteFileUtilityProcess = null;
    }
  }

  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  public String getLocalCopyFileName() {
	  String tempdir = System.getProperty(Tools.TEMP_DIR);
      final String result = ((tempdir == null || tempdir.trim().isEmpty()) ? 
    		  (new File(Tools.getPropertyFile()).getParent()) : tempdir) 
    		  + File.separatorChar + remoteFileName + localCopyFileVersion;
    return result;
  }

  public File getLocalCopyFile() {
    final String localCopyFileName = getLocalCopyFileName();
    final File result = new File( localCopyFileName );
    return result;
  }

  public void setVisible( final boolean visible ) {
	super.setVisible( visible ); // Show or hide this pop-up dialog window.

	if ( visible ) {

      if ( remoteUserName.getText().length() > 0 ) { // User name entered.
        connectButton.doClick(); // Re-connect.
      }
	} else {
      remoteFileUtilityQuit(); // Disconnect.
	}
  }

  // Constructor: remoteHosts is a comma-separated list of
  // remote hosts, e.g., "amber.nesc.epa.gov,vortex.rtpnc.epa.gov".

  public RemoteFileReader( final String remoteHosts ) {
    super();
    setTitle("Remote File Access");
    readRemoteHostNames( remoteHosts );
    remoteHostName = new JComboBox( remoteHostNames );

    // Layout GUI components:

    final Container pane = getContentPane();
    JPanel loginPanel = new JPanel();
    loginPanel.add( new JLabel( "Remote User:" ) );
    loginPanel.add( remoteUserName );
    loginPanel.add( new JLabel( "Host:" ) );
    loginPanel.add( remoteHostName );
    loginPanel.add( new JLabel( "Password:" ) );
    loginPanel.add( remotePassword );
    loginPanel.add( connectButton );
    pane.add(loginPanel, BorderLayout.NORTH);

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    JPanel directoryPanel = new JPanel(new BorderLayout());
    directoryPanel.add( remoteDirectoryField );
    listPanel.add( directoryPanel );
    JPanel title1 = new JPanel(new BorderLayout());
    title1.add(new JLabel( "Double-click a NetCDF file (or directory):" ), BorderLayout.LINE_START);
    listPanel.add(title1);
    JScrollPane files = new JScrollPane( remoteDirectoryListing );
    files.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    files.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    files.setPreferredSize(new Dimension(500, 240));
    listPanel.add( files );
    JPanel title2 = new JPanel(new BorderLayout());
    title2.add(new JLabel( "Select one or more variables:" ), BorderLayout.LINE_START);
    listPanel.add(title2);
    JScrollPane vars = new JScrollPane( variableListing );
    vars.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    vars.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    vars.setPreferredSize(new Dimension(500, 240));
    listPanel.add(vars);
    listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pane.add( listPanel, BorderLayout.CENTER );
    
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.add( readButton );
    buttonsPanel.add( cancelButton );
    pane.add( buttonsPanel, BorderLayout.SOUTH );

    // Configure properties of GUI components:

    final int width = 700;
    final int height = 660;
    connectButton.addActionListener( this );
    remotePassword.addActionListener( this );
    remoteDirectoryField.addActionListener( this );
    remoteDirectoryListing.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
    variableListing.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
    variableListing.addListSelectionListener( this );
    readButton.setEnabled( false );
    readButton.addActionListener( this );
    cancelButton.addActionListener( this );
    remoteDirectoryListing.addMouseListener( this );
    setupEnterActionForAllButtons();
    setSize( width, height );
    setModal( true );
    setLocationRelativeTo(getOwner());
  }

  // Make Enter key 'click' the selected button:

  private static void setupEnterActionForAllButtons() {
    UIManager.put( "Button.defaultButtonFollowsFocus", Boolean.TRUE );
    final InputMap inputMap = (InputMap)
      UIManager.getDefaults().get( "Button.focusInputMap" );
    final Object pressedAction =
      inputMap.get( KeyStroke.getKeyStroke( "pressed SPACE" ) );
    final Object releasedAction =
      inputMap.get( KeyStroke.getKeyStroke( "released SPACE" ) );
    inputMap.put( KeyStroke.getKeyStroke( "pressed ENTER" ), pressedAction );
    inputMap.put( KeyStroke.getKeyStroke( "released ENTER" ), releasedAction );
  }

  // GUI callback for readButton, cancelButton, connectButton:

  public void actionPerformed( final ActionEvent event ) {
    final Object source = event.getSource();

    if ( source == cancelButton ) {
      setVisible( false );
    } else if ( source == readButton ) {
      busyCursor( true );
      subsetRemoteFile();
      busyCursor( false );
    } else if ( source == remotePassword || source == connectButton ) {
      readButton.setEnabled( false );
      clearDirectoryListing();
      clearVariableListing();
      busyCursor( true );

      if ( remoteFileUtilityConnect() ) {
        updateRemoteDirectory();
        updateRemoteDirectoryListing();
      }

      busyCursor( false );
    } else if ( source == remoteDirectoryField ) {
      remoteDirectory = remoteDirectoryField.getText();
      updateRemoteDirectoryListing();
    }
  }

  // GUI callback for mouse-double-clicking on remoteDirectoryListing:

  public void mouseReleased( final MouseEvent event ) {

    if ( isConnected() && event.getClickCount() > 1 &&
         event.getSource() == remoteDirectoryListing ) {
      final Object selectedValue = remoteDirectoryListing.getSelectedValue();

      if ( selectedValue instanceof String ) {
        final String selection = (String) selectedValue;

        if ( selection.equals( "../" ) ) {
          final int lastSlash = remoteDirectory.lastIndexOf( '/' );

          if ( lastSlash >= 0 && remoteDirectory.length() > 1 ) {
            remoteDirectory =
              lastSlash == 0 ? "/" : remoteDirectory.substring( 0, lastSlash );
            remoteDirectoryField.setText( remoteDirectory );
            busyCursor( true );
            updateRemoteDirectoryListing();
            busyCursor( false );
          }
        } else if ( selection.endsWith( "/" ) ) {
          final String slash = remoteDirectory.length() > 1 ? "/" : "";
          remoteDirectory =
            remoteDirectory + slash +
            selection.substring( 0, selection.length() - 1 );
          remoteDirectoryField.setText( remoteDirectory );
          busyCursor( true );
          updateRemoteDirectoryListing();
          busyCursor( false );
        } else {
          remoteFileName = selection;
          busyCursor( true );
          updateVariableListing();
          busyCursor( false );
        }
      }
    }
  }

  public void mouseEntered( final MouseEvent unused_ ) {} // Required, unused.
  public void mouseExited( final MouseEvent unused_ ) {} // Required, unused.
  public void mouseClicked( final MouseEvent unused_ ) {} // Required, unused.
  public void mousePressed( final MouseEvent unused_ ) {} // Required, unused.

  // GUI callback for variableListing:

  public void valueChanged( final ListSelectionEvent event ) {
    final Object source = event.getSource();

    if ( isConnected() && source == variableListing ) {
      readButton.setEnabled( ! variableListing.isSelectionEmpty()  );
    }
  }

  // Set/reset busy cursor:

  private void busyCursor( final boolean on ) {
    final int cursor = on ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR;
    setCursor( Cursor.getPredefinedCursor( cursor ) );
  }

  // Connected to remote host?

  private boolean isConnected() {
    final boolean result =
      remoteFileUtilityProcess != null || remoteFileUtilityChannel != null;
    return result;
  }

  // clearDirectoryListing due to a new connect command:

  private void clearDirectoryListing() {
    final String[] empty = { "" };
    remoteDirectoryListing.clearSelection();
    remoteDirectoryListing.setListData( empty );
  }

  // clearVariableListing due to a changed remoteHostName or directory:

  private void clearVariableListing() {
    final String[] empty = { "" };
    variableListing.clearSelection();
    variableListing.setListData( empty );
  }

  // Update remoteDirectory due to changed remoteHost or remoteUser:

  private void updateRemoteDirectory() {
    final String userName = remoteUserName.getText();
    final int remoteHostIndex = remoteHostName.getSelectedIndex();
    boolean changed = false;

    if ( ! userName.equals( previousRemoteUserName ) ) {
      changed = true;
      previousRemoteUserName = userName;
    }

    if ( remoteHostIndex != previousRemoteHostIndex ) {
      changed = true;
      previousRemoteHostIndex = remoteHostIndex;
    }

    if ( changed ) {
      final String directory = remoteFileUtilityDo( "pwd" );

      if ( directory.length() > 0 ) {
        remoteDirectory = directory.substring( 0, directory.length() - 1 );
        remoteDirectoryField.setText( remoteDirectory );
      }
    }
  }

  // Update remoteDirectoryListing with contents of changed remote directory:

  private void updateRemoteDirectoryListing() {
    final String command = "ls " + remoteDirectory;
    final String listing = remoteFileUtilityDo( command );
    final String[] remoteDirectoryList = stringArray( listing );
    remoteDirectoryListing.setListData( remoteDirectoryList );
    clearVariableListing();

    if ( listing.length() < 5 ) {
      final String[] message = { "Empty or unreadable directory." };
      variableListing.setListData( message );
    }
  }

  // Update variableListing with changed remoteFile:

  private void updateVariableListing() {
    final String slash = remoteDirectory.length() > 1 ? "/" : "";
    final String command =
      "variables " + remoteDirectory + slash + remoteFileName;
    final String listing = remoteFileUtilityDo( command );

    if ( listing.length() > 0 ) {
      final String[] variableList = stringArray( listing );
      variableListing.setListData( variableList );
      
      if ( variableList.length == 1 ) { // If only one variable, select it.
       variableListing.setSelectedIndex( 0 );
      }
    } else {
      final String[] message = { "Not a valid NetCDF file." };
      variableListing.setListData( message );      
    }
  }

  // Copy subset (selected variable only) of remote NetCDF file to local home:

  private void subsetRemoteFile() {
//    final Object[] selectedValues = variableListing.getSelectedValues();	// replaced with getSelectedValuesList(), returns List
    List selectedValues = variableListing.getSelectedValuesList();
    stringBuffer.delete( 0, stringBuffer.length() );

    for ( Object selection : selectedValues ) {

      if ( selection instanceof String ) {
    	final String variable = (String) selection;
        stringBuffer.append( variable );
        stringBuffer.append( ' ' );
      }
    }

    final String slash = remoteDirectory.length() > 1 ? "/" : "";
    final String command =
      "subset " + remoteDirectory + slash + remoteFileName +
      ' ' + stringBuffer;
    ++localCopyFileVersion;
    final String localCopyFileName = getLocalCopyFileName();
    final boolean ok = remoteFileUtilityDoToFile(command, localCopyFileName);

    if ( ok ) {
      TEMP_REMOTE_FILE_LIST.add(localCopyFileName);
      setVisible( false );
    } else {
      final String[] message = {
        "Sorry, failed to subset.",
        "Perhaps check disk space on",
        "local computer home directory."
      };

      variableListing.clearSelection();
      variableListing.setListData( message );
    }
  }

  // Launch remote RemoteFileUtility.

  private boolean remoteFileUtilityConnect() {
    close();
    final boolean passwordGiven = remotePassword.getPassword().length > 0;
    final boolean result =
      ( IS_WINDOWS || passwordGiven ) ? remoteFileUtilityChannelConnect() :
      AUTHORIZED_KEYS_FILE_EXISTS ? remoteFileUtilityProcessConnect() : false;

    if ( ! result ) {
      final String[] message = {
        "Sorry, unable to connect.",
        "Perhaps try a different",
        "remote user/host/password."
      };

      variableListing.clearSelection();
      variableListing.setListData( message );
    }

    return result;
  }

  // Launch remote RemoteFileUtility process using RemoteFileUtilityProcess.

  private boolean remoteFileUtilityProcessConnect() {
    boolean result = false;

    try {
      final String userName = remoteUserName.getText();
      final int hostIndex = remoteHostName.getSelectedIndex();
      final String hostName =
        remoteHostNames[ hostIndex ] + remoteHostDomains[ hostIndex ];
      final String remoteUserAtHost =
        userName.length() > 0 ? userName + "@" + hostName : hostName;
      final String command =
    	  remoteSSHCmd + " " + remoteUserAtHost + " " +
        remoteFileUtilityPath + " loop\n";
      remoteFileUtilityProcess = Runtime.getRuntime().exec( command );
      result = remoteFileUtilityProcess != null;
    } catch ( Exception unused_ ) {
      result = false;
    }

    return result;
  }

  // Launch remote RemoteFileUtility process using remoteFileUtilityChannel.

  private boolean remoteFileUtilityChannelConnect() {
    final String userName = remoteUserName.getText();
    final int hostIndex = remoteHostName.getSelectedIndex();
    final String hostName =
      remoteHostNames[ hostIndex ] + remoteHostDomains[ hostIndex ];
    Socket socket = null;
    ClientTransport transport = null;

    try {
      socket = new Socket();
      socket.connect( new InetSocketAddress( hostName, SSH_PORT ) );
      transport = new ClientTransport( socket );
      final int timeout = CONNECTION_TIMEOUT_SECONDS * 1000; // To milliseconds
      transport.start( null, timeout );
      final String[] next =
        transport.authPassword( userName,
                                new String( remotePassword.getPassword() ),
                                timeout );

      if ( next.length == 0 ) {
        remoteFileUtilityChannel =
          transport.openSession( timeout );
        final String command = remoteFileUtilityPath + " loop\n";
        final int neverTimeout = -1;
        remoteFileUtilityChannel.execCommand( command, neverTimeout );
      }
    } catch ( Exception unused_ ) {

      if ( socket != null ) {
        try { socket.close(); } catch ( Exception unused_2 ) {}
        socket = null;
      }

      if ( transport != null ) {
        try { transport.close(); } catch ( Exception unused_2 ) {}
        transport = null;
      }

      if ( remoteFileUtilityChannel != null ) {
        try {remoteFileUtilityChannel.close();} catch (Exception unused_2) {}
        remoteFileUtilityChannel = null;
      }
    }

    final boolean result = remoteFileUtilityChannel != null;
    return result;
  }

  // Get RemoteFileUtility input stream.

  private InputStream getRemoteFileUtilityInputStream() {
    final InputStream result =
      remoteFileUtilityProcess != null ?
        remoteFileUtilityProcess.getInputStream()
      : remoteFileUtilityChannel.getInputStream();
    return result;
  }

  // Get RemoteFileUtility output stream.

  private OutputStream getRemoteFileUtilityOutputStream() {
    final OutputStream result =
      remoteFileUtilityProcess != null ? remoteFileUtilityProcess.getOutputStream()
      : remoteFileUtilityChannel != null ? remoteFileUtilityChannel.getOutputStream()
      : null;
    return result;
  }

  // Terminate remote RemoteFileUtility process.

  private void remoteFileUtilityQuit() {

    try {
      final OutputStream writeStream = getRemoteFileUtilityOutputStream();

      if ( writeStream != null ) {
        writeStream.write( "quit\n".getBytes() );
        writeStream.flush();
      }
    } catch ( Exception unused_ ) {
    }

    close();
  }

  // Send a command to the RemoteFileUtility process and return String result.

  private String remoteFileUtilityDo( final String command ) {
    String result = "";
    stringBuffer.delete( 0, stringBuffer.length() );

    try {
      final OutputStream writeStream = getRemoteFileUtilityOutputStream();
      writeStream.write( ( command + "\n" ).getBytes() );
      writeStream.flush();
      final InputStream readStream = getRemoteFileUtilityInputStream();
      int bytesRead = readStream.read( readBuffer );

      if ( bytesRead > 1 ) {
        final String firstLine = new String( readBuffer, 0, bytesRead - 1 );
        final int totalBytesToRead = Integer.parseInt( firstLine );
        int totalBytesRead = 0;

        while ( totalBytesRead < totalBytesToRead ) {
          bytesRead = readStream.read( readBuffer );

          if ( bytesRead > 0 ) {
            stringBuffer.append( new String( readBuffer, 0, bytesRead ) );
            totalBytesRead += bytesRead;
          }
        }
      }

    } catch ( Exception unused_ ) {
    }

    result = stringBuffer.toString();
    return result;
  }

  // Send a command to RemoteFileUtility process and copy its output to a file.

  private boolean remoteFileUtilityDoToFile( final String command,
                                             final String fileName) {
    boolean result = false;
    OutputStream fileStream = null;

    try {
      final OutputStream writeStream = getRemoteFileUtilityOutputStream();
      writeStream.write( ( command + "\n" ).getBytes() );
      writeStream.flush();
      final InputStream readStream = getRemoteFileUtilityInputStream();
      int bytesRead = readStream.read( readBuffer );

      if ( bytesRead > 1 ) {
        fileStream = new FileOutputStream( fileName );

        final String firstLine = new String( readBuffer, 0, bytesRead - 1 );
        final int totalBytesToRead = Integer.parseInt( firstLine );
        int totalBytesRead = 0;

        while ( totalBytesRead < totalBytesToRead ) {
          bytesRead = readStream.read( readBuffer );

          if ( bytesRead > 0 ) {
            fileStream.write( readBuffer, 0, bytesRead );
            fileStream.flush();
            totalBytesRead += bytesRead;
          }
        }

        result = totalBytesRead == totalBytesToRead;
      }
    } catch ( Exception unused_ ) {
      result = false;
    } finally {

      if ( fileStream != null ) {
        try { fileStream.close(); } catch ( Exception unused_ ) {}
      }
    }

    return result;
  }

  // Convert String containing multiple '\n' to an array of Strings:

  private static String[] stringArray( final String string ) {
    final int length = string.length();
    int lines = 0;

    for ( int index = 0; index < length; ++index ) {

      if ( string.charAt( index ) == '\n' ) {
        ++lines;
      }
    }

    String[] result = new String[ lines ];
    int line = 0;
    int from = 0;

    for ( int index = 0; index < length; ++index ) {

      if ( string.charAt( index ) == '\n' ) {
        result[ line ] = string.substring( from, index );
        ++line;
        from = index + 1;
      }
    }

    return result;
  }

}

