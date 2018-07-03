package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * DOCUMENT ME! 
 *
 * A command action for copying the current time to the pasteboard.
 * 
 * @author $Aarthy Somasundaram$
 * @version $Dec 2010$
 */

public class CopyCurrentTimeToPasteBoardCA extends CommandAction   {
    
    /**
     * Creates a new CopyCurrentTimeToPasteBoardCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public CopyCurrentTimeToPasteBoardCA(ViewerManager2 theVM) {       
    	super(theVM, ELANCommandFactory.COPY_CURRENT_TIME);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.COPY_CURRENT_TIME);
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
        Object[] args = new Object[1];
        args[0] = vm.getMasterMediaPlayer();        
        
        return args;
    }    
}