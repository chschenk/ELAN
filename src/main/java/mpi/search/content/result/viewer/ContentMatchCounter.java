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
package mpi.search.content.result.viewer;

import javax.swing.SwingConstants;

import mpi.search.SearchLocale;
import mpi.search.content.result.model.ContentResult;
import mpi.search.result.viewer.MatchCounter;


/**
 * $Id: ContentMatchCounter.java 8348 2007-03-09 09:43:13Z klasal $
 * $Author$ $Version$
 */
public class ContentMatchCounter extends MatchCounter {

    /**
     * Creates a new MatchCounter object.
     */
    public ContentMatchCounter() {
        setHorizontalAlignment(SwingConstants.CENTER);
        render();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void render() {
        StringBuilder text = new StringBuilder();

        if (result != null) {
            if (result instanceof ContentResult) {
                text.append(
                    ((ContentResult) result).getOccurrenceCount() + " " +
                    SearchLocale.getString(
                        (((ContentResult) result).getOccurrenceCount() == 1)
                        ? "Search.Occurrence_SG" : "Search.Occurrence_PL") + " in ");

                text.append(
                    result.getRealSize() + " " +
                    SearchLocale.getString(
                        (result.getRealSize() == 1) ? "Search.Annotation_SG"
                                                      : "Search.Annotation_PL"));
            }
            else {
                text.append(result.getRealSize() + " matches");
            }
        }

        setText(text.toString());
    }
}
