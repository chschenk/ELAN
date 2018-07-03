package mpi.eudico.client.annotator.recognizer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.data.SelectionComparator;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.MutableInt;
import mpi.eudico.util.TimeFormatter;

/**
 * A class for writing segments/selections/tiers to the AVATecH 
 * XML Tier format or AVATecH CSV format.
 * 
 * @author Han Sloetjes
 */
public class RecTierWriter {
	
	private boolean writeInNewFormat = false;
	private final String SC = ";";
	
	/**
	 * Constructor
	 */
	public RecTierWriter() {
		super();
	}
	
	/**
	 * Sets the writer to the new xml tier format
	 * 
	 * @param newFormat if true writes in new format
	 */
	public void setNewTierFormat(boolean newFormat){
		writeInNewFormat = newFormat;
	}

	/**
	 * Writes the specified selections to an xml file. Assumes there is one "tier", 
	 * or one column in the columns attribute. 
	 * Note: currently no check are done on overlaps in segments! 
	 * 
	 * @param outputFile the destination file
	 * @param segments the segments/selections, in ascending time order
	 */
	public void write(File outputFile, List<RSelection> segments) throws IOException {
		if (outputFile == null) {
			new IOException("Cannot write to file: file is null");
		}
		
		String outName = outputFile.getName().toLowerCase();
		boolean xmlOut = !(outName.endsWith("csv") || outName.endsWith("txt"));
		
		/*
		discover the number of columns based on repetition of the same time values?
		create a list of occurrences per combination of begin time - end time and
		either take the maximum count as number of columns or infer based on distribution
		the optimal number of columns padding in some cases ignoring selections in others 
		*/
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8")));// utf-8 is always supported, I guess
		if (xmlOut) {
			if(writeInNewFormat){
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.print("<TIERS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
				writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tiers.xsd\">");
				writer.println("<TIER ");				
			}else{
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.print("<TIER xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
				writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tier.xsd\" ");				
			}
			
			writer.println("columns=\"tier_1\">");
			
		} else {
			writer.println("\"#starttime\";\"#endtime\";" + "\"tier_1\"");
		}
		
		if (segments != null && segments.size() > 0) {		
			// resolve overlaps??
			for (int i = 0; i < segments.size(); i++) {				
				writeSpan(writer, segments.get(i), xmlOut);				
			}
		}
		
		if (xmlOut) {			
			writer.println("</TIER>");
			if(writeInNewFormat){
				writer.println("</TIERS>");
			}
		} 
		
		writer.close();
	}
	
	/**
	 * Writes the specified segmentations (tiers) to an xml/text file. 
	 * 
	 * @param outputFile the destination file
	 * @param segments the segments in ascending time order
	 * @param trans the transcription
	 */
	public void write(File outputFile, List<Segmentation> segments, Transcription trans) throws IOException {
		if (outputFile == null) {
			new IOException("Cannot write to file: file is null");
		}
	
		//also check if the segments is not null and has one tier
		if(segments != null && segments.size() > 0){
			new IOException("No Sements available.");
		}
		
		String outName = outputFile.getName().toLowerCase();
		boolean xmlOut = !(outName.endsWith("csv") || outName.endsWith("txt"));		
		
		if(!xmlOut || !writeInNewFormat){
			writeInOldFormat(outputFile, segments, xmlOut);
		} else{
			//new format
			writeInNewFormat(outputFile, segments, trans);
		}
	}
	
