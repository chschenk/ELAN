package mpi.eudico.client.annotator.update;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;

import org.xml.sax.InputSource;

/**
 * Class which check for new updates of ELAN 
 * and creates a dialog to show the result.
 * 
 * @author aarsom
 */
public class ElanUpdateDialog {
	/** the major version value */
    private int major = 0;

    /** the minor version value */
    private int minor = 0;

    /** the micro (bug fix) version value */
    //private int micro = 0;    
    
    private String downloadURL;
    
    private final String websiteURL = "http://tla.mpi.nl/tools/tla-tools/elan/";
  
    private String summary;
    
    private boolean update = false;
    
    private boolean error = false;
    
    private boolean automaticUpdate;
    
    private Frame parent;
    
    /**
     * Creates an instance of ElanUpdateDialog
     * 
     * @param frame, the parent object
     */
	public ElanUpdateDialog(Frame frame){	
		this(frame, false);
	}
	
	/**
     * Creates an instance of ElanUpdateDialog
     * 
     * @param frame, the parent object
     * @param autoUpdate, indicates whether the call is made
     * 					 as a result of automatic update or
     * 					from the 'check for updates menu'
     */
	public ElanUpdateDialog(Frame frame, boolean autoUpdate){
		parent = frame;
		automaticUpdate = autoUpdate;
	}
	
	/**
	 * Parses the xml file from website and check for new updates
	 */
	public void checkForUpdates(){	
		UpdateXmlParser reader;
		String errorMessage = ElanLocale.getString("ElanUpdateDialog.Error.UnKnown");
		try {		
			String url;
			String test = System.getProperty("UpdateTest");
			if(test != null && test.toLowerCase().equals("true")){
				url = "http://www.mpi.nl/tools/elan/TestUpdateInfo.xml";	
			} else{
				url = "http://www.mpi.nl/tools/elan/ElanUpdateInfo.xml";					
			}
			URLConnection con = new URL(url).openConnection();	
			reader = new UpdateXmlParser(new InputSource(con.getInputStream()));	
			
			reader.parse();
			
			major = reader.getMajorVersion();
			minor = reader.getMinorVersion();
			//micro = reader.getMicroVersion();
			
			downloadURL =reader.getWebsiteURL();
			
			summary= reader.getSummary();
			
			validateVersionFromFile();
			
			if(!error){
				if(major > ELAN.major){
					update = true;
				} else if(minor > ELAN.minor){
					update = true;
				} 
				/* As of ELAN 5.0 the micro version has been dropped
				 else if(micro > ELAN.micro){
					update = true;
				} 
				*/	
			}
		} catch (ParseException e) {
			error = true;
			ClientLogger.LOG.info("Cannot parse the file: " + e.getMessage());			
			errorMessage = ElanLocale.getString("ElanUpdateDialog.Error.Parse");
			e.printStackTrace();
		}
		catch (IOException e) {
			error = true;
			ClientLogger.LOG.info("URL Connection not available: " + e.getMessage());			
			errorMessage = ElanLocale.getString("ElanUpdateDialog.Error.Internet");
		}	
	
		if(!error){
			Preferences.set("ElanUpdater.LastUpdate", Calendar.getInstance().getTimeInMillis() ,null, false, true);
			if(update){
				showUpdateDialog();
			} else if(!automaticUpdate){
				showMessageDialog(ElanLocale.getString("ElanUpdateDialog.UpToDate"));
			}
		} else {
			if(!automaticUpdate){
				//display a error message
				showMessageDialog(errorMessage + " "+ ElanLocale.getString("ElanUpdateDialog.Error.Part2"));
			}
		}
	}
	
	/**
	 * Error message dialog
	 * 
	 * @param errorMessage, the error message to be shown in the
	 * 						dialog
	 */
	private void showMessageDialog(String message){		
		JLabel messageLabel = new JLabel();
		JLabel linkLabel = getLinkLabel(websiteURL);
		linkLabel.setText("ELAN website");	
		
		JPanel messagePanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(4,6,4,6);
		messagePanel.add(messageLabel, gbc);
		
		if(!message.startsWith(ElanLocale.getString("ElanUpdateDialog.Error.Internet"))){
			gbc.gridy = 1;
			gbc.gridwidth = 1;  
			messagePanel.add(new JLabel(" "+ElanLocale.getString("ElanUpdateDialog.Message")), gbc);
			
			gbc.gridx = 1;
			gbc.insets = new Insets(4,0,4,6);
			messagePanel.add(linkLabel, gbc);
		}
		
		int message_Type;
		ImageIcon icon = null;
		
		if(error){
			messageLabel.setText(message);
			message_Type = JOptionPane.ERROR_MESSAGE;
		}else{
			messageLabel.setText(message);
			message_Type = JOptionPane.PLAIN_MESSAGE;
			icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/ELAN64.png"));
		}
		
		JOptionPane.showMessageDialog(parent, messagePanel, 
				ElanLocale.getString("ElanUpdateDialog.Title"), message_Type, icon);
	}
	
