package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.SystemReporting;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;


/**
 * DOCUMENT ME! $Id: CommandAction.java 44138 2015-07-30 21:28:45Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 */
public abstract class CommandAction extends AbstractAction
    implements ElanLocaleListener {
	/** a prefix for mnemonic keys */
	public static final String MNEMONIC = "MNEMONIC.";
    /** Holds value of property DOCUMENT ME! */
    protected Command command;
    private String commandId;

    /** Holds value of property DOCUMENT ME! */
    protected ViewerManager2 vm;
    private static boolean useMnemonics = true;
    
    static {	
    	if (SystemReporting.isMacOS()) {
    		useMnemonics = false;
    	}
    }
    
    public CommandAction(ViewerManager2 theVM, String name) {
        super(name);

        vm = theVM;
        commandId = name;

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
//        putValue(Action.ACCELERATOR_KEY, 
//        		ShortcutsUtil.getInstance().getKeyStrokeForAction(commandId, null));
        updateLocale();
    }

    /**
     * Creates a new CommandAction instance
     *
     * @param theVM DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param icon DOCUMENT ME!
     */
    public CommandAction(ViewerManager2 theVM, String name, Icon icon) {
        super(name, icon);

        vm = theVM;
        commandId = name;

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        updateLocale();
    }

    /**
     * DOCUMENT ME!
     */
    protected abstract void newCommand();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object getReceiver() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object[] getArguments() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        newCommand();

        if (command != null) {
            command.execute(getReceiver(), getArguments());
        }
    }
    
    public void setActionKeyStroke(KeyStroke ks){
    	 putValue(Action.ACCELERATOR_KEY, ks);    	
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale() {
        Object newString = null;

        if (commandId != null) {
            newString = "" + ElanLocale.getString(commandId);
        }

        //when there is an icon, set text to empty string (otherwise text appears on a button)
        //also handle the tooltip text
        Object[] obj = getKeys();

        for (int i = 0; i < obj.length; i++) {
            if (obj[i].equals("SmallIcon")) {
                newString = "";
            }
        }

        Object object = getValue(Action.SHORT_DESCRIPTION);

        if ((object == null) || (object.equals("") == false)) {
            putValue(Action.SHORT_DESCRIPTION,
                ElanLocale.getString(commandId + "ToolTip"));
        }

        putValue(Action.NAME, newString);

        if (useMnemonics) {
	        String mnemonic = ElanLocale.getString(MNEMONIC + commandId);
	        if (mnemonic.length() > 0) {
	        	try {
	        		putValue(Action.MNEMONIC_KEY, Integer.valueOf(mnemonic.charAt(0)));
	        	} catch (NumberFormatException nfe) {
	        		try {
	        			putValue(Action.MNEMONIC_KEY, Integer.valueOf(Integer.parseInt(mnemonic)));
	        		} catch (NumberFormatException nfe2){
	        			putValue(Action.MNEMONIC_KEY, null);
	        		}
	        	}
	        }
        }
    }
}
