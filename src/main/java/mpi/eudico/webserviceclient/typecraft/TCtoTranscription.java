package mpi.eudico.webserviceclient.typecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;

/**
 * A class to convert typecraft phrase records to a transcription.
 * 
 * @author Han Sloetjes
 *
 */
public class TCtoTranscription {
	// constants
	public static final String PHRASE = "phrase";
	public static final String TRANSLATION = "translation";
	public static final String DESCRIPTION = "description";
	public static final String VALIDITY = "validity";
	public static final String WORDS = "words";
	public static final String HEAD = "head";
	public static final String POS = "pos";
	public static final String MORPH = "morph";
	public static final String BASE = "baseform";
	public static final String MEANING = "meaning";
	public static final String GLOSS = "gloss";
	public static final String GLOSS_DELIMITER = ";";
	public static final String TC_ID_PREFIX = "tc";
	
	private List<String> firstLevelSymAssTiers;
	private List<PhraseRecord> phraseRecords;
	private TextRecord textRecord;
	private Map<String, String> speakerToSuffixMap = new HashMap<String, String>();
	
	private TranscriptionImpl transcription;
	
	private long curTime = 0L;
	private long defPhraseDur = 5000L;
	
	/**
	 * Constructor.
	 */
	public TCtoTranscription() {
		super();
		firstLevelSymAssTiers = new ArrayList<String>();
		firstLevelSymAssTiers.add(TRANSLATION);
		firstLevelSymAssTiers.add(DESCRIPTION);
		firstLevelSymAssTiers.add(VALIDITY);
	}
	
	/**
	 * @param phraseRecords
	 * @param textRecord
	 * 
	 * @return a transcription
	 */
	public TranscriptionImpl createTranscription(List<PhraseRecord> phraseRecords,
			TextRecord textRecord) {
		this.phraseRecords = phraseRecords;
		this.textRecord = textRecord;
		
		if (phraseRecords == null) {
			return null;
		}
		
		transcription = new TranscriptionImpl();
		transcription.setNotifying(false);
		if (textRecord != null) {
			PropertyImpl pi = new PropertyImpl("id", textRecord.id);
			transcription.addDocProperty(pi);
			if (textRecord.lang != null) {
				PropertyImpl la = new PropertyImpl("lang", textRecord.lang);
				transcription.addDocProperty(la);
			}
			if (textRecord.title != null) {
				PropertyImpl ti = new PropertyImpl("title", textRecord.title);
				transcription.addDocProperty(ti);
			}
			if (textRecord.titleTrans != null) {
				PropertyImpl ti = new PropertyImpl("titleTranslation", textRecord.titleTrans);
				transcription.addDocProperty(ti);
			}
			if (textRecord.body != null) {
				PropertyImpl bo = new PropertyImpl("body", textRecord.body);
				transcription.addDocProperty(bo);
			}
		}
		
		addDefaultTypes();
		//addDefaultTiers();
		
		for (PhraseRecord record : phraseRecords) {
			createAnnotations(record); 
		}
		
		return transcription;
	}
	
	private void addDefaultTypes() {
		LinguisticType type = new LinguisticType(PHRASE);
		type.setTimeAlignable(true);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(TRANSLATION);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(DESCRIPTION);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(VALIDITY);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(WORDS);
		type.addConstraint(new SymbolicSubdivision());// or time subdivision?
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(HEAD);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(POS);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(MORPH);
		type.addConstraint(new SymbolicSubdivision());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(BASE);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(MEANING);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
		
		type = new LinguisticType(GLOSS);
		type.addConstraint(new SymbolicAssociation());
		type.setTimeAlignable(false);
		transcription.addLinguisticType(type);
	}
	/*
	private void addDefaultTiers() {
		TierImpl phraseTier = new TierImpl("phrase", "unknown", 
				transcription, transcription.getLinguisticTypeByName("phrase"));
	}
	*/
	
