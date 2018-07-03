package mpi.eudico.client.annotator.interlinear;


import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;
import mpi.eudico.util.TimeRelation;


/**
 * The BlockMetrics class contains fields and methods for storing and
 * calculating  blocks of annotations in an interlinear way.
 *
 * @author Han Sloetjes
 */
public class BlockMetrics {
    TranscriptionImpl transcription;

    /**
     * the interlinear object holding all parameters and a reference to  the
     * Transcription object containing the data
     */
    final Interlinear interlinearizer;

    /** constant for a timecode tier label */
    public final String TC_TIER_NAME = "TC";

    /** constant for a timecode separator */
    public final String TIME_SEP = " - ";
    
    /** constant for a silenceDuration tier label */
    public final String SD_TIER_NAME = "SD";
    
    /**   */
    public final String SD_SEP1 = "(";
    
    /** */
    public final String SD_SEP2 = ")";    

    /** constant for a space character */
    public final char SPACE_CHAR = ' ';
    private Map<String, Integer> tierHeights;
    private Map<String, String> timecodeLabels;
    private Map<String, String> silDurationLabels;
    List<DefaultMutableTreeNode> annotationBlocks;
    List<InterlinearBlock> printBlocks;
    private ArrayList<int[]> pageBreaks;
    List<Tier> visibleTiers;
    List<String> tierTemplate;
    private int leftMargin;
    private DefaultMutableTreeNode tierTree;

    /**
     * Creates a new BlockMetrics instance
     *
     * @param interlinearizer the Interlinear object holding parameters and
     *        data
     */
    public BlockMetrics(Interlinear interlinearizer) {
        this.interlinearizer = interlinearizer;
        transcription = interlinearizer.getTranscription();
        tierHeights = new HashMap<String, Integer>();
        timecodeLabels = new HashMap<String, String>();
        silDurationLabels = new HashMap<String, String>();
        annotationBlocks = new ArrayList<DefaultMutableTreeNode>();
        tierTemplate = new ArrayList<String>();
        printBlocks = new ArrayList<InterlinearBlock>();
        pageBreaks = new ArrayList<int[]>();
        tierTree = new DefaultMutableTreeNode();

        //visibleTiers = interlinearizer.getVisibleTiers();
    }

    /**
     * Reset the previously calculated blocks and pages etc.
     */
    public void reset() {
        tierHeights.clear();
        timecodeLabels.clear();
        annotationBlocks.clear();
        tierTemplate.clear();
        visibleTiers = interlinearizer.getVisibleTiers();
        tierTree = new DefaultMutableTreeNode();
    }

    /**
     * Calculate annotation blocks, tier heights and margin using the specified
     * Graphics  object for measurements.
     *
     * @param g the <code>Graphics</code> object for measurements
     */
    public void calculateAnnotationBlocks(Graphics g) {
        //calculateTierHeightsAndMargin(g);
        AnnotationBlockCreator creator = new AnnotationBlockCreator();
        tierTree = creator.createTierTree(interlinearizer.getTranscription(),
                visibleTiers);

        boolean pixelBased = (interlinearizer.getAlignmentUnit() == Interlinear.PIXELS);
        int maxTierLabelWidth = 0;
        Font font = null;
        FontMetrics fontMetrics = null;

        // create a list of relevant top level tiers, meaning all top level 
        // tiers that is either in the visible tiers list itself or has a 
        // child tier in the visible tiers list
        ArrayList<TierImpl> relevantTopTiers = new ArrayList<TierImpl>();
        TierImpl tier;
        TierImpl rootTier;

        for (int i = 0; i < visibleTiers.size(); i++) {
            tier = (TierImpl) visibleTiers.get(i);

            String name = tier.getName();
            tierTemplate.add(name);

            rootTier = tier.getRootTier();

            if (!relevantTopTiers.contains(rootTier)) {
                relevantTopTiers.add(rootTier);
                
                if(interlinearizer.isShowSilenceDuration()){
                	String sdLabel = SD_TIER_NAME;
                	sdLabel = createSDLabel(rootTier.getName());
                	
                	if (sdLabel != null) {
                        silDurationLabels.put(rootTier.getName(), sdLabel);
                        
                        if (pixelBased) {
                            interlinearizer.setFontSize(sdLabel,
                                Interlinear.TIMECODE_FONT_SIZE);
                            interlinearizer.setFont(sdLabel,
                                Interlinear.DEFAULTFONT);

                            font = interlinearizer.getFont(sdLabel);
                            fontMetrics = g.getFontMetrics(font);

                            int tierHeight = fontMetrics.getHeight();
                            setTierHeight(sdLabel, tierHeight);

                            int labWidth = fontMetrics.stringWidth(SD_TIER_NAME);

                            if (labWidth > maxTierLabelWidth) {
                                maxTierLabelWidth = labWidth;
                            }
                        } else {
                            // the timecode label is 2 chars
                            if (maxTierLabelWidth < 2) {
                                maxTierLabelWidth = 2;
                            }
                        }
                	}
                }

                //if (interlinearizer.isTimeCodeShown()) {
                // CC 29/11/2010
                if (interlinearizer.isTimeCodeShown() || interlinearizer.isPlaySoundSel()) {
                    String tcLabel = TC_TIER_NAME;

                    if (interlinearizer.getTimeCodeMultiplicity() == Interlinear.SINGLE_TIMECODE) {
                        tcLabel = createTCLabel("");
                    } else {
                        tcLabel = createTCLabel(rootTier.getName());
                    }

                    if (tcLabel != null) {
                        timecodeLabels.put(rootTier.getName(), tcLabel);

                        if (pixelBased) {
                            interlinearizer.setFontSize(tcLabel,
                                Interlinear.TIMECODE_FONT_SIZE);
                            interlinearizer.setFont(tcLabel,
                                Interlinear.DEFAULTFONT);

                            font = interlinearizer.getFont(tcLabel);
                            fontMetrics = g.getFontMetrics(font);

                            int tierHeight = fontMetrics.getHeight();
                            setTierHeight(tcLabel, tierHeight);

                            int labWidth = fontMetrics.stringWidth(TC_TIER_NAME);

                            if (labWidth > maxTierLabelWidth) {
                                maxTierLabelWidth = labWidth;
                            }
                        } else {
                            // the timecode label is 2 chars
                            if (maxTierLabelWidth < 2) {
                                maxTierLabelWidth = 2;
                            }
                        }
                    }
                }
            }

            if (pixelBased) {
                font = interlinearizer.getFont(name);
                fontMetrics = g.getFontMetrics(font);

                int tierHeight = fontMetrics.getHeight();
                setTierHeight(name, tierHeight);

                int labWidth = fontMetrics.stringWidth(name);

                if (labWidth > maxTierLabelWidth) {
                    maxTierLabelWidth = labWidth;
                }
            } else {
                setTierHeight(name, Interlinear.DEFAULT_FONT_SIZE);

                int labWidth = name.length();

                if (labWidth > maxTierLabelWidth) {
                    maxTierLabelWidth = labWidth;
                }
            }
        }

        if (pixelBased) {
            setLeftMargin(maxTierLabelWidth + interlinearizer.getEmptySpace());
        } else {
            setLeftMargin(maxTierLabelWidth + Interlinear.LABEL_VALUE_MARGIN);
        }

        // create blocks, using the annotations on relevant top level tiers 
        // as the root
        tier = null;
        rootTier = null;

        DefaultMutableTreeNode node = null;
        InterlinearAnnotation prann = null;

        //AnnotationBlockCreator creator = new AnnotationBlockCreator();
        long selBT = 0;
        long selET = Long.MAX_VALUE;

        if (interlinearizer.isSelectionOnly() &&
                (interlinearizer.getSelection() != null) &&
                (interlinearizer.getSelection()[0] != interlinearizer.getSelection()[1])) {
            selBT = interlinearizer.getSelection()[0];
            selET = interlinearizer.getSelection()[1];
        }

        for (int i = 0; i < relevantTopTiers.size(); i++) {
            rootTier = relevantTopTiers.get(i);

            List<AbstractAnnotation> annots = rootTier.getAnnotations();

            Iterator<AbstractAnnotation> anIter = annots.iterator();
            AbstractAnnotation ann;

            while (anIter.hasNext()) {
                ann = anIter.next();

                // check selection 
                if (TimeRelation.overlaps(ann, selBT, selET)) {
                    if (interlinearizer.isEmptySlotsShown()) {
                        node = creator.createBlockFillEmptyPositions(ann, visibleTiers);
                    } else {
                        node = creator.createBlockForAnnotation(ann, visibleTiers);    
                    }
                    

                    if (node != null) {
                        // add a TimeCode annotation, if applicable
                        //if (interlinearizer.isTimeCodeShown()) {
                    	//CC 29/11/2010 
                    	if (interlinearizer.isTimeCodeShown() || 
                    			(interlinearizer.isPlaySoundSel() && interlinearizer.getOutputMode() == Interlinear.HTML)) { 
                            prann = (InterlinearAnnotation) node.getUserObject();

                            if (prann != null) {
                                StringBuilder timeString = new StringBuilder();

                                if (interlinearizer.getTimeCodeType() == Interlinear.HHMMSSMS) {
                                    timeString.append(TimeFormatter.toString(
                                            prann.bt));
                                    timeString.append(TIME_SEP);
                                    timeString.append(TimeFormatter.toString(
                                            prann.et));
                                } else if (interlinearizer.getTimeCodeType() == Interlinear.SSMS) {
                                    timeString.append(TimeFormatter.toSSMSString(
                                            prann.bt));
                                    timeString.append(TIME_SEP);
                                    timeString.append(TimeFormatter.toSSMSString(
                                            prann.et));
                                } else {
                                    timeString.append(prann.bt);
                                    timeString.append(TIME_SEP);
                                    timeString.append(prann.et);
                                }

                                String tcLabel = timecodeLabels.get(rootTier.getName());
                                InterlinearAnnotation tcAnn = new InterlinearAnnotation(timeString.toString(),
                                        tcLabel);
                                node.add(new DefaultMutableTreeNode(tcAnn));
                            }
                        }

                        annotationBlocks.add(node);
                        calculateBlock(node, g);

                        // printoutNode(node);
                        // sort the printannotations
                    }
                }
            }
        }

        Collections.sort(annotationBlocks, new AnnotationNodeComparator());

        
        if(interlinearizer.isShowSilenceDuration()){        	
        	DefaultMutableTreeNode n =null;
        	DefaultMutableTreeNode nextN = null;
        	
            long dur;
            
        	for (int i = 0; i < annotationBlocks.size(); i++) {
        		nextN = annotationBlocks.get(i); 
        		InterlinearAnnotation nextAnn =(InterlinearAnnotation) nextN.getUserObject();        		         
        		if(n != null){
        			// add a TimeCode annotation, if applicable
        		InterlinearAnnotation ann =(InterlinearAnnotation) n.getUserObject();
            	   String sdLabel = silDurationLabels.get(ann.getTierName());
            	   if(sdLabel != null){            		 
            		   dur = nextAnn.bt - ann.et ;           			   
            		   if (dur >= interlinearizer.getMinSilenceDuration()){
            			   String silDur = null;
            			   if(interlinearizer.getNumOfDecimalDigits() == Constants.ONE_DIGIT){     			              			  
            				   silDur = SD_SEP1 + String.valueOf(Math.round(dur/100f) / 10f) + SD_SEP2; 
            			   } else if (interlinearizer.getNumOfDecimalDigits() == Constants.TWO_DIGIT){
            				   silDur = SD_SEP1 + String.valueOf(Math.round(dur/10) / 100f) + SD_SEP2;              				   
            			   } else if (interlinearizer.getNumOfDecimalDigits() == Constants.THREE_DIGIT){
            				   silDur = SD_SEP1 + String.valueOf(Math.round(dur) / 1000f) + SD_SEP2;              				   
            			   } 
            			   
            			   InterlinearAnnotation sdAnn = new InterlinearAnnotation(silDur,
            					   sdLabel);
            			   n.add(new DefaultMutableTreeNode(sdAnn));
            			   calculateBlock(n, g);
            		   }
            	   }
        		}            			   
        		n = nextN;
        	}             
        }
        
        /*
        System.out.println("\n" + "Sorted... ");
        for (int i = 0; i < annotationBlocks.size(); i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) annotationBlocks.get(i);
            printoutNode(n);
        }
      */
    }

