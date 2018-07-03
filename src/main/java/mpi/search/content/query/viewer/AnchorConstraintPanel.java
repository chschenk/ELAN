package mpi.search.content.query.viewer;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

import mpi.eudico.client.annotator.search.model.ElanType;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;

/**
 * $Id: AnchorConstraintPanel.java 7355 2006-11-28 11:36:23Z klasal $
 *
 * $Author$
 */
@SuppressWarnings("serial")
public class AnchorConstraintPanel extends AbstractConstraintPanel {
	final private JLabel lockedConstraintLabel = new JLabel();
	private String lockedTierName = null;
	private String lockedPattern = null;

	public AnchorConstraintPanel(AnchorConstraint constraint, DefaultTreeModel treeModel, CorpusType type, Action startAction) {
		super(constraint, treeModel, type, startAction);
		// 'head line' of constraint
		titleComponent.add(
			new JLabel(
				SearchLocale.getString("Search.Query.Find").toUpperCase()));
		titleComponent.setBorder(new EmptyBorder(0, 0, 5, 0));

		tierComboBox = new JComboBox(type.getTierNames()) {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				tierComboBoxWidth = this.getPreferredSize().width;
			}
		};

		if (type.allowsSearchOverMultipleTiers() && type.getTierNames().length > 1){
			tierComboBox.insertItemAt(Constraint.ALL_TIERS, 0);
			if (type instanceof ElanType) {
				tierComboBox.insertItemAt(Constraint.CUSTOM_TIER_SET, 1);
			}
		}
		
		lockedConstraintLabel.setHorizontalAlignment(JLabel.CENTER);
		framedPanel.add(lockedConstraintLabel, "locked");
		
		makeLayout();
		setConstraint(constraint);
		tierComboBox.addPopupMenuListener(this);
	}

	@Override
	protected void setTierName(String tierName) {
		for (int i = 0; i < type.getIndexTierNames().length; i++) {
			if (type.getIndexTierNames()[i].equals(tierName.toUpperCase())) {
				lockedTierName = tierName.toUpperCase();
				framedPanelLayout.show(framedPanel, "locked");
				return;
			}
		}
		//if lockedField was set, reset.
		if (lockedTierName != null) {
			lockedTierName = null;
			lockedPattern = null;
			framedPanelLayout.show(framedPanel, "");
		}
		super.setTierName(tierName);
	}

	protected void setTierNames(List<String> tierNames) {
		if (tierNames.size() > 0) {
			if (tierNames.size() == 1) {
				setTierName(tierNames.get(0));
			} else {
				tierComboBox.setSelectedItem(Constraint.CUSTOM_TIER_SET);
				selectedTiers = new ArrayList<String>(tierNames);
			}
		} else {
			tierComboBox.setSelectedIndex(0);
		}
	
	}
	
	protected void setTierNames(String[] tierNames) {
		setTierNames(Arrays.asList(tierNames));
	}
	
	@Override
	public String getTierName(){
	    return lockedTierName == null ? super.getTierName() : lockedTierName;
	}

	@Override
	protected void setPattern(String pattern) {
		if (lockedTierName != null) {
			lockedPattern = pattern;
			lockedConstraintLabel.setText(type.getUnabbreviatedTierName(lockedTierName) + " " + lockedPattern);
		} else {
			super.setPattern(pattern);
		}
	}

	@Override
	protected String getPattern(){
	    return lockedPattern == null ? super.getPattern() : lockedPattern;
	}
	
	public void setConstraint(AnchorConstraint c){
		setTierNames(c.getTierNames());
		super.setConstraint(c);
	}
}