	private void createAnnotations(PhraseRecord record) {
		if (record == null) {
			return;
		}
		long bt, et;
		if (record.bt < record.et && record.et != 0) {
			bt = record.bt;// this might overwrite previous annotations
			et = record.et;
		} else {
			// calculate times
			bt = curTime;
			et = curTime + defPhraseDur;
		}
		curTime = et;
		TierImpl phraseTier;
		String suffix = null;
		// hier... what convention to use? @name, or _A, _B?
		if (record.speaker != null && record.speaker.length() != 0) {
			suffix = speakerToSuffixMap.get(record.speaker);
			if (suffix == null) {
				// add it
				char suf = (char)('A' + speakerToSuffixMap.size());
				String sufStr = "_" + suf;
				speakerToSuffixMap.put(record.speaker, sufStr);
				String tierName = PHRASE + sufStr;
				phraseTier = new TierImpl(tierName, record.speaker, 
						transcription, transcription.getLinguisticTypeByName(PHRASE));
				transcription.addTier(phraseTier);
			} else {
				phraseTier = (TierImpl) transcription.getTierWithId(PHRASE + suffix);
			}
		} else {
			phraseTier = (TierImpl) transcription.getTierWithId(PHRASE);
			if (phraseTier == null) {
				phraseTier = new TierImpl(PHRASE, "", transcription, transcription.getLinguisticTypeByName(PHRASE));
				transcription.addTier(phraseTier);
			}
		}
		AbstractAnnotation rootAnn = (AbstractAnnotation) phraseTier.createAnnotation(bt, et);
		if (rootAnn != null) {
			rootAnn.setValue(record.text);
			rootAnn.setId(TCtoTranscription.TC_ID_PREFIX + record.id);
		} else {
			System.out.println("Could not create annotation on Phrase: " + record.text);
			return;
		}
		
		// add translation, check the tier
		if (record.translation != null) {
			TierImpl trTier = getTier(TRANSLATION, suffix);
			
			if (trTier == null) {
				trTier = createTier(phraseTier, transcription.getLinguisticTypeByName(TRANSLATION), 
						TRANSLATION, suffix);
				transcription.addTier(trTier);
				if (record.speaker != null) {
					trTier.setParticipant(record.speaker);
				}
			}
			if (trTier != null) {
				createDepAnnotationSA(trTier, record.translation, rootAnn);
			}
		}
		// add description
		if (record.description != null) {
			TierImpl trTier = getTier(DESCRIPTION, suffix);
			
			if (trTier == null) {
				trTier = createTier(phraseTier, transcription.getLinguisticTypeByName(DESCRIPTION), 
						DESCRIPTION, suffix);
				transcription.addTier(trTier);
				if (record.speaker != null) {
					trTier.setParticipant(record.speaker);
				}
			}
			if (trTier != null) {
				createDepAnnotationSA(trTier, record.description, rootAnn);
			}
		}
		// add validity
		if (record.valid != null) {
			TierImpl trTier = getTier(VALIDITY, suffix);
			
			if (trTier == null) {
				trTier = createTier(phraseTier, transcription.getLinguisticTypeByName(VALIDITY), 
						VALIDITY, suffix);
				transcription.addTier(trTier);
				if (record.speaker != null) {
					trTier.setParticipant(record.speaker);
				}
			}
			if (trTier != null) {
				createDepAnnotationSA(trTier, record.valid, rootAnn);
			}
		}
		
		// word tier and lower level
		if (record.wordRecords != null && record.wordRecords.size() > 0) {
			WordRecord wrecord;
			AbstractAnnotation wa = null;
			AbstractAnnotation prevwa = null;
			
			TierImpl wTier = getTier(WORDS, suffix);
			if (wTier == null) {
				wTier = createTier(phraseTier, transcription.getLinguisticTypeByName(WORDS), 
						WORDS, suffix);
				transcription.addTier(wTier);
				if (record.speaker != null) {
					wTier.setParticipant(record.speaker);
				}
			}
			
			for (int i = 0; i < record.wordRecords.size(); i++) {
				wrecord = record.wordRecords.get(i);

				if (wTier != null) {
					wa = createWordDepAnnotationSS(wTier, suffix, wrecord, rootAnn, prevwa);
					if (wa != null) {
						prevwa = wa;
					}
				}
			}
		}
	}
	
	/**
	 * Returns the tier with the name and suffix or null if it not exists.
	 * 
	 * @param name base name
	 * @param suffix the speaker suffix
	 * @return the tier
	 */
	private TierImpl getTier(String name, String suffix) {
		TierImpl t = null;
		
		if (suffix != null) {
			t = (TierImpl) transcription.getTierWithId(name + suffix);
		} else {
			t = (TierImpl) transcription.getTierWithId(name);
		}
		
		return t;
	}
	
	/**
	 * 
	 * @param parent parent tier
	 * @param type the ling. type for the tier
	 * @param name the base name
	 * @param suffix the speaker suffix
	 * @return
	 */
	private TierImpl createTier(TierImpl parent, LinguisticType type, String name, String suffix) {
		TierImpl t = null;
		
		if (suffix != null) {
			t = new TierImpl(parent, name + suffix, null, transcription, type);
		} else {
			t = new TierImpl(parent, name, null, transcription, type);
		}
		
		return t;
	}
	
	/**
	 * Create a symbolic association depending annotation.
	 * @param tier
	 * @param value
	 * @param parent
	 * @return the new annotation or null
	 */
	private AbstractAnnotation createDepAnnotationSA (TierImpl tier, String value, 
			AbstractAnnotation parent) {
		long mid = (parent.getBeginTimeBoundary() + parent.getEndTimeBoundary()) / 2;
		AbstractAnnotation aa = (AbstractAnnotation) tier.createAnnotation(mid, mid);
		if (aa != null) {
			aa.setValue(value);
		}
		return aa;
	}
	
