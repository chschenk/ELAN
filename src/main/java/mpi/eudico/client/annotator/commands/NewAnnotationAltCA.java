package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An alternative for the Alt-N CommandAction; on (some?) MacOS 10.4 
 * systems Alt-N (sometimes) doesn't create a new annotation but invokes
 * some kind of input method window. Temporarely alternative?
 *  
 * @author Han Sloetjes, MPI
 *
 */
public class NewAnnotationAltCA extends NewAnnotationCA {
	
	/**
	 * Constructor
	 * @param viewerManager the viewer manager
	 */
	public NewAnnotationAltCA(ViewerManager2 viewerManager) {
		super(viewerManager);
		
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, 
                		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                		ActionEvent.ALT_MASK));
	}

}
