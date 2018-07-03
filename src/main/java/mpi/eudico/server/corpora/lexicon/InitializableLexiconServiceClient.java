package mpi.eudico.server.corpora.lexicon;

/**
 * Interface for a initializable lexicon service client
 * @author michahulsbosch
 *
 */
public interface InitializableLexiconServiceClient extends LexiconServiceClient {
	public void initialize(Boolean doInBackground) throws LexiconServiceClientException;
}
