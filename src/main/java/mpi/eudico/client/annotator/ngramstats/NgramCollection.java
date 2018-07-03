package mpi.eudico.client.annotator.ngramstats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Holds the collection of identical N-grams in the result
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramCollection {
	protected final List<Ngram> ngrams = new ArrayList<Ngram>();
	private final int ngramSize;

	// formatter for ss.ms values
	private static final DecimalFormat timeFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.US));

	// holds all the statistics for this collation
	// have no fear of using strings here, Java has the string literal pool so we shouldn't consume gobs of RAM!
	private final HashMap<String, Double> stats = new HashMap<String, Double>();

	// for in-depth statistical analyses, we need to build a contingency table
	// the size is determined by the ngramSize, and thus calculated during runtime
	private int[][] cT; // the contingency table ( with marginal totals )
	private double[][] cT_exp; // the expected values for the cells
	
	public NgramCollection(int size) {
		ngramSize = size;
	}

	/**
	 * The name of this N-gram e.x. "BUY|BOOK"
	 * @return String the name
	 */
	public String getName() {
		return ngrams.get(0).annotationsAsString();
	}

	/**
	 * The count of occurrences of this N-gram in the search
	 * @return int the count
	 */
	public int getOccurrences() {
		// we cheat a bit here to reduce number of method calls as we "should" call stats.get(blabla)
		return ngrams.size();
	}

	/**
	 * The count of occurrences of this N-gram in specific files in this search
	 * @return int the count
	 */
	public int getFileOccurrences() {
		// TODO it's annoying that you can't convert a Double to an Integer directly...
		return (int)Math.round(stats.get("File Occurrences"));
	}

	// called from NgramStatsTableModel, so we don't want to violate encapsulation by exposing stats
	// does the formatting for us ( convert from milliseconds to human-readable )
	public String getAvgDuration() {
		return timeFormat.format(stats.get("Duration|Mean"));
	}
	public String getMinDuration() {
		return timeFormat.format(stats.get("Duration|Min"));
	}
	public String getMaxDuration() {
		return timeFormat.format(stats.get("Duration|Max"));
	}
	public String getAvgAnnotationTime() {
		// we don't calculate this if it's a unigram
		if (ngramSize > 1) {
			return timeFormat.format(stats.get("Total Annotation Time|Mean"));
		} else {
			return null;
		}
	}
	public String getAvgIntervalTime() {
		// we don't calculate this if it's a unigram
		if (ngramSize > 1) {
			return timeFormat.format(stats.get("Total Interval Time|Mean"));
		} else {
			return null;
		}
	}

	/**
	 * Double-checks the data in this N-gram collection for correctness
	 * @throws Exception If some statistics are malformed
	 */
	private void sanityCheck() throws Exception {
		// this Ngram collection needs at least 1 Ngram!
		if (ngrams.size() == 0) {
			throw new Exception("No ngram exists in this collation" + toString());
		}
			
		// make sure all ngrams have identical annotations!
		String masterNgram = ngrams.get(0).annotationsAsString();
		for (int i = 1; i < ngrams.size(); i++) {
			if ( ! masterNgram.equals( ngrams.get(i).annotationsAsString() ) ) {
				throw new Exception("Not all ngrams have identical annotations" + toString());
			}
		}
	}
	
	/**
	 * Calculates a whole host of descriptive statistics (mean, median, std.dev, etc) on the data
	 * @param name The name of the data so we shove it into the stats hashmap
	 * @param data The list of numbers we will process to create the stats
	 */
	private void calcDescriptiveStats(String name, List<Double> data) {
		/*
		 * TODO more tests we could implement?
		 * Mean Square Displacement
		 * Root Mean Square Deviation
		 * Coefficient of Variation - calculate: StdDev / Mean
		 * Range - calculate: Max-Min
		 * InterQuartile Range - calculate: IQ3 - IQ1
		 * Quartile Deviation - calculate: 0.5 * (IQ3 - IQ1)
		 * Quartile Coefficient of Dispersion - calculate: (IQ3 - IQ1) / (IQ1 + IQ3)
		 * InterQuartile Mean
		 */
		
		// REMEMBER, this function might be handed an empty list! We still have to insert entries in stats otherwise the output will be screwed up!!!
		// The solution is to insert NaN if there is no applicable data
		
		{
			// we have to watch out for divide by 0, it happens here a lot :(
			double mean = 0, variance, skewness, kurtosis, stddev, moment2 = 0, moment3 = 0, moment4 = 0;
			if (data.size() > 0) {
				// calculate the mean
				for (Double x : data) {
					mean += x;
				}
				mean /= (double)data.size();

				if (data.size() > 1) {
					// calculate the variance, skewness, and kurtosis at the same time
					for (Double x : data) {
						moment2 += Math.pow( x - mean, 2 );
						moment3 += Math.pow( x - mean, 3 );
						moment4 += Math.pow( x - mean, 4 );
					}
					
					// calculate the unbiased variance where we divide by N-1
					variance = moment2 / (double)(data.size() - 1);
				
					// calculate the stddev from the variance
					stddev = Math.sqrt(variance);
					
					// skewness is defined as moment3 / (moment2)^(3/2)
					skewness = moment3 / Math.pow( Math.sqrt( moment2 ), 3 );

					// kurtosis is defined as moment4 / (moment2)^2
					kurtosis = moment4 / Math.pow( moment2, 2 );
				} else {
					variance = skewness = kurtosis = stddev = Double.NaN;
				}
			} else {
				mean = variance = skewness = kurtosis = stddev = Double.NaN;
			}

			// store the values
			stats.put(name + "|Mean", mean );
			stats.put(name + "|Variance", variance);
			stats.put(name + "|StdDev", stddev);
			stats.put(name + "|Skewness", skewness);
			stats.put(name + "|Kurtosis", kurtosis);
		}
		
		{
			// calculate the minimum value
			double min = data.size() > 0 ? Double.MAX_VALUE : Double.NaN;
			for (Double x : data) {
				if ( x < min ) {
					min = x;
				}
			}
			stats.put(name + "|Min", min);
		}
		
		{
			// calculate the maximum value
			double max = data.size() > 0 ? -Double.MAX_VALUE : Double.NaN;
			for (Double x : data) {
				if ( x > max ) {
					max = x;
				}
			}
			stats.put(name + "|Max", max);
		}
		
		{
			// calculate the median value
			// we have to sort in order to find the "center"
			Collections.sort(data);
			stats.put( name + "|Median", calcMedian(data));
		}

		{
			// calculate the quartile stuff
			double iq1, iq3;
			if (data.size() > 2) {
				if (data.size() % 2 == 0) { // even-sized array
					// this means we don't have to play around with the center of the data
					iq1 = calcMedian( data.subList( 0, data.size() / 2 ) );
					iq3 = calcMedian( data.subList( data.size() / 2, data.size() ) );
				} else {
					// we have to include the center in the quartile to make it unbiased
					iq1 = calcMedian( data.subList( 0, (int)Math.ceil(data.size() / 2) ) );
					iq3 = calcMedian( data.subList( (int)Math.floor(data.size() / 2), data.size() ) );
				}
			} else {
				// TODO is there a published algorithm where we deal with only 2 elements???
				iq1 = iq3 = Double.NaN;
			}
			
			// store the quartiles
			stats.put( name + "|Quartile1", iq1 );
			stats.put( name + "|Quartile3", iq3 );
		}
		
		{
			// calculate the mode
			// get the counts
			HashMap<Double, Integer> modeMap = new HashMap<Double, Integer>();
			for (Double x : data) {
				if (modeMap.get(x) != null) {
					modeMap.put(x, modeMap.get(x) + 1);
				} else {
					modeMap.put(x, 1);
				}
			}
			
			// find the mode!
			double mode = modeMap.size() > 0 ? Double.MIN_VALUE : Double.NaN;
			int count = 0;
			for (Double x : modeMap.keySet()) {
				// in order to avoid multimodal issues, we pick the highest value in the array to represent the mode
				// TODO is there a better way to display this info?
				if (modeMap.get(x) == count && x > mode) {
					mode = x;
				} else 	if (modeMap.get(x) > count) {
					mode = x;
					count = modeMap.get(x);
				}
			}
			stats.put( name + "|Mode", mode);
		}
	}
	
	/**
	 * Calculates the median value of a list
	 * @param data
	 * @return NaN if empty list, Double otherwise
	 */
	private double calcMedian(List<Double> data) {
		// we assume the data is already sorted...
		
		// no use in analyzing an empty list!
		if (data.size() == 0) {
			return Double.NaN;
		}
		
		if (data.size() % 2 == 0) { // even-sized array
			// average the 2 center numbers...
			// convert from size to 0-based indices!$#@#$
			double median = data.get( (data.size() / 2) - 1 );
			median += data.get( data.size() / 2 );
			return median / 2d;
		} else { // odd-sized array
			// just pick the center number
			// use floor to get the 0-based indices
			return data.get( (int)Math.floor( data.size() / 2 ) );
		}		
	}

	/**
	 * Calculates various statistics on this N-gram after parsing is done
	 * @param result The NgramStatsResult object we use to calculate stats on
	 * @throws Exception If some statistics are malformed
	 */
	protected void calculateStatistics(NgramStatsResult result) throws Exception {
		// make sure nothing is funky about this ngram collation!
		sanityCheck();
		
		{
			// some basic stats that we don't need to compute much on :)
			stats.put( "Occurrences", (double)ngrams.size() );
		}
		
		{
			// calculate the number of occurrences in files
			HashMap<String, Integer> files = new HashMap<String, Integer>();
			for (Ngram n : ngrams) {
				files.put(n.filePath, 1);
			}
			stats.put( "File Occurrences", (double)files.size() );
		}
		
		{
			// calculate the stats on the duration
			List<Double> duration = new ArrayList<Double>();
			for (Ngram n : ngrams) {
				// convert from ms to seconds
				duration.add(n.duration / 1000d);
			}
			calcDescriptiveStats("Duration", duration);
		}
		
		{
			// calculate the stats on the annotation time
			// we skip this in the case of unigrams (obvious, eh? :)
			if (ngramSize > 1) {
				List<Double> annTime = new ArrayList<Double>();
				for (Ngram n : ngrams) {
					// convert from ms to seconds
					annTime.add(n.totalAnnotationTime / 1000d);
				}
				calcDescriptiveStats("Total Annotation Time", annTime);
			}
		}
		
		{
			// calculate the stats on the interval time
			// we skip this in the case of unigrams (obvious, eh? :)
			if (ngramSize > 1) {
				List<Double> intTime = new ArrayList<Double>();
				for (Ngram n : ngrams) {
					// convert from ms to seconds
					intTime.add(n.totalIntervalTime / 1000d);
				}
				calcDescriptiveStats("Total Interval Time", intTime);
			}
		}
		
		{
			// calculate the stats on the latency (time until the ngram is annotated)
			List<Double> latency = new ArrayList<Double>();
			for (Ngram n : ngrams) {
				// convert from ms to seconds
				latency.add( n.annotations.get(0).beginTime / 1000d );
			}
			calcDescriptiveStats("Latency", latency);
		}
		
		{
			// calculate the stats on the before/after Intervals
			List<Double> interval = new ArrayList<Double>();
			int first = 0, last = 0;
			for (Ngram n : ngrams) {
				if (n.annotations.get(0).hasBeforeInterval) {
					// convert from ms to seconds
					interval.add( n.annotations.get(0).beforeInterval / 1000d );
				} else {
					// count how many times this ngram was the first annotation in the tier
					first++;
				}
			}
			calcDescriptiveStats("Before Interval", interval);
			stats.put("First N-gram", (double)first);
			
			interval.clear();
			for (Ngram n : ngrams) {
				if (n.annotations.get( n.annotations.size() - 1 ).hasAfterInterval) {
					// convert from ms to seconds
					interval.add( n.annotations.get( n.annotations.size() - 1 ).afterInterval / 1000d );
				} else {
					// count how many times this ngram was the last annotation in the tier
					last++;
				}
			}
			calcDescriptiveStats("After Interval", interval);
			stats.put("Last N-gram", (double)last);
		}
		
		{
			// calculate the stats on the ngram position
			List<Double> pos = new ArrayList<Double>();
			for (Ngram n : ngrams) {
				pos.add( (double)n.position );
			}
			calcDescriptiveStats("N-gram Position", pos);
		}

		// create the contingency table for this ngram
		if (ngramSize == 2) {
			createContingencyTable(result);
		} else {
			// not implemented yet :)
		}
	}

	/**
	 * Uses the data calculated in the contingency table to come up with various stats for bigrams
	 */
	private void analyzeContingencyTable() {
		/*
		 * TODO more tests we could implement?
		 * They were taken from my stats books + wikipedia browsing :)
		 * However, I'm unsure as to whether they can be useful to ngram analysis...
		 * 
		 * Pearson product-moment correlation coefficient
		 * Polychoric correlation
		 * Cramer's V ( Cramer's phi )
		 * Spearman's rank correlation coefficient
		 * Goodman and Kruskal's lambda
		 * Uncertainty coefficient ( Thell's U )
		 * Goodman and Kruskal's gamma
		 * Kendall tau rank correlation coefficient
		 * Tschuprow's T
		 */
			
		{
			// find the Chi-squared score
			double chi = calcDeviation(cT[0][0], cT_exp[0][0]);
			chi += calcDeviation(cT[0][1], cT_exp[0][1]);
			chi += calcDeviation(cT[1][0], cT_exp[1][0]);
			chi += calcDeviation(cT[1][1], cT_exp[1][1]);
			stats.put("cT|Chi-squared", chi);
				
			// find the Phi coefficient
			// NOTE: this is the Phi^2, not the Phi value!
			stats.put("cT|Phi Coefficient", chi / (double)cT[2][2] );
		}
			
		{
			// find the t-score
			stats.put("cT|T-score", (cT[0][0] - cT_exp[0][0]) / Math.sqrt(cT[0][0]) );
		}
			
		{
			// find the Dice coefficient
			double dice = (2 * cT[0][0]) / (double)(cT[0][2] + cT[2][0]);
			stats.put("cT|Dice Coefficient", dice);
		
			// find the Jaccard coefficient
			stats.put("cT|Jaccard Coefficient", dice / (double)(2 - dice));
		}
			
		{
			// find the loglikelihood
			double ll = cT[0][0] * calcPMI( cT[0][0], cT_exp[0][0] );
			ll += cT[0][1] * calcPMI( cT[0][1], cT_exp[0][1] );
			ll += cT[1][0] * calcPMI( cT[1][0], cT_exp[1][0] );
			ll += cT[1][1] * calcPMI( cT[1][1], cT_exp[1][1] );
			stats.put("cT|Log-likelihood", 2 * ll);
		}
			
		{
			// find the pmi
			// TODO: it is possible to modify this via cT**exponent where exponent is user-supplied...
			stats.put("cT|Pointwise Mutual Information", calcPMI( cT[0][0], cT_exp[0][0] ) / Math.log(2) );
		}
			
		{
			// find the poissonstirling
			stats.put("cT|Poisson-Stirling Measure", cT[0][0] * ( calcPMI( cT[0][0], cT_exp[0][0] ) - 1 ));
		}
			
		{
			// find the tmi
			double tmi = ((cT[0][0] / (double)cT[2][2]) * calcPMI( cT[0][0], cT_exp[0][0] )) / Math.log(2);
			tmi += ((cT[0][1] / (double)cT[2][2]) * calcPMI( cT[0][1], cT_exp[0][1] )) / Math.log(2);
			tmi += ((cT[1][0] / (double)cT[2][2]) * calcPMI( cT[1][0], cT_exp[1][0] )) / Math.log(2);
			tmi += ((cT[1][1] / (double)cT[2][2]) * calcPMI( cT[1][1], cT_exp[1][1] )) / Math.log(2);
			stats.put("cT|True Mutual Information", tmi);
		}
			
		{
			// find the odds ratio
			// we need to avoid zero denominators so we change them to 1
			int n21 = ( cT[1][0] == 0 ? 1 : cT[1][0] );
			int n12 = ( cT[0][1] == 0 ? 1 : cT[0][1] );
			stats.put("cT|Odds Ratio", ((cT[0][0] * cT[1][1]) / (double)(n12 * n21)));
		}
			
		{
			// find the fisher left sided
			int start_range = cT[0][2] + cT[2][0] - cT[2][2];
			if (start_range < 0) {
				start_range = 0;
			}
			HashMap<Integer, Double> probabilities = calcDistribution( start_range, cT[0][0] );
				
			// calculate the leftfisher by summing the probabilites via ascending order from the map
			List<Integer> keys = new ArrayList<Integer>(probabilities.keySet());
			Collections.sort(keys);			
			double leftfisher = 0;
			for (Integer k : keys) {
				if (k > cT[0][0]) {
					break;
				} else {
					leftfisher += probabilities.get(k);
				}
			}
			stats.put("cT|Fisher Exact Left Sided", leftfisher);
		}
		
		{			
			// find the fisher right sided
			// same thing as from the fisher left sided, with some optimizations
			boolean left_flag = false;
			int final_limit = (cT[0][2] < cT[2][0]) ? cT[0][2] : cT[2][0];
			int n11_start = cT[0][2] + cT[2][0] - cT[2][2];
			if ( n11_start < cT[0][0] ) {
				n11_start = cT[0][0];
			}
				
			// find the faster way to calculate the probabilities
			int left_final_limit = cT[0][0] - 1;
			int left_n11 = cT[0][2] + cT[2][0] - cT[2][2];
			if (left_n11 < 0) {
				left_n11 = 0;
			}
				
			// actually calculate the probabilities!
			HashMap<Integer, Double> probabilities;
			if( (left_final_limit - left_n11) < (final_limit - n11_start) ) {
				left_flag = true;
				probabilities = calcDistribution(left_n11, left_final_limit);
			} else {
				// argh, no shortcuts here :(
				probabilities = calcDistribution(n11_start, final_limit);
			}
				
			// calculate the rightfisher by summing the probabilites via descending order from the map
			List<Integer> keys = new ArrayList<Integer>(probabilities.keySet());
			Collections.sort(keys, Collections.reverseOrder());
			double rightfisher = 0;
			for (Integer k : keys) {
				if (left_flag) {
					if (k >= cT[0][0]) {
						break;
					}
				} else {
					if (k < cT[0][0]) {
						break;
					}
				}
				rightfisher += probabilities.get(k);
			}
				
			// did we use the shortcut? if so, fix the result!
			if (left_flag) {
				rightfisher = 1 - rightfisher;
			}
			stats.put("cT|Fisher Exact Right Sided", rightfisher);
		}
			
		{
			// find the fisher two-tailed
			// same concept as left/right but we sum both
			int final_limit = (cT[0][2] < cT[2][0]) ? cT[0][2] : cT[2][0];
			int n11_start = cT[0][2] + cT[2][0] - cT[2][2];
			if ( n11_start < 0 ) {
				n11_start = 0;
			}
			HashMap<Integer, Double> probabilities = calcDistribution( n11_start, final_limit );
				
			// calculate the fisher by summing the probabilites via ascending order from the map
			List<Double> values = new ArrayList<Double>(probabilities.values());
			Collections.sort(values);
			double ttfisher = 0;
			for (Double v : values) {
				if (v <= probabilities.get(cT[0][0])) {
					ttfisher += v;
				}
			}
			stats.put("cT|Fisher Exact Two Tailed", ttfisher);
		}
	}

	/**
	 * Computes the probabilities for all possible contingency tables
	 * <p>Big thanks to the Perl package Text::NSP::Measures::2D::Fisher2 v0.97
	 * @param start The starting range to search on the table
	 * @param end The ending range to search on the table
	 * @return HashMap of Integer => Double mappings of each range in the table with their probability 
	 */
	private HashMap<Integer, Double> calcDistribution( final int start, final int end ) {
		final HashMap<Integer, Double> rv = new HashMap<Integer, Double>();
		
		// init the searching values
		// the names are taken from the cT definitions in createContingencyTable()
		int n11 = start;
		int n12 = cT[0][2] - n11;
		int n21 = cT[2][0] - n11;
		int n22 = cT[1][2] - n21;
			
		// shift the values so we have a non-negative n22
		while (n22 < 0) {
			n11++;
			n12 = cT[0][2] - n11;
			n21 = cT[2][0] - n11;
			n22 = cT[1][2] - n21;
		}
			
		// finally, calculate the hypergeometric probabilities
		for (int i = n11; i <= end; i++) {
			// alter the values for this run
			n12 = cT[0][2] - i;
			n21 = cT[2][0] - i;
			n22 = cT[1][2] - n21;

			rv.put(i, calcHypergeometric( i, n12, n21, n22 ));
		}
			
		return rv;
	}

	/**
	 * Computes the HyperGeometric distribution of a contingency table
	 * <p>Big thanks to the Perl package Text::NSP::Measures::2D::Fisher2 v0.97
	 * @param n11 The upper left cell in the table
	 * @param n12 The upper right cell in the table
	 * @param n21 The lower left cell in the table
	 * @param n22 The lower right cell in the table
	 * @return Double the hypergeometric distribution
	 */
	private double calcHypergeometric( final int n11, final int n12, final int n21, final int n22 ) {
		// TODO use the apache commons math library? not used here as I didn't want to add more libraries and bloat ELAN...
		// furthermore, it seems like the NSP authors did different calculations and I just wanted to copy them :)
		
		// create the numerators from the marginals, then sort in descending order
		final List<Integer> nums = new ArrayList<Integer>();
		Collections.addAll(nums, cT[0][2], cT[1][2], cT[2][0], cT[2][1]);
		Collections.sort(nums, Collections.reverseOrder());
			
		// create the denominators from the cells + total, then sort in descending order
		final List<Integer> dems = new ArrayList<Integer>();
		Collections.addAll(dems, cT[2][2], n11, n12, n21, n22);
		Collections.sort(dems, Collections.reverseOrder());
			
		// the return value (product) of the calculations
		double product = 1;
			
		// other variables used
		final List<Integer> dLimits = new ArrayList<Integer>();
		final List<Integer> nLimits = new ArrayList<Integer>();
		int dIndex = 0;
		int nIndex = 0;
			
		// set the dLimit/nLimit arrays
		for (int i = 0; i < 4; i++) {
			if (nums.get(i) > dems.get(i)) {
				nLimits.add(nIndex++, dems.get(i) + 1);
				nLimits.add(nIndex++, nums.get(i));
			} else if (dems.get(i) > nums.get(i)) {
				dLimits.add(dIndex++, nums.get(i) + 1);
				dLimits.add(dIndex++, dems.get(i));
			}
		}
			
		// add the remaining denominator ( we have 5 dems, only 4 nums )
		dLimits.add(dIndex++, 1);
		dLimits.add(dIndex, dems.get(4));
			
		// actually calculate the product now!
		while ( ! nLimits.isEmpty() ) {
			// the funky 10000000 number is used to prevent overflow
			// look at the Perl module's code to understand this better!
				
			// multiply the product by the numerators
			while ( (product < 10000000) && ! nLimits.isEmpty() ) {
				product = product * (double)nLimits.get(0);
				nLimits.set(0, nLimits.get(0) + 1);
				if (nLimits.get(0) > nLimits.get(1)) {
					// remove the pair
					nLimits.remove(0); nLimits.remove(0);
				}
			}
				
			// divide the product by the denominators
			while (product > 1) {
				product = product / (double)dLimits.get(0);
				dLimits.set(0, dLimits.get(0) + 1);
				if (dLimits.get(0) > dLimits.get(1)) {
					// remove the pair
					dLimits.remove(0); dLimits.remove(0);
				}
			}				
		}
			
		// calculate the remaining denominators...
		while ( ! dLimits.isEmpty() ) {
			product = product / (double)dLimits.get(0);
			dLimits.set(0, dLimits.get(0) + 1);
			if (dLimits.get(0) > dLimits.get(1)) {
				// remove the pair
				dLimits.remove(0); dLimits.remove(0);
			}
		}			
		
		return product;
	}

	/**
	 * Computes the Pointwise Mutual Information on a pair
	 * <p>Big thanks to the Perl package Text::NSP::Measures::2D::MI v1.03
	 * @param n The observed value
	 * @param m The expected value
	 * @return Double the pmi
	 */
	private double calcPMI( final double n, final double m ) {
		if (n > 0) {
			return Math.log( n / m );
		} else {
			return 0;
		}
	}

	/**
	 * Computes the deviation in observed value with respect to the expected value
	 * <p>Big thanks to the Perl package Text::NSP::Measures::2D::CHI v1.03
	 * @param n The observed value
	 * @param m The expected value
	 * @return Double the deviation
	 */
	private double calcDeviation( final double n, final double m ) {
		if (m > 0) {
			return Math.pow((n - m), 2) / (double)m;
		} else {
			return 0;
		}
	}

	/**
	 * Computes the contingency table for a bigram
	 * @param result The NgramStatsResult object we use to calculate stats on 
	 * @throws Exception
	 */
	private void createContingencyTable(NgramStatsResult result) throws Exception {
		// table looks like this: (lifted from the Perl Text-NSP package, THANKS!)
		// bigram is: ann1|ann2
		//				ann2		notann2		Total
		//			----------------------------------
		// ann1		|	n11		|	n12		|	n1p
		//			|---------------------------------
		// notann1	|	n21		|	n22		|	n2p
		//			|---------------------------------
		// Total	|	np1		|	np2		|	npp
		cT = new int[3][3];
			
		// fill in the data!
		cT[0][0] = ngrams.size();
		cT[2][2] = result.getNumNgrams();
		String a1 = ngrams.get(0).annotations.get(0).value; //ann1
		String a2 = ngrams.get(0).annotations.get(1).value; //ann2
			
		// get the ngrams containing ann1
		List<Ngram> a1List = result.findNgramsWithAnnotation( a1 );
			
		// filter for ngrams NOT containing ann2
		List<Ngram> a1NOa2 = new ArrayList<Ngram>();
		for (Ngram n : a1List) {
			if ( n.annotations.get(0).value.equals(a1) && ! n.annotations.get(1).value.equals(a2)) {
				a1NOa2.add(n);
			}
		}
			
		// fill in the data!
		cT[0][1] = a1NOa2.size();
			
		// get the ngrams containing ann2
		List<Ngram> a2List = result.findNgramsWithAnnotation( a2 );

		// filter for ngrams NOT containing ann1
		List<Ngram> a2NOa1 = new ArrayList<Ngram>();
		for (Ngram n : a2List) {
			if ( n.annotations.get(1).value.equals(a2) && ! n.annotations.get(0).value.equals(a1)) {
				a2NOa1.add(n);
			}
		}
						
		// fill in the data!
		cT[1][0] = a2NOa1.size();

		// lastly, calculate the final cell in the contingency table
		cT[1][1] = cT[2][2] - ( cT[0][0] + cT[0][1] + cT[1][0] );
		
		// now, we calculate the marginals(totals) from the table
		cT[0][2] = cT[0][0] + cT[0][1];
		cT[1][2] = cT[1][0] + cT[1][1];
		cT[2][0] = cT[0][0] + cT[1][0];
		cT[2][1] = cT[0][1] + cT[1][1];
			
		// calculate the expected values from the table
		// it is a direct overlay on the observed values (not the marginals)
		cT_exp = new double[2][2];
		cT_exp[0][0] = (cT[0][2] * cT[2][0]) / (double)cT[2][2];
		cT_exp[0][1] = (cT[0][2] * cT[2][1]) / (double)cT[2][2];
		cT_exp[1][0] = (cT[1][2] * cT[2][0]) / (double)cT[2][2];
		cT_exp[1][1] = (cT[1][2] * cT[2][1]) / (double)cT[2][2];
			
		// sanity check our table!
		sanityCheckContingencyTable();
		
		// finally, calculate the metrics!
		analyzeContingencyTable();
	}

	/**
	 * Double-checks the data in the contingency table for correctness
	 * @throws Exception If some statistics are malformed in the table
	 */
	private void sanityCheckContingencyTable() throws Exception {
		// obviously the cells must sum = total ngrams
		if ((cT[0][0] + cT[0][1] + cT[1][0] + cT[1][1]) != cT[2][2]) {
			throw new Exception("sum of cells != total" + toString());
		}
			
		// Obviously, the ngram itself must have a hit!
		if (cT[0][0] <= 0) {
			throw new Exception("n11 with no hits" + toString());
		}
		if (cT[0][0] > cT[2][2]) {
			throw new Exception("n11 > npp" + toString());
		}
		if (cT[0][0] > cT[2][0] || cT[0][0] > cT[0][2]) {
			throw new Exception("n11 > np1 || n11 > np1" + toString());
		}
			
		// double-check the rest of the cells
		if (cT[0][1] < 0) {
			throw new Exception("n12 < 0" + toString());
		}
		if (cT[1][0] < 0) {
			throw new Exception("n21 < 0" + toString());
		}
		if (cT[1][1] < 0) {
			throw new Exception("n22 < 0" + toString());
		}
			
		// double-check the marginal calculations
		if (cT[0][2] < 1 || cT[0][2] > cT[2][2] ) {
			throw new Exception("n1p < 1 || > npp" + toString());
		}
		if (cT[2][0] < 1 || cT[2][0] > cT[2][2] ) {
			throw new Exception("np1 < 1 || > npp" + toString());
		}
		if (cT[1][2] != (cT[2][2] - cT[0][2]) ) {
			throw new Exception("n2p != npp-n1p" + toString());
		}
		if (cT[2][1] != (cT[2][2] - cT[2][0]) ) {
			throw new Exception("np2 != npp-np1" + toString());
		}
			
		// make sure we don't have divide by 0 errors in the expected values
		if (cT[0][0] > 0 && cT_exp[0][0] == 0) {
			throw new Exception("m11 == 0" + toString());
		}
		if (cT[0][1] > 0 && cT_exp[0][1] == 0) {
			throw new Exception("m12 == 0" + toString());
		}
		if (cT[1][0] > 0 && cT_exp[1][0] == 0) {
			throw new Exception("m21 == 0" + toString());
		}
		if (cT[1][1] > 0 && cT_exp[1][1] == 0) {
			throw new Exception("m22 == 0" + toString());
		}
			
		// the loglikelihood/pmi/stirling tests require that we don't have negative m11 etc...
		if (cT_exp[0][0] < 0) {
			throw new Exception("m11 < 0" + toString());
		}
		if (cT_exp[0][1] < 0) {
			throw new Exception("m12 < 0" + toString());
		}
		if (cT_exp[1][0] < 0) {
			throw new Exception("m21 < 0" + toString());
		}
		if (cT_exp[1][1] < 0) {
			throw new Exception("m22 < 0" + toString());
		}
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

		// add the 100s stats we generated :0
		List<String> sortedKeys = new ArrayList<String>(stats.keySet());
		
		Collections.sort(sortedKeys);
		rv.addAll(sortedKeys);

		return rv;
	}
	
	/**
	 * Composes a string suitable for writing into an exported file ala "CSV" format
	 * @param d the delimiter
	 * @return String the N-gram as a string (no trailing newline)
	 */
	public String toCSV( String d ) {
		StringBuilder rv = new StringBuilder();

		// if the values collide with the delimiter just kill the programmer! :)

		// firstly, the name...
		rv.append(getName() + d );
		
		// ahh, now the 100s of stats :0
		List<String> sortedKeys = new ArrayList<String>(stats.keySet());
		
		Collections.sort(sortedKeys);
		Iterator itr = sortedKeys.iterator();
		while (itr.hasNext()) {
			rv.append( stats.get( (String)itr.next() ) );
			if ( itr.hasNext()) {
				// add the delimiter for the next element
				rv.append( d );
			}
		}

		return rv.toString();
	}

	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		rv.append("\n NgramCollection(" + getName() + ")");
		
		// the 100s of stats :0
		List<String> sortedKeys = new ArrayList<String>(stats.keySet());
		Collections.sort(sortedKeys);
		for (String k : sortedKeys) {
			rv.append("\n\t " + k + ": " + stats.get(k));
		}
		
		// the cT stuff
		try {
			rv.append("\n\t cT: n11=" + cT[0][0]);
			rv.append("\n\t cT: n12=" + cT[0][1]);
			rv.append("\n\t cT: n21=" + cT[1][0]);
			rv.append("\n\t cT: n22=" + cT[1][1]);
			rv.append("\n\t cT: n1p=" + cT[0][2]);
			rv.append("\n\t cT: n2p=" + cT[1][2]);
			rv.append("\n\t cT: np1=" + cT[2][0]);
			rv.append("\n\t cT: np2=" + cT[2][1]);
			rv.append("\n\t cT: npp=" + cT[2][2]);
			
			rv.append("\n\t cT_exp m11=" + cT_exp[0][0]);
			rv.append("\n\t cT_exp m12=" + cT_exp[0][1]);
			rv.append("\n\t cT_exp m21=" + cT_exp[1][0]);
			rv.append("\n\t cT_exp m22=" + cT_exp[1][1]);
		} catch (Exception e) {
			// ignore any exceptions, the cells might have not been filled as error somewhere in calculations...
		}
			
		return rv.toString();
	}
}
