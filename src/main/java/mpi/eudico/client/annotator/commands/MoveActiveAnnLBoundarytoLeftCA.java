package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;


/**
 * DOCUMENT ME! 
 *
 * A command action for moving the left boundary of the active annotation to the left.
 * 
 * @author $Aarthy Somasundaram$
 * @version $Jan 2010$
 */
public class MoveActiveAnnLBoundarytoLeftCA extends CommandAction {

	/**
     * Creates a new MoveActiveAnnLBoundarytoLeftCA instance
     *
     * @param theVM DOCUMENT ME!
     */
	public MoveActiveAnnLBoundarytoLeftCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT);		
	}

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() { 
        
        if (vm.getActiveAnnotation().getAnnotation() instanceof AlignableAnnotation) {
            AlignableAnnotation aa = (AlignableAnnotation) vm.getActiveAnnotation()
                                                             .getAnnotation();

            if (aa !=null) {
            	command = ELANCommandFactory.createCommand(vm.getTranscription(),
                        ELANCommandFactory.MODIFY_ANNOTATION_TIME);
            } else {
                command = null;
            }
        } else {
            command = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getActiveAnnotation().getAnnotation();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() { 
        Object[] args = new Object[2];
        args[0] = new Long( vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary() - (long)vm.getTimeScale().getMsPerPixel());
        args[1] = new Long(vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary());            
        
        return args;
    } 

}
