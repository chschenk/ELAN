/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;


/**
 * $Id: TriptychLayout.java 20115 2010-09-29 12:34:59Z wilelb $ This class performs a
 * horizontally layout of three components.  The central component will always be exactly in the
 * center and will be set to its preferred size. The remaining space is distributed equally to the
 * components on the left and on the right. The height is set equal to the maximum height of the
 * three components;
 *
 * @author $Author$
 * @version $Revision$
 */
public class TriptychLayout implements LayoutManager {
    /** Position in center */
    public static final String CENTER = "Center";

    /** Position left to center */
    public static final String LEFT = "Left";

    /** Position right to center */
    public static final String RIGHT = "Right";
    private Component[] components = new Component[3];

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.getContentPane().setLayout(new TriptychLayout());
        frame.getContentPane().add(LEFT, new javax.swing.JButton("Button 1"));
        frame.getContentPane().add(CENTER, new javax.swing.JButton("Button 2"));
        frame.getContentPane().add(RIGHT, new javax.swing.JButton("Button 3"));
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param comp DOCUMENT ME!
     */
    @Override
	public void addLayoutComponent(String name, Component comp) {
        if ((name == null) || name.equals("")) {
            for (int i = 0; i < 3; i++) {
                if (components[i] == null) {
                    components[i] = comp;

                    break;
                }
            }
        }

        else {
            if (LEFT.equals(name)) {
                components[0] = comp;
            }

            if (CENTER.equals(name)) {
                components[1] = comp;
            }

            if (RIGHT.equals(name)) {
                components[2] = comp;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    @Override
	public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int parentWidth = parent.getSize().width;

        while ((parentWidth == 0) && (parent.getParent() != null)) {
            parent = parent.getParent();
            parentWidth = parent.getSize().width;
        }

        parentWidth -= (insets.left + insets.right);

        int height = getPreferredHeight();

        int middleWidth = 0;

        if (components[1] != null) {
            middleWidth = components[1].getPreferredSize().width;

            int x = Math.max(0, (parentWidth - middleWidth) / 2);
            components[1].setBounds(x + insets.left, insets.top, middleWidth, height);
        }

        if (components[0] != null) {
            components[0].setBounds(
                insets.left, insets.top, (parentWidth - middleWidth) / 2, height);
        }

        if (components[2] != null) {
            components[2].setBounds(
                insets.left + ((parentWidth + middleWidth) / 2), insets.top,
                (parentWidth - middleWidth) / 2, height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension minimumLayoutSize(Container parent) {
        return (components[1] != null) ? components[1].getMinimumSize() : new Dimension(0, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(getPreferredWidth(), getPreferredHeight());
    }

    /**
     * DOCUMENT ME!
     *
     * @param comp DOCUMENT ME!
     */
    @Override
	public void removeLayoutComponent(Component comp) {
        for (int i = 0; i < 3; i++) {
            if (components[i] == comp) {
                components[i] = null;
            }
        }
    }

    private int getPreferredHeight() {
        int preferredHeight = 0;

        for (int i = 0; i < 3; i++) {
            if (components[i] != null) {
                preferredHeight = Math.max(
                        preferredHeight, components[i].getPreferredSize().height);
            }
        }

        return preferredHeight;
    }

    private int getPreferredWidth() {
        int preferredWidth = 0;

        for (int i = 0; i < 3; i++) {
            if (components[i] != null) {
                preferredWidth += components[i].getPreferredSize().width;
            }
        }

        return preferredWidth;
    }
}
