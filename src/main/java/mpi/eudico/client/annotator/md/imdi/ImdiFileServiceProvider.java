package mpi.eudico.client.annotator.md.imdi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.util.DocumentNotLoadedException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * A metadata service provider that extracts information from a local Imdi
 * file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiFileServiceProvider implements MDServiceProvider {
    private String sourcePath;
    private Map<String, String> mdValuesMap = new LinkedHashMap<String, String>();
    private ImdiDoc imdiDoc;
    private List<String> selectedKeys = new ArrayList<String>();
    private ImdiMDViewerPanel viewerPanel = null;

    /**
     * Creates a new ImdiFileServiceProvider instance
     */
    public ImdiFileServiceProvider() {
        super();
    }

    /**
     * Returns true, selection of IMDI fields is supported.
     *
     * @return true
     */
    @Override
	public boolean isConfigurable() {
        return true;
    }

    /**
     * Returns a List containing the metadata keys.
     *
     * @return the keys
     */
    @Override
	public List<String> getKeys() {
        if (imdiDoc != null) {
            return imdiDoc.getKeys();
        }

        return null;
    }

    /**
     * Sets the currently selected keys.  Retrieves the values from the ImdiDoc
     * object.
     *
     * @param selKeys the new selected keys
     */
    @Override
	public void setSelectedKeys(List<String> selKeys) {
        this.selectedKeys.clear();

        if (selKeys != null) {
            this.selectedKeys.addAll(selKeys);

            if (imdiDoc != null) {
                mdValuesMap = imdiDoc.getValuesForKeys(selectedKeys, true);
            }
        }
    }

    /**
     * Returns the current selection of IMDI keys.
     *
     * @return the list of selected keys
     */
    @Override
	public List<String> getSelectedKeys() {
        return selectedKeys;
    }

    /**
     * Returns the values for the specified keys; a single key can have
     * multiple values  (or a key can occur more than once, e.g.
     * Actors.Actor.Name).
     *
     * @param key the key
     *
     * @return the list of values.
     */
    @Override
	public List<String> getValues(String key) {
        if (imdiDoc != null) {
            return imdiDoc.getValues(key);
        }

        return null;
    }

    /**
     * Returns a description of the metadata format, in this case IMDI.
     *
     * @return a description of the format: IMDI
     */
    @Override
	public String getMDFormatDescription() {
        return "IMDI";
    }

    /**
     * Returns the value for a certain key.
     *
     * @param key the key
     *
     * @return the value or null
     */
    @Override
	public String getValue(String key) {
        String val = mdValuesMap.get(key);

        if ((val == null) && (imdiDoc != null)) {
            return imdiDoc.getValue(key);
        }

        return val;
    }

    /**
     * Returns a configuration panel showing a tree or table view of the
     * metadata keys.
     *
     * @return an imdi configuration panel
     */
    @Override
	public MDConfigurationPanel getConfigurationPanel() {
        return new ImdiConfigurationPanel(this);
    }

    /**
     * Returns a linked hashmap containing keys and values.
     *
     * @return the keys and values
     */
    @Override
	public Map<String, String> getSelectedKeysAndValues() {
        return mdValuesMap;
    }

    /**
     * Returns the root of the selected items as a tree.
     *
     * @return the root of the tree
     */
    public DefaultMutableTreeNode getSelectedAsTree() {
        if (mdValuesMap == null) {
            return null;
        }
        
        if (imdiDoc != null) {
        return imdiDoc.getTreeForKeys(selectedKeys, true);
        }

        return null;
    }

    /**
     * Returns a custom metadata viewer panel.
     *
     * @return an IMDI metadata viewer panel.
     */
    @Override
	public MDViewerComponent getMDViewerComponent() {
        if (viewerPanel == null) {
            viewerPanel = new ImdiMDViewerPanel(this);
        }

        return viewerPanel;
    }

    /**
     * Sets the path to the metadata file. If it is not an Imdi file this
     * method returns false.
     *
     * @param filePath the path to the file
     *
     * @return true if it is an Imdi file, false otherwise
     */
    @Override
	public boolean setMetadataFile(String filePath) {
        if (filePath == null) {
            return false;
        }

        if (filePath.startsWith("file:")) {
        	filePath = filePath.substring(5);	// just strip file:
        }
        
        File f = new File(filePath);

        if (!f.exists()) {
            if (filePath.indexOf("!/") > -1) {
                return setJarMetadataFile(filePath);
            }

            ClientLogger.LOG.warning("The metadata file does not exist: " +
                filePath);

            return false;
        }

        if (!f.canRead()) {
            ClientLogger.LOG.warning("The metadata file cannot be read: " +
                filePath);

            return false;
        }

        if (f.isDirectory()) {
            ClientLogger.LOG.warning("The path is a directory: " + filePath);

            return false;
        }

        if (f.length() == 0) {
            ClientLogger.LOG.warning("The metadata file has zero length: " +
                filePath);

            return false;
        }

        // read the file, check if it is an imdi session file, based on possible file extensions
        boolean extFound = false;
        String lowerPath = filePath.toLowerCase();
        
        for (String element : FileExtension.IMDI_EXT) {
        	if (lowerPath.endsWith(element)) {
        		extFound = true;
        		break;
        	}
        }
        
        if (extFound) {
            sourcePath = filePath;

            ImdiSaxCheck checker = new ImdiSaxCheck();

            return checker.isSessionFile(f);
        }

        return false;
    }

    /**
     * Checks if the jar can be read from. Special case for reading  a special
     * imdi file from a jar.
     *
     * @param filePath the complete path
     *
     * @return true if jar and entry exist
     */
    private boolean setJarMetadataFile(String filePath) {
        if (filePath == null) {
            return false;
        }

        try {
            URL url = new URL(filePath);
            InputSource is = new InputSource(url.openStream());

            if (filePath.startsWith("jar:file")) {
                sourcePath = filePath;

                return true;
            }
        } catch (MalformedURLException mue) {
            ClientLogger.LOG.warning("Not a valid jar url: " + filePath);

            return false;
        } catch (IOException ioe) {
            ClientLogger.LOG.warning("Cannot open stream from jar: " +
                filePath);

            return false;
        }

        return false;
    }

    /**
     * Returns the source path
     *
     * @return the source path
     */
    @Override
	public String getMetadataFile() {
        return sourcePath;
    }

    /**
     * Creates an ImdiDoc object.
     */
    @Override
	public void initialize() {
        try {
            if ((sourcePath != null) && sourcePath.startsWith("jar:file")) {
                try {
                    URL url = new URL(sourcePath);
                    InputSource is = new InputSource(url.openStream());
                    imdiDoc = new ImdiDoc(is);
                } catch (MalformedURLException mue) {
                    ClientLogger.LOG.warning("Not a valid jar url: " +
                        sourcePath);
                } catch (IOException ioe) {
                    ClientLogger.LOG.warning("Cannot open stream from jar: " +
                        sourcePath);
                }
            } else {
                imdiDoc = new ImdiDoc(sourcePath);
            }
        } catch (DocumentNotLoadedException dnle) {
            ClientLogger.LOG.warning("Could not load the Imdi document: " +
                dnle.getMessage());
        }
    }

    /*    IMDI API approach
       private void parseFile(String filePath) {
           if (filePath == null) {
               ClientLogger.LOG.warning("File path is null");
               return;
           }
           try {
               File file = new File(filePath);
               OurURL ourl = new OurURL(file.toURL());
               IMDIDom iDom = new IMDIDom();
               Document doc = iDom.loadIMDIDocument(ourl, true);
               if (doc == null) {
                   ClientLogger.LOG.warning("Not a valid IMDI file");
                   ClientLogger.LOG.warning(iDom.getMessage());
                   return;
               }
               NodeList nodes = doc.getChildNodes();
               System.out.println("N: " + doc.getNodeName());
               if (doc.hasChildNodes()) {
                   printChildren(doc);
               }
               // test
               IMDIElement ielm = iDom.getIMDIElement(doc, "Session.Actor.Name");
               if (ielm != null) {
                   System.out.println("Session: " + ielm.getDomId() + " " +
                       ielm.getValue());
               } else {
                   System.out.println("No IMDI Element found");
               }
           } catch (MalformedURLException mue) {
               ClientLogger.LOG.warning("Invalid URL: " + filePath);
               System.out.println(mue);
           }
       }
     */
    private void printChildren(Node node) {
        NodeList nodes = node.getChildNodes();
        Element el;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nd = nodes.item(i);
            System.out.println("N: " + nd.getNodeName() + " V: " +
                nd.getNodeValue());

            if (nd instanceof Element) {
                el = (Element) nd;
                System.out.println("Id: " + el.getAttribute("id"));
            }

            if (nd.hasChildNodes()) {
                printChildren(nd);
            }
        }
    }

    /*
       private void parseFile2(String filePath) {
           if (filePath == null) {
               ClientLogger.LOG.warning("File path is null");
               return;
           }
           OurURL ourl = null;
           try {
               File file = new File(filePath);
               ourl = new OurURL(file.toURL());
           } catch (MalformedURLException mue) {
               mue.printStackTrace();
               return;
           }
           DocumentBuilder docBuilder;
           DocumentBuilderFactory factory = org.apache.xerces.jaxp.DocumentBuilderFactoryImpl.newInstance();
           factory.setIgnoringElementContentWhitespace(true);
           factory.setValidating(false);
           factory.setNamespaceAware(true);
           try {
               String fileName = ourl.toString();
               String uri = fileName.substring(0, fileName.lastIndexOf('/') + 1);
               InputSource fileSource = new InputSource(ourl.openStream());
               fileSource.setSystemId(uri);
               // the following can throw a ParserConfigurationException:
               docBuilder = factory.newDocumentBuilder();
               factory.setValidating(true);
               factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                   "http://www.w3.org/2001/XMLSchema");
               //          factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
               //              "http://www.mpi.nl/IMDI/schemas/xsd/IMDI_3.0.xsd");
               // the openStream can throw an IOException:
               factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                   this.getClass()
                       .getResource("/mpi/eudico/resources/IMDI_3.0.xsd")
                       .openStream());
               // the following can throw a ParserConfigurationException:
               docBuilder = factory.newDocumentBuilder();
               doc = docBuilder.parse(fileSource);
               //doc.normalize();
               doc.normalizeDocument();
           } catch (SAXParseException spe) {
               spe.printStackTrace();
           } catch (SAXException sxe) {
               sxe.printStackTrace();
           } catch (IllegalArgumentException iae) {
               iae.printStackTrace();
               //logger.error("IllegalArgumentException setting DocumentBuilderFactory attributes: " + iae.getMessage(),iae);
           } catch (ParserConfigurationException pce) {
               pce.printStackTrace();
               //logger.error("ParserConfigurationException creating DocumentBuilder : " + pce.getMessage(),pce);
           } catch (IOException ioe) {
               ioe.printStackTrace();
               //logger.error("IOException validating creating DocumentBuilder : " + ioe.getMessage(),ioe);
           }
           if (doc != null) {
               createIds(doc, doc.getDocumentElement(), null);
               //System.out.println("N: " + doc.getNodeName());
    
               if (doc.hasChildNodes()) {
                   printChildren(doc);
               }
    
               //System.out.println("Testing dom...");
    
               NodeList tagList = doc.getElementsByTagName("Name");
               System.out.println("Number of tags: " + tagList.getLength());
               Node nd;
               for (int j = 0; j < tagList.getLength(); j++) {
                   nd = (Node) tagList.item(j);
                   System.out.println("N: " + nd.getNodeName() + " V: " +
                       nd.getNodeValue());
               }
    
               Element actors = doc.getElementById(
                       "METATRANSCRIPT.Session.MDGroup.Actors.Actor.Name");
               if (actors != null) {
                   System.out.println("Actor: " + actors.getNodeName() + " " +
                       actors.getTextContent());
               } else {
                   System.out.println("Element not found...");
               }
    
           }
       }
     */
}
