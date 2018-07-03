package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SIMPLELAN;
import mpi.eudico.client.annotator.prefs.PreferencesReader;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;

/**
 * A singleton class for handling keyboard shortcuts and their mapping to
 * actions. Any new action that potentially can be triggered via a keyboard
 * shortcut should be added to 1 of the categories in the private method
 * fillActionsMap().
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ShortcutsUtil {
    private static ShortcutsUtil shortcutsUtil;

    /** annotation editing category */
    public static final String ANN_EDIT_CAT = "Frame.ShortcutFrame.Sub.AnnotationEdit";

    /** annotation navigation category */
    public static final String ANN_NAVIGATION_CAT = "Frame.ShortcutFrame.Sub.AnnotationNavigation";

    /** tier and type category */
    public static final String TIER_TYPE_CAT = "Frame.ShortcutFrame.Sub.TierType";

    /** selection category */
    public static final String SELECTION_CAT = "Frame.ShortcutFrame.Sub.Selection";

    /** media navigation category */
    public static final String MEDIA_CAT = "Frame.ShortcutFrame.Sub.MediaNavigation";

    /** document and file i/o category */
    public static final String DOCUMENT_CAT = "Frame.ShortcutFrame.Sub.Document";

    /** miscellaneous category */
    public static final String MISC_CAT = "Frame.ShortcutFrame.Sub.Misc";  
    
    public static final String PREF_FILEPATH = Constants.ELAN_DATA_DIR +
    	System.getProperty("file.separator") + "shortcuts.pfsx";
    
    public static final String NEW_PREF_FILEPATH = Constants.ELAN_DATA_DIR +
		System.getProperty("file.separator") + "shortcuts1.pfsx";
  
    // TODO could/should have an enum for the existing modes ??
    
    /** shortcuttable actions for each mode
     *  structure Map< modeName, Map<category, List<all actions>>>*/ 
    private Map<String, Map<String, List<String>>> shortcuttableActionsMap;
    
    /** shortcut keystrokes for each mode
     *  structure Map< modeName, Map<actionName, keystroke>>*/ 
    private Map<String,Map<String, KeyStroke>> shortcutKeyStrokesMap;
    
    private boolean shortcutClash = false;

    /**
     * Creates the single ShortcutsUtil instance
     */
    private ShortcutsUtil() {
        shortcuttableActionsMap = new LinkedHashMap<String, Map<String, List<String>>>(8);
        shortcutKeyStrokesMap = new LinkedHashMap<String,Map<String, KeyStroke>>(80);
        
        fillActionsMap();
        fillShortcutMap();
    }
    
    /**
     * Returns the single instance of this class
     *
     * @return the single instance of this class
     */
    public static ShortcutsUtil getInstance() {
        if (shortcutsUtil == null) {
            shortcutsUtil = new ShortcutsUtil();
        }

        return shortcutsUtil;
    }
    
    /**
     * Adds the constant identifiers of actions that potentially can be invoked
     * by a keyboard shortcut, to one of the categories of actions. Any new
     * action that is created has to be added here as well, if it has a
     * default shortcut it should be added to {@link #loadDefaultShortcuts()}.
     */
    private void fillActionsMap() {    	
    	shortcuttableActionsMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, getInterlinearModeShortcuttableActionsMap());
    }
    
    /**
     * Read from stored preferences or use defaults.
     */
    private void fillShortcutMap() {
        if (!readCurrentShortcuts()) {
            loadDefaultShortcuts();
        }
    }
    
    /**
     * Load default shortcuts.
     */
    private void loadDefaultShortcuts() {
        // defaults...
    	shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, getInterlinearModeDefaultShortcutsMap());
       
        //add all actions that have no default keystroke
        addActionsWithoutShortcut();
    }  

    private Map<String, List<String>> getAnnotationModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.ANNOTATION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category
    	editActions.add(ELANCommandFactory.NEW_ANNOTATION);
    	editActions.add(ELANCommandFactory.NEW_ANNOTATION_BEFORE);
        editActions.add(ELANCommandFactory.NEW_ANNOTATION_AFTER);
        editActions.add(ELANCommandFactory.KEY_CREATE_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_ANNOTATION_TREE);      
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_HERE);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE);
        editActions.add(ELANCommandFactory.DUPLICATE_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_TO_NEXT_ANNOTATION);        
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_DC);        
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_RIGHT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_RIGHT);        
        editActions.add(ELANCommandFactory.REMOVE_ANNOTATION_VALUE);        
        editActions.add(ELANCommandFactory.DELETE_ANNOTATION);
        editActions.add(ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION);
        editActions.add(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
       
        // actions with no default shortcut key
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        editActions.add(ELANCommandFactory.REGULAR_ANNOTATION_DLG);        
        editActions.add(ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);
        editActions.add(ELANCommandFactory.DELETE_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.DELETE_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_RIGHT_OF);        
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOTATIONS);
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_IN_SELECTION);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_DC_DLG);
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);
        
        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION);
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION);
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION_EDIT);
        navActions.add(ELANCommandFactory.ANNOTATION_UP);
        navActions.add(ELANCommandFactory.ANNOTATION_DOWN);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
        
        List<String> tierActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "tier and type" category
        tierActions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
        tierActions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
       
        List<String> selActions = new ArrayList<String>();        
        selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.CLEAR_SELECTION_AND_MODE);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);
        selActions.add(ELANCommandFactory.SELECTION_MODE);
        selActions.add(ELANCommandFactory.CENTER_SELECTION);
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>();
        
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);        
        medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.NEXT_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
        medNavActions.add(ELANCommandFactory.GO_TO_END);
        medNavActions.add(ELANCommandFactory.GOTO_DLG);
        medNavActions.add(ELANCommandFactory.LOOP_MODE);
        //actions with no default shortcut key
       // medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG);
        medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);
        shortcuttableActions.put(MEDIA_CAT, medNavActions);
        
        List<String> miscActions = new ArrayList<String>();         
        //miscActions.add(ELANCommandFactory.ADD_COMMENT);        
        miscActions.add(ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
        miscActions.add(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);  
        miscActions.add("MultiTierViewer.ShiftToolTip");
        miscActions.add(ELANCommandFactory.ZOOM_IN);
        miscActions.add(ELANCommandFactory.ZOOM_OUT);
        miscActions.add(ELANCommandFactory.ZOOM_DEFAULT);
        miscActions.add(ELANCommandFactory.CYCLE_TIER_SETS);
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    }
    
    // default shortcuts for annotation mode
    private Map<String, KeyStroke> getAnnotationModeDefaultShortcutsMap(){    	
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
    	
    	final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_BEFORE,
 	    	KeyStroke.getKeyStroke(KeyEvent.VK_N,
 	        	menuShortcutKeyMask +
 	            ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_AFTER,
    	 	KeyStroke.getKeyStroke(KeyEvent.VK_N,
    	    	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.KEY_CREATE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
    	 
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_ANNOTATION,
    	 	KeyStroke.getKeyStroke(KeyEvent.VK_C,
    	    	menuShortcutKeyMask));
    	 
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_ANNOTATION_TREE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_C,
    	    	menuShortcutKeyMask +
    	        ActionEvent.ALT_MASK));
    	        
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	menuShortcutKeyMask));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_HERE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	menuShortcutKeyMask +
    	    	ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_TREE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	menuShortcutKeyMask +
    	        ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	     	menuShortcutKeyMask +
    	        ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.DUPLICATE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_D,
    	    	menuShortcutKeyMask));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_TO_NEXT_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_D,
    	    	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME,
        	 	KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
        	    	menuShortcutKeyMask));     
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_DC,
    		KeyStroke.getKeyStroke(KeyEvent.VK_M,
    	    	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG,
        		KeyStroke.getKeyStroke(KeyEvent.VK_M,
        				menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	        
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT,
    	 	KeyStroke.getKeyStroke( KeyEvent.VK_J, 
    	    	ActionEvent.CTRL_MASK));
    	       
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_RIGHT,
    	 	KeyStroke.getKeyStroke( KeyEvent.VK_U, 
    	    	ActionEvent.CTRL_MASK));
    	        
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT,
    		KeyStroke.getKeyStroke( KeyEvent.VK_J, 
    	    	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK ));
      	
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_RIGHT,
    	  	KeyStroke.getKeyStroke( KeyEvent.VK_U, 
    	     	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK ));
      	
      	shortcutKeyStrokes.put(ELANCommandFactory.REMOVE_ANNOTATION_VALUE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.ALT_MASK));
    	       
        shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
            	menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_E, 
            		menuShortcutKeyMask + 
            		ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
             	menuShortcutKeyMask +
            	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION,
        		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION_EDIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
        		menuShortcutKeyMask +
            	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_UP,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_DOWN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ACTIVE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_C,
            	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION_AND_MODE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
            	menuShortcutKeyMask +
                	ActionEvent.ALT_MASK ));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_MODE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_K,
            	menuShortcutKeyMask));

       	shortcutKeyStrokes.put(ELANCommandFactory.CENTER_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_A, 
             	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
       	
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*menuShortcutKeyMask*/
            	ActionEvent.CTRL_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
            	/*menuShortcutKeyMask*/
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION_SLOW,
            	KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.CTRL_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION_NORMAL_SPEED,
            	KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_STEP_AND_REPEAT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 
             	ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
            	menuShortcutKeyMask +
             	ActionEvent.SHIFT_MASK));
           
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
            	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
            	menuShortcutKeyMask));
            
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
         	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
             	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_SCROLLVIEW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
             	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_SCROLLVIEW,
         	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
            	menuShortcutKeyMask));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_B,
             	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
        	KeyStroke.getKeyStroke(KeyEvent.VK_E,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
         	KeyStroke.getKeyStroke(KeyEvent.VK_G,
             	menuShortcutKeyMask));

        shortcutKeyStrokes.put(ELANCommandFactory.LOOP_MODE,
           	 KeyStroke.getKeyStroke(KeyEvent.VK_L,
                 	menuShortcutKeyMask));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_RATE_TOGGLE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_R,
            	menuShortcutKeyMask +
             	ActionEvent.ALT_MASK));

        shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_R,
            	menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_IN, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_OUT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_DEFAULT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_0,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.CYCLE_TIER_SETS,
            	KeyStroke.getKeyStroke(KeyEvent.VK_T,
        	 	//menuShortcutKeyMask +
        		ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
        
        return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getTranscModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.TRANSCRIPTION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category 
    	
    	editActions.add(ELANCommandFactory.COMMIT_CHANGES); 
        editActions.add(ELANCommandFactory.CANCEL_CHANGES);
        editActions.add(ELANCommandFactory.DELETE_ANNOTATION); 
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);  
        
        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        navActions.add(ELANCommandFactory.MOVE_UP);        
        navActions.add(ELANCommandFactory.MOVE_DOWN);        
        navActions.add(ELANCommandFactory.MOVE_LEFT);
        navActions.add(ELANCommandFactory.MOVE_RIGHT);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
    	
    	List<String> selActions = new ArrayList<String>();    	 
    	selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);

        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>(); 
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_FROM_START);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);  
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION_SLOW);  
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.LOOP_MODE);        
        shortcuttableActions.put(MEDIA_CAT, medNavActions); 
        
        List<String> miscActions = new ArrayList<String>(); 
        // has no default shortcuts
        miscActions.add(ELANCommandFactory.EDIT_IN_ANN_MODE);
        miscActions.add(ELANCommandFactory.FREEZE_TIER);  
        miscActions.add(ELANCommandFactory.HIDE_TIER); 
        miscActions.add(ELANCommandFactory.ZOOM_IN);
        miscActions.add(ELANCommandFactory.ZOOM_OUT);
        miscActions.add(ELANCommandFactory.ZOOM_DEFAULT);
        shortcuttableActions.put(MISC_CAT, miscActions);    	
        
        return shortcuttableActions;
    }
    
    private Map<String, KeyStroke> getTranscModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes  = new LinkedHashMap<String, KeyStroke>(80);
    	final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.COMMIT_CHANGES,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CANCEL_CHANGES,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_A, 
            			menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WB,
            	KeyStroke.getKeyStroke(KeyEvent.VK_B, 
            			menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_UP,
            	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_DOWN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_LEFT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_RIGHT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));	
    	    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_C,
                	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
            
		shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                	menuShortcutKeyMask));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
               	menuShortcutKeyMask +
                   	ActionEvent.ALT_MASK ));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE, 
            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_FROM_START, 
            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
          	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
               	/*menuShortcutKeyMask*/
           		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));  
       
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask +
                	ActionEvent.SHIFT_MASK));
              
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	menuShortcutKeyMask +
                   ActionEvent.SHIFT_MASK));
           
       shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
    	   KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask));
               
       shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
            	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                	menuShortcutKeyMask));
       
       shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
               
       shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
      shortcutKeyStrokes.put(ELANCommandFactory.LOOP_MODE,
    	 KeyStroke.getKeyStroke(KeyEvent.VK_L,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_IN, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_OUT, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_DEFAULT, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_0,
          	menuShortcutKeyMask));
      
      return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getSegmentModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.SEGMENTATION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();    	
    	editActions.add(ELANCommandFactory.DELETE_ANNOTATION); 
    	editActions.add(ELANCommandFactory.SEGMENT); 
    	editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG);
        
    	// actions with no default shortcut key    
        editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);        
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);
        
        List<String> tierActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "tier and type" category
        tierActions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
        tierActions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
        
    	List<String> selActions = new ArrayList<String>();    	 
    	selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>();
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);   
        medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);
        medNavActions.add(ELANCommandFactory.PREVIOUS_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.NEXT_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
        medNavActions.add(ELANCommandFactory.GO_TO_END);
        medNavActions.add(ELANCommandFactory.GOTO_DLG);
        
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG);
        medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);
        shortcuttableActions.put(MEDIA_CAT, medNavActions);
        
        List<String> miscActions = new ArrayList<String>();         
        miscActions.add(ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
        miscActions.add(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);
        //actions with no default shortcuts
        miscActions.add(ELANCommandFactory.BULLDOZER_MODE);
        miscActions.add(ELANCommandFactory.TIMEPROP_NORMAL);
        miscActions.add(ELANCommandFactory.SHIFT_MODE);
        miscActions.add(ELANCommandFactory.ZOOM_IN);
        miscActions.add(ELANCommandFactory.ZOOM_OUT);
        miscActions.add(ELANCommandFactory.ZOOM_DEFAULT);
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    }
    
    private Map<String, KeyStroke> getSegmentModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
        final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0 ));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.SEGMENT,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_A, 
            			menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WB,
            	KeyStroke.getKeyStroke(KeyEvent.VK_B, 
            			menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG,
    			KeyStroke.getKeyStroke(KeyEvent.VK_M,
        				menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER,
    			KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ACTIVE_TIER,
    			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));     
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_C,
                	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
            

		shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                	menuShortcutKeyMask));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
               	menuShortcutKeyMask +
                   	ActionEvent.ALT_MASK ));
        
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*menuShortcutKeyMask*/
                	ActionEvent.CTRL_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
          	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
               	/*menuShortcutKeyMask*/
           		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));        
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_STEP_AND_REPEAT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 
               	ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask +
               	ActionEvent.SHIFT_MASK));
               
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
           
       shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask));
                
       shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
             	menuShortcutKeyMask));
       
      shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
    	  KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
          
      shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
    	  KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
      
      shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_SCROLLVIEW,
          	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
               	menuShortcutKeyMask));    
          shortcutKeyStrokes.put(ELANCommandFactory.NEXT_SCROLLVIEW,
           	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
              	menuShortcutKeyMask)); 
      
      shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
      	KeyStroke.getKeyStroke(KeyEvent.VK_B,
           	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
      	KeyStroke.getKeyStroke(KeyEvent.VK_E,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
       	KeyStroke.getKeyStroke(KeyEvent.VK_G,
           	menuShortcutKeyMask));      
      
      shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_RATE_TOGGLE,
      	KeyStroke.getKeyStroke(KeyEvent.VK_R,
          	menuShortcutKeyMask +
           	ActionEvent.ALT_MASK));

      shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE,
      	KeyStroke.getKeyStroke(KeyEvent.VK_R,
          	menuShortcutKeyMask +
          	ActionEvent.SHIFT_MASK));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_IN, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_OUT, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
          	menuShortcutKeyMask));
      
      shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_DEFAULT, 
      		KeyStroke.getKeyStroke(KeyEvent.VK_0,
          	menuShortcutKeyMask));
       
      return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getSyncModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.SYNC_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	 List<String> medNavActions = new ArrayList<String>();
    	 medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
         medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
         medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
         medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
         medNavActions.add(ELANCommandFactory.NEXT_FRAME);
         medNavActions.add(ELANCommandFactory.SECOND_LEFT);
         medNavActions.add(ELANCommandFactory.SECOND_RIGHT);         
         medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
         medNavActions.add(ELANCommandFactory.GO_TO_END);
         medNavActions.add(ELANCommandFactory.GOTO_DLG);         
         //actions without default shortcut key
         medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);         
         shortcuttableActions.put(MEDIA_CAT, medNavActions);
         return shortcuttableActions;
    
    }
    
    private Map<String, KeyStroke> getSyncModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
    	
        final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*menuShortcutKeyMask*/
                	ActionEvent.CTRL_MASK));
            
		shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
              
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	menuShortcutKeyMask));
                
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_B,
             	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
        	KeyStroke.getKeyStroke(KeyEvent.VK_E,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
         	KeyStroke.getKeyStroke(KeyEvent.VK_G,
             	menuShortcutKeyMask));
            
        return shortcutKeyStrokes;       
    }
    
    /**
     * Action map for the simplified, turns-and-scene transcription mode which (initially) is developed as a 
     * separate application.
     * @return
     */
    private Map<String, List<String>> getTurnsAndSceneModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.TURNS_SCENE_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category
    	// splitting an annotation or "gap" is the way to create annotations in this mode
    	editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);
    	editActions.add(ELANCommandFactory.REMOVE_ANNOTATION_VALUE);
    	editActions.add(ELANCommandFactory.DELETE_ANNOTATION);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME);
    	shortcuttableActions.put(ANN_EDIT_CAT, editActions);
    	
        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        // "previous" is an annotation "up" in the table
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION);
        //navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);
        // "next" is an annotation "down" in the table
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION);
        //navActions.add(ELANCommandFactory.NEXT_ANNOTATION_EDIT);
        //navActions.add(ELANCommandFactory.ANNOTATION_UP);
        //navActions.add(ELANCommandFactory.ANNOTATION_DOWN);
        navActions.add(ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME);
        // deactivates the cell editor in this mode but the annotation can still be the active annotation
        navActions.add(ELANCommandFactory.CANCEL_ANNOTATION_EDIT);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
        
        List<String> selActions = new ArrayList<String>();        
        selActions.add(ELANCommandFactory.CLEAR_SELECTION);
