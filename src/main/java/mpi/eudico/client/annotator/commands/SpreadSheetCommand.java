/*
 * File:     SpreadSheetCommand.java
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Displays a spreadsheet with a column for each root tier that lists its
 * annotations ordered according to time
 */
public class SpreadSheetCommand implements Command {
    private String commandName;

    //Frame containing the spreadsheet
    private JFrame sheetFrame;
    private TranscriptionImpl transcription;

    //Vector containing the root tiers
    private List<TierImpl> rootTiers;

    /**
     * Creates a new SpreadSheetCommand instance
     *
     * @param name the command name
     */
    public SpreadSheetCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the frame.
     *
     * @param receiver the transcription
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        extractRootTiers();
        showFrame();
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Displays the frame
     */
    private void showFrame() {
        createSheetFrame();

        if (sheetFrame != null) {
            sheetFrame.setVisible(true);
            sheetFrame.toFront();
        }
    }

    /**
     * Creates the frame from a table model
     */
    private void createSheetFrame() {
        try {
            sheetFrame = new JFrame();
            sheetFrame.getContentPane().setLayout(new BorderLayout());

            DefaultTableModel annTableModel = getAnnotationsTable();
            JTable annTable = new JTable();

            if (annTableModel != null) {
                annTable.setModel(annTableModel);
            }

            annTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            annTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
            annTable.setEnabled(false);

            JScrollPane annTableScrollPane = new JScrollPane(annTable);
            JPanel annTablePanel = new JPanel(new BorderLayout());
            annTablePanel.add(annTableScrollPane, BorderLayout.CENTER);

            sheetFrame.getContentPane().add(annTablePanel, BorderLayout.CENTER);

            updateLocale();

            sheetFrame.pack();

            sheetFrame.setResizable(true);
        } catch (Exception ex) {
            System.out.println("Couldn't create Sheet Frame.");
            ex.printStackTrace();
        }
    }

    /**
     * Needed to set title of dialog
     */
    public void updateLocale() {
        if (sheetFrame != null) {
            sheetFrame.setTitle(ElanLocale.getString("Menu.View.SpreadSheet"));
            sheetFrame.repaint();
        }
    }

    /**
     * Creates a table model with one column for each root tier
     *
     * @return the table model
     */
    private DefaultTableModel getAnnotationsTable() {
        DefaultTableModel annTableModel = null;

        if (rootTiers != null) {
            //Compute number of rows in the table (=max number of annotations
            //on a tier).
            Iterator tierIt = rootTiers.iterator();
            int maxAnnos = 0;

            while (tierIt.hasNext()) {
                TierImpl tier = (TierImpl) tierIt.next();
                maxAnnos = Math.max(tier.getAnnotations().size(), maxAnnos);
            }

            //Array of annotation values 
            String[][] tableVal = new String[maxAnnos][rootTiers.size()];

            //Array of headers
            String[] tierNames = new String[rootTiers.size()];

            //Fills in the table with annotation values
            tierIt = rootTiers.iterator();

            int j = 0;

            while (tierIt.hasNext()) {
                TierImpl tier = (TierImpl) tierIt.next();
                tierNames[j] = tier.getName();

                List<? extends Annotation> annotations = tier.getAnnotations();

                if (annotations != null) {
                    //Order annotations according to time
                    Collections.sort(annotations);

                    for (int i = 0; i < maxAnnos; i++) {
                        if (i < annotations.size()) {
                            Annotation ann = annotations.get(i);
                            tableVal[i][j] = ann.getValue();
                        }
                        //Sets table cell to empty if no more annotations
                        else {
                            tableVal[i][j] = "";
                        }
                    }
                }

                j++;
            }

            annTableModel = new DefaultTableModel(tableVal, tierNames);
        }

        return annTableModel;
    }

    /**
     * Returns a vector containing the root tiers
     *
     * @return the root tiers
     */
    private List<TierImpl> extractRootTiers() {
        rootTiers = null;

        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();
            rootTiers = new ArrayList<TierImpl>();

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

        return rootTiers;
    }
}
