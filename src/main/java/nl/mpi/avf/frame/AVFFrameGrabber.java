package nl.mpi.avf.frame;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for retrieving an image of a video frame or the bytes of a (decoded) video frame
 * or for saving an image using native code. 
 * The native code is based on the macOS AVFoundation.
 *  
 * @author Han Sloetjes
 */
public class AVFFrameGrabber {
	private final static Logger LOG = Logger.getLogger("AVF");
	static {
		try {
			// load libAVFFrameGrabber.dylib
			System.loadLibrary("AVFFrameGrabber");
		} catch (Throwable t) {
			//t.printStackTrace();
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Error loading native library: " + t.getMessage());
			}
		}
	}
	
	private String mediaPath;
	private long id;
	private boolean fieldsInited = false;
	// the following fields are given a value in native code, a change in field name
	// should be combined with a change in the native code
	private int numBytesPerFrame;
	private int numBytesPerRow;
	private int numBitsPerPixel;
	// component meaning R, G, B and/or A
	private int numBitsPerPixelComponent;
	private int imageWidth;
	private int imageHeight;
	private String colorModelCG;
	private String alphaInfo;
	private String bitmapInfo;
	private int videoWidth;
	private int videoHeight;
	private long videoDuration;
	
	/** for use with possibly repeated calls to {@link #getVideoFrameImage(long)} */
	private ByteBuffer byteBuffer;
	private ColorModel colorModel;
