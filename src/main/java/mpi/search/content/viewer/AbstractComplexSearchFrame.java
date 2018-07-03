package mpi.search.content.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.gui.HTMLViewer;
import mpi.search.SearchLocale;

public class AbstractComplexSearchFrame extends JFrame {
	private Action helpAction;
	
	protected final AbstractComplexSearchPanel searchPanel;
    /** Holds value of property DOCUMENT ME! */
	
	public AbstractComplexSearchFrame(AbstractComplexSearchPanel searchPanel){
        super(SearchLocale.getString("SearchDialog.Title"));
		this.searchPanel = searchPanel;
		getContentPane().add(searchPanel);
		createMenuBar();
	    addWindowListener(
        new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent event) {
                AbstractComplexSearchFrame.this.searchPanel.stopSearch();
            }
        });
	}

	private void createMenuBar() {
	       helpAction = new AbstractAction(SearchLocale.getString("Action.Help")) {
	            @Override
				public void actionPerformed(ActionEvent e) {
	                showInfoDialog();
	            }
	        };
	        helpAction.putValue(Action.SHORT_DESCRIPTION, SearchLocale
	                .getString("Action.Tooltip.Help"));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(SearchLocale.getString("SearchDialog.File"));
        fileMenu.add(searchPanel.getCloseAction());
        menuBar.add(fileMenu);

        JMenu queryMenu = new JMenu(SearchLocale.getString("SearchDialog.Query"));
        queryMenu.add(searchPanel.getStartAction());
        queryMenu.add(searchPanel.zoomAction);
        queryMenu.addSeparator();
        queryMenu.add(searchPanel.saveAction);
        queryMenu.add(searchPanel.readAction);
        queryMenu.add(searchPanel.getExportAction());
        menuBar.add(queryMenu);

        JMenu helpMenu = new JMenu(SearchLocale.getString("SearchDialog.Help"));
        helpMenu.add(helpAction);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
	
    private void showInfoDialog() {
//      JDialog infoDialog = new SimpleHtmlViewer(this, "Search Info", false,
//               "/mpi/eudico/client/annotator/resources/SearchHelp.html");
    	//TODO this is an undesirable dependency on the client packages
    	try {
	      HTMLViewer viewer = new HTMLViewer("/mpi/eudico/client/annotator/resources/SearchHelp.html", 
	    		  false, "Search Info");
	      JDialog infoDialog = viewer.createHTMLDialog(this);
	      infoDialog.pack();
	      infoDialog.setSize(600, 400);
	      infoDialog.setVisible(true);
    	} catch (IOException ioe) {
    		// message box
    		JOptionPane.showMessageDialog(this, ("Unable to load the Search help file " + ioe.getMessage()), 
    				"Warning", JOptionPane.WARNING_MESSAGE, null);
    	}
  }

}
