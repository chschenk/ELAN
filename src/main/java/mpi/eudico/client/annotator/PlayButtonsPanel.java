package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * DOCUMENT ME!
 * $Id: PlayButtonsPanel.java 45169 2016-04-04 08:51:03Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class PlayButtonsPanel extends JComponent {
    private JButton butPlay;
    private JButton butPrevious;
    private JButton butNext;
    private JButton butGoToBegin;
    private JButton butGoToEnd;
    private JButton but1SecLeft;
    private JButton but1SecRight;
    private JButton but1PixelLeft;
    private JButton but1PixelRight;
    private JButton butGoToPreviousScrollview;
    private JButton butGoToNextScrollview;

    /**
     * Creates a new PlayButtonsPanel instance
     *
     * @param buttonSize DOCUMENT ME!
     * @param theVM DOCUMENT ME!
     */
    public PlayButtonsPanel(Dimension buttonSize, ViewerManager2 theVM) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        butGoToBegin = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.GO_TO_BEGIN));
        butGoToBegin.setPreferredSize(buttonSize);
        add(butGoToBegin);

        butGoToPreviousScrollview = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(),
                    ELANCommandFactory.PREVIOUS_SCROLLVIEW));
        butGoToPreviousScrollview.setPreferredSize(buttonSize);
        add(butGoToPreviousScrollview);

        but1SecLeft = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.SECOND_LEFT));
        but1SecLeft.setPreferredSize(buttonSize);
        add(but1SecLeft);

        butPrevious = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PREVIOUS_FRAME));
        butPrevious.setPreferredSize(buttonSize);
        add(butPrevious);

        but1PixelLeft = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PIXEL_LEFT));
        but1PixelLeft.setPreferredSize(buttonSize);
        add(but1PixelLeft);

        butPlay = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PLAY_PAUSE));
        butPlay.setPreferredSize(buttonSize);
        add(butPlay);

        but1PixelRight = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PIXEL_RIGHT));
        but1PixelRight.setPreferredSize(buttonSize);
        add(but1PixelRight);

        butNext = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.NEXT_FRAME));
        butNext.setPreferredSize(buttonSize);
        add(butNext);

        but1SecRight = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.SECOND_RIGHT));
        but1SecRight.setPreferredSize(buttonSize);
        add(but1SecRight);

        butGoToNextScrollview = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.NEXT_SCROLLVIEW));
        butGoToNextScrollview.setPreferredSize(buttonSize);
        add(butGoToNextScrollview);

        butGoToEnd = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.GO_TO_END));
        butGoToEnd.setPreferredSize(buttonSize);
        add(butGoToEnd);
    }


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		//butPlay.setEnabled(enabled);
		butPlay.getAction().setEnabled(enabled);
		butPrevious.getAction().setEnabled(enabled);
		butNext.getAction().setEnabled(enabled);
		butGoToBegin.getAction().setEnabled(enabled);
		butGoToEnd.getAction().setEnabled(enabled);
		but1SecLeft.getAction().setEnabled(enabled);
		but1SecRight.getAction().setEnabled(enabled);
		but1PixelLeft.getAction().setEnabled(enabled);
		but1PixelRight.getAction().setEnabled(enabled);
		butGoToPreviousScrollview.getAction().setEnabled(enabled);
		butGoToNextScrollview.getAction().setEnabled(enabled);
	}


	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		
		Dimension prefSize = butPlay.getPreferredSize();
		return new Dimension(11 * prefSize.width + 6, prefSize.height + 6);
	}


	@Override
	public Dimension getMinimumSize() {
		if (isMinimumSizeSet()){
			return super.getMinimumSize();
		}
		
		Dimension prefSize = butPlay.getPreferredSize();
		return new Dimension(11 * prefSize.width, prefSize.height);
	}
    
    
}
