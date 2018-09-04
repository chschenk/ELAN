package nl.mpi.jmmf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * First tests: the native video is displayed on the canvas, but not in the right place.
 * Setting the window position does not seem to work.
 * @author han
 *
 */
@SuppressWarnings("serial")
public class JMMFCanvas extends Canvas implements ComponentListener, HierarchyListener {
	JMMFPlayer player;

	public JMMFCanvas() {
		super();
	}

	public JMMFCanvas(JMMFPlayer player) {
		super();
		this.player = player;
		this.setBackground(Color.GREEN);
		super.setIgnoreRepaint(true);
		addComponentListener(this);
		addHierarchyListener(this);
	}
	
	public JMMFCanvas(GraphicsConfiguration gc) {
		super(gc);
	}

	public void setPlayer(JMMFPlayer player) {
		this.player = player;
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		System.out.println("Panel add notify...");
		if (player != null && this.isDisplayable()) {
			//System.out.println("Panel add notify, displayable...");
			player.setVisualComponent(this);
			player.setVisible(true);
			player.repaintVideo();
		}
	}

	@Override
	public void removeNotify() {
		
		if (player != null) {
			//player.setVisualComponent(null);
			//player.setVisible(false);
		}
		super.removeNotify();
	}

	@Override
	public void componentHidden(ComponentEvent ce) {
		if (player != null) {
			player.setVisible(false);
		}
	}

	@Override
	public void componentMoved(ComponentEvent ce) {
		componentResized(ce);
		player.repaintVideo();
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentSize(getWidth(), getHeight());
//			int w = getWidth();
//			int h = getHeight();
//			player.setVisualComponentPos(-w/4, -h/4, 2*w, 2*h);
			player.repaintVideo();
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
		player.repaintVideo();
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && isDisplayable()) {
			//System.out.println("Hierarchy...");
			if (player != null) {
				player.setVisualComponent(this);
				player.setVisible(true);
				player.repaintVideo();
			}
		}
	}

	@Override
	public void repaint() {
		//super.repaint();
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void paint(Graphics g) {
		//super.paint(g);
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void update(Graphics g) {
		//super.update(g);
		if (player != null) {
			player.repaintVideo();
		}
	}

}
