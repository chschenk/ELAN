//
//  NoPlayerException.java
//  JavaQTMovieView_64
//
//  Created by Han Sloetjes on 21-07-09.
//  Copyright 2009 MPI. All rights reserved.
//
package player;

/**
 * Exception thrown if QTKit could not create a QTMovie object, e.g. if the file type is not supported.
 */
public class NoCocoaPlayerException extends Exception {
	
	/**
	 * Constructor with a message string as parameter.
	 */
	public NoCocoaPlayerException(String message) {
		super(message);
	}

}
