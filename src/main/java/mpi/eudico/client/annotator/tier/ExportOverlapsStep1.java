package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

@SuppressWarnings("serial")
public class ExportOverlapsStep1 extends StepPane implements MouseListener {
	private ArrayList<String> tierNames;
    protected DefaultTableModel model1;
    protected TierExportTableModel model2;
    protected JTable table1;
    protected JTable table2;
    protected JLabel firstLabel;
    protected JLabel secLabel;
    
    /** column id for the include in export checkbox column, invisible */
    protected final String SELECT_COLUMN = "select";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";
    
    /**
     * Constructor
     * @param multiPane
     */
	public ExportOverlapsStep1(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
		SwingUtilities.invokeLater(new LoadThread());
	}

	@Override
	protected void initComponents() {
		model1 = new DefaultTableModel();
        model2 = new TierExportTableModel();
        model1.setColumnIdentifiers(new String[] { TIER_NAME_COLUMN });
        model2.setColumnIdentifiers(new String[] { SELECT_COLUMN, TIER_NAME_COLUMN });

        table1 = new JTable(model1);
        table1.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table1.setTableHeader(null);
        table1.addMouseListener(this);
        
        table2 = new TierExportTable(model2);
        table2.addMouseListener(this);
        
        model1.addRow(new Object[]{"Loading tiers..."});
        
        Dimension prdim = new Dimension(120, 80);
        JScrollPane p1 = new JScrollPane(table1);
        p1.setPreferredSize(prdim);

        JScrollPane p2 = new JScrollPane(table2);
        p2.setPreferredSize(prdim);

        firstLabel = new JLabel(ElanLocale.getString(
                    "ExportOverlapsDialog.Label.RefTier"));
        secLabel = new JLabel(ElanLocale.getString(
                    "ExportOverlapsDialog.Label.OtherTiers"));

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;

        add(firstLabel, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p1, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        add(secLabel, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p2, gbc);
	}
	
	/**
	 * Extracts all unique tier names from the selected files.
	 */
	private void extractTierNames() {
		List<File> files = (List<File>) multiPane.getStepProperty("files");
		tierNames = new ArrayList<String>();
		if (files == null || files.size() == 0) {
			return;
		}
		TranscriptionImpl trans;
		Tier tier;
		String name;
		
		for (File f : files) {
			if (f == null) {
				continue;
			}
			try {
				trans = new TranscriptionImpl(f.getAbsolutePath());
				List<? extends Tier> tiers = trans.getTiers();
				
				for (int i = 0; i < tiers.size(); i++) {
					tier = tiers.get(i);
					name = tier.getName();
					if (!tierNames.contains(name)) {
						tierNames.add(name);
					}
				}
			} catch (Exception ex) {
				// catch any exception, io, parse etc
			}
		}
		Collections.sort(tierNames);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				model1.removeRow(0);
				
		        for (String tname : tierNames) {
		        	model1.addRow(new Object[] { tname });
		        	model2.addRow(Boolean.FALSE, tname);
		        }
			}});
	}
	
	
	/**
	 * Returns the selected tiers.
	 * 
	 * @return a list of selected tiers
	 */
    private List<String> getSelectedTiers2() {
    	return model2.getSelectedTiers();
    }
	
    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("ExportOverlapsDialog.SelectTiers");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
    	int row1 = table1.getSelectedRow();
    	if (row1 < 0) {
    		return false;
    	}
    	String refTier = (String) table1.getValueAt(row1, 0);
    	
    	List<String> selTiers2 = getSelectedTiers2();
    	selTiers2.remove(refTier);
    	// double check?
        if (selTiers2.size() > 0) {
            multiPane.putStepProperty("Tier-1", refTier);

            multiPane.putStepProperty("Tiers-2", selTiers2);

            return true;
        }

        return false;
    }	
    
    class LoadThread extends Thread {
    	
    	@Override
		public void run() {
    		extractTierNames();
    	}
    }

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
     * Checks if in both table one tier is selected and that they are not the
     * same.
     * 
     * The NEXT button should be enabled if
     * The upper table has a selection AND
     *     the lower table has a selection of a different tier
     * (if multiple tiers are selected, one of them may be the same,
     * that doesn't matter).
     * <p>
	 * Implementation note:
	 * use mouseReleased() since directly clicking on the checkboxes doesn't call valueChanged(),
	 * and dragging won't call mouseClicked().
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
    	int row1 = table1.getSelectedRow();    	
        boolean different = false;

        if (row1 >= 0) {
            int includeCol = model2.findColumn(SELECT_COLUMN);
	        int rowCount = model2.getRowCount();
	        for (int i = 0; i < rowCount; i++) {
	            Boolean include = (Boolean) model2.getValueAt(i, includeCol);
	
	            if (include.booleanValue() && i != row1) {
	                different = true;
	                break;
	            }
	        }
        }
        
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, different);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
