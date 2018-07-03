package mpi.eudico.client.annotator.interlinear.edit;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.awt.Component;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.commands.AnnotationsFromSuggestionSetCommand;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerConfig;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerTypeConfig;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionListener;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetListener;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetProvider;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.util.Pair;
import nl.mpi.lexan.analyzers.LexiconTextAnalyzer;
import nl.mpi.lexan.analyzers.TextAnalyzer;
import nl.mpi.lexan.analyzers.TextAnalyzerContext;
import nl.mpi.lexan.analyzers.helpers.Information;
import nl.mpi.lexan.analyzers.helpers.LexanSuggestionSelectionListener;
import nl.mpi.lexan.analyzers.helpers.Position;
import nl.mpi.lexan.analyzers.helpers.PositionLexicon;
import nl.mpi.lexan.analyzers.helpers.SourceTargetConfiguration;
import nl.mpi.lexan.analyzers.helpers.Suggestion;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;
import nl.mpi.lexan.analyzers.helpers.prompt.Prompt;

/**
 * An Analyzer Host Context, the context for TextAnalyzers to communicate with.
 * 
 * This is to some extend a combination of old Mediator and Controller functionality.
 */
public class TextAnalyzerHostContext 
	implements TextAnalyzerContext, SuggestionSelectionListener, 
				SuggestionSetProvider {
	private File analyzerCacheFolder = new File(Constants.ELAN_DATA_DIR, Constants.ANALYZER_CACHE_FOLDER_NAME);
	private InterlinearizationManager manager;
	private Transcription transcription;
	private List<SuggestionSetListener> suggestionListeners;
	/**
	 * Let interested TextAnalyzers know about what suggestion was selected by the user.
	 */
	private List<LexanSuggestionSelectionListener> suggestionSelectionListeners;
	
	// administration of loaded analyzers
	private List<TextAnalyzer> loadedAnalyzers;// analyzers for actual use with source and target set
	private Map<Information, Component> info2configComp;
	private Map<String, List<TextAnalyzer>> sourceAnalyzerMap;
	private List<AnalyzerConfig> configs;
	private boolean recursive = false;

	private boolean sugWindowOpened = false;
	private boolean autoAnalyzeMode = false;
	private Deque<Pair<TextAnalyzer, Position>> positionQueue;
	
	/**
	 * Constructor
	 * @param manager the manager tying components together
	 */
	public TextAnalyzerHostContext(InterlinearizationManager manager) {
		super();
		this.manager = manager;
		this.transcription =  manager.getTranscription();
		loadedAnalyzers = new ArrayList<TextAnalyzer>();
		info2configComp = new HashMap<Information, Component>();
		configs = new ArrayList<AnalyzerConfig>(6);
		suggestionListeners = new ArrayList<SuggestionSetListener>(4);
		sourceAnalyzerMap = new HashMap<String, List<TextAnalyzer>>();
		positionQueue = new ArrayDeque<Pair<TextAnalyzer, Position>>();
		ensureCacheFolder();
	}
	
	/**
	 * Checks if the cache folder exists and creates it if not.
	 */
	public void ensureCacheFolder() {
		if (analyzerCacheFolder == null) {
			analyzerCacheFolder = new File(Constants.ELAN_DATA_DIR, 
					Constants.ANALYZER_CACHE_FOLDER_NAME);
		}
		
		try {
			if (!analyzerCacheFolder.exists()) {
				try {
					analyzerCacheFolder.mkdir();
				} catch (Throwable t) {// any
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning(String.format("Cannot create the cache folder for analyzers: %s", 
								t.getMessage()));
					}
					return;
				}
			}
		} catch (Throwable thr) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning(String.format("Cannot check the existence of the analyzers cache folder: %s", 
						thr.getMessage()));
			}
		}
	}
	
	/**
	 * @return the Transcription of this context
	 */
	public Transcription getTranscription() {
		return transcription;
	}
	
	/**
	 * Adds a configuration to the list of tier-analyzer configurations.
	 * <p>
	 * This gets called from the preferences interpreting code.
	 * This also means that it gets called again when preferences change.<br/>
	 * This is nasty.<br/>
	 * To counter this, perform a check for duplications (and disallow them).
	 *  
	 * @param config
	 */
	public void addConfig(AnalyzerConfig config) {
		if (!configs.contains(config)) {
			configs.add(config);
			
			List<SourceTargetConfiguration> sourceTargetConfList = convertToSTConfList(config);

			TextAnalyzer ta = TextAnalyzerRegistry.getInstance().getAnalyzerForDoc(
					transcription.getURN().toString(), config.getAnnotId());
			boolean success = false;
			
			if (ta != null) {
				if (loadedAnalyzers.contains(ta)) {
					// Add to the already loaded configuration.
					// (Alternatively: do ta.unload() and ta.load() with ALL configs on `configs'.)
					success = ta.partLoad(sourceTargetConfList);
				} else {
					// temp call loadAnalyzer for now. Has to be decided how best to init / load analyzers
					// loadAnalyzer could return a boolean instead
					success = (ta == loadAnalyzer(config.getAnnotId(), sourceTargetConfList));
				}
			}
			
			if (success) {
				for (SourceTargetConfiguration stc : sourceTargetConfList) {
					List<TextAnalyzer> la = sourceAnalyzerMap.get(stc.getSource().getTierId());
					if (la == null) {
						la = new ArrayList<TextAnalyzer>();
						sourceAnalyzerMap.put(stc.getSource().getTierId(), la);
					}
					la.add(ta);
				}
			}
			
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Initialized annotation analyzer '%s': %s\n", 
						config.getAnnotId().getName(), success));
			}

			// notify the editor
			manager.getInterEditor().configsChanged();
		}
	}
	
	/**
	 * Creates a Position or a PositionLexicon object for the specified tier.
	 * 
	 * @param t the tier to create a Position object for
	 * @return a {@link Position} or {@link PositionLexicon} object
	 */
	private Position tierToPosition (Tier tier) {
		if (tier == null) {
			return null;
		}
		Position pos = null;
		
		LinguisticType sourceType = tier.getLinguisticType();
		// the query bundle, if not null, contains the name of the lexicon and of the entry field 
		LexiconQueryBundle2 lexBundle = sourceType.getLexiconQueryBundle();
		if (lexBundle != null) {
			// should we check if LexiconLink and LexiconIdentification are not null?
			String lexiconName = lexBundle.getLink().getLexId().getName();
			String field = lexBundle.getFldId().getName();
			if (lexiconName != null && field != null) {
				pos = new PositionLexicon(tier.getName(), lexiconName, field);
			} else {
				pos = new Position(tier.getName());
			}
		} else {
			pos = new Position(tier.getName());
		}
		
		return pos;
	}
	
	/**
	 * Converts a tier based configuration (one source tier, one or more target tiers)
	 * to a LEXAN API source-target configuration object.
	 * 
	 * @param ac the configurations to convert
	 * @return a SourceTargetConfiguration object or null
	 */
	private SourceTargetConfiguration convertToSTConfiguration(AnalyzerConfig ac) {
		if (ac == null || ac instanceof AnalyzerTypeConfig) {
			return null;
		}
		// one source, one or more destination objects
		Tier sourceTier = transcription.getTierWithId(ac.getSource());
		if (sourceTier == null) {
			// add logging
			return null;
		}
		Position sourcePos = tierToPosition(sourceTier);
		if (sourcePos == null) {
			return null;
		}
		List<Position> targetPosList = new ArrayList<Position>(2);
		for(String targetName : ac.getDest()) {
			Tier targetTier = transcription.getTierWithId(targetName);
			Position targetPos = tierToPosition(targetTier);
			if (targetPos != null) {
				targetPosList.add(targetPos);
			}
		}
		// must be at least one target tier, at the moment
		if (targetPosList.isEmpty()) {
			return null;
		}
		
		SourceTargetConfiguration stConfig = new SourceTargetConfiguration(sourcePos, targetPosList);
		stConfig.setDocumentId(transcription.getURN().toString());
		
		return stConfig;
	}
	
	/**
	 * Converts an analyzer configuration object to a list of LEXAN API source-target 
	 * configuration objects. 
	 * 
	 * @param ac a tier-based or a type-based configuration. In the latter case there 
	 * might be more than one source tiers and the returned list's size might be > 1.
	 * 
	 * @return a list of source-target configuration objects (or null if the input is null)
	 */
	private List<SourceTargetConfiguration> convertToSTConfList(AnalyzerConfig ac) {
		if (ac == null) {
			return null;
		}
		List<SourceTargetConfiguration> sourceTargetList = new ArrayList<SourceTargetConfiguration>();
		List<AnalyzerConfig> acTierConfList = ac.getTierConfigurations();
		
		for (AnalyzerConfig anaCon : acTierConfList) {
			SourceTargetConfiguration stc = convertToSTConfiguration(anaCon);
			if (stc != null) {
				sourceTargetList.add(stc);
			}
		}
		
		return sourceTargetList;
	}
	
	/**
	 * Removes a configuration from an analyzer. This can currently not be used in case of 
	 * removal of a tier.
	 * 
	 * @param config the configuration to remove
	 */
	public void removeConfig(AnalyzerConfig config) {
		if (configs.remove(config)) {
			
			List<SourceTargetConfiguration> sourceTargetConfList = convertToSTConfList(config);
			if (sourceTargetConfList.isEmpty()) {
				// log
				return;
			}

			unloadAnalyzer(config.getAnnotId(), sourceTargetConfList);
			
			for (SourceTargetConfiguration stc : sourceTargetConfList) {
				List<TextAnalyzer> la = sourceAnalyzerMap.remove(stc.getSource().getTierId());
				
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine(String.format("Removed %d analyzer(s) for source %s", 
							(la == null ? 0 : la.size()), stc.getSource().getTierId()));
				}
			}
		}
	}
	
    /**
	 * Gives access to the list of configurations
	 * @return
	 */
	public List<AnalyzerConfig> getConfigurations() {
		return configs;
	}
	
	/**
	 * The list of available text analyzers as discovered in the extensions location(s).
	 * 
	 * @return a list of analyzers information objects 
	 */
	public List<Information> listTextAnalyzersInfo() {
		return TextAnalyzerRegistry.getInstance().getAnalyzersInfo();
	}
	
	/**
	 * Returns whether a tier is configured as source tier for any analyzer
	 * 
	 * @param tierName the name of the tier
	 * @return true if there is at least one configuration with the specified tier
	 * (or its type) as source, false otherwise 
	 */
	public boolean isAnalyzerSource(String tierName) {
		if (tierName != null) {
			Tier t = transcription.getTierWithId(tierName);
			String typeName = t.getLinguisticType().getLinguisticTypeName();
			 for (AnalyzerConfig ac : configs) {
				 if (ac.getSource().equals(typeName)) {
					 return true;
				 }
			 }
		}
		return false;
	}
	
	/**
	 * Load an analyzer (used from addConfig(AnalyzerConfig)).
	 * 
	 * @param info descriptor for the analyzer
	 * @param stConfigs the source-target configurations which include lexicon information if applicable
	 * 
	 * @return the analyzer instance if loaded successfully
	 */
    private TextAnalyzer loadAnalyzer(Information info, List<SourceTargetConfiguration> stConfigs) {  	
        if (stConfigs == null || stConfigs.isEmpty()) {
        	// log error throw exception?
        	return null;
        }

        TextAnalyzer ta = TextAnalyzerRegistry.getInstance().getAnalyzerForDoc(
        		transcription.getURN().toString(), info);
        
        if (ta != null) {
        	prepareAnalyzer(ta);
        	if (ta.load(stConfigs)) {	
        		loadedAnalyzers.add(ta);
        		
        		return ta;
        	}       	
        }
    	return null;
    }
    
    
    /**
     * Unloads an analyzer, which means it is disconnected from the source(s) and target(s).
     * @param info the information object of the analyzer
     * @param source the source tiers
     * @param target the target tiers
     */
    public void unloadAnalyzer(Information info, List<SourceTargetConfiguration> stConfigs) {
        TextAnalyzer toUnLoad = null;
        for (TextAnalyzer a : loadedAnalyzers) {
        	// the Information of the analyzer is a different instance, so cannot test with ==
            if (info.equals(a.getInformation())) {
            	toUnLoad = a;
            	break;
            }
        }
        
        if (toUnLoad == null) {
        	return;
        }
        
        // Check if we want to completely unload or partially unload??
        if (toUnLoad.partUnload(stConfigs) == 0) {
            toUnLoad.unload();
            loadedAnalyzers.remove(toUnLoad);//??
            toUnLoad.setAnalyzerContext(null);
    		if (toUnLoad instanceof LexiconTextAnalyzer) {
    			((LexiconTextAnalyzer) toUnLoad).setLexiconContext(null);

    		}

            TextAnalyzerRegistry.getInstance().removeAnalyzerForDoc(
            		transcription.getURN().toString(), info);
            return;
        }
        // Just partially unloaded
    }
	
    @Override
    public Component getConfigurationComponent(Information info, boolean loadIt) {
    	if (info2configComp.containsKey(info)) { // also succeeds when value == null
    		return info2configComp.get(info);
    	}
    	
    	for (TextAnalyzer a: loadedAnalyzers) {
            if (info == a.getInformation()) { 
            	// Some analyzers need that setCacheFolder() has been called on them.
            	// Similarly for setLexiconContext().
            	Component c = a.getConfigurationComponent();
            	info2configComp.put(info, c);
            	return c;
            }
        }

    	if (loadIt) {
	    	// load the analyzer and get the component? Not sure when this can happen
	    	TextAnalyzer ta = TextAnalyzerRegistry.getInstance().getAnalyzerForDoc(
	    			transcription.getURN().toString(), info);
			if (ta != null) {
				// Add the analyzer to the list of loaded analyzers??
				// Maybe if we can load() it without a configuration, which isn't the case (yet?).
				prepareAnalyzer(ta);
				Component c = ta.getConfigurationComponent();
				if (c != null) {
					info2configComp.put(info, c);
				}
				
				return c;
			}
    	}
    	
    	return null;
    }

	/**
	 * Prepare an analyzer for use by giving it some context information it needs for its work,
	 * and which the configuration component needs.
	 * <p>
	 * Implementation note: used both for loadAnalyzer() and getConfigurationComponent().
	 * For the latter, the analyzer itself is no longer needed, and should not be prevented from
	 * being garbage-collected by what we do here.
	 * On the other hand it may be a cached instance, so it should not explicitly be
	 * deconfigured.
	 */
	private void prepareAnalyzer(TextAnalyzer ta) {
    	ta.setAnalyzerContext(this);
		ta.setCacheFolder(analyzerCacheFolder);
		if (ta instanceof LexiconTextAnalyzer) {
			((LexiconTextAnalyzer) ta).setLexiconContext(manager.getTextAnalyzerLexiconContext());

		}
	}
    
	/**
	 * Indicates whether an analyzer process, including suggestion selection is ongoing
	 * 
	 * @return true if a suggestion window is open or if there are still positions in the queue,
	 * false otherwise
	 */
    public boolean isPositionPending() {
    	return sugWindowOpened || !positionQueue.isEmpty();
    }
    
    /**
     * 
     * @param autoAnalyzeMode if true the autoAnalyzeMode is activated,
     * otherwise it is de-activated, an ongoing suggestion is canceled
     */
    public void setAutoAnalyzeMode(boolean autoAnalyzeMode) {
    	if (this.autoAnalyzeMode == autoAnalyzeMode) {
    		return;
    	}
    	
    	if (autoAnalyzeMode) {
    		// cancel an ongoing, visible suggestion set
            notifyCancelSuggestionSet();
            positionQueue.clear();
            sugWindowOpened = false;
    	}
    	
    	this.autoAnalyzeMode = autoAnalyzeMode;
    }
    
    /**
     * @return the flag indicating if the automatic, sequential analyzing
     * mode is active
     */
    public boolean isAutoAnalyzeMode() {
    	return autoAnalyzeMode;
    }
    
    /**
     * Sets the flag for recursive analyzer calls.
     * @param selected if true, newly created annotations by an analyzer will 
     * immediately be analyzed by the next (if configured so)
     */
	public void setRecursive(boolean selected) {
		recursive = selected;
	}
    
    /**
     * Call all analyzers that have the specified position as a source.
     * If there is an ongoing analysis/suggestion , it will be canceled.
     * The combinations of analyzers and the specified position (in most cases
     * there will probably only be one analyzer) are added to the queue first
     * and then the process (re-)starts with the head of the queue. 
     * 
     * 
     * The analyzers will call back to our methods
     * {@link #newAnnotation(Position, List)},
     * {@link #newAnnotation(Position, Suggestion)},
     * and/or
     * {@link #newAnnotations(List)}.
     *  
     * @param pos the position indicating which annotation to analyze
     */
	public void analyze(Position pos) {
		if (sugWindowOpened || !positionQueue.isEmpty()) {
			// cancel ongoing analysis / suggestions
			notifyCancelSuggestionSet();
			positionQueue.clear();
		}
        // add to queue
		List<TextAnalyzer> relAnalyzers = sourceAnalyzerMap.get(pos.getTierId());
		if (relAnalyzers != null) {
			for (TextAnalyzer a : relAnalyzers) {
	            positionQueue.addFirst(new Pair<TextAnalyzer, Position>(a, pos));
	        }
		}
		// start queue
		analyzeNext();
	}
	
	/** 
	 * Called internally in case of recursive analyzer calls, where the new annotations
	 * created by one analyzer are the source or input for the next analyzer.
	 * The positions will be added to the LIFO queue (stack) in reversed order 
	 * (so that they will be processed in natural order).
	 * 
	 * @param sourcePositions  the (annotation) positions to analyze. In the list
	 * in 'natural' order, 'left-to-right', smallest start times first
	 */
	private void addPositionsToQueue(List<Position> sourcePositions) {

		if (!sourcePositions.isEmpty()) {
			String sourceId = sourcePositions.get(0).getTierId();
			
			List<TextAnalyzer> relAnalyzers = sourceAnalyzerMap.get(sourceId);
			if (relAnalyzers != null) {
				// add the positions in reversed order to the stack so that they will be processed
				// 'left-to-right' 
				for (int i = sourcePositions.size() - 1; i >= 0; i--) {
					Position pos = sourcePositions.get(i);
					for (TextAnalyzer ta : relAnalyzers) {
						//ta.analyze(pos);
						positionQueue.addFirst(new Pair<TextAnalyzer, Position>(ta, pos));
					}
				}
			}
		}
	}
	
	/**
	 * Checks if there are still pending annotations to be analyzed and if so
	 * takes the first pair of the (LIFO) queue and calls the analyzer. 
	 */
	private void analyzeNext() {
		if (!positionQueue.isEmpty()) {
			Pair<TextAnalyzer, Position> np = positionQueue.peekFirst();
			np.getFirst().analyze(np.getSecond());
		}
	}
	
	@Override
	public boolean changeAnnotation(Position pos, Suggestion ann) {
		if (pos == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Change annotation: Position is null");
			}
			return false;
		}
		if (ann == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Change annotation: no annotation specified");
			}
			return false;
		}
		
		boolean changed = false;
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		
		if (destTier != null) {
			long t = (pos.getBeginTime() + pos.getEndTime()) / 2;
			AbstractAnnotation aa = (AbstractAnnotation) destTier.getAnnotationAtTime(t);
			if (aa != null) {
				String oldText = aa.getValue();
				String newText = ann.getContent();
				if (newText == null) {
					newText = "";
				}
				Command command = ELANCommandFactory.createCommand(transcription,
						ELANCommandFactory.MODIFY_ANNOTATION);
				command.execute(aa, new Object[] { oldText, newText } );
				changed = true;
			} else {
				// warn
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Change annotation: there is no annotation at the specified position");
				}
			}
		}
		
		return changed;
	}

	/**
	 * Creates one new annotation at the specified position.
	 * In case of a subdivision tier the annotation will be added after the last annotation
	 * in the interval (if it exists).
	 * <p>
	 * This is non-interactive, and so recursive analyze() calls will be performed.
	 */
	@Override
	public boolean newAnnotation(Position pos, Suggestion ann) {
		if (ann == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "New annotation: no new annotation specified");
			}
			return false;
		}

		List<Suggestion> list = new ArrayList<Suggestion>(1);
		list.add(ann);
		return newAnnotation(pos, list);
	}
	
	/**
	 * Creates multiple new annotations at the specified position.
	 * In case of a subdivision tier the annotations will be added after the last annotation
	 * in the interval (if it exists).
	 * <p>
	 * This is non-interactive, and so recursive analyze() calls will be performed.
	 */
	@Override
	public boolean newAnnotation(Position pos, List<Suggestion> annotations) {
		if (pos == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "New annotation: Position is null");
			}
			return false;
		}
		if (annotations == null || annotations.isEmpty()) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "New annotation: no new annotations specified");
			}
			return false;
		}
		
		boolean created = false;
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		
		if (destTier != null) {
			Constraint cons = destTier.getLinguisticType().getConstraints();
			if (cons == null) {
				// warn, illegal tier type for destination?
				return false;
			}
			
			SuggestionSet sugSet = new SuggestionSet(pos);
			sugSet.addAll(annotations);
			
			// this position has been analyzed, remove from the queue
			// the source position is not part of the returned suggestions in this case, 
			// only the target position
			removePositionFromQueue(sugSet);
			notifyLexanSuggestionSelected(0, sugSet);
			
			// Create annotations via a AnnotationsFromSuggestionSetCommand,
			// so the user can undo/redo it.
			annotationsFromSuggestionSet(sugSet);
			
			analyzeNext();
		}
		
		return created;
	}

	/**
	 * Create annotations via a AnnotationsFromSuggestionSetCommand, so the user can undo/redo it.
	 * <p>
	 * Also do recursive analyzing, if enabled.
	 * <p>
	 * Implementation note: when the `subscribing' to tiers actually works (currently
	 * it is unimplemented!), analyzers that
	 * subscribe to their sources will be recursively invoked via that mechanism,
	 * and recursion here will be unneeded.
	 * 
	 * @param sugSet the set of suggestions to turn into annotations
	 */
	@SuppressWarnings("unchecked")
	private void annotationsFromSuggestionSet(SuggestionSet sugSet) {
		AnnotationsFromSuggestionSetCommand command = 
				(AnnotationsFromSuggestionSetCommand) ELANCommandFactory.createCommand(
						transcription, ELANCommandFactory.ANNS_FROM_SUGGESTION_SET);
		command.execute(transcription, new Object[]{ sugSet, Boolean.valueOf(recursive) });
		
		if (recursive) {
			List<AbstractAnnotation> recurses = (List<AbstractAnnotation>) command.getCreatedAnnotations();
			
			if (recurses != null) {
				List<Position> posList = new ArrayList<Position>(recurses.size());
				
				for (AbstractAnnotation ann : recurses) {
					Position np = new Position(ann.getTier().getName(), 
							ann.getBeginTimeBoundary(), ann.getEndTimeBoundary());
					posList.add(np);
				}
				addPositionsToQueue(posList);
			}
		}
	}

    /**
     * If a list of alternative suggestions is produced by one of the extensions, notify listeners.
     * For now this mechanism is used to push the suggestions to the user interface layer.
     * <p>
     * The first call (after {@link #interLinearizeActionStarting()})
     * passes the suggestions on via {@link #notifySuggestionSetDelivered(List)}
     * which likely causes a window to be opened for the user (eventually, when the GUI
     * gets to run). This strategy may hope to
     * <p>
     * On subsequent calls the sugSets are collected for display to the user later. 
     * 
     * @param sugSets the list of suggestion sets. Each set represents a list of possible 
     * values from which the user can pick one. They are put into a window and the user
     * can click.
     * <p>
     * Child suggestions are processed too, but recursive annotation seems not to be done
     * (depending on the SuggestionSetListener). This is because our own 
     * {@link #suggestionSelected(SuggestionSelectionEvent)}
     * does not use our own {@link #newAnnotation(Position, Suggestion) or
     * {@link #newAnnotation(Position, List)}
     * methods.
     * <p>
     * That does make some sense though, since generating suggestion pop-ups from suggestions
     * may be confusing (and current GUI code would not handle it).
     */
	@Override // TextAnalyzerContext
	public boolean newAnnotations(List<SuggestionSet> sugSets) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, String.format("newAnnotations(): the Positions queue has %d items\n",
				positionQueue.size()));
		}
		if (sugSets != null) {
			if (sugSets.size() == 1) {
				// don't notify the listeners, which would open a suggestion 
				// selection window, but apply the single set
				SuggestionSet sugSet = sugSets.get(0);
				// this position has been analyzed, remove from the queue
				removePositionFromQueue(sugSet);
				notifyLexanSuggestionSelected(0, sugSet);
				
				// create annotations via a AnnotationsFromSuggestionSetCommand
				// Apply the suggestions, and analyze recursively if enabled.
				annotationsFromSuggestionSet(sugSet);		
				
				analyzeNext();
				return true;
			} else {
				// show the suggestions
				notifySuggestionSetDelivered(sugSets);
				sugWindowOpened = true;
				return true;	
			}
		}
		return false;
	}

    /**
     * We received some suggestions and the GUI can act on them.
     * <p>
     * In our practice, the listener is IGTViewer which will open
     * a window and show the suggestions.
     * <p>
     * This will in the end cause our {@link #suggestionSelected(SuggestionSelectionEvent)}
     * method to be called if the user clicks on one of the suggestions,
     * {@link #suggestionIgnored(SuggestionSelectionEvent)}
     * if the user wants to skip that suggestion, 
     * or {@link #suggestionClosed()} if the user dismisses the window.
     * (We are the SuggestionSelectionListener.)
     * 
     * @param sugSets
     * @param recursionLevel 
     */
	private void notifySuggestionSetDelivered(List<SuggestionSet> sugSets) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, String.format("notifySuggestionSetDelivered: this opens a window; position queue has %d items",
				positionQueue.size()));
		}
		SuggestionSetEvent event = new SuggestionSetEvent(this, sugSets, 0);

		for (SuggestionSetListener ssl : suggestionListeners) {
			ssl.suggestionSetDelivered(event);
			// Eventually our suggestionSelected() method is called
			// if the user clicks on one of the suggestions;
			// suggestionIgnored() if the user clicks outside a suggestion;
			// suggestionClosed() the user dismisses the window.
		}
	}

	/**
	 * This notification is sent when the user just clicked some action
	 * to create new suggestions. Therefore any previously opened GUI windows
	 * for suggestions need to be cancelled.
	 */
	private void notifyCancelSuggestionSet() {
		for (SuggestionSetListener ssl : suggestionListeners) {
			ssl.cancelSuggestionSet();
		}
	}
    
    @Override
	public Object prompt(Prompt arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the annotations from the specified position 
	 * (i.e. from the specified tier strictly within the specified interval).
	 * Use end = -1 for no end time.
	 * 
	 * @param pos the Position containing tier identifier and time interval
	 * @return a list of annotations converted to Suggestion objects, or null if Position is null
	 */
	@Override
	public List<Suggestion> readAnnotation(Position pos) {
		if (pos == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Read annotations: Position is null");
			}
			return null;
		}
		List<Suggestion> annotations = new ArrayList<Suggestion>();
		
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		
		if (destTier != null) {
			List<AbstractAnnotation> curAnnos = destTier.getAnnotations();
			final long posBeginTime = pos.getBeginTime();
			final long posEndTime = pos.getEndTime() >= 0 ? pos.getEndTime()
					                                      : Long.MAX_VALUE;
			
			for (int i = 0; i < curAnnos.size(); i++) {
				AbstractAnnotation aa = curAnnos.get(i);
				
				final long annoEndTime = aa.getEndTimeBoundary();
				if (annoEndTime < posBeginTime) {
					continue;
				}
				
				final long annoBeginTime = aa.getBeginTimeBoundary();
				if (annoBeginTime > posEndTime) {
					break;
				}
				
				if (annoBeginTime >= posBeginTime && annoEndTime <= posEndTime) {
					Suggestion a = new Suggestion(aa.getValue(),
							                      new Position(pos, annoBeginTime, annoEndTime));
					annotations.add(a);
				}
			}
		}
		
		return annotations;
	}

	/**
	 * Returns the parent annotations of the annotation(s) found at the specified position. 
	 * The returned list is a flat list of annotations; if the list contains more than one
	 * annotation (because the specified position contains multiple annotations with different
	 * parent annotations), there is no obvious or direct relation between "input" child 
	 * annotations and the annotations in the returned list. Those relations have to be 
	 * reconstructed based on time stamps.
	 * The typical use case is to get the parent annotation of a single input annotation,
	 * in which case the returned list will have a size of 1. 
	 * 
	 * @param pos the Position containing tier identifier and time interval
	 * @return a list of parent annotations converted to Suggestion objects, 
	 * or null if Position is null or if the specified tier doesn't have a parent tier
	 */
	@Override
	public List<Suggestion> readParentAnnotation(Position pos) {
		if (pos == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Read parent annotation: Position is null");
			}
			return null;
		}
		
		
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		if (destTier == null || destTier.getParentTier() == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Read parent annotation: Requested tier and/or its parent tier is null");
			}
			return null;
		}
		
		List<Suggestion> annotations = new ArrayList<Suggestion>();
		// get the parent tier
		TierImpl parentTier = destTier.getParentTier();
		List<AbstractAnnotation> curAnnos = parentTier.getAnnotations();
		final long posBeginTime = pos.getBeginTime();
		final long posEndTime = pos.getEndTime() >= 0 ? pos.getEndTime()
				                                      : Long.MAX_VALUE;
		
		for (int i = 0; i < curAnnos.size(); i++) {
			AbstractAnnotation aa = curAnnos.get(i);
			
			final long annoEndTime = aa.getEndTimeBoundary();
			if (annoEndTime < posBeginTime) {
				continue;
			}
			
			final long annoBeginTime = aa.getBeginTimeBoundary();
			if (annoBeginTime > posEndTime) {
				break;
			}
			
			if (annoBeginTime >= posBeginTime && annoEndTime <= posEndTime) {
				Suggestion a = new Suggestion(aa.getValue(),
						                      new Position(parentTier.getName(), annoBeginTime, annoEndTime));
				annotations.add(a);
			}
		}
		
		return annotations;
	}

	/**
	 * Returns the sibling annotations of the annotation found at the specified position. 
	 * The returned list is a list of annotations with the same parent. If the specified 
	 * position contains multiple annotations with different parent annotations, only the 
	 * first annotation will be processed. 
	 * 
	 * @param pos the Position containing tier identifier and time interval
	 * @return a list of sibling annotations converted to Suggestion objects, 
	 * or null if Position is null or if the specified tier doesn't have a parent tier and
	 * therefore there are no sibling annotations. The list will contain the annotation
	 * corresponding to the pos parameter.
	 */
	@Override
	public List<Suggestion> readSiblingAnnotations(Position pos) {
		if (pos == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Read sibling annotations: Position is null");
			}
			return null;
		}
		
		
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		if (destTier == null || destTier.getParentTier() == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Read sibling annotations: Requested tier and/or its parent tier is null");
			}
			return null;
		}
		List<Suggestion> annotations = new ArrayList<Suggestion>();
		List<AbstractAnnotation> curAnnos = destTier.getAnnotations();
		final long posBeginTime = pos.getBeginTime();
		final long posEndTime = pos.getEndTime() >= 0 ? pos.getEndTime()
				                                      : Long.MAX_VALUE;
		AbstractAnnotation firstInInterval = null;
		// find the first (probably only) annotation in the interval
		for (int i = 0; i < curAnnos.size(); i++) {
			AbstractAnnotation aa = curAnnos.get(i);
			
			final long annoEndTime = aa.getEndTimeBoundary();
			if (annoEndTime < posBeginTime) {
				continue;
			}
			
			final long annoBeginTime = aa.getBeginTimeBoundary();
			if (annoBeginTime > posEndTime) {
				break;
			}
			
			if (annoBeginTime >= posBeginTime && annoEndTime <= posEndTime) {
				firstInInterval = aa;
				break;
			}
		}
		
		if (firstInInterval != null) {
			AbstractAnnotation parentAnn = (AbstractAnnotation) firstInInterval.getParentAnnotation();
			if (parentAnn != null) {// shouldn't be the case
				final long parBeginTime = parentAnn.getBeginTimeBoundary();
				final long parEndTime = parentAnn.getEndTimeBoundary();
				
				for (int i = 0; i < curAnnos.size(); i++) {
					AbstractAnnotation aa = curAnnos.get(i);
					
					final long annoEndTime = aa.getEndTimeBoundary();
					if (annoEndTime < parBeginTime) {
						continue;
					}
					
					final long annoBeginTime = aa.getBeginTimeBoundary();
					if (annoBeginTime > parEndTime) {
						break;
					}
					
					if (annoBeginTime >= parBeginTime && annoEndTime <= parEndTime) {
						Suggestion a = new Suggestion(aa.getValue(),
			                      new Position(pos, annoBeginTime, annoEndTime));
						annotations.add(a);
					}
				}
			}
		}
		
		return annotations;
	}

	@Override
	public boolean remAnnotation(Position pos) {
		if (pos == null) {
			// warn
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Remove annotation: Position is null");
			}
			return false;
		}
		
		TierImpl destTier = (TierImpl) transcription.getTierWithId(pos.getTierId());
		
		if (destTier != null) {
			long t = (pos.getBeginTime() + pos.getEndTime()) / 2;
			AbstractAnnotation aa = (AbstractAnnotation) destTier.getAnnotationAtTime(t);
			if (aa != null) {
				// use the undoable delete command
				Command command = ELANCommandFactory.createCommand(transcription, 
						ELANCommandFactory.DELETE_ANNOTATION);
				command.execute(destTier, new Object[]{ELANCommandFactory.getViewerManager(transcription), aa});
				// notify the analyzer of the removal? 
				//should there be an annotationDeleted method in the api? 
				//lexanMediator.remAnnotation(pos);
				return true;
			} else {
				// warn
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, 
							"Remove annotation: there is no annotation at the specified position");
				}
			}
		}

		return false;
	}

	/**
	 * Add the listener to the list.
	 * 
	 * @param listener the suggestion set listener
	 */
	@Override
	public void addSuggestionSetListener(SuggestionSetListener listener) {
		if (listener != null && !suggestionListeners.contains(listener)) {
			suggestionListeners.add(listener);
		}
	}
	
	/**
	 * Removes the specified listener from the list of listeners.
	 * 
	 * @param listener the listener to remove
	 */
	@Override
	public void removeSuggestionSetListener(SuggestionSetListener listener) {
		suggestionListeners.remove(listener);
	}

	/**
	 * Implementation of SuggestionSelectionListener.
	 * Notification of selection of a suggestion set by the user.
	 * 
	 * @param event the suggestion selection event. The source of an event is a suggestion viewer model.
	 */
	@Override // SuggestionSelectionListener
	public void suggestionSelected(SuggestionSelectionEvent event) {
		if (event != null) {
			IGTSuggestionViewerModel viewModel = (IGTSuggestionViewerModel) event.getSource();
			int row = event.getSelectedRow();
			try {
				IGTSuggestionModel sugModel = viewModel.getRowData(row);
				SuggestionSet sugSet = sugModel.getSuggestionSet();
				// recursion level not used anymore
				//int recursionLevel = viewModel.getRecursionLevel();
				// this position has been analyzed, remove from the queue
				removePositionFromQueue(sugSet);
				notifyLexanSuggestionSelected(row, sugSet);
				
				// create annotations via a AnnotationsFromSuggestionSetCommand
				// Apply the suggestions, and analyze recursively if enabled.
				annotationsFromSuggestionSet(sugSet);		
				
				analyzeNext();
			} catch (ArrayIndexOutOfBoundsException abe) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "The row of the selcted suggestion does not exist: " + row);
				}
			}
		}

		sugWindowOpened = false;
	}

	/**
	 * Callback called when the user does not want to be presented with any
	 * more suggestions.
	 */
	@Override // SuggestionSelectionListener
	public void suggestionClosed(SuggestionSelectionEvent event) {
		if (event != null) {
			IGTSuggestionViewerModel viewModel = (IGTSuggestionViewerModel) event.getSource();
			IGTSuggestionModel sugModel = viewModel.getRowData(0);
			SuggestionSet sugSet = sugModel.getSuggestionSet();
			
			notifyLexanSuggestionClosed(sugSet);
			removePositionFromQueue(sugSet);
			analyzeNext();
		}
		// break out of auto analyze mode
		sugWindowOpened = false;
		autoAnalyzeMode = false;
	}
	
	private void removePositionFromQueue(SuggestionSet sugSet) {
		if (sugSet != null) {
			Pair<TextAnalyzer, Position> firstPos = positionQueue.peekFirst();
			if (firstPos != null && firstPos.getSecond().equals(sugSet.getSource())) {
				// the stored position is the source of the suggestion set
				positionQueue.removeFirst();
			} else if (firstPos != null) {
				// if the "source" position of the suggestions is actually the target of a configuration 
				// of the analyzer in the first position of the queue,
				// assume the first element can be removed from the queue 
				// (until the API ensures that source and target position are passed to newAnnotation()
				for (AnalyzerConfig ac : configs) {
					if (ac.getAnnotId().equals(firstPos.getFirst().getInformation())) {
						for (AnalyzerConfig at : ac.getTierConfigurations()) {
							if (at.getDest().contains(sugSet.getSource().getTierId())) {
								positionQueue.removeFirst();
								return;
							}
						}
					}
				}
			} else {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, 
							"The processed Position is not the first object in the queue, cannot remove it");
				}
			}
		} else {
			// remove the first element anyway
			if (positionQueue.peekFirst() != null) {
				positionQueue.removeFirst();
			}
		}
	}

	/**
	 * Callback called when the user does not want to act on this particular
	 * suggestion, but does want to see more subsequent suggestions.
	 */
	@Override // SuggestionSelectionListener
	public void suggestionIgnored(SuggestionSelectionEvent event) {
		if (event != null) {
			IGTSuggestionViewerModel viewModel = (IGTSuggestionViewerModel) event.getSource();
			IGTSuggestionModel sugModel = viewModel.getRowData(0);
			SuggestionSet sugSet = sugModel.getSuggestionSet();
			
			removePositionFromQueue(sugSet);			
			notifyLexanSuggestionIgnored(sugSet);
			analyzeNext();
		}
		
		sugWindowOpened = false;
	}

	public void annotationDeleted(Position pos) {
		// TODO Auto-generated method stub
	}

	public void annotationsAdded(Position pos) {
		// TODO Auto-generated method stub
	}

	@Override // TextAnalyzerContext
	public void addSuggestionSelectionListener(LexanSuggestionSelectionListener l) {
		if (suggestionSelectionListeners == null) {
			suggestionSelectionListeners = new ArrayList<LexanSuggestionSelectionListener>();
		}
		
		if (l != null && !suggestionSelectionListeners.contains(l)) {
			suggestionSelectionListeners.add(l);
		}
	}

	@Override // TextAnalyzerContext
	public void removeSuggestionSelectionListener(LexanSuggestionSelectionListener l) {
		if (suggestionSelectionListeners != null) {
			suggestionSelectionListeners.remove(l);
		}
	}
	
	// Handle LexanSuggestionSelectionListeners:
	private void notifyLexanSuggestionSelected(int nr, SuggestionSet selection) {
		if (suggestionSelectionListeners != null) {
			for (LexanSuggestionSelectionListener l : suggestionSelectionListeners) {
				l.suggestionSelected(nr, selection);
			}
		}
	}

	private void notifyLexanSuggestionIgnored(SuggestionSet sugSet) {
		if (suggestionSelectionListeners != null) {
			for (LexanSuggestionSelectionListener l : suggestionSelectionListeners) {
				l.suggestionIgnored(sugSet);
			}
		}
	}

	private void notifyLexanSuggestionClosed(SuggestionSet sugSet) {
		if (suggestionSelectionListeners != null) {
			for (LexanSuggestionSelectionListener l : suggestionSelectionListeners) {
				l.suggestionClosed(sugSet);
			}
		}
	}
}