	/**
	 * write span
	 */
	private void writeSpan(PrintWriter writer, RSelection sel, boolean xmlOut){
		if(xmlOut){
			writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(sel.beginTime) + "\" ");
			writer.print("end=\"" + TimeFormatter.toSSMSString(sel.endTime) + "\">");
			writer.print("<v>");
			if (sel instanceof Segment && ((Segment)sel).label != null) {
				writer.print(((Segment)sel).label);
			} 
			writer.print("</v>");
			writer.println("</span>");
		}else{
			writer.print(TimeFormatter.toSSMSString(sel.beginTime) + SC);
			writer.print(TimeFormatter.toSSMSString(sel.endTime) + SC);
			if (sel instanceof Segment && ((Segment)sel).label != null) {
				writer.print("\"" + ((Segment)sel).label.trim() + "\"");
				writer.println();
				
			} else {
				writer.println();
			}
		}
	}
	
	
	
	/**
	 * 
	 * 
	 * @param outputFile
	 * @param tiers
	 * @param trans
	 * @throws IOException
	 */
	private void writeInNewFormat(File outputFile, List<Segmentation> tiers, Transcription trans) throws IOException{	
		int numTiers = tiers.size();
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8")));
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.print("<TIERS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		writer.println("xsi:noNamespaceSchemaLocation=\"file:avatech-tiers.xsd\"> ");
		
		if(numTiers > 1){
			Map<String, List<String>> tierMap = new HashMap<String, List<String>>();
			Map<String, List<RSelection>> selMap = new HashMap<String, List<RSelection>>();
			Map<String, Map<RSelection, List<String>>> spanValueMap = new HashMap<String, Map<RSelection, List<String>>>();			
						
			List<String> parentTiers = new ArrayList<String>();
			TierImpl tier = null;
			
			for(int i=0; i < tiers.size(); i++){
				//load the tier map
				tier = (TierImpl) trans.getTierWithId(tiers.get(i).getName());
				String parentName;
				if(tier == null){
					continue;
				}				
				
				if(tier.getLinguisticType().getConstraints() != null && tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){ 
		 			TierImpl parentTier = (TierImpl) tier.getParentTier();
					while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
						parentTier = (TierImpl) parentTier.getParentTier();
					}
					
					parentName = parentTier.getName();
		 		} else{
		 			parentName = tier.getName();
		 		}				
				
				if(!parentTiers.contains(parentName)){
					parentTiers.add(parentName);
					tierMap.put(parentName, new ArrayList<String>());	
					selMap.put(parentName, new ArrayList<RSelection>());	
					spanValueMap.put(parentName, new HashMap<RSelection, List<String>>());
				} 
				
				//add current tier to the map
				tierMap.get(parentName).add(tier.getName());
				
				// load the RSelections && values
				List<String> values;
				List<RSelection >selList = selMap.get(parentName);					
				Map<RSelection, List<String>> spanMap = spanValueMap.get(parentName);	
				List<RSelection> tempList = tiers.get(i).getSegments();	
			
				//check with the current selection list in the map
				for(RSelection sel : selList){
					values = spanMap.get(sel);
					if(tempList.contains(sel)){
						int index = tempList.indexOf(sel);
						values.add(((Segment)tempList.get(index)).label);
						tempList.remove(index);
					} else {
						values.add(null);
					}
				}
				
				//add all the rest of new selections
				for(RSelection sel : tempList){
					selList.add(sel);
					values = new ArrayList<String>();
					int n = 0;
					while(n < (tierMap.get(parentName).size()-1)){
						values.add(null);
						n++;
					}
					values.add(((Segment)sel).label);
					spanMap.put(sel, values);
				}
			}
			
			
			// write file
			Iterator it = tierMap.entrySet().iterator();
			Entry<String, List<String>> entry;
			List<String> tierList;
			List<RSelection> selList;
			List<String> values;
			String parentName;	
			
			it = tierMap.entrySet().iterator();
			Map<RSelection, List<String>> spanMap;
			SelectionComparator comparator = new SelectionComparator();
			
			while(it.hasNext()){
				entry = (Entry<String, List<String>>) it.next();
				parentName = entry.getKey();
				tierList = entry.getValue();
				int numColumns = tierList.size();
				
				writer.print("\t<TIER columns=\"");
				for(String t : tierList){
					writer.print(t.replaceAll(" ", "_"));				
					if (numColumns > 1) {
						writer.print(" ");
					}
				}
				writer.println("\">");
				
				// write the span elements
				selList = selMap.get(parentName);
				Collections.sort(selList, comparator);
				spanMap = spanValueMap.get(parentName);
				for(RSelection sel : selList){					
					values = spanMap.get(sel);
					if(values != null){
						writer.print("\t\t<span start=\"" + TimeFormatter.toSSMSString(sel.beginTime) + "\" ");
						writer.print("end=\"" + TimeFormatter.toSSMSString(sel.endTime) + "\">");
						for(String val: values){
							writer.print("<v>");
							if (val != null) {
								writer.print(val);
							} 
							writer.print("</v>");
						}
						
						for(int x = values.size(); x < tierList.size(); x++){
							writer.print("<v></v>");
						}
						writer.println("</span>");
					}
				}
				writer.println("\t</TIER>");				
			}	
		} else {			
			writer.println("\t<TIER columns=\"" + tiers.get(0).getName() + "\">");
			for(RSelection sel : tiers.get(0).getSegments()){
				writer.print("\t\t<span start=\"" + TimeFormatter.toSSMSString(sel.beginTime) + "\" ");
				writer.print("end=\"" + TimeFormatter.toSSMSString(sel.endTime) + "\">");
				writer.print("<v>");
				if (sel instanceof Segment && ((Segment)sel).label != null) {
					writer.print(((Segment)sel).label);
				} 
				writer.print("</v>");
				writer.println("</span>");
			}
			writer.println("\t</TIER>");	
		}
		writer.print("</TIERS>");
		writer.close();
	}
	
	/**
	 * 
	 * 
	 * @param outputFile
	 * @param tiers
	 * @param xmlOut
	 * @throws IOException
	 */
	private void writeInOldFormat(File outputFile, List<Segmentation> tiers, boolean xmlOut) throws IOException{			
		// tiers should not be null 
		int numTiers = 0;
		int numColumns = 1;		
		Map<RSelection, MutableInt> selMap = null;
			
		numTiers = tiers.size();
		
		if (numTiers > 1) {			
			// if more tiers			
			selMap = new HashMap<RSelection, MutableInt>();
			Segmentation seg = tiers.get(0);
			for (RSelection sel : seg.getSegments()) {
				selMap.put(sel, new MutableInt(1));
			}
			MutableInt val;
			RSelection key;
			for (int i = 1; i < tiers.size(); i++) {
				seg = tiers.get(i);
				for (RSelection sel : seg.getSegments()) {
					Iterator<RSelection> keyIt = selMap.keySet().iterator();
					boolean found = false;
					while (keyIt.hasNext()) {
						key = keyIt.next();
						if (key.beginTime == sel.beginTime && key.endTime == sel.endTime) {
							found = true;
							selMap.get(key).intValue++;
							break;
						}
					}
					if (!found) {
						selMap.put(sel, new MutableInt(1));
					}
				}
			}
			int numKeys = selMap.size();
			int numOccur = 0;
			Iterator<MutableInt> iter = selMap.values().iterator();
			while (iter.hasNext()) {
				numOccur += iter.next().intValue;
			}
			
			// if more than 50%? of the segments occur in all tiers (on average) treat them as columns
			if (numKeys > 0 && (numOccur / (float) numKeys) >= 1.5) {
				numColumns = numTiers;
			}
		} 
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "UTF-8")));
				
		if (xmlOut) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.print("<TIER xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tier.xsd\" ");
			writer.print("columns=\"");
		} else {
			writer.print("\"#starttime\";\"#endtime\";");
		}
		
		// one column
		if(numColumns == 1){
			if (xmlOut) {
				if (tiers.size() > 0) {
					writer.println("all_tiers\">");
				} else {
					writer.print(tiers.get(0).getName().replaceAll(" ", "_"));
					writer.println("\">");
				}
			} else {
				if (tiers.size() > 0) {					
					writer.println("\"all_tiers\"");
				} else {
					writer.print("\"" + tiers.get(0).getName() + "\"");
					writer.println();
				}
			}
			
			// write the spans
			List<RSelection> segs = new ArrayList<RSelection>();
			for (Segmentation s : tiers) {
				segs.addAll(s.getSegments());
			}			
			Collections.sort(segs, new SelectionComparator());
			
			// resolve overlaps??
			for (int i = 0; i < segs.size(); i++) {							
				writeSpan(writer, segs.get(i), xmlOut);
			}
		} else if (numColumns > 1) {
			// write column names
			for (int i = 0; i < numColumns; i++) {
				if (xmlOut) {
					writer.print(tiers.get(i).getName().replaceAll(" ", "_"));
				} else {
					writer.print("\"" + tiers.get(i).getName() + "\"");
				}
				if (i != numColumns - 1) {
					if (xmlOut) {
						writer.print(" ");
					} else {
						writer.print(SC);
					}
				}
			}
			
			if (xmlOut) {
				writer.println("\">");
			} else {
				writer.println();
			}
			
			// the selMap should not be null
			List<RSelection> segs = new ArrayList<RSelection>(selMap.size());
			segs.addAll(selMap.keySet());
			
			Collections.sort(segs, new SelectionComparator());
			
			int[] counters = new int[numTiers];
			// explicitly set to 0?
			Arrays.fill(counters, 0);
			
			RSelection iter;
			RSelection curSel;
			Segmentation curTier;
			List<RSelection> curSelList;

			for (int i = 0; i < segs.size(); i++) {
				iter = segs.get(i);
				if (xmlOut) {
					writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(iter.beginTime) + "\" ");
					writer.print("end=\"" + TimeFormatter.toSSMSString(iter.endTime) + "\">");
				} else {
					writer.print(TimeFormatter.toSSMSString(iter.beginTime) + SC);
					writer.print(TimeFormatter.toSSMSString(iter.endTime) + SC);
				}
				
				for (int j = 0; j < numTiers; j++) {
					curTier = tiers.get(j);
					curSelList = curTier.getSegments();
					if (counters[j] < curSelList.size()) {
						curSel = curSelList.get(counters[j]);
						if (curSel.beginTime == iter.beginTime && curSel.endTime == iter.endTime) {
							if (curSel instanceof Segment && ((Segment)curSel).label != null) {
								if (xmlOut) {
									writer.print("<v>" + ((Segment)curSel).label + "</v>");
								} else {
									writer.print("\"" + ((Segment)curSel).label + "\"");
								}
							} else {
								if (xmlOut) {
									writer.print("<v></v>");
								}
							}
							counters[j]++;
							//break;
						} else {// fill in empty
							if (xmlOut) {
								writer.print("<v></v>");
							} 
						}
					} else { // no more segments for this tier, fill in empty
						if (xmlOut) {
							writer.print("<v></v>");
						}
					}
					
					if (!xmlOut && j < numTiers -1) {
						writer.print(SC);
					}
				}
				if (xmlOut) {
					writer.println("</span>");
				} else {
					writer.println();
				}
			}			
		}
		
		if (xmlOut) {
			writer.println("</TIER>");
		}		
		writer.close();
	}
	
	
	
	/**
	 * Writes the specified segmentations (tiers) and selections to an xml file. 
	 * Assumes the Segmentations come before the selections in the list.
	 * Tries to detect whether the segmentations have the same time spans. 
	 * 
	 * @param outputFile the destination file
	 * @param segments the segments/selections, in ascending time order
	 * @param includeSelections if false only write the tiers/segmentations. otherwise add individual selections
	 */
	public void write(File outputFile, List<Segmentation> tiers, boolean includeSelections, boolean xmlOut) throws IOException {
		
		int numTiers = 0;
		int numColumns = 1;
		Map<RSelection, MutableInt> selMap = null;
		
		
		numTiers = tiers.size();
		if (numTiers > 1) {
			selMap = new HashMap<RSelection, MutableInt>();
			Segmentation seg = tiers.get(0);
			for (RSelection sel : seg.getSegments()) {
				selMap.put(sel, new MutableInt(1));
			}
			MutableInt val;
			RSelection key;
			for (int i = 1; i < tiers.size(); i++) {
				seg = tiers.get(i);
				for (RSelection sel : seg.getSegments()) {
					Iterator<RSelection> keyIt = selMap.keySet().iterator();
					boolean found = false;
					while (keyIt.hasNext()) {
						key = keyIt.next();
						if (key.beginTime == sel.beginTime && key.endTime == sel.endTime) {
						found = true;
							selMap.get(key).intValue++;
							break;
						}
					}
					if (!found) {
						selMap.put(sel, new MutableInt(1));
					}
				}
			}
			int numKeys = selMap.size();
			int numOccur = 0;
			Iterator<MutableInt> iter = selMap.values().iterator();
			while (iter.hasNext()) {
				numOccur += iter.next().intValue;
			}
			// if more than 50%? of the segments occur in all tiers (on average) treat them as columns
			if (numKeys > 0 && (numOccur / (float) numKeys) >= 1.5) {
				numColumns = numTiers;
			}
		}
		
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8")));// utf-8 is always supported, I guess
		if (xmlOut) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.print("<TIER xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tier.xsd\" ");
			writer.print("columns=\"");
		} else {
			writer.print("\"#starttime\";\"#endtime\";");
		}
		
		if (numColumns > 1) {
			for (int i = 0; i < numColumns; i++) {
				if (xmlOut) {
					writer.print(tiers.get(i).getName().replaceAll(" ", "_"));
				} else {
					writer.print("\"" + tiers.get(i).getName() + "\"");
				}
				if (i != numColumns - 1) {
					if (xmlOut) {
						writer.print(" ");
					} else {
						writer.print(SC);
					}
				}
			}
			if (xmlOut) {
				writer.println("\">");
			} else {
				writer.println();
			}
		} else {
			if (xmlOut) {
				if (tiers.size() > 0) {
					//writer.println(tiers.get(0).getName().replaceAll(" ", "_") + "\">");
					writer.println("all_tiers\">");
				} else {
					writer.println("tier_1\">");
				}
			} else {
				if (tiers.size() > 0) {
					//writer.println( "\"" + tiers.get(0).getName() + "\"");
					writer.println("\"all_tiers\"");
				} else {
					writer.println("\"tier_1\"");
				}
			}
		}
		
		// write the spans
		if (numColumns == 1) {// one column
			List<RSelection> segs = new ArrayList<RSelection>();
			
			for (Segmentation s : tiers) {
				segs.addAll(s.getSegments());
			}
			
			
			Collections.sort(segs, new SelectionComparator());

			RSelection iter;
			// resolve overlaps??
			for (int i = 0; i < segs.size(); i++) {
				iter = segs.get(i);
				//write span
			}
		} else if (numColumns > 1) {
			// the selMap should not be null
			List<RSelection> segs = new ArrayList<RSelection>(selMap.size());
			segs.addAll(selMap.keySet());
			
			Collections.sort(segs, new SelectionComparator());
			
			int[] counters = new int[numTiers];
			// explicitly set to 0?
			Arrays.fill(counters, 0);
			
			RSelection iter;
			RSelection curSel;
			Segmentation curTier;
			List<RSelection> curSelList;

			for (int i = 0; i < segs.size(); i++) {
				iter = segs.get(i);
				if (xmlOut) {
					writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(iter.beginTime) + "\" ");
					writer.print("end=\"" + TimeFormatter.toSSMSString(iter.endTime) + "\">");
				} else {
					writer.print(TimeFormatter.toSSMSString(iter.beginTime) + SC);
					writer.print(TimeFormatter.toSSMSString(iter.endTime) + SC);
				}
				
				for (int j = 0; j < numTiers; j++) {
					curTier = tiers.get(j);
					curSelList = curTier.getSegments();
					if (counters[j] < curSelList.size()) {
						curSel = curSelList.get(counters[j]);
						if (curSel.beginTime == iter.beginTime && curSel.endTime == iter.endTime) {
							if (curSel instanceof Segment && ((Segment)curSel).label != null) {
								if (xmlOut) {
									writer.print("<v>" + ((Segment)curSel).label + "</v>");
								} else {
									writer.print("\"" + ((Segment)curSel).label + "\"");
								}
							} else {
								if (xmlOut) {
									writer.print("<v></v>");
								}
							}
							counters[j]++;
							//break;
						} else {// fill in empty
							if (xmlOut) {
								writer.print("<v></v>");
							} 
						}
					} else { // no more segments for this tier, fill in empty
						if (xmlOut) {
							writer.print("<v></v>");
						}
					}
					
					if (!xmlOut && j < numTiers -1) {
						writer.print(SC);
					}
				}
				if (xmlOut) {
					writer.println("</span>");
				} else {
					writer.println();
				}
			}
			
		}
		if (xmlOut) {
			writer.println("</TIER>");
		}
		
		writer.close();
	}
}
