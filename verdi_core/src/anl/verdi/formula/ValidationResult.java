package anl.verdi.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the result of validating a Formula.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ValidationResult {

	public enum Status {
		FAIL, WARN, PASS
	};

	private Status status = Status.PASS;
	private String message = "";
	private IllegalFormulaException ex;
	private List<FormulaVariable> variables = new ArrayList<FormulaVariable>();

	/**
	 * Creates a failed result using the specified exception.
	 * @param ex the exception containing the details of the failure.
	 * @return a failed result using the specified exception.
	 */
	public static ValidationResult fail(IllegalFormulaException ex) {
		return new ValidationResult(ex, Status.FAIL);
	}

	/**
	 * Creates a warning result using the specified exception.
	 * @param ex the exception containing the details of the warning.
	 * @return a warning result using the specified exception.
	 */
	public static ValidationResult warn(IllegalFormulaException ex) {
		return new ValidationResult(ex, Status.WARN);
	}

	/**
	 * Creates a failed result.
	 * @param message contains the details of the warning.
	 * @return a failed result.
	 */
	public static ValidationResult fail(String message) {
		return new ValidationResult(message, Status.FAIL);
	}

	/**
	 * Creates a passing result.
	 *
	 * @return a passing result.
	 */
	public static ValidationResult pass() {
		return new ValidationResult("passed", Status.PASS);
	}

	/**
	 * Creates a warning result.
	 * @param message contains the details of the warning.
	 * @return a warning result.
	 */
	public static ValidationResult warn(String message) {
		return new ValidationResult(message, Status.WARN);
	}

	private Map<String, Object> props = new HashMap<String, Object>();

	private ValidationResult(IllegalFormulaException ex, Status status) {
		this(ex.getMessage(), status);
		this.ex = ex;
	}

	private ValidationResult(String message, Status status) {
		this.message = message;
		this.status = status;
	}

	/**
	 * Gets the variables in the formula. This will return
	 * an empty list if the validation is not successful.
	 *
	 * @return  the variables in the formula or an empty list if
	 * the validation is not successful.
	 */
	public List<FormulaVariable> getVariables() {
		return variables;
	}

	void setVariables(List<FormulaVariable> variables) {
		this.variables.clear();
		this.variables.addAll(variables);
	}

	/**
	 * Gets the status of the result.
	 *
	 * @return the status of the result.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Gets the message explaining the status.
	 *
	 * @return the message explaining the status.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the exception if any associated with the status.
	 *
	 * @return the exception if any associated with the status. May return null if
	 * there is no exception (e.g. in a passing result).
	 */
	public IllegalFormulaException getException() {
		return ex;
	}

	/**
	 * Gets an arbitrary property from the result.
	 *
	 * @return an arbitrary property from the result.
	 */
	public Object getProperty(String name) {
		return props.get(name);
	}

	/**
	 * Puts a property into this result.
	 *
	 * @param name the name of the property
	 * @param prop the value of the property
	 */
	public void putProperty(String name, Object prop) {
		props.put(name, prop);
	}
}
