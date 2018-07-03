package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.MultipleFileStatisticsCommand;
import mpi.eudico.client.annotator.export.ExportStatistics;
import mpi.eudico.client.annotator.tier.DisplayableContentTableModel;
import mpi.eudico.client.annotator.tier.SelectableContentTableModel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.multiplefilesedit.statistics.StatisticsCollectionMF;
import mpi.eudico.client.util.TableHeaderToolTipAdapter;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.eudico.util.EmptyStringComparator;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;

/**
 * A dialog for creating simple statistics for multiple files.
 * It is possible to select a subset of the tiers found in all files.
 *
 */
@SuppressWarnings("serial")
public class StatisticsMultipleFilesDialog extends ClosableDialog
	implements ClientLogger, ListSelectionListener, ActionListener, ProgressListener {
	public final int ANN_COL_INDEX = 0;
	public final int TIER_COL_INDEX = 1;
	public final int TYPE_COL_INDEX = 2;
	public final int PART_COL_INDEX = 3;
	public final int ANNOTATOR_COL_INDEX = 4;
	public final int LANGUAGE_COL_INDEX = 5;
	private JButton saveButton, closeButton;
	private JFrame parent;
	private JLabel titleLabel;
	private JTabbedPane tabPane;
	
	private JPanel fileSelectPanel, buttonPanel, titlePanel;
	private JScrollPane tierTableScrollPane;
	private JTable tierTable;
	private JButton selectDomainButton, selectFilesButton, selectAllButton, selectNoneButton, updateStatisticsButton;
	
	private Insets defaultInsets, extraVerticalInset;
	private JProgressBar progressBar;
	
	private List<TranscriptionImpl> transImplList;
	private List<String> fileNames;
	private static String DEFAULT_TABLE_MESSAGE = ElanLocale.getString("Statistics.Multi.NoShow");
	private static String TIER_SELECTION_TABLE_HEADER = ElanLocale.getString("Frame.GridFrame.ColumnTierName");
	
	// have two maps, index to tab id (name) and id (name) to table
	private Map<Integer, String> tabTitles;
	private Map<String, JTable> tabTables;
	private Map<Integer, String[]> tableHeaders;
	private StatisticsCollectionMF curStatsCollection;
	private MultipleFileStatisticsCommand curCommand;
	
	public StatisticsMultipleFilesDialog(JFrame parentFrame){
		super(parentFrame);
		parent = parentFrame;
		
		transImplList = new ArrayList<TranscriptionImpl>();
		
		defaultInsets = new Insets(2, 6, 2, 6);
		extraVerticalInset = new Insets(2, 6, 8, 6);
		
		initComponents();
		
		setTitle(ElanLocale.getString("Statistics.Multi.Title"));
		setSize(500,300);
		//setVisible(true);
		
		tierTable.setModel(new DisplayableContentTableModel(StatisticsMultipleFilesDialog.DEFAULT_TABLE_MESSAGE));
		tierTable.getColumnModel().getColumn(0).setHeaderValue(TIER_SELECTION_TABLE_HEADER);
		tierTable.getTableHeader().setReorderingAllowed(false);
		
		//resize and relocate window
		pack();
		setLocationRelativeTo(parent);
	}
	
	private void initComponents(){
		titleLabel = new JLabel(ElanLocale.getString("Statistics.Multi.Title"));
        titlePanel = new JPanel();
        
        tabPane = new JTabbedPane();
        initStatisticsTab();
        
        //Button components
        saveButton = new JButton(ElanLocale.getString("Button.Save"));
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        buttonPanel = new JPanel();

        fileSelectPanel = new JPanel( new GridBagLayout() );
        
        initTierSelectionPanel();
        
        //add event handlers 
        saveButton.addActionListener(this);
        closeButton.addActionListener(this);
        selectDomainButton.addActionListener(this);
        selectFilesButton.addActionListener(this);
        selectAllButton.addActionListener(this);
        selectNoneButton.addActionListener(this);
        updateStatisticsButton.addActionListener(this);

        GridBagConstraints gridBagConstraints;
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(2, 6, 2, 6);

        //Setting title
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(titlePanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(fileSelectPanel, gridBagConstraints);
        
        // add tabpane
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabPane, gridBagConstraints);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(progressBar, gridBagConstraints);

        //Setting buttons
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 2));
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        fileSelectPanel.setBorder(new TitledBorder(ElanLocale.getString("Statistics.Multi.TierSelection")));
        
        getRootPane().setDefaultButton(closeButton);
	}
	
	private void initTierSelectionPanel(){
		tierTable = new JTable( new DisplayableContentTableModel("Please wait") );
		tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierTable.getSelectionModel().addListSelectionListener(this);
		tierTable.setShowVerticalLines(false);
		tierTableScrollPane = new JScrollPane(tierTable);
		tierTableScrollPane.setPreferredSize(new Dimension(400, 150));
		tierTableScrollPane.setColumnHeaderView(null);
		
		selectDomainButton = new JButton(ElanLocale.getString("MFE.DomainDefKey"));
        selectFilesButton = new JButton(ElanLocale.getString("Statistics.Multi.Files"));
        selectAllButton = new JButton(ElanLocale.getString("Button.SelectAll"));
        selectNoneButton = new JButton(ElanLocale.getString("Button.SelectNone"));
        updateStatisticsButton = new JButton(ElanLocale.getString("Statistics.Multi.Update"));
        updateStatisticsButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        selectNoneButton.setEnabled(false);
        
        //layout components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.insets = defaultInsets;
        fileSelectPanel.add(tierTableScrollPane, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        fileSelectPanel.add(selectDomainButton, gbc);
        
        gbc.gridy = 1;
        gbc.insets = extraVerticalInset;
        fileSelectPanel.add(selectFilesButton, gbc);
        
        gbc.gridy = 2;
        gbc.insets = defaultInsets;
        fileSelectPanel.add(selectAllButton, gbc);
        
        gbc.gridy = 3;
        gbc.insets = extraVerticalInset;
        fileSelectPanel.add(selectNoneButton, gbc);
        
        gbc.gridy = 4;
        gbc.insets = defaultInsets;
        fileSelectPanel.add(updateStatisticsButton, gbc);		
	}
	
	/**
	 * Initializes the tab pane, including the tables for each tab.
	 */
	private void initStatisticsTab(){
		tabPane.setPreferredSize(new Dimension(400,300));

		tabTitles = new HashMap<Integer, String>(6);
		tabTables = new HashMap<String, JTable>(6);
		tableHeaders = new HashMap<Integer, String[]>(6);
		// annotation
		String id = ElanLocale.getString("Statistics.Panel.Annotation");
		tabTitles.put(ANN_COL_INDEX, id);
		String[] headers = new String[9];
		headers[0] = ElanLocale.getString("Frame.GridFrame.ColumnTierName");
        headers[1] = ElanLocale.getString("Frame.GridFrame.ColumnAnnotation");
        headers[2] = ElanLocale.getString("Statistics.Occurrences");
        headers[3] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[4] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[5] = ElanLocale.getString("Statistics.AverageDuration");
        headers[6] = ElanLocale.getString("Statistics.MedianDuration");
        headers[7] = ElanLocale.getString("Statistics.TotalDuration");
        headers[8] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(ANN_COL_INDEX, headers);
        JTable annTable = new JTable(new DefaultTableModel(headers, 1));
        annTable.setEnabled(false);
        annTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		annTable.getTableHeader()));
		tabTables.put(id, annTable);
		tabPane.addTab(id, new JScrollPane(annTable));
		
		// tier
		id = ElanLocale.getString("Statistics.Panel.Tier");
		tabTitles.put(TIER_COL_INDEX, id);
        headers = new String[9];
        headers[0] = ElanLocale.getString("Frame.GridFrame.ColumnTierName");
        headers[1] = ElanLocale.getString("Statistics.NumFiles");
        headers[2] = ElanLocale.getString("Statistics.NumAnnotations");
        headers[3] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[4] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[5] = ElanLocale.getString("Statistics.AverageDuration");
        headers[6] = ElanLocale.getString("Statistics.MedianDuration");
        headers[7] = ElanLocale.getString("Statistics.TotalDuration");
        headers[8] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(TIER_COL_INDEX, headers);
        JTable tiTable = new JTable(new DefaultTableModel(headers, 0));
        tiTable.setEnabled(false);
        tiTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		tiTable.getTableHeader()));
		tabTables.put(id, tiTable);
		tabPane.addTab(id, new JScrollPane(tiTable));
		
		// type
		id = ElanLocale.getString("Statistics.Panel.Type");
		tabTitles.put(TYPE_COL_INDEX, id);
		headers = new String[10];
		headers[0] = ElanLocale.getString("Statistics.Type");
		headers[1] = ElanLocale.getString("Statistics.NumFiles");
		headers[2] = ElanLocale.getString("Statistics.NumTiers");
        headers[3] = ElanLocale.getString("Statistics.NumAnnotations");
        headers[4] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[5] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[6] = ElanLocale.getString("Statistics.AverageDuration");
        headers[7] = ElanLocale.getString("Statistics.MedianDuration");
        headers[8] = ElanLocale.getString("Statistics.TotalDuration");
        headers[9] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(TYPE_COL_INDEX, headers);
        JTable typTable = new JTable(new DefaultTableModel(headers, 0));
        typTable.setEnabled(false);
        typTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		typTable.getTableHeader()));
		tabTables.put(id, typTable);
		tabPane.addTab(id, new JScrollPane(typTable));
		
		// participant
		id = ElanLocale.getString("Statistics.Panel.Participant");
		tabTitles.put(PART_COL_INDEX, id);
		headers = new String[10];
		headers[0] = ElanLocale.getString("Statistics.Participant");
		headers[1] = ElanLocale.getString("Statistics.NumFiles");
		headers[2] = ElanLocale.getString("Statistics.NumTiers");
        headers[3] = ElanLocale.getString("Statistics.NumAnnotations");
        headers[4] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[5] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[6] = ElanLocale.getString("Statistics.AverageDuration");
        headers[7] = ElanLocale.getString("Statistics.MedianDuration");
        headers[8] = ElanLocale.getString("Statistics.TotalDuration");
        headers[9] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(PART_COL_INDEX, headers);
        JTable partTable = new JTable(new DefaultTableModel(headers, 0));
        partTable.setEnabled(false);
        partTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		partTable.getTableHeader()));
		tabTables.put(id, partTable);
		tabPane.addTab(id, new JScrollPane(partTable));
		
		// annotator
		id = ElanLocale.getString("Statistics.Panel.Annotator");
		tabTitles.put(ANNOTATOR_COL_INDEX, id);
		headers = new String[10];
		headers[0] = ElanLocale.getString("Statistics.Annotator");
		headers[1] = ElanLocale.getString("Statistics.NumFiles");
		headers[2] = ElanLocale.getString("Statistics.NumTiers");
        headers[3] = ElanLocale.getString("Statistics.NumAnnotations");
        headers[4] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[5] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[6] = ElanLocale.getString("Statistics.AverageDuration");
        headers[7] = ElanLocale.getString("Statistics.MedianDuration");
        headers[8] = ElanLocale.getString("Statistics.TotalDuration");
        headers[9] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(ANNOTATOR_COL_INDEX, headers);
        JTable annotTable = new JTable(new DefaultTableModel(headers, 0));
        annotTable.setEnabled(false);
        annotTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		annotTable.getTableHeader()));
		tabTables.put(id, annotTable);
		tabPane.addTab(id, new JScrollPane(annotTable));
		
		// language, this can all be done a bit more efficient
		id = ElanLocale.getString("Statistics.Panel.Language");
		tabTitles.put(LANGUAGE_COL_INDEX, id);
		headers = new String[10];
		headers[0] = ElanLocale.getString("MFE.TierHeader.Language");// re-use
		headers[1] = ElanLocale.getString("Statistics.NumFiles");
		headers[2] = ElanLocale.getString("Statistics.NumTiers");
        headers[3] = ElanLocale.getString("Statistics.NumAnnotations");
        headers[4] = ElanLocale.getString("Statistics.MinimalDuration");
        headers[5] = ElanLocale.getString("Statistics.MaximalDuration");
        headers[6] = ElanLocale.getString("Statistics.AverageDuration");
        headers[7] = ElanLocale.getString("Statistics.MedianDuration");
        headers[8] = ElanLocale.getString("Statistics.TotalDuration");
        headers[9] = ElanLocale.getString("Statistics.Latency");
        tableHeaders.put(LANGUAGE_COL_INDEX, headers);
        JTable langTable = new JTable(new DefaultTableModel(headers, 0));
        langTable.setEnabled(false);
        langTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(
        		langTable.getTableHeader()));
		tabTables.put(id, langTable);
		tabPane.addTab(id, new JScrollPane(langTable));
	}
	
	/**
	 * Updates all tables after a (new) run of statistics calculation. 
	 */
	private void updateTableTabs() {
		if (curStatsCollection != null) {
			List<String[]> annStats = curStatsCollection.getAllAnnotationStatistics();
			// annotations
			JTable annTable = tabTables.get(tabTitles.get(ANN_COL_INDEX));
			String[] headers = tableHeaders.get(ANN_COL_INDEX);
			DefaultTableModel model = new DefaultTableModel(0, headers.length);
			for (String[] row : annStats) {
				model.addRow(row);
			}
			model.setColumnIdentifiers(headers);
			annTable.setModel(model);
			addRowSorterAttributeTable(annTable, 2, 3);
			
			// tiers
			List<String[]> tierStats = curStatsCollection.getTierStatistics();
			JTable tierTable = tabTables.get(tabTitles.get(TIER_COL_INDEX));
			headers = tableHeaders.get(TIER_COL_INDEX);
			DefaultTableModel tmodel = new DefaultTableModel(0, headers.length);
			for (String[] row : tierStats) {
				tmodel.addRow(row);
			}
			tmodel.setColumnIdentifiers(headers);
			tierTable.setModel(tmodel);
			addRowSorterAttributeTable(tierTable, 1, 3);
			
			// types
			List<String[]> typeStats = curStatsCollection.getTypeStatistics();
			JTable typeTable = tabTables.get(tabTitles.get(TYPE_COL_INDEX));
			headers = tableHeaders.get(TYPE_COL_INDEX);
			DefaultTableModel typemodel = new DefaultTableModel(0, headers.length);
			for (String[] row : typeStats) {
				typemodel.addRow(row);
			}
			typemodel.setColumnIdentifiers(headers);
			typeTable.setModel(typemodel);
			addRowSorterAttributeTable(typeTable, 1, 4);
			
			// participant
			List<String[]> partStats = curStatsCollection.getPartStatistics();
			JTable partTable = tabTables.get(tabTitles.get(PART_COL_INDEX));
			headers = tableHeaders.get(PART_COL_INDEX);
			DefaultTableModel partModel = new DefaultTableModel(0, headers.length);
			for (String[] row : partStats) {
				partModel.addRow(row);
			}
			partModel.setColumnIdentifiers(headers);
			partTable.setModel(partModel);
			addRowSorterAttributeTable(partTable, 1, 4);
			
			// annotator
			List<String[]> annotatorStats = curStatsCollection.getAnnotatorStatistics();
			JTable annotatorTable = tabTables.get(tabTitles.get(ANNOTATOR_COL_INDEX));
			headers = tableHeaders.get(ANNOTATOR_COL_INDEX);
			DefaultTableModel annotatorModel = new DefaultTableModel(0, headers.length);
			for (String[] row : annotatorStats) {
				annotatorModel.addRow(row);
			}
			annotatorModel.setColumnIdentifiers(headers);
			annotatorTable.setModel(annotatorModel);
			addRowSorterAttributeTable(annotatorTable, 1, 4);
			
			// language
			List<String[]> langStats = curStatsCollection.getLanguageStatistics();
			JTable langTable = tabTables.get(tabTitles.get(LANGUAGE_COL_INDEX));
			headers = tableHeaders.get(LANGUAGE_COL_INDEX);
			DefaultTableModel langModel = new DefaultTableModel(0, headers.length);
			for (String[] row : langStats) {
				langModel.addRow(row);
			}
			langModel.setColumnIdentifiers(headers);
			langTable.setModel(langModel);
			addRowSorterAttributeTable(langTable, 1, 4);
		}
	}
	
	/**
	 * Adds a row sorter to the table. The assumption is there are first one or more
	 * columns containing Strings, then one or more columns with int values (as string) and
	 * next one or more columns containing float values (as string).
	 * 
	 * @param table the table, not null
	 * @param firstIntCol the first column containing int values, assumed > 0 and < number of columns
	 * @param firstFloatCol the first column containing float values, assumed > first int column and < number of columns
	 */
	private void addRowSorterAttributeTable(JTable table, int firstIntCol, int firstFloatCol) {
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(table.getModel());
        IntStringComparator<String> intComp = new IntStringComparator<String>();
        
        for (int i = firstIntCol; i < firstFloatCol; i++) {
        	rowSorter.setComparator(i, intComp);
        }
        
        FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
        for (int i = firstFloatCol; i < table.getColumnCount(); i++) {
        	rowSorter.setComparator(i, fsComp);
        }
        table.setRowSorter(rowSorter);
	}
	
    /**
     * Scans the folders for eaf files and adds their paths to the files list,
     * recursively.
     * Adaptation from DomainPane, List of String instead of List of Files
     * 
     * @param dir the directory or folder
     * @param files the list to add the file paths to
     */
    protected void addFiles(File dir, List<String> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
                addFiles(allSubs[i], files);
            } else {
                if (allSubs[i].canRead()) {
                    if (allSubs[i].getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                        files.add(allSubs[i].getAbsolutePath());
                    }
                }
            }
        }
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton sender = (JButton) e.getSource();
		
		if ( sender == saveButton ) {
			int col = tabPane.getSelectedIndex();
			JTable curTable = tabTables.get(tabTitles.get(col));
			if (curTable.getRowCount() == 0) {
				// message?
				return;
			}
			new ExportStatistics((JFrame) getParent(), true, null,
					curTable);
			return;
		}
		
		if ( sender == closeButton ) {
			setVisible(false);
			dispose();
			return;
		}

	    if (sender == selectDomainButton) {
			// create domain dialog
			MFDomainDialog domainDialog = new MFDomainDialog(
					StatisticsMultipleFilesDialog.this, true);
			domainDialog.setVisible(true);
			// when domain is selected, get the search paths
			List<String> searchPaths = domainDialog.getSearchPaths();
			List<String> searchDirs = domainDialog.getSearchDirs();
			fileNames = searchPaths;
			if (searchDirs.size() > 0) {
				String name;
				File f;
				for (int i = 0; i < searchDirs.size(); i++) {
					name = searchDirs.get(i);
					f = new File(name);
					if (f.isFile() && f.canRead()) {
						fileNames.add(f.getAbsolutePath());// should not occur
					} else if (f.isDirectory() && f.canRead()) {
						addFiles(f, fileNames);
					}
				}
			}
			// check if domain contains files
			if (!fileNames.isEmpty()) {
				// load the files in the selected domain
				Thread t = new OpenFilesThread(fileNames);
				t.start();
			}
		} 

		if ( sender == selectFilesButton ) {
			List<String> filenames = showMultiFileChooser();
			if( filenames != null && !filenames.isEmpty() ){
				fileNames = filenames;
				Thread t = new OpenFilesThread( filenames );
				t.start();
			}
			return;
		}
		
		if ( sender == selectAllButton ) {
			if (tierTable.getModel() instanceof SelectableContentTableModel) {
				SelectableContentTableModel model = (SelectableContentTableModel) tierTable.getModel();
				model.selectAll();
			}
			return;
		}
		
		if ( sender == selectNoneButton ){
			if (tierTable.getModel() instanceof SelectableContentTableModel) {
				SelectableContentTableModel model = (SelectableContentTableModel) tierTable.getModel();
				model.selectNone();
			}
			return;
		}
		
		if ( sender == updateStatisticsButton ) {
			//compute statistics
			SelectableContentTableModel model = (SelectableContentTableModel)tierTable.getModel();
			List<Object> selectedTiersList = model.getSelectedValues();
			
			if (selectedTiersList.size() == 0) {
				// warn
				JOptionPane.showMessageDialog(this, ElanLocale.getString("Statistics.Multi.NoTierSelected"),
			            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			String[] selectedTiers = selectedTiersList.toArray(new String[]{});
			String[] filePaths = fileNames.toArray(new String[]{});
			curCommand = new MultipleFileStatisticsCommand("Multiple File Statistics");
			curCommand.addProgressListener(this);
			curCommand.setProcessReport(new SimpleReport(ElanLocale.getString("ProcessReport")));
			curStatsCollection = new StatisticsCollectionMF();
			boolean loadAll = (selectedTiers.length == model.getRowCount());
			curCommand.execute(null, new Object[]{filePaths, selectedTiers, 
					Boolean.valueOf(loadAll), curStatsCollection});
			
		}
	}
	
	/**
	 * Handler for the case if a user selects a tier in the table.
	 * It then sets the value in the table accordingly
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {		
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if( e.getValueIsAdjusting() ){
			int selectedRow = lsm.getMinSelectionIndex();
			
			//only continue if selectedRow is greater than or equal to 0
			if( selectedRow >= 0 )
				tierTable.setValueAt( !(Boolean)tierTable.getValueAt(selectedRow, 0), selectedRow, 0);
		}
	}

	@Override
	public void progressCompleted(Object source, String message) {
        progressBar.setValue(100);
        progressBar.setString(message);
		
        updateTableTabs();

        // show report
        if ((curCommand != null) && (curCommand.getProcessReport() != null)) {
            new ReportDialog(this, curCommand.getProcessReport()).setVisible(true);
        }
        progressBar.setValue(0);
        progressBar.setString("");
	}

	@Override
	public void progressInterrupted(Object source, String message) {
		progressBar.setString(message);
		// popup message
		JOptionPane.showMessageDialog(this, message,
	            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
		// show report
        if ((curCommand != null) && (curCommand.getProcessReport() != null)) {
            new ReportDialog(this, curCommand.getProcessReport()).setVisible(true);
        }
	}

	@Override
	public void progressUpdated(Object source, int percent, String message) {
        if (percent == progressBar.getMaximum()) {
            progressCompleted(source, message);
        } else {
            progressBar.setValue(percent);
            progressBar.setString(message);
        }
	}
	
	/**
	 * Shows a multiple file chooser dialog, checks if every selected file exists
	 * and stores the selected files in private variable eafFiles
	 * @return boolean to indicate if file selection went successful
	 */
	private List<String> showMultiFileChooser(){		
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.GENERIC, 
        		FileExtension.EAF_EXT, "LastUsedEAFDir");
        
        List<String> fileNames = null;
        
        Object[] objects = chooser.getSelectedFiles();

		if (objects!= null) {
            //convert all objects to the file array and checks if all files exist
            //this extra step is done because mutliFileChooser.getFiles()
            //can't be casted to a File array directly (ClassCastException)
			if(objects.length > 0){
				fileNames = new ArrayList<String>();
				for (int i = 0; i < objects.length; i++) {
                    if (!fileNames.contains(objects[i])) {
                        fileNames.add(""+objects[i]);
                    }
                }
			}
        }
		
		return fileNames;
	}
	
	/**
	 * A thread for loading tiers from a selection of files.
	 *
	 */
	private class OpenFilesThread extends Thread{
		List<String> filenames;
		DisplayableContentTableModel model;
		
		public OpenFilesThread( List<String> filenames ){
			this.filenames = filenames;
			model = new DisplayableContentTableModel( new String[]{"Busy with Opening Files. Please wait...", "0 out of " + filenames.size() + " opened (0%)"} );
			model.connectTable(tierTable);
			tierTable.setModel(model);
			tierTable.getColumnModel().getColumn(0).setHeaderValue(TIER_SELECTION_TABLE_HEADER);
			progressBar.setString("Extracting tiers..." );
		}
		
		@Override
		public void run(){
			Set<String> uniqueTierNames = new TreeSet<String>();
			List<String> failed = new ArrayList<String>();
			TierImpl tier;
			
			for( int i=0; i<filenames.size(); i++ ){
                try {
                    EAFSkeletonParser parser = new EAFSkeletonParser(filenames.get(i));
                    parser.parse();

                    List<TierImpl> tiers = parser.getTiers();

                    for (int j = 0; j < tiers.size(); j++) {
                        tier = (TierImpl) tiers.get(j);

                        if (tier != null) {
                            //loadedTierNames.add(tier.getName());
                        	uniqueTierNames.add(tier.getName());
                        }
                    }
                } catch (ParseException pe) {
                    //pe.printStackTrace();
                	ClientLogger.LOG.warning("Parsing failed: " + pe.getMessage());
                    failed.add(fileNames.get(i));
                }

                progressBar.setValue(Math.round((i+1) / ((float)filenames.size()) * 100.0f));
				model.updateMessage(1, (i+1) + " out of " + filenames.size() + " opened (" + Math.round((i+1)/((float)filenames.size())*100.0f) + "%)");
			}
			// remove failing files
			if (failed.size() > 0) {
				SimpleReport report = new SimpleReport(ElanLocale.getString("Message.Warning"));
				report.append("A number of files could not be parsed: " + failed.size());
				for (String s : failed) {
					filenames.remove(s);
					report.append(s);
				}
				// popup a message 
				new ReportDialog(StatisticsMultipleFilesDialog.this, report).setVisible(true);
			}
			
			//update table
			if ( uniqueTierNames.isEmpty() ) {
				transImplList.clear();
				fileNames.clear();//??
				DisplayableContentTableModel model = (DisplayableContentTableModel) tierTable.getModel();
				//tierTable.setModel(dataModel)
				
				model.setValueAt( StatisticsMultipleFilesDialog.DEFAULT_TABLE_MESSAGE, 0, 0);
				updateStatisticsButton.setEnabled(false);
				selectAllButton.setEnabled(false);
				selectNoneButton.setEnabled(false);
			} else {
				tierTable.setModel(new SelectableContentTableModel(uniqueTierNames));
				tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
				tierTable.getColumnModel().getColumn(0).setHeaderValue("");
				tierTable.getColumnModel().getColumn(1).setHeaderValue(TIER_SELECTION_TABLE_HEADER);
				tierTable.repaint();
				
				updateStatisticsButton.setEnabled(true);
				selectAllButton.setEnabled(true);
				selectNoneButton.setEnabled(true);
			}
			
			// add sorting
			TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(tierTable.getModel());
	        EmptyStringComparator emptyComp = new EmptyStringComparator();
	        rowSorter.setComparator(1, emptyComp);
	        tierTable.setRowSorter(rowSorter);
			
			progressBar.setValue(0);
			progressBar.setString("");
		}
	}


}
