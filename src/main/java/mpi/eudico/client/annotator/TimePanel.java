package mpi.eudico.client.annotator;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.util.TimeFormatter;


/**
 * A panel with a label showing the current time of a media player.
 * 
 * @version May 2017 changed the font setting for the time label, 
 *   only the size is now based on the default font setting
 */
@SuppressWarnings("serial")
public class TimePanel extends AbstractViewer implements ActionListener,
	PreferencesUser {
    private JLabel timeLabel;

    // popup for time format
    private JPopupMenu popup;
    private ButtonGroup formatBG;
    private JMenu formatMenu;
    private JRadioButtonMenuItem hhmmssmsMI;
    private JRadioButtonMenuItem timecodePalMI;
    private JRadioButtonMenuItem timecodePal50MI;
    private JRadioButtonMenuItem timecodeNtscMI;
    private JRadioButtonMenuItem msMI;
    private JRadioButtonMenuItem secMI;
    
    private final int TC = 0;
    private final int TC_PAL = 1;
    private final int TC_NTSC = 2;
    private final int MS = 3;
    private final int SEC = 4;
    private final int TC_PAL_50 = 5;
    private int mode = TC;
    
    /**
     * Creates a new TimePanel instance
     */
    public TimePanel() {
        setLayout(new BorderLayout());

        timeLabel = new JLabel("00:00:00.000");
        timeLabel.setFont(Constants.deriveSmallFont(timeLabel.getFont()));
        		
        addMouseListener(new TimeLabelMouseHandler());

        add(timeLabel, BorderLayout.CENTER);

        updateLocale();

        setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            updateLabel();
        }
    }
    
    /**
     * Update the time label.
     */
    public void updateLabel() {
        switch (mode) {
        case MS:
        	timeLabel.setText(String.valueOf(getMediaTime()));
        	break;
        case TC_PAL:    
            timeLabel.setText(TimeFormatter.toTimecodePAL(getMediaTime()));
            break;
        case TC_NTSC:
            timeLabel.setText(TimeFormatter.toTimecodeNTSC(getMediaTime()));
            break;
        case SEC:
        	timeLabel.setText(TimeFormatter.toSSMSString(getMediaTime()));
        	break;
        case TC_PAL_50:
        	timeLabel.setText(TimeFormatter.toTimecodePAL50(getMediaTime()));
        	break;
        default:
            timeLabel.setText(TimeFormatter.toString(getMediaTime()));
        }
        
        repaint();        
    }

    /**
     * Input box for setting the time where the crosshair should jump to
     */
    public void showCrosshairTimeInputBox() {
        String strNewTime;
        boolean bAgain = true;

        while (bAgain == true) {
            strNewTime = JOptionPane.showInputDialog(this,
            		ElanLocale.getString(
                        "MediaPlayerControlPanel.MediaTimeInputBoxNewTime"), 
                    ElanLocale.getString(
                        "MediaPlayerControlPanel.MediaTimeInputBoxTitle"),
                    JOptionPane.PLAIN_MESSAGE);

            if ((strNewTime != null) && (!strNewTime.equals(""))) {
                long lngSeconds = TimeFormatter.toMilliSeconds(strNewTime);

                if (lngSeconds >= 0.0) {
                    setMediaTime(lngSeconds);
                    bAgain = false;
                } else {
                	String inputFormatMessage = ElanLocale.getString("TimeCodeFormat.TimeCode");
                	
                	try {
                		inputFormatMessage = String.format(                	
                			ElanLocale.getString("MediaPlayerControlPanel.MediaTimeInputBoxFormats"), 
                			ElanLocale.getString("TimeCodeFormat.TimeCode"), ElanLocale.getString("TimeCodeFormat.Seconds"),
                			ElanLocale.getString("TimeCodeFormat.MilliSec"));
                	} catch (Throwable t){/*ignore*/}
                	
                    JOptionPane.showMessageDialog(this,
                    	ElanLocale.getString(
                            "MediaPlayerControlPanel.MediaTimeInputBoxFormatError1") + "\n" +
                    	String.format(ElanLocale.getString(
                            "MediaPlayerControlPanel.MediaTimeInputBoxFormatError2"), inputFormatMessage),
                        ElanLocale.getString(
                            "MediaPlayerControlPanel.MediaTimeInputBoxErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    bAgain = true;
                }
            }

            //cancel is clicked
            if (strNewTime == null) {
                break;
            }
        }
    }

    private void createPopupMenu() {
        popup = new JPopupMenu();
        formatBG = new ButtonGroup();
        formatMenu = new JMenu();
        hhmmssmsMI = new JRadioButtonMenuItem();
        timecodePalMI = new JRadioButtonMenuItem();
        timecodePal50MI = new JRadioButtonMenuItem();
        timecodeNtscMI = new JRadioButtonMenuItem();
        msMI = new JRadioButtonMenuItem();
        secMI = new JRadioButtonMenuItem();
        formatBG.add(hhmmssmsMI);
        formatBG.add(timecodePalMI);
        formatBG.add(timecodePal50MI);
        formatBG.add(timecodeNtscMI);
        formatBG.add(secMI);
        formatBG.add(msMI);
        hhmmssmsMI.setSelected(mode == TC);
        timecodePalMI.setSelected(mode == TC_PAL);
        timecodePal50MI.setSelected(mode == TC_PAL_50);
        timecodeNtscMI.setSelected(mode == TC_NTSC);
        secMI.setSelected(mode == SEC);
        msMI.setSelected(mode == MS);
        hhmmssmsMI.addActionListener(this);
        timecodePalMI.addActionListener(this);
        timecodePal50MI.addActionListener(this);
        timecodeNtscMI.addActionListener(this);
        secMI.addActionListener(this);
        msMI.addActionListener(this);
        popup.add(formatMenu);
        formatMenu.add(hhmmssmsMI);
        formatMenu.add(timecodePalMI);
        formatMenu.add(timecodePal50MI);
        formatMenu.add(timecodeNtscMI);
        formatMenu.add(secMI);
        formatMenu.add(msMI);
        updateLocale();
    }
    
    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale() {
        if (popup != null) {
            formatMenu.setText(ElanLocale.getString("TimeCodeFormat.Label.TimeFormat"));
            hhmmssmsMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode"));
            timecodePalMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL"));
            timecodePal50MI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50"));
            timecodeNtscMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC"));
            msMI.setText(ElanLocale.getString("TimeCodeFormat.MilliSec"));
            secMI.setText(ElanLocale.getString("TimeCodeFormat.Seconds"));
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateSelection() {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (timecodePalMI.isSelected()) {
            mode = TC_PAL;
            setPreference("TimePanel.TimeFormat", Constants.PAL_STRING, 
            		getViewerManager().getTranscription());
        } else if (timecodePal50MI.isSelected()) {
            mode = TC_PAL_50;
            setPreference("TimePanel.TimeFormat", Constants.PAL_50_STRING, 
            		getViewerManager().getTranscription());
        } else if (timecodeNtscMI.isSelected()) {
            mode = TC_NTSC;
            setPreference("TimePanel.TimeFormat", Constants.NTSC_STRING, 
            		getViewerManager().getTranscription());
        } else if (msMI.isSelected()) {
            mode = MS;
            setPreference("TimePanel.TimeFormat", Constants.MS_STRING, 
            		getViewerManager().getTranscription());
        } else if (secMI.isSelected()) {
            mode = SEC;
            setPreference("TimePanel.TimeFormat", Constants.SSMS_STRING, 
            		getViewerManager().getTranscription());
        } else {
            mode = TC;
            setPreference("TimePanel.TimeFormat", Constants.HHMMSSMS_STRING, 
            		getViewerManager().getTranscription());
        }
        updateLabel();
    }
    
	@Override
	public void preferencesChanged() {		
		String timeformat = Preferences.getString("TimePanel.TimeFormat", 
				getViewerManager().getTranscription());
		if (timeformat != null) {
			if (timeformat.equals(Constants.HHMMSSMS_STRING)) {
				mode = TC;
			} else if (timeformat.equals(Constants.PAL_STRING)) {
				mode = TC_PAL;
			} else if (timeformat.equals(Constants.PAL_50_STRING)) {
				mode = TC_PAL_50;
			} else if (timeformat.equals(Constants.NTSC_STRING)) {
				mode = TC_NTSC;
			} else if (timeformat.equals(Constants.MS_STRING)) {
				mode = MS;
			} else if (timeformat.equals(Constants.SSMS_STRING)) {
				mode = SEC;
			}
			updateLabel();
		}
	}
    
    /**
     * Handles a mouse click on the time label
     */
    private class TimeLabelMouseHandler extends MouseAdapter {
        /**
         * The user clicked on the time label so bring up an input box
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mouseClicked(MouseEvent e) {
            // HS nov 04: e.isPopupTrigger always returns false on my PC...
        	// test on Windows.. why different, compared to TimeLineViewer?
        	if (SwingUtilities.isRightMouseButton(e) || 
        			(e.getButton() == MouseEvent.BUTTON1 && (!e.isMetaDown() && e.isControlDown()))
            		|| e.isPopupTrigger()) {
            	return;
            }
            showCrosshairTimeInputBox();
        }
        
        @Override
		public void mousePressed(MouseEvent e) {
            Point pp = e.getPoint();

            // HS nov 04: e.isPopupTrigger always returns false on my PC...
            if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown()))
            		|| e.isPopupTrigger()) {
                if (popup == null) {
                    createPopupMenu();
                }
                popup.show(TimePanel.this, pp.x, pp.y);
            }
        }

    }
     // end of TimeLabelMouseHandler

}
