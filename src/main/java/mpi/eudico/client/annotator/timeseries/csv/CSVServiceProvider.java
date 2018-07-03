package mpi.eudico.client.annotator.timeseries.csv;

import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.NonContinuousRateTSTrack;
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
 * A TimeSeries Service Provider providing support for .csv / tab-delimited
 * text files.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class CSVServiceProvider implements ClientLogger, TSServiceProvider {
    private final String[] suffices = new String[] { "txt", "csv" };

    // a source file -> reader map; keep one reader per source
    private HashMap sourceToReaderMap;

    /**
     * Creates a new CSVServiceProvider instance
     */
    public CSVServiceProvider() {
        super();
        sourceToReaderMap = new HashMap();
    }

    /**
     * Returns whether this provider can handle the file specified.
     *
     * @param filePath the path to the data file
     *
     * @return whether this provider can handle the specified file
     */
    @Override
	public boolean canHandle(String filePath) {
        LOG.info("Polling: " + filePath);

        if ((filePath == null) || (filePath.length() < 5)) {
            return false;
        }

        if (sourceToReaderMap.get(filePath) != null) {
            return true;
        }

        String lowerPath = filePath.toLowerCase();

        for (int i = 0; i < suffices.length; i++) {
            if (lowerPath.endsWith(suffices[i])) {
                try {
                    CSVReader reader = new CSVReader(filePath);

                    if (reader.isValidFile()) {
                        sourceToReaderMap.put(filePath, reader);

                        return true;
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Returns true. This provider supports configuration of tracks by
     * supplying a  configuration panel.
     *
     * @return true tracks can and must be configured by the user through a
     *         configuration panel
     */
    @Override
	public boolean isConfigurable() {
        return true;
    }

    /**
     * Returns the cinfiguration panel.
     *
     * @param config the configuration object
     *
     * @return the configuration panel
     */
    @Override
	public TSConfigPanel getConfigPanel(TSSourceConfiguration config) {
        CSVConfigPanel configPanel = new CSVConfigPanel();
        configPanel.setSourceConfiguration(config);

        if (sourceToReaderMap.get(config.getSource()) != null) {
            configPanel.setReader((CSVReader) sourceToReaderMap.get(
                    config.getSource()));
        } else {
            CSVReader reader = new CSVReader(config.getSource());
            // configure?
            sourceToReaderMap.put(config.getSource(), reader);
            configPanel.setReader(reader);
        }

        return configPanel;
    }

    /**
     * Empty method, tracks are not automatically created.
     *
     * @param config the source configuration object
     */
    @Override
	public void autoCreateTracks(TSSourceConfiguration config) {
        // no tracks are created without configuration
    }

    /**
     * Creates tracks previously configured and stored.
     *
     * @param config the source configuration object
     */
    @Override
	public void createTracksFromConfiguration(TSSourceConfiguration config) {
        if (config == null) {
            LOG.warning("The configuration object is null.");

            return;
        }

        CSVReader reader = null;

        if (sourceToReaderMap.containsKey(config.getSource())) {
            reader = (CSVReader) sourceToReaderMap.get(config.getSource());
        } else {
            reader = new CSVReader(config.getSource());
            sourceToReaderMap.put(config.getSource(), reader);
        }

        boolean continRate = false;

        if (config.getSampleType() != null) {
            continRate = TimeSeriesConstants.CONTINUOUS_RATE.equals(config.getSampleType());
        }

        int timeColumn = config.getTimeColumn();

        if (timeColumn < 0) {
            LOG.warning(
                "Could not restore tracks: the time column has not been specified.");

            return;
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

            SamplePosition spos = trconf.getSamplePos();
            int dataCol = spos.getColumns()[0];
            int derLevel = parseInt(trconf.getProperty(
                        TimeSeriesConstants.DERIVATION));
            AbstractTSTrack track = null;

            try {
                if (continRate) {
                	ContinuousRateTSTrack t = new ContinuousRateTSTrack();
                    t.setData(reader.readContinuousRateTrack(timeColumn, dataCol,
                            derLevel));
                    track = t;
                    track.setSampleRate(reader.getSampleFrequency());
                    track.setType(TimeSeriesTrack.VALUES_FLOAT_ARRAY);
                } else {
                	NonContinuousRateTSTrack t = new NonContinuousRateTSTrack();
                    t.setData(reader.readNonContinuousRateTrack(timeColumn, dataCol,
                            derLevel));
                    track = t;
                    track.setType(TimeSeriesTrack.TIME_VALUE_LIST);
                }
            } catch (IOException ioe) {
                LOG.severe("Could not read track: " + trconf.getTrackName() +
                    " from: " + config.getSource() + ": " + ioe.getMessage());

                continue;
            } catch (Throwable th) {
                LOG.severe("Could not read track: " + trconf.getTrackName() +
                        " from: " + config.getSource() + ": " + th.getMessage());

                    continue;
            }

            track.setName(trconf.getTrackName());
            track.setDerivativeLevel(derLevel);
            track.setTimeOffset(offset);
            track.setDescription(trconf.getProperty(TimeSeriesConstants.DESC));
            track.setUnitString(trconf.getProperty(TimeSeriesConstants.UNITS));

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
