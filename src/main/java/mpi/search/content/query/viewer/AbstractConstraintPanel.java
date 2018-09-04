/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.content.query.viewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultTreeModel;

import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.DependentConstraint;
import mpi.search.content.query.model.RestrictedAnchorConstraint;


/**
 * The ConstraintPanel is the GUI for editing one single Constraint
 * ConstraintPanels are meant to be included in the SearchConfigPanel
 *
 * $Id: AbstractConstraintPanel.java 13085 2008-08-20 15:43:18Z hasloe $
 * $Author$
 */
@SuppressWarnings("serial")
public abstract class AbstractConstraintPanel extends JPanel
    implements ItemListener, /*ActionListener,*/ PopupMenuListener {

	protected static int tierComboBoxWidth = 0;
    protected final Action startAction;
    protected final CardLayout framedPanelLayout = new CardLayout();
    protected final Constraint constraint;

    /** Holds value of property DOCUMENT ME! */
    protected final CorpusType type;
    protected final DefaultTreeModel treeModel;

    /** Holds value of property DOCUMENT ME! */
    protected final JCheckBox caseCheckBox = new JCheckBox(SearchLocale.getString(
                "Search.Constraint.CaseSensitive"), false);

    /** Holds value of property DOCUMENT ME! */
    protected final JCheckBox regExCheckBox = new JCheckBox(SearchLocale.getString(
                "Search.Constraint.RegularExpression"), false);

    /** Holds value of property DOCUMENT ME! */
    protected AttributeConstraintPanel attributePanel;
    protected final JPanel framedPanel = new JPanel(framedPanelLayout);

    /** Holds value of property DOCUMENT ME! */
    protected final JPanel optionPanel = new JPanel(new BorderLayout());

    /** Holds value of property DOCUMENT ME! */
    protected JComboBox tierComboBox;
    /**
     * If not null, selectedTiers holds the selection of the custom tier set.
     * It should be not-null IFF tierComboBox has Constraint.CUSTOM_TIER_SET selected.
     * It may be empty, but since queries don't like empty tier sets,
     * in some places ALL_TIERS is substituted instead.
     * <p>
     * If null, some other tier (or Constraint.ALL_TIERS) is selected in tierComboBox.
     */
    protected List<String> selectedTiers;
    protected JPanel titleComponent = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 5, 1));
    protected PatternPanel patternPanel;

    /** Holds value of property DOCUMENT ME! */
    protected RelationPanel relationPanel;

    /** Holds value of property DOCUMENT ME! */
    protected final Border blueBorder = new CompoundBorder(new EmptyBorder(1, 0,
                1, 0),
            new CompoundBorder(new LineBorder(Color.BLUE),
                new EmptyBorder(0, 3, 0, 0)));

    /**
     * Creates a new AbstractConstraintPanel object.
     *
     * @param constraint DOCUMENT ME!
     * @param treeModel DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param startAction DOCUMENT ME!
     */
    public AbstractConstraintPanel(Constraint constraint,
        DefaultTreeModel treeModel, CorpusType type, Action startAction) {
        this.type = type;
        this.constraint = constraint;
        this.treeModel = treeModel;
        this.startAction = startAction;
    }

    /**
     * DOCUMENT ME!
     *
     * @param c DOCUMENT ME!
     */
    public void setConstraint(Constraint c) {
        setRegEx(c.isRegEx());
        setCaseSensitive(c.isCaseSensitive());
        setPattern(c.getPattern());
        relationPanel.setLowerBoundary(c.getLowerBoundary());
        relationPanel.setUpperBoundary(c.getUpperBoundary());
        relationPanel.setUnit(c.getUnit());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isRegEx() {
        return regExCheckBox.isSelected();
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public String getTierName() {
        return getTierNames()[0];
    }

    //implementation of ItemListener
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == tierComboBox) {
            	if (e.getItem() != Constraint.CUSTOM_TIER_SET) {
            		selectedTiers = null;
            	}
                if (type.isClosedVoc((String) e.getItem())) {
                    regExCheckBox.setEnabled(false);
                    setRegEx(false);
                    setCaseSensitive(false);

                    if (type.getClosedVoc((String) e.getItem()).isEmpty()) {
                        startAction.setEnabled(false);
                    } else {
                        startAction.setEnabled(true);
                    }
                } else {
                    startAction.setEnabled(true);
                    regExCheckBox.setEnabled(true);
                }

                if (type.strictCaseSensitive((String) e.getItem())) {
                    setCaseSensitive(true);
                }

                boolean caseSensitiveFixed = type.isClosedVoc((String) e.getItem()) ||
                    type.strictCaseSensitive((String) e.getItem());

                if (!caseSensitiveFixed) {
                    if (!caseCheckBox.isEnabled()) {
                        setCaseSensitive(false);
                    }

                    // change from fixed to variable -> set default
                }

                caseCheckBox.setEnabled(!caseSensitiveFixed);

                if (attributePanel != null) {
                    attributePanel.setTier((String) e.getItem());
                }
            }
        }
    }

    /**
     * Action listener for the one item in the tier combo box that should always create
     * a dialog for selecting multiple tiers.
     * This doesn't work well on Windows in case the same item is selected again (
     * like when wanting to change the custom set of tiers)
     */
    /*
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tierComboBox) {
        	if (tierComboBox.getSelectedItem() == Constraint.CUSTOM_TIER_SET) {
        		selectCustomTierSet();
        	}
		}
	}
	*/

	/**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        String s = super.toString();
        //System.out.println("string " + s);

        return s;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected HashMap<String, String> getAttributes() {
        HashMap<String, String> attributes = new HashMap<String, String>();

        if (attributePanel != null) {
            String[] attributeNames = type.getAttributeNames(getTierName());

            for (String attributeName : attributeNames) {
                attributes.put(attributeName,
                    attributePanel.getAttributeValue(attributeName));
            }
        }

        return attributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sensitiv DOCUMENT ME!
     */
    protected void setCaseSensitive(boolean sensitiv) {
        caseCheckBox.setSelected(sensitiv);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean isCaseSensitive() {
        return caseCheckBox.isSelected();
    }

    protected Constraint getConstraint() {
        updateNode();

        return constraint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pattern DOCUMENT ME!
     */
    protected void setPattern(String pattern) {
        patternPanel.setPattern(pattern);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected String getPattern() {
        return patternPanel.getPattern();
    }

    /**
     * DOCUMENT ME!
     *
     * @param regEx DOCUMENT ME!
     */
    protected void setRegEx(boolean regEx) {
        regExCheckBox.setSelected(regEx);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     */
    protected void setTierName(String tierName) {
        tierComboBox.setSelectedItem(tierName);
    }

    protected String[] getTierNames() {
    	if (selectedTiers != null) {
    		if (selectedTiers.isEmpty()) {
                return getAllTiers();
    		} else {
    			return selectedTiers.toArray(new String[selectedTiers.size()]);
    		}
    	}
    	Object[] selectedObjects = tierComboBox.getSelectedObjects();

        if ((selectedObjects == null) || (selectedObjects.length == 0)) {
            return getAllTiers();
        }

        /*
         * The first time around, selectedTiers has not had a chance yet to be set,
         * so special-case this here. Letting the place-holder escape must be prevented.
         * Since queries don't like empty tier sets, return a full one.
         */
        if (selectedObjects.length == 1 && selectedObjects[0] == Constraint.CUSTOM_TIER_SET) {
            return getAllTiers();
        }

        String[] tierNames = new String[selectedObjects.length];

        for (int i = 0; i < selectedObjects.length; i++) {
            tierNames[i] = (String) selectedObjects[i];
        }

        return tierNames;
    }

	/**
	 * @return an array with the ALL_TIERS placeholder.
	 */
	private String[] getAllTiers() {
		return new String[] { Constraint.ALL_TIERS };
	}

    /**
     * DOCUMENT ME!
     */
    protected void makeLayout() {
        //RegExPanel
        patternPanel = new PatternPanel(type, tierComboBox, regExCheckBox, constraint,
                startAction);

        //RelationPanel
        relationPanel = new RelationPanel(type, constraint);

        //OptionPanel
        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
        regExCheckBox.setFont(getFont().deriveFont(Font.PLAIN, 9f));
        caseCheckBox.setFont(getFont().deriveFont(Font.PLAIN, 9f));
        checkBoxPanel.add(regExCheckBox);
        checkBoxPanel.add(caseCheckBox);
        optionPanel.add(checkBoxPanel, BorderLayout.WEST);

        //InputPanel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        inputPanel.add(patternPanel);
        inputPanel.add(relationPanel);

        //AttributePanel
        if (type.hasAttributes()) {
            attributePanel = new AttributeConstraintPanel(type);
            optionPanel.add(attributePanel, BorderLayout.CENTER);
            attributePanel.setTier(getTierName());
        }

        //FramedPanel
        JPanel specificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                    0, 1));
        specificationPanel.add(inputPanel);
        specificationPanel.add(optionPanel);
        framedPanel.add(specificationPanel, "");
        framedPanel.setBorder(blueBorder);
        framedPanelLayout.show(framedPanel, "");

        //this
        setLayout(new BorderLayout());
        add(titleComponent, BorderLayout.NORTH);
        add(framedPanel, BorderLayout.CENTER);

        tierComboBox.addItemListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        Action addConstraintAction = new AbstractAction(SearchLocale.getString(
                    "Search.Query.Add")) {
                @Override
				public void actionPerformed(ActionEvent e) {
                    addConstraint();
                }
            };

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK);
        addConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

        JButton addButton = new JButton(addConstraintAction);
        addButton.setFont(getFont().deriveFont(11f));
        buttonPanel.add(addButton);

        if ((constraint.getParent() != null) &&
                !(constraint.getParent() instanceof RestrictedAnchorConstraint)) {
            Action deleteConstraintAction = new AbstractAction(SearchLocale.getString(
                        "Search.Query.Delete")) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        deleteConstraint();
                    }
                };

            ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
            deleteConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

            JButton deleteButton = new JButton(deleteConstraintAction);
            deleteButton.setFont(getFont().deriveFont(11f));
            buttonPanel.add(deleteButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        try {
            Class popupMenu = type.getInputMethodClass();
            popupMenu.getConstructor(new Class[] {
                    Component.class, AbstractConstraintPanel.class
                })
                     .newInstance(new Object[] {
                    patternPanel.getDefaultInputComponent(),
                    AbstractConstraintPanel.this
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param comboBox DOCUMENT ME!
     * @param units DOCUMENT ME!
     */
    protected static void updateComboBox(JComboBox comboBox, String[] units) {
        //don't refill if equal strings
        if (units.length == comboBox.getItemCount()) {
            boolean equal = true;

            for (int i = 0; i < units.length; i++) {
                if (!units[i].equals(comboBox.getItemAt(i))) {
                    equal = false;
                    break;
                }
            }

            if (equal) {
                return;
            }
        }

        Object oldItem = comboBox.getSelectedItem();

        comboBox.removeAllItems();

        for (String unit : units) {
            comboBox.addItem(unit);
        }

        //select old item
        comboBox.setSelectedItem(oldItem);

        //if oldItem not in Box, select smallest possible linguistic unit
        if (comboBox.getSelectedItem() == null) {
            comboBox.setSelectedIndex(0);
        }
    }

    protected void updateNode() {
        try {
            constraint.setTierNames(getTierNames());
            constraint.setPattern(getPattern());
            constraint.setLowerBoundary(relationPanel.getLowerBoundary());
            constraint.setUpperBoundary(relationPanel.getUpperBoundary());
            constraint.setUnit(relationPanel.getUnit());
            constraint.setRegEx(isRegEx());
            constraint.setCaseSensitive(isCaseSensitive());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void addConstraint() {
        treeModel.insertNodeInto(new DependentConstraint(getTierNames()),
            constraint, constraint.getChildCount());
    }

    protected void deleteConstraint() {
        treeModel.removeNodeFromParent(constraint);
    }
    
    protected void selectCustomTierSet() {
    	// stub
    }
    

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		//stub or store time of cancel
	}

	/**
	 * Creates a tier selection dialog after the popup is gone and if the custom tier set
	 * option is (still) selected.
	 * Has to be performed in a separate thread otherwise the popup will be in front of the dialog.
	 */
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		if (e.getSource() == tierComboBox) {
			if (tierComboBox.getSelectedItem() == Constraint.CUSTOM_TIER_SET) {
				SwingUtilities.invokeLater(new Thread(new Runnable() {
					@Override
					public void run() {
						selectCustomTierSet();
					}
				}));
			}
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		//stub
	}

}
