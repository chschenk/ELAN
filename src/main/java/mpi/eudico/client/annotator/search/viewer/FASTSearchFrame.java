package mpi.eudico.client.annotator.search.viewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.eudico.client.annotator.search.model.FASTSearchEngine;

/** 
 * The frame containing the FAST panel
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @since June 2013
 */
public class FASTSearchFrame extends ClosableFrame {
	private static final long serialVersionUID = -6144374835744538603L;
	FASTSearchPanel searchPanel;

	public FASTSearchFrame(ElanFrame2 elanFrame) {
		super("FASTSearch");
        ImageIcon icon = new ImageIcon(this.getClass()
                .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));

		if (icon != null) {
			setIconImage(icon.getImage());
		}
		searchPanel = new FASTSearchPanel(elanFrame);
		getContentPane().add(searchPanel);
		pack();
		
		// we need to dispose of our threadpool
		addWindowListener(new WindowAdapter() {
		      @Override
			public void windowClosing(WindowEvent e) {
		    	  ((FASTSearchEngine)(searchPanel.getSearchController())).closeEngine();
		      }
		});
	}
}
