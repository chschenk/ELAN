package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.gui.CtrlWCloseAction;
import mpi.eudico.client.annotator.gui.EscCloseAction;
import mpi.eudico.client.annotator.multiplefilesedit.MFEModel.Changes;
import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.dobes.LanguageRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

@SuppressWarnings("serial")
public class MFEFrame extends JFrame implements ActionListener, ProgressListener {

	private static final Insets insets = new Insets(1,1,1,1);
	
	private MFEModel model;

    private DomainPane domainPanel;
    private TableByTierPanel tableByTierPanel;
    private TableByTypePanel tableByTypePanel;
    
    Set<String> parseErrorSet = new HashSet<String>();	// a Set to prevent duplicates after multiple domain selections
    
    private JProgressBar progressBar;
    private JButton closeButton;
    
//    private boolean isRunning = false;
    
    /**
     * This method starts the multiple file editor by creating a JFrame with all the
     * required contents.
     * @param name The title of the JFrame.
     */
	public MFEFrame(String name) {
		super();
		model = new MFEModel();
		
		//setVisible(true);
		setSize(640,480);
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		ensureMultipleLanguagesKnown();
		initComponents();
		updateLocale();
		pack();
//		addCloseActions();
		postInit();
	}
	
	private void initComponents() {
		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());
        c.insets=insets;
		
        /* Add the domain panel */
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx=1;
        c.fill=GridBagConstraints.BOTH;
        domainPanel = new DomainPane(this);
        add(domainPanel,c);
        
        /* Add tab panel */
        c.gridy=1;
        c.weighty=0.9;
        c.fill = GridBagConstraints.BOTH;
        JTabbedPane tabbedPane = new JTabbedPane();
	        /* Add the tier table */
	        tableByTierPanel = new TableByTierPanel(model);
			tabbedPane.addTab(ElanLocale.getString("MFE.TierTab.Title"), null,
					tableByTierPanel, ElanLocale.getString("MFE.TierTab.TitleMO"));
			/* Add the type table */
	        tableByTypePanel = new TableByTypePanel(model, this);
			tabbedPane.addTab(ElanLocale.getString("MFE.TypeTab.Title"), null,
					tableByTypePanel, ElanLocale.getString("MFE.TypeTab.TitleMO"));
		add(tabbedPane,c);
		
