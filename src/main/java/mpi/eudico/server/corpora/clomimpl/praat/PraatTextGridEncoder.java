package mpi.eudico.server.corpora.clomimpl.praat;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.util.ServerLogger;


/**
 * An encoder class that encodes tiers and annotations as Praat TextGrid
 * Interval Tiers.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class PraatTextGridEncoder implements AnnotationDocEncoder, ServerLogger {
    private final String indent = "    ";
    private final String indent2 = "        ";
    private final String indent3 = "            ";
    private final String xmin = "xmin = ";
    private final String xmax = "xmax = ";
    private final String intervals = "intervals [";
    private final String tx = "text = ";
    private final String NEW_LINE = "\n";
    private String encoding;

    /**
     * Constructor.
     */
    public PraatTextGridEncoder() {
        super();
    }

    /**
     * @see mpi.eudico.server.corpora.clom.AnnotationDocEncoder#encodeAndSave(mpi.eudico.server.corpora.clom.Transcription,
     *      mpi.eudico.server.corpora.clom.EncoderInfo, java.util.List,
     *      java.lang.String)
     */
    @Override
	public void encodeAndSave(Transcription theTranscription,
        EncoderInfo theEncoderInfo, List<TierImpl> tierOrder, String path)
        throws IOException {
        // if the transcription or tier vector or path is null throw exception
        if (theTranscription == null) {
            LOG.warning("The transcription is null");
            throw new IllegalArgumentException("The transcription is null.");
        }

        if ((tierOrder == null) || (tierOrder.size() == 0)) {
            LOG.warning("No tiers have been specified for export");
            throw new IllegalArgumentException("No tiers specified for export");
        }

        if (path == null) {
            LOG.warning("No file path for the TextGrid file has been specified");
            throw new IllegalArgumentException(
                "No file path for the TextGrid file has been specified");
        }

        long bt = 0;
        long et = 0;
        long offset = 0;

        if (theEncoderInfo instanceof PraatTGEncoderInfo) {
            bt = ((PraatTGEncoderInfo) theEncoderInfo).getBeginTime();
            et = ((PraatTGEncoderInfo) theEncoderInfo).getEndTime();
            encoding = ((PraatTGEncoderInfo) theEncoderInfo).getEncoding();
            boolean exportSelection = ((PraatTGEncoderInfo) theEncoderInfo).isExportSelection();
            offset = ((PraatTGEncoderInfo) theEncoderInfo).getOffset();
            if (offset > 0) {
            	if (exportSelection) {
            		bt+= offset;
            	}
            	et+= offset;
            }
        }

        //try {
        OutputStreamWriter out = null;
        try {
        	if (encoding != null) {
                out = new OutputStreamWriter(new FileOutputStream(
                        path), encoding);
        	} else {
                out = new OutputStreamWriter(new FileOutputStream(
                        path));
        	}
        } catch (UnsupportedEncodingException uee) {
        	LOG.warning("Encoding not supported: " + uee.getMessage());
            out = new OutputStreamWriter(new FileOutputStream(
                    path));
        }
        
        BufferedWriter writer = new BufferedWriter(out);
        writer.write("File type = \"ooTextFile\"");
        writer.write(NEW_LINE);
        writer.write("Object class = \"TextGrid\"");
        writer.write(NEW_LINE + NEW_LINE);
        writer.write(xmin + (bt / 1000d));
        writer.write(NEW_LINE);
        writer.write(xmax + (et / 1000d));
        writer.write(NEW_LINE);
        writer.write("tiers? <exists>");
        writer.write(NEW_LINE);
        writer.write("size = " + tierOrder.size());
        writer.write(NEW_LINE);
        writer.write("item []:");
        writer.write(NEW_LINE);
        // System.out.println("Enc: " + out.getEncoding());
        writeTiers(theTranscription, tierOrder, bt, et, offset, writer);
        writer.flush();
        writer.close();

        //} catch (IOException  ex) {
        //throw new Exception(ex);
        //  LOG.severe("IO Exception occurred: " + ex.getMessage());
        //ex.printStackTrace();
        //} 
    }

    private void writeTiers(Transcription theTranscription, List<TierImpl> tiers,
        long bt, long et, long offset, Writer out) throws IOException {
        if ((tiers == null) || (tiers.size() == 0)) {
            return;
        }

        List<AnnotationCoreImpl> annotations = new ArrayList<AnnotationCoreImpl>();

        for (int i = 0; i < tiers.size(); i++) {
        	Tier tier = tiers.get(i);
            out.write(indent);
            out.write("item[" + (i + 1) + "]:");
            out.write(NEW_LINE);
            out.write(indent2);
            out.write("class = \"IntervalTier\"");
            out.write(NEW_LINE);
            out.write(indent2);
            out.write("name = \"" + tier.getName() + "\"");
            out.write(NEW_LINE);
            out.write(indent2);
            out.write(xmin + (bt / 1000d));
            out.write(NEW_LINE);
            out.write(indent2);
            out.write(xmax + (et / 1000d));
            out.write(NEW_LINE);
            // fill the list of annotation objects, while replacing gaps with new empty annotations.
            // this is necessary in order to be able to calculate and export the number of intervals
            annotations.clear();
            Annotation ann1 = null;
            Annotation ann2 = null;
            
            List<? extends Annotation> anns = tier.getAnnotations();

            for (int j = 0; j < anns.size(); j++) {
                ann2 = anns.get(j);

                if (ann2.getEndTimeBoundary() + offset <= bt) {
                    ann1 = ann2;

                    continue;
                }

                // check begintime
                if (ann2.getBeginTimeBoundary() + offset <= bt) {
                    annotations.add(new AnnotationCoreImpl(
                            ann2.getValue(),
                            Math.max(bt, ann2.getBeginTimeBoundary() + offset),
                            Math.min(et, ann2.getEndTimeBoundary() + offset)));

                    if (ann2.getEndTimeBoundary() + offset >= et) {
                        break;
                    }
                } else { //ann2 begin > bt

                    if (ann1 != null) {
                        // fill gap if any
                        if (ann1.getEndTimeBoundary() < ann2.getBeginTimeBoundary()) {
                            annotations.add(new AnnotationCoreImpl("",
                                    Math.max(bt, ann1.getEndTimeBoundary() + offset),
                                    Math.min(et, ann2.getBeginTimeBoundary() + offset)));
                        }
                    } else { // first annotation begins > bt, fill gap
                        annotations.add(new AnnotationCoreImpl("", bt,
                                Math.min(et, ann2.getBeginTimeBoundary() + offset)));
                    }

                    if (ann2.getBeginTimeBoundary() + offset >= et) {
                        break;
                    } else {
                        // add ann 2
                        annotations.add(new AnnotationCoreImpl(
                                ann2.getValue(), ann2.getBeginTimeBoundary() + offset,
                                Math.min(et, ann2.getEndTimeBoundary() + offset)));

                        if (ann2.getEndTimeBoundary() + offset >= et) {
                            break;
                        }
                    }
                }

                ann1 = ann2;
            }
            // fill with an empty interval after the last real interval if the last interval
            // does not end at the media end, create an empty interval from bt to et if ther
            // are no intervals at all
            if (annotations.size() > 0) {
            	AnnotationCoreImpl record = annotations.get(annotations.size() - 1);
            	if (record.getEndTimeBoundary() < et) {
            		annotations.add(new AnnotationCoreImpl("", record.getEndTimeBoundary(), et));
            	}
            } else {
            	annotations.add(new AnnotationCoreImpl("", bt, et));
            }
            // the intervals list is filled, the size is known now
            out.write(indent2);
            out.write("intervals: size = " + annotations.size());
            out.write(NEW_LINE);

            AnnotationCoreImpl record = null;
            String value = null;

            for (int j = 0; j < annotations.size(); j++) {
                record = annotations.get(j);
                value = record.getValue().replaceAll("\"", "\"\"");
                out.write(indent2);
                out.write(intervals + (j + 1) + "]");
                out.write(NEW_LINE);
                out.write(indent3);
                out.write(xmin);
                out.write(String.valueOf(record.getBeginTimeBoundary() / 1000d));
                out.write(NEW_LINE);
                out.write(indent3);
                out.write(xmax);
                out.write(String.valueOf(record.getEndTimeBoundary() / 1000d));
                out.write(NEW_LINE);
                out.write(indent3);
                out.write(tx);
                out.write("\"" + value + "\"");
                out.write(NEW_LINE);
            }

        }
    }
}
