package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.NgramStatisticsDialog; 

import java.awt.event.ActionEvent;


/**
 * Menu action that creates a Statistics Dialog for multiple files.
 *
 * @author Jeffrey Lemein
 * @version 1.0
 */
public class NgramStatisticsMA extends FrameMenuAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3599860637746077322L;

	/**
     * Creates a new StatisticsMultipleFilesMA instance
     *
     * @param name the name of the action
     * @param frame the parent frame
     */
    public NgramStatisticsMA(String name, ElanFrame2 frame) {
    	super(name, frame);
    }

    /**
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	new NgramStatisticsDialog(frame).setVisible(true);
    }
}
