package mpi.eudico.client.annotator.interlinear;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;


/**
 * This class renders pixel based interlinearized content.
 *
 * @author Han Sloetjes
 */
public class PixelRenderer {
    /**
     * Renders the contents of the Interlinear object to the specified
     * BufferedImage  using a horizontal and vertical offset.
     *
     * @param interlinear the <code>Interlinear</code> object
     * @param bi the BufferedImage, the destination for rendering
     * @param offset horizontal and vertical offset (shift)
     */
    public static void render(Interlinear interlinear, BufferedImage bi,
        int[] offset) {
        Graphics2D g2d = (Graphics2D) bi.getGraphics();

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        int[] visibleRect = { bi.getWidth(), bi.getHeight() };

        render(interlinear, g2d, offset, visibleRect);
    }

    /**
     * Renders the contents of the specified page of the Interlinear object to
     * the specified  Graphics object.
     *
     * @param interlinear the <code>Interlinear</code> object
     * @param g the Graphics object, the destination for rendering
     * @param pageIndex the index of the page to render
     */
    public static void render(Interlinear interlinear, Graphics g, int pageIndex) {
        if ((pageIndex < 0) ||
                (pageIndex >= interlinear.getMetrics().getPageBreaks().size())) {
            return; // throw exception or return false
        }

        Graphics2D g2d = (Graphics2D) g;
        int[] pageBreak = interlinear.getMetrics().getPageBreaks()
                                             .get(pageIndex);
        g2d.setColor(Color.LIGHT_GRAY);

        // margin
        if (interlinear.isTierLabelsShown()) {
            int h = interlinear.getPageHeight();

            if (interlinear.isShowPageNumber()) {
                h -= interlinear.pageNumberAreaHeight;
            }

            g2d.drawLine(interlinear.getMetrics().getLeftMargin() - 2, 0,
                interlinear.getMetrics().getLeftMargin() - 2, h);
        }

        int[] offset = { 0, 0 };
        int[] visRect = { interlinear.getWidth(), interlinear.getHeight() };
        renderPage(g2d, interlinear, pageBreak, offset, visRect, 0);

        if (interlinear.isShowPageNumber()) {
            g2d.setFont(Interlinear.DEFAULTFONT.deriveFont((float) 10));

            int w = g2d.getFontMetrics().stringWidth("" + (pageIndex + 1));
            g2d.drawString("" + (pageIndex + 1),
                (interlinear.getWidth() / 2) - (w / 2),
                interlinear.getPageHeight() - 2);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(0,
                interlinear.getPageHeight() - interlinear.pageNumberAreaHeight +
                2, interlinear.getWidth(),
                interlinear.getPageHeight() - interlinear.pageNumberAreaHeight +
                2);
        }
    }

    /**
     * This pixel based renderer can render a preview of a character based
     * linearization. Positions of tiers and annotations are converted to
     * pixel values and they are rendered using a monospaced font.
     *
     * @param interlinear the <code>Interlinear</code> object containing the
     *        data
     * @param bi the BufferedImage to render to
     * @param offset the horizontal and vertical (scroll) offset
     */
    public static void renderCharacterPreview(Interlinear interlinear,
        BufferedImage bi, int[] offset) {
        Graphics2D g2d = (Graphics2D) bi.getGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g2d.setColor(Color.BLACK);
        g2d.setFont(Interlinear.MONOSPACED_FONT);
        g2d.translate(-offset[0], 0.0);

        int[] visibleRect = { bi.getWidth(), bi.getHeight() };
        renderCharacterPreview(interlinear, g2d, offset, visibleRect);
    }

