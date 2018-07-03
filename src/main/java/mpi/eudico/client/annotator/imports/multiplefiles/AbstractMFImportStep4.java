package mpi.eudico.client.annotator.imports.multiplefiles;

import java.io.File;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * Final step pane for the multiple file import functions 
 * 
 * @author aarsom
 * @created May 2012
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractMFImportStep4 extends ProgressStepPane implements ProcessReporter{	
	protected Object[] files;	
	private boolean useOriginalDir;
	private boolean newDir;
	private String newDirName;
	private boolean sameDir;
	private String sameDirName;
	private boolean useOriginalFileName;
	private boolean useOriginalFileNameWithSuffix;	
	protected String encoding;
	protected ProcessReport report;
	
	private final String EXTENSION = ".eaf";
	
	protected TranscriptionImpl transImpl;
	    
	/**
	 * Constructor for AbstractMFImportStep4
	 * 
	 * @param multiPane
	
	 */
	public AbstractMFImportStep4(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}	
	
	 /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("MultiFileImport.Step4.Title");
    }

    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	@Override
	public void enterStepForward() {              
        useOriginalDir = (Boolean)multiPane.getStepProperty("UseOriginalDir");
        newDir = (Boolean)multiPane.getStepProperty("NewDirectory");
        newDirName = (String)multiPane.getStepProperty("NewDirName");
        sameDir = (Boolean)multiPane.getStepProperty("TogetherInSameDir");        
        sameDirName = (String)multiPane.getStepProperty("SameDirectoryName");   
        useOriginalFileName = (Boolean)multiPane.getStepProperty("UseOriginalFileName");
        useOriginalFileNameWithSuffix = (Boolean)multiPane.getStepProperty("UseOriginalFileNameWithSuffix");     
        
        files = (Object[])multiPane.getStepProperty("FilesToBeImported");  
        encoding = (String)multiPane.getStepProperty("Encoding");  
    	
        doFinish();
    }
    
    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
    	completed = false;
        
    	// disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        
        report = new SimpleReport(multiPane.getDialog().getTitle());
		
        Thread t = new ImportThread();
		t.start();
		
		// the action is performed on a separate thread, don't close
        return false;
    }
    /**
     * Returns the proces report.
     *
     * @return the process report, or null
     */
    @Override
	public ProcessReport getProcessReport() {
        return report;
    }

    /**
     * Sets the process report.
     *
     * @param report the new report to append messages to
     */
    @Override
	public void setProcessReport(ProcessReport report) {
        this.report = report;
    }

    /**
     * Adds a message to the report.
     *
     * @param message the message
     */
    @Override
	public void report(String message) {
        if (report != null) {
            report.append(message);
        }
    }
    
    /**
     * The actual writing.
     *
     * @param fileName path to the file, can not null
     * @param orderedTiers tier names, ordered by the user, min size 1	     
     *
     * @return true if all went well, false otherwise
     */
    abstract protected boolean doImport(File sourceFile);
    
	private class ImportThread extends Thread{		
		private final int YES = 1, NO = 2, YES_TO_ALL = 0, NO_TO_ALL = 3;	
		
		/**
		 * Constructor
		 * 
		 */
		public ImportThread(){
		}
		
		/**
		 * Returns the directory where the corresponding file needs to be imported in.
		 * This depends on the selected options for the import
		 * 
		 * @param file particular file where the directory is computed for
		 * @return the directory where the file needs to be saved
		 */
		private String getDirectoryToSave( String fileName ){
			int index = fileName.lastIndexOf(File.separatorChar);
			String originalDirectory = fileName.substring( 0, index+1);
			
			//if original directory is selected
			if( useOriginalDir ){
				return originalDirectory;
			}
			
			//if new directory has been selected
			if( newDir )
				return originalDirectory + newDirName;
			
			//if same directory has been chosen
			if( sameDir)
				return sameDirName;
			
			return null;
		}
		
		/**
		 * Gets the file name of the import file and edits it to the format 
		 * specified for importing to eaf
		 * 
		 * @param transImpl the transcription implementation of the file
		 * @param path the path of the new file
		 * 
		 * @return the name of the file to be saved or null if no name could be found
		 */
		private String getFileName(String path, File sourceFile){						
			String orifileName = sourceFile.getName();			
			int index = orifileName.lastIndexOf('.');
			orifileName = orifileName.substring(0, index);
				
			if( useOriginalFileName ){		
				return orifileName + EXTENSION ;
			} 
			
			if( useOriginalFileNameWithSuffix ){
				int i = 1;
				String fileName = orifileName;
				File f = new File(path+fileName+EXTENSION);					
				while(f.exists()){
					fileName = orifileName + i;
					f = new File(path+fileName+EXTENSION);
					i = i+1;
				}					
				return fileName + EXTENSION;
			}
					
			return orifileName + EXTENSION;
			
		}
		
		/**
		 * Checks if the given directory exists. If it does not exist, it will
		 * try to create one.
		 * 
		 * @param directoryPath the path to an existing (or nonexisting) directory
		 * @return boolean value indicating if directory exists (and is created succesfully if needed)
		 */
		private boolean createDirectory( String directoryPath ){
			boolean directoryExists = true;
			File directory = new File(directoryPath);
			
			//if directory does not exist, try to make it
			if( !directory.exists() )
				directoryExists = (new File(directoryPath)).mkdir();
			
			return directoryExists;
		}
		
		/**
		 * The start of the save thread. All files will be saved here.
		 */
		@Override
		public void run(){
			int failedImports = 0; //counter to count number of failed imports
			int refusedImports = 0; //nr imports refused because user indicated that files should not overwrite existing files			
			boolean saveForever = false, skipForever = false;
			
			String fileName;
			String newfileName;			
			TranscriptionStore store;
			
			report(ElanLocale.getString("MultiFileImport.Report.NoOfFiles") + " " + files.length);      		     
			report("\n");
			
			//walk through all files
			for( int f=0; f < files.length; f++ ){
				
				if (((File)files[f]) == null || !((File)files[f]).exists()) {					
					if (((File)files[f]) != null) {	
			        	report(ElanLocale.getString("MultiFileImport.Report.ImportFileName") + " " + ((File)files[f]).getAbsolutePath());   
			        }	
					failedImports++;
					report(ElanLocale.getString("MultiFileImport.Report.NoFile"));	
					report(ElanLocale.getString("MultiFileImport.Report.ImportFailed"));		
					report("\n");
					continue;
		        }
				
				newfileName = null;
				fileName =  ((File)files[f]).getAbsolutePath();  
				
				report(ElanLocale.getString("MultiFileImport.Report.ImportFileName") + " " + fileName);      
							      
		        try {
		        	 String path = getDirectoryToSave( fileName );	
		        	 if (path.charAt(path.length() - 1) != File.separatorChar) {
		        		 path += File.separatorChar;
		        	 }
		        	 newfileName = path + getFileName( path, (File)files[f]);
		        	 String directoryToSave = newfileName;     	
		        	 
		        	 report(ElanLocale.getString("MultiFileImport.Report.ResultingFileName") + " " + newfileName);
		        	
		        	 //if directory does not exist, then create it
		        	 boolean directoryExists = createDirectory(path);
		        	 boolean saveThisFile = true;
		        	 boolean fileExists = new File(directoryToSave).exists();
					
		        	 if( fileExists && skipForever )
		        		 saveThisFile = false;
					
		        	 //overwrite files is not selected, then check if file exists and ask for overwriting
		        	 if( !skipForever && !saveForever  && fileExists ){
		        		 //show dialog to ask if existing file should be overwritten
		        		 Object[] possibleValues = { "Yes To All", "Yes", "No", "No To All" };
						
		        		 int choice = JOptionPane.showOptionDialog( null, 
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description1") + " " + newfileName + " " + ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description2") + "\n\n" +
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description3") + "\t " + directoryToSave + "\n\n" +
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description4") + "\n", ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Title"), 
										JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, possibleValues, possibleValues[2]);
						
		        		 switch( choice ){
								case YES:
									saveThisFile = true;
									break;
							
								case YES_TO_ALL:
									saveForever = true;
									break;
								
								case NO_TO_ALL:
									skipForever = true;
									saveThisFile = false;
									break;
								
								default: //NO and other
									saveThisFile = false;
		        		 }
							
		        	 }
					
		        	 //save files
		        	 if( directoryExists )
		        		 //if all files need to be saved OR this file need to be saved		        		
		        		 if( saveForever || saveThisFile ){		
		        			// transImpl = new TranscriptionImpl(fileName);
		        			 boolean imported = doImport((File)files[f]);	
		        			 if(!imported){
		        				 report(ElanLocale.getString("MultiFileImport.Report.ImportFailed"));
		        				 failedImports++;
		        			 } else {		        				 
		        	             store = ACMTranscriptionStore.getCurrentTranscriptionStore();
		        	             int saveAsType = SaveAs27Preferences.saveAsType(transImpl);
		        	         	 store.storeTranscription(transImpl, null, null, newfileName, saveAsType);
		        	         	 if(fileExists){
		        	         		report(ElanLocale.getString("MultiFileImport.Report.FileOverWrite"));
		        	         	 }
		        	         	 report(ElanLocale.getString("MultiFileImport.Report.ImportSucceed"));
		        			}
		        		 } else{
		        			 report(ElanLocale.getString("MultiFileImport.Report.FileExits"));
		        			 report(ElanLocale.getString("MultiFileImport.Report.ImportFailed"));
		        			 refusedImports++;
		        		 }
		        	 else  
		        		 failedImports++;
		         } catch (Exception ex) {
		                // catch any exception that could occur and continue
		                ClientLogger.LOG.warning("Could not handle file: " + fileName +"\n" + ex.getMessage());
		                report(ElanLocale.getString("MultiFileImport.Report.ExceptionOccured") + 
		                		"\n" + ex.getMessage());
		         }
				
		         //update progress bar
		        if((f+1) < files.length){
		        	progressUpdated(AbstractMFImportStep4.this, Math.round(100 * (f+1)/(float)files.length), null );
		        }
		        report("\n");
			}
			
			//show information on the export process
			String msg = (files.length-failedImports-refusedImports) + " " + ElanLocale.getString("ExportTiersDialog.Message.OutOf") + 
					" " + files.length + " " + ElanLocale.getString("MultiFileImport.Report.Msg.Success");
			
			//construct additional details if not all files are exported successfully
			if(  refusedImports > 0 ){
				msg += "\n" + refusedImports + " " + ElanLocale.getString("MultiFileImport.Report.Msg.RefusedImports");			
			}
			
			if(failedImports > 0){
				msg += "\n" + failedImports + " " + ElanLocale.getString("MultiFileImport.Report.Msg.FaliedImports");
			}
			
			report(ElanLocale.getString("MultiFileImport.Report.Summary"));
			report("\n" + msg);
			
			//exporting finished, so update progress bar
			progressCompleted(AbstractMFImportStep4.this, ElanLocale.getString("MultiFileImport.Step4.ImportFinished"));     
			new ReportDialog(report).setVisible(true);
		}
	}	
}
