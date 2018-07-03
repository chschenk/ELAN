package mpi.eudico.client.annotator.interannotator.modkappa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.interannotator.CompareCombi;
import mpi.eudico.client.annotator.interannotator.CompareUnit;

/**
 * A class that extends the CompareCombi with storage for contingency tables.
 * A big one for all values in the annotations, and for each value a 2x2 table
 * that is the basis of the kappa calculation.
 */
public class KappaCompareCombi extends CompareCombi {
	private List<String> allValues;
	private Map<String, TwoSquareTable> perValueTables;
	private AgreementTable overallTable;
	
	public KappaCompareCombi(CompareUnit firstUnit, CompareUnit secondUnit) {
		super(firstUnit, secondUnit);
	}

	/**
	 * Returns the 2x2 agreement table for the specified value.
	 * 
	 * @param codeValue the code value to get the agreement table of
	 * @return the 2x2 agreement table or null
	 */
	public TwoSquareTable getAgreementTable(String codeValue) {
		if (perValueTables != null) {
			return perValueTables.get(codeValue);
		}
		return null;
	}
	
	/**
	 * Adds the 2x2 agreement table for the specified coding value.  
	 * @param codeValue the code value
	 * @param table the 2x2 table
	 */
	public void setAgreementTable(String codeValue, TwoSquareTable table) {
		if (perValueTables == null) {
			perValueTables = new HashMap<String, TwoSquareTable>();
		}
		perValueTables.put(codeValue, table);
	}
	
	/* a straight set of getters and setters */
	public List<String> getAllValues() {
		return allValues;
	}

	public void setAllValues(List<String> allValues) {
		this.allValues = allValues;
	}

	public AgreementTable getOverallTable() {
		return overallTable;
	}

	public void setOverallTable(AgreementTable overallTable) {
		this.overallTable = overallTable;
	}

	public Map<String, TwoSquareTable> getPerValueTables() {
		return perValueTables;
	}

	public void setPerValueTables(Map<String, TwoSquareTable> perValueTables) {
		this.perValueTables = perValueTables;
	}
	
	/**
	 * Doubles all the values in the cells that refer to (time-wise) "matched" annotations.
	 * See the Holle & Rein paper:
	 * Holle, H., & Rein, R. (2014). EasyDIAg: A tool for easy determination of interrater agreement. 
	 * Behavior Research Methods, published online August 2014. 
	 * doi:10.3758/s13428-014-0506-7
	 */
	public void doubleMatchedValues() {
		if (overallTable != null) {
			overallTable.doubleMatchedValues();
		}
		if (perValueTables != null) {
			for (AgreementTable at : perValueTables.values()) {
				at.doubleMatchedValues();
			}
		}
	}
}
