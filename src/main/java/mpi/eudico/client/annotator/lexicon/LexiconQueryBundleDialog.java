package mpi.eudico.client.annotator.lexicon;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import nl.mpi.lexiconcomponent.impl.LexiconContext;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.lexicon.lexcom.LexiconComponentClient;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Lets the user select an existing Lexicon Link and select a Lexical Entry Field
 * of that Lexicon Link and combines that to a LexiconQueryBundle
 * @author Micha Hulsbosch
 */
@SuppressWarnings("serial")
public class LexiconQueryBundleDialog extends ClosableDialog implements
		ActionListener, ListSelectionListener {
	
	private JLabel lexiconLinkLabel;
	private JComboBox lexiconLinkComboBox;
	private JTable lexiconEntryFieldTable;
	private JScrollPane entryFieldScroller;
	private JButton okButton;
	private JButton cancelButton;
	private TranscriptionImpl transcription;
	private LexiconQueryBundle2 oldQueryBundle;
	private boolean canceled;
	/** value for no connection! */
    private String none;
    
    private HashMap<String, LexiconEntryTableModel> fieldLists;
	private JLabel titleLabel;
	private JPanel lexiconEntryFieldPanel;
	
	private Component parent;

	public LexiconQueryBundleDialog(Dialog dialog, boolean modal, Transcription trans) {
		super(dialog, modal);
		this.parent = dialog;
		this.transcription = (TranscriptionImpl) trans;
		if (!transcription.isLexiconServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
				}
			}
		}
		fieldLists = new HashMap<String, LexiconEntryTableModel>();
		canceled = true;
		initComponents();
		postInit();
	}

	public LexiconQueryBundleDialog(Dialog dialog, boolean modal, Transcription trans,
			LexiconQueryBundle2 queryBundle) {
		super(dialog, modal);
		this.parent = dialog;
		this.transcription = (TranscriptionImpl) trans;
		if (!transcription.isLexiconServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
				}
			}
		}
		this.oldQueryBundle = queryBundle;
		fieldLists = new HashMap<String, LexiconEntryTableModel>();
		canceled = true;
		initComponents();
		postInit();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
			@Override
			public void windowOpened(WindowEvent evt) {
				fillEntryFieldTable();
			}
		});
		
		titleLabel = new JLabel();
		lexiconLinkLabel = new JLabel();
		lexiconLinkComboBox = new JComboBox();
		lexiconLinkComboBox.addActionListener(this);
		lexiconEntryFieldPanel = new JPanel();
		lexiconEntryFieldPanel.setLayout(new GridBagLayout());
		lexiconEntryFieldPanel.setBorder(new TitledBorder(""));
		lexiconEntryFieldTable = new JTable();
		lexiconEntryFieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lexiconEntryFieldTable.getSelectionModel().addListSelectionListener(this);
		
		entryFieldScroller = new JScrollPane(lexiconEntryFieldTable);
		
		okButton = new JButton();
		okButton.addActionListener(this);
		cancelButton = new JButton();
		cancelButton.addActionListener(this);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Insets insets = new Insets(6, 6, 6, 6);
		
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = insets;
        c.weightx = 1.0;
        c.gridwidth = 2;
        this.add(titleLabel, c);
		
        c.fill = GridBagConstraints.NONE;
		c.insets = insets;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		this.add(lexiconLinkLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 1;
		this.add(lexiconLinkComboBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		lexiconEntryFieldPanel.add(entryFieldScroller, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		this.add(lexiconEntryFieldPanel, c);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		c.insets = insets;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.SOUTH;
		this.add(buttonPanel, c);

	}

	private void postInit() {
		updateLocale();
		fillLexiconLinkCombo();
		setLocationRelativeTo(getParent());
		updateButtons();
		
		pack();
		
		int w = 450;
	    int h = 550;
	    setSize((getSize().width < w) ? w : getSize().width,
	        (getSize().height < h) ? h : getSize().height);
	    setLocationRelativeTo(parent);
	}

	private void updateLocale() {
		this.setTitle(ElanLocale.getString("EditQueryBundle.Title"));
		titleLabel.setText(getTitle());
		lexiconLinkLabel.setText(ElanLocale.getString("EditQueryBundle.Label.Links"));
		lexiconEntryFieldPanel.setBorder(new TitledBorder(ElanLocale.getString("EditQueryBundle.Label.Entryfield")));
		okButton.setText(ElanLocale.getString("Button.OK"));
		cancelButton.setText(ElanLocale.getString("Button.Cancel"));
		none = ElanLocale.getString("EditQueryBundle.None");
		if (lexiconLinkComboBox.getItemCount() > 0) {
			lexiconLinkComboBox.removeItemAt(0);
		}
		lexiconLinkComboBox.insertItemAt(none, 0);
	}

	/**
	 * Checks whether a Lexical Entry Field is selected in the table
	 * and enables/disables the button accordingly
	 * @author Micha Hulsbosch
	 */
	private void updateButtons() {
		if((lexiconLinkComboBox.getSelectedIndex() > -1 && lexiconEntryFieldTable.getSelectedRow() > -1)) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	/**
	 * Fills the Lexicon Link Combobox with all Lexicon Links of the Transcription and the option
	 * to select none
	 * @author Micha Hulsbosch
	 */
	private void fillLexiconLinkCombo() {
		ArrayList<LexiconLink> lexiconLinks = new ArrayList(transcription.getLexiconLinks().values());
		lexiconLinkComboBox.removeActionListener(this);
		lexiconLinkComboBox.removeAllItems();

		//lexiconLinkComboBox.addItem(none);
		for(LexiconLink link : lexiconLinks) {
			lexiconLinkComboBox.addItem(link.getName());
		}
		
		// HS 08-2016 if there are no lexicon links, produce a LexiconComponent 
		// link for each available lexicon
		addLexiconComponentLinks();
		
		if(oldQueryBundle != null) {
			String linkName = oldQueryBundle.getLinkName();
			for(int i = 0; i < lexiconLinkComboBox.getItemCount(); i++) {
				if(linkName.equals(lexiconLinkComboBox.getItemAt(i))) {
					lexiconLinkComboBox.setSelectedIndex(i);
					break;
				}
			}
		} else if(lexiconLinkComboBox.getItemCount() > 0) {
			lexiconLinkComboBox.setSelectedIndex(0);
		} else {
			lexiconLinkComboBox.addItem(none);// ?? or show message
		}
		
		lexiconLinkComboBox.addActionListener(this);
	}
	
	/**
	 * The ELAN Lexicon Component is not an extension, it is always available. 
	 * If there are any lexicons (which is not necessarily the case) create links 
	 * for those.
	 */
	private void addLexiconComponentLinks() {
		String[] availabelLexs = LexiconContext.getInstance().getAvailableLexicons();
		if (availabelLexs.length > 0) {
			// iterate the names and check if links are already in the list
			// add them if not
			// TODO maybe create LexiconLink objects and add these to the transcription?
			for (String lexName : availabelLexs) {
				boolean alreadyThere = false;
				for (int i = 0; i < lexiconLinkComboBox.getItemCount(); i++) {
					if (lexName.equals(lexiconLinkComboBox.getItemAt(i))) {
						alreadyThere = true;
					}
				}
				if (!alreadyThere) {
					lexiconLinkComboBox.addItem(lexName);
				}
			}
		}
		
	}

	/** 
	 * Fills the Entry Field Table with the entry fields that belong to 
	 * the selected Lexicon Link
	 * If the entry fields are loaded before during the life-cycle of this 
	 * dialog, the entry fields are loaded from memory
	 * 
	 * @author Micha Hulsbosch
	 */
	private void fillEntryFieldTable() {
		lexiconEntryFieldTable.setModel(new LexiconEntryTableModel());
		if (lexiconLinkComboBox.getSelectedIndex() >= 0) {
			if (lexiconLinkComboBox.getSelectedItem() == none) {
				return;
			}
			List<LexiconLink> lexiconLinks = new ArrayList<LexiconLink>(
					transcription.getLexiconLinks().values());
			LexiconLink link = null;
			for(LexiconLink lnk2 : lexiconLinks) {
				if (lnk2.getName().equals(
						lexiconLinkComboBox.getSelectedItem())) {
					link = lnk2;
					break;
				}
			}
			// HS 08-2016 First check if the selected item is a LexiconComponent lexicon
			boolean isLexiconComponentLexicon = false;
			if (link == null) {// the selected string is probably a ELAN lexicon name, automatically added
				isLexiconComponentLexicon = fillEntryFieldTableLC(link, (String) lexiconLinkComboBox.getSelectedItem());
			} else if (LexiconComponentClient.CLIENT_TYPE.equals(link.getLexSrvcClntType())) {
				isLexiconComponentLexicon = fillEntryFieldTableLC(link, null);
			}
			
			if (isLexiconComponentLexicon) {
				return;
			}
			// end LexiconComponent
			
			if (link == null || link.getSrvcClient() == null) {
				JOptionPane.showMessageDialog(this, ElanLocale.getString("LexiconLink.NoClient"), 
						"Warning", JOptionPane.WARNING_MESSAGE);
			} else {
				if (!fieldLists.containsKey(lexiconLinkComboBox.getSelectedItem())) {
					boolean tryGetFieldIds = true;
					while (tryGetFieldIds) {
						try {
							ArrayList<LexicalEntryFieldIdentification> fldIds = link
							.getSrvcClient()
							.getLexicalEntryFieldIdentifications(link.getLexId());
							Collections.sort(fldIds);
							LexiconEntryTableModel tmpModel = new LexiconEntryTableModel();
							for (LexicalEntryFieldIdentification fldId : fldIds) {
								tmpModel.addRow(fldId);
							}
							lexiconEntryFieldTable.setModel(tmpModel);
							fieldLists.put(
									(String) lexiconLinkComboBox.getSelectedItem(),
									tmpModel);
							tryGetFieldIds = false;
						} catch (LexiconServiceClientException e) {
							if (e.getMessage().equals(LexiconServiceClientException.NO_USERNAME_OR_PASSWORD)
									|| e.getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
								LexiconLoginDialog loginDialog = new LexiconLoginDialog(
										this, link);
								loginDialog.setVisible(true);
								if (loginDialog.isCanceled()) {
									tryGetFieldIds = false;
								}
							} else {
								String title = ElanLocale.getString("LexiconLink.Action.Error");
								String message = title
								+ "\n"
								+ ElanLocale
								.getString("LexiconServiceClientException.Cause")
								+ " "
								+ e.getMessageLocale();
								JOptionPane.showMessageDialog(this,
										message, title,
										JOptionPane.ERROR_MESSAGE);
								tryGetFieldIds = false;
							}
						}
					}
					if (oldQueryBundle != null) {
						String fldIdName = oldQueryBundle.getFldId().getName();
						for (int i = 0; i < lexiconEntryFieldTable.getModel()
								.getRowCount(); i++) {
							if (fldIdName.equals(((LexiconEntryTableModel) lexiconEntryFieldTable
											.getModel()).getFldIdAtRow(i).getName())) {
								lexiconEntryFieldTable.setRowSelectionInterval(
										i, i);
								break;
							}
						}
					}
				} else {
					lexiconEntryFieldTable.setModel(fieldLists
							.get(lexiconLinkComboBox.getSelectedItem()));
				}
			}
		} 
	}
	
	/**
	 * Fills the table of lexical entry field names for a lexicon created in the 
	 * Lexicon Component, if the specified lexicon is found. Does so even if no
	 * lexicon link has been created/specified by the user.
	 * 
	 * @param lexLink not null if the link has been used or set before
	 * @param lexiconName the name of a lexicon in case it hasn't been used and 
	 * added to the transcription as a lexicon link
	 * 
	 * @return true if the lexicon name identifies a Lexicon Component lexicon or if
	 * the Lexicon Link is not null and its client is a LexiconComponentClient
	 */
	private boolean fillEntryFieldTableLC(LexiconLink lexLink, String lexiconName) {
		LexiconIdentification lexId = null;
		
		if (lexLink == null) {// means lexiconName != null
			lexId = new LexiconIdentification();			
			lexId.setName(lexiconName);
			lexId.setId(lexiconName);
		} else {
			lexId = lexLink.getLexId();
		}
		
		try {// try the Lexicon Service Client
			LexiconServiceClient lcClient = null;
			if (lexLink != null && lexLink.getSrvcClient() != null) {
				lcClient = lexLink.getSrvcClient();			
			} else {
				lcClient = new  LexiconComponentClient();
				// add it to the link?
			}
			
			List<LexicalEntryFieldIdentification> fields = lcClient.getLexicalEntryFieldIdentifications(lexId);
			
			if (fields != null) {
				// is it necessary to create a new model here?
				LexiconEntryTableModel tmpModel = new LexiconEntryTableModel();
				for (LexicalEntryFieldIdentification fldId : fields) {
					tmpModel.addRow(fldId);
				}
				lexiconEntryFieldTable.setModel(tmpModel);
				return true;
			} else {
				// log, message box
			}
		} catch (LexiconServiceClientException lsce) {
			// warning? or just log that it is not a lexicon component lexicon
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning(lsce.getMessage());
			}
			if (lexLink != null && LexiconComponentClient.CLIENT_TYPE.equals(lexLink.getLexSrvcClntType())) {
				// show warning message?
				return true; 
			}
			return false;
		}

		return false;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == okButton) {
			setCanceled(false);
			closeDialog();
		} else if (ae.getSource() == lexiconLinkComboBox) {
			fillEntryFieldTable();
			updateButtons();
		} else if (ae.getSource() == cancelButton){
			closeDialog();
		}
	}


	protected void closeDialog() {
		setVisible(false);
		dispose();
	}
	
	private void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return this.canceled;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		updateButtons();
	}

	/**
	 * Returns the Lexicon Query Bundle containing the name of the
	 * Lexicon Link and the Lexical Entry Field ID
	 * 
	 * @return the Lexicon Query Bundle
	 * @author Micha Hulsbosch
	 */
	public LexiconQueryBundle2 getBundle() {
		if (lexiconLinkComboBox.getSelectedIndex() >= 0 &&
				lexiconEntryFieldTable.getSelectedRow() > -1) {
			LexiconLink theLink = null;
			String linkName = (String) lexiconLinkComboBox.getSelectedItem();
			if (linkName.equals(none)) {
				return null;
			}
			ArrayList<LexiconLink> links = new ArrayList<LexiconLink>(transcription.getLexiconLinks().values());
			for(LexiconLink link : links) {
				if(link.getName().equals(linkName)) {
					theLink = link;
				}
			}
			
			if (theLink == null) {
				// situation of an automatically added LexiconComponent lexicon
				LexiconIdentification lid = new LexiconIdentification();
				lid.setId(linkName);
				lid.setName(linkName);
				theLink = new LexiconLink(linkName, LexiconComponentClient.CLIENT_TYPE, "", new LexiconComponentClient(), lid);
				transcription.addLexiconLink(theLink);
			}
			LexicalEntryFieldIdentification theFldId = ((LexiconEntryTableModel) lexiconEntryFieldTable.getModel()).getFldIdAtRow(
					lexiconEntryFieldTable.getSelectedRow());
			return new LexiconQueryBundle2(theLink, theFldId);		
		}
		
		return null;
	}

	private class LexiconEntryTableModel extends AbstractTableModel {
		
		ArrayList<LexicalEntryFieldIdentification> entryFields;
		private String[] columnNames;
		
		public LexiconEntryTableModel() {
			entryFields = new ArrayList<LexicalEntryFieldIdentification>();
			columnNames = new String[2];
			columnNames[0] = ElanLocale.getString("EditQueryBundle.Label.Name");//"Name"
			columnNames[1] = ElanLocale.getString("EditQueryBundle.Label.Description");//"Description";
		}
		
		public void addRow(LexicalEntryFieldIdentification entryField) {
			entryFields.add(entryField);
			fireTableRowsInserted(entryFields.size() - 1, entryFields.size() - 1);
		}
		
		@Override
		public String getColumnName(int col) {
	        return columnNames[col];
	    }
	
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
	
		@Override
		public int getRowCount() {
			return entryFields.size();
		}
	
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LexicalEntryFieldIdentification entryField = entryFields.get(rowIndex);
			if(columnIndex == 0) {
				return entryField.getName();
			} else if (columnIndex == 1) {
				return entryField.getDescription();
			}
			return null;
		}
		
		public LexicalEntryFieldIdentification getFldIdAtRow(int rowNumber) {
			return entryFields.get(rowNumber);
		}
		
	}

}
