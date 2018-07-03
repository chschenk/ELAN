package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class SaveAs2_7CA extends CommandAction {
    private TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SaveAs2_7CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_EAF_2_7);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.STORE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
    	if (vm.getMultiTierControlPanel() != null) {
	        return new Object[] {
	            transcriptionStore, Boolean.FALSE, Boolean.TRUE,
	            vm.getMultiTierControlPanel().getVisibleTiers(),
	            Integer.valueOf(TranscriptionStore.EAF_2_7)
	        };
    	} else {
	        return new Object[] {
		            transcriptionStore, Boolean.FALSE, Boolean.TRUE,
		            new ArrayList(0), Integer.valueOf(TranscriptionStore.EAF_2_7)
		        };
    	}
    }
}
