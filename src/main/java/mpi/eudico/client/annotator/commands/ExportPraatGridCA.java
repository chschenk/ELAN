package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Export to Praat TextGrid CA.
 * 
 * @author Han Sloetjes
 */
public class ExportPraatGridCA extends CommandAction {

    /**
     * Creates a new ExportPraatGridCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportPraatGridCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_PRAAT_GRID);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_PRAAT_GRID);
    }
    
    /**
     * Returns the arguments
     *
     * @return the arguments
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription(), vm.getSelection()};
    }

}
