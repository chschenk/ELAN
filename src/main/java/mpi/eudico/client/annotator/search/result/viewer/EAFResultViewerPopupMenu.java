package mpi.eudico.client.annotator.search.result.viewer;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportResultTableAsEAF;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.client.annotator.grid.GridViewerPopupMenu;


public class EAFResultViewerPopupMenu extends GridViewerPopupMenu {
    final private JMenuItem exportAsEAFMenuItem;

    /**
     * @param table
     */
    public EAFResultViewerPopupMenu(AnnotationTable table) {
        super(table);
        
        exportAsEAFMenuItem = new JMenuItem(ElanLocale.getString("Frame.GridFrame.ExportTableAsEAF"));
        exportAsEAFMenuItem.addActionListener(this);
    }

    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exportAsEAFMenuItem) {
            // export results as eaf
            ExportResultTableAsEAF exporter = new ExportResultTableAsEAF();
            exporter.exportTableAsEAF(table);
        } else {
            super.actionPerformed(e);    
        }
    }
    
    /**
     * Calls super.makeLayout and adds an additional item, export with context as eaf.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();
        add(exportAsEAFMenuItem);
    }
    
}
