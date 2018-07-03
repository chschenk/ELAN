package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;


public class CheckboxTreeCellEditor extends JCheckBox implements
        TreeCellEditor, ItemListener {
    //  Colors
    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;
    
    private Object uObject;
    private boolean edited = false;
    private JTree tree;
    
    public CheckboxTreeCellEditor() {
        super();
        addItemListener(this);
        initColors();
    }

    private void initColors() {
        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
	}
   
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int)
     */
    @Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.tree = tree;

        if (value instanceof DefaultMutableTreeNode) {
            uObject = ((DefaultMutableTreeNode)value).getUserObject();
            if (uObject instanceof String) {
                setText(uObject.toString());
                setSelected(false);
            } else if (uObject instanceof SelectableObject) {
                setText(uObject.toString());
                setSelected(((SelectableObject)uObject).isSelected());
            }
        } else if (value instanceof String) {
            setText((String) value);
            setSelected(false);
        } 
        edited = false;
        
        if (isSelected) {
            setForeground(getTextSelectionColor());
            setBackground(getBackgroundSelectionColor());
        } else {
            setForeground(getTextNonSelectionColor());
            setBackground(getBackgroundNonSelectionColor());
        }

        setComponentOrientation(tree.getComponentOrientation());
        return this;
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
     * Returns the last object passed to the getTreeCellEditorComponent method.
     * Can be null.
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
	public Object getCellEditorValue() {
        return uObject;
    }

    /**
     * Returns true for now; could check the kind of object in the selected treepath. 
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    @Override
	public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    /**
     * Returns true.
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    @Override
	public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * Returns true; always accept.
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    @Override
	public boolean stopCellEditing() {
        return true;
    }

    /**
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    @Override
	public void cancelCellEditing() {
    }

    /**
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
	public void addCellEditorListener(CellEditorListener l) {
    }

    /**
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
	public void removeCellEditorListener(CellEditorListener l) {
    }

    /**
     * Updates the current value with the selected state of the checkbox.
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (uObject instanceof SelectableObject && !edited) {
            //((SelectableString)uo).setSelected( !((SelectableString)uo).isSelected());
            ((SelectableObject)uObject).setSelected(isSelected());
            edited = true;
        }
        if (tree != null) {
            tree.stopEditing();
        }
    }

}
