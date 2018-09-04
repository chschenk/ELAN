package nl.mpi.jds;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

@SuppressWarnings("serial")
public class JDSPanel extends Panel implements ComponentListener, HierarchyListener {
	private JDSPlayer player;
	
	public JDSPanel() {
		super(null);
		addComponentListener(this);
		addHierarchyListener(this);
	}

	public JDSPanel(JDSPlayer player) {
		super(null);
		this.player = player;
		this.setBackground(new Color(0, 0, 128));
		addComponentListener(this);
		addHierarchyListener(this);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		//System.out.println("Panel add notify...");
		if (player != null && this.isDisplayable()) {
			//System.out.println("Panel add notify, displayable...");
			player.setVisualComponent(this);
			player.setVisible(true);
		}
	}

	@Override
	public void removeNotify() {
		
		if (player != null) {
			player.setVisualComponent(null);
			player.setVisible(false);
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
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentPos(0, 0, getWidth(), getHeight());
//			int w = getWidth();
//			int h = getHeight();
//			player.setVisualComponentPos(-w/4, -h/4, 2*w, 2*h);
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && isDisplayable()) {
			//System.out.println("Hierarchy...");
			player.setVisualComponent(this);
			player.setVisible(true);
		}
	}

}
