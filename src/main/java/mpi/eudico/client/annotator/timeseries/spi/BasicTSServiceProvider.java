package mpi.eudico.client.annotator.timeseries.spi;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * Service Provider for continuous rate, time - value pairs text files.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class BasicTSServiceProvider implements TSServiceProvider, ClientLogger {
    private final static String suffix = "txt";

    // a source file -> reader map; keep one reader per source
    private Map<String, BasicTSFileReader> sourceToReaderMap;
    private int trackCount = 0;
    private final Color color = new Color(0, 160, 0);

    /**
     * Creates a new BasicTSServiceProvider instance
     */
    public BasicTSServiceProvider() {
        super();
        sourceToReaderMap = new HashMap<String, BasicTSFileReader>();
    }

    /**
     * Check the file extension and read a dozen samples.
     *
     * @see mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider#canHandle(java.lang.String)
     */
    @Override
	public boolean canHandle(String filePath) {
        LOG.info("Polling: " + filePath);

        if ((filePath == null) || (filePath.length() < 5)) {
            return false;
        }

        if (!filePath.toLowerCase().endsWith(suffix)) {
            return false;
        }

        BasicTSFileReader reader = null;

        if (sourceToReaderMap.get(filePath) != null) {
            return true;
        } else {
            reader = new BasicTSFileReader(filePath);

            if (reader.isValidFile()) {
                try {
                    reader.detectSampleFrequency();
                } catch (IOException ioe) {
                    // could/should be a reason to return false??
                    LOG.severe("Could not detect the sample frequency.");
                }

                sourceToReaderMap.put(filePath, reader);

                return true;
            } else {
                LOG.info("Not a valid basic timeseries file: " + filePath);
            }
        }

        return false;
    }

    /**
     * No configuration supported. The one track will be read, a name will be
     * produced as well as a Color.
     *
     * @see mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider#isConfigurable()
     */
    @Override
	public boolean isConfigurable() {
        return false;
    }

    /**
     * No configuration options provided (yet).
     *
     * @see mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider#getConfigPanel(mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration)
     */
    @Override
	public TSConfigPanel getConfigPanel(TSSourceConfiguration config) {
        return null;
    }

    /**
     * Create a TrackConfiguration object from the given source file and add it
     * to the  SourceConfiguration.
     *
     * @see mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider#autoCreateTracks(mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration)
     */
    @Override
	public void autoCreateTracks(TSSourceConfiguration config) {
        if (config == null) {
            return;
        }

        BasicTSFileReader reader = null;

        final String source = config.getSource();
		if (sourceToReaderMap.containsKey(source)) {
            reader = sourceToReaderMap.get(source);
        } else {
            reader = new BasicTSFileReader(source);

            try {
                reader.detectSampleFrequency();
            } catch (IOException ioe) {
                // could/should be a reason to return false??
                LOG.severe("Could not detect the sample frequency.");
            }

            sourceToReaderMap.put(source, reader);
        }

        ContinuousRateTSTrack track = new ContinuousRateTSTrack();
        track.setName("BasicTrack-" + trackCount++);
        track.setColor(color); // generate a unique color, like in MultiTierControlPanel?

        try {
        	float[] data = reader.readTrack();
        	if (data != null) {
        		try {
        			track.setData(data);
        		} catch (IllegalArgumentException iae) {
        			
        		}
        	}
            
            track.setSampleRate(reader.getSampleFrequency());
            track.setRange(new float[] { reader.getMin(), reader.getMax() });
            track.setType(TimeSeriesTrack.VALUES_FLOAT_ARRAY);
        } catch (IOException ioe) {
            LOG.severe("Could not read data from the timeseries file " +
                source);
        }

        // create a track config object
        TSTrackConfiguration trconf = new TSTrackConfiguration(track.getName(),
                track);
        config.putObject(track.getName(), trconf);
    }

    /**
     * Create the track, use data from config file.
     *
     * @see mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider#createTracksFromConfiguration(mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration)
     */
    @Override
	public void createTracksFromConfiguration(TSSourceConfiguration config) {
        if (config == null) {
            return;
        }

        BasicTSFileReader reader = null;

        if (sourceToReaderMap.containsKey(config.getSource())) {
            reader = sourceToReaderMap.get(config.getSource());
        } else {
            reader = new BasicTSFileReader(config.getSource());

            try {
                reader.detectSampleFrequency();
            } catch (IOException ioe) {
                // could/should be a reason to return false??
                LOG.severe("Could not detect the sample frequency.");
            }

            sourceToReaderMap.put(config.getSource(), reader);
        }

        Iterator trIt = config.objectKeySet().iterator();
        Object key;
        Object val;
        TSTrackConfiguration trconf;
        int offset = config.getTimeOrigin();

        while (trIt.hasNext()) {
            key = trIt.next();
            val = config.getObject(key);

            if (!(val instanceof TSTrackConfiguration)) {
                continue;
            }

            trconf = (TSTrackConfiguration) val;

            // most information has, temporarely, been stored as properties
            ContinuousRateTSTrack track = new ContinuousRateTSTrack();
            track.setName(trconf.getTrackName());
            track.setTimeOffset(offset);

            try {
                // for the time being only single sample tracks
                track.setData(reader.readTrack());
                track.setSampleRate(reader.getSampleFrequency());
            } catch (IOException ioe) {
                LOG.severe("Could not read track: " + trconf.getTrackName() +
                    " from: " + config.getSource());
            }

            track.setDescription(trconf.getProperty(TimeSeriesConstants.DESC));
            track.setUnitString(trconf.getProperty(TimeSeriesConstants.UNITS));
            track.setType(TimeSeriesTrack.VALUES_FLOAT_ARRAY);

            float min = 0f;
            float max = 100f;

            try {
                min = Float.parseFloat(trconf.getProperty(
                            TimeSeriesConstants.MIN));
            } catch (NumberFormatException nfe) {
            }

            try {
                max = Float.parseFloat(trconf.getProperty(
                            TimeSeriesConstants.MAX));
            } catch (NumberFormatException nfe) {
            }

            track.setRange(new float[] { min, max });

            Color c = parseColor(trconf.getProperty(TimeSeriesConstants.COLOR));
            track.setColor(c);

            trconf.putObject(track.getName(), track);
            //finally clean up the temporarely (load time) stored properties
            trconf.removeProperty(TimeSeriesConstants.DERIVATION);
            trconf.removeProperty(TimeSeriesConstants.DESC);
            trconf.removeProperty(TimeSeriesConstants.UNITS);
            trconf.removeProperty(TimeSeriesConstants.MIN);
            trconf.removeProperty(TimeSeriesConstants.MAX);
            trconf.removeProperty(TimeSeriesConstants.COLOR);

            break;
        }
    }

    /**
     * Creates a Color object from a comma separated string.
     *
     * @param rgb the string
     *
     * @return the color
     */
    private Color parseColor(String rgb) {
        if (rgb == null) {
            return Color.GREEN;
        }

        int r;
        int g;
        int b;
        int index = rgb.indexOf(',');
        int index2 = rgb.lastIndexOf(',');

        if ((index > -1) && (index2 > -1)) {
            r = parseInt(rgb.substring(0, index));
            g = parseInt(rgb.substring(index + 1, index2));
            b = parseInt(rgb.substring(index2 + 1));

            return new Color(r, g, b);
        } else {
            return Color.GREEN;
        }
    }

    private int parseInt(String sint) {
        try {
            return Integer.parseInt(sint);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}
