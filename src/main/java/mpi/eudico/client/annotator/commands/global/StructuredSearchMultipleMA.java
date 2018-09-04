package mpi.eudico.client.annotator.commands.global;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.smfsearch.StructuredMultipleFileSearchFrame;


/**
 * A menu action that creates the structured search window.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class StructuredSearchMultipleMA extends FrameMenuAction implements WindowListener {
	/** count the number of open windows, after the last one is closed check the setting for 
	 * tooltips */
	private static int numWindows = 0;
	/*
	 * HS Aug 2013:up to ELAN 4.6.1 it has been possible to open multiple instances of the search window.
	 * But when regular expressions are used (if only behind the scenes) interference of queries can occur
	 * because of a static member in HSQLDB for storing regular expressions in the DB.
	 * Therefore from now there will exist no more than one search window per VM.   
	 */
	private static JFrame globalSearchFrame = null;
	
    /**
     * Creates a new StructuredSearchMultipleMA instance
     *
     * @param name the name of the command
     * @param frame the parent frame
     */
    public StructuredSearchMultipleMA(String name, ElanFrame2 frame) {
        super(name, frame);
        
        numWindows++;
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (globalSearchFrame != null) {
    		if (!globalSearchFrame.isVisible()) {
    			globalSearchFrame.setVisible(true);
    		}
    		globalSearchFrame.toFront();
    	} else {
	        //JFrame searchFrame = new StructuredMultipleFileSearchFrame(frame);
    		globalSearchFrame = new StructuredMultipleFileSearchFrame(frame);
    		
	        Point p = Preferences.getPoint("MFSearchFrame.Location", null);
	        
	        if (p != null) {
	        	final int MARGIN = 30;
	        	
	        	Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	        	if (p.x < wRect.x) {
	        		p.x = wRect.x;
	        	} else if (p.x > wRect.width - MARGIN) {
	        		p.x = wRect.width - MARGIN;
	        	}
	        	if (p.y < wRect.y) {
	        		p.y = wRect.y;
	        	} else if (p.y > wRect.height - MARGIN) {
	        		p.y = wRect.height - MARGIN;
	        	}
	        	
	        	globalSearchFrame.setLocation(p);
	        }
	        
	        Dimension size = Preferences.getDimension("MFSearchFrame.Size", null);
	        
	        if (size != null) {
	        	globalSearchFrame.setSize(size);
	        }
	        
	        globalSearchFrame.addWindowListener(this);
	        globalSearchFrame.setVisible(true);
    	}
    }

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getWindow() != null) {
			Preferences.set("MFSearchFrame.Location", e.getWindow().getLocation(), null, false, false);
			Preferences.set("MFSearchFrame.Size", e.getWindow().getSize(), null, false, false);
			
			e.getWindow().removeWindowListener(this);
			numWindows--;
			globalSearchFrame = null;
			if (numWindows == 0) {
				Boolean ttPref = Preferences.getBool("UI.ToolTips.Enabled", null);
				
				if (ttPref != null) {
					if (!ttPref) {// tooltips globally disabled
						// the search might have enabled tooltips
						if (ToolTipManager.sharedInstance().isEnabled()) {
							ToolTipManager.sharedInstance().setEnabled(false);
						}
					}
				}
			}
		}
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
