/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.search.viewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.CtrlWCloseAction;
import mpi.search.SearchLocale;
import mpi.search.content.viewer.AbstractComplexSearchFrame;


/**
 * $Id: ElanSearchFrame.java 46106 2017-05-08 10:10:31Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class ElanSearchFrame extends AbstractComplexSearchFrame {

    /**
     * Creates a new ElanSearchFrame object.
     *
     * @param viewerManager the viewer manager
     */
    public ElanSearchFrame(ViewerManager2 viewerManager) {
        super(new ElanSearchPanel(viewerManager));
        
        try {
	        ImageIcon icon = new ImageIcon(this.getClass()
	                .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));
	        setIconImage(icon.getImage());
        } catch (Throwable t) {
        	setIconImage(null);
        }

        JMenu editMenu = new JMenu(SearchLocale.getString("SearchDialog.Edit"));
        editMenu.add(ELANCommandFactory.getUndoCA(viewerManager.getTranscription()));
        editMenu.add(ELANCommandFactory.getRedoCA(viewerManager.getTranscription()));
        editMenu.addSeparator();
        editMenu.add(((ElanSearchPanel) searchPanel).replaceAction);
        ((ElanSearchPanel) searchPanel).replaceAction.setEnabled(false);

    	Component c = getToolBar(searchPanel);
    	if (c instanceof JToolBar) {
    		((JToolBar) c).addSeparator();
    		((JToolBar) c).add(((ElanSearchPanel) searchPanel).replaceAction);
    	}
        
        getJMenuBar().add(editMenu, 1);
        
        pack();
        Rectangle bounds = Preferences.getRect("SearchAndReplaceFrame.Bounds", null);
        if (bounds == null) {
	        setSize(660,600);
	        setLocationRelativeTo(getParent());
        } else {
        	setBounds(bounds);// check if it is on the current screen?
        }
        validate();
        // ensure that the panel is disconnected from the viewer manager and is no longer registered 
        // as ACMEditListener
        this.addWindowListener(new WindowAdapter(){
            @Override
			public void windowClosed(WindowEvent we) {
            	Preferences.set("SearchAndReplaceFrame.Bounds", we.getWindow().getBounds(), null);
                ((ElanSearchPanel)ElanSearchFrame.this.searchPanel).close();
            }
        });
        
        addCloseActions();
        
        setVisible(true);        
    }
    
    /**
     * Add the Escape and Ctrl-W close actions.
     */
    protected void addCloseActions() {
//        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction wAction = new CtrlWCloseAction(this);
        
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
//            String esc = "esc";
//            inputMap.put((KeyStroke) escAction.getValue(
//                    Action.ACCELERATOR_KEY), esc);
//            actionMap.put(esc, escAction);
            String wcl = "cw";
            inputMap.put((KeyStroke) wAction.getValue(
                    Action.ACCELERATOR_KEY), wcl);
            actionMap.put(wcl, wAction);
        }
    }
    
    protected Container getToolBar(Container container) {
    	Component c = null;
    	Container c2 = null;
        for (int i = 0; i < container.getComponentCount(); i++) {
        	c = container.getComponent(i);
        	if (c instanceof JToolBar) {
        		return (Container)c;
        	}
        	// recursion
        	if (c instanceof Container) {
	        	c2 = getToolBar((Container) c);
	        	if (c2 instanceof JToolBar) {
	        		return (Container)c2;
	        	}
        	}
        }
    	return null;
    }
    
}
