
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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.PtyMode;

//import net.lag.jaramiko.Channel;
//import net.lag.jaramiko.ClientTransport;
import anl.verdi.core.VerdiApplication;
import anl.verdi.util.Tools;

public class RemoteFileReader extends JDialog
  implements ActionListener, MouseListener, ListSelectionListener {
	
  public static List<String> TEMP_REMOTE_FILE_LIST = new ArrayList<String>();

  // Attributes:

  private static final long READ_TIMEOUT = 2;
  private static final long serialVersionUID = 7610862979074837762L;//serialver
  private static final int SSH_PORT = 22;
  private static final int CONNECTION_TIMEOUT_SECONDS = 15;
  private static final String remoteFileUtil = System.getProperty(Tools.REMOTE_UTIL_PATH);
  private static final String remoteSSHPath = System.getProperty(Tools.SSH_PATH);
  private static final String remoteFileUtilityPath = (remoteFileUtil == null || remoteFileUtil.trim().isEmpty()) ?
		  "/usr/local/bin/RemoteFileUtility" : remoteFileUtil.trim();
  private static final String remoteSSHCmd = (remoteSSHPath == null || remoteSSHPath.trim().isEmpty()) ?
		  "/usr/bin/ssh" : remoteSSHPath.trim();
  private static String[] remoteHostDisplayNames = null;
  private static String[] remoteHostFullNames = null;
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
  private final JButton deferredConnectButton = new JButton( "" );
  private final JTextField remoteDirectoryField = new JTextField( 80 );
  private final JList remoteDirectoryListing = new JList();
  private final JList variableListing = new JList();
  private final JButton readButton = new JButton( "Read" );
  private final JButton cancelButton = new JButton( "Cancel" );
  private Process remoteFileUtilityProcess = null;
  private ChannelShell sshdRemoteFileUtilityChannel = null;
  private ClientSession sshdSession = null;
  private SshClient sshdClient = null;
  private PipedOutputStream localOut = null;
  private PipedInputStream localIn = null;

  private String remoteDirectory = null;
  private String remoteFileName = null;
  private String previousRemoteUserName = "";
  private int previousRemoteHostIndex = -1;
  private final byte[] readBuffer = new byte[ 16384 ];
  private final StringBuffer stringBuffer = new StringBuffer( 1024 );
  
  private static final Pattern IPV4_PATTERN = Pattern.compile(
          "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

  public static boolean isValidIPv4(String ipAddress) {
      return IPV4_PATTERN.matcher(ipAddress).matches();
  }


  // Read property file to initialize remoteHostNames and RemoteHostDomains:

  public static void readRemoteHostNames( final String remoteHosts ) {

    if ( remoteHosts != null && remoteHosts.length() > 0 ) {
      final String[] hosts = remoteHosts.split( "," );
      final int count = hosts.length;

      if ( count > 0 ) {
        remoteHostDisplayNames = new String [ count ];
        remoteHostFullNames = new String [ count ];

        for ( int index = 0; index < count; ++index ) {
          final String host = hosts[ index ];
          final int dotIndex = host.indexOf( '.' );
          
    	  remoteHostDisplayNames[index] = host;
    	  remoteHostFullNames[index] = host;
          if (!isValidIPv4(hosts[ index ]) && host.indexOf(".") > 1) {
        	  remoteHostDisplayNames[index] = remoteHostDisplayNames[index].substring(0, remoteHostDisplayNames[index].indexOf("."));
          }

        }
      }
    } else {
    	remoteHostDisplayNames = new String[] { "localhost" };
    	remoteHostFullNames = new String[] { "localhost" };
    }
  }

  // Destructor:

  protected void close() {

	  if (sshdRemoteFileUtilityChannel != null) {
		  sshdRemoteFileUtilityChannel.close(false);
		  sshdRemoteFileUtilityChannel = null;
	  }
	  
	  
	  if (sshdSession != null) {
		  sshdSession.close(false);
		  sshdSession = null;
	  }
	  
	  if (sshdClient != null) {
		  sshdClient.stop();
		  sshdClient = null;
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
    remoteHostName = new JComboBox( remoteHostDisplayNames );

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
    deferredConnectButton.addActionListener( this );
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
    } else if (( source == remotePassword || source == connectButton ) && isVisible()) {
      readButton.setEnabled( false );
      clearDirectoryListing();
      clearVariableListing();
      busyCursor( true );
      //Listings and cursors don't update while event is being handled.  Make the updates,
      //allow the event to finish, and fire a new event to actually connect
      deferredConnectButton.doClick(); 
    } else if (source == deferredConnectButton) {
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
        final String selection = ((String) selectedValue).trim();

        if ( selection.equals( "../" ) ) {
          final int lastSlash = remoteDirectory.lastIndexOf( '/' );

          if ( lastSlash >= 0 && remoteDirectory.length() > 1 ) {
            remoteDirectory =
              lastSlash == 0 ? "/" : remoteDirectory.substring( 0, lastSlash );
            while (remoteDirectory.endsWith("/") && remoteDirectory.length() > 1)
            	remoteDirectory = remoteDirectory.substring(0, remoteDirectory.length() - 1);
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
      remoteFileUtilityProcess != null || sshdRemoteFileUtilityChannel != null;
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
        remoteDirectory = directory;
        if (directory.endsWith("/"))
        	remoteDirectory = directory.substring( 0, directory.length() - 1 );
        remoteDirectoryField.setText( remoteDirectory );
      } else {
    	  previousRemoteHostIndex = -1;
      }
    }
  }

  // Update remoteDirectoryListing with contents of changed remote directory:

  private void updateRemoteDirectoryListing() {
    final String command = "ls " + remoteDirectory;
    final String listing = remoteFileUtilityDo( command );
    final String[] remoteDirectoryList = listing.split("\n");
    remoteDirectoryListing.setListData( remoteDirectoryList );
    clearVariableListing();

    if ( listing.length() < 5 ) {
      final String[] message = { "Empty or unreadable directory." };
      variableListing.setListData( message );
      previousRemoteHostIndex = -1;
    }
  }

  // Update variableListing with changed remoteFile:

  private void updateVariableListing() {
    final String slash = remoteDirectory.length() > 1 ? "/" : "";
    final String command =
      "variables " + remoteDirectory + slash + remoteFileName;
    final String listing = remoteFileUtilityDo( command );

    if ( listing.length() > 0 ) {
      final String[] variableList = listing.split("\n");
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
    //System.err.println("Remote command: " + command);
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
      previousRemoteHostIndex = -1;
    }

    return result;
  }

  // Launch remote RemoteFileUtility process using RemoteFileUtilityProcess.

  private boolean remoteFileUtilityProcessConnect() {
    boolean result = false;

    String hostName = null;
    try {
      final String userName = remoteUserName.getText();
      final int hostIndex = remoteHostName.getSelectedIndex();
      hostName =
        remoteHostFullNames[ hostIndex ];
      final String remoteUserAtHost =
        userName.length() > 0 ? userName + "@" + hostName : hostName;
      final String command =
    	  remoteSSHCmd + " " + remoteUserAtHost + " " +
        remoteFileUtilityPath + " loop\n";
      remoteFileUtilityProcess = Runtime.getRuntime().exec( command );
      result = remoteFileUtilityProcess != null;
    } catch ( Exception e ) {
    	VerdiApplication.getInstance().getGui().showError("RemoteFileReader", "Unable to execute " + remoteFileUtilityPath + " on " + hostName);
    	e.printStackTrace();
      result = false;
    }

    return result;
  }

  // Launch remote RemoteFileUtility process using remoteFileUtilityChannel.

  private boolean remoteFileUtilityChannelConnect() {
	boolean result = false;
    final String userName = remoteUserName.getText();
    final int hostIndex = remoteHostName.getSelectedIndex();
    final String hostName =
      remoteHostFullNames[ hostIndex ];
 
    try {
        
        sshdClient = SshClient.setUpDefaultClient();
        sshdClient.start(); 

        // using the client for multiple sessions...
        sshdSession = sshdClient.connect(userName, hostName, SSH_PORT)
                    .verify(CONNECTION_TIMEOUT_SECONDS * 1000)
                    .getSession();
        sshdSession.addPasswordIdentity(new String(remotePassword.getPassword()));
        
        sshdSession.auth().verify(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        
        String command = remoteFileUtilityPath + " loop\n";

        ChannelShell channel = sshdSession.createShellChannel();
        Map<PtyMode, Integer> modes = channel.getPtyModes();
        modes.put(PtyMode.CS8, 1);//0
        modes.put(PtyMode.ICANON, 0);//0
        modes.put(PtyMode.ECHO, 0);
        modes.put(PtyMode.ONLCR, 0);
     //   modes.put(PtyMode.ONOCR, 0);
        modes.put(PtyMode.ONOCR, 0);
        
        modes.put(PtyMode.ONLRET, 0);
        modes.put(PtyMode.OPOST, 0);

        modes.put(PtyMode.INLCR, 0);//0
        modes.put(PtyMode.OCRNL, 0);
        
        
        channel.setPtyModes(modes);
       /* modes = channel.getPtyModes();
        System.out.println("Modes: " + modes);
        for (PtyMode mode : modes.keySet()) {
        	System.out.println("Mode " + mode + ": " + modes.get(mode));
        }*/
        sshdRemoteFileUtilityChannel = channel;
        
        localOut = new PipedOutputStream();
        PipedInputStream channelIn = new PipedInputStream(localOut);
        
        PipedOutputStream channelOut = new PipedOutputStream();
        localIn = new PipedInputStream(channelOut);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        
        channel.setIn(channelIn);
        channel.setOut(channelOut);
        channel.setErr(err);
     
        
        //keep channel open
        result = channel.open().await(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        localOut.write(command.getBytes());
        
        InputStream readStream = getRemoteFileUtilityInputStream();
        String ret = readInput((PipedInputStream)readStream, READ_TIMEOUT);
        result = ret.length() > 0;

       
     //   channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CONNECTION_TIMEOUT_SECONDS);
     //   int exitStatus = channel.getExitStatus();  
        
      
    } catch ( Throwable e ) {
    	
        VerdiApplication.getInstance().getGui().showError("RemoteFileReader", "Unable to connect to " + hostName);
        e.printStackTrace();
        
        close();


    }

    return result;
  }

  // Get RemoteFileUtility input stream.

  private InputStream getRemoteFileUtilityInputStream() {
    final InputStream result =
      remoteFileUtilityProcess != null ?
        remoteFileUtilityProcess.getInputStream()
      : localIn;
    return result;
  }

  // Get RemoteFileUtility output stream.

  private OutputStream getRemoteFileUtilityOutputStream() {
    final OutputStream result =
      remoteFileUtilityProcess != null ? remoteFileUtilityProcess.getOutputStream()
      : sshdRemoteFileUtilityChannel != null ? sshdRemoteFileUtilityChannel.getInvertedIn()
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
  
  private String readInput(InputStream in ) throws IOException, InterruptedException {
	  return readInput(in, READ_TIMEOUT);
  }
  
  private String readInput(InputStream in, long timeout ) throws IOException, InterruptedException {
	  int bytesRead = 0;
	  int oldBytesRead = bytesRead;
	  timeout *= 1000;

	    stringBuffer.delete( 0, stringBuffer.length() );
	  long readTimeoutTime = System.currentTimeMillis() + timeout;
	  //wait for data to appear
      while (in.available() <=0 && System.currentTimeMillis() < readTimeoutTime)
    	  Thread.sleep(100);
      //read available data
      if (in.available() < 1)
    	  System.err.println("No data received after " + Math.round(timeout / 1000) + "s");

      while (in.available() > 0) {
          bytesRead = in.read( readBuffer );
          String nextLine = new String( readBuffer, 0, bytesRead);
          if ( bytesRead > 0 )
          	stringBuffer.append(nextLine);
    	  readTimeoutTime = System.currentTimeMillis() + timeout;
          while (in.available() <=0 && System.currentTimeMillis() < readTimeoutTime) {
          	Thread.sleep(100);
          }
      }
      return stringBuffer.toString().trim();

  }

  // Send a command to the RemoteFileUtility process and return String result.

  private String remoteFileUtilityDo( String command ) {
	  //System.err.println("Remote command: " + command);
		String result = "";
		stringBuffer.delete( 0, stringBuffer.length() );
		
		try {
			InputStream readStream = getRemoteFileUtilityInputStream();
			OutputStream writeStream = getRemoteFileUtilityOutputStream();
			writeStream.write( ( command + "\n" ).getBytes() );
			writeStream.flush();

			result = readInput(readStream);
			//System.out.println("Command result: " + result);
			int sizeEnd = result.indexOf("\n");
			String sizeStr = result.substring(0, sizeEnd);
			if (sizeStr.trim().equals(command)) { //Got local echo
				sizeEnd = result.indexOf("\n", sizeEnd + 1);
			}
			result = result.substring(sizeEnd + 1);
			return result;

    	/*if (readStream.available()< 1) {

    	}*/



    } catch ( Exception e ) {
    	e.printStackTrace();
    	final String[] message = {
	    	        "Sorry, no response from server.",
	    	        "Please try connecting again."
    	};

    	variableListing.clearSelection();
    	variableListing.setListData( message );
    	previousRemoteHostIndex = -1;
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
      long readStart = System.currentTimeMillis();
      while (readStream.available() < 1024 || System.currentTimeMillis() - readStart < 15000) {
    	  Thread.sleep(100);
      }
      int bytesRead = readStream.read( readBuffer );

      if ( bytesRead > 1 ) {
        fileStream = new FileOutputStream( fileName );

        int totalBytesToRead = 0;
        int totalBytesRead = 0;

        String firstLine = new String( readBuffer, 0, bytesRead - 1 );
        
        //Detect line ending
    	String remoteEnding = null;
    	int lineEndOffset = -1;
    	String[] LINE_ENDINGS = new String[] { "\r\n", "\r", "\n" };
    	for (int i = 0; i < LINE_ENDINGS.length; ++i) {
    		if (firstLine.indexOf(LINE_ENDINGS[i]) >= 0) {
    			int endingOffset = firstLine.indexOf(LINE_ENDINGS[i]);
    			if (lineEndOffset < 0 || endingOffset < lineEndOffset) {
    				remoteEnding = LINE_ENDINGS[i];
    				//System.err.println("Detected ending " + i + " at " + endingOffset);
    				lineEndOffset = endingOffset;
    			}
    		}
    	}
    	if (remoteEnding == null) {
    		remoteEnding = "\n";
    	}
        
    	String[] lines = firstLine.split(remoteEnding);
    	
		//System.out.println("Command result: " + result);

		int dataOffset = firstLine.indexOf(remoteEnding) + remoteEnding.length();
		if (lines[0].trim().equals(command.trim())) { //Got local echo
			if (lines.length > 1) {
				//local echo
				lines[0] = lines[1];
				dataOffset = firstLine.indexOf(remoteEnding, dataOffset) + remoteEnding.length();
			}
		} 
			try {
				Integer.parseInt(lines[0]);
			} catch (Throwable t) {
				//System.err.println("Executed command: " + command + ": " + command.length());
				//t.printStackTrace();
				/*StringBuffer buf = new StringBuffer();
				for (int i = command.length() - 2; i < command.length() + 2 && i < firstLine.length(); ++i) {
					buf.append(Integer.toString((int)firstLine.charAt(i)) + " " );
				}
				System.err.println("Ret length: " + firstLine.length());
				System.err.println(buf);
				System.err.println(firstLine);
				System.err.println("L0: " + lines[0]);
				*/
				if (lines.length <= 1) {
					//System.err.println("No newlines");
					lines = firstLine.split(remoteEnding);
				}
				/* else {
					System.err.println("L1: "+ lines[1]);
				}*/
					
				dataOffset = lines[0].length() + lines[1].length() + remoteEnding.length() * 2;
				lines[0] = lines[1];
			}
		
    	
    	totalBytesToRead = Integer.parseInt(lines[0]);
    	bytesRead -= dataOffset;
    	totalBytesRead = bytesRead;
        fileStream.write( readBuffer, dataOffset, bytesRead );

        while ( totalBytesRead < totalBytesToRead ) {
          bytesRead = readStream.read( readBuffer );
          if (totalBytesRead + bytesRead > totalBytesToRead) 
        	  bytesRead = totalBytesToRead - totalBytesRead;

          if ( bytesRead > 0 ) {
            fileStream.write( readBuffer, 0, bytesRead );
            fileStream.flush();
            totalBytesRead += bytesRead;
          }
        }

        if (readStream.available() > 0) {
        	bytesRead = readStream.read( readBuffer );
        }
        result = totalBytesRead == totalBytesToRead;
      }
    } catch ( Exception e ) {
    	e.printStackTrace();
      result = false;
    } finally {

      if ( fileStream != null ) {
        try { fileStream.close(); } catch ( Exception unused_ ) {}
      }
    }

    return result;
  }

}

