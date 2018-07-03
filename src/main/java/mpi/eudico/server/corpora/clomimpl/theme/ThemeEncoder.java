package mpi.eudico.server.corpora.clomimpl.theme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.util.ServerLogger;

/**
 * Exports tiers to a Theme text file.
 * 
 * @author Han Sloetjes
 */
public class ThemeEncoder implements AnnotationDocEncoder {
    private final String NEW_LINE = "\r\n";// use the windows line separator?
    private final String TAB = "\t";
    private final String B = "b";
    private final String E = "e";
    private final String COMMA = ",";
    
    /**
     * No-arg constructor.
     */
	public ThemeEncoder() {
		super();
	}

	/**
	 * Writes the annotations of the specified tiers to a file.
	 * See above for a description of the format.
	 * 
	 * TODO: combine symbolically associated annotations into a single line
	 */
	@Override
	public void encodeAndSave(Transcription theTranscription,
			EncoderInfo theEncoderInfo, List<TierImpl> tierOrder, String path)
			throws IOException {
        // if the transcription or tier vector or path is null throw exception
        if (theTranscription == null) {
            ServerLogger.LOG.warning("The transcription is null");
            throw new IllegalArgumentException("The transcription is null.");
        }

        if ((tierOrder == null) || (tierOrder.size() == 0)) {
        	ServerLogger.LOG.warning("No tiers have been specified for export");
            throw new IllegalArgumentException("No tiers specified for export");
        }

        if (path == null) {
        	ServerLogger.LOG.warning("No file path for the TextGrid file has been specified");
            throw new IllegalArgumentException(
                "No file path for the TextGrid file has been specified");
        }
        
        ThemeEncoderInfo encoderInfo = null;
        if (theEncoderInfo instanceof ThemeEncoderInfo) {
        	encoderInfo = (ThemeEncoderInfo) theEncoderInfo;
        } else {
        	// create default values
        	encoderInfo = new ThemeEncoderInfo();
        }
        //create a list of time point - value combinations. The value is comma separated string of 
        // participant or tier name, begin or end marker, and annotation value 
        // could collapse symbolically associated annotations into one line
        List<ThemeLine> lineList = new ArrayList<ThemeLine>();
        LinkedHashMap<String, List<String>> vvtMap = new LinkedHashMap<String, List<String>>();
        vvtMap.put("A", new ArrayList<String>());
        List<String> be = new ArrayList<String>(2);
        be.add("B");
        be.add("E");
        vvtMap.put("b_e", be);
        
        for (TierImpl ti : tierOrder) {
        	String tierName = ti.getName();// or take the participant?
        	String partName = ti.getParticipant();
    		List<String> tValues = new ArrayList<String>();
    		
        	List<? extends Annotation> annos = ti.getAnnotations();
        	
        	if (encoderInfo.isTierNameAsActor() || (partName == null || partName.isEmpty())) {
        		// add tier as actor
        		vvtMap.get("A").add(tierName);
        		// add tier is top level root of the coding values
        		// could use the controlled vocabulary if there is any
        		vvtMap.put(tierName, tValues);
        		
            	for (int i = 0; i < annos.size(); i++) {
            		Annotation ann = annos.get(i);

            		lineList.add(new ThemeLine(ann.getBeginTimeBoundary(), B, tierName, ann.getValue()));
            		lineList.add(new ThemeLine(ann.getEndTimeBoundary(), E, tierName, ann.getValue())); 			
          		
            		if (!tValues.contains(ann.getValue())) {
            			tValues.add(ann.getValue());
            		}
            	}
        	} else {
    			if (!vvtMap.get("A").contains(partName)) {
    				vvtMap.get("A").add(partName);
    			}
    			if (!vvtMap.containsKey(partName)) {
    				vvtMap.put(partName, tValues);
    			}
    			
            	for (int i = 0; i < annos.size(); i++) {
            		Annotation ann = annos.get(i);
            		String codeValues = tierName + COMMA + ann.getValue();
            		// participant name as actor
            		lineList.add(new ThemeLine(ann.getBeginTimeBoundary(), B, partName, codeValues));
            		lineList.add(new ThemeLine(ann.getEndTimeBoundary(), E, partName, codeValues));
            		
            		// add annotation value to hierarchy tree
            		// or should this be a combination of tier name and annotation value
            		if (!tValues.contains(ann.getValue())) {
            			tValues.add(ann.getValue());
            		}
            	}
        	}
      	
        }

        if (lineList.size() == 0) {
        	ServerLogger.LOG.warning("There are no annotations to export");
        	throw new IOException("There are no annotations to export.");
        }
        
        Collections.sort(lineList);
        
        @SuppressWarnings("resource")
		OutputStreamWriter out = null;
        BufferedWriter writer = null;

        try {
        	if (encoderInfo.getEncoding() != null) {
                out = new OutputStreamWriter(new FileOutputStream(
                        path), encoderInfo.getEncoding());
        	} else {
                out = new OutputStreamWriter(new FileOutputStream(
                        path), "UTF-8");
        	}
        	
            writer = new BufferedWriter(out);
            writer.write("Time");
            writer.write(TAB);
            writer.write("Event");
            writer.write(NEW_LINE);
            // write mandatory intro line
            ThemeLine tl = lineList.get(0);
            writer.write(String.valueOf(tl.time));
            writer.write(TAB);
            writer.write(":");
            writer.write(NEW_LINE);
            
            // line loop
            for (ThemeLine thl : lineList) {
            	writer.write(String.valueOf(thl.time));
            	writer.write(TAB);
            	writer.write(thl.partName);
            	writer.write(COMMA);
            	writer.write(thl.beginOrEnd);
            	writer.write(COMMA);
            	writer.write(thl.label);
            	writer.write(NEW_LINE);
            }
            
            tl = lineList.get(lineList.size() - 1);
            writer.write(String.valueOf(tl.time));
            writer.write(TAB);
            writer.write("&");
            writer.write(NEW_LINE);
            
            writer.flush();
        } catch (UnsupportedEncodingException uee) {
        	ServerLogger.LOG.warning("Encoding not supported: " + uee.getMessage());
        } finally {
        	try {
        		if (writer != null) {
        			writer.close();
        		}
        	} catch (Throwable t) {
        		ServerLogger.LOG.warning("Could not close the file writer: " + t.getMessage());
        	}
        	try {
        		if (out != null) {
        			out.close();
        		}
        	} catch (Throwable t) {
        		ServerLogger.LOG.warning("Could not close the file: " + t.getMessage());
        	}
        }
        
        // hier... write the vvt.vvt file
        int lastSep = path.lastIndexOf(File.separatorChar);
        String vvtPath = null;
        if (lastSep > 0) {
        	vvtPath = path.substring(0, lastSep) + File.separator + "vvt.vvt";
        } else {
        	// try '/' in case file separator is anything else
        	if (File.separatorChar != '/') {
        		lastSep = path.lastIndexOf('/');
        		if (lastSep > 0) {
        			vvtPath = path.substring(0, lastSep) + "/vvt.vvt";
        		}
        	}
        }
        
        if (vvtPath != null) {
            @SuppressWarnings("resource")
			OutputStreamWriter vvtOut = null;
            BufferedWriter vvtWriter = null;
            try {
            	if (encoderInfo.getEncoding() != null) {
            		vvtOut = new OutputStreamWriter(new FileOutputStream(
            				vvtPath), encoderInfo.getEncoding());
            	} else {
            		vvtOut = new OutputStreamWriter(new FileOutputStream(
            				vvtPath), "UTF-8");
            	}
            	// loop the values map
            	vvtWriter = new BufferedWriter(vvtOut);
            	
            	Iterator<String> keyIter = vvtMap.keySet().iterator();
            	String key;
            	
            	while (keyIter.hasNext()) {
            		key = keyIter.next();
            		List<String> tValues = vvtMap.get(key);
            		if (tValues.size() > 0) {
            			vvtWriter.write(key);
            			vvtWriter.write(NEW_LINE);
            			for (String s : tValues) {
            				vvtWriter.write(TAB);
            				vvtWriter.write(s);
            				vvtWriter.write(NEW_LINE);
            			}
            		}
            		vvtWriter.flush();
            	}
            	
            } catch (UnsupportedEncodingException uee) {
            	ServerLogger.LOG.warning("Encoding not supported: " + uee.getMessage());
            } finally {
            	try {
            		if (vvtWriter != null) {
            			vvtWriter.close();
            		}
            	} catch (Throwable t) {
            		ServerLogger.LOG.warning("Could not close the vvt file writer: " + t.getMessage());
            	}
            	try {
            		if (vvtOut != null) {
            			vvtOut.close();
            		}
            	} catch (Throwable t) {
            		ServerLogger.LOG.warning("Could not close the vvt file: " + t.getMessage());
            	}
            }
        } else {
        	ServerLogger.LOG.warning("Could not write the vvt file: unable to construct a file path.");
        }
	}

}
