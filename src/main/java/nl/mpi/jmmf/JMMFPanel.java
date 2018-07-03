package nl.mpi.jmmf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

@SuppressWarnings("serial")
public class JMMFPanel extends Panel implements ComponentListener, HierarchyListener {
	private JMMFPlayer player;
	
	public JMMFPanel() {
		super(null);
		this.setBackground(new Color(0, 0, 128));
		addComponentListener(this);
		addHierarchyListener(this);
		super.setIgnoreRepaint(true);
	}

	public JMMFPanel(JMMFPlayer player) {
		super(null);
		this.player = player;
		this.setBackground(new Color(0, 0, 128));
		addComponentListener(this);
		addHierarchyListener(this);
		super.setIgnoreRepaint(true);
	}
	
	public void setPlayer(JMMFPlayer player) {
		this.player = player;
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		//System.out.println("Panel add notify...");
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
//		player.repaintVideo();
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentSize(getWidth(), getHeight());

			player.repaintVideo();
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
//		player.repaintVideo();
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

	@Override
	public void paintComponents(Graphics g) {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void validate() {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void paintAll(Graphics g) {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void repaint(long tm) {
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w,
			int h) {
		return false;
	}

	@Override
	public void setIgnoreRepaint(boolean ignoreRepaint) {

		super.setIgnoreRepaint(true);
	}

}
