package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.SegmentsToTiersCommand;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.TableHeaderToolTipAdapter;
import mpi.eudico.server.corpora.clom.Transcription;


/**
 * A dialog that allows the user to configure how segmentations ate to be
 * converted to tiers and annotations.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SegmentsToTiersDialog extends ClosableDialog
    implements ActionListener, ItemListener, ListSelectionListener,
        ProgressListener {
    private Transcription transcription;
    private List<Segmentation> segmentations;
    private JTabbedPane tabPane;
    private JPanel simplePanel;
    private JPanel advancedPanel;
    private JLabel selectSegmentationLb;
    private JLabel selectSegmentsLb;
    private JComboBox allSegmentationsCombo;
    private JTable segmentsTable;
    private SegmentTableModel model;

    //private JCheckBox editCB;
    private JLabel selectSegmentationsLb;
    private JTable segmentationsTable;
    private DefaultTableModel dtModel;
    private JProgressBar progressBar;
    private SegmentsToTiersCommand com;

    //Button panel
    private JButton okButton;
    private JButton closeButton;
    private JPanel buttonPanel;

    /**
     * Constructor.
     *
     * @param transcription the transcription that is to be modified
     * @param segmentations the segmentations from a recognizer
     *
     * @throws HeadlessException he
     */
    public SegmentsToTiersDialog(Transcription transcription,
        List<Segmentation> segmentations) throws HeadlessException {
        super();
        this.transcription = transcription;
        this.segmentations = segmentations;
        initComponents();
        postInit();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param transcription the transcription that is to be modified
     * @param segmentations the segmentations from a recognizer
     *
     * @throws HeadlessException
     */
    public SegmentsToTiersDialog(Frame owner, Transcription transcription,
        List<Segmentation> segmentations) throws HeadlessException {
        super(owner, true);
        this.transcription = transcription;
        this.segmentations = segmentations;
        initComponents();
        extractSegmentations();
        postInit();
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("SegmentsToTierDialog.Title"));
        getContentPane().setLayout(new GridBagLayout());

        tabPane = new JTabbedPane();
        advancedPanel = new JPanel();
        tabPane.addTab(ElanLocale.getString(
                "SegmentsToTierDialog.Tab.PerSegmentation"), advancedPanel);
        simplePanel = new JPanel();
        tabPane.addTab(ElanLocale.getString("SegmentsToTierDialog.Tab.All"),
            simplePanel);
        tabPane.setSelectedComponent(advancedPanel);

        // fill the panels
        Insets insets = new Insets(2, 6, 2, 6);
        advancedPanel.setLayout(new GridBagLayout());
        selectSegmentationLb = new JLabel(ElanLocale.getString(
                    "SegmentsToTierDialog.Label.SelectSegmentation"));
        selectSegmentsLb = new JLabel(ElanLocale.getString(
                    "SegmentsToTierDialog.Label.ConfigureSegment"));
        allSegmentationsCombo = new JComboBox();

        model = new SegmentTableModel();
        segmentsTable = new JTable(model);
        //editCB = new JCheckBox();
        //editCB.addChangeListener(this);
        //segmentsTable.setDefaultEditor(Boolean.class,
        //    new DefaultCellEditor(editCB));
        //segmentsTable.setDefaultRenderer(Boolean.class,
        //    new CheckBoxTableCellRenderer());
        segmentsTable.getColumn(model.getColumnName(model.findColumn(
                    SegmentTableModel.INCLUDE))).setPreferredWidth(50);
        segmentsTable.getColumn(model.getColumnName(model.findColumn(
                    SegmentTableModel.NUMBER))).setPreferredWidth(50);
        segmentsTable.getTableHeader()
                     .addMouseMotionListener(new TableHeaderToolTipAdapter(
                segmentsTable.getTableHeader()));
        segmentsTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        segmentsTable.setShowGrid(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        advancedPanel.add(selectSegmentationLb, gbc);
        gbc.gridy++;
        advancedPanel.add(allSegmentationsCombo, gbc);
        gbc.gridy++;
        advancedPanel.add(selectSegmentsLb, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        JScrollPane scPane = new JScrollPane(segmentsTable);
        scPane.getViewport().setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        scPane.setPreferredSize(new Dimension(300, 180));
        advancedPanel.add(scPane, gbc);

        // simple panel
        simplePanel.setLayout(new GridBagLayout());
        selectSegmentationsLb = new JLabel(ElanLocale.getString(
                    "SegmentsToTierDialog.Label.SelectSegmentations"));
        dtModel = new DefaultTableModel() {
                    @Override
					public boolean isCellEditable(int row, int column) {
                        return column == 0;
                    }
                };
        dtModel.setColumnIdentifiers(new String[] {
                SegmentTableModel.INCLUDE, SegmentTableModel.LABEL
            });
        segmentationsTable = new JTable(dtModel);
        segmentationsTable.getColumn(SegmentTableModel.INCLUDE)
                          .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        segmentationsTable.getColumn(SegmentTableModel.INCLUDE)
                          .setCellRenderer(new CheckBoxTableCellRenderer());
        segmentationsTable.getColumn(SegmentTableModel.INCLUDE).setMaxWidth(30);
        segmentationsTable.setShowVerticalLines(false);
        segmentationsTable.setTableHeader(null);
        segmentationsTable.setDefaultEditor(String.class, null);
        segmentationsTable.getSelectionModel().addListSelectionListener(this);
        // add segmentations
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        simplePanel.add(selectSegmentationsLb, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        JScrollPane listScroll = new JScrollPane(segmentationsTable);
        listScroll.setPreferredSize(new Dimension(300, 180));
        simplePanel.add(listScroll, gbc);

        tabPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(tabPane, gbc);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(2, 10, 4, 10);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(progressBar, gbc);

        okButton = new JButton(ElanLocale.getString(
                    "SegmentsToTierDialog.Button.Create"));
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 2));

        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTH;
        getContentPane().add(buttonPanel, gbc);
        setModal(true);
        allSegmentationsCombo.addItemListener(this);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int minimalWidth = 520;
        int minimalHeight = 300;
        setSize((getSize().width < minimalWidth) ? minimalWidth : getSize().width,
            (getSize().height < minimalHeight) ? minimalHeight : getSize().height);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    /**
     * Puts the name of the segmentations in the table, by default all  rows
     * are selected.
     */
    private void extractSegmentations() {
        if ((segmentations != null) && (segmentations.size() > 0)) {
            Segmentation seg = null;
            String[] names = new String[segmentations.size()];

            for (int i = 0; i < segmentations.size(); i++) {
                seg = segmentations.get(i);
                names[i] = seg.getName();
                dtModel.addRow(new Object[] { Boolean.TRUE, seg.getName() });
            }

            allSegmentationsCombo.setModel(new DefaultComboBoxModel(names));
            updateSegmentTable();
        }
    }

    /**
     * Extracts all unique segment labels and places them in a table with:<br>
     * - a checkbox to indicate whether or not this segment should be
     * converted to annotations<br>
     * - a textfiled for an alternative label <br>
     * - a checkbox to indicate that the label should receive an index number
     * as a suffix
     */
    private void updateSegmentTable() {
        if (allSegmentationsCombo.getSelectedIndex() > -1) {
            // clear table
            SegmentTableModel dtm = (SegmentTableModel) segmentsTable.getModel();
            dtm.removeAllRows();

            // fill table
            String name = (String) allSegmentationsCombo.getSelectedItem();
            Segmentation seg = null;

            for (int i = 0; i < segmentations.size(); i++) {
                seg = segmentations.get(i);

                if (name.equals(seg.getName())) {
                    List<RSelection> segments = seg.getSegments();
                    Segment segment = null;
                    List<String> segLabels = new ArrayList<String>();

                    for (int j = 0; j < segments.size(); j++) {
                        segment = (Segment) segments.get(j);

                        if ((segment != null) &&
                                !segLabels.contains(segment.label)) {
                            model.addSegment(segment);
                            segLabels.add(segment.label);
                        }
                    }

                    break;
                }
            }
        }
    }

    /**
     * This is the one tier / segmentation per operation conversion. The users
     * can  select, rename and number segments based on the segment labels. It
     * passes a HashMap of size 1 to the conversion command.
     */
    private void convertSingleSegmentation() {
        Segmentation segmentation = null;
        String segmentationName = (String) allSegmentationsCombo.getSelectedItem();

        for (int i = 0; i < segmentations.size(); i++) {
            if (segmentations.get(i).getName()
                     .equals(segmentationName)) {
                segmentation = segmentations.get(i);

                break;
            }
        }

        if (segmentation == null) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString(
                    "SegmentsToTierDialog.Warning.NoSegmentation"), "",
                JOptionPane.WARNING_MESSAGE);

            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            
            setBusyUI(false);

            return; // message
        }

        // find selected segment values and configurable options
        HashMap<String, String> labelMap = new HashMap<String, String>(segmentsTable.getRowCount());

        // first make sure no cell is being edited right now
        if (segmentsTable.isEditing()) {
        	int ecol = segmentsTable.getEditingColumn();
        	int erow = segmentsTable.getEditingRow();
        	segmentsTable.getCellEditor(erow, ecol).stopCellEditing();
        }
        // a counter per label. an int array of size 1 is used to mimic a mutable Integer
        HashMap<String, int[]> numMap = new HashMap<String, int[]>(segmentsTable.getRowCount());
        Boolean sel;

        for (int i = 0; i < segmentsTable.getRowCount(); i++) {
            sel = (Boolean) model.getValueAt(i,
                    model.findColumn(SegmentTableModel.INCLUDE));

            if (sel.booleanValue()) {
                String segLabel = (String) model.getValueAt(i,
                        model.findColumn(SegmentTableModel.LABEL));
                String newSegLabel = (String) model.getValueAt(i,
                        model.findColumn(SegmentTableModel.LABEL_NEW));
                Boolean indexCount = (Boolean) model.getValueAt(i,
                        model.findColumn(SegmentTableModel.NUMBER));

                // System.out.println("L: " + segLabel + " NL: " + newSegLabel + " I: " + indexCount);
                if ((newSegLabel == null) || newSegLabel.equals("")) {
                    newSegLabel = segLabel;
                }

                labelMap.put(segLabel, newSegLabel);

                if (indexCount.booleanValue()) {
                    numMap.put(segLabel, new int[] { 1 });
                }
            }
        }

        if (labelMap.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString(
                    "SegmentsToTierDialog.Warning.NoSegmentsSelected"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            
            setBusyUI(false);

            return;
        }

        // int arrays of size 1 to deal with multiple number counters
        List<RSelection> segments = segmentation.getSegments();
        List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>(segments.size());
        Segment seg = null;
        String val = null;
        int[] curCount = null;

        for (int i = 0; i < segments.size(); i++) {
            seg = (Segment) segments.get(i);

            if (seg.label == null) {
                val = labelMap.get(SegmentTableModel.NULL);
            } else {
                val = labelMap.get(seg.label);
            }

            if (val != null) {
                if (SegmentTableModel.NULL.equals(val)) {
                    val = "";
                }

                if (seg.label == null) {
                    curCount = numMap.get(SegmentTableModel.NULL);
                } else {
                    curCount = numMap.get(seg.label);
                }

                if (curCount != null) {
                    val += curCount[0]++;
                }

                records.add(new AnnotationDataRecord("", val, seg.beginTime,
                        seg.endTime));
            }
        }

        HashMap<String, List<AnnotationDataRecord>> segmentationMap = new HashMap<String, List<AnnotationDataRecord>>(1);
        segmentationMap.put(segmentation.getName(), records);

        // create command
        com = (SegmentsToTiersCommand) ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.SEGMENTS_2_TIER);
        com.addProgressListener(this);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        com.execute(transcription, new Object[] { segmentationMap });

    }

    /**
     * Converts all selected segmentations to tiers without customization
     * possibilities. A HashMap containing an undefined number of
     * name-segments key-value pairs is passed to the command.
     */
    private void convertMultiSegmentation() {
        ArrayList<String> selSeg = new ArrayList<String>();
        Segmentation seg = null;
        String name = null;
        Boolean sel;

        for (int i = 0; i < segmentationsTable.getRowCount(); i++) {
            sel = (Boolean) dtModel.getValueAt(i,
                    dtModel.findColumn(SegmentTableModel.INCLUDE));

            if (sel.booleanValue()) {
                selSeg.add((String) dtModel.getValueAt(i,
                        dtModel.findColumn(SegmentTableModel.LABEL)));
            }
        }

        if (selSeg.size() == 0) {
            // message
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString(
                    "SegmentsToTierDialog.Warning.NoSegmentationSelected"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            setBusyUI(false);

            return;
        }

        HashMap<String, ArrayList<AnnotationDataRecord>> segmentationMap = new HashMap<String, ArrayList<AnnotationDataRecord>>(selSeg.size());
        Segment segment;
        ArrayList<RSelection> segments = null;
        ArrayList<AnnotationDataRecord> records = null;

        for (int i = 0; i < selSeg.size(); i++) {
            seg = null;
            name = selSeg.get(i);

            for (int j = 0; j < segmentations.size(); j++) {
                if (segmentations.get(j).getName().equals(name)) {
                    seg = segmentations.get(j);

                    break;
                }
            }

            if (seg == null) {
                continue;
            }

            segments = seg.getSegments();
            records = new ArrayList<AnnotationDataRecord>();

            for (int j = 0; j < segments.size(); j++) {
                segment = (Segment) segments.get(j);
                records.add(new AnnotationDataRecord("", segment.label,
                        segment.beginTime, segment.endTime));
            }

            segmentationMap.put(name, records);
        }

        // create command
        com = (SegmentsToTiersCommand) ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.SEGMENTS_2_TIER);
        com.addProgressListener(this);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        com.execute(transcription, new Object[] { segmentationMap });

    }

    private void setBusyUI(boolean busy) {
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
        okButton.setEnabled(!busy);
        closeButton.setEnabled(!busy);
    }

    /**
     * The action performed handling
     *
     * @param e event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            setBusyUI(true);

            if (tabPane.getSelectedIndex() == 0) {
                // single segmentation, user configurable
                convertSingleSegmentation();
            } else {
                // multiple segmentations, simple conversion
                convertMultiSegmentation();
            }

            //setBusyUI(false);
        } else if (e.getSource() == closeButton) {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Update the table after selection of a segmentation in the segmentation
     * list.
     *
     * @param e the event
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        updateSegmentTable();
    }

    /**
     * Applies state changes in the table cell editor's checkbox.
     *
     * @param lse the change event
     */

    /*
       public void stateChanged(ChangeEvent e) {
           if (e.getSource() == editCB) {
               model.setValueAt(Boolean.valueOf(editCB.isSelected()),
                   segmentsTable.getEditingRow(),
                   model.findColumnByName(segmentsTable.getColumnName(
                           segmentsTable.getEditingColumn())));
           }
       }
     */
    /**
     * Handling of changes in the row selection of the segmentation's  table.
     *
     * @param lse the event
     */
    @Override
	public void valueChanged(ListSelectionEvent lse) {
        if ((dtModel != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = dtModel.findColumn(SegmentTableModel.INCLUDE);

            for (int i = b; i <= e; i++) {
                if (segmentationsTable.isRowSelected(i)) {
                    dtModel.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
    }

    /**
     * Notification of the end of the operation.
     *
     * @param source ignored
     * @param message ignored
     */
    @Override
	public void progressCompleted(Object source, String message) {
        progressBar.setValue(progressBar.getMaximum());

        if (com != null) {
            com.removeProgressListener(this);
        }

        JOptionPane.showMessageDialog(this,
            ElanLocale.getString("Message.Complete"), "",
            JOptionPane.INFORMATION_MESSAGE);
        setBusyUI(false);
    }

    /**
     * Notification of interrupted operation.
     *
     * @param source ignored
     * @param message ignored for now
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        // ignore, or warn
        if (com != null) {
            com.removeProgressListener(this);
        }

        setBusyUI(false);
    }

    /**
     * Notification of progress update.
     *
     * @param source ignored
     * @param percent the progress value
     * @param message ignored
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if (percent < progressBar.getMinimum()) {
            percent = progressBar.getMinimum();
        } else if (percent > progressBar.getMaximum()) {
            progressCompleted(null, null);

            return;
        }

        progressBar.setValue(percent);
    }
}
