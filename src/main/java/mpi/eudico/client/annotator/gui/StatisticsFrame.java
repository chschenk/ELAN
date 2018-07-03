/*
 * File:     StatisticsFrame.java
 * Project:  MPI Linguistic Application
 * Date:     27 January 2007
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
package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.export.ExportStatistics;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A frame to display statistics about annotations on root tiers Statistics
 * computed are: number of occurrences, frequency, average duration, time
 * ratio, latency
 *
 * @author Ouriel Grynszpan
 */
@SuppressWarnings("serial")
public class StatisticsFrame extends ClosableFrame implements ActionListener,
    ClientLogger {
    private TranscriptionImpl transcription;

    //Title GUI
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JTabbedPane tabPane;

    //Button panel
    private JButton saveButton;
    private JButton closeButton;
    private JPanel buttonPanel;

    //Time boundaries
    private ElanMediaPlayer player;
    private long mediaDuration;

    /**
     * Creates a new StatisticsFrame instance
     *
     * @param transcription the transcription
     */
    public StatisticsFrame(Transcription transcription) {
        super();
        this.transcription = (TranscriptionImpl) transcription;
        player = ELANCommandFactory.getViewerManager(transcription)
                                   .getMasterMediaPlayer();

        if (player != null) {
            mediaDuration = player.getMediaDuration();
            initComponents();
            postInit();
            tabPane.setSelectedIndex(0);
        }
    }

    /**
     * Initializes UI elements.
     */
    private void initComponents() {
        //Title components
        titleLabel = new JLabel();
        titlePanel = new JPanel();
        tabPane = new JTabbedPane();
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Annotation"),
                new AnnotationStatisticsPanel(transcription, mediaDuration));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Annotation") + " II",
                new AnnotationStatisticsPanel2(transcription, mediaDuration));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Tier"),
            new TierStatisticsPanel(transcription, mediaDuration));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Type"),
        		new TierAttributeStatisticsPanel(transcription, mediaDuration, 
        				"Statistics.Type", new TierImpl.LinguisticTypeNameGetter()));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Participant"),
        		new TierAttributeStatisticsPanel(transcription, mediaDuration, 
        				"Statistics.Participant", new TierImpl.ParticipantGetter()));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Annotator"),
        		new TierAttributeStatisticsPanel(transcription, mediaDuration, 
        				"Statistics.Annotator", new TierImpl.AnnotatorGetter()));
        tabPane.addTab(ElanLocale.getString("Statistics.Panel.Language"),
        		new TierAttributeStatisticsPanel(transcription, mediaDuration, 
        				"MFE.TierHeader.Language", new TierImpl.LanguageGetter()));
        //Button components
        saveButton = new JButton();
        closeButton = new JButton();
        buttonPanel = new JPanel();

        updateLocale();

        GridBagConstraints gridBagConstraints;
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        //Setting title
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(titlePanel, gridBagConstraints);

        // add tabpane
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabPane, gridBagConstraints);

        //Setting buttons
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 2));

        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        getRootPane().setDefaultButton(closeButton);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int w = 550;
        int h = 450;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    public void updateLocale() {
        setTitle(ElanLocale.getString("Menu.View.Statistics"));
        titleLabel.setText(ElanLocale.getString("Statistics.Title"));

        saveButton.setText(ElanLocale.getString("Button.Save"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
        repaint();
    }

    /**
     * The button actions.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == saveButton) {
            Object panel = tabPane.getComponentAt(tabPane.getSelectedIndex());

            if (panel instanceof AbstractStatisticsPanel) {
                new ExportStatistics(this, true, transcription,
                    ((AbstractStatisticsPanel) panel).getStatisticsTable());
            }
        } else if (source == closeButton) {
            setVisible(false);
            dispose();
        }
    }
}
