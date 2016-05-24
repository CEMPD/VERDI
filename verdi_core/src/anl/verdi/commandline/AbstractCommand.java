package anl.verdi.commandline;

import java.util.ArrayList;

/**Interface for handling commands entered from the command line
 * 
 * @author Amanda Wagner
 */
public abstract interface AbstractCommand {
	
	public void run(ArrayList<String> args);
}
