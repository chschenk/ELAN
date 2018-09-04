package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.interannotator.AnnotatorCompare;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates a dialog that allows to compare the segmentation and labeling of two annotators.
 * 
 * @version 2 Jan 2015: this command now creates the multiple file, multiple step version 
 * of the inter-annotator reliability calculation process. This new version supports multiple
 * algorithms for this calculation.
 */
public class CompareAnnotatorsDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public CompareAnnotatorsDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the dialog
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;

        new AnnotatorCompare(trans, ELANCommandFactory.getRootFrame(trans), false); 
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
