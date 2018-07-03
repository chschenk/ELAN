package mpi.eudico.client.annotator.interannotator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An object to combine (two) sets of segments with some additional information 
 * about each set (file name, tier name, annotator).
 * It can also be used to store the agreement value(s) once calculated.
 * The agreement value will mostly be a number between 0 and 1 but this may depend on
 * the algorithm. 
 */
public class CompareCombi {
	private CompareUnit firstUnit;
	private CompareUnit secondUnit;
	private double overallAgreement;
	private Map<String, Double> perValueAgreement;
	
	/**
	 * Constructor with to compare units as parameter. Should both be non-null.
	 * 
	 * @param firstUnit
	 * @param secondUnit
	 */
	public CompareCombi(CompareUnit firstUnit, CompareUnit secondUnit) {
		super();
		this.firstUnit = firstUnit;
		this.secondUnit = secondUnit;
		overallAgreement = 0.0d;
		perValueAgreement = new HashMap<String, Double>();
	}

	public CompareUnit getFirstUnit() {
		return firstUnit;
	}
	
	public CompareUnit getSecondUnit() {
		return secondUnit;
	}

	public Map<String, Double> getPerValueAgreement() {
		return perValueAgreement;
	}

	public void setPerValueAgreement(Map<String, Double> perValueAgreement) {
		this.perValueAgreement = perValueAgreement;
	}
	
	/**
	 * Adds the agreement for a specific code or annotation value.
	 * 
	 * @param value the code
	 * @param agreement the agreement
	 */
	public void addAgreementForValue(String value, double agreement) {
		if (perValueAgreement == null) {
			perValueAgreement = new HashMap<String, Double>();
		}
		perValueAgreement.put(value, agreement);
	}
	
	/**
	 * @param value the code or annotation value
	 * @return the agreement for a specific annotation value or 0 if the value is not in the map
	 */
	public double getAgreementForValue(String value) {
		if (perValueAgreement != null) {
			Double ag = perValueAgreement.get(value);
			if (ag != null) {
				return ag;
			}
		}
		
		return 0.0d;
	}
	
	/**
	 * The overall agreement can be used if the calculation has been done regardless of codes 
	 * (annotation values), or if there is only one value in all annotations or for the average
	 *  agreement value.
	 * 
	 * @return the overall agreement
	 */
	public double getOverallAgreement() {
		return overallAgreement;
	}

	/**
	 * Sets the overall agreement.
	 * 
	 * @param overallAgreement
	 */
	public void setOverallAgreement(double overallAgreement) {
		this.overallAgreement = overallAgreement;
	}

	/**
	 * Calculates the average of the agreements per value. ?
	 */
	public void calculateOverallAgreement() {
		if (perValueAgreement != null) {
			int count = perValueAgreement.size();
			if (count == 0) {
				return;
			}
			double aTotal = 0.0;
			Iterator<Double> valueIterator = perValueAgreement.values().iterator();
			while (valueIterator.hasNext()) {
				aTotal += valueIterator.next();
			}
			
			overallAgreement = aTotal / count;
		}		
	}
}