	/**
	 * Checks whether the version in the 
	 * file is less than the current elan version
	 * (shouldn't happen)
	 * 
	 */
	private void validateVersionFromFile(){
		if(major < ELAN.major){
			error = true;
		} else if(major == ELAN.major){
			if(minor < ELAN.minor){
				error = true;
			} 
			/* as of ELAN 5.0 the micro version has been dropped
			else if( minor == ELAN.minor){
				if(micro < ELAN.micro){
					error = true;
				} 			
			}
			*/
		}
	}
	
	/**
	 * Return a label, which a link to 
	 * the given URL
	 * 
	 * @param linkURL, the url to which the string is linked to
	 * 
	 * @return
	 */
	private JLabel getLinkLabel(final String linkURL){
		final JLabel linkLabel = new JLabel();
		linkLabel.setForeground(Color.BLUE);		
		linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkLabel.addMouseListener(new MouseAdapter(){
		    @Override
			public void mousePressed(MouseEvent e) {		    	
		    	linkLabel.setForeground(Color.MAGENTA);
				URI r;
				try {
					r = new URI(linkURL);
					Desktop.getDesktop().browse(r);
				} catch (URISyntaxException e1) {					
					e1.printStackTrace();
				} catch (IOException e2) {					
					e2.printStackTrace();
				}
		    }
		});		
		return linkLabel;
	}
	
	/**
	 * If new updates available, Creates a dialog to show the
	 * details about the new update.
	 * 
	 */
	private void showUpdateDialog(){	
		
		final JDialog dialog = new JDialog(parent);
		dialog.setTitle(ElanLocale.getString("ElanUpdateDialog.Title"));
		if(automaticUpdate){
			dialog.setModal(false);
			dialog.setAlwaysOnTop(true);
		}else {
			dialog.setModal(true);
		}	
		
		ImageIcon elanIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/ELAN128.png"));		
		JLabel imageLabel = new JLabel(elanIcon);
		
		JLabel linkLabel = getLinkLabel(downloadURL);
		linkLabel.setText("<html><u>" + ElanLocale.getString("ElanUpdateDialog.Update2.Part1")+ "</u></html>");
		
		JPanel textPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(4,6,4,6);
		textPanel.add( new JLabel(ElanLocale.getString("ElanUpdateDialog.Update1")), gbc);
		
		gbc.gridy = 1;
		textPanel.add(new JLabel("ELAN " + major + "." + minor), gbc);
		
		gbc.gridy = 2;
		textPanel.add(new JLabel(ElanLocale.getString("ElanUpdateDialog.Update2")), gbc);
		
		gbc.gridy = 3;
		textPanel.add(linkLabel, gbc);

		
		JEditorPane textArea = new JEditorPane();
		textArea.setMargin(new Insets(5,5,5,5));
		textArea.setContentType("text/html");
		textArea.setText("<span style=\"font-size: 20pt\"><b>"+ ElanLocale.getString("ElanUpdateDialog.Summary")+"</b></span><br>" + summary);
		textArea.setEditable(false);
		textArea.setCaretPosition(0);		
		
		JScrollPane scrollPane = new JScrollPane(textArea);			
		
		JButton okButton = new JButton(ElanLocale.getString("Button.OK"));		
		okButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {					
				dialog.dispose();
			}			
		});
		
		JPanel outerPanel = new JPanel(new GridBagLayout());		
		outerPanel.setBorder(new EmptyBorder(10,10,10,10));		
		
		gbc = new GridBagConstraints();		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;		
		outerPanel.add(imageLabel, gbc);
		
		gbc.gridx = 1;
		gbc.insets = new Insets(4,6,4,6);
		outerPanel.add(textPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;			
		gbc.weightx  = 1.0;	
		gbc.weighty  = 1.0;	
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		outerPanel.add(scrollPane, gbc); 	
		
		gbc.gridy = 2;
		gbc.weightx  = 0.0;	
		gbc.weighty  = 0.0;	
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.NONE;
		outerPanel.add(okButton, gbc); 
		
		dialog.getContentPane().add(outerPanel);	
		
		dialog.setPreferredSize(new Dimension(500,600));
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}
