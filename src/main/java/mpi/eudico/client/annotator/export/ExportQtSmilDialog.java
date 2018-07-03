package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;

/**
 * DOCUMENT ME!
 * 
 * /**
 * This class extracts annotations out of an eaf-file puts them into the SMIL-format.
 * The output consists of at least two files for Quick Time
 * One with ending .sml that contains layout information for the quick time player and a reference to the media file and subtitle file.
 * One or more ending with .txt which contain(s) the annotations of the corresponding tier(s) as subtitles.
 *  
 *
 *
 * Created on Oct 08 2010
 * @author $Aarthy Somasundaram $
 * @version $Oct 08 2010 $
 */

@SuppressWarnings("serial")
public class ExportQtSmilDialog extends ExportQtSubtitleDialog{
	
	private static Transformer transformer2smilQt;

	/**
     * 
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     * 
     */
    public ExportQtSmilDialog (Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
    	super(parent, modal, transcription, selection, true);           
    }
    
    
    /**
     * export with reference to media file
     *
     * @param eafURL
     * @param smilFile
     * @param tierNames
     * @param mediaFileName reference to media file
     * @param fontSettingMap : Map<String, Object> Objects are Color,
     * or Boolean for the key "transparent".
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMILQt(URL eafURL, File smilFile,
        String[] tierNames, String mediaFileName, Map<String, Object> fontSettingMap)
        throws IOException, TransformerException {
        createTransformer();

        //Parameter setting
        String fileName = new File(eafURL.getFile()).getName();

        int index = fileName.lastIndexOf('.');
        String title = (index > 0) ? (fileName = fileName.substring(0, index))
                                   : fileName;

        String comment = "Generated from " + fileName + " on " +
            new Date(System.currentTimeMillis());

        String txtFileName = smilFile.getName();
        index = txtFileName.lastIndexOf('.');

        if (index > 0) {
            txtFileName = txtFileName.substring(0, index);        }
        
        
        transformer2smilQt.setParameter("comment", comment);
        transformer2smilQt.setParameter("title", title); 
        if(fontSettingMap != null){
        
        	if(fontSettingMap.get("backColor") != null){  
        		String rgb = Integer.toHexString(((Color)fontSettingMap.get("backColor")).getRGB());
        		rgb = rgb.substring(2, rgb.length());

        		transformer2smilQt.setParameter("background_color", "#"+rgb);
        	}
        	
        	if(fontSettingMap.get("size") != null){  
        		transformer2smilQt.setParameter("font_size", fontSettingMap.get("size"));
        	}
        	
        	if(fontSettingMap.get("transparent") != null){
        		if((Boolean)fontSettingMap.get("transparent")){
        			transformer2smilQt.setParameter("transparent_background", "true");
        		} else{
        			transformer2smilQt.setParameter("transparent_background", "false");
        		}
        	}      		
        	
        }
        

        if (mediaFileName != null) {
            transformer2smilQt.setParameter("media_url", mediaFileName);
        }

        if (tierNames != null) {
        	
            String tierString = tierNames[0];
            

            for (int i = 1; i < tierNames.length; i++) {
                tierString += ("," + tierNames[i]);
            }

            transformer2smilQt.setParameter("tier", tierString);
        }

        transformer2smilQt.setParameter("txtFileName", txtFileName + ".txt");

        FileOutputStream stream;
		//transformation
        // HS May 2008: use a FileOutputStream instead of just the file to 
        // prevent a FileNotFoundException
        transformer2smilQt.transform(new StreamSource(eafURL.openStream()),
            new StreamResult(stream = new FileOutputStream(smilFile)));
        stream.close();
        
        //clear
        transformer2smilQt.clearParameters();        
    }
    
    /** restrict export to annotations within time interval;
     * media file isn't changed, however a player should play only the indicated interval
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param mediaFileName
     * @param beginTime
     * @param endTime
     * @param recalculateTimeInterval
     * @param merged
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMILQt(File eafFile, File smilFile,
        String[] tierNames, String mediaFileName, long beginTime, long endTime, boolean recalculateTimeInterval, boolean merged, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        try {        	
        	
            URL eafURL = new URL("file:///"+eafFile.getAbsolutePath());
            export2SMILQt(eafURL, smilFile, tierNames, mediaFileName, beginTime,
                endTime, recalculateTimeInterval, merged, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }        
    } 
    
    /**  same as above, only URL instead of File 
     *
     * @param eafURL
     * @param smilFile
     * @param tierNames
     * @param mediaFileName 
     * @param beginTime
     * @param endTime
     * @param recalculateTimeInterval
     * @param merged
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMILQt(URL eafURL, File smilFile,
        String[] tierNames, String mediaFileName, long beginTime, long endTime, boolean recalculateTimeInterval, boolean merged, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        createTransformer();
        
        String begin_time = TimeFormatter.toString(beginTime);
        int index = begin_time.indexOf(':');
        begin_time = begin_time.substring(index+1, begin_time.indexOf('.'));
        
        String end_time = TimeFormatter.toString(endTime + 1000L );
        index = end_time.indexOf(':');
        end_time = end_time.substring(index+1, end_time.indexOf('.')); 
        
       // set parameter merged to tru and add chaanges acc in the xsl   
        transformer2smilQt.setParameter("selected_time_interval", "true");
        if( recalculateTimeInterval){
        	transformer2smilQt.setParameter("recalculate_time_interval", "true");
        }
        if(merged){
        	transformer2smilQt.setParameter("merge", "true");
        }
        transformer2smilQt.setParameter("media_start_time", begin_time);
        transformer2smilQt.setParameter("media_stop_time", end_time);
        transformer2smilQt.setParameter("media_dur", TimeFormatter.toString(endTime - beginTime));        
        export2SMILQt(eafURL, smilFile, tierNames, mediaFileName, fontSettingHashMap);
    }
    
    
    /**  export the all the annotations 
     *
     * @param eafURL
     * @param smilFile
     * @param tierNames
     * @param mediaURL 
     * @param beginTime
     * @param endTime
     * @param merged
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMILQt(URL eafURL, File smilFile,
        String[] tierNames, String mediaURL,  long mediaDur, boolean merged, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        createTransformer();
        
        if(merged){
        	transformer2smilQt.setParameter("merge", "true");
        }
        transformer2smilQt.setParameter("media_dur", TimeFormatter.toString(mediaDur));
        export2SMILQt(eafURL, smilFile, tierNames, mediaURL, fontSettingHashMap);
    }
    
    /**
     * same as above with File instead of URL
     *
     * @param eafFile
     * @param smilFile
     * @param tierNames
     * @param mediaURL
     * @param beginTime
     * @param endTime     
     * @param merged
     *
     * @throws IOException
     * @throws TransformerException
     */
    public static void export2SMILQt(File eafFile, File smilFile,
    		String[] tierNames, String mediaURL,  long mediaDur, boolean merged, Map<String, Object> fontSettingHashMap)
        throws IOException, TransformerException {
        try {
            URL eafURL = new URL("file:///"+ eafFile.getAbsolutePath());
            export2SMILQt(eafURL, smilFile, tierNames, mediaURL, mediaDur, merged, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
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
        if( transformer2smilQt == null){
        	String file = "/mpi/eudico/resources/eaf2smilQt.xsl";
        	URL eaf2smilQt = ExportQtSmilDialog.class.getResource(file);        	

        	TransformerFactory tFactory = TransformerFactory.newInstance();

        	transformer2smilQt = tFactory.newTransformer(new StreamSource(
                eaf2smilQt.openStream()));    	
        }
    }
}


   

    
    
