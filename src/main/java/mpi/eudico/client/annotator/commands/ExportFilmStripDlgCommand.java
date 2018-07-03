package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.export.ExportFilmStripDialog;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.VideoFrameGrabber;


/**
 * A command that creates a dialog for configuring filmstrip + waveform image
 * creation.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportFilmStripDlgCommand implements Command {
    private String commandName;

    /**
     * Creates the command
     *
     * @param commandName the name
     */
    public ExportFilmStripDlgCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Creates the dialog for configuration.
     *
     * @param receiver the viewer manager
     * @param arguments ignored, null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;

        long bt = vm.getSelection().getBeginTime();
        long et = vm.getSelection().getEndTime();

        if (bt == et) {
            // no selection, warn
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                    vm.getTranscription()),
                ElanLocale.getString("ExportFilmStrip.Error.NoSelection"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        List<ElanMediaPlayer> visPlayers = new ArrayList<ElanMediaPlayer>(6);

        if (vm.getMasterMediaPlayer() instanceof VideoFrameGrabber) {
            visPlayers.add(vm.getMasterMediaPlayer());
        }

        List<ElanMediaPlayer> slaves = vm.getSlaveMediaPlayers();
        ElanMediaPlayer slPl;

        for (int i = 0; i < slaves.size(); i++) {
            slPl = slaves.get(i);

            if (slPl instanceof VideoFrameGrabber) {
                visPlayers.add(slPl);
            }
        }

        if (visPlayers.size() == 0) {
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                    vm.getTranscription()),
                ElanLocale.getString("ExportFilmStrip.Error.NoVideo"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        ElanMediaPlayer[] players = visPlayers.toArray(new ElanMediaPlayer[visPlayers.size()]);
        String wavFile = null;

        if (vm.getSignalViewer() != null) {
            wavFile = vm.getSignalViewer().getMediaPath();
        }

        // create a config dialog
        ExportFilmStripDialog dialog = new ExportFilmStripDialog(
        		ELANCommandFactory.getRootFrame(vm.getTranscription()), players,
                wavFile, vm.getSelection());
        dialog.setVisible(true);
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
