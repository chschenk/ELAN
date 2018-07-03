package mpi.eudico.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;


/**
 * A logging Handler class that redirects the  System.out and System.err to the
 * same file as the logging messages. It imitates the
 * java.util.logging.FileHandler class to a certain extent. It supports part
 * of the properties of the FileHandler class.  See the javadoc of
 * java.util.logging.FileHandler. These properties are <b>not</b> supported by
 * the ErrOutLogFileHandler:
 * 
 * <ul>
 * <li>
 * filter (no filtering)
 * </li>
 * <li>
 * encoding (default encoding)
 * </li>
 * <li>
 * count (defaults to 1)
 * </li>
 * <li>
 * append (defaults to false)
 * </li>
 * <li>
 * limit (no file size limit)
 * </li>
 * <li>
 * %g (generation) in the pattern
 * </li>
 * </ul>
 * 
 * Leaves the following ErrOutLogFileHandler properties:
 * 
 * <ul>
 * <li>
 * mpi.library.util.ErrOutLogFileHandler.pattern<br> specifies a pattern for
 * generating the output file name.  See the FileHandler's javadoc.
 * </li>
 * <li>
 * mpi.library.util.ErrOutLogFileHandler.level<br>     the logging Level. Only
 * applicable to the logging messages; prints to System.err and System.out are
 * always written to the file.
 * </li>
 * <li>
 * mpi.library.util.ErrOutLogFileHandler.formatter <br>
 * the Formatter for this logging handler. Default is SimpleFormatter. Using
 * an  XMLFormatter is of no use because real Log messages and
 * System.out.print messages  end up, intermingled, in the same file, so the
 * log file will not be a valid  xml file.
 * </li>
 * </ul>
 */
public class ErrOutLogFileHandler extends StreamHandler {
    /** default file pattern */
    public static final String DEFAULT_LOG_FILE_PATTERN = "%h/mpitools%u.log";

    /** max. number of unique log files */
    protected static final int MAX_UNIQUE_FILES = 30;

    /** the log manager */
    protected LogManager manager = LogManager.getLogManager();

    // private boolean append;
    // private int limit;       // zero => no limit.
    // protected int count;

    /** the file pattern */
    protected String pattern;

    /** the LogRecord formatter */
    protected Formatter formatter;

    /** the log level */
    protected Level level;

    /** the default System.out printstream */
    protected PrintStream origOut;

    /** the default System.err printstream */
    protected PrintStream origErr;

    /** the file outputstream */
    protected FileOutputStream outStream;

    /** the file to write to */
    protected File file;
    /** the file to write to and read from */
    //private RandomAccessFile raf;
    //private FileInputStream inStream;
    private FileChannel rfc;
    private long logStartPoint = 0;


    /** the printstream that replaces the default System.out and System.err 
     * printstream */
    protected PrintStream printStream;

    /**
     * Creates a new ErrOutLogFileHandler obtaining all configuration options
     * from a config file, or using default values when no config file has
     * been specified.
     *
     * @throws IOException
     * @throws SecurityException
     */
    public ErrOutLogFileHandler() throws IOException, SecurityException {
        checkLogAccess();
        readConfiguration();

        initHandler();
    }

    /**
     * Creates a ErrOutLogFileHandler with the specified pattern overruling the
     * pattern from the configfile.
     *
     * @param pattern the file pattern
     *
     * @throws IOException
     * @throws SecurityException
     */
    public ErrOutLogFileHandler(String pattern)
        throws IOException, SecurityException {
        checkLogAccess();
        readConfiguration();

        if ((pattern != null) && (pattern.length() > 0)) {
            this.pattern = pattern;
        }

        initHandler();
    }

