/*
 * File:     CopyToNextAnnotationCA.java
 * Project:  MPI Linguistic Application
 * Date:     27 December 2006
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * CommandAction for copying values of annotations at current time to next
 * adjacent annotations
 *
 * @author $Author$
 */
@SuppressWarnings("serial")
public class CopyToNextAnnotationCA extends CommandAction {
    private Annotation currentAnnot = null;
    private Annotation newAnnot = null;

    /**
     * Creates a new CopyToNextAnnotationCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public CopyToNextAnnotationCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.COPY_TO_NEXT_ANNOTATION);

        //con = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/CopyToNextAnnotation.gif"));
        //putValue(SMALL_ICON, icon);
        putValue(DEFAULT, "COPY_TO_NEXT_ANNOTATION");
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new command if certain conditions are met.
     */
    @Override
	protected void newCommand() {
        Annotation[] activeAnnotations = retrieveAnnotations();
        currentAnnot = activeAnnotations[0];
        newAnnot = activeAnnotations[1];

        if (currentAnnot != null) {
            command = ELANCommandFactory.createCommand(vm.getTranscription(),
                    ELANCommandFactory.COPY_TO_NEXT_ANNOTATION);
        }
        //Avoid executing the command if there is no current annotation
        // or no next annotation
        else {
            command = null;
        }
    }

    /**
     * Returns the viewermanager.
     *
     * @return the viewermanager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * Retrieves current active annotation and simultaneous annotations on
     * other tiers.
     *
     * @return Array containing next annotation, current annotation and
     *         simultaneous annotations
     */
    @Override
	protected Object[] getArguments() {
        List<Annotation> simultaneousAnnotations = getSimultaneousAnnotations(currentAnnot);

        Object[] args = new Object[3];
        args[0] = newAnnot;
        args[1] = currentAnnot;
        args[2] = simultaneousAnnotations;

        return args;
    }

    /**
     * Extract the root tiers as candidates for copy to next annotations.
     * 
     * @param transcription the transcription
     * @param rootTiers the list the root tiers are to be added to
     */
    private void extractRootTiers(TranscriptionImpl transcription, List<TierImpl> rootTiers) {
        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();

            if (tiers != null) {
                Iterator tierIt = tiers.iterator();
                TierImpl tier = null;

                while (tierIt.hasNext()) {
                    tier = (TierImpl) tierIt.next();

                    if (tier.getLinguisticType().getConstraints() == null) {
                        rootTiers.add(tier);
                    }
                }
            }
        }
    }

    /**
     * Returns the current active annotation and the next active annotation. If
     * active annotation not found, returns an array of null.
     *
     * @return the current active annotation and it's next adjacent annotation 
     * (if both exist)
     */
    private Annotation[] retrieveAnnotations() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationAfter(currentActiveAnnot);
        } else { // try on basis of current time and active tier

            TierImpl activeTier = (TierImpl) vm.getMultiTierControlPanel()
                                               .getActiveTier();

            if (activeTier != null) {
                currentActiveAnnot = activeTier.getAnnotationAtTime(vm.getMasterMediaPlayer()
                                                                      .getMediaTime());
                newActiveAnnot = activeTier.getAnnotationAfter(currentActiveAnnot);
            }
        }

        //Works only if there exist a next annotation
        if (newActiveAnnot == null) {
            currentActiveAnnot = null;
        }

        //Works only for annotation on top level tiers
        if (currentActiveAnnot != null) {
            TierImpl currentTier = (TierImpl) currentActiveAnnot.getTier();

            if (currentTier.getLinguisticType().getConstraints() != null) {
                currentActiveAnnot = null;
                newActiveAnnot = null;
            }
        }

        Annotation[] activeAnnotations = new Annotation[2];
        activeAnnotations[0] = currentActiveAnnot;
        activeAnnotations[1] = newActiveAnnot;

        return activeAnnotations;
    }

    /**
     * Returns annotations on other top level tiers that are simultaneous with
     * the current active annotation
     *
     * Note: check if this is what is intended: probably better to check on begin 
     * and end time, or even for symbolically associated annotations?? hier...
     * 
     * @param currentActiveAnnot active annotation
     *
     * @return annotations on other tiers at the position of the active 
     * annotation begintime
     */
    private List<Annotation> getSimultaneousAnnotations(Annotation currentActiveAnnot) {
        List<TierImpl> rootTiers = new ArrayList<TierImpl>();
        extractRootTiers((TranscriptionImpl)vm.getTranscription(), rootTiers);

        List<Annotation> simultaneousAnnotations = null;

        if (currentActiveAnnot != null) {
            TierImpl currentTier = (TierImpl) currentActiveAnnot.getTier();

            simultaneousAnnotations = new ArrayList<Annotation>();

            Iterator tiersIter = rootTiers.iterator();

            while (tiersIter.hasNext()) {
                TierImpl tier = (TierImpl) tiersIter.next();

                //skip the tier of the active annotation
                if ((currentTier.getName()).equals(tier.getName())) {
                    continue;
                }

                Annotation ann = tier.getAnnotationAtTime(vm.getMasterMediaPlayer()
                                                            .getMediaTime());

                if (ann != null) {
                    simultaneousAnnotations.add(ann);
                }
            }
        }

        return simultaneousAnnotations;
    }
}
