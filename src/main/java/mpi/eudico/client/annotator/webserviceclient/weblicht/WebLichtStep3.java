package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
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
 * A step that loads available tokenizer web services and allows the user to choose one.
 * Here also the default sentence duration can be set. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtStep3 extends StepPane implements ListSelectionListener {
	private JLabel sentenceDurationLabel;
	private JTextField sentenceDurationTF;
	
	private JLabel selectTokenizerLabel;
	private JList/*<WLServiceDescriptor>*/ serviceList;
	private DefaultListModel/*<WLServiceDescriptor>*/ model;
	private List<WLServiceDescriptor> wlDescList = null;
	private WebLichtHarvester harvester;
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle3a");
	}

	public WebLichtStep3(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

	/**
	 * Populates the panel with elements for selecting the services to call.
	 */
	@Override
	protected void initComponents() {
		super.initComponents();
		
		sentenceDurationLabel = new JLabel (ElanLocale.getString("WebServicesDialog.WebLicht.Duration"));
		sentenceDurationTF = new JTextField(12);
		sentenceDurationTF.setText("3000");
		selectTokenizerLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.Tokenizer"));
		
		model = new DefaultListModel/*<WLServiceDescriptor>*/();
		serviceList = new JList/*<WLServiceDescriptor>*/(model);
		serviceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceList.setCellRenderer(new WLDescriptorListRenderer());
		JScrollPane scrollPane = new JScrollPane(serviceList);
		scrollPane.setPreferredSize(new Dimension(100, 80));
		
    	setLayout(new GridBagLayout());
     	setBorder(new EmptyBorder(5, 10, 5, 10));
    	Insets insets = new Insets(2, 0, 2, 0);
    	Insets globalInsets = new Insets(5, 10, 5, 10);
    	//setBorder(new EmptyBorder(12, 12, 12, 12));
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = insets;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	gbc.gridwidth = 2;

    	add(selectTokenizerLabel, gbc);
    	
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(scrollPane, gbc);
    	
    	gbc.gridy = 2;
    	gbc.gridwidth = 1;
    	gbc.insets = new Insets(12, 0, 2, 0);
    	gbc.weightx = 0.0;
    	gbc.weighty = 0.0;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.anchor = GridBagConstraints.WEST;
    	add(sentenceDurationLabel, gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = new Insets(12, 4, 2, 0);;
    	add(sentenceDurationTF, gbc);
    	
    	// load prefs
    	Integer val = Preferences.getInt("WebLicht.SentenceDuration", null);
    	
    	if (val != null) {
    		sentenceDurationTF.setText(String.valueOf(val));
    	}
	}


	@Override
	public void enterStepForward() {
		fillTokenizerList();
		// maybe there was already one selected before?
		if (serviceList.getSelectedIndex() > -1) {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);			
		}
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
	}

	@Override
	public boolean leaveStepForward() {
		int duration = 3000;
		String durInput = sentenceDurationTF.getText();
		if (durInput != null) {
			try {
				duration = Integer.parseInt(durInput);
			} catch (NumberFormatException nfe) {
				
			}
		}
		multiPane.putStepProperty("SentenceDuration", duration);
		// store preferences
		if (duration != 3000 || Preferences.getInt("WebLicht.SentenceDuration", null) != null) {
			Preferences.set("WebLicht.SentenceDuration", duration, null);
		}
		// there should be a service selected 
		Object selValue = serviceList.getSelectedValue();
		if (selValue == null) {
			showWarning(ElanLocale.getString("WebServicesDialog.WebLicht.Warning3"));
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
			return false;
		} else {
			multiPane.putStepProperty("WLTokenizerDescriptor", selValue);
			Preferences.set("WebLicht.TokenizerDescriptor", 
					((WLServiceDescriptor) selValue).fullURL, null);
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

	private void fillTokenizerList() {
		//model.removeAllElements();
		//serviceList.removeListSelectionListener(this);
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
				
				// try to filter tokenizers 
				if (wlDescList != null) {
					for (WLServiceDescriptor descriptor : wlDescList) {
						if (descriptor.tcfInput && descriptor.sentenceOutput && descriptor.tokensOutput) {
							model.addElement(descriptor);
						}
					}
				}
				
				serviceList.addListSelectionListener(this);
				// load pref
				String tokUrl = Preferences.getString("WebLicht.TokenizerDescriptor", null);
				if (tokUrl != null) {
					
					for (int i = 0; i < model.getSize(); i++) {
						WLServiceDescriptor wlDesc = (WLServiceDescriptor) model.getElementAt(i);
						if (tokUrl.equals(wlDesc.fullURL)) {
							serviceList.setSelectedIndex(i);
							serviceList.scrollRectToVisible(serviceList.getCellBounds(i, i));
							break;
						}
					}
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
	}

	
	private void showWarning(String message) {
		JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (serviceList.getSelectedIndex() > -1) {
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		}	
	}
	
}
