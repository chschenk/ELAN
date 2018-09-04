package mpi.eudico.client.annotator.interlinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A class that renders interlinearized content as characters to a File, using
 * UTF-8 encoding.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class CharacterRenderer {
    /** new line string */
    private final String NEW_LINE = "\n";

    /** white space string */
    private final String SPACE = " ";
    /** a tab string */
    private final String TAB = "\t";
    private Interlinear interlinear;
    private File outFile;
	/** the character encoding for the text file */
	private String charEncoding;

    /**
     * Creates a new CharacterRenderer instance
     *
     * @param interlinear the Interlinear object holding the formatted,
     *        interlinearized content
     * @param outFile the File to write to
     */
    public CharacterRenderer(Interlinear interlinear, File outFile) {
    	this(interlinear, outFile, "UTF-8");
    }
    
	/**
	 * Creates a new CharacterRenderer instance
	 *
	 * @param interlinear the Interlinear object holding the formatted,
	 *        interlinearized content
	 * @param outFile the File to write to
	 * @param charEncoding the character encoding for output
	 */
	public CharacterRenderer(Interlinear interlinear, File outFile, 
		String charEncoding) {
		this.interlinear = interlinear;
		this.outFile = outFile;
		this.charEncoding = charEncoding;
	}

    /**
     * Renders (writes) the content to a File, adding spaces and new line
     * characters  as needed.
     *
     * @throws IOException any IOException that can occur while writing to a
     *         file
     * @throws FileNotFoundException thrown when the export file could not be
     *         found
     * @throws NullPointerException when the Interlinear object or the export
     *         file  is <code>null</code>
     */
    public void renderText() throws IOException, FileNotFoundException {
        if (interlinear == null) {
            throw new NullPointerException("Interlinear object is null");
        }

        if (outFile == null) {
            throw new NullPointerException("Export file is null");
        }

        // create output stream
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

            // file info
            writer.write(interlinear.getTranscription().getFullPath());
            writer.write(NEW_LINE);

            writer.write(DateFormat.getDateTimeInstance(DateFormat.FULL,
                    DateFormat.SHORT, Locale.getDefault()).format(new Date(
                        System.currentTimeMillis())));

            writer.write(NEW_LINE);
            writer.write(NEW_LINE);

            // annotations
            List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
            InterlinearBlock printBlock = null;
            List<InterlinearTier> tiers = null;
            InterlinearTier pt = null;

            for (int i = 0; i < blocks.size(); i++) {
                printBlock = blocks.get(i);
                tiers = printBlock.getPrintTiers();

                for (int j = 0; j < tiers.size(); j++) {
                    pt = tiers.get(j);
                    renderTier(interlinear, pt, writer);
                }

                for (int j = 0; j < interlinear.getBlockSpacing(); j++) {
                    writer.write(NEW_LINE);
                }
            }

            writer.flush();
            writer.close();
        } finally {
        	if (writer != null) {
		        try {
		            writer.close();
		        } catch (Exception ee) {
		        }
        	}
        }
    }

    /**
     * Writes the contents of the annotations of a single tier to the file.
     *
     * @param inter the Interlinear object
     * @param pt the tier to write
     * @param writer the buffered writer
     *
     * @throws IOException any ioexception
     */
    private void renderTier(Interlinear inter, InterlinearTier pt, Writer writer)
        throws IOException {
        // tier label
        if (inter.isTierLabelsShown() || inter.isShowSilenceDuration()) {
        	String label;
            if (pt.isTimeCode()) {
            	label = inter.getMetrics().TC_TIER_NAME;
            } else if (pt.isSilDuration()) {
            	label = inter.getMetrics().SD_TIER_NAME;
            } else {
            	label = pt.getTierName();
            }
            writer.write(label);
            padSpacesAndOrTab(writer, inter, inter.getMetrics().getLeftMargin() - label.length());
        }


        // annotations
        List<InterlinearAnnotation> annos = pt.getAnnotations();
        InterlinearAnnotation prevPa = null;

        for (int i = 0; i < annos.size(); i++) {
        	InterlinearAnnotation pa = annos.get(i);

            if (prevPa != null) {
            	// For the second and subsequent annotations on a line,
            	// print the separation with the previous one.
                int pad = pa.x - (prevPa.x + prevPa.realWidth);
                padSpacesAndOrTab(writer, inter, pad);
            }

            if (pa.nrOfLines == 1) {
                writer.write(pa.getValue());
            } else {
                final int numLines = pa.getLines().length;
				for (int line = 0; line < numLines; line++) {
                    if (line == 0) {
                        writer.write(pa.getLines()[line]); //rest of line is empty

                        if (line != (numLines - 1)) {
                            writer.write(NEW_LINE);
                        }
                    } else {
                        if (inter.isTierLabelsShown()) {
                            // fill the label margin
                            padSpacesAndOrTab(writer, inter, inter.getMetrics().getLeftMargin());
                        }

                        writer.write(pa.getLines()[line]);

                        if (line != (numLines - 1)) {
                            writer.write(NEW_LINE);
                        }
                    }
                }
            }

            prevPa = pa;
        }

        // end with a new line
        writer.write(NEW_LINE);
    }

	/**
	 * Do padding with spaces and/or a tab.
	 * <p>
	 * There are two checkboxes: isInsertTabs() which adds tabs to the normal spaces,
	 * and isTabsReplaceSpaces() which may additionally be set to omit the spaces.
	 * The second is greyed out if the first is not enabled.
	 * 
	 * @param writer
	 * @param inter
	 * @param pad
	 * @throws IOException
	 */
	private void padSpacesAndOrTab(Writer writer, Interlinear inter, int pad)
			throws IOException {
		if (!inter.isInsertTabs() || (inter.isInsertTabs() && !inter.isTabsReplaceSpaces())) {
			padSpaces(writer, pad);
		}
		if (inter.isInsertTabs()) {
		    writer.write(TAB);
		}
	}

    /**
     * Writes the specified number of whitespace characters to the file in
     * order to fill up the space to the next annotation
     *
     * @param writer the buffered writer
     * @param numSpaces the number of whitespace characters to write
     *
     * @throws IOException any IOEception
     */
    private void padSpaces(Writer writer, int numSpaces)
        throws IOException {
        if (numSpaces <= 0) {
            return;
        }

        for (int i = 0; i < numSpaces; i++) {
            writer.write(SPACE);
        }
    }
}
