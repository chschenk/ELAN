package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class ExportCHATCA extends CommandAction {
    private TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public ExportCHATCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_CHAT);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_CHAT);
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
	            transcriptionStore, vm.getMultiTierControlPanel().getVisibleTiers()
	        };
    	} else {
	        return new Object[] {
		            transcriptionStore, new ArrayList(0)
		        };
    	}
    }
}
