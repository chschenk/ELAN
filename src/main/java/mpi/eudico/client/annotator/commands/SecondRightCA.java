package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME!
 * $Id: SecondRightCA.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SecondRightCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondRightCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public SecondRightCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_RIGHT);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/1SecRightButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SECOND_RIGHT);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
