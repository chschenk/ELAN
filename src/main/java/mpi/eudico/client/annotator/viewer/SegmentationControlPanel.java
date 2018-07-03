package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.TierOrder;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.TierSortAndSelectDialog2;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * An alternative multitier control panel with reduced functionality as compared to the
 * one of the annotation  mode.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SegmentationControlPanel extends JPanel implements MouseListener,
MouseMotionListener, ActionListener, ComponentListener{
    /** the horizontal margins of the component */
    public final static int MARGIN = 5;
    private TranscriptionImpl transcription;
    private List<TierImpl> tiers;
    private List<TierImpl> acceptableTiers;
    private List<TierImpl> visibleTiers;
    private Map<TierImpl, String> tierNames;
    private Map<TierImpl, Color> tierRootColors;
    private Map<TierImpl, Color> prefTierColors;
    private MultiTierViewer viewer;
    private int[] tierPositions;
    private Tier activeTier;
    private BufferedImage bi;
    private Graphics2D big2d;
    private FontMetrics fontMetrics;
    private Font boldFont;
    private FontMetrics boldMetrics;
    private boolean dragging;
    private int dragX;
    private int dragY;
    private String dragLabel;
    private int tierHeight;
    private int editTierHeight;
    private JComponent resizer = null;
    private JPopupMenu popup;
    private TierOrder tierOrder;
    
	/**
	 * @param transcription
	 */
	public SegmentationControlPanel(TranscriptionImpl transcription) {
		super();
		setLayout(null);
		this.transcription = transcription;
		initComponents();
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	private void initComponents() {
        tiers = transcription.getTiers();
        acceptableTiers = new ArrayList<TierImpl>(tiers.size());
        visibleTiers = new ArrayList<TierImpl>(tiers.size());
        tierNames = new HashMap<TierImpl, String>(tiers.size());           
        tierRootColors = new HashMap<TierImpl, Color>();
        prefTierColors = new HashMap<TierImpl, Color>();
        editTierHeight = 30;
        
        Iterator tierIter = transcription.getTiers().iterator();
        TierImpl tier;
        while (tierIter.hasNext()) {
            tier = (TierImpl) tierIter.next();
            if (!tier.hasParentTier() || (tier.getLinguisticType().getConstraints() != null && 
            		tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)){
            	tierNames.put(tier, tier.getName());
            	acceptableTiers.add(tier);
            	visibleTiers.add(tier);
            }
        }
        if (visibleTiers.size() > 0) {
        	activeTier = visibleTiers.get(0);
        }
        loadPreferences();
	}

	public MultiTierViewer getViewer() {
		return viewer;
	}

	public void setViewer(MultiTierViewer viewer) {
		this.viewer = viewer;
		viewer.setVisibleTiers(visibleTiers);
		if (activeTier != null) {
			viewer.setActiveTier(activeTier);
		}
	}
	
	/**
	 * When a tier order object is set the list of visible tiers is re-ordered based on the ordering
	 * in the global tier order. Ideally this should be called before a previously stored 
	 * specialized ordering for this viewer is loaded.
	 * 
	 * @param tierOrder an object containing a list of tier names.
	 */
    public void setTierOrderObject(TierOrder tierOrder) {
        this.tierOrder = tierOrder;
        
        if (tierOrder != null && tierOrder.getTierOrder() != null && tierOrder.getTierOrder().size() > 1) {
        	List <TierImpl> tempList = new ArrayList<TierImpl>(visibleTiers);
        	visibleTiers.clear();
        	String name;
        	TierImpl iterTier;
        	for (int i = 0; i < tierOrder.getTierOrder().size(); i++) {
        		name = tierOrder.getTierOrder().get(i);
        		
        		for (int j = 0; j < tempList.size(); j++) {
        			iterTier = tempList.get(j);
        			if (name.equals(iterTier.getName())) {
        				tempList.remove(j);
        				visibleTiers.add(iterTier);
        				break;
        			}
        		}
        		if (tempList.size() == 0) {
        			break;
        		}
        	}
        	// add any remaining tiers from the existing visible tiers list /??
        	visibleTiers.addAll(tempList);
        	
        	// in case acceptable and visible are not equal
        	tempList = new ArrayList<TierImpl>(acceptableTiers);
        	acceptableTiers.clear();

        	for (int i = 0; i < tierOrder.getTierOrder().size(); i++) {
        		name = tierOrder.getTierOrder().get(i);
        		
        		for (int j = 0; j < tempList.size(); j++) {
        			iterTier = tempList.get(j);
        			if (name.equals(iterTier.getName())) {
        				tempList.remove(j);
        				acceptableTiers.add(iterTier);
        				break;
        			}
        		}
        		if (tempList.size() == 0) {
        			break;
        		}
        	}
        	// add any remaining tiers from the existing visible tiers list /??
        	acceptableTiers.addAll(tempList);
        }
    }
    
    public int getEditTierHeight() {
		return editTierHeight;
	}

	public void setEditTierHeight(int editTierHeight) {
		this.editTierHeight = editTierHeight;
		paintBuffer();
	}
	
	public void setTierHeight(int tierHeight) {
		this.tierHeight = tierHeight;
	}

	/**
     * Overrides <code>JComponent.setFont(Font)</code> by creating a bold
     * derivative  and <code>FontMetrics</code> objects for both Font objects.
     *
     * @param f DOCUMENT ME!
     */
    @Override
	public void setFont(Font f) {
        super.setFont(f);
        tierHeight = 3 * f.getSize(); // first guess
        fontMetrics = getFontMetrics(getFont());
        boldFont = getFont().deriveFont(Font.BOLD);
        boldMetrics = getFontMetrics(boldFont);
    }
    
    public void setResizeComponent(JComponent comp) {
    	if (resizer != null) {
    		remove(resizer);
    	}
    	resizer = comp;
    	if (resizer != null) {
    		add(resizer);
    		resizer.setBounds(getWidth() - resizer.getWidth(), 1, resizer.getWidth(), resizer.getHeight());
    		resizer.setBackground(Constants.ACTIVETIERCOLOR);
    	}
    	repaint();
    }
    
    /**
     * Sets the y positions of the visible tiers.
     *
     * @param tierPositions the y positions of the tiers
     */
    public void setTierPositions(int[] tierPositions) {
        this.tierPositions = tierPositions;

//        if (tierPositions.length > 2) {
//            tierHeight = tierPositions[2] - tierPositions[1];
//        } else {
//        	tierHeight = 3 * getFont().getSize();
//        }

        paintBuffer();
    }
    
    /**
     * Tells the ControlPanel which is the new active tier.<br>
     * The panel updates it's own state and notifies the attached viewer.
     *
     * @param tier the new active tier
     */
    public void setActiveTier(Tier tier) {
    	if (tier == activeTier) {
    		return;
    	}
    	
        activeTier = tier;
        
        if (activeTier != null) {
        	if (viewer != null) {
        	viewer.setActiveTier(tier);
        		setPreference("SegmentationViewer.ActiveTier", tier.getName(), transcription);
        	}
        } else {
        	setPreference("SegmentationViewer.ActiveTier", null, transcription);
        }
        	
        
        paintBuffer();
    }

    /**
     * Returns the currently active tier.
     *
     * @return the active Tier, can be null
     */
    public Tier getActiveTier() {
        return activeTier;
    }
    
    /**
     * Adds a tier if it is not in the list yet.
     * @param tier
     */
    public void addTier(TierImpl tier) {
    	if (tier == null) {
    		return;
    	}
    	if (tierNames.containsValue(tier.getName())) {
    		return;
    	}
    	if (tierNames.containsKey(tier)) {
    		return;
    	}
    	if (!tier.hasParentTier() || (tier.getLinguisticType().getConstraints() != null && 
        		tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)){
	    	tierNames.put(tier, tier.getName());
	    	acceptableTiers.add(tier);
	    	visibleTiers.add(tier);
	    	
	        if (visibleTiers.size() == 1) {
	        	activeTier = tier;
	        }
	        if (viewer != null) {
	        	viewer.setVisibleTiers(visibleTiers);
	        }
	        paintBuffer();
	        storeTierOrders();
    	}
    }
    
    /**
     * A tier has been removed from the document, remove it from the list.
     * @param tier
     */
    public void removeTier (TierImpl tier) {
    	if (tier == null) {
    		return;
    	}
    	tierNames.remove(tier);
    	acceptableTiers.remove(tier);
    	visibleTiers.remove(tier);
    	if (activeTier == tier) {
    		activeTier = null;
    	}
        if (viewer != null) {
        	viewer.setVisibleTiers(visibleTiers);
        }
        
        paintBuffer();
        storeTierOrders();
    }
    
    /**
     * Update the name label.
     * @param tier
     */
    public void changeTier (TierImpl tier) {
    	if (tier == null) {
    		return;
    	}
    	if (tierNames.containsKey(tier)) {
    		tierNames.put(tier, tier.getName());
    		
            if (viewer != null) {
            	viewer.setVisibleTiers(visibleTiers);
            }
            
            paintBuffer();
            storeTierOrders();
    	} 	

    }
    
    /**
     * Creates a BufferedImage when necessary and paints the tierlabels in this
     * buffer.
     */
    private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if ((bi == null) || (bi.getWidth() != getWidth()) ||
                (bi.getHeight() != getHeight())) {
            bi = new BufferedImage(getWidth(), getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        }

        big2d = bi.createGraphics();
        if (SystemReporting.antiAliasedText) {
	        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        //big2d.setColor(getBackground());
        big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        big2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        if ((tierPositions == null) || tierPositions.length == 0) {
        	repaint();
            return;
        }
        int y;
        y = tierPositions[0];
        int panelWidth = getWidth();
        int actualLabelWidth;
        int availableLabelWidth = panelWidth - (2 * MARGIN);
        TierImpl tier;
        String label = "";
        
        // mark active/edit tier area
        big2d.setColor(Constants.ACTIVETIERCOLOR);
        big2d.fillRect(0, 0, panelWidth, editTierHeight);
        if (activeTier != null) {
        	big2d.setFont(boldFont);
        	big2d.setColor(Color.RED);
        	label = truncateString(tierNames.get(activeTier), availableLabelWidth, boldMetrics);
            actualLabelWidth = SwingUtilities.computeStringWidth(boldMetrics,
                    label);

            int x = (MARGIN + availableLabelWidth) - actualLabelWidth;
            y = tierPositions[0] + (boldFont.getSize() / 2);
            big2d.drawString(label, x, y);
        }
               
        big2d.setColor(Constants.SHAREDCOLOR3);
        //big2d.drawLine(0, editTierHeight, bi.getWidth(), editTierHeight);
        big2d.setFont(getFont());
        
        if (tierPositions.length > 1) {
        	int tierIndex = 0;
            for (int i = 1; i < tierPositions.length; i++) {
            	y = tierPositions[i];
            	if (y < editTierHeight + tierHeight - 2) {
            		// draw a line at the end of the height if it is > edit tier height
            		//y += (tierHeight / 2);
                	if (i % 2 == 0 && y + tierHeight > editTierHeight) {
                		big2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
                		big2d.fillRect(0, editTierHeight, bi.getWidth(), y - editTierHeight);
                	}
            		if (y >= editTierHeight + tierHeight) {
            			big2d.drawLine(0, y, bi.getWidth(), y);
            		}

            		continue;
            	}
            	// test 
            	tierIndex = i - 1;
            	if (i % 2 == 0) {
            		big2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
            		big2d.fillRect(0, y - tierHeight + 1, bi.getWidth(), tierHeight);
            	}
            	if (tierIndex < visibleTiers.size()) {
                	tier = visibleTiers.get(tierIndex);
                	if (tier == activeTier) {
                		//continue;// active tier is also part of the visible tiers
                		if (tierIndex >= 0) {// mark the position of the active tier in the list of tiers
                			big2d.setColor(Color.RED);
                			//big2d.fillOval(1, y - tierHeight - 3, 6, 6);
                			big2d.fillRect(1, y - tierHeight, 4, tierHeight);
                		}
//                		tierIndex++;
//                		if (tierIndex < visibleTiers.size()) {
//                			tier = visibleTiers.get(tierIndex);
//                		} else {
//                			break;
//                		}
                	}
                    label = truncateString(tierNames.get(tier), availableLabelWidth,
                            fontMetrics);
                    actualLabelWidth = SwingUtilities.computeStringWidth(fontMetrics,
                            label);

                    int x = (MARGIN + availableLabelWidth) - actualLabelWidth;
                    //y += (getFont().getSize() / 2);
                    y -= (tierHeight - getFont().getSize()) / 2;
                	Color col = prefTierColors.get(tier);
                	if (col == null) {        	
        	            big2d.setColor(Constants.SHAREDCOLOR6);
                	} else {
                		big2d.setColor(col);
                	}

                	
                	big2d.drawString(label, x, y);
                	//tierIndex++;
            	}
            	//if (y > editTierHeight) {
            	//y += (tierHeight / 2) - (getFont().getSize() / 2);
            	big2d.setColor(Constants.SHAREDCOLOR3);
            	y = tierPositions[i];
            	big2d.drawLine(0, y, bi.getWidth(), y);
            	//}
                //y += tierHeight;
            }
        }
      
        repaint();
    }
    
    /**
     * Draw the buffered tier names and eventually the label of 
     * the tier that is being dragged.
     *
     * @param g the graphics context
     */
    @Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        if (SystemReporting.antiAliasedText) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        //g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        //g2d.fillRect(0, 0, getWidth(), getHeight());
        if (bi != null) {
        	g2d.drawImage(bi, 0, 0, this);
        }       

        g2d.setColor(Constants.SELECTIONCOLOR);
        g2d.drawLine(0, 0, getWidth() - 1, 0);
        g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        if (dragging) {
            g2d.setFont(boldFont);
            g2d.setColor(Color.blue);
            g2d.drawString(dragLabel, dragX, dragY);
        }
        if (resizer != null) {
        	resizer.repaint();
        }
    }
    
    /**
     * Create a truncated String of a label to display in the panel.
     *
     * @param string the label's value
     * @param width the available width for the String
     * @param fMetrics the font metrics
     *
     * @return the truncated String
     */
    private String truncateString(String string, int width, FontMetrics fMetrics) {
        String line = string;

        if (fMetrics != null) {
            int stringWidth = fMetrics.stringWidth(line);

            if (stringWidth > (width - 4)) { // truncate

                int i = 0;
                String s = "";
                int size = line.length();

                while (i < size) {
                    if (fMetrics.stringWidth(s) > (width - 4)) {
                        break;
                    } else {
                        s = s + line.charAt(i++);
                    }
                }

                if (!s.equals("")) {
                    line = s.substring(0, s.length() - 1);
                } else {
                    line = s;
                }
            }
        }

        return line;
    }

    private void showTierSelectionDialog() {
    	List<String> allNames = new ArrayList<String>(acceptableTiers.size());
    	for (TierImpl t : acceptableTiers) {
    		allNames.add(t.getName());
    	}
    	List<String> visNames = new ArrayList<String>(visibleTiers.size());
    	for (TierImpl t : visibleTiers) {
    		visNames.add(t.getName());
    	}
    	
    	TierSortAndSelectDialog2 dialog = new TierSortAndSelectDialog2(
    			ELANCommandFactory.getRootFrame(transcription), 
    			transcription, allNames, visNames, true, true, AbstractTierSortAndSelectPanel.Modes.ROOT_W_INCLUDED);
    	
//    	TierSortAndSelectDialog dialog = new TierSortAndSelectDialog(ELANCommandFactory.getRootFrame(transcription), 
//    			true, transcription, allNames, visNames);// must be modal, must block
    	dialog.setTitle(ElanLocale.getString("MultiTierControlPanel.Menu.ShowTiers"));
    	dialog.setLocationRelativeTo(this);
    	Rectangle bounds = dialog.getBounds();
    	if (bounds.x < this.getLocationOnScreen().x) {
    		dialog.setBounds(this.getLocationOnScreen().x, bounds.y, 
    				bounds.width, bounds.height);
    	}
    	
    	//List hiddenTiers = (List) Preferences.get("SegmentationViewer.HiddenTiers", transcription);
    	String selectionMode = Preferences.getString("SegmentationViewer.SelectTierMode", transcription);
    	List<String> hiddenItems = Preferences.getListOfString("SegmentationViewer.HiddenItems", transcription);
    	//dialog.setSelectedMode(selectionMode, hiddenTiers);
    	dialog.setSelectionMode(selectionMode, hiddenItems);
    	dialog.setVisible(true);
    	    	
    	List<String> editedTiers = dialog.getSelectedTiers();
    	if (editedTiers != null) {
    		setPreference("SegmentationViewer.HiddenTiers", dialog.getHiddenTiers(), transcription);
    		setPreference("SegmentationViewer.SelectTierMode", dialog.getSelectionMode(), transcription);
    		setPreference("SegmentationViewer.HiddenItems", dialog.getUnselectedItems(), transcription);
    		// set tier order and tier visibility, store prefs
    		List<String> allSorted = dialog.getTierOrder();
    		acceptableTiers.clear();
    		TierImpl t;
    		for (String name : allSorted) {
    			t = transcription.getTierWithId(name);
    			if (t != null) {
    				acceptableTiers.add(t);
    			}
    		}
    		visibleTiers.clear();
    		for (String name : editedTiers) {
    			t = transcription.getTierWithId(name);
    			visibleTiers.add(t);
    		}
    		
    		if (activeTier != null) {
    			if (!visibleTiers.contains(activeTier)) {
    				if (visibleTiers.size() > 0) {
    					activeTier = visibleTiers.get(0);
    				}
    			}
    		} else {
				if (visibleTiers.size() > 0) {
					activeTier = visibleTiers.get(0);
				}
    		}
    		viewer.setVisibleTiers(visibleTiers);
    		viewer.setActiveTier(activeTier);
    		storeTierOrders();
    		paintBuffer();
    	}
    }
    
    /**
     * Sets the tier at the specified y position as the active tier.
     * The calculation is based on the tierpositions, the list of visible tiers and
     * (the index of) the current active tier.
     * 
     * @param yPos y coordinates of the mouse click
     */
    private void activateTier(int yPos) {
    	if (yPos < editTierHeight) {
    		return;
    	}
    	if (tierPositions == null || tierPositions.length <= 1) {
    		return;
    	}
    	if (yPos > tierPositions[tierPositions.length - 1]) {
    		return;
    	}
    	
    	for (int i = 0; i < tierPositions.length; i++) {
    		if (yPos > tierPositions[i]) {
    			if (i < tierPositions.length - 1) {
    				if (yPos > tierPositions[i + 1]) {
    					continue;
    				} else {
    					if (i < visibleTiers.size()) {
    						TierImpl t = visibleTiers.get(i);
    						if (t != activeTier) {
    							setActiveTier(t);
    							
    							break;
    						}
//    						int actIndex = visibleTiers.indexOf(activeTier);
//    						int shiftFactor = 0;
//    						if (actIndex > -1 && actIndex <= i) {
//    							shiftFactor++;
//    						}
//    						TierImpl t = visibleTiers.get(i + shiftFactor);   						
//    						if (t == activeTier) {
//    							if (i + shiftFactor + 1 < visibleTiers.size()) {
//    								setActiveTier(visibleTiers.get(i + shiftFactor + 1));
//    								break;
//    							}
//    						} else {
//    							setActiveTier(t);
//    							
//    							break;
//    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    /**
     * Update any ui labels.
     */
    public void updateLocale() {
    	popup = null;// reset the menu labels
    }
    
    private void loadPreferences() {
    	String tierName = Preferences.getString("SegmentationViewer.ActiveTier", transcription);
    	if (tierName != null) {
    		for (TierImpl t : acceptableTiers) {
    			if (t == null) {
    				continue;
    			}
    			if (t.getName().equals(tierName)) {
    				setActiveTier(t);
    				break;
    			}
    		}
    	}
    	
    	List<String> allTiers = Preferences.getListOfString("SegmentationViewer.TierOrder", transcription);
    	if (allTiers != null) {
    		acceptableTiers.clear();
    		for (String name : allTiers) {
    			TierImpl t = transcription.getTierWithId(name);
    			if (t == null) {
    				continue;
    			}
    			// check if t is still the right type??
                if (!t.hasParentTier() || (t.getLinguisticType().getConstraints() != null && 
                		t.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)){
                	acceptableTiers.add(t);
                	tierNames.put(t, t.getName());//??
                }
    		}
    	}
    	
    	// add all the (new) tiers that were created after the preference is stored
    	//(new tiers which are not in the preferences)
    	for (TierImpl t : visibleTiers) {
    		if(!acceptableTiers.contains(t)) {
    			acceptableTiers.add(t);
    		}
    	}
    	
    	List<String> hidTiers = Preferences.getListOfString("SegmentationViewer.HiddenTiers", transcription);
    	if (hidTiers != null) {
    		visibleTiers.clear();
    		visibleTiers.addAll(acceptableTiers);// in the right order

    		for (String name : hidTiers) {
    			TierImpl t = transcription.getTierWithId(name);
    			if (t != null) {
    				visibleTiers.remove(t);
    			}
    		}
    		if (viewer != null) {
    			viewer.setVisibleTiers(visibleTiers);
    		}
    	}
    }
    
    /** 
     * Stores tier order and hidden tiers.
     */
    private void storeTierOrders() {
		List<String> hiddenTiers = new ArrayList<String>();
    	List<String> allNames = new ArrayList<String>(acceptableTiers.size());
    	for (TierImpl t : acceptableTiers) {
    		allNames.add(t.getName());
    	}
    	List<String> visNames = new ArrayList<String>(visibleTiers.size());
    	for (TierImpl t : visibleTiers) {
    		visNames.add(t.getName());
    	}
        Iterator<String> it = allNames.iterator();
        String name;

        while (it.hasNext()) {
            name = it.next();
            if (!visNames.contains(name)) {
            	hiddenTiers.add(name);
            }
        }
        setPreference("SegmentationViewer.HiddenTiers", hiddenTiers, transcription);
        setPreference("SegmentationViewer.TierOrder", allNames, transcription);
    }
    
	private void setPreference(String key, Object value, Transcription trans) {
		// store tierorder, hidden tiers and active tier
		Preferences.set(key, value, trans, false, false);
	}
    
// listeners methods...    
    /**
     * 
     */
	@Override
	public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() > 1)) {
        	activateTier(e.getPoint().y);
        }
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
			if (popup == null) {
				popup = new JPopupMenu("");
				JMenuItem mi = new JMenuItem(ElanLocale.getString("MultiTierControlPanel.Menu.ShowHideMore"));
				mi.addActionListener(this);
				mi.setActionCommand("Select");
				popup.add(mi);
			}
			popup.show(this, e.getPoint().x, e.getPoint().y);
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Select")) {
			// show select tiers dialog
			showTierSelectionDialog();
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (resizer != null) {
			resizer.setBounds(getWidth() - resizer.getWidth(), 1, resizer.getWidth(), resizer.getHeight());
		}
		paintBuffer();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}
