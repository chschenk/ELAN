package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.TierTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.webserviceclient.weblicht.TCFConstants;

/**
 * This step allows to select the tier to upload and to specify what kind of 
 * content the tier represents (sentence or token). 
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class WebLichtTierBasedStep2 extends StepPane implements ItemListener, ListSelectionListener {
	private TierTableModel ttm;
	private JTable tierTable;
	private ComboBoxModel/*<String>*/ typeModel;
	private JComboBox/*<String>*/ typeCB;
	private boolean tiersLoaded = false;
	
	public WebLichtTierBasedStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}


	@Override
	protected void initComponents() {
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(5, 10, 5, 10));
		
		JLabel selectLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.SelectTier"));
		JLabel typeLabel = new JLabel( ElanLocale.getString("WebServicesDialog.WebLicht.SpecifyContentType"));
		ttm = new TierTableModel();
		// load the tiers the first time this pane becomes active
		tierTable = new JTable(ttm);
		JScrollPane tierScroll = new JScrollPane(tierTable);
		typeModel = new DefaultComboBoxModel/*<String>*/(); 
		typeCB = new JComboBox/*<String>*/(typeModel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(2, 0, 2, 0);
		gbc.insets = insets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 2;
		
		add(selectLabel, gbc);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(tierScroll, gbc);
		
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		add(typeLabel, gbc);
		
		gbc.gridx = 1;
		gbc.insets = new Insets(2, 4, 2, 0);
		add(typeCB, gbc);
	}

	/**
	 * The first time this pane is entered fill the tier table.
	 */
	@Override
	public void enterStepForward() {
		if (!tiersLoaded) {
			TranscriptionImpl trans = (TranscriptionImpl) multiPane.getStepProperty("transcription");
			
			if (trans != null) {
				List<TierImpl> tiers = trans.getTiers();
				
				for (int i = 0; i < tiers.size(); i++) {
					ttm.addRow(tiers.get(i));
				}
			}
			tierTable.getSelectionModel().addListSelectionListener(this);
			tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			typeCB.addItem(ElanLocale.getString("WebServicesDialog.WebLicht.SentenceInput"));
			typeCB.addItem(ElanLocale.getString("WebServicesDialog.WebLicht.TokenInput"));
			typeCB.addItemListener(this);
			
			// load prefs
			String stringPref = Preferences.getString("WebLicht.TierContentType", null);
			
			if (stringPref != null) {
				// default selection is index 0, sentence
				if (TCFConstants.TOKEN.equals(stringPref)) {
					typeCB.setSelectedIndex(1);
				}
			}
			
			stringPref = Preferences.getString("WebLicht.SelectedTier", null);
			
			if (stringPref != null) {
				String tierName = stringPref;
				int nameCol = ttm.findColumn(TierTableModel.NAME);
				
				for (int i = 0; i < ttm.getRowCount(); i++) {
					if (tierName.equals(ttm.getValueAt(i, nameCol))) {
						tierTable.setRowSelectionInterval(i, i);
						tierTable.scrollRectToVisible(tierTable.getCellRect(i, nameCol, true));
					}
				}
			}
			
			tiersLoaded = true;
		}
		// set enabled, warn if conditions are not met
		if (tierTable.getSelectedRow() > -1) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}
	
	/**
	 * (Re)enable the next button.
	 */
	@Override
	public void enterStepBackward() {
		super.enterStepBackward();
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
	}
	
	/**
	 * Return to the step of the choice between plain text and tier upload.
	 * 
	 * @return the id of the text/tier decision step
	 */
	@Override
	public String getPreferredPreviousStep() {
		return "TextOrTierStep1";
	}

	/**
     * Returns the title
     */
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle2b");
	}

	/**
	 * Note: this listener is probably not needed anymore.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {

			if (tierTable.getSelectedRow() < 0) {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			} else {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
			}
			
		}		
	}

	/**
	 * Checks whether a tier and a type are selected.
	 */
	@Override
	public boolean leaveStepForward() {
		if (tierTable.getSelectedRow() < 0) {
			// show message
			JOptionPane.showMessageDialog(this, "WebServicesDialog.WebLicht.Warning.SelectTier", 
					ElanLocale.getString("Message.Warning"), 
					JOptionPane.WARNING_MESSAGE, null);
			
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			return false;
		}
//		if (typeCB.getSelectedIndex() == 0) {
//			// show message
//			JOptionPane.showMessageDialog(this, "Please select the type of content to be uploaded.", "Warning", 
//					JOptionPane.WARNING_MESSAGE, null);
//			
//			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
//			return false;
//		}
		int selTypeIndex = typeCB.getSelectedIndex();
		String cType = selTypeIndex == 0 ? TCFConstants.SENT : TCFConstants.TOKEN;// only two variants now
		
		String tName = (String) tierTable.getValueAt(tierTable.getSelectedRow(), 0);
		multiPane.putStepProperty("Tier", tName);
		multiPane.putStepProperty("ContentType", cType);
		
		// store prefs
		Preferences.set("WebLicht.SelectedTier", tName, null);
		Preferences.set("WebLicht.TierContentType", cType, null);
		return true;
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = tierTable.getSelectedRow();
		if (row < 0) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		}
		
	}
	
	
}
