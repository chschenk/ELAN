package mpi.eudico.client.annotator.interlinear.edit;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.interlinear.edit.lexicon.LexanLexiconImpl;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import nl.mpi.lexan.analyzers.TextAnalyzerLexiconContext;
import nl.mpi.lexan.analyzers.lexicon.LexEntry;
import nl.mpi.lexan.analyzers.lexicon.LexItem;
import nl.mpi.lexan.analyzers.lexicon.LexanLexicon;
import nl.mpi.lexan.analyzers.lexicon.LexiconChangeEvent;
import nl.mpi.lexan.analyzers.lexicon.LexiconChangeListener;
import nl.mpi.lexiconcomponent.events.LexiconEditListener;
import nl.mpi.lexiconcomponent.events.LexiconEvent;
import nl.mpi.lexiconcomponent.exceptions.LexiconNotFoundException;
import nl.mpi.lexiconcomponent.impl.EntryImpl;
import nl.mpi.lexiconcomponent.impl.LexiconContext;
import nl.mpi.lexiconcomponent.impl.LexiconImpl;
import nl.mpi.lexiconcomponent.impl.SenseImpl;

public class TextAnalyzerLexiconHostContext implements
		TextAnalyzerLexiconContext, LexiconEditListener {
	private File lexiconFolder = new File(Constants.ELAN_DATA_DIR, Constants.LEXAN_LEXICON_DIR);
	
	/**
	 * Implementation note:
	 * We use the reference to the manager to get at the TierFieldMapping,
	 * which is implemented as a GUI dialog.
	 * <p>
	 * Conversely, the manager can reach us via the TextAnalyzerHostContext,
	 * which is an interface completely separate from TextAnalyzerLexiconContext
	 * (in theory at least).
	 */
	private InterlinearizationManager manager;
	
	/**
	 * A mapping from Lexan lexicon instances to ELAN lexicons.
	 * Needed? or just a list?
	 */
	private Map<LexanLexicon, LexiconImpl> lexiconMap;
	private List<LexiconChangeListener> listeners;// TODO moved to LexanLexiconImpl?
	
	public TextAnalyzerLexiconHostContext(InterlinearizationManager manager) {
		// need a reference to TextAnalyzerHostContext or InterlinearizationManager?
		lexiconMap = new HashMap<LexanLexicon, LexiconImpl>();
		listeners = new ArrayList<LexiconChangeListener>();
		this.manager = manager;

		ensureLexiconFolder();
		LexiconContext.getInstance().scanLexiconsFromFolder(lexiconFolder);
	}
	
	/**
	 * @return the Transcription of this context
	 */
	public Transcription getTranscription() {
		return manager.getTranscription();
	}

	@Override // TextAnalyzerLexiconContext
	public List<String> getLexiconNames() {
		List<String> lexNames = new ArrayList<String>();
		
		for (String lexName : getAvailableLexicons()) {
			if (!lexNames.contains(lexName)) {
				lexNames.add(lexName);
			}
		}
		return lexNames;
	}

	/**
	 * Checks if the cache folder already exists and creates it if not.
	 */
	public void ensureLexiconFolder() {
		if (lexiconFolder == null) { // not applicable in the current situation
			lexiconFolder = new File(Constants.ELAN_DATA_DIR, Constants.LEXAN_LEXICON_DIR);
		}
		try {
			if (!lexiconFolder.exists()) {
				try {
					lexiconFolder.mkdir();
				} catch (Throwable t) {// any
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning(String.format("Cannot create the folder for LEXAN lexicons: %s", 
								t.getMessage()));
					}
					return;
				}
			}
		} catch (Throwable thr) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning(String.format("Cannot check the existence of the LEXAN lexicons folder: %s", 
						thr.getMessage()));
			}
		}
		// lexiconFolder should be readable and writable for the owner here 
	}
	
	private String[] getAvailableLexicons() {
		return LexiconContext.getInstance().getAvailableLexicons();
	}

	@Override // TextAnalyzerLexiconContext
	public LexanLexicon getLexicon(String lexiconName) {
		for (String lexName : getAvailableLexicons()) {
			if (lexName.equals(lexiconName)) {
				try {
					LexiconImpl lexImpl = LexiconContext.getInstance().getLexicon(lexName);
					LexanLexiconImpl lexProx = new LexanLexiconImpl(lexImpl);
					lexImpl.addLexiconEditListener(lexProx);
					lexiconMap.put(lexProx, lexImpl);//??
					
					return lexProx;
				} catch (LexiconNotFoundException lnfe) {
					// log...
					System.err.printf("Lexicon not found: %s\n", lnfe.toString());
				}
			}
		}
		
		// Fallback method if the library was not found by getAvailableLexicons():
		File file = new File(Constants.ELAN_DATA_DIR, lexiconName + ".xml");
		
		if (file.exists()) {
			try {
				LexiconImpl lexImpl = LexiconContext.getInstance().loadLexiconFromURL(file.toURI().toURL());
				LexanLexiconImpl lexProx = new LexanLexiconImpl(lexImpl);
				lexImpl.addLexiconEditListener(lexProx);
				lexiconMap.put(lexProx, lexImpl);//??
				
				return lexProx;
			} catch (LexiconNotFoundException lnfe) {
				// log...
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	/**
	 * @return the folder where lexicons are stored as a File object
	 */
	public File getLexiconFolder() {
		return lexiconFolder;
	}
	
	/**
	 * At the moment it is not possible to explicitly set a default, or 
	 * store the last used lexicon as a preference (and treat it as default)
	 * 
	 * @return (currently) the name of the first lexicon in the list
	 */
	public String getDefaultLexiconName() {
		String[] lexicons = getAvailableLexicons();
		if (lexicons.length > 0) {
			return lexicons[0];
		}
		return "";
	}
	
	/**
	 * Tries to initialize and show a dialog for creating a new entry in the lexicon
	 * referenced by the lexicon bundle.
	 * 
	 * @param lexIdBundle the bundle containing the lexicon name, type and the lexicon field
	 * @param fieldValues fieldID to value pairs, should contain at least one mapping, for the 
	 * fieldID in the bundle
	 */
	public void initiateCreateNewEntry(LexiconQueryBundle2 lexIdBundle, Map<String, String> fieldValues) {
		if (lexIdBundle == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("Cannot create Add Entry dialog, no lexicon name and lexicon type specified (LexiconQueryBundle2 is null)");
			}
			return;
		}
		if (fieldValues == null || fieldValues.isEmpty()) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("Cannot create Add Entry dialog, no initial field value(s) specified");
			}
			return;
		}
		
		if (lexIdBundle.getLink().getLexSrvcClntType().equals(LexiconContext.getInstance().PRODUCER)) {
			try {
				LexiconImpl lexicon = LexiconContext.getInstance().getLexicon(lexIdBundle.getLink().getName());
				// this goes via the UI/panel of lexicon component
				manager.getLexiconPanel().createNewEntry(lexicon, fieldValues);
			} catch (LexiconNotFoundException lnfe) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning(String.format("The lexicon named \"%s\" is not found", lexIdBundle.getLink().getName()));
				}
			}
		} else {
			// not supported
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create Add Entry dialog, the lexicon is not of type " + LexiconContext.getInstance().PRODUCER);
			}
		}
	}

	@Override // TextAnalyzerLexiconContext
	public boolean addToLexicalEntry(LexanLexicon lex, LexEntry lexEntry, LexItem lexItem) {
		// TODO Auto-generated method stub
		//System.out.println("addToLexicalEntry");
		return false;
	}


	@Override // TextAnalyzerLexiconContext
	public boolean addEntryToLexicon(LexanLexicon lex, String entryValue) {
		// TODO Auto-generated method stub
		//System.out.println("addEntryToLexicon");
		return false;
	}

	@Override // TextAnalyzerLexiconContext
	public boolean addEntryToLexicon(LexanLexicon lex, Map<String, String> entryValues) {
		// TODO Auto-generated method stub
		//System.out.println("addEntryToLexicon 2");
		return false;
	}
/*
	private void notifyListeners (LexiconChangeEvent event) {
		if (event != null) {
			for (LexiconChangeListener l : listeners) {
				l.lexiconChanged(event);
			}
		}
	}
*/
	// ######################## Lexicon Component Listener implementation ################################
	/**
	 * Notifications from the lexiconComponent to Lexan.
	 * There is no need to pass them on to LexanLexiconImpl since
	 * that is also a listener.
	 * Perhaps this class doesn't need to be a listener at all.
	 */
	@Override // nl.mpi.lexiconcomponent.events.LexiconEditListener
	public void lexiconEdited(LexiconEvent<LexiconImpl> arg0) {
		// TODO notifyListeners
		//System.out.println("lexiconEdited");
	}

	@Override // LexiconEditListener
	public void lexiconEntryEdited(LexiconEvent<EntryImpl> arg0) {
		// TODO notifyListeners
		//System.out.println("lexiconEntryEdited");
	}

	@Override // LexiconEditListener
	public void lexiconSenseEdited(LexiconEvent<SenseImpl> arg0) {
		// TODO notifyListeners
		//System.out.println("lexiconSenseEdited");
	}

}
