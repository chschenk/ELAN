package mpi.eudico.server.corpora.lexicon;

import mpi.eudico.client.annotator.ElanLocale;

/**
 * Exception thrown by LexiconServiceClient methods
 * @author Micha Hulsbosch
 *
 */
public class LexiconServiceClientException extends Exception {
	public static final String NO_USERNAME_OR_PASSWORD = "No username or password";
	public static final String MALFORMED_URL = "Malformed Url";
	public static final String CLIENT_MALFUNCTION = "Client malfunction";
	public static final String INCORRECT_USERNAME_OR_PASSWORD = "Incorrect username or password";
	public static final String CONNECTION_MALFUNCTION = "CONNECTION_MALFUNCTION";

	public LexiconServiceClientException() {
		// TODO Auto-generated constructor stub
	}

	public LexiconServiceClientException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LexiconServiceClientException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LexiconServiceClientException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public String getMessageLocale() {
		if(getMessage().equals(MALFORMED_URL)) {
			return ElanLocale.getString("LexiconServiceClientException.MalformedUrl");
		} else if(getMessage().equals(CLIENT_MALFUNCTION)) {
			return ElanLocale.getString("LexiconServiceClientException.ClientMalfunction");
		} else if(getMessage().equals(CONNECTION_MALFUNCTION)) {
			return ElanLocale.getString("LexiconServiceClientException.ConnectionMalfunction");
		} else if(getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
			return ElanLocale.getString("LexiconServiceClientException.IncorrectUsernameOrPassword");
		}	
		return null;
	}
}
