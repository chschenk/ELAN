//
//  JavaQTMovieViewCreatedListener.java
//  JavaQTMovieView
//
//  Created by Albert Russel on 2/5/09.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

package player;

/**
 * The listener gets informed with a call to playerCreated(JavaQTMoviePlayer player) 
 * when the JavaQTMoviePlayer is created, i.e. when it is added to a container hierarchy 
 * with a Frame or a Window at the top.
 *
 * Only after the JavaQTMoviePlayer is created it is possible to use all its methods
 */
public interface JavaQTMoviePlayerCreatedListener {
	public void playerCreated(JavaQTMoviePlayer player);
}
