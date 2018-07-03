package mpi.eudico.client.annotator.lexicon;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;

public class ValueChooseDialog extends ClosableDialog implements ActionListener, ListSelectionListener {

	private ArrayList<String> values;
	private JLabel msgLabel;
	private JLabel titleLabel;
	private JList valueList;
	private JScrollPane valueListScroller;
	private JButton okButton;
	private JButton cancelButton;
	private Component parent;
	private boolean canceled;

	public ValueChooseDialog(Frame parent, ArrayList<String> values) {
		super(parent, true);
		this.parent = parent;
		this.values = values;
		canceled = false;
		initComponents();
		postInit();
	}
	
	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});
		
		titleLabel = new JLabel();
		msgLabel = new JLabel();
		valueList = new JList(values.toArray());
		valueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		valueList.addListSelectionListener(this);
		
		valueListScroller = new JScrollPane(valueList);
		
		okButton = new JButton();
		okButton.addActionListener(this);
		cancelButton = new JButton();
		cancelButton.addActionListener(this);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Insets insets = new Insets(6, 6, 6, 6);
		
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor = GridBagConstraints.NORTH;
	    c.insets = insets;
	    c.weightx = 0.0;
	    c.gridwidth = 2;
	    this.add(titleLabel, c);
		
	    c.fill = GridBagConstraints.NONE;
		c.insets = insets;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		this.add(msgLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridy = 2;
		this.add(valueListScroller, c);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		c.insets = insets;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.SOUTH;
		this.add(buttonPanel, c);
	
	}

	private void postInit() {
		updateLocale();
		okButton.setEnabled(false);
		setLocationRelativeTo(getParent());
		
		pack();
		
		int w = 450;
	    int h = 250;
	    setSize(w, (getSize().height < h) ? h : getSize().height);
	    setLocationRelativeTo(parent);
	}

	private void updateLocale() {
		this.setTitle(ElanLocale.getString("LexiconEntryViewer.ValueChooseDialog.Title"));
		titleLabel.setText(ElanLocale.getString("LexiconEntryViewer.ValueChooseDialog.Title"));
		msgLabel.setText(ElanLocale.getString("LexiconEntryViewer.ValueChooseDialog.Message"));
		okButton.setText(ElanLocale.getString("Button.OK"));
		cancelButton.setText(ElanLocale.getString("Button.Cancel"));
	}

	protected void closeDialog() {
		setVisible(false);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == okButton) {
			closeDialog();
		} else if (event.getSource() == cancelButton) {
			canceled = true;
			closeDialog();
		} 
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (event.getSource() == valueList) {
			if(valueList.getSelectedIndex() > -1) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	public String getSelectedValue() {
		if(valueList.getSelectedIndex() > -1) {
			return values.get(valueList.getSelectedIndex());
		}
		return null;
	}

}
