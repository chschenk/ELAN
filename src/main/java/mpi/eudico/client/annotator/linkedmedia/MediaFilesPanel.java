package mpi.eudico.client.annotator.linkedmedia;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.ElanFileFilter;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MediaFilesPanel extends JPanel implements ActionListener,
	ListSelectionListener, TableModelListener {

	private static final String NO_SOURCE = "-";

	// ui stuff
	private JScrollPane mediaScrollPane;
	private JTable mediaTable;
	private JPanel linkInfoPanel;
	private JLabel linkInfoLabel;
	private JButton addRemoteMB;
	private JButton addMB;
	private JButton removeMB;
	private JButton updateMB;
	private JButton masterMB;
	private JButton extractMB;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JPanel moveButtonPanel;
	private JPanel mediaButtonPanel;

	private TranscriptionImpl transcription;
	private List<MediaDescriptor> currentMDCopy;
	private boolean offsetEditMode = false;
	private LinkedFilesDialog linkedFilesDialog;

	/**
	 * Creates a modal new LinkedFilesDialog.
	 *
	 * @param transcription the transcription
	 */
	public MediaFilesPanel(Transcription transcription, LinkedFilesDialog linkedFilesDialog) {
		this.transcription = (TranscriptionImpl) transcription;
		this.linkedFilesDialog = linkedFilesDialog;

		if (transcription != null) {
			List<MediaDescriptor> orgMD = transcription.getMediaDescriptors();
			currentMDCopy = new ArrayList<MediaDescriptor>(orgMD.size());

			MediaDescriptor md;
			MediaDescriptor cloneMD;

			for (int i = 0; i < orgMD.size(); i++) {
				md = orgMD.get(i);
				cloneMD = (MediaDescriptor) md.clone();

				if (cloneMD != null) {
					currentMDCopy.add(cloneMD);
				}
			}
		}

		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * dialog.
	 */
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
		ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
		ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
		ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
		CheckBoxTableCellRenderer cbRenderer = new CheckBoxTableCellRenderer();
		cbRenderer.setIcon(untickIcon);
		cbRenderer.setSelectedIcon(tickIcon);
		cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		mediaScrollPane = new JScrollPane();
		mediaTable = new JTable();
		linkInfoPanel = new JPanel();
		linkInfoLabel = new JLabel();
		mediaButtonPanel = new JPanel();
		addRemoteMB = new JButton();
		addMB = new JButton();
		removeMB = new JButton();
		updateMB = new JButton();
		masterMB = new JButton();
		extractMB = new JButton();
		moveUpButton = new JButton();
		moveDownButton = new JButton();
		
		setLayout(new GridBagLayout());

		Insets insets = new Insets(2, 6, 2, 6);

		mediaScrollPane.setMinimumSize(new Dimension(100, 100));
		mediaScrollPane.setPreferredSize(new Dimension(550, 100));

		MediaDescriptorTableModel model = new MediaDescriptorTableModel(currentMDCopy);
		mediaTable.setModel(model);

		mediaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mediaTable.getSelectionModel().addListSelectionListener(this);
		mediaTable.getModel().addTableModelListener(this);

		for (int i = 0; i < mediaTable.getColumnCount(); i++) {
			if (mediaTable.getModel().getColumnClass(i) != String.class) {
				mediaTable.getColumn(mediaTable.getModel().getColumnName(i))
						  .setPreferredWidth(35);
			}

			if (mediaTable.getModel().getColumnClass(i) == Boolean.class) {
				mediaTable.getColumn(mediaTable.getModel().getColumnName(i))
						  .setCellRenderer(cbRenderer);
			}
		}

		mediaScrollPane.setViewportView(mediaTable);
		mediaScrollPane.getViewport().setBackground(mediaTable.getBackground());

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = insets;
		add(mediaScrollPane, gridBagConstraints);

		linkInfoPanel.setLayout(new BorderLayout());
		linkInfoLabel.setFont(linkInfoLabel.getFont().deriveFont(Font.PLAIN, 10));
		fillInfoPanel(-1);
		linkInfoPanel.add(linkInfoLabel, BorderLayout.WEST);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = insets;
		add(linkInfoPanel, gridBagConstraints);

		mediaButtonPanel.setLayout(new GridLayout(2, 4, 6, 2));

		addRemoteMB.addActionListener(this);
		mediaButtonPanel.add(addRemoteMB);
		
		addMB.addActionListener(this);
		mediaButtonPanel.add(addMB);

		removeMB.setEnabled(false);
		removeMB.addActionListener(this);
		mediaButtonPanel.add(removeMB);

		updateMB.setEnabled(false);
		updateMB.addActionListener(this);
		mediaButtonPanel.add(updateMB);

		mediaButtonPanel.add(new JLabel());
		
		masterMB.setEnabled(false);
		masterMB.addActionListener(this);
		mediaButtonPanel.add(masterMB);

		extractMB.setEnabled(false);
		extractMB.addActionListener(this);
		mediaButtonPanel.add(extractMB);

		moveButtonPanel = new JPanel();
		moveButtonPanel.setLayout(new GridLayout(1, 2, 6, 2));

		moveUpButton.setIcon(upIcon);
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(this);
		moveButtonPanel.add(moveUpButton);

		moveDownButton.setIcon(downIcon);
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(this);
		moveButtonPanel.add(moveDownButton);

		mediaButtonPanel.add(moveButtonPanel);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.insets = insets;
		add(mediaButtonPanel, gridBagConstraints);

		updateLocale();
	}

	/**
	 * Applies localized strings to the ui elements.
	 */
	private void updateLocale() {

		linkInfoPanel.setBorder(new TitledBorder(ElanLocale.getString(
					"LinkedFilesDialog.Label.LinkInfo")));
		addRemoteMB.setText(ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"));
		addMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Add"));
		removeMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Remove"));
		updateMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Update"));
		masterMB.setText(ElanLocale.getString(
				"LinkedFilesDialog.Button.MasterMedia"));
		extractMB.setText(ElanLocale.getString(
				"LinkedFilesDialog.Button.Extracted"));
		moveUpButton.setToolTipText(ElanLocale.getString(
				"LinkedFilesDialog.Button.Up"));
		moveDownButton.setToolTipText(ElanLocale.getString(
				"LinkedFilesDialog.Button.Down"));
	}

	/**
	 * Checks whether changes have been made to the set of linked media files
	 * and, if any, creates a command that replaces the  media descriptors.
	 * The dialog is then closed.
	 */
	void applyChanges() {
		boolean anyChange = hasChanged();

		// warn if a video and an extracted audio have a different 
		// offset
		checkUnequalOffsets();

		if (anyChange) {
			Command c = ELANCommandFactory.createCommand(transcription,
					ELANCommandFactory.CHANGE_LINKED_FILES);
			if (offsetEditMode) {// hier...
				c.execute(transcription, new Object[] { currentMDCopy, Boolean.TRUE, Boolean.TRUE });
			} else {
				c.execute(transcription, new Object[] { currentMDCopy, Boolean.TRUE });
			}
		}
		// re-read the current media descriptors after applying the changes
		List<MediaDescriptor> orgMD = transcription.getMediaDescriptors();
		currentMDCopy = new ArrayList<MediaDescriptor>(orgMD.size());

		MediaDescriptor md;
		MediaDescriptor cloneMD;

		for (int i = 0; i < orgMD.size(); i++) {
			md = orgMD.get(i);
			cloneMD = (MediaDescriptor) md.clone();

			if (cloneMD != null) {
				currentMDCopy.add(cloneMD);
			}
		}
	}

	/**
	 * Checks whether anything has changed in the linked media files setup. 
	 * 
	 * @return whether anything has been changed in the linked media files setup
	 */
	boolean hasChanged() {
		if (mediaTable.isEditing()) {
			mediaTable.getCellEditor().stopCellEditing();
		}
		boolean anyChange = false;

		List<MediaDescriptor> orgMD = transcription.getMediaDescriptors();
		MediaDescriptor olddesc;
		MediaDescriptor newdesc;

		// first compare the size of the vectors
		if (orgMD.size() != currentMDCopy.size()) {
			anyChange = true;
		}

		// if the size is the same check if all elements are the same
		if (!anyChange) {
outerloop: 
			for (int i = 0; i < orgMD.size(); i++) {
				olddesc = orgMD.get(i);

				for (int j = 0; j < currentMDCopy.size(); j++) {
					newdesc = currentMDCopy.get(j);

					if ((olddesc != null) && olddesc.equals(newdesc)) {
						// check on change in master media and let the order be important
						//if ((i == 0 && j > 0) || (i > 0 && j == 0)) { master change
						if (i != j) {
							anyChange = true;

							break outerloop;
						}

						continue outerloop;
					}
					
					// HS July 2013 added check on offset
					if (olddesc!= null && newdesc != null && olddesc.mediaURL.equals(newdesc.mediaURL)) {
						if (olddesc.timeOrigin != newdesc.timeOrigin) {
							anyChange = true;
							
							break outerloop;
						}
					}
				}

				// if we come here something has changed
				anyChange = true;

				break outerloop;
			}
		}
				
		return anyChange;
	}
	
	/**
	 * Creates an input pane for the url of a rtsp media stream.
	 * The syntax of the returned address is roughly checked and a MediaDescriptor is created.
	 */
	private void addRemoteMedia() {
        String url = chooseRemoteFile();
        
        if (url == null) {
            return;
        } else {
            MediaDescriptor md = MediaDescriptorUtil.createMediaDescriptor(url);
	        addMediaDescriptor(md);
        }
	}
	
	/**
	 * Adds a MediaDescriptor to the list of descriptors. - prompts the user to
	 * locate the media file - performs some checking on type and validity of
	 * the new media  (e.g. it is not allowed to have more than one audio
	 * file) - adds the mediadescriptor to the table model (i.e. to the vector
	 * of copied descriptors)
	 */
	private void addMediaDescriptor() {
		String file = chooseMediaFile(ElanFileFilter.MEDIA_TYPE, false);

		if (file == null) {
			return;
		}

		MediaDescriptor md = MediaDescriptorUtil.createMediaDescriptor(file);

		addMediaDescriptor(md);
	}

	/**
	 * Adds a MediaDescriptor to the list of descriptors. Performs some checking on type and validity
	 * of the new media, adds the mediadescriptor to the table model (i.e. to the vector
	 * of copied descriptors).
	 * @param md the media descriptor
	 */
	private void addMediaDescriptor(MediaDescriptor md) {
		if (md == null) {
			return;
		}

		if (linkedFilesDialog.urlAlreadyLinked(md.mediaURL)) {
			showWarningDialog(ElanLocale.getString(
					"LinkedFilesDialog.Message.AlreadyLinked"));

			return;		
		}

		int row = mediaTable.getSelectedRow();

		for (int i = 0; i < currentMDCopy.size(); i++) {
			MediaDescriptor otherMD = currentMDCopy.get(i);

			// should this automatic detection of extracted_from remain??
			if (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
					MediaDescriptorUtil.isVideoType(otherMD)) {
				if (FileUtility.sameNameIgnoreExtension(md.mediaURL,
							otherMD.mediaURL)) {
					md.extractedFrom = otherMD.mediaURL;
				}
			}

			if (otherMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
					MediaDescriptorUtil.isVideoType(md)) {
				if (FileUtility.sameNameIgnoreExtension(md.mediaURL,
							otherMD.mediaURL)) {
					otherMD.extractedFrom = md.mediaURL;
				}
			}
		}

		currentMDCopy.add(md);

		// the table model has a reference to the same vector of media descriptors
		((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();

		if (row >= 0) {
			setSelectionIndex(currentMDCopy.size() - 1);
		}
	}
	
	public boolean urlAlreadyLinked(String url) {
		for (int i = 0; i < currentMDCopy.size(); i++) {
			MediaDescriptor otherMD = currentMDCopy.get(i);

			if (otherMD.mediaURL.equals(url)) {
				return true;
			}
		}
	
		return false;
	}
	
	
	/**
	 * Removes the selected mediadescriptor from the list/table. It thereby is
	 * also removed from the vector of copied descriptors. Other descriptors
	 * may have to be changed, e.g. when ther is an mediadescriptor with an
	 * extracted from field pointing to the removed descriptor.
	 */
	private void removeMediaDescriptor() {
		int row = mediaTable.getSelectedRow();

		if (row >= 0) {
			// the row number is the same as the position of the MediaDescriptor 
			// in the vector of descriptors.
			MediaDescriptor md = currentMDCopy.get(row);

			if (md.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
				for (int i = 0; i < currentMDCopy.size(); i++) {
					if (i == row) {
						continue;
					}

					MediaDescriptor desc = currentMDCopy.get(i);

					if (desc.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
							(desc.extractedFrom != null) &&
							desc.extractedFrom.equals(md.mediaURL)) {
						desc.extractedFrom = null;

						if (md.timeOrigin != 0) {
							if (showOptionDialog(ElanLocale.getString(
											"LinkedFilesDialog.Question.AudioKeepOffset"))) {
								desc.timeOrigin = md.timeOrigin;
							}
						}

						break;
					}
				}
			}

			currentMDCopy.remove(row);

			// the table model has a reference to the same vector of media descriptors
			((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
		}
	}

	/**
	 * Set the selected row in the table.
	 * Use setSelectionInterval() rather than setLeadSelectionIndex() because the latter
	 * doesn't do anything when there is no current selection.
	 * 
	 * @param row
	 */
	private void setSelectionIndex(int row) {
		mediaTable.getSelectionModel().setSelectionInterval(row, row);
	}
	
	/**
	 * Changes an existing media descriptor. A typical use would be to update
	 * the  location of a (missing) file. But it can also be used to replace
	 * one file by another of the same type.  - the user is prompted to locate
	 * the new file - when the new file has another name than the old file and
	 * the old file  had a non-zero time offset the user is prompted whether
	 * or not to  keep the offset. Same for extracted from. - checks whether
	 * other existing descriptors should be changed as well
	 */
	private void updateMediaDescriptor() {
		int row = mediaTable.getSelectedRow();

		if (row >= 0) {
			MediaDescriptor updateMD = currentMDCopy.get(row);
			String file = null;

			if (updateMD.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
				file = chooseMediaFile(ElanFileFilter.MPEG_TYPE, true);
			} else if (updateMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
				file = chooseMediaFile(ElanFileFilter.WAV_TYPE, true);
			} else {
				file = chooseMediaFile(ElanFileFilter.MEDIA_TYPE, true);
			}

			if (file == null) {
				return;
			}

			MediaDescriptor md = MediaDescriptorUtil.createMediaDescriptor(file);

			// it should not be exactly the same file
			if (md.mediaURL.equals(updateMD.mediaURL)) {
				showWarningDialog(ElanLocale.getString(
						"LinkedFilesDialog.Message.SameFile"));

				return;
			}

			// should the updated file be of the same mime-type?
			for (int i = 0; i < currentMDCopy.size(); i++) {
				if (i == row) {
					continue;
				}

				MediaDescriptor otherMD = currentMDCopy.get(i);

				// check whether the file was already linked
				if (otherMD.mediaURL.equals(md.mediaURL)) {
					showWarningDialog(ElanLocale.getString(
							"LinkedFilesDialog.Message.AlreadyLinked"));

					return;
				}

				// if there is an audio descriptor that has been extracted from the descriptor
				// being updated, prompt the user whether the audio should now be considered
				// to be extracted from the new file
				if ((otherMD.extractedFrom != null) &&
						otherMD.extractedFrom.equals(updateMD.mediaURL)) {
					if (md.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
						if (showOptionDialog(ElanLocale.getString(
										"LinkedFilesDialog.Question.UpdateExtractedFrom"))) {
							otherMD.extractedFrom = md.mediaURL;
						} else {
							otherMD.extractedFrom = null;
						}
					} else {
						otherMD.extractedFrom = null;
					}
				}

				// should this automatic detection of extracted_from remain??
				if (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
						otherMD.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
					if (FileUtility.sameNameIgnoreExtension(md.mediaURL,
								otherMD.mediaURL)) {
						md.extractedFrom = otherMD.mediaURL;
					}
				}

				if (otherMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
						md.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
					if (FileUtility.sameNameIgnoreExtension(md.mediaURL,
								otherMD.mediaURL)) {
						otherMD.extractedFrom = md.mediaURL;
					}
				}
			}

			if (updateMD.timeOrigin != 0) {
				// prompt user whether or not to maintain the offset
				if (showOptionDialog(ElanLocale.getString(
								"LinkedFilesDialog.Question.UpdateKeepOffset"))) {
					md.timeOrigin = updateMD.timeOrigin;
				}
			}

			// finally replace the descriptor
			currentMDCopy.remove(row);
			currentMDCopy.add(row, md);

			// the table model has a reference to the same vector of media descriptors
			((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
			setSelectionIndex(row);
		}
	}

	/**
	 * Sets the selected media (descriptor) to be the master media. The master
	 * media is always the first one in the list of media descriptors.
	 */
	private void setMasterMedia() {
		int row = mediaTable.getSelectedRow();

		if (row > 0) {
			MediaDescriptor md = currentMDCopy.remove(row);
			currentMDCopy.add(0, md);

			// the table model has a reference to the same vector of media descriptors
			((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
			setSelectionIndex(0);
		}
	}

	/**
	 * Changes the "extracted from" field of an audio file to a video file
	 * selected by the user.
	 */
	private void setExtractedFrom() {
		int row = mediaTable.getSelectedRow();

		if (row >= 0) {
			MediaDescriptor md = currentMDCopy.get(row);

			if (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
				String source = showVideoSelectionDialog(md);

				if (source != null) {
					if (source == NO_SOURCE) {
						md.extractedFrom = null;
					} else {
						md.extractedFrom = source;
					}
				}

				// the table model has a reference to the same vector of media descriptors
				((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
				setSelectionIndex(row);
			}
		}
	}

	/**
	 * Moves the selected media descriptor one position up in the list  of
	 * descriptors.
	 */
	private void moveUp() {
		int row = mediaTable.getSelectedRow();

		if (row > 0) {
			MediaDescriptor md = currentMDCopy.remove(row);
			currentMDCopy.add(row - 1, md);

			// the table model has a reference to the same vector of media descriptors
			((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
			setSelectionIndex(row - 1);
		}
	}

	/**
	 * Moves the selected media descriptor one position down in the list  of
	 * descriptors.
	 */
	private void moveDown() {
		int row = mediaTable.getSelectedRow();

		if ((row >= 0) && (row < (currentMDCopy.size() - 1))) {
			MediaDescriptor md = currentMDCopy.remove(row);
			currentMDCopy.add(row + 1, md);

			// the table model has a reference to the same vector of media descriptors
			((MediaDescriptorTableModel) mediaTable.getModel()).rowDataChanged();
			setSelectionIndex(row + 1);
		}
	}

	/**
	 * Enables/disables buttons after a change in table data or table
	 * selection.
	 */
	private void updateUIComponents() {
		int row = mediaTable.getSelectedRow();

		if ((row >= 0) && (row < currentMDCopy.size())) {
			removeMB.setEnabled(true);
			updateMB.setEnabled(true);

			if (row == 0) {
				masterMB.setEnabled(false);
				moveUpButton.setEnabled(false);
			} else {
				masterMB.setEnabled(true);
				moveUpButton.setEnabled(true);
			}

			if (row == (currentMDCopy.size() - 1)) {
				moveDownButton.setEnabled(false);
			} else {
				moveDownButton.setEnabled(true);
			}

			MediaDescriptor md = currentMDCopy.get(row);

			if (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
				extractMB.setEnabled(true);
			} else {
				extractMB.setEnabled(false);
			}
		} else {
			removeMB.setEnabled(false);
			updateMB.setEnabled(false);
			masterMB.setEnabled(false);
			extractMB.setEnabled(false);
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
		}

		fillInfoPanel(row);
	}

	/**
	 * Sets the contents of the media file info panel. A JLabel with html
	 * formatting is used for the info strings, which are a kind of key-value
	 * pairs.
	 *
	 * @param row the source MediaDescriptor for the info,  when null empty
	 *        value strings are used
	 */
	private void fillInfoPanel(int row) {
		TableModel model = mediaTable.getModel();

		Object masterObj = model.getValueAt(row, 5);
		Object linkedObj = model.getValueAt(row, 6);
		boolean isMaster = (masterObj instanceof Boolean)
			? ((Boolean) masterObj).booleanValue() : false;
		boolean isLinked = (linkedObj instanceof Boolean)
			? ((Boolean) linkedObj).booleanValue() : false;

		linkInfoLabel.setText("<html><table>" + "<tr><td>" +
			model.getColumnName(0) + "</td><td>" +
			((model.getValueAt(row, 0) != null) ? model.getValueAt(row, 0) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(1) + "</td><td>" +
			((model.getValueAt(row, 1) != null) ? model.getValueAt(row, 1) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(2) + "</td><td>" +
			((model.getValueAt(row, 2) != null) ? model.getValueAt(row, 2) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(3) + "</td><td>" +
			((model.getValueAt(row, 3) != null) ? model.getValueAt(row, 3) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(4) + "</td><td>" +
			((model.getValueAt(row, 4) != null) ? model.getValueAt(row, 4) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(5) + "</td><td>" +
			((model.getValueAt(row, 5) != null)
			? (isMaster ? ElanLocale.getString("LinkedFilesDialog.Label.Yes")
						: ElanLocale.getString("LinkedFilesDialog.Label.No")) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(6) + "</td><td>" +
			((model.getValueAt(row, 6) != null)
			? (isLinked
			? ElanLocale.getString("LinkedFilesDialog.Label.StatusLinked")
			: ElanLocale.getString("LinkedFilesDialog.Label.StatusMissing")) : "") +
			"</td></tr>" + "</table></html>");
	}

	/**
	 * Shows a FileChooser for a mediafile.
	 *
	 * @param mediaType an elan file filter type
	 *
	 * @return the full path to a mediafile as a String, or null
	 */
	private String chooseMediaFile(int mediaType, boolean updating) {
		FileChooser chooser = new FileChooser(this);
		String[] mainExt = null;
		List<String[]> extensions = null;
		
		switch(mediaType){
		case ElanFileFilter.MEDIA_TYPE:
			mainExt = FileExtension.MEDIA_EXT;
			extensions = new ArrayList<String[]>();		
			extensions.add(FileExtension.MEDIA_EXT);
			extensions.add(FileExtension.MPEG_EXT);
			extensions.add(FileExtension.WAV_EXT);
			extensions.add(FileExtension.MPEG4_EXT);
			extensions.add(FileExtension.QT_EXT);		
			if (!updating) {
				// june 2008: set the all files filter as the default
				mainExt = null;				
				// jan 2009 use a user preference
				Integer val = Preferences.getInt("Media.LastUsedMediaType", null);
				if (val != null) {
					int type = val.intValue();
					if (type == ElanFileFilter.MEDIA_TYPE) {
						mainExt = FileExtension.MEDIA_EXT;						
					} else if (type == ElanFileFilter.MP4_TYPE) {
						mainExt = FileExtension.MPEG4_EXT;
					} else if (type == ElanFileFilter.QT_TYPE) {
						mainExt = FileExtension.QT_EXT;
					}
				}
			}
			break;
		case ElanFileFilter.MPEG_TYPE:
			mainExt = FileExtension.MPEG_EXT;
			break;
		case ElanFileFilter.WAV_TYPE:
			mainExt = FileExtension.WAV_EXT;
			break;
		}
		
		chooser.createAndShowFileDialog(ElanLocale.getString("LinkedFilesDialog.SelectMediaDialog.Title"), FileChooser.OPEN_DIALOG, 
					ElanLocale.getString("LinkedFilesDialog.SelectMediaDialog.Approve"), extensions, mainExt, true, "MediaDir", FileChooser.FILES_ONLY, null);
		
		File selected = chooser.getSelectedFile();
		if (selected != null) {
			String ext = FileUtility.getExtension(selected);
			if (!updating) {
				int type = -1;
				List<String> extensionsList = Arrays.asList(FileExtension.MEDIA_EXT);
				if (extensionsList.contains(ext)) {
					type = ElanFileFilter.MEDIA_TYPE;
				} 
				
				extensionsList = Arrays.asList(FileExtension.MPEG4_EXT);
				if (extensionsList.contains(ext)) {
					type = ElanFileFilter.MP4_TYPE;
				} 
				
				extensionsList = Arrays.asList(FileExtension.QT_EXT);
				if (extensionsList.contains(ext)) {
					type = ElanFileFilter.QT_TYPE;
				}				
				Preferences.set("Media.LastUsedMediaType", Integer.valueOf(type), null);
			}
			return selected.getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Presents an input pane for an rtsp streaming address.
	 * Copied from MultiFileChooser.
	 * @return the string/url of the new file.
	 */
	private String chooseRemoteFile() {
		String url = "rtsp://";
		
		for (;;) {
		    Object rf = JOptionPane.showInputDialog(this, 
	                ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteLabel"), 
	                ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"),
	                JOptionPane.PLAIN_MESSAGE,
	                null, null,
	                url);
	        if (rf == null) {
	            return null;
	        }
	        url = (String) rf;
	        url.replace('\\', '/');
	        // try some simple repairs
	        boolean valid = true;
	        if (!url.startsWith("rtsp")) {
	            int ds = url.indexOf("//");
	            if (ds > -1) {
	                url = "rtsp:" + url.substring(ds);
	            } else {
	                url = "rtsp://" + url;
	            }
	        }
	        if (url.indexOf("://") != 4) {
	            valid = false; // do not even try to repair
	        }
	        int dot = url.lastIndexOf('.');
	        int slash = url.lastIndexOf('/');
	        if (dot < 0 || dot < slash) {
	            valid = false;
	        }
	        
	        // no use trying to check the string as an URL (doesn't know the rtsp protocol)
	        // or URI (accepts almost any string)
	        
	        if (valid) {
	        	return url;
	        }
            JOptionPane.showMessageDialog(this, 
                    ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMessage") + url, 
                    ElanLocale.getString("Message.Error"), 
                    JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Shows a warning/error dialog with the specified message string.
	 *
	 * @param message the message to display
	 */
	private void showWarningDialog(String message) {
		JOptionPane.showMessageDialog(this, message,
			ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Shows a yes-no option dialog with the specified question.
	 *
	 * @param question the question
	 *
	 * @return true if the user's answer is confirmative, false otherwise
	 */
	private boolean showOptionDialog(String question) {
		int option = JOptionPane.showOptionDialog(this, question,
				ElanLocale.getString("LinkedFilesDialog.Title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new String[] {
					ElanLocale.getString("Button.Yes"),
					ElanLocale.getString("Button.No")
				}, ElanLocale.getString("Button.Yes"));

		return (option == JOptionPane.YES_OPTION);
	}

	/**
	 * Lets the user select a linked video file that has been the source for
	 * the audio file in the specified MediaDescriptor.
	 *
	 * @param forAudioMD the descriptor of the audio file
	 *
	 * @return the url of the source video file
	 */
	private String showVideoSelectionDialog(MediaDescriptor forAudioMD) {
		if (forAudioMD == null) {
			return null;
		}

		List<String> videos = new ArrayList<String>();
		videos.add(NO_SOURCE);

		MediaDescriptor md;

		for (int i = 0; i < currentMDCopy.size(); i++) {
			md = currentMDCopy.get(i);

			if (md.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
				videos.add(md.mediaURL);
			}
		}

		String option = (String) JOptionPane.showInputDialog(this,
				ElanLocale.getString("LinkedFilesDialog.Question.SelectSource"),
				ElanLocale.getString("LinkedFilesDialog.Title"),
				JOptionPane.QUESTION_MESSAGE, null, videos.toArray(), NO_SOURCE);

		return option;
	}

	/**
	 * Tests the media descriptors for video files and extracted audio files
	 * with different offsets.  When found a warning message is displayed.
	 */
	private void checkUnequalOffsets() {
		if (currentMDCopy.size() < 2) {
			return;
		}

		StringBuilder mesBuf = null;
		MediaDescriptor amd;
		MediaDescriptor vmd;

		for (int i = 0; i < currentMDCopy.size(); i++) {
			amd = currentMDCopy.get(i);

			if (amd.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
				for (int j = 0; j < currentMDCopy.size(); j++) {
					vmd = currentMDCopy.get(j);

					if (MediaDescriptorUtil.isVideoType(vmd)) {
						if (vmd.mediaURL.equals(amd.extractedFrom) && (j != 0) &&
								(vmd.timeOrigin != amd.timeOrigin)) {
							// add to the list
							if (mesBuf == null) {
								mesBuf = new StringBuilder(ElanLocale.getString(
											"LinkedFilesDialog.Message.OffsetNotEqual"));
								mesBuf.append("\n\n");
							}

							mesBuf.append("- " + vmd.mediaURL + "\n");
							mesBuf.append("- " + amd.mediaURL + "\n\n");

							break;
						}
					}
				}
			}
		}

		if (mesBuf != null) {
			showWarningDialog(mesBuf.toString());
		}
	}

	/**
	 * The action performed method.
	 *
	 * @param actionEvent DOCUMENT ME!
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();

		if (source == addMB) {
			addMediaDescriptor();
		} else if (source == removeMB) {
			removeMediaDescriptor();
		} else if (source == updateMB) {
			updateMediaDescriptor();
		} else if (source == masterMB) {
			setMasterMedia();
		} else if (source == extractMB) {
			setExtractedFrom();
		} else if (source == moveUpButton) {
			moveUp();
		} else if (source == moveDownButton) {
			moveDown();
		} else if (source == addRemoteMB) {
		    addRemoteMedia();
		}
	}

	/**
	 * Updates some buttons after a change in the selected row.
	 *
	 * @param lse the event
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent lse) {
		if (!lse.getValueIsAdjusting()) {
			if (!offsetEditMode) {
				updateUIComponents();
			} else { // only time offsets can be changed
				int row = mediaTable.getSelectedRow();

				if ((row >= 0) && (row < currentMDCopy.size())) {
					fillInfoPanel(row);
				}
			}
		}
	}

	/**
	 * Updates some buttons after a change in the table model.
	 *
	 * @param tme the event
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent tme) {
		if(tme.getColumn() < 0) {// a row has been added, removed or moved
			((MediaDescriptorTableModel) mediaTable.getModel()).setGlobalCellEditable(false);
			updateUIComponents();
		} else {// column is the time offset column, test this?
			// only the offset column should be editable, disable all buttons if the offset changed
			if (tme.getColumn() == ((MediaDescriptorTableModel) mediaTable.getModel()).findColumn(
					ElanLocale.getString(LinkedFilesTableModel.LABEL_PREF + LinkedFilesTableModel.OFFSET))) {
				MediaDescriptor md = currentMDCopy.get(tme.getFirstRow());
				Integer newValue = (Integer) ((MediaDescriptorTableModel) mediaTable.getModel()).getValueAt(
						tme.getFirstRow(), tme.getColumn());
				if (newValue.intValue() != md.timeOrigin) {
					offsetEditMode = true;
					addMB.setEnabled(false);
					addRemoteMB.setEnabled(false);
					removeMB.setEnabled(false);
					updateMB.setEnabled(false);
					masterMB.setEnabled(false);
					extractMB.setEnabled(false);
					moveUpButton.setEnabled(false);
					moveDownButton.setEnabled(false);
				}				
			}

		}
		
	}
}
