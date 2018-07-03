package mpi.eudico.client.annotator.prefs.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

/**
 * Abstract class for which sets a common UserInterface
 * setup for different panels in the edit preferences dialog
 * 
 * @author aarsom
 *
 */
public abstract class AbstractEditPrefsPanel extends JPanel {

	private JScrollPane scrollPane;	
	protected JPanel outerPanel;
	
	protected Insets globalInset = new Insets(2, 6, 2, 6);
	protected Insets globalPanelInset = new Insets(6, 15, 2, 6);	
	protected Insets singleTabInset = new Insets(2,34,2,0);
	protected Insets doubleTabInset = new Insets(2,60,2,0);
	
	protected Insets topInset = new Insets(4,0,0,0);
	protected Insets leftInset = new Insets(0,6,0,0);
	protected Insets catInset = new Insets(15,2,5,2); 
	protected Insets catPanelInset = new Insets(2, 15, 2, 6);	
	protected Insets smallCatInset = new Insets(5,2,2,2); 
	
	/**
	 * Constructor
	 */
	public AbstractEditPrefsPanel(){
		super();			
		initComponents("");
	}
	
	/**
	 * Constructor
	 * 
	 * @param title tilte for the panel
	 */
	public AbstractEditPrefsPanel(String title){
		super();			
		initComponents(title);
	}
    
	/**
	 * Initialize basic components
	 * 
	 * @param title title fot the panel
	 */
	private void initComponents(String title){
		outerPanel = new JPanel(new GridBagLayout());
		
    	scrollPane = new JScrollPane(outerPanel);
        scrollPane.setBorder(new TitledBorder(title));   
        scrollPane.setBackground(outerPanel.getBackground());  
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;   
        gbc.weightx = 1.0;  
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(scrollPane , gbc);
    }
	
	/**
	 * Set the title for the panel
	 * 
	 * @param title, the title for the panel
	 */
	protected void setTitle(String title){
		((TitledBorder)scrollPane.getBorder()).setTitle(title);
	}
}
