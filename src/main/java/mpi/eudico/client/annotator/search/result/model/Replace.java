package mpi.eudico.client.annotator.search.result.model;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.search.content.result.model.ContentResult;

/**
 * @author klasal
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Replace {

    static public void execute(ContentResult result, String string) {
        execute(result, string, null);
    }

    static public void execute(ContentResult result, String string,
            TranscriptionImpl transcription) {
        if (transcription != null)
            transcription.setNotifying(false);

        for (int i = 1; i <= result.getRealSize(); i++) {
            ElanMatch match = (ElanMatch) result.getMatch(i);
            String oldString = match.getAnnotation().getValue();
            int[][] locations = match.getMatchedSubstringIndices();
            if (locations.length > 0) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(oldString.substring(0, locations[0][0]));
                    sb.append(string);
                    for (int j = 0; j < locations.length - 1; j++) {
                        sb.append(oldString.substring(locations[j][1],
                                locations[j + 1][0]));
                        sb.append(string);
                    }
                    sb.append(oldString.substring(locations[locations.length - 1][1],
                            oldString.length()));
                    match.getAnnotation().setValue(sb.toString());
                    // erase matchIndices
                    match.setMatchedSubstringIndices(new int[0][0]);
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println("Warning: " + e.getMessage());
                }

            }
        }

        if (transcription != null)
            transcription.setNotifying(true);
    }
}
