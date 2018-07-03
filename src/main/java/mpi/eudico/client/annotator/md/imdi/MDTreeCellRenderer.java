package mpi.eudico.client.annotator.md.imdi;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

/**
 * A tree cell renderer for imdi metadata key value pairs.
 * The keys are painted in bold font, the values in plain font.
 * Long metadata values are placed in a text area, the wrapping is done
 * by the renderer instead of the textarea in order to be able to determine
 * the number of lines and from that to calculate the preferred size for
 * the area and the component as a whole.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class MDTreeCellRenderer extends JPanel 
implements TreeCellRenderer {
	private final int MARGIN = 4;
	private final char NL = '\n';
	private Font plainFont;
	private Font boldFont;
	private ImageIcon sessionIcon;
	private ImageIcon mediaIcon;
	private ImageIcon audioIcon;
	private ImageIcon videoIcon;
	private ImageIcon writtenIcon;
	private ImageIcon annIcon;
	private ImageIcon eafIcon;
	
	private JLabel keyLabel;
	private JTextArea valueArea;
	private Dimension preferredSize;
	private int expanderSize = 0;
	
	/**
	 * Constructor, initializes the components and loads icons.
	 */
	public MDTreeCellRenderer() {
		super();
		keyLabel = new JLabel();
		keyLabel.setOpaque(true);
		
		valueArea = new JTextArea();
		valueArea.setLineWrap(false);
		valueArea.setWrapStyleWord(false);
		valueArea.setOpaque(true);
		//valueArea.setBackground(Color.RED);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		
		add(keyLabel, gbc);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		add(valueArea, gbc);
		preferredSize = new Dimension(500, 40);
		
		UIDefaults uid = UIManager.getDefaults();
		Enumeration<Object> enKey = uid.keys();
		while (enKey.hasMoreElements()) {
			Object obj = enKey.nextElement();
			//System.out.println("K: " + obj + " V: " + UIManager.get(obj));
			if ("Tree.expandedIcon".equals(obj)) {
				Icon expander = uid.getIcon(obj);
				if (expander != null) {
					//System.out.println("Expander Size: " + expander.getIconWidth());
					expanderSize = expander.getIconWidth();
				} else {
					System.out.println("Tree: no expander icon: ");
				}
				break;
			}
		}

		// icons
		try {
			sessionIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/session_color.png"));
		} catch (Throwable t){
			sessionIcon = null;
		}
		try {
			mediaIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/mediafile.png"));
		} catch (Throwable t){
			mediaIcon = null;
		}
		try {
			audioIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/audio.png"));
		} catch (Throwable t){
			audioIcon = null;
		}
		try {
			videoIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/video.png"));
		} catch (Throwable t){
			videoIcon = null;
		}
		
		try {
			writtenIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/writtenresource.png"));
		} catch (Throwable t){
			writtenIcon = null;
		}
		try {
			annIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/annotation.png"));
		} catch (Throwable t){
			annIcon = null;
		}
		try {
			eafIcon = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));
		} catch (Throwable t){
			eafIcon = null;
		}
	}


	/**
	 * Configures the components for the object in the specified row.
	 * It tries to take into account the (display) width of the tree 
	 * (probably the width of the viewport of a scrollpane) to wrap the text of
	 * the metadata key-value pair. The key is displayed in bold and is placed
	 * in a JLabel, optionally with an icon and with as much text of the value 
	 * as fits in the space. The remainder of the value is added to a text area.
	 * The line wrapping is done by this renderer in order to be able to retrieve 
	 * the correct number of lines and from there to be able to calculate the height
	 * of the components. In a tree with variable row height the renderer component's
	 * preferred size is used to determine the height for each row. 
	 * The parameters selected, expanded, leaf and hasFocus are currently ignored.
	 * Note: Instead of a JLabel and a JTextArea a JTextPane or JEditorPane could
	 * be used, with styles. 
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		//System.out.println("Tree width: " + tree.getWidth());// get width can be used to determine available width
		//System.out.println("Tree display width: " + ((MDTree)tree).getDisplayWidth());
		int width = tree.getWidth();
		if (tree instanceof MDTree) {
			if (((MDTree) tree).getDisplayWidth() != 0) {
				width = ((MDTree) tree).getDisplayWidth() - MARGIN;
				//System.out.println("Tree display width2: " + ((MDTree)tree).getDisplayWidth());
			}
		}
		
		if (width <= 0) {
			width = 600;
		}
		int rowIndent = 0;
		// getPathBounds and getRowBounds cannot be used here to get information about the x value of the component
		
		valueArea.setText("");
		if (selected) {
			//setBackground(tree.);
		} else {
			setBackground(tree.getBackground());
			keyLabel.setBackground(tree.getBackground());
			valueArea.setBackground(tree.getBackground());
		}
		if (plainFont != tree.getFont()) {
			this.setFont(tree.getFont());
		}
		
		if (value instanceof DefaultMutableTreeNode) {
			Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
			// how to get to the indent per level? Is expander size reliable?
			int level = ((DefaultMutableTreeNode) value).getLevel();
			if (level > 0) {
				level--;
			}
			rowIndent = level * expanderSize;
			
			if (userObj instanceof MDKVData) {
				MDKVData mdkv = (MDKVData) userObj;
				String remains = setLabel(mdkv, width - rowIndent);
				
				if (remains != null) {
					setText(remains, width - rowIndent);//sets preferred size
				} else {
					valueArea.setPreferredSize(new Dimension(0, 0));
				}
				
			} else if (userObj instanceof String) {
				keyLabel.setText((String) userObj);
				valueArea.setPreferredSize(new Dimension(0, 0));
			}
			
		}
//		int valHeight = valueArea.getPreferredSize().height;
//		int labHeight = keyLabel.getPreferredSize().height;
//		if (valHeight > 0) {
//			int nt = valHeight / labHeight;
//			valHeight = (nt + 1) * labHeight; // exact n times the height of the label
//		}
		preferredSize = new Dimension(width - rowIndent + MARGIN, keyLabel.getPreferredSize().height + 
				valueArea.getPreferredSize().height);		
		this.setPreferredSize(preferredSize);

		return this;
	}


	@Override
	public void setFont(Font font) {
		super.setFont(font);
		plainFont = font;
		if (font != null) {
			boldFont = font.deriveFont(Font.BOLD);
			if (keyLabel != null) {
				keyLabel.setFont(font);
			}
			if (valueArea != null) {
				valueArea.setFont(font);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		// System.out.println("getPreferredSize: " + preferredSize);
		return new Dimension(preferredSize);
	}

	/**
	 * Sets the html formatted text for the label part. It consists of the key and part of the value.
	 * 
	 * 
	 * @param mdkv
	 * @param availWidth
	 * @return
	 */
	private String setLabel(MDKVData mdkv, int availWidth) {
		// set icon
		if (sessionIcon != null && mdkv.key.endsWith("Session")) {
			keyLabel.setIcon(sessionIcon);
			availWidth -= sessionIcon.getIconWidth();
		} else if (mediaIcon != null && mdkv.key.endsWith("MediaFile")){
			keyLabel.setIcon(mediaIcon);
			availWidth -= mediaIcon.getIconWidth();
		} else if (writtenIcon != null && mdkv.key.endsWith("WrittenResource")) {
			keyLabel.setIcon(writtenIcon);
			availWidth -= writtenIcon.getIconWidth();
		} else if (mdkv.key.equals("Type")) {
			if (mdkv.value != null) {
				String lower = mdkv.value.toLowerCase();
				if (lower.equals("video")) {
					keyLabel.setIcon(videoIcon);
				} else if (lower.equals("audio")) {
					keyLabel.setIcon(audioIcon);
				} else if (lower.equals("annotation")) {
					keyLabel.setIcon(annIcon);
				} else {
					keyLabel.setIcon(null);
				}
			} else {
				keyLabel.setIcon(null);
			}
		} else if (mdkv.key.equals("Format")) {
			if (mdkv.value != null && mdkv.value.equals("text/x-eaf+xml")) {
				keyLabel.setIcon(eafIcon);
			}
		} else {
			keyLabel.setIcon(null);
		}
		
		if (mdkv.value == null || mdkv.value.length() == 0) {
			keyLabel.setText(getHtmlString(mdkv.key, null));
		} else {
			// key + part of value?
			// split the value into a part that is in the label with the key
			String[] parts = splitValueText(mdkv, availWidth);
			keyLabel.setText(getHtmlString(mdkv.key, parts[0]));
			return parts[1];
		}
		
		return null;
	}

	/**
	 * Sets the text for the value text area. Delegates to 
	 * {@link #setText(String, int)}
	 * @param mdkv the metadata key value pair
	 * @param availWidth the available width for the label
	 */
	private void setText(MDKVData mdkv, int availWidth) {
		if (mdkv.value == null || mdkv.value.length() == 0) {
			valueArea.setText("");
			valueArea.setPreferredSize(new Dimension(0, 0));
			return;
		} else {
			setText(mdkv.value, availWidth);
		}
	}
	
	/**
	 * 
	 * @param value the text to split into lines and to add to the text area.
	 * @param availWidth the width for the text area
	 */
	private void setText(String value, int availWidth) {
		if (value == null || value.length() == 0) {
			valueArea.setText("");
			valueArea.setPreferredSize(new Dimension(0, 0));
			return;
		}
		FontMetrics fm = valueArea.getFontMetrics(plainFont);
		Graphics g = valueArea.getGraphics();
		StringBuilder builder = new StringBuilder(value);
    	int beginIndex = 0;
    	int endIndex = -1;
    	int lastTestedEndIndex = 0;
    	char[] seq;
    	
    	for (int i = 0; i < builder.length(); i++) {
    		endIndex++;
    		if (Character.isWhitespace(builder.charAt(i))) {
				if (endIndex > beginIndex) {
					seq = new char[endIndex - 1 - beginIndex];
					builder.getChars(beginIndex, endIndex - 1, seq, 0);
					if (fm.getStringBounds(seq, 0, seq.length, g).getWidth() > availWidth) {//too wide
						if (lastTestedEndIndex > beginIndex) {
							valueArea.append(builder.substring(beginIndex, lastTestedEndIndex + 1) + NL);
							beginIndex = lastTestedEndIndex + 1;
						} else {// one word wider than width, never mind? or split at availWidth
							// loop back from endIndex to beginIndex test per character
							for (int j = seq.length - 1, k = 1; j > 0; j--, k++) {
								if (fm.getStringBounds(seq, 0, seq.length - k, g).getWidth() < availWidth) {
									valueArea.append(new String(seq, 0, seq.length - k) + NL);
									beginIndex = endIndex - k + 1;
									lastTestedEndIndex = beginIndex;
									break;
								}
							}
							//beginIndex = endIndex + 1;
						}
					} else {
						lastTestedEndIndex = endIndex;
						if (builder.charAt(i) == NL) {// add this part of the line 
							valueArea.append(builder.substring(beginIndex, endIndex + 1));
							beginIndex = endIndex + 1;
						}
						//continue;
					}
				}// else ?
    			
    		} /*else {
    			//??
    		}*/
    		
    		if (i == builder.length() - 1) {
    			if (endIndex > beginIndex) {//remaining line
    				seq = new char[endIndex + 1 - beginIndex];
    				builder.getChars(beginIndex, endIndex + 1, seq, 0);
    				if (fm.getStringBounds(seq, 0, seq.length, g).getWidth() > availWidth) {//line too long
    	    			if (lastTestedEndIndex > beginIndex) {// whitespace in remaining bit, split in two
    	    				valueArea.append(builder.substring(beginIndex, lastTestedEndIndex + 1) + NL);
    	    				valueArea.append(builder.substring(lastTestedEndIndex + 1, endIndex + 1));
    	    				beginIndex = lastTestedEndIndex + 1;
    	    			} else {// one word too long, just split in two
							// loop back from endIndex to beginIndex test per character
							for (int j = seq.length - 1, k = 1; j > 0; j--, k++) {
								if (fm.getStringBounds(seq, 0, seq.length - k, g).getWidth() <= availWidth) {
									valueArea.append(new String(seq, 0, seq.length - k) + NL);
									valueArea.append(new String(seq, seq.length - k, k));
									break;
								}
							}
    	    			}
    				} else {
    					valueArea.append(builder.substring(beginIndex, endIndex + 1));
    				}
    			}
    		}
    	}

    	valueArea.setPreferredSize(new Dimension(availWidth + MARGIN, valueArea.getLineCount() * fm.getHeight()));
	}
	
	/**
	 * Splits the value into a part that can be in the label with the key, the rest will be 
	 * in the text area for the md value.
	 * 
	 * @param mdkv the key value pair
	 * @param availWidth the available width for the label 
	 * @return an array of Strings, size 2, the first index is the part that fits in the key label
	 */
	private String[] splitValueText(MDKVData mdkv, int availWidth) {
		// assume mdkv is not null, key is not null, value is not null
		String[] parts = new String[2];
		FontMetrics boldMetrics = keyLabel.getFontMetrics(boldFont);
		int w = boldMetrics.stringWidth(mdkv.key + " ");
		if (w >= availWidth) {
			parts[0] = null;
			parts[1] = mdkv.value;
		} else {
			int valWidth = availWidth - w;
			FontMetrics pm = keyLabel.getFontMetrics(plainFont);
	    	StringBuilder builder = new StringBuilder(mdkv.value);
	    	int endIndex = -1;
	    	int lastTestedEndIndex = 0;
	    	char[] seq;
	    	
	    	for (int i = 0; i < builder.length(); i++) {
	    		endIndex++;
	    		if (Character.isWhitespace(builder.charAt(i))) {
	    			seq = new char[endIndex];
					builder.getChars(0, endIndex, seq, 0);
					if (pm.getStringBounds(seq, 0, seq.length, keyLabel.getGraphics()).getWidth() > valWidth) {//too wide
						if (lastTestedEndIndex == 0) {
							parts[0] = null;
							parts[1] = mdkv.value;
						} else {
							parts[0] = builder.substring(0, lastTestedEndIndex);
							if (lastTestedEndIndex + 1 < builder.length() - 1) {
								parts[1] = builder.substring(lastTestedEndIndex + 1);
							} else {
								parts[1] = null;
							}
						}
						return parts;
					} else {
						lastTestedEndIndex = endIndex;
						// break if a newline is encountered
						if (builder.charAt(i) == NL) {
							parts[0] = builder.substring(0, lastTestedEndIndex);
							if (lastTestedEndIndex + 1 < builder.length() - 1) {
								parts[1] = builder.substring(lastTestedEndIndex + 1);
							} else {
								parts[1] = null;
							}
							return parts;
						}
						
						if (endIndex == builder.length() - 1) {// everything fits on one row
							parts[0] = mdkv.value;
							parts[1] = null;
						}
					}
	    		} else {
					if (endIndex == builder.length() - 1) {
						if (pm.getStringBounds(mdkv.value, keyLabel.getGraphics()).getWidth() <= valWidth) {// everything fits on one row
							parts[0] = mdkv.value;
							parts[1] = null;
						} else {
							if (lastTestedEndIndex > 0) {
								parts[0] = builder.substring(0, lastTestedEndIndex);
								if (lastTestedEndIndex + 1 < builder.length() - 1) {
									parts[1] = builder.substring(lastTestedEndIndex + 1);
								} else {
									parts[1] = null;
								}
							} else {// no spaces
								parts[0] = null;
								parts[1] = mdkv.value;
							}
						}						
					}
	    		}
	    	}	
		}
		
		return parts;
	}
	
//################
    private final String HTML_B = "<html>";
    private final String HTML_E = "</html>";
    private final String B_B = "<b>";
    private final String B_E = "</b>";
    
    private String getHtmlString(String key, String val) {
    	if (key == null) {
    		return null;
    	}

    	StringBuilder b = new StringBuilder(HTML_B);
    	b.append(B_B);
    	b.append(key);
    	b.append(B_E);
    	if (val != null) {
    		b.append(" ");
    		b.append(val);
    	}
    	b.append(HTML_E);
    	
    	return b.toString();
    }
}
