package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.util.TimeFormatter;

/**
 * A command action for copying the current time to the pasteboard.
 * 
 * @author Aarthy Somasundaram
 * @version Dec 2010
 * @version Jan 2016 Added PAL 50 frames per second. Note that in the preferences (media pref panel)
 * at some point a change has been made to store non-localized values from Constants, checks for that have
 * added now as well.  
 */

public class CopyCurrentTimeToPasteBoardCommand implements Command {
    private String commandName;
    
    private static String HH_MM_SS_MS = ElanLocale.getString("TimeCodeFormat.Hours");
    private static String SS_MS = ElanLocale.getString("TimeCodeFormat.Seconds");
    private static String MS = ElanLocale.getString("TimeCodeFormat.MilliSec");
    private static String NTSC = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC");   
    private static String PAL = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL");
    private static String PAL_50 = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50");

    /**
     * Creates a new CopyCurrentTimeToPasteBoardCommand instance
     *
     * @param theName name of the command
     */
    public CopyCurrentTimeToPasteBoardCommand(String theName) {
        commandName = theName;
    }

    /**
     *
     * @param receiver ElanMediaPlayerController (not actually used at the moment)
     * @param arguments arguments[0] = ElanMediaPlayer
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
    	// receiver is master ElanMediaPlayerController
        // arguments[0] is ElanMediaPlayer               
        // ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            return;
        }
        String currentTime = null;
        String timeFormat = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
        if (timeFormat != null) {
        	// check the Constants first
        	currentTime = checkTimeFormatConstants(timeFormat, player.getMediaTime());
        	if (currentTime == null) {
	        	if (timeFormat.equals(HH_MM_SS_MS)){
	            	currentTime = TimeFormatter.toString(player.getMediaTime());
	            } else if(timeFormat.equals(SS_MS)){
	            	currentTime = TimeFormatter.toSSMSString(player.getMediaTime());
	            } else if(timeFormat.equals(NTSC)){
	            	currentTime = TimeFormatter.toTimecodeNTSC(player.getMediaTime());
	            } else if(timeFormat.equals(PAL)){
	            	currentTime = TimeFormatter.toTimecodePAL(player.getMediaTime());
	            } else if(timeFormat.equals(PAL_50)){
	            	currentTime = TimeFormatter.toTimecodePAL50(player.getMediaTime());
	            } else {
	            	currentTime = Long.toString(player.getMediaTime());
	            }
        	}
        } else {
        	currentTime = Long.toString(player.getMediaTime());
        }
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection strSel = new StringSelection(currentTime);
		clipboard.setContents(strSel, null);
    }
    
    private String checkTimeFormatConstants(String timeFormatPref, long time) {
    	if (timeFormatPref != null) {
    		if (timeFormatPref.equals(Constants.HHMMSSMS_STRING)) {
    			return TimeFormatter.toString(time);
    		} else if (timeFormatPref.equals(Constants.SSMS_STRING)) {
    			return TimeFormatter.toSSMSString(time);
    		} else if (timeFormatPref.equals(Constants.NTSC_STRING)) {
    			return TimeFormatter.toTimecodeNTSC(time);
    		} else if (timeFormatPref.equals(Constants.PAL_STRING)) {
    			return TimeFormatter.toTimecodePAL(time);
    		} else if (timeFormatPref.equals(Constants.PAL_50_STRING)) {
    			return TimeFormatter.toTimecodePAL50(time);
    		}
    	}
    	return null;
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
