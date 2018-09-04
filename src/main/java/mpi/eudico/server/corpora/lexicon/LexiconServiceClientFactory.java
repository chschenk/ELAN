package mpi.eudico.server.corpora.lexicon;

import java.util.ArrayList;


/**
 * The abstract factory which Lexicon Service extension should extend
 * @author Micha Hulsbosch
 *
 */
public abstract class LexiconServiceClientFactory {
	public abstract void setType(String type);
	public abstract String getType();
	public abstract void setDescription(String description);
	public abstract String getDescription();
	public abstract void setDefaultUrl(String url);
	public abstract String getDefaultUrl();
	public abstract LexiconServiceClient createClient();
	public abstract LexiconServiceClient createClient(String url);
	public abstract LexiconServiceClient createClient(String username, String password);
	public abstract LexiconServiceClient createClient(String url, String username, String password);
	public abstract void addSearchConstraint(String content);
	public abstract ArrayList<String> getSearchConstraints();
}
