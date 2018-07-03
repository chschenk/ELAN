package mpi.eudico.client.annotator.imports.praat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Second step in import Praat TextGrid process: select a (root) linguistic
 * type.
 */
@SuppressWarnings("serial")
public class ImportPraatTGStep2 extends StepPane
    implements ListSelectionListener {
    private TranscriptionImpl curTranscription;
    private List<LinguisticType> rootTypes;
    private JTable typeTable;
    private String selTypeName;
    private LinguisticTypeTableModel model;
    private String[] columns;

    /**
     * Creates a new instance of the second step of the wizard.
     *
     * @param multiPane the parent pane
     * @param curTranscription the transcription
     */
    public ImportPraatTGStep2(MultiStepPane multiPane,
        TranscriptionImpl curTranscription) {
        super(multiPane);
        this.curTranscription = curTranscription;
        extractTypes();
        initComponents();
    }

    /**
     * Extracts Linguistic Types without constraints.
     */
    private void extractTypes() {
        rootTypes = new ArrayList<LinguisticType>(6);

        List<LinguisticType> types = curTranscription.getLinguisticTypes();

        for (LinguisticType lt : types) {
            if (lt.getConstraints() == null) {
                rootTypes.add(lt);
            }
        }

        if (rootTypes.size() == 0) {
            // no (root) types yet, create a default
            Command com = ELANCommandFactory.createCommand(curTranscription,
                    ELANCommandFactory.ADD_TYPE);
            com.execute(curTranscription,
                new Object[] {
                    "default-lt", null, null, Boolean.TRUE, Boolean.FALSE
                });

            LinguisticType lt = curTranscription.getLinguisticTypeByName("default-lt");

            if (lt != null) {
                rootTypes.add(lt);
            }
        }
    }

    /**
     * Initializes ui components.
     */
    @Override
	public void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        columns = new String[] {
                LinguisticTypeTableModel.NAME,
                LinguisticTypeTableModel.STEREOTYPE,
                LinguisticTypeTableModel.CV_NAME,
            };
        model = new LinguisticTypeTableModel(rootTypes, columns);
        typeTable = new JTable(model);
        typeTable.getSelectionModel()
                 .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeTable.getSelectionModel().addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(typeTable);
        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.Label.Type"); // re-use
    }

    /**
     * Just go to the next step pane, the progress monitor.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        multiPane.nextStep();

        return false;
    }

    /**
     * If there are types in the table, select the first one.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        if (typeTable.getRowCount() > 0) {
            typeTable.setRowSelectionInterval(0, 0);

            // does this lead to a value changed event?? otherwise
            int col = model.findColumn(LinguisticTypeTableModel.NAME);
            Object o = model.getValueAt(0, col);

            if (o instanceof String) {
                selTypeName = (String) o;
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
            }
        }
    }

    /**
     * Store the name of the selected Linguistic Type.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        multiPane.putStepProperty("Type", selTypeName);

        return true;
    }

    /**
     * Enable the next/finish button once a valid type has been selected.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
	public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int row = typeTable.getSelectedRow();

            if (row > -1) {
                int col = model.findColumn(LinguisticTypeTableModel.NAME);
                String typeName = (String) model.getValueAt(row, col);

                for (int i = 0; i < rootTypes.size(); i++) {
                    LinguisticType t = rootTypes.get(i);

                    if (t.getLinguisticTypeName().equals(typeName)) {
                        selTypeName = typeName;

                        break;
                    }
                }

                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
            } else {
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
            }
        }
    }
}
