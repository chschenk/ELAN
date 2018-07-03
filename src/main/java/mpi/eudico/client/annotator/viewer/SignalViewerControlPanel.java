package mpi.eudico.client.annotator.viewer;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.WaveFormViewerMenuManager;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
/**
 * A control panel for the SignalViewer (waveform viewer). 
 * Currently only shows list of wave files 
 * 
 * @author Han Sloetjes
 * @author aarsom
 */
@SuppressWarnings("serial")
public class SignalViewerControlPanel extends JPanel implements ActionListener {
	private JComboBox wfComboBox;
	private WaveFormViewerMenuManager manager;
	
	/**
	 * The manager performs relevant actions and connects to and updates
	 * the frame and its menus.
	 * 
	 * @param manager the waveform menu manager for updating the menu etc.
	 */
	public SignalViewerControlPanel(WaveFormViewerMenuManager manager){
		super();
		this.manager = manager;
		initComponents();
	}
	
	private void initComponents(){
		manager.setSignalViewerControlPanel(this);
		
		setLayout(new GridBagLayout());
		
		wfComboBox = new JComboBox();
		wfComboBox.addActionListener(this);
		int origFontSize = wfComboBox.getFont().getSize();
		// slightly smaller font
		wfComboBox.setFont(new Font(wfComboBox.getFont().getFontName(), wfComboBox.getFont().getStyle(), (5 * origFontSize) / 6));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(2, 2, 2, 6);
		add(wfComboBox, gbc);
		
		initViewerPopUPMenu();		
		
	}
	
	/**
	 * Initialize the items in popup menu
	 */
	public void initViewerPopUPMenu(){
		wfComboBox.removeActionListener(this);
		wfComboBox.removeAllItems();
		
		List<SelectableObject<MediaDescriptor>>  list= manager.getWaveFormList();
		String fileName;
		String mediaUrl;		
		JComboBoxItem selectedItem = null;
		JComboBoxItem item;
		for(SelectableObject<MediaDescriptor> sob : list){
			mediaUrl = sob.getValue().mediaURL;
			fileName = FileUtility.fileNameFromPath(mediaUrl);
			item = new JComboBoxItem(mediaUrl, fileName);
			wfComboBox.addItem(item);
			if(sob.isSelected()){
				selectedItem = item;
			}
		}
		
		if(selectedItem != null){
			wfComboBox.setSelectedItem(selectedItem);
		}
		
		wfComboBox.addActionListener(this);
	}
	
	public void updateWaveFormPanel(String mediaUrl){
		JComboBoxItem item;
		wfComboBox.removeActionListener(this);
        for (int i = 0; i < wfComboBox.getItemCount(); i++) {
            item = (JComboBoxItem)wfComboBox.getItemAt(i);
            if(item.mediaURL.equals(mediaUrl)){
               wfComboBox.setSelectedIndex(i);
               break;
            }
        }
        wfComboBox.addActionListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == wfComboBox){
			String mediaURL = ((JComboBoxItem)wfComboBox.getSelectedItem()).mediaURL;
			// this also updates the menu in the frame
			manager.performActionFor(mediaURL, e);
		}			
	}
	
	private class JComboBoxItem {
		String mediaURL;
		String fileName;
		
		JComboBoxItem(String mediaURL, String fileName){
			this.mediaURL = mediaURL;
			this.fileName =fileName;
		}
		
		@Override
		public String toString(){
			return fileName;
		}
		
	}
}
