package mpi.eudico.client.util;

import java.awt.Color;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeRelation;

/**
 * This class extracts annotations out of an eaf-file puts them into the SMIL-format.
 * The output consists of at least two files for Real Player
 * One with ending .smil that contains layout information for the media player and a reference to the media file.
 * One or more ending with .rt which contain(s) the annotations of the corresponding tier(s) as subtitles.
 *    
 * 
 * Created on Apr 15, 2004
 *
 * @author Alexander Klassmann
 * @version Apr 15, 2004
 */
public class EAF2SMIL {
    private static Transformer transformer2smil;
    private static Transformer transformer2rt;   

    /**
     * export without reference to media file
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(File eafFile, File smilFile,
        String[] tierNames, long offset, int  minimalDur, Map<String, Object> fontSettingHashMap) throws IOException, TransformerException {
        export2SMIL(eafFile, smilFile, tierNames, null, offset, minimalDur, fontSettingHashMap);
    }

    /**
     * export with reference to media file
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param mediaURL
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(File eafFile, File smilFile,
        String[] tierNames, String mediaURL, long offset, int  minimalDur, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        try {
            URL eafURL = new URL("file:///" + eafFile.getAbsolutePath());
            export2SMIL(eafURL, smilFile, tierNames, mediaURL, offset, minimalDur, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * same as above, only URL instead of File
     *
     * @param eafURL
     * @param smilFile
     * @param tierNames
     * @param mediaURL reference to media file
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(URL eafURL, File smilFile,
        String[] tierNames, String mediaURL, long offset, int  minimalDur, Map<String, Object> fontSettingMap)
        throws IOException, TransformerException {
        
    	createTransformer();

        //Parameter setting
        String fileName = new File(eafURL.getFile()).getName();

        int index = fileName.lastIndexOf('.');
        String title = (index > 0) ? (fileName = fileName.substring(0, index))
                                   : fileName;

        String comment = "Generated from " + fileName + " on " +
            new Date(System.currentTimeMillis());

        String rtFileName = smilFile.getName();
        index = rtFileName.lastIndexOf('.');

        if (index > 0) {
            rtFileName = rtFileName.substring(0, index);
        }
        
        transformer2rt.setParameter("offset", offset);
        transformer2rt.setParameter("minimalDur", minimalDur);
        
        transformer2smil.setParameter("offset", offset);
        transformer2smil.setParameter("comment", comment);
        transformer2smil.setParameter("title", title);
        
        if(fontSettingMap != null){
        
        	if(fontSettingMap.get("size") != null){
        		transformer2smil.setParameter("font_size", (Integer)fontSettingMap.get("size"));
        		transformer2rt.setParameter("font_size", (Integer)fontSettingMap.get("size"));
        	}
        	
        	if(fontSettingMap.get("font") != null){
        		transformer2rt.setParameter("font", (String)fontSettingMap.get("font"));
        	}
        
        	if(fontSettingMap.get("textColor") != null){
        		String rgb = Integer.toHexString(((Color)fontSettingMap.get("textColor")).getRGB());
        		rgb = rgb.substring(2, rgb.length());
        		transformer2rt.setParameter("subtitle_foreground_color", "#"+rgb);
        	}
        
        	if(fontSettingMap.get("backColor") != null){
        		String rgb = Integer.toHexString(((Color)fontSettingMap.get("backColor")).getRGB());
        		rgb = rgb.substring(2, rgb.length());
        		transformer2rt.setParameter("subtitle_background_color", "#"+rgb);
        		transformer2smil.setParameter("subtitle_background_color","#"+ rgb);
        		transformer2smil.setParameter("video_background_color","#"+ rgb);
        	}
       	}
        

        if (mediaURL != null) {
            transformer2smil.setParameter("media_url", mediaURL);
        }

        if (tierNames != null) {
            String tierString = tierNames[0];

            for (int i = 1; i < tierNames.length; i++) {
                tierString += (" " + tierNames[i]);
            }

            transformer2smil.setParameter("tier", tierString);
        }

        transformer2smil.setParameter("rtFileName", rtFileName + ".rt");

        FileOutputStream stream;
		//transformation
        // HS May 2008: use a FileOutputStream instead of just the file to 
        // prevent a FileNotFoundException
        transformer2smil.transform(new StreamSource(eafURL.openStream()),
            new StreamResult(stream = new FileOutputStream(smilFile)));
        stream.close();

        for (int i = 0; i < tierNames.length; i++) {
            File rtFile = new File(smilFile.getParent(),
                    rtFileName + "_" + tierNames[i] + ".rt");

            transformer2rt.setParameter("tier", tierNames[i]);

            transformer2rt.transform(new StreamSource(eafURL.openStream()),
                new StreamResult(stream = new FileOutputStream(rtFile)));
            stream.close();
        }

        //clear
        transformer2smil.clearParameters();
        transformer2rt.clearParameters();
    }

    /**
     * without reference to media
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param beginTime
     * @param endTime
     *
     * @throws IOException
     * @throws TransformerException
     */
    public void export2SMIL(File eafFile, File smilFile, String[] tierNames,
        long beginTime, long endTime,long offset, int  minimalDur, boolean recalculateTime, Map<String, Object> fontSettingHashMap) throws IOException, TransformerException {
        export2SMIL(eafFile, smilFile, tierNames, null, beginTime, endTime, offset, minimalDur, recalculateTime, fontSettingHashMap);
    }

