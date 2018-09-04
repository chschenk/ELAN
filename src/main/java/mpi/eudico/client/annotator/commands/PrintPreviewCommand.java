package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.InterlinearPreviewDlg;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JFrame;


/**
 * Shows a dialog with a print preview and ui elements to change parameters.
 *
 * @author Hennie Brugman
 */
public class PrintPreviewCommand implements Command {
    private String commandName;

    /**
     * Creates a new PrintCommand instance
     *
     * @param name the name of the Command
     */
    public PrintPreviewCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the preview dialog.
     *
     * @param receiver a Transcription
     * @param arguments a Selection object (for printing the selection only
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;
        Selection sel = (Selection) arguments[0];

        JFrame fr = ELANCommandFactory.getRootFrame(tr);
        Interlinear interlinear = new Interlinear((TranscriptionImpl) tr);

        if ((sel != null) && (sel.getBeginTime() != sel.getEndTime())) {
            interlinear.setSelection(new long[] {
                    sel.getBeginTime(), sel.getEndTime()
                });
        }

        new InterlinearPreviewDlg(fr, true, interlinear).setVisible(true);
    }

    /**
     * Returns the name of the Command
     *
     * @return the name of the Command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
