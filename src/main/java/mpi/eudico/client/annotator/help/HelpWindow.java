package mpi.eudico.client.annotator.help;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.MainWindow;
import javax.help.Presentation;
import javax.help.WindowPresentation;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;


/**
 * A singleton class that creates and returns the JavaHelp window.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class HelpWindow {
    private static Window helpWindow = null;

    /**
     * Creates a new HelpWindow instance
     */
    private HelpWindow() {
    }

    /**
     * Creates (if necessary) and returns the single help window.
     *
     * @return the help window
     *
     * @throws HelpException if the helpfiles can not be found or the helpset
     *         can not be read, or the JavaHelp libraries could not be loaded
     */
    public static Window getHelpWindow() throws HelpException {
        if (helpWindow == null) {
            try {
                URL url = null;
                url = HelpWindow.class.getResource("/help/jhelpset.hs");
                //System.out.println("Help url: " + url);

                HelpSet hs = null;
                ClassLoader cl = HelpWindow.class.getClassLoader();
                hs = new HelpSet(cl, url);

                Presentation pres = MainWindow.getPresentation(hs, "ELAN Help");

                if (pres instanceof WindowPresentation) {
                    ((WindowPresentation) pres).createHelpWindow();
                    //((WindowPresentation) pres).setToolbarDisplayed(false);
                    helpWindow = ((WindowPresentation) pres).getHelpWindow();
                }

                if (helpWindow == null) {
                    throw new HelpException("Help window is null");
                } else {
                    try {
                        ImageIcon icon = new ImageIcon(HelpWindow.class.getResource(
                                    "/mpi/eudico/client/annotator/resources/ELAN16.png"));

                        if (helpWindow instanceof Frame && (icon != null)) {
                            ((Frame) helpWindow).setIconImage(icon.getImage());
                        }
                    } catch (Exception ee) {
                    }
                    // disable print buttons because of possible crashes
                    checkComponentsForButton(((JFrame)helpWindow).getContentPane());
                }
            } catch (HelpSetException hse) {
                //hse.printStackTrace();
                throw new HelpException("Could not create help window: " +
                    hse.getMessage(), hse.getCause());
            } catch (Exception ex) {
                //ex.printStackTrace();
                throw new HelpException("Could not create help window: " +
                    ex.getMessage(), ex.getCause());
            } catch (Error err) {
                //err.printStackTrace();
                throw new HelpException("Could not create help window: " +
                    err.getMessage(), err.getCause());
            }
        }

        return helpWindow;
    }
    
    private static void checkComponentsForButton(Component c) {
    	if (c instanceof JButton) {
    		JButton butt = (JButton)c;
    		//System.out.println("Button: " + butt.getIcon());
    		if (butt.getAccessibleContext() != null) {
    			String name = butt.getAccessibleContext().getAccessibleName();
    			if ("Print Button".equals(name) || "Page Setup Button".equals(name)) {
    				butt.setEnabled(false);
    			}
    		}
    		return;
    	}
    	if (c instanceof Container) {
    		Component[] comps = ((Container)c).getComponents();
    		for (Component cc : comps) {
    			checkComponentsForButton(cc);
    		}
    	}
    }
}
