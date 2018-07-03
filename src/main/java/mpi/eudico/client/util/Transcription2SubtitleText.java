package mpi.eudico.client.util;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.util.TimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.text.DecimalFormat;

import java.util.List;


/**
 * Exports annotations of a selection of tiers to a DVD subtitle textformat.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class Transcription2SubtitleText implements ClientLogger {
    /** the Windows CR+LF line break for SRT */
    final private String WIN_NEWLINE = "\r\n";

    /** A formatter for two-digit sequences. */
    private static final DecimalFormat twoDigits = new DecimalFormat("00");

    /**
     * Constructor
     */
    public Transcription2SubtitleText() {
        super();
    }

    /**
     * Exports to .srt format file.  Provides support for a minimum duration
     * tries to solve overlaps.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     * @param reCalculateTime if true make Selection begin time to start from zero 
     *        and not the actual begintime
     *
     * @throws IOException any io exception
     */
    public void exportTiersSRT(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset, boolean reCalculateTime) throws IOException {
        if (exportFile == null) {
            LOG.severe("No export file specified");

            return;
        }
        
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;
        	offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        out = new FileOutputStream(exportFile);

        if (encoding != null) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException uee) {
                LOG.warning("Encoding not supported: " + encoding);
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }  

        for (int i = 0; i < allUnits.size(); i++) {
            unit = (SubtitleUnit) allUnits.get(i);
            writer.write(String.valueOf(i + 1)); // some apps don't accept index 0
            writer.write(WIN_NEWLINE); 
            Long b = unit.getBegin();
            Long e = unit.getCalcEnd();
            if(selection ==0){
            	if( b < recalculateTimeInterval){
            		recalculateTimeInterval = b;
            		selection = 1;
            	}            	
            }           
           
            writer.write(TimeFormatter.toString(b-recalculateTimeInterval).replace('.',','));
            writer.write(" --> ");
            writer.write(TimeFormatter.toString(e-recalculateTimeInterval).replace('.',',')); 
            writer.write(WIN_NEWLINE);   
            

            for (int j = 0; j < unit.getValues().length; j++) {
            	writer.write(unit.getValues()[j].replace('\n', ' '));

            	if (j != (unit.getValues().length - 1)) {
            		writer.write(WIN_NEWLINE);
            	}
            }

            writer.write(WIN_NEWLINE);
            writer.write(WIN_NEWLINE);
        }

        writer.close();
    }

    /**
     * Exports to .srt format file.  Provides support for a minimum duration
     * tries to solve overlaps.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @throws IOException any io exception
     */
    public void exportTiersSRT(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset) throws IOException {
    	exportTiersSRT(transcription, tierNames, exportFile, encoding, beginTime,
                endTime, minimalDuration, offset, false);
    }
    
    /**
     * Exports to .srt format file using the default file encoding, UTF-8.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @throws IOException any io exception
     */
    public void exportTiersSRT(Transcription transcription, String[] tierNames,
        File exportFile, long beginTime, long endTime, int minimalDuration)
        throws IOException {
        exportTiersSRT(transcription, tierNames, exportFile, null, beginTime,
            endTime, minimalDuration, 0L, false);
    }

    /**
     * Exports to .srt format file, using default file encoding (UTF-8),
     * interval begin (0), interval end (Long.MAX_VALUE) and minimal duration
     * (0).
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     *
     * @throws IOException any io exception
     */
    public void exportTiersSRT(Transcription transcription, String[] tierNames,
        File exportFile) throws IOException {
        exportTiersSRT(transcription, tierNames, exportFile, 0, Long.MAX_VALUE,
            0);
    }

    /**
     * Reformats a standard timecode into the format expected in Spruce STL.
     *
     * @param time the time to convert
     * @param frameRate assumed frame rate; if negative, assume default
     * @return an STL-formatted timecode
     */
    private String toSTLTimecode(long time, double frameRate) {
        String tc = null;

        if (frameRate == 25.0) {
            // If PAL, return a PAL timecode.
            tc = TimeFormatter.toTimecodePAL(time);
        } else if (frameRate == 29.97) {
            // If NTSC (drop-frame), return a 30drop-SMPTE timecode.
            tc = TimeFormatter.toTimecodeNTSC(time);
        } else if (frameRate == 30.0) {
            // If this is NTSC (non-drop-frame), return an SMPTE timecode in
            // STL format, assuming a literal 29.27fps.  (This may seem odd,
            // but for DVD Studio Pro 4, using 30drop SMPTE with NTSC sources
            // produces synchronization drift).
            int fc = (int) ((time * 29.97) / 1000);
            String hours = twoDigits.format((((fc / 30) / 60) / 60) % 24);
            String minutes = twoDigits.format(((fc / 30) / 60) % 60);
            String seconds = twoDigits.format((fc / 30) % 60);
            String frames = twoDigits.format(fc % 30);
            tc = hours + ":" + minutes + ":" + seconds + "." + frames;
        } else {
            // Otherwise, default to PAL.
            tc = TimeFormatter.toTimecodePAL(time);
        }

        // Replace the final comma in the timecode with a period.
        tc = tc.substring(0, 8) + '.' + tc.substring(9);
        return tc;
    }

    /**
     * Exports to .stl format file.  Provides support for a minimum duration
     * tries to solve overlaps.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     * @param frameRate the assumed frame rate (negative if assuming default)
     * @param reCalculateTime if true make Selection begin time to start from zero 
     *        and not the actual begintime
     *
     * @throws IOException any io exception
     */
    public void exportTiersSTL(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset, double frameRate, boolean reCalculateTime) throws IOException {
        if (exportFile == null) {
            LOG.severe("No export file specified");
            return;
        }
        
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;
        	offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        out = new FileOutputStream(exportFile);

        if (encoding != null) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException uee) {
                LOG.warning("Encoding not supported: " + encoding);
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }

        // Write the preamble.
        writer.write("$TapeOffset = False");
        writer.write(WIN_NEWLINE);
        
        for (int i = 0; i < allUnits.size(); i++) {
            unit = (SubtitleUnit) allUnits.get(i);
            
            Long b = unit.getBegin() ;
            Long e = unit.getCalcEnd();
            if(selection == 0){
            	if( b < recalculateTimeInterval){
            		recalculateTimeInterval = b;
            		selection = 1;
            	}            	
            }
            writer.write(toSTLTimecode(b-recalculateTimeInterval, frameRate));
            writer.write(',');
            writer.write(toSTLTimecode(e-recalculateTimeInterval, frameRate));
            writer.write(',');

            String[] values = unit.getValues();
            for (int j = 0; j < values.length; j++) {
            	writer.write(values[j].replace('\n', '|'));

            	if (j < values.length - 1) {
            		writer.write('|');
            	}
            }

            writer.write(WIN_NEWLINE);
        }

        writer.close();
    }
    
    /**
     * Exports to .stl format file.  Provides support for a minimum duration
     * tries to solve overlaps.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     * @param frameRate the assumed frame rate (negative if assuming default)
     *
     * @throws IOException any io exception
     */
    public void exportTiersSTL(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset, double frameRate) throws IOException {
    	exportTiersSTL(transcription, tierNames, exportFile, null, beginTime,
                endTime, minimalDuration, 0L, -1.0, false);    
    }

    /**
     * Exports to .stl format file using the default file encoding, UTF-8.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @throws IOException any io exception
     */
    public void exportTiersSTL(Transcription transcription, String[] tierNames,
        File exportFile, long beginTime, long endTime, int minimalDuration)
        throws IOException {
        exportTiersSTL(transcription, tierNames, exportFile, null, beginTime,
            endTime, minimalDuration, 0L, -1.0);
    }

    /**
     * Exports to .stl format file, using default file encoding (UTF-8),
     * interval begin (0), interval end (Long.MAX_VALUE) and minimal duration
     * (0).
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     *
     * @throws IOException any io exception
     */
    public void exportTiersSTL(Transcription transcription, String[] tierNames,
        File exportFile) throws IOException {
        exportTiersSTL(transcription, tierNames, exportFile, 0, Long.MAX_VALUE,
            0);
    }
    
    // ##### LRC section  ######
    /**
     * Exports to .lrc format file.  Does support a minimum duration 
     * (but because lrc has only begin times it is used to insert empty lines), 
     * tries to solve overlaps.
     * A maximum duration would be useful.
     *
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8 (otherwise it should be ANSI)
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param reCalculateTime if true make Selection begin time to start from zero 
     *        and not the actual begintime
     *
     * @throws IOException any io exception
     */
    public void exportTiersLRC(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset, boolean reCalculateTime) throws IOException {
        if (exportFile == null) {
            LOG.severe("No export file specified");
            return;
        }
//        format
//        [COLOUR]0xFF66FF//
//        [00:03.120]Owaranai
//        [00:05.548]mugen no hikari ...
//        [00:09.927]
//        [00:21.016]taeran ninmu
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;
        	offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit;
        SubtitleUnit nextUnit;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        out = new FileOutputStream(exportFile);

        if (encoding != null) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException uee) {
                LOG.warning("Encoding not supported: " + encoding);
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }

        // Write the preamble.
        //writer.write("");// could write ID tags [ti: name.eaf] and [by: ELAN]
        writer.write(WIN_NEWLINE);
        
        for (int i = 0; i < allUnits.size(); i++) {
            unit = (SubtitleUnit) allUnits.get(i);
            
            Long b = unit.getBegin() ;
            Long e = unit.getCalcEnd();
            if(selection == 0){
            	if( b < recalculateTimeInterval){
            		recalculateTimeInterval = b;
            		selection = 1;
            	}            	
            }

            writer.write('[');
            writer.write(toLRCTimeCode(b - recalculateTimeInterval));
            writer.write(']');

            String[] values = unit.getValues();
            for (int j = 0; j < values.length; j++) {
            	writer.write(values[j].replace('\n', ' '));

            	if (j < values.length - 1) {
            		writer.write(' ');
            	}
            }

            writer.write(WIN_NEWLINE);
            // check if there is a next unit and if there is a time gap insert an empty line
            // starting with end time
            if (i < allUnits.size() - 1) {
            	nextUnit = (SubtitleUnit) allUnits.get(i + 1);
            	if (e < nextUnit.getBegin() - 100) {// only insert empty line if gaps is > 100 ms?
                    writer.write('[');
                    writer.write(toLRCTimeCode(e - recalculateTimeInterval));
                    writer.write(']');
                    writer.write(WIN_NEWLINE);
            	}
            }
        }

        writer.close();
    }
    
    /**
     * Converts a time value to min:sec.ms format (should ms be rounded to two decimals?).
     * 
     * @param t
     * @return string representation
     */
    private String toLRCTimeCode(long t) {
    	long minutes = t / 60000;
    	String minString = twoDigits.format(minutes);
    	long seconds = (t - (60000 * minutes)) / 1000;
    	String secString = twoDigits.format(seconds);
    	long millis = (t - (60000 * minutes) - (1000 * seconds)) / 10;
    	String msString = twoDigits.format(millis);// or three digits
    	
    	return minString + ":" + secString + "." + msString;
    }
