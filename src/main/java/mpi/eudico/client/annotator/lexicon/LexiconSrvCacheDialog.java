package mpi.eudico.client.annotator.lexicon;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * A dialog showing the services in a global "cache": all services that have been 
 * defined before.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class LexiconSrvCacheDialog extends ClosableDialog implements 
ActionListener, ListSelectionListener{
	private List<LexiconQueryBundle2> cacheBundles;
    private DefaultListModel model;
    private JList lexServiceList;
    
    private JButton applyButton;
    private JButton removeButton;
    
    private List<LexiconQueryBundle2> results = null;

    /**
     * Constructor for a Frame parent.
     * @param owner
     * @param modal
     * @param cacheBundles
     * @throws HeadlessException
     */
	public LexiconSrvCacheDialog(Frame owner, boolean modal, List<LexiconQueryBundle2> cacheBundles)
			throws HeadlessException {
		super(owner, modal);
		this.cacheBundles = cacheBundles;
		initComponents();
	}

	/**
	 * Constructor for a Dialog parent.
	 * @param owner
	 * @param modal
	 * @param cacheBundles
	 * @throws HeadlessException
	 */
	public LexiconSrvCacheDialog(Dialog owner, boolean modal, List<LexiconQueryBundle2> cacheBundles)
			throws HeadlessException {
		super(owner, modal);
		this.cacheBundles = cacheBundles;
		initComponents();
	}
	
	/**
	 * Returns a list of bundles or null.
	 * 
	 * @return a list of bundles or null
	 */
	public List<LexiconQueryBundle2> getSelectedBundles() {
		return results;
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

	private void initComponents() {
		setTitle(ElanLocale.getString("EditLexSrvcDialog.Label.DefinedServices"));
        GridBagConstraints gbc;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        JPanel servicesPanel = new JPanel(new GridBagLayout());
        
        servicesPanel.setBorder(new TitledBorder(ElanLocale.getString(
        "EditLexSrvcDialog.Label.Servicename")));
        model = new DefaultListModel();
        if (cacheBundles != null) {
        	for (int i = 0; i < cacheBundles.size(); i++) {
        		model.addElement(cacheBundles.get(i));
        	}
        }
        lexServiceList = new JList(model);
        lexServiceList.addListSelectionListener(this);
        
        applyButton = new JButton(ElanLocale.getString("Button.Import"));
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);
		removeButton = new JButton(ElanLocale.getString("Button.Delete"));
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);
		
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        servicesPanel.add(new JScrollPane(lexServiceList), gbc);
        
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
        servicesPanel.add(buttonPanel, gbc);
        
        setContentPane(servicesPanel);
        postInit();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == applyButton) {
			Object[] sels = lexServiceList.getSelectedValues();
			
			if (sels != null && sels.length > 0) {
				results = new ArrayList<LexiconQueryBundle2>(sels.length);
				for (int i = 0; i < sels.length; i++) {
					results.add((LexiconQueryBundle2) sels[i]);
				}
			}
			setVisible(false);
			dispose();
		} else if (e.getSource() == removeButton) {
			int [] selIndices = lexServiceList.getSelectedIndices();
			
			if (selIndices != null && selIndices.length > 0) {
	        	// ask confirmation
	        	int option = JOptionPane.showConfirmDialog(this, 
	        			ElanLocale.getString("EditLexSrvcDialog.Message.Confirmdelete"), 
	        			ElanLocale.getString("Message.Warning"), JOptionPane.YES_NO_OPTION);
	        	if (option != JOptionPane.YES_OPTION) {
	        		return;
	        	}
	        	// delete and save??
	        	lexServiceList.removeListSelectionListener(this);
	        	for (int i = selIndices.length - 1; i >= 0; i--) {
	        		int index = selIndices[i];
	        		model.removeElementAt(index);
	        		cacheBundles.remove(index);
	        	}
	        	lexServiceList.clearSelection(); //superfluous...
	        	lexServiceList.addListSelectionListener(this);
	        	applyButton.setEnabled(false);
	        	removeButton.setEnabled(false);
	        	
	        	LexiconConfigIO lexIO = new LexiconConfigIO();
	        	
	        	try {
	        		lexIO.writeLexConfigs(cacheBundles);
	        	} catch (Exception ex) {
	        		// any uncaught exception
	        		ClientLogger.LOG.warning("Error while writing the file: " + ex.getMessage());
	        	}
			}
		}
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == lexServiceList) {
            boolean sel = lexServiceList.getSelectedIndices().length > 0;
            applyButton.setEnabled(sel);
            removeButton.setEnabled(sel);
        }
		
	}

}
