package mpi.eudico.client.annotator.commands;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.MutableInt;

/**
 * A command to export overlap information form a single file.
 * 
 * @author Han Sloetjes
 */
public class ExportOverlapsCommand implements Command {
	private String name;
	private Writer singleWriter;
	
	final String NL = "\n";
	final String TAB = "\t";
	final String Y = "1";
	final String N = "0";
	
	/**
	 * Constructor. 
	 * @param name
	 */
	public ExportOverlapsCommand(String name) {
		this.name = name;
	}

	/**
	 * Exports overlap information form a single file.
	 * 
	 * @param receiver an output writer or null
	 * @param arguments the arguments:
	 * <ul><li>arg[0] = the file to process (String)</li>
	 * <li>arg[1] = the first, reference tier (String)</li>
	 * <li>arg[2] = the tiers to compare (List[String])</li>
	 * <li>arg[3] (optional) = the file to write to (String)</li></ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		Writer outWriter = (Writer) receiver;
		String refTier = (String) arguments[1];
		List<String> compTiers = (List<String>) arguments[2];
		if (outWriter == null && arguments.length > 3) {
			try {
				singleWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream((String) arguments[3]), "UTF-8"));
				// output layout
				// bt > et > refTier > ct overlap > ct samevalue > ct ov duration > ct number of overlaps > ct value(s)
				singleWriter.write("Begin time");
				singleWriter.write(TAB);
				singleWriter.write("End time");
				singleWriter.write(TAB);
				singleWriter.write(refTier);
				singleWriter.write(TAB);
				for (int i = 0; i < compTiers.size(); i++) {
					String nm = compTiers.get(i);
					singleWriter.write(nm + "-ov");
					singleWriter.write(TAB);
					singleWriter.write(nm + "-same");
					singleWriter.write(TAB);
					singleWriter.write(nm + "ov-dur");
					singleWriter.write(TAB);
					singleWriter.write(nm + "-no-ann");
					singleWriter.write(TAB);
					singleWriter.write(nm + "-value");
					singleWriter.write(TAB);
				}
				singleWriter.write(NL);
				outWriter = singleWriter;
			} catch (FileNotFoundException fnfe) {
				ClientLogger.LOG.warning("Export failed: " + fnfe.getMessage());
				return;
			} catch (UnsupportedEncodingException uee) {
				ClientLogger.LOG.warning("Export failed: " + uee.getMessage());
				return;
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Export failed: " + ioe.getMessage());
				return;
			}
		}
		
		if (outWriter == null) {
			ClientLogger.LOG.warning("Export failed: no file to write to.");
			return;
		}
		
		try {
			TranscriptionImpl trans = new TranscriptionImpl((String) arguments[0]);
			exportOverlaps(outWriter, trans, refTier, compTiers);
			outWriter.flush();
		} catch (Exception ex) {// catch any exception that can occur
			ClientLogger.LOG.warning("Export failed: " + ex.getMessage());
		}
		
		if (singleWriter != null) {
			try {
				singleWriter.flush();
				singleWriter.close();
			} catch (IOException ioe) {}// do nothing
		}
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Calculates and writes overlap information from one transcription to the output stream.
	 * Always one tier is iteratively compared with one or more other tiers, 
	 * for each of the other tiers a number of columns is added.
	 * TO DO: what columns to export should be made flexible
	 * 
	 * @param outWriter the writer
	 * @param tr the transcription
	 * @param refTier the reference tier, all annotations of this tier will be compared with other annotations
	 * @param compTiers the tiers to compare with
	 * @throws IOException any io exception
	 */
	private void exportOverlaps(Writer outWriter, TranscriptionImpl tr, String refTier, List<String> compTiers) 
		throws IOException{

		outWriter.write(tr.getPathName());
		outWriter.write(NL);
		
		TierImpl tier = tr.getTierWithId(refTier);
		if (tier == null) {
			ClientLogger.LOG.warning("The reference tier was not found in this transcription.");
			return;
		}
		List<AbstractAnnotation> refAnnos = tier.getAnnotations();
		int numRows = refAnnos.size();
		if (numRows == 0) {
			return;
		}
		int numComs = compTiers.size();
		Map<String, List<AbstractAnnotation>> annosForTiers = new HashMap<String, List<AbstractAnnotation>>(compTiers.size());
		Map<String, MutableInt> curAnnIndex = new HashMap<String, MutableInt>(compTiers.size());
		
		// try to fill the maps
		for (int i = 0; i < compTiers.size(); i++) {
			String name = compTiers.get(i);
			TierImpl nt = tr.getTierWithId(name);
			if (nt != null) {
				annosForTiers.put(name, nt.getAnnotations());
			} else {
				annosForTiers.put(name, null);
			}
			curAnnIndex.put(name, new MutableInt(0));
		}
		
		// start looping over source annotations
		long bt = 0;
		long et = 0;
		int k = 0;
		AbstractAnnotation aa1, aa2;
		List<AbstractAnnotation> annos;
		
		for (int i = 0; i < refAnnos.size(); i++) {
			aa1 = refAnnos.get(i);
			bt = aa1.getBeginTimeBoundary();
			et = aa1.getEndTimeBoundary();
			outWriter.write(String.valueOf(bt));
			outWriter.write(TAB);
			outWriter.write(String.valueOf(et));
			outWriter.write(TAB);
			outWriter.write(String.valueOf(et - bt));
			outWriter.write(TAB);
			outWriter.write(aa1.getValue());
			outWriter.write(TAB);
			
			for (int j = 0; j < numComs; j++) {
				String tName = compTiers.get(j);
				annos = annosForTiers.get(tName);
				if (annos == null) {
					outWriter.write(TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB);
					continue;
				}
				k = curAnnIndex.get(tName).intValue;
				long obt, oet;
				String annValue = new String();
				int numOv = 0;
				long dur = 0;
				boolean same = false;
				AbstractAnnotation nextAfterRef = null;
				
				for (; k < annos.size(); k++) {
					aa2 = annos.get(k);
					obt = aa2.getBeginTimeBoundary();
					oet = aa2.getEndTimeBoundary();
					
					if (oet <= bt) {
						continue;
					} else if (obt >= et) {
						curAnnIndex.get(tName).intValue = k;
						nextAfterRef = aa2;
						break;
					} else {
						if (annValue.length() == 0) {
							annValue = aa2.getValue();
						} else {
							annValue = annValue + ", " + aa2.getValue();
						}
						numOv++;
						 if (numOv == 1 && aa2.getValue().equals(aa1.getValue())) {
							 same = true;
						 } else {
							 same = false;
						 }
						 dur += Math.min(et, oet) - Math.max(bt, obt);
						 
						 if (oet > et && obt < et) {
							curAnnIndex.get(tName).intValue = k;
							if (k < annos.size() - 2) {
								nextAfterRef = annos.get(k + 1);
							}
							break;
						 }
					}
				}
				// write
				if (numOv == 0) {
					outWriter.write(N);// is there overlap
					outWriter.write(TAB);
					outWriter.write(N); //is the value the same
					outWriter.write(TAB);
					outWriter.write(String.valueOf(dur));// the duration of overlap, 0
					outWriter.write(TAB);
					outWriter.write(String.valueOf(numOv));// the number of overlapping annotations, 0
					outWriter.write(TAB);
					//outWriter.write(annValue);// the total value
					outWriter.write(TAB);
				} else {
					outWriter.write(Y);// is there overlap
					outWriter.write(TAB);
					outWriter.write(same ? Y : N ); //is the value the same
					outWriter.write(TAB);
					outWriter.write(String.valueOf(dur));// the duration of overlap
					outWriter.write(TAB);
					outWriter.write(String.valueOf(numOv));// the number of overlapping annotations
					outWriter.write(TAB);
					outWriter.write(annValue);// the total value
					outWriter.write(TAB);
				}
				// write info about the next annotation starting after the current reference annotation
				if (nextAfterRef != null) {
					obt = nextAfterRef.getBeginTimeBoundary();
					oet = nextAfterRef.getEndTimeBoundary();
					// output obt - bt, obt - et (oet - et?) 
					outWriter.write(String.valueOf(obt - bt));
					outWriter.write(TAB);
					outWriter.write(String.valueOf(obt - et));
					outWriter.write(TAB);
					outWriter.write(String.valueOf(oet - et));
					outWriter.write(TAB);
					outWriter.write(String.valueOf(obt));
					outWriter.write(TAB);
					outWriter.write(String.valueOf(oet));
					outWriter.write(TAB);
					outWriter.write(nextAfterRef.getValue());
					outWriter.write(TAB);
				} else {
					outWriter.write("0");// or empty?
					outWriter.write(TAB);
					outWriter.write("0");
					outWriter.write(TAB);
					outWriter.write("0");
					outWriter.write(TAB);
					outWriter.write("0");// or empty?
					outWriter.write(TAB);
					outWriter.write("0");
					outWriter.write(TAB);
					//outWriter.write("");
					outWriter.write(TAB);
				}
				
			} // compare tiers loop
			
			outWriter.write(NL);
		} // reference annotations loop
		outWriter.write(NL);
	}

}
