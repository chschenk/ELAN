/*
 * $Id: ShoeboxArray.java 43699 2015-04-16 11:47:41Z olasei $
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * For parsing the shoeboxfile, I first read it into an array because:  - Each
 * line of the shoeboxfile has to be adressed over and over again -
 * shoeboxfile is organized into blocks and tiers, which translate to rows and
 * columns. - Shoebox wraps a a line at 80 characters. As a parse
 * preprocessing, the hardwrap of the shoeboxfile is removed.     For
 * inter-tier processing of annotation, the ShoeboxArray is a better starting
 * point as a shoebox file.
 */
public class ShoeboxArray {
//    private static Logger logger = Logger.getLogger(ShoeboxArray.class.getName());
    private String shoeboxheader = "";
    private String label_ref;

    /** Holds value of property DOCUMENT ME! */
    public static String label_eudicoparticipant = "\\EUDICOp";

    /** Holds value of property DOCUMENT ME! */
    public static String label_eudicot0 = "\\EUDICOt0";

    /** Holds value of property DOCUMENT ME! */
    public static String label_eudicot1 = "\\EUDICOt1";

    /** Holds value of property DOCUMENT ME! */
//    public static String label_eudicot2 = "\\EUDICOt2";
    private File file = null;
    // store the marker order as encountered in the file
    private List<String> markerOrder = new ArrayList<String>();
    // store the previous label name to determine the placee in the list
    private String prevLabel = null;
    private List<String> labelList = new ArrayList<String>();
    private List<DefaultMutableTreeNode> labelNodeList = new ArrayList<DefaultMutableTreeNode>();
    private String[][] shoeboxArray;
    private int[] shoeboxArrayMaxLength;
    private int currentIndexBlock = -1; // increments *before* \ref
    private int currentIndexLabel = -1; // initial no-sense value
    private int maxIndexBlocks;
    private boolean isShoeboxArrayPreparation = true;
    private boolean strict1;
    private Set<String> interlinearTierMarkers;
    private Map<String, Integer> lineCounts = new HashMap<String, Integer>(); // stores number of lines in block for each interlinear tier
    private boolean readingWAC = false;
    
    //private boolean completelyUnaligned = true;
    //private boolean treatAsUnaligned = true;	// temp solution, until interpolated times are supported
    
    private ArrayList<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    
    private ShoeboxTypFile typFile;

    /**
     * Reads a Shoebox file into an array.
     *
     * @param file the shoeboxfile
     * @param label_ref record marker (including leading "\")
     * @param theTypFile the ShoeboxTypeFile object
     *
     * @throws Exception DOCUMENT ME!
     */
    public ShoeboxArray(File file, String label_ref,
        ShoeboxTypFile theTypFile) throws Exception {
        //this(file, true, label_ref);
        this(file, false, label_ref, theTypFile); // HB, 23 jul 02, set strict1 to false

        // HB, 30 jul 02, added ilTierMarkers
    }

    /**
     * Reads a Shoebox file into an array.
     *
     * @param file the shoebofile
     * @param strict1 line must start with label (e.g. \ref). ALWAYS TRUE
     * @param label_ref record marker (including leading "\")
     * @param theTypFile the ShoeboxTypeFile object
     *
     * @throws Exception DOCUMENT ME!
     */
    public ShoeboxArray(File file, boolean strict1, String label_ref,
 //       HashSet theInterlinearTierMarkers) throws Exception {
 			ShoeboxTypFile theTypFile) throws Exception {
        if (!file.canRead()) {
            throw new Exception("cannot read \"" + file + "\"");
        }

        this.file = file;
        this.typFile = theTypFile;

        //strict1 = true; //strict1;
        this.strict1 = strict1; // HB, 23 jul 02: why else have extra argument?
        
		define_default_labels(label_ref);
		
        if (label_ref == null) {	// set to default
        	label_ref = ShoeboxEncoder.elanBlockStart;
        }
		this.label_ref = label_ref;
        this.interlinearTierMarkers = theTypFile.getInterlinearTierMarkers();

 //       define_default_labels(label_ref);

 //       logger.log(Level.FINE, "preparation START");
        readSbx();
        maxIndexBlocks = currentIndexBlock;
//        logger.log(Level.FINE,
//            "preparation STOP, found " + getNumberOfBlocks() + " blocks");

        /*for (int xx = 0; xx < getNumberOfLabels(); xx++) {
           logger.log(Level.FINE, "label " + xx + " = " + getLabel(xx));
           }*/
        shoeboxArray = new String[getNumberOfLabels()][getNumberOfBlocks()];

        //logger.log(Level.FINE, getNumberOfLabels()+ " ]creating array[ " + getNumberOfBlocks());
        shoeboxArrayMaxLength = new int[getNumberOfBlocks()];
        currentIndexBlock = -1; // reset
//        logger.log(Level.FINE, "reading START");
        readSbx();
 //       logger.log(Level.FINE, "reading STOP");
    }

