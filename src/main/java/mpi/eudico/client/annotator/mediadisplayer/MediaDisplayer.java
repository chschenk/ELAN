package mpi.eudico.client.annotator.mediadisplayer;

import java.awt.Rectangle;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_ORIENTATION;

/**
 * 
 * @author michahulsbosch
 *
 */
public interface MediaDisplayer {
	public void setMediaBundle(MediaBundle mediabundle);
	public void displayMedia(JComponent component, Rectangle bounds, int delay, MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation);
	public void discard();
}
