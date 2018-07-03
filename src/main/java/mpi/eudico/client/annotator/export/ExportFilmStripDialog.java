package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.viewer.SignalViewer;


/**
 * A dialog for configuring the filmstrip with waveform export.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class ExportFilmStripDialog extends ClosableDialog
    implements ActionListener, ProgressListener {
    private JSpinner widthSpinner;
    private JSpinner skipSpinner;
    private JRadioButton everyRB;
    private JRadioButton skipRB;
    private JCheckBox includeFrameTC;
    private JCheckBox includeWavCB;
    private JLabel wavHeightLb;
    private JSpinner heightSpinner;
    private JCheckBox rulerVisCB;
    private JLabel stereoLb;
    private JRadioButton separateRB;
    private JRadioButton mergedRB;
    private JRadioButton blendedRB;
    private JProgressBar progressBar;
    private JButton closeButton;
    private JButton okButton;
    private ExportFilmStrip exporter;
    private ElanMediaPlayer[] players;
    private String waveFile;
    private Selection selection;
    private final int DEF_WIDTH = 120;
    private final int DEF_SKIP_FRAMES = 2;

    /**
     * Creates a new ExportFilmStripDialog instance
     *
     * @param parent the parent window
     * @param players the video players
     * @param waveFile the wavefile
     * @param selection the time selection
     *
     * @throws HeadlessException 
     * @throws IllegalArgumentException if there is no selection
     */
    public ExportFilmStripDialog(Frame parent, ElanMediaPlayer[] players,
        String waveFile, Selection selection) throws HeadlessException {
        super(parent, true);

        if ((selection == null) ||
                (selection.getBeginTime() == selection.getEndTime())) {
            throw new IllegalArgumentException(
                "No valid time selection has been specified");
        }

        this.players = players;
        this.waveFile = waveFile;
        this.selection = selection;
        initComponents();
        postInit();
    }

    /**
     * Creates a new ExportFilmStripDialog instance
     *
     * @param players the video players
     * @param waveFile the wavefile
     * @param selection the time selection
     *
     * @throws HeadlessException 
     * @throws IllegalArgumentException if there is no selection
     */
    public ExportFilmStripDialog(ElanMediaPlayer[] players, String waveFile,
        Selection selection) throws HeadlessException {
        super((Frame)null, true);

        if ((selection == null) ||
                (selection.getBeginTime() == selection.getEndTime())) {
            throw new IllegalArgumentException(
                "No valid time selection has been specified");
        }

        this.players = players;
        this.waveFile = waveFile;
        this.selection = selection;
        initComponents();
        postInit();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int w = 460;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(4, 6, 4, 6);
        Dimension spinDim = new Dimension(100, 20);
        setTitle(ElanLocale.getString("ExportFilmStrip.Title"));

        JLabel titleLabel = new JLabel(ElanLocale.getString(
                    "ExportFilmStrip.Title"));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(titleLabel, gbc);

        // add video panel
        JPanel vidPanel = new JPanel(new GridBagLayout());
        vidPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportFilmStrip.Video")));

        JLabel videoWidthLabel = new JLabel(ElanLocale.getString(
                    "ExportFilmStrip.VideoFrameWidth"));
        widthSpinner = new JSpinner(new SpinnerNumberModel(DEF_WIDTH, 10, 2000, 5));
        widthSpinner.setPreferredSize(spinDim);

        JLabel includeLabel = new JLabel(ElanLocale.getString(
                    "ExportFilmStrip.IncludeFrames"));
        everyRB = new JRadioButton(ElanLocale.getString(
                    "ExportFilmStrip.EveryFrame"));
        everyRB.setSelected(true);
        skipRB = new JRadioButton(ElanLocale.getString(
                    "ExportFilmStrip.EveryNthFrame"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(everyRB);
        bg.add(skipRB);
        everyRB.addActionListener(this);
        skipRB.addActionListener(this);
        skipSpinner = new JSpinner(new SpinnerNumberModel(DEF_SKIP_FRAMES, 1, 100, 1));
        skipSpinner.setPreferredSize(spinDim);
        skipSpinner.setEnabled(false);
        includeFrameTC = new JCheckBox(ElanLocale.getString(
                    "ExportFilmStrip.IncludeTimeCode"));

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        vidPanel.add(videoWidthLabel, gbc);

        gbc.gridx = 1;
        vidPanel.add(widthSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        vidPanel.add(includeLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(4, 16, 0, 6);
        vidPanel.add(everyRB, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 16, 4, 6);
        vidPanel.add(skipRB, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 6, 4, 6);
        vidPanel.add(skipSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = insets;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        vidPanel.add(includeFrameTC, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(vidPanel, gbc);

        // add waveform panel
        JPanel audPanel = new JPanel(new GridBagLayout());
        audPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportFilmStrip.Waveform")));
        includeWavCB = new JCheckBox(ElanLocale.getString(
                    "ExportFilmStrip.Waveform.Include"));
        includeWavCB.setSelected(true);
        includeWavCB.addActionListener(this);
        wavHeightLb = new JLabel(ElanLocale.getString(
                    "ExportFilmStrip.Waveform.Height"));
        heightSpinner = new JSpinner(new SpinnerNumberModel(DEF_WIDTH, 20, 400, 5));
        heightSpinner.setPreferredSize(spinDim);
        rulerVisCB = new JCheckBox(ElanLocale.getString(
                    "TimeScaleBasedViewer.TimeRuler.Visible"));
        stereoLb = new JLabel(ElanLocale.getString("SignalViewer.Stereo"));
        separateRB = new JRadioButton(ElanLocale.getString(
                    "SignalViewer.Stereo.Separate"));
        separateRB.setSelected(true);
        mergedRB = new JRadioButton(ElanLocale.getString(
                    "SignalViewer.Stereo.Merged"));
        blendedRB = new JRadioButton(ElanLocale.getString(
                    "SignalViewer.Stereo.Blended"));

        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(separateRB);
        bg2.add(mergedRB);
        bg2.add(blendedRB);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        gbc.gridwidth = 2;
        audPanel.add(includeWavCB, gbc);

        Insets insets2 = new Insets(4, 16, 4, 6);
        gbc.gridy = 1;
        gbc.insets = insets2;
        audPanel.add(rulerVisCB, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        audPanel.add(wavHeightLb, gbc);

        gbc.gridx = 1;
        gbc.insets = insets;
        audPanel.add(heightSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets2;
        audPanel.add(stereoLb, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 26, 2, 6);
        audPanel.add(separateRB, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 26, 0, 6);
        audPanel.add(mergedRB, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 26, 4, 6);
        audPanel.add(blendedRB, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(audPanel, gbc);
        // add progressbar panel
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 6, 10, 6);
        getContentPane().add(progressBar, gbc);

        // add button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        buttonPanel.add(okButton);
        buttonPanel.add(closeButton);
        okButton.addActionListener(this);
        closeButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(buttonPanel, gbc);
        
        setPreferredSetting();

        if (waveFile == null) {
            includeWavCB.setSelected(false);
            enableWaveFormUI(false);
        }
    }

    private void enableButtons(boolean enable) {
        okButton.setEnabled(enable);
        closeButton.setEnabled(enable);
    }

    private void enableWaveFormUI(boolean enabled) {
        wavHeightLb.setEnabled(enabled);
        rulerVisCB.setEnabled(enabled);
        stereoLb.setEnabled(enabled);
        separateRB.setEnabled(enabled);
        mergedRB.setEnabled(enabled);
        blendedRB.setEnabled(enabled);
    }

    /**
     * Starts the export. Configures the exporter based on user selections.
     *
     */
    private void startExport() {    	
    	savePreferences();
    	
        int vidWidth = ((Integer) widthSpinner.getValue()).intValue();
        int framesStep = 1;

        if (skipRB.isSelected()) {
            framesStep = ((Integer) skipSpinner.getValue()).intValue();
        }

        boolean includeWav = includeWavCB.isSelected();

        if (waveFile == null) {
            includeWav = false;
        }

        boolean includeRuler = rulerVisCB.isSelected();
        int wavHeight = ((Integer) heightSpinner.getValue()).intValue();
        int stereoMode = 0;

        if (separateRB.isSelected()) {
            stereoMode = SignalViewer.STEREO_SEPARATE;
        } else if (mergedRB.isSelected()) {
            stereoMode = SignalViewer.STEREO_MERGED;
        } else if (blendedRB.isSelected()) {
            stereoMode = SignalViewer.STEREO_BLENDED;
        }

        // to be sure reset
        progressBar.setValue(0);
        exporter = new ExportFilmStrip(players, waveFile, vidWidth, framesStep,
                includeWav, wavHeight);
        exporter.setIncludeTimeCodeInFrames(includeFrameTC.isSelected());

        if (includeWav) {
            exporter.setTimeRulerVisible(includeRuler);
            exporter.setStereoMode(stereoMode);
        }

        exporter.addProgressListener(this);
        exporter.createImageInThread(selection.getBeginTime(),
            selection.getEndTime());
    }

    /**
     * Saves the created image, if there is one. 
     *
     */
    private void saveImage() {
        if (exporter != null) {
            Image img = exporter.getImage();

            if (img != null) {
                ImageExporter ie = new ImageExporter();
                ie.exportImage(img);
            } else {
                JOptionPane.showMessageDialog(getParent(),
                    ElanLocale.getString("ExportFilmStrip.Error.NoImage"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            }
        }

        progressBar.setValue(0);
    }

    /**
     * The action performed handling.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            if (exporter != null) {
                exporter.removeProgressListener(this);
            }

            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {
            enableButtons(false);
            startExport();
        } else if (e.getSource() == includeWavCB) {
            enableWaveFormUI(includeWavCB.isSelected());
        } else if ((e.getSource() == everyRB) || (e.getSource() == skipRB)) {
            skipSpinner.setEnabled(skipRB.isSelected());
        }
    }

    /**
     * Called when the progress is completed.
     *
     * @param source the source
     * @param message the message to display
     */
    @Override
	public void progressCompleted(Object source, String message) {
        if (message != null) {
            progressBar.setValue(100);
            progressBar.setString(message);
        }

        enableButtons(true);
        saveImage();
    }

    /**
     * Called when the progress is interrupted.
     *
     * @param source the source
     * @param message the message to display
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        if (message != null) {
            progressBar.setString(message);
        }
        JOptionPane.showMessageDialog(getParent(),
                ElanLocale.getString("ExportFilmStrip.Error.Unknown") + " " + message,
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
        // reset
        progressBar.setValue(0);
        enableButtons(true);
    }

    /**
     * Called to update the progress value.
     *
     * @param source the source
     * @param percent the completed percentage
     * @param message the message to display
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if (message != null) {
            progressBar.setString(message);
        }

        progressBar.setValue(percent);

        if (percent == progressBar.getMaximum()) {
            enableButtons(true);
            saveImage();
        }
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Boolean boolPref = Preferences.getBool("ExportFilmStripDialog.everyRB", null);
    
    	if (boolPref != null) {
    		everyRB.setSelected(boolPref); 
    	}	
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.skipRB", null);
    	if (boolPref != null) {
    		skipRB.setSelected(boolPref); 
    	}
    	
    	skipSpinner.setEnabled(skipRB.isSelected());
    	
    	boolPref = Preferences.getBool("ExportFilmStripDialog.includeFrameTC", null);
    	if (boolPref != null) {
    		includeFrameTC.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.includeWavCB", null);
    	if (boolPref != null) {
    		includeWavCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.rulerVisCB", null);
    	if (boolPref != null) {
    		rulerVisCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.separateRB", null);
    	if (boolPref != null) {
    		separateRB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.mergedRB", null);
    	if (boolPref != null) {
    		mergedRB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportFilmStripDialog.blendedRB", null);
    	if (boolPref != null) {
    		blendedRB.setSelected(boolPref); 
    	}
    	
    	Integer intPref = Preferences.getInt("ExportFilmStripDialog.VideoImageWidth", null);
    	if (intPref != null) {
    		widthSpinner.setValue(intPref);
    	}
    	
    	intPref = Preferences.getInt("ExportFilmStripDialog.EveryNthFrame", null);
    	if (intPref != null) {
    		skipSpinner.setValue(intPref);
    	}
    	
    	intPref = Preferences.getInt("ExportFilmStripDialog.WaveHeight", null);
    	if (intPref != null) {
    		heightSpinner.setValue(intPref);
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportFilmStripDialog.everyRB", everyRB.isSelected(), null);    
    	Preferences.set("ExportFilmStripDialog.skipRB", skipRB.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.includeFrameTC", includeFrameTC.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.includeWavCB", includeWavCB.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.rulerVisCB", rulerVisCB.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.separateRB", separateRB.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.mergedRB", mergedRB.isSelected(), null);
    	Preferences.set("ExportFilmStripDialog.blendedRB", blendedRB.isSelected(), null);
    	// some values only save as a preference the first time it differs from the default
    	Integer vidWidth = (Integer) widthSpinner.getValue();
    	if (vidWidth != DEF_WIDTH || Preferences.getInt("ExportFilmStripDialog.VideoImageWidth", null) != null) {
    		Preferences.set("ExportFilmStripDialog.VideoImageWidth", vidWidth, null);
    	}
    	
    	if (skipRB.isSelected()) {
    		Integer skipFrames = (Integer) skipSpinner.getValue();
    		if (skipFrames != DEF_SKIP_FRAMES || Preferences.getInt("ExportFilmStripDialog.EveryNthFrame", null) != null) {
    			Preferences.set("ExportFilmStripDialog.EveryNthFrame", skipFrames, null);
    		}
    	}
    	
    	if (includeWavCB.isSelected()) {
    		Integer wavHeight = (Integer) heightSpinner.getValue();
    		if (wavHeight != DEF_WIDTH || Preferences.getInt("ExportFilmStripDialog.WaveHeight", null) != null) {
    			Preferences.set("ExportFilmStripDialog.WaveHeight", heightSpinner.getValue(), null);
    		}
    	}
    }
}