		/* Add the progress bar and buttons */
		c.gridy=2;
		c.weighty=0;
		progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar,c);
		
		c.gridy=3;
		c.gridwidth = 1;
		c.weightx = 0.5;
		closeButton = new JButton(ElanLocale.getString("Button.Close"));
		closeButton.setActionCommand("CloseMultiFiles");
		closeButton.addActionListener(this);
		add(closeButton,c);
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent we) {
				Preferences.set("MFE.FrameBounds", MFEFrame.this.getBounds(), null, false, false);
			}
		});
	}
	
	private void postInit() {
		Rectangle bounds = Preferences.getRect("MFE.FrameBounds", null);
		if (bounds != null) {
			// check size compare to window size
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			if (bounds.width > dim.width) {
				bounds.width = dim.width;
			}
			if (bounds.height > dim.height) {
				bounds.height = dim.height;
			}
			setBounds(bounds);
		}
	}
	
	/**
	 * We're probably going to show "multiple languages" in a combobox.
	 * Make sure it is available. We need this to happen some time before the
	 * comboboxes in the table are created.
	 */
	private void ensureMultipleLanguagesKnown() {
		RecentLanguages rl = RecentLanguages.getInstance();
		LangInfo info = rl.getLanguageInfo(MFEModel.MULTIPLE_LANGUAGES_ID);
		if (info == null) {
			info = LanguageCollection.getLanguageInfo(MFEModel.MULTIPLE_LANGUAGES_ID);
			if (info != null) {
				rl.addRecentLanguage(info);
			}
		}
	}
	
	/**
	 * Pass-through method to initialize the combobox column in the tier panel.
	 */
	public void initCombobox() {
		tableByTierPanel.initCombobox();
	}
	/**
	 * Starts an EAFLoadThread to load the files from the selected domain.
	 */
	public void loadFiles() {
    	enableUI(false);

    	final EAFLoadThread elt = new EAFLoadThread();
		
		try {
        	elt.start();
        } catch (IllegalThreadStateException ie) {
            ie.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	/**
	 * Starts an EAFWriteThread to write all changes to the appropriate domain files.
	 */
	public void writeChanges() {
    	enableUI(false);

		int saveAsType = SaveAs27Preferences.saveAsType(null);
		final EAFWriteThread ewt = new EAFWriteThread(saveAsType);
		
		try {
			ewt.start();
		} catch (IllegalThreadStateException ie) {
            ie.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void updateLocale() {
		domainPanel.updateLocale();
		tableByTierPanel.updateLocale();
		tableByTypePanel.updateLocale();
		closeButton.setText(ElanLocale.getString("Button.Close"));
		setTitle(ElanLocale.getString("MFE.FrameTitle"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton)
		{
			if(e.getActionCommand().equals("CloseMultiFiles")) {
				this.dispose();
			}
		}
	}
	
    /**
     * Notification that the progress is completed.
     *
     * @param source the source
     * @param message the message
     */
    @Override
	public void progressCompleted(Object source, String message) {
        if (source instanceof EAFLoadThread) {
            progressBar.setValue(0);
            progressBar.setString("");
            enableUI(true);
        } else {
            //popup a message and show report
            progressBar.setValue(100);
            progressBar.setString(message);

            // update ui, set running state
            enableUI(true);
//            isRunning = false;
            progressBar.setValue(0);
            progressBar.setString("");
        }
    }
    
    /**
     * Call progressCompleted from an application thread.
     * 
     * @param source
     * @param message
     */
    private void threadProgressCompleted(final Object source, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressCompleted(source, message);
			}});
    }

    /**
     * Shows message and or report, updates ui elements.
     * (Doesn't seem to be called in practice.)
     *
     * @param source the source
     * @param message the message
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        if (source instanceof EAFLoadThread) {
            progressBar.setValue(0);
            progressBar.setString("");
        } else if (source instanceof EAFWriteThread) {
            progressBar.setValue(0);
            progressBar.setString("");
        } else {
            // popup a message and show report
            progressBar.setString(message);
            showWarningDialog(ElanLocale.getString(
                    "MultipleFileSearch.FindReplace.Warn5"));

            // show report
//            enableUI(true);
//            isRunning = false;
            progressBar.setValue(0);
            progressBar.setString("");
        }
        enableUI(true);
    }

    /**
     * Updates the progress bar.
     *
     * @param source the source
     * @param percent the percentage completed
     * @param message a message to display
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if (percent == progressBar.getMaximum()) {
            progressCompleted(source, message);
        } else {
            progressBar.setValue(percent);
            progressBar.setString(message);
        }
    }
    
    /**
     * Call progressUpdated() from an application thread.
     */
    private void threadProgressUpdated(final Object source, final int percent, final String message) {
 		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressUpdated(source, percent, message);
			}});
    }
    
    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    private void enableUI(boolean b) {
    	boolean enableEdit = b && domainPanel.getSearchFiles().length > 0;
    	
    	domainPanel.enableUI(b);
    	tableByTierPanel.enableUI(enableEdit);
    	tableByTypePanel.enableUI(enableEdit);
        closeButton.setEnabled(b);
    	if (b) {
            MFEFrame.this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	} else {
        	MFEFrame.this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    	}
    }
    /**
     * Add the Escape and Ctrl-W close actions.
     */
    protected void addCloseActions() {
        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction wAction = new CtrlWCloseAction(this);

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            String esc = "esc";
            inputMap.put((KeyStroke) escAction.getValue(Action.ACCELERATOR_KEY),
                esc);
            actionMap.put(esc, escAction);

            String wcl = "cw";
            inputMap.put((KeyStroke) wAction.getValue(Action.ACCELERATOR_KEY),
                wcl);
            actionMap.put(wcl, wAction);
        }
    }
	
