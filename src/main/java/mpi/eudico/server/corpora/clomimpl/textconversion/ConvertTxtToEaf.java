package mpi.eudico.server.corpora.clomimpl.textconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * Class to create a eaf from the txt files(PenyBrown)
 * 
 * @author aarsom
 * @created April 2012
 */
public class ConvertTxtToEaf {	
	private final String TAB = "\t";
	private final String UNKNOWN_PART = "unknown";
	private final String UNKNOWN_SPEECH ="---";	
	private final long timeRqdForCh = 50L;	
	private final String CHAT_TIER = "*";
	
	public ConvertTxtToEaf(String inFile){			
		createEAF(inFile, null);
	}
	
	public ConvertTxtToEaf(String inFile, List<String> mediaFiles){			
		createEAF(inFile, mediaFiles);
	}
	
	/**
	 * @param inFile
	 */
	private void createEAF(String inFile, List<String> mediaFiles) {
		TranscriptionImpl trans = new TranscriptionImpl();	
		
		if(mediaFiles != null && mediaFiles.size() > 0){
			trans.setMediaDescriptors(MediaDescriptorUtil.createMediaDescriptors(mediaFiles));			
		}
		
		LinguisticType type = new LinguisticType("Text");
		type.setTimeAlignable(true);
		trans.addLinguisticType(type);
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
		} catch (FileNotFoundException fe) {
			System.out.println("File not found : " + fe.getMessage());
			return;
		}
		
		Pattern pat = Pattern.compile(TAB);
		String line;
		String[] tokens;
		String tierName, annVal;
		long bt = 0L, et =0L, dur = 0L; 
		AbstractAnnotation ann;
		TierImpl tier;
		
		System.out.println("Reading file... ");
		try {
			while ((line = read.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				ann = null;
				tokens = pat.split(line);
				if (tokens.length != 2) {
					continue;
				}
				
				tierName = tokens[0];
				annVal = tokens[1];
				
				
				if(tierName.startsWith(CHAT_TIER)){
					tierName= tierName.substring(1);
				} else if(tierName.trim().length() == 0){
					tierName = UNKNOWN_PART;
				} 
				
				if(tierName.endsWith(":")){
					tierName = tierName.substring(0, tierName.length()-1);
				}
				
				tier = (TierImpl) trans.getTierWithId(tierName);
				
				if( tier == null){
					tier = new TierImpl(tierName, "", trans, type);		
					trans.addTier(tier);
				}	
				
				dur = getDuration(annVal);
				bt = et;
				et = et + dur;
				
				ann = (AbstractAnnotation) tier.createAnnotation(bt, et);
				if (ann != null) {
					ann.setValue(annVal);				
				} else {
					System.out.println("Cannot create annotation: " + annVal + " line: " + line);
				}
			}
			
			read.close();
		} catch (IOException ioe) {
			System.out.println("IO Exception : " + ioe.getMessage());
		}
		
		try {
			String fileName = getValidFileName(inFile);
			if(fileName == null){
				System.out.println("Transcription not created for '" +inFile+"'." );
				return;
			}
			TranscriptionStore store = ACMTranscriptionStore.getCurrentTranscriptionStore();
			int saveAsType = SaveAs27Preferences.saveAsType(trans);
			store.storeTranscription(trans, null, null, fileName, saveAsType);
		} catch (IOException ioe) {
			System.out.println("Cannot save transcription: " + ioe.getMessage());
		}
	}
	
	private String getValidFileName(String infile){
		String fileName = infile;
		if(fileName != null){
	        int li = infile.lastIndexOf(".");
	        fileName = infile.substring(0, li)+".eaf";			
		}		
		
		if((new File(fileName)).exists()){
			int option = JOptionPane.showConfirmDialog(null, fileName+" already exists. Do you want to overwrite the existing file", 
					"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if(option == JOptionPane.YES_OPTION){
				return fileName;	
			}	 else{ 
				FileChooser chooser = new FileChooser(null);
				chooser.setCurrentDirectory(infile);
				chooser.createAndShowFileDialog("", FileChooser.SAVE_DIALOG, null, null, FileExtension.EAF_EXT, true, null, FileChooser.FILES_ONLY, fileName);
				if(chooser.getSelectedFile() == null){
					return null;
				}
				return chooser.getSelectedFile().getAbsolutePath();
			}
		}
		
		return fileName;	
	}
	
	
	private long getDuration(String value){
		long dur =0l;
		int numOfChars = value.length();				
		if(value.contains(UNKNOWN_SPEECH)){
			numOfChars = numOfChars - UNKNOWN_SPEECH.length();
			dur = numOfChars * timeRqdForCh + 1000L;
		} else {
			dur = numOfChars * timeRqdForCh;
		}
		return dur;				
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("No file...");
			return;
		}	
		
		ConvertTxtToEaf inst;
		File dir = new File(args[0]);
		if(!dir.exists()){
			System.out.println(dir + " file does not exist");
			return;
		}
		
		if(dir.isDirectory()){					
			for(File file : dir.listFiles()){
				if (file.getName().endsWith((".txt")) || file.getName().endsWith((".TXT"))) {
					inst = new ConvertTxtToEaf(file.getAbsolutePath());
				}
			}
		} else if(dir.isFile()){
			List<String> mediaFiles = new ArrayList<String>();
			for(int i=1; i< args.length; i++){
				mediaFiles.add(args[i]);
			}
			inst = new ConvertTxtToEaf(args[0], mediaFiles);
		}
	}
}
