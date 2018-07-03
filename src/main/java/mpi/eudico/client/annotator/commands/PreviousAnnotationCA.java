package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME!
 * $Id: PreviousAnnotationCA.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PreviousAnnotationCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new PreviousAnnotationCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public PreviousAnnotationCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.PREVIOUS_ANNOTATION);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToPreviousAnnotation.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationBefore(currentActiveAnnot);

            if (newActiveAnnot == null) {
                newActiveAnnot = currentActiveAnnot;
            }
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = ((TierImpl) activeTier).getAnnotationBefore(vm.getMasterMediaPlayer()
                                                                               .getMediaTime());
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }
}
