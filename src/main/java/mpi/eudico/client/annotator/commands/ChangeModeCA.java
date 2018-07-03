package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.CommandAction;

import java.awt.event.ActionEvent;

import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;


/**
 * A command action that switches between the modes. By
 * default the Annotation mode is selected
 *
 * @author Aarthy Somasundaram
 */
public class ChangeModeCA extends CommandAction {
    private ElanLayoutManager layoutManager;     
    private String modeName;

    /**
     * Creates a new ChangeModeCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param layoutManager DOCUMENT ME!
     */
    public ChangeModeCA(ViewerManager2 theVM, ElanLayoutManager layoutManager, String mode) {
        super(theVM, mode);
        modeName = mode;
        this.layoutManager = layoutManager;        
        putValue(Action.LONG_DESCRIPTION, mode);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), modeName);
    }

    /**
     * The receiver of this CommandAction is the layoutManager.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] = current mode reference
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
    	return new Object[] {  getModeId() };
    } 
    
    private int getModeId(){
    	if(modeName.equals(ELANCommandFactory.SYNC_MODE)){
    		return ElanLayoutManager.SYNC_MODE;
    	} else if(modeName.equals(ELANCommandFactory.TRANSCRIPTION_MODE)){
    		return ElanLayoutManager.TRANSC_MODE;
    	} else if(modeName.equals(ELANCommandFactory.SEGMENTATION_MODE)){
    		return ElanLayoutManager.SEGMENT_MODE;
    	} else if(modeName.equals(ELANCommandFactory.INTERLINEARIZATION_MODE)){
    		return ElanLayoutManager.INTERLINEAR_MODE;
    	} else {
    		return ElanLayoutManager.NORMAL_MODE;
    	}
    }
    
    /**
     * Not very elegant way to reselect the JRadioMenuItem in case a switch to
     * a certain mode is not allowed. When more mode are added to
     * ElanLayoutManager the switch statement should adapted.
     *
     * @see ActionCommand#actionPerformed
     */
    @Override
	public void actionPerformed(final ActionEvent event) {
        JRadioButtonMenuItem oldItem = null;
        JRadioButtonMenuItem item = null;
        int mode = layoutManager.getMode();
        
        // if the same mode is selected, do nothing
        if(mode == getModeId()){
         	return;
         }

        if (event.getSource() instanceof JRadioButtonMenuItem) {
            item = (JRadioButtonMenuItem) event.getSource();

            if (item.getModel() instanceof JToggleButton.ToggleButtonModel) {
                ButtonGroup group = ((JToggleButton.ToggleButtonModel) item.getModel()).getGroup();
                Enumeration e = group.getElements();
elementloop: 
                while (e.hasMoreElements()) {  
                	oldItem = (JRadioButtonMenuItem) e.nextElement();
                    switch (mode) {  
                    case ElanLayoutManager.NORMAL_MODE:                         	 
                    	 if (oldItem.getAction() == ELANCommandFactory.getCommandAction(
                                    vm.getTranscription(),
                                    ELANCommandFactory.ANNOTATION_MODE)) {
                            break elementloop;
                        } 
                    	oldItem = null;
                        break;
                        
                    case ElanLayoutManager.SYNC_MODE:                    	

                        if (oldItem.getAction() == ELANCommandFactory.getCommandAction(
                                    vm.getTranscription(),
                                    ELANCommandFactory.SYNC_MODE)) {
                            break elementloop;
                        }
                        oldItem = null;
                        break;
                        
                    case ElanLayoutManager.TRANSC_MODE:
                    	
                        if (oldItem.getAction() == ELANCommandFactory.getCommandAction(
                                    vm.getTranscription(),
                                    ELANCommandFactory.TRANSCRIPTION_MODE)) {
                            break elementloop;
                        }
                        oldItem = null;
                        break;
                        
                    case ElanLayoutManager.SEGMENT_MODE:
                        if (oldItem.getAction() == ELANCommandFactory.getCommandAction(
                                    vm.getTranscription(),
                                    ELANCommandFactory.SEGMENTATION_MODE)) {
                            break elementloop;
                        }
                        oldItem = null;
                        break;
                        
                    case ElanLayoutManager.INTERLINEAR_MODE:
                        if (oldItem.getAction() == ELANCommandFactory.getCommandAction(
                                    vm.getTranscription(),
                                    ELANCommandFactory.INTERLINEARIZATION_MODE)) {
                            break elementloop;
                        }
                        oldItem = null;
                        break;

                    default:
                        break;
                    }
                }
            }
        }

        super.actionPerformed(event);

        if (layoutManager.getMode() != this.getModeId()) {
            if (oldItem != null) {
                oldItem.setSelected(true);
            }
        }
    }
}
