package mpi.eudico.client.annotator.commands;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Removes a tier from the transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteTiersCommand implements UndoableCommand {
    private String commandName;

    //state 
    //private TierImpl tier;
    private TierImpl[] tiers;
    private List<TierImpl>[] depTiers;
    private ArrayList<DefaultMutableTreeNode>[] annotationsNodes;
    private Map<String, Color>[] colorPrefs;
    private Map<String, Font>[] fontPrefs;

    // receiver
    private TranscriptionImpl transcription;

    /**
     * A command to delete a tier (and depending tiers) from a transcription.
     *
     * @param name the name of the command
     */
    public DeleteTiersCommand(String name) {
        commandName = name;
    }

    /**
     * Adds the removed tier to the transcription.
     */
    @Override
	public void undo() {
        if ((transcription != null) && (tiers != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);

            TierImpl deptier;

            for (int i = 0; i < tiers.length; i++) {
            	//tier = tiers[i];
				if (transcription.getTierWithId(tiers[i].getName()) == null) {
					transcription.addTier(tiers[i]);
				}
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						deptier = depTiers[i].get(j);

						if (transcription.getTierWithId(deptier.getName()) == null) {
							transcription.addTier(deptier);
						}
					}
				}
				if (annotationsNodes[i].size() > 0) {
					transcription.setNotifying(false);

					DefaultMutableTreeNode node;

					if (tiers[i].hasParentTier()) {
						AnnotationRecreator.createAnnotationsSequentially(
								transcription, annotationsNodes[i], true);
					} else {
						for (int j = 0; j < annotationsNodes[i].size(); j++) {
							node = annotationsNodes[i]
									.get(j);
							AnnotationRecreator.createAnnotationFromTree(
									transcription, node, true);
						}
					}

					transcription.setNotifying(true);
				}
				
				// restore preferences ??
	            if (colorPrefs != null) {
	            	Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
	        		if (colors != null) {
	        			colors.putAll(colorPrefs[i]);
	        			
	        			Preferences.set("TierColors", colors, transcription, true);
	        		}
	            }
	            if (fontPrefs != null) {
	            	Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
	            	if (fonts != null) {
	            		fonts.putAll(fontPrefs[i]);
	            		
	            		Preferences.set("TierFonts", fonts, transcription, true);
	            	}
	            }
			}
			setWaitCursor(false);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
            
            
        }
    }

    /**
     * Again removes the tier from the transcription.
     */
    @Override
	public void redo() {
        for (int i = 0; i < tiers.length; i++) {
			//tier = tiers[i];
			if ((transcription != null) && (tiers[i] != null)) {
				transcription.removeTier(tiers[i]);
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						transcription.removeTier(depTiers[i].get(j));
					}
				}
				// again delete preferences
				Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
				if (colors != null) {
					for (TierImpl t : depTiers[i]) {
						colors.remove(t.getName());
					}
					colors.remove(tiers[i].getName());
					//Preferences.set("TierColors", colors, transcription);
				}
				Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
				if (fonts != null) {
					for (TierImpl t : depTiers[i]) {
						fonts.remove(t.getName());
					}
					fonts.remove(tiers[i].getName());
				}
			}
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the tier to remove
     *        (TierImpl)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof TranscriptionImpl) {
            transcription = (TranscriptionImpl) receiver;
        } else {
            return;
        }

        int numTiers = arguments.length;
        
        tiers = new TierImpl[numTiers];
        depTiers = new List[numTiers]; // new List<TierImpl>[numTiers]; // -> Cannot create a generic array of List<TierImpl>
        annotationsNodes = new ArrayList[numTiers];
        colorPrefs = new Map[numTiers];
        fontPrefs = new Map[numTiers];
        
        for (int i = 0; i < numTiers; i++) {
			//tier = (TierImpl) arguments[i];
			tiers[i] = (TierImpl) arguments[i];
			if (tiers[i] != null) {
				
				depTiers[i] = tiers[i].getDependentTiers();
				// first store all annotations
				annotationsNodes[i] = new ArrayList<DefaultMutableTreeNode>();
				List<AbstractAnnotation> annos = tiers[i].getAnnotations();
				Iterator<AbstractAnnotation> anIter = annos.iterator();
				AbstractAnnotation ann;
				while (anIter.hasNext()) {
					ann = anIter.next();
					annotationsNodes[i].add(AnnotationRecreator
							.createTreeForAnnotation(ann));
				}
				// then remove the tiers			
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						transcription.removeTier(depTiers[i].get(j));
					}
				}
				transcription.removeTier(tiers[i]);
				// store preferred colors
				Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
				if (colors != null) {
					colorPrefs[i] = new HashMap<String, Color>(colors.size());
					for (TierImpl t : depTiers[i]) {
						Color col = colors.remove(t.getName());

						if (col != null) {
							colorPrefs[i].put(t.getName(), col);
						}
					}
					Color col = colors.remove(tiers[i].getName());
					if (col != null) {
						colorPrefs[i].put(tiers[i].getName(), col);
					}
				}
				Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
				if (fonts != null) {
					fontPrefs[i] = new HashMap<String, Font>(fonts.size());
					for (TierImpl t : depTiers[i]) {
						Font fon = fonts.remove(t.getName());
						if (fon != null) {
							fontPrefs[i].put(t.getName(), fon);
						}
					}
					Font fon = fonts.remove(tiers[i].getName());
					if (fon != null) {
						fontPrefs[i].put(tiers[i].getName(), fon);
					}
				}
			}
		}
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }
}
