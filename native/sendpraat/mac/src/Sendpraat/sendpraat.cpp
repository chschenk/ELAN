/* sendpraat.c */
/* by Paul Boersma */
/* 19 March 2013 */

/*
 * The sendpraat subroutine (Unix with GTK; Windows; Macintosh) sends a message
 * to a running program that uses the Praat shell.
 * The sendpraat program does the same from a Unix command shell,
 * from a Windows console, or from a MacOS X terminal window.
 *
 * Newer versions of sendpraat may be found at http://www.praat.org or http://www.fon.hum.uva.nl/praat/sendpraat.html
 *
 * On Windows, this version works only with Praat version 4.3.28 (November 2005) or newer.
 * On Macintosh, this version works only with Praat version 3.8.75 (October 2000) or newer.
 * On Unix with GTK, this version works only with Praat version 5.1.33 (May 2010) or newer.
 * Newer versions of Praat may respond faster or more reliably.
 */

/*******************************************************************

   THIS CODE CAN BE COPIED, USED, AND DISTRIBUTED FREELY.
   IF YOU MODIFY IT, PLEASE KEEP MY NAME AND MARK THE CHANGES.
   IF YOU IMPROVE IT, PLEASE NOTIFY ME AT paul.boersma@uva.nl.

*******************************************************************/

/*******************************************************************************
 Changes by Han Sloetjes November 2013 for the purpose of creating a jnilib library
 for use in/from a Java application:
 - removed the wchar_t *sendpraatW function, doesn't seem to work (on Mac OS at least)
 - split the original into platform specific versions (there was very little code overlap,
   this is the Mac OS X version)
 - added an error message parameter to be provided by the caller
 *******************************************************************************/

    #include "sendpraat.h"
	#include <signal.h>
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	#include <unistd.h>
	#include <ctype.h>
	#include <wchar.h>
	#include <AppleEvents.h>
	#ifdef __MACH__
		#include <AEMach.h>
	#endif
	#include <MacErrors.h>


/*
 * Parameters:
 * 'programName' is the name of the program that receives the message.
 *    This program must have been built with the Praat shell (the most common such programs are Praat and ALS).
 *    On Unix, the program name is usually all lower case, e.g. "praat" or "als", or the name of any other program.
 *    On Windows, you can use either "Praat", "praat", or the name of any other program.
 *    On Macintosh, 'programName' must be "Praat", "praat", "ALS", or the Macintosh signature of any other program.
 * 'timeOut' is the time (in seconds) after which sendpraat will return with a time-out error message
 *    if the receiving program sends no notification of completion.
 *    On Unix and Macintosh, the message is sent asynchronously if 'timeOut' is 0;
 *    this means that sendpraat will return OK (NULL) without waiting for the receiving program
 *    to handle the message.
 *    On Windows, the time out is ignored.
 * 'text' contains the contents of the Praat script to be sent to the receiving program.
 * 'errorMessage' a char array to store an error message, the caller should provide a suffiently sized char array (1000)
 */
void sendpraat (const char *programName, long timeOut, const char *text, char *errorMessage) {
    char nativeProgramName [100];

    AEDesc programDescriptor;
    AppleEvent event, reply;
    OSErr err;
    UInt32 signature;

    /*
     * Handle case differences.
     */
    strcpy (nativeProgramName, programName);
    nativeProgramName [0] = toupper (nativeProgramName [0]);

    /*
     * Convert the program name to a Macintosh signature.
     * I know of no system routine for this, so I'll just translate the two most common names:
     */
    if (! strcmp (programName, "praat") || ! strcmp (programName, "Praat") || ! strcmp (programName, "PRAAT")) {
        signature = 'PpgB';
    } else if (! strcmp (programName, "als") || ! strcmp (programName, "Als") || ! strcmp (programName, "ALS")) {
        signature = 'CclA';
    } else {
        signature = 0;
    }
    
    AECreateDesc (typeApplSignature, & signature, 4, & programDescriptor);

    /*
     * Notify the running program by sending it an Apple event of the magic class 758934755.
     */
    AECreateAppleEvent (758934755, 0, & programDescriptor, kAutoGenerateReturnID, 1, & event);
    AEPutParamPtr (& event, 1, typeChar, text, strlen (text) + 1);
    
    #ifdef __MACH__
    err = AESendMessage (& event, & reply,
                         ( timeOut == 0 ? kAENoReply : kAEWaitReply ) | kAECanInteract | kAECanSwitchLayer,
                         timeOut == 0 ? kNoTimeOut : 60 * timeOut);
    #else
    err = AESend (& event, & reply,
                  ( timeOut == 0 ? kAENoReply : kAEWaitReply ) | kAECanInteract | kAECanSwitchLayer,
                  kAENormalPriority, timeOut == 0 ? kNoTimeOut : 60 * timeOut, NULL, NULL);
    #endif
    
    if (err != noErr) {
        if (err == procNotFound || err == connectionInvalid) {
            sprintf (errorMessage, "Could not send message to program \"%s\".\n"
                     "The program is probably not running (or an old version).", programName);
        } else if (err == errAETimeout) {
            sprintf (errorMessage, "Message to program \"%s\" timed out "
                    "after %ld seconds, before completion.", programName, timeOut);
        } else {
            sprintf (errorMessage, "Unexpected sendpraat error %d.\nNotify the author.", err);
        }
    }
    AEDisposeDesc (& programDescriptor);
    AEDisposeDesc (& event);
    AEDisposeDesc (& reply);

} // end sendpraat

/* End of file sendpraat.c */
