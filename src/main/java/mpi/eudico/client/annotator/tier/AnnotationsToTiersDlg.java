package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.client.annotator.gui.ClosableDialog;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * A dialog for customizing the annotations to tiers conversion action.
 *
 * @author Han Sloetjes
 */
public class AnnotationsToTiersDlg extends ClosableDialog
    implements ActionListener {
    private TranscriptionImpl transcription;

    // ui elements
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
    private JTable tierTable;
    private TierTableModel tierModel;
    private SpinnerNumberModel spinnerModel;
    private JSpinner maxNumSpinner;

    /**
     * Creates a new AnnotationsToTiersDlg instance
     *
     * @param owner the parent frame
     * @param transcription the transcription
     *
     * @throws HeadlessException
     */
    public AnnotationsToTiersDlg(Frame owner, TranscriptionImpl transcription) throws HeadlessException {
        super(owner);
        this.transcription = transcription;
        initComponents();
        extractTiers();
        postInit();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
       
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    private void initComponents() {
        //titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();

        tierModel = new TierTableModel(transcription.getTiers(), new String[]{TierTableModel.NAME});
        tierTable = new JTable(tierModel);
        tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(tierModel);
        tierTable.setRowSorter(rowSorter);
        
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc;

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(titleLabel, gbc);
        
     // add more elements to this panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        getContentPane().add(tierPanel, gbc); 
        
        // add elements to the optionspanel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;       
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(optionsPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc); 
        
        
        //tier Panel elements
       
        tierPanel.setLayout(new GridBagLayout());
        Dimension tableDim = new Dimension(450, 160);
        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;        
        tierPanel.add(tierScrollPane, gbc);        
      
        //option panel elements
        optionsPanel.setLayout(new GridBagLayout());
        insets.bottom = 3;
        JLabel maxNumLabel = new JLabel(ElanLocale.getString(
                    "AnnotationsToTiersDlg.Label.MaxNumTiers"));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        optionsPanel.add(maxNumLabel, gbc);
        
        spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        maxNumSpinner = new JSpinner(spinnerModel);
        gbc.gridx = 1;
        optionsPanel.add(maxNumSpinner, gbc);

        //button panel
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);        

        updateLocale();
    }

    private void updateLocale() {
        setTitle(ElanLocale.getString("AnnotationsToTiersDlg.Title"));
        titleLabel.setText(ElanLocale.getString("AnnotationsToTiersDlg.Title"));
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "FillGapsDialog.Label.SelectTiers")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Options")));
        
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * Extract all top-level tiers and fill the list.
     */
    private void extractTiers() {
        if (transcription != null) {
        	if (tierTable.getRowCount() > 0) {
        		tierTable.getSelectionModel().setSelectionInterval(0, 0);
        	} else {
        		startButton.setEnabled(false);
        	}
        }
    }

    /**
     * The action performed handling.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            setVisible(false);
            dispose();
        } else if (e.getSource() == startButton) {
            //get selected tier name from list
        	int selectedRow = tierTable.getSelectedRow();
            
            //if no tier has been selected, then warn the user
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("LabelAndNumberDialog.Warning.NoTier"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }
            
            int row = tierTable.convertRowIndexToModel(selectedRow);
            String tierName = (String) tierModel.getValueAt(row, 0);
            int maxNumTier = spinnerModel.getNumber().intValue();

           Command command = ELANCommandFactory.createCommand(transcription, 
        		   ELANCommandFactory.ANNOTATIONS_TO_TIERS);
           command.execute(transcription, new Object[]{tierName, maxNumTier});
           
        }
    }

}
