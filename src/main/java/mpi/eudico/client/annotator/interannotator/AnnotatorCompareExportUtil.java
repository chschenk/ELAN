package mpi.eudico.client.annotator.interannotator;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.TierNameCompare;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A utility class for exporting the results of a rater compare.
 */
public class AnnotatorCompareExportUtil {
	private TranscriptionImpl transcription;
	private Component parent = null;
	private char delimiter = '_';// needs to be settable
	private int affixMode = TierNameCompare.SUFFIX_MODE;// needs to be settable
    String TAB = "\t";
    String NL = "\n";
	
	public AnnotatorCompareExportUtil(TranscriptionImpl transcription, Frame parent) {
		super();
		this.transcription = transcription;
		this.parent = parent;
	}
	
	public AnnotatorCompareExportUtil(TranscriptionImpl transcription, Dialog parent) {
		super();
		this.transcription = transcription;
		this.parent = parent;
	}
	
	public void doExport() throws IOException{
		// prompt for file
		FileChooser chooser = new FileChooser(parent);		
		chooser.createAndShowFileAndEncodingDialog(null,  FileChooser.SAVE_DIALOG, 
				FileExtension.TEXT_EXT, "LastUsedExportDir", FileChooser.UTF_8);
		File exportFile = chooser.getSelectedFile();
		String encoding = chooser.getSelectedEncoding();
		
		if (exportFile != null) {
			BufferedWriter writer = null;

            try {
                FileOutputStream out = new FileOutputStream(exportFile);
                OutputStreamWriter osw = null;

                try {
                    osw = new OutputStreamWriter(out, encoding);
                } catch (UnsupportedCharsetException uce) {
                    osw = new OutputStreamWriter(out, "UTF-8");
                }

                writer = new BufferedWriter(osw);
                writer.write(transcription.getFullPath());
                writer.write(NL);
                writer.write(NL);
            } catch (Exception ex) {
                // FileNotFound, Security or UnsupportedEncoding exceptions
            	if (writer != null) {
            		writer.close();
            	}
                throw new IOException("Cannot write to file: " + ex.getMessage());
            }
            
			// find tier pairs, store processed tiers
			List<String> exportedTiers = new ArrayList<String>();
			List<String> tierNames = new ArrayList<String>(transcription.getTiers().size());
			
			for (int i = 0; i < transcription.getTiers().size(); i++) {
				tierNames.add(((Tier) transcription.getTiers().get(i)).getName());
			}
			
			String tierName;
			String tierName2;
			String r1Suffix = null;

			for (int i = 0; i < tierNames.size(); i++) {
				tierName = tierNames.get(i);
				if (exportedTiers.contains(tierName)) {
					continue;
				}
				tierName2 = findCorrespondingTierName(tierName, tierNames);
				if (tierName2 != null) {
					// with the first combination of tiers determine which suffix should be R1
					if (r1Suffix == null) {
						r1Suffix = findFirstOrderSuffix(tierName, tierName2);
					}
					// compare and export
					try {
						if (r1Suffix == null || tierName.endsWith(r1Suffix)) {
							write(writer, tierName, tierName2);
						} else {
							write(writer, tierName2, tierName);
						}
						
					} catch (IOException ex) {
						System.out.println("Error exporting the tiers: " + tierName + " + " + tierName2);
					}
					exportedTiers.add(tierName2);
				}
				exportedTiers.add(tierName);
			}
			
			try {
				writer.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	
	private void write(BufferedWriter writer, String tierName1, String tierName2) throws IOException {
		writer.write("R1 (columns): " + tierName1);
		writer.write(NL);
		writer.write("R2 (rows): " + tierName2);
		writer.write(NL);
		writer.write(NL);
		
		AnnotatorCompareTable rct = new AnnotatorCompareTable();
		int[][] segmentationTable;
		TableModel model = rct.getComparisonTable(transcription, tierName1, tierName2, 0.60f);// hardcoded for now
		if (model != null) {
            for (int i = 0; i < model.getColumnCount(); i++) {
            	writer.write(model.getColumnName(i));
            	if (i < model.getColumnCount() - 1) {
            		writer.write(TAB);
            	}
            }
            writer.write(NL);
            writer.write(NL);
            segmentationTable = new int[2][2];
            
            for (int i = 0; i < model.getRowCount(); i++) {
            	for (int j = 0; j < model.getColumnCount(); j++) {
            		writer.write((String) model.getValueAt(i, j));
            		if (j < model.getColumnCount() - 1) {
            			writer.write(TAB);
            		} else {
            			writer.write(NL);
            		}
            		// update the segmentation agreement table for these tiers
            		if (j > 0) {
            			if (i < model.getRowCount() - 1) {
            				if (j < model.getColumnCount() - 1) {
            					// represents a match in terms of time alignment
            					try {
            						int num = Integer.valueOf((String) model.getValueAt(i, j));
            						segmentationTable[0][0] += num;
            					} catch (NumberFormatException nfe) {
            						System.out.println("ERROR: non numeric value in row,col: " + i + "-" + j);
            					}
            				} else if (j == model.getColumnCount() - 1) {
            					// an "orphan" segment by R1
            					try {
            						int num = Integer.valueOf((String) model.getValueAt(i, j));
            						segmentationTable[0][1] += num;
            					} catch (NumberFormatException nfe) {
            						System.out.println("ERROR: non numeric value in row,col: " + i + "-" + j);
            					}
            				}
            			} else if (i == model.getRowCount() - 1) {
            				// an "orphan" segment by R2 
        					try {
        						int num = Integer.valueOf((String) model.getValueAt(i, j));
        						segmentationTable[1][0] += num;
        					} catch (NumberFormatException nfe) {
        						System.out.println("ERROR: non numeric value in row,col: " + i + "-" + j);
        					}
            			}
            		}
            	}
            }
            // write segmentation matrix
            writer.write(NL);
            writer.write("Segmentation matrix");
            writer.write(NL);
            writer.write(String.valueOf(segmentationTable[0][0]));
            writer.write(TAB);
            writer.write(String.valueOf(segmentationTable[0][1]));
            writer.write(NL);
            writer.write(String.valueOf(segmentationTable[1][0]));
            writer.write(TAB);
            writer.write(String.valueOf(segmentationTable[1][1]));
            
            writer.write(NL);
            writer.write(NL);
		} else {
			writer.write("Could not create a combination table for the tiers");
            writer.write(NL);
            writer.write(NL);
		}
		
	}

	/**
	 * Returns the (first) matching tier name, based on current suffix/prefix mode.
	 * 
	 * @param refTier the reference tier
	 * @param allTierNames the list of all tier names
	 * @return the name of the matching tier 
	 */
	private String findCorrespondingTierName(String refTier, List<String> allTierNames) {
		int delIndex;
		if (affixMode == TierNameCompare.SUFFIX_MODE) {
			delIndex = refTier.lastIndexOf(delimiter);
			if (delIndex == -1 || delIndex == refTier.length() - 1) {
				return null;
			}
			
			String firstPart1 = refTier.substring(0, delIndex);
			String lastPart1 = refTier.substring(delIndex + 1);
			String firstPart2;
			String lastPart2;
			String otherName;
			for (int i = 0; i < allTierNames.size(); i++) {
				otherName = allTierNames.get(i);
				if (otherName.equals(refTier)) {
					continue;
				}
				int index = otherName.lastIndexOf(delimiter);
				if (index == -1 | index == otherName.length() - 1) {
					continue;
				}
				firstPart2 = otherName.substring(0, index);
				lastPart2 = otherName.substring(index + 1);
				if (firstPart2.equals(firstPart1) && !lastPart2.equals(lastPart1)) {
					return otherName;
				}
			}
		} else {
			// to be implemented
		}
		return null;
	}
	
	private String findFirstOrderSuffix(String tierName1, String tierName2) {
		if (affixMode == TierNameCompare.SUFFIX_MODE) {
			int i1 = tierName1.lastIndexOf(delimiter);
			int i2 = tierName2.lastIndexOf(delimiter);
			if (i1 > -1 && i2 > -1 && i1 < tierName1.length() - 1 && i2 < tierName2.length() - 1) {
				String suf1 = tierName1.substring(i1 + 1);
				String suf2 = tierName2.substring(i2 + 1);
				if (suf1.compareTo(suf2) > 0) {
					return suf2;
				} else {
					return suf1;
				}
			}
		} else {
			// to be implemented
		}
		
		return null;
	}
}
