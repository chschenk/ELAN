package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * DOCUMENT ME!
 * $Id: ActiveAnnotationCommand.java 45912 2017-02-01 15:48:13Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class ActiveAnnotationCommand implements Command {
    private String commandName;

    /**
     * Creates a new ActiveAnnotationCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ActiveAnnotationCommand(String theName) {
        commandName = theName;
    }

    /**
     * Added support for an additional argument that determines whether or not the crosshair may be moved
     * 
     *@version 01 2013 added argument
     * @param receiver the ViewerManager
     * @param arguments arg[0] = the annotation to activate (can be null) (Annotation)
     * 					arg[1] = flag to indicate whether the media time may be changed (Boolean)
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        Annotation annot = (Annotation) arguments[0];
        
        boolean setCrosshair = true;
        if (arguments.length > 1) {
        	if (arguments[1] instanceof Boolean) {
        		setCrosshair = (Boolean)arguments[1];
        	}
        }

        vm.getActiveAnnotation().setAnnotation(annot);
        final Selection selection = vm.getSelection();
		long curBeg = selection.getBeginTime();
        long curEnd = selection.getEndTime();
        
        if (annot != null) {
            if (annot instanceof AlignableAnnotation) {
            	if (curBeg != annot.getBeginTimeBoundary() || curEnd != annot.getEndTimeBoundary()) {
	                selection.setSelection(annot.getBeginTimeBoundary(),
	                    annot.getEndTimeBoundary());
            	}

            	if (setCrosshair) {
	                if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
	                    vm.getMediaPlayerController().toggleActiveSelectionBoundary();
	                }
            	}
            } else if (annot instanceof RefAnnotation) {
            	Annotation parent = annot;
            	while (true) {
            		parent = parent.getParentAnnotation();
            		if (parent == null || parent instanceof AlignableAnnotation) {
            			break;
            		}
            	}
            	if (parent instanceof AlignableAnnotation) {
            		AlignableAnnotation aa = (AlignableAnnotation) parent;
            		
            		if (curBeg != aa.getBeginTimeBoundary() || curEnd != aa.getEndTimeBoundary()) { 
						selection.setSelection(aa.getBeginTimeBoundary(),
								aa.getEndTimeBoundary());
            		}
            	}
            }

            if (setCrosshair) {
            	final ElanMediaPlayer masterMediaPlayer = vm.getMasterMediaPlayer();
				masterMediaPlayer.setMediaTime(annot.getBeginTimeBoundary());

				Boolean pref = Preferences.getBool("Media.Autoplay.ActivateAnnotation", null);
				if (pref != null && pref) { 
					long startTime = selection.getBeginTime();
					long stopTime = selection.getEndTime();
					masterMediaPlayer.playInterval(startTime, stopTime);
				}
            }

            /* // different behavior in selectionmode / non-selectionmode
               if (!vm.getMediaPlayerController().getSelectionMode()) {
                   vm.getSelection().setSelection(annot.getBeginTimeBoundary(), annot.getEndTimeBoundary());
                   if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                       vm.getMediaPlayerController().toggleActiveSelectionBoundary();
                   }
                   vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
               }    else { //selection mode
                   if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                       vm.getMasterMediaPlayer().setMediaTime(annot.getEndTimeBoundary());
                   }    else {
                       vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
                   }
               }
               } else { // non-alignable annotation
                   vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
               }
             */
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
