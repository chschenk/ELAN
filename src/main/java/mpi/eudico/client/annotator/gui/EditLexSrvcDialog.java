package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.lexicon.LexiconClientFactoryLoader;
import mpi.eudico.client.annotator.lexicon.LexiconConfigIO;
import mpi.eudico.client.annotator.lexicon.LexiconServiceStep1;
import mpi.eudico.client.annotator.lexicon.LexiconServiceStep2;
import mpi.eudico.client.annotator.lexicon.LexiconSrvCacheDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * In this dialog the user can add and delete Lexicon Links
 * @author Micha Hulsbosch
 *
 */
@SuppressWarnings("serial")
public class EditLexSrvcDialog extends ClosableDialog implements ActionListener, ItemListener {

	private static final int DEFAULT_MINIMUM_HEIGHT = 260;
    private static final int DEFAULT_MINIMUM_WIDTH = 700;
    
    private TranscriptionImpl transcription;
    
	private JPanel titlePanel;
	private JLabel titleLabel;

	private JPanel serviceInfoPanel;
	private JLabel serviceNameLabel;
	private JComboBox serviceNameBox;
	private JLabel serviceInfoLabel;
	private JTextArea serviceInfoText;

	private JPanel serviceButtonPanel;
	private JButton addServiceButton;
	private JButton editServiceButton;
	private JButton deleteServiceButton;
	private JButton importServiceButton;

	private JPanel closeButtonPanel;
	private JButton closeButton;
	
	private List<LexiconQueryBundle2> cacheBundles;
	

