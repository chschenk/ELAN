package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;


/**
 * Calculates and shows (tier) statistics per transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AnnotationStatisticsPanel2 extends AbstractStatisticsPanel
    implements ItemListener, ActionListener {
    // columns: tiername, number of annotations, minimum, maximum, 
    // average and median duration, total annotation duration, (total annotation duration as percentage 
    // of media duration,) latency
    // if this changes, check the column comparators
    private int numCols = 8;

    /** Holds value of property DOCUMENT ME! */
    String[] headers;

    //	Tier selection GUI
    private JComboBox tierComboBox;
    private List<TierImpl> rootTiers;
    private String curTier;
    private JLabel tierLabel;
    private JPanel tierSelectionPanel;
    private JCheckBox rootTiersOnlyCB;
    //private long beginBoundary;
    //private long endBoundary;
    private boolean rootsOnly = true;

    /**
     * Creates a new TierStatisticsPanel instance
     *
     * @param transcription the transcription
     */
    public AnnotationStatisticsPanel2(TranscriptionImpl transcription) {
        super(transcription);
        initComponents();
    }

    /**
     * Creates a new TierStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param totalDuration total duration
     */
    public AnnotationStatisticsPanel2(TranscriptionImpl transcription,
        long totalDuration) {
        super(transcription, totalDuration);
        numCols++;
        initComponents();
    }

    /**
     * Returns the statistics table.
     *
     * @return the table
     */
    @Override
	public JTable getStatisticsTable() {
        return statTable;
    }

    /**
     * Initializes ui components and table.
     */
    @Override
	void initComponents() {
        createHeaders();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        //    	Tier selection components
        tierComboBox = new JComboBox();
        rootTiers = new ArrayList<TierImpl>();

        if (rootsOnly) {
            extractRootTiers();
        } else {
            extractAllTiers();
        }

        tierLabel = new JLabel();
        rootTiersOnlyCB = new JCheckBox();
        rootTiersOnlyCB.setSelected(rootsOnly);

        tierSelectionPanel = new JPanel(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 6, 2, 12);
        tierSelectionPanel.add(tierLabel, gridBagConstraints);

        tierComboBox.addItemListener(this);
        tierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(tierComboBox, gridBagConstraints);

        //Insets insets2 = new Insets(0, 6, 0, 6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        //gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 26, 0, 12);
        tierSelectionPanel.add(rootTiersOnlyCB, gridBagConstraints);

        //Statistics table components
        statPanel = new JPanel();
        statTable = new JTable();
        statTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        //statTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
        statTable.setEnabled(false);
        //Initializing table
        initTable();
        statPane = new JScrollPane(statTable);

        Dimension size = new Dimension(500, 100);
        //	statPane.setMinimumSize(size);
        statPane.setPreferredSize(size);

        updateLocale();

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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(tierSelectionPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(statPanel, gridBagConstraints);
        rootTiersOnlyCB.addActionListener(this);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    public void updateLocale() {
        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "Statistics.Panel.Tier")));
        tierLabel.setText(ElanLocale.getString("Statistics.Label.Tier"));
        rootTiersOnlyCB.setText(ElanLocale.getString(
                "Statistics.Label.RootTiers"));
        statPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "Statistics.Pane.Table")));
    }

    /**
     * Note: any changes here have to be taken into account in the
     * settings of the row sorter and row comparators.
     */
    private void createHeaders() {
        headers = new String[numCols];
        headers[0] = ElanLocale.getString("Frame.GridFrame.ColumnAnnotation");
        headers[1] = ElanLocale.getString("Statistics.Occurrences");
        headers[2] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[3] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[4] = ElanLocale.getString("Statistics.AverageDuration");
        headers[5] = ElanLocale.getString("Statistics.MedianDuration");
        headers[6] = ElanLocale.getString("Statistics.TotalDuration");
        headers[7] = ElanLocale.getString("Statistics.Latency");

        if (numCols == 9) {
            headers[7] = ElanLocale.getString(
                    "Statistics.TotalDurationPercentage");
            headers[8] = ElanLocale.getString("Statistics.Latency");
        }
    }

    /**
     * Creates contents and headers for the statistics table.
     */
    private void initTable() {
        if (transcription != null) {
            String[][] data = null;

            if (curTier == null) {
                data = new String[0][numCols];
            } else {
                TierImpl tier = (TierImpl) transcription.getTierWithId(curTier);

                if (tier != null) {
                    // find num unique values -> num rows
                    TreeSet<String> annos = getAnnotationValues(tier);
                    int numRows = 0;

                    if ((annos != null) && (annos.size() > 0)) {
                        numRows = annos.size();
                        data = new String[numRows][numCols];

                        int i = 0;
                        Iterator<String> itAnnValues = annos.iterator();

                        while (itAnnValues.hasNext()) {
                            String value = itAnnValues.next();
                            data[i] = getRowForValue(tier, value);
                            i++;
                        }
                    } else {
                        data = new String[numRows][numCols];
                    }
                } else {
                    data = new String[0][numCols];
                }
            }

            DefaultTableModel model = new DefaultTableModel(data, headers);
            statTable.setModel(model);
            statTable.getTableHeader()
                     .addMouseMotionListener(new TableHeaderToolTipAdapter(
                    statTable.getTableHeader()));
            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
            IntStringComparator<String> intComp = new IntStringComparator<String>();
            rowSorter.setComparator(1, intComp);
            
            FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
            for (int i = 2; i < numCols; i++) {
            	rowSorter.setComparator(i, fsComp);
            }
            
            statTable.setRowSorter(rowSorter);
        }
    }

    /**
     * Returns the set of different annotation values for a tier
     *
     * @param tier the tier to get annotation values from
     *
     * @return a treeset of annotations
     */
    private TreeSet<String> getAnnotationValues(TierImpl tier) {
        TreeSet<String> annValues = null;

        if ((tier == null) || (tier.getLinguisticType() == null)) {
            return annValues;
        }

        //If the tier is using controlled vocabulary
        //return all the vocabulary entries extended with values that are not in the CV
        if (tier.getLinguisticType().isUsingControlledVocabulary()) {
            String CVname = tier.getLinguisticType()
                                .getControlledVocabularyName();
            ControlledVocabulary CV = transcription.getControlledVocabulary(CVname);

//            if ((CV != null) && (CV.getEntryValues().length > 0)) {
//                String[] cvEntriesVal = CV.getEntryValues();
//                annValues = new TreeSet();
//
//                for (int i = 0; i < cvEntriesVal.length; i++)
//                    if ((cvEntriesVal[i] != null) &&
//                            (cvEntriesVal[i].length() > 0)) {
//                        annValues.add(cvEntriesVal[i]);
//                    }
//            }
            if (CV != null) {
            	int nLanguages = CV.getNumberOfLanguages();
                annValues = new TreeSet<String>();
            	
            	for (int i = 0; i < nLanguages; i++) {
            		for (String val : CV.getValuesIterable(i)) {
            			if (val != null && !val.isEmpty())
            				annValues.add(val);
            		}
            	}
            }
        }

        // scan the annotations and retrieve values
        List<AbstractAnnotation> annotations = tier.getAnnotations();

        if ((annotations != null) && (annotations.size() > 0)) {
            if (annValues == null) {
                annValues = new TreeSet<String>();
            }

            Iterator<AbstractAnnotation> annosIt = annotations.iterator();

            while (annosIt.hasNext()) {
                AbstractAnnotation ann = (AbstractAnnotation) annosIt.next();

                if ((ann.getValue() != null)) {
                    annValues.add(ann.getValue());
                }
            }
        }

        return annValues;
    }

    /**
     * Iterates over all annotations and calculates minimum, maximum, average
     * a,d total duration as well as number of annotations and the latency
     * (which currently is the begin time of the first annotation).
     *
     * @param tier the tier
     * @param annValue the annotation value to calculate statistics for
     *
     * @return a string array, one row of the statistics table
     */
    private String[] getRowForValue(TierImpl tier, String annValue) {
        String[] row = new String[numCols];
        row[0] = annValue;

        List<AbstractAnnotation> annotations = tier.getAnnotations();
        int numAnn = annotations.size();

        if (numAnn == 0) {
            for (int i = 1; i < numCols; i++) {
                row[i] = EMPTY;
            }

            return row;
        }

        int numOcc = 0;
        AbstractAnnotation ann = null;
        long minDur = Long.MAX_VALUE;
        long maxDur = 0L;
        long totalDur = 0L;
        long medianDur = 0L;
        List<Long> durList = new ArrayList<Long>(numAnn); //size could be smaller, or default
        long firstOcc = Long.MAX_VALUE;

        long b;
        long e;
        long d;

        for (int i = 0; i < numAnn; i++) {
            ann = (AbstractAnnotation) annotations.get(i);

            if (!annValue.equals(ann.getValue())) {
                continue;
            }

            numOcc++;
            b = ann.getBeginTimeBoundary();
            e = ann.getEndTimeBoundary();
            d = e - b;
            durList.add(new Long(d));

            if (b < firstOcc) {
                firstOcc = b;
            }

            if (d < minDur) {
                minDur = d;
            }

            if (d > maxDur) {
                maxDur = d;
            }

            totalDur += d;
        }

        if (numOcc == 0) {
            for (int i = 1; i < numCols; i++) {
                row[i] = "0";
            }

            return row;
        }

        // calculate median
        Collections.sort(durList);

        int numDurs = durList.size(); // should be same as numOcc

        if (numDurs == 1) {
            medianDur = durList.get(0).longValue();
        } else {
            if ((numDurs % 2) != 0) {
                // in case of an odd number, take the middle value
                medianDur = durList.get(numDurs / 2).longValue();
            } else {
                // in case of an even number, calculate the average of the 
                // two middle values
                long h = durList.get(numDurs / 2).longValue();
                long l = durList.get((numDurs / 2) - 1).longValue();
                medianDur = (h + l) / 2;
            }
        }

        row[1] = String.valueOf(numOcc);
        row[2] = format2.format(minDur / (float) 1000);
        row[3] = format2.format(maxDur / (float) 1000);
        row[4] = format2.format((totalDur / (float) numOcc) / 1000);
        row[5] = format2.format(medianDur / (float) 1000);
        row[6] = format2.format(totalDur / (float) 1000);
        row[7] = format2.format(firstOcc / (float) 1000);

        if (numCols == 9) {
            if (totalDuration != 0) {
                row[7] = format2.format((totalDur / (float) totalDuration) * 100);
            } else {
                row[7] = "-";
            }

            row[8] = format2.format(firstOcc / (float) 1000);
        }

        return row;
    }

    /**
     * Extract the root tiers
     */
    private void extractRootTiers() {
        tierComboBox.removeItemListener(this);
        tierComboBox.removeAllItems();

        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();
            Iterator<TierImpl> tierIt = tiers.iterator();
            TierImpl tier = null;

            while (tierIt.hasNext()) {
                tier = (TierImpl) tierIt.next();

                if (tier.getLinguisticType().getConstraints() == null) {
                    tierComboBox.addItem(tier.getName());
                    rootTiers.add(tier);
                }
            }

            if (curTier != null) {
                tierComboBox.setSelectedItem(curTier);
            }

            // if there are no tiers yet
            if (tierComboBox.getModel().getSize() == 0) {
                tierComboBox.addItem(EMPTY);
            }
        } else {
            tierComboBox.addItem(EMPTY);
        }

        curTier = (String) tierComboBox.getSelectedItem();
        tierComboBox.addItemListener(this);
    }

    /**
     * Extract all the tiers and add them to the combobox.
     */
    private void extractAllTiers() {
        tierComboBox.removeItemListener(this);
        tierComboBox.removeAllItems();

        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();
            Iterator<TierImpl> tierIt = tiers.iterator();
            TierImpl tier = null;

            while (tierIt.hasNext()) {
                tier = (TierImpl) tierIt.next();
                tierComboBox.addItem(tier.getName());
                rootTiers.add(tier);
            }

            if (curTier != null) {
                tierComboBox.setSelectedItem(curTier);
            }

            // if there are no tiers yet
            if (tierComboBox.getModel().getSize() == 0) {
                tierComboBox.addItem(EMPTY);
            }
        } else {
            tierComboBox.addItem(EMPTY);
        }

        curTier = (String) tierComboBox.getSelectedItem();
        tierComboBox.addItemListener(this);
    }

    /**
     * Selection of a different tier.
     *
     * @param ie the item event
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            String newSel = (String) tierComboBox.getSelectedItem();

            if (newSel.equals(curTier)) {
                return;
            }

            curTier = newSel;
            //Reset the statistics table
            initTable();
            repaint();
        }
    }

    /**
     * Event handling for the checkboxes.
     *
     * @param e event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == rootTiersOnlyCB) {
            String oldCurTier = curTier;

            if (rootTiersOnlyCB.isSelected()) {
                extractRootTiers();
            } else {
                extractAllTiers();
            }

            if (!oldCurTier.equals(curTier)) {
                tierComboBox.setSelectedItem(oldCurTier); // try to restore
                curTier = (String) tierComboBox.getSelectedItem();
                initTable();
                repaint();
            }
        }
    }
}