//        selActions.add(ELANCommandFactory.CLEAR_SELECTION_AND_MODE);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);
//        selActions.add(ELANCommandFactory.SELECTION_MODE);
        //selActions.add(ELANCommandFactory.CENTER_SELECTION);
        selActions.add(ELANCommandFactory.SELECTION_BEGIN);
        selActions.add(ELANCommandFactory.SELECTION_END);
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
    	 List<String> medNavActions = new ArrayList<String>();
          
         medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
//         medNavActions.add(ELANCommandFactory.PLAY_FROM_START);
         medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
         medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);        
//         medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);//??
         medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
         medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
         medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
         medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
         medNavActions.add(ELANCommandFactory.SECOND_LEFT);
         medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
         medNavActions.add(ELANCommandFactory.PREVIOUS_SCROLLVIEW);
         medNavActions.add(ELANCommandFactory.NEXT_SCROLLVIEW);
         medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
         medNavActions.add(ELANCommandFactory.GO_TO_END);
         medNavActions.add(ELANCommandFactory.GOTO_DLG);
         medNavActions.add(ELANCommandFactory.LOOP_MODE); 
         medNavActions.add(ELANCommandFactory.CONTINUOUS_PLAYBACK_MODE);
         shortcuttableActions.put(MEDIA_CAT, medNavActions);
         
         List<String> miscActions = new ArrayList<String>();         
         miscActions.add(ELANCommandFactory.ZOOM_IN);
         miscActions.add(ELANCommandFactory.ZOOM_OUT);
         miscActions.add(ELANCommandFactory.ZOOM_DEFAULT);
         shortcuttableActions.put(MISC_CAT, miscActions);
         
         return shortcuttableActions;
    
    }
  
    /**
     * 
     * @return a map containing the default shortcuts for the simplified turns and scene transcription mode
     */
    private Map<String, KeyStroke> getTurnsAndSceneModeDefaultShortcutsMap(){
		Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(
				80);

		final int menuShortcutKeyMask = Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask();

		shortcutKeyStrokes.put(ELANCommandFactory.SPLIT_ANNOTATION, 
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		
		shortcutKeyStrokes.put(ELANCommandFactory.REMOVE_ANNOTATION_VALUE, 
//				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.ALT_MASK));
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 
						menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
//				KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
				KeyStroke.getKeyStroke(KeyEvent.VK_D, 
						menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
		
		shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WB,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 
						menuShortcutKeyMask + ActionEvent.SHIFT_MASK));
		
		shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WN,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 
						menuShortcutKeyMask + ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, menuShortcutKeyMask));
						
		// in use when there is no text editor active
		shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
		// in use when a text editor is active
		shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_UP,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, menuShortcutKeyMask));
		// no text editor active
		shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION, 
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
		// a text editor is active
		shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_DOWN,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, menuShortcutKeyMask));
		
		shortcutKeyStrokes.put(ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME, 
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, menuShortcutKeyMask));
		
		shortcutKeyStrokes.put(ELANCommandFactory.CANCEL_ANNOTATION_EDIT, 
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK
						+ ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BEGIN,
				KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, menuShortcutKeyMask));		
		shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_END,
				KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 
						menuShortcutKeyMask + ActionEvent.ALT_MASK));

