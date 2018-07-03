package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * Reads the database description file (*.typ) of a Shoeboxfile.
 */
public class ShoeboxTypFile {

	//private List markers = new ArrayList();
	private String typFileName = "";
	
	private String databaseType = "";

	/**  each tier that is parent in a from-to relation */
	public List<String> fromArray = new ArrayList<String>();
	/**  each tier that is child in a from-to relation */
	private List<String> toArray = new ArrayList<String>();
	/**  hash of the above */
	public Map<String, String> tofromHash  = new HashMap<String, String>();

	// HB, 24 jul 02: store procedureType, to derive constraint stereotype from.
	// Key values are same as for tofromHash.
	public Map<String, String> procedureTypeHash = new HashMap<String, String>();

	/** watch out: no \\ backslash on recordMarker */
	public String recordMarker = null;
	public String interlinearRootMarker = null;

	/** shoebox stores the name of a tier */
	private List<String> tiersWithIPA = new ArrayList<String>();
	private List<String> tiersWithUnicode = new ArrayList<String>();
	private List<String> excludedTiers = new ArrayList<String>();
	private boolean allTiersUnicode = false;
	private boolean ddebug = "true".equals(System.getProperty("ddebug"));
	private boolean debug = "true".equals(System.getProperty("debug"));
	private void ddebug(String s) {
		if (ddebug) { System.out.println("---- ShoeboxFile3: " + s); }
	}
	private void debug(String s) {
		if (debug) { System.out.println("-- ShoeboxFile3: " +s); }
	}

//	private List getMarkers() {
//		if (typFileName != null && typFileName.equals("")) {
//			return markers;
//		}
//		else {
//			return null;
//		}
//	}

	/**
	 * Allow the DatabaseTpe value to be overruled from a .txt file (?)
	 */
	public void setDatabaseType(String theType) {
		databaseType = theType;
	}
	
	public String getDatabaseType() {
		if (databaseType.equals("") && !typFileName.equals("")) {
			// get database type from typFileName
			if ( typFileName.endsWith(".typ") || typFileName.endsWith(".TYP")) {
				int leafIndex = typFileName.lastIndexOf("/") + 1;
				if (leafIndex <= 0) {
					leafIndex = typFileName.lastIndexOf("\\") + 1;	
				}
				
				int endIndex = typFileName.lastIndexOf(".");
				if (leafIndex > 0 && endIndex > 0 && leafIndex < endIndex) {
					databaseType = typFileName.substring(leafIndex, endIndex);
				}
			}
		}
		return databaseType;
	}


	/**
	   @param strict1 line must start with label (e.g. \ref).
	   @param label00 name of block starting label, including leading \
	 */
	public ShoeboxTypFile (File file) throws IllegalArgumentException, Exception {
		if (file == null) {	 // ShoeboxMarkerDialog MarkerRecords to be use
	//		initializeFromMarkerRecords();
		}
		else {
			if (!file.canRead()) {
				throw new IllegalArgumentException("cannot read \"" + file + "\"");
			} 
			typFileName = file.getAbsolutePath();
			readFile(file);			
		}
	}
	
	public ShoeboxTypFile(List<MarkerRecord> markerRecords) {
		if (markerRecords != null) {
			initializeFromMarkerRecords(markerRecords);
		}
	}

	/**
	 * Fake typ-file, needed for WAC
	 */
	public ShoeboxTypFile () throws IllegalArgumentException, Exception {
	}

