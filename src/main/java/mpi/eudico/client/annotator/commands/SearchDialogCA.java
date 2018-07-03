package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for creating a search dialog.
 *
 * @author Han Sloetjes
 */
public class SearchDialogCA extends CommandAction {
    /**
     * Creates a new SearchDialogCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SearchDialogCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SEARCH_DLG);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SEARCH_DLG);
    }

    /**
     * There is no clear receiver for a search action. All necessary objects
     * are passed as arguments to the Search Dialog.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm;

        return args;
    }

    /*
       protected Object[] getArguments() {
           Object[] args = new Object[6];
           args[0] = rootFrame;
           args[1] = vm.getTranscription();
           args[2] = vm.getSelection();
           args[3] = vm.getActiveAnnotation();
           args[4] = vm.getMasterMediaPlayer();
           args[5] = null; //identity
           return args;
       }
     */
}
