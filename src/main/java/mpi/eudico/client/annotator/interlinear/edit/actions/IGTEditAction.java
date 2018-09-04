package mpi.eudico.client.annotator.interlinear.edit.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;

/**
 * An abstract action class for editing an IGTAnnotation.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public abstract class IGTEditAction extends AbstractAction {
	/** The IGTAnnotation to be edited */
	protected IGTAnnotation igtAnnotation;
	
	/**
	 * Constructor taking an IGTAnnotation as argument.
	 * 
	 * @param igtAnnotation the IGTAnnotation
	 */
	public IGTEditAction(IGTAnnotation igtAnnotation) {
		super();
		this.igtAnnotation = igtAnnotation;
	}

	/**
	 * Constructor.
	 *  
	 * @param igtAnnotation the IGTAnnotation
	 * @param label the label for the action
	 */
	public IGTEditAction(IGTAnnotation igtAnnotation, String label) {
		super(label);
		this.igtAnnotation = igtAnnotation;
	}

	/**
	 * Constructor.
	 * 
	 * @param igtAnnotation the IGTAnnotation
	 * @param label the label for the action
	 * @param icon the icon for the action
	 */
	public IGTEditAction(IGTAnnotation igtAnnotation, String label, Icon icon) {
		super(label, icon);
		this.igtAnnotation = igtAnnotation;
	}

}
