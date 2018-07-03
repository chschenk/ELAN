package mpi.eudico.client.annotator.timeseries;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clom.Transcription;


/**
 * Second step of a wizard to extract certain data from a time series track
 * based on time intervals (annotations) on a time-alignable tier. In this
 * step a track should be selected and a calculation and overwrite  method can
 * be specified.
 *
 * @author Han Sloetjes
 * @version 1.0 March 2006
 * @version December 2017 added median and range, added some preference settings
 */
@SuppressWarnings("serial")
public class ExtractStep2 extends StepPane implements ListSelectionListener {
    //private Transcription transcription;
    private TSTrackManager manager;
    private JList trackList;
    private DefaultListModel trackModel;
    private JRadioButton aveRB;
    private JRadioButton minRB;
    private JRadioButton maxRB;
    private JRadioButton sumRB;
    private JRadioButton atBeginRB;
    private JRadioButton atEndRB;
    private JRadioButton rangeRB;
    private JRadioButton medianRB;
    private JCheckBox overwriteCB;

    /**
     * A panel for the second step of the wizard.
     *
     * @param multiPane the container multistep pane
     * @param transcription the transcription containing source and destination
     *        tier
     * @param manager the track manager containing the time series tracks
     */
    public ExtractStep2(MultiStepPane multiPane,
        Transcription transcription, TSTrackManager manager) {
        super(multiPane);
        //this.transcription = transcription;
        this.manager = manager;
        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    @Override
	public void initComponents() {
        // setPreferredSize
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        JScrollPane listScroll;

        trackModel = new DefaultListModel();
        trackList = new JList(trackModel);
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScroll = new JScrollPane(trackList);

        if (manager != null) {
            List<TimeSeriesTrack> tracks = manager.getRegisteredTracks();
            AbstractTSTrack tr;

            for (int i = 0; i < tracks.size(); i++) {
                tr = (AbstractTSTrack) tracks.get(i);
                trackModel.addElement(tr.getName());
            }
        }

        ButtonGroup group = new ButtonGroup();
        aveRB = new JRadioButton(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Average"));
        minRB = new JRadioButton(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Minimum"));
        maxRB = new JRadioButton(ElanLocale.getString(
        	"TimeSeriesViewer.Extract.Maximum"));
        sumRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.Sum"));
        atBeginRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.AtBegin"));
        atEndRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.AtEnd"));
        medianRB = new JRadioButton(ElanLocale.getString("TimeSeriesViewer.Extract.Median"));
        rangeRB =  new JRadioButton(ElanLocale.getString("TimeSeriesViewer.Extract.Range"));
        group.add(aveRB);
        group.add(minRB);
        group.add(maxRB);
        group.add(sumRB);
        group.add(atBeginRB);
        group.add(atEndRB);
        group.add(medianRB);
        group.add(rangeRB);
        minRB.setSelected(true);
        overwriteCB = new JCheckBox(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Overwrite"));
        overwriteCB.setSelected(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.SourceTrack") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(listScroll, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.Method") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        add(minRB, gbc);
        gbc.gridx = 1;
        add(maxRB, gbc);
        gbc.gridx = 2;
        add(rangeRB, gbc);
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(aveRB, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        add(sumRB, gbc);
        gbc.gridx = 1;
        add(atBeginRB, gbc);
        gbc.gridx = 2;
        add(atEndRB, gbc);
        gbc.gridx = 3;
        add(medianRB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(overwriteCB, gbc);

        trackList.getSelectionModel().addListSelectionListener(this);
        readPrefs();
    }

    /**
     * Returns the title of this step.
     *
     * @return the title
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("TimeSeriesViewer.Extract.SelectTrack");
    }

    /**
     * Check if the Finish button can be enabled.
     */
    @Override
	public void enterStepForward() {
        if (trackList.getSelectedIndex() >= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }

    /**
     * Enable/disable buttons.
     *
     * @see #enterStepForward()
     */
    @Override
	public void enterStepBackward() {
        this.enterStepForward();
    }

    /**
     * If a track has been selected store track name, calculation method and
     * overwrite mode.
     *
     * @return true if all conditions to proceed have been met, false otherwise
     */
    @Override
	public boolean leaveStepForward() {
        if (trackList.getSelectedIndex() >= 0) {
            String trackName = (String) trackList.getSelectedValue();
            multiPane.putStepProperty("TrackName", trackName);

            String calcType = "Min";

            if (maxRB.isSelected()) {
                calcType = "Max";
            } else if (aveRB.isSelected()) {
                calcType = "Ave";
            } else if (sumRB.isSelected()) {
            	calcType = "Sum";
            } else if (atBeginRB.isSelected()) {
            	calcType = "AtBegin";
            } else if (atEndRB.isSelected()) {
            	calcType = "AtEnd";
            } else if (medianRB.isSelected()) {
            	calcType = "Median";
            } else if (rangeRB.isSelected()) {
            	calcType = "Range";
            }

            multiPane.putStepProperty("Calc", calcType);
            multiPane.putStepProperty("Overwrite",
                String.valueOf(overwriteCB.isSelected()));
            storePrefs();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finishes by moving to the next step, the progress panel.
     *
     * @return false, the window should not be closed yet
     */
    @Override
	public boolean doFinish() {
        if (leaveStepForward()) {
            multiPane.nextStep();
        }

        return false;
    }

    /**
     * Change in track selection; check if the Finish button can be enabled
     *
     * @param e the event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if (trackList.getSelectedIndex() >= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }
    
    private void storePrefs() {
        String calcType = "Minimum";

        if (maxRB.isSelected()) {
            calcType = "Maximum";
        } else if (aveRB.isSelected()) {
            calcType = "Average";
        } else if (sumRB.isSelected()) {
        	calcType = "Sum";
        } else if (atBeginRB.isSelected()) {
        	calcType = "AtBegin";
        } else if (atEndRB.isSelected()) {
        	calcType = "AtEnd";
        } else if (medianRB.isSelected()) {
        	calcType = "Median";
        } else if (rangeRB.isSelected()) {
        	calcType = "Range";
        }
        
        Preferences.set("TimeSeriesViewer.Extract.Calculation", calcType, null);
        Preferences.set("TimeSeriesViewer.Extract.Overwrite", Boolean.valueOf(overwriteCB.isSelected()), null);
    }
    
    private void readPrefs() {
    	String calcType = Preferences.getString("TimeSeriesViewer.Extract.Calculation", null);
    	// minRB is selected by default
    	if (calcType != null) {
    		if (calcType.equals("Maximum")) {
    			maxRB.setSelected(true);
    		} else if (calcType.equals("Average")) {
    			aveRB.setSelected(true);
    		} else if (calcType.equals("Sum")) {
    			sumRB.setSelected(true);
    		} else if (calcType.equals("AtBegin")) {
    			atBeginRB.setSelected(true);
    		} else if (calcType.equals("AtEnd")) {
    			atEndRB.setSelected(true);
    		} else if (calcType.equals("Median")) {
    			medianRB.setSelected(true);
    		} else if (calcType.equals("Range")) {
    			rangeRB.setSelected(true);
    		}
    	}
    	
    	Boolean overWrPref = Preferences.getBool("TimeSeriesViewer.Extract.Overwrite", null);
    	if (overWrPref != null) {
    		overwriteCB.setSelected(overWrPref.booleanValue());
    	}
    }
}
