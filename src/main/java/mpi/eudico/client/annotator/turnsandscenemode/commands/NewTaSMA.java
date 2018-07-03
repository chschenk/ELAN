package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;
import java.util.List;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.commands.global.NewMA;
import mpi.eudico.client.annotator.turnsandscenemode.TaSFrame;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

@SuppressWarnings("serial")
public class NewTaSMA extends NewMA {

	public NewTaSMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Since the Simplified ELAN has only one frame open at a time,
	 * first check and close an already open file.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (frame.getViewerManager() != null && frame.getViewerManager().getTranscription() != null) {
			frame.checkSaveAndClose();
		}
		
		super.actionPerformed(e);
	}

	@Override
	protected void createFrame(Transcription nextTranscription) {
		if (frame instanceof TaSFrame) {
			((TaSFrame)frame).setTranscription(nextTranscription);
		} else {
			super.createFrame(nextTranscription);
		}
	}

	/**
	 * In the situation where a new transcription is started via File->New the check on an already
	 * open transcription is performed in the {@link #actionPerformed(ActionEvent)} method. 
	 * To enable the creation of a new transcription via drag and drop of files, the check on
	 * the viewer manager of the current frame is repeated here. 
	 */
	@Override
	public void createNewFile(List<String> fileNames) {
		if (frame.getViewerManager() != null && frame.getViewerManager().getTranscription() != null) {
			frame.checkSaveAndClose();
		}
		
		super.createNewFile(fileNames);
	}

	@Override
	protected void addInitialTierAndType(TranscriptionImpl transcription) {
		 String transTierId = "transcript";
         LinguisticType type = new LinguisticType(transTierId);                     
         transcription.addLinguisticType(type); 
         
         TierImpl tier = new TierImpl(transTierId, "", transcription, type);
         transcription.addTier(tier);
         tier.setDefaultLocale(null);
	}
	
	
}
