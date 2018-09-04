package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.CreateAnnsOnDependentTiersStep1;
import mpi.eudico.client.annotator.tier.CreateAnnsOnDependentTiersStep2;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates the create empty annotations on dependent tiers dialog.
 */
public class CreateAnnsOnDependentTiersDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public CreateAnnsOnDependentTiersDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the create empty annotations on dependent tiers dialog.
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

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());

        StepPane step1 = new CreateAnnsOnDependentTiersStep1(pane, trans);
        StepPane step2 = new CreateAnnsOnDependentTiersStep2(pane,trans);
        pane.addStep(step1);
        pane.addStep(step2);

        pane.createDialog(ELANCommandFactory.getRootFrame(trans),  ElanLocale.getString("CreateAnnsOnDependentTiersDlg.Title"), true)
            .setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}

