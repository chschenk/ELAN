package mpi.eudico.client.annotator.update;

import java.util.Calendar;

import mpi.eudico.client.annotator.Preferences;

/**
 * An external thread to check for new updates for 
 * ELAN. This thread is a low priority thread.
 * 
 */
public class ExternalUpdaterThread extends Thread
{
	private static boolean running;
	private static final int INTERVAL_IN_DAYS = 7;// check once per week
	
	/**
	 * Creates a ExternalUpdaterThread instance and 
	 * sets the thread priority to low
	 * 
	 */
    public ExternalUpdaterThread() {
    	setPriority(Thread.MIN_PRIORITY);
    }
   
    /**
     * Starts the thread
     */
    @Override
	public synchronized void start() {
         running = true;   
         System.out.println("External updater thread started ");
         super.start();
    }

    /**
     * Stops the thread
     */
    public void close() {
        running = false;
        if (isAlive()) {
            try {
                interrupt();
            } catch (SecurityException se) {
                System.out.println("Could not stop the external updater thread: " + se.getMessage());
            }
        }
    }
    
    /**
     * Starts the check for updates process
     */
    @Override
	public void run() {
    	checkForUpdates();        	
    }
    
    /**
     * This method check for new updates once in 30 days.
     * 
     */
    private void checkForUpdates(){
    	if (!running) {
	         return;
	     }   	
	
		long lastUpdate = -1L;
		
		Long val = Preferences.getLong("ElanUpdater.LastUpdate", null);		
		if (val != null) {
			lastUpdate = val.longValue();
		}	
		
		long difference = Calendar.getInstance().getTimeInMillis() - lastUpdate;
		long diffInDays = difference / (24 * 60 * 60 * 1000);
		if(diffInDays < INTERVAL_IN_DAYS){
			return;
		}
		
		ElanUpdateDialog updater = new ElanUpdateDialog(null,true);
		updater.checkForUpdates();
    }    
   
    @Override
	public void finalize() {
    	close();
    }
}

