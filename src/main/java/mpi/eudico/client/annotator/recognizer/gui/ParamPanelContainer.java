package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.TextParam;
import mpi.eudico.server.corpora.clom.Tier;

/**
 * A container panel for parameter panels.
 * 
 * @author Han Sloetjes
 * @updated Sep 2012, aarsom
 */
@SuppressWarnings("serial")
public class ParamPanelContainer extends JPanel implements ParamPreferences {
	private String recogizerName;	
	private List<AbstractParamPanel> panels;
	
	private List<FileParamPanel> mediaUpdatePanels;
	private List<TierSelectionPanel> tierUpdatePanels;
	
	private JPanel inputParamPanel;
	private JPanel outputParamPanel;
	private JPanel settingsParamPanel;
	
	private JPanel advancedInputPanel;
	private JPanel advancedOutputPanel;
	private JPanel advancedSettingsPanel;
	
	private ViewerManager2 vm;
	private RecognizerHost host;
	private List<String> tiers;
	private List<String> supportedMediaFiles;
	
	private boolean startReg = true;
	
	private JDialog advancedDialog;
	
	private JButton advancedParamButton;	
	
	private final Color BACKGROUND_COLOR = new Color(250,250,250);
	
	/**
	 * A container panel for parameter panels.
	 * 
	 * @param recogizerName the name of the recognizer
	 */
	public ParamPanelContainer(String recogizerName) {
		super();
		this.recogizerName = recogizerName;		
		this.setLayout(new GridBagLayout());	
		this.setBorder(new EmptyBorder(2,2,2,2));
		panels = new ArrayList<AbstractParamPanel>(10);
	}
	
	/**
	 * A container panel for parameter panels.
	 * 
	 * @param recogizerName the name of the recognizer
	 * @param params the list of parameters of the recognizer 
	 * @param supportedMediaFiles a list of media files according to the mode
	 * @param mode Recognizer.AUDIO_TYPE etc, the kind of recognizer this is.
	 */
	public ParamPanelContainer(String recogizerName, List<Param> params, RecognizerHost host, ViewerManager2 vm, int mode) {
		super();
		
		this.recogizerName = recogizerName;
		this.host = host;
		
		mediaUpdatePanels = new ArrayList<FileParamPanel>();
		tierUpdatePanels = new ArrayList<TierSelectionPanel>();
		
		if (params != null) {
			this.vm = vm;	
			
			tiers = new ArrayList<String>();
			List<? extends Tier> list = vm.getTranscription().getTiers();
			if(list !=null){
				for (Tier tier : list) {
					tiers.add(tier.getName());
				}
			}
			
			this.supportedMediaFiles = host.getMediaFiles(mode);
			
			loadParameterPanels(params, mode);
					
			setLayout(new GridBagLayout());
			setBorder(new EmptyBorder(2,2,2,2));
			
			doLayout(false);
			
		} else {
			panels = new ArrayList<AbstractParamPanel>(10);
		}		
	}
	
	/**
	 * Makes the layout
	 * 
	 * @param addScrollPanes
	 */
	public void doLayout(boolean addScrollPanes){
		this.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
				
		JScrollPane jsp;
		if(addScrollPanes){		
			gbc.gridy=0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;			
			if(settingsParamPanel != null){	
				settingsParamPanel.setBorder(null);
				jsp = new JScrollPane(settingsParamPanel);
				jsp.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel")));
				jsp.setBackground(getBackground());
				jsp.getViewport().setBackground(getBackground());
				add(jsp, gbc);
			} 
			
			if(inputParamPanel != null){
				gbc.gridy++;	
				inputParamPanel.setBorder(null);
				jsp = new JScrollPane(inputParamPanel);
				jsp.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.InputPanel")));
				jsp.setBackground(getBackground());
				jsp.getViewport().setBackground(getBackground());
				add(jsp, gbc);
			}
			
			if(outputParamPanel != null){
				gbc.gridy++;
				outputParamPanel.setBorder(null);
				jsp = new JScrollPane(outputParamPanel);
				jsp.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel")));
				jsp.setBackground(getBackground());
				jsp.getViewport().setBackground(getBackground());
				add(jsp, gbc);
			}
			
			if(advancedParamButton != null){
				gbc.gridy++;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				add(advancedParamButton, gbc);
			}
		} else{		
			gbc.gridy = -1;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			if(advancedParamButton != null){
				gbc.gridy++;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				add(advancedParamButton, gbc);
			}		
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			if(settingsParamPanel != null){	
				gbc.gridy++;	
				settingsParamPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel")));
				add(settingsParamPanel, gbc);
			} 
			
			if(inputParamPanel != null){
				gbc.gridy++;		
				inputParamPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.InputPanel")));
				add(inputParamPanel, gbc);				
			}
			
			if(outputParamPanel != null){
				gbc.gridy++;
				outputParamPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel")));
				add(outputParamPanel, gbc);						
			}				
		}
	}
	