//        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE, 
//            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
//        
//        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_FROM_START, 
//            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK));
        
		shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /* menuShortcutKeyMask */
						ActionEvent.CTRL_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION, KeyStroke
				.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				/* menuShortcutKeyMask */
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(
				ELANCommandFactory.PIXEL_LEFT,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, menuShortcutKeyMask
						+ ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(
				ELANCommandFactory.PIXEL_RIGHT,
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, menuShortcutKeyMask
						+ ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT, KeyStroke
				.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT, KeyStroke
				.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));

		shortcutKeyStrokes
				.put(ELANCommandFactory.PREVIOUS_SCROLLVIEW, KeyStroke
						.getKeyStroke(KeyEvent.VK_PAGE_UP, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.NEXT_SCROLLVIEW, KeyStroke
				.getKeyStroke(KeyEvent.VK_PAGE_DOWN, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
				KeyStroke.getKeyStroke(KeyEvent.VK_B, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, menuShortcutKeyMask));

		shortcutKeyStrokes.put(ELANCommandFactory.LOOP_MODE,
				KeyStroke.getKeyStroke(KeyEvent.VK_L, menuShortcutKeyMask));
		
		shortcutKeyStrokes.put(ELANCommandFactory.CONTINUOUS_PLAYBACK_MODE,
				KeyStroke.getKeyStroke(KeyEvent.VK_K, menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_IN, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_OUT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_DEFAULT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_0,
            	menuShortcutKeyMask));

		return shortcutKeyStrokes;      
    }
    
    /**
     * 
     * @return the action map for the interlinearization mode
     */
    private Map<String, List<String>> getInterlinearModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.INTERLINEARIZATION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category
    	// to be decided which ones, if any, should be active in Interlinearization mode
//    	editActions.add(ELANCommandFactory.NEW_ANNOTATION);// is based on active tier, selection and/or active annotation
    	editActions.add(ELANCommandFactory.NEW_ANNOTATION_BEFORE);
        editActions.add(ELANCommandFactory.NEW_ANNOTATION_AFTER);
//        editActions.add(ELANCommandFactory.KEY_CREATE_ANNOTATION);// based on begin time and end time
//        editActions.add(ELANCommandFactory.COPY_ANNOTATION);// based on active annotation
//        editActions.add(ELANCommandFactory.COPY_ANNOTATION_TREE);      
//        editActions.add(ELANCommandFactory.PASTE_ANNOTATION);// based on active tier and/or time position
//        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_HERE);
//        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE);
//        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE);
//        editActions.add(ELANCommandFactory.DUPLICATE_ANNOTATION);
//        editActions.add(ELANCommandFactory.COPY_TO_NEXT_ANNOTATION);// based on active annotation        
//        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION);// based on active annotation
//        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME);// time based, not possible in this mode
//        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_DC);// active annotation        
//        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT);// time based, not possible in this mode
//        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_RIGHT);
//        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT);
//        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_RIGHT);        
//        editActions.add(ELANCommandFactory.REMOVE_ANNOTATION_VALUE);// active annotation        
        editActions.add(ELANCommandFactory.DELETE_ANNOTATION);
//        editActions.add(ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION);// time based
        editActions.add(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
       
        // actions with no default shortcut key
//        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);// active annotation
//        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
//        editActions.add(ELANCommandFactory.REGULAR_ANNOTATION_DLG);// although this could be a way to start in  this text only mode       
//        editActions.add(ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);// time selection based
//        editActions.add(ELANCommandFactory.DELETE_ANNOS_LEFT_OF);//??
//        editActions.add(ELANCommandFactory.DELETE_ANNOS_RIGHT_OF);
//        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_LEFT_OF);//??
//        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_RIGHT_OF);//??        
//        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOTATIONS);// time based, but would be possible?
//        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_LEFT_OF);
//        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_RIGHT_OF);
//        editActions.add(ELANCommandFactory.SHIFT_ANNOS_IN_SELECTION);// needs selection
//        editActions.add(ELANCommandFactory.SHIFT_ANNOS_LEFT_OF);
//        editActions.add(ELANCommandFactory.SHIFT_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);
//        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_DC_DLG);// active annotation
        editActions.add(ELANCommandFactory.CREATE_DEPEND_ANN);
        editActions.add(ELANCommandFactory.ANALYZE_ANNOTATION);
        editActions.add(ELANCommandFactory.ADD_TO_LEXICON);
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);

        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        // only if active annotation is used
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION);
//        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);// command only implemented for Annotation mode
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION);
//        navActions.add(ELANCommandFactory.NEXT_ANNOTATION_EDIT);
        navActions.add(ELANCommandFactory.ANNOTATION_UP);
        navActions.add(ELANCommandFactory.ANNOTATION_DOWN);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
        
        List<String> tierActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "tier and type" category
        // only if active tier concept is used
