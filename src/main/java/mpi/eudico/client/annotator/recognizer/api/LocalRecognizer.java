package mpi.eudico.client.annotator.recognizer.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.TextParam;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.XMLEscape;
/**
 * This class sort of combines a Recognizer and a RecognizerBundle
 * and is responsible for creating a Process for the Recognizer executable and sending receiving messages.
 * 
 * @author Han Sloetjes
 * @updated Sep 2012, aarsom
 *
 */
public class LocalRecognizer implements Recognizer {
	protected RecognizerHost host;
	protected String runCommand;
	protected List<Param> paramList;
	protected List<String> mediaPaths;
	protected String name;
	protected String id;// the short, internal identifier string from the cmdi
	protected int recognizerType;
	protected File baseDir;
	
	protected Process process;
	protected OutputStream outStream;
	protected InputStream inStream;
	protected BufferedReader reader;
	protected boolean isRunning = false;
	protected long lastStartTime = 0L;
	protected volatile long lastReadSucces = 0L;
	
	public LocalRecognizer() {
		// empty
	}

	/**
	 * 
	 * @param runCommand the command to run
	 */
	public LocalRecognizer(String runCommand) {
		this.runCommand = runCommand;
	}
	
	/**
	 * Assumes the passed list is uniquely used by this recognizer (i.e. it is a cloned list).
	 * 
	 * @param paramList the list of parameters
	 */
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	
	/**
	 * Returns whether the recognizer is capable of combining patterns in more than one file.
	 */
	@Override
	public boolean canCombineMultipleFiles() {
		if (paramList == null || paramList.isEmpty()) {
			return false;
		}
		int numAudio = 0;
		int numVideo = 0;
		
		for (Param p : paramList) {
			if (p instanceof FileParam) {
				FileParam fp = (FileParam) p;
				if (fp.ioType == FileParam.IN && fp.contentType == FileParam.AUDIO) {
					numAudio++;
				} else if (fp.ioType == FileParam.IN && fp.contentType == FileParam.VIDEO) {
					numVideo++;
				}
			}
		}
		
		return numAudio + numVideo > 1;
	}	
	
	@Override
	public boolean setMedia(List<String> mediaFilePaths) {
		return false;
	}

	@Override
	public boolean canHandleMedia(String mediaFilePath) {
		return true;
	}

	@Override
	public void dispose() {
		// destroy a running process?
		// close reader and writer?
	}

	@Override
	public JPanel getControlPanel() {
		// create and return a custom made panel?
		return null;
	}
	
