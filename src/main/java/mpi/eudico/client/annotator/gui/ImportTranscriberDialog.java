 package mpi.eudico.client.annotator.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.transcriber.TranscriberDecoderInfo;

/**
 * A dialog for selection of a Transcriber file to import and for specifying import options.
 */
public class ImportTranscriberDialog extends ClosableDialog implements ActionListener {
    private Object value;
    private JPanel selectPanel;
    private JPanel optionPanel;
    private JPanel buttonPanel;
    private JButton selectButton;
    private JTextField fileField;
    private JRadioButton singleSpeakerTierRB;
    private JRadioButton tierPerSpeakerRB;
    private JButton okButton;
    private JButton cancelButton;
    
    /**
     * Creates an instance of ImportTranscriberDialog without making it visible.
     * showDialog has to be called to make the dailog visible and get a return value.
     *  
     * @param parent the parent frame
     */
    public ImportTranscriberDialog(Frame parent) {
        super(parent, true);
        initComponents();
    }
    
    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(2, 6, 2, 6);
        
        selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBorder(new TitledBorder(ElanLocale.getString("")));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        selectPanel.add(new JLabel(ElanLocale.getString("ImportDialog.Label.Transcriber")), gbc);
        
        fileField = new JTextField("", 20);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        selectPanel.add(fileField, gbc);
        
        selectButton = new JButton("...");
        selectButton.addActionListener(this);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        selectPanel.add(selectButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(selectPanel, gbc);
        
        optionPanel = new JPanel(new GridBagLayout());
        optionPanel.setBorder(new TitledBorder(ElanLocale.getString("ImportDialog.Label.Options ")));
        singleSpeakerTierRB = new JRadioButton(ElanLocale.getString("ImportDialog.Label.SingleSpeakerTier"));
        tierPerSpeakerRB = new JRadioButton(ElanLocale.getString("ImportDialog.Label.TierPerSpeaker"));
        tierPerSpeakerRB.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(tierPerSpeakerRB);
        group.add(singleSpeakerTierRB);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        optionPanel.add(tierPerSpeakerRB, gbc);
        gbc.gridy = 1;
        optionPanel.add(singleSpeakerTierRB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(optionPanel, gbc);
        
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(buttonPanel, gbc);
    }
    
    /**
     * Sets the dialog visible and blocks untill "Ok" or "Cancel" has been clicked (or untill the dialog 
     * is closed through the window close button). The created value is returned; it is either an 
     * TranscriberDecoderInfo object or null.
     * 
     * @return a TranscriberDecoderInfo object or null
     */
    public Object showDialog() {
        pack();
        //setVisible(true);
        setLocationRelativeTo(getParent());
        setVisible(true); //blocks
        dispose();
        return value;
    }
    
    /**
     * Checks whether an existing Transcriber file has been selected and creates en decoder info object.
     */
    private void createValueAndClose() {
        String path = fileField.getText();
        boolean nofile = false;
        
        if (path == null || path.length() == 0) {
            nofile =true;
        } else {
            File f = new File(path);
            if (!f.exists() || f.isDirectory()) {
                nofile = true;
            }
        }
        
        if (nofile) {
            String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
            strMessage += path;
            strMessage += ElanLocale.getString("Menu.Dialog.Message2");

            String strError = ElanLocale.getString("Message.Error");
            JOptionPane.showMessageDialog(this, strMessage, strError,
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //check if file is a '.trs' file
        if (path.toString().toLowerCase().endsWith(".trs") == false) {
            String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
            strMessage += path;
            strMessage += ElanLocale.getString("Menu.Dialog.Message3");

            String strError = ElanLocale.getString("Message.Error");
            JOptionPane.showMessageDialog(this, strMessage, strError,
                JOptionPane.ERROR_MESSAGE);

            return;
        }
        
        // replace all backslashes by forward slashes
        path = path.replace('\\', '/');
        TranscriberDecoderInfo tdInfo = new TranscriberDecoderInfo(path);
        tdInfo.setSingleSpeakerTier(singleSpeakerTierRB.isSelected());
        value = tdInfo;
        
        setVisible(false);
    }
    
    /**
     * Prompts the user to select a Transcriber file.
     */
    private void selectFile() {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("Button.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.TRANSCRIBER_EXT, false, "LastUsedTranscriberDir", FileChooser.FILES_ONLY, null);
        File f = chooser.getSelectedFile();
        if (f != null) {
         
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
     
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
       if (e.getSource() == selectButton) {
           selectFile();
       } else if (e.getSource() == okButton) {
           createValueAndClose();
       } else if (e.getSource() == cancelButton) {
           value = null;
           setVisible(false);
           //dispose();
       }
        
    }
}
