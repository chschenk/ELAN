package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.InterlinearPreviewDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates an export as HTML preview dialog.
 */
public class ExportHTMLDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExporHTMLDlgCommand instance
     *
     * @param theName the name of the command
     */
    public ExportHTMLDlgCommand(String theName) {
        commandName = theName;
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object(Transcription)</li> <li>arg[1] = the Selection object
     *        (Selection)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) arguments[0];
        Selection selection = (Selection) arguments[1];
        Interlinear inter = new Interlinear(transcription,
                Interlinear.HTML);

        if ((selection != null) &&
                (selection.getBeginTime() != selection.getEndTime())) {
            inter.setSelection(new long[] {
                    selection.getBeginTime(), selection.getEndTime()
                });
        }

        new InterlinearPreviewDlg(ELANCommandFactory.getRootFrame(transcription),
            true, inter).setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }

}
