package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.VideoFrameGrabber;
import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.data.AudioSegment;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.VideoSegment;
import mpi.eudico.client.annotator.svg.Graphics2DEditor;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;

/**
 * A panel which allows you to select a tier or a selection
 * or a file
 * 
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class TierSelectionPanel extends AbstractSelectionPanel implements ActionListener {
	private JRadioButton selectionsRB;
	private JRadioButton tierRB;	
	private JRadioButton fileRB;
	
	private JLabel tierLabel;
	private JComboBox tierComboBox;
	private DefaultComboBoxModel model;
	
	private SelectionPanel selectionPanel;
	private JPanel settingsPanel;	
	
	private JTextField fileField;
	
	private JButton browseButton;	
	
	private String initialPath;
	private int dialogType = -1;
	private List<String[]> fileExtensions = null;
	
	private boolean enableFileSelection = false;
	private ViewerManager2 vm;		
	
	public static final int AUDIO_MODE = 0; 
	public static final int VIDEO_MODE = 1;
	
	public static final String SELECTIONS = "Selections"; 
	public static final String TIER = "Tier";
	public static final String FILE_NAME = "FileName";
	public static final String TIER_NAME = "TierName";
	
	private static final String SELECT_TIER = "<select a tier>";
	
	private int avMode = AUDIO_MODE;
	
	private Insets insets =  new Insets(1, 1, 0, 1);
	private List<String> mediaFiles;
	
	private Map<String, Object> paramValueMap;

	/**
	 * Constructor
	 * 
	 * @param mode - TierSelectionPanel.AUDIO_MODE / TierSelectionPanel.VIDEO_MODE
	 * @param supportedMediaFiles - list of media files supported by this recognizer
	 * @param vm - ViewerManager2
	 * 
	 */
	public TierSelectionPanel(int mode, List<String> supportedMediaFiles, ViewerManager2 vm) {		
		this(mode, supportedMediaFiles,  vm , false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mode - TierSelectionPanel.AUDIO_MODE / TierSelectionPanel.VIDEO_MODE
	 * @param supportedMediaFiles - list of media files supported by this recognizer
	 * @param vm - ViewerManager2
	 * @param enableFileSelection - if true enables the option to select a file, else
	 * 								file selection option is disabled.	
	 */
	public TierSelectionPanel(int mode, List<String> supportedMediaFiles, ViewerManager2 vm, boolean enableFileSelection) {		
		super();
		if (vm != null) {				
			this.vm = vm;
			this.enableFileSelection = enableFileSelection;
			mediaFiles = supportedMediaFiles;
			avMode = mode;			
			paramValueMap = new HashMap<String, Object>();		
			
			initComponents();			
		}
	}	
	
	/**
	 * Changes the default radio button option 
	 * 
	 * By default, tiers radio button is selected
	 * 
	 * @option value fixed value; should be any of one of these
	 *         TierSelectionPanel.SELECTIONS or
	 *         TierSelectionPanel.FILE_NAME	
	 */
	public void setDefaultOption(String option){
		if(option.equals(SELECTIONS)){
			selectionsRB.doClick();
		} else if(option.equals(FILE_NAME) && fileRB.isEnabled()){
			fileRB.doClick();
		}
	}
	/**
	 * Enables or disables the file selection option
	 * 
	 * @param enable
	 */
	@Override
	public void enableFileSelection(boolean enable){
		enableFileSelection = enable;
		fileRB.setEnabled(enableFileSelection);
	}
	
	/**
	 * Updates the background of this panel and
	 * all other components in this panel
	 * 
	 * @param color - background color to be updated
	 */
	public void updateBackgroundColor(Color color){
		settingsPanel.setBackground(color);
		selectionPanel.updateBackgroundColor(color);
		super.setBackground(color);		
	}
	
	/**
	 * Sets the type of file dialog which is created
	 * when the file browse button is clicked
	 * 
	 * @param dialogType - FileChooser.OPEN_DIALOG /
	 *                     FileChooser.SAVE_DIALOG 
	 */
	public void setFileDialogType(int dialogType){
		this.dialogType = dialogType;
	}
	
	/**
	 * Sets the array of extensions which will be
	 * used as file filters for the file chooser
	 * dialog
	 * 
	 * @param fileExtensions
	 */
	public void setFileExtensions(List<String[]> fileExtensions){
		this.fileExtensions = fileExtensions;
	}

	/**
	 * Initialize components
	 */
	protected void initComponents() {				
		setLayout(new GridBagLayout());	
		
		selectionPanel = new SelectionPanel();
		
		selectionsRB = new JRadioButton();
		tierRB = new JRadioButton();
		fileRB = new JRadioButton();		
		
		ButtonGroup group = new ButtonGroup();
		group.add(selectionsRB);
		group.add(tierRB);
		group.add(fileRB);
		
		selectionsRB.addActionListener(this);
		tierRB.addActionListener(this);
		fileRB.addActionListener(this);	
		fileRB.setEnabled(enableFileSelection);		
			
		// tierRB settings panel
		tierLabel = new JLabel();		
		model = new DefaultComboBoxModel();
		tierComboBox = new JComboBox(model);		
		updateTierComboBox();
		
		//fileRB settings panel
		fileField = new JTextField();
		fileField.setEditable(true);
		browseButton = new JButton();
		
		ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
		if (icon != null) {
			browseButton.setIcon(icon);
		} else {
			browseButton.setText("...");
		}
		browseButton.addActionListener(this);
		
		tierRB.setSelected(true);
		
		// settings panel
		settingsPanel = new JPanel(new GridBagLayout());		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;	
		gbc.gridx = 0;
		gbc.insets = new Insets(3, 1, 0, 1);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		settingsPanel.add(tierLabel, gbc);					
		
		gbc.gridx = 1;	
		gbc.weightx = 1.0;
		gbc.insets= new Insets(0, 1, 0, 1);
		settingsPanel.add(tierComboBox, gbc);			
		
		gbc = new GridBagConstraints();		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets = insets;
		add(selectionsRB, gbc);
		
		gbc.gridx = 1;	
		add(tierRB, gbc);
		
		gbc.gridx = 2;		
		add(fileRB, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		add(settingsPanel, gbc);
		// ensure the labels etc. are set
		updateLocale(null);
	}
	
	/**
	 * Updates the tiers in the drop down list 
	 * 
	 */
	private void updateTierComboBox(){	
		if(model != null){
			model.removeAllElements();
			model.addElement(SELECT_TIER);
			List<? extends Tier> tiers = vm.getTranscription().getTiers();
			if(tiers != null){			
				for(Tier t : tiers){
					model.addElement(t.getName());
				}
			}
		}
		
		if(model == null || model.getSize() <= 0){
			tierRB.setEnabled(false);
		}
	}
	
	/**
	 * Gets the current mode of this panel
	 * 
	 * @return avMode - TierSelectionPanel.AUDIO_MODE / 
	 * 					TierSelectionPanel.VIDEO_MODE
	 */
	@Override
	public int getMode(){
		return avMode;
	}
	

	/**
	 * Notifies the selection panel with the files that are 
	 * currently selected for the recognizer.
	 * 
	 * Used in video mode to determine which video file to 
	 * show for changing the region of interest.
	 * 
	 * @param mediaFiles 
	 */
	public void updateMediaFiles(List<String> mediaFilePaths){		
		if(avMode == Recognizer.VIDEO_TYPE){
			mediaFiles = mediaFilePaths;
		}		
		selectionPanel.updateStereoMode();		
	}
	
	
	public void updateTierNames(int event){
		switch(event){
		case ACMEditEvent.ADD_TIER:				
		case ACMEditEvent.REMOVE_TIER:				
			String tierName = (String) model.getSelectedItem();
			updateTierComboBox();
			if(model.getIndexOf(tierName) >= 0){
				model.setSelectedItem(tierName);
			}else{
				tierComboBox.setSelectedIndex(0);
			}
			break;
		case ACMEditEvent.CHANGE_TIER:		
			int index = tierComboBox.getSelectedIndex();
			updateTierComboBox();
			tierComboBox.setSelectedIndex(index);			
			break;
		}
	}
	
	/**
	 * Returns the path to the file or folder or 
	 * selected media file.
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 * @return Map
	 */
	@Override
	public Map<String, Object> getParamValue() {	
		paramValueMap.clear();
		if(selectionsRB.isSelected()) {
			paramValueMap.put(SELECTIONS, selectionPanel.getSelections());
		} else if(tierRB.isSelected()){
			Object val = getTierSelections();
			if(val == null){
				paramValueMap.put(TIER, null);	
				paramValueMap.put(TIER_NAME, null);	
			} else {
				paramValueMap.put(TIER, getTierSelections());	
				paramValueMap.put(TIER_NAME, model.getSelectedItem().toString());	
			}
			
		} else {
			paramValueMap.put(FILE_NAME, fileField.getText());	
		}
		return paramValueMap;
	}
	
	/**
	 * Returns the current selection value
	 * 
	 * @return ArrayList<RSelection> or String or null
	 */
	@Override
	public Object getSelectionValue(){		
		getParamValue();
		if(selectionsRB.isSelected()) {
			return paramValueMap.get(SELECTIONS);
		} else if(tierRB.isSelected()){
			return paramValueMap.get(TIER);				
		} else {
			return paramValueMap.get(FILE_NAME);	
		}
	}
	
	/**
	 * Converts the given map into a new map in the format which 
	 * can be stored in the elan preferences
	 * 
	 * @param map map to be converted to a storable map
	 * @return Map - map which can be stored by elan preferences
	 */
	@Override
	public Map<String, Object> getStorableParamPreferencesMap(Map<String, Object> map){
		Map<String, Object> newMap = null;
		Object value = null;
		if(map.containsKey(TierSelectionPanel.SELECTIONS)){
			value = map.get(TierSelectionPanel.SELECTIONS);
			newMap = new HashMap<String, Object>();
			if(value instanceof List){
				// List<RSelection> -> List<Long>(2)
				// store the selection objects;						
				Map<String, List<Long>> selectionMap = new HashMap<String, List<Long>>();
				List<Long> selection;
				for(int i= 0; i <((List)value).size(); i++){
					Object val = ((List)value).get(i);
					if(val instanceof RSelection){
						selection = new ArrayList<Long>();
						selection.add(((RSelection)val).beginTime);
						selection.add(((RSelection)val).endTime);								
						selectionMap.put(Integer.toString(selectionMap.size()+1), selection);
					}
				}
					
				newMap.put(TierSelectionPanel.SELECTIONS,selectionMap);				
			}else if(value == null){
				newMap.put(TierSelectionPanel.SELECTIONS, null);
			}
		} else if(map.containsKey(TierSelectionPanel.TIER_NAME)){
			newMap = new HashMap<String, Object>();
			newMap.put(TierSelectionPanel.TIER_NAME, map.get(TierSelectionPanel.TIER_NAME));
		} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
			newMap = new HashMap<String, Object>();
			newMap.put(TierSelectionPanel.FILE_NAME, map.get(TierSelectionPanel.FILE_NAME));
		}		
		return newMap;
	}
	

	/**
	 * Set the value for current parameters 
	 * 
	 * @param string value of the filepath
	 */
	@Override
	public void setParamValue(String value){
		fileRB.doClick();
		if(value != null){
			fileField.setText(value);
		}
	}
	
	/**
	 * Set the value for current parameters 
	 * 
	 * @param map map which contains the parameter name and its value
	 */
	@Override
	public void setParamValue(Map<String, ? extends Object> map){
		Object paramValue;
		if(map.containsKey(TierSelectionPanel.SELECTIONS)){
			//selectionsRB.setSelected(true);
			selectionsRB.doClick();
			paramValue = map.get(TierSelectionPanel.SELECTIONS);
			if(paramValue != null && paramValue instanceof Map) {
				// Map<String, List<Long>>
				Map<String, Object> mapv = (Map<String, Object>)paramValue;

	            for (Object rSel : mapv.values()) {
	                if (rSel != null && rSel instanceof List && ((List)rSel).size() == 2) {
	                	List<Long> sel = (List<Long>)rSel;
	                	if (sel.get(0) < sel.get(1)) {
	    					if (avMode == AUDIO_MODE) {
	    						selectionPanel.addSelection(new AudioSegment(sel.get(0), sel.get(1), null, 1));
	    					} else {
	    						selectionPanel.addSelection(new VideoSegment(sel.get(0), sel.get(1), null));
	    					}
	    				}
	                }
	            }
			}
		} else if(map.containsKey(TierSelectionPanel.TIER_NAME)){
			//tierRB.setSelected(true);	
			tierRB.doClick();
			paramValue = map.get(TierSelectionPanel.TIER_NAME);
			if(paramValue != null && paramValue instanceof String && model.getIndexOf(paramValue) > 0){
				model.setSelectedItem(paramValue.toString());
			}
		} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
			fileRB.doClick();
			paramValue = map.get(TierSelectionPanel.FILE_NAME);
			if(paramValue != null && paramValue instanceof String){
				fileField.setText(paramValue.toString());
			}
		}
	}
	
	/**
	 * Returns a list of tier selections 
	 * 
	 * @return an ArrayList with recognizer.data.RSelection Objects,
	 * 			can be null
	 */
	private Object getTierSelections(){		
		String tierName = tierComboBox.getSelectedItem().toString();
		TierImpl ti = null;
		if(!tierName.equals(SELECT_TIER)){
			ti = (TierImpl) vm.getTranscription().getTierWithId(tierName);
		}
		
		if (ti != null) {					
			List<AbstractAnnotation> anns = ti.getAnnotations();
			List<RSelection> segments = new ArrayList<RSelection>(anns.size());
			for (AbstractAnnotation aa: anns) {
				segments.add(new AudioSegment(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary(), 
						aa.getValue()));
			}
			
			return segments;
		}		
		return null;
	}
	
	/**
	 *@see java.awt.event.ActionListener#actionPerformed(ActionEvent e)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source instanceof JRadioButton){
			if( source == selectionsRB){
				settingsPanel.removeAll();
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.WEST;			
				gbc.insets = insets;	
				gbc.weightx = 1.0;
				settingsPanel.add(selectionPanel, gbc);
			
			} else if( source == tierRB){
				settingsPanel.removeAll();
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.NORTHWEST;	
				gbc.gridx = 0;
				gbc.insets = new Insets(3, 1, 0, 1);
				gbc.weightx = 0.0;
				gbc.fill = GridBagConstraints.NONE;
				settingsPanel.add(tierLabel, gbc);					
				
				gbc.gridx = 1;	
				gbc.weightx = 1.0;
				gbc.insets= new Insets(0, 1, 0, 1);
				settingsPanel.add(tierComboBox, gbc);	
				
			}else if( source == fileRB){
				settingsPanel.removeAll();		
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.insets = insets;	
				gbc.anchor = GridBagConstraints.NORTHWEST;	
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				settingsPanel.add(fileField, gbc);			
				
				gbc.gridx = 1;		
				gbc.weightx = 0.0;
				gbc.fill = GridBagConstraints.NONE;
				settingsPanel.add(browseButton, gbc);	
			}			
			revalidate();
		} else if(source == browseButton){
			FileChooser chooser = new FileChooser(this);
			
			String[] mainFilterExt = null;
			if(fileExtensions != null && fileExtensions.size() > 0){
				mainFilterExt = fileExtensions.get(0);
			}			
			chooser.createAndShowFileDialog("Select a tier(s) file", dialogType, fileExtensions, mainFilterExt, "Recognizer.Dir", null);
			File f = chooser.getSelectedFile();
			if(f != null){
				initialPath = f.getAbsolutePath();
				fileField.setText(initialPath);
			}
		} 
	}
	
	/**
	 * Only updates the UI if the specified Locale is the same as the ELAN Locale 
	 * or when it is null.
	 */
	@Override
	public void updateLocale(Locale locale) {
		if (locale == null || locale == ElanLocale.getLocale()) {
			selectionsRB.setText(ElanLocale.getString("Recognizer.SelectionsPanel.RB.Selection"));
			tierRB.setText(ElanLocale.getString("Recognizer.SelectionsPanel.RB.Tier"));
			fileRB.setText(ElanLocale.getString("Recognizer.SelectionsPanel.RB.File"));
			
			tierLabel.setText(ElanLocale.getString("Recognizer.SelectionsPanel.Label.Tier"));
			
			if(selectionPanel != null){
				selectionPanel.updateLocale();
			}
		}
	}
	
	/**
	 * This implementation has direct access to ElanLocale, it just 
	 * forwards to {@link #updateLocale(Locale)}
	 */
	@Override
	public void updateLocaleBundle(ResourceBundle bundle) {
		if (bundle != null) {
			updateLocale(bundle.getLocale());
		} else {
			updateLocale(null);
		}
	}

	/**
	 * Panel to select selections
	 * 
	 * @author Han Sloetjes
	 */
	private class SelectionPanel extends JPanel implements ActionListener {
		private Selection selection;	
		protected TitledBorder border;
		protected JPanel buttonPanel;	
		private JButton addSelection;
		private JButton addSelection1;
		private JButton addSelection2;
		private JButton removeSelection;		
		protected JList selectionList;
		private DefaultListModel selectionModel;
		private JScrollPane scrollPane;
		
		private boolean stereoMode = false;		
		
		/**
		 * Constructor
		 */
		public SelectionPanel() {
			super();			
			selection = vm.getSelection();
			
			border = new TitledBorder(ElanLocale.getString("Recognizer.SelectionsPanel.Title"));
			setBorder(border);
			setLayout(new BorderLayout());
			
			buttonPanel = new JPanel();		
			
			addSelection = new JButton();	
			addSelection1 = new JButton(ElanLocale.getString("Recognizer.SelectionsPanel.Channel1"));
			addSelection2 = new JButton(ElanLocale.getString("Recognizer.SelectionsPanel.Channel2"));
			
			ImageIcon icon = null;
			String text = ElanLocale.getString("Recognizer.SelectionsPanel.Add");
			try {
				icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
				addSelection.setIcon(icon);		
				addSelection.setToolTipText(text);
				
				addSelection1.setIcon(icon);		
				addSelection2.setIcon(icon);		
				
				addSelection.setIcon(icon);		
				addSelection.setToolTipText(text);
			} catch (Exception ex) {// any		
				addSelection.setText(text);	
				addSelection1.setText(text + "  "+ addSelection1.getText());		
				addSelection2.setText(text + "  "+ addSelection2.getText());		
			}	
			
			addSelection.addActionListener(this);
			addSelection1.addActionListener(this);
			addSelection2.addActionListener(this);
			
			removeSelection = new JButton( );
			text = ElanLocale.getString("Recognizer.SelectionsPanel.Remove");
			try {
				icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
				removeSelection.setIcon(icon);		
				removeSelection.setToolTipText(text);
			} catch (Exception ex) {// any		
				removeSelection.setText(text);		
			}	
			removeSelection.addActionListener(this);
			
			if (stereoMode) {
				buttonPanel.add(addSelection1);
				buttonPanel.add(addSelection2);
				buttonPanel.add(removeSelection);
			} else {
				buttonPanel.add(addSelection);
				buttonPanel.add(removeSelection);
			}			
			selectionModel = new DefaultListModel();
			selectionList = new JList(selectionModel);
			selectionList.setCellRenderer(new SelectionListRenderer());
			selectionList.addMouseListener(new MouseHandler());
			selectionList.setBackground(getBackground());
			
			updateStereoMode();
			
			setLayout(new GridBagLayout());
			setBorder(border);	
			
			scrollPane = new JScrollPane(selectionList);
			scrollPane.getViewport().setPreferredSize(new Dimension(300,50));			
			buttonPanel.setLayout(new GridLayout(2, 1));					
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			add(scrollPane, gbc);
			
			gbc.gridx = 1;
			add(buttonPanel, gbc);	
		}		
		
		/**
		 * Updates the background of this panel and
		 * all other components in this panel
		 * 
		 * @param color - background color to be updated
		 */
		private void updateBackgroundColor(Color color){
			buttonPanel.setBackground(color);
			scrollPane.setBackground(color);
			selectionList.setBackground(color);
			super.setBackground(color);
		}
		
		private void updateLocale() {
			border.setTitle(ElanLocale.getString("Recognizer.SelectionsPanel.Title"));
			
			if(removeSelection.getText() != null && removeSelection.getText().length() >0){
				removeSelection.setText(ElanLocale.getString("Recognizer.SelectionsPanel.Remove"));
			}
			
			if(addSelection1.getIcon() == null){
				String text = ElanLocale.getString("Recognizer.SelectionsPanel.Add");
				addSelection.setText(text);
				addSelection1.setText(text + " " + ElanLocale.getString("Recognizer.SelectionsPanel.Channel1"));
				addSelection2.setText(text + " " + ElanLocale.getString("Recognizer.SelectionsPanel.Channel2"));
			}else{
				addSelection1.setText(ElanLocale.getString("Recognizer.SelectionsPanel.Channel1"));
				addSelection2.setText(ElanLocale.getString("Recognizer.SelectionsPanel.Channel2"));
			}
		}
		
		/**
		 * Sets the new stereo mode.
		 * 
		 * @param stereoMode true if separate segments for channel one and channel two should be supported
		 */
		private void updateStereoMode() {
			boolean stereoMode = false;
			if(getMode() == AUDIO_MODE){
				if (vm.getSignalViewer() != null) {
					try {					
						stereoMode = new WAVSampler(vm.getSignalViewer().getMediaPath()).getWavHeader().getNumberOfChannels() > 1;
					} catch (Exception e) {
						System.out.println("Cannot handle file: " + e.getMessage());	// log	
					}
				}
			}
			
			if (this.stereoMode == stereoMode) {
				return;
			}
			
			this.stereoMode = stereoMode;
			
			if (stereoMode) {
				buttonPanel.removeAll();
				buttonPanel.add(addSelection1);
				buttonPanel.add(addSelection2);
				buttonPanel.add(removeSelection);
			} else {
				buttonPanel.removeAll();
				buttonPanel.add(addSelection);
				buttonPanel.add(removeSelection);
			}
			selectionModel.removeAllElements();
		}
		
		/**
		 * Returns the current stereo mode.
		 * 
		 * @return true if currently segments for the left and right channel can be specified
		 */
		public boolean getStereoMode() {
			return stereoMode;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			if (source.equals(addSelection)) {
				if (selection.getBeginTime() < selection.getEndTime()) {
					if (avMode == AUDIO_MODE) {
						addSelection(new AudioSegment(selection.getBeginTime(), selection.getEndTime(), null, 1));
					} else {
						addSelection(new VideoSegment(selection.getBeginTime(), selection.getEndTime(), null));
					}
				}
			} else if (source.equals(addSelection1)) {// audio segments
				if (selection.getBeginTime() < selection.getEndTime()) {
					addSelection(new AudioSegment(selection.getBeginTime(), selection.getEndTime(), null, 1));
				}
			} else if (source.equals(addSelection2)) {
				if (selection.getBeginTime() < selection.getEndTime()) {
					addSelection(new AudioSegment(selection.getBeginTime(), selection.getEndTime(), null, 2));
				}
			} else if (source.equals(removeSelection)) {
				int[] selIndices = selectionList.getSelectedIndices();
				if (selIndices != null) {
					for (int i = selIndices.length - 1; i >= 0; i--) {					
						selectionModel.remove(selIndices[i]);
					}
				}
			} 			
		}
				
		/**
		 * Adds a selection to the model
		 * 
		 * @param sel
		 */
		private void addSelection(RSelection sel) {
			if (selectionModel.isEmpty()) {
				selectionModel.addElement(sel);
				return;
			}
			
			Object iter;
			RSelection otherSel;
			
			for (int i = 0; i < selectionModel.getSize(); i++) {
				iter = selectionModel.get(i);
				if (iter instanceof RSelection) {
					otherSel = (RSelection) iter;
					if (otherSel.beginTime == sel.beginTime && otherSel.endTime == sel.endTime) {
						if (otherSel instanceof AudioSegment && sel instanceof AudioSegment) {
							if (((AudioSegment)otherSel).channel == ((AudioSegment)sel).channel) {
								// same selection already in the list
								return;
							} else {
								if (((AudioSegment)sel).channel < ((AudioSegment)otherSel).channel) {
									selectionModel.add(i, sel);
									return;
								} else {
									if (i < selectionModel.getSize() - 1) {
										selectionModel.add(i, sel);
										return;
									} else {
										selectionModel.addElement(sel);
										return;
									}
								}
							}
						} else {
							// same selection already in the list
							return;
						}
					} else if (otherSel.beginTime == sel.beginTime && otherSel.endTime > sel.endTime) {
						selectionModel.add(i, sel);// add before
						return;
					} else if (sel.beginTime < otherSel.beginTime) {
						selectionModel.add(i, sel);// add before
						return;
					}
				}
				if (i == selectionModel.getSize() - 1) {
					// not yet inserted
					selectionModel.addElement(sel);
				}
			}	
		}
		
		/**
		 * Shows a popup for Audio and Video segments
		 */
		private void handlePopUp(Point p) {
			int row = selectionList.locationToIndex(p);
			
			if (row > -1) {
				Object sel = selectionModel.elementAt(row);

				if (sel instanceof AudioSegment) {
					// create popup with one setLabel item
					final AudioSegment as = (AudioSegment) sel;
					JPopupMenu popup = new JPopupMenu();
					JMenuItem mi = new JMenuItem(new LabelAction(as));
					popup.add(mi);
//					popup.add(new SaveTierAction(null));
					popup.show(selectionList, p.x, p.y);				
				} else if (sel instanceof VideoSegment) {
					// create popup with a setLabel and setShape item
					final VideoSegment vs = (VideoSegment) sel;
					JPopupMenu popup = new JPopupMenu();
					JMenuItem mi = new JMenuItem(new LabelAction(vs));
					popup.add(mi);
					mi = new JMenuItem(new ShapeAction(vs));
					popup.add(mi);
//					popup.add(new SaveTierAction(null));
					popup.show(selectionList, p.x, p.y);
				} else if (!selectionModel.isEmpty()) {
//					JPopupMenu popup = new JPopupMenu();
//					popup.add(new SaveTierAction(null));
//					popup.show(selectionList, p.x, p.y);
				}
			}
		}
		
		/**
		 * Prompts the user to specify a location where to store the tier/selections.
		 * 
		 * @return the path or null if canceled
		 */
		private String promptForTierFile() {
			ArrayList<String[]> extensions = new ArrayList<String[]>();
			extensions.add(FileExtension.CSV_EXT);

			FileChooser chooser = new FileChooser(this);
			chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, extensions,FileExtension.XML_EXT, "Recognizer.Path", null);
			
			File f = chooser.getSelectedFile();
			if (f != null) {			
				return f.getAbsolutePath();
			} 
			
			return null;
		}
		
		/**
		 * Returns the list of selections 
		 * @return an ArrayList with recognizer.data.RSelection Objects
		 */
		public List<RSelection> getSelections() {
			List<RSelection> selectionObjects = null;
			
			for (int i = 0; i < selectionModel.getSize(); i++) {
				if(selectionObjects == null){
					selectionObjects = new ArrayList<RSelection>();
				}
				selectionObjects.add((RSelection) selectionModel.get(i));
			}
			
			return selectionObjects;
		}
		
		/**
		 * Returns whether there is at least one selection/segment.
		 * 
		 * @return true if there is at least one selection, false otherwise 
		 */
		public boolean hasSelections() {
			int numSels = 0;
			Object iter;
			
			for (int i = 0; i < selectionModel.getSize(); i++) {
				iter = selectionModel.get(i);
				if (iter instanceof RSelection) {
					numSels ++;
					break;
				}
			}
			
			return numSels > 0;
		}
		
		/**
		 * Clears the list of selections.
		 */
		public void clearSelections() {
			if (!selectionModel.isEmpty()) {
				selectionModel.clear();
			}
		}
	
		/**		
		 * Enables / disables this panel
		 * 
		 * @param enabled - true->enabled, false ->disabled		 
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (addSelection != null) {
				addSelection.setEnabled(enabled);
			}
			if (addSelection1 != null) {
				addSelection1.setEnabled(enabled);
			}
			if (addSelection2 != null) {
				addSelection2.setEnabled(enabled);
			}			
			if (removeSelection != null) {
				removeSelection.setEnabled(enabled);
			}
		}
		
		/**
		 * Mouse handler class.
		 * 
		 * @author Han Sloetjes
		 */
		public class MouseHandler extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent event) {
				// set the selection to the double clicked entry in the JList
				if (event.getClickCount() > 1) {
					Object o = selectionList.getSelectedValue();
					if (o instanceof RSelection) {
						long bt = ((RSelection) o).beginTime;
						long et = ((RSelection) o).endTime;
						
						selection.setSelection(bt, et);
						vm.getMasterMediaPlayer().setMediaTime(bt);
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// right mouse popup handling
		        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
		                || e.isPopupTrigger()) {
		        	//int row = selectionList.locationToIndex(e.getPoint());
		        	handlePopUp(e.getPoint());
		        }
			}
		}
		
		/** 
		 * An action to set/change the label of a segment.
		 * 
		 * @author Han Sloetjes
		 */
		class LabelAction extends AbstractAction {
			private Segment segment;

			/**
			 * Constructor with a segment as an argument.
			 * 
			 * @param segment the segment to set the label for
			 */
			public LabelAction(Segment segment) {
				super(ElanLocale.getString("Recognizer.SelectionsPanel.SetLabel"));
				this.segment = segment;
			}

			/**
			 * Shows an input dialog for this segment.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				String initial = "";
				if (segment.label != null) {
					initial = segment.label;
				}
				String val = (String) JOptionPane.showInputDialog(SelectionPanel.this, 
						ElanLocale.getString("Recognizer.SelectionsPanel.SetLabelDesc"), 
						ElanLocale.getString("Recognizer.SelectionsPanel.SetLabel"),
						JOptionPane.PLAIN_MESSAGE, null, null,
						initial);			
				if (val != null) {//null means input dialog canceled
					segment.label = val; //can be empty
				}			
			}		
		}
		
		/**
		 * An action to set/change a region of interest in a video stream.
		 * 
		 * @author Han Sloetjes
		 */
		class ShapeAction extends AbstractAction {
			private VideoSegment segment;
			
			public ShapeAction(VideoSegment segment) {
				super(ElanLocale.getString("Recognizer.SelectionsPanel.SetShape"));
				this.segment = segment;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaFiles == null || mediaFiles.size() == 0) {
					// show warning, no media file
					JOptionPane.showMessageDialog(SelectionPanel.this, 
							ElanLocale.getString("Recognizer.SelectionsPanel.WarnNoMedia"), 
							ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				String mediaPathToUse = null;
				if (mediaFiles.size() > 1) {
					String[] options = new String[mediaFiles.size()];
					String p;
					for (int i = 0; i < mediaFiles.size(); i++) {
						p = mediaFiles.get(i);
						p = FileUtility.fileNameFromPath(p);
						options[i] = p;
					}
					String sel = (String) JOptionPane.showInputDialog(SelectionPanel.this, 
							ElanLocale.getString("Recognizer.SelectionsPanel.SelectMedia"), "", 
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (sel != null) {
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(sel)) {
								mediaPathToUse = mediaFiles.get(i);
								break;
							}
						} 
					}
				} else {
					mediaPathToUse = mediaFiles.get(0);
				}
				
				ElanMediaPlayer player = null;
				String url;
				
				if (vm.getMasterMediaPlayer().getMediaDescriptor() != null) {
					url = vm.getMasterMediaPlayer().getMediaDescriptor().mediaURL;
					if (url != null && url.endsWith(mediaPathToUse)) {
						player = vm.getMasterMediaPlayer();
					}
				}
				
				if (player == null) {
					List<ElanMediaPlayer> slaves = vm.getSlaveMediaPlayers();
					for (ElanMediaPlayer slayer : slaves) {
						if (slayer.getMediaDescriptor() != null) {
							url = slayer.getMediaDescriptor().mediaURL;
							if (url != null && url.endsWith(mediaPathToUse)) {
								player = slayer;
								break;
							}
						}
					}
				}
				
				if (player == null) {
					// show warning, no player
					JOptionPane.showMessageDialog(SelectionPanel.this, 
							ElanLocale.getString("Recognizer.SelectionsPanel.WarnNoPlayer"), 
							ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if (!(player instanceof VideoFrameGrabber)) {
					JOptionPane.showMessageDialog(SelectionPanel.this, 
							ElanLocale.getString("Recognizer.SelectionsPanel.WarnNoGrabber"), 
							ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				// we have the player
				//System.out.println("Player: " + player.getMediaDescriptor().mediaURL);
				List<Shape> curSh = new ArrayList<Shape> (4);
				if (segment.getShape() != null) {
					curSh.add(segment.getShape());
				}
				
				Graphics2DEditor editor = new Graphics2DEditor(
						ELANCommandFactory.getRootFrame(vm.getTranscription()), (VideoFrameGrabber) player, 
						segment.beginTime, segment.endTime, curSh);
				editor.setVisible(true);
				List<Shape> shapes = editor.getShapes();
				if (shapes != null && !shapes.isEmpty()) {
					segment.setShape(shapes.get(0));
				}
			}
			
		}
		
//		class SaveTierAction extends AbstractAction {
//			private Segmentation segmentation;
//
//			/**
//			 * @param segmentation
//			 */
//			public SaveTierAction(Segmentation segmentation) {
//				super();
//				if (segmentation != null) {
//					putValue(Action.NAME, ElanLocale.getString("Recognizer.SelectionsPanel.SaveTier"));
//				} else {
//					putValue(Action.NAME, ElanLocale.getString("Recognizer.SelectionsPanel.SaveSelections"));
//				}
//				this.segmentation = segmentation;
//			}
//
//			public void actionPerformed(ActionEvent e) {		
//				String filePath = promptForTierFile();
//				File tf = new File(filePath);
//				try { 
//					if (tf.exists()) {
//	                    int answer = JOptionPane.showConfirmDialog(SelectionPanel.this,
//	                            ElanLocale.getString("Message.Overwrite"),
//	                            ElanLocale.getString("SaveDialog.Message.Title"),
//	                            JOptionPane.YES_NO_OPTION,
//	                            JOptionPane.WARNING_MESSAGE);
//
//	                    if (answer == JOptionPane.NO_OPTION) {
//	                        return;
//	                    }
//					}
//				} catch (Exception ex) {// any exception
//					return;
//				}
//				
//				if (segmentation != null) {
//					List<Object> list = new ArrayList<Object>(1);
//					list.add(segmentation);
//					try {
//						RecTierWriter xTierWriter = new RecTierWriter();
//						xTierWriter.write(tf, list, false);
//					} catch (IOException ioe) {
//						// show message
//						JOptionPane.showMessageDialog(SelectionPanel.this, ElanLocale.getString(
//								"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
//								ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
//					}
//				} else if (!selectionModel.isEmpty()) {
//					try {
//						RecTierWriter xTierWriter = new RecTierWriter();
//						xTierWriter.write(tf, getSelections());
//					} catch (IOException ioe) {
//						// show message
//						JOptionPane.showMessageDialog(SelectionPanel.this, ElanLocale.getString(
//								"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
//								ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
//					}
//				}
//			}
//		}
	}
}
