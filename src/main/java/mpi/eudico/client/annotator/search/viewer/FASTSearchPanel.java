package mpi.eudico.client.annotator.search.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.prefs.MultipleFileDomains;
import mpi.eudico.client.annotator.search.model.EAFType;
import mpi.eudico.client.annotator.search.model.FASTSearchEngine;
import mpi.eudico.client.annotator.search.result.viewer.ContentMatch2TabDelimitedText;
import mpi.eudico.client.annotator.search.result.viewer.EAFMultipleFileResultViewer;
import mpi.eudico.client.annotator.search.result.viewer.EAFResultViewerTableModel;
import mpi.eudico.client.util.LinkButton;
import mpi.search.SearchLocale;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentMatchCounter;
import mpi.search.model.DefaultSearchController;
import mpi.search.query.model.Query;
import mpi.search.query.viewer.AbstractSimpleSearchPanel;
import mpi.search.query.viewer.StartStopPanel;

import org.xml.sax.SAXException;

/** 
 * The panel containing FAST
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @since June 2013
 */
public class FASTSearchPanel extends AbstractSimpleSearchPanel {
	private static final long serialVersionUID = 2833583409122787971L;

	/**DOCUMENT ME! */
    protected static final String LAST_DIR_KEY = "MultipleFileSearchLastDir";
    protected static final String PREFERENCES_DIRS_KEY = "MultipleFileSearchDirs";
    protected static final String PREFERENCES_PATHS_KEY = "MultipleFileSearchPaths";
    protected static final String PREFERENCES_LAST_DOMAIN = "LastUsedMFSearchDomain";
    
    private final Action defineDomainAction;
    private List<String> searchDirs;
    private List<String> searchPaths;
    private final Box optionBox = new Box(BoxLayout.Y_AXIS);
    private final Box searchCategoryBox = new Box(BoxLayout.X_AXIS);
    private final JCheckBox caseSensitiveCheckBox;
    private final JCheckBox regexCheckBox;
    private final JLabel searchLogo = new JLabel("FASTSearch");
    private final JLabel infoLabel = new JLabel();
    private final JLabel domainLabel = new JLabel();
    private final JPanel centralPanel;
    private final JTextField searchField = new JTextField(18);
    private final LinkButton exportButton;
    private File[] searchFiles;
    
    // store a copy of the engine instead of digging around in AbstractSimpleSearchPanel/DefaultSearchController...
    private final FASTSearchEngine FASTSearcher;

