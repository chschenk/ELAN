package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.prefs.MultipleFileDomains;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFileUtilities;
import mpi.eudico.client.annotator.smfsearch.IMDISessionParser;
import mpi.eudico.client.annotator.smfsearch.ImdiSearchServiceParser;
import mpi.eudico.client.annotator.util.FileExtension;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.xml.sax.SAXException;


/**
 * A dialog for creating, loading, deleting multiple file (search) 
 * domains.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class MFDomainDialog extends JDialog implements ActionListener,
    MouseListener, ListSelectionListener {
    private List<String> searchDirs;
    private List<String> searchPaths;
    private String curDomainName = "";
    private DefaultListModel model;
    private JList domainList;
    private JButton newButton;
    private JButton fromIMDIButton;
    private JButton applyButton;
    private JButton removeButton;

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent frame
     *
     * @throws HeadlessException 
     */
    public MFDomainDialog(Frame owner) throws HeadlessException {
        super(owner);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent dialog
     *
     * @throws HeadlessException
     */
    public MFDomainDialog(Dialog owner) throws HeadlessException {
        super(owner);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent frame
     * @param modal blocking flag
     *
     * @throws HeadlessException 
     */
    public MFDomainDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent frame
     * @param title title
     *
     * @throws HeadlessException 
     */
    public MFDomainDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent dialog
     * @param modal blocking flag
     *
     * @throws HeadlessException
     */
    public MFDomainDialog(Dialog owner, boolean modal)
        throws HeadlessException {
        super(owner, modal);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent dialog
     * @param title title
     *
     * @throws HeadlessException
     */
    public MFDomainDialog(Dialog owner, String title) throws HeadlessException {
        super(owner, title);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent frame
     * @param title title
     * @param modal blocking flag
     *
     * @throws HeadlessException
     */
    public MFDomainDialog(Frame owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        initComponents();
    }

    /**
     * Creates a new MFDomainDialog instance
     *
     * @param owner the parent dialog
     * @param title title
     * @param modal blocking flag
     *
     * @throws HeadlessException
     */
    public MFDomainDialog(Dialog owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        initComponents();
    }

    /**
     * Initializes ui components and reads the initial list of stored
     * domains.
     */
    protected void initComponents() {
        GridBagConstraints gbc;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        JPanel newPanel = new JPanel(new GridBagLayout());
        JPanel domainsPanel = new JPanel(new GridBagLayout());

        newButton = new JButton(ElanLocale.getString(
                    "MultipleFileSearch.NewDomain"));
        newButton.addActionListener(this);
        newPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MultipleFileSearch.NewDomainSpecify")));

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        newPanel.add(newButton, gbc);

        fromIMDIButton = new JButton(ElanLocale.getString(
                    "MultipleFileSearch.NewDomainFromIMDI"));
        fromIMDIButton.addActionListener(this);
        gbc.gridy = 1;
        newPanel.add(fromIMDIButton, gbc);
        gbc.gridy = 0;
        getContentPane().add(newPanel, gbc);

        domainsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MultipleFileSearch.ExistingDomain")));
        model = new DefaultListModel();
        loadDomainList();
        domainList = new JList(model);
        domainList.addListSelectionListener(this);
        //domainList.getSelectionModel().setSelectionMode(
        //		ListSelectionModel.SINGLE_SELECTION);
        domainList.addMouseListener(this);
        applyButton = new JButton(ElanLocale.getString(
                    "MultipleFileSearch.Load"));
        applyButton.addActionListener(this);
        applyButton.setEnabled(false);
        removeButton = new JButton(ElanLocale.getString("Button.Delete"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(this);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        domainsPanel.add(new JScrollPane(domainList), gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.add(removeButton);
        buttonPanel.add(applyButton);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridy = 1;
        domainsPanel.add(buttonPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridy = 1;
        getContentPane().add(domainsPanel, gbc);

        postInit();
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();

        int w = 280;
        int h = 450;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    /**
     * Loads/reloads the domain list.
     *
     */
    private void loadDomainList() {
        if (model != null) {
            model.removeAllElements();

            List<String> currDoms = MultipleFileDomains.getInstance().getDomainList();

            for (int i = 0; i < currDoms.size(); i++) {
                model.addElement(currDoms.get(i));
            }
        }
    }

    /**
     * Returns the selected or loaded file paths
     *
     * @return the selected or loaded file paths
     */
    public List<String> getSearchPaths() {
        if (searchPaths == null) {
            return new ArrayList<String>(1);
        }

        return searchPaths;
    }

    /**
     * Returns the selected or loaded directories
     *
     * @return the selected or loaded directories
     */
    public List<String> getSearchDirs() {
        if (searchDirs == null) {
            return new ArrayList<String>(1);
        }

        return searchDirs;
    }

    /**
     * Sets the current directories.
     *
     * @param searchDirs a list of the current directories
     */
    public void setSearchDirs(List<String> searchDirs) {
        this.searchDirs = searchDirs;
    }

    /**
     * Sets the current paths
     *
     * @param searchPaths a list of the current paths
     */
    public void setSearchPaths(List<String> searchPaths) {
        this.searchPaths = searchPaths;
    }
    
    /**
     * @return the name of the loaded domain(s) or an empty string if the dialog
     * was cancelled or if there is an unnamed selection of files and/or folders
     */
    public String getDomainName() {
    	return curDomainName;
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
     * Prompts for a domain name, checks if it exists and asks whether to replace
     * and saves the domain. 
     */
    private void saveDomain() {
    	// prompt for a name
        String option = JOptionPane.showInputDialog(this,
                ElanLocale.getString("MultipleFileSearch.Message.Name"),
                ElanLocale.getString("MultipleFileSearch.SaveDomain"),
                JOptionPane.PLAIN_MESSAGE);

        if (option != null) {
            // update the stored domains preferences, check name collisions
            List currDoms = MultipleFileDomains.getInstance().getDomainList();

            if (currDoms.contains(option)) {
                String[] repOptions = new String[] {
                        ElanLocale.getString("Button.Yes"),
                        ElanLocale.getString("Button.No")
                    };
                int opt = JOptionPane.showOptionDialog(this,
                        ElanLocale.getString(
                            "MultipleFileSearch.Message.Replace"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, repOptions,
                        repOptions[0]);

                if (opt == JOptionPane.YES_OPTION) {
                    MultipleFileDomains.getInstance()
                                       .addDomain(option, searchDirs,
                        searchPaths);
                } else {
                    // rename
                    String tmp = option;

                    for (int count = 1; count < 40; count++) {
                        tmp = option + "-" + count;

                        if (!currDoms.contains(tmp)) {
                            MultipleFileDomains.getInstance()
                                               .addDomain(tmp, searchDirs,
                                searchPaths);
                            option = tmp;
                            break;
                        }
                    }
                }
            } else {
                // add to list
                MultipleFileDomains.getInstance()
                                   .addDomain(option, searchDirs, searchPaths);
            }
            curDomainName = option;
            
            Preferences.set("LastUsedMFSearchDomain", option, null, false, false);
        }
    }
    
    /**
     * Creates a new domain by showing a multiple file dialog, populated
     * with the current selection. Prompts to store the selection.
     *
     */
    private void createNewDomain() {
        if (searchDirs == null) {
            searchDirs = new ArrayList<String>(10);
        } else {
        	searchDirs = new ArrayList<String>(searchDirs);
        }

        if (searchPaths == null) {
            searchPaths = new ArrayList<String>(10);
        } else {
        	searchPaths = new ArrayList<String>(searchPaths);
        }

        // this also stores the last dirs and paths
        boolean specified = EAFMultipleFileUtilities.specifyDomain(this, searchDirs, searchPaths);
        
        if (specified) {
        	saveDomain();
        }
    }

    /**
     * Lets the user select an IMDI SearchService file (or any other Corpus file 
     * for that matter), extracts the links to Session files and returns a 
     * list of eaf files referenced from those session files.
     * Prompts to store the domain.
     */
    private void domainFromImdiSearch() {
        // open dialog to select an imdi SearchService file
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("MultipleFileSearch.SelectImdiFile"), FileChooser.OPEN_DIALOG, FileExtension.IMDI_EXT, "MultipleFileSearchLastDir");     
        File imdiFile = chooser.getSelectedFile();
       	if (imdiFile != null) {
            try {
            	ImdiSearchServiceParser ssParser = new ImdiSearchServiceParser(
            			imdiFile.getAbsolutePath());
            	List<String> corpPaths = ssParser.getSessionFiles();
            	if (corpPaths == null || corpPaths.size() == 0) {
            		showWarningMessage(ElanLocale.getString(
            				"MultipleFileSearch.Message.NoSessions"));
            		return;
            	}
            	int failedSessionFiles = 0;
            	ArrayList<String> eafPaths = new ArrayList<String>();
            	try {
            		IMDISessionParser sessParser = new IMDISessionParser();
            		List<String> tmpList = null;
            		String tmpPath;
            		for (int i = 0; i < corpPaths.size(); i++) {
            			try {
	            			tmpList = sessParser.parse(corpPaths.get(i));
	            			if (tmpList == null) {
	            				failedSessionFiles++;
	            				continue;
	            			}
	            			for (int j = 0; j < tmpList.size(); j++) {
	            				tmpPath = tmpList.get(j);
	            				if (!eafPaths.contains(tmpPath)) {
	            					eafPaths.add(tmpPath);
	            				}
	            			}
            			} catch (Exception ex) {
            				// a single file could not be parsed, keep a count of failed files
            				failedSessionFiles++;
            			}
            		}
            		if (failedSessionFiles > 0) {
            			showWarningMessage(ElanLocale.getString("MultipleFileSearch.Message.FailedSessions")
            					+ " " + failedSessionFiles);
            		}
            		if (eafPaths.size() == 0) {
            			showWarningMessage(ElanLocale.getString("MultipleFileSearch.Message.NoEAF"));
            			return;
            		}
            		// all went well
            		searchPaths = eafPaths;
            		searchDirs = new ArrayList<String>(0);//??
            	} catch (SAXException saxe) {
            		// warn, no session parser
            		showWarningMessage(ElanLocale.getString(
    					"MultipleFileSearch.Message.NoParser"));
            	}
            } catch (SAXException sax) {
        		showWarningMessage(ElanLocale.getString(
    					"MultipleFileSearch.Message.NoSearchLoad"));
            	return;
            } catch (IOException ioe) {
        		showWarningMessage(ElanLocale.getString(
    					"MultipleFileSearch.Message.NoSearchLoad"));
            	return;
            }
        } else {
            // don't close the dialog
        	return;
        }

        saveDomain();
        setVisible(false);
        dispose();
    }

    /**
     * Loads a saved domain.
     * 
     * @param domainName identifier of the domain
     */
    private void loadDomain(String domainName) {
        Map<String, List<String>> domain = MultipleFileDomains.getInstance()
                                                              .getDomain(domainName);

        if (domain != null) {
            List<String> dirs = domain.get(domainName +
                    MultipleFileDomains.DIR_SUF);

            if (dirs == null) {
                dirs = new ArrayList<String>(0);
            }

            List<String> paths = domain.get(domainName +
                    MultipleFileDomains.PATH_SUF);

            if (paths == null) {
                paths = new ArrayList<String>(0);
            }

            // pass the lists or create copies?
            searchDirs = dirs;
            searchPaths = paths;
            curDomainName = domainName;
            Preferences.set("LastUsedMFSearchDomain", domainName, null, false, false);
            // close the dialog
            setVisible(false);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("MultipleFileSearch.Message.NoLoad"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads all directories and files from the specified domains.
     * This merges the domains temporarily.
     *  
     * @param domainNames the selected domains
     */
    private void loadDomains(Object[] domainNames) {
        if ((domainNames == null) || (domainNames.length == 0)) {
            return;
        }

        List<String> allPaths = new ArrayList<String>();
        List<String> allDirs = new ArrayList<String>();
        StringBuilder domainSb = new StringBuilder();

        for (int i = 0; i < domainNames.length; i++) {
            String name = (String) domainNames[i];
            Map<String, List<String>> domain = MultipleFileDomains.getInstance().getDomain(name);

            if (domain != null) {
            	List<String> dir = domain.get(name + MultipleFileDomains.DIR_SUF);

                if (dir != null) {
                    for (int j = 0; j < dir.size(); j++) {
                        String elem = dir.get(j);

                        if (!allDirs.contains(elem)) {
                            allDirs.add(elem);
                        }
                    }
                }

                List<String> path = domain.get(name + MultipleFileDomains.PATH_SUF);

                if (path != null) {
                    for (int j = 0; j < path.size(); j++) {
                        String elem = path.get(j);

                        if (!allPaths.contains(elem)) {
                            allPaths.add(elem);
                        }
                    }
                }
                domainSb.append(name);
                if (i < domainNames.length - 1) {
                	domainSb.append(',');
                }
            }
        }

        searchDirs = allDirs;
        searchPaths = allPaths;
        curDomainName = domainSb.toString();
        // close the dialog
        setVisible(false);
        dispose();
    }

    /**
     * Removes a stored domain from the preferences file.
     * 
     * @param domainNames the domains to remove
     */
    private void removeDomains(Object[] domainNames) {
        if ((domainNames != null) && (domainNames.length != 0)) {
        	// ask confirmation
        	int option = JOptionPane.showConfirmDialog(this, 
        			ElanLocale.getString("MultipleFileSearch.Message.Delete"), 
        			ElanLocale.getString("Message.Warning"), JOptionPane.YES_NO_OPTION);
        	if (option != JOptionPane.YES_OPTION) {
        		return;
        	}
        	
            for (int i = 0; i < domainNames.length; i++) {
                MultipleFileDomains.getInstance()
                                   .removeDomain((String) domainNames[i]);
            }

            loadDomainList();
        }
    }

    /**
     * The action handling
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newButton) {
            // create the new domain dialog
            createNewDomain();
        } else if (e.getSource() == fromIMDIButton) {
            // creat a load from IMDI dialog
            domainFromImdiSearch();
            return;
        } else if (e.getSource() == applyButton) {
            // check if there is something selected in the list and load
            // maybe allow multiple selection
            Object[] vals = domainList.getSelectedValues();

            if (vals != null) {
                if (vals.length == 1) {
                    loadDomain((String) vals[0]);
                } else {
                    loadDomains(vals);
                }
            } else {
                // message??
                return;
            }
        } else if (e.getSource() == removeButton) {
            Object[] vals = domainList.getSelectedValues();

            if (vals != null) {
                removeDomains(vals);
            }

            return; // don't close
        }

        setVisible(false);
        dispose();
    }

    /**
     * Supports double clicking a domain to load it.
     *
     * @param e the event
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            int selIndex = domainList.locationToIndex(e.getPoint());

            if (selIndex > -1) {
                String namedDomain = (String) model.elementAt(selIndex);

                if (namedDomain != null) {
                    loadDomain(namedDomain);

                    setVisible(false);
                    dispose();
                }
            }
        }
    }

    /**
     * Stub
     *
     * @param e 
     */
    @Override
	public void mouseEntered(MouseEvent e) {
        // stub		
    }

    /**
     * Stub
     *
     * @param e 
     */
    @Override
	public void mouseExited(MouseEvent e) {
        // stub		
    }

    /**
     * Stub
     *
     * @param e 
     */
    @Override
	public void mousePressed(MouseEvent e) {
        // stub		
    }

    /**
     * Stub
     *
     * @param e 
     */
    @Override
	public void mouseReleased(MouseEvent e) {
        // stub		
    }

    /**
     * Updates some buttons after a change in domain selection. 
     *
     * @param e the event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == domainList) {
            boolean sel = domainList.getSelectedIndices().length > 0;
            applyButton.setEnabled(sel);
            removeButton.setEnabled(sel);
        }
    }
}
