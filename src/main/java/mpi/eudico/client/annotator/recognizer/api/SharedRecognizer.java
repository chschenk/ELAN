package mpi.eudico.client.annotator.recognizer.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.TextParam;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.util.TimeFormatter;

/**
 * A shared recognizer (local network).
 * 
 * @author Han Sloetjes
 */
public class SharedRecognizer extends LocalRecognizer {
	private final int TELNET = 0;
	private final int HTTP = 1;
	
	private int shareMode = HTTP;
	
	private HttpURLConnection httpConn;
	private Socket socket;
	
	private List<String> shareMappings;
	private Map<String, String> localToShareMap;
	
	/**
	 * A recognizer mediator that connects to a REST or telnet service. 
	 */
	public SharedRecognizer() {
		super();
		shareMappings = new ArrayList<String>(10);
		localToShareMap = new HashMap<String, String>(10);
	}

	/**
	 * @param runCommand
	 */
	public SharedRecognizer(String runCommand) {
		super(runCommand);
		shareMappings = new ArrayList<String>(10);
		localToShareMap = new HashMap<String, String>(10);
		if (runCommand != null && runCommand.startsWith("telnet")) {
			shareMode = TELNET;
		}
	}
	
	
	/**
	 * Closes a connection.
	 */
	@Override
	public void dispose() {
		super.dispose();
		
		if (httpConn != null) {
			httpConn.disconnect();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ioe) {
				
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void start() {
		if (runCommand == null || runCommand.length() == 0) {
			if (host != null) {
				ClientLogger.LOG.severe("No run command found");
				host.errorOccurred("No run command found");
			}
			return;
		}
		readShares();
		switch (shareMode) {
		case TELNET:
			startTelnet();
			break;
		case HTTP:
			startHTTP();
			break;
			default:
				// return
		}
	}

	/**
	 * Creates a socket and starts a TCP/IP (telnet) session. 
	 */
	private void startTelnet() {
		try {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Closing socket failed: " + ioe.getMessage());
				}
			}
			
			isRunning = true;
			host.setProgress(-1f);
			// set the start time before possible errors can occur
			lastStartTime = System.currentTimeMillis();
			
			ClientLogger.LOG.info("Creating socket..." + runCommand);
			
			StringBuilder command = new StringBuilder(runCommand);
			if (runCommand.startsWith("telnet://")) {
				command.delete(0, 9);
			} else if (runCommand.startsWith("telnet:")) {
				command.delete(0, 7);
			}
			int index = command.lastIndexOf(":");
			int port = -1;
			if (index > -1 && index < command.length() - 1) {
				String portString = command.substring(index + 1);
				try {
					port = Integer.parseInt(portString);
				} catch (NumberFormatException nfe) {
					ClientLogger.LOG.warning("Error parsing the port number.");
				}
				command.delete(index, command.length());
			}
			String com = command.toString();
			host.appendToReport("Running command : " + com + " using port: " + port + "\n");
			ClientLogger.LOG.info("Running command : " + com + " using port: " + port);
			
			socket = new Socket(com, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new ReaderThread().start();
			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);// BufferedOutputStream, the input of the process

			// write parameters to input of process
			if (paramList != null && paramList.size() > 0) {
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.println("<PARAM xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file:avatech-call.xsd\">");
				for (Param p : paramList) {

					if (p instanceof NumParam) {
						writer.print("<param name=\"" + p.id + "\">");
						writer.print(((NumParam) p).current);
						writer.println("</param>");
					} else if (p instanceof TextParam) {
						String value = ((TextParam) p).curValue;
						if (value == null) {
							value = ((TextParam) p).defValue;
						}
						if (value == null) {
							value = ((TextParam) p).defValue;
						}
						if (value != null && value.length() > 0) {
							writer.print("<param name=\"" + p.id + "\">");
							writer.print(value);
							writer.println("</param>");
						}
					} else if (p instanceof FileParam) {						
						String path = ((FileParam) p).filePath;
						
						if (path != null && path.length() > 0) {
							writer.print("<param name=\"" + p.id + "\">");
							// in case of file protocol strip the protocol part
							if (path.startsWith("file:")) {
								path = path.substring(5);
							}
							// convert to unc path
							String uncPath = toUNC(path);
							writer.print(uncPath);
							writer.println("</param>");
						}
					}					
					
				}
				writer.println("</PARAM>");
			}
			//writer.print('\u0004');
			// don't close the writer, because it will close the socket
			//writer.close();
			new ConnectionChecker().start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Runs a recognizer through a HTTPUrlConnection to a REST interface.
	 */
	private void startHTTP() {
		try {
			isRunning = true;
			host.setProgress(-1f);
			// set the start time before possible errors can occur
			lastStartTime = System.currentTimeMillis();
			
			StringBuilder command = new StringBuilder(runCommand);
			
			
			if (paramList != null && paramList.size() > 0) {
				command.append("?");
				
				Param p = null;
				final String utf8 = "UTF-8";
				boolean firstParamAdded = false;
				
				for (int i = 0; i < paramList.size(); i++) {
					p = paramList.get(i);

					if (p instanceof NumParam) {
						if (firstParamAdded) {
							command.append("&");
						}
						command.append(URLEncoder.encode(p.id, utf8));
						command.append("=");
						command.append(((NumParam) p).current);
						firstParamAdded = true;
					} else if (p instanceof TextParam) {
						String value = ((TextParam) p).curValue;
						if (value == null) {
							value = ((TextParam) p).defValue;
						}
						if (value != null && value.length() > 0) {
							if (firstParamAdded) {
								command.append("&");
							}
							command.append(URLEncoder.encode(p.id, utf8));
							command.append("=");
							command.append(URLEncoder.encode(value, utf8));
							firstParamAdded = true;
						}					
					} else if (p instanceof FileParam) {
						String path = ((FileParam) p).filePath;
						// only add file params that have a value
						if (path != null && path.length() > 0) {
							// in case of file protocol strip the protocol part
							if (path.startsWith("file:")) {
								path = path.substring(5);
							}
							if (firstParamAdded) {
								command.append("&");
							}
							command.append(URLEncoder.encode(p.id, utf8));
							command.append("=");
							String uncPath = toUNC(path);
							command.append(URLEncoder.encode(uncPath, utf8));
							firstParamAdded = true;
						}
					}
				}
			}
			
			URL url = new URL(command.toString());
			
			if (httpConn != null) {
				httpConn.disconnect();
			}
			
			httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDefaultUseCaches(true);//??
            
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            int respCode = httpConn.getResponseCode();

            if (respCode != 200) {
    			ClientLogger.LOG.severe("Error while accessing server: " + runCommand + " : " + respCode);
    			host.appendToReport("Error while accessing server: " + runCommand + " : " + respCode + "\n");
    			host.errorOccurred("Error while accessing server: " + runCommand + " : " + respCode);

    			httpConn.disconnect();

    			return;
            }
			
            Object cont = httpConn.getContent();

            if (cont instanceof InputStream) {
                InputStream is = (InputStream) cont;
    			reader = new BufferedReader(new InputStreamReader(is));// BufferedInputStream, the output of the process
    			new ReaderThread().start();
    			new ConnectionChecker().start();
            } else {
            	ClientLogger.LOG.severe("Unknown content from server: " + cont);
            	host.appendToReport("Unknown content from server: " + cont + "\n");
            	host.errorOccurred("Unknown content from server: " + cont);
            }
	            
            
		} catch (UnsupportedEncodingException uee) {
			ClientLogger.LOG.severe("Could not run the recognizer: " + uee.getMessage());
			host.appendToReport("Could not run the recognizer: " + uee.getMessage() + "\n");
			host.errorOccurred("Could not run the recognizer: " + uee.getMessage());
		} catch (MalformedURLException mue) {
			ClientLogger.LOG.severe("Could not run the recognizer: " + mue.getMessage());
			host.appendToReport("Could not run the recognizer: " + mue.getMessage() + "\n");
			host.errorOccurred("Could not run the recognizer: " + mue.getMessage());
		} catch (IOException ioe) {
			ClientLogger.LOG.severe("Could not run the recognizer: " + ioe.getMessage());
			host.appendToReport("Could not run the recognizer: " + ioe.getMessage() + "\n");
			host.errorOccurred("Could not run the recognizer: " + ioe.getMessage());
		}
	}
	
	
	@Override
	public void stop() {
		if (isRunning) {
			isRunning = false;
			convertTiers();
			if (httpConn != null) {
				httpConn.disconnect();
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ioe) {
					
				}
			}
		}
	}

	private void readShares() {
		shareMappings.clear();
		localToShareMap.clear();
		
		if (SystemReporting.isWindows()) {
			List<String> coms = new ArrayList<String>(2);
			int[] colIndexes = new int[4];
			coms.add("net");
			coms.add("use");			
			
			ProcessBuilder pb = new ProcessBuilder(coms);
			pb.redirectErrorStream(true);
			try {
				Process proc = pb.start();
				BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				
				while ((line = procReader.readLine()) != null) {
					// relevant output is like this
					// Status       Local     Remote                    Network
					// OK           Z:        \\CORPORA\Avatech         Microsoft Windows Network
					
					if (line.length() == 0) {
						continue;
					}
					if (line.startsWith("Status")) {
						colIndexes[0] = 0;
						colIndexes[1] = line.indexOf("Local");
						colIndexes[2] = line.indexOf("Remote");
						colIndexes[3] = line.indexOf("Network");
					}
					// test if it should be included accept all? status codes
					// map even if disconnected or unavailable or empty
					if (line.startsWith("OK") || line.startsWith("Dis") || 
							line.startsWith("  ") || line.startsWith("Una")) {
						shareMappings.add(line);
					}
				}
				procReader.close();
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Could not read the network shares mappings");
			}
			// test
			//shareMappings.add("OK                     \\\\CORPORA\\Bvtech          Microsoft Windows Network");
			//shareMappings.add("             Z:        \\\\CORPORA\\Avatech         Microsoft Windows Network");
			
			for (String s : shareMappings) {
				String local = null;
				String share = null;
				if (colIndexes[1] > -1 && colIndexes[2] > colIndexes[1] && colIndexes[2] < s.length()) {
					local = s.substring(colIndexes[1], colIndexes[2]).trim();
					if (colIndexes[3] > colIndexes[2] && colIndexes[3] < s.length()) {
						share = s.substring(colIndexes[2], colIndexes[3]).trim();
					} else {
						share = s.substring(colIndexes[2], s.indexOf(" ", colIndexes[2])).trim();
					}
				} else {
					// detect otherwise??
					int colon = s.indexOf(':');
					if (colon > 1 && colon < s.length() - 1) {
						local = s.substring(colon - 1, colon + 1);
						int slash = s.indexOf('\\', colon);
						if (slash > -1 && slash < s.length() - 1) {
							share = s.substring(slash, s.indexOf(' ', slash)).trim();
						}
					}
				}
				if (local != null && local.length() > 0 && share != null) {
					localToShareMap.put(local, share.replace("\\", "/"));
				}
			}
		} else if (SystemReporting.isMacOS()) {
			List<String> coms = new ArrayList<String>(2);
			coms.add("mount");
			
			ProcessBuilder pb = new ProcessBuilder(coms);
			pb.redirectErrorStream(true);
			try {
				Process proc = pb.start();
				BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				while ((line = procReader.readLine()) != null) {
					// relevant output is like this
					// //hasloe@sun3:139/Avatech on /Volumes/Avatech (smbfs, nodev, nosuid, mounted by han)

					// test if it should be included
					if (line.startsWith("//") && line.indexOf("/Volumes") > -1) {
						shareMappings.add(line);
					}
				}
				procReader.close();
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Could not read the network shares mappings");
			}
			// test 
			//shareMappings.add("//hasloe@sun3:139/Avatech on /Volumes/Avatech (smbfs, nodev, nosuid, mounted by han)");
			for (String s : shareMappings) {
					StringBuilder builder = new StringBuilder("//");
					
					int atPos = s.indexOf("@");
					int firstSp = s.indexOf(" on ");
					
					if (atPos > -1) {
						if (firstSp > atPos + 1) {
							String host = s.substring(atPos + 1, firstSp);
							// remove port
							int colon = host.indexOf(":");
							if (colon > -1) {
								builder.append(host.substring(0, colon));
								int slash = host.indexOf("/", colon);
								if (slash > -1) {
									builder.append(host.substring(slash));
								} else {
									//builder.append("/");
								}
							} else {
								builder.append(host);
							}
						}
					} else {
						if (firstSp > -1) {
							String host = s.substring(2, firstSp);
							int colon = host.indexOf(":");
							if (colon > -1) {
								builder.append(host.substring(0, colon));
								int slash = host.indexOf("/", colon);
								if (slash > -1) {
									builder.append(host.substring(slash));
								}
							} else {
								builder.append(host);
							}
						}
						
					}
					
					// the share part should be found here
					String share = builder.toString();
					String local = null;

					int volIndex = 0;
					if (firstSp > -1) {
						volIndex = s.indexOf("/Volumes", firstSp);
						int brackIndex;
						if (volIndex > -1) {
							brackIndex = s.indexOf(" (", volIndex);
							if (brackIndex > -1) {
								local = s.substring(volIndex, brackIndex);
							} else {
								local = s.substring(volIndex).trim();//??
							}
						} // local = null						
					}// local = null

				if (share != null && local != null) {
					localToShareMap.put(local, share);
				}
			}
		}
	}
	
	private String toUNC(String path) {
		if (path == null || localToShareMap.size() == 0) {
			return path;
		}
		if (path.startsWith("///")) {
			if (SystemReporting.isMacOS() || SystemReporting.isLinux()) {
				path = path.substring(2);
			} else if (SystemReporting.isWindows()) {
				path = path.substring(3);
			}
		}
		
		Iterator<String> localsIter = localToShareMap.keySet().iterator();
		String local;
		while (localsIter.hasNext()) {
			local = localsIter.next();
			if (path.startsWith(local)) {
				return path.replaceFirst(local, localToShareMap.get(local)).replace('/', '\\');
				//return path.replaceFirst(local, localToShareMap.get(local)).replace('\\', '\/');
				//return path.replaceFirst(local, localToShareMap.get(local));
			}
		}
		
		return path;
	}
	
	/**
	 * Checks whether the socket has been closed or been idle for n seconds.
	 * 
	 * @author hasloe
	 */
	class ConnectionChecker extends Thread {
		// start time of this thread
		private long sleepTime = 5000;
		private long reportAfterIdleTime = 10000;

		/**
		 * Constructor with default sleep time and reporting time
		 */
		public ConnectionChecker() {
			super();
		}
		
		/**
		 * Constructor
		 * 
		 * @param sleepTime the sleep time for the checks to occur
		 * @param reportAfterIdleTime the idle time after which to report the idle time
		 */
		public ConnectionChecker(long sleepTime, long reportAfterIdleTime) {
			super();
			this.sleepTime = sleepTime;
			this.reportAfterIdleTime = reportAfterIdleTime;
		}

		/**
		 * Checks every n seconds
		 */
		@Override
		public void run() {
			// start with a sleep
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) {
			}
			
			while (isRunning) {
				if (socket != null ) {
					if (socket.isClosed()) {
						isRunning = false;
						ClientLogger.LOG.warning("The connection is closed.");
						host.appendToReport("The connection is closed.\n");
						host.errorOccurred("The connection is closed.");
						break;
					}
					else if (!socket.isConnected()) {
						isRunning = false;
						ClientLogger.LOG.warning("The connection is lost.");
						host.appendToReport("The connection is lost.\n");
						host.errorOccurred("The connection is lost.");
						break;
					}
					else if (socket.isInputShutdown()) {
						isRunning = false;
						ClientLogger.LOG.warning("The connection is closed.");
						host.appendToReport("The connection is closed.\n");
						host.errorOccurred("The connection is closed.");
						break;
					}
				}
				
				long idle = System.currentTimeMillis() - lastReadSucces;
				if(idle > reportAfterIdleTime) {
					ClientLogger.LOG.warning("Connection idle for: " + TimeFormatter.toSSMSString(idle) + " seconds");
					host.appendToReport("Connection idle for: " + TimeFormatter.toSSMSString(idle) + " seconds\n");
				} else {
					ClientLogger.LOG.info("Connection active, last read: " + TimeFormatter.toSSMSString(idle) + " seconds");
				}
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {
					// do anything?
					break;
				}
			}

		}
		
	}
	
}
