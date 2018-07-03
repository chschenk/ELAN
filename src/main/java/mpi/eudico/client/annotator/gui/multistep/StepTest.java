package mpi.eudico.client.annotator.gui.multistep;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JDialog;


/**
 * StepTest
 *
 * @author Han Sloetjes
 */
public class StepTest {
    /**
     * Creates a new StepTest instance
     */
    public StepTest() {
        MultiStepPane pane = new MultiStepPane();
        StepPane step = new StepPane(pane);
        step.setBackground(Color.cyan);
        step.setPreferredSize(new Dimension(400, 300));
        step.setMinimumSize(new Dimension(400, 300));
        pane.addStep(step);

        StepPane step2 = new StepPane(pane);
        step2.setBackground(Color.magenta);
        pane.addStep(step2);

        JDialog dialog = pane.createDialog(new JDialog(), "Step test", true);
        dialog.pack();
        dialog.setVisible(true);

        //pane.nextStep();
        //pane.revalidate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        new StepTest();
        System.exit(0);
    }
}
