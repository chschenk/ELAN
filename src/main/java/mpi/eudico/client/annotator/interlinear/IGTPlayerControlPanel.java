package mpi.eudico.client.annotator.interlinear;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;

/**
 * A small player control panel for the Interlinearization mode.
 * The play button allows to play the interval of the selected block,
 * the selected row in the list. 
 * Currently only the audio is rendered, even if the master media 
 * is video.
 * 
 * @author Han Sloetjes
 *
 * @version Feb 15, 2018
 */
@SuppressWarnings("serial")
public class IGTPlayerControlPanel extends AbstractViewer {
	private JButton playPauseButton;
	private PlaybackProgressPanel ppPanel; 
	
	private int margin = 5;
	private Dimension prefSize;
	private Dimension minSize;
	
	/**
	 * Constructor, initializes the panel.
	 */
	public IGTPlayerControlPanel() {
		initPanel();
	}

	/**
	 * Adds a button and a player progress panel. The action of the button 
	 * is added when the viewer manager is set. 
	 * 
	 * @see #setViewerManager(ViewerManager2)
	 */
	private void initPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(margin, margin, margin, margin);
		
		playPauseButton = new JButton();
		add(playPauseButton, gbc);
		
		ppPanel = new PlaybackProgressPanel();
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(margin, 0, margin, margin);
		add(ppPanel, gbc);
	}	
	
	/**
	 * Adds the play selection action to the play/pause button.
	 * 
	 * @param viewerManager holds a reference to the transcription object
	 * which is used to get the play selection action
	 */
	@Override
	public void setViewerManager(ViewerManager2 viewerManager) {
		super.setViewerManager(viewerManager);
		if (viewerManager != null && playPauseButton != null) {
			playPauseButton.setAction(ELANCommandFactory.getCommandAction(
	                getViewerManager().getTranscription(), 
	                ELANCommandFactory.PLAY_SELECTION));
			revalidate();
		}
	}

	/**
	 * A controller update just triggers a repaint of the progress panel.
	 * 
	 * @param event the controller event
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {
		ppPanel.repaint();
	}

	/**
	 * If there is a selection the progress panel will be repainted,
	 * otherwise the panel is disabled.
	 */
	@Override
	public void updateSelection() {
		if (getSelectionBeginTime() == getSelectionEndTime()) {
			// no selection
			setEnabled(false);
		} else {
			if (!isEnabled()) {
				setEnabled(true);
			}
		}
		ppPanel.repaint();
	}

	/**
	 * Calculates the position of the current media time within
	 * the current interval (selection).
	 * 
	 * @return the position as a decimal fraction between 0 and 1
	 */
	protected float getMediaAdvance() {
		long b = getSelectionBeginTime();
		long e = getSelectionEndTime();
		long m = getMediaTime();
		
		if ( b != e && m >= b && m <= e) {
			if (m == e) {
				return 1.0f;
			} else {
				return (m - b) / (float)(e - b);
			}
		}
		
		return 0f;
	}
	
	@Override
	public void updateActiveAnnotation() {
		// stub
	}

	@Override
	public void updateLocale() {
		// stub
	}

	@Override
	public void preferencesChanged() {
		// stub
	}

	
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		playPauseButton.setEnabled(enabled);
		ppPanel.setEnabled(enabled);
	}

	/**
	 * @return a preferred size based on the preferred size of the button,
	 * some margins and a preferred width of the progress bar
	 */
	@Override
	public Dimension getPreferredSize() {
		if (prefSize == null) {
			Dimension d = playPauseButton.getPreferredSize();
			Dimension d2 = ppPanel.getPreferredSize();
			int w = d.width + 3 * margin + d2.width;
			int h = d.height + 2 * margin;
			prefSize = new Dimension(w, h);
		}
		
		return prefSize;
	}

	/**
	 * @return the minimal size based on the preferred size of the button,
	 * some margins and a minimal width for the progress bar 
	 */
	@Override
	public Dimension getMinimumSize() {
		if (minSize == null) {
			Dimension d = playPauseButton.getMinimumSize();
			Dimension d2 = ppPanel.getMinimumSize();
			int w = d.width + 2 * margin + d2.width;
			int h = d.height;
			minSize = new Dimension(w, h);
		}
		
		return minSize;
	}

	//############################################
	/**
	 * A panel with a simple progress bar showing the progress
	 * of the media time within the time interval of the selection
	 * (corresponding to the selected block in the interlinear view).
	 * 
	 * @version Feb 15, 2018
	 */
	private class PlaybackProgressPanel extends JPanel {
		private Dimension prefSizePPP;
		private Dimension minSize;
		
		public PlaybackProgressPanel() {
			super(null);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);			

			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
			float adv = getMediaAdvance();
			int w = (int) (adv * getWidth());
			boolean enabled = isEnabled();
			
			if (enabled) {
				g.setColor(Constants.SELECTIONCOLOR);
			} else {
				g.setColor(Color.LIGHT_GRAY);
			}
			g.fillRect(0, 0, w, getHeight());
			
			if (enabled) {
				g.setColor(Constants.CROSSHAIRCOLOR);
			} else {
				g.setColor(Constants.MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR);
			}
			if (w == 0) w++;
			if (w >= getWidth() - 1) w = w -= 2;
			g.drawLine(w, 0, w, getHeight());
			
			g.setColor(Constants.MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		/**
		 * @return the preferred width is set to the preferred width of
		 * the button
		 */
		@Override
		public Dimension getPreferredSize() {
			if (prefSizePPP == null) {
				Dimension butSize = playPauseButton.getPreferredSize();
				prefSizePPP = new Dimension(butSize.width, butSize.height / 2);
			}
			
			return prefSizePPP;
		}

		/**
		 * @return the minimum width is set to half the preferred width of
		 * the button
		 */
		@Override
		public Dimension getMinimumSize() {
			if (minSize == null) {
				Dimension butSize = playPauseButton.getPreferredSize();
				minSize = new Dimension(butSize.height / 2, butSize.height / 2);
			}
			
			return minSize;
		}		
	}
}