    /**
     * same as below with File instead of URL
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param mediaURL
     * @param beginTime
     * @param endTime
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(File eafFile, File smilFile,
        String[] tierNames, String mediaURL, long beginTime, long endTime, long offset, int  minimalDur, boolean recalculateTime, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        try {
            URL eafURL = new URL("file:///" + eafFile.getAbsolutePath());
            export2SMIL(eafURL, smilFile, tierNames, mediaURL, beginTime,
                endTime, offset,  minimalDur, recalculateTime, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }  
    
    /**
     * same as below with File instead of URL
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param mediaURL
     * @param beginTime
     * @param endTime
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(Transcription transcription, File smilFile,
        String[] tierNames, String mediaURL, long beginTime, long endTime, long offset, int  minimalDur, boolean recalculateTime, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
    	
       	 Annotation[] annotations = null;
    	         
       	 // to avoid negative values in the time, if the selection made is not accurate
         if(recalculateTime){          
         	for (int j = 0; j < tierNames.length; j++) {
         		if (tierNames.length > 1) { 
         			Tier tier = (Tier) transcription.getTierWithId(tierNames[j]); 
         			annotations = (Annotation[]) tier.getAnnotations().toArray(new Annotation[0]);  
         			long b;   
         			for (int i = 0; i < annotations.length; i++) {
         				if (annotations[i] != null) {
         					if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) {                     	                    	                    	                    	
         						b = annotations[i].getBeginTimeBoundary(); 
         							if (b < beginTime){
         								beginTime = b;         								
         								break;
         							}
         					}
         				}
         			}
         		}
         	}
         }
         							
        try {
            URL eafURL = new URL("file:///" + (new File(((TranscriptionImpl) transcription).getPathName())).getAbsolutePath());
            export2SMIL(eafURL, smilFile, tierNames, mediaURL, beginTime,
                endTime, offset,  minimalDur, recalculateTime, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }   

    /**
     * restrict export to annotations within time interval;
     * media file isn't changed, however a player should play only the indicated interval 
     *
     * @param eafURL
     * @param smilFile
     * @param tierNames
     * @param mediaURL 
     * @param beginTime
     * @param endTime
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMIL(URL eafURL, File smilFile,
        String[] tierNames, String mediaURL, long beginTime, long endTime, long offset, int  minimalDur, boolean recalculateTime, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        createTransformer();
        
        transformer2smil.setParameter("media_start_time", "" + beginTime);
        transformer2smil.setParameter("media_stop_time", "" + endTime);       
        transformer2rt.setParameter("media_start_time", "" + beginTime);
        transformer2rt.setParameter("media_stop_time", "" + endTime);
        if(recalculateTime){
        	 transformer2rt.setParameter("recalculate_time_interval", "true");       
        	 transformer2smil.setParameter("recalculate_time_interval", "true");       
        }
        export2SMIL(eafURL, smilFile, tierNames, mediaURL, offset, minimalDur, fontSettingHashMap);
    }    
    
    
    /**
     * command line approach
     *
     * @param args URL of eaf-file
     */
    public static void main(String[] args) {
        String eafFile = null;

        if (args.length > 0) {
            eafFile = args[0];

            if (eafFile.indexOf(':') == -1) {
                eafFile = "file:///" + eafFile;
            }
        }

        try {
            URL eafURL = new URL((eafFile != null) ? eafFile
                                                   : ("file:///" +
                    System.getProperty("user.dir") +
                    "/resources/testdata/elan/elan-example2.eaf"));
            export2SMIL(eafURL,
                new File(eafURL.getFile().replaceAll(".eaf$", ".smil")),
                new String[] { "W-Spch", "K-Spch" }, null, 15200, 17200, 0L, 0, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sets xsl-scripts
     *
     * @throws TransformerException
     * @throws IOException
     */
    private static void createTransformer()
        throws TransformerException, IOException {
        if (transformer2smil == null) {
            URL eaf2smil = EAF2SMIL.class.getResource(
                    "/mpi/eudico/resources/eaf2smil.xsl");
            URL eaf2rt = EAF2SMIL.class.getResource(
                    "/mpi/eudico/resources/eaf2rt.xsl");

            TransformerFactory tFactory = TransformerFactory.newInstance();

            transformer2smil = tFactory.newTransformer(new StreamSource(
                        eaf2smil.openStream()));
            transformer2rt = tFactory.newTransformer(new StreamSource(
                        eaf2rt.openStream()));
        } 
    }
}
