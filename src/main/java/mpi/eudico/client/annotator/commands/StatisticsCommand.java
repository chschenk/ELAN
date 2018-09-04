/*
 * File:     StatisticsCommand.java
 * Project:  MPI Linguistic Application
 * Date:     26 January 2007
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

import mpi.eudico.client.annotator.gui.StatisticsFrame;
import mpi.eudico.server.corpora.clom.Transcription;


/**
 * Command for displaying statistics
 */
public class StatisticsCommand implements Command {
    private String commandName;
    private Transcription transcription;

    /**
     * Creates a new TierDependenciesCommand instance
     *
     * @param name the name
     */
    public StatisticsCommand(String name) {
        commandName = name;
    }

    /**
     * Creates a new StatisticsFrame.
     *
     * @param receiver transcription
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;

        new StatisticsFrame(transcription).setVisible(true);
    }

    /**
     * The name
     *
     * @return name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
