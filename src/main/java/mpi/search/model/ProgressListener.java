package mpi.search.model;
/**
 * Created on May 14, 2005
 * $Id: ProgressListener.java 3932 2005-07-13 14:14:26Z klasal $
 *
 * $Author$
 * $Version$
 */

public interface ProgressListener {
    
    public void setProgress(int i);
    
    public void setStatus(int i);
    
    public void setIndeterminate(boolean b);

}
