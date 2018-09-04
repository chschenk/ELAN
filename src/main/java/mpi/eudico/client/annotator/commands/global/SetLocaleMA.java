package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.event.ActionEvent;

import java.io.File;

import java.util.Locale;

import javax.swing.Action;


/**
 * A menu action to change the language of the ui.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SetLocaleMA extends FrameMenuAction {
    private Locale locale;

    /**
     * Creates a new SetLocaleMA instance
     *
     * @param name the name of the command
     * @param frame the frame (is needed for the empty frame, because listener
     *        registration takes a transcription as a key)
     * @param locale the new Locale
     */
    public SetLocaleMA(String name, ElanFrame2 frame, Locale locale) {
        super(name, frame);
        this.locale = locale;
        super.updateLocale(); // only once

        if (name.equals(ELANCommandFactory.CUSTOM_LANG)) {
            // check if the action should be enabled or disabled
            try {
                File custFile = new File(Constants.ELAN_DATA_DIR +
                        Constants.FILESEPARATOR + "ElanLanguage.properties");

                if (!custFile.exists()) {
                    setEnabled(false);
                }
            } catch (Exception ex) {
                // catch any exception
            }
        } else {
        	putValue(Action.NAME, name);
        }       
    }

    /**
     * Returns the Locale of this action
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ElanLocale.setLocale(locale);
        Preferences.set("Locale", ElanLocale.getLocale(), null);

        if (frame.getViewerManager() == null) { // not yet initialized, not yet registered as locale listener
            frame.updateLocale();
        }
    }

    /**
     * Ignore.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#updateLocale()
     */
    @Override
    public void updateLocale() {
    }
}
