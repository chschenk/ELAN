package mpi.eudico.client.annotator.player;

import java.awt.Image;


/**
 * Defines methods to be implemented by any class capable of grabbing images
 * from a video file or stream.
 *
 * @author Han Sloetjes
 */
public interface VideoFrameGrabber {
    /**
     * Grabs the current video frame and converts it to an Image object.
     *
     * @return the current video frame
     */
    public Image getCurrentFrameImage();

    /**
     * Grabs the frame for the specified time and converts it to an Image.
     *
     * @param time the media time to grab the frame for
     *
     * @return the frame image or null
     */
    public Image getFrameImageForTime(long time);
}
