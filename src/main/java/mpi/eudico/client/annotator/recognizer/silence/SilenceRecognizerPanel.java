package mpi.eudico.client.annotator.recognizer.silence;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//import mpi.eudico.client.annotator.ElanLocale;
//import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.data.RSelection;

/**
 * A panel for setting the minimal silence and non-silence durations.
 * Optionally the noise levels for left and right channels can be set, instead of using selections.
 * 
 * @author albertr
 * @updated aarsom, Sep 2012
 */
@SuppressWarnings("serial")
public class SilenceRecognizerPanel extends JPanel implements ChangeListener, ParamPreferences {
	private JLabel minimalSilenceDurationLabel;
	private JSlider minimalSilenceDuration;
	private JLabel minimalNonSilenceDurationLabel;
	private JSlider minimalNonSilenceDuration;
	
	private JComboBox mediaFilesComboBox;
	
	private JPanel settingsPanel;
	private AbstractSelectionPanel selectionPanel;
	
	private ArrayList<String> mediaFilesList;
	private JPanel selPanel;
	
	private ManualOrExamplePanel manualOrExamplePanel;
	private ResourceBundle languageBundle;
	
	/**
	 * Constructor. Initializes the components.
	 */
	public SilenceRecognizerPanel(AbstractSelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
		//this.selectionPanel.setDefaultOption(AbstractSelectionPanel.SELECTIONS);
		
		initComponents();
	}
	
	private void initComponents(){
		
		mediaFilesComboBox = new JComboBox();
		
		JPanel filePanel = new JPanel(new GridBagLayout());			
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		filePanel.add(new JLabel("Files List :"), gbc);
		
		gbc.gridx = 1;	
		gbc.weightx = 1.0;
		filePanel.add(mediaFilesComboBox, gbc);
		
		selPanel = new JPanel(new GridBagLayout());
		selPanel.setBorder(new TitledBorder("Selection Panel"));
		gbc = new GridBagConstraints();			
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.weightx = 1.0;
		selPanel.add(selectionPanel, gbc);	
					
		initializeSettingsPanel();
		JPanel settingPanel = new JPanel(new GridBagLayout());
		settingPanel.setBorder(new TitledBorder("Settings"));
		gbc = new GridBagConstraints();			
		gbc.anchor = GridBagConstraints.NORTHWEST;			
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 1, 1, 1);
		settingPanel.add(settingsPanel, gbc);				
		
