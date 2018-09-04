package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.recognizer.data.AudioSegment;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.util.TimeFormatter;

/**
 * A renderer for a (mixed) list of RSelection, Segment and Segmentation objects.
 *  
 * 
 * @author Han Sloetjes
 *
 */
public class SelectionListRenderer extends DefaultListCellRenderer {

	@Override
	/**
	 * Calls super(...) sets the label text for known objects.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
		if (value instanceof AudioSegment) {
			StringBuilder sb = new StringBuilder();
			AudioSegment as = (AudioSegment) value;
			sb.append(' ');
			sb.append(as.channel);
			sb.append(' ');
			sb.append(TimeFormatter.toString(as.beginTime));
			sb.append(" - ");
			sb.append(TimeFormatter.toString(as.endTime));
			setText(sb.toString());
		} else if (value instanceof RSelection) {
			StringBuilder sb = new StringBuilder();
			RSelection rs = (RSelection) value;
			sb.append(TimeFormatter.toString(rs.beginTime));
			sb.append(" - ");
			sb.append(TimeFormatter.toString(rs.endTime));
			setText(sb.toString());
		} else if (value instanceof Segmentation) {
			Segmentation sg = (Segmentation) value;
			if (sg.getMediaDescriptors() != null && sg.getMediaDescriptors().size() > 0) {
				MediaDescriptor md = (MediaDescriptor) sg.getMediaDescriptors().get(0);
				int channel = md.channel;
				setText(" " + channel + " " + ElanLocale.getString("Menu.Tier") + ": " + ((Segmentation) value).getName());
			} else {
				setText(ElanLocale.getString("Menu.Tier") + ": " + ((Segmentation) value).getName());	
			}
		}
		
		return this;
	}

}
