package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.Constants;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.VideoFrameGrabber;

import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.util.SystemReporting;

import mpi.eudico.client.annotator.viewer.SignalViewer;

import mpi.eudico.util.TimeFormatter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import java.util.ArrayList;


/**
 * A class for exporting a number of video frames together with a waveform to
 * an image file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportFilmStrip {
    private int frameWidth = 120;
    private int frameStep = 1;
    private boolean includeTimeCodeInFrames = false;
    private boolean includeWaveform = true;
    private boolean timeRulerVisible = true;
    private int waveHeight = 100;
    private int stereoMode = 0;
    private ElanMediaPlayer[] players;
    private String waveFile;
    private final int MAX_IMG_WIDTH = 3000; //arbitrary
    private int numVideoRows = 0;
    private int totalImageWidth = 0;
    private int totalImageHeight = 0;
    private int margin = 2;
    private double msPerSample = 40d;
    private long bt = 0;
    private long et = 0;
    private BufferedImage bufImg;
    private ArrayList<ProgressListener> listeners;

    /**
     * Creates a new ExportFilmStrip instance
     *
     * @param players the video players to grab images from
     * @param waveFile the wav file to use for the waveform
     *
     * @throws NullPointerException if players is null
     */
    public ExportFilmStrip(ElanMediaPlayer[] players, String waveFile) {
        if (players == null) {
            throw new NullPointerException("No players to grab image from.");
        }

        if (waveFile == null) {
            includeWaveform = false;
        }

        this.players = players;
        this.waveFile = waveFile;
        init();
    }

    /**
     * Creates a new ExportFilmStrip instance
     *
     * @param players the video players to grab images from
     * @param waveFile the wav file to use for the waveform
     * @param frameWidth the width for each video frame
     * @param frameStep the number of frames to skip (each n-th frame)
     * @param includeWaveform whether or not to create a waveform
     * @param waveHeight the height of the waveform
     *
     * @throws NullPointerException if players is null
     */
    public ExportFilmStrip(ElanMediaPlayer[] players, String waveFile,
        int frameWidth, int frameStep, boolean includeWaveform, int waveHeight) {
        if (players == null) {
            throw new NullPointerException("No players to grab image from.");
        }

        if (waveFile == null) {
            includeWaveform = false;
        }

        this.players = players;
        this.waveFile = waveFile;

        if (frameWidth > 0) {
            this.frameWidth = frameWidth;
        }

        if (frameStep > 0) {
            this.frameStep = frameStep;
        }

        this.includeWaveform = includeWaveform;

        if (waveHeight > 0) {
            this.waveHeight = waveHeight;
        }

        init();
    }

    private void init() {
        totalImageHeight = margin;

        for (int i = 0; i < players.length; i++) {
            if (players[i] instanceof VideoFrameGrabber) {
                if (players[i].getAspectRatio() > 0) {
                    int h = (int) Math.ceil(frameWidth / players[i].getAspectRatio());
                    totalImageHeight += (h + margin);
                    numVideoRows++;
                }
            }
        }

        if (includeWaveform) {
            totalImageHeight += (waveHeight + margin);
        }
    }

    /**
     * Returns the image as created in a separate thread; call {@link #createImageInThread(long, long)} first.
     *
     * @return the complete image
     */
    public Image getImage() {
        return bufImg;
    }

    /**
     * Sets the width of each frame (video image).
     *
     * @param frameWidth the frameWidth to set
     */
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
        init();
    }

    /**
     * Set the flag whether to include a waveform or not.
     *
     * @param includeWaveform the includeWaveform to set
     */
    public void setIncludeWaveform(boolean includeWaveform) {
        this.includeWaveform = includeWaveform;
        init();
    }

    /**
     * Sets the height in pixels for the waveform.
     *
     * @param waveHeight the waveHeight to set
     */
    public void setWaveHeight(int waveHeight) {
        this.waveHeight = waveHeight;
        init();
    }

    /**
     * Sets the frame step. A value of e.g. 3 means that one frame is painted 
     * and then 2 are skipped. In other words; every n-th frame.
     *
     * @param frameStep the frame step. 1 means every frame
     */
    public void setFrameStep(int frameStep) {
        if (frameStep > 0) {
            this.frameStep = frameStep;
        }
    }

    /**
     * Sets whether a timeruler should be painted in the waveform viewer.
     *
     * @param timeRulerVisible whether a timeruler should be painted!
     */
    public void setTimeRulerVisible(boolean timeRulerVisible) {
        this.timeRulerVisible = timeRulerVisible;
    }

    /**
     * Sets the stereo mode for the waveform viewer; SEPARATE, MERGED or BLENDED.
     *
     * @param stereoMode the stereo channel mode
     */
    public void setStereoMode(int stereoMode) {
        this.stereoMode = stereoMode;
    }

    /**
     * Sets whether the timecode for each included frame should be included.
     *
     * @param includeTimeCodeInFrames whether the timecode for each included frame should be included
     */
    public void setIncludeTimeCodeInFrames(boolean includeTimeCodeInFrames) {
        this.includeTimeCodeInFrames = includeTimeCodeInFrames;
    }

    /**
     * Creates the complete image on a separate thread, allowing listeners to
     * monitor the progress. Call getImage() after completion.
     *
     * @param beginTime the selection begin time
     * @param endTime the selection end time
     */
    public void createImageInThread(long beginTime, long endTime) {
        // total width based on framerate of the first video
        msPerSample = players[0].getMilliSecondsPerSample();
        long beginFrame = (long) (beginTime / msPerSample);
        long endFrame = (long) (endTime / msPerSample);
        
        bt = (long) Math.ceil(beginFrame * msPerSample);
        et = (long) ((endFrame + 1) * msPerSample);
//        bt = beginTime - (beginTime % msPerSample);
//        et = (endTime - (endTime % msPerSample) + msPerSample);

        try {
            new ImageThread().start();
        } catch (Exception ex) {
            //
            progressInterrupt(ex.getMessage());
        }
    }

    /**
     * Creates the image. First creates a row or multiple rows of video images,
     * then adds the waveform below the images.
     * If stepframe is > 1, it is assured that the last frame of the interval is painted,
     * so there might be a smaller time gap between the last 2 images.
     *
     * @param beginTime the segment begin time
     * @param endTime the segment end time
     *
     * @return an image
     */
    public Image createImage(long beginTime, long endTime) {
        // total width based on framerate of the first video
        msPerSample = players[0].getMilliSecondsPerSample();
        long beginFrame = (long) (beginTime / msPerSample);
        long endFrame = (long) (endTime / msPerSample);
        
        bt = (long) Math.ceil(beginFrame * msPerSample);
        et = (long) ((endFrame + 1) * msPerSample);
//        bt = beginTime - (beginTime % msPerSample);
//        et = (endTime - (endTime % msPerSample) + msPerSample);

        int totalNumSamples = (int) ((et - bt) / msPerSample);
        int numSamples = (int) (totalNumSamples / frameStep);

        // check how many frames are left over
        if ((totalNumSamples - (numSamples * frameStep)) > 0) {
            numSamples++;
        }

        // always include the last frame of the range, so that the sequence 
        // runs until end time
        if (frameStep > 1) {
            long timeSpan = (long)((numSamples - 1) * (msPerSample * frameStep)) +
                (long) msPerSample;

            if (timeSpan <= (et - bt - msPerSample)) {
                numSamples++;
            }
        }

        totalImageWidth = (numSamples * frameWidth) + (2 * margin);

        if ((totalImageWidth == 0) || (totalImageHeight == 0)) {
            // throw exception
            progressInterrupt("Image width or height is 0");

            return null;
        }

        int totalNumImages = numSamples * players.length;
        float perImg = includeWaveform ? (80 / (float) totalNumImages)
                                       : (98 / (float) totalNumImages);

        //System.out.println("Num samples: " + totalNumImages
        //		+ " Per img: " + perImg);
        BufferedImage buf = new BufferedImage(totalImageWidth,
                totalImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buf.createGraphics();
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        if (g2d.getFont() != null) {
        	g2d.setFont(Constants.deriveSmallFont(g2d.getFont()));
        } else {
        	if (Constants.DEFAULT_LF_LABEL_FONT != null) {
        		g2d.setFont(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT));
        	} else {
        		g2d.setFont(Constants.deriveSmallFont(Constants.DEFAULTFONT));
        	}
        }
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, totalImageWidth, totalImageHeight);
        g2d.translate(margin, margin);

        for (int i = 0; i < players.length; i++) {
            if (players[i] instanceof VideoFrameGrabber) {
                if (players[i].getAspectRatio() > 0) {
                    // at the end of each row the transform of the graphics object is updated
                    paintFramesOfPlayer(g2d, players[i],
                        i * (numSamples * perImg), perImg);
                }
            }
        }

        if (includeWaveform) {
            SignalViewer signalViewer = new SignalViewer(waveFile);
            progressUpdate(90, "");

            //float mspp = (msPerSample / (float) frameWidth) * frameStep;
            // this should be more accurate; the timespan / the actually used width
            float mspp = (et - bt) / (float) ((numSamples * frameWidth) +
                ((numSamples - 1) * margin));
            signalViewer.setTimeScaleConnected(false);
            signalViewer.setSize(totalImageWidth - (2 * margin), waveHeight);
            signalViewer.setMsPerPixel(mspp);
            signalViewer.setIntervalBeginTime(bt);
            signalViewer.setTimeRulerVisible(timeRulerVisible);
            signalViewer.setChannelMode(stereoMode, false);
            //signalViewer.setIntervalBeginTime(et);
            signalViewer.paintComponent(g2d);
            progressUpdate(98, "");
        }

        bufImg = buf;
        progressComplete("");

        return buf;
    }

    /**
     * Grabs the images from the player, at times based on the segment interval
     * and the frame step size.
     * @param g2d the graphics object of the complete image
     * @param player the current player
     * @param startProg the current progress, for progress monitoring
     * @param perImg the amount of progress per image
     */
    private void paintFramesOfPlayer(Graphics2D g2d, ElanMediaPlayer player,
        float startProg, float perImg) {
        int imgH = (int) Math.ceil(frameWidth / player.getAspectRatio());
        Image curImg;
        AffineTransform identity = new AffineTransform();
        AffineTransform at = null;
        AffineTransformOp atOp = null;
        BufferedImage scaleImg;
        int i = 1;
        long curTime = bt;
        long lastFrameTime = curTime;

        for (; curTime < et; curTime += (frameStep * msPerSample)) {
            curImg = ((VideoFrameGrabber) player).getFrameImageForTime(curTime);
            lastFrameTime = curTime;

            if (curImg != null) {
                if (at == null) {
                    at = new AffineTransform();
                    at.scale(frameWidth / (float) player.getSourceWidth(),
                        imgH / (float) player.getSourceHeight());
                    atOp = new AffineTransformOp(at,
                            AffineTransformOp.TYPE_BICUBIC);
                }

                if (curImg instanceof RenderedImage) {
                	if (SystemReporting.isMacOS()) {
                		g2d.drawRenderedImage((RenderedImage) curImg, at);
                	} else {
	                    try {
	                        scaleImg = new BufferedImage(frameWidth, imgH,
	                                BufferedImage.TYPE_INT_RGB);
	                        scaleImg = atOp.filter((BufferedImage) curImg, scaleImg);
	                        g2d.drawRenderedImage((RenderedImage) scaleImg, identity);
	                    } catch (Exception ex) {
	                        g2d.drawRenderedImage((RenderedImage) curImg, at);
	                    }
                	}
                    //g2d.drawRenderedImage((RenderedImage) curImg, at);
                } else {
                    g2d.drawImage(curImg, at, null);
                }

                g2d.drawRect(0, 0, frameWidth, imgH);

                if (includeTimeCodeInFrames) {
                    String tc = TimeFormatter.toString(curTime);
                    int wi = g2d.getFontMetrics().stringWidth(tc);

                    if (wi < (frameWidth - 2)) {
                        g2d.setColor(Color.LIGHT_GRAY);
                        g2d.drawString(tc, 3, g2d.getFont().getSize() + 2);
                        g2d.setColor(Color.BLUE);
                        g2d.drawString(tc, 2, g2d.getFont().getSize() + 1);
                    }

                    g2d.setColor(Color.WHITE);
                }
            }

            g2d.translate(frameWidth, 0.0);

            progressUpdate((int) (startProg + (perImg * i)), "");
            //System.out.println("prog: " + ((int)(startProg + (perImg * i))));
            i++;
        }

        // check if the last image of the interval has to be added
        if (lastFrameTime < (et - msPerSample)) {
            curImg = ((VideoFrameGrabber) player).getFrameImageForTime(et -
                    (long) msPerSample + 1);

            if (curImg != null) {
                if (at == null) {
                    at = new AffineTransform();
                    at.scale(frameWidth / (float) player.getSourceWidth(),
                        imgH / (float) player.getSourceHeight());
                    atOp = new AffineTransformOp(at,
                            AffineTransformOp.TYPE_BICUBIC);
                }

                if (curImg instanceof RenderedImage) {
                    try {
                        scaleImg = new BufferedImage(frameWidth, imgH,
                                BufferedImage.TYPE_INT_RGB);
                        scaleImg = atOp.filter((BufferedImage) curImg, scaleImg);
                        g2d.drawRenderedImage((RenderedImage) scaleImg, identity);
                    } catch (Exception ex) {
                        g2d.drawRenderedImage((RenderedImage) curImg, at);
                    }

                    g2d.drawRenderedImage((RenderedImage) curImg, at);
                } else {
                    g2d.drawImage(curImg, at, null);
                }

                g2d.drawRect(0, 0, frameWidth, imgH);

                if (includeTimeCodeInFrames) {
                    String tc = TimeFormatter.toString(curTime);
                    int wi = g2d.getFontMetrics().stringWidth(tc);

                    if (wi < (frameWidth - 2)) {
                        g2d.setColor(Color.LIGHT_GRAY);
                        g2d.drawString(tc, 3, g2d.getFont().getSize() + 2);
                        g2d.setColor(Color.BLUE);
                        g2d.drawString(tc, 2, g2d.getFont().getSize() + 1);
                    }

                    g2d.setColor(Color.WHITE);
                }
            }

            g2d.translate(frameWidth, 0.0);

            progressUpdate((int) (startProg + (perImg * i)), "");
        }

        g2d.translate((-g2d.getTransform().getTranslateX() + margin),
            imgH + margin);
    }

    /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressInterrupted(this,
                    message);
            }
        }
    }

    /**
     * A separate thread to start the image creation.
     * 
     * @author Han Sloetjes
     * @version 1.0
      */
    private class ImageThread extends Thread {
        /**
         * Creates a new ImageThread instance
         */
        public ImageThread() {
            super();
        }

        /**
         * The run function. Just calls createImage.
         */
        @Override
		public void run() {
            createImage(bt, et);
        }
    }
}
