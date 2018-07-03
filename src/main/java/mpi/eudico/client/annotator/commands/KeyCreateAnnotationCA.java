package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.awt.event.ActionEvent;


/**
 * A CommandAction to create an annotation by pressing a key combination
 * alternatively at the  begin time and the end time of the desired new
 * annotation.
 */
@SuppressWarnings("serial")
public class KeyCreateAnnotationCA extends CommandAction {
    private Tier receiver;
    private long firstTime = -1;
    private long secTime = -1;

    /**
     * Constructor.
     *
     * @param vm the viewer manager
     */
    public KeyCreateAnnotationCA(ViewerManager2 vm) {
        super(vm, ELANCommandFactory.KEY_CREATE_ANNOTATION);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = null;

        if (receiver != null) {        	
            Boolean boolPref = Preferences.getBool("CreateDependingAnnotations", null);                
            if (boolPref != null) {
            	 if (boolPref.booleanValue()){
            		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
                             ELANCommandFactory.NEW_ANNOTATION_REC);  
            		 
            	 } else {
            		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
                             ELANCommandFactory.NEW_ANNOTATION);                		 
            	 }
            } else {
            	command = ELANCommandFactory.createCommand(vm.getTranscription(),
                     ELANCommandFactory.NEW_ANNOTATION);                		 
            }
        }
    }

    /**
     * Returns the current interval, begin and end time and resets the internal
     * first  key stroke and second key stroke values.
     *
     * @return an Object array containing 2 Longs, begin and end time
     */
    @Override
	protected Object[] getArguments() {
    	Long b, e;
        if (firstTime < secTime) {
            b = new Long(firstTime);
            e = new Long(secTime);
        } else {
            e = new Long(firstTime);
            b = new Long(secTime);
        }
        //reset
        firstTime = -1;
        secTime = -1;

        return new Object[] { b, e };
    }

    /**
     * Returns the tier to which a new annotation should be added.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
	protected Object getReceiver() {
        return receiver;
    }

    /**
     * Checks whether it is the first or the second key stroke, if the active
     * tier is time alignable, if at the position of the interval an
     * annotation can be created etc.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        // check if this is the first or second key stroke
        if (firstTime == -1) {
            firstTime = vm.getMasterMediaPlayer().getMediaTime();
            Boolean boolPref = Preferences.getBool("Media.Autoplay.KeyCreateAnnotation", null);                
            if (boolPref != null && boolPref) {
            	vm.getMasterMediaPlayer().start();
            }           
        } else {
            long st = vm.getMasterMediaPlayer().getMediaTime();

            if (st == firstTime) {
                return; // do nothing
            }

            if ((vm.getMultiTierControlPanel() != null) &&
                    (vm.getMultiTierControlPanel().getActiveTier() != null)) {
                TierImpl ti = (TierImpl) vm.getMultiTierControlPanel()
                                           .getActiveTier();

                if (ti.isTimeAlignable()) {
                    if (!ti.hasParentTier()) {
                        secTime = st;
                        receiver = ti;
                        super.actionPerformed(event);

                        return;
                    } else {
                        if ((((TierImpl) ti.getParentTier()).getAnnotationAtTime(
                                    firstTime) != null) ||
                                (((TierImpl) ti.getParentTier()).getAnnotationAtTime(
                                    st) != null)) {
                            // the interval will be forced within the parent's elsewhere
                            secTime = st;
                            receiver = ti;
                            super.actionPerformed(event);

                            return;
                        }
                    }
                }
            }

            firstTime = st; // treat this time as the first time
        }

        //super.actionPerformed(event);
    }
}