    /**
     * Renders the contents of the Interlinear object to the Graphics context
     * using the specified horizontal and vertical offset, limiting the
     * rendering  to the specified visible rectangle.
     *
     * @param interlinear the <code>Interlinear</code> object
     * @param g2d the Graphics object, the destination for rendering
     * @param offset horizontal and vertical offset (shift)
     * @param visibleRect the visible rectangle
     */
    private static void render(Interlinear interlinear, Graphics2D g2d,
        int[] offset, int[] visibleRect) {
        // ignore the horizontal shift
        int vertOffset = offset[1];
        int firstVisPage = vertOffset / interlinear.getPageHeight(); // 0 based

        g2d.setColor(Color.WHITE);
        g2d.translate(-offset[0], 0);
        g2d.fillRect(0, 0, interlinear.getWidth(), visibleRect[1]);

        g2d.setColor(Color.LIGHT_GRAY);

        // margin
        if (interlinear.isTierLabelsShown()) {
            g2d.drawLine(interlinear.getMetrics().getLeftMargin() - 2, 0,
                interlinear.getMetrics().getLeftMargin() - 2, visibleRect[1]);
        }

        // page boundary markers		
        g2d.translate(0.0, -vertOffset);

        List<int[]> breaks = interlinear.getMetrics().getPageBreaks();
        g2d.setColor(Color.GRAY);

        for (int i = firstVisPage; i < breaks.size(); i++) {
            g2d.drawLine(0, i * interlinear.getPageHeight(), visibleRect[0],
                i * interlinear.getPageHeight());

            //System.out.println("Draw page marker at: " + (i * interlinear.getPageHeight()));
            if ((i * interlinear.getPageHeight()) > (offset[1] +
                    visibleRect[1])) {
                break;
            }
        }

        // annotations
        List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
        InterlinearBlock printBlock = null;
        InterlinearTier pt = null;
        int y = 0;
pageloop: 
        for (int i = firstVisPage; i < breaks.size(); i++) {
            int[] br = breaks.get(i);

            y = i * interlinear.getPageHeight();

            if (y < (offset[1] + visibleRect[1])) {
                //System.out.println("Page: " + i);
                renderPage(g2d, interlinear, br, offset, visibleRect, y);

                if (interlinear.isShowPageNumber()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(Interlinear.DEFAULTFONT.deriveFont((float) 10));

                    int w = g2d.getFontMetrics()
                               .stringWidth("" + (firstVisPage + 1));
                    g2d.drawString("" + (firstVisPage + 1),
                        (interlinear.getWidth() / 2) - (w / 2),
                        (i * interlinear.getPageHeight()) - 2);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(0,
                        (i * interlinear.getPageHeight()) -
                        interlinear.pageNumberAreaHeight + 2, visibleRect[0],
                        (i * interlinear.getPageHeight()) -
                        interlinear.pageNumberAreaHeight + 2);
                }

                continue pageloop;
            } else {
                if ((1 + 1) == 2) {
                    return;
                }
            }

            // this is now obsolete, but not yet deleted
            for (int j = br[0]; j <= br[2]; j++) {
                printBlock = blocks.get(j);

                List<InterlinearTier> tiers = printBlock.getPrintTiers();

                // line index var
                int k = 0;
                int maxK = printBlock.getNumberOfLines();

                if (j == br[0]) {
                    k = br[1];
                }

                if (j == br[2]) {
                    maxK = br[3];
                }

                for (int count = k; count < tiers.size(); count++) {
                    pt = tiers.get(count);

                    for (int z = 0; z < pt.getNumLines(); z++) {
                        if (count > maxK) {
                            continue pageloop;

                            //break tierloop;
                        }

                        if (y >= vertOffset) {
                            // paint the annotations
                            //System.out.println("Print: Page: " + i + " Block: " + j + " Tier: " + pt.getTierName() + " line: " + z);
                            renderTier(g2d, interlinear, pt, 0, y, z);
                        }

                        y += pt.getPrintHeight();

                        if (y > (offset[1] + visibleRect[1])) {
                            return;
                        }

                        if (z != (pt.getNumLines() - 1)) {
                            y += interlinear.getLineSpacing();
                        }
                    }

                    if (count != (tiers.size() - 1)) {
                        y += interlinear.getLineSpacing();
                    }
                }

                y += interlinear.getBlockSpacing();
            }
        }
    }

