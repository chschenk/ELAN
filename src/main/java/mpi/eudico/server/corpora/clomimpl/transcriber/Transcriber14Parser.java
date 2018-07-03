package mpi.eudico.server.corpora.clomimpl.transcriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.CVEntryRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.CVRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.Pair;

// import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Parser for Transcriber 1.4 compliant XML files. MAYBE THIS
 * CLASS MUST BE MADE THREAD SAFE BY ADDING SOME SYNCHRONIZED BLOCKS OR BY
 * GIVING UP THE SINGLETON PATTERN.
 * 
 * @version sep 2005 the constructor is now public giving up the singleton pattern.
 * The path parameter of all getter methods could be removed in the next parser version
 * (add a public parse(String path) method)
 * Hashtable and Vector in Parser have been replaced by HashMap and ArrayList 
 * 
 * @author Hennie Brugman
 */
public class Transcriber14Parser extends Parser {
    
    private static boolean verbose = false;
    //private static Transcriber14Parser parser;
    
    private static int annotationCounter = 0;
    private static int timeSlotCounter = 0;
    private static final String ANN_PREFIX = "a";
    private static final String TS_PREFIX = "ts";
    private static final String SP_PREFIX = "Sp";
    
    private static final String SECTION_TIER_NAME = "Sections";
    private static final String TURN_TIER_NAME = "Turns";
	private static final String BACKGROUND_TIER_NAME = "Background";
	private static final String COMMENT_TIER_NAME = "Comments";
	private static final String MODE_TIER_NAME = "Mode";
	private static final String FIDELITY_TIER_NAME = "Fidelity";
	private static final String CHANNEL_TIER_NAME = "Channel";
	private static final String SINGLE_TIER_NAME = "Speech";
    
    private static final String UTTERANCE_TYPE = "UtteranceType";
	private static final String SECTION_TYPE = "SectionType";
	private static final String TURN_TYPE = "TurnType";
	private static final String MODE_TYPE = "ModeType";
	private static final String FIDELITY_TYPE = "FidelityType";
	private static final String CHANNEL_TYPE = "ChannelType";
	private static final String BACKGROUND_TYPE = "BackgroundType";
	private static final String COMMENT_TYPE = "CommentType";
	
	private static final String SECTION_TYPE_CV = "SectionTypeCV";
	private static final String TURN_MODE_CV = "TurnModeCV";
	private static final String TURN_FIDELITY_CV = "TurnFidelityCV";
	private static final String TURN_CHANNEL_CV = "TurnChannelCV";
	
	private static final String SPEAKER_UNSPECIFIED = "Unspecified";

    /** Holds value of property DOCUMENT ME! */
    private SAXParser saxParser;
	private DefaultHandler transcriberHandler;
	private TranscriberDecoderInfo decoderInfo;
	private boolean singleSpeechTier = false;

    private String lastParsed = "";
    private String currentFileName;
    private boolean parseError;
    
    // members to store parse results
	private String audioFileName;
	private String scribe;
	private String language;
	private String date;
	
	private Map<String, String> speakersHash = new HashMap<String, String>();
	private List<SectionRecord> sectionArrayList = new ArrayList<SectionRecord>();
	private List<UtteranceRecord> utteranceRecords = new ArrayList<UtteranceRecord>();
	private List<BackgroundRecord> backgroundArrayList = new ArrayList<BackgroundRecord>();
	private Map<String, Long> timeSlots = new HashMap<String, Long>();
	private Map<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();
	private TreeSet<String> tierNameSet = new TreeSet<String>();
	private Map<String, String> topicHash = new HashMap<String, String>();
	private List<CommentRecord> commentRecords = new ArrayList<CommentRecord>();
	private Map<String, CVRecord> controlledVocabularies = null;

