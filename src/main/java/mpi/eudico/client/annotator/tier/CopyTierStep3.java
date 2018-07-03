package mpi.eudico.client.annotator.tier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.CopyTierCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * The third step in the reparent process:  allow or, in some cases, force to
 * choose another linguistic type.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CopyTierStep3 extends ProgressStepPane implements ListSelectionListener,
    ActionListener, ProgressListener {
    private TranscriptionImpl transcription;
    private JLabel tierOverview;
    private JTable typeTable;
    private JScrollPane scrollPane;
    private String tierName;
    private String newParentName;
    private TierImpl selTier;
    private TierImpl newParent;
    private LinguisticType curType;
	private LinguisticType selType;
    private String selTypeName;
    private LinguisticTypeTableModel typeModel;
    private String[] columns;
    private List<LinguisticType> types;
    private JButton typButton;
    private GridBagLayout layout;
    private CopyTierCommand com;
    private boolean copyMode = false;
	private boolean includeDepTiers = true;
	private boolean copyIncludesTimeAlignableChild;
	private JCheckBox omitDependingTiersCB;

    /**
     * Creates a new CopyTierStep3 instance.
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep3(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;

        types = this.transcription.getLinguisticTypes();

        if (multiPane.getStepProperty("CopyMode") != null) {
            copyMode = true;
        }

        initComponents();
    }

    /**
     * Initialise components.
     */
    @Override
	public void initComponents() {   
        // setPreferredSize
        layout = new GridBagLayout();
        setLayout(layout);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        tierOverview = new JLabel();

        ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
        ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
        CheckBoxTableCellRenderer cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setIcon(untickIcon);
        cbRenderer.setSelectedIcon(tickIcon);
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // create a tablemodel for linguistic types
        if (types != null) {
            columns = new String[] {
                    LinguisticTypeTableModel.NAME,
                    LinguisticTypeTableModel.STEREOTYPE,
                    LinguisticTypeTableModel.CV_NAME,
                    LinguisticTypeTableModel.TIME_ALIGNABLE,
                };
            typeModel = new LinguisticTypeTableModel(types, columns);
        } else {
            typeModel = new LinguisticTypeTableModel();
        }

        typeTable = new JTable(typeModel);
        typeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeTable.getSelectionModel().addListSelectionListener(this);

        final int columnCount = typeModel.getColumnCount();
        // The column model apparently counts in view coordinates, not model coordinates,
        // but they are (still) the same here.
		for (int i = 0; i < columnCount; i++) {
            if (typeModel.getColumnClass(i) != String.class) {
                typeTable.getColumnModel().getColumn(i)
                         .setPreferredWidth(35);
            }

            if (typeModel.getColumnClass(i) == Boolean.class) {
                typeTable.getColumnModel().getColumn(i)
                         .setCellRenderer(cbRenderer);
            }
        }

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(typeTable);

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(tierOverview, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        // "Add New Linguistic Type..."
        typButton = new JButton(ElanLocale.getString(
                    "Menu.Type.AddNewType"));
        typButton.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        gbc.weightx = 0.0;
        add(typButton, gbc);
        
        // "Omit depending tiers (they prevent copying for this type)."
        omitDependingTiersCB = new JCheckBox(ElanLocale.getString(
                "MultiStep.Reparent.OmitDepTiers"));
        omitDependingTiersCB.setVisible(false);
        omitDependingTiersCB.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        gbc.weightx = 0.0;
        add(omitDependingTiersCB, gbc);
    }

    /**
     * Remove the table and linguistictype button and add a progressbar  and a
     * message label.
     */
    private void adjustComponents() {
        JPanel progressPanel = new JPanel(new GridBagLayout());
        JPanel filler = new JPanel();
        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        progressPanel.add(filler, gbc);

        progressLabel = new JTextArea("...");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        progressPanel.add(progressLabel, gbc);
        
        progressLabel.setEditable(false);
        progressLabel.setBackground(progressPanel.getBackground());

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setValue(0);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        progressPanel.add(progressBar, gbc);

        remove(scrollPane);
        remove(typButton);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(progressPanel, gbc);

        revalidate();
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("MultiStep.Reparent.SelectType");
    }

    /**
     * Get the selected tier and new parent and fill the linguistic type table.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        tierName = (String) multiPane.getStepProperty("SelTier");

        selTier = transcription.getTierWithId(tierName);

        if (selTier != null) {
            curType = selTier.getLinguisticType();
        }

        Object par = multiPane.getStepProperty("SelNewParent");

        if (par != null) {
            newParentName = (String) par;

            newParent = transcription.getTierWithId(newParentName);
        } else {
        	newParent = null;
            newParentName = "-";
        }

        tierOverview.setText("<html><table><tr><td>" +
            ElanLocale.getString("MultiStep.Reparent.SelectedTier") + " " +
            "</td><td>" + tierName + "</td></tr>" + "<tr><td>" +
            ElanLocale.getString("MultiStep.Reparent.SelectedParent") + " " +
            "</td><td>" + newParentName + "</td></tr>");

        Constraint con = null;

        if (newParent != null) {
            con = newParent.getLinguisticType().getConstraints();
        }

        if (newParent == null) {
            typeModel.showOnlyStereoTypes(new int[] { -1 }); // -1 == no constraints
        } else if ((con == null) ||
                (con.getStereoType() == Constraint.TIME_SUBDIVISION) ||
                (con.getStereoType() == Constraint.INCLUDED_IN)) {
            // parent is root or time subdivision
            typeModel.showOnlyStereoTypes(new int[] {
                    Constraint.TIME_SUBDIVISION, Constraint.INCLUDED_IN,
                    Constraint.SYMBOLIC_SUBDIVISION, Constraint.SYMBOLIC_ASSOCIATION
                });
        } else {
            // parent is symbolic subdivision or association
            typeModel.showOnlyStereoTypes(new int[] {
                    Constraint.SYMBOLIC_SUBDIVISION,
                    Constraint.SYMBOLIC_ASSOCIATION
                });
        }

        // Find out if we have to care about which type the user selects
        Object include = multiPane.getStepProperty("IncludeDepTiers");

        if (include instanceof Boolean) {
            includeDepTiers = (Boolean) include;
        }
        
        copyIncludesTimeAlignableChild = false;
        if (includeDepTiers) {
        	List<TierImpl> depTiers = selTier.getChildTiers();
        
        	for (TierImpl dep : depTiers) {
        		if (dep.isTimeAlignable()) {
        			copyIncludesTimeAlignableChild = true;
        			break;
        		}
        	}
        }
    	omitDependingTiersCB.setVisible(false);

        // Pre-select a type, if possible.
        int col = typeModel.findColumn(LinguisticTypeTableModel.NAME);

        for (int i = 0; i < typeModel.getRowCount(); i++) {
            Object o = typeModel.getValueAt(i, col);

            if (o instanceof String) {
                if (curType.getLinguisticTypeName().equals(o)) {
                    selTypeName = (String) o;
                    // This triggers a valueChanged event (!valueIsAdjusting)
                    typeTable.setRowSelectionInterval(i, i);

                    break;
                }
            }
        }
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        // n.a.
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        // this is the last step, cannot go to next
        return false;
    }

    /**
     * Disable the finish button and allow a step back.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);

        return true;
    }

    /**
     * No cleanup necessary.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#cancelled()
     */
    @Override
	public void cancelled() {
    }

    /**
     * No cleanup necessary.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#finished()
     */
    @Override
	public void finished() {
    }

    /**
     * This is the last step. When all conditions have been met create a
     * command and register as progress listener. Disable buttons.
     *
     * @return true if the process has been finished successfully
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        // the actual reparenting/copying of the tier(s)
        // disable all buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        //System.out.println("T: " + tierName + " P: " + newParentName + " LT: " +
         //   selTypeName);

        // create a command passing the selected tier, the selected new parent and the
        // (new) linguistic type for selected tier
        // add this panel as progress listener
        adjustComponents();

        if ((tierName != null) && (newParentName != null) &&
                (selTypeName != null)) {
            Boolean renameOriginalTiers = Boolean.FALSE;

            if (copyMode) {
                com = (CopyTierCommand) ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.COPY_TIER);

                Object rename = multiPane.getStepProperty("RenameOriginalTiers");

                if (rename instanceof Boolean) {
                    renameOriginalTiers = (Boolean) rename;
                }
            } else {
            	// REPARENT_TIER actually results in the same CopyTierCommand.
                com = (CopyTierCommand) ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.REPARENT_TIER);
            }
            
            boolean omitDependentTiers = omitDependingTiersCB.isVisible() &&
            		                     omitDependingTiersCB.isSelected();

            com.addProgressListener(this);
            com.execute(transcription,
                new Object[] {
                    tierName, newParentName, selTypeName,
                    includeDepTiers && !omitDependentTiers,
                    renameOriginalTiers
                });

            return false;
        } else {
            progressLabel.setText("Illegal selection");
            multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
            multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);

            return false;
        }

        //System.out.println("T: " + tierName + " P: " + newParentName + " LT: " + selTypeName);
        //return false;
    }

    /**
     * Enable the finish button once a valid type has been selected.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override // ListSelectionListener
	public void valueChanged(ListSelectionEvent lse) {
        if ((typeModel != null) && !lse.getValueIsAdjusting()) {
            int row = typeTable.getSelectedRow();

            if (row > -1) {
                int col = typeModel.findColumn(LinguisticTypeTableModel.NAME);
                String typeName = (String) typeModel.getValueAt(row, col);

                final int size = types.size();
				for (int i = 0; i < size; i++) {
                    LinguisticType t = types.get(i);

                    if (t.getLinguisticTypeName().equals(typeName)) {
                        selTypeName = typeName;
                        selType = t;
                        break;
                    }
                }

            } else {
                selTypeName = null;
                selType = null;
            }
        }
        maybeEnableFinish();
    }

    /**
     * Determine of the Finish button should be enabled.
     */
    private void maybeEnableFinish() {
    	boolean enableFinish;
    	
    	if (selType == null) {
    		enableFinish = false;
    	} else if (copyIncludesTimeAlignableChild) {
	    	/* If the tier has a time-alignable child, the copy operation
	    	 * won't be able to convert it to unaligned.
	    	 * (of course it could, but it doesn't have the code to do it)
	    	 * Therefore, only allow the choice of a time-alignable type.
	    	 */
	    	enableFinish = selType.isTimeAlignable();
	    	omitDependingTiersCB.setVisible(!enableFinish);
	    	// Or if the user is prepared to forego copying the child tiers.
	    	if (omitDependingTiersCB.isVisible() && omitDependingTiersCB.isSelected()) {
	    		enableFinish = true;
	    	}
	    } else {
    		enableFinish = true;
	    }

    	multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, enableFinish);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
    	Object source = e.getSource();
    	
    	if (source == omitDependingTiersCB) {
    		maybeEnableFinish();
    	} else if (source == typButton) {
    		addNewLinguisticType();
    	}
    }

    /**
     * Show the "add new linguistic type" dialog, then rescan the types to add
     * to  the table.
     */
	private void addNewLinguisticType() {
        // store current selection, if any
        String typeName = null;

        if (typeTable.getRowCount() > 0) {
            int row = typeTable.getSelectedRow();

            if (row > -1) {
                int col = typeModel.findColumn(LinguisticTypeTableModel.NAME);
                typeName = (String) typeModel.getValueAt(row, col);
            }
        }

        List<String> curTypes = new ArrayList<String>();
        int col = typeModel.findColumn(LinguisticTypeTableModel.NAME);
        
        for (int i = 0; i < typeModel.getRowCount(); i++) {
        	curTypes.add( (String) typeModel.getValueAt(i, col) );
        }
        
        new EditTypeDialog2(null, true, transcription, EditTypeDialog2.ADD).setVisible(true);
        // if the new type dialog is used then check whether any type has changed
        // rebuild the model. Check if new type has been added; if so select it
        typeModel.removeAllRows();
        
        for (int i = 0; i < types.size(); i++) {
            typeModel.addLinguisticType(types.get(i));
        }
        
    	// try to detect the (first) new type, select it
    	int firstNewOrChanged = -1;
        String tname = null;

        for (int i = 0; i < typeModel.getRowCount(); i++) {
            tname = (String) typeModel.getValueAt(i, col);
        
            if (!curTypes.contains(tname)) {
        		firstNewOrChanged = i;
        		break;
            }
        }
        
        if (firstNewOrChanged > -1) {
            selTypeName = (String) typeTable.getValueAt(firstNewOrChanged, col);
            // This triggers a valueChanged event (!valueIsAdjusting)
            typeTable.setRowSelectionInterval(firstNewOrChanged, firstNewOrChanged);
        } else if (typeName != null) {// this selects the previously selected.
            tname = null;

            for (int i = 0; i < typeModel.getRowCount(); i++) {
                tname = (String) typeModel.getValueAt(i, col);

                if (typeName.equals(tname)) {
                    selTypeName = tname;
                    // This triggers a valueChanged event (!valueIsAdjusting)
                    typeTable.setRowSelectionInterval(i, i);

                    break;
                }
            }
        }
    }
    
    /**
     * Unregister as a progress listener and close the pane.
     */
	@Override
	protected void endOfProcess() {
        if (com != null) {
            com.removeProgressListener(this);
        }

        multiPane.close();
	}

}
