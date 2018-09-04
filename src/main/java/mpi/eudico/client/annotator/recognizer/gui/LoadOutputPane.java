package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.util.CheckBoxBListCellRenderer;
import mpi.eudico.client.util.SelectableObject;

@SuppressWarnings("serial")
public class LoadOutputPane extends JPanel {
	private List<SelectableObject<String>> options;
	private JList list;
	private DefaultListModel model;
	
	/**
	 * @param options the options to add to the selection list.
	 */
	public LoadOutputPane(List<SelectableObject<String>> options) {
		super();
		this.options = options;
		initComponents();
	}

	/**
	 * Initialize the list.
	 */
	private void initComponents() {
		setLayout(new GridBagLayout());
		JLabel label = new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.LoadOutput"));
		Insets insets = new Insets(2, 6, 2, 6);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.insets = insets;
		add(label, gbc);
		
		model = new DefaultListModel();
		list = new JList(model);
		// load preferences
		for (int i = 0; i < options.size(); i++) {			
			model.add(i, options.get(i));
		}
		list.setCellRenderer(new CheckBoxBListCellRenderer());
		//list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//list.addListSelectionListener(this);
		list.addMouseListener(new ListMouseListener());
		JScrollPane pane = new JScrollPane(list);
		pane.setPreferredSize(new Dimension(160, 80));
		gbc.gridy = 1;

		add(pane, gbc);

	}
	
	/**
	 * Returns the selected elements.
	 * 
	 * @return the selected elements
	 */
	public List<String> getSelectedItems() {
		List<String> result = new ArrayList<String>(4);
		Object obj = null;
		SelectableObject<String> selObj;
		
		for (int i = 0; i < model.getSize(); i++) {
			obj = model.get(i);
			if (obj instanceof SelectableObject) {
				selObj = (SelectableObject<String>) obj;
				if (selObj.isSelected()) {
					result.add(selObj.getValue());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * A mouse listener for the list.
	 * 
	 * @author Han Sloetjes
	 */
	class ListMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int row = list.locationToIndex(e.getPoint());
			if (row > -1) {
				SelectableObject<String> sel = (SelectableObject<String>) model.get(row);
				sel.setSelected(!sel.isSelected());
				list.repaint();
			}
		}		
	}
}