//        tierActions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
//        tierActions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
       
        // only if Selection is used and even then it is questionable
        List<String> selActions = new ArrayList<String>();        
//        selActions.add(ELANCommandFactory.CLEAR_SELECTION);
//        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);// no sense without a player with timeline??
//        selActions.add(ELANCommandFactory.SELECTION_CENTER);
//        selActions.add(ELANCommandFactory.SELECTION_MODE);// makes no sense
//        selActions.add(ELANCommandFactory.CENTER_SELECTION);//??
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>();
        // media navigation is not applicable if there is no player and/or no selection
        // if a player is there (invisible) then playing an annotation is probably implemented separately
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);        
//        medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);
//        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
//        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
//        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
//        medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
//        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
//        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
//        medNavActions.add(ELANCommandFactory.PREVIOUS_SCROLLVIEW);
//        medNavActions.add(ELANCommandFactory.NEXT_SCROLLVIEW);
//        medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);//??
//        medNavActions.add(ELANCommandFactory.GO_TO_END);//??
        medNavActions.add(ELANCommandFactory.GOTO_DLG);// maybe, if the viewer listens to controllerUpdates?
        medNavActions.add(ELANCommandFactory.LOOP_MODE);// maybe if selection is used
        //actions with no default shortcut key
       // medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG);
//        medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);
        shortcuttableActions.put(MEDIA_CAT, medNavActions);
        // misc
        List<String> miscActions = new ArrayList<String>();                 
//        miscActions.add(ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
//        miscActions.add(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);  
        miscActions.add(ELANCommandFactory.ZOOM_IN);// if the editor/viewer implements zoomable
        miscActions.add(ELANCommandFactory.ZOOM_OUT);
        miscActions.add(ELANCommandFactory.ZOOM_DEFAULT);
