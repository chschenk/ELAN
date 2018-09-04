package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.TierOrder;
import mpi.eudico.client.annotator.TierOrderListener;
import mpi.eudico.client.annotator.gui.MenuScroller;
import mpi.eudico.client.annotator.tiersets.TierSet;
import mpi.eudico.client.annotator.tiersets.TierSetListener;
import mpi.eudico.client.annotator.tiersets.TierSetUtil;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;


/**
 * A viewer that reflects the annotation density and distribution over
 * the media duration by short vertical lines.
 *
 * @author MP
 */
@SuppressWarnings("serial")
public class AnnotationDensityViewer extends AbstractViewer
    implements ACMEditListener, MouseListener, ActionListener, TierSetListener {
    private static Dimension SLIDERDIMENSION = new Dimension(600, 28);
    
    private static String N_A = "-";

    /** Holds value of property DOCUMENT ME! */
    final private int imageHeight = 5;
    private int imageWidth;
    private TranscriptionImpl transcription;
    private BufferedImage bi;
    private Graphics2D big2d;
    private BufferedImage displayImage;

    /** Enum type to specify the mode of the AnnotationDensityViewer */
    private enum ViewMode {
    	SHOW_ALL, SHOW_BY_TIER, SHOW_BY_TYPE, SHOW_BY_ANNOTATOR, SHOW_BY_PARTICIPANT, SHOW_BY_LANGUAGE, 
    	SHOW_BY_TIERSET
    }
    
    // tiers popup menu
    private JPopupMenu popup;
    private JMenu tierSubMenu;
    private List<JCheckBoxMenuItem> tierCheckBoxes;
    private JMenu annotatorSubMenu;
    private List<JCheckBoxMenuItem> annotatorCheckBoxes;
	private JMenu participantSubMenu;
    private List<JCheckBoxMenuItem> participantCheckBoxes;
    private JMenu linguisticTypeSubMenu;
    private List<JCheckBoxMenuItem> linguisticTypeCheckBoxes;
    private JMenu languageSubMenu;
    private List<JCheckBoxMenuItem> languageCheckBoxes;
    private JMenu tiersetSubMenu;
    private List<JCheckBoxMenuItem> tiersetCheckBoxes;
	
    private JRadioButtonMenuItem allAnnotationsRB;
    
    /* viewMode is used to specify which type of annotations should be displayed*/
    private ViewMode viewMode = ViewMode.SHOW_ALL;
    /* viewModeSearchStrings is used to specify a list of names of the {tier_name,
     * participant_name, annotator_name, type_name, language} only these are shown */
    private List<String> viewModeKeywords = new ArrayList<String>();
    
    private TierOrder tierOrder;



    /**
     * Constructor
     *
     * @param transcription the transcription containing the annotations
     */
    public AnnotationDensityViewer(TranscriptionImpl transcription) {
        super();

        this.transcription = transcription;

        setPreferredSize(SLIDERDIMENSION);
        
        TierSetUtil.getTierSetUtilInstance().addTierSetListener(transcription, this);

        paintBuffer();
    }
    
    public void setTierOrderObject(TierOrder tierOrder){
    	this.tierOrder = tierOrder;
    	
    	tierOrder.addTierOrderListener(new TierOrderListener(){
    		@Override
			public void updateTierOrder(List<String> tierOrder) {
    			createPopupMenu();
			}
        });
    }

    /**
     * Returns minimum size of the slider
     * See also getPreferredSize
     *
     * @return the minimum size
     */
    @Override
	public Dimension getMinimumSize() {
    	if (isMinimumSizeSet()) {
    		return super.getMinimumSize();
    	}
        return SLIDERDIMENSION;
    }

    /**
     * Returns minimum size of the slider
     * See also getMinimumSize
     *
     * @return the preferred size
     */
    @Override
	public Dimension getPreferredSize() {
    	if (isPreferredSizeSet()) {
    		return super.getPreferredSize();
    	}
        return SLIDERDIMENSION;
    }

    private void paintBuffer() {
        if (getWidth() > 0) {
            imageWidth = getWidth();
        } else {
            imageWidth = SLIDERDIMENSION.width;
        }

        if ((bi == null) || (bi.getWidth() != imageWidth) ||
                (bi.getHeight() != imageHeight)) {
            bi = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            big2d = bi.createGraphics();
        }

        //paint the background color
        big2d.setColor(Color.lightGray);

        //complete rectangle
        big2d.fillRect(0, 0, imageWidth, imageHeight);
        big2d.setColor(Color.white);

        //upper line
        big2d.fillRect(0, 0, imageWidth, 1);

        //left line
        big2d.drawLine(0, 0, 0, imageHeight);
        big2d.setColor(Color.darkGray);

        //lower line
        big2d.fillRect(0, imageHeight - 1, imageWidth, 1);

        //paint the vertical line where the annotations are
        big2d.setColor(Color.darkGray.brighter());// 91, 91, 91

        int duration = (int) getMediaDuration();

        if (duration == 0) {
            displayImage = bi;
            repaint();

            return;
        }
        
        updateAnnotationFilter();

        displayImage = bi;
        repaint();
    }
    /**
     * This private method redraws all annotations on the AnnotationDurationViewer, based
     * on the viewMode and the keywords in viewModeKeywords
     */
    private void updateAnnotationFilter()
    {
    	List<Annotation> filtered_annotations = new ArrayList<Annotation>();
    	List<TierImpl> allTiers = transcription.getTiers();
    	// Iterate over all selected tiers and add their annotations.
    	
    	TierImpl.ValueGetter getter = null;

        switch(viewMode) {
        case SHOW_BY_TIER:
        	for(String tierName:viewModeKeywords) {
            	TierImpl tier = transcription.getTierWithId(tierName);
                if (tier != null) {
                	for(Annotation a : tier.getAnnotations()) {
                		filtered_annotations.add(a);
                	}
                }
        	}
        	break;
        case SHOW_BY_ANNOTATOR:
        	getter = new TierImpl.AnnotatorGetter();
        	break;
        case SHOW_BY_PARTICIPANT:
        	getter = new TierImpl.ParticipantGetter();
        	break;
        case SHOW_BY_TYPE:
        	getter = new TierImpl.LinguisticTypeNameGetter();
        	break;
        case SHOW_BY_LANGUAGE:
        	getter = new TierImpl.LanguageGetter();
        	break;        	
        case SHOW_BY_TIERSET:
        	for(String tierset: viewModeKeywords) {
        		List<String> tiersInTierset = TierSetUtil.getTierSetUtilInstance().getTierSet(tierset).getVisibleTierList();
        		for(String tierName: tiersInTierset) {
        			TierImpl tier = transcription.getTierWithId(tierName);
                    if (tier != null) {
                    	for(Annotation a : tier.getAnnotations()) {
                    		filtered_annotations.add(a);
                    	}
                    }
        		}
        	}
        	break;  
        case SHOW_ALL:
        default:
	    	for(TierImpl t:allTiers) {
	    		for (Annotation a : t.getAnnotations()) {
	        		filtered_annotations.add(a);
	    		}
	    	}
	    	break;
        }
        
        if (getter != null) {
        	for (TierImpl t : allTiers) {
	    		String value = getter.getSortValue(t);
	    		if(viewModeKeywords.contains(value)) {
	    			for(Annotation a : t.getAnnotations()) {
	            		filtered_annotations.add(a);
	    			}
	    		}
        	}
        }
    	
    	// Iterate over all annotations and draw their lines.
    	for(Annotation annotation:filtered_annotations) {
            int begintime = (int) annotation.getBeginTimeBoundary();
            int endtime = (int) annotation.getEndTimeBoundary();

            int midtime = (begintime + endtime) / 2;

            int intx = (int) (imageWidth * ((float) midtime / getMediaDuration()));
            big2d.drawLine(intx, 0, intx, imageHeight);
        }
    	/*
    	// Iterate over all annotations and draw their lines.
    	int[] cols = new int[imageWidth];
    	for(Annotation annotation:filtered_annotations) {
    		
    		float msPerPixel = (float) getMediaDuration() / imageWidth;
            int begintime = (int) annotation.getBeginTimeBoundary();
            int endtime = (int) annotation.getEndTimeBoundary();
            int bx = (int) (begintime / msPerPixel);
            int ex = (int) (endtime / msPerPixel);
            
            for (int i = bx; i <= ex && i < cols.length; i++) {
            	cols[i]++;
            }
        }
    	
    	int maxPix = 0;
    	for (int i = 0; i < cols.length; i++) {
    		if (cols[i] > maxPix) {
    			maxPix = cols[i];
    		}
    	}
    	Color c;
    	// from 192 to 64, steps of 20? 122 in 6 steps of 20 or 8 steps of 15?
    	//System.out.println("Max num ann per pixel: " + maxPix);
    	if (maxPix <= 6){
    		// steps of 20
	    	for (int i = 0; i < cols.length; i++) {
	    		if (cols[i] == 0) {
	    			continue;
	    		}
	    		//int val = Math.max(64, 192 - (cols[i] * 15));
	    		int val = 192 - (int) (20 * cols[i]);
	    		c = new Color(val ,val, val);
	    		big2d.setColor(c);
	    		big2d.drawLine(i, 1, i, imageHeight - 2);
	    	}
    	} else {
    		// use factor    	
	    	float factor = (192 - 64) / (float) maxPix;
	    	//System.out.println("Factor: " + factor);
	    	for (int i = 0; i < cols.length; i++) {
	    		if (cols[i] == 0) {
	    			continue;
	    		}
	    		//int val = Math.max(64, 192 - (cols[i] * 15));
	    		int val = 192 - (int) (factor * cols[i]);
	    		c = new Color(val ,val, val);
	    		big2d.setColor(c);
	    		big2d.drawLine(i, 1, i, imageHeight - 2);
	    	}
    	}
    	*/
    }

    /**
     * Handles the painting of the slider Consists of a vertical bar, a
     * crosshair and maybe a selection
     *
     * @param g the graphics object
     */
    @Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getWidth() != imageWidth) {
            paintBuffer();
        }

        Graphics2D g2d = (Graphics2D) g;

        if (displayImage != null) {
            g2d.drawImage(displayImage, 0, 15, this);
        }
    }

    /**
     * Creates a popup menu, with a radio button for each tier and one for
     * all tiers.
     * 
     * The "Show All" checkmark may only be selected by the user, never deselected.
     * It is indirectly deselected if any of the other options is selected.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu("Density");
        allAnnotationsRB = new JRadioButtonMenuItem(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ShowAllTiers"));
        allAnnotationsRB.setSelected(true);
		allAnnotationsRB.setEnabled(false);
        allAnnotationsRB.addActionListener(this);
        popup.add(allAnnotationsRB);
        popup.addSeparator();
        
        tierSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuTier"));
        annotatorSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuAnnotator"));
        participantSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuParticipant"));
        linguisticTypeSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuLinguisticType"));
        languageSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuLanguage"));
        tiersetSubMenu = new JMenu(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuTierSet"));
        
        MenuScroller.setScrollerFor(tierSubMenu);
        MenuScroller.setScrollerFor(linguisticTypeSubMenu);
               
        if (transcription != null) {
        	List<TierImpl> allTiers;
        	if(tierOrder!= null && tierOrder.getTierOrder()!= null){
        		List<String> tierOrderList = tierOrder.getTierOrder();
        		allTiers = new ArrayList<TierImpl>();
        		for(int i= 0; i < tierOrderList.size(); i++){
        			TierImpl t = transcription.getTierWithId(tierOrderList.get(i));
        			if(t != null && !allTiers.contains(t)){
        				allTiers.add(t);
        			}
        		}
        	} else {
        		allTiers = transcription.getTiers();
        	}
            
            /* Add tier sub menu */
            tierCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem tierCheckBoxItem;
            /* Add annotator sub menu */
            annotatorCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem annotatorCheckBoxItem;
            Set<String> annotators = new HashSet<String>();
            /* Add participant sub menu */
            participantCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem participantCheckBoxItem;
            Set<String> participants = new HashSet<String>();
            /* Add linguistic type sub menu */
            linguisticTypeCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem linguisticTypeCheckboxItem;
            /* Add language sub menu */
            languageCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem languageCheckboxItem;
            Set<String> languages = new HashSet<String>();
            /* Add tier set sub menu */
            tiersetCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
            JCheckBoxMenuItem tiersetCheckBoxItem;
            
            for (TierImpl tier:allTiers) {
            	/* Create tier sub menu item */
                String name = tier.getName();
                tierCheckBoxItem = new JCheckBoxMenuItem(name);
                tierCheckBoxItem.setActionCommand("TIER_"+name);
                tierCheckBoxItem.addActionListener(this);
                tierCheckBoxes.add(tierCheckBoxItem);
                tierSubMenu.add(tierCheckBoxItem);
                
                /* Add annotator */
                if (!tier.getAnnotator().isEmpty()) {
                	annotators.add(tier.getAnnotator());
                } else {
                	annotators.add(N_A);
                }
                /* Add participant */
                if (!tier.getParticipant().isEmpty()) {
                	participants.add(tier.getParticipant());
                } else {
                	participants.add(N_A);
                }
                /* Add language */
                String value = tier.getLangRef();
                if (value != null && !value.isEmpty()) {
                	languages.add(value);
                } else {
                	languages.add(N_A);
                }
            }
            /* Create annotator sub menu items */
            for(String annotator:annotators) {
            	annotatorCheckBoxItem = new JCheckBoxMenuItem(annotator);
            	annotatorCheckBoxItem.setActionCommand("ANNO_"+annotator);
            	annotatorCheckBoxItem.addActionListener(this);
            	annotatorCheckBoxes.add(annotatorCheckBoxItem);
            	annotatorSubMenu.add(annotatorCheckBoxItem);
            }
            /* Create participant sub menu items */
            for(String participant:participants) {
            	participantCheckBoxItem = new JCheckBoxMenuItem(participant);
            	participantCheckBoxItem.setActionCommand("PART_"+participant);
            	participantCheckBoxItem.addActionListener(this);
            	participantCheckBoxes.add(participantCheckBoxItem);
            	participantSubMenu.add(participantCheckBoxItem);
            }
            /* Create linguistic type sub menu and sub menu items */
            List<LinguisticType> linguisticTypes = transcription.getLinguisticTypes();
            for(LinguisticType linguisticType:linguisticTypes) {
            	linguisticTypeCheckboxItem = new JCheckBoxMenuItem(linguisticType.getLinguisticTypeName());
            	linguisticTypeCheckboxItem.setActionCommand("TYPE_"+linguisticType.getLinguisticTypeName());
            	linguisticTypeCheckboxItem.addActionListener(this);
            	linguisticTypeCheckBoxes.add(linguisticTypeCheckboxItem);
            	linguisticTypeSubMenu.add(linguisticTypeCheckboxItem);
            }
            /* Create language sub menu items */
            for(String language:languages) {
            	languageCheckboxItem = new JCheckBoxMenuItem(language);
            	languageCheckboxItem.setActionCommand("LANG_"+language);
            	languageCheckboxItem.addActionListener(this);
            	languageCheckBoxes.add(languageCheckboxItem);
            	languageSubMenu.add(languageCheckboxItem);
            }
            /* Create tier set sub menu item */
            for(String tierset:TierSetUtil.getTierSetUtilInstance().getTierSetList()) {
            	tiersetCheckBoxItem = new JCheckBoxMenuItem(tierset);
            	tiersetCheckBoxItem.setActionCommand("TRST_"+tierset);
            	tiersetCheckBoxItem.addActionListener(this);
            	tiersetCheckBoxes.add(tiersetCheckBoxItem);
            	tiersetSubMenu.add(tiersetCheckBoxItem);
            }
        }
        popup.add(tierSubMenu);
        popup.add(participantSubMenu);
        popup.add(annotatorSubMenu);
        popup.add(linguisticTypeSubMenu);
        popup.add(languageSubMenu);
        popup.add(tiersetSubMenu);
    }

    /**
     * The ACM editing event handling. Updates the popup menu and repaints.
     *
     * @param e the event
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER: /* When tier is added */
        case ACMEditEvent.REMOVE_TIER: /* When tier is removed */
        case ACMEditEvent.CHANGE_TIER: /* When tier is changed (covers new annotator/participant) */
        case ACMEditEvent.ADD_LINGUISTIC_TYPE: /* When type is added */
        case ACMEditEvent.REMOVE_LINGUISTIC_TYPE: /* When type is removed */
        	createPopupMenu();
        	updateAnnotationFilter();
        }
        paintBuffer();
    }

    /**
     * Updates the all tiers menu item, if it exists.
     */
    @Override
	public void updateLocale() {
        if (allAnnotationsRB != null) {
			allAnnotationsRB.setText(ElanLocale.getString("MultiTierControlPanel.Menu.ShowAllTiers"));
		}
        if(tierSubMenu != null) {
			tierSubMenu.setText(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuTier"));
		}
        if(annotatorSubMenu != null) {
			annotatorSubMenu.setText(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuAnnotator"));
		}
        if(participantSubMenu != null) {
			participantSubMenu.setText(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuParticipant"));
		}
        if(linguisticTypeSubMenu != null) {
			linguisticTypeSubMenu.setText(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuLinguisticType"));
		}
        if(languageSubMenu != null) {
			languageSubMenu.setText(ElanLocale.getString("MultiTierControlPanel.Menu.SubMenuLanguage"));
		}
    }

    /**
     * Ignored
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * Ignored
     */
    @Override
	public void updateSelection() {
    }

    /**
     * Ignored
     *
     * @param event the event
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
    }

    /**
     * Ignored
     */
    @Override
	public void preferencesChanged() {
    }

    /**
     * Ignored
     * @param e The mouse event.
     */
    @Override
	public void mouseClicked(MouseEvent e) {
    }

    /**
     * Ignored
     * @param e The mouse event.
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /**
     * Ignored
     * @param e The mouse event.
     */
    @Override
	public void mouseExited(MouseEvent e) {
    }

    /**
     * Shows the popup menu.
     */
    @Override
	public void mousePressed(MouseEvent e) {
        if ((SwingUtilities.isRightMouseButton(e) &&
                ((e.getButton() == MouseEvent.BUTTON1) ^ e.isMetaDown())) ||
                e.isPopupTrigger()) {
            if (popup == null) {
                createPopupMenu();
            }
            popup.show(this, e.getPoint().x, e.getPoint().y);
        }
    }

    /**
     * Ignored
     * @param e The mouse event.
     */
    @Override
	public void mouseReleased(MouseEvent e) {
    }

    /**
     * Method to deselect all checkboxes in a list.
     */
    private void deselectBoxes(List<JCheckBoxMenuItem> boxes) {
    	for(JCheckBoxMenuItem checkBox : boxes) {
			checkBox.setSelected(false);
		}    	
    }
    /**
     * Method to deselect all tiers.
     */
    private void deselectTiers() {
    	deselectBoxes(tierCheckBoxes);
    }
    /**
     * Method to deselect all annotators.
     */
    private void deselectAnnotators() {
    	deselectBoxes(annotatorCheckBoxes);
    }
    /**
     * Method to deselect all participants.
     */
    private void deselectParticipants() {
    	deselectBoxes(participantCheckBoxes);
    }
    /**
     * Method to deselect all linguistic types.
     */
    private void deselectLinguisticTypes() {
    	deselectBoxes(linguisticTypeCheckBoxes);
    }
    /**
     * Method to deselect all languages.
     */
    private void deselectLanguages() {
    	deselectBoxes(languageCheckBoxes);
    }
    /**
     * Method to deselect all tier sets.
     */
    private void deselectTiersets() {
    	deselectBoxes(tiersetCheckBoxes);
    }
    /**
     * Changes the selected tier.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	ViewMode old_viewMode = viewMode;
    	
        if (e.getSource() == allAnnotationsRB) {
        	viewMode=ViewMode.SHOW_ALL;
        	/* Deselect all tier checkboxes. */
    		deselectLinguisticTypes();
    		deselectAnnotators();
    		deselectParticipants();
    		deselectTiers();
    		deselectLanguages();
    		deselectTiersets();
        	viewModeKeywords.clear();
        	// Make sure that if "Show All" is selected, it can't be deselected.
        	if (allAnnotationsRB.isSelected()) {
        		allAnnotationsRB.setEnabled(false);
        	}
        } else {
            String keyword=e.getActionCommand().substring(5);
            String filterType=e.getActionCommand().substring(0,5);
            
            if(((JCheckBoxMenuItem)e.getSource()).isSelected()) {
            	allAnnotationsRB.setSelected(false);
        		allAnnotationsRB.setEnabled(true);
            	if(filterType.equals("TIER_")) {
					viewMode=ViewMode.SHOW_BY_TIER;
				} else if(filterType.equals("ANNO_")) {
					viewMode=ViewMode.SHOW_BY_ANNOTATOR;
				} else if(filterType.equals("PART_")) {
					viewMode=ViewMode.SHOW_BY_PARTICIPANT;
				} else if(filterType.equals("TYPE_")) {
					viewMode=ViewMode.SHOW_BY_TYPE;
				} else if(filterType.equals("LANG_")) {
					viewMode=ViewMode.SHOW_BY_LANGUAGE;
				} else if(filterType.equals("TRST_")) {
					viewMode=ViewMode.SHOW_BY_TIERSET;
				}
            	/* If viewMode changes, remove all keywords and deselect other modes */
            	if(old_viewMode!=viewMode) {
                	viewModeKeywords.clear();
            		if (viewMode != ViewMode.SHOW_BY_TIER) {
    	        		deselectTiers();
            		}
            		if (viewMode != ViewMode.SHOW_BY_ANNOTATOR) {
    	        		deselectAnnotators();
            		}
            		if (viewMode != ViewMode.SHOW_BY_PARTICIPANT) {
    	        		deselectParticipants();
            		}
            		if (viewMode != ViewMode.SHOW_BY_TYPE) {
    	        		deselectAnnotators();
            		}
            		if (viewMode != ViewMode.SHOW_BY_LANGUAGE) {
    	        		deselectLanguages();
            		}
            		if (viewMode != ViewMode.SHOW_BY_TIERSET) {
    	        		deselectTiersets();
            		}
                }
            	/* Add new keyword */
            	if (N_A.equals(keyword)) {
            		viewModeKeywords.add("");
            	} else {
            		viewModeKeywords.add(keyword);
            	}
            } else {
            	if (N_A.equals(keyword)) {
            		viewModeKeywords.remove("");
            	} else {
            		viewModeKeywords.remove(keyword);
            	}
            	
            	if(viewModeKeywords.isEmpty()) {
        			viewMode=ViewMode.SHOW_ALL;
        			allAnnotationsRB.setSelected(true);
            		allAnnotationsRB.setEnabled(false);
        		}
            }
        }
        
        paintBuffer();
    }

	/**
	 * Notification of a change in media offset and therefore duration.
	 */
	@Override
	public void mediaOffsetChanged() {
		paintBuffer();
	}

	@Override
	public void tierSetChanged() {
		if (viewMode == ViewMode.SHOW_BY_TIERSET) {
			updateTiersetSubmenu();
			paintBuffer();
		}
	}

	@Override
	public void tierSetVisibilityChanged(TierSet set) {
		if (viewMode == ViewMode.SHOW_BY_TIERSET) {
			updateTiersetSubmenu();
			paintBuffer();
		}
	}

	@Override
	public void tierVisibilityChanged(String tierName, boolean isVisible) {
		if (viewMode == ViewMode.SHOW_BY_TIERSET) {
			updateTiersetSubmenu();
			paintBuffer();
		}
	}
    
	/**
	 * Updates the Tierset submenu
	 */
	private void updateTiersetSubmenu() {
		List<String> keywordsToDelete = new ArrayList<String>();
		
		for(JCheckBoxMenuItem checkBox : tiersetCheckBoxes) {
			String checkBoxText = checkBox.getText();
			if(!TierSetUtil.getTierSetUtilInstance().checkIfTierSetExists(checkBoxText)) {
				if(checkBox.isSelected()) {
					keywordsToDelete.add(checkBoxText);
				}
				tiersetSubMenu.remove(checkBox);
			}
		}
		
		
		for(String keyword : keywordsToDelete) {
			viewModeKeywords.remove(keyword);
		}
		
		if(viewModeKeywords.isEmpty()) {
			viewMode=ViewMode.SHOW_ALL;
			allAnnotationsRB.setSelected(true);
    		allAnnotationsRB.setEnabled(false);
		}	
	}
}