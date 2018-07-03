package mpi.eudico.client.annotator.gui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

import mpi.eudico.client.annotator.ElanLocale;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.util.TimeFormatter;

/**
 * A dialog for entering (typing) start and/or end time of an existing annotation.
 * The current time format used is hh:mm:ss:mss.
 * This dialog could maybe also be used for new annotations?
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TimeIntervalEditDialog extends ClosableDialog implements ActionListener {
	private JLabel messageLabel;
	private JLabel limitsLabel;
	private JButton okButton;
	private JButton cancelButton;
	private JFormattedTextField btTextField;
	private JFormattedTextField etTextField;
	
	private long beginTime = -1L;
	private long endTime   = -1L;
	
	private long minBeginTime = 0L;
	private long maxEndTime   = Long.MAX_VALUE;

	/**
	 * Constructors of the super classes.
	 * @throws HeadlessException
	 */
	public TimeIntervalEditDialog() throws HeadlessException {
		this((Frame) null, true);
	}

	public TimeIntervalEditDialog(Frame owner) throws HeadlessException {
		this(owner, null, true);
	}

	public TimeIntervalEditDialog(Frame owner, boolean modal)
			throws HeadlessException {
		this(owner, null, modal);
	}

	public TimeIntervalEditDialog(Frame owner, String title)
			throws HeadlessException {
		this(owner, title, true);
	}

	public TimeIntervalEditDialog(Frame owner, String title, boolean modal)
			throws HeadlessException {
		this(owner, title, modal, null);
	}

	public TimeIntervalEditDialog(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		initComponents();
	}

	public TimeIntervalEditDialog(Dialog owner) throws HeadlessException {
		this(owner, null, true);
	}

	public TimeIntervalEditDialog(Dialog owner, boolean modal)
			throws HeadlessException {
		this(owner, null, modal);
	}

	public TimeIntervalEditDialog(Dialog owner, String title)
			throws HeadlessException {
		this(owner, title, true, null);
	}

	public TimeIntervalEditDialog(Dialog owner, String title, boolean modal)
			throws HeadlessException {
		this(owner, title, modal, null);
	}

	public TimeIntervalEditDialog(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc) throws HeadlessException {
		super(owner, title, modal, gc);
		initComponents();
	}
	
	/**
	 * Sets the initial values of start and end time in case of modifying an existing
	 * annotation, otherwise both fields are initialized with 0's.
	 * 
	 * @param begin the current begin time 
	 * @param end the current end time
	 */
	public void setInterval(long begin, long end) {
		beginTime = begin;
		endTime = end;
		btTextField.setValue(TimeFormatter.toString(beginTime));
		etTextField.setValue(TimeFormatter.toString(endTime));
	}
	
	/**
	 * The limits are provided as a visible suggestion but are not enforced.
	 * In case of an annotation on a top level tier the limits will be 0 - media duration,
	 * in case of a dependent annotation, the parent's boundaries will serve as limits. 
	 * 
	 * @param minBT the minimal value for the begin time
	 * @param maxET the maximal value for the end time
	 */
	public void setLimits(long minBT, long maxET) {
		minBeginTime = minBT;
		maxEndTime = maxET;
		// update label
		limitsLabel.setText(String.format(ElanLocale.getString("ModifyTimesDialog.EnterTimes.Range"), 
				TimeFormatter.toString(minBeginTime), TimeFormatter.toString(maxEndTime)));
		pack();
	}
	
	/**
	 * @return the entered begin and end time as an long array of size 2 
	 */
	public long[] getValue() {
		return new long[]{beginTime, endTime};
	}
	
	/**
	 * @return the entered begin time, currently it is not prevented that start >= end 
	 */
	public long getBeginTime() {
		return beginTime;
	}
	
	/**
	 * 
	 * @return the entered end time, currently it is not prevented that end <= start
	 */
	public long getEndTime() {
		return endTime;
	}

	private void initComponents() {
		getContentPane().setLayout(new GridBagLayout());
		messageLabel = new JLabel(ElanLocale.getString("ModifyTimesDialog.EnterTimes"), SwingConstants.CENTER);
		limitsLabel = new JLabel("", SwingConstants.CENTER);
		JLabel btLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"), SwingConstants.TRAILING);
		JLabel etLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"), SwingConstants.TRAILING);
		
		HhMmSsMssMaskFormatter timeFormatter = new HhMmSsMssMaskFormatter();		
		btTextField = new JFormattedTextField(timeFormatter);
		etTextField = new JFormattedTextField(timeFormatter);
		
		okButton = new JButton(ElanLocale.getString("Button.OK"));
		cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
		//buttonPanel.add(okButton);
		//buttonPanel.add(cancelButton);
		okButton.addActionListener(null);
		cancelButton.addActionListener(null);
		
		
		Container cp = getContentPane();
		((JComponent) cp).setBorder(new EmptyBorder(6, 8, 2, 8));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets (4, 4, 4, 4);
		cp.add(messageLabel, gbc);
	
		gbc.gridy = 1;
//		gbc.insets = new Insets (4, 4, 4, 4);
		cp.add(limitsLabel, gbc);	
		
		gbc.gridy = 2;
		//gbc.fill = GridBagConstraints.NONE;
		//gbc.weightx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
//		gbc.insets = new Insets (4, 4, 4, 4);
		cp.add(btLabel, gbc);	
		
		gbc.gridy = 3;
		cp.add(etLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		cp.add(btTextField, gbc);
		gbc.gridy = 3;
		cp.add(etTextField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
//		gbc.fill = GridBagConstraints.NONE;
//		gbc.weightx = 0;
		gbc.insets = new Insets (12, 4, 4, 4);
		gbc.anchor = GridBagConstraints.EAST;
		cp.add(okButton, gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		cp.add(cancelButton, gbc);
		
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		pack();
		setLocationRelativeTo(getParent());
		//getRootPane().setDefaultButton(okButton);
	}

	/**
	 * The buttons action event handling. 
	 * "Apply" converts the current time value to long values.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			// ignore values
			setVisible(false);
			dispose();
		} else if (e.getSource() == okButton) {
			beginTime = TimeFormatter.toMilliSeconds(
					(String) btTextField.getValue());
			endTime = TimeFormatter.toMilliSeconds(
					(String) etTextField.getValue());
			
			if (beginTime >= endTime) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info(String.format("The entered begin time (%d) is >= the end time (%d).", beginTime, endTime));
				}
			}
			setVisible(false);
			dispose();
		}
	}
	
	/**
	 * An extension of the MaskFormatter which prevents the first digit of
	 * the minutes number and of the seconds number to be > 5.
	 * This is only applied when committing the entered time value; implementation
	 * of correction at input time would require a rewrite of this formatter 
	 * (or another one) because most fields and methods are (package) private.
	 */
	private class HhMmSsMssMaskFormatter extends MaskFormatter {
		private DecimalFormat twoDigits = new DecimalFormat("00");
		
		public HhMmSsMssMaskFormatter() {
			super();
			try {
				setMask("##:##:##.###");
				setPlaceholderCharacter('0');
			} catch (ParseException pe) {
				// should never happen with this fixed mask
			}
		}
		
		/**
		 * @param ch the char to test
		 * @return true if the char is a digit between 0 and 5, inclusive,
		 * false otherwise
		 */
		private boolean isValidMSChar(char ch) {
			return (ch == '0' || ch == '1' || 
					ch == '2' || ch == '3' || 
					ch == '4' || ch == '5');
		}
		
		/**
		 * Tests the first digit of the minutes and seconds positions in the
		 * time string, after first calling the test of the super class.
		 * In case of values > 5 (>59) the seconds, minutes and/or hour
		 * fields are recalculated.
		 * 
		 * @param value the String to test
		 * @return the updated input string in case of seconds or minutes values > 59, 
		 * 		otherwise the input string is returned
		 * @throws ParseException if the specified digits are invalid 
		 */
		@Override
		public Object stringToValue(String value) throws ParseException {
			Object temp = super.stringToValue(value);
			if (temp instanceof String) {
				String s = (String) temp;
				// the super's check already verified the length of the string
				if (!isValidMSChar(s.charAt(6))) {
					try {
						int isec = Integer.valueOf(s.substring(6, 7));
						int m = Integer.valueOf(s.substring(3, 5));
						isec -= 6;// decrease with 60 seconds
						m++;// increase one minute
						s = s.substring(0, 3) + twoDigits.format(m) + s.substring(5, 6) + 
								String.valueOf(isec) + s.substring(7);
					} catch (NumberFormatException nfe) {
						throw new ParseException("Invalid seconds value", 6);
					}
					// throw new ParseException("Invalid seconds value", 6);
				}
				
				if (!isValidMSChar(s.charAt(3))) {
					try {
						int imin = Integer.valueOf(s.substring(3, 4));
						int h = Integer.valueOf(s.substring(0, 2));
						imin -= 6;// decrease with 60 minutes
						h++;// increase one hour
						s = twoDigits.format(h) + s.substring(2, 3) + 
								String.valueOf(imin) + s.substring(4);
					} catch (NumberFormatException nfe) {
						throw new ParseException("Invalid minute value", 3);
					}				
					//throw new ParseException("Invalid minute value", 3);
				}
				temp = s;
			}
			return temp;
		}
	}
}
