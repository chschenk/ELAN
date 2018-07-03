package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * This CommandAction is not intended to be used in a user interface  as an
 * Action for a button or menu item.<br>
 * This Action is only created after the user has chosen to use automatic
 * backup. Other ActionCommands set / change fields in this action whereupon
 * this action  changes or creates a backup Timer task.
 *
 * @version April 2010 instead of one bu file use multiple
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class BackupCA extends CommandAction {
    /** Holds value of property DOCUMENT ME! */
    private static final Logger LOG = Logger.getLogger(BackupCA.class.getName());

    /** The delay for the backup thread */
    private int delay = 0;

    /** Only prompt the user once to save the file before backup starts */
    private boolean promptedOnce = false;

    /** The path of the .eaf file */
    private String filePath = "";

    /** The timer that manages backup process */
    private Timer timer;

    /** the transcription store */
    protected TranscriptionStore transcriptionStore;
    
    /** the size of the carousel of back up files */
    private int numBuFiles = 1;
    
    /** the current index of bu file */
    private int curIndex = 1;

    /**
     * Creates a new BackupCA instance
     *
     * @param viewerManager the viewermanager
     */
    public BackupCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();

        if (vm.getTranscription() instanceof TranscriptionImpl) {
            filePath = ((TranscriptionImpl) vm.getTranscription()).getPathName();
        }
    }

    /**
     * This CommandAction creates no Command. All the actions are performed by
     * this  CommandAction.
     */
    @Override
	protected void newCommand() {
        command = null;
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return <code>null</code>
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * Returns null.
     *
     * @return <code>null</code>
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }

    /**
     * Returns the delay of the current backup thread.
     *
     * @return the current delay of the timer task
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Returns the path to the .eaf file.
     *
     * @return the path to the .eaf file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Changes the backup delay time and starts a new backup thread.
     *
     * @param delayTime the new delay of the timer task
     */
    public void setDelay(int delayTime) {
        if (delay != delayTime) {
            delay = delayTime;
            Preferences.set("BackUpDelay", delay, null);

            if (delayTime == 0) {
                if (timer != null) {
                    timer.stop();
                }
            } else {
                if (timer != null) {
                    timer.stop();

                    /*
                       timer.setDelay(delayTime);
                       } else {
                           createTimer();
                     */
                }

                createTimer();
            }
        }
    }

	/**
	 * Returns the number of back up files.
	 * 
	 * @return the number of back up files
	 */
    public int getNumBuFiles() {
		return numBuFiles;
	}

    /**
     * Sets the number of back up file to use.
     * 
     * @param numBuFiles the new number of back up files to use
     */
	public void setNumBuFiles(int numBuFiles) {
		this.numBuFiles = numBuFiles;
		
    	if (curIndex > this.numBuFiles) {
    		curIndex = 1;
    	}
	}
	
    /**
     * Sets the path of the backup file and creates a new backup thread.
     *
     * @param path the new path to the .eaf file
     */
    public void setFilePath(String path) {
        if (!path.equals(filePath)) {
            filePath = path;

            if (timer != null) {
                timer.stop();

                if (delay != 0) {
                    createTimer();
                }
            }
        }
    }

    
    
    /**
     * Stops the backup thread.
     */
    public void stopBackUp() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void createTimer() {
        timer = new Timer(delay, new BackupActionListener());
        timer.start();
    }

    /**
     * The <code>ActionListener</code> for the backup Timer task. <br>
     * Performs the save  action every time it is notified by the Timer.
     *
     * @author Han Sloetjes
     * @version 1.1 20 Apr 2004
     */
    class BackupActionListener implements ActionListener {
        /**
         * Invokes <code>storeTranscription</code> on the
         * <code>Transcription</code> object.
         *
         * @param e the action event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            if (!(vm.getTranscription() instanceof TranscriptionImpl)) {
                return;
            }

            if (transcriptionStore != null) {
                List<TierImpl> visibleTiers = null;// is ignored nowadays when saving to eaf
                if (vm.getMultiTierControlPanel() != null) {
                	vm.getMultiTierControlPanel().getVisibleTiers();
                }

                try {
                    String path = null;
                    String suffix = ".00" + curIndex;

                    if (getFilePath().length() == 0) {
                        path = ((TranscriptionImpl) vm.getTranscription()).getPathName() +
                            suffix;
                    } else if (getFilePath().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                        if (!promptedOnce) {
                            // prompt the user to save the file, but only once
                            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                                    vm.getTranscription()),
                                ElanLocale.getString("Message.Backup"),
                                ElanLocale.getString("Message.Warning"),
                                JOptionPane.WARNING_MESSAGE);
                            promptedOnce = true;
                        }

                        // let the save action continue anyway 
                        path = getFilePath() + ".eaf" + suffix;
                    } else {
                        path = getFilePath() + suffix;
                    }
                    try {
                    transcriptionStore.storeTranscriptionIn(vm.getTranscription(),
                        null, visibleTiers, path, TranscriptionStore.EAF);
                    
                    	curIndex++;
                    	if (curIndex > numBuFiles) {
                    		curIndex = 1;
                    	}
                    } catch (IOException ioe){
                        LOG.severe("Cannot save a backup file: " + ioe.getMessage());
                    }
                } catch (SecurityException sex) {
                    LOG.severe(
                        "Cannot save a backup file - no write permission");
                }
            }
        }
    }

}
