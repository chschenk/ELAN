package mpi.eudico.client.annotator.interlinear;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.List;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxEncoderInfo;


/**
 * Renders the Toolbox records to an output stream. It takes the
 * interlinearized  blocks and does some special processing to make the output
 * Toolbox compatible.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxRenderer implements ClientLogger {
    /** new line string */
    private final String NEW_LINE = "\n";

    /** white space string */
    private final String SPACE = " ";
    
    /** white space char */
    private final char SPACE_CH = ' ';
    
    /** back slash string */
    private final String B_S = "\\";
    
    /** underscore char */
    private final char U_S = '_';
    
    /**
     * Creates a new ToolboxRenderer instance
     */
    public ToolboxRenderer() {
        super();
    }

    /**
     * Renders the contents to the specified writer.
     *
     * @param writer the output writer
     * @param interlinear the interlinear object
     * @param tei additional encoding information
     *
     * @throws IOException any io exception
     * @throws NullPointerException if writer or interlinear object is null
     */
    public void renderText(Writer writer, Interlinear interlinear,
        ToolboxEncoderInfo tei) throws IOException {
        if (writer == null) {
            LOG.severe("Writer object is null");
            throw new NullPointerException("Writer object is null");
        }

        if (interlinear == null) {
            LOG.severe("The interlinear object is null");
            throw new NullPointerException("The interlinear object is null");
        }
        
        // first process the visible tiers; in the provided list of tiers the "@participant"
        // part is left out, all ref@xx, ref@yy etc have been collapsed
        // also ELANBegin etc can be part of the list
        // List visTiers = processTierList(interlinear, tei.getOrderedVisibleTiers());

        ToolboxMetrics metrics = (ToolboxMetrics) interlinear.getMetrics();
        // this also sets the visible tiers for the interlinear object
        metrics.setVisibleMarkerNames(tei.getOrderedVisibleTiers());
        metrics.setMarkersWithBlankLine(tei.getMarkersWithBlankLines());
        if (tei.isIncludeMediaMarker()) {
        	metrics.setMediaMarker(tei.getMediaMarker());
        	metrics.setMediaURL(tei.getMediaFileName());
        }
        metrics.calculateAnnotationBlocks(null);
        metrics.calculatePrintBlocks();
        
        renderBlocks(writer, interlinear, tei);
    }

    private void renderBlocks(Writer writer, Interlinear interlinear,
            ToolboxEncoderInfo tei) throws IOException {
    	ToolboxMetrics metrics = (ToolboxMetrics) interlinear.getMetrics();
    	
    	
    	// HS June 2010 the number of decimal positions changed from 3 
    	// to minimal 3 but more if the number of blocks is > 999
    	int numPos = 3;
    	int numBlocks = metrics.getToolboxBlocks().size();
    	if (numBlocks > 999) {
    		String nm = String.valueOf(numBlocks);
    		numPos = nm.length();
    	}
    	StringBuilder sb = new StringBuilder("#");
    	for (int i = 0; i < numPos; i++) {
    		sb.append("0");
    	}
    	DecimalFormat df = new DecimalFormat(sb.toString());
    	
    	if (metrics.getToolboxBlocks() != null) {
	    	List<ToolboxRecordBlock> blocks = metrics.getToolboxBlocks();
	    	ToolboxRecordBlock printBlock = null;
	    	InterlinearTier pt = null;
	    	
	    	String recMarker = null;
	    	int count = 1;
	    	//DecimalFormat df = new DecimalFormat("#000");
	    	
	    	for (int i = 0; i < blocks.size(); i++) {
	    		printBlock = blocks.get(i);
	    		if (printBlock.getPrintTiers().size() > 0) {
	    			pt = printBlock.getPrintTiers().get(0);
	    			if (pt.getTierName().equals(tei.getRecordMarker())) {
	    				renderToolboxBlock(writer, printBlock, metrics, tei, null);
	    			} else {
	    				recMarker = B_S + tei.getRecordMarker() + SPACE + df.format(count++);
	    				renderToolboxBlock(writer, printBlock, metrics, tei, recMarker);
	    			}
	    		}
	    	}
    	} else {
	    	List<InterlinearBlock> blocks = metrics.getPrintBlocks();
	    	InterlinearBlock printBlock = null;
	    	String recMarker = null;
	    	int count = 1;
	    	//DecimalFormat df = new DecimalFormat("#000");
	    	
	        for (int i = 0; i < blocks.size(); i++) {
	            printBlock = blocks.get(i);
	            
	            if (printBlock.getPrintTier(tei.getRecordMarker()) == null && 
	            		printBlock.isStartOfAnnotationBlock()) {
	            	recMarker = B_S + tei.getRecordMarker() + SPACE + df.format(count++);
	            }
	            renderBlock(writer, printBlock, metrics, tei, recMarker);
	        }
    	}
    }

    /**
     * Renders the Toolbox record style wrapped block of tiers.
     * @param writer the writer
     * @param block the Toolbox record block
     * @param metrics the metrics object
     * @param tei the encoder info object
     * @param recMarker the record mareker to add, or null
     * @throws IOException
     */
    private void renderToolboxBlock (Writer writer, ToolboxRecordBlock block, ToolboxMetrics metrics,
            ToolboxEncoderInfo tei, String recMarker) throws IOException {
        List<InterlinearTier> tiers = null;
        InterlinearTier tier = null;
        InterlinearTier pt = null;
        
        if (recMarker != null) {
        	writer.write(recMarker);
        	writer.write(NEW_LINE);
        	if (metrics.isMarkerWithBlankLine(tei.getRecordMarker())) {
        		writer.write(NEW_LINE);
        	}
        }
        tiers = block.getPrintTiers();
        
        for (int i = 0; i < tiers.size(); i++) {
        	tier = tiers.get(i);
        	
        	if (tier instanceof EmptyPrintTier) {
        		writer.write(NEW_LINE);
        		continue;
        	}
        	pt = tier;
        	// render tier
        	if (pt.getAnnotations().size() == 0 && (!tei.isIncludeEmptyMarkers())) {
        		continue;
        	}
        	renderTier(writer, pt);
        	if (metrics.isMarkerWithBlankLine(pt.getTierName())) {
        		// if the next line is already an empty line don't add a blank line
        		if (i < tiers.size() - 1) {
        			if (!(tiers.get(i + 1) instanceof EmptyPrintTier)) {
        				writer.write(NEW_LINE);
        			} 				
        		} else {
        			writer.write(NEW_LINE); // ?? do this
        		}
        	}
        }

        // end block with newline
        writer.write(NEW_LINE);
    }
    
    /**
     * Renders a tier as is. It is assumed that block- and line wrapping has been applied.
     * If a tier has with multiple lines, they are all written to the output.
     * 
     * @param writer the writer
     * @param pt the tier
     * @throws IOException any io exception
     */
    private void renderTier(Writer writer, InterlinearTier pt) throws IOException {
        List<InterlinearAnnotation> annos = pt.getAnnotations();
        InterlinearAnnotation pa = null;
        InterlinearAnnotation prevPa = null;
        
        writer.write(B_S + pt.getTierName().replace(SPACE_CH, U_S) + SPACE);
    	for (int i = 0; i < annos.size(); i++) {
            pa = annos.get(i);

            if (pa.nrOfLines == 1) {
                int pad = 0;

                if (prevPa != null) {
                    pad = pa.x - (prevPa.x + prevPa.realWidth);
                } else {
                    pad = pa.x;
                }

                padSpaces(writer, pad);
                writer.write(pa.getValue());
            } else {
                for (int line = 0; line < pa.getLines().length; line++) {
                    if (line == 0) {
                        writer.write(pa.getLines()[line]); //rest of line is empty

                        if (line != (pa.getLines().length - 1)) {
                            writer.write(NEW_LINE);
                        }
                    } else {                            
                        // don't fill the label margin, or repeat the label
                        //padSpaces(writer, pt.getTierName().length() + 2);
                        writer.write(pa.getLines()[line]);

                        if (line != (pa.getLines().length - 1)) {
                            writer.write(NEW_LINE);
                        }
                    }
                }
            }

            prevPa = pa;
        }
        // end the tier with a new line char
        writer.write(NEW_LINE);
    }
    
    private void renderBlock(Writer writer, InterlinearBlock block, ToolboxMetrics metrics,
            ToolboxEncoderInfo tei, String recMarker) throws IOException {
        List<InterlinearTier> tiers = null;
        InterlinearTier pt = null;
        
        if (recMarker != null && block.isStartOfAnnotationBlock()) {
        	writer.write(recMarker);
        	writer.write(NEW_LINE);
        	if (metrics.isMarkerWithBlankLine(tei.getRecordMarker())) {
        		writer.write(NEW_LINE);
        	}
        }
        
        tiers = block.getPrintTiers();
        
    	// if lines have to be wrapped to the end of block, multiple iterations are needed
        int numIterstions = 1;
        boolean wrapEndOfBlock = false;

        for (int j = 0; j < numIterstions; j++) {
            for (int i = 0; i < tiers.size(); i++) {
            	pt = tiers.get(i);
            	// render tiers
            	if (pt.getAnnotations().size() == 0 && (!tei.isIncludeEmptyMarkers() || 
            			!block.isStartOfAnnotationBlock())) {
            		continue;
            	}
            	
            	renderTier(writer, pt, j, wrapEndOfBlock);
            	if (metrics.isMarkerWithBlankLine(pt.getTierName())) {
            		writer.write(NEW_LINE);
            	}
            }
        }
        // end block with newline
        writer.write(NEW_LINE);
    }
    
    /**
     * Renders a tier. If linewrapping should be applied all lines are written at once if 
     * the lines should be wrapped to the next line. Otherwise the lines will be wrapped
     * to the end of the block through multiple calls to renderTier.
     * 
     * @param writer the writer object
     * @param pt the ptint tier
     * @param lineIndex zero based line index (applicable to tiers with multiple lines)
     * ignored when wrapEndOfBlock is false
     * @param wrapEndOfBlock flag indicating the style of line wrapping
     * @throws IOException io exception
     */
    private void renderTier(Writer writer, InterlinearTier pt, int lineIndex,
    		boolean wrapEndOfBlock) throws IOException {
        // annotations
        List<InterlinearAnnotation> annos = pt.getAnnotations();
        InterlinearAnnotation pa = null;
        InterlinearAnnotation prevPa = null;
        
        if (!wrapEndOfBlock) {
        	writer.write(B_S + pt.getTierName().replace(SPACE_CH, U_S) + SPACE);
        	for (int i = 0; i < annos.size(); i++) {
                pa = annos.get(i);

                if (pa.nrOfLines == 1) {
                    int pad = 0;

                    if (prevPa != null) {
                        pad = pa.x - (prevPa.x + prevPa.realWidth);
                    } else {
                        pad = pa.x;
                    }

                    padSpaces(writer, pad);
                    writer.write(pa.getValue());
                } else {
                    for (int line = 0; line < pa.getLines().length; line++) {
                        if (line == 0) {
                            writer.write(pa.getLines()[line]); //rest of line is empty

                            if (line != (pa.getLines().length - 1)) {
                                writer.write(NEW_LINE);
                            }
                        } else {                            
                            // don't fill the label margin or repeat the label
                            // padSpaces(writer, pt.getTierName().length() + 2);
                            writer.write(pa.getLines()[line]);

                            if (line != (pa.getLines().length - 1)) {
                                writer.write(NEW_LINE);
                            }
                        }
                    }
                }

                prevPa = pa;
            }
        } else {
        	if (lineIndex >= pt.getNumLines()) {
        		return;
        	}
        	writer.write(B_S + pt.getTierName().replace(SPACE_CH, U_S) + SPACE);
        	for (int i = 0; i < annos.size(); i++) {
                pa = annos.get(i);
                
                if (pa.nrOfLines == 1 && lineIndex == 0) {
                    int pad = 0;

                    if (prevPa != null) {
                        pad = pa.x - (prevPa.x + prevPa.realWidth);
                    } else {
                        pad = pa.x;
                    }

                    padSpaces(writer, pad);
                    writer.write(pa.getValue());
                } else if (pa.nrOfLines > lineIndex) {
                	writer.write(pa.getLines()[lineIndex]);
                }
                
                prevPa = pa;
        	}      	
        }
        // end the tier with a new line char
        writer.write(NEW_LINE);
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
