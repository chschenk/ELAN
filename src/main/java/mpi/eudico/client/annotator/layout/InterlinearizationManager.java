package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTAddToLexiconAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTCreateDependentAnnotationsAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTDeleteAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTInterlinearizeAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTNavigateAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTSplitAnnotationAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTStartEditAction;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerSettingsPanel;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerConfigPanel;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerConfig;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerTypeConfig;
import mpi.eudico.client.annotator.interlinear.edit.InterlinearEditor;
import mpi.eudico.client.annotator.interlinear.edit.PotentialTiers;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerLexiconHostContext;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.NeedsCreateNewVisualComponent;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import nl.mpi.lexan.analyzers.TextAnalyzerLexiconContext;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChangeListener;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChanger;
import nl.mpi.lexan.analyzers.helpers.Information;
import nl.mpi.lexan.analyzers.helpers.parameters.Parameter;
import nl.mpi.lexiconcomponent.gui.ElanLexRootPanel;
import nl.mpi.lexiconcomponent.gui.LexiconGuiResources;

public class InterlinearizationManager
	implements ModeLayoutManager, ConfigurationChangeListener {
	/**
	 * The field separator for configurations when stored in the Preferences.
	 * Note that this <em>must</em> be a string that <em>can't</em> occur 
	 * in the name of any analyzer.
	 */
	private static final String SEPARATOR = "/,/";
	
	private ViewerManager2 viewerManager;
	private ElanLayoutManager layoutManager;
	private Container container;
	// store for restoring orig values
	int origMediaAreaWidth = ElanLayoutManager.MASTER_MEDIA_WIDTH;
	int origMediaAreaHeight = ElanLayoutManager.MASTER_MEDIA_HEIGHT;
	// components
	private JSplitPane leftRightSplit;
	private JSplitPane rightTopBottomSplit;
	private JSplitPane leftTopBottomSplit;
	
	private AnalyzerSettingsPanel topLeftPanel;
	private AnalyzerConfigPanel topRightPanel;
	private InterlinearEditor interPanel;
	private ElanLexRootPanel lexiconPanel;
	
	private TextAnalyzerHostContext analyzerHostContext;
    private TextAnalyzerLexiconHostContext lexiconHostContext;
    private boolean initialLoadPerformed = false;
    
    private List<KeyStroke> ksNotToBeConsumed;  
    private List<AbstractAction> modeActions;
    // preferences constants
    private final String INTER_CONFIG_KEY = "InterlinearizationConfigurations";
    private final String INTER_CONFIG_EXCLUDED_KEY = "InterlinearizationConfigurations.ExcludedTierConfigurations";
    private final String INTER_LEXICON_PANEL_KEY = "InterlinearizationMode.LexiconPanelPrefs";
    private final String INTER_LR_SPLIT_KEY = "InterlinearizationMode.LeftRightSplit";
    private final String INTER_LTB_SPLIT_KEY = "InterlinearizationMode.LeftTopBottomSplit";
    private final String INTER_RTB_SPLIT_KEY = "InterlinearizationMode.RightTopBottomSplit";

	/**
	 * Creates an instance of the InterlinearizationManager
	 * 
	 * @param viewerManager
	 * @param elanLayoutManager
	 */
    public InterlinearizationManager(ViewerManager2 viewerManager, ElanLayoutManager elanLayoutManager) {
    	super();
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;
        
        lexiconHostContext = new TextAnalyzerLexiconHostContext(this);
        analyzerHostContext = new TextAnalyzerHostContext(this);

        container = layoutManager.getContainer();
        // This loads the config panels, but does not "load" (initialize) the analyzers.
        // That happens only later in readPreferences() when calling
        // HostContext.addConfig(ac). There can also be multiple loaded analyzers but there
        // will be only one config panel (per analyzer class).
        // So each analyzer can subscribe to configuration changes.
        topLeftPanel = new AnalyzerSettingsPanel(analyzerHostContext);
        // passes some resources and properties to the component, reconsider. 
        // Should introduce a small API, or merge with LexiconContext?
        LexiconGuiResources.setLanguageBundle(ElanLocale.getResourceBundle());
		lexiconPanel = new ElanLexRootPanel(lexiconHostContext.getLexiconFolder().getAbsolutePath(),
				lexiconHostContext.getDefaultLexiconName(), true);

        topRightPanel = new AnalyzerConfigPanel(this);
        interPanel = new InterlinearEditor(this);
        interPanel.setViewerManager(viewerManager);
        
        // temp this might needed to be done elsewhere, in the viewer manager
        viewerManager.connectViewer(interPanel, true);
        // No need to add as a addACMEditListener(): done in connectViewer.
        //viewerManager.getTranscription().addACMEditListener(interPanel);
        
        preferencesChanged();	// read preferences and set initialized = true so that we get doLayout calls
    }

    /**
     * Convenient access to the TextAnalyzerHostContext.
     */
	public TextAnalyzerHostContext getTextAnalyzerContext() {
		return analyzerHostContext; 
	}

	/**
	 * @return the TextAnalyzerLexiconHostContext
	 */
	public TextAnalyzerLexiconHostContext getTextAnalyzerLexiconContext() {
		return lexiconHostContext;
	}
	
	/**
	 * Convenient access to the transcription.
	 */
	public Transcription getTranscription() {
		return viewerManager.getTranscription();
	}
	
	/**
	 * Access to the ViewerManager2.
	 */
	public ViewerManager2 getViewerManager() {
		return viewerManager;
	}
	
	/**
	 * Temp: grant access to the editor component.
	 * 
	 * @return
	 */
	public InterlinearEditor getInterEditor() {
		return interPanel;
	}
	
	public AnalyzerConfigPanel getConfigPanel() {
		return topRightPanel;
	}
	
	/**
	 * Give access to the UI component for display of lexicons
	 * 
	 * @return the ELanLexRootPanel
	 */
	public ElanLexRootPanel getLexiconPanel() {
		return lexiconPanel;
	}
	
	@Override // ModeLayoutManager
	public void add(Object object) {
		// stub, currently not used. 
	}
	
	@Override // ModeLayoutManager
	public void remove(Object object) {
		// stub, currently not used
	}
	
	@Override // ModeLayoutManager
	public void doLayout() {
		int containerWidth 		= container.getWidth();
	    int containerHeight 	= container.getHeight();
	    
	    leftRightSplit.setBounds(0, 0, containerWidth, containerHeight);
	    leftRightSplit.validate();	
	}
	
	@Override // ModeLayoutManager
	public void updateLocale() {
		topRightPanel.updateLocale();
        // passes some resources and properties to the component, reconsider. 
		// Have a mini API, or merge with LexiconContext?
        LexiconGuiResources.setLanguageBundle(ElanLocale.getResourceBundle());
        lexiconPanel.updateLocale();
	}
	
	/**
	 * Undo initComponents() and readPreferences().
	 */
	@Override // ModeLayoutManager
	public void clearLayout() {
		// Remove/unload all configs: when we go back to this layout they will be restored.
		List<AnalyzerConfig> toRemove = new ArrayList<AnalyzerConfig>(getTextAnalyzerContext().getConfigurations());
		for (AnalyzerConfig config : toRemove) {
			getTextAnalyzerContext().removeConfig(config);
		}

        viewerManager.connectViewer(interPanel, false);
        // removeACMEditListener() should not be needed: done in connectViewer().
		viewerManager.getTranscription().removeACMEditListener(interPanel);

		// remove all interlinear components
		container.remove(leftRightSplit);
		// restore some values
		layoutManager.setMediaAreaWidth(origMediaAreaWidth);
		layoutManager.setMediaAreaHeight(origMediaAreaHeight);
		
		// add the video players
		ElanMediaPlayer masterPlayer = viewerManager.getMasterMediaPlayer();
		List<PlayerLayoutModel> players = layoutManager.getPlayerList();
		
		for (PlayerLayoutModel model : players) {
			if (model.player != masterPlayer) {
				if (model.isVisual() && model.isAttached()) {
					if (model.player instanceof NeedsCreateNewVisualComponent) {
						model.visualComponent.setVisible(true);
					} else {
						container.add(model.visualComponent);
					}
				}
			}
		}

		//container.validate(); // is not sufficient: when switching to another mode it leaves
								// remains from this mode which are not (always? depending on mode?)
								// overpainted.
		container.repaint();
	}
	
	private void storePreferences() {
		storeGUIPreferences();
		
		final Transcription transcription = viewerManager.getTranscription();

		if (analyzerHostContext != null && analyzerHostContext.getConfigurations() != null) {	
					
			List<String> confList = new ArrayList<String>();
			String config;
			Map<String, List<String>> excludedTierMap = new HashMap<String, List<String>>();
			
			//String activeConfig = null;
			//AnalyzerConfig active = interPanel.getActiveConfiguration();
			
			for (AnalyzerConfig ac : analyzerHostContext.getConfigurations()) {
				config = ac.getAnnotId().getName() + SEPARATOR + ac.getSource() + SEPARATOR;
				for (String dest : ac.getDest()) {
					config = config.concat(dest + SEPARATOR);
				}
				config = config.concat(Boolean.toString(ac.isTypeMode()));
				
				confList.add(config);
				
				if (ac.isTypeMode()) {
					List<String> excludedSourceList = new ArrayList<String>();
					
					List<? extends Tier> sourceTiers = transcription.getTiersWithLinguisticType(ac.getSource());
					for (Tier ti : sourceTiers) {
						excludedSourceList.add(ti.getName());
					}
					
					for (AnalyzerConfig tc : ((AnalyzerTypeConfig)ac).getTierConfigurations()) {
						excludedSourceList.remove(tc.getSource());
					}
					
					excludedTierMap.put(config, excludedSourceList);
				}
			}
				
			Preferences.set(INTER_CONFIG_KEY, confList, transcription, false, false);
			Preferences.set(INTER_CONFIG_EXCLUDED_KEY, excludedTierMap, transcription, false, false);
		}
		
		topLeftPanel.storePreferences();
		Map<String, Object> lexPanelPrefs = lexiconPanel.getUserPreferences();
		if (lexPanelPrefs != null) {
			Preferences.set(INTER_LEXICON_PANEL_KEY, lexPanelPrefs, transcription);
		}
	}

	@SuppressWarnings("unchecked")
	private void readPreferences() {	
		if (initialLoadPerformed) {
			// this loading has to be performed only once
			return;
		}
		final Transcription transcription = viewerManager.getTranscription();

		List<String> confObj = Preferences.getListOfString(INTER_CONFIG_KEY, transcription);		
		Map<String, ?> excludedObj = Preferences.getMap(INTER_CONFIG_EXCLUDED_KEY, transcription);		
		
		Map<String, List<String>> excludedTiersMap = new HashMap<String, List<String>>();
		
		if (excludedObj != null) {
			excludedTiersMap = (Map<String, List<String>>) excludedObj;	
		} else {
			excludedTiersMap = new HashMap<String, List<String>>();
		}
		
		List<Information> analyzers = analyzerHostContext.listTextAnalyzersInfo();

		if (confObj != null && analyzers != null) {
			List<String> storedConfigs = confObj;
			
			for (String item : storedConfigs) {
				if (item instanceof String) {
					String conf = item;
					List<String> excludedTiers = excludedTiersMap.get(conf);
					
					// "Lexicon Analyzer, word@AAA, word1, word2, false"
					String[] tokens = conf.split(SEPARATOR);
					if (tokens.length >= 4) {
						List<String> destList = new ArrayList<String>();
						Information info = null;
						String name = tokens[0];
						
						for (Information in : analyzers) {
							if (name.equals(in.getName())) {
								info = in;
								break;
							}
						}
						
						//check if the analyzer is available
						if (info == null) {
							continue;
						}
						
						String source = tokens[1];
						
						for (int i = 2; i < tokens.length - 1; i++) {
							destList.add(tokens[i]);
						}
						
						// if type config
						if (tokens[tokens.length-1].equals(Boolean.toString(true))) {
							AnalyzerTypeConfig atc = new AnalyzerTypeConfig(info, source, destList);
							
							fillWithTierConfigs(info, atc, excludedTiers);
							
							analyzerHostContext.addConfig(atc);
						} else { // Tier config
							if (transcription.getTierWithId(source) == null) {
								continue;
							}
							
							for (String dest: destList) {
								if (transcription.getTierWithId(dest) == null) {
									continue;
								}
							}
							
							AnalyzerConfig ac = new AnalyzerConfig(info, source, destList);
							analyzerHostContext.addConfig(ac);
						}
					}
				}
			}
		}
		
		Map<String, ?> lexPanelPrefs = Preferences.getMap(INTER_LEXICON_PANEL_KEY, transcription);
		if (lexPanelPrefs != null && lexiconPanel != null) {
			lexiconPanel.setUserPreferences((Map<String, Object>) lexPanelPrefs);
		}
		
		initialLoadPerformed = true;
		if (topRightPanel != null && !analyzerHostContext.getConfigurations().isEmpty()) {
			topRightPanel.configsChanged();
		}
	}

	/**
	 * Store the GUI preferences.
	 */
	private void storeGUIPreferences() {
		final Transcription transcription = viewerManager.getTranscription();

		int value = leftRightSplit.getDividerLocation();
		double size = (double) leftRightSplit.getWidth();
		Preferences.set(INTER_LR_SPLIT_KEY,
				new Double(size > 0 ? (value / size) : 0), transcription, false, false);
		
		value = leftTopBottomSplit.getDividerLocation();
		size = (double) leftTopBottomSplit.getHeight();
		Preferences.set(INTER_LTB_SPLIT_KEY,
				new Double(size > 0 ? (value / size) : 0), transcription, false, false);

		value = rightTopBottomSplit.getDividerLocation();
		size = (double) rightTopBottomSplit.getHeight();
		Preferences.set(INTER_RTB_SPLIT_KEY,
				new Double(size > 0 ? (value / size) : 0), transcription, false, false);
	}

	/**
	 * Read (and apply) the GUI preferences.
	 * 
	 * This is separate from readPreferences() because the GUI shouldn't just
	 * be changed any time there is a notification of a change in any preferences.
	 * That would only work if the prefs were kept up to date whenever the user changes
	 * the underlying GUI elements, which is wasteful.
	 * This method is called before the size of the window is restored, so regardless the resizeWeight
	 * of any of the split panes, the divider location(s) will change once the window is resized based
	 * on the preferences. 
	 * The quick fix applied here, retrieve the target window size and recalculate divider locations such
	 * that in the end they will be located correctly. 
	 */
	private void readGUIPreferences() {
		final Transcription transcription = viewerManager.getTranscription();
		Dimension d = Preferences.getDimension("FrameSize", transcription);
		Dimension curFrameSize = layoutManager.getElanFrame().getSize();
		double hScale = 1.0d;
		double vScale = 1.0d;
		if (d != null && curFrameSize != null) {
			hScale = d.width / (double) curFrameSize.width;
			vScale = d.height / (double) curFrameSize.height;
		}

		Double intObj = Preferences.getDouble(INTER_LR_SPLIT_KEY, transcription);
		if (intObj != null) {
			leftRightSplit.setDividerLocation(intObj * hScale);
		}
		
		intObj = Preferences.getDouble(INTER_LTB_SPLIT_KEY, transcription);
		if (intObj != null) {
			leftTopBottomSplit.setDividerLocation(intObj * vScale);
		}

		intObj = Preferences.getDouble(INTER_RTB_SPLIT_KEY, transcription);
		if (intObj != null) {
			rightTopBottomSplit.setDividerLocation(intObj * vScale);
		}
	}

	private Map<String, List<String>> createTierMap() {          
		Map<String, List<String>> tiersMap = new HashMap<String, List<String>>();
		
	    List<TierImpl> list = ((TranscriptionImpl)getTranscription()).getTiers();
	  	List<String> allTierNames = new ArrayList<String>(list.size());	
	    
	  	for (TierImpl tier : list) {
	  		allTierNames.add(tier.getName());

	  		List<TierImpl> childList = tier.getChildTiers();
	  		
	  		if (childList != null && !childList.isEmpty()) {
	  			List<String>  childTierNamesList = new ArrayList<String>();
	  			
	  			for (TierImpl child : childList) { 
	  				childTierNamesList.add(child.getName());	
	  			}        			
	  			
	  			tiersMap.put(tier.getName(), childTierNamesList);
	  		}
	  	}
	  	
	  	tiersMap.put("", allTierNames);
	  	
	  	if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("createTierMap: %s",
					String.valueOf(tiersMap)));
		}
	  	return tiersMap;
	}
	
	/**
	 * Given an AnalyzerTypeConfig, which expresses a source and destination in
	 * type names, find some concrete tier names that match these.
	 * <p>
	 * This version was originally (more or less) taken from what the preferences code does.
	 * <p>
	 * Uses the PotentialTiers helper class to generate fitting tiers according to the
	 * requirement of each analyzer.
	 * <p>
	 * XXX Assumes 1 source.
	 * 
	 * @param atc
	 * @param transcription
	 * @param excludedTiers
	 */
	public void fillWithTierConfigs(Information parameterInformation, AnalyzerTypeConfig atc, List<String> excludedTiers) {
		// Get all potential source tiers.
		final String sourceType = atc.getSource();
		final Transcription transcription = viewerManager.getTranscription();
		final List<? extends Tier> sourceTiers = transcription.getTiersWithLinguisticType(sourceType);
		
		if (sourceTiers == null) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("fillWithTierConfigs: source 0: no tiers available of type %s",
						sourceType));
			}
			return;
		}
		
		List<Parameter> params = parameterInformation.getParameters();
		PotentialTiers potentialTiers = new PotentialTiers(params);
		potentialTiers.setChildMap(createTierMap(), false);
		List<String> potentialSourceTiers = potentialTiers.getPotentialSourceNames(0, null);

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("fillWithTierConfigs: source 0: %s, excluded %s",
				String.valueOf(potentialSourceTiers),
				String.valueOf(excludedTiers)));
		}
		
		List<String> destTypeList = atc.getDest();
		
		for (Tier sourceTier : sourceTiers) {
			final String sourceTierName = sourceTier.getName();
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("fillWithTierConfigs: considering typed tier %s",
						sourceTierName));
			}
			if (excludedTiers != null && excludedTiers.contains(sourceTierName)) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("fillWithTierConfigs: reject tier %s: excluded",
							sourceTierName));
				}
				continue;
			}
			if (!potentialSourceTiers.contains(sourceTierName)) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("fillWithTierConfigs: reject tier %s: not in potential source tiers",
							sourceTierName));
				}
				continue;
			}
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("fillWithTierConfigs: tier %s: ok so far",
						sourceTierName));
			}
			
			//List<? extends Tier> childTiers = sourceTier.getChildTiers();
			List<String> destTierList = new ArrayList<String>();

			// Loop over the desired destination tier types and
			// for each type find a tier.
			final int numDests = destTypeList.size();
			final int numSources = 1; // ASSUMPTION!!!!
			assert(numDests == potentialTiers.getNumberOfTargetTiers());
			
			potentialTiers.setTierName(0, sourceTierName);
			for (int i = 0; i < numDests; i++) {
				potentialTiers.setTierName(numSources + i, "");
			}
			
			for (int i = 0; i < numDests; i++) {
				boolean foundTier = false;
				List<String> potentialTargets = potentialTiers.getPotentialTargetNames(numSources + i);
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("fillWithTierConfigs: dest %d: getPotentialTargetNames() => %s",
							i, String.valueOf(potentialTargets)));
				}
				
				for (String tierName : potentialTargets) {
					Tier tier = transcription.getTierWithId(tierName);
					if (tier.getLinguisticType().getLinguisticTypeName().equals(
							destTypeList.get(i))) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("fillWithTierConfigs: dest %d: tier %s is of correct type %s",
									i, tierName, destTypeList.get(i)));
						}
						
						// By using PotentialTiers, it automatically avoids 
						// using the same tier more than once as a destination.
						destTierList.add(tierName);
						potentialTiers.setTierName(numSources + i, tierName);
						foundTier = true;
						break;
					} else {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("fillWithTierConfigs: dest %d: tier %s is of wrong type %s (required %s)",
									i, tierName, tier.getLinguisticType().getLinguisticTypeName(), destTypeList.get(i)));
						}
					}
				}
				
				// If we didn't find a tier for this type, then going on with this
				// is not useful.
				if (!foundTier) {
					break;
				}
			}

			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("fillWithTierConfigs: destTierList: %s)",
						String.valueOf(destTierList)));
				LOG.finer(String.format("fillWithTierConfigs: have %d (required %d)",
						destTierList.size(), numDests));
			}
			// If we managed to find the required number of destination tiers,
			// record the whole combination.
			// Potentially skip this if configExists(ac)?
			if (destTierList.size() == numDests) {
				AnalyzerConfig ac = new AnalyzerConfig(atc.getAnnotId(), sourceTierName, destTierList);

//				if (getTextAnalyzerContext().configExists(ac)) {
//					continue;
//				}
				atc.addTierConf(ac);
			}
		}
	}
		
