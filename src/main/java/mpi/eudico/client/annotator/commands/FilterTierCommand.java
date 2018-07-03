package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that filters the contents of the annotations of a source tier  to
 * new annotations on a destination tier. Can also be used to copy a tier. The
 * destination is a tier of type symbolic association (one-to-one
 * relationship).
 *
 * @author Han Sloetjes
 */
public class FilterTierCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private TranscriptionImpl transcription;
    private TierImpl sourceTier;
    private TierImpl destTier;
    private String[] filters;
    private boolean preserve;
    private boolean createEmpty;

    // store for undo

    /** backup data of existing annotations on the destination tier */
    private List<AnnotationDataRecord> existAnnotations;

    /** store the data of the newly created annotations */
    private List<AnnotationDataRecord> newAnnotations;

    // store for redo

    /** a list of source annotations that have been changed */
    private List<AnnotationDataRecord> existChangedAnnotations;

    /**
     * Creates a new FilterTierCommand instance.
     *
     * @param name the name of the command
     */
    public FilterTierCommand(String name) {
        commandName = name;
    }

    /**
     * Undo the changes made by this command.
     */
    @Override
	public void undo() {
        if ((transcription == null) || (sourceTier == null) ||
                (destTier == null)) {
            return;
        }

        int curPropMode = 0;


        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);

        setWaitCursor(true);

        // delete created annotations
        if (newAnnotations.size() > 0) {
            AnnotationDataRecord destRecord;
            AbstractAnnotation destAnn;

            for (int i = 0; i < newAnnotations.size(); i++) {
                destRecord = newAnnotations.get(i);
                destAnn = (AbstractAnnotation) destTier.getAnnotationAtTime(destRecord.getBeginTime());

                if (destAnn != null) {

                        destTier.removeAnnotation(destAnn);

                } else {
                    LOG.warning("Undo filter tier: could not remove annotation: " + destRecord.getValue() +
						" " + destRecord.getBeginTime() + " - " + destRecord.getEndTime());
                }
            }
        }

        // restore annotation values that have been overwritten 
        if (!preserve && (existAnnotations.size() > 0)) {
            AnnotationDataRecord extRecord;
            AbstractAnnotation extAnn;

            for (int i = 0; i < existAnnotations.size(); i++) {
                extRecord = existAnnotations.get(i);
                extAnn = (AbstractAnnotation) destTier.getAnnotationAtTime(extRecord.getBeginTime());

                if (extAnn != null) {
                    extAnn.setValue(extRecord.getValue());
                } else {
                    LOG.warning("Undo filter tier: could not restore annotation value: " + extRecord.getValue() +
						" " + extRecord.getBeginTime() + " - " + extRecord.getEndTime());
                }
            }
        }

        transcription.setNotifying(true);

        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        if ((transcription == null) || (sourceTier == null) ||
                (destTier == null)) {
            return;
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);

        setWaitCursor(true);

        if (newAnnotations.size() > 0) {
            AnnotationDataRecord destRecord;
            AbstractAnnotation destAnn;

            for (int i = 0; i < newAnnotations.size(); i++) {
                destRecord = newAnnotations.get(i);

                long time = (destRecord.getBeginTime() +
                    destRecord.getEndTime()) / 2;


                    destAnn = (AbstractAnnotation) destTier.createAnnotation(time,
                            time);

                    if (destAnn != null) {
                        destAnn.setValue(destRecord.getValue());
                    } else {
                        LOG.warning("Redo filter tier: could not recreate annotation: " + destRecord.getValue() +
							" " + destRecord.getBeginTime() + " - " + destRecord.getEndTime());
                    }

            }
        }

        // redo values changes on existing annotations 
        if (!preserve && (existChangedAnnotations.size() > 0)) {
            AnnotationDataRecord extRecord;
            AbstractAnnotation extAnn;

            for (int i = 0; i < existChangedAnnotations.size(); i++) {
                extRecord = existChangedAnnotations.get(i);
                extAnn = (AbstractAnnotation) destTier.getAnnotationAtTime(extRecord.getBeginTime());

                if (extAnn != null) {
                    extAnn.setValue(extRecord.getValue());
                } else {
					LOG.warning("Redo filter tier: could not recreate annotation value: " + extRecord.getValue() +
						" " + extRecord.getBeginTime() + " - " + extRecord.getEndTime());
                }
            }
        }

        transcription.setNotifying(true);

        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver the TranscriptionImpl
	 * @param arguments the arguments: <ul><li>arg[0] = the name of the source 
	 * 		tier (String)<li> <li>arg[1] = the name of the destination tier (String)
	 * 		</li> <li>arg[2] = the filter strings (String[])</li> <li>arg[3] = 
	 * 		a flag denoting whether or not to preserve existing annotations 
	 * 		on the destination tier (Boolean)</li> <li>arg[4] = a flag denoting 
	 * 		whether or not to create new annotations for empty source annotations 
	 *    (Boolean)</li></ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        String sourceName = (String) arguments[0];
        String destName = (String) arguments[1];

        filters = (String[]) arguments[2]; //can be null
        preserve = ((Boolean) arguments[3]).booleanValue();
        createEmpty = ((Boolean) arguments[4]).booleanValue();


            sourceTier = (TierImpl) transcription.getTierWithId(sourceName);
            destTier = (TierImpl) transcription.getTierWithId(destName);


        if ((transcription == null) || (sourceTier == null) ||
                (destTier == null)) {
            LOG.severe(
                "Error in retrieving the transcription or one of the tiers.");

            return;
        }

        existAnnotations = new ArrayList<AnnotationDataRecord>();
        newAnnotations = new ArrayList<AnnotationDataRecord>();
        existChangedAnnotations = new ArrayList<AnnotationDataRecord>();

        // filter thread
        new FilterThread().start();

    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Applies the specified filters to the source string.<br>
     * The output is created by extracting/removing the strings in the filters
     * from the source.
     *
     * @param source the source string
     * @param filters the filters
     *
     * @return a filtered string
     */
    public String applyFilters(String source, String[] filters) {
        if ((filters == null) || (filters.length == 0) || (source == null) ||
                (source.length() == 0)) {
            return source;
        }

        char[] resultchars = source.toCharArray();

        // the filters loop
        for (int i = 0; i < filters.length; i++) {
            if ((filters[i] == null) || (filters[i].length() == 0) ||
                    (filters[i].length() > resultchars.length)) {
                continue;
            }

            char[] filter = filters[i].toCharArray();
            int from = 0;

searchloop: 
            while (true) {
                if (from > (resultchars.length - filter.length)) {
                    break searchloop;
                }

                for (; from <= (resultchars.length - filter.length); from++) {
                    // check the first char
                    if (resultchars[from] == filter[0]) {
                        int count = 1;
                        int start = from + count;
                        int end = (from + filter.length) - 1;

                        while (start <= end) {
                            if (resultchars[start++] != filter[count++]) {
                                from++;

                                continue searchloop;
                            }
                        }

                        // if we get here the filter is found
                        StringBuilder buf = new StringBuilder(resultchars.length);
                        buf.append(resultchars);
                        buf.delete(from, end + 1);

                        char[] ch = new char[buf.length()];
                        buf.getChars(0, buf.length(), ch, 0);
                        resultchars = ch;

                        continue searchloop;
                    }
                }
            }

            // end of the current filter
        }

        // end of all filters
        if (resultchars.length == source.length()) {
            return source;
        } else {
            return String.valueOf(resultchars);
        }
    }

    ///////////////////////////////////////////
    // inner class: execution thread
    ///////////////////////////////////////////

    /**
     * Class that handles the filtering in a separate thread.
     *
     * @author Han Sloetjes
     */
    private class FilterThread extends Thread {
        /**
         * Before using this inner class we must ensure none of the relevant
         * fields (transcription, sourcetier and destination tier) are null!
         */
        FilterThread() {
        }

        /**
         * DOCUMENT ME!
         */
        @Override
		public void run() {
            final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(ELANCommandFactory.getRootFrame(
                        transcription), true,
                    ElanLocale.getString("FilterDialog.Message.Filtering"),
                    true, ElanLocale.getString("Button.Cancel"));

            // if we are blocking (modal) call show from a separate thread
            new Thread(new Runnable() {
                    @Override
					public void run() {
                        monitor.show();
                    }
                }).start();

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            ///

                List<AbstractAnnotation> sourceAnnos = sourceTier.getAnnotations();

                if (sourceAnnos.size() <= 0) {
                    monitor.close();

                    return;
                }

                FilterTierCommand.this.transcription.setNotifying(false);

                //start iterating over source annotations
                String srcValue;
                List<Annotation> childrenOnDest;
                AbstractAnnotation childOnDest;

                for (AbstractAnnotation srcAnn : sourceAnnos) {

                    childrenOnDest = srcAnn.getChildrenOnTier(destTier);

                    // should be only one annotation on a symbolic association tier
                    if ((childrenOnDest.size() > 0) && !preserve) {
                        // store old annotation
                        childOnDest = (AbstractAnnotation) childrenOnDest.get(0);
                        existAnnotations.add(new AnnotationDataRecord(
                                childOnDest));

                        childOnDest.setValue(FilterTierCommand.this.applyFilters(
                                srcAnn.getValue(),
                                FilterTierCommand.this.filters));

                        // store the annotation with the new value for redo
                        existChangedAnnotations.add(new AnnotationDataRecord(
                                childOnDest));
                    }

                    // if existing anns need to be preserved, do nothing
                    if (childrenOnDest.size() == 0) {
                        srcValue = srcAnn.getValue();

                        if (srcValue.length() == 0) {
                            // if the source annotation is empty and the create destination
                            // for empty source is selected create one empty annotation
                            if (createEmpty) {
                                long time = (srcAnn.getBeginTimeBoundary() +
                                    srcAnn.getEndTimeBoundary()) / 2;
                                Annotation ann = destTier.createAnnotation(time,
                                        time);

                                if (ann != null) {
                                    newAnnotations.add(new AnnotationDataRecord(
                                            ann));
                                } else  {
									LOG.warning("Filter tier: could not create a new annotation for: " + srcValue + 
										" " + srcAnn.getBeginTimeBoundary() + " - " + srcAnn.getEndTimeBoundary());
                                }
                            }
                        } else {
                            long time = (srcAnn.getBeginTimeBoundary() +
                                srcAnn.getEndTimeBoundary()) / 2;
                            Annotation ann = destTier.createAnnotation(time,
                                    time);

                            if (ann != null) {
                                // apply filters
                                ann.setValue(FilterTierCommand.this.applyFilters(
                                        srcAnn.getValue(),
                                        FilterTierCommand.this.filters));
                                newAnnotations.add(new AnnotationDataRecord(ann));
                            } else  {
								LOG.warning("Filter tier: could not create a new annotation for: " + srcValue + 
									" " + srcAnn.getBeginTimeBoundary() + " - " + srcAnn.getEndTimeBoundary());
							}
                        }
                    }

                    // after completion of a whole source annotation, check the cancelled value of the monitor
                    if (monitor.isCancelled()) {
                        //monitor.close();
                        //return;
                        break;
                    }
                }

                FilterTierCommand.this.transcription.setNotifying(true);

                transcription.setTimeChangePropagationMode(curPropMode);

            ///
            monitor.close();
        }
    }
}
