package mpi.eudico.client.annotator.player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A panel to display an image.
 * 
 * @author Han Sloetjes
 *
 */
public class ImagePanel extends JPanel implements ComponentListener {
	private BufferedImage image;
	// don't calculate width and height on every repaint, cache
	private float curImgWidth;
	private float curImgHeight;
	private int curImgHorPos;
	private int curImgVertPos;
	private AffineTransform transform;
	// cursor
	private int numCols = 2;
	private int numRows = 2;
	// the first cell is index 0. The number of cells is numCols * numRows  1, default 0
	private int curCell = 0;
	private BasicStroke baseStroke = new BasicStroke();
	private BasicStroke stroke = new BasicStroke(2.0F);
	private Color cursorColor = Color.GREEN;
	private Rectangle2D.Double cursor;
	private boolean cursorVisible = true;
	
	/**
	 * Constructor.
	 * 
	 * @param image the image to display
	 */
	public ImagePanel(BufferedImage image) {
		this.image = image;
		cursor = new Rectangle2D.Double();
		addComponentListener(this);
	}

	/**
	 * Returns the current image.
	 * 
	 * @return the image 
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Sets the image to display.
	 * 
	 * @param image the image to display
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	/**
	 * Clean up.
	 */
	public void flush() {
		if (image != null) {
			image.flush();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			//((Graphics2D) g).drawImage(image, curImgHorPos, curImgVertPos, (int) curImgWidth, (int) curImgHeight, null);
			((Graphics2D) g).drawRenderedImage(image, transform);
		}
		g.setColor(Color.DARK_GRAY);
		((Graphics2D) g).drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		if (cursorVisible) {
			g.setColor(cursorColor);
			((Graphics2D) g).setStroke(stroke);
			((Graphics2D) g).draw(cursor);
			((Graphics2D) g).setStroke(baseStroke);
		}
	}

	/**
	 * Based on their panel's size/aspect ratio and the image's size/aspect ratio 
	 */
	private void calculateSize() {
		if (image != null) {
			if (image.getWidth() > 0 && image.getHeight() > 0) {
				float was = (float) getWidth() / image.getWidth();
				float has = (float) getHeight() / image.getHeight();
				float oas = was < has ? was : has;
				curImgWidth = oas * image.getWidth();
				curImgHeight = oas * image.getHeight();
				
				curImgHorPos = (int) ((getWidth() - curImgWidth) / 2);
				curImgVertPos = (int) ((getHeight() - curImgHeight) / 2);
				transform = new AffineTransform();
				transform.setToScale(oas, oas);
				transform.translate(curImgHorPos, curImgVertPos);
				
				if (cursorVisible) {
					int row = curCell / numCols;
					int col = curCell < numCols ? curCell : curCell % numCols;
					cursor.setFrame(col * (curImgWidth / numCols), row * (curImgHeight / numRows), 
							curImgWidth / numCols, curImgHeight / numRows);
				}
			}
			
			repaint();
		}
	}
	
	public void setCursorGrid(int cols, int rows) {
		if (cols > 0) {
			numCols = cols;
		} else {
			numCols = 1;
		}
		if (rows > 0) {
			numRows = rows;
		} else {
			numRows = 1;
		}
		if (curCell > numCols * numRows) {
			curCell = numCols * numRows;
		}
		calculateSize();
	}
	
	public int[] getCursorGrid() {
		return new int[] {numCols, numRows};
	}
	
	public void setCursorVisible(boolean visible) {
		cursorVisible = visible;
		repaint();
	}
	
	public boolean isCursorVisible() {
		return cursorVisible;
	}
	
	/**
	 * Sets the position of the cursor, based on the progress of the playhead. 
	 * 
	 * @param progress a value between 0 and 1
	 */
	public void setCursorProgress(float progress) {
		curCell = (int) ((numCols * numRows) * progress);
		if (curCell > numCols * numRows - 1) {
			curCell = numCols * numRows - 1;
		}
		calculateSize();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// stub		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		//  stub
	}

	@Override
	public void componentResized(ComponentEvent e) {
		calculateSize();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		calculateSize();
	}
	

}