	private void initializeFromMarkerRecords(List<MarkerRecord> markerRecords) {
	//	Vector markerRecords = ShoeboxMarkerDialog.getMarkers();
		MarkerRecord topMarker = null;
		
		// assume that there is only one top marker
		// take the first marker without a parent
		for (MarkerRecord mr : markerRecords) {
			if (mr.getParentMarker() == null && !mr.isExcluded()) {
				if (topMarker == null) {
					topMarker = mr;
				}
				else {	// more than 1 root marker, assume that we deal with ELAN exported Toolbox files
					topMarker = null;	// reset
					break;
				}
			}
		}
		if (topMarker != null) {
			interlinearRootMarker = topMarker.getMarker();
		}
		
		//System.out.println("Root: " + interlinearRootMarker);
		// if interlinearRootMarker == null parsing seems to silently return 
		// a transcription with zero tiers ??
		
		// fill fromArray, toArray, toFromHash and procedureTypeHash from mr's
		for (MarkerRecord mr : markerRecords) {
			if (mr.getParentMarker() != null) {
				fromArray.add("\\" + mr.getParentMarker());
				toArray.add("\\" + mr.getMarker());
				tofromHash.put("\\" + mr.getMarker(), "\\" + mr.getParentMarker());
				if (	mr.getStereoType() != null && 
						mr.getStereoType().equals("Symbolic Association")) {
					procedureTypeHash.put("\\" + mr.getMarker(), "Lookup");
				}
				else if (mr.getStereoType() != null && 
						mr.getStereoType().equals("Time Subdivision")) {
					procedureTypeHash.put("\\" + mr.getMarker(), "TimeSubdivision");
				} else if (mr.getStereoType() != null && mr.getStereoType().equals("Included In")) {
				    procedureTypeHash.put("\\" + mr.getMarker(), "IncludedIn");
				}
				else {
					procedureTypeHash.put("\\" + mr.getMarker(), "Parse");
				}
			}
			
			if (mr.getCharsetString().equals(MarkerRecord.SILIPASTRING)) {
				tiersWithIPA.add(mr.getMarker());
			}
			if (mr.getCharsetString().equals(MarkerRecord.UNICODESTRING)) {
				tiersWithUnicode.add(mr.getMarker());
			}
			if (mr.getParticipantMarker()) {
				ShoeboxArray.label_eudicoparticipant = "\\" + mr.getMarker();
			}
			if (mr.isExcluded()) {
				excludedTiers.add(mr.getMarker());
			}
		}
		
		// HS jul 2005: if there are any markers marked for exclusion,
		// add their descendants to the excluded tiers array as well
		if (excludedTiers.size() > 0) {
			for (int i = 0; i < excludedTiers.size(); i++) {
				String parent = "\\" + excludedTiers.get(i);
				if (fromArray.contains(parent)) {
					addDescendantsToExcludedTiers(parent);
				}
			}
		}
	}
	
	public static void main (String[] arg) throws Exception {
		ShoeboxTypFile s = new ShoeboxTypFile (new File(arg[0]));
		System.out.println(s.interlinearRootMarker);
		System.out.println(s.fromArray);
		System.out.println(s.toArray);
		System.out.println("");
		System.out.println(s.procedureTypeHash.keySet());
		System.out.println(s.procedureTypeHash.values());
	}



	/*
	  Used for preparation (counting) and storing.
	*/
	private final void readFile(File file) throws IOException, Exception {
		String line   = null;
		/*
		  A shoebox file may contain 8byte characters from custom fonts.
		  Treating it as isolatin-1 may introduce character errors!
		 */
		Reader filereader;

		// HB, 24 jul 02: FAKE IMPLEMENTATION IS TO BE SUBSTITUTED !
		boolean useDedicatedCharacterset = false;
		if (useDedicatedCharacterset) {
			InputStream fis = new FileInputStream(file);
			filereader = new InputStreamReader(fis, "DedicatedCharacterset");
		} else {
			// use the default encoding
			filereader = new FileReader(file);
		}
		// explicit performance care: buffering the filereader
		BufferedReader br = new BufferedReader(filereader);

		String label = null;
		String content = null;
		String tierToSetLanguageFor = null;
		int linenumber = 0;
		String lastFrom = "";
		String lastTo = "";
		String procType = "Lookup";

		try {
			while ((line = br.readLine()) != null) {
				linenumber++;
				line = line.trim();
				debug("  ..." + line);
				if (line.length() == 0) {
					// skip white lines
					continue;
				}
				if (linenumber == 1) {
					// HB, 24 jul 02: accept only DatabaseType TEXT, only works for Shoebox text databases
					// MK/02/10/13 loosening check
					if (line.startsWith("\\+DatabaseType")) {
						StringTokenizer st = new StringTokenizer(line);
						st.nextToken();
						String db = st.nextToken();
						if (db != null) {
							databaseType = db.trim();
						}
						continue;
					} else {
						throw new Exception ("Shoebox typ file must begin with '\\+DatabaseType', found '" + line + "'" );
					}
				}
				// tokenize the shoebox line into label and content
				{
					StringTokenizer xxx = new StringTokenizer(line);
					label = xxx.nextToken(); // the first word
					// label contains trailing backslash!
				}
				content = (line.substring(label.length())).trim();

				if (recordMarker == null && label.equals("\\mkrRecord")) {
					interlinearRootMarker = content;
					recordMarker = content;
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
					// store results in Hashtables and arrays
					fromArray.add("\\" + lastFrom);
					toArray.add("\\" + lastTo);
					tofromHash.put("\\" + lastTo, "\\" + lastFrom);
					procedureTypeHash.put("\\" + lastTo, procType);
				}

				//MK:02/08/16 add language for tier
				if (label.equals("\\+mkr")) {
					tierToSetLanguageFor = content;
				}
				if (label.equals("\\lng")) {
					if (content == null) {
						continue;
					}
					if (!content.equals("IPA") && !content.equals("Phonetic")) {
						continue;
					}
					if (tierToSetLanguageFor == null) {
						continue;
					}
					tiersWithIPA.add(tierToSetLanguageFor);
				}
				if (label.equals("\\-mkr")) {
					tierToSetLanguageFor = null;
				}
			}
		} finally {
			br.close();
			filereader.close();
		}

		// HS july 2005 do some post processing; the first mkrFrom under the RecordMarker
		// is typically not listed in the 'intproclst'. Now assume that a marker that is 
		// direct child of the RecordMarker and has a child with proc "Parse" that this 
		// in between marker also has a "Parse" relation to the RecordMarker and add it 
		// to the hash
		// HS 29 sep 2005: it seems to be save to assume a "Parse" relation in the above 
		// situation even this direct child of the RecordMarker only has a "Lookup" child.
		int size = toArray.size(); //loop only over existing entries
		for (int i = 0; i < size; i++){
			String key = toArray.get(i);
			String val = procedureTypeHash.get(key);
			if (val != null && (val.equals("Parse") || val.equals("Lookup"))) {
				String from = tofromHash.get(key);
				if (from != null && !toArray.contains(from)) {
					// assume a Parse relation to RecordMarker
					fromArray.add("\\" + recordMarker);
					toArray.add(from);
					tofromHash.put(from, "\\" + recordMarker);
					procedureTypeHash.put(from, "Parse");
				}
			}
		}
	}

