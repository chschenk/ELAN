package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: PreviousScrollViewCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PreviousScrollViewCommand implements Command {
    private String commandName;

    /**
     * Creates a new PreviousScrollViewCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PreviousScrollViewCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            long duration = ts.getIntervalDuration();

            long newBegin = ts.getBeginTime() - duration;

            if (newBegin < 0) {
                newBegin = 0L;
            }

            ts.setBeginTime(newBegin);
            ts.setEndTime(ts.getBeginTime() + duration);

            if ((((ElanMediaPlayer) receiver).getMediaTime() -
                    ts.getIntervalDuration()) < 0) {
                ((ElanMediaPlayer) receiver).setMediaTime(0L);
            } else {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() -
                    ts.getIntervalDuration());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
