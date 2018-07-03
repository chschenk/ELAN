package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.util.SystemReporting;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * The base class for action objects that are not connected to a particular
 * document object (Transcription). Used for JMenu's, that have an empty
 * actionPerformed method, and a few JMenuItems that are present in e.g.  an
 * empty Elan frame. Useful for handling updateLocale and setting the
 * mnemonic.
 *
 * @author Han Sloetjes, MPI
 * @version April 2009: keyboard shortcuts are now obtained via the ShortcutUtil 
 * class. The accelerator keys are now user definable.
 */
public class MenuAction extends AbstractAction implements ElanLocaleListener {
    /** a prefix for mnemonic keys */
    public static final String MNEMONIC = "MNEMONIC.";

    /** the command name */
    protected String commandId;
    private static boolean useMnemonics = true;
    
    static {	
    	if (SystemReporting.isMacOS()) {
    		useMnemonics = false;
    	}
    }

    /**6
     * Creates a new MenuAction.
     *
     * @param name the name of the command
     */
    public MenuAction(String name) {
        super(name);

        commandId = name;
        putValue(Action.ACCELERATOR_KEY, 
        		ShortcutsUtil.getInstance().getKeyStrokeForAction(commandId, null));
        updateLocale();
    }

    /**
     * Updates the NAME (what is shown in menu's and on buttons), tooltip  and
     * eventually mnemonic.
     */
    @Override
	public void updateLocale() {
        if (commandId != null) {
            putValue(Action.NAME, ElanLocale.getString(commandId));

            // don't bother about an icon...
            Object desc = getValue(Action.SHORT_DESCRIPTION);

            if (desc instanceof String && (((String) desc).length() > 0)) {
                putValue(Action.SHORT_DESCRIPTION,
                    ElanLocale.getString(commandId + "ToolTip"));
            }

            if (useMnemonics) {
	            // mnemonic
	            String mnemonic = ElanLocale.getString(MNEMONIC + commandId);
	
	            if (mnemonic.length() > 0) {
	                try {
	                    putValue(Action.MNEMONIC_KEY,
	                    	Integer.valueOf(mnemonic.charAt(0)));
	                } catch (NumberFormatException nfe) {
	                    try {
	                        putValue(Action.MNEMONIC_KEY, Integer.valueOf(mnemonic));
	                    } catch (NumberFormatException nfe2) {
	                        putValue(Action.MNEMONIC_KEY, null);
	                    }
	                }
	            }
            }
        }
    }

    /**
     * The action; empty by default.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    }
}