    /**
     * When annotations have been put into an interlinear layout, they are
     * added to  output or print blocks. Wrapping and empty line style etc are
     * taken into account.
     */
    public void calculatePrintBlocks() {
        if (annotationBlocks.size() == 0) {
            return;
        }

        // add the timecode tier/tiers to the template, at the right position
        // after the last tier of a tiergroup, or at the end if only one timecode tier is used
        // if (interlinearizer.isTimeCodeShown()) {
        //CC 29/11/2010 OR isListenCode
        if (interlinearizer.isTimeCodeShown() || interlinearizer.isPlaySoundSel()) {
            if (interlinearizer.getTimeCodeMultiplicity() == Interlinear.SINGLE_TIMECODE) {
                // the timecode tier is the last one
                tierTemplate.add(createTCLabel(""));
            } else {
                ArrayList<String> done = new ArrayList<String>();

                for (int i = tierTemplate.size() - 1; i >= 0; i--) {
                    String name = tierTemplate.get(i);

                    TierImpl t = (TierImpl) getTierWithId(name);
                    TierImpl root = t.getRootTier();

                    if (!done.contains(root.getName())) {
                        // this is the last before tier from it's tier group,
                        // add the tc after this one
                        tierTemplate.add(i+1,
                            timecodeLabels.get(root.getName()));
                        done.add(root.getName());
                    }
                }
            }
        }
        
        if (interlinearizer.isShowSilenceDuration()) {            
        	ArrayList<String> done = new ArrayList<String>();
            
        	for (int i = tierTemplate.size() - 1; i >= 0; i--) {
        		String name = tierTemplate.get(i);

                TierImpl t = (TierImpl) getTierWithId(name);
                if(t!= null){
                	TierImpl root = t.getRootTier();

                if (!done.contains(root.getName())) {
                    // this is the last tier from it's tier group,
                    // add the sd after this one
                	 if (interlinearizer.isTimeCodeShown()) {         
                		 	tierTemplate.add(i+2 ,  silDurationLabels.get(root.getName()));
                        		done.add(root.getName());
                	 }else {
                		 tierTemplate.add(i+1 ,  silDurationLabels.get(root.getName()));
                 		done.add(root.getName());
                		 
                	 }
                    }
               	}
        	}
        }
        
        // create PrintBlocks
        switch (interlinearizer.getBlockWrapStyle()) {
        case Interlinear.EACH_BLOCK:
            calcPrintBlocksWrapEach();

            break;

        case Interlinear.BLOCK_BOUNDARY:
            calcPrintBlocksWrapBoundary();

            break;

        case Interlinear.WITHIN_BLOCKS:
            calcPrintBlocksWrapWithin();

            break;

        case Interlinear.NO_WRAP:
            calcPrintBlocksNoWrap();

            break;

        default:}
        
        //test
        /*
        if (printBlocks.size() > 0) {
            printHTML((InterlinearBlock)printBlocks.get(0));
            for (int i = 0; i < printBlocks.size(); i++) {
                printoutPrintBlock((InterlinearBlock)printBlocks.get(i));
            }
        }
        */
        if (interlinearizer.getOutputMode() == Interlinear.HTML) {
            addHiddenCellsAndColSpan();
        }
    }

    /**
     * Page breaks are stored as an array of four integers: the first index is
     * the first InterlinearBlock index, the second index is the index of the
     * first line  within that block. The third index is the index of the last
     * InterlinearBlock on the page  and the fourth index is the index of the
     * last line in that block that fits on the  page. All indices are zero
     * based.  For text based output there is only one page break object. In
     * that case  the total height is calculated just for preview painting.
     */
    public void calculatePageBreaks() {
        pageBreaks.clear();

        if (printBlocks.size() == 0) {
            return;
        }

        InterlinearBlock curBlock = null;
        List<InterlinearTier> printTiers = null;
        int curPageHeight = 0;

        if ((interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT) ||
                (interlinearizer.getOutputMode() == Interlinear.SHOEBOX_TEXT)) {
            for (int block = 0; block < printBlocks.size(); block++) {
                curBlock = printBlocks.get(block);
                curPageHeight += (curBlock.getNumberOfLines() * Interlinear.DEFAULT_FONT_SIZE);
                curPageHeight += (interlinearizer.getBlockSpacing() * Interlinear.DEFAULT_FONT_SIZE);
            }

            interlinearizer.setHeight(curPageHeight);

            // it is just one page
            curBlock = printBlocks.get(printBlocks.size() -
                    1);
            pageBreaks.add(new int[] {
                    0, 0, printBlocks.size() - 1,
                    curBlock.getNumberOfLines() - 1
                });

            return;
        }

        int pageHeight = interlinearizer.getPageHeight();
        
        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
        	if (interlinearizer.isShowPageNumber()) {
        		pageHeight -= interlinearizer.pageNumberAreaHeight;
        	}
        }

        //System.out.println("Page height: " + pageHeight);       
        int beginBlock = 0;
        int beginLine = 0;
        int line = 0;
        InterlinearTier curTier = null;