    /**
     * Renders a page, as far it overlaps in the visible area.
     *
     * @param g2d the Graphics object, the destination for rendering
     * @param interlinear the <code>Interlinear</code> object
     * @param br the pagebreaks
     * @param offset horizontal and vertical offset (shift)
     * @param visibleRect the visible rectangle
     * @param startYShift initial y position of page
     */
    private static void renderPage(Graphics2D g2d, Interlinear interlinear,
        int[] br, int[] offset, int[] visibleRect, int startYShift) {
        // annotations
        List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
        InterlinearBlock printBlock = null;
        InterlinearTier pt = null;
        int y = startYShift;

        for (int j = br[0]; j <= br[2]; j++) {
            printBlock = blocks.get(j);

            List<InterlinearTier> tiers = printBlock.getPrintTiers();

            // total line counter in this block
            int lineNum = 0;

            // line index var, min and max
            int k = 0;
            int maxK = printBlock.getNumberOfLines();

            if (j == br[0]) {
                k = br[1];
            }

            if (j == br[2]) {
                maxK = br[3];
            }

//System.out.println("K: " + k + " max-K: " + maxK);
//tierloop: 
            for (int tierCount = 0; tierCount < tiers.size(); tierCount++) {
                pt = tiers.get(tierCount);

                for (int tierLine = 0; tierLine < pt.getNumLines();
                        tierLine++) {
                    if (lineNum < k) {
                        lineNum++;

                        continue;
                    }

                    if (lineNum > maxK) {
                        return;
                    }

                    if (y >= offset[1]) {
                        // paint the annotations
                        //System.out.println("Print: Block: " + j + " Tier: " + pt.getTierName() + " line: " + tierLine);
                        renderTier(g2d, interlinear, pt, 0, y, tierLine);
                    }

                    y += pt.getPrintHeight();

                    if (y > (offset[1] + visibleRect[1])) {
                        return;
                    }

                    if (tierLine != (pt.getNumLines() - 1)) {
                        y += interlinear.getLineSpacing();
                    }

                    lineNum++;
                }

                if (tierCount != (tiers.size() - 1)) {
                    y += interlinear.getLineSpacing();
                }
            }

            y += interlinear.getBlockSpacing();
        }
    }

    /**
     * Render the contents of a InterlinearTier, called per line.
     *
     * @param g2d the Graphics object, the destination for rendering
     * @param interlinear the <code>Interlinear</code> object
     * @param pt the print tier holding the annotations
     * @param xShift horizontal shift
     * @param yShift the y-coord of the top of the tier
     * @param line the line (index) to render
     */
    private static void renderTier(Graphics2D g2d, Interlinear interlinear,
        InterlinearTier pt, int xShift, int yShift, int line) {
        yShift += pt.getPrintHeight();
        g2d.setFont(interlinear.getFont(pt.getTierName()));

        if (interlinear.isTierLabelsShown() && (line == 0)) {
            if (!g2d.getFont().getName()
                        .equals(Interlinear.DEFAULTFONT.getName())) {
                g2d.setFont(Interlinear.DEFAULTFONT.deriveFont(
                        (float) interlinear.getFontSize(pt.getTierName())));
            }

            g2d.setColor(Color.LIGHT_GRAY);

            if (pt.isTimeCode()) {
                g2d.drawString(interlinear.getMetrics().TC_TIER_NAME, xShift,
                    yShift - g2d.getFontMetrics().getDescent());
            } else if (pt.isSilDuration()) {
                g2d.drawString(interlinear.getMetrics().SD_TIER_NAME, xShift,
                        yShift - g2d.getFontMetrics().getDescent());
             }else {
                g2d.drawString(pt.getTierName(), xShift,
                    yShift - g2d.getFontMetrics().getDescent());
            }

            // reset font
            g2d.setFont(interlinear.getFont(pt.getTierName()));
        }

        if (pt.isTimeCode()) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }

        xShift += interlinear.getMetrics().getLeftMargin();

        List<InterlinearAnnotation> annos = pt.getAnnotations();
        InterlinearAnnotation pa = null;

