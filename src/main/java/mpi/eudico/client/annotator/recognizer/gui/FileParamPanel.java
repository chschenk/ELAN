package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * A panel to specify a file or folder or a media file 
 * for input or output from a detector.
 *  
 * @author Han Sloetjes
 * @updated June 2012, by aarsom
 */
@SuppressWarnings("serial")
public class FileParamPanel extends AbstractParamPanel implements ActionListener {
	private boolean inputType;
	private int contentType;
	private boolean optional;
	private String initialPath;
	private List<String> mediaFiles;
	private List<String> mimeTypes;
	protected Insets insets =  new Insets(1, 1, 0, 1);
	private boolean acceptAllFiles = false;
	
	protected JTextField fileField;
	protected JButton browseButton;	
	private JComboBox mediaCB;
	
	/**
	 * The name of the parameter.
	 * 
	 * @param paramName the name of the parameter
	 * @param inputType if true, this is for selecting an input file for the detector, otherwise
	 * a file or folder for output needs to be specified
	 * @param contentType the type of the content of the file, tier, timeseries, audio, video, auxiliary. 
	 * A constant from FileParam.
	 * @param optional if true this parameter does not have to be set
	 */
	public FileParamPanel(String paramName, String description, boolean inputType, int contentType, boolean optional) {
		this(paramName, description, inputType, contentType, optional, null);
	}
	
	/**
	 * The name of the parameter.
	 * 
	 * @param paramName the name of the parameter
	 * @param inputType if true, this is for selecting an input file for the detector, otherwise
	 * a file or folder for output needs to be specified
	 * @param contentType the type of the content of the file, tier, timeseries, audio, video, auxiliary. 
	 * A constant from FileParam.
	 * @param optional if true this parameter does not have to be set
	 * @param mediaFiles, list of media files that can be selected for this parameter
	 */
	public FileParamPanel(String paramName, String description, boolean inputType, int contentType, boolean optional, List<String> mediaFiles) {
		super(paramName, description);
		this.inputType = inputType;
		this.contentType = contentType;
		this.optional = optional;
		this.mediaFiles = mediaFiles;
		if (!inputType || (contentType != FileParam.TIER && contentType != FileParam.MULTITIER && contentType != FileParam.CSV_TIER)) {
			//not TierParamPanel
			initComponents();
		}
	}
	
	/**
	 * Constructor accepting a FileParam as an argument.
	 * 
	 * @param param the file param object
	 */
	public FileParamPanel(FileParam param) {
		this(param, null);		
	}
	
	/**
	 * Constructor accepting a FileParam as an argument.
	 * 
	 * @param param the file param object
	 * @param mediaFiles, list of media files that can be selected for this parameter
	 */
	public FileParamPanel(FileParam param, List<String> mediaFiles) {
		super(param);
		if (param != null) {
			this.mediaFiles = mediaFiles;
			mimeTypes = param.mimeTypes;
			inputType = (param.ioType == FileParam.IN);
			optional = param.optional;
			contentType = param.contentType;
			if (!inputType || 
					(contentType != FileParam.TIER && 
					 contentType != FileParam.MULTITIER &&
					 contentType != FileParam.CSV_TIER)) {
				// not TierParamPanel
				initComponents();
			}
		}
	}	

	/**
	 * Adds a textfield and a browse button  or 
	 * a combobox to the panel.
	 */
	@Override
	protected void initComponents() {
		super.initComponents();
			
		ImageIcon icon = null;
		try {
			if (inputType) {
				icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
			} else {
				icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Save16.gif"));
			}
		} catch (Exception ex) {
			
		}
		
		GridBagConstraints gbc;
		
		if (inputType && (contentType == FileParam.AUDIO || contentType == FileParam.VIDEO)) {
			mediaCB = new JComboBox();
			updateMediaFiles(mediaFiles);
			
			gbc = new GridBagConstraints();
			gbc.gridwidth = 3;
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;			
			gbc.insets = insets;
			add(mediaCB, gbc);
		} else {
			fileField = new JTextField();
			fileField.setEditable(true);
			browseButton = new JButton();// or icon?
			if (icon != null) {
				browseButton.setIcon(icon);
			} else {
				browseButton.setText("...");
			}
			browseButton.addActionListener(this);
			
			gbc = new GridBagConstraints();
			gbc.gridwidth = 2;
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = insets;
			add(fileField, gbc);
			
			gbc.gridx = 2;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			add(browseButton, gbc);
		}
				
		StringBuilder builder = new StringBuilder("<html><table><tr>");
		if (!optional) {
			builder.append("<td style=\"color:red\"> ");
		} else {
			builder.append("<td style=\"color:green\"> ");
		}
		
		if (contentType == FileParam.CSV_TS ) {
			builder.append("<small>[csv ts]</small>: </td><td>");
		}else if (contentType == FileParam.CSV_TIER) {
			builder.append("<small>[csv tier]</small>: </td><td>");
		}else if (contentType == FileParam.TIMESERIES ){
			builder.append("<small>[xml ts]</small>: </td><td>");
		}else if (contentType == FileParam.TIER ){
			builder.append("<small>[xml tier]</small>: </td><td>");
		}else if (contentType == FileParam.MULTITIER) {
			builder.append("<small>[xml tiers]</small>: </td><td>");
		}else if (contentType == FileParam.AUDIO) {
			builder.append("<small>[audio]</small>: </td><td>");
		}else if (contentType == FileParam.VIDEO) {
			builder.append("<small>[video]</small>: </td><td>");
		}else {
			builder.append("<small>[aux]</small>: </td><td>");
		}
		builder.append(description);
		if(showParamNames){
			builder.append(" <i>[" + paramName + "]</i>");
		}		
		builder.append("</td><tr></table></html>");
		descLabel.setText(builder.toString());
	}	