// ##############  TTML  ##########################
    /**
     * Exports to TTML .xml format file (Timed Text ML, http://www.w3.org/TR/ttaf1-dfxp/). 
     * Does support a minimum duration and tries to solve overlaps.
     * Currently performs a "poor man's" xml output, without a DocumentBuilder etc.
     * 
     * @param transcription the transcription
     * @param tierNames the tiers to include
     * @param exportFile the file to export to
     * @param encoding the file encoding, defaults to UTF-8
     * @param beginTime the (selection) begintime
     * @param endTime the (selection) end time
     * @param reCalculateTime if true make Selection begin time to start from zero 
     *        and not the actual begintime
     *
     * @throws IOException any io exception
     */
    public void exportTiersTTML(Transcription transcription, String[] tierNames,
        File exportFile, String encoding, long beginTime, long endTime,
        int minimalDuration, long offset, boolean reCalculateTime) throws IOException {
        if (exportFile == null) {
            LOG.severe("No export file specified");
            return;
        }
        
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;
        	offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        out = new FileOutputStream(exportFile);

        if (encoding != null) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException uee) {
                LOG.warning("Encoding not supported: " + encoding);
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }
        final String IND1 = "    ";
        final String IND2 = "        ";
        final String IND3 = "            ";
        // write "header"
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.write(WIN_NEWLINE);
        writer.write("<tt xmlns=\"http://www.w3.org/ns/ttml\">");
        writer.write(WIN_NEWLINE);
        writer.write(IND1 + "<head>");
        writer.write(WIN_NEWLINE);
        writer.write(IND2 + "<metadata xmlns:ttm=\"http://www.w3.org/ns/ttml#metadata\">");
        writer.write(WIN_NEWLINE);        
        writer.write(IND3 + "<ttm:title>" + transcription.getName() + "</ttm:title>");
        writer.write(WIN_NEWLINE);
        writer.write(IND2 + "</metadata>");
        writer.write(WIN_NEWLINE);
        writer.write(IND1 + "</head>");
        writer.write(WIN_NEWLINE);
        writer.write(IND1 + "<body><div>");
        writer.write(WIN_NEWLINE);
        
        // body contents
        for (int i = 0; i < allUnits.size(); i++) {
            unit = (SubtitleUnit) allUnits.get(i);

            Long b = unit.getBegin();
            Long e = unit.getCalcEnd();
            if(selection == 0){
            	if( b < recalculateTimeInterval){
            		recalculateTimeInterval = b;
            		selection = 1;
            	}            	
            }           
           
            writer.write(IND2 + "<p begin=\"");
            writer.write(TimeFormatter.toSSMSString(b - recalculateTimeInterval));
            writer.write("s\" end=\"");
            writer.write(TimeFormatter.toSSMSString(e-recalculateTimeInterval));
            writer.write("s\">");
            writer.write(WIN_NEWLINE);  
            
            for (int j = 0; j < unit.getValues().length; j++) {
            	writer.write(IND3 + unit.getValues()[j].replace('\n', ' '));

            	if (j != (unit.getValues().length - 1)) {
            		writer.write("<br/>");
            		writer.write(WIN_NEWLINE);
            	}
            }

            writer.write(WIN_NEWLINE);
            writer.write(IND2 + "</p>");
            writer.write(WIN_NEWLINE);
        }
        // closing tags
        //writer.write(WIN_NEWLINE);
        writer.write(IND1 + "</div></body>");
        writer.write(WIN_NEWLINE);
        writer.write("</tt>");
        
        writer.close();
    }
// example:
//  <tt xmlns="http://www.w3.org/ns/ttml" xml:lang="en">
//  <body>
//   <div>
//     <p begin="00:00:22" end="00:00:27">
//       I'll teach thee Bugology, Ignatzes
//     </p>
//     <p begin="00:00:40" end="00:00:43">
//       Something tells me
//     </p>
//     <p begin="00:00:58" end="00:00:64">
//       Look, Ignatz, a sleeping bee
//     </p>
//   </div>
//  </body>
// </tt>
}
