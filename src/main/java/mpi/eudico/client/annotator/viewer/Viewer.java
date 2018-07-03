package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.ViewerManager2;

public interface Viewer {
    // viewer manager
    public void setViewerManager(ViewerManager2 viewerManager);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ViewerManager2 getViewerManager();
}