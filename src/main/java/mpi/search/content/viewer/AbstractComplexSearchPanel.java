package mpi.search.content.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import mpi.search.SearchLocale;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.viewer.QueryPanel;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentMatchCounter;
import mpi.search.gui.TriptychLayout;
import mpi.search.query.model.Query;
import mpi.search.query.model.QueryManager;
import mpi.search.query.viewer.AbstractSimpleSearchPanel;
import mpi.search.query.viewer.StartStopPanel;



/**
 * Created on Jul 21, 2004
 *
 * @author Alexander Klassmann
 * @version November 2004
 */
@SuppressWarnings("serial")
public abstract class AbstractComplexSearchPanel
    extends AbstractSimpleSearchPanel {
    /** action to read query/results from file */
    protected Action readAction;

    /** action to save query/results to file */
    protected Action saveAction;

    /** action to specify (sub)query on existing result */
    protected Action zoomAction;

    /** holds name of corpus */
    protected JLabel corpusLabel;

    /** Holds counter, start/StopButtons and progressViewer */
    protected final JPanel bottomPanel = new JPanel();

    /** stores queries */
    protected final QueryManager queryManager = new QueryManager();

    /** Holds panel for query formulation */
    protected QueryPanel queryPanel;
    private final JLabel resultCounterLabel = new JLabel();

    /** Panel to contain toolbar and queryPanel */
    private final JPanel toolAndQueryPanel = new JPanel(new BorderLayout());

    /** Holds value of property DOCUMENT ME! */
    private final JToolBar toolBar = new JToolBar();

    /** action to go to last result */
    private Action backAction;

    /** action to go to next result */
    private Action forwardAction;

    /** Holds action to open empty query panel */
    private Action newAction;

    /**
     * Constructor that has a ViewerManager as an argument. <b>Note: </b>the
     * viewer manager is currently needed to be able to connect and disconnect
     * as a listener. The SearchDialog is not an AbstractViewer and is
     * (currently) not created by the viewer manager, so it has to be
     * connected seperately.
     */
    public AbstractComplexSearchPanel() {
        matchCounter = new ContentMatchCounter();
        createActions();
    }

    @Override
	protected Query getQuery() {
        ContentQuery query = queryPanel.getQuery();

        if (!query.equals(queryManager.getCurrentQuery())) {
            queryManager.addQuery(query);

            return query;
        } else if (queryManager.getCurrentQuery().getResult().getStatus() != ContentResult.COMPLETE) {
            return queryManager.getCurrentQuery(); // is equal but not the same object
        }

        return null;
    }

    protected void addQuery(ContentQuery query) {
        queryPanel.setQuery(query);
        clearResult();
    }

    protected void clearResult() {
        resultViewer.reset();
        matchCounter.setVisible(false);
        progressViewer.setVisible(false);
        queryManager.setCurrentQueryNumber(queryManager.size() + 1);
        updateActions();
    }

    protected void gotoQuery(int nr) {
        queryManager.setCurrentQueryNumber(nr);

        ContentQuery query = (ContentQuery) queryManager.getCurrentQuery();

        if (query.getResult() != null) {
            queryPanel.setQuery(query);

            if (query.getResult().getStatus() == ContentResult.MODIFIED) {
                startSearch();
            } else {
                matchCounter.setResult(query.getResult());
                matchCounter.setVisible(true);
                progressViewer.setStatus(query.getResult().getStatus());
                progressViewer.setVisible(true);
                updateActions();
                resultViewer.showResult(query.getResult());
            }
        }
    }

    protected void makeLayout() {
        // toolBar.add(corpusLabel);
        toolBar.addSeparator();
        toolBar.add(startAction);
        toolBar.add(stopAction);
        toolBar.add(newAction);
        toolBar.add(zoomAction);
        toolBar.addSeparator();
        toolBar.add(saveAction);
        toolBar.add(readAction);
        toolBar.add(exportAction);
        toolBar.addSeparator();
        toolBar.add(backAction);
        toolBar.add(resultCounterLabel);
        toolBar.add(forwardAction);
        toolBar.addSeparator();
        toolBar.setBorderPainted(true);
        toolBar.setBorder(new LineBorder(Color.gray));
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setFloatable(false);	// the toolbar should be in a BorderLayout for floating to work.

        toolAndQueryPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        toolAndQueryPanel.add(toolBar, c);

        //
        JPanel queryCont = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        queryCont.add(queryPanel, c);

        JPanel filler = new JPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 10.0;
        queryCont.add(filler, c);

        //
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JScrollPane scrollPane = new JScrollPane(queryCont);

        //scrollPane.setPreferredSize(new Dimension(100, 300));
        toolAndQueryPanel.add(scrollPane, c);

        //toolAndQueryPanel.add(toolBar, BorderLayout.NORTH);
        //toolAndQueryPanel.add(queryPanel, BorderLayout.CENTER);
        toolAndQueryPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        bottomPanel.setLayout(new TriptychLayout());

        matchCounter.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3),
                new LineBorder(Color.gray)));

        progressViewer.setBorder(new CompoundBorder(
                new EmptyBorder(3, 3, 3, 3), new LineBorder(Color.gray)));

        startStopPanel = new StartStopPanel(startAction, stopAction, closeAction);

        bottomPanel.add(TriptychLayout.LEFT, matchCounter);
        bottomPanel.add(TriptychLayout.RIGHT, progressViewer);
        bottomPanel.add(TriptychLayout.CENTER, startStopPanel);
        matchCounter.setVisible(false);
        progressViewer.setVisible(false);

        setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                toolAndQueryPanel, (JComponent) resultViewer);
        splitPane.setResizeWeight(0.8);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(splitPane, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(bottomPanel, c);

        updateActions();
    }

    protected abstract void read();

    protected abstract void save();

    @Override
	protected void updateActions() {
        super.updateActions();

        boolean executing = (searchEngine != null) &&
            searchEngine.isExecuting();
        backAction.setEnabled(!executing && queryManager.hasPreviousQuery());
        forwardAction.setEnabled(!executing && queryManager.hasNextQuery());

        boolean positiveResult = !executing &&
            (queryManager.getCurrentQuery() != null) &&
            (queryManager.getCurrentQuery().getResult().getRealSize() > 0) &&
            (queryManager.getCurrentQuery().getResult().getStatus() != ContentResult.MODIFIED);

        saveAction.setEnabled(positiveResult &&
            !((ContentQuery)queryManager.getCurrentQuery()).isRestricted());
        exportAction.setEnabled(positiveResult);
        zoomAction.setEnabled(positiveResult);

        resultCounterLabel.setText((queryManager.getCurrentQueryNumber() > 0)
            ? ("" + queryManager.getCurrentQueryNumber()) : "");
    }

    private void createActions() {
        Icon searchIcon = null;
        Icon stopIcon = null;
        Icon newIcon = null;
        Icon zoomIcon = null;
        Icon saveIcon = null;
        Icon readIcon = null;
        Icon exportIcon = null;
        Icon backIcon = null;
        Icon forwardIcon = null;

        try {
            searchIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"));
            stopIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/media/Stop16.gif"));
            newIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/New16.gif"));
            zoomIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Zoom16.gif"));
            saveIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));
            readIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
            exportIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Export16.gif"));
            backIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif"));
            forwardIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startAction.putValue(Action.SMALL_ICON, searchIcon);
        stopAction.putValue(Action.SMALL_ICON, stopIcon);
        exportAction.putValue(Action.SMALL_ICON, exportIcon);

        newAction = new AbstractAction(SearchLocale.getString("Action.New"),
                newIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        newQuery();
                    }
                };
        newAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.New"));

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK);
        newAction.putValue(Action.ACCELERATOR_KEY, ks);

        saveAction = new AbstractAction(SearchLocale.getString("Action.Save"),
                saveIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        save();
                    }
                };
        saveAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Save"));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        saveAction.putValue(Action.ACCELERATOR_KEY, ks);

        zoomAction = new AbstractAction(SearchLocale.getString("Action.Zoom"),
                zoomIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        queryPanel.newRestrictedQuery((ContentResult) queryManager.getCurrentQuery()
                                                                                  .getResult(),
                            SearchLocale.getString("Search.Constraint.Matches") +
                            " " + SearchLocale.getString("Search.FoundIn") +
                            " " +
                            SearchLocale.getString("Search.Result") +
                            " " + queryManager.getCurrentQueryNumber());
                        clearResult();
                    }
                };
        zoomAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Zoom"));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK);
        zoomAction.putValue(Action.ACCELERATOR_KEY, ks);

        readAction = new AbstractAction(SearchLocale.getString("Action.Open"),
                readIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        read();
                        updateActions();
                    }
                };
        readAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Open"));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        readAction.putValue(Action.ACCELERATOR_KEY, ks);

        closeAction = new AbstractAction(SearchLocale.getString("Action.Close")) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        userQuit();
                    }
                };
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
        closeAction.putValue(Action.ACCELERATOR_KEY, ks);

        backAction = new AbstractAction(SearchLocale.getString("Action.Back"),
                backIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        gotoQuery(queryManager.getCurrentQueryNumber() - 1);
                    }
                };
        backAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Back"));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        backAction.putValue(Action.ACCELERATOR_KEY, ks);

        forwardAction = new AbstractAction(SearchLocale.getString(
                    "Action.Forward"), forwardIcon) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        gotoQuery(queryManager.getCurrentQueryNumber() + 1);
                    }
                };
        forwardAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Forward"));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK);
        forwardAction.putValue(Action.ACCELERATOR_KEY, ks);

        stopAction.setEnabled(false);
    }

    private void newQuery() {
        queryPanel.reset();
        clearResult();
    }

    /**
     * Disconnect from the viewermanager and dispose the dialog.
     */
    protected void userQuit() {
        stopSearch();
        ((Window) getTopLevelAncestor()).dispose();
    }
}
