package mpi.eudico.client.annotator.webserviceclient.tc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * A step pane that allows to chose between downloading text from and 
 * uploading text to TypeCraft.
 * 
 * @author Han Sloetjes
 */
public class TypeCraftfInOutStep extends StepPane implements ActionListener{
	private JButton downloadButton;
	private JButton uploadButton;
	
	public TypeCraftfInOutStep(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

    /**
     * Initialize buttons for upload and download.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
    	downloadButton = new JButton("Download text from TypeCraft");
    	downloadButton.addActionListener(this);
    	uploadButton = new JButton("Upload text to TypeCraft");
    	uploadButton.addActionListener(this);
//    	uploadButton.setEnabled(false);
    	setLayout(new GridBagLayout());
    	
    	JPanel surroundPanel = new JPanel();
    	surroundPanel.setLayout(new GridBagLayout());
    	surroundPanel.setLayout(new GridLayout(2, 1, 4, 8));
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.NONE;
    	
    	surroundPanel.add(downloadButton);
    	surroundPanel.add(uploadButton);
    	
    	add(surroundPanel, gbc);
    }

    /**
     * Button event handling.
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == downloadButton) {
			multiPane.nextStep();
		} else {
			multiPane.goToStep("upload", true);
		}
		
	}

	@Override
	public String getStepTitle() {
		return "TypeCraft select to download or upload text";
	}
	
	
}
