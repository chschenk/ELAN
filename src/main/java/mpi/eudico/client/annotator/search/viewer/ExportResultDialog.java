package mpi.eudico.client.annotator.search.viewer;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.AbstractBasicExportDialog;
import mpi.eudico.client.annotator.search.result.viewer.ContentMatch2TabDelimitedText;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.search.content.query.model.ContentQuery;


/**
 * Dialog to chose file type and parameters for the export of a result
 * 
 * $Id: ExportResultDialog.java 43915 2015-06-10 09:02:42Z olasei $
 *
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class ExportResultDialog extends AbstractBasicExportDialog
    implements ActionListener {
    private final ButtonGroup fileTypeGroup = new ButtonGroup();
    private final ButtonGroup formatTypeGroup = new ButtonGroup();
    private final ContentQuery query;

    /** insets for ui components */
    private final JLabel dataFormatLabel = new JLabel();
    private final JLabel fileFormatLabel = new JLabel();
    private final JRadioButton asTableButton = new JRadioButton();
    private final JRadioButton asTreeButton = new JRadioButton();
    private final JRadioButton htmlButton = new JRadioButton();
    private final JRadioButton tabButton = new JRadioButton();

    /**
     * Creates a new ExportElanMatchDialog object.
     *
     * @param parent parent window
     * @param modal should be always true
     * @param transcription transcription
     * @param query query (containing result)
     */
    public ExportResultDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, ContentQuery query) {
        super(parent, modal, transcription);
        this.query = query;
        makeLayout();
        updateLocale();
        postInit();
    }

    @Override
	protected void makeLayout() {
        super.makeLayout();
        getContentPane().setLayout(new GridBagLayout());
        fileTypeGroup.add(tabButton);
        fileTypeGroup.add(htmlButton);
        formatTypeGroup.add(asTableButton);
        formatTypeGroup.add(asTreeButton);

        JPanel fileFormatPanel = new JPanel(new GridLayout(0, 1));
        fileFormatPanel.add(tabButton);
        fileFormatPanel.add(htmlButton);

        JPanel dataFormatPanel = new JPanel(new GridLayout(0, 1));
        dataFormatPanel.add(asTableButton);
        dataFormatPanel.add(asTreeButton);

        optionsPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 0.25;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;

        optionsPanel.add(fileFormatLabel, c);
        c.weighty = 0.5;
        optionsPanel.add(fileFormatPanel, c);
        c.weighty = 0;
        optionsPanel.add(new JSeparator(), c);
        c.weighty = 0.25;
        optionsPanel.add(dataFormatLabel, c);
        c.weighty = 0.5;
        optionsPanel.add(dataFormatPanel, c);

        c.anchor = GridBagConstraints.CENTER;
        c.weighty = 0.0;
        getContentPane().add(titleLabel, c);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        getContentPane().add(optionsPanel, c);
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        getContentPane().add(buttonPanel, c);

        asTreeButton.setVisible(query.getAnchorConstraint().getChildCount() > 0);
        asTreeButton.setEnabled(false);

        htmlButton.addChangeListener(new ChangeListener() {
                @Override
				public void stateChanged(ChangeEvent e) {
                    if (tabButton.isSelected()) {
                        asTableButton.setSelected(true);
                    }

                    asTreeButton.setEnabled(htmlButton.isSelected());
                }
            });
        tabButton.setSelected(true);
        asTableButton.setSelected(true);
    }

    @Override
	protected boolean startExport() throws IOException {
        File exportFile = null;
        if (tabButton.isSelected()) {
            exportFile = promptForFile(ElanLocale.getString(
            "ExportResultDialog.Title"), null, FileExtension.TEXT_EXT, true);
        } else {
            exportFile = promptForFile(ElanLocale.getString(
            "ExportResultDialog.Title"), null, FileExtension.HTML_EXT,true);
        }

        if (exportFile == null) {
            return false;
        }

        if (tabButton.isSelected()) {
            ContentMatch2TabDelimitedText.exportMatches(query.getResult()
                                                             .getMatches(),
                exportFile, encoding);
        } else {
            ElanQuery2HTML.exportQuery(query, exportFile,
                asTableButton.isSelected(), transcription.getName(), encoding);
        }

        return true;
    }

    /**
     * Set the localized text on ui elements
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportResultDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportResultDialog.Title"));
        asTableButton.setText(ElanLocale.getString("ExportResultDialog.AsTable"));
        asTreeButton.setText(ElanLocale.getString("ExportResultDialog.AsTree"));
        htmlButton.setText(ElanLocale.getString(
                "ExportDialog.FileDescription.Html"));
        tabButton.setText(ElanLocale.getString(
                "ExportResultDialog.TabDelimitedText"));

        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.Options")));
        fileFormatLabel.setText(ElanLocale.getString(
                "ExportResultDialog.FileFormat"));
        dataFormatLabel.setText(ElanLocale.getString(
                "ExportResultDialog.ExportMatches"));
    }
}
