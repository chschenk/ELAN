package mpi.eudico.client.annotator.gui;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;


/**
 * A frame with registered actions to close (dispose) the frame with the Escape
 * or  Ctrl-W (Command-W) key events.
 */
public class ClosableFrame extends JFrame {
    /**
     * Constructor.
     *
     * @throws HeadlessException
     */
    public ClosableFrame() throws HeadlessException {
        super();
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param gc
     */
    public ClosableFrame(GraphicsConfiguration gc) {
        super(gc);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param title
     *
     * @throws HeadlessException
     */
    public ClosableFrame(String title) throws HeadlessException {
        super(title);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param title the title
     * @param gc
     */
    public ClosableFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        addCloseActions();
    }

    /**
     * Add the Escape and Ctrl-W close actions.
     */
    protected void addCloseActions() {
        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction wAction = new CtrlWCloseAction(this);

        InputMap inputMap = getRootPane()
                                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            String esc = "esc";
            inputMap.put((KeyStroke) escAction.getValue(Action.ACCELERATOR_KEY),
                esc);
            actionMap.put(esc, escAction);

            String wcl = "cw";
            inputMap.put((KeyStroke) wAction.getValue(Action.ACCELERATOR_KEY),
                wcl);
            actionMap.put(wcl, wAction);
        }
    }
}
