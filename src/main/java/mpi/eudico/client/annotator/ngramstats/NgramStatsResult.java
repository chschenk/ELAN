package mpi.eudico.client.annotator.ngramstats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Stores the result of the N-gram analysis
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramStatsResult {
	// which domain/tier are we parsing?
	private final String domain;
	private final String tier;
	
	// determines the size of the ngram (bigram, trigram, etc)
	private final int ngramSize;
	
	// how many files did we parse?
	private int numFiles = 0;
	
	// how many annotations did we collect?
	private int numAnnotations = 0;
	
	// holds temporary data while parsing
    private List<NgramAnnotation> currentAnnotations = new ArrayList<NgramAnnotation>();	// the annotations in the file
    private String curFile;	// the file being parsed
    private int ngramPos;	// the position of the ngram in the file
    
    // stores the timestamp when we started/finished the search
    private long startTime = System.currentTimeMillis();
    private long endTime;
    
    // stores all the ngrams seen in the search so we can search through them
    private final List<Ngram> seen_ngrams = new ArrayList<Ngram>();
    
    // stores the collected ngrams ( identical Ngrams shoved into NgramCollection objects )
    private final List<NgramCollection> ngrams = new ArrayList<NgramCollection>();
    
    // formatter for ss.ms values
 	private final DecimalFormat timeFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.US));
	
	public NgramStatsResult(String dom, String selected_tier, int size) {
		domain = dom;
		tier = selected_tier;
		ngramSize = size;
	}

	/**
	 * The N-gram size this search used
	 * @return int the size
	 */
	public int getNgramSize() {
		return ngramSize;
	}

	/**
	 * The domain this search was performed on
	 * @return String the domain
	 */
	public String getDomain() {
		return domain;
	}
	
	/**
	 * The tier this search was performed on
	 * @return String the tier
	 */
	public String getTier() {
		return tier;
	}
	
	/**
	 * The total time this search took
	 * @return String the search time in s.ms format e.x. "2.469"
	 */
	public String getSearchTime() {
		return timeFormat.format( (endTime - startTime) / 1000f);
	}
	
	/**
	 * Retrieves the collected N-gram from a specific row in the result
	 * @param row The row index (0-based!)
	 * @return NgramCollection the N-gram
	 */
	public NgramCollection getCollectedNgramAt(int row) {
		return ngrams.get(row);
	}
	
	/**
	 * Retrieves the N-gram from a specific row in the result
	 * @param row The row index (0-based!)
	 * @return Ngram the N-gram
	 */
	public Ngram getNgramAt(int row) {
		return seen_ngrams.get(row);
	}
	
	/**
	 * The number of files parsed during this search
	 * @return int file count
	 */
	public int getNumFiles() {
		return numFiles;
	}
	
	/**
	 * The count of total annotations in this search
	 * @return int annotations
	 */
	public int getNumAnnotations() {
		return numAnnotations;
	}
	
	/**
	 * The count of collected N-grams in this search
	 * <p>Collected N-grams is a grouping of N-grams with the same annotations
	 * <p>This differs from {@link #getNumNgrams()} in that this is the list of collected N-grams
	 * @return int collected N-grams
	 */
	public int getNumCollectedNgrams() {
		return ngrams.size();
	}
	
	/**
	 * The count of individual N-grams in this search
	 * <p>This differs from {@link #getNumCollectedNgrams()} in that this is the entire list
	 * @return int seen N-grams
	 */
	public int getNumNgrams() {
		return seen_ngrams.size();
	}

	/**
	 * Adds an annotation to our collection while a file is being parsed
	 * @param aa The NgramAnnotation object
	 * @throws Exception If startFile(path) is not called before
	 */
	public void addAnnotation(AbstractAnnotation aa) throws Exception {
		if (curFile == null) {
			throw new Exception("Call startFile(path) first");
		}
		// convert the AbstractAnnotation into our internal NgramAnnotation format
		// so we can fix overlapped annotations, argh!!!!
		currentAnnotations.add(new NgramAnnotation(aa));
			
		numAnnotations++;
	}

	/**
	 * Signals that a file is going to be parsed
	 * @param path The file to be parsed
	 * @throws Exception If a file is being processed
	 */
	public void startFile(String path) throws Exception {
		if (curFile != null) {
			throw new Exception("Call endFile() first");
		}

		curFile = path;
		numFiles++;	// TODO if parsing blows up, we should numFiles--?? :(
		
		// Our ngram position counter ( starts at 1 because linguists are not programmers :)
		ngramPos = 1;
	}

	/**
	 * Signals that a file has completed parsing
	 * <p>Builds the resulting N-gram + Annotation objects
	 * @throws Exception If errors happen during calculating statistics in the file
	 */
	public void endFile() throws Exception {
		if (curFile == null) {
			throw new Exception("Call startFile(path) first");
		}
		
		// short-circuit the search if necessary
		if (currentAnnotations.size() >= ngramSize) {
			// make sure we have no overlapped annotations
			long tempTimeStamp = 0;
			for (NgramAnnotation ann : currentAnnotations) {
				if ( ann.beginTime < tempTimeStamp ) {
					// argh, we have to "fix" the overlapped annotation by shifting the latter one to start at the end of the previous one
					// this works based on the assumption that the ELAN code sorts annotations via the earlier timestamp
					// i.e. BOOK and HOLD have same beginTime, but HOLD's duration is longer than BOOK
					// we want BOOK, then HOLD not HOLD, then BOOK!
					ann.beginTime = tempTimeStamp;
				}

				tempTimeStamp = ann.endTime;
			}

			// set the before/after Intervals for each annotation
			for (int i = 0; i < currentAnnotations.size(); i++) {
				NgramAnnotation ann = currentAnnotations.get(i);

				// first one?
				if (i != 0) {
					ann.hasBeforeInterval = true;
					ann.beforeInterval = ann.beginTime - currentAnnotations.get(i - 1).endTime;
				}

				// last one?
				if (i != currentAnnotations.size() - 1) {
					ann.hasAfterInterval = true;
					ann.afterInterval = currentAnnotations.get(i + 1).beginTime - ann.endTime;
				}
			}

			// compile the ngrams
			Ngram newNgram;
			for (int i = 0; i < currentAnnotations.size(); i++) {
				// short-circuit if not enough annotations left to create a full ngram
				if (currentAnnotations.size() - i < ngramSize) {
					break;
				}

				// Grab the annotations to create a ngram
				newNgram = new Ngram(curFile, ngramSize);
				for (int j = i; j < currentAnnotations.size(); j++) {
					if ( j - i < ngramSize ) {
						newNgram.annotations.add(currentAnnotations.get(j));
					} else {
						// we've made the right-sized ngram!
						break;
					}
				}

				newNgram.position = ngramPos++;
				newNgram.calculateStatistics();
				seen_ngrams.add(newNgram);
			}
		}
		
		// all done with this file!
		currentAnnotations.clear();
		curFile = null;
	}

	/**
	 * Calculates various statistics on the result after parsing is done
	 * @throws Exception If some statistics are malformed
	 */
	public void calculateStatistics() throws Exception {
		// put same ngrams into a collection
		for (Ngram n : seen_ngrams) {
			String searchName = n.annotationsAsString();
			
			// is the ngram already in the collection list?
			boolean found = false;
			for (NgramCollection nc : ngrams) {
				if (nc.getName().equals(searchName)) {
					nc.ngrams.add(n);
					found = true;
					break;
				}
			}
			if (!found) {
				NgramCollection nc = new NgramCollection(ngramSize);
				nc.ngrams.add(n);
				ngrams.add(nc);
			}
		}
		
		// calculate stats on ngrams
		for (NgramCollection nc : ngrams) {
			nc.calculateStatistics(this);
		}
		
		// finally, we sort the ngram list by occurrences
		Collections.sort(ngrams, new Comparator<NgramCollection>() {
			@Override
			public int compare(NgramCollection o1, NgramCollection o2) {
				// sort ascending based on occurrences

				if (o1.getOccurrences() > o2.getOccurrences()) {
					return -1;
				} else if (o1.getOccurrences() < o2.getOccurrences()) {
					return 1;
				} else {
					// same number of occurrences, we now weigh by the name :)
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		});
		
		// we're finally done parsing+calculating!
		endTime = System.currentTimeMillis();
	}

	/**
	 * Finds N-grams containing an annotation
	 * @param search The N-gram name to search for e.x. "BOOK"
	 * @return List of matching N-grams
	 */
	protected List<Ngram> findNgramsWithAnnotation(String search) {
		List<Ngram> rv = new ArrayList<Ngram>();
		
		// optimize the search by only looking through collections instead of entire seen list
		// not a big speed-up due to Zipf's law, argh!
		for (NgramCollection nc : ngrams) {
			for (NgramAnnotation ann : nc.ngrams.get(0).annotations) {
				if (ann.value.equals(search)) {
					for (Ngram n : nc.ngrams){
						rv.add(n);
					}
					break;
				}
			}
		}
		
		return rv;
	}
}
