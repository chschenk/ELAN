package mpi.eudico.client.annotator.tier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JButton;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * The first pane for the 'reparent tier' task. Select the tier that is to be
 * moved.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyTierStep1 extends StepPane implements TreeSelectionListener, ActionListener {
    private TranscriptionImpl transcription;
    private JTree tierTree;
    private JScrollPane scrollPane;
    private JCheckBox depTiersCB;
    private JCheckBox renameTiersCB;
    private TierTree tree;
    
    JButton sortButton;
    JButton reverseButton;
    JPanel sortButtonsPanel;
    
    private boolean copyMode = false;
    private boolean selectedTierChanged = false;

    /**
     * Creates a new CopyTierStep1 instance
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep1(MultiStepPane multiPane,
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
        
        sortButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortAZ"));
        reverseButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortZA"));
                
        sortButton.setEnabled(true);
        reverseButton.setEnabled(true);
                
        sortButton.addActionListener(this);
        reverseButton.addActionListener(this);

        if (transcription != null) {
            //TierTree tree = new TierTree(transcription, null);
            tree = new TierTree(transcription);
            tierTree = new JTree(tree.getTree());
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
        //tierTree.setRootVisible(false);
        tierTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        //tierTree.setFont(tierTree.getFont().deriveFont((float) 14));

        scrollPane = new JScrollPane(tierTree);

        //scrollPane.setViewportView(tierTree);
        for (int i = 0; i < tierTree.getRowCount(); i++) {
            tierTree.expandRow(i);
        }

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        if (copyMode) {
            add(new JLabel("<html>" +
                    ElanLocale.getString("MultiStep.Copy.SelectTier") + 
                    "</html>"), gbc);
        } else {
            add(new JLabel("<html>" +
                ElanLocale.getString("MultiStep.Reparent.SelectTier") + " " +
                ElanLocale.getString("MultiStep.Reparent.Depending") +
                "</html>"), gbc);
        }
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);
        
        // add sort buttons
        sortButtonsPanel = new JPanel(new GridLayout(1, 2, 4, 2));
        sortButtonsPanel.add(sortButton);
        sortButtonsPanel.add(reverseButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        
        add(sortButtonsPanel,gbc);
        
        if (copyMode) {
            depTiersCB = new JCheckBox(ElanLocale.getString("MultiStep.Copy.Depending"));
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            add(depTiersCB, gbc);

            renameTiersCB = new JCheckBox(ElanLocale.getString("MultiStep.Copy.RenameOriginalTiers"));
            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            add(renameTiersCB, gbc);
        }
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        if (copyMode) {
            return ElanLocale.getString("MultiStep.Copy.SelectTier");
        }
        return ElanLocale.getString("MultiStep.Reparent.SelectTier");
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        // the next button is already disabled
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        selectedTierChanged = false;
    }

    /**
     * If a tier has been selected store it in the properties map for use by
     * other steps and return true.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if (tierTree.getSelectionModel().getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                multiPane.putStepProperty("SelTier",
                    ((DefaultMutableTreeNode) o).getUserObject());
                multiPane.putStepProperty("SelTierChanged", Boolean.valueOf(selectedTierChanged));

                //System.out.println("selected obj: " + o);
            }
            if (copyMode) {
                multiPane.putStepProperty("IncludeDepTiers", Boolean.valueOf(depTiersCB.isSelected()));
                multiPane.putStepProperty("RenameOriginalTiers", Boolean.valueOf(renameTiersCB.isSelected()));
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
        return true;
    }

    /**
     * Nothing to do. Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        return true;
    }

    /**
     * When a tier has been selected in the tree, enable the 'next' button.
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    @Override
	public void valueChanged(TreeSelectionEvent e) {
        if ((tierTree.getSelectionCount() > 0) &&
                (tierTree.getSelectionModel().getMinSelectionRow() > 0)) {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
            selectedTierChanged = true;
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
    
    private JTree redrawTree(DefaultMutableTreeNode rootNode) {
    	JTree tree = new JTree(rootNode);

    	//tree.setRootVisible(false);
    	tree.putClientProperty("JTree.lineStyle", "Angled");
    	DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
    	renderer.setLeafIcon(null);
    	renderer.setOpenIcon(null);
    	renderer.setClosedIcon(null);
    	renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);
    	
    	tree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
    	//tree.setFont(tierTree.getFont().deriveFont((float) 14));
    	
    	return tree;
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

    		gbc.gridy = 1;
    		gbc.gridx = 0;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		add(scrollPane, gbc);

    		revalidate();
    		repaint();

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

    		gbc.gridy = 1;
    		gbc.gridx = 0;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		add(scrollPane, gbc);

    		revalidate();
    		repaint();
    	}
    }
}
