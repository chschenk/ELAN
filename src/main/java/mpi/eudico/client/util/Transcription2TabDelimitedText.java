package mpi.eudico.client.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.TabExportTableModel;
import mpi.eudico.client.annotator.util.AnnotationCoreComparator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoFiles;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoTrans;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.TimeFormatter;
import mpi.eudico.util.TimeRelation;


/**
 * Created on Apr 20, 2004 Jun 2005: added optional export of time values in
 * milliseconds
 *
 * @author Alexander Klassmann
 * @version June 30, 2005
 * @version Aug 2005 Identity removed
 * @version Nov 2015 while adding new export options the methods with more than a dozen parameters
 * were replaced by methods accepting an encoding configuration object. Taking away the need for the
 * set of methods that call another similar method with more parameters using default values.
 * @version Jan 2016 added support for PAL with 50 fps as one of the SMPTE formats
 * @version Aug 2017 changed all static methods to instance methods so that is easier to configure
 * the output for different purposes. Added support for output as CSV file, using comma as the
 * delimiter instead of tab.
 */
public class Transcription2TabDelimitedText {
    /** the separator (now customizable, e.g. ';' or ',' ) */
    public String TAB = "\t";
    
    private String COMMA = ",";

    /** new line string (might make this customizable, e.g. \r\n) */
    final private String NEWLINE = "\n";
    private final String SQ = "\"";
    private final String DQ = "\"\"";
    private boolean csvEncodeText = false;
    
    /**
     * No-arg constructor
     */
    public Transcription2TabDelimitedText() {
		super();
	}
    
    /**
     * Sets the delimiter to use. 
     * 
     * @param delimiter the delimiter to use to separate cells
     */
    public void setDelimiter(String delimiter) {
    	if (delimiter != null) {
    		TAB = delimiter;
    	}
    }
    
    /**
     * The default is tab, for csv files it is set to comma in the exportTiers*** methods,
     * if the EncoderInfo object indicates that the output is a .csv file.
     * 
     * @return the current delimiter
     */
    public String getDelimiter() {
    	return TAB;
    }

	/**
     * Exports a transcription taking the data from a loaded Transcription object.
     * This method replaces the exportTiers functions with more than a dozen parameters.
     * 
     * @param encoderInfo the object containing the configuration parameters for encoding the file,
     * not tested for null. 
     * 
     * @throws IOException any io related exception
     */
     public void exportTiers(DelimitedTextEncoderInfoTrans encoderInfo) throws IOException {
    	if (encoderInfo.getExportFile() == null) {
    		throw new IOException("Encoder: no destination file specified for export");
    	}
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        // HS Nov 2015 new option, write header lines
        if (encoderInfo.getMediaHeaderLines() != null) {
        	for (String line : encoderInfo.getMediaHeaderLines()) {
        		writer.write(line + NEWLINE);
        	}
        	writer.write(NEWLINE);
        }
        
    	long mediaOffset = 0L;
    	if (encoderInfo.isAddMasterMediaOffset()) {
    		if (encoderInfo.getTranscription().getMediaDescriptors() != null && !encoderInfo.getTranscription().getMediaDescriptors().isEmpty()) {
    			mediaOffset = encoderInfo.getTranscription().getMediaDescriptors().get(0).timeOrigin;
    		}
    	}
    	// determination of SMPTE format string
    	String smpteType = null;
    	if (encoderInfo.isIncludeSMPTE()) {
    		if (encoderInfo.isPalFormat()) {
    			smpteType = Constants.PAL_STRING;
    		} else if (encoderInfo.isPal50Format()) {
    			smpteType = Constants.PAL_50_STRING;
    		} else {
    			smpteType = Constants.NTSC_STRING;
    		}
    	}
        // end header lines
        if (encoderInfo.getTierNames() != null) {
	        for (String tierName : encoderInfo.getTierNames()) {
	        	Map<String, String> cvEntryMap = null;
	        	String cvName = null;
	            
	            TierImpl tier = encoderInfo.getTranscription().getTierWithId(tierName);
	            String participant = tier.getParticipant();
	            
	            if(encoderInfo.isIncludeCVDescription()){        
	            	cvEntryMap = new HashMap<String, String>();
	                cvName = tier.getLinguisticType().getControlledVocabularyName();
	            	if(cvName != null){
	            		ControlledVocabulary cv = encoderInfo.getTranscription().getControlledVocabulary(cvName);
	            		int defLang = cv.getDefaultLanguageIndex();
	            		for (CVEntry cve : cv) {
	                    	cvEntryMap.put(cve.getValue(defLang), cve.getDescription(defLang));
	            		}
	            	}
	        	}
	
	            if (participant == null) {
	                participant = "";
	            }
	
	            for (Annotation annotation : tier.getAnnotations()) {  
	                if (annotation != null) {
	                    if (TimeRelation.overlaps(annotation, encoderInfo.getBeginTime(), encoderInfo.getEndTime())) {
	                        if (encoderInfo.isIncludeNames()) {
	                            writer.write(csvEncodeCond1(tierName));
	                            if (encoderInfo.isIncludeParticipants()) {
	                            	writer.write(TAB + csvEncodeCond1(participant));
	                            }
	                        } else {
	                        	if (encoderInfo.isIncludeParticipants()) {
	                        		writer.write(csvEncodeCond1(participant));
	                        	}
	                        }
	
	                        String ts = getTabString(annotation,
	                        	encoderInfo.isIncludeBeginTime(), encoderInfo.isIncludeEndTime(),
	                        	encoderInfo.isIncludeDuration(), encoderInfo.isIncludeHHMM(),
	                        	encoderInfo.isIncludeSSMS(), encoderInfo.isIncludeMS(), encoderInfo.isIncludeSMPTE(),
	                        	smpteType, mediaOffset);
	                        
	                        if(encoderInfo.isIncludeCVDescription()){      
	                        	String description = cvEntryMap.get(annotation.getValue());
	                        	if(description != null){
	                        		StringBuilder tsBuffer = new StringBuilder(ts.replace(NEWLINE, TAB));
	                        		tsBuffer.append(csvEncodeCond2(description) + NEWLINE);
	                        		ts = tsBuffer.toString();
	                        	}
	                        }
	
	                        if (encoderInfo.isIncludeNames() || encoderInfo.isIncludeParticipants()) {
	                        	writer.write(ts);
	                        } else {
	                        	writer.write(ts, 1, ts.length() - 1);
	                        }
	                    }
	                }
	            }
	        }
        } else {
        	ClientLogger.LOG.warning("There are no tiers selected for export.");
        }
        try {
        	writer.close();
        } catch (IOException ioe){}
    }
    
