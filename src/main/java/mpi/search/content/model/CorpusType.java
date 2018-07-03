/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.content.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.util.CVEntry;


/**
 * This interface describes Corpus-specific Types and Relations of tiers and possible
 * units of distance between them. Both Tiers and Units are stored in Arrays of
 * FieldTypes, witch contain a full name and a mnemonic, resp.
 *
 * $Id: CorpusType.java 10188 2007-09-18 11:08:38Z klasal $
 */
public abstract class CorpusType {
    /** Holds value of short names of units */
    protected final Map<String, String> unitMnemonics = new HashMap<String, String>();
    protected String frameTitle;
    protected String coarserUnit1 = null;
    protected String coarserUnit2 = null;
    protected String coarserUnit3 = null;
    protected String standardUnit = null;
    protected String[] tierNames;

    /**
     * Returns a fixed, hard-wired list of Tier-Types.
     *
     * @return DOCUMENT ME!
     */
    public String[] getTierNames() {
        return tierNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract String[] getIndexTierNames();

    /**
     * Returns a mnemonic (used by the perl/python script) for a unit
     *
     * @param unit DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUnitMnemonic(String unit) {
        return unitMnemonics.containsKey(unit) ? (String) unitMnemonics.get(unit) : null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract boolean allowsSearchOverMultipleTiers();

    /**
     * DOCUMENT ME!
     *
     * @param mnemonic
     *
     * @return String
     */
    public String getUnitFromMnemonic(String mnemonic) {
//        for (String key : unitMnemonics.keySet()) {
//
//            if (unitMnemonics.get(key).equals(mnemonic)) {
//                return key;
//            }
//        }
      for (Map.Entry<String, String> e : unitMnemonics.entrySet()) {

          if (e.getValue().equals(mnemonic)) {
              return e.getKey();
          }
      }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName
     *
     * @return unabbreviatedTierName
     */
    public String getUnabbreviatedTierName(String tierName) {
        return Arrays.asList(tierNames).contains(tierName) ? tierName : null;
    }

    /**
     * Returns a title for the Search-Frame
     *
     * @return DOCUMENT ME!
     */
    public String getFrameTitle() {
        return frameTitle;
    }

    /**
     * Returns, if exists, a list of closed vocabulary corresponding to a tier; Otherwise
     * null.
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract List<CVEntry> getClosedVoc(String tierName);

    /**
     * Returns true if there is a "closed vocabular" for this fieldType
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract boolean isClosedVoc(String tierName);

    /**
     * Returns a true if a "Closed Vocabulary" should be editable (thus not really
     * closed).
     *
     * @param closedVoc DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract boolean isClosedVocEditable(List<CVEntry> closedVoc);

    /**
     * Returns the default Locale of a Field Type
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract Locale getDefaultLocale(String tierName);

    /**
     * returns all tiers with the same root tier as the specified tier
     *
     * @param tierName DOCUMENT ME!
     *
     * @return array of tier Names
     */
    public abstract String[] getRelatedTiers(String tierName);

    /**
     * Returns the possible units, which fieldType1 and fieldType2 both can be measured
     * with
     *
     * @param tierName1 DOCUMENT ME!
     * @param tierName2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getPossibleUnitsFor(String tierName1, String tierName2) {
        List<String> list = new ArrayList<String>();

        if (standardUnit != null) {
            list.add(standardUnit);
        }

        if (coarserUnit1 != null) {
            list.add(coarserUnit1);
        }

        if (coarserUnit2 != null) {
            list.add(coarserUnit2);
        }

        if (coarserUnit3 != null) {
            list.add(coarserUnit3);
        }

        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Some tiers might be obligatory case sensitive (pho)
     *
     * @param tierName DOCUMENT ME!
     *
     * @return boolean
     */
    public abstract boolean strictCaseSensitive(String tierName);

    /**
     * Returns the default unit
     *
     * @return String
     */
    public String getDefaultUnit() {
        return standardUnit;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract boolean hasAttributes();

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract String[] getAttributeNames(String tierName);

    /**
     * DOCUMENT ME!
     *
     * @param attributeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract String getToolTipTextForAttribute(String attributeName);

    public abstract boolean allowsQuantifierNO();
    
    public abstract boolean allowsTemporalConstraints();
    
    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param attributeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract Object getPossibleAttributeValues(
        String tierName, String attributeName);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract Class getInputMethodClass();
}
