package mpi.eudico.server.corpora.clomimpl.textconversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;

public class NystWordToTab {
	private String inFile = null;
	private long lastHMTime = 0L;
	private long lastT = -1L;
	private long timeShift = 0L;
	private final Pattern colonPat = Pattern.compile(":");
	private final String TAB = "\t";
	
	/**
	 * @param inFile
	 */
	public NystWordToTab(String inFile) {
		super();
		this.inFile = inFile;
		convert();
	}

	private void convert() {
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
		} catch (FileNotFoundException fe) {
			System.out.println("File not found: " + fe.getMessage());
			return;
		} catch (UnsupportedEncodingException nee) {
			System.out.println("Unsupported encoding: " + nee.getMessage());
			return;
		}
		BufferedWriter bw = null;
		try {
		bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(inFile + ".tab.txt"), "UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			System.out.println("Unsupported encoding: " + uee.getMessage());
			try {
				read.close();
			} catch (IOException e) {
			}
			return;
		} catch (FileNotFoundException fne) {
			System.out.println("File not found: " + fne.getMessage());
			try {
				read.close();
			} catch (IOException e) {
			}
			return;
		}
		
		Pattern pat = Pattern.compile("\t");
		String line;
		String trimmed;
		String[] tokens;
		long lineCount = 0;
		
		//boolean inBlock = false;
		boolean secLine = false;
		//long lastBT = -1L;
		String lastValues = null;
		
		try {
			while ((line = read.readLine()) != null) {
				lineCount++;
				trimmed = line.trim();
				if (trimmed.length() == 0) {
					secLine = false;
					continue;
				}
				tokens = pat.split(trimmed);
				if (tokens.length == 3) {// a line with time value
					long t = getTime(tokens[0]);
					
					if (t == -1) {
						System.out.println("Cannot extract time, continuing anyway... " + tokens[0] + " line: " + lineCount);
					}
					
					if (lastT != -1) {
						bw.newLine();
						bw.write(Long.toString(lastT));
						bw.write(TAB);
						bw.write(Long.toString(t - timeShift));
						bw.write(TAB);
						// skip second token is speaker "ID"
						bw.write(lastValues);
						//bw.write(TAB);
					} else if (t != -1) {
						timeShift = t;// shift anotations to start at time 0
					}
					lastT = t - timeShift;
					lastValues = tokens[2];
					// wait for second line
					secLine = true;
				} else if (tokens.length == 1 && secLine) {
					//add refline and newline
					//bw.write(tokens[0]);
					lastValues += TAB;
					lastValues += tokens[0];
					//bw.newLine(); // write teh new line just before the time values are written
					secLine = false;
				} else if (tokens.length == 1 && !secLine) {// ignore lines with only one "token" not being the second line
					
				}
			}
			// write last values
			if (lastT != -1) {
				bw.newLine();
				bw.write(Long.toString(lastT));
				bw.write(TAB);
				bw.write(Long.toString(lastT + 1000));
				bw.write(TAB);
				bw.write(lastValues);
			}
			bw.flush();
			System.out.println("Finished transforming file...");
		} catch (IOException ioe) {
			System.out.println("IO Exception: " + ioe.getMessage());
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		createEAF(inFile);
	}
	
	private void createEAF(String inFile) {
		TranscriptionImpl trans = new TranscriptionImpl();
		LinguisticType lt1 = new LinguisticType("text");
		lt1.setTimeAlignable(true);
		trans.addLinguisticType(lt1);
		LinguisticType lt2 = new LinguisticType("translation");
		lt2.setTimeAlignable(false);
		lt2.addConstraint(new SymbolicAssociation());
		trans.addLinguisticType(lt2);
		TierImpl textTier = new TierImpl("text", "", trans, lt1);
		trans.addTier(textTier);
		TierImpl transTier = new TierImpl(textTier, "trans", "", trans, lt2);
		trans.addTier(transTier);
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(new FileInputStream(inFile + ".tab.txt"), "UTF-8"));
		} catch (FileNotFoundException fe) {
			System.out.println("File not found 2: " + fe.getMessage());
			return;
		} catch (UnsupportedEncodingException nee) {
			System.out.println("Unsupported encoding 2: " + nee.getMessage());
			return;
		}
		
		Pattern pat = Pattern.compile("\t");
		String line;
		String[] tokens;
		long bt, et, mid;
		String par, ch;
		AbstractAnnotation parAnn, chAnn;
		
		try {
			while ((line = read.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				parAnn = null;
				chAnn = null;
				
				tokens = pat.split(line);
				if (tokens.length <= 3) {
					continue;
				}
				bt = getMsTime(tokens[0]);
				et = getMsTime(tokens[1]);
				if (bt == -1 || et == -1) {
					System.out.println("Begin or end time is unknown, skipping: " + line);
					continue;
				}
				par = tokens[2];
				if (tokens.length >= 4) {
					ch = tokens[3];
				} else {
					ch = "";
				}
				parAnn = (AbstractAnnotation) textTier.createAnnotation(bt, et);
				if (parAnn != null) {
					parAnn.setValue(par);
					mid = (bt + et) / 2;
					chAnn = (AbstractAnnotation) transTier.createAnnotation(mid, mid);
					if (chAnn != null) {
						chAnn.setValue(ch);
					} else {
						System.out.println("Cannot create child annotation: " + ch);
					}
				} else {
					System.out.println("Cannot create parent annotation: " + par + " line: " + line);
				}
			}
			
			read.close();
		} catch (IOException ioe) {
			System.out.println("IO Exception 2: " + ioe.getMessage());
		}
		
		try {
			TranscriptionStore store = ACMTranscriptionStore.getCurrentTranscriptionStore();
			int saveAsType = SaveAs27Preferences.saveAsType(trans);
			store.storeTranscription(trans, null, null, inFile + ".eaf", saveAsType);
		} catch (IOException ioe) {
			System.out.println("Cannot save transcription: " + ioe.getMessage());
		}
	}

	private long getTime(String time) {
		
		if (time.indexOf(':') > -1) {
			String[] hhmmss = colonPat.split(time);
			if (hhmmss.length == 3) {
				try {
					int h = Integer.parseInt(hhmmss[0], 10);
					int m = Integer.parseInt(hhmmss[1], 10);
					int s = Integer.parseInt(hhmmss[2], 10);
					long t = (h * (60 * 60 * 1000)) + (m * (60 * 1000));
					lastHMTime = t;
					t += (s * 1000);
					return t;
				} catch (NumberFormatException nfe) {
					System.out.println("Could not parse time...: " + time + " " + nfe.getMessage());
					return -1;
				}
			} else {
				System.out.println("Unexpected time format...:" + time);
			}
		} else {
			// single seconds value
			try {
				int t = Integer.parseInt(time, 10);
				return lastHMTime + (t * 1000);
			} catch (NumberFormatException nfe) {
				System.out.println("Could not parse single time...: " + time + " " + nfe.getMessage());
				return -1;
			}
		}
		
		return -1;
	}
	
	private long getMsTime(String msString) {
		try {
			return Long.parseLong(msString);
		} catch (NumberFormatException nfe) {
			System.out.println("Failed to convert ms string");
		}
		return -1L;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("No file...");
			return;
		}
		NystWordToTab inst = new NystWordToTab(args[0]);
	}

}