        for (int block = 0; block < printBlocks.size(); block++) {
            curBlock = printBlocks.get(block);
            printTiers = curBlock.getPrintTiers();
            line = 0;

            for (int j = 0; j < printTiers.size(); j++) {
                curTier = printTiers.get(j);

                for (int k = 0; k < curTier.getNumLines(); k++) {
                    curPageHeight += curTier.getPrintHeight();

                    //System.out.println("Height after: " + curTier.getTierName() + " line: " + k + " = " + curPageHeight);
                    if (curPageHeight > pageHeight) {
                        // add a new pagebreak
                        if ((line == 0) && (block > 0)) {
                            // if the first line of a block does not fit 
                            // the last line of the previous block is the last on the page
                            InterlinearBlock prev = printBlocks.get(block -
                                    1);
                            pageBreaks.add(new int[] {
                                    beginBlock, beginLine, block - 1,
                                    prev.getNumberOfLines() - 1
                                });

                            //System.out.println("Add break after block: " + beginBlock + " - " + beginLine + " - " +
                            //	(block - 1) + " - " + (prev.getNumberOfLines() - 1) + "(" + curPageHeight + ")" + " tier: " + curTier.getTierName());
                        } else {
                            pageBreaks.add(new int[] {
                                    beginBlock, beginLine, block, line - 1
                                });

                            //System.out.println("Add break in block: " + beginBlock + " - " + beginLine + " - " +
                            //	(block) + " - " + (line - 1) + "(" + curPageHeight + ")" + " tier: " + curTier.getTierName());
                        }

                        beginBlock = block;
                        beginLine = line;

                        curPageHeight = curTier.getPrintHeight();
                    }

                    // don't add line spacing after the last line in a tier
                    if (k != (curTier.getNumLines() - 1)) {
                        curPageHeight += interlinearizer.getLineSpacing();
                    }

                    line++;
                }

                // don't add line spacing after the last tier in a block 
                if (j != (printTiers.size() - 1)) {
                    curPageHeight += interlinearizer.getLineSpacing();
                }
            }

            if (block != (printBlocks.size() - 1)) {
                // add block spacing
                curPageHeight += interlinearizer.getBlockSpacing();
            } else {
                // add a break after the last block
                pageBreaks.add(new int[] { beginBlock, beginLine, block, line -
                        1 });

                //System.out.println("Add final break: " + beginBlock + " - " + beginLine + " - " +
                //(block) + " - " + (line - 1) + " tier: " + curTier.getTierName());
            }
        }
    }

    /**
     * Returns a List of <code>InterlinearBlock</code> (a.k.a PrintBlock)
     * objects.
     *
     * @return a List of <code>InterlinearBlock</code> objects
     */
    public List<InterlinearBlock> getPrintBlocks() {
        return printBlocks;
    }

    /**
     * Returns a List of page break objects. A page break object is an aaray of
     * four integers: the first block (index), the first line (index) in that
     * block, the last block (index) and the last line (index) in that block
     *
     * @return a List of page break objects
     */
    public List<int[]> getPageBreaks() {
        return pageBreaks;
    }
    
    /**
     * Sends the contents of InterlinearAnnotation objects in a node or tree to
     * standard out. For testing.
     *
     * @param node the (root) node holding a <code>InterlinearAnnotation</code>
     *        object
     */
    private void printoutNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return;
        }

        Enumeration nodeEnum = node.breadthFirstEnumeration();
        DefaultMutableTreeNode curNode;
        InterlinearAnnotation prann;

        while (nodeEnum.hasMoreElements()) {
            curNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
            prann = (InterlinearAnnotation) curNode.getUserObject();
            System.out.println("T: " + prann.getTierName() + " A: " +
                prann.getValue() + " X: " + prann.x + " C: " + prann.calcWidth +
                " R: " + prann.realWidth);

            if (prann.nrOfLines > 1) {
                System.out.println("Num lines: " + prann.nrOfLines);

                for (int i = 0; i < prann.nrOfLines; i++) {
                    System.out.println("" + i + " " + prann.getLines()[i]);
                }
            }
        }
    }

    /**
     * Sends the contents of a InterlinearBlock object to standard out. For
     * testing
     *
     * @param block a <code>InterlinearBlock</code> object
     */
    private void printoutPrintBlock(InterlinearBlock block) {
        if (block == null) {
            return;
        }
        System.out.println("New block:");
        List<InterlinearTier> pts = block.getPrintTiers();

        for (int i = 0; i < pts.size(); i++) {
            InterlinearTier pt = pts.get(i);
            System.out.println("Tier: " + pt.getTierName());

            List<InterlinearAnnotation> anns = pt.getAnnotations();

            for (int j = 0; j < anns.size(); j++) {
                InterlinearAnnotation pa = anns.get(j);

                if (pa.nrOfLines > 1) {
                    System.out.println("A: " + pa.getLines()[0]);

                    for (int k = 1; k < pa.nrOfLines; k++) {
                        System.out.println("..." + pa.getLines()[k]);
                    }
                } else {
                    System.out.println("A: " + pa.getValue());
                }

                System.out.println("\tX: " + pa.x + " W: " + pa.calcWidth);
            }
        }
    }

    /*
       private void calculateTierHeightsAndMargin(Graphics g) {
           int maxTierLabelWidth = 0;
           Tier t;
           String name = null;
           Font font = null;
           FontMetrics fontMetrics = null;
           // iterate over visible tiers
           for (int i = 0; i < visibleTiers.size(); i++) {
               t = (Tier) visibleTiers.get(i);
               name = "";
               try {
                   name = t.getName();
               } catch (RemoteException re) {
               }
               font = interlinearizer.getFont(name);
               fontMetrics = g.getFontMetrics(font);
    
                  int tierHeight = fontMetrics.getHeight();
                  setTierHeight(name, tierHeight);
    
                  if (fontMetrics.stringWidth(name) > maxTierLabelWidth) {
                      maxTierLabelWidth = fontMetrics.stringWidth(name);
                  }
              }
    
              setLeftMargin(maxTierLabelWidth + interlinearizer.getEmptySpace());
          }
     */

    /**
     * Calculates sizes and positions of annotations in one block, using the
     * specified  Graphics object for measurements.
     *
     * @param rootNode the root node holding the root annotation
     * @param g the <code>Graphics</code> object
     */
    private void calculateBlock(DefaultMutableTreeNode rootNode, Graphics g) {
        if ((rootNode == null) ||
                ((g == null) &&
                (interlinearizer.getAlignmentUnit() == Interlinear.PIXELS))) {
            return;
        }

        //Enumeration enum = rootNode.depthFirstEnumeration();
        Enumeration en = rootNode.breadthFirstEnumeration();
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<DefaultMutableTreeNode>();
        Font font = null;
        FontMetrics fontMetrics = null;
        DefaultMutableTreeNode curNode = null;
        DefaultMutableTreeNode otherNode = null;
        InterlinearAnnotation curAnn = null;
        InterlinearAnnotation otherAnn = null;
        boolean includeRootInCalculations = true;
        int availableWidth = Math.max(1, interlinearizer.getWidth() - getLeftMargin());

        // System.out.println("PR width: " + availableWidth);
        // first calculate real widths
        while (en.hasMoreElements()) {
            curNode = (DefaultMutableTreeNode) en.nextElement();
            curAnn = (InterlinearAnnotation) curNode.getUserObject();

            if (curNode == rootNode) {
                if (!visibleTiers.contains(getTierWithId(
                                curAnn.getTierName()))) {
                    includeRootInCalculations = false;
                }
            }

            int size = 0;

            if (interlinearizer.getAlignmentUnit() == Interlinear.PIXELS) {
                if (curAnn.type == InterlinearAnnotation.TIMECODE) {
                    //font = Interlinear.DEFAULTFONT.deriveFont((float) Interlinear.TIMECODE_FONT_SIZE);
                    font = interlinearizer.getFont(curAnn.getTierName());
                } else {
                    font = interlinearizer.getFont(curAnn.getTierName());
                }

                fontMetrics = g.getFontMetrics(font);
                // ensure a mininmal width of "empty space"?
                size = Math.max(fontMetrics.stringWidth(curAnn.getValue()), interlinearizer.getEmptySpace());
            } else {
                size = curAnn.getValue().length();
                
                if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT) {
                	size -= getNumNonSpacingCharacters(curAnn.getValue());
                } else if (interlinearizer.getOutputMode() == Interlinear.SHOEBOX_TEXT) {
                	//size += getNumNonSpacingCharacters(curAnn.getValue());
                	// check multi byte characters as well
                	size += getNumExtraBytes(curAnn.getValue());
                }
                
            }

            if (curNode.isRoot() && !includeRootInCalculations) {
                curAnn.realWidth = 0;
                curAnn.calcWidth = 0;
            } else {
                curAnn.realWidth = size;
                curAnn.calcWidth = size;
            }

            // check wrapping params and wrap if needed
            if ((interlinearizer.getBlockWrapStyle() != Interlinear.NO_WRAP) &&
                    (interlinearizer.getLineWrapStyle() == Interlinear.NEXT_LINE || 
                    		interlinearizer.getLineWrapStyle() == Interlinear.END_OF_BLOCK)) {
                if (curAnn.realWidth > availableWidth) {
                    if (interlinearizer.getAlignmentUnit() == Interlinear.PIXELS) {
                        splitAnnotation(curAnn, availableWidth, fontMetrics);
                    } else {
                        splitAnnotation(curAnn, availableWidth);
                    }
                }
            }

            allNodes.add(curNode);
        }

        // printoutNode(rootNode);
        // next update calculated widths, the iteration is starting at the bottom
        // note: you cannot reliably call getChildCount from within an enumeration...
        //for (int i = 0; i < allNodes.size(); i++) {
        for (int i = allNodes.size() - 1; i >= 0; i--) {
            curNode = allNodes.get(i);
            curAnn = (InterlinearAnnotation) curNode.getUserObject();

            if (curNode.getChildCount() == 0) {
                continue;
            }

            if (curNode.getChildCount() == 1) {
                otherNode = (DefaultMutableTreeNode) curNode.getFirstChild();
                otherAnn = (InterlinearAnnotation) otherNode.getUserObject();

                // don't change the size of a multiple line annotation
                if ((otherAnn.calcWidth > curAnn.calcWidth) &&
                        (curAnn.nrOfLines == 1)) {
                    curAnn.calcWidth = otherAnn.calcWidth;
                } else if ((otherAnn.calcWidth < curAnn.calcWidth) &&
                        (otherAnn.nrOfLines == 1)) {
                    otherAnn.calcWidth = curAnn.calcWidth;
                    propagateSizeDown(otherNode);
                }
            } else {
                // more than one child, can be on several tiers
                propagateSizeDown(curNode);
            }
        }

        // printoutNode(rootNode);
        // calculate horizontal positions within the root annotation block,
        // where the root, visible or not, starts at x = 0, and annotations 
        // inherit positions. Start at top node and propagate downward
        for (int i = 0; i < allNodes.size(); i++) {
            curNode = allNodes.get(i);

            if (i == 0) {
                curAnn = (InterlinearAnnotation) curNode.getUserObject();
                curAnn.x = 0;
            }

            calculateXPosDown(curNode);
        }
    }

    /**
     * Check and adapt annotation sizes top-down and recursively. If the sum of
     * the sizes of the child annotations is larger than  the size of the
     * root, the root annotation is changed and the change is  propagated
     * again.
     *
     * @param rootNode the node holding the root annotation of a (sub)tree
     */
    private void propagateSizeDown(DefaultMutableTreeNode rootNode) {
        if (rootNode == null) {
            return;
        }

        if (rootNode.getChildCount() == 1) {
            InterlinearAnnotation curAnn = (InterlinearAnnotation) rootNode.getUserObject();
            DefaultMutableTreeNode otherNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
            InterlinearAnnotation otherAnn = (InterlinearAnnotation) otherNode.getUserObject();

            // don't change the size of a multiple line annotation
            if ((otherAnn.calcWidth > curAnn.calcWidth) &&
                    (curAnn.nrOfLines == 1)) {
                curAnn.calcWidth = otherAnn.calcWidth;
            } else if ((otherAnn.calcWidth < curAnn.calcWidth) &&
                    (otherAnn.nrOfLines == 1)) {
                otherAnn.calcWidth = curAnn.calcWidth;

                if (otherNode.getChildCount() > 0) {
                    propagateSizeDown(otherNode);
                }
            }
        } else if (rootNode.getChildCount() > 1) {
            InterlinearAnnotation curAnn = (InterlinearAnnotation) rootNode.getUserObject();
            int maxWidth = curAnn.calcWidth;
            DefaultMutableTreeNode otherNode = null;
            DefaultMutableTreeNode prevNode = null;
            InterlinearAnnotation otherAnn = null;
            InterlinearAnnotation prevAnn = null;
            String tierName = null;
            String lastTierName = null;
            int widthPerTier = 0;

            for (int i = 0; i < rootNode.getChildCount(); i++) {
                otherNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                otherAnn = (InterlinearAnnotation) otherNode.getUserObject();
                tierName = otherAnn.getTierName();

                if (tierName == lastTierName) {
                    // add to the current width
                    widthPerTier += (interlinearizer.getEmptySpace() +
                    otherAnn.calcWidth);
                } else {
                    if (lastTierName != null) {
                        // we have had all annotations of one child tier
                        if (widthPerTier > maxWidth) {
                            maxWidth = widthPerTier;
                        } else if ((widthPerTier < curAnn.calcWidth) &&
                                (prevAnn.nrOfLines == 1)) {
                            //add to the last annotation of that tier
                            prevAnn.calcWidth += (curAnn.calcWidth -
                            widthPerTier);

                            if (prevNode.getChildCount() > 0) {
                                propagateSizeDown(prevNode);
                            }
                        }

                        // reset
                        widthPerTier = otherAnn.calcWidth;
                    } else {
                        widthPerTier = otherAnn.calcWidth;
                    }
                }

                if (i == (rootNode.getChildCount() - 1)) {
                    //the last child annotation
                    if (widthPerTier > maxWidth) {
                        maxWidth = widthPerTier;
                    } else if ((widthPerTier < curAnn.calcWidth) &&
                            (otherAnn.nrOfLines == 1)) {
                        otherAnn.calcWidth += (curAnn.calcWidth - widthPerTier);

                        if (otherNode.getChildCount() > 0) {
                            propagateSizeDown(otherNode);
                        }
                    }
                }

                lastTierName = tierName;
                prevAnn = otherAnn;
                prevNode = otherNode;
            }

            if ((maxWidth > curAnn.calcWidth) && (curAnn.nrOfLines == 1)) {
                curAnn.calcWidth = maxWidth;

                // do it again, with changed size
                propagateSizeDown(rootNode);
            }
        }
    }

    /**
     * Calculate the x positions of child annotations recursively.
     *
     * @param rootNode the node holding the root annotation
     */
    private void calculateXPosDown(DefaultMutableTreeNode rootNode) {
        if (rootNode == null) {
            return;
        }

        if (rootNode.getChildCount() == 1) {
            InterlinearAnnotation curAnn = (InterlinearAnnotation) rootNode.getUserObject();
            DefaultMutableTreeNode otherNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
            InterlinearAnnotation otherAnn = (InterlinearAnnotation) otherNode.getUserObject();

            // the one child gets the same x-pos as the parent
            otherAnn.x = curAnn.x;

            if (otherNode.getChildCount() > 0) {
                calculateXPosDown(otherNode);
            }
        } else if (rootNode.getChildCount() > 1) {
            InterlinearAnnotation curAnn = (InterlinearAnnotation) rootNode.getUserObject();
            DefaultMutableTreeNode otherNode = null;
            InterlinearAnnotation otherAnn = null;
            InterlinearAnnotation prevAnn = null;
            String tierName = null;
            String lastTierName = null;

            for (int i = 0; i < rootNode.getChildCount(); i++) {
                otherNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                otherAnn = (InterlinearAnnotation) otherNode.getUserObject();
                tierName = otherAnn.getTierName();

                if (tierName == lastTierName) {
                    otherAnn.x = prevAnn.x + prevAnn.calcWidth +
                        interlinearizer.getEmptySpace();
                } else {
                    otherAnn.x = curAnn.x;
                }

                if (otherNode.getChildCount() > 0) {
                    calculateXPosDown(otherNode);
                }

                lastTierName = tierName;
                prevAnn = otherAnn;
            }
        }

        // printoutNode(rootNode);
    }

    /**
     * Splits/wraps annotation values at space boundaries. Words that are wider
     * than the  available width are not split. The typical scenario will be
     * that a value is just a bit wider  than one line and that it therefore
     * is most efficient to start calculations by  truncating words from the
     * end of the string. Note: It is assumed that the params are not null and
     * that the annotation width has been checked to be larger than the
     * available width.
     *
     * @param prAnn the InterlinearAnnotation containing the value
     * @param available the available width for the value
     * @param metrics the font metrics, used to calculate sizes
     */
    private void splitAnnotation(InterlinearAnnotation prAnn, int available,
        FontMetrics metrics) {
        int estimatedNrLines = (int) Math.ceil(prAnn.realWidth / available);
        ArrayList<String> lines = new ArrayList<String>();

        if (estimatedNrLines <= 2) {
            //if (1 + 1 != 2) {
            // start calculations at the end			
            String value = prAnn.getValue();
            int charIndex = value.length();
            int size = prAnn.realWidth;
            String temp = null;

            while (true) {
                charIndex = value.lastIndexOf(SPACE_CHAR, charIndex);

                if (charIndex < 0) {
                    // don't try to split words
                    lines.add(value);

                    break;
                }

                temp = value.substring(0, charIndex);
                size = metrics.stringWidth(temp);

                if (size < available) {
                    lines.add(temp);
                    value = value.substring(charIndex + 1);
                    size = metrics.stringWidth(value);

                    if (size < available) {
                        lines.add(value);

                        break;
                    }

                    charIndex = value.length();
                } else {
                    //value = temp;
                    charIndex--;
                }
            }
        } else {
            // start calculations at the begin
            String value = prAnn.getValue();
            int charIndex = 0;
            int prevIndex = -1;
            int size = prAnn.realWidth;
            String temp = null;

            while (true) {
                charIndex = value.indexOf(SPACE_CHAR, charIndex);

                if (charIndex < 0) {
                    if (prevIndex > 0) {
                        // one word will go to the next line
                        temp = value.substring(0, prevIndex);
                        lines.add(temp);
                        value = value.substring(prevIndex + 1);
                        lines.add(value);

                        break;
                    } else {
                        // don't try to split words
                        lines.add(value);

                        break;
                    }
                }

                temp = value.substring(0, charIndex);
                size = metrics.stringWidth(temp);

                if (size > available) {
                    if (prevIndex < 0) {
                        // just add temp
                        lines.add(temp);
                        value = value.substring(charIndex + 1);
                        prevIndex = charIndex;

                        if (metrics.stringWidth(value) < available) {
                            lines.add(value);

                            break;
                        }
                    } else {
                        temp = value.substring(0, prevIndex);
                        lines.add(temp);
                        value = value.substring(prevIndex + 1);

                        if (metrics.stringWidth(value) < available) {
                            lines.add(value);

                            break;
                        }

                        charIndex = 0;
                        prevIndex = -1;
                    }
                } else {
                    prevIndex = charIndex;
                    charIndex++;
                }
            }
        }

        if (lines.size() > 1) {
            prAnn.setLines(lines.toArray(new String[] {  }));
            prAnn.calcWidth = available;
            prAnn.realWidth = available;
        }
    }

    /**
     * Splits/wraps annotation values at space boundaries. Words that are wider
     * than the  available width are not split.  Measurements are based on
     * number of characters.
     *
     * @param prAnn the InterlinearAnnotation containing the value
     * @param available the available width for the value
     */
    private void splitAnnotation(InterlinearAnnotation prAnn, int available) {
        String val = prAnn.getValue();

        if ((val.indexOf(SPACE_CHAR) < 0) || (val.length() < available)) {
            return;
        }

        ArrayList<String> vals = new ArrayList<String>();
        String sub = null;

        while (val.length() > available) {
            sub = val.substring(0, available);

            int breakSpace = sub.lastIndexOf(SPACE_CHAR);

            if (breakSpace < 0) {
                breakSpace = val.indexOf(SPACE_CHAR);

                if (breakSpace < 0) {
                    vals.add(val);

                    break;
                } else {
                    vals.add(val.substring(0, breakSpace + 1));
                    val = val.substring(breakSpace + 1);
                }
            } else {
                vals.add(sub.substring(0, breakSpace + 1));
                val = val.substring(breakSpace + 1);
            }

            if (val.length() <= available) {
                vals.add(val);

                break;
            }
        }

        // if there are more than one line
        if (vals.size() > 1) {
            prAnn.setLines(vals.toArray(new String[] {  }));

            // ?? is this right, or calc the max length of the strings?
            prAnn.calcWidth = available;
            prAnn.realWidth = available;
        }
    }

    /**
     * Creates a unique label for a timecode 'tier', based on the name of the
     * root tier or toplevel tier and only used internally.
     *
     * @param tierName the name of the top level tier
     *
     * @return a String in the form 'tiername'+ '-' + 'TC'
     */
    private String createTCLabel(String tierName) {
        if (tierName == null) {
            return null;
        }

        String label = tierName + "-" + TC_TIER_NAME;

        if (getTierWithId(label) == null) {
            return label;
        } else {
            for (int i = 0; i < 10; i++) {
                String nl = label + i;

                if (getTierWithId(nl) == null) {
                    return nl;
                }
            }
        }

        return null;
    }
    
    /**
     * Creates a unique label for a silDuration 'tier', based on the name of the
     * root tier or toplevel tier and only used internally.
     *
     * @param tierName the name of the top level tier
     *
     * @return a String in the form 'tiername'+ '-' + 'SD'
     */
    private String createSDLabel(String tierName) {
        if (tierName == null) {
            return null;
        }

        String label = tierName + "-" + SD_TIER_NAME;

        if (getTierWithId(label) == null) {
            return label;
        } else {
            for (int i = 0; i < 10; i++) {
                String nl = label + i;

                if (getTierWithId(nl) == null) {
                    return nl;
                }
            }
        }

        return null;
    }

    /**
     * Create a template print block; this is a block with a InterlinearTier
     * for every tie in the specified List. Print annotation can then be added
     * to this print block. Depending on the 'empty line style' parameter,
     * empty tiers/lines can be deleted  to finish the block.
     *
     * @param names a List of visible tier (+time code tier) names
     *
     * @return a template <code>InterlinearBlock</code>
     */
    private InterlinearBlock createPrintBlock(List<String> names) {
        if (names == null) {
            return null;
        }

        List<InterlinearTier> printTiers = new ArrayList<InterlinearTier>(names.size());
        int height;
        InterlinearTier pt = null;

        for (int i = 0; i < names.size(); i++) {
            String tierName = names.get(i);
            pt = new InterlinearTier(tierName);
            pt.setMarginWidth(getLeftMargin());

            //height = interlinearizer.getFontSize(tierName);
            height = getTierHeight(tierName);

            if (height != 0) {
                pt.setPrintHeight(height);
            }

            if (timecodeLabels.containsValue(tierName)) {
                pt.setTimeCode(true);
            }
            
            if (silDurationLabels.containsValue(tierName)) {
                pt.setSilDuration(true);
            }

            printTiers.add(pt);
        }

        if (printTiers.size() > 0) {
            return new InterlinearBlock(printTiers);
        }

        return null;
    }

    /**
     * Add all annotations to one block, without applying any wrapping.
     */
    private void calcPrintBlocksNoWrap() {
        printBlocks.clear();

        InterlinearBlock currentBlock = createPrintBlock(tierTemplate);
        currentBlock.setStartOfAnnotationBlock(true);
        
        DefaultMutableTreeNode curNode = null;
        InterlinearAnnotation curAnn = null;

        // loop over annotation blocks
        for (int i = 0; i < annotationBlocks.size(); i++) {
            curNode = annotationBlocks.get(i);
            curAnn = (InterlinearAnnotation) curNode.getUserObject();

            int w = curAnn.calcWidth;
            Enumeration en = curNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                curNode = (DefaultMutableTreeNode) en.nextElement();
                curAnn = (InterlinearAnnotation) curNode.getUserObject();

                InterlinearTier pt = currentBlock.getPrintTier(curAnn.getTierName());
                int cur = pt.getPrintAdvance();

                if (cur != 0) {
                    curAnn.x = cur + interlinearizer.getEmptySpace();
                }

                addToPrintTier(pt, curAnn);
            }

            if (i == 0) {
                currentBlock.setOccupiedBlockWidth(w);
            } else {
                currentBlock.setOccupiedBlockWidth(currentBlock.getOccupiedBlockWidth() +
                    interlinearizer.getEmptySpace() + w);
            }
        }

        //remove empty lines
        if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
            currentBlock.removeEmptyTiers();
            
            if (interlinearizer.isEmptySlotsShown()) {
                currentBlock.removeEmptySlotOnlyTiers();
            }
        }

        // print out block
        //printoutPrintBlock(currentBlock);
        printBlocks.add(currentBlock);
    }
    
    /**
     * Wrap to a new 'block line' for every new annotation block. This means
     * that every next toplevel annotation starts on a new  'block line'.
     */
    private void calcPrintBlocksWrapEach() {
        printBlocks.clear();

        InterlinearBlock currentBlock = null;
        InterlinearTier pt = null;
        ArrayList<DefaultMutableTreeNode> leftovers = new ArrayList<DefaultMutableTreeNode>();
        DefaultMutableTreeNode curNode = null;
        InterlinearAnnotation curAnn = null;

        int availableWidth = interlinearizer.getWidth() - getLeftMargin();

        // loop over annotation blocks
        for (int i = 0; i < annotationBlocks.size(); i++) {
            // start a new print block for every annotation block
            currentBlock = createPrintBlock(tierTemplate);
            currentBlock.setStartOfAnnotationBlock(true);
            
            printBlocks.add(currentBlock);
            leftovers.clear();
            curNode = annotationBlocks.get(i);

            Enumeration en = curNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                curNode = (DefaultMutableTreeNode) en.nextElement();
                curAnn = (InterlinearAnnotation) curNode.getUserObject();
                pt = currentBlock.getPrintTier(curAnn.getTierName());

                if (curNode.isRoot()) {
                    addToPrintTier(pt, curAnn);
                } else {
                    if (leftovers.contains(curNode.getParent())) {
                        leftovers.add(curNode);
                    } else if (curAnn.calcWidth > availableWidth) {
                        // symbolic association of root?
                        if ((curAnn.x + curAnn.realWidth) <= availableWidth) {
                            addToPrintTier(pt, curAnn);
                        } else {
                            leftovers.add(curNode);
                        }
                    } else if ((curAnn.x + curAnn.calcWidth) <= availableWidth) {
                        // add if it fits
                        addToPrintTier(pt, curAnn);
                    } else {
                        // add to leftovers for next blocks
                        leftovers.add(curNode);
                    }
                }
            }

            //remove empty lines
            if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                currentBlock.removeEmptyTiers();
                
                if (interlinearizer.isEmptySlotsShown()) {
                    currentBlock.removeEmptySlotOnlyTiers();
                }
                //printoutPrintBlock(currentBlock);
            }

            // the annotations that have not been placed, wrap to next blocks
            // reuse a map of per tier shift values
            HashMap<String, Integer> shifts = new HashMap<String, Integer>();

            while (leftovers.size() > 0) {
                // first find the lowest x-pos of the annotations not yet added
                int xShift;
                getXShiftPerTopNode(leftovers, shifts);

                // start with a new block and add as much as possible
                currentBlock = createPrintBlock(tierTemplate);

                ArrayList<DefaultMutableTreeNode> temp = new ArrayList<DefaultMutableTreeNode>(); // next leftovers

                for (int k = 0; k < leftovers.size(); k++) {
                    curNode = leftovers.get(k);

                    if (temp.contains(curNode.getParent())) {
                        temp.add(curNode);

                        continue;
                    }

                    curAnn = (InterlinearAnnotation) curNode.getUserObject();
                    pt = currentBlock.getPrintTier(curAnn.getTierName());
                    xShift = shifts.get(curAnn.getTierName()).intValue();

                    //int curAdv = pt.getPrintAdvance(); 
                    if (((curAnn.x + curAnn.calcWidth) - xShift) <= availableWidth) {
                        // adjust x-pos and add
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else if (curAnn.calcWidth > availableWidth) {
                        // is an error: to prevent endless loop add it just the same
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else {
                        // add for next block
                        temp.add(curNode);
                    }
                }

                //remove empty lines
                if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                    currentBlock.removeEmptyTiers();
                    
                    if (interlinearizer.isEmptySlotsShown()) {
                        currentBlock.removeEmptySlotOnlyTiers();
                    }
                }

                printBlocks.add(currentBlock);

                //printoutPrintBlock(currentBlock);
                leftovers = temp;
            }
        }
    }

    /**
     * Wrap to a new 'block line' if the whole block does not fit completely in
     * the current block. This means that a block of annotations is kept
     * together  on one 'block line' as much as possible.
     */
    private void calcPrintBlocksWrapBoundary() {
        printBlocks.clear();

        InterlinearBlock currentBlock = createPrintBlock(tierTemplate);
        currentBlock.setStartOfAnnotationBlock(true);
        
        InterlinearTier pt = null;
        ArrayList<DefaultMutableTreeNode> leftovers = new ArrayList<DefaultMutableTreeNode>();
        DefaultMutableTreeNode curNode = null;
        InterlinearAnnotation curAnn = null;

        int availableWidth = interlinearizer.getWidth() - getLeftMargin();
        int xShift = 0;

        // loop over annotation blocks
        for (int i = 0; i < annotationBlocks.size(); i++) {
            leftovers.clear();
            curNode = annotationBlocks.get(i);

            int blockwidth = 0;
            int curOccup = 0;

            Enumeration en = curNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                curNode = (DefaultMutableTreeNode) en.nextElement();
                curAnn = (InterlinearAnnotation) curNode.getUserObject();
                pt = currentBlock.getPrintTier(curAnn.getTierName());

                if (curNode.isRoot()) {
                    // check if the root (== block) fits in the space left in the current block
                    blockwidth = curAnn.calcWidth;

                    if (blockwidth > availableWidth) {
                        blockwidth = availableWidth;
                    }

                    curOccup = currentBlock.getOccupiedBlockWidth();

                    if (curOccup > 0) {
                        xShift = curOccup + interlinearizer.getEmptySpace();
                    }

                    if ((i == 0) || (curOccup == 0)) {
                        addToPrintTier(pt, curAnn);

                        continue;
                    }

                    if ((xShift + blockwidth) <= availableWidth) {
                        // ...if so, add to the block
                        if (curOccup > 0) {
                            curAnn.x += xShift;
                        }

                        addToPrintTier(pt, curAnn);
                    } else {
                        // ...else jump to the next block
                        // remove empty lines of current block
                        if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                            currentBlock.removeEmptyTiers();
                            
                            if (interlinearizer.isEmptySlotsShown()) {
                                currentBlock.removeEmptySlotOnlyTiers();
                            }
                        }

                        printBlocks.add(currentBlock);

                        //printoutPrintBlock(currentBlock);
                        currentBlock = createPrintBlock(tierTemplate);
                        currentBlock.setStartOfAnnotationBlock(true);
                        
                        pt = currentBlock.getPrintTier(curAnn.getTierName());
                        addToPrintTier(pt, curAnn);
                        xShift = 0;
                    }
                } else {
                    if (leftovers.contains(curNode.getParent())) {
                        leftovers.add(curNode);
                    } else {
                        // check if this annotation fits on this block
                        // if the calcWidth > available width it is a sym-association annotation
                        int relWidth = (curAnn.calcWidth > availableWidth)
                            ? curAnn.realWidth : curAnn.calcWidth;

                        if ((curAnn.x + relWidth + xShift) <= availableWidth) {
                            curAnn.x += xShift;
                            addToPrintTier(pt, curAnn);
                        } else {
                            leftovers.add(curNode);
                        }
                    }
                }
            }

            if (leftovers.size() == 0) {
                currentBlock.setOccupiedBlockWidth(currentBlock.getOccupiedBlockWidth() +
                    interlinearizer.getEmptySpace() + blockwidth);

                // go to next annotation block
                continue;
            } else {
                //remove empty lines
                if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                    currentBlock.removeEmptyTiers();
                    
                    if (interlinearizer.isEmptySlotsShown()) {
                        currentBlock.removeEmptySlotOnlyTiers();
                    }
                }

                printBlocks.add(currentBlock);

                //printoutPrintBlock(currentBlock);
            }

            // the annotations that have not been placed, wrap to next blocks
            // reuse a map of per tier shift values
            HashMap<String, Integer> shifts = new HashMap<String, Integer>();

            while (leftovers.size() > 0) {
                // find the minimal x-pos of the annotations not yet added
                getXShiftPerTopNode(leftovers, shifts);

                //System.out.println("xShift: " + xShift);
                currentBlock = createPrintBlock(tierTemplate);

                ArrayList<DefaultMutableTreeNode> temp = new ArrayList<DefaultMutableTreeNode>();

                for (int k = 0; k < leftovers.size(); k++) {
                    curNode = leftovers.get(k);

                    if (temp.contains(curNode.getParent())) {
                        temp.add(curNode);

                        continue;
                    }

                    curAnn = (InterlinearAnnotation) curNode.getUserObject();
                    pt = currentBlock.getPrintTier(curAnn.getTierName());
                    xShift = shifts.get(curAnn.getTierName()).intValue();

                    //int curAdv = pt.getPrintAdvance();
                    if (((curAnn.x + curAnn.calcWidth) - xShift) <= availableWidth) {
                        // adjust x-pos and add
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else if (curAnn.calcWidth > availableWidth) {
                        // is an error: to prevent endless loop add it just the same
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else {
                        // add for next block
                        temp.add(curNode);
                    }
                }

                leftovers = temp;

                if (leftovers.size() > 0) {
                    //remove empty lines and create a new block
                    if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                        currentBlock.removeEmptyTiers();
                        
                        if (interlinearizer.isEmptySlotsShown()) {
                            currentBlock.removeEmptySlotOnlyTiers();
                        }
                    }

                    printBlocks.add(currentBlock);

                    //printoutPrintBlock(currentBlock);
                    //currentBlock = createPrintBlock(tierTemplate);
                } else {
                    // finish the block by calculating the current occupied horizontal block space
                    currentBlock.setOccupiedBlockWidth(currentBlock.calculateOccupiedBlockWidth());
                }
            }
        }

        // end annotations loop
        // fimally finish and add the last block, if needed
        if (!printBlocks.contains(currentBlock)) {
            if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                currentBlock.removeEmptyTiers();
                
                if (interlinearizer.isEmptySlotsShown()) {
                    currentBlock.removeEmptySlotOnlyTiers();
                }
            }

            printBlocks.add(currentBlock);

            //printoutPrintBlock(currentBlock);
        }
    }

    /**
     * Wrap to a new 'block line' if the "root" annotation and it's first
     * children on each depending tier don't fit completely in the current
     * block. Else start adding to the current block.
     */
    private void calcPrintBlocksWrapWithin() {
        printBlocks.clear();

        InterlinearBlock currentBlock = createPrintBlock(tierTemplate);
        InterlinearTier pt = null;
        ArrayList<DefaultMutableTreeNode> leftovers = new ArrayList<DefaultMutableTreeNode>();
        ArrayList<InterlinearAnnotation> firstChildren;
        DefaultMutableTreeNode curNode = null;
        InterlinearAnnotation curAnn = null;

        int availableWidth = interlinearizer.getWidth() - getLeftMargin();
        int xShift = 0;

        // loop over the annotation blocks
        for (int i = 0; i < annotationBlocks.size(); i++) {
            leftovers.clear();
            curNode = annotationBlocks.get(i);

            // calculate the max. realwidth of the root and first children
            int relMaxWidth = getMaximumWidthOfFirstChildren(curNode);
            firstChildren = getFirstChildrenOfRoot(curNode);

            int blockwidth = 0;
            int curOccup = 0;

            Enumeration en = curNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                curNode = (DefaultMutableTreeNode) en.nextElement();
                curAnn = (InterlinearAnnotation) curNode.getUserObject();
                pt = currentBlock.getPrintTier(curAnn.getTierName());

                if (curNode.isRoot()) {
                    // check if the root (== block) and relevant child annotations 
                    // fit in the space left in the current block
                    blockwidth = curAnn.calcWidth;

                    if (blockwidth > availableWidth) {
                        blockwidth = availableWidth;
                    }

                    curOccup = currentBlock.getOccupiedBlockWidth();

                    if (curOccup > 0) {
                        xShift = curOccup + interlinearizer.getEmptySpace();
                    }

                    if ((i == 0) || (curOccup == 0)) {
                        addToPrintTier(pt, curAnn);

                        continue;
                    }

                    if ((xShift + relMaxWidth) <= availableWidth) {
                        // ...if so, add to the block
                        if (curOccup > 0) {
                            curAnn.x += xShift;
                        }

                        addToPrintTier(pt, curAnn);
                    } else {
                        // ...else jump to the next block
                        // remove empty lines of current block
                        if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                            currentBlock.removeEmptyTiers();
                            
                            if (interlinearizer.isEmptySlotsShown()) {
                                currentBlock.removeEmptySlotOnlyTiers();
                            }
                        }

                        printBlocks.add(currentBlock);

                        //printoutPrintBlock(currentBlock);
                        currentBlock = createPrintBlock(tierTemplate);
                        pt = currentBlock.getPrintTier(curAnn.getTierName());
                        addToPrintTier(pt, curAnn);
                        xShift = 0;
                    }
                } else {
                    if (leftovers.contains(curNode.getParent())) {
                        leftovers.add(curNode);
                    } else {
                        // check if this annotation fits on this block
                        // if the calcWidth > available width it is a sym-association annotation
                        int relWidth = (curAnn.calcWidth > availableWidth)
                            ? curAnn.realWidth : curAnn.calcWidth;

                        if ((curAnn.x + relWidth + xShift) <= availableWidth) {
                            curAnn.x += xShift;
                            addToPrintTier(pt, curAnn);
                        } else if (firstChildren.contains(curAnn)) {
                            curAnn.x += xShift;
                            addToPrintTier(pt, curAnn);
                        } else {
                            leftovers.add(curNode);
                        }
                    }
                }
            }

            if (leftovers.size() == 0) {
                currentBlock.setOccupiedBlockWidth(currentBlock.getOccupiedBlockWidth() +
                    interlinearizer.getEmptySpace() + blockwidth);

                // go to next annotation block
                continue;
            } else {
                //remove empty lines
                if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                    currentBlock.removeEmptyTiers();
                    
                    if (interlinearizer.isEmptySlotsShown()) {
                        currentBlock.removeEmptySlotOnlyTiers();
                    }
                }

                printBlocks.add(currentBlock);

                //printoutPrintBlock(currentBlock);
            }

            // the annotations that have not been placed, wrap to next blocks
            // reuse a map of per tier shift values
            HashMap<String, Integer> shifts = new HashMap<String, Integer>();

            while (leftovers.size() > 0) {
                // find the minimal x-pos of the annotations not yet added
                getXShiftPerTopNode(leftovers, shifts);

                //System.out.println("xShift: " + xShift);
                currentBlock = createPrintBlock(tierTemplate);

                ArrayList<DefaultMutableTreeNode> temp = new ArrayList<DefaultMutableTreeNode>();

                for (int k = 0; k < leftovers.size(); k++) {
                    curNode = leftovers.get(k);

                    if (temp.contains(curNode.getParent())) {
                        temp.add(curNode);

                        continue;
                    }

                    curAnn = (InterlinearAnnotation) curNode.getUserObject();
                    pt = currentBlock.getPrintTier(curAnn.getTierName());
                    xShift = shifts.get(curAnn.getTierName()).intValue();

                    //int curAdv = pt.getPrintAdvance();
                    if (((curAnn.x + curAnn.calcWidth) - xShift) <= availableWidth) {
                        // adjust x-pos and add
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else if (curAnn.calcWidth > availableWidth) {
                        // is an error: to prevent endless loop add it just the same
                        curAnn.x -= xShift;
                        addToPrintTier(pt, curAnn);
                    } else {
                        // add for next block
                        temp.add(curNode);
                    }
                }

                leftovers = temp;

                if (leftovers.size() > 0) {
                    //remove empty lines and create a new block
                    if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                        currentBlock.removeEmptyTiers();
                        
                        if (interlinearizer.isEmptySlotsShown()) {
                            currentBlock.removeEmptySlotOnlyTiers();
                        }
                    }

                    printBlocks.add(currentBlock);

                    //printoutPrintBlock(currentBlock);
                    //currentBlock = createPrintBlock(tierTemplate);
                } else {
                    // finish the block by calculating the current occupied horizontal block space
                    currentBlock.setOccupiedBlockWidth(currentBlock.calculateOccupiedBlockWidth());
                }
            }
        }

        // end annotations loop
        // fimally finish and add the last block, if needed
        if (!printBlocks.contains(currentBlock)) {
            if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
                currentBlock.removeEmptyTiers();
                
                if (interlinearizer.isEmptySlotsShown()) {
                    currentBlock.removeEmptySlotOnlyTiers();
                }
            }

            printBlocks.add(currentBlock);

            //printoutPrintBlock(currentBlock);
        }
    }

    /**
     * Add a print annotation to a print tier in a print block.
     *
     * @param pt the <code>InterlinearTier</code> that is to receive the
     *        annotation
     * @param prann the <code>InterlinearAnnotation</code> to add to the tier
     */
    private void addToPrintTier(InterlinearTier pt, InterlinearAnnotation prann) {
        if ((pt != null) && (prann != null)) {
            pt.addAnnotation(prann);

            if (prann.nrOfLines > pt.getNumLines() ) {
                pt.setNumLines(prann.nrOfLines);
            }
        }
    }

    /**
     * Return the minumum x value of the annotations in the list.
     *
     * @param printNodes the printannotations
     * @param shiftMap DOCUMENT ME!
     */

    /*
       private int getMinimumXValue(ArrayList printAnnotations) {
           int xMin = Integer.MAX_VALUE;
           InterlinearAnnotation prann = null;
           if (printAnnotations != null) {
               if (printAnnotations.size() == 0) {
                   return 0;
               }
               for (int i = 0; i < printAnnotations.size(); i++) {
                   prann = (InterlinearAnnotation) printAnnotations.get(i);
                   if (prann.x < xMin) {
                       xMin = prann.x;
                   }
               }
           }
           return xMin;
       }
     */

    /**
     * Return the minumum x value of the annotations in the list.
     *
     * @param printNodes the printannotations
     * @param shiftMap DOCUMENT ME!
     */

    /*
       private int getMinimumXValueOfNodes(ArrayList printNodes) {
           int xMin = Integer.MAX_VALUE;
           InterlinearAnnotation prann = null;
           DefaultMutableTreeNode node = null;
           if (printNodes != null) {
               if (printNodes.size() == 0) {
                   return 0;
               }
               for (int i = 0; i < printNodes.size(); i++) {
                   node = (DefaultMutableTreeNode) printNodes.get(i);
                   prann = (InterlinearAnnotation) node.getUserObject();
                   //System.out.println("Level: " + node.getLevel() + " A: " + prann + " T: " + prann.getTierName());
                   if (prann.x < xMin) {
                       xMin = prann.x;
                   }
               }
           }
           return xMin;
       }
     */

    /**
     * Calculates the horizontal shift for blockwise wrapping, one value for
     * each  subtree present in the List of nodes that still have to be placed
     * on a print block. So, annotations on a descendant tier inherit the
     * shift value from the 'highest'  ancestor present.
     *
     * @param printNodes the PrintAnnotations that have to be placed in a block
     * @param shiftMap the destination for the tiername - x-shift pairs
     */
    private void getXShiftPerTopNode(List<DefaultMutableTreeNode> printNodes, HashMap<String, Integer> shiftMap) {
        shiftMap.clear();

        InterlinearAnnotation prann = null;
        InterlinearAnnotation otherAnn = null;
        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode otherNode = null;
        List<DefaultMutableTreeNode> tempPerTier = new ArrayList<DefaultMutableTreeNode>();

        if (printNodes != null) {
            if (printNodes.size() == 0) {
                return;
            }

            for (int i = 0; i < printNodes.size(); i++) {
                node = printNodes.get(i);
                prann = (InterlinearAnnotation) node.getUserObject();

                //System.out.println("Level: " + node.getLevel() + " A: " + prann + " T: " + prann.getTierName());
                if (!shiftMap.containsKey(prann.getTierName())) {
                    shiftMap.put(prann.getTierName(), Integer.valueOf(prann.x));
                    tempPerTier.add(node);
                }
            }

            for (int i = 0; i < tempPerTier.size(); i++) {
                node = tempPerTier.get(i);

                for (int j = 0; j < tempPerTier.size(); j++) {
                    otherNode = tempPerTier.get(j);

                    if (node != otherNode) {
                        prann = (InterlinearAnnotation) node.getUserObject();
                        otherAnn = (InterlinearAnnotation) otherNode.getUserObject();

                        if (isTierAncestor(prann.getTierName(),
                                    otherAnn.getTierName())) {
                            shiftMap.put(otherAnn.getTierName(),
                            		Integer.valueOf(prann.x));
                        }
                    }
                }
            }
        }
    }

    /**
     * Polls the tier tree that has been build from the visible tiers for the
     * relationship between two tiers. Is the first an ancestor of the second.
     *
     * @param tier the first tier
     * @param otherTier the second tier
     *
     * @return true if the first tier is an ancestor of the second tier
     */
    private boolean isTierAncestor(String tier, String otherTier) {
        if (tierTree == null) {
            return false;
        }

        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode otherNode = null;
        DefaultMutableTreeNode temp;
        Enumeration en = tierTree.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            temp = (DefaultMutableTreeNode) en.nextElement();

            if (temp.getUserObject() == tier) {
                node = temp;
            }

            if (temp.getUserObject() == otherTier) {
                otherNode = temp;
            }
        }

        if ((node == null) || (otherNode == null)) {
            //System.out.println("Node: " + node + " other " + otherNode);

            return false;
        }

        return otherNode.isNodeAncestor(node);
    }

    /**
     * Returns a list of PrintAnnotations, all the first child on their own
     * child tier  of the  parent tier.
     *
     * @param root the root containing the root annotation
     *
     * @return a list of InterlinearAnnotation objects, all the first child on
     *         their own tier
     */
    private ArrayList<InterlinearAnnotation> getFirstChildrenOfRoot(DefaultMutableTreeNode root) {
        ArrayList<InterlinearAnnotation> list = new ArrayList<InterlinearAnnotation>();
        InterlinearAnnotation prann = null;

        if (root != null) {
            prann = (InterlinearAnnotation) root.getUserObject();

            DefaultMutableTreeNode otherNode = null;
            String tierName = null;
            String lastTierName = null;

            for (int i = 0; i < root.getChildCount(); i++) {
                otherNode = (DefaultMutableTreeNode) root.getChildAt(i);
                prann = (InterlinearAnnotation) otherNode.getUserObject();
                tierName = prann.getTierName();

                if (tierName != lastTierName) {
                    list.add(prann);
                    lastTierName = tierName;
                }
            }
        }

        return list;
    }

    /**
     * Calculates the realwidth of the root annotation, its symbolic
     * association depending  annotations and the first child annotations on
     * subdivision depending tiers.
     *
     * @param root the root node containing the root annotation
     *
     * @return the maximum width of the relevant annotations
     */
    private int getMaximumWidthOfFirstChildren(DefaultMutableTreeNode root) {
        int width = 0;
        InterlinearAnnotation prann = null;

        if (root != null) {
            prann = (InterlinearAnnotation) root.getUserObject();
            width = prann.realWidth;

            DefaultMutableTreeNode otherNode = null;
            String tierName = null;
            String lastTierName = null;

            for (int i = 0; i < root.getChildCount(); i++) {
                otherNode = (DefaultMutableTreeNode) root.getChildAt(i);
                prann = (InterlinearAnnotation) otherNode.getUserObject();
                tierName = prann.getTierName();

                if (tierName != lastTierName) {
                    int cw = prann.realWidth;

                    if (cw > width) {
                        width = cw;
                    }

                    lastTierName = tierName;
                }
            }
        }

        return width;
    }

    /**
     * Fill empty spaces on interlinear tiers with hidden/empty Interlinear annotations for proper 
     * conversion to html tables. Hidden annotations are created by using the one arg constructor
     * with arg null.
     */
    private void addHiddenCellsAndColSpan() {
        if (printBlocks.size() == 0) {
            return;
        }
        InterlinearBlock block;
        InterlinearTier pt;
        InterlinearAnnotation ia, iaPrev, nextIA;
        
        int maxw = 0;
        int pw = interlinearizer.getWidth() - getLeftMargin();
        for (int i = 0; i < printBlocks.size(); i++) {
            block = printBlocks.get(i);
            maxw = 0;
            for (int j = 0 ; j < block.getPrintTiers().size(); j++) {
                pt = block.getPrintTiers().get(j);
                if (pt.getAnnotations().size() > 0) {
                    ia = pt.getAnnotations().get(pt.getAnnotations().size() - 1);
                    if (ia.x + ia.realWidth > maxw) {
                        maxw = ia.x + ia.realWidth;
                    }
                }
                if (pt.getPrintWidth() > maxw && !(pt.getPrintWidth() > pw)) {
                    //maxw = pt.getPrintWidth();
                }
            }
            
            for (int j = 0 ; j < block.getPrintTiers().size(); j++) {
                pt = block.getPrintTiers().get(j);
                
                for (int k = pt.getAnnotations().size() - 1; k >= 0; k--) {
                    ia = pt.getAnnotations().get(k);
                    
                    if (k == pt.getAnnotations().size() - 1) {
                        if (ia.x + ia.calcWidth + interlinearizer.getEmptySpace() < maxw) {
                            // add empty interlinear annotation to end
                            nextIA = new InterlinearAnnotation(pt.getTierName(), InterlinearAnnotation.ASSOCIATION);
                            nextIA.x = ia.x + ia.calcWidth + interlinearizer.getEmptySpace();
                            nextIA.calcWidth = maxw - nextIA.x;
                            nextIA.hidden = true;
                            pt.getAnnotations().add(nextIA);
                        }
                    } 
                    if (k == 0) {
                        if (ia.x > interlinearizer.getEmptySpace()) {
                            nextIA = new InterlinearAnnotation(pt.getTierName(), InterlinearAnnotation.ASSOCIATION);
                            nextIA.x = 0;
                            nextIA.calcWidth = ia.x - interlinearizer.getEmptySpace();
                            nextIA.hidden = true;
                            pt.getAnnotations().add(0, nextIA);
                        }
                    } else {
                        // compare 2 adjacent annotations
                        iaPrev = pt.getAnnotations().get(k - 1);
                        if (ia.x > iaPrev.x + iaPrev.calcWidth + (2 * interlinearizer.getEmptySpace())) {
                            // insert empty annotation
                            nextIA = new InterlinearAnnotation(pt.getTierName(), InterlinearAnnotation.ASSOCIATION);
                            nextIA.x = iaPrev.x + iaPrev.calcWidth + interlinearizer.getEmptySpace();
                            nextIA.calcWidth = ia.x - interlinearizer.getEmptySpace() - nextIA.x;
                            nextIA.hidden = true;
                            pt.getAnnotations().add(k, nextIA);
                        }
                    }
                }
            }
            
            //printoutPrintBlock(block);
            // calculate the html colspan for each annotation
            ArrayList<Integer> xValues = new ArrayList<Integer>();
            //InterlinearTier it = null;
            Integer xInt;
            maxw = pw;
            
            for (int k = 0; k < block.getPrintTiers().size(); k++) {
                pt = block.getPrintTiers().get(k);
                for (int j = 0; j < pt.getAnnotations().size(); j++) {
                    ia = pt.getAnnotations().get(j);
                    
                    /*int w = Math.min((ia.x + ia.calcWidth), maxw);
                    if (j == it.getAnnotations().size() - 1) {
                        w = maxw;
                    } else {
                        w += interlinear.getEmptySpace();
                    }*/
                    
                    xInt = Integer.valueOf(ia.x);
                    if (!xValues.contains(xInt)) {
                        xValues.add(xInt);
                    }
                }
            }
            // add the last width value
            xInt = Integer.valueOf(maxw);
            if (!xValues.contains(xInt)) {
                xValues.add(xInt);
            }
            Collections.sort(xValues);
            
           // concatenate values that are close together
            int lastv = 0;
            for (int k = 1; k < xValues.size(); k++) {
                xInt = xValues.get(k);
                if (xInt.intValue() - lastv <= interlinearizer.getEmptySpace()) {
                    // check if it isn't part of a small interval
                    xValues.remove(k);
                    k--;
                    continue;
                }
                
                lastv = xInt.intValue();
            }
            int[] xval = new int[xValues.size()];
            for (int k = 0 ; k < xval.length; k++) {
                xInt = xValues.get(k);
                xval[k] = xInt.intValue();
            }
            
            int b, e, col, usedCol;
            for (int n = 0; n < block.getPrintTiers().size(); n++) {
                pt = block.getPrintTiers().get(n);
                
                usedCol = 0;
                for (int j = 0; j < pt.getAnnotations().size(); j++) {
                    ia = pt.getAnnotations().get(j);
                    col = 1; b = 0; e = 0;
                    int w = Math.min((ia.x + ia.calcWidth), maxw);
                    if (j == pt.getAnnotations().size() - 1) {
                        // make sure the last annotation fills the rest of the columns
                        col = xval.length - 1 - usedCol;
                    } else {
                        w += interlinearizer.getEmptySpace();
	    	                // find begin and end index
	    	                for (int k = usedCol; k < xval.length; k++) {
	    	                    if (Math.abs(ia.x - xval[k]) <= interlinearizer.getEmptySpace()) {
	    	                        b = k;
	    	                        continue;
	    	                    }
	    	                    if (Math.abs(w - xval[k]) <= interlinearizer.getEmptySpace()) {
	    	                        e = k;
	    	                        break;
	    	                    }
	    	                }
	    	                col = e - b;
	    	                if (col < 1) {
	    	                    //System.out.println("negative colspan...: " + col + " b: " + b + " e: " + e);
	    	                    col = 1;
	    	                }
	    	                usedCol += col;
                    }
                    // store the colspan 
                    ia.colSpan = col;             
                }
            }
        }
        
    }
    
    /**
     * Returns the number of non-spacing characters in a string.
     * 
     * @param value the string to test
     * @return the number of non-spacing characters
     */
    private int getNumNonSpacingCharacters(String value) {
    	if (value == null || value.length() == 0) {
    		return 0;
    	}
    	int count = 0;
    	
        char[] chars = value.toCharArray();

        for (char c : chars) {
            if (isNonSpacing(c)) {
            	count++;
            }
        }
    	return count;
    }
    
    /**
     * Returns the number of extra bytes in a string. 
     * Per character that is the number of bytes more than 2 bytes.
     * 
     * @param value the string to test
     * @return the number of extra bytes
     */
    private int getNumExtraBytes(String value) {
    	if (value == null || value.length() == 0) {
    		return 0;
    	}
    	int count = 0;
    	
        char[] chars = value.toCharArray();

        for (char c : chars) {
            count += getNumExtraBytes(c);
        }
        
    	return count;
    }
    
    /**
     * Check whether the character is a non-spacing character. This is needed
     * to overcome the discrepancy  between the length() of a Java string on
     * the one hand and the interlinear alignment based on the  "rendered"
     * number of positions of words. E.g. "Yo(Combining Ring Below)" (\u0059
     * \u006f \u0325) (if that would be a word)  consists of 3 characters
     * (length=3) bu occupies 2 positions in the interlinear alignment.
     *
     * @param c the character
     *
     * @return true if the character is non-spacing
     */
    private boolean isNonSpacing(char c) {
        int type = Character.getType(c);

        return ((type == Character.NON_SPACING_MARK) ||
        (type == Character.ENCLOSING_MARK) ||
        (type == Character.COMBINING_SPACING_MARK));
    }
    
    /**
     * Returns the number of extra bytes used for a character. 
     * The character alignment is in some cases based on the number of bytes.
     *
     * @param c the character
     *
     * @return 1 (extra) for a 2 bytes character, 2 (extra) for a 3 bytes
     *         character
     */
    private int getNumExtraBytes(char c) {
        if ((c == '\u0000') || ((c >= '\u0080') && (c <= '\u07ff'))) { // 2 bytes
                                                                       //System.out.println("2b: "+ c + "  " +Integer.toHexString(c));

            return 1;
        } else if ((c >= '\u0800') && (c <= '\uffff')) { // 3 bytes
                                                         //System.out.println("3b: "+ c + "  " +Integer.toHexString(c));

            return 2;
        }

        return 0;
    }
 
    /**
     * Returns the tier with the specified name.
     * @param name the name of the tier
     * @return the tier
     */
    Tier getTierWithId(String name) {
    	// TO DO hier... op verschillende plekken hierboven transcription.getTierWithId(name)
    	// vervangen door een call naar deze methode
    	return transcription.getTierWithId(name);
    }
    
    /**
     * Sets and stores the height of the specified tier.
     *
     * @param tierName the name of the tier
     * @param tierHeight the height of the tier
     */
    public void setTierHeight(String tierName, int tierHeight) {
        tierHeights.put(tierName, Integer.valueOf(tierHeight));
    }

    /**
     * Returns the height of the specified tier.
     *
     * @param forTier the tier
     *
     * @return the height for the tier
     */
    public int getTierHeight(String forTier) {
        Integer i = tierHeights.get(forTier);

        if (i != null) {
            return i.intValue();
        } else {
            return 0;
        }
    }

    /**
     * Returns the left margin (i.e. the margin for the tier name labels). The
     * width of the margin is based on the widest tier label
     *
     * @return the width of the margin, in pixels or number of characters
     */
    public int getLeftMargin() {
        if (interlinearizer.isTierLabelsShown()) {
            return leftMargin;
        } else {
            return 0;
        }
    }

    /**
     * Sets the width of the tier label margin.
     *
     * @param i the new width of the label margin
     */
    public void setLeftMargin(int i) {
        leftMargin = i;
    }

    /**
     * A Comparator for
     * <code>DefaultMutableTreeNode</code> objects holding a
     * InterlinearAnnotation.
     *
     * @author HS
     * @version 1.0
     */
    class AnnotationNodeComparator implements Comparator<DefaultMutableTreeNode> {
        /**
         * Compares (root) annotations/InterlinearAnnotations based on begin
         * and end time. If both are the same check whether the root tiers are
         * visible and which one comes first in the ordering.
         *
         * @param o1 the first DefaultMutableTreeNode
         *        containing a InterlinearAnnotation
         * @param o2 the second DefaultMutableTreeNode
         *        containing a InterlinearAnnotation
         *
         * @return -1 if o1 is 'smaller', 1 if o2 is 'smaller' or else 0
         *
         * @throws ClassCastException when either of the params is not a
         *         InterlinearAnnotation or  DefaultMutableTreeNode
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
            InterlinearAnnotation a1 = (InterlinearAnnotation) o1.getUserObject();
            InterlinearAnnotation a2 = (InterlinearAnnotation) o2.getUserObject();

            if (a1.bt < a2.bt) {
                return -1;
            }

            if (a1.bt > a2.bt) {
                return 1;
            }

            if (a1.et < a2.et) {
                return -1;
            }

            if (a1.et > a2.et) {
                return 1;
            }

            // if all is equal we could check on the position in the visible tier list

            return 0;
        }
    }
    
//    /**
//     * A Comparator for <code>InterlinearAnnotation</code> objects or
//     * <code>DefaultMutableTreeNode</code> objects holding a
//     * InterlinearAnnotation.
//     *
//     * @author HS
//     * @version 1.0
//     */
//    class PrintAnnotationComparator implements Comparator {
//        /**
//         * Compares (root) annotations/InterlinearAnnotations based on begin
//         * and end time. If both are the same check whether the root tiers are
//         * visible and which one comes first in the ordering.
//         *
//         * @param o1 the first InterlinearAnnotation or DefaultMutableTreeNode
//         *        containing a InterlinearAnnotation
//         * @param o2 the second InterlinearAnnotation or DefaultMutableTreeNode
//         *        containing a InterlinearAnnotation
//         *
//         * @return -1 if o1 is 'smaller', 1 if o2 is 'smaller' or else 0
//         *
//         * @throws ClassCastException when either of the params is not a
//         *         InterlinearAnnotation or  DefaultMutableTreeNode
//         *
//         * @see java.util.Comparator#compare(java.lang.Object,
//         *      java.lang.Object)
//         */
//        @Override
//		public int compare(Object o1, Object o2) {
//            if ((!(o1 instanceof InterlinearAnnotation) &&
//                    !(o1 instanceof DefaultMutableTreeNode)) ||
//                    (!(o2 instanceof InterlinearAnnotation) &&
//                    !(o2 instanceof DefaultMutableTreeNode))) {
//                System.out.println("O1: " + o1.getClass().getName());
//                System.out.println("O2: " + o2.getClass().getName());
//                throw new ClassCastException(
//                    "Objects should either be of type InterlinearAnnotation or DefaultMutableTreeNode");
//            }
//
//            InterlinearAnnotation a1;
//            InterlinearAnnotation a2;
//
//            if (o1 instanceof InterlinearAnnotation) {
//                a1 = (InterlinearAnnotation) o1;
//            } else {
//                a1 = (InterlinearAnnotation) ((DefaultMutableTreeNode) o1).getUserObject();
//            }
//
//            if (o2 instanceof InterlinearAnnotation) {
//                a2 = (InterlinearAnnotation) o2;
//            } else {
//                a2 = (InterlinearAnnotation) ((DefaultMutableTreeNode) o2).getUserObject();
//            }
//
//            if (a1.bt < a2.bt) {
//                return -1;
//            }
//
//            if (a1.bt > a2.bt) {
//                return 1;
//            }
//
//            if (a1.bt == a2.bt) {
//                if (a1.et < a2.et) {
//                    return -1;
//                }
//
//                if (a1.et > a2.et) {
//                    return 1;
//                }
//
//                // if all is equal we could check on the position in the visible tier list
//            }
//
//            return 0;
//        }
//    }
}
