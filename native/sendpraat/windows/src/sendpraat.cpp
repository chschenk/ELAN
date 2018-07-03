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
 Changes by Han Sloetjes November 2013 for the purpose of creating a jni shared library (.dll)
 for use in/from a Java application:
 - removed the wchar_t *sendpraatW function, doesn't seem to work (only tested on Mac OS)
 - split the original into platform specific versions (there was very little code overlap, 
   this is the Windows variant)
 - added an error message parameter to be provided by the caller
 *******************************************************************************/

	#include "sendpraat.h"
	#include <windows.h>
	#include <stdio.h>
	#include <wchar.h>
	#ifdef __MINGW32__
		#define swprintf  _snwprintf
	#endif


/*
 * Parameters:
 * 'programName' is the name of the program that receives the message.
 *    This program must have been built with the Praat shell (the most common such programs are Praat and ALS).
 *    On Unix, the program name is usually all lower case, e.g. "praat" or "als", or the name of any other program.
 *    On Windows, you can use either "Praat", "praat", or the name of any other program.
 *    On Macintosh, 'programName' must be "Praat", "praat", "ALS", or the Macintosh signature of any other program.
 * 'text' contains the contents of the Praat script to be sent to the receiving program.
 * 'errorMessage' a buffer for storing and returning the error message
 */

void sendpraat (const char *programName, const char *text, char *errorMessage) {
	char nativeProgramName [100];

	char homeDirectory [256], messageFileName [256], windowName [256];
	HWND window;

	/*
	 * Handle case differences.
	 */
	strcpy (nativeProgramName, programName);
	nativeProgramName [0] = toupper (nativeProgramName [0]);

	/*
	 * If the text is going to be sent in a file, create its name.
	 * The file is going to be written into the preferences directory of the receiving program.
	 * On Unix, the name will be something like /home/jane/.praat-dir/message.
	 * On Windows, the name will be something like C:\Documents and Settings\Jane\Praat\Message.txt,
	 * or C:\Windows\Praat\Message.txt on older systems.
	 * On Macintosh, the text is NOT going to be sent in a file.
	 */
	if (GetEnvironmentVariableA ("USERPROFILE", homeDirectory, 255)) {
		;   /* Ready. */
	} else if (GetEnvironmentVariableA ("HOMEDRIVE", homeDirectory, 255)) {
		GetEnvironmentVariableA ("HOMEPATH", homeDirectory + strlen (homeDirectory), 255);
	} else {
		GetWindowsDirectoryA (homeDirectory, 255);
	}
	sprintf (messageFileName, "%s\\%s\\Message.txt", homeDirectory, programName);


	/*
	 * Save the message file (Unix and Windows only).
	 */
	FILE *messageFile;
	if ((messageFile = fopen (messageFileName, "w")) == NULL) {
		sprintf (errorMessage, "Cannot create message file \"%s\" "
			"(no privilege to write to directory, or disk full, or program is not called %s).\n", messageFileName, programName);
		return;
	}

	fprintf (messageFile, "%s", text);
	fclose (messageFile);


	/*
	 * Where shall we send the message?
	 * Get the window handle of the "Objects" window of a running Praat-shell program.
	 */
	sprintf (windowName, "PraatShell1 %s", programName);
	window = FindWindowA (windowName, NULL);
	if (! window) {
		sprintf (errorMessage, "Program %s not running (or an old version).", programName);
		return;
	}

	/*
	 * Notify the running program by sending a WM_USER message to its main window.
	 */
	if (SendMessage (window, WM_USER, 0, 0)) {
		sprintf (errorMessage, "Program %s returns error.", programName);   /* BUG? */
	}
	
}

/* End of file sendpraat.c */
