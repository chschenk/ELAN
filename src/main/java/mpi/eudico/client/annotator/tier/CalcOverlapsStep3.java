package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.AnnotationsFromOverlapsClasCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clom.Transcription;


/**
 * The final step, the actual calculation.  A command is created and this pane
 * is connected as progress listener. The ui is a progress monitor.
 *
 * @author Han Sloetjes
 * @version 1.0 Jan 2007
 */
public class CalcOverlapsStep3 extends ProgressStepPane {
    Transcription transcription;
    private AnnotationsFromOverlapsClasCommand com;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public CalcOverlapsStep3(MultiStepPane multiPane,
        Transcription transcription) {
        super(multiPane);
        this.transcription = transcription;
        initComponents();
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.Calculating");
    }

    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        doFinish();
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
    	completed = false;
        // disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);

        String tierName1 = (String) multiPane.getStepProperty("Source-1");
        String tierName2 = (String) multiPane.getStepProperty("Source-2");
        String destTier = (String) multiPane.getStepProperty("DestTier");
        String typeName = (String) multiPane.getStepProperty("Type");
        Boolean content = (Boolean) multiPane.getStepProperty("Content");
        Integer format = (Integer) multiPane.getStepProperty("Format");
        Boolean onlyWhenValuesMatch = (Boolean) multiPane.getStepProperty("MatchingValuesOnly");
        Boolean specValuesOnly = (Boolean) multiPane.getStepProperty("SpecificValueOnly");
        String specValue = (String) multiPane.getStepProperty("SpecificValue");

        if ((tierName1 == null) || (tierName2 == null) || (destTier == null) ||
                (typeName == null)) {
            progressInterrupted(null,
                "Illegal argument: a tier or type could not be found");
        }

        // create a command and connect as listener
        com = (AnnotationsFromOverlapsClasCommand) ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ANN_FROM_OVERLAP_CLAS);
        com.addProgressListener(this);
        com.execute(transcription,
            new Object[] {
                tierName1, tierName2, destTier, typeName, content, format, onlyWhenValuesMatch, 
                specValuesOnly, specValue
            });

        return false;
    }

}
