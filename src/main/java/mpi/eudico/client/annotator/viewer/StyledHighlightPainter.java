package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;


/**
 * A class that adds some style options to the default text HighlightPainter. <br>
 * This highlighter can paint the highlight either solid or stroked (outlines)
 * and  its visibility can be set.
 *
 * @author Han Sloetjes
 */
public class StyledHighlightPainter extends DefaultHighlightPainter {
    /** A constant for the stroked, outline styled highlight */
    public static final int STROKED = 0;

    /** A constant for the filled, solid styled highlight */
    public static final int FILLED = 1;
    
    /** A constant for the squiggly underline styled highlight */
    public static final int SQUIGGLED = 2;
    
    int paintOffset;
    boolean visible = true;
    int paintMode = STROKED;

    /**
     * Creates a new StyledHighlightPainter instance.
     *
     * @param c the Color to used for the highlight
     * @param paintOffset the number of pixels to add to the size of the
     *        highlight
     */
    public StyledHighlightPainter(Color c, int paintOffset) {
        super(c);
        this.paintOffset = paintOffset;
    }

    /**
     * Creates a new StyledHighlightPainter instance.
     *
     * @param c the Color to used for the highlight
     * @param paintOffset the number of pixels to add to the size of the
     *        highlight
     * @param paintMode FILLED or STROKED
     */
    public StyledHighlightPainter(Color c, int paintOffset, int paintMode) {
        super(c);
        this.paintOffset = paintOffset;

        if ((paintMode >= STROKED) && (paintMode <= SQUIGGLED)) {
            this.paintMode = paintMode;
        }
    }

