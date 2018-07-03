package mpi.eudico.client.annotator.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;


/**
 * A dialog with registered actions to close (dispose) the dialog with the
 * Escape or  Ctrl-W (Command-W) key events.
 */
public class ClosableDialog extends JDialog {
    /**
     * Constructor
     *
     * @throws HeadlessException
     */
    public ClosableDialog() throws HeadlessException {
        this((Frame) null, false);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Frame owner) throws HeadlessException {
        this(owner, null, false);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     * @param modal wether the dialog is modal or not
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Frame owner, boolean modal) throws HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     * @param title the dialog title
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Frame owner, String title) throws HeadlessException {
        this(owner, title, false);
    }

    /**
     * Constructor, calls super's constructor and adds close actions.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal wether the dialog is modal or not
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Frame owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal wether the dialog is modal or not
     * @param gc the graphics configuration
     */
    public ClosableDialog(Frame owner, String title, boolean modal,
        GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Dialog owner) throws HeadlessException {
        this(owner, false);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param modal wether the dialog is modal or not
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Dialog owner, boolean modal)
        throws HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Dialog owner, String title) throws HeadlessException {
        this(owner, title, false);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal wether the dialog is modal or not
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Dialog owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal wether the dialog is modal or not
     * @param gc the graphics configuration
     *
     * @throws HeadlessException
     */
    public ClosableDialog(Dialog owner, String title, boolean modal,
        GraphicsConfiguration gc) throws HeadlessException {
        super(owner, title, modal, gc);
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
