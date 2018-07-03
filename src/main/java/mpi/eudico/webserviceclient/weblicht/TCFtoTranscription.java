package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.*;

/**
 * A class that creates a transcription from the contents of a TCB string.
 * 
 * @author Han Sloetjes
 */
public class TCFtoTranscription {
	private TranscriptionImpl transcription;
	private TCFParser parser;
	
	private long defDuration = 5000;
	// by default everything is converted to tiers and annotations
	private boolean includeTokens = true;
	private boolean includePOSTags = true;
	private boolean includeLemmas = true;

	/**
	 * Constructor
	 */
	public TCFtoTranscription() {
		super();
	}
	
	/**
	 * Sets the default duration per annotation.
	 * 
	 * @param duration the default duration;
	 */
	public void setDefaultDuration(long duration) {
		defDuration = duration;
	}
	
	/**
	 * Configures which tiers to include.
	 * 
	 * @param includeTokens the tokens tier
	 * @param includePOSTags the pos tag tier
	 * @param includeLemmas the lemmas tier
	 */
	public void setTiersToInclude(boolean includeTokens, boolean includePOSTags, boolean includeLemmas) {
		this.includeTokens = includeTokens;
		this. includePOSTags = includePOSTags;
		this.includeLemmas = includeLemmas;
	}
	
	/**
	 * Creates a transcription based on the TCB input text.
	 * 
	 * @param tcbString the tcb xml returned by a WebLicht service
	 */
	public TranscriptionImpl createTranscription(String tcbString) throws SAXException, IOException {
		if (tcbString != null) {
			//System.out.println(tcbString);
			parser = new TCFParser(tcbString);
			parser.parse();
			
			transcription = new TranscriptionImpl();
			transcription.setNotifying(false);
			// create default tiers
			createTiers();
			// check sentences, tokens, tags
			createAnnotations();
			
			return transcription;
		} else {
			throw new IOException("The TCB input is null.");
		}
	}
	
	/**
	 * Create a default set of tiers.
	 */
	private void createTiers() {
		// sentence
		LinguisticType senType = new LinguisticType(SENT);
		senType.setTimeAlignable(true);// should not be settable
		transcription.addLinguisticType(senType);
		TierImpl senTier = new TierImpl(SENT, null, transcription, senType);
		transcription.addTier(senTier);
		
		if (includeTokens) {
			LinguisticType tokType = new LinguisticType(TOKEN);
			tokType.addConstraint(new SymbolicSubdivision());
			tokType.setTimeAlignable(false);// 
			transcription.addLinguisticType(tokType);
			TierImpl tokTier = new TierImpl(senTier, TOKEN, null, transcription, tokType);
			transcription.addTier(tokTier);
			
			if (includePOSTags) {
				LinguisticType posType = new LinguisticType(POSTAGS);
				posType.addConstraint(new SymbolicAssociation());
				posType.setTimeAlignable(false);
				transcription.addLinguisticType(posType);
				TierImpl posTagTier = new TierImpl(tokTier, POSTAGS, null, transcription, posType);
				transcription.addTier(posTagTier);
			}
			
			if (includeLemmas) {
				LinguisticType lemType = new LinguisticType(LEMMA);
				lemType.addConstraint(new SymbolicAssociation());
				lemType.setTimeAlignable(false);
				transcription.addLinguisticType(lemType);
				TierImpl lemTagTier = new TierImpl(tokTier, LEMMA, null, transcription, lemType);
				transcription.addTier(lemTagTier);
			}
		}
		
	}
	
