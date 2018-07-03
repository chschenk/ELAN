package mpi.eudico.client.annotator.webserviceclient.tc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * In this panel the uploading of a transcription to TypeCraft can be
 * initiated.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class TypeCraftStep3 extends StepPane implements
		ActionListener {
	private JRadioButton tierListRB;
	private JRadioButton typeListRB;
	private JList tierList;
	private DefaultListModel tierModel;
	private JList typeList;
	private DefaultListModel typeModel;
	private JButton uploadButton;
	private TranscriptionImpl trans;
	private boolean tiersLoaded = false;
	
	public TypeCraftStep3(MultiStepPane multiPane) {
		super(multiPane);

		initComponents();
	}

    /**
     * Initialize buttons, textfield and lists etc.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
    	setLayout(new GridBagLayout());
    	Insets insets = new Insets(2, 4, 2, 4);
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = insets;
    	setBorder(new TitledBorder("Select tier or type"));
    	    		typeListRB = new JRadioButton("Select the phrase level type:");
		typeListRB.setSelected(true);
		typeListRB.addActionListener(this);
		tierListRB = new JRadioButton("Select the phrase level tier:");
		tierListRB.addActionListener(this);
		tierListRB.setEnabled(false);// for the time being only allow type based upload
		ButtonGroup ttGroup = new ButtonGroup();
		ttGroup.add(typeListRB);
		ttGroup.add(tierListRB);
		   		
		typeModel = new DefaultListModel();
		typeList = new JList(typeModel);
		typeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		tierModel = new DefaultListModel();
		tierList = new JList(tierModel);
		tierList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierList.setEnabled(false);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		add(typeListRB, gbc);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		add(new JScrollPane(typeList), gbc);
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		add(tierListRB, gbc);
		
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		add(new JScrollPane(tierList), gbc);
		
		uploadButton = new JButton("Upload text");
		uploadButton.addActionListener(this);
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		add(uploadButton, gbc);
    	
    }
    
    /**
     * Check if there is a transcription and if tiers already have been loaded.
     */
	@Override
	public void enterStepForward() {
		super.enterStepForward();
		if (!tiersLoaded) {
	    	// get the transcription
	    	Object transObj = multiPane.getStepProperty("transcription");
	    	if (transObj instanceof TranscriptionImpl) {
	    		trans = (TranscriptionImpl) transObj;
		    	loadTypeList();
		    	loadTierList();
	    	}

	    	tiersLoaded = true;
		}
	}

	/**
     * Loads all linguistic types. Maybe only the types with stereotype None and / or 
     * stereotype Symbolic Association? 
     */
    private void loadTypeList() {
    	if (trans != null && typeList != null) {
    		List<LinguisticType> types = trans.getLinguisticTypes();
    		LinguisticType lt;
    		for (int i = 0; i < types.size(); i++) {
    			lt = types.get(i);
    			typeModel.addElement(lt.getLinguisticTypeName());
    			if (lt.getLinguisticTypeName().equals("phrase")) {
    				typeList.setSelectedIndex(i);
    			}
    		}
    	}
    }

    /**
     * Loads the list of tiers.
     * Note: for phrase level it could be assumed that it is a root tier or 1-to-1 
     * relation depending tier.?
     */
    private void loadTierList() {
    	if (trans != null && tierList != null) {
    		List<TierImpl> tiers = trans.getTiers();
    		TierImpl t;
    		
    		for (int i = 0; i < tiers.size(); i++) {
    			t = tiers.get(i);
    			tierModel.addElement(t.getName());
    			if (t.getName().equals("phrase")) {
    				tierList.setSelectedIndex(i);
    			}
    		}
    	}
    }
    
	/**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return "TypeCraft upload text";
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == uploadButton) {
			if (typeListRB.isSelected()) {
				// upload all phrase tiers
				String typeName = (String) typeList.getSelectedValue();
				if (typeName != null) {
					TCEncoder tcenc = new TCEncoder();
					String result = tcenc.encodeTCTypeBased(trans, typeName);
				} else {
					// message
				}
			} else {
				// upload tier from phrase tier
				String tierName = (String) tierList.getSelectedValue();
				if (tierName != null) {
					
				} else {
					// message
				}
			}
		} else if (e.getSource() == typeListRB) {
			typeList.setEnabled(true);
			tierList.setEnabled(false);
		} else if (e.getSource() == tierListRB) {
			typeList.setEnabled(false);
			tierList.setEnabled(true);			
		}

	}

	private String encodeText(List<String> tiers) {
		// encode all phrase tiers in TC XML and upload
		return null;
	}
}
