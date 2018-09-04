package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.JOptionPane;


/**
 * A class to export a BufferedImage to file.
 *
 * @author Han Sloetjes
 */
public class ImageExporter {
	Frame parentFrame = null;
	
    /**
     * Creates a new ImageExporter instance
     */
    public ImageExporter() {
    }

    /**
     * Creates a new ImageExporter instance
     */
    public ImageExporter(Frame frame) {
    	parentFrame = frame;
    }
    
    /**
     * Shows a save dialog with .jpg and .png as file filters and  writes the
     * image to file.
     *
     * @param image theimage to save
     */
    public void exportImage(Image image) {
        BufferedImage img;

        if (!(image instanceof BufferedImage)) {
            img = imageToBufferedImage(image);
        } else {
			img = (BufferedImage) image;
        }

        if (img == null) {
            JOptionPane.showMessageDialog(parentFrame,
                ElanLocale.getString("ImageExporter.Message.NoImage"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        String saveDir = Preferences.getString("MediaDir", null);

        if (saveDir == null) {
            saveDir = System.getProperty("user.dir");
        }

        FileChooser chooser = new FileChooser(parentFrame);
        chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.IMAGE_EXT, "MediaDir");

        String imageIOType = "jpg";
        File saveFile = chooser.getSelectedFile();

        if (saveFile != null) {
        	String fileName = saveFile.getAbsolutePath();
            String lowerFileName = fileName.toLowerCase();

            if (lowerFileName.endsWith("png")) {
            	imageIOType = "png";
            } else if (lowerFileName.endsWith("bmp")) {
              	imageIOType = "bmp";
            } else if (!lowerFileName.endsWith("jpg") &&
                !lowerFileName.endsWith("jpeg")) {
                fileName += ".jpg";
            }

            final File newSaveFile = new File(fileName);
            
            // Cocoa QT currently delivers png images with alpha. Remove alpha in case of saving as jpg or bmp
            // implement a more elegant way of fixing the transparency later...
            if (img.getColorModel().hasAlpha() && !imageIOType.equals("png")) {
               	BufferedImage flatImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
               	flatImg.getGraphics().drawImage(img, 0, 0, null);
               	img = flatImg;
            }
                
            try {
            	ImageIO.write(img, imageIOType, newSaveFile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame,
                ElanLocale.getString("ExportDialog.Message.Error"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BufferedImage imageToBufferedImage(Image image) {
        if (image == null) {
            return null;
        }

        BufferedImage bufImg = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufImg.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufImg;
    }
}
