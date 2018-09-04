package mpi.eudico.client.annotator.lexicon;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.lexicon.LexiconLink;

/**
 * If the username and/or password for a certain Lexicon Service is wrong (or empty),
 * this dialog ask the user to enter them.
 * @author Micha Hulsbosch
 *
 */
public class LexiconLoginDialog extends ClosableDialog implements ActionListener {

	private JTextField usernameFld;
	private JPasswordField passwordFld;
	private JButton okBtn;
	private JButton cancelBtn;
	private LexiconLink link;
	private boolean canceled;
	private Component parent;
	
	public LexiconLoginDialog(Frame parent, LexiconLink link) {
		super(parent, ElanLocale.getString("EditLexSrvcDialog.Label.NameAndPassword"), true);
		
		this.link = link;
		this.canceled = true;
		this.parent = parent;
		
		initComponents(link);
		postInit();
	}
	
	public LexiconLoginDialog(Dialog parent, LexiconLink link) {
		super(parent, ElanLocale.getString("EditLexSrvcDialog.Label.NameAndPassword"), true);
		
		this.link = link;
		this.canceled = true;
		this.parent = parent;
		
		initComponents(link);
		postInit();
	}

	private void initComponents(LexiconLink link) {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});
		
		String intro =  "<html>" + ElanLocale.getString("LexiconLoginDialog.Label") + " '"
				+ link.getName() + "' (" + link.getUrl() + ").";
		JLabel introLbl = new JLabel(intro);
		JLabel usernameLbl = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Username"));
		usernameFld = new JTextField();
		JLabel passwordLbl = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Password"));
		passwordFld = new JPasswordField();
		okBtn = new JButton(ElanLocale.getString("Button.OK"));
		okBtn.addActionListener(this);
		cancelBtn = new JButton(ElanLocale.getString("Button.Cancel"));
		cancelBtn.addActionListener(this);
		
		JPanel mainPnl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,5,0,5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		mainPnl.add(introLbl,c);
		
		c.insets = new Insets(15,5,0,0);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		mainPnl.add(usernameLbl, c);
		
		c.insets = new Insets(15,5,0,5);
		c.gridx = 1;
		c.gridy = 1;
		mainPnl.add(usernameFld, c);
		
		c.insets = new Insets(5,5,0,0);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		mainPnl.add(passwordLbl, c);
		
		c.insets = new Insets(5,5,0,5);
		c.gridx = 1;
		c.gridy = 2;
		mainPnl.add(passwordFld, c);
		
		JPanel buttonPnl = new JPanel();
		buttonPnl.setLayout(new BoxLayout(buttonPnl, BoxLayout.LINE_AXIS));
		buttonPnl.add(Box.createHorizontalGlue());
		buttonPnl.add(okBtn);
		buttonPnl.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPnl.add(cancelBtn);
		
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(5,5,5,5);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		mainPnl.add(buttonPnl, c);
		
		mainPnl.setPreferredSize(new Dimension(460, 160));
		
		setContentPane(mainPnl);
		getRootPane().setDefaultButton(okBtn);
	}

	private void postInit() {
		pack();
		
		int w = 450;
	    int h = 200;
	    setSize((getSize().width > w) ? w : getSize().width,
	        (getSize().height < h) ? h : getSize().height);
	    setLocationRelativeTo(parent);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == okBtn) {
			setVisible(false);
			link.getSrvcClient().setUsername(usernameFld.getText());
			link.getSrvcClient().setPassword(new String(passwordFld.getPassword()));
			canceled = false;
			closeDialog();
		} else if (ae.getSource() == cancelBtn) {
			closeDialog();
		}
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	private void closeDialog() {
		dispose();
	}
}
