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
package mpi.search.content.result.model;

import java.util.ArrayList;
import java.util.List;

import mpi.search.result.model.Result;

/**
 * Created on Jul 27, 2004
 * 
 * @author Alexander Klassmann
 * @version Jul 27, 2004
 */
@SuppressWarnings("serial")
public class ContentResult extends Result {
    private final List<String> tierNames = new ArrayList<String>();

    private int occurrenceCount = 0;

    /**
     * DOCUMENT ME!
     * TODO HS check if synchronized really makes sense and solves concurrency problems in the FASTsearch
     * 
     * @param match
     *            DOCUMENT ME!
     */
    @Override
	public synchronized void addMatch(ContentMatch match) {
        super.addMatch(match);

        if (match instanceof ContentMatch) {
            final ContentMatch contentMatch = (ContentMatch) match;
			if (!tierNames.contains(contentMatch.getTierName())) {
                tierNames.add(contentMatch.getTierName());
            }

            if (contentMatch.getMatchedSubstringIndices() != null) {
                occurrenceCount += contentMatch.getMatchedSubstringIndices().length;
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param tierName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public List<ContentMatch> getMatches(String tierName) {
        if (tierName == null) {
            return null;
        }

        List<ContentMatch> matchesInTier = new ArrayList<ContentMatch>();

        for (int i = 1; i <= getRealSize(); i++) {
            if (tierName.equals(((ContentMatch) getMatch(i)).getTierName())) {
                matchesInTier.add(getMatch(i));
            }
        }

        return matchesInTier;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void reset() {
        super.reset();
        tierNames.clear();
        occurrenceCount = 0;
    }

    /**
     * returns a list of all tierNames present in matches. Ordered along first
     * occurence.
     * 
     * @return List
     */
    public String[] getTierNames() {
        return (String[]) tierNames.toArray(new String[0]);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getOccurrenceCount() {
        return occurrenceCount;
    }
}
