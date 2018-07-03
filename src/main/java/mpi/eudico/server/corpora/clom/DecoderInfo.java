package mpi.eudico.server.corpora.clom;

/**
 * An interface for objects containing format specific decoding information. 
 */
public interface DecoderInfo {
    /**
     * Returns the path to the file that is to be decoded/parsed.
     * 
     * @return the path to the file
     */
    public String getSourceFilePath();
}
