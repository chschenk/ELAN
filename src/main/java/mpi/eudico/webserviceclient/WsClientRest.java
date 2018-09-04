package mpi.eudico.webserviceclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ServerLogger;

/**
 * Utility class for calling REST web services.
 * 
 * @author Han Sloetjes
 *
 */
public class WsClientRest {
	//private final String boundary = "DaDa0x";
	//private final String nl = "\r\n";
	
	/**
	 * No arg constructor.
	 */
	public WsClientRest() {
		super();
	}
	
	/**
	 * Calls a web service using POST and by uploading one or more files.
	 * TODO: Unfinished!
	 * 
	 * @param urlString the url of the service
	 * @param params the parameters of the call
	 * @param requestProperties properties for the request
	 * @param files files to upload, the keys in the map are the param names to use when uploading
	 * @param pr a progress report to add to
	 * @param progListener  progress listener for monitoring the progress
	 * @param beginProg the current position of the progress
	 * @param progExtent the total progress extent for this call 
	 * @return the response as a string or null in case an error occurred
	 * 
	 * @throws IOException any io exception
	 */
	public String callServicePostMethodWithFiles(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties, Map<String, File> files, 
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethodWithFiles: the webservice url is null.\n");
			} else {
				ServerLogger.LOG.warning("callServicePostMethodWithFiles: the webservice url is null.");
			}
			return null;
		}
		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        
	        // specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // POST with files
	        if (files != null) {
	        	// specify a boundary and calculate the total length
	        	//httpConn.setFixedLengthStreamingMode((int) totalLength);
	        	//httpConn.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + boundary);
	        } else {
	        	
	        }
	        
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + pe.getMessage());
			}
			throw new IOException(pe.getMessage());
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + ioe.getMessage());
			}
			throw (ioe);
		}
		// step 3: set properties
		// step 4: create strings for the upload message, calculate size
		// step 5: start upload, monitor progress
		// step 6: read response, return content
		return null;
	}

	/**
	 * Calls a web service using POST method to upload a string object.
	 * 
	 * @param urlString (base) url of the service
	 * @param params the parameters of the call
	 * @param requestProperties properties for the request
	 * @param text string to upload
	 * @param pr a progress report to add to
	 * @param progListener  progress listener for monitoring the progress
	 * @param beginProg the current position of the progress
	 * @param progExtent the total progress extent for this call 
	 * @return the response as a string or null in case of an error
	 * 
	 * @throws IOException any io exception
	 */
	public String callServicePostMethodWithString(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties, String text, 
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethodWithString: the webservice url is null.\n");
			} else {
				ServerLogger.LOG.warning("callServicePostMethodWithString: the webservice url is null.");
			}
			return null;
		}

		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        // step 3: set properties
	        // specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // POST with String object
	        if (text != null) {
	        	// step 4: start upload, monitor progress
	        	// open connection, create output writer and write the text
	        	try {
	                Writer osw = new BufferedWriter(new OutputStreamWriter(httpConn.getOutputStream(),
	                        "UTF-8"));
	                osw.write(text);
	                if (progListener != null) {
	                	progListener.progressUpdated(this, (int) (100 * (beginProg + (progExtent / 2))), 
	                			"Upload complete, waiting for response");
	                }
	                osw.flush();
	        	} catch (IOException ioe) {
	    			if (pr != null) {
	    				pr.append("Could not upload the text: " + ioe.getMessage() + "\n");
	    			} else {
	    				ServerLogger.LOG.warning("Could not upload the text: " + ioe.getMessage());
	    			}
	    			throw(ioe);
	        	}
	        } else {
	        	if (pr != null) {
    				pr.append("There is no text to upload." + "\n");
    			} else {
    				ServerLogger.LOG.warning("There is no text to upload.");
    			}
	        }
	        
	        // step 5: read response, return content
	        int respCode = httpConn.getResponseCode();
	        if (respCode == HttpURLConnection.HTTP_OK) {
	        	BufferedReader procReader = new BufferedReader(
	        			new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        	StringBuilder builder = new StringBuilder(1000);
	        	String line = null;
	        	final String nl = "\n";
	        	while ((line = procReader.readLine()) != null) {
	        		builder.append(line);
	        		builder.append(nl);
	        	}
	        	try {
	        		procReader.close();
	        		httpConn.disconnect();//??
	        	} catch (IOException ignoreEx){}
	        	// write log or report
	        	if (pr != null) {
    				pr.append("Successfully received the response text." + "\n");
    			} else {
    				ServerLogger.LOG.warning("Successfully received the response text.");
    			}
	        	
	        	if (progListener != null) {
                	progListener.progressUpdated(this, (int) (100 * (beginProg + progExtent)), 
                			"Received the response text.");
                }
	        	return builder.toString();
	        } else {
	        	if (pr != null) {
    				pr.append("The server returned an error: " + respCode + "\n");
    			} else {
    				ServerLogger.LOG.warning("The server returned an error: " + respCode);
    			}
	        	throw new IOException("The server returned an error code: " + respCode);
	        }
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + pe.getMessage());
			}
			throw new IOException(pe.getMessage());
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + ioe.getMessage());
			}
			throw (ioe);
		}

	}

	/**
	 * Calls a service without String or File content to upload.
	 * @param urlString the url of the service
	 * @param params the parameters for the request
	 * @param requestProperties the properties for the request
	 * @param pr an optional progress report to write to
	 * @param progListener a progress listener that monitors the progresas
	 * @param beginProg the begin value of the progress
	 * @param progExtent the total extent of this sub-process in the overall process
	 * @return the returned content 
	 * 
	 * @throws IOException any io exception
	 */
	public String callServicePostMethod(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties,  
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethod: the webservice url is null.\n");
				ServerLogger.LOG.warning("callServicePostMethod: the webservice url is null.");
				return null;
			}
		}
		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        
	        // step 3: set properties / specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
				
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // step 4: connect
	        httpConn.connect();
	        
	        // step 5: read response, return content
	        int respCode = httpConn.getResponseCode();
	        // this is probably not a good general test, can't assume this
	        if (respCode == HttpURLConnection.HTTP_OK) {
	        	//System.out.println("Content-Type: " + httpConn.getContentType());// extract the charset from the content-type?
	        	BufferedReader procReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        	StringBuilder outputBuilder = new StringBuilder();
				String line = null;
				
				while ((line = procReader.readLine()) != null) {
					outputBuilder.append(line);
				}
				
				try {
					procReader.close();
				} catch (Throwable t) {
					// catch whatever
				}
				
	        	if (pr != null) {
	        		pr.append("Succesfully connected to the server: " + respCode + "\n");
	        	} else {
	        		ServerLogger.LOG.warning("Succesfully connected to the server: " + respCode);
	        	}
	        	
	        	if (progListener != null) {
	        		progListener.progressUpdated(this, (int)(beginProg + progExtent), "Succesfully connected to the server");
	        	}
	        	
				return outputBuilder.toString();
	        } else {
	        	if (pr != null) {
	        		pr.append("Server returned error code: " + respCode + "\n");
	        	} else {
	        		ServerLogger.LOG.warning("Server returned error code: " + respCode);
	        	}
	        	throw new IOException("The server returned an error code: " + respCode);
	        }
	        
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + pe.getMessage());
			}
			throw new IOException(pe.getMessage());
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + ioe.getMessage());
			}
			throw (ioe);
		}
	}

	/**
	 * Calls a web service using the GET method.
	 * 
	 * @param urlString the url of the service
	 * @param params the request parameters
	 * @param requestProperties request properties
	 * @param pr a progress report
	 * @param progListener a progress listener
	 * @param beginProg the current overall progress, at the beginning of this method
	 * @param progExtent the total progress extent of this call
	 * 
	 * @return the retrieved document as a String or null in case of an error
	 * 
	 * @throws IOException an IOException
	 */
	public String callServiceGetMethod(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties,  
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServiceGetMethod: the webservice url string is null.\n");
			} else {
				ServerLogger.LOG.warning("callServiceGetMethod: the webservice url string is null.");
			}
			return null;
		}
		URL url = createFullURL(urlString, params, pr);
		if (url == null) {
			if (pr != null) {
				pr.append("callServiceGetMethod: the webservice url is null.\n");
			} else {
				ServerLogger.LOG.warning("callServiceGetMethod: the webservice url is null.");
			}
			return null;
		}
		
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        //httpConn.setDoInput(false);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("GET");
	        httpConn.setInstanceFollowRedirects( false );
	        
	        // step 3: set properties / specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
				
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // step 4: connect
	        httpConn.connect();
	        
	        // step 5: read response, return content
	        int respCode = httpConn.getResponseCode();
	        // this is probably not a good general test, can't assume this
	        if (respCode == HttpURLConnection.HTTP_OK) {
	        	//System.out.println("Content-Type: " + httpConn.getContentType());// extract the charset from the content-type?
	        	BufferedReader procReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        	StringBuilder outputBuilder = new StringBuilder();
				String line = null;
				
				while ((line = procReader.readLine()) != null) {
					outputBuilder.append(line);
				}
				
				try {
					procReader.close();
				} catch (Throwable t) {
					// catch whatever
				}
				
	        	if (pr != null) {
	        		pr.append("Succesfully connected to the server: " + respCode + "\n");
	        	} else {
	        		ServerLogger.LOG.warning("Succesfully connected to the server: " + respCode);
	        	}
	        	
	        	if (progListener != null) {
	        		progListener.progressUpdated(this, (int)(beginProg + progExtent), "Succesfully connected to the server");
	        	}
	        	
				return outputBuilder.toString();
	        } else {
	        	if (pr != null) {
	        		pr.append("Server returned error code: " + respCode + "\n");
	        	} else {
	        		ServerLogger.LOG.warning("Server returned error code: " + respCode);
	        	}
	        	throw new IOException("The server returned an error code: " + respCode);
	        }
	        
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + pe.getMessage());
			} 
			throw new IOException(pe.getMessage()); 
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				ServerLogger.LOG.warning("Could not contact the server: " + ioe.getMessage());
			}
			throw (ioe);
		}
	}
	
	/**
	 * 
	 * @param urlString the url of the service (assumed to be not null)
	 * @param params the request parameters
	 * @param pr a progress report
	 * 
	 * @return a (parameterized) URL or null in case of an error
	 */
	private URL createFullURL(String urlString, Map<String, String> params, ProcessReport pr) {
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					ServerLogger.LOG.warning("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		
		return url;
	}	
}