	/**
	 * Returns the path to the file or folder or 
	 * selected media file.
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 */
	@Override
	protected Object getParamValue() {
		if(mediaCB != null){
			if(mediaFiles != null && mediaFiles.size() > 0){				
				return mediaFiles.get(mediaCB.getSelectedIndex());
			} 
		} else{
			if(fileField.getText() != null && fileField.getText().trim().length() > 0){
				return this.fileField.getText();
			}
		}	
		
		return null;
	}

	/**
	 * Sets a previously stored value for the path or
	 * sets the previously selected media file.
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#setParamValue(java.lang.Object)
	 */
	@Override
	protected void setParamValue(Object value) {
		if(value instanceof String){
			if (mediaCB != null && mediaFiles != null && mediaFiles.contains(value)){
				String name = FileUtility.fileNameFromPath((String)value);
				if(name != null){					
					mediaCB.setSelectedItem(name);	
				}
			} else if (fileField != null) {
				initialPath = (String) value;
				fileField.setText(initialPath);
			}
		}
	}
	
	/**
	 * Updates the new media files that are supported 
	 * by the current recognizer.
	 * 
	 * @param mediaFilePaths
	 */
	public void updateMediaFiles(List<String> mediaFilePaths){
		mediaFiles = new ArrayList<String>();
		
		if(mediaCB != null){
			String selectedMedia = (String) mediaCB.getSelectedItem();
			boolean selectedMediaFound = false;
			mediaCB.removeAllItems();
			if(mediaFilePaths != null){
				for(String s: mediaFilePaths){
					String name = FileUtility.fileNameFromPath(s);
					if(name != null && isMediaSupported(s)){
						mediaCB.addItem(name);	
						mediaFiles.add(s);
						if(name.equals(selectedMedia)){
							selectedMediaFound = true;
						}
					}
				}
			}
			
			if(selectedMediaFound){
				mediaCB.setSelectedItem(selectedMedia);
			} else if(mediaCB.getItemCount() >0){
				mediaCB.setSelectedIndex(0);
			} else {
				mediaCB.addItem("No supported media files available");		
			}
		}		
	}
	