    /**
     * Reads a WAC file into the array.
     *
     * @param wacfile the wacfile
     *
     * @throws Exception DOCUMENT ME!
     */
    public ShoeboxArray(File wacfile) throws Exception {
        readingWAC = true;
        define_default_labels("\\ref");

        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                                                   .newDocumentBuilder();
        Document doc = db.parse(wacfile);

        readWac(doc); // prepare 1/2
        prepare_or_finish_block(); // prepare 2/2 
        isShoeboxArrayPreparation = false; // 

        shoeboxArray = new String[getNumberOfLabels()][getNumberOfBlocks()];
        shoeboxArrayMaxLength = new int[getNumberOfBlocks()];
        currentIndexBlock = -1; // reset

        readWac(doc); // store
        prepare_or_finish_block(); // ... 
    }

    /////////////////
    final public String getShoeboxHeader() {
        return shoeboxheader;
    }

    /**
     * get value of array
     *
     * @param label DOCUMENT ME!
     * @param block DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public String getCell(int label, int block) {
        String result = null;
 //       logger.log(Level.FINE, " --- getCell(" + label + ", " + block);
        result = shoeboxArray[label][block];
//        logger.log(Level.FINE, " --- getCell == '" + result + "'");

        //alway return the max length padded result
        if ((label > 3) && (result != null)) { // HB, 23 jul 02: added null check	

            while (result.length() < shoeboxArrayMaxLength[block]) {
                result = result + " ";
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param label DOCUMENT ME!
     * @param block DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public String getCell(String label, int block) { //throws Exception{
//        logger.log(Level.FINE, " --- getCell(" + label + ", " + block);

        int x = labelList.indexOf(label);

        if (x < 0) {
        	/*
            JOptionPane.showMessageDialog(null,
                (getClass() + ".getCell(" + label + ", " + block +
                ") \n FATAL ERROR"), "", JOptionPane.ERROR_MESSAGE);
			*/
			System.out.println(getClass() + ".getCell(" + label + ", " + block +
				") \n FATAL ERROR");
            return "";
        }

        //if (x < 0) throw new Exception("label '"+ label + "' does not exist.");
        return getCell(x, block);
    }

    /**
     * DOCUMENT ME!
     *
     * @param label DOCUMENT ME!
     * @param block DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    final private void setC(int label, int block, String value) {
        shoeboxArray[label][block] = value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param block the block/row
     *
     * @return the name of the speaker of given block
     */
    final public String getSpeaker(int block) {
		String result = getCell(ShoeboxEncoder.elanParticipantLabel, block);
        
		if ((result == null) || (result.length() == 0)) {
			result = "unknown";
		}

        result = result.trim();	// HB, 24-8-04

        return result;
    }

    /**
     * Returns the begin time value as read from the file, or 
     * the default of -1, if no time information was found.
     *
     * @param block the block/row
     *
     * @return t0 of given block in milliseconds
     */
    final public long getT0(int block) {
		long t0 = getTX(ShoeboxEncoder.elanBeginLabel, block);
		/*
		if (treatAsUnaligned) {
			//t0 = block * 1000;
			t0 = block * ShoeboxPreferences.preferredBlockDuration;
		}
        */
        if (block == 0 && t0 < 0) {
        	t0 = 0;
        }
        return t0;
    }

    /**
     * Returns the end time value as read from the file, or 
     * the default of -1, if no time information was found.
     *
     * @param block the block/row
     *
     * @return t0 of given block in milliseconds
     */
    final public long getT1(int block) {
		long t1 = getTX(ShoeboxEncoder.elanEndLabel, block);
 		/*
		if (treatAsUnaligned) {
			//t1 = (block + 1) * 1000;
			t1 = (block + 1) * ShoeboxPreferences.preferredBlockDuration;
		}
		*/
        return t1;
    }
    
    public ArrayList<MediaDescriptor> getMediaDescriptors() {
    	return mediaDescriptors;
    }

    /**
     * DOCUMENT ME!
     *
     * @param label DOCUMENT ME!
     * @param block the block/row
     *
     * @return t0 of given block in milliseconds
     */
    final private long getTX(String label, int block) {
        String sresult = getCell(label, block);
        long result = 0;

        try {
            double d = Double.parseDouble(sresult);

            // seconds to milliseconds
            d = d * 1000d;
            
            if (d == -1000) {	// correct unaligned
            	d = -1;
            }

            //Double dd = new Double(d);
            //result = dd.longValue();
            // Double.longValue just casts the double to long
			result = (long) d;

            //result = Long.parseLong(sresult);
        } catch (NumberFormatException e) {
            //System.out.println(" ======= getTX " + block + " found " + sresult);
            return toMilliSeconds(sresult, block);
        }

        return result;
    }
    
	/**
	 * Converts a time definition in the format hh:mm:ss.sss into a long that
	 * contains the time in milli seconds. 
	 * Copied from mpi.eudico.client.util.TimeFormatter
	 *
	 * @param timeString the string that contains the time in the format
	 *        hh:mm:ss.sss
	 * @param block the block index that the time belongs to (for error report)
	 * 
	 * @return the time in seconds, -1.0 if the time string has an illegal
	 *         format
	 */
	public long toMilliSeconds(String timeString, int block) {
		try {
			String hourString = new String("0.0");
			String minuteString = new String("0.0");
			String secondString = new String("0.0");

			int mark1 = timeString.indexOf(':', 0);

			if (mark1 == -1) { // no :, so interpret string as sss.ss
				secondString = timeString;
			} else {
				int mark2 = timeString.indexOf(':', mark1 + 1);

				if (mark2 == -1) { // only one :, so interpret string as mm:ss.sss
					minuteString = timeString.substring(0, mark1);
					secondString = timeString.substring(mark1 + 1,
							timeString.length());
				} else { // two :, so interpret string as hh:mm:ss.sss
					hourString = timeString.substring(0, mark1);
					minuteString = timeString.substring(mark1 + 1, mark2);
					secondString = timeString.substring(mark2 + 1,
							timeString.length());
				}
			}

			double hours = Double.valueOf(hourString).doubleValue();
			double minutes = Double.valueOf(minuteString).doubleValue();
			double seconds = Double.valueOf(secondString).doubleValue();

			return (long) (1000 * ((hours * 3600.0) + (minutes * 60.0) +
			seconds));
		} catch (Exception e) { // the timeString was not parseable
			System.out.println("TX: " + block + " unknown time format: " + timeString);
			return -1;
		}
	}
	
	/**
	 * Returns the ordered list of markers.
	 * @return the list of markers
	 */
	public final List<String> getMarkerOrder() {
	    return markerOrder;
	}

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public int getNumberOfLabels() {
        return labelList.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public Iterator<String> getLabels() {
        return labelList.iterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public String getLabel(int i) {
        return labelList.get(i);
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public DefaultMutableTreeNode getLabelNode(int i) {
        return labelNodeList.get(i);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public DefaultMutableTreeNode getLabelNode(String name) {
        try {
            return labelNodeList.get(getLabelIndex(name));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return labelNodeList.get(0);
        }
    }

    /**
     * returns -1 if not found
     *
     * @param labelincludingtrailingbackslash DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public int getLabelIndex(String labelincludingtrailingbackslash) {
        return labelList.indexOf(labelincludingtrailingbackslash);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    final public int getNumberOfBlocks() {
        return maxIndexBlocks;
    }

    /**
     * DOCUMENT ME!
     *
     * @return file-dump from Array.
     *
     * @throws Exception DOCUMENT ME!
     */
    final public String getShoeboxFile() throws Exception {
        String result = getShoeboxHeader() + "\n";

        for (int bi = 0; bi < getNumberOfBlocks(); bi++) {
            for (int i = 0; i < getNumberOfLabels(); i++) {
                String cntnt = shoeboxArray[i][bi];

                if ((cntnt == null) || (cntnt.length() == 0)) {
                    continue;
                }

                String label = getLabel(i);

                if (i == 0) { // label_ref.equals(label)
                    result = result + "\n";
                }

                result = result + label + "\t" + cntnt.replace('\n', ' ') +
                    "\n";
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception yepp
     */
    final public void dump() throws Exception {
        for (int bi = 0; bi < getNumberOfBlocks(); bi++) {
            for (int i = 0; i < getNumberOfLabels(); i++) {
                String cntnt = getCell(i, bi);

                if (cntnt == null) {
                    cntnt = "#";
                }

                String label = getLabel(i);
                System.out.println(i + "/" + bi + ":\t" + label + "\t" +
                    cntnt.replace('\n', ' '));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception yepp
     */
    final public void dump2() throws Exception {
        for (int _b = 0; _b < getNumberOfBlocks(); _b++) {
            for (int _l = 0; _l < getNumberOfLabels(); _l++) {
                String label = getLabel(_l);
                String cntnt = getCell(label, _b);
                System.out.println(_b + " " + label + " \t" + cntnt);
            }
        }
    }

    /**
     * should look like sbx
     *
     * @throws Exception yepp
     */
    final public void dumpSbx() throws Exception {
        for (int bi = 0; bi < getNumberOfBlocks(); bi++) {
            for (int i = 0; i < getNumberOfLabels(); i++) {
                String cntnt = getCell(i, bi);

                if (cntnt == null) {
                    cntnt = "#";
                }

 //               logger.log(Level.FINE, cntnt.replace('\n', ' '));

                if (i == (getNumberOfLabels() - 1)) {
 //                   logger.log(Level.FINE, "\n");
                } else {
 //                   logger.log(Level.FINE, "\t");
                }
            }
        }
    }

    private void define_default_labels(String label_ref)
        throws Exception {
        // define the ordering of the array by storing labels
        store_label(label_ref);
    //    store_label(label_eudicoparticipant);
        store_label(ShoeboxEncoder.elanParticipantLabel);
    //    store_label(label_eudicot0);
        store_label(ShoeboxEncoder.elanBeginLabel);
    //    store_label(label_eudicot1);
        store_label(ShoeboxEncoder.elanEndLabel);
    }
    
    /**
     * Tries to keep a list of markers in the order they appear in the file. Since each block may not 
     * contain all markers, the result may not always be satisfactory  
     * @param label the label to add
     * @param prevLabel the previous label in the block
     */
    private void storeLabelInOrder(String label, String prevLabel) {
        if (label == null || label.length() == 1 || markerOrder.contains(label)) {
            return;
        }
        if (prevLabel == null) {
            markerOrder.add(label);
        } else {
            if (markerOrder.size() == 0) {
                markerOrder.add(label);
                return;
            }
            for (int i = 0; i < markerOrder.size(); i++) {
                if (prevLabel.equals(markerOrder.get(i))) {
                    // insert after
                    markerOrder.add(i + 1, label);
                    return;
                }
            }
        }
    }

    /**
     * Testing
     *
     * @param arg DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] arg) throws Exception {
        ShoeboxArray s = new ShoeboxArray(new File(arg[0]));
        s.dump2();
    }

    /*
       Add label only once
     */
    private void store_label(String label) throws Exception {
        if (labelList.contains(label) || label == null || label.length() == 1 ||
        		typFile.excludeFromImport(label)) {
            return;
        }

 //       logger.log(Level.FINE,
 //           getClass().getName() + ": store_label (" + label + ")...");

        // rectify the name of the label.
        // brute force.
        // I could escape it to the XML sequence.
        // boolean bad = false;
        /* May 2006: doesn't seem to do anything
        for (int i = 1; i < label.length(); i++) {
            int c = (int) label.charAt(i);

            if (!(((c >= 65) && (c <= 90)) || ((c >= 97) && (c <= 122)) ||
                    ((c >= 48) && (c <= 57)))) {
                // TO DO
                // remove all non-ascii characters for labels
                //label = label.replace(label.charAt(i), 'x');
            }
        }
        */
        labelList.add(label);
        labelNodeList.add(new DefaultMutableTreeNode(label));

        // skip the rest for WAC
        if (readingWAC) {
            return;
        }

        // the file has to be started with the block defining label
        // OOF
        if ((getNumberOfLabels() == 0) && !label.equals(label_ref)) {
            throw new Exception("found '" + label + "', expected '" +
                label_ref + "'.");
        }

        //logger.log(Level.FINE, "good label " + label + " " + labelList.indexOf(label));
    }

    /**
     * preparation and time linking for the last block
     */
    private final void prepare_or_finish_block() {
  //      int pax = getLabelIndex(label_eudicoparticipant);
        int elanPax = getLabelIndex(ShoeboxEncoder.elanParticipantLabel);
  //      int t0 = getLabelIndex(label_eudicot0);
        int elanT0 = getLabelIndex(ShoeboxEncoder.elanBeginLabel);
  //      int t1 = getLabelIndex(label_eudicot1);
        int elanT1 = getLabelIndex(ShoeboxEncoder.elanEndLabel);
        

        if (!isShoeboxArrayPreparation) {
            if (currentIndexBlock >= 0) {
				if ((shoeboxArray[elanPax][currentIndexBlock] == null) ||
						(shoeboxArray[elanPax][currentIndexBlock].length() == 0)) {
					shoeboxArray[elanPax][currentIndexBlock] = "unknown";
				}

				if ((shoeboxArray[elanT0][currentIndexBlock] == null) ||
						(shoeboxArray[elanT0][currentIndexBlock].length() == 0)) {
					Integer II = Integer.valueOf(-1);
					shoeboxArray[elanT0][currentIndexBlock] = II.toString();
				} 
				
                if ((shoeboxArray[elanT1][currentIndexBlock] == null) ||
                        (shoeboxArray[elanT1][currentIndexBlock].length() == 0)) {
                    Integer II = Integer.valueOf(-1);
                    shoeboxArray[elanT1][currentIndexBlock] = II.toString();
                }
                
				correctLineBreaksIfNeeded();	// if not all interlinear lines break off
            }
        }

        currentIndexBlock += 1;
        currentIndexLabel = -1; //reset

//        logger.log(Level.FINE, "     prepare_block()");
    }

    /**
     * sideeffect on variables: currentIndexLabel, currentIndexBlock stores the
     * content for this label in shoeboxArray. if label is \per, store name
     * seperately (aditionally) in shoeboxArray.
     *
     * @param label name of tier
     * @param block DOCUMENT ME!
     * @param content value of tier
     *
     * @throws Exception DOCUMENT ME!
     */
    private final void overwriteContent(String label, int block, String content)
        throws Exception {
        if (isShoeboxArrayPreparation) {
            return;
        }

        shoeboxArray[labelList.indexOf(label)][block] = content; // store in array
        shoeboxArrayMaxLength[block] = content.length();
    }

    /*
       sideeffect on variables: currentIndexLabel, currentIndexBlock
       stores the content for this label in Array.
       if label is \per, store name seperately (aditionally) in shoeboxArray.
     */
    private final void store_label_and_content(String label, String content)
        throws Exception {
        //logger.log(Level.FINE, "store_label_and_content (" + label + ")(" + content + ")...");
        // append to the existing content of the label
        // TODO use function...
        
        // substitute old EUDICO style labels with new ELAN style labels
        if (label.equals(label_eudicoparticipant)) {
			label = ShoeboxEncoder.elanParticipantLabel;
		}
        if (label.equals(label_eudicot0)) {
			label = ShoeboxEncoder.elanBeginLabel;
		}
        if (label.equals(label_eudicot1)) {
			label = ShoeboxEncoder.elanEndLabel;
		}
        
        //if (completelyUnaligned && label.equals(ShoeboxEncoder.elanBeginLabel)) {
        //	completelyUnaligned = false;
        //}
        
        currentIndexLabel = labelList.indexOf(label);

        if (isShoeboxArrayPreparation) {
            return;
        }

        // may be a bad label...
        if (currentIndexLabel < 0) {
            return;
        }
        
        if (typFile.excludeFromImport(label)) {
        	return;
        }

        // may be a bad day...
        // TODO...
        if (currentIndexBlock < 0) {
            return;
        }

        if ((currentIndexLabel > 0) && (currentIndexBlock > 0)) {
            //System.out.println(" --------- testi a " + currentIndexBlock);
            String testi = shoeboxArray[0][currentIndexBlock];

            if ((testi == null) || "".equals(testi)) {
                shoeboxArray[0][currentIndexBlock] = null;
//                logger.log(Level.FINE,
//                    "  stored [" + 0 + "," + currentIndexBlock +
//                    "] (-----)(null)");
            }
        }

        //debug ("...probing oldContent " + currentIndexLabel +"//" + currentIndexBlock);
        String oldContent = shoeboxArray[currentIndexLabel][currentIndexBlock];

        //	if (oldContent != null) content = oldContent + "\n" + content;
        // HB, 30 jul 02: concatenate content of identical labels within the same block
        // using the right number of spaces iso a line break.
        // HB, 31 jul 02 ---------------------------------
        // Algorithm: 
        // - if line of interlinear block, increment the corresponding count in 'lineCounts'
        // - if oldContent != null, add '\n' at beginning of current line
        // - add current line to oldContent
        // - if all line counts > 0, then, for all lines in interlinear block
        // 	- determine largest line until first newline or end
        //	- substitute first \n, or attach to end, the right number of spaces in shoeboxArray
        //	- decrement all counts by one
        //
        // This should work irrespective of the order in which broken tiers occur (so both for tx,
        // mb, gl, ps, tx, mb, gl, ps and for tx, tx, mb, mb, gl, gl, ps, ps)
        if (interlinearTierMarkers.contains(label)) {
            lineCounts.put(label,
            	Integer.valueOf((lineCounts.get(label)).intValue() + 1)); // incr count
        }

        if (oldContent != null) {
            content = oldContent + "\n" + content; // concatenate with a newline by default
        }

        shoeboxArray[currentIndexLabel][currentIndexBlock] = content; // store in array

		correctLineBreaksIfNeeded();
		
        // HB, 31 jul 02, end of block break padding-----------------------
//        logger.log(Level.FINE,
//            "  stored [" + currentIndexLabel + "," + currentIndexBlock + "] (" +
//            label + ")(" + shoeboxArray[currentIndexLabel][currentIndexBlock] +
//            ")");

        // set the maximum length of this block
        int oldLen = shoeboxArrayMaxLength[currentIndexBlock];
        int newLen = content.length();
        int max = (oldLen < newLen) ? newLen : oldLen;
        shoeboxArrayMaxLength[currentIndexBlock] = max;
    }
    
    private void correctLineBreaksIfNeeded() {
		// check if we have to correct interlinear structure at block break
		boolean correct = true;
		Iterator<String> markerIter = interlinearTierMarkers.iterator();

		while (markerIter.hasNext()) {
			if ((lineCounts.get(markerIter.next())).intValue() == 0) {
				correct = false;

				break;
			}
		}

		if (correct) {
			correctLineBreaks();	
		}   	
    }
    
    private void correctLineBreaks() {
		// find maxLengthInInterlinearBlock
		int maxLengthInInterlinearBlock = 0;

		Iterator markerIter2 = interlinearTierMarkers.iterator();

		while (markerIter2.hasNext()) {
			int lblIndex = labelList.indexOf(markerIter2.next());
			String c = shoeboxArray[lblIndex][currentIndexBlock];

			int l = -1;
			if (c != null) {
				l = c.indexOf("\n"); // find first newline
			}

			if ((l < 0) && (c != null)) { // no newline, take length of block
				l = c.length();
			}

			maxLengthInInterlinearBlock = (l < maxLengthInInterlinearBlock)
				? maxLengthInInterlinearBlock : l;
		}

		// pad each interlinear line with right number of spaces
		Iterator<String> markerIter3 = interlinearTierMarkers.iterator();

		while (markerIter3.hasNext()) {
			int lblIndex = labelList.indexOf(markerIter3.next());
			String c = shoeboxArray[lblIndex][currentIndexBlock];

			if (c != null) {
				boolean nlFound = true;
				int l = c.indexOf("\n"); // find first newline
	
				if (l < 0) { // no newline, take length of block
					l = c.length();
					nlFound = false;
				}
	
				int numOfSpaces = maxLengthInInterlinearBlock - l;
				String spaces = "";
	
				for (int i = 0; i < numOfSpaces; i++) {
					spaces += " ";
				}
	
				String newC = "";
	
				if (nlFound) {
					newC = c.substring(0, l) + spaces + " " +
						c.substring(l + 1);
				} else {
					newC = c.substring(0, l) + spaces;
				}
	
				shoeboxArray[lblIndex][currentIndexBlock] = newC; // store in array
			}
		}

		// decrement all lineCounts
		Iterator markerIter4 = interlinearTierMarkers.iterator();

		while (markerIter4.hasNext()) {
			String lbl = (String) markerIter4.next();

			int newCount = (lineCounts.get(lbl)).intValue() -
				1;

			if (newCount < 0) {
				newCount = 0;
			}

			Integer i = Integer.valueOf(newCount);
			lineCounts.put(lbl, i); // decr count
		}
    }
    

    /*
       Used for preparation (counting) and storing.
     */
    private final void readSbx() throws Exception {
        String line = null;
        String utf8Line = null;

        /*
           A shoebox file may contain 8byte characters from custom fonts.
           Treating it as isolatin-1 may introduce character errors!
         */
        Reader filereader = null;
        Reader utf8FileReader = null;
        
        MediaDescriptor mediaDescriptor = null;
        mediaDescriptors = new ArrayList<MediaDescriptor>();
        
//        boolean useDedicatedCharacterset = false;

//        if (useDedicatedCharacterset) {
//            InputStream fis = new FileInputStream(file);
//            filereader = new InputStreamReader(fis, "DedicatedCharacterset");
//        } else {
            // use the default encoding
       //     filereader = new FileReader(file);
	   		filereader = new InputStreamReader(new FileInputStream(file), "ISO-8859-1");
            utf8FileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
//        }

        BufferedReader br = null;
        BufferedReader utf8Br = null;
        try {
	        // explicit performance care: buffering the filereader
	        br = new BufferedReader(filereader);
	        utf8Br = new BufferedReader(utf8FileReader);
	
	        String label = null;
	        prevLabel = null;
	        String content = null;
	        int linenumber = 0;
	
	        while ((line = br.readLine()) != null) {
	        	utf8Line = utf8Br.readLine();
	        	prevLabel = label;
				// if unicode tier, substitute line with utf8Line
				StringTokenizer t = new StringTokenizer(line);
				if (t.hasMoreTokens()) {
					label = t.nextToken(); // the first word
	 				// test here if the line starts with "\". If not (append action) this label is the 
					// same as the previous and it's encoding too.
					if (label.length() > 1 && !utf8Line.startsWith("\ufeff") && (!label.startsWith("\\") || 
					        (label.charAt(0) == '\\' && (label.charAt(1) == ' ' || label.charAt(1) == '\t')))) {
					    label = prevLabel;
					}
	 				if (typFile.isUnicodeTier(label)) {
	 					line = utf8Line;	
	 					if (!isShoeboxArrayPreparation && interlinearTierMarkers.contains(label)) {
	 					    line = decodeToolboxUnicode(line);
	 					}
	 				}
				}
	
	            linenumber++;
	            line = line.trim();
	//            logger.log(Level.FINE, "  ..." + line);
	
	            if (linenumber == 1) {
	            	// HS 06-2006 extended the test with support for files with the Unicode Byte Order Mark,
	            	// \ufeff
	                if ((line.startsWith("\\_sh v4.0")) ||
	                        (line.startsWith("\\_sh v3.0")) ||
	                        (utf8Line.startsWith("\ufeff\\_sh v3.0") || 
	                        		utf8Line.startsWith("\ufeff\\_sh v4.0"))) {
	                    shoeboxheader = line;
	                    
	                    // last token is database type, store in ShoeboxTypFile
	                    String dbType = "";
	                    while (t.hasMoreTokens()) {
	                    	dbType = t.nextToken();
	                    }
	                    if (!dbType.equals("")) {
	                        typFile.setDatabaseType(dbType);
	                    }
	
	                    continue;
	                } else {
	                    throw new Exception(
	                        "A shoebox file must begin with '\\_sh v4.0' or '\\_sh v3.0', found " +
	                        line + "!");
	                }
	            }
	
	            if (line.length() == 0) {
	                // skip white lines
	                continue;
	            }
	
	            if (line.startsWith("\\_") && (currentIndexBlock == -1)) {
	                // add to header
	                shoeboxheader = shoeboxheader + "\n" + line;
	
	                continue;
	            }
	
	            if (!line.startsWith("\\")) {
	                /* when a line does not start with a label, this is an error.
	                   If we are not in strict mode,
	                   we assume that the preceding line is continued.
	                 */
	                if (strict1) {
	                    throw new Exception("tier without leading label \"" + line +
	                        "\"");
	                }
	
	                //else nevertested
	                if (currentIndexLabel < 0) {
	                    throw new Exception(
	                        "There is no tier where I can append \"" + line +
	                        "\" to!");
	                }
	
	                if (isShoeboxArrayPreparation) {
	                    continue;
	                }
	
	                // hacky append
	                String oldContent = shoeboxArray[currentIndexLabel][currentIndexBlock];
	
	                //if (oldContent.length() == 0) {
	                // HS 06-2006 only throw exception when there is no old contents
	                if (oldContent == null) {
	                    throw new Exception(
	                        "There is no tier where I can append \"" + line +
	                        "\" to!");
	                }
	
	                // concatenate, do not mark the point of concatenation,
	                // fix error silently.
	                shoeboxArray[currentIndexLabel][currentIndexBlock] = oldContent +
	                    " " + line;
	                content = ""; // ??
	//                logger.log(Level.FINE, "  appended (" + line + ")");
	
	                continue;
	            } else if (!isShoeboxArrayPreparation && 
	            		(line.length() == 1 || line.charAt(1) == ' ' || line.charAt(1) == '\t')) {
	            	//HS june 2006: allow a single backslash to be part of the content of a marker
	            	// append
	            	if (shoeboxArray[currentIndexLabel][currentIndexBlock] != null) {
	            		shoeboxArray[currentIndexLabel][currentIndexBlock] = shoeboxArray[currentIndexLabel][currentIndexBlock] +
	                    " " + line;
	            		content = "";
	            	}
	            	continue;
	            }
	            
	            // tokenize the shoebox line into label and content
	            {
	                StringTokenizer xxx = new StringTokenizer(line);
	                //prevLabel = label;
	                label = xxx.nextToken(); // the first word
	                storeLabelInOrder(label, prevLabel);
	                // label contains leading backslash!
	            }
	
	
				content = "";
				if (line.length() > label.length()) {
	            	content = (line.substring(label.length() + 1));
				}
	            
	            // strip trailing spaces, if any
	            if (content.length() > 0) {
	            	int lastNonSpaceIndex = content.length() - 1;
	            	while (content.charAt(lastNonSpaceIndex) == ' ') {
	            		lastNonSpaceIndex--;
	            	}
	           		if (lastNonSpaceIndex < content.length() - 1 && lastNonSpaceIndex >= 0) {
	            		content = content.substring(0, lastNonSpaceIndex);
	            	}
	            }
	
				if (line.startsWith(ShoeboxEncoder.elanMediaURLLabel)) {
					if (mediaDescriptor != null) {
						mediaDescriptors.add(mediaDescriptor);	
					}
	            	
					mediaDescriptor = new MediaDescriptor(content, null);
					continue;
				}
	
				if (line.startsWith(ShoeboxEncoder.elanMediaMIMELabel)) {
					if (mediaDescriptor != null) {
						mediaDescriptor.mimeType = content;	
					}
					continue;
				}
	
				if (line.startsWith(ShoeboxEncoder.elanMediaExtractedLabel)) {
					if (mediaDescriptor != null) {
						mediaDescriptor.extractedFrom = content;	
					}
					continue;
				}
	
				if (line.startsWith(ShoeboxEncoder.elanMediaOriginLabel)) {
					if (mediaDescriptor != null) {
						mediaDescriptor.timeOrigin = Long.parseLong(content);	
					}
					continue;
				}
	
				store_label(label);
	
	            if (label.equals(label_ref)) {
	        //        lastlabel = label_ref;
	                prepare_or_finish_block();
	
	                // HB, 31 jul 02, reset lineCounts
	                Iterator<String> markerIter = interlinearTierMarkers.iterator();
	
	                while (markerIter.hasNext()) {
	                    lineCounts.put(markerIter.next(), Integer.valueOf(0));
	                }
	            }
	
	            store_label_and_content(label, content);
	        }
	        
	        // add last pending mediaDescriptor, if present
			if (mediaDescriptor != null) {
				mediaDescriptors.add(mediaDescriptor);	
			}
	
	        prepare_or_finish_block();
	        
	        //checkIfCompletelyAligned();
        } finally {
	        isShoeboxArrayPreparation = false; // only once
        	try {
        		if (br != null) {
        			br.close();
        		}
        	} finally {
        		if (utf8Br != null) {
        			utf8Br.close();
        		}
        	}
        }
    }
    /*
    private void checkIfCompletelyAligned() {
    	// if completelyUnaligned is false there is at least one time set.
    	// For the moment time alignment must be complete, otherwise imported file is
    	// to be treated as completely unaligned.
    	// TEMPORARY: method can be removed when proper dealing with partial time alignment
    	// on top level tiers is implemented
    	if (!completelyUnaligned && !isShoeboxArrayPreparation) {
    		treatAsUnaligned = false;
    		
    		// check t0's for value -1
			int x = labelList.indexOf(ShoeboxEncoder.elanBeginLabel);   		
			String[] beginStrings = shoeboxArray[x];
			for (int i = 0; i < beginStrings.length; i++) {
				if (beginStrings[i].equals("-1")) {
					treatAsUnaligned = true;
					break;	
				}
			}
			
    		// check t1's for value -1, only if treatAsUnaligned isn't already true
    		if (!treatAsUnaligned) {   		
				x = labelList.indexOf(ShoeboxEncoder.elanEndLabel);   		
				String[] endStrings = shoeboxArray[x];
				for (int j = 0; j < endStrings.length; j++) {
					if (endStrings[j].equals("-1")) {
						treatAsUnaligned = true;
						break;
					}
				}
    		}
    	}
    } */
    
    public String getRootMarkerForBlock(int row) {
    	String result = "";
    	
    	Iterator<String> en = getLabels();
    	while (en.hasNext()) {
    		String lbl = en.next();
    		if (lbl.equals(ShoeboxEncoder.elanBeginLabel) ||
    			lbl.equals(ShoeboxEncoder.elanEndLabel) ||
    			lbl.equals(ShoeboxEncoder.elanParticipantLabel) ||
    			lbl.equals(ShoeboxEncoder.elanELANLabel) ||
    			lbl.equals(ShoeboxEncoder.elanBlockStart)) {
    				
    				continue;
    		}
    		if (!typFile.tofromHash.containsKey(lbl) && getCell(lbl, row) != null) {
    			result = lbl;
    			break;
    		}
    	}
    	
    	return result;	
    }

    /**
     * DOCUMENT ME!
     *
     * @param doc wacfile
     *
     * @throws Exception DOCUMENT ME!
     */
    private final void readWac(Document doc) throws Exception {
        NodeList blockList = doc.getElementsByTagName("block");

        for (int i = 0; i < blockList.getLength(); i++) {
            Element blockElement = (Element) blockList.item(i);

            if (isShoeboxArrayPreparation) {
                maxIndexBlocks += 1;
            }

            NodeList tierList = blockElement.getElementsByTagName("tier");

            for (int j = 0; j < tierList.getLength(); j++) {
                Element tierElement = (Element) tierList.item(j);

                //MK:02/11/29 the sad tale of standard procedures: WAC tiernames must follow sbx \-convention...
                String tierName = "\\" + tierElement.getAttribute("name");
                String tierValue = tierElement.getFirstChild().getNodeValue();

                if (isShoeboxArrayPreparation) {
                    store_label(tierName);

                    //					System.out.println(i+"/"+j+")  " + tierName + ": " + tierValue);
                } else {
                    overwriteContent(tierName, i, tierValue);
                }
            }

            prepare_or_finish_block();
        }
    }
    
	/**
	 * Toolbox uses 2 bytes or 3 bytes for certain characters on Unicode markers. 
	 * The interlinear alignement based on whitespace characters is corrected here by adding extra space characters
	 * in between words, depending on the characters in the String. This way the alignement corresponds to the 
	 * alignment in ISO Latin markers.
	 * 
	 * 	 Toolbox stores interlinearization on basis of byte position
	 *	 This causes a problem in case of UTF-8 encodings of more than 1 byte.
	 * 
	 * @param value the original Toolbox unicode encoded String
	 * @return the modified string
	 */
    private String decodeToolboxUnicode(String value) {
    	    if (value == null) {
    	        return value;
    	    }
    	    int length = value.length();
    	    char[] chars = value.toCharArray();
    	    // first count how many chars to add
    	    int count = 0;
    	    char cc;
    	    for (int i = 0; i < length; i++) {
    	        cc = chars[i];

    	        if((cc == '\u0000') ||
    					(cc >= '\u0080' && cc <= '\u07ff')) { // 2 bytes
    	            count++;
    	        } else if((cc >= '\u0800') && (cc <= '\uffff')) { // 3 bytes
    	            count += 2;
    	        }
    	    }
    	    
    	    char[] resChars = new char[length + count];
    	    count = 0;
    	    int k = 0;
    	    
    	    for (int i = 0; i < length && k < resChars.length; i++, k++) {
    	        cc = chars[i];
    	        resChars[k] = chars[i];
    	        if (cc == ' ') {	            
    	            if (count > 0) {
    	                for (int z = 0; z < count; z++) {
    	                    k++;
    	                    if (k < resChars.length) {
    	                        resChars[k] = ' '; 
    	                    } else {
    	                        break;
    	                    }
    	                    
    	                }
    	                count = 0;
    	            }
    	        } else if((cc == '\u0000') ||
    					(cc >= '\u0080' && cc <= '\u07ff')) { // 2 bytes
    	            count++;
    	        } else if((cc >= '\u0800') && (cc <= '\uffff')) { // 3 bytes
    	            count += 2;
    	        }
    	    }
    	    
    	    return new String(resChars);
    }
}
