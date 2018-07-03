package mpi.eudico.client.annotator.tier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.JButton;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * The second step in the reparent process: select the destination tier, or the
 * transcription itself when the tier should become a root tier.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CopyTierStep2 extends StepPane implements TreeSelectionListener, ActionListener {
    private TranscriptionImpl transcription;
    private JTree tierTree;
    private String tierName;
    private TierImpl selTier;
    private JLabel tierLabel;
    private boolean copyMode = false;
    
    private JScrollPane scrollPane;
    private TierTree tree;
    JButton sortButton;
    JButton reverseButton;
    JPanel sortButtonsPanel;

    /**
     * Creates a new CopyTierStep2 instance.
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;
        if (multiPane.getStepProperty("CopyMode") != null) {
            copyMode = true;
        }
        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    @Override
	public void initComponents() {
        // setPreferredSize
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        tierLabel = new JLabel();

        sortButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortAZ"));
        reverseButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortZA"));
                
        sortButton.setEnabled(true);
        reverseButton.setEnabled(true);
        
        sortButton.addActionListener(this);
        reverseButton.addActionListener(this);
        
        if (transcription != null) {
            tree = new TierTree(transcription);
            DefaultMutableTreeNode transNode = tree.getTree();
            transNode.setUserObject(ElanLocale.getString("MultiStep.Reparent.Transcription"));
            tierTree = new JTree(transNode);
        } else {
            tierTree = new JTree();
        }

        DefaultTreeSelectionModel model = new DefaultTreeSelectionModel();
        model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        model.addTreeSelectionListener(this);
        tierTree.setSelectionModel(model);
        tierTree.putClientProperty("JTree.lineStyle", "Angled");

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tierTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

        //tierTree.setShowsRootHandles(false);
        tierTree.setRootVisible(true);
        tierTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        tierTree.setFont(tierTree.getFont().deriveFont((float) 14));

        scrollPane = new JScrollPane(tierTree);

        for (int i = 0; i < tierTree.getRowCount(); i++) {
            tierTree.expandRow(i);
        }

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(tierLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("MultiStep.Reparent.SelectParent") + " " +
                ElanLocale.getString("MultiStep.Reparent.SelectTrans") +
                "</html>"), gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);
        
        // add sort buttons
        sortButtonsPanel = new JPanel(new GridLayout(1, 2, 4, 2));
        sortButtonsPanel.add(sortButton);
        sortButtonsPanel.add(reverseButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        
        add(sortButtonsPanel,gbc);        
        
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("MultiStep.Reparent.SelectParent");
    }

    /**
     * Fetch the name of the tier that has been selected to move.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        tierName = (String) multiPane.getStepProperty("SelTier");
        Boolean selTierChanged = (Boolean) multiPane.getStepProperty("SelTierChanged");

        if (tierName != null) {
            tierLabel.setText(ElanLocale.getString(
                    "MultiStep.Reparent.SelectedTier") + " " + tierName);
            selTier = transcription.getTierWithId(tierName);
        	// If we went back to the previous step, did not change the tier,
        	// and went forward again, don't change this selection either.
        	if (selTierChanged != null && selTierChanged) {
	            if (copyMode) {
	                if (selTier.hasParentTier()) {
	                    String parentName = selTier.getParentTier().getName();
	                    Enumeration en = ((DefaultMutableTreeNode) tierTree.getModel().getRoot()).breadthFirstEnumeration();
	                    DefaultMutableTreeNode node = null;
	                    
	                    while (en.hasMoreElements()) {
	                        node = (DefaultMutableTreeNode) en.nextElement();
	                        if (parentName.equals(node.getUserObject())) {
	                            tierTree.getSelectionModel().setSelectionPath(new TreePath(
	                                    ((DefaultTreeModel)tierTree.getModel()).getPathToRoot(node)));
	                            break;
	                        }
	                    }
	                } else {
	                    tierTree.getSelectionModel().setSelectionPath(new TreePath(tierTree.getModel().getRoot()));
	                }
	            }
        	}
        } else {
            // handle error
            tierName = "";
            selTier = null;
        }

        // if we have been here before check tree selection
        valueChanged(null);
    }

    /**
     * Enable the next button, a parent has been selected before.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * Store the name of the new parent (or the transcription) in the
     * properties map.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if (tierTree.getSelectionModel().getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;

                if (node.isRoot()) {
                    //multiPane.putStepProperty("HasParent", Boolean.FALSE);
                	multiPane.putStepProperty("SelNewParent", null);
                } else {
                    //multiPane.putStepProperty("HasParent", Boolean.TRUE);
                    String parentName = (String) node.getUserObject();
                    multiPane.putStepProperty("SelNewParent", parentName);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Always allowed.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
        return true;
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        return true;
    }

    /**
     * Perform some checks on the selected parent. Enable the next button if a
     * valid parent has been selected: a tier can not be it's own parent  a
     * tier can not be assigned to its current parent and a tier that was
     * already a root tier can not be assigned to the  transcription again.
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    @Override
	public void valueChanged(TreeSelectionEvent e) {
        if (tierTree.getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;

                String parentName = (String) node.getUserObject();
                if (!copyMode) {
	                if (parentName.equals(tierName)) {
	                    // cannot be it's own parent
	                    tierTree.setSelectionPath(null);
	
	                    return;
	                } else if ((selTier != null) &&
	                        (selTier.getParentTier() == null) && node.isRoot()) {
	                    // the tier was already an independent tier
	                    tierTree.setSelectionPath(null);
	
	                    return;
	                } else if ((selTier != null) &&
	                        (selTier.getParentTier() != null)) {
	                    String oldParent = selTier.getParentTier().getName();
	
	                    if (oldParent.equals(parentName)) {
	                        // the same parent as the old parent
	                        tierTree.setSelectionPath(null);
	
	                        return;
	                    }
	                }
                }
            }

            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
    
    private JTree redrawTree(DefaultMutableTreeNode rootNode) {

    	JTree tree = new JTree(rootNode);

    	tree.setRootVisible(true);

    	tree.putClientProperty("JTree.lineStyle", "Angled");
    	tree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
    	tree.setFont(tierTree.getFont().deriveFont((float) 14));
    	DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
    	renderer.setLeafIcon(null);
    	renderer.setOpenIcon(null);
    	renderer.setClosedIcon(null);
    	renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

    	return tree;
    }
    
    public void triggerTreeSelection() {
    	if (tierTree.getSelectionCount() > 0) {
    		Object o = tierTree.getLastSelectedPathComponent();

    		if (o instanceof DefaultMutableTreeNode) {
    			DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;

    			String parentName = (String) node.getUserObject();
    			if (!copyMode) {
    				if (parentName.equals(tierName)) {
    					// cannot be it's own parent
    					tierTree.setSelectionPath(null);

    					return;
    				} else if ((selTier != null) &&
    						(selTier.getParentTier() == null) && node.isRoot()) {
    					// the tier was already an independent tier
    					tierTree.setSelectionPath(null);

    					return;
    				} else if ((selTier != null) &&
    						(selTier.getParentTier() != null)) {
    					String oldParent = selTier.getParentTier().getName();

    					if (oldParent.equals(parentName)) {
    						// the same parent as the old parent
    						tierTree.setSelectionPath(null);

    						return;
    					}
    				}
    			}
    		}

    		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    	} else {
    		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
    	}
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
    	if (ae.getSource() == sortButton) {
    		TreeModel model = tierTree.getModel();
    		Object root = model.getRoot();
    		Object[] path = {root};
    		TreePath pathRoot = new TreePath(path);
    		Enumeration<TreePath> expandedDescendants = tierTree.getExpandedDescendants(pathRoot);
    		TreePath[] selectedNodes = tierTree.getSelectionPaths();                            

    		tierTree = redrawTree(tree.sortAlphabetically());  // redraw

    		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    		selectionModel.addTreeSelectionListener(this);
    		tierTree.setSelectionModel(selectionModel);

    		for ( ; expandedDescendants.hasMoreElements(); ) {
    			TreePath pathToNode = (TreePath) expandedDescendants.nextElement();
    			tierTree.expandPath(pathToNode);
    		}

    		tierTree.setSelectionPaths(selectedNodes);

    		this.remove(scrollPane);                           

    		scrollPane = new JScrollPane(tierTree);

    		Insets insets = new Insets(4, 6, 4, 6);
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.insets = insets;
    		gbc.weightx = 1.0;

    		gbc.gridy = 2;
    		gbc.gridx = 0;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		add(scrollPane, gbc);

    		revalidate();
    		repaint();

    		triggerTreeSelection();

    	} else if (ae.getSource() == reverseButton) {

    		TreeModel model = tierTree.getModel();
    		Object root = model.getRoot();

    		Object[] path = {root};
    		TreePath pathRoot = new TreePath(path);
    		Enumeration<TreePath> expandedDescendants = tierTree.getExpandedDescendants(pathRoot);
    		TreePath[] selectedNodes = tierTree.getSelectionPaths();

    		tierTree = redrawTree(tree.sortReverseAlphabetically());  // changed

    		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    		selectionModel.addTreeSelectionListener(this);
    		tierTree.setSelectionModel(selectionModel);

    		for ( ; expandedDescendants.hasMoreElements(); ) {
    			TreePath pathToNode = (TreePath) expandedDescendants.nextElement();
    			tierTree.expandPath(pathToNode);

    		}

    		tierTree.setSelectionPaths(selectedNodes);

    		this.remove(scrollPane);                           

    		scrollPane = new JScrollPane(tierTree);

    		Insets insets = new Insets(4, 6, 4, 6);
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.insets = insets;
    		gbc.weightx = 1.0;                            

    		gbc.gridy = 2;
    		gbc.gridx = 0;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		add(scrollPane, gbc);

    		revalidate();
    		repaint();

    		triggerTreeSelection();

    	}
    }
}
