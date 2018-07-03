package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMacApplication;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SIMPLELAN;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.CommandAction;
//import mpi.eudico.client.annotator.ElanFrame2.ElanFrameWindowListener;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.commands.global.MenuAction;
import mpi.eudico.client.annotator.gui.ElanMenuItem;
import mpi.eudico.client.annotator.layout.TurnsAndSceneManager;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.turnsandscenemode.commands.AboutTaSMA;
import mpi.eudico.client.annotator.turnsandscenemode.commands.ExitTaSMA;
import mpi.eudico.client.annotator.turnsandscenemode.commands.HelpTaSMA;
import mpi.eudico.client.annotator.turnsandscenemode.commands.NewTaSMA;
import mpi.eudico.client.annotator.turnsandscenemode.commands.OpenTaSMA;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mac.MacApplicationListener;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Customized frame for the simplified annotation layout. 
 * Initially this will be a single layer/single tier working mode.
 *
 */
@SuppressWarnings("serial")
public class TaSFrame extends ElanFrame2 {
	JMenuItem helpMI;
	protected ElanMenuItem exportRegularEAFMI;
	private JPanel emptyFramePanel;
	private SLWindowListener windowListener;

	public TaSFrame() {
		super();
		applicationName = SIMPLELAN.getApplicationName() + " " + SIMPLELAN.getVersionString();
		setTitle(applicationName);
		setFrameIcon();
	}
	
	/**
	 * 
	 * @param path can be a single layer eaf or an audio or video file.
	 */
	public TaSFrame(String path) {
		this();
		
//		System.out.println("File: " + path);
		if (path != null) {
			openEAF(path);
		} else {
			ClientLogger.LOG.info("File path is null");
		}
	}
	
	/**
	 * The Simplified ELAN application is (for the time being) a single window, single document application.
	 * This allows to set the transcription for the application.
	 *  
	 * @param nextTranscription the next transcription to work on
	 */
	@Override
	public void setTranscription(Transcription nextTranscription) {
		if (this.transcriptionForThisFrame != null) {
			// could clean up and then set the new transcription. For now
			// the assumption is that the cleaning up has been done or the frame is still empty.
			// throw exception or show an error message pane
			ClientLogger.LOG.warning("There is already a transcription loaded in this frame");
			return;
		}
		if (nextTranscription == null) {
			// could interpret this as a "clean up" request?
			ClientLogger.LOG.warning("There new transcription is null");
			return;
		}
		
		//super.setTranscription(nextTranscription);
		this.transcriptionForThisFrame = nextTranscription;
		initElan();
	}
	
	/**
	 * Sets the icon for the frame.
	 * Could move the retrieval of the icon to the main class or Constants.
	 */
	@Override
	protected void setFrameIcon() {
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/SIMPLE-ELAN16.png"));
        setIconImage(icon.getImage());
	}
	
	/**
	 * Creates its own, reduced version of a menu bar.
	 * Might later be changed to enable/disable menus in the "usual" way.
	 *  
	 */
	@Override
	protected void initMenuBar() {
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
        //make menu visible / appear above heavy weight video
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
		MenuAction ma = null;
		
        ma = new MenuAction("Menu.File");
        menuFile = new JMenu(ma);
        menuActions.put("Menu.File", ma);
        menuBar.add(menuFile);
        
        ma = new NewTaSMA(ELANCommandFactory.NEW_DOC, this);
        menuActions.put(ELANCommandFactory.NEW_DOC, ma);
        menuItemFileNew = new JMenuItem(ma);
        menuFile.add(menuItemFileNew);

        ma = new OpenTaSMA(ELANCommandFactory.OPEN_DOC, this);
        menuActions.put(ELANCommandFactory.OPEN_DOC, ma);
        menuItemFileOpen = new JMenuItem(ma);
        menuFile.add(menuItemFileOpen);
        

        ma = new MenuAction(ELANCommandFactory.CLOSE);
        closeMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.CLOSE, ma);
        menuFile.add(closeMI);
        menuFile.addSeparator();
		
