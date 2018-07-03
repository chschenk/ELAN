package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.TimeEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME! $Id: ActiveSelectionBoundaryCA.java,v 1.1.1.1 2004/03/25
 * 16:23:15 wouthuij Exp $
 *
 * @author $Author$
 * @version $Revision$
 */
public class ActiveSelectionBoundaryCA extends CommandAction
    implements ControllerListener {
    private Icon leftIcon;
    private Icon rightIcon;
    private boolean leftActive = false;

    /**
     * Creates a new ActiveSelectionBoundaryCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public ActiveSelectionBoundaryCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SELECTION_BOUNDARY);

        // ask ViewerManager to connect to player
        vm.connectListener(this);

        leftIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/CrosshairInSelectionLeft.gif"));
        rightIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/CrosshairInSelectionRight.gif"));
        putValue(SMALL_ICON, leftIcon);

        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SELECTION_BOUNDARY);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getMediaPlayerController();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[3];
        args[0] = vm.getMasterMediaPlayer();
        args[1] = vm.getSelection();
        args[2] = this;

        return args;
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            if (vm.getMediaPlayerController().isBeginBoundaryActive() &&
                    !leftActive) {
                setLeftIcon(false);
                leftActive = true;
            }

            if (!vm.getMediaPlayerController().isBeginBoundaryActive() &&
                    leftActive) {
                setLeftIcon(true);
                leftActive = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param left DOCUMENT ME!
     */
    public void setLeftIcon(boolean left) {
        if (left) {
            putValue(SMALL_ICON, leftIcon);
        } else {
            putValue(SMALL_ICON, rightIcon);
        }
    }
}
