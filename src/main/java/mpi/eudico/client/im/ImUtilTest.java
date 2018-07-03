package mpi.eudico.client.im;


/*
   DONE
   - cursors instead of numbers
   - cursors respect borders
   - page up/down
   TODO
   - locales displays
 */
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;

import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


/**
 * DOCUMENT ME!
 * $Id: ImUtilTest.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class ImUtilTest extends JFrame implements ActionListener {
    private Locale[] allLanguages;
    private JTextArea jTextArea;

    /**
     * Creates a new ImUtilTest instance
     *
     * @throws Exception DOCUMENT ME!
     */
    public ImUtilTest() throws Exception {
        jTextArea = new JTextArea(25, 53);

        // create the page
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.add(jTextArea, null);

        // get the list of all kanguages.
        allLanguages = ImUtil.getLanguages(jTextArea);

        // create the menu
        MenuBar menuBar = new MenuBar();
        this.setMenuBar(menuBar);

        Menu fileMenu = new Menu("File");
        Menu selectLanguageMenu = new Menu("Select Language");
        menuBar.add(fileMenu);
        menuBar.add(selectLanguageMenu);

        for (int i = 0; i < allLanguages.length; i++) {
            addItem(selectLanguageMenu, allLanguages[i].getDisplayName());
        }

        addItem(fileMenu, "Exit");
        addFocusListener(new FocusAdapter() {
                @Override
				public void focusGained(FocusEvent e) {
                    jTextArea.requestFocus();
                }
            });

        ImUtil.setLanguage(jTextArea, allLanguages[0]);
        setLocation(60, 30);
        pack();
        setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param a DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] a) throws Exception {
        IUT iut = new IUT() {
                @Override
				public void go() throws Exception {
                    new ImUtilTest();
                }
            };

        iut.go();
    }

    /*
       add a MenuItem to a given Menu
     */
    private void addItem(Menu menu, String text) {
        MenuItem newItem = new MenuItem(text);
        newItem.addActionListener(this);
        menu.add(newItem);
    }

    /*
       Satisfying the ActionListener interface
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Exit")) {
            System.exit(0);
        }

        for (int i = 0; i < allLanguages.length; i++) {
            if (command.equals(allLanguages[i].getDisplayName())) {
                ImUtil.setLanguage(jTextArea, allLanguages[i]);

                return;
            }
        }
    }

    /**
     * Overridden so we can exit when window is closed
     *
     * @param e DOCUMENT ME!
     */
    @Override
	protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }

    /**
     * DOCUMENT ME!
     * $Id: ImUtilTest.java 43915 2015-06-10 09:02:42Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    interface IUT {
        /**
         * DOCUMENT ME!
         *
         * @throws Exception DOCUMENT ME!
         */
        void go() throws Exception;
    }
}
