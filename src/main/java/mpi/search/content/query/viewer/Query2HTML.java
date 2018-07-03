package mpi.search.content.query.viewer;

import java.util.Map;

import mpi.search.SearchLocale;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.DependentConstraint;
import mpi.search.content.query.model.RestrictedAnchorConstraint;


/**
 * $Id: Query2HTML.java 8120 2007-02-06 16:36:07Z klasal $
 *
 * @author $author$
 * @version $Revision$
 */
public class Query2HTML {
    public static final String bodyStyle = "body { font-weight:normal; margin-top: 5px; }\n";
    public static final String constraintStyle = ".constraint { border-width:1px; border-style:solid; border-color:gray; padding:10px;font-size:medium;}\n";
    public static final String patternStyle = ".pattern { background:#FFFFFF; white-space:pre; font-weight:bold;}\n";
    static final String css = "<style type=\"text/css\">" + bodyStyle +
        constraintStyle + patternStyle + "<style>";

    /**
     * appends single constraint as HTML to StringBuilder (without opening and closing BODY-tag)
     *
     * @param sb buffer to append on
     * @param constraint to be rendered in HTML
     */
    public static void appendConstraint(StringBuilder sb, Constraint constraint) {
        if (constraint instanceof DependentConstraint) {
            sb.append(SearchLocale.getString("Search.Query.With").toUpperCase() +
                " " + SearchLocale.getString("Search.Query.Constraint"));
        } else {
            sb.append(SearchLocale.getString("Search.Query.Find").toUpperCase());
        }

        sb.append("<BR><div class=\"constraint\">");

        if (constraint instanceof RestrictedAnchorConstraint) {
            sb.append(((RestrictedAnchorConstraint) constraint).getComment());
        } else {
            sb.append(SearchLocale.getString(constraint.getQuantifier()));
            sb.append(" " +
                SearchLocale.getString("Search.Annotation_SG") +
                " ");

            String[] tierNames = constraint.getTierNames();

            if (tierNames.length > 0) {
                if (!tierNames[0].equals(Constraint.ALL_TIERS)) {
                	if (tierNames.length < 3) {
                		sb.append(SearchLocale.getString("Search.Constraint.OnTier")+" ");
	                    for (int j = 0; j < tierNames.length; j++) {
	                        sb.append("\"<b>" + tierNames[j] + "</b>\"");
	                        if (j != tierNames.length - 1) {
	                        	sb.append(", ");
	                        } else {
	                        	sb.append(" ");
	                        }
	                    }
                	} else {
                		sb.append(SearchLocale.getString("Search.Constraint.OnTierInSet") + " ");
                	}
                }
            }

            if (!constraint.getPattern().equals("")) {
                if (constraint instanceof AnchorConstraint) {
                    sb.append(SearchLocale.getString("Search.Constraint.That") +
                        " ");
                }

                sb.append(SearchLocale.getString("Search.Constraint.Matches") +
                    " ");

                if (constraint.isCaseSensitive()) {
                    sb.append(SearchLocale.getString(
                            "Search.Constraint.CaseSensitive") + " ");
                }

                sb.append(constraint.isRegEx()
                    ? (SearchLocale.getString(
                        "Search.Constraint.RegularExpression") + " ")
                    : (SearchLocale.getString("Search.Constraint.String") +
                    " "));

                sb.append("<span class=\"pattern\" style=\"white-space:pre;\"><b> " +
                    constraint.getPattern() + " </b></span>");
            }

            sb.append("<BR>");

            if (constraint instanceof AnchorConstraint) {
                if ((constraint.getLowerBoundary() > Long.MIN_VALUE) ||
                        (constraint.getUpperBoundary() < Long.MAX_VALUE)) {
                    if (constraint.getPattern().equals("")) {
                        sb.append(SearchLocale.getString(
                                "Search.Constraint.That"));
                    } else {
                        sb.append(SearchLocale.getString("Search.And"));
                    }

                    sb.append(" " +
                        SearchLocale.getString(constraint.getUnit()) + " " +
                        SearchLocale.getString("Search.Interval") + " [" +
                        constraint.getLowerBoundary() + " ms ; " +
                        constraint.getUpperBoundaryAsString() + " ms]\n");
                }
            } else {
                if (constraint.getMode().equals(Constraint.STRUCTURAL)) {
                    sb.append(SearchLocale.getString(
                            "Search.Constraint.Distance") + " " +
                        constraint.getLowerBoundaryAsString() + " " +
                        SearchLocale.getString("Search.To") + " " +
                        constraint.getUpperBoundaryAsString() + " " +
                        constraint.getUnit() + " ");
                } else {
                    if (constraint.getPattern().equals("")) {
                        sb.append(SearchLocale.getString(
                                "Search.Constraint.That") + " ");
                    } else {
                        sb.append(SearchLocale.getString("Search.And") + " ");
                    }

                    String unit = SearchLocale.getString(constraint.getUnit());

                    boolean constraintWithDistance = Constraint.WITHIN_OVERALL_DISTANCE.equals(constraint.getUnit()) ||
                        Constraint.WITHIN_DISTANCE_TO_LEFT_BOUNDARY.equals(constraint.getUnit()) ||
                        Constraint.WITHIN_DISTANCE_TO_RIGHT_BOUNDARY.equals(constraint.getUnit()) ||
                        Constraint.BEFORE_LEFT_DISTANCE.equals(constraint.getUnit()) ||
                        Constraint.AFTER_RIGHT_DISTANCE.equals(constraint.getUnit());

                    if (constraintWithDistance) {
                        unit = unit.replaceFirst("\\.\\.\\.",
                                constraint.getUpperBoundaryAsString() + " ms");
                    }

                    sb.append(unit);
                }

                sb.append("\n");
            }

            sb.append("\n");

            Map<String, String> attributes = constraint.getAttributes();

            if (attributes != null) {
                for (Map.Entry<String, String> e : attributes.entrySet()) {
                    String key = e.getKey();
                    String attributeValue = e.getValue();

                    if (!attributeValue.equals("*") &&
                            !attributeValue.equals("") &&
                            !attributeValue.equals("ANY")) {
                        sb.append(" " + key + "=");

                        if (attributeValue.equals(">0")) {
                            sb.append("[^0]");
                        } else if (attributeValue.equals(">1")) {
                            sb.append("[^01]");
                        } else {
                            sb.append(attributeValue);
                        }
                    }
                }
            }
        }

        sb.append("</div>\n");
    }

