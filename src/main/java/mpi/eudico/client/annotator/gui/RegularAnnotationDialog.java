/*
 * File:     RegularAnnotationDialog.java
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
package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;


/**
 * A dialog for convenient batch-like creation of annotations/segmentation.
 *
 * @author Ouriel Grynszpan
 * @version Dec 2006 Identity removed
 */
public class RegularAnnotationDialog extends JDialog implements ActionListener,
    PropertyChangeListener {
    //Title
    private JLabel titleLabel;
    private JPanel titlePanel;

    //Tiers selection panel
    private AbstractTierSortAndSelectPanel tierSelectionPanel;       

    //Time specification panel
    private JPanel timeSpecPanel;

    //Start time GUI
    private JLabel startLabel;
    private JFormattedTextField startHH;
    private JFormattedTextField startMM;
    private JFormattedTextField startSS;
    private JFormattedTextField startMS;

    //Duration GUI
    private JLabel durationLabel;
    private JFormattedTextField durHH;
    private JFormattedTextField durMM;
    private JFormattedTextField durSS;
    private JFormattedTextField durMS;

    //To inhibit crisscross calls to propertyChange 
    /** hour part of duration value */
    boolean durHH_inhib = false;

    /** minute part of duration value */
    boolean durMM_inhib = false;

    /** second part of duration value */
    boolean durSS_inhib = false;

    /** ms part of duration value */
    boolean durMS_inhib = false;

    //End time GUI
    private JLabel endLabel;
    private JFormattedTextField endHH;
    private JFormattedTextField endMM;
    private JFormattedTextField endSS;
    private JFormattedTextField endMS;

    //To inhibit crisscross calls to propertyChange 
    /** hour part of end value */
    boolean endHH_inhib = false;

    /** minute part of end value */
    boolean endMM_inhib = false;

    /** second part of end value */
    boolean endSS_inhib = false;

    /** ms part of end value */
    boolean endMS_inhib = false;

    //Annotation size GUI
    private JLabel annotationSizeLabel;
    private JFormattedTextField annSizeHH;
    private JFormattedTextField annSizeMM;
    private JFormattedTextField annSizeSS;
    private JFormattedTextField annSizeMS;

    //actual time value in ms
    private long start;
    private long end;
    private long dur;
    private JButton applyButton;
    private JButton closeButton;
    private JPanel buttonPanel;
    private TranscriptionImpl transcription;
    private ElanMediaPlayer player;
    private long mediaDuration;

    /**
     * Creates a new RegularAnnotationDialog instance
     *
     * @param transcription the transcription
     */
    public RegularAnnotationDialog(TranscriptionImpl transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = transcription;
        player = ELANCommandFactory.getViewerManager(transcription)
                                   .getMasterMediaPlayer();

        if (player != null) {
            mediaDuration = player.getMediaDuration();
            initComponents();
            postInit();
        }
    }

    /**
     * Initializes UI elements.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        //Title
        titleLabel = new JLabel();
        titlePanel = new JPanel();

        //Tiers selection panel
        tierSelectionPanel = new TranscriptionTierSortAndSelectPanel(transcription, null, new ArrayList(), 
        		true, true, Modes.ROOT_TIERS );
        
        //Creating and building time specification panel
        initTimeSpecPanel();
        
        //Buttons
        applyButton = new JButton();
        closeButton = new JButton();
        buttonPanel = new JPanel();
        updateLocale();

        GridBagConstraints gridBagConstraints;
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        //Creating title panel
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        getContentPane().add(titlePanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tierSelectionPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;        
        getContentPane().add(timeSpecPanel, gridBagConstraints);

        //Creating button panel
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        applyButton.addActionListener(this);
        buttonPanel.add(applyButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        //gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
    }

    /**
     * Initializes time specification GUI elements.
     *
     * @return time specifications panel
     */
    private JPanel initTimeSpecPanel() {
    	startLabel = new JLabel();
        endLabel = new JLabel();
        durationLabel = new JLabel();
        annotationSizeLabel = new JLabel();
        timeSpecPanel = new JPanel();
        timeSpecPanel.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints;
        Insets insets = new Insets(2, 6, 2, 6);

        //Format for hours, minutes and seconds
        DecimalFormat twoDigits = new DecimalFormat("00");
        twoDigits.setMaximumIntegerDigits(2);
        twoDigits.setMinimumIntegerDigits(2);
        twoDigits.setMaximumFractionDigits(0);

        //Format for milliseconds
        DecimalFormat threeDigits = new DecimalFormat("000");
        threeDigits.setMaximumIntegerDigits(3);
        threeDigits.setMinimumIntegerDigits(3);
        threeDigits.setMaximumFractionDigits(0);

        //Start time GUI initialization
        //Start time is initialized with the current position on the media player
        String currentTime = TimeFormatter.toString(player.getMediaTime());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(startLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        startHH = new JFormattedTextField(twoDigits);
        startHH.setColumns(1);
        startHH.setValue(Long.valueOf(currentTime.substring(0, 2)));
        startHH.addPropertyChangeListener("value", this);
        timeSpecPanel.add(startHH, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        startMM = new JFormattedTextField(twoDigits);
        startMM.setColumns(1);
        startMM.setValue(Long.valueOf(currentTime.substring(3, 5)));
        startMM.addPropertyChangeListener("value", this);
        timeSpecPanel.add(startMM, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        startSS = new JFormattedTextField(twoDigits);
        startSS.setColumns(1);
        startSS.setValue(Long.valueOf(currentTime.substring(6, 8)));
        startSS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(startSS, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel("."), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = insets;
        startMS = new JFormattedTextField(threeDigits);
        startMS.setColumns(2);
        startMS.setValue(Long.valueOf(currentTime.substring(9)));
        startMS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(startMS, gridBagConstraints);

        //Duration GUI initialization
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(durationLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        durHH = new JFormattedTextField(twoDigits);
        durHH.setColumns(1);
        durHH.setValue(Long.valueOf(0));
        durHH.addPropertyChangeListener("value", this);
        timeSpecPanel.add(durHH, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        durMM = new JFormattedTextField(twoDigits);
        durMM.setColumns(1);
        durMM.setValue(Long.valueOf(0));
        durMM.addPropertyChangeListener("value", this);
        timeSpecPanel.add(durMM, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        durSS = new JFormattedTextField(twoDigits);
        durSS.setColumns(1);
        durSS.setValue(new Long(0));
        durSS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(durSS, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel("."), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = insets;
        durMS = new JFormattedTextField(threeDigits);
        durMS.setColumns(2);
        durMS.setValue(Long.valueOf(0));
        durMS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(durMS, gridBagConstraints);

        //End time GUI initialization
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(endLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        endHH = new JFormattedTextField(twoDigits);
        endHH.setColumns(1);
        endHH.setValue(Long.valueOf(0));
        endHH.addPropertyChangeListener("value", this);
        timeSpecPanel.add(endHH, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        endMM = new JFormattedTextField(twoDigits);
        endMM.setColumns(1);
        endMM.setValue(Long.valueOf(0));
        endMM.addPropertyChangeListener("value", this);
        timeSpecPanel.add(endMM, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        endSS = new JFormattedTextField(twoDigits);
        endSS.setColumns(1);
        endSS.setValue(Long.valueOf(0));
        endSS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(endSS, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel("."), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        endMS = new JFormattedTextField(threeDigits);
        endMS.setColumns(2);
        endMS.setValue(Long.valueOf(0));
        endMS.addPropertyChangeListener("value", this);
        timeSpecPanel.add(endMS, gridBagConstraints);

        //Annotation size GUI initialization
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(annotationSizeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        annSizeHH = new JFormattedTextField(twoDigits);
        annSizeHH.setColumns(1);
        annSizeHH.setValue(Long.valueOf(0));
        timeSpecPanel.add(annSizeHH, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        annSizeMM = new JFormattedTextField(twoDigits);
        annSizeMM.setColumns(1);
        annSizeMM.setValue(Long.valueOf(0));
        timeSpecPanel.add(annSizeMM, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel(":"), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = insets;
        annSizeSS = new JFormattedTextField(twoDigits);
        annSizeSS.setColumns(1);
        annSizeSS.setValue(Long.valueOf(0));
        timeSpecPanel.add(annSizeSS, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        timeSpecPanel.add(new JLabel("."), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        annSizeMS = new JFormattedTextField(threeDigits);
        annSizeMS.setColumns(2);
        annSizeMS.setValue(Long.valueOf(0));
        timeSpecPanel.add(annSizeMS, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx =1.0;
        timeSpecPanel.add(new JPanel(), gridBagConstraints);
        

        return timeSpecPanel;
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());
        //setResizable(false);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        setTitle(ElanLocale.getString("RegularAnnotationDialog.Title"));
        titleLabel.setText(ElanLocale.getString("RegularAnnotationDialog.Title"));

        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "RegularAnnotationDialog.Label.Tier")));
        timeSpecPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "RegularAnnotationDialog.Label.Spec")));
        //tierLabel.setText(ElanLocale.getString(
        //        "RegularAnnotationDialog.Label.Tier"));
        startLabel.setText(ElanLocale.getString(
                "RegularAnnotationDialog.Label.Start"));
        endLabel.setText(ElanLocale.getString(
                "RegularAnnotationDialog.Label.End"));
        durationLabel.setText(ElanLocale.getString(
                "RegularAnnotationDialog.Label.Duration"));
        annotationSizeLabel.setText(ElanLocale.getString(
                "RegularAnnotationDialog.Label.Size"));
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        closeButton.setText(ElanLocale.getString("Button.Cancel"));
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog(WindowEvent evt) {
        if (transcription != null) {
            ELANCommandFactory.getViewerManager(transcription)
                              .disconnectListener(this);
        }

        setVisible(false);
        dispose();
    }

    /**
     * Computes start time from data on GUI.
     *
     * @return the start time
     */
    private Long getStartTime() {
        long hh = 0;
        long mm = 0;
        long ss = 0;
        long ms = 0;
        boolean noStart = true;

        if (startHH.getValue() != null) {
            hh = (((Long) startHH.getValue()).longValue() % 100) * 3600000;
            noStart = false;
        }

        if (startMM.getValue() != null) {
            mm = (((Long) startMM.getValue()).longValue() % 100) * 60000;
            noStart = false;
        }

        if (startSS.getValue() != null) {
            ss = (((Long) startSS.getValue()).longValue() % 100) * 1000;
            noStart = false;
        }

        if (startMS.getValue() != null) {
            ms = ((Long) startMS.getValue()).longValue() % 1000;
            noStart = false;
        }

        if (noStart) {
            return null;
        } else {
            return new Long(hh + mm + ss + ms);
        }
    }

    /**
     * Computes duration from data on GUI.
     *
     * @return the duration
     */
    private Long getDuration() {
        long hh = 0;
        long mm = 0;
        long ss = 0;
        long ms = 0;
        boolean noStart = true;

        if (durHH.getValue() != null) {
            hh = (((Long) durHH.getValue()).longValue() % 100) * 3600000;
            noStart = false;
        }

        if (durMM.getValue() != null) {
            mm = (((Long) durMM.getValue()).longValue() % 100) * 60000;
            noStart = false;
        }

        if (durSS.getValue() != null) {
            ss = (((Long) durSS.getValue()).longValue() % 100) * 1000;
            noStart = false;
        }

        if (durMS.getValue() != null) {
            ms = ((Long) durMS.getValue()).longValue() % 1000;
            noStart = false;
        }

        if (noStart) {
            return null;
        } else {
            return new Long(hh + mm + ss + ms);
        }
    }

    /**
     * Computes end time from data on GUI.
     *
     * @return the endtime!
     */
    private Long getEndTime() {
        long hh = 0;
        long mm = 0;
        long ss = 0;
        long ms = 0;
        boolean noStart = true;

        if (endHH.getValue() != null) {
            hh = (((Long) endHH.getValue()).longValue() % 100) * 3600000;
            noStart = false;
        }

        if (endMM.getValue() != null) {
            mm = (((Long) endMM.getValue()).longValue() % 100) * 60000;
            noStart = false;
        }

        if (endSS.getValue() != null) {
            ss = (((Long) endSS.getValue()).longValue() % 100) * 1000;
            noStart = false;
        }

        if (endMS.getValue() != null) {
            ms = ((Long) endMS.getValue()).longValue() % 1000;
            noStart = false;
        }

        if (noStart) {
            return null;
        } else {
            return new Long(hh + mm + ss + ms);
        }
    }

    /**
     * Computes annotation size from data on GUI.
     *
     * @return the duration of the annotations
     */
    private Long getAnnotationSize() {
        long hh = 0;
        long mm = 0;
        long ss = 0;
        long ms = 0;
        boolean noStart = true;

        if (annSizeHH.getValue() != null) {
            hh = (((Long) annSizeHH.getValue()).longValue() % 100) * 3600000;
            noStart = false;
        }

        if (annSizeMM.getValue() != null) {
            mm = (((Long) annSizeMM.getValue()).longValue() % 100) * 60000;
            noStart = false;
        }

        if (annSizeSS.getValue() != null) {
            ss = (((Long) annSizeSS.getValue()).longValue() % 100) * 1000;
            noStart = false;
        }

        if (annSizeMS.getValue() != null) {
            ms = ((Long) annSizeMS.getValue()).longValue() % 1000;
            noStart = false;
        }

        if (noStart) {
            return null;
        } else {
            return new Long(hh + mm + ss + ms);
        }
    }

    /**
     * True if source is from start time GUI.
     *
     * @param source the component to check 
     *
     * @return true if the component is one of the start time gui elements
     */
    private boolean isStartTimeSource(Object source) {
        return (source == startHH) || (source == startMM) ||
        (source == startSS) || (source == startMS);
    }

    /**
     * True if source is from duration GUI.
     *
     * @param source the component to check 
     *
     * @return true if the component is one of the duration gui elements
     */
    private boolean isDurationSource(Object source) {
        return (source == durHH) || (source == durMM) || (source == durSS) ||
        (source == durMS);
    }

    /**
     * True if source is from end time GUI.
     *
     * @param source the component to check 
     *
     * @return true if the component is one of the end time gui elements
     */
    private boolean isEndTimeSource(Object source) {
        return (source == endHH) || (source == endMM) || (source == endSS) ||
        (source == endMS);
    }

    /**
     * Sets new end time value when duration has been changed.
     */
    private void setEndTime() {
        end = dur + start;

        try {
            String timeStr = TimeFormatter.toString(end);
            long hh = Long.parseLong(timeStr.substring(0, 2));
            long mm = Long.parseLong(timeStr.substring(3, 5));
            long ss = Long.parseLong(timeStr.substring(6, 8));
            long ms = Long.parseLong(timeStr.substring(9));

            Long hhVal = (Long) endHH.getValue();
            Long mmVal = (Long) endMM.getValue();
            Long ssVal = (Long) endSS.getValue();
            Long msVal = (Long) endMS.getValue();

            //Checks if GUI elements' values need to be mofified,
            //makes moficiations accordingly
            //and triggers propertyChange inhibitor (this is to avoid
            //never ending loops)
            if ((null == hhVal) || (hh != hhVal.longValue())) {
                endHH_inhib = true;
                endHH.setValue(Long.valueOf(hh));
            }

            if ((null == mmVal) || (mm != mmVal.longValue())) {
                endMM_inhib = true;
                endMM.setValue(Long.valueOf(mm));
            }

            if ((null == ssVal) || (ss != ssVal.longValue())) {
                endSS_inhib = true;
                endSS.setValue(Long.valueOf(ss));
            }

            if ((null == msVal) || (ms != msVal.longValue())) {
                endMS_inhib = true;
                endMS.setValue(Long.valueOf(ms));
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Sets new duration value when end time has been changed.
     */
    private void setDuration() {
        dur = end - start;

        try {
            String timeStr = TimeFormatter.toString(dur);
            long hh = Long.parseLong(timeStr.substring(0, 2));
            long mm = Long.parseLong(timeStr.substring(3, 5));
            long ss = Long.parseLong(timeStr.substring(6, 8));
            long ms = Long.parseLong(timeStr.substring(9));

            Long hhVal = (Long) durHH.getValue();
            Long mmVal = (Long) durMM.getValue();
            Long ssVal = (Long) durSS.getValue();
            Long msVal = (Long) durMS.getValue();

            //Checks if GUI elements' values need to be mofified,
            //makes moficiations accordingly
            //and triggers propertyChange inhibitor (this is to avoid
            //never ending loops)
            if ((null == hhVal) || (hh != hhVal.longValue())) {
                durHH_inhib = true;
                durHH.setValue(Long.valueOf(hh));
            }

            if ((null == mmVal) || (mm != mmVal.longValue())) {
                durMM_inhib = true;
                durMM.setValue(Long.valueOf(mm));
            }

            if ((null == ssVal) || (ss != ssVal.longValue())) {
                durSS_inhib = true;
                durSS.setValue(Long.valueOf(ss));
            }

            if ((null == msVal) || (ms != msVal.longValue())) {
                durMS_inhib = true;
                durMS.setValue(Long.valueOf(ms));
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Listens for changes on start time, end time and duration. Updates either
     * end time or duration.
     *
     * @param e the event
     */
    @Override
	public void propertyChange(PropertyChangeEvent e) {
        //If an inhibitor is triggered, untrigger it and do nothing
        //This is to avoid never ending loops
        if (endHH_inhib) {
            endHH_inhib = false;

            return;
        }

        if (endMM_inhib) {
            endMM_inhib = false;

            return;
        }

        if (endSS_inhib) {
            endSS_inhib = false;

            return;
        }

        if (endMS_inhib) {
            endMS_inhib = false;

            return;
        }

        if (durHH_inhib) {
            durHH_inhib = false;

            return;
        }

        if (durMM_inhib) {
            durMM_inhib = false;

            return;
        }

        if (durSS_inhib) {
            durSS_inhib = false;

            return;
        }

        if (durMS_inhib) {
            durMS_inhib = false;

            return;
        }

        //Computes start time
        if (getStartTime() != null) {
            start = getStartTime().longValue();
        }

        //Checks the source of the change.
        //If source is start time changes end time
        if (isStartTimeSource(e.getSource()) && (getStartTime() != null)) {
            if (getDuration() != null) {
                dur = getDuration().longValue();
                setEndTime();
            }

            //If source is duration changes end time
        } else if (isDurationSource(e.getSource()) && (getDuration() != null)) {
            if (getStartTime() != null) {
                dur = getDuration().longValue();
                setEndTime();
            }

            //If source is end time changes duration
        } else if (isEndTimeSource(e.getSource()) && (getEndTime() != null)) {
            if (getStartTime() != null) {
                end = getEndTime().longValue();
                setDuration();
            }
        }
    }

    /**
     * The button actions.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == applyButton) {
            try {
                if ((null == getStartTime()) || (null == getAnnotationSize())) {
                    showWarningDialog(ElanLocale.getString(
                            "RegularAnnotationDialog.Message.InvalidTime"));

                    return;
                } else if (getAnnotationSize().longValue() == 0L) {
                    showWarningDialog(ElanLocale.getString(
                            "RegularAnnotationDialog.Message.InvalidSize"));
                    annSizeSS.requestFocus();

                    return;
                }

                long start = getStartTime().longValue();
                long annSize = getAnnotationSize().longValue();

                long end;

                //Computes end time from either end time GUI or duration GUI
                if (getEndTime() != null) {
                    end = getEndTime().longValue();
                } else if (getDuration() != null) {
                    end = start + getDuration().longValue();
                } else {
                    showWarningDialog(ElanLocale.getString(
                            "RegularAnnotationDialog.Message.InvalidTime"));

                    return;
                }

                //Check consistency
                if ((start >= end) || (annSize > (end - start)) ||
                        (start > mediaDuration)) {
                    showWarningDialog(ElanLocale.getString(
                            "RegularAnnotationDialog.Message.InvalidTime"));

                    return;
                }

                //To avoid exceeding total media duration
                if (end > mediaDuration) {
                    end = mediaDuration;
                }

                List<String> selectedTiers =  tierSelectionPanel.getSelectedTiers();
                		if (selectedTiers.size() < 1) {
                    showWarningDialog(ElanLocale.getString(
                            "RegularAnnotationDialog.Message.NoTier"));

                    return;
                }

                Command c = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.REGULAR_ANNOTATION);

                Object[] args = new Object[4];
                args[0] = new Long(start);
                args[1] = new Long(end);
                args[2] = new Long(annSize);
                args[3] = selectedTiers;
                c.execute(transcription, args);

                closeDialog(null);
            } catch (NumberFormatException e) {
                showWarningDialog(ElanLocale.getString(
                        "RegularAnnotationDialog.Message.InvalidTime"));
            }
        } else if (source == closeButton) {
            closeDialog(null);
        }
    }

    /**
     * Shows a message dialog.
     *
     * @param message the message
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
}
