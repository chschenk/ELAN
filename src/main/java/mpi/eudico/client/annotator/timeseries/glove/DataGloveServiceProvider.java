package mpi.eudico.client.annotator.timeseries.glove;

import mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.config.SamplePosition;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.timeseries.spi.TSConfigPanel;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider;

import mpi.eudico.client.annotator.util.ClientLogger;

import java.awt.Color;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Implementation of the timeseries Service Provider Interface. Provides
 * support for the proprietary cyberglove log files.
 *
 * @author Han Sloetjes
 */
public class DataGloveServiceProvider implements TSServiceProvider,
    ClientLogger {
    private final static String suffix = "log";

    // a source file -> reader map; keep one reader per source
    private HashMap sourceToReaderMap;

    /**
     * Constructor
     */
    public DataGloveServiceProvider() {
        super();
        sourceToReaderMap = new HashMap();
    }

    /**
     * Checks the file extension (.log), protocol(?), and reads a few lines
     * from the  file (header comment) to determine if the file is an mpi
     * dataglove file.
     *
     * @see mpi.eudico.server.timeseries.spi.TSServiceProvider#canHandle(java.lang.String)
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

        DataGloveFileReader reader = new DataGloveFileReader(filePath);

        if (reader.isValidFile()) {
            try {
                reader.detectSampleFrequency();
            } catch (IOException ioe) {
                // could/should be a reason to return false??
                LOG.severe("Could not detect sample frequency.");
            }

            sourceToReaderMap.put(filePath, reader);
        } else {
            LOG.info("Not a valid data glove file: " + filePath);
        }

        return reader.isValidFile();
    }

    /**
     * Returns true. User interaction is needed for the selection and
     * configuration of tracks.
     *
     * @see mpi.eudico.server.timeseries.spi.TSServiceProvider#isConfigurable()
     */
    @Override
	public boolean isConfigurable() {
        return true;
    }

    /**
     * Return the configuration panel.
     *
     * @see mpi.eudico.server.timeseries.spi.TSServiceProvider#getConfigPanel(mpi.eudico.server.timeseries.TSSourceConfiguration)
     */
    @Override
	public TSConfigPanel getConfigPanel(TSSourceConfiguration config) {
        DataGloveConfigPanel panel = new DataGloveConfigPanel();
        panel.setSourceConfiguration(config);

        if (sourceToReaderMap.get(config.getSource()) != null) {
            panel.setReader((DataGloveFileReader) sourceToReaderMap.get(
                    config.getSource()));
        } else {
            DataGloveFileReader dr = new DataGloveFileReader(config.getSource());

            try {
                dr.detectSampleFrequency();
            } catch (IOException ioe) {
                LOG.severe("Could not detect sample frequency.");
            }

            sourceToReaderMap.put(config.getSource(), dr);
            panel.setReader(dr);
        }

        return panel;
    }

    /**
     * @see mpi.eudico.server.timeseries.spi.TSServiceProvider#createTracks(mpi.eudico.server.timeseries.TSSourceConfiguration)
     */
    @Override
	public void autoCreateTracks(TSSourceConfiguration config) {
        // don't create tracks without configuration
    }

    /**
     * @see mpi.eudico.server.timeseries.spi.TSServiceProvider#createTracksFromConfiguration(mpi.eudico.server.timeseries.TSSourceConfiguration)
     */
    @Override
	public void createTracksFromConfiguration(TSSourceConfiguration config) {
        if (config == null) {
            return;
        }

        DataGloveFileReader reader = null;

        if (sourceToReaderMap.containsKey(config.getSource())) {
            reader = (DataGloveFileReader) sourceToReaderMap.get(config.getSource());
        } else {
            reader = new DataGloveFileReader(config.getSource());

            try {
                reader.detectSampleFrequency();
            } catch (IOException ioe) {
                LOG.severe("Could not detect sample frequency.");
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
            
            SamplePosition spos = trconf.getSamplePos();
            int derLevel = 0;

            try {
                derLevel = Integer.parseInt(trconf.getProperty(
                            TimeSeriesConstants.DERIVATION));
            } catch (NumberFormatException nfe) {
            }

            try {
                // for the time being only single sample tracks
                track.setData(reader.readTrack(spos.getRows()[0],
                        spos.getColumns()[0], derLevel));
                track.setSampleRate(reader.getSampleFrequency());
                track.setDerivativeLevel(derLevel);
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
        }
    }

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