    /**
     * Exports the annotations of each tier in a separate column. If annotations
     * of multiple tiers share the same begin AND end time they will be on the same
     * row in the output. All annotations are collected in one list and sorted on
     * the time values.
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by parameters contained in the encoder info object).
     *
     * @param encoderInfo the settings for the encoder, not null
     *
     * @throws IOException i/o exception
     */
     public void exportTiersColumnPerTier(DelimitedTextEncoderInfoTrans encoderInfo) throws IOException {
    	if (encoderInfo.getExportFile() == null) {
            throw new IOException("Encoder: no destination file specified for export");
        }
    	if (encoderInfo.getTierNames() == null || encoderInfo.getTierNames().isEmpty()) {
    		throw new IOException("Encoder: no tiers selected for export");
    	}
    	
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        List<Annotation> allAnnotations = new ArrayList<Annotation>(100);
        List<String> allCVs = new ArrayList<String>();
        
    	long mediaOffset = 0L;
    	if (encoderInfo.isAddMasterMediaOffset()) {
    		if (encoderInfo.getTranscription().getMediaDescriptors() != null && !encoderInfo.getTranscription().getMediaDescriptors().isEmpty()) {
    			mediaOffset = encoderInfo.getTranscription().getMediaDescriptors().get(0).timeOrigin;
    		}
    	}
    	
        // Nov 2015 write media headers if selected
        if (encoderInfo.getMediaHeaderLines() != null) {
        	for (String line : encoderInfo.getMediaHeaderLines()) {
        		writer.write(line + NEWLINE);
        	}
        }
    	// determination of SMPTE format string
    	String smpteType = null;
    	if (encoderInfo.isIncludeSMPTE()) {
    		if (encoderInfo.isPalFormat()) {
    			smpteType = Constants.PAL_STRING;
    		} else if (encoderInfo.isPal50Format()) {
    			smpteType = Constants.PAL_50_STRING;
    		} else {
    			smpteType = Constants.NTSC_STRING;
    		}
    	}
    	
        for (String tierName : encoderInfo.getTierNames()) {
            Tier tier = encoderInfo.getTranscription().getTierWithId(tierName);

            if (tier != null) {
                if (encoderInfo.isIncludeCVDescription()) {
                	String cvName = tier.getLinguisticType().getControlledVocabularyName();
                	if (cvName != null) {
                		allCVs.add(cvName);
                	}
                }

                for (Annotation annotation : tier.getAnnotations()) {
                    if (annotation != null) {
                        if (TimeRelation.overlaps(annotation, encoderInfo.getBeginTime(),
                                    encoderInfo.getEndTime())) {
                            allAnnotations.add(annotation);
                        }

                        if (annotation.getBeginTimeBoundary() > encoderInfo.getEndTime()) {
                            break;
                        }
                    }
                }
            }
        }
         // end tier loop

        Collections.sort(allAnnotations, new AnnotationCoreComparator());        
        
        Map<String, Map<String,String>> cvMap = null;
        if (encoderInfo.isIncludeCVDescription()){
        	cvMap = new HashMap<String, Map<String,String>>();
        	for (String cvName : allCVs) {
        		Map<String,String> map = new HashMap<String,String>();
        		ControlledVocabulary cv = encoderInfo.getTranscription().getControlledVocabulary(cvName);
        		if (!cv.isEmpty()) {
        			cvMap.put(cvName, map);
        			int defLang = cv.getDefaultLanguageIndex();
        			for (CVEntry entry : cv) {
        				map.put(entry.getValue(defLang), entry.getDescription(defLang));
        			}
        		}
        	}
        }

        // group the annotations that share the same begin and end time
        TabExportTableModel model = new TabExportTableModel(allAnnotations, 
        		cvMap, encoderInfo.getTierNames());

        // write header, write each row, taking into account the formatting flags
        writer.write(getHeaders(model, encoderInfo.isIncludeBeginTime(), encoderInfo.isIncludeEndTime(),
                encoderInfo.isIncludeDuration(), encoderInfo.isIncludeHHMM(), encoderInfo.isIncludeSSMS(), encoderInfo.isIncludeMS(),
                encoderInfo.isIncludeSMPTE(), smpteType));
        
        writeRows(writer, model, encoderInfo.isIncludeBeginTime(), encoderInfo.isIncludeEndTime(),
            encoderInfo.isIncludeDuration(), encoderInfo.isIncludeHHMM(), encoderInfo.isIncludeSSMS(), encoderInfo.isIncludeMS(), encoderInfo.isIncludeSMPTE(),
            smpteType, mediaOffset, false, false );

        try {
        	writer.close();
        } catch (IOException ioe) {}
    }
    
