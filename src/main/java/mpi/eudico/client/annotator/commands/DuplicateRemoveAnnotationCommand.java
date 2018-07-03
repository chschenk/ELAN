package mpi.eudico.client.annotator.commands;

/**
 * A command to duplicate an annotation to the active tier and to 
 * then delete the original annotation.
 * 
 * @author Han Sloetjes
 *
 */
public class DuplicateRemoveAnnotationCommand extends
		DuplicateAnnotationCommand {
	
	/**
	 * @param name
	 */
	public DuplicateRemoveAnnotationCommand(String name) {
		super(name);
	}

	/**
	 * 
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		// TODO Auto-generated method stub
		super.execute(receiver, arguments);
	}

	/**
	 * 
	 */
	@Override
	public void redo() {
		// TODO Auto-generated method stub
		super.redo();
	}

	/**
	 * 
	 */
	@Override
	public void undo() {
		// TODO Auto-generated method stub
		super.undo();
	}

	
}
