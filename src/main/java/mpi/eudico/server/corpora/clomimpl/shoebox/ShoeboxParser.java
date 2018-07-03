/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.utr22.SimpleConverter;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.util.ServerLogger;

/**
 * @author hennie
 *
 * @version sep 2005 the constructor is now public giving up the singleton pattern.
 * The path parameter of all getter methods could be removed in the next parser version
 * (add a public parse(String path) method)
 * Hashtable and Vector in Parser have been replaced by HashMap and ArrayList
 * @version may 2006 Shoebox Unicode tiers are now pre-edited in ShoeboxArray, lifting the need for 
 * special treatment in the methods where single words are extracted from the marker lines.
 */
public class ShoeboxParser extends Parser implements ServerLogger {
	//private static ShoeboxParser parser;
	
	private final static String ANN_ID_PREFIX = "ann";
	private final static String TS_ID_PREFIX = "ts";
	
	private long annotId = 0;
	private long tsId = 0;
	
	private SimpleConverter simpleConverter;

	private ShoeboxArray sbxfile; // shoebox transcription file
	private ShoeboxTypFile typfile; // shoebox typ file
	private ToolboxDecoderInfo decoderInfo;

	/**
	 * Hierachical structure of the tags in the shoebox file. Elements are of
	 * type String.
	 */
	DefaultMutableTreeNode tiertree = new DefaultMutableTreeNode();

	private List<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>();
	private List<String> participantOrder = new ArrayList<String>();
	private Set<String> tierNameSet = new TreeSet<String>();
	private List<long[]> timeOrder = new ArrayList<long[]>();	// of long[2]
	private List<long[]> timeSlots = new ArrayList<long[]>(); // of long[2], {id,time}
	private List<AnnotationRecord> annotationRecords = new ArrayList<AnnotationRecord>();
	private Map<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();

