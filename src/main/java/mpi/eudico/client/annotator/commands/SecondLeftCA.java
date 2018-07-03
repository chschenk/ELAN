package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME!
 * $Id: SecondLeftCA.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SecondLeftCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondLeftCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public SecondLeftCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_LEFT);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/1SecLeftButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SECOND_LEFT);
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
