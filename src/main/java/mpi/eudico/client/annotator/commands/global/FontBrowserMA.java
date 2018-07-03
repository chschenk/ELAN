package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.FontGui;


/**
 * Menu action that creates a Font Browser window.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class FontBrowserMA extends FrameMenuAction {
    private FontGui browser = null;

    /**
     * Creates a new FontBrowserMA instance
     *
     * @param name the name of the action
     */
    public FontBrowserMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates the browser if it does not exist or brings it to front if it is
     * already created.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (browser == null) {
            browser = new FontGui();

            // remove the default window listeners, one of them calls System.exit
            WindowListener[] wl = browser.getWindowListeners();

            for (int i = 0; i < wl.length; i++) {
                browser.removeWindowListener(wl[i]);
            }

            browser.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            // add a window listener that nullifies the browser member when
            // the font browser window is closed
            browser.addWindowListener(new WindowAdapter() {
                    @Override
					public void windowClosed(WindowEvent e) {
                        FontBrowserMA.this.browser.removeWindowListener(this); //??
                        FontBrowserMA.this.browser = null;
                    }
                });
            
            browser.setLocationRelativeTo(frame);
            browser.setVisible(true);
        } else {
            browser.setVisible(true);
            browser.toFront();
        }
    }
}
