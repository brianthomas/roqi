/**
 * 
 */
package net.ivoa.roqi.model.builder;

/**
 * @author thomas
 *
 */
public class RegistryResourceIdentifierNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	public RegistryResourceIdentifierNotFoundException() {
	}

	/**
	 * @param message
	 */
	public RegistryResourceIdentifierNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RegistryResourceIdentifierNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RegistryResourceIdentifierNotFoundException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
