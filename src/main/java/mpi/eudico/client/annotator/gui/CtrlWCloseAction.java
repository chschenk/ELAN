package mpi.eudico.client.annotator.gui;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * An action to close a window by pressing Ctrl+W or Command+W.
 */
public class CtrlWCloseAction extends AbstractAction {
    private Window window;

    /**
     * Constructor.
     *
     * @param window the window to close
     */
    public CtrlWCloseAction(Window window) {
        super();
        this.window = window;
        putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (window != null) {
            window.dispose();
        }
    }
}
