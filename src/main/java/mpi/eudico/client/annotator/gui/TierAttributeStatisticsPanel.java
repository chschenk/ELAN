package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.util.TableHeaderToolTipAdapter;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.ValueGetter;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;


/**
 * Calculates and shows statistics per transcription, 
 * statistics based on a specific attribute of a tier.
 * Replaces the separate panels for participant, annotator, linguistic type and content language.
 *
 * @author Mark Blokpoel, Han Sloetjes
 * @version 1.0 Dec. 2014 Generalization of statistics based on a specific attribute of tiers (type, participant,
 * annotator, language)
 */
@SuppressWarnings("serial")
public class TierAttributeStatisticsPanel extends AbstractStatisticsPanel {

    /** stores a set of all unique values of the specific attribute used in the file */
    protected Set<String> tierAttribute;
    // the localisation key for the attribute's table header
    protected String attributeHeaderString;
    // the getter for the respective attribute
    protected ValueGetter valueGetter;

    /**
     * Default constructor, invoking superclass
     *
     * @param transcription the transcription
     */
    public TierAttributeStatisticsPanel(TranscriptionImpl transcription) {
        super(transcription);
    }

    /**
     * Creates a new ParticipantStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param duration the media duration
     */
    public TierAttributeStatisticsPanel(TranscriptionImpl transcription,
        long duration) {
        super(transcription, duration);
    }
    
    /**
     * Creates a new ParticipantStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param duration the media duration
     * @param attributeHeaderString the key for the localized table header of the attribute
     * @param valueGetter the object for getting the right attribute of a tier
     */
    public TierAttributeStatisticsPanel(TranscriptionImpl transcription,
        long duration, String attributeHeaderString, ValueGetter valueGetter) {
        super(transcription, duration);
        this.attributeHeaderString = attributeHeaderString;
        this.valueGetter = valueGetter;
        initComponents();
    }

    /**
     * Returns the table
     *
     * @return the statistics table
     */
    @Override
    public JTable getStatisticsTable() {
        return statTable;
    }

    /**
     * Initializes the table.
     */
    @Override
    void initComponents() {
        //Statistics table components
        statPanel = new JPanel();
        statTable = new JTable();
        statTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);

        //statTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
        statTable.setEnabled(false);

        //Initializing table
        initTable();
        statPane = new JScrollPane(statTable);

        Dimension size = new Dimension(600, 100);

        statPane.setPreferredSize(size);

