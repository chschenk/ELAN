package mpi.search.content.query.viewer;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import mpi.search.content.query.model.AnchorConstraint;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.Constraint;


/**
 * Created on Jul 14, 2004
 * $Id: RelationPanel.java 13090 2008-08-20 15:46:55Z hasloe $
 * $Author$
 * $Version$
 */
public class RelationPanel extends JPanel {
    protected AbstractDistancePanel structuralDistancePanel;
    protected AbstractDistancePanel temporalDistancePanel;

    /** Holds value of property DOCUMENT ME */
    protected final JComboBox quantifierComboBox = new JComboBox(Constraint.QUANTIFIERS);
    private final CardLayout distanceInputLayout = new CardLayout();
    private final JPanel distancePanelPlaceHolder = new JPanel(distanceInputLayout);

    public RelationPanel(CorpusType type, Constraint constraint) {
        setLayout(new FlowLayout(FlowLayout.LEFT,0,0));

        GridBagConstraints c = new GridBagConstraints();

        if (constraint instanceof AnchorConstraint) {
            temporalDistancePanel = new AnchorTemporalDistancePanel();
        } else {
            temporalDistancePanel = new DependentTemporalDistancePanel();
        }

        structuralDistancePanel = new StructuralDistancePanel();
        distancePanelPlaceHolder.add(structuralDistancePanel,
            Constraint.STRUCTURAL);
        distancePanelPlaceHolder.add(temporalDistancePanel, Constraint.TEMPORAL);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.NONE;

        add(distancePanelPlaceHolder);

        c.anchor = GridBagConstraints.WEST;

        if (constraint instanceof AnchorConstraint && !type.allowsTemporalConstraints()) {
            setVisible(false);
        }else{
        		setDistanceMode(constraint.getMode());
        }
    }

    /**
     *
     *
     * @param mode DOCUMENT ME!
     */
    public void setDistanceMode(String mode) {
        distanceInputLayout.show(distancePanelPlaceHolder, mode);
   }

    /**
     * DOCUMENT ME!
     *
     * @param boundary DOCUMENT ME!
     */
    public void setLowerBoundary(long boundary) {
        (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                           : structuralDistancePanel).setLowerBoundary(boundary);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getLowerBoundary() {
        return ((!isVisible() || temporalDistancePanel.isVisible())
        ? temporalDistancePanel : structuralDistancePanel).getLowerBoundary();
    }

    /**
     *
     *
     * @param unit DOCUMENT ME!
     */
    public void setUnit(String unit) {
        if (unit != null) {
            (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                               : structuralDistancePanel).setUnit(unit);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUnit() {
        return (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                                  : structuralDistancePanel).getUnit();
    }

    /**
     * Fills the unitComboBox with all SearchUnits which share the tier on this
     * constraint and the tier of the constraint it refers to
     *
     * @return DOCUMENT ME!
     */
    public JComboBox getUnitComboBox() {
        return ((StructuralDistancePanel) structuralDistancePanel).getUnitComboBox();
    }

    /**
     * DOCUMENT ME!
     *
     * @param boundary DOCUMENT ME!
     */
    public void setUpperBoundary(long boundary) {
        (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                           : structuralDistancePanel).setUpperBoundary(boundary);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getUpperBoundary() {
        return ((!isVisible() || temporalDistancePanel.isVisible())
        ? temporalDistancePanel : structuralDistancePanel).getUpperBoundary();
    }
}
