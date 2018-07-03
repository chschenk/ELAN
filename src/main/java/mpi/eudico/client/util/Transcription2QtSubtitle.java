package mpi.eudico.client.util;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeRelation;
import mpi.eudico.util.TimeFormatter;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;


/**
 * Exports the annotations of a selection of tiers as a QuickTime (subtitle)
 * text file.   Created on Jul 2, 2004
 *
 * @author Alexander Klassmann
 * @version Dec 2007 support for an offset, a minimal duration per entry and merging of tiers added
 * @version June 2015 added support for export to TeXML - tx3g format in a least effort way and in 
 * such a way that as little as possible is changed to existing code 
 */
public class Transcription2QtSubtitle {
    /** new line character */
    final static private String NEWLINE = "\n";
    final static private char[] bracks = new char[]{'[', ']', '(', ')'}; 
    final static private char NL_CHAR = '\n'; 

    /**
     * Exports all annotations on specified tiers
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile) throws IOException {
        exportTiers(transcription, tierNames, exportFile, 0L, Long.MAX_VALUE);
    }

    /**
     * Exports all annotations on specified tiers, applying the specified
     * minimal duration.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param minimalDuration the minimal duration for each annotation/subtitle
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, int minimalDuration)
        throws IOException {
        exportTiers(transcription, tierNames, exportFile, 0L, Long.MAX_VALUE);
    }

    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime)
        throws IOException {
        exportTiers(transcription, tierNames, exportFile, beginTime, endTime,
            0L, 0, 0L, false, null);
    }
    
    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified,  applying the specified offset and minimal duration. For
     * each tier a separate text file is created.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration)
        throws IOException {
    	exportTiers(transcription, tierNames, exportFile, beginTime, endTime,
                offset, minimalDuration, mediaDuration , false, null);
    }

    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified, applying the specified offset and minimal duration. For
     * each tier a separate text file is created.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     * @param the font and color setting for the subtitles
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration, boolean reCalculateTime, Map<String, Object> newSubtitleSetting)
        throws IOException {
        if (exportFile == null) {
            return;
        }

        if (exportFile.getName().toLowerCase().endsWith(".xml")) {// simple file name test to check if tx3g has to be exported
        	exportTiersTx3g(transcription, tierNames, exportFile, beginTime, endTime, 
        			offset, minimalDuration, mediaDuration, reCalculateTime, toTx3gStyleMap(newSubtitleSetting));
        	return;
        }
        
        Annotation[] annotations = null;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        if (tierNames.length == 1) {
            out = new FileOutputStream(exportFile);
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }        
        
        long recalculateTimeInterval = 0L;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime; 
        	offset = 0L;
        	
        	for (int j = 0; j < tierNames.length; j++) {        	
                
                Tier tier = (Tier) transcription.getTierWithId(tierNames[j]);

                annotations = (Annotation[]) tier.getAnnotations()
                                                 .toArray(new Annotation[0]);
                for (int i = 0; i < annotations.length; i++) {
                	if (annotations[i] != null) {
                		if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) { 
                    	                    	                    	                    	
                        long b = annotations[i].getBeginTimeBoundary(); 
                        
                        if (b < recalculateTimeInterval){
                        		recalculateTimeInterval = b;
                        		break;
                        	}                        	
                        }
                	}
                }
            }
        	annotations = null;
        }
        
        for (int j = 0; j < tierNames.length; j++) {
            if (tierNames.length > 1) {
                String nextName = exportFile.getAbsolutePath();
                int index = nextName.lastIndexOf('.');

                if (index > 0) {
                    nextName = nextName.substring(0, index) + "_" +
                        tierNames[j] + ".txt";
                } else {
                    nextName = nextName + "_" + tierNames[j];
                }

                out = new FileOutputStream(new File(nextName));
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }

            Tier tier = (Tier) transcription.getTierWithId(tierNames[j]);

            annotations = (Annotation[]) tier.getAnnotations()
                                             .toArray(new Annotation[0]);
            
            writer.write(getSettings(newSubtitleSetting));            

            long b;
            long e;
            long d;
            long nextB = 0L;
            long lastE = 0L;            
            
            for (int i = 0; i < annotations.length; i++) {
                if (annotations[i] != null) {
                    if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) { 
                    	                    	                    	                    	
                        b = annotations[i].getBeginTimeBoundary();
                        d = b + minimalDuration;
                        e = Math.max(annotations[i].getEndTimeBoundary(), d);  
                        
                        if (i < (annotations.length - 1)) {
                            nextB = annotations[i + 1].getBeginTimeBoundary();
                            e = Math.min(e, nextB);
                        }

                        if (lastE < e) {
                            lastE = e;
                        }

                        writer.write("[" + TimeFormatter.toString(b -recalculateTimeInterval + offset) +
                            "]" + NEWLINE);
                        writer.write("{textEncoding:256}");
                        writer.write(replaceBrackets(annotations[i].getValue()));
                        writer.append(NL_CHAR);
                        if (nextB - e < 10 && nextB - b >= 20) {// min 10 ms in between
                            writer.write("[" + TimeFormatter.toString(nextB-recalculateTimeInterval - 10 + offset) +
                                    "]" + NEWLINE);
                        } else {
                        	writer.write("[" + TimeFormatter.toString(e -recalculateTimeInterval+ offset) +
                            "]" + NEWLINE);
                        }
                    }
                }
            }

            if (mediaDuration > lastE + 20) {
            	// what about the offset?
                writer.write("[" +
                    TimeFormatter.toString(mediaDuration -
                        Math.min(40, (mediaDuration - lastE + 10))) + "]" + NEWLINE);
                writer.write("[" + TimeFormatter.toString(mediaDuration) + "]");
            }

            if (tierNames.length > 1) {
                writer.close();
            }
        }

        writer.close();
    }

    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     *
     * @throws IOException any io exception
     */    
    static public void exportTiersMerged(Transcription transcription,
            String[] tierNames, File exportFile, long beginTime, long endTime,
            long offset, int minimalDuration, long mediaDuration) throws IOException{
    	exportTiersMerged(transcription, tierNames, exportFile, beginTime,
                endTime, offset, minimalDuration, mediaDuration ,false,  null);    	
    }
    
    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     * @param newSubtitleSetting the font and color setting for the subtitles
     *
     * @throws IOException any io exception
     */
    static public void exportTiersMerged(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration, boolean reCalculateTime, Map<String, Object> newSubtitleSetting)
        throws IOException {
        if (exportFile == null) {
            return;
        }
        // for now, if xml is the output format, ignore the merge option
        if (exportFile.getName().toLowerCase().endsWith(".xml")) {// simple file name test to check if tx3g has to be exported
        	exportTiersTx3g(transcription, tierNames, exportFile, beginTime, endTime, 
        			offset, minimalDuration, mediaDuration, reCalculateTime, toTx3gStyleMap(newSubtitleSetting));
        	return;
        }
        
        long recalculateTimeInterval = 0L;
        int selection =0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;  
        	offset = 0L;
        	
        }  
        
