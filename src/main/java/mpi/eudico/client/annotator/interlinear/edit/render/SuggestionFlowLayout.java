package mpi.eudico.client.annotator.interlinear.edit.render;

/*
 * Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;

import javax.swing.JViewport;

/**
 * <b>This is a modified version of FlowLayout</b>
 * based on the GPL version of the Sun openjdk 1.6, which has removed some
 * unneeded features for the use case (such as baseline outlining and
 * serialisation). Added is the capability to handle components that may be a
 * different size depending on their position in the layout. Also, normally
 * an layout that is very sensitive to the available width doesn't play well
 * in a JScrollPane. This layout looks at the available width in the viewport
 * in such a case.
 * <p>
 * The hgap and vgap are only used between the Components, and not between
 * the container and the components. The insets are much more suited to that.
 * <p>
 * It was complicated to create a subclass of FlowLayout because an overridden
 * layoutContainer() would still need access to a private moveComponents().
 * <p>
 * The source has been reformatted in Eclipse.
 * <p>
 * 
 * A flow layout arranges components in a directional flow, much like lines of
 * text in a paragraph. The flow direction is determined by the container's
 * <code>componentOrientation</code> property and may be one of two values:
 * <ul>
 * <li><code>ComponentOrientation.LEFT_TO_RIGHT</code>
 * <li><code>ComponentOrientation.RIGHT_TO_LEFT</code>
 * </ul>
 * Flow layouts are typically used to arrange buttons in a panel. It arranges
 * buttons horizontally until no more buttons fit on the same line. The line
 * alignment is determined by the <code>align</code> property. The possible
 * values are:
 * <ul>
 * <li>{@link #LEFT LEFT}
 * <li>{@link #RIGHT RIGHT}
 * <li>{@link #CENTER CENTER}
 * </ul>
 * <p>
 * For example, the following picture shows an applet using the flow layout
 * manager (its default layout manager) to position three buttons:
 * <p>
 * <img src="doc-files/FlowLayout-1.gif"
 * ALT="Graphic of Layout for Three Buttons" ALIGN=center HSPACE=10 VSPACE=7>
 * <p>
 * Here is the code for this applet:
 * <p>
 * <hr>
 * <blockquote>
 * 
 * <pre>
 * import java.awt.*;
 * import java.applet.Applet;
 * 
 * public class myButtons extends Applet {
 * 	Button button1, button2, button3;
 * 
 * 	public void init() {
 * 		button1 = new Button(&quot;Ok&quot;);
 * 		button2 = new Button(&quot;Open&quot;);
 * 		button3 = new Button(&quot;Close&quot;);
 * 		add(button1);
 * 		add(button2);
 * 		add(button3);
 * 	}
 * }
 * </pre>
 * 
 * </blockquote>
 * <hr>
 * <p>
 * A flow layout lets each component assume its natural (preferred) size.
 * 
 * @version %I%, %G%
 * @author Arthur van Hoff
 * @author Sami Shaio
 * @author Olaf Seibert
 * @see ComponentOrientation
 */

public class SuggestionFlowLayout implements LayoutManager {

	/**
	 * This value indicates that each row of components should be
	 * left-justified.
	 */
	public static final int LEFT = 0;

	/**
	 * This value indicates that each row of components should be centered.
	 */
	public static final int CENTER = 1;

	/**
	 * This value indicates that each row of components should be
	 * right-justified.
	 */
	public static final int RIGHT = 2;
	
	public static final int MIN_WIDTH = 120;
	public static final int MIN_HEIGHT = 75;
	
	/**
	 * Record the dimension, so that a scroll panel can know when to
	 * deploy scrollbars.
	 */
	private int width = MIN_WIDTH, height = MIN_HEIGHT;

	/**
	 * Some components may want to change their size depending on the
	 * location they get.
	 * Allow for them to know it.
	 * 
	 * @author olasei
	 */
	public interface Location {
		void setGrid(int col, int row);
		public Dimension getPreferredSize(int col, int row);
	}

	/**
	 * <code>newAlign</code> is the property that determines how each row
	 * distributes empty space for the Java 2 platform, v1.2 and greater. It can
	 * be one of the following three values:
	 * <ul>
	 * <code>LEFT</code> <code>RIGHT</code> <code>CENTER</code>
	 * </ul>
	 *
	 * @see #getAlignment
	 * @see #setAlignment
	 */
	int newAlign; // This is the one we actually use

	/**
	 * The flow layout manager allows a separation of components with gaps. The
	 * horizontal gap will specify the space between components. The space between the
	 * components and the borders of the <code>Container</code> is determined by the insets
	 * of the <code>Container</code>.
	 *
	 * @see #getHgap()
	 * @see #setHgap(int)
	 */
	int hgap;

