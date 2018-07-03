package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.EditTierDialog2;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * Creates a JDialog for defining, changing or deleting a Tier.
 *
 * @author Han Sloetjes
 */
public class EditTierDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new EditTierDlgCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public EditTierDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the edit mode, ADD,
     *        CHANGE or DELETE (Integer)</li> <li>arg[1] = the tier to
     *        initialise the dialog with (TierImpl)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) receiver;
        JFrame frame = ELANCommandFactory.getRootFrame(transcription);
        Integer mode = (Integer) arguments[0];
        TierImpl tier = null;

        if (arguments[1] instanceof TierImpl) {
            tier = (TierImpl) arguments[1];
        }

        if (mode.intValue() == EditTierDialog2.ADD) {
            // don't show the add tier dialog when no linguistic types have been defined
            if (transcription.getLinguisticTypes().size() == 0) {
                StringBuilder buf = new StringBuilder();
                buf.append(ElanLocale.getString(
                        "EditTierDialog.Message.NoTypes"));
                buf.append("\n");
                buf.append(ElanLocale.getString(
                        "EditTierDialog.Message.CreateType"));
                JOptionPane.showMessageDialog(frame, buf.toString(),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        new EditTierDialog2(frame, true, transcription, mode.intValue(),
            tier).setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
