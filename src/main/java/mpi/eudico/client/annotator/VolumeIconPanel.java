package mpi.eudico.client.annotator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A panel with a volume button that, when clicked, shows a little popup window
 * with a volume slider. 
 * Note: a label could be added, showing the current volume value.
 *  
 * @author Han Sloetjes
 */
public class VolumeIconPanel extends JPanel implements ActionListener {
	private ViewerManager2 vm;
	private JButton volButton;
	private int orientation = SwingConstants.HORIZONTAL;
	//private JLabel volLabel;// could add a label showing the volume
	private int size1 = 200;
	private int size2 = 24;
	private Dimension prefSize;
	
	/**
	 * Constructor
	 * Defaults to a horizontal popup window.
	 * @param vm the viewermanager
	 */
	public VolumeIconPanel(ViewerManager2 vm) {
		this(vm, SwingConstants.HORIZONTAL);
	}
	
	/**
	 * Constructor.
	 * @param vm the viewer manager
	 * @param orientation either SwingConstants.HORIZONTAL or SwingConstants.VERTICAL
	 */
	public VolumeIconPanel(ViewerManager2 vm, int orientation) {
		super();
		this.vm = vm;
		this.orientation = orientation;
		initComponents();
	}
	
	/**
	 * Constructor.
	 * @param vm the viewer manager
	 * @param orientation either SwingConstants.HORIZONTAL or SwingConstants.VERTICAL
	 * @param buttonSize the preferred size for the button/panel
	 */
	public VolumeIconPanel(ViewerManager2 vm, int orientation, Dimension buttonSize) {
		super();
		this.vm = vm;
		this.orientation = orientation;
		initComponents();
		prefSize = buttonSize;
	}
	
	/**
	 * Allows to set the width and height of the popup window.
	 * 
	 * @param width the new width
	 * @param height the new height 
	 */
	public void setPopupSize(int width, int height) {
		size1 = width;
		size2 = height;
	}
	
	/**
	 * Creates the button and adds it to the panel.
	 */
	private void initComponents() {
		setLayout(new GridLayout(1, 1));
		Icon icon = null;
		String text = null;
		try {
			icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/media/Volume16.gif"));
		} catch (Exception ex) {// any
			text = "V";
		}
		volButton = new JButton(text, icon);
		volButton.setBorderPainted(false);
		volButton.setPreferredSize(new Dimension (24, 24));
		volButton.setAlignmentY(0.5f);
		volButton.setAlignmentX(0.5f);
		volButton.addActionListener(this);
		add(volButton);
	}

	/**
	 * Creates the popup window.
	 * Note: maybe a check should be built in whether the window is fully on screen.
	 * 
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		VolumeWindow vw = new VolumeWindow(SwingUtilities.getWindowAncestor(volButton));
		Point p = this.getLocationOnScreen();
		int w, h;
		if (orientation == SwingConstants.HORIZONTAL) {
			w = size1;
			h = size2;
			vw.setBounds(p.x + this.getWidth(), p.y, w, h);
		} else {
			h = size1;
			w = size2;
			vw.setBounds(p.x, p.y + this.getHeight(), w, h);
		}
		
		vw.setVisible(true);
	}	
	
	/**
	 * Returns the preferred size of the panel.
	 */
	@Override
	public Dimension getPreferredSize() {
		if (prefSize != null) {
			return prefSize;
		}
		
		return super.getPreferredSize();
	}
	
	/**
	 * Sets the preferred size.
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		this.prefSize = preferredSize;
		
		super.setPreferredSize(preferredSize);
	}

	/**
	 * A JWindow that shows a slider and a label for the value.
	 * 
	 * @author Han Sloetjes
	 */
	class VolumeWindow extends JWindow implements FocusListener, ChangeListener, MouseListener {
		private JPanel compPanel; 
		private JSlider slider;
		private JLabel volLabel;
		
		/**
		 * 
		 */
		public VolumeWindow(Window parent) {
			super(parent);
			initComponents();
		}
		
		private void initComponents() {
			compPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			float volume = 100f;
			volume = 100 * vm.getVolumeManager().getMasterVolume();
			volLabel = new JLabel(String.valueOf((int) volume));
			volLabel.setFont(volLabel.getFont().deriveFont(Font.PLAIN, 10f));
			volLabel.setHorizontalAlignment(SwingConstants.CENTER);
			int tw = volLabel.getFontMetrics(volLabel.getFont()).stringWidth("000");
			volLabel.setPreferredSize(new Dimension(tw, volLabel.getPreferredSize().height));
			volLabel.setMinimumSize(new Dimension(tw, volLabel.getPreferredSize().height));
			compPanel.add(volLabel, gbc);
			
			if (orientation == SwingConstants.HORIZONTAL) {
				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;				
			} else {
				gbc.gridy = 1;
				gbc.fill = GridBagConstraints.VERTICAL;
				gbc.weighty = 1.0;
			}
			
			slider = new JSlider(VolumeIconPanel.this.orientation, 0, 100, (int) volume);
			//slider.putClientProperty("JComponent.sizeVariant", "small");// On MacOS regular, small, mini
			slider.addChangeListener(this);
			slider.addFocusListener(this);
			slider.addMouseListener(this);
			compPanel.add(slider, gbc);
			add(compPanel);
		}

		@Override
		public void focusGained(FocusEvent e) {
			
		}

		/**
		 * Hide the window when the focus is lost. 
		 */
		@Override
		public void focusLost(FocusEvent e) {
			setVisible(false);
			dispose();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			float volume = slider.getValue() / (float) 100;
			vm.getMediaPlayerController().setVolume(volume);	// sets usual master volume slider, which sets master
			volLabel.setText(String.valueOf(slider.getValue()));
		}

		@Override
		public void setVisible(boolean b) {
			super.setVisible(b);
			if (b) {
				slider.grabFocus();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		/**
		 * Hide the window when the mouse exits the window.
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			setVisible(false);
			dispose();		
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
		
	}
}
