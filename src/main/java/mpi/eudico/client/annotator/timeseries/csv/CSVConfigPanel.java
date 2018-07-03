package mpi.eudico.client.annotator.timeseries.csv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.NonContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesChangeEvent;
import mpi.eudico.client.annotator.timeseries.TimeSeriesChangeListener;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.timeseries.TrackTableModel;
import mpi.eudico.client.annotator.timeseries.config.SamplePosition;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.timeseries.spi.TSConfigPanel;
import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * A panel providing ui elements to configure tracks from a csv file. It allows
 * the user to specify which column contains the time information  and whether
 * the data is continuous rate or non-continuous.  Apart from the name and
 * description of each track, the column of the data can be specified, it's
 * range, derivative level, unit string and the color to use.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CSVConfigPanel extends TSConfigPanel implements ActionListener,
    ItemListener, ChangeListener, ListSelectionListener, ClientLogger {
    /** the add mode */
    public static final int ADD = 0;

    /** the change mode */
    public static final int CHANGE = 1;

    /** the delete mode */
    public static final int DELETE = 2;
    private TSSourceConfiguration config;
    private List<TimeSeriesChangeListener> listeners;
    private TSTrackConfiguration currentTrack;
    private List<TSTrackConfiguration> tracks;
    private int mode = ADD;
    private CSVReader reader;

    // ui components
    private JLabel titleLabel;
    private JLabel sourceLabel;
    private JPanel tablePanel;
    private JTable trackTable;
    private JTabbedPane tabPane;
    private TrackTableModel model;
    private JTable sampleTable;
    private JPanel examplePanel;
    private JLabel timeColLabel;
    private JSpinner timeColSpinner;
    private SpinnerNumberModel timeColSpinModel;
    private JCheckBox continuousCB;

    //  ui elements for edit panel
    private JPanel editPanel;
    private JLabel selectTrackLabel;
    private JComboBox currentTracksCB;
    private JLabel trackNameLabel;
    private JTextField trackNameTF;
    private JLabel trackDescLabel;
    private JTextField trackDescTF;
    private JLabel columnLabel;
    private JSpinner colSpinner;
    private SpinnerNumberModel colSpinModel;
    private JLabel rangeLabel;
    private JPanel rangePanel;
    private JRadioButton calcRangeRB;
    private JRadioButton manRangeRB;
    private JLabel minLabel;
    private JFormattedTextField minTF;
    private JLabel maxLabel;
    private JFormattedTextField maxTF;
    private JLabel deriveLabel;
    private JPanel derivePanel;
    private JRadioButton deriv0RB;
    private JRadioButton deriv1RB;
    private JRadioButton deriv2RB;
    private JRadioButton deriv3RB;
    private JLabel unitLabel;
    private JTextField unitTF;
    private JLabel colorLabel;
    private JButton colorButton;

    // buttonpanel
    private JButton changeButton;
    private JPanel buttonPanel;

    /**
     * Creates a new CSVConfigPanel instance
     */
    public CSVConfigPanel() {
        super();
        tracks = new ArrayList<TSTrackConfiguration>();
        initComponents();
        updateLocale();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sourceLabel = new JLabel();
        sourceLabel.setFont(titleLabel.getFont().deriveFont((float) 10));
        sourceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        examplePanel = new JPanel(new GridBagLayout());
        sampleTable = new JTable();

        JScrollPane tableScrollPane = new JScrollPane(sampleTable);
        Dimension size = new Dimension(500, 100);
        tableScrollPane.setMinimumSize(size);
        tableScrollPane.setPreferredSize(size);
        timeColLabel = new JLabel();
        timeColSpinModel = new SpinnerNumberModel();
        timeColSpinModel.setMinimum(Integer.valueOf(1)); // 1 based
        timeColSpinModel.setMaximum(Integer.valueOf(1000)); // estimation of max.
        timeColSpinModel.setValue(timeColSpinModel.getMinimum());
        timeColSpinner = new JSpinner(timeColSpinModel);
        continuousCB = new JCheckBox();
        continuousCB.setSelected(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        examplePanel.add(tableScrollPane, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        examplePanel.add(timeColLabel, gbc);
        gbc.gridx = 1;
        examplePanel.add(timeColSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        examplePanel.add(continuousCB, gbc);

        tablePanel = new JPanel();
        tablePanel.setLayout(new GridBagLayout());
        model = new TrackTableModel();
        trackTable = new JTable(model);
        trackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackTable.getSelectionModel().addListSelectionListener(this);

        tableScrollPane = new JScrollPane(trackTable);
        tableScrollPane.setMinimumSize(size);
        tableScrollPane.setPreferredSize(size);

        tabPane = new JTabbedPane();

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = insets;
        add(sourceLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(examplePanel, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tablePanel.add(tableScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(tablePanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 6, 6, 6);
        gbc.weightx = 1.0;
        //gbc.weighty = 1.0;
        add(tabPane, gbc);

        // editpanel
        editPanel = new JPanel(new GridBagLayout());
        selectTrackLabel = new JLabel();
        currentTracksCB = new JComboBox();
        currentTracksCB.addItemListener(this);
        trackNameLabel = new JLabel();
        trackNameTF = new JTextField();
        trackDescLabel = new JLabel();
        trackDescTF = new JTextField();

        columnLabel = new JLabel();
        colSpinModel = new SpinnerNumberModel();
        colSpinModel.setMinimum(Integer.valueOf(1));
        colSpinModel.setMaximum(Integer.valueOf(1000));
        colSpinModel.setValue(colSpinModel.getMinimum());
        colSpinner = new JSpinner(colSpinModel);

        rangeLabel = new JLabel();
        rangePanel = new JPanel(new GridBagLayout());
        calcRangeRB = new JRadioButton();
        calcRangeRB.addChangeListener(this);
        manRangeRB = new JRadioButton();
        manRangeRB.setSelected(true);
        manRangeRB.addChangeListener(this);
        minLabel = new JLabel();

        NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);
        format.setParseIntegerOnly(false);
        minTF = new JFormattedTextField(format);
        maxLabel = new JLabel();
        maxTF = new JFormattedTextField(format);
        deriveLabel = new JLabel();
        derivePanel = new JPanel(new GridLayout(1, 4));
        deriv0RB = new JRadioButton("0");
        deriv0RB.setSelected(true);
        deriv1RB = new JRadioButton("1");
        deriv2RB = new JRadioButton("2");
        deriv3RB = new JRadioButton("3");
        unitLabel = new JLabel();
        unitTF = new JTextField(10);
        colorLabel = new JLabel();
        colorButton = new JButton();
        colorButton.setBackground(Color.GREEN);
        colorButton.addActionListener(this);

        // configure sub panels
        GridBagConstraints subgbc = new GridBagConstraints();
        subgbc.gridx = 0;
        subgbc.gridwidth = 2;
        subgbc.anchor = GridBagConstraints.NORTHWEST;
        subgbc.insets = insets;

        rangePanel.setBorder(new TitledBorder(""));

        ButtonGroup group3 = new ButtonGroup();
        group3.add(manRangeRB);
        group3.add(calcRangeRB);
        subgbc.gridy = 0;
        subgbc.gridwidth = 2;
        rangePanel.add(calcRangeRB, subgbc);
        subgbc.gridy = 1;
        rangePanel.add(manRangeRB, subgbc);
        subgbc.gridy = 2;
        subgbc.gridwidth = 1;
        subgbc.fill = GridBagConstraints.NONE;
        subgbc.weightx = 0.0;
        rangePanel.add(minLabel, subgbc);
        subgbc.gridy = 3;
        rangePanel.add(maxLabel, subgbc);
        subgbc.gridy = 2;
        subgbc.gridx = 1;
        subgbc.fill = GridBagConstraints.HORIZONTAL;
        subgbc.weightx = 0.5;
        subgbc.gridwidth = 1;
        rangePanel.add(minTF, subgbc);
        subgbc.gridy = 3;
        rangePanel.add(maxTF, subgbc);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(deriv0RB);
        group2.add(deriv1RB);
        group2.add(deriv2RB);
        group2.add(deriv3RB);

        // end subpanels
        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.anchor = GridBagConstraints.NORTHWEST;
        lgbc.insets = insets;
        editPanel.add(selectTrackLabel, lgbc);

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 1;
        rgbc.gridwidth = 4;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        rgbc.anchor = GridBagConstraints.NORTHWEST;
        rgbc.insets = insets;
        rgbc.weightx = 1.0;
        editPanel.add(currentTracksCB, rgbc);
        lgbc.gridy = 1;
        editPanel.add(trackNameLabel, lgbc);
        rgbc.gridy = 1;
        editPanel.add(trackNameTF, rgbc);
        lgbc.gridy = 2;
        editPanel.add(trackDescLabel, lgbc);
        rgbc.gridy = 2;
        editPanel.add(trackDescTF, rgbc);
        lgbc.gridy = 3;
        editPanel.add(columnLabel, lgbc);
        rgbc.gridy = 3;
        rgbc.gridwidth = 2;
        rgbc.fill = GridBagConstraints.NONE;
        editPanel.add(colSpinner, rgbc);

        lgbc.gridy = 4;
        editPanel.add(rangeLabel, lgbc);
        rgbc.gridx = 1;
        rgbc.gridwidth = 4;
        rgbc.gridy = 4;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(rangePanel, rgbc);
        lgbc.gridy = 5;
        editPanel.add(deriveLabel, lgbc);
        rgbc.gridy = 5;
        derivePanel.add(deriv0RB);
        derivePanel.add(deriv1RB);
        derivePanel.add(deriv2RB);
        derivePanel.add(deriv3RB);
        rgbc.gridwidth = 4;
        editPanel.add(derivePanel, rgbc);

        lgbc.gridy = 6;
        editPanel.add(unitLabel, lgbc);
        rgbc.gridy = 6;
        rgbc.gridx = 1;
        rgbc.gridwidth = 1;
        rgbc.fill = GridBagConstraints.NONE;
        editPanel.add(unitTF, rgbc);
        lgbc.gridy = 7;
        editPanel.add(colorLabel, lgbc);
        rgbc.gridy = 7;
        editPanel.add(colorButton, rgbc);

        // button
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(new TitledBorder(""));
        changeButton = new JButton();
        changeButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = insets;
        buttonPanel.add(changeButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        editPanel.add(buttonPanel, gbc);
        // tabpane
        tabPane.addTab(ElanLocale.getString("Button.Add"), null);
        tabPane.addTab(ElanLocale.getString("Button.Change"), null);
        tabPane.addTab(ElanLocale.getString("Button.Delete"), null);
        tabPane.setComponentAt(0, editPanel);
        tabPane.setSelectedIndex(0);
        tabPane.addChangeListener(this);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        examplePanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "TimeSeriesViewer.Config.SampleTable")));
        timeColLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.TimeColumn"));
        continuousCB.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.ContinuousRate"));
        tablePanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "TimeSeriesViewer.Config.CurrentTracks")));
        titleLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.Tracks.Title"));
        selectTrackLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.SelectTrack"));
        trackNameLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.TrackName"));
        trackDescLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.TrackDesc"));
        columnLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.SampleColumn"));
        rangeLabel.setText(ElanLocale.getString("TimeSeriesViewer.Config.Range"));
        calcRangeRB.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.RangeCalc"));
        manRangeRB.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.RangeManual"));
        minLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.RangeMinimum"));
        maxLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.RangeMaximum"));
        deriveLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.Derivative"));
        unitLabel.setText(ElanLocale.getString("TimeSeriesViewer.Config.Units"));
        colorLabel.setText(ElanLocale.getString(
                "TimeSeriesViewer.Config.TrackColor"));
        colorButton.setText("...");
        changeButton.setText(ElanLocale.getString("Button.Add"));
    }

    /**
     * Updates texts and enables/disables components for the current  edit
     * mode.
     */
    private void updateForMode() {
        switch (mode) {
        case ADD:
            currentTracksCB.setEnabled(false);
            trackNameTF.setEnabled(true);
            trackNameTF.setEditable(true);
            trackDescTF.setEnabled(true);
            trackDescTF.setEditable(true);
            colSpinner.setEnabled(true);
            calcRangeRB.setEnabled(true);
            manRangeRB.setEnabled(true);

            if (manRangeRB.isSelected()) {
                minTF.setEnabled(true);
                minTF.setEditable(true);
                maxTF.setEnabled(true);
                maxTF.setEditable(true);
            }

            deriv0RB.setEnabled(true);
            deriv1RB.setEnabled(true);
            deriv2RB.setEnabled(true);
            deriv3RB.setEnabled(true);
            unitTF.setEnabled(true);
            unitTF.setEditable(true);
            colorButton.setEnabled(true);
            changeButton.setText(ElanLocale.getString("Button.Add"));

            break;

        case CHANGE:
            currentTracksCB.setEnabled(true);
            trackNameTF.setEnabled(true);
            trackNameTF.setEditable(true);
            trackDescTF.setEnabled(true);
            trackDescTF.setEditable(true);
            colSpinner.setEnabled(false);
            calcRangeRB.setEnabled(true);
            manRangeRB.setEnabled(true);

            if (manRangeRB.isSelected()) {
                minTF.setEnabled(true);
                minTF.setEditable(true);
                maxTF.setEnabled(true);
                maxTF.setEditable(true);
            }

            deriv0RB.setEnabled(false);
            deriv1RB.setEnabled(false);
            deriv2RB.setEnabled(false);
            deriv3RB.setEnabled(false);
            unitTF.setEnabled(true);
            unitTF.setEditable(true);
            colorButton.setEnabled(true);
            changeButton.setText(ElanLocale.getString("Button.Change"));

            break;

        case DELETE:
            currentTracksCB.setEnabled(true);
            trackNameTF.setEnabled(false);
            trackNameTF.setEditable(false);
            trackDescTF.setEnabled(false);
            trackDescTF.setEditable(false);
            colSpinner.setEnabled(false);
            calcRangeRB.setEnabled(false);
            manRangeRB.setEnabled(false);
            minTF.setEnabled(false);
            minTF.setEditable(false);
            maxTF.setEnabled(false);
            maxTF.setEditable(false);
            deriv0RB.setEnabled(false);
            deriv1RB.setEnabled(false);
            deriv2RB.setEnabled(false);
            deriv3RB.setEnabled(false);
            unitTF.setEnabled(false);
            unitTF.setEditable(false);
            colorButton.setEnabled(false);
            changeButton.setText(ElanLocale.getString("Button.Delete"));

            break;
        }
    }

    private void updateForTrack(String trackName) {
        if (trackName == null) {
            // empty the fields
            trackNameTF.setText("");
            trackDescTF.setText("");
            unitTF.setText("");
            colSpinner.setValue(colSpinModel.getMinimum());
            minTF.setValue(new Float(0));
            maxTF.setValue(new Float(100));
            deriv0RB.setSelected(true);
            colorButton.setBackground(Color.GREEN);
            trackTable.getSelectionModel().clearSelection();

            return;
        }

        //get current track
        TSTrackConfiguration tst;

        for (int i = 0; i < tracks.size(); i++) {
            tst = tracks.get(i);

            if (tst.getTrackName().equals(trackName)) {
                currentTrack = tst;

                break;
            }
        }

        if (currentTrack != null) {
            AbstractTSTrack track = (AbstractTSTrack) currentTrack.getObject(currentTrack.getTrackName());

            if (currentTracksCB.getSelectedItem() != currentTrack.getTrackName()) {
                currentTracksCB.setSelectedItem(currentTrack.getTrackName());
            }

            String tmp;
            trackNameTF.setText(currentTrack.getTrackName());

            // info from track
            if (track != null) {
                tmp = track.getDescription();

                if (tmp != null) {
                    trackDescTF.setText(tmp);
                } else {
                    trackDescTF.setText("");
                }

                tmp = track.getUnitString();

                if (tmp != null) {
                    unitTF.setText(tmp);
                } else {
                    unitTF.setText("");
                }

                if ("true".equals(currentTrack.getProperty(
                                TimeSeriesConstants.AUTO_DETECT_RANGE))) {
                    calcRangeRB.setSelected(true);
                    minTF.setEnabled(false);
                    minTF.setEditable(false);
                    maxTF.setEnabled(false);
                    maxTF.setEditable(false);
                } else {
                    manRangeRB.setSelected(true);
                    minTF.setEnabled(true);
                    minTF.setEditable(true);
                    maxTF.setEnabled(true);
                    maxTF.setEditable(true);
                }

                float[] range = track.getRange();

                if (range != null) {
                    minTF.setValue(new Float(range[0]));
                    maxTF.setValue(new Float(range[1]));
                } else {
                    minTF.setValue(new Float(0));
                    maxTF.setValue(new Float(100));
                }

                int deriv = track.getDerivativeLevel();

                switch (deriv) {
                case 1:
                    deriv1RB.setSelected(true);

                    break;

                case 2:
                    deriv2RB.setSelected(true);

                    break;

                case 3:
                    deriv3RB.setSelected(true);

                    break;

                default:
                    deriv0RB.setSelected(true);
                }

                Color c = track.getColor();

                if (c != null) {
                    colorButton.setBackground(c);
                } else {
                    colorButton.setBackground(Color.GREEN);
                }
            } else {
                trackDescTF.setText("");
                unitTF.setText("");
                deriv0RB.setSelected(true);
                colorButton.setBackground(Color.GREEN);
            }

            SamplePosition spos = currentTrack.getSamplePos();

            if (spos != null) {
                int[] cols = spos.getColumns();

                if (cols.length >= 1) {
                    colSpinner.setValue(Integer.valueOf(cols[0] + 1)); // from 0 based to 1 based
                } else {
                    colSpinner.setValue(colSpinModel.getMinimum());
                }
            }

            // update table
            if (model != null) {
                int col = model.findColumn(TrackTableModel.NAME);
                trackTable.getSelectionModel().removeListSelectionListener(this);

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (currentTrack.getTrackName()
                                        .equals(model.getValueAt(i, col))) {
                        trackTable.getSelectionModel().setLeadSelectionIndex(i);

                        break;
                    }
                }

                trackTable.getSelectionModel().addListSelectionListener(this);
            }
        }
    }

    /**
     * Updates table and combo boxes with the information of the current
     * tracks.
     */
    private void reextractTracks() {
        currentTracksCB.removeItemListener(this);
        currentTracksCB.removeAllItems();

        model.removeAllRows();

        TSTrackConfiguration tr;

        for (int i = 0; i < tracks.size(); i++) {
            tr = tracks.get(i);
            currentTracksCB.addItem(tr.getTrackName());
            model.addRow(tr);
        }

        currentTracksCB.addItemListener(this);
    }

    /**
     * Notifies registered listeners of a TimeSeries change event.
     *
     * @param ev the event
     */
    private void notifyListeners(TimeSeriesChangeEvent ev) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).timeSeriesChanged(ev);
            }
        }
    }

    /**
     * Sets the reader object that is used to extract and visualize a portion
     * of the  data file.
     *
     * @param reader the reader object
     */
    public void setReader(CSVReader reader) {
        this.reader = reader;
        initSampleTable();
    }

    private void initSampleTable() {
        if ((reader != null) && reader.isValidFile()) {
            try {
                int detectedNumCols = reader.getNumColumns();

                if (detectedNumCols <= 1) {
                    return;
                }

                List<String[]> rows = reader.getSamples(5);
                DefaultTableModel csvModel = new DefaultTableModel(rows.size(),
                        detectedNumCols);
                String[] row;

                for (int i = 0; i < rows.size(); i++) {
                    row = rows.get(i);

                    for (int j = 0; (j < row.length) && (j < detectedNumCols);
                            j++) {
                        csvModel.setValueAt(row[j], i, j);
                    }

                    if (row.length < detectedNumCols) {
                        for (int j = 0; j < (detectedNumCols - row.length);
                                j++) {
                            csvModel.setValueAt("", i, row.length + j);
                        }
                    }
                }

                String[] headers = new String[detectedNumCols];

                for (int i = 0; i < detectedNumCols; i++) {
                    headers[i] = String.valueOf(i + 1);
                }

                csvModel.setColumnIdentifiers(headers);
                sampleTable.setModel(csvModel);
                sampleTable.getTableHeader().setReorderingAllowed(false);
                sampleTable.getColumnModel().getColumn(0).sizeWidthToFit();
            } catch (FileNotFoundException fnfe) {
                LOG.warning("File not found: ");

                // warning
            } catch (IOException ioe) {
                LOG.warning("Read error: " + ioe.getMessage());

                // warning
            }
        }
    }

    /**
     * Adds a listener to changes in the configuration.
     *
     * @param li the listener
     */
    @Override
	public void addTimeSeriesChangeListener(TimeSeriesChangeListener li) {
        if (listeners == null) {
            listeners = new ArrayList<TimeSeriesChangeListener>();
        }

        listeners.add(li);
    }

    private void selectColor() {
        Color newColor = JColorChooser.showDialog(this,
                ElanLocale.getString("TimeSeriesViewer.Config.TrackColor"),
                colorButton.getBackground());
        colorButton.setBackground(newColor);
    }

    /**
     * Creates a track; gets the data for the column and derivative  from the
     * file reader and initializes a continuous or noncontinuos rate track.
     *
     * @param timeCol the column containing the time values
     * @param dataCol the sample column
     * @param derLevel derivative
     * @param continuousRate if true the track is a continuous rate track (must
     *        match the file)
     *
     * @return the track or null
     */
    private AbstractTSTrack createTrack(int timeCol, int dataCol, int derLevel,
        boolean continuousRate) {
        if (reader == null) {
            return null;
        }

        if (continuousRate) {
            ContinuousRateTSTrack track = new ContinuousRateTSTrack();

            try {
                track.setData(reader.readContinuousRateTrack(timeCol, dataCol, derLevel));
                track.setSampleRate(reader.getSampleFrequency());
                track.setType(TimeSeriesTrack.VALUES_FLOAT_ARRAY);
            } catch (IOException ioe) {
                LOG.warning("Could not read data for track: " +
                    ioe.getMessage());
            }

            return track;
        } else {
            NonContinuousRateTSTrack track = new NonContinuousRateTSTrack();

            try {
                track.setData(reader.readNonContinuousRateTrack(timeCol, dataCol, derLevel));
                track.setType(TimeSeriesTrack.TIME_VALUE_LIST);
            } catch (IOException ioe) {
                LOG.warning("Could not read data for track: " +
                    ioe.getMessage());
            }

            return track;
        }
    }

    /**
     * Calculates the range (min - max) from the data in the track.
     *
     * @param track the track
     *
     * @return the range, min - max value
     */
    private float[] calculateRange(AbstractTSTrack track) {
        float[] range = new float[] { 0, 100 };

        if ((track != null) && track.getData() instanceof float[]) {
            float[] data = (float[]) track.getData();
            float min = Float.MAX_VALUE;
            float max = Integer.MIN_VALUE; // Float.MIN_VALUE doesn't compare well: data[i] > max always returns false

            for (float element : data) {
                if (element < min) {
                    min = element;
                }

                if (element > max) {
                    max = element;
                }
            }

            range[0] = min;
            range[1] = max;
        } else if ((track != null) && track.getData() instanceof List) {
            List dataList = (List) track.getData();

            if ((dataList.size() > 0) && dataList.get(0) instanceof TimeValue) {
                TimeValue tv = null;
                float min = Float.MAX_VALUE;
                float max = Integer.MIN_VALUE;// Float.MIN_VALUE doesn't compare well: data[i] > max always returns false

                for (int i = 0; i < dataList.size(); i++) {
                    tv = (TimeValue) dataList.get(i);

                    if (tv.value < min) {
                        min = tv.value;
                    }

                    if (tv.value > max) {
                        max = tv.value;
                    }
                }

                range[0] = min;
                range[1] = max;
            }
        }

        return range;
    }

    /**
     * Performs checks, creats a track(configuration) and adds it to the source
     * (configuration). Sends a notification to listeners.
     */
    private void doAdd() {
        String name = trackNameTF.getText();
        int timeCol = ((Integer) timeColSpinner.getValue()).intValue() - 1; // correct to make it 0 based
        boolean contRate = continuousCB.isSelected();
        int col = 0;
        int derLevel = 0;
        float min = 0;
        float max = 100;

        if (name != null) {
            name = name.trim();
        }

        if (name.length() == 0) {
            showWarningMessage(ElanLocale.getString(
                    "TimeSeriesViewer.Config.Message.NoName"));

            return;
        }

        TSTrackConfiguration tst;

        for (int i = 0; i < tracks.size(); i++) {
            tst = tracks.get(i);

            if (name.equals(tst.getTrackName())) {
                showWarningMessage(ElanLocale.getString(
                        "TimeSeriesViewer.Config.Message.NameExists"));

                return;
            }
        }

        // create new track
        TSTrackConfiguration nextTrack = new TSTrackConfiguration(name);

        col = ((Integer) colSpinner.getValue()).intValue() - 1;

        SamplePosition spos = new SamplePosition(new int[] { 0 },
                new int[] { col });
        nextTrack.setSamplePos(spos);

        if (calcRangeRB.isSelected()) {
            nextTrack.setProperty(TimeSeriesConstants.AUTO_DETECT_RANGE, "true");
        } else {
            Object minObj = minTF.getValue();
            Object maxObj = maxTF.getValue();

            if (minObj instanceof Double) {
                min = ((Double) minObj).floatValue();
            } else if (minObj instanceof Long) {
                min = ((Long) minObj).floatValue();
            } else if (minObj instanceof Float) {
                min = ((Float) minObj).floatValue();
            }

            if (maxObj instanceof Double) {
                max = ((Double) maxObj).floatValue();
            } else if (maxObj instanceof Long) {
                max = ((Long) maxObj).floatValue();
            } else if (maxObj instanceof Float) {
                max = ((Float) maxObj).floatValue();
            }

            nextTrack.setProperty(TimeSeriesConstants.AUTO_DETECT_RANGE, "false");
        }

        if (deriv1RB.isSelected()) {
            derLevel = 1;
        } else if (deriv2RB.isSelected()) {
            derLevel = 2;
        } else if (deriv3RB.isSelected()) {
            derLevel = 3;
        }

        AbstractTSTrack track = createTrack(timeCol, col, derLevel, contRate);

        if (track != null) {
            track.setColor(colorButton.getBackground());
            track.setDerivativeLevel(derLevel);
            track.setName(name);

            if (calcRangeRB.isSelected()) {
                track.setRange(calculateRange(track));
            } else {
                track.setRange(new float[] { min, max });
            }
            if (config != null) {
            	track.setTimeOffset(config.getTimeOrigin());
            }

            nextTrack.putObject(name, track);
        }

        if (trackDescTF.getText() != null) {
            track.setDescription(trackDescTF.getText().trim());
        }

        if (unitTF.getText() != null) {
            track.setUnitString(unitTF.getText().trim());
        }

        config.putObject(name, nextTrack);
        tracks.add(nextTrack);
        currentTracksCB.addItem(name);
        model.addRow(nextTrack);

        // these 2 settings are a bit strange at this position. When a single
        // track is created properties for the source file are set
        if (config.getTimeColumn() != timeCol) {
        	config.setTimeColumn(timeCol);
        }
        if (contRate) {
        	config.setSampleType(TimeSeriesConstants.CONTINUOUS_RATE);
        } else {
        	config.setSampleType(TimeSeriesConstants.DISCONTINUOUS_RATE);
        }
        
        notifyListeners(new TimeSeriesChangeEvent(nextTrack,
                TimeSeriesChangeEvent.ADD, TimeSeriesChangeEvent.TRACK));
        updateForTrack(name);
    }

    /**
     * Performs some checks, update the selected track (configuration) and
     * notifies listeners.
     */
    private void doChange() {
        if (currentTrack != null) {
            boolean changed = false;
            AbstractTSTrack track = (AbstractTSTrack) currentTrack.getObject(currentTrack.getTrackName());
            String name = trackNameTF.getText();

            if (name != null) {
                name = name.trim();
            }

            if (name.length() == 0) {
                showWarningMessage(ElanLocale.getString(
                        "TimeSeriesViewer.Config.Message.NoName"));

                return;
            }

            if (!name.equals(currentTrack.getTrackName())) {
                currentTrack.setTrackName(name);

                if (track != null) {
                    track.setName(name);
                }

                changed = true;
            }

            if (trackDescTF.getText() != null) {
                String desc = trackDescTF.getText().trim();

                if ((track != null) && !desc.equals(track.getDescription())) {
                    track.setDescription(desc);
                    changed = true;
                }
            }

            // skip the sample position, can not be changed
            if ("true".equals(currentTrack.getProperty(
                            TimeSeriesConstants.AUTO_DETECT_RANGE))) {
                if (!calcRangeRB.isSelected()) {
                    currentTrack.setProperty(TimeSeriesConstants.AUTO_DETECT_RANGE,
                        "false");
                    changed = true;
                }
            } else {
                if (calcRangeRB.isSelected()) {
                    currentTrack.setProperty(TimeSeriesConstants.AUTO_DETECT_RANGE,
                        "true");
                    track.setRange(calculateRange(track));
                    changed = true;
                }
            }

            if (!calcRangeRB.isSelected()&&(track != null) && (track.getRange() != null)) {
                float min = 0;
                float max = 100;
                Object minObj = minTF.getValue();
                Object maxObj = maxTF.getValue();

                if (minObj instanceof Double) {
                    min = ((Double) minObj).floatValue();
                } else if (minObj instanceof Long) {
                    min = ((Long) minObj).floatValue();
                } else if (minObj instanceof Float) {
                    min = ((Float) minObj).floatValue();
                }

                if (maxObj instanceof Double) {
                    max = ((Double) maxObj).floatValue();
                } else if (maxObj instanceof Long) {
                    max = ((Long) maxObj).floatValue();
                } else if (maxObj instanceof Float) {
                    max = ((Float) maxObj).floatValue();
                }

                if ((track.getRange()[0] != min) ||
                        (track.getRange()[1] != max)) {
                    track.setRange(new float[] { min, max });
                    changed = true;
                }
            }

            if (track != null) {
                if (unitTF.getText() != null) {
                    String units = unitTF.getText().trim();

                    if (!units.equals(track.getUnitString())) {
                        track.setUnitString(units);
                        changed = true;
                    }
                }

                if (track.getColor() != colorButton.getBackground()) {
                    track.setColor(colorButton.getBackground());
                    changed = true;
                }
            }

            if (changed) {
                notifyListeners(new TimeSeriesChangeEvent(currentTrack,
                        TimeSeriesChangeEvent.CHANGE,
                        TimeSeriesChangeEvent.TRACK));
                reextractTracks();
                updateForTrack(currentTrack.getTrackName());
            }
        }
    }

    /**
     * Removes a track from the source configuration, notifies listeners.
     */
    private void doDelete() {
        if (currentTrack != null) {
            tracks.remove(currentTrack);
            config.removeObject(currentTrack.getTrackName());
            reextractTracks();

            notifyListeners(new TimeSeriesChangeEvent(currentTrack,
                    TimeSeriesChangeEvent.DELETE, TimeSeriesChangeEvent.TRACK));

            if (currentTracksCB.getItemCount() > 0) {
                updateForTrack((String) currentTracksCB.getItemAt(0));
            } else {
                updateForTrack(null);
            }
        }
    }

    /**
     * Creates a warning message dialog with the specified message.
     *
     * @param message the message
     */
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Sets the source configuration with the path to the source file and
     * information  about the tracks that already have been defined.
     *
     * @param tssc the configuration
     */
    @Override
	public void setSourceConfiguration(TSSourceConfiguration tssc) {
        if (tssc == null) {
            return;
        }

        tracks.clear();
        config = tssc;
        sourceLabel.setText(config.getSource());

        //      update ui
        Iterator<Object> trit = config.objectKeySet().iterator();
        Object val;

        while (trit.hasNext()) {
            val = config.getObject(trit.next());

            if (val instanceof TSTrackConfiguration) {
                tracks.add((TSTrackConfiguration)val);
            }
        }

        // update table and current track box
        currentTracksCB.removeItemListener(this);
        currentTracksCB.removeAllItems();
        trackTable.getSelectionModel().removeListSelectionListener(this);
        model.removeAllRows();

        TSTrackConfiguration tr;

        for (int i = 0; i < tracks.size(); i++) {
            tr = tracks.get(i);
            currentTracksCB.addItem(tr.getTrackName());
            model.addRow(tr);
        }

        currentTracksCB.addItemListener(this);
        trackTable.getSelectionModel().addListSelectionListener(this);

        if (currentTracksCB.getItemCount() > 0) {
            updateForTrack((String) currentTracksCB.getSelectedItem());
        }
    }

    /**
     * The action performed handling.
     *
     * @param event action event
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == changeButton) {
            switch (mode) {
            case ADD:
                doAdd();

                break;

            case CHANGE:
                doChange();

                break;

            case DELETE:
                doDelete();
            }
        } else if (event.getSource() == colorButton) {
            selectColor();
        }
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tabPane) {
            mode = tabPane.getSelectedIndex();
            updateForMode();
        } else if (e.getSource() == calcRangeRB) {
        	// double check the selected state; on some systems hovering of the mouse
        	// seems to triggered an event
        	if (calcRangeRB.isSelected()){
	            minTF.setEnabled(false);
	            minTF.setEditable(false);
	            maxTF.setEnabled(false);
	            maxTF.setEditable(false);
            }
        } else if (e.getSource() == manRangeRB) {
        	if (manRangeRB.isSelected()) {
	            minTF.setEnabled(true);
	            minTF.setEditable(true);
	            maxTF.setEnabled(true);
	            maxTF.setEditable(true);
            }
        }
    }

    /**
     * ComboBox selection changes.
     *
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String trackName = (String) currentTracksCB.getSelectedItem();
            updateForTrack(trackName);
        }
    }

    /**
     * Update the ui after selection of a tier in the table (except for ADD
     * mode).
     *
     * @param e the list selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        int row = trackTable.getSelectedRow();

        if (row > -1) {
            int column = model.findColumn(TrackTableModel.NAME);
            updateForTrack((String) model.getValueAt(row, column));
        }
    }
}
