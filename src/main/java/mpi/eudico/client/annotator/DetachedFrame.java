package mpi.eudico.client.annotator;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;


/**
 * JFrame that can be used to detach components from the Elan layout
 */

@SuppressWarnings("serial")
public class DetachedFrame extends JFrame implements ComponentListener {
//public class DetachedFrame extends JDialog implements ComponentListener, ActionListener, ElanLocaleListener {
    private ElanLayoutManager layoutManager;
    private Component component;
    private float aspectRatio;
//    private JMenuBar menuBar;
//    private JMenu menuView;
//    private JMenuItem restoreItem;
//    private JMenuItem minimizeItem;
//    private JMenuItem maximizeItem;
//    private Point restoreLocation;
//    private Dimension restoreSize;
//    private boolean bRestored;
    private final int margin = 2;
    private ActionMap origActionMap;

    /**
     * Creates a new DetachedFrame instance
     *
     * @param layoutManager DOCUMENT ME!
     * @param component DOCUMENT ME!
     * @param title DOCUMENT ME!
     */
    public DetachedFrame(ElanLayoutManager layoutManager, Component component,
        String title) {
        //super(layoutManager.getElanFrame(), false);
        super();
        this.layoutManager = layoutManager;
        this.component = component;
        this.setAlwaysOnTop(true);

        //bRestored = true;

        setTitle(title);
        getContentPane().setLayout(null);
        getContentPane().add(component);

        origActionMap = getRootPane().getActionMap();
        // take over the key strokes from elan frame
        updateShortcuts();

        //make menu visible / appear above heavyweight video
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
/*
        menuView = new JMenu();
        menuBar.add(menuView);

        restoreItem = new JMenuItem();
        restoreItem.addActionListener(this);
        menuView.add(restoreItem);

        minimizeItem = new JMenuItem();
        minimizeItem.addActionListener(this);
        menuView.add(minimizeItem);

        maximizeItem = new JMenuItem();
        maximizeItem.addActionListener(this);
        menuView.add(maximizeItem);
        */

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addComponentListener(this);
        addWindowListener(new FrameWindowListener());

//        ElanLocale.addElanLocaleListener(
//        		layoutManager.getViewerManager().getTranscription(), this);
        //updateLocale();
    }

