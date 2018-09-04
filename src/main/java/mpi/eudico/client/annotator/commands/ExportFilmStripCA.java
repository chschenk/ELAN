package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to create a dialog for configuration of filmstrip + waveform
 * output.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportFilmStripCA extends CommandAction {
    /**
     * Creates a new ExportFilmStripCA instance
     *
     * @param theVM the viewer manager
     */
    public ExportFilmStripCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_FILMSTRIP);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_FILMSTRIP);
    }

    /**
     * Returns the viewer manager.
     *
     * @return the viewer manager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }
}
