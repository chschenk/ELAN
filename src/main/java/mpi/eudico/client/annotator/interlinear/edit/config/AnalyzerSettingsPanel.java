package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * A tab-pane to host the configuration panels provided by 
 * the analyzers. Analyzers that allow to change settings and 
 * to configure behavior, need to have their own, custom 
 * settings panel.
 * 
 * @author aarsom, olasei
 */
@SuppressWarnings("serial")
public class AnalyzerSettingsPanel extends JPanel {
	private TextAnalyzerHostContext hostContext;
	private List<Information> analyzers;
	
	private JTabbedPane tabPane;
	private TitledBorder titledBorder;
	
	public AnalyzerSettingsPanel(TextAnalyzerHostContext hostContext) {
		super();
		this.hostContext = hostContext;
		analyzers = hostContext.listTextAnalyzersInfo();
		initComponents();
	}
	
	private void initComponents() {
		if (analyzers == null || analyzers.isEmpty()) {
			return;
		}
		
		tabPane = new JTabbedPane();
		int tabNr = 0;
		
		for (int i = 0; i < analyzers.size(); i++) {
			Information analyzer = analyzers.get(i);
			Component comp = hostContext.getConfigurationComponent(analyzer, true);
			if (comp != null) {
				tabPane.addTab(analyzer.getName(), new JScrollPane(comp));
				if (tabNr < 10) {
					tabPane.setMnemonicAt(tabNr, KeyEvent.VK_0 + (tabNr + 1) % 10);
				}
				tabNr++;
			}
		}
		EmptyBorder marginBorder = new EmptyBorder(4, 6, 4, 6);
		titledBorder = new TitledBorder(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		setBorder(new CompoundBorder(marginBorder, titledBorder));
		
		if (tabNr > 0) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = 1;		
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;			
			add(tabPane, gbc);
			
			readPreferences();
		}
	}
	
	/**
	 * Can be called by the mode/layout manager if the UI language changed. 
	 */
	public void updateLocale() {
		titledBorder.setTitle(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
	}

	private void readPreferences() {
		if (tabPane != null) {
			String analyzer = Preferences.getString("AnalyzerConfigurationPanel.CurrentAnalyzer", 
					 hostContext.getTranscription());
			
			if (analyzer != null) {
				int tabNr = tabPane.indexOfTab(analyzer);
				if (tabNr >= 0) {
					tabPane.setSelectedIndex(tabNr);
				}
			}
		}
	}
	
	public void storePreferences() {
		if (tabPane != null && tabPane.getSelectedIndex() >= 0) {
			Preferences.set("AnalyzerConfigurationPanel.CurrentAnalyzer", 
					tabPane.getTitleAt(tabPane.getSelectedIndex()),
					hostContext.getTranscription());
		}
	}
}
