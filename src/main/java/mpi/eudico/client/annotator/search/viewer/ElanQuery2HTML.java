package mpi.eudico.client.annotator.search.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import mpi.eudico.client.annotator.search.result.viewer.ElanResult2HTML;
import mpi.search.SearchLocale;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.viewer.Query2HTML;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentResult2HTML;


/**
 * $Id: ElanQuery2HTML.java 43670 2015-04-13 15:36:51Z olasei $
 *
 * @author $author$
 * @version $Revision$
 */
public class ElanQuery2HTML {
    /* background colour same as default in elan constants */
    private static final String bodyStyle = "body { background-color: #E6E6E6; }\n";

    /* date format conform to ISO 8601 */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mmz");
    static final String css = "<style type=\"text/css\">\n" + bodyStyle +
        Query2HTML.bodyStyle + Query2HTML.constraintStyle +
        Query2HTML.patternStyle + ElanResult2HTML.matchListStyle +
        "</style>\n";

    /**
     * exports a Query with its Result to an HTML file
     *
     * @param query to export
     * @param exportFile file
     * @param asTable if true, export as table analogous to the table in the
     *        application;  if false export of matches in tree structure
     *        analogous to the tooltips of the annotation column
     * @param transcriptionFilePath DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws IOException
     */
    public static void exportQuery(ContentQuery query, File exportFile,
        boolean asTable, String transcriptionFilePath, String encoding)
        throws IOException {
        if (exportFile == null) {
            return;
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    encoding));

        StringBuilder sb = new StringBuilder("<html>\n");
        sb.append(
            "<head profile=\"http://dublincore.org/documents/dcq-html/\">\n");
        sb.append(
            "<link rel=\"schema.DC\" href=\"http://purl.org/dc/elements/1.1/\">\n");
        sb.append(
            "<link rel=\"schema.DCTERMS\" href=\"http://purl.org/dc/terms/\">\n");
        sb.append(
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=\"" +
            encoding + "\">\n");
        sb.append("<meta name=\"DC.date\" content=\"" +
            dateFormat.format(query.getCreationDate()) + "\" scheme=\"DCTERMS.W3CDTF\">\n");
        sb.append(
            "<meta name=\"DC.description\" content=\"Query performed by ELAN on file " +
            new File(transcriptionFilePath).getName() + "\">\n");
        sb.append(css);
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<H2>" + SearchLocale.getString("SearchDialog.Query") +
            ":</H2>\n");
        Query2HTML.appendQuery(sb, query);
        sb.append("<br>\n");

        String resultString = SearchLocale.getString("Search.Result");

        //capitalize first letter
        resultString = resultString.substring(0, 1).toUpperCase() +
            resultString.substring(1);
        sb.append("<H2>" + resultString + ":</H2>\n");

        if (asTable) {
            ContentResult2HTML.appendResultAsTable(sb,
                (ContentResult) query.getResult());
        } else {
            ElanResult2HTML.appendResultAsTree(sb,
                (ContentResult) query.getResult());
        }

        sb.append("</body>\n</html>");

        writer.write(sb.toString());

        writer.close();
    }
}
