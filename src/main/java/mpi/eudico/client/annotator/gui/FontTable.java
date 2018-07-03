package mpi.eudico.client.annotator.gui;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


/**
 * A table showing all characters of a certain unicode block in a certain font.
 * 
 * $Id: FontTable.java 20115 2010-09-29 12:34:59Z wilelb $
 * @author $Author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
class FontTable extends JFrame {
    /** Holds value of property DOCUMENT ME! */
    int _start;

    /** Holds value of property DOCUMENT ME! */
    int _end;

    /** Holds value of property DOCUMENT ME! */
    String _codepgname;

    /** Holds value of property DOCUMENT ME! */
    Font _font;

    /** Holds value of property DOCUMENT ME! */
    DefaultTableModel _dataModel = null;

    /** Holds value of property DOCUMENT ME! */
    JTable table = null;

    /**
     * Creates a new FontTable instance
     *
     * @param start DOCUMENT ME!
     * @param end DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param font DOCUMENT ME!
     */
    public FontTable(int start, int end, String name, Font font) {
        super("Font Browser for Codepage:" + name);
        reload(start, end, name, font);
        setSize(500, 700);
    }

    /**
     * DOCUMENT ME!
     *
     * @param start DOCUMENT ME!
     * @param end DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param font DOCUMENT ME!
     */
    public void reload(int start, int end, String name, Font font) {
        setTitle("Font Browser for Codepage:" + name);
        _start = start;
        _end = end;
        _codepgname = name;
        _font = font;

        getContentPane().removeAll();

        Object[][] data = {
            { " ", " ", " " }
        };
        String[] columnNames = { "Font", "Unicode Hex", "Display Name" };
        _dataModel = null;
        _dataModel = new DefaultTableModel(data, columnNames);
        table = new JTable(_dataModel);

        JScrollPane scrollpane = new JScrollPane(table);
        TableColumn column = null;
        table.setRowHeight(20);
        loadTable();
        column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(100);
        column.sizeWidthToFit();
        column = table.getColumnModel().getColumn(1);
        column.setMaxWidth(130);
        column.sizeWidthToFit();
        column = table.getColumnModel().getColumn(2);
        //column.setMaxWidth(420);
        column.sizeWidthToFit();

        table.setFont(_font);
        getContentPane().add(scrollpane);
        getContentPane().validate();
        //setSize(500, 700);
    }

    private void loadTable() {
        BufferedReader cdTable = null;

        try {
            cdTable = new BufferedReader(new InputStreamReader(
                        FontGui.class.getResourceAsStream("/mpi/eudico/client/annotator/resources/UnicodeData.txt")));

            String s;

            while ((s = cdTable.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(s, ";");
                String tmpe = st.nextToken();
                int uni = Integer.parseInt(tmpe, 16);
                String desc = st.nextToken();

                if ((uni >= _start) && (uni <= _end)) {
                    Vector<String> v = new Vector<String>();

                	char []chars = Character.toChars(uni);
                	v.add(new String(chars));
                    v.add(Integer.toHexString(uni));
                    v.add(desc);
                    _dataModel.addRow(v);
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();

            return;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     */
    @Override
	public void setFont(Font f) {
        table.setFont(f);
    }

}
