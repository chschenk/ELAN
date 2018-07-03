package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * A panel containing buttons for interaction with the Selection, the 
 * selected time interval.
 * @ version September 2015 added a mode where there can be two separate buttons for
 * moving the player to either the start of the selection or the end (instead of one toggle button)
 * 
 * 
 * $Id: SelectionButtonsPanel.java 44336 2015-08-27 14:53:55Z hasloe $
 */
@SuppressWarnings("serial")
public class SelectionButtonsPanel extends JComponent {
    private JButton butPlaySelection;
    private JButton butClearSelection;
    private JButton butToggleCrosshairInSelection;
    private JButton butCrosshairBegin;
    private JButton butCrosshairEnd;
    private boolean separateLeftRightButtons = false;

    /**
     * Creates a new SelectionButtonsPanel instance, with a selection toggle button
     *
     * @param buttonSize one dimension for all buttons
     * @param theVM the viewer manager
     */
    public SelectionButtonsPanel(Dimension buttonSize, ViewerManager2 theVM) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        butPlaySelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PLAY_SELECTION));
        butPlaySelection.setPreferredSize(buttonSize);
        add(butPlaySelection);

        butClearSelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.CLEAR_SELECTION));
        butClearSelection.setPreferredSize(buttonSize);
        add(butClearSelection);

        butToggleCrosshairInSelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(),
                    ELANCommandFactory.SELECTION_BOUNDARY));
        butToggleCrosshairInSelection.setPreferredSize(buttonSize);
        add(butToggleCrosshairInSelection);
        
        butCrosshairBegin = new JButton(ELANCommandFactory.getCommandAction(
        		theVM.getTranscription(), ELANCommandFactory.SELECTION_BEGIN));
        butCrosshairBegin.setPreferredSize(buttonSize);
        
        butCrosshairEnd = new JButton(ELANCommandFactory.getCommandAction(
        		theVM.getTranscription(), ELANCommandFactory.SELECTION_END));
        butCrosshairEnd.setPreferredSize(buttonSize);
    }

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		butPlaySelection.getAction().setEnabled(enabled);
		butClearSelection.getAction().setEnabled(enabled);
		butToggleCrosshairInSelection.getAction().setEnabled(enabled);
	}
    
	public void setSeparateLeftRightMode(boolean separateMode) {
		if (separateLeftRightButtons != separateMode) {
			if (separateLeftRightButtons) {
				// remove the two begin/end buttons, add the one toggle button
				remove(butCrosshairBegin);
				remove(butCrosshairEnd);
				add(butToggleCrosshairInSelection);
			} else {
				// remove the toggle button, add the jump to begin and jump to end buttons
				remove(butToggleCrosshairInSelection);
				add(butCrosshairBegin);
				add(butCrosshairEnd);
			}
			separateLeftRightButtons = separateMode; 
		}
	}
    
}
