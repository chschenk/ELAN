/*
 * File:     RemoveAnnotationValueCA.java
 * Project:  MPI Linguistic Application
 * Date:     25 January 2007
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

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * A command action for removing the value of an annotation.
 *
 * @author Ouriel Grynszpan
 */
public class RemoveAnnotationValueCA extends CommandAction
    implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new RemoveAnnotationValueCA instance
     *
     * @param viewerManager the viewermanager
     */
    public RemoveAnnotationValueCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.REMOVE_ANNOTATION_VALUE);

//        putValue(SHORT_DESCRIPTION,
//            ElanLocale.getString(ELANCommandFactory.REMOVE_ANNOTATION_VALUE));
        putValue(DEFAULT, "REMOVE_ANNOTATION_VALUE");
        viewerManager.connectListener(this);
        activeAnnotation = viewerManager.getActiveAnnotation().getAnnotation();
        setEnabled(false);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.REMOVE_ANNOTATION_VALUE);
    }

    /**
     * The receiver of this CommandAction is the Annotation for which the value
     * should be removed.
     *
     * @return the receiver, the active annotation
     */
    @Override
	protected Object getReceiver() {
        return activeAnnotation;
    }

    /**
     * Null
     *
     * @return null
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * Checks wether there is an active annotation.
     */
    protected void checkState() {
        if (activeAnnotation != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
