package mpi.eudico.client.annotator.comments;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.AddCommentCommand;
import mpi.eudico.client.annotator.commands.ChangeCommentCommand;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.IoUtil;
import mpi.eudico.util.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class CommentManager implements ClientLogger {
	public static final boolean DEBUG = false;
	
    /** The preferences setting */
    static final String SHARED_DIRECTORY_LOCATION = "CommentViewer.SharedDirectoryLocation";
    /** The preferences setting (this one is a per-transcription setting) */
    static final String USE_SHARED_DIRECTORY = "CommentViewer.UseSharedDirectory";
    /** The preferences setting */
    static final String SEARCH_EAF_DIRECTORY = "CommentViewer.SearchDirectory.EAF";
    /** The preferences setting */
    static final String SEARCH_COMMENTS_DIRECTORY = "CommentViewer.SearchDirectory.Comments";
    /** The preferences setting (in minutes) */
    public static final String UPDATE_CHECK_TIME = "CommentViewer.UpdateCheckTime";
    /** The preferences setting */
    static final String SENDER_EMAIL_ADDRESS = "CommentViewer.Sender";
    /** The preferences setting */
    static final String RECIPIENT_EMAIL_ADDRESS = "CommentViewer.Recipient";
    /** The preferences setting */
    static final String INITIALS = "CommentViewer.Initials";
    /** The preferences setting */
    static final String THREAD_ID = "CommentViewer.ThreadID";
    /** The backend's service URL */
	static final String SERVER_URL = "CommentViewer.Server.URL";
	/** The default value for preference SERVER_URL */
	static final String SERVER_URL_DEFAULT = "https://corpus1.mpi.nl/ds/webannotator-basic";
    /** The login name to use for connecting to the back end */
	static final String SERVER_LOGIN_NAME = "CommentViewer.Server.Loginname";
	/** The namespace for Comment Envelopes */
    static final String COMMENT_ENVELOPES_NAMESPACE = "http://mpi.nl/tools/coltime";
	/** The location of the schema for Comment Envelopes */
    static final String COMMENT_ENVELOPES_SCHEMA_URL = "http://www.mpi.nl/tools/coltime/schema.xsd";
    static final String COMMENT_ENVELOPES_SCHEMA_URL_OLD = "http://www.mpi.nl/tools/elan/comments.xsd";
    
    static final String COMMENT_FILENAME_SUFFIX = ".eafcomment";
    
    /**
     * When reloading comments, indicate the source.
     */
    public static final int RELOAD_FROM_FILE = 0;
    public static final int RELOAD_FROM_SERVER = 1;

    private TranscriptionImpl transcription;
    private List<CommentEnvelope> comments;
    /** an unmodifiable view of the comments List */
    private List<CommentEnvelope> readOnlyComments;
    private CommentViewer viewer;
    private CommentWebClient webClient;
    /** Are there any comments that need to be saved to the save-file? */
    private boolean toBeSavedToFile = false;
    private long nextCheckForModificationOfSaveFile;  // msec
    private long saveFileLastModified;                // msec
    private long saveFileLastLookedAt;				  // msec
    
	public CommentManager(TranscriptionImpl transcription, CommentViewer viewer) {
        this.transcription = transcription;
        comments = new ArrayList<CommentEnvelope>();
        readOnlyComments = Collections.unmodifiableList(comments);
        
        load();
        
        // Delay setting the viewer to delay "notifyOfInsert()", because
        // the viewer is still assigning this CommentManager.
        this.viewer = viewer;
    }
	
	public boolean webClientIsLoggedIn() {
		return webClient != null && webClient.isLoggedIn();
	}
	
	public boolean loginWebClient() {
		if (webClient == null) {
			webClient = CommentWebClient.getCommentWebClient(transcription);
		}

        String serviceURL = SERVER_URL_DEFAULT;
        String username = "";
        
        String servicePref = Preferences.getString(SERVER_URL, null);
        if (servicePref != null) {
        	serviceURL = servicePref;
        }
        if (serviceURL.isEmpty()) {
			LOG.severe("Can't even try to login: Service URL is empty.");
        	return false;
        }

        String usernamePref = Preferences.getString(SERVER_LOGIN_NAME, null);
        if (usernamePref != null) {
        	username = usernamePref;
        }
        if (username.isEmpty()) {
			LOG.severe("Can't even try to login: User name is empty.");
        	return false;
        }
        
        boolean loggedIn = webClient.login(serviceURL, username);
        
        if (loggedIn) {    
        	/*
			 * Reload all comments here, since their permissions might have
			 * changed since we were last logged in.
			 */
			LOG.warning("Logged in successfully.");
        	reloadFromServer(true);
        } else {
			LOG.warning("Did not manage to log in.");
        	logoutWebClient();
        }
        
        return webClientIsLoggedIn();
	}
	
	/**
	 * Logs out of the web client.
	 * (In fact, it goes further, and closes it down completely).
	 */
	public void logoutWebClient() {
    	webClient.logout();
    	webClient.close();
    	webClient = null;
	}
    
    /**
     * Create a new comment from basic information.
     * 
     * @param text
     * @param start
     * @param end
     * @return
     */
    public CommentEnvelope createComment(String text, long start, long end) {
        CommentEnvelope m = new CommentEnvelope();
        m.setMessage(text);
        m.setStartEndTime(start, end);

        m.setMessageID();
        m.setCreationDate();
        m.setModificationDate();
        m.setAnnotationFile(transcription.getURN().toASCIIString());
        m.setAnnotationFileURL("");
        m.setAnnotationFileType("EAF");

        // Override defaults from preferences
        String stringPref = Preferences.getString(SENDER_EMAIL_ADDRESS, null);
        if (stringPref != null) {
            m.setSender(stringPref);
        }
        stringPref = Preferences.getString(RECIPIENT_EMAIL_ADDRESS, null);
        if (stringPref != null) {
            m.setRecipient(stringPref);
        }
        stringPref = Preferences.getString(INITIALS, null);
        if (stringPref != null) {
            m.setInitials(stringPref);
        }
        stringPref = Preferences.getString(THREAD_ID, null);
        if (stringPref != null) {
            m.setThreadID(stringPref);
        }
        
        m.setToBeSaved(true);

        return m;
    }

    /**
     * Notify the ListDataListener.
     * @param where the location in the list that was added.
     */
    private void notifyOfInsert(int where) {
        if (viewer != null) {
            viewer.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, where, where));
        }
    }

    /**
     * Notify the ListDataListener.
     * @param where the location in the list that was removed.
     */
    private void notifyOfRemoval(int where) {
        if (viewer != null) {
            viewer.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, where, where));
        }
    }

    /**
     * Notify the ListDataListener.
     * @param where the location in the list that was added.
     */
    private void notifyOfChange(int lwb, int upb) {
        if (viewer != null) {
            viewer.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, lwb, upb));
        }
    }
    
    /**
     * The undoable method (using a Command) to insert a new CommentEnvelope.
     * @param m
     * @return the index where it was inserted.
     */
    
    public int undoableInsert(CommentEnvelope m) {
    	AddCommentCommand cmd = (AddCommentCommand)ELANCommandFactory.createCommand(
    			transcription, ELANCommandFactory.ADD_COMMENT);
    	Object[] args = { m };
    	
    	cmd.execute(this, args);
    	
    	return cmd.getInsertPosition();
    }

    /**
	 * The undoable method (using a Command) to remove a CommentEnvelope.
	 * 
	 * @param m
	 */
    
    public void undoableRemove(int i) {
    	Command cmd = ELANCommandFactory.createCommand(
    			transcription, ELANCommandFactory.DELETE_COMMENT);
    	Object[] args = { Integer.valueOf(i) };

    	cmd.execute(this, args);
    }

    /**
	 * The undoable method (using a Command) to replace a CommentEnvelope.
	 * 
	 * @param index
	 *            the position in the sequence
	 * @param ce
	 *            the replacement comment
	 */
    public int undoableReplace(int index, CommentEnvelope ce) {
    	ChangeCommentCommand cmd = (ChangeCommentCommand)ELANCommandFactory.createCommand(
    			transcription, ELANCommandFactory.CHANGE_COMMENT);
    	
    	Object[]args = { Integer.valueOf(index), ce };
    	cmd.execute(this, args);
    	
    	return cmd.getInsertPosition();
    }

    /**
     * Insert a new comment in our collection.
     * We keep them sorted by time.
     * <p>
     * This is the not-undoable version, as used by the AddCommentCommand.
     * <p>
     * Marks the collection as to be saved to the file.
     * Saves the comment to the server.
     * @param m
     */
    public int insert(CommentEnvelope m) {
        int where = 0;
        toBeSavedToFile = true;
        m.setToBeSaved(true);

        if (comments.isEmpty()) {
            comments.add(m);
        } else {
        	// Find the location to insert the new comment
            ListIterator<CommentEnvelope> i = comments.listIterator();

            CommentEnvelope next = null;
            while (i.hasNext()) {
                where++;
                next = i.next();
                // Did we just step over the element that we wanted to insert before?
                if (m.compareTo(next) <= 0) {
                    i.previous();   // step cursor back over the element we just compared with
                    i.add(m);       // add before cursor and hence before the element
                    where--;
                    notifyOfInsert(where);
                    saveToServer(m);

                    return where;
                }
            }
            i.add(m);               // add at the end
        }
        
        notifyOfInsert(where);
        saveToServer(m);
        
        return where;
    }

    /**
     * Remove a comment from our own collection and from the server,
     * and notify the ListDataListener.
     * 
     * @param index the location in the list to remove.
     */
    public void remove(int index) {
        toBeSavedToFile = true;

        CommentEnvelope ce = comments.get(index);
    	deleteFromServer(ce);

    	// Because our table model looks at this list directly, instead of keeping a copy,
    	// and the table looks in the model at the given index,
    	// notify before the item is really removed.
        notifyOfRemoval(index);
        comments.remove(index);
    }

    /**
	 * Replaces a comment, and notify the ListDataListener.
	 * <p>
	 * The new comment has the same ID as the old one, so it is not to be
	 * deleted from the server... only replaced.
	 * 
	 * @param index
	 *            the location in the list to replace.
	 * @param newCE
	 *            The CommentEnvelope to use as replacement.
	 * @return the position of the replacement comment. The new position may
	 *         differ when it has a different time, because the replacement is
	 *         re-inserted.
	 */
    public int replace(int index, CommentEnvelope newCE) {
        toBeSavedToFile = true;
    	
        comments.remove(index);
        notifyOfRemoval(index);
        return insert(newCE);	// this will save the new version of it to the server
    }

    /**
     * Get a read-only iterator over the list of comments.
     * Its initial position is such that the first '.next()' call will give
     * the first comment after the specified time.
     * @return
     */
    public ListIterator<CommentEnvelope> firstCommentAfterTime(long time) {
        ListIterator<CommentEnvelope> it = readOnlyComments.listIterator();

        if (time > 0) {
            while (it.hasNext()) {
                CommentEnvelope comment = it.next();
                if (comment.getStartTime() >= time) {
                	// we went too far: back up one place.
                    it.previous();
                    return it;
                }
            }
        }
        return it;
    }

    /**
	 * Gets a single CommentEnvelope from our list to display. If you change it,
	 * call {@link #release(int index, CommentEnvelope ce)} when you're done
	 * with it. Don't change the time (i.e. the position in the list).
	 * <p>
	 * This is the not-undoable version. See also {@link #undoableGet(int)}.
	 * 
	 * @param index
	 *            which CommentEnvelope we want.
	 * @return a reference to the actual CommentEnvelope
	 */
    public CommentEnvelope get(int index) {
        return comments.get(index);
    }

    /**
	 * A CommentEnvelope which was obtained from {@link #get(int index)} is now
	 * no longer in use. Assume it was changed, but that the position in the
	 * sequence of comments is the same.
	 * <p>
	 * Sends a ListDataEvent to the listener, marks it as to be saved, and sends
	 * to the server.
	 * 
	 * @param index
	 *            the originally obtained index. Try to be careful not to modify
	 *            the list: that makes it invalid. The value is used for GUI
	 *            updates.
	 * @param ce
	 *            the actually obtained CommentEnvelope, to be certain that
	 *            we're talking about the correct one. (Also forces you to
	 *            obtain it with {@link #get(int)}.
	 */
    public void release(int index, CommentEnvelope ce) {
        toBeSavedToFile = true;
        ce.setToBeSaved(true);
        
    	saveToServer(ce);

    	notifyOfChange(index, index);
    }

    /**
	 * Like {@link #get(int)}, but if you call
	 * {@link #undoableRelease(int, CommentEnvelope)} afterwards, then the user
	 * can undo whatever changes were made to the CommentEnvelope.
	 *
	 * @param index
	 * @return a clone of the CommentEnvelope
	 */
    public CommentEnvelope undoableGet(int index) {
    	CommentEnvelope original = get(index);
    	
    	return original.clone();
    }
    
    /**
     * The counterpart to {@link #undoableGet(int)}.
     * 
     * @param index
     * @param ce
     */
    public int undoableRelease(int index, CommentEnvelope ce) {
    	return undoableReplace(index, ce);
    }
    
    /**
     * Save a comment to the server, if we have a web client.
     * @param ce
     */
	private void saveToServer(CommentEnvelope ce) {
		if (webClientIsLoggedIn()) {
    		webClient.putCommentEnvelope(ce);
    	}
	}

	/**
	 * Remove a comment from the server, if we have a web client.
	 * In that case also clear its URL, since it won't be valid any more.
	 * 
	 * @param ce
	 */
	private void deleteFromServer(CommentEnvelope ce) {
		if (webClientIsLoggedIn()) {
    		webClient.deleteCommentEnvelope(ce);
    		ce.setMessageURL(""); // now it has no URL any more.
    	}
	}

    /**
     * Get a read-only version of the list of comments.
     */
    public List<CommentEnvelope> getList() {
        return readOnlyComments;
    }

    /**
	 * Get a read-only iterator over the list of comments. No accidental changes
	 * can be made to the list itself (but the comments in it can be changed).
	 * 
	 * @return
	 */
    public ListIterator<CommentEnvelope> listIterator() {
        return readOnlyComments.listIterator();
    }

    /**
     * Find a comment, given the messageID.
     * @param messageID
     * @return the CommentEnvelope, if found, or null otherwise.
     */
    public int findCommentById(String messageID) {
        int i = 0;
        for (CommentEnvelope c : comments) {
            if (messageID.equals(c.getMessageID())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Hook before the transcription is closed.
     */
    public void isClosing() {
        save();
       
        if (webClient != null) {
        	webClient.close();
        	webClient = null;
        }
    }

    /**
     * @return a pathname private to the current transcription, where to save the comments,
     * or null if no such can be constructed.
     */
    private String privatePathName() {
        if (transcription instanceof TranscriptionImpl) {
            TranscriptionImpl ti = (TranscriptionImpl)transcription;

            String pathName = ti.getPathName();

            if (pathName != null && ! pathName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                int dot = pathName.lastIndexOf('.');
                if (dot >= 0) {
                    pathName = pathName.substring(0, dot);
                }
                pathName += COMMENT_FILENAME_SUFFIX;

                return pathName;
            }
        }
        return null;
    }

    /**
     * @return a directory name which is shared with other users,
     * such as via the use of DropBox, a network file system,
     * or other similar technologies.
     */
    private String sharedDirectoryName() {
        Boolean boolPref = Preferences.getBool(USE_SHARED_DIRECTORY, transcription);
        if (boolPref != null && boolPref) {
            String stringPref = Preferences.getString(SHARED_DIRECTORY_LOCATION, null);

            if (stringPref != null) {
                final String sharedPathName = FileUtility.urlToAbsPath(stringPref);

                if (!sharedPathName.isEmpty() && FileUtility.fileExists(sharedPathName)) {
                    return sharedPathName + "/";
                }
            }
        }
        return null;
    }

    /**
     * Construct either a public or private path name for saving comments,
     * depending on what has been configured.
     * @return
     */
    private String effectivePathName() {
        String pathName = privatePathName();

        if (pathName != null) {
            String sharedDirName = sharedDirectoryName();
            if (sharedDirName != null) {
                String sharedFileName = sharedDirName + FileUtility.fileNameFromPath(pathName);

                return sharedFileName;

            } else {
                return pathName;
            }
        }

        return null;
    }

    /**
     * Save the comments to a file, the pathname of which is derived from the pathname of the transcription.
     * If there is no pathname yet, don't do anything.
     * If the preference for a shared directory is set, it is saved there.
     * <p>
     * If the save file was modified since we looked at it last time,
     * first merge in its modifications.
     */
    public void save() {
    	String pathName = effectivePathName();

    	if (pathName != null) {
    		File f = new File(pathName);
    		long lastModified = f.lastModified();
    		if (lastModified > saveFileLastModified) {
    			checkForFileModifications(true);
    		}
    		save(pathName);
    		saveFileLastLookedAt = 
    		saveFileLastModified = f.lastModified();
    		clearToBeSavedToFile();
    	}
    }
    
    /**
     * The comments have been saved to the default file.
     * The to-be-saved markers can be removed now.
     * Just to be on the safe side, put any comments to the server as well.
     */
    private void clearToBeSavedToFile() {
    	toBeSavedToFile = false;
    	for (CommentEnvelope ce : comments) {
    		ce.setToBeSavedToFile(false);
    		if (ce.getToBeSavedToServer()) {
    			saveToServer(ce);
    		}
    	}
    }

    /**
     * Save the current comments to a file with the given name.
     * If there are currently no comments, the given file is removed.
     * 
     * @param pathName
     */
    private void save(String pathName) {
    	if (comments.isEmpty()) {
    		File f = new File(pathName);
    		if (f.exists()) {
    			f.delete();
    		}
    	} else {
		    try {
		        save(pathName, getElement());
		    } catch (ParserConfigurationException e) {
		        e.printStackTrace();
		    }
    	}
    }

    /**
	 * Save the given comments to a file with the given name.
	 * <p>
	 * At this point it would be nice to have some mutual exclusion so
	 * that multiple users on different machines with shared directories don't
	 * overwrite each other's stuff blindly.
	 * <p>
	 * However, when something like DropBox is used for sharing, the file
	 * replication is rather delayed and this may be useless. On the other hand,
	 * DropBox will try to create a backup file in case of a collision.
	 * 
	 * @param pathName
	 *            in which file to save them.
	 * @param e
	 *            the root element (as a DOM tree) to save.
	 */
    private void save(String pathName, Element e) {
        try {
            IoUtil.writeEncodedFile("UTF-8", pathName, e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Get a DOM tree (Document Object Model) representation of the collection of comments.
     * @return the root element (not the Document)
     * @throws ParserConfigurationException
     */
    private Element getElement() throws ParserConfigurationException
    {
        return getElement(this.comments);
    }

    /**
     * Get a DOM tree from a given collection of comments, structured as a ColTimeList.
     * 
     * @param comments
     * @return
     * @throws ParserConfigurationException
     */
    private Element getElement(Collection<CommentEnvelope> comments) throws ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder         db = dbf.newDocumentBuilder();
        Document               doc = db.newDocument();

        Element list = doc.createElementNS(COMMENT_ENVELOPES_NAMESPACE, "ColTimeList");
        doc.appendChild(list);

        for (CommentEnvelope cm: comments) {
            Element elem = cm.getElement(COMMENT_ENVELOPES_NAMESPACE, doc);

            list.appendChild(elem);
        }

        Element docElement = addSchemaLocation(doc);
        return docElement;
    }

    /**
     * Get a DOM tree from a single comment.
     * 
     * @param cm
     * @return
     * @throws ParserConfigurationException
     */
    public static Element getElement(CommentEnvelope cm) throws ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder         db = dbf.newDocumentBuilder();
        Document               doc = db.newDocument();

        Element elem = cm.getElement(COMMENT_ENVELOPES_NAMESPACE, doc);
        doc.appendChild(elem);

        Element docElement = addSchemaLocation(doc);
        return docElement;
    }

	private static Element addSchemaLocation(Document doc) {
		Element docElement = doc.getDocumentElement();
		
        docElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
        		COMMENT_ENVELOPES_NAMESPACE + " " + COMMENT_ENVELOPES_SCHEMA_URL);
        
		return docElement;
	}

    /**
     * Load the current comments from a default location.
     * It searches first in a shared directory (if set), then in
     * the filename associated with the current Transcription.
     */
    private void load() {
    	String pathName = effectivePathName();

    	if (pathName != null) {
    		if (FileUtility.fileExists(pathName)) {
    	        Predicate<CommentEnvelope> filter =
    	                new Predicate<CommentEnvelope>() {
    	            @Override
    	            public boolean test(CommentEnvelope obj) {
    	    			// Copy any comments not known to the server to the server
    	            	if (obj.getAnnotationFileURL().isEmpty()) {
    	            		obj.setToBeSavedToServer(true);
    	            		saveToServer(obj);
    	            	}
    	                return true;
    	            }
    	        };
    			load(pathName, filter);
    			//saveFileLastModified = System.currentTimeMillis();
    		}
    	}

    	// Get comments from the webservice.
    	reloadFromServer(false);
    }

    /**
     * Attempt to load comments from the given pathname.
     * It takes a Predicate to check for comments that
     * we want -- for example requiring that they
     * actually belong to the current Transcription.
     *
     * @param pathName
     * @return true if comments were found there, false otherwise
     */
    private boolean load(String pathName, Predicate<CommentEnvelope> filter) {

    	saveFileLastLookedAt = System.currentTimeMillis();
        List<CommentEnvelope> comments = read(pathName, filter);

        if (comments != null && !comments.isEmpty()) {
            // Sort the comments (time-wise) just in case.
            Collections.sort(comments);
            this.comments.clear();
            this.comments.addAll(comments);
            notifyOfChange(0, this.comments.size() - 1);

            return true;
        }

        return false;
    }

    /**
     * Read a file with comments but don't store them yet as *the* comments.
     * @param pathName
     * @param filter
     * @return the comments that were read.
     */

    static List<CommentEnvelope> read(String pathName, Predicate<CommentEnvelope> filter) {
        CommentEnvelopesParser parser = new CommentEnvelopesParser();
        List<CommentEnvelope> readComments = parser.parse(pathName, filter);
        assignReadOnlyStatus(readComments, false);
        return readComments;
    }
    
    /**
     * Get the Sender's email address from the (global) preferences.
     * Returns "" if it is not set.
     */
	private static String getSenderEmailAddress() {
		String me;
		String pref = Preferences.getString(SENDER_EMAIL_ADDRESS, null);
		if (pref != null) {
			me = pref;
		} else {
			me = "";
		}
		return me;
	}
			
	/**
	 * After reading comments from a file, see which ones would be read-only.
	 * <p>
	 * This is based on the &lt;Sender> field.
	 * 
	 * @param comments
	 * @param forceRW
	 *            if set, force setting comments to Read/Write. Otherwise it is
	 *            assumed they are in that state by default.
	 */
    private static void assignReadOnlyStatus(List<CommentEnvelope> comments, boolean forceRW) {
    	String me = getSenderEmailAddress();
		
		assignReadOnlyStatus(comments, me, forceRW);
    }

    /**
     * Cache of the sender on which the current read-only determination
     * was based. This is used to check if the work should be done again.
     * Can be static because the preference is global.
     */
    private static String cachedSenderPreference = "";
    
    /**
     * Assign read-only status to a list of comments, based on the given identity
     * and each comment's Sender field.
     * <p>
     * This method is static, so that it can be called indirectly from
     * CommentSearchDialog.scanForComment(Pattern, File).
     * 
     * @param comments
     * @param me
     * @param forceRW
	 *            if set, force setting comments to Read/Write. Otherwise it is
	 *            assumed they are in that state by default.
     */
	private static void assignReadOnlyStatus(List<CommentEnvelope> comments,
			String me, boolean forceRW) {
		if (me.isEmpty()) {
			if (forceRW) {
				for (CommentEnvelope c : comments) {
					c.setReadOnly(false);
				}
			}
		} else {
			for (CommentEnvelope c : comments) {
				String sender = c.getSender();
				c.setReadOnly(!sender.isEmpty() && !me.equals(sender));
			}
		}
		cachedSenderPreference = me;
	}

    /**
     * After changing some setting that could influence it,
     * re-evaluate the read-only status of the current comments.
     * <p>
     * This will be done only if we're not logged in to the web service,
     * since its knowledge overrides ours.
     */
    private void reAssignReadOnlyStatus() {
    	if (!webClientIsLoggedIn()) {
        	String me = getSenderEmailAddress();

    		if (!cachedSenderPreference.equals(me)) {
	    		assignReadOnlyStatus(comments, me, true);
	    		// is this below needed anyway to update the viewer?    		
	    		// viewer.commentTable.setComments(comments);
    		}
    	}
    }

    // no @Override! Called from CommentViewer directly.
    public void preferencesChanged() {
		reAssignReadOnlyStatus();
    }
    
    ///                                                                                  ///
    /// This part of the comment manager is about finding a transcription given its URN. ///
    ///                                                                                  ///

	/**
     * Finds a Transcription that corresponds to the given URN.
     * It further tests the given predicate.
     * The first transcription that passes both the URN check and the predicate
     * is returned.
     * @param urn
     */
    public static TranscriptionImpl findTranscriptionFromURN(URI urn, Predicate<TranscriptionImpl> pred) {
        List <Transcription> ts = FrameManager.getInstance().getOpenTranscriptions();

        for (Transcription t: ts) {
            if (t.getURN().equals(urn)) {
            	TranscriptionImpl ti = (TranscriptionImpl) t;
            	if (pred == null || pred.test(ti)) {
            		return ti;
            	}
            }
        }
        
        // If not found in this way, try to look at some files.
        // The first one that actually has the URN is opened in a frame and returned.
        List<File> candidates = findCandidateEafFiles(urn);
        
        return findTranscriptionFromURN(urn, pred, candidates);
    }

    /**
     * Also creates an ElanFrame2 for the transcription.
     * @param urn
     * @param candidates
     * @return
     */
	static TranscriptionImpl findTranscriptionFromURN(
			URI urn, Predicate<TranscriptionImpl> pred, List<File> candidates) {
		for (File file : candidates) {
        	// Parse the file
        	TranscriptionImpl t = new TranscriptionImpl(file.getAbsolutePath());
        	// After the quick scan done before, this is a proper check for the URN:
        	if (t.getURN().equals(urn)) {
        		// Then we may have to perform further tests as specified by the Predicate.
            	if (pred == null || pred.test(t)) {
            		return t;
            	}
        	}
        }
        
        return null;
	}
	
	/**
	 * Look around in some places if we can find EAF files with the given URN.
	 * <p>
	 * These locations include
	 * <ul>
	 * <li>the configured directory for searching EAF files
	 * <li>the directories of each of the files on the recent files list
	 * <li>the configured shared directory (is this useful at all?) 
	 * </ul>
	 * 
	 * @param urn
	 *            the URI to look for
	 * @return a list of files that seem to contain the URI.
	 */
    private static List<File> findCandidateEafFiles(URI urn) {
        Set<File> dirs = new TreeSet<File>();

        // This includes the configured directory for searching EAF files,
        String stringPref = Preferences.getString(SEARCH_EAF_DIRECTORY, null/*t*/);
    	
        if (stringPref != null) {
            final String searchPathName = FileUtility.urlToAbsPath(stringPref);
        	addCandidateDirectory(dirs, new File(searchPathName));
        }

        // ... the directories of recent files,
        for (String r : FrameManager.getInstance().getRecentFiles()) {
        	addCandidateDirectory(dirs, new File(FileUtility.directoryFromPath(r)));
        }

        // ... the shared directory as set for every open transcription.
//        for (Transcription t: FrameManager.getInstance().getOpenTranscriptions()) {
//	        stringPref = Preferences.getString(SHARED_DIRECTORY_LOCATION, t);
//	
//	        if (stringPref != null) {
//	            final String sharedPathName = FileUtility.urlToAbsPath(stringPref);
//	        	addCandidateDirectory(dirs, new File(sharedPathName));
//	        }
//        }
        stringPref = Preferences.getString(SHARED_DIRECTORY_LOCATION, null);

        if (stringPref != null) {
            final String sharedPathName = FileUtility.urlToAbsPath((String) stringPref);
        	addCandidateDirectory(dirs, new File(sharedPathName));
        }

        // Ok, now we have some directories to search.
        // Find all EAF files in them that seem to contain the URI.

        List<File> res = new ArrayList<File>();

        for (File dir : dirs) {
            findCandidateEafFiles(res, urn, dir);
        }

        return res;
    }

	/**
	 * Find all EAF files in them that seem to contain the URI.
	 * 
	 * @param urn
	 *            the URI to look for
	 * @param startdir
	 *            the directory where to search (recursively)
	 * @return a list of files that seem to contain the URI.
	 */
    private static List<File> findCandidateEafFiles(URI urn, File startdir) {
        List<File> res = new ArrayList<File>();

        findCandidateEafFiles(res, urn, startdir);
        
        return res;
    }

    /**
     * Add a directory to a set of directories to search.
     * We don't need any directories that are inside others in the set,
     * so every time we add a new directory, we compare it with all
     * the ones we have already.
     * If the new directory is a parent of any existing entries,
     * then the existing entry can be removed.
     * If the new directory is a child of any existing entry,
     * it does not need to be added at all.
     * 
     * @param dirs
     * @param dir
     */
    private static void addCandidateDirectory(Set<File> dirs, File newdir) { 
    	try {
			newdir = newdir.getCanonicalFile();
		} catch (IOException e) {
			// Path is problematical, we don't want it anyway.
			return;
		}
    	
    	if (dirs.contains(newdir)) {
    		return;
    	}
    	
    	Iterator<File> iter = dirs.iterator();
    	while (iter.hasNext()) {
    		File olddir = iter.next();
    		// Trying to add a sub-directory? Not needed.
    		if (isParentOf(olddir, newdir)) {
    			return;
    		}
    		// Trying to add a parent? The subdir (olddir) is no longer needed.
    		if (isParentOf(newdir, olddir)) {
    			iter.remove();
    		}
    	}
    	dirs.add(newdir);
    }

    /**
	 * Checks if the first directory is a parent of the second one. This is
	 * based on the assumption that children's paths are longer, extended
	 * versions of their parents.
	 * 
	 * As a special case, a directory is considered to be a parent of itself.
	 * 
	 * @param parent
	 *            the potential parent directory
	 * @param child
	 *            the potential child directory
	 * @return if they are indeed parent and child.
	 */
    private static boolean isParentOf(File parent, File child) {
    	String p, c;
    	
		p = parent.getPath();	// Files have been made canonical beforehand.
    	c = child.getPath();
    	
		if (p.equals(c)) {
			return true;
		}
		
		int p_length = p.length();
		int c_length = c.length();
		
		// Not only must the parent be a prefix of the child,
		// there must also be a directory separator.
		// We don't want "a/b" to seem like a parent of "a/bc".
    	return c_length > p_length &&
    			c.startsWith(p) &&
    			c.substring(p_length, p_length + 1).equals(File.separator);
	}

	/**
	 * Search all files named *.eaf in the given directory (recursively) to
	 * check if they are the one with the given URN. Return a list of the file
	 * names that match.
	 * 
	 * @param res
	 *            the List<File> in which the results are accumulated
	 * @param urn
	 *            the URI to search for
	 * @param dir
	 *            the directory to search in (recursively).
	 */
    private static void findCandidateEafFiles(List<File> res, URI urn, File dir) {
        File files[] = dir.listFiles();

        if (files != null) {
	        for (File f : files) {
	            if (f.isDirectory()) {
	                findCandidateEafFiles(res, urn, f);
	            } else if (f.canRead()) {
	                String name = f.getName();
	                if (name.endsWith(".eaf")) {
	                    // Ok, look into this file.
	                    if (scanForURN(urn, f)) {
	                        res.add(f);
	                    }
	                }
	            }
	        }
        }
    }

    private static Pattern matchPattern;

    /**
     * Have a quick look through the file to see if it contains
     * &lt;PROPERTY NAME="URN">urn:nl-mpi-tools-elan-eaf:59d08e6a-5cd9-4aed-8aa4-7074c270e635&lt;/PROPERTY>
     * in the first few lines.
     * Where "few" doesn't need to be more than 25 (typically found in line 5 or 6).
     * It uses the knowledge that when we write XML files, each PROPERTY is on a single line.
     * (If that ever changes, an alternative strategy could be to just read in the first kilobyte or so,
     * and try to match the URN itself. It is supposed to be unique, after all.)
     *
     * @param urn
     * @param file
     * @return
     */

    private static boolean scanForURN(URI urn, File file) {
        if (matchPattern == null) {
        	// \\s is string literal denotation for \s which stands for any whitespace character
        	// The (.*) in the middle is group(1) which matches the URN.
        	// <PROPERTY NAME="URN">urn:nl-mpi-tools-elan-eaf:59d08e6a-5cd9-4aed-8aa4-7074c270e635</PROPERTY>
            matchPattern = Pattern.compile("<PROPERTY\\s+NAME\\s*=\\s*\"URN\">\\s*(.*)\\s*</PROPERTY>");
        }
        String urnAsString = urn.toString();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            Matcher m = matchPattern.matcher("");

            for (int lineNr = 0; lineNr < 25; lineNr++) {
                line = reader.readLine();
                if (line == null)
                    break;

                m = m.reset(line);
                if (m.find()) {
                    String urnInFile = m.group(1);
                    if (urnAsString.equals(urnInFile)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Performs a check on the accessibility of the system clipboard.
     * TODO: unify all copies of this function.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    public static boolean canAccessSystemClipboard() {

        if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
                se.printStackTrace();

                return false;
            }
        }

        return true;
    }

    /**
     * Put several selected comments to the clipboard.
     * 
     * @param selected the indices of the selected comments
     */
    public void commentsToClipboard(int selected[]) {
        try {
            if (canAccessSystemClipboard()) {
                Collection<CommentEnvelope> coll = new ArrayList<CommentEnvelope>();
                for (int s : selected) {
                    coll.add(comments.get(s));
                }
                Element e = getElement(coll);
                String s = serialize(e);
                StringSelection ssVal = new StringSelection(s);

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String serialize(Element e) {
        Document document = e.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        // Having he part "<?xml version="1.0" encoding="UTF-16"?>"
        // is a bit weird, because the encoding of the text may well be affected by whatever the user
        // stores or transmits it.
        // However, by the time we get it back into a String, it will be correct again.
        //serializer.getDomConfig().setParameter("xml-declaration", false);
        serializer.getDomConfig().setParameter("format-pretty-print", true);
        String str = serializer.writeToString(e);

        return str;
    }

    /**
     * Try to get some comments from the clipboard.
     * If they seem to relate to one of the currently open Transcriptions, and they are not
     * duplicates, add them.
     * This method is static because the comments are not limited to the current Transcription,
     * but can be associated with any Transcription.
     */
    public static void commentsFromClipboard() {
        if (canAccessSystemClipboard()) {
            Collection<CommentEnvelope> coll;

            /*
             * Parsing random text from the clipboard can easily go wrong.
             * Don't be surprised if the parse throws some exception.
             */
            try {
                Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String s = (String)trans.getTransferData(DataFlavor.stringFlavor);
                    CommentEnvelopesParser cep = new CommentEnvelopesParser();

                    coll = cep.parse(new InputSource(new StringReader(s)), null);

                } else {
                    return;
                }
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (coll.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        //"No comment(s) found on the clipboard.",
                        ElanLocale.getString("CommentManager.NoCommentsOnClipboard"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.ERROR_MESSAGE);
            }

            // Now try to put the comments where they belong.

            for (CommentEnvelope ce : coll) {
                URI uri = ce.getAnnotationURIBase();
                TranscriptionImpl t = findTranscriptionFromURNwithDialog(uri, null);
                Pair<ElanFrame2, Boolean> pair = getOrOpenFrameFor(t);
                ElanFrame2 frame = pair.getFirst();

                if (frame != null) {
                    ce.setToBeSaved(true);
                    frame.getViewerManager().getCommentViewer().addComment(ce);
                } else {
                    // should not happen
                    System.err.printf("Can't find frame for pathname '%s' with URN %s\n",
                    		t.getPathName(), uri.toASCIIString());
                }
            }
        }
    }

    /**
     * Try to find a Transcription which has the given URI.
     * Try a bit harder by asking the user for a place to search, if nothing was found.
     * @param pred 
     */
    public static TranscriptionImpl findTranscriptionFromURNwithDialog(URI uri, Predicate<TranscriptionImpl> pred) {
        TranscriptionImpl t = findTranscriptionFromURN(uri, pred);

        while (t == null) {
            // Show some popup, or try to search files.
            // GUI things should probably be handled by the CommentViewer.
            System.err.printf("Can't find open transcription for URN %s\n", uri.toASCIIString());
            JOptionPane.showMessageDialog(null,
                    // "Can't find an open transcription for a comment.\n
                    // Please choose a directory in which to search for\n
                    // the transcription file to which the comment belongs."
                    ElanLocale.getString("CommentManager.CantFindOpenTranscription"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.ERROR_MESSAGE);

            // Let the user choose a directory...
        	FileChooser chooser = new FileChooser(null);
        	//
        	String title = ElanLocale.getString("CommentManager.SelectDirectory");
            chooser.createAndShowFileDialog(title,
            		FileChooser.OPEN_DIALOG,
            		null /*approveButtonText*/,
            		null /*extensions*/,
            		null /* mainFilterExt*/,
            		true /*acceptAllFiles*/,
            		"LastUsedEAFDir" /*prefStringToLoadtheCurrentPath*/,
            		FileChooser.DIRECTORIES_ONLY /*fileSelectionMode*/, 
            		null /*selectedFileName*/);
            
            File file = chooser.getSelectedFile();
            if (file == null) {
            	break;
            }
        	t = findTranscriptionFromURN(uri, pred,
            			findCandidateEafFiles(uri, file));
        } 
        
        return t;
    }
    
    /**
     * Find which ElanFrame2 corresponds to a Transcription.
     * If needed, open one.
     * Returned are a Pair with the Frame, and Boolean indicating
     * whether the Frame was specifically opened here.
     */
    public static Pair<ElanFrame2, Boolean> getOrOpenFrameFor(TranscriptionImpl t) {
    	if (t != null) {
    		ElanFrame2 frame = FrameManager.getInstance().getFrameFor(t);
	        if (frame == null) {
				frame = FrameManager.getInstance().createFrame(t);
				return Pair.makePair(frame, Boolean.valueOf(true));
	        }
			return Pair.makePair(frame, Boolean.valueOf(false));
    	}
		return Pair.makePair(null, Boolean.valueOf(false));
    }
    
    /**
     * Try to open a mail client, ready to send a mail message to the set recipient
     * with the CommentEnvelopes in the mail body.
     * 
     * @param selected the indices of the comments to put in the mail
     */
    public void commentsToMail(int selected[]) {
        try {
            List<CommentEnvelope> coll = new ArrayList<CommentEnvelope>();
            for (int s : selected) {
                coll.add(comments.get(s));
            }
            Element e = getElement(coll);
            String s = serialize(e);
            String to = coll.get(0).getRecipient();

            String uriString = String.format("mailto:%s?subject=%s&body=%s",
                    mailtoURIEncode(to),
                    mailtoURIEncode("ColTime comment"),
                    mailtoURIEncode(s));
            // Could do this via a WebMA, to abstract the implementation,
            // but that's a MenuAction so not really appropriate either.
            Desktop.getDesktop().mail(new URI(uriString));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * URL-Encoding... but space becomes %20 instead of +.
     * That is special with mailto: URIs.
     * Therefore most URI building helpers are not directly useful.
     */
    String mailtoURIEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }

    /**
     * Add a comment to our collection, as long as it doesn't seem to be a duplicate.
     * This is determined by checking the ColTimeMessageID.
     * This function is undoable.
     * @param ce
     * @return if the comment was a duplicate, return the index of the already existing one.
     */
    public int undoableAddComment(CommentEnvelope ce) {
        int c = findCommentById(ce.getMessageID());
        if (c >= 0) {
            return c;
        }
        undoableInsert(ce);

        return -1;
    }

	/**
	 * Make a new comment out of it by changing the message ID.
	 * If <code>removeFromServerFirst</code> is true, 
	 * remove the old identity from the server first.
	 */
	public void updateAsNewComment(CommentEnvelope ce, boolean removeFromServerFirst) {
		if (removeFromServerFirst) {
			deleteFromServer(ce);
		}
		ce.setMessageID();
		ce.setMessageURL("");		
	}
	
    /**
     * This method can be called periodically to check if there are external modifications to
     * our saved comment file.
     * <p>
     * If the webClient is non-null, it will try to check it too.
     * <p>
     * It will only check if at least 30 seconds has passed since the last time.
     * Other than that, the frequency is determined by the caller.
     * <p>
     * Also take the opportunity to save our local comments, if needed.
     * 
     * @param force Normally it will wait some minimum time between rechecks.
     * If force is set, it will always check.
     */
    public void checkForFileModifications(boolean force) {
        long now = System.currentTimeMillis();
    	
    	if (!force) {
	        if (now < nextCheckForModificationOfSaveFile) {
	            return;
	        }
    	} 
	        
        nextCheckForModificationOfSaveFile = now + 1000L * 30L;       // 30 seconds in msec.

        String filename = effectivePathName();
        if (DEBUG) {
        	System.out.println("Checking for new comments in " + filename);
        }
        if (filename == null) {
            return;
        }
        File f = new File(filename);
        long time = f.lastModified();
        if (time > saveFileLastModified) {
            saveFileLastModified = time;
            reload(filename, null);
        }
        
    	// Check comments from the webservice as well.
        reloadFromServer(false);
        
        // If we made any changes or decisions, get the saved file up to date according
        // to our own world view as soon as possible.
        // Note: between the reload() above and now, the file may be changed again.
        // This will cause save() to recursively call back here. This may go on for a while
        // but hopefully, eventually, the file will stop changing.
        if (toBeSavedToFile) {
        	save();
        }    
        
        now = System.currentTimeMillis();
        nextCheckForModificationOfSaveFile = now + 1000L * 30L;       // 30 seconds in msec.     
    }

    /**
     * Reload a comment file: a file which has been loaded earlier.
     * 
     * @param pathName file name
     * @param filter a filter to determine inclusion of comments in the operation
     * @return true if some comments have been read from the file.
     */
    public boolean reload(String pathName, Predicate<CommentEnvelope> filter) {

        List<CommentEnvelope> comments = read(pathName, filter);

        if (comments != null && !comments.isEmpty()) {
            reload(comments, null, RELOAD_FROM_FILE);
            
            return true;
        }

        return false;
    }

    /**
     * Reload comments from the server (if we're logged in).
     * <p>
     * Normally we try to be economical with how much data we transfer,
     * but if force is set, fetch all comments. This will
     * update their permissions etc.
     * 
     * @param force fetch all comments, even if we think we already have them.
     */
    public void reloadFromServer(boolean force) {
        if (webClientIsLoggedIn()) {
        	List<CommentEnvelope> notChanged = new LinkedList<CommentEnvelope>();
	    	List<CommentEnvelope> extra = webClient.getCommentEnvelopes(
	    			transcription.getURN(),
	    			force ? null : getList(),
	    			notChanged);
	    	if (extra != null) {
	    		reload(extra, notChanged, RELOAD_FROM_SERVER);
	    	}
        }
    }

    /**
     * Reload a list of comments. The {@code comments} are possibly changed, but that will
     * be checked in more detail. It may still turn out that a comment in it is actually
     * an exact duplicate of an existing comment.
     * <br/>
     * Comments that are known to be unchanged (and not deleted) are included in
     * {@code notChanged}.
     * <p>
     * Entirely new comments are simply added (this is judged by their id).
     * <p>
     * Exact duplicates are not added to our current set.
     * <p>
     * Modified comments must be confirmed by the user in most cases.
     * <p>
     * The same for deleted comments (those are the comments not included in {@code notChanged}
     * and that were also not found to match one of those in {@code comments}).
     * 
     * @param reloadedComments the new replacement collection of comments
     * @param notChanged some comments that were not reloaded, because we think they were not changed.
     *        They are also not deleted.
     * @param reloadedFromWhere {@code RELOAD_FROM_SERVER} or {@code RELOAD_FROM_FILE}.
     */
    public void reload(List<CommentEnvelope> reloadedComments,
    				   List<CommentEnvelope> notChanged, int reloadedFromWhere) {
        // Sort the comments (time-wise) just in case.
        Collections.sort(reloadedComments);

        // Track which existing comments we haven't seen yet in the reloaded set.
        Set<String> unseen = new HashSet<String>();
        for (CommentEnvelope ce : this.comments) {
            unseen.add(ce.getMessageID());
        }
        
        // If there were any not-loaded but also not-changed comments,
        // consider them not-deleted.
        if (notChanged != null) {
            for (CommentEnvelope ce : notChanged) {
                unseen.remove(ce.getMessageID());
            }
        }

        // Process reloaded, potentially changed, comments.
        for (CommentEnvelope ce : reloadedComments) {
            // Check if this comment already exists
            String id = ce.getMessageID();
            int existingIndex = findCommentById(id);
            if (existingIndex < 0) {
                // This is a new comment. It is safe to add it. Don't bother with undo.
                insert(ce);
            } else {
                // Check if it is an exact duplicate, or if it was slightly modified
                CommentEnvelope existing = get(existingIndex);
                unseen.remove(id);
                if (ce == existing) {
                	// This one wasn't even actually loaded from the server.
                	// Nothing to do. Won't really happen either.
                	if (DEBUG) {
                		System.out.println("Equal pointer. Nothing to reload.");
                	}
                } else if (ce.interestingValueEquals(existing)) {
                    // Identical in the interesting fields. No need to do anything much.
                	existing.setUninterestingFields(ce);
                	if (reloadedFromWhere == RELOAD_FROM_SERVER) {
                    	if (DEBUG &&
                    			ce.getLastModifiedOnServer() != null) {
    	                	System.out.printf("interestingValueEquals. Copying lastModifiedOnServer: %s %s %s\n",
    	                			id, ce.getMessageURL(), ce.getLastModifiedOnServer().toString());
                    	}
                		existing.setServerModifiableFields(ce);
                	}
                } else if (ce.isReadOnly()) {
                	// When we don't control the comment, there is no point in
                	// trying to reject its changes.
                	if (DEBUG) {
                		System.out.printf("Replace %s without asking: it is read-only\n", ce.getMessageID());
                	}
        			undoableReplace(existingIndex, ce);
                } else if (reloadedFromWhere == RELOAD_FROM_FILE &&
                		existing.getToBeSavedToFile() &&
                		existing.isNewerThan(ce) &&
                		ce.getModificationDate().getTime() < saveFileLastLookedAt - 5*1000) {
					// Okay, this mismatch occurs likely because we have
					// modified this comment, but the file is re-checked
					// because some other comment in the file was changed.
					// Also check that the comment in the file wasn't changed
					// in between our previous look at the file and now
					// (with some slop for inaccurate clocks).
                	// NOTE: this can still lose an external change, if it was made
                	// before we last looked at the file, but it wasn't synced to us yet.
                	// Keep the local version of the comment.
                	if (DEBUG) {
                		System.out.printf("Keep local %s without asking: it is to-be-saved-to-file and the other is old.\n", ce.getMessageID());
                	}
                } else {
                    // The CommentViewer may to the GUI work to ask the user what to do now.
                    if (viewer != null) {
                        viewer.modifyComment(existingIndex, ce, reloadedFromWhere);
                    } else {
                    	// It is not really safe to do anything without asking the user.
                    }
                }
            }
        }

        // IDs that we haven't seen in the lists so far must have been deleted
        // from the source we got this collection from.
        //
        // Ask the user if they should be deleted locally
        // (or if not possible, don't do anything).
        //
        // To make life easier, look at the "to be saved to server flag"
        // which would be set if we edited the comment while offline.
        // It can also be set if we loaded a comment from a file, it had
        // no known URL, and we were not logged in to export it immediately.
        // If it is set, assume it is to be saved to the server without asking.
        //
        // If the comment is readonly, i.e. not ours, remove it without asking.
        if (!unseen.isEmpty()) {
//        	System.out.printf("unseen: contains %d elements\n", unseen.size());

	        for (String removed : unseen) {
//            	System.out.printf("unseen: still contains %s\n", removed);
	        	int existingIndex = findCommentById(removed);
	        	if (existingIndex >= 0) {
	        		// Clear MessageURL on our real version,
	        		// since the server doesn't have these comments.
	        		CommentEnvelope ce = get(existingIndex);
	        		ce.setMessageURL("");
	        		
	        		// Now get a clone for potential undoable operations.
	        		ce = undoableGet(existingIndex);
	        		if (ce.getToBeSavedToServer()) {
	                	if (DEBUG) {
	                		System.out.printf("Copy %s to server insted of removing: it has 'ToBeSavedToServer'\n", ce.getMessageID());
	                	}
	        			undoableRelease(existingIndex, ce); // saves to server
	        		} else if (ce.isReadOnly()) {
	                	if (DEBUG) {
	                		System.out.printf("Remove %s without asking: it is read-only\n", ce.getMessageID());
	                	}
	        			undoableRemove(existingIndex);
	        		} else if (viewer != null) {
	        			// this may release or remove it (undoably)
		                viewer.maybeRemoveComment(existingIndex, ce, reloadedFromWhere);
		            } else {
	                	// It is not really safe to do anything without asking the user.
		            }
	        	}
	        }
        }
        
        // If we made any changes or decisions, get the saved file up to date according
        // to our own world view as soon as possible.
        if (toBeSavedToFile) {
        	save();
        }    
    }
}
