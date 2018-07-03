package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Icon;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import nl.mpi.lexan.analyzers.helpers.Position;

/**
 * Class for deleting an annotation. 
 * Is undoable because it uses an UndoableCommand to perform the work.
 * 
 * @author Olaf Seibert
 *
 */
@SuppressWarnings("serial")
public class IGTSplitAnnotationAction extends IGTEditAction {
	public static final int SPLIT = 0;
	public static final int NEW_BEFORE = 1;
	public static final int NEW_AFTER = 2;

	/**
	 * The TextAnalyzerHostContext to use for starting the interlinearization action 
	 * In case of deletion the connected analyzer might need to know of the removal?
	 */
	protected TextAnalyzerHostContext hostContext;
	/**
	 * One of SPLIT, NEW_BEFORE or NEW_AFTER.
	 */
	protected int variant;
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 */
	public IGTSplitAnnotationAction(IGTAnnotation igtAnnotation,
			TextAnalyzerHostContext hostContext, int variant) {
		super(igtAnnotation);
		this.hostContext = hostContext;
		this.variant = variant;
	}

	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action 
	 */	
	public IGTSplitAnnotationAction(IGTAnnotation igtAnnotation,
			TextAnalyzerHostContext hostContext, int variant, String label) {
		super(igtAnnotation, label);
		this.hostContext = hostContext;
		this.variant = variant;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGT annotation
	 * @param hostContext the analyzer context via which the connected analyzer can be informed of deletion
	 * @param label the text for the action
	 * @param icon an icon for the action 
	 */
	public IGTSplitAnnotationAction(IGTAnnotation igtAnnotation,
			TextAnalyzerHostContext hostContext, int variant, String label, 
			Icon icon) {
		super(igtAnnotation, label, icon);
		this.hostContext = hostContext;
		this.variant = variant;
	}

	/**
	 * Do it.
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
				
				// the following code double checks if the selected annotation supports the requested action
				if (variant == SPLIT) {
					if (canSplit(aa)) {					
						Command command = ELANCommandFactory.createCommand(tr,
								ELANCommandFactory.SPLIT_ANNOTATION);
						
						Object[] args = new Object[] { aa };
						command.execute(tr, args);
						hostContext.annotationsAdded(pos);
					}
				} else {
					TierImpl t = (TierImpl) aa.getTier();
					
			        Constraint c = t.getLinguisticType().getConstraints();

			        if (c != null &&  c.supportsInsertion()) {
						String cmd = variant == NEW_AFTER 
								? ELANCommandFactory.NEW_ANNOTATION_AFTER
								: ELANCommandFactory.NEW_ANNOTATION_BEFORE;
						Command command = ELANCommandFactory.createCommand(tr, cmd);
						Object[] args = new Object[] { aa };
						command.execute(t, args);
						hostContext.annotationsAdded(pos);
			        }
				}
			}
		}

	}
	
	/**
	 * Performs a similar test to the one performed in SplitAnnotationCA.
	 * Currently only top level annotations with no or only 1-to-1 associated 
	 * child annotations are supported
	 * 
	 * @param aa the annotation, not null
	 * @return true if splitting is supported, false otherwise
	 */
	private boolean canSplit(AbstractAnnotation aa) {
		TierImpl t = (TierImpl) aa.getTier();
		if (t.getParentTier() == null) {
			boolean onlySymAssChildren = true;
			
			List<TierImpl> childTiers = t.getChildTiers();
			if (childTiers.isEmpty()) {
				return true;
			}
			for (TierImpl ti : childTiers) {
				if (ti.getLinguisticType().getConstraints().getStereoType() == 
						Constraint.SYMBOLIC_ASSOCIATION) {
					continue;
				}
				if (!aa.getChildrenOnTier(ti).isEmpty()) {
					onlySymAssChildren = false;
					break;
				}
			}
			return onlySymAssChildren;
		}
		
		return false;
	}
}