        updateLocale();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        statPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        statPanel.add(statPane, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(statPanel, gridBagConstraints);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    public void updateLocale() {
        statPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "Statistics.Pane.Table")));
    }

    /**
     * This method initializes the table and fills is with statistics.
     * The number of columns currently is 10; if this changes extending classes 
     * need to be updated. 
     */
    protected void initTable() {
        if (transcription != null) {
        	tierAttribute = new HashSet<String>();
        	
            extractAttributeValues();

            int numRows = tierAttribute.size();
            int numCols = 10;

            String[][] data = new String[numRows][numCols];
            String[] headers = {
                    ElanLocale.getString(attributeHeaderString), //0 the attribute
                    ElanLocale.getString("Statistics.NumTiers"), //1
                    ElanLocale.getString("Statistics.NumAnnotations"), //2
                    ElanLocale.getString("Statistics.MinimalDuration"), //3
                    ElanLocale.getString("Statistics.MaximalDuration"), //4
                    ElanLocale.getString("Statistics.AverageDuration"), //5
                    ElanLocale.getString("Statistics.MedianDuration"), //6
                    ElanLocale.getString("Statistics.TotalDuration"), //7
                    ElanLocale.getString("Statistics.TotalDurationPercentage"), //8
                    ElanLocale.getString("Statistics.Latency"), //9
                };

            fillTable(numRows, data);

            DefaultTableModel model = new DefaultTableModel(data, headers);
            statTable.setModel(model);
            statTable.getTableHeader()
                     .addMouseMotionListener(new TableHeaderToolTipAdapter(
                    statTable.getTableHeader()));
            
            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
            IntStringComparator<String> intComp = new IntStringComparator<String>();
            rowSorter.setComparator(1, intComp);
            rowSorter.setComparator(2, intComp);
            
            FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
            for (int i = 3; i < numCols; i++) {
            	rowSorter.setComparator(i, fsComp);
            }
            
            statTable.setRowSorter(rowSorter);
        }
    }
    
    /**
     * Adds all unique values of the specific attribute to a set. 
     */
    protected void extractAttributeValues() {
        if (transcription != null && valueGetter != null) {
        	if (tierAttribute == null) {
        		tierAttribute = new HashSet<String>();
        	}
        	
            List<TierImpl> tiers = transcription.getTiers();

            for (TierImpl tier : tiers) {
                tierAttribute.add(valueGetter.getSortValue(tier));
            }
        }		
	}

    /**
     * This method fills the table with statistics and labels
     *
     * @param numRows The number of rows in the table, usually equal to the
     *        number of the specific attribute in the file
     * @param data The data matrix that will be filled.
     */
    protected void fillTable(int numRows, String[][] data) {
    	if (valueGetter == null) {
    		return;
    	}
    	int y = 0;

        for (String curAttr : tierAttribute) {
            List<TierImpl> tiersWithCurAttr = new ArrayList<TierImpl>();

            for (TierImpl tier : ((List<TierImpl>) transcription.getTiers())) {
                if (curAttr.equals(valueGetter.getSortValue(tier))) {
                	tiersWithCurAttr.add(tier);
                }
            }

            data[y][0] = curAttr.length() == 0 ? "-" : curAttr;
            data[y][1] = Integer.toString(tiersWithCurAttr.size());

            Integer nrAnnotations = Integer.valueOf(0);
            ArrayList<Long> durationList = new ArrayList<Long>(nrAnnotations);
            long timeAnnotated = 0;
            long maxDuration = 0;
            long minDuration = Long.MAX_VALUE;
            ArrayList<Long> beginTimes = new ArrayList<Long>(nrAnnotations);

            for (TierImpl tier : tiersWithCurAttr) {
                nrAnnotations += tier.getNumberOfAnnotations();

                List<AbstractAnnotation> annotations = tier.getAnnotations();

                for (AbstractAnnotation annotation : annotations) {
                    long begin = annotation.getBeginTimeBoundary();
                    long end = annotation.getEndTimeBoundary();
                    long duration = end - begin;

                    durationList.add(new Long(duration));
                    timeAnnotated += duration;
                    maxDuration = (maxDuration < duration) ? duration
                                                           : maxDuration;
                    minDuration = (minDuration > duration) ? duration
                                                           : minDuration;
                    beginTimes.add(new Long(begin));
                }
            }

            if (nrAnnotations == 0) {
                data[y][2] = "-"; /* NumAnnotations */
                data[y][3] = "-"; /* MinDuration */
                data[y][4] = "-"; /* MaxDuration */
                data[y][5] = "-"; /* AvarageDuration */
                data[y][6] = "-"; /* MedianDuration */
                data[y][7] = "-"; /* TotalDuration */
                data[y][8] = "-"; /* TotalDurationPercentage */
                data[y][9] = "-"; /* Latency */
            } else {
                data[y][2] = nrAnnotations.toString(); /* NumAnnotations */
                data[y][3] = format2.format(minDuration / (float) 1000); /* MinDuration */
                data[y][4] = format2.format(maxDuration / (float) 1000); /* MaxDuration */
                data[y][5] = format2.format(timeAnnotated / nrAnnotations / (float) 1000); /* AvarageDuration */

                Collections.sort(durationList);

                int numDurs = durationList.size(); // should be same as numAnns
                long medianDur = 0L;

                if (numDurs == 1) {
                    medianDur = durationList.get(0).longValue();
                } else {
                    if ((numDurs % 2) != 0) {
                        // in case of an odd number, take the middle value
                        medianDur = durationList.get(numDurs / 2).longValue();
                    } else {
                        // in case of an even number, calculate the average of the 
                        // two middle values
                        long h = durationList.get(numDurs / 2).longValue();
                        long l = durationList.get((numDurs / 2) - 1).longValue();
                        medianDur = (h + l) / 2;
                    }
                }

                data[y][6] = format2.format(medianDur / (float) 1000); /* MedianDuration */
                data[y][7] = format2.format(timeAnnotated / (float) 1000); /* TotalDuration */
                if (totalDuration == 0) {
                	data[y][8] = "-"; /* TotalDurationPercentage */
                } else {
                	data[y][8] = format2.format(timeAnnotated / (float) (totalDuration * tiersWithCurAttr.size()) * 100); /* TotalDurationPercentage */
                }
                
                if (beginTimes.size() > 0) {
                    Collections.sort(beginTimes);
                    data[y][9] = format2.format(beginTimes.get(0) / (float) 1000); /* Latency */
                } else {
                    data[y][9] = "-"; /* Latency */
                }
            }

            y++;
        }
    }

}
