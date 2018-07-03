package nl.mpi.recognizer.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.RSelection;

@SuppressWarnings("serial")
public class DemoRecognizerPanel extends JPanel implements ChangeListener, ParamPreferences {
	private JLabel stepDurationLabel;
	private JSlider stepSlider;
	/* First combobox will be populated by the files passed by the Host 
	 * based on the type of the Recognizer */
	private JComboBox mediaFilesComboBox;
	/* For demonstration purposes a second box is populated with media files of one 
	 * of the other media types  */
	private JComboBox mediaFilesComboBox2;
	
	private JPanel settingsPanel;
	private AbstractSelectionPanel selectionPanel;
	
	private ArrayList<String> mediaFilesList;
	private RecognizerHost host;
	
	/**
	 * Constructor 
	 */
	public DemoRecognizerPanel(AbstractSelectionPanel selectionPanel, RecognizerHost host) {
		this.selectionPanel = selectionPanel;
		this.host = host;
		selectionPanel.enableFileSelection(false);
		initComponents();
	}
	
	/**
	 * Initializes the components.
	 */
	private void initComponents(){		
		mediaFilesComboBox = new JComboBox();
		mediaFilesComboBox2 = new JComboBox();
		
		JPanel filePanel = new JPanel(new GridBagLayout());			
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		filePanel.add(new JLabel("Files List 1:"), gbc);
		
		gbc.gridx = 1;	
		gbc.weightx = 1.0;
		filePanel.add(mediaFilesComboBox, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		filePanel.add(new JLabel("Files List 2:"), gbc);
		
		gbc.gridx = 1;	
		gbc.weightx = 1.0;
		filePanel.add(mediaFilesComboBox2, gbc);
		
		JPanel selPanel = new JPanel(new GridBagLayout());
		selPanel.setBorder(new TitledBorder("Selection Panel"));
		gbc = new GridBagConstraints();			
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.weightx = 1.0;
		selPanel.add(selectionPanel, gbc);		
		
		initializeSettingsPanel();
	
		setLayout(new GridBagLayout());		
		
		gbc = new GridBagConstraints();
		gbc.gridy = 0;		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 2, 4, 2);
		add(filePanel, gbc);
		
		gbc.gridy = 1;		
		add(selPanel, gbc);
		
		gbc.gridy = 2;			
		add(settingsPanel, gbc);
		
		gbc.gridy = 3;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(new JPanel(), gbc);			
	}
	
	/**
	 * Initialize settings Panel
	 */
	private void initializeSettingsPanel(){		
		int initialStepDuration = 5;
		
		settingsPanel = new JPanel();		
		settingsPanel.setBorder(new TitledBorder("Settings"));
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(Box.createVerticalStrut(10));
		
		stepDurationLabel = new JLabel("Step Duration: " + initialStepDuration + " seconds");
		settingsPanel.add(stepDurationLabel);
		
		stepSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, initialStepDuration);
		stepSlider.setMajorTickSpacing(1);
		stepSlider.setPaintTicks(true);
		stepSlider.setPaintLabels(true);
		stepSlider.addChangeListener(this);
		settingsPanel.add(stepSlider);
	}
	
	/**
	 * Updates the media files that are supported by this recognizer. 
	 * 
	 * For demonstration purposes a second box is populated with media files of one 
	 * of the other media types. The call to setMedia() of Recognizer is a good moment 
	 * for getting the list(s) of other types than the main media type. 
	 * 
	 * @param mediaFiles the media files provided by the host, based on the main type of the recognizer
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

		// Fill the second combobox with files obtained from the host, in this 
		// case the files of type Video are fetched
		mediaFilesComboBox2.removeAllItems();
		List<String> hostFiles = host.getMediaFiles(Recognizer.VIDEO_TYPE);
		if (hostFiles != null) {
			for (String fileName : hostFiles) {
				// this shows the full path of the file
				//mediaFilesComboBox2.addItem(fileName);
				
				// this shows the name of the file, the full path has to be stored separately 
				// in order to be able to really access the file
				mediaFilesComboBox2.addItem(fileNameFromPath(fileName));
			}
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
	
	@Override
	public void stateChanged(ChangeEvent e) {        
        if (e.getSource() == stepSlider) {
        	stepDurationLabel.setText("Step Duration: " + stepSlider.getValue() + " seconds");
        }
    }

	public int getStepDuration() {
		return stepSlider.getValue();
	}
	
	public void setStepDuration(int stepDur) {
		if (stepDur >= stepSlider.getMinimum() && stepDur <= stepSlider.getMaximum()) {
			stepSlider.setValue(stepDur);
		}
	}

	/**
	 * Returns the current settings.
	 */
	@Override
	public Map<String, Object> getParamPreferences() {
		Map<String, Object> prefs = new HashMap<String, Object>(1);
		prefs.put("StepDuration", stepSlider.getValue());
		prefs.put("SelectionPanelPref", selectionPanel.getStorableParamPreferencesMap(selectionPanel.getParamValue()));
		
		return prefs;
	}

	/**
	 * Restores the last used settings.
	 */
	@Override
	public void setParamPreferences(Map<String, Object> storedPrefs) {
		if (storedPrefs != null) {
			Object val = storedPrefs.get("StepDuration");
			if (val instanceof Integer) {
				int step = ((Integer) val).intValue();
				
				if (step >= stepSlider.getMinimum() && step <= stepSlider.getMaximum()) {
					stepSlider.setValue(step);
				}
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
	 * @return the selected media file or an empty String
	 */
	public String getSelectedMediaFile() {
		if (mediaFilesList.size() > 0) {
			int selIndex = mediaFilesComboBox.getSelectedIndex();
			if (selIndex > -1 && selIndex < mediaFilesComboBox.getItemCount()) {
				return mediaFilesList.get(selIndex);
			}
		} else {
			if (mediaFilesComboBox2.getSelectedIndex() > -1) {
				return (String) mediaFilesComboBox2.getSelectedItem();
			}
		}
		
		return "";
	}
	
	public void validateParameters() throws RecognizerConfigurationException {
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
}