		setLayout(new GridBagLayout());		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;	
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 2, 4, 2);
		add(filePanel, gbc);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;			
		manualOrExamplePanel = new ManualOrExamplePanel();
		manualOrExamplePanel.setBorder(new TitledBorder("Silence Level"));
		add(manualOrExamplePanel, gbc);
		
		add(settingPanel, gbc);
		
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(new JPanel(), gbc);			
	}
	
	/**
	 * Initialize settings Panel
	 */
	private void initializeSettingsPanel(){		
		int initialSilenceDuration = SilenceRecognizer.DEFAULT_SILENCE_DURATION;
		int initialNonSilenceDuration = SilenceRecognizer.DEFAULT_NON_SILENCE_DURATION;
		
		settingsPanel = new JPanel();
		//settingsPanel.setBorder(new TitledBorder("Settings"));
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(Box.createVerticalStrut(4));
		
//		minimalSilenceDurationLabel = new JLabel(
//				ElanLocale.getString("Recognizer.Silence.MinimalSilenceDuration") + 
//				" " + initialSilenceDuration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
		minimalSilenceDurationLabel = new JLabel();// or use hardcoded English strings
		settingsPanel.add(minimalSilenceDurationLabel);
		
		minimalSilenceDuration = new JSlider(JSlider.HORIZONTAL, 0, 1000, initialSilenceDuration);
		minimalSilenceDuration.setMajorTickSpacing(200);
		minimalSilenceDuration.setMinorTickSpacing(25);
		minimalSilenceDuration.setPaintTicks(true);
		minimalSilenceDuration.setPaintLabels(true);
		minimalSilenceDuration.addChangeListener(this);
		settingsPanel.add(minimalSilenceDuration);
		settingsPanel.add(Box.createVerticalStrut(4));
		
//		minimalNonSilenceDurationLabel = new JLabel(
//				ElanLocale.getString("Recognizer.Silence.MinimalNonSilenceDuration") + 
//				" " + initialNonSilenceDuration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
		minimalNonSilenceDurationLabel = new JLabel();
		settingsPanel.add(minimalNonSilenceDurationLabel);
		
		minimalNonSilenceDuration = new JSlider(JSlider.HORIZONTAL, 0, 1000, initialNonSilenceDuration);
		minimalNonSilenceDuration.setMajorTickSpacing(200);
		minimalNonSilenceDuration.setMinorTickSpacing(25);
		minimalNonSilenceDuration.setPaintTicks(true);
		minimalNonSilenceDuration.setPaintLabels(true);
		minimalNonSilenceDuration.addChangeListener(this);
		settingsPanel.add(minimalNonSilenceDuration);
	}
	
	/**
	 * Change event handling.
	 * 
	 * @param e the change event
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
    	int duration = (int)source.getValue();
        
        if (source == minimalSilenceDuration) {
        	if (languageBundle != null) {
        		minimalSilenceDurationLabel.setText(
        				languageBundle.getString("Recognizer.Silence.MinimalSilenceDuration") + 
	        			" " + duration + " " + languageBundle.getString("PlayAroundSelDialog.Ms"));
        	} else {
        		// use hardcoded English strings		
        	}
        } else if (source == minimalNonSilenceDuration) {
        	if (languageBundle != null) {
        		minimalNonSilenceDurationLabel.setText(
        				languageBundle.getString("Recognizer.Silence.MinimalNonSilenceDuration") +
        			" " + duration + " " + languageBundle.getString("PlayAroundSelDialog.Ms"));
        	} else {
        		// use hardcoded English strings		
        	}        	
        }
    }

	/**
	 * Returns the current minimal silence duration slider value.
	 * 
	 * @return the current minimal silence duration value
	 */
	public int getMinimalSilenceDuration() {
		return minimalSilenceDuration.getValue();
	}
	
	/**
	 * Returns the current minimal non-silence duration slider value.
	 * 
	 * @return the current minimal non-silence duration value
	 */
	public int getMinimalNonSilenceDuration() {
		return minimalNonSilenceDuration.getValue();
	}
	
	public void updateLocale(Locale locale) {
		// update the labels with hardcoded strings
		if (languageBundle == null) {
			updateLocaleBundle(null);
		}
	}
	
	/**
	 * Updates the UI elements with localized strings from the resource bundle
	 * @param bundle the new resource bundle
	 */
	public void updateLocaleBundle(ResourceBundle bundle) {
		if (bundle != null) {
			languageBundle = bundle;
			int duration = minimalSilenceDuration.getValue();
	    	minimalSilenceDurationLabel.setText(
	    			bundle.getString("Recognizer.Silence.MinimalSilenceDuration") + 
	    			" " + duration + " " + bundle.getString("PlayAroundSelDialog.Ms"));
	    	duration = minimalNonSilenceDuration.getValue();
	    	minimalNonSilenceDurationLabel.setText(
	    			bundle.getString("Recognizer.Silence.MinimalNonSilenceDuration") +
	    			" " + duration + " " + bundle.getString("PlayAroundSelDialog.Ms"));
	    	selectionPanel.updateLocaleBundle(bundle);
	    	manualOrExamplePanel.updateLocale();
		} else {
			// use hardcoded English strings
		}
	}
	
	/**
	 * Updates the media files that are
	 * supported by this recognizer
	 * 
	 * @param mediaFiles
	 */
	public void updateMediaFiles(List<String> mediaFiles) {
		if(mediaFilesComboBox == null ){
			mediaFilesComboBox = new JComboBox();
		}
		
		if(mediaFilesList == null){
			mediaFilesList = new ArrayList<String>();
		}
		
		mediaFilesComboBox.removeAllItems();
		mediaFilesList.clear();
		if(mediaFiles != null && mediaFiles.size() > 0){
			List<String> fileNameList = new ArrayList<String>();
			for(String media : mediaFiles){
				String fileName = fileNameFromPath(media);
				if(fileNameList.contains(fileName)){
					mediaFilesComboBox.addItem(media);
				} else{
					mediaFilesComboBox.addItem(fileName);
					fileNameList.add(fileName);
				}
				mediaFilesList.add(media);
			}			
			mediaFilesComboBox.setSelectedIndex(0);
		}
	}
	
	/**
	 * Extracts the file name from a path.
	 * 
	 * @param path the file path
	 * @return the file name
	 */
	private String fileNameFromPath(String path) {
		if (path == null) {
			return "Unknown";
		}
		// assume all paths have forward slashes
		int index = path.lastIndexOf('/');
		if (index > -1 && index < path.length() - 1) {
			return path.substring(index + 1);
		}
		
		return path;
	}

	/**
	 * Returns the current settings.
	 */
	@Override
	public Map<String, Object> getParamPreferences() {
		Map <String, Object> sps = new HashMap<String, Object>(5);
		sps.put("MinimalSilenceDuration", Integer.valueOf(minimalSilenceDuration.getValue()));
		sps.put("MinimalNonSilenceDuration", Integer.valueOf(minimalNonSilenceDuration.getValue()));
		sps.put("ManualNoiseThreshold", new Double(getNoiseThreshold()));
		sps.put("NoiseThresholdSetManually", Boolean.valueOf(isNoiseThresholdSetManually()));
		sps.put("SelectionPanelPref", selectionPanel.getStorableParamPreferencesMap(selectionPanel.getParamValue()));
		
		return sps;
	}

	/**
	 * Restores the last used settings.
	 */
	@Override
	public void setParamPreferences(Map<String, Object> storedPrefs) {
		if (storedPrefs != null) {
			Object val;
			val = storedPrefs.get("MinimalSilenceDuration");
			if (val instanceof Integer) {
				minimalSilenceDuration.setValue((Integer) val);
			}
			val = storedPrefs.get("MinimalNonSilenceDuration");
			if (val instanceof Integer) {
				minimalNonSilenceDuration.setValue((Integer) val);
			}
			val = storedPrefs.get("ManualNoiseThreshold");
			if (val instanceof Double) {
				setNoiseThreshold((Double) val);
			}			
			val = storedPrefs.get("NoiseThresholdSetManually");
			if (val instanceof Boolean) {
				setNoiseThresholdSetManually((Boolean) val);
			}			
			val = storedPrefs.get("SelectionPanelPref");
			if (val instanceof HashMap) {
				selectionPanel.setParamValue((HashMap) val);
			}
		}		
	}

	/**
	 * Returns the selected media file 
	 * 
	 * @return
	 */
	public String getSelectedMediaFile() {		
		return mediaFilesList.get(mediaFilesComboBox.getSelectedIndex());
	}
	
	public void validateParameters() throws RecognizerConfigurationException {
		// If direct threshold is set, no selections are needed.
		if (!isNoiseThresholdSetManually()) {
			if (getSelections() == null) {
				if (languageBundle != null) {
					throw new RecognizerConfigurationException(languageBundle.getString("Recognizer.RecognizerPanel.Warning.Selection"));
				} else {
					throw new RecognizerConfigurationException("There are no selections provided.");// 
				}
			}
		}
	}
	
	/**
	 * Returns the selections made in the selection panel
	 * 
	 * @return
	 */
	public ArrayList<RSelection> getSelections(){
		Object value = selectionPanel.getSelectionValue();		
		
		if(value instanceof ArrayList){
			return ((ArrayList<RSelection>)value);
		} 	
		
		return null;
	}
	
	/**
	 * The user-set noise threshold.
	 * @return noise threshold in the range [0, 1].
	 */
	public double getNoiseThreshold() {
		return manualOrExamplePanel.getNoiseThreshold();

	}
	
	public void setNoiseThreshold(double val) {
		manualOrExamplePanel.setNoiseThreshold(val);
	}
	
	/**
	 * @returns true if the noise threshold is set manually,
	 * or false if it is set by example (with selections or tier).
	 */
	public boolean isNoiseThresholdSetManually() {
		return manualOrExamplePanel.isNoiseThresholdSetManually();
	}

	public void setNoiseThresholdSetManually(boolean manual) {
		manualOrExamplePanel.setNoiseThresholdSetManually(manual);
	}

	/*
	 * Private class follows.
	 */
	
	private class ManualOrExamplePanel extends JPanel implements ActionListener, ChangeListener {
		private final static int SLIDER_DB_RANGE = 90;

		private JRadioButton manualRB;
		private JRadioButton byExampleRB;
		private JPanel changingPanel;
		private JLabel levelSliderLabel;
		private JSlider levelSlider;
		
		public ManualOrExamplePanel() {
		
			manualRB = new JRadioButton();
			byExampleRB = new JRadioButton();
			
			ButtonGroup group = new ButtonGroup();
			group.add(manualRB);
			group.add(byExampleRB);
			
			manualRB.addActionListener(this);
			byExampleRB.addActionListener(this);

			changingPanel = new JPanel();
			
			levelSliderLabel = new JLabel();
			
			levelSlider = new JSlider(JSlider.HORIZONTAL, -SLIDER_DB_RANGE, 0, (int)factorTodBRMS(SilenceRecognizer.DEFAULT_NOISE_THRESHOLD));
			levelSlider.setMajorTickSpacing(10);
			levelSlider.setMinorTickSpacing(5);
			levelSlider.setPaintTicks(true);
			levelSlider.setPaintLabels(true);
			levelSlider.addChangeListener(this);

			// Layout
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			add(manualRB, gbc);
			
			gbc.gridx = 1;
			add(byExampleRB, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			changingPanel.setLayout(new GridBagLayout());
			add(changingPanel, gbc);
			
			updateLocale();
			
			// If there is no preference, this remains the default:
			manualRB.doClick();
		}
		
		public void updateLocale() {
			if (languageBundle != null) {
				// "Select manually"
				manualRB.setText(languageBundle.getString("Recognizer.Silence.SelectManually"));
				// "Select by example"
				byExampleRB.setText(languageBundle.getString("Recognizer.Silence.SelectByExample"));
				updateLevelSliderLabel(levelSlider.getValue());
			} else {
				// apply English text 
				manualRB.setText("Select manually");
				byExampleRB.setText("Select by example");
			}
		}
		
		private void updateLevelSliderLabel(int level) {
			String s = null;
			if (languageBundle != null) {
				s = String.format("%s %d %s (%3.3f %%)", 
					// "Silence level"
						languageBundle.getString("Recognizer.Silence.SilenceLevel"),
					level, 
					// "dB RMS"
					languageBundle.getString("Recognizer.Silence.Decibel"),
					100 * dBRMSToFactor((double)level));
			} else {
				s = String.format("%s %d %s (%3.3f %%)", 
						"Silence level",
						//	languageBundle.getString("Recognizer.Silence.SilenceLevel"),
						level, 
						"dB RMS",
						//languageBundle.getString("Recognizer.Silence.Decibel"),
						100 * dBRMSToFactor((double)level));
			}
			levelSliderLabel.setText(s);
		}	
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			if (source instanceof JRadioButton) {
				if (source == manualRB) {
					showManualControls(true);
				} else if (source == byExampleRB) {
					showManualControls(false);
				}
			}
		}
		
		public void showManualControls(boolean manually) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			changingPanel.removeAll();

			if (manually) {
				gbc.insets = new Insets(5, 20, 1, 1);				
				changingPanel.add(levelSliderLabel, gbc);
				
				gbc.insets = new Insets(0,0,0,0);
				changingPanel.add(levelSlider, gbc);					
			} else {
				changingPanel.add(selPanel, gbc);					
			}
			revalidate();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider) e.getSource();
	    	int level = (int)source.getValue();
	        
	        if (source == levelSlider) {
	        	updateLevelSliderLabel(level);
	        } 
		}

		/**
		 * The user-set noise threshold.
		 * @return noise threshold in the range [0, 1].
		 */
		public double getNoiseThreshold() {
			return dBRMSToFactor((double)levelSlider.getValue());
		}

		public void setNoiseThreshold(double value) {
			levelSlider.setValue((int)factorTodBRMS(value));
		}

		/**
		 * The scale shown to the user is in dB(RMS).
		 *  Convert this according to the formula
		 *    dBRMS = 20 * log10 ( rms1 / rms2 )
		 * where rms1 is the noise level
		 * and   rms2 is the peak level which we'll set to to 1 here.
		 */
		private double factorTodBRMS(double factor) {
			return 20 * Math.log10(factor);
		}
		
		/**
		 * @see factorTodBRMS
		 * @param dB_RMS
		 * @return linear factor
		 */
		
		private double dBRMSToFactor(double dB_RMS) {
			return Math.pow(10, dB_RMS / 20.0);
		}
		
		
		public boolean isNoiseThresholdSetManually() {
			return manualRB.isSelected();
		}

		public void setNoiseThresholdSetManually(boolean manually) {
			if (manually) {
				manualRB.doClick();
			} else {
				byExampleRB.doClick();
			}
		}
	}
}
