package mpi.eudico.client.annotator.gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import mpi.dcr.DCSmall;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.dcr.ELANDCRDialog;
import mpi.eudico.client.annotator.dcr.ELANLocalDCRConnector;
import mpi.eudico.client.im.ImUtil;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.CVEntry;


@SuppressWarnings("serial")
public class ElanEditCVPanel extends EditCVPanel {
	// language popup and Locales
    private JPopupMenu popup;
    private Locale[] availableLocales;
    private Locale currentLocale;
    //private DCSmall currentDC;
    private boolean prefsChanged = false;

    public ElanEditCVPanel() {
    	super(true);
        // locale support
        entryValueTextField.addMouseListener(new MouseAdapter() {
                @Override
				public void mousePressed(MouseEvent event) {
                    if (SwingUtilities.isRightMouseButton(event) ||
                            event.isPopupTrigger()) {
                        createPopupMenu();

                        if (popup != null) {
                            popup.show(entryValueTextField, event.getX(),
                                event.getY());

                            //popup.setVisible(true);
                        }
                    }
                }
            });
    }

    /**
     * Creates a popup menu containing all Locales available in IMUtils.
     */
    private void createPopupMenu() {
        if (popup == null) {
            try {
                availableLocales = ImUtil.getLanguages();

                popup = new JPopupMenu();

                JMenuItem item;

                for (int i = 0; i < availableLocales.length; i++) {
                    if (i == 0 && availableLocales[i] == Locale.getDefault()) {
                        item = new JMenuItem(availableLocales[i].getDisplayName() + " (System default)");
                        item.setActionCommand(availableLocales[i].getDisplayName());
                    } else {
                        item = new JMenuItem(availableLocales[i].getDisplayName());    
                    }
                    item.addActionListener(this);
                    popup.add(item);
                }
            } catch (java.lang.NoSuchMethodError nsme) {
                // The SPI extensions have not been present at startup.
                //String msg = "Setup incomplete: you won't be able to set languages for editing.";
                String msg = ElanLocale.getString("InlineEditBox.Message.SPI") +
                    "\n" + ElanLocale.getString("InlineEditBox.Message.SPI2");
                JOptionPane.showMessageDialog(this, msg, null,
                    JOptionPane.ERROR_MESSAGE);
                popup = null;
            } catch (Exception exc) {
                //LOG.warning(LogUtil.formatStackTrace(exc));
                popup = null;
            }
        }
    }

    
	/**
	 * Set the DCR ui elements visible.
	 * Set the table cell render.
	 * 
	 * @see mpi.eudico.client.annotator.gui.EditCVPanel#makeLayout()
	 */
    @Override
	protected void makeLayout() {
		super.makeLayout();
		// Override the table data model, so that it returns objects suitable for our
		// table cell renderer, which likes to receive CVEntry objects.
		setTableModel(new CVTableModel() { 
			@Override
	        public Object getValueAt(int row, int col) {
	        	if (entries == null)
	        		return "";
	            return entries[row];
	        }
		});
    	entryTable.setDefaultRenderer(CVEntry.class, new CVEntryTableCellRenderer());
		// make the dcr ui elements visible
		dcrPanel.setVisible(true);
	}

    @Override
    public void setControlledVocabulary(BasicControlledVocabulary cv) {
    	super.setControlledVocabulary(cv);
    	
    }
    
