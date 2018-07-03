package mpi.eudico.client.annotator.md.imdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.DocumentNotLoadedException;
import mpi.eudico.util.MutableInt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Creates a document, DOM based, and offers methods to interact with it.
 * Intended for retrieval of metadata keys (elements) and their values. Read
 * only.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiDoc {
    private Document doc;
    private List<String> allKeys = new ArrayList<String>();
    private Map<String, String> mdValuesMap = new LinkedHashMap<String, String>();
    private final String KEYS_KEY = "Keys.Key";
    private final String HTML_B = "<html>";
    private final String HTML_E = "</html>";
    private final String B_B = "<b>";
    private final String B_E = "</b>";

    /**
     * Creates a new ImdiDoc instance
     *
     * @param filePath the path to an imdi file
     *
     * @throws DocumentNotLoadedException thrown when a document could not be
     *         created
     */
    public ImdiDoc(String filePath) throws DocumentNotLoadedException {
        super();
        if (filePath == null) {
        	throw new DocumentNotLoadedException("File path is null");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
        	throw new DocumentNotLoadedException("File not found");
        }

        DocumentBuilder docBuilder;
        //DocumentBuilderFactory factory = org.apache.xerces.jaxp.DocumentBuilderFactoryImpl.newInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        FileInputStream stream = null;
        
        try {
        	String fileName = file.getAbsolutePath();
        	fileName.replace('\\', '/');
            String uri = fileName.substring(0, fileName.lastIndexOf('/') + 1);
            stream = new FileInputStream(file);
            InputSource fileSource = new InputSource(stream);
            fileSource.setSystemId(uri);
            // the following can throw a ParserConfigurationException:
            docBuilder = factory.newDocumentBuilder();
            factory.setValidating(true);// HS reconsider this, above it is set to false?
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
            /*
            docBuilder.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					System.out.println("Warn: " + exception.getMessage());
					exception.printStackTrace();
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					System.out.println("Fatal: " + exception.getMessage());
					exception.printStackTrace();
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					System.out.println("Error: " + exception.getMessage());
					exception.printStackTrace();
				}
			});
			*/
            doc = docBuilder.parse(fileSource);

            //doc.normalize();//??
            //doc.normalizeDocument();//??
        } catch (SAXParseException spe) {
            //spe.printStackTrace();
            ClientLogger.LOG.warning("Cannot parse file: " + spe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                spe);
        } catch (SAXException sxe) {
            //sxe.printStackTrace();
            ClientLogger.LOG.warning("Cannot parse file: " + sxe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                sxe);
        } catch (IllegalArgumentException iae) {
            //iae.printStackTrace();
            ClientLogger.LOG.warning(
                "Cannot set attributes for document builder: " +
                iae.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                iae);
        } catch (ParserConfigurationException pce) {
            //pce.printStackTrace();
            ClientLogger.LOG.warning("Cannot set configure the parser: " +
                pce.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                pce);
        } catch (IOException ioe) {
            //ioe.printStackTrace();
            ClientLogger.LOG.warning("Cannot read the Imdi file: " +
                ioe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                ioe);
        } catch (Throwable thr) {
        	// catch everything that can be caught
            ClientLogger.LOG.warning("Error while reading the Imdi file: " +
                    thr.getMessage());
            //thr.printStackTrace();
                throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                    thr);
        } finally {
        	if (stream != null) {
	        	try {
					stream.close();
				} catch (IOException e) {
				}
        	}
        }

        // create fully qualified id's for each element
        createIds(doc, doc.getDocumentElement(), null);

        //printChildren(doc);
    }

    /**
     * Creates a new ImdiDoc instance from an InputSource
     *
     * @param inputSource the input source
     *
     * @throws DocumentNotLoadedException thrown when a document could not be
     *         created
     */
    public ImdiDoc(InputSource inputSource) throws DocumentNotLoadedException {
        super();

        DocumentBuilder docBuilder;
        //DocumentBuilderFactory factory = org.apache.xerces.jaxp.DocumentBuilderFactoryImpl.newInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        try {
            //String uri = fileName.substring(0, fileName.lastIndexOf('/') + 1);
            //inputSource.setSystemId(uri);
            // the following can throw a ParserConfigurationException:
            docBuilder = factory.newDocumentBuilder();
            factory.setValidating(true);// HS reconsider this, above it is set to false?
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
            /*
            docBuilder.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					System.out.println("Warn: " + exception.getMessage());
					exception.printStackTrace();
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					System.out.println("Fatal: " + exception.getMessage());
					exception.printStackTrace();
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					System.out.println("Error: " + exception.getMessage());
					exception.printStackTrace();
				}
			});
			*/
            doc = docBuilder.parse(inputSource);

            //doc.normalize();//??
            //doc.normalizeDocument();//??
        } catch (SAXParseException spe) {
            //spe.printStackTrace();
            ClientLogger.LOG.warning("Cannot parse file: " + spe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                spe);
        } catch (SAXException sxe) {
            //sxe.printStackTrace();
            ClientLogger.LOG.warning("Cannot parse file: " + sxe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                sxe);
        } catch (IllegalArgumentException iae) {
            //iae.printStackTrace();
            ClientLogger.LOG.warning(
                "Cannot set attributes for document builder: " +
                iae.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                iae);
        } catch (ParserConfigurationException pce) {
            //pce.printStackTrace();
            ClientLogger.LOG.warning("Cannot set configure the parser: " +
                pce.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                pce);
        } catch (IOException ioe) {
            //ioe.printStackTrace();
            ClientLogger.LOG.warning("Cannot read the Imdi file: " +
                ioe.getMessage());
            throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                ioe);
        } catch (Throwable thr) {
        	// catch everything that can be caught
            ClientLogger.LOG.warning("Error while reading the Imdi file: " +
                    thr.getMessage());
            //thr.printStackTrace();
                throw new DocumentNotLoadedException("Could not construct Imdi DOM: ",
                    thr);
        }

        // create fully qualified id's for each element
        createIds(doc, doc.getDocumentElement(), null);

        //printChildren(doc);
    }
    
    /**
     * Creates (temporary) id's for all elements. The id's are the "fully
     * qualified" element paths, that can be used as the keys in key-value
     * pairs.  Fills the "all keys" list.
     *
     * @param doc the document
     * @param elm the element
     * @param parentId the id
     */
    private void createIds(Document doc, Element elm, String parentId) {
        if (parentId != null) {
            elm.setAttribute("id", parentId + "." + elm.getNodeName());
        } else {
            elm.setAttribute("id", elm.getNodeName());
        }

        //elm.setIdAttribute("id", true); // only useful if getElementById is exploited

        String nextId = elm.getAttribute("id");

        //System.out.println("ID: " + nextId);
        if (!allKeys.contains(nextId)) {
            allKeys.add(nextId);
        }

        allKeys.remove(ImdiConstants.METATRANSCRIPT);

        NodeList childList = elm.getChildNodes();
        Node nd;

        for (int i = 0; i < childList.getLength(); i++) {
            nd = childList.item(i);

            if (nd instanceof Element) {
                createIds(doc, (Element) nd, nextId);
            }
        }

        /* test
           Element actors = doc.getElementById(
                   "METATRANSCRIPT.Session.MDGroup.Actors.Actor.Name");
           if (actors != null) {
               System.out.println("Actor: " + actors.getNodeName() + " " +
                   actors.getTextContent());
           } else {
               System.out.println("Element not found...");
           }
         */
    }
    
    /**
     * The method trim() of String class does not return an empty String in case all 
     * characters are e.g. whitespaces.
     * @param value the string to process
     * @return a trimmed string, can be empty
     */
    private String trimmedValue(String value) {
    	if (value == null || value.length() == 0) {
    		return value;
    	}
    	int len = value.length();
    	int st = 0;
    	int off = 0;
    	char[] val = value.toCharArray();

    	while ((st < len) && (val[off + st] <= ' ')) {
    	    st++;
    	}
    	while ((st < len) && (val[off + len - 1] <= ' ')) {
    	    len--;
    	}
    	if (st == len) {
    		return "";// or null?
    	}
    	return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }

    /**
     * Returns a List containing the metadata keys.
     *
     * @return the keys
     */
    public List<String> getKeys() {
        return allKeys;
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
    public List<String> getValues(String key) {
        if (key == null) {
            return null;
        }

        // Could also start by finding all elements with a certain tag name and 
        // filter the right ones out through the parent nodes
        /*
           if (key.indexOf(".") > -1) {
               int li = key.lastIndexOf('.');
               String elName = key.substring(li + 1);
               NodeList nl = doc.getElementsByTagName(elName);
               for (int i = 0; i < nl.getLength(); i++) {
                   Node nd = nl.item(i);
                   if (nd instanceof Element) {
                       System.out.println("El id: " + ((Element)nd).getAttribute("id"));
                   }
               }
           }
         */
        List<Element> elems = getElementsById(key);

        if (elems.size() > 0) {
            List<String> vals = new ArrayList<String>(elems.size());
            Element elem;

            for (int i = 0; i < elems.size(); i++) {
                elem = elems.get(i);

                if ((elem.getFirstChild() != null) &&
                        (elem.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                    vals.add(trimmedValue(elem.getFirstChild().getNodeValue()));
                } else {
                    vals.add("");
                }
            }

            return vals;
        }

        return null;
    }

    /**
     * Returns the value for a certain key.
     *
     * @param key the key
     *
     * @return the value or null
     */
    public String getValue(String key) {
        if (mdValuesMap != null) {
            return mdValuesMap.get(key);
        }

        return null;
    }

    /**
     * Returns a Map with all key-value pairs given the list of keys.
     *
     * @param keys the keys
     * @param includeChildren if true, the children of each key should be included as well
     *
     * @return a map containing all key value pairs
     */
    public Map<String, String> getValuesForKeys(List<String> keys, boolean includeChildren) {
        if ((keys == null) || (keys.size() == 0)) {
            return null;
        }

        Map<String, String> mdValues = new LinkedHashMap<String, String>();

        addKeyAndValue(keys, doc, mdValues, new MutableInt(0), includeChildren);

        return mdValues;
    }

    /**
     * Creates and returns a tree view of the selected metadata keys.
     * 
     * @param keys the keys
     * @param includeChildren if true, the children of each key should be included as well
     * 
     * @return the root of the tree
     */
    public DefaultMutableTreeNode getTreeForKeys(List<String> keys, boolean includeChildren) {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode(ImdiConstants.METATRANSCRIPT);
    	
    	addKeyAndValue(keys, doc, root, includeChildren);
    	
    	if (root.getChildCount() == 1) {
    		root = (DefaultMutableTreeNode) root.getChildAt(0);
        	if (root.getChildCount() == 1 && 
        			((MDKVData) root.getUserObject()).key.indexOf(ImdiConstants.METATRANSCRIPT) > -1) {
        		root = (DefaultMutableTreeNode) root.getChildAt(0);       		
        	}
        	int ch = root.getChildCount();
        	DefaultMutableTreeNode nd, nd2;
        	for (int i = 0; i < ch; i++) {
        		nd = (DefaultMutableTreeNode) root.getChildAt(i);
        		Enumeration nodeEnum = nd.depthFirstEnumeration();
        		while (nodeEnum.hasMoreElements()) {
        			nd2 = (DefaultMutableTreeNode) nodeEnum.nextElement();
        			Object userObj = nd2.getUserObject();
        			if (userObj instanceof MDKVData && ((MDKVData) userObj).key.indexOf(ImdiConstants.MDGROUP) > -1) {
        				nodeEnum = null;
        				nd = (DefaultMutableTreeNode) nd2.getParent();
        				
        				for(int j = nd2.getChildCount() - 1;j >= 0; j--) {
        					//nd.add((DefaultMutableTreeNode) nd2.getChildAt(j));
        					nd.insert((DefaultMutableTreeNode) nd2.getChildAt(j), 0);
        				}
        				nd.remove(nd2);
        				break;
        			}
        		}
        	}
    	}
    	//printTree(root);
    	return root;
    }
    
    /**
     * There can be multiple elements with the same id (path)!
     *
     * @param key the key
     *
     * @return a list containing all occurrences of the particular element
     */
    private List<Element> getElementsById(String key) {
        if (key != null) {
            List<Element> elems = new ArrayList<Element>(5);
            addChildElementsById(key, doc, elems);

            return elems;
        }

        return null;
    }

    /**
     * The id's are the path to the elements and there can be duplicates!
     *
     * @param key the (fully qualified) key
     * @param node the parent node
     * @param elemList the list to add to
     */
    private void addChildElementsById(String key, Node node,
        List<Element> elemList) {
        if (node != null) {
            NodeList nodes = node.getChildNodes();
            Node nd;
            Element el;

            for (int i = 0; i < nodes.getLength(); i++) {
                nd = nodes.item(i);

                if (nd instanceof Element) {
                    el = (Element) nd;

                    if (key.equals(el.getAttribute("id"))) {
                        elemList.add(el);
                    }
                }

                addChildElementsById(key, nd, elemList);
            }
        }
    }

    /**
     * Recursively checks the Node's children for an id that is in the list of
     * keys. Id's can occur multiple times since some elements can occur more
     * than once.
     *
     * @param keys the keys to search for
     * @param node the node to start with
     * @param mdValues the map to add valid key-value pairs to
     * @param index a counter to prepend to the key
     * @param includeChildren if true all child elements are also added (regardless of whether 
     * their id is in the list of keys
     */
    private void addKeyAndValue(List<String> keys, Node node,
        Map<String, String> mdValues, MutableInt index, boolean includeChildren) {
        if (node == null) {
            return;
        }

        NodeList nodes = node.getChildNodes();
        Node nd;
        Element elem;
        String key = null;
        String val = null;

        for (int i = 0; i < nodes.getLength(); i++) {
            nd = nodes.item(i);

            if (nd instanceof Element) {
                elem = (Element) nd;
                key = elem.getAttribute("id");

                if (keys.contains(key)) {
                    if ((elem.getFirstChild() != null) &&
                            (elem.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                        val = trimmedValue(elem.getFirstChild().getNodeValue());
                    } else {
                        val = "";
                    }

                    // special treatment of Keys key-value pairs
                    if (key.endsWith(KEYS_KEY)) {
                        String name = elem.getAttribute("Name");

                        if (name != null) {
                            key += ("[" + name + "]");
                        }
                    }

                    // only add if the value is non empty??
                    mdValues.put(("(" + index.intValue + ")" + key), val);

                    if (includeChildren) {
                        Node n2 = null;
                        //NodeList childEl = elem.getElementsByTagName("*"); // all child elements
                        NodeList childEl = elem.getChildNodes();

                        if (childEl.getLength() > 0) {
                            for (int j = 0; j < childEl.getLength(); j++) {
                                n2 = childEl.item(j);

                                if (n2 instanceof Element && n2.getParentNode() == elem) {
                                    index.intValue++;
                                    addChildKeyAndValue(n2,
                                        mdValues, index, includeChildren);
                                }
                            }
                        }
                    }
                }
            }

            index.intValue++;
            addKeyAndValue(keys, nd, mdValues, index, includeChildren);
        }
    }

    /**
     * Adds all child elements of the specified node, recursively.
     *
     * @param node the parent node
     * @param mdValues the map to add the values to
     * @param index a counter to prepend to the key
     * @param includeChildren if true all child elements are also added (regardless of whether 
     * their id is in the list of keys
     */
    private void addChildKeyAndValue(Node node, Map<String, String> mdValues,
        MutableInt index, boolean includeChildren) {
        if (node == null) {
            return;
        }

        Element elem;
        String key = null;
        String val = null;

        if (node instanceof Element) {
            elem = (Element) node;
            key = elem.getAttribute("id");

            if ((elem.getFirstChild() != null) &&
                    (elem.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                val = trimmedValue(elem.getFirstChild().getNodeValue());
            } else {
                val = "";
            }

            // special treatment of Keys key-value pairs
            if (key.endsWith(KEYS_KEY)) {
                String name = elem.getAttribute("Name");

                if (name != null) {
                    key += ("[" + name + "]");
                }
            }

            // only add if the value is non empty??
            mdValues.put(("(" + index.intValue + ")" + key), val);

            if (includeChildren) {
                Node n2 = null;
                //NodeList childEl = elem.getElementsByTagName("*"); // all child elements
                NodeList childEl = elem.getChildNodes();

                if (childEl.getLength() > 0) {
                    for (int j = 0; j < childEl.getLength(); j++) {
                        n2 = childEl.item(j);

                        if (n2 instanceof Element && n2.getParentNode() == elem) {
                            index.intValue++;
                            addChildKeyAndValue(childEl.item(j), mdValues, index, includeChildren);
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursively checks the Node's children for an id that is in the list of
     * keys. Id's can occur multiple times since some elements can occur more
     * than once.
     *
     * @param keys the keys to search for
     * @param node the node to start with
     * @param treeNode the node to add new tree nodes to
     * @param includeChildren if true all child elements are also added (regardless of whether 
     * their id is in the list of keys)
     */
    private void addKeyAndValue(List<String> keys, Node node,
        DefaultMutableTreeNode treeNode, boolean includeChildren) {
        if (node == null) {
            return;
        }

        NodeList nodes = node.getChildNodes();
        Node nd;
        Element elem = null;
        String elId = null;
        String key = null; 
        DefaultMutableTreeNode childNode = null;
        String val = null;
        boolean hasText = false;
        
        
        for (int i = 0; i < nodes.getLength(); i++) {
            nd = nodes.item(i);
            childNode = null;
            hasText = false;
	        
	        //if (nd instanceof Element) {
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
	        	elem = (Element) nd;
	        	elId = elem.getAttribute("id");
	        	key = elem.getNodeName();
	        	
	            if (keys.contains(elId)) {
	                if ((elem.getFirstChild() != null) &&
	                        (elem.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
	                    val = trimmedValue(elem.getFirstChild().getNodeValue());
	                    hasText = true;
	                } else {
	                    val = "-";
	                }
	
	                // special treatment of Keys key-value pairs
	                if (key.equals(ImdiConstants.KEY)) {
	                    String name = elem.getAttribute("Name");
	
	                    if (name != null && hasText) {
	                        key = name.replace(' ', '_');
	                        //childNode = new DefaultMutableTreeNode(getHtmlString(key, val));
	                        childNode = new DefaultMutableTreeNode(getDataObject(key, val));
	                        treeNode.add(childNode);
	                        //treeNode.insert(childNode, 0);
	                    }
	                    continue;	                    
	                }
	                
	                if (hasText) {
	                	//childNode = new DefaultMutableTreeNode(getHtmlString(key, val));
	                	childNode = new DefaultMutableTreeNode(getDataObject(key, val));
	                } else {
	                	// special treatment of Session, Project and Actor: get the 
	                	// Name child element value and add it
	                	if (key.equals(ImdiConstants.ACTOR) || key.equals(ImdiConstants.SESSION) || 
	                			key.equals(ImdiConstants.PROJECT)) {
	                		String name = getNameChildElement(elem);
	                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, name));
	                		childNode = new DefaultMutableTreeNode(getDataObject(key, name));
	                	} else {
	                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, null));
	                		childNode = new DefaultMutableTreeNode(getDataObject(key, null));
	                	}
	                }
	                treeNode.add(childNode);
	                //treeNode.insert(childNode, 0);

	                if (includeChildren /*&& !hasText*/) {// not a valid test some elements seem to have text as well
	                    Node n2 = null;
	                    // both elem.getElementsByTagName("*") and elem.getChildNodes() return all descendants, 
	                    // not only direct children
	                    NodeList childEl = elem.getChildNodes();

	                    if (childEl.getLength() > 0) {
	                        for (int j = 0; j < childEl.getLength(); j++) {
	                            n2 = childEl.item(j);
	                            
	                            if (n2 instanceof Element && n2.getParentNode() == elem) {
	                                addChildKeyAndValue(n2, childNode, includeChildren);
	                            }
	                        }
	                    }
	                }
	                
	            } else {// not in the list, check if children are in the list
	            	boolean mustAddEmptyTreeNode = false;
	            	String next;

	            	for (int j = 0; j < keys.size(); j++) {
	            		next = keys.get(j);
	            		if (next != null && next.startsWith(elId)) {
	            			mustAddEmptyTreeNode = true;
	            			break;
	            		}
	            	}
	            	
	            	if (mustAddEmptyTreeNode) {
	            		// special treatment of Session, Project and Actor: get the 
	                	// Name child element value and add it
	                	if (key.equals(ImdiConstants.ACTOR) || key.equals(ImdiConstants.SESSION) || 
	                			key.equals(ImdiConstants.PROJECT)) {
	                		String name = getNameChildElement(elem);
	                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, name));
	                		childNode = new DefaultMutableTreeNode(getDataObject(key, name));
	                	} else {
	                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, null));
	                		childNode = new DefaultMutableTreeNode(getDataObject(key, null));
	                	}
	            		//childNode = new DefaultMutableTreeNode(getHtmlString(key, null));
	            		treeNode.add(childNode);
	            		//treeNode.insert(childNode, 0);
	            		/*
	                    Node n2 = null;
	                    NodeList childEl = elem.getElementsByTagName("*"); // all child elements
	
	                    if (childEl.getLength() > 0) {
	                        for (int j = 0; j < childEl.getLength(); j++) {
	                            n2 = childEl.item(j);
	
	                            if (n2 instanceof Element) {
	                                addKeyAndValue(keys, n2, childNode, includeChildren);
	                            }
	                        }
	                    }
	                    */
	            	}
	            }
	        }
	        if (childNode != null) {
	        	addKeyAndValue(keys, nd, childNode, includeChildren);
	        } else {
	        	addKeyAndValue(keys, nd, treeNode, includeChildren);	
	        }
        }
    }
 

    /**
     * Adds all child elements of the specified node, recursively.
     *
     * @param node the parent node
     * @param treeNode the node to add new tree nodes to
     * @param includeChildren if true all child elements are also added (regardless of whether 
     * their id is in the list of keys
     */
    private void addChildKeyAndValue(Node node, DefaultMutableTreeNode treeNode,
        boolean includeChildren) {
        if (node == null) {
            return;
        }

        Element elem = null;
        //String elId = null;
        String key = null; 
        DefaultMutableTreeNode childNode = null;
        String val = null;
        boolean hasText = false;

        if (node instanceof Element) {
        	elem = (Element) node;
        	//elId = elem.getAttribute("id");
        	key = elem.getNodeName();

            if ((elem.getFirstChild() != null) &&
                    (elem.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                val = trimmedValue(elem.getFirstChild().getNodeValue());
                hasText = true;
            } else {
                val = "-";
            }

            // special treatment of Keys key-value pairs
            if (key.equals(ImdiConstants.KEY)) {
                String name = elem.getAttribute("Name");

                if (name != null && hasText) {
                    key = name.replace(' ', '_');
                    //hasText = true;//end element
                    //childNode = new DefaultMutableTreeNode(getHtmlString(key, val));
                    childNode = new DefaultMutableTreeNode(getDataObject(key, val));
                    treeNode.add(childNode);
                } 
                return;
                	                    
            } else {
                if (hasText) {
                	//childNode = new DefaultMutableTreeNode(getHtmlString(key, val));
                	childNode = new DefaultMutableTreeNode(getDataObject(key, val));
                } else {
                	// special treatment of Session, Project and Actor: get the 
                	// Name child element value and add it
                	if (key.equals(ImdiConstants.ACTOR) || key.equals(ImdiConstants.SESSION) || 
                			key.equals(ImdiConstants.PROJECT)) {
                		String name = getNameChildElement(elem);
                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, name));
                		childNode = new DefaultMutableTreeNode(getDataObject(key, name));
                	} else {
                		//childNode = new DefaultMutableTreeNode(getHtmlString(key, null));
                		childNode = new DefaultMutableTreeNode(getDataObject(key, null));
                	}
                }
                
                if (childNode != null) {
                	treeNode.add(childNode);
                }
                
                if (includeChildren) {
                    Node n2 = null;
                    NodeList childEl = elem.getElementsByTagName("*"); // all child elements
                    
                    if (childEl.getLength() > 0) {
                        for (int j = 0; j < childEl.getLength(); j++) {
                            n2 = childEl.item(j);

                            if (n2 instanceof Element && n2.getParentNode() == elem) {
                                addChildKeyAndValue(n2, childNode, includeChildren);
                            }
                        }
                    }
                }
            }

        }
    }
    
    /**
     * Returns the value of the "Name" child element of the specified element.
     *  
     * @param elem the parent element
     * @return the value of the Name descendant element
     */
    private String getNameChildElement(Element elem) {
    	if (elem != null) {
    		NodeList list = elem.getElementsByTagName("Name");
    		if (list.getLength() > 0) {
    			Node n = list.item(0);
    			if ((n.getFirstChild() != null) &&
                        (n.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
    				return trimmedValue(n.getFirstChild().getNodeValue());
    			}
    		}
    	}
    	return null;
    }
    
    private String getHtmlString(String key, String val) {
    	if (key == null) {
    		return null;
    	}

    	StringBuilder b = new StringBuilder(HTML_B);
    	b.append(B_B);
    	b.append(key);
    	b.append(B_E);
    	if (val != null) {
    		b.append(" ");
    		b.append(val);
    	}
    	b.append(HTML_E);
    	
    	return b.toString();
    }
    
    private MDKVData getDataObject(String key, String value) {
    	if (value != null) {
    		value = value.trim();
    		if (value.length() == 1 && value.charAt(0) == '\n') {
    			value = null;
    		}
    	}
    	return new MDKVData(key, value);
    }
    
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

                if ((el.getFirstChild() != null) &&
                        (el.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                    System.out.println("Text: " +
                        el.getFirstChild().getNodeValue());
                }

                System.out.println("Num Ch.: " +
                    el.getChildNodes().getLength());
            }

            if (nd.hasChildNodes()) {
                printChildren(nd);
            }
        }
    }
    
    private void printTree(DefaultMutableTreeNode root) {
    	if (root == null) {
    		return;
    	}
    	Enumeration nodeEnum = root.breadthFirstEnumeration();
    	DefaultMutableTreeNode node;
    	MDKVData data;
    	while(nodeEnum.hasMoreElements()) {
    		node = (DefaultMutableTreeNode) nodeEnum.nextElement();
    		data = (MDKVData) node.getUserObject();
    		int level = node.getLevel();
    		for (int i = 0; i < level; i++) {
    			System.out.print("  ");
    		}
    		System.out.println("K: " + data.key + " V: " + data.value);
    	}
    }
}