//	/**
//	 * Checks if the give conf is the active configuration
//	 */
//	private boolean isActiveConf(String[] tokens, AnalyzerConfig ac) {
//		if (tokens != null && tokens.length == 4) {
//			if (tokens[0].equals(ac.getAnnotId().getName()) &&
//					tokens[1].equals(ac.getSource()) &&
//					tokens[2].equals(ac.getDest()) &&
//					tokens[3].equals(Boolean.toString(ac.isTypeMode()))) {
//				return true;
//			}
//		}		
//		return false;
//	}
	
	@Override // ModeLayoutManager
	public void initComponents() {
		origMediaAreaWidth = layoutManager.getMediaAreaWidth();
		origMediaAreaHeight = layoutManager.getMediaAreaHeight();

		// remove remaining video player components
		List<PlayerLayoutModel> players = layoutManager.getPlayerList();
		ElanMediaPlayer masterPlayer = viewerManager.getMasterMediaPlayer();
		
		for (PlayerLayoutModel model : players) {
			if (model.isVisual() && model.isAttached()) {
				if (model.player != masterPlayer) {
					if (model.player instanceof NeedsCreateNewVisualComponent) {
						// This is a workaround. The JavaQTMediaPlayer crashes if it
						// gets detached because its NSView (or something)
						// disappears but nobody knows it, leading to
						// NullPointerException as soon as (for instance) the volume
						// is set.
						model.visualComponent.setVisible(false);
					} else {
						// Only remove the ones that are resistant to adding/removing.
						container.remove(model.visualComponent);
					}
				} else {
					model.visualComponent.setBounds(0, 0, 1, 1);
				}
			}
		}
		
		// add mode specific components
//    	
		//  +------------------+----------------------+
		//  |                  |                      |
		//  |                  |                      |
		//  |  topLeftPanel    |       topRightPanel  |
		//  |                  |                      |
		//  +------------------+                      |
		//  |                  +----------------------+
		//  |                  |                      |
		//  |                  |                      |
		//  |                  |       interPanel     |
		//  | lexiconPanel     |                      |
		//  |                  |                      |
		//  +------------------+----------------------+
		
		
		leftTopBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// The default behavior (resizeWeight = 0) is actually the best for this layout
		// extra space is allotted to bottom and right components. 
		// See also readGUIPreferences concerning restoring last used sizes.
		//leftTopBottomSplit.setResizeWeight(0.5);
		leftTopBottomSplit.setOneTouchExpandable(true);

        leftTopBottomSplit.setTopComponent(topLeftPanel);
        leftTopBottomSplit.setBottomComponent(lexiconPanel);

        //topLeftPanel.setBackground(new Color(220, 220, 255));

		rightTopBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//rightTopBottomSplit.setResizeWeight(0.5);
		rightTopBottomSplit.setOneTouchExpandable(true);
		
        rightTopBottomSplit.setTopComponent(topRightPanel);
        rightTopBottomSplit.setBottomComponent(interPanel);
        
		leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//leftRightSplit.setResizeWeight(0.5);
		leftRightSplit.setOneTouchExpandable(true);
        
		leftRightSplit.setLeftComponent(leftTopBottomSplit);
		leftRightSplit.setRightComponent(rightTopBottomSplit);		

		container.add(leftRightSplit);

        doLayout();
        readGUIPreferences();
	}

	@Override // ModeLayoutManager
	public void enableOrDisableMenus(boolean enabled) {
		// deal with actions and menu's not active in this mode
		// if the ActiveAnnotation mechanism is not used, if the InterlinearEditor does not implement updateActiveAnnotation 
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.ANNOTATION, enabled);
		// View menu
		List<String> viewSubMenus = new ArrayList<String>(3);
		viewSubMenus.add(ElanLocale.getString(ELANCommandFactory.MEDIA_PLAYERS));
		viewSubMenus.add(ElanLocale.getString(ELANCommandFactory.WAVEFORMS));
		viewSubMenus.add(ElanLocale.getString(ELANCommandFactory.VIEWERS));
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(viewSubMenus, FrameConstants.VIEW, enabled);
		// Options menu
		List<String> optionsSubMenus = new ArrayList<String>(3);
		optionsSubMenus.add(ElanLocale.getString(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG));
		optionsSubMenus.add(ElanLocale.getString(ELANCommandFactory.PLAYBACK_TOGGLE_DLG));
		optionsSubMenus.add(ElanLocale.getString("Menu.Options.FrameLength"));
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(optionsSubMenus, FrameConstants.OPTION, enabled);
		// Search menu, if the InterlinearEditor does not implement controllerUpdate and/or updateActiveAnnotation
		List<String> seachSubMenus = new ArrayList<String>(2);
		seachSubMenus.add(ElanLocale.getString(ELANCommandFactory.GOTO_DLG));
		//seachSubMenus.add(ElanLocale.getString(ELANCommandFactory.SEARCH_DLG));
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(seachSubMenus, FrameConstants.SEARCH, enabled);
		// Edit menu
		List<String> editSubMenus = new ArrayList<String>(2);
		editSubMenus.add(ElanLocale.getString(ELANCommandFactory.COPY_CURRENT_TIME));
		editSubMenus.add(ElanLocale.getString(ELANCommandFactory.LINKED_FILES_DLG));
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(editSubMenus, FrameConstants.EDIT, enabled);
		// File menu
