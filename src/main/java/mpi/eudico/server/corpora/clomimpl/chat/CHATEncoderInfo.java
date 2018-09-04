/*
 * Created on Jan 26, 2005
 */
package mpi.eudico.server.corpora.clomimpl.chat;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * CHAT specific EncoderInfo implementation
 * may 06 (HS): added booleans for two export options:<br>
 * - whether or not to recalculate begin and end times of annotations based on the master media offset<br>
 * - if true the filename and begin and end time should be on a separate line (%snd or %mov) otherwise 
 * or this info is appended to the main annotation value 
 * apr 08 (HS): added boolean for inclusion of "%lan:" lines
 * @author hennie
 */
public class CHATEncoderInfo implements EncoderInfo {
    private boolean correctAnnotationTimes = true;
    private boolean timesOnSeparateLine = false;
    private boolean includeLangLine = false;
    private long mediaOffset = 0L;

	private String[][] mainTierInfo;
	private String[][] dependentTierInfo;
		
	public CHATEncoderInfo(String[][] mainTierInfo, String[][] dependentTierInfo) {
		this.mainTierInfo = mainTierInfo;
		this.dependentTierInfo = dependentTierInfo;
	}
	
	public String[][] getMainTierInfo() {
		return mainTierInfo;
	}
	
	public String[][] getDependentTierInfo() {
		return dependentTierInfo;
	}
	
    public boolean getCorrectAnnotationTimes() {
        return correctAnnotationTimes;
    }
    
    public void setCorrectAnnotationTimes(boolean correctAnnotationTimes) {
        this.correctAnnotationTimes = correctAnnotationTimes;
    }
    
    public boolean isTimesOnSeparateLine() {
        return timesOnSeparateLine;
    }
    
    public void setTimesOnSeparateLine(boolean timesOnSeparateLine) {
        this.timesOnSeparateLine = timesOnSeparateLine;
    }
    
    public long getMediaOffset() {
        return mediaOffset;
    }
    
    public void setMediaOffset(long mediaOffset) {
        this.mediaOffset = mediaOffset;
    }

	public boolean isIncludeLangLine() {
		return includeLangLine;
	}

	public void setIncludeLangLine(boolean includeLangLine) {
		this.includeLangLine = includeLangLine;
	}
}
