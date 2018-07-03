package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.util.ServerLogger;

/**
 * New implementation of ShoeboxTypFile. Builds a tier/marker tree from either a .typ file
 * or from marker records.
 * 
 * @author Han Sloetjes
 * @version 1.0 08-2008
 */
public class ToolboxTypFile {
	private String typFileName = "";
	
	private String databaseType = "";
	
	/** recordMarker without backslash */
	private String recordMarker = null;
	/** if there is only one root or ref marker, it will be in the root node */
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
	private List<String> includedMarkers = new ArrayList<String>();
	private List<String> interlinearMarkers;
	private Map<String, String> parentMap = null;
	private String participantMarker = null;

	/**
	 * Initializes from a .typ file
	 */
	public ToolboxTypFile(File typFile) {
		super();
		if (typFile == null) {
			throw new IllegalArgumentException("No .typ file specified");
		} else {
			if (!typFile.exists() || !typFile.canRead()) {
				throw new IllegalArgumentException("The .typ file does not exist or is inaccessible");
			} else {
				typFileName = typFile.getAbsolutePath();				
				readTypFile(typFile);
			}
		}
	}

	/**
	 * Initializes from marker records
	 */
	public ToolboxTypFile(List<MarkerRecord> markers) {
		super();
		if (markers == null) {
			throw new IllegalArgumentException("No markers specified");
		}
		initializeFromMarkerRecords(markers);
	}
	
	/**
	 * Returns the record marker (without backslashes).
	 * 
	 * @return Returns the record marker (without backslashes), or null if there is no or more than one record marker.
	 */
	public String getRecordMarker() {
		return recordMarker;
	}
	
	/**
	 * Returns the tier tree: NB the root node is empty. If there is a real record marker the root will have only one child,
	 * otherwise the root will have multiple direct children.
	 * 
	 * @return the root node
	 */
	public DefaultMutableTreeNode getMarkerTree() {
		return rootNode;
	}
	
	/**
	 * Returns whether this line should be imported.
	 * 
	 * @param marker the marker
	 * @return true if this marker should be imported
	 */
	public boolean isIncluded(String marker) {
		return includedMarkers.contains(marker);
	}
	