		ma = new MenuAction(ELANCommandFactory.SAVE);
		saveMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SAVE, ma);
		menuFile.add(saveMI);
		
		ma = new MenuAction(ELANCommandFactory.SAVE_AS);
		saveAsMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SAVE_AS, ma);
		menuFile.add(saveAsMI);
		
		//EXPORT_REGULAR_MULTITIER_EAF
		ma = new MenuAction(ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF);
		exportRegularEAFMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF, ma);
		menuFile.add(exportRegularEAFMI);
		menuFile.addSeparator();
		
        ma = new MenuAction("Menu.File.Backup.Auto");
        menuActions.put("Menu.File.Backup.Auto",ma);
        menuBackup = new JMenu(ma);
        menuBackup.setEnabled(false);
        menuFile.add(menuBackup);
		menuFile.addSeparator();
		
        ma = new ExitTaSMA(ELANCommandFactory.EXIT, this);
        menuActions.put(ELANCommandFactory.EXIT, ma);
        menuItemFileExit = new JMenuItem(ma);
        menuFile.add(menuItemFileExit);

        ma = new MenuAction("Menu.Edit");
        menuActions.put("Menu.Edit", ma);
        menuEdit = new JMenu(ma);
        menuBar.add(menuEdit); 
 
        ma = new MenuAction(ELANCommandFactory.UNDO);
        undoMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.UNDO, ma);
        menuEdit.add(undoMI);
        ma = new MenuAction(ELANCommandFactory.REDO);
        redoMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.REDO, ma);
        menuEdit.add(redoMI);
		
        ma = new MenuAction("Menu.Help");
        menuActions.put("Menu.Help", ma);
        menuHelp = new JMenu(ma);
        menuBar.add(menuHelp);

        
        ma = new HelpTaSMA(ELANCommandFactory.HELP, this);
        menuActions.put(ELANCommandFactory.HELP, ma);
        menuHelp.add(new JMenuItem(ma));
        
        ma = new AboutTaSMA(ELANCommandFactory.ABOUT, this);
        menuActions.put(ELANCommandFactory.ABOUT, ma);
        menuHelp.add(new JMenuItem(ma));

        updateLocale();
	}
	
	private void initSizeAndLocation() {
		setLocation(0, 0);
		setSize(600, 400);
	}
	
	private void initComponents() {
		//initMenuBar();
		initSizeAndLocation();
		
		updateLocale();
		windowListener = new SLWindowListener();
		addWindowListener(windowListener);
		addWindowFocusListener(windowListener);
		addWindowStateListener(windowListener);
		getContentPane().addComponentListener(new SLComponentListener());
	}
	
	@Override
	public void updateLocale() {
		if (menuBar != null) {
			super.updateLocale();
//			fileMenu.setText(ElanLocale.getString("Menu.File"));
//			newMI.setText(ElanLocale.getString(ELANCommandFactory.NEW_DOC));
//			openMI.setText(ElanLocale.getString(ELANCommandFactory.OPEN_DOC));
//			closeMI.setText(ElanLocale.getString(ELANCommandFactory.CLOSE));
//			saveMI.setText(ElanLocale.getString(ELANCommandFactory.SAVE));
//			saveAsMI.setText(ElanLocale.getString(ELANCommandFactory.SAVE_AS));
//			exitMI.setText(ElanLocale.getString(ELANCommandFactory.EXIT));
//			
//			editMenu.setText(ElanLocale.getString("Menu.Edit"));
//			undoMI.setText(ElanLocale.getString(ELANCommandFactory.UNDO));
//			redoMI.setText(ElanLocale.getString(ELANCommandFactory.REDO));
//			
//			helpMenu.setText(ElanLocale.getString("Menu.Help"));
//			helpMI.setText(ElanLocale.getString(ELANCommandFactory.HELP));
		}
	}
	
	@Override
	protected void initElan() {
		viewerManager = new ViewerManager2((TranscriptionImpl)transcriptionForThisFrame);  
        layoutManager = new ElanLayoutManager(this, viewerManager);
        
        ELANCommandFactory.addDocument(this, viewerManager, layoutManager); 
        getContentPane().remove(emptyFramePanel);
        layoutManager.changeMode(ElanLayoutManager.TURN_SCENE_MODE);
        //pvMenuManager = new PlayerViewerMenuManager(this, transcriptionForThisFrame);
        //long time = System.currentTimeMillis();
        //System.out.println("B: " + 0);
        //MediaDescriptorUtil.createMediaPlayers((TranscriptionImpl) transcriptionForThisFrame,
        //    transcriptionForThisFrame.getMediaDescriptors());
        MediaDescriptorUtil.createMediaPlayers((TranscriptionImpl) transcriptionForThisFrame,
        		transcriptionForThisFrame.getMediaDescriptors());// for now
        
        //wfvMenuManager = new WaveFormViewerMenuManager(this, transcriptionForThisFrame);

        // if there is a signal viewer its mediafile is the first in the list       
        ArrayList<String> audioPaths = new ArrayList<String>(4);   
       	if (layoutManager.getSignalViewer() != null) {
       		audioPaths.add(layoutManager.getSignalViewer().getMediaPath());
       	}        	    	

        ElanLocale.addElanLocaleListener(transcriptionForThisFrame, this);
        
        setFrameTitle();
        
        initMenusAndCommands();   
        
		layoutManager.changeMode(ElanLayoutManager.TURN_SCENE_MODE);
	
				
		Preferences.addPreferencesListener(transcriptionForThisFrame, layoutManager);
	    Preferences.addPreferencesListener(transcriptionForThisFrame, this);
	    Preferences.notifyListeners(transcriptionForThisFrame);
		
        // a few prefs to load only on load
        loadPreferences();

        // instantiate Viewer components:
        //  use CommandActions for relevant toolbar buttons and combobox menu items
        //  position and size components using LayoutManager
        layoutManager.doLayout();
        
        //  this sucks but is needed to ensure visible video on the mac
        //viewerManager.getMasterMediaPlayer().setMediaTime(0);
        initialized = true;
	}

	/**
	 * Overridden temporarily? could use the super implementation if preferences
	 * keys are turned into fields etc.
	 */
	@Override
	protected void initFrame() {
		Locale savedLocale = (Locale) Preferences.get("Locale", null);

        if (savedLocale != null) {
            ElanLocale.setLocale(savedLocale);
        }
        // create the initial menu items
        initMenuBar();

        setEmptyLayout();
        // listen for WindowEvents events on the ElanFrame
        //ElanFrameWindowListener windowList = new ElanFrameWindowListener();
		windowListener = new SLWindowListener();
		addWindowListener(windowListener);
		addWindowFocusListener(windowListener);
		addWindowStateListener(windowListener);
        //addComponentListener(windowListener);

        // require the program to handle the operation in the
        // windowClosing method of a registered WindowListener object.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        pack();
        
        //updateShortcutMap(null);

        Dimension d = Preferences.getDimension("FrameSize-TaS", transcriptionForThisFrame);
        
        if (d == null) {
        	d = Preferences.getDimension("FrameSize-TaS", null);
        }

        // HS June 2010 correct dimension and location if they don't fit on the screen?
        // or make this a user preference?
        Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        
        if (d != null) {      	
        	if (d.width > wRect.width) {
        		d.setSize(wRect.width, d.height);
        	}
        	if (d.height > wRect.getHeight()) {
        		d.setSize(d.width, wRect.height);
        	}
            setSize(d);
        } else {
        	setSize(800, 600);
        }
        Point p = Preferences.getPoint("FrameLocation-TaS", transcriptionForThisFrame);
        
        if (p == null) {
        	p = Preferences.getPoint("FrameLocation-TaS", null);
        }

        if (p != null) {
        	// HS 08-2012 only adjust the location if the upper left corner is outside of the 
        	// screen or within some screen inset
        	if (p.x < wRect.x) {
        		p.x = wRect.x;
        	} else if (p.x > wRect.width - WINDOW_POS_MARGIN) {
        		p.x = wRect.width - WINDOW_POS_MARGIN;// or position at left side of the screen?
        	}
//        	int mpx = wRect.width - getWidth();
//        	if (p.x > mpx && mpx > 0) {
//        		p.x = mpx;
//        	}
        	if (p.y < wRect.y) {
        		p.y = wRect.y;
        	} else if (p.y > wRect.height - WINDOW_POS_MARGIN) {
        		p.y = wRect.height - WINDOW_POS_MARGIN;
        	}
//        	int mph = wRect.height - getHeight();
//        	if (p.y > mph && mph > 0) {
//        		p.y = mph;
//        	}
            setLocation(p);
        } else {
        	setLocation((int) ((wRect.getWidth() / 2) - (getWidth() / 2)), 
        			(int) ((wRect.getHeight() / 2) - (getHeight() / 2)));
        }
        // the call to setVisible should be moved to (a new thread created in) 
        // the class that called a constructor....
        setVisible(true);

	}

	@Override
	protected void initMenusAndCommands() {
        //MenuAction ma;
        menuActions.remove(ELANCommandFactory.CLOSE);
        Action closeAction = ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.CLOSE);
        KeyStroke closeKS = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.CLOSE, 
				ELANCommandFactory.TURNS_SCENE_MODE);
        closeAction.putValue(Action.ACCELERATOR_KEY, closeKS);
        closeMI.setAction(closeAction, true);
        
        menuActions.remove(ELANCommandFactory.SAVE);
        Action saveAction = ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SAVE);
        KeyStroke saveKS = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SAVE, 
				ELANCommandFactory.TURNS_SCENE_MODE);
        saveAction.putValue(Action.ACCELERATOR_KEY, saveKS);
        saveMI.setAction(saveAction, true);
        
        menuActions.remove(ELANCommandFactory.SAVE_AS);
        Action saveAsAction = ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SAVE_AS);
        KeyStroke saveAsKS = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SAVE_AS, 
        		ELANCommandFactory.TURNS_SCENE_MODE);
        saveAsAction.putValue(Action.ACCELERATOR_KEY, saveAsKS);
        saveAsMI.setAction(saveAsAction, true);
        
        menuActions.remove(ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF);
        exportRegularEAFMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF), true);
        
        menuActions.remove(ELANCommandFactory.UNDO);
        CommandAction undoCA = ELANCommandFactory.getUndoCA(transcriptionForThisFrame);
        undoMI.setAction(undoCA);

        menuActions.remove(ELANCommandFactory.REDO);
        CommandAction redoCA = ELANCommandFactory.getRedoCA(transcriptionForThisFrame);
        redoMI.setAction(redoCA);
        

        menuBackup.setEnabled(true);
        ButtonGroup backupGroup = new ButtonGroup();

        // retrieve the stored value for backup interval
        Integer buDelay = Preferences.getInt("BackUpDelay", null);
        JRadioButtonMenuItem neverMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_NEVER));

        if ((buDelay == null) ||
                (buDelay.compareTo(Constants.BACKUP_NEVER) == 0)) {
            neverMI.setSelected(true);
        }

        JRadioButtonMenuItem backup1MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.BACKUP_1));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_1) == 0)) {
            backup1MI.setSelected(true);
        }
        
        JRadioButtonMenuItem backup5MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_5));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_5) == 0)) {
            backup5MI.setSelected(true);
        }

        JRadioButtonMenuItem backup10MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_10));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_10) == 0)) {
            backup10MI.setSelected(true);
        }

        JRadioButtonMenuItem backup20MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_20));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_20) == 0)) {
            backup20MI.setSelected(true);
        }

        JRadioButtonMenuItem backup30MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_30));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_30) == 0)) {
            backup30MI.setSelected(true);
        }

        backupGroup.add(neverMI);
        backupGroup.add(backup1MI);
        backupGroup.add(backup5MI);
        backupGroup.add(backup10MI);
        backupGroup.add(backup20MI);
        backupGroup.add(backup30MI);
        menuBackup.add(neverMI);
        menuBackup.add(backup1MI);
        menuBackup.add(backup5MI);
        menuBackup.add(backup10MI);
        menuBackup.add(backup20MI);
        menuBackup.add(backup30MI);
        
        // temporary? fix for the mismatch in what-is-called-when
        localUpdateShortcutMap();
        //updateShortcutMap(ELANCommandFactory.TURNS_SCENE_MODE);
	}
	
	/**
	 * Resets or un-initializes the menu bar in accordance with the empty state 
	 * of the window. All actions that are connected to a transcription are removed
	 * and replaced with the same kind of place holder actions as used in the initial
	 * empty state of the window.
	 */
	protected void resetMenusAndCommands() {
		if (menuBar != null) {
			// remove actions connected to a transcription, replace by place holder actions
			// close, save, save as, export regular, back up, undo/redo
			MenuAction ma = null;
			
			ma = new MenuAction(ELANCommandFactory.CLOSE);
			closeMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.CLOSE, ma);
			
			ma = new MenuAction(ELANCommandFactory.SAVE);
			saveMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.SAVE, ma);
			
			ma = new MenuAction(ELANCommandFactory.SAVE_AS);
			saveAsMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.SAVE_AS, ma);
			
			ma = new MenuAction(ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF);
			exportRegularEAFMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF, ma);
			
			menuBackup.setEnabled(false);
			menuBackup.removeAll();
			
			ma = new MenuAction(ELANCommandFactory.UNDO);
			undoMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.UNDO, ma);
			
			ma = new MenuAction(ELANCommandFactory.REDO);
			redoMI.setAction(ma, false);
			menuActions.put(ELANCommandFactory.REDO, ma);
			
		}
	}
	
	protected void setEmptyLayout() {
		if (emptyFramePanel == null) {
			emptyFramePanel = new JPanel(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(10, 10, 10, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 0.1;
			
			JPanel openPanel = new JPanel();
			BoxLayout p1BoxLayout = new BoxLayout(openPanel, BoxLayout.Y_AXIS);
			openPanel.setLayout(p1BoxLayout);
			openPanel.setBorder(new TitledBorder(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title")));
			openPanel.add(new JLabel("To open a transcription file"));
			openPanel.add(Box.createRigidArea(new Dimension (0, 5)));
			openPanel.add(new JButton(new OpenTaSMA(ELANCommandFactory.OPEN_DOC, this)));
			openPanel.add(Box.createRigidArea(new Dimension (0, 15)));
			openPanel.add(new JLabel("or drop an .eaf file in this window."));
			
			emptyFramePanel.add(openPanel, gbc);
			
			gbc.gridy = 1;
			
			JPanel createPanel = new JPanel();
			BoxLayout p2BoxLayout = new BoxLayout(createPanel, BoxLayout.Y_AXIS);
			createPanel.setLayout(p2BoxLayout);
			createPanel.setBorder(new TitledBorder(ElanLocale.getString("Frame.ElanFrame.NewDialog.Title")));
			createPanel.add(new JLabel("To create a new transcription file"));
			createPanel.add(Box.createRigidArea(new Dimension (0, 5)));
			createPanel.add(new JButton(new NewTaSMA(ELANCommandFactory.NEW_DOC, this)));
			createPanel.add(Box.createRigidArea(new Dimension (0, 15)));
			createPanel.add(new JLabel("or drop media files in this window."));
	
			emptyFramePanel.add(createPanel, gbc);

			
			emptyFramePanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
					new LineBorder(Color.LIGHT_GRAY, 4, true)));
			//emptyFramePanel.setPreferredSize(new Dimension(300, 300));
		}
		
		if (getContentPane().getLayout() == null) {
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(emptyFramePanel, BorderLayout.CENTER);
			getContentPane().invalidate();
			getContentPane().validate();
		} else {
			getContentPane().add(emptyFramePanel);
		}
	}
	
	private void localUpdateShortcutMap() {
		
		// add some additional actions
		
		if (undoMI != null && undoMI.getAction() != null) {
			Action undoAC = undoMI.getAction();			
    		KeyStroke ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.UNDO, 
    				ELANCommandFactory.TURNS_SCENE_MODE);
    		undoAC.putValue(Action.ACCELERATOR_KEY, ks);
    		if (ks != null) {
    			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, ELANCommandFactory.UNDO);
    			getRootPane().getActionMap().put(ELANCommandFactory.UNDO, undoAC);
    		}
		}

		if (redoMI != null && redoMI.getAction() != null) {
			Action redoAC = redoMI.getAction();
			KeyStroke reKS = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.REDO, 
    				ELANCommandFactory.TURNS_SCENE_MODE);
			redoAC.putValue(Action.ACCELERATOR_KEY, reKS);
			if (reKS != null) {
    			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(reKS, ELANCommandFactory.REDO);
    			getRootPane().getActionMap().put(ELANCommandFactory.REDO, redoAC);
			}
		}
		
	}

	/**
	 * Temporarily override, doesn't call the super implementation
	 * anymore, to prevent "common" actions / keystroke to be loaded
	 * 
	 * @param modeConstant
	 */
	@Override
	public void updateShortcutMap(String modeConstant) {
		// partly copied from ElanFrame2 
		if(modeConstant != null && !modeConstant.equals(ELANCommandFactory.TURNS_SCENE_MODE)) {
			return;
		}
		
		if (modeConstant != null) {
    		if(getViewerManager() == null || getViewerManager().getTranscription() == null){
        		return;
        	}
		}
        ActionMap actionMap = getRootPane().getActionMap();
    	InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);     
        inputMap.clear();
        actionMap.clear();
        
        Map<String, KeyStroke> shortMap = ShortcutsUtil.getInstance().getShortcutKeysOnlyIn(ELANCommandFactory.TURNS_SCENE_MODE);
        Iterator<Entry<String, KeyStroke>> shortIt = shortMap.entrySet().iterator();
