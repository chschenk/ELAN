package mpi.eudico.client.annotator.dcr;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;


/**
 * A dialog to set the data category reference for an annotation.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class AnnotationDCRDialog extends ClosableDialog
    implements ActionListener {
    private Transcription transcription;
    private AbstractAnnotation annotation;

    /** resource bundle containing localized strings */
    protected ResourceBundle bundle = null;

    /** the name of a DCR */
    protected String dcrName;

    /** the location of the DCR */
    protected String dcrLocation;

    /** the title panel */
    protected JPanel titlePanel;

    /** the title label */
    protected JLabel titleLabel;

    /** the subtitle label */
    protected JLabel subtitleLabel;
    
    /** a second subtitle label */
    protected JLabel subtitleLabel2;

    /** the panel to set the refernece for an annotation */
    protected AnnotationDCPanel2 dcPanel;

    /** a panel for buttons */
    protected JPanel buttonPanel;

    /** the apply button */
    protected JButton applyButton;

    /** the cancel button */
    protected JButton cancelButton;

    /**
     * Creates a dialog.
     *
     * @param transcription the transcription the annotation is part of
     * @param annotation the annotation to set the reference for
     *
     * @throws HeadlessException headless exception
     */
    public AnnotationDCRDialog(Transcription transcription,
        AbstractAnnotation annotation) throws HeadlessException {
        super((Frame) null, true);
        this.transcription = transcription;
        this.annotation = annotation;

        initComponents();
    }

    /**
     * Creates a dialog.
     *
     * @param owner the owner frame
     * @param modal the modal flag
     * @param transcription the transcription the annotation is part of
     * @param annotation the annotation to set the reference for
     *
     * @throws HeadlessException headless exception
     */
    public AnnotationDCRDialog(Frame owner, boolean modal,
        Transcription transcription, AbstractAnnotation annotation)
        throws HeadlessException {
//        super(owner,ElanLocale.getString("Menu.Annotation.ModifyAnnotationDatCat"), modal);
    	 super(owner, modal);
        this.transcription = transcription;
        this.annotation = annotation;

        initComponents();
    }

    /**
     * @see mpi.eudico.client.annotator.dcr.ELANDCRDialog#initComponents()
     */
    protected void initComponents() {
        bundle = ElanLocale.getResourceBundle();
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titlePanel = new JPanel(new GridBagLayout());
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel = new JLabel();
        subtitleLabel.setFont(titleLabel.getFont().deriveFont((float) 10));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel2 = new JLabel();
        subtitleLabel2.setFont(titleLabel.getFont().deriveFont((float) 12));
        subtitleLabel2.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        titlePanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = insets;
        titlePanel.add(subtitleLabel, gbc);
        gbc.gridy = 2;
        titlePanel.add(subtitleLabel2, gbc);
		gbc.gridy = 0;
        getContentPane().add(titlePanel, gbc);
        
//        String annLabel = null;
//
//        if (annotation != null) {
//            annLabel = ElanLocale.getString("Menu.Tier") + ": " +
//                annotation.getTier().getName() + " - " +
//                ElanLocale.getString("Menu.Annotation") + ": " +
//                annotation.getValue();
//        }
//
//        dcPanel = new AnnotationDCPanel(ELANLocalDCRConnector.getInstance(),
//                bundle, annLabel);
//                
//        String dcId = null;
//
//        if ((annotation != null) && (annotation.getExtRef() != null)) {
//        	dcId = annotation.getExtRefValue(ExternalReference.ISO12620_DC_ID);
//        	/*
//            if (annotation.getExtRef() instanceof String) {
//                dcId = (String) annotation.getExtRef();
//            } else if (annotation.getExtRef() instanceof ExternalReference) {
//                dcId = ((ExternalReference) annotation.getExtRef()).getValue();
//            }
//            */
//        }
//
//        dcPanel.setAnnotationDCId(dcId);

        
        dcPanel = new AnnotationDCPanel2(ELANLocalDCRConnector.getInstance(),
              bundle);
       
        dcPanel.setPreferredLanguage(ELANLocalDCRConnector.getInstance().getPreferedLanguage());
        dcPanel.setAnnotation(annotation.getTier().getName(), annotation.getValue(), annotation.getExtRefValue(ExternalReference.ISO12620_DC_ID));
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 1;
        gbc.insets = insets;
        getContentPane().add(dcPanel, gbc);

        // buttons
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        applyButton = new JButton();
        applyButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);
        updateLocale();
    }

    /**
     * @see mpi.eudico.client.annotator.dcr.ELANDCRDialog#updateLocale()
     */
    protected void updateLocale() {
    	subtitleLabel2.setText(ElanLocale.getString("DCR.Label.LocalDCS"));
        titleLabel.setText(ElanLocale.getString("DCR.Label.AnnDataCat"));
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
    }

    /**
     * @see mpi.eudico.client.annotator.dcr.ELANDCRDialog#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            // create command
            if (annotation != null) {
                Command com = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.MODIFY_ANNOTATION_DC);
                com.execute(annotation,
                    new Object[] { dcPanel.getAnnotationDCId() });
            }

            // test
            /*
            ExternalReferenceGroup erg = new ExternalReferenceGroup();

            erg.addReference(new ExternalReferenceImpl("blah",
                    ExternalReference.ISO12620_DC_ID));
            erg.addReference(new ExternalReferenceImpl("boo",
                    ExternalReference.ISO12620_DC_ID));

            ExternalReferenceGroup erg2 = new ExternalReferenceGroup();
            erg2.addReference(new ExternalReferenceImpl("zoop",
                    ExternalReference.ISO12620_DC_ID));
            erg.addReference(erg2);

            try {
                erg.clone();
            } catch (CloneNotSupportedException cnse) {
                cnse.printStackTrace();
            }
            */
        }

        setVisible(false);
        dispose();
    }
}
