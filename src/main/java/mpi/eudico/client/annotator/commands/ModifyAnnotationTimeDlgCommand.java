package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.TimeIntervalEditDialog;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;

/**
 * A command that creates a dialog to type/modify start and end time of an annotation
 * and that creates a {@link ModifyAnnotationTimeCommand} when the changes in the dialog
 * are applied.
 * 
 * @author Han Sloetjes
 */
public class ModifyAnnotationTimeDlgCommand implements Command {
	private String commandName;
	
	/**
	 * Constructor.
	 * @param name name of the command
	 */
	public ModifyAnnotationTimeDlgCommand(String name) {
		commandName = name;
	}

	/**
	 * @param receiver the ViewerManager, gives access to the transcription, the frame
	 * it is in and to the master media player (for the duration)
	 * @param arguments arguments[0] = the annotation to modify (AlignableAnnotation)
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ViewerManager2 vm = (ViewerManager2) receiver;
		Annotation ann = (Annotation) arguments[0];
		
		if (ann instanceof AlignableAnnotation) {
			TimeIntervalEditDialog dialog = 
					new TimeIntervalEditDialog(ELANCommandFactory.getRootFrame(
							vm.getTranscription()), true);
			dialog.setInterval(ann.getBeginTimeBoundary(), ann.getEndTimeBoundary());
			if (ann.hasParentAnnotation()) {
				Annotation parAnn = ann.getParentAnnotation();
				// should check the aligned status
				dialog.setLimits(parAnn.getBeginTimeBoundary(), parAnn.getEndTimeBoundary());
			} else {
				dialog.setLimits(0, vm.getMasterMediaPlayer().getMediaDuration());
			}
			
			dialog.setVisible(true);
			
			long nextBT = dialog.getBeginTime();
			long nextET = dialog.getEndTime();
			if (nextBT > -1 && nextET > nextBT && 
					!(nextBT == ann.getBeginTimeBoundary() && nextET == ann.getEndTimeBoundary())) {
				// create an undoable command to change the time interval
				Command com = ELANCommandFactory.createCommand(
						ann.getTier().getTranscription(), ELANCommandFactory.MODIFY_ANNOTATION_TIME);
				com.execute(ann, new Object[]{nextBT, nextET});
			}
		}			
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return commandName;
	}

}
