package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * DOCUMENT ME! $Id: SaveAsTemplateCA.java,v 1.2 2004/07/01 12:13:58 hasloe Exp
 * $
 *
 * @author $Author$
 * @version $Revision$
 */
public class SaveAsTemplateCA extends CommandAction {
    private TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsTemplateCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SaveAsTemplateCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE_AS_TEMPLATE);

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
	            transcriptionStore, Boolean.TRUE, Boolean.TRUE,
	            vm.getMultiTierControlPanel().getVisibleTiers(),
	            Integer.valueOf(TranscriptionStore.EAF)
	        };
    	} else {
	        return new Object[] {
		            transcriptionStore, Boolean.TRUE, Boolean.TRUE,
		            new ArrayList(0), Integer.valueOf(TranscriptionStore.EAF)
		        };
    	}

    }
}
