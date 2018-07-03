package mpi.eudico.client.annotator.comments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.DebugInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Element;

import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.XmlBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.Principal;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;

public class CommentWebClient implements ClientLogger {
	/**
	 * Some constants which are basically compile-time selections to include
	 * or exclude some functionality.
	 */
	static final boolean DEBUG = CommentManager.DEBUG;
	static final private boolean CREATE_CACHED_REPRESENTATION = false; 
    private static final boolean ALWAYS_CREATE_CACHED_REPRESENTATION =
    		CREATE_CACHED_REPRESENTATION && false;   
	
    private JAXBContext jc;
    private Unmarshaller unmarshaller;
    /** The base URL for the web services */
    URI serviceURL;
    /** Pre-resolved based on serviceURL */
    URI serviceURL_api_annotations;
    /** resolve relative URLs from DWAN relative to this */
    URI resolveBaseURL;
    /** This is the path part from the serviceURL (without hostname) plus /api/ */
	private String serviceUrlPath;
    private CloseableHttpClient httpclient;
    private Marshaller marshaller;
    /**
     * Maps from Transcription urn to preferred DWAN Target URL.
     * The server may assign different ones but if we have a choice
     * we'll use this one.
     * NOTE: each transcription has its own web client, so there will be only
     * one entry in the map. Therefore it is overkill. To be fixed later.
     */
    private Map<String, String> urnToTargetURL;
    private HttpClientContext context;
	private CookieStore cookieStore;
	private String userName;
	private String loggedInPincipalURIString;
	private boolean isLoggedIn;
	private ObjectFactory objectFactory;
	private DatatypeFactory datatypeFactory;
	private StatusLine lastStatusLine;
	private TranscriptionImpl transcription;

	static final int HTTP_OK = 200;
	static final int HTTP_CLIENT_ERROR = 400; // any responses >= 400 are errors
	static final int HTTP_UNAUTHORIZED = 401;
	static final int HTTP_FORBIDDEN = 403;
	static final int HTTP_NOT_FOUND = 404;

    /**
     * Factory for the singleton CommentWebClient.
     * @return the one and only CommentWebClient.
     */
    public static CommentWebClient getCommentWebClient(TranscriptionImpl t) {
    	return new CommentWebClient(t);
    }

    /**
     * Make constructor private so users are forced to use the factory function.
     * Initialize the unmarshaller (for use in the unmarshal() method).
     */
    private CommentWebClient(TranscriptionImpl t) {
        urnToTargetURL = new HashMap<String, String>();
        transcription = t;

        try {
            jc = JAXBContext.newInstance(eu.dasish.annotation.schema.ObjectFactory.class);
            unmarshaller = jc.createUnmarshaller(); // XML -> tree
            marshaller = jc.createMarshaller();		// tree -> XML
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

            // The schema location will later be overridden, because it is
            // relative to the service URL.
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,  
            		"http://lux17.mpi.nl/ds/webannotator-basic/SCHEMA/DASISH-schema.xsd");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        objectFactory = new ObjectFactory();

        // DatatypeFactory is used when converting dates
        try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

        cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        // The context is used in .execute() requests
        context = HttpClientContext.create();
        
        isLoggedIn = false;
    }
    
    /**
     * Log the user out.
     * Since we're logged in because of a session cookie,
     * this means clearing the cookies.
     * There seems to be no active request to the webserver to tell it that
     * the session is over.
     */    
    public void logout() {
    	loggedInPincipalURIString = "";
    	cookieStore.clear();
    	isLoggedIn = false;
    }

