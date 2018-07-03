package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class SaveCA extends CommandAction {
    private TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SaveCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE);

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
        boolean saveNewCopy = false;
        String fileName = vm.getTranscription().getName();      

        if (fileName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME) || !fileName.toLowerCase().endsWith(".eaf")) {
            saveNewCopy = true;
        }
        
        if (vm.getMultiTierControlPanel() != null) {
	        return new Object[] {
	            transcriptionStore, Boolean.FALSE, Boolean.valueOf(saveNewCopy),
	            vm.getMultiTierControlPanel().getVisibleTiers(),
	            Integer.valueOf(TranscriptionStore.EAF)
	        };
        } else {
	        return new Object[] {
		            transcriptionStore, Boolean.FALSE, Boolean.valueOf(saveNewCopy),
		            new ArrayList(0), Integer.valueOf(TranscriptionStore.EAF)
		        };
        }
    }
}
