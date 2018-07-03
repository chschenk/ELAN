package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.MonitoringLogger;

/**
 * Dialog to start and stop and manage the monitoring process  
 * of elan
 * 
 * @author aarsom
 *
 */
@SuppressWarnings("serial")
public class ActivityMonitoringDialog extends ClosableDialog implements ActionListener, ChangeListener{

	private JButton startButton;
	private JButton stopButton;	
	private JButton advancedOptionsButton;
	
	private JPanel advancedOptionsPanel;
	private JPanel buttonsPanel;	
	
	private JCheckBox setLocationCB;
	private JCheckBox filesPerSessionCB;
	private JCheckBox appendToFileCB;
	private JCheckBox alwaysMonitorCB;
	private JTextField pathTextField;
	private JButton browseButton; 
	
	private static ActivityMonitoringDialog dialog;
	
	private final ImageIcon triangleUp = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/triangle_up.gif"));
	private final ImageIcon triangleDown = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/triangle_down.gif"));	
	
	private final int minWidth = 275;
	private final int minHeight = 75;
	
	private ActivityMonitoringDialog() {		
		initComponents();
		postInit();
	}
	
	/**
	 * To get a singleton instance of the dialog
	 * 
	 * @return ActivityMonitoringDialog
	 */
	public static ActivityMonitoringDialog getInstance(){
		if(dialog == null){
			dialog = new ActivityMonitoringDialog();
		}		
		return dialog;
	}

	private void postInit() {
		 pack();
	     setLocationRelativeTo(getParent());	    
	     setSize((getSize().width < minWidth) ? minWidth : getSize().width,
	            (getSize().height < minHeight) ? minHeight : getSize().height);
	     setResizable(true);
	}

	/**
	 * Initializes the components
	 */
	private void initComponents() {
		setTitle(ElanLocale.getString("Menu.Options.ActivityMonitoring"));
        getContentPane().setLayout(new GridBagLayout());
       
        //initialize the components
        startButton = new JButton(ElanLocale.getString("Button.Start"));        
    	
        stopButton = new JButton(ElanLocale.getString("Button.Stop"));
    	stopButton.setEnabled(false);
    	
    	if(MonitoringLogger.isInitiated()){
        	startButton.setText(ElanLocale.getString("Button.Pause"));
        	stopButton.setEnabled(true);
        }
    	
    	browseButton = new JButton(ElanLocale.getString("Button.Browse"));  
    	browseButton.setEnabled(false);
    	
    	advancedOptionsButton = new JButton();
    	advancedOptionsButton.setIcon(triangleDown);  
    	advancedOptionsButton.setToolTipText(ElanLocale.getString("ActivityMonitoringDialog.TriangleDown.ToolTip"));
    	advancedOptionsButton.setMaximumSize(advancedOptionsButton.getPreferredSize()); 
    	
    	Insets insets = new Insets(4,2,4,2);    
    	
    	buttonsPanel = new JPanel();
    	buttonsPanel.setLayout(new GridLayout(1,2,0,0));  
    	buttonsPanel.add(startButton);    	
    	buttonsPanel.add(stopButton);    
    	
    	//advancedOptionsButton.setBorderPainted(false);
    	//buttonsPanel.add(advancedOptionsButton);  
        
        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        advancedOptionsButton.addActionListener(this);   
        
        getContentPane().setLayout(new GridBagLayout()); 
    	
        GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = insets;   
    	gbc.anchor = GridBagConstraints.NORTH;      	
    	getContentPane().add(buttonsPanel, gbc);    
    	
     	gbc.gridx = 1;  
     	gbc.anchor = GridBagConstraints.EAST;   
    	getContentPane().add(advancedOptionsButton, gbc);
	}
	