    /**
     * DOCUMENT ME!
     */
    /*
    public void updateLocale() {
        menuView.setText(ElanLocale.getString("DetachedFrame.View"));
        restoreItem.setText(ElanLocale.getString("DetachedFrame.Restore"));

        //always use A for minimize
        minimizeItem.setText(ElanLocale.getString("DetachedFrame.Minimize"));
        //minimizeItem.setMnemonic(KeyEvent.VK_A);
        minimizeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        //always use X for maximize
        maximizeItem.setText(ElanLocale.getString("DetachedFrame.Maximize"));
        //maximizeItem.setMnemonic(KeyEvent.VK_X);
        maximizeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        String mnemonic = null;
        try {
        	mnemonic = ElanLocale.getString("MNEMONIC.DetachedFrame.View");
        	if (mnemonic.length() > 0) {
        		menuView.setMnemonic(mnemonic.charAt(0));
        	}
        	mnemonic = ElanLocale.getString("MNEMONIC.DetachedFrame.Restore");
        	if (mnemonic.length() > 0) {
        		restoreItem.setMnemonic(mnemonic.charAt(0));
                restoreItem.setAccelerator(KeyStroke.getKeyStroke(mnemonic.charAt(0),
                		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        	}
        	mnemonic = ElanLocale.getString("MNEMONIC.DetachedFrame.Maximize");
        	if (mnemonic.length() > 0) {
        		maximizeItem.setMnemonic(mnemonic.charAt(0));
        	}
        	mnemonic = ElanLocale.getString("MNEMONIC.DetachedFrame.Minimize");
        	if (mnemonic.length() > 0) {
        		minimizeItem.setMnemonic(mnemonic.charAt(0));
        	}
        } catch (NumberFormatException nfe) {
        	
        }
    }
	*/
    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    /*
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(restoreItem)) {
            setFrameRestored();
        } else if (e.getSource().equals(minimizeItem)) {
            setFrameMinimized();
        } else if (e.getSource().equals(maximizeItem)) {
            setFrameMaximized();
        }
    }
    */
    /*
    private void setFrameRestored() {
        if ((restoreLocation != null) && (restoreSize != null)) {
            setLocation((int) restoreLocation.getX(),
                (int) restoreLocation.getY());
            setSize((int) restoreSize.getWidth(), (int) restoreSize.getHeight());
        }

        bRestored = true;
    }

    private void setFrameMinimized() {
        if (bRestored == true) {
            getRestoreValues();
            bRestored = false;
        }

        setSize(100, 50);
		validate();
    }
	*/
    /*
    private void setFrameMaximized() {
        
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (int i = 0; i < gds.length; i++) {
        	System.out.println("Full screen: " + gds[i].isFullScreenSupported());
        	System.out.println("Display change: " + gds[i].isDisplayChangeSupported());
        	GraphicsConfiguration[] gcs = gds[i].getConfigurations();
        	for (int j = 0; j < gcs.length; j++) {
        		System.out.println("" + i + " - " + j + "  " + gcs[j].getBounds());	
        	}
        }
        gds[1].setFullScreenWindow(this);
        if (1+1==2) return;
        
        if (bRestored == true) {
            getRestoreValues();
            bRestored = false;
        }

        Dimension dimScreen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(0, 0);
        setSize((int) dimScreen.getWidth(), (int) dimScreen.getHeight());
    }
*/
    /*
    private void getRestoreValues() {
        restoreLocation = getLocation();
        restoreSize = getSize();
        System.out.println("Loc: " + restoreLocation + " Size: " + restoreSize);
    }
*/
    /**
     * DOCUMENT ME!
     *
     * @param aspectRatio DOCUMENT ME!
     */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    
    /**
     * When the frame is created or after a change in keyboard shortcuts, the detached
     * frames need to be updating their input map and action map as well.
     */
    public void updateShortcuts() {
      InputMap rootMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      InputMap parentFrameMap = layoutManager.getElanFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      rootMap.clear();
      
      KeyStroke[] keys = parentFrameMap.allKeys();
      for (KeyStroke ks : keys) {
      	rootMap.put(ks, parentFrameMap.get(ks));
      }
      // link the action map to the action map of the parent frame
      getRootPane().setActionMap(layoutManager.getElanFrame().getRootPane().getActionMap());
    }
    
    /**
     * When the frame is closed (attached) remove references to objects in the main window.
     */
    public void resetShortcutMaps() {
    	InputMap rootMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    	rootMap.clear();
    	if (origActionMap != null) {
    		getRootPane().setActionMap(origActionMap);
    	}
    }

    /*
     *
     */
    @Override
	public void componentHidden(ComponentEvent e) {
    }

    /*
     *
     */
    @Override
	public void componentMoved(ComponentEvent e) {
    }

    /*
     *
     */
    @Override
	public void componentResized(ComponentEvent e) {
        if (aspectRatio != 0) {
            int w = getContentPane().getWidth() - 2 * margin;
            int h = getContentPane().getHeight() - 2 * margin;

            if (w > (h * aspectRatio)) {
                int xMargin = (int) ((w - (h * aspectRatio)) / 2) + margin;
                component.setBounds(xMargin, margin, (int) (h * aspectRatio), h);
            } else if (w < (h * aspectRatio)) {
                //				int yMargin = (int) ((h - w / aspectRatio) / 2);
                component.setBounds(margin, margin, w, (int) (w / aspectRatio));
            }
        }
    }

    /*
     *
     */
    @Override
	public void componentShown(ComponentEvent e) {
    }

    /**
     * Translate an exit into an attach
     */

    
       private class FrameWindowListener extends WindowAdapter {
           // triggered when the window is closed by clicking on the cross in the upper right corner
           @Override
		public void windowClosing(WindowEvent e) {
               layoutManager.attach(component);
           }
       }
     
}