	/**
	 * The flow layout manager allows a separation of components with gaps. The
	 * vertical gap will specify the space between rows. The space between the the
	 * components and the borders of the <code>Container</code> is determined by the insets
	 * of the <code>Container</code>.
	 *
	 * @see #getHgap()
	 * @see #setHgap(int)
	 */
	int vgap;

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 */
	public SuggestionFlowLayout() {
		this(CENTER, 5, 5);
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with the specified alignment and
	 * a default 5-unit horizontal and vertical gap. The value of the alignment
	 * argument must be one of <code>FlowLayout.LEFT</code>,
	 * <code>FlowLayout.RIGHT</code>, <code>FlowLayout.CENTER</code>,
	 * <code>FlowLayout.LEADING</code>, or <code>FlowLayout.TRAILING</code>.
	 * 
	 * @param align
	 *            the alignment value
	 */
	public SuggestionFlowLayout(int align) {
		this(align, 5, 5);
	}

	/**
	 * Creates a new flow layout manager with the indicated alignment and the
	 * indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of
	 * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
	 * <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
	 * <code>FlowLayout.TRAILING</code>.
	 * 
	 * @param align
	 *            the alignment value
	 * @param hgap
	 *            the horizontal gap between components and between the
	 *            components and the borders of the <code>Container</code>
	 * @param vgap
	 *            the vertical gap between components and between the components
	 *            and the borders of the <code>Container</code>
	 */
	public SuggestionFlowLayout(int align, int hgap, int vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
		setAlignment(align);
	}

	/**
	 * Gets the alignment for this layout. Possible values are
	 * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
	 * <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
	 * <code>FlowLayout.TRAILING</code>.
	 * 
	 * @return the alignment value for this layout
	 * @see java.awt.FlowLayout#setAlignment
	 * @since JDK1.1
	 */
	public int getAlignment() {
		return newAlign;
	}

	/**
	 * Sets the alignment for this layout. Possible values are
	 * <ul>
	 * <li><code>FlowLayout.LEFT</code>
	 * <li><code>FlowLayout.RIGHT</code>
	 * <li><code>FlowLayout.CENTER</code>
	 * <li><code>FlowLayout.LEADING</code>
	 * <li><code>FlowLayout.TRAILING</code>
	 * </ul>
	 * 
	 * @param align
	 *            one of the alignment values shown above
	 * @see #getAlignment()
	 * @since JDK1.1
	 */
	public void setAlignment(int align) {
		this.newAlign = align;
	}

	/**
	 * Gets the horizontal gap between components and between the components and
	 * the borders of the <code>Container</code>
	 *
	 * @return the horizontal gap between components and between the components
	 *         and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#setHgap
	 * @since JDK1.1
	 */
	public int getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between components and between the components and
	 * the borders of the <code>Container</code>.
	 *
	 * @param hgap
	 *            the horizontal gap between components and between the
	 *            components and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#getHgap
	 * @since JDK1.1
	 */
	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	/**
	 * Gets the vertical gap between components and between the components and
	 * the borders of the <code>Container</code>.
	 *
	 * @return the vertical gap between components and between the components
	 *         and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#setVgap
	 * @since JDK1.1
	 */
	public int getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between components and between the components and
	 * the borders of the <code>Container</code>.
	 *
	 * @param vgap
	 *            the vertical gap between components and between the components
	 *            and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#getVgap
	 * @since JDK1.1
	 */
	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	/**
	 * Adds the specified component to the layout. Not used by this class.
	 * 
	 * @param name
	 *            the name of the component
	 * @param comp
	 *            the component to be added
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * Removes the specified component from the layout. Not used by this class.
	 * 
	 * @param comp
	 *            the component to remove
	 * @see java.awt.Container#removeAll
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
	}

	/**
	 * Returns the preferred dimensions for this layout given the <i>visible</i>
	 * components in the specified target container.
	 *
	 * @param target
	 *            the container that needs to be laid out
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 * @see Container
	 * @see #minimumLayoutSize
	 * @see java.awt.Container#getPreferredSize
	 */
	@Override
	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();
			boolean firstVisibleComponent = true;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Location ml = null;
					if (m instanceof Location) {
						ml = (Location)m;
					}
					Dimension d = (ml != null) 
							    ? ml.getPreferredSize(i, 0) 
							    : m.getPreferredSize();
					dim.height = Math.max(dim.height, d.height);
					if (firstVisibleComponent) {
						firstVisibleComponent = false;
					} else {
						dim.width += hgap;
					}
					dim.width += d.width;
				}
			}
			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right;
			dim.height += insets.top + insets.bottom;
			return dim;
		}
	}

	/**
	 * Returns the minimum dimensions needed to layout the <i>visible</i>
	 * components contained in the specified target container.
	 * 
	 * @param target
	 *            the container that needs to be laid out
	 * @return the minimum dimensions to lay out the subcomponents of the
	 *         specified container
	 * @see #preferredLayoutSize
	 * @see java.awt.Container
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();
			boolean firstVisibleComponent = true;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getMinimumSize();
					dim.height = Math.max(dim.height, d.height);
					if (firstVisibleComponent) {
						firstVisibleComponent = false;
					} else {
						dim.width += hgap;
					}
					dim.width += d.width;
				}
			}

			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right;
			dim.height += insets.top + insets.bottom;
			return dim;

		}
	}

	/**
	 * Centers the elements in the specified row, if there is any slack.
	 * 
	 * @param target
	 *            the component which needs to be moved
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param width
	 *            the width dimensions
	 * @param height
	 *            the height dimensions
	 * @param rowStart
	 *            the beginning of the row
	 * @param rowEnd
	 *            the the ending of the row (exclusive)
	 * @return actual row height
	 */
	private int moveComponents(Container target, int x, int y, int width,
			int height, int rowStart, int rowEnd, boolean ltr) {
		switch (newAlign) {
		case LEFT:
			x += ltr ? 0 : width;
			break;
		case CENTER:
			x += width / 2;
			break;
		case RIGHT:
			x += ltr ? width : 0;
			break;
		}

		for (int i = rowStart; i < rowEnd; i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				int cy;
				cy = y + (height - m.getHeight()) / 2;
				if (ltr) {
					setLocation(m, x, cy);
				} else {
					setLocation(m, target.getWidth() - x - m.getWidth(), cy);
				}
				x += m.getWidth() + hgap;
			}
		}
		return height;
	}
	
	/**
	 * Set the location of a component but only if it is actually changing.
	 * 
	 * @param c
	 * @param x
	 * @param y
	 */
	private void setLocation(Component c, int x, int y) {
		Point p = c.getLocation();
		if (p.x != x || p.y != y) {
			c.setLocation(x, y);
		}
	}

	/**
	 * Lays out the container. This method lets each <i>visible</i> component
	 * take its preferred size by reshaping the components in the target
	 * container in order to satisfy the alignment of this
	 * <code>FlowLayout</code> object.
	 *
	 * @param target
	 *            the specified component being laid out
	 * @see Container
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			/* If we're in a JScrollPane, look at the really visible available width */
			Container scrollpane = target.getParent();
			if (!(scrollpane instanceof JViewport)) {
				scrollpane = target;
			}
			int maxwidth = scrollpane.getWidth()
					- (insets.left + insets.right + hgap);
			int nmembers = target.getComponentCount();
			int x = 0, y = insets.top;
			int rowh = 0, start = 0;
			int row = 0, col = 0;
			width = MIN_WIDTH;
			height = MIN_HEIGHT;

			boolean ltr = target.getComponentOrientation().isLeftToRight();

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Location ml = null;
					if (m instanceof Location) {
						ml = (Location)m;
					}
					Dimension d = (ml != null) 
							    ? ml.getPreferredSize(col, row) 
							    : m.getPreferredSize();
					m.setSize(d.width, d.height);

					if ((x == 0) || ((x + d.width) <= maxwidth)) {
						if (x > 0) {
							x += hgap;
						}
						x += d.width;
						rowh = Math.max(rowh, d.height);
					} else {
						// move the previous components into place
						rowh = moveComponents(target, insets.left, y,
								maxwidth - x, rowh, start, i, ltr);
						// Component nr i is going to be wrapped;
						// determine what size it likes to be in that position.
						col = 0;
						row++;
						if (ml != null) {
							d = ml.getPreferredSize(col, row);
						}
						x = d.width;
						y += vgap + rowh;
						rowh = d.height;
						start = i;
					}
					if (ml != null) {
						ml.setGrid(col, row);
					}
					col++;
					width = Math.max(width, x);
				}
			}
			rowh = moveComponents(target, insets.left, y, maxwidth - x, rowh,
					start, nmembers, ltr);
			
			height = Math.max(height, y + rowh);
			width = Math.max(width, x);
		}
	}

	/**
	 * Returns a string representation of this <code>FlowLayout</code> object
	 * and its values.
	 * 
	 * @return a string representation of this layout
	 */
	@Override
	public String toString() {
		String str = "";
		switch (newAlign) {
		case LEFT:
			str = ",align=left";
			break;
		case CENTER:
			str = ",align=center";
			break;
		case RIGHT:
			str = ",align=right";
			break;
		}
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str
				+ "]";
	}
	
	/**
	 * The component which uses this Layout can ask us what the current size is.
	 */
	public Dimension getSize() {
		return new Dimension(width, height);
	}
}
