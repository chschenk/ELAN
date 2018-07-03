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
package mpi.eudico.client.annotator.search.result.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.client.annotator.grid.GridViewerPopupMenu;
import mpi.eudico.client.util.LinkButton;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.search.content.result.model.ContentMatch;
import mpi.search.result.model.Match;
import mpi.search.result.model.Result;
import mpi.search.result.model.ResultEvent;
import mpi.search.result.viewer.AbstractResultViewer;


/**
 * $Id: EAFMultipleFileResultViewer.java 43574 2015-03-23 17:20:48Z olasei $
 * $Author$ $Version$
 */
@SuppressWarnings("serial")
public class EAFMultipleFileResultViewer extends AbstractResultViewer
    implements ListDataListener {
    private AnnotationTable table;
    private EAFResultViewerTableModel dataModel;
    private ElanFrame2 elanFrame;
    private JPopupMenu popup;
    private LinkButton nextButton;
    private LinkButton previousButton;

    /**
     * Creates a new EAFMultipleFileResultViewer object.
     *
     * @param elanFrame DOCUMENT ME!
     */
    public EAFMultipleFileResultViewer(ElanFrame2 elanFrame) {
        this.elanFrame = elanFrame;
        dataModel = new EAFResultViewerTableModel();
        table = new AnnotationTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayout(new BorderLayout());
        table.setDefaultRenderer(Object.class,
            new EAFResultViewerGridRenderer(dataModel));
        table.updateLocale();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(Color.white);
        add(scrollPane, BorderLayout.CENTER);
        popup = new GridViewerPopupMenu(table);
        setTableListener();
        makeControlPanel();
    }

    /**
     * DOCUMENT ME!
     *
     * @param columnName DOCUMENT ME!
     * @param visible DOCUMENT ME!
     */
    public void setColumnVisible(String columnName, boolean visible) {
        table.setColumnVisible(columnName, visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @param list DOCUMENT ME!
     */
    public void setData(List<? extends AnnotationCore> list) {
        dataModel.updateAnnotations(list);
    }

    /**
     * Needed so components don't disappear when resizing the frame too small
     * ??
     *
     * @return the preferred size
     */
    @Override
	public Dimension getPreferredSize() {
        return table.getPreferredScrollableViewportSize();
    }

    /**
     *
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void contentsChanged(ListDataEvent e) {	
    		dataModel.setFirstRealIndex(result.getFirstShownRealIndex());
   		dataModel.updateAnnotations(result.getSubList());
    }

    /**
     *
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void intervalAdded(ListDataEvent e) {
    		dataModel.setFirstRealIndex(result.getFirstShownRealIndex());
        for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
            dataModel.addAnnotation((ContentMatch) result.getElementAt(i));
        }
    }

    /**
     *
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void intervalRemoved(ListDataEvent e) {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void reset() {
        super.reset();
        setData(new ArrayList<AnnotationCore>());
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void resultChanged(ResultEvent e) {
        result = (Result) e.getSource();
        if ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                (result.getStatus() == Result.INIT)) {
        	
            result.addListDataListener(this);
        }

        if (e.getType() == ResultEvent.STATUS_CHANGED && result.getRealSize() == 0) {
            reset();
        } else {
        	if(e.getType() == ResultEvent.PAGE_COUNT_INCREASED){
        		controlPanel.setVisible(true);
        		updateButtons();
        	}
        	else if ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                    ((result.getStatus() == Result.COMPLETE) ||
                    (result.getStatus() == Result.INTERRUPTED))) {
                 updateButtons();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param match DOCUMENT ME!
     */
    public void showMatch(Match match) {
   }

    /**
     * DOCUMENT ME!
     *
     * @param result DOCUMENT ME!
     */
    @Override
	public void showResult(Result result) {
    	System.out.println("In show");
        setData(result.getMatches());
        updateButtons();
    }

    /**
     * This method sets listeners for the table
     */
    protected void setTableListener() {
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
                @Override
				public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        return;
                    }

                    int selrow = table.getSelectedRow();

                    if (selrow < 0) {
                        return;
                    }

                    if (elanFrame != null) {
                        int fileColumn = dataModel.findColumn(EAFResultViewerTableModel.FILENAME);
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                        //ElanFrame2 newElanFrame = elanFrame.getFrameFor(
                        //        (String) dataModel.getValueAt(selrow, fileColumn));
                        final ElanFrame2 newElanFrame = FrameManager.getInstance()
                                                                    .getFrameFor((String) dataModel.getValueAt(
                                    selrow, fileColumn));
                        setCursor(Cursor.getPredefinedCursor(
                                Cursor.DEFAULT_CURSOR));

                        if (newElanFrame != null) {
                            final ContentMatch match = ((ContentMatch) dataModel.getValueAt(selrow,
                                    dataModel.findColumn(
                                        EAFResultViewerTableModel.ANNOTATION)));

                            if (newElanFrame.getViewerManager() != null && newElanFrame.isFullyInitialized()) {
                                newElanFrame.getViewerManager().getSelection()
                                            .setSelection(match.getBeginTimeBoundary(),
                                    match.getEndTimeBoundary());
                                newElanFrame.getViewerManager()
                                            .getMasterMediaPlayer()
                                            .setMediaTime(match.getBeginTimeBoundary());
                            } else {
                            	 new Thread(new Runnable() {// new thread, this doesn't work on the eventqueue with invokeLater
                                     @Override
									public void run() {        
                                     	  // check initialization of frame, use a time out period
                                     	  long timeOut = System.currentTimeMillis() + 60000;	// one minute
                                     	  while (!newElanFrame.isFullyInitialized() && System.currentTimeMillis() < timeOut) {
                                     		  try {
                                     			  Thread.sleep(200);
                                     		  } catch (InterruptedException ie) {
                                     			  
                                     		  }
                                     	  }
                                     	  
                                     	  // If the frame still isn't initialized, maybe the user is in some dialog to
                                     	  // locate a missing media file. There is no way to know how long that will take.
                                     	  // Just give up after the time-out.
                                     	  
                                     	  if (!newElanFrame.isFullyInitialized()) {
                                     		  return;
                                     	  }

                                         newElanFrame.getViewerManager().getSelection()
                                                     .setSelection(match.getBeginTimeBoundary(),
                                                             match.getEndTimeBoundary());
                                         newElanFrame.getViewerManager()
                                                     .getMasterMediaPlayer()
                                                     .setMediaTime(match.getBeginTimeBoundary());
                                         /*
                                         TierImpl t = (TierImpl) newElanFrame.getViewerManager()
                                                                             .getTranscription()
                                                                             .getTierWithId(tierName);

                                         if (t != null) {
                                             Annotation ann = t.getAnnotationAtTime(beginTime);

                                             if (ann != null) {
                                                 newElanFrame.getViewerManager()
                                                             .getActiveAnnotation()
                                                             .setAnnotation(ann);
                                             }
                                         }
                                         */
                                     }
                                 }).start();
                            	/*
                                EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            newElanFrame.getViewerManager()
                                                        .getSelection()
                                                        .setSelection(match.getBeginTimeBoundary(),
                                                match.getEndTimeBoundary());
                                            newElanFrame.getViewerManager()
                                                        .getMasterMediaPlayer()
                                                        .setMediaTime(match.getBeginTimeBoundary());
                                        }
                                    });
                                    */
                            }

                            // HS 07 apr 2005            
                            newElanFrame.toFront();
                            SwingUtilities.windowForComponent(EAFMultipleFileResultViewer.this)
                                          .toFront();
                        }
                    }
                }
            });

        table.addMouseListener(new MouseAdapter() {
                @Override
				public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) ||
                            e.isPopupTrigger()) {
                        popup.show(EAFMultipleFileResultViewer.this, 100, 20);
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    protected void makeControlPanel() {
        previousButton = new LinkButton(previousAction);
        nextButton = new LinkButton(nextAction);
        previousButton.setVisible(false);
        nextButton.setVisible(false);

        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        controlPanel.add(previousButton);
        controlPanel.add(currentLabel);
        controlPanel.add(nextButton);
        controlPanel.add(javax.swing.Box.createHorizontalGlue());
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void updateButtons() {
        super.updateButtons();
        previousButton.setVisible(previousAction.isEnabled());
        nextButton.setVisible(nextAction.isEnabled());
        previousButton.setLabel(intervalToString(getPreviousInterval()));
        nextButton.setLabel(intervalToString(getNextInterval()));
        ((java.awt.Window) getTopLevelAncestor()).validate();
    }
}
