package mpi.eudico.client.annotator.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.AnnotationSlicer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoFiles;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoTrans;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.TimeFormatter;
import mpi.eudico.util.TimeInterval;
import mpi.eudico.util.TimeRelation;


/**
 * A class for exporting one or multiple transcriptions as tab delimited text,
 * whereby separate columns are created for each tier and the value of
 * spanning annotations can be repeated in rows of spanned annotations.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version Nov 2015 modified to accept {@link DelimitedTextEncoderInfo} objects
 * @version Aug 2017 support for "sliced" output and for CSV encoded output
 */
public class ExportTabdelimited {
    /** the delimiter, default a tab and a comma in case of CSV output */
	private String TAB = "\t";
    /** new line string (might make this customizable, e.g. \r\n) */
    final private String NEWLINE = "\n";

    private String COMMA = ",";
    private final String SQ = "\"";
    private final String DQ = "\"\"";
    private boolean csvEncodeText = false;
    
    /**
     * Creates a new ExportTabdelimited instance
     */
    public ExportTabdelimited() {
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
     * Exports a single transcription. With a separate column for each selected tier.<br>
     * 
     * @param encoderInfo the encoder object containing all configuration parameters, not null
     */
    public void exportTiersColumnPerTier(DelimitedTextEncoderInfoTrans encoderInfo) throws IOException {
        if (encoderInfo.getExportFile() == null) {
            throw new IOException("Encoder: no destination file specified for export");
        }
        
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
	
	        if (encoderInfo.getTierNames() == null) {
	        	List<String> includedTiers = new ArrayList<String>();
	            // use all
	            List<? extends Tier> tiers = encoderInfo.getTranscription().getTiers();
	
	            for (Tier t : tiers) {
	                includedTiers.add(t.getName());
	            }
	            encoderInfo.setTierNames(includedTiers);
	        }
	        // Nov 2015 write media headers if selected
	        if (encoderInfo.getMediaHeaderLines() != null) {
	        	for (String line : encoderInfo.getMediaHeaderLines()) {
	        		writer.write(line + NEWLINE);
	        	}
	        }
	        
	        writeHeaders(writer, encoderInfo);
	        
	        writeTiersColumnPerTier(writer, encoderInfo.getTranscription(), encoderInfo);

        } catch (IOException ex) {
        	throw ex;
        } catch (Exception ex) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + ex.getMessage());
        } finally {
	        try {
	            if (writer != null) {
	            	writer.close();
	            }
	        } catch (IOException iioo) {
	            //iioo.printStackTrace();
	        }
        }
    }
    		
    /**
     * Exports the selected tiers from the selected files to one tab delimited
     * file. There are separate columns per tier and the files are treated
     * sequentially; no attempts are made to put annotations from different
     * files into one row.  <br>
     * 
     * 
     * @param encoderInfo the object containing all information and parameters for the output, not null
     * @throws IOException any IO related exception also thrown in case of incomplete input  
     */
    public void exportTiersColumnPerTierForFiles(DelimitedTextEncoderInfoFiles encoderInfo)
            		throws IOException {
    	if (encoderInfo.getExportFile() == null) {
    		throw new IOException("No destination file specified for export");
    	}

    	if ((encoderInfo.getFiles() == null) || (encoderInfo.getFiles().isEmpty())) {
    		throw new IOException("No files specified for export");
    	}

    	if ((encoderInfo.getTierNames() == null) || encoderInfo.getTierNames().isEmpty()) {
    		throw new IOException("No tiers specified for export");
    	}
    	
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
    	BufferedWriter writer = null;

    	try {
			FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
			OutputStreamWriter osw = null;

			try {
				osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
			} catch (UnsupportedCharsetException uce) {
				osw = new OutputStreamWriter(out, "UTF-8");
			}

			writer = new BufferedWriter(osw);

    		writeHeaders(writer, encoderInfo);

    		// create transcriptions of files and write
    		for (File file : encoderInfo.getFiles()) {
    			if (file == null) {
    				continue;
    			}

    			try {
    				TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());

    				writeTiersColumnPerTier(writer, trans, encoderInfo);
    			} catch (Exception ex) {
    				// catch any exception that could occur and continue
    				ClientLogger.LOG.warning("Could not handle file: " +
    						file.getAbsolutePath());
    			}
    		}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			// FileNotFound, Security or UnsupportedEncoding exceptions
			throw new IOException("Cannot write to file: " + ex.getMessage());
    	} finally {
    		try {
    			if (writer != null) {
    				writer.close();
    			}
    		} catch (IOException iioo) {
    			//iioo.printStackTrace();
    		}
    	}
    	
    }
    
    /**
     * Note: first set the boolean's for inclusions and formats.
     * Nov 2015 changed to private
     *
     * @param writer the writer to write the results to
     * @param transcription the transcription
     * @param encoderInfo the encoder parameter object
     *
     * @throws IOException if there is no valid writer object
     * @throws NullPointerException if the transcription is null
     */
    private void writeTiersColumnPerTier(BufferedWriter writer,
        TranscriptionImpl transcription, 
        DelimitedTextEncoderInfo encoderInfo) throws IOException {
        if (transcription == null) {
            throw new NullPointerException("The transcription is null");
        }

        if (writer == null) {
            throw new IOException("No writer supplied to write to");
        }
        
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
        // Nov 2015 handle media offset, both in case of single transcription export mode
        // and in case of multiple file export mode taken from the media descriptors
        long mediaOffset = 0L;
        
        if (encoderInfo.isAddMasterMediaOffset()) {
        	if (transcription.getMediaDescriptors() != null && !transcription.getMediaDescriptors().isEmpty()) {
        		mediaOffset = transcription.getMediaDescriptors().get(0).timeOrigin;
        	}
        }
        
        // if table headers would be written for each transcription it could be done after the following two blocks
    	// Nov 2015 new option, file name or path in a row  
        if (encoderInfo instanceof DelimitedTextEncoderInfoFiles) {
        	DelimitedTextEncoderInfoFiles ecoderInfoFiles = (DelimitedTextEncoderInfoFiles) encoderInfo;
        	if (ecoderInfoFiles.isFileNameInRow()) {
        		writer.write(NEWLINE); //insert empty line
        		if (ecoderInfoFiles.isIncludeFileName()) {
        			writer.write("\"#" + transcription.getName() + "\"" + NEWLINE);
        		} 
        		// write the path if neither file name nor file path is selected but file name in a row is
        		if ( ecoderInfoFiles.isIncludeFilePath() || (!ecoderInfoFiles.isIncludeFileName() && !ecoderInfoFiles.isIncludeFilePath()) ){
        			writer.write("\"#" + transcription.getPathName() /*getFullPath()?*/ + "\"" + NEWLINE);
        		}
        	}
        	// Nov 2015 new option, linked media information
        	if (ecoderInfoFiles.isIncludeMediaHeaders()) {
        		List<String> medHeaders = getMediaHeaders(transcription);
    			if (medHeaders != null) {
            		if (!ecoderInfoFiles.isFileNameInRow()) {
            			writer.write(NEWLINE); //insert empty line
            		}
    				for (String s : medHeaders) {
    					writer.write(s);
    					writer.write(NEWLINE);
    				}
    			}         		
        	}
        }

        // first create "fully filled" blocks of annotations in the same tree,
        // but without deleting the rows only containing the toplevel tier 
        // (+ symbolically associated tiers)

        // create a tree
        DefaultMutableTreeNode rootNode = createTree(encoderInfo.getTierNames(),
                transcription);
        List<MinimalTabExportTableModel> allBlocks = new ArrayList<MinimalTabExportTableModel>();

        // loop over the "toplevel" tiers, find annotations within the selected interval
        // add the depending annotations to each and create a "filled" table
        DefaultMutableTreeNode node;

        // loop over the "toplevel" tiers, find annotations within the selected interval
        // add the depending annotations to each and create a "filled" table
        DefaultMutableTreeNode chNode;
        List<Annotation> allAnnotations = new ArrayList<Annotation>(100);      
        
        for (int i = 0; i < rootNode.getChildCount(); i++) {
        	Map<String, String> cvEntryMap = null;
        	String cvName = null;
             
            node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            String tierName = (String) node.getUserObject();
            TierImpl tier = transcription.getTierWithId(tierName);
                        
            if (tier != null) {
            	if(encoderInfo.isIncludeCVDescription()){
            		cvEntryMap = new HashMap<String, String>();
                    cvName = tier.getLinguisticType().getControlledVocabularyName();
                	if(cvName != null){
                		ControlledVocabulary cv = transcription.getControlledVocabulary(cvName);
                		int defLang = cv.getDefaultLanguageIndex();
                		for (CVEntry cve : cv) {
                        	cvEntryMap.put(cve.getValue(defLang), cve.getDescription(defLang));
                		}
                	}
            	}
            	
                List<AbstractAnnotation> annos = tier.getAnnotations();

                for (int j = 0; j < annos.size(); j++) {
                    allAnnotations.clear();

                    Annotation ann = annos.get(j);

                    // create a block per toplevel annotation
                    if (ann != null) {
                        if (TimeRelation.overlaps(ann, encoderInfo.getBeginTime(), encoderInfo.getEndTime())) {
                            allAnnotations.add(ann);

                            long b = ann.getBeginTimeBoundary();
                            long e = ann.getEndTimeBoundary();

							@SuppressWarnings("unchecked")
							Enumeration<DefaultMutableTreeNode> nodeEn = node.depthFirstEnumeration();

                            //Enumeration nodeEn = node.breadthFirstEnumeration();
                            while (nodeEn.hasMoreElements()) {
                                chNode = nodeEn.nextElement();

                                if (chNode == node) {
                                    continue;
                                }

                                tierName = (String) chNode.getUserObject();
                                tier = transcription.getTierWithId(tierName);

                                if (tier != null) {
                                    List<AbstractAnnotation> annos2 = tier.getAnnotations();

                                    for (int k = 0; k < annos2.size(); k++) {
                                        Annotation ann2 = annos2.get(k);

                                        if (ann2 != null) {
                                            if (TimeRelation.overlaps(ann2, b, e)) {
                                                allAnnotations.add(ann2);
                                            }

                                            if (ann2.getBeginTimeBoundary() > e) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            // with the current list of annotations fill a table model
                            MinimalTabExportTableModel tm = null;
                            
                            if (encoderInfo.isRepeatValues() && encoderInfo.isCombineBlocks()) {
                            	tm = new MinimalTabExportTableModel(encoderInfo.getTierNames(),
                                        allAnnotations, cvEntryMap, true, true, 
                                        encoderInfo.isIncludeAnnotationId());
                            } else {
                            	tm = new MinimalTabExportTableModel(encoderInfo.getTierNames(),
                                        allAnnotations, cvEntryMap, true, false, // repeat values is always true?
                                        encoderInfo.isIncludeAnnotationId());
                            }
                            // check for file name/path in a row
                            if (encoderInfo instanceof DelimitedTextEncoderInfoFiles) {
                            	if ( !((DelimitedTextEncoderInfoFiles) encoderInfo).isFileNameInRow() ) {
	                            	if(((DelimitedTextEncoderInfoFiles) encoderInfo).isIncludeFileName()){
	                            		tm.setFileName(transcription.getName());
	                            	}
	                            	if(((DelimitedTextEncoderInfoFiles) encoderInfo).isIncludeFilePath()){
	                            		tm.setFilePath(transcription.getFullPath());
	                            	}
                            	}
                            }

                            if (encoderInfo.isCombineBlocks()) {
                                tm.setSpan(new long[] { b, e });
                                allBlocks.add(tm);
                            } else {
                                writeBlock(writer, tm, encoderInfo, mediaOffset);
                            }
                        }

                        if (ann.getBeginTimeBoundary() > encoderInfo.getEndTime()) {
                            break;
                        }
                    }
                }
            }
        }

        if (encoderInfo.isCombineBlocks()) {
            List<MinimalTabExportTableModel> removableBlocks = new ArrayList<MinimalTabExportTableModel>();

            // combine
            MinimalTabExportTableModel mtm1;

            // combine
            MinimalTabExportTableModel mtm2;
            long[] span1;
            long[] span2;

            for (int i = allBlocks.size() - 1; i >= 0; i--) {
                mtm2 = allBlocks.get(i);
                span2 = mtm2.getSpan();

                for (int j = 0; j < allBlocks.size(); j++) {
                    if (j == i) {
                        continue;
                    }

                    mtm1 = allBlocks.get(j);
                    span1 = mtm1.getSpan();

                    // full overlap, or partial overlap, consider merging rows
                    if ((span1[0] <= span2[0]) && (span1[1] >= span2[1])) {
                        boolean empty = mergeTables(mtm1, mtm2);

                        if (empty) {
                            removableBlocks.add(mtm2);
                        }
                    }
                }
            }

            if (removableBlocks.size() > 0) {
                for (int i = allBlocks.size() - 1; i >= 0; i--) {
                    if (removableBlocks.contains(allBlocks.get(i))) {
                        allBlocks.remove(i);
                    }
                }
            }

            // write the whole bunch
            for (int i = 0; i < allBlocks.size(); i++) {
                writeBlock(writer, allBlocks.get(i), encoderInfo, mediaOffset);
            }
        }
    }

    /**
     * Merges the rows from the second table with the first table (the first
     * table is the  destination). Removes merged rows from the second table.
     * This can lead to loss of a direct relation between rows.
     *
     * @param mtm1 the first table
     * @param mtm2 the second table
     *
     * @return true if all rows have been merged (second table is empty) false
     *         other wise
     */
    private boolean mergeTables(MinimalTabExportTableModel mtm1, MinimalTabExportTableModel mtm2) {
        // merge rows with identical begin and end time
        List<Object> curRow;

        // merge rows with identical begin and end time
        List<Object> otherRow;
        long l1;
        long l2;
        long l3;
        long l4;
        List<Integer> removals = new ArrayList<Integer>();
        Object val;

outerloop: 
        for (int i = 0; i < mtm2.getRows().size(); i++) {
            curRow = mtm2.getRows().get(i);
            l1 = ((Long) curRow.get(1)).longValue();
            l2 = ((Long) curRow.get(2)).longValue();

            for (int j = 0; j < mtm1.getRows().size(); j++) {
                otherRow = mtm1.getRows().get(j);
                l3 = ((Long) otherRow.get(1)).longValue();
                l4 = ((Long) otherRow.get(2)).longValue();

                if ((l1 == l3) && (l2 == l4)) {
                    removals.add(i);

                    for (int k = 3; k < curRow.size(); k++) {
                        val = curRow.get(k);

                        if (val != null) {
                            otherRow.set(k, val);
                        }
                    }

                    continue outerloop;
                } else if ((l3 <= l1) && (l4 >= l2)) {
                    for (int k = 3; k < otherRow.size(); k++) {
                        val = otherRow.get(k);

                        if (val != null) {
                            curRow.set(k, val);
                        }
                    }
                }
            }
        }

        //remove the "normal" removables, after merging
        for (int i = removals.size() - 1; i >= 0; i--) {
        	//System.out.println("Remove row: " + ((Integer) removals.get(i)).intValue());
            mtm2.getRows().remove(removals.get(i).intValue());
        }

        return mtm2.getRows().size() == 0;
    }

    /**
     * Writes the column headers; first time columns, then the tier columns and
     * optionally the file name column.
     *
     * @param writer the writer to write to
     * @param encoderInfo the parameters for encoding
     *
     * @throws IOException any io exception
     */
    private void writeHeaders(BufferedWriter writer, DelimitedTextEncoderInfo encoderInfo)
        throws IOException {
        if (encoderInfo.isIncludeBeginTime()) {
            if (encoderInfo.isIncludeHHMM()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode")) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds")) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec")) + TAB);
            }

            if (encoderInfo.isIncludeSMPTE()) {
                if (encoderInfo.isPalFormat()) {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (encoderInfo.isPal50Format()){
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) +
                        TAB);
                } else {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        if (encoderInfo.isIncludeEndTime()) {
            if (encoderInfo.isIncludeHHMM()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode")) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds")) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec")) + TAB);
            }

            if (encoderInfo.isIncludeSMPTE()) {
                if (encoderInfo.isPalFormat()) {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (encoderInfo.isPal50Format()) {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) + TAB);
                } else {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        if (encoderInfo.isIncludeDuration()) {
            if (encoderInfo.isIncludeHHMM()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode")) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds")) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(csvEncodeCond1(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec")) + TAB);
            }

            if (encoderInfo.isIncludeSMPTE()) {
                if (encoderInfo.isPalFormat()) {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL")) + TAB);
                } else if (encoderInfo.isPal50Format()) {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50")) + TAB);
                } else {
                    writer.write(csvEncodeCond1(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC")) +
                        TAB);
                }
            }
        }

        for (String name : encoderInfo.getTierNames()) {
            writer.write(csvEncodeCond1(name));
            writer.write(TAB);
        }

        if(encoderInfo.isIncludeCVDescription()){
        	writer.write(csvEncodeCond1(ElanLocale.getString("EditCVDialog.Label.CVDescription")));
            writer.write(TAB);
        }
        
        if (encoderInfo instanceof DelimitedTextEncoderInfoFiles) {
        	if ( !((DelimitedTextEncoderInfoFiles) encoderInfo).isFileNameInRow() ) {
            	if( ((DelimitedTextEncoderInfoFiles) encoderInfo).isIncludeFileName() ){
            		writer.write(csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnFileName")) + TAB);
            	}
            	if( ((DelimitedTextEncoderInfoFiles) encoderInfo).isIncludeFilePath() ){
            		writer.write(csvEncodeCond1(ElanLocale.getString("Frame.GridFrame.ColumnFilePath")));
            	}
        	}
        }

        writer.write(NEWLINE);
    }

    /**
     * Writes the contents of a block of annotations. If the first cell of a
     * row contains the Hidden marker, it is ignored.
     *
     * @param writer the writer to write to
     * @param tm the table model holding the data
     * @param encoderInfo the parameters for encoding
     * @param mediaOffset the media offset (in case of the multiple file export this value differs per file)
     *
     * @throws IOException any
     */
    private void writeBlock(BufferedWriter writer, MinimalTabExportTableModel tm, 
    		DelimitedTextEncoderInfo encoderInfo, long mediaOffset)
        throws IOException {
        if (tm == null) {
            ClientLogger.LOG.warning("No table model provided");
            return;
        }

        List<List<Object>> rows = tm.getRows();
        List<Object> row = null;
        long bt;
        long et;
        Object value;

        for (int i = 0; i < rows.size(); i++) {
            row = rows.get(i);
            
            if (row.get(0) == tm.HIDDEN) {
            	continue;
            }
            
            bt = ((Long) row.get(1)).longValue() + mediaOffset;
            et = ((Long) row.get(2)).longValue() + mediaOffset;

            writeTimes(writer, encoderInfo, bt, et);
            /*
            if (encoderInfo.isIncludeBeginTime()) {
                if (encoderInfo.isIncludeHHMM()) {
                    writer.write(TimeFormatter.toString(bt) + TAB);
                }

                if (encoderInfo.isIncludeSSMS()) {
                    writer.write(Double.toString(bt / 1000.0) + TAB);
                }

                if (encoderInfo.isIncludeMS()) {
                    writer.write(bt + TAB);
                }

                if (encoderInfo.isIncludeSMPTE()) {
                    if (encoderInfo.isPalFormat()) {
                        writer.write(TimeFormatter.toTimecodePAL(bt) + TAB);
                    } else if (encoderInfo.isPal50Format()) {
                        writer.write(TimeFormatter.toTimecodePAL50(bt) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                    }
                }
            }

            if (encoderInfo.isIncludeEndTime()) {
                if (encoderInfo.isIncludeHHMM()) {
                    writer.write(TimeFormatter.toString(et) + TAB);
                }

                if (encoderInfo.isIncludeSSMS()) {
                    writer.write(Double.toString(et / 1000.0) + TAB);
                }

                if (encoderInfo.isIncludeMS()) {
                    writer.write(et + TAB);
                }

                if (encoderInfo.isIncludeSMPTE()) {                   
                    if (encoderInfo.isPalFormat()) {
                        writer.write(TimeFormatter.toTimecodePAL(et) + TAB);
                    } else if (encoderInfo.isPal50Format()) {
                        writer.write(TimeFormatter.toTimecodePAL50(et) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(et) +
                            TAB);
                    }
                }
            }

            if (encoderInfo.isIncludeDuration()) {
                long d = et - bt;

                if (encoderInfo.isIncludeHHMM()) {
                    writer.write(TimeFormatter.toString(d) + TAB);
                }

                if (encoderInfo.isIncludeSSMS()) {
                    writer.write(Double.toString(d / 1000.0) + TAB);
                }

                if (encoderInfo.isIncludeMS()) {
                    writer.write(d + TAB);
                }
                // Nov 2015 this check was not there before. 
                // If duration in SMPTE would make no sense then at least a TAB should be written
                if (encoderInfo.isIncludeSMPTE()) {                   
                    if (encoderInfo.isPalFormat()) {
                        writer.write(TimeFormatter.toTimecodePAL(d) + TAB);
                    } else if (encoderInfo.isPal50Format()) {
                        writer.write(TimeFormatter.toTimecodePAL50(d) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(d) +
                            TAB);
                    }
                }
            }
			*/
            // write annotations in the columns
            for (int j = 3; j < row.size(); j++) {
                value = row.get(j);

                if (value != null) {
                    if (value instanceof String) {
                        writer.write(csvEncodeCond2((String) value));
                    } else {
                        writer.write(csvEncodeCond2(value.toString()));
                    }
                }

                if (j != (row.size() - 1)) {
                    writer.write(TAB);
                }
            }

            if (tm.getFileName() != null) {
                writer.write(TAB + csvEncodeCond1(tm.getFileName()));
            }
            
            if(tm.getFilePath() != null){
            	writer.write(TAB + csvEncodeCond1(tm.getFilePath()));
            }

            writer.write(NEWLINE);
        }

        //writer.write(NEWLINE); 
    }

    /**
     * Creates a tree of t he included tiers.
     *
     * @param includedTiers the included tiers names
     * @param transcription the transcription
     *
     * @return the root of a tree
     */
    private DefaultMutableTreeNode createTree(List<String> includedTiers,
        Transcription transcription) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        List<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>(includedTiers.size());

        for (String name : includedTiers) {
            nodeList.add(new DefaultMutableTreeNode(name));
        }

        String tierName;
        String parentName;
        TierImpl tier;
        TierImpl parentTier;

        for (DefaultMutableTreeNode node : nodeList) {
            tierName = (String) node.getUserObject();
            tier = (TierImpl) transcription.getTierWithId(tierName);

            if (tier != null) {
                parentTier = tier.getParentTier();

                if (parentTier == null) {
                    rootNode.add(node);
                } else {
                    parentName = parentTier.getName();

                    if (!includedTiers.contains(parentName)) {
                        // look for ancestors
                        while (true) {
                            parentTier = parentTier.getParentTier();

                            if (parentTier == null) {
                                parentName = null;

                                break;
                            }

                            parentName = parentTier.getName();

                            if (includedTiers.contains(parentName)) {
                                break;
                            }
                        }
                    }

                    if (parentName == null) {
                        rootNode.add(node);
                    } else {
                        // find the parent node
                        for (DefaultMutableTreeNode node2 : nodeList) {
                            if (parentName.equals(node2.getUserObject())) {
                                node2.add(node);

                                break;
                            }
                        }
                    }
                }

                // check
                if (node.getParent() == null) {
                    ClientLogger.LOG.warning("Tier " + tierName +
                        " could not be added to a parent");
                    rootNode.add(node);
                }
            }
        } // tree created

        return rootNode;
    }
    
    /**
     * Produces a list of strings, one for each media descriptor
     * (This is a copy of the same method in Transcription2TabDelimitedText. Re-factor?)
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
    
    //########################################################################################
    //experimental
    /**
     * Sliced export means that all start and end times of all annotations on selected tiers
     * are collected in one sorted list and each interval between the times at index n and n + 1
     * is exported (one row in the table) if there is at least one annotation on one tier
     * in that interval.
     * 
     * @param encoderInfo contains the settings for the export
     * @throws IOException
     */
    public void exportTiersSliced(DelimitedTextEncoderInfoTrans encoderInfo) throws IOException {
        if (encoderInfo.getExportFile() == null) {
            throw new IOException("Encoder: no destination file specified for export");
        }
        
    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
	
	        if (encoderInfo.getTierNames() == null) {
	        	List<String> includedTiers = new ArrayList<String>();
	            // use all
	            List<? extends Tier> tiers = encoderInfo.getTranscription().getTiers();
	
	            for (Tier t : tiers) {
	                includedTiers.add(t.getName());
	            }
	            encoderInfo.setTierNames(includedTiers);
	        }
	        // write media headers if selected
	        if (encoderInfo.getMediaHeaderLines() != null) {
	        	for (String line : encoderInfo.getMediaHeaderLines()) {
	        		writer.write(line + NEWLINE);
	        	}
	        }
	        
	        writeHeaders(writer, encoderInfo);
	        
	        writeSlices(writer, encoderInfo.getTranscription(), encoderInfo);

        } catch (IOException ex) {
        	throw ex;
        } catch (Exception ex) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + ex.getMessage());
        } finally {
	        try {
	            if (writer != null) {
	            	writer.close();
	            }
	        } catch (IOException iioo) {
	            //iioo.printStackTrace();
	        }
        }        
    }
    
    /**
     * 
     * @param encoderInfo the encoder info containing the files and tiers and other settings
     * @throws IOException
     * @see {@link #exportTiersSliced(DelimitedTextEncoderInfoTrans)}
     */
    public void exportTiersSlicedForFiles(DelimitedTextEncoderInfoFiles encoderInfo) throws IOException {
    	if (encoderInfo.getExportFile() == null) {
    		throw new IOException("No destination file specified for export");
    	}

    	if ((encoderInfo.getFiles() == null) || (encoderInfo.getFiles().isEmpty())) {
    		throw new IOException("No files specified for export");
    	}

    	if ((encoderInfo.getTierNames() == null) || encoderInfo.getTierNames().isEmpty()) {
    		throw new IOException("No tiers specified for export");
    	}

    	if (encoderInfo.isExportCSVFormat()) {
    		TAB = COMMA;
    		csvEncodeText = true;
    	}
    	
    	BufferedWriter writer = null;

    	try {
			FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
			OutputStreamWriter osw = null;

			try {
				osw = new OutputStreamWriter(out, encoderInfo.getCharEncoding());
			} catch (UnsupportedCharsetException uce) {
				osw = new OutputStreamWriter(out, "UTF-8");
			}

			writer = new BufferedWriter(osw);

    		writeHeaders(writer, encoderInfo);
    		
    		// create transcriptions of files and write
    		for (File file : encoderInfo.getFiles()) {
    			if (file == null) {
    				if (ClientLogger.LOG.isLoggable(Level.INFO)) {
    					ClientLogger.LOG.info("A tier in the list is null");
    				}
    				continue;
    			}

    			try {
    				TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());

    				writeSlices(writer, trans, encoderInfo);
    			} catch (Exception ex) {
    				// catch any exception that could occur and continue
    				ClientLogger.LOG.warning("Could not handle file: " +
    						file.getAbsolutePath());
    			}
    		}
    		
    	} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			// FileNotFound, Security or UnsupportedEncoding exceptions
			throw new IOException("Cannot write to file: " + ex.getMessage());
    	} finally {
    		try {
    			if (writer != null) {
    				writer.close();
    			}
    		} catch (IOException iioo) {
    			//iioo.printStackTrace();
    		}
    	}
    }
    
    /**
     * Write the "sliced" content of a single transcription
     * @param writer the writer
     * @param transcription the transcription to write
     * @param encoderInfo the settings
     * @throws IOException 
     * @see {@link #exportTiersSliced(DelimitedTextEncoderInfoTrans)} and 
     * 	{@link #exportTiersSlicedForFiles(DelimitedTextEncoderInfoFiles)}
     */
    private void writeSlices(BufferedWriter writer,
            TranscriptionImpl transcription, 
            DelimitedTextEncoderInfo encoderInfo) throws IOException {
        if (transcription == null) {
            throw new NullPointerException("The transcription is null");
        }

        if (writer == null) {
            throw new IOException("No writer supplied to write to");
        }
        
        long mediaOffset = 0L;
        
        if (encoderInfo.isAddMasterMediaOffset()) {
        	if (transcription.getMediaDescriptors() != null && !transcription.getMediaDescriptors().isEmpty()) {
        		mediaOffset = transcription.getMediaDescriptors().get(0).timeOrigin;
        	}
        }
        // check file name in row for multiple file export
        // if table headers would be written for each transcription it could be done after the following two blocks  
        if (encoderInfo instanceof DelimitedTextEncoderInfoFiles) {
        	DelimitedTextEncoderInfoFiles ecoderInfoFiles = (DelimitedTextEncoderInfoFiles) encoderInfo;
        	if (ecoderInfoFiles.isFileNameInRow()) {
        		writer.write(NEWLINE); //insert empty line
        		if (ecoderInfoFiles.isIncludeFileName()) {
        			writer.write("\"#" + transcription.getName() + "\"" + NEWLINE);
        		} 
        		// write the path if neither file name nor file path is selected but file name in a row is
        		if ( ecoderInfoFiles.isIncludeFilePath() || (!ecoderInfoFiles.isIncludeFileName() && !ecoderInfoFiles.isIncludeFilePath()) ){
        			writer.write("\"#" + transcription.getPathName() /*getFullPath()?*/ + "\"" + NEWLINE);
        		}
        	}
        	// Nov 2015 new option, linked media information
        	if (ecoderInfoFiles.isIncludeMediaHeaders()) {
        		List<String> medHeaders = getMediaHeaders(transcription);
    			if (medHeaders != null) {
            		if (!ecoderInfoFiles.isFileNameInRow()) {
            			writer.write(NEWLINE); //insert empty line
            		}
    				for (String s : medHeaders) {
    					writer.write(s);
    					writer.write(NEWLINE);
    				}
    			}         		
        	}
        }
        
        // create list of TierImpl objects 
        List<TierImpl> tierList = new ArrayList<TierImpl>(encoderInfo.getTierNames().size());
        // create list of indices of existing tiers in this transcription, relevant for multiple file export
        List<Integer> tierIndexList = new ArrayList<Integer>(tierList.size());
        
        for (int i = 0; i < encoderInfo.getTierNames().size(); i++) {
        	String name = encoderInfo.getTierNames().get(i);
        	TierImpl t = transcription.getTierWithId(name);
        	if (t != null) {
        		tierList.add(t);
        		tierIndexList.add(tierList.indexOf(t));
        	} else {
        		tierIndexList.add(-1);
        	}
        }
        
        List<Long> timeValues = AnnotationSlicer.getTimeValues(tierList);
        // check if not empty?
        if (timeValues == null || timeValues.isEmpty()) {
        	// write new line
        	writer.write(NEWLINE);
        	return;
        }
        SortedMap<TimeInterval, List<Annotation>> sliceMap = 
        		AnnotationSlicer.getIntervalAnnotationMap(timeValues, tierList);
        // check null, empty
        if (sliceMap == null || sliceMap.isEmpty()) {
        	// write new line?
        	writer.write(NEWLINE);
        	return;
        }
        Iterator<TimeInterval> keySetIter = sliceMap.keySet().iterator();
        while (keySetIter.hasNext()) {
        	TimeInterval nextKey = keySetIter.next();
        	List<Annotation> annList = sliceMap.get(nextKey);
        	long bt = nextKey.getBeginTime() + mediaOffset;
        	long et = nextKey.getEndTime() + mediaOffset;
        	
        	writeTimes(writer, encoderInfo, bt, et);
        	
        	for (Integer i : tierIndexList) {
        		if (i == -1) {
        			writer.write(TAB);
        		} else {// i is an index in the actual tier list
        			Annotation a = annList.get(i);
            		if (a != null) {
            			if (!encoderInfo.isIncludeAnnotationId()) {
            				writer.write(csvEncodeCond2(a.getValue()));            				        			
            			} else {
            				writer.write(csvEncodeCond2(a.getValue() + " [" + a.getId() + "]"));
            			}
            		}
            		writer.write(TAB);
        		}
        	}
        	
            if (encoderInfo instanceof DelimitedTextEncoderInfoFiles) {
            	DelimitedTextEncoderInfoFiles ecoderInfoFiles = (DelimitedTextEncoderInfoFiles) encoderInfo;
            	if (ecoderInfoFiles.isIncludeFileName()) {
            		writer.write(TAB + csvEncodeCond1(transcription.getName()));
            	}
            	if (ecoderInfoFiles.isIncludeFilePath()) {
            		writer.write(TAB + csvEncodeCond1(transcription.getPathName()));
            	}
            }
        	/*
        	        if (a != null) {
            			writer.write("\"");
            			writer.write(a.getValue().replace(NEWLINE, " ").replaceAll("\"", "\"\""));
            			if (encoderInfo.isIncludeAnnotationId()) {
            				writer.write(" [" + a.getId() + "]");        			
            			}
            			writer.write("\"");
            		}
        	 */
        	writer.write(NEWLINE);
        }
        
    }
    
    /**
     * Writes begin time, end time and/or duration in all selected formats.
     * 
     * @param writer the writer that performs the writing
     * @param encoderInfo the encoder object containing the settings
     * @param bt the begin time
     * @param et the end time
     * @throws IOException
     */
    private void writeTimes(BufferedWriter writer, DelimitedTextEncoderInfo encoderInfo, 
    		long bt, long et) throws IOException {
        if (encoderInfo.isIncludeBeginTime()) {
            if (encoderInfo.isIncludeHHMM()) {
                writer.write(TimeFormatter.toString(bt) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(Double.toString(bt / 1000.0) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(bt + TAB);
            }

            if (encoderInfo.isIncludeSMPTE()) {
                if (encoderInfo.isPalFormat()) {
                    writer.write(TimeFormatter.toTimecodePAL(bt) + TAB);
                } else if (encoderInfo.isPal50Format()) {
                    writer.write(TimeFormatter.toTimecodePAL50(bt) + TAB);
                } else {
                    writer.write(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                }
            }
        }

        if (encoderInfo.isIncludeEndTime()) {
            if (encoderInfo.isIncludeHHMM()) {
                writer.write(TimeFormatter.toString(et) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(Double.toString(et / 1000.0) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(et + TAB);
            }

            if (encoderInfo.isIncludeSMPTE()) {                   
                if (encoderInfo.isPalFormat()) {
                    writer.write(TimeFormatter.toTimecodePAL(et) + TAB);
                } else if (encoderInfo.isPal50Format()) {
                    writer.write(TimeFormatter.toTimecodePAL50(et) + TAB);
                } else {
                    writer.write(TimeFormatter.toTimecodeNTSC(et) +
                        TAB);
                }
            }
        }

        if (encoderInfo.isIncludeDuration()) {
            long d = et - bt;

            if (encoderInfo.isIncludeHHMM()) {
                writer.write(TimeFormatter.toString(d) + TAB);
            }

            if (encoderInfo.isIncludeSSMS()) {
                writer.write(Double.toString(d / 1000.0) + TAB);
            }

            if (encoderInfo.isIncludeMS()) {
                writer.write(d + TAB);
            }
            // Nov 2015 this check was not there before. 
            // If duration in SMPTE would make no sense then at least a TAB should be written
            if (encoderInfo.isIncludeSMPTE()) {                   
                if (encoderInfo.isPalFormat()) {
                    writer.write(TimeFormatter.toTimecodePAL(d) + TAB);
                } else if (encoderInfo.isPal50Format()) {
                    writer.write(TimeFormatter.toTimecodePAL50(d) + TAB);
                } else {
                    writer.write(TimeFormatter.toTimecodeNTSC(d) +
                        TAB);
                }
            }
        }
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
