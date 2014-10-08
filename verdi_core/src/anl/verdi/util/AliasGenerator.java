package anl.verdi.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

/**
 * Generates unique sequential strings each time getNextAlias is called. The strings
 * are produced in a sequence that runs from [0] to [n].
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class AliasGenerator {
	static final Logger Logger = LogManager.getLogger(AliasGenerator.class.getName());
	private int alias = 1;
	private Set<Integer> usedAlias = new HashSet<Integer>();

	/**
	 * Gets the next alias in the sequence.
	 *
	 * @return the next alias in the sequence.
	 */
	public String getNextAlias() {
		while (usedAlias.contains(alias)) {
			alias++;
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(alias);
		buf.append("]");
		alias++;
		return buf.toString();
	}

	/**
	 * Marks the specified alias as used so it won't be
	 * created again. This is used to insure that any
	 * aliases that are created from the outside are
	 * not re-created here.
	 *
	 * @param alias the alias to mark.
	 */
	public void markAliasUsed(String alias) {
		int index = alias.indexOf("]");
		String val = alias.substring(1, index);
		usedAlias.add(Integer.valueOf(val));
	}
	
	/**
	 * Removes all the alias 
	 *
	 * @param alias the alias to remove.
	 */
	public void clearAlias() {
		usedAlias.clear();
		alias = 1;
	}

	/**
	 * Splits the specified string into the
	 * alias and the rest of the string.
	 *
	 * @param str the string to remove the alias from.
	 * @return an array whose first element is the alias and whose
	 * second element is the remaining string.
	 */
	public String[] splitAlias(String str) {
		int index = str.lastIndexOf("[");
		String name = str.substring(0, index);
		String alias = str.substring(index, str.length());
		return new String[]{alias, name};
	}

	public static void main(String[] args) {
		AliasGenerator gen = new AliasGenerator();
		for (int i = 0; i < 3000; i++) {
			Logger.debug(gen.getNextAlias());
		}
	}
}
