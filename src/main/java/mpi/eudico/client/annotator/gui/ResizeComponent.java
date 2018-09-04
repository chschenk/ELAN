package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;


/**
 * A ui component that can be used to change the size of the media area in the
 * main window by dragging the component with the mouse. <br>
 * <b>Note: </b> only vertical resizing is supported in the current version,
 * horizontal size (width) is calculated based on the height.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ResizeComponent extends JPanel implements MouseListener,
    MouseMotionListener, ActionListener {
    private JLabel iconLabel;
    private ElanLayoutManager manager;
    private int orientation = SwingConstants.HORIZONTAL;
    private int forComponent = MEDIA_AREA;
    private JPopupMenu popup;

    // dragging
    private int startXY = 0;
    private boolean dragging = false;
    public static final int MEDIA_AREA = 0;
    public static final int CONTROL_PANEL = 1;
    
    private boolean decrease = false;

    /**
     * Creates a new ResizeComponent instance
     *
     * @param manager the layout manager that manages the layout of the window
     * @param orientation either <code>SwingConstants.HORIZONTAL</code> or
     *        <code>SwingConstants.VERTICAL</code>
     */
    public ResizeComponent(ElanLayoutManager manager, int orientation) {
        this.manager = manager;

        if (orientation == SwingConstants.VERTICAL) {
            this.orientation = orientation;
        }

        initComponents();
    }

    /**
     * Creates a new ResizeComponent instance
     *
     * @param manager the layout manager that manages the layout of the window
     * @param orientation either <code>SwingConstants.HORIZONTAL</code> or
     *        <code>SwingConstants.VERTICAL</code>
     */
    public ResizeComponent(ElanLayoutManager manager, int orientation, int forComponent) {
        this.manager = manager;

        if (orientation == SwingConstants.VERTICAL) {
            this.orientation = orientation;
        }
        this.forComponent = forComponent;
        
        initComponents();
    }
    
    public void changeBehaviourToDecrease(boolean value){
    	decrease = value;
    }

    /**
     * Loads the icon image ands adds it to a label that is added to this
     * panel. Sets preferred size and adds mouse listeners.
     */
    private void initComponents() {
        setLayout(null);
        iconLabel = new JLabel();

        try {
        	if (orientation == SwingConstants.VERTICAL) {
	            ImageIcon icon = new ImageIcon(this.getClass()
	                                               .getResource("/mpi/eudico/client/annotator/resources/SplitPaneDividerV6.gif"));
	            iconLabel.setIcon(icon);
        	} else {
	            ImageIcon icon = new ImageIcon(this.getClass()
                        .getResource("/mpi/eudico/client/annotator/resources/SplitPaneDividerH6.gif"));
	            iconLabel.setIcon(icon);
        	}
        } catch (Exception ex) {
            // if the icon could not be loaded
        	if (orientation == SwingConstants.VERTICAL) {
        		iconLabel.setText("^");
        	} else {
        		iconLabel.setText("<>");
        	}
        }

        add(iconLabel);
        iconLabel.setBounds(0, 0, 16, 16);
        setPreferredSize(new Dimension(16, 16));
        setBorder(new LineBorder(Constants.DEFAULTBACKGROUNDCOLOR, 1));
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    /**
     * Creates the popup menu and adds one item, to restore the default height.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu();

        JMenuItem item = new JMenuItem(ElanLocale.getString("Button.Default"));
        item.addActionListener(this);
        popup.add(item);
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseClicked(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
	public void mousePressed(MouseEvent e) {
        // HS jun 2006: change to discriminate Ctrl-click and Command-click on Mac
        // isRightMouseButton() returns true for both, isMetaDown() returns true for Command only 
        // but on Windows isMetaDown() returns true when the right mouse button (BUTTON3) has been pressed
        if ((SwingUtilities.isRightMouseButton(e) &&
                ((e.getButton() == MouseEvent.BUTTON1) ^ e.isMetaDown())) ||
                e.isPopupTrigger()) {
            if (popup == null) {
                createPopupMenu();
            }

            popup.show(this, e.getX(), e.getY());

            return;
        }

        if (orientation == SwingConstants.HORIZONTAL) {
        	setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            startXY = e.getX();
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            startXY = e.getY();
        }

        dragging = true;
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseReleased(MouseEvent e) {
        if (dragging) {
        	if (orientation == SwingConstants.HORIZONTAL) {
                int dist = e.getX() - startXY;
                if(decrease){
                	dist = -1 * (dist);
                }
                if (forComponent == CONTROL_PANEL) {
                	manager.setMultiTierControlPanelWidth(manager.getMultiTierControlPanelWidth() + dist);
                } else {
                	manager.setMediaAreaWidth(manager.getMediaAreaWidth() + dist);
                }
            } else {
                int dist = e.getY() - startXY;
                if(decrease){
                	dist = -1 * (dist);
                }
                manager.setMediaAreaHeight(manager.getMediaAreaHeight() + dist);// change if more components are added
            }
        }

        startXY = 0;
        dragging = false;

        //setCursor(Cursor.getDefaultCursor());
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    	if (orientation == SwingConstants.VERTICAL) {
    		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    	} else {
    		setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    	}
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseExited(MouseEvent e) {
        if (!dragging) {
            //setCursor(Cursor.getDefaultCursor());    
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseDragged(MouseEvent e) {
        if (orientation == SwingConstants.HORIZONTAL) {
            int dist = e.getX() - startXY;            
            if(decrease){
            	dist = -1 * (dist);
            }
            if (forComponent == CONTROL_PANEL) {
            	manager.setMultiTierControlPanelWidth(manager.getMultiTierControlPanelWidth() + dist);
            } else {
            	manager.setMediaAreaWidth(manager.getMediaAreaWidth() + dist);
            }
        } else {
            int dist = e.getY() - startXY;
            if(decrease){
            	dist = -1 * (dist);
            }
            manager.setMediaAreaHeight(manager.getMediaAreaHeight() + dist);// change if more components are added
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseMoved(MouseEvent e) {
    	if (orientation == SwingConstants.VERTICAL) {
    		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    	} else {
    		setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    	}
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        // only one item
    	if (forComponent == MEDIA_AREA) {
    		//if (orientation == SwingConstants.VERTICAL) {
    			manager.setMediaAreaHeight(ElanLayoutManager.MASTER_MEDIA_HEIGHT);
    	//	}
    	} else if (forComponent == CONTROL_PANEL) {
    		if (orientation == SwingConstants.HORIZONTAL) {
    			manager.setMultiTierControlPanelWidth(ElanLayoutManager.CONTROL_PANEL_WIDTH);
    		}
    	}
        
    }


}
