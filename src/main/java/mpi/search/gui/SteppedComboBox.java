package mpi.search.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;


/**
 * SteppedComboBox is a ComboBox with fixed size of title item, but variable
 * size of the PopupMenu
 *
 * @version 1.0 12/12/98
 */
@SuppressWarnings("serial")
public class SteppedComboBox extends JComboBox {
    /** Holds value of property DOCUMENT ME! */
    protected int popupWidth;

    /**
     * Creates a new SteppedComboBox instance
     *
     * @param aModel DOCUMENT ME!
     */
    public SteppedComboBox(ComboBoxModel aModel) {
        super(aModel);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    /**
     * Creates a new SteppedComboBox instance
     *
     * @param items DOCUMENT ME!
     */
    public SteppedComboBox(final Object[] items) {
        super(items);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    /**
     * Creates a new SteppedComboBox instance
     *
     * @param items DOCUMENT ME!
     */
    public SteppedComboBox(Vector items) {
        super(items);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    /**
     * Creates a new SteppedComboBox instance
     */
    public SteppedComboBox() {
        super();
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param width DOCUMENT ME!
     */
    public void setPopupWidth(int width) {
        popupWidth = width;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPopupSize() {
        Dimension size = getSize();

        if (popupWidth < 1) {
            popupWidth = size.width;
        }

        return new Dimension(popupWidth, size.height);
    }
}


/**
 * DOCUMENT ME!
 * $Id: SteppedComboBox.java 20115 2010-09-29 12:34:59Z wilelb $
 * @author $Author$
 * @version $Revision$
 */
class SteppedComboBoxUI extends MetalComboBoxUI {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected ComboPopup createPopup() {
        @SuppressWarnings("serial")
		BasicComboPopup popup = new BasicComboPopup(comboBox) {
                // setVisible(boolean) replaces show() and hide() in Java 1.1
                @Override
				public void setVisible(boolean visible) {
                    if (!visible) return;
                    Dimension popupSize = ((SteppedComboBox) comboBox).getPopupSize();
                    popupSize.setSize(popupSize.width,
                        getPopupHeightForRowCount(comboBox.getMaximumRowCount()));

                    Rectangle popupBounds = computePopupBounds(0,
                            comboBox.getBounds().height, popupSize.width,
                            popupSize.height);
                    scroller.setMaximumSize(popupBounds.getSize());
                    scroller.setPreferredSize(popupBounds.getSize());
                    scroller.setMinimumSize(popupBounds.getSize());
                    list.invalidate();

                    int selectedIndex = comboBox.getSelectedIndex();

                    if (selectedIndex == -1) {
                        list.clearSelection();
                    } else {
                        list.setSelectedIndex(selectedIndex);
                    }

                    list.ensureIndexIsVisible(list.getSelectedIndex());
                    setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

                    show(comboBox, popupBounds.x, popupBounds.y);
                }
            };

        popup.getAccessibleContext().setAccessibleParent(comboBox);

        return popup;
    }
}
