package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * Administrates the current selection.
 */
public class Selection {
    private List<SelectionListener> listeners;
    private long selectionBeginTime;
    private long selectionEndTime;
    private long previousSelectionBeginTime;
    private long previousSelectionEndTime;

    /**
     * Creates an empty Selection (begin time == end time).
     */
    public Selection() {
        listeners = new ArrayList<SelectionListener>();
        selectionBeginTime = 0;
        selectionEndTime = 0;
        previousSelectionBeginTime = 0;
        previousSelectionEndTime = 0;
    }

    /**
     * AR Should be called set? it is obvious we are talking about a selection,
     * refactor with eclipse Sets the beginTime and the endTime of the
     * selection in milli seconds.
     *
     * @param beginTime the beginTime of the selection in milli seconds.
     * @param endTime the endTime of the selection in milli seconds.
     */
    public void setSelection(long beginTime, long endTime) {
        // Only update if needed. Null selection only allowed
        // with beginTime = endTime = 0;
        //		if (beginTime != 0 && beginTime == endTime) {
        //			return;
        //		}
        // check if it realy is a new selection
        if ((selectionBeginTime != beginTime) | (selectionEndTime != endTime)) {
            previousSelectionBeginTime = selectionBeginTime;
            previousSelectionEndTime = selectionEndTime;
            selectionBeginTime = beginTime;
            selectionEndTime = endTime;

            // Tell all the interested Selectionlisteners about the change
            notifyListeners();
        }
    }

    /**
     * Set the selection to the begin and end time boundaries of the
     * annotation. If the annotation == null the selection is not changed
     *
     * @param annotation
     */
    public void setSelection(Annotation annotation) {
        if (annotation != null) {
            setSelection(annotation.getBeginTimeBoundary(),
                annotation.getEndTimeBoundary());
        }
    }

    /**
     * Utility method to clear the selection
     */
    public void clear() {
        setSelection(0, 0);
    }

    /**
     * Tell all the listeners what the current Selection is.
     */
    public void notifyListeners() {
        final int size = listeners.size();
		for (int i = 0; i < size; i++) {
            listeners.get(i).updateSelection();
        }
    }

    /**
     * Returns the begin time of the selection in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTime() {
        return selectionBeginTime;
    }

    /**
     * Returns the end time of the selection in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getEndTime() {
        return selectionEndTime;
    }

    /**
     * Returns the previous begin time of the selection in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getPreviousBeginTime() {
        return previousSelectionBeginTime;
    }

    /**
     * Returns the previous end time of the selection in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getPreviousEndTime() {
        return previousSelectionEndTime;
    }

    /**
     * Add a listener for Selection events.
     *
     * @param listener the listener that wants to be notified for Selection
     *        events.
     */
    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
        listener.updateSelection();
    }

    /**
     * Remove a listener for Selection events.
     *
     * @param listener the listener that no longer wants to be notified for
     *        Selection events.
     */
    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }
}
