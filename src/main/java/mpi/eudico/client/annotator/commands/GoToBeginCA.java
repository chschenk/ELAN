package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME! $Id: GoToBeginCA.java,v 1.1.1.1 2004/03/25 16:23:15 wouthuij
 * Exp $
 *
 * @author $Author$
 * @version $Revision$
 */
public class GoToBeginCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new GoToBeginCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public GoToBeginCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.GO_TO_BEGIN);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToBeginButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.GO_TO_BEGIN);
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
