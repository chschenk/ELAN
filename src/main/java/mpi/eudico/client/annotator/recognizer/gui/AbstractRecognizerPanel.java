package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.SegmentsToTiersCommand;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.CompoundIcon;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.HTMLViewer;
import mpi.eudico.client.annotator.recognizer.api.LocalRecognizer;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.RecogAvailabilityDetector;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.api.SharedRecognizer;
import mpi.eudico.client.annotator.recognizer.data.AudioSegment;
import mpi.eudico.client.annotator.recognizer.data.BoundarySegmentation;
import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.io.CsvTierIO;
import mpi.eudico.client.annotator.recognizer.io.CsvTimeSeriesIO;
import mpi.eudico.client.annotator.recognizer.io.ParamIO;
import mpi.eudico.client.annotator.recognizer.io.RecTierWriter;
import mpi.eudico.client.annotator.recognizer.io.XmlTierIO;
import mpi.eudico.client.annotator.recognizer.io.XmlTimeSeriesReader;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public abstract class AbstractRecognizerPanel extends JComponent implements ActionListener, Runnable, 
RecognizerHost, ElanLocaleListener, ItemListener, ACMEditListener {
	protected ViewerManager2 viewerManager;
	// param panel provided by recognizer or created based on configuration file
	protected JPanel controlPanel;
	protected JPanel progressPanel;
	protected JPanel recognizerAndFilesPanel;
	protected JPanel paramPanel;
	protected JScrollPane jsp;
	protected JButton detachButton;
	protected List<List<String>> mediaFilePaths;		// paths per recognizer type
	protected List<List<String>> supportedMediaFiles;	// paths per recognizer type
	protected JLabel recognizerLabel;
	protected JComboBox recognizerList;	
	protected Map<String, Recognizer> recognizers;
	protected Recognizer currentRecognizer;
	protected Map<String, Segmentation> segmentations;
	protected JProgressBar progressBar;
	protected JButton startStopButton;
	protected JButton reportButton;
	protected JButton createSegButton;
	protected JPanel paramButtonPanel;
	protected JButton saveParamsButton;
	protected JButton loadParamsButton;
	protected JButton helpButton;
	protected JButton configureButton;	
	protected boolean isRunning;
	protected boolean notMono;
	protected boolean reduceFilePrompt = true;
	protected long lastStartTime = 0L;
	protected Timer elapseTimer;
	protected StringBuilder progressReport;	// use StringBuilder to be thread-safe.
	protected boolean needToPollProgressReport;
	protected Method getReport;
	
	private boolean detached = false;
	private ParamDialog detachedDialog;	

	private Map<String, TierSelectionPanel> selPanelMap;
	private JTextArea progressReportTextArea;
	private ImageIcon soundIcon;				// the icon representing audio recognizers
	private ImageIcon movieIcon;				// the icon representing video recognizers
	private ImageIcon otherIcon;				// the icon representing other recognizers
	private Map<String,Icon> recognizerIcon;	// recognizer name => its Icon

	/**
	 *  Initializes data structures and user interface components.
	 */
	public AbstractRecognizerPanel(ViewerManager2 viewerManager) {
		super();

		this.viewerManager = viewerManager;
		this.viewerManager.connectListener(this);

		mediaFilePaths = new ArrayList<List<String>>(Recognizer.NUM_FILE_TYPES);
		supportedMediaFiles = new ArrayList<List<String>>(Recognizer.NUM_FILE_TYPES);
		for (int i = 0; i < Recognizer.NUM_FILE_TYPES; i++) {
			this.mediaFilePaths.add(new ArrayList<String>());
			supportedMediaFiles.add(new ArrayList<String>());
		}

		segmentations = new HashMap<String, Segmentation>();
		selPanelMap = new HashMap<String, TierSelectionPanel>();
		
		initComponents();
		initRecognizers();
	}
	
	/**
	 * Initialize interface components
	 */
	protected void initComponents() {
		
		Boolean boolPref = Preferences.getBool("Recognizer.ReduceFilePrompts", null);
		if (boolPref != null){
			reduceFilePrompt = boolPref.booleanValue();
		}
		setLayout(new GridBagLayout());

		recognizerLabel = new JLabel();
		recognizerList = new JComboBox();
		recognizerAndFilesPanel = new JPanel(new GridBagLayout());		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 2, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		recognizerAndFilesPanel.add(recognizerLabel, gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		recognizerAndFilesPanel.add(recognizerList, gbc);		
		
		// start/stop and progress information panel		
		JPanel progPanel = new JPanel();		
		progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.X_AXIS));
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progPanel.add(Box.createHorizontalStrut(10));
		progPanel.add(progressBar);
		progPanel.add(Box.createHorizontalStrut(10));
		startStopButton = new JButton(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
		startStopButton.addActionListener(this);
		progPanel.add(startStopButton);	
		reportButton = new JButton();
		reportButton.addActionListener(this);
		reportButton.setEnabled(false);
		progPanel.add(Box.createHorizontalStrut(15));
		progPanel.add(reportButton);
		createSegButton = new JButton();
		createSegButton.addActionListener(this);
		createSegButton.setEnabled(false);
		progPanel.add(Box.createHorizontalStrut(15));
		progPanel.add(createSegButton);
		
		progressPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		progressPanel.add(progPanel, gbc);
		
		elapseTimer = new Timer();
		progressReport = new StringBuilder();
		
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		progressPanel.add(elapseTimer.getTimerPanel(), gbc);
		
		paramPanel = new JPanel(new GridBagLayout());
		paramButtonPanel = new JPanel(new GridBagLayout());	
		
		saveParamsButton = new JButton();
		saveParamsButton.addActionListener(this);
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource(
					"/toolbarButtonGraphics/general/SaveAs16.gif"));
			saveParamsButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			saveParamsButton.setText("S");
		}
		
		loadParamsButton = new JButton();
		loadParamsButton.addActionListener(this);
		try {
			ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
			loadParamsButton.setIcon(openIcon);
		} catch (Exception ex) {
			// catch any image loading exception
			loadParamsButton.setText("L");
		}
		
		detachButton = new JButton();
		detachButton.addActionListener(this);
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource(
					"/mpi/eudico/client/annotator/resources/Detach.gif"));
			detachButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			detachButton.setText("D");
		}
		
		configureButton = new JButton();
		configureButton.addActionListener(this);
		try {
			ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Configure16.gif"));
			configureButton.setIcon(openIcon);
		} catch (Exception ex) {
			// catch any image loading exception
			configureButton.setText("C");
		}	
		
		helpButton = new JButton();	
		helpButton.addActionListener(this);		
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help16.gif"));
			helpButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			helpButton.setText("H");
		}		 
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHEAST;	
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 2, 0, 2);	
		paramButtonPanel.add(loadParamsButton, gbc);
		
		gbc.gridx = 1;			
		paramButtonPanel.add(saveParamsButton, gbc);		
		
		gbc.gridx = 2;
		gbc.insets = new Insets(0, 10, 0, 2);
		paramButtonPanel.add(detachButton, gbc);	
		
		gbc.gridx = 3;
		gbc.insets = new Insets(0, 2, 0, 2);	
		paramButtonPanel.add(configureButton, gbc);
		
		gbc.gridx = 4;		
		paramButtonPanel.add(helpButton, gbc);
		
		// add components
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		add(recognizerAndFilesPanel, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;	
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(paramPanel, gbc);		
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;	
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		add(progressPanel, gbc);
	}
	
	static Color secondColor = new Color(234,245,245);

	/**
	 * Initializes recognizer and related ui elements, only once.
	 */
	protected void initRecognizers() {
		Map<String, Recognizer> audio = RecogAvailabilityDetector.getAudioRecognizers();
		Map<String, Recognizer> video = RecogAvailabilityDetector.getVideoRecognizers();
		Map<String, Recognizer> other = RecogAvailabilityDetector.getOtherRecognizers();
		
		recognizers = new HashMap<String, Recognizer>(audio.size() + video.size() + other.size());
		recognizers.putAll(audio);
		recognizers.putAll(video);
		recognizers.putAll(other);

		if (currentRecognizer != null) {
			paramPanel.removeAll();
			currentRecognizer = null;
		}
		
		try {
			// Possibilities:
			// /toolbarButtonGraphics/media/Volume16.gif or /mpi/eudico/client/annotator/resources/Notes16.gif
			//                              Movie16.gif
			//                              Zoom16.gif or StepForward16.gif or Search16.gif
			// /mpi/eudico/client/annotator/resources/audio.png
			//                                        video.png
			//                                        writtenresource.png
			//soundIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/media/Volume16.gif"));
			soundIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Notes16.gif"));
			movieIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/media/Movie16.gif"));
			otherIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Zoom16.gif"));
		} catch (Exception ex) {// any
		}

		recognizerList.removeItemListener(this);
		recognizerList.removeAllItems();
		recognizerIcon = new HashMap<String, Icon>(recognizers.size());
		
		// Set a custom renderer to add an icon to each entry in the combobox.
		recognizerList.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				if (c instanceof JLabel) { // normally true
					JLabel label = (JLabel)c;
					Icon icon = recognizerIcon.get(value);
					label.setIcon(icon);
				}
				return c;
			}
		});
		
		fillRecognizerList(audio, soundIcon);
		fillRecognizerList(video, movieIcon);
		fillRecognizerList(other, otherIcon);
		
		recognizerList.setSelectedIndex(-1);
		
		if (recognizerList.getItemCount() == 0) {
			recognizerList.addItem(ElanLocale.getString("Recognizer.RecognizerPanel.No.Recognizers"));
			//paramPanel.add(new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.No.Parameters")), BorderLayout.CENTER);
			
			jsp = new JScrollPane(getParamPanel(null));
			jsp.setBackground(getBackground());
			jsp.getViewport().setBackground(getBackground());
			jsp.getVerticalScrollBar().setUnitIncrement(20);	
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
			
			startStopButton.setEnabled(false);
			recognizerList.setEnabled(false);
			createSegButton.setEnabled(false);
			paramButtonPanel.setVisible(false);
		} else {
			String lastActiveRecognizer;
			lastActiveRecognizer = Preferences.getString("ActiveRecognizerName", viewerManager.getTranscription()); 
			
			if(lastActiveRecognizer != null && recognizers.containsKey(lastActiveRecognizer)){
				recognizerList.setSelectedItem(lastActiveRecognizer);
				setRecognizer(lastActiveRecognizer);
			} else
			
			if (recognizerList.getSelectedIndex() < 0 && recognizerList.getModel().getSize() > 0) {
				recognizerList.setSelectedIndex(0);
				setRecognizer((String) recognizerList.getSelectedItem());
				recognizerList.setEnabled(true);
			}
		}
		recognizerList.addItemListener(this);
	}

	/**
	 * Put some recognizers into the combobox in sorted order.
	 * Each recognizer gets an icon according to its type (audio, video, other).
	 * If it has an icon of its own, both are shown, by creating a CompoundIcon.
	 * 
	 * @param toAdd a Map mapping to a bunch of recognizers.
	 */
	private void fillRecognizerList(Map<String, Recognizer> toAdd, Icon typeIcon) {
		if (!toAdd.isEmpty()) {
			List<String> recognizers = new ArrayList<String>(toAdd.keySet());
			Collections.<String>sort(recognizers);
			
			for (String name : recognizers) {
				recognizerList.addItem(name);
				Icon recogIcon = typeIcon;
				// Check if the recognizer has an icon of its own.
				// If so, combine it with the icon representing the recognizer's type.
				URL iconRef = RecogAvailabilityDetector.getIconURL(name);
				if (iconRef != null) {
					Icon icon2 = new ImageIcon(iconRef);
					recogIcon = new CompoundIcon(typeIcon, icon2);
				}
				recognizerIcon.put(name, recogIcon);
			}
		}
	}
	
	/**
	 * @return the currently available recognizers, mixed audio, video and other.
	 */
	protected Map<String, Recognizer> getAvailableRecognizers() {
		return recognizers;
	}
	
	/**
	 * Updates the media files that supported by the
	 * current recognizer
	 */
	protected void updateSupportedFiles(){
		// check the current media files	
		for (int i = 0; i < mediaFilePaths.size(); i++) {
			List <String> smfL = supportedMediaFiles.get(i);
			smfL.clear();
			for (String mediaFilePath : mediaFilePaths.get(i)) {
				if (currentRecognizer.canHandleMedia(mediaFilePath)) {
					smfL.add(mediaFilePath);
				} 
			}
		}
	}
	
	/**
	 * Method called before closing the recognizer to
	 * store the preferences
	 */
	public void isClosing(){
		if (currentRecognizer != null) {
			// store preferences
			if (controlPanel instanceof ParamPreferences) {
				Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
				if (prefs != null) {
					Preferences.set(currentRecognizer.getName(), 
						prefs, viewerManager.getTranscription(), false, false);
				}
			}
			Preferences.set("ActiveRecognizerName", currentRecognizer.getName(), viewerManager.getTranscription(), false, false);
			Preferences.set("Recognizer.ReduceFilePrompts", reduceFilePrompt, null, false, false);
		}
	}
	
	/**
	 * Sets the recognizer, gets the parameter panel, updates the files list.
	 *  
	 * @param name the name of the recognizer
	 */
	protected void setRecognizer(String name) {
		if (currentRecognizer != null) {
			if (currentRecognizer.getName().equals(name)) {
				return;
			}
			
			// store preferences
			if (controlPanel instanceof ParamPreferences) {
				Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
				if (prefs != null) {
					Preferences.set(currentRecognizer.getName(), 
						((ParamPreferences) controlPanel).getParamPreferences(), viewerManager.getTranscription());
				}
			}
			currentRecognizer.dispose();
			paramPanel.removeAll();
			segmentations.clear();
			createSegButton.setEnabled(false);
		}
		
		selPanelMap.clear();
		elapseTimer.resetTimer();
		currentRecognizer = recognizers.get(name);
		checkForOldRecognizerInterface();
		currentRecognizer.setRecognizerHost(this);
		setRecognizerResourceBundle();
		
		updateSupportedFiles();	
		
		int mode = currentRecognizer.getRecognizerType();
		currentRecognizer.setMedia(supportedMediaFiles.get(mode));		
		
		controlPanel = currentRecognizer.getControlPanel();
		if(controlPanel == null){			
			// check if there are parameters, create factory panel	
			controlPanel = getParamPanel(currentRecognizer);
		}		
		
		if(controlPanel != null){
			jsp = new JScrollPane(controlPanel);
			jsp.getVerticalScrollBar().setUnitIncrement(20);
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
				
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.NORTHEAST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			paramPanel.add(paramButtonPanel, gbc);
		}else {
			jsp = null;

			// label, no configurable params
			paramPanel.add(new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.No.Parameters")), new GridBagConstraints());
			paramPanel.repaint();
		}
			
		if (controlPanel instanceof ParamPreferences) {
			Map<String, ?> prefs = Preferences.getMap(currentRecognizer.getName(), viewerManager.getTranscription());
			if (prefs != null) {
				((ParamPreferences) controlPanel).setParamPreferences((Map<String, Object>) prefs);
			}
			loadParamsButton.setEnabled(true);
			saveParamsButton.setEnabled(true);
		} else {
			loadParamsButton.setEnabled(false);
			saveParamsButton.setEnabled(false);
		}
		
		if(RecogAvailabilityDetector.getHelpFile(currentRecognizer.getName()) == null){
			helpButton.setEnabled(false);
		}	 else {
			helpButton.setEnabled(true);
		}
		progressBar.setString("");
		progressBar.setValue(0);
		
		validate();
	}
	
	/**
	 * There may be old recognizers about that have not been updated to use
	 * the new RecognizerHost.appendToReport() method.
	 * Try to find this out: if the Recognizer has a getReport() method,
	 * it is likely to be old.
	 */
	protected void checkForOldRecognizerInterface() {
		// Find out if the Recognizer has the (old) getReport() method.
		// If so, it probably won't use the new RecognizerHost.appendReport() call.
		needToPollProgressReport = false;
		getReport = null;
		try {
			getReport = currentRecognizer.getClass().getMethod("getReport");
			needToPollProgressReport = getReport != null;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
			// this is in fact what we hope for. No old Recognizers.
		}
	}
	
	/**
	 * Returns a factory created user interface for setting parameters for the recognizer.
	 * 
	 * @param recognizer the recognizer
	 * 
	 * @return a parameter panel
	 */
	protected JPanel getParamPanel(Recognizer recognizer) {
		if (recognizer != null) {
			List<Param> params = RecogAvailabilityDetector.getParamList(recognizer.getName());
				
			if (params != null) {
				int mode = recognizer.getRecognizerType();
				ParamPanelContainer ppc = new ParamPanelContainer(recognizer.getName(), params, this, viewerManager, mode);
				startStopButton.setEnabled(ppc.checkStartReg());
				ppc.validate();
				return ppc;
			}
		}
		return null;
	}	
	
	/**
	 * Called after a change in the set of linked media files.
	 */
	protected void updateMediaFiles(){
		if(currentRecognizer == null){
			return ;
		}
		
		updateSupportedFiles();		

		int mode = currentRecognizer.getRecognizerType();
		List<String> smf = supportedMediaFiles.get(mode);
		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateMediaFiles(smf);
			startStopButton.setEnabled(((ParamPanelContainer)controlPanel).checkStartReg());			
		} else {
			if(smf.isEmpty()){
				startStopButton.setEnabled(false);	
			} else {
				startStopButton.setEnabled(true);	
			}
		}
		
		for (TierSelectionPanel panel : selPanelMap.values()) {
			if (panel != null) {
				panel.updateMediaFiles(smf);
			}
		}

		// update files list for the java plugin recognizers
		if(currentRecognizer != null){
			currentRecognizer.setMedia(smf);
		}
	}
	
	/**
	 * Called after a change in the tier.
	 */
	protected void updateTiers(int event){
		if(currentRecognizer == null){
			return ;
		}				
		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateTiers(event);			
		} else {			
			for (TierSelectionPanel panel : selPanelMap.values()) {
				if (panel != null) {
					panel.updateTierNames(event);
				}	
			}
		}
	}	
	
	/**
	 * Handling of combobox selection events.
	 * @param e the event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == recognizerList && e.getStateChange() == ItemEvent.SELECTED) {
			final String selectedItem = (String) recognizerList.getSelectedItem();

			if (currentRecognizer != null && isBusy()) {
				// tell the user that the current recognizer is still running
				JOptionPane.showMessageDialog(this,
					    currentRecognizer.getName() + ": " + ElanLocale.getString("Recognizer.RecognizerPanel.Warning.Busy"),
					    currentRecognizer.getName() + " " + ElanLocale.getString("Recognizer.RecognizerPanel.Warning.Busy2"),
					    JOptionPane.PLAIN_MESSAGE);
				// restore the current recognizers name in the combo box
				recognizerList.setSelectedItem(currentRecognizer.getName());
				return;
			}
			if (detached) {
				detachedDialog.dispose();
			}
			// reset the current segmentations
			//segmentations = new HashMap<String, Segmentation>();
			segmentations.clear();
			// remove current recognizer GUI
			paramPanel.removeAll();
			// set the new current recognizer
			setRecognizer(selectedItem);
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source.equals(startStopButton)) {
			if (currentRecognizer == null) {
				return;
			}
			if (isRunning) {
				startStopButton.setText(ElanLocale.getString(
				"Recognizer.RecognizerPanel.Start"));
				stopRecognizers();
				if (progressBar.isIndeterminate()) {
					progressBar.setIndeterminate(false);
				}
				progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Canceled"));
				progressBar.setValue(0);
				//setProgress(0, ElanLocale.getString("Recognizer.RecognizerPanel.Canceled"));
			} else {
				if (currentRecognizer.getRecognizerType() == Recognizer.AUDIO_TYPE) {
					final SignalViewer signalViewer = viewerManager.getSignalViewer();
					if (signalViewer != null) {
						signalViewer.setSegmentationChannel1(null);
						signalViewer.setSegmentationChannel2(null);
					}
				}
				startRecognizer();
			}
		}  
		else if (source == reportButton) {
			if (currentRecognizer != null) {
				showReport();
			}
		} else if (source == configureButton) {
			showConfigureDialog();
		}else if (source == saveParamsButton) {
			saveParameterFile();
		} else if (source == loadParamsButton) {
			loadParameterFile();
		} else if (source == detachButton) {
			detachParamPanel();
		} else if(source == helpButton){
			showHelpDialog();
		}else if( source == createSegButton){
			if (isBusy()) {
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString(
                            "SegmentsToTierDialog.Warning.Busy"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
        		return;
        	}
            List<Segmentation> segments = getSegmentations();

            if ((segments == null) || (segments.size() == 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "SegmentsToTierDialog.Warning.NoSegmentation"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }

            // needs the transcription
            Command cc = ELANCommandFactory.createCommand(viewerManager.getTranscription(),
                    ELANCommandFactory.SEGMENTS_2_TIER_DLG);
            cc.execute(viewerManager.getTranscription(),
                new Object[] { segments });
        
		}
	}
	
	protected void startRecognizer() {		
		if (isRunning) {
			return;
		}

		// Clear progress report
		progressReport = new StringBuilder();

		if(controlPanel instanceof ParamPanelContainer){				
			// get all the required input  && output parameters
			List<FileParamPanel> inputFPPS = new ArrayList<FileParamPanel>();
			List<FileParamPanel> outputFPPS = new ArrayList<FileParamPanel>();
			ParamPanelContainer ppc = (ParamPanelContainer) controlPanel;
			int numPanels = ppc.getNumPanels();
			int notFilled = 0;
			for (int i = 0; i < numPanels; i++) {
				AbstractParamPanel app = ppc.getParamPanel(i);
				if(app instanceof FileParamPanel && !((FileParamPanel)app).isOptional()){
					FileParamPanel ffp = (FileParamPanel)app;					
					
					if(ffp.isInputType()){
						if(!ffp.isValueFilled()){
							notFilled++;	
						}
						inputFPPS.add(ffp);	
					} else {
						outputFPPS.add(ffp);	
					}					
				}
			}
			
			// display a error message regarding the parameters that
			// doesn't have a value
			if(notFilled > 0){
				JOptionPane.showMessageDialog(this,									    
					ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyReqdParam"),	
				    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam"),
				    JOptionPane.ERROR_MESSAGE);	
				return;
			} 
			
			//validate all required input param values
			for(FileParamPanel panel : inputFPPS) {
				Object value = panel.getParamValue();
				String file = null;
				if(value instanceof Map){
					Map map = (Map)value;
					if(map.containsKey(TierSelectionPanel.SELECTIONS)){
						value = map.get(TierSelectionPanel.SELECTIONS);
					} else if(map.containsKey(TierSelectionPanel.TIER)){
						value = map.get(TierSelectionPanel.TIER);
					} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
						value = map.get(TierSelectionPanel.FILE_NAME);
					}
				} 
				
				if(value instanceof List){
					file = writeAndGetTierOrSelectionFile(panel, (List<RSelection>)value);
				}else if(value instanceof String){
					File tf = new File((String)value);
					if(tf != null && tf.exists()){
						file = value.toString();
					} else{
						JOptionPane.showMessageDialog(this, value.toString()+" file not found.",		
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}	
				
				if(file != null && file.length() > 0){
					currentRecognizer.setParameterValue(panel.getParamName(), file);
				} else {
//					JOptionPane.showMessageDialog(this, value.toString() +" file not found.",		
//							ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
//							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			
			//validate all required output param values
			for(FileParamPanel panel : outputFPPS) {	
				String val = (String) panel.getParamValue();
				if (val == null || val.length() == 0) {
					if (reduceFilePrompt) {
						// Create a file in the directory of the first media file.
						int mode = currentRecognizer.getRecognizerType();
						val = autoCreateOutputFile(mediaFilePaths.get(mode), 
								panel.getParamName(), panel.getContentType());							
					} else {
						// prompt file							
						FileChooser chooser = new FileChooser(this);
						List<String[]> extensions = panel.getFileTypeExtension();
						String[] mainFilterExt = null;
						if(extensions != null && extensions.size() > 0){
							mainFilterExt = extensions.get(0);
						}
						chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, extensions, mainFilterExt, "Recognizer.Path", null);
						val = chooser.getSelectedFile().getAbsolutePath();
					}
						
					// check again
					if (val == null || val.length() == 0)  {
						// prompt and return
						JOptionPane.showMessageDialog(this,									    
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam") + " \"" +
							    panel.description + "\" " +
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam2"),
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam"),
							    JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						panel.setParamValue(val);		
					}
				} else {
					// check if file exists and set access
					createOutputFile(val);
				}
					
				if(val != null && val.length() > 0){
					currentRecognizer.setParameterValue(panel.getParamName(), val);
				}  else {
					return;
				}
			}			
			
			// set other available parameters to the recognizer
			for (int i = 0; i < numPanels; i++) {
				AbstractParamPanel app = ppc.getParamPanel(i);
				// Skip everything that would be in inputFPPS or outputFPPS,
				// non-optional file parameters, since those were treated above.
				if(app instanceof FileParamPanel && !((FileParamPanel)app).isOptional()){
					continue;
				}
				Object value = app.getParamValue();
				
				if(app instanceof FileParamPanel){
					String file = null;
					FileParamPanel ffp = (FileParamPanel)app;	
					
					//if outputparam
					if(!ffp.isInputType()){
						if(value != null){							
							file = value.toString();
						}	
					}else {
						if(value instanceof Map){
							Map map = (Map)value;
							if(map.containsKey(TierSelectionPanel.SELECTIONS)){
								value = map.get(TierSelectionPanel.SELECTIONS);
							} else if(map.containsKey(TierSelectionPanel.TIER)){
								value = map.get(TierSelectionPanel.TIER);
							} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
								value = map.get(TierSelectionPanel.FILE_NAME);
							}
						} 
											
						
						if(value instanceof List){
							file = writeAndGetTierOrSelectionFile(ffp, (List<RSelection>)value);			
						} else if(value instanceof String){
							File tf = new File((String)value);
							if(tf != null &&tf.exists()){
								file = value.toString();
							}
						}
					}
					
					// NOTE: This extra length check means that for a TierSelector, if you
					// select an empty file name in the GUI, the previously selected value
					// from the previous run remains in effect.
					if(file != null /*&& file.length() > 0*/) {
						currentRecognizer.setParameterValue(ffp.getParamName(), file);
					} else {
						currentRecognizer.setParameterValue(ffp.getParamName(), null);
					}
				} else	if (value instanceof Float) {
					currentRecognizer.setParameterValue(app.paramName, ((Float) value).floatValue());
				} else if (value instanceof Double) {
					currentRecognizer.setParameterValue(app.paramName, ((Double) value).floatValue());
				} else if (value instanceof String) {
					currentRecognizer.setParameterValue(app.paramName, (String) value);
				}
			}
		} else {
			// java plug-in
		}
		
		 try {
				currentRecognizer.validateParameters();
			} catch (RecognizerConfigurationException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(),		
					    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
		
		// store preferences
		if (controlPanel instanceof ParamPreferences) {
//			Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
//			if (prefs != null) {
//				Preferences.set(currentRecognizer.getName(), 
//					((ParamPreferences) controlPanel).getParamPreferences(), null);
//			}
		}
		// clear the list of segmentations created by a previous run
		segmentations.clear();

		progressBar.setValue(0);
		progressBar.setString("");
		
		startStopButton.setText(ElanLocale.getString("Button.Cancel"));
		// Once we are starting the recognizer, there can be a progress report.
		reportButton.setEnabled(true);
		isRunning = true;
		progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Recognizing"));
		new Thread(this).start();	
		elapseTimer.start();
	}

	/**
	 * Prompts the user to specify a location where to store the selections.
	 * @param contentType FileParam.TIER or FileParam.CSV_TIER
	 * @return the path or null if canceled
	 */
	private String promptForTierFile(String title, int contentType) {
		String[] extensions = null;
		if (contentType == FileParam.CSV_TIER) {
			extensions = FileExtension.CSV_EXT;
		} else {
			extensions = FileExtension.XML_EXT;
		}
		FileChooser chooser = new FileChooser(this);		
		
		chooser.createAndShowFileDialog(title, FileChooser.SAVE_DIALOG,  extensions, "Recognizer.Path");
		
		File f = chooser.getSelectedFile();
		if (f != null) {			
			return f.getAbsolutePath();
		} else {
			return null;
		}		
	}
	
	/**
	 * Creates a filename based on the first media file and the parameter identifier.
	 * Instead of prompting the user to specify a path. 
	 * The file is also created if it does not exist.
	 * <p>
	 * In fact the media files are not required here;
	 * a random file name can be chosen instead,
	 * placed in a directory for temporary files.
	 * In fact there is no guarantee the user can write in the directory
	 * of the first media file.
	 * <p>
	 * However, when generating an output file it is nice if the user can guess
	 * where it is created and approximately what it is called.
	 * 
	 * @param mediaFiles the selected media files
	 * @param paramName the name/identifier of a parameter
	 * @param contentType csv or xml or other
	 * 
	 * @return the path to a file
	 */
	private String autoCreateOutputFile(List<String> mediaFiles, String paramName, int contentType) {
		File directory = null;
		String prefix = paramName;
		
		if (mediaFiles != null && !mediaFiles.isEmpty()) {
			String firstMed = mediaFiles.get(0);
			File f = new File(firstMed);
			directory = f.getParentFile();
			prefix = FileUtility.dropExtension(f.getName()) + '_' + prefix;
		}
		
		String suffix = null;
		if (contentType == FileParam.CSV_TIER || contentType == FileParam.CSV_TS) {
			suffix = ".csv";
		} else if (contentType == FileParam.TIER || contentType == FileParam.MULTITIER || contentType == FileParam.TIMESERIES) {
			suffix = ".xml";
		}

		try {
			File tempFile = File.createTempFile(prefix, suffix, directory);
			if (currentRecognizer instanceof SharedRecognizer) {
				try {
					changeFileAccess(tempFile);
				} catch (Exception ex) {// just catch any exception that can occur
					ClientLogger.LOG.warning("Cannot change the file permissions: " + ex.getMessage());
				}
			}

			return tempFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create an output file for a recognizer based on a user specified path 
	 * and set the access rights.
	 * 
	 * @param path the path to the file
	 */
	private void createOutputFile(String path) {
		if (path == null) {
			return;
		}
		try {
			File out = new File(path);
			if (!out.exists()) {
				out.createNewFile();
				if (currentRecognizer instanceof SharedRecognizer) {
					try {
						changeFileAccess(out);
					} catch (Exception ex) {// just catch any exception that can occur
						ClientLogger.LOG.warning("Cannot change the file permissions: " + ex.getMessage());
					}
				}
			}
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Cannot create the file: " + ioe.getMessage());
		} catch (SecurityException se) {
			ClientLogger.LOG.warning("Cannot create the file: " + se.getMessage());
		}
	}
	
	/**
	 * Tries to make the output file readable, writable and executable for all users.
	 * 
	 * @param f the file
	 */
	private void changeFileAccess(File f) {
		if (f == null) {
			return;
		}
		// java 1.6 method via reflection, on Windows Vista this doesn't change anything even if true is returned
		/*
		Class<?>[] params = new Class[2];
		params[0] = boolean.class;
		params[1] = boolean.class;
		Object[] values = new Object[2];
		values[0] = Boolean.TRUE;
		values[1] = Boolean.FALSE;
		try {
			Method m = f.getClass().getMethod("setExecutable", params);
			Object result = m.invoke(f, values);
			if (result instanceof Boolean) {
				ClientLogger.LOG.info("Set executable: " + result);
			}
			m = f.getClass().getMethod("setWritable", params);
			result = m.invoke(f, values);
			if (result instanceof Boolean) {
				ClientLogger.LOG.info("Set writable: " + result);
			}
		} catch (NoSuchMethodException nsme) {
		*/
			// no java 1.6, try system tools
			ArrayList<String> coms = new ArrayList<String>(5);
			
			if (SystemReporting.isWindows()) {				
				coms.add("CACLS");
				coms.add("\"" + f.getAbsolutePath() + "\"");
				coms.add("/E");
				coms.add("/G");
				coms.add("Everyone:f"); //avatech fails "No mapping between account names and security IDs was done."			
			} else {// MacOS, Linux
				coms.add("chmod");
				coms.add("a+rwx");
				coms.add("\"" + f.getAbsolutePath() + "\"");
			}
			
			ProcessBuilder pb = new ProcessBuilder(coms);
			pb.redirectErrorStream(true);
			try {
				Process proc = pb.start();
				int exit = proc.exitValue();
				if (exit != 0) {
					ClientLogger.LOG.warning("Could not set the file access attributes via using native tool, error: " + exit);
				}
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Cannot set the file access attributes via native tool");
			}
		/*	
		} catch (Exception ex) {
			// any other exception
			ClientLogger.LOG.warning("Cannot set the file access attributes");
		}
		*/
	}
	
	/**
	 * When the recognizer has finished, check if there is output like 
	 * tier or timeseries files and ask if the segments and 
	 * the tracks should be loaded.
	 * <p>
	 * Must be called on the Event Dispatching Thread
	 * (because it can display several dialogs, and for setSegmentation()).
	 */
	private void checkOutput() {
		if (isRunning) {
			return;
		}
		boolean loadTS = false;
		boolean loadTiers = false;
		boolean tsAvailable = false;
		boolean tierFileAvailable = false;

		if (controlPanel instanceof ParamPanelContainer) {			
			ParamPanelContainer ppc = (ParamPanelContainer) controlPanel;
			int numPanels = ppc.getNumPanels();
			AbstractParamPanel app;
			FileParamPanel fpp;
			// first loop for prompt
			for (int i = 0; i < numPanels; i++) {
				app = ppc.getParamPanel(i);
				if (app instanceof FileParamPanel) {
					fpp = (FileParamPanel) app;
					if (!fpp.isInputType() && 
							(fpp.getContentType() == FileParam.CSV_TS || fpp.getContentType() == FileParam.TIMESERIES)) {
						Object file = fpp.getParamValue();
						if (file instanceof String) {
							File f = new File((String) file);

							if (f.exists() && f.canRead() && f.lastModified() >= lastStartTime) {
								tsAvailable = true;					
							}
						}	
					} else if (!fpp.isInputType() && 
							(fpp.getContentType() == FileParam.TIER || fpp.getContentType() == FileParam.MULTITIER || fpp.getContentType() == FileParam.CSV_TIER)) {
						Object file = fpp.getParamValue();
						if (file instanceof String) {
							File f = new File((String) file);
							//System.out.println("Last mod: " + f.lastModified() + " Start: " + lastStartTime);
							if (f.exists() && f.canRead() && f.lastModified() >= lastStartTime) {
								tierFileAvailable = true;
								//break;						
							}
						}	
					}
				}
			}
			
			boolean tiersAvailable = segmentations.size() > 0;
			if (!tiersAvailable && !tsAvailable && !tierFileAvailable) {
				JOptionPane.showMessageDialog(this, 
						ElanLocale.getString("Recognizer.RecognizerPanel.Warning.NoOutput"), 
						ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			// ask the user whether tracks and tiers should be loaded
			List<SelectableObject<String>> resources = new ArrayList<SelectableObject<String>>(4);

			if (tiersAvailable || tierFileAvailable) {
				boolean sel = true;
				Boolean pref = Preferences.getBool("Recognizer.RecognizerPanel.Tiers", null);

				if (pref != null) {
					sel = pref.booleanValue();
				}
				resources.add(new SelectableObject<String>(
						ElanLocale.getString("Recognizer.RecognizerPanel.Tiers"), sel));

			}
			if (tsAvailable) {
				boolean sel = true;
				Boolean pref = Preferences.getBool("Recognizer.RecognizerPanel.TimeSeries", null);

				if (pref != null) {
					sel = pref.booleanValue();
				}
				resources.add(new SelectableObject<String>(
						ElanLocale.getString("Recognizer.RecognizerPanel.TimeSeries"), sel));
			}
			
			LoadOutputPane lop = new LoadOutputPane(resources);
			int option = JOptionPane.showConfirmDialog(this, 
					lop,
					"", 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option != JOptionPane.YES_OPTION) {
				return;
			} else {

				SelectableObject<String> selObj;
				for (int i = 0; i < resources.size(); i++) {
					selObj = resources.get(i);
					if (selObj.getValue().equals(ElanLocale.getString("Recognizer.RecognizerPanel.Tiers"))) {
						loadTiers = selObj.isSelected();
						Preferences.set("Recognizer.RecognizerPanel.Tiers", Boolean.valueOf(loadTiers), null, false, false);
					}
					else if (selObj.getValue().equals(ElanLocale.getString("Recognizer.RecognizerPanel.TimeSeries"))) {
						loadTS = selObj.isSelected();
						Preferences.set("Recognizer.RecognizerPanel.TimeSeries", Boolean.valueOf(loadTS), null, false, false);
					}
				}
			}
			
			if (loadTiers) {
				if (tierFileAvailable && !tiersAvailable) {
					// load them from file if possible
					List<File> csvFiles = new ArrayList<File>(4);
					List<File> xmlFiles = new ArrayList<File>(4);
					for (int i = 0; i < numPanels; i++) {
						app = ppc.getParamPanel(i);
						if (app instanceof FileParamPanel) {
							fpp = (FileParamPanel) app;
							if (!fpp.isInputType() && 
									(fpp.getContentType() == FileParam.CSV_TIER || fpp.getContentType() == FileParam.TIER)) {
								Object file = fpp.getParamValue();
								if (file instanceof String) {
									File f = new File((String) file);
									if (f.exists() && f.canRead() && f.lastModified() >= lastStartTime) {
										if (fpp.getContentType() == FileParam.CSV_TIER) {
											csvFiles.add(f);
										} else {
											xmlFiles.add(f);
										}
									}
								}
							}
						}
					}
					
					for (File csvFile : csvFiles) {
						if (csvFile.exists() && csvFile.canRead() && csvFile.lastModified() >= lastStartTime) {
							CsvTierIO cio = new CsvTierIO();
							// NOTE: CsvTierIO has no MediaDescriptors with its Segmentation.
							List<Segmentation> segm = cio.read(csvFile);
							if (segm != null && segm.size() > 0) {
								for (Segmentation s : segm) {
									addSegmentation(s);
								}
							}
						}
					}
					
		        	StringBuilder mesBuf = new StringBuilder();  					
					for (File xmlFile : xmlFiles) {
						if (xmlFile.exists() && xmlFile.canRead() && xmlFile.lastModified() >= lastStartTime) {
							XmlTierIO xio = new XmlTierIO(xmlFile);
							// NOTE: XmlTierIO has no MediaDescriptors with its Segmentation.
							List<Segmentation> segm = null;
							try{
								segm = xio.parse();
							} catch (Exception e){
								mesBuf.append(xmlFile.getAbsolutePath() + " : " + e.getMessage()  + "\n");
							}
							 
							if (segm != null && segm.size() > 0) {
								for (Segmentation s : segm) {
									addSegmentation(s);
								}
							}
						}
					}
					
					if(mesBuf.length() > 0) {
						JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), 
								mesBuf, ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
						
						if(getSegmentations().size() == 0){
							return;
						}
	                }
					
				}
	            // needs the transcription
				Map<String, List<AnnotationDataRecord>> segmentationMap = 
					new HashMap<String, List<AnnotationDataRecord>>();
		        List<Segmentation> segs = getSegmentations();
		        for (Segmentation seg : segs) {
		            if (seg == null) {
		                continue;
		            }

		            List<RSelection> segments = seg.getSegments();
		            List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>();

		            for (int j = 0; j < segments.size(); j++) {
		                Segment segment = (Segment) segments.get(j);
		                records.add(new AnnotationDataRecord("", segment.label,
		                        segment.beginTime, segment.endTime));
		            }

		            segmentationMap.put(seg.getName(), records);
		        }

		        // create command
		        SegmentsToTiersCommand com = (SegmentsToTiersCommand) ELANCommandFactory.createCommand(
		        		viewerManager.getTranscription(),
		                ELANCommandFactory.SEGMENTS_2_TIER);
		        //com.addProgressListener(this);
		        //progressBar.setIndeterminate(false);
		        //progressBar.setValue(0);
		        com.execute(viewerManager.getTranscription(), new Object[] { segmentationMap });

			}
			
			if (loadTS) {
				List<File> csvFiles = new ArrayList<File>(4);
				List<File> xmlFiles = new ArrayList<File>(4);
				for (int i = 0; i < numPanels; i++) {
					app = ppc.getParamPanel(i);
					if (app instanceof FileParamPanel) {
						fpp = (FileParamPanel) app;
						if (!fpp.isInputType() && 
								(fpp.getContentType() == FileParam.CSV_TS || fpp.getContentType() == FileParam.TIMESERIES)) {
							Object file = fpp.getParamValue();
							if (file instanceof String) {
								File f = new File((String) file);
								if (f.exists() && f.canRead() && f.lastModified() >= lastStartTime) {
									if (fpp.getContentType() == FileParam.CSV_TS) {
										csvFiles.add(f);
									} else {
										xmlFiles.add(f);
									}
								}
							}
						}
					}
				}
				
				List<Object> tracks = new ArrayList<Object>(10);
				for (File f : csvFiles) {
					CsvTimeSeriesIO csvIO = new CsvTimeSeriesIO(f);
					List<Object> result = csvIO.getAllTracks();
					if (result != null) {
						tracks.addAll(result);
					}
				}
				for (File f : xmlFiles) {
					XmlTimeSeriesReader xmlIO = new XmlTimeSeriesReader(f);
					try {
						List<Object> result = xmlIO.parse();
						if (result != null) {
							tracks.addAll(result);
						}
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(this, 
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed") + "\n" + 
								ioe.getMessage(), 
								ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
					} catch (SAXException sax) {
						JOptionPane.showMessageDialog(this, 
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed") + "\n" + 
								sax.getMessage(), 
								ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
					}
				}
				
				Command com = ELANCommandFactory.createCommand(viewerManager.getTranscription(), 
						ELANCommandFactory.ADD_TRACK_AND_PANEL);
				com.execute(viewerManager, new Object[]{tracks});
			}
		}
	}
	
	/**
	 * This method will run in a separate Thread to decouple the recognizer from ELAN
	 */
	@Override
	public void run() {
		if (currentRecognizer != null) {
			currentRecognizer.start();
			// use a time value rounded to seconds, some OS's do that with the last modified flag
			lastStartTime = (System.currentTimeMillis() / 1000) * 1000;
		}
	}

	/**
	 * Takes care of giving running recognizers a chance to stop gracefully
	 * ELAN should call this method before it quits
	 *
	 */
	public void stopRecognizers() {
		if (currentRecognizer != null) {
			elapseTimer.stop();
			currentRecognizer.stop();			
			isRunning = false;
		}
	}

	/**
	 * Tells if there is one or more recognizer busy
	 * 
	 * @return true if there is some recognizing going on
	 */
	@Override
	public boolean isBusy() {
		return isRunning;
	}
	
	/**
	 * Tries to pass the current locale's Resource Bundle to the current recognizer.
	 */
	private void setRecognizerResourceBundle() {
		if (currentRecognizer != null) {
			try {
				currentRecognizer.getClass().getMethod("updateLocaleBundle", ResourceBundle.class);
				currentRecognizer.updateLocaleBundle(ElanLocale.getResourceBundle());
			} catch (NoSuchMethodException nme){
				currentRecognizer.updateLocale(ElanLocale.getLocale());				
			} catch (Throwable t) {
				currentRecognizer.updateLocale(ElanLocale.getLocale());
			}
		}		
	}

	/**
	 * Sets the localized labels for ui elements.
	 */
	@Override
	public void updateLocale() {		
		createSegButton.setText(ElanLocale.getString(
                "Recognizer.SegmentationsPanel.Make.Tier"));

		setRecognizerResourceBundle();
		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateLocale();
		}
		helpButton.setToolTipText(ElanLocale.getString("Button.Help.ToolTip"));
		startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
		reportButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Report"));
		recognizerLabel.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Recognizer"));			
		progressPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.RecognizerPanel.Progress")));
		paramPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.RecognizerPanel.Parameters")));
		detachButton.setToolTipText(ElanLocale.getString("Detachable.detach"));
		configureButton.setToolTipText(ElanLocale.getString("MetadataViewer.Configure"));		
		saveParamsButton.setToolTipText(ElanLocale.getString("Recognizer.RecognizerPanel.SaveParameters"));
		loadParamsButton.setToolTipText(ElanLocale.getString("Recognizer.RecognizerPanel.LoadParameters"));
		
	}
	
	//
	// RecognizerHost interface implementation
	//

	/**
	 * Add a segmentation to our storage, and to the signal viewer (if it is an audio recognizer).
	 * Uses the MediaDescriptor(s) that is attached to the segmentation.
	 * Except that there may be none (when called from checkOutput() for instance!)...
	 * Therefore assume channel 1 of the currently visible media. 
	 * <p>
	 * NOTE: this must be called on the Event Dispatching Thread.
	 */
	@Override
	public void addSegmentation(Segmentation segmentation) {
		segmentations.put(segmentation.getName(), segmentation);
		createSegButton.setEnabled(true);
		
		// The rest of the work needs only be done by audio recognizers.
		if (currentRecognizer.getRecognizerType() != Recognizer.AUDIO_TYPE) {
			return;
		}
		
		// make sure the SignalViewer knows about the new segmentation
		final SignalViewer signalViewer = viewerManager.getSignalViewer();
		if (signalViewer != null) {
			int channel = 0;
			List<MediaDescriptor> mediaDescriptors = segmentation.getMediaDescriptors();
			final String signalViewerMediaPath = signalViewer.getMediaPath();
			
			if (mediaDescriptors.isEmpty()) {
				channel += 1;	// assume channel 1
			} else {
				for (MediaDescriptor descriptor : mediaDescriptors) {
					// CHECK IF THE MEDIA FILE IS THE VISIBLE ONE!!! ???	
					if(descriptor.mediaFilePath.equals(signalViewerMediaPath)){
						channel += descriptor.channel;
					}
				}
			}
			
			if (channel == 0 && mediaDescriptors.size() >= 1) {
				if(mediaDescriptors.get(0).mediaFilePath.equals(signalViewerMediaPath)){
					signalViewer.setSegmentation(new BoundarySegmentation(segmentation));
				}				
			} else	if (channel == 1) {
				if (notMono) {
					signalViewer.setSegmentationChannel1(new BoundarySegmentation(segmentation));
				} else {
					signalViewer.setSegmentation(new BoundarySegmentation(segmentation));
				}
			} else if (channel == 2) {
				signalViewer.setSegmentationChannel2(new BoundarySegmentation(segmentation));
			} else if (channel == 3) { // something for combined channel result?
				
			}
		}
	}
	
	/**
	 * 
	 */
	@Override
	public List<Segmentation> getSegmentations() {
		return new ArrayList<Segmentation>(segmentations.values());
	}
	
	/**
	 * By calling this method a recognizer gives information about the progress of its recognition task
	 * 
	 * @param progress  a float between 0 and 1 with 1 meaning that the task is completed
	 */
	@Override
	public void setProgress(float progress) {	
		int progPercent = (int) (100 * progress);
		setProgress(progPercent, null);
	}

	/**
	 * By calling this method a recognizer gives information about the progress of its recognition task
	 * 
	 * @param progress  a float between 0 and 1 with 1 meaning that the task is completed
	 * @param message a progress message
	 */
	@Override
	public void setProgress(float progress, String message) {
		int progPercent = (int) (100 * progress);
		setProgress(progPercent, message);
	}	
	
	/**
	 * For internal use only, called from {@link #setProgress(float)} or {@link #setProgress(float, String)}.
	 * 
	 * @param percentage an integer value between 1 and 100, inclusive. 100 means completed
	 * @param message a message string or null
	 */
	private void setProgress(int percentage, String message) {
		if (needToPollProgressReport) {
			pollProgressReport();
		}
		if (percentage < 0) {
			if (!progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(true);
			}
			if (message != null) {
				progressBar.setString(message);
			} else {
				progressBar.setString("");
			}
			elapseTimer.recognizerUpdate();//??
		} else {
			if (progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(false);
			}
			
			progressBar.setValue(percentage);
			
			if (percentage >= 100) {
				progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Ready"));
				startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
				isRunning = false;
				elapseTimer.stop();
				// get report??
				// check for any output; since this probably runs on a separate thread
				// use invokeLater.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						checkOutput();
					}
				});
			} else {
				if (isRunning) {
					if (message != null) {
						progressBar.setString(String.valueOf(percentage) + "% " + message);
					} else {
						progressBar.setString(String.valueOf(percentage) + "%");
					}
				}
				elapseTimer.recognizerUpdate();
			}
		}
		
	}

	/**
	 * Called by the recognizer to signal that a fatal error occurred.
	 * 
	 * @param message a description of the error
	 */
	@Override
	public void errorOccurred(String message) {
		elapseTimer.stop();
		JOptionPane.showMessageDialog(this, ElanLocale.getString("Recognizer.RecognizerPanel.Error") + "\n" + message, 
				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
		// just to be sure
		if (isRunning) {			
			currentRecognizer.stop();
		}
		setProgress(1f);
	}
	
	/**
	 * Called by the recognizer to show some informational message to
	 * the user.
	 * This may happen from a separate thread.
	 */
	@Override
	public void appendToReport(String message) {
		// If a Recognizer calls this, it can't be the old type that needs polling.
		needToPollProgressReport = false;
		elapseTimer.recognizerUpdate();
//		System.err.printf("message: '%s'\n", message);
		updateProgressReport(message);
	}
	
	/**
	 * Returns a tier selection panel for the given parameter
	 * 
	 * @param paramName - name of the parameter
	 */
	@Override
	public TierSelectionPanel getSelectionPanel(String paramName) {
		if(paramName == null || paramName.trim().length()==0){
			paramName = "DEFAULT";
		}
		TierSelectionPanel panel = selPanelMap.get(paramName);		
		if(panel == null){
			int mode = currentRecognizer.getRecognizerType();
			panel = new TierSelectionPanel(mode, supportedMediaFiles.get(mode), viewerManager);
			selPanelMap.put(paramName, panel);			
		}
		return panel;
	}
	
	/**
	 * Preliminary!
	 * Attaches the parameter panel to the main panel again.
	 */
	public void attachParamPanel(JComponent paramComp) {
		// is the param scrollpane
		if (paramComp == controlPanel || paramComp == jsp) {
			if(paramComp == controlPanel && controlPanel instanceof ParamPanelContainer){
				((ParamPanelContainer)controlPanel).doLayout(false);
				jsp.setViewportView(controlPanel);
			}
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
			paramButtonPanel.setVisible(true);
			
			detached = false;
			detachedDialog = null;
			
//			if(controlPanel){
//			//	startStopButton.setEnabled(true);
//			}
		}
	}
	
	private void showConfigureDialog(){		
		ConfigWindow cw = new ConfigWindow(ELANCommandFactory.getRootFrame(
				viewerManager.getTranscription()));
		cw.pack();
		Dimension dim = cw.getPreferredSize();
		Point p = configureButton.getLocationOnScreen();
		cw.setBounds(p.x - dim.width, p.y, dim.width, dim.height);
		cw.setVisible(true);
	}
	
	/**
	 * Preliminary!
	 * Detaches the parameter panel from the main panel.
	 */
	public void detachParamPanel() {
		paramPanel.remove(jsp);
		paramPanel.repaint();
		paramButtonPanel.setVisible(false);		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).doLayout(true);
			detachedDialog = new ParamDialog(ELANCommandFactory.getRootFrame(
					viewerManager.getTranscription()), this, controlPanel);
		} else {
			detachedDialog = new ParamDialog(ELANCommandFactory.getRootFrame(
					viewerManager.getTranscription()), this, jsp);
		}
		
		detachedDialog.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle curRect = detachedDialog.getBounds();
		// add some extra width for a possible scrollbar in the scrollpane
		detachedDialog.setBounds(screen.width / 2, 10, 
				Math.min(screen.width / 2 - 20, curRect.width), Math.min(screen.height - 30, curRect.height));
		detachedDialog.setVisible(true);
		detached = true;
		//startStopButton.setEnabled(false);
	}
	
	/**
	 * Checks if help is available for the current 
	 * recognizer
	 * 
	 * @return
	 */
	public boolean isHelpAvailable(){
		return helpButton.isEnabled();
	}
	
	/**
	 * Show a new Help dialog
	 * 
	 */
	public void showHelpDialog(){
		try {
			String fileName = RecogAvailabilityDetector.getHelpFile(currentRecognizer.getName());
			HTMLViewer helpViewer = new HTMLViewer(fileName, false, ElanLocale.getString("Recognizer.RecognizerPanel.Help"));
    		JDialog dialog = helpViewer.createHTMLDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()));
    		dialog.pack();
    		dialog.setSize(500, 600);
    		dialog.setVisible(true);
    	} catch (IOException ioe) {
    		// message box
    		JOptionPane.showMessageDialog(this, (ElanLocale.getString("Message.LoadHelpFile")+ " "   + ioe.getMessage()), 
    				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE, null);
    	}
	}
	
	/**
	 * Loads the parameter settings from a PARAM XML file and applies them to the current recognizer. 
	 */
	protected void loadParameterFile() {
		if (currentRecognizer == null || controlPanel == null || !(controlPanel instanceof ParamPreferences)) {
			return;
		}
		
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(null, FileChooser.OPEN_DIALOG, FileExtension.XML_EXT, "Recognizer.Path");
		File selFile = chooser.getSelectedFile();
		if (selFile != null && selFile.canRead()) {
			try {
				ParamIO pio = new ParamIO();
				Map<String, Object> parMap = pio.read(selFile);
				((ParamPreferences) controlPanel).setParamPreferences(parMap);
			} catch (IOException ioe) {
				// 
				JOptionPane.showMessageDialog(this, ElanLocale.getString(
					"Recognizer.RecognizerPanel.Warning.LoadFailed")  + ioe.getMessage(), 
					ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Writes the current parameter settings to a PARAM XML file. 
	 */
	protected void saveParameterFile() {
		if (currentRecognizer == null || controlPanel == null || !(controlPanel instanceof ParamPreferences)) {
			return;
		}
		
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.XML_EXT, "Recognizer.Path");	
		File f = chooser.getSelectedFile();
		if(f != null){	
			ParamIO pio = new ParamIO();
			// hier... or get parameters from the recognizer?
			Map<String, Object> paramMap = ((ParamPreferences) controlPanel).getParamPreferences();
			String recogId = null;
			if (currentRecognizer instanceof LocalRecognizer) {
				recogId = ((LocalRecognizer) currentRecognizer).getId();
			} else {
				recogId = currentRecognizer.getName();
			}
			
			try {
				pio.writeParamFile(recogId, paramMap, f);
			} catch (IOException ioe) {
				// message
				JOptionPane.showMessageDialog(this, ElanLocale.getString(
						"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			}			
		}
	}
	
	private String writeAndGetTierOrSelectionFile(FileParamPanel panel, List<RSelection> selList){
		String filePath = null;
		// ask for file name					
		if (reduceFilePrompt) {
			// Use an arbitrary file name in a temporary directory
			filePath = autoCreateOutputFile(null, panel.getParamName(), panel.getContentType());
		} else {						
			filePath = promptForTierFile(panel.description, panel.getContentType());
		}
		
		if(filePath == null){
			return null;	
		}		
	 		
		File tf = new File(filePath);
		try { 			
			if(tf.exists()){
				writeFile(filePath, selList, panel.getContentType());
			} else {
				JOptionPane.showMessageDialog(this, "File not exists.", 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
				return null;
			}
		} catch (IOException ioe) {
				// show message
			JOptionPane.showMessageDialog(this, ElanLocale.getString(
					"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
					ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			return null ;
		}	catch (Exception ex) {// any exception
			return null;
		}					
		 
		return filePath;
	}
	
	/**
	 * Writes the selections from the selection panel to a csv or xml file.
	 * It is assumed that a check has been performed on the list of selections and that
	 * the file is safe to save to.
	 * 
	 * @param filepath the path to the file
	 * @param contentType CSV_TIER or TIER (xml)
	 * @throws IOException any io error that can occur during writing
	 */
	protected void writeFile(String filepath, Object value, int contentType) throws IOException {		
		
		
		RecTierWriter xTierWriter = new RecTierWriter();
		File f = new File(filepath);
		
		if(value instanceof List){			
			xTierWriter.write(f, (List<RSelection>)value);			
		} else if(value instanceof String){
			// create segments			
			TierImpl tier = ((TranscriptionImpl)viewerManager.getTranscription()).getTierWithId(value.toString());	
			ArrayList<RSelection> segments = null;
			if (tier != null) {					
				List<AbstractAnnotation> anns = tier.getAnnotations();
				segments = new ArrayList<RSelection>(anns.size());
				for (int j = 0; j < anns.size(); j++) {
					AbstractAnnotation aa = anns.get(j);
					segments.add(new AudioSegment(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary(), 
							aa.getValue()));
				}				
			}
			
			List<Segmentation> segmentations = new ArrayList<Segmentation>();
			segmentations.add(new Segmentation(tier.getName(), segments, ""));// pass the master media path?
			
			xTierWriter.write(f, segmentations, viewerManager.getTranscription());		
		}
	}
	
	/**
	 * Creates a dialog with a text area containing the report.
	 * 
	 * @param report the report
	 */
	protected void showReport() {
		String report = progressReport.toString();
		if (report.isEmpty() && needToPollProgressReport) {
			report = getPolledProgressReport();
		}
		if (report != null) {
			JDialog rD = new ClosableDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), true);
			progressReportTextArea = new JTextArea(report);
			progressReportTextArea.setLineWrap(true);
			progressReportTextArea.setWrapStyleWord(true);
			progressReportTextArea.setEditable(false);
			progressReportTextArea.setCaretPosition(report.length());
			progressReportTextArea.getCaret().setVisible(true);
			
			rD.getContentPane().setLayout(new BorderLayout());
			JScrollPane scroll = new JScrollPane(progressReportTextArea);
			rD.getContentPane().add(scroll, BorderLayout.CENTER);
			rD.pack();
			rD.setSize(800, 400);
			rD.setLocationRelativeTo(this);
			
			rD.setVisible(true); // modal, so we won't return here until closed.
			
			progressReportTextArea = null;
		} else {
			JOptionPane.showMessageDialog(this, ElanLocale.getString("Recognizer.RecognizerPanel.No.Report"), 
				ElanLocale.getString("Message.Warning"), JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * If the relevant JTextArea is visible, update the collected progress report
	 * into it.
	 * This method may be called from other threads.
	 */
	protected void updateProgressReport(String toAppend) {
		progressReport.append(toAppend);

		if (progressReportTextArea != null) {
			Document doc = progressReportTextArea.getDocument();
			// It is hoped that an insert operation is more efficient than simply
			// replacing all text, due to not re-rendering the same text over and over
			// after every update.
			int oldLen = doc.getLength();
			boolean wasAtEnd = oldLen == progressReportTextArea.getCaretPosition();
			try {
				// By way of exception in Swing, PlainDocument.insertString() is documented to be thread-safe.
				doc.insertString(oldLen, toAppend, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			// By way of exception in Swing, setText() is documented to be thread-safe.
			//progressReportTextArea.setText(progressReport.toString());
			
			// Scroll the end of the text into view, if it was already.
			// That way there is auto-scroll unless canceled by the user.
			// XXX thread safety
			if (wasAtEnd) {
				progressReportTextArea.setCaretPosition(progressReport.length());
			}
		}
	}

	/**
	 * <p>Call the "getReport()" method on the current Recognizer, using reflection.
	 * Should only be called if needToPollProgressReport is true.
	 * <p>This is for old-style Recognizers that need to have their report polled,
	 * and don't update their report by calling RecognizerHost.appendToReport().
	 * <p>Since the method is no longer part of the interface, the only way to call
	 * it is via reflection.
	 * @return
	 */
	protected String getPolledProgressReport() {
		try {
			Object res = getReport.invoke(currentRecognizer);
			if (res instanceof String) {
				return (String)res;
			} else if (res == null) {
				return null;
			}
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		// If any error occurred, don't try this again.
		needToPollProgressReport = false;
		return null;
	}

	/**
	 * Poll for the progress report and display it,
	 * but only if the relevant JTextArea exists.
	 * Should only be called if needToPollProgressReport is true.
	 */
	protected void pollProgressReport() {
		if (progressReportTextArea != null) {
			String report = getPolledProgressReport();
			if (report != null) {
				progressReportTextArea.setText(report);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (currentRecognizer != null) {
			if (isRunning) {
				currentRecognizer.stop();
				elapseTimer.stop();
				currentRecognizer.dispose();
			}
			currentRecognizer = null;
		}
		super.finalize();
	}
	
	@Override
	public void ACMEdited(ACMEditEvent e){
		switch (e.getOperation()){			
			case ACMEditEvent.ADD_TIER:	
			case ACMEditEvent.REMOVE_TIER:	
			case ACMEditEvent.CHANGE_TIER:		
				updateTiers(e.getOperation());
			break;
		}
	}
	
	@Override
	public List<String> getMediaFiles(int mode) {
		//return mediaFilePaths.get(mode); // the unfiltered list
		return supportedMediaFiles.get(mode);
	}

	/**
	 * Common code for setting audio file paths, video file paths, and
	 * other file paths.
	 * The input file paths may be file: urls. This will be stripped.
	 * 
	 * @param destPaths	add to this list
	 * @param avFilePaths from this list
	 */
	protected void setAudioVideoFilePaths(List<String> destPaths, List<String> avFilePaths) {				
		destPaths.clear();
		for (String path : avFilePaths) {
			path = FileUtility.urlToAbsPath(path);
			if (!destPaths.contains(path)) {
				destPaths.add(path);
			}
		}		
		segmentations = new HashMap<String, Segmentation>();
		
		// update the files list
		updateMediaFiles();
	}

	/**
	 * Sets the new audio file paths
	 * 
	 * @param audioFilePath the audioFilePath to set
	 */
	public void setAudioFilePaths(List<String> audioFilePaths) {				
		List<String> audioMediaPaths = mediaFilePaths.get(Recognizer.AUDIO_TYPE);
		setAudioVideoFilePaths(audioMediaPaths, audioFilePaths);
	}

	/**
	 * Sets the new video file paths
	 * 
	 * @param videoFilePath the videoFilePath to set
	 */
	public void setVideoFilePaths(List<String> videoFilePaths) {
		List<String> videoMediaPaths = mediaFilePaths.get(Recognizer.VIDEO_TYPE); 
		setAudioVideoFilePaths(videoMediaPaths, videoFilePaths);
	}

	/**
	 * Sets the new other file paths
	 * 
	 * @param videoFilePath the otherFilePath to set
	 */
	public void setOtherFilePaths(List<String> otherFilePaths) {
		List<String> otherMediaPaths = mediaFilePaths.get(Recognizer.OTHER_TYPE); 
		setAudioVideoFilePaths(otherMediaPaths, otherFilePaths);
	}

	/**
	 * Class to update the elapsed time and time since
	 * the last update from the recognizer
	 *  
	 * @author aarsom
	 *
	 */
	private class Timer{		
		private Thread internalThread;
		private boolean stopUpdate;
		
		private JPanel timerPanel;
		private JLabel elapseTimeLabel;
		private JLabel lastUpdateLabel;
		
		private long lastUpdateTime;		
		
		SimpleDateFormat df = new SimpleDateFormat("mm:ss");
		
		public Timer() {
		}
		
		public JPanel getTimerPanel(){
			if(timerPanel == null){
				elapseTimeLabel = new JLabel("00:00");
				lastUpdateLabel = new JLabel("00:00");
				
				JLabel elapseLabel = new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.Timer.ElapseTime")+ " ");
				JLabel updateLabel = new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.Timer.UpdateTime")+ " ");
				
				Font font = new Font(elapseTimeLabel.getFont().getFontName(), Font.PLAIN, 10);				
				elapseTimeLabel.setFont(font);
				lastUpdateLabel.setFont(font);
				elapseLabel.setFont(font);
				updateLabel.setFont(font);
				
				timerPanel = new JPanel();
				timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(elapseLabel);
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(elapseTimeLabel);				
				timerPanel.add(Box.createHorizontalStrut(15));
				timerPanel.add(updateLabel);				
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(lastUpdateLabel);
			}
			
			return timerPanel;
		}
		
		public void resetTimer(){
			elapseTimeLabel.setText("00:00");
			lastUpdateLabel.setText("00:00");
			stopUpdate = false;
			repaint();
		}
		
		public void recognizerUpdate(){
			lastUpdateTime = System.currentTimeMillis();
		}
		
		public void start(){
			resetTimer();
			
		    internalThread = new Thread("ElapseTimer"){
		    	@Override
				public void run() {
					try {						
		    			long startTime = System.currentTimeMillis();
		    			lastUpdateTime = System.currentTimeMillis();
		    	
		    	        while ( !stopUpdate) {
		    	        	Thread.sleep(1000);
		    	        	long currTime = System.currentTimeMillis();		    	       
			    	        	
			    	        elapseTimeLabel.setText(df.format(new Date(currTime - startTime)));
			    	        lastUpdateLabel.setText(df.format(new Date(currTime - lastUpdateTime)));
			    	        if (needToPollProgressReport) {
			    	        	SwingUtilities.invokeLater(new Runnable(){
									@Override
									public void run() {
					    	        	pollProgressReport();
									}
			    	        	});
			    	        }
		    	        }
					} catch ( InterruptedException x ) {
			            //x.printStackTrace();
		             }
				}
		    };
			internalThread.start();
		}			
	
	    public void stop(){
	    	stopUpdate = true;
	    	if(internalThread != null && internalThread.isAlive()){
	    		internalThread.interrupt();
	    	}
	    }
	}
	
	/**
	 * Popup window for configuring properties 
	 * for the recognizer 
	 * 
	 * @author Aarthy Somasundaram
	 */
	private class ConfigWindow extends JWindow implements ActionListener{
		private JPanel compPanel;
		private JButton closeButton;
		private JCheckBox reduceFilePromptCB;
		
		/**
		 * @param owner
		 */
		public ConfigWindow(Window owner) {
			super(owner);
			initComponents();
		}
		
		private void initComponents() {
			Icon icon = null;
			String text = null;
			try {
				icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Close16.gif"));
			} catch (Exception ex) {// any
				text = "X";
			}
			compPanel = new JPanel(new GridBagLayout());
			compPanel.setBorder(new CompoundBorder(new LineBorder(Constants.SHAREDCOLOR6, 1), 
					new EmptyBorder(2, 4, 2, 2)));

			closeButton = new JButton(text, icon);// load icon...
			closeButton.setToolTipText(ElanLocale.getString("Button.Close"));
			closeButton.setBorderPainted(false);
			closeButton.setPreferredSize(new Dimension(16, 16));
			closeButton.addActionListener(this);
			
			reduceFilePromptCB = new JCheckBox(ElanLocale.getString("Recognizer.RecognizerPanel.ReduceFilePrompt"));
			reduceFilePromptCB.setSelected(reduceFilePrompt);
			reduceFilePromptCB.addActionListener(this);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridwidth = 2;
			compPanel.add(closeButton, gbc);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridwidth = 1;
			gbc.gridy = 1;
			compPanel.add(reduceFilePromptCB, gbc);		
			
			add(compPanel);
		}

		/**
		 * Action events.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == closeButton) {
				close();
			}	else if(e.getSource() == reduceFilePromptCB){
				reduceFilePrompt = reduceFilePromptCB.isSelected();
			}		
		}		
		
		/**
		 * Closes the window.
		 */
		private void close() {			
			setVisible(false);
			dispose();
		}

	}
}
