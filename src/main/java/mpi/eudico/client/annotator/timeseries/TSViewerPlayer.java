package mpi.eudico.client.annotator.timeseries;

import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.MultiSourcePlayer;
import mpi.eudico.client.annotator.player.SyncPlayer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

import java.awt.Component;
import java.util.ArrayList;


/**
 * A player with a time series viewer as visual component.
 * Used for synchronization of time series files to media files. 
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class TSViewerPlayer extends EmptyMediaPlayer implements SyncPlayer,
    MultiSourcePlayer {
    private TimeSeriesViewer viewer;
    private TSTrackManager trackManager;
    private String currentSource;
    private MediaDescriptor[] descriptors;
    private String[] sources;
    private boolean syncConnected = false;

    /**
     * Creates a new TSViewerPlayer instance
     *
     * @param duration the duration of the player
     */
    public TSViewerPlayer(long duration) {
        super(duration);
    }

    /**
     * Creates a new TSViewerPlayer instance
     *
     * @param viewer the viewer
     * @param duration the duration of the player
     */
    public TSViewerPlayer(TimeSeriesViewer viewer, long duration) {
        super(duration);
        this.viewer = viewer;
        viewer.setRightMargin(0);
    }
    
	@Override
	public boolean isSyncConnected() {
		return syncConnected;
	}

	@Override
	public void setSyncConnected(boolean syncConnected) {
		this.syncConnected = syncConnected;
		if (viewer != null) {
			viewer.setSyncConnected(syncConnected);
//			if (syncConnected) {				
//				viewer.setPlayer(this);
//			}
		}
	}
	
	@Override
	public synchronized void controllerUpdate(ControllerEvent event) {
		if (syncConnected) {
			super.controllerUpdate(event); 
		}
	}

	@Override
	public void startControllers() {
		if (syncConnected) {
			super.startControllers();
		}
	}

	@Override
	public void setMediaTime(long time) {
		if (syncConnected) {
			super.setMediaTime(time);
		}
	}
	
	@Override
	public long getMediaTime() {
		return super.getMediaTime();
	}

	/**
     * Returns the time series viewer.
     *
     * @return the viewer
     */
    @Override
	public Component getVisualComponent() {
        return viewer;
    }

    /**
     * Returns the viewer.
     *
     * @return the viewer
     */
    public TimeSeriesViewer getViewer() {
        return viewer;
    }

    /**
     * Sets the viewer for this player
     *
     * @param viewer the viewer 
     */
    public void setViewer(TimeSeriesViewer viewer) {
        this.viewer = viewer;
        //viewer.setPlayer(this);
        viewer.setRightMargin(0);
    }

	/**
     * Returns the track manager, containing the time series sources 
     * and the configured tracks. 
     *
     * @return the track manager
     */
    public TSTrackManager getTrackManager() {
        return trackManager;
    }

    /**
     * Sets the track manager for this viewer player.
     *
     * @param trackManager the time series track manager
     */
    public void setTrackManager(TSTrackManager trackManager) {
        this.trackManager = trackManager;
        sources = trackManager.getCurrentSourceNames();

        if (sources != null) {
            descriptors = new MediaDescriptor[sources.length];

            for (int i = 0; i < sources.length; i++) {
                descriptors[i] = new MediaDescriptor(sources[i],
                        MediaDescriptor.UNKNOWN_MIME_TYPE);
            }

            if (sources.length > 0) {
                setCurrentSource(sources[0]);
            }
        }
    }

    /**
     * Makes sure the player is in sync with the sources in the TrackManager.
     */
    private void updateDescriptorStrings() {
        String[] curTrackSources = trackManager.getCurrentSourceNames();

        if (sources == null) {
            sources = curTrackSources;
            descriptors = new MediaDescriptor[sources.length];

            for (int i = 0; i < sources.length; i++) {
                descriptors[i] = new MediaDescriptor(sources[i],
                        MediaDescriptor.UNKNOWN_MIME_TYPE);
            }

            if ((currentSource == null) && (sources.length > 0)) {
                setCurrentSource(sources[0]);
            } else {
                for (int i = 0; i < sources.length; i++) {
                    if (sources[i].equals(currentSource)) {
                        break;
                    }

                    if (i == (sources.length - 1)) {
                        // currentSource no longer in the list
                        setCurrentSource(sources[0]);
                    }
                }
            }
        } else {
            // synchronize
            ArrayList<String> newSources = new ArrayList<String>(2);
            ArrayList<String> remSources = new ArrayList<String>(2);
trackloop: 
            for (int i = 0; i < curTrackSources.length; i++) {
                for (int j = 0; j < sources.length; j++) {
                    if (curTrackSources[i].equals(sources[j])) {
                        continue trackloop;
                    }

                    if (j == (sources.length - 1)) {
                        newSources.add(curTrackSources[i]);
                    }
                }
            }

sourceloop: 
            for (int i = 0; i < sources.length; i++) {
                for (int j = 0; j < curTrackSources.length; j++) {
                    if (sources[i].equals(curTrackSources[j])) {
                        continue sourceloop;
                    }

                    if (j == (curTrackSources.length - 1)) {
                        remSources.add(sources[i]);
                    }
                }
            }

            if ((newSources.size() == 0) && (remSources.size() == 0)) {
                return;
            }

            ArrayList<String> curSources = new ArrayList<String>(4);

            for (int j = 0; j < sources.length; j++) {
                if (!remSources.contains(sources[j])) {
                    curSources.add(sources[j]);
                }
            }

            curSources.addAll(newSources);
            sources = curSources.toArray(new String[] {  });

            descriptors = new MediaDescriptor[sources.length];

            for (int i = 0; i < sources.length; i++) {
                descriptors[i] = new MediaDescriptor(sources[i],
                        MediaDescriptor.UNKNOWN_MIME_TYPE);
            }

            if ((currentSource == null) && (sources.length > 0)) {
                setCurrentSource(sources[0]);
            } else {
                for (int i = 0; i < sources.length; i++) {
                    if (sources[i].equals(currentSource)) {
                        break;
                    }

                    if (i == (sources.length - 1)) {
                        // currentSource no longer in the list
                        setCurrentSource(sources[0]);
                    }
                }
            }
        }
    }

    /**
     * Returns the currently selected source file; the track manager 
     * and the time series viewer can handle multiple source files.
     *
     * @return the selected source
     */
    @Override
	public String getCurrentSource() {
        return currentSource;
    }

    /**
     * Sets the current source.
     *
     * @param currentSource the source to be selected
     */
    @Override
	public void setCurrentSource(String currentSource) {
        this.currentSource = currentSource;

        if (trackManager != null) {
            setOffset(trackManager.getOffset(currentSource));
        } else {
            setOffset(0);
        }
    }

    /**
     * Returns all media descriptor url's.
     *
     * @return the media descriptors url's
     */
    @Override
	public String[] getDescriptorStrings() {
        if (sources == null) {
            sources = new String[] {  };
        }

        updateDescriptorStrings();

        return sources;
    }

    /**
     * Returns the media descriptors.
     *
     * @return the media (source) descriptors
     */
    @Override
	public MediaDescriptor[] getDescriptors() {
        return descriptors;
    }

    /**
     * Sets the media (source) descriptors
     *
     * @param descriptors the new descriptors
     */
    @Override
	public void setDescriptors(MediaDescriptor[] descriptors) {
        this.descriptors = descriptors;

        if (descriptors != null) {
            String[] sources = new String[descriptors.length];

            for (int i = 0; i < descriptors.length; i++) {
                sources[i] = descriptors[i].mediaURL;
            }
        }
    }

    /**
     * Sets the time offset for the player and for the currently
     * selected time series source file.
     *
     * @param offset the offset in milliseconds
     */
    @Override
	public void setOffset(long offset) {
        super.setOffset(offset);

        if (trackManager != null) {
            trackManager.setOffset(currentSource, (int) offset);
        }

        if (viewer != null) {
        	viewer.controllerUpdate(new TimeEvent(this));
            // force creation of new buffer image and repaint
            //viewer.componentResized(null);
        }
    }

    /**
     * Returns the aspect ratio of the visual component.
     *
     * @return the aspect ratio
     */
    @Override
	public float getAspectRatio() {
        return 2.0f;
    }

    /**
     * Tries to return the media duration of the currently selected data source.
     * It is assumed that all tracks from the same source have the same duration 
     * (i.e. that there is at most one column with time stamps per file) 
     * 
     * @return the data duration of the current source file
     */
	@Override
	public long getMediaDuration() {
		// try to get the duration of the currently selected source, e.g. via the TrackManager
		// or from the first track created from that source (if any)
		if (currentSource != null) {
			long dataDuration = trackManager.getDataDuration(currentSource);
			
			if (dataDuration > 0) {// >= ??
				return dataDuration; // or dataDuration - offset ??
			}
		}
		
		return super.getMediaDuration();
	}
    
    
}
