package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.util.TableHeaderToolTipAdapter;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;

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
import java.util.Set;
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


/**
 * Calculates and shows (annotation) statistics per tier.  Only root tiers are
 * currently supported.
 *
 * @author Ouriel Grynszpan, Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AnnotationStatisticsPanel extends AbstractStatisticsPanel
    implements ItemListener, ActionListener {
	//	Tier selection GUI
    private JComboBox tierComboBox;
    private List<TierImpl> rootTiers;
    private String curTier;
    private JLabel tierLabel;
    private JPanel tierSelectionPanel;
    private JCheckBox rootTiersOnlyCB;
    private JCheckBox collapseAdjacentCB;
    private JCheckBox mediaDurObservationCB;

    //Statistics table headers
    private String annotations = "Annotations";
    private String occurrences = "Occurrences";
    private String frequency = "Frequency";
    private String averageDuration = "Average Duration";
    private String timeRatio = "Time Ratio";
    private String latency = "Latency";
    private long beginBoundary;
    private long endBoundary;
    private boolean rootsOnly = true;
    private boolean collapseValues = true;
    private boolean useMediaDuration = false;

    /**
     * Creates a new AnnotationStatisticsPanel instance
     *
     * @param transcription the transcription
     */
    public AnnotationStatisticsPanel(TranscriptionImpl transcription) {
        super(transcription);
        readPreferences();
        initComponents();
    }

    /**
     * Creates a new AnnotationStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param totalDuration the duration
     */
    public AnnotationStatisticsPanel(TranscriptionImpl transcription,
        long totalDuration) {
        super(transcription, totalDuration);
        readPreferences();
        initComponents();
    }

    /**
     * Returns the current table.
     *
     * @return the current statistics table
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
        collapseAdjacentCB = new JCheckBox();
        collapseAdjacentCB.setSelected(collapseValues);
        mediaDurObservationCB = new JCheckBox();
        mediaDurObservationCB.setSelected(useMediaDuration);
        tierSelectionPanel = new JPanel(new GridBagLayout());

        //Statistics table components
        statPanel = new JPanel();
        statTable = new JTable();
        statTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        statTable.setEnabled(false);
        //Initializing table
        setStatTable();
        statPane = new JScrollPane(statTable);

        Dimension size = new Dimension(500, 100);
        //	statPane.setMinimumSize(size);
        statPane.setPreferredSize(size);

        updateLocale();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

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

        Insets insets2 = new Insets(0, 6, 0, 6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets2;
        tierSelectionPanel.add(rootTiersOnlyCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets2;
        tierSelectionPanel.add(collapseAdjacentCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets2;
        tierSelectionPanel.add(mediaDurObservationCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(tierSelectionPanel, gridBagConstraints);

        //Setting statistics panel
        //Dimension tpd = new Dimension(550, 200);
        //statPanel.setMinimumSize(tpd);
        //statPanel.setPreferredSize(tpd);
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 10.0;
        add(statPanel, gridBagConstraints);
        rootTiersOnlyCB.addActionListener(this);
        collapseAdjacentCB.addActionListener(this);
        mediaDurObservationCB.addActionListener(this);
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
        collapseAdjacentCB.setText(ElanLocale.getString(
                "Statistics.Label.Collapse"));
        mediaDurObservationCB.setText(ElanLocale.getString(
                "Statistics.Label.MediaDuration"));
        statPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "Statistics.Pane.Table")));
        repaint();
    }

    /**
     * Applies localized strings for statistics table header.
     */
    private void updateLocalsForVariables() {
        annotations = ElanLocale.getString("Frame.GridFrame.ColumnAnnotation");
        occurrences = ElanLocale.getString("Statistics.Occurrences");
        frequency = ElanLocale.getString("Statistics.Frequency");
        averageDuration = ElanLocale.getString("Statistics.AverageDuration");
        timeRatio = ElanLocale.getString("Statistics.TimeRatio");
        latency = ElanLocale.getString("Statistics.Latency");
    }

    /**
     * Builds statistics table for current selected tier.
     */
    private void setStatTable() {
        TierImpl tier = (TierImpl) transcription.getTierWithId(curTier);
        setTimeBoundaries();

        DefaultTableModel statTableModel = getStatistics(tier);
        statTable.setModel(statTableModel);
        statTable.getTableHeader()
                 .addMouseMotionListener(new TableHeaderToolTipAdapter(
                statTable.getTableHeader()));
        
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(statTableModel);
        IntStringComparator<String> intComp = new IntStringComparator<String>();
        rowSorter.setComparator(1, intComp);
        
        FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
        for (int i = 2; i < 6; i++) {
        	rowSorter.setComparator(i, fsComp);
        }
        
        statTable.setRowSorter(rowSorter);
    }

    /**
     * Create table model with statistics for a tier
     * Note: any changes in the number or order of columns hve to be taken into account
     * where the row sorter and column comparators are set.
     * 
     * @param tier the tier to calculate statistics for
     *
     * @return a table model
     */
    private DefaultTableModel getStatistics(TierImpl tier) {
        DefaultTableModel statTableModel = null;

        //Table headers
        String[] tableHeader = new String[6];
        updateLocalsForVariables();
        tableHeader[0] = annotations;
        tableHeader[1] = occurrences;
        tableHeader[2] = frequency;
        tableHeader[3] = averageDuration;
        tableHeader[4] = timeRatio;
        tableHeader[5] = latency;

        Set<String> annValues = getAnnotationValues(tier);
        String[][] tableStat;

        //if no annotations at all
        if ((null == annValues) || (annValues.size() == 0)) {
            tableStat = new String[0][6];
        } else {
            tableStat = new String[annValues.size()][6];

            //Fills the first columns with annotations' values 
            Iterator<String> itAnnValues = annValues.iterator();
            int r = 0;

            while (itAnnValues.hasNext()) {
                tableStat[r][0] = itAnnValues.next();
                r++;
            }

            //Occurrences
            for (int i = 0; i < annValues.size(); i++)
                tableStat[i][1] = "" + getOccurrences(tier, tableStat[i][0]);

            //Frequency
            for (int i = 0; i < annValues.size(); i++)
                tableStat[i][2] = "" + getFrequency(tier, tableStat[i][0]);

            //Average Duration
            for (int i = 0; i < annValues.size(); i++)
                tableStat[i][3] = "" +
                    getAverageDuration(tier, tableStat[i][0]);

            //Time Ratio
            for (int i = 0; i < annValues.size(); i++)
                tableStat[i][4] = "" + getTimeRatio(tier, tableStat[i][0]);

            //Latency
            for (int i = 0; i < annValues.size(); i++) {
                double latency = getLatency(tier, tableStat[i][0]);

                if (latency == (((double) (totalDuration - beginBoundary)) / 1000)) {
                    tableStat[i][5] = "-";
                } else {
                    tableStat[i][5] = "" + latency;
                }
            }
        }

        statTableModel = new DefaultTableModel(tableStat, tableHeader);

        return statTableModel;
    }

    /**
     * Returns the set of different annotation values for a tier
     *
     * @param tier the tier to get annotation values from
     *
     * @return a treeset of annotations
     */
    private Set<String> getAnnotationValues(TierImpl tier) {
        TreeSet<String> annValues = null;

        if ((null == tier) || (null == tier.getLinguisticType())) {
            return annValues;
        }

        //If the tier is using controlled vocabulary
        //returns all the vocabulary entries
        if (tier.getLinguisticType().isUsingControlledVocabulary()) {
            String CVname = tier.getLinguisticType()
                                .getControlledVocabularyName();
            ControlledVocabulary CV = transcription.getControlledVocabulary(CVname);

            if (CV != null) {
            	// Get all words in all languages
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
        //If controlled vocabulary is not used, scans the annotations
        //and retrieves values
        else {
            List<AbstractAnnotation> annotations = tier.getAnnotations();

            if ((annotations != null) && (annotations.size() > 0)) {
                annValues = new TreeSet<String>();

                Iterator<AbstractAnnotation> annosIt = annotations.iterator();

                while (annosIt.hasNext()) {
                    AbstractAnnotation ann = annosIt.next();

                    if ((ann.getValue() != null)) {
                        annValues.add(ann.getValue());
                    }
                }
            }
        }

        return annValues;
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
                tier = tierIt.next();

                if (tier.getLinguisticType().getConstraints() == null) {
                    tierComboBox.addItem(tier.getName());
                    rootTiers.add(tier);
                }
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
                tier = tierIt.next();
                tierComboBox.addItem(tier.getName());
                rootTiers.add(tier);
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
     * Computes the number of occurrences. Contiguous annotations holding the
     * same value account for  only one occurrence. <br>Mar 2009: a flag is
     * introduced to change the counting behavior
     *
     * @param tier tier, the annotation value
     * @param annValue the value of the annotation
     *
     * @return the number of occurrences
     */
    private long getOccurrences(TierImpl tier, String annValue) {
        long occ = 0;
       List<AbstractAnnotation> annotations = tier.getAnnotations();

        if (annotations != null) {
            Collections.sort(annotations);

            for (int i = 0; i < annotations.size(); i++) {
                AbstractAnnotation ann = annotations.get(i);

                if (annValue.equals(ann.getValue())) {
                    if (!collapseValues) {
                        occ++;

                        continue;
                    }

                    AbstractAnnotation prevAnn = (AbstractAnnotation) tier.getAnnotationBefore(ann);

                    //first annotation
                    if (null == prevAnn) {
                        occ++;
                    }
                    //previous annotation is not contiguous to current
                    else if (prevAnn.getEndTimeBoundary() < ann.getBeginTimeBoundary()) {
                        occ++;
                    }
                    //previous annotation is contiguous to current
                    //but values are different
                    else if (!annValue.equals(prevAnn.getValue())) {
                        occ++;
                    }
                }
            }
        }

        return occ;
    }

    /**
     * Sets the observation time boundary: - the begining boundary equals the
     * lower time boundary  of the first annotation in the whole transcription
     * - the ending boundary equals the upper time boundary  of the last
     * annotation in the whole transcription Mar 09: added the option to use
     * the mediaduration as the observation period
     */
    private void setTimeBoundaries() {
        if (useMediaDuration) {
            beginBoundary = 0;
            endBoundary = totalDuration;

            return;
        }

        beginBoundary = totalDuration;
        endBoundary = 0;

        Iterator<TierImpl> itRootTier = rootTiers.iterator();

        while (itRootTier.hasNext()) {
            TierImpl tier = itRootTier.next();
            List<AbstractAnnotation> annotations = tier.getAnnotations();

            if (annotations != null) {
                Iterator<AbstractAnnotation> itAnnos = annotations.iterator();

                while (itAnnos.hasNext()) {
                    AbstractAnnotation ann = itAnnos.next();
                    beginBoundary = Math.min(beginBoundary,
                            ann.getBeginTimeBoundary());
                    endBoundary = Math.max(endBoundary, ann.getEndTimeBoundary());
                }
            }
        }

        ClientLogger.LOG.info("Observation beginning boundary = " + beginBoundary +
            ", observation ending boundary = " + endBoundary);
    }

    /**
     * Computes the total observation period
     *
     * @return the duration in seconds
     */
    private double getObservationTime() {
        //is in seconds
        return (double) (endBoundary - beginBoundary) / 1000;
    }

    /**
     * Computes the frequency defined as the number of occurrences divided by
     * the total observation period
     *
     * @param tier the tier
     * @param annValue the annotation value
     *
     * @return the frequency
     */
    private double getFrequency(TierImpl tier, String annValue) {
        double freq = 0;
        long occ = getOccurrences(tier, annValue);

        // frequency is the number of occurrences divided
        // by the total duration of observation
        if (getObservationTime() > 0) {
            freq = (double) occ / getObservationTime();
        }

        return freq;
    }

    /**
     * Computes the duration for an annotation value, which is the sum of the
     * durations of all annotations holding this value
     *
     * @param tier the tier
     * @param annValue the annotation value
     *
     * @return the sum of the durations (in seconds)
     */
    private double getAnnValueDuration(TierImpl tier, String annValue) {
        double valDuration = 0;
        List<AbstractAnnotation> annotations = tier.getAnnotations();

        if (annotations != null) {
            Iterator<AbstractAnnotation> itAnnos = annotations.iterator();

            while (itAnnos.hasNext()) {
                AbstractAnnotation ann = itAnnos.next();

                if (annValue.equals(ann.getValue())) {
                    double annDur = ann.getEndTimeBoundary() -
                        ann.getBeginTimeBoundary();
                    valDuration = valDuration + annDur;
                }
            }
        }

        //result in seconds
        return valDuration / 1000;
    }

    /**
     * Computes the average duration for an annotation value, defined as the
     * duration for the annotation value divided by the number of occurrences
     *
     * @param tier tier
     * @param annValue the annotation value
     *
     * @return the average duration in seconds
     */
    private double getAverageDuration(TierImpl tier, String annValue) {
        double avDuration = 0;

        if (getOccurrences(tier, annValue) > 0) {
            avDuration = getAnnValueDuration(tier, annValue) / (double) getOccurrences(tier,
                    annValue);
        }

        return avDuration;
    }

    /**
     * Computes the time ratio for an annotation value, defined as the duration
     * for the annotation value divided by total observation period
     *
     * @param tier the tier
     * @param annValue the annotation value
     *
     * @return the ratio of this value (value duration / observation time)
     *
     * @see #setTimeBoundaries()
     */
    private double getTimeRatio(TierImpl tier, String annValue) {
        double timeRatio = 0;

        if (getObservationTime() > 0) {
            timeRatio = getAnnValueDuration(tier, annValue) / getObservationTime();
        }

        return timeRatio;
    }

    /**
     * Computes the latency for an annotation value, defined as the length of
     * time between the begining of the observation period and the first
     * occurrence of the value
     *
     * @param tier tier
     * @param annValue the annotation value
     *
     * @return the latency
     */
    private double getLatency(TierImpl tier, String annValue) {
        double latency = totalDuration - beginBoundary;
        List<AbstractAnnotation> annotations = tier.getAnnotations();

        if (annotations != null) {
            Iterator<AbstractAnnotation> itAnnos = annotations.iterator();

            while (itAnnos.hasNext()) {
                AbstractAnnotation ann = itAnnos.next();

                if (annValue.equals(ann.getValue())) {
                    latency = Math.min(latency,
                            ann.getBeginTimeBoundary() - beginBoundary);
                }
            }
        }

        //result in seconds
        return latency / 1000;
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
            setStatTable();
            repaint();
        }
    }

    /**
     * Event handling for the checkboxes.
     *
     * @param e DOCUMENT ME!
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
                setStatTable();
                repaint();
            }
        } else if (e.getSource() == collapseAdjacentCB) {
            collapseValues = collapseAdjacentCB.isSelected();
            setStatTable();
            repaint();
        } else if (e.getSource() == mediaDurObservationCB) {
            useMediaDuration = mediaDurObservationCB.isSelected();
            setStatTable();
            repaint();
        }
        savePreferences();
    }

    /**
     * Reads stored preferences.
     */
    private void readPreferences() {
        Boolean flag = Preferences.getBool("Statistics.Annotation.RootTiersOnly",
                null);

        if (flag != null) {
            rootsOnly = flag.booleanValue();
        }

        flag = Preferences.getBool("Statistics.Annotation.CollapseContiguous",
                null);

        if (flag != null) {
            collapseValues = flag.booleanValue();
        }

        flag = Preferences.getBool("Statistics.Annotation.MediaObservationTime",
                null);

        if (flag != null) {
            useMediaDuration = flag.booleanValue();
        }
    }

    /**
     * Stores the current preferences.
     *
     */
    private void savePreferences() {
        Preferences.set("Statistics.Annotation.RootTiersOnly",
        	Boolean.valueOf(rootTiersOnlyCB.isSelected()), null, false, false);
        Preferences.set("Statistics.Annotation.CollapseContiguous",
        	Boolean.valueOf(collapseAdjacentCB.isSelected()), null, false, false);
        Preferences.set("Statistics.Annotation.MediaObservationTime",
        	Boolean.valueOf(mediaDurObservationCB.isSelected()), null, false, false);
    }
}
