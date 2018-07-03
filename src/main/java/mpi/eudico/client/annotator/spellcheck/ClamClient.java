package mpi.eudico.client.annotator.spellcheck;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * This utility class can be used to connect to CLAM-based webservices (https://proycon.github.io/clam/)
 * @author michahulsbosch
 *
 */
public class ClamClient  implements WebServiceClient {
	/* The settings of the CLAM webservice */
	String host;
	HttpHost httpHost;
	protected String basePath;
	HttpClientContext context;
	
	/* The credetials of the CLAM webservice */
	private AuthenticationProtocol protocol;
	private String username;
	private String password;
	
	private String scheme;
	
	/* The HTTP client that is used for communicating with the CLAM webservice */
	private CloseableHttpClient httpclient;
	
	protected enum Method {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	/**
	 * Setup the CLAM client
	 * TODO Add stuff to set the authentication method (OAUTH or DIGEST). Using DIGEST for now. 
	 * @param host 
	 * @param scheme 
	 * @param path
	 * @param username
	 * @param password
	 */
	public ClamClient(String host, String scheme, String path, String username, String password) {
		this.host = host;
		this.scheme = scheme;
		this.basePath = path.endsWith("/") ? path.substring(0, path.length()-1) : path; // Strip trailing "/"
		this.username = username;
		this.password = password;
		
		protocol = AuthenticationProtocol.DIGEST; // TODO this default for now
		
		//initialize();
	}
	
	/**
	 * Initializes the Clam client 
	 * @param scheme
	 * @param host
	 */
	public void initialize() {
		createOrUpdateHttpHost();
		
		creatOrUpdateContext();
		
		httpclient = HttpClients.createDefault();
	}
	
	private void createOrUpdateHttpHost() {
		int port = scheme.equals("https") ? 443 : 80;
		httpHost = new HttpHost(host, port, scheme);
	}
	
	/**
	 * Sets the HttpClientContext, e.g. the authentication stuff
	 */
	private void creatOrUpdateContext() {
		if(protocol.equals(AuthenticationProtocol.DIGEST)) {
			// Setup the DIGEST authentication
			final CredentialsProvider credsProvider = new BasicCredentialsProvider();
		    credsProvider.setCredentials(AuthScope.ANY,
		            new UsernamePasswordCredentials(this.username, this.password));
		    final AuthCache authCache = new BasicAuthCache();
		    DigestScheme digestAuth = new DigestScheme();
		    authCache.put(httpHost, digestAuth);
		    
		    // Create and configure execution context //// TODO must probably be outside if block
		    context = HttpClientContext.create();
		    context.setCredentialsProvider(credsProvider);
		    context.setAuthCache(authCache);
		} else if(protocol.equals(AuthenticationProtocol.OAUTH)) {
			// TODO
		}
	}
	
	/**
	 * Creates and executes a request
	 * TODO Put data in the request
	 * @param method
	 * @param fullPath
	 * @param data
	 */
	protected String request(Method method, String fullPath, List<NameValuePair> data) {
		String body = null;
		try {
			HttpRequestBase request = null;
			switch (method) {
				case GET:
					if(data != null && !data.isEmpty()) {
						fullPath = fullPath.endsWith("?") ? fullPath : fullPath + "?";
						fullPath += URLEncodedUtils.format(data, "utf-8");
					}
					request = new HttpGet(fullPath);
					break;
				case POST:
					HttpPost httpPost = new HttpPost(fullPath);
					httpPost.setEntity(new UrlEncodedFormEntity(data));
					request = httpPost;
					break;
				case PUT:
					request = new HttpPut(fullPath);
					break;
				case DELETE:
					request = new HttpDelete(fullPath);
					break;
			}
			CloseableHttpResponse response = httpclient.execute(httpHost, request, context);
			HttpEntity entity = response.getEntity();
			body = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error in the http protocol when connecting to " + fullPath + " (" + e.getMessage() + ")");
            }
		} catch (IOException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error connecting to " + fullPath + " (" + e.getMessage() + ")");
            }
		}
		return body;
	}
	
	/**
	 * Creates a project in the CLAM webservice
	 * TODO Report the response 
	 * @param projectName
	 */
	public void createProject(String projectName) {
		request(Method.PUT, basePath + "/" + projectName, null);
	}
	
	/**
	 * Deletes a project in the CLAM webservice
	 * TODO Report the response
	 * @param projectName
	 */
	public void deleteProject(String projectName) {
		request(Method.DELETE, basePath + "/" + projectName, null);
	}
	
	/**
	 * Uploads text to the CLAM webservice which puts it in a file for further processing
	 * TODO Report the response
	 * @param fileName
	 */
	public void uploadText(String projectName, String fileName, String text) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(makeNameValuePair("inputtemplate", "textinput"));
		data.add(makeNameValuePair("contents", text));
		
		request(Method.POST, basePath + "/" + projectName + "/input/" + fileName, data);
	}
	
	/**
	 * Start project execution
	 * @param projectName
	 * @param sensitivity
	 */
	public void startProject(String projectName, String sensitivity) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(makeNameValuePair("sensitivity", sensitivity));
		
		request(Method.POST, basePath + "/" + projectName, data);
	}
	
	/**
	 * Polls a project for status
	 * @param projectName
	 */
	public void pollProject(String projectName) {
		String body = request(Method.GET, basePath + "/" + projectName, null);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse( new InputSource( new StringReader( body ) ) );
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate("/clam/status/@errors",
			        document.getDocumentElement(), XPathConstants.NODESET);
		} catch (ParserConfigurationException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error in parser configuration when parsing XML from " + basePath + " (" + e.getMessage() + ")");
            }
		} catch (SAXException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error when parsing XML from " + basePath + " (" + e.getMessage() + ")");
            }
		} catch (IOException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error when connecting to " + basePath + " (" + e.getMessage() + ")");
            }
		} catch (XPathExpressionException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error in XPath expression when extracting nodes on " + basePath + " (" + e.getMessage() + ")");
            }
		}
	}
	
	public void retrieveOutput(String projectName, String outputFile) {
		String body = request(Method.GET, basePath + "/" + projectName + "/output/" + outputFile, null);
		// TODO body is FoLiA XML; do something useful with it
	}
	
	/**
	 * Util method for making a NameValuePair
	 * @param name
	 * @param value
	 * @return
	 */
	public static NameValuePair makeNameValuePair(final String name, final String value) {
		return new NameValuePair() {
			
			@Override
			public String getValue() {
				return value;
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}

	@Override
	public void setHost(HttpHost host) {
		httpHost = host;
	}

	@Override
	public HttpHost getHost() {
		return httpHost;
	}

	@Override
	public void setPath(String path) {
		basePath = path;
	}

	@Override
	public String getPath() {
		return basePath;
	}

	@Override
	public void setUsername(String username) {
		if (!this.username.equals(username)) {
			this.username = username;
			creatOrUpdateContext();
		}
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setPassword(String password) {
		if (!this.password.equals(password)) {
			this.password = password;
			creatOrUpdateContext();
		}
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setSecure(Boolean ssl) {
		String scheme = ssl ? "https" : "http";
		if (this.scheme != scheme) {
			this.scheme = scheme;
			createOrUpdateHttpHost();
		}
	}

	@Override
	public Boolean getSecure() {
		return scheme.equals("https");
	}

	@Override
	public void setAuthenticationProtocol(AuthenticationProtocol protocol) {
		if(this.protocol != protocol) {
			this.protocol = protocol;
			creatOrUpdateContext();
		}
	}

	@Override
	public AuthenticationProtocol getAuthenticationProtocol() {
		return protocol;
	}

	@Override
	public void setAuthentication(AuthenticationProtocol protocol, String username, String password) {
		if(this.protocol != protocol ||
				!this.username.equals(username) ||
				!this.password.equals(password)) {
			this.protocol = protocol;
			this.username = username;
			this.password = password;
			creatOrUpdateContext();
		}
	}
}
