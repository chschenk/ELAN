package mpi.eudico.client.annotator.md.cmdi;

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

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.md.imdi.ImdiConstants;
import mpi.eudico.client.annotator.md.imdi.MDTree;
import mpi.eudico.client.annotator.md.imdi.MDTreeCellRenderer;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;

@SuppressWarnings("serial")
public class CMDIViewerPanel extends JPanel implements MDViewerComponent, ComponentListener,
ActionListener, MouseListener{
	private CMDIServiceProvider provider;
	protected JTree tree;
	protected JScrollPane scrollPane;
	// have menu items for these options?
	private boolean allRowsExpanded = true;
	private boolean allTopNodesExpanded = false;
    private JPopupMenu popup;
    private JMenuItem expandAllMI;
    private JMenuItem collapseAllMI;
    private JMenuItem expandTopMI;
    private JMenuItem collapseTopMI;
	
	/**
	 * No-arg constructor
	 */
	public CMDIViewerPanel() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param provider expected is a CMDIServiceProvider
	 */
	public CMDIViewerPanel(MDServiceProvider provider) {
		super();
		if (provider instanceof CMDIServiceProvider) {
			this.provider = (CMDIServiceProvider) provider;
		}
		
		initComponents();
	}

	/**
	 * Sets the provider.
	 */
	@Override
	public void setProvider(MDServiceProvider provider) {
		if (provider instanceof CMDIServiceProvider) {
			this.provider = (CMDIServiceProvider) provider;
		}
	}
	
	protected void initComponents() {
		readPreferences();
		setLayout(new GridBagLayout());
		
		tree = new MDTree(new DefaultMutableTreeNode(ImdiConstants.SESSION));
		scrollPane = new JScrollPane(tree);
        tree.setCellRenderer(new MDTreeCellRenderer());
        tree.setRowHeight(-1);// variable row height
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        add(scrollPane, gbc);
        
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUnitIncrement(tree.getFont().getSize());
		int barWidth = vBar.getWidth();
		
        ((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth() - barWidth);
        ((MDTree)tree).forceUIUpdate();
		
		if (provider != null) {
			DefaultMutableTreeNode tNode = provider.getAsTree();
			if (tNode != null) {
				((DefaultTreeModel) tree.getModel()).setRoot(tNode);
				
				if (allRowsExpanded) {
			        for (int i = 0; i < tree.getRowCount(); i++) {
			            tree.expandRow(i);
			        }
				} else if (allTopNodesExpanded) {

			        for (int i = 0; i < tree.getRowCount(); i++) {
			        	TreePath tp = tree.getPathForRow(i);
			        	if (tp != null && tp.getPathCount() == 2) {// direct child of the root node
			        		tree.expandRow(i);	
			        	}
			        }
				}
			}
		}
		addComponentListener(this);
		tree.addMouseListener(this);
		scrollPane.addMouseListener(this);
	}
	
	/**
	 * Creates the pop up menu.
	 */
	private void createPopupMenu() {
		popup = new JPopupMenu("CMDI");
		expandAllMI = new JMenuItem("Expand All");
		expandAllMI.addActionListener(this);
		popup.add(expandAllMI);
		collapseAllMI = new JMenuItem("Collapse All");
		collapseAllMI.addActionListener(this);
		popup.add(collapseAllMI);
		expandTopMI = new JMenuItem("Expand Top Nodes");
		expandTopMI.addActionListener(this);
		popup.add(expandTopMI);
		collapseTopMI = new JMenuItem("Collapse Top Nodes");
		collapseTopMI.addActionListener(this);
		popup.add(collapseTopMI);
	}
	
	/**
	 * Expands or collapses all rows in the tree.
	 * 
	 * @param expand if true expand all rows, collapse all rows otherwise
	 */
	private void updateAllTreeNodes(boolean expand) {
		if (tree != null) {
	        for (int i = 0; i < tree.getRowCount(); i++) {
	        	if (expand) {
	        		tree.expandRow(i);
	        	} else {
	        		tree.collapseRow(i);
	        	}
	        }
		}
		
	}
	
	/**
	 * Expands or collapses all direct children of the root node of the tree.
	 * 
	 * @param expand if true expand all top nodes, collapse all top nodes otherwise
	 */
	private void updateTopTreeNodes(boolean expand) {
		if (tree != null) {
	        for (int i = 0; i < tree.getRowCount(); i++) {
	        	TreePath tp = tree.getPathForRow(i);
	        	if (tp != null && tp.getPathCount() == 2) {// direct child of the root node
	        		if (expand) {
	        			tree.expandRow(i);
	        		} else {
	        			tree.collapseRow(i);
	        		}
	        	}
	        }
		}
	}
	
	/**
	 * Load some stored preferences.
	 */
	private void readPreferences() {
		Boolean boolPref = Preferences.getBool("Metadata.CMDI.AllTreeNodesExpanded", null);
		if (boolPref != null) {
			allRowsExpanded = boolPref;
		}
		
		boolPref = Preferences.getBool("Metadata.CMDI.AllTopNodesExpanded", null);
		if (boolPref != null) {
			allTopNodesExpanded = boolPref;
		}
	}
	
	@Override
	public void setSelectedKeysAndValues(Map<String, String> keysAndValuesMap) {
		// ignored for now

	}

	@Override
	public void setResourceBundle(ResourceBundle bundle) {
		// ignored for now

	}
	
	/**
	 * A method to inform the viewer of any update in the data model.
	 * Currently simply repaints the tree.
	 */
	public void dataModelUpdated() {
		if (tree != null) {
			tree.repaint();
		}
	}
	
	/**
	 * Request to get the tree after loading of the metadata in a background thread
	 */
	public void reinitializeTree() {
		if (provider != null) {
			DefaultMutableTreeNode tNode = provider.getAsTree();
			if (tNode != null) {
				((DefaultTreeModel) tree.getModel()).setRoot(tNode);
				
				if (allRowsExpanded) {
			        for (int i = 0; i < tree.getRowCount(); i++) {
			            tree.expandRow(i);
			        }
				} else if (allTopNodesExpanded) {

			        for (int i = 0; i < tree.getRowCount(); i++) {
			        	TreePath tp = tree.getPathForRow(i);
			        	if (tp != null && tp.getPathCount() == 2) {// direct child of the root node
			        		tree.expandRow(i);	
			        	}
			        }
				}
				provider.updateTreeForLanguage();
				
				int barWidth = scrollPane.getVerticalScrollBar().getWidth();
				((MDTree) tree).setDisplayWidth(scrollPane.getViewport().getWidth() - barWidth);
				((MDTree) tree).forceUIUpdate();
			} else {
				((DefaultTreeModel) tree.getModel()).setRoot(new DefaultMutableTreeNode(
						ElanLocale.getString("MetadataViewer.NoMetadataLoaded")));
			}
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// stub
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// stub
	}

	/**
	 * Recalculates string length and applies line wrapping. 
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		if (tree != null) {
			int barWidth = scrollPane.getVerticalScrollBar().getWidth();
			((MDTree)tree).setDisplayWidth(scrollPane.getViewport().getWidth() - barWidth);
			((MDTree)tree).forceUIUpdate();
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
		if (tree != null) {
			((MDTree)tree).forceUIUpdate();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// stub
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (tree == null) {
			return;
		}
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
                SwingUtilities.convertPointToScreen(pp, CMDIViewerPanel.this);

                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                Window w = SwingUtilities.windowForComponent(CMDIViewerPanel.this);

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

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// stub
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == expandAllMI) {
			updateAllTreeNodes(true);
			allRowsExpanded = true;
			Preferences.set("Metadata.CMDI.AllTreeNodesExpanded", Boolean.valueOf(allRowsExpanded), null);
		} else if (ae.getSource() == collapseAllMI) {
			updateAllTreeNodes(false);
			allRowsExpanded = false;
			Preferences.set("Metadata.CMDI.AllTreeNodesExpanded", Boolean.valueOf(allRowsExpanded), null);
		} else if (ae.getSource() == expandTopMI) {
			updateTopTreeNodes(true);
			allTopNodesExpanded = true;
			Preferences.set("Metadata.CMDI.AllTopNodesExpanded", Boolean.valueOf(allTopNodesExpanded), null);
		} else if (ae.getSource() == collapseTopMI) {
			updateTopTreeNodes(false);
			allTopNodesExpanded = false;
			Preferences.set("Metadata.CMDI.AllTopNodesExpanded", Boolean.valueOf(allTopNodesExpanded), null);
		}		
	}

}