	// HB, 30 jul 02: added because necessary in ShoeboxArray class to determine number of spaces
	// to pad lines in case of 'broken' interlinear blocks.

	/**
	 * Returns all shoebox tier markers that take part in the interlinear setup.
	 * 
	 * Note: HS july 2005
	 * Changed the way the set of markers that participate in the interlinear setup 
	 * is build: (based on a very basic understanding of what is possible in a Shoebox file)<br>
	 * - before: 
	 *   - include all markers except the root tier marker
	 * - after: 
	 *   - include all markers (and their descendants) that have a "Parse" or "TimeSubdivision"  
	 *     relationship with the root tier marker or that have at least one descendant marker 
	 *     that has a "Parse" or "TimeSubdivision" relationship with its parent 
	 * 
	 * Question: should markers that are marked for exclusion from import (and their descendants) 
	 * be excluded from this Set (or is this handled by ShoeboxArray/Parser)??
	 */
	public HashSet<String> getInterlinearTierMarkers() {
		//printStats();
		HashSet<String> markerSet = new HashSet<String>();

		String rootMkr = "\\" + interlinearRootMarker;

		for (int i = 0; i < toArray.size(); i++) {
			String mkrLabel = toArray.get(i);
			if (mkrLabel.equals(rootMkr) || excludeFromImport(mkrLabel)) {
				//System.out.println("Skipping marker: " + mkrLabel);
				continue;
			}
			String proc = procedureTypeHash.get(mkrLabel);
			String parent = tofromHash.get(mkrLabel);
			if (proc != null && parent != null && parent.equals(rootMkr)) {
				if (proc.equals("Parse") || proc.equals("TimeSubdivision") || 
				        proc.equals("IncludedIn")) {
					markerSet.add(mkrLabel);
					markerSet.addAll(getDescendantsOf(mkrLabel));
					//System.out.println("Adding... " + mkrLabel);
				} else {
					// only add the marker + sub markers if somewhere down the tree a 
					// "Parse" relationship is encountered

					if (atLeastOneParseInTree(mkrLabel)) {
						//System.out.println("Parse found in tree: " + mkrLabel);
						markerSet.add(mkrLabel);
						markerSet.addAll(getDescendantsOf(mkrLabel));
					}
				}
			}
		}

		//markerSet.addAll(toArray);
		//markerSet.addAll(fromArray);
		
		// HB, 15-9-04
		if (markerSet.contains("\\" + interlinearRootMarker)) {
			markerSet.remove("\\" + interlinearRootMarker);
		}
		/*
		System.out.println("\nMarkers...");
		Iterator hsIter = markerSet.iterator();
		while (hsIter.hasNext()) {
			String mkrLabel = (String) hsIter.next();
			System.out.println(mkrLabel);
		}
		*/
		return markerSet;
	}
	
	/**
	 * Recursively get the children (of any 'procedure' type) of the 
	 * specified marker.
	 * 
	 * @param mkrLabel the parent marker
	 * @return a (flat) list of descendant markers
	 */
	private List<String> getDescendantsOf(String mkrLabel) {
		List<String> desc = new ArrayList<String>();
		if (fromArray.contains(mkrLabel) && !excludeFromImport(mkrLabel)) {
			for (Entry<String, String> e : tofromHash.entrySet()) {
				String key = e.getKey();
				String val = e.getValue();
				if (val.equals(mkrLabel) && !excludeFromImport(key)) {
					desc.add(key); //key is the "toMkr"
					desc.addAll(getDescendantsOf(key));
				}
			}
		}
		return desc;
	}
	
