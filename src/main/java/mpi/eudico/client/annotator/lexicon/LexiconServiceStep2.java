package mpi.eudico.client.annotator.lexicon;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.lexicon.InitializableLexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;

/**
 * The second step of the Lexicon Service Wizard
 * It lets the user select a lexicon
 * @author Micha Hulsbosch
 *
 */
public class LexiconServiceStep2 extends StepPane implements ListSelectionListener {

	private JLabel questionLabel;
	private JList lexiconList;
	private JScrollPane lexiconListScroller;
	private DefaultListModel lexiconListModel;
	private JTextArea lexiconDescArea;
	private JScrollPane lexiconDescScroller;
	
	public LexiconServiceStep2(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	@Override
	protected void initComponents() {
		setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        
        questionLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.SelectLexicon"));
		lexiconListModel = new DefaultListModel();
		lexiconList = new JList(lexiconListModel);
		lexiconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lexiconList.addListSelectionListener(this);
		lexiconListScroller = new JScrollPane(lexiconList);

		lexiconDescArea = new JTextArea("");
		lexiconDescArea.setEditable(false);
		lexiconDescArea.setLineWrap(true);
		lexiconDescScroller = new JScrollPane(lexiconDescArea);
		
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(5,0,5,0);
		c.gridx = 0;
		c.gridy = 0;
		add(questionLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0,0,5,0);
		add(lexiconListScroller, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 2;
		//c.ipady = 40;
		c.insets = new Insets(0,0,5,0);
		add(lexiconDescScroller, c);
	}

	/**
	 * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
	 */
	@Override
	public String getStepTitle() {
        return ElanLocale.getString("EditLexSrvcDialog.Title.Step2");
    }
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
    	multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        fillLexiconList();
    }

	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
    	multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        fillLexiconList();
    }
    
    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
    	
    	return false;
    }
   
    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
    	lexiconListModel.clear();
    	return true;
    }
    
    /**
     * Creates a new Lexicon Link and puts it in the MultiPane to be fetched
     */
    @Override
	public boolean doFinish() {
    	if(lexiconList.getSelectedIndex() >= 0) {
			LexiconServiceClient client = (LexiconServiceClient) multiPane.getStepProperty("serviceClient");
			
			if(client instanceof InitializableLexiconServiceClient) {
				Object[] options = {"OK", "Cancel"};
				int n = JOptionPane.showOptionDialog(this.multiPane.getDialog(), 
						"The lexicon service will now be initialized", 
						"Service initialization", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						options, 
						"Cancel");
				if(n == 1) {
					return false;
				}
				try {
					((InitializableLexiconServiceClient) client).initialize(true);
				} catch (LexiconServiceClientException e) {
					JOptionPane.showMessageDialog(this.multiPane.getDialog(), "The connection could not be established. Please review your credentials.", "Unable to connect", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			
			LexiconLink link = new LexiconLink((String) multiPane.getStepProperty("linkName"),
					client.getType(), client.getUrl(), client, 
					(LexiconIdentification) lexiconList.getSelectedValue());
			multiPane.putStepProperty("newLink", link);
		}
		
		

		return true;
	}
    
    /**
     * Fills the list of lexica
     */
	private void fillLexiconList() {
		Object lexica = multiPane.getStepProperty("lexicaIds");
	    if(lexica instanceof ArrayList<?>) {
	    	for(Object lexicon: (ArrayList<?>) lexica) {
	    		if(lexicon instanceof LexiconIdentification) {
	    			lexiconListModel.addElement(lexicon);
	    		}
	    	}
	    }
	}

	@Override
	public void valueChanged(ListSelectionEvent ae) {
		if(ae.getSource() == lexiconList) {
			if(lexiconList.getSelectedIndex() != -1) {
				lexiconDescArea.setText(((LexiconIdentification) lexiconList.getSelectedValue()).getDescription());
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
			} else {
				lexiconDescArea.setText("");
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
			}
		}
	}

}
