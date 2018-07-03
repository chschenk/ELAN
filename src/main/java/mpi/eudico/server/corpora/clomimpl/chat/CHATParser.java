/*
 * Created on Jun 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * @author hennie
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * @version sep 2005 the constructor is now public giving up the singleton pattern.
 * The path parameter of all getter methods could be removed in the next parser version
 * (add a public parse(String path) method)
 * Hashtable and Vector in Parser have been replaced by HashMap and ArrayList 
 */
public class CHATParser extends Parser {

	//private static CHATParser parser;
	
	private final static String MAIN_TYPE = "orthography";
	private final static char TIER_NAME_SEPARATOR = '@';
	private final static String TS_ID_PREFIX = "ts";
	private final char BULLET = '\u0015';
	private final String MEDIA_HEADER = "@Media";
	private final String PARTICIPANTS_HEADER = "@Participants";
	//private final String LANGS_HEADER = "@Languages";
	private final String AT = "@";
	private final String PERC = "%";
	private final String AST = "*";
	private final String COLON = ":";
	private final String SND = "%snd";
	private final String MOV = "%mov";
	
	private String participantLine = null;
	private String mediaFileName = null;
	private ArrayList<ArrayList<String[]>> chatBlocks = new ArrayList<ArrayList<String[]>>();
	private ArrayList<Integer> blocksWithTime = new ArrayList<Integer>();
	
	private ArrayList<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>();
	private ArrayList<String> tierNames = new ArrayList<String>();
	private HashMap<String, String> parentHash = new HashMap<String, String>();
	private ArrayList<long[]> timeOrder = new ArrayList<long[]>();
	private ArrayList<long[]> timeSlots = new ArrayList<long[]>(); // of long[2], {id,time}
	private ArrayList<AnnotationRecord> annotationRecords = new ArrayList<AnnotationRecord>();
	private HashMap<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();
	
	private String lastParsed = "";
	
	private BufferedReader br;

	/**
	 * Private constructor for EAFParser because the Singleton pattern is
	 * applied here.
	 */
	public CHATParser() {
		
	}
 	
	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getMediaDescriptors(java.lang.String)
	 */
	@Override
	public ArrayList<MediaDescriptor> getMediaDescriptors(String fileName) {
		ArrayList<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
		
		parse(fileName);

		String mediaURL = null;
		if (mediaFileName != null) {
			mediaURL = pathToURLString(mediaFileName);
			
			// could call a method in MediaDescriptorUtil but that would create an undesirable dependency 
			// on the eudico.client package
			String mimeType = "unknown";
			String lower = mediaFileName.toLowerCase();
			if (lower.endsWith(".wav")) {
				mimeType = "audio/x-wav";
			} else if (lower.endsWith(".mov")) {
				mimeType = "video/quicktime";
			} else if (lower.endsWith(".mp4")) {
				mimeType = "video/mp4";
			}	
			
			MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);
			mediaDescriptors.add(md);
		} 	
		