        for (int i = 0; i < annos.size(); i++) {
            pa = annos.get(i);

            if (pa.nrOfLines == 1) {
                g2d.drawString(pa.getValue(), xShift + pa.x,
                    yShift - g2d.getFontMetrics().getDescent());

                // test drawing of outlines
                // g2d.drawRect(xShift + pa.x, yShift - pt.getPrintHeight(), pa.calcWidth, pt.getPrintHeight());
                if (interlinear.isEmptySlotsShown() &&
                        pa.getValue().equals("")) {
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(xShift + pa.x,
                        yShift - pt.getPrintHeight() + 1, pa.calcWidth,
                        pt.getPrintHeight() - 2);
                    g2d.setColor(Color.BLACK);
                }
            } else {
                int y = yShift - g2d.getFontMetrics().getDescent();

                if (line < pa.getLines().length) {
                    g2d.drawString(pa.getLines()[line], xShift + pa.x, y);
                }
            }
        }
    }

    /**
     * Renders a character based interlinearization to the given graphics
     * object by  converting char position to pixel positions.
     *
     * @param interlinear the <code>Interlinear</code> object containing the
     *        data
     * @param g2d the graphics context to render to
     * @param offset the horizontal and vertical (scroll) offset
     * @param visibleRect the visible area, used to prevent unnecessary paint
     *        operations
     */
    private static void renderCharacterPreview(Interlinear interlinear,
        Graphics2D g2d, int[] offset, int[] visibleRect) {
        int charWidth = g2d.getFontMetrics().charWidth('w');
        int vertOffset = offset[1];
        g2d.translate(0.0, -vertOffset);

        // annotations
        List<InterlinearBlock> blocks = interlinear.getMetrics().getPrintBlocks();
        InterlinearBlock printBlock = null;
        List<InterlinearTier> tiers = null;
        InterlinearTier pt = null;
        int y = 0;

        for (int i = 0; i < blocks.size(); i++) {
            printBlock = blocks.get(i);
            tiers = printBlock.getPrintTiers();

            if ((y +
                    (printBlock.getNumberOfLines() * Interlinear.DEFAULT_FONT_SIZE)) > vertOffset) {
                // render this block
                for (int j = 0; j < tiers.size(); j++) {
                    pt = tiers.get(j);
                    renderCharTier(g2d, interlinear, pt, charWidth, y);
                    y += (pt.getNumLines() * Interlinear.DEFAULT_FONT_SIZE);
                }
            } else {
                y += (printBlock.getNumberOfLines() * Interlinear.DEFAULT_FONT_SIZE);
            }

            y += (interlinear.getBlockSpacing() * Interlinear.DEFAULT_FONT_SIZE);

            if (y > (vertOffset + visibleRect[1])) {
                return;
            }
        }
    }

    /**
     * Renders a complete InterlinearTier, including tierlabel and multi line
     * annotations, using a monospaced font.
     *
     * @param g2d the graphics context
     * @param interlinear the <code>Interlinear</code> object containing the
     *        data
     * @param pt the <code>InterlinearTier</code> holding the label and
     *        formatted annotations
     * @param charWidth the width in pixels per character (it is a monospaced
     *        font
     * @param yShift the y position of the print tier, the y coordinate of the
     *        bounding  box of the text
     */
    private static void renderCharTier(Graphics2D g2d, Interlinear interlinear,
        InterlinearTier pt, int charWidth, int yShift) {
        yShift += Interlinear.DEFAULT_FONT_SIZE;

        int xShift = 0;

        if (interlinear.isTierLabelsShown()) {
            if (pt.isTimeCode()) {
                g2d.drawString(interlinear.getMetrics().TC_TIER_NAME, xShift,
                    yShift - g2d.getFontMetrics().getDescent());
            } else if (pt.isSilDuration()) {
                g2d.drawString(interlinear.getMetrics().SD_TIER_NAME, xShift,
                        yShift - g2d.getFontMetrics().getDescent());
             } else {
                g2d.drawString(pt.getTierName(), xShift,
                    yShift - g2d.getFontMetrics().getDescent());
            }

            xShift = interlinear.getMetrics().getLeftMargin() * charWidth;
        }
        
       
            

        List<InterlinearAnnotation> annos = pt.getAnnotations();
        InterlinearAnnotation pa = null;

        for (int i = 0; i < annos.size(); i++) {
            pa = annos.get(i);

            if (pa.nrOfLines == 1) {
                g2d.drawString(pa.getValue(), xShift + (pa.x * charWidth),
                    yShift - g2d.getFontMetrics().getDescent());

                // test drawing of outlines
                // g2d.drawRect(xShift + (pa.x * charWidth), yShift - pt.getPrintHeight(), 
                //        (pa.calcWidth * charWidth), pt.getPrintHeight());
            } else {
                int y = yShift - g2d.getFontMetrics().getDescent();

                for (int line = 0; line < pa.getLines().length; line++) {
                    g2d.drawString(pa.getLines()[line],
                        xShift + (pa.x * charWidth), y);
                    y += Interlinear.DEFAULT_FONT_SIZE;
                }
            }
        }
    }
}