	private JPanel getAdvancedOptionsPanel(){
		if(advancedOptionsPanel != null){
			return advancedOptionsPanel;
		}
		
		advancedOptionsPanel = new JPanel();
		advancedOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString("DisplaySettingsPane.Label.AdvancedOptions")));
		advancedOptionsPanel.setLayout(new GridBagLayout());
		
		setLocationCB = new JCheckBox(ElanLocale.getString("ActivityMonitoringDialog.CB.SetLocation"));
		setLocationCB.addChangeListener(this);
		setLocationCB.addActionListener(this);	
		
    	pathTextField = new JTextField();    
    	pathTextField.setEnabled(false);  
    	pathTextField.setColumns(5);
		    	
    	browseButton  = new JButton(ElanLocale.getString("Button.Browse"));
    	browseButton.setEnabled(false);  
    	browseButton.addActionListener(this);  
    	
    	Boolean boolPref = Preferences.getBool("ActivityMonitoring.UseLocation", null);
		if (boolPref != null) {
			setLocationCB.setSelected(boolPref.booleanValue());
    	}	
		
		String stringPref = Preferences.getString("ActivityMonitoring.PathLocation", null);
		if (stringPref != null) {
			pathTextField.setText(stringPref);
    	}
    	
    	filesPerSessionCB = new JCheckBox(ElanLocale.getString("ActivityMonitoringDialog.CB.FilesPerSession"));
    	boolPref = Preferences.getBool("ActivityMonitoring.FilesPerSession", null);
		if (boolPref != null) {
    		filesPerSessionCB.setSelected(boolPref.booleanValue());
    	}
    	filesPerSessionCB.addActionListener(this);    	
    	
    	appendToFileCB = new JCheckBox(ElanLocale.getString("ActivityMonitoringDialog.CB.AppendToFile"));
    	appendToFileCB.setSelected(true);    	
    	boolPref = Preferences.getBool("ActivityMonitoring.AppendToFile", null);
		if (boolPref != null) {
    		appendToFileCB.setSelected(boolPref.booleanValue());
    	} 
		appendToFileCB.setEnabled(!filesPerSessionCB.isSelected());	
		appendToFileCB.addActionListener(this);
    	
    	alwaysMonitorCB = new JCheckBox(ElanLocale.getString("ActivityMonitoringDialog.CB.AlwaysMonitor")); 
    	boolPref = Preferences.getBool("ActivityMonitoring.AlwaysStartMonitoring", null);
		if (boolPref != null) {
    		alwaysMonitorCB.setSelected(boolPref.booleanValue());
    	}
    	alwaysMonitorCB.addActionListener(this);
    	
    	Insets insets = new Insets(2,4,2,4);    	    
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = insets;   
    	gbc.anchor = GridBagConstraints.NORTHWEST;  
    	gbc.fill = GridBagConstraints.NONE;    
    	advancedOptionsPanel.add(setLocationCB, gbc);
	
    	gbc.gridx = 1;    	
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 0.5;		
    	advancedOptionsPanel.add(pathTextField, gbc);
 	
    	gbc.gridx = 2;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.anchor = GridBagConstraints.EAST;	
    	gbc.weightx = 0.0;		
    	advancedOptionsPanel.add(browseButton, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;    	
    	gbc.gridwidth = 3;
    	gbc.anchor = GridBagConstraints.NORTHWEST;	    		
    	advancedOptionsPanel.add(filesPerSessionCB, gbc);
    	
    	gbc.gridy = 2;
    	advancedOptionsPanel.add(appendToFileCB, gbc);
    	
    	gbc.gridy = 3; 
    	advancedOptionsPanel.add(alwaysMonitorCB, gbc);
    	
    	return advancedOptionsPanel;
	}
	
	/**
	 * Dialog to select the folder where the log files are to be stored
	 */
	private void getOpenFileName() {	
	    FileChooser chooser = new FileChooser(this);
	    chooser.createAndShowFileDialog(ElanLocale.getString("ActivityMonitoringDialog.OpenDialog.Title"), FileChooser.OPEN_DIALOG, 
	    		ElanLocale.getString("Button.Select"), null, null, true, "LastUsedEAFDir", FileChooser.DIRECTORIES_ONLY, null);

	    File folder = chooser.getSelectedFile();	       
	    if(folder != null){
	    	MonitoringLogger.setDirectory(folder.getPath());
            pathTextField.setText(folder.getPath());
            Preferences.set("ActivityMonitoring.PathLocation", pathTextField.getText().trim() , null);
	    } 
	}

	@Override
	public void actionPerformed(ActionEvent e) {	
		if(e.getSource() == startButton){
			stopButton.setEnabled(true);
			if(startButton.getText().equals(ElanLocale.getString("Button.Start"))){
				startButton.setText(ElanLocale.getString("Button.Pause"));
				MonitoringLogger.startMonitoring(true);					
				MonitoringLogger.logInAllLoggers(MonitoringLogger.MONITORING_STARTED);
			} else {
				startButton.setText(ElanLocale.getString("Button.Start"));	
				MonitoringLogger.logInAllLoggers(MonitoringLogger.MONITORING_PAUSED);
				MonitoringLogger.startMonitoring(false);
			}
			stopButton.setEnabled(true);
			//Preferences.set("ActivityMonitoringDialog.Started", true, null);
		}
		else if(e.getSource() == stopButton){			
			MonitoringLogger.logInAllLoggers(MonitoringLogger.MONITORING_STOPPED);
			MonitoringLogger.startMonitoring(false);
			stopButton.setEnabled(false);
			startButton.setText(ElanLocale.getString("Button.Start"));
			//Preferences.set("ActivityMonitoringDialog.Started", false, null);
		}		
		// shows/hides the advanced options panel
		else if(e.getSource() == advancedOptionsButton){
			if(advancedOptionsButton.getIcon() == triangleDown){
				GridBagConstraints gbc = new GridBagConstraints();
		    	gbc.insets = new Insets(4,2,4,2);    	   
		    	gbc.anchor = GridBagConstraints.NORTHWEST;  
		    	gbc.weightx = 0.5;
		    	gbc.weighty = 0.5;
		    	gbc.fill = GridBagConstraints.HORIZONTAL;   
		    	gbc.gridy = 1;
		    	gbc.gridwidth = 2;
				getContentPane().add(getAdvancedOptionsPanel(), gbc);
				advancedOptionsButton.setIcon(triangleUp);
				advancedOptionsButton.setToolTipText(ElanLocale.getString("ActivityMonitoringDialog.TriangleUp.ToolTip"));
				pack();
			} else {
				advancedOptionsButton.setIcon(triangleDown);
				advancedOptionsButton.setToolTipText(ElanLocale.getString("ActivityMonitoringDialog.TriangleDown.ToolTip"));
				getContentPane().remove(getAdvancedOptionsPanel());
				pack();
				setSize((getSize().width < minWidth) ? minWidth : getSize().width,
			            (getSize().height < minHeight) ? minHeight : getSize().height);
			}
		} 
		// opens a filechooser dialog to select the destination folder
		else if(e.getSource() == browseButton){
			getOpenFileName();
		} else if(e.getSource() == alwaysMonitorCB){
			Preferences.set("ActivityMonitoring.AlwaysStartMonitoring", alwaysMonitorCB.isSelected() , null);
		}  else if(e.getSource() == filesPerSessionCB){
			Preferences.set("ActivityMonitoring.FilesPerSession", filesPerSessionCB.isSelected() , null);			
			MonitoringLogger.createNewFilesPerSession(filesPerSessionCB.isSelected());
			appendToFileCB.setEnabled(!filesPerSessionCB.isSelected());			
		} else if(e.getSource() == appendToFileCB){
			MonitoringLogger.setAppendFileFlag(appendToFileCB.isSelected());
			Preferences.set("ActivityMonitoring.AppendToFile", appendToFileCB.isSelected() , null);
		} else if(e.getSource() == setLocationCB){
			Preferences.set("ActivityMonitoring.UseLocation", setLocationCB.isSelected() , null);
			if(!setLocationCB.isSelected()){
				MonitoringLogger.setDirectory(null);
			} else {				
				if(pathTextField.getText() != null && pathTextField.getText().length() > 0){
					MonitoringLogger.setDirectory(pathTextField.getText().trim());
				} else{
					MonitoringLogger.setDirectory(null);
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		pathTextField.setEnabled(setLocationCB.isSelected());
		browseButton.setEnabled(setLocationCB.isSelected());		
	}
}

