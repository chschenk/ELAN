package mpi.eudico.client.annotator.lexicon.lexcom;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.tree.TreeModel;

import nl.mpi.lexiconcomponent.impl.LexiconContext;
import nl.mpi.lexiconcomponent.impl.LexiconImpl;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.Lexicon;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;

/**
 * A "client" that is mainly used for configuration purposes, for linking a tier
 * to a field in a lexical entry.
 * 
 * @author Han Sloetjes
 */
public class LexiconComponentClient implements LexiconServiceClient {
	public static final String CLIENT_TYPE = "ELAN Lexicon Component";
	private String url = "";
	private String user;
	
	/**
	 * Constructor.
	 */
	public LexiconComponentClient() {
		super();
	}

	/**
	 * The current implementation of lexicon link configurations requires that 
	 * factory and client return the same string!
	 * @return a constant to identify this client (and the corresponding factory)
	 */
	@Override
	public String getType() {
		return CLIENT_TYPE;
	}

	@Override
	public String getDescription() {
		// localize
		return "Built-in ELAN lexicon client";
	}

	@Override
	public void setUrl(String lexiconWebserviceUrl) {
		// not supported, this is not based on a web service of which the URL might change
	}

	/**
	 * Returns an empty string since this is not an actual web service client.
	 * 
	 * @return an empty String
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * No need to login to this component, user name and password are not used.
	 */
	@Override
	public void setUsername(String username) {
		// not supported, or could this be useful?
		this.user = username;
	}

	@Override
	public String getUsername() {
		// not supported, or could this be useful?
		if (user != null) {
			return user;
		}
		return "";
	}

	@Override
	public void setPassword(String password) {
		// not supported
	}

	@Override
	public String getPassword() {
		// not supported
		return null;
	}

	/**
	 * For storing configurations in EAF and loading from EAF these identifier
	 * objects have to be created.
	 * 
	 * @return a list of lexicon identifiers (currently the name of a lexicon is used as the id)
	 */
	@Override
	public ArrayList<LexiconIdentification> getLexiconIdentifications()
			throws LexiconServiceClientException {
		try {
			String[] lexiconNames = LexiconContext.getInstance().getAvailableLexicons();
			
			if (lexiconNames != null) {
				ArrayList<LexiconIdentification> lexIds = new ArrayList<LexiconIdentification>(lexiconNames.length);
				for (String lName : lexiconNames) {
					// this (probably?) forces loading of the lexicon, needed for the description of lexicon
					LexiconImpl lexImpl = LexiconContext.getInstance().getLexicon(lName);
					if (lexImpl != null) {
						LexiconIdentification li = new LexiconIdentification(lName, lName);// reconsider setting the ID
						li.setDescription(lexImpl.getHeader().getDescription());
						lexIds.add(li);
					} else {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.info(String.format("A lexicon name exists (%s) but the lexicon could not be loaded.", lName));
						}
					}
				}
				// store the list (semi-)permanently?
				return lexIds;
			} else {
				// throw exception or return an empty list?
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("No lexicons found.");
				}
				return new ArrayList<LexiconIdentification>(0);
			}
		} catch (Throwable t) {
			throw new LexiconServiceClientException(t);
		}

	}

	/**
	 * Would require conversion of LexiconImpl (
	 * nl.mpi.lexiconcomponent.impl.LexiconImpl) to Lexicon (mpi.eudico.server.corpora.lexicon.Lexicon)
	 * 
	 * Note: although this is part of the (old) lexicon API (intended for lexicon web services), it is
	 * all right for a client to just return null here. mpi.eudico.server.corpora.lexicon.Lexicon seems
	 * to be used only for visualization (of search results).
	 * 
	 * @param lexId the id of the lexicon to return
	 * @return null
	 * @throws LexiconServiceClientException 
	 */
	@Override
	public Lexicon getLexicon(LexiconIdentification lexId)
			throws LexiconServiceClientException {
		return null;
	}

	/**
	 * Returns a flat list of entry field names present in a specific
	 * nl.mpi.lexiconcomponent.impl.LexiconImpl instance. The name of 
	 * a field is also used as its id (since fields as such don't have
	 * an id in the lexicon component schema).  
	 *  
	 * @return a flat list of entry field names
	 */
	@Override
	public ArrayList<LexicalEntryFieldIdentification> getLexicalEntryFieldIdentifications(
			LexiconIdentification lexId) throws LexiconServiceClientException {
		if (lexId == null) {
			throw new LexiconServiceClientException("The lexicon identifier is null");
		}
		
		try {
			String[] lexiconNames = LexiconContext.getInstance().getAvailableLexicons();
			
			for (String lName : lexiconNames) {
				if (lexId.getName().equals(lName)) {
					LexiconImpl lexImpl = LexiconContext.getInstance().getLexicon(lName);
					
					if (lexImpl != null) {
						List<String> entryFieldNames = new ArrayList<String>();
						entryFieldNames = lexImpl.getEntryFieldNames();
						// in LexiconComponent a field does not have an id String
						ArrayList<LexicalEntryFieldIdentification> fieldIds = new ArrayList<LexicalEntryFieldIdentification>();
						for (String fieldName : entryFieldNames) {
							fieldIds.add(new LexicalEntryFieldIdentification("", fieldName));
						}
						return fieldIds;
					} else {
						throw new LexiconServiceClientException(String.format("The Lexicon named \'%s\' could not be found", lName));
					}
				}
			}
		} catch (Throwable t) {
			// log
			throw new LexiconServiceClientException("The entry fields could not be listed: " + t.getMessage());
		}
		
		return null;
	}

	/**
	 * Would require conversion of LexiconImpl to mpi.eudico.server.corpora.lexicon.Lexicon
	 * 
	 * Note: although this is part of the (old) lexicon API (intended for lexicon web services), it is
	 * all right for a client to just return null here. mpi.eudico.server.corpora.lexicon.Lexicon seems
	 * to be used only for visualization (of search results).
	 * 
	 * @param lexId
	 * @return null
	 * @throws LexiconServiceClientException
	 */
	@Override
	public TreeModel getLexicalEntryStructure(LexiconIdentification lexId)
			throws LexiconServiceClientException {
		// not supported
		return null;
	}

	/**
	 * @return null
	 */
	@Override
	public ArrayList<String> getSearchConstraints() {
		// search not supported yet, no specific search constraints to report
		return null;
	}

	/**
	 * @return null
	 */
	@Override
	public Lexicon search(LexiconIdentification lexId,
			LexicalEntryFieldIdentification fldId, String constraint,
			String searchString) throws LexiconServiceClientException {
		// search in this context not supported
		return null;
	}

}