	@Override
	public void validateParameters() throws RecognizerConfigurationException {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getParameterValue(String param) {
		if (param == null) {
			return null;
		}
		
		if (paramList != null && !paramList.isEmpty()) {
			for (Param p : paramList) {
				if (p.id != null && p.id.equals(param)) {
					if (p instanceof NumParam) {
						if (((NumParam) p).current > Float.MIN_VALUE) {
							return new Float(((NumParam) p).current);
						} else {
							return new Float(((NumParam) p).def);
						}
					} else if (p instanceof TextParam) {
						String val = ((TextParam) p).curValue;
						if (val == null) {
							return ((TextParam) p).defValue;
						} else {
							return val;
						}
					} else if (p instanceof FileParam) {
						return ((FileParam) p).filePath;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Returns the type
	 * @return the type
	 */
	@Override
	public int getRecognizerType() {
		return recognizerType;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type one of the predefined types:
	 * {@link Recognizer#AUDIO_TYPE},
	 * {@link Recognizer#VIDEO_TYPE} or
	 * {@link Recognizer#OTHER_TYPE}.
	 */
	public void setRecognizerType(int type) {
		recognizerType = type;//omit checking
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the user specified value for a parameter.
	 */
	@Override
	public void setParameterValue(String param, String value) {
		if (paramList != null && !paramList.isEmpty()) {
			for (Param p : paramList) {
				if (p.id != null && p.id.equals(param)) {
					if (p instanceof TextParam) {
						((TextParam) p).curValue = value;
					} else if (p instanceof FileParam) {
						((FileParam) p).filePath = value;
					}
					break;
				}
			}
		}
	}

	/**
	 * Sets the user specified value for a parameter.
	 */
	@Override
	public void setParameterValue(String param, float value) {
		if (paramList != null && !paramList.isEmpty()) {
			for (Param p : paramList) {
				if (p.id != null && p.id.equals(param)) {
					if (p instanceof NumParam) {
						((NumParam) p).current = value;
					}
					break;
				}
			}
		}
	}

	/**
	 * Sets the host of the recognizer.
	 */
	@Override
	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;
	}

	/**
	 * Starts execution of the recognizer.
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

		try {
			isRunning = true;
			host.setProgress(-1f);
			// round to whole seconds, some OS's do that for the last modified flag
			lastStartTime = (System.currentTimeMillis() / 1000) * 1000;
			
			StringTokenizer tokenizer = new StringTokenizer(runCommand);
			List<String> cmds = new ArrayList<String>();
			while (tokenizer.hasMoreTokens()) {
				cmds.add(tokenizer.nextToken());
			}
			ProcessBuilder pBuilder = new ProcessBuilder(cmds);
			pBuilder.redirectErrorStream(true);
			pBuilder.directory(baseDir);
			ClientLogger.LOG.info("Setting directory: " + baseDir);
			//pBuilder.environment().put("MYPRAAT", "/Programma/Praat.app/Contents/MacOS/Praat");
			//String[] env = new String[]{"MYPRAAT=/Programma/Praat.app/Contents/MacOS/Praat"};// /Programma/Praat.app/Contents/MacOS/Praat
			process = pBuilder.start();
			ClientLogger.LOG.info("Created process... command: " + runCommand);
			//process = Runtime.getRuntime().exec(runCommand, null, baseDir);//runCommand
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));// BufferedInputStream, the output of the process
			new ReaderThread().start();
			XMLEscape xmlEscape = new XMLEscape();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);// BufferedOutputStream, the input of the process
			//writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
			// write parameters to input of process
			if (paramList != null && paramList.size() > 0) {
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.println("<PARAM xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file:avatech-call.xsd\">");
				// HS July 2014 write a InvocationContext param 
				// accept the formatted (Java 1.6) string as is: 2014-07-31 14:23:09+0200
				String timestamp = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssZ").format(new Date());
				writer.print("<param name=\"InvocationContext\">");
				writer.print( (id != null ? xmlEscape.escape(id) : "Unknown") );
				writer.print(" " + timestamp);
				writer.println("</param>");
				
				for (Param p : paramList) {
					writer.print("<param name=\"" + p.id + "\">");
					if (p instanceof NumParam) {
						writer.print(((NumParam) p).current);
					} else if (p instanceof TextParam) {
						String value = ((TextParam) p).curValue;
						if (value == null) {
							value = ((TextParam) p).defValue;
						}

						if (value != null && value.length() > 0) {
							writer.print(xmlEscape.escape(value));
						}
						
					} else if (p instanceof FileParam) {
						String path = ((FileParam) p).filePath;
						if(path != null){
							// in case of file protocol strip the protocol part
							if (path.startsWith("file:")) {
								path = path.substring(5);
							}
							if (path.length() > 5) {
								if (path.substring(0, 5).matches("///[a-zA-Z]:")) {
									// assume Windows
									path = path.substring(3).replace('/', '\\');// remove the 3 backslashes and replace slashes
								}
							}
							writer.print(xmlEscape.escape(path));
						}
					}					
					writer.println("</param>");
				}
				writer.println("</PARAM>");
			}
			//writer.print('\u0004');
			writer.close();
			
			
			
			// create threads for reading and writing
			//new ReaderThread().start();
			//System.out.println("In: " + process.getInputStream().getClass().getName());
			//System.out.println("Out: " + process.getOutputStream().getClass().getName());
		} catch (IOException ioe) {
			final String msg = "Could not run the recognizer: " + ioe.getMessage();
			ClientLogger.LOG.severe(msg);
			host.appendToReport(msg + '\n');
			host.errorOccurred(msg);
		}
		
	}

	@Override
	public void stop() {
		// if there is a process stop it?
		if (isRunning && process != null) {			
			// send a message first? Is there a way of closing gracefully?
			ClientLogger.LOG.info("Stopping recognizer...");
			process.destroy();
			isRunning = false;
			convertTiers();
		}

	}
	
	/**
	 * Convert the output segmentation to a segmentation in the signal viewer.
	 * However there are at least 2 reasons why this should not be done:<ol>
	 * <li>calls methods that must run on the GUI thread
	 * <li>is done again by the AbstractRecognizerPanel.</ol> 
	 */
	protected void convertTiers() {
		return;
		
//		if (paramList != null && paramList.size() > 0) {
//			for (Param p : paramList) {
//				if (p instanceof FileParam) {
//					FileParam fileParam = (FileParam) p;
//					if (fileParam.ioType == FileParam.OUT) {
//						if (fileParam.filePath == null || fileParam.filePath.length() == 0) {
//							if (!fileParam.optional) {
//								if (host != null) {
//									host.appendToReport("Warning: no output file found for parameter: " + fileParam.id + '\n');
//								}
//							}
//							continue;
//						}
//						if (fileParam.contentType == FileParam.CSV_TIER) {
//							File csvFile = new File(fileParam.filePath);
//							if (csvFile.exists() && csvFile.canRead() && csvFile.lastModified() > lastStartTime) {
//								CsvTierIO cio = new CsvTierIO();
//								List<Segmentation> segm = cio.read(csvFile);
//								if (segm != null && segm.size() > 0) {
//									for (Segmentation s : segm) {
//										host.addSegmentation(s);
//									}
//								}
//							}
//						} else if (fileParam.contentType == FileParam.TIER || fileParam.contentType == FileParam.MULTITIER) {
//							File xmlFile = new File(fileParam.filePath);
//							if (xmlFile.exists() && xmlFile.canRead() && xmlFile.lastModified() >= lastStartTime) {
//								XmlTierIO xio = new XmlTierIO(xmlFile);
//								List<Segmentation> segm = null;
//								try {
//									segm = xio.parse();
//								} catch (Exception exe){
//									JOptionPane.showMessageDialog(null, exe.getMessage(), 
//											ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
//								}
//								if (segm != null && segm.size() > 0) {
//									for (Segmentation s : segm) {
//										host.addSegmentation(s);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
	}
	
	
	@Override
	public void updateLocale(Locale locale) {
		// stub
	}
	
	@Override
	public void updateLocaleBundle(ResourceBundle bundle) {
		// stub
		
	}

	public String getRunCommand() {
		return runCommand;
	}

	public void setRunCommand(String runCommand) {
		this.runCommand = runCommand;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	/**
	 * The id string from the cmdi. 
	 * Might move to the Recognizer interface once api changes are planned.
	 * 
	 * @return the shorthand identifier string
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Internal thread class for reading messages from the recognizer.
	 * 
	 * @author Han Sloetjes
	 */
	class ReaderThread extends Thread {
		private final String DONE = "RESULT: DONE.";
		private final String FAIL = "RESULT: FAILED.";
		private final String PROG = "PROGRESS:";
		private final String PROG2 = "INFO: PROGRESS:";
		//private final String INFO = "INFO:";
		//private final String RESULT = "RESULT:";		
		private float lastProg = -1f;	
		
		private float convertToFloat(String progValue){
			float prog = -1;
			try {
				//checks if the the progValue is a % 
				if(progValue.endsWith("%")){										
					prog  = Float.parseFloat(progValue.substring(0,progValue.length()-1));
					prog = prog/100;
				} else{
					prog = Float.parseFloat(progValue);
				}
			} catch (NumberFormatException nfe) {}
			
			return prog;
		}
		
		@Override
		public void run() {
			//System.out.println("Start Reading...");
			while (isRunning && reader != null) {
				try {
						String line = reader.readLine();	
						//System.out.println("Read: " + line);
						if (line != null) {
							lastReadSucces = System.currentTimeMillis();
							host.appendToReport(line + "\n");
							
							if (line.equals(DONE)) {
								convertTiers();
								host.setProgress(1.0f);
								break;
							} else if (line.equals(FAIL)) {
								//convertTiers();//??
								ClientLogger.LOG.warning("Recognizer failed...");
								host.errorOccurred("Recognizer failed.");
								break;
							} else if (line.startsWith(PROG) || line.startsWith(PROG2) ) {
								int colIndex = line.indexOf(':');
								if(line.startsWith(PROG2)){
									colIndex = line.indexOf(':', colIndex);
								}
								
								String message = null;
								float prog = -1;
								
								if (colIndex < line.length() - 2) {
									String remains = line.substring(colIndex + 1).trim();
									int space = remains.indexOf(' ');	
									
									// if has a message
									if (space > -1) {										
										prog = convertToFloat(remains.substring(0, space));
										
										if (space < remains.length() - 1) {
											message = remains.substring(space).trim();
										}										
									} else {										
										prog = convertToFloat(remains);
									}

									if(prog > lastProg){
										lastProg = prog;
									} else {
										prog = lastProg;
									}
									
									host.setProgress(prog, message);
								}
							}
							// not meant for progress info
							/*else if (line.startsWith(INFO) || line.startsWith(RESULT)) {
								host.setProgress(lastProg, line);
							}*/
							// try to detect an error in a recognizer, end of file, end of transmission codes
							else if (line.length() == 1) {
								if (line.charAt(0) == '\u0004') {// end of transmission
									ClientLogger.LOG.warning("Recognizer failed... end of transmission");
									host.errorOccurred("Recognizer failed, end of transmission.");
									host.appendToReport("Recognizer failed, end of transmission.\n");
									break;
								} else {
									try {
										int eof = Integer.parseInt(line);
										if (eof == -1) {// != 0 ??
											ClientLogger.LOG.warning("Recognizer failed... end of transmission");
											host.errorOccurred("Recognizer failed, end of transmission.");
											host.appendToReport("Recognizer failed, end of transmission.\n");
											break;
										} else if (eof == 0){
											ClientLogger.LOG.info("Recognizer terminated successfully, but did not send the corresponding RESULT code");
											host.appendToReport("Recognizer terminated successfully...\n");
											host.setProgress(1.0f);
											break;
										}
									} catch (NumberFormatException nfe) {
										//ignore
									}
								}
							}
						} else {
							// end of stream
							ClientLogger.LOG.warning("Recognizer stopped; unexpected end of transmission.");
							host.errorOccurred("Recognizer stopped; unexpected end of transmission.");
							host.appendToReport("Recognizer stopped; unexpected end of transmission.\n");
							break;
						}
				} catch (IOException ioe) {
					ClientLogger.LOG.info("Exception while reading the recognizer output: " + ioe.getMessage());
					// break;??
				}
			}
			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					// ignore
				}
			}
			isRunning = false;
		}
	}

}