//        String id = "Act-";
//        String nextId;
//        int index = 0;
        
        while(shortIt.hasNext()) {
        	Action action = null;        	
        	Entry<String, KeyStroke> entry = shortIt.next();
        	String actionName = entry.getKey();
        	KeyStroke ks = entry.getValue();
        	String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ks);
        	
        	if (modeConstant != null) {
        		if (!TurnsAndSceneManager.REDEFINED_ACTIONS.contains(actionName)) {
        			action = ELANCommandFactory.getCommandAction(
        					getViewerManager().getTranscription(), actionName);
        		}
        	}
        	if (action != null) {
        		action.putValue(Action.ACCELERATOR_KEY, ks);
//        		nextId = id + index++;
        		if (ks != null) {           		
        			inputMap.put(ks, actionName);
        			actionMap.put(actionName, action);
        		}
        		if(ksDescription != null && ksDescription.trim().length() > 0){
            		String shortValue = (String) action.getValue(Action.SHORT_DESCRIPTION);
            		if(shortValue != null){
            			action.putValue(Action.SHORT_DESCRIPTION, shortValue + " (" + ksDescription+")");
            		}
            	}
        	} else {
        		action = menuActions.get(actionName);
            	if(action != null){                      		
            		action.putValue(Action.ACCELERATOR_KEY, ks);
//            		nextId = id + index++;
            		if (ks != null) {
            			inputMap.put(ks, actionName);
            			actionMap.put(actionName, action);
            		}
            	}
        	}
        }
        
		localUpdateShortcutMap();
	}

	/**
	 * Temporarily override?
	 * @param modeConstant
	 */
	@Override
	public void clearShortcutsMap(String modeConstant) {
		super.clearShortcutsMap(ELANCommandFactory.TURNS_SCENE_MODE);
	}

	@Override
	protected void savePreferences() {
		//super.savePreferences();
        if (viewerManager != null) {
            Preferences.set("MediaTime-TaS",
                viewerManager.getMasterMediaPlayer().getMediaTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("SelectionBeginTime-TaS",
                viewerManager.getSelection().getBeginTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("SelectionEndTime-TaS",
                viewerManager.getSelection().getEndTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("TimeScaleBeginTime-TaS",
                viewerManager.getTimeScale().getBeginTime(),
                transcriptionForThisFrame, false, true);// forces writing
        }
        // replace by setPreference();
        setPreference("Locale", ElanLocale.getLocale(), null);
        // 10-2012 store per transcription. or global if transcription is null
        setPreference("FrameSize-TaS", getSize(), transcriptionForThisFrame);
        // 10-2012 store either for the transcription or global if transcription is null
        Preferences.set("FrameLocation-TaS", getLocation(), transcriptionForThisFrame, false, true);// forces writing
	}

	@Override
	protected void loadPreferences() {
		// super.loadPreferences() also starts a BackUp thread, not sure if that is a problem
        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                Long beginTime = Preferences.getLong("SelectionBeginTime-TaS",
                        transcriptionForThisFrame);
                Long endTime = Preferences.getLong("SelectionEndTime-TaS",
                        transcriptionForThisFrame);

                if ((beginTime != null) && (endTime != null)) {
                    viewerManager.getSelection().setSelection(beginTime.longValue(),
                        endTime.longValue());
                }

                Long mediaTime = Preferences.getLong("MediaTime-TaS",
                        transcriptionForThisFrame);

                if (mediaTime != null) {
                    viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime.longValue());
                }

                Long timeScaleBeginTime = Preferences.getLong("TimeScaleBeginTime-TaS",
                        transcriptionForThisFrame);

                if (timeScaleBeginTime != null) {
                    viewerManager.getTimeScale().setBeginTime(timeScaleBeginTime.longValue());
                }
                
                fullyInitialized = true;
                Preferences.notifyListeners(transcriptionForThisFrame);
                // Sometimes layoutManager is still null at this time
                if (layoutManager != null) {
                	layoutManager.doLayout();
                	getRootPane().revalidate();
                }
                Toolkit.getDefaultToolkit().sync();
            }
        });
		// ensure the preferences are set/applied after the window fully initialized
	}

	@Override
	public void saveAndClose(boolean unregister) {
		super.saveAndClose(false);
	}

	@Override
	public void doClose(boolean unregister) {
		savePreferences();
		
    	if (transcriptionForThisFrame != null) {
  		    layoutManager.isClosing();	// save various preferences in the active mode layout manager
  		    
  		  if (viewerManager != null) {
              viewerManager.cleanUpOnClose();
          } 
      	
  		  if (layoutManager != null) {
  			  layoutManager.cleanUpOnClose();
  			  
  			  this.getContentPane().repaint();
  		  }  
      	
          ELANCommandFactory.removeDocument(viewerManager);
          Preferences.removeDocument(transcriptionForThisFrame);
          // remove this frame as locale listener
          ElanLocale.removeElanLocaleListener(transcriptionForThisFrame);            
          
          transcriptionForThisFrame = null;
          viewerManager = null;
          layoutManager = null;
          // return to the "empty" menu bar
          resetMenusAndCommands();
          setEmptyLayout();
    	} else {
    		// exit the application
    		System.exit(0);
    	}  	    	
	}
	
	/**
	 * Could reuse or extend the window listener class in ElanFrame2
	 * 
	 *
	 */
	private class SLWindowListener extends WindowAdapter {

		@Override
		public void windowOpened(WindowEvent e) {
			if (layoutManager != null) {
				layoutManager.doLayout();
			}
		}

		@Override
		public void windowClosing(WindowEvent e) {
        	if (transcriptionForThisFrame != null) {
        		// do nothing if this is an empty frame because a new
        		// empty frame would be created
        		checkSaveAndClose();	
        	} else {
        		// with an empty window, exit the 
        		// application when the close button is pressed
        		doClose(true);
        	}
		}
		
		
	}
	/**
	 * Might become obsolete after closer integration with the main ELAN frame
	 */
	private class SLComponentListener extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			if (layoutManager != null) {
				layoutManager.doLayout();
			}
		}

		@Override
		public void componentShown(ComponentEvent e) {
			if (layoutManager != null) {
				layoutManager.doLayout();
			}
		}
		
	}
	
	/**
	 * A Mac Application listener mainly to prevent a Quit without saving if the 
	 * Mac Application's Quit menu has been invoked.
	 * This class can be removed once the FrameManager has been modified such that it 
	 * supports a single window mode.
	 * 
	 * @see ElanMacApplication 
	 */
	public class SimpleELANMacApplication implements MacApplicationListener {

		@Override
		public void macHandleQuit() {
			//System.out.println("Mac Handle Quit");
			ExitTaSMA ma = new ExitTaSMA(ELANCommandFactory.EXIT, TaSFrame.this);
			ma.actionPerformed(null);
		}

		@Override
		public void macHandleAbout() {
			AboutTaSMA ma = new AboutTaSMA(ELANCommandFactory.ABOUT, TaSFrame.this);
			ma.actionPerformed(null);
		}

		@Override
		public void macHandlePreferences() {
			JOptionPane.showMessageDialog(TaSFrame.this, 
					ElanLocale.getString("PreferencesDialog.Message.NoConfigurableSettings"), 
					ElanLocale.getString("Message.Info"), JOptionPane.INFORMATION_MESSAGE);
		}

		@Override
		public void macHandleOpenApplication(String fileName) {
			System.out.println("Mac Handle Open Application");
			// stub
		}

		@Override
		public void macHandleOpenFile(String fileName) {
	        if (fileName != null) {
	            File f = new File(fileName);

	            try {
	                if (!f.exists() || f.isDirectory()) {
	                    ClientLogger.LOG.info("Cannot open file: " + fileName);

	                    return;
	                }

	                OpenTaSMA openAction = new OpenTaSMA("", TaSFrame.this);
	                openAction.createFrameForPath(fileName);
	            } catch (Exception ex) {
	                ClientLogger.LOG.info("Cannot open file: " + ex.getMessage());
	            }
	        } else {
	            ClientLogger.LOG.info("No file specified.");
	        }			
		}

		@Override
		public void macHandlePrintFile(String fileName) {
			// stub
			
		}

		@Override
		public void macHandleReOpenApplication(String fileName) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
