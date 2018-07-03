package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * A Color Chooser panel for organizing favorite colors. Saving and loading favorite colors
 * is the responsibility of the caller.
 *  
 * @author Han Sloetjes
 */
public class FavoriteColorPanel extends AbstractColorChooserPanel implements ActionListener, 
    ComponentListener, MouseListener {
	public final int NUM_ROWS = 5;
	public final int NUM_COLS = 10;
	private final int GAP = 5;
	private final int CELL_DIM = 20;
	private Dimension matrixSize;
	private int selIndex = 0;
	private ResourceBundle bundle;
	private Color[] colors;
	private JButton addButton;
	private JButton insertButton;
	private JButton removeButton;
	private JButton copyButton;
	private JButton cutButton;
	private JButton pasteButton;
	private JPanel buttonPanel;
	private Color copyColor = null;

	/**
	 * No argument constructor.
	 */
	public FavoriteColorPanel() {
		super();
		colors = new Color[NUM_COLS * NUM_ROWS];
		matrixSize = new Dimension(NUM_COLS * CELL_DIM + ((NUM_COLS + 1) * GAP), 
				NUM_ROWS * CELL_DIM + ((NUM_ROWS + 1) * GAP));
	}
	
	/**
	 * Constructor with a resource bundle containing localized strings for the UI.
	 * 
	 * @param bundle the resource bundle
	 */
	public FavoriteColorPanel(ResourceBundle bundle) {
		this();
		this.bundle = bundle;
	}
	
	/**
	 * Constructor with an array of previously stored favorite colors.
	 * 
	 * @param favColors the stored favorite colors
	 */
	public FavoriteColorPanel(Color[] favColors) {
		this();
		setColors(favColors);
	}
	
	/**
	 * Constructor wit a resource bundle and an array of favorite colors.
	 * 
	 * @param bundle a bundle with localized resources
	 * @param favColors an array of previously stored colors
	 */
	public FavoriteColorPanel(ResourceBundle bundle, Color[] favColors) {
		this();
		this.bundle = bundle;
		setColors(favColors);
	}
	
	/**
	 * Sets the current favorite colors.
	 * 
	 * @param nextColors the new array of favorite colors.
	 */
	public void setColors(Color[] nextColors) {
		if (nextColors != null) {
			int numColors = NUM_COLS * NUM_ROWS;
			for (int i = 0; i < numColors && i < nextColors.length; i++) {
				colors[i] = nextColors[i];
			}
		}
	}

	/**
	 * Returns the current array of favorite colors. (Does nor make a copy, so allows direct access.)
	 * 
	 * @return the current array of favorite colors
	 */
	public Color[] getColors() {
		return colors;
	}
	
	/**
	 * Creates the layout of the panel.
	 */
	@Override
	protected void buildChooser() {
		this.setLayout(null);
		int margin = 2;
		buttonPanel = new JPanel(new GridLayout(1, 7, margin, margin));
		
		addButton = new JButton();
		addButton.addActionListener(this);
		buttonPanel.add(addButton);
		
		insertButton = new JButton();
		insertButton.addActionListener(this);
		buttonPanel.add(insertButton);
		
		removeButton = new JButton();
		removeButton.addActionListener(this);
		buttonPanel.add(removeButton);
		
		JPanel div = new JPanel();
		div.setPreferredSize(new Dimension(8, 8));
		buttonPanel.add(div);
		
		copyButton = new JButton();
		copyButton.addActionListener(this);
		buttonPanel.add(copyButton);		
		
		cutButton = new JButton();
		cutButton.addActionListener(this);
		buttonPanel.add(cutButton);
		
		pasteButton = new JButton();
		pasteButton.addActionListener(this);
		buttonPanel.add(pasteButton);
		
		// try to load icons
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Add16.gif"));
			if (icon != null) {
				addButton.setIcon(icon);
			} else {
				addButton.setText("A");
			}
		} catch (Exception ex) {// any exception
			addButton.setText("A");
		}

		try {
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Insert16.gif"));
			if (icon != null) {
				insertButton.setIcon(icon);
			} else {
				insertButton.setText("I");
			}
		} catch (Exception ex) {// any exception
			insertButton.setText("I");
		}
		
		try {
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Delete16.gif"));
			if (icon != null) {
				removeButton.setIcon(icon);
			} else {
				removeButton.setText("D");
			}
		} catch (Exception ex) {// any exception
			removeButton.setText("D");
		}
		
		try {
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Copy16.gif"));
			if (icon != null) {
				copyButton.setIcon(icon);
			} else {
				copyButton.setText("C");
			}
		} catch (Exception ex) {// any exception
			copyButton.setText("C");
		}
		
		try {
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Cut16.gif"));
			if (icon != null) {
				cutButton.setIcon(icon);
			} else {
				cutButton.setText("X");
			}
		} catch (Exception ex) {// any exception
			copyButton.setText("X");
		}
		
		try {
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Paste16.gif"));
			if (icon != null) {
				pasteButton.setIcon(icon);
			} else {
				pasteButton.setText("P");
			}
		} catch (Exception ex) {// any exception
			copyButton.setText("P");
		}
		
		Dimension bdim = addButton.getPreferredSize();
		buttonPanel.setPreferredSize(new Dimension(6 * bdim.width + 5 * margin, bdim.height + 2 * margin));
		this.add(buttonPanel);
		addMouseListener(this);
		setFocusable(true);
		setTexts();
	}

	/**
	 * If there is a resource bundle tooltip texts are used from the bundle.
	 */
	private void setTexts() {
		if (bundle != null) {
			String tt = null;
			tt = bundle.getString("Button.Add");
			if (tt != null) {
				addButton.setToolTipText(tt);
			}
			tt = bundle.getString("Button.Insert");
			if (tt != null) {
				insertButton.setToolTipText(tt);
			}
			tt = bundle.getString("Button.Delete");
			if (tt != null) {
				removeButton.setToolTipText(tt);
			}
			tt = bundle.getString("Button.Copy");
			if (tt != null) {
				copyButton.setToolTipText(tt);
			}
			tt = bundle.getString("Button.Cut");
			if (tt != null) {
				cutButton.setToolTipText(tt);
			}
			tt = bundle.getString("Button.Paste");
			if (tt != null) {
				pasteButton.setToolTipText(tt);
			}
		} else {
			addButton.setToolTipText("Add");
			insertButton.setToolTipText("Insert");
			removeButton.setToolTipText("Delete");
			copyButton.setToolTipText("Copy");
			cutButton.setToolTipText("Cut");
			pasteButton.setToolTipText("Paste");
		}
	}
	
	/**
	 * Returns the name of the panel to use in the chooser.
	 * @return the name of the panel
	 */
	@Override
	public String getDisplayName() {
		if (bundle != null) {
			String title = bundle.getString("ColorChooser.Favorites.Title");
			if (title != null) {
				return title;
			}
		}
		return "Favorites";
	}

	/**
	 * Returns null.
	 * @return null
	 */
	@Override
	public Icon getLargeDisplayIcon() {
		return null;
	}

	/**
	 * Returns null.
	 * 
	 * @return null
	 */
	@Override
	public Icon getSmallDisplayIcon() {
		return null;
	}

	/**
	 * Recalculates the position of the button panel 
	 */
	@Override
	public void updateChooser() {
		if (buttonPanel != null) {
			int w = this.getWidth();
			int h = this.getHeight();
			buttonPanel.setBounds((w - buttonPanel.getPreferredSize().width) / 2, h - buttonPanel.getPreferredSize().height, 
					buttonPanel.getPreferredSize().width, buttonPanel.getPreferredSize().height);			
		}
		Color curCol = getColorSelectionModel().getSelectedColor();
		int firstEmpty = -1;
		for(int i = 0; i < colors.length; i++) {
			if (firstEmpty < 0 && colors[i] == null) {
				firstEmpty = i;
			}
			if (colors[i] != null && colors[i].equals(curCol)) {
				selIndex = i;
				break;
			}
			if (i == colors.length - 1) {
				selIndex = firstEmpty < 0 ? i : firstEmpty;
			}
		}
		requestFocus();
		repaint();
	}

	/**
	 * Returns the preferred size, which is based on the size of the array, the color cell dimensions
	 * and the button panel.
	 * 
	 * @return the preferred size
	 */
	@Override
	public Dimension getPreferredSize() {
		int w = matrixSize.width + 2 * GAP;
		int h = matrixSize.height + 30 + GAP;
		if (buttonPanel != null) {
			int bw = buttonPanel.getPreferredSize().width;
			w = bw > w ? bw : w;
			int bh = buttonPanel.getPreferredSize().height;
			h = bh > h ? bh : h;
		}
		return new Dimension(w, h);
	}

	/**
	 * Paints the color cells, a grid of 5 X 10 squares.
	 * 
	 * @param g the graphics object
	 */
	@Override
	protected void paintComponent(Graphics g) {
		//super.paintComponent(g);
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(), getHeight());
        int x = (getWidth() - matrixSize.width) / 2;
        int bh = 0;
        if (buttonPanel != null) {
        	bh = buttonPanel.getPreferredSize().height;
        }
        int y = (getHeight() - bh - matrixSize.height) / 2;
        
        g.setColor(getForeground());
        g.drawRect(x, y, matrixSize.width, matrixSize.height);
        x += GAP;
        y+= GAP;
        
        int r, c;
        for (int i = 0; i < colors.length; i++) {
        	r = i / NUM_COLS;
        	c = i - r * NUM_COLS;
        	if (i == selIndex) {
        		g.setColor(Color.BLUE); // use a ui manager color
        		g.drawRect(x + c * CELL_DIM + c * GAP - 2, y + r * CELL_DIM + r * GAP - 2, CELL_DIM + 4, CELL_DIM + 4);
        	}
        	if (colors[i] != null) {
        		g.setColor(colors[i]);
        		g.fillRect(x + c * CELL_DIM + c * GAP, y + r * CELL_DIM + r * GAP, CELL_DIM, CELL_DIM);
        	}
        	//g.fillRect(x + c * CELL_DIM + c * GAP, y + r * CELL_DIM + r * GAP, CELL_DIM, CELL_DIM);
        	g.setColor(Color.DARK_GRAY);
        	g.drawRect(x + c * CELL_DIM + c * GAP, y + r * CELL_DIM + r * GAP, CELL_DIM, CELL_DIM);
        }
	}

	/**
	 * Handles the edit button actions 
	 * 
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {// add after the last non-null color
			Color curCol = getColorFromModel();
			if (curCol != null) {
				for (int i = colors.length - 1; i >= 0; i--) {
					if (colors[i] != null && i < colors.length - 2) {
						colors[i + 1] = curCol;
						selIndex = i + 1;
						break;
					}
					if (i == 0 && colors[i] == null) {
						colors[0] = curCol;
						selIndex = 0;
					}
				}
			}
		} else if (e.getSource() == insertButton) {// insert at current index. 
			//If current index is not null, shift all colors after this index 
			Color curCol = getColorFromModel();
			if (curCol != null && selIndex >= 0 && selIndex < colors.length) {
				if (colors[selIndex] != null) {
					for (int i = colors.length - 2; i >= selIndex; i--) {
						colors[i + 1] = colors[i];
					}
				}
				//colors[selIndex] = curCol;// or insert empty
				colors[selIndex] = null;
			}
		} else if (e.getSource() == removeButton) {
			if (selIndex >= 0 && selIndex < colors.length) {
				colors[selIndex] = null;
			}
		} else if (e.getSource() == copyButton) {
			if (selIndex >= 0 && selIndex < colors.length) {
				copyColor = colors[selIndex];
			}
		} else if (e.getSource() == cutButton) {
			if (selIndex >= 0 && selIndex < colors.length) {
				copyColor = colors[selIndex];
				colors[selIndex] = null;
			}
		} else if (e.getSource() == pasteButton) {
			if (copyColor != null && selIndex >= 0 && selIndex < colors.length) {
				colors[selIndex] = copyColor;
			}
		}
		getColorSelectionModel().setSelectedColor(colors[selIndex]);
		repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// method stub
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// method stub
	}

	/**
	 * Recalculates the position of the button panel and repaints.
	 * 
	 * @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		if (buttonPanel != null) {
			int x = (getWidth() - buttonPanel.getPreferredSize().width) / 2; 
			int y = getHeight() - buttonPanel.getPreferredSize().height;
			buttonPanel.setBounds(x, y , buttonPanel.getPreferredSize().width, buttonPanel.getPreferredSize().height);
		}
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// method stub
	}

	/**
	 * Activates or selects a color cell in the grid. Can be used to insert, cut, copy or paste etc. 
	 * a color to/from this cell. 
	 * 
	 * @param e the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		int matX = (getWidth() - matrixSize.width) / 2 + GAP;
        int bh = 0;
        if (buttonPanel != null) {
        	bh = buttonPanel.getPreferredSize().height;
        }
        int matY = (getHeight() - bh - matrixSize.height) / 2 + GAP;
		int rowY = e.getY() - matY;
		int colX = e.getX() - matX;
		if (colX < 0 || rowY < 0 || colX > matX + matrixSize.width || rowY > matY + matrixSize.height) {
			return;
		}
		
		int row = -1;
		while (rowY > 0) {
			row++;
			rowY -= (CELL_DIM + GAP);
			if (rowY < 0 && rowY >- GAP) {
				// in  a gap
				row = -1;
				break;
			}
		}
		if (row >= 0) {
			int col = -1;
			while (colX > 0) {
				col++;
				colX -= (CELL_DIM + GAP);
				if (colX < 0 && colX > -GAP) {
					// in a gap
					col = -1;
					break;
				}
			}
			
			if (col >= 0) {
				int index = row * NUM_COLS + col;
				if (index >= 0 && index < colors.length) {
					selIndex = index;
					if (colors[selIndex] != null) {
						getColorSelectionModel().setSelectedColor(colors[selIndex]);
					}
					repaint();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// method stub
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// method stub
	}

	/**
	 * Overrides the default behavior for the left-right-up-down arrow keys to select 
	 * an other cell in the grid.
	 * 
	 * @return true if the event's key was one of the four arrow keys
	 */
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed) {
		// condition is always JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT in this situation
		if (pressed) {
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

				selIndex++;
				if (selIndex >= colors.length) {
					selIndex = 0;
				}
				repaint();
				if (colors[selIndex] != null) {
					getColorSelectionModel().setSelectedColor(colors[selIndex]);
				}
				
				return true;
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {

				selIndex--;
				if (selIndex < 0) {
					selIndex = colors.length - 1;
				}
				repaint();
				if (colors[selIndex] != null) {
					getColorSelectionModel().setSelectedColor(colors[selIndex]);
				}
				
				return true;
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				selIndex -= NUM_COLS;
				if (selIndex < 0) {
					selIndex += colors.length;// += (NUM_COLS * NUM_ROWS)
				}
				repaint();
				if (colors[selIndex] != null) {
					getColorSelectionModel().setSelectedColor(colors[selIndex]);
				}
				
				return true;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				selIndex += NUM_COLS;
				if (selIndex > colors.length - 1) {
					selIndex -= colors.length;
				}
				repaint();
				if (colors[selIndex] != null) {
					getColorSelectionModel().setSelectedColor(colors[selIndex]);
				}
				
				return true;
			}
		}

		return super.processKeyBinding(ks, e, condition, pressed);
	}
	
	

}
