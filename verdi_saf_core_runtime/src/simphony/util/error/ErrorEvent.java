/*CopyrightHere*/
package simphony.util.error;

/**
 * 
 * 
 * @author Jerry Vos
 * @version $Revision: 1.1 $ $Date: 2005/11/07 18:42:32 $
 */
public class ErrorEvent {
	private int errorCode;

	private Object info;

	private Throwable error;

	private Object[] metaData;

	public ErrorEvent(Throwable error, int errorCode, Object info,
			Object[] metaData) {
		super();
		this.error = error;
		this.errorCode = errorCode;
		this.info = info;
		this.metaData = metaData;
	}

	
	public Throwable getError() {
		return error;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public Object getInfo() {
		return info;
	}

	public Object[] getMetaData() {
		return metaData;
	}
}
