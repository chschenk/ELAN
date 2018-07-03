package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An alternative for the Alt-D CommandAction; on (some?) MacOS 10.4 
 * systems Alt-D (sometimes) doesn't delete the annotation but invokes
 * some kind of input method window. This is an alternative.
 * Also the Alt-<single-char> combinations interfere with the mnemonics access to 
 * the menu's.
 *  
 * @author Han Sloetjes, MPI
 */
public class DeleteAnnotationAltCA extends DeleteAnnotationCA {

    /**
     * Constructor.
     * 
     * @param viewerManager the viewer manager
     */
    public DeleteAnnotationAltCA(ViewerManager2 viewerManager) {
        super(viewerManager);
        
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, 
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                        ActionEvent.ALT_MASK));
    }

}
