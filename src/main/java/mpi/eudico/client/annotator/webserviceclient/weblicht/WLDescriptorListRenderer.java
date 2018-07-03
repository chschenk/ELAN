package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;

/**
 * A minimal cell renderer for display of information on web services.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WLDescriptorListRenderer extends DefaultListCellRenderer {

	/**
	 * No-arg constructor.
	 */
	public WLDescriptorListRenderer() {
		super();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof WLServiceDescriptor) {
			WLServiceDescriptor wlDesc = (WLServiceDescriptor) value;
			String description = "";
			if (wlDesc.description != null) {
				description = maxLengthString(80, wlDesc.description);
			}
			
			JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, 
					isSelected, cellHasFocus);
			
			StringBuilder sb = new StringBuilder("<html><table><tr><td><b>");
			sb.append(wlDesc.name);
			sb.append("</b></td><td>");
			sb.append(wlDesc.creator);
			sb.append("</td></tr><tr><td colspan=\"2\">");
			sb.append(description);
			sb.append("</td></tr></table></html>");
			component.setText(sb.toString());
			component.setToolTipText(multiLineHTML(80, wlDesc.description));
			
			if (index % 2 == 0 && !isSelected) {
				component.setBackground(Constants.EVEN_ROW_BG);
			}
			
			return component;
		} 
		
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}

	/**
	 * Returns a shorter substring.
	 * 
	 * @param length the max length of the string to produce
	 * @param input the input string
	 * @return a cut off version of the string
	 */
	private String maxLengthString(int length, String input) {
		if (input.length() <= length) {
			return input;
		} else {
			int index = input.lastIndexOf(" ", length);
			if (index > -1) {
				return input.substring(0, index);
			} else {
				return input.substring(0, length);
			}
		}
	}
	
	/**
	 * Creates a multiple-line html string for in a tool tip.
	 *  
	 * @param width the (rough) width in characters of
	 * @param input the input text
	 * @return an html formatted tool tip text
	 */
	private String multiLineHTML(int width, String input) {
		if (input.length() > width) {
			StringBuilder sb = new StringBuilder("<html>");
			int curCut = 0;
			int curBI = 0;
			int curEI = width;
			
			while (curEI <= input.length()) {
				curCut = input.indexOf(" ", curEI);
				if (curCut > -1) {
					sb.append(input.substring(curBI, curCut));
					sb.append("<br>");
					curBI = curCut;
					curEI = curBI + width;
					
					if (curEI >= input.length()) {
						sb.append(input.substring(curCut));
						break;
					}
				} else {
					sb.append(input.substring(curEI));
					sb.append("<br>");
				}
			}
			sb.append("</html>");
			
			return sb.toString();
		}
		
		return input;
	}
}
