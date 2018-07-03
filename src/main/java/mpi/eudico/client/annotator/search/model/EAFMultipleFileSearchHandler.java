package mpi.eudico.client.annotator.search.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.search.result.model.EAFMultipleFileMatch;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.Utilities;
import mpi.search.content.result.model.ContentResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  
 */
class EAFMultipleFileSearchHandler extends DefaultHandler {
    final private static String NULL = "iuhfiahfafb29384hc";

    final private ContentResult result;

    final private Pattern pattern;

    final private Map<String, String> timeSlots;

    final private Map<EAFMultipleFileMatch, String> timeUnsolvedMatches;

    final private Map<String, String> ref1;

    final private Map<String, String> ref2;

    final private List<String> tierNames;

    private boolean doAppend;

    private boolean processAfter;

    private StringBuilder textBuffer;

    private EAFMultipleFileMatch lastMatch;

    private String annotationBefore;

    private String tierName;

    private String timeSlotRef1;

    private String timeSlotRef2;

    private String annotationRef;

    private int indexInTier;

    private File file;
    
    // test for resolving unaligned alignable annotations
    private List<String> unalignedAlignablesIds;
    private List<EAFMultipleFileMatch> unalignedAlignableMatches;
    private String lastAlignedBTS;
	private String id;
    
    
    public EAFMultipleFileSearchHandler(ContentQuery query) {
        this.result = (ContentResult) query.getResult();
        pattern = Utilities.getPattern(query.getAnchorConstraint(), new EAFType());
        tierNames = new ArrayList<String>();
        timeSlots = new HashMap<String, String>();
        ref1 = new HashMap<String, String>();
        ref2 = new HashMap<String, String>();
        timeUnsolvedMatches = new HashMap<EAFMultipleFileMatch, String>();
		unalignedAlignablesIds = new ArrayList<String>();
		unalignedAlignableMatches = new ArrayList<EAFMultipleFileMatch>();
    }

    public void newFile(File file) {
        this.file = file;
        timeSlots.clear();
        ref1.clear();
        ref2.clear();
        timeUnsolvedMatches.clear();
		unalignedAlignablesIds.clear();
		unalignedAlignableMatches.clear();
    }

    public ContentResult getResult() {
        return result;
    }

    public List<String> getTierNames() {
        return tierNames;
    }

    @Override
	public void startElement(String namespaceURI, String sName, String qName,
            Attributes attrs) throws SAXException {
        doAppend = false;

        if (qName.equals("TIER")) {
            tierName = attrs.getValue("TIER_ID");

            // remember all tier names since last reset
            if (!tierNames.contains(tierName)) {
                tierNames.add(tierName);
            }
            annotationBefore = null;
            processAfter = false;
            indexInTier = -1; // let the first index be 0
        }
        else if (qName.equals("ANNOTATION_VALUE")) {
            doAppend = true;
            textBuffer = new StringBuilder();
            indexInTier++;
        }
        else if (qName.equals("ALIGNABLE_ANNOTATION")) {
            id = attrs.getValue("ANNOTATION_ID");
            timeSlotRef1 = attrs.getValue("TIME_SLOT_REF1");
            timeSlotRef2 = attrs.getValue("TIME_SLOT_REF2");
            annotationRef = null;
            ref1.put(id, timeSlotRef1);
            ref2.put(id, timeSlotRef2);

			if (lastAlignedBTS == null &&
				timeSlots.containsKey(timeSlotRef1) && 
				!timeSlots.containsKey(timeSlotRef2)) {
					lastAlignedBTS = timeSlotRef1;
			}
        }
        else if (qName.equals("REF_ANNOTATION")) {
            id = attrs.getValue("ANNOTATION_ID");
            annotationRef = attrs.getValue("ANNOTATION_REF");
            timeSlotRef1 = null;
            timeSlotRef2 = null;
            ref1.put(id, NULL);
            ref2.put(id, annotationRef);
       }
        else if (qName.equals("TIME_SLOT")) {
            id = attrs.getValue("TIME_SLOT_ID");
            String value = attrs.getValue("TIME_VALUE");
            // must be robust, at least one dobes files has error
            if (id != null && value != null) {
                timeSlots.put(id, value);
            }
        }
    }

