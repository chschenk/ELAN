package mpi.eudico.client.annotator.timeseries;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ExtractTrackDataCommand;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;



/**
 * The final step of the extraction process.  Closes the wizard when finished.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExtractStep3 extends ProgressStepPane implements ClientLogger,
    ProgressListener {
    private TranscriptionImpl transcription;
    private TSTrackManager manager;
    private Command command;

    /**
     * Creates a new ExtractStep3 instance, the final step.
     *
     * @param multiPane the container multistep pane
     * @param transcription the transcription containing source and destination
     *        tier
     * @param manager the track manager containing the time series tracks
     */
    public ExtractStep3(MultiStepPane multiPane,
        TranscriptionImpl transcription, TSTrackManager manager) {
        super(multiPane);
        this.transcription = transcription;
        this.manager = manager;
        initComponents();
    }

    /**
     * Initialize ui components etc. a label and a progressbar.
     */
    @Override
	public void initComponents() {
    	super.initComponents();
    	progressLabel.setText(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Extracting"));
    }

    /**
     * Returns the title of this step.
     *
     * @return the title
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("TimeSeriesViewer.Extract.Extracting");
    }

    /**
     * Starts the actual extraction progress in a separate thread. Closes the
     * dialog on finish.
     *
     * @return false
     */
    @Override
	public boolean doFinish() {
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);

        String sourceTierName = (String) multiPane.getStepProperty("SourceTier");
        String destTierName = (String) multiPane.getStepProperty("DestTier");
        String trackName = (String) multiPane.getStepProperty("TrackName");
        String method = (String) multiPane.getStepProperty("Calc");

        if (method == null) {
            LOG.warning("Unknown calculation method.");
            notifyCancel(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NoMethod"));

            return false;
        }

        String overwr = (String) multiPane.getStepProperty("Overwrite");
        boolean overwrite = true;

        if ("false".equals(overwr)) {
            overwrite = false;
        }

        Tier sourceTier = (Tier) transcription.getTierWithId(sourceTierName);

        if (sourceTier == null) {
            LOG.warning("Source tier is null: " + sourceTierName);
            notifyCancel(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " +
                sourceTierName);

            return false;
        }

        Tier destTier = (Tier) transcription.getTierWithId(destTierName);

        if (destTier == null) {
            LOG.warning("Destination tier is null: " + destTierName);
            notifyCancel(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " + destTierName);

            return false;
        }

        AbstractTSTrack track = manager.getTrack(trackName);

        if (track == null) {
            LOG.warning("Track is null: " + trackName);
            notifyCancel(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " + trackName);

            return false;
        }

        command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EXT_TRACK_DATA);
        ((ExtractTrackDataCommand) command).addProgressListener(this);

        command.execute(transcription,
            new Object[] {
                sourceTierName, destTierName, track, method,
                Boolean.valueOf(overwrite)
            });

        // the action is performed on a separate thread, don't close
        return false;
    }

    /**
     * This is a "finish only" step, no user interaction required. Delegates to
     * doFinish().
     */
    @Override
	public void enterStepForward() {
        doFinish();
    }

    private void notifyCancel(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
        multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
    }

    /**
     * Unregister as a progres listener and close.
     */
	@Override
	protected void endOfProcess() {
        if (command != null) {
            ((ExtractTrackDataCommand) command).removeProgressListener(this);
        }

        multiPane.close();
	}

}