	/**
	 * Check the marker's subtree to see if there is somewhere down the tree 
	 * a "Parse" or "TimeSubdivision" relationship.
	 * 
	 * @param mkrLabel probe the descendants of this marker
	 * @return true if a Parse relationship has been found, false otherwise
	 */
	private boolean atLeastOneParseInTree(String mkrLabel) {
		boolean parse = false;
		if (toArray.contains(mkrLabel)) {
			for (Entry<String, String> e : tofromHash.entrySet()) {
				String key = e.getKey();
				String val = e.getValue();
				String proc = procedureTypeHash.get(key);
				if (val.equals(mkrLabel)) {
					if (proc != null && (proc.equals("Parse") || proc.equals("TimeSubdivision"))) {
						return true;
					} else {
						return atLeastOneParseInTree(key);
					}
				}
			}
		}
		return parse;
	}
	
	/**
	 * When a marker is marked for exclusion recursively add the descendant markers 
	 * to the array of excluded tiers as well.
	 * 
	 * @param parent the parent marker
	 */
	private void addDescendantsToExcludedTiers(String parent) {
		for (Entry<String, String> e : tofromHash.entrySet()) {
			String toKey = e.getKey();
			String fromVal = e.getValue();
			if (fromVal.equals(parent)) {
				excludedTiers.add(toKey.substring(1));
				addDescendantsToExcludedTiers(toKey);				
			}
		}

	}

	/**
	 * @return All shoebox tiers (without backslash) with language IPA
	 */
	public boolean isIPAtier(String name) {
		if (name.startsWith("\\")) {
			name = name.substring(1);
		}
//		System.out.println("isIPAtier("+name+") --> " + tiersWithIPA.contains(name));
		return tiersWithIPA.contains(name);
	}
	
	public boolean isUnicodeTier(String name) {
	    if (allTiersUnicode) {
	        return true;
	    }
		if (name.startsWith("\\")) {
			name = name.substring(1);
		}
//		System.out.println("isIPAtier("+name+") --> " + tiersWithIPA.contains(name));
		return tiersWithUnicode.contains(name);		
	}

	public boolean excludeFromImport(String name) {
		if (name.startsWith("\\")) {
			name = name.substring(1);
		}
		return excludedTiers.contains(name);
	}
	
	/**
	 * Prints the contents of the arrays and hashtables, to get an idea of what 
	 * is inside.
	 */
	private void printStats() {
		System.out.println("Root: " + interlinearRootMarker);
		
		System.out.println("\nTo-From hash...");
		for (Entry<String, String> e : tofromHash.entrySet()) {
			String key = e.getKey();
			String val = e.getValue();
			System.out.println("Key-value: " + key + " - " + val);
		}
		
		// print procedure Hash
		System.out.println("\nProcedure hash...");
		for (Entry<String, String> e : procedureTypeHash.entrySet()) {
			String key = e.getKey();
			String val = e.getValue();
			System.out.println("Key-value: " + key + " - " + val);
		}

		String rootMkr = "\\" + interlinearRootMarker;
		System.out.println("\nTo array...");
		for (int i = 0; i < toArray.size(); i++) {
			String mkrLabel = toArray.get(i);
			if (mkrLabel.equals(rootMkr)) {
				System.out.println(mkrLabel + " (root)");
			} else {
				System.out.println(mkrLabel);
			}	
		}
		
		System.out.println("\nFrom array...");
		for (int i = 0; i < fromArray.size(); i++) {
			String mkrLabel = fromArray.get(i);
			if (mkrLabel.equals(rootMkr)) {
				System.out.println(mkrLabel + " (root)");
			} else {
				System.out.println(mkrLabel);
			}
		}
		
		System.out.println("\nExcluded array...");
		for (int i = 0; i < excludedTiers.size(); i++) {
			String mkrLabel = excludedTiers.get(i);
			if (mkrLabel.equals(rootMkr)) {
				System.out.println(mkrLabel + " (root)");
			} else {
				System.out.println(mkrLabel);
			}
		}
	}
	
    /**
     * When true all markers are to be considered to be Unicode.
     * @return Returns whether all tiers/markers are Unicode.
     */
    public boolean isAllTiersUnicode() {
        return allTiersUnicode;
    }
    
    /**
     * When true all markers will be considered to be Unicode.
     * @param allTiersUnicode true if all markers are to be handled as Unicode.
     */
    public void setAllTiersUnicode(boolean allTiersUnicode) {
        this.allTiersUnicode = allTiersUnicode;
    }
}
