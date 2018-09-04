package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.dcr.AnnotationDCRDialog;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;


/**
 * Creates a dialog to change the ISO Data Category reference of an annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationDatCatDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param commandName the name of the command
     */
    public ModifyAnnotationDatCatDlgCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the active Annotation
     * @param arguments the arguments: arg[0]: the transcription
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Annotation activeAnn = (Annotation) receiver;
        Transcription transcription = null;

        if (activeAnn instanceof AbstractAnnotation) {
            // create a dialog
            if ((arguments != null) && arguments[0] instanceof Transcription) {
                transcription = (Transcription) arguments[0];
            }

            AnnotationDCRDialog dialog = new AnnotationDCRDialog(ELANCommandFactory.getRootFrame(
                        transcription), true, transcription,
                    (AbstractAnnotation) activeAnn);
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getOwner());
            dialog.setVisible(true);
        }
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
