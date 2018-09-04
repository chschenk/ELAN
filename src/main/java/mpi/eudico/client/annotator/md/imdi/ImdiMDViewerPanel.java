package mpi.eudico.client.annotator.md.imdi;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.md.DefaultMDViewerComponent;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;


/**
 * Metadata viewer for IMDI metadata. Currently extends the
 * DefaultMDViewerComponent  by setting a custom table renderer for the
 * metadata keys. It might be necessary to have more flexibility in the
 * future. This viewer could then extend JPanel and implement
 * MDViewerComponent. A tree view could be offered as an alternative in that
 * case.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ImdiMDViewerPanel extends DefaultMDViewerComponent
    implements ActionListener, MouseListener, ComponentListener {
    /** Holds value of property DOCUMENT ME! */
    protected JTree tree;
    private final int TABLE = 0;
    private final int TREE = 1;
    private int mode;
    private JPopupMenu popup;
    private JRadioButtonMenuItem tableMI;
    private JRadioButtonMenuItem treeMI;
    private JCheckBoxMenuItem hideEmptyMI;
    private boolean hideEmptyRows;

    /**
     * Creates a new ImdiMDViewerPanel instance
     */
    private ImdiMDViewerPanel() {
        super();
    }

    /**
     * Creates a new ImdiMDViewerPanel instance
     *
     * @param provider the metadata provider
     */
    public ImdiMDViewerPanel(MDServiceProvider provider) {
        super(provider);
    }

    /**
     * Initializes the components. Adds a custom renderer to the table.
     */
    @Override
	protected void initComponents() {
    	mode = TABLE;
        String stringPref = Preferences.getString("Metadata.IMDI.ViewMode", null);

        if (stringPref != null) {
            if ("Tree".equals(stringPref)) {
                mode = TREE;
            }
        }
        Boolean boolPref = Preferences.getBool("Metadata.HideEmptyValues", null);
        
        if (boolPref != null) {
        	hideEmptyRows = boolPref.booleanValue();
        } else {
        	hideEmptyRows = false;
        }
        
        addComponentListener(this);
        //super.initComponents();
        scrollPane = new JScrollPane();
        scrollPane.addComponentListener(this);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        add(scrollPane, gbc);

        if (mode == TABLE) {
            model = new DefaultTableModel(1, 2);
            model.setColumnIdentifiers(new String[] { keyColumn, valColumn });
            mdTable = new MDTable(model);
            mdTable.getTableHeader().setReorderingAllowed(false);
            //mdTable.setEnabled(false);
            //mdTable.getColumn(keyColumn).setCellRenderer(new ImdiKeyRenderer());

            //MultiLineValueRenderer valueRenderer = new MultiLineValueRenderer(true);
            //mdTable.getColumn(valColumn).setCellRenderer(valueRenderer);
            mdTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            scrollPane.setViewportView(mdTable);
            scrollPane.getVerticalScrollBar().setUnitIncrement(mdTable.getRowHeight());
            mdTable.addMouseListener(this);
        } else {
            //tree = new JTree(new DefaultMutableTreeNode(ImdiConstants.SESSION));
        	tree = new MDTree(new DefaultMutableTreeNode(ImdiConstants.SESSION));
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setOpenIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setClosedIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setLeafIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setTextNonSelectionColor(Color.BLACK);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setTextSelectionColor(Color.BLACK);
            tree.setCellRenderer(new MDTreeCellRenderer());
            tree.setRowHeight(-1);// variable row height
            //tree.setEditable(false);
            //tree.setEnabled(false);
            //tree.putClientProperty("JTree.lineStyle", "Horizontal");
            scrollPane.setViewportView(tree);
            scrollPane.getVerticalScrollBar().setUnitIncrement(tree.getFont().getSize());
            ((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth());
            ((MDTree)tree).forceUIUpdate();
            tree.addMouseListener(this);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.md.DefaultMDViewerComponent#setSelectedKeysAndValues(java.util.Map)
     */
    @Override
	public void setSelectedKeysAndValues(Map<String, String> keysAndValuesMap) {
        if (mode == TREE) {
            /*
               if (tree == null) {
                   tree = new JTree(((ImdiFileServiceProvider) provider).getSelectedAsTree());
                   ((DefaultTreeCellRenderer) tree.getCellRenderer()).setOpenIcon(null);
                   ((DefaultTreeCellRenderer) tree.getCellRenderer()).setClosedIcon(null);
                   ((DefaultTreeCellRenderer) tree.getCellRenderer()).setLeafIcon(null);
                   tree.setEditable(false);
                   tree.setEnabled(false);
                   //tree.putClientProperty("JTree.lineStyle", "Horizontal");
                   scrollPane.setViewportView(tree);
                   expandAllRows(tree);
                   if (mdTable != null) {
                       mdTable.removeMouseListener(this);
                   }
                   tree.addMouseListener(this);
               } else {*/
        	DefaultMutableTreeNode rootNode = ((ImdiFileServiceProvider) provider).getSelectedAsTree();
            if (hideEmptyRows) {
            	removeEmptyLeaves(rootNode);
            }
            ((DefaultTreeModel) tree.getModel()).setRoot(rootNode);
            expandAllRows(tree);

            //}
        } else {
            super.setSelectedKeysAndValues(keysAndValuesMap);
            // to do: this could better be done in the service provider, would need an extra parameter
            // or setting
            if (hideEmptyRows) {
            	removeEmptyRows();
            }
        }
    }
    
    /**
     * Post processes a node by removing leaves with empty values from the tree.
     * 
     * @param node the node to process
     */
    private void removeEmptyLeaves(DefaultMutableTreeNode node) {
    	if (node == null) {
    		return;
    	}
    	int numCH = node.getChildCount();
    	if (numCH == 0) {
    		return;
    	}
    	DefaultMutableTreeNode n;
    	Object data;
    	MDKVData mdkv;
    	for (int i = numCH - 1; i >= 0; i--) {
    		n = (DefaultMutableTreeNode) node.getChildAt(i);
    		if (n.isLeaf()) {
    			data = n.getUserObject();
    			if (data instanceof MDKVData) {
    				mdkv = (MDKVData) data;
    				if (mdkv.value == null || mdkv.value.length() == 0) {
    					node.remove(n);
    				}
    			}
    		} else {
    			removeEmptyLeaves(n);
    			if (n.getChildCount() == 0) {
    				data = n.getUserObject();
        			if (data instanceof MDKVData) {
        				mdkv = (MDKVData) data;
        				if (mdkv.value == null || mdkv.value.length() == 0) {
        					node.remove(n);
        				}
        			}
    			}
    		}
    	}
    }

    private void expandAllRows(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
    
    /**
     * Removes rows from an existing table (model) that have no metadata value.
     * I.e. the second column has a null object or an empty string 
     */
    private void removeEmptyRows() {
    	if (mdTable != null) {
        	DefaultTableModel dtm = (DefaultTableModel) mdTable.getModel();
        	int numRows = dtm.getRowCount();
        	String val;
        	for (int i = numRows - 1; i >= 0; i--) {
        		val = (String) dtm.getValueAt(i, 1);
        		if (val == null || val.length() == 0) {
        			dtm.removeRow(i);
        		}
        	}
    	}
    }

    private void createPopupMenu() {
        popup = new JPopupMenu("Popup");

        ButtonGroup group = new ButtonGroup();
        tableMI = new JRadioButtonMenuItem();
        tableMI.setSelected(mode == TABLE);
        group.add(tableMI);
        tableMI.addActionListener(this);
        popup.add(tableMI);
        treeMI = new JRadioButtonMenuItem();
        treeMI.setSelected(mode == TREE);
        group.add(treeMI);
        treeMI.addActionListener(this);
        popup.add(treeMI);
        popup.addSeparator();
        hideEmptyMI = new JCheckBoxMenuItem();
        hideEmptyMI.setSelected(hideEmptyRows);
        hideEmptyMI.addActionListener(this);
        popup.add(hideEmptyMI);

        if (bundle != null) {
            String text = bundle.getString("MetadataViewer.TableView");

            if (text != null) {
                tableMI.setText(text);
            } else {
                tableMI.setText("Table View");
            }

            text = bundle.getString("MetadataViewer.TreeView");

            if (text != null) {
                treeMI.setText(text);
            } else {
                treeMI.setText("Tree View");
            }
            
            text = bundle.getString("MetadataViewer.HideEmptyValues");
            
            if (text != null) {
            	hideEmptyMI.setText(text);
            } else {
            	hideEmptyMI.setText("Hide Empty Metadata Fields");
            }
        } else {
            tableMI.setText("Table View");
            treeMI.setText("Tree View");
            hideEmptyMI.setText("Hide Empty Metadata Fields");
        }
    }

    /**
     * @see mpi.eudico.client.annotator.md.DefaultMDViewerComponent#setResourceBundle(java.util.ResourceBundle)
     */
    @Override
    public void setResourceBundle(ResourceBundle bundle) {
        super.setResourceBundle(bundle);

        // update popup menu
        if (popup != null) {
            String text = bundle.getString("MetadataViewer.TableView");

            if (text != null) {
                tableMI.setText(text);
            } else {
                tableMI.setText("Table View");
            }

            text = bundle.getString("MetadataViewer.TreeView");

            if (text != null) {
                treeMI.setText(text);
            } else {
                treeMI.setText("Tree View");
            }
            
            text = bundle.getString("MetadataViewer.HideEmptyValues");
            
            if (text != null) {
            	hideEmptyMI.setText(text);
            } else {
            	hideEmptyMI.setText("Hide Empty Metadata Fields");
            }
        }
    }

    /**
     * Menu items action handling.
     *
     * @param e event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == tableMI) {
        	if (mode == TABLE) {
        		return;
        	}
            mode = TABLE;

            if (tree != null) {
                tree.removeMouseListener(this);
            }

            model = new DefaultTableModel(0, 2);
            model.setColumnIdentifiers(new String[] { keyColumn, valColumn });
            mdTable = new MDTable(model);
            mdTable.getTableHeader().setReorderingAllowed(false);
            //mdTable.setEnabled(false);
            //mdTable.getColumn(keyColumn).setCellRenderer(new ImdiKeyRenderer());
            //MultiLineValueRenderer valueRenderer = new MultiLineValueRenderer(true);
            //mdTable.getColumn(valColumn).setCellRenderer(valueRenderer);
            mdTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            scrollPane.setViewportView(mdTable);
            scrollPane.getVerticalScrollBar().setUnitIncrement(mdTable.getRowHeight());
            mdTable.addMouseListener(this);
            setSelectedKeysAndValues(provider.getSelectedKeysAndValues());
            tree = null;
            Preferences.set("Metadata.IMDI.ViewMode", "Table", null, false, false);
        } else if (e.getSource() == treeMI) {
        	if (mode == TREE) {
        		return;
        	}
            mode = TREE;

            if (mdTable != null) {
                mdTable.removeMouseListener(this);
            }

            //tree = new JTree(new DefaultMutableTreeNode(ImdiConstants.SESSION));
            tree = new MDTree(new DefaultMutableTreeNode(ImdiConstants.SESSION));
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setOpenIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setClosedIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setLeafIcon(null);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setTextNonSelectionColor(Color.BLACK);
//            ((DefaultTreeCellRenderer) tree.getCellRenderer()).setTextSelectionColor(Color.BLACK);
            tree.setCellRenderer(new MDTreeCellRenderer());
            tree.setRowHeight(-1);
            //tree.setEditable(false);
            //tree.setEnabled(false);
            //tree.putClientProperty("JTree.lineStyle", "Horizontal");
            scrollPane.setViewportView(tree);
            scrollPane.getVerticalScrollBar().setUnitIncrement(tree.getFont().getSize());
            tree.addMouseListener(this);
            setSelectedKeysAndValues(null);
            ((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth());
            ((MDTree)tree).forceUIUpdate();
            mdTable = null;
            Preferences.set("Metadata.IMDI.ViewMode", "Tree", null, false, false);
        } else if (e.getSource() == hideEmptyMI) {
        	boolean sel = hideEmptyMI.isSelected();
        	if (hideEmptyRows != sel) {
        		hideEmptyRows = sel;
        		if (mode == TREE) {
        			if (hideEmptyRows) {// adjust existing tree
        				DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        				removeEmptyLeaves(root);
        				// without setting the root the tree is not (properly) updated
        				((DefaultTreeModel) tree.getModel()).setRoot(root);
        				expandAllRows(tree);
        			} else {
        				setSelectedKeysAndValues(null);
        			}
                    ((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth());
                    ((MDTree)tree).forceUIUpdate();
        		} else {// table
        			if (hideEmptyRows) {
        				removeEmptyRows();
        			} else {
        				setSelectedKeysAndValues(provider.getSelectedKeysAndValues());
        			}
        		}
        	}
        	Preferences.set("Metadata.HideEmptyValues", Boolean.valueOf(hideEmptyRows), null, false, false);
        }
    }

    /**
     * Shows the popup menu.
     *
     * @param e event
     */
    @Override
	public void mousePressed(MouseEvent e) {
        if ((SwingUtilities.isRightMouseButton(e) &&
                ((e.getButton() == MouseEvent.BUTTON1) ^ e.isMetaDown())) ||
                e.isPopupTrigger()) {
            if (popup == null) {
                createPopupMenu();
            }

            Point pp = e.getPoint();
            pp.move(pp.x, pp.y - scrollPane.getVerticalScrollBar().getValue());

            if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
                popup.show(this, pp.x, pp.y);
            } else {
                popup.show(this, pp.x, pp.y);
                SwingUtilities.convertPointToScreen(pp, ImdiMDViewerPanel.this);

                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                Window w = SwingUtilities.windowForComponent(ImdiMDViewerPanel.this);

                if ((pp.x + popup.getWidth()) > d.width) {
                    pp.x -= popup.getWidth();
                }

                //this does not account for a desktop taskbar
                if ((pp.y + popup.getHeight()) > d.height) {
                    pp.y -= popup.getHeight();
                }

                //keep it in the window then
                if ((pp.y + popup.getHeight()) > (w.getLocationOnScreen().y +
                        w.getHeight())) {
                    pp.y -= popup.getHeight();
                }

                popup.setLocation(pp);
            }
        }
    }

    /**
     * Stub
     *
     * @param e event
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        // stub
    }

    /**
     * Stub
     *
     * @param e event
     */
    @Override
	public void mouseEntered(MouseEvent e) {
        // stub
    }

    /**
     * Stub
     *
     * @param e event
     */
    @Override
	public void mouseExited(MouseEvent e) {
        // stub
    }

    /**
     * Stub
     *
     * @param e event
     */
    @Override
	public void mouseReleased(MouseEvent e) {
        // stub
    }

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	/**
	 * In tree mode updates the value of display width so that the wrapping of key/value 
	 * lines is updated. 
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		if (tree != null) {
			((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth());
			((MDTree)tree).forceUIUpdate();
			//tree.setCellRenderer(new MDTreeCellRenderer()); //works...
			//tree.updateUI(); //works... 
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
		if (tree != null) {
			//tree.revalidate();
			((MDTree)tree).forceUIUpdate();
		}
	}
    
}
