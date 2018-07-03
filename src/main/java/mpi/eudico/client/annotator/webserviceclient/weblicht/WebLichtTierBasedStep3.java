package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;
import mpi.eudico.webserviceclient.weblicht.WebLichtHarvester;

/**
 * A step that loads available web services and allows the user to choose one.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtTierBasedStep3 extends StepPane implements ChangeListener, ListSelectionListener, 
DocumentListener {
	private JList/*<WLServiceDescriptor>*/ serviceList;
	private DefaultListModel/*<WLServiceDescriptor>*/ model;
	private String contentType;
	private JCheckBox manualURLCB;
	private JTextField manualURLTF;
	
	private List<WLServiceDescriptor> wlDescList = null;
	private WebLichtHarvester harvester;
	
	public WebLichtTierBasedStep3(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		setBorder(new EmptyBorder(5, 10, 5, 10));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(2, 0, 2, 0);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		add(new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.SelectService")), gbc);
		
		model = new DefaultListModel/*<WLServiceDescriptor>*/();
		serviceList = new JList/*<WLServiceDescriptor>*/(model);
		serviceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceList.setCellRenderer(new WLDescriptorListRenderer());
		JScrollPane scrollPane = new JScrollPane(serviceList);
		scrollPane.setPreferredSize(new Dimension(100, 80));
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(scrollPane, gbc);
		
		manualURLCB = new JCheckBox(ElanLocale.getString("WebServicesDialog.WebLicht.ManualService"));
		manualURLCB.addChangeListener(this);
		manualURLTF = new JTextField();
		manualURLTF.setEnabled(false);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		gbc.gridy = 2;
		add(manualURLCB, gbc);
		gbc.gridy = 3;
		add(manualURLTF, gbc);
		
		manualURLTF.getDocument().addDocumentListener(this);
		
		// load prefs
		Boolean boolPref = Preferences.getBool("WebLicht.CustomService", null);
		
		if (boolPref != null) {
			manualURLCB.setSelected(boolPref);
		}
		
		String stringPref = Preferences.getString("WebLicht.CustomServiceURL", null);
		if (stringPref != null) {
			manualURLTF.setText((String) stringPref);
		}
	}

	@Override
	public void enterStepForward() {
		super.enterStepForward();
		
		if (contentType == null) {
			contentType = (String) multiPane.getStepProperty("ContentType");
			fillListForType(/*contentType*/);
		} else {
			String oldContentType = contentType;
			contentType = (String) multiPane.getStepProperty("ContentType");
			if (contentType != null && !contentType.equals(oldContentType)) {
				fillListForType(/*contentType*/);
			}
			updateButtons();
		}
	}
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle3b");
	}
	
	private void showWarning(String message) {
		JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Fills the JList with the services that are (probably) useful and suitable to 
	 * process sentences and or tokens. At this moment no attempts are made to filter services
	 * for sentence- and token level separately. In practice in tcf these two types are 
	 * almost always combined; sentence depend on token elements and if there are tokens there
	 * most likely sentences as well. 
	 */
	private void fillListForType(/*String type*/) {
		model.removeAllElements();
		serviceList.removeListSelectionListener(this);
		// add services
		if (harvester == null) {
			harvester = new WebLichtHarvester();
		}
		// reuse existing list
		if (wlDescList == null) {
			// check when the services have been harvested remotely for the last time 
			try {
				String cachePath = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + harvester.CACHE_FILENAME;
				long lastCache = harvester.getLastCachingTime(cachePath);
				
				if (lastCache == 0 || System.currentTimeMillis() - lastCache > harvester.cacheReloadInterval) {
					String oaiResult = harvester.harvestServicesRemote();
					
					if (oaiResult != null) {
						wlDescList = harvester.parseRelevantServices(oaiResult);
						
						// cache locally
						harvester.storeCachedVersion(cachePath, oaiResult);
					}
				} else {
					// load from cache
					wlDescList = harvester.loadRelevantServicesFromCache(cachePath);
				}
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Unable to harvest and show relevant WebLicht services: " + ioe.getMessage());
				showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning1") + ": \n" + ioe.getMessage());
				return;
			} catch (Throwable th) {
				ClientLogger.LOG.warning("Unable to harvest and show relevant WebLicht services: " + th.getMessage());
				showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning1") + ": \n" + th.getMessage());
				return;
			}
		}

		if (wlDescList == null) {
			showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning1"));
			return;
		}
		
		// sentence level input and token level input
		for (WLServiceDescriptor descriptor : wlDescList) {
			if (descriptor.sentenceInput && descriptor.tokensInput && descriptor.tcfInput) {
				model.addElement(descriptor);
			}
		}

		// System.out.println("Number of services: " + model.getSize());
		serviceList.addListSelectionListener(this);
		// load prefs
		String servUrl = Preferences.getString("WebLicht.TierServiceDescriptor", null);
		
		if (servUrl != null) {
			
			for (int i = 0; i < model.getSize(); i++) {
				WLServiceDescriptor wlDesc = (WLServiceDescriptor) model.getElementAt(i);
				if (servUrl.equals(wlDesc.fullURL)) {
					serviceList.setSelectedIndex(i);
					serviceList.scrollRectToVisible(serviceList.getCellBounds(i, i));
					break;
				}
			}
		}
	}
	
	private void updateButtons() {
		if (manualURLCB.isSelected()) {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, !manualURLTF.getText().isEmpty());
		} else {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, serviceList.getSelectedIndex() > -1);
		}
	}

	/**
	 * Performs checks and stores selected properties.
	 */
	@Override
	public boolean leaveStepForward() {
		if (manualURLCB.isSelected()) {
			String manUrl = manualURLTF.getText();
			if (manUrl == null || manUrl.isEmpty()) {
				// warning
				ClientLogger.LOG.warning("No service url has been specified manually");
				showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning2"));
				manualURLTF.requestFocus();
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
				return false;
			} else {
				multiPane.putStepProperty("ManualServiceURL", manUrl);
				Preferences.set("WebLicht.CustomService", true, null);
				Preferences.set("WebLicht.CustomServiceURL", manUrl, null);
			}
		} else {
			// there should be a service selected 
			Object selValue = serviceList.getSelectedValue();
			if (selValue == null) {
				showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning3"));
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
				return false;
			} else {
				multiPane.putStepProperty("WLServiceDescriptor", selValue);
				Preferences.set("WebLicht.CustomService", false, null);
				Preferences.set("WebLicht.TierServiceDescriptor", 
						((WLServiceDescriptor) selValue).fullURL , null);
			}
		}
		
		return true;
	}

	/**
     * Delegates to {@link #leaveStepForward()}; Next and Finish both move to the 
     * Progress monitoring step in which the real work is done and monitored.  
     * 
     * @return {@link #leaveStepForward()}
     */
    @Override
	public boolean doFinish() {
    	
    	if (leaveStepForward()) {
    		multiPane.nextStep();
    		return false;
    	}
    	return false;
    }

	/**
	 * Updates the enabled state of the url textfield.
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		manualURLTF.setEnabled(manualURLCB.isSelected());
		serviceList.setEnabled(!manualURLCB.isSelected());
		
		updateButtons();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (serviceList.getSelectedIndex() > -1) {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		}
		
	}
	
	/**
	 * Document listening for enabling/disabling the Finish button
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, !manualURLTF.getText().isEmpty());		
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, !manualURLTF.getText().isEmpty());
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, !manualURLTF.getText().isEmpty());	
	}
}
