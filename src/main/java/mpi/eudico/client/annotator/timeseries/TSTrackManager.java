package mpi.eudico.client.annotator.timeseries;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationEncoder;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceRegistry;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Manages time series tracks, their sources and configurations  and one or
 * more TimeSeriesViewers.
 *
 * @author Han Sloetjes
 */
public class TSTrackManager implements TimeSeriesChangeListener {
    private TranscriptionImpl transcription;

    /** stores all defined tracks, from all source files */
    private List<TimeSeriesTrack> tracks; // sometimes used as AbstractTSTrack

    // sourcename - configuration mappings
    private Map<String, TSSourceConfiguration> trackSourceConfigs;
    private List<TimeSeriesChangeListener> listeners;
    private TSConfigurationEncoder encoder;
    //private TSViewerPlayer syncPlayer;
    // a flag to store the changed state (for those cases in which changes are not immediately stored on disc)
    private boolean changed = false;

    /**
     * Creates a new TSTrackManager instance
     *
     * @param transcription the transcription, the document identifier
     */
    public TSTrackManager(Transcription transcription) {
        this.transcription = (TranscriptionImpl) transcription;
        tracks = new ArrayList<TimeSeriesTrack>();
        trackSourceConfigs = new HashMap<String, TSSourceConfiguration>();
        encoder = new TSConfigurationEncoder();

        //encoder.encodeAndSave((TranscriptionImpl) transcription);
    }

    /**
     * Returns the list of all tracks from all sources that have been
     * associated with the document.
     *
     * @return the tracks that have been added to the manager
     */
    public List<TimeSeriesTrack> getRegisteredTracks() {
        return tracks;
    }

    /**
     * Returns the track with the specified name, if defined.
     *
     * @param name the name of the track
     *
     * @return the track or null
     */
    public AbstractTSTrack getTrack(String name) {
        if (name == null) {
            return null;
        }

        AbstractTSTrack tr;

        for (int i = 0; i < tracks.size(); i++) {
            tr = (AbstractTSTrack) tracks.get(i);

            if (tr.getName().equals(name)) {
                return tr;
            }
        }

        return null;
    }

    /**
     * Returns a key set of the source configurations that have been added to
     * the manager.
     *
     * @return the set of source configuration keys
     */
    public Set<String> getConfigKeySet() {
        return trackSourceConfigs.keySet();
    }
    
    /**
     * Returns the configuration objects.
     * 
     * @return the collection of the configuration objects
     */
    public Collection<TSSourceConfiguration> getConfigs() {
    	return trackSourceConfigs.values();
    }

    /**
     * Returns the source url's of the currently managed sources.
     *  
     * @return a String array of source url's
     */
    public String[] getCurrentSourceNames() {
    	try {
    		return getConfigKeySet().toArray(new String[]{});
    	} catch (Exception ex) {
    		return new String[0];
    	}   		
    }
    
