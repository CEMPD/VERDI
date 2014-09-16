/*CopyrightHere*/
package simphony.util.messages;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is a layout that strips from the messages the internal anchor of the message center. So a 
 * message goes from "[main] FATAL MessageCenter.INTERNAL.simphony.util.messages.Log4jMessageListener blas"
 * to "[main] FATAL simphony.util.messages.Log4jMessageListener blas" 
 * 
 * @author Jerry Vos
 */
public class MessageCenterLayout extends PatternLayout {
	public MessageCenterLayout(String pattern) {
		super(pattern);
		// TODO Auto-generated constructor stub
		//	2014: had to do this to get rid of an Eclipse error:
		// Implicit super constructor PatternLayout() is undefined for default constructor. Must define an explicit constructor.
	}

	public static final String REPLACED_STRING = Log4jMessageListener.INTERNAL_ANCHOR + ".";
	
	@Override
	public String format(LoggingEvent event) {
		String superFormat = super.format(event);
		
		return superFormat.replace(REPLACED_STRING, "");
	}
}
