package mpi.eudico.client.annotator;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JWindow;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.PlayStepAndRepeatCA;

/**
 * A panel with ui elements for the step-and-repeat play back mode, a.k.a walker mode.
 * A button gives access to a configuration popup which allows to set:
 * - the duration of the interval
 * - the duration of the pause between two runs of an interval
 * - the number of times each interval has to be played
 * - the number of ms with which to shift the interval 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class StepAndRepeatPanel extends JPanel implements ElanLocaleListener, ActionListener {
	private ViewerManager2 vm;
	
	private JCheckBox enableCB;
	private JButton playPauseButton;
	private JButton configureButton;
	private PlayStepAndRepeatCA playAction;
	
	// fields for configuration settings
	private long beginTime = -1;// results in current media time as begin time
	private long endTime = -1;// results in media duration as end time
	private int intervalDuration = 2000;
	private int pauseDuration = 500;
	private int numRepeats = 3;
	private int stepSize = 1000;
	private boolean enabled = true;
	/**
	 * @param vm
	 */
	public StepAndRepeatPanel(ViewerManager2 vm) {
		super();
		this.vm = vm;
		readPreferences();
		initComponents();
	}
	
	/**
	 * The panel contains a checkbox for activating step-and-repeat mode (?)
	 * a play/pause button and a "configure" icon/button.
	 */
	private void initComponents() {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);
        
		enableCB = new JCheckBox();
		enableCB.setSelected(enabled);
		enableCB.addActionListener(this);
		// add listener
		add(enableCB);
		// to action eventually
		Icon icon = null;
		String text = null;
		
		playAction = (PlayStepAndRepeatCA) ELANCommandFactory.getCommandAction(vm.getTranscription(), 
				ELANCommandFactory.PLAY_STEP_AND_REPEAT);
		playPauseButton = new JButton(playAction);
		playPauseButton.setPreferredSize(new Dimension(30, 20));
		playPauseButton.setEnabled(enabled);
		add(playPauseButton);
		
		// load the icon for the configure button
		text = null;
		icon = null;
		try {
			//icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif"));
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Configure16.gif"));
		} catch (Exception ex) {// any
			text = "E";
		}
		configureButton = new JButton(text, icon);// create with icon
		configureButton.setPreferredSize(new Dimension(20, 20));
		configureButton.setBorderPainted(false);
		configureButton.setEnabled(enabled);
		configureButton.addActionListener(this);
		add(configureButton);
		updateLocale();
		readPreferences();
	}

	@Override
	public void updateLocale() {
		enableCB.setToolTipText(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.Enable"));
		configureButton.setToolTipText(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.Configure"));
	}

	private void readPreferences() {
		Integer intPref = Preferences.getInt("StepAndRepeat.IntervalDuration", null);
		if (intPref != null) {
			intervalDuration = intPref;
		}
		intPref = Preferences.getInt("StepAndRepeat.PauseDuration", null);
		if (intPref != null) {
			pauseDuration = intPref;
		}
		intPref = Preferences.getInt("StepAndRepeat.NumberOfRepeats", null);
		if (intPref != null) {
			numRepeats = intPref;
		}
		intPref = Preferences.getInt("StepAndRepeat.StepSize", null);
		if (intPref != null) {
			stepSize = intPref;
		}
	}
	
	/**
	 * Changes the icon of the action in a play or pause icon.
	 * @param play if true the play icon is set (indicating the paused state
	 */
	public void setPlayIcon(boolean play) {
		if (playAction != null) {
			playAction.setPlayIcon(play);
		}
	}
	
	/**
	 * 
	 * @param showEnableCheckBox
	 */
	public void showEnableCheckBox(boolean showEnableCheckBox) {
		enableCB.setVisible(showEnableCheckBox);
		revalidate();
	}
	
	public int getIntervalDuration() {
		return intervalDuration;
	}

	public void setIntervalDuration(int intervalDuration) {
		this.intervalDuration = intervalDuration;
		//vm.getMediaPlayerController().setStepAndRepeatMode(true);
		Preferences.set("StepAndRepeat.IntervalDuration", intervalDuration, null, false, false);
	}

	public int getPauseDuration() {
		return pauseDuration;
	}

	public void setPauseDuration(int pauseDuration) {
		this.pauseDuration = pauseDuration;
		Preferences.set("StepAndRepeat.PauseDuration", pauseDuration, null, false, false);
	}

	public int getNumRepeats() {
		return numRepeats;
	}

	public void setNumRepeats(int numRepeats) {
		this.numRepeats = numRepeats;
		Preferences.set("StepAndRepeat.NumberOfRepeats", numRepeats, null, false, false);
	}

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
		Preferences.set("StepAndRepeat.StepSize", stepSize, null, false, false);
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == configureButton) {
			// show config window
			ConfigWindow cw = new ConfigWindow(SwingUtilities.getWindowAncestor(this));
			cw.pack();
			Dimension dim = cw.getPreferredSize();
			Point p = configureButton.getLocationOnScreen();
			cw.setBounds(p.x - dim.width, p.y, dim.width, dim.height);
			cw.setVisible(true);
		} else if (e.getSource() == enableCB) {
			playPauseButton.setEnabled(enableCB.isSelected());
			configureButton.setEnabled(enableCB.isSelected());
		}
		
	}
	
	
	/**
	 * Popup window for configuring the step and repeat mode. Shows four elements;
	 * a close button, fields for changing the interval duration, for setting the 
	 * number of times each interval should be played and the size of the step, i.e.
	 * the number of ms. to shift the interval forward.
	 * 
	 * @author Han Sloetjes
	 */
	private class ConfigWindow extends JWindow implements ActionListener, ChangeListener {
		private JPanel compPanel;
		private JButton closeButton;
		private JLabel durLabel;
		private JLabel repeatLabel;
		private JLabel pauseLabel;
		private JLabel stepLabel;
		private JSpinner durSpinner;
		private JSpinner repeatSpinner;
		private JSpinner pauseSpinner;
		private JSpinner stepSpinner;
		
		/**
		 * @param owner
		 */
		public ConfigWindow(Window owner) {
			super(owner);
			initComponents();
		}
		
		private void initComponents() {
			Icon icon = null;
			String text = null;
			try {
				icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Close16.gif"));
			} catch (Exception ex) {// any
				text = "X";
			}
			compPanel = new JPanel(new GridBagLayout());
			compPanel.setBorder(new CompoundBorder(new LineBorder(Constants.SHAREDCOLOR6, 1), 
					new EmptyBorder(2, 4, 2, 2)));

			closeButton = new JButton(text, icon);// load icon...
			closeButton.setToolTipText(ElanLocale.getString("Button.Close"));
			closeButton.setBorderPainted(false);
			closeButton.setPreferredSize(new Dimension(16, 16));
			closeButton.addActionListener(this);
			
			durLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.IntervalDuration"));
			Font smallFont = durLabel.getFont().deriveFont(Font.PLAIN, 10f);
			durLabel.setFont(smallFont);
			repeatLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.Repeats"));
			repeatLabel.setFont(smallFont);
			pauseLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.PauseDuration"));
			pauseLabel.setFont(smallFont);
			stepLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.StepAndRepeat.StepSize"));
			stepLabel.setFont(smallFont);
			durSpinner = new JSpinner(new SpinnerNumberModel(intervalDuration, 200, 10000, 200));
			durSpinner.setEditor(new JSpinner.NumberEditor(durSpinner, "#"));
			durSpinner.addChangeListener(this);
			repeatSpinner = new JSpinner(new SpinnerNumberModel(numRepeats, 1, 10, 1));
			repeatSpinner.setEditor(new JSpinner.NumberEditor(repeatSpinner, "#"));
			pauseSpinner = new JSpinner(new SpinnerNumberModel(pauseDuration, 0, 4000, 100));
			pauseSpinner.setEditor(new JSpinner.NumberEditor(pauseSpinner, "#"));
			stepSpinner = new JSpinner(new SpinnerNumberModel(stepSize, 200, 10000, 200));
			stepSpinner.setEditor(new JSpinner.NumberEditor(stepSpinner, "#"));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridwidth = 2;
			compPanel.add(closeButton, gbc);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridwidth = 1;
			gbc.gridy = 1;
			compPanel.add(durLabel, gbc);
			gbc.gridy++;
			compPanel.add(repeatLabel, gbc);
			gbc.gridy++;
			compPanel.add(pauseLabel, gbc);
			gbc.gridy++;
			compPanel.add(stepLabel, gbc);
			gbc.gridy = 1;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			compPanel.add(durSpinner, gbc);
			gbc.gridy++;
			compPanel.add(repeatSpinner, gbc);
			gbc.gridy++;
			compPanel.add(pauseSpinner, gbc);
			gbc.gridy++;
			compPanel.add(stepSpinner, gbc);
			
			// add listeners to all spinners
			add(compPanel);
		}

		/**
		 * Button action events.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == closeButton) {
				close();
			}			
		}

		/**
		 * Spinner change events.
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e. getSource() == durSpinner) {
				Object val = durSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setIntervalDuration((Integer) val);
				}
			} else if (e.getSource() == repeatSpinner) {
				Object val = repeatSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setNumRepeats((Integer) val);
				}
			} else if (e.getSource() == pauseSpinner) {
				Object val = pauseSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setPauseDuration((Integer) val);
				}
			} else if (e.getSource() == stepSpinner) {
				Object val = stepSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setStepSize((Integer) val);
				}
			}			
		}
		
		/**
		 * Commits edits, sets the values and closes the window.
		 */
		private void close() {
			// apply values?
			try {
				durSpinner.commitEdit();
				Object val = durSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setIntervalDuration((Integer) val);
				}
			} catch (ParseException pe) {
				// ignore
			}
			try {
				repeatSpinner.commitEdit();
				Object val = repeatSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setNumRepeats((Integer) val);
				}
			} catch (ParseException pe) {
				// ignore
			}
			try {
				pauseSpinner.commitEdit();
				Object val = pauseSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setPauseDuration((Integer) val);
				}
			} catch (ParseException pe) {
				// ignore
			}
			try {
				stepSpinner.commitEdit();
				Object val = stepSpinner.getValue();
				if (val instanceof Integer) {
					StepAndRepeatPanel.this.setStepSize((Integer) val);
				}
			} catch (ParseException pe) {
				// ignore
			}
			setVisible(false);
			dispose();
		}

	}
}