		return mediaDescriptors;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypes(java.lang.String)
	 */
	@Override
	public ArrayList<LingTypeRecord> getLinguisticTypes(String fileName) {
		parse(fileName);

		if (lingTypeRecords.size() != 0) {
			return lingTypeRecords;
		}
		
		Set<String> labels = new HashSet<String>();

		Iterator<ArrayList<String[]>> blockIter = chatBlocks.iterator();
		while (blockIter.hasNext()) {
			ArrayList<String[]> block = (ArrayList<String[]>) blockIter.next();

			Iterator<String[]> lineIter = block.iterator();

			while (lineIter.hasNext()) {
				String[] line = (String[]) lineIter.next();
				String lbl = line[0];

				if (!lbl.equals(SND) && 
					!(lbl.length() > 1 && lbl.substring(1).startsWith(PERC))) {
					labels.add(lbl);
				}
			}
		}
		
		// create main "orthography" ling type for participant tiers
		LingTypeRecord orthoType = new LingTypeRecord();
		orthoType.setLingTypeId(MAIN_TYPE);
		orthoType.setTimeAlignable("true");
		
		lingTypeRecords.add(orthoType);
		
		// for each label, create a matching lingtype
		Iterator<String> lblIter = labels.iterator();
		while (lblIter.hasNext()) {
			String label = (String) lblIter.next();
			
			if (!label.startsWith(AST)) {
				LingTypeRecord lt = new LingTypeRecord();
				lt.setLingTypeId(label);
				lt.setTimeAlignable("false");		// all symbolic associations of ortho tier
				lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
			
				lingTypeRecords.add(lt);
			}
		}
				
		return lingTypeRecords;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTimeOrder(java.lang.String)
	 */
	@Override
	public ArrayList<String> getTimeOrder(String fileName) {
		parse(fileName);	
		// compose ordered list of timeslot ids from timeSlots
		
		// algorithm:
		// find first time after t = 0, put slot id in result list
		// find first time after t or equal to time of last result slot id
		// terminate when no time found
		//
		// handling of unaligned slots:
		// put all unaligned slots immediately preceding a result slot 
		// immediately before this slot in the result
		ArrayList<long[]> unalignedSlots = new ArrayList<long[]>();
		
		long[] firstSlotAfter = firstTimeSlotAfter(null, unalignedSlots);
		if (firstSlotAfter == null) {
			timeOrder.addAll(unalignedSlots);
		}
		while (firstSlotAfter != null) {
			timeOrder.addAll(unalignedSlots);
			timeOrder.add(firstSlotAfter);
			
			unalignedSlots.clear();
			firstSlotAfter = firstTimeSlotAfter(
					firstSlotAfter, 
					unalignedSlots);
		}
		
		// add trailing unaligned timeslots, if any
		long[] lastAddedSlot = (long[]) timeOrder.get(timeOrder.size() - 1);
		if (timeSlots.indexOf(lastAddedSlot) != timeSlots.size() - 1) {	 // not last
			for (int i = timeSlots.indexOf(lastAddedSlot); i < timeSlots.size(); i++) {
				timeOrder.add(timeSlots.get(i));
				if (i == timeSlots.size() - 1) {	// align last slot manually
					((long[]) timeSlots.get(i))[1] = lastAddedSlot[1] + 1000;
				}
			}
		}	
		
		ArrayList<String> resultTimeOrder = new ArrayList<String>();
		for (int i = 0; i < timeOrder.size(); i++) {
			resultTimeOrder.add(TS_ID_PREFIX + ((long[]) (timeOrder.get(i)))[0]);
		}
		
		return resultTimeOrder;
	}
	
	private long[] firstTimeSlotAfter(long[] afterTimeSlot, ArrayList<long[]> unalignedSlots) {	
		long[] firstSlot = null;
		long firstTimeAfter = Long.MAX_VALUE;
		
		ArrayList<long[]> unalignedStore = new ArrayList<long[]>();
		
		long afterTime = 0;
		long afterTimeId = -1;
		
		if (afterTimeSlot != null) {
			afterTime = afterTimeSlot[1];
			afterTimeId = afterTimeSlot[0];
		} 
		
		Iterator<long[]> tsIter = timeSlots.iterator();
		while (tsIter.hasNext()) {
			long[] ts = (long[]) tsIter.next();
			
			long time = ts[1];
			if (time < 0) {	// unaligned
				unalignedStore.add(ts);
			}
			else if (	(time >= afterTime) && 
						(time < firstTimeAfter) &&
						(!(ts[0] == afterTimeId)) &&
						(!(timeOrder.contains(ts)))  ) {
				firstTimeAfter = time;
				firstSlot = ts;
				
				unalignedSlots.clear();
				unalignedSlots.addAll(unalignedStore);
				unalignedStore.clear();
			} else if (time > 0){	// not 'first time after', also not unaligned, so reset
				unalignedStore.clear();		
			}
		}	
		
		if (firstSlot == null)	{	// none found
			unalignedSlots.addAll(unalignedStore);
		}
		
		return firstSlot;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTimeSlots(java.lang.String)
	 */
	@Override
	public HashMap<String,String> getTimeSlots(String fileName) {		
		parse(fileName);
		
		// generate HashMap from ArrayList with long[2]'s
		HashMap<String, String> resultSlots = new HashMap<String, String>();
		
		Iterator<long[]> timeSlotIter = timeSlots.iterator();
		while (timeSlotIter.hasNext()) {
			long[] timeSlot = (long[]) timeSlotIter.next();
			String tsId = TS_ID_PREFIX + ((long) timeSlot[0]);
			String timeValue = Long.toString(((long) timeSlot[1]));
		
			resultSlots.put(tsId, timeValue);
		}		
		
		return resultSlots;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTierNames(java.lang.String)
	 */
	@Override
	public ArrayList<String> getTierNames(String fileName) {
		// tierNames in ELAN are either the main tier '*PAR' labels, or
		// the combination of tier label plus participant, like '%mor@PAR'
		parse(fileName);

		return tierNames;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getParticipantOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParticipantOf(String tierName, String fileName) {
		String participant = "";

		if (tierName.startsWith(AST)) {
			participant = tierName.substring(1); // main tier label without *
		}
		else {
			int i = tierName.indexOf(TIER_NAME_SEPARATOR);		// part of tier name after @
			
			if ((i > 0) && (tierName.length() > i+2)) {
				participant = tierName.substring(i+1);
			}
		}
		
		return participant;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypeOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getLinguisticTypeIDOf(
		String tierName,
		String fileName) {
		
		String lingTypeId = "";	
		
		if (tierName.startsWith(AST)) {
			lingTypeId = MAIN_TYPE; // main tier label without *
		}
		else {
			int i = tierName.indexOf(TIER_NAME_SEPARATOR);		// part of tier name after @
		
			if (i > 0) {
				lingTypeId = tierName.substring(0, i);
			}
		}
				
		return lingTypeId;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getParentNameOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParentNameOf(String tierName, String fileName) {
		parse(fileName);
		
		return (String) parentHash.get(tierName);
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getAnnotationsOf(java.lang.String, java.lang.String)
	 */
	@Override
	public ArrayList<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
		parse(fileName);
		
		ArrayList<AnnotationRecord> resultAnnotRecords = new ArrayList<AnnotationRecord>();
		
		Iterator<AnnotationRecord> it = annotRecordToTierMap.keySet().iterator();
		while (it.hasNext()) {
			AnnotationRecord annRec = (AnnotationRecord) it.next();
			if (annotRecordToTierMap.get(annRec).equals(tierName)) {
				resultAnnotRecords.add(annRec);
			}
		}
		
		return resultAnnotRecords;
	}

	private void parse(String fileName) {			
		if (lastParsed.equals(fileName)) {
			return;
		}

		// (re)set everything to null for each parse
		participantLine = null;
		mediaFileName = null;
		chatBlocks.clear();
		lingTypeRecords.clear();
		tierNames.clear();
		parentHash.clear();
		timeOrder.clear();
		timeSlots.clear();
		annotationRecords.clear();
		annotRecordToTierMap.clear();
				
		br = null;
		
		// parse the file
		lastParsed = fileName;
		
		// do actual parsing
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (Exception fnf) {
			fnf.printStackTrace();
		}
		
		String line = null;
		try {
		if ((line = br.readLine()) != null) {
			if (line.startsWith("@UTF8")) {		// CHAT UTF-8
				br.close();
				br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			}
		}
		} catch (IOException iox) {
			iox.printStackTrace();
		}
		
		parseLines();
		processBlocks();
		
		try {
			br.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
	
	private void parseLines() {
		String line = null;
		String outputLine = "";
		boolean recordingParticipant = false;
		boolean containsMediaTime = false;
		int numBlocks = 0;
		
		ArrayList<String[]> chatBlock = null;		
		
		try {
			while ((line = br.readLine()) != null) {
				//line = line.trim();
				// Participant lines from header
				if (line.startsWith(PARTICIPANTS_HEADER)) {
					recordingParticipant = true;
					participantLine = line;
				} 
				else if (recordingParticipant == true) {
					if (!(line.startsWith(AT) || line.startsWith(AST) ||
							line.startsWith(PERC))) {
						// continuation of participants line
						participantLine += line;
					} else { // new header line or block line, end recording
						recordingParticipant = false;
					}
				}
				if (line.startsWith(MEDIA_HEADER)) {
					mediaFileName = extractMediaFromHeader(line);
					continue;
				}
				// CHAT "blocks"
				if (line.startsWith(AST)) { // new block
					// finish last line of previous block
					if (!outputLine.equals("") && (chatBlock != null)) {
						addLineToBlock(outputLine, chatBlock);
					}
					
					// output block
					if (chatBlock != null) {
						chatBlocks.add(chatBlock);
						if (containsMediaTime) {
							blocksWithTime.add(numBlocks);
						}
						numBlocks++;
					}

					// start new recording
					chatBlock = new ArrayList<String[]>();
					containsMediaTime = false;

					if (containsMediaTime(line)) {
						containsMediaTime = true;
					}
					// add line to new recording
					outputLine = line;
				} else if (line.startsWith(PERC) || 
						(line.length() > 1 && line.substring(1).startsWith(PERC))) { // other lines
					// finish last line
					if (!outputLine.equals("") && (chatBlock != null)) {
						addLineToBlock(outputLine, chatBlock);
					}

					outputLine = line;
					
					if ((mediaFileName == null) && 
							(startsWithMediaLabel(line))) { // HS 06-2010 the method startsWithMediaLabel now also checks for BULLET + media label
							//|| (line.length() > 1 && startsWithMediaLabel(line.substring(1))))) {	// bullet in chat-utf8
						containsMediaTime = true;
						// parse this line, second token is media file name.
						StringTokenizer st = new StringTokenizer(line);

						if (st.hasMoreTokens()) { // 'eat' %snd label
							st.nextToken();
						}

						if (st.hasMoreTokens()) {
							mediaFileName = st.nextToken();
						}

						// strip off possible double quotes
						if (mediaFileName.startsWith("\"")) {
							mediaFileName = mediaFileName.substring(1);
						}

						if (mediaFileName.endsWith("\"")) {
							mediaFileName = mediaFileName.substring(0,
									mediaFileName.length() - 1);
						}							
					} else if (startsWithMediaLabel(line)) {
						containsMediaTime = true;
					}
				} else if (!line.startsWith(AT)) { // no label, continuation of previous line
					outputLine += (line.replace('\t', ' '));// check again if it has media times
					if (containsMediaTime(outputLine)) {
						containsMediaTime = true;
					}
				}				
			}

			// finish last line
			if (!outputLine.equals("") && (chatBlock != null)) {
				addLineToBlock(outputLine, chatBlock);
			}
			
			// output last block
			if (chatBlock != null) {
				chatBlocks.add(chatBlock);
				if (containsMediaTime) {
					blocksWithTime.add(numBlocks);
				}
			}

		} catch (FileNotFoundException fex) {
			fex.printStackTrace();
		} catch (IOException iex) {
			iex.printStackTrace();
		}		
	}

	/**
	 * Helper method to avoid copy and paste
	 *
	 * @param file DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
//	private final BufferedReader file2br(File file) throws IOException {
		/*
		   A file is opened from the operating system.
		   This stream of bytes could be a UTF-8 encoded unicode stream.
		   If a file interpreted as UTF-8 contains isolatin-1, the file
		   cannot be read. An Exception is thrown.
		   Therefore, special care has to be taken when reading in UTF-8.
		   As a first measure, the filename is used to decide if to read as UTF-8.
		   This has to be changend in a future version.
		   This is just done in order to include Unicode characters into Eudico.
		 */
/*		Reader filereader;

		if (-1 != file.getName().lastIndexOf(".utf8.")) { // this means 'contains'
			filereader = new InputStreamReader(new FileInputStream(file),
					"UTF-8");
		} else {
			// use the locale encoding.
			filereader = new FileReader(file);
		}

		BufferedReader br = new BufferedReader(filereader);

		return br;
	}
*/

	private void addLineToBlock(String theLine, ArrayList<String[]> theBlock) {
		String label = null;
		String value = null;

		label = getLabelPart(theLine);
		value = getValuePart(theLine);

		if ((label != null) && (value != null)) {
			String[] line = {label, value};
			theBlock.add(line);
		} else if (label != null && value == null){
			// maybe a valid tierlabel with empty annotation content
			String[] line = {label, ""};
			theBlock.add(line);
		}
	}

	private String getLabelPart(String theLine) {
		String label = null;

		int index = theLine.indexOf(COLON);

		if (index > 0) {
			label = theLine.substring(0, index);
		}

		return label;
	}

	private String getValuePart(String theLine) {
		String value = null;

		int index = theLine.indexOf(COLON);

		if (index < (theLine.length() - 2)) {
			value = theLine.substring(index + 1).trim();
		}

		return value;
	}

	private void processBlocks() {
		Set<String> tNames = new HashSet<String>();		

		String annotationIdPrefix = "ann";
		long annotId = 0;
		long tsId = 0;
		
		// store last end per root annot, to prevent overlaps within tier
		HashMap<String, Long> lastEndTimes = new HashMap<String, Long>();	
		
		//long[] firstSlotAfterSync = null;	// store first slot for a range of unaligned blocks,
											// time interval may apply to a sequence of blocks
		// HS May 2010: change in handling of unaligned blocks on top level tiers. 
		// time values of unaligned blocks between two aligned blocks will be interpolated
		int blockIndex = 0;
		int numBlocksForSegment = 1;
		boolean hasTime = false;
		//long sb = 0L;// segment begin, can contain multiple blocks
		long interpol = 0L; // block interpolated "current" end time
		//long se = 0L;// segment end
		long blockDur = 0L;
		
		Iterator<ArrayList<String[]>> blockIter = chatBlocks.iterator();
		while (blockIter.hasNext()) {
			String participantLabel = "";
			String tierName = null;
			String[] mediaLine = null;
			int bi = blocksWithTime.indexOf(blockIndex);
			if (bi > -1) {
				hasTime = true;
				blockDur = 0L;
				if (bi < blocksWithTime.size() -1) {
					int nextWith = blocksWithTime.get(bi + 1);
					numBlocksForSegment = nextWith - blockIndex;
				} else if (blockIndex < chatBlocks.size() - 1) {
					numBlocksForSegment = chatBlocks.size() - 1 - blockIndex;
				}
			} else {
				hasTime = false;
			}
			
			String rootAnnotId = "";
			long beginTSId = 0;
			long endTSId = 0;
			long begin = TimeSlot.TIME_UNALIGNED;
			long end = TimeSlot.TIME_UNALIGNED;
						
			ArrayList<String[]> block = (ArrayList<String[]>) blockIter.next();
			
			Iterator<String[]> lineIter = block.iterator();
			
			while (lineIter.hasNext()) {
				// (compose and) collect tier names
				String[] line = (String[]) lineIter.next();
				String lbl = line[0];
				String value = line[1];

				
				if (lbl.startsWith(AST)) {
					participantLabel = lbl;
					tierName = lbl;
				}
				else if ( !startsWithMediaLabel(lbl) ) { // HS 06-2010 method startsWithMediaLabel changed
							 //&& !((lbl.length() > 1 && startsWithMediaLabel(lbl.substring(1))))) {
					tierName = lbl + participantLabel.replace('*', TIER_NAME_SEPARATOR);
					parentHash.put(tierName, participantLabel);
				}

				tNames.add(tierName);
			
				// create AnnotationRecord for main and dependent tiers
				// create time slots per block
				if (lbl.startsWith(AST)) {	// main utterance tier
					AnnotationRecord annRec = new AnnotationRecord();
					
					rootAnnotId = annotationIdPrefix + annotId;		// store annot id for parent referencing
					
					annRec.setAnnotationId(annotationIdPrefix + annotId++);
					annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
					
					beginTSId = tsId++;
					endTSId = tsId++;
					annRec.setBeginTimeSlotId(TS_ID_PREFIX + Long.toString(beginTSId));
					annRec.setEndTimeSlotId(TS_ID_PREFIX + Long.toString(endTSId));
					
					if (hasTime) {
						// Feb 2006: add support for media and time information on the main 
						// utterance tier: format BULLET%snd:"2MEHT10"_392998_397665BULLET
						// May 2010: temp add support for mov and snd without BULLET
						int index = value.indexOf(BULLET);
						if (index == -1) {
							index = value.indexOf(MOV);
						}
						if (index == -1) {
							index = value.indexOf(SND);
						}
						
						//if (value.indexOf(BULLET) > -1) {
						if (index > -1) {
							mediaLine = extractMediaAndTime(value.substring(index));
							if (mediaLine != null) {
								if (mediaLine[0] != null && startsWithMediaLabel(mediaLine[0])) {
									if (mediaLine[1] != null && mediaFileName == null) {
										mediaFileName = mediaLine[1];
									}
								}// HS Nov 2013 cater for the situation that the media file is null but bt and et !null
								if (mediaLine[2] != null) {
									try {
										begin = Long.parseLong(mediaLine[2]);
									} catch (NumberFormatException nfe) {
										System.out.println("Invalid time value: " + mediaLine[2]);
									}
								}
								if (mediaLine[3] != null) {
									try {
										end = Long.parseLong(mediaLine[3]);
									} catch (NumberFormatException nfe) {
										System.out.println("Invalid time value: " + mediaLine[3]);
									}
								}
								//}					
							}
							//annRec.setValue(value.substring(0, value.indexOf(BULLET)).trim());
							// annRec.setValue(value.substring(0, index));
							// HS Nov 2013 filter the annotation content
							annRec.setValue(filterValue(value.substring(0, index)));
						} else {
							// HS Nov 2013 filter the annotation content
							annRec.setValue(filterValue(value));
						}
					} else {
						// HS Nov 2013 filter the annotation content
						annRec.setValue(filterValue(value));
					}
					
					annotationRecords.add(annRec);
					annotRecordToTierMap.put(annRec, tierName);
				}
				else if (hasTime && startsWithMediaLabel(lbl)) {// HS 06-2010 startsWithMediaLabel changed
						//|| (lbl.length()>1 && startsWithMediaLabel(lbl.substring(1)))) {
					String timeString = value;

					if (timeString != null) {
						StringTokenizer st = new StringTokenizer(timeString);

						if (st.hasMoreTokens()) {	// skip first token, the sound file name
							st.nextToken();
						}

						if (st.hasMoreTokens()) {	// second token is begin time
							String bString = st.nextToken();
							int positionOfDot = bString.indexOf(".");	// for MED/X-Waves aligned CHAT data
							if (positionOfDot > 0) {
								bString = bString.substring(0, positionOfDot);
							}
							
							begin = Long.parseLong(bString);
						}

						if (st.hasMoreTokens()) {	// third token is end time
							String eString = st.nextToken();
							int positionOfDot = eString.indexOf(".");	// for MED/X-Waves aligned CHAT data
							if (positionOfDot > 0) {
								eString = eString.substring(0, positionOfDot);
							}

							end = Long.parseLong(eString);
						}
					}

				}
				else {	// consider reference annotation on dependent tier
					AnnotationRecord annRec = new AnnotationRecord();
					
					annRec.setAnnotationId(annotationIdPrefix + annotId++);
					annRec.setAnnotationType(AnnotationRecord.REFERENCE);					
					annRec.setReferredAnnotId(rootAnnotId);
					// // HS Nov 2013 filter the annotation content
					annRec.setValue(filterValue(value));
					
					annotationRecords.add(annRec);
					annotRecordToTierMap.put(annRec, tierName);
				}			
			}
			
			long beginMsec = TimeSlot.TIME_UNALIGNED;
			long endMsec = TimeSlot.TIME_UNALIGNED;
			
			if (hasTime) {				
				beginMsec = begin;
				// prevent overlaps within one tier
				long lastEnd = 0;
				if (lastEndTimes.get(participantLabel) != null) {
					lastEnd = ((Long) lastEndTimes.get(participantLabel)).longValue();
				}
				if ( lastEnd > beginMsec ) {
					beginMsec = lastEnd;
				}
				blockDur = (end - begin) / numBlocksForSegment;
				if (blockDur <= 0) {
					System.out.println("Overlapping annotations on a tier: " + tierName + " at: " + begin);
				}
				endMsec = beginMsec + blockDur;
				interpol = endMsec;
				
				if (end > beginMsec) {
					lastEndTimes.put(participantLabel, new Long(endMsec));
				}
			} else {
				if (interpol > 0) {
					beginMsec = interpol;
					endMsec = beginMsec + blockDur;
					interpol = endMsec;
				}
			}			
			/* May 2010
			if (begin != TimeSlot.TIME_UNALIGNED) {
				beginMsec = (long) begin;
				
				// prevent overlaps within one tier
				long lastEnd = 0;
				if (lastEndTimes.get(participantLabel) != null) {
					lastEnd = ((Long) lastEndTimes.get(participantLabel)).longValue();
				}
				if ( lastEnd > beginMsec ) {
					beginMsec = lastEnd;
				}
			} 

			if (end != TimeSlot.TIME_UNALIGNED) {
				endMsec = (long) end;
				lastEndTimes.put(participantLabel, new Long(endMsec));
			} 
			*/
			long[] bSlot = {beginTSId, beginMsec};
			
			// in case %snd time intervale applies to a sequence of blocks, store first slot for later alignment
//			if ((firstSlotAfterSync == null) && (begin == TimeSlot.TIME_UNALIGNED)) {	// store 
//				firstSlotAfterSync = bSlot;
//			}// removed May 2010
					
			long[] eSlot = {endTSId, endMsec};
			/* May 2010
			if (	(firstSlotAfterSync != null) && 
					(begin != TimeSlot.TIME_UNALIGNED) && 
					(end != TimeSlot.TIME_UNALIGNED) ) {
						
				firstSlotAfterSync[1] = beginMsec;	
				bSlot[1] = TimeSlot.TIME_UNALIGNED;	
				
				firstSlotAfterSync = null;
			}
			*/
			timeSlots.add(bSlot);
			timeSlots.add(eSlot);	
			
			blockIndex++;
		}
		
		tierNames = new ArrayList<String>(tNames);				
	}
	
	private boolean startsWithMediaLabel(String line) {
		boolean start = false;
		
		if (line.startsWith(SND) || line.startsWith(MOV)) {
			start = true;
		}
		// HS 06-2010 check for BULLET + media label as well
		if (line.startsWith(BULLET + SND) || line.startsWith(BULLET + MOV)) {
			start = true;
		}
		
		return start;
	}
	
	private boolean containsMediaTime(String line) {
		if (line == null || line.length() == 0) {
			return false;
		}
		
		if (line.indexOf(SND) > -1) {
			return true;
		}
		if (line.indexOf(MOV) > -1) {
			return true;
		}
		if (line.indexOf(BULLET) > -1) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Extracts label, medianame, begintime and endtime from a CHAT formatted 
	 * media string. This is the media string that follows an utterance on the 
	 * same line (as opposed to media information in a separate tier)
	 * Format: {BULLET}%snd:"1MEHT10"_8742_10762{BULLET}
	 * @param value the formatted string
	 * @return a String array with the single tokens
	 */
	private String[] extractMediaAndTime(String value) {
		if (value == null) {
			return null;
		}
		String[] result = new String[4];
		StringBuilder buf = new StringBuilder(value);
		// remove bullets
		if (buf.charAt(0) == BULLET) {
			buf.delete(0, 1);
		}
		if (buf.charAt(buf.length() - 1) == BULLET) {
			buf.delete(buf.length() - 1, buf.length());
		}
		int colon = buf.indexOf(COLON);
		int quot = buf.indexOf("\"");
		int quot2 = buf.lastIndexOf("\"");
		if (colon > -1) {
			result[0] = buf.substring(0, colon);//%snd or %mov
		}
		if (quot > -1 && quot2 > quot + 1) {
			// media filename, without extension!
			result[1] = buf.substring(quot + 1, quot2);
			
			int under = buf.indexOf("_", quot2);
			int under2 = buf.indexOf("_", under + 1);
			if (under > -1) {
				if (under2 > under + 1) {
					result[2] = buf.substring(under + 1, under2);
					if (under2  < buf.length() - 1) {
						result[3] = buf.substring(under2 + 1);
					}					
				} else {
					// only begintime?
					result[2] = buf.substring(under + 1);
				}
			}
		} 
		
		if (colon < 0 && quot < 0) {// might be time info only, BULLET3000_4000BULLET
			int under = buf.indexOf("_");
			if (under > -1 && under < buf.length() - 1) {
				int under2 = buf.indexOf("_", under + 1);// double check
				if (under2 < 0) {
					result[2] = buf.substring(0, under);
					result[3] = buf.substring(under + 1);
				}
			}
		}
		
		return result;
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
	 * Filters characters out of (annotation) values that are illegal in xml content.
	 * 
	 * @param value the input value
	 * @return a  string without characters that are not valid in xml documents
	 */
	private String filterValue(String value) {
		if (value == null || value.length() == 0) {
			return value;
		}
		
		// for filtering out illegal xml characters
		StringBuilder b = new StringBuilder(value.length());
		char[] ch = value.toCharArray();
		for (char c : ch) {
			if (c >= '\u0020') {
				b.append(c);
			} else {
				System.out.println("Illegal char in CHAT content: " + Integer.toHexString(c));
			}
		}
		
		return b.toString();
	}
	
	/**
	 * Extracts the media file name from a "@Media" header line.
	 * @param line the input line
	 * @return the extracted file name, or null
	 */
	private String extractMediaFromHeader(String line) {
		if (line == null) {
			return null;
		}
		// format is e.g.
		// @Media: file_name, video
		if (!line.startsWith(MEDIA_HEADER)) {// double check
			return null;
		}
		int colonIndex = line.indexOf(':');
		if (colonIndex > -1 && colonIndex < line.length() - 1) {
			String medString = line.substring(colonIndex + 1);
			
			int commaIndex = medString.indexOf(',');
			if (commaIndex > -1) {
				// could check the audio/video attribute, but what to do with it?
				// look in the folder where the .cha file is and test for well known extensions?
				return medString.substring(0, commaIndex).trim();
			} else {
				return medString.trim();
			}
		}
		
		return null;
	}
}
