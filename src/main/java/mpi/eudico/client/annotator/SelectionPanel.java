package mpi.eudico.client.annotator;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.eudico.util.TimeFormatter;


/**
 * A few labels to present a few things from a selection - begin time - end
 * time - duration time
 * 
 * @version May 2017 changed the font setting for the labels, these now use 
 * a smaller version of the system/jre's default font
 */
@SuppressWarnings("serial")
public class SelectionPanel extends JPanel implements ElanLocaleListener,
    SelectionListener {
    private JLabel selectionLabel;
    private JLabel beginLabel;
    private JLabel endLabel;
    private JLabel lengthLabel;
    private long begin;
    private long end;
    private ViewerManager2 vm;

    /**
     * Creates a new SelectionPanel instance
     *
     * @param theVM viewer manager
     */
    public SelectionPanel(ViewerManager2 theVM) {
        vm = theVM;
        init();
    }

    private void init() {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        // declare first to enable set length
        lengthLabel = new JLabel();
        lengthLabel.setFont(Constants.deriveSmallFont(lengthLabel.getFont()));

        selectionLabel = new JLabel();
        selectionLabel.setFont(lengthLabel.getFont());
        add(selectionLabel);

        beginLabel = new JLabel();
        beginLabel.setFont(lengthLabel.getFont());
        setBegin(0);
        add(beginLabel);

        JLabel separator = new JLabel(" - ");
        separator.setFont(lengthLabel.getFont());
        add(separator);

        endLabel = new JLabel();
        endLabel.setFont(lengthLabel.getFont());
        setEnd(0);
        add(endLabel);

        JLabel spaces = new JLabel("  ");
        spaces.setFont(lengthLabel.getFont());
        add(spaces);

        add(lengthLabel);

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        updateLocale();

        vm.getSelection().addSelectionListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param str DOCUMENT ME!
     */
    public void setNameLabel(String str) {
        selectionLabel.setText(str + ": ");
    }

    /**
     * DOCUMENT ME!
     *
     * @param begin DOCUMENT ME!
     */
    public void setBegin(long begin) {
        this.begin = begin;
        beginLabel.setText(TimeFormatter.toString(begin));
        setLength();
    }

    /**
     * DOCUMENT ME!
     *
     * @param end DOCUMENT ME!
     */
    public void setEnd(long end) {
        this.end = end;
        endLabel.setText(TimeFormatter.toString(end));
        setLength();
    }

    //      private void setLength(long length)
    private void setLength() {
        lengthLabel.setText("" + (end - begin));
    }

    /**
     * method from ElanLocaleListener
     */
    @Override
	public void updateLocale() {
        setNameLabel(ElanLocale.getString(
                "MediaPlayerControlPanel.Selectionpanel.Name"));
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateSelection() {
        long begin = vm.getSelection().getBeginTime();
        long end = vm.getSelection().getEndTime();

        // make sure it does not look ugly in the panel if selectionBegin == selectionEnd
        if (begin == end) {
            begin = 0;
            end = 0;
        }

        setBegin(begin);
        setEnd(end);
    }
}
 //end of SelectionPanel
