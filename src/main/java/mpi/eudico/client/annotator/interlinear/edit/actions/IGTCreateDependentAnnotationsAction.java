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
 * Temporary class for deleting an annotation. 
 * Should be undoable.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class IGTCreateDependentAnnotationsAction extends IGTEditAction {
	/** The analyzer context to use for starting the interlinearization action 
	 * In case of deletion the connected analyzer might need to know of the removal? */
	protected TextAnalyzerHostContext hostContext;
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 */
	public IGTCreateDependentAnnotationsAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext) {
		super(igtAnnotation);
		this.hostContext = hostContext;
	}

	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action 
	 */	
	public IGTCreateDependentAnnotationsAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext, String label) {
		super(igtAnnotation, label);
		this.hostContext = hostContext;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action
	 * @param icon an icon for the action 
	 */
	public IGTCreateDependentAnnotationsAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext hostContext, String label, 
			Icon icon) {
		super(igtAnnotation, label, icon);
		this.hostContext = hostContext;
	}

	/**
	 * Uses ELANCommandFactory.CREATE_DEPEND_ANN but NEW_ANNOTATION_REC seems very similar.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (hostContext != null) {
			AbstractAnnotation aa = null;
			Transcription tr = hostContext.getTranscription();
			
			if (igtAnnotation != null) {
				aa = igtAnnotation.getAnnotation();
			} else {
				ViewerManager2 vm = ELANCommandFactory.getViewerManager(tr);
				aa = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
			}
			
			if (aa != null) {				
				Position pos = new Position(aa.getTier().getName(),
											aa.getBeginTimeBoundary(), aa.getEndTimeBoundary());

				TierImpl tier = (TierImpl) aa.getTier();
				if (!tier.getChildTiers().isEmpty()) {
					Command command = ELANCommandFactory.createCommand(tr, ELANCommandFactory.CREATE_DEPEND_ANN);
					Object[] args = new Object[] { new Long(aa.getBeginTimeBoundary()),
							                       new Long(aa.getEndTimeBoundary()) };
					command.execute(tier, args);
	
					// insufficient/wrong tiers (but the call does nothing yet, fortunately)
					hostContext.annotationsAdded(pos);
				}
			}
		}

	}
}
