package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Iterates over the annotations of one or more tiers and replaces the value by
 * a given label or prefix followed by an increasing index/number. Prefix and
 * counter all both optional. The format, start value and increment value of
 * the counter part are customizable, as well as the prefix. When more tiers
 * are involved all annotations will be added to one List and sorted before
 * the actual iteration.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class LabelAndNumberCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private TranscriptionImpl transcription;
    private List<String> tierNames;
    private String prefix;
    private int numDigits;
    private int startIntValue = -1;
    private int intIncr = 0;
    private double startDoubleVal = -1.0d;
    private double doubleIncr = 0.0d;
    private int numDecimals = 0;
    private boolean numberCountPart = true;
    private ArrayList<AnnotationValuesRecord> records;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public LabelAndNumberCommand(String name) {
        commandName = name;
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        setWaitCursor(true);
        transcription.setNotifying(false);

        String name = null;
        TierImpl tier = null;

        if (tierNames != null) {
            if (records != null) {
                Annotation ann = null;
                AnnotationValuesRecord record = null;

                for (int i = 0; i < records.size(); i++) {
                    record = records.get(i);

                    name = record.getTierName();

                    if ((tier == null) || !tier.getName().equals(name)) {
                        tier = transcription.getTierWithId(name);
                    }

                    if (tier != null) {
                        ann = tier.getAnnotationAtTime(record.getBeginTime());

                        if ((ann != null) &&
                                (ann.getEndTimeBoundary() == record.getEndTime())) {
                            ann.setValue(record.getValue());
                        } else {
                            LOG.warning(
                                "The annotation could not be found for undo");
                        }
                    } else {
                        LOG.warning("The tier could not be found: " + name);
                    }
                }
            } else {
                LOG.info("No annotation records have been stored for undo.");
            }
        } else {
            LOG.warning("No tier names have been stored.");
        }

        transcription.setNotifying(true);
        setWaitCursor(false);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        setWaitCursor(true);
        transcription.setNotifying(false);

        String name = null;
        TierImpl tier = null;

        if (tierNames != null) {
            if (records != null) {
                Annotation ann = null;
                AnnotationValuesRecord record = null;

                for (int i = 0; i < records.size(); i++) {
                    record = records.get(i);
                    name = record.getTierName();

                    if ((tier == null) || !tier.getName().equals(name)) {
                        tier = transcription.getTierWithId(name);
                    }

                    if (tier != null) {
                        ann = tier.getAnnotationAtTime(record.getBeginTime());

                        if ((ann != null) &&
                                (ann.getEndTimeBoundary() == record.getEndTime())) {
                            ann.setValue(record.getNewLabelValue());
                        } else {
                            LOG.warning(
                                "The annotation could not be found for redo");
                        }
                    } else {
                        LOG.warning("Could not find tier for redo: " + name);
                    }
                }
            } else {
                LOG.info("No annotation records have been stored for undo.");
            }
        } else {
            LOG.warning("No tier names have been stored.");
        }

        transcription.setNotifying(true);
        setWaitCursor(false);
    }

    /**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.<br>
	 * If arg[2] is null it is assumed that there is no number part.
	 *
	 * @param receiver
	 *            the TranscriptionImpl
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the selected tiers (List&lt;String>)</li>
	 *            <li>arg[1] = the prefix part (can be null) (String)</li>
	 *            <li>arg[2] = the number format can be null(Integer or Double)</li>
	 *            <li>arg[3] = the number of integer digits (can be null)
	 *            (Integer)</li>
	 *            <li>arg[4] = the start value (can be null) (Integer or Double)
	 *            </li>
	 *            <li>arg[5] = the increment (can be null) (Integer or Double)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        tierNames = (List<String>) arguments[0];
        prefix = (String) arguments[1];

        Object format = arguments[2];
        if (arguments[3] != null) {
            numDigits = ((Integer) arguments[3]).intValue();
        }

        Object startVal = arguments[4];
        Object incrementVal = arguments[5];

        if (prefix == null) {
            prefix = "";
        }

        if (format == null) {
            numberCountPart = false;
        } else {
            if (startVal instanceof Integer) {
                startIntValue = ((Integer) startVal).intValue();
            } else {
                startDoubleVal = ((Double) startVal).doubleValue();
            }

            if (incrementVal instanceof Integer) {
                intIncr = ((Integer) incrementVal).intValue();
            } else {
                doubleIncr = ((Double) incrementVal).doubleValue();

                String dv = String.valueOf(doubleIncr);
                numDecimals = dv.length() - dv.indexOf(".") - 1;
            }
        }

        changeAnnotationValues();
    }

    /**
     * The actual iteration and label creation.
     */
    private void changeAnnotationValues() {
        setWaitCursor(true);
        transcription.setNotifying(false);

        TierImpl tier = null;
        List<AbstractAnnotation> anns = null;

        if ((tierNames == null) || (tierNames.size() == 0)) {
            LOG.warning("No tier selected.");
            transcription.setNotifying(true);
            setWaitCursor(false);

            return;
        } else if (tierNames.size() == 1) {
            tier = transcription.getTierWithId(tierNames.get(0));

            if (tier == null) {
                LOG.warning("The tier " + tierNames.get(0) +
                    " does not exist.");
                transcription.setNotifying(true);
                setWaitCursor(false);

                return;
            }

            // are the annotations always ordered??
            anns = tier.getAnnotations();
        } else {
            anns = new ArrayList<AbstractAnnotation>();

            String name;

            for (int i = 0; i < tierNames.size(); i++) {
                name = tierNames.get(i);
                tier = transcription.getTierWithId(name);

                if (tier != null) {
                    anns.addAll(tier.getAnnotations());
                } else {
                    LOG.warning("The tier " + name + " does not exist.");
                }
            }

            Collections.sort(anns);
        }

        records = new ArrayList<AnnotationValuesRecord>(anns.size());

        Annotation ann = null;
        String nextLabel = null;
        AnnotationValuesRecord record = null;

        for (int i = 0; i < anns.size(); i++) {
            ann = anns.get(i);
            nextLabel = getNextLabel(i);
            record = new AnnotationValuesRecord(ann);
            record.setNewLabelValue(nextLabel);
            records.add(record);
            ann.setValue(nextLabel);
        }

        transcription.setNotifying(true);
        setWaitCursor(false);
    }

    /**
     * Constructs the next label based on prefix, number format, start and
     * increment value and  the current index in the list.
     *
     * @param count the current index in the list of annotations
     *
     * @return the formatted string
     */
    private String getNextLabel(int count) {
        if (numberCountPart) {
            if (startIntValue > -1) {
                if (numDigits == 1) {
                    return prefix +
                    String.valueOf(startIntValue + (count * intIncr));
                } else {
                	StringBuilder sb = new StringBuilder(String.valueOf(startIntValue +
                                (count * intIncr)));

                    while (sb.length() < numDigits) {
                        sb.insert(0, 0);
                    }

                    return prefix + sb.toString();
                }
            } else if (startDoubleVal > -1) {
                double nd = startDoubleVal + (count * doubleIncr);

                if (numDigits == 1) {
                    String sv = String.valueOf(startDoubleVal +
                            (count * doubleIncr));

                    if ((sv.length() - sv.indexOf(".") - 1) > numDecimals) {
                        sv = sv.substring(0, sv.indexOf(".") + numDecimals);
                    }

                    return prefix + sv;
                } else {
                	StringBuilder sb = new StringBuilder(String.valueOf(nd));

                    while (sb.indexOf(".") < numDigits) {
                        sb.insert(0, 0);
                    }

                    if ((sb.length() - sb.indexOf(".") - 1) > numDecimals) {
                        return prefix +
                        sb.substring(0, sb.indexOf(".") + 1 + numDecimals);
                    } else {
                        return prefix + sb.toString();
                    }
                }
            } else {
                return prefix;
            }
        } else {
            return prefix;
        }
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
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
