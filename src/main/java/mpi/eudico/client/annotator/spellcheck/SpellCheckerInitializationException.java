/**
 * 
 */
package mpi.eudico.client.annotator.spellcheck;

/**
 * @author micha
 *
 */
public class SpellCheckerInitializationException extends Exception {

	/**
	 * 
	 */
	public SpellCheckerInitializationException() {
	}

	/**
	 * @param message
	 */
	public SpellCheckerInitializationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SpellCheckerInitializationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SpellCheckerInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SpellCheckerInitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause);
	}

}