//        miscActions.add(ELANCommandFactory.CYCLE_TIER_SETS);// if this mode supports tier sets
        shortcuttableActions.put(MISC_CAT, miscActions);
        
    	return shortcuttableActions;
    }
    
    /**
     * 
     * @return a map containing the default shortcuts for the interlinearization mode
     */
    private Map<String, KeyStroke> getInterlinearModeDefaultShortcutsMap(){
		Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(
				10);
		final int menuShortcutKeyMask = Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask();
		// some annotation editing actions could be considered if active annotation and selection are activated
//    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION,
//            	KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		// same for annotation navigation

		// edit active annotation currently only implemented for Annotation Mode (2 viewers)
//        shortcutKeyStrokes.put(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT,
//            	KeyStroke.getKeyStroke(KeyEvent.VK_E, 
//            		menuShortcutKeyMask + 
//            		ActionEvent.SHIFT_MASK));
		
        shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        shortcutKeyStrokes.put(ELANCommandFactory.CREATE_DEPEND_ANN,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, menuShortcutKeyMask + 
                		ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
		shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_BEFORE,
	 	    	KeyStroke.getKeyStroke(KeyEvent.VK_N,
	 	        	menuShortcutKeyMask +
	 	            ActionEvent.SHIFT_MASK));	    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_AFTER,
    	 	KeyStroke.getKeyStroke(KeyEvent.VK_N,
    	    	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
    	shortcutKeyStrokes.put(ELANCommandFactory.SPLIT_ANNOTATION,
        	 	KeyStroke.getKeyStroke(KeyEvent.VK_J, menuShortcutKeyMask));
    	shortcutKeyStrokes.put(ELANCommandFactory.ANALYZE_ANNOTATION,
        	 	KeyStroke.getKeyStroke(KeyEvent.VK_I, menuShortcutKeyMask));
    	shortcutKeyStrokes.put(ELANCommandFactory.ADD_TO_LEXICON,
        	 	KeyStroke.getKeyStroke(KeyEvent.VK_L, menuShortcutKeyMask));
        shortcutKeyStrokes.put(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_E, 
            		menuShortcutKeyMask + 
            		ActionEvent.SHIFT_MASK));
    	
	    	
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        	
//        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT,
//            	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
//                 	menuShortcutKeyMask +
//                	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
            
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION,
        		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
            
//        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION_EDIT,
//        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
//        		menuShortcutKeyMask +
//            	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_UP,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_DOWN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
		// player actions??
//        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
//            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*menuShortcutKeyMask*/
//                	ActionEvent.CTRL_MASK));
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
             	KeyStroke.getKeyStroke(KeyEvent.VK_G,
                 	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_IN, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_OUT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
            	menuShortcutKeyMask));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ZOOM_DEFAULT, 
        		KeyStroke.getKeyStroke(KeyEvent.VK_0,
            	menuShortcutKeyMask));
    	
		return shortcutKeyStrokes;
    }
    
    /**
     * The following CommandActions don't need shortcut keys:
     * 
     * SET_TIER_NAME needs parameters (is not a proper CommandAction)
     * SYNTAX_VIEWER mpi.syntax.elan.ElanSyntaxViewer does not exist
     * COPY_TIER is not a CommandAction (but COPY_TIER_DLG borrows its menu entry text)
     * FILTER_TIER is not a CommandAction (but FILTER_TIER_DLG borrows its menu entry text)
     * DELETE_TIERS is a functional duplicate of DELETE_TIER
     * CLEAR_SELECTION_ALT represents a weird subclass of CLEAR_SELECTION with a fixed shortcut and seems unused (remove?)
     * DELETE_ANNOTATION_ALT represents a weird subclass of DELETE_ANNOTATION with a fixed shortcut and seems unused (remove?)
     * MODIFY_ANNOTATION_ALT represents a weird subclass of MODIFY_ANNOTATION with a fixed shortcut and seems unused (remove?)
     * NEW_ANNOTATION_ALT represents a weird subclass of NEW_ANNOTATION with a fixed shortcut and seems unused (remove?)
     * SELECTION_BOUNDARY_ALT 
     */
    private Map<String, List<String>> getCommonShortcuttableActionsMap(){   
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);  
    	
    	List<String> tierActions = new ArrayList<String>();
        tierActions.add(ELANCommandFactory.ADD_TIER);
        tierActions.add(ELANCommandFactory.DELETE_TIER);
        tierActions.add(ELANCommandFactory.ADD_TYPE);        
     // actions with no default shortcut key
        tierActions.add(ELANCommandFactory.CHANGE_TIER);
        tierActions.add(ELANCommandFactory.REPARENT_TIER);
        tierActions.add(ELANCommandFactory.TOKENIZE_DLG);
        tierActions.add(ELANCommandFactory.FILTER_TIER_DLG);
        tierActions.add(ELANCommandFactory.COPY_TIER_DLG);
        tierActions.add(ELANCommandFactory.ANN_FROM_OVERLAP);
        tierActions.add(ELANCommandFactory.MERGE_TIERS);
        tierActions.add(ELANCommandFactory.ANN_FROM_GAPS);
        tierActions.add(ELANCommandFactory.CHANGE_CASE);
        tierActions.add(ELANCommandFactory.ANNOTATOR_COMPARE_MULTI);
        tierActions.add(ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES);
        tierActions.add(ELANCommandFactory.ANN_ON_DEPENDENT_TIER);
        tierActions.add(ELANCommandFactory.LABEL_AND_NUMBER);
        tierActions.add(ELANCommandFactory.TIER_DEPENDENCIES);
        tierActions.add(ELANCommandFactory.CHANGE_TYPE);
        tierActions.add(ELANCommandFactory.DELETE_TYPE);
        tierActions.add(ELANCommandFactory.ADD_PARTICIPANT);        
        tierActions.add(ELANCommandFactory.ANNOTATIONS_TO_TIERS);  
        tierActions.add(ELANCommandFactory.ANN_FROM_OVERLAP_CLAS);  
        tierActions.add(ELANCommandFactory.ANN_FROM_SUBTRACTION); 
        tierActions.add(ELANCommandFactory.MERGE_TIERS_CLAS);
        tierActions.add(ELANCommandFactory.MERGE_TIER_GROUP);
        tierActions.add(ELANCommandFactory.CREATE_DEPEND_ANN);
        tierActions.add(ELANCommandFactory.NEW_ANNOTATION_REC);
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
        
        List<String> docActions = new ArrayList<String>();
        docActions.add(ELANCommandFactory.NEW_DOC);
        docActions.add(ELANCommandFactory.OPEN_DOC);
        docActions.add(ELANCommandFactory.SAVE);
        docActions.add(ELANCommandFactory.SAVE_AS);
        docActions.add(ELANCommandFactory.SAVE_AS_TEMPLATE);        
        docActions.add(ELANCommandFactory.PRINT);
        docActions.add(ELANCommandFactory.PREVIEW);
        docActions.add(ELANCommandFactory.PAGESETUP);
        docActions.add(ELANCommandFactory.NEXT_WINDOW);
        docActions.add(ELANCommandFactory.PREV_WINDOW);
        docActions.add(ELANCommandFactory.CLOSE);
        docActions.add(ELANCommandFactory.EXIT);
        //actions with no default shortcut key
        docActions.add(ELANCommandFactory.SAVE_SELECTION_AS_EAF);
        docActions.add(ELANCommandFactory.MERGE_TRANSCRIPTIONS);
        docActions.add(ELANCommandFactory.IMPORT_SHOEBOX);
        docActions.add(ELANCommandFactory.IMPORT_TOOLBOX);
        docActions.add(ELANCommandFactory.IMPORT_FLEX);
        docActions.add(ELANCommandFactory.IMPORT_CHAT);
        docActions.add(ELANCommandFactory.IMPORT_TRANS);
        docActions.add(ELANCommandFactory.IMPORT_TAB);
        docActions.add(ELANCommandFactory.IMPORT_PRAAT_GRID);
        docActions.add(ELANCommandFactory.IMPORT_PREFS);
        docActions.add(ELANCommandFactory.IMPORT_TIERS);
        docActions.add(ELANCommandFactory.IMPORT_TYPES);
        
        docActions.add(ELANCommandFactory.IMPORT_FLEX_MULTI);
        docActions.add(ELANCommandFactory.IMPORT_PRAAT_GRID_MULTI);
        docActions.add(ELANCommandFactory.IMPORT_TOOLBOX_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_TOOLBOX_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_FLEX_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_TAB_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_PRAAT_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_ANNLIST_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_WORDLIST_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_TIERS_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_OVERLAPS_MULTI);
        
        docActions.add(ELANCommandFactory.EXPORT_FILMSTRIP);
        docActions.add(ELANCommandFactory.EXPORT_HTML);
        docActions.add(ELANCommandFactory.EXPORT_IMAGE_FROM_WINDOW);
        docActions.add(ELANCommandFactory.EXPORT_INTERLINEAR);
        docActions.add(ELANCommandFactory.EXPORT_MEDIA);
        docActions.add(ELANCommandFactory.CLIP_MEDIA);
        docActions.add(ELANCommandFactory.EXPORT_PRAAT_GRID);
        docActions.add(ELANCommandFactory.EXPORT_PREFS);
        docActions.add(ELANCommandFactory.EXPORT_QT_SUB);
        docActions.add(ELANCommandFactory.EXPORT_SHOEBOX);
        docActions.add(ELANCommandFactory.EXPORT_SMIL_RT);               
        docActions.add(ELANCommandFactory.EXPORT_SMIL_QT);
        docActions.add(ELANCommandFactory.EXPORT_SUBTITLES);
        docActions.add(ELANCommandFactory.EXPORT_TAB);
        docActions.add(ELANCommandFactory.EXPORT_RECOG_TIER);
        docActions.add(ELANCommandFactory.EXPORT_TIGER);
        docActions.add(ELANCommandFactory.EXPORT_TOOLBOX);
        docActions.add(ELANCommandFactory.EXPORT_FLEX);
        docActions.add(ELANCommandFactory.EXPORT_TRAD_TRANSCRIPT);
        docActions.add(ELANCommandFactory.EXPORT_WORDS);
        docActions.add(ELANCommandFactory.EXPORT_CHAT);
        docActions.add(ELANCommandFactory.EXPORT_EAF_2_7);
        docActions.add(ELANCommandFactory.EXPORT_TEX);
        docActions.add(ELANCommandFactory.BACKUP);
        docActions.add(ELANCommandFactory.BACKUP_NEVER);
        docActions.add(ELANCommandFactory.BACKUP_1);
        docActions.add(ELANCommandFactory.BACKUP_5);
        docActions.add(ELANCommandFactory.BACKUP_10);
        docActions.add(ELANCommandFactory.BACKUP_20);
        docActions.add(ELANCommandFactory.BACKUP_30);
        docActions.add(ELANCommandFactory.SET_PAL);
        docActions.add(ELANCommandFactory.SET_NTSC);
        docActions.add(ELANCommandFactory.IMPORT_RECOG_TIERS);
        shortcuttableActions.put(DOCUMENT_CAT, docActions);
        
        List<String> miscActions = new ArrayList<String>();
        miscActions.add(ELANCommandFactory.UNDO);
        miscActions.add(ELANCommandFactory.REDO);
        miscActions.add(ELANCommandFactory.SEARCH_DLG);
        miscActions.add(ELANCommandFactory.SEARCH_MULTIPLE_DLG);
        miscActions.add(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG);
        miscActions.add(ELANCommandFactory.LINKED_FILES_DLG);
        miscActions.add(ELANCommandFactory.EDIT_CV_DLG);
        miscActions.add(ELANCommandFactory.HELP);
        miscActions.add(ELANCommandFactory.COPY_CURRENT_TIME);
        //actions with no default shortcut key        
        miscActions.add(ELANCommandFactory.REPLACE_MULTIPLE);        
        miscActions.add(ELANCommandFactory.EDIT_LANGUAGES_LIST);
        miscActions.add(ELANCommandFactory.EDIT_TIER_SET);
        miscActions.add(ELANCommandFactory.EDIT_LEX_SRVC_DLG);
        miscActions.add(ELANCommandFactory.EDIT_PREFS);
        miscActions.add(ELANCommandFactory.EDIT_SHORTCUTS);
        miscActions.add(ELANCommandFactory.SET_AUTHOR);
        miscActions.add(ELANCommandFactory.FONT_BROWSER);
        miscActions.add(ELANCommandFactory.SHORTCUTS);
        miscActions.add(ELANCommandFactory.SPREADSHEET);
        miscActions.add(ELANCommandFactory.STATISTICS);        
        miscActions.add(ELANCommandFactory.ABOUT);      
        miscActions.add(ELANCommandFactory.SHOW_INTERLINEAR);        
        miscActions.add(ELANCommandFactory.SHOW_TIMELINE);        
        miscActions.add(ELANCommandFactory.TYPECRAFT_DLG);        
        miscActions.add(ELANCommandFactory.WEBLICHT_DLG);        
        //miscActions.add(ELANCommandFactory.KIOSK_MODE); // doesn't work properly        
        // category to be checked
        miscActions.add(ELANCommandFactory.ANNOTATION_MODE);
        miscActions.add(ELANCommandFactory.SYNC_MODE);
        miscActions.add(ELANCommandFactory.TRANSCRIPTION_MODE);
        miscActions.add(ELANCommandFactory.SEGMENTATION_MODE);
        miscActions.add(ELANCommandFactory.INTERLINEARIZATION_MODE);        
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    	
    }
    
    private Map<String, KeyStroke> getCommonDefaultShortcutsMap(){ 
    	Map<String, KeyStroke> shortcutKeyStrokes  = new LinkedHashMap<String, KeyStroke>(80);
    	
    	final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		shortcutKeyStrokes.put(ELANCommandFactory.ADD_TIER,
                KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_T,
             	menuShortcutKeyMask +
            	ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.ADD_TYPE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_T,
            	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_DOC,
        	KeyStroke.getKeyStroke(KeyEvent.VK_N,
            	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.OPEN_DOC,
        	KeyStroke.getKeyStroke(KeyEvent.VK_O,
            	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE_AS,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK));
           
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE_AS_TEMPLATE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.PRINT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
            	menuShortcutKeyMask));
            
    	shortcutKeyStrokes.put(ELANCommandFactory.PREVIEW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
             	menuShortcutKeyMask +
            	ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.PAGESETUP,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
        		menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.NEXT_WINDOW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.SHIFT_MASK));
           
    	shortcutKeyStrokes.put(ELANCommandFactory.PREV_WINDOW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.CLOSE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_W,
        		menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.EXIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.UNDO,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.REDO,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Y,
             	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SEARCH_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
            	menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SEARCH_MULTIPLE_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
             	menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
        		menuShortcutKeyMask +
                ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));

        
    	shortcutKeyStrokes.put(ELANCommandFactory.LINKED_FILES_DLG,
          	KeyStroke.getKeyStroke(KeyEvent.VK_L,
             	menuShortcutKeyMask +
               	ActionEvent.ALT_MASK));        
        
    	shortcutKeyStrokes.put(ELANCommandFactory.EDIT_CV_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_C,
            	menuShortcutKeyMask +
            	ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.HELP,
            KeyStroke.getKeyStroke(KeyEvent.VK_H,
                menuShortcutKeyMask));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_CURRENT_TIME,
        	KeyStroke.getKeyStroke(KeyEvent.VK_G, 
        		menuShortcutKeyMask +
                ActionEvent.ALT_MASK));
    	
    	return shortcutKeyStrokes;
    }
    
    /**
     * adds all actions that are shortcuttable but are missing from shortcutKeyStrokes to it
     */
    private void addActionsWithoutShortcut() {
    	for (Entry<String, Map<String, List<String>>> mode : shortcuttableActionsMap.entrySet()) {
    		String modeName = mode.getKey();
    		Map<String, KeyStroke> shortcutKeyStrokes = shortcutKeyStrokesMap.get(modeName);

    		if (shortcutKeyStrokes == null) {
    			// A new mode name doesn't usually happen, but it did during development...
    			shortcutKeyStrokes = new HashMap<String, KeyStroke>();
    			shortcutKeyStrokesMap.put(modeName, shortcutKeyStrokes);
    		}
    		
    		for (Entry<String, List<String>> kvpair : mode.getValue().entrySet()) {
        		List<String> actionList = kvpair.getValue();

        		for (String action : actionList) {
        			if (!(shortcutKeyStrokes.containsKey(action))) {
        				shortcutKeyStrokes.put(action, null);
        			}
        		}
    		}
    	}
    }

	/**
     * Returns the KeyStroke for the specified action identifier.
     *
     * @param actionID the identifier, one of the constants in {@link
     *        ELANCommandFactory}.
     *
     * @return a KeyStroke or null
     */
    public KeyStroke getKeyStrokeForAction(String actionID, String modeName) {
    	KeyStroke ks = null;
        if (actionID != null) { 
        	if(modeName == null){
        		if(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).get(actionID);
        		}
        	}else{
        		if(shortcutKeyStrokesMap.get(modeName).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(modeName).get(actionID);
        		} else if(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).get(actionID);
        		}
        			
        	}
        }
        return ks;
    }

    /**
     * Returns the current id- keystroke mappings.
     *
     * @return a map containing id to keystroke mappings
     */
    public Map<String, KeyStroke> getCurrentShortcuts(String modeName) {
        Map<String, KeyStroke> currentShortcutKeyStrokes = new HashMap<String, KeyStroke> ();
        currentShortcutKeyStrokes.putAll(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS));
        if(modeName != null){
        	currentShortcutKeyStrokes.putAll(shortcutKeyStrokesMap.get(modeName));
        }
        
		return currentShortcutKeyStrokes ;
    }

    /**
     * Returns a map of all action that can have a shortcut. The keys are
     * category names, the values are lists of action identifiers.
     *
     * @return a mapping of all actions, grouped per category
     */
    public Map<String, List<String>> getShortcuttableActions(String modeName) {
    	if(modeName == null){
    		return shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	}
        return shortcuttableActionsMap.get(modeName);
    }    
  
    /**
     * Returns all the shortcuts used in the give mode
     * 
     * @param modeName, name of the mode
     * @return Map with all the shortcuts associated with this mode
     */
    public Map<String, KeyStroke> getShortcutKeysOnlyIn(String modeName) {
    	if(modeName == null){
    		return shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	}
        return shortcutKeyStrokesMap.get(modeName);
    }

    /**
     * Returns a (friendly) description of the action (by default the tooltip
     * description). This is independent of any transcription or instantiated
     * actions.
     * <p>
     * Note that for the AnnotationsFromOverlapsClasDlgCA (ANN_FROM_OVERLAP_CLAS)
     * this doesn't work: it needs instance.getValue(Action.SHORT_DESCRIPTION).
     *
     * @param actionID the id
     *
     * @return a description or the empty string
     */
    public String getDescriptionForAction(String actionID) {
        if (actionID == null) {
            return "";
        }

        String desc = ElanLocale.getString(actionID + "ToolTip");

        if (desc == null || desc.isEmpty()) {
            desc = ElanLocale.getString(actionID);
        }

        return desc;
    }
    
    
    /**
     * Returns the category this action belongs to
     *
     * @param actionID the id
     *
     * @return a category name or the empty string
     */
    public String getCategoryForAction(String modeName, String actionID)
    {
        if (actionID == null) {
            return "";
        }
        
        Iterator<Entry<String, List<String>>> it;
        
        if(modeName == null){
        	it = shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS).entrySet().iterator(); 
    	} else{
    		it = shortcuttableActionsMap.get(modeName).entrySet().iterator();
    	}
        
        while (it.hasNext())
        {
        		Entry<String, List<String>> pairs = it.next();
        		String cat = pairs.getKey();
        		List<String> actionList = pairs.getValue();
        		if (actionList.contains(actionID))
        		{
        			return cat;
        		}
        }
        return "";
    }
    
    /**
     * Returns a user readable, platform specific, description of the key and
     * the modifiers.
     *
     * @param ks the keystroke
     *
     * @return the description
     */
    public String getDescriptionForKeyStroke(KeyStroke ks) {
        if (ks == null) {
            return "";
        }

        String nwAcc = "";

        if (SystemReporting.isMacOS()) {
            int modifier = ks.getModifiers();

            if ((modifier & InputEvent.CTRL_MASK) != 0) {
                nwAcc += "\u2303";
            }
            
            if ((modifier & InputEvent.SHIFT_MASK) != 0) {
                nwAcc += "\u21E7";
            }

            if ((modifier & InputEvent.ALT_MASK) != 0) {
                nwAcc += "\u2325";
            }
            
            if ((modifier & InputEvent.META_MASK) != 0) {
                nwAcc += "\u2318";
            }

            if (ks.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
            	if(ks.getKeyCode() == KeyEvent.VK_DELETE){
            		nwAcc += "Delete";
            	} else if( ks.getKeyCode() == KeyEvent.VK_PAGE_UP){
            		nwAcc += "PageUp";
            	} else if( ks.getKeyCode() == KeyEvent.VK_PAGE_DOWN){
            		nwAcc += "PageDown";
            	} else if( ks.getKeyCode() == KeyEvent.VK_SPACE){
            		nwAcc += "Space";
//            	} else if( ks.getKeyCode() == KeyEvent.VK_ENTER){
//            		nwAcc += "Return";
            	} else {
            		nwAcc += KeyEvent.getKeyText(ks.getKeyCode());
            	}
            } else {
                nwAcc += String.valueOf(ks.getKeyChar());
            }
        } else {
            int modifier = ks.getModifiers();

            if ((modifier & InputEvent.CTRL_MASK) != 0) {
                nwAcc += "Ctrl+";
            }

            if ((modifier & InputEvent.ALT_MASK) != 0) {
                nwAcc += "Alt+";
            }

            if ((modifier & InputEvent.SHIFT_MASK) != 0) {
                nwAcc += "Shift+";
            }

            if (ks.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {            	
                nwAcc += KeyEvent.getKeyText(ks.getKeyCode());
            } else {
                nwAcc += String.valueOf(ks.getKeyChar());
            }
        }

        return nwAcc;
    }

    /**
     * Restores the default keyboard shortcuts.
     */   
    public void restoreDefaultShortcutsForthisMode(String modeName){
    	if(modeName == null || modeName.equals(ELANCommandFactory.COMMON_SHORTCUTS)){
    		shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	} else 	if(modeName.equals(ELANCommandFactory.ANNOTATION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getAnnotationModeDefaultShortcutsMap());
    	} 
    	else if(modeName.equals(ELANCommandFactory.SYNC_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getSyncModeDefaultShortcutsMap());
    	}
    	else if(modeName.equals(ELANCommandFactory.TRANSCRIPTION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getTranscModeDefaultShortcutsMap());
    	}
    	else if(modeName.equals(ELANCommandFactory.SEGMENTATION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getSegmentModeDefaultShortcutsMap());
    	}   
    	else if(modeName.equals(ELANCommandFactory.TURNS_SCENE_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getTurnsAndSceneModeDefaultShortcutsMap());
    	}
    	else if(modeName.equals(ELANCommandFactory.INTERLINEARIZATION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getInterlinearModeDefaultShortcutsMap());
    	}
    	
    	JOptionPane.showMessageDialog( null,ElanLocale.getString("Shortcuts.Message.Restored") + " "+ ElanLocale.getString(modeName));
    }
    
    public void restoreAll(){
    	shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, getInterlinearModeDefaultShortcutsMap());
    	
    	JOptionPane.showMessageDialog( null,ElanLocale.getString("Shortcuts.Message.RestoredAll"));
    }
    
    /**
     * Reads stored shortcuts from file
     *
     * @return true if stored mappings have been loaded, false otherwise
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean readCurrentShortcuts() 
    {    	
    	PreferencesReader	xmlPrefsReader = new PreferencesReader();
    	Map<String,Map<String, KeyStroke>> shortcutKeyMap = new HashMap<String,Map<String, KeyStroke>>();     	    	
    	Map shortcutMapRaw = null;
    	
    	try{
    		File file = new File(NEW_PREF_FILEPATH);
    		if(file.exists()){
    			shortcutMapRaw = xmlPrefsReader.parse(NEW_PREF_FILEPATH);
    		} else {
    			shortcutMapRaw = xmlPrefsReader.parse(PREF_FILEPATH);
    		}
    		
    		// if parsing is not successful
    		if(!xmlPrefsReader.isParsingSuccessful()){
    			ClientLogger.LOG.info("Could not load the keyboard shortcut preferences file. The file does not exist or is not valid.");
    			return false;
    		}    		
    	}catch (Exception ex) {
            ClientLogger.LOG.warning("Could not load the keyboard shortcut preferences file.");
        }  
    	
    	
    	
    	if (shortcutMapRaw != null && (!shortcutMapRaw.isEmpty()))
  	 	{    		
    		if(shortcutMapRaw.values().iterator().hasNext()){    			
          		Object val = shortcutMapRaw.values().iterator().next();   
          		// if the preferences file is in the old format
				if (val instanceof ArrayList) 
          		{
					Iterator it = shortcutMapRaw.entrySet().iterator();
					HashMap<String, KeyStroke> shortcutMap = new HashMap<String, KeyStroke>();
	          	 	while (it.hasNext())
	          	 	{            	 		
	          	 		Map.Entry pair = (Map.Entry) it.next();
	          	 		String actionName = (String) pair.getKey();
	              		val = pair.getValue();            	 		
	    	
	              		if (val instanceof ArrayList) 
	              		{
	              			ArrayList<String> codes = (ArrayList<String>) val;
							if (codes.size() != 2)
	              			{
	              				shortcutMap.put(actionName, null);
	              			}
	              			else
	              			{
	              				try {
	              					int keycode = Integer.parseInt(codes.get(0));
		    	            		int modcode = Integer.parseInt(codes.get(1));
		    	            		KeyStroke aks = KeyStroke.getKeyStroke(keycode, modcode);
		    	            		shortcutMap.put(actionName, aks);
           						} catch(NumberFormatException e){
           							shortcutMap.put(actionName, null);
           						}
	              			}
	              		}
	          	 	}
	          	 	
	          	 	// convert the old preference format to the new format
	          	 	shortcutKeyMap.putAll(convertToNewShortCutMap(shortcutMap));	  
          		} 
				
				// if the preferences file is in the new format based on different modes
				else if(val instanceof Map) {  
          			Iterator it = shortcutMapRaw.entrySet().iterator();         	
        			while (it.hasNext())
                 	{            	 		
                 		Map.Entry pair = (Map.Entry) it.next();
                 		String modeName = (String) pair.getKey();
                 		//Map<String, List<String>> actions = shortcuttableActionsMap.get(modeName);
                   		val = pair.getValue();   
                   		Map<String, KeyStroke> shortcutMap = null;
                   		if(val instanceof Map){
                   			shortcutMap = new HashMap<String, KeyStroke>();
                   			Iterator it1 = ((Map<String, List<String>>)val).entrySet().iterator();
                    		while (it1.hasNext())
                         	{            	
                   				pair = (Map.Entry) it1.next();
                   				String actionName = (String) pair.getKey();
                   				val = pair.getValue();     
                   				if (val instanceof ArrayList) 
                   				{
                   					ArrayList<String> codes = (ArrayList<String>) val;
                   					if (codes.size() != 2)
                   					{
                   						shortcutMap.put(actionName, null);
                   					}
                   					else
                   					{
                   						try {
                   							int keycode = Integer.parseInt(codes.get(0));
                       						int modcode = Integer.parseInt(codes.get(1));
                       						KeyStroke aks = KeyStroke.getKeyStroke(keycode,modcode);
                       						shortcutMap.put(actionName, aks);
                   						} catch(NumberFormatException e){
                   							shortcutMap.put(actionName, null);
                   						}
                   					}
                   				}
                         	}
                   		}           		
                   		shortcutKeyMap.put(modeName, shortcutMap); 
                 	}
          		}
				
				shortcutKeyStrokesMap.clear();
        		shortcutKeyStrokesMap.putAll(shortcutKeyMap);
        		// ensure all modes are (still) there in the shortcutKeyStrokesMap
        		ensureAllModesInKeyStrokesMap();
        		
        		if(checkForNewShortcuts()){
        			PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    		
        			try 
        			{            		
        				xmlPrefsWriter.encodeAndSave(getStorableShortcutMap(shortcutKeyStrokesMap), NEW_PREF_FILEPATH);    		
        			}  catch (Exception ex) {
        				ClientLogger.LOG.warning("Error while updating the shortcuts file. File not created.");
        			} 
        		}
    		}
    		
    		// ensure that all shortcuttable Actions are in the hash
    		addActionsWithoutShortcut();
    		return true;
  	 	} else {
  	 		return false;    	 		
  	 	} 
    }
    
    public Map<String, Map<String, List<String>>> getStorableShortcutMap(Map<String,Map<String, KeyStroke>> shortcutKeyStrokesMap){
    	//overwrite the shortcut preferences file in new format
    	Map<String ,Map<String, List<String>>> shortcutModeMap = new HashMap<String, Map<String, List<String>>>(); 
    	Iterator<Entry<String, Map<String, KeyStroke>>> it = shortcutKeyStrokesMap.entrySet().iterator();
    	while(it.hasNext()){
    		Entry<String, Map<String, KeyStroke>> pair = it.next();
     		String modeName = pair.getKey();
     		Map<String, KeyStroke> valMap = pair.getValue();   
    		Iterator<Entry<String, KeyStroke>> it1 = valMap.entrySet().iterator();
    		Map<String, List<String>> map = new HashMap<String, List<String>>();  
    		while(it1.hasNext()){
    			Entry<String, KeyStroke> pair1 = it1.next();
         		String actionName = pair1.getKey();
         		KeyStroke ks = pair1.getValue(); 
         		ArrayList<String> codes = new ArrayList<String>(2);
         		if(ks != null){
         			codes.add(String.valueOf(ks.getKeyCode()));
         			codes.add(String.valueOf(ks.getModifiers()));
         		}
      			map.put(actionName,codes);
        		
    		}
    		shortcutModeMap.put(modeName, map);
    	}
    	return shortcutModeMap;
    }
    	
    private HashMap<String,Map<String, KeyStroke>> convertToNewShortCutMap(HashMap<String, KeyStroke> shortcutMap) {
    	final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    	List<String> generalActions = new ArrayList<String>();
    	List<String> annModeActions = new ArrayList<String>();
    	List<String> transModeActions = new ArrayList<String>();
    	List<String> syncModeActions = new ArrayList<String>();
    	List<String> segMentActions = new ArrayList<String>();
    	
    	Map<String, KeyStroke> annModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> transModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> syncModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> segmentModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> commonModeMap = new HashMap<String, KeyStroke>();
    	
    	Iterator<List<String>> it = getCommonShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		generalActions.addAll(it.next());
    	}
    	
    	it = getAnnotationModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		annModeActions.addAll(it.next());
    	}
    	
    	it = getTranscModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		transModeActions.addAll(it.next());
    	}
    	
    	it = getSyncModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		syncModeActions.addAll(it.next());
    	}
    	
    	it = getSegmentModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		segMentActions.addAll(it.next());
    	}   
    	
    	// assign the old shortcuts to its relevant mode
    	Iterator<Entry<String, KeyStroke>> it2 = shortcutMap.entrySet().iterator();
    	while(it2.hasNext()){
    		Entry<String, KeyStroke> pair = it2.next();
     		String actionName = pair.getKey();
     		KeyStroke val = pair.getValue();   
     		if(generalActions.contains(actionName) && val != null){
     			commonModeMap.put(actionName, val);
     		} else {
     			if(annModeActions.contains(actionName) && val != null){
     				annModeMap.put(actionName, val);
         		}
     			
     			if(transModeActions.contains(actionName) && val != null){
     				transModeMap.put(actionName, val);
     			}
     			
     			if(syncModeActions.contains(actionName) && val != null){
     				syncModeMap.put(actionName, val);
         		}
     			
     			if(segMentActions.contains(actionName) && val != null){
     				segmentModeMap.put(actionName, val);
         		}
     		}
    	}   
    	
    	
    	// New Modes have different shortcut keys for the actions in old shortcut file
    	// update all new shortcut keys(default) for the new modes
    	// only when shortcuts.pfsx file is found (versions till 4.1.2)
    	
    	/// Transcription mode special shortcuts
    	
    	KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.COMMIT_CHANGES,	ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.CANCEL_CHANGES, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.DELETE_ANNOTATION, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, 
    			menuShortcutKeyMask + ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, 
    			menuShortcutKeyMask + ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_UP, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_DOWN, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_LEFT, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_RIGHT, ks);	
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.PLAY_PAUSE, ks);
        
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.PLAY_FROM_START, ks);

    	// SegementationMode special shortcuts
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.DELETE_ANNOTATION, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.SEGMENT, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.NEXT_ACTIVE_TIER, ks); 
    	
    	HashMap<String,Map<String, KeyStroke>> shortcutKeysMap = new HashMap<String,Map<String, KeyStroke>>();    	
    	shortcutKeysMap.put(ELANCommandFactory.COMMON_SHORTCUTS, commonModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.ANNOTATION_MODE,annModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.SYNC_MODE, syncModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, transModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.SEGMENTATION_MODE, segmentModeMap);
    	// add empty maps
    	shortcutKeysMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, new HashMap<String, KeyStroke>(0));
    	shortcutKeysMap.put(ELANCommandFactory.TURNS_SCENE_MODE, new HashMap<String, KeyStroke>(0));
    	
		return shortcutKeysMap;
	}
    
    /**
     * Check is made for each version for new shortcuts added
     * 
     * @return
     */
    private boolean checkForNewShortcuts(){
    	String appName = System.getProperty("ELANApplicationMain");
    	boolean shortcutsCkecked = false; 
    	// ELAN
    	if (appName == null || appName.equals(ELAN.class.getName())) {
	    	String val= Preferences.getString("ShortcutKeyUpdateVersion", null);
	    	String version = ELAN.getVersionString();
	    	
	    	if (val == null || !val.equals(version)) { 
	    		addNewShortcuts(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap()); 
	    		addNewShortcuts(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap()); 
	    		addNewShortcuts(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap()); 
	    		addNewShortcuts(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap()); 
	    		addNewShortcuts(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
	    		addNewShortcuts(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeDefaultShortcutsMap());
	    		addNewShortcuts(ELANCommandFactory.INTERLINEARIZATION_MODE, getInterlinearModeDefaultShortcutsMap());
	    		Preferences.set("ShortcutKeyUpdateVersion", version, null);
	    		
	    		if(shortcutClash){
	    			// display a message
	    			String message = ElanLocale.getString("Shortcuts.Warning.Clashes") + System.getProperty("line.separator") +
	    					ElanLocale.getString("Shortcuts.Warning.Edit");
	    	        	JOptionPane.showMessageDialog(null, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	    		}
	    		shortcutsCkecked = true;
	    	}
    	}
    	// Simple-ELAN
    	if (appName != null && appName.equals(SIMPLELAN.class.getName())) {
	    	String val= Preferences.getString("SimpleELAN.ShortcutKeyUpdateVersion", null);
	    	String version = SIMPLELAN.getVersionString();	    	
	    	 
	    	if (val == null || !val.equals(version)) {    		
	    		addNewShortcuts(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeDefaultShortcutsMap());
	    		Preferences.set("SimpleELAN.ShortcutKeyUpdateVersion", version, null);
	    		
	    		shortcutsCkecked = true;
	    	}
    	}
    	
    	return shortcutsCkecked;
    }
    	
    private void addNewShortcuts(String modeName, Map<String, KeyStroke> defaultShortcutMap){
    	Map<String, KeyStroke> currentShortcutMapForThisMode = shortcutKeyStrokesMap.get(modeName); 
    	
    	Map<String, KeyStroke> currentlyUsedShortcutMap = new HashMap<String, KeyStroke>();
    	if (currentShortcutMapForThisMode != null) {
    		currentlyUsedShortcutMap.putAll(currentShortcutMapForThisMode);   
    	}
    	if(!modeName.equals(ELANCommandFactory.COMMON_SHORTCUTS)){    	
    		currentlyUsedShortcutMap.putAll(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS));
    	}
    	
		Iterator shortcutIt = defaultShortcutMap.entrySet().iterator();
		Map.Entry pair;
		String actionName;
		KeyStroke ks;		
		
		while(shortcutIt.hasNext()){
    		pair = (Map.Entry) shortcutIt.next();
    		actionName = (String) pair.getKey();
    		if(!currentShortcutMapForThisMode.containsKey(actionName)){
    			ks = (KeyStroke)pair.getValue();       			
    			// shortcuts clashes
    			currentShortcutMapForThisMode.put(actionName, ks);
    			
    			if(currentlyUsedShortcutMap.containsValue(ks)){
    				shortcutClash = true;
    			}    		
    			currentlyUsedShortcutMap.put(actionName, ks);
    			
    		}
		}
		shortcutKeyStrokesMap.put(modeName, currentShortcutMapForThisMode);  		
    }
	
    /**
     * After reading a stored shortcut preference file, make sure that a map for 
     * each mode is in the  shortcutKeyStrokesMap
     */
    private void ensureAllModesInKeyStrokesMap() {
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	} 
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.ANNOTATION_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap());
    	} 
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.SYNC_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap());
    	}
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.TRANSCRIPTION_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap());
    	}
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.SEGMENTATION_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
    	}
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.TURNS_SCENE_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.TURNS_SCENE_MODE, getTurnsAndSceneModeDefaultShortcutsMap());
    	}
    	if (shortcutKeyStrokesMap.get(ELANCommandFactory.INTERLINEARIZATION_MODE) == null) {
    		shortcutKeyStrokesMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, getInterlinearModeDefaultShortcutsMap());
    	}
    }
    
    /**
     * Saves the user defined keyboard shortcut to action mappings.
     */
    public void saveCurrentShortcuts(Map<String, Map<String, List<String>>> shortcutMap) 
    {
    	PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    		
    	try 
    	{
    		xmlPrefsWriter.encodeAndSave(shortcutMap, NEW_PREF_FILEPATH);    		
    		JOptionPane.showMessageDialog( null, ElanLocale.getString("Shortcuts.Message.Saved"));
        }  catch (Exception ex) {
           ClientLogger.LOG.warning("Could not save the keyboard shortcut preferences file");
       	  	JOptionPane.showMessageDialog( null, ElanLocale.getString("Shortcuts.Message.NotSaved"));
        } 
    } 
}
