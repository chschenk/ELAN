/*
 * File:     RegularAnnotationCommand.java
 * Project:  MPI Linguistic Application
 * Date:     13 December 2006
 *
 * Feature added by Ouriel Grynszpan, European contract MATHESIS IST-027574
 * CNRS UMR 7593, Paris, France
 *
 * Copyright (C) 2001-2005  Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that creates regular annotations of equal size on a selected tiers
 *
 * @author Ouriel Grynszpan
 */
public class RegularAnnotationCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private TranscriptionImpl transcription;

    //Time specifications
    private long start;

    //User given end time
    private long end;

    //Computed end time
    private long realEnd;

    //annotations' size
    private long annSize;

    //Selected tiers
    private List<String> tierNames;
    private List<TierImpl> tiers;

    /** backup data of existing annotations on the tiers */
    private List<List<DefaultMutableTreeNode>> existAnnotationSets;
    private List<List<DefaultMutableTreeNode>> partiallyOverlappingAnnotations;
    private boolean firstRun = false;

    /**
     * Creates a new RegularAnnotationCommand instance.
     *
     * @param name the name of the command
     */
    public RegularAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * Undo the changes made by this command.
     */
    @Override
	public void undo() {
        if ((transcription == null) || (tiers == null) ||
                (existAnnotationSets == null)) {
            return;
        }

        if (tiers.size() != existAnnotationSets.size()) {
            LOG.severe("Error while undoing regular annotations.");

            return;
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);

        setWaitCursor(true);
        
        // remove created annotations
        for (int tierIdx = 0; tierIdx < tiers.size(); tierIdx++) {
            TierImpl tier = tiers.get(tierIdx);

            for (AbstractAnnotation ann : tier.getAnnotations()) {
                long annBT = ann.getBeginTimeBoundary();
                long annET = ann.getEndTimeBoundary();

//                if (((start <= annBT) && (annBT < end)) ||
//                        ((start < annET) && (annET <= end))) {
                if (annBT >= start && annET <= realEnd) {
                    tier.removeAnnotation(ann);
                }
            }
            
            List<DefaultMutableTreeNode> partials = partiallyOverlappingAnnotations.get(tierIdx);
            if (partials != null && partials.size() > 0) {
            	DefaultMutableTreeNode root = null;
            	
            	for (int i = 0; i < partials.size(); i ++) {
            		root = partials.get(i);
            		AnnotationDataRecord dataRecord = null;
            		AbstractAnnotation aa = null;
            		
            		if ( root != null) {
            			Object usObj = root.getUserObject();
            			if (usObj instanceof AnnotationDataRecord) {
            				dataRecord = (AnnotationDataRecord) usObj;
            				if (dataRecord.getBeginTime() < start) {
            					aa = (AbstractAnnotation) tier.getAnnotationAtTime(dataRecord.getBeginTime());
            				} else {
            					aa = (AbstractAnnotation) tier.getAnnotationAtTime(dataRecord.getEndTime() - 1);
            				}
            				// to be sure that depending annotations will be restores as well first delete
            				// the remainders of partially overlapping annotations
            				if (aa != null) {
            					tier.removeAnnotation(aa);
            				}
            			}
            		}
            	}
            	
            	for (int i = 0; i < partials.size(); i ++) {
            		root = partials.get(i);
            		
            		if ( root != null) {
            			AnnotationRecreator.createAnnotationFromTree(transcription, root, true);
            		}
            	}
            }

            List<DefaultMutableTreeNode> existAnn = existAnnotationSets.get(tierIdx);

            if ((existAnn != null) && (existAnn.size() > 0)) {
                AnnotationRecreator.createAnnotationsSequentially(transcription,
                    existAnn, true);
            }
        }

        transcription.setNotifying(true);

        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        if ((transcription == null) || (tiers == null)) {
            return;
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);

        setWaitCursor(true);

        new RegularAnnotationThread().start();

        transcription.setNotifying(true);

        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul><li>arg[0] = the start time
     *        (long)</li> <li>arg[1] = the end time (long) </li> <li>arg[2] =
     *        the annotations' size (long)</li> <li>arg[3] = the seleted
     *        tiers' names (Vector)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        //LOG.info("Starting RegularAnnotationCommand.execute");
        transcription = (TranscriptionImpl) receiver;

        start = ((Long) arguments[0]).longValue();
        end = ((Long) arguments[1]).longValue();
        annSize = ((Long) arguments[2]).longValue();
        tierNames = (List<String>) arguments[3];

        if ((null == tierNames) || (tierNames.isEmpty())) {
            LOG.severe("Error in retrieving the tiers.");

            return;
        }

        if (null == transcription) {
            LOG.severe("Error in retrieving the transcription.");

            return;
        }

        //Retrieving tiers
        tiers = new ArrayList<TierImpl>();

        Iterator<String> tierNamesIt = tierNames.iterator();

        while (tierNamesIt.hasNext()) {
            String tierName = tierNamesIt.next();

            if (null == tierName) {
                LOG.severe("Error in retrieving the tiers.");

                return;
            }

            TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
            tiers.add(tier);
        }

        //The difference between the real end time and the start time
        //must be a multiple of the annotation size
        realEnd = start + (((end - start) / annSize) * annSize);
        LOG.info("User given end time is " + end + "ms; computed end time is " +
            realEnd + "ms");

        //creating backup structure, could use Map instead
        existAnnotationSets = new ArrayList<List<DefaultMutableTreeNode>>();
        partiallyOverlappingAnnotations = new ArrayList<List<DefaultMutableTreeNode>>();

        for (int i = 0; i < tiers.size(); i++) {
            existAnnotationSets.add(new ArrayList<DefaultMutableTreeNode>());
            partiallyOverlappingAnnotations.add(new ArrayList<DefaultMutableTreeNode>());
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        setWaitCursor(true);

        new RegularAnnotationThread().start();

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

        setWaitCursor(false);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }

    ///////////////////////////////////////////
    // inner class: execution thread
    ///////////////////////////////////////////
    /**
     * Class that handles the regular annotations in a separate thread.
     *
     * @author Ouriel Grynszpan
     */
    private class RegularAnnotationThread extends Thread {
        /**
         * Creates regular annotation for each selected tier and stores
         * previously existing annotations in an element of
         * existAnnotationSets
         */
        RegularAnnotationThread() {
        }

        /**
         * The actual creation of annotations.
         */
        @Override
		public void run() {
            final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(ELANCommandFactory.getRootFrame(
                        transcription), true,
                    ElanLocale.getString(
                        "RegularAnnotationDialog.Message.Annotating"), true,
                    ElanLocale.getString("Button.Cancel"));

            // if we are blocking (modal) call show from a separate thread
            new Thread(new Runnable() {
                    @Override
					public void run() {
                        monitor.show();
                    }
                }).start();

            RegularAnnotationCommand.this.transcription.setNotifying(false);
            ArrayList<AbstractAnnotation> removables = new ArrayList<AbstractAnnotation>();

            for (int tierIdx = 0; tierIdx < tiers.size(); tierIdx++) {
                TierImpl tier = tiers.get(tierIdx);
                removables.clear();
                
                LOG.info("Creating regular annotations for " + tier.getName() +
                    "...");

                // previously existing annotations for the current tier are stored
                // in the element of existAnnotationSets that has the same index
                // as the current tier in tiers vector.
                List<DefaultMutableTreeNode> existAnnos = existAnnotationSets.get(tierIdx);
                List<DefaultMutableTreeNode> partOverAnnos = partiallyOverlappingAnnotations.get(tierIdx);
                List<AbstractAnnotation> annos = tier.getAnnotations();

                //Storing previously existing annotations
                Iterator<AbstractAnnotation> annosIt = annos.iterator();

                while (annosIt.hasNext()) {
                    AbstractAnnotation ann = annosIt.next();
                    long annBT = ann.getBeginTimeBoundary();
                    long annET = ann.getEndTimeBoundary();
                    // check for overlap, inclusion for deleting, partial overlap for later
                    if (annBT >= start && annET <= realEnd) {// inclusion
                    	if (!firstRun) {
                    		existAnnos.add(AnnotationRecreator.createTreeForAnnotation(
                                ann));
                    	}
                        removables.add(ann);
                    } else if ((annBT < start && annET > start) || (annBT < realEnd && annET > realEnd)) { //partial
                        if (!firstRun) {
                        	partOverAnnos.add(AnnotationRecreator.createTreeForAnnotation(
                                ann));
                        }
                    }
                }

                //Removing previously existing annotations
                for (AbstractAnnotation aa : removables) {
                	tier.removeAnnotation(aa);
                }

                //Creating regular annotations
                for (long time = start; time < realEnd;
                        time = time + annSize) {
                    Annotation ann = tier.createAnnotation(time, time +
                            annSize);
                }
                
                firstRun = true;
                //after completion of a whole tier, check the canceled value of the monitor
                if (monitor.isCancelled()) {
                    break;
                }
            }

            RegularAnnotationCommand.this.transcription.setNotifying(true);

            monitor.close();
        }
    }
}