//		List<String> fileSubMenus = new ArrayList<String>(2);
		// The following doesn't work unless that action has been disconnected as selection listener. 
		// Not really problematic. Could consider to update the Selection if one (or more?) rows are selected in the IGT viewer/editor
//		fileSubMenus.add(ElanLocale.getString(ELANCommandFactory.SAVE_SELECTION_AS_EAF));
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(fileSubMenus, FrameConstants.FILE, enabled);
		// disable / enable some actions		
		enableAction(ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), 
				ELANCommandFactory.PREVIOUS_ANNOTATION), enabled);
		enableAction(ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), 
				ELANCommandFactory.NEXT_ANNOTATION), enabled);
		enableAction(ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), 
				ELANCommandFactory.ANNOTATION_UP), enabled);
		enableAction(ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), 
				ELANCommandFactory.ANNOTATION_DOWN), enabled);
		enableAction(ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), 
				ELANCommandFactory.ACTIVE_ANNOTATION_EDIT), enabled);
		
		// mode specific actions and key strokes will be added in shortcutsChanged() 
		// and removed in ElanFrame when switching to a different mode
	}
	
	private void enableAction (Action action, boolean enable) {
		if (action != null) {
			action.setEnabled(enable);
		}
	}

	@Override // ModeLayoutManager
	public void detach(Object object) {
		// stub, this is a mode without media players and the viewer(s) cannot yet be detached.
	}
	
	@Override // ModeLayoutManager
	public void attach(Object object) {
		// stub, this is a mode without media players that can be detached
		
	}

	@Override // ModeLayoutManager
	public void preferencesChanged() {
		readPreferences();
	}
	
	@Override // ModeLayoutManager
	public void cleanUpOnClose() {
		// temporary
		clearLayout();
		// maybe the lexicon panel needs to clean up anything?
	}

	/**
	 * When shortcuts are changed or when changing the working mode in ElanFrame2 the root pane's
	 * action map and input map are cleared. So the mode specific actions have to be added (again) here. 
	 */
	@Override // ModeLayoutManager
	public void shortcutsChanged() {
		ksNotToBeConsumed = new ArrayList<KeyStroke>(10);
		// create/add mode specific actions
		addModeActions();
		// update the input map with keyboard shortcuts
		JRootPane rootPane = SwingUtilities.getRootPane(interPanel);
		InputMap imap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ShortcutsUtil su = ShortcutsUtil.getInstance();
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.PREVIOUS_ANNOTATION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.PREVIOUS_ANNOTATION);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.NEXT_ANNOTATION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.NEXT_ANNOTATION);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.ANNOTATION_UP, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.ANNOTATION_UP);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.ANNOTATION_DOWN, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.ANNOTATION_DOWN);
		
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.DELETE_ANNOTATION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.DELETE_ANNOTATION);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.CREATE_DEPEND_ANN, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.CREATE_DEPEND_ANN);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.SPLIT_ANNOTATION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.SPLIT_ANNOTATION);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.NEW_ANNOTATION_BEFORE, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.NEW_ANNOTATION_BEFORE);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.NEW_ANNOTATION_AFTER, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.NEW_ANNOTATION_AFTER);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.ANALYZE_ANNOTATION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.ANALYZE_ANNOTATION);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.ADD_TO_LEXICON, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.ADD_TO_LEXICON);
		imap.put(su.getKeyStrokeForAction(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT, 
				ELANCommandFactory.INTERLINEARIZATION_MODE), ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
		
		// special treatment of keystrokes and actions that are not in the menu but need to be invoked
		// even when the interlinear table has focus
		KeyStroke ks = null;
		ks = su.getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, 
				ELANCommandFactory.INTERLINEARIZATION_MODE);
		if (ks != null) {
			ksNotToBeConsumed.add(ks);
		}
		// more
		interPanel.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
	}
	
	/**
	 * Creates (if needed) and adds the mode specific actions to the action map.
	 * @see #shortcutsChanged() in case keyboard shortcuts need to be added as well
	 */
	private void addModeActions() {
		if (modeActions == null) {
			modeActions = new ArrayList<AbstractAction>(20);
			
			modeActions.add(new IGTNavigateAction(interPanel, 
					ELANCommandFactory.PREVIOUS_ANNOTATION, IGTNavigateAction.Direction.LEFT));			
			modeActions.add(new IGTNavigateAction(interPanel, 
					ELANCommandFactory.NEXT_ANNOTATION, IGTNavigateAction.Direction.RIGHT));		
			modeActions.add(new IGTNavigateAction(interPanel, 
					ELANCommandFactory.ANNOTATION_UP, IGTNavigateAction.Direction.UP));			
			modeActions.add(new IGTNavigateAction(interPanel, 
					ELANCommandFactory.ANNOTATION_DOWN, IGTNavigateAction.Direction.DOWN));			
			// edit action that are also in a context menu
			modeActions.add(new IGTDeleteAction(null, analyzerHostContext, 
					ELANCommandFactory.DELETE_ANNOTATION));
			modeActions.add(new IGTCreateDependentAnnotationsAction(null, analyzerHostContext, 
					ELANCommandFactory.CREATE_DEPEND_ANN));
			modeActions.add(new IGTSplitAnnotationAction(null, analyzerHostContext, 
					IGTSplitAnnotationAction.SPLIT, ELANCommandFactory.SPLIT_ANNOTATION));
			modeActions.add(new IGTSplitAnnotationAction(null, analyzerHostContext, 
					IGTSplitAnnotationAction.NEW_BEFORE, ELANCommandFactory.NEW_ANNOTATION_BEFORE));
			modeActions.add(new IGTSplitAnnotationAction(null, analyzerHostContext, 
					IGTSplitAnnotationAction.NEW_AFTER, ELANCommandFactory.NEW_ANNOTATION_AFTER));
			modeActions.add(new IGTInterlinearizeAction(null, analyzerHostContext, 
					ELANCommandFactory.ANALYZE_ANNOTATION));
			modeActions.add(new IGTAddToLexiconAction(null, lexiconHostContext,
					ELANCommandFactory.ADD_TO_LEXICON));
			modeActions.add(new IGTStartEditAction(interPanel, 
					ELANCommandFactory.ACTIVE_ANNOTATION_EDIT));
		}
		ActionMap amap = SwingUtilities.getRootPane(interPanel).getActionMap();
		
		for (AbstractAction aa : modeActions) {
			amap.put(aa.getValue(Action.NAME), aa);
		}		
	}

	@Override // ModeLayoutManager
	public void createAndAddViewer(String viewerName) {
		// stub, not (yet) used by this mode
		
	}

	@Override // ModeLayoutManager
	public boolean destroyAndRemoveViewer(String viewerName) {
		// stub, not (yet) used by this mode
		return false;
	}

	@Override // ModeLayoutManager
	public void isClosing() {
		storePreferences();
		interPanel.isClosing();
		lexiconPanel.isClosing();
	}

	@Override // ModeLayoutManager
	public List<Zoomable> getZoomableViewers() {
		List<Zoomable> zoomList = new ArrayList<Zoomable>(1);
		if (interPanel != null && interPanel.getZoomable() != null) {
			zoomList.add(interPanel.getZoomable());
		}
		return zoomList;
	}

	/**
	 * Keep the lexicon panel informed about settings changes.
	 */
	@Override // ConfigurationChangeListener
	public void configurationChanged(ConfigurationChanger panel) {

	}

} 
