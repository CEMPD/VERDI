package anl.verdi.commandline;

import java.util.ArrayList;

/**Class whose main purpose is to parse what is received from the command line
 * 
 * @author A. Wagner
 *
 */

public class CommandLineParser {
	
	public static ArrayList<ArrayList<String>> parseCommands(String args[])
	{
		ArrayList<ArrayList<String>> allCommands = new ArrayList<ArrayList<String>>();
		ArrayList<String> thisCommand = new ArrayList<String>();
		
		//Currently I am checking to see if this is the last item in the command or a '\'
		for(int i = 0; i < args.length; i++)
		{
			String thisArgItem = args[i];
			if(args[i].charAt(0) != '-')
			{
				String tempStr = System.getenv(args[i]);
				if(tempStr != null)
				{
					thisArgItem = tempStr;
				}
			}
			
			if(!args[i].equals("\\"))
			{
				thisCommand.add(thisArgItem);
				//thisCommand.add(args[i]);
			}
			
			//if the next item starts with a "-" or we have reached the end of the arguments
			if((i < args.length - 1 && args[i + 1].charAt(0) == '-')
					|| i == args.length - 1)
			{
				allCommands.add(thisCommand);
				thisCommand = new ArrayList<String>();
			}
		}
		
		return allCommands;
	}

	
}
