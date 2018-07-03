package mpi.eudico.client.annotator.mediadisplayer;

import java.awt.Rectangle;

public interface MediaDisplayerHost {
	public void hostMediaDisplayer(Object[] arguments, Rectangle sourceBounds);
	public void discardMediaDisplayer();
}
