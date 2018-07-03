package mpi.eudico.client.annotator.timeseries.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.config.SamplePosition;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.util.ClientLogger;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 * A parser for timeseries configuration files.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TSConfigurationParser implements ClientLogger {
    /** the SAx parser */
    private final SAXParser saxParser;

    /**
     * Creates a new TSConfigurationParser instance
     */
    public TSConfigurationParser() {
        saxParser = new SAXParser();

        try {
            saxParser.setFeature("http://xml.org/sax/features/validation", false);
            //saxParser.setFeature("http://apache.org/xml/features/validation/dynamic",
            //    true);
            saxParser.setContentHandler(new ConfigHandler());
        } catch (SAXNotRecognizedException e) {
            LOG.warning("Parser error: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXNotSupportedException e) {
            LOG.warning("Parser error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of source configuration objects extracted from the
     * specified configuration file.
     *
     * @param filePath the path to the file
     *
     * @return list of source configuration objects
     */
    public List<TSSourceConfiguration> parseSourceConfigs(String filePath) {
        try {
            saxParser.parse(filePath);

            return ((ConfigHandler) saxParser.getContentHandler()).getConfigurations();
        } catch (SAXException se) {
            LOG.warning("Could not parse file: " + filePath + ": " +
                se.getMessage());

            return null;
        } catch (IOException ioe) {
            LOG.warning("Could not parse file: " + filePath + ": " +
                ioe.getMessage());

            return null;
        }
    }

    /**
     * The content handler for the configration xml files.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    private class ConfigHandler implements ContentHandler {
        private String content = "";
        private String curValue;
        private List<TSSourceConfiguration> configs = new ArrayList<TSSourceConfiguration>();
        private Object currentElement;
        private TSSourceConfiguration sourceConf;
        private TSTrackConfiguration trackConf;
        private SamplePosition spos;
        private List<int[]> positions = new ArrayList<int[]>();

        /**
         * Returns a list of source configuration objects
         *
         * @return a list of source configuration objects
         */
        public List<TSSourceConfiguration> getConfigurations() {
            return configs;
        }

        /**
         * End of document.
         *
         * @throws SAXException
         */
        @Override
		public void endDocument() throws SAXException {

        }

        /**
         * Start of document
         *
         * @throws SAXException
         */
        @Override
		public void startDocument() throws SAXException {

        }

        /**
         * Content characters.
         *
         * @param ch chars
         * @param start index
         * @param length
         *
         * @throws SAXException
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            content += new String(ch, start, length);
        }

        /**
         * Ignored
         *
         * @param ch
         * @param start
         * @param length
         *
         * @throws SAXException
         */
        @Override
		public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        }

        /**
         * Ignored
         *
         * @param prefix
         *
         * @throws SAXException
         */
        @Override
		public void endPrefixMapping(String prefix) throws SAXException {
        }

        /**
         * Ignored
         *
         * @param name
         *
         * @throws SAXException
         */
        @Override
		public void skippedEntity(String name) throws SAXException {
        }

        /**
         * Ignored
         *
         * @param locator
         */
        @Override
		public void setDocumentLocator(Locator locator) {
        }

        /**
         * Ignored
         *
         * @param target
         * @param data
         *
         * @throws SAXException
         */
        @Override
		public void processingInstruction(String target, String data)
            throws SAXException {
        }

        /**
         * Ignored
         *
         * @param prefix
         * @param uri
         *
         * @throws SAXException
         */
        @Override
		public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        }

        /**
         * Finish the current element.
         *
         * @param namespaceURI ignored
         * @param name element name
         * @param qName ignored
         *
         * @throws SAXException
         */
        @Override
		public void endElement(String namespaceURI, String name, String qName)
            throws SAXException {
            if (name.equals(TimeSeriesConstants.DESC)) {
                if (currentElement == spos) {
                    spos.setDescription(content);
                } else if (currentElement == trackConf) {
                    trackConf.setProperty(TimeSeriesConstants.DESC, content);
                }
            } else if (name.equals(TimeSeriesConstants.POSITION)) {
                // add rows and cols
                int[] rows = new int[positions.size()];
                int[] cols = new int[positions.size()];

                for (int i = 0; i < positions.size(); i++) {
                    int[] ps = positions.get(i);
                    rows[i] = ps[0];
                    cols[i] = ps[1];
                }

                SamplePosition nsp = new SamplePosition(rows, cols);
                nsp.setDescription(spos.getDescription());
                trackConf.setSamplePos(nsp);
                positions.clear();
                currentElement = trackConf;
            } else if (name.equals(TimeSeriesConstants.UNITS)) {
                trackConf.setProperty(TimeSeriesConstants.UNITS, content);
            } else if (name.equals(TimeSeriesConstants.COLOR)) {
                trackConf.setProperty(TimeSeriesConstants.COLOR, content);
            }
        }

        /**
         * Start of an element.
         *
         * @param namespaceURI ignored
         * @param name the name of the element
         * @param qName ignored
         * @param atts the element's attributes
         *
         * @throws SAXException
         */
        @Override
		public void startElement(String namespaceURI, String name,
            String qName, Attributes atts) throws SAXException {
            // reset
            content = "";

            if (name.equals(TimeSeriesConstants.SOURCE)) {
                curValue = atts.getValue(TimeSeriesConstants.URL);

                if (curValue != null) {
                    sourceConf = new TSSourceConfiguration(curValue);
                    configs.add(sourceConf);
                    currentElement = sourceConf;
                    curValue = atts.getValue(TimeSeriesConstants.ORIGIN);

                    if (curValue != null) {
                        sourceConf.setTimeOrigin(parseInt(curValue));
                    }

                    curValue = atts.getValue(TimeSeriesConstants.SAMPLE_TYPE);

                    if (curValue != null) {
                        sourceConf.setSampleType(curValue);
                    }
                    
                    curValue = atts.getValue(TimeSeriesConstants.TIME_COLUMN);
                    
                    if (curValue != null) {
                    	sourceConf.setTimeColumn(parseInt(curValue, -1));
                    }
                }
            } else if (name.equals(TimeSeriesConstants.PROP)) {
                String key = atts.getValue(TimeSeriesConstants.KEY);
                curValue = atts.getValue(TimeSeriesConstants.VALUE);

                if ((key != null) && (curValue != null)) {
                    if (currentElement == sourceConf) {
                        if (key.equals(TimeSeriesConstants.PROVIDER)) {
                            sourceConf.setProviderClassName(curValue);
                        } else {
                            sourceConf.setProperty(key, curValue);
                        }
                    } else if (currentElement == trackConf) {
                        trackConf.setProperty(key, curValue);
                    }
                }
            } else if (name.equals(TimeSeriesConstants.TRACK)) {
                curValue = atts.getValue(TimeSeriesConstants.NAME);

                if (curValue != null) {
                    trackConf = new TSTrackConfiguration(curValue);
                    sourceConf.putObject(curValue, trackConf);
                    curValue = atts.getValue(TimeSeriesConstants.DERIVATION);

                    if (curValue != null) {
                        trackConf.setProperty(TimeSeriesConstants.DERIVATION,
                            curValue);
                    }
                    currentElement = trackConf;
                }
            } else if (name.equals(TimeSeriesConstants.RANGE)) {
                curValue = atts.getValue(TimeSeriesConstants.MIN);

                if (curValue != null) {
                    trackConf.setProperty(TimeSeriesConstants.MIN, curValue);
                }

                curValue = atts.getValue(TimeSeriesConstants.MAX);

                if (curValue != null) {
                    trackConf.setProperty(TimeSeriesConstants.MAX, curValue);
                }
            } else if (name.equals(TimeSeriesConstants.POSITION)) {
                spos = new SamplePosition();
                currentElement = spos;
            } else if (name.equals(TimeSeriesConstants.SAMPLE_POS)) {
                int row = parseInt(atts.getValue(TimeSeriesConstants.ROW));
                int col = parseInt(atts.getValue(TimeSeriesConstants.COL));
                positions.add(new int[] { row, col });
            }
        }

        private int parseInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        
        private int parseInt(String value, int defaultVal) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                return defaultVal;
            }
        }
    }
}
