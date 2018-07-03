package mpi.eudico.client.annotator.search.viewer;

import guk.im.GateIM;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.MultiFindAndReplaceCommand;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.gui.TierSelectionDialog;
import mpi.eudico.client.annotator.search.query.viewer.EAFPopupMenu;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.search.SearchLocale;


/**
 * A dialog for a find-and-replace action in multiple files.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class EAFMultipleFindReplaceDialog extends JDialog
    implements ActionListener, FocusListener, CaretListener, ProgressListener {
    //private File[] domainFiles;
    private File[] searchFiles;
    private List<String> selTiersList;
    private List<String> allTiersList;
    private JButton domainButton;
    private JPanel domainPanel;
    private JTable filesTable;
    private JRadioButton allTiersRB;
    private JRadioButton selectedTiersRB;
    private JButton selTiersButton;
    private JProgressBar loadTiersBar;
    private JPanel definePanel;
    private JLabel defineLabel;
    private JTextField findField;
    private Locale orgLocale;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox regexCheckBox;
    private JLabel replaceLabel;
    private JTextField replaceField;
    private JPanel progPanel;
    private JButton startStopButton;
    private JProgressBar progressBar;
    private JButton closeButton;
    private boolean isRunning = false;
    private boolean reloadTierList = false;
    private MultiFindAndReplaceCommand command;
    
    private List<String> searchDirs;
    private List<String> searchPaths;

    /**
     * Creates a new EAFMultipleFindReplaceDialog instance
     *
     * @throws HeadlessException headless exception
     */
    public EAFMultipleFindReplaceDialog() throws HeadlessException {
        this(null);
    }

    /**
     * Creates a new EAFMultipleFindReplaceDialog instance
     *
     * @param owner the parent frame
     *
     * @throws HeadlessException headless exception
     */
    public EAFMultipleFindReplaceDialog(Frame owner) throws HeadlessException {
        super(owner, true);

        initComponents();
        pack();
        postInit();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int w = 460;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(ElanLocale.getString("MultipleFileSearch.FindReplace.Title"));
        domainButton = new JButton();
        domainPanel = new JPanel();
        filesTable = new JTable();
        filesTable.setEnabled(false);
        filesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        allTiersRB = new JRadioButton();
        allTiersRB.setSelected(true);
        selectedTiersRB = new JRadioButton();
        selTiersButton = new JButton("...");
        selTiersButton.setEnabled(false);
        loadTiersBar = new JProgressBar(0, 100);
        loadTiersBar.setEnabled(false);
        loadTiersBar.setStringPainted(true);
        loadTiersBar.setString("");

        ButtonGroup bgroup = new ButtonGroup();
        bgroup.add(allTiersRB);
        bgroup.add(selectedTiersRB);
        definePanel = new JPanel();
        defineLabel = new JLabel();
        findField = new JTextField();
        findField.setFont(Constants.DEFAULTFONT.deriveFont(Font.BOLD));
        caseSensitiveCheckBox = new JCheckBox();
        caseSensitiveCheckBox.setFont(Constants.deriveSmallFont(caseSensitiveCheckBox.getFont()));
        regexCheckBox = new JCheckBox();
        regexCheckBox.setFont(caseSensitiveCheckBox.getFont());
        replaceLabel = new JLabel();
        replaceField = new JTextField();
        replaceField.setFont(Constants.DEFAULTFONT.deriveFont(Font.BOLD));

        startStopButton = new JButton();
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        closeButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(4, 6, 4, 6);
        domainPanel.setLayout(new GridBagLayout());
        domainPanel.setPreferredSize(new Dimension(300, 180));

        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = insets;
        domainPanel.add(domainButton, gbc);
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        domainPanel.add(new JScrollPane(filesTable), gbc);

        JPanel tierPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.insets = new Insets(0, 0, 0, 6);
        tierPanel.add(allTiersRB, gbc2);
        gbc2.gridx = 1;
        tierPanel.add(selectedTiersRB, gbc2);
        gbc2.gridx = 2;
        tierPanel.add(selTiersButton, gbc2);
        gbc2.gridx = 3;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;
        tierPanel.add(loadTiersBar, gbc2);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        domainPanel.add(tierPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(domainPanel, gbc);

        definePanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;
        definePanel.add(defineLabel, gbc);

        gbc.gridy = 1;
        definePanel.add(findField, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        definePanel.add(regexCheckBox, gbc);
        gbc.gridx = 1;
        definePanel.add(caseSensitiveCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        definePanel.add(replaceLabel, gbc);

        gbc.gridy = 4;
        definePanel.add(replaceField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(definePanel, gbc);

        progPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        progPanel.add(progressBar, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        progPanel.add(startStopButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(progPanel, gbc);

        // add close button...
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 6, 2));
        buttonPanel.add(closeButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(buttonPanel, gbc);

        domainButton.addActionListener(this);
        startStopButton.setEnabled(false);
        startStopButton.addActionListener(this);
        closeButton.addActionListener(this);
        updateLocale();

        if (findField.getInputContext() != null) {
            orgLocale = findField.getInputContext().getLocale();
        }

        new EAFPopupMenu(findField, null);
        findField.addFocusListener(this);

        if (orgLocale != null) {
            findField.setLocale(orgLocale);
        }

        new EAFPopupMenu(replaceField, null);
        replaceField.addFocusListener(this);

        if (orgLocale != null) {
            replaceField.setLocale(orgLocale);
        }

        findField.addActionListener(this);
        findField.addCaretListener(this);
        allTiersRB.addActionListener(this);
        selectedTiersRB.addActionListener(this);
        selTiersButton.addActionListener(this);
    }

    private void updateLocale() {
        domainPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MultipleFileSearch.SearchDomain")));
        domainButton.setText(ElanLocale.getString(
                "MultipleFileSearch.DomainDefKey"));
        allTiersRB.setText(ElanLocale.getString(
                "MultipleFileSearch.FindReplace.AllTiers"));
        selectedTiersRB.setText(ElanLocale.getString(
                "MultipleFileSearch.FindReplace.SelectedTiers"));
        //filesTable headers??
        definePanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace")));
        defineLabel.setText(ElanLocale.getString(
                "MultipleFileSearch.FindReplace.Find"));
        caseSensitiveCheckBox.setText(SearchLocale.getString(
                "Search.Constraint.CaseSensitive"));
        regexCheckBox.setText(SearchLocale.getString(
                "Search.Constraint.RegularExpression"));
        replaceLabel.setText(ElanLocale.getString(
                "MultipleFileSearch.FindReplace.Replace"));
        progPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Progress")));
        startStopButton.setText(ElanLocale.getString(
                "MultipleFileSearch.FindReplace"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    private void enableUI(boolean enable) {
        domainButton.setEnabled(enable);
        findField.setEnabled(enable);
        regexCheckBox.setEnabled(enable);
        caseSensitiveCheckBox.setEnabled(enable);
        replaceField.setEnabled(enable);
        closeButton.setEnabled(enable);
    }

    private void checkStartStopState() {
        if (!isRunning) {
            if ((searchFiles != null) && (searchFiles.length > 0) &&
                    (findField.getText() != null) &&
                    (findField.getText().length() > 0)) {
                startStopButton.setEnabled(true);
            } else {
                startStopButton.setEnabled(false);
            }
        }
    }

    /**
     * Shows a file chooser to specify the files and directories to process.
     */
    private void updateFileList() {
        List<File> files = getMultipleFiles(null,
                ElanLocale.getString("MultipleFileSearch.DomainDialogTitle"));

        if ((files != null) && !files.isEmpty()) {
            reloadTierList = true; // the domain changed
            searchFiles = files.toArray(new File[files.size()]);

            // clear the table
            // add to the table
            String[][] data = new String[files.size()][2];
            String[] headers = new String[] {
                    ElanLocale.getString("Menu.File"),
                    ElanLocale.getString("LinkedFilesDialog.Label.MediaURL")
                };
            File f;

            for (int i = 0; i < files.size(); i++) {
                f = files.get(i);
                data[i][1] = f.getAbsolutePath();
                data[i][0] = FileUtility.fileNameFromPath(data[i][1]);
            }

            DefaultTableModel model = new DefaultTableModel(data, headers);
            filesTable.setModel(model);

            TableColumn tc = filesTable.getColumnModel().getColumn(0);
            tc.setPreferredWidth(140);
            tc = filesTable.getColumnModel().getColumn(1);
            tc.setPreferredWidth(600);
            filesTable.doLayout();
            startStopButton.setEnabled(true); //?? check textfields??
        }
    }

    /**
     * Shows a multiple file chooser to select multiple eaf files and or
     * folders.
     *
     * @param parent the parent frame
     * @param title the title for the dialog
     *
     * @return a list of File objects (files and folders)
     */
    protected List<File> getMultipleFiles(JFrame parent, String title) {
        ArrayList<File> files = new ArrayList<File>();
        if (searchDirs == null){
        	searchDirs = new ArrayList<String>();
        }
        if (searchPaths == null) {
        	searchPaths = new ArrayList<String>();
        }
        
    	// prompt with a list of domains
    	// if one is picked load that domain, otherwise continue with 
    	// "new domain prompt"
    	MFDomainDialog mfDialog = new MFDomainDialog(this, 
    			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
    	mfDialog.setSearchDirs(searchDirs);
    	mfDialog.setSearchPaths(searchPaths);
    	mfDialog.setVisible(true);
    	searchDirs = (List<String>) mfDialog.getSearchDirs();// neccessary?
    	searchPaths = (List<String>) mfDialog.getSearchPaths();//neccessary?

        if (searchPaths.size() > 0) {
        	String name;
        	File f;
        	for (int i = 0; i < searchPaths.size(); i++) {
        		name = searchPaths.get(i);
        		f = new File(name);
        		if (f.isFile() && f.canRead()) {
        			files.add(f);
        		} else if (f.isDirectory() && f.canRead()) {
        			addFiles(f, files);// should not occur
        		}
        	}
        }
        if (searchDirs.size() > 0) {
        	String name;
        	File f;
        	for (int i = 0; i < searchDirs.size(); i++) {
        		name = searchDirs.get(i);
        		f = new File(name);
        		if (f.isFile() && f.canRead()) {
        			files.add(f);//should not occur
        		} else if (f.isDirectory() && f.canRead()) {
        			addFiles(f, files);
        		}
        	}
        }
        
        return files;
    }

    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the directory or folder
     * @param files the list to add the files to
     */
    protected void addFiles(File dir, List<File> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (File allSub : allSubs) {
            if (allSub.isDirectory() && allSub.canRead()) {
                addFiles(allSub, files);
            } else {
                if (allSub.canRead()) {
                    if (allSub.getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                        files.add(allSub);
                    }
                }
            }
        }
    }

    private void selectTiers() {
        if ((searchFiles == null) || (searchFiles.length == 0)) {
            showWarningDialog(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Warn1"));
            enableUI(true);

            return;
        }

        if ((allTiersList == null) || reloadTierList) {
            loadTierNames();
            reloadTierList = false;
        } else {
            showTierSelectionDialog();
        }
    }

    private void showTierSelectionDialog() {
        if (allTiersList != null) {
            TierSelectionDialog tsd = new TierSelectionDialog(this,
                    allTiersList, selTiersList);
            tsd.setVisible(true); //blocks

            List<String> selTiers = tsd.getValue();

            if (selTiers != null) {
                selTiersList = selTiers;
            }
        }
    }

    private void loadTierNames() {
        selTiersButton.setEnabled(false);
        loadTiersBar.setString(ElanLocale.getString(
                "MultipleFileSearch.FindReplace.LoadingTiers"));
        allTiersList = new ArrayList<String>();

        final TierLoadThread tlt = new TierLoadThread(allTiersList);

        try {
            tlt.start();
        } catch (IllegalThreadStateException ie) {
            ie.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void start() {
        // first do some checks
        if ((searchFiles == null) || (searchFiles.length == 0)) {
            showWarningDialog(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Warn1"));
            enableUI(true);

            return;
        }

        if ((findField.getText() == null) ||
                (findField.getText().length() == 0)) {
            showWarningDialog(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Warn2"));
            enableUI(true);

            return;
        }

        if ((replaceField.getText() == null) ||
                (replaceField.getText().length() == 0)) {
            // option
            int option = JOptionPane.showConfirmDialog(this,
                    ElanLocale.getString("MultipleFileSearch.FindReplace.Warn3"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option != JOptionPane.YES_OPTION) {
                enableUI(true);

                return;
            }
        }

        // finally create a command and start
        String searchPattern = findField.getText(); // trim??
        Boolean regEx = Boolean.valueOf(regexCheckBox.isSelected());
        Boolean caseSens = Boolean.valueOf(caseSensitiveCheckBox.isSelected());
        String replPattern = replaceField.getText();

        if (replPattern == null) {
            replPattern = "";
        }

        // check the pattern if it is a regular expression
        if (regEx.booleanValue()) {
            int flag = caseSens.booleanValue() ? 0 : Pattern.CASE_INSENSITIVE;

            try {
                Pattern.compile(searchPattern, flag);
            } catch (PatternSyntaxException pse) {
                showWarningDialog(ElanLocale.getString(
                        "MultipleFileSearch.FindReplace.Warn4") + " " +
                    pse.getMessage());
                enableUI(true);

                return;
            }
        }

        String[] selTiers = null;

        if (selectedTiersRB.isSelected() && (selTiersList != null)) {
            selTiers = selTiersList.toArray(new String[] {  });
        }

        // then popup some warning
        int option = JOptionPane.showConfirmDialog(this,
                ElanLocale.getString("MultipleFileSearch.FindReplace.WarnFinal"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option != JOptionPane.YES_OPTION) {
            enableUI(true);

            return;
        }

        command = new MultiFindAndReplaceCommand("Menu.Search.FindReplaceMulti");
        command.setProcessReport(new SimpleReport(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Report")));
        command.addProgressListener(this);
        command.execute(null,
            new Object[] {
                searchFiles, selTiers, searchPattern, replPattern, regEx,
                caseSens
            });
        isRunning = true;
        startStopButton.setText(ElanLocale.getString("Button.Stop"));
    }

    private void stop() {
        if (command != null) {
            command.interrupt();

            // wait for call to progress interrupted
        }
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Button actions.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            if (!isRunning) {
                setVisible(false);
                dispose();
            }
        } else if (e.getSource() == domainButton) {
            if (!isRunning) {
                updateFileList();
                checkStartStopState();
            }
        } else if (e.getSource() == allTiersRB) {
            selTiersButton.setEnabled(false);
            loadTiersBar.setEnabled(false);
        } else if (e.getSource() == selectedTiersRB) {
            selTiersButton.setEnabled(true);
            loadTiersBar.setEnabled(true);
        } else if (e.getSource() == selTiersButton) {
            if (!isRunning) {
                selectTiers();
            }
        } else if (e.getSource() == startStopButton) {
            if (!isRunning) {
                enableUI(false);
                // start thread
                start();
            } else {
                // stop running thread
                stop();
            }
        } else if (e.getSource() == findField) {
            checkStartStopState();
        }
    }

    /**
     * Sets the Locale to the original input context's locale.
     *
     * @param e focus event
     */
    @Override
	public void focusGained(FocusEvent e) {
        if (orgLocale != null) {
            e.getComponent().setLocale(orgLocale);
        }
    }

    /**
     * Hides a input method component if necessary.
     *
     * @param e the focus event
     */
    @Override
	public void focusLost(FocusEvent e) {
        // ImUtil.setLanguage(e.getComponent(), Locale.getDefault());// works, is ugly
        // hide virtual keyboard
        if (e.getComponent().getInputContext() != null) {
            Object imObject = e.getComponent().getInputContext()
                               .getInputMethodControlObject();

            if ((imObject != null) && imObject instanceof GateIM) {
                ((GateIM) imObject).setMapVisible(false);
            }
        }
    }

    /**
     * Caret update in the find pattern textfield
     *
     * @param e the caret event
     */
    @Override
	public void caretUpdate(CaretEvent e) {
        checkStartStopState();
    }

    /**
     * Notification that the progress is completed.
     *
     * @param source the source
     * @param message the message
     */
    @Override
	public void progressCompleted(Object source, String message) {
        if (source instanceof TierLoadThread) {
            showTierSelectionDialog();
            selTiersButton.setEnabled(true);
            loadTiersBar.setValue(0);
            loadTiersBar.setString("");
        } else {
            //popup a message and show report
            progressBar.setValue(100);
            progressBar.setString(message);

            // or only show report
            if ((command != null) && (command.getProcessReport() != null)) {
                new ReportDialog(this, command.getProcessReport()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("Message.Complete"), "",
                    JOptionPane.INFORMATION_MESSAGE);
            }

            // update ui, set running state
            enableUI(true);
            isRunning = false;
            startStopButton.setText(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace"));
            progressBar.setValue(0);
            progressBar.setString("");
        }
    }

    /**
     * Shows message and or report, updates ui elements.
     *
     * @param source the source
     * @param message the message
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        if (source instanceof TierLoadThread) {
            showTierSelectionDialog();
            selTiersButton.setEnabled(true);
            loadTiersBar.setValue(0);
            loadTiersBar.setString("");
        } else {
            // popup a message and show report
            progressBar.setString(message);
            showWarningDialog(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Warn5"));

            if ((command != null) && (command.getProcessReport() != null)) {
                new ReportDialog(this, command.getProcessReport()).setVisible(true);
            }

            // show report
            enableUI(true);
            isRunning = false;
            startStopButton.setText(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace"));
            progressBar.setValue(0);
            progressBar.setString("");
        }
    }

    /**
     * Updates the progress bar.
     *
     * @param source the source
     * @param percent the percentage completed
     * @param message a message to display
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if (source instanceof TierLoadThread) {
            loadTiersBar.setValue(percent);

            if (message != null) {
                loadTiersBar.setString(message);
            }
        } else {
            if (percent == progressBar.getMaximum()) {
                progressCompleted(source, message);
            } else {
                progressBar.setValue(percent);
                progressBar.setString(message);
            }
        }
    }

    ////////// thread for loading tier names ///////////////////////////
    /**
     * Loads unique tier names from the selected files.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    private class TierLoadThread extends Thread {
        /** Holds value of property DOCUMENT ME! */
        /**
         * Holds value of property DOCUMENT ME!
         */
        /**
         * the list to add tier names to
         */
        List<String> loadedTierNames = null;

        /**
         * Creates a new TierLoadThread instance
         *
         * @param loadedTierNames the list to add names to
         */
        public TierLoadThread(List<String> loadedTierNames) {
            super();
            this.loadedTierNames = loadedTierNames;
        }

        /**
         * Parses the files and extracts the tier names.
         */
        @Override
        public void run() {
            if ((searchFiles != null) && (searchFiles.length != 0)) {
                String fileName = null;
                TierImpl tier;
                float perFile = 100 / (float) searchFiles.length;
                int count = 0;

                for (File file : searchFiles) {
                    if (file == null) {
                        progressUpdated(this, (int) (++count * perFile), null);

                        continue;
                    }

                    fileName = file.getAbsolutePath();
                    fileName = FileUtility.pathToURLString(fileName).substring(5);

                    try {
                        EAFSkeletonParser parser = new EAFSkeletonParser(fileName);
                        parser.parse();

                        List<TierImpl> tiers = parser.getTiers();

                        for (int i = 0; i < tiers.size(); i++) {
                            tier = tiers.get(i);

                            if ((tier != null) &&
                                    !loadedTierNames.contains(tier.getName())) {
                                loadedTierNames.add(tier.getName());
                            }
                        }
                    } catch (ParseException pe) {
                        pe.printStackTrace();
                    }

                    progressUpdated(this, (int) (++count * perFile), null);
                }

                progressCompleted(this, null);
            }
        }
    }
}
