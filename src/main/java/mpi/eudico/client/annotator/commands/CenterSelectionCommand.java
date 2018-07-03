package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command to position the selection horizontally in the center of the
 * Timeline viewer.
 *
 * @author Han Sloetjes
 */
public class CenterSelectionCommand implements Command {
    private String name;

    /**
     * Constructor.
     *
     * @param name
     */
    public CenterSelectionCommand(String name) {
        this.name = name;
    }

    /**
     * Centers the selection in the Timeline viewer.
     *
     * @param receiver the viewer manager
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof ViewerManager2) {
            if (!((ViewerManager2) receiver).getMasterMediaPlayer().isPlaying()) {
                Selection s = ((ViewerManager2) receiver).getSelection();
                ElanLayoutManager elm = ELANCommandFactory.getLayoutManager(((ViewerManager2) receiver).getTranscription());

                if ((elm.getVisibleMultiTierViewer() == elm.getTimeLineViewer()) &&
                        (s.getBeginTime() != s.getEndTime())) {
                    long bt = elm.getTimeLineViewer().getIntervalBeginTime();
                    long et = elm.getTimeLineViewer().getIntervalEndTime();
                    long selMid = (s.getBeginTime() + s.getEndTime()) / 2;
                    long oldMid = (bt + et) / 2;
                    long newBT = bt + (selMid - oldMid);

                    if ((newBT + (et - bt)) > ((ViewerManager2) receiver).getMasterMediaPlayer()
                                                   .getMediaDuration()) {
                        newBT = ((ViewerManager2) receiver).getMasterMediaPlayer()
                                 .getMediaDuration() - (et - bt);
                    }

                    elm.getTimeLineViewer()
                       .setIntervalBeginTime((newBT < 0) ? 0 : newBT);
                }
            }
        }
    }

    /**
     * Returns the name
     *
     * @return name
     */
    @Override
	public String getName() {
        return name;
    }
}
