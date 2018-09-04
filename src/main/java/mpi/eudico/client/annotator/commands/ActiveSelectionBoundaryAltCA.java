package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Alternative for the Ctrl + / combination.
 * 
 * @author HS
 * @version 1.0
  */
public class ActiveSelectionBoundaryAltCA extends ActiveSelectionBoundaryCA {
    /**
     * Creates a new ActiveSelectionBoundaryAltCA instance
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionBoundaryAltCA(ViewerManager2 theVM) {
        super(theVM);

        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_K,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + 
                    ActionEvent.SHIFT_MASK));
    }
}
