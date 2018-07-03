package mpi.eudico.client.annotator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentEvent;


/**
 * A dialog/frame for detached viewers. The frame will always be on top.
 *
 * @author Han Sloetjes
 */
public class DetachedViewerFrame extends DetachedFrame {
    /**
     * Constructor. Sets a BorderLayout for the contentpane and adds the
     * viewer.
     *
     * @param layoutManager the ElanLayoutManager
     * @param component the viewer
     * @param title the title for the viewer
     */
    public DetachedViewerFrame(ElanLayoutManager layoutManager,
        Component component, String title) {
        super(layoutManager, component, title);

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(component);
    }

    /**
     * Override the superclass implementation: the layout manager will handle
     * the resizing of the component.
     *
     * @param e the component event!
     */
    @Override
	public void componentResized(ComponentEvent e) {
    }
}
