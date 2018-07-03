package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FontSizePanel;
import mpi.eudico.client.annotator.interlinear.IGTPlayerControlPanel;
import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.config.AnalyzerConfig;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAbstractDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDefaultModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.RenderingConfigDialog;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.client.annotator.util.AnnotationCoreComparator;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import nl.mpi.lexan.analyzers.helpers.Position;

/**
 * The  InterlinearEditor contains a scrollbar, an IGTViewer controlled by that scrollbar,
 * and a button panel. The button panel contains a play selection control panel, 
 * a button to start the Interlinearization process, a checkbox to make the process 
 * recursive, a configuration button and a font size panel.
 * <pre>
 *   +----------------------------------------------------------------+
 *   |                                                                |
 *   | Button(s)                                                      |
 *   |+-----------------------------------------------------------++-+|
 *   || IGTViewer                                                 ||s||
 *   || ....                                                      ||c||
 *   ||                                                           ||r||
 *   ||                                                           ||o||
 *   ||                                                           ||l||
 *   ||                                                           ||l||
 *   |+-----------------------------------------------------------++-+|
 *   +----------------------------------------------------------------+
 * </pre>
 */
@SuppressWarnings("serial")
public class InterlinearEditor extends AbstractViewer 
implements ComponentListener, ActionListener, ACMEditListener {
	private InterlinearizationManager manager;
	private TranscriptionImpl transcription = null;
	
    private Font font;
    private int bpHeight = 40;
    
    private JPanel buttonPanel;
    private JButton interButton;
    private JButton configureButton;
    private JCheckBox recursiveCB;
    private FontSizePanel fontSizePanel;
    private IGTPlayerControlPanel playerControlPanel;
    private JSplitPane buttonSplitPane;
	private SpringLayout buttonLayout;
    
    private AnalyzerConfig activeConfig = null;
    private IGTViewer igtViewer = null;
    private IGTViewerModelImpl viewerModel = null;
    
	/**
	 * 
	 * @param manager
	 */
	public InterlinearEditor(InterlinearizationManager manager) {
		super();
		this.manager = manager;
		this.transcription = (TranscriptionImpl) manager.getTranscription();
		initViewer();
	}
	
	public void configsChanged() {
		paintBuffer();
	}
	
	public void setActiveConfiguration(AnalyzerConfig ac) {
		activeConfig = ac;
	}
	
	public AnalyzerConfig getActiveConfiguration() {
		return activeConfig;
	}
	
	/**
	 * Returns the component that implements the Zoomable interface and will
	 * receive calls from key events.
	 * 
	 * @return the zoomable component, the font size panel
	 */
	public Zoomable getZoomable() {
		return fontSizePanel;
	}
	
	private void initViewer() {
		setOpaque(true);
        font = Constants.DEFAULTFONT;
        setFont(font);
        //metrics = getFontMetrics(font);
		setBackground(Color.WHITE);
		buttonLayout = new SpringLayout();
		buttonPanel = new JPanel(buttonLayout);
		playerControlPanel = new IGTPlayerControlPanel();
		buttonSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		buttonSplitPane.setOneTouchExpandable(false);
		buttonSplitPane.setLeftComponent(playerControlPanel);
		buttonSplitPane.setRightComponent(buttonPanel);
		buttonSplitPane.setContinuousLayout(true);
		buttonSplitPane.setBorder(null);
		//buttonSplitPane.setDividerSize(6);
		
		add(buttonSplitPane);
		
		interButton = new JButton();
		interButton.addActionListener(this);
		buttonPanel.add(interButton);
		recursiveCB = new JCheckBox();
		buttonPanel.add(recursiveCB);
		recursiveCB.addActionListener(this);
		//	
		viewerModel = new IGTViewerModelImpl();
		initModel();
		igtViewer = new IGTViewer(viewerModel, manager.getTextAnalyzerContext(), 
				manager.getTextAnalyzerLexiconContext(), this);
		igtViewer.readGUIPreferences(transcription);
		add(igtViewer);
		//
		configureButton = new JButton();
		configureButton.addActionListener(this);
		try {
			ImageIcon confIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Configure16.gif"));
			configureButton.setIcon(confIcon);
		} catch (Exception ex) {
			// catch any image loading exception
			configureButton.setText("C");
		}
		//configureButton.setEnabled(false);
		buttonPanel.add(configureButton);
		
		fontSizePanel = new FontSizePanel(igtViewer);
		buttonPanel.add(fontSizePanel);
		// layout buttonPanel
		buttonLayout.putConstraint(SpringLayout.WEST, interButton, 6, 
				SpringLayout.WEST, buttonPanel);
		buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, interButton, 0, 
				SpringLayout.VERTICAL_CENTER, buttonPanel);
		buttonLayout.putConstraint(SpringLayout.WEST, recursiveCB, 6, 
				SpringLayout.EAST, interButton);
		buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, recursiveCB, 0, 
				SpringLayout.VERTICAL_CENTER, buttonPanel);
		buttonLayout.putConstraint(SpringLayout.EAST, fontSizePanel, -3, 
				SpringLayout.EAST, buttonPanel);
		buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, fontSizePanel, 0, 
				SpringLayout.VERTICAL_CENTER, buttonPanel);	
		buttonLayout.putConstraint(SpringLayout.EAST, configureButton, -12, 
				SpringLayout.WEST, fontSizePanel);
		buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, configureButton, 0, 
				SpringLayout.VERTICAL_CENTER, buttonPanel);
		//
		bpHeight = fontSizePanel.getPreferredSize().height + 4;
		buttonSplitPane.setDividerLocation(igtViewer.getHeaderWidth() - buttonSplitPane.getDividerSize() / 2);
		setPreferredFontAndColorSettings();
		manager.getViewerManager().connectViewer(playerControlPanel, true);
		manager.getViewerManager().connectListener(playerControlPanel);
		
		addComponentListener(this);
		playerControlPanel.addComponentListener(this);
		updateLocale();
		
		paintBuffer();
	}
	
	/**
	 * Sets the preferred font and color setting for the tiers                        							
	 */
	private void setPreferredFontAndColorSettings(){			
		// preferred tier colors
		Map<String, Color> tierColorMap = new HashMap<String, Color>();
		Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors != null) {
			for (Map.Entry<String, Color> e : colors.entrySet()) {
				String name = e.getKey();
				Color col = e.getValue();
				Tier t = transcription.getTierWithId(name);
				if (t != null) {					
					tierColorMap.put(name, col);
				}
			}
		}
		
		igtViewer.setTierColorMap(tierColorMap);
		
		//preferred fonts
		Map<String, Font> fo = Preferences.getMapOfFont("TierFonts", transcription);
		if (fo != null) {
			igtViewer.setTierFontMap(fo);
		}
		
		//preferred font size
		Integer size = Preferences.getInt("InterlinearizationEditor.FontSize", transcription);
		if (size != null) {
			fontSizePanel.setFontSize(size);
		}
	}
	
	private void initModel() {		
        List<String> hiddenTiers = Preferences.getListOfString("InterlinearEditor.HiddenTiers", transcription);
        if (hiddenTiers == null) {
        	// try the tier set of the timeline viewer
        	hiddenTiers = Preferences.getListOfString("MultiTierViewer.HiddenTiers", transcription);
        	if (hiddenTiers == null) {
        		hiddenTiers = new ArrayList<String>();
        	}
        }
        viewerModel.setHiddenTiers(hiddenTiers);
        // read preferences for special tiers
        List<IGTTierType> hiddenSpecialTiers = new ArrayList<IGTTierType>(3);
        Boolean showTCPref = Preferences.getBool("InterlinearEditor.ShowTimeCode", transcription);
        if (showTCPref != null && !showTCPref) {
        	hiddenSpecialTiers.add(IGTTierType.TIME_CODE);
        	viewerModel.setSpecialTierVisibility(IGTTierType.TIME_CODE, showTCPref);// default is true, only set when false
        }
		
        Boolean showSpeakPref = Preferences.getBool("InterlinearEditor.ShowSpeaker", transcription);
        if (showSpeakPref != null && !showSpeakPref) {
        	hiddenSpecialTiers.add(IGTTierType.SPEAKER_LABEL);
        	viewerModel.setSpecialTierVisibility(IGTTierType.SPEAKER_LABEL, showSpeakPref);// default is true, only set when false
        }
		// get all annotations from all (toplevel) tiers, minus the hidden tiers
		List<AlignableAnnotation> allAnns = new ArrayList<AlignableAnnotation>();
		
		for (TierImpl t : transcription.getTiers()) {
			if (!t.hasParentTier() && !hiddenTiers.contains(t.getName())) {
				allAnns.addAll(t.getAlignableAnnotations());
			}
		}
		
		AnnotationCoreComparator comparator = new AnnotationCoreComparator();
		Collections.sort(allAnns, comparator);
		
		for (AlignableAnnotation aa : allAnns) {
			IGTDefaultModel rowModel = new IGTDefaultModel(aa, hiddenTiers, hiddenSpecialTiers);
			viewerModel.addRow(rowModel);
		}
	}
	
	/**
	 * @param row the row index
	 * @return the list of visible rows in the order of the view (special rows are ignored),
	 * null if the row does not exist (instead of ArrayIndexOutOfBoundsException) 
	 */
	public List<String> getVisibleTiersRow(int row) {
		if (row >= 0 && row < viewerModel.getRowCount()) {
			return ((IGTAbstractDataModel)viewerModel.getRowData(row)).getVisibleTierOrder();
		}
		return null;
	}
	
	/**
	 * @param row the row index
	 * @return the data model in that row or null if the row does not exist
	 */
	public IGTAbstractDataModel getModelFromRow (int row) {
		if (row >= 0 && row < viewerModel.getRowCount()) {
			return (IGTAbstractDataModel)viewerModel.getRowData(row);
		}
		
		return null;
	}
	
	/**
	 * @param annotation the annotation to find in the IGT model
	 * @return the row in the table where this annotation is part of, -1 if not found
	 */
	public int getRowForAnnotation(AbstractAnnotation annotation) {
		if (annotation != null) {
			return findRowIndexForAnnotation(annotation);
		}
		
		return -1;
	}
	
	/**
	 * @return the currently selected row, the row that is being edited, -1 if no row is selected
	 */
	public int getSelectedRow() {
		return igtViewer.getSelectedRow();
	}
	
	/**
	 * @return the number of rows in the table (i.e. in the viewer model).
	 */
	public int getRowCount() {
		return viewerModel.getRowCount();
	}
	
	/**
	 * Delegates the actual implementation to IGTViewer, passing the active annotation 
	 * (which can be null). 
	 * The IGTViewer might have to select the correct row in the table and/or might have 
	 * to select and set a new active annotation.
	 */
	public void startEditAnnotation() {
		igtViewer.startEditAnnotation(getActiveAnnotation());
	}
	
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//	}

	@Override
	public void controllerUpdate(ControllerEvent event) {
		// stub

	}
	
	/**
	 * This is the time to store preferences...
	 */
	@Override
	public void isClosing() {
		Preferences.set("InterlinearizationEditor.FontSize", igtViewer.getFontSize(), 
				transcription, false, false);
		
		manager.getViewerManager().connectViewer(playerControlPanel, false);
		manager.getViewerManager().disconnectListener(playerControlPanel);
		igtViewer.isClosing(transcription);
	}
	
	private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }
        
        repaint();
	}
	
	/**
	 * For now, at any edit event, repaint
	 * @param e
	 */
	@Override
	public void ACMEdited(ACMEditEvent e) {
		// check the event type, and how to respond to each relevant event
		final int rowCount = viewerModel.getRowCount();
		switch (e.getOperation()) {
		case ACMEditEvent.ADD_ANNOTATION_HERE:
			
            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof AbstractAnnotation) {
            	AbstractAnnotation newAnn = (AbstractAnnotation) e.getModification();
                TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                assert(invTier == newAnn.getTier());

                IGTDefaultModel rowModel = findAnnotationRow(newAnn, invTier);
                if (rowModel != null) {
    				// if this succeeds this generates an event 
    				rowModel.annotationAdded(newAnn);
                }
            }
            
			break;
		case ACMEditEvent.ADD_ANNOTATION_AFTER:
			
            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof AbstractAnnotation) {
            	AbstractAnnotation newAnn = (AbstractAnnotation) e.getModification();
                TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                assert(invTier == newAnn.getTier());
                AbstractAnnotation annBefore = (AbstractAnnotation) invTier.getAnnotationBefore(newAnn);
                if (annBefore != null && annBefore.getParentAnnotation() == newAnn.getParentAnnotation()) {
	
	                IGTDefaultModel rowModel = findAnnotationRow(newAnn, invTier);
	                if (rowModel != null) {
        				// if this succeeds this generates an event 
        				rowModel.annotationAddedAfter(newAnn, annBefore);
	                }
                }// else warn??
            }
            
			break;
		case ACMEditEvent.ADD_ANNOTATION_BEFORE:

            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof AbstractAnnotation) {
            	AbstractAnnotation newAnn = (AbstractAnnotation) e.getModification();
                TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                assert(invTier == newAnn.getTier());
                AbstractAnnotation annAfter = (AbstractAnnotation) invTier.getAnnotationAfter(newAnn);
                if (annAfter != null && annAfter.getParentAnnotation() == newAnn.getParentAnnotation()) {
	                
	                IGTDefaultModel rowModel = findAnnotationRow(newAnn, invTier);
	
	                if (rowModel != null) {
						// if this succeeds this generates an event 	
						rowModel.annotationAddedBefore(newAnn, annAfter);          	
	                }
                } // else warn??
            }
            
			break;
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
        	// modification object is null
            if (e.getInvalidatedObject() instanceof AbstractAnnotation) {
            	AbstractAnnotation a = (AbstractAnnotation) e.getInvalidatedObject();

                TierImpl invTier = (TierImpl) a.getTier();          	
                IGTDefaultModel rowModel = findAnnotationRow(a, invTier);

                if (rowModel != null) {
    				// if this succeeds this generates an event 
    				rowModel.annotationValueChanged(a);				
                }
                int row = igtViewer.getSelectedRow();
                if (row > -1) {
                	if (viewerModel.getRowData(row) == rowModel) {
                		igtViewer.updateStaleEditor(row);
                	}
                }
            }
            
            break;
            
        case ACMEditEvent.REMOVE_ANNOTATION:
        	// currently the transcription is the only information available in the event
        	// the deleted annotation needs to be accessible as well
            if (e.getInvalidatedObject() instanceof Transcription) {
                //System.out.println("Invalidated object: " + e.getInvalidatedObject());
            	
            	if (e.getModification() instanceof AbstractAnnotation) {
            		AbstractAnnotation remAnnotation = (AbstractAnnotation) e.getModification();
                    
                    TierImpl invTier = (TierImpl) remAnnotation.getTier();
                	
                    IGTDefaultModel rowModel = findAnnotationRow(remAnnotation, invTier);
                    if (rowModel != null) {
        				// if this succeeds this generates an event 
                    	if (rowModel.getRootRow().getTierName().equals(invTier.getName())) {
                    		int row = findRowIndexForAnnotation(remAnnotation);
                    		viewerModel.removeRowData(rowModel);
                    		// check if the row is still in the model and there is no selected row anymore
                    		// because the row was the previously selected row
                    		if (row < viewerModel.getRowCount() && igtViewer.getSelectedRow() == -1) {
                    			igtViewer.updateStaleEditor(row);
                    		}
                    	} else {
                    		rowModel.annotationRemoved(remAnnotation);
                    	}
                    }
            	} else {
            		// The case where getModification() == null.
            		// We must assume that anything is now potentially changed.
            		// Loop over ALL annotations in out model, and if any is markedDeleted,
            		// remove the IGTAnnotation for it.
            		// (Note that this case happens when applying suggestions but somehow
            		// the overlapped removed annotations are not yet marked deleted anyway...)
                    for (int i = 0; i < rowCount; i++) {
                    	IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
                    	
                    	int tierRowCount = rowModel.getRowCount();
                    	for (int j = 0; j < tierRowCount; j++) {
                    		IGTTier tierRowModel = rowModel.getRowData(j);
                    		ArrayList<IGTAnnotation> anns = tierRowModel.getAnnotations();
                    		
                    		// Loop backwards because we remove elements from this ArrayList.
                    		for (int k = anns.size() - 1; k >= 0; k--) {
                    			IGTAnnotation igtAnn = anns.get(k);
                    			final AbstractAnnotation annotation = igtAnn.getAnnotation();
								//if (annotation != null) {
								//	System.out.printf("Ann: %s deleted: %s\n", annotation.getValue(), annotation.isMarkedDeleted());
								//}
								if (annotation != null && annotation.isMarkedDeleted()) {
                    				rowModel.annotationRemoved(annotation);
                    				tierRowCount = rowModel.getRowCount(); // this value might change if child tiers get empty???
                    			}
                    		}
                    	}             	
                     }
                    igtViewer.recalculateAllRows(true);
            	}
            }

            break;
            
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        	// any number of annotations might have been changed. Usually this happens after an undo 
        	// of deleting an annotation with dependent annotations 
        	eventChangeAnnotations();        	
        	break;
        	
        case ACMEditEvent.REMOVE_TIER:
        	Object obj  = e.getModification();
			if(obj instanceof TierImpl){
				TierImpl invTier = (TierImpl) obj;
                TierImpl rootTier = invTier.getRootTier();
                String rootTierName = rootTier.getName();
                
                for (int i = 0; i < rowCount; i++) {
                	IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
                	IGTTier igtTier = rowModel.getRootRow();
                	
                 	if (igtTier.getTierName().equals(rootTierName)) {
                 		//igtTier.removeChildTier(invTier.getName());
                 		rowModel.tierRemoved(invTier.getName());
                 		if(invTier == rootTier){
                 			//viewerModel.removeRowData(rowModel);
                 			viewerModel.removeRow(i);
                 			i--;	// counteract the i++
                 		}
                 	}
                 }
                igtViewer.recalculateAllRows(true);
			}
			break;	
			
        case ACMEditEvent.ADD_TIER:
        	obj  = e.getModification();
			if (obj instanceof TierImpl) {
				TierImpl invTier = (TierImpl) obj;
                if (invTier.hasParentTier()) {
                	// A Child tier has been added
                	TierImpl rootTier = invTier.getRootTier();
                    String rootTierName = rootTier.getName();
                    
                    for (int i = 0; i < rowCount; i++) {
                    	IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
                    	IGTTier igtTier = rowModel.getRootRow();
                    	
                     	if (igtTier.getTierName().equals(rootTierName)) {
                     		//igtTier.removeChildTier(invTier.getName());
                     		rowModel.tierAdded(invTier, viewerModel.getHiddenTiers());
                     	}
                     }
                    igtViewer.recalculateAllRows(true);
                } else {
                	// A root tier has been added
                	if (viewerModel.getHiddenTiers().contains(invTier.getName())) {
                		break;
                	}
                	List<AlignableAnnotation> allAnns = new ArrayList<AlignableAnnotation>();
                	allAnns.addAll(invTier.getAlignableAnnotations());
                	
            		int nextIndex = 0;
            		while (allAnns.size() > 0) {
            			AlignableAnnotation aa = allAnns.get(0);
            			IGTDefaultModel newRowModel = new IGTDefaultModel(aa, viewerModel.getHiddenTiers());
            			
            			if(nextIndex >= rowCount-1){
            				viewerModel.addRow(newRowModel);	
            				nextIndex++;
            			} else {
            				for (int i = nextIndex; i < rowCount; i++) {
            					IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
                            	long beginTime = rowModel.getBeginTime();
                             	
                            	// begin time
                                if (aa.getBeginTimeBoundary() < beginTime) {
                                	viewerModel.insertRow(newRowModel, i);
                                	nextIndex = i+1;
                                	break;
                                } else if (aa.getBeginTimeBoundary() > beginTime) {
                                   continue;
                                }
                                
                                // begin time equal, compare end time
                                if (aa.getEndTimeBoundary() < rowModel.getEndTime()) {
                                	viewerModel.insertRow(newRowModel, i);
                                	nextIndex = i+1;
                                	break;
                                } else if (aa.getEndTimeBoundary() > rowModel.getEndTime()) {
                                	continue;
                                }
                             }
            			}
            		}
            		if (allAnns.size() > 0) {
                    	igtViewer.recalculateAllRows(true);
                    }
                }
			}
			break;
			
		default:
				// do nothing
		}
		paintBuffer();
	}

	/**
	 * @param changedAnn
	 * @param changedTier
	 * @param annAfter
	 */
	private IGTDefaultModel findAnnotationRow(AbstractAnnotation changedAnn,
			TierImpl changedTier) {
        long bt = changedAnn.getBeginTimeBoundary();
        long et = changedAnn.getEndTimeBoundary();
        
        TierImpl rootTier = changedTier.getRootTier();
        String rootTierName = rootTier.getName();

        final int rowCount = viewerModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
			IGTTier igtTier = rowModel.getRootRow();
			
			if (igtTier.getTierName().equals(rootTierName)) {
				List<IGTAnnotation> annos = igtTier.getAnnotations();
				if (annos != null && annos.size() > 0) {
					IGTAnnotation igtAnn = annos.get(0);
					
					if (igtAnn.getAnnotation().getBeginTimeBoundary() <= bt && 
							igtAnn.getAnnotation().getEndTimeBoundary() >= et) {
						return rowModel;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Finds the index of the row the specified annotation is in.
	 * @param aa the annotation to find
	 * @return the index of the row or -1 if the annotation is not found
	 */
	private int findRowIndexForAnnotation(AbstractAnnotation aa) {
        long bt = aa.getBeginTimeBoundary();
        long et = aa.getEndTimeBoundary();
        
        final int rowCount = viewerModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
			
			if (rowModel.getBeginTime() <= bt && rowModel.getEndTime() >= et) {
				IGTTier igtTier = rowModel.getRowDataForTier(aa.getTier().getName());
				if (igtTier != null) {
					for (IGTAnnotation igta : igtTier.getAnnotations()) {
						if (igta.getAnnotation() == aa) {
							return i;
						}
					}
				}
			}
		}
		
        return -1;
	}

	/**
	 * Handle "unspecified annotations" being changed. This usually happens 
	 * after an undo of a delete action where an annotation with child annotations
	 * has been deleted.
	 * Since there is no way to tell which of the rows in the model is effected 
	 * (if any) it is necessary to:<br>
	 * a) rebuild the entire model <br>
	 * b) iterate over all top level annotations plus descendants and compare the number 
	 * of annotations with the structure of the rows in the viewer model.
	 * 
	 * Based on some simple tests it seems that b performs a bit better
	 * 
	 * This event is also generated when e.g. all annotations of a tier have been removed
	 * or when the value of all annotations of a tier have been changed (e.g. Label and Number)
	 * so there is almost no alternative than to completely recreate the viewer model. 
	 */
	private void eventChangeAnnotations() {
		int origditRow = viewerModel.getEditingRow();
		boolean justRebuild = true;
		long curTime = System.nanoTime();
		
		if (justRebuild) {
			// reload the whole model
			while (viewerModel.getRowCount() > 0) {
				viewerModel.removeRow(viewerModel.getRowCount() - 1);
		    }
			initModel();		
		
			igtViewer.recalculateAllRows(true);  // Generates a viewChanged() call to IGTViewerChangeListeners.
		} else {
			revalidateModel(); 
		}

		if (origditRow >= 0 && origditRow < viewerModel.getRowCount()) {
			// Let editor know its data may have changed under its feet.
			// The 'IGTAbstractDataModel model' it is editing is now probably stale...
			// Or should this happen via some listener?
			// Wrapping has not occurred properly.
			igtViewer.updateStaleEditor(origditRow);
		}
		//System.out.println("Update took: " + (System.nanoTime() - curTime));
		if (LOG.isLoggable(Level.FINE)) {
			long total = System.nanoTime() - curTime;
			LOG.fine(String.format("Viewer update took: %d nano", total));
		}
	}
	
	/**
	 * Something like this would be needed when an ACM event of type ACMEditEvent.CHANGE_ANNOTATIONS
	 * is received. Every row in the viewer would need to be compared with top level annotations 
	 * and all their children on visible tiers.
	 * Not used! 
	 */
	private void revalidateModel() {
		// get all annotations from all (toplevel) tiers, minus the hidden tiers
		List<AlignableAnnotation> allAnns = new ArrayList<AlignableAnnotation>();
		List<IGTTierType> hiddenSpecialTiers = new ArrayList<IGTTierType>(3);
		if (!viewerModel.getSpecialTierVisibility(IGTTierType.TIME_CODE)) {
			hiddenSpecialTiers.add(IGTTierType.TIME_CODE);
		}
		if (!viewerModel.getSpecialTierVisibility(IGTTierType.SPEAKER_LABEL)) {
			hiddenSpecialTiers.add(IGTTierType.SPEAKER_LABEL);
		}
		
		for (TierImpl t : transcription.getTiers()) {
			if (!t.hasParentTier() && !viewerModel.getHiddenTiers().contains(t.getName())) {
				allAnns.addAll(t.getAlignableAnnotations());
			}
		}
		
		AnnotationCoreComparator comparator = new AnnotationCoreComparator();
		Collections.sort(allAnns, comparator);
		// first remove rows of which the annotations are no longer there
		for (int i = viewerModel.getRowCount() - 1; i >= 0; i--) {
			IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
			Annotation rootAnn = rowModel.getRootRow().getRootTier().getAnnotations().get(0).getAnnotation();
			if (!allAnns.contains(rootAnn)) {
				viewerModel.removeRow(i);
			}
		}
		
		for (int i = 0; i < allAnns.size(); i++) {
			AlignableAnnotation aa = allAnns.get(i);
			if (i < viewerModel.getRowCount()) {
				IGTDefaultModel rowModel = (IGTDefaultModel) viewerModel.getRowData(i);
				if (aa != rowModel.getRootRow().getRootTier().getAnnotations().get(0).getAnnotation()) {
					IGTDefaultModel rowModelInsert = new IGTDefaultModel(aa, viewerModel.getHiddenTiers(), hiddenSpecialTiers);
					viewerModel.insertRow(rowModelInsert, i);
					igtViewer.calculateHeightForRow(i, false);
				} else {
					// would have to check all annotations on all visible tiers
					// error prone and maybe as processing intensive as recreating the model?
					for (int j = 0; j <rowModel.getRowCount(); j++) {
						IGTTier loopTier = rowModel.getRowData(j);
						if (loopTier.isSpecial()) {
							continue;
						}
						TierImpl ti = transcription.getTierWithId(loopTier.getTierName());
						if (loopTier.getAnnotations().size() != ti.getOverlappingAnnotations(rowModel.getBeginTime(), rowModel.getEndTime()).size()) {
							// replace row
							viewerModel.removeRow(i);
							IGTDefaultModel rowModelInsert = new IGTDefaultModel(aa, viewerModel.getHiddenTiers(), hiddenSpecialTiers);
							viewerModel.insertRow(rowModelInsert, i);
							igtViewer.calculateHeightForRow(i, false);
							break;
						}
					}
				}
			} else {
				IGTDefaultModel rowModelInsert = new IGTDefaultModel(aa, viewerModel.getHiddenTiers(), hiddenSpecialTiers);
				viewerModel.addRow(rowModelInsert);
				igtViewer.calculateHeightForRow(viewerModel.getRowCount() - 1, false);
			}
		}
	}
	
//	public void handleSuggestions(List<SuggestionSet> sugSets) {
//		
//	}
	
	/**
	 * We don't really have a way to show, use or manipulate the selection,
	 * which is basically just a time period. So we ignore this.
	 * On the other hand, we (or rather the IGTViewer) 
	 * do set the selection to the time period of the
	 * active annotation, when we set it.
	 * When switching between modes it would be good to jump to e.g. the first
	 * row in the time selection.
	 */
	@Override // AbstractViewer
	public void updateSelection() {
		if (getSelectionBeginTime() != getSelectionEndTime()) {
			igtViewer.selectRowForTime(getSelectionBeginTime());
		}
	}

	/**
	 * Here we get notified when someone changes the active annotation.
	 * We are a listener because <code>ViewerManager2.connect(AbstractViewer)</code>
	 * makes us one.
	 */
	@Override // AbstractViewer
	public void updateActiveAnnotation() {
		igtViewer.updateActiveAnnotation(getActiveAnnotation());
	}

	@Override // AbstractViewer
	public void updateLocale() {
		if (interButton != null) {
			interButton.setText(ElanLocale.getString("InterlinearEditor.Button.Interlinearize"));
			recursiveCB.setText(ElanLocale.getString("InterlinearEditor.CheckBox.Recursive"));
		}
		if (configureButton != null) {
			configureButton.setToolTipText(ElanLocale.getString("InterlinearEditor.Button.ConfigureViewer"));
		}
		if (fontSizePanel != null) {
			fontSizePanel.updateLocale();
		}
	}

	@Override // AbstractViewer
	public void preferencesChanged() {
		setPreferredFontAndColorSettings();
		
		Boolean boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);
		if (boolPref != null) {
			igtViewer.setDeselectCommits(boolPref.booleanValue());
		}
		
		// rendering settings for the editor
		Color evenRowColor = Preferences.getColor(IGTConstants.KEY_BACKGROUND_COLOR_EVEN, null);	
		if (evenRowColor != null) {
			igtViewer.getViewerRenderInfo().backgroundColor = evenRowColor;
		}
		Color oddRowColor = Preferences.getColor(IGTConstants.KEY_BACKGROUND_COLOR_ODD, null);
		if (oddRowColor != null) {
			igtViewer.getViewerRenderInfo().backgroundColor2 = oddRowColor;
		}
		Boolean showAnnBorders = Preferences.getBool(IGTConstants.KEY_ANN_BORDER_VIS_FLAG, null);
		if (showAnnBorders != null) {
			igtViewer.getViewerRenderInfo().showAnnoBorders = showAnnBorders;
		}
		Boolean showAnnBackground = Preferences.getBool(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG, null);
		if (showAnnBackground != null) {
			igtViewer.getViewerRenderInfo().showAnnoBackground = showAnnBackground;
		}
		Color annBorderColor = Preferences.getColor(IGTConstants.KEY_ANN_BORDER_COLOR, null);
		if (annBorderColor != null) {
			igtViewer.getViewerRenderInfo().annoBorderColor = annBorderColor;
		}
		Color annBgColor = Preferences.getColor(IGTConstants.KEY_ANN_BACKGROUND_COLOR, null);
		if (annBgColor != null) {
			igtViewer.getViewerRenderInfo().annoBackgroundColor = annBgColor;
		}
		
		boolean needRecalc = false;
		Integer topMargin = Preferences.getInt(IGTConstants.KEY_BBOX_TOP_MARGIN, null);
		Insets bBoxInsets = igtViewer.getViewerRenderInfo().annBBoxInsets;
		if (topMargin != null) {
			if (topMargin.intValue() != bBoxInsets.top) {
				 bBoxInsets.top = topMargin.intValue();
				 needRecalc = true;
			}
			if (topMargin.intValue() != bBoxInsets.bottom) {
				 bBoxInsets.bottom = topMargin.intValue();
				 needRecalc = true;
			}
		}
		Integer leftMargin = Preferences.getInt(IGTConstants.KEY_BBOX_LEFT_MARGIN, null);
		if (leftMargin != null) {
			if (leftMargin.intValue() != bBoxInsets.left) {
				bBoxInsets.left = leftMargin.intValue();
				needRecalc = true;
			}
			if (leftMargin.intValue() != bBoxInsets.right) {
				bBoxInsets.right = leftMargin.intValue();
				needRecalc = true;
			}
		}
		Integer whiteSpaceWidth = Preferences.getInt(IGTConstants.KEY_WHITESPACE_WIDTH, null);
		if (whiteSpaceWidth != null) {
			if (igtViewer.getViewerRenderInfo().whitespaceWidth != whiteSpaceWidth.intValue()) {
				igtViewer.getViewerRenderInfo().whitespaceWidth = whiteSpaceWidth.intValue();
				needRecalc = true;
			}
		}
		
		if (needRecalc) {
			igtViewer.recalculateAllRows(true);
		}
		//
		repaint();
	}
	
	/**
	 * Updates the location and sizes of the components.
	 */
	@Override // ComponentListener
	public void componentResized(ComponentEvent e) {
		if (e.getSource() == playerControlPanel) {
			int width = playerControlPanel.getWidth();
			igtViewer.setHeaderWidth(width + buttonSplitPane.getDividerSize() / 2);
		} else if (e.getSource() == this) {	

			int w = e.getComponent().getWidth();
			int h = e.getComponent().getHeight();
			
			buttonSplitPane.setBounds(0, 0, w, bpHeight);
			igtViewer.setBounds(0, bpHeight, w, h - bpHeight);
			//igtViewer.recalculateAllRows(); // now done instead in the viewer's componentResized().
	        validate(); // this seems needed:
	        // It should be invoked when this container's subcomponents are modified
	        // (...or layout-related information changed) after the container has
	        // been displayed.	
		}
	}

	@Override // ComponentListener
	public void componentMoved(ComponentEvent e) {
		// stub
	}

	/**
	 * Repaints.
	 */
	@Override // ComponentListener
	public void componentShown(ComponentEvent e) {
		// stub		
	}

	@Override // ComponentListener
	public void componentHidden(ComponentEvent e) {
		// stub	
	}

	@Override // ActionListener
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == interButton) {
			if (manager.getTextAnalyzerContext().isAutoAnalyzeMode()) {
				manager.getTextAnalyzerContext().setAutoAnalyzeMode(false);
				return;
			}
			// first check if there is an active annotation
			Annotation activeAnn = getActiveAnnotation();
			TierImpl activeSourceTier = null; // the tier the active annotation is on, potential source tier
			AnalyzerConfig ac = activeConfig;
			// the following list will be filled with tier configurations
			List<AnalyzerConfig> activeConfList = new ArrayList<AnalyzerConfig>();
			
			if (activeAnn != null) {
				activeSourceTier = (TierImpl) activeAnn.getTier();
				String activeTypeName = activeSourceTier.getLinguisticType().getLinguisticTypeName();
				final List<AnalyzerConfig> configurations = manager.getTextAnalyzerContext().getConfigurations();
				
				for (AnalyzerConfig acf : configurations) {
					if (acf.isTypeMode()) {
						if (acf.getSource().equals(activeTypeName)) {
							// add all tier configurations of this type? or just of the active tier?
							activeConfList.addAll(acf.getTierConfigurations());
						}
					} else {
						// one tier configuration
						if (acf.getSource().equals(activeSourceTier.getName())) {
							activeConfList.add(acf);
						}
					}
				}
			} else {
				// start with active configuration or just the first configuration in the list
				if (ac == null) {
					final List<AnalyzerConfig> configurations = manager.getTextAnalyzerContext().getConfigurations();
					if (configurations.size() > 0) {
						ac = configurations.get(0);
						activeConfList.addAll(ac.getTierConfigurations());
					}
				} else {
					activeConfList.addAll(ac.getTierConfigurations());
				}
			}			

			if (/*ac == null*/activeConfList.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						ElanLocale.getString("InterlinearEditor.Warning.NoConfig.Message"),
						ElanLocale.getString("InterlinearEditor.Warning.NoConfig.Title"),
					    JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			manager.getTextAnalyzerContext().setAutoAnalyzeMode(true);
			new AnalyzeThread(activeConfList, activeAnn).start();
		
		} else if (e.getSource() == recursiveCB) {
			manager.getTextAnalyzerContext().setRecursive(recursiveCB.isSelected());
		} else if (e.getSource() == configureButton) {
			RenderingConfigDialog dialog = new RenderingConfigDialog(
					ELANCommandFactory.getRootFrame(transcription), 
					ElanLocale.getString("InterlinearEditor.RenderDialog.Title"), true);
			dialog.setVisible(true);
			// handle the settings after closing the dialog
			applySettings(dialog.getSettings());
		}
	}
	
	/**
	 * A list of keystrokes that either shouldn't be consumed by the interlinear editor
	 * (i.e. the table of the IGTViewer). 
	 *  
	 * @param ksNotToBeConsumed a list of keystrokes that need to be handled on
	 * a higher level, i.e. the JTable should not consume these events
	 */
	public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksNotToBeConsumed) {
		igtViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
	}

	/**
	 * Applies the settings to the editor's renderer and stores or removes
	 * settings as preferences.
	 * 
	 * @param nextSettings a map with changed settings
	 */
	private void applySettings(Map<String, Object> nextSettings) {
		if (nextSettings != null) {// otherwise the dialog was cancelled
			// if a key is not present, (re-)set to default and remove from the preferences file
			Color evenRowColor = (Color) nextSettings.get(IGTConstants.KEY_BACKGROUND_COLOR_EVEN);	
			if (evenRowColor != null) {
				igtViewer.getViewerRenderInfo().backgroundColor = evenRowColor;
				Preferences.set(IGTConstants.KEY_BACKGROUND_COLOR_EVEN, evenRowColor, null);
			} else {
				igtViewer.getViewerRenderInfo().backgroundColor = IGTConstants.TABLE_BACKGROUND_COLOR1;
				Preferences.set(IGTConstants.KEY_BACKGROUND_COLOR_EVEN, null, null);
			}
			
			Color oddRowColor = (Color) nextSettings.get(IGTConstants.KEY_BACKGROUND_COLOR_ODD);
			if (oddRowColor != null) {
				igtViewer.getViewerRenderInfo().backgroundColor2 = oddRowColor;
				Preferences.set(IGTConstants.KEY_BACKGROUND_COLOR_ODD, oddRowColor, null);
			} else {
				igtViewer.getViewerRenderInfo().backgroundColor2 = IGTConstants.TABLE_BACKGROUND_COLOR2;
				Preferences.set(IGTConstants.KEY_BACKGROUND_COLOR_ODD, null, null);
			}
			
			Boolean showAnnBorders = (Boolean) nextSettings.get(IGTConstants.KEY_ANN_BORDER_VIS_FLAG);
			if (showAnnBorders != null) {
				igtViewer.getViewerRenderInfo().showAnnoBorders = showAnnBorders;
				Preferences.set(IGTConstants.KEY_ANN_BORDER_VIS_FLAG, showAnnBorders, null);
			} else {
				igtViewer.getViewerRenderInfo().showAnnoBorders = IGTConstants.SHOW_ANNOTATION_BORDER;
				Preferences.set(IGTConstants.KEY_ANN_BORDER_VIS_FLAG, null, null);
			}
			
			Boolean showAnnBackground = (Boolean) nextSettings.get(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG);
			if (showAnnBackground != null) {
				igtViewer.getViewerRenderInfo().showAnnoBackground = showAnnBackground;
				Preferences.set(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG, showAnnBackground, null);
			} else {
				igtViewer.getViewerRenderInfo().showAnnoBackground = IGTConstants.SHOW_ANNOTATION_BACKGROUND;
				Preferences.set(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG, null, null);
			}
			
			Color annBorderColor = (Color) nextSettings.get(IGTConstants.KEY_ANN_BORDER_COLOR);
			if (annBorderColor != null) {
				igtViewer.getViewerRenderInfo().annoBorderColor = annBorderColor;
				Preferences.set(IGTConstants.KEY_ANN_BORDER_COLOR, annBorderColor, null);
			} else {
				igtViewer.getViewerRenderInfo().annoBorderColor = IGTConstants.ANNO_BORDER_COLOR;
				Preferences.set(IGTConstants.KEY_ANN_BORDER_COLOR, null, null);
			}
			
			Color annBgColor = (Color) nextSettings.get(IGTConstants.KEY_ANN_BACKGROUND_COLOR);
			if (annBgColor != null) {
				igtViewer.getViewerRenderInfo().annoBackgroundColor = annBgColor;
				Preferences.set(IGTConstants.KEY_ANN_BACKGROUND_COLOR, annBgColor, null);
			} else {
				igtViewer.getViewerRenderInfo().annoBackgroundColor = IGTConstants.ANNO_BACKGROUND_COLOR;
				Preferences.set(IGTConstants.KEY_ANN_BACKGROUND_COLOR, null, null);
			}
			
			//... changes that require recalculation of sizes and positions
			boolean needRecalc = false;
			Integer topMargin = (Integer) nextSettings.get(IGTConstants.KEY_BBOX_TOP_MARGIN);
			Insets bBoxInsets = igtViewer.getViewerRenderInfo().annBBoxInsets;
			if (topMargin != null) {
				if (topMargin.intValue() != bBoxInsets.top) {
					 bBoxInsets.top = topMargin.intValue();
					 needRecalc = true;
				}
				if (topMargin.intValue() != bBoxInsets.bottom) {
					 bBoxInsets.bottom = topMargin.intValue();
					 needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_BBOX_TOP_MARGIN, topMargin, null);
			} else {// reset
				if (bBoxInsets.top != IGTConstants.TEXT_MARGIN_TOP) {
					bBoxInsets.top = IGTConstants.TEXT_MARGIN_TOP;
					needRecalc = true;
				}
				if (bBoxInsets.bottom != IGTConstants.TEXT_MARGIN_BOTTOM) {
					bBoxInsets.bottom = IGTConstants.TEXT_MARGIN_BOTTOM;
					needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_BBOX_TOP_MARGIN, null, null);
			}
			
			Integer leftMargin = (Integer) nextSettings.get(IGTConstants.KEY_BBOX_LEFT_MARGIN);
			if (leftMargin != null) {
				if (leftMargin.intValue() != bBoxInsets.left) {
					bBoxInsets.left = leftMargin.intValue();
					needRecalc = true;
				}
				if (leftMargin.intValue() != bBoxInsets.right) {
					bBoxInsets.right = leftMargin.intValue();
					needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_BBOX_LEFT_MARGIN, leftMargin, null);
			} else {
				if (bBoxInsets.left != IGTConstants.TEXT_MARGIN_LEFT) {
					bBoxInsets.left = IGTConstants.TEXT_MARGIN_LEFT;
					needRecalc = true;
				}
				if (bBoxInsets.right != IGTConstants.TEXT_MARGIN_RIGHT) {
					bBoxInsets.right = IGTConstants.TEXT_MARGIN_RIGHT;
					needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_BBOX_LEFT_MARGIN, null, null);
			}
			
			Integer whiteSpaceWidth = (Integer) nextSettings.get(IGTConstants.KEY_WHITESPACE_WIDTH);
			if (whiteSpaceWidth != null) {
				if (igtViewer.getViewerRenderInfo().whitespaceWidth != whiteSpaceWidth.intValue()) {
					igtViewer.getViewerRenderInfo().whitespaceWidth = whiteSpaceWidth.intValue();
					needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_WHITESPACE_WIDTH, whiteSpaceWidth, null);
			} else {
				if (igtViewer.getViewerRenderInfo().whitespaceWidth != IGTConstants.WHITESPACE_PIXEL_WIDTH) {
					igtViewer.getViewerRenderInfo().whitespaceWidth = IGTConstants.WHITESPACE_PIXEL_WIDTH;
					needRecalc = true;
				}
				Preferences.set(IGTConstants.KEY_WHITESPACE_WIDTH, null, null);
			}
			
			if (needRecalc) {
				igtViewer.recalculateAllRows(true);
			}
			
			repaint();
		}
	}

	/**
	 * A thread to enter a loop to sequentially perform an analyze action without 
	 * blocking the UI system and with waiting for a suggestions window to be closed
	 * before continuing.
	 */
	private class AnalyzeThread extends Thread {
		List<AnalyzerConfig> activeConfList;
		Annotation activeAnn;
		boolean interruptInternal = false;
		
		/**
		 * Constructor.
		 * 
		 * @param activeConfList the list of active configurations (containing the source tier(s))
		 * @param activeAnn the current active annotation to start the sequence of analyze actions at
		 */
		public AnalyzeThread(List<AnalyzerConfig> activeConfList, Annotation activeAnn) {
			super();
			this.activeConfList = activeConfList;
			this.activeAnn = activeAnn;
		}

		/**
		 * Enters a loop over the annotations of the tiers that are retrieved from the
		 * active configurations. 
		 */
		@Override
		public void run() {
			// it is best to follow the order of the annotations in the viewer
			if (activeAnn == null) {
				if (igtViewer.getRowCount() == 0) {
					return;
				}
				int curRow = Math.max(0, igtViewer.getSelectedRow());
				rowloop:
				for (int row = curRow; row < igtViewer.getRowCount(); row++) {
					long[] timeInterval = igtViewer.getTimeIntervalForRow(row);
					for (AnalyzerConfig conf : activeConfList) {
						TierImpl tier = transcription.getTierWithId(conf.getSource());
						if (tier != null) {
							if (igtViewer.isTierInRow(tier.getName(), row)) {
								List<Annotation> annInInterval = tier.getOverlappingAnnotations(
										timeInterval[0], timeInterval[1]);
								if (annInInterval.size() > 0) {
									setActiveAnnotation(annInInterval.get(0));
									activeAnn = getActiveAnnotation();
									break rowloop;
								}
							} 
						}
					}
				}
			}
			
			if (activeAnn != null) {
				TierImpl activeSourceTier = (TierImpl) activeAnn.getTier();
				int startRow = igtViewer.getRowIndexForTierAndTime(activeSourceTier.getName(), activeAnn.getBeginTimeBoundary());
				for (int row = startRow; row < igtViewer.getRowCount(); row++) {
					long[] timeInterval = igtViewer.getTimeIntervalForRow(row);
					if (timeInterval == null) {
						continue;//break
					}
					
					if (row == startRow) {
						List<AbstractAnnotation> annotations = activeSourceTier.getAnnotations();
						int annIndex = annotations.indexOf(activeAnn);
						for (int i = annIndex; i < annotations.size(); i++) {
							if (interruptInternal) {
								manager.getTextAnalyzerContext().setAutoAnalyzeMode(false);
								return;
							}
							Annotation a = annotations.get(i);
							
							if (a.getBeginTimeBoundary() >= timeInterval[0] && a.getEndTimeBoundary() <= timeInterval[1]) {
								Position pos = new Position(activeSourceTier.getName(),
										//pos.tierId = ac.getDestTier();
										a.getBeginTimeBoundary(),
										a.getEndTimeBoundary());
								if (a != activeAnn) {
									setActiveAnnotation(a);
								}
								manager.getTextAnalyzerContext().analyze(pos);
								waitForNext();
							} else {
								break;
							}
						}
					} else {
						// all other rows after the "current" or the start row
						// check whether next row contains a tier that is in the source configurations
						for (AnalyzerConfig conf : activeConfList) {
							TierImpl tier = transcription.getTierWithId(conf.getSource());
							if (tier != null) {
								if (igtViewer.isTierInRow(tier.getName(), row)) {
									List<Annotation> annInInterval = tier.getOverlappingAnnotations(
											timeInterval[0], timeInterval[1]);
									for (Annotation a : annInInterval) {
										if (interruptInternal) {
											manager.getTextAnalyzerContext().setAutoAnalyzeMode(false);
											return;
										}
										Position pos = new Position(conf.getSource(),
												//pos.tierId = ac.getDestTier();
												a.getBeginTimeBoundary(),
												a.getEndTimeBoundary());
										
										if (a != activeAnn) {
											setActiveAnnotation(a);
										}
										
										manager.getTextAnalyzerContext().analyze(pos);
										waitForNext();
									}
								}
							}
						}
					}
				}
			} else {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("No active annotation to start automatic interlinearization.");
				}
			}
			manager.getTextAnalyzerContext().setAutoAnalyzeMode(false);
		}
		
		/**
		 * First gives the suggestion window some time to be opened and then waits/blocks
		 * until it is closed. It checks whether the current suggestion was canceled and,
		 * if so, ends the 'automatic', sequential analyze action.
		 */
		private void waitForNext() {
			try {
				Thread.sleep(300);
				while (manager.getTextAnalyzerContext().isPositionPending()) {
					Thread.sleep(300);
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer("Waiting for the suggestion window to close");
					}
				}
			} catch (Throwable t){
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("Error while waiting for the suggestions window to be closed: " + t.getMessage());
				}
			}
			// is the automatic analyze mode still active or has it been canceled
			if (!manager.getTextAnalyzerContext().isAutoAnalyzeMode()) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer("Auto Analyze mode has been canceled");
				}
				interruptInternal = true;
			}
		}
	}
}
