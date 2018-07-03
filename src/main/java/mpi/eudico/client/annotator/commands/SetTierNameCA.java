package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * DOCUMENT ME!
 * $Id: SetTierNameCA.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SetTierNameCA extends CommandAction {
    /**
     * Creates a new SetTierNameCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public SetTierNameCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SET_TIER_NAME);

        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_TIER_NAME);
    }
}