	private String lastParsed = "";
	// for calculation of 'root annotation' times
	private List<long[]> rootSlots = new ArrayList<long[]>(); // of long[2], {id,time}
	// flag whether or not to try to fix improper Toolbox alignment, see snapWord
	// this could be made settable
	private boolean fixImproperAlign = true;
	private int preferredBlockDuration = ToolboxDecoderInfo.DEFAULT_BLOCK_DURATION;
	
	
	/**
	 * Public constructor: the Singleton pattern is no longer applied to the parsers.
	 * Create a new Parser for every file to parse.
	 */
	public ShoeboxParser() {
		try {
			simpleConverter = new SimpleConverter(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The instance method returns the single incarnation of CHATParser to the
	 * caller.
	 *
	 * @return DOCUMENT ME!
	 */
	/*
	public static ShoeboxParser Instance() {
		if (parser == null) {
			try {
				parser = new ShoeboxParser();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return parser;
	}
	*/
	
	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getMediaDescriptors(java.lang.String)
	 */
	@Override
	public ArrayList<MediaDescriptor> getMediaDescriptors(String fileName) {
		parse(fileName);
		return sbxfile.getMediaDescriptors();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypes(java.lang.String)
	 */
	@Override
	public List<LingTypeRecord> getLinguisticTypes(String fileName) {
		parse(fileName);		
		
		Iterator<String> en = sbxfile.getLabels();
		while (en.hasNext()) {
			String label = (String) en.next();
			if (	!(label.equals(ShoeboxArray.label_eudicoparticipant)) &&
					!(label.equals(ShoeboxArray.label_eudicot0)) &&
					!(label.equals(ShoeboxArray.label_eudicot1)) &&
					!(label.equals(ShoeboxEncoder.elanParticipantLabel)) &&
					!(label.equals(ShoeboxEncoder.elanBeginLabel)) &&
					!(label.equals(ShoeboxEncoder.elanEndLabel)) &&
					!(label.equals(ShoeboxEncoder.elanBlockStart)) &&
					!(label.equals(ShoeboxEncoder.elanELANLabel))) {
					
				String ltName = label.substring(1); // cut off backslash
				
				LingTypeRecord lt = new LingTypeRecord();
				lt.setLingTypeId(ltName);
				
				// set defaults
				lt.setTimeAlignable("false");
				lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
				
				// set default for root tier
				if ((typfile.interlinearRootMarker != null) &&
					typfile.interlinearRootMarker.equals(ltName)) {

					lt.setStereoType(null);
					lt.setTimeAlignable("true");						
				} 
				
				if (typfile.getDatabaseType().equals(ShoeboxEncoder.defaultDBType) &&
					!typfile.tofromHash.containsKey(ltName)) { // root tiers for import of ELAN exported Toolbox files
					lt.setStereoType(null);
					lt.setTimeAlignable("true");											
				}				
				
				// make first marker under recordMarker a symbolic subdivision of record marker
				// if not already set by user defined shoebox markers
		//		if (	!typfile.interlinearRootMarker.equals(ltName) &&
				if(		typfile.tofromHash.containsKey(ltName) &&	// if not root tier
						typfile.fromArray.contains(label) &&
						!typfile.procedureTypeHash.containsValue(label)	) {
					
					lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
					lt.setTimeAlignable("false");
				}
				
				String procType = typfile.procedureTypeHash.get(label);
				if (procType != null) {
					if (procType.equals("Lookup")) {
						lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
						lt.setTimeAlignable("false");
					}
					else if (procType.equals("Parse")) {
						lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
						lt.setTimeAlignable("false");						
					}
					else if (procType.equals("TimeSubdivision")) {
						lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
						lt.setTimeAlignable("true");						
					}
					else if (procType.equals("IncludedIn")) {
						lt.setStereoType(Constraint.stereoTypes[Constraint.INCLUDED_IN]);
						lt.setTimeAlignable("true");						
					}
				}				
			
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

		ArrayList<String> resultTimeOrder = new ArrayList<String>();
		for (int i = 0; i < timeOrder.size(); i++) {
			resultTimeOrder.add(TS_ID_PREFIX + (timeOrder.get(i))[0]);
		}
		
		return resultTimeOrder;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTimeSlots(java.lang.String)
	 */
	@Override
	public HashMap<String, String> getTimeSlots(String fileName) {
		parse(fileName);

		// generate HashMap from ArrayList with long[2]'s
		HashMap<String, String> resultSlots = new HashMap<String, String>();
		
		Iterator<long[]> timeSlotIter = timeSlots.iterator();
		while (timeSlotIter.hasNext()) {
			long[] timeSlot = timeSlotIter.next();
			String tsId = TS_ID_PREFIX + (timeSlot[0]);
			String timeValue = Long.toString((timeSlot[1]));
		
			resultSlots.put(tsId, timeValue);
		}		
		
		return resultSlots;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTierNames(java.lang.String)
	 */
	@Override
	public List<String> getTierNames(String fileName) {
		parse(fileName);
		
		// strip begin backslashes
		List<String> result = new ArrayList<String>();
		
		// add in same order as in shoebox file
		List<String> allNames = new ArrayList<String>(tierNameSet);
		List<String> markerOrder = sbxfile.getMarkerOrder();
		String spk = null;
		String marker = null;
		
		for (int i = 0; i < participantOrder.size(); i++) {
		    spk = participantOrder.get(i);
		    for (int j = 0; j < markerOrder.size(); j++) {
		        marker = markerOrder.get(j);
		        
		        if (allNames.contains(marker + "@" + spk)) {
		            result.add((marker + "@" + spk).substring(1));
		        }
		    }
		}
		/*
		Iterator iter = tierNameSet.iterator();
		while (iter.hasNext()) {
			String tierName = (String) iter.next();
			tierName = tierName.substring(1);
			result.add(tierName);
		}
		*/	
		return result;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getParticipantOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParticipantOf(String tierName, String fileName) {
		String result = "";
		
		parse(fileName);
		
		int index = tierName.indexOf("@");
		if ((index > 0) && (tierName.length() > index+1)) {
			result = tierName.substring(index + 1);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypeIDOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {
		String result = tierName;
		
		parse(fileName);
		
		int index = tierName.indexOf("@");
		if (index > 0) {
			result = tierName.substring(0, index);
		}		
		
		return result;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getParentNameOf(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParentNameOf(String tierName, String fileName) {
		String parentName = null;
		
		parse(fileName);
		
		String labelPart = "\\" + tierName;
		String spkr = "@";
		
		int index = tierName.indexOf("@");
		if ((index > 0) && (tierName.length() > index)) {
			labelPart = "\\" + tierName.substring(0, index);
			spkr = tierName.substring(index);
		}
				
		// use typfile.tofromHash, or parent is typfile.recordMarker
		if (typfile.tofromHash.keySet().contains(labelPart)) {
			parentName = typfile.tofromHash.get(labelPart).substring(1);
		}
		else if ((typfile.interlinearRootMarker != null) && 
				(!tierName.equals(typfile.interlinearRootMarker + spkr))) {
			parentName = typfile.interlinearRootMarker;
		}
		
		if (parentName != null) {
			return parentName + spkr;
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getAnnotationsOf(java.lang.String, java.lang.String)
	 */
	@Override
	public ArrayList<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
		parse(fileName);
		
		ArrayList<AnnotationRecord> resultAnnotRecords = new ArrayList<AnnotationRecord>();
		tierName = "\\" + tierName;
		
		Iterator<AnnotationRecord> it = annotRecordToTierMap.keySet().iterator();
		while (it.hasNext()) {
			AnnotationRecord annRec = it.next();
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
		// these calls could be removed since the parser is no longer 
		// used as a singleton
		lingTypeRecords.clear();
		participantOrder.clear();
		tierNameSet.clear();
		timeOrder.clear();
		timeSlots.clear();
		annotationRecords.clear();
		annotRecordToTierMap.clear();
		rootSlots.clear();
		
		annotId = 0;
		tsId = 0;
		
		tiertree = null;
		
		// parse the file
		lastParsed = fileName;
		
		// fall back: check if mkr file present in shoebox txt file's directory
		List<MarkerRecord> markers = null;
		String typFileName = "";
		if (decoderInfo != null) {
		    markers = decoderInfo.getShoeboxMarkers();
		    typFileName = decoderInfo.getTypeFile();
		}
		// parse
		checkArguments(fileName, typFileName, markers);
       
       	try {
			if (typFileName.equals("")) {
				typfile = new ShoeboxTypFile(markers);
			}
			else {
				typfile = new ShoeboxTypFile(new File(typFileName));
				typfile.setAllTiersUnicode(decoderInfo.isAllUnicode());
			}
	 
	 		String rootMarker = null;
	 		if (typfile.interlinearRootMarker != null) {
	 			rootMarker = "\\" + typfile.interlinearRootMarker;
	 		}
			sbxfile = new ShoeboxArray(new File(fileName),
				  rootMarker, typfile);
	
			tiertreeInit();

			for (int i = 0; i < sbxfile.getNumberOfBlocks(); i++) {
				createBlock(i);
			}
				
			// loop over root slots
			/*
			long[] slot;
			for (int i = 0; i < rootSlots.size(); i++) {
				slot = (long[]) rootSlots.get(i);
				System.out.println("" + i + ": " + slot[1]);
			}
			*/
			// calculate times for unaligned 'root' slots
			calculateRootTimes();
			/*
			for (int i = 0; i < rootSlots.size(); i++) {
				slot = (long[]) rootSlots.get(i);
				System.out.println("" + i + ": " + slot[1]);
			}
			*/
       	} catch (Exception e) {
       		e.printStackTrace();
       	}
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param sbxfn DOCUMENT ME!
	 * @param typfn DOCUMENT ME!
	 * @param mfn DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	private final void checkArguments(String sbxfn, String typfn, List<MarkerRecord> markers)
		throws IllegalArgumentException {
		if ((sbxfn == null) || (sbxfn.length() == 0)) {
			throw new IllegalArgumentException("Please specify a shoebox file.");
		}

		if (((typfn == null) || (typfn.length() == 0)) &&
			(markers.size() == 0)) {
				
			// try to fall back on sbxfn.mkr file in same directory
			String mkrFileName = sbxfn;
			if (sbxfn.endsWith(".txt")) {
				mkrFileName = sbxfn.substring(0, sbxfn.length()-4) + ".mkr";	
			}
			if (new File(mkrFileName).exists()) {
				readMarkersFromFile(mkrFileName, markers);
			}
			else {	
				throw new IllegalArgumentException(
					"Please specify a shoebox typ file or define markers.");
			}
		}

		if (!(new File(sbxfn)).exists()) {
			throw new IllegalArgumentException("Shoebox file doesn't exist.");
		}

		if (typfn != null && typfn.length() > 0 && !(new File(typfn)).exists()) {
			throw new IllegalArgumentException(
				"Shoebox type file doesn't exist.");
		}
	}
	
	private void readMarkersFromFile(String mkrFileName, List<MarkerRecord> markers) {
		File f = new File(mkrFileName);
			
		if (f != null) {
			String line = null;
				
			FileReader filereader = null;
			BufferedReader br = null;

			try {
				filereader = new FileReader(f);
				br = new BufferedReader(filereader);
					
				MarkerRecord newRecord = null;
					
				while ((line = br.readLine()) != null) {
					line = line.trim();
					String label = getLabelPart(line);
					String value = getValuePart(line);
						
					if (label.equals("marker")) {
						newRecord = new MarkerRecord();
						if (!value.equals("null")) {
							newRecord.setMarker(value);
						}
					} else if (newRecord == null) {
						// do nothing but guard against it being a null pointer below.
					} else if (label.equals("parent")){
						if (!value.equals("null")) {
							newRecord.setParentMarker(value);
						}
					} else if (label.equals("stereotype")) {
						if (!value.equals("null")) {
							newRecord.setStereoType(value);
						}
					} else if (label.equals("charset")) {
						if (!value.equals("null")) {
							newRecord.setCharset(value);
						}
					} else if (label.equals("exclude")) {
						if (!value.equals("null")) {
							if (value.equals("true")) {
								newRecord.setExcluded(true);
							} else {
								newRecord.setExcluded(false);
							}
						}
					} else if (label.equals("participant")) {
						if (!value.equals("null")) {
							if (value.equals("true")) {
								newRecord.setParticipantMarker(true);
							} else {
								newRecord.setParticipantMarker(false);
							}
						}	
							
						markers.add(newRecord);
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (filereader != null) {
						filereader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private String getLabelPart(String theLine) {
		String label = null;

		int index = theLine.indexOf(':');

		if (index > 0) {
			label = theLine.substring(0, index);
		}

		return label;
	}

	private String getValuePart(String theLine) {
		String value = null;

		int index = theLine.indexOf(':');

		if (index < (theLine.length() - 2)) {
			value = theLine.substring(index + 1).trim();
		}

		return value;
	}
	
	/**
	 * Initialise tiertree. 
	 */
	private final void tiertreeInit() {
		DefaultMutableTreeNode elanTopNode = null;
		
		String rec = "\\" + typfile.interlinearRootMarker;
		
		tiertree = new DefaultMutableTreeNode();
		
		// when exported from ELAN multiple tier trees can exist.
		// in that case, put \ELANExport label on top of all trees.
		if (typfile.getDatabaseType().equals(ShoeboxEncoder.defaultDBType)) {	// ELAN exported
			elanTopNode = new DefaultMutableTreeNode(ShoeboxEncoder.elanELANLabel);
			tiertree.add(elanTopNode);
		}
		else {
			elanTopNode = tiertree;
		}

		for (int i = 0; i < sbxfile.getNumberOfLabels(); i++) {
			String label = sbxfile.getLabel(i);
			
			if (label.startsWith("\\ELAN")) {
				continue;
			}
			DefaultMutableTreeNode labelNode = sbxfile.getLabelNode(i);

			if (label.equals(rec)) {
			//	tiertree.add(labelNode);
				elanTopNode.add(labelNode);

				continue;
			}

			DefaultMutableTreeNode parentnode = null;
			String parent = null;

			if (typfile.tofromHash.containsKey(label)) {
				parent = typfile.tofromHash.get(label);
				parentnode = sbxfile.getLabelNode(parent);
			} else if (!typfile.excludeFromImport(label)) {
				if (typfile.getDatabaseType().equals(ShoeboxEncoder.defaultDBType)) {
					parentnode = elanTopNode;
				}
				else {
					parentnode = sbxfile.getLabelNode(rec);
				}				
			}

//			  logger.log(Level.INFO, "attaching " + label + " to " + parent);
//			if (parent != null) {
			if (parentnode != null) {
//				parentnode = sbxfi le.getLabelNode(parent);
				parentnode.add(labelNode);
			}
		}

		// Loop all fields(columns in array), in breadth-first order
//		Enumeration fibfo = tiertree.breadthFirstEnumeration();
//
//		while (fibfo.hasMoreElements()) {
//			DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) fibfo.nextElement();
 //           logger.log(Level.INFO,
 //               "-- tiertree: " + dmt.getParent() + " ... " + dmt);
//		}
	}

	/**
	 * <p>
	 * MK:02/06/10<br> The top tier starts with the shoebox record marker.
	 * Because the same shoebox top tier can have different speakers with each
	 * block, which map to different CLOM-participants with each
	 * CLOM-annotation, in CLOM, there will be as many top tiers as there are
	 * speakers, build as ref_AT_Paul, ref_AT_Mary, etc.<br>
	 * </p>
	 *
	 * @param row Sbxfile row
	 *
	 * @throws Exception up
	 */
	private void createBlock(int row) throws Exception {
 //       logger.log(Level.INFO, "== createBlock(" + row);

		String spk = sbxfile.getSpeaker(row);
		long t0 = sbxfile.getT0(row);
		long t1 = sbxfile.getT1(row);
			
		String rootMarker = sbxfile.getRootMarkerForBlock(row);
		
//		String val = sbxfile.getCell("\\" + typfile.interlinearRootMarker, row);
		String val = sbxfile.getCell(rootMarker, row);
		if (decoderInfo != null && decoderInfo.isTimeInRefMarker()) {
		    t0 = sbxfile.toMilliSeconds(val, row);
		    t1 = -1;
		}
		// top tier name: typfile.recordMarker@spk
//		String tierName = "\\" + typfile.interlinearRootMarker + "@"  + spk;
		String tierName = rootMarker + "@"  + spk;

		if (!participantOrder.contains(spk)) {
		    participantOrder.add(spk);
		}
		tierNameSet.add(tierName);
		
		// add AnnotationRecord.
		//   create timeslot
		AnnotationRecord annRec = new AnnotationRecord();
					
		annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
		annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
					
		long beginTSId = tsId++;
		long endTSId = tsId++;
		annRec.setBeginTimeSlotId(TS_ID_PREFIX + Long.toString(beginTSId));
		annRec.setEndTimeSlotId(TS_ID_PREFIX + Long.toString(endTSId));
					
		annRec.setValue(val);
					
		annotationRecords.add(annRec);
		annotRecordToTierMap.put(annRec, tierName);
		
		// store timeslots
		long[] begin = {beginTSId, t0};
		long[] end = {endTSId, t1};
		
		timeSlots.add(begin);
		timeSlots.add(end);
		
		timeOrder.add(begin);
		timeOrder.add(end);
		
		rootSlots.add(begin);
		rootSlots.add(end);
		// HACK: make sure top tier of interlinearized tiers is the first
		String topTierName = "";
/*		Set toNames = typfile.tofromHash.keySet();

		Enumeration e = typfile.tofromHash.elements(); // iterate over 'parents'

		while (e.hasMoreElements()) {
			String fromName = (String) e.nextElement();

			if (!toNames.contains(fromName)) { // 'from' name not also pointed at
				topTierName = fromName;

				break;
			}
		}
*/

		// hb, 17 sep 04: alternative hack, one that does work
		// (what fun it is to work with old shit)
		
		// MK's code assumes that interlinear tiers are handled before additional 'symbolic
		// association' tiers. Therefore, reorder by putting the first interlinear line under the 
		// interlinearRootMarker first in vdcs
		
		// iterate over 'to' names. If 'from' value is interlinearRootMarker and 'to' is element 
		// of 'from' (has children), then make it topTierName
/*		Enumeration toEnum = typfile.tofromHash.keys();
		while (toEnum.hasMoreElements()) {
			String toName = (String) toEnum.nextElement();
			
			if (	(((String) typfile.tofromHash.get(toName)).equals(rootMarker)) &&
					(typfile.tofromHash.containsValue(toName))) {
						
				topTierName = toName;
				break;		
			}
		} */
		topTierName = rootMarker;

		List<String> vdcs = childs(rootMarker);

		if (topTierName != "") {
			int maxDepth = 0;
			
			List<String> reorderedChildren = new ArrayList<String>();

			Iterator<String> vdcsIter = vdcs.iterator();

			while (vdcsIter.hasNext()) {
				String n = vdcsIter.next();
				
				// hb, 19-4-05
				DefaultMutableTreeNode childNode = sbxfile.getLabelNode(n);
				if (childNode.getDepth() > maxDepth) {
					reorderedChildren.add(0,n);
					maxDepth = childNode.getDepth();
				}
				else {
					reorderedChildren.add(n);
				}

			/*	if (n.equals(topTierName)) {
					reorderedChildren.add(0, n);
				} else {
					reorderedChildren.add(n);
				} */
			}

			vdcs = reorderedChildren;
		}

		// Annotation has to be split up by words
		int wordcounter = 0;
		boolean hasMoreWords = true;

		while (hasMoreWords) {
//			  logger.log(Level.INFO, "-- create Block for word " + wordcounter);
			//System.out.println("wc: " + wordcounter);
			Iterator<String> dcs = vdcs.iterator();
 //           logger.log(Level.INFO, "== vdcs=" + vdcs);
			//hasMoreWords = createChildsInBlock(annRec, dcs, row, null, wordcounter); 
			// HS may 06 new implementation after change in ShoeboxArray: Unicode tiers are converted there
			hasMoreWords = createChildrenInBlock(annRec, dcs, row, null, wordcounter);
			wordcounter += 1;
		}
	}
	
	/**
	 * Recursively find word boundaries and create annotations.
	 * 
	 * @param par the parent annotation
	 * @param brothers enumeration of sibling
	 * @param row the current row or block or record index
	 * @param wordboundaries the wordboundaries of the parent
	 * @param wordcount the (current) index in the list of boundaries
	 * @return true as long as there are more siblings to process
	 * @throws Exception ??
	 */
	private boolean createChildrenInBlock(AnnotationRecord par, Iterator<String> brothers,
			int row, List<Integer> wordboundaries, int wordcount) throws Exception {
		boolean result = true;

		if (!brothers.hasNext()) {
 //           logger.log(Level.INFO, "== ending recursion");

			return false;
		}

		String name = brothers.next();
		String spk = sbxfile.getSpeaker(row);
//		  logger.log(Level.INFO,
//			  "==  createChildsInBlock(" + par.getValue() + ", '" + name + "', " +
//			  row + ", " + wordboundaries + ", " + wordcount);

		List<Integer> mywordboundaries = null;
		String val = sbxfile.getCell(name, row);

		if ((val == null) || (val.length() == 0)) {
			// skip this value, but there might be more brothers
			return createChildrenInBlock(par, brothers, row, wordboundaries,
				wordcount);
		}

		if ((simpleConverter != null) && typfile.isIPAtier(name)) {
			val = simpleConverter.toUnicode(val);
		}
         
		List<String> vdcs = childs(name);
		Iterator<String> dcs = vdcs.iterator();
		boolean iHaveKids = dcs.hasNext();

		// HS March 2007: if the marker has no children but is the first element in a Parse procedure 
		// (subdivision) treat it as if it had children?
		// Sep 2007: still not right: different files with a single "parse" markers were treated 
		// differently: sometimes only the first word of the subdivision appeared was extracted
		// special case for 1 "parse" (subdivision) marker without dependent markers
		if (wordboundaries == null && !iHaveKids && typfile.getInterlinearTierMarkers().contains(name)) {
			String procName = typfile.procedureTypeHash.get(name);
			if (procName != null && (procName.equals("TimeSubdivision") || 
					procName.equals("IncludedIn") || procName.equals("Parse"))) {
				if (wordcount > 0) {
					return false;
				}
				StringTokenizer valToken = new StringTokenizer(val);
				String annVal;
				while (valToken.hasMoreTokens()) {
					annVal = valToken.nextToken();
					AnnotationRecord annRec = new AnnotationRecord();
					
					annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
					annRec.setValue(annVal.trim());
					if (procName.equals("TimeSubdivision") || procName.equals("IncludedIn")) {
						annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
						annRec.setReferredAnnotId(par.getAnnotationId());	// used to create and connect timeslots
						
						// NOTE: order of these 3 statements important
						annotRecordToTierMap.put(annRec, name + "@" + spk);
						createAndConnectTimeSlots(annRec);					
						annotationRecords.add(annRec);	
					} else {
						annRec.setAnnotationType(AnnotationRecord.REFERENCE);					
						annRec.setReferredAnnotId(par.getAnnotationId());
												
						// NOTE: order of these 3 statements important
						annotRecordToTierMap.put(annRec, name + "@" + spk);
						fillInPrevAnnotRef(annRec);					
						annotationRecords.add(annRec);
					}
				}
				if (!participantOrder.contains(spk)) {
				    participantOrder.add(spk);
				}
				tierNameSet.add(name + "@" + spk);
				
				return createChildrenInBlock(par, brothers, row,
						wordboundaries, wordcount);
			}
		    //iHaveKids = true;//??
		}
		
		// HS Apr 2008: special treatment for symbolically associated (lookup) markers
		// the splitting up and "snapping" of words does not always work
		// and is (maybe) unnecessary
		
		//if (!typfile.getInterlinearTierMarkers().contains(name)){
		if (typfile.getInterlinearTierMarkers().size() == 0){
			String procName = typfile.procedureTypeHash.get(name);
			if (procName != null && (procName.equals("Lookup"))) {
				//System.out.println("SA: " + val);
				return createSymAssociatedChildren(par, brothers, row, name);
			}
		}
		
		if ((wordboundaries == null) && !iHaveKids) {
			// append only once
			if (wordcount > 0) {
				return false;
			}

			//	logger.log(Level.INFO, "== ("+val.substring(0, 8)+") is hanging childless under \\ref ");
//			  logger.log(Level.INFO,
 //               "== (" + val + ") is hanging childless under \\ref ");
		} else {
			// tier is not under ref, kids or not
			// use the wordcounter
			if (wordboundaries == null) {
				// ref is my parent and I have kids, I set the wordboundaries and the wordcounter.
				mywordboundaries = wordbounds(val);
				
				result = wordcount < (mywordboundaries.size() - 2);
			} else {
				// my parent set some wordboundaries
				// I have to get the right word
				mywordboundaries = wordboundaries;

				// If I have (inner )wordboundaries myself,
				// I have to ignore parents bounds and set a new ones.
				String xval = snapWord(val, mywordboundaries, wordcount, true);
				int index = mywordboundaries.get(wordcount).intValue();
				
				// pad xval with spaces until endIndex (hb, 3 sept 04)
				int endIndex = mywordboundaries.get(wordcount+1).intValue();
				
				int xvalLength = xval.length();
				for (int i = 0; i < (endIndex - index - xvalLength - 1); i++) {
					xval += " ";
				}			
				
				List<Integer> xmywordboundaries = wordbounds(xval, index);
//				  logger.log(Level.INFO,
//					  "             (" + xval + ") " + index + "/" +
 //                   xmywordboundaries);

				if (xmywordboundaries.size() > 2) {
					/////////////////////////////////
					//  recursion over trees within words (bern -e)
					int ww_wordcount = 0;
					boolean ww_hasMoreWords = true;

					while (ww_hasMoreWords) {
						String ww_val = snapWord(val, xmywordboundaries,
								ww_wordcount, true);
 //                       logger.log(Level.INFO,
 //                           " ...........            '" + ww_val + "', of " +
 //                           ww_wordcount);
 //                       logger.log(Level.INFO,
 //                           "  brothers....          '" + vdcs);

						AnnotationRecord annRec = new AnnotationRecord();
					
						annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
						annRec.setValue(ww_val.trim());
						
						if 	(	(typfile.procedureTypeHash.get(name) != null) &&
								(typfile.procedureTypeHash.get(name).equals("TimeSubdivision") ||
								        typfile.procedureTypeHash.get(name).equals("IncludedIn"))) {	// alignable annot
								
							annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
							annRec.setReferredAnnotId(par.getAnnotationId());	// used to create and connect timeslots
							
							// NOTE: order of these 3 statements important
							annotRecordToTierMap.put(annRec, name + "@" + spk);
							createAndConnectTimeSlots(annRec);					
							annotationRecords.add(annRec);			
						}
						else {	// ref annotation
							annRec.setAnnotationType(AnnotationRecord.REFERENCE);					
							annRec.setReferredAnnotId(par.getAnnotationId());
													
							// NOTE: order of these 3 statements important
							annotRecordToTierMap.put(annRec, name + "@" + spk);
							fillInPrevAnnotRef(annRec);					
							annotationRecords.add(annRec);
						}

						if (!participantOrder.contains(spk)) {
						    participantOrder.add(spk);
						}
						tierNameSet.add(name + "@" + spk);

 //                       logger.log(Level.INFO, "ww ");
						createChildrenInBlock(annRec, vdcs.iterator(), row,
							xmywordboundaries, ww_wordcount);
						ww_hasMoreWords = ww_wordcount < (xmywordboundaries.size() -
							2);
						ww_wordcount += 1;
					}

					return createChildrenInBlock(par, brothers, row,
						wordboundaries, wordcount);

					//////////////////////////////////////////////
				}
			}

			// snap sentence to word
			val = snapWord(val, mywordboundaries, wordcount, true);
		}

		AnnotationRecord aRec = new AnnotationRecord();
					
		aRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
		aRec.setValue(val.trim());
		
		if 	(	(typfile.procedureTypeHash.get(name) != null) &&
				(typfile.procedureTypeHash.get(name).equals("TimeSubdivision") ||
				        typfile.procedureTypeHash.get(name).equals("IncludedIn"))) {	// alignable annot
	
			aRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
			aRec.setReferredAnnotId(par.getAnnotationId());	// used to create and connect timeslots
					
			// NOTE: order of these 3 statements important
			annotRecordToTierMap.put(aRec, name + "@" + spk);
			createAndConnectTimeSlots(aRec);					
			annotationRecords.add(aRec);			
		
		}
		else {	// ref annot
			aRec.setAnnotationType(AnnotationRecord.REFERENCE);					
			aRec.setReferredAnnotId(par.getAnnotationId());
									
			// NOTE: order of next 3 statements important		
			annotRecordToTierMap.put(aRec, name + "@" + spk);
			fillInPrevAnnotRef(aRec);	
			annotationRecords.add(aRec);
		}

		if (!participantOrder.contains(spk)) {
		    participantOrder.add(spk);
		}
		tierNameSet.add(name + "@" + spk);
		
		// System.out.println("added annot: " + me.getValue());
		if (iHaveKids) {
			createChildrenInBlock(aRec, dcs, row, mywordboundaries, wordcount);
		}

		createChildrenInBlock(par, brothers, row, wordboundaries, wordcount);

		return result;
	}
	
	/**
	 * Simplified method for Lookup or Symbolic Association markers/tiers.
	 * @param par parent record
	 * @param brothers sibling markers
	 * @param row the current row (record)
	 * @return false if there are no more child annotations or siblings to process
	 * @throws Exception
	 */
	private boolean createSymAssociatedChildren(AnnotationRecord par, Iterator<String> brothers,
			int row, String name) throws Exception {
		//if (!brothers.hasMoreElements()) {
			 //  logger.log(Level.INFO, "== ending recursion");

		//	return false;
		//}

		//String name = (String) brothers.nextElement();
		String spk = sbxfile.getSpeaker(row);
		
		String val = sbxfile.getCell(name, row);

		if ((val == null) || (val.length() == 0)) {
			// skip this value, but there might be more brothers
			//return createSymAssociatedChildren(par, brothers, row, (String) brothers.nextElement());
			return createChildrenInBlock(par, brothers, row, wordbounds(val), 0);
		}

		if ((simpleConverter != null) && typfile.isIPAtier(name)) {
			val = simpleConverter.toUnicode(val);
		}
         
		List<String> vdcs = childs(name);
		Iterator<String> dcs = vdcs.iterator();
		boolean iHaveKids = dcs.hasNext();
		
		AnnotationRecord aRec = new AnnotationRecord();
		
		aRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
		aRec.setValue(val.replace('\n', ' ').trim());
		// is a ref annotation
		aRec.setAnnotationType(AnnotationRecord.REFERENCE);					
		aRec.setReferredAnnotId(par.getAnnotationId());
								
		// NOTE: order of next 3 statements important		
		annotRecordToTierMap.put(aRec, name + "@" + spk);
		fillInPrevAnnotRef(aRec);	
		annotationRecords.add(aRec);
		
		if (!participantOrder.contains(spk)) {
		    participantOrder.add(spk);
		}
		tierNameSet.add(name + "@" + spk);
		
		// can a lookup marker have a parse (subdivision) child marker?
		if (iHaveKids) {
			createChildrenInBlock(aRec, dcs, row, wordbounds(val), 0);
		}

		return createChildrenInBlock(par, brothers, row, wordbounds(val), 0);
		
		//return false;
	}
	
	private final List<Integer> wordbounds(String s) {	        
		return wordbounds(s, 0);
	}
	
	/**
	 * Get the wordboundaries from given String. Wordboundaries are the
	 * positions of all white space. If white space is followed by white
	 * space, the last position is used.
	 *
	 * @param s given String
	 * @param offset to add to all wordboundaries
	 *
	 * @return List of wordboundaries
	 */
	private final List<Integer> wordbounds(String s, int offset) {
		//System.out.println(""+val+ "   ---- entry");
		List<Integer> result = new ArrayList<Integer>();
		result.add(Integer.valueOf(offset));

		List<Integer> idx = indicesOf(s.trim(), ' ');
		//ArrayList idx = indexesOf(s, utf8, ' ');

		//System.out.println(""+v1+ "   ---- indexes of");
		idx = lastIntInRow(idx);
		idx = addToAllIntegers(idx, offset + 1);

		//System.out.println(""+v1+ "   ---- last in row");
		result.addAll(idx);
		
		// hb, 2-9-04: added +1 because rest of code assumes space between
		// word beginnings

		// String.getBytes(charset).length can be different from String.length()
		result.add(Integer.valueOf(s.length() + 1 + offset));  
		
		
		// ending on ws
		result = lastIntInRow(result);

		//System.out.println(""+wordboundaries+ "   ---- result");
		return result;
	}
	
	/**
	 * Returna a list with all indices of a certain char.
	 * @param val the string
	 * @param lookingfor the character to find in the string
	 * @return a list of indices
	 */
	private final List<Integer> indicesOf(String val, char lookingfor) {
		List<Integer> result = new ArrayList<Integer>();

		try {
		    char[] chars = new char[val.length()];
			val.getChars(0, val.length(), chars, 0);
			
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == lookingfor) {
				    result.add(Integer.valueOf(i));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Extract a word from the input string, based on word boundaries and index.
	 * 
	 * May 2006: unproper alignment was fixed in previous version in non utf-8 markers.
	 * This distinction is no longer made (see ShoeboxArray): 
	 * @param val the complete line
	 * @param wb the boundary indices
	 * @param wc the index into the boundary list
	 * @param trim whether or not to trim the result
	 * @return the extracted word
	 */
	private final String snapWord(String val, List<Integer> wb, int wc, boolean trim) {
		//logger.log(Level.FINE, "-- snap (" + val+  ", " + wb+  ", " + wc);
		String result = "";
		
		int b = 0;
		int e = 0;
		
		if (wc < wb.size()) {
			b = wb.get(wc).intValue();
		}
		if (wc < wb.size() - 1) {
			e = wb.get(wc + 1).intValue();
		}

		if (val.length() < e) {
			e = val.length();
		}
		
		// HB, 3 nov 04, hack to fix improper shoebox alignment pattern
		// ... woi Bia teri...
		// ... woi     teri...
		// ... mother  across...
		if (fixImproperAlign) {
			if (val.charAt(e - 1) != ' ') {		// take with previous word
				if (wc < wb.size() - 2) {
					e = wb.get(wc + 2).intValue();
				}			
			}
			
			if (val.length() < e) {
				e = val.length();
			}
	
			if (val.length() < b) {
				b = val.length();
			}
			
			if (b > 0 && val.charAt(b - 1) != ' ') {	// ignore, if taken with previous word
				b = e;
			}

		} 
		
		if (b > e) {
		    //System.out.println("Val: " + val);
		    LOG.warning("begin > end: " + b + " - " + e + " value: " + val);
		    e = val.length();
		}
		if (b >= val.length()) {
		    //System.out.println("Error b >= l: " + b + " " + e + " l: " + val.length());
		    //System.out.println("Val: " + val);
		    LOG.warning("begin >= length: " + b + " - " + val.length() + " value: " + val);
		    return result;
		}
		if (e >= val.length()) {
		    //System.out.println("Error e >= l: " + b + " " + e + " l: " + val.length());
		    //System.out.println("Val: " + val);
		    if (e > val.length()) {
		        LOG.warning("end > length: " + e + " - " + val.length() + " value: " + val);
		    }		    
		    result = val.substring(b);
		} else {
		    result = val.substring(b, e);
		}

		if (trim) {
			result = result.trim();
		}

		return result;
	}

	
	/**
	 * DOCUMENT ME!
	 *
	 * @param tname shoebox tier (with or without leading \)
	 *
	 * @return names of direct childs of given tier
	 *
	 * @throws Exception up
	 */
	private final List<String> childs(String tname) throws Exception {
		if (!tname.startsWith("\\")) {
			tname = "\\" + tname;
		}

		List<String> result = new ArrayList<String>();

		//MK:02/06/19 I have to find the tree element by linear search?!
		DefaultMutableTreeNode found = null;
		Enumeration all = tiertree.postorderEnumeration();

		while ((found == null) && all.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) all.nextElement();
			String name = (String) node.getUserObject();

			//logger.log(Level.FINE, " ," + name);
			if ((name != null) && name.equals(tname)) {
				found = node;
			}
		}

		if (found == null) {
			return result;
		}

		Enumeration kids = found.children();

		while (kids.hasMoreElements()) {
			DefaultMutableTreeNode kid = (DefaultMutableTreeNode) kids.nextElement();
			String name = (String) kid.getUserObject();

			if (name == null) {
				continue;
			}

			if (name.startsWith("\\EUDICO")) {
				continue;
			}
			if (name.startsWith("\\ELAN")) {
				continue;
			}
			if (name.startsWith(ShoeboxArray.label_eudicoparticipant)) {
				continue;
			}

			result.add(name);
		}

		return result;
	} 
	

	/**
	 * MK:02/06/10<br> For a single row, create the RefAnno for this dad. If
	 * there are no depended Annos, ignore the word-counter, otherwise, use
	 * the wordcounter and create all children Annos.
	 *
	 * @param dad the annotation above me
	 * @param brothers tiers on the same level of myself
	 * @param row Sbxfile row
	 * @param wordboundaries list of row-character count from file
	 * @param wordcount actual row-character count from file
	 *
	 * @return true if there are more words in the given wordboundaries
	 *
	 * @throws Exception DOCUMENT ME!
	 */

	//private static int ccib = 0;
	private boolean createChildsInBlock(AnnotationRecord dad, Iterator<String> brothers,
		int row, List<Integer> wordboundaries, int wordcount)
		throws Exception {
		String dadtierName = annotRecordToTierMap.get(dad);
		boolean result = true;

		if (!brothers.hasNext()) {
 //           logger.log(Level.INFO, "== ending recursion");

			return false;
		}

		String name = brothers.next();
		String spk = sbxfile.getSpeaker(row);
//		  logger.log(Level.INFO,
//			  "==  createChildsInBlock(" + dad.getValue() + ", '" + name + "', " +
//			  row + ", " + wordboundaries + ", " + wordcount);

		List<Integer> mywordboundaries = null;
		String val = sbxfile.getCell(name, row);
		/*
		if (typfile.isUnicodeTier(name)) {
		    byte[] bytes = val.getBytes("UTF-8");
		    //System.out.println("ol: " + val.length());
		    val = new String(bytes, "UTF-8");
		    //System.out.println("by: " + bytes.length + " nl: " + val.length());
		}
		*/

		if ((val == null) || (val.length() == 0)) {
			// skip this value, but there might be more brothers
			return createChildsInBlock(dad, brothers, row, wordboundaries,
				wordcount);
		}

		if ((simpleConverter != null) && typfile.isIPAtier(name)) {
			val = simpleConverter.toUnicode(val);
		}
         
		List<String> vdcs = childs(name);
		Iterator<String> dcs = vdcs.iterator();
		boolean iHaveKids = dcs.hasNext();

		if ((wordboundaries == null) && !iHaveKids) {
			// append only once
			if (wordcount > 0) {
				return false;
			}

			//	logger.log(Level.INFO, "== ("+val.substring(0, 8)+") is hanging childless under \\ref ");
//			  logger.log(Level.INFO,
 //               "== (" + val + ") is hanging childless under \\ref ");
		} else {
			// tier is not under ref, kids or not
			// use the wordcounter
			if (wordboundaries == null) {
				// ref is my dad and I have kids, I set the wordboundaries and the wordcounter.
				mywordboundaries = wbound(val, typfile.isUnicodeTier(name));
				
				result = wordcount < (mywordboundaries.size() - 2);
			} else {
				// my dad set some wordboundaries
				// I have to get the right word
				mywordboundaries = wordboundaries;

				// If I have (inner )wordboundaries myself,
				// I have to ignore dads bounds and set a new ones.
				String xval = snap(val, mywordboundaries, wordcount, true, typfile.isUnicodeTier(name));
				int index = mywordboundaries.get(wordcount).intValue();
				
				// pad xval with spaces until endIndex (hb, 3 sept 04)
				int endIndex = mywordboundaries.get(wordcount+1).intValue();
				
				int xvalLength = xval.length();
				for (int i = 0; i < (endIndex - index - xvalLength - 1); i++) {
					xval += " ";
				}			
				
				List<Integer> xmywordboundaries = wbound(xval, typfile.isUnicodeTier(name), index);
//				  logger.log(Level.INFO,
//					  "             (" + xval + ") " + index + "/" +
 //                   xmywordboundaries);

				if (xmywordboundaries.size() > 2) {
					/////////////////////////////////
					//  recursion over trees within words (bern -e)
					int ww_wordcount = 0;
					boolean ww_hasMoreWords = true;

					while (ww_hasMoreWords) {
						String ww_val = snap(val, xmywordboundaries,
								ww_wordcount, true, typfile.isUnicodeTier(name));
 //                       logger.log(Level.INFO,
 //                           " ...........            '" + ww_val + "', of " +
 //                           ww_wordcount);
 //                       logger.log(Level.INFO,
 //                           "  brothers....          '" + vdcs);

						AnnotationRecord annRec = new AnnotationRecord();
					
						annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
						annRec.setValue(ww_val.trim());
						
						if 	(	(typfile.procedureTypeHash.get(name) != null) &&
								(typfile.procedureTypeHash.get(name).equals("TimeSubdivision") ||
								 typfile.procedureTypeHash.get(name).equals("IncludedIn")     )) {	// alignable annot
								
							annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
							annRec.setReferredAnnotId(dad.getAnnotationId());	// used to create and connect timeslots
							
							// NOTE: order of these 3 statements important
							annotRecordToTierMap.put(annRec, name + "@" + spk);
							createAndConnectTimeSlots(annRec);					
							annotationRecords.add(annRec);			
						}
						else {	// ref annotation
							annRec.setAnnotationType(AnnotationRecord.REFERENCE);					
							annRec.setReferredAnnotId(dad.getAnnotationId());
													
							// NOTE: order of these 3 statements important
							annotRecordToTierMap.put(annRec, name + "@" + spk);
							fillInPrevAnnotRef(annRec);					
							annotationRecords.add(annRec);
						}

						if (!participantOrder.contains(spk)) {
						    participantOrder.add(spk);
						}
						tierNameSet.add(name + "@" + spk);

 //                       logger.log(Level.INFO, "ww ");
						createChildsInBlock(annRec, vdcs.iterator(), row,
							xmywordboundaries, ww_wordcount);
						ww_hasMoreWords = ww_wordcount < (xmywordboundaries.size() -
							2);
						ww_wordcount += 1;
					}

					return createChildsInBlock(dad, brothers, row,
						wordboundaries, wordcount);

					//////////////////////////////////////////////
				}
			}

			// snap sentence to word
			val = snap(val, mywordboundaries, wordcount, true, typfile.isUnicodeTier(name));
		}

		AnnotationRecord aRec = new AnnotationRecord();
					
		aRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
		aRec.setValue(val.trim());
		
		if 	(	(typfile.procedureTypeHash.get(name) != null) &&
				(typfile.procedureTypeHash.get(name).equals("TimeSubdivision") ||
				        typfile.procedureTypeHash.get(name).equals("IncludedIn") )) {	// alignable annot
	
			aRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
			aRec.setReferredAnnotId(dad.getAnnotationId());	// used to create and connect timeslots
					
			// NOTE: order of these 3 statements important
			annotRecordToTierMap.put(aRec, name + "@" + spk);
			createAndConnectTimeSlots(aRec);					
			annotationRecords.add(aRec);			
		
		}
		else {	// ref annot
			aRec.setAnnotationType(AnnotationRecord.REFERENCE);					
			aRec.setReferredAnnotId(dad.getAnnotationId());
									
			// NOTE: order of next 3 statements important		
			annotRecordToTierMap.put(aRec, name + "@" + spk);
			fillInPrevAnnotRef(aRec);	
			annotationRecords.add(aRec);
		}

		if (!participantOrder.contains(spk)) {
		    participantOrder.add(spk);
		}
		tierNameSet.add(name + "@" + spk);
		
		// System.out.println("added annot: " + me.getValue());
		if (iHaveKids) {
			createChildsInBlock(aRec, dcs, row, mywordboundaries, wordcount);
		}

		createChildsInBlock(dad, brothers, row, wordboundaries, wordcount);

		return result;
	}
	
	private void fillInPrevAnnotRef(AnnotationRecord annRec) {
		long highestIndex = -1;
		
		String onTier = annotRecordToTierMap.get(annRec);
		
		Iterator<AnnotationRecord> annIter = annotationRecords.iterator();
		while (annIter.hasNext()) {
			AnnotationRecord aRec = annIter.next();
			
			if (	(annotRecordToTierMap.get(aRec).equals(onTier)) &&		// on same tier
					(aRec.getReferredAnnotId().equals(annRec.getReferredAnnotId()))	){	// with same parent annot
				// remember the annot id with the highest index part
				String idString = aRec.getAnnotationId();
				long annotIndex = Long.valueOf(idString.substring(ANN_ID_PREFIX.length())).longValue();	// get rid of prefix part

				if (annotIndex > highestIndex) {
					highestIndex = annotIndex;
				}
			}
		}
		if (highestIndex >= 0) {
			annRec.setPreviousAnnotId(ANN_ID_PREFIX + highestIndex);
		}
	}
	
	private void createAndConnectTimeSlots(AnnotationRecord annRec) {
		long highestIndex = -1;
		AnnotationRecord highestAnnot = null;
		AnnotationRecord parentRec = null;
		AnnotationRecord aRec = null;
		
		String onTier = annotRecordToTierMap.get(annRec);
		
		Iterator<AnnotationRecord> annIter = annotationRecords.iterator();
		while (annIter.hasNext()) {
			aRec = annIter.next();
			
			if (parentRec == null && annRec.getReferredAnnotId().equals(aRec.getAnnotationId())) {
				// store parent annotation record for later use
				parentRec = aRec;
			}
			
			if (	(annotRecordToTierMap.get(aRec).equals(onTier)) &&		// on same tier
					(aRec.getReferredAnnotId().equals(annRec.getReferredAnnotId()))	){	// with same parent annot
				// remember the annot id with the highest index part
				String idString = aRec.getAnnotationId();
				long annotIndex = Long.valueOf(idString.substring(ANN_ID_PREFIX.length())).longValue();	// get rid of prefix part

				if (annotIndex > highestIndex) {
					highestIndex = annotIndex;
					highestAnnot = aRec;
				}
			}
		}
		if (highestIndex >= 0) {
			// make new begin timeslot
			// set end of highest existing annot to this begin
			
			long beginTSId = tsId++;
			
			annRec.setBeginTimeSlotId(TS_ID_PREFIX + Long.toString(beginTSId));
			String oldEndTSId = highestAnnot.getEndTimeSlotId();
			highestAnnot.setEndTimeSlotId(TS_ID_PREFIX + Long.toString(beginTSId));
			updateChildAnnot(highestAnnot, oldEndTSId);
										
			// store timeslots
			long[] begin = {beginTSId, TimeSlot.TIME_UNALIGNED};		
			timeSlots.add(begin);
			
			// find index of aRec's begin ts in timeOrder	
			String beginId = highestAnnot.getBeginTimeSlotId();
			String beginNo = "";
			
			int index = timeOrder.size();
			
			if (beginId != null) {
			 	beginNo = highestAnnot.getBeginTimeSlotId().substring(TS_ID_PREFIX.length());

				for (int i = 0; i < timeOrder.size(); i++) {
					long[] ts = timeOrder.get(i);
					if (ts[0] == Integer.valueOf(beginNo).intValue()) {
						index = i;
						break;
					}
				}
			}	
			if (index > timeOrder.size()-1) {
				timeOrder.add(begin);
			}
			else {
				timeOrder.add(index+1, begin);
			}
		}
		else {	// first, connect to parent begin
			if (parentRec != null) {
				annRec.setBeginTimeSlotId(parentRec.getBeginTimeSlotId());
			}
		}
		
		//	set end to end of parent
		if (parentRec != null) {		
			annRec.setEndTimeSlotId(parentRec.getEndTimeSlotId());
		}
	}

	/**
	 * Recursively update the end timeslot id of annotations referring to this annotation referring to the same 
	 * old end time slot.
	 * 
	 * @param par the annotatiom that has been modified
	 * @param oldEndTSId the old timeslot id
	 */
	private void updateChildAnnot(AnnotationRecord par, String oldEndTSId) {
		AnnotationRecord aRec = null;
		
		Iterator<AnnotationRecord> annIter = annotationRecords.iterator();
		while (annIter.hasNext()) {
			aRec = annIter.next();
			
			if (aRec.getReferredAnnotId() == par.getAnnotationId() && 
			        aRec.getEndTimeSlotId() == oldEndTSId) {
			    aRec.setEndTimeSlotId(par.getEndTimeSlotId());
			    updateChildAnnot(aRec, oldEndTSId);
			    return;
			}
		}
	}
	
	/**
	 * Word boundaries without offset
	 *
	 * @param s given String
	 *
	 * @return index of last of contiguous whitespace
	 */
	private final List<Integer> wbound(String s, boolean utf8) {	        
		return wbound(s, utf8, 0);
	}

	/**
	 * Get the wordboundaries from given String. Wordboundaries are the
	 * positions of all white space. If white space is followed by white
	 * space, the last position is used.
	 *
	 * @param s given String
	 * @param offset to add to all wordboundaries
	 *
	 * @return ArrayList of wordboundaries
	 */
	private final List<Integer> wbound(String s, boolean utf8, int offset) {
		//System.out.println(""+val+ "   ---- entry");
		List<Integer> result = new ArrayList<Integer>();
		result.add(Integer.valueOf(offset));

		List<Integer> idx = indexesOf(s.trim(), utf8, ' ');
		//ArrayList idx = indexesOf(s, utf8, ' ');

		//System.out.println(""+v1+ "   ---- indexes of");
		idx = lastIntInRow(idx);
		idx = addToAllIntegers(idx, offset + 1);

		//System.out.println(""+v1+ "   ---- last in row");
		result.addAll(idx);
		
		// hb, 2-9-04: added +1 because rest of code assumes space between
		// word beginnings
/*		Integer lastBound = (Integer) result.lastElement();
		if (lastBound.intValue() == s.length() + offset - 1) {
			result.remove(lastBound);
			result.add(Integer.valueOf(s.length() + offset - 2));
		}*/
		// this is now done in indexesOf, because the values are based on bytes positions
		// String.getBytes(charset).length can be different from String.length()
		//if (!utf8) {
		    result.add(Integer.valueOf(s.length() + 1 + offset));  
		//}
		
		
		// ending on ws
		result = lastIntInRow(result);

		//System.out.println(""+wordboundaries+ "   ---- result");
		return result;
	}

	/**
	 * Returns the list of all integers i where myself.indexOf(i) is true.
	 *
	 * @param myself 'this' String
	 * @param lookingfor the String you look for
	 *
	 * @return List of Integers.
	 */
	private final static List<Integer> indexesOf(String myself, String lookingfor) {
		List<Integer> result = new ArrayList<Integer>();
		int i = myself.indexOf(lookingfor, 0);

		while (i != -1) {
			result.add(Integer.valueOf(i));
			i = myself.indexOf(lookingfor, i + 1);
		}

		return result;
	}

	private final static List<Integer> indexesOf(String myself, boolean utf8, char lookingfor) {
		List<Integer> result = new ArrayList<Integer>();

		try {
			// bytes are needed for proper counting, since alignment
			// is on basis of bytes.
			byte[] bytes = null;
			if (utf8) {
				bytes = myself.getBytes("UTF-8");
			} 
			else {
				bytes = myself.getBytes("ISO-8859-1");
			}
	
			for (int i = 0; i < bytes.length; i++) {
				if (bytes[i] == lookingfor) {
					result.add(Integer.valueOf(i));
				}
			}
			// temp add the las index if it is not yet in there
			//if (utf8 && result.size() > 0 && ((Integer)result.get(result.size() - 1)).intValue() != bytes.length - 1) {
			//    result.add(Integer.valueOf(bytes.length - 1));    
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param in List of ordered Integers
	 *
	 * @return List of ordered Integers, only last in row
	 */
	private final static List<Integer> lastIntInRow(List<Integer> in) {
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> it = in.iterator();
		int last = 0;
		Integer Last = null;

		if (it.hasNext()) {
			Last = it.next();
			last = Last.intValue();
		}

		while (it.hasNext()) {
			Integer I = it.next();
			int i = I.intValue();

			//System.out.println("last="+ last + " i="+ i);
			if ((last + 1) == i) {
				Last = I;
				last = i;
			} else {
				result.add(Last);
				Last = I;
				last = i;
			}
		}

		if (Last != null) {
			result.add(Last);
		}

		return result;
	}

	/**
	 * Add the offset to all Integers in List.
	 *
	 * @param myself 'this' List of Integers
	 * @param offset the offset you want to add
	 *
	 * @return List of Integers
	 */
	private final static List<Integer> addToAllIntegers(List<Integer> myself, int offset) {
		List<Integer> result = new ArrayList<Integer>(myself.size());

		for (Integer e : myself) {
			result.add(Integer.valueOf(e + offset));
		}

		return result;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param val DOCUMENT ME!
	 * @param wb DOCUMENT ME!
	 * @param wc DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private final String snap(String val, List<Integer> wb, int wc, boolean trim, boolean utf8) {
		//logger.log(Level.FINE, "-- snap (" + val+  ", " + wb+  ", " + wc);

		String result = "";
		
		int b = 0;
		int e = 0;
		
		if (wc < wb.size()) {
			b = wb.get(wc).intValue();
		}
		if (wc < wb.size() - 1) {
			e = wb.get(wc + 1).intValue();
		}

		if (!utf8 && val.length() < e) {
			e = val.length();
		}
		
		// HB, 3 nov 04, hack to fix improper shoebox alignment pattern
		// ... woi Bia teri...
		// ... woi     teri...
		// ... mother  across...
		if (!utf8) {
			if (val.charAt(e - 1) != ' ') {		// take with previous word
				if (wc < wb.size() - 2) {
					e = wb.get(wc + 2).intValue();
				}			
			}
			
			if (val.length() < e) {
				e = val.length();
			}
	
			if (val.length() < b) {
				b = val.length();
			}
			
			if (b > 0 && val.charAt(b - 1) != ' ') {	// ignore, if taken with previous word
				b = e;
			}

		} 
		
		// toolbox stores interlinearization on basis of byte position
		// This causes a problem in case of UTF-8 encodings of more than
		// 1 byte.
		// Correct b and e to fix this
		if (utf8) {
			char[] chars = new char[val.length()];
			val.getChars(0, val.length(), chars, 0);
			
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				
				if ((ch == '\u0000') ||
					(ch >= '\u0080' && ch <= '\u07ff')) {	// 2 bytes
						
					if (i < b) {
						b--;
					}
					if (i <= e) {
						e--;
					}	
				}
				else if ((ch >= '\u0800') && (ch <= '\uffff')) {	// 3 bytes
					if (i < b) {
						b-=2;
					}
					if (i <= e) {
						e-=2;
					}					
				}
				if (i > e) {
					break;
				}
			}
		}
		if (b > e) {
		    System.out.println("Val: " + val);
		    System.out.println("b > e: " + b + " - " + e + " l: " + val.length());
		    e = val.length();
		    //result = val.substring(b);
		    //return result;
		}
		if (b >= val.length()) {
		    //System.out.println("Error b >= l: " + b + " " + e + " l: " + val.length());
		    //System.out.println("Val: " + val);
		    return result;
		}
		if (e >= val.length()) {
		    //System.out.println("Error e >= l: " + b + " " + e + " l: " + val.length());
		    //System.out.println("Val: " + val);
		    result = val.substring(b);
		} else {
		    result = val.substring(b, e);
		}
	//	if (e == val.length()-1) {	// hb, 2-9-04: when no trailing space. Rest of code assumes this.
	//		result = val.substring(b);
	//	}
	//	else {
	//		result = val.substring(b, e);
	//	}
		if (trim) {
			result = result.trim();
		}

		return result;
	}
	
	/**
	 * Loops over the list of root slots and finds intervals (size >= 1) of consecutive 
	 * unaligned slots (time == -1). Begin and end index of such interval are passed
	 * to calculateSlotsInInterval(), where appropriate times for each unaligned slot is 
	 * calculated/interpolated.
	 * The list of slots contains alternating beginslot and endslot objects, 
	 * even index is beginslot, odd index is endslot.
	 * 
	 * @see #calculateSlotsInInterval(int, int)
	 */
	private void calculateRootTimes() {
		long[] slot;
		int firstUAIndex = -1;
		
		for (int i = 0; i < rootSlots.size(); i++) {
			slot = rootSlots.get(i);
			if (slot[1] == -1) {
				if (firstUAIndex == -1) {
					firstUAIndex = i;
				} 
			} else {
				if (firstUAIndex != -1) {
				// unaligned has already been found
				calculateSlotsInInterval(firstUAIndex, i - 1);
					
				firstUAIndex = -1;
				}
			}
			
			if (i == rootSlots.size() - 1 && firstUAIndex != -1) {
				calculateSlotsInInterval(firstUAIndex, i);
			}
			//System.out.println("" + i + ": " + slot[1]);
		}
	}
	
	/**
	 * Calculates (interpolates) time values for a number of unaligned slots.
	 * There are some special cases:<br>
	 * - only one unaligned begin slot: set time to that of previous end slot
	 * - only one unaligned end slot: set time to begintime of next slot (or 
	 * to previous beginslot time + preferred block duration, whichever is smaller)
	 * - all unaligned (except first): make all intervals the preferred block duration
	 * - more than one unaligned slots: calculate total available time for the slots
	 * (next aligned slot value - previous aligned slot value), calculate the number of
	 * intervals that are involved and calculate the amount of ms for each 'annotation'
	 * (delta = ms / numIntervals). If this amount is greater than the pref. block 
	 * duration, use this duration instead. (This will leave a gap or the last annotation
	 * will have a longer duration.   
	 * 
	 * @param firstUAIndex the index of the first unaligned slot in a serie 
	 * @param lastUAIndex the index of the last unaligned slot in a serie 
	 */
	private void calculateSlotsInInterval(int firstUAIndex, int lastUAIndex) {
		long[] slot;
		long[] otherSlot;
		
		// special cases, just one slot
		if (firstUAIndex == lastUAIndex) {
			if (firstUAIndex % 2 == 0) {
				//begin time slot, set to the time of previous end slot
				otherSlot = rootSlots.get(firstUAIndex - 1);
				slot = rootSlots.get(firstUAIndex);
				slot[1] = otherSlot[1];
			} else {
				// an end time slot
				if (lastUAIndex < rootSlots.size() - 1) {
					otherSlot = rootSlots.get(lastUAIndex + 1);
					long nextVal = otherSlot[1];
					slot = rootSlots.get(lastUAIndex);
					// always connect to the next begin, or limit to preferred duration?
					otherSlot = rootSlots.get(lastUAIndex - 1);
					if (nextVal - otherSlot[1] > preferredBlockDuration) {
						nextVal = otherSlot[1] + preferredBlockDuration;
					}
					//
					slot[1] = nextVal;
				} else {
					otherSlot = rootSlots.get(lastUAIndex - 1);
					slot = rootSlots.get(lastUAIndex);
					slot[1] = otherSlot[1] + preferredBlockDuration;
				}
			}
			return;
		}
		// all unaligned
		if (firstUAIndex == 1 && lastUAIndex == rootSlots.size() - 1) {
			for (int i = 1; i <= lastUAIndex; i++) {
				slot = rootSlots.get(i);							
				slot[1] = (long) Math.ceil((float)i / 2) * preferredBlockDuration;
			}
			return;
		}
		// mix, interval at the end
		if (lastUAIndex == rootSlots.size() - 1) {
			slot = rootSlots.get(firstUAIndex - 1);
			long startTime = slot[1];
			// additional counter
			int j = firstUAIndex % 2 == 0 ? 1 : 2;
			for (int i = firstUAIndex; i <= lastUAIndex; i++, j++) {
				slot = rootSlots.get(i);							
				slot[1] = startTime + (j / 2) * preferredBlockDuration;
			}
		} else {
			// interval anywhere
			int numIntervals = 0;
			long delta = 0;
			// calculate the number of involved 'annotations'
			int begin = firstUAIndex % 2 == 0 ? firstUAIndex + 1 : firstUAIndex;
			int end = lastUAIndex % 2 == 0 ? lastUAIndex + 1 : lastUAIndex;
			numIntervals = (end - begin) / 2 + 1;
			//System.out.println("num intervals: " + numIntervals);
			
			// find left and right aligned values
			slot = rootSlots.get(firstUAIndex - 1);
			otherSlot = rootSlots.get(lastUAIndex + 1);
			long startTime = slot[1];
			long endTime = otherSlot[1];
			delta = (endTime - startTime) / numIntervals;
			//System.out.println("Time per Interval: " + delta);
			
			if (delta > preferredBlockDuration) {
				delta = preferredBlockDuration;
				//System.out.println("Interval decreased to: " + delta);
			}
			
			// loop over the unaligned slots
			int j = firstUAIndex % 2 == 0 ? 1 : 2;
			for (int i = firstUAIndex; i <= lastUAIndex; i++, j++) {
				slot = rootSlots.get(i);
				slot[1] = startTime + (j / 2) * delta;
			}
		}
		
	}
	
    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#setDecoderInfo(mpi.eudico.server.corpora.clom.DecoderInfo)
     */
    @Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
        if (decoderInfo instanceof ToolboxDecoderInfo) {
            this.decoderInfo = (ToolboxDecoderInfo) decoderInfo;
            preferredBlockDuration = (int) this.decoderInfo.getBlockDuration();
        }
    }
}