	public void updateLocale(){
		if(settingsParamPanel != null){	
			if(settingsParamPanel.getBorder() == null){
				((TitledBorder)((JScrollPane)settingsParamPanel.getParent()).getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel"));				
			} else {
				((TitledBorder)settingsParamPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel"));
			} 
		}
		
		if(inputParamPanel != null){	
			if(inputParamPanel.getBorder() == null){
				((TitledBorder)((JScrollPane)inputParamPanel.getParent()).getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.InputPanel"));				
			} else {
				((TitledBorder)inputParamPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.InputPanel"));
			} 
		}
		
		if(outputParamPanel != null){	
			if(outputParamPanel.getBorder() == null){
				((TitledBorder)((JScrollPane)outputParamPanel.getParent()).getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel"));				
			} else {
				((TitledBorder)outputParamPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel"));
			} 
		}
		
		if(advancedSettingsPanel != null ){				
			((TitledBorder)advancedSettingsPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel"));			
		}

		if(advancedInputPanel != null ){				
			((TitledBorder)advancedInputPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.InputPanel"));			
		}
		
		if(advancedOutputPanel != null ){				
			((TitledBorder)advancedOutputPanel.getBorder()).setTitle(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel"));			
		}
		
		if(advancedParamButton != null){
			advancedParamButton.setText(ElanLocale.getString("Recognizer.ParamPanel.AdvancedParamPanel"));
		}
	}
	
	/**
	 * Creates respective panel for each parameter
	 * depending on their type
	 * 
	 * @param params list of parameters
	 * @param mode (AUDIO /VIDEO recognizer)
	 */
	private void loadParameterPanels(List<Param> params, int mode){		
		panels = new ArrayList<AbstractParamPanel>(params.size());
		AbstractParamPanel panel;
		int settingsPanelRowIndex = -1;
		int inputPanelRowIndex= -1;
		int outputPanelRowIndex = -1;
		
		int advancedSPRowIndex = -1;
		int advancedIPRowIndex= -1;
		int advancedOPRowIndex = -1;
		
		
		Comparator<Param> compare = new Comparator<Param>(){
			@Override
			public int compare(Param o1, Param o2) {
				if(!(o1 instanceof FileParam) && !(o2 instanceof FileParam)){
					return 0;
				}
				
				if(o1 instanceof FileParam && o2 instanceof FileParam){
					
					if((!((FileParam)o1).optional && !((FileParam)o2).optional) ||
							(((FileParam)o1).optional && ((FileParam)o2).optional)){
						
						if( (((FileParam)o1).contentType == FileParam.AUDIO || ((FileParam)o1).contentType == FileParam.VIDEO) &&
								(((FileParam)o2).contentType == FileParam.AUDIO || ((FileParam)o2).contentType == FileParam.VIDEO)){
							return 0;
						}
						
						if(((FileParam)o1).contentType == FileParam.AUDIO || ((FileParam)o1).contentType == FileParam.VIDEO){
							return -1;
						}
						
						if(((FileParam)o2).contentType == FileParam.AUDIO || ((FileParam)o2).contentType == FileParam.VIDEO){
							return 1;
						}
						
						return 0;
					}			
					
					if(!((FileParam)o1).optional){
						return -1;
					}
					
					if(!((FileParam)o2).optional){
						return 1;
					}
				}
				
				if(o1 instanceof FileParam ){
					if(!((FileParam)o1).optional){
						return -1;
					}
				}
				
				if(o2 instanceof FileParam){
					if(!((FileParam)o2).optional){
						return 1;
					}
				}
				
				if(o1 instanceof FileParam && !(o2 instanceof FileParam)){
					return -1;					
				} else {
					return 1;
				}
				
			}
		};
				
		Collections.sort(params, compare);
		
		GridBagConstraints gbc = new GridBagConstraints();		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1,1,4,1);
		gbc.weightx = 1.0;
		
		TierSelectionPanel tierPanel;
				
		for (Param p : params) {
			if (p instanceof NumParam) {	
				if(p.level.equals(Param.BASIC)){
					if(settingsParamPanel == null){
						settingsParamPanel = new JPanel(new GridBagLayout());	
					}
					
					panel = new NumParamPanel((NumParam) p);
					gbc.gridy = ++settingsPanelRowIndex;
					settingsParamPanel.add(panel, gbc);
					panels.add(panel);		
					
					panel.setBackground(getBackgroundColorForPanel(settingsPanelRowIndex));
					
				} else {
					if(advancedSettingsPanel == null){
						advancedSettingsPanel = new JPanel(new GridBagLayout());
						advancedSettingsPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel")));
					}
					
					panel = new NumParamPanel((NumParam) p);
					gbc.gridy = ++advancedSPRowIndex;
					advancedSettingsPanel.add(panel, gbc);
					panels.add(panel);		
					
					panel.setBackground(getBackgroundColorForPanel(advancedSPRowIndex));
				}
				
			} else if (p instanceof TextParam) {
				if(p.level.equals(Param.BASIC)){
					if(settingsParamPanel == null){
						settingsParamPanel = new JPanel(new GridBagLayout());						
					} 
					
					panel = new TextParamPanel((TextParam) p);
					gbc.gridy = ++settingsPanelRowIndex;
					settingsParamPanel.add(panel, gbc);
					panels.add(panel);
					
					panel.setBackground(getBackgroundColorForPanel(settingsPanelRowIndex));
				} else {
					if(advancedSettingsPanel == null){
						advancedSettingsPanel = new JPanel(new GridBagLayout());
						advancedSettingsPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.SettingsPanel")));
					} 
					
					panel = new TextParamPanel((TextParam) p);
					gbc.gridy = ++advancedSPRowIndex;
					advancedSettingsPanel.add(panel, gbc);
					panels.add(panel);
					
					panel.setBackground(getBackgroundColorForPanel(advancedSPRowIndex));
				}
				
			} else if (p instanceof FileParam) {
				tierPanel = null;
				final FileParam fileParam = (FileParam) p;
				if (fileParam.contentType == FileParam.AUDIO || fileParam.contentType == FileParam.VIDEO) {
					int fileMode = fileparamToMode(fileParam.contentType);
					List<String> mediaFiles = host.getMediaFiles(fileMode);
					panel = new FileParamPanel((FileParam) p, mediaFiles /*supportedMediaFiles*/);		
					mediaUpdatePanels.add((FileParamPanel) panel);
					if (!fileParam.optional && mediaFiles.isEmpty()) {
						startReg = false;
					}
				} else if (fileParam.ioType == FileParam.IN && 
						(fileParam.contentType == FileParam.CSV_TIER || fileParam.contentType == FileParam.TIER || fileParam.contentType == FileParam.MULTITIER)) {	
						List<String> tierList = new ArrayList<String>();
						for(String name : tiers){
							tierList.add(name);
						}	
						tierPanel = new TierSelectionPanel(mode, supportedMediaFiles, vm, true);
						panel = new TierParamPanel((FileParam) p, tierPanel);
						tierUpdatePanels.add(tierPanel);
						mediaUpdatePanels.add((FileParamPanel) panel);
				} else{
					 panel = new FileParamPanel((FileParam) p);
				}			
				
				if(!fileParam.optional || p.level.equals(Param.BASIC)){					
					if((fileParam.ioType == FileParam.IN)){
						if(inputParamPanel == null){
							inputParamPanel = new JPanel(new GridBagLayout());
						} 
						
						gbc.gridy = ++inputPanelRowIndex;
						inputParamPanel.add(panel, gbc);
						panels.add(panel);
					
						panel.setBackground(getBackgroundColorForPanel(inputPanelRowIndex));
						if(tierPanel != null){
							tierPanel.updateBackgroundColor(getBackgroundColorForPanel(inputPanelRowIndex));
						}
					} else{
						if(outputParamPanel == null){
							outputParamPanel = new JPanel(new GridBagLayout());
						} 
											
						gbc.gridy = ++outputPanelRowIndex;
						outputParamPanel.add(panel, gbc);
						panels.add(panel);
						
						panel.setBackground(getBackgroundColorForPanel(outputPanelRowIndex));
					}				
				} else {						
					if((fileParam.ioType == FileParam.IN)){
						if(advancedInputPanel == null){
							advancedInputPanel = new JPanel(new GridBagLayout());
							advancedInputPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.InputPanel")));
						} 
						
						gbc.gridy = ++advancedIPRowIndex;
						advancedInputPanel.add(panel, gbc);
						panels.add(panel);
						
						panel.setBackground(getBackgroundColorForPanel(advancedIPRowIndex));
					
					} else{
						if(advancedOutputPanel == null){
							advancedOutputPanel = new JPanel(new GridBagLayout());
							advancedOutputPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.ParamPanel.OutputPanel")));
						} 
											
						gbc.gridy = ++advancedOPRowIndex;
						advancedOutputPanel.add(panel, gbc);
						panels.add(panel);
						
						panel.setBackground(getBackgroundColorForPanel(advancedOPRowIndex));
					}				
				}
				
			}
		}
		
		if((settingsParamPanel != null && advancedSettingsPanel != null) ||
				(inputParamPanel != null && advancedInputPanel != null) ||
				(outputParamPanel != null && advancedOutputPanel != null) ){		
			advancedParamButton = new JButton(ElanLocale.getString("Recognizer.ParamPanel.AdvancedParamPanel"));
			advancedParamButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					showAdvanceParametersDialog();					
				}
			});							
		} else {
			advancedParamButton = null;
		}
	}

	private int fileparamToMode(int fileParam) {
		switch (fileParam) {
			case FileParam.AUDIO:
				return Recognizer.AUDIO_TYPE;
			case FileParam.VIDEO:
				return Recognizer.VIDEO_TYPE;
			default:
				return Recognizer.OTHER_TYPE;
		}
	}
	
	/**
	 * Returns the background color for the  panel 
	 * at the given index
	 * 
	 * @param index
	 * @return color
	 */
	private Color getBackgroundColorForPanel(int index){
		if(index % 2 != 0){
			return BACKGROUND_COLOR;
		} else {
			return this.getBackground();
		}
		
	}
	
	/**
	 * Updates the media files that can be handled by the 
	 * current recognizer
	 * 
	 * @param mediaFilePaths, list of media files
	 */
	public void updateMediaFiles(List<String> mediaFilePaths){
		supportedMediaFiles = mediaFilePaths;
		startReg = true;
		for(FileParamPanel panel : mediaUpdatePanels){
			panel.updateMediaFiles(mediaFilePaths);
			if(!panel.isOptional() && supportedMediaFiles.size() <= 0){
				if(panel.getContentType() == FileParam.AUDIO || panel.getContentType() == FileParam.VIDEO){
					startReg = false;
				}
			}
		}
	}
	
	/**
	 * Updates the tier changes
	 * 
	 * @param event - ACMEditEvent.ADD_TIER or	
			          ACMEditEvent.REMOVE_TIER:	
			          ACMEditEvent.CHANGE_TIER:		
	 */
	public void updateTiers(int event){		
		for(TierSelectionPanel panel : tierUpdatePanels)
			panel.updateTierNames(event);		
	}
	
	/**
	 * Method which checks whether any supported
	 * media file is available to start the recognizer
	 * 
	 * @return boolean, indicates whether a recognizer
	 * 	       can be started or not
	 */
	public boolean checkStartReg(){
		return startReg;
	}
	

	/**
	 * Returns the name of the recognizer this panel is presenting a ui for.
	 * 
	 * @return the name of the recognizer
	 */
	public String getRecognizerName() {
		return recogizerName;
	}
	
	/**
	 * Stores the current values.
	 * 
	 * @return a map with the current values
	 */
	@Override
	public Map<String, Object> getParamPreferences() {
		Map<String, Object> storedPrefs = new HashMap<String, Object>(panels.size());
		
		for (AbstractParamPanel p : panels) {
			Object value = p.getParamValue();
			if(value instanceof Map){
				if(p instanceof TierParamPanel)	{
					storedPrefs.put(p.getParamName(), ((TierParamPanel)p).getStorableMap((HashMap)value));
				}
			} else {
				storedPrefs.put(p.getParamName(), value);
			}
		}
		return storedPrefs;
	}
	
	/**
	 * Returns the advanced parameter panel
	 * 
	 * @return JPanel
	 */
	private JPanel getAdvancedParameterPanel(){
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new GridBagLayout());
		advancedPanel.setBorder(new EmptyBorder(2,2,2,2));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy=0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		if(advancedSettingsPanel != null){
			advancedPanel.add(advancedSettingsPanel, gbc);
		}
		
		if(advancedInputPanel != null){
			gbc.gridy++;
			advancedPanel.add(advancedInputPanel, gbc);
		}
		
		if(advancedOutputPanel != null){
			gbc.gridy++;
			advancedPanel.add(advancedOutputPanel, gbc);
		}
		
		// add a close button
		
		return advancedPanel;
	}
	
	/**
	 * Show the dialog with advanced parameters
	 */
	private void showAdvanceParametersDialog(){
		if(advancedDialog == null){
			advancedDialog = new JDialog(ELANCommandFactory.getRootFrame(
					vm.getTranscription()), false);
					
			advancedDialog.setTitle(ElanLocale.getString("Recognizer.RecognizerPanel.Parameters"));			
			advancedDialog.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			advancedDialog.getContentPane().add(new JScrollPane(getAdvancedParameterPanel()), gbc);
			
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weighty = 0.0;
			JButton closeButton = new JButton(ElanLocale.getString("Button.Close"));
			closeButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					advancedDialog.setVisible(false);
					advancedDialog.dispose();
				}	
				
			});
			advancedDialog.getContentPane().add(closeButton, gbc);
			advancedDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

		advancedDialog.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle curRect = advancedDialog.getBounds();
		// add some extra width for a possible scrollbar in the scrollpane
		advancedDialog.setBounds(screen.width / 2, 10, 
				Math.min(screen.width / 2 - 20, curRect.width + 30), Math.min(screen.height - 30, curRect.height));
		advancedDialog.setVisible(true);
	}

	/**
	 * Restores previously stored values.
	 * 
	 * @param storedPrefs the (previously used) stored values
	 */
	@Override
	public void setParamPreferences(Map<String, Object> storedPrefs) {
		for (String key : storedPrefs.keySet()) {
			for (AbstractParamPanel p : panels) {
				if (p.getParamName() != null && p.getParamName().equals(key)) {
					p.setParamValue(storedPrefs.get(key));
					break;
				}
			}
		}
	}

	/**
	 * Returns the number of panels (= the number of parameters).
	 * 
	 * @return the number of panels
	 */
	public int getNumPanels() {
		return panels.size();
	}
	
	/**
	 * Returns the panel at the given index.
	 * 
	 * @param index the index 
	 * @return the panel at the index or null (in case of index out of bounds)
	 */
	public AbstractParamPanel getParamPanel(int index) {
		if (index >= 0 && index < panels.size()) {
			return panels.get(index);
		}		
		return null;
	}
}
