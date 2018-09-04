package mpi.eudico.client.annotator.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.linkedmedia.LinkedFileDescriptorUtil;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command to change the set of linked media or secondary files.
 *
 * @author Han Sloetjes
 */
public class ChangeLinkedFilesCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription 
    private TranscriptionImpl transcription;
    private List<MediaDescriptor> oldMediaDescriptors;
    private List<MediaDescriptor> newMediaDescriptors;
    
    private List<LinkedFileDescriptor> oldLinkedFileDescriptors;
    private List<LinkedFileDescriptor> newLinkedFileDescriptors;

    // a flag for the kind of descriptors that have been passed to the command
    private boolean areMediaDesc = true;
    // a flag to indicate that only the time offsets of linked files changed
    private boolean timeOffsetsChangedOnly = false;
    // contains mapping from media or link url to an offset. Assumes the same file is not linked more than once.
    private Map<String, Long> oldDescOffsetMap;
    private Map<String, Long> newDescOffsetMap;

    /**
     * Creates a new ChangeLinkedFilesCommand instance
     *
     * @param name the name of the command
     */
    public ChangeLinkedFilesCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Restores the old values of the linked files.
     */
    @Override
	public void undo() {
        if ((transcription != null) && (oldMediaDescriptors != null)) {
            if (areMediaDesc) {
            	if (!timeOffsetsChangedOnly) {
            		updateMediaPlayers(transcription, oldMediaDescriptors);
            	} else {
            		updateMediaPlayersOffset(transcription, oldDescOffsetMap);
            	}
            } else {
            	if (!timeOffsetsChangedOnly) {
            		updateLinkedFiles(transcription, oldLinkedFileDescriptors);
            	} else {
            		updateLinkedFilesOffset(transcription, oldDescOffsetMap);
            	}
            }
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if ((transcription != null) && (newMediaDescriptors != null)) {
            if (areMediaDesc) {
            	if (!timeOffsetsChangedOnly) {
            		updateMediaPlayers(transcription, newMediaDescriptors);
            	} else {
            		updateMediaPlayersOffset(transcription, newDescOffsetMap);
            	}
            } else {
            	if (!timeOffsetsChangedOnly) {
            		updateLinkedFiles(transcription, newLinkedFileDescriptors);
            	} else {
            		updateLinkedFilesOffset(transcription, newDescOffsetMap);
            	}
            }
        }
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the list with the new media descriptors or
	 *            secondary linked files descriptors (List)</li>
	 *            <li>arg[1] = a flag for the type of descriptors: if
	 *            <code>true</code> the descriptors are primary, media
	 *            descriptors, if <code>false</code> the descriptors are
	 *            secondary linked file descriptors</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if ((arguments != null) && (arguments.length >= 1)) {
            if (arguments.length > 1) {
                areMediaDesc = ((Boolean) arguments[1]).booleanValue();
            }
            if (arguments.length > 2) {
            	timeOffsetsChangedOnly = ((Boolean) arguments[2]).booleanValue();
            }
            
            if (areMediaDesc) {
                newMediaDescriptors = (List<MediaDescriptor>) arguments[0];
            } else {
                newLinkedFileDescriptors = (List<LinkedFileDescriptor>) arguments[0];
            }
        }

        if (transcription != null) {
            if (areMediaDesc) {
                oldMediaDescriptors = transcription.getMediaDescriptors();

                if (!timeOffsetsChangedOnly) {
	                // check if there is any difference??
	                updateMediaPlayers(transcription, newMediaDescriptors);
	                // enable / disable the setFrameLength menu
	                ElanFrame2 ef2 = (ElanFrame2) ELANCommandFactory.getRootFrame(transcription);
	                if (ef2 != null) {
	                    ElanMediaPlayer master = ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer();
	                    ef2.setMenuEnabled(FrameConstants.FRAME_LENGTH, !master.isFrameRateAutoDetected());
	                    ef2.updateMenu(FrameConstants.MEDIA_PLAYER);
	                    ef2.updateMenu(FrameConstants.WAVE_FORM_VIEWER);
	                }
                } else {
                	oldDescOffsetMap = new HashMap<String, Long>(oldMediaDescriptors.size());                	
                	newDescOffsetMap = new HashMap<String, Long>(newMediaDescriptors.size());
                	
                	MediaDescriptor md;
                	for (int i = 0; i < oldMediaDescriptors.size(); i++) {
                		md = oldMediaDescriptors.get(i);
                		oldDescOffsetMap.put(md.mediaURL, md.timeOrigin);
                	}
                	for (int i = 0; i < newMediaDescriptors.size(); i++) {
                		md = newMediaDescriptors.get(i);
                		newDescOffsetMap.put(md.mediaURL, md.timeOrigin);
                	}
                	
                	updateMediaPlayersOffset(transcription, newDescOffsetMap);
                }
            } else {
                oldLinkedFileDescriptors = transcription.getLinkedFileDescriptors();
                if (!timeOffsetsChangedOnly) {
                	updateLinkedFiles(transcription, newLinkedFileDescriptors);
                } else {
                	oldDescOffsetMap = new HashMap<String, Long>(oldLinkedFileDescriptors.size());
                	newDescOffsetMap = new HashMap<String, Long>(newLinkedFileDescriptors.size());
                	LinkedFileDescriptor lfd;
                	
                	for(int i = 0; i < oldLinkedFileDescriptors.size(); i++) {
                		lfd = oldLinkedFileDescriptors.get(i);
                		oldDescOffsetMap.put(lfd.linkURL, lfd.timeOrigin);
                	}
                	
                	for(int i = 0; i < newLinkedFileDescriptors.size(); i++) {
                		lfd = newLinkedFileDescriptors.get(i);
                		newDescOffsetMap.put(lfd.linkURL, lfd.timeOrigin);
                	}
                	
                	updateLinkedFilesOffset(transcription, newDescOffsetMap);
                }
            }
        }
    }

    /**
     * Tries to update the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    private void updateMediaPlayers(TranscriptionImpl transcription,
        List<MediaDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        MediaDescriptorUtil.updateMediaPlayers(transcription, descriptors);
    }
    
    /**
     * Tries to update the existing mediaplayers and viewers in the viewermanager as well as the
     * layoutmanager and the media descriptors in the transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param offsetMapping the new offsets per media url
     */
    private void updateMediaPlayersOffset(TranscriptionImpl transcription,
        Map<String, Long> newMediaOffsets) {
        if ((transcription == null) || (newMediaOffsets == null)) {
            return;
        }

        MediaDescriptorUtil.updateMediaPlayerOffsets(transcription, newMediaOffsets);
    }

    /**
     * Delegates all updating that needs to be done to a utility class.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new linked file descriptors
     */
    private void updateLinkedFiles(TranscriptionImpl transcription,
        List<LinkedFileDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        LinkedFileDescriptorUtil.updateLinkedFiles(transcription, descriptors);
    }
    
    /**
     * Updates the offsets of the timeseries tracks and of the media descriptors in 
     * the transcription.
     *
     * @param transcription the Transcription 
     * @param offsetMapping the new offsets per linked file
     */
    private void updateLinkedFilesOffset(TranscriptionImpl transcription,
        Map<String, Long> newLinkOffsets) {
        if ((transcription == null) || (newLinkOffsets == null)) {
            return;
        }
        
        LinkedFileDescriptorUtil.updateLinkedFilesOffsets(transcription, newLinkOffsets);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
