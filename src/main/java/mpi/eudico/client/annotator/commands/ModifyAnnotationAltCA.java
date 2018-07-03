package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An alternative for the Alt-M CommandAction; on (some?) MacOS 10.4 
 * systems Alt-M (sometimes) doesn't create the annotation edit box but invokes
 * some kind of input method window. This is an alternative.
 * Also the Alt-<single-char> combinations interfere with the mnemonics access to 
 * the menu's.
 *  
 * @author Han Sloetjes, MPI
 */
public class ModifyAnnotationAltCA extends ModifyAnnotationCA {

    /**
     * Constructor.
     * 
     * @param viewerManager the viewer manager
     */
    public ModifyAnnotationAltCA(ViewerManager2 viewerManager) {
        super(viewerManager);
        
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_M, 
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                        ActionEvent.ALT_MASK));
    }

}
