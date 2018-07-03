package mpi.eudico.client.annotator.export;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * $Id: AbstractBasicExportDialog.java 45478 2016-08-23 14:05:54Z olasei $
 *
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public abstract class AbstractBasicExportDialog extends ClosableDialog
    implements ActionListener, ClientLogger{
	/** key to store last used export directory in preferences file */
    public static final String LAST_USED_EXPORT_DIR = "LastUsedExportDir";
    
    /** insets between subcomponents */
    protected final Insets insets = new Insets(4, 6, 4, 6);

    /** header in window (top component) */
    protected final JLabel titleLabel = new JLabel();

    /** panel for start and close buttons (bottom component) */
    protected final JPanel buttonPanel = new JPanel();

    /** panel for export options (one of 'body' components) */
    protected final JPanel optionsPanel = new JPanel();

    /** store default as UTF-8 */
    protected final String defaultEncoding = "UTF-8";

    /** table model for tier table */
    protected final TranscriptionImpl transcription;

    /** Character Encoding of export file */
    protected String encoding = defaultEncoding;

    /** close button */
    private final JButton closeButton = new JButton();

    /** start export button */
    private final JButton startButton = new JButton();

    /** minimal window height */
    private final int minimalHeight = 400;

    /** minimal window width */
    private final int minimalWidth = 550;

    /**
     * Creates a new AbstractBasicExportDialog object.
     *
     * @param parent DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param transcription DOCUMENT ME!
     */
    public AbstractBasicExportDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription) {
        super(parent, modal);
        this.transcription = transcription;
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == startButton) {
            try {
                boolean success = startExport();

                if (success) {
                    closeDialog(null);
                } else {
                    // do nothing
                }
            } catch (Exception ee) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                    "(" + ee.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
                ee.printStackTrace();
            }
        } else if (source == closeButton) {
            closeDialog(null);
        }
    }

    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false otherwise
     *
     * @throws IOException DOCUMENT ME!
     */
    protected abstract boolean startExport() throws IOException;

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    protected void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    /**
     * Enables / Disables the start export button
     * 
     * @param enabled
     */
    public void setStartButtonEnabled(boolean enabled){
    	startButton.setEnabled(enabled);
    }

    protected void makeLayout() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        startButton.addActionListener(this);
        buttonPanel.add(startButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();
        
        // set initial location and size, take insets into account
      
        Rectangle pref = Preferences.getRect("AbstractBasicExportDialog.bounds", null);
        // Find on which screen the parent is...
        Container parent = getParent();
        final GraphicsConfiguration graphicsConfiguration = parent.getGraphicsConfiguration();

        // and what the size of that screen is.
        Rectangle screenBounds = graphicsConfiguration.getBounds();
        Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
        int maxW = screenBounds.width - ins.left - ins.right;
        int maxH = screenBounds.height - ins.top - ins.bottom;
        
        // If the preferences indicate a rectangle that is not (or: does not fit) 
        // inside the current screen, disregard them.
        if (pref != null && !screenBounds.contains(pref)) {
        	pref = null;
        }
        
        Dimension size = getSize();
        int targetW = pref != null ? pref.width : size.width;
        targetW = Math.max(targetW, minimalWidth);
        targetW = Math.min(targetW, maxW);
        
        int targetH = pref != null ? pref.height : size.height;
        targetH = Math.max(targetH, minimalHeight);
        targetH = Math.min(targetH, maxH);
        
        setSize(targetW, targetH);
        
        if (pref != null) {
        	setLocation(pref.x, pref.y);
        } else {
        	// This also makes a decent attempt to keep the window completely on-screen. 
        	setLocationRelativeTo(getParent());
        }
//        Point curLoc = getLocationOnScreen(); // the frame should be visible before getting the location on screen
//        
//        int targetX = curLoc.x < ins.left ? ins.left : curLoc.x;
//        int targetY = curLoc.y < ins.top ? ins.top : curLoc.y;
//        setLocation(targetX, targetY);
        //setResizable(false);
        
        // Arrange for window resizes to be recorded in the Preferences
        addComponentListener(new SizeListener());
    }

    /**
     * Prompts the user for a file name and location.
     *
     * @param chooserTitle the title for the save dialog
     * @param extensions the file extensions (one of the constants of FileExtension)
      * @param mainExt the main filter type
     * @param showEncodingBox if true, a combobox for selecting the encoding for the output file 
     * @param encodings the list of encodings the user can choose from
     * 
     * @return a file (unique) path
     */
    protected File promptForFile(String chooserTitle, List<String[]> extensions, 
            String[] mainExt, boolean showEncodingBox, String[] encodings) {
    	
    	String selectedFile = null;
    	String ext = null;
    	
    	if(mainExt != null){
    		ext = mainExt[0];
    	} else if(extensions != null){
    		String[] extArray;
    	outerLoop:
    		for(int i=0; i < extensions.size() ; i++){
    			extArray = extensions.get(i);
    			if(extArray != null && extArray.length >0){
    				ext = extArray[0];
    				break outerLoop;
    			}
    		}
    	}
    	 if (transcription != null) {
    		 selectedFile = getDefaultExportFile(transcription.getFullPath(), ext);
         } else {
        	 selectedFile = getDefaultExportFile(null, ext);
         }
    	
    	
        FileChooser chooser = new FileChooser(this);
        
        if (showEncodingBox) {            
        	chooser.createAndShowFileAndEncodingDialog(chooserTitle, FileChooser.SAVE_DIALOG, extensions, 
        			mainExt, LAST_USED_EXPORT_DIR, encodings, null, selectedFile); 
        } else{
        	chooser.createAndShowFileDialog(chooserTitle, FileChooser.SAVE_DIALOG, extensions, mainExt, LAST_USED_EXPORT_DIR, selectedFile);
        }

        if (showEncodingBox) {
        	encoding = chooser.getSelectedEncoding();    
        }     
        
        return chooser.getSelectedFile();        
    }

    /**
     * Prompts the user for a file name and location.
     *
     * @param chooserTitle the title for the save dialog
     * @param extensions the file extensions (one/more of the constants of FileExtension)    
     * @param showEncodingBox if true, a combobox for selecting the encoding for the output file 
     * 
     * @return a file (unique) path
     */
    protected File promptForFile(String chooserTitle, List<String[]> extensions, 
    		String[] mainExt, boolean showEncodingBox) {
    	return promptForFile(chooserTitle, extensions, mainExt, 
    			showEncodingBox, null);
    } 
    
    protected void updateLocale() {
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * tries to find most appropiate default file
     *
     * @param transcriptionPath
     * @param extension
     *
     * @return export file
     */
    private static String getDefaultExportFile(String transcriptionPath,
        String extension) {       
        File file = null;

        if (transcriptionPath != null) {
            file = new File(transcriptionPath);
        }

        int index = -1;

        if (file != null) {
            index = file.getName().lastIndexOf('.');
        }

        String exportFileName = (index > -1)
            ? file.getName().substring(0, index)
            : ElanLocale.getString("Frame.ElanFrame.Untitled");

        return exportFileName + "." + extension;
    }
    
    class SizeListener extends ComponentAdapter {
		@Override
		public void componentMoved(ComponentEvent e) {
			componentResized(e);
		}

		@Override
		public void componentResized(ComponentEvent e) {
			Rectangle bounds = getBounds();
			Preferences.set("AbstractBasicExportDialog.bounds", bounds, null, false, false);
		}
    }
}
