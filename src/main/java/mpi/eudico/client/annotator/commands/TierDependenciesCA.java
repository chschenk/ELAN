package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.util.ACMEditableDocument;

/**
 *
 */
@SuppressWarnings("serial")
public class TierDependenciesCA extends CommandAction implements ACMEditListener, ActionListener {
	private JFrame dependencyFrame;

	private ActionHandler actionHandler;

	private JButton sortButton;
	private JButton reverseButton;

	private int locationX = -1;
	private int locationY = -1;
	private int sizeWidth = -1;
	private int sizeHeight = -1;

	private JTree tree;
    private TierTree tTree;
    private JScrollPane jScrollPane;
    private JPanel sortButtonPanel;

	/**
	 * Creates a new TierDependenciesCA instance
	 *
	 * @param theVM DOCUMENT ME!
	 */
	public TierDependenciesCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.TIER_DEPENDENCIES);

		try {
			((ACMEditableDocument) vm.getTranscription()).addACMEditListener(
				(ACMEditListener) this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	@Override
	protected void newCommand() {
		command =
			ELANCommandFactory.createCommand(
				vm.getTranscription(),
				ELANCommandFactory.TIER_DEPENDENCIES);
	}

	/**
	 *
	 */
	@Override
	protected Object getReceiver() {
		return null;
	}

	/**
	 * Returns null, no arguments need to be passed.
	 *
	 * @return DOCUMENT ME!
	 */
	@Override
	protected Object[] getArguments() {
		Object[] args = new Object[1];
		args[0] = getDependencyFrame();

		return args;
	}
	
	/**
	 * Give access to the frame.
	 * @return the dependency frame or null
	 */
	public JFrame getFrame() {
		return dependencyFrame;
	}

	private JFrame getDependencyFrame() {
		if (dependencyFrame == null) {
			createDependencyFrame();
			
			Point p = Preferences.getPoint("DependenciesFrame.Location", null);
			if (p != null) {
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				int x = p.x <= screen.width - 50 ? p.x : screen.width - 50;
				int y = p.y <= screen.height - 50 ? p.y : screen.height - 50;
				dependencyFrame.setLocation(x, y);
			}
			
			Dimension dim = Preferences.getDimension("DependenciesFrame.Size", null);
			if (dim != null) {
				dependencyFrame.setSize(dim);
			}
		}

		return dependencyFrame;
	}

	private void createDependencyFrame() {
		try {
			tTree = new TierTree((TranscriptionImpl)vm.getTranscription());
			tree = initTree(tTree.getTree());       
	        
			dependencyFrame = new ClosableFrame(ElanLocale.getString("Tier Dependencies"));

			actionHandler = new ActionHandler();

			dependencyFrame.getContentPane().setLayout(new GridBagLayout());

			Insets insets = new Insets(2, 2, 2, 2);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;

			jScrollPane = new JScrollPane(tree);

			dependencyFrame.getContentPane().add(jScrollPane,gbc);

			createSortButtonPanel();
			
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			dependencyFrame.getContentPane().add(sortButtonPanel, gbc);
			
			
			if ((locationX != -1)
				&& (locationY != -1)
				&& (sizeWidth != -1)
				&& (sizeHeight != -1)) {
				dependencyFrame.setLocation(locationX, locationY);
				dependencyFrame.setSize(sizeWidth, sizeHeight);
			}

			if ((dependencyFrame.getHeight() < 100) || (dependencyFrame.getWidth() < 133)) {
				dependencyFrame.setSize(133, 200);
			}
			addCloseActions();
			
			updateLocale();
		}
		catch (Exception ex) {
			System.out.println("Couldn't create dependencyFrame.");
			//ex.printStackTrace();
		}
	}
	
	private void createSortButtonPanel() {
		sortButtonPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints sortButtonPanelGbc = new GridBagConstraints();
		sortButtonPanelGbc.gridx = 0;
		sortButtonPanelGbc.gridy = 0;
		sortButtonPanelGbc.insets = new Insets(2, 2, 2, 2);
		
		sortButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortAZ"));
		sortButtonPanel.add(sortButton, sortButtonPanelGbc);
		sortButton.addActionListener(this.actionHandler);

		sortButtonPanelGbc.gridx = 1;
		
		reverseButton = new JButton(ElanLocale.getString("EditCVDialog.Button.SortZA"));
		sortButtonPanel.add(reverseButton, sortButtonPanelGbc);
		reverseButton.addActionListener(this.actionHandler);
	}

	/**
	 * Initialize a JTree.
	 * 
	 * @param rootNode the root node
	 * 
	 * @return a configured JTree
	 */
	private JTree initTree(DefaultMutableTreeNode rootNode) {
		JTree tree = new JTree(rootNode);

        tree.setRootVisible(false);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
		return tree;
	}

	private JTree redrawTree(DefaultMutableTreeNode rootNode) {
		JTree tree = new JTree(rootNode);

		tree.setRootVisible(false);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

		return tree;
	}
	
	/**
	 * Returns the tree object.
	 * 
	 * @return the tree, can be null.
	 */
	public JTree getTree() {
		return tree;
	}

	//needed to set title of dialog
	@Override
	public void updateLocale() {
		super.updateLocale();

		if (dependencyFrame != null) {
			dependencyFrame.setTitle(ElanLocale.getString("Menu.View.DependenciesDialog"));
			dependencyFrame.repaint();
		}
	}

    /**
     * Adds a listener to the closing action. The window will be removed as ACMEditListener
     * and the location will be stored as preference.
     */
    protected void addCloseActions() {
        if (dependencyFrame != null) {
	        dependencyFrame.addWindowListener(new WindowAdapter() {
	            @Override
				public void windowClosing(WindowEvent e) {
					if (e.getID() == WindowEvent.WINDOW_CLOSING) {
		                ((ACMEditableDocument) vm.getTranscription()).removeACMEditListener(
		        				(ACMEditListener) TierDependenciesCA.this);
		                Point p = dependencyFrame.getLocationOnScreen();
		                Dimension d = dependencyFrame.getSize();
		                Preferences.set("DependenciesFrame.Location", p, null, false, false);
		                Preferences.set("DependenciesFrame.Size", d, null, false, false);
					} else {
						System.err.println("DEBUG: TierDependenciesCA, window event not closing");
					}
	            }
	        });
        }
    }
        
	/**
	 * DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	@Override
	public void ACMEdited(ACMEditEvent e) {
		switch (e.getOperation()) {
			case ACMEditEvent.ADD_TIER :
			case ACMEditEvent.REMOVE_TIER :
			case ACMEditEvent.CHANGE_TIER :
				{
					if (dependencyFrame == null) {
						break;
					}

					boolean bVisible = dependencyFrame.isVisible();

					//remember last position before disposing
					locationX = (int) dependencyFrame.getLocation().getX();
					locationY = (int) dependencyFrame.getLocation().getY();
					sizeWidth = (int) dependencyFrame.getSize().getWidth();
					sizeHeight = (int) dependencyFrame.getSize().getHeight();

					dependencyFrame.dispose();

					//update tree
					createDependencyFrame();

					//if tree was visible before updating, show it again
					if ((command != null) && (bVisible == true)) {
						command.execute(getReceiver(), getArguments());
					}
				}
		}
	}
    
	private class ActionHandler implements ActionListener{

		public ActionHandler() {	   		  		
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == sortButton || ae.getSource() == reverseButton) {

				if (dependencyFrame != null) {
					TreeModel model = tree.getModel();
					Object root = model.getRoot();
					Object[] path = {root};
					TreePath pathRoot = new TreePath(path);
					Enumeration<TreePath> expandedDescendants = tree.getExpandedDescendants(pathRoot);
					TreePath[] selectedNodes = tree.getSelectionPaths();

					if(ae.getSource() == sortButton) {
						tree = redrawTree(tTree.sortAlphabetically());  // changed
					} else if(ae.getSource() == reverseButton) {
						tree = redrawTree(tTree.sortReverseAlphabetically());  // changed
					}

					Insets insets = new Insets(2, 2, 2, 2);

					dependencyFrame.getContentPane().removeAll();

					GridBagConstraints gbc = new GridBagConstraints();
					gbc.gridx = 0;
					gbc.gridy = 0;
					gbc.insets = insets;
					gbc.fill = GridBagConstraints.BOTH;
					gbc.weightx = 1.0;
					gbc.weighty = 1.0;

					if (tree == null) { System.err.println("DEBUG: JTree is null"); }

					for ( ; expandedDescendants.hasMoreElements(); ) {
						TreePath pathToNode = (TreePath) expandedDescendants.nextElement();
						tree.expandPath(pathToNode);
					}

					tree.setSelectionPaths(selectedNodes);

					jScrollPane = new JScrollPane(tree);
					dependencyFrame.getContentPane().add(jScrollPane, gbc);
					
					gbc.gridy = 1;
					gbc.fill = GridBagConstraints.NONE;
					gbc.weightx = 0.0;
					gbc.weighty = 0.0;
					dependencyFrame.getContentPane().add(sortButtonPanel, gbc);

					dependencyFrame.getContentPane().validate();
					dependencyFrame.getContentPane().repaint();
				}
			}
		}
	}
}
