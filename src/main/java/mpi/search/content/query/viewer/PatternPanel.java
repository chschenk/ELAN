package mpi.search.content.query.viewer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreeNode;

import mpi.eudico.util.CVEntry;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;


/**
 * $Id: PatternPanel.java 13088 2008-08-20 15:45:39Z hasloe $ $Author:
 * klasal $
 */
@SuppressWarnings("serial")
public class PatternPanel extends JPanel implements ItemListener {
    /** Holds cardLayout */
    protected final CardLayout inputLayout = new CardLayout();

    /** Holds value of quantifier input component */
    protected final JComboBox quantifierComboBox = new JComboBox(AnchorConstraint.QUANTIFIERS);

    /** Holds container for pattern input component (TextField or ComboBox) */
    protected final JPanel inputPanel = new JPanel(inputLayout);

    /** Holds value of regular expression or string */
    protected JTextField textField = new JTextField(9);
    protected final CorpusType type;

    /* 
     * if LightwightPopup is enabled, one gets (at least for linux/java 1.5)
     * weird behavior of the tooltips for closed vocabularies:
     * the tooltips are BEHIND the jcomboBox-menu and the items in the menu are not 
     * always changed back from selected to unselected
     * This has most probably to do with the fact, that the ConstraintPanel itself is already
     * part of the CellRenderer of the constraint-JTable (comboBoxes elsewhere work fine).
     * Setting this property at the specific JComboBox has no effect. 
     */
    static{
    	//has unwanted side effects
        //ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }
    
    /**
     * Creates a new PatternPanel object.
     *
     * @param type DOCUMENT ME!
     * @param tierComboBox DOCUMENT ME!
     * @param node DOCUMENT ME!
     * @param startAction DOCUMENT ME!
     */
    public PatternPanel(CorpusType type, JComboBox tierComboBox, final JCheckBox regExCheckBox, TreeNode node,
        final Action startAction) {
        this.type = type;
        textField.setFont(getFont().deriveFont(Font.BOLD, 16f));
        inputPanel.add(textField, "");

        for (int j = 0; j < type.getTierNames().length; j++) {
            String tierName = type.getTierNames()[j];
            List<CVEntry> closedVoc = type.getClosedVoc(tierName);

            if (closedVoc != null) {
                final JComboBox comboBox = new JComboBox(closedVoc.toArray()) {
                        @Override
						public Dimension getPreferredSize() {
                            return new Dimension(textField.getPreferredSize().width,
                                super.getPreferredSize().height);
                        }
                    };

                comboBox.setMaximumRowCount(15); // larger than swing default
                //renderer doesn't work properly with lightweight tooltips
                comboBox.setEditable(type.isClosedVocEditable(closedVoc));
                
                comboBox.addItemListener(new ItemListener(){
                	@Override
					public void itemStateChanged(ItemEvent e){
                		if(e.getStateChange() == ItemEvent.SELECTED)
                			//regular expression if edited and different to all entries
                			regExCheckBox.setSelected(comboBox.getSelectedItem() instanceof String 
                					&& getMatchingIndex(comboBox, (String) comboBox.getSelectedItem()) == -1);
                	}
                });

                inputPanel.add(comboBox, tierName);
            }
        }

        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JLabel label;

        if ((node.getParent() == null) || !type.allowsQuantifierNO()) {
            label = new JLabel(SearchLocale.getString(AnchorConstraint.ANY));
            label.setFont(getFont().deriveFont(Font.PLAIN));
            add(label);
        } else {
            add(quantifierComboBox);
        }

        label = new JLabel(" " +
                SearchLocale.getString("Search.Annotation_SG") + " " +
                SearchLocale.getString("Search.Constraint.OnTier") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(tierComboBox);

        if (node.getParent() == null) {
            label = new JLabel(" " +
                    SearchLocale.getString("Search.Constraint.That"));
            label.setFont(getFont().deriveFont(Font.PLAIN));
            add(label);
        }

        label = new JLabel(" " +
                SearchLocale.getString("Search.Constraint.Matches") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(inputPanel);

        inputLayout.show(inputPanel, "");
        quantifierComboBox.setRenderer(new LocalizeListCellRenderer());
        quantifierComboBox.setSelectedItem(AnchorConstraint.ANY);
        tierComboBox.addItemListener(this);
        tierComboBox.setRenderer(new TierListCellRenderer(type));
        textField.requestFocus();

        if (startAction != null) {
            KeyListener l = new KeyAdapter() {
                    @Override
					public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            startAction.actionPerformed(new ActionEvent(
                                    e.getSource(), e.getID(),
                                    KeyEvent.getKeyText(e.getKeyCode())));
                        }
                    }
                };

            textField.addKeyListener(l);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getDefaultInputComponent() {
        return textField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pattern DOCUMENT ME!
     */
    public void setPattern(String pattern) {
        Component c = getVisibleInputComponent();

        if (c instanceof JTextField) {
            ((JTextField) c).setText(pattern);
        } else {
        	JComboBox comboBox = (JComboBox) c;
        	int matchingIndex = getMatchingIndex(comboBox, pattern);
        	if(matchingIndex >=0 ){
        		comboBox.setSelectedIndex(matchingIndex);
        	}
        	else{ 
        		comboBox.setSelectedItem(pattern);
        	}
        	
        }

        c.requestFocus();
    }

    /*
     * test if pattern is contained in closed vocabulary 
     * returns the index of that item whose .toString() method equals pattern; -1 if no item matches
     */
    private int getMatchingIndex(JComboBox comboBox, String pattern){
    	int selectedIndex = -1;
    	for(int i=0; i<comboBox.getItemCount(); i++){
    		if(comboBox.getItemAt(i).toString().equals(pattern)){
    			comboBox.setSelectedIndex(i);
    			selectedIndex = i;
    			break;
    		}
    	}
    	return selectedIndex;
    }
    
    /**
     * 
     *
     * @return string from input field (text or selected item from closed vocabulary combo box)
     */
    public String getPattern() {
        Component c = getVisibleInputComponent();
        return (c instanceof JTextField) ? ((JTextField) c).getText()
                                         : ((JComboBox) c).getSelectedItem()
                                            .toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param quantifier DOCUMENT ME!
     */
    public void setQuantifier(String quantifier) {
        quantifierComboBox.setSelectedItem(quantifier);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getQuantifier() {
        return (String) quantifierComboBox.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getVisibleInputComponent() {
        Component[] comps = inputPanel.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i].isVisible()) {
                return comps[i];
            }
        }

        return textField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (type.isClosedVoc((String) e.getItem())) {
                inputLayout.show(inputPanel, (String) e.getItem());
            } else {
                inputLayout.show(inputPanel, "");
            }

            inputPanel.setLocale(type.getDefaultLocale((String) e.getItem()));
            validate();
            repaint();
        }
    }
}