	/**
	 * Check whether the given media file is supported
	 * for this parameter
	 * 
	 * @param mediaFilePath, media file to be checked
	 * 
	 * @return boolean true, if this param supports the
	 *                       given media file
	 * 
	 */
	public boolean isMediaSupported(String mediaFilePath) {
		if (mediaFilePath == null || mediaFilePath.length() == 0) {
			return false;
		}
		
		int index = mediaFilePath.lastIndexOf('.');
		String ext = null;
		
		if (index > -1 && index < mediaFilePath.length() - 1) {
			ext = mediaFilePath.substring(index + 1);
		}
		
		if (ext == null || ext.length() == 0) {
			return false; // or just true?
		}
		
		String mime = MediaDescriptorUtil.mimeTypeForExtension(ext);
		
		if (mimeTypes != null) {
			if (mimeTypes.contains(mime)) {
				return true;
		    } else {
		    	for(String type : mimeTypes){
		    		if(contentType == FileParam.AUDIO){
		    			if((type.startsWith("audio") && mime.startsWith("audio")) ||
		    				(type.startsWith("video") && mime.startsWith("video"))){
			    			return true;
			    		}
		    		}else{
		    			if(type.startsWith("video") && mime.startsWith("video")){
				    		return true;
				    	}
		    		}		    		
		    	}
		    	
		    	//if the mimeTypes are invalid (say doesn't start with audio/video)
		    	if(mime.startsWith("audio") && contentType == FileParam.AUDIO){
	    			//
	    			return true; 
	    		} else if(mime.startsWith("video") && contentType == FileParam.VIDEO){
	    			return true; 
	    		}
			}		
		} else {
			if (contentType == FileParam.AUDIO) {
				return true; // if no mimetypes specified audio from video files could be supported
			} else if (contentType == FileParam.VIDEO) {
				if (mime != MediaDescriptor.GENERIC_AUDIO_TYPE || mime != MediaDescriptor.WAV_MIME_TYPE) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns whether this file is optional.
	 * 
	 * @return whether this file is optional
	 */
	public boolean isOptional() {
		return optional;
	}
	
	/**
	 * 
	 * @return the content type, timeseries or tier in csv or xml or
	 * audio or video
	 */
	public int getContentType() {
		return contentType;
	}
	
	/**
	 * 
	 * @return 
	 */
	public boolean isFileInput(){			
		if(fileField != null ) {
			return true;
		}	
		return false;		
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isValueFilled(){		
		Object value = getParamValue();
		if (value instanceof String) {
			if (((String) value).trim().length() > 0) {
				return true;					
			} 
		} 		
		return false;		
	}
	
	/**
	 * 
	 * @return whether this is an input or output file panel
	 */
	public boolean isInputType() {
		return inputType;
	}
	
	/**
	 * Get the supported file extensions for this parameter
	 */
	public List<String[]> getFileTypeExtension(){
		List<String[]> extensions = null;
		if (contentType == FileParam.CSV_TS || contentType == FileParam.CSV_TIER) {
			acceptAllFiles = false;
			extensions = new ArrayList<String[]>();
			extensions.add(FileExtension.CSV_EXT);			
		} else if (contentType == FileParam.TIMESERIES || contentType == FileParam.TIER || contentType == FileParam.MULTITIER) {
			acceptAllFiles = false;
			extensions = new ArrayList<String[]>();
			extensions.add(FileExtension.XML_EXT);
		} else if(contentType == FileParam.AUDIO || contentType == FileParam.VIDEO)	{
			if(mimeTypes != null){
				extensions = new ArrayList<String[]>();
				String mimeType;
				String[] extArray;
				for(int i=0; i < mimeTypes.size(); i++){	
					mimeType = mimeTypes.get(i);
					extArray = MediaDescriptorUtil.extensionForMimeType(mimeType);
					if(extArray != null && !extensions.contains(extArray)){
						extensions.add(extArray);
					}	
					
					if(extArray == null){
						if (mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) || mimeType.startsWith("audio")) {
							acceptAllFiles = true;
						}  else if (mimeType.equals(MediaDescriptor.GENERIC_VIDEO_TYPE) || mimeType.startsWith("video")) {
							acceptAllFiles = true;
						} 
					}					
				}
				
				if(extensions.size() == 0){
					extensions = null;
				}
			}			
		}
		
		return extensions;
		
	}
	
	/**
	 * Only the browse button can currently be the source.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == browseButton){
			FileChooser chooser = new FileChooser(this);
			if(initialPath != null) {
				chooser.setCurrentDirectory(initialPath);		
			}
			
			int dialogType = -1;		
	
			if (inputType) {
				dialogType = FileChooser.OPEN_DIALOG;			
			} else {
				// output
				dialogType = FileChooser.SAVE_DIALOG;
			}
			
			List<String[]> extensions = getFileTypeExtension();
			String[] mainFilterExt = null;
			if(extensions != null && extensions.size() > 0){
				mainFilterExt = extensions.get(0);
			}
			
			String title = null;
			if(contentType == FileParam.AUDIO){
				title = "Select a audio file";
			} else if(contentType == FileParam.VIDEO){
				title = "Select a video file";
			} else if(contentType == FileParam.TIER || contentType == FileParam.CSV_TIER){
				title = "Select a tier file";
			}  else if( contentType == FileParam.MULTITIER ){
				title = "Select a multitier file";
			}			
			else if(contentType == FileParam.TIMESERIES || contentType == FileParam.CSV_TS){
				title = "Select a timeseries file";
			}
			
			chooser.createAndShowFileDialog(title, dialogType, "select", extensions, mainFilterExt, acceptAllFiles, "Recognizer.Dir", FileChooser.FILES_ONLY, null);
			File f = chooser.getSelectedFile();
			if(f != null){
				initialPath = f.getAbsolutePath();
				fileField.setText(initialPath);
			}
		}
	}
}
