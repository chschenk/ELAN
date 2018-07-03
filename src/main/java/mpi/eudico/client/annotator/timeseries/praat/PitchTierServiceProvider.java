package mpi.eudico.client.annotator.timeseries.praat;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.client.annotator.timeseries.NonContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.timeseries.spi.TSConfigPanel;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider;
import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * A service provider for Praat PitchTier files and currently also for
 * IntensityTier files.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class PitchTierServiceProvider implements TSServiceProvider {
    //private final String suffix = new String("PitchTier");

    // a source file -> reader map; keep one reader per source
    private HashMap<String, PitchTierFileReader> sourceToReaderMap;
    private int trackCount = 0;
    private final Color color = new Color(0, 160, 0);

    /**
     * Creates a new PitchTierServiceProvider instance
     */
    public PitchTierServiceProvider() {
        super();
        sourceToReaderMap = new HashMap<String, PitchTierFileReader>();
    }

    /**
     * Creates tracks without user configuration.
     *
     * @param config a configuration object containing information on the
     *        source
     */
    @Override
	public void autoCreateTracks(TSSourceConfiguration config) {
    	 if (config == null) {
             return;
         }
         
         PitchTierFileReader ptfr = sourceToReaderMap.get(config.getSource());

         if (ptfr == null) {
             ptfr = new PitchTierFileReader(config.getSource());
             sourceToReaderMap.put(config.getSource(), ptfr);
         }           

         Iterator trIt = config.objectKeySet().iterator();
         Object key;
         Object val;
         TSTrackConfiguration trconf;
         int offset = config.getTimeOrigin();
         
         NonContinuousRateTSTrack track;

         while (trIt.hasNext()) {
             key = trIt.next();
             val = config.getObject(key);

             if (!(val instanceof TSTrackConfiguration)) {
                 continue;
             }

             trconf = (TSTrackConfiguration) val;

             // most information has, temporarely, been stored as properties
             NonContinuousRateTSTrack ncrt = new NonContinuousRateTSTrack();
             track = ncrt;
             String trackName = trconf.getTrackName();
             track.setName(trackName);
             track.setTimeOffset(offset);

             try {
                 // for the time being only single sample tracks
                 ncrt.setData(ptfr.readTrack());
                 track.setSampleRate(ptfr.getSampleFrequency());
             } catch (IOException ioe) {
                 ClientLogger.LOG.severe("Could not read track: " +
                     trconf.getTrackName() + " from: " + config.getSource());
             }

             track.setDescription(trconf.getProperty(TimeSeriesConstants.DESC));
             track.setUnitString(trconf.getProperty(TimeSeriesConstants.UNITS));
             track.setType(TimeSeriesTrack.TIME_VALUE_LIST);

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
             
             int trackNo = parseInt(trackName.substring(trackName.lastIndexOf("-")+1));
             if(trackNo >= trackCount){
            	 trackCount = trackNo;
            	 trackCount++;
             }
             return;
         }
    	
         
         // if there is not track configuration then    	
         NonContinuousRateTSTrack ncrt = new NonContinuousRateTSTrack();
         track = ncrt;
         //track.setName("PitchTrack-" + trackCount++);
         track.setColor(color); // generate a unique color, like in MultiTierControlPanel?

         try {
        	 List<TimeValue> data = ptfr.readTrack();
            
        	 if (data != null) {
                  try {
                        ncrt.setData(data);
                    } catch (IllegalArgumentException iae) {
                    }
                }

                track.setName(ptfr.getTrackName() + "-" + trackCount++);
                track.setSampleRate(ptfr.getSampleFrequency());
                track.setRange(new float[] { ptfr.getMin(), ptfr.getMax() });
                track.setType(TimeSeriesTrack.TIME_VALUE_LIST);
            } catch (IOException ioe) {
                ClientLogger.LOG.severe(
                    "Could not read data from the timeseries file " +
                    config.getSource());
            }

            // create a track config object
            trconf = new TSTrackConfiguration(track.getName(),
                    track);
            config.putObject(track.getName(), trconf);
    }

    /**
     * Checks the specified file and determines if it is a valid PitchTier file
     *
     * @param filePath path to the file
     *
     * @return true if the file is valid
     */
    @Override
	public boolean canHandle(String filePath) {
        ClientLogger.LOG.info("Polling: " + filePath);

        if ((filePath == null) || (filePath.length() < 5)) {
            return false;
        }

        if (sourceToReaderMap.get(filePath) != null) {
            return true;
        }

        String lowerPath = filePath.toLowerCase();

        if (lowerPath.endsWith("pitchtier") ||
                lowerPath.endsWith("intensitytier")) {
            // create a reader
            try {
                PitchTierFileReader ptfr = new PitchTierFileReader(filePath);

                // test validity
                if (ptfr.isValidFile()) {
                    sourceToReaderMap.put(filePath, ptfr);

                    return true;
                }
            } catch (Exception ex) {
                return false;
            }

            return false;
        }

        return false;
    }

    /**
     * Creates tracks from a stored configuration.
     *
     * @param config the source configuration
     */
    @Override
	public void createTracksFromConfiguration(TSSourceConfiguration config) {
        if (config == null) {
            return;
        }

        PitchTierFileReader reader = null;

        if (sourceToReaderMap.containsKey(config.getSource())) {
            reader = sourceToReaderMap.get(config.getSource());
        } else {
            reader = new PitchTierFileReader(config.getSource());

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
            NonContinuousRateTSTrack track = new NonContinuousRateTSTrack();
            track.setName(trconf.getTrackName());
            track.setTimeOffset(offset);

            try {
                // for the time being only single sample tracks
                track.setData(reader.readTrack());
                track.setSampleRate(reader.getSampleFrequency());
            } catch (IOException ioe) {
                ClientLogger.LOG.severe("Could not read track: " +
                    trconf.getTrackName() + " from: " + config.getSource());
            }

            track.setDescription(trconf.getProperty(TimeSeriesConstants.DESC));
            track.setUnitString(trconf.getProperty(TimeSeriesConstants.UNITS));
            track.setType(TimeSeriesTrack.TIME_VALUE_LIST);

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
     * Returns null.
     *
     * @param config the source configuration
     *
     * @return null, no panel
     */
    @Override
	public TSConfigPanel getConfigPanel(TSSourceConfiguration config) {
        return null;
    }

    /**
     * Returns false.
     *
     * @return false, configuration is not supported
     */
    @Override
	public boolean isConfigurable() {
        return false;
    }

    /**
     * Creates a Color object from a comma separated string. Should move to a
     * utility class.
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