    @Override
	public void endElement(String namespaceURI, String sName, String qName)
            throws SAXException {
        if (qName.equals("ANNOTATION_VALUE")) {
            // update last hit if needed
            if (processAfter) {
                lastMatch.setRightContext(textBuffer.toString());
                processAfter = false;
            }

            Matcher matcher = pattern.matcher(textBuffer);
            if (matcher.find()) {
                EAFMultipleFileMatch match = new EAFMultipleFileMatch(textBuffer
                        .toString());

                List<int[]> substringIndices = new ArrayList<int[]>();
                do {
                    substringIndices.add(new int[] { matcher.start(0), matcher.end(0) });
                } while (matcher.find());
                match.setMatchedSubstringIndices(substringIndices
                        .toArray(new int[0][0]));

                match.setIndex(indexInTier);
                match.setId(id);
                if (annotationBefore != null)
                    match.setLeftContext(annotationBefore);
                match.setTierName(tierName);
                match.setFileName(file.getAbsolutePath());

                match.setBeginTimeBoundary(-1l);
                match.setEndTimeBoundary(-1l);

                if (annotationRef == null) {
                	if (!timeSlots.containsKey(timeSlotRef1) || 
                		!timeSlots.containsKey(timeSlotRef2)) {
                		unalignedAlignableMatches.add(match);
                	} 
					setTimeForMatch(match, timeSlotRef1, timeSlotRef2);               
                }
                // do something for ref annotations,
                else {
                    String alignedAnnID = annotationRef;
                    while (ref1.get(alignedAnnID) == NULL) {
                        // are there more than 1 refs possible?
                        alignedAnnID = ref2.get(alignedAnnID);
                    }
                    Object timeSlotRef = ref1.get(alignedAnnID);
                    if (timeSlotRef != null) {
                        setTimeForMatch(match, ref1.get(alignedAnnID), ref2
                                .get(alignedAnnID));
                    }
                    // if the annotation refered to comes later then the
                    // refering annotation in .eaf resolve references at the end of the document
                    else {
                        timeUnsolvedMatches.put(match, annotationRef);
                    }
                }

                //result.addMatch(match);

                lastMatch = match;
                processAfter = true;
            }

            annotationBefore = textBuffer.toString();
        } 
        else if (qName.equals("ALIGNABLE_ANNOTATION")) {
        	// only do something if an unaligned annotation was encountered
			if (lastAlignedBTS != null) {
				if (timeSlots.containsKey(timeSlotRef2)) {
					// time to calculate interpolated time values
					// the stored lastAlignedBegin TS and the current timeSlotRef2 are used
					// for the interpolation
					//unalignedAlignablesIds.add(Integer.valueOf(indexInTier));
					unalignedAlignablesIds.add(id);
					calculateUnalignedAlignedMatches();
				} else {
					//unalignedAlignablesIds.add(Integer.valueOf(indexInTier));
					unalignedAlignablesIds.add(id);
				}
			} 
        }
    }

    @Override
	public void endDocument() {
        try {
            for (EAFMultipleFileMatch match : timeUnsolvedMatches.keySet()) {
                String alignedAnnID = timeUnsolvedMatches.get(match);
                while (ref1.get(alignedAnnID) == NULL) {
                    // are there more than 1 refs possible?
                    alignedAnnID = ref2.get(alignedAnnID);
                }
                Object timeSlotRef = ref1.get(alignedAnnID);
                if (timeSlotRef != null) {
                    setTimeForMatch(match, ref1.get(alignedAnnID), ref2.get(alignedAnnID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to resolve the slot references and sets begin and end time in match
     * 
     * @param match
     */
    private void setTimeForMatch(EAFMultipleFileMatch match, Object timeSlotRef1,
            Object timeSlotRef2) {
        if (timeSlotRef1 != null) {
            String value = timeSlots.get(timeSlotRef1);
            if (value != null) {
                match.setBeginTimeBoundary(Long.valueOf(value).longValue());
            }
        }
        if (timeSlotRef2 != null) {
            String value = timeSlots.get(timeSlotRef2);
            if (value != null) {
                match.setEndTimeBoundary(Long.valueOf(value).longValue());
            }
            //if end time is smaller than begin time (e.g. value missing), set it 1 ns after begin time
            if(match.getEndTimeBoundary() < match.getBeginTimeBoundary()){
                //match.setEndTimeBoundary(match.getBeginTimeBoundary()+ 1l);
            }
        }
        result.addMatch(match);
    }

    @Override
	public void characters(char buf[], int offset, int len) throws SAXException {
        if (doAppend) {
            textBuffer.append(buf, offset, len);
        }
    }
    
    /**
     * Calculates interpolated time values for unaligned timeslots used by alignable
     * annotations. If ther are matches using these slots their begin- and / or endtime
     * values will be updated.
     */
    private void calculateUnalignedAlignedMatches() {
    	if (unalignedAlignablesIds.size() != 0) {
			//System.out.println("num una: " + unalignedAlignablesIds.size());
    		//System.out.println("num una match: " + unalignedAlignableMatches.size());
    		long refBT = 0L;
    		long refET = 0L;
    		
    		try {
    			refBT = Long.valueOf(timeSlots.get(lastAlignedBTS)).longValue();
    		} catch (NumberFormatException ex) {   			
    		}
    		
			try {
				refET = Long.valueOf(timeSlots.get(timeSlotRef2)).longValue();
			} catch (NumberFormatException ex) {		
			}
			
			long span = refET - refBT;
			if (span < 0) {
				span = 0;
			}
			
			int step = (int) (span / unalignedAlignablesIds.size());
			// add the calculated time for the 'end' time slot to the table
			for (int i = 0; i < unalignedAlignablesIds.size(); i++) {
				String id = unalignedAlignablesIds.get(i);
				String tsRef2 = ref2.get(id);
				if (!timeSlots.containsKey(tsRef2)) {
					timeSlots.put(tsRef2, String.valueOf(refBT + ((i + 1) * step)));
				}				
			}
			
			// apply to the matches in the current set of unaligned annotations
			for (int i = 0; i < unalignedAlignableMatches.size(); i++) {
				EAFMultipleFileMatch match = unalignedAlignableMatches.get(i);
				String id = match.getId(); 
				
				if (match.getBeginTimeBoundary() == -1) {
					String tsRef1 = ref1.get(id);
					String value = timeSlots.get(tsRef1);
					if (value != null) {
						match.setBeginTimeBoundary(Long.valueOf(value).longValue());
					}
				}
				if (match.getEndTimeBoundary() == -1) {
					String tsRef2 = ref2.get(id);
					String value = timeSlots.get(tsRef2);
					if (value != null) {
						match.setEndTimeBoundary(Long.valueOf(value).longValue());
					}
				}

			}
    	}
    	
		unalignedAlignablesIds.clear();
		unalignedAlignableMatches.clear();
		lastAlignedBTS = null;
    }
}
