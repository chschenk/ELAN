/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * ImageRenderer renders an interlinearized view on a BufferedImage using  the
 * image's Graphics object, or immediately on a Graphics object (e.g. when the
 * printer graphics.
 *
 * @author hennie
 */
public class ImageRenderer extends Renderer {
    /**
     * Renders an interlinearized view on a BufferedImage. Potentially shows
     * page boundaries, e.g. when generating a print preview.
     *
     * @param metrics The metrics object containing all dimensions, positions
     *        etc,  necessary for drawing
     * @param bi DOCUMENT ME!
     * @param offset DOCUMENT ME!
     */
    public static void render(Metrics metrics, BufferedImage bi, int[] offset) {
        Graphics2D g2d = (Graphics2D) bi.getGraphics();

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        int[] visibleRect = { bi.getWidth(), bi.getHeight() };

        render(metrics, bi.getGraphics(), offset, visibleRect);
    }

    /**
     * Renders a page of an interlinear view, specified by pageIndex.
     *
     * @param metrics
     * @param g
     * @param pageIndex
     *
     * @return Return true if page exists
     */
    public static boolean render(Metrics metrics, Graphics g, int pageIndex) {
        boolean pageExists = true;

        int[] pageBoundaries = metrics.getPageBoundaries(pageIndex,
                metrics.getInterlinearizer().getHeight());

        if (pageBoundaries[0] == pageBoundaries[1]) {
            pageExists = false;
        } else {
            int[] offset = { 0, pageBoundaries[0] };
            int[] visibleRect = {
                metrics.getInterlinearizer().getWidth(),
                pageBoundaries[1] - pageBoundaries[0]
            };

            render(metrics, g, offset, visibleRect);

            /*    Graphics2D g2d = (Graphics2D) g;
            
                   g2d.setColor(Color.WHITE);
                   g2d.fillRect(0,0,
                               metrics.getInterlinearizer().getWidth(),
                               metrics.getInterlinearizer().getHeight());
            
                   if (metrics.leftMarginShown()) {
                       g2d.setColor(Color.LIGHT_GRAY);
                       g2d.drawLine(metrics.getLeftMargin(), 0,
                                   metrics.getLeftMargin(),
                                   metrics.getInterlinearizer().getHeight());
            
                       drawTierLabels(metrics, g2d, pageBoundaries);
                   }
            
                           drawAnnotationValues(metrics, g2d, pageBoundaries);    */
        }

        return pageExists;
    }

