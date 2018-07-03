package mpi.eudico.client.annotator.timeseries.io;

import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.config.SamplePosition;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Encoder class for timeseries configuration xml.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TSConfigurationEncoder implements ClientLogger {
    protected final String VERSION = "1.0";
    protected String configFile;
    protected DocumentBuilder db;
    protected Document doc;

    /**
     * Creates a new TSConfigurationEncoder instance
     */
    public TSConfigurationEncoder() {
    }

    /**
     * Creates a DOM tree and saves the file.
     *
     * @param transcription the Transcription, used for creation of file path
     * @param tsConfigs a collection of configuration objects of all linked
     *        timeseries sources
     */
    public void encodeAndSave(TranscriptionImpl transcription,
        Collection tsConfigs) {
        // create a path and / or check the path for config file
        if ((transcription == null) ||
                transcription.getPathName()
                                 .equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
            return;
        }

        String existingConf = getExistingConfFile(transcription);

        if (existingConf == null) {
            configFile = createPath(transcription.getFullPath());
        } else {
            configFile = existingConf;
        }

        /*
           if (transcription.getLinkedFileDescriptors().size() == 0) {
                // delete the config file??
               if (FileUtility.fileExists(configFile)) {
        
               }
           }
         */
        doc = createNewDocument();

        if (doc != null) {
            Element docElement = createDOM(tsConfigs);

            try {
                IoUtil.writeEncodedFile("UTF-8", configFile.substring(5),
                    docElement);
                LOG.info("Configuration file saved: " + configFile);

                // optionally add config file as a linked file to the transcription
                LinkedFileDescriptor lfd;

                for (int i = 0;
                        i < transcription.getLinkedFileDescriptors().size();
                        i++) {
                    lfd = (LinkedFileDescriptor) transcription.getLinkedFileDescriptors()
                                                              .get(i);

                    //if (lfd.linkURL.equals(configFile)) {
                    if (lfd.linkURL.toLowerCase().endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
                        return;
                    }
                }

                // create and add a new descriptor if we get here
                lfd = new LinkedFileDescriptor(configFile,
                        LinkedFileDescriptor.XML_TYPE);
                transcription.getLinkedFileDescriptors().add(lfd);
                transcription.setChanged();
            } catch (Exception e) {
                LOG.warning("Could not save configuration file: " +
                    e.getMessage());
            }
        }
    }   

    private String getExistingConfFile(TranscriptionImpl transcription) {
        LinkedFileDescriptor lfd;

        for (int i = 0; i < transcription.getLinkedFileDescriptors().size();
                i++) {
            lfd = transcription.getLinkedFileDescriptors().get(i);

            if (lfd.linkURL.endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
            	if (new File(lfd.linkURL).exists()) {
            		return lfd.linkURL;
            	}
            }
        }

        return null;
    }

    /**
     * Create a fileName for the configuration file.
     *
     * @param eafPath the eaf path
     *
     * @return the config path
     */
    protected String createPath(String eafPath) {
        String cp = eafPath;

        if (cp.startsWith("file:")) {
            cp = cp.substring(5);
        }

        cp = FileUtility.pathToURLString(cp);

        if (cp.endsWith(".eaf")) {
            cp = cp.substring(0, cp.length() - 4);
        }

        cp = cp + TimeSeriesConstants.CONF_SUFFIX;
        System.out.println("config path: " + cp);

        return cp;
    }

    protected Document createNewDocument() {
        if (db == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                LOG.severe("Cannot create document builder: " +
                    pce.getMessage());

                return null;
            }
        }

        return db.newDocument();
    }

    protected Element createDOM(Collection configs) {
        Element rootElem = createDocElement();
        doc.appendChild(rootElem);

        Iterator cit = configs.iterator();

        while (cit.hasNext()) {
            Object sc = cit.next();

            if (sc instanceof TSSourceConfiguration) {
                rootElem.appendChild(createTrackSourceElement(
                        (TSSourceConfiguration) sc));
            }
        }

        return rootElem;
    }

    protected Element createDocElement() {
        Element root = doc.createElement(TimeSeriesConstants.TIMESERIES);
        root.setAttribute(TimeSeriesConstants.DATE, getDate());
        root.setAttribute(TimeSeriesConstants.VERS, VERSION);

        return root;
    }

    protected Element createTrackSourceElement(TSSourceConfiguration tsc) {
        if ((tsc == null) || (tsc.getSource() == null)) {
            return null;
        }

        Element sourceElem = doc.createElement(TimeSeriesConstants.SOURCE);
        sourceElem.setAttribute(TimeSeriesConstants.URL, tsc.getSource());

        if (tsc.getTimeOrigin() != 0) {
            sourceElem.setAttribute(TimeSeriesConstants.ORIGIN,
                String.valueOf(tsc.getTimeOrigin()));
        }

        if (tsc.getSampleType() != null) {
            sourceElem.setAttribute(TimeSeriesConstants.SAMPLE_TYPE,
                tsc.getSampleType());
        }

        if (tsc.getProviderClassName() != null) {
            sourceElem.appendChild(createPropertyElement(
                    TimeSeriesConstants.PROVIDER, tsc.getProviderClassName()));
        }

        if (tsc.getTimeColumn() > -1) {
            sourceElem.setAttribute(TimeSeriesConstants.TIME_COLUMN,
                String.valueOf(tsc.getTimeColumn()));
        }

        Enumeration propEnum = tsc.propertyNames();
        String prop;
        String val;
        Element propElem;

        while (propEnum.hasMoreElements()) {
            prop = (String) propEnum.nextElement();
            val = tsc.getProperty(prop);
            propElem = createPropertyElement(prop, val);

            if (propElem != null) {
                sourceElem.appendChild(propElem);
            }
        }

        Iterator objIt = tsc.objectKeySet().iterator();
        Object key;
        Object oval;
        Element trackElem;

        while (objIt.hasNext()) {
            key = objIt.next();
            oval = tsc.getObject(key);

            if (oval instanceof TSTrackConfiguration) {
                trackElem = createTrackElement((TSTrackConfiguration) oval);

                if (trackElem != null) {
                    sourceElem.appendChild(trackElem);
                }
            }
        }

        return sourceElem;
    }

    protected Element createPropertyElement(String key, String value) {
        if ((key == null) || (value == null)) {
            return null;
        }

        Element propElement = doc.createElement(TimeSeriesConstants.PROP);
        propElement.setAttribute(TimeSeriesConstants.KEY, key);
        propElement.setAttribute(TimeSeriesConstants.VALUE, value);

        return propElement;
    }

    protected Element createTrackElement(TSTrackConfiguration trConfig) {
        if ((trConfig == null) || (trConfig.getTrackName() == null)) {
            return null;
        }

        AbstractTSTrack track;
        Element trackElem = doc.createElement(TimeSeriesConstants.TRACK);
        trackElem.setAttribute(TimeSeriesConstants.NAME, trConfig.getTrackName());

        // properties
        Enumeration propEnum = trConfig.propertyNames();
        String prop;
        String val;
        Element propElem;

        while (propEnum.hasMoreElements()) {
            prop = (String) propEnum.nextElement();
            val = trConfig.getProperty(prop);
            propElem = createPropertyElement(prop, val);

            if (propElem != null) {
                trackElem.appendChild(propElem);
            }
        }

        SamplePosition spos = trConfig.getSamplePos();

        if (spos != null) {
            Element spElem = doc.createElement(TimeSeriesConstants.POSITION);

            if (spos.getDescription() != null) {
                Element ds = doc.createElement(TimeSeriesConstants.DESC);
                ds.appendChild(doc.createTextNode(spos.getDescription()));
                spElem.appendChild(ds);
            }

            for (int i = 0; i < spos.getRows().length; i++) {
                Element pos = doc.createElement(TimeSeriesConstants.SAMPLE_POS);
                pos.setAttribute(TimeSeriesConstants.ROW,
                    String.valueOf(spos.getRows()[i]));
                pos.setAttribute(TimeSeriesConstants.COL,
                    String.valueOf(spos.getColumns()[i]));
                spElem.appendChild(pos);
            }

            trackElem.appendChild(spElem);
        }

        Object obj = trConfig.getObject(trConfig.getTrackName());

        if (obj instanceof AbstractTSTrack) {
            track = (AbstractTSTrack) obj;
            trackElem.setAttribute(TimeSeriesConstants.DERIVATION,
                String.valueOf(track.getDerivativeLevel()));

            if (track.getDescription() != null) {
                Element descElem = doc.createElement(TimeSeriesConstants.DESC);
                descElem.appendChild(doc.createTextNode(track.getDescription()));
                trackElem.appendChild(descElem);
            }

            if (track.getUnitString() != null) {
                Element unitElem = doc.createElement(TimeSeriesConstants.UNITS);
                unitElem.appendChild(doc.createTextNode(track.getUnitString()));
                trackElem.appendChild(unitElem);
            }

            if (track.getRange() != null) {
                Element rangeElem = doc.createElement(TimeSeriesConstants.RANGE);
                rangeElem.setAttribute(TimeSeriesConstants.MIN,
                    String.valueOf(track.getRange()[0]));
                rangeElem.setAttribute(TimeSeriesConstants.MAX,
                    String.valueOf(track.getRange()[1]));
                trackElem.appendChild(rangeElem);
            }

            Element colElem = doc.createElement(TimeSeriesConstants.COLOR);
            Color c = track.getColor();
            String cs = c.getRed() + "," + c.getGreen() + "," + c.getBlue();
            colElem.appendChild(doc.createTextNode(cs));
            trackElem.appendChild(colElem);
        }

        return trackElem;
    }

    private String getDate() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        StringBuilder buf = new StringBuilder(dateFmt.format(
                    Calendar.getInstance().getTime()));
        int offsetGMT = Calendar.getInstance().getTimeZone().getRawOffset() / (60 * 60 * 1000);

        if (offsetGMT > 0) {
            buf.append('+');

            if (offsetGMT < 10) {
                buf.append('0');
            }
        } else {
            buf.append('-');

            if (offsetGMT > -10) {
                buf.append('0');
            }
        }

        buf.append(Math.abs(offsetGMT));
        buf.append(":00");

        int firstSpace = buf.indexOf(" ");

        if (firstSpace > -1) {
            buf.setCharAt(firstSpace, 'T');
        }

        return buf.toString().replace('.', '-');
    }
}
