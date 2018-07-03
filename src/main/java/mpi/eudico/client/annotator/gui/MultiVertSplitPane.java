package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class MultiVertSplitPane extends JComponent implements ComponentListener, 
    MouseListener, MouseMotionListener {
	private final int DIV_HEIGHT = 8;
	private List<Container> components;
	private List<Container> dividers;
	// resize of whole component
	private int curSPY = -1;
	private int curSPH = -1;
    // dragging
    private int startXY = 0;
    private boolean dragging = false;
    //private JLabel iconLabel;
    static JPanel testScroll;
	
	public MultiVertSplitPane() {
		setLayout(null);
		setDoubleBuffered(false);
		components = new ArrayList<Container>(4);
		dividers = new ArrayList<Container>(4);
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		LookAndFeel.installColors(this, "SplitPane.background", "SplitPane.foreground");
		addComponentListener(this);
	}

	public void addComponent(Container container, int index) {
		if (components.size() == 0) {
			components.add(container);
		} else {
			JPanel divid = createDivider();
			if (index <= 0) {
				components.add(0, container); // insert first
				dividers.add(0, divid);// add a divider
				add(divid);
				// resize components
			} else if (index >= components.size()) { 
				components.add(container);// add at the end
				dividers.add(divid);// add a divider
				add(divid);
				// resize components
			} else {
				components.add(index, container);
				dividers.add(index - 1, divid);// add a divider
				add(divid);
				// resize components
			}
		}
		add(container);
	}
	
	public int getNumComponents() {
		return components.size();
	}
	
	public Component getComponentAt(int index) {
		if (index < 0 || index > components.size() - 1) {
			throw new ArrayIndexOutOfBoundsException("Index: " + index + " < 0 or >= than number of components: " + components.size());
		}
		return components.get(index);
	}
	
	public int getIndexOf(Component comp) {
		return components.indexOf(comp);
	}

	/**
	 * Repaint super, components and dividers.
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {		
		super.paintComponent(g);
		/*
		for (int i = 0; i < components.size(); i++) {
			components.get(i).repaint();
		}
		for (int i = 0; i < dividers.size(); i++) {
			dividers.get(i).repaint();
		} 
		getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
		*/
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
	
	private JPanel createDivider() {
		JPanel divid = new JPanel(null);
		//divid.setBackground(Color.GRAY);
		//divid.setBorder(new BevelBorder(BevelBorder.RAISED));
		divid.setBorder(new LineBorder(Color.GRAY, 1));
		//divid.setBorder(UIManager.getBorder("SplitPaneDivider.border"));
		divid.addMouseListener(this);
		divid.addMouseMotionListener(this);
		
		JLabel iconLabel = new JLabel();
		/*
        try {
            ImageIcon icon = new ImageIcon(this.getClass()
                                               .getResource("/mpi/eudico/client/annotator/resources/ResizeDivider.gif"));
            iconLabel.setIcon(icon);
        } catch (Exception ex) {
            // if the icon could not be loaded
            iconLabel.setText("^");
        }
        */
        iconLabel.setIcon(new ArrowIcon(true));
        divid.add(iconLabel);
        iconLabel.setBounds(4, 1, DIV_HEIGHT, DIV_HEIGHT);
		JLabel iconLabel2 = new JLabel();
		iconLabel2.setIcon(new ArrowIcon(false));
		divid.add(iconLabel2);
		iconLabel2.setBounds(4 + DIV_HEIGHT, 1, DIV_HEIGHT, DIV_HEIGHT);
		return divid;
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		//System.out.println("Split hidden");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		//System.out.println("Split moved");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		//System.out.println("Split resized, h: " + getWidth());
		//System.out.println("Border size: " + getBorder().getBorderInsets(this));
		if (components.size() == 0) {
			repaint();
			return;
		}
		int border = getBorder().getBorderInsets(this).top;
		Rectangle rect;
		if (curSPH == -1) {// distribute equally
			curSPH = getHeight();
			curSPY = getY();
			float cHeight = (getHeight() - (2 * border) - (dividers.size() * DIV_HEIGHT)) / (float) components.size();
			for (int i = components.size() - 1; i > -1; i--) {
				components.get(i).setBounds(border, (int)(i * cHeight) + (i * DIV_HEIGHT) + border, getWidth() - (2 * border), (int)cHeight);
				//System.out.println("comp: " + i + " " + components.get(i).getBounds());
			}
			for (int i = 0; i < dividers.size(); i++) {
				dividers.get(i).setBounds(border, (int)((i + 1) * cHeight) + (i * DIV_HEIGHT) + border, getWidth() - (2 * border), DIV_HEIGHT);
				//System.out.println("divi: " + i + " " + dividers.get(i).getBounds());
			}
		} else {
			for (int i = components.size() - 1; i > -1; i--) {
				rect = components.get(i).getBounds();
				rect.width = getWidth() - (2 * border);
				components.get(i).setBounds(rect);
			}
			for (int i = 0; i < dividers.size(); i++) {
				rect = dividers.get(i).getBounds();
				rect.width = getWidth() - (2 * border);
				dividers.get(i).setBounds(rect);
			}
			
			if (curSPH == getHeight()) {
				repaint();
				return;
			}
			
			if (curSPH > getHeight()) {// decreased height
				if (curSPY == getY()) {
					// reduce height of last component
					rect = components.get(components.size() - 1).getBounds();
					rect.height = getHeight() - border - rect.y;
					if (rect.height < 0) {
						rect.height = 0;
					}
					components.get(components.size() - 1).setBounds(rect);
					// check other components
					Rectangle b2, b3;
					for (int i = dividers.size() - 1; i >= 0; i--) {
						int maxH = getHeight() - border - ((dividers.size() - 1 - i) * DIV_HEIGHT);
						b2 = dividers.get(i).getBounds(); 
						if (b2.y + DIV_HEIGHT > maxH) {
							b2.y = maxH - DIV_HEIGHT;
							dividers.get(i).setBounds(b2);
							rect.y = maxH;
							components.get(i + 1).setBounds(rect);
							// check the component above the divider
							b3 = components.get(i).getBounds();
							if (b3.y + b3.height > b2.y) {
								b3.height = b2.y - b3.y;
								if (b3.height < 0) {
									b3.height = 0;
								}
								components.get(i).setBounds(b3);
							}
						} else {
							break;
						}
					}
				} else {
					// reduce the top component
					//System.out.println("Decrease top");
					int decr = getY() - curSPY;
					rect = components.get(0).getBounds();
					rect.height -= decr;
					if (rect.height < 0) {
						rect.height = 0;
					}
					components.get(0).setBounds(rect);
					// check other components
					Rectangle b2, b3;
					for (int i = 0; i < dividers.size(); i++) {
						int minY = border + i * DIV_HEIGHT;
						b2 = dividers.get(i).getBounds();
						b2.y -= decr;
						if (b2.y < minY) { // is this possible?
							b2.y = minY;
							// check comp above this div
							rect.y = b2.y;
							rect.height = 0;
							components.get(i).setBounds(rect);
						}
						dividers.get(i).setBounds(b2);

						// check next
						b3 = components.get(i + 1).getBounds();
						b3.y -= decr;
						components.get(i + 1).setBounds(b3);
						
							
						if (b3.y < minY + DIV_HEIGHT) {
							int dif = minY + DIV_HEIGHT - b3.y;
							b3.y = minY + DIV_HEIGHT;
							b3.height -= dif;
							if (b3.height < 0) {
								b3.height = 0;
							}
						}
						components.get(i + 1).setBounds(b3);
					}
				}
			} else {// increased height
				if (curSPY == getY()) {
					// increase height of last component
					rect = components.get(components.size() - 1).getBounds();
					rect.height = getHeight() - border - rect.y;
					if (rect.height < 0) {
						rect.height = 0;
					}
					components.get(components.size() - 1).setBounds(rect);
				} else {
					// increase height of the top component
					System.out.println("Increase top");
					rect = components.get(0).getBounds();
					rect.height = rect.height + (rect.y - getY()) - border;
					// check other components
					components.get(0).setBounds(rect);
				}
			}
			curSPH = getHeight();
			curSPY = getY();
		}
		
		/*
		float cWidth = (getWidth() - (dividers.size() * DIV_HEIGHT)) / (float)components.size();
		
		for (int i = 0; i < components.size(); i++) {
			components.get(i).setBounds((int)(i * cWidth) + i * DIV_HEIGHT, 0, (int)cWidth, getHeight());
			System.out.println("comp: " + i + " " + components.get(i).getBounds());
		}
		for (int i = 0; i < dividers.size(); i++) {
			dividers.get(i).setBounds((int)((i + 1) * cWidth) + (i * DIV_HEIGHT), 0, DIV_HEIGHT, getHeight());
			System.out.println("divi: " + i + " " + dividers.get(i).getBounds());
		}
		int lastWidth = getWidth() - components.get(components.size() - 1).getX();
		components.get(components.size() - 1).setBounds(components.get(components.size() - 1).getX(), 0, lastWidth, getHeight());
		System.out.println("last comp: " + components.get(components.size() - 1).getBounds());
		*/
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		System.out.println("Split shown");
		//componentResized(e);
		
	}
	
	public static void main (String[] args) {
		MultiVertSplitPane sp = new MultiVertSplitPane();
		JPanel first = new JPanel();
		first.setBackground(Color.MAGENTA);
		sp.addComponent(first, 0);
		JPanel sec = new JPanel();
		sec.setBackground(Color.GREEN);
		//sec.setDoubleBuffered(false);
		//sec.setOpaque(true);
		sp.addComponent(sec, 1);
		JPanel third = new JPanel();
		third.setBackground(Color.BLUE);
		sp.addComponent(third, 2);
		JFrame testFrame = new JFrame();
		testFrame.getContentPane().setLayout(new BorderLayout());
		
		//
		testFrame.setSize(400, 300);
		testFrame.setVisible(true);
		
		testFrame.getContentPane().add(sp, BorderLayout.CENTER);
		testScroll = new JPanel();
		testScroll.setBackground(Color.YELLOW);
		testScroll.addMouseListener(sp);
		testScroll.setBounds(2, 2, 30, 30);
		testFrame.getContentPane().add(testScroll, BorderLayout.NORTH);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("clicked");

		// TODO Auto-generated method stub
		if (e.getComponent() == testScroll) {
			System.out.println("clicked-2");
			Rectangle r = testScroll.getBounds();// hier, kan weg
			r.height += 20;
			testScroll.setBounds(r);
			r = getBounds();
			r.y += 20;
			r.height -= 20;
			setBounds(r);
			invalidate();
			
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!dragging) {
            setCursor(Cursor.getDefaultCursor());    
        }		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		startXY = e.getY();
		//System.out.println("Start: " + startXY);
		dragging = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (dragging) {
			int dist = e.getY() - startXY;
			// setBounds of involved components
			adjustSizes(e.getComponent(), dist);
			/*
			Rectangle rect = e.getComponent().getBounds();
			rect.y += dist;
			e.getComponent().setBounds(rect);
			repaint();
			*/
		}
        startXY = 0;
        dragging = false;		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int dist = e.getY() - startXY;
		//System.out.println("dist: " + dist);
		adjustSizes(e.getComponent(), dist);
		// setBounds of involved components
		/*
		int minY = getBorder().getBorderInsets(this).top;

		int index = dividers.indexOf(e.getComponent());
		if (index > -1) {
			Rectangle rect = e.getComponent().getBounds();
			int nextY = rect.y + dist;
			if (index > 0) {
				minY = dividers.get(index - 1).getY() + DIV_HEIGHT;
			}
			nextY = nextY > minY ? nextY : minY;

			rect.y = nextY;
			e.getComponent().setBounds(rect);
			
			rect = components.get(index).getBounds();
			rect.height = nextY - rect.y;
			components.get(index).setBounds(rect);
			
			rect = components.get(index + 1).getBounds();
			int oldYH = rect.y + rect.height;
			rect.y = nextY + DIV_HEIGHT;
			rect.height = oldYH - rect.y;
			components.get(index + 1).setBounds(rect);
		}
		repaint();
		*/
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void adjustSizes(Component divider, int amount) {
		if (divider == null || amount == 0) {
			return;
		}
		int minY = getBorder().getBorderInsets(this).top;
		int maxY = getHeight() - minY - DIV_HEIGHT;
		int index = dividers.indexOf(divider);
		if (index > -1) {
			Rectangle rect = divider.getBounds();
			int oldY = rect.y;
			int nextY = rect.y + amount;
			if (amount < 0) {
				if (index > 0) {
					minY = dividers.get(index - 1).getY() + DIV_HEIGHT;
				}
			} else {
				if (index < dividers.size() - 1) {
					maxY = dividers.get(index + 1).getY() - DIV_HEIGHT;
				}
			}
			if (amount < 0) {
				nextY = nextY > minY ? nextY : minY;
			} else {
				nextY = nextY < maxY ? nextY : maxY;
			}
			
			if (nextY == oldY) {
				return;
			}
			rect.y = nextY;
			divider.setBounds(rect);

			rect = components.get(index).getBounds();
			rect.height = nextY - rect.y;
			components.get(index).setBounds(rect);
				
			rect = components.get(index + 1).getBounds();
			int oldYH = rect.y + rect.height;
			rect.y = nextY + DIV_HEIGHT;
			rect.height = oldYH - rect.y;
			components.get(index + 1).setBounds(rect);
				
			components.get(index).repaint();
			components.get(index + 1).repaint();
			divider.repaint();
		}
	}
	
	class ArrowIcon implements Icon {
		Polygon pol = new Polygon();
		
		/**
		 * 
		 */
		public ArrowIcon(boolean up) {
			super();
			if (up) {
				pol.addPoint(1, 5);
				pol.addPoint(4, 1);
				pol.addPoint(7, 5);
			} else {
				pol.addPoint(1, 1);
				pol.addPoint(7, 1);
				pol.addPoint(4, 5);
			}
		}

		@Override
		public int getIconHeight() {
			return 6;
		}

		@Override
		public int getIconWidth() {
			return 8;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
	        Graphics2D g2d = (Graphics2D) g.create();

	        g2d.setColor(UIManager.getColor("Panel.foreground"));
	        g2d.fillPolygon(pol);
	        
	        g2d.dispose();
		}
		
	}

}
