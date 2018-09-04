package mpi.eudico.client.annotator.interlinear;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Renders a print preview of interlinearized content.
 *
 * @author HS
 * @version 1.0
 */
public class PreviewPanel extends JPanel implements ComponentListener,
    AdjustmentListener {
    private Interlinear interlinear;
    private BufferedImage bi;
    private ImagePanel drawPanel;
    private JScrollPane scrollPane;
    private int[] offset = { 0, 0 };
    private Dimension visibleDimensions = new Dimension(550, 600);
    // preview of html export
    private JEditorPane htmlPanel;
    // the panel has its own html renderer
    private HTMLRenderer htmlRenderer;

    /**
     * Creates a new PreviewPanel instance
     *
     * @param interlinear the <code>Interlinear</code> object
     */
    public PreviewPanel(Interlinear interlinear) {
        this.interlinear = interlinear;
        initComponents();
    }

    /**
     * Initialises the user interface components.
     */
    private void initComponents() {
        if (interlinear.getOutputMode() == Interlinear.HTML) {
            htmlRenderer = new HTMLRenderer(interlinear);
            htmlPanel = new JEditorPane();
            htmlPanel.setContentType("text/html");
            htmlPanel.setEditable(false);
            scrollPane = new JScrollPane(htmlPanel);
        } else {
            drawPanel = new ImagePanel();

            //drawPanel.setDoubleBuffered(true);
            scrollPane = new JScrollPane(drawPanel);
        }
        
        scrollPane.setPreferredSize(visibleDimensions);

        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
        
        scrollPane.getVerticalScrollBar().setUnitIncrement(Interlinear.DEFAULT_FONT_SIZE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(Interlinear.DEFAULT_FONT_SIZE);

        //scrollPane.setDoubleBuffered(true);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        //setBackground(Color.yellow);
        if (interlinear.getOutputMode() == Interlinear.HTML) {
            createBufferedImage(10, 10);
        } else {
            createBufferedImage(visibleDimensions.width, visibleDimensions.height); 
        }
        
        addComponentListener(this);
    }

    /**
     * Creates a BufferedImage of the given width and height.
     *
     * @param width the width of the buffer
     * @param height the height of the buffer
     */
    private void createBufferedImage(int width, int height) {
        if ((bi == null) || (bi.getWidth() < width) ||
                (bi.getHeight() < height)) {
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            repaint();
        }
    }

    /**
     * Returns the BufferedImage used for the buffered rendering.
     *
     * @return the BufferedImage
     */
    public BufferedImage getBufferedImage() {
        return bi;
    }

    /**
     * Returns the current horizontal and vertical (scroll) offset.
     *
     * @return an array of size 2, the current horizontal and vertical (scroll)
     *         offset
     */
    public int[] getOffset() {
        return offset;
    }

    /**
     * Sets the size of the panel. This causes the scrollpane to update its
     * scrollbars.
     *
     * @param size the new size of the panel
     */
    public void setImageableSize(Dimension size) {
        if (drawPanel != null) {
            drawPanel.setPreferredSize(size);
            drawPanel.revalidate();    
        }
    }

    /**
     * Causes the panel to adjust its size if necessary and the
     * interlinearization  to be repainted.
     */
    public void updateView() {
        if (interlinear.getOutputMode() == Interlinear.HTML) {
            htmlPanel.setText(htmlRenderer.renderToText());
        } else {
            drawPanel.setPreferredSize(new Dimension(interlinear.getWidth(),
                interlinear.getHeight()));
            drawPanel.revalidate();
            interlinear.drawViewOnImage(bi, offset);
        }
        repaint();
    }

    /**
     * Implements the ComponentListener interface.  Invokes a repaint of the
     * linearization.
     *
     * @param e the component event
     */
    @Override
	public void componentResized(ComponentEvent e) {
        if (interlinear.getOutputMode() != Interlinear.HTML) {
            createBufferedImage(this.getWidth(), this.getHeight());
            interlinear.drawViewOnImage(bi, offset);    
        }
        repaint();
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
	public void componentMoved(ComponentEvent e) {
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
	public void componentShown(ComponentEvent e) {
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
	public void componentHidden(ComponentEvent e) {
    }

    /**
     * Implements the AdjustmentListener interface.
     *
     * @param e the adjustment event
     */
    @Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
        offset[0] = scrollPane.getHorizontalScrollBar().getValue();
        offset[1] = scrollPane.getVerticalScrollBar().getValue();
        
        if (interlinear.getOutputMode() != Interlinear.HTML) {
            interlinear.drawViewOnImage(bi, offset);    
        }
        
        repaint();
    }

    /**
     * A class for painting a buffered image to its graphics environment
     *
     * @author HS
     * @version 1.0
     */
    private class ImagePanel extends JPanel {
        /**
         * Overrides the JComponent's paintComponent method.
         *
         * @param g the graphics context
         */
        @Override
		public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bi != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(bi, offset[0], offset[1], null);

                //g2d.drawImage(bi, 0, 0, null);
            }
        }
    }
}
