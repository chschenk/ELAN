package mpi.eudico.client.annotator.tier;

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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog that lets the user select 2 tiers for "rating" comparison.
 * 
 * @author Han Sloetjes
 *
 * @version Jan 2015: This dialog has been replaced by the multiple file, multiple step variant.
 * This class might be deleted?
 */
@SuppressWarnings("serial")
public class CompareAnnotatorsDlg extends ClosableDialog implements ActionListener{
	private TranscriptionImpl transcription;
    private TierTableModel model1;
    private TierTableModel model2;
    private JTable table1;
    private JTable table2;
    private JLabel firstLabel;
    private JLabel secLabel;
    private JLabel titleLabel;
    private JPanel tierPanel;
    private JButton closeButton;
    private JButton compareButton;
    private JPanel buttonPanel;
    
	/**
	 * Constructor. 
	 * 
	 * @param transcription the transcription containing the tiers
	 * 
	 * @param parent the parent frame
	 * @throws HeadlessException he
	 */
	public CompareAnnotatorsDlg(TranscriptionImpl transcription, Frame parent)
			throws HeadlessException {
		super(parent, true);
		this.transcription = transcription;
		initComponents();
		postInit();
	}
	
    private void postInit() {
        pack();
        //setSize((getSize().width < minimalWidth) ? minimalWidth : getSize().width,
        //    (getSize().height < minimalHeight) ? minimalHeight : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }
    
    /**
     * Initialize ui components etc.
     */
    public void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);
        
        // get the alignable tiers
        model1 = new TierTableModel(null,
                new String[] { TierTableModel.NAME, TierTableModel.TYPE });
        model2 = new TierTableModel(null,
                new String[] { TierTableModel.NAME, TierTableModel.TYPE });

        TierImpl ti;

        for (int i = 0; i < transcription.getTiers().size(); i++) {
            ti = (TierImpl) transcription.getTiers().get(i);

            if (ti.isTimeAlignable()) {
                model1.addRow(ti);
                model2.addRow(ti);
            }
        }

        table1 = new JTable(model1);
        table1.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table1.getSelectionModel().addListSelectionListener(this);
        table2 = new JTable(model2);
        table2.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table2.getSelectionModel().addListSelectionListener(this);

        Dimension prdim = new Dimension(400, 150);
        JScrollPane p1 = new JScrollPane(table1);
        p1.setPreferredSize(prdim);

        JScrollPane p2 = new JScrollPane(table2);
        p2.setPreferredSize(prdim);

        firstLabel = new JLabel();
        secLabel = new JLabel();
        
        tierPanel = new JPanel();
        tierPanel.setLayout(new GridBagLayout());
        //tierPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;

        tierPanel.add(firstLabel, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        tierPanel.add(p1, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        tierPanel.add(secLabel, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        tierPanel.add(p2, gbc);
        
        // add tierpanel
        gbc.gridy = 1;
        getContentPane().add(tierPanel, gbc);
        // add buttons
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        compareButton = new JButton();
        closeButton = new JButton();
        
        compareButton.addActionListener(this);
        buttonPanel.add(compareButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);
        
        updateLocale();
    }
    
    private void updateLocale() {
    	setTitle(ElanLocale.getString("CompareAnnotatorsDialog.Title"));
    	titleLabel.setText(ElanLocale.getString("CompareAnnotatorsDialog.Title"));
    	tierPanel.setBorder(new TitledBorder(
    			ElanLocale.getString("ExportDialog.Label.SelectTiers")));
    	firstLabel.setText(ElanLocale.getString(
        	"CompareAnnotatorsDialog.Label.First"));
    	secLabel.setText(ElanLocale.getString(
        	"CompareAnnotatorsDialog.Label.Second"));
    	
        compareButton.setText(ElanLocale.getString("CompareAnnotatorsDialog.Label.Compare"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * The action vent handling. Compare or close.
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
			dispose();
		} else if (e.getSource() == compareButton) {
			// check if there are 2 tiers selected
			if (table1.getSelectedRow() == -1) {				
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("CompareAnnotatorsDialog.Message.First"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.WARNING_MESSAGE);
                
				return;
			}
			if (table2.getSelectedRow() == -1) {				
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("CompareAnnotatorsDialog.Message.Second"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.WARNING_MESSAGE);
                
				return;
			}
			
			String name1 = (String) table1.getValueAt(table1.getSelectedRow(), 
					model1.findColumn(TierTableModel.NAME));
			String name2 = (String) table2.getValueAt(table2.getSelectedRow(), 
					model2.findColumn(TierTableModel.NAME));
			// create a comparator utility
			AnnotatorCompareUtil acu = new AnnotatorCompareUtil();
			TableModel model = acu.getComparisonTable(transcription, name1, name2);
			if (model != null) {
				new CompareAnnotatorResultsDlg(this, model).setVisible(true);
			}
		}
		
	}

}
