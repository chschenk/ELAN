package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import nl.mpi.lexan.analyzers.helpers.Position;

/**
 * Wrapper class for deleting an annotation in this mode. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTDeleteAction extends IGTEditAction {
	/** The analyzer context to use for starting the interlinearization action 
	 * In case of deletion the connected analyzer might need to know of the removal? */
	protected TextAnalyzerHostContext hostContext;
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 */
	public IGTDeleteAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext) {
		this(igtAnnotation, hostContext, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action 
	 */	
	public IGTDeleteAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext,
			String label) {
		this(igtAnnotation, hostContext, label, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action
	 * @param icon an icon for the action 
	 */
	public IGTDeleteAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext, String label, 
			Icon icon) {
		super(igtAnnotation, label, icon);
		this.hostContext = hostContext;
	}

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (hostContext != null) {
			AbstractAnnotation aa = null;
			Transcription tr = hostContext.getTranscription();
			ViewerManager2 vm = ELANCommandFactory.getViewerManager(tr);
			
			if (igtAnnotation != null) {				
				aa = igtAnnotation.getAnnotation();
			} else {
				aa = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
			}
			
			if (aa != null) {
				Position pos = new Position(aa.getTier().getName(),
						aa.getBeginTimeBoundary(),
						aa.getEndTimeBoundary());
				TierImpl tier = (TierImpl) aa.getTier();
				
				Command command = ELANCommandFactory.createCommand(tr,
						ELANCommandFactory.DELETE_ANNOTATION);
				command.execute(tier, new Object[] { vm, aa });

				// this might need changing
				hostContext.annotationDeleted(pos);
			}
		}
		
	}
}
