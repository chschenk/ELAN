package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;


@SuppressWarnings("serial")
public class CheckboxTreeCellRenderer extends JCheckBox implements
        TreeCellRenderer {

    //  Colors
    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;

    /** Color to use for the focus indicator when the node has focus. */
    protected Color borderSelectionColor;
    
    /**
     * 
     */
    public CheckboxTreeCellRenderer() {
        super();
        initColors();
    }

    /**
     * @param icon
     */
    public CheckboxTreeCellRenderer(Icon icon) {
        super(icon);
        initColors();
    }
    
    private void initColors() {
        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
	}

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
       if (value instanceof DefaultMutableTreeNode) {
            Object uO = ((DefaultMutableTreeNode)value).getUserObject();
            if (uO instanceof String) {
                setText(value.toString());
                setSelected(false);
            } else if (uO instanceof SelectableObject) {
                setText(uO.toString());
                setSelected(((SelectableObject)uO).isSelected());
            }
        } else if (value instanceof String) {
            setText((String) value);
            setSelected(false);
        } 
        
        setEnabled(tree.isEnabled());
        //setFont(tree.getFont());
        
        if (selected) {
            setForeground(getTextSelectionColor());
            setBackground(getBackgroundSelectionColor());
        } else {
            setForeground(getTextNonSelectionColor());
            setBackground(getBackgroundNonSelectionColor());
        }

        setComponentOrientation(tree.getComponentOrientation());
        return this;
    }
    
    // copied from DefaultTreeCellRenderer
    /**
     * Subclassed to map <code>FontUIResource</code>s to null. If 
     * <code>font</code> is null, or a <code>FontUIResource</code>, this
     * has the effect of letting the font of the JTree show
     * through. On the other hand, if <code>font</code> is non-null, and not
     * a <code>FontUIResource</code>, the font becomes <code>font</code>.
     */
    @Override
	public void setFont(Font font) {
        if(font instanceof FontUIResource)
            font = null;
        super.setFont(font);
    }

    /**
     * Subclassed to map <code>ColorUIResource</code>s to null. If 
     * <code>color</code> is null, or a <code>ColorUIResource</code>, this
     * has the effect of letting the background color of the JTree show
     * through. On the other hand, if <code>color</code> is non-null, and not
     * a <code>ColorUIResource</code>, the background becomes
     * <code>color</code>.
     */
    @Override
	public void setBackground(Color color) {
	if(color instanceof ColorUIResource)
	    color = null;
		super.setBackground(color);
    }
    /**
     * Sets the color the text is drawn with when the node is selected.
     */
   public void setTextSelectionColor(Color newColor) {
	textSelectionColor = newColor;
   }

   /**
     * Returns the color the text is drawn with when the node is selected.
     */
   public Color getTextSelectionColor() {
	return textSelectionColor;
   }

   /**
     * Sets the color the text is drawn with when the node isn't selected.
     */
   public void setTextNonSelectionColor(Color newColor) {
	textNonSelectionColor = newColor;
   }

   /**
     * Returns the color the text is drawn with when the node isn't selected.
     */
   public Color getTextNonSelectionColor() {
	return textNonSelectionColor;
   }

   /**
     * Sets the color to use for the background if node is selected.
     */
   public void setBackgroundSelectionColor(Color newColor) {
	backgroundSelectionColor = newColor;
   }

   /**
     * Returns the color to use for the background if node is selected.
     */
   public Color getBackgroundSelectionColor() {
	return backgroundSelectionColor;
   }

   /**
     * Sets the background color to be used for non selected nodes.
     */
   public void setBackgroundNonSelectionColor(Color newColor) {
	backgroundNonSelectionColor = newColor;
   }

   /**
     * Returns the background color to be used for non selected nodes.
     */
   public Color getBackgroundNonSelectionColor() {
	return backgroundNonSelectionColor;
   }

    /**
     * Overridden for performance reasons.
     * See the DefaultTreeCellRenderer
     */
     @Override
	public void validate() {}
     @Override
	public void revalidate() {}
     @Override
	public void repaint(long tm, int x, int y, int width, int height) {}
     @Override
	public void repaint(Rectangle r) {}
     @Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
     @Override
	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
     @Override
	public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
     @Override
	public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
     @Override
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
     @Override
	public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
     @Override
	public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
     @Override
	public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
     @Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
