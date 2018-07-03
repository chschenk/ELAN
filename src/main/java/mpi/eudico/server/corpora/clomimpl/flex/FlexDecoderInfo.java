package mpi.eudico.server.corpora.clomimpl.flex;

import mpi.eudico.server.corpora.clom.DecoderInfo;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import java.util.List;

/**
 * An decoder information class for the FLEx file format parser.
 *
 * @author Han Sloetjes
 * @updatedBy Aarthy Somsundarum, Feb 2013
 */
public class FlexDecoderInfo implements DecoderInfo {
    /** a flag whether or not to include the "interlinear-text" element */
    public boolean inclITElement = true;

    /** a flag whether or not to include the "paragraph" element */
    public boolean inclParagraphElement = true;
    
    public boolean importParticipantInfo = false;

    /** the duration per smallest alignable element */
    public long perPhraseDuration = 1000;
    
    /** the "smallest" element that is time aligned */
    public String smallestWithTimeAlignment = FlexConstants.PHRASE;
    
    private String sourcePath = "";    
    private List<MediaDescriptor> mediaDescriptors = null;
     
    public boolean createLingForNewType = false;
    public boolean createLingForNewLang = false;
    
    /**
     * Creates a decoder info instance with default values.
     */
    public FlexDecoderInfo() {
        super();        
    }
    
    /**
     * Constructor with the source file path as parameter
     *
     * @param sourcePath the FLEx source file
     */
    public FlexDecoderInfo(String sourcePath) {
        super();
        this.sourcePath = sourcePath;
    }
    
    /**
     * Sets the path to the Flex file.
     * 
     * @param sourcePath the path to the Flex file
     */
    public void setSourceFilePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
   
    /**
     * Returns the path to the source file
     *
     * @return the source file path or null
     */
    @Override
	public String getSourceFilePath() {
        return sourcePath;
    }

    /**
     * Returns a list containing the media descriptors
     *
     * @return the list of media descriptors
     */
    public List<MediaDescriptor> getMediaDescriptors() {
        return mediaDescriptors;
    }

    /**
     * Sets the list of media descriptors.
     *
     * @param mediaDescriptors the list of media descriptors
     */
    public void setMediaDescriptors(List<MediaDescriptor> mediaDescriptors) {
        this.mediaDescriptors = mediaDescriptors;
    }
}
