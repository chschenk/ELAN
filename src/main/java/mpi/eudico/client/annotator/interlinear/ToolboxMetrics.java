package mpi.eudico.client.annotator.interlinear;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeFormatter;


/**
 * A Metrics class that extends the BlockMetric class by <br>
 * - adding a mapping between  transcription per participant tiernames
 * (ref@nnn) to toolbox marker names, <br>
 * - postprocessing of the annotation blocks (tier-marker name replacement)<br>
 * - distributing time codes over ELANBegin and ELANEnd markers
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxMetrics extends BlockMetrics {
    private final String ELAN_BEGIN = "ELANBegin";
    private final String ELAN_END = "ELANEnd";
    private final String ELAN_PART = "ELANParticipant";
    private final String at = "@";
    private final String UNKNOWN = "unknown";
    private Map<String, String> tierToMarkerMap;
    private List<String> levelOneNonInterTiers;
    private List<String> markerNames;
    private List<String> markersWithBlank;
    private Tier beginTier;
    private Tier endTier;
    private Tier partTier;
    private Tier mediaTier;
    private String mediaMarker;
    private String mediaURL;

    /**
     * a list of ToolboxRecordBlocks to return instead of the regular print
     * blocks (in some cases
     */
    private ArrayList<ToolboxRecordBlock> toolboxBlocks;

    /**
     * Constructor
     *
     * @param interlinearizer
     */
    public ToolboxMetrics(Interlinear interlinearizer) {
        super(interlinearizer);
        // to be sure
        interlinearizer.setTimeCodeShown(false);
        tierToMarkerMap = new HashMap<String, String>();
        markerNames = new ArrayList<String>();
        markersWithBlank = new ArrayList<String>();
        levelOneNonInterTiers = new ArrayList<String>();
        toolboxBlocks = new ArrayList<ToolboxRecordBlock>();
    }

    /**
     * Sets the names of the markers that should be included in the output.
     * This are Toolbox marker names, so tier names like ref@nnn, ref@mmm are
     * flattened to one marker name. In this method the corresponding
     * transcription tier(name)s are resolved. <br>
     * This method should be called before {@link
     * #calculateAnnotationBlocks(Graphics)}
     *
     * @param markerNames the list of output marker names
     */
    public void setVisibleMarkerNames(List<String> markerNames) {
        if (markerNames == null) {
            interlinearizer.setVisibleTiers(new ArrayList<Tier>(
                    transcription.getTiers()));
            visibleTiers = interlinearizer.getVisibleTiers();
        } else {
            this.markerNames = markerNames;
            visibleTiers = new ArrayList<Tier>();

            List<Tier> allTiers = new ArrayList<Tier>(transcription.getTiers());
            String name = null;
            String tierName = null;
            TierImpl tier = null;
            int atIndex = -1;

            for (int i = 0; i < markerNames.size(); i++) {
                name = markerNames.get(i);

                if (name.equals(ELAN_BEGIN)) {
                    beginTier = new TierImpl(name, null, null, null);

                    continue;
                }

                if (name.equals(ELAN_END)) {
                    endTier = new TierImpl(name, null, null, null);

                    continue;
                }

                if (name.equals(ELAN_PART)) {
                    partTier = new TierImpl(name, null, null, null);

                    continue;
                }
            }

            for (int j = 0; j < allTiers.size(); j++) {
                tier = (TierImpl) allTiers.get(j);
                tierName = tier.getName();
                atIndex = tierName.indexOf(at);

                if (atIndex > 0) {
                    tierName = tierName.substring(0, atIndex);

                    if (markerNames.contains(tierName)) {
                        visibleTiers.add(tier);
                        tierToMarkerMap.put(tier.getName(), tierName);
                    }
                } else {
                    if (markerNames.contains(tierName)) {
                        visibleTiers.add(tier);
                        tierToMarkerMap.put(tierName, tierName);

                        // replace spaces at the moment of writing
                        //tierToMarkerMap.put(tierName, tierName.replace(' ', '_'));
                    }
                }
            }

            interlinearizer.setVisibleTiers(visibleTiers);
        }
    }

    /**
     * Sets the markers that are followed by a blank line.
     *
     * @param markersWithBlank list of markers followed by a blank line
     */
    public void setMarkersWithBlankLine(List<String> markersWithBlank) {
        if (markersWithBlank != null) {
            this.markersWithBlank = markersWithBlank;
        }
    }

    /**
     * Returns whether or not this marker should be followed by a blank line.
     *
     * @param markerName the name of the marker
     *
     * @return true if a blank line should be inserted, false otherwise
     */
    public boolean isMarkerWithBlankLine(String markerName) {
        if ((markersWithBlank != null) && (markerName != null)) {
            return markersWithBlank.contains(markerName);
        }

        return false;
    }

    /**
     * After creating a treebased block in the usual way, tiernames in the
     * nodes are replaced by the flattened Toolbox marker names. This way the
     * creation of the print blocks will use the output names
     *
     * @param g the graphics object (== null, in this case)
     */
    @Override
	public void calculateAnnotationBlocks(Graphics g) {
        super.calculateAnnotationBlocks(g);

        // post process everything in annotationBlocks
        // replace tier names by marker names, add nodes for begin, end and participant
        // if necessary, add time offset to bt and et
        DefaultMutableTreeNode curNode = null;
        DefaultMutableTreeNode rootNode = null;
        InterlinearAnnotation curAnn = null;
        String tierName;
        String nextTierName;
        long bt = 0;
        long et = 0;
        String part;

        for (int i = 0; i < annotationBlocks.size(); i++) {
            part = null;
            rootNode = annotationBlocks.get(i);
            
            Enumeration en = rootNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                curNode = (DefaultMutableTreeNode) en.nextElement();
                curAnn = (InterlinearAnnotation) curNode.getUserObject();
                tierName = curAnn.getTierName();
                nextTierName = tierToMarkerMap.get(tierName);

                if ((curNode.getLevel() == 1) &&
                        (curAnn.type == InterlinearAnnotation.ASSOCIATION)) {
                    if (!levelOneNonInterTiers.contains(nextTierName)) {
                        levelOneNonInterTiers.add(nextTierName);
                    }
                }

                if (curNode.isRoot()) {
                    bt = curAnn.bt;
                    et = curAnn.et;

                    int index = tierName.indexOf(at);

                    if ((index > -1) && (index < (tierName.length() - 1))) {
                        part = tierName.substring(index + 1);
                    }
                }

                curAnn.setTierName(nextTierName);
            }

            if (markerNames.contains(ELAN_BEGIN)) {
                InterlinearAnnotation la = new InterlinearAnnotation(ELAN_BEGIN,
                        InterlinearAnnotation.ASSOCIATION);

                if (interlinearizer.getTimeCodeType() == Interlinear.HHMMSSMS) {
                    la.setValue(TimeFormatter.toString(bt +
                            interlinearizer.getTimeOffset()));
                } else if (interlinearizer.getTimeCodeType() == Interlinear.SSMS) {
                    la.setValue(TimeFormatter.toSSMSString(bt +
                            interlinearizer.getTimeOffset()));
                } else {
                    la.setValue(String.valueOf(bt +
                            interlinearizer.getTimeOffset()));
                }

                rootNode.add(new DefaultMutableTreeNode(la));
            }

            if (markerNames.contains(ELAN_END)) {
                InterlinearAnnotation la = new InterlinearAnnotation(ELAN_END,
                        InterlinearAnnotation.ASSOCIATION);

                if (interlinearizer.getTimeCodeType() == Interlinear.HHMMSSMS) {
                    la.setValue(TimeFormatter.toString(et +
                            interlinearizer.getTimeOffset()));
                } else if (interlinearizer.getTimeCodeType() == Interlinear.SSMS) {
                    la.setValue(TimeFormatter.toSSMSString(et +
                            interlinearizer.getTimeOffset()));
                } else {
                    la.setValue(String.valueOf(et +
                            interlinearizer.getTimeOffset()));
                }

                rootNode.add(new DefaultMutableTreeNode(la));
            }

            if (markerNames.contains(ELAN_PART) && (part != null)) {
                InterlinearAnnotation la = new InterlinearAnnotation(ELAN_PART,
                        InterlinearAnnotation.ASSOCIATION);
                if (part != null) {
                	la.setValue(part);
                } else {
                	la.setValue(UNKNOWN);
                }
                
                rootNode.add(new DefaultMutableTreeNode(la));
            }
            
            if (mediaMarker != null && mediaURL != null) {
            	InterlinearAnnotation la = new InterlinearAnnotation(mediaMarker,
                        InterlinearAnnotation.ASSOCIATION);
            	la.setValue(mediaURL + " " + TimeFormatter.toSSMSString(bt +
                            interlinearizer.getTimeOffset()) + " " + 
                            TimeFormatter.toSSMSString(et + interlinearizer.getTimeOffset()));
            	
            	rootNode.add(new DefaultMutableTreeNode(la));
            }
        }

        // add the dummy tiers to the visible tiers, to prevent exceptions in 
        // calculatePrintBlocks
        if (beginTier != null) {
            visibleTiers.add(beginTier);
        }

        if (endTier != null) {
            visibleTiers.add(endTier);
        }

        if (partTier != null) {
            visibleTiers.add(partTier);
        }

        // replace tierTemplate
        if (markerNames != null) {
            tierTemplate.clear();
            tierTemplate.addAll(markerNames);
        }

        // call calculatePrintBlocks from the Renderer
    }

    // override calculatePrintBlocks to fix linewrapping (only NEXT LINE is currently 
    // supported) and add a custom recordmarker if necessary?? and empty lines??
    /**
     * Performs postprocessing in case of line wrapping to the end of the
     * block. The wrapped part of the interlinearization has been moved to
     * next print blocks. Lines have been wrapped to the next line (within an
     * InterlinearAnnotation). Move lines with index > 1 to the next block(s).
     * Also forces tiers/markers that are not part of the interlinearization
     * and occur after the first tier/marker in the interlinearized block, to
     * the last block.
     */
    @Override
	public void calculatePrintBlocks() {
        super.calculatePrintBlocks();

        if ( /*(interlinearizer.getLineWrapStyle() == Interlinear.END_OF_BLOCK) ||*/
            (interlinearizer.getWidth() != Integer.MAX_VALUE) || interlinearizer.getOutputMode() == Interlinear.SHOEBOX_TEXT) {
            toolboxBlocks.clear();
            toolboxBlocks.ensureCapacity(printBlocks.size());

            List<InterlinearBlock> group = new ArrayList<InterlinearBlock>(3);
            InterlinearBlock printBlock = null;
            ToolboxRecordBlock trb = null;

            for (int i = 0; i < printBlocks.size(); i++) {
                printBlock = printBlocks.get(i);

                if (printBlock.isStartOfAnnotationBlock()) {
                    if (group.size() > 1) {
                        //wrapToEndOfBlock(group);
                        trb = convertToToolboxBlock(group,
                                interlinearizer.getLineWrapStyle());

                        if (trb != null) {
                            toolboxBlocks.add(trb);
                        }
                    } else if (group.size() == 1) {
                        trb = convertToToolboxBlock(group.get(
                                    0), interlinearizer.getLineWrapStyle());

                        if (trb != null) {
                            toolboxBlocks.add(trb);
                        }
                    }

                    group.clear();
                }

                group.add(printBlock);
            }
            //  check if the last block has been added
            if (group.size() > 1) {
                //wrapToEndOfBlock(group);
                trb = convertToToolboxBlock(group,
                        interlinearizer.getLineWrapStyle());

                if (trb != null) {
                    toolboxBlocks.add(trb);
                }
            } else if (group.size() == 1) {
                trb = convertToToolboxBlock(group.get(
                            0), interlinearizer.getLineWrapStyle());

                if (trb != null) {
                    toolboxBlocks.add(trb);
                }
            }
        }
    }

    /**
     * This moves wrapped lines to the last print block in the group.
     * Annotations with level 1 that occur after the "interlinear block" in
     * the first printblock of the group are also moved to the last block.
     *
     * @param blocks a group of print blocks representing a single Toolbox
     *        record
     */
    private void wrapToEndOfBlock(List<InterlinearBlock> blocks) {
        if ((blocks == null) || (blocks.size() <= 1)) {
            return;
        }

        InterlinearBlock printBlock = blocks.get(0);
        List<InterlinearTier> tiers = printBlock.getPrintTiers();
        InterlinearTier pt = null;
        InterlinearTier nextPt = null;
        InterlinearAnnotation pa = null;
        String[] lines = null;
        boolean interlinearBlockStarted = false;

        for (int i = 0; i < tiers.size(); i++) {
            pt = tiers.get(i);

            if ((i > 0) && !interlinearBlockStarted &&
                    !levelOneNonInterTiers.contains(pt.getTierName())) {
                interlinearBlockStarted = true;

                //System.out.println("Interl start: " + pt.getTierName());
                continue;
            }

            if (interlinearBlockStarted &&
                    levelOneNonInterTiers.contains(pt.getTierName())) {
                // move annotation to last block
                pa = pt.getAnnotations().get(0);
                nextPt = blocks.get(blocks.size() - 1).getPrintTier(pt.getTierName());

                if ((nextPt != null) && (nextPt.getAnnotations().size() == 0)) {
                    nextPt.addAnnotation(pa);
                    pt.getAnnotations().clear();
                    pt.setNumLines(1);
                }
            } else if (pt.getAnnotations().size() == 1) {
                pa = pt.getAnnotations().get(0);
                lines = pa.getLines();

                if ((lines != null) && (lines.length > 1)) {
                    nextPt = blocks.get(blocks.size() - 1).getPrintTier(pt.getTierName());

                    if ((nextPt != null) &&
                            (nextPt.getAnnotations().size() == 0)) {
                        InterlinearAnnotation nextAnn = new InterlinearAnnotation(nextPt.getTierName(),
                                InterlinearAnnotation.ASSOCIATION);

                        if (lines.length > 2) {
                            pa.setValue(lines[0]);
                            pa.setLines(null);

                            String[] restLines = new String[lines.length - 1];

                            for (int j = 1; j < lines.length; j++) {
                                restLines[j - 1] = lines[j];
                            }

                            nextAnn.setLines(restLines);
                        } else {
                            pa.setValue(lines[0]);
                            pa.setLines(null);
                            nextAnn.setValue(lines[1]);
                        }

                        nextPt.addAnnotation(nextAnn);
                    }
                }
            }
        }
    }

    /**
     * Converts a group of block-wise wrapped records to 1 toolbox record representation.
     *
     * @param blocks a group of interlinear blocks belonging to one record
     * @param lineWrapStyle END_OF_BLOCK or NEXT_LINE
     *
     * @return a toolbox record block
     */
    private ToolboxRecordBlock convertToToolboxBlock(List<InterlinearBlock> blocks,
        int lineWrapStyle) {
        if (blocks == null) {
            return null;
        }

        ToolboxRecordBlock trb = new ToolboxRecordBlock();

        InterlinearBlock printBlock = null;
        List<InterlinearTier> tiers = null;
        List<InterlinearTier> leftovers = new ArrayList<InterlinearTier>();
        InterlinearTier pt = null;
        InterlinearTier nextPt = null;
        InterlinearAnnotation pa = null;
        InterlinearAnnotation nextAnn = null;
        String[] lines = null;
        int insertIndex = -1;

        for (int k = 0; k < blocks.size(); k++) {
            printBlock = blocks.get(k);
            tiers = printBlock.getPrintTiers();

            if (k == 0) {
                // add all from the first block
                for (int i = 0; i < tiers.size(); i++) {
                    pt = tiers.get(i);
                    trb.addPrintTier(pt);
                }
            } else {
                // get the wrapped tiers and insert after the last wrapped tier
                leftovers.clear();

                for (int i = 0; i < tiers.size(); i++) {
                    pt = tiers.get(i);

                    if (pt.getAnnotations().size() > 0) {
                        leftovers.add(pt);
                    }
                }

                if (leftovers.size() > 0) {
                    pt = leftovers.get(leftovers.size() - 1);

                    if (insertIndex == -1) {
                        insertIndex = trb.lastIndexOfTier(pt.getTierName());
                        insertIndex++;
                    }

                    trb.insertEmptyPrintTier(insertIndex++);

                    for (int j = 0; j < leftovers.size(); j++) {
                        trb.insertPrintTier(insertIndex++,
                            leftovers.get(j));
                    }

                    if (k == (blocks.size() - 1)) {
                        trb.insertEmptyPrintTier(insertIndex++);
                    }
                }
            }
        }

        // line wrapping??
        if (lineWrapStyle == Interlinear.END_OF_BLOCK) {
            boolean emptyLineInserted = false;
            tiers = trb.getPrintTiers();

            int numTiers = tiers.size();
            InterlinearTier tiObj;

            for (int i = 0; i < numTiers; i++) {
                tiObj = tiers.get(i);

                if (tiObj instanceof EmptyPrintTier) {
                    continue;
                }

                pt = tiObj;

                if (pt.getAnnotations().size() == 1) {
                    pa = pt.getAnnotations().get(0);
                    lines = pa.getLines();

                    if ((lines != null) && (lines.length > 1)) {
                        nextPt = new InterlinearTier(pt.getTierName());
                        nextAnn = new InterlinearAnnotation(nextPt.getTierName(),
                                InterlinearAnnotation.ASSOCIATION);

                        if (lines.length > 2) {
                            pa.setValue(lines[0]);
                            pa.setLines(null);

                            String[] restLines = new String[lines.length - 1];

                            for (int j = 1; j < lines.length; j++) {
                                restLines[j - 1] = lines[j];
                            }

                            nextAnn.setLines(restLines);
                        } else {
                            pa.setValue(lines[0]);
                            pa.setLines(null);
                            nextAnn.setValue(lines[1]);
                        }

                        nextPt.addAnnotation(nextAnn);

                        if (!emptyLineInserted) {
                            trb.addEmptyPrintTier();
                            emptyLineInserted = true;
                        }

                        trb.addPrintTier(nextPt);
                    }
                }
            }
        }

        return trb;
    }

    /**
     * Converts a single interlinear block into a toolbox record block, by
     * justing moving the tiers to a new toolbox block if linewrapping is not
     * END_OF_BLOCK and otherwise by creating new printtiers and adding them
     * to the end of the block.
     *
     * @param printBlock the interlinear block
     * @param lineWrapStyle the line wrap style, next line or end of block
     *
     * @return a Toolbox record block
     */
    private ToolboxRecordBlock convertToToolboxBlock(
        InterlinearBlock printBlock, int lineWrapStyle) {
        if (printBlock == null) {
            return null;
        }

        ToolboxRecordBlock trb = new ToolboxRecordBlock();
        List<InterlinearTier> tiers = printBlock.getPrintTiers();
        InterlinearTier pt = null;
        InterlinearTier nextPt = null;
        InterlinearAnnotation pa = null;
        InterlinearAnnotation nextAnn = null;
        String[] lines = null;

        if (lineWrapStyle == Interlinear.END_OF_BLOCK) {
            List<InterlinearTier> tiersToAdd = new ArrayList<InterlinearTier>();

            for (int i = 0; i < tiers.size(); i++) {
                pt = tiers.get(i);

                if (pt.getAnnotations().size() == 1) {
                    pa = pt.getAnnotations().get(0);
                    lines = pa.getLines();

                    if (lines != null) {
                        if (lines.length <= 1) {
                            trb.addPrintTier(pt);
                        } else {
                            nextPt = new InterlinearTier(pt.getTierName());
                            nextAnn = new InterlinearAnnotation(nextPt.getTierName(),
                                    InterlinearAnnotation.ASSOCIATION);

                            if (lines.length > 2) {
                                pa.setValue(lines[0]);
                                pa.setLines(null);

                                String[] restLines = new String[lines.length -
                                    1];

                                for (int j = 1; j < lines.length; j++) {
                                    restLines[j - 1] = lines[j];
                                }

                                nextAnn.setLines(restLines);
                            } else {
                                pa.setValue(lines[0]);
                                pa.setLines(null);
                                nextAnn.setValue(lines[1]);
                            }

                            nextPt.addAnnotation(nextAnn);
                            tiersToAdd.add(nextPt);
                            trb.addPrintTier(pt);
                        }
                    } else {
                        trb.addPrintTier(pt);
                    }
                } else {
                    trb.addPrintTier(pt);
                }
            }

            // add the extra tiers
            if (tiersToAdd.size() > 0) {
                trb.addEmptyPrintTier();

                for (int i = 0; i < tiersToAdd.size(); i++) {
                    trb.addPrintTier(tiersToAdd.get(i));
                }
            }
        } else {
            // nothing to change, just create the toolbox record block
            for (int i = 0; i < tiers.size(); i++) {
                pt = tiers.get(i);
                trb.addPrintTier(pt);
            }
        }

        return trb;
    }

    /**
     * Find a tier that for the given name. Since the specified name might be
     * one of the marker names extracted from an ELAN tiername (ref@nnn), the
     * name is resolved and the tier returned. If the name is one of the
     * constants ELANBegin, ELANEnd or ELANParticipant one of the 'dummy'
     * tiers is returned.
     *
     * @param name the name of the tier
     *
     * @return the tier
     */
    @Override
	Tier getTierWithId(String name) {
        // first check the dummy tiers...
        if (name.equals(ELAN_BEGIN)) {
            return beginTier;
        }

        if (name.equals(ELAN_END)) {
            return endTier;
        }

        if (name.equals(ELAN_PART)) {
            return partTier;
        }
        
        if (name.equals(mediaMarker)) {
        	return mediaTier;
        }

        Tier t = super.getTierWithId(name);

        if (t == null) {
            Iterator<String> nameIt = tierToMarkerMap.keySet().iterator();
            String tierName;

            while (nameIt.hasNext()) {
                tierName = nameIt.next();

                if ((tierName.indexOf(at) > 0) && tierName.startsWith(name)) {
                    // the first one will do
                    t = transcription.getTierWithId(tierName);

                    break;
                }
            }
        }

        return t;
    }

    /**
     * Returns the Toolbox-records style blocks.
     *
     * @return the Toolbox-records style blocks or null if no toolbox blocks
     *         have been created
     */
    public List<ToolboxRecordBlock> getToolboxBlocks() {
        if (toolboxBlocks.size() == 0) {
            return null;
        }

        return toolboxBlocks;
    }

    /**
     * Sets the name for a media marker for Toolbox.
     * 
     * @param mediaMarker the marker name
     */
	public void setMediaMarker(String mediaMarker) {
		if (mediaMarker != null && mediaMarker.length() > 1) {
			if (mediaMarker.charAt(0) == '\\') {
				this.mediaMarker = mediaMarker.substring(1);
			} else {
				this.mediaMarker = mediaMarker;
			}
			
			
			if (mediaTier == null) {
				mediaTier = new TierImpl(this.mediaMarker, null, null, null);
				if (!markerNames.contains(this.mediaMarker)) {
					markerNames.add(this.mediaMarker);
				}
			}
		} else {
			this.mediaMarker = mediaMarker;
		}
	}

	/**
	 * Sets the url for the media file.
	 * @param mediaURL
	 */
	public void setMediaURL(String mediaURL) {
		this.mediaURL = mediaURL;
	}
}
