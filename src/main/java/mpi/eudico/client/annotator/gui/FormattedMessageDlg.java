package mpi.eudico.client.annotator.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.player.*;
import mpi.eudico.util.TimeFormatter;


/**
 * Shows a dialog with a formatted message.
 *
 * @author Han Sloetjes
 */
public class FormattedMessageDlg {
    private JLabel label;

    /**
     * Creates a new FormattedMessageDlg instance.
     *
     * @param mes a two-dimensional array of Strings, that will be shown in  a
     *        html table in a message dialog
     */
    public FormattedMessageDlg(String[][] mes) {
        showMessage(null, mes);
    }

    /**
     * Creates a new FormattedMessageDlg instance that extracts information
     * from the player's media descriptor.
     *
     * @param player an Elan Media Player
     */
    public FormattedMessageDlg(ElanMediaPlayer player) {
        String[][] mes = createMediaInfo(player);
        showMessage(player, mes);
    }

    /**
     * Creates a two-dimensional String array.
     *
     * @param player an Elan media player
     *
     * @return a two-dimensional String array
     */
    private String[][] createMediaInfo(ElanMediaPlayer player) {
        if ((player == null) || (player.getMediaDescriptor() == null)) {
            return null;
        }

        String[][] info = new String[6][2];
        info[0][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaURL");
        info[0][1] = player.getMediaDescriptor().mediaURL;
        info[1][0] = ElanLocale.getString("LinkedFilesDialog.Label.MimeType");
        info[1][1] = player.getMediaDescriptor().mimeType;
        info[2][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaOffset");
        info[2][1] = String.valueOf(player.getMediaDescriptor().timeOrigin);
        info[3][0] = ElanLocale.getString("Player.duration");
        info[3][1] = TimeFormatter.toString(player.getMediaDuration());
        info[4][0] = ElanLocale.getString("Player.FrameRate");
        info[4][1] = String.valueOf((float) 1000 / player.getMilliSecondsPerSample());
        info[5][0] = ElanLocale.getString("Player.Framework");
        info[5][1] = player.getFrameworkDescription();

        return info;
    }

    private void showMessage(ElanMediaPlayer player, String[][] mes) {
        if (mes == null) {
            return;
        }

        label = new JLabel();

        StringBuilder sb = new StringBuilder();
        sb.append("<html><table>");

        for (int i = 0; i < mes.length; i++) {
            sb.append("<tr>");

            for (int j = 0; j < mes[i].length; j++) {
                sb.append("<td>");
                sb.append(mes[i][j]);
                sb.append("</td>");
            }

            sb.append("</tr>");
        }

        sb.append("</table></html>");
        label.setText(sb.toString());
        Component cc = null;
        
        if (player != null) {
        	cc = player.getVisualComponent();
        }
        if (cc != null) {
	        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(cc), label, "ELAN",
	            JOptionPane.INFORMATION_MESSAGE);
        } else {
	        JOptionPane.showMessageDialog(null, label, "ELAN",
		            JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