    /**
     * appends all constraints of a query as tree like structure
     *
     * @param sb Buffer to append on
     * @param query 
     */
    public static void appendQuery(StringBuilder sb, ContentQuery query) {
        Constraint anchorConstraint = query.getAnchorConstraint();
        appendConstraint(sb, anchorConstraint);
        appendDescendantConstraints(sb, anchorConstraint);
    }

    /**
     * translates whole Query into HTML, including HTML start- and end-tags as well as css
     *
     * @param query to be rendered
     *
     * @return complete html document
     */
    public static String translate(ContentQuery query) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>" + css + "</head><body>");

        Query2HTML.appendQuery(sb, query);

        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * translates single constraint into HTML, including HTML start- and end-tags as well as css
     *
     * @param constraint to be rendered
     *
     * @return complete html document
     */
    public static String translate(Constraint constraint) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>" + css + "</head><body>");

        appendConstraint(sb, constraint);

        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * appends recursively descendants of a constraint (not the constraint itself) in a tree like structure
     * @param sb
     * @param parentConstraint
     */
    private static void appendDescendantConstraints(StringBuilder sb, Constraint parentConstraint) {
        if (parentConstraint.getChildCount() > 0) {
            sb.append("<ul>");

            for (int i = 0; i < parentConstraint.getChildCount(); i++) {
                Constraint childConstraint = parentConstraint.getChildAt(i);

                sb.append("<li>");

                appendConstraint(sb, childConstraint);

                appendDescendantConstraints(sb, childConstraint);

                sb.append("</li>");
            }

            sb.append("</ul>");
        }
    }
}
