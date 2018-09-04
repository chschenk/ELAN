package nl.mpi.jmmf;

/**
 * A class to hold header information for (the data of) a Device Independent Bitmap structure.
 * Analogous to BITMAPINFOHEADER and used in JNI calls to Microsoft Media Foundation objects.
 * 
 * @author Han Sloetjes
 */
public class DIBInfoHeader {
	public long size;
	public int width;
	public int height;
	public int planes;
	public int bitCount;
	public int compression;
	public long sizeImage;
	public long xPelsPerMeter;
	public long yPelsPerMeter;
	public boolean clrUsed;
	public boolean clrImportant;
}
