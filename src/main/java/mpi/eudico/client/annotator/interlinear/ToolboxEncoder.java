package mpi.eudico.client.annotator.interlinear;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxEncoderInfo;


/**
 * Alternative for the
 * mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxEncoder. This class
 * provides support for more user options, exports utf-8 only and uses classes
 * in the mpi.eudico.client.annotator.interlinear package rather than classes
 * in mpi.eudico.server.corpora.clomimpl.shoebox.interlinear.  For that reason
 * this encoder class is part of the eudico.client packages rather  then in
 * the eudico.server packages.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxEncoder implements AnnotationDocEncoder, ClientLogger {
    final private String NEWLINE = "\n";
    private final String EMPTY = "";

    /** white space string */
    private final String SPACE = " ";
    private final String defaultDBType = "ElanExport";
    private final String elanMediaURLLabel = "\\ELANMediaURL";
    private final String elanMediaExtractedLabel = "\\ELANMediaExtracted";
    private final String elanMediaMIMELabel = "\\ELANMediaMIME";
    private final String elanMediaOriginLabel = "\\ELANMediaOrigin";

    /**
     * @see mpi.eudico.server.corpora.clom.AnnotationDocEncoder#encodeAndSave(mpi.eudico.server.corpora.clom.Transcription,
     *      mpi.eudico.server.corpora.clom.EncoderInfo, java.util.List,
     *      java.lang.String)
     */
    @Override
    public void encodeAndSave(Transcription transcription,
        EncoderInfo encoderInfo, List<TierImpl> tierOrder, String path)
        throws IOException {
        if (transcription == null) {
        	LOG.severe("Transcription object is null");
            throw new NullPointerException("Transcription object is null");
        }

        if (path == null) {
        	LOG.severe("Export file is null");
            throw new NullPointerException("Export file is null");
        }

        // create and initialize an Interlinearizer
        Interlinear interlinear = new Interlinear((TranscriptionImpl) transcription,
                Interlinear.SHOEBOX_TEXT);
        ToolboxEncoderInfo tei = (ToolboxEncoderInfo) encoderInfo;

        if (encoderInfo != null) {
        	// Integer.MAX_VALUE means no wrapping within a block
            interlinear.setWidth(tei.getPageWidth());
            interlinear.setBlockWrapStyle(Interlinear.EACH_BLOCK);

            if (tei.isWrapLines()) {
                interlinear.setLineWrapStyle(tei.getLineWrapStyle());
            } else {
                interlinear.setLineWrapStyle(Interlinear.NO_WRAP);
            }

            if (tei.isIncludeEmptyMarkers()) {
                interlinear.setEmptyLineStyle(Interlinear.TEMPLATE);
            } else {
            	if (interlinear.getLineWrapStyle() == Interlinear.END_OF_BLOCK || 
            			interlinear.getWidth() != Integer.MAX_VALUE) {
            		// needed to be able to transfer lines to last block
            		// and needed to be able to keep wrapped interlinear blocks 
            		// together; maybe there should be a flag for this?
            		interlinear.setEmptyLineStyle(Interlinear.TEMPLATE);
            	}
            }

            interlinear.setBlockSpacing(1);
            interlinear.setTierLabelsShown(true);
            interlinear.setTimeCodeShown(false);
            interlinear.setPlaySoundSel(false);
            interlinear.setShowSilenceDuration(false);
            interlinear.setTimeCodeType(tei.getTimeFormat());
            interlinear.setTimeOffset(tei.getTimeOffset());
        }

        // create a writer
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

            writer = new BufferedWriter(osw);
            // write header
            writeHeader(writer, tei.getDatabaseType());
            
            ToolboxRenderer renderer = new ToolboxRenderer();
            renderer.renderText(writer, interlinear, tei);
            
            // media descriptors are written at the end of the Toolbox file. 
            // When written at begin/in header Toolbox throws them away without 
            // any notification.
            writeMediaDescriptors(writer, transcription);
        } finally {
        	if (writer != null) {
        		writer.close();
        	}
        }

        if (writer != null) {
        	writer.close();
        }
    }

    /**
     * Writes the Shoebox/Toolbox header lines
     *
     * @param writer the writer object
     * @param dbType the reference to a Toolbox database type (in a .typ file)
     *
     * @throws IOException
     */
    private void writeHeader(Writer writer, String dbType)
        throws IOException {
        if (dbType != null) {
            writer.write("\\_sh v3.0  400  " + dbType + NEWLINE);
        } else {
            writer.write("\\_sh v3.0  400  " + defaultDBType + NEWLINE);
        }

        writer.write("\\_DateStampHasFourDigitYear" + NEWLINE);
        writer.write(NEWLINE + NEWLINE);
    }

    /**
     * Writes the media descriptors.
     *
     * @param writer the writer
     * @param transcription the transcription
     *
     * @throws IOException io exception
     */
    private void writeMediaDescriptors(Writer writer,
        Transcription transcription) throws IOException {
        List mds = transcription.getMediaDescriptors();

        if ((mds != null) && (mds.size() > 0)) {
            writer.write(NEWLINE);

            for (int i = 0; i < mds.size(); i++) {
                MediaDescriptor md = (MediaDescriptor) mds.get(i);

                if ((md.mediaURL != null) && !md.mediaURL.equals(EMPTY)) {
                    writer.write((elanMediaURLLabel + SPACE + md.mediaURL +
                        NEWLINE));
                }

                if ((md.mimeType != null) && !md.mimeType.equals(EMPTY)) {
                    writer.write((elanMediaMIMELabel + SPACE + md.mimeType +
                        NEWLINE));
                }

                if (md.timeOrigin != 0) {
                    writer.write((elanMediaOriginLabel + SPACE + md.timeOrigin +
                        NEWLINE));
                }

                if ((md.extractedFrom != null) &&
                        !md.extractedFrom.equals(EMPTY)) {
                    writer.write((elanMediaExtractedLabel + SPACE +
                        md.extractedFrom + NEWLINE));
                }
            }
        }
    }
}