   /**
    * Exports the annotations of each tier in a separate column. If annotations
    * of multiple tiers share the same begin AND end time they will be on the same
    * row in the output. All annotations are collected in one list and sorted on
    * the time values.
    * Exports annotations of the specified tiers that overlap the specified
    * interval. Which time information and in which time formats should be
    * included in the output is specified by the encoder information object.
    *
    * @param encoderInfo the encoder information object, not null
    * 
    * @throws IOException any IO exception
    */
     public void exportTiersColumnPerTierFromFiles(DelimitedTextEncoderInfoFiles encoderInfo) throws IOException {
        if (encoderInfo.getExportFile() == null) {
            throw new IOException("Encoder: no destination file specified for export");
        }

        // if no tiers specified don't export
        if (encoderInfo.getTierNames() == null || encoderInfo.getTierNames().isEmpty()) {
        	throw new IOException("Encoder: no tiers specified for export");
        }
        
        if (encoderInfo.getFiles() == null || encoderInfo.getFiles().isEmpty()) {
            throw new IOException("Encoder: no files specified for export");
        }
        
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        List<Annotation> allAnnotations = new ArrayList<Annotation>(100);
        AnnotationCoreComparator comparator = new AnnotationCoreComparator();

     // determination of SMPTE format string
    	String smpteType = null;
    	if (encoderInfo.isIncludeSMPTE()) {
    		if (encoderInfo.isPalFormat()) {
    			smpteType = Constants.PAL_STRING;
    		} else if (encoderInfo.isPal50Format()) {
    			smpteType = Constants.PAL_50_STRING;
    		} else {
    			smpteType = Constants.NTSC_STRING;
    		}
    	}
        List<String> allCVs = new ArrayList<String>();

        for (int i = 0; i < encoderInfo.getFiles().size(); i++) {
            File file = encoderInfo.getFiles().get(i);
            allAnnotations.clear();

            if (file == null) {
                continue;
            }

            try {
            	TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());
            	//HS Nov 2015 support for media offset correction in multiple file export
            	long mediaOffset = 0L;
            	if (encoderInfo.isAddMasterMediaOffset()) {
            		if (trans.getMediaDescriptors() != null && !trans.getMediaDescriptors().isEmpty()) {
            			mediaOffset = trans.getMediaDescriptors().get(0).timeOrigin;
            		}
            	}
            	
                for (String tierName : encoderInfo.getTierNames()) {
                    Tier tier = trans.getTierWithId(tierName);

                    if (tier != null) {
                    	if(encoderInfo.isIncludeCVDescription()){
                        	String cvName = tier.getLinguisticType().getControlledVocabularyName();
                        	if(cvName != null){
                        		allCVs.add(cvName);
                        	}
                        }
                    	
                        for (Annotation annotation : tier.getAnnotations()) {
                            if (annotation != null) {
                                //if (TimeRelation.overlaps(annotations[k], beginTime, endTime)) {
                                allAnnotations.add(annotation);

                                //}
                                //if (annotations[k].getBeginTimeBoundary() > endTime) {
                                //    break;
                                //}
                            }
                        }
                    }
                }
                 // end tier loop

                Collections.sort(allAnnotations, comparator);
                
                Map<String, Map<String,String>> cvMap = null;
                if (encoderInfo.isIncludeCVDescription()){
                	cvMap = new HashMap<String, Map<String,String>>();
                	for (String cvName : allCVs) {
                		Map<String,String> map = new HashMap<String,String>();
                		ControlledVocabulary cv = trans.getControlledVocabulary(cvName);
                		if (!cv.isEmpty()) {
                			cvMap.put(cvName, map);
                			int defLang = cv.getDefaultLanguageIndex();
                			for(CVEntry entry : cv){
                				// HS Nov 2015 putting cve ID - cve Description key-value pairs might be better?
                				map.put(entry.getValue(defLang), entry.getDescription(defLang));
                			}
                		}
                	}
                }

                // group the annotations that share the same begin and end time
                TabExportTableModel model = new TabExportTableModel(allAnnotations, cvMap, encoderInfo.getTierNames());
                model.setFileName(file.getName());
                model.setAbsoluteFilePath(file.getAbsolutePath());
                
                // write header, write each row, taking into account the formatting flags
                if (i == 0) {
                    String header = getHeaders(model, encoderInfo.isIncludeBeginTime(),
                            encoderInfo.isIncludeEndTime(), encoderInfo.isIncludeDuration(), 
                            encoderInfo.isIncludeHHMM(),
                            encoderInfo.isIncludeSSMS(), encoderInfo.isIncludeMS(), 
                            encoderInfo.isIncludeSMPTE(), smpteType);
                    writer.write(header, 0, header.length() - 1);
                    // the next line could depend on choices for file name/path by the user
                    writer.write(TAB +
                        csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnFileName")) + TAB + 
                        csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnFilePath")) + NEWLINE); 
                }
                // if table headers would be written for each transcription it could be done after the following 2 blocks
            	// Nov 2015 new option, file name or path in a row            	
            	if (encoderInfo.isFileNameInRow()) {
            		writer.write(NEWLINE); //insert empty line
            		if (encoderInfo.isIncludeFileName()) {
            			writer.write(csvEncodeCond1(file.getName()) + NEWLINE);
            		} 
            		// write the path if neither file name nor file path is selected but file name in a row is
            		if ( encoderInfo.isIncludeFilePath() || (!encoderInfo.isIncludeFileName() && !encoderInfo.isIncludeFilePath()) ){
            			writer.write(csvEncodeCond1(file.getAbsolutePath()) + NEWLINE);
            		}
            	}
            	// Nov 2015 new option, linked media information
            	if (encoderInfo.isIncludeMediaHeaders()) {
            		List<String> medHeaders = getMediaHeaders(trans);
        			if (medHeaders != null) {
                		if (!encoderInfo.isFileNameInRow()) {
                			writer.write(NEWLINE); //insert empty line
                		}
        				for (String s : medHeaders) {
        					writer.write(s);
        					writer.write(NEWLINE);
        				}
        			}         		
            	}
                
                writeRows(writer, model, encoderInfo.isIncludeBeginTime(), encoderInfo.isIncludeEndTime(),
                    encoderInfo.isIncludeDuration(), encoderInfo.isIncludeHHMM(), encoderInfo.isIncludeSSMS(), 
                    encoderInfo.isIncludeMS(), encoderInfo.isIncludeSMPTE(), smpteType, mediaOffset, 
                    (encoderInfo.isIncludeFileName() && !encoderInfo.isFileNameInRow()), 
                    (encoderInfo.isIncludeFilePath() && !encoderInfo.isFileNameInRow()) );
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                ClientLogger.LOG.warning("Could not handle file: " +
                    file.getAbsolutePath());
            }
        }

        writer.close();
    }
    
