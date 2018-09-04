package mpi.eudico.client.annotator.player;


//import com.jniwrapper.util.ImageUtils;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import nl.mpi.jmmf.DIBInfoHeader;


/**
 * A utility class that converts DIB Image data to a BufferedImage.
 *
 * @author Han Sloetjes
 */
public class DIBToImage {
    /**
     * Converts grabbed pixel data (in Device Independent Bitmap format) to a
     * BufferedImage.  Support is limited to uncompressed 24-bit and 32-bit
     * color image data. <br>
     * Note: Adaptation of Jeff West's JavaTip 43 on javaworld.com.
     *
     * @param data the pixel data
     *
     * @return a BufferedImage
     */
    public static BufferedImage DIBDataToBufferedImage(byte[] data) {
        if ((data == null) || (data.length < 55)) {
            return null;
        }

        BufferedImage image = null;
        int nbisize = (((int) data[3] & 0xff) << 24) |
            (((int) data[2] & 0xff) << 16) | (((int) data[1] & 0xff) << 8) |
            ((int) data[0] & 0xff);

        // System.out.println("Size of bitmapinfoheader is :" + nbisize);
        int nwidth = (((int) data[7] & 0xff) << 24) |
            (((int) data[6] & 0xff) << 16) | (((int) data[5] & 0xff) << 8) |
            ((int) data[4] & 0xff);

        // System.out.println("Width is :" + nwidth);
        int nheight = (((int) data[11] & 0xff) << 24) |
            (((int) data[10] & 0xff) << 16) | (((int) data[9] & 0xff) << 8) |
            ((int) data[8] & 0xff);

        // System.out.println("Height is :" + nheight);
        byte[] bi = data;

        // int nplanes = (((int) bi[13] & 0xff) << 8) | ((int) bi[12] & 0xff);
        // System.out.println("Planes is :" + nplanes); // == 1
        int nbitcount = (((int) bi[15] & 0xff) << 8) | ((int) bi[14] & 0xff);

        // System.out.println("BitCount is :" + nbitcount);
        // Look for non-zero values to indicate compression
        // int ncompression = (((int) bi[19]) << 24) | (((int) bi[18]) << 16) |
        //    (((int) bi[17]) << 8) | (int) bi[16];
        // System.out.println("Compression is :" + ncompression); //0 = uncompressed
        int nsizeimage = (((int) bi[23] & 0xff) << 24) |
            (((int) bi[22] & 0xff) << 16) | (((int) bi[21] & 0xff) << 8) |
            ((int) bi[20] & 0xff);

        // System.out.println("SizeImage is :" + nsizeimage);

        /*
           int nxpm = (((int) bi[27] & 0xff) << 24) |
               (((int) bi[26] & 0xff) << 16) | (((int) bi[25] & 0xff) << 8) |
               ((int) bi[24] & 0xff);
           // System.out.println("X-Pixels per meter is :" + nxpm);
           int nypm = (((int) bi[31] & 0xff) << 24) |
               (((int) bi[30] & 0xff) << 16) | (((int) bi[29] & 0xff) << 8) |
               ((int) bi[28] & 0xff);
           // System.out.println("Y-Pixels per meter is :" + nypm);
           int nclrused = (((int) bi[35] & 0xff) << 24) |
               (((int) bi[34] & 0xff) << 16) | (((int) bi[33] & 0xff) << 8) |
               ((int) bi[32] & 0xff);
           // System.out.println("Colors used are :" + nclrused);
           int nclrimp = (((int) bi[39] & 0xff) << 24) |
               (((int) bi[38] & 0xff) << 16) | (((int) bi[37] & 0xff) << 8) |
               ((int) bi[36] & 0xff);
         */

        // System.out.println("Colors important are :" + nclrimp);
        if (nbitcount == 24) {
            // No Palette data for 24-bit format but scan lines are
            // padded out to even 4-byte boundaries.
            int npad = (nsizeimage / nheight) - (nwidth * 3);

            if (npad == 4) {
                npad = 0;
            }

            int[] ndata = new int[nheight * nwidth];

            int nindex = nbisize;

            for (int j = 0; j < nheight; j++) {
                for (int i = 0; i < nwidth; i++) {
                    ndata[(nwidth * (nheight - j - 1)) + i] = ((255 & 0xff) << 24) |
                        (((int) data[nindex + 2] & 0xff) << 16) |
                        (((int) data[nindex + 1] & 0xff) << 8) |
                        ((int) data[nindex] & 0xff);

                    nindex += 3;
                }

                nindex += npad;
            }

            Image ii = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                        nwidth, nheight, ndata, 0, nwidth));

