package mpi.eudico.server.corpora.event;

/**
 * LabelNotUniqueException is thrown when an attempt is made to add a track to
 * a CodeGroup that has a label that is already used. A label has to be unique
 * because it is used to uniquely identify a CodeGroup's tracks.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 29-Jun-1998
 */
public class LabelNotUniqueException extends Exception {
}