//	public boolean isRunning() {
//		return isRunning;
//	}
	/**
	 * Writes all changes in the MFEModel to the files within the domain.
	 * 
	 * @author Mark Blokpoel
	 */
	private class EAFWriteThread extends Thread {
		
		private File[] domain_files;
		int saveAsType;
		
		public EAFWriteThread(int saveAsType) {
			super();
			this.saveAsType = saveAsType;
			domain_files = domainPanel.getSearchFiles();
		}
		
		@Override
		public void run() {
			if(domain_files != null && domain_files.length>0) {
                float perFile = 100 / (float) domainPanel.getSearchFiles().length;
                int count = 0;
                
				for(File file:domain_files) {
					if (file == null) {
                        //threadProgressUpdated(this, (int) (++count * perFile),ElanLocale.getString("MFE.SavingChanges")+" ("+file.getName()+")");
                        threadProgressUpdated(this, (int) (++count * perFile),ElanLocale.getString("MFE.SavingChanges"));

                        continue;
					}
					String fileName = file.getAbsolutePath();
                    fileName = FileUtility.pathToURLString(fileName).substring(5);
                    if(parseErrorSet.contains(fileName)) {
						continue;
					}
					
					System.out.println("INFO: "+file.getName());
					/* Open the transcription */
					TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());
					
					/* Perform changes on the transcription */
					// change type properties
					System.out.println("DEBUG: Linguistic types");
					List<LinguisticType> types = trans.getLinguisticTypes();
					for(LinguisticType type:types) {
						MFEModel.Changes change = model.getTypeChangeByOriginalName(type.getLinguisticTypeName());
						System.err.println("DEBUG: "+type.getLinguisticTypeName()+"."+change);
						if(change!=null) {
							switch(change) {
							case NEW:
							case NEW_MODIFIED:
								LinguisticType new_type = model.getTypeByOriginalName(type.getLinguisticTypeName());
								trans.addLinguisticType(new_type);
								break;
							case MODIFIED:
								LinguisticType updated_type = model.getTypeByOriginalName(type.getLinguisticTypeName());
								type.setLinguisticTypeName(updated_type.getLinguisticTypeName());
								break;
							case REMOVED:
								// not possible to remove types
								break;
							case NONE:
							default:
								// do nothing
								break;
							}
						}
					}

					int type_index = 0;
					for (LinguisticType type:model.getTypes()) {
						if (model.getTypeChange(type_index) == Changes.NEW ||
								model.getTypeChange(type_index) == Changes.NEW_MODIFIED) {
							System.err.println("DEBUG: New type " + type.getLinguisticTypeName());
							if (trans.getLinguisticTypeByName(type.getLinguisticTypeName()) == null) {
								// type does not exist in this file, add it
								trans.addLinguisticType(type);
								System.err.println("DEBUG: Unknown type " + type.getLinguisticTypeName());
							}
						}
						type_index++;
					} 
	                   
					// change tier properties
					System.out.println("DEBUG: Tiers");
					List<TierImpl> tiers = trans.getTiers();
					List<TierImpl> toBeRemoved = new ArrayList<TierImpl>();
					for(TierImpl tier:tiers) {
						MFEModel.Changes change = model.getTierChangeByOriginalName(tier.getName());
						System.err.println("DEBUG: "+tier.getName()+"."+change);
						switch(change) {
						case MODIFIED:
							TierImpl new_tier = model.getTierByOriginalName(tier.getName());
							if(new_tier!=null) {
								tier.setName(new_tier.getName());
								String annotator = new_tier.getAnnotator();
								if(!annotator.contains(",")) {
									tier.setAnnotator(annotator);
								}
								String participant = new_tier.getParticipant();
								if(!participant.contains(",")) {
									tier.setParticipant(participant);
								}
								LinguisticType new_type = new_tier.getLinguisticType();
								if(!new_type.getLinguisticTypeName().equals(ElanLocale.getString("MFE.Multiple"))) {
									if(trans.getLinguisticTypeByName(new_type.getLinguisticTypeName())==null) {
										// type does not exist in this file, add it
										trans.addLinguisticType(new_type);
									}
									tier.setLinguisticType(new_type);
								}
								String new_lang = new_tier.getLangRef();
								if (new_lang == null) {
									tier.setLangRef(null);
								} else if(!new_lang.contains(",")) {
									tier.setLangRef(new_lang);
								}
								
							}
							break;
						case REMOVED:
							toBeRemoved.add(tier);
							break;
						case NONE:
						default:
							break;
						}
					}
					for (TierImpl t : toBeRemoved) {
						trans.removeTier(t);
					}
					int tier_index = 0;
					for(TierImpl tier:model.getTiers()) {
						if(model.getTierChange(tier_index)==Changes.NEW ||
							model.getTierChange(tier_index)==Changes.NEW_MODIFIED) {
							if (trans.getTierWithId(tier.getName()) == null) { /* HS don't add a new tier if it is already there */
								LinguisticType type = tier.getLinguisticType();
								System.err.println("DEBUG: New tier "+tier.getName());
								if(trans.getLinguisticTypeByName(type.getLinguisticTypeName())==null) {
									// type does not exist in this file, add it
									trans.addLinguisticType(type);
									System.err.println("DEBUG: Unknown type, adding type "+type.getLinguisticTypeName());
								} /* HS if linguistic type is in the file, check if stereotype is correct */
								else {
									Constraint transType = trans.getLinguisticTypeByName(type.getLinguisticTypeName()).getConstraints();
									Constraint tierType = type.getConstraints();
									
									int transStereotype = -1;
									int tierStereotype = -1;
									
									if(transType != null){
										transStereotype = transType.getStereoType();
									}
									
									if(tierType != null){
										tierStereotype = tierType.getStereoType();
									}
									
									if(transStereotype != tierStereotype){
										System.err.println("DEBUG: Incompatible (stereo) types " + type.getLinguisticTypeName() + " skipping tier");
										tier_index++;
										continue;
									}
								}
								trans.addTier(tier);
							}
						}
						tier_index++;
					}
					
					/* Write changes */
					try {
						ACMTranscriptionStore.getCurrentTranscriptionStore()
							.storeTranscription(trans,null, new ArrayList<TierImpl>(0),
							saveAsType);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::");
					System.out.println("::::: Closing file :::::::::::::::::::::::::::::::::");
					System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::");
					
					threadProgressUpdated(this, (int) (++count * perFile), ElanLocale.getString("MFE.SavingChanges")+" ("+file.getName()+")");
				}
			}
			threadProgressCompleted(this, null);
		}
	}
	
	
    /**
     * Loads unique tier names from the selected files and other EAF data.
     *
     * @author Han Sloetjes, modified by Mark Blokpoel
     * @version 1.2
     */
    private class EAFLoadThread extends Thread {
        /**
         * the list to add tier names to
         */
        //List<String> loadedTierNames = null;
        
        private boolean continue_without_dialog = false;
        private boolean continue_loading = true;
        
        /**
         * Creates a new TierLoadThread instance
         *
         * @param loadedTierNames the list to add names to
         */
        public EAFLoadThread() {
            super();
        }

        /**
         * Parses the files and extracts the tier names.
         */
        @Override
        public void run() {
        	if ((domainPanel.getSearchFiles() != null) && (domainPanel.getSearchFiles().length != 0)) {
        		String fileName = null;
        		float perFile = 100 / (float) domainPanel.getSearchFiles().length;
        		int count = 0;

        		model.clear();
        		continue_without_dialog = false;
        	    List<String> parseErrorList = new ArrayList<String>();

        		for (File file : domainPanel.getSearchFiles()) {
        			if (file == null) {
        				threadProgressUpdated(this, (int) (++count * perFile),ElanLocale.getString("MFE.LoadingFiles"));

        				continue;
        			}
        			if(!continue_loading) {
        				break;
        			}

        			fileName = file.getAbsolutePath();
        			fileName = FileUtility.pathToURLString(fileName).substring(5);

        			try {
        				EAFSkeletonParser parser = new EAFSkeletonParser(fileName,true);
        				parser.parse();
        				/* Iterate through all types in the file */
        				List<LinguisticType> types = parser.getLinguisticTypes();
        				for(LinguisticType type:types) {
        					if(type!=null) {
        						try {
        							int row_nr = model.addOriginalType(type);
        							fireTypeRowAdded(row_nr);
        						} catch(final InconsistentTypeException ite) {
        							final String fileName1 = fileName;
									// GUI stuff must be done on the Event Dispatching Thread.
									invokeAndWait(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(domainPanel,
													ElanLocale.getString("MFE.Loader.InconsistentType.Warning1")+" "+fileName1+" "+
															ElanLocale.getString("MFE.Loader.InconsistentType.Warning2")+" "+
															ite.getInconsistentType().getLinguisticTypeName()+
															" "+ElanLocale.getString("MFE.Loader.InconsistentType.Warning3"),
															ElanLocale.getString("MFE.Loader.InconsistentType.Title"),
															JOptionPane.WARNING_MESSAGE);
										}});
        						}
        					}
        				}

        				// Make sure that we have all languages in these files in our recent collection.
        				List<LanguageRecord> langs = parser.getLanguageRecords();
        				if (langs != null) {
        					RecentLanguages recent = RecentLanguages.getInstance();
        					for (LanguageRecord l : langs) {
        						recent.addRecentLanguage(new LangInfo(l.getId(), l.getDef(), l.getLabel()));
        					}
        				}

        				/* Iterate through all tiers in the file */
        				List<TierImpl> tiers = parser.getTiers();
        				for (TierImpl tier:tiers) {
        					if (tier != null) {
        						String name = tier.getName();
        						String type = tier.getLinguisticType().getLinguisticTypeName();
        						String annotator = tier.getAnnotator();
        						String participant = tier.getParticipant();
        						String parent = tier.getParentTier() == null ? null
        								: tier.getParentTier().getName();
        						String langRef = tier.getLangRef();
        						if (langRef == null) {
        							langRef = "";
        						}

        						int row_nr=-1;
        						try {
        							row_nr = model.addOriginalTier(name, type, annotator, participant, parent, langRef);
        						} catch (final InconsistentChildrenException e) {
        							final String fileName1 = fileName;
									// GUI stuff must be done on the Event Dispatching Thread.
									invokeAndWait(new Runnable() {
										@Override
										public void run() {
											int n;

											if(!continue_without_dialog) {
												final Object[] options = {ElanLocale.getString("MFE.Loader.InconsistentChild.YesDontAsk"),
														ElanLocale.getString("MFE.Loader.InconsistentChild.YesCont"),
														ElanLocale.getString("MFE.Loader.InconsistentChild.NoStop")};
												String message0 = ElanLocale.getString("MFE.Loader.InconsistentChild.Warning1")+
														"\n " + fileName1 + ".\n";
												if(e.getLoadedParents()==null) {
													/* should not have parent */
													message0 += ElanLocale.getString("MFE.Loader.InconsistentChild.Warning3")+" `"+
															e.getChild()+"' "+ElanLocale.getString("MFE.Loader.InconsistentChild.Warning3a");
												} else {
													/* should have (another) parent */
													message0 += ElanLocale.getString("MFE.Loader.InconsistentChild.Warning3")+" `"+
															e.getChild()+"' "+ElanLocale.getString("MFE.Loader.InconsistentChild.Warning3b")+" `"+e.getLoadedParents()+"'.";
												}
												final String title = ElanLocale.getString("MFE.Loader.InconsistentChild.Title");
												final String message = message0;

												n = JOptionPane.showOptionDialog(domainPanel,
														message, title,
														JOptionPane.YES_NO_CANCEL_OPTION,
														JOptionPane.WARNING_MESSAGE,
														null, options, options[2]);
											} else {
												n=JOptionPane.NO_OPTION;
											}
											if(n==JOptionPane.YES_OPTION) {
												model.setRemovableTiers(false);
												continue_without_dialog = true;
											} else if (n==JOptionPane.NO_OPTION) {
												model.setRemovableTiers(false);
											} else {
												model.clear();
												fireTierRowAdded(0);
												continue_loading = false;
												domainPanel.clearSearchFiles();
												//break; done below with a check of continue_loading
											}
										}});
        						}

        						if (!continue_loading) {
    								break;
    							}

    							fireTierRowAdded(row_nr);
        					} 	// tier != null
        				} 		// loop over tiers
        				SwingUtilities.invokeLater(new Runnable() {
        					@Override
        					public void run() {
        						// GUI stuff must be done on the Event Dispatching Thread.
        						tableByTierPanel.initCombobox();
        					}});
        			} catch (ParseException pe) {
        				// pe.printStackTrace();
        				parseErrorSet.add(fileName);
        				parseErrorList.add(fileName);
        			} 

        			threadProgressUpdated(this, (int) (++count * perFile), ElanLocale.getString("MFE.LoadingFiles")+" ("+file.getName()+")");
        		}			// loop over files
        		if (!parseErrorList.isEmpty()) {
        			String message = ElanLocale.getString("MFE.Loader.ParseErrorMessage") + "\n";
        			for (String file : parseErrorList) {
        				message += file + "\n";
        			}
        			String title = ElanLocale.getString("MFE.Loader.ParseErrorTitle");
        			showMessageDialog(message, title);
        		}

        		if(!model.areTiersRemovable()) {
        			String message = ElanLocale.getString("MFE.Loader.TierRemovalDisabled.Warning");
        			String title = ElanLocale.getString("MFE.Loader.TierRemovalDisabled.Title");
        			showMessageDialog(message, title);
        		}

        		threadProgressCompleted(this, null);
        	}
        }

        /**
         * Invoke the runnable on the Event Dispatch Thread.
         * Hide any InterruptedException and InvocationTargetException but print stack traces.
         * 
         * @param runnable
         */
		private void invokeAndWait(Runnable runnable) {
            try {
                SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}

		/**
		 * Show a message dialog on the Event Dispatch Thread.
		 * @param message
		 * @param title
		 */
		private void showMessageDialog(final String message, final String title) {
            invokeAndWait(new Runnable() {
			    @Override
			    public void run() {
					// GUI stuff must be done on the Event Dispatching Thread.
			        JOptionPane.showMessageDialog(domainPanel, message, title, JOptionPane.WARNING_MESSAGE);
			    }});
		}

		/**
		 * Fire the TableRowsInserted (TableModelEvent.INSERT) event, if there was an added row.
		 * 
		 * @param row_nr >=0 for a valid row.
		 */
		private void fireTypeRowAdded(final int row_nr) {
			if (row_nr >= 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Must be done on EDT since it fires Swing events.
						// Probably does not need invokeAndWait().
						tableByTypePanel.rowAdded(row_nr);
				}});
			}
		}

		private void fireTierRowAdded(final int row_nr) {
			if (row_nr >= 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Must be done on EDT since it fires Swing events.
						// Probably does not need invokeAndWait().
						tableByTierPanel.rowAdded(row_nr);
				}});
			}
		}
    }
}