	/**
    * The button actions.
    *
    * @param actionEvent the actionEvent
    */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check if from popup
        if (source instanceof JMenuItem) {
            // language menuitem
            String locale = actionEvent.getActionCommand();

            for (int i = 0; i < availableLocales.length; i++) {
                if (availableLocales[i].getDisplayName().equals(locale)) {
                    currentLocale = availableLocales[i];
                    ImUtil.setLanguage(entryValueTextField, currentLocale);
                    entryValueTextField.setFont(Constants.DEFAULTFONT);

                    return;
                }
            }
        } else if (source == dcrButton) {
        	    Window ancestor = SwingUtilities.getWindowAncestor(this);
        	    ELANDCRDialog dialog = null;
        	    if (ancestor instanceof JDialog) {
        	    	    dialog = new ELANDCRDialog((JDialog) ancestor, true, ELANDCRDialog.LOCAL_MODE);
        	    } else if (ancestor instanceof JFrame) {
        	    	    dialog = new ELANDCRDialog((JFrame) ancestor, true, ELANDCRDialog.LOCAL_MODE);
        	    }
	    		if (dialog == null) {
	    			return;
	    		}
	    		dialog.pack();
	    		dialog.setVisible(true);
	    		Object selValue = dialog.getValue();
	    		if (selValue instanceof List<?>) {
	    			List<?> vals = (List<?>) selValue;
	    			if (vals.size() > 0) {
	    				Object valueObj = vals.get(0);
	    				if (valueObj instanceof DCSmall) {
	    					DCSmall dcs = (DCSmall) valueObj;
	    					//currentDC = dcs;
	    					dcrField.setText(dcs.getIdentifier());
	    					dcIdField.setText(dcs.getId());
	    					dcrRemoveButton.setEnabled(true);
	    				}
	    			}
	    		}
	    		return;
        } else if(source == dcrRemoveButton){
        	dcIdField.setText(EMPTY);
			dcrField.setText(EMPTY);
			dcrRemoveButton.setEnabled(false);
        }
        else if (source == moreOptionsButton) {
        	// create a more options dialog
        	if (currentEntry != null) {
        		Window ancestor = SwingUtilities.getWindowAncestor(this);
        		if (ancestor instanceof JDialog) {
        			CVEntryOptionsDialog dialog = new CVEntryOptionsDialog((JDialog) ancestor,
        											true, currentEntry, getSelectedColumn());
        			dialog.setVisible(true);//blocks
        			CVEntry copy = dialog.getCVEntry();
        			if (copy != null) {// if dialog is canceled or not changed copy is null
        				applyPrefChanges(copy);
        			}
        		}
        	}
        	return;
        }

