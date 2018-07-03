package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * DOCUMENT ME!
 * $Id: AnnotationNavigationPanel.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class AnnotationNavigationPanel extends JComponent {
    private JButton butGoToPreviousAnnotation;
    private JButton butGoToNextAnnotation;
    private JButton butGoToLowerAnnotation;
    private JButton butGoToUpperAnnotation;

    /**
     * Creates a new AnnotationNavigationPanel instance
     *
     * @param buttonSize DOCUMENT ME!
     * @param theVM DOCUMENT ME!
     */
    public AnnotationNavigationPanel(Dimension buttonSize, ViewerManager2 theVM) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        butGoToPreviousAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(),
                    ELANCommandFactory.PREVIOUS_ANNOTATION));
        butGoToPreviousAnnotation.setPreferredSize(buttonSize);
        add(butGoToPreviousAnnotation);

        butGoToNextAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.NEXT_ANNOTATION));
        butGoToNextAnnotation.setPreferredSize(buttonSize);
        add(butGoToNextAnnotation);

        butGoToLowerAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.ANNOTATION_DOWN));
        butGoToLowerAnnotation.setPreferredSize(buttonSize);
        add(butGoToLowerAnnotation);

        butGoToUpperAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.ANNOTATION_UP));
        butGoToUpperAnnotation.setPreferredSize(buttonSize);
        add(butGoToUpperAnnotation);
    }
}
