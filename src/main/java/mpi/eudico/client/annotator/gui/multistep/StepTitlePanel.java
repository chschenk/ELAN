package mpi.eudico.client.annotator.gui.multistep;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * StepTitlePanel. The top panel of the multistep pane, displaying the 
 * title (short description) of the active or current step.
 *
 * @author Han Sloetjes
 */
public class StepTitlePanel extends JPanel {
    private JLabel titleLabel;

    /**
     * Creates a new StepTitlePanel instance.
     */
    public StepTitlePanel() {
        super();
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(4, 10, 4, 4);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        titleLabel = new JLabel();
        add(titleLabel, gridBagConstraints);
    }

    /**
     * Sets the Label's text, the title of the step.
     *
     * @param text the title
     */
    public void setTitleText(String text) {
        titleLabel.setText("<html>" + text + "</html>");
    }
}