	public EditLexSrvcDialog(Transcription tr) {
		super(ELANCommandFactory.getRootFrame(tr), true);
		transcription = (TranscriptionImpl) tr;
		if (!transcription.isLexiconServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				ClientLogger.LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
			}
		}
		initComponents();
		postInit();
	}

	private void warnIfNoExtensions() {
		if (transcription.getLexiconServiceClientFactories() == null || transcription.getLexiconServiceClientFactories().isEmpty()) {
			addServiceButton.setEnabled(false);
			JOptionPane.showMessageDialog(this, ElanLocale.getString("LexiconServiceClient.NoClient"), 
    				"Warning", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void initComponents(){
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
			@Override
			public void windowOpened(WindowEvent w) {
				warnIfNoExtensions();
			}
		});

		GridBagConstraints gridBagConstraints;
		getContentPane().setLayout(new GridBagLayout());

		Insets insets = new Insets(2, 6, 2, 6);

		titlePanel = new JPanel();
		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titlePanel.add(titleLabel);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = insets;
		getContentPane().add(titlePanel, gridBagConstraints);

		serviceInfoPanel = new JPanel();
		serviceInfoPanel.setLayout(new GridBagLayout());

		serviceNameLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		serviceInfoPanel.add(serviceNameLabel, gridBagConstraints);

		serviceNameBox = new JComboBox();
		serviceNameBox.addItemListener(this);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = insets;
		serviceInfoPanel.add(serviceNameBox, gridBagConstraints);

		serviceInfoLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		serviceInfoPanel.add(serviceInfoLabel, gridBagConstraints);

		serviceInfoText = new JTextArea();
		//serviceInfoText.setContentType("text/html");
		serviceInfoText.setLineWrap(false);
		serviceInfoText.setWrapStyleWord(true);
		serviceInfoText.setEditable(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		serviceInfoPanel.add(new JScrollPane(serviceInfoText), gridBagConstraints);

		serviceButtonPanel = new JPanel();
		serviceButtonPanel.setLayout(new GridLayout(0, 1, 6, 6));

		addServiceButton = new JButton();
		addServiceButton.addActionListener(this);
		serviceButtonPanel.add(addServiceButton);

		importServiceButton = new JButton();
		importServiceButton.setEnabled(false);
		importServiceButton.addActionListener(this);
		serviceButtonPanel.add(importServiceButton);
		
		editServiceButton = new JButton();
		editServiceButton.setEnabled(false);
		editServiceButton.addActionListener(this);
		//serviceButtonPanel.add(editServiceButton);

		deleteServiceButton = new JButton();
		deleteServiceButton.setEnabled(false);
		deleteServiceButton.addActionListener(this);
		serviceButtonPanel.add(deleteServiceButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.insets = insets;
		serviceInfoPanel.add(serviceButtonPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(serviceInfoPanel, gridBagConstraints);
		
		closeButtonPanel = new JPanel();
		closeButtonPanel.setLayout(new GridLayout(1, 1, 0, 2));

		closeButton = new JButton();
		closeButton.addActionListener(this);

		closeButtonPanel.add(closeButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = insets;
		getContentPane().add(closeButtonPanel, gridBagConstraints);
	}

	private void postInit() {
		addCloseActions();
		updateLocale();
		setPosition();
		updateServiceNameBox();
		updateUIforSelectedService();
		checkConfigCache();
	}

	private void updateLocale() {
		setTitle(ElanLocale.getString("EditLexSrvcDialog.Title"));
		titleLabel.setText(ElanLocale.getString("EditLexSrvcDialog.Title"));
		serviceNameLabel.setText(ElanLocale.getString("EditLexSrvcDialog.Label.Servicename"));
		serviceInfoLabel.setText(ElanLocale.getString("EditLexSrvcDialog.Label.Serviceinfo"));
		addServiceButton.setText(ElanLocale.getString("EditLexSrvcDialog.Button.Add"));
		importServiceButton.setText(ElanLocale.getString("Button.Import"));
		editServiceButton.setText(ElanLocale.getString("EditLexSrvcDialog.Button.Edit"));
		deleteServiceButton.setText(ElanLocale.getString("EditLexSrvcDialog.Button.Delete"));
		closeButton.setText(ElanLocale.getString("EditLexSrvcDialog.Button.Close"));
	}

	private void updateServiceNameBox() {
		serviceNameBox.removeItemListener(this);
		
		ArrayList<LexiconLink> services = getServices();
		
		serviceNameBox.removeAllItems();
		for(int i = 0; i < services.size(); i++){
			serviceNameBox.addItem(services.get(i));
		}
		
		if(services.size() > 0){
			serviceNameBox.setSelectedIndex(0);
			
		}
		
		serviceNameBox.addItemListener(this);
	}
	
	private void updateUIforSelectedService() {
		if (serviceNameBox.getSelectedIndex() > -1) {
			LexiconLink link = (LexiconLink) serviceNameBox.getSelectedItem();
			String typeStr = ElanLocale.getString("EditLexSrvcDialog.Label.Type");
			String urlStr = ElanLocale.getString("EditLexSrvcDialog.Label.Url");
			String lexiconStr = ElanLocale.getString("EditLexSrvcDialog.Label.Lexicon");
			serviceInfoText.setText(typeStr + ": " + link.getLexSrvcClntType()
					+ "\n" + urlStr + ": " + link.getUrl() + "\n"+ lexiconStr + ": "
					+ link.getLexId().getName());
			if (link.getSrvcClient() != null) {
				editServiceButton.setEnabled(true);
			} else {
				editServiceButton.setEnabled(false);
			} 
			deleteServiceButton.setEnabled(true);
		} else {
			deleteServiceButton.setEnabled(false);
		}
	}
	
	/**
	 * Checks whether there are service configurations stored on the system, loads them if there are 
	 * any and enables the import button.
	 */
	private void checkConfigCache() {
		LexiconConfigIO lexIO = new LexiconConfigIO();
		try {
			cacheBundles = lexIO.readLexConfigs();
			
			if (cacheBundles != null && cacheBundles.size() > 0) {
				importServiceButton.setEnabled(true);
			}
		} catch (Exception ex) {
			// catch any exception that hasn't been caught yet
		}
	}
	
	/**
	 * Loads current cached services, adds the new one and saves the cache.
	 * 
	 * @param link the new lexicon link
	 */
	private void addToLocalCache(LexiconLink link) {
		if (link == null) {
			return;
		}
		
		LexiconConfigIO lexIO = new LexiconConfigIO();
		try {
			cacheBundles = lexIO.readLexConfigs();
			
			if (cacheBundles != null && cacheBundles.size() > 0) {
				// check if a bundle with the same name is already there?
				LexiconQueryBundle2 bundle = null;
				for (int i = 0; i < cacheBundles.size(); i++) {
					bundle = cacheBundles.get(i);
					if (link.getName().equals(bundle.getLinkName())) {
						//replace
						cacheBundles.remove(i);
						cacheBundles.add(i, new LexiconQueryBundle2(link, null));
						lexIO.writeLexConfigs(cacheBundles);
						return;
					}
				}
				// not yet in the list
				cacheBundles.add(new LexiconQueryBundle2(link, null));
			} else {
				cacheBundles = new ArrayList<LexiconQueryBundle2>(1);				
				cacheBundles.add(new LexiconQueryBundle2(link, null));
			}
			
			lexIO.writeLexConfigs(cacheBundles);
		} catch (Exception ex) {
			// catch any exception that hasn't been caught yet
			ClientLogger.LOG.warning("Error while loading lexicon services from cache: " + ex.getMessage());
		}
	}
	
	/**
	 * Creates a dialog that allows to select named services to add to the transcription.
	 */
	private void importServices() {
		LexiconSrvCacheDialog lexDialog = new LexiconSrvCacheDialog(this, true, cacheBundles);
		lexDialog.setVisible(true);
		
		List<LexiconQueryBundle2> selBundles = lexDialog.getSelectedBundles();
		if (selBundles == null || selBundles.size() == 0) {
			return;
		}
		
		LexiconLink lLink;
		Set<String> names = transcription.getLexiconLinks().keySet();
		LexiconClientFactoryLoader loader = new LexiconClientFactoryLoader();
		
		for (LexiconQueryBundle2 bundle : selBundles) {
			lLink = bundle.getLink();
			boolean alreadyThere = false;
			for (String name : names) {
				if (name.equals(lLink.getName())) {
					alreadyThere = true;
					break;
				}
			}
			if (!alreadyThere) {
				// load service client??
				loader.loadLexiconClientFactory(transcription, lLink);
				
    			Command com = ELANCommandFactory.createCommand(transcription,
    	                ELANCommandFactory.ADD_LEX_LINK);
    	        Object[] args = new Object[1];
    	        args[0] = lLink;

    	        com.execute(transcription, args);
			}
		}
		
		updateServiceNameBox();
		updateUIforSelectedService();
	}

	private ArrayList<LexiconLink> getServices() {
		return new ArrayList<LexiconLink>(transcription.getLexiconLinks().values());
	}

	/**
     * Pack, size and set location.
     */
    protected void setPosition() {
        pack();
        setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH),
            Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
        setLocationRelativeTo(getParent());
    }

	protected void closeDialog() {
		setVisible(false);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == addServiceButton) {
			showLexiconServiceWizard(null);
		} else if(ae.getSource() == editServiceButton) {
			showLexiconServiceWizard((LexiconLink) serviceNameBox.getSelectedItem());
		} else if(ae.getSource() == deleteServiceButton) {
			deleteService();
		} else if(ae.getSource() == importServiceButton) {
			importServices();
		} else if(ae.getSource() == closeButton) {
			closeDialog();
		}
	}

    private void deleteService() {
		LexiconLink link = (LexiconLink) serviceNameBox.getSelectedItem();
		if(link != null) {
			if (!showConfirmDialog(ElanLocale.getString("EditLexSrvcDialog.Message.Confirmdelete"))) {
                return;
            }
		}
		deleteService(link);
	}

	private void deleteService(LexiconLink link) {
		Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.DELETE_LEX_LINK);
        Object[] args = new Object[1];
        args[0] = link;

        com.execute(transcription, args);
		updateServiceNameBox();
		updateUIforSelectedService();
	}

	/**
	 * Shows the Lexicon Service Wizard and deals with the results
	 * @param oldLink
	 */
	private void showLexiconServiceWizard(LexiconLink oldLink) {
    	LexiconLink link = oldLink;

		MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
		
		StepPane step1 = new LexiconServiceStep1(pane, oldLink, transcription);
        StepPane step2 = new LexiconServiceStep2(pane);
        pane.addStep(step1);
        pane.addStep(step2);
        
        JDialog dialog = pane.createDialog(this, ElanLocale.getString("EditLexSrvcDialog.Title"), true);
        dialog.pack();
		int w = 500;
	    int h = 450;
	    dialog.setSize((dialog.getSize().width > w) ? w : dialog.getSize().width,
	        (dialog.getSize().height < h) ? h : dialog.getSize().height);
	    setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        link = (LexiconLink) pane.getStepProperty("newLink");
        if(link != null) {
    		if(oldLink == null) {
    			Command com = ELANCommandFactory.createCommand(transcription,
    	                ELANCommandFactory.ADD_LEX_LINK);
    	        Object[] args = new Object[1];
    	        args[0] = link;

    	        com.execute(transcription, args);
    	        
    	        // HS Jan 2011: add the new link to the local cache
    	        addToLocalCache(link);
    		} else { 
    			// Micha Hulsbosch: I decided to remove the Edit Lexicon Link function
    			// to avoid the process of finding a new Lexical Entry Field for every
    			// Lexicon Query Bundle that uses the changed Lexicon Link
//    			Command com = ELANCommandFactory.createCommand(transcription,
//    	                ELANCommandFactory.CHANGE_LEX_LINK);
//    	        Object[] args = new Object[1];
//    	        args[0] = oldLink;
//    	        args[1] = link;
//
//    	        com.execute(transcription, args);
    		}
    		updateServiceNameBox();
    		updateUIforSelectedService();
        }
	}    

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		updateUIforSelectedService();
	}
	
	 /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the messsage to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message, "Warning",
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }
}
