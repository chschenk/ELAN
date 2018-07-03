package mpi.search.result.viewer;
/**
 * Created on May 3, 2005
 * $Id: MatchCounter.java 8348 2007-03-09 09:43:13Z klasal $
 *
 * $Author$
 * $Version$
 */

import javax.swing.JLabel;

import mpi.search.result.model.Result;
import mpi.search.result.model.ResultChangeListener;
import mpi.search.result.model.ResultEvent;

public class MatchCounter extends JLabel implements ResultChangeListener{
    protected Result result;

   /**
     * DOCUMENT ME!
     * 
     * @param e
     *            DOCUMENT ME!
     */
    @Override
	public void resultChanged(ResultEvent e) {
        setResult((Result) e.getSource());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param result
     *            DOCUMENT ME!
     */
    public void setResult(Result result) {
        this.result = result;
        render();
    }
    
    /**
     * DOCUMENT ME!
     */
    public void render(){
        setText(result.getRealSize() + " matches");
    }
     
}