        super.actionPerformed(actionEvent);
    }

    @Override
    protected void updateLabels() {
        moveToTopButton.setToolTipText(ElanLocale.getString(
                "EditCVDialog.Button.Top"));
        moveUpButton.setToolTipText(ElanLocale.getString(
                "EditCVDialog.Button.Up"));
        moveDownButton.setToolTipText(ElanLocale.getString(
                "EditCVDialog.Button.Down"));
        moveToBottomButton.setToolTipText(ElanLocale.getString(
                "EditCVDialog.Button.Bottom"));
        deleteEntryButton.setText(ElanLocale.getString("Button.Delete"));
        changeEntryButton.setText(ElanLocale.getString("Button.Change"));
        addEntryButton.setText(ElanLocale.getString("Button.Add"));
        entryDescLabel.setText(ElanLocale.getString(
                "EditCVDialog.Label.EntryDescription"));
        entryValueLabel.setText(ElanLocale.getString("EditCVDialog.Label.Value"));
        setBorder(new TitledBorder(ElanLocale.getString(
                    "EditCVDialog.Label.Entries")));
        dcrLabel.setText(ElanLocale.getString("DCR.Label.ISOCategory"));
        dcrRemoveButton.setToolTipText(ElanLocale.getString("DCR.Label.RemoveReference"));
        dcrButton.setText(ElanLocale.getString("Button.Browse"));
        invalidValueMessage = ElanLocale.getString(
                "EditCVDialog.Message.EntryValidValue");
        valueExistsMessage = ElanLocale.getString(
                "EditCVDialog.Message.EntryExists");

        undoButton.setToolTipText(ElanLocale.getString("Menu.Edit.Undo"));
        redoButton.setToolTipText(ElanLocale.getString("Menu.Edit.Redo"));
        if (moreOptionsButton != null) {
        	moreOptionsButton.setText(ElanLocale.getString("EditCVDialog.Label.MoreOptions"));
        }
    }

    /**
     * Returns whether entry preferences have been changed.
     * 
     * @return whether entry preferences have been changed
     */
    public boolean isPrefsChanged() {
    	return prefsChanged;
    }
    
	/** 
	 * Checks value, description and dcr fields and adds an entry.
	 * 
	 * @see mpi.eudico.client.annotator.gui.EditCVPanel#addEntry()
	 */
    @Override
	protected void addEntry() {
		if (cv == null) {
            return;
        }

        String entry = entryValueTextField.getText();

        entry = entry.trim();

        if (entry.length() == 0) {
            showWarningDialog(invalidValueMessage);

            return;
        }

        int language = getSelectedColumn();
        
        if (cv.containsValue(language, entry)) {
            showWarningDialog(valueExistsMessage);
        } else {
            String desc = entryDescTextField.getText();

            if (desc != null) {
                desc = desc.trim();
            }

            CVEntry newEntry = new CVEntry(cv, language, entry, desc);
            if (dcrField.getText() != null && dcIdField.getText().length() > 0 /*&& currentDC != null*/) {
            	//newEntry.setExternalRef(currentDC.getId());// or be on the safe side and check the contents of the field?
            	ExternalReferenceImpl eri = new ExternalReferenceImpl(dcIdField.getText(), ExternalReference.ISO12620_DC_ID);
            	newEntry.setExternalRef(eri);
            }
            cv.addEntry(newEntry);
            updateList();

            //make text fields free for next input!
            setSelectedEntry(null);
        }
	}

	/**
	 * Checks value, description and dcr fields and changes an entry.
	 * 
	 * @see mpi.eudico.client.annotator.gui.EditCVPanel#changeEntry()
	 */
	@Override
	protected void changeEntry() {
		if (cv == null) {
            return;
        }

        String newValue = entryValueTextField.getText().trim();
        int language = getSelectedColumn();

        // only object if ALL language values are empty
       if (newValue.isEmpty()) {
    	   boolean ok = false;
    	   for (int i = 0; i < cv.getNumberOfLanguages(); i++) {
    		   if (i != language && !currentEntry.getValue(i).isEmpty()) {
    			   ok = true;
    			   break;
    		   }
    	   }
    	   if (!ok) {
	            showWarningDialog(invalidValueMessage);
	            entryValueTextField.setText((currentEntry != null)
	                ? currentEntry.getValue(language) : "");
	
	            return;
    	   }
        }

        String newDescription = entryDescTextField.getText().trim();
        
        if (newValue.equals(currentEntry.getValue(language))) {
            if ((newDescription != null) &&
                    !newDescription.equals(currentEntry.getDescription(language))) {
                CVEntry newEntry = new CVEntry(currentEntry, language, newValue, newDescription);
                // check dc Id
                String dcId = dcIdField.getText();
                if (dcId != null && dcId.length() != 0) {
                	// always create a new ext ref instance because of undo/redo
                	newEntry.setExternalRef(new ExternalReferenceImpl(dcId, ExternalReference.ISO12620_DC_ID));
                }
                
                cv.replaceEntry(currentEntry, newEntry);
                updateList();
                setSelectedEntry(newEntry);
            } else {
            	// check dc Id
            	String dcId = dcIdField.getText();
            	if (dcId != null && dcId.length() > 0) {          		
            		
            		if (currentEntry.getExternalRef() instanceof ExternalReferenceImpl) {
                		if (!dcId.equals(((ExternalReferenceImpl) currentEntry.getExternalRef()).getValue())) {
                			// changed dc id value, replace the entry
                            CVEntry newEntry = new CVEntry(currentEntry, language, newValue, newDescription);
                			newEntry.setExternalRef(new ExternalReferenceImpl(dcId, ExternalReference.ISO12620_DC_ID));
                            cv.replaceEntry(currentEntry, newEntry);
                            updateList();
                            setSelectedEntry(newEntry);
                		} 
                	} else {
                		// new dc id value
                        CVEntry newEntry = new CVEntry(currentEntry, language, newValue, newDescription);
                		newEntry.setExternalRef(new ExternalReferenceImpl(dcId, ExternalReference.ISO12620_DC_ID));
                        cv.replaceEntry(currentEntry, newEntry);
                        updateList();
                        setSelectedEntry(newEntry);
                	}
            	}
            }

            return;
        }

        // entry value has changed...
        if (!newValue.isEmpty() && cv.containsValue(language, newValue)) {
            showWarningDialog(valueExistsMessage);
        } else {
            CVEntry newEntry = new CVEntry(currentEntry, language, newValue, newDescription);
            //check dcr
        	String dcId = dcIdField.getText();
        	if (dcId != null && dcId.length() > 0) { 
        		newEntry.setExternalRef(new ExternalReferenceImpl(dcId, ExternalReference.ISO12620_DC_ID));
        	}
            
            cv.replaceEntry(currentEntry, newEntry);
            updateList();
            setSelectedEntry(newEntry);
        }
	}

	/**
	 * Checks value, description and dcr fields and updates the entry fields.
	 * 
	 * @see mpi.eudico.client.annotator.gui.EditCVPanel#updateTextFields()
	 */
	@Override
	protected void updateTextFields() {
		super.updateTextFields();
		CVEntry selEntry = getSelectedEntry();
		//if (entryList.getSelectedIndex() == -1) {
		if (selEntry == null) {
			dcIdField.setText(EMPTY);
			dcrField.setText(EMPTY);
			dcrRemoveButton.setEnabled(false);
		} else {
			//CVEntry selEntry = (CVEntry) entryList.getSelectedValue();
			//CVEntry selEntry = getSelectedEntry();
			if (selEntry.getExternalRef() instanceof ExternalReferenceImpl) {
				ExternalReferenceImpl eri = (ExternalReferenceImpl) selEntry.getExternalRef();
				String dcId = eri.getValue();
				dcIdField.setText(dcId);
				DCSmall sm = ELANLocalDCRConnector.getInstance().getDCSmall(dcId);
				
				if (sm != null) {
					dcrField.setText(sm.getIdentifier());
				}	
				dcrRemoveButton.setEnabled(true);
			} /*else if (selEntry.getExternalRef() instanceof String) {
				String dcId = (String) selEntry.getExternalRef();
				dcIdField.setText(dcId);
				DCSmall sm = ELANLocalDCRConnector.getInstance().getDCSmall(dcId);
				
				if (sm != null) {
					dcrField.setText(sm.getIdentifier());
				}
			} */else {
				dcIdField.setText(EMPTY);
				dcrField.setText(EMPTY);
				dcrRemoveButton.setEnabled(false);
			}
		}
	}
    
	private void applyPrefChanges(CVEntry copyEntry) {		
		currentEntry.setPrefColor(copyEntry.getPrefColor());
		// check shortcut
		if (copyEntry.getShortcutKeyCode() != -1) {
			boolean doChange = true;
			// Check for uniqueness of the shortcut key.
			for (CVEntry cve : cv) {
				if (cve == currentEntry) {
					continue;
				}
				if (cve.getShortcutKeyCode() == copyEntry.getShortcutKeyCode() && cve != currentEntry) {
					// prompt change existing or don't change current
					int option = JOptionPane.showOptionDialog(this, ElanLocale.getString("EditCVDialog.Message.ShortcutUsed") + " " + cve.getValue(0), 
							"ELAN", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
							new String[]{ElanLocale.getString("EditCVDialog.Message.ShortcutChange"), 
						ElanLocale.getString("EditCVDialog.Message.ShortcutDontChange")}, ElanLocale.getString("EditCVDialog.Message.ShortcutChange"));
					
					if (option == JOptionPane.YES_OPTION) {
						cve.setShortcutKeyCode(-1);
					} else {
						doChange = false;
					}
					break;
				}
			}			
			if (doChange) {
				// apply after the last entry has been checked
				currentEntry.setShortcutKeyCode(copyEntry.getShortcutKeyCode());					
			}
		} else {
			currentEntry.setShortcutKeyCode(copyEntry.getShortcutKeyCode());
		}
		prefsChanged = true;
		updateList();	
	}
    
}
