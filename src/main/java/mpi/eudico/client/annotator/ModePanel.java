package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.util.TimeFormatter;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * A panel with two check boxes for selection mode and loop mode
 * 
 * @version May 2017 Changed the font settings for the components to only use
 * the size of the global small font, without changing the font (name) itself 
 */
@SuppressWarnings("serial")
public class ModePanel extends JPanel implements ElanLocaleListener {
    private ElanMediaPlayerController mediaPlayerController;
    private ViewerManager2 vm;
    private JCheckBox chkLoopMode;
    private JCheckBox chkSelectionMode;

    //next variables are used for localization of input boxes
    private String STRTIMEBETWEENLOOPSINPUTBOX1 = "";
    private String STRTIMEBETWEENLOOPSINPUTBOX2 = "";
    private String STRTIMEBETWEENLOOPSINPUTBOX3 = "";
    private String STRTIMEBETWEENLOOPSINPUTBOX4 = "";
    private String STRTIMEBETWEENLOOPSINPUTBOX5 = "";
    private String STRTIMEBETWEENLOOPSINPUTBOX6 = "";

    /**
     * Creates a new ModePanel instance
     *
     * @param theVM DOCUMENT ME!
     * @param theMPC DOCUMENT ME!
     */
    public ModePanel(ViewerManager2 theVM, ElanMediaPlayerController theMPC) {
        vm = theVM;
        mediaPlayerController = theMPC;

        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        chkSelectionMode = new JCheckBox(ELANCommandFactory.getCommandAction(
                    vm.getTranscription(), ELANCommandFactory.SELECTION_MODE));
        chkSelectionMode.setFont(Constants.deriveSmallFont(chkSelectionMode.getFont()));
        add(chkSelectionMode);

        JLabel jlabeltemp = new JLabel("  ");
        add(jlabeltemp);

        chkLoopMode = new JCheckBox(ELANCommandFactory.getCommandAction(
                    vm.getTranscription(), ELANCommandFactory.LOOP_MODE));
        chkLoopMode.setFont(Constants.deriveSmallFont(chkLoopMode.getFont()));
        add(chkLoopMode);

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        updateLocale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param onOff DOCUMENT ME!
     */
    public void updateLoopMode(boolean onOff) {
        chkLoopMode.setSelected(onOff);
    }

    /**
     * DOCUMENT ME!
     *
     * @param onOff DOCUMENT ME!
     */
    public void updateSelectionMode(boolean onOff) {
    	    if (chkSelectionMode.isSelected() != onOff) {
            chkSelectionMode.setSelected(onOff);
    	    }
    }

    /**
     * Input box for setting the pause time between 2 loops when playing a
     * selection in loop mode
     */
    public void showTimeBetweenLoopsInputBox() {
        String strNewTime;
        boolean bAgain = true;

        while (bAgain == true) {
            double curTimeInSeconds = ((double) (double) mediaPlayerController.getUserTimeBetweenLoops() / (double) 1000);
            strNewTime = JOptionPane.showInputDialog(this,
                    STRTIMEBETWEENLOOPSINPUTBOX1 + " " + curTimeInSeconds +
                    "\n" + STRTIMEBETWEENLOOPSINPUTBOX2,
                    STRTIMEBETWEENLOOPSINPUTBOX3, JOptionPane.PLAIN_MESSAGE);

            if ((strNewTime != null) && (!strNewTime.equals(""))) {
                long lngSeconds = TimeFormatter.toMilliSeconds(strNewTime);

                if (lngSeconds >= 0.0) {
                    mediaPlayerController.setUserTimeBetweenLoops(lngSeconds);
                    bAgain = false;
                } else {
                    JOptionPane.showMessageDialog(this,
                        STRTIMEBETWEENLOOPSINPUTBOX4 + "\n" +
                        STRTIMEBETWEENLOOPSINPUTBOX5,
                        STRTIMEBETWEENLOOPSINPUTBOX6, JOptionPane.ERROR_MESSAGE);
                    bAgain = true;
                }
            }

            //cancel is clicked
            if (strNewTime == null) {
                break;
            }
        }
    }
    
    /**
     * Method to set one of the components to (in)visible.
     * @param mode id of the mode
     * @param visible the visibility flag
     */
    public void setModeVisible(String mode, boolean visible) {
    	if (ELANCommandFactory.SELECTION_MODE.equals(mode)) {
    		chkSelectionMode.setVisible(visible);
    	} else if (ELANCommandFactory.LOOP_MODE.equals(mode)) {
    		chkLoopMode.setVisible(visible);
    	}
    }

    /**
     */
    @Override
	public void updateLocale() {
        STRTIMEBETWEENLOOPSINPUTBOX1 = ElanLocale.getString(
                "MediaPlayerControlPanel.STRTIMEBETWEENLOOPSINPUTBOX1");
        STRTIMEBETWEENLOOPSINPUTBOX2 = ElanLocale.getString(
                "MediaPlayerControlPanel.STRTIMEBETWEENLOOPSINPUTBOX2");
        STRTIMEBETWEENLOOPSINPUTBOX3 = ElanLocale.getString(
                "MediaPlayerControlPanel.STRTIMEBETWEENLOOPSINPUTBOX3");
        STRTIMEBETWEENLOOPSINPUTBOX4 = ElanLocale.getString(
                "MediaPlayerControlPanel.STRTIMEBETWEENLOOPSINPUTBOX4");
        STRTIMEBETWEENLOOPSINPUTBOX5 = ElanLocale.getString(
                "MediaPlayerControlPanel.STRTIMEBETWEENLOOPSINPUTBOX5");
    }
}