    /**
     * Creates a new EAFMultipleFileSearchPanel object.
     *
     * @param elanFrame DOCUMENT ME!
     */
    public FASTSearchPanel(ElanFrame2 elanFrame) {
        matchCounter = new ContentMatchCounter();

        defineDomainAction = new AbstractAction(SearchLocale.getString("Action.DefineDomain")) {
			private static final long serialVersionUID = -8606295445946585789L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// user clicked "define domains"
               	MFDomainDialog mfDialog = null;
               	Container w = FASTSearchPanel.this.getTopLevelAncestor();
               	if (w instanceof Frame) {
               		mfDialog = new MFDomainDialog((Frame) w, 
               			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
               	} else if (w instanceof Dialog) {
               		mfDialog = new MFDomainDialog((Dialog) w, 
               			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
               	} else {
               		return;
               	}

               	mfDialog.setSearchDirs(searchDirs);
                mfDialog.setSearchPaths(searchPaths);
                mfDialog.setVisible(true);
                searchDirs = mfDialog.getSearchDirs();
                searchPaths = mfDialog.getSearchPaths();
                searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs, searchPaths);

                // reset the domain label
                String val = Preferences.getString(PREFERENCES_LAST_DOMAIN, null);
            	if (val != null) {
            		domainLabel.setText(ElanLocale.getString("MultipleFileSearch.SelectedDomain") + " " + val);
            	}
            }
        };
        
        defineDomainAction.putValue(
            Action.SHORT_DESCRIPTION, SearchLocale.getString("Action.Tooltip.DefineDomain"));

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        defineDomainAction.putValue(Action.ACCELERATOR_KEY, ks);

        regexCheckBox = new JCheckBox(
                SearchLocale.getString("Search.Constraint.RegularExpression"));
        caseSensitiveCheckBox = new JCheckBox(
                SearchLocale.getString("Search.Constraint.CaseSensitive"));
        
        // default to case-sens search so we can utilize the faster search algo
        caseSensitiveCheckBox.setSelected(true);

        startStopPanel = new StartStopPanel(startAction, stopAction);
        resultViewer = new EAFMultipleFileResultViewer(elanFrame);
        exportButton = new LinkButton(exportAction);
        matchCounter.setHorizontalAlignment(JLabel.RIGHT);
        centralPanel = new JPanel(new BorderLayout());

        initComponents();
        makeInitialPanel();

        setVisible(true);

        searchField.addActionListener(startAction);

    	// check the last used domain
        String domainName = Preferences.getString(PREFERENCES_LAST_DOMAIN, null);
    	
    	if (domainName != null) {
    		domainLabel.setText(ElanLocale.getString("MultipleFileSearch.SelectedDomain") + " " + domainName);
    		
            Map<String, List<String>> domain = MultipleFileDomains.getInstance()
            		.getDomain(domainName);

			if (domain != null) {
				List<String> dirs = domain.get(domainName +
						MultipleFileDomains.DIR_SUF);
				
				if (dirs != null) {
					searchDirs = dirs;
					//dirs = new ArrayList<String>(0);
				}
				
				List<String> paths = domain.get(domainName +
						MultipleFileDomains.PATH_SUF);
				
				if (paths != null) {
					searchPaths = paths;
					//paths = new ArrayList<String>(0);
				}
			}
    	}
    	
    	if (searchDirs == null && searchPaths == null) {
	        // get defaults from preferences
    		List<String> listPref = Preferences.getListOfString(PREFERENCES_DIRS_KEY, null);
	        searchDirs = listPref != null ? listPref
	        		                      : new ArrayList<String>(0);
    		listPref = Preferences.getListOfString(PREFERENCES_PATHS_KEY, null);
	        searchPaths = listPref != null ? listPref 
	        		                       : new ArrayList<String>(0);

	        domainLabel.setText(ElanLocale.getString("MultipleFileSearch.SelectedDomain") + " " + 
	        ElanLocale.getString("MultipleFileSearch.SelectedDomainNone"));
    	} else {
    		if (searchDirs == null) {
    			searchDirs = new ArrayList<String>(0);
    		}
    		if (searchPaths == null) {
    			searchPaths = new ArrayList<String>(0);
    		}
    	}

    	FASTSearcher = new FASTSearchEngine(progressViewer);
        searchEngine = new DefaultSearchController(this, FASTSearcher);

        // construct the unique list of searchable files
        searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs, searchPaths);
    }

    public FASTSearchEngine getSearchController() {
    	return FASTSearcher;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension getPreferredSize() {
        return new Dimension(900, 600);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public File[] getSelectedFiles() {
        return searchFiles;
    }

    /**
     * This function doesn't seem to be called, so I can't determine
     * the type of the list elements.
     *
     * @return DOCUMENT ME!
     */
    public List getSelectedNodes() {
        return new ArrayList();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void executionStarted() {
        super.executionStarted();

        if (((JComponent) resultViewer).getWidth() == 0) {
            removeAll();
            makeResultsPanel();
            validate();
        }

        infoLabel.setText("");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void executionStopped() {
        super.executionStopped();
        infoLabel.setText(
            "in " + searchFiles.length + " files (" + (searchEngine.getSearchDuration() / 1000.0) +
            " seconds)");
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void handleException(Exception e) {
        //System.out.println(e.getClass().getName());
        if (e instanceof PatternSyntaxException) {
            JOptionPane.showMessageDialog(
                this, e.getMessage(), SearchLocale.getString("Search.Exception.Formulation"),
                JOptionPane.ERROR_MESSAGE);
            searchEngine.stopExecution();
        }
        else if (e instanceof SAXException || e instanceof IOException) {
            JOptionPane.showMessageDialog(
                this, e.getMessage(), SearchLocale.getString("Search.Exception.Parse"),
                JOptionPane.ERROR_MESSAGE);

            //don't stop execution!
        }
        else {
            super.handleException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Query getQuery() {
        AnchorConstraint ac = new AnchorConstraint(
                "", searchField.getText(), regexCheckBox.isSelected(),
                caseSensitiveCheckBox.isSelected());

        return new ContentQuery(ac, new EAFType(), searchFiles);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void export() {
        FileChooser fc = new FileChooser(this);
        fc.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, null, LAST_DIR_KEY);
  
        if (fc.getSelectedFile() != null) {
            try {
                ContentResult result = (ContentResult) searchEngine.getResult();

                //System.out.println(result.getQuery());
                ContentMatch2TabDelimitedText.exportMatches(
                    result.getMatches(), fc.getSelectedFile());
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    this, e.getMessage(), ElanLocale.getString("MultipleFileSearch.Message.ExportError"), 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initComponents() {
        setBackground(Color.white);
        searchLogo.setFont(new Font("Serif", 0, 36));
        searchLogo.setBorder(new EmptyBorder(5, 5, 5, 5));
        searchLogo.setForeground(Color.BLUE);
        searchField.setFont(searchField.getFont().deriveFont(16f));

        regexCheckBox.setOpaque(false);
        regexCheckBox.setFont(regexCheckBox.getFont().deriveFont(10f));
        regexCheckBox.setBackground(getBackground());
        
        caseSensitiveCheckBox.setOpaque(false);
        caseSensitiveCheckBox.setFont(caseSensitiveCheckBox.getFont().deriveFont(10f));
        caseSensitiveCheckBox.setBackground(getBackground());

        startStopPanel.setOpaque(false);

        exportButton.setFont(searchField.getFont().deriveFont(10f));
        exportButton.setEnabled(false);

        progressViewer.setOpaque(false);
        progressViewer.setBorder(
            new CompoundBorder(new EmptyBorder(3, 3, 3, 3), progressViewer.getBorder()));

        searchCategoryBox.setOpaque(false);
        searchCategoryBox.setBorder(new EmptyBorder(3, 0, 3, 0));
    }

    private void makeResultsPanel() {
    	exportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionBox.add(exportButton);

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(matchCounter);
        infoPanel.add(infoLabel);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(
            ((EAFMultipleFileResultViewer) resultViewer).getControlPanel(), BorderLayout.WEST);
        statusPanel.add(infoPanel, BorderLayout.EAST);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        add(searchLogo, c);

        c.anchor = GridBagConstraints.CENTER;
        add(centralPanel, c);
        add(startStopPanel, c);
        add(optionBox, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        add(progressViewer, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        add(new JSeparator(JSeparator.HORIZONTAL), c);

        c.insets = new java.awt.Insets(0, 0, 0, 0);
        add(statusPanel, c);

        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add((JComponent) resultViewer, c);

        ((EAFMultipleFileResultViewer) resultViewer).setColumnVisible(
            EAFResultViewerTableModel.TIMEPOINT, false);
    }

    private void makeInitialPanel() {
        setLayout(new GridBagLayout());

        LinkButton defineDomainButton = new LinkButton(defineDomainAction);
        defineDomainButton.setFont(searchField.getFont().deriveFont(10f));
        defineDomainButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionBox.add(defineDomainButton);
        domainLabel.setFont(searchField.getFont().deriveFont(10f));
        domainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionBox.add(domainLabel);       

        JPanel flagPanel = new JPanel(new GridLayout(1, 0));
        flagPanel.setOpaque(false);
        flagPanel.add(regexCheckBox);
        flagPanel.add(caseSensitiveCheckBox);

        centralPanel.setOpaque(false);
        centralPanel.add(searchCategoryBox, BorderLayout.NORTH);
        centralPanel.add(searchField, BorderLayout.CENTER);
        centralPanel.add(flagPanel, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new FlowLayout());
        panel.setOpaque(false);
        panel.add(centralPanel);
        panel.add(optionBox);

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        add(searchLogo, c);
        add(panel, c);
        add(startStopPanel, c);
    }
}