            if (ii != null) {
                image = new BufferedImage(nwidth, nheight,
                        BufferedImage.TYPE_INT_RGB);

                Graphics g = image.createGraphics();
                g.drawImage(ii, 0, 0, null);
                g.dispose();

                // ImageUtil delivers a BufferedImage that cannot always be saved as a .jpg
                //image = ImageUtils.createBufferedImage(ii);
            }
        } else if (nbitcount == 32) {
            // No Palette data for 32-bit format
            // padding of scan lines to even 4-byte boundaries doesn't seem necessary

            /*
               int npad = (nsizeimage / nheight) - (nwidth * 4);
               if (npad == 4) {
                   npad = 0;
               }
             */
            int[] ndata = new int[nheight * nwidth];

            int nindex = nbisize;

            for (int j = 0; j < nheight; j++) {
                for (int i = 0; i < nwidth; i++) {
                    // one unused byte per 4 bytes
                    ndata[(nwidth * (nheight - j - 1)) + i] = ((255 & 0xff) << 24) |
                        (((int) data[nindex + 2] & 0xff) << 16) |
                        (((int) data[nindex + 1] & 0xff) << 8) |
                        ((int) data[nindex] & 0xff);

                    nindex += 4;
                }

                //nindex += npad;
            }

            Image ii = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                        nwidth, nheight, ndata, 0, nwidth));

            if (ii != null) {
                // convert to BufferedImage
                image = new BufferedImage(nwidth, nheight,
                        BufferedImage.TYPE_INT_RGB);

                Graphics g = image.createGraphics();
                g.drawImage(ii, 0, 0, null);
                g.dispose();

                // ImageUtil delivers a BufferedImage that cannot always be saved as a .jpg
                //image = ImageUtils.createBufferedImage(ii);
            }
        }

        return image;
    }
    
    /**
     * Converts grabbed pixel data (in Device Independent Bitmap format) to a
     * BufferedImage.  Support is limited to uncompressed 24-bit and 32-bit
     * color image data. <br>
     * Note: Adaptation of Jeff West's JavaTip 43 on javaworld.com.
     *
     * @param header a data structure containing properties of the data
     * @param data the pixel data, without a header
     *
     * @return a BufferedImage
     */
    public static BufferedImage DIBDataToBufferedImage(DIBInfoHeader header, byte[] data) {
        if ((data == null) || (header == null)) {
            return null;
        }
        
        BufferedImage image = null;
        int nbisize = (int) header.size;
        // System.out.println("Size of bitmapinfoheader is :" + nbisize);
        int nwidth = header.width;
        // System.out.println("Width is :" + nwidth);
        int nheight = header.height;
        // System.out.println("Height is :" + nheight);
        // int nplanes = header.planes;
        // System.out.println("Planes is :" + nplanes); // == 1
        int nbitcount = header.bitCount;
        // System.out.println("BitCount is :" + nbitcount);
        // Look for non-zero values to indicate compression
        // int ncompression = header.compression;
        // System.out.println("Compression is :" + ncompression); //0 = uncompressed
        int nsizeimage = (int) header.sizeImage;
        // System.out.println("SizeImage is :" + nsizeimage);

        byte[] bi = data;
        if (nbitcount == 24) {
            // No Palette data for 24-bit format but scan lines are
            // padded out to even 4-byte boundaries.
            int npad = (nsizeimage / nheight) - (nwidth * 3);

            if (npad == 4) {
                npad = 0;
            }

            int[] ndata = new int[nheight * nwidth];

            int nindex = 0;// start at index 0

            for (int j = 0; j < nheight; j++) {
                for (int i = 0; i < nwidth; i++) {
                    ndata[(nwidth * (nheight - j - 1)) + i] = ((255 & 0xff) << 24) |
                        (((int) data[nindex + 2] & 0xff) << 16) |
                        (((int) data[nindex + 1] & 0xff) << 8) |
                        ((int) data[nindex] & 0xff);

                    nindex += 3;
                }

                nindex += npad;
            }

            Image ii = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                        nwidth, nheight, ndata, 0, nwidth));

            if (ii != null) {
                image = new BufferedImage(nwidth, nheight,
                        BufferedImage.TYPE_INT_RGB);

                Graphics g = image.createGraphics();
                g.drawImage(ii, 0, 0, null);
                g.dispose();

            }
        } else if (nbitcount == 32) {
            // No Palette data for 32-bit format
            // padding of scan lines to even 4-byte boundaries doesn't seem necessary

            /*
               int npad = (nsizeimage / nheight) - (nwidth * 4);
               if (npad == 4) {
                   npad = 0;
               }
             */
            int[] ndata = new int[nheight * nwidth];

            int nindex = 0;

            for (int j = 0; j < nheight; j++) {
                for (int i = 0; i < nwidth; i++) {
                    // one unused byte per 4 bytes
                    ndata[(nwidth * (nheight - j - 1)) + i] = ((255 & 0xff) << 24) |
                        (((int) data[nindex + 2] & 0xff) << 16) |
                        (((int) data[nindex + 1] & 0xff) << 8) |
                        ((int) data[nindex] & 0xff);

                    nindex += 4;
                }

                //nindex += npad;
            }

            Image ii = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                        nwidth, nheight, ndata, 0, nwidth));

            if (ii != null) {
                // convert to BufferedImage
                image = new BufferedImage(nwidth, nheight,
                        BufferedImage.TYPE_INT_RGB);

                Graphics g = image.createGraphics();
                g.drawImage(ii, 0, 0, null);
                g.dispose();

            }
        }

        return image;

    }
}