	/**
	 * 
	 * @param wTier
	 * @param the speaker suffix
	 * @param wRecord
	 * @param parent
	 * @param previous
	 * @return
	 */
	private AbstractAnnotation createWordDepAnnotationSS(TierImpl wTier, String suffix,
			WordRecord wRecord, AbstractAnnotation parent, AbstractAnnotation previous) {
		AbstractAnnotation aa = null;
		if (previous != null) {
			aa = (AbstractAnnotation) wTier.createAnnotationAfter(previous);
		} else {
			long mid = (parent.getBeginTimeBoundary() + parent.getEndTimeBoundary()) / 2;
			aa = (AbstractAnnotation) wTier.createAnnotation(mid, mid);
		}
		if (aa != null) {
			aa.setValue(wRecord.text);

			// process depending layers
			if (wRecord.morphs != null && wRecord.morphs.size() > 0) {
				MorphRecord mRecord = null;
				TierImpl mTier = getTier(MORPH, suffix);

				if (mTier == null) {
					mTier = createTier(wTier, transcription.getLinguisticTypeByName(MORPH), 
							MORPH, suffix);
					transcription.addTier(mTier);
					mTier.setParticipant(wTier.getParticipant());
					mTier.setLangRef(wTier.getLangRef());
				}
				AbstractAnnotation ma = null;
				AbstractAnnotation prevma = null;
				
				for (int i = 0; i < wRecord.morphs.size(); i++) {
					mRecord = wRecord.morphs.get(i);
					ma = createMorphDepAnnotationSS(mTier, suffix, mRecord, aa, prevma);
					if (ma != null) {
						prevma = ma;
					} else {
						break;// stop this sequence?
					}
				}
			}
			// pos tier
			if (wRecord.pos != null) {
				TierImpl pTier = getTier(POS, suffix);
				
				if (pTier == null) {
					pTier = createTier(wTier, transcription.getLinguisticTypeByName(POS), 
							POS, suffix);
					transcription.addTier(pTier);
					pTier.setParticipant(wTier.getParticipant());
				}
				createDepAnnotationSA(pTier, wRecord.pos, aa);
			}
			
			if(wRecord.head != null) {
				TierImpl hTier = getTier(HEAD, suffix);
				
				if (hTier == null) {
					hTier = createTier(wTier, transcription.getLinguisticTypeByName(HEAD), 
							HEAD, suffix);
					transcription.addTier(hTier);
					hTier.setParticipant(wTier.getParticipant());
				}
				createDepAnnotationSA(hTier, wRecord.head, aa);
			}
		}
		
		return aa;
	}
	
	/**
	 * Creates annotation on the morph tier plus depending annotations.
	 * @param mTier morph tier
	 * @param suffix suffix
	 * @param mRecord the morph record
	 * @param parent parent annotation
	 * @param previous the previous morph with the same parent
	 * @return the created annotation
	 */
	private AbstractAnnotation createMorphDepAnnotationSS(TierImpl mTier, String suffix, 
			MorphRecord mRecord,AbstractAnnotation parent, AbstractAnnotation previous) {
		AbstractAnnotation aa = null;
		if (previous != null) {
			aa = (AbstractAnnotation) mTier.createAnnotationAfter(previous);
		} else {
			long mid = (parent.getBeginTimeBoundary() + parent.getEndTimeBoundary()) / 2;
			aa = (AbstractAnnotation) mTier.createAnnotation(mid, mid);	
		}
	
		if (aa != null) {
			aa.setValue(mRecord.text);
			// add depending annotations
			if (mRecord.baseform != null){
				TierImpl bTier = getTier(BASE, suffix);
				
				if (bTier == null) {
					bTier = createTier(mTier, transcription.getLinguisticTypeByName(BASE), 
							BASE, suffix);
					transcription.addTier(bTier);
					bTier.setParticipant(mTier.getParticipant());
					bTier.setLangRef(mTier.getLangRef());
				}
				// don't a reference to the created annotation
				createDepAnnotationSA(bTier, mRecord.baseform, aa);
			}
			if (mRecord.meaning != null) {
				TierImpl nTier = getTier(MEANING, suffix);
				
				if (nTier == null) {
					nTier = createTier(mTier, transcription.getLinguisticTypeByName(MEANING), 
							MEANING, suffix);
					transcription.addTier(nTier);
					nTier.setParticipant(mTier.getParticipant());
					nTier.setLangRef(mTier.getLangRef());
				}
				
				createDepAnnotationSA(nTier, mRecord.meaning, aa);
			}
			if (mRecord.glosses != null && mRecord.glosses.size() > 0) {
				TierImpl gTier = getTier(GLOSS, suffix);
				
				if (gTier == null) {
					gTier = createTier(mTier, transcription.getLinguisticTypeByName(GLOSS),
							GLOSS, suffix);
					transcription.addTier(gTier);
					gTier.setParticipant(mTier.getParticipant());
					gTier.setLangRef(mTier.getLangRef());
				}
				// create one string for now
				String gl = "";
				for (String s : mRecord.glosses) {
					if (gl.length() > 0) {
						gl = gl + TCtoTranscription.GLOSS_DELIMITER + s;
					} else {
						gl = s;
					}					
				}
				createDepAnnotationSA(gTier, gl, aa);
			}
		}

		return aa;
	}
}