    /**
     * Exports annotations from selected tiers, from selected files to a specified output file 
     * location. 
     * 
     * @param encoderInfo an object containing all configuration parameters for the export.
     * Not null. The embedded list of files is not null and not empty.
     * 
     * @throws IOException any IO exception
     */
     public void exportTiersFromFiles(DelimitedTextEncoderInfoFiles encoderInfo) throws IOException {
        if (encoderInfo.getExportFile() == null) {
            throw new IOException("Encoder: no destination file specified for export");
        }
        // if no tiers specified don't export
        if (encoderInfo.getTierNames() == null || encoderInfo.getTierNames().isEmpty()) {
        	throw new IOException("Encoder: no tiers specified for export");
        }

        if (encoderInfo.getFiles() == null || encoderInfo.getFiles().isEmpty()) {
            throw new IOException("Encoder: no files specified for export");
        }
        
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        final String EMPTY = "";

        for (File file : encoderInfo.getFiles()) {
            if (file == null) {
                continue;
            }

            try {
            	TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());
                
                // HS Nov 2015 new option to correct for media offset
            	long mediaOffset = 0L;
            	if (encoderInfo.isAddMasterMediaOffset()) {
            		if (trans.getMediaDescriptors() != null && !trans.getMediaDescriptors().isEmpty()) {
            			mediaOffset = trans.getMediaDescriptors().get(0).timeOrigin;
            		}
            	}
            	
            	// Nov 2015 new option, file name or path in a row            	
            	if (encoderInfo.isFileNameInRow()) {
            		if (encoderInfo.isIncludeFileName()) {
            			writer.write("\"#" + file.getName() + "\"" + NEWLINE);
            		} 
            		// write the path if neither file name nor file path is selected but file name in a row is
            		if ( encoderInfo.isIncludeFilePath() || (!encoderInfo.isIncludeFileName() && !encoderInfo.isIncludeFilePath()) ) {
            			writer.write("\"#" + file.getAbsolutePath() + "\"" + NEWLINE);
            		}
            	}
            	// Nov 2015 new option, linked media information
            	if (encoderInfo.isIncludeMediaHeaders()) {
            		List<String> medHeaders = getMediaHeaders(trans);
        			if (medHeaders != null) {
        				for (String s : medHeaders) {
        					writer.write(s);
        					writer.write(NEWLINE);
        				}
        			}         		
            	}
            	
            	String smpteType = null;
            	if (encoderInfo.isIncludeSMPTE()) {
            		if (encoderInfo.isPalFormat()) {
            			smpteType = Constants.PAL_STRING;
            		} else if (encoderInfo.isPal50Format()) {
            			smpteType = Constants.PAL_50_STRING;
            		} else {
            			smpteType = Constants.NTSC_STRING;
            		}
            	}
            	// This outputs the tiers in the order of tierNames.
            	// The original code used the order of the tiers in the transcription.
                List<TierImpl> tiers = trans.getTiersWithIds(encoderInfo.getTierNames());

                for (TierImpl tier : tiers) {
                    
                    Map<String, String> cvEntryMap = null;
                	String cvName = null;
                    // HS Nov 2015 putting cve ID - cve Description key-value pairs might be better?
                    if (encoderInfo.isIncludeCVDescription()){        
                    	cvEntryMap = new HashMap<String, String>();
                        cvName = tier.getLinguisticType().getControlledVocabularyName();
                    	if (cvName != null){
                    		ControlledVocabulary cv = trans.getControlledVocabulary(cvName);
                    		int defLang = cv.getDefaultLanguageIndex();
                    		for (CVEntry cve : cv) {
                            	cvEntryMap.put(cve.getValue(defLang), cve.getDescription(defLang));
                    		}
                    	}
                	}

                    String participant = tier.getParticipant();
                    
                    if (participant == null) {
                    	participant = EMPTY;
                    }

                    for (AnnotationCore annotation : tier.getAnnotations()) {
                        if (annotation != null) {
                            if (encoderInfo.isIncludeNames()) {
                                writer.write(csvEncodeCond1(tier.getName()));
                                if (encoderInfo.isIncludeParticipants()) {
                                	writer.write(TAB + csvEncodeCond1(participant));
                                }
                            } else {
                            	if (encoderInfo.isIncludeParticipants()) {
                                	writer.write(csvEncodeCond1(participant));
                                }
                            }
                            
                            String tabString = getTabString(annotation,
                            		encoderInfo.isIncludeBeginTime(), encoderInfo.isIncludeEndTime(),
                            		encoderInfo.isIncludeDuration(), encoderInfo.isIncludeHHMM(), encoderInfo.isIncludeSSMS(),
                            		encoderInfo.isIncludeMS(), encoderInfo.isIncludeSMPTE(),
                            		smpteType, mediaOffset);       		
                            
                            if (encoderInfo.isIncludeCVDescription()) { 
                            	// TODO HS Nov 2015 this can fail in case of multilingual CV's; getting the description
                            	// on the basis of the annotation value returns null if the annotation does not have the 
                            	// default language's cve value. Matching on the basis of the CVE ID could be better in some cases
                            	// although it would be possible to get mixed languages results. Or get the description in
                            	// the same language of the value by checking all values of the cve based on the id?
                            	String description = cvEntryMap.get(annotation.getValue());
                            	if (description != null) {
                            		StringBuilder tsBuffer = new StringBuilder(tabString.replace(NEWLINE, TAB));
                            		tsBuffer.append(csvEncodeCond2(description) + NEWLINE);
                            		tabString = tsBuffer.toString();
                            	}
                            }  

                            if (encoderInfo.isIncludeNames() || encoderInfo.isIncludeParticipants()) {
                                writer.write(tabString, 0,
                                        tabString.length() - 1);
                            } else {
                            	writer.write(tabString, 1,
                                        tabString.length() - 2);
                            }
                            
                            // Nov 2015 don't add the file name in a column if it is in the first row
                            if (!encoderInfo.isFileNameInRow()) {
	                            if (encoderInfo.isIncludeFileName()) {
	                            	writer.write(TAB + csvEncodeCond1(file.getName()));
	                            }
	                            
	                            if (encoderInfo.isIncludeFilePath()) {
	                            	writer.write(TAB + csvEncodeCond1(file.getAbsolutePath()));
	                            }
                            }
                            writer.write( NEWLINE);                              
                        }
                    }
                }
                // extra empty line in some cases
                if (encoderInfo.isFileNameInRow() || encoderInfo.isIncludeMediaHeaders()) {
                	writer.write(NEWLINE);
                }
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                ClientLogger.LOG.warning("Could not handle file: " +
                    file.getAbsolutePath());
            }
        }

