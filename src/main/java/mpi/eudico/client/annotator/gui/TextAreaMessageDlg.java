package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaMessageDlg {

	public TextAreaMessageDlg(String[] text) {
		super();
		showMessage(null, text, null);
	}
	
	public TextAreaMessageDlg(Component c, String[] text) {
		super();
		showMessage(c, text, null);
	}
	
	public TextAreaMessageDlg(Component c, String[] text, String title) {
		super();
		showMessage(c, text, title);
	}
	
	public TextAreaMessageDlg(String text) {
		super();
		showMessage(null, text, null);
	}
	
	public TextAreaMessageDlg(Component c, String text) {
		super();
		showMessage(c, text, null);
	}
	
	public TextAreaMessageDlg(Component c, String text, String title) {
		super();
		showMessage(c, text, title);
	}
	
	private void showMessage(Component parent, String text, String title) {
		JTextArea ta = new JTextArea(text);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		JScrollPane pane = new JScrollPane(ta);
		pane.setPreferredSize(new Dimension (400, 300));
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), pane, 
				title == null ? "ELAN" : title,
	            JOptionPane.PLAIN_MESSAGE, null);
	}
	
	private void showMessage(Component parent, String[] text, String title) {
		JTextArea ta = new JTextArea();
		final String nl = "\n";
		for (String s : text) {
			ta.append(s);
			ta.append(nl);
		}
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		JScrollPane pane = new JScrollPane(ta);
		pane.setPreferredSize(new Dimension (400, 300));
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), pane, 
				title == null ? "ELAN" : title,
	            JOptionPane.PLAIN_MESSAGE, null);
	}
}