    private static void render(Metrics metrics, Graphics g, int[] offset,
        int[] visibleRect) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, metrics.getInterlinearizer().getWidth(),
            metrics.getInterlinearizer().getHeight());

        // draw page boundaries when print preview
        if (metrics.getInterlinearizer().forPrinting() &&
                (metrics.getInterlinearizer().getPageHeight() > 0)) {
            int[] pageBoundaries = metrics.getPageBoundaries(metrics.getInterlinearizer()
                                                                    .getPageHeight());

            int width = metrics.getInterlinearizer().getWidth();

            g2d.setColor(Color.LIGHT_GRAY);

            for (int pageBoundary : pageBoundaries) {
                if ((pageBoundary > offset[1]) &&
                        (pageBoundary < (offset[1] + visibleRect[1]))) {
                    g2d.drawLine(0, pageBoundary - offset[1], width,
                        pageBoundary - offset[1]);
                }
            }
        }

        if (metrics.leftMarginShown()) {
            if (metrics.getLeftMargin() > offset[0]) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(metrics.getLeftMargin() - offset[0], 0,
                    metrics.getLeftMargin() - offset[0],
                    metrics.getInterlinearizer().getHeight());

                drawTierLabels(metrics, g2d, offset, visibleRect);
            }
        }

        drawAnnotationValues(metrics, g2d, offset, visibleRect);
    }

    /**
     * Draws labels of tiers in the left margin.
     *
     * @param metrics
     * @param g2d
     * @param offset
     * @param visibleRect DOCUMENT ME!
     */
    private static void drawTierLabels(Metrics metrics, Graphics2D g2d,
        int[] offset, int[] visibleRect) {
        g2d.setColor(Color.LIGHT_GRAY);

        if (metrics.getInterlinearizer().getEmptyLineStyle() == Interlinearizer.HIDE_EMPTY_LINES) {
            drawLabelsForVisibleAnnots(metrics, g2d, offset, visibleRect);
        } else { // TEMPLATE style, show all labels, including those for empty lines
            drawLabelsAsTemplates(metrics, g2d, offset, visibleRect);
        }
    }

    /**
     * Draws labels for all lines that have annotation content.
     *
     * @param metrics
     * @param g2d
     * @param offset
     * @param visibleRect DOCUMENT ME!
     */
    private static void drawLabelsForVisibleAnnots(Metrics metrics,
        Graphics2D g2d, int[] offset, int[] visibleRect) {
        Integer vPos = null;
        String tierLabel = "";
        int vShift = 0;
        int hShift = 0;

        List<Integer> vPositions = metrics.getPositionsOfNonEmptyTiers();

        Iterator posIter = vPositions.iterator();

        while (posIter.hasNext()) {
            vPos = (Integer) posIter.next();
            tierLabel = metrics.getTierLabelAt(vPos.intValue());

            if ((offset != null) && (visibleRect != null) &&
                    ((vPos.intValue() < offset[1]) ||
                    (vPos.intValue() > (offset[1] + visibleRect[1])))) {
                continue;
            }

            if (offset != null) {
                hShift = offset[0];
                vShift = offset[1];
            }

            Font font = metrics.getInterlinearizer().getFont(tierLabel);
            FontMetrics fontMetrics = g2d.getFontMetrics(font);

            g2d.setFont(font);

            // subtract descent to evenly use available vertical space for tier
            if (!tierLabel.startsWith("TC-")) {
                g2d.drawString(tierLabel, -hShift,
                    vPos.intValue() - fontMetrics.getDescent() - vShift);
            } else {
                g2d.drawString("TC", -hShift,
                    vPos.intValue() - fontMetrics.getDescent() - vShift);
            }
        }
    }

    /**
     * Draws tier labels for all tier lines, including lines without annotation
     * content.
     *
     * @param metrics
     * @param g2d
     * @param offset
     * @param visibleRect DOCUMENT ME!
     */
    private static void drawLabelsAsTemplates(Metrics metrics, Graphics2D g2d,
        int[] offset, int[] visibleRect) {
        int currentBlockPosition = 0;
        int vPos = 0;
        int hShift = 0;
        int vShift = 0;

        String templateLabel = "";

        Interlinearizer interlinearizer = metrics.getInterlinearizer();

        int maxPosition = metrics.getMaxVerticalPosition();
        int[] templatePositions = metrics.getVPositionsInTemplate();
        String[] templateLabels = interlinearizer.getVisibleTiers();

        int blockIncrement = 0;

        if (templatePositions.length > 0) {
            blockIncrement = templatePositions[templatePositions.length - 1];
        }

        blockIncrement += (interlinearizer.getBlockSpacing() +
        interlinearizer.getLineSpacing());

        while (currentBlockPosition < maxPosition) {
            // draw template
            for (int i = 0; i < templatePositions.length; i++) {
                vPos = currentBlockPosition + templatePositions[i];
                templateLabel = templateLabels[i];

                if ((offset != null) && (visibleRect != null) &&
                        ((vPos < offset[1]) ||
                        (vPos > (offset[1] + visibleRect[1])))) {
                    // next i	 
                } else {
                    if (offset != null) {
                        hShift = offset[0];
                        vShift = offset[1];
                    }

                    Font font = metrics.getInterlinearizer().getFont(templateLabel);
                    FontMetrics fontMetrics = g2d.getFontMetrics(font);

                    g2d.setFont(font);

                    // subtract descent to evenly use available vertical space for tier
                    if (!templateLabel.startsWith("TC-")) {
                        g2d.drawString(templateLabel, -hShift,
                            vPos - fontMetrics.getDescent() - vShift);
                    } else {
                        g2d.drawString("TC", -hShift,
                            vPos - fontMetrics.getDescent() - vShift);
                    }
                }
            }

            // go further...
            currentBlockPosition += blockIncrement;
        }
    }

    /**
     * Draws all visible annotations with the proper font size and color at the
     * proper position.
     *
     * @param metrics
     * @param g2d
     * @param offset
     * @param visibleRect DOCUMENT ME!
     */
    private static void drawAnnotationValues(Metrics metrics, Graphics2D g2d,
        int[] offset, int[] visibleRect) {
        int hShift = 0;
        int vShift = 0;

        g2d.setColor(Color.BLACK);

        List<Annotation> annots = metrics.getBlockWiseOrdered();

        for (Annotation a : annots) {
            int vPos = metrics.getVerticalPosition(a);

            if ((offset != null) && (visibleRect != null) &&
                    ((vPos < offset[1]) ||
                    (vPos > (offset[1] + visibleRect[1])))) {
                continue;
            }

            if (offset != null) {
                hShift = offset[0];
                vShift = offset[1];
            }

            String tierName = a.getTier().getName();

            if (tierName.startsWith("TC-")) { // time code tier
                g2d.setColor(Color.RED);
            }

            Font font = metrics.getInterlinearizer().getFont(tierName);
            FontMetrics fontMetrics = g2d.getFontMetrics(font);

            g2d.setFont(font);

            g2d.drawString(a.getValue(),
                (metrics.getLeftMargin() + metrics.getHorizontalPosition(a)) -
                hShift,
                metrics.getVerticalPosition(a) - fontMetrics.getDescent() -
                vShift);

            if (tierName.startsWith("TC-")) { // time code tier, reset
                g2d.setColor(Color.BLACK);
            }
        }
    }
}
