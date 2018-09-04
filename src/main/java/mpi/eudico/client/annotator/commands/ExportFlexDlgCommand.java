package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportFlexStep1;
import mpi.eudico.client.annotator.export.ExportFlexStep2;
import mpi.eudico.client.annotator.export.ExportFlexStep3;
import mpi.eudico.client.annotator.export.ExportFlexStep4;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A command that creates an export flex dialog.
 *
 * @author Aarhty Somasundaram.
 */
public class ExportFlexDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExportFlexDlgCommand instance
     *
     * @param theName the name
     */
    public ExportFlexDlgCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object(TranscriptionImpl)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof TranscriptionImpl) {
        	
        	TranscriptionImpl transcription = (TranscriptionImpl) arguments[0];
//        	ExportFlexDialog dialog = new ExportFlexDialog(ELANCommandFactory.getRootFrame(transcription),
//        			 transcription);
//        	
//        	dialog.setVisible(true);       
//        	
//        	FlexEncoderInfo info = dialog.getEncoderInfo();
//        	if(info != null){
//        		FlexEncoder encoder = new FlexEncoder();
//            	encoder.setEncoderInfo(info);
//    			encoder.encode(transcription);		
//        	}
        	
        	
        	MultiStepPane multipane = new MultiStepPane();
        	multipane.addStep(new ExportFlexStep1(multipane, transcription));
        	multipane.addStep(new ExportFlexStep2(multipane, transcription));
        	multipane.addStep(new ExportFlexStep3(multipane, transcription));
        	multipane.addStep(new ExportFlexStep4(multipane, transcription));


        	JDialog dialog = multipane.createDialog(ELANCommandFactory.getRootFrame(transcription), ElanLocale.getString("ExportFlexDialog.Title"), true);
    	    dialog.setPreferredSize(new Dimension(600, 600));
    	    dialog.pack();
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