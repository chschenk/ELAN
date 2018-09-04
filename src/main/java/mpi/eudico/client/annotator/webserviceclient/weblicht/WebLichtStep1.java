package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * The first step in contacting a WebLicht service. 
 * Choice to upload plain text or tiers.  
 */
@SuppressWarnings("serial")
public class WebLichtStep1 extends StepPane {
	private JRadioButton fromScratchRB;
	private JRadioButton uploadTiersRB;
	
	public WebLichtStep1(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

    /**
     * Initialize the panel.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
    	setLayout(new GridBagLayout());
    	fromScratchRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.PlainTextInput"), true);
    	uploadTiersRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.TierInput"));
    	ButtonGroup buttonGroup = new ButtonGroup();
    	buttonGroup.add(fromScratchRB);
    	buttonGroup.add(uploadTiersRB);
    	
    	setBorder(new EmptyBorder(5, 10, 5, 10));
    	JLabel label = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.Start"));
    	GridBagConstraints gbc = new GridBagConstraints();
    	Insets insets = new Insets(2, 0, 2, 0);
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	add(label, gbc);
    	
    	gbc.gridy = 1;
    	gbc.insets = new Insets(2, 20, 2, 0);
    	add(fromScratchRB, gbc);
//    	add(fromScratchB, gbc);
    	gbc.gridy = 2;
    	add(uploadTiersRB, gbc);
//    	add(uploadTiersB, gbc);
    	
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;
    	add(new JPanel(), gbc);
    	
    	// load prefs
    	String stringPref = Preferences.getString("WebLicht.UploadContents", null);
    	
    	if (stringPref != null) {
    		if ("text".equals(stringPref)) {
    			fromScratchRB.setSelected(true);//
    		} else if ("tier".equals(stringPref)) {
    			uploadTiersRB.setSelected(true);
    		}
    	}
    }

    /**
     * Returns the title
     */
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle1");
	}

	/**
	 * Store which radio button is selected.
	 * 
	 *  @return true
	 */
	@Override
	public boolean leaveStepForward() {
		if (fromScratchRB.isSelected()) {
			multiPane.putStepProperty("UploadContents", "Plain Text");
			Preferences.set("WebLicht.UploadContents", "text", null);
		} else {
			multiPane.putStepProperty("UploadContents", "Tiers");
			Preferences.set("WebLicht.UploadContents", "tier", null);
		}
		
		return true;
	}

	@Override
	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	@Override
	public void enterStepForward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	
	/**
	 * The next step depends on the selected radio button.
	 */
	@Override
	public String getPreferredNextStep() {
		if (fromScratchRB.isSelected()) {
			return "TextStep2";
		} else {
			return "TierStep2";
		}
	}
    
    
}
