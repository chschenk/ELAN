/*
 * File:     RegularAnnotationDlgCommand.java
 * Project:  MPI Linguistic Application
 * Date:     31 October 2005
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

import mpi.eudico.client.annotator.gui.RegularAnnotationDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that brings up a JDialog for regular annotations.
 *
 * @author Ouriel Grynszpan
 */
public class RegularAnnotationDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new regular annotation dialog command.
     *
     * @param name the name of the command
     */
    public RegularAnnotationDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the regular annotation dialog.
     *
     * @param receiver the transcription holding the tiers
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;
        new RegularAnnotationDialog(transcription).setVisible(true);
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
