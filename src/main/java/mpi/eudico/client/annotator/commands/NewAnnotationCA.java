package mpi.eudico.client.annotator.commands;

import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * A CommandAction for the creation of new annotations on a tier.<br>
 * <b>Note: </b> this action was supposed to enable and disable itself
 * depending on selection, active annotation and active tier. Since there is
 * no listener mechanism for active tier, this action is no longer supposed to
 * be visible as a menu item and no longer checks whether it should be enabled
 * or disabled. It simply checks conditions every time it is called and
 * creates a command when appropriate. This Action is mainly intended to
 * provide a key shortcut for the creation of a new annotation.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class NewAnnotationCA extends CommandAction {
    private TierImpl receiver;
    private long beginTime;
    private long endTime;    

    /**
     * Creates a new NewAnnotationCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public NewAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.NEW_ANNOTATION);

        //setEnabled(false);
    }

    public NewAnnotationCA(ViewerManager2 viewerManager, String commandName) {
    	super(viewerManager, commandName);
	}

	/**
     * Before just creating a command check if it is possible to create a new
     * annotation and if so, on which tier. If receiver is <code>null</code>
     * no command is created (since the command should be undoable we don't
     * want to check in the command itself).
     */
    @Override
	protected void newCommand() {
        command = null;        

        if (checkState()) {
            //doublecheck
            if (receiver != null) {
            	Boolean val = Preferences.getBool("CreateDependingAnnotations", null);                
                if (val != null) {
                	 if (val.booleanValue()) {
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
    }

    /**
     * The receiver of this CommandAction is a TierImpl object on which the new
     * annotation should be created. This can be either the active tier or the
     * tier the active annotation is on.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return receiver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = new Long(beginTime);
        args[1] = new Long(endTime);

        return args;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     * This depends on the type of the annotation, the type of the Tier it
     * belongs to and on the current selection.
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */

    /*
       public void updateActiveAnnotation() {
           activeAnnotation = viewerManager.getActiveAnnotation().getAnnotation();
           checkState();
       }
     */

    /**
     * On a change in the Selection perform a check to determine whether this
     * action should be enabled or disabled.<br>
     * This depends on the current selection, the type of the active
     * annotation and on the type of the tier the annotation belongs to.
     *
     * @see SelectionListener#updateSelection()
     */

    /*
       public void updateSelection() {
           checkState();
       }
     */

    /**
     * First try to create an annotation on the actieve tier. When that is not
     * possible check if it is possible to create an annotation on a child
     * tier of the tier the active annotation is on.
     *
     * @return DOCUMENT ME!
     */
    protected boolean checkState() {
        //setEnabled(false);
        receiver = null;
        beginTime = 0;
        endTime = 0;

        if (vm.getSelection() == null) {
            return false;
        }

        // look for the active tier
        if (vm.getMultiTierControlPanel() != null) {
            if (vm.getMultiTierControlPanel().getActiveTier() != null) {
                receiver = (TierImpl) vm.getMultiTierControlPanel()
                                        .getActiveTier();

                if (vm.getSelection().getBeginTime() != vm.getSelection()
                                                              .getEndTime()) {
                    beginTime = vm.getSelection().getBeginTime();
                    endTime = vm.getSelection().getEndTime();

                    if (receiver.isTimeAlignable()) {
                        return true;
                    } else {
                        //check if we can create a child annotation
                        Constraint c = null;

                        if (receiver.getLinguisticType() != null) {
                            c = receiver.getLinguisticType().getConstraints();
                        }

                        if ((receiver.getParentTier() != null) && (c != null)) {
                            if ((c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) ||
                                    (c.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION)) {
                                long time = (beginTime + endTime) / 2;
                                Annotation refA = receiver.getParentTier().getAnnotationAtTime(time);
                                Annotation curA = receiver.getAnnotationAtTime(time);

                                if ((refA != null) && (curA == null)) {
                                    beginTime = time;
                                    endTime = time;

                                    return true;
                                } else if (refA != null) {
                                	// there is already a child annotation
                                	JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()), 
                                			ElanLocale.getString("Message.ExistingAnnotation"), 
                                			ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
                                } else {
                                	//there is no parent
                                	JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()), 
                                			ElanLocale.getString("Message.NoParent"), 
                                			ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        }
                    }
                } else {
                    // there is no selection, look for an active annotation and see
                    // if an annotation can be created at the 'joint' of active tier and active annotation
                    if ((vm.getActiveAnnotation() != null) &&
                            (vm.getActiveAnnotation().getAnnotation() != null)) {
                        Annotation activeAnn = vm.getActiveAnnotation()
                                                 .getAnnotation();

                        if (activeAnn.getTier() == receiver) {
                            return false;
                        }

                        List<TierImpl> depTiers = ((TierImpl) activeAnn.getTier()).getDependentTiers();

                        if (depTiers == null || depTiers.isEmpty() ||
                                !depTiers.contains(receiver)) {
                            return false;
                        }

                        Constraint con = null;

                        if (receiver.getLinguisticType() != null) {
                            con = receiver.getLinguisticType().getConstraints();
                        }

                        if ((con != null) &&
                                ((con.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) ||
                                (con.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION))) {
                            long time = (activeAnn.getBeginTimeBoundary() +
                                activeAnn.getEndTimeBoundary()) / 2;

                            if (receiver.getAnnotationAtTime(time) == null) {
                                beginTime = time;
                                endTime = time;

                                return true;
                            }
                        }
                    }
                }
            }
        }

        // end active tier  search
        receiver = null;

        return false;

        // if we get here try to get the active annotation and find children
        // this might be not very intuitive

        /*
           if (vm.getActiveAnnotation() == null || vm.getActiveAnnotation().getAnnotation() == null) {
               return false;
           }
           Annotation active = vm.getActiveAnnotation().getAnnotation();
           Vector depTiers = ((TierImpl)active.getTier()).getDependentTiers(null);
           if (depTiers == null || depTiers.size() == 0) {
               return false;
           }
           Iterator tierIt = depTiers.iterator();
           while (tierIt.hasNext()) {
               TierImpl child = (TierImpl)tierIt.next();
               Constraint con = null;
               if (child.getLinguisticType() != null) {
                   con = child.getLinguisticType().getConstraints();
               }
               if (con == null) {
                   continue;
               } else {
                   if (con.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION || con.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
                       long time = (active.getBeginTimeBoundary() + active.getEndTimeBoundary()) / 2;
                       if (((TierImpl)active.getTier()).getAnnotationAtTime(time) != null) {
                           continue;
                       } else {
                           receiver = child;
                           beginTime = time;
                           endTime = time;
                           return true;
                       }
                   }
               }
           }
           return false;
         */
    }
}
