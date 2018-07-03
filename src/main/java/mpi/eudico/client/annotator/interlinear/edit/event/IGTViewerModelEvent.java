package mpi.eudico.client.annotator.interlinear.edit.event;

import java.util.EventObject;

import mpi.eudico.client.annotator.interlinear.edit.IGTViewerModel;

/**
 * An IGTViewerModelEvent object is passed to listeners to changes in a IGTViewerModel.
 * 
 * @author Han Sloetjes
 */
public class IGTViewerModelEvent extends EventObject {
	private int row = -1;
	private ModelEventType type = ModelEventType.CHANGE;
	private IGTDataModelEvent dataEvent;
	/**
	 * Constructor with the source of the event as argument
	 *  
	 * @param source the source of the event, the viewer model
	 */
	public IGTViewerModelEvent(IGTViewerModel source) {
		super(source);
	}

	/**
	 * A viewer model event with a row index and data model event as parameters. The data in the specific
	 * row changed 
	 * 
	 * @param source the viewer model
	 * @param row the changed row
	 * @param dataEvent the event of the change in the data. Implies that the event type is CHANGE 
	 */
	public IGTViewerModelEvent(IGTViewerModel source, int row, IGTDataModelEvent dataEvent) {
		super(source);
		this.row = row;
		this.dataEvent = dataEvent;
	}
	
	/**
	 * A viewer model event with a row index and data model event as parameters. The data in the specific
	 * row changed 
	 * 
	 * @param source the viewer model
	 * @param row the changed row
	 * @param type the type of event e.g. a row was added or removed
	 */
	public IGTViewerModelEvent(IGTViewerModel source, int row, ModelEventType type) {
		super(source);
		this.row = row;
		this.type = type;
	}
	
	/**
	 * Returns the changed row, or -1 if the event is not connected to a single row.
	 *  
	 * @return the editing row
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Returns the type of the viewer model event, defaults to CHANGE
	 * @return
	 */
	public ModelEventType getType() {
		return type;
	}
	
	/**
	 * Returns the row change event that triggered the viewer model event.
	 * 
	 * @return the row change event that triggered the viewer model event
	 */
	public IGTDataModelEvent getRowDataEvent() {
		return dataEvent;
	}
}
