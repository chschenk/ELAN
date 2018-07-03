package mpi.eudico.client.annotator.mediadisplayer;

import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_TYPE;

/**
 * An interface for media providers
 * @author michahulsbosch
 *
 */
public interface MediaProvider {
	public MediaBundle getMedia(MediaDisplayerFactory.MEDIA_TYPE type, Object[] args);
	public Boolean providesType(MEDIA_TYPE mediaType);
	public MEDIA_TYPE getPreferredMediaType();
}