    /** 
     * Clean-up of the Web Client.
     * Don't use it any more after calling this.
     */
    public void close() {
    	logout();
    	try {
			httpclient.close();
			httpclient = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Register who wants to connect to where.
     * 
     * @param serviceURL The Service Provider to connect to (end with a /)
     * @param aIdpUrl    the Identity Provider
     * @param user       the user
     * @param password   and her password.
     */
    public boolean login(String serviceURL, String user) {
    	if (!serviceURL.endsWith("/")) {
    		serviceURL += "/";
    	}
		
		try {
			this.serviceURL = new URI(serviceURL);
			// The server will give relative URLs such as
			// /ds/webannotatornonshibb/api/targets/a355e057-eb58-4ba6-a0df-092a70afb4e2
			// Resolve these with respect to the resolveBaseURL.
			//this.resolveBaseURL = new URI(this.serviceURL.getScheme() + this.serviceURL.getAuthority());
			// Because it starts with a slash /, there is no need to chop off
			// the path from the serviceURL. This way, it will keep working if the relative URL
			// will change to start with "api/targets/....".
			this.resolveBaseURL = this.serviceURL;
		} catch (URISyntaxException e) {
			LOG.severe("Service URL is bad: URISyntaxException");
			return false;
		}
        try {
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,  
					resolveBaseURL + "SCHEMA/DASISH-schema.xsd");
		} catch (PropertyException e) {
			e.printStackTrace();
		}
        this.userName = user;
        
        // From here on we're going to assume that this.serviceURL represents
        // a valid URL.
        
        serviceURL_api_annotations = this.serviceURL.resolve("api/annotations");
        this.serviceUrlPath = this.serviceURL.getPath() + "api/";

        return reLogin();
    }
    
    /**
     * Ask the user for a password and login.
     * Repeat until there is success, or the user cancelled.
     * Could be used to automatically relogin when the server thinks
     * you are not authorized.
     * 
     * @return whether the user was logged in eventually.
     */
    public boolean reLogin() {
    	boolean success = false;
    	while (!success) {
	    	char[] password = getPassword(serviceURL, userName);
	    	if (password == null) {
				LOG.warning("password == null, user probably hit CANCEL in getPassword() dialog");
	    		return false;
	    	}
	    	success = reLogin(userName, password);
	    	if (!success) {
	    		LOG.severe("reLogin(userName, password) failed; password is probably incorrect");
	    	}
    	}
    	return success;
    }

    /**
     * Puts up a dialog and asks the user for a password, or cancel.
     * 
     * @param serviceURL
     * @param username
     * @return null if the user cancelled, a char[] with password otherwise.
     */
    private char[] getPassword(URI serviceURL, String username) {
    	// Enter password for %s:
    	String prompt = String.format(ElanLocale.getString("CommentViewer.EnterPassword"),
    							      username);
    	JPanel panel = new JPanel();
    	JLabel label = new JLabel(prompt);
    	JPasswordField pass = new JPasswordField(16);
    	panel.add(label);
    	panel.add(pass);
    	String[] options = new String[] {
    			ElanLocale.getString("Button.OK"),
    			ElanLocale.getString("Button.Cancel")
    		};
    	int option = JOptionPane.showOptionDialog(null, panel, serviceURL.toString(),
    	                         JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
    	                         null, options, options[0]);
    	if (option == 0) // pressing OK button
    	{
    	    return pass.getPassword();
    	}
    	return null;
    }

    /**
     * Log the user in with a given password, and find out their identity
     * as seen by the server (their PrincipalURI).
     * @param user
     * @param password
     * @return boolean whether the login was successful.
     */
    public boolean reLogin(String user, char[] password) {
        boolean loggedIn = loginByPOST("api/authentication/login",
        								user, new String(password));
        loggedIn = loggedIn && getPrincipalURI();
        
    	this.isLoggedIn = loggedIn;

    	return loggedIn;    
    }
    
    /**
     * Process the login page which is based on a POST.
     * Inspired by an Apache example:
     * https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientFormLogin.java
     * @param loginPage
     * @param user
     * @param password
     */
    private boolean loginByPOST(String loginPage, String user, String password) {
    	URI startPage = serviceURL.resolve(loginPage);
    	URI redirectedPage = null;
    	String postToPage = "j_spring_security_check"; // TODO should detect this from form/@action, if possible
    	boolean loggedIn = true;	// assume it will succeed
    	
        try {
            HttpGet httpget = new HttpGet(startPage);
            CloseableHttpResponse response1 = httpclient.execute(httpget, context);
            
            try {
                HttpEntity entity = response1.getEntity();

                if (DEBUG) {
                	System.out.printf("Login form1 GET %s =>\n%s\n", startPage, response1.getStatusLine());
                }
                EntityUtils.consume(entity);
                if (response1.getStatusLine().getStatusCode() >= HTTP_CLIENT_ERROR) {
                	warnAboutLoginProblem(response1.getStatusLine().toString());
                	return false;
                }                

                if (DEBUG) {
	                System.out.println("Initial set of cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies == null || cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
                }
                /*
                 * Get the page where we were redirected to.
                 */
                HttpHost target = context.getTargetHost();
                List<URI> redirectLocations = context.getRedirectLocations();
                redirectedPage = URIUtils.resolve(httpget.getURI(), target, redirectLocations);
                if (DEBUG) {
                	System.out.println("Final HTTP location: " + redirectedPage.toASCIIString());
                }
                // Expected to be an absolute URI

            } catch (javax.net.ssl.SSLException sslex) {
            	LOG.severe("Logging in; got SSLException. Is 'sunjce_provider.jar' necessary?");
            	sslex.printStackTrace();
            } finally {
                response1.close();
            }
            
            redirectedPage = serviceURL.resolve(postToPage);            
            
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(redirectedPage)
                    .addParameter("username", user)
                    .addParameter("password", password)
                    .addParameter("submit", "submit")
                    .build();

            CloseableHttpResponse response2 = httpclient.execute(login, context);

            try {
                HttpEntity entity = response2.getEntity();

                if (DEBUG) {
                	System.out.printf("Login form2 %s =>\n%s\n", login.toString(), response2.getStatusLine());
                }
                EntityUtils.consume(entity);
                if (response2.getStatusLine().getStatusCode() >= HTTP_CLIENT_ERROR) {
                	warnAboutLoginProblem(response1.getStatusLine().toString());
                	loggedIn = false;
                }

                if (DEBUG) {
	                System.out.println("Post logon cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
                }
            } finally {
                response2.close();
            }
        } catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
        } catch (javax.net.ssl.SSLException sslex) {
        	LOG.severe("Logging in; got SSLException. Is 'sunjce_provider.jar' necessary?");
        	sslex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
        }
        
        return loggedIn;
    }
    

    /**
     * Returns true if we seem to have logged in successfully.
     */
	public boolean isLoggedIn() {
		return this.isLoggedIn;
	}

    /**
     * Unmarshals (XML to Object) with JAXB methods.
     */
    @SuppressWarnings("unchecked")
    public <T> T unmarshal(InputStream inputStream)
            throws JAXBException {
        JAXBElement<T> doc = (JAXBElement<T>)unmarshaller.unmarshal(inputStream);
        return doc.getValue();
    }

    /**
     * Marshals (Object to XML output stream) with JAXB methods.
     * See https://jaxb.java.net/tutorial/section_4_5-Calling-marshal.html
     * @param document
     * @param os
     * @throws JAXBException
     */
    public <T> void marshal(JAXBElement<T> document, OutputStream os) throws JAXBException {
        marshaller.marshal(document, os);
    }

    /**
     * A somewhat general DELETE method.
     * Has no argument and expects no response object.
     */

    public void doDELETE(URI uri) {
        try {
            HttpDelete req = new HttpDelete(uri);

            if (DEBUG) {
            	System.out.println("DELETE: " + uri.toASCIIString());
            }
            @SuppressWarnings("resource") // consume() will close it
			HttpResponse res = httpclient.execute(req, context);
            if (DEBUG) {
            	System.out.println("response: " + res.getStatusLine());
            }

            lastStatusLine = res.getStatusLine();
            boolean shouldWarn = false;

            if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
            	isLoggedIn = false;
            	shouldWarn = true;
            } else if (lastStatusLine.getStatusCode() == HTTP_FORBIDDEN) {
            	// this comment belongs to some other user (even though we checked for that earlier)
            	// "delete"
            	warnAboutForbidden(ElanLocale.getString("CommentViewer.WarnAboutForbiddenDelete"));
            } else if (lastStatusLine.getStatusCode() >= HTTP_CLIENT_ERROR) {
            	shouldWarn = true;
            }
            EntityUtils.consume(res.getEntity());
            
            if (shouldWarn) {
            	warnAboutServerResponse(
            			ElanLocale.getString("CommentViewer.WarnAboutSR.Removing"),
            			lastStatusLine.toString());

            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A somewhat general GET method.
     * Has no data to send to the server but expects a RESponse object.
     */

	@SuppressWarnings("resource")
	public <RES> RES doGET(URI uri) {
        CloseableHttpResponse res = null;
        try {
            HttpGet req = new HttpGet(uri);
            
            if (DEBUG) {
            	System.out.println("GET: " + uri.toASCIIString());
            }
			res = httpclient.execute(req, context);
            if (DEBUG) {
            	System.out.println("response: " + res.getStatusLine());
            }
            	
            RES result = null;
            lastStatusLine = res.getStatusLine();

            if (lastStatusLine.getStatusCode() < HTTP_CLIENT_ERROR) {
				InputStream inputStream = res.getEntity().getContent();
	            if (DEBUG) {
	            	inputStream = new DebugInputStream(inputStream);
	            }
	            result = this.<RES>unmarshal(inputStream);
            } else {
            	if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
            		isLoggedIn = false;
            	}
            	warnAboutServerResponse(
            			ElanLocale.getString("CommentViewer.WarnAboutSR.Getting"),
            			lastStatusLine.toString());

            }

            return result;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            try {
            	if (res != null) {
            		EntityUtils.consume(res.getEntity());  // close()s the inputStream
            	}
			} catch (IOException e) {
			}
        }

        // If failed, return nothing.
        return null;
    }


    /**
     * A somewhat general POST or PUT method with a ARGument and RESult type.
     * The argument has already been converted to an InputStreamEntity.
     */
	@SuppressWarnings("resource") // consume() takes care of res and its inputStream
	public <RES> RES doPOSTorPUTwithResult(HttpEntityEnclosingRequestBase req, HttpEntity reqEntity ) {
        CloseableHttpResponse res = null;
        try {
            req.setEntity(reqEntity);

            // Execute the request
			res = httpclient.execute(req, context);
            if (DEBUG) {
            	System.out.println("response: " + res.getStatusLine());
            }

            RES object = null;
            lastStatusLine = res.getStatusLine();
            boolean shouldWarn = false;
            
            if (lastStatusLine.getStatusCode() < HTTP_CLIENT_ERROR) {
	            // Catch the return data stream and create an object from it.
	            InputStream inputStream = res.getEntity().getContent();
	            if (DEBUG) {
	            	inputStream = new DebugInputStream(inputStream); // print a copy to the console
	            }
	            object = this.<RES>unmarshal(inputStream);
            } else if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
            	isLoggedIn = false;
            	shouldWarn = true;
            } else if (lastStatusLine.getStatusCode() == HTTP_FORBIDDEN) {
            	// this comment belongs to some other user (even though we checked for that earlier)
            	// "modify
            	warnAboutForbidden(ElanLocale.getString("CommentViewer.WarnAboutForbiddenModify"));
            } else {
            	shouldWarn = true;
            }

            if (shouldWarn) {
            	warnAboutServerResponse(
            			ElanLocale.getString("CommentViewer.WarnAboutSR.Putting"),
            			lastStatusLine.toString());
            }

            return object;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            try {
            	if (res != null) {
            		EntityUtils.consume(res.getEntity());  // close()s the inputStream
            	}
			} catch (IOException e) {
			}
        }

        // If failed, return nothing.
        return null;
    }


    /**
     * POST or PUT a plain String.
     * 
     * @param req
     * @param textPlain
     * @return
     */
    public <RES> RES doPOSTorPUTwithResult(HttpEntityEnclosingRequestBase req, String textPlain) {
        if (DEBUG) {
        	System.out.printf("POST/PUT text/plain:\n%s\n", textPlain);
        }
        StringEntity reqEntity = new StringEntity(textPlain, ContentType.TEXT_PLAIN);
        return doPOSTorPUTwithResult(req, reqEntity);
    }

    /**
     * POST or PUT an object of type ARG, which will be converted to XML.
     * This version can do without a buffered version of je
     * (if, as here, the Entity is used for sending its value).
     */
    public <ARG, RES> RES doPOSTorPUTwithResult(HttpEntityEnclosingRequestBase req, final JAXBElement<ARG> je) {            
        ContentProducer producer = new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				try {
			        // Serialize the argument
					marshal(je, outstream);
	                if (DEBUG) {
	                	marshal(je, System.out);
	                }
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
        	
        };
        AbstractHttpEntity reqEntity = new EntityTemplate(producer);
        reqEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
        
        return doPOSTorPUTwithResult(req, reqEntity);
    }

    /**
     * POST or PUT an object of type ARG, which will be converted to XML.
     * This older version needs to buffer the marshalled version of je.
     */
    @SuppressWarnings("unused")
	private <ARG, RES> RES doPOSTorPUTwithResult_OLD(HttpEntityEnclosingRequestBase req, final JAXBElement<ARG> je) {
        // Serialize the argument
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
			marshal(je, output);
            byte []bytes = output.toByteArray();
            try {
				output.close();
			} catch (IOException e) {
			}
            output = null;  // make available for garbage collection, especially since toByteArray() above created a copy.

            AbstractHttpEntity reqEntity = new InputStreamEntity(
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    ContentType.APPLICATION_XML);
            if (DEBUG) {
            	System.out.printf("POST/PUT application/xml:\n%s\n", new String(bytes));
            }
            bytes = null;
                        
            return doPOSTorPUTwithResult(req, reqEntity);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
        
        return null;
    }

    /**
     * A somewhat general POST method with an ARGument and RESult type.
     */

    public <ARG, RES> RES doPOST(URI uri, JAXBElement<ARG> je) {
        HttpPost req = new HttpPost(uri);
        if (DEBUG) {
        	System.out.println("POST: " + uri.toASCIIString());
        }
        return doPOSTorPUTwithResult(req, je);
    }

    /**
     * A somewhat general PUT method with an ARGument and RESult type.
     */
    public <ARG, RES> RES doPUT(URI uri, JAXBElement<ARG> je) {
        HttpPut req = new HttpPut(uri);
        if (DEBUG) {
        	System.out.println("PUT: " + uri.toASCIIString());
        }
        return doPOSTorPUTwithResult(req, je);
    }

    /**
     * A somewhat general PUT method with a string argument and RESult type.
     */
    public <RES> RES doPUT(URI uri, String textPlain) {
        HttpPut req = new HttpPut(uri);
        if (DEBUG) {
        	System.out.println("PUT: " + uri.toASCIIString());
        }
        return doPOSTorPUTwithResult(req, textPlain);
    }

    public void warnAboutForbidden(String action) {
    	// Permission to %s the comment\n
    	// has been denied.\n
    	// The comment has not been modified on the server.
    	String fmt = ElanLocale.getString("CommentViewer.WarnAboutForbidden");
    	String msg = String.format(fmt, action);
		JOptionPane.showMessageDialog(null,
				msg,
				ElanLocale.getString("Message.Warning"),
                JOptionPane.ERROR_MESSAGE);
    }

    public void warnAboutServerResponse(String process, String message) {
    	// "There is a problem with %s.\n" +
    	// "The server responded:\n"
    	String fmt = ElanLocale.getString("CommentViewer.WarnAboutServerResponse");
    	String msg = String.format(fmt, process) + message;
   		LOG.severe(msg);

		JOptionPane.showMessageDialog(null,
				msg,
				ElanLocale.getString("Message.Error"),
                JOptionPane.ERROR_MESSAGE);
    }

    public void warnAboutLoginProblem(String message) {
    	warnAboutServerResponse(
    			ElanLocale.getString("CommentViewer.WarnAboutSR.Login"),
    			message);

    }

    /**
     * After logging in, we can find the URI that has been assigned to this principal.
     * To do that, GET (https://corpus1.mpi.nl/ds/webannotator-basic/)api/authentication/principal
     * 
     * @return a boolean which indicates we appear to be logged in successfully.
     */
    private boolean getPrincipalURI() {
		URI uri = serviceURL.resolve("api/authentication/principal");
		Principal p;
		
		p = doGET(uri);
		if (p != null) {
			loggedInPincipalURIString = p.getHref();
		} else {
			loggedInPincipalURIString = "";
		}
    	
		return !loggedInPincipalURIString.isEmpty();
    }

    /**
     * GET an AnnotationInfoList from a web service URI. 
     *
     *
     * api/annotations?access=read&link={uri}&matchMode=exact
     *
     * @param uri
     * @return
     */
    public AnnotationInfoList getAnnotationInfoList(URI uri) {
        try {
            URIBuilder ub;
            ub = new URIBuilder(serviceURL.toString() + "api/annotations");
            ub.addParameter("access", "read");
            ub.addParameter("link", uri.toString()); // takes care of necessary escaping
            ub.addParameter("matchMode", "exact");

            AnnotationInfoList ail = this.<AnnotationInfoList>doGET(ub.build());
            if (ail != null) {
                return ail;
            }
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }

        // If failed, return null to indicate  failure
        return null;
    }

    /**
     * Get an annotation.
     * 
     * Example URL:
     * https://corpus1.mpi.nl/ds/webannotator-basic/api/annotations/09e1ebaf-fca6-4509-ac26-362b2301f37f
     *
     * @param uri
     * @return Annotation or null.
     */
    public Annotation getAnnotation(URI uri) {
        Annotation a = this.<Annotation>doGET(uri);
        return a;
    }

    /**
     * Check if the Annotation can be modified (or not).
     * This is the case when the public permission is "write"
     * or when the logged in user occurs in the permission list with access level "write".
     * In all other cases, the Annotation is read-only.
     * 
     * @param ann
     * @return true if the Annotation is read-only.
     */
    private boolean isReadOnly(Annotation ann) {
    	PermissionList pl = ann.getPermissions();
    	
    	Access ac = pl.getPublic();
    	if (ac.value().equals("write")) {
    		return false;
    	}
    	
    	for (Permission p : pl.getPermission()) {
    		if (p.getPrincipalHref().equals(this.loggedInPincipalURIString)) {
    			return ! p.getLevel().value().equals("write"); 
    		}
    	}
    	
    	return true;
    }

    /**
     * Take an AnnotationInfoList, and look up the full information on all annotations in it.
     * Extract the CommentEnvelope out of each.
     * <p>
     * If null is passed in (presumably due to a auth or communication failure),
     * return null as well.
     * @param ail
     * @return a list of the recovered CommentEnvelopes.
     */
    private List<CommentEnvelope> getCommentEnvelopes(AnnotationInfoList ail, URI urn) {
        if (DEBUG) {
        	System.out.println("getCommentEnvelopes: for " + urn.toASCIIString());
        }
    	
        List<CommentEnvelope> lce = new ArrayList<CommentEnvelope>();
        String targetURI = null;
        
        Set<String> fetchedIDs = new HashSet<String>();

        for (AnnotationInfo ai : ail.getAnnotationInfo()) {
            String firstTarget = getFirstTarget(ai);
            if (targetURI == null) {
                targetURI = firstTarget;
            } else {
                if ( ! targetURI.equals(firstTarget)) {
                    // something potentially weird? It can happen but we try to avoid it.
                    if (DEBUG) {                    	
                    	System.err.printf("Targets not consistent: first=%s, now=%s\n", firstTarget, targetURI);
                    }
                }
            }

            URI annoref = resolveBaseURL.resolve(ai.getHref());// the (possibly relative) URI that identifies the actual annotation
            Annotation a = getAnnotation(annoref);
            AnnotationBody body;
            if (a != null && (body = a.getBody()) != null) {
	            XmlBody xb = body.getXmlBody();
	            if (xb != null) {
	                Element e = xb.getAny();
	                CommentEnvelope ce = new CommentEnvelope(e);
	                
	                String id = ce.getMessageID();
	                if (fetchedIDs.contains(id)) {
	                	// The database has multiple instances of the same ID!
	                	// This is never going to work properly unless we delete one.
	                    if (DEBUG) {
	                    	System.err.printf("Duplicate AnnotationID! %s\n", id);
	                    }
	                } else {	                	
		                // Override the URLs embedded in the message
		                // in favour of values that should always be correct;
		                // somebody may have deleted, then undeleted, the comment, which gives it
		                // a new URL and/or a new target.
		                ce.setMessageURL(a.getHref());
		                ce.setAnnotationFileURL(firstTarget);
		                // Get the server's Last Modified time.
		            	ce.setLastModifiedOnServer(a.getLastModified());
		            	// Check if we will be able to modify this comment.
		            	ce.setReadOnly(isReadOnly(a));
		
		                lce.add(ce);
		                fetchedIDs.add(id);
	                }
	            }
            }
        }

        // Remember this association for later, when we're making more Annotations on this Transcription
        if (targetURI != null) {
            if (DEBUG) {
            	System.out.printf("urnToTargetURL: %s -> %s\n", urn.toString(), targetURI);
            }
            urnToTargetURL.put(urn.toString(), targetURI);
        }

        return lce;
    }

    /**
	 * Get all comment envelopes from the server which are new to us. Don't add
	 * them yet or anything, that is for another part of code to decide. (Maybe
	 * also save all comments that are not on the server yet?)
	 * <p>
	 * We restrict the fetching of full annotations to those that seem new to
	 * us. This is based on a cached value of the lastModified date of the
	 * server. That is really just an optimization. If a comment is wrongly
	 * considered new enough here, further down the line it will be checked more
	 * thoroughly.
	 * <p>
	 * If either {@code existing} or {@code hadThemAlready} is null, it just
	 * fetches all annotations.
	 * <p>
	 * If no annotations were fetched, it returns an empty list.
	 * <p>
	 * If there is some kind of error in the process to fetch them, it returns
	 * null. This indicates that the true value of the list is unknown, and
	 * should (for instance) not be used to delete local comments.
	 * 
	 * @param urn
	 *            The URN of the Transcription
	 * @param existing
	 *            the Comments we already have (a non-modifiable list)
	 * @param hadThemAlready
	 *            pass an empty list. The list will be filled with the members
	 *            from {@code existing} that were on the server but not fetched.
	 *            This is so you can know they were not deleted.
	 * @return
	 */
    public List<CommentEnvelope> getCommentEnvelopes(URI urn,
    		List<CommentEnvelope> existing,
    		List<CommentEnvelope> hadThemAlready) {
        AnnotationInfoList ail = getAnnotationInfoList(urn);
        
        if (ail == null) {
        	return null;
        }
        
        List<AnnotationInfo> lai = ail.getAnnotationInfo();
        if (lai == null || lai.isEmpty()) {
        	return Collections.emptyList();        	
        }
        
        if (DEBUG) {
        	System.out.printf("Start with %d annotations to fetch\n", ail.getAnnotationInfo().size());
        }

        // Now check all AnnotationInfos for potential freshness.
        if (existing != null && !existing.isEmpty() && hadThemAlready != null) {
        	Map<String, CommentEnvelope> map = new HashMap<String, CommentEnvelope>();
        	
        	// make a map for quick access
        	for (CommentEnvelope ce : existing) {
        		map.put(ce.getMessageURL(), ce);        		
        	}
        	
        	// Walk through the received list and see which annotations we have already
        	Iterator<AnnotationInfo> iter = lai.iterator();
        	while (iter.hasNext()) {
        		AnnotationInfo ai = iter.next();
        		CommentEnvelope ce = map.get(ai.getHref());
        		
        		if (ce != null) {
        			XMLGregorianCalendar date = ce.getLastModifiedOnServer();
        			
        			if (date != null && date.equals(ai.getLastModified())) {
        				// Ok, we have this one already and it wasn't modified.
       					hadThemAlready.add(ce);
        				// Remove it from the set to be fetched fully.
        				iter.remove();
                        if (DEBUG) {
                        	System.out.printf("No need to fetch %s: %s\n", ai.getHref(), date.toString());
                        }
        			} else {
                        if (DEBUG) {
	        				System.out.printf("Need to fetch %s: my time: %s, server time: %s\n",
	        						ai.getHref(),
	        						(date != null? date.toString() : "NULL"),
	        						ai.getLastModified().toString());
                        }
        			}
        		} else {
                    if (DEBUG) {
	    				System.out.printf("Need to fetch (don't have the URL) %s\n",
	    						ai.getHref());
                    }
        		}
        	}
        }
        
        if (DEBUG) {
        	System.out.printf("Still %d annotations to fetch\n", ail.getAnnotationInfo().size());
        }

        List<CommentEnvelope> fetched = getCommentEnvelopes(ail, urn);
        
        return fetched;
    }


    /**
	 * Extract the first target (in DWAN-speak) from the annotationinfo. A
	 * target is the server-side representation of a source (our URI). Since we
	 * only ever add one target for each source, we will be surprised to find
	 * more than one. Do this mainly to know how the server refers to our
	 * Transcription.
	 * 
	 * @param ai
	 * @return
	 */
    private String getFirstTarget(AnnotationInfo ai) {
        ReferenceList list = ai.getTargets();
        List<String> list2 = list.getHref();
        
        if (list2 != null && list2.size() >= 1) {
            return list2.get(0);
        }
        
        return "";
    }

    /**
     * See {@link #getFirstTarget(AnnotationInfo)}.
     */
    private String getFirstTarget(Annotation replyA) {
		TargetInfoList replyTIL = replyA.getTargets();
		
		if (replyTIL != null) {
	    	List<TargetInfo> lti = replyTIL.getTargetInfo();
	    	
	    	if (lti != null && !lti.isEmpty()) {
	    		TargetInfo ti2 = lti.get(0);
	    		String ref = ti2.getHref();

	        	return ref;
	    	}
		}
		
		return null;
    }
    
    /**
     * Return whether the URL of an annotation seems to be known.
     * Also try to test whether it is still up to date wrt
     * service URL changes (but this check may be imperfect).
     * 
     * @param ce The CommentEnvelope we wish to check.
     */
    private boolean annotationURLIsKnown(CommentEnvelope ce) {
    	String ceURL = ce.getMessageURL();
    	
    	if (ceURL.startsWith("http")) {
        	// Check absolute URL
    		if (!ceURL.startsWith(serviceURL + "api/")) {
    			return false;
    		}
    	} else {
    		// serviceURL should be known by now, and absolute.
        	if (serviceURL == null || !serviceURL.isAbsolute()) {
        		return false;
        	}
        	
    		// Check relative URL
    		if (!ceURL.startsWith(serviceUrlPath)) {
    			return false;
    		}
    	}

		return true;
    }

    /**
     * Take the (possibly relative) URL from a CommentEnvelope, and resolve it
     * relative to the serviceURL. The "extra" part is added too.
     */
    public URI resolveEnvelopeURL(CommentEnvelope ce, String extra) {
    	if (!annotationURLIsKnown(ce)) {
    		return null;
    	}
    	
        try {
            URI uri = resolveBaseURL.resolve(ce.getMessageURL() + extra);
            return uri;
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get the "file name" part of a URL path.
     * 
     * Example: /ds/webannotatornonshibb/api/annotations/632911a9-9e4c-40f0-a45a-22b21e6615a9
     * => 
     * 632911a9-9e4c-40f0-a45a-22b21e6615a9
     * 
     * @param url
     * @return
     */
    private static String lastPathPart(String url) {
    	int slashPos = url.lastIndexOf('/');
    	if (slashPos > 0) {
    		return url.substring(slashPos + 1);
    	} else {
    		return url;
    	}
    }
    
    /**
     * A helper class to pass some shared variables through all functions
     * that are associated with updating an Annotation to the server.
     * This saves us from making them class fields, which would be ugly.
     */
    private static class AnnPostParams {
        String sourceURL;
        String targetURL;

    	boolean annotationURLIsKnown;
        boolean targetIsKnown;
    }
    
    private static final String TEMP_TARGET_REF = "__TEMP_TARGET_REF__";
    private static final String TEMP_ANNOTATION_REF = "__TEMP_ANNOTATION_REF__";
    
    /**
     * From our CommentEnvelope, create a JAXB-annotated data structure in DWAN terms.
     */
    private Annotation createAnnotation(CommentEnvelope ce, AnnPostParams h) {
    	// Some of the changes must not be made in the original,
    	// so make a clone.
    	CommentEnvelope cloneCE = ce.clone();
    	
        h.sourceURL = ce.getAnnotationURIBase().toString();
        h.targetURL = /*ce.getAnnotationFileURL();*/ urnToTargetURL.get(h.sourceURL);

    	h.annotationURLIsKnown = annotationURLIsKnown(ce);
        h.targetIsKnown = (h.targetURL != null && !h.targetURL.isEmpty());

    	/*
    	 * If we have some placeholder values, change them in a copy of the CommentEnvelope.
    	 * The idea is that the server will put the real values into its database,
    	 * doing a global search and replace. We will pick them up as well and put them
    	 * in our local copies of the comments.
    	 */
    	if (!h.annotationURLIsKnown) {
    		cloneCE.setMessageURL(TEMP_ANNOTATION_REF);
    	}
    	if (!h.targetIsKnown) {
    		cloneCE.setAnnotationFileURL(TEMP_TARGET_REF);
    		h.targetURL = TEMP_TARGET_REF;
    	} else {
    		ce.setAnnotationFileURL(h.targetURL);    		
    		cloneCE.setAnnotationFileURL(h.targetURL);    		
    	}
    	
        Element e;
        try {
            e = CommentManager.getElement(cloneCE);
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
            return null;
        }

        // Check what "target" the backend has associated with this
        // URI. When the URI is completely new, there is no target yet;
        // it will be generated as soon as the annotation is posted.
        // Chicken and egg problem: we want the target recorded inside
        // the annotation.
        TargetInfo ti = new TargetInfo();
        ti.setHref(h.targetURL);
        ti.setLink(h.sourceURL);
        ti.setVersion(""); // TODO?

        Annotation a = new eu.dasish.annotation.schema.Annotation();

        a.setOwnerHref(loggedInPincipalURIString);
        a.setHref(cloneCE.getMessageURL());
        a.setId(lastPathPart(cloneCE.getMessageURL())); // xml:id is a required attribute
         
        AnnotationBody aB = new AnnotationBody();
	        XmlBody xB = new XmlBody();
		        xB.setMimeType("text/xml");
		        xB.setAny(e);
        aB.setXmlBody(xB);

        a.setBody(aB);
        String headline = cloneCE.getMessage();
        headline = headline.substring(0, Math.min(40, headline.length()));
        a.setHeadline(headline);
        a.setLastModified(
        		datatypeFactory.newXMLGregorianCalendar(
        				cloneCE.getCreationDateString()));

        TargetInfoList til = new TargetInfoList();
        til.getTargetInfo().add(ti);

        a.setTargets(til);

        // Set permissions to some default. Their presence is required.
        // Make the annotation world-readable, and writable by ourselves.
        PermissionList pl = new PermissionList();
        pl.setPublic(Access.READ);	// the public may READ this
        Permission p = new Permission();
        p.setPrincipalHref(loggedInPincipalURIString);
        p.setLevel(Access.WRITE);	// the user may WRITE this
        pl.getPermission().add(p);

        a.setPermissions(pl);

        return a;
    }
    
    
    /**
     * Common code for processing the ResponseBody after trying to update 
     * an annotation on the server.
     * <p>
     * If the request was successful, it extracts the Last-Modified time from it.<br/>
     * If there was a 404, it retries with a POST to create a new Annotation.
     * 
     * @param ce
     * @param rb
     * @return rb
     */
    private ResponseBody process(CommentEnvelope ce, ResponseBody rb) {
        if (rb != null) {
            Annotation replyA = rb.getAnnotation();
            if (replyA != null) {
            	ce.setLastModifiedOnServer(replyA.getLastModified());
            }            	
        } else {
        	// Failed? Maybe somebody deleted the comment in the mean time.
        	// Retry it as a POST.
        	if (lastStatusLine.getStatusCode() == HTTP_NOT_FOUND) {
            	ce.setMessageURL(""); // make URL unknown
        		return putCommentEnvelope(ce);
        	}
        }
        
        return rb;
    }
    
    /**
     * PUT a whole Annotation. <br/>
     * This is the simple approach, but it does overwrite the permissions that may
     * have been changed outside our knowledge.
     * It also updates the target.
     */
    
    @SuppressWarnings("unused")
    private ResponseBody putAnnotation(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "");

        if (uri == null) {
        	return null;
        }
        JAXBElement<Annotation> ja = objectFactory.createAnnotation(a);

        ResponseBody rb = doPUT(uri, ja);
        return process(ce, rb);
    }

    /**
     * PUT just a Body (part of an Annotation).
     * @param ce
     * @param a
     * @param h
     * @return
     */
    private ResponseBody putBody(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "/body");
        if (uri == null) {
        	return null;
        }

        AnnotationBody aB = a.getBody();
        JAXBElement<AnnotationBody> ja = objectFactory.createAnnotationBody(aB);

        ResponseBody rb = doPUT(uri, ja);
        return process(ce, rb);        
    }
    
    /**
     * PUT just a Headline (part of an Annotation).
     * @param ce
     * @param a
     * @param h
     * @return
     */
    private ResponseBody putHeadline(CommentEnvelope ce, String headline, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "/headline");
        if (uri == null) {
            return null;
        }

        ResponseBody rb = doPUT(uri, headline);
        return process(ce, rb);
    }
    
    /**
     * Update the Annotation on the server by PUTtine the Body, and if needed,
     * also the Headline.
     * This leaves other aspects of the Annotation unchanged, in particular the
     * permissions.
     * 
     * @param ce
     * @param a
     * @param h
     * @return
     */

    private ResponseBody putBodyAndHeadline(CommentEnvelope ce, Annotation a, AnnPostParams h) {
    	ResponseBody rb = putBody(ce, a, h);
    	if (rb != null) {
            // Check if we need to update the headline too; if the change in the text is
    		// restricted to the end, it may not be necessary.
    		// Note that if the putBody() did a retry as a POST, the headline
    		// would register as correctly updated by this point.
            String headline = a.getHeadline();
            Annotation replyA = rb.getAnnotation();
            
            if (! headline.equals(replyA.getHeadline())) {
            	rb = putHeadline(ce, headline, h);
            }
        }
    	
    	return rb;
    }
        
    /**
     * POST a whole Annotation. This creates a new one at the server.
     * After it has succeeded, update our knowledge of the Annotation's URL
     * and (if needed) the Transcription's URL (which is a DWAN "Target").
     * 
     * @return the new Annotation as the server knows it now,
     *         wrapped up with some extra information which we ignore.
     */
    
    private ResponseBody postAnnotation(CommentEnvelope ce, Annotation a, AnnPostParams h) {
    	JAXBElement<Annotation> ja = objectFactory.createAnnotation(a);

    	// serviceURL + "api/annotations"
        ResponseBody rb = doPOST(serviceURL_api_annotations, ja);
        if (rb != null) {
            // Pick up the URI this annotation has been assigned.
            Annotation replyA = rb.getAnnotation();
            if (replyA != null) {
                if (DEBUG) {
                	System.out.printf("Annotation was assigned the URI %s\n", replyA.getHref());
                }
        		// Next time this annotation is saved, it will contain the
            	// server-assigned annotation URI and target URI.
            	ce.setMessageURL(replyA.getHref());
                ce.setLastModifiedOnServer(replyA.getLastModified());
            	
                // Pick up the target's URI, if it was new.
            	// Remember it globally and in the comment.
            	if (! h.targetIsKnown) {
            		String replyTarget = getFirstTarget(replyA);
            		if (replyTarget != null) {
                        if (DEBUG) {
                        	System.out.printf("Transcription was assigned the URI %s\n", replyTarget);
                        }

	            		urnToTargetURL.put(h.sourceURL, replyTarget);
	                    ce.setAnnotationFileURL(replyTarget);
	            	}
            	}            	
            }
        }
        return rb;
    }
    
    /**
	 * Copy one of our comments to the server.
	 * <p>
	 * Tries to see if it is new or a modification (in which case its URL is
	 * already known).
	 */
    public ResponseBody putCommentEnvelope(CommentEnvelope ce) {
        if (DEBUG) {
        	System.out.println("putCommentEnvelope: " + ce.toString());
        }
    	if (ce.isReadOnly()) {
        	LOG.warning("Cannot putCommentEnvelope: it is READ ONLY");
	        ce.setToBeSavedToServer(false);
    		return null;
    	}

    	AnnPostParams h = new AnnPostParams();
    	Annotation a = createAnnotation(ce, h);
    	ResponseBody rb = null;
    	
        // The URL of the comment (aka annotation) itself is kept as ColTime/@ColTimeMessageURL.
        // If this is a known comment, it would already look like an URL.
        if (h.annotationURLIsKnown) {	
        	//rb = putAnnotation(ce, a, h); // PUTs the whole object, including Permissions
        	rb = putBodyAndHeadline(ce, a, h); // leaves things like Permissions untouched
        } else {
            // If this is a new comment, POST it (the whole thing)
        	rb = postAnnotation(ce, a, h);
        }
        
        if (rb != null) {
	        // This one has been saved now.
	        ce.setToBeSavedToServer(false);
	        
	        checkActionList(ce, rb);
        }
        return rb;
    }
    
    // look at the actionList and do any CREATE_CACHED_REPRESENTATIONs.
    // Actually, the value of those is quite limited for our purposes, and a lot of work.
    // What would we actually use as a cached representation anyway?
    // And if it is there, it is useless if we can't show it to the user in some way.
     
	private void checkActionList(CommentEnvelope ce, ResponseBody rb) {
		if (ALWAYS_CREATE_CACHED_REPRESENTATION) { // for testing
			String object = ce.getAnnotationFileURL();
			createCachedRepresentation(ce, object);			
		} else {
			ActionList actionList = rb.getActionList();
			if (actionList.getAction() != null) {
				for (Action action : actionList.getAction()) {
					if (CREATE_CACHED_REPRESENTATION &&
					    action.getMessage().equals("CREATE_CACHED_REPRESENTATION")) {
						String object = action.getObject();
						createCachedRepresentation(ce, object);
					}
				}
			}
		}
	}
	
	/**
	 * Create a cached representation of the current Transcription, and register
	 * it for the given server object (given as an URL).
	 * 
	 * @param ce
	 * @param object
	 */
	private void createCachedRepresentation(CommentEnvelope ce,
			String object) {
		URI targetURI = resolveBaseURL.resolve(object + "/");
		String frag = ce.getFragment();
		if (DEBUG) {
			System.out.printf("Fragment string: %s\n", frag);
		}
		// Encode the fragment (which is likely to contain a '/') THREE times.
		// Twice because the web server always performs an URLdecode and we
		// want to have no bare / left over.
		// A third time, probably needed because Tomcat or some such driver
		// program performs an additional decode.
		String encFrag = CommentEnvelope.fragmentEncode(frag);
		if (!frag.equals(encFrag)) {
			// If a single encoding doesn't change it, two more won't do anything either.
			encFrag = CommentEnvelope.fragmentEncode(encFrag);
			encFrag = CommentEnvelope.fragmentEncode(encFrag);
		}
		URI cachedRepresentationURI = targetURI.resolve("fragment/"
					+ encFrag
					+ "/cached");
		if (DEBUG) {
			System.out.printf("URI: %s\n", cachedRepresentationURI.toASCIIString());
		}
		
		createCachedRepresentation(ce, cachedRepresentationURI);					

	}

	/**
	 * Create a cached representation of the current Transcription, and use
	 * the given URI to POST it.
	 * 
	 * @param ce
	 * @param cachedRepresentationURI
	 */
	private void createCachedRepresentation(CommentEnvelope ce,
			URI cachedRepresentationURI) {
		
		CachedRepresentationInfo cri = new CachedRepresentationInfo();
		cri.setMimeType("application/xml");
		cri.setTool("ELAN");
		cri.setType("EAF");
		cri.setHref(ce.getAnnotationFileURL());
		cri.setId(lastPathPart(ce.getAnnotationFileURL()));	// the id part of the Target URL 
		
		final JAXBElement<CachedRepresentationInfo> jcri = objectFactory.createCachedRepresentationInfo(cri);
		
		// create a multipart/mixed request with [0] = cri and [1] = the Transcription
		// then POST it

		// TODO: that is the unsaved file... put something else... like a screenshot.
		HttpEntity entity = createMultipartEntity(jcri, new File(transcription.getPathName()));
		
        HttpPost req = new HttpPost(cachedRepresentationURI);
        System.out.println("POST: " + cachedRepresentationURI.toASCIIString());
        doPOSTorPUTwithResult(req, entity);
	}

	static final String crlf = "\r\n";
	static final String boundary = generateBoundary();
	static final String boundary_line = "--" + boundary + crlf;
	static final String boundary_end = "--" + boundary + "--" + crlf + crlf;
	static final String latin1 = "ISO-8859-1"; 
	
	/**
	 * Create a multipart entity.
	 * 
	 * We do this "manually". The org.apache.http.entity.mime.MultipartEntityBuilder 
	 * always creates multipart/form-data and we really want multipart/mixed.
	 * 
	 * @param part1 a JAXBElement<?> which will be serialized
	 * @param part2 a File to be added as the second part
	 * @return the entity containing both
	 */
	private HttpEntity createMultipartEntity(
			final JAXBElement<?> part1,
			final File part2) {
        ContentProducer producer = new ContentProducer() {
        	
        	@Override
			public void writeTo(OutputStream outstream) throws IOException {
            	byte[] crlf_b = crlf.getBytes(latin1);
            	byte[] boundary_b = boundary_line.getBytes(latin1);
            	
				try {
					outstream.write(boundary_b);
					outstream.write("Content-Type: application/xml".getBytes(latin1));
					outstream.write(crlf_b);
					outstream.write(crlf_b); // empty line, ending headers
					
			        // Serialize the XML argument
					marshal(part1, outstream);
//	                if (DEBUG) {
//	                	marshal(part1, System.out);
//	                }
					outstream.write(crlf_b); // empty line, part of boundary
					outstream.write(boundary_b);
					outstream.write("Content-Type: application/octet-stream".getBytes(latin1));
					outstream.write(crlf_b);
					outstream.write(crlf_b); // empty line, ending headers
					
					// copy the file
					FileInputStream in = new FileInputStream(part2);
					byte[] buffer = new byte[4096];
					int len;
			        while ((len = in.read(buffer)) != -1) {
			            outstream.write(buffer, 0, len);
			        }
					in.close();
					buffer = null;

					outstream.write(crlf_b); // empty line, part of boundary
					outstream.write(boundary_end.getBytes(latin1)); // incl. 1 empty line
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
        };
        
        AbstractHttpEntity reqEntity = new EntityTemplate(producer);
        reqEntity.setContentType("multipart/mixed; boundary=\"" + boundary + "\"");

        return reqEntity;
	}

	/**
     * Example result: "r4DfdqFJVA9dtYH0RodeGRzUeqenbDi1GdGx_I";
     */
	
    private static String generateBoundary() {
    	// The pool of ASCII chars to be used for generating a multipart boundary.
    	final char[] MULTIPART_CHARS =		
                "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        .toCharArray();
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random();
        final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

	/**
     * Delete one of our comments from the server.
     */
    public void deleteCommentEnvelope(CommentEnvelope ce) {
        if (DEBUG) {
        	System.out.println("CommentWebClient.deleteCommentEnvelope: " + ce.toString());
        }
    	if (ce.isReadOnly()) {
        	LOG.warning("Cannot deleteCommentEnvelope: it is READ ONLY");
	        ce.setToBeSavedToServer(false);
    		return;
    	}
        URI uri = resolveEnvelopeURL(ce, "");
    	
        if (uri != null) {	
            doDELETE(uri);
        } else {
        	LOG.warning("Can't DELETE (URL unknown/out of date) id " + ce.getMessageID());
        }
    }

    /**
     * Unmarshals (XML to Object) with JAXB methods.
     */
    @SuppressWarnings({ "unchecked", "unused" })
    private <T> T unmarshal( Class<T> docClass, InputStream inputStream )
            throws JAXBException {
        String packageName = docClass.getPackage().getName();   // "eu.dasish.annotation.schema"
        JAXBContext jc = JAXBContext.newInstance( packageName );
        Unmarshaller u = jc.createUnmarshaller();
        // Up to here needs to be done only once, probably?
        JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( inputStream );
        return doc.getValue();
    }
    
    
}