	// members to remember current parse state
	private SectionRecord currentSectionRecord;
	private SectionRecord lastSectionRecord;
	private TurnRecord currentTurnRecord;
	private TurnRecord lastTurnRecord;
	private String currentSpeakerId;
	private String speakersForCurrentTurn;
	private String lastSyncTime;
	private Map<String, String> currentSpeakerContents = new HashMap<String, String>();	// contains utterances under construction
	private BackgroundRecord lastBackgroundRecord;
	private String currentComments = "";
	
	
    /**
     * Private constructor for Transcriber14Parser because the Singleton pattern is
     * applied here.
     */
    public Transcriber14Parser() {
  		SAXParserFactory factory = SAXParserFactory.newInstance();
  		factory.setValidating(false);
  		factory.setNamespaceAware(false);
  		
  		try {
  			saxParser = factory.newSAXParser();
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		
		transcriberHandler = new Transcriber14Handler();
    }

    /**
     * The instance method returns the single incarnation of TranscriberParser to the
     * caller.
     *
     * @return DOCUMENT ME!
     */
    /*
    public static Transcriber14Parser Instance() {
        if (parser == null) {
            parser = new Transcriber14Parser();
        }

        return parser;
    }
	*/
	
    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getMediaFile(String fileName) {
        parse(fileName);

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<MediaDescriptor> getMediaDescriptors(String fileName) {
        parse(fileName);
        
        List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
        
        if (audioFileName != null) {
        	// compose audio file url
        	String urlString = pathToURLString(fileName);
        	String mediaURL = urlString.substring(0, urlString.lastIndexOf("/") + 1) + audioFileName;
        	if (!mediaURL.endsWith(".wav")) {
        		mediaURL += ".wav";
        	}
        	
        	MediaDescriptor md = new MediaDescriptor(mediaURL, MediaDescriptor.WAV_MIME_TYPE);
        	mediaDescriptors.add(md);
        }

        return mediaDescriptors;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getAuthor(String fileName) {
        parse(fileName);

        return scribe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<LingTypeRecord> getLinguisticTypes(String fileName) {
    	List<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>();
    	
        parse(fileName);
        
        // Transcriber uses a number of fixed linguistic types.
        LingTypeRecord lt = new LingTypeRecord();		
		lt.setLingTypeId(UTTERANCE_TYPE);
        lt.setTimeAlignable("true");
        lt.setStereoType(null);
        lt.setControlledVocabulary(null);
        
        lingTypeRecords.add(lt);
        
		lt = new LingTypeRecord();		
		lt.setLingTypeId(SECTION_TYPE);
		lt.setTimeAlignable("true");
		lt.setStereoType(null);
		lt.setControlledVocabulary(SECTION_TYPE_CV);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(TURN_TYPE);
		lt.setTimeAlignable("true");
		lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(MODE_TYPE);
		lt.setTimeAlignable("false");
		lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(FIDELITY_TYPE);
		lt.setTimeAlignable("false");
		lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(CHANNEL_TYPE);
		lt.setTimeAlignable("false");
		lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(BACKGROUND_TYPE);
		lt.setTimeAlignable("true");
		lt.setStereoType(null);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

		lt = new LingTypeRecord();		
		lt.setLingTypeId(COMMENT_TYPE);
		lt.setTimeAlignable("true");
		lt.setStereoType(null);
		lt.setControlledVocabulary(null);
		
		lingTypeRecords.add(lt);

        return lingTypeRecords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<String> getTimeOrder(String fileName) {
        parse(fileName);
        
        List<String> timeOrder = new ArrayList<String>();
        
		List<Pair<String, Long>> tempOrder = new ArrayList<Pair<String, Long>>();
		
		Iterator<String> it = timeSlots.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			Long time = timeSlots.get(id);

			Pair<String, Long> pair = Pair.makePair(id, time);
			
			tempOrder.add(pair);
		}
		
		Collections.sort(tempOrder, new PairComparator());
	
		for (int i = 0; i < tempOrder.size(); i++) {
			timeOrder.add(tempOrder.get(i).getFirst());
		}
		
        return timeOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Map<String, String> getTimeSlots(String fileName) {
        parse(fileName);
        
        Map<String, String> resultSlots = new HashMap<String, String>();
        
        for (Map.Entry<String, Long> e : timeSlots.entrySet()) {
        	String id = e.getKey();
        	String timeString = e.getValue().toString();
        	
        	resultSlots.put(id, timeString);
        }

        return resultSlots;
    }
    
	/**
	 * Returns a Map of CVRecord with the cv id's as keys.<br>
	 *
	 * @param fileName the eaf filename
	 *
	 * @return Map<String, CVRecord>
	 */
    @Override
	public Map<String, CVRecord> getControlledVocabularies(String fileName) {
    	parse(fileName);
    	
    	if (controlledVocabularies == null) {	// fixed cv's, only has to be done once
    		//ArrayList cvArrayList = new ArrayList();
    		// HS Jan 2011 adaptation to changes in ACM27TranscriptionStore
    		CVEntryRecord cvEntry = null;
    		
    		controlledVocabularies = new HashMap<String, CVRecord>();
    		
    		// Section - type cv
    		CVRecord cv = new CVRecord(SECTION_TYPE_CV);
    		cv.setDescription("All values that are allowed for Transcriber Section.type attribute");
    		
    		cvEntry = new CVEntryRecord();
    		cvEntry.setValue("report");
    		cv.addEntry(cvEntry);
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("nontrans");
			cv.addEntry(cvEntry);

			cvEntry = new CVEntryRecord();
			cvEntry.setValue("filler");
			cv.addEntry(cvEntry);
			
    		controlledVocabularies.put(SECTION_TYPE_CV, cv);
    		
    		// Turn - mode cv
    		cv = new CVRecord(TURN_MODE_CV);
    		cv.setDescription("All values that are allowed for Transcriber Turn.mode attribute");
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("spontaneous");
			cv.addEntry(cvEntry);
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("planned");
			cv.addEntry(cvEntry);

			controlledVocabularies.put(TURN_MODE_CV, cv);
   		
    		// Turn - fidelity cv
			cv = new CVRecord(TURN_FIDELITY_CV);
			cv.setDescription("All values that are allowed for Transcriber Turn.fidelity attribute");
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("high");
			cv.addEntry(cvEntry);
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("medium");
			cv.addEntry(cvEntry);

			cvEntry = new CVEntryRecord();
			cvEntry.setValue("low");
			cv.addEntry(cvEntry);

			controlledVocabularies.put(TURN_FIDELITY_CV, cv);
   		
    		// Turn - channel cv
			cv = new CVRecord(TURN_CHANNEL_CV);
			cv.setDescription("All values that are allowed for Transcriber Turn.channel attribute");
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("telephone");
			cv.addEntry(cvEntry);
    		
			cvEntry = new CVEntryRecord();
			cvEntry.setValue("studio");
			cv.addEntry(cvEntry);

			controlledVocabularies.put(TURN_CHANNEL_CV, cv);
    	}
    	
    	return controlledVocabularies;
    }

    /**
     * Returns the names of the Tiers that are present in the Transcription
     * file
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<String> getTierNames(String fileName) {
        parse(fileName);
        
		Set<String> tierNames = new HashSet<String>(annotRecordToTierMap.values());
        return new ArrayList<String>(tierNames);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        String part = "";
        
        if (!tierName.equals(SECTION_TIER_NAME) && !tierName.equals(TURN_TIER_NAME)) {
        	part = tierName;
        }

        return part;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {
        parse(fileName);

        String lType = UTTERANCE_TYPE; // name of type
        
        if (tierName.equals(SECTION_TIER_NAME)) {
        	lType = SECTION_TYPE;
        }
        else if (tierName.equals(TURN_TIER_NAME)) {
        	lType = TURN_TYPE;
        }
        else if (tierName.equals(MODE_TIER_NAME)) {
        	lType = MODE_TYPE;
        }
		else if (tierName.equals(FIDELITY_TIER_NAME)) {
			lType = FIDELITY_TYPE;
		}
		else if (tierName.equals(CHANNEL_TIER_NAME)) {
			lType = CHANNEL_TYPE;
		}
		else if (tierName.equals(BACKGROUND_TIER_NAME)) {
			lType = BACKGROUND_TYPE;
		}
		else if (tierName.equals(COMMENT_TIER_NAME)) {
			lType = COMMENT_TYPE;
		}

        return lType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);
        
        if (tierName.equals(TURN_TIER_NAME)) {
        	return SECTION_TIER_NAME;
        }
        if (tierName.equals(MODE_TIER_NAME)) {
        	return TURN_TIER_NAME;
        }
		if (tierName.equals(FIDELITY_TIER_NAME)) {
			return TURN_TIER_NAME;
		}
		if (tierName.equals(CHANNEL_TIER_NAME)) {
			return TURN_TIER_NAME;
		}
        else {
        	return "";
        }
    }

    /**
     * Returns a List with the Annotations for this Tier. Each
     * AnnotationRecord contains begin time, end time and text values
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return List of AnnotationRecord
     */
    @Override
	public List<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
        parse(fileName);

		List<AnnotationRecord> resultAnnotRecords = new ArrayList<AnnotationRecord>();
		
		Iterator<AnnotationRecord> it = annotRecordToTierMap.keySet().iterator();
		while (it.hasNext()) {
			AnnotationRecord annRec = it.next();
			if (annotRecordToTierMap.get(annRec).equals(tierName)) {
				resultAnnotRecords.add(annRec);
			}
		}
		
		return resultAnnotRecords;
    }

    /**
     * Parses a Transcriber v1.4 xml file.
     *
     * @param fileName the Transcriber 1.4 xml file that must be parsed.
     */
    private void parse(String fileName) {
        long start = System.currentTimeMillis();

        //		System.out.println("Parse : " + fileName);
        //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
        // only parse the same file once
        if (lastParsed.equals(fileName)) {
            return;
        }

        // (re)set everything to null for each parse
        speakersHash.clear();
        sectionArrayList.clear();
		currentSpeakerContents.clear();
		currentSpeakerContents.put(SINGLE_TIER_NAME, "");
		utteranceRecords.clear();
		timeSlots.clear();
		annotRecordToTierMap.clear();
		commentRecords.clear();
		
		lastBackgroundRecord = null;		
		speakersForCurrentTurn = "";
		annotationCounter = 0;
		timeSlotCounter = 0;
		currentSectionRecord = null;
		currentSpeakerId = null;
		currentTurnRecord = null;
		lastSectionRecord = null;
		lastSyncTime = null;
		lastTurnRecord = null;
		currentComments = "";
		tierNameSet.clear();
		topicHash.clear();
        
		if (singleSpeechTier) {
		    currentSpeakerId = SINGLE_TIER_NAME;
		    speakersHash.put(SINGLE_TIER_NAME, SINGLE_TIER_NAME);
		}
        // parse the file
        lastParsed = fileName;
        currentFileName = fileName;

        try {
            saxParser.parse(fileName, transcriberHandler);
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
			// the SAX parser can have difficulties with certain characters in 
			// the filepath: try to create an InputSource for the parser 
            File f = new File(fileName);
            if (f.exists()) {
            	FileInputStream fis = null;
				try {
					fis = new FileInputStream(f);
					InputSource source = new InputSource(fis);
					saxParser.parse(source, transcriberHandler);
					// just catch any exception
				} catch (Exception ee) {
					System.out.println("Parsing retry error: " + ee.getMessage());
		        } finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException ioe) {
					}
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            printErrorLocationInfo("Fatal(?) Error! " + e.getMessage());
        }
        
        // go through Sections, Turns, Utterances and Backgrounds and compose
        // TimeOrder, time slots and annotation records.
        processSectionsAndTurns();
        processUtterances();
        processBackgrounds();
        processComments();

        //  long duration = System.currentTimeMillis() - start;
        //	System.out.println("Parsing took " + duration + " milli seconds");
    }

	private void processSectionsAndTurns() {
		tierNameSet.add(SECTION_TIER_NAME);
		tierNameSet.add(TURN_TIER_NAME);
		
		for (int i = 0; i < sectionArrayList.size(); i++) {
			SectionRecord sectionR = sectionArrayList.get(i);
			
			long sectionBegin = new Double((new Double(sectionR.startTime)).doubleValue() * 1000).longValue();
			long sectionEnd = new Double((new Double(sectionR.endTime)).doubleValue() * 1000).longValue();

			String sectionBeginSlotId = TS_PREFIX + timeSlotCounter++;
			String sectionEndSlotId = TS_PREFIX + timeSlotCounter++;
			
			String value = sectionR.type;
			if (sectionR.topicId != null && !sectionR.topicId.equals("")) {
				value += " - " + topicHash.get(sectionR.topicId);
			}
			
			addAnnotRecordAndTimeSlots(sectionBegin, sectionEnd, sectionBeginSlotId, sectionEndSlotId, 
							value, SECTION_TIER_NAME);										
						
			// turns
			String turnBeginSlotId = "";
			String turnEndSlotId = "";
			for (int j = 0; j < sectionR.turnRecords.size(); j++) {
				TurnRecord turnR = sectionR.turnRecords.get(j);
			
				long turnBegin = new Double((new Double(turnR.startTime)).doubleValue() * 1000).longValue();
				long turnEnd = new Double((new Double(turnR.endTime)).doubleValue() * 1000).longValue();
							
				if (turnBegin == sectionBegin) {
					turnBeginSlotId = sectionBeginSlotId;
				}
				else if (!turnEndSlotId.equals("")){
					turnBeginSlotId = turnEndSlotId;
				}
				else {
					turnBeginSlotId = TS_PREFIX + timeSlotCounter++;
				}
				
				if (turnEnd == sectionEnd) {
					turnEndSlotId = sectionEndSlotId;
				}
				else {
					turnEndSlotId = TS_PREFIX + timeSlotCounter++;
				}

				String speakerString = "";
				if (turnR.speakers != null) {
					StringTokenizer tokenizer = new StringTokenizer(turnR.speakers);
					while (tokenizer.hasMoreTokens()) {
						speakerString += speakersHash.get(tokenizer.nextToken());
						if (tokenizer.hasMoreTokens()) {
							speakerString += " + ";	
						}
					}
				}
				else {
					speakerString = "(no speaker)";
				}

				String parentId = addAnnotRecordAndTimeSlots(turnBegin, turnEnd, turnBeginSlotId, turnEndSlotId, 
								speakerString, TURN_TIER_NAME);	
								
				if (turnR.mode != null && !turnR.mode.equals("")) {
					tierNameSet.add(MODE_TIER_NAME);
					addAnnotRecord(parentId, turnR.mode, MODE_TIER_NAME);										
				}
				if (turnR.fidelity != null && !turnR.fidelity.equals("")) {
					tierNameSet.add(FIDELITY_TIER_NAME);
					addAnnotRecord(parentId, turnR.fidelity, FIDELITY_TIER_NAME);										
				}
				if (turnR.channel != null && !turnR.channel.equals("")) {
					tierNameSet.add(CHANNEL_TIER_NAME);
					addAnnotRecord(parentId, turnR.channel, CHANNEL_TIER_NAME);										
				}
			}			
		}
	}
	
	private void processUtterances() {
		// add tier names to tierNameSet
		Iterator<String> speakerNameIter = speakersHash.values().iterator();
		while (speakerNameIter.hasNext()) {
			tierNameSet.add(speakerNameIter.next());
		}
		
		// iterate over utteranceRecords
		for (int i = 0; i < utteranceRecords.size(); i++) {
			UtteranceRecord utteranceR = utteranceRecords.get(i);

			long uttBegin = new Double((new Double(utteranceR.startTime)).doubleValue() * 1000).longValue();
			long uttEnd = new Double((new Double(utteranceR.endTime)).doubleValue() * 1000).longValue();

			String beginSlotId = TS_PREFIX + timeSlotCounter++;
			String endSlotId = TS_PREFIX + timeSlotCounter++;
			
			addAnnotRecordAndTimeSlots(uttBegin, uttEnd, beginSlotId, endSlotId, 
							utteranceR.text, utteranceR.speaker);
		}
	}
	
	private void processBackgrounds() {
		tierNameSet.add(BACKGROUND_TIER_NAME);
		
		long bgBeginTime = 0;
		long bgEndTime = 0;
		
		for (int i = 0; i < backgroundArrayList.size(); i++) {
			BackgroundRecord backgroundR = backgroundArrayList.get(i);
			
			bgEndTime = new Double((new Double(backgroundR.time)).doubleValue() * 1000).longValue();

			if (lastBackgroundRecord != null && !lastBackgroundRecord.level.equals("off")) {	// not first background
				bgBeginTime = new Double((new Double(lastBackgroundRecord.time)).doubleValue() * 1000).longValue(); 

				String beginSlotId = TS_PREFIX + timeSlotCounter++;
				String endSlotId = TS_PREFIX + timeSlotCounter++;
				
				addAnnotRecordAndTimeSlots(bgBeginTime, bgEndTime, beginSlotId, endSlotId,
							lastBackgroundRecord.type, BACKGROUND_TIER_NAME);
			}
			
			lastBackgroundRecord = backgroundR;						
		}	
		if (lastBackgroundRecord != null && !lastBackgroundRecord.level.equals("off") && sectionArrayList.size() > 0) {		// handle last background record, end at last section end
			SectionRecord sectionR = sectionArrayList.get(sectionArrayList.size() - 1);
			if (sectionR != null) {
				bgBeginTime = new Double((new Double(lastBackgroundRecord.time)).doubleValue() * 1000).longValue(); 
				bgEndTime = new Double((new Double(sectionR.endTime)).doubleValue() * 1000).longValue();

				String beginSlotId = TS_PREFIX + timeSlotCounter++;
				String endSlotId = TS_PREFIX + timeSlotCounter++;
				
				addAnnotRecordAndTimeSlots(bgBeginTime, bgEndTime, beginSlotId, endSlotId, 
							lastBackgroundRecord.type, BACKGROUND_TIER_NAME);				
			}
		}	
	}
	
	private void processComments() {
		tierNameSet.add(COMMENT_TIER_NAME);
		
		// iterate over commentRecords
		for (int i = 0; i < commentRecords.size(); i++) {
			CommentRecord commentR = commentRecords.get(i);

			long commBegin = new Double((new Double(commentR.begin)).doubleValue() * 1000).longValue();
			long commEnd = new Double((new Double(commentR.end)).doubleValue() * 1000).longValue();

			String beginSlotId = TS_PREFIX + timeSlotCounter++;
			String endSlotId = TS_PREFIX + timeSlotCounter++;
			
			addAnnotRecordAndTimeSlots(commBegin, commEnd, beginSlotId, endSlotId, 
							commentR.desc, COMMENT_TIER_NAME);
		}		
	}
	
	private String addAnnotRecordAndTimeSlots(long begin, long end, 
							String beginSlotId, String endSlotId, 
							String value, String tierName) {
			
		timeSlots.put(beginSlotId, new Long(begin));
		timeSlots.put(endSlotId, new Long(end));
		
		String annId = ANN_PREFIX + annotationCounter++;
			
		AnnotationRecord annRec = new AnnotationRecord();
		annRec.setAnnotationId(annId);
		annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
		annRec.setBeginTimeSlotId(beginSlotId);
		annRec.setEndTimeSlotId(endSlotId);
		annRec.setValue(value);
			
		annotRecordToTierMap.put(annRec, tierName);	
		
		return annId;									
	}
	
	private void addAnnotRecord(String parentId, String value, String tierName) {
		AnnotationRecord annRec = new AnnotationRecord();
		annRec.setAnnotationId(ANN_PREFIX + annotationCounter++);
		annRec.setAnnotationType(AnnotationRecord.REFERENCE);		
		annRec.setReferredAnnotId(parentId);
		annRec.setValue(value);
			
		annotRecordToTierMap.put(annRec, tierName);			
	}

    private void println(String s) {
        if (verbose) {
            System.out.println(s);
        }
    }

    private void printErrorLocationInfo(String message) {
        System.out.println(message);
        System.out.println("Exception for " + currentFileName);
    }
    
	/*
	 * This method should be in a Utility class or a URL class
	 * Convert a path to a file URL string. Takes care of Samba related problems
	 * file:///path works for all files except for samba file systems, there we need file://machine/path,
	 * i.e. 2 slashes insteda of 3
	 *
	 * What's with relative paths?
	 */
	private String pathToURLString(String path) {
		// replace all back slashes by forward slashes
		path = path.replace('\\', '/');

		// remove leading slashes and count them
		int n = 0;

		while (path.charAt(0) == '/') {
			path = path.substring(1);
			n++;
		}

		// add the file:// or file:/// prefix
		if (n == 2) {
			return "file://" + path;
		} else {
			return "file:///" + path;
		}
	}

    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#setDecoderInfo(mpi.eudico.server.corpora.clom.DecoderInfo)
     */
    @Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
        if (decoderInfo instanceof TranscriberDecoderInfo) {
            this.decoderInfo = (TranscriberDecoderInfo) decoderInfo;
            singleSpeechTier = this.decoderInfo.isSingleSpeakerTier();
        }
    }
    
    /**
     * DOCUMENT ME!
     */
    class Transcriber14Handler extends DefaultHandler {

        /**
         * ContentHandler method
         *
         * @throws SAXException DOCUMENT ME!
         */
        @Override
		public void startDocument() throws SAXException {
            parseError = false;
        }

        /**
         * ContentHandler method
         *
         * @throws SAXException DOCUMENT ME!
         */
        @Override
		public void endDocument() throws SAXException {
        }

        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         * @param attributes DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        @Override
		public void startElement(String nameSpaceURI, String name,
            String qName, Attributes attributes) throws SAXException {
            //	System.out.println("startElement called for name:" + name);
            
            if (qName.equals("Trans")) {
                audioFileName = attributes.getValue("audio_filename");
                scribe = attributes.getValue("scribe");
                language = attributes.getValue("xml:lang");
                date = attributes.getValue("version_date");
            }
            else if (qName.equals("Speaker")) {
            	speakersHash.put(
            		attributes.getValue("id"), 
            		attributes.getValue("name"));
            		
            	if (!singleSpeechTier) {
            	    currentSpeakerContents.put(attributes.getValue("id"), "");
            	}
				
            }
            else if (qName.equals("Section")) {
            	currentSectionRecord = new SectionRecord(
					attributes.getValue("type"), 
					attributes.getValue("startTime"), 
					attributes.getValue("endTime"),
					attributes.getValue("topic"));
            	sectionArrayList.add(currentSectionRecord);	
            }
            else if (qName.equals("Turn")) {
            	currentTurnRecord = new TurnRecord(
					attributes.getValue("startTime"), 
					attributes.getValue("endTime"),
					attributes.getValue("speaker"), 
					attributes.getValue("mode"), 
					attributes.getValue("fidelity"), 
					attributes.getValue("channel"));
					
				if (currentSectionRecord != null) {
					currentSectionRecord.turnRecords.add(currentTurnRecord);
				}
				
				speakersForCurrentTurn = attributes.getValue("speaker");
				
				if (!singleSpeechTier) {
					if (speakersForCurrentTurn != null) {
						StringTokenizer tokenizer = new StringTokenizer(speakersForCurrentTurn);
						if (tokenizer.hasMoreTokens()) {
							currentSpeakerId = tokenizer.nextToken();	
						}
						else {
							currentSpeakerId = "";	
						}
					}
					else {
						currentSpeakerId = SPEAKER_UNSPECIFIED;
						speakersHash.put(SPEAKER_UNSPECIFIED, SPEAKER_UNSPECIFIED);
	            	
						currentSpeakerContents.put(SPEAKER_UNSPECIFIED, "");
					}
				}
            }
            else if (qName.equals("Sync")) {
            	String time = attributes.getValue("time");
            	storeUtterances(time);
            	storeComments(time);
            	
            	// store
				lastSyncTime = time;
            }
            else if (qName.equals("Event")) {
            	String desc = attributes.getValue("desc");
            	String extent = attributes.getValue("extent");
            	
            	String eventString = TranscriberEvent.getEventString(desc, extent);
            	
            	if (currentSpeakerContents != null && currentSpeakerId != null) {
					String content = currentSpeakerContents.get(currentSpeakerId);
	        	
					if (content != null) {            	
						content += eventString;
					}  
				 	
					currentSpeakerContents.put(currentSpeakerId, content);
            	}          	
            }
            else if (qName.equals("Who")) {
                String nb = attributes.getValue("nb");
                
                if (singleSpeechTier) {
                	if (currentSpeakerContents != null && currentSpeakerId != null) {
    					String content = currentSpeakerContents.get(currentSpeakerId);
    					String whoStr = "(" + SP_PREFIX + ":" + nb + ")";
    					if (content != null) { 
    					    if (content.length() == 0) {
    					        content = whoStr;
    					    } else {
    					        content += (" " + whoStr);    
    					    }
    					} else {
    					    content = whoStr;
    					}
    				 	
    					currentSpeakerContents.put(currentSpeakerId, content);
                	} 
                } else {               
	            	int num = Integer.parseInt(nb);
	            	int counter = 1;
	            	
					if (speakersForCurrentTurn != null) {
						StringTokenizer tokenizer = new StringTokenizer(speakersForCurrentTurn);
						while (tokenizer.hasMoreTokens()) {
							String spkr = tokenizer.nextToken();
							if (counter++ == num) {
								currentSpeakerId = spkr;
								break;
							}
						}
					}
	            }
            }
            else if (qName.equals("Background")) {
				backgroundArrayList.add(new BackgroundRecord(
					attributes.getValue("time"), 
					attributes.getValue("type"), 
					attributes.getValue("level")));	           	
            }
            else if (qName.equals("Topic")) {
				topicHash.put(
					attributes.getValue("id"), 
					attributes.getValue("desc"));            		
            }
            else if (qName.equals("Comment")) {
            	if (!currentComments.equals("")) {
            		currentComments += " ";
            	}
            	currentComments += "{" + attributes.getValue("desc") + "}";	
            }
        }
        //startElement

		private void storeUtterances(String time) {
			String spkId = "";
			String spkContent = "";
            	
			// store utterance(s) for each speaker in Turn
			Iterator<String> it = currentSpeakerContents.keySet().iterator();
			while (it.hasNext()) {
				 spkId = it.next();
				 spkContent = currentSpeakerContents.get(spkId);
				 if (!spkContent.equals("")) {
					utteranceRecords.add(new UtteranceRecord(
						spkContent, 
						speakersHash.get(spkId),
						lastSyncTime,
						time,
						lastTurnRecord));
				 }	
			}            	
            	
			// reset
			// clean currentSpeakerContents
			Iterator<String> it2 = currentSpeakerContents.keySet().iterator();
			while (it2.hasNext()) {
				 spkId = it2.next();
				 currentSpeakerContents.put(spkId, "");
			}
				            	
			lastSectionRecord = currentSectionRecord;
			lastTurnRecord = currentTurnRecord;			
		}
		
		private void storeComments(String time) {
			if (!currentComments.equals("")) {
				commentRecords.add(new CommentRecord(lastSyncTime, time, currentComments));
				
				currentComments = "";	
			}
		}
		
        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        @Override
		public void endElement(String nameSpaceURI, String name, String qName)
            				throws SAXException {
            	
			if (qName.equals("Section") && currentSectionRecord != null) {	// make sure last utterance is stored
				String time = currentSectionRecord.endTime;
				storeUtterances(time);
				storeComments(time);
				
				// store
				lastSyncTime = time;
			}
        }

        /**
         * ContentHandler method
         *
         * @param ch DOCUMENT ME!
         * @param start DOCUMENT ME!
         * @param end DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        @Override
		public void characters(char[] ch, int start, int end)
            	throws SAXException {
            	
        	if (currentSpeakerContents == null || currentSpeakerId == null) {
        		return;
        	}
        	
        	String content = currentSpeakerContents.get(currentSpeakerId);
        	
        	if (content != null) {            	
        		content += new String(ch, start, end).trim();
        	}
        	
			currentSpeakerContents.put(currentSpeakerId, content);
        }
        
		@Override
		public InputSource resolveEntity (String publicId, String systemId) {
			// do this to prevent an exception when a DTD is not found
			return new InputSource(new StringReader(""));
		}
    }
    
    private class SectionRecord {
    	public String type;
    	public String startTime;
    	public String endTime;
    	public String topicId;
    	public ArrayList<TurnRecord> turnRecords;
    	
    	public SectionRecord(String type, String startTime, String endTime, String topicId) {
    		this.type =type;
    		this.startTime = startTime;
    		this.endTime = endTime;
    		this.topicId = topicId;
    		
    		turnRecords = new ArrayList<TurnRecord>();
    	}
    }
    
    private class TurnRecord {
    	public String startTime;
    	public String endTime;
    	public String mode;
    	public String fidelity;
    	public String channel;
    	public String speakers;
    	
    	public TurnRecord(String startTime, String endTime, String speakers,
    				String mode, String fidelity, String channel) {
    		this.startTime = startTime;
    		this.endTime = endTime;
    		this.speakers = speakers;
    		this.mode = mode;
    		this.fidelity = fidelity;
    		this.channel = channel;			
    	}
    }
    
    private class UtteranceRecord {
    	public String text;
    	public String speaker;
    	public String startTime;
    	public String endTime;
    	public TurnRecord turnRecord;
    	
    	public UtteranceRecord(String text, String speaker, String startTime,
    						String endTime, TurnRecord turnRecord) {
    		this.text = text;
    		this.speaker = speaker;
    		this.startTime = startTime;
    		this.endTime = endTime;
    		this.turnRecord = turnRecord;					
    	}
    }
 
	private class BackgroundRecord {
		public String time;
		public String type;
		public String level;
    	
		public BackgroundRecord(String time, String type, String level) {
			this.time = time;
			this.type = type;
			this.level = level;
		}
	}
 
	private class CommentRecord {
		public String begin;
		public String end;
		public String desc;
    	
		public CommentRecord(String begin, String end, String desc) {
			this.begin = begin;
			this.end = end;
			this.desc = desc;
		}
	}
  
	class PairComparator implements Comparator<Pair<String, Long>> {
	
		/** 
		 * Compares pairs {String tsId, Long time}, first on basis of time, then on 
		 * basis of number part of id
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Pair<String, Long> pair0, Pair<String, Long> pair1) {
			
			long time0 = pair0.getSecond().longValue();
			long time1 = pair1.getSecond().longValue();
			
			if (time0 < time1) {
				return -1;	
			}
			if (time0 > time1) {
				return 1;
			}
			if (time0 == time1) {
				long id0 = Long.parseLong(pair0.getFirst().substring(TS_PREFIX.length()));
				long id1 = Long.parseLong(pair1.getFirst().substring(TS_PREFIX.length()));
				
				if (id0 < id1) {
					return -1;	
				}
				if (id0 > id1) {
					return 1;	
				}
			}
				
			return 0;
		}
	}

}
