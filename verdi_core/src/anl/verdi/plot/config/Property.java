package anl.verdi.plot.config;

/**
 * @author Nick Collier
* @version $Revision$ $Date$
*/
public class Property {
	private Class propType;
	private String key;
	private Object value;


	public Property(String key, Class propType, Object value) {
		this.key = key;
		this.propType = propType;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Class getType() {
		return propType;
	}

	public Object getValue() {
		return value;
	}
}