	/**
	 * Creates annotations based on the elements extracted by the parser.
	 * Called after the creation of a transcription. The parser should not be null either.
	 */
	private void createAnnotations() {
		if (parser == null || transcription == null) {
			return;
		}
		
		final String SPACE = " ";
		List<TCFElement> sentences = parser.getElementsByType(TCFType.SENTENCE);
		long t = 0;
		long dur = defDuration;
		int numProcessedTokens = 0;
		if (sentences != null) {
			for (TCFElement senEl : sentences) {
				if (senEl.getIdRefs() != null) {
					//System.out.println(senEl.getIdRefs());
					String[] toks = senEl.getIdRefs().split(SPACE);
					List<TCFElement> tokens = getTokensWithIDs(toks);
					
					if (tokens != null && tokens.size() > 0) {
						// make a sentence annotation and dependent token/word annotations
						TierImpl sentTier = (TierImpl) transcription.getTierWithId(SENT);
						if (sentTier == null) {
							return;// error message
						}
						AbstractAnnotation sentAnn = (AbstractAnnotation) sentTier.createAnnotation(t, t + dur);
						if (sentAnn == null) {
							return;// message
						}
						StringBuilder sentBuilder = new StringBuilder();
						TCFElement tokEl;
						AbstractAnnotation curTokAnn = null;
						for (int i = 0; i < tokens.size(); i++) {
							tokEl = tokens.get(i);
							if (i != 0 && tokEl.getText().length() > 0 && 
									Character.getType(tokEl.getText().charAt(0)) != Character.END_PUNCTUATION) {
								sentBuilder.append(SPACE);
							}
							sentBuilder.append(tokEl.getText());
							AbstractAnnotation nextAnnotation = null;
							
							if (includeTokens && transcription.getTierWithId(TOKEN) != null) {
								if (curTokAnn == null) {
									nextAnnotation = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(TOKEN)).createAnnotation(
										t + dur / 2, t + dur / 2);
								} else {
									nextAnnotation = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(TOKEN)).createAnnotationAfter(curTokAnn);
								}
							}
							
							if (nextAnnotation != null) {
								if (tokEl.getText() != null) {
									nextAnnotation.setValue(tokEl.getText());
								}
								curTokAnn = nextAnnotation;
								long mid = (nextAnnotation.getBeginTimeBoundary() + nextAnnotation.getEndTimeBoundary()) / 2;
								// immediately create depending lemma / pos annotations?
								if (includePOSTags && transcription.getTierWithId(POSTAGS) != null) {
									TCFElement posEl = getDependentsForToken(tokEl.getId(), TCFType.TAG);
									if (posEl != null) {
										// Note the created tier is POSTags not Tags
										AbstractAnnotation posAnn = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(POSTAGS)).createAnnotation(
												 mid, mid);
										if (posAnn != null && posEl.getText() != null) {
											posAnn.setValue(posEl.getText());
										} // else {} log
									}
								}
								
								if (includeLemmas && transcription.getTierWithId(LEMMA) != null) {
									TCFElement lemEl = getDependentsForToken(tokEl.getId(), TCFType.LEMMA);
									if (lemEl != null) {
										AbstractAnnotation lemAnn = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(LEMMA)).createAnnotation(
												 mid, mid);
										
										if (lemAnn != null && lemEl.getText() != null) {
											lemAnn.setValue(lemEl.getText());
										}
									}
								}
							}						
						}
						sentAnn.setValue(sentBuilder.toString());
						numProcessedTokens += tokens.size();
					}
				} //else no tokens? no sentence
				t += dur;
			}
		} // else no sentences? check tokens?
	}
	
	/**
	 * Collect the tokens with the given id's.
	 * 
	 * @param ids the token id's
	 * @return a list of token elements or null
	 */
	private List<TCFElement> getTokensWithIDs(String[] ids) {
		if (ids == null || ids.length == 0) {
			return null;
		}
		List<TCFElement> tokEls = new ArrayList<TCFElement>(ids.length);
		
		for (String id : ids) {
			// if we can rely on the order of the tokens this could be optimized considerably.
			for (TCFElement tokEl : parser.getElementsByType(TCFType.TOKEN)) {
				if (id.equals(tokEl.getId())) {
					tokEls.add(tokEl);
					break;
				}
			}
		}
		
		return tokEls;
	}
	
	/**
	 * 
	 * @param id the id of the token
	 * @param type the type of dependent annotation
	 * 
	 * @return an element of the specified type or null
	 */
	private TCFElement getDependentsForToken(String id, TCFType type) {
		if (id == null) {
			return null;
		}
		
		List<TCFElement> elemList = parser.getElementsByType(type);
		
		if (elemList != null) {
			String idRefString;
			String[] idRefs;
			for (TCFElement te : elemList) {
				idRefString = te.getIdRefs();
				if (idRefString != null) {
					idRefs = idRefString.split(" ");
					for (int i = 0; i < idRefs.length; i++) {
						if (id.equals(idRefs[i])) {
							return te;
						}
					}
				}
			}
		}
		
		return null;
	}
}