	/**
	 * Returns whether this marker is any of the subdivision types.
	 * 
	 * @param marker the marker
	 * @return true if this is a subdivision type (split line into units), false otherwise
	 */
	public boolean isSubdivision(String marker) {
		if (marker == null) {
			return false;
		}
		ToolboxMarker curMark = null;
		DefaultMutableTreeNode node = null;

		Enumeration markEn = rootNode.breadthFirstEnumeration();
		while (markEn.hasMoreElements()) {
			node = (DefaultMutableTreeNode) markEn.nextElement();
			if (node.getUserObject() instanceof ToolboxMarker) {
				curMark = (ToolboxMarker) node.getUserObject();
				if (curMark.getMarker().equals(marker)) {
					int stereo = curMark.getStereoType();
					
					return (stereo == Constraint.SYMBOLIC_SUBDIVISION || stereo == Constraint.TIME_SUBDIVISION || 
							stereo == Constraint.INCLUDED_IN);
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns whether the specified marker is part of an interlinearized block of markers.
	 * 
	 * @param marker the marker name
	 * @return true if the marker is part of a block of interlinearized markers
	 */
	public boolean isInterlinear(String marker) {
		if (marker == null) {
			return false;
		}
		if (interlinearMarkers == null) {
			interlinearMarkers = new ArrayList<String>();
			
			ToolboxMarker curMark = null;
			ToolboxMarker parMark = null;
			DefaultMutableTreeNode node = null;

			Enumeration markEn = rootNode.breadthFirstEnumeration();
			while (markEn.hasMoreElements()) {
				node = (DefaultMutableTreeNode) markEn.nextElement();
				if (node.getUserObject() instanceof ToolboxMarker) {
					curMark = (ToolboxMarker) node.getUserObject();
					int stereo = curMark.getStereoType();
					if (stereo == Constraint.SYMBOLIC_SUBDIVISION || stereo == Constraint.TIME_SUBDIVISION || 
							stereo == Constraint.INCLUDED_IN) {
						interlinearMarkers.add(curMark.getMarker());
					} else if (stereo == Constraint.SYMBOLIC_ASSOCIATION) {
						while (node.getParent() != null && node.getParent() != rootNode) {
							node = (DefaultMutableTreeNode) node.getParent();
							if (node.getUserObject() instanceof ToolboxMarker) {
								parMark = (ToolboxMarker) node.getUserObject();
								stereo = parMark.getStereoType();
								if (stereo == Constraint.SYMBOLIC_SUBDIVISION || stereo == Constraint.TIME_SUBDIVISION || 
										stereo == Constraint.INCLUDED_IN) {
									interlinearMarkers.add(curMark.getMarker());
									break;
								}
							}
						}
					}
				}
			}
		}
		
		return interlinearMarkers.contains(marker);
	}
	
	/** 
	 * Returns the stereotype for a marker.
	 * 
	 * @param markerName the name of the marker
	 * @return the type, one of the {@link #Constraint} constants
	 */
	public int getStereoType(String markerName) {
		if (markerName == null) {
			return -1;
		}
		ToolboxMarker curMark = null;
		DefaultMutableTreeNode node = null;
		int type = -1;

		Enumeration markEn = rootNode.breadthFirstEnumeration();
		while (markEn.hasMoreElements()) {
			node = (DefaultMutableTreeNode) markEn.nextElement();
			if (node.getUserObject() instanceof ToolboxMarker) {
				curMark = (ToolboxMarker) node.getUserObject();
				if (curMark.getMarker().equals(markerName)) {
					return curMark.getStereoType();
				}
			}
		}
		
		return type;
	}
	
	/**
	 * Returns the parent of the marker, or null if there is no parent.
	 * 
	 * @param marker the marker
	 * @return the parent or null
	 */
	public String getParentMarker(String marker) {
		if (marker == null) {
			return null;
		}
		
		if (parentMap != null) {
			return parentMap.get(marker);
		}
		
		return null;
	}
	
	
	/**
	 * @return the participantMarker
	 */
	public String getParticipantMarker() {
		return participantMarker;
	}

	/**
	 * @param participantMarker the participantMarker to set
	 */
	public void setParticipantMarker(String participantMarker) {
		this.participantMarker = participantMarker;
	}

	/**
	 * Fills a map with child-parent mappings.
	 */
	private void fillParentMap() {
		if (parentMap == null) {
			parentMap = new HashMap<String, String>();
			
			ToolboxMarker curMark = null;
			ToolboxMarker parMark = null;
			DefaultMutableTreeNode node = null;

			Enumeration markEn = rootNode.breadthFirstEnumeration();
			while (markEn.hasMoreElements()) {
				node = (DefaultMutableTreeNode) markEn.nextElement();
				if (node.getUserObject() instanceof ToolboxMarker) {
					curMark = (ToolboxMarker) node.getUserObject();
					if (node.getParent() != null) {
						if ( ((DefaultMutableTreeNode) node.getParent()).getUserObject() instanceof ToolboxMarker) {
							parMark = (ToolboxMarker) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
							parentMap.put(curMark.getMarker(), parMark.getMarker());
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a tier tree from a Toolbox database type file, .typ.
	 * All markers defined in the mkr sections are added to the tree. The Record marker should be 
	 * defined explicitly in the .typ file, as well as the relation between markers that are part of
	 * an interlinearized block. If "Parse" is specified in the intprc section, the marker is a symbolic subdivision.
	 * (In fact subdivision is the default between the record marker and the first tier in a block of depending tiers.)
	 * 
	 * @param typFile
	 */
	private void readTypFile(File typFile) {
	    String line   = null;
	    ToolboxMarker curMark = null;
	    List<ToolboxMarker> tfMarkers = new ArrayList<ToolboxMarker>(12);
		String label = null;
		String content = null;
		//String tierToSetLanguageFor = null;
		int linenumber = 0;
		String lastFrom = "";
		String lastTo = "";
		String procType = "Lookup";
	    
	    try {
	    	Reader filereader = new FileReader(typFile);
	    	BufferedReader br = new BufferedReader(filereader);
	    	
	    	try {
				while ((line = br.readLine()) != null) {
					linenumber++;
					line = line.trim();
	
				    if (line.length() == 0) {
						// skip white lines
						continue;
					}
					if (linenumber == 1) {
						//if (line.startsWith("\\+DatabaseType")) {// indexOf??
						if (line.indexOf("\\+DatabaseType") > -1) {// except and ignore any BOM
						    StringTokenizer st = new StringTokenizer(line);
						    st.nextToken();
						    String db = st.nextToken();
						    if (db != null) {
						        databaseType = db.trim();
						    }
							continue;
						} else {
							// or just continue?, read some more lines
							//throw new Exception ("Shoebox typ file must begin with '\\+DatabaseType', found '" + line + "'" );
						}
					}
					// tokenize the line into label and content
					
					StringTokenizer xxx = new StringTokenizer(line);
					label = xxx.nextToken(); // the first word
					// label starts with backslash!
					
					content = (line.substring(label.length())).trim();
	
					if (recordMarker == null && label.equals("\\mkrRecord")) {
						recordMarker = content;
						curMark = new ToolboxMarker(content);
						rootNode.setUserObject(curMark);
						includedMarkers.add(content);
					}
				/*
					if (label.equals("\\mkrFrom")) {
						fromArray.add("\\" + content);
						lastFrom = content;
					}
	
					if (label.equals("\\mkrTo")) {
						toArray.add("\\" + content);
						tofromHash.put("\\" + content, "\\" + lastFrom);
						//System.out.println("tofromHash.put "+ content + "--" + lastFrom+ "'");
					}
				*/
					if (label.equals("\\+mkr")) {
						includedMarkers.add(content);
						curMark = new ToolboxMarker(content);
						tfMarkers.add(curMark);
						if (recordMarker != null && !content.equals(recordMarker)) {// default under the record marker
							curMark.setParent(recordMarker);
							curMark.setStereoType(Constraint.SYMBOLIC_ASSOCIATION);
						}
					}
					if (label.equals("\\-mkr")) {
						curMark = null;
					}
					
					// HB, 24 jul 02: new logic, also storing procedureType, and insensitive to
					// order of markers.
	
					if (label.equals("\\+intprc")) {
						// reset
						lastFrom = "";
						lastTo = "";
			//			procType = "Lookup";
						procType = "Parse";		// hb, 7 sep 04, change default
					}
	
					if (label.equals("\\mkrFrom")) {
						lastFrom = content;
					}
	
					if (label.equals("\\mkrTo")) {
						lastTo = content;
					}
	
					if (line.indexOf("Lookup") >= 0) {	// line contains ParseProc
			//			procType = "Parse";
						procType = "Lookup";	// hb, 7 sep 04
					}
					
					// hb, 16 sep 04
					if (line.indexOf("ParseProc") >= 0) {
						procType = "Parse";
					}
	
					if (label.equals("\\-intprc")) {
						
						ToolboxMarker tm;
						// this assumes that all markers are read first
						for (int i = 0; i < tfMarkers.size(); i++) {
							tm = tfMarkers.get(i);
							if (tm.getMarker().equals(lastTo)) {
								tm.setParent(lastFrom);
								if (procType.equals("Parse")) {
									tm.setSubdivision(true);
									tm.setStereoType(Constraint.SYMBOLIC_SUBDIVISION);
								}
								break;
							}
						}
					}
	
					//MK:02/08/16 add language for tier
					/*
					if (label.equals("\\+mkr")) {
						tierToSetLanguageFor = content;
					}
					if (label.equals("\\lng")) {
						if (content == null) continue;
						if (!content.equals("IPA") && !content.equals("Phonetic")) continue;
						if (tierToSetLanguageFor == null) continue;
						tiersWithIPA.add(tierToSetLanguageFor);
					}
					
					if (label.equals("\\-mkr")) {
						tierToSetLanguageFor = null;
					}
					*/
				}
				br.close();
				filereader.close();
				
				// postprocess and build tree
				// HS july 2005 do some post processing; the first mkrFrom under the RecordMarker
				// is typically not listed in the 'intproclst'. Now assume that a marker that is 
				// direct child of the RecordMarker and has a child with proc "Parse" that this 
				// in between marker also has a "Parse" relation to the RecordMarker and add it 
				// to the hash
				// HS 29 sep 2005: it seems to be save to assume a "Parse" relation in the above 
				// situation even this direct child of the RecordMarker only has a "Lookup" child.
				ToolboxMarker tm1;
				ToolboxMarker tm2;
				List<DefaultMutableTreeNode> allNodes = new ArrayList<DefaultMutableTreeNode>(tfMarkers.size());
				
				for (int i = 0; i < tfMarkers.size(); i++) {
					tm1 = tfMarkers.get(i);
					if (tm1.getMarker().equals(recordMarker)) {
						continue;
					}
					allNodes.add(new DefaultMutableTreeNode(tm1));
					/*
					if (tm1.getParent() == null && tm1.getMarker().equals(recordMarker)) {
						rootNode.setUserObject(tm1);
					} else {
						allNodes.add(new DefaultMutableTreeNode(tm1));
					}
					*/
					if (tm1.getParent() != null && tm1.getParent().equals(recordMarker)) {
						for (int j = 0; j < tfMarkers.size(); j++) {
							if (j == i) {
								continue;
							}
							tm2 = tfMarkers.get(j); 
							if (tm2.getParent() != null && tm2.getParent().equals(tm1.getMarker())) {
								// tm1 is the first marker of a block of interlinearized markers, make subdivision
								// note: this excludes the possibility of a sym assoc child of a sym.assoc child of the record marker
								tm1.setSubdivision(true);
								tm1.setStereoType(Constraint.SYMBOLIC_SUBDIVISION);
								break;
							}
						}
					}
				}
				// now build tree
				DefaultMutableTreeNode n1;
				DefaultMutableTreeNode n2;
				
				for (int i = 0; i < allNodes.size(); i++) {
					n1 = allNodes.get(i);
					tm1 = (ToolboxMarker) n1.getUserObject();
					if (tm1.getParent() == null || tm1.getParent().equals(recordMarker)) {
						rootNode.add(n1);
					} else {
						for (int j = 0; j < allNodes.size(); j++) {
							if (j == i) {
								continue;
							}
							n2 = allNodes.get(j);
							tm2 = (ToolboxMarker) n2.getUserObject();
							if (tm1.getParent().equals(tm2.getMarker())) {
								n2.add(n1);
								break;
							}
						}
					}					
				}
				
				fillParentMap();
	    	} catch (IOException ioe) {
	    		ServerLogger.LOG.warning("Read error: " + ioe.getMessage());
	    	}
	    	   	
	    } catch (FileNotFoundException fne) {
	    	ServerLogger.LOG.warning("No file: " + fne.getMessage());
	    }
	}
	
	/**
	 * Creates a tier tree from the list of Marker Records. This allows multiple root tiers,
	 * the record marker is null in that case.
	 * 
	 * @param markerRecords the List of marker records
	 */
	private void initializeFromMarkerRecords(List<MarkerRecord> markerRecords) {
	    MarkerRecord mr = null;
		ToolboxMarker tm1;
		ToolboxMarker tm2;
		DefaultMutableTreeNode n1;
		DefaultMutableTreeNode n2;
		List<DefaultMutableTreeNode> allNodes = new ArrayList<DefaultMutableTreeNode>(markerRecords.size());
		int numRootTiers = 0;
			
			// assume that there is only one top marker
			// take the first marker without a parent
			Iterator mrIter = markerRecords.iterator();
			while (mrIter.hasNext()) {
				mr = (MarkerRecord) mrIter.next();
				if (mr.getParentMarker() == null && !mr.isExcluded()) {
					numRootTiers++;
				}
				if (!mr.isExcluded()) {
					includedMarkers.add(mr.getMarker());
				}
				tm1 = new ToolboxMarker(mr.getMarker());
				tm1.setParent(mr.getParentMarker());
				if (	mr.getStereoType() != null && 
						mr.getStereoType().equals("Symbolic Association")) {
					tm1.setStereoType(Constraint.SYMBOLIC_ASSOCIATION);
				} else if (mr.getStereoType() != null && 
						mr.getStereoType().equals("Time Subdivision")) {
					tm1.setSubdivision(true);
					tm1.setStereoType(Constraint.TIME_SUBDIVISION);
				} else if (mr.getStereoType() != null && mr.getStereoType().equals("Included In")) {
					tm1.setSubdivision(true);
					tm1.setStereoType(Constraint.INCLUDED_IN);
				} else if (mr.getStereoType() != null && mr.getStereoType().equals("Symbolic Subdivision")) {
					tm1.setSubdivision(true);
					tm1.setStereoType(Constraint.SYMBOLIC_SUBDIVISION);
				}
				
				
				// participant marker and excluded markers
				if (mr.getParticipantMarker()) {
					participantMarker = /*"\\" +*/ mr.getMarker();
					mr.setStereoType("Symbolic Association");
				}
				
				allNodes.add(new DefaultMutableTreeNode(tm1));
			}
			
			if (numRootTiers == 1) {
				// one record marker, find it
				for (int i = 0; i < allNodes.size(); i++) {
					n1 = allNodes.get(i);
					tm1 = (ToolboxMarker) n1.getUserObject();
					if (tm1.getParent() == null) {
						recordMarker = tm1.getMarker();
						rootNode.setUserObject(tm1);
						break;
					}
				}
			} else {
				recordMarker = null;
			}
			
			if (recordMarker == null) {
				// error no show if there is no record marker, throw exception?? No!
			}
			
			// build tree
			for (int i = 0; i < allNodes.size(); i++) {
				n1 = allNodes.get(i);
				tm1 = (ToolboxMarker) n1.getUserObject();
				if (tm1.getParent() == null || tm1.getParent().equals(recordMarker)) {
					rootNode.add(n1);
				} else {
					for (int j = 0; j < allNodes.size(); j++) {
						if (j == i) {
							continue;
						}
						n2 = allNodes.get(j);
						tm2 = (ToolboxMarker) n2.getUserObject();
						if (tm1.getParent().equals(tm2.getMarker())) {
							n2.add(n1);
							break;
						}
					}
				}					
			}
			
			fillParentMap();
	}
	
	
}
