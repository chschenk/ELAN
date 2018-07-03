package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.gui.ImportFLExDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;


/**
 * Action that starts an Import FLEx sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ImportFlexMA extends FrameMenuAction {
    /**
     * Creates a new ImportFlexMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportFlexMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import FLEx dialog and creates a new transcription.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        ImportFLExDialog dialog = new ImportFLExDialog(frame);
        Object value = dialog.getDecoderInfo();
        dialog.dispose();//??
        
        if (value == null) {
            return;
        }

        DecoderInfo decInfo = (DecoderInfo) value;
        if (decInfo.getSourceFilePath() == null) {
            return;
        }
        
        
//        if(true){
//        	String sourcePath = decInfo.getSourceFilePath();        	
//        	String transformedPath = sourcePath.substring(0, sourcePath.lastIndexOf('.'))
//        			+ "-transformed" + sourcePath.substring(sourcePath.lastIndexOf('.'), sourcePath.length());         	
//        	
//        	File file = new File(transformedPath);
//        	try {
//				file.createNewFile();
//				boolean success = transform(sourcePath,file);
//				if(success){
//					((FlexDecoderInfo)decInfo).setSourceFilePath(transformedPath);
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        
//        
//        } 

        try {               
            String path = decInfo.getSourceFilePath();
            Transcription transcription = new TranscriptionImpl(path, decInfo);           
            transcription.setChanged();

            int lastSlash = path.lastIndexOf('/');
            String flexPath = path.substring(0, lastSlash);
            boolean validMedia = true;

            if (frame != null) {
                validMedia = frame.checkMedia(transcription, flexPath);
            }

            if (!validMedia) {
                // ask if no media session is ok, if not return
                int answer = JOptionPane.showConfirmDialog(frame,
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaQuestion"),
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaAvailable"),
                        JOptionPane.YES_NO_OPTION);

                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            FrameManager.getInstance().createFrame(transcription);
        } catch (Exception e) {
        	ClientLogger.LOG.warning("Could not convert the FLEx file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
//    /**
//     * sets xsl-scripts
//     * @throws TransformerFactoryConfigurationError 
//     *
//     * @throws TransformerException
//     * @throws IOException
//     */
//    private boolean transform(String originalFileName, File transformedFile){ 
//    	String url = FileUtility.pathToURLString(originalFileName);
//    	 URL originalFileURL = null;
//		try {
//			originalFileURL = new URL(url);
//		} catch (MalformedURLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		String file = "/mpi/eudico/resources/flexTransformation.xsl";
//    	 URL flextTextURL = ImportFlexMA.class.getResource(file);     
//
//             
//           //transformation
//		   // HS May 2008: use a FileOutputStream instead of just the file to 
//		   // prevent a FileNotFoundException
//           try {
//        	  TransformerFactory.newInstance().newTransformer(new StreamSource(
//						 flextTextURL.openStream())).transform(new StreamSource(originalFileURL.openStream()),
//						            new StreamResult(new FileOutputStream(transformedFile)));
//			} catch (TransformerException e) {
//				e.printStackTrace();
//				return false;
//			} catch (TransformerFactoryConfigurationError e) {
//				e.printStackTrace();
//				return false;
//			}  catch (IOException e) {
//				e.printStackTrace();
//				return false;
//			}
//           
//            return true;
//    }    
}
