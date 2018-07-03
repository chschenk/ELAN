package mpi.search.content.query.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.DependentConstraint;


/**
 * $Id: Query2Xml.java 7382 2006-11-29 14:47:37Z klasal $ $Author$
 */
public class Query2Xml {
    private static final String ENCODING = "UTF-8";

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void translate(String fileName, ContentQuery query)
        throws IOException {
        File outputFile = new File(fileName);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    outputFile), ENCODING);
        out.write("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>\n");
        writeQueryToStream(query, out);
        out.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param out DOCUMENT ME!
     * @param constraint DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void writeAttributes(OutputStreamWriter out,
        Constraint constraint) throws IOException {
        out.write("\" regularExpression=\"");
        out.write("" + constraint.isRegEx());
        out.write("\" caseSensitive=\"");
        out.write("" + constraint.isCaseSensitive());

        if (constraint.getLowerBoundary() != Long.MIN_VALUE) {
            out.write("\" from=\"");
            out.write("" + constraint.getLowerBoundary());
        }

        if (constraint.getUpperBoundary() != Long.MAX_VALUE) {
            out.write("\" to=\"");
            out.write("" + constraint.getUpperBoundary());
        }

        out.write("\" unit=\"");
        out.write(constraint.getUnit());
    }

    /**
     * DOCUMENT ME!
     *
     * @param query
     * @param out
     *
     * @throws IOException
     */
    public static void writeQueryToStream(ContentQuery query,
        OutputStreamWriter out) throws IOException {
        out.write("<query date=\"" +
            new Date(System.currentTimeMillis()).toString() + "\">\n");

        out.write("<description>");
        out.write(query.toString());
        out.write("</description>\n");

        List<Constraint> constraints = query.getConstraints();
        AnchorConstraint anchorConstraint = (AnchorConstraint) constraints.get(0);

        if (anchorConstraint != null) {
            out.write("<anchorConstraint id=\"");
            out.write("" + anchorConstraint.getId());
            writeAttributes(out, anchorConstraint);
            out.write("\">\n");

            String[] tierNames = anchorConstraint.getTierNames();

            for (String tierName : tierNames) {
                out.write("<tier>" + tierName + "</tier>");
            }

            out.write("<pattern>" + anchorConstraint.getPattern() +
                "</pattern>\n");
            out.write("</anchorConstraint>");
        }

        for (int i = 1; i < constraints.size(); i++) {
            DependentConstraint dependentConstraint = (DependentConstraint) constraints.get(i);
            out.write("<dependentConstraint id=\"");
            out.write("" + dependentConstraint.getId());
            out.write("\" mode=\"");
            out.write(dependentConstraint.getMode());
            out.write("\" quantifier=\"");
            out.write(dependentConstraint.getQuantifier());
            writeAttributes(out, dependentConstraint);
            out.write("\" id_ref=\"");
            out.write("" +
                dependentConstraint.getParent().getId());
            out.write("\">\n");
            out.write("<tier>" + dependentConstraint.getTierName() + "</tier>");
            out.write("<pattern>" + dependentConstraint.getPattern() +
                "</pattern>\n");
            out.write("</dependentConstraint>\n");
        }

        out.write("</query>\n");
    }
}
