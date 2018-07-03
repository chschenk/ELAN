package mpi.eudico.client.annotator.search.result.viewer;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;

import mpi.search.SearchLocale;

import mpi.search.content.result.model.ContentMatch;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentResult2HTML;


/**
 * There is no class ElanResult; it is merely assumed that a ContentResult
 * contains ElanMatches  $Id: ElanMatch2HTML.java,v 1.4 2007/01/31 16:35:28
 * klasal Exp $
 *
 * @author $author$
 * @version $Revision$
 */
public class ElanResult2HTML {
    private static final int maxVisibleChildren = 5;

    /**
     * 
     */
    public static final String matchListStyle = "ul { list-style-type:none;}\n";
    private static final String css = "<style type=\"text/css\">" +
        matchListStyle + "<style>";

    /**
     *
     *
     * @param sb DOCUMENT ME!
     * @param rootMatch DOCUMENT ME!
     * @param withChildren DOCUMENT ME!
     * @param withCSS DOCUMENT ME!
     */
    public static void appendMatch(StringBuilder sb, ContentMatch rootMatch,
        boolean withChildren, boolean withCSS) {
        ContentResult2HTML.appendMatchValue(sb, rootMatch);

        if (rootMatch instanceof ElanMatch && withChildren) {
            addChildren(sb, (ElanMatch) rootMatch, withCSS);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param sb
     * @param result
     */
    public static void appendResultAsTree(StringBuilder sb, ContentResult result) {
        for (int i = 0; i < result.getRealSize(); i++) {
            ElanResult2HTML.appendMatch(sb, (ElanMatch) result.getMatch(i + 1),
                true, true);
            sb.append("<br>\n");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param rootMatch root resp. anchor match
     * @param withChildren true, if children matches should be included
     *
     * @return html representation of match
     */
    public static String translate(ContentMatch rootMatch, boolean withChildren) {
        return translate(rootMatch, withChildren, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param rootMatch root resp. anchor match
     * @param withChildren true if children matches should be included
     * @param withCSS true, if resulting html is to be interpreted by a browser
     *
     * @return html representation of match
     */
    public static String translate(ContentMatch rootMatch,
        boolean withChildren, boolean withCSS) {
        StringBuilder sb = new StringBuilder("<HTML>\n");

        if (withCSS) {
            sb.append("<HEAD>" + css + "</HEAD>\n");
        }

        sb.append("<BODY>\n");

        appendMatch(sb, rootMatch, withChildren, withCSS);

        sb.append("\n</BODY>\n</HTML>");

        return sb.toString();
    }

    /**
     * within java components, it isn't possible to turn off the marker of a
     * list item with css; as a workaround, this method applies tag BR instead
     * of LI; For an export to an html file (to be read by a browser), the use
     * of css is recommended.
     *
     * @param sb
     * @param parentMatch
     * @param withCSS
     */
    private static void addChildren(StringBuilder sb, ElanMatch parentMatch,
        boolean withCSS) {
        if (parentMatch.getChildCount() > 0) {
            sb.append("<ul>");

            String lastConstraintId = null;

            for (int i = 0; i < parentMatch.getChildCount(); i++) {
                if (i >= maxVisibleChildren) {
                    sb.append("... (" +
                        (parentMatch.getChildCount() - maxVisibleChildren) +
                        " " + SearchLocale.getString("Search.More") + ")");

                    break;
                }

                ElanMatch childMatch = (ElanMatch) parentMatch.getChildAt(i);

                if ((lastConstraintId != null) &&
                        !(childMatch.getConstraintId().equals(lastConstraintId))) {
                    sb.append("</ul><ul>");

                    lastConstraintId = childMatch.getConstraintId();
                }

                //if (withCSS) {
                    sb.append("<li>");
                //}

                ContentResult2HTML.appendMatchValue(sb, childMatch);
                // append tier name of child??
                sb.append("  (" + childMatch.getTierName() + ")");

                addChildren(sb, childMatch, withCSS);

                //if (withCSS) {
                    sb.append("</li>");
                //} else {
                    //if (!withCSS){
                    //	sb.append("<br>\n");
                   // }                   
                //}
            }

            sb.append("</ul>");
        }
    }
}
