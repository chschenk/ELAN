package mpi.eudico.client.annotator.search.query.viewer;

import mpi.search.content.model.CorpusType;

import mpi.search.content.query.viewer.PatternPanel;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.tree.TreeNode;


/**
 * ELAN subclass with additional font support.
 * 
 * @author HS
 * @version Aug 2008
  */
public class ElanPatternPanel extends PatternPanel {
    /**
     * Creates a new ElanPatternPanel instance and applies a default
     * ELAN font to relevant ui elements.
     *
     * @param type 
     * @param tierComboBox 
     * @param regExCheckBox 
     * @param node 
     * @param startAction 
     * @param prefFont 
     */
    public ElanPatternPanel(CorpusType type, JComboBox tierComboBox,
        JCheckBox regExCheckBox, TreeNode node, Action startAction,
        Font prefFont) {
        super(type, tierComboBox, regExCheckBox, node, startAction);

        if (prefFont != null) {
            textField.setFont(prefFont.deriveFont(Font.BOLD));
            /* unclear why a different than the ui font should be used here
            Component[] comps = inputPanel.getComponents();

            for (int i = 0; i < comps.length; i++) {
                if (comps[i] instanceof JComboBox) {
                    comps[i].setFont(prefFont.deriveFont(
                            comps[i].getFont().getStyle(),
                            comps[i].getFont().getSize()));
                }
            }

            tierComboBox.setFont(prefFont.deriveFont(tierComboBox.getFont()
                                                                 .getStyle(),
                    tierComboBox.getFont().getSize()));
            */
        }
    }

	@Override
	public void grabFocus() {
		textField.grabFocus();
	}
    
    
}
