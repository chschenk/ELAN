package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.InterlinearEditor;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAbstractDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * An alternative annotation navigation implementation, based on the interlinear layout 
 * of the blocks of annotations. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTNavigateAction extends AbstractAction {
	public static enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN
	}

	private InterlinearEditor interEditor;
	private Direction direction;
	private boolean skipEmptyLinesWithAnnotationUpDown = true;
	
	/**
	 * Constructor.
	 * @param interEditor the viewer containing the special layout of annotations (in sub-components)
	 * @param name the name of the action
	 * @param direction the direction of the activation
	 */
	public IGTNavigateAction(InterlinearEditor interEditor, 
			String name, Direction direction) {
		super(name);
		this.interEditor = interEditor;
		this.direction = direction;
	}

	/**
	 * Searches for the next annotation to activate.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Annotation activeAnn = interEditor.getActiveAnnotation();
		if (activeAnn == null) {
			return; // could try to select the first or last in the current block?
		}
		AbstractAnnotation aa = (AbstractAnnotation)activeAnn;
		int rowIndex = interEditor.getRowForAnnotation(aa);
		
		if (rowIndex == -1) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("The active annotation is not found in the viewer");
			}
			// the tier the annotation is on might be hidden, could do a "best guess" of
			// an annotation to activate?
			return;
		}
		IGTAbstractDataModel rowModel = interEditor.getModelFromRow(rowIndex);
		int currentTierIndex = rowModel.getRowIndexForTier(aa.getTier().getName());
		// don't need to check the index, should be >= 0 because of the test above getRowForAnnotation		
		IGTTier curIgtTier = rowModel.getRowData(currentTierIndex);		
		List<IGTAnnotation> igtAnnList = curIgtTier.getAnnotations();
		int curAnnIndex = -1;
		IGTAnnotation curIgtAnn = null;
		
		for (IGTAnnotation ia : igtAnnList) {
			if (ia.getAnnotation() == aa) {
				curIgtAnn = ia;
				break;
			}
		}
		if (curIgtAnn != null) {
			curAnnIndex = igtAnnList.indexOf(curIgtAnn);
		} 
		
		if (curIgtAnn == null || curAnnIndex < 0) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("The active annotation is not found in the row it is supposed to be in: %d %s", 
						currentTierIndex, curIgtTier.getTierName()));
			}
		}
		
		IGTAnnotation nextIGTAnn = null;
		
		switch (direction) {
		case LEFT:
				nextIGTAnn = curIgtTier.getPreviousAnnotation(curIgtAnn);
				
				if (nextIGTAnn == null) {
					// move a tier up, if there
					IGTTier nextIgtTier = null;
					if (currentTierIndex > 0) {
						for (int i = currentTierIndex - 1; i >= 0; i--) {
							nextIgtTier = rowModel.getRowData(i);
							if (nextIgtTier.isSpecial()) {
								continue;
							}
							if (nextIgtTier.getAnnotations().isEmpty()) {
								continue;
							}
							nextIGTAnn = nextIgtTier.getAnnotations().get(nextIgtTier.getAnnotations().size() - 1);
							break;
						}
					}
					if (nextIGTAnn == null) {
						// move up one row
						if (rowIndex > 0) {
							nextIGTAnn = findAnnotationUpStartingFromRow(rowIndex - 1);
						}
					}
				}
			
			break;
		case RIGHT:
			nextIGTAnn = curIgtTier.getNextAnnotation(curIgtAnn);
			
			if (nextIGTAnn == null) {
				// move a tier down, if there
				IGTTier nextIgtTier = null;
				if (currentTierIndex < rowModel.getRowCount() - 1) {
					for (int i = currentTierIndex + 1; i < rowModel.getRowCount(); i++) {
						nextIgtTier = rowModel.getRowData(i);
						if (nextIgtTier.isSpecial()) {
							continue;
						}
						if (nextIgtTier.getAnnotations().isEmpty()) {
							continue;
						}
						nextIGTAnn = nextIgtTier.getAnnotations().get(0);
						break;
					}
				}
				if (nextIGTAnn == null) {
					// move down one row
					if (rowIndex < interEditor.getRowCount() - 1) {
						nextIGTAnn = findAnnotationDownStartingFromRow(rowIndex + 1);
					}
				}
			}
			break;
		case UP:
			if (currentTierIndex > 0) {
				int xCoord = curIgtAnn.getRenderInfo().x;
				
				for (int i = currentTierIndex - 1; i >= 0; i--) {
					IGTTier nextTier = rowModel.getRowData(i);
					if (nextTier.isSpecial()) {
						continue;
					}
					nextIGTAnn = getAnnotationAtLocation(nextTier, xCoord);
				
					if (nextIGTAnn != null) {
						break; // stop the tier loop
					}
				}
			}
			
			if (nextIGTAnn == null) {
				//move up one row
				if (rowIndex > 0) {
					nextIGTAnn = findAnnotationUpStartingFromRow(rowIndex - 1);
				}
			}
			break;
		case DOWN:
			if (currentTierIndex < rowModel.getRowCount() - 1) {
				int xCoord = curIgtAnn.getRenderInfo().x;
				
				for (int i = currentTierIndex + 1; i < rowModel.getRowCount(); i++) {
					IGTTier nextTier = rowModel.getRowData(i);
					if (nextTier.isSpecial()) {
						continue;
					}
					nextIGTAnn = getAnnotationAtLocation(nextTier, xCoord);
					
					if (nextIGTAnn != null) {
						break; // stop the tier loop
					}
				}
			}
			
			if (nextIGTAnn == null) {
				//move down one row
				if (rowIndex < interEditor.getRowCount() - 1) {
					nextIGTAnn = findAnnotationDownStartingFromRow(rowIndex + 1);
				}
			}
			break;
			default:
		}

		if (nextIGTAnn != null) {
			Command actCom = ELANCommandFactory.createCommand(interEditor.getViewerManager().getTranscription(),
					ELANCommandFactory.ACTIVE_ANNOTATION);
			actCom.execute(interEditor.getViewerManager(), new Object[]{nextIGTAnn.getAnnotation(), Boolean.FALSE});
		}
	}

	/**
	 * @param igtTier the tier (not null!) to check for an annotation at or overlapping the specified
	 * x-coordinate. This methods takes the flag for (not) skipping empty position into account. 
	 * @param xCoord the location or position to check for
	 * @return the matching annotation or null
	 */
	private IGTAnnotation getAnnotationAtLocation(IGTTier igtTier, int xCoord) {
		IGTAnnotation traversAnn = null;
		IGTAnnotation matchAnn = null;
		
		for (int j = 0; j < igtTier.getAnnotations().size(); j++) {
			IGTAnnotation loopAnn = igtTier.getAnnotations().get(j);
			if (loopAnn.getRenderInfo().x + loopAnn.getRenderInfo().calcWidth < xCoord) {
				traversAnn = loopAnn;
			} else if (xCoord >= loopAnn.getRenderInfo().x && xCoord <=  
					loopAnn.getRenderInfo().x + loopAnn.getRenderInfo().calcWidth){
				matchAnn = loopAnn;
				break;							
			} else if (traversAnn == null && !skipEmptyLinesWithAnnotationUpDown) {
				// accept the first after the reference annotation
				matchAnn = loopAnn;
				break;
			}						
		}
		// use a flag to determine whether or not to skip a tier if it is "empty" at the location
		// of the current active annotation
		if (!skipEmptyLinesWithAnnotationUpDown && matchAnn == null && traversAnn!= null) {
			matchAnn = traversAnn;
		}
		
		return matchAnn;
	}
	
	/**
	 * Search the rows upward starting at the specified row until an annotation is found.
	 * The tiers in the row are also search bottom-to-top.
	 * 
	 * @param rowIndex the row in the editor to start to search
	 * @return the first real annotation found or null
	 */
	private IGTAnnotation findAnnotationUpStartingFromRow(int rowIndex) {
		for (int i = rowIndex; i >= 0; i--) {
			IGTAbstractDataModel rowModel = interEditor.getModelFromRow(i);
			
			for (int j = rowModel.getRowCount() - 1; j >=0; j--) {
				IGTTier igtTier = rowModel.getRowData(j);
				if (igtTier.isSpecial() || igtTier.getAnnotations().isEmpty()) {
					continue;
				}
				
				switch (direction) {
				case LEFT:
					return igtTier.getAnnotations().get(igtTier.getAnnotations().size() - 1);// last one
					
				case UP:
					return igtTier.getAnnotations().get(0);// first annotation
					
					default:
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Search the rows downward starting at the specified row until an annotation is found.
	 * The tiers in the row are also search top-to-bottom.
	 * 
	 * @param rowIndex the row in the editor to start to search
	 * @return the first real annotation found or null
	 */
	private IGTAnnotation findAnnotationDownStartingFromRow(int rowIndex) {
		for (int i = rowIndex; i < interEditor.getRowCount(); i++) {
			IGTAbstractDataModel rowModel = interEditor.getModelFromRow(i);
			
			for (int j = 0; j < rowModel.getRowCount(); j++) {
				IGTTier igtTier = rowModel.getRowData(j);
				if (igtTier.isSpecial() || igtTier.getAnnotations().isEmpty()) {
					continue;
				}
				
				switch (direction) {
				case RIGHT:
					// fall through always take the first annotation
				case DOWN:
					return igtTier.getAnnotations().get(0);// first annotation
					
					default:
				}
			}
		}
		
		return null;
	}
}