//	private WritableRaster raster;
	private byte[] byteArray;
	private boolean useByteArray = false;// use of ByteBuffer is the default 
	
	/**
	 * Constructor, initializes a native AVFoundation Asset for the specified
	 * media (video) file. The native asset is stored in memory for repeated  
	 * frame grabbing without requiring initialization each time.
	 * 
	 * @param mediaPath the path or url of the video file
	 */
	public AVFFrameGrabber(String mediaPath) {
		this.mediaPath = mediaPath;
		
		id = initNativeAsset(mediaPath);
		
		// test some fields 
		if (id > 0 && videoWidth > 0) {
			fieldsInited = true;
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("The AVFoundation based FrameGrabber could not be initialized");
			}
		}
	}
	
	/**
	 * Creates and returns an image object for the specified time.
	 * 
	 * @param sampleTime the time to get the image for
	 * @return a BufferedImage of the natural size of the video frame, or null if 
	 * an error occurred
	 */
	public synchronized BufferedImage getVideoFrameImage(long sampleTime) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No Image: the FrameGrabber was not correctly initialized");
			}
			return null;
		}
		//long time = System.currentTimeMillis();
		if(numBytesPerFrame == 0) {
			numBytesPerFrame = numBytesPerRow * imageHeight;
		}
		int numBytes = 0;
		
		if (useByteArray) {
			if (byteArray == null) {
				byteArray = new byte[numBytesPerFrame];
			}
			numBytes = grabVideoFrameBA(id, sampleTime, byteArray);
		} else {
			if (byteBuffer == null) {
				byteBuffer = ByteBuffer.allocateDirect(numBytesPerFrame);
			} else {
				//reset position to 0
				byteBuffer.position(0);
			}
			numBytes = grabVideoFrame(id, sampleTime, byteBuffer);
		}
 
		if (numBytes > 0) {
			try {
				byte[] dataBytes = null;
				if (useByteArray) {
					dataBytes = byteArray;
				} else {
					if (byteBuffer.hasArray()) {// returns false
						dataBytes = byteBuffer.array();
					} else {
						// this repeated creation of a new byte array seems to be non-optimal 
						dataBytes = new byte[numBytes];
						byteBuffer.get(dataBytes, 0, numBytes);
					}
				}				
				
				// maybe the raster can be reused after creation by calling 
				// setDataElements(int x, int y, int w, int h, Object inData)
				// this seems to result in scrambled images
				//raster.setDataElements(0, 0, imageWidth, imageHeight, dataBytes);
				DataBufferByte dataBufferByte = new DataBufferByte(dataBytes, numBytes);
				WritableRaster raster = Raster.createInterleavedRaster(dataBufferByte, 
					imageWidth, imageHeight, numBytesPerRow, numBitsPerPixel / numBitsPerPixelComponent, 
					getBandOffsets(), null);
				
				if (colorModel == null) {
					colorModel = createCorrespondingColorModel();
				}
				//System.out.println("T: " +(System.currentTimeMillis() - time));
				/*BufferedImage img =*/ return new BufferedImage(colorModel, raster, true, null);
			} catch (Throwable t) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("Error converting frame bytes to Java image: " + t.getMessage());
				}
				//t.printStackTrace();
			}
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Error retrieving frame bytes from the AVFoundation image generator ");
			}
		}
		
		return null;
	}
	
	/**
	 * The actual ColorModel to create should depend on the characteristics of the native CGImage
	 * @return a color model that corresponds to or is compatible with the properties of the 
	 * CoreGraphics CGImage
	 */
	private ColorModel createCorrespondingColorModel() {
		// especially the color space, the hasAlpha and isAlphaPremultiplied parameters should 
		// depend on the properties of the native image 
		ComponentColorModel comModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 
				true, true, // hasAlpha, isAlphaPremultiplied
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		return comModel;
	}
	
	/**
	 * Note: the actual band offsets should be determined by the colorModelCG 
	 * and/or the alphaInfo.
	 * 
	 * @return the band offsets for interpreting the bytes array
	 */
	private int[] getBandOffsets() {
		return new int[]{1, 2, 3, 0};
	}

	/**
	 * Grabs the bytes of a video frame and stores them in the ByteBuffer, shared between 
	 * Java and the native interface.
	 * 
	 * @param sampleTime the time to get the frame for
	 * @param buffer the buffer to copy the bytes into, the buffer must be of sufficient size
	 * @return the actual number of copied bytes
	 */
	public int grabVideoFrame(long sampleTime, ByteBuffer buffer) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No pixel bytes: the FrameGrabber was not correctly initialized");
			}
			return 0;
		}
		
		return grabVideoFrame(id, sampleTime, buffer);
	}
	
	/**
	 * Grabs the bytes of a video frame and stores them in the byte[].
	 * 
	 * @param sampleTime the time to get the frame for
	 * @param byteArray the byte array to copy the bytes into, the array must be of sufficient size
	 * @return the actual number of copied bytes
	 */
	public int grabVideoFrameByteArray(long sampleTime, byte[] byteArray) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No pixel bytes: the FrameGrabber was not correctly initialized");
			}
			return 0;
		}
		
		return grabVideoFrameBA(id, sampleTime, byteArray);
	}
	
	/**
	 * Saves the video frame in the specified location using native code. If the file 
	 * already exists it will be overwritten without warning.
	 * 
	 * @param imageURL the location to store the image. The type of the image is based on
	 * the file extension, .png in case of unknown or unsupported image format. Other 
	 * supported formats are .jpg and .bmp.
	 * @param sampleTime the time for which to save the frame image
	 * 
	 * @return true if the file was saved successfully, false otherwise
	 */
	public boolean saveFrameNativeAVF(String imageURL, long sampleTime) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Could not save the image: the FrameGrabber was not correctly initialized");
			}
			return false;
		}
		
		return saveFrameNativeAVF(id, imageURL, sampleTime);
	}
	
	/**
	 * Releases the native resources from memory when done with this class.
	 */
	public void release() {
		if (id > 0) {
			release(id);
		}		
	}

	/**
	 * Initializes the native Asset and ImageGenerator and generates an id for it.
	 * @param mediaPath the path or url of the video
	 * @return an id which serves as a key for storing and retrieving the generator 
	 * in and from a map or dictionary.
	 */
	private native long initNativeAsset(String mediaPath);
	
	private native int grabVideoFrame(long id, long sampleTime, ByteBuffer buffer);
	
	private native int grabVideoFrameBA(long id, long sampleTime, byte[] byteArray);
	
	private native boolean saveFrameNativeAVF(long id, String imageURL, long sampleTime);
	
	private native void release(long id);

	// getters for details of the image if the caller loads the bytes into a ByteBuffer
	/**
	 * @return the number of bytes necessary for storing the pixels of a single video frame
	 */
	public int getNumBytesPerFrame() {
		return numBytesPerFrame;
	}

	/**
	 * @return the number of bytes per row 
	 */
	public int getNumBytesPerRow() {
		return numBytesPerRow;
	}

	/**
	 * @return the number of bits necessary for storing a single pixel
	 */
	public int getNumBitsPerPixel() {
		return numBitsPerPixel;
	}

	/**
	 * @return the number of bits per pixel component, e.g. the R (or G or B etc.) component
	 */
	public int getNumBitsPerPixelComponent() {
		return numBitsPerPixelComponent;
	}

	/**
	 * @return the width in pixels of a frame image, this can be different from the videoWidth 
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * @return the height in pixels of a frame image, this can be different from the videoHeight 
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * @return a String representation of the color model of the image, one of the constants of
	 * CGColorSpaceModel, part of the CoreGraphics Framework
	 */
	public String getColorModelCG() {
		return colorModelCG;
	}

	/**
	 * @return the width of the video, can be different from the decoded frame image width
	 */
	public int getVideoWidth() {
		return videoWidth;
	}

	/**
	 * @return the height of the video, can be different from the decoded frame image height
	 */
	public int getVideoHeight() {
		return videoHeight;
	}

	/**
	 * @return the duration of the video in milliseconds
	 */
	public long getVideoDuration() {
		return videoDuration;
	}

	/**
	 * @return whether or not a byte array should be used when grabbing an image,
	 * instead of a nio.ByteBuffer 
	 * 
	 * @see #getVideoFrameImage(long)
	 */
	public boolean isUseByteArray() {
		return useByteArray;
	}

	/**
	 * Sets the flag that determines whether a nio.ByteBuffer is used to receive the pixels
	 * from the native AVFoundation or a byte array
	 * 
	 * @param useByteArray the new value of this flag
	 * @see #getVideoFrameImage(long)
	 */
	public void setUseByteArray(boolean useByteArray) {
		this.useByteArray = useByteArray;
	}
	
}