        try {
        	writer.close();
        } catch (IOException ioe){}
    }

    /**
     * Returns the column headers / labels.
     * @param model the table model used to group annotations with the same begin and end time
     * in one row
     * @param includeBeginTime
     * @param includeEndTime
     * @param includeDuration
     * @param includeHHMM
     * @param includeSSMS
     * @param includeMS
     * @param includeSMPTE
     * @param actualSMPTEType
     * @return the header labels delimited by tabs
     */
    private  String getHeaders(TabExportTableModel model,
        boolean includeBeginTime, boolean includeEndTime,
        boolean includeDuration, boolean includeHHMM, boolean includeSSMS,
        boolean includeMS, boolean includeSMPTE, String actualSMPTEType) {
        StringBuilder buf = new StringBuilder();

        if (includeBeginTime) {
            if (includeHHMM) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode")) + TAB);
            }

            if (includeSSMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds")) + TAB);
            }

            if (includeMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec")) + TAB);
            }

            if (includeSMPTE) {
                if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (actualSMPTEType == Constants.PAL_50_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) + TAB);
                } else {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        if (includeEndTime) {
            if (includeHHMM) {
                buf.append(csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.TimeCode")) +
                    TAB);
            }

            if (includeSSMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.Seconds")) +
                    TAB);
            }

            if (includeMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.MilliSec")) +
                    TAB);
            }

            if (includeSMPTE) {
                if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (actualSMPTEType == Constants.PAL_50_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) + TAB);
                } else {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        if (includeDuration) {
            if (includeHHMM) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode")) + TAB);
            }

            if (includeSSMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds")) + TAB);
            }

            if (includeMS) {
                buf.append(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec")) + TAB);
            }

            if (includeSMPTE) {
                if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (actualSMPTEType == Constants.PAL_50_STRING) {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) + TAB);
                } else {
                    buf.append(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        for (int i = 2; i < model.getColumnCount(); i++) {
            buf.append(csvEncodeCond1(model.getColumnName(i)));

            if (i != (model.getColumnCount() - 1)) {
                buf.append(TAB);
            }
        }        buf.append(NEWLINE);

        return buf.toString();
    }

    private  void writeRows(BufferedWriter writer,
        TabExportTableModel model, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        String smpteType, long mediaOffset, boolean includeFileName, boolean includeFilePath ) throws IOException {
        long bt;
        long et;
        Object value;

        for (int i = 0; i < model.getRowCount(); i++) {
            bt = ((Long) model.getValueAt(i, 0)).longValue() + mediaOffset;
            et = ((Long) model.getValueAt(i, 1)).longValue() + mediaOffset;

            if (includeBeginTime) {
                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(bt) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(bt / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(bt + TAB);
                }

                if (includeSMPTE) {
                	if (smpteType == null || smpteType == Constants.PAL_STRING) {
                        writer.write(TimeFormatter.toTimecodePAL(bt) + TAB);
                    } else if (smpteType == Constants.PAL_50_STRING) {
                    	writer.write(TimeFormatter.toTimecodePAL50(bt) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                    }
                }
            }

            if (includeEndTime) {
                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(et) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(et / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(et + TAB);
                }

                if (includeSMPTE) {
                	if (smpteType == null || smpteType == Constants.PAL_STRING) {
                        writer.write(TimeFormatter.toTimecodePAL(et) + TAB);
                    } else if (smpteType == Constants.PAL_50_STRING) {
                    	writer.write(TimeFormatter.toTimecodePAL50(et) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(et) +
                            TAB);
                    }              
                }
            }

            if (includeDuration) {
                long d = et - bt;

                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(d) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(d / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(d + TAB);
                }
                // Nov 2015 if writing a duration in SMPTE does make no sense then at least a tab should be written
                if (includeSMPTE) {
                	if (smpteType == null || smpteType == Constants.PAL_STRING) {
                		writer.write(TimeFormatter.toTimecodePAL(d) + TAB);
                	} else if (smpteType == Constants.PAL_50_STRING){
                		writer.write(TimeFormatter.toTimecodePAL50(d) + TAB);
                	} else {
                		writer.write(TimeFormatter.toTimecodeNTSC(d) +
                            TAB);
                	}
                }
            }

            // write annotations in the columns
            for (int j = 2; j < model.getColumnCount(); j++) {
                value = model.getValueAt(i, j);

                if (value instanceof String) {
                    writer.write(csvEncodeCond2((String) value));
                }

                if (j != (model.getColumnCount() - 1)) {
                    writer.write(TAB);
                }
            }
            
            if (model.getFileName() != null) {
            	if(includeFileName) {
					writer.write(TAB + csvEncodeCond1(model.getFileName()));
				}
            	if(includeFilePath) {
					writer.write(TAB + csvEncodeCond1(model.getAbsoluteFilePath()));
				}            		
            }

            writer.write(NEWLINE);
        }

        writer.write(NEWLINE);
    }

    /**
     * Exports a List of AnnotationCores to Tab limited text with default settings
     *
     * @param tierName the tier name to export
     * @param annotations the annotations included in the export
     * @param exportFile the file to write to
     *
     * @throws IOException
     */
     public void exportAnnotations(String tierName, List<AnnotationCore> annotations,
        File exportFile) throws IOException {
        if (exportFile == null) {
            return;
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    "UTF-8"));

        for (int i = 0; i < annotations.size(); i++) {
            if (annotations.get(i) instanceof AnnotationCore) {
                writer.write(csvEncodeCond1(tierName) +
                    getTabString((AnnotationCore) annotations.get(i)));
            }
        }
        
        writer.close();
    }
    
    /**
     * Produces a list of strings, one for each media descriptor.
     * Each line is enclosed in double quotes, so suitable for csv output.
     * 
     * @param transcription the transcription containing the descriptors
     * @return a list of strings of null
     */
     private List<String> getMediaHeaders(Transcription transcription) {
    	if (transcription != null && transcription.getMediaDescriptors() != null && 
    			!transcription.getMediaDescriptors().isEmpty()) {
    		List<String> lines = new ArrayList<String>(transcription.getMediaDescriptors().size());
    		for (MediaDescriptor md : transcription.getMediaDescriptors()) {
    			lines.add("\"#" + md.mediaURL + " -- offset: " + md.timeOrigin + "\"");
    		}
    		return lines;
    	}
    	return null;
    }

    /**
     * Creates a tab delimited string.
     *
     * @param annotationCore the annotation
     *
     * @return String
     */
     public String getTabString(AnnotationCore annotationCore) {
        return getTabString(annotationCore, true, true);
    }

    /**
     * Creates a tab delimited string.
     *
     * @param annotationCore
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in second.milliseconds format
     *
     * @return String
     */
     public String getTabString(AnnotationCore annotationCore,
        boolean HHMMformat, boolean SSMSFormat) {
        return getTabString(annotationCore, true, true, true, HHMMformat,
            SSMSFormat, false); 
    }
    

    /**
     * Creates a tab delimited string with time information and the annotation
     * value.
     *
     * @param annotationCore the annotation
     * @param beginTime include begin time in output
     * @param endTime include end time in output
     * @param duration include duration in output
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in sec.milliseconds format
     * @param MSFormat if true, output of times in milliseconds format
     *
     * @return String the tab separated result string
     */
     public String getTabString(AnnotationCore annotationCore,
        boolean beginTime, boolean endTime, boolean duration,
        boolean HHMMformat, boolean SSMSFormat, boolean MSFormat) {
        return getTabString(annotationCore, beginTime, endTime, duration,
            HHMMformat, SSMSFormat, MSFormat, false, null, 0L);
    }

    /**
     * Creates a tab delimited string with time information and the annotation
     * value.
     *
     * @param annotationCore the annotation
     * @param beginTime include begin time in output
     * @param endTime include end time in output
     * @param duration include duration in output
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in sec.milliseconds format
     * @param MSFormat if true, output of times in milliseconds format
     * @param SMPTEFormat if true, output times in SMPTE time code format
     * @param actualSMPTEType if SMPTEFormat is true the output can be PAL time code,
     * PAL-50fps or NTSC drop frame time code (one of the Constant Strings)
     * @param mediaOffset the (master) media offset to be added to the annotations' time values
     *
     * @return String the tab separated result string
     */
     public String getTabString(AnnotationCore annotationCore,
        boolean beginTime, boolean endTime, boolean duration,
        boolean HHMMformat, boolean SSMSFormat, boolean MSFormat,
        boolean SMPTEFormat, String actualSMPTEType, long mediaOffset) {
        StringBuilder sb = new StringBuilder(TAB);

        long bt = annotationCore.getBeginTimeBoundary() + mediaOffset;
        long et = annotationCore.getEndTimeBoundary() + mediaOffset;
        
        // begin time
        if (beginTime) {
            if (HHMMformat) {
                sb.append(TimeFormatter.toString(bt) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(bt / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(bt + TAB);
            }

            if (SMPTEFormat) {
                if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
                    sb.append(TimeFormatter.toTimecodePAL(bt) + TAB);
                } else if (actualSMPTEType == Constants.PAL_50_STRING) {
                    sb.append(TimeFormatter.toTimecodePAL50(bt) + TAB);
                } else {
                    sb.append(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                }
            }
        }

        // end time
        if (endTime) {
            if (HHMMformat) {
                sb.append(TimeFormatter.toString(et) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(et / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(et + TAB);
            }

            if (SMPTEFormat) {
                if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
                    sb.append(TimeFormatter.toTimecodePAL(et) + TAB);
                } else if (actualSMPTEType == Constants.PAL_50_STRING) {
                    sb.append(TimeFormatter.toTimecodePAL50(et) + TAB);
                } else {
                    sb.append(TimeFormatter.toTimecodeNTSC(et) + TAB);
                }
            }
        }

        // duration
        if (duration) {
            long d = et - bt;

            if (HHMMformat) {
                sb.append(TimeFormatter.toString(d) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(d / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(d + TAB);
            }
            // Jan 2016 if writing a duration in SMPTE does make no sense then at least a tab should be written?
            if (SMPTEFormat) {
            	if (actualSMPTEType == null || actualSMPTEType == Constants.PAL_STRING) {
            		sb.append(TimeFormatter.toTimecodePAL(d) + TAB);
            	} else if (actualSMPTEType == Constants.PAL_50_STRING) {
            		sb.append(TimeFormatter.toTimecodePAL50(d) + TAB);
            	} else {
            		sb.append(TimeFormatter.toTimecodeNTSC(d) + TAB);
            	}
            }
        }        	
        
        sb.append(csvEncodeCond2(annotationCore.getValue()) + NEWLINE);

        return sb.toString();
    }

 	/**
 	 * Conditionally encodes the input text for CSV output. If the csvEncodeText
 	 * flag is false, the input text is returned.
 	 * 
 	 * @param inputText not null
 	 * @return the string with new line characters replaced by spaces and, if 
 	 * configured so, enclosed in quotation marks and internal quotation marks
 	 * replaced by double quotation marks
 	 */
 	private String csvEncodeCond1(String inputText) {
 		if (csvEncodeText) {
 			return SQ + inputText.replace('\n', ' ').replaceAll(SQ, DQ) + SQ;
 		} else {
 			return inputText;
 		}
 	}
 	
	/**
	 * Conditionally encodes the input text for CSV output. If the csvEncodeText
	 * flag is false new line characters are still replaced by white spaces.
	 * 
	 * @param inputText not null
	 * @return the string with new line characters replaced by spaces and, if 
	 * configured so, enclosed in quotation marks and internal quotation marks
	 * replaced by double quotation marks
	 */
	private String csvEncodeCond2(String inputText) {
		if (csvEncodeText) {
			return SQ + inputText.replace('\n', ' ').replaceAll(SQ, DQ) + SQ;
		} else {
			return inputText.replace('\n', ' ');
		}
	}
}