    /**
     * Paints the highlight.
     *
     * @param g the graphics object
     * @param offs0 begin offset
     * @param offs1 end offset
     * @param bounds the bounds
     * @param c the text component
     */
    @Override
	public void paint(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c) {
        if (!visible) {
            return;
        }

        Rectangle alloc = bounds.getBounds();

        try {
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);

            // --- render ---
            Color color = getColor(offs0);

            if (color == null) {
                g.setColor(c.getSelectionColor());
            } else {
                g.setColor(color);
            }

            if (p0.y == p1.y) {
                // same line, render a rectangle
                Rectangle r = p0.union(p1);

                if (paintMode == STROKED) {
                    g.drawRect(r.x + paintOffset, r.y + paintOffset,
                        r.width - (2 * paintOffset),
                        r.height - (2 * paintOffset));
                } else if (paintMode == FILLED) {
                    g.fillRect(r.x + paintOffset, r.y + paintOffset,
                        r.width - (2 * paintOffset),
                        r.height - (2 * paintOffset));
                } else if (paintMode == SQUIGGLED) {
                	//System.out.println("SQUIGGLED 1");
                	int squiggle = 2;
            		int twoSquiggles = squiggle * 2;
            		int y = r.y + r.height - squiggle;

                    for (int x = r.x; x <= r.x + r.width - twoSquiggles; x += twoSquiggles)
                    {
                        g.drawArc(x, y, squiggle, squiggle, 0, 180);
                        g.drawArc(x + squiggle, y, squiggle, squiggle, 180, 181);
                    }
                }
            } else {
                // different lines
                int p0ToMarginWidth = (alloc.x + alloc.width) - p0.x;

                if (paintMode == STROKED) {
                    g.drawRect(p0.x + paintOffset, p0.y + paintOffset,
                        p0ToMarginWidth - (2 * paintOffset),
                        p0.height - (2 * paintOffset));

                    if ((p0.y + p0.height) != p1.y) {
                        g.drawRect(alloc.x + paintOffset,
                            p0.y + p0.height + paintOffset,
                            alloc.width - (2 * paintOffset),
                            p1.y - (p0.y + p0.height) - (2 * paintOffset));
                    }

                    g.drawRect(alloc.x + paintOffset, p1.y + paintOffset,
                        (p1.x - alloc.x) - (2 * paintOffset),
                        p1.height - (2 * paintOffset));
                } else if (paintMode == FILLED) {
                    g.fillRect(p0.x + paintOffset, p0.y + paintOffset,
                        p0ToMarginWidth - (2 * paintOffset),
                        p0.height - (2 * paintOffset));

                    if ((p0.y + p0.height) != p1.y) {
                        g.fillRect(alloc.x + paintOffset,
                            p0.y + p0.height + paintOffset,
                            alloc.width - (2 * paintOffset),
                            p1.y - (p0.y + p0.height) - (2 * paintOffset));
                    }

                    g.fillRect(alloc.x + paintOffset, p1.y + paintOffset,
                        (p1.x - alloc.x) - (2 * paintOffset),
                        p1.height - (2 * paintOffset));
                } else if (paintMode == SQUIGGLED) {
                	//TODO
                	
                }
            }
        } catch (BadLocationException e) {
            // can't render
        }
    }

    /**
     * Paints the layered highlight.
     *
     * @param g the graphics object
     * @param offs0 begin offset
     * @param offs1 end offset
     * @param bounds the bounds
     * @param c the text component
     * @param view the text view
     *
     * @return the shape that has been painted or null
     */
    @Override
	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c, View view) {
        if (!visible) {
            return null;
        }

        Color color = getColor(offs0);

        if (color == null) {
            g.setColor(c.getSelectionColor());
        } else {
            g.setColor(color);
        }

        if ((offs0 == view.getStartOffset()) && (offs1 == view.getEndOffset())) {
            // Contained in view, can just use bounds.
            Rectangle alloc;

            if (bounds instanceof Rectangle) {
                alloc = (Rectangle) bounds;
            } else {
                alloc = bounds.getBounds();
            }

            if (paintMode == STROKED) {
                g.drawRect(alloc.x + paintOffset, alloc.y + paintOffset,
                    alloc.width - (2 * paintOffset),
                    alloc.height - (2 * paintOffset));
            } else if (paintMode == FILLED) {
                g.fillRect(alloc.x + paintOffset, alloc.y + paintOffset,
                    alloc.width - (2 * paintOffset),
                    alloc.height - (2 * paintOffset));
            } else if (paintMode == SQUIGGLED) {
            	int squiggle = 2;
        		int twoSquiggles = squiggle * 2;
        		int y = alloc.y + alloc.height - squiggle;

                for (int x = alloc.x; x <= alloc.x + alloc.width - twoSquiggles; x += twoSquiggles)
                {
                    g.drawArc(x, y, squiggle, squiggle, 0, 180);
                    g.drawArc(x + squiggle, y, squiggle, squiggle, 180, 181);
                }
            }

            return alloc;
        } else {
            // Should only render part of View.
            try {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                        offs1, Position.Bias.Backward, bounds);
                Rectangle r = (shape instanceof Rectangle) ? (Rectangle) shape
                                                           : shape.getBounds();

                if (paintMode == STROKED) {
                    g.drawRect(r.x + paintOffset, r.y + paintOffset,
                        r.width - (2 * paintOffset),
                        r.height - (2 * paintOffset));
                } else if (paintMode == FILLED) {
                    g.fillRect(r.x + paintOffset, r.y + paintOffset,
                        r.width - (2 * paintOffset),
                        r.height - (2 * paintOffset));
                } else if (paintMode == SQUIGGLED) {
                	int squiggle = 2;
            		int twoSquiggles = squiggle * 2;
            		int y = r.y + r.height - squiggle + paintOffset;
            		
                    for (int x = r.x + paintOffset; x <= r.x + r.width - twoSquiggles; x += twoSquiggles)
                    {
                    	g.drawArc(x, y, squiggle, squiggle, 0, 180);
                        g.drawArc(x + squiggle, y, squiggle, squiggle, 180, 181);
                    }
                }

                return r;
            } catch (BadLocationException e) {
                // can't render
            }
        }

        // Only if exception
        return null;
    }
    
    /**
     * Added for convenient extending with a painter that has a map of indices to colors. This way the 
     * paint and paintLayer methods don't need to be overridden.
     *  
     * @param beginIndex ignored
     * @return {@link #getColor()}
     */
	public Color getColor(int beginIndex) {
		return getColor();
	}

    /**
     * Returns the visibility.
     *
     * @return Returns the visibility.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility.
     *
     * @param visible the visibility value.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the paint mode.
     *
     * @return the current paint mode
     */
    public int getPaintMode() {
        return paintMode;
    }

    /**
     * Sets the paint mode.
     *
     * @param mode the new paint mode, either FILLED or STROKED
     */
    public void setPaintMode(int mode) {
        if ((mode == FILLED) || (mode == STROKED)) {
            paintMode = mode;
        }
    }

    /**
     * Returns the paint offset.
     *
     * @return the offset, positive or negative, used to modify the size of the
     *         highlight (as compared to the default highlight size)
     */
    public int getPaintOffset() {
        return paintOffset;
    }

    /**
     * Sets the paint offset.
     *
     * @param offset the paint offset
     *
     * @see #getPaintOffset()
     */
    public void setPaintOffset(int offset) {
        paintOffset = offset;
    }
}
