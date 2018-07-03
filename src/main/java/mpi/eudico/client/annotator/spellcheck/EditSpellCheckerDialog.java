package mpi.eudico.client.annotator.spellcheck;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import java.util.Iterator;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.util.Pair;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

public class EditSpellCheckerDialog extends ClosableDialog implements ActionListener, ItemListener {

	private static final int DEFAULT_MINIMUM_HEIGHT = 260;
    private static final int DEFAULT_MINIMUM_WIDTH = 700;
   
    private JPanel titlePanel;
	private JLabel titleLabel;

	private JPanel checkerInfoPanel;
	private JLabel checkerNameLabel;
	private JComboBox checkerNameBox;
	private JLabel checkerInfoLabel;
	private JTextArea checkerInfoText;

	private JPanel checkerButtonPanel;
	private JButton addCheckerButton;
	private JButton deleteCheckerButton;

	private JPanel closeButtonPanel;
	private JButton closeButton;
	
	private Transcription transcription = null;
	
	public EditSpellCheckerDialog(Frame owner, boolean modal) {
		super(owner, modal);
		if(owner != null && owner instanceof ElanFrame2) {
			ViewerManager2 vm = ((ElanFrame2) owner).getViewerManager();
			if(vm != null) {
				transcription = vm.getTranscription();
			}
		}
		initComponents();
		postInit();
	}
	
	private void initComponents(){
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		GridBagConstraints gridBagConstraints;
		getContentPane().setLayout(new GridBagLayout());

		Insets insets = new Insets(2, 6, 2, 6);

		titlePanel = new JPanel();
		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titlePanel.add(titleLabel);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = insets;
		getContentPane().add(titlePanel, gridBagConstraints);

		checkerInfoPanel = new JPanel();
		checkerInfoPanel.setLayout(new GridBagLayout());

		checkerNameLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerNameLabel, gridBagConstraints);

		checkerNameBox = new JComboBox();
		checkerNameBox.addItemListener(this);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerNameBox, gridBagConstraints);

		checkerInfoLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerInfoLabel, gridBagConstraints);

		checkerInfoText = new JTextArea();
		//checkerInfoText.setContentType("text/html");
		checkerInfoText.setLineWrap(true);
		checkerInfoText.setWrapStyleWord(true);
		checkerInfoText.setEditable(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		checkerInfoPanel.add(new JScrollPane(checkerInfoText), gridBagConstraints);

		checkerButtonPanel = new JPanel();
		checkerButtonPanel.setLayout(new GridLayout(0, 1, 6, 6));

		addCheckerButton = new JButton();
		addCheckerButton.addActionListener(this);
		checkerButtonPanel.add(addCheckerButton);

		deleteCheckerButton = new JButton();
		deleteCheckerButton.setEnabled(false);
		deleteCheckerButton.addActionListener(this);
		checkerButtonPanel.add(deleteCheckerButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerButtonPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(checkerInfoPanel, gridBagConstraints);
		
		closeButtonPanel = new JPanel();
		closeButtonPanel.setLayout(new GridLayout(1, 1, 0, 2));

		closeButton = new JButton();
		closeButton.addActionListener(this);

		closeButtonPanel.add(closeButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = insets;
		getContentPane().add(closeButtonPanel, gridBagConstraints);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		updateUIforSelectedChecker();
	}

	protected void closeDialog() {
		setVisible(false);
		dispose();
	}

	private void postInit() {
		addCloseActions();
		updateLocale();
		setPosition();
		updateCheckerNameBox();
		updateUIforSelectedChecker();
		//checkConfigCache(); // TODO
	}

	private void updateLocale() {
		setTitle(ElanLocale.getString("EditSpellCheckerDialog.Title"));
		titleLabel.setText(ElanLocale.getString("EditSpellCheckerDialog.Title"));
		checkerNameLabel.setText(ElanLocale.getString("EditSpellCheckerDialog.Label.Checkername"));
		checkerInfoLabel.setText(ElanLocale.getString("EditSpellCheckerDialog.Label.Checkerinfo"));
		addCheckerButton.setText(ElanLocale.getString("Button.Add"));
		deleteCheckerButton.setText(ElanLocale.getString("Button.Delete"));
		closeButton.setText(ElanLocale.getString("Button.Close"));
	}

	/**
	 * Pack, size and set location.
	 */
	protected void setPosition() {
	    pack();
	    setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH),
	        Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
	    setLocationRelativeTo(getParent());
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == addCheckerButton) {
			showSpellCheckerWizard(null);
		} else if(ae.getSource() == deleteCheckerButton) {
			deleteChecker();
		} else if(ae.getSource() == closeButton) {
			closeDialog();
		}
	}
	
	private void deleteChecker() {
		Pair<String, SpellChecker> pair = (Pair<String, SpellChecker>) checkerNameBox.getSelectedItem();
		SpellCheckerRegistry.getInstance().delete(pair.getFirst());

		updateCheckerNameBox();
		updateUIforSelectedChecker();
	}

	private void updateCheckerNameBox() {
		checkerNameBox.removeItemListener(this);
		
		SpellCheckerRegistry checkerRegistry = SpellCheckerRegistry.getInstance();
		Set<String> checkerIds = checkerRegistry.getSpellCheckers().keySet();
		
		checkerNameBox.removeAllItems();
		Iterator<String> checkerIdIterator = checkerIds.iterator();
		while(checkerIdIterator.hasNext()){
			String next = checkerIdIterator.next();
			checkerNameBox.addItem(new Pair<String, SpellChecker>(next, checkerRegistry.getSpellChecker(next)));
		}
		
		if(checkerIds.size() > 0){
			checkerNameBox.setSelectedIndex(0);
			
		}
		
		checkerNameBox.addItemListener(this);
	}

	private void updateUIforSelectedChecker() {
		if (checkerNameBox.getSelectedIndex() > -1) {
			Pair<String, SpellChecker> pair = (Pair<String, SpellChecker>) checkerNameBox.getSelectedItem();
			String langRef = pair.getFirst();
			SpellChecker checker = (SpellChecker) pair.getSecond();
			String langStr = ElanLocale.getString("Button.Language");
			String typeStr = ElanLocale.getString("EditSpellCheckerDialog.Label.Type");
			String infoStr = ElanLocale.getString("EditSpellCheckerDialog.Label.Info");
			String descStr = ElanLocale.getString("EditSpellCheckerDialog.Label.Description");
			checkerInfoText.setText(langStr + ": " + LanguageCollection.getLanguageInfo(langRef).getLabel() + "\n" 
				+ typeStr + ": " + checker.getType() + "\n"
				+ infoStr + ": " + checker.getInfo() + "\n"+ descStr + ": "
				+ checker.getDescription());
			deleteCheckerButton.setEnabled(true);
		} else {
			checkerInfoText.setText("");
			deleteCheckerButton.setEnabled(false);
		}
	}

	private void showSpellCheckerWizard(SpellChecker checker) {
		AddSpellCheckerDialog dialog = new AddSpellCheckerDialog(this, true, transcription);
		dialog.setVisible(true);
		if(dialog.getSpellChecker() != null) {
			//SpellCheckerRegistry.getInstance().putSpellChecker(dialog.getLanguageRef(), dialog.getSpellChecker());
			SpellCheckerRegistry.getInstance().putSpellChecker(dialog.getLangInfo().getId(), dialog.getSpellChecker());
			updateCheckerNameBox();
			updateUIforSelectedChecker();
		}
	}
}
