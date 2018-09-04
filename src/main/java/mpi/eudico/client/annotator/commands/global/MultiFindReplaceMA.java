package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFindReplaceDialog;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * Creates a "find-and-replace in multiple files" dialog.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MultiFindReplaceMA extends AbstractProcessMultiMA {
    /**
     * Creates a new MultiFindReplaceMA instance
     *
     * @param name the name of the command
     * @param frame the parent frame
     */
    public MultiFindReplaceMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates the dialog
     *
     * @param e the event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int option = JOptionPane.showConfirmDialog(frame,
                ElanLocale.getString("MultipleFileSearch.FindReplace.WarnInit"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            // always modal??
            new EAFMultipleFindReplaceDialog(frame).setVisible(true);
        }
    }
}
