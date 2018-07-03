package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


/**
 * A panel with a tab pane, containing a tab for general information 
 * and a tab for acknowledgments (developers and translators).
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
    private JTabbedPane tabPane;
    private JPanel aboutTabPanel;
    private JPanel acknowledgeTabPanel;
    private JPanel citingElanTabPanel;

    /**
     * Creates a new AboutPanel instance
     */
    public AboutPanel() {
        super();
        initPanel();
    }

    private void initPanel() {
        setLayout(new GridBagLayout());
        tabPane = new JTabbedPane();
        aboutTabPanel = new JPanel(new GridBagLayout());

        Icon icon = null;
        Icon mpgLogo = null;
        Icon tlaLogo = null;

        try {
            icon = new ImageIcon(this.getClass()
                                     .getResource("/mpi/eudico/client/annotator/resources/ELAN256.png"));
//            mpgLogo = new ImageIcon(this.getClass()
//                    .getResource("/mpi/eudico/client/annotator/resources/MPG_logo.png"));
            mpgLogo = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/MPI_logo.gif"));
            tlaLogo = new ImageIcon(this.getClass()
                    .getResource("/mpi/eudico/client/annotator/resources/TLA_logo.jpg"));
        } catch (Exception ex) {
        }

        StringBuilder textBuf = new StringBuilder("<html>");
        textBuf.append("<b>");
        textBuf.append("E L A N - ELAN Linguistic Annotator");
        textBuf.append("<br>");
        textBuf.append("Version: ");
        textBuf.append(ELAN.getVersionString());
        textBuf.append("<br><br>");
        textBuf.append("Copyright \u00A9 2001 - 2017");
        textBuf.append("<br>");
        textBuf.append("Max-Planck-Institute for Psycholinguistics");
        textBuf.append("<br>");
        textBuf.append("Nijmegen, The Netherlands");
        //textBuf.append("</b><br><br><br>");
        //textBuf.append(ElanLocale.getString("Menu.Help.AboutText.GPL"));
        textBuf.append("<br>");
        textBuf.append("</html>");

        JLabel label = new JLabel(textBuf.toString()
                                         .replaceAll("\\u000A", "<br>"));
        //label.setFont(label.getFont().deriveFont(Font.PLAIN));

        Insets insets = new Insets(2, 6, 2, 6);
        JPanel iconPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        if(mpgLogo != null){
        	//mpgLogo = new ImageIcon(((ImageIcon)mpgLogo).getImage().getScaledInstance(100,100, Image.SCALE_AREA_AVERAGING));
        	JLabel iconLabel = new JLabel(mpgLogo);
        	iconLabel.setToolTipText("Max-Planck-Gesellschaft");
        	iconPanel.add(iconLabel, gbc);
        }
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        if(tlaLogo != null){
        	tlaLogo = new ImageIcon(((ImageIcon)tlaLogo).getImage().getScaledInstance(90,87, Image.SCALE_AREA_AVERAGING));
        	JLabel iconLabel = new JLabel(tlaLogo);
        	iconLabel.setToolTipText("The Language Archive");
        	iconPanel.add(iconLabel, gbc);        	
        }
        
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        if (icon != null) {
            aboutTabPanel.add(new JLabel(icon), gbc);
        }
        
        gbc.gridx = 1;
        gbc.gridheight = 1;
        //gbc.anchor = GridBagConstraints.CENTER;
        aboutTabPanel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 6, 6, 6);
        aboutTabPanel.add(iconPanel, gbc);
        
        textBuf = new StringBuilder("<html>");
        textBuf.append(ElanLocale.getString("Menu.Help.AboutText.GPL"));
        textBuf.append("</html>");
        JLabel gplLabel = new JLabel(textBuf.toString().replaceAll("\\u000A", "<br>"));
        gplLabel.setFont(gplLabel.getFont().deriveFont(Font.PLAIN, 12f));

        tabPane.addTab(String.format(ElanLocale.getString("Menu.Help.About"), ELAN.getApplicationName()), aboutTabPanel);
        gbc.gridy = 2;
        aboutTabPanel.add(gplLabel, gbc);

        gbc.gridx = 0;
        add(tabPane, gbc);
        
        citingElanTabPanel = getCiteElanPanel();
        JScrollPane scrollPane = new JScrollPane(citingElanTabPanel);
        scrollPane.setPreferredSize(new Dimension(200, 80));
        tabPane.add(ElanLocale.getString("AboutDialog.CitingElan"), scrollPane);
        
        
        acknowledgeTabPanel = new JPanel(new GridBagLayout());

        JTabbedPane acknowTabPane = new JTabbedPane();
        JScrollPane devScrollPane = new JScrollPane(getDeveloperTable());
        devScrollPane.setPreferredSize(new Dimension(200, 80));
        acknowTabPane.addTab(ElanLocale.getString("AboutDialog.Source"),
            devScrollPane);

        JScrollPane transScrollPane = new JScrollPane(getTranslatorsTable());
        transScrollPane.setPreferredSize(new Dimension(200, 80));
        acknowTabPane.addTab(ElanLocale.getString("AboutDialog.Translations"),
            transScrollPane);
        
        JScrollPane softScrollPane = new JScrollPane(getSoftwarePanel());
        softScrollPane.setPreferredSize(new Dimension(200, 80));
        acknowTabPane.addTab(ElanLocale.getString("AboutDialog.Software"),
        		softScrollPane);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        acknowledgeTabPanel.add(acknowTabPane, gbc);
        tabPane.addTab(ElanLocale.getString("AboutDialog.Acknowledgments"),
            acknowledgeTabPanel);
    }

    private JTable getDeveloperTable() {
        DefaultTableModel model = new DefaultTableModel(0, 0);
        model.addColumn(ElanLocale.getString("AboutDialog.Name"));
        model.addColumn(ElanLocale.getString("AboutDialog.Affiliation"));
        model.addRow(new String[] { "Eric Auer", "MPI" });
        model.addRow(new String[] { "Hennie Brugman", "MPI" });
        model.addRow(new String[] { "Greg Gulrajani", "MPI" });
        model.addRow(new String[] { "Alex Klassmann", "MPI" });
        model.addRow(new String[] { "Alex K\u00f6nig", "MPI" });
        model.addRow(new String[] { "Markus Kramer", "MPI" });
        model.addRow(new String[] { "Kees Jan van de Looij", "MPI" });
        model.addRow(new String[] { "Marc Pippel", "MPI" });
        model.addRow(new String[] { "Albert Russel", "MPI" });
        model.addRow(new String[] { "Olaf Seibert", "MPI" });
        model.addRow(new String[] { "Han Sloetjes", "MPI" });
        model.addRow(new String[] { "Aarthy Somasundaram", "MPI" });
        model.addRow(new String[] { "Harriet Spenke", "MPI" });
        model.addRow(new String[] { "", "" });
        model.addRow(new String[] { "SIDGrid team", "SIDGrid, Chicago" });
        model.addRow(new String[] { "Ouriel Grynzspan", "CNRS, Paris" }); //H\\u00f4pital de La Salp\\u00e8tri\\u00e8re,
        model.addRow(new String[] { "Mark Blokpoel", "Radboud University, Nijmegen" });
        model.addRow(new String[] { "Martin Schickbichler", "TU Graz" });
        model.addRow(new String[] { "Tom Myers, Consultant, and the Research Staff", "NSF Project \"Five Languages of Eurasia\"" });
        model.addRow(new String[] { "Jeffrey Lemein", "Radboud University, Nijmegen" });
        model.addRow(new String[] { "Micha Hulsbosch", "Radboud University, Nijmegen" });
        model.addRow(new String[] { "Christopher Cox", "University of Alberta" });
        model.addRow(new String[] { "Coralie Villes", "CorpAfroAs, CNRS Villejuif" });
        model.addRow(new String[] { "Christian Chanard", "CorpAfroAs, CNRS Villejuif" });
        model.addRow(new String[] { "Larwan Berke", "DePaul University, Chicago" });
        model.addRow(new String[] { "Bob Kuczewski", "Salk Institute, La Jolla"});
        
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private JTable getTranslatorsTable() {
        DefaultTableModel model = new DefaultTableModel(0, 0);
        model.addColumn(ElanLocale.getString("AboutDialog.Name"));
        model.addColumn(ElanLocale.getString("Menu.Options.Language"));
        
        model.addRow(new String[] {"Alexandre Arkhipov and the NSF-funded Five Languages of Eurasia project", "Russian"});
        model.addRow(new String[] {"Gemma Barbera", "Catalan, Spanish"});
        model.addRow(new String[] {"Li Bin", "Chinese Simplified"});     
        model.addRow(new String[] {"Carl B\u00f6rstell", "Swedish"});
        model.addRow(new String[] {"Elisabet Eir Cortes", "Swedish"});
        model.addRow(new String[] {"Onno Crasborn", "Dutch"});
        model.addRow(new String[] {"Thomas Debay", "French"});
        model.addRow(new String[] {"Florian Gu\u00e9niot", "French"});
        model.addRow(new String[] {"Anna Khoroshkina", "Russian"});
        model.addRow(new String[] {"Alex Klassmann", "German"});
        model.addRow(new String[] {"Alexander Koenig", "German"});
        model.addRow(new String[] {"National Institute of Korean Language", "Korean"});
        model.addRow(new String[] {"Tarc\u00edsio Leite", "Portuguese"});
        model.addRow(new String[] {"Johanna Mesch", "Swedish"});
        model.addRow(new String[] {"Julia Misersky", "German"});
        model.addRow(new String[] {"Kristina Nilsson Bj\u00F6rkenstam", "Swedish"});
        model.addRow(new String[] {"Vlado Plaga", "German"});
        model.addRow(new String[] {"Josep Quer", "Catalan, Spanish"}); 
        model.addRow(new String[] {"Raquel Santiago", "Catalan, Spanish"});       
        model.addRow(new String[] {"Andresa Furtado Schmitz", "Portuguese"});
        model.addRow(new String[] {"Florian Wittenburg", "German"});
        model.addRow(new String[] {"Yuki Yamada", "Japanese"});

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }
    
    private JPanel getCiteElanPanel(){    	
    	JPanel panel = new JPanel(new GridLayout());
    	
    	StringBuilder textBuf = new StringBuilder("<html>");
    	textBuf.append("<b>Guidelines on how to refer to ELAN in papers and articles. </b> <br><br>");
    	textBuf.append("When mentioning ELAN in a paper, article or book please include the following information:<br><br>");
    	textBuf.append("<ul><li>the URL: <b>http://tla.mpi.nl/tools/tla-tools/elan/</b></li><br>");
    	textBuf.append("<li>the institute: <b>Max Planck Institute for Psycholinguistics, The Language Archive, Nijmegen, The Netherlands</b></li><br>");
    	textBuf.append("<li>a reference to at least one of the following papers:</li><br>");
    	textBuf.append("<ul><li>Sloetjes, H., & Wittenburg, P. (2008).<br>"+
    			" Annotation by category - ELAN and ISO DCR.<br>"+
    	        "In: Proceedings of the 6th International Conference on Language Resources and Evaluation (LREC 2008).</li><br>");   
    	textBuf.append("<li>Wittenburg, P., Brugman, H., Russel, A., Klassmann, A., Sloetjes, H. (2006).<br>"+
    	        "ELAN: a Professional Framework for Multimodality Research.<br"+
    	        "In: Proceedings of LREC 2006, Fifth International Conference on Language Resources and Evaluation.</li><br>");    	
    	textBuf.append("<li>Brugman, H., Russel, A. (2004).,br"+
    	        "Annotating Multimedia/ Multi-modal resources with ELAN.<br>" +
    	        "In: Proceedings of LREC 2004, Fourth International Conference on Language Resources and Evaluation.</li><br>");
    	textBuf.append("<li>Crasborn, O., Sloetjes, H. (2008).<br>" +
    	        "Enhanced ELAN functionality for sign language corpora.<br>" +
    	        "In: Proceedings of LREC 2008, Sixth International Conference on Language Resources and Evaluation.</li><br>");
    	textBuf.append("<li>Lausberg, H., & Sloetjes, H. (2009).<br>"+
    	        "Coding gestural behavior with the NEUROGES-ELAN system.<br>"+
    	        "Behavior Research Methods, Instruments, & Computers, 41(3), 841-849. doi:10.3758/BRM.41.3.591.</li></ul></ul><br><br>");
    	textBuf.append("</html>");
    	
    	JLabel info = new JLabel(textBuf.toString());
    	
    	panel.add(info);
    	
    	return panel;
    }
    
    private JPanel getSoftwarePanel(){
    	
    	JPanel panel = new JPanel(new GridLayout());
    	
    	StringBuilder textBuf = new StringBuilder("<html>");
    	textBuf.append("<b>This product includes software developed by : </b> <br><br>");
    	textBuf.append("<ul><li><b>Apache Software Foundation</b> http://apache.org/</li><br>");    	
    	textBuf.append("<li><b>HyperSQL</b> http://hsqldb.org/ </li><br>");
    	textBuf.append("<li><b>University of Sheffield</b> http://gate.ac.uk/gate/ </li><br>");
    	textBuf.append("<li><b>Caprica Software</b> http://www.capricasoftware.co.uk/#/projects/vlcj</li><br>");
    	textBuf.append("<li><b>Staccato</b> SaGA (speech and gesture alignment) project</li><br>");
    	textBuf.append("</html>");
    	
    	JLabel info = new JLabel(textBuf.toString());
    	
    	panel.add(info);
    	
    	return panel;
    }
    
    
}
