package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;

/**
 * Converter for a very specific type of Comma Separated Values file.
 * Files provided by Sophie Sallfner. A repetition of rows with tiernames in the 
 * first column, followed by multiple values in the next columns, or just 1 value
 * followed by multiple "empty" values (an annotation spanning multiple other 
 * annotations.
 *  
 * @author Han Sloetjes
 *
 */
public class CSVWrapped2EAF {
	private String filePath;
	private TranscriptionImpl trans;
	private String refLine = "Ikaan;";
	private final String delim = "\t";
	private final String delim2 = ";";
	private int phraseDur = 5000;
	
	Pattern pattern = Pattern.compile(delim2);
	
	public CSVWrapped2EAF(String filePath) {
		this.filePath = filePath;
		trans = new TranscriptionImpl();
		addTiers();
		parse();
	}
	
	private void parse() {
        BufferedReader bufRead = null;
		try {
	        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
	        		filePath), "UTF-16");
	
	        //FileReader fileReader = new FileReader(sourceFile);
	        bufRead = new BufferedReader(fileReader);
	        String line;
	        
	        int numLines = 0;
	        int blockIndex = 0;
	        List<String> curLines = new ArrayList<String>(10);

	        while (((line = bufRead.readLine()) != null)) {
	            if (line.length() <= 1 /*|| line.startsWith("#")*/) {//problems with utf-16 encoding on Mac
	            	//numLines++;
	                continue;
	            }
	            //System.out.println("Last char: " + line.codePointAt(line.length() - 1));
	            numLines++;
	            if (numLines == 1) {//wav file
	            	int sind = line.indexOf(' ');
	            	int firstSC = line.indexOf(delim);
	            	if (sind > -1 && firstSC > sind) {
	            		String wavFile = line.substring(sind, firstSC).trim();
	            		MediaDescriptor md = new MediaDescriptor(wavFile, MediaDescriptor.WAV_MIME_TYPE);
	            		md.relativeMediaURL = wavFile;
	            		List<MediaDescriptor> mdc = new ArrayList<MediaDescriptor>(1);
	            		mdc.add(md);
	            		trans.setMediaDescriptors(mdc);
	            	}
	            } else {
	            	line = line.replace(delim, delim2);
	            	if (line.startsWith(refLine)) {
	            		if (curLines.size() > 0) {
	            			convertCurLines(curLines, blockIndex);
	            			blockIndex++;
	            			curLines.clear();
	            		}
	            		curLines.add(line);
	            	} else {
	            		curLines.add(line);
	            	}
	            }
	            
	        }
	        convertCurLines(curLines, ++blockIndex);
	        // write transcription
			int saveAsType = SaveAs27Preferences.saveAsType(trans);
	        String eafFile = filePath.substring(0, filePath.length() - 3) + "eaf";
	        ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscription(trans, null, 
	        		new ArrayList<TierImpl>(0), eafFile, saveAsType);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bufRead != null) {
					bufRead.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void convertCurLines(List<String> lines, int blockIndex) {
		if (lines == null || lines.size() == 0) {
			return;
		}
		
		ArrayList<String[]> lineArrs = new ArrayList<String[]>(lines.size());
		int maxNumSubs = 1; // contains tier name
		//StringTokenizer tokenizer;
		
		for (int i = 0 ; i < lines.size(); i++) {
			//tokenizer = new StringTokenizer(lines.get(i), delim);
			
			String[] tokens = pattern.split(lines.get(i));
			int numtok = tokens.length;
			//int numtok = tokenizer.countTokens();
			if (numtok > maxNumSubs) {
				if (!tokens[0].equals("English translation") && !tokens[0].equals("Notes and comments")) {
					maxNumSubs = numtok;
				}
			}
			//String[] toks = new String[numtok];
			//int c = 0;
			//while(tokenizer.hasMoreTokens()) {
			//	toks[c++] = tokenizer.nextToken().trim();
			//}
			//lineArrs.add(toks);
			if (tokens.length > 0) {
				lineArrs.add(tokens);
			}
		}
		if (maxNumSubs == 1) {
			return;
		}
		TierImpl curTier, topLevelTier;
		long bt = blockIndex * phraseDur;
		long perAnn = phraseDur / (maxNumSubs - 1);
		long et = bt + phraseDur;
		et = bt + (maxNumSubs - 1) * perAnn;//prevent rounding errors
		String nm = "phrase";
		topLevelTier = trans.getTierWithId(nm);
		if (topLevelTier != null) {
			Annotation ann = topLevelTier.createAnnotation(bt, et);
			ann.setValue("" + blockIndex);
		}

		String[] curArr; 
		for (int i = 0; i < lineArrs.size(); i++) {
			//int numCreated = 0;
			curArr = lineArrs.get(i);
			nm = curArr[0];
			curTier = trans.getTierWithId(nm);
			if (curTier != null) {
				if (curTier.getParentTier() == topLevelTier) {
					if (curTier.getName().equals("Ikaan")) {
						Annotation prevAnn = null, nextAnn = null;
						for (int j = 1; j < curArr.length; j++) {
							if (j == 1) {
								prevAnn = curTier.createAnnotation((bt + et) / 2, (bt + et) / 2);
								prevAnn.setValue(curArr[j].trim());
								//numCreated++;
							}
							else {
								nextAnn = curTier.createAnnotationAfter(prevAnn);
								nextAnn.setValue(curArr[j].trim());
								prevAnn = nextAnn;
								//numCreated++;
							}
						}
						if (curArr.length < maxNumSubs) {
							// add empty
							for (int j = curArr.length; j < maxNumSubs; j++) {
								nextAnn = curTier.createAnnotationAfter(prevAnn);
								prevAnn = nextAnn;
								//numCreated++;
							}
						}
//						System.out.println("created: " + numCreated);
//						Annotation ta = topLevelTier.getAnnotationAtTime((bt + et) / 2);
//						System.out.println("topl : " + ta.getBeginTimeBoundary() + " " + ta.getEndTimeBoundary());
//						ArrayList pl = ((AlignableAnnotation)ta).getParentListeners();
//						System.out.println("num child: " + pl.size());
//						for (int z = 0; z < pl.size(); z++) {
//							Annotation ch = (Annotation)pl.get(z);
//							System.out.println(z + ": " + ch.getBeginTimeBoundary() + "-" + ch.getEndTimeBoundary());
//						}
					} else {
						if (curArr.length > 1) {
							Annotation ann = curTier.createAnnotation((bt + et) / 2, (bt + et) / 2);
							ann.setValue(curArr[1].trim());
							if (curArr.length > 2) {
								for (int j = 2; j< curArr.length; j++) {
									ann.setValue(ann.getValue() + " " + curArr[j]);
								}
							}
						}
					}
				} else { // associated with Ikaan
					if (curArr.length == 1) {
						continue;
					}
					//long step = phraseDur / (curArr.length - 1);
					long step = phraseDur / (maxNumSubs - 1);
					Annotation ann;
					for (int j = 1; j < curArr.length; j++) {
						long time = bt + (step / 2) + (step * (j - 1));
						//System.out.println("time: " + time);
						//long time = bt + 1 + (step * (j - 1));
						ann = curTier.createAnnotation(time, time);
						if (ann == null) {
							System.out.println("null");
							Annotation exAnn = curTier.getAnnotationAtTime(time);
							System.out.println(exAnn.getBeginTimeBoundary() + "-" + exAnn.getEndTimeBoundary());
						}
						ann.setValue(curArr[j].trim());
					}
				}
			}
		}
	}
	/*
	private void addTier(String name, int type) {
		LinguisticType lt = new LinguisticType(name);
		if (type == -1) {
			lt.setTimeAlignable(true);
		} else if (type == Constraint.SYMBOLIC_SUBDIVISION) {
			lt.setTimeAlignable(false);
			lt.addConstraint(new SymbolicSubdivision());
		} else if (type == Constraint.SYMBOLIC_ASSOCIATION) {
			lt.setTimeAlignable(false);
			lt.addConstraint(new SymbolicAssociation());
		}
		trans.addLinguisticType(lt);
		TierImpl t = new TierImpl(name, "", trans, lt);
		trans.addTier(t);
	}
	*/
	private void addTiers() {
		String name = "phrase";
		LinguisticType lt = new LinguisticType(name);
		lt.setTimeAlignable(true);
		trans.addLinguisticType(lt);
		TierImpl t = new TierImpl(name, "", trans, lt);
		trans.addTier(t);
		TierImpl ikaTier, topLevelTier;
		topLevelTier = t;
		
		name = "Ikaan";
		lt = new LinguisticType(name);
		lt.setTimeAlignable(false);
		lt.addConstraint(new SymbolicSubdivision());
		trans.addLinguisticType(lt);
		ikaTier = new TierImpl(name, "", trans, lt);
		ikaTier.setParentTier(topLevelTier);
		trans.addTier(ikaTier);
		
		lt = new LinguisticType("association");
		lt.setTimeAlignable(false);
		lt.addConstraint(new SymbolicAssociation());
		trans.addLinguisticType(lt);
		
		name = "Ikaan words";
		t = new TierImpl(name, "", trans, lt);
		t.setParentTier(ikaTier);//??
		trans.addTier(t);
		
		name = "Corrections";
		t = new TierImpl(name, "", trans, lt);
		t.setParentTier(ikaTier);//??
		trans.addTier(t);
		
		name = "Crude translation";
		t = new TierImpl(name, "", trans, lt);
		t.setParentTier(ikaTier);
		trans.addTier(t);		
		
		name = "English translation";
		t = new TierImpl(name, "", trans, lt);
		t.setParentTier(topLevelTier);
		trans.addTier(t);
		
		name = "Notes and comments";
		t = new TierImpl(name, "", trans, lt);
		t.setParentTier(topLevelTier);
		trans.addTier(t);
	}
	
	public static void main(String[] args) {
		//CSVWrapped2EAF csv2eaf = new CSVWrapped2EAF(args[0]);
		//csv2eaf.parse();
		CSVWrapped2EAF csv2eaf = null;
		if (args != null && args.length > 0) {
			try {
				File dir = new File(args[0]);
				File[] files = dir.listFiles();
				for (File file : files) {
					if (file.getName().endsWith(".txt")) {
						csv2eaf = new CSVWrapped2EAF(file.getAbsolutePath());
						System.out.println("Processed: " + file.getAbsolutePath());
					}
				}
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
}
