package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FontSizer;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;


/**
 * A simple dialog showing a process report. Currently this is done in a
 * textarea in a scrollpane.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
@SuppressWarnings("serial")
public class ReportDialog extends ClosableDialog implements FontSizer {
    private ProcessReport report;
    private JComponent mainComponent;
    private FontSizePanel fontSizePanel;

    /**
     * Creates a new ReportDialog instance for the specified report.
     *
     * @param report The report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(ProcessReport report) throws HeadlessException {
        super();
        this.report = report;
        initComponents();
    }

    /**
     * Creates a new ReportDialog instance for the specified report with the
     * specified owner for the dialog.
     *
     * @param owner the parent dialog
     * @param report the report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(Dialog owner, ProcessReport report)
        throws HeadlessException {
        super(owner);
        this.report = report;
        initComponents();
    }

    /**
     * Creates a new ReportDialog instance for the specified report with the
     * specified owner for the dialog.
     *
     * @param owner the parent frame
     * @param report the report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(Frame owner, ProcessReport report)
        throws HeadlessException {
        super(owner);
        this.report = report;
        initComponents();
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("ProcessReport"));
        getContentPane().setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(4, 4));

        if (report != null) {
            if ((report.getName() != null) && (report.getName().length() > 0)) {
                content.setBorder(new TitledBorder(report.getName()));
            } else {
                content.setBorder(new TitledBorder(ElanLocale.getString(
                            "ProcessReport")));
            }

            if (report instanceof SimpleReport) {
                JTextArea area = new JTextArea(report.getReportAsString());
                area.setLineWrap(false);
                mainComponent = area;
                
                JScrollPane pane = new JScrollPane(area);
                Dimension dim = new Dimension(400, 300);
                pane.setPreferredSize(dim);
                pane.setMinimumSize(dim);
                content.add(pane);
            } else {
                // the same at this moment, change if e.g. a logrecord based report is available
                JTextArea area = new JTextArea(report.getReportAsString());
                area.setLineWrap(false);
                mainComponent = area;
                
                JScrollPane pane = new JScrollPane(area);
                Dimension dim = new Dimension(400, 300);
                pane.setPreferredSize(dim);
                pane.setMinimumSize(dim);
                content.add(pane);
            }
        } else {
            JLabel mes = new JLabel(ElanLocale.getString(
                        "ProcessReport.NoReport"));
            mes.setPreferredSize(new Dimension(200, 80));
            content.add(mes);
            mainComponent = mes;
        }
        
        JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1, 3, 1, 3);

		JButton saveButton = new JButton(ElanLocale.getString("Button.Save"));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveReport();
			}
		});
		buttonPanel.add(saveButton, gbc);
		gbc.gridx = 1;
		
		JButton closeButton = new JButton(ElanLocale.getString("Button.Close"));
		closeButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				savePreferences();
				close();
			}
		});
		buttonPanel.add(closeButton, gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.EAST;
		fontSizePanel = new FontSizePanel(this);
		buttonPanel.add(fontSizePanel, gbc);
		
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		buttonPanel.add(new JPanel(), gbc);
		
		content.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(content);
        pack();

        if (this.getParent() != null) {
        	Dimension curSize = this.getSize();
        	Dimension parSize = this.getParent().getSize();
        	
        	int dx = parSize.width - curSize.width; 
        	int dy = parSize.height - curSize.height;
        	if ( dx > 0 || dy > 0) {
        		Dimension dim = new Dimension(dx > 0 ? parSize.width : curSize.width, 
        				dy > 0 ? parSize.height : curSize.height);
        		this.setSize(dim);
        	}
            setLocationRelativeTo(this.getParent());
        }
        loadPreferences();
    }
    
    /**
     * Shows a file chooser to the user and saves the report
     */
    private void saveReport() {
		FileChooser chooser = new FileChooser(this);
		List<String[]> extsList = new ArrayList<String[]>(1);
		extsList.add(FileExtension.TEXT_EXT);
        chooser.createAndShowFileDialog(ElanLocale.getString("ReportDialog.FileChooser.Title"), FileChooser.SAVE_DIALOG, 
        		ElanLocale.getString("Button.OK"), extsList, 
        		FileExtension.TEXT_EXT, true, 
        		"LastUsedExportDir", FileChooser.FILES_ONLY, null);  
        File newSaveFile = chooser.getSelectedFile();
        if (newSaveFile != null) {
            try {
                    FileOutputStream out = new FileOutputStream(newSaveFile);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                    writer.write(report.getReportAsString());
                    writer.close();
            } catch (FileNotFoundException ex) {
            	if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
	            	ClientLogger.LOG.warning("File " + newSaveFile + " not found (" + ex.getMessage() + ")");
	            }
            } catch (UnsupportedEncodingException e) {
            	if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
	            	ClientLogger.LOG.warning("Encoding UTF-8 could not be used (" + e.getMessage() + ")");
	            }
			} catch (IOException e) {
				if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
	            	ClientLogger.LOG.warning("Error writing file " + newSaveFile + " (" + e.getMessage() + ")");
	            }
			}
        }
	}
    
    /**
     * Closes this dialog
     */
    private void close() {
    	setVisible(false);
		dispose();
    }
    
    /**
     * Load font size and window bounds, if possible.
     */
    private void loadPreferences() {
    	Integer prefFontSize = Preferences.getInt("ReportDialog.FontSize", null);
    	if (prefFontSize != null) {
    		if (fontSizePanel != null) {
    			fontSizePanel.setFontSize(prefFontSize.intValue());
    		} else {
    			setFontSize(prefFontSize.intValue());
    		}
    	}
    	
    	Rectangle b = Preferences.getRect("ReportDialog.Bounds", null);

    	if (b != null) {
    		GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    		for (GraphicsDevice gd : screens) {
    			if (gd.getDefaultConfiguration().getBounds().intersects(b)) {
    				this.setBounds(b);
    				break;
    			}
    		}
    	}
    }
    
    /**
     * Save font size and window bounds.
     */
    private void savePreferences() {
    	Preferences.set("ReportDialog.FontSize", Integer.valueOf(getFontSize()), null);
    	Preferences.set("ReportDialog.Bounds", this.getBounds(), null);
    }

    /**
     * Sets the font size for the text area or the message label.
     * @param fontSize the new font size
     * @since June 2018
     */
	@Override
	public void setFontSize(int fontSize) {
		if (mainComponent != null) {
			Font f = mainComponent.getFont();
			
			mainComponent.setFont(f.deriveFont((float)fontSize));
		}
		
	}

	/**
	 * @return the current size of the font of the main text area or
	 *  message label
	 *  @since June 2018
	 */
	@Override
	public int getFontSize() {
		if (mainComponent != null) {
			return mainComponent.getFont().getSize();
		}
		
		return Constants.DEFAULTFONT.getSize();
	}
}
