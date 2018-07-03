package mpi.eudico.client.annotator.imports.praat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.praat.PraatSpecialChars;


/**
 * A class to extract annotations from a Praat .TextGrid file.
 * Only "IntervalTier"s and "TextTier"s are supported.
 * The expected format is roughly like below, but the format is only loosely checked.
 * 
 * 1 File type = "ooTextFile"
 * 2 Object class = "TextGrid"
 * 3 
 * 4 xmin = 0 
 * 5 xmax = 36.59755102040816 
 * 6 tiers? &lt;exists&; 
 * 7 size = 2 
 * 8 item []: 
 * 9     item [1]:
 * 10         class = "IntervalTier" 
 * 11         name = "One" 
 * 12         xmin = 0 
 * 13         xmax = 36.59755102040816 
 * 14         intervals: size = 5 
 * 15         intervals [1]:
 * 16             xmin = 0 
 * 17             xmax = 1 
 * 18             text = "" 
 * 
 * @version Feb 2013 the short notation format (roughly the same lines without the keys and the indentation)
 * is now also supported
 */
public class PraatTextGrid {
    private final char brack = '[';
    private final String eq = "=";
    private final String item = "item";
    private final String cl = "class";
    private final String tierSpec = "IntervalTier";
    private final String textTierSpec = "TextTier";
    private final String nm = "name";
    private final String interval = "intervals";
    private final String min = "xmin";
    private final String max = "xmax";
    private final String tx = "text";
    private final String points = "points";
    private final String time = "time";
    private final String mark = "mark";
    private final String number = "number";
    private final String escapedInnerQuote = "\"\"";
    private final String escapedOuterQuote = "\"\"\"";
    
    private boolean includeTextTiers = false;
    private int pointDuration = 1;
    private String encoding;
    
    private File gridFile;
    private List<String> tierNames;
    private Map<String, List<AnnotationDataRecord>> annotationMap;
    private PraatSpecialChars lookUp;
    
    private enum SN_POSITION {// short notation line position
    	OUTSIDE,// not in any type of tier
    	NEXT_IS_NAME,// next line is a tier name
    	NEXT_IS_MIN,// next line is the min time of interval
    	NEXT_IS_MAX,// next line is the max time of interval
    	NEXT_IS_TEXT,// next line is the text of an interval
    	NEXT_IS_TIME,// next line is the time of a point annotation
    	NEXT_IS_MARK,// next line is the text of a point annotation
    	NEXT_IS_TOTAL_MIN,// next line is the overall min of a tier, ignored
    	NEXT_IS_TOTAL_MAX,// next line is the overall max of a tier, ignored
    	NEXT_IS_SIZE// next line is the number of annotations of a tier, ignored
    }; 

    /**
     * Creates a new Praat TextGrid parser for the file at the specified path
     *
     * @param fileName the path to the file
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(String fileName) throws IOException {
        this(fileName, false, 1);
    }
    
    /**
     * Creates a new Praat TextGrid parser for the file at the specified path. 
     *
     * @param fileName the path to the file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(String fileName, boolean includeTextTiers, int pointDuration) 
        throws IOException {
        if (fileName != null) {
            gridFile = new File(fileName);
        }
        this.includeTextTiers = includeTextTiers;
        this.pointDuration = pointDuration;
        
        parse();
    }

    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile) throws IOException {
        this(gridFile, false, 1);
    }

    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     * 
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile, boolean includeTextTiers, int pointDuration) throws IOException {
        this(gridFile, includeTextTiers, pointDuration, null);
    }
    
    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     * @param encoding the character encoding of the file
     * 
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile, boolean includeTextTiers, int pointDuration, 
    		String encoding) throws IOException {
        this.gridFile = gridFile;
        
        this.includeTextTiers = includeTextTiers;
        this.pointDuration = pointDuration;
        this.encoding = encoding;
        
        parse();
    }
    
    /**
     * Returns a list of detected interval tiers.
     *
     * @return a list of detected interval tiernames
     */
    public List<String> getTierNames() {
        return tierNames;
    }

    /**
     * Returns a list of annotation records for the specified tier.
     *
     * @param tierName the name of the tier
     *
     * @return the annotation records of the specified tier
     */
    public List<AnnotationDataRecord> getAnnotationRecords(String tierName) {
        if ((tierName == null) || (annotationMap == null)) {
            return null;
        }

        return annotationMap.get(tierName);
    }
    
