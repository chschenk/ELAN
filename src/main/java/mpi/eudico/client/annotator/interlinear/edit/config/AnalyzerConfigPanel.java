package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import nl.mpi.lexan.analyzers.helpers.Information;

/** 
 * A class for selecting and configuring available "analyzers".
 * 
 * <pre>
 * +------------------------------------------------------------------+
 * |                                                                  |
 * |  +--Analyzer configuration------------------------------------+  |
 * |  |                                                            |  |
 * |  |  | Analyzer           || Source         ||  Target      |  |  |
 * |  |  ---------------------++----------------++---------------  |  |
 * |  |  | Parse and Gloss ...|| testtier       ||  sub1,sub2   |  |  |
 * |  |  | Random Analyzer    || default-lt     ||  sub-lt      |  |  | <= type config
 * |  |  |                    || /test/         ||  /testsub/   |  |  | <= 1 or more tiers that fit the types
 * |  |  |                    ||                ||              |  |  |
 * |  |                                                            |  |
 * |  +------------------------------------------------------------+  |
 * |                                                                  |
 * |  [v] Show tier mapping for all type based configurations         |
 * |                                                                  |
 * |                [ Remove ]   [ Edit configurations... ]           |
 * |                                                                  |
 * +------------------------------------------------------------------+
 * </pre>
 * 
 * @author Han Sloetjes
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class AnalyzerConfigPanel extends JPanel implements ComponentListener, 
	ActionListener, ListSelectionListener {
	private static final int ANALYZER_NAME_COL = 0;
	private static final int SOURCE_COL = 1;
	private static final int TARGET_COL = 2;
	private static final int TYPE_MODE_COL = 3;
	private static final int CONFIG_COL = 4;		// Annot{Tier,Type}Config
	private static final int TIER_SUBCONFIG_COL = 5;// only if the CONFIG_COL has a AnalyzerTypeConfig
	private static final String LIN_TYPE = "LinType";         // name for column TYPE_MODE_COL
	private static final String ANNOT_CONFIG = "AnalyzerConfig"; // name for column CONFIG_COL
	private static final String SUB_CONFIG = "SubConfig";     // name for column TIER_SUBCONFIG_COL
	private static final Color TYPE_BG = Color.WHITE;
	private static final Color TYPE_FG = Color.BLACK;
	private static final Color TIER_BG = Color.LIGHT_GRAY;
	private static final Color TIER_FG = Color.WHITE;
	private InterlinearizationManager manager;	
	private JButton editConfigButton;	
	private JButton removeConfigButton;
	private JCheckBox showTiersCB;
	private JTable configsTable;
	private DefaultTableModel configsModel;
	private JPanel configPanel;
	
	public AnalyzerConfigPanel(InterlinearizationManager manager) {
		super();
		this.manager = manager;
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		Insets insets = new Insets (4, 6, 4, 6);
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		// config Panel
		configPanel = new JPanel(new GridBagLayout());
		configPanel.setBorder(new TitledBorder(""));   
       
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		//add(scrollPane, gbc);
		
		add(configPanel, gbc);
		// table 
		configsModel = new DefaultTableModel(new String[]{
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.Analyzer"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.SourceTier"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.TargetTier"), 
				LIN_TYPE, ANNOT_CONFIG, SUB_CONFIG}, 0){
			@Override
			public boolean isCellEditable(int row, int column) {	
				 return false;
			 }
		};
		
		configsTable = new JTable(configsModel);
		
		//DefaultTableCellRenderer
        DefaultTableCellRenderer render = new DefaultTableCellRenderer(){
        	private Font italic = new Font(configsTable.getFont().getFontName(), Font.ITALIC, configsTable.getFont().getSize()-4);
        	private Font plain = new Font(configsTable.getFont().getFontName(), Font.PLAIN, configsTable.getFont().getSize());
       	 	
        	@Override
			public Component getTableCellRendererComponent(JTable table,
       			 Object value, boolean isSelected, boolean hasFocus, int row,
       			 	int column){
       		 
       	 		Component cell = super.getTableCellRendererComponent(table, value, 
       				 isSelected, hasFocus, row, column);  
       	 		
       	 		if (value != null && table.getValueAt(row, ANALYZER_NAME_COL) == null) {
       	 			// Tier mapping of a type named in the configuration.
       	 			// Has an empty cell in the leftmost column.
       	 			cell.setFont(italic);  
       	 			cell.setBackground(TIER_BG);
       	 			cell.setForeground(TIER_FG);
       	 		} else {
       	 			cell.setFont(plain);
	 			
       	 			// tier
       	 			if (value != null && !(Boolean)configsModel.getValueAt(row,TYPE_MODE_COL)) {
       	 				cell.setBackground(TIER_BG);
       	 				cell.setForeground(TIER_FG);
       	 			} else {
           	 			cell.setBackground(TYPE_BG); // type
           	 			cell.setForeground(TYPE_FG);
       	 			}
       	 		} 		
       	 		       	 		
       	 		if(table.getSelectedRow() == row){       	 			  	 			
       	 			cell.setBackground(table.getSelectionBackground());
       	 			cell.setForeground(table.getSelectionForeground());
       	 		}
       	 		
       	 		return cell;    		   
       	 	}
        };  
       
		configsTable.setDefaultRenderer(Object.class, render);
		configsTable.getTableHeader().setReorderingAllowed(false);
		configsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configsTable.getSelectionModel().addListSelectionListener(this);

		// These columns are for our internal use; hide them from the user.
		configsTable.removeColumn(configsTable.getColumn(LIN_TYPE));
		configsTable.removeColumn(configsTable.getColumn(ANNOT_CONFIG));
		configsTable.removeColumn(configsTable.getColumn(SUB_CONFIG));
		
		removeConfigButton = new JButton();
		removeConfigButton.addActionListener(this);
		
		editConfigButton = new JButton();
		editConfigButton.addActionListener(this);
		
		showTiersCB = new JCheckBox();
		showTiersCB.setSelected(true);
		showTiersCB.addActionListener(this);
		
		gbc = new GridBagConstraints();
		gbc.insets = insets;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 3;
		configPanel.add(new JScrollPane(configsTable), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 1;
		configPanel.add(showTiersCB, gbc);	
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(removeConfigButton, gbc);
		
		gbc.gridx = 2;	
		gbc.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(editConfigButton, gbc);			
		
		addComponentListener(this);
		
		updateLocale();
	}
	
	public void updateLocale(){
		((TitledBorder)configPanel.getBorder()).setTitle(ElanLocale.getString("InterlinearAnalyzerConfigDlg.Title"));
		removeConfigButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.RemoveConfig"));
		editConfigButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.EditConfig"));
		showTiersCB.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ShowTiers"));
		configsModel.setColumnIdentifiers(new String[]{
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.Analyzer"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.SourceTier"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.TargetTier"),
				LIN_TYPE, ANNOT_CONFIG, SUB_CONFIG});
		// These columns are for our internal use; hide them from the user.
		configsTable.removeColumn(configsTable.getColumn(LIN_TYPE));
		configsTable.removeColumn(configsTable.getColumn(ANNOT_CONFIG));
		configsTable.removeColumn(configsTable.getColumn(SUB_CONFIG));
		repaint();// the titled border does not automatically update		
	}
	
	/**
	 * Changes from the preferences
	 */
	public void configsChanged() {
		configsTable.getSelectionModel().removeListSelectionListener(this);
		
		if (configsModel.getRowCount() > 0) {
			for (int i = configsModel.getRowCount() - 1; i >= 0; i--) {
				configsModel.removeRow(i);
			}
		}
		
		List<AnalyzerConfig> conf = manager.getTextAnalyzerContext().getConfigurations();
				
		AnalyzerConfig activeConf = manager.getInterEditor().getActiveConfiguration();
		int activeConfRowIndex = -1;
		
		String target;
		for (AnalyzerConfig ac : conf) {
			target = null;
			for (String s : ac.getDest()) {
				if(target == null){
					target = s;
				} else {
					target = target.concat(",").concat(s);
				}
			}
			configsModel.addRow(new Object[]{ac.getAnnotId().getName(), ac.getSource(), target, ac.isTypeMode(), ac, null});

			if (activeConf == null) {
				activeConf = ac;
				activeConfRowIndex = 0;
				manager.getInterEditor().setActiveConfiguration(ac);
			}

			if (ac.isTypeMode() && showTiersCB.isSelected()) {
				List<AnalyzerConfig> tierConfig = ((AnalyzerTypeConfig)ac).getTierConfigurations();

				for (AnalyzerConfig atc : tierConfig) {	
					target = null;
					for (String s : atc.getDest()) {
						if (target == null) {
							target = s;
						} else {
							target = target.concat(",").concat(s);
						}
					}
					configsModel.addRow(new Object[]{null, atc.getSource(), target, null, ac, atc});	

					if (activeConfRowIndex < 0) {
						if (atc.equals(activeConf)) {									
							activeConfRowIndex = configsModel.getRowCount() - 1;
						}
					}
				}
			}

			if (activeConfRowIndex < 0) {
				if (ac.equals(activeConf)) {									
					activeConfRowIndex = configsModel.getRowCount() - 1;
				}
			}
		}
		
		configsTable.getSelectionModel().addListSelectionListener(this);
		
		if (activeConfRowIndex < 0 && conf.size() > 0) {
			//configsTable.getSelectionModel().setSelectionInterval(configsTable.getRowCount()-1, configsTable.getRowCount()-1);
			activeConfRowIndex = 0;
			activeConf = conf.get(0);
			configsTable.getSelectionModel().setSelectionInterval(0, 0);
			manager.getInterEditor().setActiveConfiguration(activeConf);
		} else {
			configsTable.getSelectionModel().setSelectionInterval(activeConfRowIndex, activeConfRowIndex);
		}
	}
	
	/**
	 * Updates the configuration table with the configurations
	 * from the configuration dialog AnalyzerConfigDialog.
	 *  
	 * @param configList : List&lt;AConfig>
	 * @param typeMode : true if the mapping is based on linguistic types
	 */
	protected void updateConfigurations(List<AnalyzerConfigDialog.AConfig> configList, 
			boolean typeMode) {
		
		if (configList == null) {
			return;
		}
		
		List<Information> analyzers = manager.getTextAnalyzerContext().listTextAnalyzersInfo();
		// Duplicate list of existing configs
		ArrayList<AnalyzerConfig> oldConfigs = new ArrayList<AnalyzerConfig>(manager.getTextAnalyzerContext().getConfigurations());
		boolean changed = false;
		
		for (AnalyzerConfigDialog.AConfig  annotConfig : configList) {
			String annot = annotConfig.getAnalyzer();
			String source = annotConfig.getSource();
			List<String>  targetList = annotConfig.getTargetTierList();
			Information info = null;			
			
			// find the analyzer			
			if (analyzers != null) {				
				for (Information li : analyzers) {
					if (annot.equals(li.getName())) {
						info = li;
						break;
					}
				}
			}
			
			// if info == null, popup warning
			if (info == null) {
				JOptionPane.showMessageDialog(this, annot + ": "+ 
			ElanLocale.getString("InterlinearAnalyzerConfigPanel.AnalyzerNotFound")
						+" -" + source,	"Warning", JOptionPane.WARNING_MESSAGE);
			} else {	
				if (typeMode) {					
					AnalyzerTypeConfig atc = new AnalyzerTypeConfig(info, source, targetList);		
					manager.fillWithTierConfigs(info, atc, null);
					changed = updateConfig(oldConfigs, atc) | changed;
				} else {
					AnalyzerConfig ac = new AnalyzerConfig(info, source, targetList);
					changed = updateConfig(oldConfigs, ac) | changed;
				}
			}
		}
		
		// Remove the old configs which didn't get matched by equal new configs,
		// i.e. the ones that were changed or removed by the user.
		for (AnalyzerConfig toRemove : oldConfigs) {
			if (toRemove.isTypeMode() == typeMode) {
				manager.getTextAnalyzerContext().removeConfig(toRemove);
				changed = true;
			}
		}
		
		if (changed) {
			configsChanged();
		}
	}
	
	/**
	 * If the AnalyzerConfig was not there already, add it.
	 * Otherwise, remove it from the 'old' list and keep it.
	 * <p>
	 * We don't need to look in more detail at AnnotTypeConfigs, like
	 * configExists() does, because we are dealing here with the data
	 * as entered by the user. It would be unexpected to mix it with
	 * AnnotConfigs derived from AnnotTypeConfigs.
	 * <p>
	 * Note that AnalyzerConfig and AnalyzerTypeConfig's equals() methods
	 * require the argument's type mode to be the same, and disregard
	 * the derived data.
	 * 
	 * @return whether the config was new.
	 */
	private boolean updateConfig(ArrayList<AnalyzerConfig> oldConfigs, AnalyzerConfig config) {
		int index = oldConfigs.indexOf(config);
		
		if (index < 0) {
			// The old configurations don't contain this new one.
			manager.getTextAnalyzerContext().addConfig(config);
			return true;
		} else {
			// Don't delete this config later on: it's still there.
			oldConfigs.remove(index);
			return false;
		}
	}
	
	
	/**
	 * Get the targetList from the given string
	 * 
	 * @param target, string in which the targets are comma separated 
	 * @return targetList
	 */
	public List<String> getTargetList(String target){
		List<String> targetList = null;
		if(target != null){
			targetList = new ArrayList<String>();
			String[] targets = target.split(",");
			for(String s : targets){
				targetList.add(s);
			}
		}
		return targetList;
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == removeConfigButton) {
			int row = configsTable.getSelectedRow();
			if (row > -1) {
				int selOption = JOptionPane.showConfirmDialog(this, ElanLocale.getString("InterlinearAnalyzerConfigPanel.RemoveWarning"), 
						ElanLocale.getString("Message.Warning"), JOptionPane.OK_CANCEL_OPTION, 
						JOptionPane.WARNING_MESSAGE, null);
				if (selOption != JOptionPane.OK_OPTION) {
					return;
				}
				AnalyzerConfig ac = (AnalyzerConfig) configsModel.getValueAt(row, CONFIG_COL);
				AnalyzerConfig atsc = (AnalyzerConfig) configsModel.getValueAt(row, TIER_SUBCONFIG_COL);
				
				if (atsc != null && ac instanceof AnalyzerTypeConfig) {
					// one of the tier sub-configs of a type config
					AnalyzerTypeConfig atc = (AnalyzerTypeConfig)ac;
					// Remove row from table
					configsModel.removeRow(row);
					// Remove tier config from type config
					atc.removeTierConf(atsc);

					// If the type config has no tiers any more, remove the type config too
					if (atc.getTierConfigurations().isEmpty()) {
						configsModel.removeRow(row - 1);
						manager.getTextAnalyzerContext().removeConfig(ac);
					}
				} else {
					// A plain tier config, or 
					// a type config which usually has 1 or more tier sub-configs to be removed as well
					if (ac instanceof AnalyzerTypeConfig && showTiersCB.isSelected()) {
						// remove all rows
						int n = row + 1;
						while (configsModel.getRowCount() > n && 
							   configsModel.getValueAt(n, CONFIG_COL) == ac) {
							configsModel.removeRow(n);
						}
					}
					
					configsModel.removeRow(row);					
					manager.getTextAnalyzerContext().removeConfig(ac);
				}
				
				repaint();
			}
		}		
		else if(e.getSource() == editConfigButton){
			AnalyzerConfigDialog configDialog = new AnalyzerConfigDialog(null, manager);
			configDialog.setVisible(true);
			if (configDialog.isApplied()) {
				updateConfigurations(configDialog.getConfigurationMap(false), false);
				updateConfigurations(configDialog.getConfigurationMap(true), true);
			}
		} else if(e.getSource() == showTiersCB){
			configsChanged();
		}
	}


	@Override
	public void componentHidden(ComponentEvent e) {
		// stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		revalidate();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = configsTable.getSelectedRow();
		if (row > -1 && !e.getValueIsAdjusting()) {

			AnalyzerConfig ac = (AnalyzerConfig) configsModel.getValueAt(row, TIER_SUBCONFIG_COL);
			if (ac == null) {
				ac = (AnalyzerConfig) configsModel.getValueAt(row, CONFIG_COL);
			}
			if (ac != null) {
				manager.getInterEditor().setActiveConfiguration(ac);
			}
		}		
	}
}
