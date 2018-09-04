/*
 * File:     SpreadSheetCA.java
 * Project:  MPI Linguistic Application
 * Date:     02 January 2007
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
 * Command Action for displaying a spreadsheet of annotations of the root tiers
 */
public class SpreadSheetCA extends CommandAction {
    /**
     * Creates a new SpreadSheetCA instance
     *
     * @param theVM the viewer manager
     */
    public SpreadSheetCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SPREADSHEET);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SPREADSHEET);
    }

    /**
     * The receiver is the transcription
     *
     * @return the transcription
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns null, no arguments need to be passed.
     *
     * @return null
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