       String fileName = exportFile.getAbsolutePath();
        int index = fileName.lastIndexOf('.');
        
        if (index > 0) {
        	fileName = fileName.substring(0, index) +".txt";
        }
        
        FileOutputStream out = new FileOutputStream(new File(fileName));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    "UTF-8"));
        writer.write(getSettings(newSubtitleSetting)); 
        
        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit = null;
        SubtitleUnit nextUnit = null;
        //Long d = 0L;

        for (int i = 0; i < allUnits.size(); i++) {
            unit = allUnits.get(i);
            if(selection ==0){
            	if (unit.getBegin() < recalculateTimeInterval){
            		recalculateTimeInterval = unit.getBegin();
            		selection =1;
            	}            	
            }            
            writer.write("[" + TimeFormatter.toString(unit.getBegin() - recalculateTimeInterval) + "]" +
            		NEWLINE);            	
            writer.write("{textEncoding:256}");

            for (int j = 0; j < unit.getValues().length; j++) {
            	//writer.write(unit.getValues()[j].replace('\n', ' '));
            	writer.write(replaceBrackets(unit.getValues()[j]));
            	writer.append(NL_CHAR);
            }
            
            if (i < allUnits.size() - 1) {
            	nextUnit = allUnits.get(i + 1);
            	if (nextUnit.getBegin() - unit.getCalcEnd() < 10 && 
            			nextUnit.getBegin() - unit.getBegin() >= 20) {// adjust end time: min 10 ms in between
            		writer.write("[" + TimeFormatter.toString(nextUnit.getBegin() - 10 - recalculateTimeInterval ) + "]" +
                            NEWLINE);
            	} else {
            		writer.write("[" + TimeFormatter.toString(unit.getCalcEnd() - recalculateTimeInterval ) + "]" +
                            NEWLINE);
            	}
            } else {
            	writer.write("[" + TimeFormatter.toString(unit.getCalcEnd() -recalculateTimeInterval ) + "]" +
                        NEWLINE);
            }
            
        }

        if ((unit != null) && (mediaDuration > unit.getCalcEnd() + 20)) { // unit is the last unit
            writer.write("[" +
                TimeFormatter.toString(mediaDuration -
                    Math.min(40, (mediaDuration - unit.getCalcEnd() - 10 ))) + "]" +
                NEWLINE);
            writer.write("[" + TimeFormatter.toString(mediaDuration) + "]");
        }

        writer.close();
    }

    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param minimalDuration the minimal duration for each annotation/subtitle
     *
     * @throws IOException any io exception
     */
    static public void exportTiersMerged(Transcription transcription,
        String[] tierNames, File exportFile, int minimalDuration)
        throws IOException {
        exportTiersMerged(transcription, tierNames, exportFile, 0L,
            Long.MAX_VALUE, 0L, minimalDuration, 0L, false, null );
    }
    
    /**
     * Replaces square brackets by parentheses because square brackets have 
     * a special meaning in the QT text format.
     *  
     * @param value the string value
     * @return a char array without square brackets
     */
    private static char[] replaceBrackets(String value) {
    	if (value == null || value.length() == 0) {
    		return new char[]{};
    	}
    	char[] ch = value.toCharArray();
    	
    	for (int i = 0; i < ch.length; i++) {
    		if (ch[i] == bracks[0]) {
    			ch[i] = bracks[2];
    		} else if (ch[i] == bracks[1]) {
    			ch[i] = bracks[3];
    		}
    	}
    	
    	return ch;
    }
    
    /**
     * Creates a string with  the necessary settings required for the subtitles
     *  
     * @param newSubtitleSetting the Map which has the subtitle settings
     * @return a char array without square brackets
     */
   
    
    private static String getSettings(Map<String, Object> newSubtitleSetting) {
    	if(newSubtitleSetting != null){
    		StringBuilder setting = new StringBuilder("{QTtext}{timescale:100}");
        	
        	if (newSubtitleSetting.get("font") != null){
        		setting.append("{font:");
        		setting.append( newSubtitleSetting.get("font").toString());
        		setting.append("}");
        	}else {
        		setting.append("{font:Arial Unicode MS}");
        	}
        	
        	setting.append("{plain}");
        	
        	if(newSubtitleSetting.get("size") != null){
        		setting.append("{size:");
        		setting.append(newSubtitleSetting.get("size").toString());
        		setting.append("}");
        	}else{
        		setting.append("{size:12}");
        	}
        	
        	Color newColor = (Color)newSubtitleSetting.get("backColor");
        	int mc = 65535/255; // color value multiplication
        	if (newColor != null ) {	
        		setting.append("{backColor:");
        		setting.append(newColor.getRed() * mc);
        		setting.append(",");
        		setting.append(newColor.getGreen() * mc);
        		setting.append(",");
        		setting.append(newColor.getBlue() * mc);
        		setting.append("}");
        	}
        	else{
        		setting.append("{backColor:0,0,0}");
        	}
        	
        	newColor = (Color) newSubtitleSetting.get("textColor");
        	if ( newColor != null ){
        		setting.append("{textColor:");
        		setting.append(newColor.getRed() * mc);
        		setting.append(",");
        		setting.append(newColor.getGreen() * mc);
        		setting.append(",");
        		setting.append(newColor.getBlue() * mc);
        		setting.append("}");
        	}else{
        		setting.append("{textColor:65535,65535,65535}");
        	}
        	
        	if(newSubtitleSetting.get("transparent") != null){
        		if((Boolean)newSubtitleSetting.get("transparent")){
        			setting.append("{keyedText:on}");
        		} else{
        			setting.append("{keyedText:off}");
        		}
        	}   
        	
        	if (newSubtitleSetting.get("width") != null) {
        		setting.append("{width:");
        		setting.append(newSubtitleSetting.get("width").toString());
        		setting.append("}");
        	} else {
        		setting.append("{width:320}");
        	}
        	
        	if (newSubtitleSetting.get("height") != null) {
        		setting.append("{height:");
        		setting.append(newSubtitleSetting.get("height").toString());
        		setting.append("}");
        	} else {
        		setting.append("{height:0}");
        	}
        	
        	if(newSubtitleSetting.get("justify") != null){
        		setting.append("{justify:");
        		setting.append(newSubtitleSetting.get("justify").toString());
        		setting.append("}");
        	}else{
        		setting.append("{justify:left}");
        	}
        	setting.append(NEWLINE);
        	
        	return setting.toString();
        } else {
        	return "{QTtext}{timescale:100}{font:Arial Unicode MS}{plain}{size:12}{backColor:0,0,0}{textColor:65535,65535,65535}{width:320}{height:0}{justify:left}" + NEWLINE;
        }
    }
    
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // TeXML output of/for Apple's tx3g tracks. Can be opened in some versions of QuickTime Player and added to video as subtitles.
    // This option has been added the quick-and-dirty way, it's probably too obscure to add it as an export in a proper way.
	/*
    <sample duration="988" keyframe="true">
	<description horizontalJustification="Center" verticalJustification="Top" backgroundColor="0%, 0%, 0%, 100%" format="tx3g">
		<defaultTextBox width="960" height="50"></defaultTextBox>
		<fontTable>
			<font id="1" name="Geneva"></font>
		</fontTable>
		<sharedStyles>
			<style id="1">{font-table: 1}{font-size: 28}{font-style: normal}{font-weight: normal}{text-decoration: normal}{color: 73%, 99%, 82%, 100%}</style>
		</sharedStyles>
	</description>
	<sampleData targetEncoding="utf8">
		<text styleID="1"> </text>
	</sampleData>
	</sample>
	*/
    /**
     * Creates an XML Element for a single sample (see the format above). 
     * @param doc the document
     * @param unit a subtitle unit, time information and text
     * @param styleSettings a map containing choice for the style, alignment, position etc of the subtitles
     * @return a sample element
     */
    private static Element getSampleElement(Document doc, SubtitleUnit unit, Map<String, String> styleSettings) {
    	// sample
    	Element sampleEl = doc.createElement("sample");
    	sampleEl.setAttribute("duration", String.valueOf(unit.getCalcEnd() - unit.getBegin()));//required
    	sampleEl.setAttribute("keyframe", "true");//optional
    	//doc.appendChild(sampleEl);
    	// description
    	Element descriptionEl = doc.createElement("description");//required
    	sampleEl.appendChild(descriptionEl);
    	// make sure the settings map contains everything needed
    	descriptionEl.setAttribute("horizontalJustification", styleSettings.get("horizontalJustification"));//required
    	descriptionEl.setAttribute("verticalJustification", styleSettings.get("verticalJustification"));//required
    	descriptionEl.setAttribute("backgroundColor", styleSettings.get("backgroundColor"));// optional
    	descriptionEl.setAttribute("format", "tx3g");//required
    	// optional attribute: displayFlags
    	// defaultTextBox
    	Element textBoxEl = doc.createElement("defaultTextBox");//required, the attributes x, y, width and height are optional and default to 0
    	descriptionEl.appendChild(textBoxEl);
    	textBoxEl.setAttribute("width", styleSettings.get("width"));
    	textBoxEl.setAttribute("height", styleSettings.get("height"));
    	// fontTable optional?
    	Element fonttableEl = doc.createElement("fontTable");//required
    	descriptionEl.appendChild(fonttableEl);
    	Element fontEl = doc.createElement("font");
    	fonttableEl.appendChild(fontEl);
    	fontEl.setAttribute("id", "1");//required. There is support for only 1 font per tier in the export
    	fontEl.setAttribute("name", styleSettings.get("font"));//required, can be a comma separated list of fonts or styles (e.g. Serif, Sans-Serif)
    	// sharedStyles
    	Element sharedEl = doc.createElement("sharedStyles");//required
    	descriptionEl.appendChild(sharedEl);
    	Element styleEl = doc.createElement("style");// minimal 1 style required, the content is modified CSS string
    	sharedEl.appendChild(styleEl);
    	styleEl.setAttribute("id", "1");//required
    	styleEl.appendChild(doc.createTextNode(getStyleString("1", styleSettings)));
    	// sampleData
    	Element dataEl = doc.createElement("sampleData");//required
    	sampleEl.appendChild(dataEl);
    	dataEl.setAttribute("targetEncoding", "utf8");// required? Either utf8 or utf16
    	Element textEl = doc.createElement("text");
    	dataEl.appendChild(textEl);
    	textEl.setAttribute("styleID", "1");//required, one style element to reference
    	if (unit.getValues().length == 1) {
    		textEl.appendChild(doc.createTextNode(unit.getValues()[0]));
    	} else if (unit.getValues().length > 1){
    		// concatenate values?
    		StringBuilder sb = new StringBuilder(unit.getValues()[0]);
    		for (int i = 1; i < unit.getValues().length; i++) {
    			sb.append(" ");//?? or \n?
    			sb.append(unit.getValues()[i]);
    		}
    		textEl.appendChild(doc.createTextNode(sb.toString()));//whitespace, line breaks and indentation are preserved in the text
    	}
    	
    	return sampleEl;
    }
    
    /**
     * Returns a css style string with values filled in for font id, font size and font color.
     * 
     * @param id the the id reference to a font in the font table 
     * @param styleSettings the map containing style settings
     * @return a formatted style string
     */
    private static String getStyleString(String id, Map<String, String> styleSettings) {
    	// style = normal or italic
    	// weight = normal or bold
    	// decoration = normal or underline
    	return String.format("{font-table: %s}{font-size: %s}{font-style: normal}{font-weight: normal}{text-decoration: normal}{color: %s}", 
    			id, // font id
    			styleSettings.get("fontSize"), // font size
    			styleSettings.get("foregroundColor"));// pre-formatted text color
    }
    
    /**
     * Converts a color to a TeXML color string (a Cascading Style Sheet (CSS) style color 
     * specification with four component percentages (RGBA)).
     * 255 corresponds to 100%.
     * 
     * @param c the color to convert
     * @return a string with percentages for red, green, blue and alpha
     */
    private static String colorToPercentString(Color c) {
    	float max = 255f;
    	int r = (int) ((c.getRed() / max) * 100);
    	int g = (int) ((c.getGreen() / max) * 100);
    	int b = (int) ((c.getBlue() / max) * 100);
    	int a = (int) ((c.getAlpha() / max) * 100);
    	return String.format("%d%5$s, %d%5$s, %d%5$s, %d%5$s", r, g, b, a, "%");// maybe do other type of rounding
    }
    
    /**
     * Creates a map containing string values, ensuring that expected key-value pairs are there
     * 
     * @param settingsMap the string-object mapping of the export dialog
     * @return a string-string map for the TeXML export 
     */
    private static Map<String, String> toTx3gStyleMap(Map<String, Object> settingsMap) {
    	Map<String, String> tx3gMap = new HashMap<String, String>();
    	if (settingsMap == null) {
    		settingsMap = new HashMap<String, Object>(0);
    	}
    	
    	String horJust = (String) settingsMap.get("justify");
    	if (horJust != null) {// left, center, right
    		tx3gMap.put("horizontalJustification", horJust.replaceFirst(horJust.substring(0, 1), String.valueOf(Character.toUpperCase(horJust.charAt(0)))));
    	} else {
    		tx3gMap.put("horizontalJustification", "Center");
    	}
    	tx3gMap.put("verticalJustification", "Bottom");
    	tx3gMap.put("backgroundColor", "0%, 0%, 0% 0%"); // set default background: transparent
    	tx3gMap.put("foregroundColor", "100%, 100%, 100%, 100%"); // set default foreground: white
    	
    	Boolean transparent = (Boolean) settingsMap.get("transparent");
    	Color bc = (Color) settingsMap.get("backColor");
    	if (bc == null) {
			if (transparent != null && !transparent) {
				tx3gMap.put("backgroundColor", "0%, 0%, 0% 100%");// non transparent black
			}
    	} else {
    		if (transparent == null || !transparent) {
    			tx3gMap.put("backgroundColor", colorToPercentString(bc));
    		}
    	}
    	
    	Color fc = (Color) settingsMap.get("textColor");
    	if (fc == null) {
    		tx3gMap.put("foregroundColor", "100%, 100%, 100%, 100%");// opaque white
    	} else {
    		tx3gMap.put("foregroundColor", colorToPercentString(fc));
    	}
    	Integer width = (Integer) settingsMap.get("width");
    	if (width == null) {
    		tx3gMap.put("width", "320");// default width:??
    	} else {
    		tx3gMap.put("width", String.valueOf(width));
    	}
    	Integer height = (Integer) settingsMap.get("height");
    	if (height == null) {
    		tx3gMap.put("height", "50");// default height: 50
    	} else {
    		tx3gMap.put("height", String.valueOf(height));
    	}	
    	Integer vidHeight = (Integer) settingsMap.get("videoHeight");
    	if (vidHeight == null) {
    		tx3gMap.put("videoHeight", "240");// default height: ??
    	} else {
    		tx3gMap.put("videoHeight", String.valueOf(vidHeight));
    	}

    	String fontName = (String) settingsMap.get("font");
    	if (fontName != null) {
    		tx3gMap.put("font", fontName);
    	} else {
    		tx3gMap.put("font", "Arial Unicode MS");
    	}
    	Integer fontSize = (Integer) settingsMap.get("size");
    	if (fontSize != null) {
    		tx3gMap.put("fontSize", String.valueOf(fontSize));
    	} else {
    		tx3gMap.put("fontSize", "24");// default font size
    	}
    	return tx3gMap;
    }
    
    /**
     * Export in tx3g xml format (TeXML), one xml file per tier.
     * 
     * @param transcription the transcription
     * @param tierNames the tiers to export
     * @param exportFile the (base) file to export to, if multiple files are exported the file names 
     * are derived from this file's name
     * @param beginTime export from here if only a selection needs to be exported
     * @param endTime export till here if only a selection needs to be exported
     * @param offset the offset of the media file
     * @param minimalDuration the minimal duration of a subtitle
     * @param mediaDuration the total duration of the video
     * @param reCalculateTime a flag to specify whether the annotation times need to be adjusted 
     * based on the selection that has to be exported
     * @param newSubtitleSetting a map containing some export settings
     */
    private static void exportTiersTx3g(Transcription transcription,
            String[] tierNames, File exportFile, long beginTime, long endTime,
            long offset, int minimalDuration, long mediaDuration, 
            boolean reCalculateTime, Map<String, String> newSubtitleSetting) throws IOException {
    	// copied from exportTiers(...), temporarily
    	Annotation[] annotations = null; 
        long recalculateTimeInterval = 0L;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime; 
        	offset = 0L;
        	
        	for (int j = 0; j < tierNames.length; j++) {        	
                
                Tier tier = (Tier) transcription.getTierWithId(tierNames[j]);

                annotations = (Annotation[]) tier.getAnnotations()
                                                 .toArray(new Annotation[0]);
                for (int i = 0; i < annotations.length; i++) {
                	if (annotations[i] != null) {
                		if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) { 
                    	                    	                    	                    	
                        long b = annotations[i].getBeginTimeBoundary(); 
                        
                        if (b < recalculateTimeInterval){
                        		recalculateTimeInterval = b;
                        		break;
                        	}                        	
                        }
                	}
                }
            }
        	annotations = null;
        }
        // end copied
        Map<String, String> tierToFileNameMap = new HashMap<String, String>(tierNames.length);
        if (tierNames.length == 1) {// don't modify the output file name
        	tierToFileNameMap.put(tierNames[0], exportFile.getAbsolutePath());
        } else {
	        int dotIndex = exportFile.getAbsolutePath().lastIndexOf('.');
	        for (String tn : tierNames) {
	            String nextName = exportFile.getAbsolutePath();
	
	            if (dotIndex > 0) {
	                nextName = nextName.substring(0, dotIndex) + "_" +
	                    tn.replaceAll("[:;/|!?\\\\]", "_") + /*".xml"*/ nextName.substring(dotIndex);
	            } else {
	                nextName = nextName + "_" + tn.replaceAll("[:;/|!?\\\\]", "_");
	            }
	            
	            tierToFileNameMap.put(tn, nextName);
	        }
        }
        // end output names
        // loop over tier names
        int failedExports = 0;
        
        for (String tierName : tierNames) {
        	TierImpl t = (TierImpl) transcription.getTierWithId(tierName);
        	String language = "eng";
        	if (t.getLangRef() != null) {
        		language = t.getLangRef();
        	}
        	List<AbstractAnnotation> annots = t.getAnnotations();
        	List<SubtitleUnit> subUnits = new ArrayList<SubtitleUnit>();
        	
        	long b;
        	long e;
        	long d;
        	//long nextB = 0L;
        	long lastE = 0L;            
		 
        	for (AbstractAnnotation aa : annots) {
        		if (aa != null) {
        			if (TimeRelation.overlaps(aa, beginTime, endTime)) {
		         	                    	                    	                    	
        				b = aa.getBeginTimeBoundary();
        				d = b + minimalDuration;
        				e = Math.max(aa.getEndTimeBoundary(), d);  
		
        				if (lastE < e) {
        					lastE = e;
        				}
        				
        				subUnits.add(new SubtitleUnit(b- recalculateTimeInterval + offset, 
        						e - recalculateTimeInterval+ offset, aa.getValue()));
        			} else if (aa.getBeginTimeBoundary() > endTime) {
        				break;
        			}
        		}
        	}
        	
        	// add empty units to fill gaps
        	SubtitleUnit suCur = null;
        	SubtitleUnit suPrev = null;
        	
        	for (int j = subUnits.size() - 1; j >= 0; j--) {
        		suCur = subUnits.get(j);
        		
        		if (j > 0) {
        			// special case for the last unit
        			if (j == subUnits.size() - 1) {
        				if (suCur.getRealEnd() < mediaDuration) {
        					// offset?
        					subUnits.add(new SubtitleUnit(suCur.getRealEnd(), mediaDuration, ""));
        				}
        			}
        			
        			suPrev = subUnits.get(j - 1);
        			if (suCur.getBegin() > suPrev.getRealEnd()) {
        				// a gap, fill (could check size of gap
        				subUnits.add(j, new SubtitleUnit(suPrev.getRealEnd(), suCur.getBegin(), ""));
        				// j--;?
        			}
        		} else {
        			// check if a unit has to be added before the first
        			if (suCur.getBegin() > 0) {
        				subUnits.add(0, new SubtitleUnit(0, suCur.getBegin(), ""));
        			}
        		}
        	}
        	
        	// now create a Document and write it
            Document doc = getT3GTrackDocument();
            Element root = doc.createElement("text3GTrack");
            root.setAttribute("trackWidth", newSubtitleSetting.get("width"));
            root.setAttribute("trackHeight", newSubtitleSetting.get("height"));
            /* not used currently
            int textHeight = 50;
            int vidHeight =  240;
            try {
            	textHeight = Integer.parseInt(newSubtitleSetting.get("height"));
            	vidHeight = Integer.parseInt(newSubtitleSetting.get("videoHeight"));
            } catch (NumberFormatException nfe) {
            	ClientLogger.LOG.warning("Not a valid number: " + nfe.getMessage());
            }
            */
            // root.setAttribute("transform", String.format("translate(0.0,%.1f)", (float)(vidHeight - textHeight)));// translation of video height - text height puts it the top
            root.setAttribute("transform", "translate(0.0,0.0)");// y translation of 0.0 puts the track at the bottom 
            root.setAttribute("timeScale", "1000");
            root.setAttribute("language", language);
            doc.appendChild(root);
            
            for (SubtitleUnit su : subUnits) {
            	Element sampleEl = getSampleElement(doc, su, newSubtitleSetting);
            	if (sampleEl != null) {
            		root.appendChild(sampleEl);
            	}
            }
            // write the file
            try {
	    		FileOutputStream outputstream = new FileOutputStream(tierToFileNameMap.get(tierName));
	
	    		DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
	    		LSSerializer serializer = domImplLS.createLSSerializer();
	    		serializer.getDomConfig().setParameter("format-pretty-print", true);
	    		LSOutput destination = domImplLS.createLSOutput();
	    		destination.setEncoding("utf-8");
	    		destination.setByteStream(outputstream);
	
	    		serializer.write(doc, destination);
	    		outputstream.close();
            } catch (FileNotFoundException fnfe) {
            	ClientLogger.LOG.severe("Cannot write to file: " + fnfe.getMessage());
            	failedExports++;
            } catch (IOException ioe) {
            	ClientLogger.LOG.severe("Cannot export the XML: " + ioe.getMessage());
            	failedExports++;
            }
        }
        
        if (failedExports > 0) {
        	throw new IOException(String.format("Some tiers could not be exported: %d", failedExports));
        }
    }
    
    /**
     * Creates a Document instance and add the required ProcessingInstruction 'type="application/x-quicktime-texml"'
     *  
     * @return a TeXML document instance
     */
    private static Document getT3GTrackDocument() {
        try {
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	DocumentBuilder db = dbf.newDocumentBuilder();
            if (db != null) {
            	Document doc = db.newDocument();
            	ProcessingInstruction piNode = doc.createProcessingInstruction("quicktime", "type=\"application/x-quicktime-texml\"");
            	if (piNode != null) {
            		doc.appendChild(piNode);
            	}
            	return doc;
            }
        } catch (FactoryConfigurationError fce) {
        	ClientLogger.LOG.severe("Unable to create an XML Document Builder: " +
                fce.getMessage());
        } catch (ParserConfigurationException pce) {
        	ClientLogger.LOG.severe("Unable to create an XML Document Builder: " +
                pce.getMessage());
        }
        
    	return null;
    }
}
