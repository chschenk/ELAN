package mpi.eudico.client.annotator.interlinear.edit;

import java.util.List;

//import javax.swing.JPopupMenu;

import mpi.eudico.client.annotator.interlinear.edit.actions.IGTEditAction;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;

/**
 * An interface specifying methods for setting up and providing 
 * (ui) elements needed for editing IGT annotations. 
 * 
 * @author Han Sloetjes
 *
 */
public interface IGTEditProvider {
	
	/**
	 * Returns a List of Action objects for the given annotation.
	 * 
	 * @param igtAnnotation the annotation to list the actions for
	 * @return a list of actions
	 */
	public List<IGTEditAction> actionsForAnnotation(IGTAnnotation igtAnnotation);

	/**
	 * 
	 * @param parent the parent annotation
	 * @return a list of actions for "empty slots", empty positions on depending tiers
	 * where there is a parent annotation
	 */
	public List<IGTEditAction> actionsForEmptySpace(IGTAnnotation parent);
}
