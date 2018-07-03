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

/******************************************************************
Changes by Han Sloetjes January 2014 for the purpose of creating 
a JNI library for use in/from a Java application.
- removed the whar_t variant (wcahr_t *sendpraatW) because it doesn't 
  work on all platforms
- the original sendpraat.c file has been split into platform specific
  versions (there was little code overlap)
- added an error message parameter to be provided by the caller
*******************************************************************/

#include <sys/types.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>
#include <wchar.h>
/*
 * The gtk include requires installation and configuration of several libraries,
 * haven't done so, so far.
 */
//#include <gtk/gtk.h>

static char errorMessage [1000];
static long theTimeOut;
static void handleCompletion (int message) {
	(void) message;
	//printf("handleCompletion called.\n");
}
static void handleTimeOut (int message) {
	(void) message;
	sprintf (errorMessage, "Timed out after %ld seconds.", theTimeOut);
	//printf("handleTimeOut called.\n");
}


void sendpraat (const char *programName, long timeOut, const char *text, char *errorBuffer) {
	char nativeProgramName [100];

	char *home, pidFileName [256], messageFileName [256];
	FILE *pidFile;
	long pid, wid = 0;

	/*
	 * Clean up from an earlier call.
	 */
	errorMessage [0] = '\0';

	/*
	 * Handle case differences.
	 */
	strcpy (nativeProgramName, programName);

	nativeProgramName [0] = tolower (nativeProgramName [0]);


	if ((home = getenv ("HOME")) == NULL) {
		sprintf (errorBuffer, "HOME environment variable not set.");
		return;
	}
	sprintf (messageFileName, "%s/.%s-dir/message", home, programName);


	/*
	 * Save the message file (Unix and Windows only).
	 */

	FILE *messageFile;
	if ((messageFile = fopen (messageFileName, "w")) == NULL) {
		sprintf (errorBuffer, "Cannot create message file \"%s\" "
			"(no privilege to write to directory, or disk full, or program is not called %s).\n", messageFileName, programName);
		return;
	}

	if (timeOut)
		fprintf (messageFile, "#%ld\n", (long) getpid ());   /* Write own process ID for callback. */

	fprintf (messageFile, "%s", text);
	fclose (messageFile);


	/*
	 * Where shall we send the message?
	 */
	/*
	 * Get the process ID and the window ID of a running Praat-shell program.
	 */
	sprintf (pidFileName, "%s/.%s-dir/pid", home, programName);
	if ((pidFile = fopen (pidFileName, "r")) == NULL) {
		sprintf (errorBuffer, "Program %s not running (or a version older than 3.6).", programName);
		return;
	}
	if (fscanf (pidFile, "%ld%ld", & pid, & wid) < 1) {
		fclose (pidFile);
		sprintf (errorBuffer, "Program %s not running, or disk has been full.", programName);
		return;
	}
	fclose (pidFile);


	/*
	 * Send the message.
	 */
	/*
	 * Be ready to receive notification of completion.
	 */
	if (timeOut)
		signal (SIGUSR2, handleCompletion);

	/*
	 * Wait for the running program to notify us of completion,
	 * but do not wait for more than 'timeOut' seconds.
	 */
	if (timeOut) {
		signal (SIGALRM, handleTimeOut);
		alarm (timeOut);
		theTimeOut = timeOut;   /* Hand an argument to handleTimeOut () in a static variable. */
		errorMessage [0] = '\0';
		pause ();
		if (errorMessage [0] != '\0') {
			sprintf (errorBuffer, errorMessage);
		}
	}

}
