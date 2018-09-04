/*
 * File:     RegularAnnotationDlgCA.java
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

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates a Regular Annotation dialog.
 *
 * @author Ouriel Grynszpan
 */
public class RegularAnnotationDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param theVM the ViewerManager
     */
    public RegularAnnotationDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.REGULAR_ANNOTATION_DLG);
    }

    /**
     * Creates a new Regular Annotation dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.REGULAR_ANNOTATION_DLG);
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * There are no arguments for the command that creates a dialog.
     *
     * @return null
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
