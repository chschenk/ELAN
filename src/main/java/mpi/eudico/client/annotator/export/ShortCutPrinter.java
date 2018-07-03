package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.util.TableSubHeaderObject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * Prints the contents of the shortcut table.
 */
public class ShortCutPrinter implements Printable, Pageable {   
    private int numPages = 1;
    private int firstColWidth = 0;
    private PageFormat pf;
    private float scale = 0.9f;
    private ArrayList<JTable> tableList;
    
    /**
     * Hash< pageIndex, Hash< tableIndex, Integer[rowBeginIndex, rowEndIndex]>>
     * 
     */
    HashMap<Integer, HashMap<Integer, Integer[]>> pageTableMap;

    /**
     * Constructor.
     *
     * @param shortCutTable the shortcut table
     */
    public ShortCutPrinter(JTable shortCutTable) {
    	tableList = new ArrayList<JTable>();
    	tableList.add(shortCutTable);
    }
    
    public ShortCutPrinter(ArrayList<JTable> shortCutTable) {
        tableList = shortCutTable;
    }

    /**
     * Initiates printing, shows page setup dialog and a print dialog.
     */
    public void startPrint() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        pf = printJob.pageDialog(printJob.defaultPage());
        calculateNumPages();
        printJob.setPrintable(this, pf);
        printJob.setPageable(this);
        

        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    ElanLocale.getString(
                        "InterlinearizerOptionsDlg.Error.Print") + " \n" + "(" +
                    ex.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * @see java.awt.print.Printable#print(java.awt.Graphics,
     *      java.awt.print.PageFormat, int)
     */
    @Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
        throws PrinterException {
        if (pageIndex >= getNumberOfPages()) {
            return Printable.NO_SUCH_PAGE;
        }       

        renderPage(graphics, pageIndex);

        return Printable.PAGE_EXISTS;
    }

    /**
     * @see java.awt.print.Pageable#getNumberOfPages()
     */
    @Override
	public int getNumberOfPages() {
        return numPages;
    }

    /**
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

        return pf;
    }

    /**
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

    /**
     * Calculates the number of pages, based on the row heights. Line wrapping
     * is not yet  taken into account. Will probably not be  necessary.
     */
    private void calculateNumPages() {
        if ((tableList == null) || (pf == null)) {
            return;
        }
        
        pageTableMap = new  HashMap<Integer, HashMap<Integer, Integer[]>>();
       
        
        int imgH = (int) pf.getImageableHeight();
        int curY = 0;
        int curp = 0;
        
        int startIndex;
        JTable table;
        HashMap<Integer, Integer[]> tableRowMap = new HashMap<Integer, Integer[]>();
        Integer[] extr;       
        
        for(int c =0; c< tableList.size(); c++){            	 
        	table = tableList.get(c);        		
        	curY += (int) (scale * table.getTableHeader().getHeight());  
        	if(table.getName() != null && table.getName().trim().length() > 0){
        		curY += (int) (scale * table.getRowHeight() * 3);   
        	}    	
        	
        	startIndex = 0;
        	extr = new Integer[2];
        	        	
        	if(c==0){
        		tableRowMap = new HashMap<Integer, Integer[]>();
        	}
        	for (int i = 0; i < table.getRowCount(); i++) {
        		curY += (int) (scale * table.getRowHeight(i));
	
	   	        if (curY > imgH) {
	   	        		numPages++;   	        		
		               
	   	        		
	   	        		if(i != 0){
	   	        			extr[0] = Integer.valueOf(startIndex);
			                
			                
	 		               // start with this row on a new page
	 		                startIndex = i--;
	 	
	 		                // if the last row is a sub header move it to the next page
	 		                if (table.getValueAt(i, 0) instanceof TableSubHeaderObject) {
	 		                	startIndex = i--;
	 		                }
	 		                
	 		                extr[1] = Integer.valueOf(i);
	 		               tableRowMap.put(c, extr);
	 		               extr = new Integer[2];
	   	        		}
		                
		                pageTableMap.put(curp, tableRowMap);
		                tableRowMap = new HashMap<Integer, Integer[]>();
		                
		                curp++;
		                curY = 0;
		                
		                if(i == 0){
		                	c--;
		                	break;
		                }		                
	            } else  if (i == (table.getRowCount() - 1)) {
	            	 extr[0] = Integer.valueOf(startIndex);
	                 extr[1] = Integer.valueOf(i);	                 
	                 tableRowMap.put(c, extr);	
	            }
        	}
        	
        	// table is half way done
        	if( c == tableList.size()-1){
            	pageTableMap.put(curp, tableRowMap);
        	}
        }       
    }	   

    /**
     * Calculates the width of the first column based on the length of the
     * strings in the shortcuts column
     *
     * @param graphics the printer graphics object
     *
     * @return the width for the first column
     */
    private int calcFirstColWidth(Graphics graphics, JTable table) {
    	if(table == null){
    		return 200;
    	}
        int cw = 0;
        int rowW = 0;
        Object rv;
        Graphics2D g2d = (Graphics2D) graphics;
        
        // first calc the header text width
        String val = (String) table.getTableHeader().getColumnModel()
                                   .getColumn(0).getHeaderValue();

        cw = (int) (scale * g2d.getFontMetrics(table.getTableHeader().getFont())
                               .stringWidth(val));

        for (int i = 0; i < table.getRowCount(); i++) {
            rv = table.getValueAt(i, 0);

            // skip the sub-headers
            if (rv instanceof String) {
                val = (String) rv;
                rowW = (int) (scale * g2d.getFontMetrics(table.getFont())
                                         .stringWidth(val));

                if (rowW > cw) {
                    cw = rowW;
                }
            }
        }
       
        // add a margin
        return cw + 40;
    }

    /**
     * Renders the page, identified by the firts and last row to print, to the
     * graphics context.
     *
     * @param graphics the printer graphics
     * @param rowExt the row extremes, first and last row for this page
     */
    private void renderPage(Graphics graphics, int pageIndex) {
    	if ((tableList == null) || (pf == null)) {
    		return;
    	}
    	
    	Graphics2D g2d = (Graphics2D) graphics;
    	g2d.setStroke(new BasicStroke(0.6f));
    	int ix = (int) pf.getImageableX();
    	int iy = (int) pf.getImageableY();
    	int iw = (int) pf.getImageableWidth();
       
    	int curY = iy;
    	Object rv;
    	String val;
    	TableSubHeaderObject tsho;
        
    	HashMap<Integer, Integer[]> tableRowMap = pageTableMap.get(pageIndex);
    	if(tableRowMap == null){
    		return;
    	} 
        
    	Iterator it = tableRowMap.entrySet().iterator();
    	JTable table;
    	while (it.hasNext()){
    		Map.Entry pair = (Entry) it.next();
    		int tableIndex = ((Integer)pair.getKey()).intValue();
    		Integer[] rowExt = (Integer[]) pair.getValue();
    		table = tableList.get(tableIndex);
    		firstColWidth = calcFirstColWidth(graphics, table);
    		
    		Font normF = table.getFont()
                    .deriveFont(Font.PLAIN,
                    		(int) (scale * table.getFont().getSize()));
    		Font boldF = table.getFont()
                    .deriveFont(Font.BOLD,
                    		(int) (scale * (table.getFont().getSize()+2)));
    		
    		Font italicF = table.getFont()
                    .deriveFont(Font.ITALIC,
                    		(int) (scale * (table.getFont().getSize() + 2)));
    		
    		if (rowExt[0] == 0) {
    			int tw = 0;
    			int tx = 0;
    			val = (String)table.getName();
    			if(val == null || val.trim().length() > 0){
    				curY = curY + (int) (scale * table.getTableHeader().getHeight());
                    g2d.drawString("", tx, curY - g2d.getFontMetrics().getDescent());
                    
    				tw = (int) (scale * g2d.getFontMetrics(italicF)
                            .stringWidth(val));
    				tx = ix + ((iw- tw) / 2);
                    curY = curY + (int) (scale * table.getTableHeader().getHeight());
                    g2d.setFont(italicF);
                    g2d.drawString(val, tx, curY - g2d.getFontMetrics().getDescent());
                    
                    curY = curY + (int) (scale * table.getTableHeader().getHeight());
                    g2d.drawString("", tx, curY - g2d.getFontMetrics().getDescent());
                    
    			}
    			
    			// we have to print the headers, centered in the columns
                val = (String) table.getTableHeader().getColumnModel().getColumn(0)
                                    .getHeaderValue();
                tw = (int) (scale * g2d.getFontMetrics(table.getTableHeader()
                                                            .getFont())
                                       .stringWidth(val));

                tx = ix + ((firstColWidth - tw) / 2);
                curY = curY + (int) (scale * table.getTableHeader().getHeight());
                g2d.setFont(normF);
                g2d.drawString(val, tx, curY - g2d.getFontMetrics().getDescent());
                val = (String) table.getTableHeader().getColumnModel().getColumn(1)
                                    .getHeaderValue();
                tw = (int) (scale * g2d.getFontMetrics(table.getTableHeader()
                                                            .getFont())
                                       .stringWidth(val));
                tx = ix + firstColWidth + ((iw - firstColWidth - tw) / 2);
                g2d.drawString(val, tx, curY - g2d.getFontMetrics().getDescent());
                g2d.drawLine(ix, curY, ix + iw, curY);
            } 
    		
    		boolean gray = true;
            Color bg = new Color(236, 236, 236);
            int th = 0;

            for (int i = rowExt[0]; (i <= rowExt[1]) && (i < table.getRowCount());
                    i++) {
                rv = table.getValueAt(i, 0);
                th = (int) (scale * table.getRowHeight(i));
                curY += th;

                if (rv instanceof String) {
                    if (gray) {
                        g2d.setColor(bg);
                        g2d.fillRect(ix, curY - th, iw, th);
                    }

                    gray = !gray;
                    g2d.setColor(Color.BLACK);
                    g2d.drawLine(ix, curY - th, ix + iw, curY - th);
                    g2d.setFont(normF);
                    g2d.drawString((String) rv, ix + 4,
                        curY - g2d.getFontMetrics().getDescent());
                    rv = table.getValueAt(i, 1);
                    g2d.drawString((String) rv, ix + firstColWidth,
                        curY - g2d.getFontMetrics().getDescent());
                } else if (rv instanceof TableSubHeaderObject) {
                    if (i != 0) {
                        g2d.drawLine(ix, curY - th, ix + iw, curY - th);
                    }

                    g2d.setFont(boldF);
                    tsho = (TableSubHeaderObject) rv;
                    g2d.drawString(tsho.toString(), ix + 4,
                        curY - g2d.getFontMetrics().getDescent());
                    gray = true;
                }
            }   
    	}        
    }
}