    /**
     * The publish method receives all generated LogRecords. 
     * The handling (writing) is simply forwarded to the super class
     * (StreamHandler)
     *
     * @param record the log record
     */
    @Override
	public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }        

        super.publish(record);
        flush();
    }

    /**
     * Closes the Handler's output stream, releases the lock and  restores the
     * normal System.err and System.out.
     *
     * @see java.util.logging.Handler#close()
     */
    @Override
	public synchronized void close() throws SecurityException {
        super.close();
        
        if (outStream != null) {
            try {
                // this will also free the lock
                outStream.close();
            } catch (IOException ioe) {
                //return;
            }

            if (origOut != null) {
                System.setOut(origOut);
            }

            if (origErr != null) {
                System.setErr(origErr);
            }
        }
    }

    /**
     * Nothing special to do for now.
     *
     * @see java.util.logging.Handler#flush()
     */
    @Override
	public synchronized void flush() {
        super.flush();
    }
    
    /**
     * Reads the current content from the log file and returns it as a single string.
     * 
     * @return current content of the log file
     * @throws IOException 
     */
    public String getCurrentContent() throws IOException, SecurityException {
    	if (file != null) {
    		StringBuilder builder = new StringBuilder();
    		// this does not work because the file is locked...
//			FileReader fr = new FileReader(file);
//			char[] ch = new char[1024];
//			
//			int read = 0;
//			while ((read = fr.read(ch)) > -1) {
//				builder.append(ch, 0, read);
//			}
    		//FileInputStream inStream = new FileInputStream(raf.getFD());
    		//FileChannel inChannel = inStream.getChannel();
    		
    		

    		
    		FileChannel inChannel = rfc;   		
    		
    		inChannel.position(logStartPoint);
    		int size = 256;
    		ByteBuffer buf = ByteBuffer.allocate(size);
    		Charset chSet = Charset.forName("UTF-8");

    	    while (inChannel.read(buf) != -1) {    	    	
    	    	buf.flip();
    	    	builder.append(chSet.decode(buf));
    	        buf.clear();
    	    } 
 
    		//inChannel.close();

			return builder.toString();
    	}
    	return null;
    }

    /**
     * Intializes the Handler. Tries to find or create a file that can be
     * locked for exclusive use.
     *
     * @throws IOException
     * @throws SecurityException
     */
    protected void initHandler() throws IOException, SecurityException {
        if (formatter != null) {
            setFormatter(formatter);
        }

        if (level != null) {
            setLevel(level);
        }

        openFile();

        if ((file != null) && (outStream != null)) {
            origOut = System.out;
            origErr = System.err;

            BufferedOutputStream bout = new BufferedOutputStream(outStream);
            printStream = new PrintStream(bout, true);
            System.setErr(printStream);
            System.setOut(printStream);
            setOutputStream(printStream);
        }
    }

    /**
     * Checks the "control" permission on logging.
     *
     * @throws SecurityException
     */
    protected void checkLogAccess() throws SecurityException {
        manager.checkAccess();
    }

    /**
     * Initializes properties by calling on the LogManager for options in the
     * logging configuration file.
     */
    protected void readConfiguration() {
        String className = ErrOutLogFileHandler.class.getName();

        // get the pattern
        pattern = manager.getProperty(className + ".pattern");

        if (pattern == null) {
            pattern = DEFAULT_LOG_FILE_PATTERN;
        } else {
            pattern = pattern.trim();
        }

        // get the count value

        /*
           String countString = manager.getProperty(className + ".count");
           int c = 1;
           if (countString != null) {
               try {
                   c = Integer.parseInt(countString.trim());
                   if (c <= 0) {
                       count = 1;
                   } else {
                       count = c;
                   }
               } catch (NumberFormatException nfe) {
                   count = 1;
               }
           }
         */

        // get the formatter
        String formatString = manager.getProperty(className + ".formatter");

        if (formatString != null) {
            try {
                Class formClass = ClassLoader.getSystemClassLoader().loadClass(formatString.trim());
                formatter = (Formatter) formClass.newInstance();
            } catch (Exception e) {
                // catch any possible exception
                formatter = new SimpleFormatter();
            }
        }

        // get the Level
        String levelString = manager.getProperty(className + ".level");

        if (levelString != null) {
            try {
                level = Level.parse(levelString.trim());
            } catch (Exception e) {
                // catch any possible exception
                level = null;
            }
        }
    }

    /**
     * Copied from Sun's java.util.logging.FileHandler class, with some
     * adaptations. Create a File object from the pattern for the unique id.
     *
     * @param pattern the file pattern to parse
     * @param unique the current unique id
     *
     * @return a File object
     *
     * @throws IOException
     */
    protected File generate(String pattern, int unique)
        throws IOException {
        File file = null;
        String word = "";
        int ix = 0;

        //boolean sawg = false;
        boolean sawu = false;

        while (ix < pattern.length()) {
            char ch = pattern.charAt(ix);
            ix++;

            char ch2 = 0;

            if (ix < pattern.length()) {
                ch2 = Character.toLowerCase(pattern.charAt(ix));
            }

            if (ch == '/') {
                if (file == null) {
                    file = new File(word);
                } else {
                    file = new File(file, word);
                }

                word = "";

                continue;
            } else if (ch == '%') {
                if (ch2 == 't') {
                    String tmpDir = System.getProperty("java.io.tmpdir");

                    if (tmpDir == null) {
                        tmpDir = System.getProperty("user.home");
                    }

                    file = new File(tmpDir);
                    ix++;
                    word = "";

                    continue;
                } else if (ch2 == 'h') {
                    file = new File(System.getProperty("user.home"));

                    /*
                       if (isSetUID()) {
                       // Ok, we are in a set UID program.  For safety's sake
                       // we disallow attempts to open files relative to %h.
                         throw new IOException("can't use %h in set UID program");
                       }
                     */
                    ix++;
                    word = "";

                    continue;
                }
                /*else if (ch2 == 'g') {
                   word = word + generation;
                   sawg = true;
                   ix++;
                   continue;
                   } */
                else if (ch2 == 'u') {
                    word = word + unique;
                    sawu = true;
                    ix++;

                    continue;
                } else if (ch2 == '%') {
                    word = word + "%";
                    ix++;

                    continue;
                }
            }

            word = word + ch;
        }

        /*
           if (count > 1 && !sawg) {
               word = word + "." + generation;
           }
         */
        if ((unique > 0) && !sawu) {
            word = word + "." + unique;
        }

        if (word.length() > 0) {
            if (file == null) {
                file = new File(word);
            } else {
                file = new File(file, word);
            }
        }
        return file;
    }

    /**
     * Tries to get a locked file for exclusive use.
     * <p>
     * The output of this function is outStream and file.
     * r must not be r.close()d: outStream uses it.
     * 
     * @throws IOException
     */
    @SuppressWarnings("resource")
	protected void openFile() throws IOException {
        for (int i = 0; i < MAX_UNIQUE_FILES; i++) {
            File f = null;
            RandomAccessFile r = null;
            try {
                f = generate(pattern, i);
                r = new RandomAccessFile(f, "rw");
                long filesizeInKB = f.length() / 1024;
                
                if(filesizeInKB >= 256){
                	r.setLength(0);
                	logStartPoint = 0L;
                }else{
                	logStartPoint = (int) f.length();
                	r.seek(f.length());
                }
                
                outStream = new FileOutputStream(r.getFD());
                //inStream = new FileInputStream(r.getFD());
                rfc = r.getChannel();
                FileLock lock = rfc.tryLock();
                /*
                // try to get the lock
                outStream = new FileOutputStream(f);

                FileChannel fc = outStream.getChannel();
                FileLock lock = fc.tryLock();
				*/
                if (lock == null) {
                    // already locked, try next
                    continue;
                }
            } catch (IOException ioe) {
            	if (r != null) {
            		r.close();
            	}
                if (i == (MAX_UNIQUE_FILES - 1)) {
                    throw ioe;
                }

                // different io exceptions could have happened
                // try next
                continue;
            }

            if (f != null) {
                file = f;
                //raf = r;
                break;
            }
        }
    }
}
