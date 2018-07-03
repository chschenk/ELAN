  package mpi.eudico.client.annotator.imports.multiplefiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
import mpi.eudico.server.corpora.util.ServerLogger;

public class MFToolboxImportStep4 extends AbstractMFImportStep4{		
	private ToolboxDecoderInfo2 decoderInfo;
	private String typFileLinked;
	
	public MFToolboxImportStep4(MultiStepPane multiPane) {
		super(multiPane);		
	}
	
	@Override
	public void enterStepForward() {  
		decoderInfo = (ToolboxDecoderInfo2) multiPane.getStepProperty("ToolboxDecoderInfo");		 
	    super.enterStepForward();
    }
	
	@Override
	protected boolean doImport(File sourceFile) {		
        final File impFile = sourceFile;        

        if ((impFile == null) || !impFile.exists()) {
        	//progressInterrupted(null, ElanLocale.getString("MultiFileImport.Report.NoFile"));   
        	ClientLogger.LOG.severe("Toolbox file not found :" + impFile.getAbsolutePath());
 			report(ElanLocale.getString("MultiFileImport.Report.NoFile"));	            
        	return false;
        }
        
        if(decoderInfo == null){
        	return false;
        }

        decoderInfo.setSourceFilePath(impFile.getAbsolutePath());  
        parseFile(impFile);
        
        if(typFileLinked != null ){
        	String typeFileName = FileUtility.fileNameFromPath(decoderInfo.getTypeFile());
        	if (typeFileName != null && typeFileName.length() > 0) {// could be a marker file has been specified
	            int li = typeFileName.lastIndexOf(".");
	            typeFileName = typeFileName.substring(0 , li);
	            if(!typeFileName.trim().equals(typFileLinked.trim())){
	//            	ClientLogger.LOG.warning("Type file mismatch");     	
	            	report("Type file mismatch");
	            	report("Type file used for import : " + typeFileName.trim());
	            	report("Required type file : " + typFileLinked.trim());
	            	report("The type file mentioned in the toolbox file is different from the type file selected for this import.");    
	            	report("If an eaf is imported, that might miss some details like tiers,dependency etc..");    
	            }
        	} else if (decoderInfo.getShoeboxMarkers() == null || decoderInfo.getShoeboxMarkers().isEmpty()) {
        		report("Neither a .typ file has been specified nor custom markers have been created. The import will probably fail.");
        	}
        }
        
        try {
        	transImpl = new TranscriptionImpl(impFile.getAbsolutePath(), decoderInfo);
        } catch (Exception e){
        	ClientLogger.LOG.warning(ElanLocale.getString("MultiFileImport.Report.ExceptionOccured : ")+ e.getMessage());     	
        	report(ElanLocale.getString("MultiFileImport.Report.ExceptionOccured : ") + e.getMessage());       
        	return false;
        }
         
		return true;
	}
	
	 private void parseFile(File toolboxFile) {
	        Reader reader;
	        BufferedReader bufRead = null;	        
	        typFileLinked = null;

	        try {
	            if (decoderInfo.isAllUnicode()) {
	                reader = new InputStreamReader(new FileInputStream(toolboxFile),
	                        "UTF-8");
	                bufRead = new BufferedReader(reader);
	            } else {
	                reader = new InputStreamReader(new FileInputStream(toolboxFile),
	                        "ISO-8859-1");
	                bufRead = new BufferedReader(reader);
	            }
	        } catch (FileNotFoundException fne) {
	            ClientLogger.LOG.severe("Toolbox file not found :" + toolboxFile.getAbsolutePath());
	            report(ElanLocale.getString("MultiFileImport.Report.NoFile"));	
	            return;
	        } catch (UnsupportedEncodingException uee) {
	        	ClientLogger.LOG.severe("Encoding not supported"); //unlikely

	            return;
	        }

	        String line = null;
	        int lineCount = 0;
	        
	        try {
	            while ((line = bufRead.readLine()) != null) {
	                line = line.trim(); // trim the line immediately after reading
	                lineCount++;

	                if ((lineCount <= 3) &&
	                        ((line.indexOf("\\_sh v4.0") > -1) ||
	                        (line.indexOf("\\_sh v3.0") > -1))) {
	                   
	                	int lastSpaceIndex = line.trim().lastIndexOf(' ');

	                    if (lastSpaceIndex > -1) {
	                    	typFileLinked = line.substring(lastSpaceIndex).trim();
	                        ServerLogger.LOG.info("Database type in header: " + typFileLinked);
	                        break;
	                    }
	                }

	                if ((lineCount > 3)) {
	                	ClientLogger.LOG.severe("No Toolbox header found, no Toolbox file? :" + toolboxFile.getAbsolutePath());
	    	            report("No Toolbox header found, no Toolbox file?");
	                    break;
	                }
	            }
	        } catch (IOException ioe) {
	        	ClientLogger.LOG.severe("Error reading file: " + ioe.getMessage());
	        	report("Error reading file: " + ioe.getMessage());
	        } finally {
				try {
					if (bufRead != null) {
						bufRead.close();
					}
				} catch (IOException e) {
				}
	        }
	    }
}
