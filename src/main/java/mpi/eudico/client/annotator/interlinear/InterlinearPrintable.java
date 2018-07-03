package mpi.eudico.client.annotator.interlinear;

import mpi.eudico.client.annotator.commands.PrintCommand;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;


/**
 * The Pritable and Pageable class for Interlinear objects.
 *
 * @author HS
 * @version 1.0
 */
public class InterlinearPrintable implements Printable, Pageable {
    private Interlinear interlinear;

    /**
     * Creates a new InterlinearPrintable instance
     *
     * @param interlinear the Interlinear object containing the interlinearized
     *        content
     */
    public InterlinearPrintable(Interlinear interlinear) {
        this.interlinear = interlinear;
    }

    /**
     * Checks whether the page format is the same as the pageformat of the
     * Interlinear object. If not the interlinearisation has to be
     * recalculated. The rendering is delegated to the Interlinear object.
     *
     * @param graphics the printer graphics
     * @param pageFormat the format of the printer page
     * @param pageIndex the page to print
     *
     * @return PAGE_EXISTS or NO_SUCH_PAGE
     *
     * @throws PrinterException any exception while printing
     *
     * @see java.awt.print.Printable#print(java.awt.Graphics,
     *      java.awt.print.PageFormat, int)
     */
    @Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
        throws PrinterException {
        int pw = (int) pageFormat.getImageableWidth();
        int ph = (int) pageFormat.getImageableHeight();

        if ((interlinear.getWidth() != pw) ||
                (interlinear.getPageHeight() != ph)) {
            interlinear.setWidth(pw);
            interlinear.setPageHeight(ph);
            interlinear.calculateMetrics(graphics);
        }

        if (pageIndex > (getNumberOfPages() - 1)) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        interlinear.renderPage(g2d, pageIndex);

        return PAGE_EXISTS;
    }

    /**
     * Returns the calculated number of pages.
     *
     * @return the number of pages in the document
     *
     * @see java.awt.print.Pageable#getNumberOfPages()
     */
    @Override
	public int getNumberOfPages() {
        return interlinear.getMetrics().getPageBreaks().size();
    }

    /**
     * This method is called before <code>print</code>.
     *
     * @param pageIndex the index of the page to print
     *
     * @return the PageFormat for the specified page index
     *
     * @throws IndexOutOfBoundsException if pageIndex less than 0 or > number
     *         of pages - 1
     *
     * @see java.awt.print.Pageable#getPageFormat(int)
     */
    @Override
	public PageFormat getPageFormat(int pageIndex)
        throws IndexOutOfBoundsException {
        if (pageIndex < 0) {
            throw new IndexOutOfBoundsException("Page Index: " + pageIndex +
                " < 0");
        }

        if (pageIndex >= getNumberOfPages()) {
            throw new IndexOutOfBoundsException("Page Index: " + pageIndex +
                " > " + (getNumberOfPages() - 1));
        }

        return PrintCommand.pageFormat;
    }

    /**
     * Returns the Printable object (== this)
     *
     * @param pageIndex the page to get the printable object for, zero based
     *
     * @return the printable object for the specified page index, is
     *         <code>this</code>
     *
     * @throws IndexOutOfBoundsException if pageIndex less than 0 or > number
     *         of pages - 1
     *
     * @see java.awt.print.Pageable#getPrintable(int)
     */
    @Override
	public Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException {
        if (pageIndex < 0) {
            throw new IndexOutOfBoundsException("Page Index: " + pageIndex +
                " < 0");
        }

        if (pageIndex >= getNumberOfPages()) {
            throw new IndexOutOfBoundsException("Page Index: " + pageIndex +
                " > " + (getNumberOfPages() - 1));
        }

        return this;
    }
}
