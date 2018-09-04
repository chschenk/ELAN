package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.util.ServerLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * A parser class for Toolbox files, that can be either completely encoded in
 * utf-8 or ISO-Latin. Information about tier structure is taken from a
 * ToolboxTypFile class, which builds on a a Shoebox .typ file or an ELAN
 * marker file.  Toolbox markers that are part of an interlinearization are
 * treated such that  words/units are never cut-off. In re-constructing the
 * alignment both non-spacing characters and "multiple bytes" characters are
 * taken into account (in most Toolbox files the alignment seems to be based
 * on number of bytes).
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxParser extends Parser {
    private final static String ANN_ID_PREFIX = "ann";
    private final static String TS_ID_PREFIX = "ts";
    private final static String UNKNOWN = "unknown";
    private final static String AT = "@";

    /** the old MPI marker for the participant */
    public static String label_eudicoparticipant = "EUDICOp";

    /** the old MPI marker for the begin time */
    public static String label_eudicot0 = "EUDICOt0";

    /** the old MPI marker for the end time */
    public static String label_eudicot1 = "EUDICOt1";

    /** a special marker for a Toolbox file exported by ELAN */
    public static final String elanELANLabel = "ELANExport";

    /** the default record marker for export by ELAN */
    public static final String elanBlockStart = "block";

    /** the new MPI marker for the begin time */
    public static final String elanBeginLabel = "ELANBegin";

    /** the new MPI marker for the end time */
    public static final String elanEndLabel = "ELANEnd";

    /** the new MPI marker for the participant */
    public static final String elanParticipantLabel = "ELANParticipant";

    /** the MPI marker for a media url */
    public static final String elanMediaURLLabel = "ELANMediaURL";

    /**
     * the MPI marker for the extracted from attribute of an eaf media
     * descriptor
     */
    public static final String elanMediaExtractedLabel = "ELANMediaExtracted";

    /** the MPI marker for a media mime type */
    public static final String elanMediaMIMELabel = "ELANMediaMIME";

    /** the MPI marker for a media offset */
    public static final String elanMediaOriginLabel = "ELANMediaOrigin";
    private long annotId = 0;
    private long tsId = 0;
    private ToolboxTypFile typFile;
    private ToolboxDecoderInfo2 decoderInfo;
    private String lastParsed = "";
    private String userParticipantMarker = null;

    /**
     * Hierarchical structure of the tags in the shoebox file. Elements are of
     * type String.
     */
    private DefaultMutableTreeNode tierTree;
    private ArrayList<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>(); // LingTypeRecord objects
    private ArrayList<String> participantOrder = new ArrayList<String>();
    private TreeSet<String> tierNameSet = new TreeSet<String>();
    private ArrayList<String> markerOrder = new ArrayList<String>();
    private HashMap<String, String> parentHash = new HashMap<String, String>();
    private ArrayList<long[]> timeOrder = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<long[]> timeSlots = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<AnnotationRecord> annotationRecords = new ArrayList<AnnotationRecord>();
    private HashMap<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();
    private HashMap<String, ArrayList<AnnotationRecord>> tierNameToAnnRecordMap = new HashMap<String, ArrayList<AnnotationRecord>>();

    // for calculation of 'root annotation' times
    private ArrayList<long[]> rootSlots = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    private final ArrayList<String> specialMarkers = new ArrayList<String>(10);
    private int preferredBlockDuration = ToolboxDecoderInfo.DEFAULT_BLOCK_DURATION;
    private char[] scrubbables = new char[]{' ', '\t', '\f', '\r'};

    private int noOfDefectiveBlocks = 0;
    // for caching records for the purpose of (re-)ordering based on the begin times
    private boolean sortRecordsBeforeParsing = false;
    //private long lastCachedBeginTime = 0L;
    //private long lastCachedEndTime = 0L;
    private Map<String, TreeMap<Long, List<ToolboxLine>>> speakerCacheMap = new HashMap<String, TreeMap<Long, List<ToolboxLine>>>();
    
    
    /**
     * Creates a new ToolboxParser instance. Creates a list of special markers.
     */
    public ToolboxParser() {
        specialMarkers.add(label_eudicoparticipant);
        specialMarkers.add(elanParticipantLabel);
        specialMarkers.add(label_eudicot0);
        specialMarkers.add(elanBeginLabel);
        specialMarkers.add(label_eudicot1);
        specialMarkers.add(elanEndLabel);
        specialMarkers.add(elanMediaExtractedLabel);
        specialMarkers.add(elanMediaMIMELabel);
        specialMarkers.add(elanMediaOriginLabel);
        specialMarkers.add(elanMediaURLLabel);
        String sortProperty = System.getProperty("ToolboxParser.SortRecordsBeforeParsing");
        if (sortProperty != null && sortProperty.toLowerCase().equals("true")) {
        	sortRecordsBeforeParsing = true;
        }
    }

    // parse the txt file
    private void parse(String fileName) {
        if (lastParsed.equals(fileName)) {
            return;
        }

        File tbFile;

        if (fileName == null) {
            throw new IllegalArgumentException("Toolbox file not specified");
        } else {
            tbFile = new File(fileName);

            if (!tbFile.exists()) {
                throw new IllegalArgumentException(
                    "Specified Toolbox file does not exist: " + fileName);
            }
        }

        // throw exception if there is no decoder info?? or just try
        if ((decoderInfo == null) || (decoderInfo.getTypeFile() == null)) {
            throw new IllegalArgumentException("No Toolbox typ file specified");
        }

        // (re)set everything to null for each parse
        // these calls could be removed since the parser is no longer 
        // used as a singleton
        lingTypeRecords.clear();
        participantOrder.clear();
        tierNameSet.clear();
        parentHash.clear();
        timeOrder.clear();
        timeSlots.clear();
        annotationRecords.clear();
        annotRecordToTierMap.clear();
        rootSlots.clear();
        mediaDescriptors.clear();

        annotId = 0;
        tsId = 0;

        // parse the file
        lastParsed = fileName;

        if ((decoderInfo != null) && (decoderInfo.getToolboxTypFile() != null)) {
            typFile = decoderInfo.getToolboxTypFile();
            userParticipantMarker = typFile.getParticipantMarker();
            if (elanParticipantLabel.equals(userParticipantMarker) || 
            		label_eudicoparticipant.equals(userParticipantMarker)) {
            	// always recognized/supported
            	userParticipantMarker = null;
            }
            tierTree = typFile.getMarkerTree();
        } else {
            tierTree = new DefaultMutableTreeNode(); //??
        }

        parseFile(tbFile);
    }

    private void parseFile(File toolboxFile) {
        Reader reader;
        BufferedReader bufRead = null;

        try {
            if (decoderInfo.isAllUnicode()) {
                reader = new InputStreamReader(new FileInputStream(toolboxFile),
                        "UTF-8");
                bufRead = new BufferedReader(reader);
            } else {
                reader = new InputStreamReader(new FileInputStream(toolboxFile),
                        "ISO-8859-1");
                bufRead = new BufferedReader(reader);
            }
        } catch (FileNotFoundException fne) {
            ServerLogger.LOG.severe("Toolbox file not found");

            return;
        } catch (UnsupportedEncodingException uee) {
            ServerLogger.LOG.severe("Encoding not supported"); //unlikely

            return;
        }
        
        int successiveBlocks = 0;

        String line = null;
        int lineCount = 0;

        boolean headerFound = false;
        ArrayList<String> blockLines = new ArrayList<String>();
        try {
            while ((line = bufRead.readLine()) != null) {
                line = line.trim(); // trim the line immediately after reading

                lineCount++;

                if ((lineCount <= 3) &&
                        ((line.indexOf("\\_sh v4.0") > -1) ||
                        (line.indexOf("\\_sh v3.0") > -1))) {
                    headerFound = true;

                    int lastSpaceIndex = line.trim().lastIndexOf(' ');

                    if (lastSpaceIndex > -1) {
                        String db = line.substring(lastSpaceIndex).trim();
                        ServerLogger.LOG.info("Database type in header: " + db);

                        // do something with the database type??
                        continue;
                    }
                }

                if ((lineCount > 3) && !headerFound) {
                    ServerLogger.LOG.warning(
                        "No Toolbox header found, no Toolbox file?");
                    // break;??
                    headerFound = true; //try to continue
                }

                if (line.startsWith("\\" + typFile.getRecordMarker())) {
                    if (blockLines.size() > 0) {
                    	int defectiveBlocks = this.noOfDefectiveBlocks;                    	
                    	processBlock(blockLines);
                        if(defectiveBlocks == noOfDefectiveBlocks){
                        	successiveBlocks++;
                        }
                        blockLines.clear();
                    }

                    blockLines.add(line);
                } else if (line.length() > 0) {
                    blockLines.add(line);
                }
            }

            // finish last block
            if (blockLines.size() > 0) {
                //processBlock(blockLines);
            	processLastBlock(blockLines);
            }
            
            bufRead.close();
            
            if(noOfDefectiveBlocks > 0){
            	if(successiveBlocks == 0 ){
            		throw  new mpi.eudico.server.corpora.clomimpl.abstr.ParseException("Import Failed: Might be due to toolbox type file mismatch with the toolbox file.");
            	} else{
            		ClientLogger.LOG.warning(toolboxFile.getName() + " : ToolBox file imported but it might miss some details, since few blocks were not imported(possibly due to mismatch btw the toolbox type file).");
            	}
            } else {
            	if(successiveBlocks == 0 ){
            		throw  new mpi.eudico.server.corpora.clomimpl.abstr.ParseException("Import Failed: Toolbox file or the type file might be invalid.");
            	} 
            }
            
            if (sortRecordsBeforeParsing) {
            	createAnnotationsFromCache();
            }
            
            calculateRootTimes();
        } catch (IOException ioe) {
            ServerLogger.LOG.severe("Error reading file: " + ioe.getMessage());
        }
    }

    /**
     * Special case, the last block might contain multiple ELAN media descriptor
     * lines. They are not handled correctly in processBlock (use of map or hash
     * allows only one media descriptor). Fix here without having to change the 
     * mechanism for each block.
     * 
     * @param lines the lines of the last block
     */
    private void processLastBlock(ArrayList<String> lines) {
        if ((lines == null) || (lines.size() == 0)) {
            return;
        }
        String line = null;
        String curMark = null;
        ArrayList<Integer> removables = new  ArrayList<Integer>(4);
        MediaDescriptor mediaDescriptor = null;
        
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            
            if (line.startsWith("\\")) {
                if ((line.length() > 1) && (line.charAt(1) != ' ') &&
                        (line.charAt(1) != '\t')) {
                    int space = line.indexOf(' ');
                    if (space <= 1) {
                        // only a backslash or a marker
                        curMark = line.substring(1);

                        // continue;
                    } else {
                        curMark = line.substring(1, space);
                    }
                    // always assume a consistent order? Yes...
                    if (curMark.equals(elanMediaURLLabel)) {
                    	if (space < line.length() && space > -1) {
                    		mediaDescriptor = new MediaDescriptor(line.substring(space + 1), null);
                    		mediaDescriptors.add(mediaDescriptor);
                    	}
                    	removables.add(i);
                    } else if (curMark.equals(elanMediaMIMELabel)) {
                    	if (space < line.length() && space > -1) {
	                    	if (mediaDescriptor != null) {
	                    		mediaDescriptor.mimeType = line.substring(space + 1);
	                    	}
                    	}
                    	removables.add(i);
                    } else if (curMark.equals(elanMediaExtractedLabel)) {
                    	if (space < line.length() && space > -1) {
	                    	if (mediaDescriptor != null) {
	                    		mediaDescriptor.extractedFrom = line.substring(space + 1);
	                    	}
                    	}
                    	removables.add(i);
                    } else if (curMark.equals(elanMediaOriginLabel)) {
                    	if (space < line.length() && space > -1) {
	                    	if (mediaDescriptor != null) {
	                            try {
	                                mediaDescriptor.timeOrigin = Long.parseLong(line.substring(space + 1));
	                            } catch (NumberFormatException nfe) {
	                            }
	                    	}
                    	}
                    	removables.add(i);
                    }
                }
            }
        }
        if (removables.size() > 0) {
        	for (int i = removables.size() - 1; i >= 0; i--) {
        		lines.remove(removables.get(i).intValue());
        	}
        }
        // finally process the last block
        processBlock(lines);
    }
    /**
     * Iterates over the lines and creates Toolbox lines depending on the type
     * of marker.  Markers that appear multiple times are appended to the same
     * Toolbox line. In case of interlinearized markers that are repeated
     * special care is taken to ensure  proper appending.
     *
     * @param lines the list of lines in the block
     */
    private void processBlock(ArrayList<String> lines) {
        if ((lines == null) || (lines.size() == 0)) {
            return;
        }

        String line = null;
        String curMark = null;
        String prevMark = null;
        String parMark;

        ToolboxLine tl = null;
        HashMap<String, ToolboxLine> tbLines = new LinkedHashMap<String, ToolboxLine>(12);

        // to cope with structures like this
        // \ref 001
        // \tx tochi' ij
        // \tx ney, jamcho
        // \m ney  jam -cho

        // mapping {marker, number of occurrences so far}
        HashMap<String, Integer> numLinesPerMarker = new HashMap<String, Integer>(12);

        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            if (line.startsWith("\\")) {
                if ((line.length() > 1) && (line.charAt(1) != ' ') &&
                        (line.charAt(1) != '\t')) {
                    int space = line.indexOf(' ');
                    prevMark = curMark;

                    if (space <= 1) {
                        // only a backslash or a marker
                        curMark = line.substring(1);

                        // continue;
                    } else {
                        curMark = line.substring(1, space);
                    }

                    storeLabelInOrder(curMark, prevMark);

                    if (typFile.isIncluded(curMark)) {
                        if (tbLines.containsKey(curMark)) {
                            if (typFile.isInterlinear(curMark)) {
                                // check number of occurrences
                                int numL = 0;

                                if (numLinesPerMarker.containsKey(curMark)) {
                                    numL = numLinesPerMarker.get(curMark);
                                }

                                int numPL = 0;
                                parMark = typFile.getParentMarker(curMark);

                                if (parMark != null) {
                                    if (numLinesPerMarker.containsKey(parMark)) {
                                        numPL = numLinesPerMarker.get(parMark);
                                    }

                                    int numDif = numPL - numL;

                                    if (numDif > 1) {
                                        for (int k = 1; k < numDif; k++) {
                                            tbLines.get(curMark).appendLine(" ");
                                        }
                                    }
                                }

                                if (line.length() > space && space > -1) {
                                    tbLines.get(curMark)
                                           .appendLine(line.substring(space +
                                            1));
                                } else {
                                    tbLines.get(curMark).appendLine(" ");
                                }

                                numLinesPerMarker.put(curMark, numL + 1);
                            } else { // non interlinear

                                if (line.length() > space && space > -1) {
                                    tbLines.get(curMark)
                                           .appendLine(line.substring(space +
                                            1));
                                } else {
                                    tbLines.get(curMark).appendLine(" ");
                                }
                            }
                        } else {
                            if (typFile.isInterlinear(curMark)) {
                                // check number of parent lines first
                                int numPL = 0;
                                parMark = typFile.getParentMarker(curMark);

                                if (parMark != null) {
                                    if (numLinesPerMarker.containsKey(parMark)) {
                                        numPL = numLinesPerMarker.get(parMark);
                                    }
                                }

                                if (numPL > 1) { // there are more than one parent lines, add empty lines
                                    tl = new InterlinearToolboxLine(curMark, " ");

                                    for (int k = 1; k < (numPL - 1); k++) {
                                        tl.appendLine(" ");
                                    }

                                    if (line.length() > space && space > -1) {
                                        tl.appendLine(line.substring(space + 1));
                                    } else {
                                        tl.appendLine(" ");
                                    }

                                    tbLines.put(curMark, tl);
                                    numLinesPerMarker.put(curMark, numPL);
                                } else { // zero or one parent line (zero would be a problem)

                                    if (line.length() > space && space > -1) {
                                        tl = new InterlinearToolboxLine(curMark,
                                                line.substring(space + 1));
                                    } else {
                                        tl = new InterlinearToolboxLine(curMark,
                                                " ");
                                    }

                                    tbLines.put(curMark, tl);
                                    numLinesPerMarker.put(curMark, 1);
                                }
                            } else {
                                if (line.length() > space && space > -1) {
                                    tl = new SimpleToolboxLine(curMark,
                                            line.substring(space + 1));
                                } else {
                                    tl = new SimpleToolboxLine(curMark, "");
                                }

                                tbLines.put(curMark, tl);
                                numLinesPerMarker.put(curMark, 1);
                            }
                        }
                    } else {
                        // check for the special elan labels
                        if (specialMarkers.contains(curMark)) {
                            if (line.length() > space && space > -1) {
                                tl = new SimpleToolboxLine(curMark,
                                        line.substring(space + 1));
                            } else {
                                tl = new SimpleToolboxLine(curMark, "");
                            }

                            tbLines.put(curMark, tl);
                            numLinesPerMarker.put(curMark, 1);
                        }
                    }
                } else {
                    if (typFile.isIncluded(curMark) ||
                            specialMarkers.contains(curMark)) {
                        // append the line	
                        if (tl != null) {
                            tl.appendLine(line);
                        }
                    }
                }
            } else {
                if (typFile.isIncluded(curMark) ||
                        specialMarkers.contains(curMark)) {
                    // append to previous line
                    if (tl != null) {
                        tl.appendLine(line);
                    }
                }
            }
        }

        ArrayList<ToolboxLine> allLines = new ArrayList<ToolboxLine>(tbLines.values());
        // establish parent-child relations
        tl = null;

        ToolboxLine tl2 = null;

        String parentMarker = null;

        for (int i = 0; i < allLines.size(); i++) {
            tl = allLines.get(i);

            if (tl.getMarkerName().equals(typFile.getRecordMarker())) {
                continue;
            }

            parentMarker = typFile.getParentMarker(tl.getMarkerName());

            if (parentMarker != null) {
                for (int j = 0; j < allLines.size(); j++) {
                    if (j == i) {
                        continue;
                    }

                    tl2 = allLines.get(j);

                    if (tl2.getMarkerName().equals(parentMarker)) {
                        tl.setParent(tl2);

                        break;
                    }

                    if (j == (allLines.size() - 1)) {
                        // reparent under the recordmarker??
                        ToolboxLine recLine = tbLines.get(typFile.getRecordMarker());

                        if (recLine != null) {
                            tl.setParent(recLine);
                            ServerLogger.LOG.warning("Parent for " +
                                tl.getMarkerName() +
                                " not found; reparenting under the record marker");
                        } else {
                            //?? serious error
                        }
                    }
                }
            }
        }

        // everything is set (?), create indices for subdivisions and next annotation records etc.
        tl = null;

        for (int i = 0; i < allLines.size(); i++) {
            tl = allLines.get(i);

            if (tl instanceof InterlinearToolboxLine) {
                ((InterlinearToolboxLine) tl).setCorrectForMultipleByteChars(decoderInfo.isRecalculateForCharBytes());
                ((InterlinearToolboxLine) tl).createIndices2();
            }
        }

        // could try to detect whether the indices are correct and retry without 
        // correction for the number of bytes per character: sometimes this correction is not necessary ??

        // correct indices if needed, based on parent-child relations
        // ensure that every begin index is also present on the child line
        Enumeration<?> breadthEn = tierTree.breadthFirstEnumeration();
        DefaultMutableTreeNode node1;
        DefaultMutableTreeNode node2;
        ToolboxMarker tm1;
        ToolboxMarker tm2;
        ToolboxLine tbl1;
        ToolboxLine tbl2;

        while (breadthEn.hasMoreElements()) {
            node1 = (DefaultMutableTreeNode) breadthEn.nextElement();

            if ((node1.getParent() != null) && (node1.getParent() != tierTree)) {
                tm1 = (ToolboxMarker) node1.getUserObject();

                if (typFile.isInterlinear(tm1.getMarker())) {
                    node2 = (DefaultMutableTreeNode) node1.getParent();

                    if (node2.getUserObject() instanceof ToolboxMarker) {
                        tm2 = (ToolboxMarker) node2.getUserObject();

                        if (typFile.isInterlinear(tm2.getMarker())) {
                            tbl1 = (ToolboxLine) tbLines.get(tm1.getMarker()); // child
                            tbl2 = (ToolboxLine) tbLines.get(tm2.getMarker()); //parent

                            if (tbl1 instanceof InterlinearToolboxLine &&
                                    tbl2 instanceof InterlinearToolboxLine) {
                                //checkIndices((InterlinearToolboxLine) tbl2, (InterlinearToolboxLine) tbl1);
                                addMissingIndicesOnChild((InterlinearToolboxLine) tbl2,
                                    (InterlinearToolboxLine) tbl1);
                            }
                        }
                    }
                }
            }
        } // end insertion on child markers

        /*
           // i.e. insert annotations on a parent if there is a child without a parent given the type of
           // parent-child relations (subdivision or association)
           // start iteration with the leaf nodes
           Enumeration depthEn = tierTree.depthFirstEnumeration();
           DefaultMutableTreeNode node1, node2;
           ToolboxMarker tm1, tm2;
           ToolboxLine tbl1, tbl2;
        
           while (depthEn.hasMoreElements()) {
               node1 = (DefaultMutableTreeNode) depthEn.nextElement();
               if (node1.isLeaf()) {
                   if (node1.getUserObject() instanceof ToolboxMarker) {
                       tm1 = (ToolboxMarker) node1.getUserObject();
                       if (typFile.isInterlinear(tm1.getMarker())) {
                           node2 = (DefaultMutableTreeNode) node1.getParent();
                           if (node2 != null && node2.getUserObject() instanceof ToolboxMarker) {
                               tm2 =  (ToolboxMarker) node2.getUserObject();
                               if (typFile.isInterlinear(tm2.getMarker())) {
                                   // get the ToolboxLine + parent
                                   tbl1 = (ToolboxLine) tbLines.get(tm1.getMarker());// child
                                   tbl2 = (ToolboxLine) tbLines.get(tm2.getMarker());//parent
                                   if (tbl1 instanceof InterlinearToolboxLine && tbl2 instanceof InterlinearToolboxLine) {
                                       harmonizeLines((InterlinearToolboxLine)tbl2, (InterlinearToolboxLine)tbl1);
                                       //traverse up
                                       uploop:
                                       while (node2.getParent() != null) {
                                           node1 = node2;
                                           tm1 = tm2;
                                           tbl1 = tbl2;
                                           node2 = (DefaultMutableTreeNode) node1.getParent();
                                           if (node2 != null && node2.getUserObject() instanceof ToolboxMarker) {
                                               tm2 =  (ToolboxMarker) node2.getUserObject();
                                               if (typFile.isInterlinear(tm2.getMarker())) {
                                                   tbl2 = (ToolboxLine) tbLines.get(tm2.getMarker());//parent
                                                   if (tbl1 instanceof InterlinearToolboxLine && tbl2 instanceof InterlinearToolboxLine) {
                                                       //harmonizeLines((InterlinearToolboxLine)tbl2, (InterlinearToolboxLine)tbl1);
                                                   }
                                               } else {
                                                   break uploop;
                                               }
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
           } // end of harmonization of interlinear markers, bottom up
         */

        // create annotation records
        /*
           for (int i = 0; i < allLines.size(); i++) {
               tl = allLines.get(i);
               System.out.println("M: " + tl.getMarkerName());
               if (tl instanceof InterlinearToolboxLine) {
                   InterlinearToolboxLine itl = (InterlinearToolboxLine) tl;
                   int k = itl.getNumberOfWords();
                   for (int j = 0; j < itl.getStartIndices().length; j++) {
                       System.out.print(itl.getWordAtIndex(itl.getStartIndices()[j], 0).word + " (" + itl.getStartIndices()[j] +
                               " l: " + itl.getWordAtIndex(itl.getStartIndices()[j], 0).calcW + ") ");
                   }
                   System.out.println();
               } else if (tl instanceof SimpleToolboxLine) {
                   System.out.println(((SimpleToolboxLine) tl).getLine());
               }
           }
           System.out.println("End of Block\n\n");
         */
        if (!sortRecordsBeforeParsing) {
        	createAnnotations(allLines);
        } else {
        	cacheBlock(allLines);
        }
    }

    /**
     * Creates annotation records and related records for one record.
     *
     * @param allLines a list of Toolbox lines
     */
    private void createAnnotations(List<ToolboxLine> allLines) {
        if ((allLines == null) || (allLines.size() == 0)) {
            return;
        }
        
        // first get the record marker, begin time end time markers, participant markers,
        // other special markers
        ToolboxLine tl;
        SimpleToolboxLine stl = null;
        SimpleToolboxLine rootTl = null;
        String speaker = UNKNOWN;
        long t0 = -1;
        long t1 = -1;
        ArrayList<SimpleToolboxLine> removables = new ArrayList<SimpleToolboxLine>();

        MediaDescriptor mediaDescriptor = null;

        for (int i = 0; i < allLines.size(); i++) {
            tl = allLines.get(i);

            if (tl instanceof SimpleToolboxLine) {
                stl = (SimpleToolboxLine) tl;

                if (stl.getMarkerName().equals(typFile.getRecordMarker())) {
                    if (decoderInfo.isTimeInRefMarker()) {
                        t0 = timeForString(stl.getLine());
                    }

                    rootTl = stl;
                } else if (stl.getMarkerName().equals(elanBeginLabel) ||
                        stl.getMarkerName().equals(label_eudicot0)) {
                    t0 = timeForString(stl.getLine());
                    removables.add(stl);
                } else if (stl.getMarkerName().equals(elanEndLabel) ||
                        stl.getMarkerName().equals(label_eudicot1)) {
                    t1 = timeForString(stl.getLine());
                    removables.add(stl);
                } else if (stl.getMarkerName().equals(elanParticipantLabel) ||
                        stl.getMarkerName().equals(label_eudicoparticipant) || 
                        stl.getMarkerName().equals(userParticipantMarker)) {
                    speaker = stl.getLine();
                    removables.add(stl);
                }
                // media url related markers 
                else if (stl.getMarkerName().equals(elanMediaURLLabel)) {
                    //if (mediaDescriptor != null) {
                    //	mediaDescriptors.add(mediaDescriptor);	
                    //}
                    mediaDescriptor = new MediaDescriptor(stl.getLine(), null);
                    mediaDescriptors.add(mediaDescriptor);
                    removables.add(stl);

                    continue;
                } else if (stl.getMarkerName().equals(elanMediaMIMELabel)) {
                    if (mediaDescriptor != null) {
                        mediaDescriptor.mimeType = stl.getLine();
                    }

                    removables.add(stl);

                    continue;
                } else if (stl.getMarkerName().equals(elanMediaExtractedLabel)) {
                    if (mediaDescriptor != null) {
                        mediaDescriptor.extractedFrom = stl.getLine();
                    }

                    removables.add(stl);

                    continue;
                } else if (stl.getMarkerName().equals(elanMediaOriginLabel)) {
                    if (mediaDescriptor != null) {
                        try {
                            mediaDescriptor.timeOrigin = Long.parseLong(stl.getLine());
                        } catch (NumberFormatException nfe) {
                        }
                    }

                    removables.add(stl);

                    continue;
                }
            }
        }
        
        if(rootTl == null){
        	noOfDefectiveBlocks++;
        	System.out.println(" defective blocks : " + noOfDefectiveBlocks);
        	if(noOfDefectiveBlocks >= 5){
        		throw  new mpi.eudico.server.corpora.clomimpl.abstr.ParseException("defective blocks");
        	}               	
        } else {
        	if (!participantOrder.contains(speaker)) {
                participantOrder.add(speaker);
            }

            tierNameSet.add(rootTl.getMarkerName() + AT + speaker);

            allLines.removeAll(removables);

            // create a tree of the remaining elements, containing simple toolbox lines and toolbox words
            List<DefaultMutableTreeNode> allNodes = new ArrayList<DefaultMutableTreeNode>(allLines.size());
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootTl);

            for (int i = 0; i < allLines.size(); i++) {
                tl = allLines.get(i);

                if (tl != rootTl) {
                    allNodes.add(new DefaultMutableTreeNode(tl));
                }

                tierNameSet.add(tl.getMarkerName() + AT + speaker);
            }

            DefaultMutableTreeNode n1;
            DefaultMutableTreeNode n2;
            ToolboxLine tl2;

            for (int i = 0; i < allNodes.size(); i++) {
                n1 = allNodes.get(i);
                tl = (ToolboxLine) n1.getUserObject();

                if (tl.getParent() == rootTl) {
                    rootNode.add(n1);
                    parentHash.put((tl.getMarkerName() + AT + speaker),
                        (rootTl.getMarkerName() + AT + UNKNOWN));
                    	//	(rootTl.getMarkerName() + AT + speaker));

                    continue;
                }

                for (int j = 0; j < allNodes.size(); j++) {
                    n2 = allNodes.get(j);
                    tl2 = (ToolboxLine) n2.getUserObject();

                    if (tl2 == tl.getParent()) {
                        n2.add(n1);
                        parentHash.put((tl.getMarkerName() + AT + speaker),
                            (tl2.getMarkerName() + AT + UNKNOWN));
                        	//	(tl2.getMarkerName() + AT + speaker));

                        break;
                    }
                }
            }

            // all line-relations set, add the individual words 
            n1 = null;
            n2 = null;

            for (int i = 0; i < rootNode.getChildCount(); i++) {
                n1 = (DefaultMutableTreeNode) rootNode.getChildAt(i);

                //if (n1.isLeaf()) {
                //	continue;
                //}
                if (n1.getUserObject() instanceof InterlinearToolboxLine) {
                    createWordNodes(rootNode, n1);
                } else if (n1.getUserObject() instanceof SimpleToolboxLine) {
                    // check children to be interlinearized
                    Enumeration<?> depEnum = n1.depthFirstEnumeration();

                    while (depEnum.hasMoreElements()) {
                        n2 = (DefaultMutableTreeNode) depEnum.nextElement();

                        if (n2.getUserObject() instanceof InterlinearToolboxLine &&
                                ((DefaultMutableTreeNode) n2.getParent()).getUserObject() instanceof SimpleToolboxLine) {
                            createWordNodes((DefaultMutableTreeNode) n2.getParent(),
                                n2);
                        }
                    }
                }
            }

            //System.out.println("Words aligned....");
            String rootTierName = rootTl.getMarkerName() + AT + speaker;
            AnnotationRecord annRec = new AnnotationRecord();

            annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
            annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);

            long beginTSId = tsId++;
            long endTSId = tsId++;
            annRec.setBeginTimeSlotId(TS_ID_PREFIX + Long.toString(beginTSId));
            annRec.setEndTimeSlotId(TS_ID_PREFIX + Long.toString(endTSId));

            if (decoderInfo.isScrubAnnotations()) {
            	annRec.setValue(scrubAnnotation(rootTl.getLine()));// should root tier be scrubbed?
            } else {
            	annRec.setValue(rootTl.getLine());
            }

            if ((rootSlots.size() == 0) && (t0 < 0)) {
                t0 = 0; // initialize the first time value to 0, unless otherwise specified
            }

            // time info
            long[] begin = { beginTSId, t0 };
            long[] end = { endTSId, t1 };

            timeSlots.add(begin);
            timeSlots.add(end);

            timeOrder.add(begin);
            timeOrder.add(end);

            rootSlots.add(begin);
            rootSlots.add(end);

            annotationRecords.add(annRec);

            addRecordToTierMap(annRec, rootTierName);

            createChildAnnotations(rootNode, annRec, speaker);
        }

        
    }

    /**
     * See comments on createWordNodes about the special status of
     * InterlinearToolbox nodes.
     *
     * @param parNode the parent node in the tree
     * @param parentRecord the parent record, for referencing
     * @param speaker the speaker name
     */
    private void createChildAnnotations(DefaultMutableTreeNode parNode,
        AnnotationRecord parentRecord, String speaker) {
        DefaultMutableTreeNode chNode;
        Object userObj;
        ToolboxWord tw;
        AnnotationRecord annRec;

        //ArrayList<DefaultMutableTreeNode> wordNodes = new ArrayList<DefaultMutableTreeNode>();
        LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> wordsMap = null;

        for (int i = 0; i < parNode.getChildCount(); i++) {
            chNode = (DefaultMutableTreeNode) parNode.getChildAt(i);
            userObj = chNode.getUserObject();

            if (userObj instanceof InterlinearToolboxLine) {
                if (chNode.getChildCount() > 0) {
                    createChildAnnotations(chNode, parentRecord, speaker);
                } else {
                    continue;
                }
            }

            if (userObj instanceof SimpleToolboxLine) {
                SimpleToolboxLine stl = (SimpleToolboxLine) userObj;
                String tierName = stl.getMarkerName() + AT + speaker;
                annRec = new AnnotationRecord();

                annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
                annRec.setAnnotationType(AnnotationRecord.REFERENCE);
                if (decoderInfo.isScrubAnnotations()) {
                	annRec.setValue(scrubAnnotation(stl.getLine()));
                } else {
                	annRec.setValue(stl.getLine());
                }
                annRec.setReferredAnnotId(parentRecord.getAnnotationId());
                annotationRecords.add(annRec);

                addRecordToTierMap(annRec, tierName);
                createChildAnnotations(chNode, annRec, speaker);
            } else if (userObj instanceof ToolboxWord) {
                tw = (ToolboxWord) userObj;

                if (wordsMap == null) {
                    wordsMap = new LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>>();
                }

                if (wordsMap.containsKey(tw.getMarkerName())) {
                    wordsMap.get(tw.getMarkerName()).add(chNode);
                } else {
                    wordsMap.put(tw.getMarkerName(),
                        new ArrayList<DefaultMutableTreeNode>());
                    wordsMap.get(tw.getMarkerName()).add(chNode);
                }
            }
        }

        if (wordsMap != null) {
            Iterator<String> markIt = wordsMap.keySet().iterator();
            String curMarker;
            String curTier;
            AnnotationRecord prevRec = null;
            ArrayList<DefaultMutableTreeNode> curWords;

            while (markIt.hasNext()) {
                prevRec = null;
                curMarker = markIt.next();
                curTier = curMarker + AT + speaker;
                curWords = wordsMap.get(curMarker);

                int type = typFile.getStereoType(curMarker);
                boolean alignable = ((type == Constraint.INCLUDED_IN) ||
                    (type == Constraint.TIME_SUBDIVISION));

                for (int i = 0; i < curWords.size(); i++) {
                    chNode = curWords.get(i);
                    tw = (ToolboxWord) chNode.getUserObject();
                    annRec = new AnnotationRecord();

                    annRec.setAnnotationId(ANN_ID_PREFIX + annotId++);
                    if (decoderInfo.isScrubAnnotations()) {
                    	annRec.setValue(scrubAnnotation(tw.word));
                    } else {
                    	annRec.setValue(tw.word);
                    }

                    if (alignable) {
                        annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);

                        //annRec.setReferredAnnotId(parentRecord.getAnnotationId());// needed?
                        //if (type == Constraint.INCLUDED_IN) {// included in does not support unaligned slots
                        // create 2 new time slots, what about the time values?
                        // hier... solve...
                        //}
                        if (i == 0) {
                            annRec.setBeginTimeSlotId(parentRecord.getBeginTimeSlotId());

                            if (i < (curWords.size() - 1)) {
                                long endTsId = tsId++;
                                annRec.setEndTimeSlotId(TS_ID_PREFIX +
                                    Long.toString(endTsId));

                                long[] end = { endTsId, TimeSlot.TIME_UNALIGNED };
                                timeSlots.add(end);
                                // add to time order in the correct position
                                insertTimeSlot(end,
                                    parentRecord.getBeginTimeSlotId());
                            } else {
                                annRec.setEndTimeSlotId(parentRecord.getEndTimeSlotId());
                            }
                        } else if (i == (curWords.size() - 1)) {
                            annRec.setEndTimeSlotId(parentRecord.getEndTimeSlotId());
                            annRec.setBeginTimeSlotId(prevRec.getEndTimeSlotId()); // there must be a prev. annotation
                        } else {
                            // new end slot, begin slot is the same as end slot of previous annotation
                            annRec.setBeginTimeSlotId(prevRec.getEndTimeSlotId());

                            long endTsId = tsId++;
                            annRec.setEndTimeSlotId(TS_ID_PREFIX +
                                Long.toString(endTsId));

                            long[] end = { endTsId, TimeSlot.TIME_UNALIGNED };
                            timeSlots.add(end);
                            // add to time order in the correct position
                            insertTimeSlot(end, prevRec.getEndTimeSlotId());
                        }
                    } else {
                        annRec.setAnnotationType(AnnotationRecord.REFERENCE);
                        annRec.setReferredAnnotId(parentRecord.getAnnotationId());

                        if ((prevRec != null) &&
                                (type == Constraint.SYMBOLIC_SUBDIVISION)) {
                            annRec.setPreviousAnnotId(prevRec.getAnnotationId());
                        }
                    }

                    prevRec = annRec;

                    annotationRecords.add(annRec);

                    addRecordToTierMap(annRec, curTier);
                    // hier... create children right away or do that in a second run? need to store node and annrec then
                    createChildAnnotations(chNode, annRec, speaker);
                }
            }
        }
    }

    /**
     * Creates nodes for individual words of interlinearized lines. The list of
     * nodes of the parent marker/node are passed as an argument. Is executed
     * recursively. Private method, casts should be save.
     *
     * @param parNodes a list of nodes per word/unit of the parent marker
     * @param chLine a node containing a {@link InterlinearToolboxLine}
     *        containing words that need to be attached to a parent node
     */
    private void createWordNodes(List<DefaultMutableTreeNode> parNodes,
        DefaultMutableTreeNode chLine /*, InterlinearToolboxLine parLine*/) {
        List<DefaultMutableTreeNode> chNodes = new ArrayList<DefaultMutableTreeNode>();
        InterlinearToolboxLine tbl = (InterlinearToolboxLine) chLine.getUserObject();
        ToolboxWord tw = null;
        ToolboxWord parw = null;
        DefaultMutableTreeNode parNode;
        DefaultMutableTreeNode nextNode;

        for (int i = 0; i < tbl.getStartIndices().length; i++) {
            tw = tbl.getWordAtIndex(tbl.getStartIndices()[i], 0);

            if (tw != null) {
                // find a parent, this could be done more efficient
                for (int j = 0; j < parNodes.size(); j++) {
                    parNode = parNodes.get(j);
                    parw = (ToolboxWord) parNode.getUserObject();

                    //note: this fails if there are gaps!
                    if (((tw.calcX >= parw.calcX) &&
                            ((tw.calcX + tw.calcW) <= (parw.calcX + parw.calcW))) ||
                            ((j == (parNodes.size() - 1)) &&
                            (tw.calcX >= parw.calcX))) {
                        // parent found
                        if (typFile.isSubdivision(tw.getMarkerName())) {
                            nextNode = new DefaultMutableTreeNode(tw);
                            parNode.add(nextNode);
                            chNodes.add(nextNode);
                        } else {
                            // check if there is already a child
                            if (parNode.getChildCount() == 0) {
                                nextNode = new DefaultMutableTreeNode(tw);
                                parNode.add(nextNode);
                                chNodes.add(nextNode);
                            } else {
                                // check if one of the other children is of the same marker as this one
                                // hier...
                                //System.out.println("There is already a child for this unit: " + parw.word + " on: " + parw.getMarkerName());
                                // skip, or insert a node on the parent's parent?
                                // add anyway for the time being
                                nextNode = new DefaultMutableTreeNode(tw);
                                parNode.add(nextNode);
                                chNodes.add(nextNode);
                            }
                        }

                        break;
                    }

                    if (j == (parNodes.size() - 1)) {
                        ServerLogger.LOG.warning("Could not add child: " +
                            tw.getMarkerName() + " - " + tw.word);

                        if (i < parNodes.size()) { // try the one with the same index, maybe only with symb. association
                            parNode = parNodes.get(i);
                            parw = (ToolboxWord) parNode.getUserObject();

                            int maxX1 = Math.max(tw.calcX, parw.calcX);
                            int minX2 = Math.min(tw.calcX + tw.calcW,
                                    parw.calcX + parw.calcW);
                            int curO = minX2 - maxX1;

                            if (curO > 0) {
                                // there is some overlap
                                // check overlap with previous and next parent words
                                ToolboxWord tmpw = null;
                                DefaultMutableTreeNode tmpNode;
                                int prevO = -1;
                                int prevX = 0;
                                int nextO = -1;
                                int nextX = 0;

                                if (i > 0) {
                                    tmpNode = parNodes.get(i - 1);
                                    tmpw = (ToolboxWord) tmpNode.getUserObject();
                                    prevX = tmpw.calcX;
                                    prevO = Math.min(tw.calcX + tw.calcW,
                                            tmpw.calcX + tmpw.calcW) -
                                        Math.max(tw.calcX, tmpw.calcX);
                                }

                                if (i < (parNodes.size() - 1)) {
                                    tmpNode = parNodes.get(i + 1);
                                    tmpw = (ToolboxWord) tmpNode.getUserObject();
                                    nextX = tmpw.calcX;
                                    nextO = Math.min(tw.calcX + tw.calcW,
                                            tmpw.calcX + tmpw.calcW) -
                                        Math.max(tw.calcX, tmpw.calcX);
                                }

                                int bestMatch = i;

                                if ((prevO > curO) &&
                                        (Math.abs(prevX - tw.calcX) < Math.abs(parw.calcX -
                                            tw.calcX))) {
                                    bestMatch = i - 1;
                                }

                                if ((nextO > curO) &&
                                        (Math.abs(nextX - tw.calcX) < Math.abs(parw.calcX -
                                            tw.calcX))) {
                                    bestMatch = i + 1;
                                }

                                if (bestMatch != i) {
                                    parNode = parNodes.get(bestMatch);
                                }

                                parw = (ToolboxWord) parNode.getUserObject();

                                nextNode = new DefaultMutableTreeNode(tw);
                                parNode.add(nextNode);
                                chNodes.add(nextNode);
                                ServerLogger.LOG.info("Added child: " +
                                    tw.getMarkerName() + " - " + tw.word +
                                    " to parent: " + parw.word);
                            } else { // try all parents and find best match based on overlap and x value

                                int bestMatch = -1;
                                curO = -1;

                                int curdifX = Integer.MAX_VALUE;
                                int bestO = curO;
                                int bestdifX = curdifX;

                                for (int k = 0; k < parNodes.size(); k++) {
                                    parNode = parNodes.get(k);
                                    parw = (ToolboxWord) parNode.getUserObject();

                                    curO = Math.min(tw.calcX + tw.calcW,
                                            parw.calcX + parw.calcW) -
                                        Math.max(tw.calcX, parw.calcX);
                                    curdifX = Math.abs(parw.calcX - tw.calcX);

                                    if ((curO >= bestO) &&
                                            (curdifX < bestdifX)) {
                                        bestO = curO;
                                        bestdifX = curdifX;
                                        bestMatch = k;
                                    }
                                }

                                if (bestMatch > -1) {
                                    parNode = parNodes.get(bestMatch);
                                    parw = (ToolboxWord) parNode.getUserObject();

                                    nextNode = new DefaultMutableTreeNode(tw);
                                    parNode.add(nextNode);
                                    chNodes.add(nextNode);
                                    ServerLogger.LOG.info("Added child 2: " +
                                        tw.getMarkerName() + " - " + tw.word +
                                        " to parent: " + parw.word);
                                }
                            }
                        } else { // can not base on index, loop over the candidate parents
                                 // same method as the "else" part above

                            int bestMatch = -1;
                            int curO = -1;
                            int curdifX = Integer.MAX_VALUE;
                            int bestO = curO;
                            int bestdifX = curdifX;

                            for (int k = 0; k < parNodes.size(); k++) {
                                parNode = parNodes.get(k);
                                parw = (ToolboxWord) parNode.getUserObject();

                                curO = Math.min(tw.calcX + tw.calcW,
                                        parw.calcX + parw.calcW) -
                                    Math.max(tw.calcX, parw.calcX);
                                curdifX = Math.abs(parw.calcX - tw.calcX);

                                if ((curO >= bestO) && (curdifX < bestdifX)) {
                                    bestO = curO;
                                    bestdifX = curdifX;
                                    bestMatch = k;
                                }
                            }

                            if (bestMatch > -1) {
                                parNode = parNodes.get(bestMatch);
                                parw = (ToolboxWord) parNode.getUserObject();

                                nextNode = new DefaultMutableTreeNode(tw);
                                parNode.add(nextNode);
                                chNodes.add(nextNode);
                                ServerLogger.LOG.info("Added child 3: " +
                                    tw.getMarkerName() + " - " + tw.word +
                                    " to parent: " + parw.word);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < chLine.getChildCount(); i++) {
            nextNode = (DefaultMutableTreeNode) chLine.getChildAt(i);
            createWordNodes(chNodes, nextNode);
        }
    }

    /**
     * Creates nodes for individual words of an interlinearized line. The
     * parent marker/node is passed as an argument. The child nodes will
     * become children of the node the interlinear line is on. The next level
     * of children are added to these child nodes. There will be one
     * placeholder node for  the words/units of each marker that is the first
     * marker in an interlinearized block. Private method, casts should be
     * save.
     *
     * @param parNode the parent tree node
     * @param chLine an {@link InterlinearToolboxLine} containing words that
     *        need to be attached to a parent node
     */
    private void createWordNodes(DefaultMutableTreeNode parNode,
        DefaultMutableTreeNode chLine) {
        List<DefaultMutableTreeNode> chNodes = new ArrayList<DefaultMutableTreeNode>();
        InterlinearToolboxLine tbl = (InterlinearToolboxLine) chLine.getUserObject();
        ToolboxWord tw = null;
        DefaultMutableTreeNode nextNode;

        for (int i = 0; i < tbl.getStartIndices().length; i++) {
            tw = tbl.getWordAtIndex(tbl.getStartIndices()[i], 0);

            if (tw != null) {
                nextNode = new DefaultMutableTreeNode(tw);
                chNodes.add(nextNode);
            }
        }

        // add child nodes recursively
        for (int j = 0; j < chLine.getChildCount(); j++) {
            nextNode = (DefaultMutableTreeNode) chLine.getChildAt(j);

            //if (nextNode.isLeaf()) {
            //	continue;
            //}
            if (nextNode.getUserObject() instanceof InterlinearToolboxLine) {
                createWordNodes(chNodes, nextNode /*, tbl*/);
            }
        }

        // now add the childnodes
        for (int i = 0; i < chNodes.size(); i++) {
            //chLine.add(chNodes.get(i));
            parNode.add(chNodes.get(i)); // hier... what is correct??
        }
    }

    // re-implement!
    private void harmonizeLines(InterlinearToolboxLine parentLine,
        InterlinearToolboxLine childLine) {
        if ((parentLine != null) && (childLine != null)) {
            int parNumWords = parentLine.getNumberOfWords();
            int chNumWords = childLine.getNumberOfWords();

            if ((parNumWords == 0) && (chNumWords == 0)) {
                return;
            } else {
                boolean isSubDiv = typFile.isSubdivision(childLine.getMarkerName());
                int[] parInd = parentLine.getStartIndices();
                int[] chInd = childLine.getStartIndices();
                int i = 0;
                int j = 0;

                for (; i < parInd.length; i++) {
                    for (; j < chInd.length; j++) {
                        if ((i == j) && (parInd[i] == chInd[j])) {
                            j++;

                            break;
                        } else if ((j > i) && !isSubDiv &&
                                (chInd[j] > (parInd[i] + 1))) {
                            // conditionally insert an empty parent "word"
                            /*
                               int end = 0;
                               if (j < chInd.length - 1) {
                                   end =  chInd[j + 1] - 1;
                               } else {
                                   end = childLine.getWordAtIndex(chInd[j], 0).calcW;
                               }
                               System.out.println("Insert word: " + parentLine.getMarkerName() + " " + chInd[j]);
                               boolean insSuccess = parentLine.conditionallyInsertWord(chInd[j], end);
                               System.out.println("Insert word succes: " + insSuccess);
                               if (insSuccess) {
                                   i++;
                               }
                               break;
                             */
                        } else if ((i == j) && !isSubDiv &&
                                (chInd[j] > parInd[i])) {
                            // fill a gap on the child. or at least adjust the last child's width
                            int end = 0;

                            if (i < (parInd.length - 1)) {
                                end = parInd[i + 1] - 1;
                            } else {
                                end = parentLine.getWordAtIndex(parInd[i], 0).calcW;
                            }

                            boolean succ = childLine.conditionallyInsertWord(parInd[i],
                                    end);
                            ServerLogger.LOG.info("Inserted word on child: " +
                                childLine.getMarkerName() + parInd[i] + " " +
                                succ);

                            if (succ) {
                                //harmonizeLines(parentLine, childLine);
                                //return;
                                j++;

                                //break;
                            }
                        } else if (chInd[j] > parInd[i]) {
                            //break;//??
                        } // what else??
                    }
                }
            }
        }
    }

    /**
     * Adds empty words on the child marker at indices that are present in the
     * parent but not in the child line. These indices are not in the child in
     * case of empty "words" or positions on the child.
     *
     * @param parentLine the parent line
     * @param childLine the child line
     */
    private void addMissingIndicesOnChild(InterlinearToolboxLine parentLine,
        InterlinearToolboxLine childLine) {
        if ((parentLine != null) && (childLine != null)) {
            int[] parInd = parentLine.getStartIndices();
            int[] chInd = childLine.getStartIndices();

            //parentloop:
            for (int i = 0; i < parInd.length; i++) {
                for (int j = 0; j < chInd.length; j++) {
                    if (parInd[i] == chInd[j]) {
                        //continue parentloop;
                        break;
                    }

                    if (chInd[j] > parInd[i]) {
                        // we missed an index
                        int begin = parInd[i];
                        int end = begin;

                        if (i < (parInd.length - 1)) {
                            end = parInd[i + 1] - 1;
                        } else {
                            end = parInd[i] +
                                parentLine.getWordAtIndex(parInd[i], 0).calcW;
                        }

                        boolean succ = childLine.conditionallyInsertWord(parInd[i],
                                end);

                        if (succ) {
                            ServerLogger.LOG.info("Inserted on " +
                                childLine.getMarkerName() + " at " + begin);
                            chInd = childLine.getStartIndices();
                        } else {
                            ServerLogger.LOG.info("Could not insert on " +
                                childLine.getMarkerName() + " at " + begin);
                        }

                        break;
                    }
                }
            }
        }
    }

    private void checkIndices(InterlinearToolboxLine parentLine,
        InterlinearToolboxLine childLine) {
        if ((parentLine != null) && (childLine != null)) {
            int[] parInd = parentLine.getStartIndices();
            int[] chInd = childLine.getStartIndices();

            //parentloop:
            for (int i = 0; i < parInd.length; i++) {
                for (int j = 0; j < chInd.length; j++) {
                    if (parInd[i] == chInd[j]) {
                        //continue parentloop;
                        break;
                    }

                    if (chInd[j] > parInd[i]) {
                        if (j == 0) {
                            System.out.println("pb: " + parInd[i] + " rw: " +
                                parentLine.getWordAtIndex(parInd[i], 0).realW +
                                " cw: " +
                                parentLine.getWordAtIndex(parInd[i], 0).calcW);
                            System.out.println("cb: " + chInd[j] + " rw: " +
                                childLine.getWordAtIndex(chInd[j], 0).realW +
                                " cw: " +
                                childLine.getWordAtIndex(chInd[j], 0).calcW);
                        } else {
                            System.out.println("pb: " + parInd[i] + " rw: " +
                                parentLine.getWordAtIndex(parInd[i], 0).realW +
                                " cw: " +
                                parentLine.getWordAtIndex(parInd[i], 0).calcW);
                            System.out.println("cb: " + chInd[j - 1] + " rw: " +
                                childLine.getWordAtIndex(chInd[j - 1], 0).realW +
                                " cw: " +
                                childLine.getWordAtIndex(chInd[j - 1], 0).calcW);
                        }

                        break;
                    }
                }
            } // parentloop
        }
    }

    /**
     * Tries to keep a list of markers in the order they appear in the file.
     * Since each block may not  contain all markers, the result may not
     * always be satisfactory
     *
     * @param label the label to add
     * @param prevLabel the previous label in the block
     */
    private void storeLabelInOrder(String label, String prevLabel) {
        if ((label == null) || (label.length() == 0) ||
                markerOrder.contains(label)) {
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
     * Converts a string to milliseconds.
     *
     * @param time the string
     *
     * @return the millisecond value
     */
    private long timeForString(String time) {
        if (time == null) {
            return -1;
        }

        long t0 = -1;

        try {
            double d = Double.parseDouble(time);
            d *= 1000d;

            if (d == -1000) { // correct unaligned, unlikely
                d = -1;
            }

            t0 = (long) d;
        } catch (NumberFormatException nfe) {
            t0 = toMilliSeconds(time);
        }

        return t0;
    }

    /**
     * Adds a record to the list of records of a specified tier. If the list
     * does not exist yet, it is created.
     *
     * @param annRec the record
     * @param tierName the tier name
     */
    private void addRecordToTierMap(AnnotationRecord annRec, String tierName) {
        annotRecordToTierMap.put(annRec, tierName);

        if (tierNameToAnnRecordMap.containsKey(tierName)) {
            tierNameToAnnRecordMap.get(tierName).add(annRec);
        } else {
            ArrayList<AnnotationRecord> ar = new ArrayList<AnnotationRecord>();
            ar.add(annRec);
            tierNameToAnnRecordMap.put(tierName, ar);
        }
    }

    /**
     * Inserts the time slot, a combination of id and time value, after the
     * specified index, or adds to the end.
     *
     * @param ts the time slot information
     * @param afterId the index of the slot after which to insert
     */
    private void insertTimeSlot(long[] ts, String afterId) {
        // add to time order in the correct position
        long afterIndex = -1;

        try {
            afterIndex = Long.valueOf(afterId.substring(TS_ID_PREFIX.length()))
                             .longValue();
        } catch (NumberFormatException nfe) {
        }

        int toIndex = timeOrder.size();
        long[] loopTs;

        for (int j = 0; j < timeOrder.size(); j++) {
            loopTs = (long[]) timeOrder.get(j);

            if (loopTs[0] == afterIndex) {
                toIndex = j;

                break;
            }
        }

        if (toIndex > (timeOrder.size() - 1)) {
            timeOrder.add(ts);
        } else {
            timeOrder.add(toIndex + 1, ts);
        }
    }

    /**
     * Loops over the list of root slots and finds intervals (size >= 1) of
     * consecutive  unaligned slots (time == -1). Begin and end index of such
     * interval are passed to calculateSlotsInInterval(), where appropriate
     * times for each unaligned slot is  calculated/interpolated. The list of
     * slots contains alternating beginslot and endslot objects,  even index
     * is beginslot, odd index is endslot.
     *
     * @see #calculateSlotsInInterval(int, int)
     */
    private void calculateRootTimes() {
        long[] slot;
        int firstUAIndex = -1;

        for (int i = 0; i < rootSlots.size(); i++) {
            slot = (long[]) rootSlots.get(i);

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

            if ((i == (rootSlots.size() - 1)) && (firstUAIndex != -1)) {
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
     * to previous beginslot time + preferred block duration, whichever is
     * smaller) - all unaligned (except first): make all intervals the
     * preferred block duration - more than one unaligned slots: calculate
     * total available time for the slots (next aligned slot value - previous
     * aligned slot value), calculate the number of intervals that are
     * involved and calculate the amount of ms for each 'annotation' (delta =
     * ms / numIntervals). If this amount is greater than the pref. block
     * duration, use this duration instead. (This will leave a gap or the last
     * annotation will have a longer duration.
     *
     * @param firstUAIndex the index of the first unaligned slot in a series
     * @param lastUAIndex the index of the last unaligned slot in a series
     */
    private void calculateSlotsInInterval(int firstUAIndex, int lastUAIndex) {
        long[] slot;
        long[] otherSlot;

        // special cases, just one slot
        if (firstUAIndex == lastUAIndex) {
            if ((firstUAIndex % 2) == 0) {
                //begin time slot, set to the time of previous end slot
                otherSlot = (long[]) rootSlots.get(firstUAIndex - 1);
                slot = (long[]) rootSlots.get(firstUAIndex);
                slot[1] = otherSlot[1];
            } else {
                // an end time slot
                if (lastUAIndex < (rootSlots.size() - 1)) {
                    otherSlot = (long[]) rootSlots.get(lastUAIndex + 1);

                    long nextVal = otherSlot[1];
                    slot = (long[]) rootSlots.get(lastUAIndex);
                    // always connect to the next begin, or limit to preferred duration?
                    otherSlot = (long[]) rootSlots.get(lastUAIndex - 1);

                    if ((nextVal - otherSlot[1]) > preferredBlockDuration) {
                        nextVal = otherSlot[1] + preferredBlockDuration;
                    }

                    //
                    slot[1] = nextVal;
                } else {
                    otherSlot = (long[]) rootSlots.get(lastUAIndex - 1);
                    slot = (long[]) rootSlots.get(lastUAIndex);
                    slot[1] = otherSlot[1] + preferredBlockDuration;
                }
            }

            return;
        }

        // all unaligned
        if ((firstUAIndex == 1) && (lastUAIndex == (rootSlots.size() - 1))) {
            for (int i = 1; i <= lastUAIndex; i++) {
                slot = (long[]) rootSlots.get(i);
                slot[1] = (long) Math.ceil((float) i / 2) * preferredBlockDuration;
            }

            return;
        }

        // mix, interval at the end
        if (lastUAIndex == (rootSlots.size() - 1)) {
            slot = (long[]) rootSlots.get(firstUAIndex - 1);

            long startTime = slot[1];

            // additional counter
            int j = ((firstUAIndex % 2) == 0) ? 1 : 2;

            for (int i = firstUAIndex; i <= lastUAIndex; i++, j++) {
                slot = (long[]) rootSlots.get(i);
                slot[1] = startTime + ((j / 2) * preferredBlockDuration);
            }
        } else {
            // interval anywhere
            int numIntervals = 0;
            long delta = 0;

            // calculate the number of involved 'annotations'
            int begin = ((firstUAIndex % 2) == 0) ? (firstUAIndex + 1)
                                                  : firstUAIndex;
            int end = ((lastUAIndex % 2) == 0) ? (lastUAIndex + 1) : lastUAIndex;
            numIntervals = ((end - begin) / 2) + 1;
            //System.out.println("num intervals: " + numIntervals);

            // find left and right aligned values
            slot = (long[]) rootSlots.get(firstUAIndex - 1);
            otherSlot = (long[]) rootSlots.get(lastUAIndex + 1);

            long startTime = slot[1];
            long endTime = otherSlot[1];
            delta = (endTime - startTime) / numIntervals;

            //System.out.println("Time per Interval: " + delta);
            if (delta > preferredBlockDuration) {
                delta = preferredBlockDuration;

                //System.out.println("Interval decreased to: " + delta);
            }

            // loop over the unaligned slots
            int j = ((firstUAIndex % 2) == 0) ? 1 : 2;

            for (int i = firstUAIndex; i <= lastUAIndex; i++, j++) {
                slot = (long[]) rootSlots.get(i);
                slot[1] = startTime + ((j / 2) * delta);
            }
        }
    }
    
    /**
     * Removes tabs, multiple white spaces in a row and some illegal xml characters.
     * @param origString the input
     * @return a scrubbed version of the string
     */
    private String scrubAnnotation(String origString) {
    	if (origString == null) {
    		return origString;
    	}
    	char[] chars = origString.toCharArray();
    	StringBuilder b = new StringBuilder(origString.length());
    	boolean lastSpace = false;
    	boolean isSp = false;
    	char c;
    	for (int i = 0; i < chars.length; i++) {
    		c = chars[i];
    		// skip iso control characters
    		if (c < '\u0020') {
    			continue;
    		}
    		// check if it is a possiblr scrubbable char
    		isSp = false;
    		for (int j = 0; j < scrubbables.length; j++) {
    			if (c == scrubbables[j]) {
    				isSp = true;
    				break;
    			}
    		}
    		
    		if (!isSp) {
    			b.append(c);
    			lastSpace = false;
    		} else {
    			if (c == scrubbables[0]) {// the whitespace
    				if (!lastSpace) {
    					b.append(c);
    					lastSpace = true;
    				} // else skip this whitespace
    			} //else skip other scrubbables without setting the flag
    				
    			
    		}
    	}
    	return b.toString();
    }

    /**
     * Converts a time definition in the format hh:mm:ss.sss into a long that
     * contains the time in milli seconds.  Copied from
     * mpi.eudico.client.util.TimeFormatter
     *
     * @param timeString the string that contains the time in the format
     *        hh:mm:ss.sss
     *
     * @return the time in seconds, -1.0 if the time string has an illegal
     *         format
     */
    public long toMilliSeconds(String timeString) {
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
            ServerLogger.LOG.warning("Unknown time format: " + timeString);

            return -1;
        }
    }

    //###################################################################
    /**
     * Returns a list of AnnotationRecords for the given tier.
     *
     * @param tierName the name of the tier
     * @param fileName the file name (for historic reasons)
     *
     * @return a list of AnnotationRecords
     */
    @Override
	public List<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
        parse(fileName);

        // resolve tier name??
        ArrayList<AnnotationRecord> records = tierNameToAnnRecordMap.get(tierName);

        if (records == null) {
            records = new ArrayList<AnnotationRecord>(0);
        }

        return records;
    }

    /**
     * Returns the name of teh linguistic type for the specified tier.
     *
     * @param tierName name of the tier
     * @param fileName the file name
     *
     * @return the name of the linguistic type
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

    /**
     * Creates (so should be called only once) and returns linguistic type
     * records.
     *
     * @param fileName the file name
     *
     * @return a list of linguistic type records
     */
    @Override
	public ArrayList<LingTypeRecord> getLinguisticTypes(String fileName) {
        parse(fileName);

        if (lingTypeRecords.size() == 0) {
            //String name2;
            LingTypeRecord lt;

            for (String name : markerOrder) {
                //name2 = "\\" + name;
                if (name.equals(elanBeginLabel) || name.equals(elanELANLabel) ||
                        name.equals(elanEndLabel) ||
                        name.equals(elanParticipantLabel) ||
                        name.equals(label_eudicoparticipant) ||
                        name.equals(label_eudicot0) ||
                        name.equals(label_eudicot1) ||
                        name.equals(userParticipantMarker)) {
                    continue;
                }

                lt = new LingTypeRecord();
                lt.setLingTypeId(name);
                // set defaults
                lt.setTimeAlignable("false");
                lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);

                if (typFile.getRecordMarker().equals(name)) {
                    lt.setStereoType(null);
                    lt.setTimeAlignable("true");
                    lingTypeRecords.add(lt);

                    continue;
                }

                int stereotype = typFile.getStereoType(name);

                if (stereotype == Constraint.SYMBOLIC_SUBDIVISION) {
                    lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
                } else if (stereotype == Constraint.TIME_SUBDIVISION) {
                    lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
                    lt.setTimeAlignable("true");
                } else if (stereotype == Constraint.INCLUDED_IN) {
                    // not supported
                    lt.setStereoType(Constraint.stereoTypes[Constraint.INCLUDED_IN]);
                    lt.setTimeAlignable("true");
                }

                lingTypeRecords.add(lt);
            }
        }

        return lingTypeRecords;
    }

    /**
     * Returns a list of media descriptors
     *
     * @param fileName the file name
     *
     * @return a list of media descriptors
     */
    @Override
	public List<MediaDescriptor> getMediaDescriptors(String fileName) {
        parse(fileName);

        return mediaDescriptors;
    }

    /**
     * Returns the parent tier name for the specified tier
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the parent tier name
     */
    @Override
	public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        int atIndex = tierName.indexOf(AT);

        if ((atIndex > 0) && (tierName.length() > atIndex)) {
            String name = tierName.substring(0, atIndex);

            if (name.equals(typFile.getRecordMarker())) {
                return null;
            }

            String spk = tierName.substring(atIndex);
            String par = typFile.getParentMarker(name);

            if (par != null) {
                return (par + spk);
            }
        } else {
            return typFile.getParentMarker(tierName);
        }

        return null;
    }

    /**
     * Returns the participant part of the tier name, if any
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the participant
     */
    @Override
	public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        String participant = "";
        int atIndex = tierName.indexOf(AT);

        if ((atIndex > -1) && (tierName.length() > (atIndex + 1))) {
            participant = tierName.substring(atIndex + 1);
        }

        return participant;
    }

    /**
     * Creates a list of tiernames, ordered as much as possible according to
     * the marker order (and participant order) in the Toolbox file.  Assumes
     * that this method is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of tiernames
     */
    @Override
	public List<String> getTierNames(String fileName) {
        parse(fileName);

        List<String> names = new ArrayList<String>(tierNameSet.size());
        String spk = null;
        String marker = null;
        String fullName = null;

        for (int i = 0; i < participantOrder.size(); i++) {
            spk = (String) participantOrder.get(i);

            for (int j = 0; j < markerOrder.size(); j++) {
                marker = (String) markerOrder.get(j);

                fullName = marker + AT + spk;

                if (tierNameSet.contains(fullName)) {
                    names.add(fullName);
                }
            }
        }

        return names;
    }

    /**
     * Returns list of Strings in format "ts" + nnn.  Assumes that this method
     * is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of time slot id's
     */
    @Override
	public List<String> getTimeOrder(String fileName) {
        parse(fileName);

        List<String> resultTimeOrder = new ArrayList<String>();

        for (int i = 0; i < timeOrder.size(); i++) {
            resultTimeOrder.add(TS_ID_PREFIX +
                ((long[]) (timeOrder.get(i)))[0]);
        }

        return resultTimeOrder;
    }

    /**
     * Returns a map of time slot id's to time values, all as strings. This is
     * not the most effective solution, but adheres to the Parser calls.
     * Assumes that this method is only called once.
     *
     * @param fileName the file name
     *
     * @return a map of time slot id's to time values
     */
    @Override
	public Map<String, String> getTimeSlots(String fileName) {
        HashMap<String, String> resultSlots = new HashMap<String, String>();

        Iterator<long[]> timeSlotIter = timeSlots.iterator();
        String tsId;
        String timeValue;

        while (timeSlotIter.hasNext()) {
            long[] timeSlot = timeSlotIter.next();
            tsId = TS_ID_PREFIX + timeSlot[0];
            timeValue = Long.toString(timeSlot[1]);

            resultSlots.put(tsId, timeValue);
        }

        return resultSlots;
    }
    

    /**
     * @return a List with one property, that of the last used annotation id.
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTranscriptionProperties(java.lang.String)
	 */
	@Override
	public List<Property> getTranscriptionProperties(String fileName) {
		List<Property> propList = new ArrayList<Property>(1);
		propList.add(new PropertyImpl("lastUsedAnnotationId", String.valueOf(annotId)));//actually annotId - 1
		return propList;
	}

	/**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#setDecoderInfo(mpi.eudico.server.corpora.clom.DecoderInfo)
     */
    @Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
        if (decoderInfo instanceof ToolboxDecoderInfo2) {
            this.decoderInfo = (ToolboxDecoderInfo2) decoderInfo;
            preferredBlockDuration = (int) this.decoderInfo.getBlockDuration();
        }
    }
    
    //################################
    /**
     * Adds the block to a sorted map per speaker, with the start time as the key.
     * This will fail if there are records without a start (or end) time.
     */
    private void cacheBlock(List<ToolboxLine> allLines) {
    	long[] btet = new long[]{-1, -1};
    	String speaker = null;
    	
    	for (int i = 0; i < allLines.size(); i++) {
    		ToolboxLine tl = allLines.get(i);

            if (tl instanceof SimpleToolboxLine) {
            	SimpleToolboxLine stl = (SimpleToolboxLine) tl;

                if (stl.getMarkerName().equals(typFile.getRecordMarker())) {
                    if (decoderInfo.isTimeInRefMarker()) {
                    	btet[0] = timeForString(stl.getLine());
                    }
                } else if (stl.getMarkerName().equals(elanBeginLabel) ||
                        stl.getMarkerName().equals(label_eudicot0)) {
                	btet[0] = timeForString(stl.getLine());
                } else if (stl.getMarkerName().equals(elanEndLabel) ||
                        stl.getMarkerName().equals(label_eudicot1)) {
                	btet[1] = timeForString(stl.getLine());
                   
                } else if (stl.getMarkerName().equals(elanParticipantLabel) ||
                        stl.getMarkerName().equals(label_eudicoparticipant) || 
                        stl.getMarkerName().equals(userParticipantMarker)) {
                    speaker = stl.getLine();                   
                }
            }
    	}
		// this caching should be done per speaker? Or not at all?
    	// how to deal with records that have no time stamp at all?
//		if (btet[0] > -1) {
//			lastCachedBeginTime = btet[0];
//		} else if (btet[1] > -1) {
//			lastCachedBeginTime = btet[1];
//		} else {
//			lastCachedBeginTime += 1000;
//		}
//		if (btet[1] > -1) {
//			lastCachedEndTime = btet[1];
//		}
		// add checks, use "null" key for the case of no speaker
		TreeMap<Long, List<ToolboxLine>>  curSpeakerMap = null;
		//if (speaker != null) {
		curSpeakerMap = speakerCacheMap.get(speaker);
			if (curSpeakerMap == null) {
				curSpeakerMap = new TreeMap<Long, List<ToolboxLine>>();
				speakerCacheMap.put(speaker, curSpeakerMap);
			}
		//}
		if (btet[0] > -1) {
			curSpeakerMap.put(Long.valueOf(btet[0]), allLines);
		} else if (btet[1] > -1) {
			Long lower = curSpeakerMap.lowerKey(Long.valueOf(btet[1]));
			if (lower != null) {
				curSpeakerMap.put(Long.valueOf((btet[1] - lower) / 2), allLines);// add a begin time in the middle
			} else {
				curSpeakerMap.put(Long.valueOf(btet[1] - 100), allLines);//??
			}
		} else {
			Long lastKey = curSpeakerMap.lastKey();
			if (lastKey != null) {
				curSpeakerMap.put(Long.valueOf(lastKey + 100), allLines);//??
			} else {
				curSpeakerMap.put(Long.valueOf(0), allLines);//??
			}
		}
    }
    
    /**
     * Create annotation structures after sorting per speaker.
     */
    private void createAnnotationsFromCache() {
    	TreeMap<Long, List<ToolboxLine>> allBlocks = new TreeMap<Long, List<ToolboxLine>>();
    	Iterator<String> speakerIt = speakerCacheMap.keySet().iterator();
    	
    	while (speakerIt.hasNext()) {
    		String speak = speakerIt.next();
    		TreeMap<Long, List<ToolboxLine>> spBlocks = speakerCacheMap.get(speak);
    		Iterator<Long> blockIt = spBlocks.keySet().iterator();
    		while (blockIt.hasNext()) {
    			Long nextLong = blockIt.next();
    			List<ToolboxLine> nextBlock = spBlocks.get(nextLong);
    			if (!allBlocks.containsKey(nextLong)) {
    				allBlocks.put(nextLong, nextBlock);
    			} else {
    				// add after existing records with the same time for now
    				Long solveLong = Long.valueOf(nextLong.longValue() + 1);
    				while (allBlocks.containsKey(solveLong)) {
    					solveLong = Long.valueOf(solveLong.longValue() + 1);
    				}
    				allBlocks.put(solveLong, nextBlock);
    			}
    		}
    	}
    	
		Iterator<Long> blockIt = allBlocks.keySet().iterator();
		while (blockIt.hasNext()) {
			createAnnotations(allBlocks.get(blockIt.next()));
		}
		
    	/* maybe later
    	while (speakerIt.hasNext()) {
    		String speak = speakerIt.next();
    		TreeMap<Long, List<ToolboxLine>> spBlocks = speakerCacheMap.get(speak);
    		Iterator<Long> blockIt = spBlocks.keySet().iterator();
    		while (blockIt.hasNext()) {
    			// the speaker-by-speaker approach would be best but there is currently
    			// interference because all time slots etc. are in shared lists and 
    			// there are assumptions on order and increasing time values etc.
    			createAnnotations(spBlocks.get(blockIt.next()));
    		}
    	}
    	*/
    }
}
