package mpi.eudico.client.annotator.grid;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.tiersets.TierSetUtil;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ConcatAnnotation; //Added Coralie Villes
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.event.ACMEditEvent;

/**
 * This class adds functionality for showing all annotations of one tier plus
 * corresponding annotations on children tiers
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class GridViewer extends AbstractEditableGridViewer
    implements SingleTierViewer, ActionListener, InlineEditBoxListener{
    public static final int SINGLE_TIER_MODE = 0;
    //add association and subdivision mode to see every dependence between tiers mod. Coralie Villes
    public static final int MULTI_TIER_ASSOCIATION_MODE = 1;
    public static final int MULTI_TIER_SUBDIVISION_MODE = 2;
    
    public static final int MULTI_TIER_MODE = 1;
    private int mode = SINGLE_TIER_MODE;

    /** Holds value of property DOCUMENT ME! */
    private static final AnnotationCore EMPTY = new AnnotationCoreImpl("", -1, -1);
    private TierImpl tier;

    /**
     * Holds visibility values for the count, begintime, endtime and duration
     * columns in multi tier mode
     */
    private List<TierImpl> childTiers = new ArrayList<TierImpl>();

    /**
     * Stores the names of child tiers the moment they are added to the table.
     * This 'old' name can then be used to find the right column after a
     * change  of the tier name.
     */
    private Map<TierImpl, String> childTierNames = new HashMap<TierImpl, String>();
    private Set<Object> storedInvisibleColumns = new HashSet<Object>();

    /**
     * Constructor
     */
    public GridViewer() {
        super(new AnnotationTable(new GridViewerTableModel()));
        
        // register with popup menu for user prefs changes
        if (popup != null) {
        	popup.addActionListener(this);
        }
        // default for MultiTier-View
        storedInvisibleColumns.add(GridViewerTableModel.BEGINTIME);
        storedInvisibleColumns.add(GridViewerTableModel.ENDTIME);
        storedInvisibleColumns.add(GridViewerTableModel.DURATION);
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotations DOCUMENT ME!
     */
    @Override
	public void updateDataModel(List<? extends AnnotationCore> annotations) {
        removeChildrenColumns();
        childTiers.clear();
        childTierNames.clear();
        super.updateDataModel(annotations);
    }

    /**
     * To be called when the viewer is closing
     */
    @Override
	public void isClosing(){
    	if(table != null){
    		if(table.isEditing() && gridEditor != null){
    			Boolean val = Preferences.getBool("InlineEdit.DeselectCommits", null);
    			if (val != null && !val) {
    				gridEditor.cancelCellEditing();
    			} else {   				
    				gridEditor.commitEdit();
    			}
    		}
    	}
    }
    
    /**
     * Checks the kind of edit that has happened and updates the table when
     * necessary.
     *
     * @param e the ACMEditEvent
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        if (tier == null) {
            return;
        }

        switch (e.getOperation()) {
        case ACMEditEvent.ADD_ANNOTATION_HERE:

            if (isCreatingAnnotation) {
                // if the new annotation is created by this gridviewer return
                isCreatingAnnotation = false;

                return;
            }

        // fallthrough
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:

        // fallthrough
        case ACMEditEvent.ADD_ANNOTATION_AFTER:

        // fallthrough  
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:

            // TierImpl invTier = (TierImpl) e.getInvalidatedObject();
            // annotationsChanged(invTier);
            // jul 2004: redo all; we can not rely on the fact that only
            // dependent
            // tiers will be effected by this operation...
            // (problem: unaligned annotations on time-subdivision tiers)
            annotationsChanged(null);

            break;

        case ACMEditEvent.CHANGE_ANNOTATIONS:

        // fallthrough
        case ACMEditEvent.REMOVE_ANNOTATION:

            // it is not possible to determine what tiers have been effected
            // update the whole data model
            annotationsChanged(null);

            break;

        case ACMEditEvent.CHANGE_TIER:

            // a tier is invalidated the kind of change is unknown
            TierImpl invalTier = (TierImpl) e.getInvalidatedObject();
            tierChanged(invalTier);

            break;

        case ACMEditEvent.ADD_TIER:

        // fallthrough
        case ACMEditEvent.REMOVE_TIER:

            TierImpl ti = (TierImpl) e.getModification();
            tierChanged(ti);

            break;

        default:
            super.ACMEdited(e);
        }
    }

    /**
     * If a change in the specified tier could have effected any of the tiers
     * in the table rebuild the table data model entirely. The change could be
     * a change in the name, or in the tier hierarchy or whatever.
     *
     * @param changedTier the invalidated tier
     */
    private void tierChanged(TierImpl changedTier) {
        if (mode == SINGLE_TIER_MODE) {
            if (changedTier == tier) {
                setTier(changedTier);
            }
        } else {
            setTier(tier);
        }
    }

    /**
     * Sets the tier to the selected tier in the combobox.
     *
     * @param tier the current tier for the grid/table
     */
    @Override
	public void setTier(Tier tier) {
        // stop editing
        gridEditor.cancelCellEditing();

        this.tier = (TierImpl) tier;

        // added by AR
        if (tier == null) {
            updateDataModel(new ArrayList<AnnotationCore>());
            table.setFontsForTiers(null);
            setPreference("GridViewer.TierName", tier, 
            		getViewerManager().getTranscription());
        } else {
            List<AbstractAnnotation> annotations = null;

            try {
                annotations = this.tier.getAnnotations();
            } catch (Exception ex) {
                LOG.warning("Could not get the annotations: " + ex.getMessage());
            }

            updateDataModel(annotations);

          //mod. Coralie Villes add the possibility of showing associated tiers or subdivised tiers
            if (mode == MULTI_TIER_ASSOCIATION_MODE) {
            	extractChildTiers(this.tier, Constraint.SYMBOLIC_ASSOCIATION);
                addExtraColumns();
            }
            if (mode == MULTI_TIER_SUBDIVISION_MODE) {
                extractChildTiers(this.tier, Constraint.SYMBOLIC_SUBDIVISION);
                addExtraColumns();
            }
            
            setPreference("GridViewer.TierName", tier.getName(), 
            		getViewerManager().getTranscription());
            preferencesChanged();
        }

        updateSelection();
        doUpdate();
    }

    /**
     * In multi tier mode finds those child tiers of the current tier that have
     * a LinguisticType that answer to the Constraint given in parameter. These tiers
     * appear in the table as an extra column.
     *
     * @param tier current tier
     * @param int constraint linguistic type of the tier (symbolic_association or symbolic_subdivision)
     */
    protected void extractChildTiers(TierImpl tier, int constraint) {
    	if (tier != null) {
        	List<TierImpl> depTiers=tier.getDependentTiers();
        	
        	Boolean workWithTierSetsPref = Preferences.getBool("WorkwithTierSets", null);
        	Boolean workWithTierSets = false;
        	if(workWithTierSetsPref != null) { workWithTierSets = workWithTierSetsPref; }
        	
        	List<String> tiersInTierSets = TierSetUtil.getTierSetUtilInstance().getVisibleTiers();

            for (TierImpl t : depTiers) {
                if (t.getParentTier() == tier && (!workWithTierSets || tiersInTierSets.contains(t.getName()))) {
                    if (t.getLinguisticType().getConstraints().getStereoType() == constraint) {
                        childTiers.add(t);
                        childTierNames.put(t, t.getName());
                        extractChildTiers(t, constraint);
                    }
                }
            }
        }
    }

    /**
     * Update method from ActiveAnnotationUser.
     */
    @Override
	public void updateActiveAnnotation() {
        if (tier == null) {
            return;
        }

        if (getActiveAnnotation() == null) {
            repaint();

            return;
        }

        if (mode == SINGLE_TIER_MODE) {
            super.updateActiveAnnotation();
        } else {
            if ((getActiveAnnotation().getTier() != tier) &&
                    !childTiers.contains(getActiveAnnotation().getTier())) {
                repaint();

                return;
            }
        }

        doUpdate();
    }

    /**
     * DOCUMENT ME!
     */
    protected void addExtraColumns() {
        if (childTiers.size() == 0) {
            return;
        }

        Tier tierChild = null;

        // update vector with extra columns
        int vecChildren_size = childTiers.size();

        for (int i = 0; i < vecChildren_size; i++) {
            tierChild = childTiers.get(i);
            handleExtraColumn(tierChild);
        }
    }

    private void handleExtraColumn(Tier childTier) {
        try {
            List<AnnotationCore> v = null;
            if (mode == MULTI_TIER_ASSOCIATION_MODE) {
            	v = createChildAnnotationList(childTier);
            } else if (mode == MULTI_TIER_SUBDIVISION_MODE) {
            	v = createChildAnnotationListS(childTier);
            }
            String name = childTier.getName();
            dataModel.addChildTier(name, v);

            int columnIndex = dataModel.findColumn(name);
            TableColumn tc = new TableColumn();
            tc.setHeaderValue(name);
            tc.setIdentifier(name);
            table.addColumn(tc);

            int curIndex = table.getColumnModel().getColumnIndex(name);
            table.moveColumn(curIndex, columnIndex);
            updateColumnModelIndices();
            table.setColumnVisible(name, true);
        } catch (Exception ex) {
            LOG.warning("Could not handle the extra column for the child tier: " + ex.getMessage());
        }
    }

    /**
     * Fills a List for a child tier with the same size as the parent tier's
     * annotations List. At the indices where the childtier has no child
     * annotation an annotation with an empty String is inserted.
     *
     * @param childTier the dependent tier
     * Changed : Coralie Villes to allow displaying children of subdivision tiers
     */
    private List<AnnotationCore> createChildAnnotationListS(Tier childTier) {
    	List<AnnotationCore> cv = new ArrayList<AnnotationCore>(dataModel.getRowCount());

    	List<? extends AnnotationCore> existingChildren = ((TierImpl) childTier).getAnnotations();
    	long begin;
    	long end;

    	// get begin and end time per row and check
    	List<AnnotationCore> annotationList = new ArrayList<AnnotationCore>();
		int k = 0;
		for (int j = 0; j < dataModel.getRowCount(); j++) {
    		AnnotationCore annotation = dataModel.getAnnotationCore(j);
    		begin = annotation.getBeginTimeBoundary();
    		end = annotation.getEndTimeBoundary();
    		
    		for (; k < existingChildren.size(); k++) {
    			AnnotationCore child = existingChildren.get(k);
    			
        		if (child.getBeginTimeBoundary() >= begin && child.getEndTimeBoundary() <= end){
        			annotationList.add(child);
        		} else if (child.getBeginTimeBoundary() >= end) {
        			if (annotationList.size() > 0) {
        				cv.add(new ConcatAnnotation(annotationList));
        				annotationList.clear();
        			} else {
        				cv.add(EMPTY);
        			}
        			
        			break;
        		}
    		}
    		// when reaching the last of existing children test if there is something in the list
    		// k can be == to the size as a result of the increment at the end of the block
    		if (k == existingChildren.size() && annotationList.size() > 0) {
    			cv.add(new ConcatAnnotation(annotationList));
    			annotationList.clear();// not necessary
    		}
    		if (cv.size() < j) {
    			cv.add(EMPTY);
    		}
		}
    	return cv;
    }
    
    /**
     * Fills a List for a child tier with the same size as the parent tier's
     * annotations List. At the indices where the childtier has no child
     * annotation an annotation with an empty String is inserted.
     * <p>
     * The List will be used as a column in the grid view.
     *
     * @param childTier the dependent tier
     *
     * @return a List filled with child annotations and/or empties
     */
    private List<AnnotationCore> createChildAnnotationList(Tier childTier) {
        List<AnnotationCore> cv = new ArrayList<AnnotationCore>(dataModel.getRowCount());

        List<? extends AnnotationCore> existingChildren = ((TierImpl) childTier).getAnnotations();
        AnnotationCore annotation;
        AnnotationCore childAnnotation;
        long begin;

        for (int i = 0, j = 0; i < dataModel.getRowCount(); i++) {
            annotation = dataModel.getAnnotationCore(i);
            begin = annotation.getBeginTimeBoundary();

            if (j < existingChildren.size()) {
                childAnnotation = existingChildren.get(j);

                if (childAnnotation.getBeginTimeBoundary() == begin) {
                    cv.add(childAnnotation);
                    j++;
                } else {
                    cv.add(EMPTY);
                }
            } else {
                cv.add(EMPTY);
            }
        }

        return cv;
    }

    /**
     * Sets the edit mode. On a change of the edit mode the current visible
     * columns are stored and the previous visible columns are restored.
     *
     * @param mode the new edit mode, one of SINGLE_TIER_MODE or
     *        MULTI_TIER_ASSOCIATION_MODE or MULTI_TIER_SUBDIVISION_MODE
     */
    public void setMode(int mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;

        Set<Object> invisibleColumns = getInvisibleColumns();
        setInvisibleColumns(storedInvisibleColumns);
        storedInvisibleColumns = invisibleColumns;
        
        setPreference("GridViewer.MultiTierMode", 
        		Boolean.valueOf(mode == MULTI_TIER_ASSOCIATION_MODE || mode == MULTI_TIER_SUBDIVISION_MODE), 
        		getViewerManager().getTranscription());
        if (mode == MULTI_TIER_SUBDIVISION_MODE) {
        	setPreference("GridViewer.MultiTierMode.Subdivision", Boolean.TRUE, 
        			getViewerManager().getTranscription());
        } else if (mode == MULTI_TIER_ASSOCIATION_MODE) {// don't change the Subdivision preference if the mode is SINGLE_TIER
        	setPreference("GridViewer.MultiTierMode.Subdivision", Boolean.FALSE, 
        			getViewerManager().getTranscription());
        }
    }
    
    /**
     * For initialization from the preferences. Sets the mode field without updating anything else.
     * 
     * @param mode the new edit mode, one of SINGLE_TIER_MODE or
     *        MULTI_TIER_ASSOCIATION_MODE or MULTI_TIER_SUBDIVISION_MODE
     */
    public void setModeFromPref(int mode) {
    	this.mode = mode;
    }
    
    /**
     * Returns the current display mode.
     * 
     * @return the current display mode, SINGLE_TIER_MODE, MULTI_TIER_ASSOCIATION_MODE or MULTI_TIER_SUBDIVISION_MODE
     */
    public int getMode() {
    	return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Set<Object> getInvisibleColumns() {
        Set<Object> invisibleColumns = new HashSet<Object>();
        TableColumn tc;
        invisibleColumns.clear();

        for (int i = 0; i < table.getColumnCount(); i++) {
            tc = table.getColumnModel().getColumn(i);

            if (!table.isColumnVisible((String) tc.getIdentifier())) {
                invisibleColumns.add(tc.getIdentifier());
            }
        }

        return invisibleColumns;
    }

    /**
     * DOCUMENT ME!
     *
     * @param invisibleColumns DOCUMENT ME!
     */
    protected void setInvisibleColumns(Set<Object> invisibleColumns) {
        TableColumn tc;

        for (int i = 0; i < table.getColumnCount(); i++) {
            tc = table.getColumnModel().getColumn(i);
            table.setColumnVisible(dataModel.getColumnName(i),
                !invisibleColumns.contains(tc.getIdentifier()));
        }
    }

    /**
     * Check whether the invalidated tier is displayed in the table and update
     * the table if so.
     *
     * @param invTier the invalidated tier
     */
    protected void annotationsChanged(TierImpl invTier) {
        if ((invTier == null) || (invTier == tier) ||
                invTier.getDependentTiers().contains(tier) ||
                childTiers.contains(invTier)) {
            List<AbstractAnnotation> annotations = tier.getAnnotations();
            dataModel.updateAnnotations(annotations);

            for (int i = 0; i < childTiers.size(); i++) {
                Tier childTier = childTiers.get(i);
                List<AnnotationCore> vec = null;
                if (mode == MULTI_TIER_ASSOCIATION_MODE) {
                	vec = createChildAnnotationList(childTier);
                } else if (mode == MULTI_TIER_SUBDIVISION_MODE) {
                	vec = createChildAnnotationListS(childTier);
                }
                dataModel.addChildTier(childTier.getName(), vec);
            }

            updateSelection();
            doUpdate();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void removeChildrenColumns() {
        if (childTiers.size() > 0) {
            for (int i = 0; i < childTiers.size(); i++) {
                TierImpl t = childTiers.get(i);
                String columnID = childTierNames.get(t);

                try {
                    table.removeColumn(table.getColumn(columnID));
                    updateColumnModelIndices();
                } catch (IllegalArgumentException iae) {
                    LOG.warning("Column not found: " + iae.getMessage());
                }
            }
        }
    }

    /**
     * When adding/removing and/or moving a table column the table column model
     * indices don't seem to be updated automatically.
     */
    private void updateColumnModelIndices() {
        Enumeration ten = table.getColumnModel().getColumns();
        TableColumn tabcol = null;
        int tableIndex;

        while (ten.hasMoreElements()) {
            tabcol = (TableColumn) ten.nextElement();
            tableIndex = table.getColumnModel().getColumnIndex(tabcol.getIdentifier());
            tabcol.setModelIndex(tableIndex);
        }
    }

    /**
     * Gets the tier which is shown in the subtitle viewer
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Tier getTier() {
        return tier;
    }
    
    /**
     * Apply fontsize (tier name and multi mode are handled by layout manager).
     * 
	 * @see mpi.eudico.client.annotator.grid.AbstractEditableGridViewer#preferencesChanged()
	 */
    @Override
	public void preferencesChanged() {
    	super.preferencesChanged(); // get color preferences
    	
		final Transcription transcription = getViewerManager().getTranscription();
		
		Integer fontSi = Preferences.getInt("GridViewer.FontSize", 
				transcription);
		if (fontSi != null) {
			setFontSize(fontSi.intValue());
		}
		// preferred fonts
		Map<String, Font> foMap = Preferences.getMapOfFont("TierFonts", transcription);
		if (foMap != null && tier != null) {
			Map<String, String> gridMap = new HashMap<String, String>(5);
			
			for (Map.Entry<String, Font> e : foMap.entrySet()) {
				String key = e.getKey();
				Font ft = e.getValue();
				
				if (key != null && ft != null) {
					if (key.equals(tier.getName())) {
						gridMap.put(GridViewerTableModel.ANNOTATION, ft.getName());
					} 
					//else if (childTierNames.containsKey(key)) {
						gridMap.put(key, ft.getName());
					//}
				}
			}
			table.setFontsForTiers(gridMap);
		}
		//
		Boolean boolPref = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (boolPref != null) {
            gridEditor.setEnterCommits(boolPref.booleanValue());
        }
        
        boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);

        if (boolPref != null) {
            table.setDeselectCommits(boolPref.booleanValue());
        } else {
        	table.setDeselectCommits(true);
        }
        
        String stringPref = Preferences.getString("GridViewer.TimeFormat", transcription);
        
        if (stringPref != null) {
        	table.setTimeFormat(stringPref);
        	
        	if (popup != null) {
        		popup.setTimeFormat(stringPref);
        	}
        }
        
        boolPref = Preferences.getBool("GridViewer.ColumnBeginTime.Visible", transcription);
        if (boolPref != null) {
        	boolean curVis = table.isColumnVisible(GridViewerTableModel.BEGINTIME);
        	boolean futVis = boolPref;
        	if (curVis != futVis) {
        		table.setColumnVisible(GridViewerTableModel.BEGINTIME, futVis);
        	}
        }
        
        boolPref = Preferences.getBool("GridViewer.ColumnEndTime.Visible", transcription);
        if (boolPref != null) {
        	boolean curVis = table.isColumnVisible(GridViewerTableModel.ENDTIME);
        	boolean futVis = boolPref;
        	if (curVis != futVis) {
        		table.setColumnVisible(GridViewerTableModel.ENDTIME, futVis);
        	}
        }
        
        boolPref = Preferences.getBool("GridViewer.ColumnDuration.Visible", transcription);
        if (boolPref != null) {
        	boolean curVis = table.isColumnVisible(GridViewerTableModel.DURATION);
        	boolean futVis = boolPref;
        	if (curVis != futVis) {
        		table.setColumnVisible(GridViewerTableModel.DURATION, futVis);
        	}
        }
        
		doLayout();
	}

	/**
	 * The viewer is registered with the popup menu in order to be notified of 
	 * user preferences changes.
	 * 
	 * @param e the event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("TOGGLETIMEFORMAT")) {
	        if (dataModel instanceof GridViewerTableModel) {
	            String timeFormat = dataModel.getTimeFormat();

	            if (GridViewerTableModel.HHMMSSsss.equals(timeFormat)) {
	                setPreference("GridViewer.TimeFormat",
	                    Constants.HHMMSSMS_STRING, getViewerManager().getTranscription());
	            } else if (GridViewerTableModel.MILLISECONDS.equals(timeFormat)) {
	                setPreference("GridViewer.TimeFormat",
	                    Constants.MS_STRING, getViewerManager().getTranscription());
	            } else {
	                setPreference("GridViewer.TimeFormat", null, 
	                		getViewerManager().getTranscription());
	            }
	        }
		} else if (e.getActionCommand().equals(Constants.HHMMSSMS_STRING)) {
			 setPreference("GridViewer.TimeFormat",
	                   Constants.HHMMSSMS_STRING, getViewerManager().getTranscription());
		} else if (e.getActionCommand().equals(Constants.PAL_STRING)) {
			 setPreference("GridViewer.TimeFormat",
					 "PAL", getViewerManager().getTranscription());			
		} else if (e.getActionCommand().equals(Constants.NTSC_STRING)) {
			setPreference("GridViewer.TimeFormat",
					 "NTSC", getViewerManager().getTranscription());			
		} else if (e.getActionCommand().equals(Constants.MS_STRING)) {
			setPreference("GridViewer.TimeFormat",
	                   Constants.MS_STRING, getViewerManager().getTranscription());		
		} else if (e.getActionCommand().equals(Constants.SSMS_STRING)) {
			setPreference("GridViewer.TimeFormat",
	                   Constants.SSMS_STRING, getViewerManager().getTranscription());		
		} else if (e.getActionCommand().equals(GridViewerTableModel.BEGINTIME)) {
			if (e.getSource() instanceof AbstractButton) {
				boolean selected = ((AbstractButton) e.getSource()).isSelected();
				setPreference("GridViewer.ColumnBeginTime.Visible",
		                   Boolean.valueOf(selected), getViewerManager().getTranscription());
			}					
		} else if (e.getActionCommand().equals(GridViewerTableModel.ENDTIME)) {
			if (e.getSource() instanceof AbstractButton) {
				boolean selected = ((AbstractButton) e.getSource()).isSelected();
				setPreference("GridViewer.ColumnEndTime.Visible",
						Boolean.valueOf(selected), getViewerManager().getTranscription());
			}					
		} else if (e.getActionCommand().equals(GridViewerTableModel.DURATION)) {
			if (e.getSource() instanceof AbstractButton) {
				boolean selected = ((AbstractButton) e.getSource()).isSelected();
				setPreference("GridViewer.ColumnDuration.Visible",
						Boolean.valueOf(selected), getViewerManager().getTranscription());
			}					
		} else if (e.getActionCommand().indexOf("font") != -1) {
			setPreference("GridViewer.FontSize", Integer.valueOf(table.getFontSize()),
					getViewerManager().getTranscription());
		}
		
	}
	
//	public void editingInterrupted() {
//		isClosing();		
//	}
	
	 public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){
	    	gridEditor.setKeyStrokesNotToBeConsumed(ksList);
	    }
	
	@Override
	public void editingCommitted() {
		 if (table != null && table.isEditing()) {
			 table.editingStopped(new ChangeEvent(this));
	     }
	}

	@Override
	public void editingCancelled() {
		if (table != null && table.isEditing()) {
           table.editingCanceled(new ChangeEvent(this));
       }
	}
}
