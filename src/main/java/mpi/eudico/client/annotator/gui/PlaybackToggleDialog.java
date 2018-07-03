package mpi.eudico.client.annotator.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;


/**
 * A dialog to set the toggle values for playback rate and volume.
 * @version July 2009 independent of a transcription, sets and gets preferences
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class PlaybackToggleDialog extends ClosableDialog implements ActionListener {
    //private ViewerManager2 vm;
    private JButton okButton;
    private JButton cancelButton;
    private ElanSlider volumeSlider;
    private ElanSlider rateSlider;

    /**
     * Creates a new PlaybackToggleDialog instance
     *
     * @param owner the parent frame
     */
    public PlaybackToggleDialog(Frame owner) {
        super(owner, true);
        //this.vm = vm;

        //curRateValue = action.getValue();
        initComponents();
        pack();

        int w = 360;
        int h = 280;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);

        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("CommandActions.PlaybackToggle"));

        //setSize(350, 240);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(6, 6, 6, 6));

        JLabel label = new JLabel(ElanLocale.getString(
                    "PlaybackToggleDialog.Label"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        Insets inset = new Insets(2, 2, 12, 2);
        gbc.insets = inset;

        content.add(label, gbc);

        //JPanel volumePanel = new JPanel(new BorderLayout());
        //volumePanel.setBorder(new TitledBorder("Volume"));
        volumeSlider = new ElanSlider(ElanLocale.getString(
                    "MediaPlayerControlPanel.ElanSlider.Volume"), 0, 100, 100,
                null);
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = inset;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        content.add(volumeSlider, gbc);

        rateSlider = new ElanSlider(ElanLocale.getString(
                    "MediaPlayerControlPanel.ElanSlider.Rate"), 0, 200, 100, null);
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = inset;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        content.add(rateSlider, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        content.add(new JPanel(), gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        content.add(buttonPanel, gbc);
        setContentPane(content);
        
        // get the current toggle value
        /*
        float rate = ((PlaybackRateToggleCA)ELANCommandFactory.getCommandAction(
        	vm.getTranscription(),
        	ELANCommandFactory.PLAYBACK_RATE_TOGGLE)).getToggleValue();
        	*/
        float rate = 0.5f;
        Float val = Preferences.getFloat("PlaybackRateToggleValue", null);
        if (val != null) {
        	rate = val.floatValue();
        }
        rateSlider.setValue((int)(100 * rate));
        /*
        float vol = ((PlaybackVolumeToggleCA)ELANCommandFactory.getCommandAction(
        	vm.getTranscription(), 
        	ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE)).getToggleValue();
        	*/
        float vol = 0.7f;
        val = Preferences.getFloat("PlaybackVolumeToggleValue", null);
        if (val != null) {
        	vol = val.floatValue();
        }
        volumeSlider.setValue((int)(100 * vol));
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Action event handling.
     *
     * @param ae the action event
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == okButton) {
            float newRate = (float)rateSlider.getValue() / 100;
            float newVolume = (float)volumeSlider.getValue() / 100;
            /*
            Command c = ELANCommandFactory.createCommand(vm.getTranscription(),
                    ELANCommandFactory.PLAYBACK_TOGGLE);
            c.execute(vm.getTranscription(),
                new Object[] { new Float(newRate), new Float(newVolume) });
            */
            Preferences.set("PlaybackRateToggleValue", new Float(newRate), null, false, false);
            Preferences.set("PlaybackVolumeToggleValue", new Float(newVolume), null, true, true);
            closeDialog();
        } else {
            closeDialog();
        }
    }
}
