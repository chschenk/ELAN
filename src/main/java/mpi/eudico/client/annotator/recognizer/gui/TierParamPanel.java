package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.data.FileParam;

/** A param panel used when the parameter requires
 * a tier-csv/tier- xml type input. For such param types,
 * this panel gives a possibility to select the input as a 
 * list of selections or a tier or a file(csv/xml).
 * 
 * The Parameter value that is returned is a Map, with one entry.
 * See {@link #getParamValue()}.
 * 
 * @author aarsom 
 * @created Sep 2012
 */
@SuppressWarnings("serial")
public class TierParamPanel extends FileParamPanel{
		
	private TierSelectionPanel selectionPanel;	
	
	public TierParamPanel(FileParam param, TierSelectionPanel panel) {
		super(param);
		if (param != null) {				
			selectionPanel  = panel;			
			initComponents();
			
			if (isInputType()) {
				selectionPanel.setFileDialogType(FileChooser.OPEN_DIALOG);
			} else {
				// output
				selectionPanel.setFileDialogType(FileChooser.SAVE_DIALOG);
			}
			
			selectionPanel.setFileExtensions(getFileTypeExtension());
		}
	}
	
	/**
	 * Adds a selection panel to the panel.
	 */
	@Override
	protected void initComponents() {		
		setLayout(new GridBagLayout());	
		
		descLabel = new JLabel(description);
		StringBuilder builder = new StringBuilder("<html><table><tr>");
		if (!isOptional()) {
			builder.append("<td style=\"color:red\">");
		} else {
			builder.append("<td style=\"color:green\">");
		}
			
		if (getContentType() == FileParam.CSV_TIER ) {
			builder.append("<small>[csv tier]</small>: </td><td>");
		} else if (getContentType() == FileParam.TIER) {
			builder.append("<small>[xml tier]</small>: </td><td>");
		} else if (getContentType() == FileParam.MULTITIER) {
			builder.append("<small>[xml tiers]</small>: </td><td>");
		}
		
		builder.append(description);
		if(showParamNames){
			builder.append(" <i>[" + paramName + "]</i>");
		}
		
		builder.append("</td><tr></table></html>");
		descLabel.setText(builder.toString());	
		
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(1, 1, 0, 1);
		add(descLabel, gbc);
		
		gbc.gridy = 1;			
//		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		add(selectionPanel, gbc);
		}
	
	/**
	 * Updates the new media files that are supported by
	 * the current recognizer.
	 * 
	 */
	@Override
	public void updateMediaFiles(List<String> mediaFilePaths){
		super.updateMediaFiles(mediaFilePaths);
		
		if(selectionPanel.getMode() == Recognizer.VIDEO_TYPE){
			selectionPanel.updateMediaFiles(mediaFilePaths);
		}
	}
	
	/**
	 * Returns a hash map specifying the selected input
	 * type as a key (selection/tier,/file) and the selected
	 * values as value in the map.
	 * 
	 * Possible Key-value pairs in a map
	 *   TierSelectionPanel.SELECTIONS, List<RSelection>
	 *   
	 *   TierSelectionPanel.TIER, List<RSelection>
	 *   TierSelectionPanel.TIER_NAME, String(tier name)
	 *   
	 *   TierSelectionPanel.FILE_NAME, String(filePath)
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 */
	@Override
	protected Object getParamValue() {	
		return selectionPanel.getParamValue();
	}	
	
	/**
	 * Converts the given map into to map that can be 
	 * written/stored in the preference file.
	 * 
	 * @param map - the map to be converted
	 * @return the converted map
	 */
	public Map getStorableMap(Map map){		
		return selectionPanel.getStorableParamPreferencesMap(map);
	}
	 
	/** Check if all the parameter vales are filled
	 * 
	 * @return true, if all the values are filled, else
	 * 				 returns false.
	 */
	@Override
	public boolean isValueFilled(){		
		Object value = getParamValue();
		if(value instanceof Map){
			Map map = (Map)value;
			if(map.containsKey(TierSelectionPanel.SELECTIONS)){
				value = map.get(TierSelectionPanel.SELECTIONS);
			} else if(map.containsKey(TierSelectionPanel.TIER)){
				value = map.get(TierSelectionPanel.TIER);
			} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
				value = map.get(TierSelectionPanel.FILE_NAME);
			}
			
			if (value instanceof String) {
				//return super.isValueFilled();
				return ((String) value).trim().length() > 0;
			} else if(value instanceof List && ((List)value).size() > 0){
				return true;
			}
		}
		
		return false;		
	}
	
	/**
	 * Sets a previously stored value parameters
	 * in this panel
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#setParamValue(java.lang.Object)
	 */
	@Override
	protected void setParamValue(Object value) {
		if(value instanceof String){
			selectionPanel.setParamValue((String)value);
		} else if(value instanceof java.util.HashMap){
			selectionPanel.setParamValue((java.util.HashMap)value);
		}
	}
}