    /**
     * Reads a few lines and returns whether the file is in short notation.
     * 
     * @param reader the reader object
     * @return true if in short text notation, false otherwise
     */
    private boolean isShortNotation(BufferedReader reader) throws IOException {
    	if (reader == null) {
    		return false;
    	}
    	
    	String line;
    	int lineCount = 0;
    	
    	boolean xmin = false, xmax = false, tiers = false;// are the keys xmin and xmax and tiers? found
    	
    	while ((line = reader.readLine()) != null && lineCount < 5) {
    		if (line.length() == 0) {
    			continue;// skip empty lines
    		}
    		
    		if (lineCount == 2) {
    			xmin = (line.indexOf(min) > -1);
    		}
    		if (lineCount == 3) {
    			xmax = (line.indexOf(max) > -1);
    		}
    		if (lineCount == 4) {
    			tiers = (line.indexOf("tiers?") > -1);
    		}
    		lineCount++;   		
    	}
    	
    	return (!xmin && !xmax && !tiers);
    }

    /**
     * Parses the file and extracts interval tiers with their annotations.
     *
     * @throws IOException if the file can not be read for any reason
     */
    private void parse() throws IOException {
        if ((gridFile == null) || !gridFile.exists()) {
            ClientLogger.LOG.warning("No existing file specified.");
            throw new IOException("No existing file specified.");
        }

        BufferedReader reader = null;

        try {
        	if (encoding == null) {      	
        		reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(gridFile)));
        	} else {
        		try {
        			reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(gridFile), encoding));
        		} catch (UnsupportedEncodingException uee) {
        			ClientLogger.LOG.warning("Unsupported encoding: " + uee.getMessage());
        			reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(gridFile)));
        		}
        	}
            // Praat files on Windows and Linux are created with encoding "Cp1252"
            // on Mac with encoding "MacRoman". The ui could/should be extended
            // with an option to specify the encoding
            // InputStreamReader isr = new InputStreamReader(
            //        new FileInputStream(gridFile));
            // System.out.println("Encoding: " + isr.getEncoding());
        	// System.out.println("Read encoding: " + encoding);
        	ClientLogger.LOG.info("Read encoding: " + encoding);
        	
        	boolean isShortNotation = isShortNotation(reader);
        	ClientLogger.LOG.info("Praat TextGrid is in short notation: " + isShortNotation);
        	
        	if (isShortNotation) {
        		parseShortNotation(reader);
        		return;
        	}
        	
            tierNames = new ArrayList<String>(4);
            annotationMap = new HashMap<String, List<AnnotationDataRecord>>(4);

            List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>();
            AnnotationDataRecord record = null;

            String line;
            //int lineNum = 0;
            String tierName = null;
            String annValue = "";
            long begin = -1;
            long end = -1;
            boolean inTier = false;
            boolean inInterval = false;
            boolean inTextTier = false;
            boolean inPoints = false;
            int eqPos = -1;

            while ((line = reader.readLine()) != null) {
                //lineNum++;
                //System.out.println(lineNum + " " + line);

                if ((line.indexOf(cl) >= 0) && 
                        ((line.indexOf(tierSpec) > 5) || (line.indexOf(textTierSpec) > 5))) {
                    // check if we have to include text (point) tiers
                    if (line.indexOf(textTierSpec) > 5) {
                        if (includeTextTiers) {
                            inTextTier = true;
                        } else {
                            inTextTier = false;
                            inTier = false;
                            continue;
                        }
                    }
                    // begin of a new tier
                    records = new ArrayList<AnnotationDataRecord>();
                    inTier = true;
            
                    continue;
                }

                if (!inTier) {
                    continue;
                }

                eqPos = line.indexOf(eq);
                
                if (inTextTier) {
                    // text or point tier
                    if (eqPos > 0) {
	                    // split and parse
	                    if (!inPoints && (line.indexOf(nm) >= 0) &&
	                            (line.indexOf(nm) < eqPos)) {
	                        tierName = extractTierName(line, eqPos);
	
	                        if (!annotationMap.containsKey(tierName)) {
	                            annotationMap.put(tierName, records);
	                            tierNames.add(tierName);
	                            ClientLogger.LOG.info("Point Tier detected: " + tierName);
	                        } else {
	                        	// the same (sometimes empty) tiername can occur more than once, rename
	                        	int count = 2;
	                        	String nextName = "";
	                        	for (; count < 50; count++) {
	                        		nextName = tierName + "-" + count;
	                        		if (!annotationMap.containsKey(nextName)) {
	    	                            annotationMap.put(nextName, records);
	    	                            tierNames.add(nextName);
	    	                            ClientLogger.LOG.info("Point Tier detected: " + tierName + " and renamed to: " + nextName);
	    	                            break;
	                        		}
	                        	}
	                        }
	
	                        continue;
	                    } else if (!inPoints) {
	                        continue;
	                    } else if (line.indexOf(time) > -1 || line.indexOf(number) > -1) {
	                        begin = extractLong(line, eqPos);
	                        //System.out.println("B: " + begin);
	                    } else if (line.indexOf(mark) > -1) {
	                        // extract value
	                        annValue = extractTextValue(line, eqPos);
	                        // finish and add the annotation record
	                        inPoints = false;
	                        //System.out.println("T: " + annValue);
	                        record = new AnnotationDataRecord(tierName, annValue,
	                                begin, begin + pointDuration);
	                        records.add(record);
	                        // reset
	                        annValue = "";
	                        begin = -1;
	                    }
	                } else {
	                    // points??
	                    if ((line.indexOf(points) >= 0) &&
	                            (line.indexOf(brack) > points.length())) {
	                        inPoints = true;
	
	                        continue;
	                    } else {
	                        if ((line.indexOf(item) >= 0) &&
	                                (line.indexOf(brack) > item.length())) {
	                            // reset
	                            inTextTier = false;
	                            inPoints = false;
	                        }
	                    }
	                } // end point tier
                } else {
                    // interval tier
	                if (eqPos > 0) {
	                    // split and parse
	                    if (!inInterval && (line.indexOf(nm) >= 0) &&
	                            (line.indexOf(nm) < eqPos)) {
	                        tierName = extractTierName(line, eqPos);
	
	                        if (!annotationMap.containsKey(tierName)) {
	                            annotationMap.put(tierName, records);
	                            tierNames.add(tierName);
	                            ClientLogger.LOG.info("Tier detected: " + tierName);
	                        } else {
	                        	// the same (sometimes empty) tiername can occur more than once, rename
	                        	int count = 2;
	                        	String nextName = "";
	                        	for (; count < 50; count++) {
	                        		nextName = tierName + "-" + count;
	                        		if (!annotationMap.containsKey(nextName)) {
	    	                            annotationMap.put(nextName, records);
	    	                            tierNames.add(nextName);
	    	                            ClientLogger.LOG.info("Tier detected: " + tierName + " and renamed to: " + nextName);
	    	                            break;
	                        		}
	                        	}
	                        }
	
	                        continue;
	                    } else if (!inInterval) {
	                        continue;
	                    } else if (line.indexOf(min) > -1) {
	                        begin = extractLong(line, eqPos);
	                        //System.out.println("B: " + begin);
	                    } else if (line.indexOf(max) > -1) {
	                        end = extractLong(line, eqPos);
	                        //System.out.println("E: " + end);
	                    } else if (line.indexOf(tx) > -1) {
	                        // extract value
	                        annValue = extractTextValue(line, eqPos);
	                        // finish and add the annotation record
	                        inInterval = false;
	                        //System.out.println("T: " + annValue);
	                        record = new AnnotationDataRecord(tierName, annValue,
	                                begin, end);
	                        records.add(record);
	                        // reset
	                        annValue = "";
	                        begin = -1;
	                        end = -1;
	                    }
	                } else {
	                    // interval?
	                    if ((line.indexOf(interval) >= 0) &&
	                            (line.indexOf(brack) > interval.length())) {
	                        inInterval = true;
	
	                        continue;
	                    } else {
	                        if ((line.indexOf(item) >= 0) &&
	                                (line.indexOf(brack) > item.length())) {
	                            // reset
	                            inTier = false;
	                            inInterval = false;
	                        }
	                    }
	                }
                }
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception fe) {
            throw new IOException("Error occurred while reading the file: " +
                fe.getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /**
     * Parses the short notation of a Praat TextGrid file. 
     * Handling completely separated from the long notation.
     * 
     * @param reader the (configured) reader
     */
    private void parseShortNotation(BufferedReader reader) throws IOException {
    	if (reader == null) {
    		throw new IOException("The reader object is null, cannot read from the file.");
    	}
    	
        tierNames = new ArrayList<String>(4);
        annotationMap = new HashMap<String, List<AnnotationDataRecord>>(4);

        List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>();
        AnnotationDataRecord record = null;

        String line;
        String tierName = null;
        String annValue = "";
        long begin = -1;
        long end = -1;
        
        boolean inTextTier = false;// in text tier
        boolean inTier = false;// in interval tier
        int eqPos = -1;
        
        SN_POSITION linePos = SN_POSITION.OUTSIDE;

        while ((line = reader.readLine()) != null) {
        	if (line.length() == 0) {
        		continue;
        	}
        	
    		if (line.indexOf(tierSpec) > -1) {
    			linePos = SN_POSITION.NEXT_IS_NAME;
    			inTier = true;
    			inTextTier = false;
    			continue;
    		}
    		if (line.indexOf(textTierSpec) > -1) {
    			linePos = SN_POSITION.NEXT_IS_NAME;
    			inTier = false;
    			inTextTier = true;
    			continue;
    		}
        	
        	if (linePos == SN_POSITION.NEXT_IS_NAME) {// tier name on this line
        		if (!inTier && !inTextTier) {
        			linePos = SN_POSITION.NEXT_IS_TOTAL_MIN;
        			continue;
        		}
        		
        		if (inTier || (inTextTier && includeTextTiers)) {
	        		tierName = removeQuotes(line);
	        		if (tierName.length() == 0) {
	        			tierName = "Noname";
	        		}
	        		
	        		records = new ArrayList<AnnotationDataRecord>();	
	        		
	                if (!annotationMap.containsKey(tierName)) {
	                    annotationMap.put(tierName, records);
	                    tierNames.add(tierName);
	                    if (inTextTier) {
	                    	ClientLogger.LOG.info("Point Tier detected: " + tierName);
	                    } else {
	                    	ClientLogger.LOG.info("Interval Tier detected: " + tierName);
	                    }
	                } else {
	                	// the same (sometimes empty) tiername can occur more than once, rename
	                	int count = 2;
	                	String nextName = "";
	                	for (; count < 50; count++) {
	                		nextName = tierName + "-" + count;
	                		if (!annotationMap.containsKey(nextName)) {
	                            annotationMap.put(nextName, records);
	                            tierNames.add(nextName);
	                            if (inTextTier) {
	                            	ClientLogger.LOG.info("Point Tier detected: " + tierName + " and renamed to: " + nextName);
	                            } else {
	                            	ClientLogger.LOG.info("Interval Tier detected: " + tierName + " and renamed to: " + nextName);
	                            }
	                            break;
	                		}
	                	}
	                }
        		}
        		
                linePos = SN_POSITION.NEXT_IS_TOTAL_MIN;
                continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_TOTAL_MIN) {
        		linePos = SN_POSITION.NEXT_IS_TOTAL_MAX;
        		continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_TOTAL_MAX) {
        		linePos = SN_POSITION.NEXT_IS_SIZE;
        		continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_SIZE) {
        		if (inTextTier) {
        			linePos = SN_POSITION.NEXT_IS_TIME;
        		} else {// interval tier
        			linePos = SN_POSITION.NEXT_IS_MIN;
        		}
        		continue;
        	}
        	// point text tiers
        	if (linePos == SN_POSITION.NEXT_IS_TIME) {
        		if (includeTextTiers) {
        			// hier extract time
        			begin = extractLong(line, eqPos);// eqPos = -1
        		}
        		linePos = SN_POSITION.NEXT_IS_MARK;
        		continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_MARK) {
        		if (includeTextTiers) {
                    annValue = extractTextValue(line, eqPos);// eqPos = -1
                    // finish and add the annotation record
                    record = new AnnotationDataRecord(tierName, annValue,
                            begin, begin + pointDuration);
                    records.add(record);
                    // reset
                    annValue = "";
                    begin = -1;
        		}
        		linePos = SN_POSITION.NEXT_IS_TIME;
        		continue;
        	}
        	// interval tiers
        	if (linePos == SN_POSITION.NEXT_IS_MIN) {
        		begin = extractLong(line, eqPos);// eqPos = -1
        		 linePos = SN_POSITION.NEXT_IS_MAX;
        		 continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_MAX) {
        		end = extractLong(line, eqPos);// eqPos = -1
        		linePos = SN_POSITION.NEXT_IS_TEXT;
        		continue;
        	}
        	
        	if (linePos == SN_POSITION.NEXT_IS_TEXT) {
                // extract value
                annValue = extractTextValue(line, eqPos);// eqPos = -1
                // finish and add the annotation record
                record = new AnnotationDataRecord(tierName, annValue,
                        begin, end);
                records.add(record);
                // reset
                annValue = "";
                begin = -1;
                end = -1;
                
                linePos = SN_POSITION.NEXT_IS_MIN;
                continue;
        	}
        }
    }

    /**
     * Extracts the tiername from a line.
     *
     * @param line the line
     * @param eqPos the indexof the '=' sign
     *
     * @return the tier name
     */
    private String extractTierName(String line, int eqPos) {
        if (line.length() > (eqPos + 1)) {
            String name = line.substring(eqPos + 1).trim();

            if (name.length() < 3) {
            	if ("\"\"".equals(name)) {
            		return "Noname";
            	}
            	
                return name;
            }

            return removeQuotes(name);
        }

        return line; // or null??
    }

    /**
     * Extracts the text value and, if needed, converts Praat's special
     * character sequences into unicode chars.
     *
     * @param value the text value
     * @param eqPos the index of the equals sign
     *
     * @return the annotation value. If necessary Praat's special symbols have
     *         been converted  to Unicode.
     */
    private String extractTextValue(String value, int eqPos) {
        if (value.length() > (eqPos + 1)) {
            String rawV = removeQuotes(value.substring(eqPos + 1).trim()); // should be save

            if (lookUp == null) {
                lookUp = new PraatSpecialChars();
            }
            rawV = lookUp.replaceIllegalXMLChars(rawV);
            
            if (rawV.indexOf('\\') > -1) {
                // convert
//                if (lookUp == null) {
//                    lookUp = new PraatSpecialChars();
//                }

                return lookUp.convertSpecialChars(rawV);
            }

            return rawV;
        }

        return "";
    }

    /**
     * Extracts a double time value, multiplies by 1000 (sec to ms) and
     * converts to long.
     *
     * @param value the raw value
     * @param eqPos the index of the equals sign
     *
     * @return the time value rounded to milliseconds
     */
    private long extractLong(String value, int eqPos) {
        if (value.length() > (eqPos + 1)) {
            String v = value.substring(eqPos + 1).trim();
            long l = -1;

            try {
                Double d = new Double(v);
                l = Math.round(d.doubleValue() * 1000);
            } catch (NumberFormatException nfe) {
            	ClientLogger.LOG.warning("Not a valid numeric value: " + value);
            }

            return l;
        }

        return -1;
    }

    /**
     * Removes a beginning and end quote mark from the specified string. Does
     * no null check nor are spaces trimmed.
     * 
     * @version Feb 2013 added handling for outer escaped quotes (""") and inner escaped quotes ("")
     *
     * @param value the value of which leading and trailing quote chars should
     *        be removed
     *
     * @return the value without the quotes
     */
    private String removeQuotes(String value) {
    	boolean removeOuterQuotes = true;
    	if (value.startsWith(escapedOuterQuote) && value.endsWith(escapedOuterQuote)) {
    		removeOuterQuotes = false;
    	}
    	// replace all """ sequences by a single "
    	value = value.replaceAll(escapedOuterQuote, "\"");
    	value = value.replaceAll(escapedInnerQuote, "\"");
    	
    	if (removeOuterQuotes) {
	        if (value.charAt(0) == '"') {
	            if (value.charAt(value.length() - 1) == '"' && value.length() > 1) {
	                return value.substring(1, value.length() - 1);
	            } else {
	                return value.substring(1);
	            }
	        } else {
	            if (value.charAt(value.length() - 1) == '"') {
	                return value.substring(0, value.length() - 1);
	            } else {
	                return value;
	            }
	        }
    	} else {
    		return value;
    	}
    }

}
