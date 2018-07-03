package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Rectangle;
import java.io.File;
import java.util.logging.Level;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_ORIENTATION;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * A class to show a video within a bounding box
 * @author michahulsbosch
 *
 */
public class DynamicVideoPlayer implements Runnable {

	private JComponent component;
	private Rectangle bounds;
	private MediaDescriptor md;
	private int delay;
	private MEDIA_ORIENTATION horizontalOrientation;
	private MEDIA_ORIENTATION verticalOrientation;
	
	private ElanMediaPlayer player;
	private Boolean cleanedUp = false;
	
	/**
	 * Creates a object containing all necessary info to display a (short) CV-entry video
	 * in a specified component at a specified place.
	 * 
	 * The orientation arguments can be used to determine which corner of the video is the anchor of
	 * the bounds.
	 * 
	 * @param component
	 * @param bounds
	 * @param delay
	 * @param videoFile
	 * @param horizontalOrientation The horizontal orientation of the video bounds anchor
	 * @param verticalOrientation The vertial orientation of the video bounds anchor
	 */
	public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, File videoFile, MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
		this.component= component;
		setBounds(bounds);
		setVideoFile(videoFile);
		this.delay = delay;
		setOrientation(horizontalOrientation, verticalOrientation);
	}
	
	public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, File videoFile) {
		this(component, bounds, delay, videoFile, MEDIA_ORIENTATION.WEST, MEDIA_ORIENTATION.NORTH);
	}
	
	/**
	 * 
	 * @param videoFile
	 */
	public void setVideoFile(File videoFile) {
		md = MediaDescriptorUtil.createMediaDescriptor(videoFile.getAbsolutePath());
	}
	
	/**
	 * 
	 * @param bounds
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}
	
	/**
	 * 
	 * @param horizontalOrientation
	 * @param verticalOrientation
	 */
	public void setOrientation(MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
		this.horizontalOrientation = horizontalOrientation;
		this.verticalOrientation = verticalOrientation;
	}
	
	/**
	 * 
	 */
	@Override
	public void run() {
		setCleanedUp(false);
		try {
			Thread.sleep(delay);
			player = PlayerFactory.createElanMediaPlayer(md);
			
			// Fit the player within the given bounds, but
			// do not display black bars.
			float playerAspectRatio = player.getAspectRatio();
			float boundsAspectRatio = bounds.width / bounds.height;
			Rectangle displayBounds = new Rectangle(bounds);
			if(playerAspectRatio > 0) {
				if(playerAspectRatio < boundsAspectRatio) {
					displayBounds.setSize((int) Math.ceil(bounds.height*playerAspectRatio), bounds.height);  
				} else if(playerAspectRatio > boundsAspectRatio) {
					displayBounds.setSize(bounds.width, (int) Math.ceil(bounds.width/playerAspectRatio)); 
				}
			}
			
			if(horizontalOrientation == MEDIA_ORIENTATION.EAST) {
				displayBounds.x = displayBounds.x - displayBounds.width;
			}
					
			if(verticalOrientation == MEDIA_ORIENTATION.SOUTH) {
				displayBounds.y = displayBounds.y - displayBounds.height;
			}
			
			Component videoComponent = player.getVisualComponent();
			videoComponent.setBounds(displayBounds);
			component.add(videoComponent);
			videoComponent.setVisible(true);
			component.validate();
			component.repaint();
			player.setMediaTime(0);
			player.start();
			Thread.sleep(player.getMediaDuration() + 10);
		} catch(InterruptedException ee) {
			if(ClientLogger.LOG.isLoggable(Level.INFO)) {
            	ClientLogger.LOG.info("Displaying video " + md.mediaURL + " interrupted (" + ee.getMessage() + ")");
            }
		} catch (NoPlayerException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Not able to create a player for " + md.mediaURL + " (" + e.getMessage() + ")");
            }
		} finally {
			cleanUp();
		}
	}

	/**
	 * Removes the player from the hosting component
	 * and cleans it for garbage collection.
	 */
	public void cleanUp() {
		if(component != null && player != null) {
			component.remove(player.getVisualComponent());
			//player.cleanUpOnClose();
			player = null;
		}
		setCleanedUp(true);
	}

	public Boolean isCleanedUp() {
		return cleanedUp;
	}
	
	public void setCleanedUp(Boolean cleanedUp) {
		this.cleanedUp = cleanedUp;
	}
}
