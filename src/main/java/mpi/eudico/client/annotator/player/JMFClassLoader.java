package mpi.eudico.client.annotator.player;

/**
 * Created on Feb 12, 2004
 *
 * @author Alexander Klassmann
 * @version Feb 12, 2004
 */
public class JMFClassLoader {
	public static String initError = null;
	private static boolean loadTried = false;
    /**
     * this method MUST BE CALLED BEFORE any JMF init/opertions can be used
     * this is because of a bug/feature ??? in the Webstart class laoder that
     * causes jni libs to be ignored unless loaded by hand.. added by gng Apr
     * 14 2003 - moved from MediaAligmentTool
     */
    public static final void initJMFJNI() {
    	if (loadTried) {
    		return;// only once
    	}
        System.out.println("current library path:");
        System.out.println(System.getProperty("java.library.path"));

        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                /* ... for windows */
                System.out.println("loading windows native libs");
                System.loadLibrary("jmutil");
                System.loadLibrary("jmmpegv");
                System.loadLibrary("jmddraw");
                System.loadLibrary("jmam");

                // GNG:May-15-2003 Removed due to jsound superseeding jmf.jar
                // in smb resolution
                //	System.loadLibrary("jsound");
                System.loadLibrary("jmdaud");
                System.loadLibrary("jmacm");
                System.loadLibrary("jmcvid");
                System.loadLibrary("jmgdi");
                System.loadLibrary("jmgsm");
                System.loadLibrary("jmh261");
                System.loadLibrary("jmjpeg");
                System.loadLibrary("jmmpa");
                System.loadLibrary("jmvcm");
                System.loadLibrary("jmvh263");
            } else if (System.getProperty("os.name").startsWith("Linux")) {
                System.out.println("loading Linux native libs...");
                System.loadLibrary("jmutil");
                System.out.println("jmutil loaded");
                System.loadLibrary("jawt");
                System.out.println("jawt loaded");
                System.loadLibrary("jmmpx");
                System.out.println(".");
                System.loadLibrary("jmcvid");
                System.out.println(".");
                System.loadLibrary("jmdaud");
                System.out.println(".");
                System.loadLibrary("jmg723");
                System.out.println(".");
                System.loadLibrary("jmgsm");
                System.out.println(".");
                System.loadLibrary("jmh261");
                System.out.println(".");
                System.loadLibrary("jmh263enc");
                System.out.println(".");
                System.loadLibrary("jmjpeg");
                System.out.println(".");
                System.loadLibrary("jmsound");
                System.out.println(".");
                System.loadLibrary("jmxlib");
                System.out.println(".");
                System.loadLibrary("jmmpegv");
                System.out.println("jmmpegv loaded");
                System.loadLibrary("jmmpa");
                System.out.println(".");
            } else if (System.getProperty("os.name").startsWith("SunOS")) {
                System.loadLibrary("jmg723");
                System.loadLibrary("jmmpa");
                System.loadLibrary("jmutil");
                System.loadLibrary("jmcvid");
                System.loadLibrary("jmgsm");
                System.loadLibrary("jmmpegv");
                System.loadLibrary("jmvh263");
                System.loadLibrary("jmdaud");
                System.loadLibrary("jmh261");
                System.loadLibrary("jmmpx");
                System.loadLibrary("jmxil");
                System.loadLibrary("CvidPro");
                System.loadLibrary("jmfCVIDPro");
                System.loadLibrary("jmh263enc");

                //			System.loadLibrary("jmopi");
                System.loadLibrary("jmxlib");
                System.loadLibrary("jmfjawt");
                System.loadLibrary("jmjpeg");

                //	System.loadLibrary("jmsunray");
                System.loadLibrary("jsound");
            }
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.err.println(
                "Warning: Unable to load a dll or Sharedobject: " +
                e.getMessage());
            initError = e.getMessage();
        } catch (Throwable tr) {
        	System.out.println("Error while loading native libraries: " + tr.getMessage());
        	initError = tr.getMessage();
        }
        loadTried = true;
    }
}
