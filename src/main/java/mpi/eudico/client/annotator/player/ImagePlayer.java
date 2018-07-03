package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;

/**
 * A "player" for a still image, with a virtual duration or time line.
 * 
 * @author Han Sloetjes
 */
public class ImagePlayer extends ControllerManager implements ElanMediaPlayer, ControllerListener, 
    MouseListener, ActionListener {
	
	private MediaDescriptor mediaDescriptor;
	protected BufferedImage image;
	protected ImagePanel visComponent;
	protected boolean attached = true;
	protected boolean cursorVisible = false;
	private ElanLayoutManager layoutManager;
	
	// media time related fields
	// the original, internal media time, the time in player space (excl. offset)
    private long mediaTime;
    // the offset into internal media time, determines now '0' point
    private long offset;
    private float rate;
    private boolean playing;
    private double milliSecondsPerSample;
    // the original, internal media duration, exclusive offset
	protected long duration = 10000L;
	protected int[] cursorGrid = {1, 1};
	
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	
    private long startTimeMillis;
    private boolean playingInterval;
    private PeriodicUpdateController periodicController;
    private PeriodicUpdateController playerController;
    private long intervalStopTime;
    
    private JPopupMenu popup;
    private JMenuItem detachMI;
	private JMenuItem durationItem;
	private JCheckBoxMenuItem cursorVisItem;
	private final DecimalFormat format = new DecimalFormat("#.###");
	
	/**
	 * 
	 */
	public ImagePlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		this.mediaDescriptor = mediaDescriptor;	
		
		// initialize image, image component and duration
        offset = 0;
        rate = 1;
        milliSecondsPerSample = 40;
        
        if (mediaDescriptor != null) {
        	offset = mediaDescriptor.timeOrigin;
        	if (mediaDescriptor.mediaURL != null) {
        		try {
        			URL url = new URL(mediaDescriptor.mediaURL);
        			try {
        				image = ImageIO.read(url);
        			} catch (IOException ioe) {
        				throw new NoPlayerException("Cannot create player: " + ioe.getMessage());
        			}
        		} catch (MalformedURLException mue) {
        			if (mediaDescriptor.mediaURL.startsWith("file:")) {
        				String path = mediaDescriptor.mediaURL.substring(5);
        				File f = new File(path);
        				if (!f.exists() || !f.canRead()) {
        					throw new NoPlayerException("Cannot create player: file does not exist or cannot be read - "
        							+ mediaDescriptor.mediaURL);
        				}
            			try {
            				image = ImageIO.read(f);
            			} catch (IOException ioe) {
            				throw new NoPlayerException("Cannot create player: " + ioe.getMessage());
            			}
        			} else {
        				// no url, no "file" prefix?
        				File f = new File(mediaDescriptor.mediaURL);
        				if (!f.exists() || !f.canRead()) {
        					throw new NoPlayerException("Cannot create player: file does not exist or cannot be read - "
        							+ mediaDescriptor.mediaURL);
        				}
        				throw new NoPlayerException("Cannot create player for: " + mediaDescriptor.mediaURL);
        			}
        		}
        		
        		if (image != null) {
        			duration = 60000 * 10 - offset;// 10 minutes - offset?
        		} else {
        			// ImageIO.read does not throw an exception if there is no reader for the file  type
        			throw new NoPlayerException("Cannot create player for: " + mediaDescriptor.mediaURL);
        		}
        	} else {
        		throw new NoPlayerException("Cannot create player: no media descriptor");
        	}
        }
        
        playerController = new PeriodicUpdateController(40);
        playerController.addControllerListener(this);
        addController(playerController);
	}
	
	/**
	 * Repaints, if needed.
	 * 
	 * @param event the controller event
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            if (periodicController != null) {
                if (getMediaTime() >= intervalStopTime) {
                    // stop the player
                    stop();
                }
            } else {
            	// the player's own controller
            	if (getMediaTime() >= duration) {
            		stop();
            		playerController.setMediaTime(duration);
            	}
            }
        }
        // update the image viewer
        if (visComponent != null) {
        	if (visComponent.isCursorVisible()) {
        		long mt = getMediaTime();
        		if (duration - offset > 0) {
        			visComponent.setCursorProgress((float) (mt + offset) / duration);// compare to original duration
        		} else {
        			visComponent.setCursorProgress(0);
        		}
        	}
        }
        // System.out.println("Event: " + event.toString());
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#cleanUpOnClose()
	 */
	@Override
	public void cleanUpOnClose() {
		if (visComponent != null) {
			visComponent.flush();
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getAspectRatio()
	 */
	@Override
	public float getAspectRatio() {
		if (image != null) {
			if (image.getHeight() != 0) {
				return (float)image.getWidth() / image.getHeight();
			}
		}
		
		return 1f;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getFrameworkDescription()
	 */
	@Override
	public String getFrameworkDescription() {
		return "MPI_PL Image Player";
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getMediaDescriptor()
	 */
	@Override
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getMediaDuration()
	 */
	@Override
	public long getMediaDuration() {
		return duration - offset;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getMediaTime()
	 */
	@Override
	public long getMediaTime() {
        if (playing) {
            return (mediaTime + System.currentTimeMillis()) - startTimeMillis - offset;
        }

        return mediaTime - offset;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getMilliSecondsPerSample()
	 */
	@Override
	public double getMilliSecondsPerSample() {
		return milliSecondsPerSample;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getOffset()
	 */
	@Override
	public long getOffset() {
		return offset;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getRate()
	 */
	@Override
	public float getRate() {
		return rate;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceHeight()
	 */
	@Override
	public int getSourceHeight() {
		if (image != null) {
			return image.getHeight();
		}
		return 0;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceWidth()
	 */
	@Override
	public int getSourceWidth() {
		if (image != null) {
			return image.getWidth();
		}
		return 0;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getVisualComponent()
	 */
	@Override
	public Component getVisualComponent() {
		if (visComponent == null) {
			visComponent = new ImagePanel(image);
			visComponent.setCursorVisible(cursorVisible);
			visComponent.setCursorGrid(cursorGrid[0], cursorGrid[1]);
			// add mouse listener "this" for popup menu
			visComponent.addMouseListener(this);
		}
		
		return visComponent;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getVolume()
	 */
	@Override
	public float getVolume() {
		return 0L;
	}

	@Override
    public void setSubVolume(float level) {
    }
    
    @Override
    public float getSubVolume(){
    	return 0;
    }
    
    @Override
    public void setMute(boolean mute) {
    }
    
    @Override
    public boolean getMute() {
    	return true;
    }
    
	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isFrameRateAutoDetected()
	 */
	@Override
	public boolean isFrameRateAutoDetected() {
		return true;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isPlaying()
	 */
	@Override
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#nextFrame()
	 */
	@Override
	public void nextFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		setMediaTime((long) Math.ceil((curFrame + 1) * milliSecondsPerSample));
    	} else {
    		setMediaTime((long) Math.ceil(getMediaTime() + getMilliSecondsPerSample()));
    	}
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#playInterval(long, long)
	 */
	@Override
	public void playInterval(long startTime, long stopTime) {
        if (playingInterval || (stopTime <= startTime)) {
            return;
        }

        periodicController = new PeriodicUpdateController(25);
        periodicController.addControllerListener(this);
        addController(periodicController);
        intervalStopTime = stopTime;
        setMediaTime(startTime);
        playingInterval = true;
        start();
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#previousFrame()
	 */
	@Override
	public void previousFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		if (curFrame > 0) {
    			setMediaTime((long) Math.ceil((curFrame - 1) * milliSecondsPerSample));
    		} else {
    			setMediaTime(0);
    		}
    	} else {
    		setMediaTime((long) Math.ceil(getMediaTime() - getMilliSecondsPerSample()));
    	}
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setAspectRatio(float)
	 */
	@Override
	public void setAspectRatio(float aspectRatio) {
		// stub ?
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setFrameStepsToFrameBegin(boolean)
	 */
	@Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		this.frameStepsToFrameBegin = stepsToFrameBegin;

	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setLayoutManager(mpi.eudico.client.annotator.ElanLayoutManager)
	 */
	@Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
		
		if (layoutManager != null) {
			Long longPref = Preferences.getLong("ImagePlayer.Duration", layoutManager.getViewerManager().getTranscription());
			if (longPref != null) {
				duration = longPref.longValue();
			}
			Boolean boolPref = Preferences.getBool("ImagePlayer.CursorVisible", layoutManager.getViewerManager().getTranscription());
			if (boolPref != null) {
				cursorVisible = boolPref.booleanValue();
				if (visComponent != null) {
					visComponent.setCursorVisible(cursorVisible);
				}
			}
			Integer intPref = Preferences.getInt("ImagePlayer.CursorGrid.Columns", layoutManager.getViewerManager().getTranscription());
			if (intPref != null) {
				cursorGrid[0] = intPref.intValue();
			}
			intPref = Preferences.getInt("ImagePlayer.CursorGrid.Rows", layoutManager.getViewerManager().getTranscription());
			if (intPref != null) {
				cursorGrid[1] = intPref.intValue();
			}
			if (visComponent != null) {
				visComponent.setCursorGrid(cursorGrid[0], cursorGrid[1]);
			}
			
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setMediaTime(long)
	 */
	@Override
	public void setMediaTime(long time) {
        mediaTime = time + offset;
        if (mediaTime > duration) {
        	mediaTime = duration;
        }
        setControllersMediaTime(time);
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setMilliSecondsPerSample(long)
	 */
	@Override
	public void setMilliSecondsPerSample(long milliSeconds) {
		this.milliSecondsPerSample = milliSeconds;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setOffset(long)
	 */
	@Override
	public void setOffset(long offset) {
		this.offset = offset;
		mediaDescriptor.timeOrigin = offset;
		//duration -= offset;
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setRate(float)
	 */
	@Override
	public void setRate(float rate) {
		this.rate = rate;

	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setStopTime(long)
	 */
	@Override
	public void setStopTime(long stopTime) {
		// stub ?? this is not the interval stop time

	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#setVolume(float)
	 */
	@Override
	public void setVolume(float level) {
		// no sound
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#start()
	 */
	@Override
	public void start() {
        playing = true;

        startTimeMillis = System.currentTimeMillis();

        // make sure all managed controllers are started
        startControllers();
	}

	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#stop()
	 */
	@Override
	public void stop() {
        if (playing) {
            mediaTime += (System.currentTimeMillis() - startTimeMillis);
        }

        playing = false;

        // make sure all managed controllers are stopped
        stopControllers();

        // make sure that all interval playing is finished
        if (playingInterval) {
            stopPlayingInterval();
        }
	}

    /**
     * Puts the specified text on the clipboard.
     * 
     * @param text the text to copy
     */
    private void copyToClipboard(String text) {
    	    if (text == null) {
    		    return;
    	    }
    	    //System.out.println(text);
    	    if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (SecurityException se) {
                //LOG.warning("Cannot copy, cannot access the clipboard.");
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        } else {
            try {
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        }
    }
    
    /**
     * Disable all code for interval playing
     */
    private void stopPlayingInterval() {
        if (periodicController != null) {
            periodicController.removeControllerListener(this);
            removeController(periodicController);
            periodicController = null;
        }

        playingInterval = false;
    }
    
	/**
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#updateLocale()
	 */
	@Override
	public void updateLocale() {
		if (popup != null) {
			popup = null;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (image == null) {
			return;
		}
        try {
            //System.out.println("OW: " + image.getWidth() + " OH: " + 
            //		image.getHeight());
            if (e.isAltDown()) {
            	   copyToClipboard(format.format(e.getX() / (float)visComponent.getWidth()) + "," 
            			   + format.format(e.getY() / (float)visComponent.getHeight()));
            }  else if (e.isShiftDown()){
                copyToClipboard("" + (int)((image.getWidth() / (float)visComponent.getWidth()) * e.getX()) 
            		    + "," + (int)((image.getHeight() / (float)visComponent.getHeight()) * e.getY()));
            } else {
                copyToClipboard("" + (int)((image.getWidth() / (float)visComponent.getWidth()) * e.getX()) 
            		    + "," + (int)((image.getHeight() / (float)visComponent.getHeight()) * e.getY())
            		    + " [" + image.getWidth() + "," + image.getHeight() + "]");
            }
        } catch (Exception exep) {
        	   exep.printStackTrace();
        }
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
                || e.isPopupTrigger()) {
    		if (popup == null) {
    			popup = new JPopupMenu("Popup");
    			detachMI = new JMenuItem();
    			detachMI.setActionCommand("detach");
    			if (attached) {
    				detachMI.setText(ElanLocale.getString("Detachable.detach"));
    			} else {
    				detachMI.setText(ElanLocale.getString("Detachable.attach"));
    			}
    			detachMI.addActionListener(this);
    			popup.add(detachMI); 
    			// add cursor related items, set visibility, set grid
    			// set duration
    			// player info
    			// ...
    			JMenuItem infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
    			infoItem.setActionCommand("info");
                infoItem.addActionListener(this);
                popup.add(infoItem);
				
				JMenuItem setDurationItem = new JMenuItem(ElanLocale.getString("Player.SetDuration"));
				setDurationItem.setActionCommand("duration");
				setDurationItem.addActionListener(this);
				popup.add(setDurationItem);
				
                durationItem = new JMenuItem(ElanLocale.getString("Player.duration") +
                        ":  " + TimeFormatter.toString(getMediaDuration()));
                durationItem.setEnabled(false);
				popup.add(durationItem);
    			  
				popup.addSeparator();
				cursorVisItem = new JCheckBoxMenuItem(ElanLocale.getString("ImagePlayer.CursorVisible"));
				cursorVisItem.setActionCommand("curvis");
				cursorVisItem.setSelected(cursorVisible);
				cursorVisItem.addActionListener(this);
				popup.add(cursorVisItem);
				JMenuItem setCursorGridItem = new JMenuItem(ElanLocale.getString("ImagePlayer.CursorGrid"));
				setCursorGridItem.setActionCommand("setgrid");
				setCursorGridItem.addActionListener(this);
				popup.add(setCursorGridItem);
    		} else {
            	// check the detached state, attaching can be done independently of the menu
            	if (layoutManager.isAttached(this)) {
            		if (!attached) {
            			attached = true;
            			detachMI.setText(ElanLocale.getString("Detachable.detach"));
            		}
            	}
    		}
    		
    		popup.show((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
        }
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("detach")) {
			if (attached) {
				if (layoutManager != null && visComponent != null) {
					layoutManager.detach(visComponent);
					attached = !attached;
					detachMI.setText(ElanLocale.getString("Detachable.attach"));
				}
			} else {
				if (layoutManager != null && visComponent != null) {
					layoutManager.attach(visComponent);
					attached = !attached;
					detachMI.setText(ElanLocale.getString("Detachable.detach"));
				}
			}			
		} else if (e.getActionCommand().equals("info")) {
			new FormattedMessageDlg(this);
		} else if (e.getActionCommand().equals("duration")) {
			String dur = null;
			Component parent = visComponent;
			if (layoutManager != null) {
				parent = layoutManager.getElanFrame();
			}
			dur = JOptionPane.showInputDialog(parent, ElanLocale.getString("Player.SetDurationMessage"), 
						ElanLocale.getString("Player.SetDuration"), JOptionPane.PLAIN_MESSAGE);
			if (dur != null && dur.length() > 0) {
				long ms = TimeFormatter.toMilliSeconds(dur);
				if (ms > offset && ms > milliSecondsPerSample) {
					duration = ms;
					if (durationItem != null) {
						durationItem.setText(ElanLocale.getString("Player.duration") +
		                        ":  " + TimeFormatter.toString(getMediaDuration()));
					}
					// post message
					if (getMediaTime() > getMediaDuration()) {
						setMediaTime(getMediaDuration());
					}
				}
	        	if (visComponent.isCursorVisible()) {
	        		long mt = getMediaTime();
	        		if (duration - offset > 0) {
	        			visComponent.setCursorProgress((float) (mt + offset) / duration);
	        		} else {
	        			visComponent.setCursorProgress(0);
	        		}
	        	}
	        	layoutManager.setPreference("ImagePlayer.Duration", new Long(duration), 
	        			layoutManager.getViewerManager().getTranscription());
			}
			
		} else if (e.getActionCommand().equals("curvis")) {
			if (visComponent != null && cursorVisItem != null) {
				visComponent.setCursorVisible(cursorVisItem.isSelected());
				
	        	if (visComponent.isCursorVisible()) {
	        		long mt = getMediaTime();
	        		if (duration > 0) {
	        			visComponent.setCursorProgress((float) mt / duration);
	        		} else {
	        			visComponent.setCursorProgress(0);
	        		}
	        	}
	        	layoutManager.setPreference("ImagePlayer.CursorVisible", Boolean.valueOf(visComponent.isCursorVisible()), 
	        			layoutManager.getViewerManager().getTranscription());
			}
		} else if (e.getActionCommand().equals("setgrid")) {
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.gridwidth = 2;
			panel.add(new JLabel(ElanLocale.getString("ImagePlayer.CursorGridLabel")), gbc);
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel(ElanLocale.getString("ImagePlayer.CursorGridColumns")), gbc);
			JTextField colField = new JTextField(8);
			colField.setText(String.valueOf(cursorGrid[0]));
			gbc.gridx = 1;
			panel.add(colField, gbc);			
			JTextField rowField = new JTextField(8);
			rowField.setText(String.valueOf(cursorGrid[1]));
			gbc.gridy = 2;
			panel.add(rowField, gbc);
			gbc.gridx = 0;
			panel.add(new JLabel(ElanLocale.getString("ImagePlayer.CursorGridRows")), gbc);
			
			Component parent = visComponent;
			if (layoutManager != null) {
				parent = layoutManager.getElanFrame();
			}
			int option = JOptionPane.showConfirmDialog(parent, panel, ElanLocale.getString("ImagePlayer.CursorGrid"), 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			
			if (option == JOptionPane.OK_OPTION) {
				try {
					cursorGrid[0] = Integer.parseInt(colField.getText());
				} catch (NumberFormatException nfe) {}// silently fail
				
				try {
					cursorGrid[1] = Integer.parseInt(rowField.getText());
				} catch (NumberFormatException nfe) {}// silently fail
				
				visComponent.setCursorGrid(cursorGrid[0], cursorGrid[1]);
				
	        	layoutManager.setPreference("ImagePlayer.CursorGrid.Columns", Integer.valueOf(cursorGrid[0]), 
	        			layoutManager.getViewerManager().getTranscription());
	        	layoutManager.setPreference("ImagePlayer.CursorGrid.Rows", Integer.valueOf(cursorGrid[1]), 
	        			layoutManager.getViewerManager().getTranscription());
			}
		}
		
	}

	@Override
	public void preferencesChanged() {
		// stub, the preferences are (still) loaded when the layout manager is set 
		// (it depends on the transcription object as a key)	
	}

}