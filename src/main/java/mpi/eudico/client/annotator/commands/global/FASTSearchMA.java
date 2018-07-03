package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.search.viewer.FASTSearchFrame;

/**
 * A menu action to show the FAST frame.
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @since June 2013
 * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
 */
public class FASTSearchMA extends FrameMenuAction {
	private static final long serialVersionUID = -501944440841850928L;

    public FASTSearchMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	FASTSearchFrame searchFrame = new FASTSearchFrame(frame);
    	searchFrame.setLocationRelativeTo(frame);
    	searchFrame.setVisible(true);
    }
}
