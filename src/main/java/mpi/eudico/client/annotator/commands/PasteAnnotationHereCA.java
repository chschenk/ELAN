package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CoammandAction to paste an annotation from the system clipboard to the active tier 
 * at the current media time.
 */
public class PasteAnnotationHereCA extends PasteAnnotationCA {
    
    /**
     * Constructor.
     * @param viewerManager the ViewerManager
     */
    public PasteAnnotationHereCA (ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PASTE_ANNOTATION_HERE);
        
        putValue(Action.NAME, ELANCommandFactory.PASTE_ANNOTATION_HERE);
        updateLocale();
    }

    /**
     * The name of the active tier and the current media time. 
     * Depends on the check in actionPerformed.
     * 
     * @return an Object array size = 2
     */
    @Override
	protected Object[] getArguments() {
        return new Object[]{vm.getMultiTierControlPanel().getActiveTier().getName(), 
                new Long(vm.getMasterMediaPlayer().getMediaTime())};
    }
    
    /**
     * Don't create a command if there is no active tier. Otherwise perform the test in 
     * the super class.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getMultiTierControlPanel().getActiveTier() == null) {
            return;
        }
        
        super.actionPerformed(event);
    }
}