    /**
     * Sets the offset of the specified time series source. All tracks configured from that 
     * source receive the same offset.
     *
     * @param source the source identifier (source url)
     * @param offset the new offset for the ts source
     */
    public void setOffset(String source, int offset) {
        if (source == null) {
            return;
        }

        TSSourceConfiguration configuration = trackSourceConfigs.get(source);

        if (configuration != null) {
            configuration.setTimeOrigin(offset);
            
            // update the transcription
            List<LinkedFileDescriptor> lfds = transcription.getLinkedFileDescriptors();
            for (int j = 0; j < lfds.size(); j++) {
            	LinkedFileDescriptor lfd = lfds.get(j);
            	if (lfd.linkURL.equals(source)) {
            		lfd.timeOrigin = offset;
            		transcription.setChanged();
            		break;
            	}
            }

            Iterator keyIt = configuration.objectKeySet().iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                Object o = configuration.getObject(key);

                if (o instanceof AbstractTSTrack) {
                    ((AbstractTSTrack) o).setTimeOffset(offset);
                } else if (o instanceof TSTrackConfiguration) { // default

                    TSTrackConfiguration tstc = (TSTrackConfiguration) o;
                    Object tr = tstc.getObject(tstc.getTrackName());

                    if (tr instanceof AbstractTSTrack) {
                        ((AbstractTSTrack) tr).setTimeOffset(offset); // default situation
                    } else {
                        Iterator objIt = tstc.objectKeySet().iterator();

                        while (objIt.hasNext()) {
                            Object ke = objIt.next();
                            Object oo = tstc.getObject(ke);

                            if (oo instanceof AbstractTSTrack) {
                                ((AbstractTSTrack) oo).setTimeOffset(offset);
                            }
                        }
                    }
                }
            }

            notifyListeners(new TimeSeriesChangeEvent(configuration,
                    TimeSeriesChangeEvent.CHANGE,
                    TimeSeriesChangeEvent.TS_SOURCE));
            encoder.encodeAndSave(transcription,
                trackSourceConfigs.values());
        }
    }

    /**
     * Returns the time offset (origin) of the specified source
     *
     * @param source the source url string (id)
     *
     * @return the offset of the specified timeseries source and thus the offset 
     * of all tracks originating from that source
     */
    public int getOffset(String source) {
        if (source == null) {
            return 0;
        }

        TSSourceConfiguration configuration = trackSourceConfigs.get(source);

        if (configuration != null) {
            return configuration.getTimeOrigin();
        }

        return 0;
    }
    

    /**
     * Returns the "duration" of the tracks in the specified source, if retrievable.
     * Duration here means the value of the last (or largest) time stamp, it doesn't
     * consider the value of the first time stamp (implicitly 0).
     * 
     * @param source the source url string (id)
     * @return the data duration or -1 if not available
     */
	public long getDataDuration(String source) {
		if (source != null) {
	        TSSourceConfiguration configuration = trackSourceConfigs.get(source);

	        if (configuration != null) {
	            Iterator<Object> keyIt = configuration.objectKeySet().iterator();
	            long dataDuration = -1;
	            
	            while (keyIt.hasNext()) {
	                Object key = keyIt.next();
	                Object o = configuration.getObject(key);

	                if (o instanceof AbstractTSTrack) {
	                    dataDuration = ((AbstractTSTrack) o).getDataDuration();
	                    break;
	                } else if (o instanceof TSTrackConfiguration) { // default

	                    TSTrackConfiguration tstc = (TSTrackConfiguration) o;
	                    Object tr = tstc.getObject(tstc.getTrackName());

	                    if (tr instanceof AbstractTSTrack) {// default situation
	                        dataDuration = ((AbstractTSTrack) tr).getDataDuration();
	                        break;
	                    } else {
	                        Iterator<Object> objIt = tstc.objectKeySet().iterator();

	                        while (objIt.hasNext()) {
	                            Object ke = objIt.next();
	                            Object oo = tstc.getObject(ke);

	                            if (oo instanceof AbstractTSTrack) {
	    	                        dataDuration = ((AbstractTSTrack) oo).getDataDuration();
	    	                        break;
	                            }
	                        }
	                    }
	                }
	            }
	            
	            return dataDuration;
	        }
		}
		
		return -1;
	}

    /**
     * If there is a configurable track source show the configuration dialog.
     * If there are more than one sources show a selection dialog with the
     * sources. Otherwise just show a message dialog that there is nothing to
     * configure.
     *
     * @param parent the parent component
     */
    public void configureTracks(Component parent) {
        TSServiceRegistry registry = TSServiceRegistry.getInstance();
        TSServiceProvider provider;
        List<TSSourceConfiguration> configurables = new ArrayList<TSSourceConfiguration>(3);
        Iterator<String> confIt = getConfigKeySet().iterator();
        TSSourceConfiguration config;

        while (confIt.hasNext()) {
            config = trackSourceConfigs.get(confIt.next());

            if (config.getProviderClassName() != null) {
                provider = registry.getProviderByClassName(config.getProviderClassName());
            } else {
                provider = registry.getProviderForFile(config.getSource());
            }

            if (provider != null) {
                if (provider.isConfigurable()) {
                    configurables.add(config);
                }
            }
        }

        TSConfigurationUI cui = new TSConfigurationUI();

        switch (configurables.size()) {
        case 0:
            // show message
            cui.showNoConfigMessage(parent);

            break;

        case 1:
            // show config for the one
            cui.showConfigDialog(parent,
                configurables.get(0), this);

            break;

        default:

            // show selection option pane
            TSSourceConfiguration cfg = cui.selectConfigurableSource(parent,
                    configurables);

            if (cfg != null) {
                cui.showConfigDialog(parent, cfg, this);
            }

            break;
        }
    }

    /**
     * Informs the track manager about the creation of a new track from the
     * specified source file. The track(configuration) might or might not have
     * been added to the source configuration.  In the latter case it is added
     * to the source config here.  The track is added to the list of
     * registered tracks and listeners are notified.
     *
     * @param sourceConfig the configuration of the source file
     * @param trackConfig the configuration of the track
     */
    public void addTrack(TSSourceConfiguration sourceConfig,
        TSTrackConfiguration trackConfig) {
        if ((sourceConfig == null) || (trackConfig == null)) {
            return;
        }

        // check if the track has been added to the source configuration
        if (sourceConfig.getObject(trackConfig.getTrackName()) == null) {
            sourceConfig.putObject(trackConfig.getTrackName(), trackConfig);
        }

        Object tr = trackConfig.getObject(trackConfig.getTrackName());

        if (tr instanceof TimeSeriesTrack) {
            tracks.add((TimeSeriesTrack) tr); // default situation
        } else {
            Iterator objIt = trackConfig.objectKeySet().iterator();

            while (objIt.hasNext()) {
                Object key = objIt.next();
                Object oo = trackConfig.getObject(key);

                if (oo instanceof TimeSeriesTrack) {
                    tracks.add((TimeSeriesTrack) oo);
                }
            }
        }

        notifyListeners(new TimeSeriesChangeEvent(trackConfig,
                TimeSeriesChangeEvent.ADD, TimeSeriesChangeEvent.TRACK));
    }
    
    /**
     * Add a track (temporarily), without configuration.
     * @param tr the track
     */
    public void addTrack(TimeSeriesTrack tr) {
    	if (tr != null) {
    		tracks.add(tr);
    		
            notifyListeners(new TimeSeriesChangeEvent(tr,
                    TimeSeriesChangeEvent.ADD, TimeSeriesChangeEvent.TRACK_AND_PANEL));  		
    	}
    }
    
    /**
     * Remove a track, without a configuration.
     * 
     * @param tr the track
     */
    public void removeTrack(TimeSeriesTrack tr) {
    	if (tr != null) {
    		tracks.remove(tr);
    		
            notifyListeners(new TimeSeriesChangeEvent(tr,
                    TimeSeriesChangeEvent.DELETE, TimeSeriesChangeEvent.TRACK_AND_PANEL));  		
    	}
    }

    /**
     * Informs the track manager about the deletion of a track from the
     * specified source file. The track(configuration) might or might not have
     * been removed from the source configuration.  In the latter case it is
     * removed from the source config here.  The track is removed from the
     * list of registered tracks and listeners are notified.
     *
     * @param sourceConfig the configuration of the source file
     * @param trackConfig the configuration of the track
     */
    public void removeTrack(TSSourceConfiguration sourceConfig,
        TSTrackConfiguration trackConfig) {
        if ((sourceConfig == null) || (trackConfig == null)) {
            return;
        }

        // check if the track has been removed from the source configuration
        if (sourceConfig.getObject(trackConfig.getTrackName()) != null) {
            sourceConfig.removeObject(trackConfig.getTrackName());
        }

        Object tr = trackConfig.getObject(trackConfig.getTrackName());

        if (tr instanceof TimeSeriesTrack) {
            tracks.remove(tr); // default situation
        } else {
            Iterator objIt = trackConfig.objectKeySet().iterator();

            while (objIt.hasNext()) {
                Object key = objIt.next();
                Object oo = trackConfig.getObject(key);

                if (oo instanceof TimeSeriesTrack) {
                    tracks.remove(oo);
                }
            }
        }

        notifyListeners(new TimeSeriesChangeEvent(trackConfig,
                TimeSeriesChangeEvent.DELETE, TimeSeriesChangeEvent.TRACK));
    }

    /**
     * Removes a track from the list of registered tracks.
     *
     * @param sourceConfig the configuration of the source file
     * @param track the track object
     *
     * @see #removeTrack(TSSourceConfiguration, TSTrackConfiguration)
     */
    public void removeTrack(TSSourceConfiguration sourceConfig,
        TimeSeriesTrack track) {
        if ((sourceConfig == null) || (track == null)) {
            return;
        }

        // check if the track has been removed from the source configuration
        if (sourceConfig.getObject(track.getName()) != null) {
            sourceConfig.removeObject(track.getName());
        }

        tracks.remove(track);

        notifyListeners(new TimeSeriesChangeEvent(track,
                TimeSeriesChangeEvent.DELETE, TimeSeriesChangeEvent.TRACK));
    }

    /**
     * Called when a track configuration has changed.
     *
     * @param trackConfig the track configuration
     */
    public void trackChanged(TSTrackConfiguration trackConfig) {
        if (trackConfig == null) {
            return;
        }

        // check if the track is in the list
        AbstractTSTrack track = null;
        Object tr = trackConfig.getObject(trackConfig.getTrackName());

        if (tr instanceof AbstractTSTrack) {
            track = (AbstractTSTrack) tr;

            if (tracks.contains(track)) {
                notifyListeners(new TimeSeriesChangeEvent(track,
                        TimeSeriesChangeEvent.CHANGE,
                        TimeSeriesChangeEvent.TRACK));
            }
        } else {
            Iterator objIt = trackConfig.objectKeySet().iterator();

            while (objIt.hasNext()) {
                Object key = objIt.next();
                Object oo = trackConfig.getObject(key);

                if (oo instanceof AbstractTSTrack) {
                    track = (AbstractTSTrack) oo;

                    if (tracks.contains(track)) {
                        notifyListeners(new TimeSeriesChangeEvent(track,
                                TimeSeriesChangeEvent.CHANGE,
                                TimeSeriesChangeEvent.TRACK));
                    }
                }
            }
        }
    }

    /**
     * Called when a track has changed.
     *
     * @param track the track
     */
    public void trackChanged(TimeSeriesTrack track) {
        if (track == null) {
            return;
        }

        if (tracks.contains(track)) {
            // notify listeners; listeners might have to check what has changed
            notifyListeners(new TimeSeriesChangeEvent(track,
                    TimeSeriesChangeEvent.CHANGE, TimeSeriesChangeEvent.TRACK));
        }
    }

    /**
     * Generic request to send a notification to listeners
     *
     * @param type ADD, CHANGE, DELETE
     */
    public void tsSourceChanged(int type) {
        notifyListeners(new TimeSeriesChangeEvent(this, type,
                TimeSeriesChangeEvent.TS_SOURCE));
        encoder.encodeAndSave(transcription, trackSourceConfigs.values());
    }

    /**
     * Adds a timeseries source configuration object to the manager. Any track
     * objects found through this source configuration will be added to the
     * list of managed tracks.
     *
     * @param configuration a configuration object associated with a single
     *        source (file)
     * @param notifyAndSave if true, the configuration is new and listeners need to be notified
     * and the setup saved
     */
    public void addTrackSource(TSSourceConfiguration configuration, boolean notifyAndSave) {
        if (configuration != null) {
            trackSourceConfigs.put(configuration.getSource(), configuration);

            // add tracks to the list
            Iterator keyIt = configuration.objectKeySet().iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                Object o = configuration.getObject(key);

                if (o instanceof TimeSeriesTrack) {
                    tracks.add((TimeSeriesTrack) o);
                } else if (o instanceof TSTrackConfiguration) { // default

                    TSTrackConfiguration tstc = (TSTrackConfiguration) o;
                    Object tr = tstc.getObject(tstc.getTrackName());

                    if (tr instanceof TimeSeriesTrack) {
                        tracks.add((TimeSeriesTrack) tr); // default situation
                    } else {
                        Iterator objIt = tstc.objectKeySet().iterator();

                        while (objIt.hasNext()) {
                            Object ke = objIt.next();
                            Object oo = tstc.getObject(ke);

                            if (oo instanceof TimeSeriesTrack) {
                                tracks.add((TimeSeriesTrack) oo);
                            }
                        }
                    }
                }
            }
            /*
            if (syncPlayer == null) {     	
            	TimeSeriesViewer tsViewer = ELANCommandFactory.getViewerManager(transcription)
                	.createTimeSeriesViewer();
            	//ELANCommandFactory.getViewerManager(transcription).disableViewer(tsViewer);
            	tsViewer.setTrackManager(this); 
            	syncPlayer = new TSViewerPlayer(tsViewer, 
            			ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer().getMediaDuration());
            	syncPlayer.setTrackManager(this);// adds the sources
            	ELANCommandFactory.getViewerManager(transcription).addMediaPlayer(syncPlayer);
            	ELANCommandFactory.getLayoutManager(transcription).add(syncPlayer);
            	ELANCommandFactory.getViewerManager(transcription).destroyMediaPlayer(syncPlayer);
            }
            */
            if (notifyAndSave) {
	            notifyListeners(new TimeSeriesChangeEvent(configuration,
	                    TimeSeriesChangeEvent.ADD, TimeSeriesChangeEvent.TS_SOURCE));
	            encoder.encodeAndSave(transcription,
	                trackSourceConfigs.values());
            }
        }
    }

    /**
     * Removes a source configuration from the managers map
     *
     * @param configuration a configuration object
     *
     * @see #removeTrackSource(String)
     */
    public void removeTrackSource(TSSourceConfiguration configuration) {
        if (configuration != null) {
            removeTrackSource(configuration.getSource());
        }
    }

    /**
     * Removes a timeseries source configuration object from the manager. Any
     * track objects found through this source configuration will be removed
     * from the list of managed tracks.
     *
     * @param source name of the configuration (=path)
     */
    public void removeTrackSource(String source) {
        if (source != null) {
            TSSourceConfiguration configuration = trackSourceConfigs.remove(source);

            if (configuration != null) {
                Iterator keyIt = configuration.objectKeySet().iterator();

                while (keyIt.hasNext()) {
                    Object key = keyIt.next();
                    Object o = configuration.getObject(key);

                    if (o instanceof TimeSeriesTrack) {
                        tracks.remove(o);
                    } else if (o instanceof TSTrackConfiguration) { // default

                        TSTrackConfiguration tstc = (TSTrackConfiguration) o;
                        Object tr = tstc.getObject(tstc.getTrackName());

                        if (tr instanceof TimeSeriesTrack) {
                            tracks.remove(tr); // default situation
                        } else {
                            Iterator objIt = tstc.objectKeySet().iterator();

                            while (objIt.hasNext()) {
                                Object ke = objIt.next();
                                Object oo = tstc.getObject(ke);

                                if (oo instanceof TimeSeriesTrack) {
                                    tracks.remove(oo);
                                }
                            }
                        }
                    }

                    notifyListeners(new TimeSeriesChangeEvent(o,
                            TimeSeriesChangeEvent.DELETE,
                            TimeSeriesChangeEvent.TRACK));
                }

                notifyListeners(new TimeSeriesChangeEvent(configuration,
                        TimeSeriesChangeEvent.DELETE,
                        TimeSeriesChangeEvent.TS_SOURCE));
            }

            
            if (trackSourceConfigs.isEmpty()) {
            	/*
            	if (syncPlayer != null) {
            		ELANCommandFactory.getLayoutManager(transcription).remove(syncPlayer);
            		ELANCommandFactory.getLayoutManager(transcription).remove(
            				syncPlayer.getViewer());
            		ELANCommandFactory.getViewerManager(transcription).destroyViewer(
            			syncPlayer.getViewer());
            		ELANCommandFactory.getViewerManager(transcription).destroyMediaPlayer(
            				syncPlayer);
            		syncPlayer = null;
            	}
            	*/
            }
        }

        encoder.encodeAndSave(transcription, trackSourceConfigs.values());
    }

    /**
     * Called after a change in configuration settings.
     *
     * @param configuration the changed configuration
     */
    public void trackSourceChanged(TSSourceConfiguration configuration) {
        if (configuration == null) {
            return;
        }

        // check if the config is in the list
        if (trackSourceConfigs.get(configuration.getSource()) != null) {
            // check if the time origins of the tracks in this config need to be updated
            int origin = configuration.getTimeOrigin();
            AbstractTSTrack track = null;

            Iterator keyIt = configuration.objectKeySet().iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                Object o = configuration.getObject(key);

                if (o instanceof AbstractTSTrack) {
                    track = (AbstractTSTrack) o;

                    if (track.getTimeOffset() != origin) {
                        track.setTimeOffset(origin);
                        notifyListeners(new TimeSeriesChangeEvent(track,
                                TimeSeriesChangeEvent.CHANGE,
                                TimeSeriesChangeEvent.TRACK));
                    }
                } else if (o instanceof TSTrackConfiguration) { // default

                    TSTrackConfiguration tstc = (TSTrackConfiguration) o;
                    Object tr = tstc.getObject(tstc.getTrackName());

                    if (tr instanceof AbstractTSTrack) {
                        track = (AbstractTSTrack) tr;

                        if (track.getTimeOffset() != origin) {
                            track.setTimeOffset(origin);
                            notifyListeners(new TimeSeriesChangeEvent(track,
                                    TimeSeriesChangeEvent.CHANGE,
                                    TimeSeriesChangeEvent.TRACK));
                        }
                    } else {
                        Iterator objIt = tstc.objectKeySet().iterator();

                        while (objIt.hasNext()) {
                            Object ke = objIt.next();
                            Object oo = tstc.getObject(ke);

                            if (oo instanceof AbstractTSTrack) {
                                track = (AbstractTSTrack) oo;

                                if (track.getTimeOffset() != origin) {
                                    track.setTimeOffset(origin);
                                    notifyListeners(new TimeSeriesChangeEvent(
                                            track,
                                            TimeSeriesChangeEvent.CHANGE,
                                            TimeSeriesChangeEvent.TRACK));
                                }
                            }
                        }
                    }
                }
            }

            notifyListeners(new TimeSeriesChangeEvent(configuration,
                    TimeSeriesChangeEvent.CHANGE,
                    TimeSeriesChangeEvent.TS_SOURCE));
            encoder.encodeAndSave(transcription, trackSourceConfigs.values());
        }
    }

    /**
     * Adds a listener to the list of time series change listeners.
     *
     * @param li the listener
     */
    public void addTimeSeriesChangeListener(TimeSeriesChangeListener li) {
        if (listeners == null) {
            listeners = new ArrayList<TimeSeriesChangeListener>();
        }

        listeners.add(li);
    }

    /**
     * Removes the listeners from the list
     *
     * @param li the listener
     */
    public void removeTimeSeriesChangeListener(TimeSeriesChangeListener li) {
        if (listeners != null) {
            listeners.remove(li);
        }
    }

    /**
     * Dispatches the event to all interested in time series change events
     *
     * @param event the time series change event
     */
    private void notifyListeners(TimeSeriesChangeEvent event) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).timeSeriesChanged(event);
            }
        }
    }

    /**
     * Dispatches events to the registered listeners.
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void timeSeriesChanged(TimeSeriesChangeEvent event) {
        if (event.getEditSourceType() == TimeSeriesChangeEvent.TRACK) {
            if (event.getSource() instanceof TSTrackConfiguration) {
                TSTrackConfiguration trc = (TSTrackConfiguration) event.getSource();
                Object tr = trc.getObject(trc.getTrackName());

                if (event.getEditType() == TimeSeriesChangeEvent.ADD) {
                    if (!tracks.contains(tr)) {
                        tracks.add((TimeSeriesTrack) tr);
                    }
                } else if (event.getEditType() == TimeSeriesChangeEvent.DELETE) {
                    tracks.remove(tr);
                }
            }
        }

        notifyListeners(event);
        encoder.encodeAndSave(transcription, trackSourceConfigs.values());
    }
    
    /**
     * Sets the changed flag. 
     * 
     * @param changed if true there are changes that are not stored yet, else the changed ("dirty") state is cleared
     */
    public void setChanged(boolean changed) {
    	this.changed = changed;
    }
    
    /**
     * Returns the current changed state.
     * 
     * @return the current changed flag
     */
    public boolean isChanged() {
    	return changed;
    }
    
    /**
     * Saves changes that are not stored yet.
     */
    public void saveIfChanged() {
    	if (changed) {
    		encoder.encodeAndSave(transcription, trackSourceConfigs.values());
    		changed = false;
    	}
    }

}
