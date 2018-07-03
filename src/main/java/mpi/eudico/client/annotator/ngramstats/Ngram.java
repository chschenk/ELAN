package mpi.eudico.client.annotator.ngramstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the data for a N-gram in the search
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class Ngram {
	private final int ngramSize;
	protected final String filePath;
	protected final List<NgramAnnotation> annotations = new ArrayList<NgramAnnotation>();
	protected Long duration = 0l, totalAnnotationTime = 0l, totalIntervalTime = 0l;
	
	// the position of the ngram in the file ( 1-based )
	protected int position;

	public Ngram(String path, int size) {
		filePath = path;
		ngramSize = size;
	}

	/**
	 * Utility method to generate a representation of the annotations in this ngram
	 * @return String e.x. "BOOK|BUY"
	 */
	protected String annotationsAsString() {
		StringBuilder rv = new StringBuilder();
		for (NgramAnnotation ann : annotations) {
			rv.append(ann.value + "|");
		}
		rv.deleteCharAt(rv.length() - 1);

		return rv.toString();
	}

	/**
	 * Double-checks the data in this N-gram for correctness
	 * @throws Exception
	 */
	private void sanityCheck() throws Exception {
		// simple ngram size check
		if (annotations.size() != ngramSize) {
			throw new Exception("Invalid ngram size" + toString());
		}

		// our annotations should be properly ordered with respect to time...
		long ts = 0;
		for (NgramAnnotation ann : annotations) {
			if (ann.beginTime < ts) {
				throw new Exception("Annotation time snafu" + toString());
			} else {
				ts = ann.endTime;
			}
		}
		
		// passed sanity checks
	}

	/**
	 * Calculates various statistics on this N-gram after parsing is done
	 * @throws Exception
	 */
	protected void calculateStatistics() throws Exception {
		// make sure nothing is funky about this ngram!
		sanityCheck();

		// calculate some basic stats
		duration = annotations.get(ngramSize - 1).endTime - annotations.get(0).beginTime;
		long tempTimeStamp = 0;
		for (NgramAnnotation ann : annotations) {
			totalAnnotationTime += (ann.endTime - ann.beginTime);
			if (tempTimeStamp != 0) {
				totalIntervalTime += ( ann.beginTime - tempTimeStamp );
			}
			tempTimeStamp = ann.endTime;
		}

		// The annotation time is basically duration - interval
		if ( (duration - totalIntervalTime) != totalAnnotationTime ) {
			throw new Exception("Invalid annotationTime" + toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		rv.append("Ngram(" + annotationsAsString() + ")[" + this.hashCode() + "] in " + filePath);
		rv.append("\n\t beginTime=" + annotations.get(0).beginTime );
		rv.append("\n\t endTime=" + annotations.get(ngramSize - 1).endTime );
		rv.append("\n\t duration=" + duration );
		rv.append("\n\t annotationTime=" + totalAnnotationTime );
		rv.append("\n\t intervalTime=" + totalIntervalTime );
		rv.append("\n\t Annotations:");
		for (NgramAnnotation ann : annotations) {
			rv.append("\n\t\t " + ann.toString());
		}

		return rv.toString();
	}

	/**
	 * Lists the columns that we will export, used in conjunction with {@link #toCSV(d)}
	 * @return List of column Strings
	 */
	public List<String> toCSVColumns() {
		// build the return object
		List<String> rv = new ArrayList<String>();
				
		// start with the ngram name :)
		rv.add("N-gram");
		
		// The basic stats we will export
		// TODO argh keep this in sync with toCSV() :(
		Collections.addAll(rv, "After Interval", "Before Interval", "Duration", "File", "First N-gram", "Last N-gram", "Latency", "N-gram Position");
		
		// Only applicable for bigram and bigger N-grams
		if (ngramSize > 1) {
			Collections.addAll(rv, "Total Annotation Time", "Total Interval Time");
		}
		
		return rv;
	}

	/**
	 * Composes a string suitable for writing into an exported file ala "CSV" format
	 * @param d the delimiter
	 * @return String the N-gram as a string (no trailing newline)
	 */
	public String toCSV(String delim) {
		StringBuilder rv = new StringBuilder();

		// if the values collide with the delimiter just kill the programmer! :)
		// TODO argh keep this in sync with toCSVColumns() :(

		// firstly, the name...
		rv.append(annotationsAsString() + delim );

		// after/before interval ( convert to seconds from ms )
		if ( annotations.get( annotations.size() - 1 ).hasAfterInterval ) {
			rv.append( annotations.get( annotations.size() - 1 ).afterInterval / 1000d );
		} else {
			rv.append( "NaN" );
		}
		rv.append(delim);
		if ( annotations.get( 0 ).hasBeforeInterval ) {
			rv.append( annotations.get( 0 ).beforeInterval / 1000d );
		} else {
			rv.append( "NaN" );
		}
		rv.append(delim);

		// duration ( convert to seconds from ms )
		rv.append( (duration / 1000d) + delim);

		rv.append(filePath + delim);

		// First/Last Ngram
		if ( annotations.get( 0 ).hasBeforeInterval ) {
			rv.append( "0" );
		} else {
			rv.append( "1" );
		}
		rv.append(delim);
		if ( annotations.get( annotations.size() - 1 ).hasAfterInterval ) {
			rv.append( "0" );
		} else {
			rv.append( "1" );
		}
		rv.append(delim);

		// latency ( convert to seconds from ms )
		rv.append( (annotations.get(0).beginTime / 1000d) + delim );

		rv.append(position + delim);

		// bigram or greater stats
		if (ngramSize > 1) {
			// annotation/interval time ( convert to seconds from ms )
			rv.append( (totalAnnotationTime / 1000d) + delim);
			rv.append( (totalIntervalTime / 1000d) + delim);
		}

		return rv.toString();
	}
}