package mpi.eudico.client.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.MutableInt;


/**
 * Extracts unique words from a selection of tiers and writes the results to a
 * text file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class Transcription2WordList implements ClientLogger {
    final private String NEWLINE = "\n";
    private String delimiters = " \t\n\r\f.,!?\"\'";

    /**
     * Creates a new Transcription2WordList instance
     */
    public Transcription2WordList() {
        super();
    }

    /**
     * Exports the unique words from a selection of tiers.<br>
     *
     * @param transcription the transcription containing the tiers
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     * @param countOccurrences if true the frequency of the words/annotations is also exported
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWords(TranscriptionImpl transcription, List<String> tierNames,
        File exportFile, String charEncoding, String delimiters, boolean countOccurrences)
        throws IOException {
    	if (countOccurrences) {
    		exportWordsAndCount(transcription, tierNames, exportFile, charEncoding, delimiters);
    		return;
    	}
    	
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if (transcription == null) {
            LOG.severe("No transcription specified for wordlist");

            return;
        }

        if (tierNames == null) {
            LOG.warning("No tiers specified for the wordlist: using all tiers");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        TreeSet<String> uniqueWords = new TreeSet<String>();
        addUniqueWords(uniqueWords, transcription, tierNames);

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        // Use the fact that a TreeSet is sorted.
        for (String word : uniqueWords) {
            writer.write(word);
            writer.write(NEWLINE);
        }

        writer.close();
    }

    /**
     * Exports the unique words from a selection of tiers from a number of files.<br>
     * Note: tests should be done with respect to performance (use of a
     * TreeSet instead of an List followed by a sort.
     *
     * @param files a list of eaf files
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     * @param countOccurrences if true the frequency of the words/annotations is also exported
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWords(List<File> files, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters, boolean countOccurrences) throws IOException {
    	if (countOccurrences) {
    		exportWordsAndCount(files, tierNames, exportFile, charEncoding, delimiters);
    		return;
    	}
    	
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            LOG.warning("No files specified for export");
            throw new IOException("No files specified for export");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        TreeSet<String> uniqueWords = new TreeSet<String>();

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);

            if (file == null) {
                continue;
            }

            try {
            	TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());
                addUniqueWords(uniqueWords, trans, tierNames);

            } catch (Exception ex) {
                // catch any exception that could occur and continue
                LOG.severe("Could not handle file: " + file.getAbsolutePath());
                LOG.severe(ex.getMessage());
            }
        }

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        // Use the fact that a TreeSet is sorted.
        for (String word : uniqueWords) {
            writer.write(word);
            writer.write(NEWLINE);
        }

        writer.close();
    }

    /**
     * Creates a list of unique words in the specified tiers from the specified
     * transcription.
     * @param uniqueWords the TreeSet to add the words to
     * @param transcription the transcription
     * @param tierNames the tiers
     *
     * @return the words
     */
    private void addUniqueWords(Set<String> uniqueWords, TranscriptionImpl transcription,
    		List<String> tierNames) {
        if (transcription == null) {
            LOG.severe("No transcription specified to extract words from");

            return;
        }

        List<? extends Tier> tierList;
        
        if (tierNames != null) {
        	tierList = transcription.getTiersWithIds(tierNames);
        } else {
        	tierList = transcription.getTiers();
        }
        
        for (Tier t : tierList) {
        	for (Annotation ann : t.getAnnotations()) {
                if (ann != null) {
                    final String value = ann.getValue();
                    
					if (value.length() > 0) {
                        if (delimiters.length() > 0) {
                        	StringTokenizer tokenizer = new StringTokenizer(value,
                                    delimiters);

                            while (tokenizer.hasMoreTokens()) {
                                String token = tokenizer.nextToken();

                                uniqueWords.add(token);
                            }
                        } else {
                            uniqueWords.add(value);
                        }
                    }
                } else {
                    LOG.warning("Annotation is null");
                }

        	}
        }
    }

    /**
     * Exports the unique words and their frequencies from a selection of tiers from a number of files.
     *
     * @param transcription a transcription
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWordsAndCount(TranscriptionImpl transcription, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters) throws IOException {
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }
        
        if (transcription == null) {
            LOG.severe("No transcription specified for wordlist");

            return;
        }      

        if (tierNames == null) {
            LOG.warning("No tiers specified for the wordlist: using all tiers");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }
        
        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();
        addUniqueWordsAndCount(uniqueWords, transcription, tierNames);
        
        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        // Use the fact that a TreeMap is ordered
        for (Map.Entry<String, MutableInt> e : uniqueWords.entrySet()) {
        	String key = e.getKey();
        	MutableInt val = e.getValue();

        	writer.write(key);
        	writer.write("\t" + val.intValue);
            writer.write(NEWLINE);
        }

        writer.close();
    }
        
    /**
     * Exports the unique words and their frequencies from a selection of tiers from a number of files.
     *
     * @param files a list of eaf files
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWordsAndCount(List<File> files, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters) throws IOException {
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            LOG.warning("No files specified for export");
            throw new IOException("No files specified for export");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();

        File file;
        TranscriptionImpl trans;

        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());
                addUniqueWordsAndCount(uniqueWords, trans, tierNames);

            } catch (Exception ex) {
                // catch any exception that could occur and continue
                LOG.severe("Could not handle file: " + file.getAbsolutePath());
                LOG.severe(ex.getMessage());
            }
        }

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        // Use the fact that a TreeMap is ordered
        for (Map.Entry<String, MutableInt> e : uniqueWords.entrySet()) {
        	String key = e.getKey();
        	MutableInt val = e.getValue();

        	writer.write(key);
        	writer.write("\t" + val.intValue);
            writer.write(NEWLINE);
        }

        writer.close();
    }

    
    /**
     * Adds the (unique) words in the specified tiers from the specified
     * transcription to the specified map and updates their frequency.
     *
     * @param uniqueWords a map containing word - frequency pairs
     * @param transcription the transcription
     * @param tierNames the tiers
     */
    private void addUniqueWordsAndCount(Map<String, MutableInt> uniqueWords,
    		TranscriptionImpl transcription, List<String> tierNames) {
    	
        if (transcription == null) {
            LOG.severe("No transcription specified to extract words from");

            return;
        }

        List<? extends Tier> tierList;
        
        if (tierNames != null) {
        	tierList = transcription.getTiersWithIds(tierNames);
        } else {
        	tierList = transcription.getTiers();
        }
        
        for (Tier t : tierList) {
                if (t != null) {
                	for (Annotation ann : t.getAnnotations()) {
                        if (ann != null) {
                            final String value = ann.getValue();
							if (value.length() > 0) {
                                if (delimiters.length() > 0) {
                                	StringTokenizer tokenizer = new StringTokenizer(value,
                                            delimiters);

                                    while (tokenizer.hasMoreTokens()) {
                                        String token = tokenizer.nextToken();

                                        if (!uniqueWords.containsKey(token)) {
                                        	uniqueWords.put(token, new MutableInt(1));
                                        } else {
                                        	uniqueWords.get(token).intValue++;
                                        }
                                    }
                                } else {
                                    if (!uniqueWords.containsKey(value)) {
                                        uniqueWords.put(value, new MutableInt(1));
                                    } else {
                                    	uniqueWords.get(value).intValue++;
                                    }
                                }
                            }
                        } else {
                            LOG.warning("Annotation is null");
                        }
                    }
                }
        }
    }
}
