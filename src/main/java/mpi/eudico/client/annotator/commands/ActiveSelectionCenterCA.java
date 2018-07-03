
package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * DOCUMENT ME! 
 *
 * A command action for moving the crosshair to the center of the selection.
 * 
 * @author $Aarthy Somasundaram$
 * @version $Dec 2010$
 */

public class ActiveSelectionCenterCA extends CommandAction   {
    
    /**
     * Creates a new ActiveSelectionCenterCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public ActiveSelectionCenterCA(ViewerManager2 theVM) {       
        super(theVM, ELANCommandFactory.SELECTION_CENTER);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SELECTION_CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getMediaPlayerController();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getMasterMediaPlayer();
        args[1] = vm.getSelection();
        
        return args;
    }    
}
