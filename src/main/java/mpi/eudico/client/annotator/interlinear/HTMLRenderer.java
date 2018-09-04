package mpi.eudico.client.annotator.interlinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.util.XMLEscape;

/**
 * A class that renders interlinearized content as tables in a html file, using
 * UTF-8 encoding.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class HTMLRenderer {
    /** new line string */
    private final String NEW_LINE = "\n";

    /** new line string */
    private final String BREAK = "<br>";// <br/> would be more strict

    /** white space string */
    private final String NBSP = "&nbsp;";
    private Interlinear interlinear;
    private HashMap<String, String> tierMap;
    private String mediaType = "";
    private XMLEscape xmlEscape;

    /**
     * Creates a new HTMLRenderer instance
     *
     * @param interlinear the Interlinear object holding the formatted,
     *        interlinearized content
     */
    public HTMLRenderer(Interlinear interlinear) {
        this.interlinear = interlinear;
        tierMap = new HashMap<String, String>();
        xmlEscape = new XMLEscape();
    }
    
    /**
     * Renders (writes) the content to a html File, converting annotation blocks to html tables,
     * using the default utf-8 character encoding
     * 
     * @param outFile the File to write the html to
     * @throws IOException any IOException that can occur while writing to a file
     * @throws FileNotFoundException thrown when the export file could not be found
     */
    public void renderToFile(File outFile) throws IOException, FileNotFoundException {
        renderToFile(outFile, "UTF-8");
    }
    
    /**
     * Renders (writes) the content to a html File, converting annotation blocks to html tables.
     * 
     * @param outFile the File to write the html to
     * @param charEncoding the character encoding for output
     * @throws IOException any IOException that can occur while writing to a
     *         file
     * @throws FileNotFoundException thrown when the export file could not be
     *         found
     * @throws NullPointerException when the Interlinear object or the export
     *         file  is <code>null</code>
     */
    public void renderToFile(File outFile, String charEncoding) throws IOException, FileNotFoundException {
        if (interlinear == null) {
            throw new NullPointerException("Interlinear object is null");
        }

        if (outFile == null) {
            throw new NullPointerException("Export file is null");
        }
        
        tierMap.clear();
        // fill a map of visible tier names to css class names
        Tier ti;
        for (int i = 0; i < interlinear.getVisibleTiers().size(); i++) {
            ti = interlinear.getVisibleTiers().get(i);
            tierMap.put(ti.getName(), ("ti-" + i));
        }
//      create output stream
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(outFile);
			OutputStreamWriter osw = null;
			try {
				osw = new OutputStreamWriter(out, charEncoding);
			} catch (UnsupportedCharsetException uce) {
				osw = new OutputStreamWriter(out, "UTF-8");
			}
			writer = new BufferedWriter(osw);

			writeHTMLHeader(writer);
			writeBlocks(writer);
			writeFooter(writer);
        } finally {
            try {
                writer.close();
            } catch (Exception ee) {
            }
        }
    }
    
    /**
     * Writes html code to a StringWriter, the content of which is returned.
     * Special html generation is applied in order to be able to use the Java JEditorPane 
     * to render the preview. This pane with the HTMLEditorkit does not seem to support all
     * css style attributes.
     * @return the html code as a single string
     */
    public String renderToText() {
        tierMap.clear();
        // fill a map of visible tier names to css class names
        Tier ti;
        for (int i = 0; i < interlinear.getVisibleTiers().size(); i++) {
            ti = interlinear.getVisibleTiers().get(i);
            tierMap.put(ti.getName(), ("ti-" + i));
        }
        
        StringWriter writer = new StringWriter(10000);
        try {
            writePreviewHTML(writer); 
        } catch (IOException ioe) {
            
            return writer.toString();   
        }
        // System.out.println(writer.toString());
        return writer.toString();
    }
    
    /**
     * Writes the header part of the html file
     * @param writer the writer
     * @throws IOException io exception
     */
    private void writeHTMLHeader(Writer writer) throws IOException {
        // doctype
    	if (interlinear.isPlaySoundSel()) {// HS requires HTML 5
    		writer.write("<!DOCTYPE html>");
    	} else {
    		writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" + NEW_LINE +
    		"\"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">");
    	}
        writer.write(NEW_LINE);
        writer.write("<html>" + NEW_LINE + "<head>" + NEW_LINE);
        if (interlinear.isPlaySoundSel()) {
        	writer.write("<meta charset=\"UTF-8\"/>" + NEW_LINE);// html 5
        } else {
        	writer.write("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"/>" + NEW_LINE);
        }
        writer.write("<title>" + interlinear.getTranscription().getName() + "</title>" + NEW_LINE);
        writeStyles(writer);
        writer.write("</head>" + NEW_LINE);
        writer.write("<body>" + NEW_LINE);
        
        if (interlinear.isPlaySoundSel()) {
            //CC 25/10/2010 
            String mediaURL=interlinear.getMediaURL();
            if (interlinear.isMediaVideo()) {
            	mediaType = "video";
            } else {
            	mediaType = "audio";
            }
            if (mediaURL != null) {
	            String media = mediaURL.substring(mediaURL.lastIndexOf("/")+1);
	            writer.write("<div id=\"main\">" + 
	                    "<"+ mediaType + " id=\"" + media + "\" src=\"" + media + "\" controls>" +
	                    "</" + mediaType + "></div>" + NEW_LINE);
            }
        }
        writer.write("<h3>");
        writer.write(interlinear.getTranscription().getFullPath());
        writer.write("</h3>");
        writer.write(NEW_LINE);
        writer.write("<p>");
        writer.write(DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.SHORT, Locale.getDefault()).format(new Date(
                    System.currentTimeMillis())));
        writer.write("</p>");
        writer.write(NEW_LINE);
        writer.write(NEW_LINE);
        // start outer table
        writer.write("<table class=\"out\">");
        writer.write(NEW_LINE);
    }
    
    /**
     * Inserts css style sheets into the header of the html document (rather than to a separate css file).
     * Creates a separate style for each tier in the output.
     * @param writer the writer
     * @throws IOException any ioexception
     */
    private void writeStyles(Writer writer) throws IOException {
        writer.write("<style type=\"text/css\" media=\"screen\">" + NEW_LINE);
        // body defaults
        writer.write("body {" + NEW_LINE);
        writer.write("background-color: #FFFFF2;" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-weight: normal;" + NEW_LINE);
        writer.write("font-size: 12px;" + NEW_LINE);// default
        writer.write("color: #000000;" + NEW_LINE);
        writer.write("line-height: 15px;" + NEW_LINE);
        writer.write("font-style: normal;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // tables for the annotation blocks
        writer.write("table {" + NEW_LINE);
        writer.write("width: auto;" + " /* change to 100% to horizontally stretch the table */" + NEW_LINE);
        writer.write("border-collapse: collapse;" + NEW_LINE);
        writer.write("border: 1px solid #666666;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // the table enclosing the annotation tables
        writer.write("table.out {" + NEW_LINE);
        writer.write("border: 0px;" + NEW_LINE);
        writer.write("width: 100%;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // row defaults
        writer.write("tr {" + NEW_LINE);
        writer.write("border-collapse: collapse;" + NEW_LINE);
        writer.write("border: 1px solid #dddddd;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        writer.write("tr.out {" + NEW_LINE);
        writer.write("border: 0px;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // cell defaults
        writer.write("td {" + NEW_LINE);
        writer.write("padding: 2px 8px 2px 2px;" + NEW_LINE);
        writer.write("border-collapse: collapse;" + NEW_LINE);
        writer.write("border: 1px solid #666666;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        writer.write("td.out {" + NEW_LINE);// cell containing a annotation table
        writer.write("border: 0px;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // the tier labels
        writer.write("td.label {" + NEW_LINE);
        writer.write("width: " + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + ";" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-weight: bold;" + NEW_LINE);
        writer.write("font-size: 12px;" + NEW_LINE);
        writer.write("color: #444444;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // timecode values and labels
        writer.write("td.tclabel {" + NEW_LINE);
        writer.write("width: " + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + ";" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-size: 11px;" + NEW_LINE);
        writer.write("font-weight: bold;" + NEW_LINE);
        writer.write("color: #9E001C;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        writer.write("td.tc {" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-size: 11px;" + NEW_LINE);
        writer.write("font-weight: bold;" + NEW_LINE);
        writer.write("color: #9E001C;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // silence duration sdlabel
        writer.write("td.sdlabel {" + NEW_LINE);
        writer.write("width: " + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + ";" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-size: 11px;" + NEW_LINE);
        writer.write("font-weight: bold;" + NEW_LINE);
        writer.write("color: #9E001C;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        writer.write("td.sd {" + NEW_LINE);
        writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
        writer.write("font-size: 11px;" + NEW_LINE);
        writer.write("font-weight: bold;" + NEW_LINE);
        writer.write("color: #9E001C;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // invisible cells
        writer.write("td.hide {" + NEW_LINE);
        writer.write("border: 0px;" + NEW_LINE);
        writer.write("}" + NEW_LINE);
        // add a per tier class with font size and (in the future) font name
        writer.write("/* a style class for each visible tier */" + NEW_LINE);
        Set<String> keys = tierMap.keySet();
        String key, val;
        Iterator<String> keit = keys.iterator();
        while (keit.hasNext()) {
            key = keit.next();
            val = tierMap.get(key);
            writer.write("tr." + val + " { " + "/* " + key + " */" + NEW_LINE);
            // next line should print the preferred font name 
            // writer.write("font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;" + NEW_LINE);
            writer.write("font-family: \"" + interlinear.getFont(key).getName() + "\";" + NEW_LINE);
            //writer.write("/* add font-family: nnn; to use a custom font */" + NEW_LINE);
            writer.write("font-size: " + interlinear.getFontSize(key) + "px;" + NEW_LINE);
            writer.write("}" + NEW_LINE);
        }
        
        writer.write("</style>"); 
        writer.write(NEW_LINE);
    }
    
    /**
     * Adds the contents of the annotation blocks to the body of the html file.
     * @param writer the writer
     * @throws IOException any io exception
     */
    private void writeBlocks(Writer writer) throws IOException {
        List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
        InterlinearBlock printBlock = null;

        for (int i = 0; i < blocks.size(); i++) {
            printBlock = blocks.get(i);
            writer.write("<tr class=\"out\"><td class=\"out\">" + NEW_LINE);
            if (interlinear.isPlaySoundSel()) {
            	writeBlockSound(writer, printBlock);
            } else {
            	writeBlock(writer, printBlock);
            }
            writer.write("</td></tr>" + NEW_LINE);
            writer.write("<tr class=\"out\"><td class=\"out\">" + NBSP);
            for (int j = 0; j < interlinear.getBlockSpacing(); j++) {
                writer.write(BREAK);
            }
            writer.write("</td></tr>" + NEW_LINE);
        }
    }
    
    /**
     * Converts each block of annotations to an html table, that is inserted in a cell of 
     * an enclosing table.
     * @param writer the writer
     * @param block the annotation/print block 
     * @throws IOException any ioexception
     */
    private void writeBlock(Writer writer, InterlinearBlock block) throws IOException {
        InterlinearTier it = null;
        InterlinearAnnotation ia = null;
        
        writer.write("<table>" +  NEW_LINE);
        for (int i = 0; i < block.getPrintTiers().size(); i++) {
            it = block.getPrintTiers().get(i);
            writer.write("<tr class=\"" + tierMap.get(it.getTierName()) + "\">");
            if (interlinear.isTierLabelsShown()) {// HS why is this commented out?
                if (it.isTimeCode()) {
                    writer.write("<td class=\"tclabel\">");
                    writer.write(interlinear.getMetrics().TC_TIER_NAME);                    
                } else if (it.isSilDuration()) { //HS 07-2011 added else?
                    writer.write("<td class=\"sdlabel\">");
                    writer.write(interlinear.getMetrics().SD_TIER_NAME);                    
                } else {
                    writer.write("<td class=\"label\">");
                    writer.write(xmlEscape.escape(it.getTierName()));// HS 09-2012 escape
                }
                writer.write("</td>");
            }
            
            for (int j = 0; j < it.getAnnotations().size(); j++) {
                ia = it.getAnnotations().get(j);
                
                if (ia.hidden) {
                    writer.write("<td colspan=\"" + ia.colSpan + "\" class=\"hide\">" + "</td>" );
                } else {
                    if (it.isTimeCode()) {
                        writer.write("<td colspan=\"" + ia.colSpan + "\" class=\"tc\">" + ia.getValue() + "</td>" );
                    } else if (it.isSilDuration()) {
                        writer.write("<td colspan=\"" + ia.colSpan + "\" class=\"sd\">" + ia.getValue() + "</td>" );
                    } else {                       
                        if (ia.nrOfLines == 1){
                            writer.write("<td colspan=\"" + ia.colSpan + "\">" + xmlEscape.escape(ia.getValue()) + "</td>" );// HS 09-2012 escape
                        } else {
                            writer.write("<td colspan=\"" + ia.colSpan + "\">");
                            for (int k = 0; k < ia.nrOfLines; k++) {
                                writer.write(xmlEscape.escape(ia.getLines()[k]));// HS 09-2012 escape
                                if (k < ia.nrOfLines - 1) {
                                    writer.write(BREAK);
                                }
                            }
                            writer.write("</td>");
                        }
                    }
                }                
            }
            writer.write("</tr>" + NEW_LINE);
        }
        writer.write("</table>" + NEW_LINE);
    }
    
    /**
     * Converts each block of annotations to an html table, that is inserted in a cell of 
     * an enclosing table. This version includes attributes for audio playback, requires HTML 5.
     * CC : modif to allow javascript audio.play()
     * @param writer the writer
     * @param block the annotation/print block 
     * @throws IOException any ioexception
     */
    private void writeBlockSound(Writer writer, InterlinearBlock block) throws IOException {
        InterlinearTier it = null;
        InterlinearTier itx = null;
        InterlinearAnnotation ia = null;
        InterlinearAnnotation iax = null;
         
        //recherche des index son. CC 180311      
        String href = ""; 
        HashMap<Integer, String> hrefMap = new HashMap<Integer, String>();
        //last=last tier but TC
        // HS changed because of introduction of silence duration markers
        for (int i = 0; i < block.getPrintTiers().size(); i++) {
            itx = block.getPrintTiers().get(i);
            if (itx.isTimeCode()){ 
                for (int k = 0; k < itx.getAnnotations().size(); k++) {     	
    	        	iax = itx.getAnnotations().get(k); 
    				String[] tCode = iax.getValue().split(" - "); 
    				href = "";
    				if (tCode.length > 1) {
    					String begin = tCode[0];
    					String end=tCode[1];
    					href="<a href=\"javascript:jumpToTimeoffset('" + begin + "','" + end + "');\">";	
                	}
    				hrefMap.put(k, href);
                }
            }
        }
        /*
        Integer last = block.getPrintTiers().size() - 1;
        itx = (InterlinearTier) block.getPrintTiers().get(last);	//derniere ligne=TimeCode
        if (itx.isTimeCode()){ 
            for (int k = 0; k < itx.getAnnotations().size(); k++) {     	
	        	iax = (InterlinearAnnotation) itx.getAnnotations().get(k); 
				String[] tCode = iax.getValue().split(" - "); 
				href = "";
				if (tCode.length > 1) {
					String begin = tCode[0];
					String end=tCode[1];
					href="<a href=\"javascript:jumpToTimeoffset('" + begin + "','" + end + "');\">";	
            	}
				hrefMap.put(k, href);
            }
        }
        */
        Boolean play= true; 
        writer.write("<table>" +  NEW_LINE);   
//        if (interlinear.isTimeCodeShown()) {
//        	last++;
//        }	// affichage de la tier TimeCode
        
        for (int i = 0; i < block.getPrintTiers().size(); i++) {				//pour chaque Tier
           it = block.getPrintTiers().get(i);
           if (it.isTimeCode() && !interlinear.isTimeCodeShown()) {
        	   continue;// HS to prevent writing of time code while it should not be visible
           }
           writer.write("<tr class=\"" + tierMap.get(it.getTierName()) + "\">");
           if (interlinear.isTierLabelsShown()) {
                if (it.isTimeCode() /*&& !interlinear.isPlaySoundSel()*/) {// HS why this check on isPlaySoundSel?
 	               writer.write("<td class=\"tclabel\">");
	               writer.write(interlinear.getMetrics().TC_TIER_NAME);                    
                 } else if (it.isSilDuration()) { //HS 07-2011 added else?
                     writer.write("<td class=\"sdlabel\">");
                     writer.write(interlinear.getMetrics().SD_TIER_NAME);                    
                 } else {
                    writer.write("<td class=\"label\">");
                    writer.write(xmlEscape.escape(it.getTierName()));// HS 09-2012 escape
                }
                writer.write("</td>");
           }

           for (int j = 0; j < it.getAnnotations().size(); j++) {	//pour chaque Annotation
       	   		String hRef=""; 
       	   		String hRefa=""; 
       	   		ia = it.getAnnotations().get(j);   
       	   		// first tier not hidden = play
	            if (!interlinear.isTimeCodeShown() && interlinear.isPlaySoundSel() && !ia.hidden && play==true) {
	            	hRef = hrefMap.get(j);
	            	hRefa="</a>";
	            } else {
	            	hRef="";
	            	hRefa="";
	            }
	            
	 	        if (ia.hidden) {
	                writer.write("<td colspan=\"" + ia.colSpan + "\" class=\"hide\">" + "</td>" );
	            } else {
	                if (it.isTimeCode() && !interlinear.isPlaySoundSel() && /*!*/interlinear.isTimeCodeShown()) {// HS isTimeCodeShown ?? how should the test be
	                	writer.write("<td colspan=\"" + ia.colSpan +
	 			            "\" class=\"tc\">" + ia.getValue() + "</td>");                	                    
	                } 
	                else if (it.isTimeCode() && interlinear.isTimeCodeShown() && interlinear.isPlaySoundSel()) {
	                	writer.write("<td colspan=\"" + ia.colSpan +
	 			            "\" class=\"tc\">" + hrefMap.get(j) + ia.getValue() + "</a></td>");                	                    
	                } 
	                else if (it.isSilDuration()) {
                        writer.write("<td colspan=\"" + ia.colSpan + "\" class=\"sd\">" + ia.getValue() + "</td>" );
                    }
	                else {
	                    if (ia.nrOfLines == 1) {  
	                        writer.write("<td colspan=\"" + ia.colSpan  + "\">" +
	                                 hRef + xmlEscape.escape(ia.getValue()) + hRefa+ "</td>");// HS 09-2012 escape
	                    } 
	                    else {
	                        writer.write("<td colspan=\"" + ia.colSpan + "\">" + hRef);
	                        for (int k = 0; k < ia.nrOfLines; k++) {
	                            writer.write(xmlEscape.escape(ia.getLines()[k]));// HS 09-2012 escape
	                            if (k < ia.nrOfLines - 1) {
	                                writer.write(BREAK);
	                            }
	                        }
	                        writer.write(hRefa + "</td>");
	                    }
	                }
	            }                
	        }
           writer.write("</tr>" + NEW_LINE);
           if (play == true) {
        	   play=false;
           }
       }
       writer.write("</table>" + NEW_LINE);
   }
    
    /**
     * Finishes the html document.
     * @param writer the writer
     * @throws IOException any io exception
     */
    private void writeFooter(Writer writer) throws IOException {
        writer.write("</table>" + NEW_LINE);
        if (interlinear.isPlaySoundSel()) {
            // CC 25/10/2010  javascript pour ecouter les phrases 
            writer.write("<script type=\"text/javascript\">" +
         		"var media = document.getElementsByTagName(\""+ mediaType + "\")[0];" +
         		"function jumpToTimeoffset(start, end) {" +
     	    		"media.currentTime = start;" +
     	    		"media.play();" +
     	    		"setTimeout(\"media.pause();\", (end - start)*1000);}" +
     	    	"</script>");
        }
        writer.write("</body>" + NEW_LINE);
        writer.write("</html>");
    }
 
    /**
     * Special html generation for the preview using the Java (1.4.x) JEditorPane and the HTMLEditor kit,
     * which doesn't seem to support all css attributes etc.
     * @param writer the StringWriter
     * @throws IOException any io exception
     */
    private void writePreviewHTML(Writer writer) throws IOException {
        writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" + NEW_LINE +
        "\"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">");
        writer.write(NEW_LINE);
        writer.write("<html>" + NEW_LINE + "<head>" + NEW_LINE);
        //writer.write("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" + NEW_LINE);
        writer.write("<title>" + interlinear.getTranscription().getName() + "</title>" + NEW_LINE);
        writer.write("</head>" + NEW_LINE);
        writer.write("<body style=\"background-color: #FFFFF2; font-family: \"Arial Unicode MS\", Verdana, Helvetica, sans-serif;\">" + NEW_LINE);
        writer.write("<h4 style=\"color: #ff0000\">");
        writer.write("NOTE: this preview may differ considerably from the appearance in most modern browsers!");
        writer.write("</h4>");
        writer.write("<h3>");
        writer.write(interlinear.getTranscription().getFullPath());
        writer.write("</h3>");
        writer.write(NEW_LINE);
        writer.write("<p>");
        writer.write(DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.SHORT, Locale.getDefault()).format(new Date(
                    System.currentTimeMillis())));
        writer.write("</p>" + NEW_LINE);
        // start outer table
        writer.write("<table width=\"100%\" border=\"0px\">" + NEW_LINE);
        
        List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
        InterlinearBlock printBlock = null;

        for (int i = 0; i < blocks.size(); i++) {
            printBlock = blocks.get(i);
            writer.write("<tr border=\"0px\"><td border=\"0px\">" + NEW_LINE);
            writePreviewBlock(writer, printBlock);
            writer.write("</td></tr>" + NEW_LINE);
            writer.write("<tr border=\"0px\"><td border=\"0px\">" + NBSP);
            for (int j = 0; j < interlinear.getBlockSpacing(); j++) {
                writer.write(BREAK);
            }
            writer.write("</td></tr>" + NEW_LINE);
        }
        
        writer.write("</table>" + NEW_LINE);
        writer.write(NEW_LINE);
        writer.write("</body>" + NEW_LINE);
        writer.write("</html>");
    }
    
    /**
     * Special html generation for the preview using the Java JEditorPane and the HTMLEditor kit,
     * which doesn't seem to support all css attributes etc.
     * @param writer the StringWriter
     * @param block the print block to convert
     * @throws IOException any io exception
     */
    private void writePreviewBlock(Writer writer, InterlinearBlock block) throws IOException {
        InterlinearTier it = null;
        InterlinearAnnotation ia = null;
        
        writer.write("<table width=\"auto\" border=\"1px\" style=\"border-color: #666666; border-collapse: collapse; border-style: solid;\">" +  NEW_LINE);
        for (int i = 0; i < block.getPrintTiers().size(); i++) {
            it = block.getPrintTiers().get(i);
            if (it.isTimeCode() && !interlinear.isTimeCodeShown()) {
            	continue;// HS to prevent empty rows when sound is supported and time code not shown
            }
            writer.write("<tr border=\"1px\" style=\"font-size: " + interlinear.getFontSize(it.getTierName()) +
                    "px; border-color: #dddddd; border-collapse: collapse; border-style: solid;\">");
            if (interlinear.isTierLabelsShown()) {// HS ??
                if (it.isTimeCode()) {
                	if (interlinear.isTimeCodeShown()){		//CC 29/11/2010
	                    writer.write("<td border=\"1px\" width=\"" + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + "\"" +
	                    		" style=\"font-weight: bold; border-color: #666666; border-collapse: collapse; border-style: solid; font-size: 10px; color: #9E001C;\">");
	                    writer.write(interlinear.getMetrics().TC_TIER_NAME); 
                	}
                } else if (it.isSilDuration()) {
                    writer.write("<td border=\"1px\" width=\"" + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + "\"" +
            			" style=\"font-weight: bold; border-color: #666666; border-collapse: collapse; border-style: solid; font-size: 10px; color: #9E001C;\">");
                    writer.write(interlinear.getMetrics().SD_TIER_NAME);                    
                } else {

                    writer.write("<td border=\"1px\" width=\"" + (int)(1.2 * interlinear.getMetrics().getLeftMargin()) + "\"" +
            		        " style=\"font-weight: bold; border-color: #666666; border-collapse: collapse; border-style: solid; font-size: 12px; color: #444444;\">");
                    writer.write(xmlEscape.escape(it.getTierName()));// HS 09-2012 escape
                }
                writer.write("</td>");
            }
            
            for (int j = 0; j < it.getAnnotations().size(); j++) {
                ia = it.getAnnotations().get(j);
                
                if (ia.hidden) {
                    writer.write("<td colspan=\"" + ia.colSpan + "\" border=\"1px\"" + 
                            " style=\"border-color: #dddddd; border-collapse: collapse; border-style: solid;\">" + "</td>" );
                } else {
                    if (it.isTimeCode()) {
                    	if (interlinear.isTimeCodeShown()){		//CC 29/11/2010
	                        writer.write("<td colspan=\"" + ia.colSpan + "\" border=\"1px\"" + 
	                                " style=\"font-weight: bold; border-color: #666666; border-collapse: collapse; border-style: solid; font-size: 10px; color: #9E001C;\">"
	                                + ia.getValue() + "</td>" );
                    	}
                    } else if (it.isSilDuration()) {
                        writer.write("<td colspan=\"" + ia.colSpan + "\" border=\"1px\"" + 
                                " style=\"font-weight: bold; border-color: #666666; border-collapse: collapse; border-style: solid; font-size: 10px; color: #9E001C;\">"
                                + ia.getValue() + "</td>" );
                    } else {                       
                        if (ia.nrOfLines == 1){
                            writer.write("<td colspan=\"" + ia.colSpan + "\" border=\"1px\"" + 
                                    " style=\"border-color: #666666; border-collapse: collapse; border-style: solid;\">"
                                    + xmlEscape.escape(ia.getValue()) + "</td>" );// HS 09-2012 escape
                        } else {
                            writer.write("<td colspan=\"" + ia.colSpan + "\" border=\"1px\"" + 
                                    " style=\"border-color: #666666; border-collapse: collapse; border-style: solid;\">");
                            for (int k = 0; k < ia.nrOfLines; k++) {
                                writer.write(xmlEscape.escape(ia.getLines()[k]));// HS 09-2012 escape
                                if (k < ia.nrOfLines - 1) {
                                    writer.write(BREAK);
                                }
                            }
                            writer.write("</td>");
                        }
                    }
                }                
            }
            writer.write("</tr>" + NEW_LINE);
        }
        writer.write("</table>" + NEW_LINE);
    }
    
}
