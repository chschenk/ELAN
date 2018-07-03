package mpi.search.content.result.model;

/**
 * DOCUMENT ME!
 *
 * @author klasal TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractContentMatch implements ContentMatch {
    protected String fileName = "";
    protected String leftContext = "";
    protected String rightContext = "";
    //add parent and childrencontext mod. Coralie Villes
    protected String parentContext="";
    protected String childrenContext="";
    protected String tierName = "";
    protected int[][] matchedSubstringIndices;
    protected int indexWithinTier = -1;
    protected long beginTime;
    protected long endTime;

    /* (non-Javadoc)
     * @see mpi.eudico.server.corpora.clom.AnnotationCore#getBeginTimeBoundary()
     */
    @Override
	public long getBeginTimeBoundary() {
        return beginTime;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.server.corpora.clom.AnnotationCore#getEndTimeBoundary()
     */
    @Override
	public long getEndTimeBoundary() {
        return endTime;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getFileName()
     */
    @Override
	public String getFileName() {
        return fileName;
    }

    /**
     *
     *
     * @param i DOCUMENT ME!
     */
    public void setIndex(int i) {
        indexWithinTier = i;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getIndex()
     */
    @Override
	public int getIndex() {
        return indexWithinTier;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getLeftContext()
     */
    @Override
	public String getLeftContext() {
        return leftContext;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getMatchedSubstringIndices()
     */
    @Override
	public int[][] getMatchedSubstringIndices() {
        return matchedSubstringIndices;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getRightContext()
     */
    @Override
	public String getRightContext() {
        return rightContext;
    }

    /* (non-Javadoc)
     * @see mpi.eudico.search.advanced.result.model.Match#getTierName()
     */
    @Override
	public String getTierName() {
        return tierName;
    }
}
